/********************************************************************************/
/*										*/
/*		RunnerSwingShow.java						*/
/*										*/
/*	Class to let user interact with a Swing component			*/
/*										*/
/********************************************************************************/
/*	Copyright 2013 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2013, Brown University, Providence, RI.				 *
 *										 *
 *			  All Rights Reserved					 *
 *										 *
 *  Permission to use, copy, modify, and distribute this software and its	 *
 *  documentation for any purpose other than its incorporation into a		 *
 *  commercial product is hereby granted without fee, provided that the 	 *
 *  above copyright notice appear in all copies and that both that		 *
 *  copyright notice and this permission notice appear in supporting		 *
 *  documentation, and that the name of Brown University not be used in 	 *
 *  advertising or publicity pertaining to distribution of the software 	 *
 *  without specific, written prior permission. 				 *
 *										 *
 *  BROWN UNIVERSITY DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS		 *
 *  SOFTWARE, INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND		 *
 *  FITNESS FOR ANY PARTICULAR PURPOSE.  IN NO EVENT SHALL BROWN UNIVERSITY	 *
 *  BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY 	 *
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,		 *
 *  WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS		 *
 *  ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE 	 *
 *  OF THIS SOFTWARE.								 *
 *										 *
 ********************************************************************************/



package edu.brown.cs.s6.runner;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DragSourceMotionListener;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.InputMethodEvent;
import java.awt.event.InputMethodListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MenuDragMouseEvent;
import javax.swing.event.MenuDragMouseListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuKeyEvent;
import javax.swing.event.MenuKeyListener;
import javax.swing.event.MenuListener;
import javax.swing.event.MouseInputListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.junit.Assert;



public class RunnerSwingShow
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private String		window_id;
private Component	root_component;
private Window		user_window;
private DefaultMutableTreeNode tree_root;
private Map<Component,String> node_names;
private EventListener	event_listener;
private JTree		component_tree;
private JEditorPane	event_area;
private Map<Document,Component> doc_map;
private Resulter	result_handler;
private Component	selected_component;
private Color		selected_bg;
private Boolean 	selected_opaque;
private List<EventData> all_events;


private static final Set<String> all_listeners;
private static final NumberFormat id_formatter = new DecimalFormat("C000");

private static Color EDITOR_BKG = new Color(200,200,255);
private static Color TREE_BKG = new Color(255,255,200);
private static Color SELECT_BKG = new Color(255,255,200);


static {
   all_listeners = new HashSet<String>();
   all_listeners.add("ComponentListener");
   all_listeners.add("FocusListener");
   all_listeners.add("HierarchyBoundsListener");
   all_listeners.add("HierarchyListener");
   all_listeners.add("InputMethodListener");
   all_listeners.add("KeyListener");
   all_listeners.add("MouseListener");
   all_listeners.add("MouseMotionListener");
   all_listeners.add("MouseWheelListener");
   all_listeners.add("ContainerListener");
   all_listeners.add("AncestorListener");
   all_listeners.add("VetoableChangeListener");
   all_listeners.add("PropertyChangeListener");
   all_listeners.add("ActionListener");
   all_listeners.add("AdjustmentListener");
   all_listeners.add("CaretListener");
   all_listeners.add("CellEditorListener");
   all_listeners.add("ChangeListener");
   all_listeners.add("DocumentListener");
   all_listeners.add("DragGestureListener");
   all_listeners.add("DragSourceListener");
   all_listeners.add("DragSourceMotionListener");
   all_listeners.add("DropTargetListener");
   all_listeners.add("HyperlinkListener");
   all_listeners.add("ItemListener");
   all_listeners.add("ListDataListener");
   all_listeners.add("ListSelectionListener");
   all_listeners.add("MenuDragMouseListener");
   all_listeners.add("MenuKeyListener");
   all_listeners.add("MenuListener");
   all_listeners.add("MouseInputListener");
   all_listeners.add("PopupMenuListener");
   all_listeners.add("RowSorterListener");
   all_listeners.add("TableColumnModelListener");
   all_listeners.add("TableModelListener");
   all_listeners.add("TextListener");
   all_listeners.add("TreeExpansionListener");
   all_listeners.add("TreeModelListener");
   all_listeners.add("TreeSelectionListener");
   all_listeners.add("TreeWillExpandListener");
   all_listeners.add("UndoableEditListener");
   all_listeners.add("WindowFocusListener");
   all_listeners.add("WindowListener");
   all_listeners.add("WindowStateListene");
}


