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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

//=================================================================================================
public class ImagePanel extends JPanel {
  
  private static final long serialVersionUID = 0;
  private BufferedImage image;

//-------------------------------------------------------------------------------------------------
  /**
   * This is the default constructor
   */
  public ImagePanel() {
    super();
    this.setBorder(new EmptyBorder(0,0,0,0));
  }
//-------------------------------------------------------------------------------------------------
  public void setImage(BufferedImage image) {
    this.image = image;
    repaint();
  }
//------------------------------------------------------------------------------------------------- 
  public void paint(Graphics g ) {
    if(image != null) {
      int d = Math.min(this.getWidth(), this.getHeight());
      g.drawImage(image, 0, 0, d, d, null);
    }
  }
//-------------------------------------------------------------------------------------------------
}
//=================================================================================================