/********************************************************************************/
/*										*/
/*		EngineFinder.java						*/
/*										*/
/*	Main program for S6 syntax satellite def-finder application		*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/engine/EngineFinder.java,v 1.9 2015/12/23 15:44:48 spr Exp $ */


/*********************************************************************************
 *
 * $Log: EngineFinder.java,v $
 * Revision 1.9  2015/12/23 15:44:48  spr
 * Minor fixes.
 *
 * Revision 1.8  2015/09/23 17:57:55  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.7  2012-07-20 22:14:52  spr
 * Additions for UI search
 *
 * Revision 1.6  2012-06-11 14:07:29  spr
 * Code cleanup
 *
 * Revision 1.5  2009-05-12 22:27:22  spr
 * Add server to finder.  Fix up firewall recovery.  Fix max thread setting.
 *
 * Revision 1.4  2008-11-12 13:51:31  spr
 * Performance and bug updates.
 *
 * Revision 1.3  2008-08-28 00:32:49  spr
 * Next version of S6.	Lots of bug fixes, some new functionality.
 *
 * Revision 1.2  2008-06-12 17:47:48  spr
 * Next version of S6.
 *
 * Revision 1.1.1.1  2008-06-03 12:59:21  spr
 * Initial version of S6
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.engine;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.w3c.dom.Element;

import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.ivy.xml.IvyXmlReaderThread;
import edu.brown.cs.ivy.xml.IvyXmlWriter;
import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Engine;
import edu.brown.cs.s6.common.S6Exception;
import edu.brown.cs.s6.common.S6Factory;
import edu.brown.cs.s6.common.S6FileLocation;
import edu.brown.cs.s6.common.S6Fragment;
import edu.brown.cs.s6.common.S6Language;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6Source;



public class EngineFinder implements S6Engine, S6Constants, EngineConstants {



/********************************************************************************/
/*										*/
/*	Main program								*/
/*										*/
/********************************************************************************/

public static void main(String [] args)
{
   EngineFinder em = new EngineFinder(args);

   em.start();
}



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private S6Factory	s6_factory;
private S6Language	s6_language;
private int		port_number;
private Map<String,FinderFile> file_set;
private boolean 	force_compile;
private boolean 	run_server;

private final String LANGUAGE_NAME = "java";



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

EngineFinder(String [] args)
{
   port_number = 0;
   file_set = new HashMap<String,FinderFile>();
   force_compile = true;
   run_server = false;

   scanArgs(args);

   s6_factory = S6Factory.createS6Factory();
   s6_language = s6_factory.createLanguage(this,LANGUAGE_NAME);
}




/********************************************************************************/
/*										*/
/*	Argument scanning methods						*/
/*										*/
/********************************************************************************/

private void scanArgs(String [] args)
{
   for (int i = 0; i < args.length; ++i) {
      int idx = args[i].indexOf("=");
      if (idx >= 0) {
	 String what = args[i].substring(0,idx);
	 args[i].substring(idx+1);
	 if (what.startsWith("p") || what.startsWith("-p")) {
	    try {
	       port_number = Integer.parseInt(args[++i]);
	     }
	    catch (NumberFormatException e) {
	       badArgs();
	     }
	  }
	 else badArgs();
       }
      else if (args[i].startsWith("-")) {
	 if (args[i].startsWith("-p") && i+1 < args.length) {           // -port <port>
	    try {
	       port_number = Integer.parseInt(args[++i]);
	     }
	    catch (NumberFormatException e) {
	       badArgs();
	     }
	  }
	 else if (args[i].startsWith("-s")) {                           // -server
	    run_server = true;
	  }
       }
      else {
	 badArgs();
       }
    }
}




private void badArgs()
{
   System.err.println("S6: FINDER: s6finder [-port <port>]");
   System.exit(1);
}




/********************************************************************************/
/*										*/
/*	Processing methods							*/
/*										*/
/********************************************************************************/

void start()
{
   FinderServer es = new FinderServer(run_server);
   es.start();
}



private void createClient(Socket s)
{
   try {
      FinderClient c = new FinderClient(s);
      c.start();
    }
   catch (IOException e) {
      System.err.println("S6: ENGINE: Problem create server client: " + e);
    }
}



/********************************************************************************/
/*										*/
/*	Command processing methods						*/
/*										*/
/********************************************************************************/

