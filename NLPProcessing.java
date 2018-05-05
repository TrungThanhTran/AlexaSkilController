/* author: TRAN TRUNG */
package com.amazon.alexa.avs;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Scanner;
import java.io.File;

public class NLPProcessing {
	public static final int CALL_PHONE = 1;
	public static final int PLAY_MUSIC = 2;
	public static final int PREV_MUSIC = 3;
	public static final int STOP_MUSIC = 4;
	public static final int INIT_MUSIC = 5;
	public static final int PAUSE_MUSIC = 6;
	public static final int PLAY_SONG = 7;
	public static final int NEXT_MUSIC = 8;
	public static final int QUIT_MUSIC = 9;

	public static final int CALL_ONAIR = 21;
	public static final int MUSIC_ONAIR = 22;
	public static final int RADIO_ONAIR = 23;
	public static final int DUPL_RADIO_MUSIC = 24;

	public static final String CALL = "call";
	public static final String PLAY = "play";
	public static final String NEXT = "next";
	public static final String PREV = "previous";
	public static final String STOP = "stop";
	public static final String INIT = "play music";
	public static final String PAUSE = "pause";

	public static final String PLAY_COMMAND = "play ";
	public static final String CALL_COMMAND = "call ";
	public static final String PHONE_COMMAND = "make phone ";
	public static final String SMS_COMMAND = "send message ";
	public static final String TURN_COMMAND = "turn ";

	public static final String ALBUM_CONTENT = "album";
	public static final String ARTIST_CONTENT = "artist";
	public static final String GENE_CONTENT = "gene";
	public static final String LIST_CONTENT = "list";

	public static final String STATUS_DUPL_MUSIC_RADIO = "on music or radio";	
	public static final String STATUS_DUPL_SONG_ITEM = "as a song or";
	public static final String STATUS_CONFIRM = "OK ALEXA, It is successful to";
	public static final String STATUS_CONFIRM_PLAY = "OK ALEXA, It is successful to play ";
	public static final String STATUS_NOTIFY = "It is duplicate, would you like to";
	
	public static final String PLIST = "/home/pi/Desktop/playlist/";

	public static Robot r;
	public static String[] nameSong;
	public static int isOnAirApp = 0;

	public String handleResponse(String input) {		 
		String resStatus = "";
		try {
			r = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}

		// Simulate
		listFile(PLIST);		
		// End Simulate

			if ((isOnAirApp > 0) && (!input.contains(PLAY_COMMAND)) && (!input.contains(TURN_COMMAND)) 
					     && (!input.contains(CALL_COMMAND)) && (!input.contains(PHONE_COMMAND))) 
			{
				switch (isOnAirApp) 
				{
					case MUSIC_ONAIR:
						if (true == isMusicCommand(input)) 
						{
							int com = isMusicCommandNmb(input);
							boolean ret = inputMusicCommandAnalyze(com);
							if (true == ret)
							{
								resStatus = STATUS_CONFIRM + input;
							}
						}
						break;
					case RADIO_ONAIR:
						// TODO Check Radio
						// resStatus = STATUS_CONFIRM + input;
						break;
				}
			} 
			else 
			{
				if (input.contains(PLAY_COMMAND)) 
				{
					int startIndex = 0;
					int endIndex = 5; // length of "play "
					String contentCommand = input.substring(endIndex - startIndex).trim();
					isOnAirApp = checkDuplicateContenLevel_APP(contentCommand);
					if (isOnAirApp == MUSIC_ONAIR) 
					{
						if ((contentCommand.contains("music") && 5 == contentCommand.length())) 
						{
							playListMusic();
							resStatus = STATUS_CONFIRM + input;
						} 
						else if (contentCommand.contains(ALBUM_CONTENT)) 
						{
							resStatus = checkDuplicateContentLevel_MUSIC(contentCommand,
									ALBUM_CONTENT);													
						} 
						else if (contentCommand.contains(ARTIST_CONTENT)) 
						{
							resStatus = checkDuplicateContentLevel_MUSIC(contentCommand,
									ARTIST_CONTENT);					
						} 
						else if (contentCommand.contains(GENE_CONTENT)) 
						{
							resStatus = checkDuplicateContentLevel_MUSIC(contentCommand,
									GENE_CONTENT);							
						} 
						else if (contentCommand.contains(LIST_CONTENT)) 
						{
							resStatus = checkDuplicateContentLevel_MUSIC(contentCommand,
									LIST_CONTENT);							
						} 
						else 
						{
							playSongMusic(contentCommand);
							resStatus = STATUS_CONFIRM + input;
						}									
					} 
					else if (isOnAirApp == RADIO_ONAIR) 
					{
						inputMusicCommandAnalyze(QUIT_MUSIC);
						// TODO action on Radio
						//resStatus = STATUS_CONFIRM + input					
					} 
					else if (DUPL_RADIO_MUSIC == isOnAirApp)
					{
						resStatus = input + STATUS_DUPL_MUSIC_RADIO;
					}
				} 
				else if (input.contains(CALL_COMMAND)
						|| input.contains(PHONE_COMMAND)) 
				{
					isOnAirApp = CALL_ONAIR;
					inputMusicCommandAnalyze(QUIT_MUSIC);
					//resStatus = STATUS_CONFIRM + input
				}
			}
	return resStatus;
	}
	
