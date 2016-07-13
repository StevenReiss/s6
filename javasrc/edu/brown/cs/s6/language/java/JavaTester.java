/********************************************************************************/
/*										*/
/*		JavaTester.java 						*/
/*										*/
/*	Class to handle creating and running a test case for a Java fragment	*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/language/java/JavaTester.java,v 1.17 2016/01/14 17:03:29 spr Exp $ */


/*********************************************************************************
 *
 * $Log: JavaTester.java,v $
 * Revision 1.17  2016/01/14 17:03:29  spr
 * Fix up testing for android.	Fix bug in addreturn transform.
 *
 * Revision 1.16  2015/12/23 15:45:09  spr
 * Minor fixes.
 *
 * Revision 1.15  2015/09/23 17:54:52  spr
 * Version to handle andriod UI applications.
 *
 * Revision 1.14  2014/08/29 15:16:08  spr
 * Updates for suise, testcases.
 *
 * Revision 1.13  2013/09/13 20:33:04  spr
 * Add calls for UI search.
 *
 * Revision 1.12  2013-05-09 12:26:20  spr
 * Minor changes to start ui fixups.
 *
 * Revision 1.11  2012-07-20 22:15:20  spr
 * New transforms and resolution for UI search
 *
 * Revision 1.10  2012-06-20 12:21:33  spr
 * Initial fixes for UI search
 *
 * Revision 1.9  2012-06-11 18:18:27  spr
 * Include changed/new files for package/ui search
 *
 * Revision 1.8  2012-06-11 14:07:49  spr
 * add framework search; fix bugs
 *
 * Revision 1.7  2009-09-18 01:41:35  spr
 * Handle user testing.
 *
 * Revision 1.6  2009-05-12 22:28:48  spr
 * Fix ups to make user context work.
 *
 * Revision 1.5  2008-11-12 13:52:14  spr
 * Performance and bug updates.
 *
 * Revision 1.4  2008-08-28 00:32:56  spr
 * Next version of S6.	Lots of bug fixes, some new functionality.
 *
 * Revision 1.3  2008-07-17 13:46:45  spr
 * Bug fixes and speed ups.
 *
 * Revision 1.2  2008-06-12 17:47:52  spr
 * Next version of S6.
 *
 * Revision 1.1.1.1  2008-06-03 12:59:23  spr
 * Initial version of S6
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.language.java;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.nio.charset.Charset;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import org.w3c.dom.*;

import edu.brown.cs.ivy.exec.IvyExec;
import edu.brown.cs.ivy.file.IvyFile;
import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Context;
import edu.brown.cs.s6.common.S6Exception;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6Resource;
import edu.brown.cs.s6.common.S6Security;
import edu.brown.cs.s6.common.S6Source;
import edu.brown.cs.s6.common.S6TestCase;
import edu.brown.cs.s6.common.S6TestResults;


class JavaTester implements S6Constants, JavaConstants {



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private FragmentJava		java_fragment;
private S6Request.Search	for_request;
private JavaContext		user_context;
private S6Source		for_source;

private static Pattern ERROR_PATTERN = Pattern.compile("^Result of call expected:<(.*)> but was:<(.*)>$");



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

JavaTester(FragmentJava f,S6Request.Search r,JavaContext ctx,S6Source src)
{
   java_fragment = f;
   for_request = r;
   user_context = ctx;
   for_source = src;
}



/********************************************************************************/
/*										*/
/*	Methods to create and run the test					*/
/*										*/
/********************************************************************************/

SuiteReport run()
{
   Map<String,String> idmap = new HashMap<String,String>();

   try {
      return runJunitTest(idmap);
    }
   catch (S6Exception e) {
      if (for_request.doDebug()) System.err.println("S6: TESTER: Problem running test: " + e);
    }

   return null;
}



/********************************************************************************/
/*										*/
/*	Methods to generate test code						*/
/*										*/
/********************************************************************************/

private SuiteReport runJunitTest(Map<String,String> idmap) throws S6Exception
{
   idmap.put("CLASS",S6_TEST_CLASS);
   idmap.put("SOURCEFILE",S6_TEST_CLASS + ".java");
   idmap.put("JUNITCP",JUNIT_CLASSPATH);
   idmap.put("JUNIT",JUNIT_RUNNER);
   idmap.put("JUNITOUT",JUNIT_OUT);
   idmap.put("TESTCLASS",S6_USER_CLASS);
   idmap.put("UIJAR","runuserinterface.jar");
   idmap.put("S6CLS",IvyFile.expandName("$(S6)/java"));
   idmap.put("IVY",IvyFile.expandName("$(IVY)/java"));
   idmap.put("S6BIN",IvyFile.expandName("$(S6)/bin"));
   idmap.put("S6LIB",IvyFile.expandName("$(S6)/lib"));
   idmap.put("S6SOURCE",for_source.getName());

   idmap.put("ANDROIDJARS",IvyFile.expandName("$(IVY)/lib/androidjar"));
   idmap.put("JMLCP",IvyFile.expandName("$(S6)/lib/jmlruntime.jar"));
   idmap.put("ANTRUN","test");
   idmap.put("MAXTIME","10000L");
   idmap.put("SHARED_EXT","so");
   if (System.getProperty("os.name").startsWith("Mac")) idmap.put("SHARED_EXT","dynlib");
   idmap.put("SETUP","");

   setupUserContext(idmap);

   String clsname = idmap.get("TESTCLASS");
   String pkgfix = null;
   S6Request.PackageSignature psg = null;
   switch (for_request.getSearchType()) {
      case METHOD :
	 break;
      case FULLCLASS :
      case CLASS :
	 clsname = for_request.getSignature().getName();
	 java_fragment.fixupAst();	// ensures we can do rename
	 break;
      case PACKAGE :
      case APPLICATION :
	 psg = (S6Request.PackageSignature) for_request.getSignature();
	 pkgfix = psg.getName();
	 break;
      case UIFRAMEWORK :
	 psg = (S6Request.PackageSignature) for_request.getSignature();
	 pkgfix = psg.getName();
	 setupUIHierarchy(idmap);
	 break;
      case ANDROIDUI :
	 idmap.put("ANTRUN","testandroid");
	 psg = (S6Request.PackageSignature) for_request.getSignature();
	 pkgfix = psg.getName();
	 setupUIHierarchy(idmap);
	 break;
      case TESTCASES :
	 clsname = for_request.getSignature().getClassSignature().getName();
	 java_fragment.fixupAst();
	 setupTesting(idmap);
	 break;
    }
   idmap.put("PREFIX",clsname);

   setupTestPackage(idmap);

   StringBuffer imports = new StringBuffer();
   for (JcompType jt : java_fragment.getImportTypes()) {
      imports.append("import " + jt.getName() + ";\n");
    }
   for (String s : for_request.getTests().getImportTypes()) {
      imports.append("import " + s + ";\n");
    }
   if (user_context != null) {
      for (String s : user_context.getContextImports()) {
	 imports.append("import " + s + ";\n");
       }
    }
   idmap.put("IMPORTS",imports.toString());

   setupSecurity(idmap);

   setupTests(idmap);

   JavaAstClassName cn = null;
   if (pkgfix != null) {
      JavaAst.mapPackageNames(java_fragment.getAstNode(),pkgfix,idmap.get("PACKAGE"));
    }
   else {
      cn = java_fragment.getClassNamer();
      if (cn != null) {
	 String cnm = idmap.get("PACKAGEDOT") + clsname;
	 cn.setClassName(cnm,clsname);
       }
    }

   setupCode(idmap);

   setupSourceFile(idmap);

   try {
      produceTestFile(idmap);
      produceSourceFile(idmap);
      producePackageFiles(idmap);
      produceAntFile(idmap);		// should be last
      compileAndRunTestFile(idmap);
      SuiteReport sr = readTestStatus(idmap);
      // test passes -- set final class name
      // should use name in request
      if (cn != null) cn.setClassName(idmap.get("PACKAGEDOT") + S6_TEST_CLASS,clsname);
      return sr;
    }
   finally {
      if (pkgfix != null) {
	 JavaAst.mapPackageNames(java_fragment.getAstNode(),idmap.get("PACKAGE"),pkgfix);
       }
      else {
	 if (cn != null) cn.resetClassName();
       }
      clear(idmap);
      java_fragment.clearResolve();
    }
}





