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
 * Class representing an ID3v2 "COMM" frame.
 * 
 * @author Chris Newell, BBC R&D.
 */
public class COMMFrame implements ID3v2Frame {
  protected static final String FRAME_ID = "COMM";
  protected static final String enc = "ISO-8859-1";
  protected int encoding;
  protected String language;
  protected String description;
  protected String text;
//-------------------------------------------------------------------------------------------------
  /**
   */
  public COMMFrame(String language, String description, String text) {
    this.encoding = 0x00;                    // This class only supports ISO-8859-1
    this.language = language;
    this.description = description;
    this.text = text;
  }
//-------------------------------------------------------------------------------------------------
  /**
   * @throws java.io.IOException
   * @throws de.vdheide.mp3.ID3v2DecompressionException
   */
  public COMMFrame(ID3v2Frame frame) {
    byte[] content = frame.getContent();
    encoding = content[0];
    try {
      language =  new String(content, 1, 3, "ISO-8859-1");
      int i;
      for(i=4; i < content.length && content[i] != 0; i++) {}         // Find first null.
      description = new String(content, 4, i-4, "ISO-8859-1");
      i++;                                                            // Skip null.
      int start = i;
      for(; i < content.length && content[i] != 0; i++) {}            // Find null or end of array.
      text = new String(content, start, i-start, "ISO-8859-1");
//      System.out.println("new COMMFrame(): language    = " + super.getID());
//      System.out.println("                 description = " + description);
//      System.out.println("                 text        = " + text);
    } catch(UnsupportedEncodingException e) {
      e.printStackTrace();
    }
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
   * @return Returns the language code.
   */
  public String getLanguage() {
    return language;
  }
//------------------------------------------------------------------------------------------------- 
  /**
   * @return Returns the language code.
   */
  public String getDescription() {
    return description;
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
    return description.length() + text.length() + 5;         // ISO-8859-1 (8-bit) assumed.
  }
//-------------------------------------------------------------------------------------------------
  public int getLength() {
    return getContentLength() + 10;
  }
//-------------------------------------------------------------------------------------------------
  public byte[] getContent() {
    byte[] content = new byte[getContentLength()];
    content[0] = (byte)encoding;
    try {
      byte[] languageBytes = language.getBytes(enc);
      byte[] descriptionBytes = description.getBytes(enc);
      byte[] textBytes = text.getBytes(enc);
      System.arraycopy(languageBytes, 0, content, 1, 3);
      System.arraycopy(descriptionBytes, 0, content, 4, descriptionBytes.length);
      content[descriptionBytes.length + 4] = 0;                                                     // Null separator.
      System.arraycopy(textBytes, 0, content, descriptionBytes.length + 5, textBytes.length);
      return content;
    } catch(UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    return null;
  }
//-------------------------------------------------------------------------------------------------
  public String toString() {
    return FRAME_ID;
  }
//-------------------------------------------------------------------------------------------------
  public String getID() {
    return FRAME_ID;
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