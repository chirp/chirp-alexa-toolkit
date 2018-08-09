import codecs

# Chirp Credentials and Alexa application ID
APP_KEY = "MY_APP_KEY"
APP_SECRET = "MY_APP_SECRET"
ALEXA_APP_ID = "MY_APP_ID"

# WiFi Credentials
WIFI_SSID = "MY_WIFI_SSID"
WIFI_PASSWORD = "MY_WIFI_PASSWORD"


# --------------- Helpers that build the response responses ----------------------
# OutputSpeech object is used for setting both the outputSpeech and the re-prompt properties.
def build_output_speech(text=""):
    return {
            "type": "SSML",
            "ssml": "<speak>" + text + "</speak>"
        }


# Card object containing a card to render to the Amazon Alexa App.
def build_card(type="Simple", title="", content=""):
    return {
            "type": type,
            "title": title,
            "content": content
        }


# A directive is specifying device-level actions to take using a particular interface, such as the AudioPlayer interface
# AudioPlayer.Play is a directive that
# sends Alexa a command to stream the audio file identified by the specified audioItem.
def build_play_directive(playBehavior="REPLACE_ALL", url="", stream_token="identifier"):
    return {
        "type": "AudioPlayer.Play",
        "playBehavior": playBehavior,
        "audioItem": {
          "stream": {
            "url": url,
            "token": stream_token,
            "offsetInMilliseconds": 0
          }
        }
      }


def build_response(session_attributes={}, output_speech={}, card={}, directives=[], shouldEndSession=True):
    return {
        "version": "1.0",
        "sessionAttributes": session_attributes,
        "response": {
            "outputSpeech": output_speech,
            "card": card,
            "directives": directives,
            "shouldEndSession": shouldEndSession
        }
    }


# --------------- Handler function that is triggered by Alexa ----------------------
def lambda_handler(event, context):
    intent_request = event["request"]
    session = event["session"]
    application_id = session["application"]["applicationId"]
    intent_name = intent_request["intent"]["name"]

    if application_id != ALEXA_APP_ID:
        raise ValueError("Invalid application")

    if intent_name != "connectToWiFi":
        raise ValueError("Invalid intent")

    output_speech = build_output_speech(text="Check it out!")
    card = build_card(title="Chirp Connect sending WiFi credentials",
                      content="Chirp Connect sending WiFi credentials to " + WIFI_SSID + ".")

    payload = (WIFI_SSID + ":" + WIFI_PASSWORD).encode()
    hex_payload = codecs.encode(payload, 'hex-codec').decode()
    # This endpoint will play the payload with the default protocol of your application
    # https://audio.chirp.io/v3/default
    # See more details about Chirp API at https://developers.chirp.io/connect/getting-started/audio-api/
    audio_url = "https://"+APP_KEY+":"+APP_SECRET+"@audio.chirp.io/v3/default/"+hex_payload
    directives = [
        build_play_directive(url=audio_url, stream_token=hex_payload)
    ]
    return build_response(
        output_speech=output_speech,
        card=card,
        directives=directives)
