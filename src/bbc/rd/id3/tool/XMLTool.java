package bbc.rd.id3.tool;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;
import javax.media.Duration;
import javax.media.Manager;
import javax.media.NoPlayerException;
import javax.media.Player;

import org.xml.sax.SAXException;

import bbc.rd.id3.CHAPFrame;
import bbc.rd.id3.CTOCFrame;
import bbc.rd.id3.ID3Util;

//import bbc.rd.tvanytime.TVAnytimeException;
//import bbc.rd.tvanytime.segmentInformation.*;
//import bbc.rd.tvanytime.xml.NonFatalXMLException;
//import bbc.rd.tvanytime.xml.SAXXMLParser;
//import java.io.ByteArrayInputStream;
//import java.io.UnsupportedEncodingException;
import de.vdheide.mp3.*;

//=================================================================================================
/**
 * An application which processes XML files in the format used by Apple's chapter tool application 
 * and inserts ID3v2 chapter frames into MP3 files.
 * 
 * @author Chris Newell, BBC R&D.
 */
public class XMLTool {
  
  private static int SIZE = 4096;
  private static boolean includeImages = true;
  private static boolean includeURLs = true;
  private static Vector chapFrames = new Vector();
  private static long endTime = 0;

  //-------------------------------------------------------------------------------------------------  
  /**
   * Main method.
   * 
   * This inserts chapter frames into the input file using ID3v2.3. If the input file does not have an ID3 tag a new one is created.
   * If the file already has a tag the chapter frames are appended.
   *  
   * <p>Usage: 
   * <pre>	java XMLTool -x &lt;XML filegt; -a &lt;input MP3 file&gt; -o &lt;output MP3 file&gt; [-np] [-nu]
   * <p>where:
   * 	&lt;XML file&gt;           is the path of the XML file defining the chapter information.
   * 	&lt;input MP3 file&gt;     is the path of the input MP3 file.
   * 	&lt;output MP3 file&gt;    is the path of the output MP3 file.
   *    &lt;-np (optional)&gt;     omit images from output file.;
        &lt;-nu (optional)&gt;     omit URLs from output file.</pre>
   */
  public static void main(String[] args) {
    String XMLPath = null;
    String inputPath = null;
    String outputPath = null;

    // Parse and check arguments.
    for(int i=0; i < args.length; i++) {
      if(args[i].trim().equals("-x")) {
        XMLPath = args[i+1].trim();
        i++;
      } else if(args[i].trim().equals("-a")) {
        inputPath = args[i+1].trim();
        i++;
      } else if(args[i].trim().equals("-o")) {
        outputPath = args[i+1].trim();
        i++;
      } else if(args[i].trim().equals("-np")) {
        includeImages = false;
      } else if(args[i].trim().equals("-nu")) {
        includeURLs = false;
      } else {
        exitWithInstructions();
      }
    }
    if(XMLPath == null || inputPath == null || outputPath == null) {
      exitWithInstructions();
    }
    
    // Parse the XML file.
    try {
      File XMLFile = new File(XMLPath); 
      ChapterXMLHandler handler = new ChapterXMLHandler(XMLFile, includeImages, includeURLs);
      chapFrames = handler.getChapters();
    } catch (IOException e) {
      System.err.println("XMLTool: Unable to open XML input file: " + XMLPath);
      System.exit(0);
    } catch (SAXException e) {
      System.err.println("XMLTool: Error parsing XML input file: " + XMLPath);
      System.exit(0);
    }
  
    // Copy input file.
    File inFile = new File(inputPath);
    File outFile = new File(outputPath);
    BufferedInputStream bis = null;
    try {
      bis = new BufferedInputStream(new FileInputStream(inFile));
    } catch(FileNotFoundException e) {
      System.err.println("XMLTool: Unable to open audio input file: " + inputPath);
      System.exit(0);
    }
    try {
      FileOutputStream os = new FileOutputStream(outFile);
      BufferedOutputStream bos = new BufferedOutputStream(os);
      byte[] buffer = new byte[SIZE];
      int bytesRead;
      while(true) {
        bytesRead = bis.read(buffer, 0, SIZE);
        if(bytesRead > 0) {
          bos.write(buffer, 0, bytesRead);
        } else { 
          break;
        }
      }
      bis.close();
      bos.flush();
      os.flush();
      bos.close();
      os.close();
    } catch(IOException e) {
      System.out.println("XMLTool: Unable to create audio output file: " + outputPath);
      System.exit(0);
    }
    
    // Determine duration (used for the end time of last chapter)
    URL url = null;
    try {
      if ((url = new URL("file:" + inputPath)) == null) {
        System.err.println("Can't build URL for " + inputPath);
      }
      try {
        Player player = Manager.createPlayer(url);
        player.realize();
        while(player.getDuration() == Duration.DURATION_UNKNOWN) { 
          try {
            Thread.sleep(100L);
          } catch (InterruptedException e) {
          }
        }
        endTime = (long)(player.getDuration().getSeconds() * 1000);
        player.stop();
        player.deallocate();
        // player.close();      // Causes EXCEPTION_ACCESS_VIOLATION
        player = null;
      } catch (NoPlayerException e) {
        System.err.println("Error: " + e);
      }
    } catch (MalformedURLException e) {
      System.out.println("MalformedURLException "+ url);
      System.err.println("Error: " + e);
    } catch (IOException e) {
      System.err.println("Error: " + e);
    }
  
    ID3v2 tag = null;
    try {
      tag = new ID3v2(outFile);
      
      // Convert v2.2 tags to v2.3 if required.
      if(tag.hasTag()) {
        if(tag.getHeader().version == ID3v2.VERSION_2) {
          tag.convertV220Frames(false);                   // i.e. don't use compression.
          tag.getHeader().version = ID3v2.VERSION_3;
          tag.getHeader().revision = ID3v2.REVISION;
        }
      }
      
      // Many media players create errors if used with other settings.
      tag.setUseExtendedHeader(false);
      tag.setUseCRC(false);
      tag.setUsePadding(false);
      tag.setUseUnsynchronization(false);

      // Set end time of the last chapter frame to duration value.
      CHAPFrame lastChapter = (CHAPFrame)(chapFrames.lastElement());
      lastChapter.setEndTime(endTime);
      
      // Create & add table of contents.
      CTOCFrame ctocFrame = new CTOCFrame("toc1", true, true);
      for(int i=0; i < chapFrames.size(); i++) {
        CHAPFrame chapFrame = (CHAPFrame)(chapFrames.elementAt(i));
        ctocFrame.addEntry(chapFrame.getElementId());
      }
      tag.addFrame(ID3Util.getID3v230Frame(ctocFrame));

      // Add chapter frames.
      for(int i=0; i < chapFrames.size(); i++) {
        CHAPFrame chapFrame = (CHAPFrame)(chapFrames.elementAt(i));
        tag.addFrame(ID3Util.getID3v230Frame(chapFrame));
      }
      tag.update();
    } catch(Exception e) {
      e.printStackTrace();
    }
    
    System.out.println("XMLTool: Completed.");
    System.exit(0);
  }
//------------------------------------------------------------------------------------------------- 
  public static void exitWithInstructions() {
    System.out.println("Usage: java XMLTool -x <XML file> -a <input MP3 file> -o <output MP3 file> [-np] [-nu]");  
    System.out.println("where:");
    System.out.println("<XML file>                  is the path of the XML file defining the chapter information.");
    System.out.println("<input MP3 file>            is the path of the input MP3 file.");
    System.out.println("<output MP3 file>           is the path of the output MP3 file.");
    System.out.println("-np (optional)              omit images from output file.");
    System.out.println("-nu (optional)              omit URLs from output file.");
    System.exit(0);
  }
//-------------------------------------------------------------------------------------------------  
}
//=================================================================================================
