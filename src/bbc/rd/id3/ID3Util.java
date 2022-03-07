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
package bbc.rd.id3;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import de.vdheide.mp3.ID3v230Frame;
import de.vdheide.mp3.ID3v2DecompressionException;
import de.vdheide.mp3.ID3v2Frame;
//=================================================================================================
/**
 * Utility methods for ID3v2 tags and frames.
 * 
 * @author Chris Newell, BBC R&D.
 */
public class ID3Util {

  protected static final String ENC = "ISO-8859-1";
  protected static final String CR = "\r\n";
  protected static boolean tag_alter_preservation = false;
  protected static boolean file_alter_preservation = false;
  protected static boolean read_only = true;
  protected static byte compression_type = ID3v230Frame.NO_COMPRESSION;
  protected static byte encryption_id = 0;
  protected static byte group = 0;
  protected static int version = 3;                  // Default value.
  
//------------------------------------------------------------------------------------------------  
  public static ID3v230Frame getID3v230Frame(ID3v2Frame frame) {
    ID3v230Frame v230frame = null;
    try {
      v230frame = new ID3v230Frame(frame.getID(), frame.getContent(), tag_alter_preservation, file_alter_preservation, read_only, compression_type, encryption_id, group);
    } catch (ID3v2DecompressionException e) {
    }
    return v230frame;
  }
//------------------------------------------------------------------------------------------------  
  public static ID3v230Frame getID3v230Frame(String frameId, byte[] content) {
    ID3v230Frame v230frame = null;
    try {
      v230frame = new ID3v230Frame(frameId, content, tag_alter_preservation, file_alter_preservation, read_only, compression_type, encryption_id, group);
    } catch (ID3v2DecompressionException e) {
    }
    return v230frame;
  }
//------------------------------------------------------------------------------------------------  
  public static ID3v230Frame getTextFrame(String frameId, String text) {
    try {
      byte[] bytes = text.getBytes(ENC);
      byte[] content = new byte[bytes.length + 1];
      content[0] = 0x00;                                         // String encoding value for ISO-8859-1
      System.arraycopy(bytes, 0, content, 1, bytes.length);
      ID3v230Frame frame = new ID3v230Frame(frameId, content, tag_alter_preservation, file_alter_preservation, read_only, compression_type, encryption_id, group);
      return frame;
    } catch(Exception e) {
      e.printStackTrace();
      return null;
    }
  }
//------------------------------------------------------------------------------------------------  
  public static String toString(ID3v2Frame frame) {
    String id = frame.getID();
    String string = "Frame ID      = " + id  + CR + "Size          = " + (frame.getLength() - 10) + CR;
    
    if(id.startsWith("T")) {
      try {
        TextFrame textFrame = new TextFrame(frame);
        string += "Text encoding = 0x" + Integer.toHexString(textFrame.encoding) + CR;
        string += "Text          = " + textFrame.text;
      } catch(Exception e) {
        e.printStackTrace();
      }
    } else if(id.equals("COMM")) {
      try {
        COMMFrame commFrame = new COMMFrame(frame);
        string += "Text encoding = 0x" + Integer.toHexString(commFrame.encoding) + "\r\n";
        string += "Language      = " + commFrame.language + "\r\n";
        string += "Description   = " + commFrame.description + "\r\n";
        string += "Text          = " + commFrame.text;
      } catch(Exception e) {
        e.printStackTrace();
      }
    } else if(id.equals("CHAP")) {
      try {
        CHAPFrame chapFrame = new CHAPFrame(frame);
        string = chapFrame.toString();
      } catch(Exception e) {
        e.printStackTrace();
      }
    } else if(id.equals("CTOC")) {
      try {
        CTOCFrame ctocFrame = new CTOCFrame(frame);
        string = ctocFrame.toString();
      } catch(Exception e) {
        e.printStackTrace();
      }
    } else if(id.startsWith("W")) {
      try {
        URLFrame urlFrame = new URLFrame(frame);
        string += "URL           = " + urlFrame.url;
      } catch(Exception e) {
        e.printStackTrace();
      }
    } else if(id.equals("APIC")) {
      try {
        ImageFrame imageFrame = new ImageFrame(frame);
        string += "Mime type = " + imageFrame.mimeType + "\r\n";
        string += "Description   = " + imageFrame.description + "\r\n";
        string += "Type          = 0x" + Integer.toHexString(imageFrame.type);
      } catch(Exception e) {
        e.printStackTrace();
      } 
    } else if(id.equals("ATXT")) {
        try {
          ATXTFrame audioTextFrame = new ATXTFrame(frame);
          string += "Equivalent Frame ID = " + audioTextFrame.getEquivalentFrameId() + "\r\n";
          string += "Language            = " + audioTextFrame.getLanguage() + "\r\n";
        } catch(Exception e) {
          e.printStackTrace();
        }
    } else {
      string = "Frame ID      = " + id + CR + "Size          = " + (frame.getLength()-10) + CR;
      byte[] content = frame.getContent();
      string += "Content bytes = ";
      // List the first 128 bytes for information.
      for(int i=0; i<128 && i<content.length; i++) {
        string += "0x" + Integer.toHexString(U2I(content[i])) + " ";
      }
      if(content.length > 128) {
        string += "etc...";
      }
    }
    return string;
  }
//--------------------------------------------------------------------------------------------------
  /**
   * Convert an 8-bit unsigned byte value to an integer (32-bit).
   */
   public static int U2I(byte a) { 
     int unsigned = (int)a;
     if(unsigned < 0) {
       unsigned += 256;
     }
     return unsigned;
   }
//--------------------------------------------------------------------------------------------------- 
   /**
    * Convert a formatted time string to a count in milliseconds. The method will accept time strings of the form:
    *         [s]s[.nn]
    *       [m]m:ss[.nn]
    *    [h]h:mm:ss[.nn]
    *    
    * or:
    *    
    *    N * s + [.nn]
    * 
    * @param timeString a formatted time string representing hours, minutes, seconds & centiseconds (e.g. "1:05:17.32") or seconds & centiseconds (e.g. "1572.32"). 
    * @return a time in milliseconds
    */
   public static long getTimeInMilliseconds(String timeString) {
     long hours = 0;
     long minutes = 0;
     long seconds = 0;
     long milliseconds = 0;
     int firstColonIndex = timeString.indexOf(":");
     int lastColonIndex = timeString.lastIndexOf(":");
     int periodIndex = timeString.lastIndexOf(".");    
      
     if(periodIndex != -1) {
       // Determine centiseconds.
       if(timeString.length() - periodIndex == 2) {
         milliseconds = Integer.parseInt(timeString.substring(periodIndex + 1, periodIndex + 2)) * 100;
       } else if(timeString.length() - periodIndex == 3) {
           milliseconds = Integer.parseInt(timeString.substring(periodIndex + 1, periodIndex + 3)) * 10;
       } else if(timeString.length() - periodIndex > 3) {
         milliseconds = Integer.parseInt(timeString.substring(periodIndex + 1, periodIndex + 4));
       }
     } else {
       // There are no centiseconds.
       periodIndex =  timeString.length();
     }
     
     if(lastColonIndex == -1) {
       // There are no hours or minutes. Seconds may exceed 59.
       seconds = Integer.parseInt(timeString.substring(0, periodIndex));
     } else {
       // Determine seconds.
       seconds = Integer.parseInt(timeString.substring(lastColonIndex + 1, periodIndex));
       // Determine minutes.
       if(firstColonIndex == lastColonIndex) {
         minutes = Integer.parseInt(timeString.substring(0, lastColonIndex)); 
       } else {
         minutes = Integer.parseInt(timeString.substring(firstColonIndex + 1, lastColonIndex)); 
       }
       // Determine hours.
       if(firstColonIndex != lastColonIndex && firstColonIndex != 0) {
         hours = Integer.parseInt(timeString.substring(0, firstColonIndex)); 
       }
     }
     
     long time = hours * 3600000L + minutes * 60000L + seconds * 1000L + milliseconds;
     return time;
   }
// -------------------------------------------------------------------------------------------------
  public static void setVersion(int version) {
    ID3Util.version = version;  
  }
//-------------------------------------------------------------------------------------------------
  public static int getVersion() {
    return version;  
  }
//-------------------------------------------------------------------------------------------------
  /**
   * Write a file containing the data in an array of bytes. 
   *
   * @param  directoryPath  the directory to be used.
   * @param  filename  the file name to be written.
   * @param  bytes     the byte array.
   */
  public static void writeFile(String directoryPath, String filename, byte[] bytes) {
    try {
      FileOutputStream os = new FileOutputStream(new File(directoryPath, filename));
      BufferedOutputStream bos = new BufferedOutputStream(os);
      bos.write(bytes, 0, bytes.length);
      bos.flush();
      os.flush();
      bos.close();
      os.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
// ------------------------------------------------------------------------------------------------
}
//=================================================================================================