/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public RunnerSwingShow(String id,Component c)
{
   window_id = id;
   root_component = c;
   user_window = null;
   node_names = new HashMap<Component,String>();
   event_listener = new EventListener();
   doc_map = new HashMap<Document,Component>();
   result_handler = new Resulter();
   selected_component = null;
   all_events = new ArrayList<EventData>();
}




/********************************************************************************/
/*										*/
/*	Processing methods							*/
/*										*/
/********************************************************************************/

public void process()
{
   setupRoot();
   setupTree();
   setupComponents(root_component,1);
   setupDisplay();

   boolean sts = result_handler.waitForResult();
   if (!sts) {
      System.exit(1);
      Assert.fail("User rejected solution");
    }
}



/********************************************************************************/
/*										*/
/*	Various setup methods							*/
/*										*/
/********************************************************************************/

private void setupRoot()
{
   if (root_component instanceof Dialog) {
      Dialog jd = (Dialog) root_component;
      jd.setResizable(true);
      jd.setModal(false);
      user_window = jd;
    }
   else if (root_component instanceof Frame) {
      Frame fm = (Frame) root_component;
      fm.setResizable(true);
      user_window = fm;
      user_window.pack();
    }
   else if (root_component instanceof Window) {
      user_window = (Window) root_component;
      user_window.pack();
    }
   else {
      root_component.setVisible(true);
      JFrame jf = new JFrame(window_id);
      jf.add(root_component);
      jf.pack();
      jf.validate();
      user_window = jf;
    }

}



private void setupTree()
{
   tree_root = new DefaultMutableTreeNode();
   tree_root.add(new SwingNode(root_component));
}



private int setupComponents(Component n,int idx)
{
   String nm = "C" + id_formatter.format(idx);
   idx++;
   node_names.put(n,nm);

   if (n instanceof Container) {
      Container cc = (Container) n;
      for (int i = 0; i < cc.getComponentCount(); ++i) {
	 Component c = cc.getComponent(i);
	 idx = setupComponents(c,idx);
       }
    }

   registerComponent(n);

   return idx;
}



private void registerComponent(Component c)
{
   Class<?> cls = c.getClass();
   for (Method m : cls.getMethods()) {
      String nm = m.getName();
      if (!nm.startsWith("add")) continue;
      if (m.getParameterTypes().length > 1) continue;
      String item = nm.substring(3);
      if (!all_listeners.contains(item)) continue;
      try {
	 m.invoke(c,event_listener);
       }
      catch (IllegalAccessException e) { }
      catch (IllegalArgumentException e) {
	 System.err.println("Illegal argument for " + cls + " " + nm);
	 System.err.println(e);
       }
      catch (InvocationTargetException e) { }
      catch (Throwable t) { }
    }
   if (c instanceof JTextComponent) {
      JTextComponent tc = (JTextComponent) c;
      Document d = tc.getDocument();
      if (d != null) doc_map.put(d,tc);
    }
}




/********************************************************************************/
/*										*/
/*	Display setup								*/
/*										*/
/********************************************************************************/

