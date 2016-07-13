/********************************************************************************/
/*										*/
/*		ContextPanel.java						*/
/*										*/
/*	Interactive user interface for defining an S6 Context			*/
/*										*/
/********************************************************************************/
/*	Copyright 2007 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2007, Brown University, Providence, RI.				 *
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/context/ContextPanel.java,v 1.4 2015/09/23 17:57:52 spr Exp $ */


/*********************************************************************************
 *
 * $Log: ContextPanel.java,v $
 * Revision 1.4  2015/09/23 17:57:52  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.3  2014/02/26 14:06:06  spr
 * Update context panel for user interfaces.
 *
 * Revision 1.2  2013/09/13 20:32:11  spr
 * Fix context panel.
 *
 * Revision 1.1  2012-06-11 14:07:28  spr
 * Code cleanup
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.context;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import edu.brown.cs.ivy.swing.SwingGridPanel;
import edu.brown.cs.ivy.swing.SwingListPanel;
import edu.brown.cs.ivy.swing.SwingListSet;




class ContextPanel extends SwingGridPanel implements ContextConstants,
	ActionListener
{



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private ContextMain	the_context;

private JDialog 	active_dialog;

private JTextField	output_field;
private JCheckBox	noclass_check;
private JCheckBox	userpath_check;

private SwingListSet<File>    path_items;
private ClassPanel	class_panel;
private File		class_directory;

private SwingListSet<ContextUserFile> file_items;
private FilePanel	file_panel;

private SwingListSet<String> import_list;
private ImportPanel	import_panel;
private JTextField	package_field;
private JTextField	class_field;
private JTextField	source_field;

private ContextEclipse	eclipse_iface;
private JTextField	eclipse_workspace;
private JComboBox<String> eclipse_project;
private String		cur_project;

private boolean 	result_status;

private static final String NO_ITEM = "--None--";

private static String [] default_projects = new String [] { NO_ITEM };
private static S6UserFileType [] file_types = new S6UserFileType [] {
   S6UserFileType.READ, S6UserFileType.WRITE };

private static final long serialVersionUID = 1;





/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

ContextPanel(ContextMain cm)
{
   the_context = cm;

   path_items = new SwingListSet<File>(the_context.getClassPath());
   file_items = new SwingListSet<ContextUserFile>(the_context.getUserFiles());
   import_list = new SwingListSet<String>(the_context.getInsertImports());
   class_directory = null;
   eclipse_iface = new ContextEclipse();
   cur_project = null;

   active_dialog = null;
   result_status = false;

   setupPanel();
}



/********************************************************************************/
/*										*/
/*	Dialog methods								*/
/*										*/
/********************************************************************************/

boolean showDialog()
{
   result_status = false;

   active_dialog = new JDialog();

   active_dialog.setContentPane(this);
   active_dialog.setTitle("S6 Context Builder");
   active_dialog.setModal(true);
   active_dialog.pack();
   active_dialog.setVisible(true);

   if (noclass_check.isSelected()) the_context.clearClassPath();

   return result_status;
}



/********************************************************************************/
/*										*/
/*	Window setup methods							*/
/*										*/
/********************************************************************************/

private void setupPanel()
{
   beginLayout();

   addBannerLabel("Define an S6 Context");
   addSeparator();

   addSectionLabel("Context Name");
   output_field = addFileField("Save as",the_context.getOutputName(),JFileChooser.FILES_ONLY,this,null);
   addSeparator();

   addSectionLabel("Eclipse");
   eclipse_project = addChoice("Project",default_projects,0,this);
   String dflt = System.getProperty("user.home") + File.separator + "workspace";
   eclipse_workspace = addFileField("Workspace",dflt,JFileChooser.DIRECTORIES_ONLY,this,null);
   updateEclipseWorkspace();
   addSeparator();

   addSectionLabel("Insertion Context and Imports");
   package_field = addTextField("Package",the_context.getInsertPackage(),this,null);
   class_field = addTextField("Class",the_context.getInsertClass(),this,null);
   source_field = addFileField("Source File",the_context.getSourceFile(),
				  JFileChooser.FILES_ONLY,this,null);
   import_panel = new ImportPanel();
   addLabellessRawComponent("Imports",import_panel);
   addSeparator();

   addSectionLabel("Class Path");
   noclass_check = addBoolean("No External Classes",true,this);
   userpath_check = addBoolean("Use user class path",false,this);
   class_panel = new ClassPanel();
   addLabellessRawComponent("ClassPath",class_panel);
   addSeparator();

   addSectionLabel("User Data Files");
   file_panel = new FilePanel();
   addLabellessRawComponent("UserFile",file_panel);
   addSeparator();

   addBottomButton("Cancel","Cancel",this);
   addBottomButton("Build Context","Build",this);
   addBottomButtons();
}



