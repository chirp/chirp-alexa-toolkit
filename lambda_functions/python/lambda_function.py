#Make python to look for dependencies in packages folder
import sys
sys.path.insert(0, './packages')

#import dependencies
import codecs, requests
from ask_sdk_core.skill_builder import SkillBuilder
from ask_sdk_core.dispatch_components import AbstractRequestHandler
from ask_sdk_core.utils import is_request_type, is_intent_name
from ask_sdk_core.handler_input import HandlerInput
from ask_sdk_model import Response
from ask_sdk_model.interfaces.audioplayer import PlayDirective, AudioItem, Stream
from ask_sdk_model.ui import SimpleCard, PlayBehavior
from ask_sdk_core.dispatch_components import AbstractExceptionHandler

sb = SkillBuilder()


# Chirp Credentials and Alexa application ID
APP_KEY = "MY_APP_KEY"
APP_SECRET = "MY_APP_SECRET"
ALEXA_APP_ID = "MY_APP_ID"

# WiFi Credentials
WIFI_SSID = "MY_WIFI_SSID"
WIFI_PASSWORD = "MY_WIFI_PASSWORD"

# --------------- Authenticate to ChirpAudioAPI ----------------------
# This will return a short lime living token that we can use to play audio file. After 30s it will expire.
def authenticate() -> Response:
    url = 'https://auth.chirp.io/v3/connect/token'
    return requests.get(url, auth=(APP_KEY, APP_SECRET))

class ConnectToWiFiIntentHandler(AbstractRequestHandler):
    def can_handle(self, handler_input):
        return is_intent_name("connectToWiFi")(handler_input)

    def handle(self, handler_input) -> Response:

        auth_response = authenticate()

        if auth_response.status_code != 200:
            handler_input.response_builder.speak("Sorry. I am unable to authenticate for Chirp API!").set_card(
                SimpleCard("Failed to send WiFi credentials!", "Unable to authenticate for Chirp API!"))\
                .set_should_end_session(True)
        else:
            speech_text = "Check it out!"
            auth_token = auth_response.json()['token']

            payload = (WIFI_SSID + ":" + WIFI_PASSWORD).encode()
            hex_payload = codecs.encode(payload, 'hex-codec').decode()
            # This endpoint will play the payload with the default protocol of your application
            # https://audio.chirp.io/v3/default
            # See more details about Chirp API at https://developers.chirp.io/connect/getting-started/audio-api/
            audio_url = "https://audio.chirp.io/v3/default/" + hex_payload + "?token=" + auth_token

            handler_input.response_builder.speak(speech_text).set_card(
                SimpleCard(speech_text, "Credentials are being sent.")).add_directive(
                    PlayDirective(
                        play_behavior=PlayBehavior.REPLACE_ALL,
                        audio_item=AudioItem(
                            stream=Stream(
                                token=auth_token,
                                url=audio_url,
                                offset_in_milliseconds=0,
                                expected_previous_token=None)))
            ).set_should_end_session(True)

        return handler_input.response_builder.response

class AllExceptionHandler(AbstractExceptionHandler):

    def can_handle(self, handler_input, exception):
        # type: (HandlerInput, Exception) -> bool
        return True

    def handle(self, handler_input, exception):
        # type: (HandlerInput, Exception) -> Response

        speech = "Sorry, I didn't get it. Can you please say it again!!"
        handler_input.response_builder.speak(speech).ask(speech)
        return handler_input.response_builder.response

sb.add_request_handler(ConnectToWiFiIntentHandler())
sb.add_exception_handler(AllExceptionHandler())

handler = sb.lambda_handler()
