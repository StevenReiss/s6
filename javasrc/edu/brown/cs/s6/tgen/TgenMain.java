/********************************************************************************/
/*										*/
/*		TgenMain.java							*/
/*										*/
/*	Main program for generating tests for test case generation		*/
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.w3c.dom.Element;

import edu.brown.cs.ivy.exec.IvyExec;
import edu.brown.cs.ivy.file.IvyFile;
import edu.brown.cs.ivy.jcomp.JcompAst;
import edu.brown.cs.ivy.jcomp.JcompControl;
import edu.brown.cs.ivy.jcomp.JcompMessage;
import edu.brown.cs.ivy.jcomp.JcompProject;
import edu.brown.cs.ivy.jcomp.JcompSemantics;
import edu.brown.cs.ivy.jcomp.JcompSource;
import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.ivy.xml.IvyXmlWriter;
import edu.brown.cs.s6.search.SearchWordFactory;


public class TgenMain implements TgenConstants
{

// TODO: keywords don't work
// TODO: duplicate check also fails


/********************************************************************************/
/*										*/
/*	Main program								*/
/*										*/
/********************************************************************************/

public static void main(String [] args)
{
   TgenMain tm = new TgenMain(args);
   tm.process();
   System.exit(0);
}



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private TgenOhloh	search_engine;
private Random		random_gen;
private String		ant_template;
private boolean 	remove_old;
private Map<String,String []> keyword_map;
private Set<String>	items_done;

private boolean 	random_get;

private final static String [] STD_PREFIX = {
      "java.", "javax.", "org.omg.", "org.ietf.", "org.w3c.", "org.xml.",
      "org.junit."
};



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

private TgenMain(String [] args)
{
   search_engine = new TgenOhloh();
   random_gen = new Random(1234);
   remove_old = true;
   random_get = false;
   keyword_map = new HashMap<String,String []>();
   items_done = new HashSet<String>();

   scanArgs(args);

   try {
      ant_template = IvyFile.loadFile(new File(TGEN_ANT_TEMPLATE));
    }
   catch (IOException e) {
      System.err.println("TGEN: Problem getting ant template: " + e);
    }
}




/********************************************************************************/
/*										*/
/*	Argument scanning							*/
/*										*/
/********************************************************************************/

private void scanArgs(String [] args)
{
   for (int i = 0; i < args.length; ++i) {
      if (args[i].startsWith("-")) {
	 if (args[i].startsWith("-k")) {                                        // -keep
	    remove_old = false;
	  }
	 else if (args[i].startsWith("-n")) {                                   // -new
	    remove_old = true;
	  }
	 else if (args[i].startsWith("-R") && i+1 < args.length) {              // -R <#>
	    try {
	       int no = Integer.parseInt(args[++i]);
	       random_gen = new Random(no);
	     }
	    catch (NumberFormatException e) { badArgs(); }
	  }
	 else if (args[i].startsWith("-r") || args[i].startsWith("-R")) {       // -random
	    random_gen = new Random();
	  }
	 else if (args[i].startsWith("-s") && i+1 < args.length) {              // -start <page>
	    try {
	       int no = Integer.parseInt(args[++i]);
	       search_engine.setStartPage(no);
	       remove_old = false;
	     }
	    catch (NumberFormatException e) { badArgs(); }
	  }
	 else badArgs();
       }
      else badArgs();
    }
}


private void badArgs()
{
   System.err.println("TGEN: tgen [-keep|new] [-Random <#>] [-random] [-start <page>]");
}




/********************************************************************************/
/*										*/
/*	Processing methods							*/
/*										*/
/********************************************************************************/

private void process()
{
   if (remove_old) {
      try {
	 IvyFile.remove(TGEN_DIRECTORY);
       }
      catch (IOException e_) {
	 System.err.println("TGEN: Problem removing old outputs");
       }
    }

   readKeywords();

   Set<String> done = new HashSet<String>();

   int ctr = 0;
   while (ctr < 1000) {
      List<TgenSource> srcs = null;
      if (random_get) {
	 int wln = OTHER_WORDS.length;
	 int nextr = random_gen.nextInt(wln);
	 String wd = OTHER_WORDS[nextr];
	 int pno = random_gen.nextInt(MAX_PAGE);
	 String key = wd + pno;
	 if (done.contains(key)) continue;
	 done.add(key);
	 String what = SEARCH_FOR;
	 if (wd.length() > 0) what += " " + wd;
	 srcs = search_engine.getSpecificSourceSet(what,pno,null,null);
       }
      else {
	 if (random_gen.nextDouble() > PAGE_PROB) search_engine.skipNextSourceSet();
	 else {
	    srcs = search_engine.getNextSourceSet(SEARCH_FOR,null,null);
	  }
       }
      if (srcs != null) {
	 ++ctr;
	 for (TgenSource ts : srcs) {
	    if (random_gen.nextDouble() <= USE_PROB) {
	       checkSource(ts);
	     }
	  }
       }
    }
}



private void checkSource(TgenSource tsrc)
{
   String srctext = tsrc.getText();
   System.err.println("TGEN: RETRIEVED: " + srctext);

   String pkg = TgenOhloh.findPackageName(srctext);
   if (pkg == null || pkg.equals("") || pkg.startsWith("org.junit")) return;
   CompilationUnit cu = parseSourceFile(srctext);
   if (cu == null) return;
   System.err.println("TGEN: CHECKABLE");

   TestCheck tc = new TestCheck();
   cu.accept(tc);
   if (!tc.isViable()) return;
   System.err.println("TGEN: VIABLE");

   TgenProject tproj = new TgenProject(tsrc.getProjectName(),tsrc.getProjectId());
   List<JcompSource> srclist = new ArrayList<JcompSource>();
   srclist.add(tsrc);

   for (String s : tc.getUsedClasses()) {
      String srch = "\"class " + s + "\"";
      List<TgenSource> rsrcs = search_engine.getSourceSet(srch,tsrc,tproj);
      if (rsrcs.isEmpty()) {
	 srch = s + " \"package " + pkg + "\" " + s;
	 rsrcs = search_engine.getSourceSet(srch,tsrc,tproj);
       }
      if (rsrcs.isEmpty()) {
	 srch = s + " \"package " + pkg + "\"";
	 rsrcs = search_engine.getSourceSet(srch,tsrc,tproj);
       }
      boolean fnd = false;
      for (TgenSource usrc : rsrcs) {
	String cnm = usrc.getClassFileName();
	int idx = cnm.lastIndexOf(".");
	if (idx >= 0) cnm = cnm.substring(0,idx);
	if (cnm.equals(s)) {
	   System.err.println("TGEN: USE SOURCE " + usrc.getFileName());
	   srclist.add(usrc);
	   fnd = true;
	   break;
	 }
       }
      if (!fnd) {
	 for (TgenSource usrc : rsrcs) {
	    CompilationUnit ucu = parseSourceFile(usrc.getText());
	    if (ucu == null) continue;
	    for (Object o : ucu.types()) {
	       AbstractTypeDeclaration atd = (AbstractTypeDeclaration) o;
	       String tnm = atd.getName().getIdentifier();
	       System.err.println("TGEN: CHECK " + s + " " + tnm);
	       if (s.equals(tnm)) {
		  srclist.add(usrc);
		  fnd = true;
		  break;
		}
	     }
	    if (fnd) break;
	  }
       }
      if (!fnd) return;
    }

   MessageDigest md = null;
   try {
      md = MessageDigest.getInstance("SHA-1");
    }
   catch (NoSuchAlgorithmException e) { }

   int tcnt = 0;
   Counter ctr = new Counter();
   for (int i = 1; i < srclist.size(); ++i) {
      JcompSource src = srclist.get(i);
      TgenSource xsrc = (TgenSource) src;
      System.err.println("TGEN: CONSIDER: " + xsrc.getText());
      tcnt += lineCount(src);
      if (md != null) md.update(xsrc.getText().getBytes());
      CompilationUnit ucu = parseSourceFile(xsrc.getText());
      ucu.accept(ctr);
    }
   if (tcnt < 60) return;
   if (ctr.getNumLoops() == 0) return;
   if (md != null) {
      String key = Arrays.toString(md.digest());
      if (!items_done.add(key)) return;
    }

   JcompControl jctrl = new JcompControl();
   JcompProject jproj = jctrl.getProject("/pro/s6/lib/junit.jar",srclist);
   jproj.resolve();
   int nerr = 0;
   for (JcompMessage msg : jproj.getMessages()) {
      System.err.println("TGEN: MSG: " + msg.getText() + " at " + msg.getLineNumber() +
	    " (" + msg.getStartOffset() + ") in " + msg.getSource().getFileName());
      switch (msg.getSeverity()) {
	 case ERROR :
	 case FATAL :
	    ++nerr;
	    break;
	 default :
	    break;
       }
    }
   if (nerr > 0) return;

   System.err.println("TGEN: ACCEPT");
   setupDirectory(jctrl,pkg,srclist);
}





static CompilationUnit parseSourceFile(String text)
{
   if (text == null) return null;

   CompilationUnit cu = JcompAst.parseSourceFile(text);

   return cu;
}



private static int lineCount(JcompSource src)
{
   String txt = src.getFileContents();
   if (txt == null) return 0;
   int nline = 0;
   for (int i = 0; i < txt.length(); ++i) {
      if (txt.charAt(i) == '\n') ++nline;
    }
   return nline;
}




/********************************************************************************/
/*										*/
/*	Output, run ant, see if test is okay					*/
/*										*/
/********************************************************************************/

private void setupDirectory(JcompControl jctrl,String pkg,List<JcompSource> srclist)
{
   Map<String,String> idmap = new HashMap<String,String>();
   idmap.put("JUNITCP",TGEN_JUNIT_CLASSPATH);

   File root = new File(TGEN_DIRECTORY);
   if (!root.exists() && !root.mkdirs()) {
      System.err.println("TGEN: Problem creating output directory");
      System.exit(1);
    }
   idmap.put("ROOT",root.getPath());

   String npkg = null;
   Random r = new Random();
   int dirno = 0;
   File dir = null;
   for (int i = 0; i < 1000; ++i) {
      dirno = r.nextInt(131256);
      npkg = TGEN_PACKAGE_PREFIX + dirno;
      dir = new File(root.getPath() + File.separator + npkg);
      if (dir.exists()) continue;
      if (dir.mkdir()) break;
      dir = null;
    }
   if (dir == null) {
      System.err.println("TGEN: test directory not created");
      System.exit(1);
    }
   idmap.put("DIRECTORY",dir.getPath());
   idmap.put("PACKAGE",npkg);
   idmap.put("PROJECTNAME",npkg);
   idmap.put("DIRNO",Integer.toString(dirno));
   idmap.put("ORIGPACKAGE",pkg);

   File bin = new File(dir,TGEN_BINARY_DIR);
   if (!bin.mkdir()) {
      System.err.println("Problem creating binary subdirectory: " +bin);
    }
   idmap.put("BIN",bin.getPath());

   TgenSource usrc = null;
   for (JcompSource jsrc : srclist) {
      TgenSource tsrc = (TgenSource) jsrc;
      if (idmap.get("TESTCLASS") == null) {
	 String cnm = tsrc.getClassFileName();
	 int idx = cnm.lastIndexOf(".");
	 cnm = cnm.substring(0,idx);
	 idmap.put("TESTCLASS",npkg + "." + cnm);
       }
      else if (idmap.get("USERCLASS") == null) {
	 usrc = tsrc;
	 String cnm = tsrc.getClassFileName();
	 int idx = cnm.lastIndexOf(".");
	 cnm = cnm.substring(0,idx);
	 idmap.put("USERCLASS",cnm);
       }
      File tfil = new File(dir,tsrc.getClassFileName());
      try {
	 FileWriter fw = new FileWriter(tfil);
	 String text = tsrc.getText();
	 text = text.replace(pkg,npkg);
	 fw.write(text);
	 fw.close();
       }
      catch (IOException e) {
	 System.err.println("TGEN: Problem writing output file: " + e);
	 System.exit(1);
       }
    }

   String exp = IvyFile.expandText(ant_template,idmap);
   try {
      File afile = new File(dir,"build.xml");
      FileWriter fw = new FileWriter(afile);
      fw.write(exp);
      fw.close();
    }
   catch (IOException e) {
      System.err.println("TGEN: Problem writing ant file: " + e);
      System.exit(1);
    }

   String cmd = TGEN_ANT_COMMAND + " " + dir.getPath();
   int fgs = IvyExec.ERROR_OUTPUT;
   try {
      IvyExec ex = new IvyExec(cmd,fgs);
      ex.waitFor();
    }
   catch (IOException e) {
      System.err.println("TGEN: Problem running ant: " + e);
      return;
    }

   File rslt = new File(dir,"test.out.xml");
   Element telt = IvyXml.loadXmlFromFile(rslt);
   int ntest = 0;
   int nerr = 0;
   int nfail = 0;
   int nskip = 0;
   if (telt != null) {
      nerr = IvyXml.getAttrInt(telt,"errors");
      nfail = IvyXml.getAttrInt(telt,"failures");
      nskip = IvyXml.getAttrInt(telt,"skipped");
      ntest = IvyXml.getAttrInt(telt,"tests");
    }
   if (nerr + nfail > 0 || nskip == ntest || ntest == 0) {
      System.err.println("REJECT " + nerr + " " + nfail + " " + nskip + " " + ntest);
      try {
	 IvyFile.remove(dir);
       }
      catch (IOException e) {
	 System.err.println("TGEN: Problem trying to remove directory: " + e);
       }
      return;
    }

   try {
      outputS6File(jctrl,idmap,usrc);
    }
   catch (IOException e) {
      System.err.println("TGEN: Problem setting up S6 file");
      return;
    }

   File run1 = new File(dir,"runit.csh");
   File run2 = new File(dir,"runfinal.csh");
   try {
      PrintWriter pw1 = new PrintWriter(new FileWriter(run1));
      PrintWriter pw2 = new PrintWriter(new FileWriter(run2));
      pw1.println("#! /bin/csh -f");
      pw2.println("#! /bin/csh -f");
      pw1.print("s6java -Djava.io.tmpdir=/ws/volfred/tmp -d64 -Xmx32000m ");
      pw2.print("(time s6java -Djava.io.tmpdir=/ws/volfred/tmp -d64 -Xmx32000m ");
      pw1.print("edu.brown.cs.s6.engine.EngineMain -D -thread 1 ");
      pw2.print("edu.brown.cs.s6.engine.EngineMain -thread 8 ");
      pw1.print("stest" + idmap.get("DIRNO") + ".s6 ");
      pw2.print("stest" + idmap.get("DIRNO") + ".s6)");
      pw1.println(" >&! s6.debug");
      pw2.println(" >&! s6.mtout");
      pw1.close();
      pw2.close();
    }
   catch (IOException e) {
      System.err.println("TGEN: Problem setting up run files");
    }
   run1.setExecutable(true);
   run2.setExecutable(true);

   System.err.println("TGEN: SUCCESS " + (ntest-nskip) + "/" + ntest + " " + dir);
}



private void outputS6File(JcompControl jctrl,Map<String,String> idmap,TgenSource tsrc) throws IOException
{
   File dir = new File(idmap.get("DIRECTORY"));
   File otf = new File(dir,"stest" + idmap.get("DIRNO") + ".s6");
   IvyXmlWriter xw = new IvyXmlWriter(otf);
   xw.begin("SEARCH");
   xw.field("WHAT","TESTCASES");
   xw.field("FORMAT","NONE");
   xw.field("LOCAL",false);
   xw.field("REMOTE",true);
   xw.field("OHLOH",false);
   xw.field("GITHUB",true);
   xw.begin("SIGNATURE");
   xw.begin("TESTING");
   xw.field("PACKAGE",idmap.get("PACKAGE"));
   xw.field("NAME","S6Test" + idmap.get("USERCLASS"));
   xw.begin("TESTEE");
   xw.field("PACKAGE",idmap.get("PACKAGE"));

   xw.begin("CLASS");
   xw.field("NAME",idmap.get("USERCLASS"));

   TgenSource nsrc = tsrc.getPackageSource(idmap.get("ORIGPACKAGE"),idmap.get("PACKAGE"));

   List<JcompSource> srcs = new ArrayList<JcompSource>();
   srcs.add(nsrc);
   JcompProject jproj = jctrl.getProject(srcs);
   jproj.resolve();
   JcompSemantics jsd = jctrl.getSemanticData(nsrc);
   CompilationUnit cu = (CompilationUnit) jsd.getAstNode();
   SignVisitor sv = new SignVisitor(xw,cu);
   cu.accept(sv);
   xw.end("CLASS");

   xw.end("TESTEE");
   xw.end("TESTING");
   xw.end("SIGNATURE");

   xw.begin("KEYWORDS");

   String key = idmap.get("USERCLASS");
   String [] keys = keyword_map.get(key);
   if (keys != null) {
      for (String s : keys) {
	 xw.textElement("KEYWORD",s);
       }
    }
   else {
      SearchWordFactory swf = SearchWordFactory.getFactory();
      swf.clear();
      swf.loadSource(sv.getText(),true);
      // swf.loadSource(tsrc.getText(),true);
      List<String> q = swf.getQuery();
      int ct = 0;
      for (int i = 0; ct < 3 && i < q.size(); ++i) {
	 String wd = q.get(i);
	 boolean dup = false;
	 for (int j = 0; j < i; ++j) {
	    String wd1 = q.get(j);
	    if (wd1.startsWith(wd) || wd.startsWith(wd1)) dup = true;
	  }
	 if (dup) continue;
	 xw.textElement("KEYWORD",q.get(i));
	 ++ct;
       }
    }
   xw.textElement("KEYWORD","test");
   xw.textElement("KEYWORD","org.junit");
   xw.end("KEYWORDS");

   File ctxf = new File(dir,"context.s6ctx");
   String args = "/pro/s6/bin/s6java";
   args += " edu.brown.cs.s6.context.ContextMain";
   args += " -classpath " + idmap.get("DIRECTORY") + "/bin:/pro/s6/lib/junit.jar";
   args += " -output " + ctxf.getPath();

   IvyExec ex = new IvyExec(args);
   int sts = ex.waitFor();
   if (sts != 0) throw new IOException("problem building context " + sts);

   xw.begin("CONTEXT");
   xw.field("FILE",ctxf.getPath());
   xw.end("CONTEXT");

   xw.end("SEARCH");
   xw.close();
}




private static class SignVisitor extends ASTVisitor {

