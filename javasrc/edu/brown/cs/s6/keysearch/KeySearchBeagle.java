/********************************************************************************/
/*										*/
/*		KeySearchBeagle.java						*/
/*										*/
/*	Keyword-based initial search using beagle search engine 		*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/keysearch/KeySearchBeagle.java,v 1.6 2015/09/23 17:57:58 spr Exp $ */


/*********************************************************************************
 *
 * $Log: KeySearchBeagle.java,v $
 * Revision 1.6  2015/09/23 17:57:58  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.5  2013/09/13 20:32:31  spr
 * Add calls for UI search.
 *
 * Revision 1.4  2012-06-11 14:07:30  spr
 * Code cleanup
 *
 * Revision 1.3  2008-11-12 13:51:37  spr
 * Performance and bug updates.
 *
 * Revision 1.2  2008-06-12 17:47:50  spr
 * Next version of S6.
 *
 * Revision 1.1.1.1  2008-06-03 12:59:22  spr
 * Initial version of S6
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.keysearch;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Queue;
import java.util.StringTokenizer;
import java.util.concurrent.Future;

import edu.brown.cs.cose.cosecommon.CoseSource;
import edu.brown.cs.ivy.exec.IvyExec;
import edu.brown.cs.s6.common.S6Exception;
import edu.brown.cs.s6.common.S6Fragment;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6SolutionSet;


class KeySearchBeagle extends KeySearchBase {



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private String beagle_host;

private final static String    BEAGLE_HOST = "fred3";
private final static String    BEAGLE_CMD = "beagle-query";
private final static String    BEAGLE_JAVA = "ext:java";

private final static String    SOURCE_PREFIX = "BEAGLE:";




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

KeySearchBeagle()
{
   beagle_host = BEAGLE_HOST;
   // beagle_host = null;
}


/********************************************************************************/
/*										*/
/*	Search method								*/
/*										*/
/********************************************************************************/

protected void queueInitialSolutions(S6SolutionSet ss,int tgtct,Queue<Future<Boolean>> waitq) throws S6Exception
{
   S6Request.Search sr = ss.getRequest();

   for (S6Request.KeywordSet kws : sr.getKeywordSets()) {
      StringBuffer cmd = new StringBuffer();

      if (beagle_host != null) {
	 cmd.append("ssh ");
	 cmd.append(beagle_host);
	 cmd.append(" ");
       }
      cmd.append(BEAGLE_CMD);
      cmd.append(" ");
      cmd.append(BEAGLE_JAVA);
      for (String s : kws.getWords()) {
	 StringTokenizer tok = new StringTokenizer(s,". \t");
	 while (tok.hasMoreTokens()) {
	    String st = tok.nextToken();
	    cmd.append(" '");
	    cmd.append(st);
	    cmd.append("'");
	  }
       }
      RunBeagle rb = new RunBeagle(ss,cmd.toString(),waitq);
      Future<Boolean> fb = ss.getEngine().executeTask(S6TaskType.IO,rb);
      synchronized (waitq) {
	 waitq.add(fb);
       }
    }
}



protected void queueSpecificSolution(S6SolutionSet ss,String src,Queue<Future<Boolean>> wq)
{
   if (!src.startsWith(SOURCE_PREFIX)) return;
   src = src.substring(SOURCE_PREFIX.length());

   LoadFile ls = new LoadFile(ss,src,0);
   Future<Boolean> fb = ss.getEngine().executeTask(S6TaskType.IO,ls);
   synchronized (wq) {
      wq.add(fb);
    }
}


protected void queuePackageSolutions(S6SolutionSet ss,String id,Queue<Future<Boolean>> wq,S6Fragment pfrag,int priority)
{
}


/********************************************************************************/
/*										*/
/*	RunBeagle -- run beagle command to get results				*/
/*										*/
/********************************************************************************/

private static class RunBeagle implements Runnable {

   private S6SolutionSet solution_set;
   private String beagle_cmd;
   private Queue<Future<Boolean>> wait_queue;

   RunBeagle(S6SolutionSet ss,String cmd,Queue<Future<Boolean>> waitq) {
      solution_set = ss;
      wait_queue = waitq;
      beagle_cmd = cmd;
    }

   public void run() {
      System.err.println("S6: KEYSEARCH: BEAGLE: " + beagle_cmd);
      try {
	 IvyExec ex = new IvyExec(beagle_cmd,IvyExec.READ_OUTPUT);
	 InputStream ins = ex.getInputStream();
	 BufferedReader br = new BufferedReader(new InputStreamReader(ins));
	 for (int i = 0; ; ++i) {
	    String s = br.readLine();
	    if (s == null) break;
	    LoadFile ls = new LoadFile(solution_set,s,i);
	    Future<Boolean> fb = solution_set.getEngine().executeTask(S6TaskType.IO,ls);
	    synchronized (wait_queue) {
	       wait_queue.add(fb);
	     }
	  }
	 br.close();
       }
      catch (IOException e) {
	 System.err.println("S6: KEYSEARCH: Problem running beagle query: " + e);
       }
    }

}	// end of subclass RunBeagle




/********************************************************************************/
/*										*/
/*	LoadFile -- load file/url given by beagle				*/
/*										*/
/********************************************************************************/

private static class LoadFile implements Runnable {

   private S6SolutionSet solution_set;
   private String for_item;
   private int item_index;

   LoadFile(S6SolutionSet ss,String itm,int idx) {
      solution_set = ss;
      for_item = itm;
      item_index = idx;
    }

   public void run() {
      try {
         String s = loadURI(new URI(for_item),false);
         BeagleSource bs = new BeagleSource(for_item,s,item_index);
         KeySearchBase.getSolutions(solution_set,s,bs,null);
       }
      catch (URISyntaxException e) {
         // extra output lines generally end up here -- they can be ignored
         // System.err.println("S6: KEYSEARCH: Problem with beagle name: " + e);
       }
      catch (S6Exception e) {
         System.err.println("S6: KEYSEARCH: Problem loading file: " + e);
       }
    }

}	// end of subclass LoadFile




/********************************************************************************/
/*										*/
/*	Class to hold a beagle-related source					*/
/*										*/
/********************************************************************************/

private static class BeagleSource extends KeySearchSource implements CoseSource {

   private String file_name;

   BeagleSource(String file,String code,int idx) {
      super(code,idx);
      file_name = file;
    }

   public String getName()		{ return SOURCE_PREFIX + file_name; }

   public String getDisplayName()	{ return file_name; }

}	// end of subclass BeagleSource



}	// end of class KeySearchBeagle



/* end of KeySearchBeagle.java */
