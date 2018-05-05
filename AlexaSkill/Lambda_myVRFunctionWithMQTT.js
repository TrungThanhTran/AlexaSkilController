var mqtt = require('mqtt');
const mqtt_url = 'm13.cloudmqtt.com';
const mqtt_username = 'plongthanh';
const mqtt_password = 'plongthanh';
const getNotifyMiliSecondTimeout = 7000;
var unclearCommand = null;

var helpMessage = "I can control music playback, make a phone call and so on..."
    + "To start play a song, you can say..."
    + "Alexa, ask Panda Voice to play Happy New Year..."
    + "To start play by album, artist, genre or play list, you can say..."
    + "Alexa, ask Panda Voice to play album Thriller..."
    + "To make a phone call by cantact name, you can say..."
    + "Alexa, ask Panda Voice to call John";

exports.handler = function (event, context, callback) {
    try {
        //console.log("=== Start handler function ===");
        //console.log("===event:", JSON.stringify(event, null, 2));
        //console.log("===context:", JSON.stringify(context, null, 2));
        //console.log("===callback:", JSON.stringify(callback, null, 2));
        //console.log("========================");
        
        //Check request source
        if (event.session !== undefined) {
            //Request from Alexa
            
            //Check application ID
            if (event.session.application !== undefined) {
                if (event.session.application.applicationId !== "amzn1.ask.skill.e403f550-c5c6-4da6-9cbe-70559ba0b997") {
                    context.fail("Invalid Application ID");
                }
            } else {
                context.fail("Invalid request: event.session.application is undefined");
            }
            
            //Check session
            if (event.session.new) {
                onSessionStarted({requestId: event.request.requestId}, event.session);
            }
            
            //Check request type           
            if (event.request !== undefined) {
                if (event.request.type === "LaunchRequest") {
                    onLaunch(event.request, event.session,
                        function callback(sessionAttributes, speechletResponse) {
                            context.succeed(buildResponse(sessionAttributes, speechletResponse));
                    });
                } else if (event.request.type === "IntentRequest") {
                    onIntent(event.request, event.session,
                        function callback(sessionAttributes, speechletResponse) {
                            context.succeed(buildResponse(sessionAttributes, speechletResponse));
                    });
                } else if (event.request.type === "SessionEndedRequest") {
                    onSessionEnded(event.request, event.session);
                    context.succeed();
                } else {
                    throw "Invalid event request type from Alexa";
                }
            } else {
                context.fail("Invalid request: event.request is undefined");
            }
        } else {
            //do nothing
        }
    } catch (e) {
        context.fail("Exception: " + e);
    }
};

/**
 * Handle on session started
 */
function onSessionStarted(sessionStartedRequest, session) {
    //console.log("onSessionStarted requestId=" + sessionStartedRequest.requestId
    //    + ", sessionId=" + session.sessionId);
    // add any session init logic here
}

/**
 * Called when the user invokes the skill without specifying what they want.
 */
function onLaunch(launchRequest, session, callback) {
    //console.log("onLaunch requestId=" + launchRequest.requestId
    //    + ", sessionId=" + session.sessionId);
    var cardTitle = "Hello from PandaVoice"
    var speechOutput = "I'm ready to use."
    callback(session.attributes,
        buildSpeechletResponse(cardTitle, speechOutput, "", true));
}

/**
 * Called when the user specifies an intent for this skill.
 */
