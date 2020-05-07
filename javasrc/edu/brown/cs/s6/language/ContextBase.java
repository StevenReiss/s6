/********************************************************************************/
/*										*/
/*		ContextBase.java						*/
/*										*/
/*	Basic implementation of a S6 context					*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/language/ContextBase.java,v 1.5 2016/07/18 23:05:16 spr Exp $ */


/*********************************************************************************
 *
 * $Log: ContextBase.java,v $
 * Revision 1.5  2016/07/18 23:05:16  spr
 * Update context to take string arg.
 *
 * Revision 1.4  2015/09/23 17:58:03  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.3  2013/09/13 20:32:51  spr
 * Add calls for UI search.
 *
 * Revision 1.2  2009-05-12 22:28:44  spr
 * Fix ups to make user context work.
 *
 * Revision 1.1  2008-11-12 13:53:04  spr
 * Start of context management.
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.language;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;

import org.w3c.dom.Element;

import edu.brown.cs.ivy.file.IvyFile;
import edu.brown.cs.ivy.file.IvyLog;
import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Context;
import edu.brown.cs.s6.common.S6Exception;
import edu.brown.cs.s6.common.S6Request;


public abstract class ContextBase implements S6Context, S6Constants, LanguageConstants {



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private String		file_name;
private JarFile 	jar_file;
private Collection<UserFileImpl> file_set;
private boolean 	use_jar;
private File		context_dir;
private boolean 	extract_done;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

protected ContextBase(String filename)
{
   file_name = filename;
   jar_file = null;
   file_set = null;
   use_jar = true;
   extract_done = false;
}



/********************************************************************************/
/*										*/
/*	File Access methods							*/
/*										*/
/********************************************************************************/

public boolean contains(String name)
{
   checkOpen();

   if (use_jar && jar_file != null && jar_file.getEntry(name) != null) return true;
   return false;
}



public String getSourceFile()
{
   checkOpen();

   InputStream ins = getJarInputStream(S6_CONTEXT_SOURCE);
   if (ins == null) return null;

   StringBuffer rslt = new StringBuffer();

   try {
      BufferedReader br = new BufferedReader(new InputStreamReader(ins));
      for ( ; ; ) {
	 String ln = br.readLine();
	 if (ln == null) break;
	 rslt.append(ln);
	 rslt.append("\n");
       }
      br.close();
    }
   catch (IOException e) {
      IvyLog.logE("LANGUAGE","Problem reading source file: " + e);
    }

   return rslt.toString();
}




public InputStream getInputStream(String name)
{
   checkOpen();

   if (jar_file != null && use_jar) {
      return getJarInputStream(name);
    }

   // This line should be commented out or system files are considered local
   // if (parent_context != null) return parent_context.getInputStream(name);

   return null;
}



public synchronized void close(S6Request rq)
{
   if (jar_file != null) {
      try {
	 jar_file.close();
       }
      catch (IOException e) { }
      jar_file = null;
    }

   if (context_dir != null && !rq.doDebug()) {
      try {
	 IvyFile.remove(context_dir);
       }
      catch (IOException e) { }
      context_dir = null;
    }

}



private InputStream getJarInputStream(String name)
{
   checkOpen();

   if (jar_file != null) {
      ZipEntry ent = jar_file.getEntry(name);
      if (ent != null) {
	 try {
	    return jar_file.getInputStream(ent);
	  }
	 catch (ZipException e) {
	    IvyLog.logE("LANGUAGE","Problem with context zip file " + file_name + ": " + e);
	  }
	 catch (IOException e) {
	   IvyLog.logE("LANGUAGE","Problem opening context jar entry: " + e);
	  }
       }
    }

   return null;
}




/********************************************************************************/
/*										*/
/*	Testing access methods							*/
/*										*/
/********************************************************************************/

public String getJarFileName()
{
   if (use_jar) return file_name;

   return null;
}



public File getContextDirectory() throws S6Exception
{
   extractFiles();

   return context_dir;
}



