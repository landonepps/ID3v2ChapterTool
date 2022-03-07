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

import de.vdheide.mp3.ID3v2Frame;
import bbc.rd.id3.CHAPFrame;
import bbc.rd.id3.ImageFrame;
import bbc.rd.id3.TextFrame;
import bbc.rd.id3.URLFrame;

public class ChapPanel extends JPanel implements ActionListener {
  
  private static final long serialVersionUID = 0;
  private JTextField startTextField = null;
  private JTextField endTextField = null;
  private JLabel startLabel = null;
  private JLabel endLabel = null;
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
  private DefaultListModel listModel = new DefaultListModel();
  private static File file = null;                                      // Remember directory used for image files.
  
//-------------------------------------------------------------------------------------------------
  /**
   * @return Returns the elementId.
   */
  public String getElementId() {
    return elementId;
  }
//-------------------------------------------------------------------------------------------------
  /**
   * @param elementId The elementId to set.
   */
  public void setElementId(String elementId) {
    this.elementId = elementId;
  }
//-------------------------------------------------------------------------------------------------
  /**
   * Constructor
   */
  public ChapPanel(String elementId) {
    super();
    this.elementId = elementId;
    initialize();
  }
//-------------------------------------------------------------------------------------------------
  /**
   * Constructor
   */
  public ChapPanel(String elementId, long startTime, long endTime, Vector subFrames) {
    this(elementId);
    setStartTime(((double)startTime)/1000);
    setEndTime(((double)endTime)/1000);

    for(int i=0; i<subFrames.size(); i++) {
      ID3v2Frame frame =  (ID3v2Frame)subFrames.elementAt(i);
      try {
        if(frame.getID().startsWith("T")) {
          TextFrame textFrame = new TextFrame(frame);
          if(frame.getID().startsWith("TIT2")) {
            titleField.setText(textFrame.getText());
          } else  if(frame.getID().startsWith("TIT3")) {
            descriptionTextArea.setText(textFrame.getText());
          } else {
            listModel.addElement(textFrame);
          }
        } else if(frame.getID().startsWith("W")) {
          URLFrame urlFrame = new URLFrame(frame);
          listModel.addElement(urlFrame);
        } else if(frame.getID().equals("APIC")) {
          ImageFrame imageFrame = new ImageFrame(frame);
          listModel.addElement(imageFrame);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }  
//-------------------------------------------------------------------------------------------------
  /**
   *  
   * @return void
   */
  private void initialize() {
 
    subFrameListLabel = new JLabel();
    subFrameListLabel.setBounds(new java.awt.Rectangle(183,220,69,20));
    subFrameListLabel.setText("Subframes:");
    descriptionLabel = new JLabel();
    descriptionLabel.setBounds(new java.awt.Rectangle(40,100,260,20));
    descriptionLabel.setText("Chapter Description (optional, using TIT3 subframe)");
    titleLabel = new JLabel();
    titleLabel.setBounds(new java.awt.Rectangle(40,45,260,20));
    titleLabel.setText("Chapter Title (optional, using TIT2 subframe)");
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
    startLabel = new JLabel("Start time:");
    startLabel.setBounds(40,15,65,20);
    endLabel = new JLabel("End time:");
    endLabel.setBounds(200,15,61,20);
    this.add(startLabel);
    this.add(getStartTextField());
    this.add(endLabel);
    this.add(getEndTextField());
    //ScrollPane scrollPane = new ScrollPane(getChapList());
    this.add(getListScrollPane(), null);
    this.add(subFrameListLabel, null);
    this.add(getDescriptionScrollPane(), null);
  }
//-------------------------------------------------------------------------------------------------
  /**
   * This method initializes startTextField	
   * 	
   * @return javax.swing.JTextField	
   */
  protected JTextField getStartTextField() {
    if (startTextField == null) {
      startTextField = new JTextField();
      startTextField.setHorizontalAlignment(JTextField.RIGHT);
      startTextField.setEditable(true); 
      startTextField.setBounds(95,15,70,20);
    }
    return startTextField;
  }
//-------------------------------------------------------------------------------------------------
  /**
   * This method initializes endTextField	
   * 	
   * @return javax.swing.JTextField	
   */
  protected JTextField getEndTextField() {
    if (endTextField == null) {
      endTextField = new JTextField();
      endTextField.setHorizontalAlignment(JTextField.RIGHT);
      endTextField.setEditable(true); 
      endTextField.setBounds(new java.awt.Rectangle(260,15,70,20));
    }
    return endTextField;
  }
//-------------------------------------------------------------------------------------------------
  /**
   * This method initializes titleField	
   * 	
   * @return javax.swing.JTextField	
   */
  protected JTextField getTitleField() {
    if (titleField == null) {
      titleField = new JTextField();
      titleField.setBounds(new java.awt.Rectangle(40,70,480,20));
    }
    return titleField;
  }
//-------------------------------------------------------------------------------------------------
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
      addTextButton.setToolTipText("Add an additional text subframe to this chapter.");
      addTextButton.addActionListener(this);
    }
    return addTextButton;
  }
//-------------------------------------------------------------------------------------------------
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
      addURLButton.setToolTipText("Add a URL subframe to this chapter.");
      addURLButton.addActionListener(this);
    }
    return addURLButton;
  }
