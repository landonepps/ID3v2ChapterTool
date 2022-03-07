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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.JButton;

import org.xml.sax.SAXException;

import bbc.rd.id3.CHAPFrame;
import bbc.rd.id3.CTOCFrame;
import bbc.rd.id3.ChapterFrame;
import bbc.rd.id3.ImageFrame;
import bbc.rd.id3.URLFrame;
import bbc.rd.id3.ID3Util;
import de.vdheide.mp3.ID3v2;
import de.vdheide.mp3.ID3v2Frame;


//=================================================================================================
public class ID3v2ChapterTool extends JFrame implements ActionListener, MouseListener {

  private static final long serialVersionUID = 1L;
  private File audioFile= null;
  private File xmlFile= null;
  private JPanel contentPane = null;
  private JSplitPane jSplitPane = null;
  private JPanel rhsPanel = null;
  private JTabbedPane tabbedPane = null;
  private JScrollPane treeScrollPane = null;
  private ChapterTree tree = null;
  private JMFPanel jmfPanel = null;
  private JPanel toolPanel = null;
  private JButton addChapButton = null;
  private JButton addTOCButton = null;
  private JButton addToTOCButton = null;
  private JButton removeEntryButton = null;
  private JButton backButton = null;
  private JButton startButton = null;
  private JButton endButton = null;
  private JButton nudgeLeftButton = null;
  private JButton nudgeRightButton = null;
  private JButton nextButton = null;
  private JMenuItem openMenuItem;
  private JMenuItem saveMenuItem;
  private JMenuItem saveAsMenuItem;
  private JMenuItem importMenuItem;
  private JMenuItem exportMenuItem;
  private JMenuItem exitMenuItem;
  private boolean showAddChapToRootDialog = true;
  private int chapterIndex = 1;
  private int tocIndex = 1;
  private ChapPanel lastChapPanel = null;
  private double mediaEndTime = 0;
  private double nudgeIncrement = 0.2D;          // In seconds.
//-------------------------------------------------------------------------------------------------
  /**
   * @param args
   */
  public static void main(String[] args) {
    new ID3v2ChapterTool();
  }
//-------------------------------------------------------------------------------------------------
  /**
   * This is the default constructor
   */
  public ID3v2ChapterTool() {
    super();
    initialize();
    this.setVisible(true);
  }
//-------------------------------------------------------------------------------------------------
  /**
   * This method initializes this Frame.
   * 
   * @return void
   */
  private void initialize() {
    this.setSize(new Dimension(760, 710));
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.setJMenuBar(createMenuBar());
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {
      e.printStackTrace();
    }  
    this.setTitle("ID3v2ChapterTool");
    contentPane = new JPanel();
    contentPane.setLayout(new BorderLayout());
    contentPane.add(getJSplitPane(), BorderLayout.CENTER);
    this.setContentPane(contentPane);
  }
//-------------------------------------------------------------------------------------------------
  /**
   * This method initializes jSplitPane	
   * 	
   * @return javax.swing.JSplitPane	
   */
  private JSplitPane getJSplitPane() {
    if(jSplitPane == null) {
      jSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, getTreeScrollPane(), getRHSPanel());
      jSplitPane.setDividerLocation(160);
    }
    return jSplitPane;
  }
//-------------------------------------------------------------------------------------------------
  /**
   * This method initializes the RHS Panel 
   *    
   * @return javax.swing.JPanel 
   */
  private JPanel getRHSPanel() {
    rhsPanel = new JPanel();
    rhsPanel.setLayout(new BoxLayout(rhsPanel, BoxLayout.PAGE_AXIS));
    rhsPanel.add(getJMFPanel());
    rhsPanel.add(getToolPanel());
    rhsPanel.add(getTabbedPane());
    return rhsPanel;
  }
//-------------------------------------------------------------------------------------------------
  /**
   * This method initializes jTabbedPane	
   * 	
   * @return javax.swing.JTabbedPane	
   */
  private JTabbedPane getTabbedPane() {
    tabbedPane = new JTabbedPane();
    tabbedPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), " ID3v2 Chapter Frames "));
    tabbedPane.setPreferredSize(new Dimension(250, 520));
    return tabbedPane;
  }
//-------------------------------------------------------------------------------------------------
/**
   * This method initializes chapPanel	
   * 	
   * @return bbc.rd.id3.tool.ChapPanel	
   */
  private ChapPanel getChapPanel(String elementId) {
    ChapPanel chapPanel = new ChapPanel(elementId);
    double lastEndTime = 0;
    if(lastChapPanel != null) {
      lastEndTime = lastChapPanel.getEndTime();
    }
    chapPanel.setStartTime(lastEndTime);
    if(mediaEndTime == 0) mediaEndTime = Math.max(0, jmfPanel.getMediaEndTime());
    chapPanel.setEndTime(mediaEndTime);
    return chapPanel;
  }
//-------------------------------------------------------------------------------------------------
  /**
   * This method initializes ctocPanel  
   *    
   * @return bbc.rd.id3.tool.CTOCPanel  
   */
  private CTOCPanel getCTOCPanel(String elementId) {
    CTOCPanel ctocPanel = new CTOCPanel(elementId);
    return ctocPanel;
  }
//-------------------------------------------------------------------------------------------------
  /**
   * This method initializes treeScrollPane	
   * 	
   * @return javax.swing.JScrollPane	
   */
  private JScrollPane getTreeScrollPane() {
    treeScrollPane = new JScrollPane();
    treeScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), " Table of Contents "));
    treeScrollPane.setViewportView(getTree());
    return treeScrollPane;
  }
//-------------------------------------------------------------------------------------------------
  /**
   * This method initializes tree	
   * 	
   * @return javax.swing.JTree	
   */
  private JTree getTree() {
    tree = new ChapterTree();
    tree.addMouseListener(this);
    return tree;
  }
