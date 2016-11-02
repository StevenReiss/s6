/********************************************************************************/
/*                                                                              */
/*              SuiseInputPanel.java                                            */
/*                                                                              */
/*      User interface for specifying a user interface to search for            */
/*                                                                              */
/********************************************************************************/
/*      Copyright 2013 Brown University -- Steven P. Reiss                    */
/*********************************************************************************
 *  Copyright 2013, Brown University, Providence, RI.                            *
 *                                                                               *
 *                        All Rights Reserved                                    *
 *                                                                               *
 *  Permission to use, copy, modify, and distribute this software and its        *
 *  documentation for any purpose other than its incorporation into a            *
 *  commercial product is hereby granted without fee, provided that the          *
 *  above copyright notice appear in all copies and that both that               *
 *  copyright notice and this permission notice appear in supporting             *
 *  documentation, and that the name of Brown University not be used in          *
 *  advertising or publicity pertaining to distribution of the software          *
 *  without specific, written prior permission.                                  *
 *                                                                               *
 *  BROWN UNIVERSITY DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS                *
 *  SOFTWARE, INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND            *
 *  FITNESS FOR ANY PARTICULAR PURPOSE.  IN NO EVENT SHALL BROWN UNIVERSITY      *
 *  BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY          *
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,              *
 *  WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS               *
 *  ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE          *
 *  OF THIS SOFTWARE.                                                            *
 *                                                                               *
 ********************************************************************************/



package edu.brown.cs.s6.suise;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchService;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.filechooser.FileFilter;

import org.apache.batik.swing.JSVGCanvas;
import org.w3c.dom.Element;
// import fr.itris.glips.svgeditor.Editor;

import edu.brown.cs.ivy.exec.IvyExec;
import edu.brown.cs.ivy.file.IvyFile;
import edu.brown.cs.ivy.swing.SwingGridPanel;
import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Constants.S6Location;
import edu.brown.cs.s6.common.S6Constants.S6ScopeType;


class SuiseInputPanel extends JFrame implements SuiseConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private SuiseMain       suise_main;
private JTextField      keyword_field;
private JComboBox<S6ScopeType> scope_field;
private JComboBox<S6Location> source_field;
private JCheckBox       android_field;
private JButton         search_button;
private JSVGCanvas      svg_canvas;
private File            base_file;
private JTextField      file_field;
private WatchService    watch_service;
private long            last_modified;
private SuiseRawComponent file_root;

private static final long serialVersionUID = 1;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

SuiseInputPanel(SuiseMain sm,String file)
{
   super("User Interface Search Specification");

   suise_main = sm;

   setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

   if (file != null) {
      base_file = new File(file);
      if (!base_file.exists() || !base_file.canRead()) base_file = null;
    }

   if (base_file == null) base_file = IvyFile.expandFile(IMAGE_FILE);

   last_modified = 0;
   file_root = null;

   FileSystem fs = FileSystems.getDefault();
   try {
      watch_service = fs.newWatchService();
      Watcher w = new Watcher();
      w.start();
    }
   catch (IOException e) {
      watch_service = null;
    }

   setupPanel();
}



/********************************************************************************/
/*                                                                              */
/*      Window setup                                                            */
/*                                                                              */
/********************************************************************************/

private void setupPanel()
{
   keyword_field = null;
   scope_field = null;
   source_field = null;
   search_button = null;

   SwingGridPanel pnl = new SwingGridPanel();
   pnl.beginLayout();

   JLabel ttl = new JLabel("User Interface Search",JLabel.CENTER);
   ttl.setFont(ttl.getFont().deriveFont(Font.BOLD,16f));
   pnl.addLabellessRawComponent(null,ttl,true,false);

   SwingGridPanel toppnl = new SwingGridPanel();
   keyword_field = toppnl.addTextField("Keywords:",null,48,null,new KeywordListener());
   scope_field = toppnl.addChoice("Search Scope:",S6Constants.S6ScopeType.FILE,null);
   android_field = toppnl.addBoolean("Android Interface:",false,null);
   source_field = toppnl.addChoice("Search From",S6Constants.S6Location.GITHUB,null);
   file_field = toppnl.addFileField("Image File:",
         base_file.getPath(),JFileChooser.FILES_ONLY,
         new SvgFilter(),
         new FileHandler(),null);
   pnl.addLabellessRawComponent(null,toppnl,true,false);

   svg_canvas = new JSVGCanvas();
   svg_canvas.setPreferredSize(new Dimension(620,420));
   pnl.addLabellessRawComponent(null,svg_canvas,true,true);

   pnl.addBottomButton("Edit Image","Edit Image",new EditHandler());
   search_button = pnl.addBottomButton("SEARCH","SEARCH",new SearchHandler());
   search_button.setEnabled(false);
   pnl.addBottomButtons();

   setContentPane(pnl);

   pack();

   loadFile();

   checkValid();
}