//-------------------------------------------------------------------------------------------------
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
      imageButton.setToolTipText("Add an image subframe to this chapter.");
      imageButton.addActionListener(this);
    }
    return imageButton;
  }
//-------------------------------------------------------------------------------------------------
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
//-------------------------------------------------------------------------------------------------
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
//-------------------------------------------------------------------------------------------------
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
//-------------------------------------------------------------------------------------------------
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
//-------------------------------------------------------------------------------------------------
  /**
   * This method initializes descriptionTextArea	
   * 	
   * @return javax.swing.JTextArea	
   */
  protected JTextArea getDescriptionTextArea() {
    if (descriptionTextArea == null) {
      descriptionTextArea = new JTextArea();
      descriptionTextArea.setLineWrap(true);
      descriptionTextArea.setFont((new JLabel()).getFont());
    }
    return descriptionTextArea;
  }
//-------------------------------------------------------------------------------------------------
  /**
   * 
   */
  public String toString() {
    return elementId;
  }
//-------------------------------------------------------------------------------------------------
  /**
   * @return Returns the startTime.
   */
  protected double getStartTime() {
    return getTimeInSecondsFromString(startTextField.getText());
  }
//-------------------------------------------------------------------------------------------------
  /**
   * @param startTime The startTime to set.
   */
  protected void setStartTime(double startTime) {
    startTextField.setText(getTimeString(startTime));
  }
//------------------------------------------------------------------------------------------------- 
  /**
   * @return Returns the endTime.
   */
  protected double getEndTime() {
    return getTimeInSecondsFromString(endTextField.getText());
  }
//-------------------------------------------------------------------------------------------------
  /**
   * @param endTime The endTime to set.
   */
  protected void setEndTime(double endTime) {
    endTextField.setText(getTimeString(endTime));
  }
