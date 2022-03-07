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
 * Class representing a generic URL frame.
 * 
 * @author Chris Newell, BBC R&D.
 */
public class URLFrame implements ID3v2Frame {
  protected static final String enc = "ISO-8859-1";
  protected int encoding;                     // Only used in WXXX frames.      
  protected String url;
  protected String frameId;
//-------------------------------------------------------------------------------------------------
  /**
   */
  public URLFrame(String frameId, String url) {
    this.frameId = frameId;
    this.url = url;
  }
//-------------------------------------------------------------------------------------------------
  /**
   */
  public URLFrame(ID3v2Frame frame) {
    this.frameId = frame.getID();
    byte[] content = frame.getContent();
    try {
      int i=0;
      if(content[0] == 0) {
        i=1;                      // WFED seems to start content bytes with either a null or a text encoding byte but not both.
      }
      int start = i;
      for(; i < content.length && content[i] != 0; i++) {}           // Find null or end of array.
      url = new String(content, start, i-start, "ISO-8859-1");
      // System.out.println("new URLFrame(): Frame ID = " + super.getID());
      // System.out.println("                     URL = " + url);
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
   * @return Returns the URL
   */
  public String getURL() {
    return url;
  }
//------------------------------------------------------------------------------------------------- 
  public int getContentLength() {
    return url.length() + 1;             // ISO-8859-1 (8-bit) assumed.
  }
//-------------------------------------------------------------------------------------------------
  public int getLength() {
    return getContentLength() + 10;
  }
//-------------------------------------------------------------------------------------------------
  public byte[] getContent() {
    byte[] content = new byte[getContentLength()];
    content[0] = (byte)encoding;
    byte[] urlBytes = new byte[0];
    try {
      urlBytes = url.getBytes(enc);
    } catch(UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    System.arraycopy(urlBytes, 0, content, 1, urlBytes.length);
    return content;
  }
//-------------------------------------------------------------------------------------------------
  public String toString() {
    return frameId + " : " + url;
  }
//-------------------------------------------------------------------------------------------------
  public String getID() {
    return frameId;
  }
//-------------------------------------------------------------------------------------------------
  public void setID(String frameId) {
  }
// -------------------------------------------------------------------------------------------------
  public byte[] getBytes() {
    byte[] bytes = null; 
    if(ID3Util.version == 3) {
      bytes =  ID3Util.getID3v230Frame(this).getBytes();
// /      } else if(ID3Util.version == 4) {
// /        bytes =  ID3Util.getID3v240Frame(this).getBytes();
    }
    return bytes;
   }
//-------------------------------------------------------------------------------------------------
}
//=================================================================================================