/********************************************************************************/
/*										*/
/*	Callback methods							*/
/*										*/
/********************************************************************************/

public void actionPerformed(ActionEvent e)
{
   String cmd = e.getActionCommand();

   if (cmd.equals("Save as")) {
      the_context.setOutputName(output_field.getText());
    }
   else if (cmd.equals("Use user class path")) {
      if (userpath_check.isSelected()) {
	 String cp = System.getenv("CLASSPATH");
	 the_context.addClassPath(cp);
	 updateClassPaths();
	 noclass_check.setSelected(false);
       }
    }
   else if (cmd.equals("Workspace")) {
      updateEclipseWorkspace();
    }
   else if (cmd.equals("Project")) {
      if (eclipse_project == null) return;
      String s = (String) eclipse_project.getSelectedItem();
      if (s == null || s.equals(NO_ITEM) || s.equals(cur_project)) return;
      handleEclipseProject(s);
    }
   else if (cmd.equals("No External Classes")) ;        // CHECK THIS AT END
   else if (cmd.equals("Package")) {
      String s = package_field.getText();
      the_context.setInsertPackage(s);
    }
   else if (cmd.equals("Class")) {
      String s = class_field.getText();
      the_context.setInsertClass(s);
      package_field.setText(the_context.getInsertPackage());
      class_field.setText(the_context.getInsertClass());
      if (the_context.getSourceFile() != null) {
	 source_field.setText(the_context.getSourceFile().getPath());
       }
    }
   else if (cmd.equals("Source File")) {
      String s = source_field.getText();
      if (s == null || s.length() == 0) the_context.setSourceFile(null);
      else the_context.setSourceFile(new File(s));
    }
   else if (cmd.equals("Cancel")) {
      active_dialog.setVisible(false);
      result_status = false;
    }
   else if (cmd.equals("Build")) {
      active_dialog.setVisible(false);
      result_status = true;
    }
   else System.err.println("CONTEXT: Action " + cmd);
}




/********************************************************************************/
/*										*/
/*	Eclipse handlers							*/
/*										*/
/********************************************************************************/

private void updateEclipseWorkspace()
{
   String fnm = eclipse_workspace.getText();
   if (fnm == null) return;
   File wf = new File(fnm);
   if (!wf.exists()) return;
   if (!wf.isDirectory()) return;
   eclipse_iface.setWorkspace(wf);
   Collection<String> pset = eclipse_iface.getProjects();
   eclipse_project.removeAllItems();
   eclipse_project.addItem(NO_ITEM);
   for (String p : pset) eclipse_project.addItem(p);
   eclipse_project.setSelectedIndex(0);
}



private void handleEclipseProject(String proj)
{
   Collection<File> pf = eclipse_iface.getClassPath(proj);
   if (pf != null && pf.size() > 0) {
      the_context.clearClassPath();
      for (File f : pf) the_context.addClassPath(f);
      updateClassPaths();
      if (path_items.getSize() > 0) noclass_check.setSelected(false);
    }
}




/********************************************************************************/
/*										*/
/*	Class Path panel							*/
/*										*/
/********************************************************************************/

private void updateClassPaths()
{
   ArrayList<File> npth = new ArrayList<File>(the_context.getClassPath());

   path_items.removeAll();

   for (File f : npth) {
      path_items.addElement(f);
    }
}



private class ClassPanel extends SwingListPanel<File> {

   private static final long serialVersionUID = 1;

   ClassPanel() {
      super(path_items);
    }

   protected File createNewItem() {
      JFileChooser fc = new JFileChooser(class_directory);
      fc.setDialogTitle("Select new class path entry");
      fc.setFileFilter(new ClassFilter());
      fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
      fc.setMultiSelectionEnabled(true);
      int fg = fc.showOpenDialog(this);
      class_directory = fc.getCurrentDirectory();
      if (fg == JFileChooser.APPROVE_OPTION) {
	 File [] f = fc.getSelectedFiles();
	 if (f == null || f.length == 0) return null;
	 noclass_check.setSelected(false);
	 for (int i = 0; i < f.length; ++i) {
	    path_items.addElement(f[i]);
	  }
       }
      return null;
    }

