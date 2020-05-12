/********************************************************************************/
/*										*/
/*		TgenUI.java							*/
/*										*/
/*	User interface for test case generation 				*/
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



package edu.brown.cs.s6.tgen;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.lang.reflect.Modifier;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.w3c.dom.Element;

import edu.brown.cs.ivy.exec.IvyExec;
import edu.brown.cs.ivy.file.IvyFormat;
import edu.brown.cs.ivy.file.IvyLog;
import edu.brown.cs.ivy.swing.SwingComboBox;
import edu.brown.cs.ivy.swing.SwingGridPanel;
import edu.brown.cs.ivy.swing.SwingListPanel;
import edu.brown.cs.ivy.swing.SwingListSet;
import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.ivy.xml.IvyXmlWriter;
import edu.brown.cs.s6.context.ContextMain;


public class TgenUI implements TgenConstants
{


/********************************************************************************/
/*										*/
/*	Main program								*/
/*										*/
/********************************************************************************/

public static void main(String [] args)
{
   TgenUI tm = new TgenUI(args);
   tm.process();
}



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private SwingListSet<File>	path_items;
private SwingListSet<MethodData> method_set;
private File			class_directory;
private Map<File,List<ClassData>> path_map;
private JFrame			top_frame;
private TgenPanel		setup_panel;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

TgenUI(String [] args)
{
   path_items = new SwingListSet<File>();
   path_map = new HashMap<File,List<ClassData>>();
   class_directory = null;
   method_set = new SwingListSet<MethodData>();
   top_frame = null;
   setup_panel = null;
}



/********************************************************************************/
/*										*/
/*	Processing methods							*/
/*										*/
/********************************************************************************/

private void process()
{
   setup_panel = new TgenPanel();
   top_frame = new JFrame();
   top_frame.setContentPane(setup_panel);
   top_frame.pack();
   top_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
   top_frame.setVisible(true);
}





/********************************************************************************/
/*										*/
/*	Panel definitions							*/
/*										*/
/********************************************************************************/

private class TgenPanel extends SwingGridPanel {

   private ClassPathPanel classpath_panel;
   private SwingComboBox<ClassData> userclass_panel;
   private MethodPanel method_panel;
   private JButton active_button;
   private JTextField keyword_field;

   private static final long serialVersionUID = 1;

   TgenPanel() {
      class_directory = null;

      beginLayout();

      addBannerLabel("Specify What to Test");
      addSeparator();

      addSectionLabel("Class Path");
      classpath_panel = new ClassPathPanel();
      addLabellessRawComponent("ClassPath",classpath_panel);
      classpath_panel.addActionListener(new ClassPathUpdater());
      addSeparator();

      addSectionLabel("Items to Test");
      userclass_panel = addChoice("Class to Test",new ArrayList<ClassData>(),0,false,new ClassUpdater());
      userclass_panel.setCaseSensitive(false);

      method_panel = new MethodPanel();
      addRawComponent("Methods to Test",new JScrollPane(method_panel));
      addSeparator();

      addBannerLabel("Keywords Relevant to Code to Test");
      KeywordUpdater upd = new KeywordUpdater();
      keyword_field = addTextField("Keywords",null,upd,upd);

      addBottomButton("Cancel","Cancel",new CancelAction());
      active_button = addBottomButton("Find Test Cases","Build",new FindAction());
      active_button.setEnabled(false);

      addBottomButtons();
    }

   void updateClassList(Set<ClassData> classes) {
      userclass_panel.setContents(classes);
      checkCanSearch();
    }

   void updateMethodList() {
      ClassData cd = (ClassData) userclass_panel.getSelectedItem();
      if (cd != null) {
	 method_panel.updateMethods(cd.getMethods());
       }
      else {
	 method_panel.updateMethods(null);
       }
      top_frame.pack();
      checkCanSearch();
    }

