# AlexaSkilController 23/12/2016 

I would like to make an application which use Amazon Alexa Skill to control VLC application on raspberry pi
VR by Alexa is a system communicate some components:
 
1.	Alexa Skills: Amazon Alexa cloud service is to communicate between Amazon Echo Dot and LambdaVRFunction, first user talk command to Amazon Echo Dot.
2.	lambdaVRFunction: Lambda service is to handle command from Alexa Skills and communicate with CloudMQTT.
3.	SetupCuteCat: CloudMQTT is to communicate between VR Control module and lambdaVRFunction
4.	VR control module: this is a client Device (Pi2 board) to handle command from CloudMQTT.

Alexa skills, lambdaVRFunction, CloudMQTT is server components, VR control module is a client component. For setup server components, with communicate like that, you need to setup components step by step:
1.	Step 1: Setup CloudMQTT (CuteCat) 
2.	Step 2: Build source code lambdaVRFunction
3.	Step 3: Installation Alexa Skills
4. Step 4: Copy java files to ../samples/javaclient/src/main/java/com/amazon/alexa
Amazon Echo Dot is voice-controlled device can recognition voice commands from the user.

Setup CloudMQTT(CuteCat)

MQTT (Message Queuing Telemetry Transport) is the machine-to-machine protocol of the future. It is used for broadcast message from MQTT server to client, use publish/subscribe type. Here is command from Lambda service to VR control module.
To setup MQTT we need to:
•	Go to website https://www.cloudmqtt.com/plans.html, Click Try now for Free  
•	Next, Sign up and Submit your email
•	Next, click Create to create MQTT broker
 
Input Name field, and click to Create button.
•	Next, click Details to go to Manage Users screen 
On Manage Users, set username, password, click Save
On New Rule, choose User you have just created, set name for Topic, set Read Access, Write Access, click Save
•	Then finish setup, you have value of Server, Port, User, Password, use that for create Lambda function 




Build source code lambdaVRFunction 

