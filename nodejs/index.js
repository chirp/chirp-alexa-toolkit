const Alexa = require('alexa-sdk');

/**
* Chirp Credentials and Alexa application ID
**/
const APP_KEY = 'MY_APP_KEY';
const APP_SECRET = 'MY_APP_SECRET';
const ALEXA_APP_ID = 'MY_ALEXA_APP_ID';

/**
 * WiFi Credentials
 */
const WIFI_SSID = 'MY_WIFI_SSID';
const WIFI_PASSWORD = 'MY_WIFI_PASSWORD';

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

const handlers = {
    'connectToWiFi' : function() {
        //build response first using responseBuilder and then emit
        const speechOutput = 'Check it out!';
        const behavior = 'REPLACE_ALL';
        const token = buildCredentialsPayload(WIFI_SSID, WIFI_PASSWORD);
        const url = 'https://'+APP_KEY+':'+APP_SECRET+'@audio.chirp.io/v3/default/' + token;
        const expectedPreviousToken = null;
        const offsetInMilliseconds = 0;
        this.response.speak(speechOutput)
                    .audioPlayerPlay(behavior, url, token, expectedPreviousToken, offsetInMilliseconds);
        this.emit(':responseReady');
    },
    'Unhandled': function() {
        this.response.speak('What do you want me to do with chirp connect?')
                    .listen('Sorry, I didn\'t get that. Try asking chirp connect to send wi-fi credentials.');
        this.emit(':responseReady');
    }
};

exports.handler = function(event, context, callback) {
    const alexa = Alexa.handler(event, context, callback);
    alexa.appId = ALEXA_APP_ID; // ALEXA_APP_ID is your skill id which can be found in the Amazon developer console where you create the skill.
    alexa.registerHandlers(handlers);
    alexa.execute();
};