private void setupUserContext(Map<String,String> idmap) throws S6Exception
{
   if (user_context == null) return;

   String s = for_request.getPackage();
   if (s == null) s = user_context.getContextPackage();
   if (s != null) idmap.put("PACKAGE",s);
   else idmap.put("PACKAGE","");

   String cls = user_context.getContextClass();
   if (cls != null) {
      String fcls;
      fcls = cls;
      idmap.put("TESTCLASS",fcls);
      idmap.put("PREFIX",fcls);
    }

   String jnm = user_context.getJarFileName();
   if (jnm != null) idmap.put("S6CTX",jnm);

   File cdir = user_context.getContextDirectory();
   if (cdir == null) return;

   StringBuffer buf = new StringBuffer();
   StringBuffer ebuf = new StringBuffer();
   for (S6Context.UserFile uf : user_context.getUserFiles()) {
      String nm = uf.getLocalName();
      File cfl = new File(cdir,nm);
      String unm = uf.getUserName();
      ebuf.append(nm);
      ebuf.append(">");
      ebuf.append(unm);
      ebuf.append(">");
      switch (uf.getFileType()) {
	 case READ :
	    ebuf.append("R");
	    buf.append("<exec executable='ln'><arg value='-s' /><arg value='");
	    buf.append(cfl.getPath());
	    buf.append("' /></exec>\n");
	    break;
	 case WRITE :
	    ebuf.append("W");
	    if (cfl.exists()) {
	       buf.append("<copy file='");
	       buf.append(cfl.getPath());
	       buf.append("' todir='.' />");
	     }
	    break;
	 case DIRECTORY :
	    ebuf.append("D");
	    break;
       }
      ebuf.append("&");
    }
   idmap.put("CONTEXT_ANT",buf.toString());
   idmap.put("S6_CONTEXT_MAP",ebuf.toString());
   // System.err.println("CONTEXT SETUP: ANT = " + buf.toString());
   // System.err.println("CONTEXT SETUP: S6 = " + ebuf.toString());
}




private void setupTestPackage(Map<String,String> idmap) throws S6Exception
{
   File root = new File(System.getProperty("java.io.tmpdir") + File.separator + S6_TEST_DIR);
   if (!root.exists() && !root.mkdir())
      throw new S6Exception("Can't create S6 test directory: " + root);
   idmap.put("ROOT",root.getPath());

   String pkg = null;
   Random r = new Random();
   File dir = null;
   for (int i = 0; i < 1000; ++i) {
      pkg = S6_PACKAGE_PREFIX + r.nextInt(131256);
      dir = new File(root.getPath() + File.separator + pkg);
      if (dir.exists()) continue;
      if (dir.mkdir()) break;
      dir = null;
    }

   if (dir == null) throw new S6Exception("S6 test directory not created");

   idmap.put("DIRECTORY",dir.getPath());
   if (idmap.get("PACKAGE") == null) idmap.put("PACKAGE",pkg);
   idmap.put("SRCDIR",dir.getPath());
   idmap.put("PROJECTNAME",pkg);

   File sf1 = new File(dir,"S6SOURCE");
   try {
      PrintWriter fw = new PrintWriter(new FileWriter(sf1));
      try {
	 fw.println(for_source.getDisplayName());
	 fw.println(for_source.getName());
	 fw.println(for_source.getProjectId());
       }
      catch (Throwable t) { }
      fw.close();
    }
   catch (IOException e) { }

   File bin = new File(dir,S6_BINARY_DIR);
   if (for_request.getSearchType() == S6SearchType.ANDROIDUI) {
      File f1 = new File(dir,"src");
      File f2 = new File(f1,"s6");
      File f3 = new File(f2,pkg);
      idmap.put("SRCDIR",f3.getPath());
      if (!f3.mkdirs()) {
	 System.err.println("Problem creating source subdirectory: " + f3);
       }
      idmap.put("PACKAGE","s6." + pkg);
      bin = new File(bin,"classes");
    }

   if (!bin.mkdirs()) {
      System.err.println("Problem creating binary subdirectory: " + bin);
    }
   idmap.put("BIN",bin.getPath());

   String npkg = idmap.get("PACKAGE");
   if (npkg == null || npkg.equals("*") || npkg.equals("?") || npkg.equals("")) npkg = null;
   if (npkg != null) {
      idmap.put("PACKAGESTMT","package " + npkg + ";\n");
      idmap.put("PACKAGEDOT",npkg + ".");
    }
}



private void produceTestFile(Map<String,String> idmap) throws S6Exception
{
   String dir = idmap.get("SRCDIR");
   File f = new File(dir + File.separator + S6_TEST_CLASS + ".java");

   try {
      BufferedReader fr = new BufferedReader(new FileReader(JAVA_TEST_PROTO));
      PrintWriter pw = new PrintWriter(new FileWriter(f));
      for ( ; ; ) {
	 String ln = fr.readLine();
	 if (ln == null) break;
	 ln = IvyFile.expandName(ln,idmap);
	 pw.println(ln);
       }
      pw.close();
      fr.close();
    }
   catch (IOException e) {
      throw new S6Exception("Problem creating test file: " + e);
    }
}



private void produceSourceFile(Map<String,String> idmap) throws S6Exception
{
   String cls = idmap.get("SOURCECLASS");
   if (cls == null) return;

   String dir = idmap.get("SRCDIR");
   File f = new File(dir,cls + ".java");

   try {
      FileWriter fw = new FileWriter(f);
      fw.write(idmap.get("SOURCECODE"));
      fw.close();
    }
   catch (IOException e) {
      throw new S6Exception("Problem creating source file: " + e);
    }
}