private void setupDisplay()
{
   JPanel pnl = new JPanel(new BorderLayout());

   component_tree = new JTree(tree_root);
   component_tree.setBackground(TREE_BKG);
   component_tree.setVisibleRowCount(10);
   component_tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
   component_tree.addTreeSelectionListener(new TreeSelector());
   component_tree.addMouseListener(new Mouser());

   event_area = new JEditorPane();
   event_area.setEditable(false);
   event_area.setBackground(EDITOR_BKG);
   event_area.setPreferredSize(new Dimension(500,300));

   JSplitPane spl = new JSplitPane(JSplitPane.VERTICAL_SPLIT,true);
   spl.setLeftComponent(new JScrollPane(component_tree));
   spl.setRightComponent(new JScrollPane(event_area));

   pnl.add(spl,BorderLayout.CENTER);

   Box btns = Box.createHorizontalBox();
   btns.add(Box.createHorizontalGlue());
   JButton accept = new JButton("ACCEPT");
   btns.add(accept);
   btns.add(Box.createHorizontalGlue());
   JButton reject = new JButton("REJECT");
   btns.add(reject);
   btns.add(Box.createHorizontalGlue());
   pnl.add(btns,BorderLayout.SOUTH);

   JFrame frm = new JFrame();
   frm.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
   frm.setContentPane(pnl);
   frm.pack();
   frm.setVisible(true);

   Dimension sz = root_component.getPreferredSize();
   user_window.pack();
   System.err.println("COMP SIZE = " + sz);
   user_window.setSize(sz);
   user_window.setPreferredSize(sz);
   user_window.setVisible(true);

   accept.addActionListener(result_handler);
   reject.addActionListener(result_handler);
   frm.addWindowListener(result_handler);
   user_window.addWindowListener(result_handler);
}



/********************************************************************************/
/*										*/
/*	Hanlde user result							*/
/*										*/
/********************************************************************************/

private class Resulter extends WindowAdapter implements ActionListener {

   private Boolean return_status;

   Resulter() {
      return_status = null;
    }

   @Override public void actionPerformed(ActionEvent e) {
      String cmd = e.getActionCommand();
      if (cmd.equalsIgnoreCase("ACCEPT")) setStatus(true);
      else setStatus(false);
    }

   @Override public void windowClosed(WindowEvent e) {
      setStatus(false);
    }

   private synchronized void setStatus(boolean fg) {
      return_status = fg;
      notifyAll();
    }

   synchronized boolean waitForResult() {
      while (return_status == null) {
	 try {
	    wait();
	  }
	 catch (InterruptedException e) { }
       }
      return return_status;
    }

}	// end of inner class Resulter



/********************************************************************************/
/*										*/
/*	Tree Model								*/
/*										*/
/********************************************************************************/

private String getText(Component c)
{
   try {
      Class<?> cls = c.getClass();
      Method m = cls.getMethod("getText");
      String s = (String) m.invoke(c);
      if (s == null) s = "";
      return s;
    }
   catch (Throwable t) { }

   return null;
}


private Icon getIcon(Component c)
{
   try {
      Class<?> cls = c.getClass();
      Method m = cls.getMethod("getIcon");
      return (Icon) m.invoke(c);
    }
   catch (Throwable t) { }

   return null;
}



private class SwingNode extends DefaultMutableTreeNode
{
   private static final long serialVersionUID = 1;

   SwingNode(Component c) {
      super(c);
      if (c instanceof Container) {
	 Container ctr = (Container) c;
	 for (int i = 0; i < ctr.getComponentCount(); ++i) {
	    Component chld = ctr.getComponent(i);
	    add(new SwingNode(chld));
	  }
       }
    }

   @Override public boolean getAllowsChildren() {
      return (getUserObject() instanceof Container);
    }

   @Override public String toString() {
      Component c = (Component) getUserObject();
      String tnm = c.getClass().getName();
      int idx = tnm.lastIndexOf(".");
      if (idx > 0) tnm = tnm.substring(idx+1);

      String id = node_names.get(c);
      if (id == null) id = "????";
      String txt = getText(c);
      Icon icn = getIcon(c);

      StringBuffer buf = new StringBuffer();
      buf.append(id);
      buf.append(": ");
      buf.append(tnm);
      buf.append(" ");
      buf.append(c.getWidth());
      buf.append("x");
      buf.append(c.getHeight());
      buf.append(" [");
      buf.append(c.getX());
      buf.append(",");
      buf.append(c.getY());
      buf.append("]");
      if (txt != null) {
	 buf.append(" `");
	 buf.append(txt);
	 buf.append("'");
       }
      if (icn != null) {
	 buf.append(" *");
       }
      if (!c.isVisible()) buf.append(" <INVISIBLE>");
      else if (!c.isShowing()) buf.append(" <NOT SHOWN>");

      return buf.toString();
    }




}	// end of inner class SwingNode



/********************************************************************************/
/*										*/
/*	Handler for tree selections						*/
/*										*/
/********************************************************************************/

