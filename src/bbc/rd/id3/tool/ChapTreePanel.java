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

import javax.swing.JPanel;
import javax.swing.JTree;

public class ChapTreePanel extends JPanel {
  
  private static final long serialVersionUID = 0;
  private JTree jTree = null;

  /**
   * This is the default constructor
   */
  public ChapTreePanel() {
    super();
    initialize();
  }

  /**
   * This method initializes this
   * 
   * @return void
   */
  private void initialize() {
    this.setSize(300, 200);
    this.add(getJTree(), null);
  }

  /**
   * This method initializes jTree	
   * 	
   * @return javax.swing.JTree	
   */
  private JTree getJTree() {
    if (jTree == null) {
      jTree = new JTree();
      jTree.setBackground(java.awt.Color.lightGray);
    }
    return jTree;
  }

}