//-------------------------------------------------------------------------------------------------
  /**
   * This method initializes jmfPanel	
   * 	
   * @return javax.swing.JPanel	
   */
  private JPanel getJMFPanel() {
    if (jmfPanel == null) {
      jmfPanel = new JMFPanel();
    }
    return jmfPanel;
  }
//-------------------------------------------------------------------------------------------------
  /**
   * This method initializes toolPanel	
   * 	
   * @return javax.swing.JToolPanel	
   */
  private JPanel getToolPanel() {
    if (toolPanel == null) {
      toolPanel = new JPanel();
      toolPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), " Tools "));
      toolPanel.add(getAddTOCButton());
      toolPanel.add(getAddChapButton());
      toolPanel.add(getAddTOCEntryButton());
      toolPanel.add(getRemoveEntryButton());
      toolPanel.add(getStartButton());
      toolPanel.add(getNudgeLeftButton());
      toolPanel.add(getSetStartButton());
      toolPanel.add(getSetEndButton());
      toolPanel.add(getNudgeRightButton());
      toolPanel.add(getEndButton());
    }
    return toolPanel;
  }
//-------------------------------------------------------------------------------------------------
  /**
   * This method initializes the "Add Chapter" button.	
   * 	
   * @return javax.swing.JButton	
   */
  private JButton getAddChapButton() {
    if (addChapButton == null) {
      addChapButton = new JButton();
      addChapButton.setText("Add Chapter");
      addChapButton.setIcon(new ImageIcon(ID3v2ChapterTool.class.getResource("/toolbarButtonGraphics/general/New24.gif")));
      addChapButton.setActionCommand("addChapter");
      addChapButton.setToolTipText("Add a new chapter at the selected location in the table of contents tree.");
      addChapButton.setVerticalTextPosition(AbstractButton.BOTTOM);
      addChapButton.setHorizontalTextPosition(AbstractButton.CENTER);
      addChapButton.addActionListener(this);
      addChapButton.setMargin(new Insets(2,2,2,2));
      addChapButton.setEnabled(false);
    }
    return addChapButton;
  }
//-------------------------------------------------------------------------------------------------
  /**
   * This method initializes the "Add TOC" button.	
   * 	
   * @return javax.swing.JButton	
   */
  private JButton getAddTOCButton() {
    if (addTOCButton == null) {
      addTOCButton = new JButton();
      addTOCButton.setText("Add TOC");
      addTOCButton.setActionCommand("addTOC");
      addTOCButton.setToolTipText("Add a new level at the selected location in the table of contents tree.");
      addTOCButton.setIcon(new ImageIcon(ID3v2ChapterTool.class.getResource("/toolbarButtonGraphics/general/Add24.gif")));
      addTOCButton.setVerticalTextPosition(AbstractButton.BOTTOM);
      addTOCButton.setHorizontalTextPosition(AbstractButton.CENTER);
      addTOCButton.addActionListener(this);
      addTOCButton.setMargin(new Insets(2,2,2,2));
      addTOCButton.setEnabled(false);
    }
    return addTOCButton;
  }
//-------------------------------------------------------------------------------------------------
  /**
   * This method initializes the "Add Entry" button.   
   *    
   * @return javax.swing.JButton    
   */
  private JButton getAddTOCEntryButton() {
    if (addToTOCButton == null) {
      addToTOCButton = new JButton();
      addToTOCButton.setText("Add Entry");
      addToTOCButton.setIcon(new ImageIcon(ID3v2ChapterTool.class.getResource("/toolbarButtonGraphics/general/Bookmarks24.gif")));
      addToTOCButton.setActionCommand("addEntry");
      addToTOCButton.setToolTipText("Add an entry for the selected tab to the selected location in the table of contents tree.");
      addToTOCButton.setVerticalTextPosition(AbstractButton.BOTTOM);
      addToTOCButton.setHorizontalTextPosition(AbstractButton.CENTER);
      addToTOCButton.addActionListener(this);
      addToTOCButton.setMargin(new Insets(2,2,2,2));
      addToTOCButton.setEnabled(false);
    }
    return addToTOCButton;
  }
//-------------------------------------------------------------------------------------------------
  /**
   * This method initializes the "Remove" button.   
   *    
   * @return javax.swing.JButton    
   */
  private JButton getRemoveEntryButton() {
    if(removeEntryButton == null) {
      removeEntryButton = new JButton();
      removeEntryButton.setText("Remove");
      removeEntryButton.setIcon(new ImageIcon(ID3v2ChapterTool.class.getResource("/toolbarButtonGraphics/general/Delete24.gif")));
      removeEntryButton.setActionCommand("removeEntry");
      removeEntryButton.setToolTipText("Remove the selected element in the table of contents tree.");
      removeEntryButton.setVerticalTextPosition(AbstractButton.BOTTOM);
      removeEntryButton.setHorizontalTextPosition(AbstractButton.CENTER);
      removeEntryButton.addActionListener(this);
      removeEntryButton.setMargin(new Insets(2,2,2,2));
      removeEntryButton.setEnabled(false);
    }
    return removeEntryButton;
  }
//-------------------------------------------------------------------------------------------------
  /**
   * This method initializes the "Start" button.   
   *    
   * @return javax.swing.JButton    
   */
  private JButton getStartButton() {
    if(backButton == null) {
      backButton = new JButton();
      backButton.setText("Start");
      backButton.setIcon(new ImageIcon(ID3v2ChapterTool.class.getResource("/toolbarButtonGraphics/media/StepBack24.gif")));
      backButton.setActionCommand("start");
      backButton.setToolTipText("Go to the start time of the selected chapter tab.");
      backButton.setVerticalTextPosition(AbstractButton.BOTTOM);
      backButton.setHorizontalTextPosition(AbstractButton.CENTER);
      backButton.addActionListener(this);
      backButton.setMargin(new Insets(2,8,2,8));
      backButton.setEnabled(false);
    }
    return backButton;
  }