   private IvyXmlWriter xml_writer;
   CompilationUnit root_node;
   private StringBuffer signature_text;

   SignVisitor(IvyXmlWriter xw,CompilationUnit cu) {
      xml_writer = xw;
      root_node = cu;
      signature_text = new StringBuffer();
    }

   String getText()			{ return signature_text.toString(); }

   @Override public boolean visit(MethodDeclaration md) {
      if (md.getParent().getParent() != root_node) return false;	// must be top-level
      if (!Modifier.isPublic(md.getModifiers())) return false;		// must be public
      JcompType jtyp = JcompAst.getJavaType(md);
      if (jtyp == null) return false;

      String nm = md.getName().getIdentifier();
      if (md.isConstructor()) nm = "<init>";
      if (nm.equals("equals") || nm.equals("toString") || nm.equals("main"))
	 return false;

      xml_writer.begin("METHOD");
      xml_writer.field("NAME",nm);
      xml_writer.field("SIGNATURE",jtyp.getJavaTypeName());
      if (Modifier.isStatic(md.getModifiers()))
	 xml_writer.field("STATIC",true);
      xml_writer.end("METHOD");

      if (!md.isConstructor()) {
	 signature_text.append(md.getName().getIdentifier());
	 signature_text.append(" ");
       }
      if (md.getReturnType2() != null) {
	 signature_text.append(md.getReturnType2().toString());
	 signature_text.append(" ");
       }
      for (Object o : md.parameters()) {
	 SingleVariableDeclaration svd = (SingleVariableDeclaration) o;
	 signature_text.append(svd.getName().getIdentifier());
	 signature_text.append(" ");
	 signature_text.append(svd.getType().toString());
	 signature_text.append(" ");
       }
      signature_text.append("\n");

      return false;
    }