private void producePackageFiles(Map<String,String> idmap) throws S6Exception
{
   switch (for_request.getSearchType()) {
      case PACKAGE :
      case APPLICATION :
	 break;
      case UIFRAMEWORK :
      case ANDROIDUI :
	 break;
      default :
	 return;
    }

   CompilationUnit cu = (CompilationUnit) java_fragment.getAstNode();
   for (Object o : cu.types()) {
      AbstractTypeDeclaration td = (AbstractTypeDeclaration) o;
      String cnm = td.getName().getIdentifier();
      String dir = idmap.get("SRCDIR");
      File f = new File(dir,cnm + ".java");
      try {
	 FileWriter fw = new FileWriter(f);
	 PrintWriter pw = new PrintWriter(fw);
	 pw.println(idmap.get("PACKAGESTMT"));
	 /**********
	 for (Object ox : cu.imports()) {
	    ImportDeclaration id = (ImportDeclaration) ox;
	    pw.println(id.toString());
	  }
	 *********/
	 pw.println(idmap.get("IMPORTS"));
	 pw.println();
	 pw.write(td.toString());
	 pw.println();
	 pw.println("// end of " + cnm + ".java");
	 fw.close();
       }
      catch (IOException e) {
	 throw new S6Exception("Problem creating package file for " + cnm + ": " + e);
       }
    }

   if (java_fragment.getResources() != null) {
      for (S6Resource rsrc : java_fragment.getResources()) {
	 String pnm = rsrc.getPathName();
	 String dir = idmap.get("DIRECTORY");
	 File f = new File(dir);
	 StringTokenizer tok = new StringTokenizer(pnm,"/");
	 while (tok.hasMoreTokens()) {
	    String path = tok.nextToken();
	    f.mkdir();
	    f = new File(f,path);
	  }
	 byte [] output = rsrc.getContents();
	 output = fixupResourceFile(output,f,idmap);
	 if (f != null) {
	    try {
	       FileOutputStream fw = new FileOutputStream(f);
	       fw.write(output);
	       fw.close();
	     }
	    catch (IOException e) {
	       throw new S6Exception("Problem creating resource file for " + pnm + ": " + e);
	     }
	  }
       }
    }
}



private void produceAntFile(Map<String,String> idmap) throws S6Exception
{
   String dir = idmap.get("DIRECTORY");
   File f = new File(dir + File.separator + ANT_FILE);

   try {
      String proto = JAVA_ANT_PROTO;
      if (for_request.getSearchType() == S6SearchType.ANDROIDUI) proto = JAVA_ANDROID_ANT_PROTO;
      BufferedReader fr = new BufferedReader(new FileReader(proto));
      PrintWriter pw = new PrintWriter(new FileWriter(f));
      for ( ; ; ) {
	 String ln = fr.readLine();
	 if (ln == null) break;
	 ln = IvyFile.expandName(ln,idmap);
	 pw.println(ln);
       }
      pw.close();
      fr.close();
    }
   catch (IOException e) {
      throw new S6Exception("Problem creating ant file: " + e);
    }
}




/********************************************************************************/
/*										*/
/*	Handle converting android resource files				*/
/*										*/
/********************************************************************************/

private byte[] fixupResourceFile(byte[] cnts,File f,Map<String,String> idmap)
{
   if (for_request.getSearchType() != S6SearchType.ANDROIDUI) return cnts;
   if (f == null) return cnts;
   String ext = f.getName();
   int idx = ext.lastIndexOf(".");
   if (idx < 0) return cnts;
   ext = ext.substring(idx);
   switch (ext) {
      case ".xml" :
	 String tcnts = null;
	 try {
	    tcnts = new String(cnts,"UTF-8");
	  }
	 catch (UnsupportedEncodingException e) {
	    tcnts = new String(cnts);
	  }
	 if (tcnts.startsWith("?")) tcnts = tcnts.substring(1);
	 Element xml = IvyXml.convertStringToXml(tcnts);
	 if (xml == null) {
	    System.err.println("BAD RESOURCE FILE: " + f + ": " + tcnts);
	    break;
	  }
	 if (f.getName().equals("AndroidManifest.xml")) cnts = fixupManifest(tcnts,xml,idmap);
	 else cnts = fixupXmlResource(cnts,xml,idmap);
	 break;
      default :
	 break;
    }

   return cnts;
}


private byte [] fixupManifest(String cnts,Element xml,Map<String,String> idmap)
{
   String pkg = IvyXml.getAttrString(xml,"package");
   if (pkg != null) idmap.put("ORIGPACKAGE",pkg);

   String main = null;
   Map<String,String> replaces = new HashMap<String,String>();
   String launch = null;

   boolean haveperm = false;
   for (Element perm : IvyXml.children(xml,"uses-permission")) {
      String nm = IvyXml.getAttrString(perm,"android:name");
      if (nm.equals("android.permission.WRITE_EXTERNAL_STORAGE")) haveperm = true;
    }
   if (!haveperm) {
      Document doc = (Document) xml.getParentNode();
      Element x = doc.createElement("uses-permission");
      x.setAttribute("android:name","android.permission.WRITE_EXTERNAL_STORAGE");
      for (Element app : IvyXml.children(xml,"application")) {
	 xml.insertBefore(x,app);
	 break;
       }
    }

   for (Element app : IvyXml.children(xml,"application")) {
      String nm2 = IvyXml.getAttrString(app,"android:name");
      if (nm2 == null) nm2 = IvyXml.getAttrString(app,"name");
      if (nm2 != null && nm2.startsWith(".")) {
	 int idx2 = nm2.lastIndexOf(".");
	 if (idx2 > 1) {
	    String nm3 = nm2.substring(idx2);
	    replaces.put(nm2,nm3);
	  }
       }
      for (Element act : IvyXml.children(app,"activity")) {
	 boolean ismain = false;
	 boolean islaunch = false;
	 for (Element intent : IvyXml.children(act,"intent-filter")) {
	    for (Element action : IvyXml.children(intent,"action")) {
	       String nm = IvyXml.getAttrString(action,"android:name");
	       if (nm == null) nm = IvyXml.getAttrString(action,"name");
	       if (nm != null && nm.equals("android.intent.action.MAIN")) {
		  ismain = true;
		}
	     }
	    for (Element categ : IvyXml.children(intent,"category")) {
	       String nm = IvyXml.getAttrString(categ,"android:name");
	       if (nm == null) nm = IvyXml.getAttrString(categ,"name");
	       if (nm != null && nm.equals("android.intent.category.LAUNCHER")) {
		  islaunch = true;
		}
	     }
	  }
	 String nm1 = IvyXml.getAttrString(act,"android:name");
	 if (nm1 == null) nm1 = IvyXml.getAttrString(act,"name");
	 if (nm1 != null && nm1.startsWith(".")) {
	    int idx1 = nm1.lastIndexOf(".");
	    if (idx1 > 1) {
	       String oldnm = nm1;
	       nm1 = nm1.substring(idx1);
	       replaces.put(oldnm,nm1);
	     }
	    nm1 = pkg + nm1;
	  }
	 if (ismain) main = nm1;
	 if (islaunch) launch = nm1;
       }
    }
   if (main == null || launch == null) {
      System.err.println("Activity not found");
    }
   if (main != null) main = main.replace(pkg,idmap.get("PACKAGE"));
   if (launch != null) launch = launch.replace(pkg,idmap.get("PACKAGE"));

   idmap.put("MAINACTIVITY",main);
   idmap.put("LAUNCHACTIVITY",launch);

   cnts = cnts.replace(pkg,idmap.get("PACKAGE"));
   for (Map.Entry<String,String> ent : replaces.entrySet()) {
      String from = ent.getKey();
      String to = ent.getValue();
      cnts = cnts.replace(from,to);
    }

   byte [] output = cnts.getBytes(UTF8);

   return output;
}