	public static void playSongMusic(String input)
	{
		String[] nameSongArray = input.split(" ");
		String latsname = "";
		for (int i = 0; i < nameSongArray.length; i++) {
			if (i == (nameSongArray.length - 1)) {
				latsname += nameSongArray[i];
			} else {
				latsname += nameSongArray[i] + "_";
			}
		}
		try 
		{

			String runa = "vlc " + PLIST + latsname + ".mp3";
			Process p = Runtime.getRuntime().exec(runa);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	public static void playListMusic()
	{
		try 
		{
			Process p = Runtime.getRuntime().exec(
					"vlc " + PLIST);			
			try 
			{
				p.waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String checkDuplicateContentLevel_MUSIC(String input, String inbox) {
		String response = "";
		boolean inItem = checkIfItemContain(input, inbox);
		boolean inSong = checkIfSongContain(input);
		boolean ret = false;
		if ((true == inItem) && (true == inSong)) {
			response = input + STATUS_DUPL_SONG_ITEM + inbox;
		} else if (false == inItem) {
			playSongMusic(input);		
			response = STATUS_CONFIRM_PLAY + input;
		} else if (false == inSong) {
			// TODO Play item
			response = STATUS_CONFIRM_PLAY + input +inbox;
		}
		return response;
	}

	public static int checkDuplicateContenLevel_APP(String input) {
		boolean inMusic = checkIfMusicContain(input);
		boolean inRadio = checkIfRadioContain(input);
		//Simulate
		inMusic = true;
		//End Simulate
		int idApp = 0;
		if ((true == inMusic) && (true == inRadio)) {
			idApp = DUPL_RADIO_MUSIC;
		} else if (false == inMusic) {
			idApp = RADIO_ONAIR;
		} else if (false == inRadio) {
			idApp = MUSIC_ONAIR;
		}
		return idApp;
	}

	public static boolean checkIfItemContain(String input, String inbox) {
		return false;
	}

	public static boolean checkIfMusicContain(String input) {
		return false;
	}

	public static boolean checkIfSongContain(String input) {
		return false;
	}

	public static boolean checkIfRadioContain(String input) {
		return false;
	}

	public static boolean isMusicCommand(String input) {
		boolean check = false;
		if (true == input.contains(PAUSE) || true == input.contains(PLAY)
				|| true == input.contains(NEXT) || true == input.contains(PREV)
				|| true == input.contains(STOP)) {
			check = true;
		}
		return check;
	}

	private static void listFile(String input) {
		File folder = new File(PLIST);
		File[] listOfFiles = folder.listFiles();
		String name = "";
		nameSong = new String[listOfFiles.length];
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].getName().contains(" ")) {
				nameSong = listOfFiles[i].getName().split(" ");
				int count = 0;
				while (nameSong.length > count) {
					if (count == (nameSong.length - 1)) {
						name += nameSong[count];
					} else {
						name += nameSong[count] + "_";
					}
					count++;
				}
				File newfile = new File(listOfFiles[i].getParent() + "/" + name);
				boolean test = listOfFiles[i].renameTo(newfile);
				name = "";
			}
		}
	}

	public static int isMusicCommandNmb(String input) {
		int numCom = 0;
		if (true == input.contains(INIT)) {
			numCom = INIT_MUSIC;
		} else if (true == input.contains(PAUSE)) {
			numCom = PAUSE_MUSIC;
		} else if (true == input.contains(PLAY)) {
			numCom = PLAY_MUSIC;
		} else if (true == input.contains(NEXT)) {
			numCom = NEXT_MUSIC;
		} else if (true == input.contains(PREV)) {
			numCom = PREV_MUSIC;
		} else if (true == input.contains(STOP)) {
			numCom = STOP_MUSIC;
		}

		else {
			// later
		}
		return numCom;
	}

	public void dialHandle(String input) {
		// TODO handle dial
	}

	private static boolean inputMusicCommandAnalyze(int num) {

		boolean ret = false;
		int keyCodeControl = -1;
		int keyCodeControl_se = -1;
		int keyCodeDirect = -1;
		
		switch (num) {
		case PLAY_MUSIC:
			keyCodeControl = KeyEvent.VK_ALT;
			keyCodeDirect = KeyEvent.VK_L;
			ret = true;
			break;
		case PAUSE_MUSIC:
			keyCodeControl = KeyEvent.VK_ALT;
			keyCodeDirect = KeyEvent.VK_P;
			ret = true;
			break;
		case NEXT_MUSIC:
			keyCodeControl = KeyEvent.VK_SHIFT;
			keyCodeDirect = KeyEvent.VK_N;
			ret = true;
			break;
		case PREV_MUSIC:
			keyCodeControl = KeyEvent.VK_SHIFT;
			keyCodeDirect = KeyEvent.VK_P;
			ret = true;
			break;
		case STOP_MUSIC:
			keyCodeControl = KeyEvent.VK_ALT;
			keyCodeDirect = KeyEvent.VK_S;
			ret = true;
			break;
		case QUIT_MUSIC:
			keyCodeControl = KeyEvent.VK_ALT;
			keyCodeControl_se = KeyEvent.VK_CONTROL;
			keyCodeDirect = KeyEvent.VK_SLASH;
			ret = true;
		}
		if (true == ret) {
			if (keyCodeControl_se < 0)
			{
				r.keyPress(keyCodeControl);
				r.keyPress(keyCodeDirect);
				r.keyRelease(keyCodeDirect);
				r.keyRelease(keyCodeControl);
			}
			else
			{
				r.keyPress(keyCodeControl);
				r.keyPress(keyCodeControl_se);
				r.keyPress(keyCodeDirect);
				r.keyRelease(keyCodeDirect);
				r.keyRelease(keyCodeControl_se);
				r.keyRelease(keyCodeControl);
			}			
		}		
		return ret;
	}
}
