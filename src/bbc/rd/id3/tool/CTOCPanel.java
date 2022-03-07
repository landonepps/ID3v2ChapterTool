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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextArea;
import javax.swing.JButton;
import javax.swing.JCheckBox;

import de.vdheide.mp3.ID3v2Frame;
import bbc.rd.id3.CTOCFrame;
import bbc.rd.id3.ImageFrame;
import bbc.rd.id3.TextFrame;
import bbc.rd.id3.URLFrame;

public class CTOCPanel extends JPanel implements ActionListener {
  
  private static final long serialVersionUID = 0;
  private String elementId;
  private JTextField titleField = null;
  private JLabel titleLabel = null;
  private JLabel descriptionLabel = null;
  private JButton addTextButton = null;
  private JButton addURLButton = null;
  private JButton imageButton = null;
  private JButton removeButton = null;
  private JScrollPane listScrollPane = null;
  private JList subframeList = null;
  private JLabel subFrameListLabel = null;
  private JScrollPane descriptionScrollPane = null;
  private JTextArea descriptionTextArea = null;
  private JLabel orderedLabel = null;
  private JCheckBox orderedCheckBox = null;
  private DefaultListModel listModel = new DefaultListModel();
  private static File file = null;                                      // Remember directory used for image files.
  
  /**
   * @return Returns the elementId.
   */
  public String getElementId() {
    return elementId;
  }

  /**
   * @param elementId The elementId to set.
   */
  public void setElementId(String elementId) {
    this.elementId = elementId;
  }

  /**
   * Constructor
   */
  public CTOCPanel(String elementId) {
    super();
    this.elementId = elementId;
    initialize();
  }