private byte [] fixupXmlResource(byte [] cnts,Element xml,Map<String,String> idmap)
{
   Node par = xml.getParentNode();
   boolean chng = fixupXmlResourceElement(xml,idmap);
   if (!chng) return cnts;

   Element elt = null;
   if (par instanceof Document) {
      Document doc = (Document) par;
      elt = doc.getDocumentElement();
    }
   else elt = (Element) par.getFirstChild();

   String update = IvyXml.convertXmlToString(elt);
   byte [] output = update.getBytes(UTF8);
   return output;
}


private boolean fixupXmlResourceElement(Element xml,Map<String,String> idmap)
{
   boolean chng = false;
   String match = "Theme.AppCompat.";

   String ndnm = xml.getNodeName();
   if (ndnm.contains(".") && !ndnm.startsWith("android.")) {
      Node par = xml.getParentNode();
      int idx = ndnm.lastIndexOf(".");
      String newnm = idmap.get("PACKAGE") + ndnm.substring(idx);
      Element rep = IvyXml.cloneElement(newnm,xml);
      try {
	 par.replaceChild(rep,xml);
	 chng = true;
       }
      catch (Throwable t) {
	 System.err.println("Problem: " + t);
	 t.printStackTrace();
       }
      xml = rep;
    }

   List<Attr> rename = new ArrayList<Attr>();
   NamedNodeMap attrs = xml.getAttributes();
   for (int i = 0; i < attrs.getLength(); ++i) {
      Attr attr = (Attr) attrs.item(i);
      String anm = attr.getName();
      int idx = anm.indexOf(":");
      if (idx > 0) {
	 String pfx = anm.substring(0,idx);
	 if (!pfx.equals("android") && !pfx.equals("xmlns")) {
	    rename.add(attr);
	    continue;
	  }
       }
      if (anm.equals("parent")) {
	 String val = attr.getValue();
	 int idx1 = val.indexOf(match);
	 if (idx1 >= 0) {
	    idx1 += match.length();
	    String nval = "@android:style/Theme.Holo." + val.substring(idx1);
	    attr.setValue(nval);
	    chng = true;
	  }
       }
      if (anm.startsWith("android:")) {
	 String v = attr.getValue();
	 if (v.contains("?") && !v.startsWith("?android")) {
	    v = v.replace('?','X');
	    attr.setValue(v);
	    chng = true;
	  }
       }
    }
   for (Attr attr : rename) {
      xml.removeAttributeNode(attr);
      String anm = attr.getName();
      int idx = anm.indexOf(":");
      String nnm = anm.substring(idx+1);
      xml.setAttribute(nnm,attr.getValue());
      chng = true;
    }

   for (Node n = xml.getFirstChild(); n != null; n = n.getNextSibling()) {
      if (n.getNodeType() == Element.TEXT_NODE) {
	 Text t = (Text) n;
	 String txt = t.getData();
	 if (txt.contains("?")) {
	    txt = txt.replace("?","X");
	    t.setData(txt);
	    chng = true;
	  }
       }
    }

   for (Element celt : IvyXml.children(xml)) {
      chng |= fixupXmlResourceElement(celt,idmap);
    }

   return chng;
}



/********************************************************************************/
/*										*/
/*	Methods for using ant to run junit					*/
/*										*/
/********************************************************************************/

private void compileAndRunTestFile(Map<String,String> idmap) throws S6Exception
{
   String [] env = null;
   String ctxmap = idmap.get("S6_CONTEXT_MAP");
   if (ctxmap != null) {
      Map<String,String> oenv = new HashMap<String,String>(System.getenv());
      String libname = IvyFile.expandName(CTX_LIBRARY,idmap);
      oenv.put("LD_PRELOAD",libname);
      oenv.put("S6_CONTEXTMAP",ctxmap);
      env = new String[oenv.size()];
      int i = 0;
      for (Map.Entry<String,String> ent : oenv.entrySet()) {
	 env[i++] = ent.getKey() + "=" + ent.getValue();
       }
      System.err.println("USING CONTEXT " + libname);
    }

   try {
      String cmd = ANT_COMMAND + " $(DIRECTORY)";
      cmd = IvyFile.expandName(cmd,idmap);

      int fgs = IvyExec.IGNORE_OUTPUT;
      if (for_request.doDebug()) {
	 System.err.println("RUN ANT: " + cmd);
	 fgs = IvyExec.ERROR_OUTPUT;
       }

      IvyExec ex = new IvyExec(cmd,env,fgs);

      ex.waitFor();

      // if (sts != 0) throw new S6Exception("Error running ant (" + sts + ")");
    }
   catch (IOException e) {
      throw new S6Exception("Problem running ant: " + e,e);
    }
}



private SuiteReport readTestStatus(Map<String,String> idmap) throws S6Exception
{
   SuiteReport sr = new SuiteReport(for_request,idmap);

   String onm = IvyFile.expandName("$(DIRECTORY)/$(JUNITOUT)",idmap);
   File f = new File(onm);
   if (!f.exists()) throw new S6Exception("Junit failed: " + f);
   File jarf = IvyFile.expandFile("$(DIRECTORY)/$(UIJAR)",idmap);

   Element e = IvyXml.loadXmlFromFile(onm);
   if (e == null) throw new S6Exception("No junit output found in " + onm);

   int tct = 0;
   for (Element te : IvyXml.elementsByTag(e,"testcase")) {
      boolean iserr = false;
      String cnm = IvyXml.getAttrString(te,"classname");
      String nm = IvyXml.getAttrString(te,"name");
      String msg = null;
      double tm = IvyXml.getAttrDouble(te,"time");
      Element ee = IvyXml.getElementByTag(te,"error");
      if (ee != null) iserr = true;
      else ee = IvyXml.getElementByTag(te,"failure");

      if (ee != null) {
	 msg = IvyXml.getAttrString(ee,"message");
	 if (msg == null) msg = IvyXml.getText(ee);
	 if (msg == null) msg = "UNKNOWN ERROR";
       }
      ++tct;

      sr.addReport(nm,cnm,tm,msg,iserr,jarf);
    }

   if (tct == 0) throw new S6Exception("No test case output found in " + onm);

   return sr;
}



private void clear(Map<String,String> idmap)
{
   if (!for_request.doDebug()) {
      try {
	 String dnm = IvyFile.expandName("$(DIRECTORY)",idmap);
	 IvyFile.remove(dnm);
       }
      catch (IOException e) { }
    }
}



/********************************************************************************/
/*										*/
/*	Methods to generate the searched for code				*/
/*										*/
/********************************************************************************/