private void setSelection(Component c)
{
   if (c == selected_component) return;
   if (selected_component != null) {
      selected_component.setBackground(selected_bg);
      if (selected_opaque != null && c instanceof JComponent) {
	 JComponent jc = (JComponent) c;
	 jc.setOpaque(selected_opaque);
	 selected_opaque = null;
       }
    }
   selected_component = c;
   if (selected_component != null) {
      selected_bg = selected_component.getBackground();
      selected_opaque = null;
      if (c instanceof JComponent) {
	 JComponent jc = (JComponent) c;
	 selected_opaque = jc.isOpaque();
	 jc.setOpaque(true);
       }
      selected_component.setBackground(SELECT_BKG);
    }
   resetOutput();
}



private class TreeSelector implements TreeSelectionListener
{
   @Override public void valueChanged(TreeSelectionEvent e) {
      TreePath tp = e.getPath();
      Component sc = null;
      if (tp != null && tp.getLastPathComponent() instanceof SwingNode) {
	 SwingNode sn = (SwingNode) tp.getLastPathComponent();
	 sc = (Component) sn.getUserObject();
       }
      setSelection(sc);
    }

}	// end of inner class TreeSelector




/********************************************************************************/
/*										*/
/*	Handle mouse right clicks						*/
/*										*/
/********************************************************************************/

private void handlePopup(MouseEvent e,Component c)
{
   if (e == null || c == null) return;
   JPopupMenu menu = new JPopupMenu("Component");
   String s = getText(c);
   if (s != null) {
      menu.add(new SetTextAction(c));
    }
   menu.add(new SetVisibleAction(c));
   
   menu.show(e.getComponent(),e.getX(),e.getY());
}


private class SetTextAction extends AbstractAction {

   private Component for_component;
   private static final long serialVersionUID = 1;

   SetTextAction(Component c) {
      super("Set Text ...");
      for_component = c;
    }

   @Override public void actionPerformed(ActionEvent e) {
      String v = JOptionPane.showInputDialog(component_tree,"Enter New Text");
      if (v == null) return;
      try {
	 Class<?> c = for_component.getClass();
	 Method m = c.getMethod("setText",String.class);
	 m.invoke(for_component,v);
       }
      catch (Throwable t) {
	 System.err.println("Problem setting text");
       }
    }

}	// end of inner class SetTextAction



private class SetVisibleAction extends AbstractAction {

   private Component for_component;
   private static final long serialVersionUID = 1;
   
   SetVisibleAction(Component c) {
      super((c.isVisible() ? "Set Invisible" : "Set Visible"));
      for_component = c;
    }
   
   @Override public void actionPerformed(ActionEvent e) {
      boolean fg = for_component.isVisible();
      for_component.setVisible(!fg);
    }

}	// end of inner class SetVisibleAction




private class Mouser extends MouseAdapter {

   @Override public void mousePressed(MouseEvent e) {
      JTree jt = (JTree) e.getSource();
      int selrow = jt.getRowForLocation(e.getX(),e.getY());
      if (selrow == -1 || !e.isPopupTrigger()) return;
      TreePath tp = jt.getPathForRow(selrow);
      if (tp == null) return;
      SwingNode sn = (SwingNode) tp.getLastPathComponent();
      Component c = (Component) sn.getUserObject();
      if (c == null) return;
      handlePopup(e,c);
    }

}	// end of inner class Mouser





/********************************************************************************/
/*										*/
/*	Event output methods							*/
/*										*/
/********************************************************************************/

private void generateOutput(String cb,EventObject e)
{
   // TODO: This should be better on what it does -- possibly specialize the method
   // by event type and let this be default

   String txt = e.getClass().toString();
   generateOutput(cb,(Component) e.getSource(),txt);
}


private void generateOutput(String cb,DocumentEvent e)
{
   Component c = doc_map.get(e.getDocument());
   // TODO: might want more info for document event
   String txt = e.getClass().toString();
   generateOutput(cb,c,txt);
}



private void generateOutput(String cb,Component c,String desc)
{
   if (event_area == null) return;

   EventData ed = new EventData(c,cb + ": " + desc);
   all_events.add(ed);
   if (ed.match(selected_component)) {
      appendOutput(ed.toString());
    }
}


