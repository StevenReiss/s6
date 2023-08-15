/********************************************************************************/
/*										*/
/*		ContextMain.java						*/
/*										*/
/*	Main program for creating an S6 context 				*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/context/ContextMain.java,v 1.4 2015/09/23 17:57:52 spr Exp $ */


/*********************************************************************************
 *
 * $Log: ContextMain.java,v $
 * Revision 1.4  2015/09/23 17:57:52  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.3  2015/02/14 19:40:06  spr
 * Add test case generation.
 *
 * Revision 1.2  2013-05-09 12:26:15  spr
 * Minor changes to start ui fixups.
 *
 * Revision 1.1  2012-06-11 14:07:28  spr
 * Code cleanup
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.context;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;

import edu.brown.cs.ivy.swing.SwingSetup;
import edu.brown.cs.ivy.xml.IvyXmlWriter;
import edu.brown.cs.s6.common.S6Constants;



public class ContextMain implements ContextConstants, S6Constants
{


/********************************************************************************/
/*										*/
/*	Main program								*/
/*										*/
/********************************************************************************/

public static void main(String [] args)
{
   ContextMain cm = new ContextMain(args);

   cm.process();
}



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private String		output_name;
private JarOutputStream output_jar;
private Manifest	output_manifest;
private List<File>	class_paths;
private List<ContextUserFile>  data_files;
private File		cur_directory;
private boolean 	is_interactive;
private boolean         no_exit;
private String		cur_package;
private String		cur_class;
private File		source_file;
private List<String>	import_set;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

private ContextMain(String [] args)
{
   output_name = "temp.s6ctx";
   output_jar = null;
   output_manifest = null;
   class_paths = new ArrayList<File>();
   data_files = new ArrayList<ContextUserFile>();
   cur_directory = null;
   is_interactive = false;
   no_exit = false;
   cur_package = null;
   cur_class = null;
   import_set = new ArrayList<String>();
   source_file = null;

   scanArgs(args);
}



/********************************************************************************/
/*										*/
/*	Argument scanning methods						*/
/*										*/
/********************************************************************************/

private void scanArgs(String [] args)
{
   boolean usecp = true;

   for (int i = 0; i < args.length; ++i) {
      if (args[i].startsWith("-")) {
	 if (args[i].startsWith("-c") && i+1 < args.length) {           // -classpath <path>
	    addClassPath(args[++i]);
	    usecp = false;
	  }
	 else if (args[i].startsWith("-u") && i+1 < args.length) {      // -userpath <path>
	    addClassPath(args[++i]);
	    usecp = false;
	  }
	 else if (args[i].startsWith("-noc")) {                         // -noclasspath
	    usecp = false;
	  }
         else if (args[i].startsWith("-noexit")) {
            no_exit = true;
          }
	 else if (args[i].startsWith("-o") && i+1 < args.length) {      // -output <file>
	    output_name = args[++i];
	  }
	 else if (args[i].startsWith("-D") && i+1 < args.length) {      // -D <directory>
	    cur_directory = new File(args[++i]);
	    if (!cur_directory.exists() || !cur_directory.isDirectory()) {
	       System.err.println("S6:CONTEXT: -D directory not accessible");
	       System.exit(1);
	     }
	  }
	 else if (args[i].startsWith("-r") && i+1 < args.length) {      // -read <file>
	    addUserFile(args[++i],S6UserFileType.READ);
	  }
	 else if (args[i].startsWith("-w") && i+1 < args.length) {      // -write <file>
	    addUserFile(args[++i],S6UserFileType.WRITE);
	  }
	 else if (args[i].startsWith("-d") && i+1 < args.length) {      // -directory <file>
	    addUserFile(args[++i],S6UserFileType.DIRECTORY);
	  }
	 else if (args[i].startsWith("-I") && i+1 < args.length) {      // -Import <import>
	    import_set.add(args[++i]);
	  }
	 else if (args[i].startsWith("-P") && i+1 < args.length) {      // -Package <package>
	    if (cur_package != null) badArgs();
	    cur_package = args[++i];
	  }
	 else if (args[i].startsWith("-C") && i+1 < args.length) {      // -Class <class>
	    if (cur_class != null) badArgs();
	    cur_class = args[++i];
	  }
	 else if (args[i].startsWith("-S") && i+1 < args.length) {      // -Source <file>
	    String fnm = args[++i];
	    source_file = new File(fnm);
	    if (!source_file.canRead() || source_file.isDirectory() || !fnm.endsWith(".java")) {
	       System.err.println("S6:CONTEXT: -S file is not java source file");
	       System.exit(1);
	     }
	  }
	 else if (args[i].startsWith("-i")) {                           // -interactive
	    is_interactive = true;
	    usecp = false;
	  }
	 else badArgs();
       }
      else badArgs();
    }

   if (usecp) addClassPath(System.getProperty("java.class.path"));
}