synchronized private void loadFile()
{
   base_file = base_file.getAbsoluteFile();

   if (!base_file.exists()) {
      try {
         FileWriter fw = new FileWriter(base_file);
         fw.write("<svg width='600' hieght='400' xmlns='http://www.w3.org/2000/svg' viewBox='0 0 600.0 400.0'>");
         fw.write("<defs/></svg>");
         fw.close();
       }
      catch (IOException e) {
         System.err.println("SUISE: Problem creating empty SVG file");
         System.exit(1);
       }
    }

   String url = "file://" + base_file.getAbsolutePath();
   svg_canvas.loadSVGDocument(url);

   last_modified = base_file.lastModified();
   file_root = null;

   if (watch_service != null) {
      FileSystem fs = FileSystems.getDefault();
      System.err.println("LOAD FILE: " + base_file + " " + base_file.getParent());
      Path pth = fs.getPath(base_file.getParent());
      try {
         pth.register(watch_service,StandardWatchEventKinds.ENTRY_MODIFY,
         StandardWatchEventKinds.ENTRY_CREATE,StandardWatchEventKinds.ENTRY_DELETE);
       }
      catch (IOException e) {
         System.err.println("SUISE: Problem setting up watch for file: " + e);
       }
    }

   file_root = suise_main.setupSvg(url);

   checkValid();
}







private void checkValid()
{
   if (search_button == null) return;

   boolean valid = true;
   if (keyword_field == null) valid = false;
   else if (keyword_field.getText() == null) valid = false;
   else if (keyword_field.getText().length() == 0) valid = false;

   valid &= (file_root != null);

   search_button.setEnabled(valid);
}



/********************************************************************************/
/*                                                                              */
/*      Input handling                                                          */
/*                                                                              */
/********************************************************************************/

private class KeywordListener implements UndoableEditListener {

   @Override public void undoableEditHappened(UndoableEditEvent e) {
       checkValid();
    }

}       // end of inner class KeywordListener


private class EditHandler extends WindowAdapter implements ActionListener {

   @Override public void actionPerformed(ActionEvent evt) {
      String cmd1 = "inkscape " + base_file.getAbsolutePath();
      String cmd2 = IvyFile.expandName("java -jar $(S6)/public/GLIPSGraffiti1.5/GLIPSGraffiti.jar");
      cmd2 += " file://" + base_file.getAbsolutePath();

      try {
         new IvyExec(cmd1);
         return;
       }
      catch (IOException e) { }

      try {
         new IvyExec(cmd2);
         return;
       }
      catch (IOException e) {  }

      // JFrame frm = new JFrame();
      // frm.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
      // frm.addWindowListener(this);
      // Editor ed = new Editor();
      // String file = "file://" + base_file.getAbsolutePath();
      // ed.init(frm,file,true,true,false,true,null);
      // ed.setVisible(true);
      // frm.setSize(650,500);
    }

   @Override public void windowClosed(WindowEvent e) {
      loadFile();
    }

}       // end of inner class EditHandler



private class SearchHandler implements ActionListener, Runnable {

   @Override public void actionPerformed(ActionEvent e) {
      Thread th = new Thread(this,"SearchFor" + keyword_field.getText());
      th.start();
    }

   @Override public void run() {
      String scp = scope_field.getSelectedItem().toString();
      String src = source_field.getSelectedItem().toString();
      String keys = keyword_field.getText();
      boolean android = android_field.isSelected();
      SuiseSearchProcessor ssp = new SuiseSearchProcessor(file_root,scp,keys,src,android);
      SuiseResultPanel srp = new SuiseResultPanel(suise_main);
      srp.setVisible(true);
      Element rslt = ssp.doSearch();
      Element crslt = srp.processResult(rslt);
      if (crslt != null) {
         SuiseCodePanel cp = new SuiseCodePanel(suise_main,crslt);
         cp.setVisible(true);
       }
    }

}       // end of inner class SearchHandler





private class SvgFilter extends FileFilter {

   @Override public String getDescription() {
      return "svg files";
    }

   @Override public boolean accept(File f) {
      return f.getName().endsWith(".svg");
    }

}       // end of inner class SvgFilter



private class FileHandler implements ActionListener {

   @Override public void actionPerformed(ActionEvent evt) {
      String fnm = file_field.getText();
      base_file = new File(fnm);
      loadFile();
    }

}       // end of inner class FileHandler


private class Watcher extends Thread {

   Watcher() {
      super("FileSystemWatcher");
    }

   @Override public void run() {
      while (watch_service != null) {
         try {
            watch_service.take();
            if (base_file.lastModified() > last_modified) {
               loadFile();
             }
          }
         catch (InterruptedException e) { }
       }
    }
}



}       // end of class SuiseInputPanel




/* end of SuiseInputPanel.java */