private boolean handleCommand(Element xml,IvyXmlWriter xw) throws S6Exception
{
   if (IvyXml.isElement(xml,"Initialize")) {
      file_set.clear();
      xw.emptyElement("Success");
    }
   else if (IvyXml.isElement(xml,"LoadFile")) {
      String nm = IvyXml.getTextElement(xml,"name");
      FinderFile ff = file_set.get(nm);
      if (ff == null) {
	 ff = new FinderFile(nm);
	 file_set.put(nm,ff);
       }
      xw.emptyElement("Success");
    }
   else if (IvyXml.isElement(xml,"RemoveFile")) {
      String nm = IvyXml.getTextElement(xml,"name");
      if (file_set.remove(nm) == null) xw.emptyElement("Failure");
      else {
	 force_compile = true;
	 xw.emptyElement("Success");
       }
    }
   else if (IvyXml.isElement(xml,"FindFunctionDefinition")) {
      handleRequest(xml,xw,true);
    }
   else if (IvyXml.isElement(xml,"FindFunctionReferences")) {
      handleRequest(xml,xw,false);
    }
   else if (IvyXml.isElement(xml,"GetAllNames")) {
      handleListNames(xml,xw);
    }
   else if (IvyXml.isElement(xml,"Exit")) {
      return true;
    }

   return false;
}



private void handleRequest(Element xml,IvyXmlWriter xw,boolean def) throws S6Exception
{
   Element se = IvyXml.getElementByTag(xml,"FileName");
   if (se == null) throw new S6Exception("No FileName for find request");
   String fnm = IvyXml.getTextElement(se,"value");
   FinderFile ff = file_set.get(fnm);
   if (ff == null) throw new S6Exception("File " + fnm + " not previously loaded");
   se = IvyXml.getElementByTag(xml,"FileCharOffset");
   int sto = IvyXml.getAttrInt(se,"start");
   int eno = IvyXml.getAttrInt(se,"end");

   compileAll();

   List<S6Fragment> frags = new ArrayList<S6Fragment>();
   for (FinderFile xff : file_set.values()) {
      S6Fragment sf = xff.getFragment();
      if (sf != null) frags.add(sf);
    }

   Collection<S6FileLocation> locs = null;
   S6Fragment sf = ff.getFragment();
   if (sf != null) locs = s6_language.findAll(frags,sf,sto,eno,def);

   if (locs == null || locs.isEmpty()) xw.emptyElement("NoResultsFound");
   else {
      xw.begin("ResultsFound");
      for (S6FileLocation floc : locs) {
	 int soff = floc.getStartOffset();
	 int eoff = floc.getEndOffset();
	 String fn = floc.getFileName();
	 FinderFile lff = file_set.get(fn);
	 if (lff != null) {
	    xw.begin("Match");
	    xw.field("FileName",fn);
	    xw.field("FileCharOffsetStart",soff);
	    xw.field("FileCharOffsetEnd",eoff);
	    xw.end("Match");
	  }
       }
      xw.end("ResultsFound");
    }
}



private void handleListNames(Element xml,IvyXmlWriter xw) throws S6Exception
{
   compileAll();

   xw.begin("Names");

   String fl = IvyXml.getAttrString(xml,"File");
   if (fl == null) {
      for (FinderFile xff : file_set.values()) {
	 S6Fragment sf = xff.getFragment();
	 if (sf != null) s6_language.listDefinitions(sf,xff.getFile().getPath(),xw);
       }
    }
   else {
      FinderFile ff = file_set.get(fl);
      if (ff == null) throw new S6Exception("File " + fl + " not defined");
      s6_language.listDefinitions(ff.getFragment(),ff.getFile().getPath(),xw);
    }

   xw.end("Names");
}




/********************************************************************************/
/*										*/
/*	Methods to handle compilation						*/
/*										*/
/********************************************************************************/

private void compileAll()
{
   boolean upd = false;
   for (FinderFile ff : file_set.values()) {
      if (ff.needsUpdate()) upd = true;
    }

   if (!upd && !force_compile) return;

   List<S6Fragment> frags = new ArrayList<S6Fragment>();
   for (FinderFile ff : file_set.values()) {
      S6Fragment sf = ff.getFragment();
      if (sf != null) frags.add(sf);
    }

   s6_language.resolveAll(frags);

   force_compile = false;
}




/********************************************************************************/
/*										*/
/*	Engine methods								*/
/*										*/
/********************************************************************************/

public Future<Boolean> executeTask(S6TaskType tt,Callable<Boolean> c)
{
   System.err.println("S6: FINDER: attempt to start task in finder");

   return null;
}


public Future<Boolean> executeTask(S6TaskType tt,Runnable r)
{
   System.err.println("S6: FINDER: attempt to start task in finder");

   return null;
}



public S6Fragment createFileFragment(String text,S6Source src,S6Request.Search rq)
{
   return s6_language.createFileFragment(text,src,rq);
}


public Set<String> getRelatedProjects(S6Fragment src)
{
   return new HashSet<String>();
}


public Set<String> getUsedProjects(S6Fragment src)
{
   return new HashSet<String>();
}


public S6Fragment createPackageFragment(S6Request.Search sr)
{
   return s6_language.createPackageFragment(sr);
}