   void checkCanSearch() {
      boolean fg = true;
      if (userclass_panel == null) fg = false;
      else if (userclass_panel.getSelectedItem() == null) fg = false;
      if (method_panel == null) fg = false;
      if (method_set == null || method_set.getSize() == 0) fg = false;
      if (keyword_field == null) fg = false;
      else if (keyword_field.getText().trim().length() == 0) fg = false;

      active_button.setEnabled(fg);
    }

   void setupContext(File outf) {
      List<String> args = new ArrayList<String>();
      args.add("-classpath");
      StringBuffer cp = new StringBuffer();
      for (File f : classpath_panel.getItemSet()) {
	 if (cp.length() > 0) cp.append(File.pathSeparator);
	 cp.append(f.getPath());
       }
      args.add(cp.toString());
      args.add("-output");
      args.add(outf.getPath());
      args.add("-noexit");
      String [] argls = args.toArray(new String [args.size()]);
      ContextMain.main(argls);
    }

   List<String> getKeywords() {
      String t = keyword_field.getText().trim();
      return IvyExec.tokenize(t);
    }

   ClassData getClassToTest() {
      return (ClassData) userclass_panel.getSelectedItem();
    }

   Collection<MethodData> getMethodsToTest() {
      Collection<MethodData> rslt = method_panel.getSelectedValuesList();
      if (rslt.isEmpty()) {
	 rslt = new ArrayList<MethodData>();
	 rslt.addAll(method_set.getElements());
       }
      return rslt;
    }

}	// end of inner class TgenPanel



/********************************************************************************/
/*										*/
/*	Basic Actions								*/
/*										*/
/********************************************************************************/

private static class CancelAction implements ActionListener {

   @Override public void actionPerformed(ActionEvent evt) {
      System.exit(0);
    }

}	// end of inner class CancelAction


private class FindAction implements ActionListener {

   @Override public void actionPerformed(ActionEvent evt) {
      try {
	 File ctxf = File.createTempFile("context",".s6ctx");
	 ctxf.deleteOnExit();
	 setup_panel.setupContext(ctxf);
	 String search = setupSearch(ctxf);
	 SearchHandler sh = new SearchHandler(ctxf,search);
	 sh.start();
       }
      catch (IOException e) {
	 System.err.println("TGENUI: Problem running search: " + e);
       }
    }

   private String setupSearch(File ctxf) {
      List<String> keys = setup_panel.getKeywords();
      ClassData testclass = setup_panel.getClassToTest();
      Collection<MethodData> methods = setup_panel.getMethodsToTest();

      IvyXmlWriter xw = new IvyXmlWriter();
      xw.begin("SEARCH");
      xw.field("WHAT","TESTCASES");
      xw.field("FORMAT","NONE");
      xw.field("LOCAL",false);
      xw.field("REMOTE",true);
      xw.field("OHLOH",true);
      xw.field("GITHUB",true);
      xw.begin("SIGNATURE");
      xw.begin("TESTING");
      xw.field("PACKAGE",testclass.getPackage());
      xw.field("NAME","S6Test" + testclass.getName());
      xw.begin("TESTEE");
      xw.field("PACKAGE",testclass.getPackage());

      xw.begin("CLASS");
      xw.field("NAME",testclass.getName());
      for (MethodData md : methods) {
	 md.outputMethod(xw);
       }
      xw.end("CLASS");

      xw.end("TESTEE");
      xw.end("TESTING");
      xw.end("SIGNATURE");

      xw.begin("KEYWORDS");
      for (String s : keys) {
	 xw.textElement("KEYWORD",s);
       }
      xw.textElement("KEYWORD","test");
      xw.textElement("KEYWORD","org.junit");
      xw.end("KEYWORDS");

      xw.begin("CONTEXT");
      xw.field("FILE",ctxf.getPath());
      xw.end("CONTEXT");

      xw.end("SEARCH");

      return xw.toString();
    }
}




/********************************************************************************/
/*										*/
/*	Class Path panel							*/
/*										*/
/********************************************************************************/

private class ClassPathPanel extends SwingListPanel<File> {