private void setupCode(Map<String,String> idmap)
{
   JavaContracts jc = new JavaContracts(for_request.getContracts(),java_fragment);
   if (jc.insertContracts()) idmap.put("ANTRUN","jmltest");

   String gencode = "";
   switch (for_request.getSearchType()) {
      case METHOD :
      case CLASS :
      case FULLCLASS :
      case TESTCASES :
	 gencode = java_fragment.getText();
	 break;
      case PACKAGE :
      case APPLICATION :
      case UIFRAMEWORK :
      case ANDROIDUI :
	 break;
    }

   if (java_fragment.getFragmentType() == S6FragmentType.METHOD) {
      gencode = "private static class " + idmap.get("TESTCLASS") + " {\n\n" + gencode;
      gencode += "\n}\t//end of class " + idmap.get("TESTCLASS") + "\n";
    }

   gencode = jc.fixupJmlCode(gencode);

   idmap.put("CODE",gencode);

   jc.removeContracts();
}




/********************************************************************************/
/*										*/
/*	Methods to generate static initializers 				*/
/*										*/
/********************************************************************************/

private void setupUIHierarchy(Map<String,String> idmap)
{
   S6Request.UISignature usg = (S6Request.UISignature) for_request.getSignature();

   StringBuffer buf = new StringBuffer();
   buf.append("private edu.brown.cs.s6.runner.RunnerS6HierData [] s6_hier_data = new edu.brown.cs.s6.runner.RunnerS6HierData[] {\n");
   addUIComponent(buf,usg.getHierarchy());
   buf.append("};\n");

   idmap.put("STATICS",buf.toString());
}


private void addUIComponent(StringBuffer buf,S6Request.UIComponent c)
{
   buf.append("new edu.brown.cs.s6.runner.RunnerS6HierData(");
   addString(buf,c.getId());
   buf.append(",");
   buf.append(c.getXposition());
   buf.append(",");
   buf.append(c.getYposition());
   buf.append(",");
   buf.append(c.getWidth());
   buf.append(",");
   buf.append(c.getHeight());
   buf.append(",\"");
   for (String s : c.getTypes()) {
      buf.append(s);
      buf.append(",");
    }
   buf.append("\",");
   addComp(buf,c.getTopAnchor());
   buf.append(",");
   addComp(buf,c.getBottomAnchor());
   buf.append(",");
   addComp(buf,c.getLeftAnchor());
   buf.append(",");
   addComp(buf,c.getRightAnchor());
   buf.append(",");
   addString(buf,c.getData());
   buf.append(",");
   List<S6Request.UIComponent> ch = c.getChildren();
   if (ch == null) {
      buf.append("0),\n");
    }
   else {
      buf.append(ch.size());
      buf.append("),\n");
      for (S6Request.UIComponent cc : ch) addUIComponent(buf,cc);
    }
}


private void addComp(StringBuffer buf,S6Request.UIComponent c)
{
   if (c == null) addString(buf,null);
   else addString(buf,c.getId());
}


private void addString(StringBuffer buf,String s)
{
   if (s == null) buf.append("null");
   else {
      buf.append("\"");
      buf.append(s);
      buf.append("\"");
    }
}




/********************************************************************************/
/*										*/
/*	Methods to actually generate test code					*/
/*										*/
/********************************************************************************/

private void setupTests(Map<String,String> idmap) throws S6Exception
{
   StringBuffer buf = new StringBuffer();
   String create = "";

   if (java_fragment.getUseConstructor()) {
      create = idmap.get("TESTCLASS") + " __object = new " + idmap.get("TESTCLASS") + "();\n";
      idmap.put("PREFIX","__object");
      idmap.put("SETUP",create);
    }

   boolean havetest = false;
   for (S6TestCase tc : for_request.getTests().getTestCases()) {
      if (tc.getTestType() == S6TestType.JUNIT) {
	 handleJunitTest(tc,idmap);
	 continue;
       }
      havetest = true;
      String fnm = tc.getName();
      if (!fnm.startsWith("test_")) fnm = "test_" + fnm;

      buf.append("\n\n");
      buf.append("@org.junit.Test public void " + fnm + "() throws Exception\n");
      buf.append("{\n");
      if (idmap.get("SEC_PREFIX") != null) buf.append(idmap.get("SEC_PREFIX"));

      buf.append(create);

      switch (tc.getTestType()) {
	 case USERCODE :
	    generateUserTest(tc,idmap,buf);
	    break;
	 case CALLS :
	    generateCallsTest(tc,idmap,buf);
	    break;
	 case JUNIT :
	    // shouldn't get here
	    break;
       }

      if (idmap.get("SEC_SUFFIX") != null)
	 buf.append(idmap.get("SEC_SUFFIX"));
      if (idmap.get("TEST_FINISHER") != null)
	 buf.append(idmap.get("TEST_FINISHER"));

      buf.append("}\n");
    }

   idmap.put("HAVETEST",Boolean.toString(havetest));
   idmap.put("TESTS",buf.toString());
}



/********************************************************************************/
/*										*/
/*	Methods specialized for identifying test cases				*/
/*										*/
/********************************************************************************/

private void setupTesting(Map<String,String> idmap)
{
   StringBuffer buf = new StringBuffer();

   String nm = for_request.getSignature().getClassSignature().getName();
   String pnm = for_request.getSignature().getName();
   String cnm = idmap.get("CLASS");
   String nm1 = pnm + "." + cnm + "." + nm;
   String nm2 = pnm + "." + cnm + "." + "S6TestFinisher";
   buf.append("@org.junit.runner.RunWith(org.junit.runners.Suite.class)\n");
   buf.append("@org.junit.runners.Suite.SuiteClasses({");
   buf.append(nm1 + ".class,");
   buf.append(nm2 + ".class,");
   buf.append("})\n");
   idmap.put("ANNOTATION",buf.toString());
}







private void generateUserTest(S6TestCase tc,Map<String,String> idmap,StringBuffer buf)
{
   buf.append(expandCode(tc.getUserCode(),idmap));
}



