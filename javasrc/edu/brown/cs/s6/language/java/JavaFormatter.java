/********************************************************************************/
/*										*/
/*		JavaFormatter.java						*/
/*										*/
/*	Class to handle reformatting code blocks				*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/language/java/JavaFormatter.java,v 1.7 2015/09/23 17:54:52 spr Exp $ */


/*********************************************************************************
 *
 * $Log: JavaFormatter.java,v $
 * Revision 1.7  2015/09/23 17:54:52  spr
 * Version to handle andriod UI applications.
 *
 * Revision 1.6  2014/08/29 15:16:08  spr
 * Updates for suise, testcases.
 *
 * Revision 1.5  2013-05-09 12:26:19  spr
 * Minor changes to start ui fixups.
 *
 * Revision 1.4  2012-06-11 14:07:49  spr
 * add framework search; fix bugs
 *
 * Revision 1.3  2009-09-18 01:41:35  spr
 * Handle user testing.
 *
 * Revision 1.2  2008-11-12 13:52:14  spr
 * Performance and bug updates.
 *
 * Revision 1.1  2008-08-28 00:32:56  spr
 * Next version of S6.	Lots of bug fixes, some new functionality.
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.language.java;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Element;

import edu.brown.cs.ivy.exec.IvyExec;
import edu.brown.cs.ivy.file.IvyFile;
import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.ivy.xml.IvyXmlReader;
import edu.brown.cs.ivy.xml.IvyXmlWriter;
import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Exception;


class JavaFormatter implements S6Constants, JavaConstants {



private S6SearchType	search_type;
private S6FormatType	format_type;
private Map<String,File> file_map;
private Socket		use_server;
private IvyXmlWriter	server_command;



private static final String METHOD_PREFIX = "class S6Format {\n/* FORMATTING START */\n";
private static final String METHOD_SUFFIX = "/* FORMATTING END */\n}\n";

private static final Pattern METHOD_PATTERN = Pattern.compile(
   ".*\\Q/* FORMATTING START */\\E(.*)\\Q/* FORMATTING END */\\E",
   Pattern.DOTALL|Pattern.MULTILINE);




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

JavaFormatter(LanguageJava lj,S6SearchType st,S6FormatType ft)
{
   search_type = st;
   format_type = ft;
   file_map = new HashMap<String,File>();
   use_server = null;
   server_command = null;

   int port = 0;
   switch (format_type) {
      default :
      case BROWN :
	 port = S6_FORMAT_PORT_BROWN;
	 break;
      case ECLIPSE :
	 port = S6_FORMAT_PORT_ECLIPSE;
	 break;
      case SUN :
	 port = S6_FORMAT_PORT_SUN;
	 break;
    }

   if (port != 0) {
      try {
	 use_server = new Socket(S6_FORMAT_HOST,port);
	 server_command = new IvyXmlWriter(use_server.getOutputStream());
	 server_command.begin("FILERQS");
       }
      catch (IOException e) { }
    }
}




/********************************************************************************/
/*										*/
/*     Methods	to set up files 						*/
/*										*/
/********************************************************************************/

void addFile(String id,String txt) throws S6Exception
{
   if (search_type == S6SearchType.METHOD) {
      txt = METHOD_PREFIX + txt + METHOD_SUFFIX;
    }

   try {
      File f = File.createTempFile("s6format",".java",S6_FORMAT_DIR);
      FileWriter fw = new FileWriter(f);
      fw.write(txt);
      fw.close();
      file_map.put(id,f);
      if (use_server != null) {
	 String fnm = f.getPath();
	 server_command.begin("FILERQ");
	 server_command.field("INPUT",fnm);
	 server_command.field("OUTPUT",fnm + ".out");
	 server_command.end("FILERQ");
       }
    }
   catch (IOException e) {
      throw new S6Exception("S6: FORMAT: Problem with formatting: " + e);
    }
}



/********************************************************************************/
/*										*/
/*	Methods to do reformatting						*/
/*										*/
/********************************************************************************/

boolean reformatAll() throws S6Exception
{
   if (use_server != null) {
      try {
	 return handleServer();
       }
      catch (IOException e) {
	 throw new S6Exception("S6: FORMAT: Problem with formatting: " + e);
       }
    }

   String cmd = IvyFile.expandName("$(S6)/language/src/java/reformat.csh");

   String which = null;
   switch (format_type) {
      default :
      case BROWN :
	 which = "spr";
	 break;
      case ECLIPSE :
	 which = "eclipse";
	 break;
      case SUN :
	 which = "sun";
	 break;
    }

   if (which != null) cmd += " -n " + which;

   for (File f : file_map.values()) {
      cmd += " " + f.getPath();
    }

   System.err.println("S6: FORMAT: RUN: " + cmd);

   IvyExec ex = null;

   try {
      ex = new IvyExec(cmd,IvyExec.IGNORE_OUTPUT);
    }
   catch (IOException e) {
      throw new S6Exception("S6: FORMAT: Problem running formatter: " + e);
    }

   int sts = ex.waitFor();

   return sts == 0;
}



boolean handleServer() throws IOException
{
   server_command.end("FILERQS");
   server_command.flush();

   IvyXmlReader xr = new IvyXmlReader(use_server.getInputStream());
   String msg = xr.readXml();
   xr.close();

   Element elt = IvyXml.convertStringToXml(msg);

   use_server.close();
   use_server = null;

   if (IvyXml.isElement(elt,"ERROR")) {
      System.err.println("S6: FORMATING ERROR: " + msg);
      return false;
    }

   return true;
}



/********************************************************************************/
/*										*/
/*	Methods to retrieve data						*/
/*										*/
/********************************************************************************/

String getText(String id) throws S6Exception
{
   // System.err.println("HANDLE FORMAT " + id + " " + file_map.get(id));

   File f = file_map.get(id);
   if (f == null) return null;

   File f1 = new File(f.getPath() + ".out");
   if (!f1.exists()) return null;

   StringBuilder buf = new StringBuilder();

   try {
      BufferedReader br = new BufferedReader(new FileReader(f1));
      for ( ; ; ) {
	 String ln = br.readLine();
	 if (ln == null) break;
	 buf.append(ln);
	 buf.append("\n");
       }
      br.close();
      f1.delete();
    }
   catch (IOException e ) {
      throw new S6Exception("S6: FORMAT: Problem reading result: " + e);
    }

   String rslt = null;

   switch (search_type) {
      case CLASS :
      case FULLCLASS :
	 rslt = buf.toString();
	 break;
      case METHOD :
	 Matcher m = METHOD_PATTERN.matcher(buf);
	 if (m.find()) {
	    rslt = m.group(1);
	  }
	 break;
      default :
         break;    }

   return rslt;
}



/********************************************************************************/
/*										*/
/*	Methods to clean up							*/
/*										*/
/********************************************************************************/

void cleanup()
{
   for (File f : file_map.values()) {
      if (f.exists()) f.delete();
      File f1 = new File(f.getPath() + ".out");
      if (f1.exists()) f1.delete();
    }
}




}	// end of class JavaFormatter



/* end of JavaFormatter.java */