function onIntent(intentRequest, session, callback) {
    //console.log("onIntent requestId=" + intentRequest.requestId
    //    + ", sessionId=" + session.sessionId);
    var intent = intentRequest.intent;
    var intentName = intent.name;
    
    switch(intentName) {
        case "PlayMusic":
        case "Stop":
        case "Pause":
        case "Resume":
        case "Next":
        case "Previous":
        case "AnswerIncomingCall":
        case "DeclineIncomingCall":
            publishToCloudMQTT(intentName, session, callback);
            break;
        case "PlaySong":
        case "PlayAlbum":
        case "PlayArtist":
        case "PlayGenre":
        case "PlayAList":
        case "PlayAMediaDevice":
        case "CallByContact":
        case "CallByNumber":
        case "VolumeUp":
        case "VolumeDown":
        case "SetVolumeByNumber":
        case "SetVolumeByPercent":
            var param = intent.slots[Object.keys(intent.slots)[0]].value;
            publishToCloudMQTT(intentName + "," + param, session, callback);
            break;
        case "PlaySongOfArtist":
            var param1 = intent.slots.SongName.value;
            var param2 = intent.slots.ArtistName.value;
            publishToCloudMQTT(intentName + "," + param1 + "," + param2, session, callback);
            break;
        case "CallByContactWithPhoneType":
            var param1 = intent.slots.ContactName.value;
            var param2 = intent.slots.PhoneType.value;
            publishToCloudMQTT(intentName + "," + param1 + "," + param2, session, callback);
            break;
        case "PlaySomething":
            var shouldEndSession = false;
            var musicType = intent.slots.MusicType.value;
            var response = "Which " + musicType + " do you want to play?";
            switch (musicType) {
                case "song":
                    unclearCommand = "PlaySong";
                    break;
                case "genre":
                    unclearCommand = "PlayGenre";
                    break;
                case "list":
                    unclearCommand = "PlayAList";
                    break;
                case "album":
                    unclearCommand = "PlayAlbum";
                    break;
                case "artist":
                    unclearCommand = "PlayArtist";
                    break;
                default:
                    response = "Sorry, I miss that. Please try again.";
                    shouldEndSession = true;
                    break;
            }
            callback(session.attributes,
                buildSpeechletResponseWithoutCard(response, helpMessage, shouldEndSession));
            break;
        case "UserAnswerSongName":
        case "UserAnswerAlbumName":
        case "UserAnswerArtistName":
        case "UserAnswerGenreName":
        case "UserAnswerPlaylistName":
        case "UserAnswerNumber":
        case "UserAnswerPhoneType":
            var userAnswer = intent.slots[Object.keys(intent.slots)[0]].value;
            if (unclearCommand !== null) {
                console.log("=== onIntent(): command = " + unclearCommand + "," + userAnswer);
                publishToCloudMQTT(unclearCommand + "," + userAnswer, session, callback);
                unclearCommand = null;
            } else {
                console.log("===unclearCommand = null, and userAnswer = " + userAnswer);
            }
            break;
        case "OpenHelp":
            callback(session.attributes,
                buildSpeechletResponseWithoutCard(helpMessage, "", true));   
            break;
        case "Unknown":
            callback(session.attributes,
                buildSpeechletResponseWithoutCard("Sorry, I miss that. Please try again.", "", true));
            break;
        default:
            callback(session.attributes,
                buildSpeechletResponseWithoutCard("Sorry, I miss that. Please try again.", "", true));
            break;
    }
}

/**
 * Called when the user ends the session.
 * Is not called when the skill returns shouldEndSession=true.
 */
function onSessionEnded(sessionEndedRequest, session) {
    //console.log("onSessionEnded requestId=" + sessionEndedRequest.requestId
    //    + ", sessionId=" + session.sessionId);
    // Add any cleanup logic here
}

/**
 * Build speechlet response
 */
function buildSpeechletResponse(title, output, repromptText, shouldEndSession) {
    return {
        outputSpeech: {
            type: "PlainText",
            text: output
        },
        card: {
            type: "Simple",
            title: title,
            content: output
        },
        reprompt: {
            outputSpeech: {
                type: "PlainText",
                text: repromptText
            }
        },
        shouldEndSession: shouldEndSession
    };
}

/**
 * Build speechlet response without card
 */
function buildSpeechletResponseWithoutCard(output, repromptText, shouldEndSession) {
    return {
        outputSpeech: {
            type: "PlainText",
            text: output
        },
        reprompt: {
            outputSpeech: {
                type: "PlainText",
                text: repromptText
            }
        },
        shouldEndSession: shouldEndSession
    };
}

/**
 * Build response
 */
function buildResponse(sessionAttributes, speechletResponse) {
    return {
        version: "1.0",
        sessionAttributes: sessionAttributes,
        response: speechletResponse
    };
}

/**
 * Response to user
 */