//-------------------------------------------------------------------------------------------------
  public CHAPFrame getFrame() {
    long start = (long)(getStartTime() * 1000);
    long end = (long)(getEndTime() * 1000);
    long startByteOffset = 0xFFFFFFFFL;  // tbd: calculate these offsets correctly.
    long endByteOffset = 0xFFFFFFFFL;
    Vector subFrames = getSubFrames();
    CHAPFrame chap = new CHAPFrame(getElementId(), start, end, startByteOffset, endByteOffset);
    for(int i=0; i<subFrames.size(); i++) {
      ID3v2Frame subFrame = (ID3v2Frame)(subFrames.elementAt(i));
      chap.addSubFrame(subFrame);
    }
    return chap;
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
   * Convert a formatted time string to a count in milliseconds. The method will accept time strings of the form:
   *         [s]s[.nn]
   *       [m]m:ss[.nn]
   *    [h]h:mm:ss[.nn]
   *    
   * or:
   *    
   *    N * s + [.nn]
   * 
   * @param timeString a formatted time string representing hours, minutes, seconds & centiseconds (e.g. "1:05:17.32") or seconds & centiseconds (e.g. "1572.32"). 
   * @return a time in milliseconds
   */
  protected double getTimeInSecondsFromString(String timeString) {
//    double hours = Integer.parseInt(timeString.substring(0, 2));
//    double minutes = Integer.parseInt(timeString.substring(3, 5));
//    double seconds =  Integer.parseInt(timeString.substring(6, 8));
//    double centiseconds = Integer.parseInt(timeString.substring(9, 11));
    
    double hours = 0D;
    double minutes = 0D;
    double seconds = 0D;
    double centiseconds = 0D;
    int firstColonIndex = timeString.indexOf(":");
    int lastColonIndex = timeString.lastIndexOf(":");
    int periodIndex = timeString.lastIndexOf(".");    
     
    if(periodIndex != -1) {
      // Determine centiseconds.
      if(timeString.length() - periodIndex == 2) {
        centiseconds = Integer.parseInt(timeString.substring(periodIndex + 1, periodIndex + 2)) * 10;
      } else if(timeString.length() - periodIndex > 2) {
        centiseconds = Integer.parseInt(timeString.substring(periodIndex + 1, periodIndex + 3));
      }
    } else {
      // There are no centiseconds.
      periodIndex =  timeString.length();
    }
    
    if(lastColonIndex == -1) {
      // There are no hours or minutes. Seconds may exceed 59.
      seconds = Integer.parseInt(timeString.substring(0, periodIndex));
    } else {
      // Determine seconds.
      seconds = Integer.parseInt(timeString.substring(lastColonIndex + 1, periodIndex));
      // Determine minutes.
      if(firstColonIndex == lastColonIndex) {
        minutes = Integer.parseInt(timeString.substring(0, lastColonIndex)); 
      } else {
        minutes = Integer.parseInt(timeString.substring(firstColonIndex + 1, lastColonIndex)); 
      }
      // Determine hours.
      if(firstColonIndex != lastColonIndex && firstColonIndex != 0) {
        hours = Integer.parseInt(timeString.substring(0, firstColonIndex)); 
      }
    }
    
//    System.out.println("timeString = " + timeString);
//    System.out.println("centiseconds = " + centiseconds);
//    System.out.println("seconds = " + seconds);
//    System.out.println("minutes = " + minutes);
//    System.out.println("hours = " + hours);
//    System.out.println();
     
    double time = hours * 3600D + minutes * 60D + seconds + centiseconds * 0.01D;
    return time;
  }
//-------------------------------------------------------------------------------------------------
  /**
   * Return a formatted time string.
   * @param time in seconds
   * @return a formatted time string of the form 00:00:00:00
   */
  protected static String getTimeString(double time) {
    int hours = (int)(time / 3600);
    int minutes =  (int)((time - hours * 3600) / 60);
    int seconds =  (int)(time - hours * 3600 - minutes * 60);
    int centiseconds =  (int)((time - (int)time) * 100);
    String timeString = getTimeElementString(hours) + ":" + getTimeElementString(minutes) + ":" + getTimeElementString(seconds) + "." + getTimeElementString(centiseconds);
    return timeString;
  }
//-------------------------------------------------------------------------------------------------
  protected static String getTimeElementString(int element) {
    String elementString = Integer.toString(element);
    if(elementString.length() == 1) {
     elementString = "0" + elementString;
    }
    return elementString;
  }
//-------------------------------------------------------------------------------------------------
  /**
   * ActionListener interface.
   */
  public void actionPerformed(ActionEvent e) {
    // System.out.println("ActionCommand = " + e.getActionCommand());
//-------------------------------------------------------------------------------------------------
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