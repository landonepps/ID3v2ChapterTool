/**
 * Copyright 2006 British Broadcasting Corporation
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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.media.Clock;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.MediaTimeSetEvent;
import javax.media.Player;
import javax.media.PrefetchCompleteEvent;
import javax.media.RealizeCompleteEvent;
import javax.media.StopAtTimeEvent;
import javax.media.Time;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.JButton;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.xml.sax.SAXException;

import bbc.rd.id3.CHAPFrame;
import bbc.rd.id3.CTOCFrame;
import bbc.rd.id3.ChapterFrame;
import bbc.rd.id3.ImageFrame;
import bbc.rd.id3.TextFrame;
import bbc.rd.id3.URLFrame;
import bbc.rd.id3.ID3Util;
import de.vdheide.mp3.ID3v2;
import de.vdheide.mp3.ID3v2Frame;

//=================================================================================================
public class ChapterPlayer extends JFrame implements ActionListener, ListSelectionListener, MouseListener, KeyListener, Runnable {

  private static final long serialVersionUID = 1L;
  private File audioFile = null;
  private Hashtable frameTable = null;
  private CHAPFrame[] chapters;
  private ChapterTree tree = null;
  private String topLevelId = null;             // The ElementId of the top-level CTOC frame. Null if not set.
  private String[] childIds = null;             // An ordered list of the child elementIds of the selected frame. Null if not set.
  private String fileTitle = "";                // Carried in the TIT2 frame of the file.
  private String fileDescription = "";          // Carried in the TDES frame of the file. 
  private BufferedImage fileImage = null;       // Image associated with the entire file. Null if not set.
  private JPanel contentPane = null;
  private JSplitPane horizontalSplitPane = null;
  private JSplitPane verticalSplitPane = null;
  private JPanel rhsPanel = null;
  private ImagePanel imagePanel = null;
  private JTextField titlePanel = null;
  private JTextField chapterTitlePanel = null;
  private JScrollPane descriptionScrollPane = null;
  private JTextArea descriptionTextArea = null;
  private JPanel timeBarPanel = null;
  private JMFPanel jmfPanel = null;
  private JScrollPane listScrollPane = null;
  private DefaultListModel listModel = null;
  private JList list = null;
  private JMenuItem openMenuItem;
  private JMenuItem exitMenuItem;
  private char[] arrowChar = { 0x21b5 };
  private String backArrow = new String(arrowChar);
  private Thread thread;

//-------------------------------------------------------------------------------------------------
  /**
   * @param args
   */
  public static void main(String[] args) {
    if(args.length == 0) {
      new ChapterPlayer();
    } else if(args.length == 1) {
      new ChapterPlayer(new File(args[0]));
    } else {
      System.out.println("Usage: java ChapterPlayer [audio file path]");
      System.exit(0);
    }
  }
//-------------------------------------------------------------------------------------------------
  /**
   * This is the default constructor
   */
  public ChapterPlayer() {
    super();
    initialize();
    this.setVisible(true);
  }
//-------------------------------------------------------------------------------------------------
  /**
   * 
   */
  public ChapterPlayer(File file) {
    this();
    audioFile = file;
    jmfPanel.makePlayer(file.getPath());
    // jmfPanel.addControllerListener(this);
    processTag(file);
  }
//-------------------------------------------------------------------------------------------------
  /**
   * This method initializes this Frame.
   * 
   * @return void
   */
  private void initialize() {
    this.setSize(new Dimension(500, 500));
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.setJMenuBar(createMenuBar());
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {
      e.printStackTrace();
    }  
    this.setTitle("ChapterPlayer");
    contentPane = new JPanel();
    contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
    contentPane.add(getTitlePanel());
    contentPane.add(getVerticalSplitPane());
    contentPane.add(getJMFPanel());
    this.setContentPane(contentPane);
  }