   private static final long serialVersionUID = 1;

   ClassPathPanel() {
      super(path_items);
      setVisibleRowCount(5);
    }

   @Override protected File createNewItem() {
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
	 for (int i = 0; i < f.length; ++i) {
	    path_items.addElement(f[i]);
	  }
       }
      return null;
    }

   @Override protected File editItem(Object itm) {
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

   @Override protected File deleteItem(Object itm) {
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




private class ClassPathUpdater implements ActionListener {

   @Override public void actionPerformed(ActionEvent e) {
      Set<ClassData> allclasses = new TreeSet<ClassData>();
      for (File cpfile : path_items) {
	 List<ClassData> lcd = setupPathMap(cpfile);
	 if (lcd != null) allclasses.addAll(lcd);
       }
      setup_panel.updateClassList(allclasses);
    }

}	// end of inner class ClassPathUpdater





/********************************************************************************/
/*										*/
/*	Methods list panel							*/
/*										*/
/********************************************************************************/


private class ClassUpdater implements ActionListener {

   @Override public void actionPerformed(ActionEvent e) {
       setup_panel.updateMethodList();
    }

}	// end of inner class ClassUpdater



private class MethodPanel extends JList<MethodData> implements ListSelectionListener {

   private static final long serialVersionUID = 1;

   MethodPanel() {
      super(method_set);
      setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
      setVisibleRowCount(5);
      setPreferredSize(new Dimension(400,120));
      addListSelectionListener(this);
    }

   void updateMethods(Collection<MethodData> methods) {
      method_set.removeAll();
      if (methods != null) {
	 for (MethodData md : methods) method_set.addElement(md);
       }
      method_set.update();
    }

   @Override public void valueChanged(ListSelectionEvent evt) {
      setup_panel.checkCanSearch();
    }

}	// end of inner class MethodPanel



/********************************************************************************/
/*										*/
/*	Keyword manager 							*/
/*										*/
/********************************************************************************/

private class KeywordUpdater implements ActionListener, UndoableEditListener {

   @Override public void actionPerformed(ActionEvent evt) {
      setup_panel.checkCanSearch();
    }

   @Override public void undoableEditHappened(UndoableEditEvent evt) {
      setup_panel.checkCanSearch();
    }

}	// end of inner class KeywordUpdater



/********************************************************************************/
/*										*/
/*	Find available classes on a class path entry				*/
/*										*/
/********************************************************************************/

private List<ClassData> setupPathMap(File itm)
{
   if (path_map.containsKey(itm)) return path_map.get(itm);

   List<ClassData> rslt = new ArrayList<ClassData>();
   path_map.put(itm,rslt);

   if (itm.isDirectory()) {
      setupPathMap(null,itm,rslt);
    }
   else {
      try {
	 JarFile jf = new JarFile(itm);
	 for (Enumeration<JarEntry> en = jf.entries(); en.hasMoreElements(); ) {
	    JarEntry jent = en.nextElement();
	    String name = jent.getName().replace("/",".");
	    if (!name.endsWith(".class")) continue;
	    name = name.substring(0,name.length()-6);
	    int idx = name.lastIndexOf(".");
	    String pkg = null;
	    if (idx > 0) {
	       pkg = name.substring(0,idx);
	       name = name.substring(idx+1);
	     }
	    InputStream ins = jf.getInputStream(jent);
	    try {
	       KnownClassVisitor kcv = new KnownClassVisitor();
	       ClassReader cr = new ClassReader(ins);
	       cr.accept(kcv,ClassReader.SKIP_CODE);
	       addClassData(pkg,name,kcv.getAsmClass(),rslt);
	     }
	    catch (IOException e) { }
	    try {
	       ins.close();
	     }
	    catch (IOException e) { }
	  }
	 jf.close();
       }
      catch (IOException e) {
	 System.err.println("TGENUI: Problem reading jar file: " + e);
       }
    }

   return rslt;
}



private void setupPathMap(String pfx,File dir,List<ClassData> result)
{
   if (dir.isDirectory()) {
      for (File fil : dir.listFiles()) {
	 String npfx = pfx;
	 if (fil.isDirectory()) {
	    npfx = fil.getName();
	    if (pfx != null) npfx = pfx + "." + npfx;
	  }
	 setupPathMap(npfx,fil,result);
       }
    }
   else if (dir.getName().endsWith(".class")) {
      FileInputStream ins = null;
      try {
	 ins = new FileInputStream(dir);
	 KnownClassVisitor kcv = new KnownClassVisitor();
	 ClassReader cr = new ClassReader(ins);
	 cr.accept(kcv,ClassReader.SKIP_CODE);
	 addClassData(pfx,dir.getName(),kcv.getAsmClass(),result);
       }
      catch (IOException e) {
	 System.err.println("TGENUI: Problem reading class file: " + e);
       }
      finally {
	 if (ins != null) {
	    try {
	       ins.close();
	     }
	    catch (IOException e) { }
	  }
       }
    }
}



private void addClassData(String pfx,String nam,ClassData cd,List<ClassData> result)
{
   if (cd == null) return;
   if (nam != null) {
      if (nam.endsWith(".class")) nam = nam.substring(0,nam.length()-6);
      if (!cd.getName().equals(nam)) return;
      if (pfx == null && cd.getPackage() != null) return;
      if (pfx != null && !pfx.equals(cd.getPackage())) return;
    }
   result.add(cd);
}




/********************************************************************************/
/*										*/
/*	Hold class and method information					*/
/*										*/
/********************************************************************************/

private static class ClassData implements Comparable<ClassData> {

   private String package_name;
   private String class_name;
   private Set<MethodData> available_methods;

   ClassData(String pkg,String name,String generics) {
      package_name = pkg;
      class_name = name;
      available_methods = new TreeSet<MethodData>();
    }

   void addMethod(MethodData md) {
      available_methods.add(md);
    }

   boolean isTestable() {
      if (available_methods.size() == 0) return false;
      return true;
    }

   String getPackage()			{ return package_name; }
   String getName()			{ return class_name; }

   Collection<MethodData> getMethods()	{ return available_methods; }

   @Override public String toString() {
      if (package_name == null) return class_name;
      return package_name + "." + class_name;
    }

   @Override public int compareTo(ClassData cd) {
      return toString().compareTo(cd.toString());
    }

}	// end of inner class ClassData




private static class MethodData implements Comparable<MethodData> {

   private String method_name;
   private String method_signature;
   private String method_display;
   private int	  method_access;

   MethodData(String name,String signature,int access) {
      method_name = name;
      method_signature = signature;
      method_display = name + signature;
      String txt = IvyFormat.formatTypeName(signature);
      int idx = txt.indexOf("(");
      if (idx >= 0) {
	 String ret = txt.substring(0,idx);
	 String args = txt.substring(idx);
	 method_display = name + args + " : " + ret;
       }
      method_access = access;
    }

   @Override public String toString() {
      return method_display;
    }

   @Override public int compareTo(MethodData md) {
      return toString().compareTo(md.toString());
    }

   void outputMethod(IvyXmlWriter xw) {
      xw.begin("METHOD");
      xw.field("NAME",method_name);
      xw.field("SIGNATURE",method_signature);
      if (Modifier.isStatic(method_access)) {
	 xw.field("STATIC",true);
       }
      xw.end("METHOD");
    }
}	// end of inner class MethodData



/********************************************************************************/
/*										*/
/*	ASM visitor to handle scanning class files				*/
/*										*/
/********************************************************************************/


private class KnownClassVisitor extends ClassVisitor {

   private ClassData cur_class;

   KnownClassVisitor() {
      super(Opcodes.ASM4);
      cur_class = null;
    }

   ClassData getAsmClass() {
      if (cur_class != null && !cur_class.isTestable()) cur_class = null;
      return cur_class;
    }

   public void visit(int version,int access,String name,String sign,String sup,String [] ifcs) {
      if (Modifier.isPrivate(access)) return;	// ignore private classes
      String fnm = name.replace("/",".");
      int idx = fnm.lastIndexOf(".");
      String pkg = null;
      if (idx > 0) {
	 pkg = fnm.substring(0,idx);
	 fnm = fnm.substring(idx+1);
       }
      cur_class = new ClassData(pkg,fnm,sign);
    }

   public AnnotationVisitor visitAnnotation(String dsc,boolean vis)	{ return null; }
   public void visitAttribute(Attribute attr)				{ }
   public void visitInnerClass(String n,String o,String i,int acc)	{ }
   public void visitOuterClass(String own,String nam,String d)		{ }
   public void visitSource(String src,String dbg)			{ }

   public FieldVisitor visitField(int access,String name,String desc,String sign,Object val) {
      return null;
    }

   public MethodVisitor visitMethod(int access,String name,String desc,String sign,String [] excs) {
      if (Modifier.isPrivate(access)) return null;	// ignore private methods
      if (cur_class == null) return null;
      if (name.startsWith("<")) return null;
      MethodData md = new MethodData(name,desc,access);
      cur_class.addMethod(md);
      return null;
    }

   public void visitEnd()						{ }

}	// end of class KnownClassVisitor




/********************************************************************************/
/*										*/
/*	Communicate with S6							*/
/*										*/
/********************************************************************************/

static Element sendMessageToS6(String cnts)
{
   byte [] cntb = cnts.getBytes();

   Element rslt = null;

   try {
      URL u = new URL("http://bubbles.cs.brown.edu/s6web/dosearch1.php");
      HttpURLConnection huc = (HttpURLConnection) u.openConnection();
      huc.setDoInput(true);
      huc.setDoOutput(true);
      huc.setUseCaches(false);
      huc.setRequestMethod("POST");
      huc.setRequestProperty("Accept","application/xml");
      huc.setRequestProperty("Content-Length",Integer.toString(cntb.length));
      huc.setRequestProperty("Content-Type","text/xml");
      huc.connect();
      OutputStream ots = huc.getOutputStream();
      ots.write(cntb);
      ots.close();
      InputStream ins = huc.getInputStream();
      // rslt = IvyXml.loadXmlFromStream(ins);
      Reader r = new InputStreamReader(ins);
      StringBuffer buf = new StringBuffer();
      char [] cbuf = new char[8192];
      for ( ; ; ) {
	 int ct = r.read(cbuf);
	 if (ct <= 0) break;
	 buf.append(cbuf,0,ct);
       }
      System.err.println("S6 RESULT: " + buf.toString());
      rslt = IvyXml.convertStringToXml(buf.toString());
      ins.close();
    }
   catch (IOException e) {
      IvyLog.logE("TGENUI","Error sending to S6: " + e);
      return null;
    }

   return rslt;
}



/********************************************************************************/
/*										*/
/*	ResultPanel								*/
/*										*/
/********************************************************************************/

private class SearchHandler extends Thread {

   private File context_file;
   private String s6_query;

   SearchHandler(File ctxf,String cnts) {
      super("SearchFor_" + ctxf.getName());
      context_file = ctxf;
      s6_query = cnts;
    }

   @Override public void run() {
      ResultPanel pnl = new ResultPanel();
      pnl.setVisible(true);
      IvyXmlWriter xw = new IvyXmlWriter();
      xw.begin("FILE");
      xw.field("EXTENSION",".s6ctx");
      try {
	 xw.cdataElement("CONTENTS",encodeFile(context_file));
       }
      catch (IOException e) {
	 pnl.processResult(null);
	 xw.close();
	 return;
       }
      xw.end("FILE");
      Element fctx = sendMessageToS6(xw.toString());
      xw.close();
      String ctxname = IvyXml.getText(fctx);

      s6_query = s6_query.replace(context_file.getPath(),ctxname);

      Element rslt = sendMessageToS6(s6_query);
      context_file.delete();
      pnl.processResult(rslt);
    }


   private String encodeFile(File f) throws IOException {
      StringBuffer rslt = new StringBuffer();
      FileInputStream ins = new FileInputStream(f);
      byte [] buf = new byte[8192];
      for ( ; ; ) {
	 int rln = ins.read(buf);
	 if (rln < 0) break;
	 for (int i = 0; i < rln; ++i) {
	    int v = buf[i] & 0xff;
	    if (i % 32 == 0 && i != 0) rslt.append("\n");
	    String s1 = Integer.toHexString(v);
	    if (s1.length() == 1) rslt.append("0");
	    rslt.append(s1);
	  }
       }
      ins.close();
      return rslt.toString();
    }

}	// end of inner class SearchHandler




private class ResultPanel extends JFrame {

   private JPanel display_panel;
   private Finisher finish_handler;
   private JLabel status_area;

   private static final long serialVersionUID = 1;


   ResultPanel() {
      super("Test Case Search Results");

      setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
      finish_handler = new Finisher(this);
      setupPanel();
    }

   private void setupPanel() {
      display_panel = new JPanel(new BorderLayout());
      JLabel ttl = new JLabel("Test Case Search Results",JLabel.CENTER);
      ttl.setFont(ttl.getFont().deriveFont(Font.BOLD,16f));
      display_panel.add(ttl,BorderLayout.NORTH);

      status_area = new JLabel("Awaiting search results ...",JLabel.CENTER);
      display_panel.add(status_area,BorderLayout.CENTER);

      setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
      addWindowListener(finish_handler);

      setContentPane(display_panel);
      pack();
    }

   void processResult(Element xml) {
      Element solns = IvyXml.getChild(xml,"SOLUTIONS");
      Element soln = IvyXml.getChild(solns,"SOLUTION");
      if (soln == null) {
	 status_area.setText("No results found");
	 status_area.setBackground(Color.RED);
       }
      else {
	 remove(status_area);
	 JPanel pnl = getCodePanel(soln);
	 display_panel.add(pnl,BorderLayout.CENTER);
	 pack();
       }
    }

   private JPanel getCodePanel(Element soln) {
      JPanel pnl = new JPanel(new BorderLayout());
      Border bb1 = BorderFactory.createLineBorder(Color.GRAY);
      Border bb2 = BorderFactory.createTitledBorder(bb1,IvyXml.getTextElement(soln,"NAME"));
      pnl.setBorder(bb2);
      JEditorPane ep = new JEditorPane();
      Font oft = ep.getFont();
      Font ft = new Font(Font.MONOSPACED,Font.PLAIN,oft.getSize());
      ep.setFont(ft);
      ep.setEditable(false);
      JScrollPane jsp = new JScrollPane(ep);

      pnl.add(jsp,BorderLayout.CENTER);
      pnl.setPreferredSize(new Dimension(700,300));
      pnl.setMinimumSize(new Dimension(300,300));
      ep.setContentType("text/java");
      ep.setText(IvyXml.getTextElement(soln,"CODE"));

      return pnl;
    }

}	// end of inner class ResultPanel



private class Finisher extends WindowAdapter implements ActionListener {

   private ResultPanel result_panel;
   private boolean have_results;
   private Element search_results;

   Finisher(ResultPanel rslt) {
      result_panel = rslt;
      have_results = false;
      search_results = null;
    }

   @Override public void actionPerformed(ActionEvent evt) {
      result_panel.setVisible(false);
      getResults();
    }

   @Override public void windowClosed(WindowEvent e) {
      if (have_results) getResults();
    }

   private void getResults() {
      // Element rslt = reportResults();
      synchronized (this) {
	 if (search_results == null) {
	   // search_results = rslt;
	    notifyAll();
	  }
       }
    }

}	// end of inner class Finisher








}	// end of class TgenUI




/* end og TgenUI.java */