//-------------------------------------------------------------------------------------------------
  /**
   * This method initializes the "End" button.
   *    
   * @return javax.swing.JButton    
   */
  private JButton getEndButton() {
    if(nextButton == null) {
      nextButton = new JButton();
      nextButton.setText("End");
      nextButton.setIcon(new ImageIcon(ID3v2ChapterTool.class.getResource("/toolbarButtonGraphics/media/StepForward24.gif")));
      nextButton.setActionCommand("end");
      nextButton.setToolTipText("Go to the end time of the selected chapter tab.");
      nextButton.setVerticalTextPosition(AbstractButton.BOTTOM);
      nextButton.setHorizontalTextPosition(AbstractButton.CENTER);
      nextButton.addActionListener(this);
      nextButton.setMargin(new Insets(2,8,2,8));
      nextButton.setEnabled(false);
    }
    return nextButton;
  }
//-------------------------------------------------------------------------------------------------
  /**
   * This method initializes the "Set Start" button.   
   *    
   * @return javax.swing.JButton    
   */
  private JButton getSetStartButton() {
    if(startButton == null) {
      startButton = new JButton();
      startButton.setText("Set Start");
      startButton.setIcon(new ImageIcon(ID3v2ChapterTool.class.getResource("/toolbarButtonGraphics/table/ColumnInsertBefore24.gif")));
      startButton.setActionCommand("setStart");
      startButton.setToolTipText("Set the start time of the selected chapter tab.");
      startButton.setVerticalTextPosition(AbstractButton.BOTTOM);
      startButton.setHorizontalTextPosition(AbstractButton.CENTER);
      startButton.addActionListener(this);
      startButton.setMargin(new Insets(2,2,2,2));
      startButton.setEnabled(false);
    }
    return startButton;
  }  
//-------------------------------------------------------------------------------------------------
  /**
   * This method initializes the "Set End" button.
   *    
   * @return javax.swing.JButton    
   */
  private JButton getSetEndButton() {
    if(endButton == null) {
      endButton = new JButton();
      endButton.setText("Set End");
      endButton.setIcon(new ImageIcon(ID3v2ChapterTool.class.getResource("/toolbarButtonGraphics/table/ColumnInsertAfter24.gif")));
      endButton.setActionCommand("setEnd");
      endButton.setToolTipText("Set the end time of the selected chapter tab.");
      endButton.setVerticalTextPosition(AbstractButton.BOTTOM);
      endButton.setHorizontalTextPosition(AbstractButton.CENTER);
      endButton.addActionListener(this);
      endButton.setMargin(new Insets(2,4,2,4));
      endButton.setEnabled(false);
    }
    return endButton;
  }
//-------------------------------------------------------------------------------------------------
  /**
   * This method initializes the nudge left button.   
   *    
   * @return javax.swing.JButton    
   */
  private JButton getNudgeLeftButton() {
    if(nudgeLeftButton == null) {
      nudgeLeftButton = new JButton();
      nudgeLeftButton.setText("Nudge");
      nudgeLeftButton.setIcon(new ImageIcon(ID3v2ChapterTool.class.getResource("/toolbarButtonGraphics/navigation/Back24.gif")));
      nudgeLeftButton.setActionCommand("nudgeLeft");
      nudgeLeftButton.setToolTipText("Set the current media time slightly earlier.");
      nudgeLeftButton.setVerticalTextPosition(AbstractButton.BOTTOM);
      nudgeLeftButton.setHorizontalTextPosition(AbstractButton.CENTER);
      nudgeLeftButton.addActionListener(this);
      nudgeLeftButton.setMargin(new Insets(2,2,2,2));
      nudgeLeftButton.setEnabled(false);
    }
    return nudgeLeftButton;
  }  
  //-------------------------------------------------------------------------------------------------
  /**
   * This method initializes the nudge right button.   
   *    
   * @return javax.swing.JButton    
   */
  private JButton getNudgeRightButton() {
    if(nudgeRightButton == null) {
      nudgeRightButton = new JButton();
      nudgeRightButton.setText("Nudge");
      nudgeRightButton.setIcon(new ImageIcon(ID3v2ChapterTool.class.getResource("/toolbarButtonGraphics/navigation/Forward24.gif")));
      nudgeRightButton.setActionCommand("nudgeRight");
      nudgeRightButton.setToolTipText("Set the current media time slightly later.");
      nudgeRightButton.setVerticalTextPosition(AbstractButton.BOTTOM);
      nudgeRightButton.setHorizontalTextPosition(AbstractButton.CENTER);
      nudgeRightButton.addActionListener(this);
      nudgeRightButton.setMargin(new Insets(2,2,2,2));
      nudgeRightButton.setEnabled(false);
    }
    return nudgeRightButton;
  }    
