const Alexa = require('ask-sdk-core');
const request = require('request');

/**
* Chirp Credentials and Alexa application ID
**/
const APP_KEY = '';
const APP_SECRET = '';
const ALEXA_APP_ID = '';
const CHIRP_API_AUTH = 'https://'+APP_KEY+':'+APP_SECRET+'@auth.chirp.io/v3/connect/token'

/**
 * WiFi Credentials
 */
const WIFI_SSID = '';
const WIFI_PASSWORD = '';

function hexEncode(utf8String) {
    var hex, i;

    var result = "";
    for (i=0; i<utf8String.length; i++) {
        hex = utf8String.charCodeAt(i).toString(16);
        result += (hex).slice(-4);
    }

    return result
}

function buildCredentialsPayload(ssid, passwd) {
    return hexEncode(ssid + ":" + passwd);
}

const ConnectToWiFiIntentHandler = {
  canHandle(handlerInput) {
    return handlerInput.requestEnvelope.request.type === 'IntentRequest'
      && handlerInput.requestEnvelope.request.intent.name === 'connectToWiFi';
  },
  handle(handlerInput) {
    return new Promise((resolve, reject) => {
      request({url: CHIRP_API_AUTH}, function (error, response_status, body) {
        body = JSON.parse(body)
        const responseBuilder = handlerInput.responseBuilder
        if (!body.token) {
          responseBuilder
            .speak('Sorry. I am unable to authenticate for Chirp API!')
            .withSimpleCard('Failed to send WiFi credentials!', 'Unable to authenticate for Chirp API!');

        } else {
          const wiFiToken = buildCredentialsPayload(WIFI_SSID, WIFI_PASSWORD);
          const url = 'https://audio.chirp.io/v3/default/' + wiFiToken + "?token="+body.token;
          responseBuilder
            .speak('Check it out!')
            .withSimpleCard('Check it out!', 'Credentials are being sent.')
            .addAudioPlayerPlayDirective('REPLACE_ALL', url, wiFiToken, 0);
        }
        resolve(responseBuilder.getResponse())
      });
    })
  }
};

const ErrorHandler = {
  canHandle() {
    return true;
  },
  handle(handlerInput, error) {

    return handlerInput.responseBuilder
      .speak("Sorry, I didn't get it. Can you please say it again!!")
      .reprompt("Sorry, I didn't get it. Can you please say it again!!")
      .getResponse();
  },
};

exports.handler = async function (event, context) {

  skill = Alexa.SkillBuilders.custom()
    .addRequestHandlers(
      ConnectToWiFiIntentHandler
    )
    .addErrorHandlers(ErrorHandler)
    .create();

  const response = await skill.invoke(event, context);

  return response;
};