   protected File editItem(Object itm) {
      File fitm = (File) itm;
      JFileChooser fc = new JFileChooser(class_directory);
      fc.setDialogTitle("Replace class path entry");
      fc.setFileFilter(new ClassFilter());
      fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
      fc.setSelectedFile(fitm);
      fc.setMultiSelectionEnabled(false);
      int fg = fc.showOpenDialog(this);
      class_directory = fc.getCurrentDirectory();
      if (fg == JFileChooser.APPROVE_OPTION) {
	 fitm = fc.getSelectedFile();
       }
      return fitm;
    }

   protected File deleteItem(Object itm) {
      return (File) itm;
    }

}	// end of subclass ClassPanel




private static class ClassFilter extends javax.swing.filechooser.FileFilter {

   public String getDescription()	{ return "Valid class path entries"; }

   public boolean accept(File f) {
      if (f.getPath().endsWith(".jar")) return true;
      if (f.isDirectory()) return true;
      return false;
    }

}	// end of subclass ClassFilter



/********************************************************************************/
/*										*/
/*	User File information							*/
/*										*/
/********************************************************************************/

private boolean editUserFile(ContextUserFile uf)
{
   SwingGridPanel pnl = new SwingGridPanel();

   pnl.beginLayout();
   pnl.addBannerLabel("Edit User File Item");
   JTextField lnm = pnl.addFileField("Local File",uf.getFileName(),JFileChooser.FILES_ONLY,null,null);
   JTextField rnm = pnl.addTextField("Remote File (/s6 or s:)",uf.getAccessName(),null,null);
   int idx = (uf.getFileMode() == S6UserFileType.READ ? 0 : 1);
   JComboBox<S6UserFileType> typ = pnl.addChoice("Access",file_types,idx,null);

   int fg = JOptionPane.showOptionDialog(file_panel,pnl,"Edit User File Item",
					    JOptionPane.OK_CANCEL_OPTION,
					    JOptionPane.PLAIN_MESSAGE,
					    null,null,null);

   if (fg != 0) return false;

   String l = lnm.getText();
   String r = rnm.getText();
   S6UserFileType ft = (S6UserFileType) typ.getSelectedItem();
   uf.set(new File(l),r,ft);

   return true;
}




/********************************************************************************/
/*										*/
/*	User File panel 							*/
/*										*/
/********************************************************************************/

private class FilePanel extends SwingListPanel<ContextUserFile> {

   private static final long serialVersionUID = 1;

   FilePanel() {
      super(file_items);
    }

   protected ContextUserFile createNewItem() {
      ContextUserFile uf = new ContextUserFile();
      if (editUserFile(uf)) file_items.addElement(uf);
      return null;
    }

   protected ContextUserFile editItem(Object itm) {
      ContextUserFile uf = (ContextUserFile) itm;
      editUserFile(uf);
      return uf;
    }

   protected ContextUserFile deleteItem(Object itm) {
      return (ContextUserFile) itm;
    }

}	// end of subclass ClassPanel




/********************************************************************************/
/*										*/
/*	Import panel								*/
/*										*/
/********************************************************************************/

private class ImportPanel extends SwingListPanel<String> {

   private static final long serialVersionUID = 1;

   ImportPanel() {
      super(import_list);
    }

   protected String createNewItem() {
      String rslt = JOptionPane.showInputDialog(ContextPanel.this,
						   "Enter Import Package/Class",
						   "Import",
						   JOptionPane.QUESTION_MESSAGE);
      if (rslt == null) return null;
      // check for valid import syntax here
      return rslt.intern();
    }

   protected String editItem(Object itm) {
      String rslt = JOptionPane.showInputDialog(ContextPanel.this,
						   "Enter Import Package/Class",itm);
      if (rslt == null) return null;
      // check for valid import syntax here
      return rslt.intern();
    }

   protected String deleteItem(Object itm) {
      return (String) itm;
    }

}	// end of subclass ImportPanel




}	// end of class ContextPanel




/* end of ContextPanel.java */

