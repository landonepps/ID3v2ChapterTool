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
import java.io.File;
import java.io.IOException;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.JTextArea;

import de.vdheide.mp3.ID3v2DecompressionException;

import bbc.rd.id3.ImageFrame;

//=================================================================================================
public class ImageFramePanel extends JPanel {

  private static final long serialVersionUID = 0;
  private static String frameId = "APIC";
  private JComboBox typeComboBox = null;
  private JScrollPane descriptionScrollPane;
  private JTextArea descriptionTextArea;
  private JLabel frameIdLabel;
  private JLabel descriptionLabel;
  private JLabel typeLabel;
  private JLabel fileLabel;
  private File file;
  private ImageFrame imageFrame = null;
  private static int lastSelectedIndex = 0;      // "Other" is default.
  
  private String[] typeStrings = {
      "Other",
      "32x32 pixels 'file icon' (PNG only)",
      "Other file icon",
      "Cover (front)",
      "Cover (back)",
      "Leaflet page",
      "Media (e.g. label side of CD)",
      "Lead artist/lead performer/soloist",
      "Artist/performer",
      "Conductor",
      "Band/Orchestra",
      "Composer",
      "Lyricist/text writer",
      "Recording Location",
      "During recording",
      "During performance",
      "Movie/video screen capture",
      "A bright coloured fish",
      "Illustration",
      "Band/artist logotype",
      "Publisher/Studio logotype"
    };

//-------------------------------------------------------------------------------------------------
  /**
   * Constructor
   */
  public ImageFramePanel(File file) {
    super();
    this.file = file;
    initialize();
  }
//-------------------------------------------------------------------------------------------------
  /**
   * Constructor
   */
  public ImageFramePanel(ImageFrame imageFrame) {
    super();
    this.imageFrame = imageFrame;
    initialize();
  }
//-------------------------------------------------------------------------------------------------
  /**
   * This method initializes this
   * 
   * @return void
   */
  private void initialize() {
    setLayout(null);
    this.setPreferredSize(new Dimension(220,200));
    this.setSize(new java.awt.Dimension(220,200));
    frameIdLabel = new JLabel("Frame ID:  " + frameId);
    frameIdLabel.setBounds(0,0,200,20);
    this.add(frameIdLabel, null);
    fileLabel = new JLabel("Image File:  " + file.getName());
    fileLabel.setBounds(new java.awt.Rectangle(0,30,200,20));
    this.add(fileLabel, null);
    typeLabel = new JLabel("Image type:");
    typeLabel.setBounds(new java.awt.Rectangle(0,60,60,20));
    this.add(typeLabel, null);
    this.add(getTypeComboBox(), null);
    descriptionLabel = new JLabel("Image Description:");
    descriptionLabel.setBounds(new java.awt.Rectangle(0,105,160,30));
    this.add(descriptionLabel, null);
    this.add(getDescriptionScrollPane(), null);
  }
//-------------------------------------------------------------------------------------------------
  /**
   * This method initializes typeComboBox
   *    
   * @return javax.swing.JCombobox
   */
  private JComboBox getTypeComboBox() {
    if(typeComboBox == null) {
      typeComboBox = new JComboBox(typeStrings);
      typeComboBox.setSelectedIndex(lastSelectedIndex);
      typeComboBox.setBounds(0,80,200,20);
    }
    return typeComboBox;
  }
//-------------------------------------------------------------------------------------------------
  /**
   * This method initializes descriptionScrollPane  
   *    
   * @return javax.swing.JScrollPane    
   */
  private JScrollPane getDescriptionScrollPane() {
    if (descriptionScrollPane == null) {
      descriptionScrollPane = new JScrollPane();
      descriptionScrollPane.setBounds(new java.awt.Rectangle(0,135,200,60));
      descriptionScrollPane.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
      descriptionScrollPane.setViewportView(getDescriptionTextArea());
    }
    return descriptionScrollPane;
  }
//-------------------------------------------------------------------------------------------------
  /**
   * This method initializes descriptionTextArea    
   *    
   * @return javax.swing.JTextArea  
   */
  private JTextArea getDescriptionTextArea() {
    if (descriptionTextArea == null) {
      descriptionTextArea = new JTextArea();
      descriptionTextArea.setLineWrap(true);
      descriptionTextArea.setFont((new JLabel()).getFont());
    }
    return descriptionTextArea;
  }
//-------------------------------------------------------------------------------------------------
  /**
   * @return Returns the frameId.
   */
  public String getFrameId() {
    return frameId;
  }
//-------------------------------------------------------------------------------------------------
  /**
   * @return Returns the URL string.
   */
  public String getDescription() {
    return descriptionTextArea.getText();
  }
//-------------------------------------------------------------------------------------------------
  /**
   * @return Returns the URL string.
   */
  public File getImageFile() {
    return file;
  }
//-------------------------------------------------------------------------------------------------
  protected int getType() {
    lastSelectedIndex = typeComboBox.getSelectedIndex();
    return lastSelectedIndex;
  }
//-------------------------------------------------------------------------------------------------
  /**
   * @return Get an APIC frame corresponding to this ImagePanel.
   */
  public ImageFrame getFrame() throws ID3v2DecompressionException, IOException {
    if(imageFrame == null) {
      return new ImageFrame(getDescription(), getType(), file);
    } else {
      return imageFrame;
    }
  }
//-------------------------------------------------------------------------------------------------
  /**
   * 
   */
  public String toString() {
    return frameId;
  }
//-------------------------------------------------------------------------------------------------
}
//=================================================================================================