//-------------------------------------------------------------------------------------------------
  /**
   * This method initializes jSplitPane	
   * 	
   * @return javax.swing.JSplitPane	
   */
  private JSplitPane getHorizontalSplitPane() {
    if(horizontalSplitPane == null) {
      horizontalSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, getImagePanel(), getRHSPanel());
      horizontalSplitPane.setDividerLocation(200);
      horizontalSplitPane.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
      horizontalSplitPane.setPreferredSize(new Dimension(Short.MAX_VALUE, 200));
    }
    return horizontalSplitPane;
  }
//-------------------------------------------------------------------------------------------------
  /**
   * This method initializes jSplitPane 
   *    
   * @return javax.swing.JSplitPane 
   */
  private JSplitPane getVerticalSplitPane() {
    if(verticalSplitPane == null) {
      verticalSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, getHorizontalSplitPane(), getListScrollPane());
      verticalSplitPane.setDividerLocation(202);
      verticalSplitPane.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
      verticalSplitPane.setMinimumSize(new Dimension(Short.MAX_VALUE, 40));
      verticalSplitPane.setDividerSize(3);
    }
    return verticalSplitPane;
  }
//-------------------------------------------------------------------------------------------------
  /**
   * This method initializes the RHS Panel 
   *    
   * @return javax.swing.JPanel 
   */
  private JPanel getRHSPanel() {
    rhsPanel = new JPanel();
    rhsPanel.setLayout(new BoxLayout(rhsPanel, BoxLayout.Y_AXIS));
    rhsPanel.add(getChapterTitlePanel());
    rhsPanel.add(getDescriptionScrollPane());
    return rhsPanel;
  }
//-------------------------------------------------------------------------------------------------
  /**
   * This method initializes jmfPanel	
   * 	
   * @return javax.swing.JPanel	
   */
  private JPanel getJMFPanel() {
    if(jmfPanel == null) {
      jmfPanel = new JMFPanel();
    }
    return jmfPanel;
  }
//-------------------------------------------------------------------------------------------------
  private ImagePanel getImagePanel() {
    if(imagePanel == null) {
      imagePanel = new ImagePanel();
      imagePanel.setPreferredSize(new Dimension(200, 200));
    }
    return imagePanel;
  }
//-------------------------------------------------------------------------------------------------
  private JTextField getTitlePanel() {
    if(titlePanel == null) {
      titlePanel = new JTextField();
      titlePanel.setEditable(false);
      titlePanel.setBorder(new EmptyBorder(5,5,5,5));
      titlePanel.setHorizontalAlignment(JTextField.CENTER);
      titlePanel.setFont(new Font("Arial", Font.BOLD, 14));
      titlePanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 50));
    }
    return titlePanel;
  }
//-------------------------------------------------------------------------------------------------
  private JTextField getChapterTitlePanel() {
    if(chapterTitlePanel == null) {
      chapterTitlePanel = new JTextField();
      chapterTitlePanel.setEditable(false);
      chapterTitlePanel.setBorder(new EmptyBorder(5,5,5,5));
      chapterTitlePanel.setFont(new Font("Arial", Font.BOLD, 14));
      chapterTitlePanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 50));
    }
    return chapterTitlePanel;
  }
//------------------------------------------------------------------------------------------------
  private JScrollPane getDescriptionScrollPane() {
    if(descriptionScrollPane == null) {
      descriptionScrollPane = new JScrollPane(getDescriptionTextArea());
      descriptionScrollPane.setBorder(new EmptyBorder(0,0,0,0));
      descriptionScrollPane.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER); 
    }
    return descriptionScrollPane;
  }
//------------------------------------------------------------------------------------------------
  private JTextArea getDescriptionTextArea() {
    if(descriptionTextArea == null) {
      descriptionTextArea = new JTextArea();
      descriptionTextArea.setEditable(false);
      descriptionTextArea.setBorder(new EmptyBorder(5,5,5,5));
      descriptionTextArea.setFont(new Font("Arial", Font.PLAIN, 12));
      descriptionTextArea.setLineWrap(true);
      descriptionTextArea.setFont((new JLabel()).getFont());
    }
    return descriptionTextArea;
  }