private void generateCallsTest(S6TestCase tc,Map<String,String> idmap,StringBuffer buf)
{
   String uc = tc.getUserCode();
   if (uc != null) {
      buf.append(expandCode(uc,idmap));
      buf.append("\n");
    }

   for (S6TestCase.CallTest ct : tc.getCalls()) {
      for (S6TestCase.CallArg ca : ct.getArguments()) {
	 String s = ca.getArgCode();
	 if (s != null) buf.append(expandCode(s,idmap));
       }
      S6TestCase.CallArg cr = ct.getReturnValue();
      if (cr != null && cr.getArgCode() != null) {
	 buf.append(cr.getArgCode());
	 buf.append("\n");
       }

      S6TestOp op = ct.getOperator();
      switch (op) {
	 case SAVE :
	    if (cr != null) {
	       buf.append(cr.getArgValue());
	       buf.append(" = ");
	     }
	    break;
	 case NONE :
	 case IGNORE :
	    break;
	 case EQL :
	    buf.append("assertEquals(\"Result of call\",");
	    buf.append(codeString(cr));
	    buf.append(",");
	    break;
	 case NEQ :
	    buf.append("assertNotEquals(\"Result of call\",");
	    buf.append(codeString(cr));
	    buf.append(",");
	    break;
	 case SAME :
	    buf.append("assertSame(");
	    buf.append(codeString(cr));
	    buf.append(",");
	    break;
	 case DIFF :
	    buf.append("assertNotSame(");
	    buf.append(codeString(cr));
	    buf.append(",");
	    break;
	 case THROW :
	    buf.append("try {\n");
	    break;
	 case SHOW :
	    buf.append("try {\n");
	    buf.append("assertShow(");
	    break;
	 case INTERACT :
	    if (idmap.get("UITEST") == null) idmap.put("UITEST",tc.getName());
	    buf.append("try {\n");
	    buf.append("disableTimer();\n");
	    if (for_request.getSearchType() == S6SearchType.ANDROIDUI)
	       buf.append("assertAndroidInteract(");
	    else
	       buf.append("assertInteract(");
	    break;
	 case HIERARCHY :
	    buf.append("assertMatchHierarchy(");
	    break;
	 case SCOREHIER :
	    if (cr != null) {
	       buf.append(cr.getArgValue());
	       buf.append(" = ");
	     }
	    buf.append("scoreMatchHierarchy(");
	    break;
       }

      String mthd = ct.getMethod();
      String fld = null;
      if (ct.isConstructor()) buf.append("new ");
      else if (ct.isAccess()) {
	 fld = mthd;
	 mthd = null;
	 if (fld == null) fld = "$(PREFIX)";
	 fld = expandCode(fld,idmap);
       }
      else {
	 int idx = mthd.indexOf(".");
	 if (idx < 0) mthd = "$(PREFIX)." + mthd;
	 mthd = expandCode(mthd,idmap);
       }
      if (mthd != null) {
	 buf.append(mthd);
	 buf.append("(");
	 int i = 0;
	 for (S6TestCase.CallArg ca : ct.getArguments()) {
	    if (i++ != 0) buf.append(",");
	    buf.append(codeString(ca));
	  }
	 buf.append(")");
       }
      else {
	 if (fld != null) buf.append("(" + fld + ")");
	 for (S6TestCase.CallArg ca : ct.getArguments()) {
	    buf.append(",");
	    buf.append(codeString(ca));
	  }
       }

      switch (op) {
	 default :
	    buf.append(";\n");
	    break;
	 case EQL :
	 case NEQ :
	 case SAME :
	 case DIFF :
	    buf.append(");\n");
	    break;
	 case INTERACT :
	 case SHOW :
	    buf.append(");\n");
	    buf.append("}\n");
	    buf.append("catch (junit.framework.AssertionFailedError __e) { throw __e; }\n");
	    buf.append("catch (java.lang.Throwable __t) { assertShowThrow(__t); }\n");
	    break;
	 case HIERARCHY :
	 case SCOREHIER :
	    buf.append(",s6_hier_data);\n");
	    break;
	 case THROW :
	    String ex = ct.getThrows();
	    if (ex == null) ex = "java.lang.Throwable";
	    buf.append(";\nfail(\"Exception " + ex + " expected\");\n");
	    buf.append("}\n");
	    buf.append("catch (junit.framework.AssertionFailedError __e) { throw __e; }\n");
	    buf.append("catch (" + ex + " __e) { }\n");
	    break;
       }
    }
}



private void handleJunitTest(S6TestCase tc,Map<String,String> idmap)
{
   String tc1 = idmap.get("TESTCLASSES");
   String tc2 = idmap.get("TESTCASES");
   S6TestCase.JunitTest jut = (S6TestCase.JunitTest) tc;

   if (tc1 == null) {
      idmap.put("ANNOTATION","@org.junit.runner.RunWith(S6TestClass.S6TestSelectRunner.class)");
      tc1 = "";
      tc2 = "";
    }
   tc1 += "\"" + jut.getJunitClass() + "\", ";
   tc2 += "\"" + jut.getJunitName() + "\", ";
   idmap.put("TESTCLASSES",tc1);
   idmap.put("TESTCASES",tc2);
}



private String expandCode(String s,Map<String,String> idmap)
{
   if (s == null) return "";

   int idx = s.indexOf("$(");
   if (idx < 0) return s;
   s = IvyFile.expandName(s,idmap);
   return s;
}



private String codeString(S6TestCase.CallArg ca)
{
   String r = null;

   if (ca == null) return null;

   switch (ca.getArgType()) {
      default :
      case LITERAL :
      case VARIABLE :
      case SAVE :
	 r = ca.getArgValue();
	 break;
      case STRING :
	 String s = ca.getArgValue();
	 if (s == null) s = "";
	 StringBuffer buf = new StringBuffer();
	 buf.append("\"");
	 for (int i = 0; i < s.length(); ++i) {
	    char c = s.charAt(i);
	    if (c == '"') buf.append("\\");
	    // handle escapes
	    buf.append(c);
	  }
	 buf.append("\"");
	 r = buf.toString();
	 break;
    }

   return r;
}



/********************************************************************************/
/*										*/
/*	Methods to handle user context						*/
/*										*/
/********************************************************************************/






/********************************************************************************/
/*										*/
/*	Methods to handle security						*/
/*										*/
/********************************************************************************/

private void setupSecurity(Map<String,String> idmap)
{
   S6Security sec = for_request.getSecurity();
   if (sec.isEmpty()) return;

   StringBuffer buf = new StringBuffer();
   buf.append("static " + S6_SECURITY_POLICY_CLASS + " " + S6_SECURITY_POLICY);
   buf.append(" = new " + S6_SECURITY_POLICY_CLASS + "(\"" + idmap.get("DIRECTORY") + "\");\n");
   buf.append("static " + S6_SECURITY_PERMIT_CLASS + "[] " + S6_SECURITY_PERMITS);
   buf.append(" = new " + S6_SECURITY_PERMIT_CLASS + "[] {\n");

   int ct = 0;
   for (S6Security.Permit pm : sec.getPermissions()) {
      if (ct++ > 0) buf.append(",\n");
      outputPermission(buf,pm.getType(),pm.getArgument(),pm.getOperations());
    }

   buf.append("\n};\n");
   idmap.put("SECURITY",buf.toString());

   buf = new StringBuffer();
   buf.append("try {\n");
   buf.append(S6_SECURITY_POLICY + ".runMethod(" + S6_SECURITY_PERMITS + ",\n");
   buf.append("new java.security.PrivilegedExceptionAction<java.lang.Object>() {\n");
   buf.append("public java.lang.Object run() throws Exception {\n");
   idmap.put("SEC_PREFIX",buf.toString());

   buf = new StringBuffer();
   buf.append("return null;\n} } ); }\n");
   buf.append("catch (SecurityException t) { fail(\"Security failure: \" + t.toString()); }");
   idmap.put("SEC_SUFFIX",buf.toString());
}



private void outputPermission(StringBuffer buf,S6SecurityType t,String a,String op)
{
   String pt = null;

   switch (t) {
      default :
	 break;
      case FILE :
	 pt = S6_SECURITY_PERMIT_FILE;
	 break;
      case SOCKET :
	 pt = S6_SECURITY_PERMIT_SOCKET;
	 break;
      case AWT :
	 pt = S6_SECURITY_PERMIT_AWT;
	 break;
      case PROPERTY :
	 pt = S6_SECURITY_PERMIT_PROPERTY;
	 break;
      case RUNTIME :
	 pt = S6_SECURITY_PERMIT_RUNTIME;
	 break;
    }
   if (pt == null) return;

   buf.append("new " + pt + "(");
   if (a != null) buf.append("\"" + a + "\",");
   if (op == null) buf.append("null");
   else buf.append("\"" + op + "\"");
   buf.append(")");
}