   @Override public boolean visit(TypeDeclaration td) {
      if (td.getParent().getNodeType() == ASTNode.COMPILATION_UNIT) {
	 if (Modifier.isPublic(td.getModifiers())) {
	    signature_text.append(td.getName().getIdentifier());
	    if (td.getSuperclassType() != null) {
	       signature_text.append(" ");
	       signature_text.append(td.getSuperclassType().toString());
	     }
	    for (Object o : td.superInterfaceTypes()) {
	       signature_text.append(" ");
	       signature_text.append(o.toString());
	     }
	    signature_text.append("\n");
	  }
       }
      return true;
    }

}	// end of inner class SignVisitor




/********************************************************************************/
/*										*/
/*	Visitor to check suitability of AST for testings			*/
/*										*/
/********************************************************************************/

private static class TestCheck extends ASTVisitor {

   private String package_name;
   private Set<String> class_names;
   private Set<String> used_classes;
   private boolean test_annotation;
   private Set<String> import_set;
   private Set<String> import_check;
   private Map<String,String> import_type;
   private Set<String> known_names;
   private boolean have_assert;

   TestCheck() {
      package_name = null;
      class_names = new HashSet<String>();
      used_classes = new HashSet<String>();
      import_set = new HashSet<String>();
      import_check = new HashSet<String>();
      import_check.add("java.lang");
      import_type = new HashMap<String,String>();
      known_names = new HashSet<String>();
      test_annotation = false;
      have_assert = false;
    }

