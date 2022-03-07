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

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.Vector;
import de.vdheide.mp3.ID3v2Frame;
//=================================================================================================
/**
 * Abstract class representing an ID3v2 chapter frame.
 * 
 * @author Chris Newell, BBC R&D.
 */
public abstract class ChapterFrame implements ID3v2Frame {
  protected String frameId;
  protected String elementId;
  protected Vector subFrames = new Vector();
  protected byte[] content;

//-------------------------------------------------------------------------------------------------
 /**
  * 
  */
  protected ChapterFrame(String frameId, String elementId) {
    this.frameId = frameId;
    this.elementId = elementId;
  }
//-------------------------------------------------------------------------------------------------
  /**
   * 
   */
  protected ChapterFrame(ID3v2Frame frame) {
    this.frameId = frame.getID();
    this.content = frame.getContent();
  }
//-------------------------------------------------------------------------------------------------
  public void addSubFrame(ID3v2Frame frame) {
    subFrames.addElement(frame);
  }
//-------------------------------------------------------------------------------------------------
  public ID3v2Frame getSubFrame(int index) {
    ID3v2Frame frame = (ID3v2Frame)(subFrames.elementAt(index));
    return frame;
  }
//-------------------------------------------------------------------------------------------------
  /**
   * @return Returns the elementId.
   */
  public String getElementId() {
    return elementId;
  }
//-------------------------------------------------------------------------------------------------
  public int getNumSubFrames() {
    return subFrames.size();
  }
//-------------------------------------------------------------------------------------------------
  /**
   * @return Returns the subFrames.
   */
  public Vector getSubFrames() {
    return subFrames;
  }
//-------------------------------------------------------------------------------------------------
  public String toString() {
    return elementId;
  }
//-------------------------------------------------------------------------------------------------
// Partial implementation of the frame interface.
//-------------------------------------------------------------------------------------------------
  public void setID(String id) {
    this.frameId = id;  
  }
//-------------------------------------------------------------------------------------------------
  public String getID() {
    return frameId;  
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
  /**
   * Get the title of this chapter frame from the TIT2 subframe if it exists. Otherwise return the elementId.
   * @return the title or null.
   */
  public String getTitle() {
    for(int i=0; i<getNumSubFrames(); i++) {
      ID3v2Frame subFrame = getSubFrame(i);
      if(subFrame.getID().equals("TIT2")) {
        TextFrame tit2 = new TextFrame(subFrame);
        return tit2.getText();
      }
    }
    return null;
  }
//-------------------------------------------------------------------------------------------------
  /**
   * Get the description of this chapter frame from the TIT3 subframe if it exists. Otherwise return null.
   * @return the title or null.
   */
  public String getDescription() {
    for(int i=0; i<getNumSubFrames(); i++) {
      ID3v2Frame subFrame = getSubFrame(i);
      if(subFrame.getID().equals("TIT3")) {
        TextFrame tit3 = new TextFrame(subFrame);
        return tit3.getText();
      }
    }
    return null;
  }
//-------------------------------------------------------------------------------------------------
  /**
   * Get the image associated with this chapter frame from the APIC subframe if it exists. Otherwise return null.
   * @return the title or null.
   */
  public BufferedImage getImage() {
    for(int i=0; i<getNumSubFrames(); i++) {
      ID3v2Frame subFrame = getSubFrame(i);
      if(subFrame.getID().equals("APIC")) {
        ImageFrame apic = new ImageFrame(subFrame);
        return apic.getImage();
      }
    }
    return null;
  }
//------------------------------------------------------------------------------------------------- 
}
//=================================================================================================