/********************************************************************************/
/*										*/
/*	Methods to setup the source file					*/
/*										*/
/********************************************************************************/

@SuppressWarnings("unchecked")
private void setupSourceFile(Map<String,String> idmap)
{
   if (user_context == null) return;

   String cls = user_context.getContextClass();
   if (cls == null) return;

   String src = user_context.getSourceFile();
   if (src == null) return;

   CompilationUnit cu = FragmentJava.parseSourceFile(src);
   if (cu == null) return;

   for (JcompType jt : java_fragment.getImportTypes()) {
      boolean fnd = false;
      for (Object o : cu.imports()) {
	 ImportDeclaration id = (ImportDeclaration) o;
	 if (id.isOnDemand()) continue;
	 String nm = id.getName().getFullyQualifiedName();
	 if (nm.equals(jt.getName())) fnd = true;
       }
      if (!fnd) {
	 AST ast = cu.getAST();
	 ImportDeclaration id = ast.newImportDeclaration();
	 id.setName(JavaAst.getQualifiedName(ast,jt.getName()));
	 cu.imports().add(id);
       }
    }

   AbstractTypeDeclaration typ = null;
   for (Iterator<?> it = cu.types().iterator(); it.hasNext() && typ == null; ) {
      AbstractTypeDeclaration atd = (AbstractTypeDeclaration) it.next();
      String tnm = atd.getName().getIdentifier();
      if (tnm.equals(cls)) typ = atd;
    }
   if (typ == null) return;

   S6Request.MethodSignature msg = null;
   switch (for_request.getSearchType()) {
      case METHOD :
	 msg = (S6Request.MethodSignature) for_request.getSignature();
	 break;
      default :
	 break;
   }

   List<ASTNode> decls = typ.bodyDeclarations();
   for (Iterator<ASTNode> it = decls.iterator(); it.hasNext(); ) {
      ASTNode hn = it.next();
      if (hn.getNodeType() == ASTNode.METHOD_DECLARATION && msg !=  null) {
	 MethodDeclaration md = (MethodDeclaration) hn;
	 String nm = md.getName().getIdentifier();
	 if (nm.equals(msg.getName())) {
	    it.remove();
	 }
      }
   }

   for (ASTNode hn : java_fragment.getHelpers()) {
      ASTNode nhn = ASTNode.copySubtree(cu.getAST(),hn);
      decls.add(nhn);
    }
   ASTNode bn = java_fragment.getAstNode();
   ASTNode nbn = ASTNode.copySubtree(cu.getAST(),bn);

   decls.add(nbn);

   // TODO: Add imports here
   // TODO: handle jml here

   String rsrc = cu.toString();

   idmap.remove("CODE");

   idmap.put("SOURCECODE",rsrc);
   idmap.put("SOURCECLASS",cls);
}



/********************************************************************************/
/*										*/
/*	Class to hold a test report						*/
/*										*/
/********************************************************************************/

private static class SuiteReport implements S6TestResults {

   private Map<String,TestReport> test_cases;
   private String base_directory;

   SuiteReport(S6Request.Search sr,Map<String,String> idmap) {
      base_directory = idmap.get("DIRECTORY");
      test_cases = new HashMap<String,TestReport>();
      for (S6TestCase tc : sr.getTests().getTestCases()) {
	 test_cases.put(tc.getName(),new TestReport(tc));
       }
      if (sr.getSearchType() == S6SearchType.TESTCASES) {
	 test_cases.put("S6testFinisher",new TestReport(false));
       }
    }

   public boolean allPassed() {
      for (TestReport tr : test_cases.values()) {
	 if (!tr.getPassed()) return false;
       }
      return true;
    }

   public S6SolutionFlag getSummaryResult() {
      S6SolutionFlag rslt = S6SolutionFlag.PASS;

      for (TestReport tr : test_cases.values()) {
	 if (!tr.getPassed() && !tr.isOptional()) {
	    return S6SolutionFlag.FAIL;
	  }
	 else if (tr.getPassed() && tr.getUserValue() != null) {
	    rslt = S6SolutionFlag.USER;
	  }
       }

      return rslt;
    }

   public double getRequiredTime() {
      double tot = 0;
      for (TestReport tr : test_cases.values()) {
	 if (!tr.isOptional()) tot += tr.getTime();
       }
      return tot;
    }

   public double getTime(String test) {
      TestReport tr = test_cases.get(test);
      if (tr == null) return 0;
      return tr.getTime();
    }

   public boolean getPassed(String test) {
      TestReport tr = test_cases.get(test);
      if (tr == null) return false;
      return tr.getPassed();
    }

   public boolean getFailed(String test) {
      TestReport tr = test_cases.get(test);
      if (tr == null) return true;
      return tr.getFailed();
    }

   public boolean getError(String test) {
      TestReport tr = test_cases.get(test);
      if (tr == null) return true;
      return tr.getError();
    }

   public String getErrorMessage(String test) {
      TestReport tr = test_cases.get(test);
      if (tr == null) return null;
      return tr.getErrorMessage();
    }

   public String getUserType(String test) {
      TestReport tr = test_cases.get(test);
      if (tr == null) return null;
      return tr.getUserType();
    }

   public String getUserValue(String test) {
      TestReport tr = test_cases.get(test);
      if (tr == null) return null;
      return tr.getUserValue();
    }

   public byte [] getJarFile(String test) {
      TestReport tr = test_cases.get(test);
      if (tr == null) return null;
      return tr.getJarFile();
    }

   public void setTestStatus(String test,S6SolutionFlag sts) {
      TestReport tr = test_cases.get(test);
      if (tr != null) tr.setTestStatus(sts);
    }

   public List<S6TestResults.S6ErrorResult> getErrorResults() {
      List<S6TestResults.S6ErrorResult> errs = new ArrayList<S6ErrorResult>();

      for (Map.Entry<String,TestReport> ent : test_cases.entrySet()) {
	 TestReport tr = ent.getValue();
	 if (tr.getError()) return null;
	 if (tr.getPassed()) continue;
	 String msg = tr.getErrorMessage();
	 if (msg == null) return null;
	 Matcher pm = ERROR_PATTERN.matcher(msg);
	 if (!pm.find()) return null;
	 String s0 = pm.group(1);
	 String s1 = pm.group(2);
	 errs.add(new ErrorResult(s0,s1));
       }

      return errs;
    }

   public List<String> getMessageResults() {
      List<String> errs = new ArrayList<String>();

      for (Map.Entry<String,TestReport> ent : test_cases.entrySet()) {
	 TestReport tr = ent.getValue();
	 if (tr.getError() || !tr.getPassed()) continue;
	 String msg = tr.getErrorMessage();
	 if (msg == null) continue;
	 errs.add(msg);
       }

      return errs;
    }

   void addReport(String nm,String cnm,double time,String errmsg,boolean iserr,File jarf) {
      TestReport tr = test_cases.get(nm);
      if (tr == null && nm.startsWith("test") && nm.length() > 4)
	 tr = test_cases.get(nm.substring(5));
      if (tr == null && cnm != null)
	 tr = test_cases.get(nm + "(" + cnm + ")");
      if (tr != null) tr.setReport(time,errmsg,iserr,jarf);
      else if (iserr) {
	 tr = test_cases.get("S6testFinisher");
	 if (tr != null) tr.addError();
       }
    }