private void badArgs()
{
   System.err.println("s6context [-p project] [-cp classpath] [-nocp] [-o output] [file ...]");
   System.exit(1);
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

String getOutputName()			{ return output_name; }
void setOutputName(String nm)		{ output_name = nm; }




/********************************************************************************/
/*										*/
/*	Methods to deal with class path 					*/
/*										*/
/********************************************************************************/

List<File> getClassPath()
{
   return class_paths;
}


void addClassPath(String cp)
{
   StringTokenizer tok = new StringTokenizer(cp,File.pathSeparator);
   while (tok.hasMoreTokens()) {
      File f = new File(tok.nextToken());
      addClassPath(f);
    }
}


void addClassPath(File f)
{
   if (f.exists() && f.canRead() && !class_paths.contains(f)) class_paths.add(f);
}



void clearClassPath()
{
   class_paths.clear();
}



/********************************************************************************/
/*										*/
/*	Methods to deal with user files 					*/
/*										*/
/********************************************************************************/

private void addUserFile(String name,S6UserFileType mode)
{
   if (name.startsWith(S6_DIRECTORY)) {
      name = name.substring(S6_DIRECTORY.length());
    }
   else if (name.startsWith(S6_WINDOWS)) {
      name = name.substring(S6_WINDOWS.length());
    }

   File uf = new File(cur_directory,name);

   if (mode == S6UserFileType.READ) {
      if (!uf.exists() || !uf.canRead()) {
	 System.err.println("User file " + uf + " not accessible");
	 System.exit(1);
       }
    }

   ContextUserFile ufd = new ContextUserFile(uf,name,mode);
   data_files.add(ufd);
}



Collection<ContextUserFile> getUserFiles()		{ return data_files; }




/********************************************************************************/
/*										*/
/*	Methods to deal with insertion context					*/
/*										*/
/********************************************************************************/

String getInsertPackage()			{ return cur_package; }
void setInsertPackage(String s) 		{ cur_package = s; }

String getInsertClass() 			{ return cur_class; }
void setInsertClass(String s)
{
   if (s == null) {
      cur_class = null;
      return;
    }
   int idx = s.lastIndexOf(".");
   if (idx >= 0) {
      cur_package = s.substring(0,idx);
      cur_class = s.substring(idx+1);
    }
   else {
      cur_class = s;
    }

   if (source_file == null) {
      String nm = cur_package.replace(".",File.separator);
      nm += File.separator + cur_class + ".java";
      for (File cp : class_paths) {
	 if (cp.isDirectory()) {
	    File cand = new File(cp,nm);
	    if (cand.exists()) {
	       source_file = cand;
	       break;
	     }
	  }
       }
    }
}



Collection<String> getInsertImports()		{ return import_set; }

File getSourceFile()				{ return source_file; }
void setSourceFile(File f)			{ source_file = f; }

					  

/********************************************************************************/
/*										*/
/*	Process methods 							*/
/*										*/
/********************************************************************************/

private void process()
{
   if (is_interactive) {
      new SwingSetup();
      ContextPanel cpnl = new ContextPanel(this);
      boolean fg = cpnl.showDialog();
      if (!fg) System.exit(1);
    }

   for (File f : class_paths) {
      handleManifest(f);
    }

   try {
      OutputStream ost = new BufferedOutputStream(new FileOutputStream(output_name));
      if (output_manifest != null) output_jar = new JarOutputStream(ost,output_manifest);
      else output_jar = new JarOutputStream(ost);

      for (File f : class_paths) {
	 addToClassContext(f);
       }

      for (ContextUserFile uf : data_files) {
	 addUserFile(uf);
       }

      addSourceFile();

      addContextFile();

      output_jar.close();
    }
   catch (IOException e) {
      System.err.println("S6:CONTEXT: Problem creating output file " + output_name + ": " + e);
      e.printStackTrace();
      System.exit(2);
    }

   System.err.println("S6:CONTEXT: Context file " + output_name + " created");

   if (!no_exit) System.exit(0);
}




private void addContextFile() throws IOException
{
   ZipEntry ze = new ZipEntry(S6_CONTEXT_FILE);
   output_jar.putNextEntry(ze);

   IvyXmlWriter xw = new IvyXmlWriter(output_jar);
   xw.begin("CONTEXT");
   xw.field("LANGUAGE","JAVA");
   if (class_paths.size() > 0) xw.field("USEPATH",true);
   xw.field("SEPARATOR",File.separator);

   if (cur_class != null && cur_class.length() == 0) cur_class = null;
   if (cur_package != null && cur_package.length() == 0) cur_package = null;
   if (cur_class != null && cur_package == null) {
      int idx = cur_class.lastIndexOf(".");
      if (idx >= 0) {
	 cur_package = cur_class.substring(0,idx);
	 cur_class = cur_class.substring(idx+1);
       }
    }
   else if (cur_class != null && cur_package != null && cur_class.startsWith(cur_package + ".")) {
      int ln = cur_package.length();
      cur_class = cur_class.substring(ln+1);
    }
   if (cur_package != null) xw.field("PACKAGE",cur_package);
   if (cur_class != null) xw.field("CLASS",cur_class);

   if (import_set.size() > 0) {
      xw.begin("IMPORTS");
      for (String s : import_set) {
	 xw.textElement("IMPORT",s);
       }
      xw.end("IMPORTS");
    }

   for (ContextUserFile uf : data_files) {
      uf.addEntry(xw);
    }

   xw.end();
   xw.flush();

   output_jar.closeEntry();
}




/********************************************************************************/
/*										*/
/*	Manifest methods							*/
/*										*/
/********************************************************************************/

private void handleManifest(File f)
{
   if (!f.exists() || !f.canRead()) return;
   if (f.isDirectory()) return;

   try {
      JarFile jf = new JarFile(f);
      Manifest m = jf.getManifest();
      if (m != null) {
	 mergeManifest(m);
       }
      jf.close();
    }
   catch (IOException e) {
      System.err.println("S6:CONTEXT: Java file must be directory or jar file");
    }
}



private void mergeManifest(Manifest m)
{
   if (output_manifest == null) output_manifest = new Manifest();

   Attributes na = m.getMainAttributes();
   Attributes a = output_manifest.getMainAttributes();
   a.putAll(na);
   Map<String,Attributes> nm = m.getEntries();
   Map<String,Attributes> mm = output_manifest.getEntries();
   for (Map.Entry<String,Attributes> ent : nm.entrySet()) {
      Attributes ma = mm.get(ent.getKey());
      if (ma == null) mm.put(ent.getKey(),ent.getValue());
      else ma.putAll(ent.getValue());
    }
}



/********************************************************************************/
/*										*/
/*	Methods to add files to the context					*/
/*										*/
/********************************************************************************/

private void addToClassContext(File f) throws IOException
{
   if (!f.exists() || !f.canRead()) return;
   if (f.isDirectory()) {
      addDirectoryClassFiles(f,f.getPath());
    }
   else {
      try {
	 JarFile jf = new JarFile(f);
	 System.err.println("WORK ON JAR FILE " + f);
	 addJarFile(jf);
       }
      catch (IOException e) {
	 System.err.println("BAD JAVA FILE " + f + ": " + e);
	 System.exit(1);
       }
    }
}



private void addDirectoryClassFiles(File dir,String pfx) throws IOException
{
   if (dir.isDirectory()) {
      File [] dirf = dir.listFiles();
      if (dirf != null) {
	 for (File f : dirf) {
	    addDirectoryClassFiles(f,pfx);
	  }
       }
    }
   else if (dir.getPath().endsWith(".class")) {
      addSimpleFile(dir,pfx);
    }
}




private void addSimpleFile(File f,String pfx) throws IOException
{
   byte [] buf = new byte[16384];

   System.err.println("FILE ENTRY: " + f.getPath());

   String x = f.getPath();
   if (pfx != null && x.startsWith(pfx)) {
      int i = pfx.length();
      x = x.substring(i);
      if (x.startsWith(File.separator)) x = x.substring(1);
    }

   ZipEntry ze = new ZipEntry(x);
   try {
      output_jar.putNextEntry(ze);
    }
   catch (ZipException ex) {
      System.err.println("DUPLICATE: " + ze.getName());
      return;
    }

   try (BufferedInputStream ins = new BufferedInputStream(new FileInputStream(f))) {
       for ( ; ; ) {
           int ln = ins.read(buf);
           if (ln <= 0) break;
           output_jar.write(buf,0,ln);
        }
    }
   output_jar.closeEntry();
}



private void addJarFile(JarFile jf) throws IOException
{
   byte [] buf = new byte[16384];

   for (Enumeration<JarEntry> e = jf.entries(); e.hasMoreElements(); ) {
      ZipEntry je = e.nextElement();
      if (je.getName().equals("META-INF/MANIFEST.MF")) continue;
      ZipEntry ze = new ZipEntry(je.getName());
      try {
	 output_jar.putNextEntry(ze);
       }
      catch (ZipException ex) {
	 System.err.println("DUPLICATE: " + ze.getName());
	 continue;
       }
      System.err.println("JAR ENTRY: " + ze.getName());
      BufferedInputStream ins = new BufferedInputStream(jf.getInputStream(je));
      for ( ; ; ) {
	 int ln = ins.read(buf);
	 if (ln <= 0) break;
	 output_jar.write(buf,0,ln);
       }
      ins.close();
      output_jar.flush();
      output_jar.closeEntry();
    }
}




private void addUserFile(ContextUserFile uf) throws IOException
{
   System.err.println("USER FILE: " + uf.getFile());

   addToJarFile(uf.getFile(),uf.getJarName());
}




private void addSourceFile() throws IOException
{
   if (source_file == null) return;

   addToJarFile(source_file,S6_CONTEXT_SOURCE);
}




private void addToJarFile(File f,String jnm) throws IOException
{
    byte [] buf = new byte[16384];
    
    if (f.canRead()) {
        ZipEntry ze = new ZipEntry(jnm);
        try {
            output_jar.putNextEntry(ze);
         }
        catch (ZipException ex) {
            System.err.println("DUPLICATE: " + ze.getName());
            return;
         }
        
       try (BufferedInputStream ins = new BufferedInputStream(new FileInputStream(f))) {
           for ( ; ; ) {
               int ln = ins.read(buf);
               if (ln <= 0) break;
               output_jar.write(buf,0,ln);
            }
           output_jar.closeEntry();
        }
     }
}




}	// end of class ContextMain





/* end of ContextMain.java */