public String handleSearchRequest(Element xml) throws S6Exception
{
   throw new S6Exception("Finder can't handle search requests");
}



public String handleCheckRequest(Element xml) throws S6Exception
{
   throw new S6Exception("Finder can't handle search requests");
}



public boolean waitForAll(Queue<Future<Boolean>> waitq)
{
   return false;
}


public boolean doDebug()			{ return false; }



/********************************************************************************/
/*										*/
/*	Server socket management						*/
/*										*/
/********************************************************************************/

private class FinderServer extends Thread {

   private ServerSocket server_socket;
   private boolean as_server;

   FinderServer(boolean server) {
      super("S6_FINDER_ACCEPT");
      as_server = server;
      try {
	 server_socket = new ServerSocket(port_number);
       }
      catch (IOException e) {
	 System.err.println("S6: FINDER: Problem creating server socket: " + e);
	 System.exit(1);
       }
    }

   public void run() {
      System.err.println("S6: FINDER: Engine running on port " + server_socket.getLocalPort() + " " + as_server);

      for ( ; ; ) {
	 try {
	    Socket s = server_socket.accept();
	    createClient(s);
	  }
	 catch (IOException e) {
	    System.err.println("S6: FINDER: Problem with server socket accept: " + e);
	    break;
	  }
	 if (!as_server) break;
       }
    }

}	// end of subclass FinderServer




/********************************************************************************/
/*										*/
/*	Client socket management						*/
/*										*/
/********************************************************************************/

private class FinderClient extends IvyXmlReaderThread {

  private Socket client_socket;
  private IvyXmlWriter xml_writer;

  FinderClient(Socket s) throws IOException {
     super("FinderClient_" + s.getRemoteSocketAddress(),
	      new InputStreamReader(s.getInputStream()));
     client_socket = s;
     xml_writer = new IvyXmlWriter(s.getOutputStream());
     xml_writer.setSingleLine(true);
   }

  protected void processXmlMessage(String msg) {
     Element xml = IvyXml.convertStringToXml(msg,true);
     try {
	boolean done = handleCommand(xml,xml_writer);
	if (done) {
	   try {
	      client_socket.close();
	    }
	   catch (IOException e) { }
	 }
      }
     catch (S6Exception e) {
	xml_writer.textElement("Problem",e.toString());
      }
     catch (Throwable t) {
	xml_writer.textElement("Error",t.toString());
	t.printStackTrace();
      }
     xml_writer.println();
     xml_writer.flush();
   }

  protected synchronized void processDone() {
     if (client_socket == null) return;
     try {
	client_socket.close();
	client_socket = null;
      }
     catch (IOException e) { }
   }

  protected void processIoError(IOException e) {
     // System.err.println("S6: FINDER: XML reader error for " + getName() + ": " + e);
     try {
	client_socket.close();
      }
     catch (IOException x) { }
   }

}	// end of subclass FinderClient



/********************************************************************************/
/*										*/
/*	FinderFile -- information about a user file				*/
/*										*/
/********************************************************************************/

private class FinderFile {

   private File file_name;
   private long last_modified;
   private S6Fragment file_fragment;

   FinderFile(String nm) {
      file_name = new File(nm);
      last_modified = file_name.lastModified();
      file_fragment = null;
    }

   File getFile()			{ return file_name; }

   boolean needsUpdate() {
      if (file_fragment == null) return true;
      if (file_name.lastModified() != last_modified) {
	 file_fragment = null;
	 return true;
       }

      return false;
    }

   S6Fragment getFragment() {
      if (file_fragment == null) {
         StringBuilder buf = new StringBuilder();
         try {
            BufferedReader br = new BufferedReader(new FileReader(file_name));
            for ( ; ; ) {
               String ln = br.readLine();
               if (ln == null) break;
               buf.append(ln);
               buf.append("\n");
             }
            br.close();
          }
         catch (IOException e) {
            System.err.println("FINDER: Problem reading file " + file_name + ": " + e);
            return null;
          }
         FinderSource src = new FinderSource(file_name.toString());
         file_fragment = createFileFragment(buf.toString(),src,null);
       }
      return file_fragment;
    }

}	// end of subclass FinderFile


/********************************************************************************/
/*										*/
/*	Finder source -- source for a finder file				*/
/*										*/
/********************************************************************************/

private static class FinderSource implements S6Source {

   private String file_name;

   FinderSource(String file) {
      file_name = file;
    }

   public String getName()				{ return file_name; }
   public String getDisplayName()			{ return file_name; }
   public String getPathName()                          { return file_name; }
   public String getLicenseUid()			{ return null; }
   public double getScore()				{ return 1; }
   public String getProjectId()                         { return null; }

}	// end of subclass FinderSource





}	// end of class EngineFinder




/* end of EngineFinder.java */