private void resetOutput()
{
   Document d = event_area.getDocument();
   try {
      d.remove(0,d.getLength());
    }
   catch (BadLocationException e) { }
   for (EventData ed : all_events) {
      if (ed.match(selected_component)) appendOutput(ed.toString());
    }
}

private void appendOutput(String s)
{
   Document d = event_area.getDocument();
   try {
      d.insertString(d.getLength(),s,null);
      Rectangle r = event_area.modelToView2D(d.getLength()).getBounds();
      if (r != null) {
	 event_area.scrollRectToVisible(r);
       }
    }
   catch (BadLocationException ex) { }
}




/********************************************************************************/
/*										*/
/*	Data holder								*/
/*										*/
/********************************************************************************/

private class EventData {

   private Component for_component;
   private String component_id;
   private String event_data;

   EventData(Component c,String desc) {
      for_component = c;
      component_id = node_names.get(c);
      if (component_id == null) component_id = "????";
      event_data = desc;
    }

   @Override public String toString() {
      return component_id + ": " + event_data + "\n";
    }

   boolean match(Component c) {
      if (c == null) return true;
      return c == for_component;
    }

}	// end of inner class EventData




/********************************************************************************/
/*										*/
/*	Event Handlers								*/
/*										*/
/********************************************************************************/

