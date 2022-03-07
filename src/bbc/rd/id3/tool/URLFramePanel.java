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

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JLabel;

//=================================================================================================
public class URLFramePanel extends JPanel {
  
  private static final long serialVersionUID = 0;
  private JComboBox frameIdComboBox = null;
  private JTextField urlTextField;
  private JLabel frameIdLabel;
  private JLabel urlLabel;
  private   String[] frameIdStrings = { "WCOM", "WCOP", "WFED", "WOAF", "WOAR", "WOAS", "WORS", "WPAY", "WPUB"};
  private static int lastSelectedIndex = 8;          // "WPUB" initially.
  private static String lastURL = "http://";
//-------------------------------------------------------------------------------------------------
  /**
   * This is the default constructor
   */
  public URLFramePanel() {
    super();
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
    this.setPreferredSize(new Dimension(240,50));
    this.setSize(new java.awt.Dimension(240,50));
    frameIdLabel = new JLabel("Frame ID:");
    frameIdLabel.setBounds(0,0,60,20);
    this.add(frameIdLabel, null);
    this.add(getFrameIdComboBox(), null);
    urlLabel = new JLabel("URL:");
    urlLabel.setBounds(new java.awt.Rectangle(0,30,60,20));
    this.add(urlLabel, null);
    this.add(getURLField(), null);
  }
//-------------------------------------------------------------------------------------------------
  /**
   * This method initializes FrameIdComboBox
   *    
   * @return javax.swing.JCombobox
   */
  private JComboBox getFrameIdComboBox() {
    if(frameIdComboBox == null) {
      frameIdComboBox = new JComboBox(frameIdStrings);
      frameIdComboBox.setSelectedIndex(lastSelectedIndex);
      frameIdComboBox.setBounds(50,0,60,20);
    }
    return frameIdComboBox;
  }
//-------------------------------------------------------------------------------------------------
  /**
   * This method initializes startTextField 
   *    
   * @return javax.swing.JTextField 
   */
  private JTextField getURLField() {
    if(urlTextField == null) {
      urlTextField = new JTextField(lastURL);
      urlTextField.setHorizontalAlignment(JTextField.LEFT);
      urlTextField.setBounds(50,30,240,20);
    }
    return urlTextField;
  }
//-------------------------------------------------------------------------------------------------
  /**
   * @return Returns the frameId.
   */
  public String getFrameId() {
    lastSelectedIndex = frameIdComboBox.getSelectedIndex();
    return frameIdComboBox.getSelectedItem().toString();
  }
//-------------------------------------------------------------------------------------------------
  /**
   * Set the frameId.
   */
  public void setFrameId(String frameId) {
    frameIdComboBox.setSelectedItem(frameId);
  }
//-------------------------------------------------------------------------------------------------
  /**
   * @return Returns the URL string.
   */
  public String getURL() {
    lastURL = urlTextField.getText();
    return lastURL;
  }
//-------------------------------------------------------------------------------------------------
  /**
   * @param url The URL to set.
   */
  public void setURL(String url) {
    this.urlTextField.setText(url);
  }
//-------------------------------------------------------------------------------------------------
  /**
   * 
   */
  public String toString() {
    return getFrameId();
  }
//-------------------------------------------------------------------------------------------------
}
//=================================================================================================