function responseToUser(session, callback, command, deviceNotify) {
    console.log("=== deviceNotify = " + deviceNotify);
    var responseMessage = null;
    var arr = deviceNotify.split(",");
    var notifyCode = arr[0];
    var shouldEndSession = true;
    switch (notifyCode) {
        case "OK":
            responseMessage = "OK";
            break;
        case "VolumeIsSet":
            var newVolume = arr[1];
            responseMessage = "OK, volume is set to " + newVolume;
            break;
        case "VolumeIsSetToMax":
            var newVolume = arr[1];
            responseMessage = "OK, volume is set to " + newVolume + ". This is maximum value.";
            break;
        case "VolumeIsSetToMin":
            var newVolume = arr[1];
            responseMessage = "OK, volume is set to " + newVolume + ". This is minimum value.";
            break;
        case "CommandInvalid":
            responseMessage = "Command is invalid. Please try other command.";
            break;
        case "InternalError":
            responseMessage = "Your command is not performed due to internal device error. Please try again.";
            break;
        case "NotFound":
            if (command === "PlayMusic") {
                responseMessage = "There is no song to play. Please insert CD, DVD, USB or SD card which contains music files, then try again.";
            } else {
                var subject = getSubjectFromCommand(command);
                responseMessage = "Requested " + subject + " is not exist, please try an other " + subject + ".";
            }
            break;
        case "ListEmpty":
            var subject = getSubjectFromCommand(command);
            responseMessage = "Requested " + subject + " is empty, please try an other " + subject + ".";
            break;
        case "MultiSong":
            var count = arr[1];
            responseMessage = "The song of which artist? ";
            for (var i = 0; i < count; i++) {
                responseMessage = responseMessage + arr[2 + i] + ", ";
            };
            var cmdArr = command.split(",");
            var songName = cmdArr[1];
            unclearCommand = "PlaySongOfArtist" + "," + songName;
            shouldEndSession = false;
            break;
        case "MultiPhoneType":
            var count = arr[1];
            responseMessage = "Which phone do you want to call? ";
            for (var i = 0; i < count; i++) {
                responseMessage = responseMessage + arr[2 + i] + ", ";
            };
            
            var cmdArr = command.split(",");
            var contactName = cmdArr[1];
            unclearCommand = "CallByContactWithPhoneType" + "," + contactName;
            shouldEndSession = false;
            break;
        default:
            responseMessage = "Invalid notify code from device";
            console.log("===Invalid notify code: " + notifyCode);
            break;
    }
    console.log("===responseMessage = " + responseMessage);
    callback(session.attributes,
        buildSpeechletResponseWithoutCard(responseMessage, "", shouldEndSession));    
}

/**
 * Get requested subject from command
 */
function getSubjectFromCommand(command) {
    var ret = null;
    var arr = command.split(",");
    var intent = arr[0];
    switch (intent) {
        case "PlaySong":
            ret = "song";
            break;
        case "PlayAlbum":
            ret = "album";
            break;
        case "PlayArtist":
            ret = "artist";
            break;
        case "PlayGenre":
            ret = "genre";
            break;
        case "PlayAList":
            ret = "list";
            break;
        case "PlayAMediaDevice":
            ret = "media device";
            break;
        case "PlaySongOfArtist":
            ret = "song";
            break;
        case "CallByContact":
        case "CallByContactWithPhoneType":
            ret = "contact";
            break;
        default:
            ret = "";
            break;
    }
    return ret;
}

/**
 * Publish command to cloud MQTT
 */
function publishToCloudMQTT(command, session, callback) {
    var mqttpromise = new Promise(function(resolve, reject) { 
    	var client = mqtt.connect({port:18472, host:mqtt_url, username:mqtt_username, password:mqtt_password});		 
    	client.on('connect', function() { // When connected 
    	    // publish command to MQTT server
    	    client.publish('command', command, function() { 
        		console.log("Command is published. Command = " + command); 
        		client.end();  
        		resolve('Done Sending...'); 
    	    }); 
    	}); 
    }); 
    
    mqttpromise.then(function(result) {
        //after publish command, wait for device notify and handle it.
        waitDeviceNotify(session, callback, command);
    });
}

/**
 * Wait for device notify and handle it.
 */
function waitDeviceNotify(session, callback, command) {
    var Promise = require('promise'); 
    var mqttpromise = new Promise( function(resolve,reject){ 
        var timeoutObject;
        var client = mqtt.connect({port:18472, host:mqtt_url, username:mqtt_username, password:mqtt_password});			 
        client.subscribe('deviceNotify', function() { 
            client.on('message', function(topic, message, packet) { 
    	        console.log("Received '" + message + "' on '" + topic + "'"); 
    	        //console.log("===message:", JSON.stringify(message, null, 2));
    	        var deviceNotify = message.toString('utf8');
                if (deviceNotify !== null) {
                    responseToUser(session, callback, command, deviceNotify);
                    clearTimeout(timeoutObject);
                } else {
                    console.log("deviceNotify = null"); 
                }
    	        client.end();
            }); 
        });
        
        //set timeout to check notify
        timeoutObject = setTimeout(function(){
            callback(session.attributes,
                        buildSpeechletResponseWithoutCard("Device notify timeout!", "", true));
        }, getNotifyMiliSecondTimeout);
    }); 

    mqttpromise.then(function(result){ 
        //do nothing
    });
}