private class EventListener implements ComponentListener, FocusListener,
	HierarchyBoundsListener, HierarchyListener, InputMethodListener,
	KeyListener, MouseListener, MouseMotionListener, MouseWheelListener,
	ContainerListener, AncestorListener, VetoableChangeListener,
	PropertyChangeListener, ActionListener, AdjustmentListener,
	CaretListener, CellEditorListener, ChangeListener, DocumentListener,
	DragGestureListener, DragSourceListener, DragSourceMotionListener,
	DropTargetListener, HyperlinkListener, ItemListener, ListDataListener,
	ListSelectionListener, MenuDragMouseListener, MenuKeyListener,
	MenuListener, MouseInputListener, PopupMenuListener, RowSorterListener,
	TableColumnModelListener, TableModelListener, TextListener,
	TreeExpansionListener, TreeModelListener, TreeSelectionListener,
	TreeWillExpandListener, UndoableEditListener, WindowFocusListener,
	WindowListener, WindowStateListener {

   @Override public void componentHidden(ComponentEvent e) { generateOutput("componentHidden",e); }

   @Override public void componentMoved(ComponentEvent e) { generateOutput("componentMoved",e); }

   @Override public void componentResized(ComponentEvent e) { generateOutput("componentResized",e); }

   @Override public void componentShown(ComponentEvent e) { generateOutput("componentShown",e); }

   @Override public void focusGained(FocusEvent e)	{  generateOutput("focusGained",e); }

   @Override public void focusLost(FocusEvent e)	{ generateOutput("focusLost",e); }

   @Override public void ancestorMoved(HierarchyEvent e) {  generateOutput("ancestorMoved",e); }

   @Override public void ancestorResized(HierarchyEvent e) { generateOutput("ancestorResized",e); }

   @Override public void hierarchyChanged(HierarchyEvent e) { generateOutput("hierarchyChanged",e); }

   @Override public void caretPositionChanged(InputMethodEvent e) { generateOutput("caretPositionChanged",e); }

   @Override public void inputMethodTextChanged(InputMethodEvent e) { generateOutput("inputMethodTextChanged",e); }

   @Override public void keyPressed(KeyEvent e) 	{  generateOutput("keyPressed",e); }

   @Override public void keyReleased(KeyEvent e)	{ generateOutput("keyReleased",e); }

   @Override public void keyTyped(KeyEvent e)		{ generateOutput("keyTyped",e); }

   @Override public void mouseClicked(MouseEvent e)	{ generateOutput("mouseClicked",e); }

   @Override public void mouseEntered(MouseEvent e)	{ generateOutput("mouseEntered",e); }

   @Override public void mouseExited(MouseEvent e)	{ generateOutput("mouseExited",e); }

   @Override public void mousePressed(MouseEvent e)	{ generateOutput("mousePressed",e); }

   @Override public void mouseReleased(MouseEvent e)	{ generateOutput("mouseReleased",e); }

   @Override public void mouseDragged(MouseEvent e)	{ generateOutput("mouseDragged",e); }

   @Override public void mouseMoved(MouseEvent e)	{ generateOutput("mouseMoved",e); }

   @Override public void mouseWheelMoved(MouseWheelEvent e) { generateOutput("mouseWheelMoved",e); }

   @Override public void componentAdded(ContainerEvent e) { generateOutput("componentAdded",e); }

   @Override public void componentRemoved(ContainerEvent e) { generateOutput("componentRemoved",e); }

   @Override public void ancestorAdded(AncestorEvent e) { generateOutput("ancestorAdded",e); }

   @Override public void ancestorMoved(AncestorEvent e) { generateOutput("ancestorMoved",e); }

   @Override public void ancestorRemoved(AncestorEvent e) { generateOutput("ancestorRemoved",e); }

   @Override public void vetoableChange(PropertyChangeEvent e) { generateOutput("vetoableChange",e); }

   @Override public void propertyChange(PropertyChangeEvent e) { generateOutput("propertyChange",e); }

   @Override public void actionPerformed(ActionEvent e) { generateOutput("actionPerformed",e); }

   @Override public void adjustmentValueChanged(AdjustmentEvent e) { generateOutput("adjustmentValueChanged",e); }

   @Override public void caretUpdate(CaretEvent e)	{ generateOutput("caretUpdate",e); }

   @Override public void editingCanceled(ChangeEvent e) { generateOutput("editingCanceled",e); }

   @Override public void editingStopped(ChangeEvent e)	{ generateOutput("editingStopped",e); }

   @Override public void stateChanged(ChangeEvent e)	{ generateOutput("stateChanged",e); }

   @Override public void changedUpdate(DocumentEvent e) { generateOutput("changedUpdate",e); }

   @Override public void insertUpdate(DocumentEvent e)	{ generateOutput("insertUpdate",e); }

   @Override public void removeUpdate(DocumentEvent e)	{ generateOutput("removeUpdate",e); }

   @Override public void dragGestureRecognized(DragGestureEvent e) { generateOutput("dragGestureRecognized",e); }

   @Override public void dragDropEnd(DragSourceDropEvent e) { generateOutput("dragDropEnd",e); }

   @Override public void dragEnter(DragSourceDragEvent e) { generateOutput("dragEnter",e); }

   @Override public void dragExit(DragSourceEvent e)	{ generateOutput("dragExit",e); }

   @Override public void dragOver(DragSourceDragEvent e) { generateOutput("dragOver",e); }

   @Override public void dropActionChanged(DragSourceDragEvent e) { generateOutput("dropActionChanged",e); }

   @Override public void dragMouseMoved(DragSourceDragEvent e) { generateOutput("dragMouseMoved",e); }

   @Override public void dragEnter(DropTargetDragEvent e) { generateOutput("dragEnter",e); }

   @Override public void dragExit(DropTargetEvent e)	{ generateOutput("dragExit",e); }

   @Override public void dragOver(DropTargetDragEvent e) { generateOutput("dragOver",e); }

   @Override public void drop(DropTargetDropEvent e)	{ generateOutput("drop",e); }

   @Override public void dropActionChanged(DropTargetDragEvent e) { generateOutput("dropActionChanged",e); }

   @Override public void hyperlinkUpdate(HyperlinkEvent e) { generateOutput("hyperlinkUpdate",e); }

   @Override public void itemStateChanged(ItemEvent e)	{ generateOutput("itemStateChanged",e); }

   @Override public void contentsChanged(ListDataEvent e) { generateOutput("contentsChanged",e); }

   @Override public void intervalAdded(ListDataEvent e) { generateOutput("intervalAdded",e); }

   @Override public void intervalRemoved(ListDataEvent e) { generateOutput("intervalRemoved",e); }

   @Override public void valueChanged(ListSelectionEvent e) { generateOutput("valueChanged",e); }

   @Override public void menuDragMouseDragged(MenuDragMouseEvent e) { generateOutput("menuDragMouseDragged",e); }

   @Override public void menuDragMouseEntered(MenuDragMouseEvent e) { generateOutput("menuDragMouseEntered",e); }

   @Override public void menuDragMouseExited(MenuDragMouseEvent e) { generateOutput("menuDragMouseExited",e); }

   @Override public void menuDragMouseReleased(MenuDragMouseEvent e) { generateOutput("menuDragMouseReleased",e); }

   @Override public void menuKeyPressed(MenuKeyEvent e) { generateOutput("menuKeyPressed",e); }

   @Override public void menuKeyReleased(MenuKeyEvent e) { generateOutput("menuKeyReleased",e); }

   @Override public void menuKeyTyped(MenuKeyEvent e)	{ generateOutput("menuKeyTyped",e); }

   @Override public void menuCanceled(MenuEvent e)	{ generateOutput("menuCanceled",e); }

   @Override public void menuDeselected(MenuEvent e)	{ generateOutput("menuDeselected",e); }

   @Override public void menuSelected(MenuEvent e)	{ generateOutput("menuSelected",e); }

   @Override public void popupMenuCanceled(PopupMenuEvent e) { generateOutput("popupMenuCanceled",e); }

   @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) { generateOutput("popupMenuWillBecomeInvisible",e); }

   @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) { generateOutput("popupMenuWillBecomeVisible",e); }

   @Override public void sorterChanged(RowSorterEvent e) { generateOutput("sorterChanged",e); }

   @Override public void columnAdded(TableColumnModelEvent e) { generateOutput("columnAdded",e); }

   @Override public void columnMarginChanged(ChangeEvent e) { generateOutput("columnMarginChanged",e); }

   @Override public void columnMoved(TableColumnModelEvent e) { generateOutput("columnMoved",e); }

   @Override public void columnRemoved(TableColumnModelEvent e) { generateOutput("columnRemoved",e); }

   @Override public void columnSelectionChanged(ListSelectionEvent e) { generateOutput("columnSelectionChanged",e); }

   @Override public void tableChanged(TableModelEvent e) { generateOutput("tableChanged",e); }

   @Override public void textValueChanged(TextEvent e)	{ generateOutput("textValueChanged",e); }

   @Override public void treeCollapsed(TreeExpansionEvent e) { generateOutput("treeCollapsed",e); }

   @Override public void treeExpanded(TreeExpansionEvent e) { generateOutput("treeExpanded",e); }

   @Override public void treeNodesChanged(TreeModelEvent e) { generateOutput("treeNodesChanged",e); }

   @Override public void treeNodesInserted(TreeModelEvent e) { generateOutput("treeNodesInserted",e); }

   @Override public void treeNodesRemoved(TreeModelEvent e) { generateOutput("treeNodesRemoved",e); }

   @Override public void treeStructureChanged(TreeModelEvent e) { generateOutput("treeStructureChanged",e); }

   @Override public void valueChanged(TreeSelectionEvent e) { generateOutput("valueChanged",e); }

   @Override public void treeWillCollapse(TreeExpansionEvent e) { generateOutput("treeWillCollapse",e); }

   @Override public void treeWillExpand(TreeExpansionEvent e) { generateOutput("treeWillExpand",e); }

   @Override public void undoableEditHappened(UndoableEditEvent e) { generateOutput("undoableEditHappened",e); }

   @Override public void windowGainedFocus(WindowEvent e) { generateOutput("windowGainedFocus",e); }

   @Override public void windowLostFocus(WindowEvent e) { generateOutput("windowLostFocus",e); }

   @Override public void windowActivated(WindowEvent e) { generateOutput("windowActivated",e); }

   @Override public void windowClosed(WindowEvent e)	{ generateOutput("windowClosed",e); }

   @Override public void windowClosing(WindowEvent e)	{ generateOutput("windowClosing",e); }

   @Override public void windowDeactivated(WindowEvent e) { generateOutput("windowDeactivated",e); }

   @Override public void windowDeiconified(WindowEvent e) { generateOutput("windowDeiconified",e); }

   @Override public void windowIconified(WindowEvent e) { generateOutput("windowIconified",e); }

   @Override public void windowOpened(WindowEvent e)	{ generateOutput("windowOpened",e);  }

   @Override public void windowStateChanged(WindowEvent e) { generateOutput("windowStateChanged",e); }




}	// end of innter class EventListener




}	// end of class RunnerSwingShow




/* end of RunnerSwingShow.java */

