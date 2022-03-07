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
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import de.vdheide.mp3.ID3v2Frame;
//=================================================================================================
/**
 * Class representing an ID3v2 "APIC" frame.
 * 
 * @author Chris Newell, BBC R&D.
 */
public class ImageFrame implements ID3v2Frame {
  protected static final String FRAME_ID = "APIC";
  protected static final String enc = "ISO-8859-1";
  protected int encoding;
  protected String description;
  protected String mimeType;
  protected int type;
  protected String path;
  protected String fileName = "";
  protected byte[] imageBytes;
//-------------------------------------------------------------------------------------------------
  /**
   */
  public ImageFrame() {
    this.encoding = 0x00;                    // This class only supports ISO-8859-1
  }
//-------------------------------------------------------------------------------------------------
  /**
   */
  public ImageFrame(String description, int type, File imageFile) throws IOException {
    this.encoding = 0x00;                    // This class only supports ISO-8859-1
    this.description = description;
    this.type = type;
    setImage(imageFile);
  }
//-------------------------------------------------------------------------------------------------
  /**
   */
  public ImageFrame(ID3v2Frame frame) {
    byte[] content = frame.getContent();
    encoding = content[0];
    int i;
    for(i=1; i < content.length && content[i] != 0; i++) {}           // Find first null.
    try {
      mimeType = new String(content, 1, i-1, "ISO-8859-1");
      i++;                                                              // Skip null.
      type = content[i];
      i++;
      int start = i;
      for(; i < content.length && content[i] != 0; i++) {}              // Find second null.
      description = new String(content, start, i-start, "ISO-8859-1");
      i++;                                                              // Skip null.
      imageBytes = new byte[content.length - i];
      System.arraycopy(content, i, imageBytes, 0, imageBytes.length);
    } catch (UnsupportedEncodingException e) {
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
  public String getDescription() {
    return description;
  }
//------------------------------------------------------------------------------------------------- 
  /**
   * @return Returns the type.
   */
  public int getType() {
    return type;
  }
//------------------------------------------------------------------------------------------------- 
  /**
   * @return Returns the MIME type.
   */
  public String getMimeType() {
    return mimeType;
  }
//------------------------------------------------------------------------------------------------- 
  /**
   * @return Returns the image bytes.
   */
  public byte[] getImageBytes() {
    return imageBytes;
  }
//------------------------------------------------------------------------------------------------- 
  public int getContentLength() {
    return 4 + mimeType.length() + description.length() + imageBytes.length;         // ISO-8859-1 (8-bit) assumed.
  }
//-------------------------------------------------------------------------------------------------
  public int getLength() {
    return getContentLength() + 10;          // Add frame header length;
  }
//-------------------------------------------------------------------------------------------------
  public byte[] getContent() {
    byte[] content = new byte[getContentLength()];
    content[0] = (byte)encoding;
    try {
      byte[] mimeTypeBytes = mimeType.getBytes(enc);
      byte[] descriptionBytes = description.getBytes(enc);

      System.arraycopy(mimeTypeBytes, 0, content, 1, mimeTypeBytes.length);
      content[mimeTypeBytes.length + 1] = 0x00;                                                            // Null separator.
      content[mimeTypeBytes.length + 2] = (byte)type;                                                      // Picture type.
      System.arraycopy(descriptionBytes, 0, content, mimeTypeBytes.length + 3, descriptionBytes.length);
      content[mimeTypeBytes.length + descriptionBytes.length + 3] = 0;                                     // Null separator.
      System.arraycopy(imageBytes, 0, content, mimeTypeBytes.length + descriptionBytes.length + 4, imageBytes.length);
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
  /**
   * @param description The description to set.
   */
  public void setDescription(String description) {
    this.description = description;
  }
//-------------------------------------------------------------------------------------------------
  /**
   * Set the image carried by this ImageFrame.
   * 
   * @param imageFile the image file.
   */
  public void setImage(File imageFile) throws IOException {
    String name = imageFile.getName();
    if(name.endsWith("png")) {
      mimeType = "image/png";
    } else if(name.endsWith("jpg") || name.endsWith("jpeg")) {
      mimeType = "image/jpeg";
    } else {
      mimeType = "image/";
    }
    int size;
    BufferedInputStream bis = new BufferedInputStream(new FileInputStream(imageFile));
    size = (int)imageFile.length();
    imageBytes = new byte[size];
    int bytes_read = 0;
    while(bytes_read < size) {
      bytes_read += bis.read(imageBytes, bytes_read, size-bytes_read);
    }
    bis.close();
    path = imageFile.getPath();
    fileName = " : " + imageFile.getName();
  }
//------------------------------------------------------------------------------------------------- 
  public BufferedImage getImage() {
    BufferedImage image = null;
    Iterator readers = ImageIO.getImageReadersByMIMEType(getMimeType());
    ImageReader reader = (ImageReader)readers.next();
    try {
      ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(getImageBytes()));
      reader.setInput(iis, true);
      image = reader.read(0);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return image;
  }
//-------------------------------------------------------------------------------------------------
  /**
   * @param type The type to set.
   */
  public void setType(int type) {
    this.type = type;
  }
//-------------------------------------------------------------------------------------------------
  /**
   * @return the path of the image carried in this frame (if known).
   */
  public String getPath() {
    return path;
  }
//-------------------------------------------------------------------------------------------------
  public void setID(String frameId) {
  }
//-------------------------------------------------------------------------------------------------
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