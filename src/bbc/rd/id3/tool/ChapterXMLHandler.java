/**
 * Copyright 2005 British Broadcasting Corporation
 *
 * This file is part of the BBC R&D ID3v2 Chapter Tool application.
 *
 * The BBC R&D ID3v2 Chapter Tool application is free software; you can redistribute it
 * and/or modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2 of the License, 
 * or (at your option) any later version.
 *
 * The BBC R&D ID3v2 Chapter Tool application is distributed in the hope that it will 
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General 
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with 
 * the BBC R&D ID3v2 Chapter Tool application; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package bbc.rd.id3.tool;

import java.io.*;
import java.util.Vector;
import java.util.Stack;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import bbc.rd.id3.ID3Util;
import bbc.rd.id3.ImageFrame;
import bbc.rd.id3.TextFrame;
import bbc.rd.id3.URLFrame;
import bbc.rd.id3.CHAPFrame;

//=================================================================================================
/**
 * An class which processes XML in the format used by Apple's chapter tool application. 
 * 
 * @author Chris Newell, BBC R&D.
 */
public class ChapterXMLHandler extends DefaultHandler {
  
  private Vector chapFrames = new Vector();
  private Stack elementStack = new Stack();
  private boolean includeImages;
  private boolean includeURLs;
//-------------------------------------------------------------------------------------------------  

  public ChapterXMLHandler(File file, boolean includeImages, boolean includeURLs) throws IOException, SAXException {
    this.includeImages = includeImages;
    this.includeURLs = includeURLs;
    DefaultHandler handler = this;
    // Use the default (non-validating) parser
    SAXParserFactory factory = SAXParserFactory.newInstance();
    SAXParser saxParser = null;
    try {
      saxParser = factory.newSAXParser();
    } catch (Throwable t) {
      t.printStackTrace();
    }
    saxParser.parse(file, handler);
    // System.out.println("ChapterXMLHandler: Completed.");
  }
//-------------------------------------------------------------------------------------------------  
  public void startDocument() {
    // System.out.println("ChapterXMLHandler: startDocument.");
  }
//-------------------------------------------------------------------------------------------------  
  public void endDocument() {
    // System.out.println("ChapterXMLHandler: endDocument.");
  }
//-------------------------------------------------------------------------------------------------  
  public void startElement(String uri, String localName, String qName, Attributes attributes) {
    // System.out.println("ChapterXMLHandler: startElement. qName = " + qName);
    if(qName.equals("chapter")) {
      CHAPFrame chapter = new CHAPFrame("ch" + chapFrames.size());
      String string = attributes.getValue("starttime");
      long startTime = ID3Util.getTimeInMilliseconds(string);
      chapter.setStartTime(startTime);
      // Set end time of previous chapter.
      if(!chapFrames.isEmpty()) {
        CHAPFrame lastChapter = (CHAPFrame)(chapFrames.lastElement());
        lastChapter.setEndTime(startTime);
      }
      elementStack.push(chapter);
    } else  if(qName.equals("title")) {
      TextFrame textFrame = new TextFrame("TIT2");
      elementStack.push(textFrame);
    } else  if(qName.equals("picture")) {
      ImageFrame imageFrame;
      imageFrame = new ImageFrame();
      elementStack.push(imageFrame);
    } else  if(qName.equals("link")) {
       String url =  attributes.getValue("href");
       // System.out.println("ChapterXMLHandler: url = " + url);
       URLFrame urlFrame = new URLFrame("WPUB", url);
       elementStack.push(urlFrame);
    }
  }
//-------------------------------------------------------------------------------------------------  
  public void endElement(String uri, String localName, String qName) {
    // System.out.println("ChapterXMLHandler: endElement qName = " + qName);
    if(qName.equals("chapters")) {
      return;
    }
    Object object = elementStack.pop();
    if(qName.equals("chapter")) {
      CHAPFrame chapFrame = (CHAPFrame)object;
      chapFrames.addElement(chapFrame);
    } else if(qName.equals("title")) {
      TextFrame textFrame = (TextFrame)object;
      CHAPFrame chapFrame = (CHAPFrame)(elementStack.peek());
      chapFrame.addSubFrame(textFrame);
    } else if(qName.equals("picture")) {
      if(includeImages == true) {
        ImageFrame imageFrame = (ImageFrame)object;
        if(imageFrame.getImageBytes() != null) {
          CHAPFrame chapFrame = (CHAPFrame)(elementStack.peek());
          chapFrame.addSubFrame(imageFrame);
        }
      }
    } else if(qName.equals("link")) {
      if(includeURLs == true) { 
        URLFrame urlFrame = (URLFrame)object;
        if(urlFrame.getURL() != null) {
          CHAPFrame chapFrame = (CHAPFrame)(elementStack.peek());
          chapFrame.addSubFrame(urlFrame);
        }
      }
    }
  }
//-------------------------------------------------------------------------------------------------  
  public void characters(char[] ch, int start, int length) {
    String characters = new String(ch, start, length);
    // System.out.println("ChapterXMLHandler: characters = " + characters);
    if(!elementStack.empty()) {
      Object object = elementStack.peek();
      if(object instanceof TextFrame) {
        TextFrame textFrame = (TextFrame)object;
        textFrame.setText(characters);
      } else if(object instanceof ImageFrame) {
        ImageFrame imageFrame = (ImageFrame)object;
        imageFrame.setDescription("");
        try {
          File imageFile = new File(characters);  
          imageFrame.setImage(imageFile);
        } catch (IOException e) {
          System.out.println("ChapterXMLHandler: IOException - Unable to find image file \"" + characters + "\"");
        }
      }
    }
  }
//-------------------------------------------------------------------------------------------------  
  public Vector getChapters() {
    return chapFrames;
  }
//-------------------------------------------------------------------------------------------------  
  /**
   * Convert XML reserved characters to escape sequences.
   * The characters concerned are &, <, >, ' and ".  
   * 
   * @param in the string to be processed
   * @return a string with escaped characters
   */
  public static String escape(String in) {
    StringReader reader = new  StringReader(in);
    StringBuffer out = new StringBuffer();
    char[] ch = new char[1];
    try {
      while(reader.read(ch) != -1) {
        if(ch[0] == '&') {
          out.append("&amp;");
        } else if(ch[0] == '<') {
        out.append("&lt;");
        } else if(ch[0] == '>') {
          out.append("&gt;");
        } else if(ch[0] == '\'') {
          out.append("&apos;");
        } else if(ch[0] == '\"') {
          out.append("&quot;");
        } else {
          out.append(ch);
        } 
      }
    } catch(IOException e) {
        e.printStackTrace();
    }
    // System.out.println("in  = " + in);
    // System.out.println("out = " + out);
    return out.toString();
  }
//-------------------------------------------------------------------------------------------------  
}
//=================================================================================================
