### How To: Passwordless Wi-Fi sign-in with the Chirp Alexa Toolkit

### Introduction

In a [post for the AWS Startups Blog last year](https://aws.amazon.com/blogs/startups/send-data-over-voice-using-alexa-with-the-chirp-toolkit/), we described how to build a Chirp-enabled Alexa skill with the Chirp Alexa Toolkit. Since then, we have launched a complete rebuild of the Chirp platform, consisting of improvements to our API, SDKs and websites. During the same time, the Amazon developer interface has been updated with a new skill builder, which obsoletes our earlier post.

In this article, you’ll learn how to build a Chirp-enabled Alexa skill with our new REST API alongside the new Alexa skill builder. This will allow your Alexa-enabled applications to transmit Chirp signals to communicate with any device within hearing range.

We’ll then build a complete example which shows how you can share your Wi-Fi credentials from Alexa to a nearby Android device with Chirp, so that nearby devices are able to sign on to your Wi-Fi network with no need to enter passwords.

### Use Case

To implement this demo, we’ll first need to configure Alexa to play an mp3 audio file from the Chirp API, which embeds your Wi-Fi credentials. On the receiving device, we’ll create a simple Android app which will receive, decode credentials from the Chirp signal, and then connect the device to the Wi-Fi network.

![img](https://cdn-images-1.medium.com/max/1600/1*H8CTbSij3zZ70g9_I6QW1A.png)Connect to WiFi With Alexa

### Requirements

Before starting, make sure you have these requirements installed on your machine:

- Node and npm if you prefer to use Nodejs for lambda function
- Python 2.7 or higher and pip if you prefer to use Python for lambda function
- Lambda function code samples repository <https://github.com/chirp/chirp-alexa-toolkit>
- Chirp developer account. You can sign up at [admin.chirp.io](https://admin.chirp.io)
- Amazon developer account for [Alexa](https://developer.amazon.com/alexa) and AWS Lambda
- Android studio to build the demo app on a device

### Create a new Alexa custom skill

Sign in to <https://developer.amazon.com/>, and navigate to “Alexa” → “Your Alexa Consoles” → “Skills” to open the Alexa Developer skills page. On the next page, click on the “Create Skill” button to begin creating a new skill.

![img](https://cdn-images-1.medium.com/max/1600/1*NxRSZFCVyCWIAB3wo342kw.jpeg)Creating Alexa skill - step 1

Here we need to configure our skill name and skill language. In order for your Alexa to understand your skill, you have to select the language that your Alexa was configured with. It is very important that you select “English UK” if you are using UK version of Alexa, and US accordingly if you are using the US Alexa version.

![img](https://cdn-images-1.medium.com/max/1600/1*XwAG3YCFscTQdUD35YJ5Ug.jpeg)Creating Alexa skill — Select Title and Language

Next select a custom skill and click the “Create Skill” button.

![img](https://cdn-images-1.medium.com/max/1600/1*_U6muJpHx7YnDwJ752DqHQ.jpeg)Creating Alexa skill — Choose a model

### Configure your newly created skill

From the left menu, click on the Invocation button to change the Skill Invocation Name. For our use case, we’ll use “chirp connect” as an invocation name. The invocation name is used by Alexa to start interacting with your skill. It should be two or more words, and can contain only lowercase alphabetic characters, spaces, and possessive apostrophes.

![img](https://cdn-images-1.medium.com/max/1600/1*RxNfvycTNUk2DU6f9TDxFw.jpeg)Configure Alexa skill — Choose invocation name

Next, we need to create an intent that will represent the “connectToWiFi” action. From the left menu click on the “Add Intents” button, enter the intent name, and click on the “Create Custom Intent” button. If your use case is different from ours, you may want to select one of the existing intents from Alexa’s built-in library or create a different custom intent.

![img](https://cdn-images-1.medium.com/max/1600/1*Eyr0rSm-nXE439FRfjvstA.jpeg)Configure Alexa skill — Create custom intent

Create intent utterances. Utterances represent the phrases that the user will need to ask Alexa to interact with the skill.

![img](https://cdn-images-1.medium.com/max/1600/1*1qxVoAFNg63Ussx072WAFw.jpeg)Configure Alexa skill — Add intent utterances

In order to be able to play audio files from Chirp API, we need to enable the AudioPlayer interface. From the left menu, click on Interfaces then click on Audio Player toggle to enable it.

![img](https://cdn-images-1.medium.com/max/1600/1*spKNFaFHgVEOjEHShxOr4g.jpeg)Configure Alexa skill — Enable Audio Player interface

Now click on the “Save” button to save the configurations, then go to “Endpoints” tab and copy the Alexa skill ID. We’ll need it in the next step to connect it to a Lambda function.

Next, we need to configure the endpoint that will return a JSON response with instructions for Alexa to play an mp3 file from the Chirp API. For simplicity in this tutorial, we’ll create a Lambda function. However, if you prefer you can set up a dedicated endpoint to serve your responses. Remember that the endpoint needs to be on a secure https connection.

### Create Lambda function

From your Amazon Console, go to the Lambda page and select an appropriate region. At the moment of writing this post, Alexa supports the following regions: US East (N. Virginia) for the NA region, EU (Ireland) for the Europe and India regions, Asia Pacific (Tokyo) for the Far East region. After selecting the region click on the Create Function button to create a new Lambda function.

On the next page select the “Author from Scratch” option to create a new function from scratch, then enter the function name, runtime and the role. Finally, click the “Create Function” button. Because we don’t need any specific permissions for our lambda function, we can select the “Basic Edge Lambda permissions” built-in role.

![img](https://cdn-images-1.medium.com/max/1600/1*0TI2TxiOay4PWYOXwUM4uQ.png)Creating Lambda function — Choosing name and permissions

The first thing we need to do after creating our Lambda function is to connect it to our Alexa Skill. From the Designer tab on the Lambda configuration page select API Gateway trigger.

![img](https://cdn-images-1.medium.com/max/1600/1*0XB1c8FsziiHRIll-i7q3Q.jpeg)Configure Lambda function — Add Alexa trigger

Next, scroll down to configure the Alexa Skill Kit trigger and paste the Skill ID that we previously copied from the Alexa Skill configuration page.

![img](https://cdn-images-1.medium.com/max/1600/1*6e4GmudCOwZGwoyoC7SSzQ.jpeg)Configure Lambda function — set-up trigger skill id

Next, we need to clone code samples for the Lambda function from the C[hirp Alexa Toolkit GitHub repository](https://github.com/chirp/chirp-alexa-toolkit). 

```
git clone git@github.com:chirp/chirp-alexa-toolkit.git
```

Next, before uploading the function, we need to provide credentials for the Chirp API and your local Wi-Fi network. We are providing samples for node.js and python. In order to update credentials, open the index file for your preferred platform and update the credentials constants in the header of the file.

For node.js open nodejs/index.js, for python open python/lambda_function.py, and update the following constants:

- APP_KEY and APP_SECRET — represents Chirp credentials from <https://admin.chirp.io>
- APP_ID — represents your Alexa Skill application id that we copied in previous steps
- WIFI_SSID and WIFI_PASSWORD — represents your WiFi network name and password that you want to connect to. For the sake of this example, we’re assuming the network is secured with WPA2 Personal.

Next, we need to install dependencies and zip the function with dependencies so Lambda will be able to use the function straight away. We are providing a build script that will zip the sources in a format required by Lambda.

```
# For Python run:
./build.sh python
```

```
# For Node.js run:
./build.sh nodejs
```

Now we can upload the function to Lambda and test it. In order to do this, go to the Lambda configuration page, scroll to the “Function Code” tab, choose “Upload a .ZIP file” from “Code entry type”, select the zip file created in the previous step and click the Save button to upload the file. Also, make sure that the Runtime and Handler are set up according to your platform.

![img](https://cdn-images-1.medium.com/max/1600/1*ywoqi8xYQHTRyEnYB46yTw.jpeg)Configure Lambda function — Upload function code

Almost there! The last thing remaining to do is to connect the Alexa skill to Lambda function and test it. To do this, go back to the Alexa Skill configuration page, open Endpoint tab, select “AWS Lambda ARN” from the “Service Endpoint Type” and paste the Lambda ARN (you can find it on Lambda configuration page, top right corner) in the Default Region field. You may also want to add an endpoint for each region but you’ll need to create Lambda function duplicates for each region. After you are finished with the Lambda ARN configuration, click “Save Interfaces” and “Build Model”.

![img](https://cdn-images-1.medium.com/max/1600/1*ExlkPzGSC2AWsQp0TvvHng.jpeg)Configure Alexa skill — Add Lamnda ARN

After saving and building the skill, you should be able to see your skill in your Alexa app on your mobile device. From the menu go to “Skills and Games” then click on “Your Skills” in the top-right corner.

![img](https://cdn-images-1.medium.com/max/1600/1*-UKOTtaTbPV95WaEDlBDrg.jpeg)Check for newly created Alexa skill in your Alexa App

Now you can go to “Test Console” to test the Lambda function. To interact with your skill you should type your skill invocation name and intent utterances. For our use case, we can type something like “open chirp connect and send wifi credentials”.

![img](https://cdn-images-1.medium.com/max/1600/1*FVdesY1p0N1EsFUmdW2kjg.jpeg)Testing Alexa skill in Alexa Simulator

Unfortunately, the Alexa Simulator doesn’t yet support the AudioPlayer directive so you’ll not be able to hear the Chirp signal. However, if you are getting an appropriate response from the Lambda function, go ahead and ask Alexa to send you WiFi credentials.

###  Install the Chirp Wi-Fi Android app

In the [same repository you cloned before](https://github.com/chirp/chirp-alexa-toolkit), you will find the source code for an accompanying Chirp Wi-Fi Android client app, which uses the [Chirp Android SDK](https://developers.chirp.io/docs/getting-started/android) in order to receive credentials sent by Alexa.

Note that, in this demo, we’re not encrypting credentials so they are sent in plain text (as you can see from the Lambda function). This means that any devices that are in the hearing range will be able to hear and decode the credentials using a Chirp-enabled receiving device. For secure transmission, your credentials could be secured within your app with the addition of cryptography such as RSA or AES, along with a key that is unique to your client’s credentials.

In order to install the client app, open the project in Android Studio, open “MainActivity.java”, update the Chirp credentials with your key and secret, and build the app on the phone.

```
/*
You can download licence string and credentials from your admin panel at admin.chirp.io
 */
String KEY = "YOUR_APP_KEY";
String SECRET = "YOUR_APP_SECRET";
String LICENCE = "YOUR_APP_LICENCE";
```

Once you start the app for the first time, you’ll see a pop-up window asking to accept “Audio Record” permissions. These are required forthe  Chirp SDK in order to listen for the credentials sent by Alexa. Please note that Chirp SDK is actually not recording anything —  the audio data from the microphone is processed in real-time and then discarded.

Once you approve the permissions, click on the “Start Listening” button in order for the Chirp SDK to start listening for data. The status should change from “Idle” to “Listening”, and now you can go ahead and ask Alexa to send Wi-Fi credentials:

> “Alexa, open Chirp Connect and send Wi-Fi credentials.”

Alexa should start playing a series of tones that encodes your credentials, which will then be decoded by your device. Once the credentials are received, your device will attempt to connect to your Wi-Fi network.

![img](https://cdn-images-1.medium.com/max/1600/1*AhHpvIMH1WqjLLdEcyKaIQ.jpeg)Chirp Connect to WiFi Demo app