   boolean isViable() {
      if (package_name == null) return false;
      if (!test_annotation) return false;
      if (used_classes.size() != 1) return false;
      if (!have_assert) return false;
      return true;
    }

   Set<String> getUsedClasses() 			{ return used_classes; }

   @Override public boolean visit(TypeDeclaration td) {
      String nm = td.getName().getIdentifier();
      class_names.add(nm);
      used_classes.remove(nm);
      return true;
    }

   @Override public boolean visit(EnumDeclaration td) {
      String nm = td.getName().getIdentifier();
      class_names.add(nm);
      used_classes.remove(nm);
      return true;
    }


   @Override public void endVisit(ImportDeclaration n) {
      String nm = n.getName().getFullyQualifiedName();
      if (!n.isOnDemand()) {
	 int idx = nm.lastIndexOf(".");
	 if (idx < 0) return;
	 import_type.put(nm.substring(idx+1),nm);
	 nm = nm.substring(0,idx);
       }
      else {
	 import_check.add(nm);
       }
      if (isStandardName(nm)) return;
      import_set.add(nm);
    }

   @Override public void endVisit(PackageDeclaration n) {
      package_name = n.getName().getFullyQualifiedName();
    }

   @Override public void endVisit(ClassInstanceCreation n) {
      String tnm = getTypeName(n.getType());
      if (tnm == null || isStandardName(tnm)) return;
      if (isKnownName(tnm)) return;
      used_classes.add(tnm);
    }

