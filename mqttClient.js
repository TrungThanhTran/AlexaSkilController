// PRDCV mqttClient author: TRAN TRUNG, LONGTHANHPHAN, CUONGTIENNGUYEN 
var mqtt = require('mqtt');
var net = require('net');
const mqtt_url = 'm13.cloudmqtt.com';
const mqtt_username = 'plongthanh';
const mqtt_password = 'plongthanh';
var Promise = require('promise'); 

// socket address
var HOST = '127.0.0.1';
var PORT = 6969;

var clientSocket = new net.Socket();
var client;

var mqttpromise = new Promise(function(resolve,reject) { 
    console.log("Start MQTT Client");
    connectSocket();
    client = mqtt.connect({port:18472, host:mqtt_url, username:mqtt_username, password:mqtt_password});
    client.subscribe('command', function() { 
            client.on('message', function(topic, message, packet) { 
            console.log("Received '" + message + "' on '" + topic + "'");  
            communicate(message);         
        });  
    }); 
}); 

mqttpromise.then(function(result){ 
    //Do anything after 
})

function sendNotification(notifyContent) {
            // publish a message to a topic 
            console.log("notify = " + notifyContent);
            client.publish('deviceNotify', notifyContent, function() {            
            });       
}

function communicate(request)
{        
         clientSocket.write(request + '\n');        
}

clientSocket.on('data', function(data) 
{   
		//Publish notification to MQTT server
		var da = data.toString('utf8');
		var textChunk = da.substring(2, da.length);
		console.log('notify length = ' + da.length);
		sendNotification(textChunk);		
});

function connectSocket()
{
	clientSocket.connect(PORT, HOST, function() 
	{ 
                console.log('CONNECTED TO: ' + HOST + ':' + PORT);	
	});
}