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

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Vector;
import de.vdheide.mp3.ID3v230Frame;
import de.vdheide.mp3.ID3v2Frame;
//=================================================================================================
/**
 * Class representing an ID3v2 "CTOC" frame.
 * @author Chris Newell, BBC R&D.
 */
public class CTOCFrame extends ChapterFrame {
  protected static final String FRAME_ID = "CTOC";
  protected static final String CR = "\r\n";
  protected static final String ENC = "ISO-8859-1";
  public boolean isTopLevel;
  public boolean isOrdered;
  protected Vector entries = new Vector();

//-------------------------------------------------------------------------------------------------
  /**
   */
  public CTOCFrame(String elementId, boolean isTopLevel, boolean isOrdered) {
    super(FRAME_ID, elementId);
    this.isTopLevel = isTopLevel;
    this.isOrdered = isOrdered;
  }
//-------------------------------------------------------------------------------------------------
  /**
   */
  public CTOCFrame(ID3v2Frame frame) {
    super(frame);
    int i=0;
    for(; i < content.length; i++) {
      // Look for null terminator.
      if(content[i] == 0) {
        break;
      }
    }
    try {
      this.elementId = new String(content, 0, i, "ISO-8859-1");                   // Don't include the null terminator in the constructor.
      // System.out.println("new CTOCFrame(): elementId = " + elementId);
    } catch(UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    
    if((content[i+1] & 0x02) == 0x02) {
      this.isTopLevel = true;
    } else {
      this.isTopLevel = false;
    }
    if((content[i+1] & 0x01) == 0x01) {
      this.isOrdered = true;
    } else {
      this.isOrdered = false;
    }
    int numEntries = ID3Util.U2I(content[i+2]); 
    // System.out.println("new CTOCFrame(): isTopLevel = " + isTopLevel + " isOrdered = " + isOrdered + " numEntries = " + numEntries);
    
    i+=3;
    for(int n=0; n < numEntries; n++) {
      String childElementId;
      int length = 0;
      int start = i;
      for(; i < content.length; i++) {
        // Look for null terminator.
        if(content[i] == 0) {
          break;
        } else {
          length++;
        }
      }
      i++;           // Skip terminating null.      
      try {
        childElementId = new String(content, start, length, "ISO-8859-1");           // Don't include the null terminator in the constructor.
        entries.add(childElementId);
        // System.out.println("new CTOCFrame(): childElementId = " + childElementId);
      } catch(UnsupportedEncodingException e) {
        e.printStackTrace();
      }
    }
    while(i < content.length) {
      int length =  ID3Util.U2I(content[i+4]) * 0x1000000 + ID3Util.U2I(content[i+5]) * 0x10000 + ID3Util.U2I(content[i+6]) * 0x100 + ID3Util.U2I(content[i+7]);
      ByteArrayInputStream subFrameStream = new ByteArrayInputStream(content, i, length + 10);
      i += (length + 10);
      try {
        ID3v230Frame subFrame = new ID3v230Frame(subFrameStream);
        subFrames.addElement(subFrame);
      } catch(Exception e) {
        e.printStackTrace();
        continue;
      }
    }
  }
//------------------------------------------------------------------------------------------------- 
  public int getContentLength() {
    int length = elementId.length() + 3; // ISO-8859-1 (8-bit) assumed.
    for(int i=0; i<entries.size(); i++) {
      length += (getEntry(i).length() + 1);
    }
    for(int i=0; i<subFrames.size(); i++) {
      length += getSubFrame(i).getLength();
    }
    return length;
  }
//------------------------------------------------------------------------------------------------- 
  public int getLength() {
    return getContentLength() + 10;
  }
//-------------------------------------------------------------------------------------------------
  public byte[] getContent() {
    int n = elementId.length();
    byte[] content = new byte[getContentLength()];
    byte[] elementIdBytes = null;
    try {
      elementIdBytes = elementId.getBytes(ENC);
    } catch(UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    System.arraycopy(elementIdBytes, 0, content, 0, elementIdBytes.length);
    content[n] = 0;   // Terminating null;
    int flags = 0; 
    if(isTopLevel) {
      flags += 0x02;
    }
    if(isOrdered) {
      flags += 0x01;
    } 
    content[n+1] = (byte)(flags);
    content[n+2] = (byte)(entries.size() & 0xFF);
    int offset = n+3;
    for(int i=0; i<entries.size(); i++) {
      byte[] entry = getEntry(i).getBytes();
      System.arraycopy(entry, 0, content, offset, entry.length);
      content[offset + entry.length] = 0;                                              // Add terminating null.
      offset += (entry.length + 1);
    }

    for(int i=0; i<subFrames.size(); i++) {
      byte[] bytes = null; 
      if(ID3Util.version == 3) {
        bytes =  (ID3Util.getID3v230Frame(getSubFrame(i))).getBytes();
///      } else if(ID3Util.version == 4) {
///        bytes =  bytes =  ID3Util.getID3v240Frame(this).getBytes();
      }
      System.arraycopy(bytes, 0, content, offset, bytes.length);
      offset += bytes.length;
    }
    return content;
  }
//-------------------------------------------------------------------------------------------------
  public void addEntry(String elementId) {
    entries.addElement(elementId);
  }
//-------------------------------------------------------------------------------------------------
  public String getEntry(int index) {
    String entry = (String)(entries.elementAt(index));
    return entry;
  }
//-------------------------------------------------------------------------------------------------
  public int getNumEntries() {
    return entries.size();
  }
//-------------------------------------------------------------------------------------------------
  /**
   * @return Returns the isTopLevel.
   */
  public boolean isTopLevel() {
    return isTopLevel;
  }
//-------------------------------------------------------------------------------------------------
  /**
   * @param isTopLevel
   */
  public void setTopLevel(boolean isTopLevel) {
    this.isTopLevel = isTopLevel;
  }
//-------------------------------------------------------------------------------------------------
  /**
   * @return Returns the isOrdered.
   */
  public boolean isOrdered() {
    return isOrdered;
  }
//-------------------------------------------------------------------------------------------------
  public String toString() {
    String string = "Frame ID    = CTOC" + CR + "Size        = " + getContentLength() + CR + "elementId   = " + elementId + CR;
    string += "isTopLevel  = " + isTopLevel + CR + "isOrdered   = " + isOrdered + CR + "numEntries  = " + getNumEntries();
    for(int i=0; i<getNumEntries(); i++) {
      string += CR + "Entry (" + i + ")   = " + getEntry(i);
    }
    for(int i=0; i<getNumSubFrames(); i++) {
      ID3v2Frame subFrame = getSubFrame(i);
      string += CR + "SubFrame(" + i + "):" + CR + ID3Util.toString(subFrame);
    }
    return string;
  }
//-------------------------------------------------------------------------------------------------
}
//=================================================================================================