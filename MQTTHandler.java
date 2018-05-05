/* author: TRAN TRUNG */
package com.amazon.alexa.avs;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MQTTHandler {

        private static NLPProcess nplProcessor; 
	private static ServerSocket server;
	private static Socket client;
 	private static InputStream in;
	private static DataOutputStream output;
	private static final Logger log = LoggerFactory.getLogger(MQTTHandler.class); 

	public void initMQTTHandler (int Port)
	{
		try {
			server = new ServerSocket(Port);
			client = server.accept();
			in = client.getInputStream();
			nplProcessor = new NLPProcess();
			output = new DataOutputStream(client.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void communicateNodeJS() {		
		String status = "";		
		try 
		{		
				if (in.available() > 0)
				{
					String inputString = MQTTHandler.inputStreamAsString(in);
					String resStatus = nplProcessor.handleResponse(inputString); 
					log.info("Status response to mqtt = " + resStatus);			
					output.writeUTF(resStatus);
					output.flush();	
				}
				
		} 
	        catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String inputStreamAsString(InputStream stream)
			throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(stream));
		StringBuilder sb = new StringBuilder();
		String returnStr = "";
		returnStr = br.readLine();
		if (returnStr != null)
		{
			log.info("Read mqtt command = " + returnStr);
		}		
		return returnStr;
	}
}