   public String printSummary() {
      StringBuffer buf = new StringBuffer();
      buf.append(getSummaryResult());
      buf.append(" IN ");
      buf.append(base_directory);
      return buf.toString();
    }


}	// end of subclass SuiteReport



private static class ErrorResult implements S6TestResults.S6ErrorResult {

   private String expected_value;
   private String actual_value;

   ErrorResult(String e,String a) {
      expected_value = e;
      actual_value = a;
    }

   public String getExpected()		{ return expected_value; }
   public String getActual()		{ return actual_value; }

}	// end of subclass ErrorResult




private static class TestReport {

   private boolean test_passed;
   private boolean is_optional;
   private boolean is_error;
   private String error_message;
   private double test_time;
   private String user_value;
   private String user_type;
   private byte [] jar_file;
   private int num_error;

   TestReport(S6TestCase tc) {
      this(tc.isOptional());
    }

   TestReport(boolean opt) {
      test_passed = false;
      is_optional = opt;
      error_message = null;
      is_error = false;
      test_time = 0;
      user_value = null;
      jar_file = null;
      num_error = 0;
   }

   double getTime()			{ return test_time; }
   boolean getPassed()			{ return test_passed; }
   boolean isOptional() 		{ return is_optional; }
   boolean getFailed()			{ return !test_passed; }
   boolean getError()			{ return !test_passed && is_error; }
   String getErrorMessage()		{ return error_message; }

   String getUserType() 		{ return user_type; }
   String getUserValue()		{ return user_value; }
   byte [] getJarFile() 		{ return jar_file; }
   void addError()			{ ++num_error; }

   void setTestStatus(S6SolutionFlag sts) {
      switch (sts) {
	 case KEEP :
	 case DONE_TRANSFORM :
	 case DEPEND_FAIL :
	 case DEPEND_PASS :
	    break;
	 case FAIL :
	    test_passed = false;
	    is_error = true;
	    error_message = "User Decision";
	    break;
	 case USER :
	    if (user_type == null) {
	       test_passed = false;
	       error_message = "User Decision";
	     }
	    else user_type = "RUN:" + user_type;
	    break;
	 case PASS :
	    user_value = null;
	    user_type = null;
	    break;
	 default :
	    break;
       }
    }

   void setReport(double time,String errmsg,boolean iserr,File jarf) {
      test_time = time;

      if (errmsg != null && errmsg.startsWith("Throws java.lang.AssertionError: ")) {
	 int idx0 = errmsg.indexOf(":");
	 errmsg = errmsg.substring(idx0+2);
       }
      if (errmsg != null && errmsg.startsWith("S6AskUser: ")) {
	 int idx0 = errmsg.indexOf(':');
	 int idx1 = errmsg.indexOf(':',idx0+1);
	 if (idx1 >= 0) {
	    user_type = errmsg.substring(idx0+1,idx1).trim();
	    String val = errmsg.substring(idx1+1).trim();
	    user_value = buildHtmlUserValue(user_type,val);
	  }
	 if (user_value != null) errmsg = null;
	 if (jarf != null && jarf.exists()) {
	    int len = (int) jarf.length();
	    jar_file = new byte[len];
	    try {
	       FileInputStream fis = new FileInputStream(jarf);
	       int ct = 0;
	       while (ct < len) {
		  int i = fis.read(jar_file,ct,len-ct);
		  ct += i;
		}
	       fis.close();
	       jarf.delete();
	     }
	    catch (IOException e) {
	       jar_file = null;
	     }
	  }
       }
      else if (errmsg != null && errmsg.startsWith("S6TestCount: ")) {
	 int idx0 = errmsg.indexOf(":");
	 error_message = errmsg.substring(idx0+2).trim();
	 if (num_error > 0) {
	    int idx1 = error_message.indexOf(" ");
	    int succ = Integer.parseInt(error_message.substring(0,idx1));
	    int fail = Integer.parseInt(error_message.substring(idx1+1));
	    fail += num_error*4;
	    if (fail <= (succ+4)/5) errmsg = null;
	  }
	 else errmsg = null;
       }

      if (errmsg == null) test_passed = true;
      else {
	 test_passed = false;
	 error_message = errmsg;
	 is_error = iserr;
       }
    }

}	// end of subclass TestReport




/********************************************************************************/
/*										*/
/*	Methods to build html output from user result				*/
/*										*/
/********************************************************************************/

private static String buildHtmlUserValue(String typ,String val)
{
   String rslt = null;

   if (typ.equals("IMAGE")) {
      rslt = buildInlineImage(val);
    }
   else if (typ.equals("OBJECT")) {
      rslt = val;
    }
   else if (typ.equals("STRING")) {
      rslt = val;
    }
   else if (typ.equals("NUMBER")) {
      rslt = val;
    }
   else if (typ.equals("THROW")) {
      rslt = "Throws " + val;
    }
   else if (typ.equals("HIERARCHY")) {
      rslt = val;
    }

   return rslt;
}



private static String buildInlineImage(String file)
{
   double score = 0;
   int idx = file.indexOf(" @@@ ");
   if (idx > 0) {
      try {
	 score = Double.parseDouble(file.substring(idx+5));
	 file = file.substring(0,idx);
       }
      catch (NumberFormatException e) { }
    }

   StringBuffer buf = new StringBuffer();
   buf.append("<IMG ");
   buf.append("SRC='data:image/png;base64,");
   if (!convertToBase64(file,buf)) return null;
   buf.append("' ALT='");
   if (score == 0) buf.append("IMAGE");
   else {
      buf.append("IMAGE:" + score);
    }
   buf.append("'></IMG>");
   return buf.toString();
}



private static final String BASE64_CODE =
	"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

private static final Charset	UTF8 = Charset.forName("UTF8");



private static boolean convertToBase64(String fnm,StringBuffer rslt)
{
   File f = new File(fnm);
   if (!f.exists()) return false;

   try {
      BufferedInputStream fis = new BufferedInputStream(new FileInputStream(f));
      boolean eof = false;
      while (!eof) {
	 int c0 = fis.read();
	 if (c0 < 0) break;
	 int c1 = fis.read();
	 int c2 = 0;
	 if (c1 < 0) {
	    eof = true;
	    c1 = 0;
	  }
	 else {
	    c2 = fis.read();
	    if (c2 < 0) {
	       eof = true;
	       c2 = 0;
	     }
	  }
	 int j = (c0 << 16) + (c1 << 8) + c2;
	 rslt.append(BASE64_CODE.charAt((j >> 18) & 0x3f));
	 rslt.append(BASE64_CODE.charAt((j >> 12) & 0x3f));
	 rslt.append(BASE64_CODE.charAt((j >> 6) & 0x3f));
	 rslt.append(BASE64_CODE.charAt(j & 0x3f));
       }
      fis.close();
    }
   catch (IOException e) {
      System.err.println("S6: TEST: Problem encoding image: " + e);
      return false;
    }

   return true;
}




}	// end of class JavaTester




/* end of JavaTester.java */









































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































