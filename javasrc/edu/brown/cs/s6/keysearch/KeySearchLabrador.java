/********************************************************************************/
/*										*/
/*		KeySearchLabrador.java						*/
/*										*/
/*	Keyword-based initial search using labrador search engine		*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/keysearch/KeySearchLabrador.java,v 1.11 2016/07/18 23:05:04 spr Exp $ */


/*********************************************************************************
 *
 * $Log: KeySearchLabrador.java,v $
 * Revision 1.11  2016/07/18 23:05:04  spr
 * Keysearch updates for applications, ui search.
 *
 * Revision 1.10  2015/09/23 17:57:59  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.9  2014/02/26 14:06:23  spr
 * Monifications to key search applied to local search.
 *
 * Revision 1.8  2013/09/13 20:32:32  spr
 * Add calls for UI search.
 *
 * Revision 1.7  2012-07-20 22:15:02  spr
 * Cleaup code.
 *
 * Revision 1.6  2012-06-20 12:21:28  spr
 * Initial fixes for UI search
 *
 * Revision 1.4  2012-06-11 14:07:30  spr
 * Code cleanup
 *
 * Revision 1.3  2008-11-12 13:51:37  spr
 * Performance and bug updates.
 *
 * Revision 1.2  2008-08-28 00:32:52  spr
 * Next version of S6.	Lots of bug fixes, some new functionality.
 *
 * Revision 1.1  2008-07-17 13:46:29  spr
 * Add labrador-based searching.
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.keysearch;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.Future;

import edu.brown.cs.cose.cosecommon.CoseSource;
import edu.brown.cs.ivy.exec.IvyExec;
import edu.brown.cs.ivy.file.IvyFile;
import edu.brown.cs.s6.common.S6Exception;
import edu.brown.cs.s6.common.S6Fragment;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6SolutionSet;


class KeySearchLabrador extends KeySearchBase {



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private final static String    SOURCE_PREFIX = "LOCAL:";

private final static String    LABRADOR_CMD = IvyFile.expandName("$(S6)/bin/labradorquery.sh");
private final static String    LABRADOR_JAVA = "ext:java";

private final static int	MAX_FILES = 150;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

KeySearchLabrador()
{ }


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

      cmd.append(LABRADOR_CMD);
      cmd.append(" ");
      int ct = 0;
      cmd.append("\"");
      for (String s : kws.getWords()) {
	 if (ct++ > 0) cmd.append(" AND ");
	 cmd.append(s);
       }
      cmd.append(" AND ");
      cmd.append(LABRADOR_JAVA);
      cmd.append("\"");
      System.err.println("S6: KEYSEARCH: " + cmd.toString());
      RunLabrador rb = new RunLabrador(ss,cmd.toString(),null,waitq);
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

   switch (ss.getSearchType()) {
      case PACKAGE :
      case UIFRAMEWORK :
      case ANDROIDUI :
      case APPLICATION :
	 String k = src;
	 int idx = src.lastIndexOf("/");
	 if (idx >= 0) k = src.substring(0,idx);
	 buildPackageFragment(ss,src,k,wq,0);
	 break;
      default :
	 LoadFile ls = new LoadFile(ss,src,0,null);
	 Future<Boolean> fb = ss.getEngine().executeTask(S6TaskType.IO,ls);
	 synchronized (wq) {
	    wq.add(fb);
	  }
	 break;
    }
}


protected CoseSource createPackageSource(String id,int priority)
{
   return new LabradorSource(id,id,priority);
}


protected boolean addPackages(S6SolutionSet ss,S6Fragment frag,CoseSource src,Set<String> pkgs,Queue<Future<Boolean>> wq)
{
   boolean chng = false;

   String pbase = frag.getBasePackage();
   File fsrc = new File(src.getDisplayName());

   File udir = fsrc.getParentFile();
   StringTokenizer tok = new StringTokenizer(pbase,".");
   while (tok.hasMoreTokens()) {
      tok.nextToken();
      udir = udir.getParentFile();
    }

   for (String pkg : pkgs) {
      tok = new StringTokenizer(pkg,".");
      File nudir = udir;
      while (tok.hasMoreTokens()) {
	 nudir = new File(nudir,tok.nextToken());
       }
      String ndir = nudir.getPath();
      if (ndir != null) {
	 try {
	    String s = loadURI(new URI(ndir + "/"),true);
	    if (s != null) {
	       tok = new StringTokenizer(s);
	       while (tok.hasMoreTokens()) {
		  String fnm = tok.nextToken();
		  if (fnm.endsWith(".java")) {
		     String nnm = ndir + "/" + fnm;
		     LoadFile lf = new LoadFile(ss,nnm,(int) src.getScore(),frag);
		     Future<Boolean> fb = ss.getEngine().executeTask(S6TaskType.IO,lf);
		     synchronized (wq) {
			wq.add(fb);
			chng = true;
		      }
		   }
		}
	     }
	  }
	 catch (Exception e) { }
       }
    }

   return chng;
}



protected void queuePackageSolutions(S6SolutionSet ss,String id,Queue<Future<Boolean>> wq,S6Fragment pf,int priority) throws S6Exception
{
   if (ss.getScopeType() == CoseScopeType.FILE) {
      LoadFile ls = new LoadFile(ss,id,priority,pf);
      Future<Boolean> fb = ss.getEngine().executeTask(S6TaskType.IO,ls);
      synchronized (wq) {
	 wq.add(fb);
       }
      return;
    }


   int idx = id.lastIndexOf("/");
   if (idx < 0) return;
   List<String> dirs = new ArrayList<String>();
   String dir0 = id.substring(0,idx);
   dirs.add(dir0);

   try {
      for (String dir : dirs) {
	 String s = loadURI(new URI(dir + "/"),false);
	 if (s == null) continue;
	 StringTokenizer tok = new StringTokenizer(s);
	 while (tok.hasMoreTokens()) {
	    String fnm = tok.nextToken();
	    if (fnm.endsWith(".java")) {
	       String nnm = dir + "/" + fnm;
	       LoadFile ls = new LoadFile(ss,nnm,priority,pf);
	       Future<Boolean> fb = ss.getEngine().executeTask(S6TaskType.IO,ls);
	       synchronized (wq) {
		  wq.add(fb);
		}
	     }
	  }
       }
    }
   catch (URISyntaxException e) {
      throw new S6Exception("Not a directory URI");
    }
}




/********************************************************************************/
/*										*/
/*	RunLabrador -- run labrador command to get results			*/
/*										*/
/********************************************************************************/