//-------------------------------------------------------------------------------------------------
//  private JPanel getTimeBarPanel() {
//    if(timeBarPanel == null) {
//      timeBarPanel = new JPanel();    
//      timeBarPanel.setPreferredSize(new Dimension(Short.MAX_VALUE, 30));
//      timeBarPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 30));
//      timeBarPanel.setMinimumSize(new Dimension(0, 30));
//    }
//    return timeBarPanel;
//  }
//-------------------------------------------------------------------------------------------------
  private JScrollPane getListScrollPane() {
    if(listScrollPane == null) {
      listScrollPane = new JScrollPane(getList());
      listScrollPane.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
      listScrollPane.setBorder(new EmptyBorder(0,0,0,0));
    }
    return listScrollPane;
  }
//-------------------------------------------------------------------------------------------------
  private JList getList() {
    if(list == null) {
      listModel = new DefaultListModel();
      list = new JList(listModel);
      list.addListSelectionListener(this);
      list.addMouseListener(this);
      list.addKeyListener(this);
      list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
      list.setLayoutOrientation(JList.VERTICAL);
      list.setBorder(new EmptyBorder(5,5,5,5));
    }
    return list;
  }
//-------------------------------------------------------------------------------------------------
  private JMenuBar createMenuBar() {

    JMenuBar menuBar = new JMenuBar();
    // File menu.
    JMenu fileMenu = new JMenu("File");
    fileMenu.setMnemonic(KeyEvent.VK_F);

    openMenuItem = new JMenuItem("Open");
    fileMenu.add(openMenuItem);
    openMenuItem.setMnemonic(KeyEvent.VK_O);
    openMenuItem.setIcon(new ImageIcon(ChapterPlayer.class.getResource("/toolbarButtonGraphics/general/Open16.gif")));
    openMenuItem.setToolTipText("Open an audio file.");
    openMenuItem.addActionListener(this);

//    closeMenuItem = new JMenuItem("Close")
//    fileMenu.add(closeMenuItem);
//    closeMenuItem.setIcon(new ImageIcon(ChapterPlayer.class.getResource("/toolbarButtonGraphics/general/Stop16.gif")));
//    closeMenuItem.addActionListener(this);

    exitMenuItem = new JMenuItem("Exit");
    fileMenu.add(exitMenuItem);
    exitMenuItem.setIcon(new ImageIcon(ChapterPlayer.class.getResource("/toolbarButtonGraphics/general/Stop16.gif")));
    exitMenuItem.addActionListener(this);
    exitMenuItem.setToolTipText("Exit without saving.");

    menuBar.add(fileMenu);
    return menuBar;
}
//-------------------------------------------------------------------------------------------------
  /**
   * Navigate up the tree.
   */
  protected void navigateUp() {
    tree.setSelectedToParent();
    jmfPanel.stop();
    String elementId = tree.getSelectedElementId();
    doHorizontalSplitPane(elementId);
    doList(elementId, true);
  }
//-------------------------------------------------------------------------------------------------
  /**
   * Navigate down the tree.
   */
  protected void navigateDown(String elementId) {
    ChapterFrame frame = (ChapterFrame)(frameTable.get(elementId));
    if(frame instanceof CTOCFrame) {
      tree.setSelectedToChildElement(elementId);
      doList(elementId, true);
    }
    int selectedIndex = list.getSelectedIndex();
    if(selectedIndex >=0 && selectedIndex < childIds.length) {
      doHorizontalSplitPane(childIds[selectedIndex]);
    }
    selectMediaTime(elementId);
  }
