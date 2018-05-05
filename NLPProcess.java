/* author: TRAN TRUNG */
package com.amazon.alexa.avs;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Scanner;
import java.io.File;
import java.io.FilenameFilter;
import java.io.*;
import java.net.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NLPProcess {
	public static final int INVALID = 0;
	
	/* Music Id 0 - 14 */
	public static final int RESUME_MUSIC = 2;
	public static final int PREV_MUSIC = 3;
	public static final int STOP_MUSIC = 4;
	public static final int PLAY_MUSIC = 5;
	public static final int PAUSE_MUSIC = 6;
	public static final int PLAY_SONG = 7;
	public static final int NEXT_MUSIC = 8;
	public static final int ALBUM_MUSIC = 9;
	public static final int ARTIST_MUSIC = 10;
	public static final int GENRES_MUSIC = 11;
	public static final int LIST_MUSIC = 12;
	public static final int SONGOFARTIST = 13;
	public static final int AMEDIA_DEVICE = 14;
	
	/* Call Id 40 - 43 */
	public static final int CALL_NUM = 40;
	public static final int CALL_NAME = 41;
	public static final int CALL_ANS = 42;
	public static final int CALL_DEC  =43;
	public static final int CALL_BYTYPE = 44; 

	/* Volume Id 15 - 18 */
	public static final int VOL_UP = 15;
	public static final int VOL_DWN = 16;
	public static final int VOL_NUM = 17;
	public static final int VOL_PER = 18;
	public static int currentVol = -1; 
	public static int currentPercent = -1;   	    
	public static final int INIT_VOL = 20;
	public static final int INIT_PER = 100;
	public static final int MIN_PER = 0;
	public static final int MAX_PER = 125;
	public static final int MIN_VOL = 0;
	public static final int MAX_VOL = 25;
		
	/* Music command */
	public static final String PSONG = "PlaySong,"; 
	public static final String PALBUM = "PlayAlbum,";
	public static final String PARTIST = "PlayArtist,";
	public static final String PGENRE = "PlayGenre,";
	public static final String PSONGOFART = "PlaySongOfArtist,";
	public static final String PALIST = "PlayAList,";
	public static final String PADEVICE = "PlayAMediaDevice,";
	public static final String PMUSIC = "PlayMusic,";
	public static final String STOP = "Stop,";
	public static final String PAUSE = "Pause,";
	public static final String RESUME = "Resume,";
	public static final String NEXT = "Next,";
	public static final String PREV = "Previous,";
	public static final String BYARTIST = "_byartist_";
	/* Volume */
	public static final String VOLUP = "VolumeUp,";
	public static final String VOLDWN = "VolumeDown,";
	public static final String SETVOLNUM = "SetVolumeByNumber,";
	public static final String SETVOLPER = "SetVolumeByPercent,";
	public static final String VOLSET = "VolumeIsSet,";
	public static final String VOLSETTOMAX = "VolumeIsSetToMax,";
	public static final String VOLSETTOMIN = "VolumeIsSetToMin,";

	/* Phone Command */
	public static final String CNUM = "CallByNumber,";
	public static final String CNAME = "CallByContact,";
	public static final String CANS = "AnswerIncomingCall,";
	public static final String CDEC = "DeclineIncomingCall,";
	public static final String CBYTYPE = "CallByContactWithPhoneType,";
	
	public static final String INVALID_COMMAND = "CommandInvalid";
	public static final String LISTPATH = "/home/pi/Alexa/alexa-avs-sample-app-master/samples/javaclient/files/music/";
	public static final String ALBUMPATH = "/home/pi/Alexa/alexa-avs-sample-app-master/samples/javaclient/files/music/albums/";
	public static final String ARTISTPATH = "/home/pi/Alexa/alexa-avs-sample-app-master/samples/javaclient/files/music/artists/";
	public static final String GENREPATH = "/home/pi/Alexa/alexa-avs-sample-app-master/samples/javaclient/files/music/genres/";
	public static final String PLAYLISTPATH = "/home/pi/Alexa/alexa-avs-sample-app-master/samples/javaclient/files/music/playlist/";

	private static final Logger log = LoggerFactory.getLogger(NLPProcess.class);

	public static Robot r;
	public static String[] nameSongList;
	public static String[] nameAlbumList;
	public static String[] nameArtistList;
	public static String[] nameGenreList;
	public static String[] namePlayList;
	public static String[] nameSongOfArtList;	
	public static int countSong = 0;
	public static boolean isPlayingMusic = false;

	public String handleResponse(String input)
	{	
		String requestStatus = "";
		try {
			r = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}
		
		// Get list from location folder

		listFile();
		listAlbum();
		listArtist();
		listGenre();
		listPlayList();
		countSong = 0;

		int com = isDecideCommandNmb(input);
		
		if (com < 20)
		{
			requestStatus = inputMusicCommandAnalyze(com, input);			
		}
		else if (com > 30)
		{
			requestStatus = inputCallCommandHandler(com, input);
			if (true == isPlayingMusic)
			{
				QuitMusic();
				isPlayingMusic = false;
			}
			
			//TODO send to HandFree
			//TODO recieve from HandFree
		} 
		else
		{
			requestStatus = "CommandInvalid";
		}
	
		return requestStatus;	
	}
	
	private static void listAlbum()
	{
		File folder = new File(ALBUMPATH);
		nameAlbumList = folder.list(new FilenameFilter()
		{
			public boolean accept(File current, String name)
			{
				return new File(current, name).isDirectory();
			}
		});
	}
	
	private static void listArtist()
	{
		File folder = new File(ARTISTPATH);
		nameSongOfArtList = new String[4];
		nameArtistList = folder.list(new FilenameFilter()
		{
			public boolean accept(File current, String name)
			{
				return new File(current, name).isDirectory();
			}
		});
		listArtistSongs(ARTISTPATH);		
	}
	
	private static void listArtistSongs(String path)
	{
		File directory = new File(path);
		File[] flist = directory.listFiles();
		for (int i = 0; i < flist.length; i++)
		{
			if (flist[i].isFile())	
			{
				nameSongOfArtList[countSong] = flist[i].getName().toLowerCase();
				countSong++;
			}
			else if (flist[i].isDirectory())
			{
				listArtistSongs(flist[i].getAbsolutePath());
			} 
		}		
	}
	
	private static void listGenre()
	{
		File folder = new File(GENREPATH);
		nameGenreList = folder.list(new FilenameFilter()
		{
			public boolean accept(File current, String name)
			{
				return new File(current, name).isDirectory();
			}
		});
	}
	
	private static void listPlayList()
	{
		File folder = new File(PLAYLISTPATH);
		namePlayList = folder.list(new FilenameFilter()
		{
			public boolean accept(File current, String name)
			{
				return new File(current, name).isDirectory();
			}
		});
	}
	
	private static void listFile()
	{
		File folder = new File(LISTPATH);
		
		File[] listOfFiles = folder.listFiles();
		String name = "";
		nameSongList = new String[listOfFiles.length];
		for (int i = 0; i < listOfFiles.length; i++)
		{
			if (!listOfFiles[i].getName().contains("_") && !listOfFiles[i].getName().contains(" "))
			{
				name = listOfFiles[i].getName().toLowerCase();
				File newfile =new File(listOfFiles[i].getParent() + "/" + name);
				nameSongList[i] = name;
				boolean test = listOfFiles[i].renameTo(newfile);	
				name = "";
			}
			if (listOfFiles[i].getName().contains("_"))
			{
				String[] nameSongParts = listOfFiles[i].getName().split("_");
				int count = 0;
				while (nameSongParts.length > count)
				{ 
					if (count == (nameSongParts.length - 1))
					{
						name += nameSongParts[count].toLowerCase();
					}
					else
					{
						name += nameSongParts[count].toLowerCase() + "_";
					}	
					count++;
				}
				nameSongList[i] = name;
				File newfile =new File(listOfFiles[i].getParent() + "/" + name);
				boolean test = listOfFiles[i].renameTo(newfile);				
				name = "";
			}
			if (listOfFiles[i].getName().contains(" "))
			{
				String[] nameSongParts = listOfFiles[i].getName().split(" ");
				int count = 0;
				while (nameSongParts.length > count)
				{ 
					if (count == (nameSongParts.length - 1))
					{
						name += nameSongParts[count].toLowerCase();
					}
					else
					{
						name += nameSongParts[count].toLowerCase() + "_";
					}	
					count++;
				}
				nameSongList[i] = name;
				File newfile =new File(listOfFiles[i].getParent() + "/" + name);
				boolean test = listOfFiles[i].renameTo(newfile);				
				name = "";
			}				
		}						
	}

	public int isDecideCommandNmb(String input)
	{
		int numCom = INVALID;
		String[] commandControl = input.split(",");
		String tmp = commandControl[0] + ",";
		switch(tmp)
		{
			case PMUSIC:				
				numCom = PLAY_MUSIC;
				break;
			case PALBUM:
				numCom = ALBUM_MUSIC;
				break;
			case PSONG:
				numCom = PLAY_SONG;
				break;
			case PAUSE:
				numCom = PAUSE_MUSIC;
				break;
			case RESUME:
				numCom = RESUME_MUSIC;
				break;
			case NEXT:
				numCom = NEXT_MUSIC;
				break;
			case PREV:
				numCom = PREV_MUSIC;
				break;
			case STOP:
				numCom = STOP_MUSIC;
				break;
			case PARTIST:
				numCom = ARTIST_MUSIC;
				break;
			case PGENRE:
				numCom = GENRES_MUSIC;
				break;
			case PSONGOFART:
				numCom = SONGOFARTIST;
				break;
			case PALIST:
				numCom = LIST_MUSIC;
				break;
			case PADEVICE:
				numCom = AMEDIA_DEVICE;
				break;
			case VOLUP:
				numCom = VOL_UP;
				break;
			case VOLDWN:
				numCom = VOL_DWN;
				break;
			case SETVOLNUM:
				numCom = VOL_NUM;
				break;
			case SETVOLPER:
				numCom = VOL_PER;
				break;
			case CNUM:
				numCom = CALL_NUM;
				break;
			case CNAME:
				numCom = CALL_NAME;
				break;
			case CANS:
				numCom = CALL_ANS;
				break;
			case CDEC:
				numCom = CALL_DEC;
				break;
			case CBYTYPE:
				numCom = CALL_BYTYPE;
				break;
		}
		log.info("numCom = " + numCom);
		return numCom;
	}
	
	private static String inputCallCommandHandler(int inputCommand, String input)
	{
		String status = "";
		try 
		{
			switch(inputCommand) 
			{
				case CALL_NUM:
					status = CallNumHandler(input);
					break;
				case CALL_NAME:
					status = CallNameHandler(input);
					break;
				case CALL_ANS:
					status = CallAccept("Accept");
					break;
				case CALL_DEC:
					status = CallReject("Reject");
					break;
				case CALL_BYTYPE:
					status = CallByType(input);
					break;
			}
		}
		catch(Exception e)
		{
			status = "InternalError";
			e.printStackTrace();
		}	
		return status;
	}
	
	private static String CallReject(String input) throws Exception
	{
		String status = "";
		status = sendCallToHandFreeHandler(input);
		return status;
	}

	private static String CallAccept(String input) throws Exception
	{
		String status = "";
		status = sendCallToHandFreeHandler(input);
		return status;
	}
	
	private static String CallByType(String input) throws Exception
	{
		String phonebyType = "";
		phonebyType = trimString(input, CBYTYPE); 
		phonebyType = "Call " + phonebyType;
		String status = sendCallToHandFreeHandler(phonebyType);
		return status;	
	}
	private static String CallNumHandler(String input) throws Exception
	{
		String phoneNumber = "";
		phoneNumber = trimString(input, CNUM); 
 		phoneNumber = "Call " + phoneNumber;
		String status = sendCallToHandFreeHandler(phoneNumber);
		return status;
	}

	private static String CallNameHandler(String input) throws Exception
	{
		String phoneName = "";
		phoneName = trimString(input, CNAME); 
		phoneName = "Call "  + phoneName;
		String status = sendCallToHandFreeHandler(phoneName);
	
		return status;
	}
	
	private static String inputMusicCommandAnalyze(int num, String input)
	{
	  boolean ret = false;
	  int keyCodeControl = -1;
	  int keyCodeDirect = -1;
	  String status = "";
	  try
	  {
		switch (num)
		{
			case ALBUM_MUSIC: // not in this version
				status = playAlbumHandler(input);
			        break;
			case ARTIST_MUSIC: // not in this version
				status = playArtistHandler(input);
				break;
			case GENRES_MUSIC: // not in this version
				status = playGenreHandler(input);
				break;
			case LIST_MUSIC: // not in this version
				status = playListHandler(input);
				break;
			case SONGOFARTIST: // not in this version
				status = playSongOfArtistHandler(input);
				break;
			case AMEDIA_DEVICE: //not in this version
				status = "NotFound";
				isPlayingMusic = false;
				break; 
			case VOL_UP: 
				if (true == isPlayingMusic)
				{
					status = volumeHandler(input, VOL_UP);
				}
				else 
				{
					status = INVALID_COMMAND;
				}
				break;
			case VOL_DWN:
				if (true == isPlayingMusic)
				{
					status = volumeHandler(input, VOL_DWN);
				}
				else 
				{
					status = INVALID_COMMAND;
				}
				break;
			case VOL_NUM:
				if (true == isPlayingMusic)
				{
					status = volumeHandler(input, VOL_NUM);
				}
				else 
				{
					status = INVALID_COMMAND;
				}
				break;
			case VOL_PER:
				if (true == isPlayingMusic)
				{
					status = volumeHandler(input, VOL_PER);
				}
				else 
				{
					status = INVALID_COMMAND;
				}
				break;
			case RESUME_MUSIC:
				if (true == isPlayingMusic)
				{
					keyCodeControl = KeyEvent.VK_ALT;
					keyCodeDirect = KeyEvent.VK_L;
					ret = true;
				}
				else 
				{
					status = INVALID_COMMAND;
				}
				break;
			case PAUSE_MUSIC:
				if (true == isPlayingMusic)
				{
					keyCodeControl = KeyEvent.VK_ALT;
					keyCodeDirect = KeyEvent.VK_P;
					ret = true;
				}
				else 
				{
					status = INVALID_COMMAND;
				}	        	
				break;
			case NEXT_MUSIC:
				if (true == isPlayingMusic)
				{
					keyCodeControl = KeyEvent.VK_SHIFT;
					keyCodeDirect = KeyEvent.VK_N;
					ret = true;
				}
				else 
				{
					status = INVALID_COMMAND;
				}
				break;
			case PREV_MUSIC:
				if (true == isPlayingMusic)
				{
					keyCodeControl = KeyEvent.VK_SHIFT;
					keyCodeDirect = KeyEvent.VK_P;
					ret = true;
				}
				else 
				{
					status = INVALID_COMMAND;
				}
				break;
			case STOP_MUSIC:
				if (true == isPlayingMusic)
				{
					keyCodeControl = KeyEvent.VK_ALT;
					keyCodeDirect = KeyEvent.VK_S;
					ret = true;
					isPlayingMusic = false;
				}
				else 
				{
					status = INVALID_COMMAND;
				}
				break;
			case PLAY_MUSIC:
				if (0 == nameSongList.length)
				{
					status = "ListEmpty";
					isPlayingMusic = false;
				}
				else
				{
					status = playVLCApplication(LISTPATH);
				}
				ret = false;			
				break;
			case PLAY_SONG:
				if (0 == nameSongList.length)
				{
					status = "ListEmpty";
					isPlayingMusic = false;
				}
				else
				{
					status = playSongHandler(input);
				}
				ret = false;			
				break;					
		}		
		if (true == ret)
		{	
			r.keyPress(keyCodeControl);
			r.keyPress(keyCodeDirect);
			r.keyRelease(keyCodeDirect);
		        r.keyRelease(keyCodeControl);
			status = "OK";
		}		
	  }
	  catch(Exception e)
	  {
		status = "InternalError";
		e.printStackTrace();
	  }	
	  return status;
	}
	
	public static void QuitMusic()
	{
		int keyCodeControl = -1;
		int keyCodeDirect = -1;
		int keyCodeSecond = -1;
		isPlayingMusic = false;

		keyCodeControl = KeyEvent.VK_ALT;
		keyCodeSecond = KeyEvent.VK_CONTROL;
		keyCodeDirect = KeyEvent.VK_S;
		
		r.keyPress(keyCodeControl);
		r.keyPress(keyCodeSecond);
		r.keyPress(keyCodeDirect);			
		r.keyRelease(keyCodeDirect);
		r.keyRelease(keyCodeSecond);
		r.keyRelease(keyCodeControl);				
	}

	public static void PressKey(int keyCodeControl, int keyCodeDirect, int loop)
	{
		r.keyPress(keyCodeControl);
		for (int i = 0; i < loop; i++)
		{				
			r.keyPress(keyCodeDirect);
			r.keyRelease(keyCodeDirect);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}		
		r.keyRelease(keyCodeControl);
	}

	public static String volumeUpHandler(String input)
	{
		int keyCodeControl = -1;
   		int keyCodeDirect = -1;
		String VolNum = trimString(input, VOLUP);
		int volNum = Integer.parseInt(VolNum);
		String status = "";
		keyCodeControl = KeyEvent.VK_SHIFT;
		keyCodeDirect = KeyEvent.VK_EXCLAMATION_MARK;
	   	if ((currentVol + volNum) >= MAX_VOL)
		{
			log.info("currentVol = " + currentVol);
			int loop = MAX_VOL - currentVol;
			PressKey(keyCodeControl, keyCodeDirect, loop);
			currentVol = MAX_VOL;
			currentPercent = MAX_PER;
			status = VOLSETTOMAX + Integer.toString(currentVol);				
		}
		else
		{ 	     
			log.info("currentVol = " + currentVol);   
			int loop = volNum;
			PressKey(keyCodeControl, keyCodeDirect, loop);
			currentVol += volNum;
			currentPercent = currentVol * 5;
		        status = VOLSET + Integer.toString(currentVol);		
		}			
		return status;
	}

	public static String volumeDownHandler(String input)
	{
		int keyCodeControl = -1;
		int keyCodeDirect = -1;
		String VolNum = trimString(input, VOLDWN);
		int volNum = Integer.parseInt(VolNum);
		String status = "";
		keyCodeControl = KeyEvent.VK_SHIFT;
		keyCodeDirect = KeyEvent.VK_AT;
	   	if ((currentVol - volNum) <= MIN_VOL)
		{
			log.info("currentVol = " + currentVol);	
			int loop = currentVol;
			PressKey(keyCodeControl, keyCodeDirect, loop);
			currentVol = MIN_VOL;
			currentPercent = MAX_PER;
			status = VOLSETTOMIN + Integer.toString(currentVol); 			
		}
		else
		{
			int loop = volNum;
			PressKey(keyCodeControl, keyCodeDirect, loop);
               	        currentVol -= volNum;
			currentPercent = currentVol * 5;
		        status = VOLSET + Integer.toString(currentVol);
		}		
		return status;	
	}
	
	public static String volumeNumberHandler(String input)
	{
		int keyCodeControl = -1;
		int keyCodeDirect = -1;
		String VolNum = trimString(input, SETVOLNUM);
		int volNum = Integer.parseInt(VolNum);
		String status = "";
		keyCodeControl = KeyEvent.VK_SHIFT;
	   	if (volNum  >= MAX_VOL)
		{
			keyCodeDirect = KeyEvent.VK_EXCLAMATION_MARK;
			int loop = MAX_VOL - currentVol;
			PressKey(keyCodeControl, keyCodeDirect, loop);
			currentVol = MAX_VOL;
			currentPercent = currentVol * 5;
			status = VOLSETTOMAX + Integer.toString(currentVol); 
		}
		else if (volNum <= MIN_VOL)
		{
			keyCodeDirect = KeyEvent.VK_AT;
			int loop = currentVol - MIN_VOL;
			PressKey(keyCodeControl, keyCodeDirect, loop);
               	        currentVol = MIN_VOL;
			currentPercent = currentVol * 5;
		        status = VOLSETTOMIN + Integer.toString(currentVol);
		}	
		else
		{
			if (currentVol > volNum)
			{
				int loop = currentVol - volNum;
				keyCodeDirect = KeyEvent.VK_AT;
				PressKey(keyCodeControl, keyCodeDirect, loop);
				currentVol = volNum;
				currentPercent = currentVol * 5;
				status = "OK";
			}
			else
			{
				int loop = volNum - currentVol;
				keyCodeDirect = KeyEvent.VK_EXCLAMATION_MARK;
				PressKey(keyCodeControl, keyCodeDirect, loop);
				currentVol = volNum;
				currentPercent = currentVol * 5;
				status = "OK";
			}
		}	
		return status;	
	}

	public static String playAlbumHandler(String input)
	{
		String status = "NotFound";
		isPlayingMusic = false;
		String albumName = trimString(input, PALBUM).toLowerCase();
		String[] albumPart = albumName.split(" ");
		String lastname = "";
		for (int i = 0; i < albumPart.length; i++)
		{										
			if (i == (albumPart.length - 1))
			{
				lastname += albumPart[i];
			}
			else
			{
				lastname += albumPart[i] + "_";
			}
		}

		// check if album name is in list
		for (int i = 0; i < nameAlbumList.length; i++)
		{
			if (nameAlbumList[i].contains(lastname) && (nameAlbumList[i].length() == lastname.length())) // simulate don't care it extension or algorithm
			{				
				String playPath = ALBUMPATH + lastname + "/";
				status = playVLCApplication(playPath);
				break;
			}			
		}		
		return status;
	}

	public static String playArtistHandler(String input)
	{
		String status = "NotFound";
		isPlayingMusic = false;
		String artistName = trimString(input, PARTIST).toLowerCase();
		String[] artistPart = artistName.split(" ");
		String lastname = "";
		for (int i = 0; i < artistPart.length; i++)
		{										
			if (i == (artistPart.length - 1))
			{
				lastname += artistPart[i];
			}
			else
			{
				lastname += artistPart[i] + "_";
			}
		}

		// check if album name is in list
		for (int i = 0; i < nameArtistList.length; i++)
		{
			log.info("artist name = " + nameArtistList[i]);
			if (nameArtistList[i].contains(lastname) && (nameArtistList[i].length() == lastname.length())) // simulate don't care it extension or algorithm
			{				
				String playPath = ARTISTPATH + lastname + "/";
				status = playVLCApplication(playPath);
				break;
			}
		}		
		return status;
	}

	public static String playGenreHandler(String input)
	{
		String status = "NotFound";
		isPlayingMusic = false;
		String genreName = trimString(input, PGENRE).toLowerCase();
		String[] genrePart = genreName.split(" ");
		String lastname = "";
		for (int i = 0; i < genrePart.length; i++)
		{										
			if (i == (genrePart.length - 1))
			{
				lastname += genrePart[i];
			}
			else
			{
				lastname += genrePart[i] + "_";
			}
		}

		// check if genre name is in list
		for (int i = 0; i < nameGenreList.length; i++)
		{
			if (nameGenreList[i].contains(lastname) && (nameGenreList[i].length() == lastname.length())) // simulate don't care it extension or algorithm
			{				
				String playPath = GENREPATH + lastname + "/";
				status = playVLCApplication(playPath);
				break;
			}	
		}		
		return status;
	}

	public static String playListHandler(String input)
	{
		String status = "NotFound";
		isPlayingMusic = false;
		String listName = trimString(input, PALIST).toLowerCase();
		String[] listPart = listName.split(" ");
		String lastname = "";
		for (int i = 0; i < listPart.length; i++)
		{										
			if (i == (listPart.length - 1))
			{
				lastname += listPart[i];
			}
			else
			{
				lastname += listPart[i] + "_";
			}
		}

		// check if genre name is in list
		for (int i = 0; i < namePlayList.length; i++)
		{
			if (namePlayList[i].contains(lastname) && (namePlayList[i].length() == lastname.length())) // simulate don't care it extension or algorithm
			{				
				String playPath = PLAYLISTPATH + lastname + "/";
				status = playVLCApplication(playPath);
				break;
			}	
		}		
		return status;
	}

	private static String playSongOfArtistHandler(String input)
	{
		String playPath = "";
		String status = "NotFound";
		isPlayingMusic = false;
		String contentshort = trimString (input, PSONGOFART).toLowerCase();
		String[] content = contentshort.split(",");

		String nameSong = content[0];
		String artistName = content[1];
                String[] listArtistPart = artistName.split(" ");

		// Handle Artist 
		String artistlastname = "";
		for (int i = 0; i < listArtistPart.length; i++)
		{										
			if (i == (listArtistPart.length - 1))
			{
				artistlastname += listArtistPart[i];
			}
			else
			{
				artistlastname += listArtistPart[i] + "_";
			}
		}
		for (int i = 0; i < nameArtistList.length; i++)
		{
			if (nameArtistList[i].contains(artistlastname) && (nameArtistList[i].length() == artistlastname.length())) 
			{				
				playPath = ARTISTPATH + artistlastname + "/";
				status = playSongArtistHandler(nameSong, playPath);		
				break;
			}	
		}	

		return status;		 	
	}

	public static String playSongArtistHandler(String nameSong, String playPath)
	{
		log.info("SONG NAME ft 2 = " + nameSong);	 
		String[] test = nameSong.split(" ");
		String lastname = "";
		for (int i = 0; i < test.length; i++)
		{										
			if (i == (test.length - 1))
			{
				lastname += test[i];
			}
			else
			{
				lastname += test[i] + "_";
			}
		}
	
		log.info("SONG NAME =" + playPath + lastname + ".mp3");
		String Path = playPath + lastname + ".mp3";	
		String status = "NotFound";
		for (int i = 0; i < nameSongOfArtList.length; i++)
		{
			if (nameSongOfArtList[i].contains(lastname))
			{							
				status = playVLCApplication(Path);
				log.info("do ok");
				break;	
			}
		}												
		return status;
	}

	public static String playSongHandler(String input)
	{
		String nameSong = "";
		nameSong = trimString(input, PSONG).toLowerCase();	
		log.info("SONG NAME ft 2 = " + nameSong);	
                
		String[] test = nameSong.split(" ");
		String lastname = "";
		for (int i = 0; i < test.length; i++)
		{										
			if (i == (test.length - 1))
			{
				lastname += test[i];
			}
			else
			{
				lastname += test[i] + "_";
			}
		}
	
		log.info("SONG NAME =" + LISTPATH + lastname + ".mp3");
		String playPath = LISTPATH + lastname;	
		String status = "";
		int countSong = 0;
		for (int i = 0; i < nameSongList.length; i++)
		{
			if (nameSongList[i].contains(lastname)) // simulate don't care it extension or algorithm
			{				
				countSong ++;	
			}
		}	
		if (0 == countSong)	
		{
			status = "NotFound";
			isPlayingMusic = false;
		}
		else if (1 == countSong)
		{
			playPath = playPath + ".mp3";
			status = playVLCApplication(playPath);	
		}
		else
		{
			status = multiSongResponse(countSong, lastname);
			isPlayingMusic = false;
		}
						
		return status;
	}
	
	public static String volumePercentHandler(String input)
	{
		String status = "";
		String VolPercent = trimString(input, SETVOLPER);
		int volNum = (Integer.parseInt(VolPercent)) / 5;
		log.info("percent to vol: " + volNum);
		status = volumeNumberHandler(SETVOLNUM + Integer.toString(volNum));
		return status;
	}
       
	public static String volumeHandler(String input, int type)
	{
		String status = "";
		
		if (currentVol < 0)
		{
			currentVol = INIT_VOL;
			currentPercent = INIT_PER;
		}

		switch(type)
		{
			case VOL_UP: 
				status = volumeUpHandler(input);
				break;
			case VOL_DWN:
				status = volumeDownHandler(input);			
				break;
			case VOL_NUM:
				status = volumeNumberHandler(input);
				break;
			case VOL_PER:
				status = volumePercentHandler(input);
				break;
		}
		return status;
	}

	

	public static String trimString(String iput, String trimS)
	{
		int endIndex = trimS.length();
		String contentCommand = iput.substring(endIndex);
		log.info("contentCommand = " + contentCommand);		
		return contentCommand;		
	}
 
	
	
	public static String multiSongResponse(int countS, String songName)
	{
		String status = "";
		String numSong = Integer.toString(countS);
		String artist = getArtistSim(countS, songName);
		status = "MultiSong," + numSong + artist;
		log.info("artist multi = " + status);	
		return status;
	}
	
	private static String getArtistSim(int countS, String songName)
	{
		int countSong = 0;
		String ret = "";
		String[] artistList = new String[countS];
		for (int i = 0; i < nameSongList.length; i++)
		{
			if (nameSongList[i].contains(songName))
			{			
				artistList[countSong] = trimString(nameSongList[i], songName + BYARTIST);	
				ret = ret + "," +  artistList[countSong].replaceAll("_", " ");	
				ret = ret.replaceAll("\\.mp3", "");	
				countSong ++;
			}
		}	
		return ret;
	}

	public static String playVLCApplication(String param)
	{
		String status = "OK";
		try
		{
			Process p = Runtime.getRuntime().exec("vlc " + param);
			isPlayingMusic = true;
		} 
		catch (IOException e)
		{
			status = "InternalError";
			e.printStackTrace();
		}	
		return status;
	}

	
	public static String sendCallToHandFreeHandler(String input) throws Exception 
	{ 
		String res = "InternalError";
		log.info("call command +++++++++++++++++++= " + input);
	        try 
		{
		    DataInputStream d = new DataInputStream(System.in);
	            Socket socket = new Socket("localhost", 8089);
	            DataOutputStream data_out = new DataOutputStream(socket.getOutputStream());
		    DataInputStream data_in = new DataInputStream(socket.getInputStream());
	
		    /*Check Command to call*/
	 	    data_out.writeBytes(input + "\n");
	 	    /*data_out.writeBytes("Call 0979679626\n");*/
	    	    res = data_in.readLine();
        	    log.info("Call command status from Handfree= " + res);
        	    
        	    /* Close socket for another call*/
        	    socket.close();
        	    
	        } 
		catch (Exception e) 
		{
			System.out.print(e);
	        }
		return res;
   	}
}