private class RunLabrador implements Runnable {

   private S6SolutionSet solution_set;
   private String labrador_cmd;
   private Queue<Future<Boolean>> wait_queue;

   RunLabrador(S6SolutionSet ss,String cmd,S6Fragment pf,Queue<Future<Boolean>> waitq) {
      solution_set = ss;
      wait_queue = waitq;
      labrador_cmd = cmd;
    }

   public void run() {
      if (solution_set.doDebug()) System.err.println("S6: KEYSEARCH: LABRADOR: " + labrador_cmd);
      try {
         IvyExec ex = new IvyExec(labrador_cmd,IvyExec.READ_OUTPUT);
         InputStream ins = ex.getInputStream();
         BufferedReader br = new BufferedReader(new InputStreamReader(ins));
         for (int i = 0; i < MAX_FILES; ++i) {
            String s = br.readLine();
            if (s == null) break;
            System.err.println("S6: KEYSEARCH: READ: " + s);
            switch (solution_set.getSearchType()) {
               case UIFRAMEWORK :
               case PACKAGE :
               case ANDROIDUI :
               case APPLICATION :
                  int idx = s.lastIndexOf("/");
                  if (idx >= 0) {
                     String k = s.substring(0,idx);
                     buildPackageFragment(solution_set,s,k,wait_queue,i);
                   }
                  break;
               default :
                  LoadFile ls = new LoadFile(solution_set,s,i,null);
                  Future<Boolean> fb = solution_set.getEngine().executeTask(S6TaskType.IO,ls);
                  synchronized (wait_queue) {
                     wait_queue.add(fb);
                   }
             }
          }
         br.close();
       }
      catch (IOException e) {
         System.err.println("S6: KEYSEARCH: Problem running labrador query: " + e);
       }
    }

}	// end of subclass RunLabrador




/********************************************************************************/
/*										*/
/*	LoadFile -- load file/url given by labrador				*/
/*										*/
/********************************************************************************/

private static class LoadFile implements Runnable {

   private S6SolutionSet solution_set;
   private String for_item;
   private int item_index;
   private S6Fragment package_fragment;

   LoadFile(S6SolutionSet ss,String itm,int idx,S6Fragment pf) {
      solution_set = ss;
      for_item = itm;
      item_index = idx;
      package_fragment = pf;
    }

   public void run() {
      try {
         String s = loadURI(new URI(for_item),false);
         if (s == null) return;
   
         LabradorSource bs = new LabradorSource(for_item,s,item_index);
         KeySearchBase.getSolutions(solution_set,s,bs,package_fragment);
       }
      catch (URISyntaxException e) {
         // extra output lines generally end up here -- they can be ignored
         // System.err.println("S6: KEYSEARCH: Problem with labrador name: " + e);
       }
      catch (S6Exception e) {
         System.err.println("S6: KEYSEARCH: Problem loading file: " + e);
       }
    }

}	// end of subclass LoadFile




/********************************************************************************/
/*										*/
/*	Class to hold a labrador-related source 				*/
/*										*/
/********************************************************************************/

private static class LabradorSource extends KeySearchSource implements CoseSource {

   private String file_name;

   LabradorSource(String file,String code,int idx) {
      super(code,idx);
      file_name = file;
    }

   public String getName()		{ return SOURCE_PREFIX + file_name; }

   public String getDisplayName()	{ return file_name; }

}	// end of subclass LabradorSource



}	// end of class KeySearchLabrador



/* end of KeySearchLabrador.java */