  /**
   * Constructor
   */
  public CTOCPanel(CTOCFrame frame) {
    this(frame.getElementId());
    orderedCheckBox.setSelected(frame.isOrdered());

    for(int i=0; i<frame.getNumSubFrames(); i++) {
      ID3v2Frame subframe = frame.getSubFrame(i);
      try {
        if(subframe.getID().startsWith("T")) {
          TextFrame textFrame = new TextFrame(subframe);
          if(subframe.getID().startsWith("TIT2")) {
            titleField.setText(textFrame.getText());
          } else  if(subframe.getID().startsWith("TIT3")) {
            descriptionTextArea.setText(textFrame.getText());
          } else {
            listModel.addElement(textFrame);
          }
        } else if(subframe.getID().startsWith("W")) {
          URLFrame urlFrame = new URLFrame(subframe);
          listModel.addElement(urlFrame);
        } else if(subframe.getID().equals("APIC")) {
          ImageFrame imageFrame = new ImageFrame(subframe);
          listModel.addElement(imageFrame);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
  
  /**
   * This method initializes this
   * 
   * @return void
   */
  private void initialize() {
 
    orderedLabel = new JLabel();
    orderedLabel.setBounds(new java.awt.Rectangle(40,15,58,20));
    orderedLabel.setText("Ordered:");
    subFrameListLabel = new JLabel();
    subFrameListLabel.setBounds(new java.awt.Rectangle(183,220,69,21));
    subFrameListLabel.setText("Subframes:");
    descriptionLabel = new JLabel();
    descriptionLabel.setBounds(new java.awt.Rectangle(40,100,257,20));
    descriptionLabel.setText("TOC Description (optional, using TIT3 subframe)");
    titleLabel = new JLabel();
    titleLabel.setBounds(new java.awt.Rectangle(40,44,220,20));
    titleLabel.setText("TOC Title (optional, using TIT2 subframe)");
    setLayout(null);
    this.setPreferredSize(new Dimension(350,440));
    this.setSize(new java.awt.Dimension(350,440));
    this.add(getTitleField(), null);
    this.add(titleLabel, null);
    this.add(descriptionLabel, null);
    this.add(getAddTextButton(), null);
    this.add(getAddURLButton(), null);
    this.add(getRemoveButton(), null);
    this.add(getImageButton(), null);
    this.add(getListScrollPane(), null);
    this.add(subFrameListLabel, null);
    //scrollPane.setBounds(245,60,40,140);
    this.add(getDescriptionScrollPane(), null);
    //this.add(scrollPane, null);
    this.add(orderedLabel, null);
    this.add(getOrderedCheckBox(), null);
  }

  /**
   * This method initializes titleField	
   * 	
   * @return javax.swing.JTextField	
   */
  private JTextField getTitleField() {
    if (titleField == null) {
      titleField = new JTextField();
      titleField.setBounds(new java.awt.Rectangle(40,70,480,20));
    }
    return titleField;
  }

  /**
   * This method initializes addTextButton	
   * 	
   * @return javax.swing.JButton	
   */
  private JButton getAddTextButton() {
    if (addTextButton == null) {
      addTextButton = new JButton();
      addTextButton.setBounds(new java.awt.Rectangle(40,220,94,40));
      addTextButton.setHorizontalTextPosition(javax.swing.SwingConstants.TRAILING);
      addTextButton.setText("<html>Add Text<br>subframe</html>");
      addTextButton.setActionCommand("addTextFrame");
      addTextButton.setToolTipText("Add an additional text subframe to this level in the Table of Contents.");
      addTextButton.addActionListener(this);
    }
    return addTextButton;
  }

  /**
   * This method initializes addURLButton	
   * 	
   * @return javax.swing.JButton	
   */
  private JButton getAddURLButton() {
    if (addURLButton == null) {
      addURLButton = new JButton();
      addURLButton.setBounds(new java.awt.Rectangle(40,280,94,40));
      addURLButton.setText("<html>Add URL<br>subframe</html>");
      addURLButton.setActionCommand("addURLFrame");
      addURLButton.setToolTipText("Add a URL subframe to this level in the Table of Contents.");
      addURLButton.addActionListener(this);
    }
    return addURLButton;
  }

  /**
   * This method initializes addImageButton	
   * 	
   * @return javax.swing.JButton	
   */
  private JButton getImageButton() {
    if (imageButton == null) {
      imageButton = new JButton();
      imageButton.setBounds(new java.awt.Rectangle(40,340,94,40));
      imageButton.setText("<html>Add Image<br>subframe</html>");
      imageButton.setActionCommand("addImageFrame");
      imageButton.setToolTipText("Add an image subframe to this level in the Table of Contents.");
      imageButton.addActionListener(this);
    }
    return imageButton;
  }

  /**
   * This method initializes removeButton	
   * 	
   * @return javax.swing.JButton	
   */
  private JButton getRemoveButton() {
    if (removeButton == null) {
      removeButton = new JButton();
      removeButton.setBounds(new java.awt.Rectangle(40,400,94,40));
      removeButton.setText("<html>Remove<br>subframe</html>");
      removeButton.setActionCommand("removeFrame");
      removeButton.setToolTipText("Remove the selected subframe from the subframes list.");
      removeButton.addActionListener(this);
    }
    return removeButton;
  }

  /**
   * This method initializes listScrollPane	
   * 	
   * @return javax.swing.JScrollPane	
   */
  private JScrollPane getListScrollPane() {
    if (listScrollPane == null) {
      listScrollPane = new JScrollPane();
      listScrollPane.setBounds(new java.awt.Rectangle(260,220,260,220));
      listScrollPane.setViewportView(getSubframeList());
    }
    return listScrollPane;
  }

  /**
   * This method initializes subframeList   
   *    
   * @return javax.swing.JList  
   */
  private JList getSubframeList() {
    if (subframeList == null) {
      subframeList = new JList(listModel);
    }
    return subframeList;
  }

  /**
   * This method initializes descriptionScrollPane	
   * 	
   * @return javax.swing.JScrollPane	
   */
  private JScrollPane getDescriptionScrollPane() {
    if (descriptionScrollPane == null) {
      descriptionScrollPane = new JScrollPane();
      descriptionScrollPane.setBounds(new java.awt.Rectangle(40,126,480,66));
      descriptionScrollPane.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
      descriptionScrollPane.setViewportView(getDescriptionTextArea());
    }
    return descriptionScrollPane;
  }

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

  /**
   * This method initializes orderedCheckBox	
   * 	
   * @return javax.swing.JCheckBox	
   */
  private JCheckBox getOrderedCheckBox() {
    if (orderedCheckBox == null) {
      orderedCheckBox = new JCheckBox();
      orderedCheckBox.setSelected(true);
      orderedCheckBox.setBounds(new java.awt.Rectangle(90,15,20,20));
    }
    return orderedCheckBox;
  }

  /**
   * 
   */
  public String toString() {
    return elementId;
  }
//-------------------------------------------------------------------------------------------------
  public CTOCFrame getFrame() {
    Vector subFrames = getSubFrames();
    CTOCFrame ctoc = new CTOCFrame(getElementId(), false, orderedCheckBox.isSelected());
    for(int i=0; i<subFrames.size(); i++) {
      ID3v2Frame subFrame = (ID3v2Frame)(subFrames.elementAt(i));
      ctoc.addSubFrame(subFrame);
    }
    return ctoc;
  }
//-------------------------------------------------------------------------------------------------
  public Vector getSubFrames() {
    Vector subFrames = new Vector();
    
    String title = titleField.getText().trim();
    if(title.length() > 0) {
      TextFrame titleFrame = new TextFrame("TIT2", title);
      subFrames.addElement(titleFrame);
    }
    
    String description = descriptionTextArea.getText().trim();
    if(description.length() > 0) {
      TextFrame descriptionFrame = new TextFrame("TIT3", description);
      subFrames.addElement(descriptionFrame);
    }
    
    for(int i=0; i<listModel.getSize(); i++) {
      Object element = listModel.getElementAt(i);
      subFrames.addElement(element);
    }
    return subFrames;
  }
//-------------------------------------------------------------------------------------------------
  /**
   * ActionListener interface.
   */
  public void actionPerformed(ActionEvent e) {
    // System.out.println("ActionCommand = " + e.getActionCommand());
//  -------------------------------------------------------------------------------------------------
    if(e.getActionCommand() == "addTextFrame") {
      TextFramePanel textFramePanel = new TextFramePanel();
      if(ComponentDialog.showDialog(this, textFramePanel, "Add Text Frame", true) != ComponentDialog.OK_ACTION) {
        return;
      }
      TextFrame textFrame = new TextFrame(textFramePanel.getFrameId(), textFramePanel.getText());
      listModel.addElement(textFrame);
      subframeList.setSelectedIndex(listModel.getSize() - 1);
    }
//-------------------------------------------------------------------------------------------------
    if(e.getActionCommand() == "addURLFrame") {
      URLFramePanel urlFramePanel = new URLFramePanel();
      if(ComponentDialog.showDialog(this, urlFramePanel, "Add Text Frame", true) != ComponentDialog.OK_ACTION) {
        return;
      }
      URLFrame urlFrame = new URLFrame(urlFramePanel.getFrameId(), urlFramePanel.getURL());
      listModel.addElement(urlFrame);
      subframeList.setSelectedIndex(listModel.getSize() - 1);
    }
//-------------------------------------------------------------------------------------------------
    if(e.getActionCommand() == "addImageFrame") {
      JFileChooser fileChooser = new JFileChooser(file);
      if(fileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
        return;
      }
      file = fileChooser.getSelectedFile();
      ImageFramePanel imageFramePanel = new ImageFramePanel(file);
      if(ComponentDialog.showDialog(this, imageFramePanel, "Add Image Frame", true) != ComponentDialog.OK_ACTION) {
        return;
      }
      try {
        ImageFrame imageFrame = imageFramePanel.getFrame();
        listModel.addElement(imageFrame);
        subframeList.setSelectedIndex(listModel.getSize() - 1);
      } catch (Exception exc) {
        exc.printStackTrace();
      }
    }
//-------------------------------------------------------------------------------------------------
    if(e.getActionCommand() == "removeFrame") {
      int selectedIndex = subframeList.getSelectedIndex();
      if(selectedIndex != -1 && selectedIndex < listModel.getSize()) {
        listModel.removeElementAt((selectedIndex));
        subframeList.setSelectedIndex(Math.min(selectedIndex, listModel.getSize()-1));
      }
    }
  }
//-------------------------------------------------------------------------------------------------
}
//=================================================================================================