   @Override public void endVisit(MethodInvocation n) {
      if (n.getName().getIdentifier().startsWith("assert")) have_assert = true;
      if (n.getExpression() == null) return;
      if (n.getExpression() instanceof Name) {
	 String nm = ((Name) n.getExpression()).getFullyQualifiedName();
	 if (recordName(nm)) used_classes.add(nm);
       }
    }

   @Override public void endVisit(NormalAnnotation n) {
      checkAnnotation(n);
    }

   @Override public void endVisit(MarkerAnnotation n) {
      checkAnnotation(n);
    }

   @Override public void endVisit(SingleVariableDeclaration n) {
      known_names.add(n.getName().getIdentifier());
    }

   @Override public void endVisit(VariableDeclarationFragment n) {
      known_names.add(n.getName().getIdentifier());
    }

   private boolean isStandardName(String nm) {
      for (int i = 0; i < STD_PREFIX.length; ++i) {
         if (nm.startsWith(STD_PREFIX[i])) return true;
       }
      return false;
    }

   private String getTypeName(Type t) {
      String tnm = null;
      if (t.isArrayType()) t = ((ArrayType) t).getElementType();
      if (t.isParameterizedType()) t = ((ParameterizedType) t).getType();
      if (t.isPrimitiveType()) ;
      else if (t.isUnionType() || t.isWildcardType()) ;
      else if (t.isSimpleType()) tnm = ((SimpleType) t).getName().getFullyQualifiedName();
      else if (t.isQualifiedType()) {
         QualifiedType qt = (QualifiedType) t;
         tnm = getTypeName(qt.getQualifier());
         if (tnm != null) tnm += "." + qt.getName().getFullyQualifiedName();
       }
      return tnm;
    }

