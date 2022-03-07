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

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ComponentDialog extends JDialog implements ActionListener {
  
  private static final long serialVersionUID = 0;
  private static ComponentDialog dialog;
  private static String action;
  public static final String CANCEL_ACTION = "cancel";
  public static final String OK_ACTION = "ok";

  /**
   * Set up and show the dialog.  The first Component argument determines which frame the dialog depends on; it should be
   * a component in the dialog's controlling frame. The second Component argument should be null if you want the dialog
   * to come up with its left corner in the center of the screen; otherwise, it should be the component on top of which the
   * dialog should appear.
   */
  public static String showDialog(Component owner, Component content, String title, boolean isModal) {
    Frame frame = JOptionPane.getFrameForComponent(owner);
    dialog = new ComponentDialog(frame, content, title, isModal);
    dialog.setVisible(true);
    return action;
  }

  private ComponentDialog(Frame owner, Component content, String title, boolean isModal) {
    super(owner, title, isModal);
    //Create and initialize the buttons.
    JButton cancelButton = new JButton("Cancel");
    cancelButton.setActionCommand("cancel");
    cancelButton.addActionListener(this);
    final JButton okButton = new JButton("OK");
    okButton.setActionCommand("ok");
    okButton.addActionListener(this);
    getRootPane().setDefaultButton(okButton);
    
    JScrollPane scrollPane = new JScrollPane();
    // scrollPane.setPreferredSize(new java.awt.Dimension(801,360));
    scrollPane.setViewportView(content);
    scrollPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

    // Lay out the buttons from left to right.
    JPanel buttonPane = new JPanel();
    buttonPane.setLayout(new FlowLayout(FlowLayout.CENTER));
    buttonPane.add(cancelButton);
    buttonPane.add(okButton);

    // Put everything together, using the content pane's BorderLayout.
    Container contentPane = getContentPane();
    contentPane.add(scrollPane, BorderLayout.CENTER);
    contentPane.add(buttonPane, BorderLayout.PAGE_END);
    pack();
    setLocationRelativeTo(owner);
  }
//-------------------------------------------------------------------------------------------------
  public void actionPerformed(ActionEvent e) {
    ComponentDialog.action = e.getActionCommand();
    ComponentDialog.dialog.setVisible(false);
  }
//-------------------------------------------------------------------------------------------------
}
//=================================================================================================