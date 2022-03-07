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
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.JTextArea;

//=================================================================================================
public class TextFramePanel extends JPanel {
  
  private static final long serialVersionUID = 0;
  private JComboBox frameIdComboBox = null;
  private JScrollPane textScrollPane;
  private JTextArea textAreaField = null;
  private JLabel frameIdLabel;
  private JLabel textAreaLabel;
  
// ID3v2.3:
  private String[] frameIdStrings = { "TALB", "TBPM", "TCOM", "TCON", "TCOP", "TDAT", "TDES", "TDLY", "TENC", "TEXT", "TFLT", "TIME", "TIT1", "TIT2", "TIT3", "TKEY", "TLAN", "TLEN", "TMED", "TOAL", "TOFN", "TOLY", "TOPE", "TORY", "TOWN", "TPE1", "TPE2", "TPE3", "TPE4", "TPOS", "TPUB", "TRCK", "TRDA", "TRSN", "TRS0", "TSIZ", "TSRC", "TSSE", "TYER" };

// ID3v2.4:
// private String[] frameIdStrings = { "TALB", "TBPM", "TCOM", "TCON", "TCOP", "TDEN", "TDES", "TDLY",  "TDOR", "TDRC", "TDRL", "TDTG", "TENC", "TEXT", "TFLT", "TGID", "TIPL", "TIT1", "TIT2", "TIT3", "TKEY", "TLAN", "TLEN", "TMCL", "TMED", "TMOO", "TOAL", "TOFN", "TOLY", "TOPE", "TOWN", "TPE1", "TPE2", "TPE3", "TPE4", "TPOS", "TPRO", "TPUB", "TRCK", "TRSN", "TRS0", "TSOA", "TSOP", "TSOT", "TSRC", "TSSE", "TSST", "TYER" };

  private static int lastSelectedIndex = 0;      //  "TALB" is default.
//-------------------------------------------------------------------------------------------------
  /**
   * This is the default constructor
   */
  public TextFramePanel() {
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
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    this.setPreferredSize(new Dimension(240,110));
    this.setSize(new java.awt.Dimension(240,110));
    frameIdLabel = new JLabel("Frame ID:");
    frameIdLabel.setBounds(0,0,60,20);
    JPanel panel = new JPanel(null);
    panel.setMaximumSize(new Dimension(240,50));
    panel.setPreferredSize(new Dimension(240,50));
    //panel.setAlignmentX(Component.LEFT_ALIGNMENT);
    panel.add(frameIdLabel, null);
    panel.add(getFrameIdComboBox(), null);
    textAreaLabel = new JLabel("Text:");
    textAreaLabel.setBounds(new java.awt.Rectangle(0,30,60,20));
    panel.add(textAreaLabel, null);
    this.add(panel, null);
    this.add(getTextScrollPane(), null);
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
   * This method initializes descriptionScrollPane  
   *    
   * @return javax.swing.JScrollPane    
   */
  private JScrollPane getTextScrollPane() {
    if (textScrollPane == null) {
      textScrollPane = new JScrollPane();
      textScrollPane.setBounds(new java.awt.Rectangle(0,155,200,60));
      textScrollPane.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
      textScrollPane.setViewportView(getTextAreaField());
    }
    return textScrollPane;
  }
//-------------------------------------------------------------------------------------------------
  /**
   * This method initializes textAreaField.	
   * 	
   * @return javax.swing.JTextArea	
   */
  private JTextArea getTextAreaField() {
    if (textAreaField == null) {
      textAreaField = new JTextArea();
      textAreaField.setLineWrap(true);
    }
    return textAreaField;
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
   * @return Returns the text.
   */
  public String getText() {
    return textAreaField.getText();
  }
//-------------------------------------------------------------------------------------------------
  /**
   * @param text The text to set.
   */
  public void setText(String text) {
    this.textAreaField.setText(text);
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