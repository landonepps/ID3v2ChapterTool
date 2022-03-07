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

import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import bbc.rd.id3.CHAPFrame;
import bbc.rd.id3.CTOCFrame;
import bbc.rd.id3.ChapterFrame;

//=================================================================================================

public class ChapterTree extends JTree {

  private static final long serialVersionUID = 0;
  private DefaultMutableTreeNode root;
  private DefaultTreeModel treeModel;

//-------------------------------------------------------------------------------------------------  
  public ChapterTree() {
    super();
    root = new DefaultMutableTreeNode();
    root.setAllowsChildren(true);
    treeModel = new DefaultTreeModel(root, true);   // true - defines leaves as nodes which cannot have children.
    this.setModel(treeModel);
    this.setRootVisible(false);
    this.setEditable(true);
    this.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    this.setShowsRootHandles(true);
    this.setEditable(false);
  }
//-------------------------------------------------------------------------------------------------
  /**
   * Adds element to all instances of currently selected parent.
   * 
   * returns true if the selected parent can accept the new element, otherwise false.
   */
  public boolean addElementToSelected(String elementId, boolean acceptsChildren) {
    if(!doesSelectedAcceptChildren()) {
      System.err.println("ChapterTree.addElementToSelected: selected node does not accept children.");
      return false;
    }

    DefaultMutableTreeNode selectedNode = getSelectedNode();
    DefaultMutableTreeNode node = getInstanceOf(elementId); 
    if(node == null) {
      node = new DefaultMutableTreeNode(elementId, acceptsChildren);
    }

    if(selectedNode == root) {
      addNodeWithChildren(node, root);
      scrollPathToVisible(new TreePath(node.getPath()));
      if(acceptsChildren) {
        setSelectionPath(new TreePath(node.getPath()));
      }
    } else {
      String parentId = getId(selectedNode);
      DefaultMutableTreeNode[] parentNodes = getNodes(parentId);
      for(int i=0; i<parentNodes.length; i++) {
        DefaultMutableTreeNode parent = parentNodes[i];
        addNodeWithChildren(node, parent);
      }
    }
    // Move selection to new child of selected node unless this is a leaf. 
    if(acceptsChildren) {
      DefaultMutableTreeNode newSelectedNode = getInstanceOf(selectedNode, elementId);
      setSelectionPath(new TreePath(newSelectedNode.getPath()));
    }
    
    return true;
  }
//-------------------------------------------------------------------------------------------------
  /**
   * Set selected element to the specified child elementId.
   * 
   * If the elementId is not a child of the currently selected node this method does nothing.
   */
  public void setSelectedToChildElement(String elementId) {
    DefaultMutableTreeNode selectedNode = getSelectedNode();
    DefaultMutableTreeNode newSelectedNode = getInstanceOf(selectedNode, elementId);
    if(newSelectedNode != null) {
      setSelectionPath(new TreePath(newSelectedNode.getPath()));
    }
  }
//-------------------------------------------------------------------------------------------------
  /**
   * Set selected element to be the parent of the currently selected node.
   * 
   * If the elementId is not a child of the currently seleceted node this method does nothing.
   */
  public void setSelectedToParent() {
    DefaultMutableTreeNode selectedNode = getSelectedNode();
    DefaultMutableTreeNode selectedParent = (DefaultMutableTreeNode)(selectedNode.getParent());
    if(selectedParent != null) {
      setSelectionPath(new TreePath(selectedParent.getPath()));
    }
  }
//-------------------------------------------------------------------------------------------------
  /**
   * Removes selected element from all instances of the current parent.
   */
  public void removeSelectedElement() {
    DefaultMutableTreeNode selectedNode = getSelectedNode();
    if(selectedNode == root || selectedNode == null) return;
    DefaultMutableTreeNode selectedParent = (DefaultMutableTreeNode)(selectedNode.getParent());
    if(selectedParent == root) {
      treeModel.removeNodeFromParent(selectedNode);
      setSelectionPath(new TreePath(selectedParent.getPath()));
    }
    String parentId = getId(selectedParent);
    DefaultMutableTreeNode[] parentNodes = getNodes(parentId);
    for(int i=0; i<parentNodes.length; i++) {
      DefaultMutableTreeNode parent = parentNodes[i];
      String selectedId = getId(selectedNode);
      for(int j=0; j<parent.getChildCount(); j++) {
        DefaultMutableTreeNode child = ((DefaultMutableTreeNode)(parent.getChildAt(j)));
        if(getId(child).equals(selectedId)) {
          treeModel.removeNodeFromParent(child);
          setSelectionPath(new TreePath(parent.getPath()));
          break;
        }
      }
    }
    return;
  }
//-------------------------------------------------------------------------------------------------
  /**
   * @param root
   */
  protected void setRootId(String rootId) {
    root.setUserObject(rootId);
    setRootVisible(true);
  }
//-------------------------------------------------------------------------------------------------
  /**
   * Returns null if root.
   */
  public String getSelectedElementId() {
    return getId(getSelectedNode());
  }
//-------------------------------------------------------------------------------------------------
  /**
   * Returns null if selected element is root or parent is root.
   */
  protected String getSelectedParentElementId() {
    DefaultMutableTreeNode selected = getSelectedNode();
    if(selected == null || selected == root) return null;
    DefaultMutableTreeNode parent = (DefaultMutableTreeNode)(selected.getParent());
    if(parent == null || parent == root) return null;
    return (String)(parent.getUserObject());
  }  
//-------------------------------------------------------------------------------------------------
  /**
   * Returns null if root.
   */
  protected DefaultMutableTreeNode getSelectedNode() {
    DefaultMutableTreeNode selectedNode;
    TreePath selectedPath = getSelectionPath();
    if(selectedPath == null) {
      selectedNode = root;
    } else {
      selectedNode = (DefaultMutableTreeNode)(selectedPath.getLastPathComponent());
    }
    return selectedNode;
  }
//-------------------------------------------------------------------------------------------------
  /**
   * Returns .
   */
  protected boolean doesSelectedAcceptChildren() {
    if(getSelectedNode().getAllowsChildren()) {
      return true;
    } else {
      return false;
    }
  }
//-------------------------------------------------------------------------------------------------
//  /**
//   * Non-Leaf children are ones where areChildrenAccepted returns true. They cannot be added to nodes which already have leaf children and vice versa.
//   */
//  protected boolean doesSelectedOnlyAcceptNonLeafChildren() {
//    DefaultMutableTreeNode selected = getSelectedNode();
//    if(selected == root || selected == null) return false;
//    if(selected.getChildCount() != 0) {
//      DefaultMutableTreeNode child = ((DefaultMutableTreeNode)(selected.getChildAt(0)));
//      if(child.getAllowsChildren()) return true;
//    }
//    return false;
//  }
////-------------------------------------------------------------------------------------------------
//  /**
//   * Leaf children are ones where areChildrenAccepted returns false. They cannot be added to nodes which already have non-leaf children and vice versa.
//   */
//  protected boolean doesSelectedOnlyAcceptLeafChildren() {
//    DefaultMutableTreeNode selected = getSelectedNode();
//    if(selected == root || selected == null) return false;
//    if(selected.getChildCount() != 0) {
//      DefaultMutableTreeNode child = ((DefaultMutableTreeNode)(selected.getChildAt(0)));
//      if(!child.getAllowsChildren()) return true;
//    }
//    return false;
//  }
////-------------------------------------------------------------------------------------------------
//  /**
//   * Leaf children are ones where areChildrenAccepted returns false. They cannot be added to nodes which already have non-leaf children and vice versa.
//   */
//  protected boolean doesElementOnlyAcceptLeafChildren(String elementId) {
//    DefaultMutableTreeNode node = getInstanceOf(elementId);
//    if(node == root || node == null) return false;
//    if(node.getChildCount() != 0) {
//      DefaultMutableTreeNode child = ((DefaultMutableTreeNode)(node.getChildAt(0)));
//      if(!child.getAllowsChildren()) return true;
//    }
//    return false;
//  }
//-------------------------------------------------------------------------------------------------
  /**
   * Return true if the specified elementId is a child of the root node.
   */
  protected boolean isElementChildOfRoot(String elementId) {
    return isElementChildOfNode(root, elementId);
  }
//-------------------------------------------------------------------------------------------------
  /**
   * Return true if the specified elementId is a child of the specified node.
   */
  private boolean isElementChildOfNode(DefaultMutableTreeNode node, String elementId) { 
    for(int i=0; i<node.getChildCount(); i++) {
      if(((DefaultMutableTreeNode)node.getChildAt(i)).getUserObject().toString().equals(elementId)) {
        return true;
      }
    }
    return false;
  }  
//-------------------------------------------------------------------------------------------------
  /**
   * 
   */
  protected boolean doesSelectedHaveChildren() {
    DefaultMutableTreeNode selected = getSelectedNode();
    if(selected.getChildCount() == 0) {
      return false;
    } else {
      return true;
    }
  }
//-------------------------------------------------------------------------------------------------
  /**
   * 
   */
  protected boolean doesSelectedHaveChildrenWhichAcceptChildren() {
    DefaultMutableTreeNode selected = getSelectedNode();
    for(int i=0; i<selected.getChildCount(); i++) {
      if(((DefaultMutableTreeNode)selected.getChildAt(i)).getAllowsChildren()) {
        return true;
      }
    }
    return false;
  }
//-------------------------------------------------------------------------------------------------
  /**
   *  Return all the nodes with the specified elementId; 
   */
  private DefaultMutableTreeNode[] getNodes(String elementId) {
    Vector nodes = new Vector();
    getNodes(root, elementId, nodes);
    // System.out.println("getNodes found " + nodes.size() + " matching parent nodes");
    return (DefaultMutableTreeNode[])nodes.toArray(new DefaultMutableTreeNode[nodes.size()]);
  }
//-------------------------------------------------------------------------------------------------
  /**
   *  Add all nodes with the specified elementId below the specified node to the supplied Vector. 
   */
  private void getNodes(DefaultMutableTreeNode node, String elementId, Vector nodes) {
    //if(((String)(node.getUserObject())).equals(elementId)) nodes.addElement(node);
    for(int i=0; i<node.getChildCount(); i++) {
      DefaultMutableTreeNode child = ((DefaultMutableTreeNode)(node.getChildAt(i)));
      if(getId(child).equals(elementId)) nodes.addElement(child);
      getNodes(child, elementId, nodes);
    } 
  }  
//-------------------------------------------------------------------------------------------------
  /**
   *  Add node and its children to specified parent node. 
   */
  private void addNodeWithChildren(DefaultMutableTreeNode node, DefaultMutableTreeNode parentNode) {
    DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(node.toString(), node.getAllowsChildren());
    // Fix to avoid duplicate entries.
    for(int i=0; i<parentNode.getChildCount(); i++) {
      if(((DefaultMutableTreeNode)parentNode.getChildAt(i)).getUserObject().toString().equals(node.toString())) {
        return;
      }
    }
    treeModel.insertNodeInto(newNode, parentNode, parentNode.getChildCount());
    scrollPathToVisible(new TreePath(newNode.getPath()));
    for(int i=0; i<node.getChildCount(); i++) {
      DefaultMutableTreeNode child = (DefaultMutableTreeNode)(node.getChildAt(i));
      addNodeWithChildren(child, newNode);
    }
  }
//-------------------------------------------------------------------------------------------------
  /**
   *  Get the elementId of the specified node. 
   */
  private String getId(DefaultMutableTreeNode node) {
    if(node == null || node == root) {
      return "root";
    } else {
      return (String)(node.getUserObject());
    }
  }
//------------------------------------------------------------------------------------------------
  /**
   *   
   */
  protected boolean doesSelectedContain(String elementId) {
    Enumeration children = getSelectedNode().children();
    while(children.hasMoreElements()) {
      DefaultMutableTreeNode child = (DefaultMutableTreeNode)(children.nextElement());
      if(getId(child).equals(elementId)) {
        return true;
      }
    }
    return false;
  }
//------------------------------------------------------------------------------------------------
  /**
   *   
   */
  protected DefaultMutableTreeNode getInstanceOf(String elementId) {
    return getInstanceOf(root, elementId);
  }
//------------------------------------------------------------------------------------------------
  /**
   * Get an instance of an element in or below the specified node.
   */
   protected DefaultMutableTreeNode getInstanceOf(DefaultMutableTreeNode node, String elementId) {
     Enumeration children = node.children();
     while(children.hasMoreElements()) {
       DefaultMutableTreeNode child = (DefaultMutableTreeNode)(children.nextElement());
       if(getId(child).equals(elementId)) {
         return child;
       }
       DefaultMutableTreeNode found = getInstanceOf(child, elementId);
       if(found != null) {
         return found;
       }
     }
     return null;
    }
//------------------------------------------------------------------------------------------------
 /**
  * Determine whether an element is present in the tree.
  */
  protected boolean isElementPresent(String elementId) {
    return isElementPresent(root, elementId);
  }
//------------------------------------------------------------------------------------------------
 /**
  * Determine whether an element is present in or below the specified node.
  */
  protected boolean isElementPresent(DefaultMutableTreeNode node, String elementId) {
    Enumeration children = node.children();
    while(children.hasMoreElements()) {
      DefaultMutableTreeNode child = (DefaultMutableTreeNode)children.nextElement();
      if(getId(child).equals(elementId)) return true;
      if(isElementPresent(child, elementId)) {
        // System.out.println("Found " + object);
        return true;
      }
    }
    // System.out.println("Not found " + object);
    return false;
   }
//------------------------------------------------------------------------------------------------
 /**
  * Return the child elements of the specified elementId.
  */
  protected String[] getEntries(String elementId) {
    DefaultMutableTreeNode node = getInstanceOf(elementId);
    Enumeration children = node.children();
    // System.out.println("ChapterTree.getEntries: elementId = " + elementId + " child count = " + node.getChildCount());
    String[] childElementIds = new String[node.getChildCount()];
    int i=0;
    while(children.hasMoreElements()) {
       DefaultMutableTreeNode child = (DefaultMutableTreeNode)children.nextElement();
       childElementIds[i++] = getId(child);
    }
    return childElementIds;
  }
//-------------------------------------------------------------------------------------------------
 /**
  * Build tree to reflect the structure represented by the supplied frame set.
  */
  public void syncTree(ChapterFrame[] frames) {
    // Add top level CTOC frames and structure beneath.
    for(int i=0; i<frames.length; i++) {
      ChapterFrame frame = frames[i];
      try {
        // System.out.println("frameId = " + frame.getID());
        if(frame instanceof CTOCFrame) {
          CTOCFrame ctocFrame = (CTOCFrame)frame;
          if(ctocFrame.isTopLevel()) {
            addTreeEntry(root, ctocFrame, frames);
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    
    // Add CHAP frames which are not referenced by any CTOCs to the root node.
    setSelectionPath(new TreePath(root));
    for(int i=0; i<frames.length; i++) {
      ChapterFrame frame = frames[i];
      try {
        if(frame instanceof CHAPFrame) {
          CHAPFrame chapFrame = (CHAPFrame)frame;
          if(!isElementPresent(chapFrame.getElementId())) {
            addElementToSelected(chapFrame.getElementId(), false);
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
//-------------------------------------------------------------------------------------------------
 /**
  * Add an entry to the tree.
  */
  private void addTreeEntry(DefaultMutableTreeNode node, ChapterFrame frame, ChapterFrame[] frames) {
    setSelectionPath(new TreePath(node));
    if(frame instanceof CTOCFrame) {
      CTOCFrame ctocFrame = (CTOCFrame)frame;
      // System.out.println("ctocFrame.getElementId() = " + ctocFrame.getElementId());
      addElementToSelected(ctocFrame.getElementId(), true);
      DefaultMutableTreeNode newNode = getInstanceOf(node, ctocFrame.getElementId());
      // System.out.println("ChapterTree.addTreeEntry: elementId = " + ctocFrame.getElementId() + " newNode = " + newNode + " level =  " + newNode.getLevel());
      // System.out.println("ChapterTree.addTreeEntry: ctocFrame.getNumEntries() = " + ctocFrame.getNumEntries());
      for(int i=0; i<ctocFrame.getNumEntries(); i++) {
        ChapterFrame child = getFrame(ctocFrame.getEntry(i), frames);
        if(child != null) {
          addTreeEntry(newNode, child, frames);
        } else {
          System.err.println("ChapterTree.addTreeEntry: Unable to find child with elementId = " + ctocFrame.getEntry(i)); 
        }
      }
    } else if(frame instanceof CHAPFrame) {
      CHAPFrame chapFrame = (CHAPFrame)frame;
      // System.out.println("ChapterTree.addTreeEntry: chapFrame.getElementId() = " + chapFrame.getElementId());
      addElementToSelected(chapFrame.getElementId(), false);
    }
  }
//-------------------------------------------------------------------------------------------------
  /**
   * Get the frame with the specified elementId.
   */
  private static ChapterFrame getFrame(String elementId, ChapterFrame[] frames) {
    for(int i=0; i<frames.length; i++) {
      if(frames[i].getElementId().equals(elementId)) {
        return frames[i];
      }
    }
    return null;
  }
//-------------------------------------------------------------------------------------------------
}
//=================================================================================================