   private boolean isKnownName(String nm) {
      if (import_type.get(nm) != null) return true;
      if (class_names.contains(nm)) return true;
      for (String s : import_check) {
         String cls = s + "." + nm;
         try {
            Class<?> c = Class.forName(cls);
            if (c != null) return true;
          }
         catch (Throwable e) { }
       }
      return false;
    }

   private boolean recordName(String nm) {
      if (known_names.contains(nm)) return false;
      if (class_names.contains(nm)) return false;
      if (isStandardName(nm) || isKnownName(nm)) return false;
      int idx = nm.lastIndexOf(".");
      if (idx > 0) return recordName(nm.substring(0,idx));
      return true;
    }

   private void checkAnnotation(Annotation n) {
      if (n.getParent() instanceof MethodDeclaration) {
	 MethodDeclaration md = (MethodDeclaration) n.getParent();
	 if (md.getBody() == null) return;
	 if (md.getBody().statements().size() == 0) return;
       }
      String nm = n.getTypeName().getFullyQualifiedName();
      if (nm.equals("org.junit.Test") || nm.equals("Test")) test_annotation = true;
    }
}	// end of inner class TestCheck




/********************************************************************************/
/*										*/
/*	Counter visitor to check complexity					*/
/*										*/
/********************************************************************************/

private static class Counter extends ASTVisitor {

