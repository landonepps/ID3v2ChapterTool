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
import java.io.UnsupportedEncodingException;
import de.vdheide.mp3.ID3v2Frame;
//=================================================================================================
/**
 * Class representing a generic ID3v2 Text frame.
 * 
 * @author Chris Newell, BBC R&D.
 */
public class TextFrame implements ID3v2Frame {
  protected static final String ENC = "ISO-8859-1";
  protected static final String CR = "\r\n";
  protected int encoding;
  protected String text;
  protected String frameId;
//-------------------------------------------------------------------------------------------------
  /**
   */
  public TextFrame(String frameId) {
    this.frameId = frameId;
    this.encoding = 0x00;                    // This class only supports ISO-8859-1
  }
//-------------------------------------------------------------------------------------------------
  /**
   */
  public TextFrame(String frameId, String text) {
    this.frameId = frameId;
    this.text = text;
    this.encoding = 0x00;                    // This class only supports ISO-8859-1
  }
//-------------------------------------------------------------------------------------------------
  /**
   */
  public TextFrame(ID3v2Frame frame) {
    this.frameId = frame.getID();
    byte[] content = frame.getContent();
    encoding = content[0];
    int i=1;
    for(; i < content.length && content[i] != 0; i++) {}           // Find null or end of array.
    try {
      text = new String(content, 1, i-1, ENC);
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    // System.out.println("new TextFrame(): Frame ID = " + super.getID());
    // System.out.println("                     text = " + text);
  }
//------------------------------------------------------------------------------------------------- 
  /**
   * @return Returns the encoding.
   */
  public int getEncoding() {
    return encoding;
  }
//------------------------------------------------------------------------------------------------- 
  /**
   * @return Set the text carried by this TextFrame.
   */
  public void setText(String text) {
    this.text = text;
  }
//------------------------------------------------------------------------------------------------- 
  /**
   * @return Returns the text.
   */
  public String getText() {
    return text;
  }
//------------------------------------------------------------------------------------------------- 
  public int getContentLength() {
    return text.length() + 1;             // ISO-8859-1 (8-bit) assumed.
  }
//-------------------------------------------------------------------------------------------------
  public int getLength() {
    return getContentLength() + 10;
  }
//-------------------------------------------------------------------------------------------------
  public byte[] getContent() {

    byte[] content = new byte[getContentLength()];
    content[0] = (byte)encoding;
    byte[] textBytes = new byte[0];
    try {
      textBytes = text.getBytes(ENC);
    } catch(UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    System.arraycopy(textBytes, 0, content, 1, textBytes.length);
    return content;
  }
//-------------------------------------------------------------------------------------------------
  public String toString() {
    return getID() + " : " + text;
  }
//-------------------------------------------------------------------------------------------------
  public String getID() {
    return frameId;  
  }
//-------------------------------------------------------------------------------------------------
  public void setID(String frameId) {
  }
//-------------------------------------------------------------------------------------------------
  public byte[] getBytes() {
   byte[] bytes = null; 
   if(ID3Util.version == 3) {
     bytes =  ID3Util.getID3v230Frame(this).getBytes();
///      } else if(ID3Util.version == 4) {
///        bytes =  ID3Util.getID3v240Frame(this).getBytes();
   }
   return bytes;
  }
//-------------------------------------------------------------------------------------------------
}
//=================================================================================================