//-------------------------------------------------------------------------------------------------
  private JMenuBar createMenuBar() {
    JMenuItem item;
    JMenuBar menuBar = new JMenuBar();
    // File menu.
    JMenu fileMenu = new JMenu("File");
    fileMenu.setMnemonic(KeyEvent.VK_F);

    openMenuItem = new JMenuItem("Open");
    fileMenu.add(openMenuItem);
    openMenuItem.setMnemonic(KeyEvent.VK_O);
    openMenuItem.setIcon(new ImageIcon(ID3v2ChapterTool.class.getResource("/toolbarButtonGraphics/general/Open16.gif")));
    openMenuItem.setToolTipText("Open an audio file.");
    openMenuItem.addActionListener(this);

//    closeMenuItem = new JMenuItem("Close")
//    fileMenu.add(closeMenuItem);
//    closeMenuItem.setIcon(new ImageIcon(ID3v2ChapterTool.class.getResource("/toolbarButtonGraphics/general/Stop16.gif")));
//    closeMenuItem.addActionListener(this);

    saveMenuItem = new JMenuItem("Save");
    fileMenu.add(saveMenuItem);
    saveMenuItem.setMnemonic(KeyEvent.VK_S);
    saveMenuItem.setIcon(new ImageIcon(ID3v2ChapterTool.class.getResource("/toolbarButtonGraphics/general/SaveAll16.gif")));
    saveMenuItem.setEnabled(false);
    saveMenuItem.setToolTipText("Save the current audio file.");
    saveMenuItem.addActionListener(this);
    
    saveAsMenuItem = new JMenuItem("Save As");
    fileMenu.add(saveAsMenuItem);
    saveAsMenuItem.setIcon(new ImageIcon(ID3v2ChapterTool.class.getResource("/toolbarButtonGraphics/general/SaveAs16.gif")));
    saveAsMenuItem.setEnabled(false);
    saveAsMenuItem.setToolTipText("Rename and save the current audio file.");
    saveAsMenuItem.addActionListener(this);
    
    importMenuItem = new JMenuItem("Import XML file");
    fileMenu.add(importMenuItem);
    importMenuItem.setIcon(new ImageIcon(ID3v2ChapterTool.class.getResource("/toolbarButtonGraphics/general/Import16.gif")));
    importMenuItem.setEnabled(false);
    importMenuItem.setToolTipText("Read chapter information from an XML file.");
    importMenuItem.addActionListener(this);

    exportMenuItem = new JMenuItem("Export XML file");
    fileMenu.add(exportMenuItem);
    exportMenuItem.setIcon(new ImageIcon(ID3v2ChapterTool.class.getResource("/toolbarButtonGraphics/general/Export16.gif")));
    exportMenuItem.setEnabled(false);
    exportMenuItem.setToolTipText("Save chapter information as an XML file.");
    exportMenuItem.addActionListener(this);

    exitMenuItem = new JMenuItem("Exit");
    fileMenu.add(exitMenuItem);
    exitMenuItem.setIcon(new ImageIcon(ID3v2ChapterTool.class.getResource("/toolbarButtonGraphics/general/Stop16.gif")));
    exitMenuItem.addActionListener(this);
    exitMenuItem.setToolTipText("Exit without saving.");

    menuBar.add(fileMenu);
    return menuBar;
}
//-------------------------------------------------------------------------------------------------
  /**
   * ActionListener interface.
   */
  public void actionPerformed(ActionEvent event) {
    // System.out.println("ActionCommand = " + e.getActionCommand());
    String ac = event.getActionCommand();
//-------------------------------------------------------------------------------------------------
// File menu events.
//-------------------------------------------------------------------------------------------------   
    if(ac == "Open") {
      JFileChooser fileChooser = new JFileChooser(audioFile);
      if(fileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
      // If a file is already open clear up the media player, tree and tabs.
      if(audioFile!= null) {
        jmfPanel.closePlayer();
        clear();
      }
      audioFile= fileChooser.getSelectedFile();
      String filename = audioFile.getName();
      this.setTitle("ID3v2ChapterTool - " + filename);
      
      // Set default directory for xml.
      if(xmlFile == null) {
        xmlFile = audioFile;
      }
      tree.setRootId(audioFile.getName());
      jmfPanel.makePlayer(audioFile.getPath());
      processTag(audioFile);
      enableButtons(true);
      return;
//-------------------------------------------------------------------------------------------------
    } else if(ac == "Save") {
      updateTag(audioFile);
      return;
//-------------------------------------------------------------------------------------------------
    } else if(ac == "Save As") {
      JFileChooser fileChooser = new JFileChooser(audioFile);
      if(fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
        return;
      }
      File newFile = fileChooser.getSelectedFile();
      audioFile = copyFile(audioFile, newFile.getPath());
      updateTag(audioFile);
      tree.setRootId(audioFile.getName());
      tree.repaint();
      return;
//-------------------------------------------------------------------------------------------------
    } else if(ac == "Import XML file") {
      JFileChooser fileChooser = new JFileChooser(xmlFile);
      if(fileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
      xmlFile = fileChooser.getSelectedFile();
      parseXMLFile(xmlFile);
      return;
//-------------------------------------------------------------------------------------------------
    } else if(ac == "Export XML file") {
      JFileChooser fileChooser = new JFileChooser(xmlFile);
      if(fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
        return;
      }
      xmlFile = fileChooser.getSelectedFile();
      exportXMLFile(xmlFile);
      return;
//-------------------------------------------------------------------------------------------------
    } else if(ac == "Exit") {
      jmfPanel.closePlayer();
      System.exit(0);
    }
//--------------------------------------------------------------------------------------------------
//  ToolPanel events.
// -------------------------------------------------------------------------------------------------       
    if(audioFile == null) {
      return;
    }
    String selectedId = tree.getSelectedElementId();
    Component selectedTab = tabbedPane.getSelectedComponent();
    if(selectedTab != null) { 
      selectedTab = ((JScrollPane)selectedTab).getViewport().getView();
    }
//  -------------------------------------------------------------------------------------------------
    if(ac == "addChapter") {
      // Check you're allowed to create and add chapters to the selected node. Your allowed to create but not add chapters if the root is selected.
      if(!tree.doesSelectedAcceptChildren()) {
        JOptionPane.showConfirmDialog(null, "You cannot add a Chapter to a Chapter element.", "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
        return;
      }
      
      // Alert user they are adding a chapter without a TOC entry.
      if(selectedId == "root") {
        if(showAddChapToRootDialog == true) {
          int response = JOptionPane.showConfirmDialog(null, "If you add a Chapter without selecting a TOC it will be included in the ID3v2 tag but will not be listed in the Table of Contents.", "Confirm", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
          showAddChapToRootDialog = false;
          if(response != JOptionPane.OK_OPTION) return; 
        }
      }
      
      String elementId = "ch" + (new Integer(chapterIndex++)).toString();
      ChapPanel chapPanel = getChapPanel(elementId);
      lastChapPanel = chapPanel;                                // Remember this so we can get the last end time easily.
      JScrollPane scrollPane = new JScrollPane(chapPanel);
      tabbedPane.addTab(chapPanel.getElementId(), null, scrollPane, null);
      tabbedPane.setSelectedIndex(tabbedPane.indexOfComponent(scrollPane));
      tree.addElementToSelected(elementId, false);
//-------------------------------------------------------------------------------------------------
    } else if(ac == "addTOC") {
      // Don't allow more than one root CTOC.
      if(selectedId == "root") {
        if(tree.doesSelectedHaveChildrenWhichAcceptChildren()) {
          JOptionPane.showConfirmDialog(null, "You cannot add more than one root TOC. Please select a lower level in the tree.", "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
          return;
        }  
      }
      
      // Check you're allowed to create and add CTOCs to the selected node.
      if(!tree.doesSelectedAcceptChildren()) {
        JOptionPane.showConfirmDialog(null, "You cannot add a TOC to a Chapter element.", "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
        return;
      }
      String elementId = "toc" + (new Integer(tocIndex++)).toString();
      CTOCPanel ctocPanel = getCTOCPanel(elementId);
      JScrollPane scrollPane = new JScrollPane(ctocPanel);
      tabbedPane.addTab(ctocPanel.getElementId(), null, scrollPane, null);
      tabbedPane.setSelectedIndex(tabbedPane.indexOfComponent(scrollPane));
      tree.addElementToSelected(elementId, true);
//-------------------------------------------------------------------------------------------------
    } else if(ac == "addEntry") {
      if(selectedTab == null) {
        return;
      }
      // If the selectedNode is a Chapter then you cannot add anything.
      if(!tree.doesSelectedAcceptChildren()) {
        JOptionPane.showConfirmDialog(null, "You cannot add an entry to a Chapter element.", "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
        return;
      }
      // If selectedTab is already a member of the node then ignore request.
      if(tree.doesSelectedContain(selectedTab.toString())) {
        String message = selectedId + " already has an entry for " + selectedTab.toString() + ".";
        JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
        return;
      }
      // Don't allow entries to be added to the root.
      if(selectedId == "root") {
        JOptionPane.showConfirmDialog(null, "You must select a TOC in order to add an entry.", "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
        return;
      }
 
      String message = "Add an entry for " + selectedTab.toString() + " to " + selectedId + "?";
      if(JOptionPane.showConfirmDialog(null, message, "Confirm entry", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE) != JOptionPane.YES_OPTION) {
        return;
      }
      // Set areChildrenSupported for the new node. This affects the icon used for it.
      if(selectedTab instanceof ChapPanel) {
        tree.addElementToSelected(selectedTab.toString(), false);
      } else {
        tree.addElementToSelected(selectedTab.toString(), true);
      }
//-------------------------------------------------------------------------------------------------
    } else if(ac == "removeEntry") {
      if(selectedId != "root") {
        String message;
        if(tree.doesSelectedHaveChildren()) {
          message = "Remove " + selectedId + " and its child elements?";
        } else { 
          message = "Remove " + selectedId + "?";   
        }
        if(JOptionPane.showConfirmDialog(null, message, "Confirm remove", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE) != JOptionPane.YES_OPTION) {
          return;
        }
        // Remove node.
        tree.removeSelectedElement();
        syncTabbedPane();
      }
//-------------------------------------------------------------------------------------------------   
    } else if(ac == "start") {
      double time = jmfPanel.getTime();
      if (time < 0 || selectedTab == null || !(selectedTab instanceof ChapPanel)) {
        return;
      }
      double startTime = ((ChapPanel)selectedTab).getStartTime();
      jmfPanel.setTime(startTime);
//-------------------------------------------------------------------------------------------------   
    } else if(ac == "end") {
      double time = jmfPanel.getTime();
      if (time < 0 || selectedTab == null || !(selectedTab instanceof ChapPanel)) {
        return;
      }
      double endTime = ((ChapPanel)selectedTab).getEndTime();
      jmfPanel.setTime(endTime);
//  -------------------------------------------------------------------------------------------------   
    } else if(ac == "setStart") {
      double time = jmfPanel.getTime();
      if (time < 0 || selectedTab == null || !(selectedTab instanceof ChapPanel)) {
        return;
      }
      ((ChapPanel)selectedTab).setStartTime(time);
//-------------------------------------------------------------------------------------------------   
    } else if(ac == "setEnd") {
      double time = jmfPanel.getTime();
      if (time < 0 || selectedTab == null || !(selectedTab instanceof ChapPanel)) {
        return;
      }
      ((ChapPanel)selectedTab).setEndTime(time);
   ///   lastEndSetting = time;
//  -------------------------------------------------------------------------------------------------   
    } else if(ac == "nudgeLeft") {
      double time = jmfPanel.getTime();
      time -= nudgeIncrement;
      if (time < 0) {
        return;
      }
      jmfPanel.setTime(time);
//  -------------------------------------------------------------------------------------------------   
    } else if(ac == "nudgeRight") {
      double time = jmfPanel.getTime();
      time += nudgeIncrement;
      if (time < 0) {
        return;
      }
      jmfPanel.setTime(time);
    }
  }
//-------------------------------------------------------------------------------------------------
  /**
   * MouseListener interface. Used to select a tab when tree entries are double-clicked.
   */
  public void  mouseClicked(MouseEvent e) {
    if(e.getSource() == tree) {
      if (e.getClickCount() == 2) {
        String selectedId = tree.getSelectedElementId();
        // System.out.println("selectedId = " + selectedId);
        if(selectedId != "root") {
          selectTab(selectedId);
        }
      }
    }      
  }
  public void    mouseEntered(MouseEvent e) { }
  public void    mouseExited(MouseEvent e) { }
  public void    mousePressed(MouseEvent e) { }
  public void    mouseReleased(MouseEvent e) { }
//-------------------------------------------------------------------------------------------------
  /**
   * Clear any frame in the tabbedPane that is not in the tree.
   */
  protected void syncTabbedPane() {
    for(int i = tabbedPane.getTabCount()-1; i>=0; i--) {
      // System.out.println();
      Object object = ((JScrollPane)tabbedPane.getComponentAt(i)).getViewport().getView();
      // System.out.println("i=" + i + " object = " + object);
      if(!tree.isElementPresent(object.toString())) {
        // System.out.println("Not found " + object);
        tabbedPane.removeTabAt(i);
      } else {
        // System.out.println("Found " + object);
      }
    }    
  }
  
//-------------------------------------------------------------------------------------------------
  /**
   * Clear tabbedPane and tree.
   */
  protected void clear() {
    jSplitPane.setLeftComponent(getTreeScrollPane());
    jSplitPane.setRightComponent(getRHSPanel());
    chapterIndex = 1;
    tocIndex = 1;
  }
//-------------------------------------------------------------------------------------------------
  /**
   * Get the ChapPanel or CTOCPanel with the specified elementId from the tabbed pane.
   */
  protected Object getFrame(String elementId) {
    for(int i = tabbedPane.getTabCount()-1; i>=0; i--) {
      Object object = ((JScrollPane)tabbedPane.getComponentAt(i)).getViewport().getView();
      String id = "";
      if(object instanceof ChapPanel) {
        ChapPanel chapPanel = (ChapPanel)object;
        id = chapPanel.getElementId();
      } else if(object instanceof CTOCPanel) {
        CTOCPanel ctocPanel = (CTOCPanel)object;
        id = ctocPanel.getElementId();
      }
      if(id.equals(elementId)) return object;
    }    
    return null;
  }
//-------------------------------------------------------------------------------------------------
  /**
   * Select the tab with the specified elementId in the tabbed pane.
   */
  protected void selectTab(String elementId) {
    int tabIndex = tabbedPane.indexOfTab(elementId);
    if(tabIndex >= 0) {
      tabbedPane.setSelectedIndex(tabIndex);
    }
  }
//-------------------------------------------------------------------------------------------------
  /**
   * Copy supplied file and return copy.
   */
  protected File copyFile(File inFile, String newFilePath) {
    int SIZE = 4096; 
    try {
      File newFile = new File(newFilePath);  
      BufferedInputStream bis = new BufferedInputStream(new FileInputStream(inFile));
      FileOutputStream os = new FileOutputStream(newFile);
      BufferedOutputStream bos = new BufferedOutputStream(os);
      byte[] buffer = new byte[SIZE];
      int bytesRead;
      while(true) {
        bytesRead = bis.read(buffer, 0, SIZE);
        if(bytesRead > 0) {
          bos.write(buffer, 0, bytesRead);
        } else { 
          break;
        }
      }
      bis.close();
      bos.flush();
      os.flush();
      bos.close();
      os.close();
      return newFile;
    } catch(IOException e) {
      e.printStackTrace();
      return null;
    }
  }
//-------------------------------------------------------------------------------------------------
  /**
   * Load the chapter frame information in the ID3 tag of the supplied file.
   */
  protected void processTag(File file) {
    ID3v2 tag = null;
    try {
      tag = new ID3v2(file);
      if(tag.hasTag()) {
        if(tag.getHeader().version >= ID3v2.VERSION_3) {
          Vector frames = tag.getFrames();
          Vector chapterFrames = new Vector();
          // Load data from any exiting CHAP & CTOC frames and build tabbed pane.
          for(int i=0; i<frames.size(); i++) {
            ID3v2Frame frame = (ID3v2Frame)(frames.elementAt(i));
            if(frame.getID().equals("CTOC")) {
              CTOCFrame ctocFrame = new CTOCFrame(frame);
              chapterFrames.addElement(ctocFrame);
              CTOCPanel ctocPanel = new CTOCPanel(ctocFrame);
              JScrollPane scrollPane = new JScrollPane(ctocPanel);
              tabbedPane.addTab(ctocFrame.getElementId(), null, scrollPane, null);
              try {
                int index = Integer.parseInt(ctocFrame.getElementId().substring(3));
                if(index >= tocIndex) {
                  tocIndex = index + 1;
                }
              } catch(NumberFormatException e) { 
              }
            } else if(frame.getID().equals("CHAP")) {
              CHAPFrame chapFrame = new CHAPFrame(frame);
              chapterFrames.addElement(chapFrame);
              ChapPanel chapPanel = new ChapPanel(chapFrame.getElementId(), chapFrame.getStartTime(), chapFrame.getEndTime(), chapFrame.getSubFrames());
              JScrollPane scrollPane = new JScrollPane(chapPanel);
              tabbedPane.addTab(chapPanel.getElementId(), null, scrollPane, null);
              try {
                int index = Integer.parseInt(chapFrame.getElementId().substring(2));
                if(index >= chapterIndex) {
                  chapterIndex = index + 1;
                }
              } catch(NumberFormatException e) { 
              }
            }
          }
          // Now build tree.
          if(chapterFrames.size() != 0) {
            // Build tree.
            ChapterFrame[] chapterFramesArray = (ChapterFrame[])(chapterFrames.toArray(new ChapterFrame[chapterFrames.size()]));
            tree.syncTree(chapterFramesArray);
          } else {
            // Add the root TOC frame.
            String elementId = "toc" + (new Integer(tocIndex++)).toString();
            CTOCPanel ctocPanel = getCTOCPanel(elementId);
            JScrollPane scrollPane = new JScrollPane(ctocPanel);
            tabbedPane.addTab(ctocPanel.getElementId(), null, scrollPane, null);
            tabbedPane.setSelectedIndex(tabbedPane.indexOfComponent(scrollPane));
            tree.addElementToSelected(elementId, true);
          }
        }
      }
      jSplitPane.setDividerLocation(160);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
//-------------------------------------------------------------------------------------------------
  /**
   * Update the chapter frame information in the ID3 tag of the supplied file.
   * If the file does not have an existing ID3 tag a new one is created.
   */
  protected void updateTag(File file) {
    double time = jmfPanel.getTime();
    jmfPanel.closePlayer();
    Vector frames = new Vector();
    ID3v2 tag = null;
    try {
      tag = new ID3v2(file);
      if(tag.hasTag()) {
        // Convert v2.2 tags to v2.3 if required.
        if(tag.getHeader().version == ID3v2.VERSION_2) {
          tag.convertV220Frames(false);                   // i.e. don't use compression.
          tag.getHeader().version = ID3v2.VERSION_3;
          tag.getHeader().revision = ID3v2.REVISION;
        } else {
          // Strip out any existing CHAP & CTOC frames.
          frames = tag.getFrames();
          for(int i=frames.size()-1; i>=0; i--) {
            ID3v2Frame frame = (ID3v2Frame)(frames.elementAt(i));
            if(frame.getID().equals("CTOC") || frame.getID().equals("CHAP")) {
              tag.removeFrame(frame);
            }
          }
        }
      }
    } catch(Exception e) {
      e.printStackTrace();
    }
   
    // Many media players create errors if used with other settings.
    tag.setUseExtendedHeader(false);
    tag.setUsePadding(false);                    // Ignored unless useExtendedHeader is true.
    tag.setUseCRC(false);                        // Ignored unless useExtendedHeader is true.
    tag.setUseUnsynchronization(false);
      
    // Iterate thru tabs twice - first to process CTOC frames and second to process CHAP frames.
    for(int i=0; i<tabbedPane.getTabCount(); i++) {
      Object tab = ((JScrollPane)tabbedPane.getComponentAt(i)).getViewport().getView();   
      if(tab instanceof CTOCPanel) {
        CTOCPanel ctocPanel = (CTOCPanel)tab;
        CTOCFrame ctocFrame = ctocPanel.getFrame();
        if(tree.isElementChildOfRoot(ctocFrame.getElementId())) {
          ctocFrame.setTopLevel(true);
        }
        String[] entries = tree.getEntries(ctocFrame.getElementId());
        for(int j=0; j<entries.length; j++) {
          ctocFrame.addEntry(entries[j]);
        }
        tag.addFrame(ID3Util.getID3v230Frame(ctocFrame.getID(), ctocFrame.getContent()));
      }
    }
    for(int i=0; i<tabbedPane.getTabCount(); i++) {
      Object tab = ((JScrollPane)tabbedPane.getComponentAt(i)).getViewport().getView();   
      if(tab instanceof ChapPanel) {
        ChapPanel chapPanel = (ChapPanel)tab;
        try {
          CHAPFrame chapFrame = chapPanel.getFrame();
          tag.addFrame(ID3Util.getID3v230Frame(chapFrame.getID(), chapFrame.getContent())); 
        } catch (NumberFormatException e) {
          JOptionPane.showConfirmDialog(null, "Illegal time format - please correct entry for " + chapPanel.getElementId() + " and retry.", "Failed", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
          return;
        }
      }
    }
    // Update file.
    try {
      tag.update();
    } catch(Exception e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(null, "Unable to update tag in output file.", "Error", JOptionPane.ERROR_MESSAGE);
    }
    // Now re-open player.
    jmfPanel.makePlayer(file.getPath(), time);
  }
//-------------------------------------------------------------------------------------------------
  /**
   * Update the chapter frame information in the ID3 tag of the supplied file.
   * If the file does not have an existing ID3 tag a new one is created.
   */
  protected void close() {
    jmfPanel.closePlayer();
    jSplitPane.setLeftComponent(getTreeScrollPane());
    jSplitPane.setRightComponent(getRHSPanel());
    jSplitPane.setDividerLocation(160);
    enableButtons(false);
    chapterIndex = 1;
    tocIndex = 1;
  }
//------------------------------------------------------------------------------------------------- 
  protected void enableButtons(boolean isEnabled) {
    addChapButton.setEnabled(isEnabled);
    addTOCButton.setEnabled(isEnabled);
    addToTOCButton.setEnabled(isEnabled);
    removeEntryButton.setEnabled(isEnabled);
    backButton.setEnabled(isEnabled);
    startButton.setEnabled(isEnabled);
    nudgeLeftButton.setEnabled(isEnabled);
    nudgeRightButton.setEnabled(isEnabled);
    endButton.setEnabled(isEnabled);
    nextButton.setEnabled(isEnabled);
    saveMenuItem.setEnabled(isEnabled);
    saveAsMenuItem.setEnabled(isEnabled);
    importMenuItem.setEnabled(isEnabled);
    exportMenuItem.setEnabled(isEnabled);
  }
//-------------------------------------------------------------------------------------------------
  protected void listFrames(ID3v2 tag) {
    try {
      Vector frames = tag.getFrames();
      for(int i=0; i<frames.size(); i++) {
        ID3v2Frame frame = (ID3v2Frame)(frames.elementAt(i));
        if(frame.getID().equals("CTOC")) {
          CTOCFrame ctocFrame = new CTOCFrame(frame);
          System.out.println("CTOC: " + ctocFrame.getElementId());
        }
        if(frame.getID().equals("CHAP")) {
          CHAPFrame chapFrame = new CHAPFrame(frame);
          System.out.println("CHAP: " + chapFrame.getElementId());
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
//-------------------------------------------------------------------------------------------------
  protected void parseXMLFile(File file) {
    ChapterXMLHandler handler = null;
    try {
      handler = new ChapterXMLHandler(file, true, true);
    } catch(IOException e) {
      System.err.println("XMLTool: Unable to open XML file: " + file.getPath());
      return;
    } catch(SAXException e) {
      System.err.println("XMLTool: Unable to parse XML file: " + file.getPath());
      return;
    }
    
    Vector chapterFrames = handler.getChapters();
    // Clear existing frames.
    clear();
    // Set end time of last frame to file duration.
    CHAPFrame lastFrame = (CHAPFrame)chapterFrames.lastElement();
    long endTime = (long)(jmfPanel.getMediaEndTime() * 1000);
    lastFrame.setEndTime(endTime);
    
    // Create & add table of contents.
    CTOCFrame ctocFrame = null;
    ctocFrame = new CTOCFrame("toc1", true, true);
    for(int i=0; i < chapterFrames.size(); i++) {
      CHAPFrame chapFrame = (CHAPFrame)(chapterFrames.elementAt(i));
      ctocFrame.addEntry(chapFrame.getElementId());
    }
    CTOCPanel ctocPanel = new CTOCPanel(ctocFrame);
    JScrollPane scrollPane = new JScrollPane(ctocPanel);
    tabbedPane.addTab(ctocFrame.getElementId(), null, scrollPane, null);
    
    // Add chapters.
    for(int i=0; i<chapterFrames.size(); i++) {
      CHAPFrame chapFrame = (CHAPFrame)(chapterFrames.elementAt(i));
      ChapPanel chapPanel = new ChapPanel(chapFrame.getElementId(), chapFrame.getStartTime(), chapFrame.getEndTime(), chapFrame.getSubFrames());
      JScrollPane cscrollPane = new JScrollPane(chapPanel);
      tabbedPane.addTab(chapPanel.getElementId(), null, cscrollPane, null);
      try {
        int index = Integer.parseInt(chapFrame.getElementId().substring(2));
        if(index >= chapterIndex) {
          chapterIndex = index + 1;
        }
      } catch(NumberFormatException e) { 
      }
    }
    if(ctocFrame != null) {
      chapterFrames.insertElementAt(ctocFrame, 0);
    }
    
    // Now build tree.
    ChapterFrame[] chapterFramesArray = (ChapterFrame[])(chapterFrames.toArray(new ChapterFrame[chapterFrames.size()]));
    tree.syncTree(chapterFramesArray);  
  }
//-------------------------------------------------------------------------------------------------
  protected void exportXMLFile(File file) {
    String cr = System.getProperty("line.separator");
    if(cr == null) {
      cr = "";
    }  
    try {
      FileWriter writer = new FileWriter(file);
      BufferedWriter bw = new BufferedWriter(writer);
      String header = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + cr + "<chapters version=\"1\">" + cr + cr;
      bw.write(header, 0, header.length()); 
    
      // Iterate thru tabs. 
      for(int i=0; i<tabbedPane.getTabCount(); i++) {
        Object tab = ((JScrollPane)tabbedPane.getComponentAt(i)).getViewport().getView();   
        if(tab instanceof ChapPanel) {
          ChapPanel chapPanel = (ChapPanel)tab;
          try {
            CHAPFrame chapFrame = chapPanel.getFrame();                    // Included only to trap illegal number formats.
            String startTime = chapPanel.getStartTextField().getText();
            String chapter = "\t<chapter starttime=\"" + startTime + "\">" + cr; 
            bw.write(chapter, 0, chapter.length()); 
            String titleField = chapPanel.getTitleField().getText();
            if(titleField != null) {
              String title = "\t\t<title>" + ChapterXMLHandler.escape(titleField) + "</title>" + cr;
              bw.write(title, 0, title.length()); 
            }
            for(int j=0; j<chapFrame.getNumSubFrames(); j++) {
              ID3v2Frame subFrame = chapFrame.getSubFrame(j);
              if(subFrame instanceof ImageFrame) {
                ImageFrame imageFrame = (ImageFrame)subFrame;
                String path = imageFrame.getPath();
                if(path != null) {
                  String picture = "\t\t<picture>" + ChapterXMLHandler.escape(path) + "</picture>" + cr;
                  bw.write(picture, 0, picture.length()); 
                }
              } else if(subFrame instanceof URLFrame) {
                URLFrame urlFrame = (URLFrame)subFrame;
                String url = urlFrame.getURL();
                if(url != null) {
                  String link = "\t\t<link href=\"" +  ChapterXMLHandler.escape(url) + "\"></link>" + cr;
                  bw.write(link, 0, link.length()); 
                }
              }
            }
          } catch (NumberFormatException e) {
            JOptionPane.showConfirmDialog(null, "Illegal time format - please correct entry for " + chapPanel.getElementId() + " and retry.", "Failed", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
            return;
          }
          String endChapter = "\t</chapter>" + cr + cr;
          bw.write(endChapter, 0, endChapter.length());
        }
      }
      String footer = "</chapters>" + cr;
      bw.write(footer, 0, footer.length());
      bw.flush();
      writer.flush();
      bw.close();
      writer.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
//-------------------------------------------------------------------------------------------------
} 
//=================================================================================================