AWS Lambda is a compute service executes your function automatically, response to events, when Alexa Skills get command from Amazon Echo Dot.
To build lambdaVRFunction, All you need to do is supply your code in one of the languages that AWS Lambda supports (currently support Node.js, Java, C# and Python). And now we write by Node.js language. Please do step by step:
➢	Step 1: Create an AWS Account
To create an AWS account:
•	Open https://aws.amazon.com/, and then choose Create an AWS Account.
•	Follow the online instructions.
Part of the sign-up procedure involves receiving a phone call and entering a PIN using the phone keypad.
Note your AWS account ID, because you'll need it for the next task.

➢	Step 2: Create an IAM User
Services in AWS, such as AWS Lambda, require that you provide credentials when you access them, so that the service can determine whether you have permissions to access the resources owned by that service. To create an IAM user for yourself and add the user to an Administrators group you need:
1.	Sign in to the Identity and Access Management (IAM) console at https://console.aws.amazon.com/iam/.
2.	In the navigation pane, choose Users, and then choose Add user.
3.	For User name, type a user name, such as Administrator. The name can consist of letters, digits, and the following characters: plus (+), equal (=), comma (,), period (.), at (@), underscore (_), and hyphen (-). The name is not case sensitive and can be a maximum of 64 characters in length.
4.	Select the check box next to AWS Management Console access, select Custom password, and then type the new user's password in the text box. You can optionally select Require password reset to force the user to select a new password the next time the user signs in.
 
5.	Choose Next: Permissions.
6.	On the Set permissions for user page, choose Add user to group.
7.	Choose Create group.
8.	In the Create group dialog box, type the name for the new group. The name can consist of letters, digits, and the following characters: plus (+), equal (=), comma (,), period (.), at (@), underscore (_), and hyphen (-). The name is not case sensitive and can be a maximum of 128 characters in length.
9.	For Filter, choose Job function.
 
10.	In the policy list, select the check box for AdministratorAccess. Then choose Create group.
11.	Back in the list of groups, select the check box for your new group. Choose Refresh if necessary to see the group in the list.
12.	Click Next: Review to see the list of group memberships to be added to the new user. When you are ready to proceed, choose Create user.
13.	Finish create AIM User, go to next Step.

➢	Step 3: Create a Lambda Function
To create a new Lambda function
1.	Sign in to the AWS Management Console and open the AWS Lambda console. URL is:
https://console.aws.amazon.com/lambda/
2.	Choose Get Started Now.
 
Note
The console shows the Get Started Now page only if you do not have any Lambda functions created. If you have created functions already, you will see the Lambda > Functions page. On the list page, choose Create a Lambda function to go to the Lambda > New function page. 
3.	On the Select blueprint page, first select from All runtime to Node.js 4.3. Then, select a facility blueprint for this function, here we choose hello-world. 
4.	On the Configure triggers page, you can optionally choose a service that automatically triggers your Lambda function by choosing the gray box with ellipses (...) to display a list of available services. Now you choose Alexa Skills Kit and click Next .
5.	On the Configure function page, you do: 
•	Enter function name lambdaVRFunction  in Name.
•	Existing role is set to lambda_basic_execution
•	Timeout is set from 3 to 10 sec
•	All another field is leave default value, not change
•	Click Next

6.	Choose Create Function to create a Lambda function. You will have new function with code like this:
 

7.	Prepare source code for function:
•	Open lambdaVRFunction and change Code entry type to Upload a .ZIP file 

Click Upload to upload file
•	You have source code of Lambda function and MQTT library in the file  
SourceCode/ myVRFunctionWithMQTT_pack.zip
Browse to this file and upload it.
•	Click Save.
8.	Change code of function: After upload source file, Code entry type go back to Edit code Inline 
In the code, you should change mqtt_url, mqtt_username, mqtt_password, port to the value you get from Setup CloudMQTT (CuteCat) 

Alexa skills installation
Alexa, the voice service that powers Amazon Echo, provides capabilities, or skills, that enable customers to interact with devices in a more intuitive way using voice. Alexa skills will be created by The Alexa Skills Kit - a collection of self-service APIs, tools, documentation and code samples that make it fast and easy for you to add skills to Alexa. Now you install Alexa skills to communicate Alexa to Lambda.
➢	Step 1:  Create a new Skills

•	Go to https://developer.amazon.com/
•	Login by your account like login to https://aws.amazon.com
•	Click choose ALEXA in the menu
 
•	Next, choose Get Started in board Alexa Skill Kit
 
•	Next, click Add a new Skill

•	Set Name and Invocation Name, please choose an interaction word that is unique and easy to say and hear. 
 
Click Save and Next.

➢	Step 2:  Change Interaction Model
Interaction model is the core of Alexa configuration, there are some type of data field:
1.	Intent Schema: Open file SourceCode/AlexaSkill_sourceCodeFor_InteractionModel.xlsx and copy data from sheet Intent Schema, paste to this text field on website
 
2.	Custom Slot types: 
-	Open file SourceCode/AlexaSkill_sourceCodeFor_InteractionModel.xlsx and sheet Custom Slot Types you will see data on field Type and Values

-	click Add Slot Type button to open Adding slot type panel, copy Type and Values data from Excel file to Enter Type and Enter Values text box
 
-	click Save 
-	click Add Slot Type more to add all data from excel file, total is 4 type and Values.

3.	Sample Utterances: Open file SourceCode/AlexaSkill_sourceCodeFor_InteractionModel.xlsx and copy data from sheet Sample Utterances, paste to this text field on website.
  Click Save and Next.
➢	Step 3:  Configuration with Lambda
On Global Fields panel:
1.	Select AWS Lambda ARN (Amazon Resource Name) on Service Endpoint Type
2.	Select North America check box
3.	Open lambdaVRFunction on AWS Lambda Console, and copy ARN value on top right of the panel
 
Example: arn:aws:lambda:us-east-1:439086322720:function:lambdaVRFunction
4.	Paste ARN value to North America text box of Global Fields panel
 
5.	Click Save and Next.

➢	Step 4:  Publishing Information
1.	Select Publishing Information on the left menu
 
2.	On Global Fields:
-	Select the Category to Connected Car
-	Set to Testing Instructions value: You can tell: Alexa, ask holiday light for something

3.	Input some text to another field: Short Skill Description, Full Skill Description, Example Phrases (All 3 lines Example phrases)
 
4.	Input some text to field keywords
5.	Upload images icon 108 x 108 pixel to Small Icon and upload images icon 512 x 512 pixel to Larger Icon

6.	Click Save and Next.

➢	Step 5:  Setup Privacy & Compliance
1.	Select Privacy & Compliance on the left menu
 
2.	On Privacy: choose all to No, and choose check box on Compliance 
 
3.	Click Save and Submit for Certification. Then choose Yes. 

Alexa Client application setup on Raspberry Pi
Overview	
This guideline provides user a walkthrough to build a hands-free Alexa Voice Service (AVS) prototype on Raspberry Pi 2 model B. The instructions include the setup for AVS Client which works with full functional like Alexa Echo Dot of Amazon. Follow the Alexa Developer GitHub site, it demonstrates how to access and test AVS using our Java sample app (running on a Raspberry Pi), a Node.js server, and a third-party wake word engine. You will use the Node.js server to obtain a Login with Amazon (LWA) authorization code by visiting a website using your Raspberry Pi's web browser.
Additionally, our package also offers users an ability to control music application (VLC media player) and a Bluetooth Hands-Free Profile server application(HFP) as well.  The control module has recently attached as a part of Java sample app of Alexa Amazon GitHub. The application communicates with VLC media player by simulating Global Hot Keys but transfers control commands to HFP using Socket Interposes method. At this time, Amazon Voice Service has not provided text-response ability but audio files. Fortunately, Amazon Cloud Service makes other open services available for developer

Setup VLC media player on Raspberry Pi
Interface:
 
- Playlist and Instances: Allow only one instance
Set Volume to default start 100%
 
Hotkeys: Global keys
- Pause only: Alt+p
- Play only: Alt+l
- Next: Shift + n
- Previous: Shift + p
- Stop: Alt+s
- Quit: Ctrl + Alt + /
- Volume Up: Shift + !
- Volume Down: Shift + @
 
#Communication between Java Client App and Amazon Web Service
Client java connects to Amazon Web Service through 2 ways: API Gateway and Mqtt as described in Overview.
API Gateway action is like a http web server, so it is easy for Java Client to communicate with it using http2 protocol; but API Gateway is impossible to push command direct to our Java client. Java client has to do request timely 1 second to get control command from AWS. The action makes Java client takes a lot of resource. Other solution is to use Mqtt. Mqtt is able to push control commands directly to Java Client just in time it gets data from AVS. In the other hand, Mqtt works with protocol: Subscribe/Publish that is not like a piece of cake for Java to build up.  Providentially, Nodejs supports Mqtt enough to communicate with Amazon Mqtt server. MqttClient on Raspberry Pi is initialized following this step:
Install nodejs module: sudo npm install module_name
Modle_name includes ‘net’, ‘mqtt’ and ‘promise’. Run Java Alexa Client and then open terminal and run:
sudo nodejs mqttClient.js

Run pre-setup applications
 It is convenient to start all applications just in one-click, that is the reason why we make a script call Alexa_demo.sh which is to open the client version 1.1.
To run the script, open Terminal then type: ./Alexa_demo.sh
The content of script is:
echo "################# ALEXA CLIENT STARTING #################"
(cd ~/Alexa/alexa-avs-sample-app-master/samples/companionService && npm start) &
(cd ~/Alexa/alexa-avs-sample-app-master/samples/javaclient && mvn exec:exec) &
(sleep 40s && cd ~/Alexa/alexa-avs-sample-app-master/samples/mqttClient && sudo nodejs mqttClient.js) &
((cd ~/nohands_21_01_2017/data && sudo hfconsole) & (sleep 10 && cd ~/nohands_21_01_2017/data && sudo python test_socket.py))
It is necessary to add sleep in script in order to mqttClient.js and socket.py, the Socket clients of Java Sample Application, are able to run without any IOException.

Important link: https://github.com/alexa/alexa-avs-sample-app/wiki/Raspberry-Pi