   private int num_loops;

   Counter() {
      num_loops = 0;
    }

   int getNumLoops()		{ return num_loops; }

   @Override public void endVisit(ForStatement n) {
      ++num_loops;
    }

   @Override public void endVisit(DoStatement n) {
      ++num_loops;
    }

   @Override public void endVisit(WhileStatement n) {
      ++num_loops;
    }

   @Override public void endVisit(EnhancedForStatement n) {
      ++num_loops;
    }

}	// end of inner class Counter


/********************************************************************************/
/*										*/
/*	Read keywords from file 						*/
/*										*/
/********************************************************************************/

private void readKeywords()
{
   try (BufferedReader br = new BufferedReader(new FileReader(TGEN_KEYWORDS))) {
      List<String> rslt = new ArrayList<String>();
      String key = null;
      for ( ; ; ) {
	 String ln = br.readLine();
	 if (ln == null) break;
	 ln = ln.trim();
	 if (ln.equals("")) {
	    if (key != null && rslt.size() > 0) {
	       String [] arr = new String[rslt.size()];
	       arr = rslt.toArray(arr);
	       keyword_map.put(key,arr);
	       key = null;
	       rslt.clear();
	       continue;
	     }
	  }
	 else if (key == null) key = ln;
	 else rslt.add(ln);
       }
      if (key != null && rslt.size() > 0) {
	 String [] arr = new String[rslt.size()];
	 arr = rslt.toArray(arr);
	 keyword_map.put(key,arr);
       }
    }
   catch (IOException e) {
      System.err.println("TGEN: Problem reading keyword file: " + e);
    }
}




}	// end of class TgenMain




/* end of TgenMain.java */

