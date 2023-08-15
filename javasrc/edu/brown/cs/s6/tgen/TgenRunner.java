/********************************************************************************/
/*										*/
/*		TgenRunner.java 						*/
/*										*/
/*	Extract and run the test found by S6 and compare to initial test	*/
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.w3c.dom.Element;

import edu.brown.cs.ivy.exec.IvyExec;
import edu.brown.cs.ivy.file.IvyFile;
import edu.brown.cs.ivy.xml.IvyXml;

public class TgenRunner implements TgenConstants
{


/********************************************************************************/
/*										*/
/*	Main Program								*/
/*										*/
/********************************************************************************/

public static void main(String [] args)
{
   TgenRunner tr = new TgenRunner(args);
   tr.process();
}



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private File		original_test;
private File		original_file;
private File		s6_test;
private List<File>	work_directories;
private File		test_directory;
private File		output_csv;
private boolean 	output_append;
private String		package_name;
private double		time_used;
private String		key_words;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

private TgenRunner(String [] args)
{
   original_test = null;
   original_file = null;
   s6_test = null;
   test_directory = null;
   package_name = null;
   output_csv = new File("tgenrun.csv");
   output_append = false;
   work_directories = new ArrayList<File>();
   key_words = null;

   scanArgs(args);
}



/********************************************************************************/
/*										*/
/*	Argument processing methods						*/
/*										*/
/********************************************************************************/

private void scanArgs(String [] args)
{
   for (int i = 0; i < args.length; ++i) {
      if (args[i].startsWith("-")) {
	 if (args[i].startsWith("-o") && i+1 < args.length) {           // -o <output>
	    output_csv = new File(args[++i]);
	  }
	 else if (args[i].startsWith("-a")) {                           // -append
	    output_append = true;
	  }
	 else badArgs();
       }
      else {
	work_directories.add(new File(args[i]));
       }
    }
}



private void badArgs()
{
   System.err.println("TgetRunner: [directory]");
   System.exit(1);
}



/********************************************************************************/
/*										*/
/*	Processing methods							*/
/*										*/
/********************************************************************************/

private void process()
{
   if (work_directories.size() == 0) {
      work_directories.add(new File("."));
    }

   PrintWriter fw = null;
   try {
      fw = new PrintWriter(new FileWriter(output_csv,output_append));
    }
   catch (IOException e) {
      System.err.println("TGENRUNNER: Problem opening output file: " + e);
      System.exit(1);
    }

   for (File f : work_directories) {
      setupPaths(f);

      findOriginalTestFile();

      extractS6Test();

      extractKeywords();

      runCoverageTests();

      gatherData(fw);
    }

   fw.close();
}



private void setupPaths(File f)
{
   try {
      test_directory = f.getCanonicalFile();
    }
   catch (IOException e) {
      System.err.println("TGENRUNNER: Bad directory: " + e);
      System.exit(1);
    }

   package_name = test_directory.getName();

   time_used = 0;
}



private void findOriginalTestFile()
{
   for (File f : test_directory.listFiles()) {
      String path = f.getName();
      if (!path.endsWith(".java")) continue;
      if (path.startsWith("S6Test")) continue;
      if (path.contains("Test") && original_test == null) {
	 original_test = f;
       }
      else if (original_file == null) {
	 original_file = f;
       }
    }
}


private void extractKeywords()
{
   StringBuffer buf = new StringBuffer();

   String tname = package_name;
   int idx = tname.indexOf("_");
   tname = tname.substring(idx+1);
   tname = "stest" + tname + ".s6";
   File f = new File(test_directory,tname);
   Element xml = IvyXml.loadXmlFromFile(f);
   Element keys = IvyXml.getChild(xml,"KEYWORDS");
   for (Element k : IvyXml.children(keys,"KEYWORD")) {
      String kwd = IvyXml.getText(k);
      if (kwd.equals("test") || kwd.equals("org.junit")) continue;
      if (buf.length() > 0) buf.append(" ");
      if (kwd.contains(" ")) {
	 buf.append("'");
	 buf.append(kwd);
	 buf.append("'");
       }
      else buf.append(kwd);
    }

   key_words = buf.toString();
}




private void extractS6Test()
{
   File rslt = new File(test_directory,"s6.mtout");
   if (!rslt.exists()) rslt = new File(test_directory,"s6.debug");
   if (!rslt.exists()) {
      System.err.println("Can't find S6 output file");
      System.exit(1);
    }

   int state = 0;
   String pfx = "<CODE><![CDATA[";
   String sfx = "]]></CODE>";
   File output = new File(test_directory,"S6Test" + original_file.getName());
   PrintWriter fw = null;
   String lastln = null;
   try (BufferedReader fr = new BufferedReader(new FileReader(rslt))) {
      for ( ; ; ) {
	 String ln = fr.readLine();
	 if (ln == null) break;
	 if (ln.length() > 0) lastln = ln;
	 switch (state) {
	    case 0 :
	       if (ln.equals("<SOLUTIONS COUNT='1'>")) {
		  state = 1;
		}
	       break;
	    case 1 :
	       ln = ln.trim();
	       if (!ln.trim().startsWith(pfx)) break;
	       ln = ln.substring(pfx.length());
	       fw = new PrintWriter(new FileWriter(output));
	       fw.println(ln);
	       state = 2;
	       break;
	    case 2 :
	       if (ln.contains(sfx)) {
		  state = 3;
		  if (!ln.equals(sfx)) {
		     int ct = sfx.length();
		     fw.println(ln.substring(0,ln.length()-ct));
		   }
		  fw.close();
		  fw = null;
		}
	       else {
		  fw.println(ln);
		}
	       break;
	    case 3 :
	       break;
	  }
       }
    }
   catch (IOException e) {
      System.err.println("TGENRUNNER: Problem extracting solution: " + e);
      System.exit(1);
    }

   if (state != 3) {
      System.err.println("TGENRUNNER: No solution found");
    }
   else {
      s6_test = output;
    }

   if (lastln != null && Character.isDigit(lastln.charAt(0))) {
      StringTokenizer tok = new StringTokenizer(lastln);
      if (tok.countTokens() == 7) {
	 String t = tok.nextToken();		// user time
	 t = tok.nextToken();			// system time
	 t = tok.nextToken();			// wall time
	 StringTokenizer tok1 = new StringTokenizer(t,":");
	 while (tok1.hasMoreTokens()) {
	    time_used *= 60;
	    String t1 = tok1.nextToken();
	    try {
	       Double d = Double.parseDouble(t1);
	       time_used += d;
	     }
	    catch (NumberFormatException e) {
	       System.err.println("TGENRUNNER: Problem parsing time: " + t);
	       break;
	     }
	  }
       }
    }
}


private void runCoverageTests()
{
   try {
      File fx = new File(test_directory,"orig.test.out.xml");
      fx.delete();
      fx = new File(test_directory,"s6test.test.out.xml");
      fx.delete();
      fx = new File(test_directory,"origtext.xml");
      fx.delete();
      fx = new File(test_directory,"s6test.xml");
      fx.delete();
      File f1 = new File(test_directory,"jacoco.exec");
      f1.delete();
      IvyExec ex = new IvyExec("ant compile",test_directory);
      ex.waitFor();
      ex = new IvyExec("ant origcoverage",test_directory);
      ex.waitFor();
      if (s6_test != null) {
	 f1.delete();
	 ex = new IvyExec("ant s6coverage",test_directory);
	 ex.waitFor();
       }
    }
   catch (IOException e) {
      System.err.println("TGENRUNNER: Problem running tests for coverage: " + e);
      System.exit(1);
    }
}


private void gatherData(PrintWriter fw)
{
   fw.print("\"" + package_name + "\"");
   String cnm = original_file.getName();
   cnm = cnm.substring(0,cnm.length()-5);		// remove .java
   fw.print(",\"" + cnm + "\"");
   fw.print("," + key_words);
   fw.print("," + time_used);
   gatherTestData(new File(test_directory,"orig.test.out.xml"),fw);
   gatherTestData(new File(test_directory,"s6test.test.out.xml"),fw);
   gatherCoverageData(new File(test_directory,"origtest.xml"),fw,true);
   gatherCoverageData(new File(test_directory,"s6test.xml"),fw,false);
   fw.println();
}


private void gatherTestData(File xml,PrintWriter pw)
{
   Element root = IvyXml.loadXmlFromFile(xml);
   int err = IvyXml.getAttrInt(root,"errors",0);
   int fail = IvyXml.getAttrInt(root,"failures",0);
   int tests = IvyXml.getAttrInt(root,"tests",0);
   int succ = tests - err -fail;
   pw.print("," + tests + "," + succ + "," + (err + fail));
}


private void gatherCoverageData(File xml,PrintWriter pw,boolean tots)
{
   Map<String,Integer> counts = new HashMap<String,Integer>();
   Map<String,Integer> totals = new HashMap<String,Integer>();
   String [] order = { "METHOD", "LINE", "BRANCH", "INSTRUCTION" };

   String cnts = null;
   try {
      cnts = IvyFile.loadFile(xml);
    }
   catch (IOException e) { }

   if (cnts == null) {
      for (int i = 0; i < order.length; ++i) {
	 pw.print(",0");
       }
      return;
    }

   int idx = cnts.indexOf("<report");
   cnts = cnts.substring(idx);
   Element root = IvyXml.convertStringToXml(cnts);
   Element pkg = IvyXml.getElementByTag(root,"package");
   Element cls = IvyXml.getElementByTag(pkg,"class");
   for (Element e : IvyXml.children(cls,"counter")) {
      int c = IvyXml.getAttrInt(e,"covered");
      int m = IvyXml.getAttrInt(e,"missed");
      String t = IvyXml.getAttrString(e,"type");
      counts.put(t,c);
      totals.put(t,c+m);
    }

   if (tots) {
      for (String t : order) {
	 pw.print("," + totals.get(t));
       }
    }
   for (String t : order) {
      double pct = (100.0 * counts.get(t))/totals.get(t);
      pct = Math.round(pct);
      pw.print("," + pct);
    }
}


}	// end of class TgenRunner




/* end of TgenRunner.java */