public Collection<UserFile> getUserFiles() throws S6Exception
{
   extractFiles();

   ArrayList<UserFile> r = new ArrayList<UserFile>();

   if (file_set != null) r.addAll(file_set);

   return r;
}



/********************************************************************************/
/*										*/
/*	File maintenance methods						*/
/*										*/
/********************************************************************************/

protected synchronized void checkOpen()
{
   if (jar_file != null) return;

   if (file_name == null) return;

   try {
      jar_file = new JarFile(file_name);
    }
   catch (IOException e) {
      IvyLog.logE("CONTEXT","Can't open file " + file_name + ": " + e);
      jar_file = null;
      file_name = null;
      return;
    }

   InputStream ins = getJarInputStream(S6_CONTEXT_FILE);
   if (ins == null) return;

   Element xml = IvyXml.loadXmlFromStream(ins);
   try {
      ins.close();
    }
   catch (IOException e) { }

   if (!IvyXml.getAttrBool(xml,"USEPATH")) use_jar = false;

   String usep = IvyXml.getAttrString(xml,"SEPARATOR");

   for (Element e : IvyXml.children(xml,"USERFILE")) {
      UserFileImpl uf = new UserFileImpl(e,usep);
      if (file_set == null) file_set = new ArrayList<UserFileImpl>();
      file_set.add(uf);
    }

   loadLanguageContext(xml);
}


protected void loadLanguageContext(Element e)				{ }



/********************************************************************************/
/*										*/
/*	Setting up the context directory					*/
/*										*/
/********************************************************************************/

private void setupContextDirectory() throws S6Exception
{
   checkOpen();

   if (context_dir != null || file_set == null) return;

   File root = new File(System.getProperty("java.io.tmpdir") + File.separator + S6_CONTEXT_DIR);
   if (!root.exists() && !root.mkdir())
      throw new S6Exception("Can't create S6 test directory: " + root);

   String pkg = null;
   Random r = new Random();
   context_dir = null;
   for (int i = 0; i < 100; ++i) {
      pkg = S6_CONTEXT_PREFIX + r.nextInt(65536);
      context_dir = new File(root.getPath() + File.separator + pkg);
      if (context_dir.exists()) continue;
      if (context_dir.mkdir()) break;
      if (i > 10) {
	 throw new S6Exception("Problem with context directory creation: " + context_dir);
       }
    }
}



private synchronized void extractFiles() throws S6Exception
{
   if (extract_done) return;
   checkOpen();
   extract_done = true;

   if (file_set == null) return;

   setupContextDirectory();
   if (context_dir == null) return;

   byte [] buf = new byte[8192];

   for (UserFileImpl uf : file_set) {
      try {
	 String nm = uf.getLocalName();
	 InputStream ins = getJarInputStream(nm);
	 if (ins == null) continue;
	 File f = new File(context_dir,nm);
	 FileOutputStream ots = new FileOutputStream(f);
	 for ( ; ; ) {
	    int ln = ins.read(buf);
	    if (ln < 0) break;
	    ots.write(buf,0,ln);
	  }
	 ins.close();
	 ots.close();
       }
      catch (IOException e) {
	 throw new S6Exception("S6: Problem copying context files",e);
       }
    }
}




/********************************************************************************/
/*										*/
/*	Class representing user file						*/
/*										*/
/********************************************************************************/

private static class UserFileImpl implements UserFile {

   private String user_name;
   private String jar_name;
   private S6UserFileType file_mode;

   UserFileImpl(Element e,String sep) {
      user_name = IvyXml.getAttrString(e,"NAME");
      jar_name = IvyXml.getAttrString(e,"JARNAME");
      file_mode = IvyXml.getAttrEnum(e,"ACCESS",S6UserFileType.READ);
    }

   public String getLocalName() 		{ return jar_name; }
   public String getUserName()			{ return user_name; }
   public S6UserFileType getFileType()		{ return file_mode; }

}	// end of subclass UserFileImpl



}	// end of class ContextBase




/* end of ContextBase.java */