//-------------------------------------------------------------------------------------------------
  /**
   * Load the chapter frame information in the ID3 tag of the supplied file.
   */
  protected void processTag(File file) {
    frameTable = new Hashtable();
    Vector chapterVector = new Vector();
    tree = new ChapterTree();
    topLevelId = null;
    ID3v2 tag = null;
    try {
      tag = new ID3v2(file);
      if(tag.hasTag()) {
        if(tag.getHeader().version >= ID3v2.VERSION_3) {
          Vector frames = tag.getFrames();
          // Load data from CHAP & CTOC frames and build hash table.
          for(int i=0; i<frames.size(); i++) {
            ID3v2Frame frame = (ID3v2Frame)(frames.elementAt(i));
            if(frame.getID().equals("CTOC")) {
              CTOCFrame ctocFrame = new CTOCFrame(frame);
              frameTable.put(ctocFrame.getElementId(), ctocFrame);
              if(ctocFrame.isTopLevel) {
                topLevelId = ctocFrame.getElementId();
              }
            } else if(frame.getID().equals("CHAP")) {
              CHAPFrame chapFrame = new CHAPFrame(frame);
              frameTable.put(chapFrame.getElementId(), chapFrame);
              chapterVector.add(chapFrame);
            } else if(frame.getID().equals("TALB")) {
              TextFrame tit2 = new TextFrame(frame);
              titlePanel.setText(tit2.getText());
            } else if(frame.getID().equals("TIT3")) {
              TextFrame tit3 = new TextFrame(frame);
              fileTitle = tit3.getText();
            } else if(frame.getID().equals("TDES")) {
              TextFrame tdes = new TextFrame(frame);
              fileDescription = tdes.getText();
              descriptionTextArea.setText(fileDescription);
            } else if(frame.getID().equals("APIC")) {
              ImageFrame fileImageFrame = new ImageFrame(frame);
              fileImage = fileImageFrame.getImage();
            }
          }
          chapters = (CHAPFrame[])chapterVector.toArray(new CHAPFrame[chapterVector.size()]);

          if(topLevelId == null) {
            // Invent a top-level CTOC frame.            
            topLevelId = "toc1";
            CTOCFrame ctocFrame = new CTOCFrame(topLevelId, true, true);
            for(int i=0; i<chapters.length; i++) {
              ctocFrame.addEntry(chapters[i].getElementId());
            }
            frameTable.put(ctocFrame.getElementId(), ctocFrame);
          }
          
          // Now build tree.
          if(frameTable.size() != 0) {
            // Build tree.
            ChapterFrame[] chapterFramesArray = (ChapterFrame[])(frameTable.values().toArray(new ChapterFrame[frameTable.size()]));
            tree.syncTree(chapterFramesArray);
          }

          // Now refresh display.
//          doList("root", true);
//          doHorizontalSplitPane(topLevelId);
          doList(topLevelId, false);
          doHorizontalSplitPane(topLevelId);
        }
      }
      // Set focus to chapter list.
      list.requestFocusInWindow();
      thread = new Thread(this);
      thread.start();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
//-------------------------------------------------------------------------------------------------
  protected void doList(String elementId, boolean select) {
    // System.out.println("doList: elementId " + elementId);
    listModel.clear(); 
    if(elementId == "root") {
      childIds = new String[1];
      childIds[0] = topLevelId;
      ChapterFrame childFrame = (ChapterFrame)(frameTable.get(topLevelId));
      String title = childFrame.getTitle();
      if(title != null) {
        listModel.addElement(title);
      } else {
        // If no title is found in the top-level CTOC frame use the program title.
        listModel.addElement(fileTitle);
      }
    } else {
      ChapterFrame frame = (ChapterFrame)(frameTable.get(elementId));
      if(frame instanceof CTOCFrame) {
        CTOCFrame ctocFrame = (CTOCFrame)frame;
        childIds = new String[ctocFrame.getNumEntries()];
        for(int i=0; i<ctocFrame.getNumEntries(); i++) {
          String childElementId = ctocFrame.getEntry(i);
          childIds[i] = childElementId;
          ChapterFrame childFrame = (ChapterFrame)(frameTable.get(childElementId));
          String title = childFrame.getTitle();
          if(title != null) {
            listModel.addElement(title);
          } else {
            // If no title is found for this frame use the elementId.
            listModel.addElement(childElementId);
          }
        }
        listModel.addElement(backArrow);
      }
    }
    if(select) {
      list.setSelectedIndex(0);
    }
  }
//------------------------------------------------------------------------------------------------- 
  /**
   * // Update chapter title and description for the speficied elementId (usually what is highlighted in the list panel).
   */
  protected void doHorizontalSplitPane(String elementId) {
    // System.out.println("doHorizontalSplitPane: elementId " + elementId);
    chapterTitlePanel.setText("");
    descriptionTextArea.setText("");
    
    if(elementId != "root") {    
      ChapterFrame frame = (ChapterFrame)(frameTable.get(elementId));
   
      // Update chapter title and description. 
      if(elementId == topLevelId) {
        descriptionTextArea.setText(fileDescription);
        chapterTitlePanel.setText(fileTitle);
      } else {
        String title = frame.getTitle();
        if(title != null) {
          chapterTitlePanel.setText(title);
        }
        String description = frame.getDescription();
        if(description != null) {
          descriptionTextArea.setText(description);
        }
      }

      // Set image if present.
      BufferedImage image = frame.getImage();
      if(image != null) {
        imagePanel.setImage(image);
      } else if(fileImage != null) {
        imagePanel.setImage(fileImage);
      }
    } else {
      descriptionTextArea.setText(fileDescription);
      chapterTitlePanel.setText(fileTitle);
      if(fileImage != null) {
        imagePanel.setImage(fileImage);
      }
    }
  }
//-------------------------------------------------------------------------------------------------
  /**
   * Select the media time based on the specified frame elementId.
   * Currently does nothing for CTOC frames.
   */
  protected void selectMediaTime(String elementId) {
    // System.out.println("selectMediaTime with elementId = " + elementId);
    Object frame = frameTable.get(elementId);
    if(frame instanceof CHAPFrame) {
      CHAPFrame chapFrame = (CHAPFrame)frame;
      double startTime = ((double)(chapFrame.getStartTime())) / 1000D;
      jmfPanel.stop();
      jmfPanel.setTime(startTime);
      jmfPanel.start();
      jmfPanel.repaint();
      String ctocId = tree.getSelectedElementId();
      Object object = frameTable.get(ctocId);
      if(object instanceof CTOCFrame) {
        CTOCFrame ctoc = (CTOCFrame)object;
        if(!ctoc.isOrdered) {
          double endTime = ((double)(chapFrame.getEndTime())) / 1000D;
          // System.out.println("End time = " + endTime);
          jmfPanel.setStopTime(endTime);
        }
      }
    }
  }
//-------------------------------------------------------------------------------------------------
  /**
   *
   */
  protected void close() {
    jmfPanel.closePlayer();

  }
//-------------------------------------------------------------------------------------------------
  
  public void run() {
    int lastIndex = -1;  // Used to avoid thread beating valueChanged to respond to list value changes.
    while(true) {
      try {
        Thread.sleep(500L);
      } catch (InterruptedException e) {
        System.out.println("Timer interrrupted.");
      }

      if(jmfPanel.playerRunning == false) {
        continue;
      }
      
      int selectedIndex = list.getSelectedIndex();
      if(lastIndex == -1) {
        lastIndex = selectedIndex;
      }
      
      // Put a one cycle delay where the list index has been changed by external factors rather than by this thread.
      if(selectedIndex < 0 || selectedIndex > chapters.length || selectedIndex != lastIndex) {
        lastIndex = selectedIndex;
        continue;
      }
      
      long time = (long)(jmfPanel.getTime() * 1000);
      // System.out.println("SelectedIndex = " + selectedIndex);

      for(int i=0; i<childIds.length; i++) {
        int n = (selectedIndex + i) % childIds.length;
        Object object = frameTable.get(childIds[n]);
        if(object instanceof CHAPFrame) {
          CHAPFrame chapFrame = (CHAPFrame)object;
          if(time > chapFrame.getStartTime() && time < chapFrame.getEndTime()) {
            if(i > 0) {
              // Found chapter with boundaries that match current media time. However this isn't the chapter currently selected in the chapter list.
              // System.out.println("Timer thread updating info to match media player: new selectedIndex = " + n);
              doHorizontalSplitPane(chapters[n].getElementId());
              list.removeListSelectionListener(this);
              list.setSelectedIndex(n);
              list.addListSelectionListener(this);
            } else {
              //  Selected chapter in the chapter list matches the current media time - do nothing.
            }
            break;
          }
        }
      }
    }
  }
//-------------------------------------------------------------------------------------------------
  /**
   * ActionListener interface for file menu events.
   */
  public void actionPerformed(ActionEvent event) {
    // System.out.println("ActionCommand = " + e.getActionCommand());
    String ac = event.getActionCommand();
    if(ac == "Open") {
      JFileChooser fileChooser = new JFileChooser(audioFile);
      if(fileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
      // If a file is already open clear up the media player, tree and tabs.
      if(audioFile!= null) {
        jmfPanel.closePlayer();

      }
      audioFile = fileChooser.getSelectedFile();
      String filename = audioFile.getName();
      this.setTitle("ChapterPlayer - " + filename);
      jmfPanel.makePlayer(audioFile.getPath());
      processTag(audioFile);
      return;
    } else if(ac == "Exit") {
      jmfPanel.closePlayer();
      System.exit(0);
    }  
  }
//-------------------------------------------------------------------------------------------------
  /**
   * ListSelectionListener interface.
   */
  public void valueChanged(ListSelectionEvent e) {
    if(e.getValueIsAdjusting() == false) {
      if(jmfPanel.playerRunning = true) {
        jmfPanel.pause();
      }
      int selectedIndex = list.getSelectedIndex();
      // System.out.println("valueChanged: selectedIndex = " + selectedIndex);
      if(selectedIndex >=0 && selectedIndex < childIds.length) {
        String selectedId = childIds[selectedIndex];
        doHorizontalSplitPane(selectedId);
      }
    }
  }
//-------------------------------------------------------------------------------------------------
  /**
   * MouseListener interface.
   */
  public void  mouseClicked(MouseEvent e) {
    if(e.getSource() == list) {
      int selectedIndex = list.getSelectedIndex();
      // System.out.println("Mouse clicked: selectedIndex = " + selectedIndex);
      if(selectedIndex == childIds.length) {
        // Back arrow selected.
        navigateUp();
        return;
      } else if(selectedIndex >=0 && selectedIndex < childIds.length) {
        String selectedId = childIds[selectedIndex];
        // Double click.
        if (e.getClickCount() == 2) {
          navigateDown(selectedId);
          return;
        }
        // Single click.
        doHorizontalSplitPane(selectedId);
        return;
      } else {
        // Ignore -1.
        return;
      }
    }      
  }
  public void    mouseEntered(MouseEvent e) { }
  public void    mouseExited(MouseEvent e) { }
  public void    mousePressed(MouseEvent e) { }
  public void    mouseReleased(MouseEvent e) { }
//-------------------------------------------------------------------------------------------------
  /**
   * KeyListener interface.
   */
  public void keyPressed(KeyEvent e) {
    if(e.getKeyCode() == KeyEvent.VK_ENTER) {
      int selectedIndex = list.getSelectedIndex();
      // System.out.println("ENTER key with selectedIndex = " + selectedIndex);
      if(selectedIndex == childIds.length) {
        // Back arrow selected.
        navigateUp();
        return;
      } else if(selectedIndex >=0 && selectedIndex < childIds.length) {
        String selectedId = childIds[selectedIndex];
        navigateDown(selectedId);
        return;
      } else {
        list.setSelectedIndex(0);
      }
    }
  }
  public void keyTyped(KeyEvent e) { }
  public void keyReleased(KeyEvent e) { }
//-------------------------------------------------------------------------------------------------
} 
//=================================================================================================