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
import de.vdheide.mp3.ID3v230Frame;
import de.vdheide.mp3.ID3v2Frame;
//=================================================================================================
/**
 * Class representing an ID3v2 "CHAP" frame.
 * 
 * @author Chris Newell, BBC R&D.
 */
public class CHAPFrame  extends ChapterFrame {
  protected static final String FRAME_ID = "CHAP";
  protected static final String CR = "\r\n";
  protected static final String ENC = "ISO-8859-1";
  protected long startTime;
  protected long endTime;
  protected long startByteOffset = 0xFFFFFFFF;       // Default value.
  protected long endByteOffset = 0xFFFFFFFF;         // Default value.

//-------------------------------------------------------------------------------------------------
  /**
   * Constructor
   * 
   * @param elementId
   */
  public CHAPFrame(String elementId) {
    super(FRAME_ID, elementId);
  }
//-------------------------------------------------------------------------------------------------
  /**
   * Constructor.
   * 
   * @param elementId
   * @param startTime  the startTime in milliseconds
   * @param endTime  the endTime in milliseconds
   * @param startByteOffset
   * @param endByteOffset
   */
  public CHAPFrame(String elementId, long startTime, long endTime, long startByteOffset, long endByteOffset) {
    super(FRAME_ID, elementId);
    this.startTime = startTime;
    this.endTime = endTime;
    this.startByteOffset = startByteOffset;
    this.endByteOffset = endByteOffset;
  }
//-------------------------------------------------------------------------------------------------
  /**
   * Construct a CHAPFrame from an ID3Frame.
   */
  public CHAPFrame(ID3v2Frame frame) {
    super(frame);
    int i=0;
    for(; i < content.length; i++) {
      // Look for null terminator.
      if(content[i] == 0) {
        break;
      }
    }
    try {
      this.elementId = new String(content, 0, i, "ISO-8859-1");    // Don't include the null terminator in the constructor.
       // System.out.println("new CHAPFrame(): elementId = " + elementId);
    } catch(UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    
    this.startTime = ID3Util.U2I(content[i+1]) * 0x1000000L + ID3Util.U2I(content[i+2]) * 0x10000 + ID3Util.U2I(content[i+3]) * 0x100 + ID3Util.U2I(content[i+4]);
    this.endTime = ID3Util.U2I(content[i+5]) * 0x1000000L + ID3Util.U2I(content[i+6]) * 0x10000 + ID3Util.U2I(content[i+7]) * 0x100 + ID3Util.U2I(content[i+8]);    
    this.startByteOffset = ID3Util.U2I(content[i+9]) * 0x1000000L + ID3Util.U2I(content[i+10]) * 0x10000 + ID3Util.U2I(content[i+11]) * 0x100 + ID3Util.U2I(content[i+12]);
    this.endByteOffset = ID3Util.U2I(content[i+13]) * 0x1000000L + ID3Util.U2I(content[i+14]) * 0x10000 + ID3Util.U2I(content[i+15]) * 0x100 + ID3Util.U2I(content[i+16]);
    // System.out.println("new CHAPFrame(): startTime = " + startTime + " endTime = " + endTime + " startByteOffset " + startByteOffset + " endByteOffset = " + endByteOffset);
    i+=17;
    
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
    int length = elementId.length() + 17; // ISO-8859-1 (8-bit) assumed.
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
    byte[] content = new byte[getContentLength()];
    byte[] elementIdBytes = null;
    try {
      elementIdBytes = elementId.getBytes(ENC);
    } catch(UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    int n = elementIdBytes.length;
    System.arraycopy(elementIdBytes, 0, content, 0, elementIdBytes.length);
    content[n] = 0;          // Terminating null;
    content[n+1] = (byte)((startTime >>> 24) & 0xFF);
    content[n+2] = (byte)((startTime >>> 16) & 0xFF);
    content[n+3] = (byte)((startTime >>> 8) & 0xFF);
    content[n+4] = (byte)(startTime & 0xFF);
    content[n+5] = (byte)((endTime >>> 24) & 0xFF);
    content[n+6] = (byte)((endTime >>> 16) & 0xFF);
    content[n+7] = (byte)((endTime >>> 8) & 0xFF);
    content[n+8] = (byte)(endTime & 0xFF);
    content[n+9] = (byte)((startByteOffset >>> 24) & 0xFF);
    content[n+10] = (byte)((startByteOffset >>> 16) & 0xFF);
    content[n+11] = (byte)((startByteOffset >>> 8) & 0xFF);
    content[n+12] = (byte)(startByteOffset & 0xFF);
    content[n+13] = (byte)((endByteOffset >>> 24) & 0xFF);
    content[n+14] = (byte)((endByteOffset >>> 16) & 0xFF);
    content[n+15] = (byte)((endByteOffset >>> 8) & 0xFF);
    content[n+16] = (byte)(endByteOffset & 0xFF);
    int offset = n+17;
    for(int i=0; i<subFrames.size(); i++) {
      byte[] bytes = null; 
      if(ID3Util.version == 3) {
        bytes =  (ID3Util.getID3v230Frame(getSubFrame(i))).getBytes();
///      } else if(ID3Util.version == 4) {
///        bytes =  ID3Util.getID3v240Frame(this).getBytes();
      }
      System.arraycopy(bytes, 0, content, offset, bytes.length);
      offset += bytes.length;
    }
    return content;
  }
//-------------------------------------------------------------------------------------------------
  public String toString() {
    String string = "Frame ID        = CHAP" + CR + "Size            = " + getContentLength() + CR + "elementId       = " + elementId + CR;
    string += "startByteOffset = " + startByteOffset + CR + "endByteOffset   = " + endByteOffset + CR + "startTime       = " + startTime + CR + "endTime         = " + endTime;
    for(int i=0; i<getNumSubFrames(); i++) {
        ID3v2Frame subFrame = getSubFrame(i);
        string += CR + "SubFrame(" + i + "):" + CR + ID3Util.toString(subFrame);
    }
    return string;
  }
//-------------------------------------------------------------------------------------------------
  /**
   * @param startTime the start time in milliseconds.
   */
  public void setStartTime(long startTime) {
    this.startTime = startTime;
  }
//-------------------------------------------------------------------------------------------------
  /**
   * @return Returns the start time in milliseconds.
   */
  public long getStartTime() {
    return startTime;
  }
//-------------------------------------------------------------------------------------------------
  /**
   * @param endTime the end time in milliseconds.
   */
  public void setEndTime(long endTime) {
    this.endTime = endTime;
  }
//-------------------------------------------------------------------------------------------------
  /**
   * @return Returns the end time in milliseconds.
   */
  public long getEndTime() {
    return endTime;
  }
//-------------------------------------------------------------------------------------------------
  /**
   * @param startByteOffset The startByteOffset to set.
   */
  public void setStartByteOffset(long startByteOffset) {
    this.startByteOffset = startByteOffset;
  }
//-------------------------------------------------------------------------------------------------
  /**
   * @return Returns the startByteOffset.
   */
  public long getStartByteOffset() {
    return startByteOffset;
  }
//-------------------------------------------------------------------------------------------------
  /**
   * @param endByteOffset The endByteOffset to set.
   */
  public void setEndByteOffset(long endByteOffset) {
    this.endByteOffset = endByteOffset;
  }
//-------------------------------------------------------------------------------------------------
  /**
   * @return Returns the endByteOffset.
   */
  public long getEndByteOffset() {
    return endByteOffset;
  }
//-------------------------------------------------------------------------------------------------
}
//=================================================================================================