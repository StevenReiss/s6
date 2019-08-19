/********************************************************************************/
/*										*/
/*		KeySearchSourcerer.java 					*/
/*										*/
/*	Keyword-based initial search using UCI Sourcerer			*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/keysearch/KeySearchSourcerer.java,v 1.4 2015/12/23 15:45:01 spr Exp $ */


/*********************************************************************************
 *
 * $Log: KeySearchSourcerer.java,v $
 * Revision 1.4  2015/12/23 15:45:01  spr
 * Update search (for UI at least)
 *
 * Revision 1.3  2015/09/23 17:58:00  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.2  2013/09/13 20:32:32  spr
 * Add calls for UI search.
 *
 * Revision 1.1  2012-06-11 18:41:07  spr
 * Add sourcer search enginer.
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.keysearch;


import java.util.Queue;
import java.util.StringTokenizer;
import java.util.concurrent.Future;

import org.w3c.dom.Element;

import edu.brown.cs.cose.cosecommon.CoseSource;
import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.s6.common.S6Exception;
import edu.brown.cs.s6.common.S6Fragment;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6SolutionSet;

class KeySearchSourcerer extends KeySearchBase {



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private final static String    SOURCERER_SCHEME = "http";
private final static String    SOURCERER_AUTHORITY = "sourcerer.ics.uci.edu";
private final static String    SOURCERER_PATH = "/sourcerer/ws-search/search";
private final static String    SOURCERER_FRAGMENT = null;
private final static String    SOURCERER_QUERY = "epp=100&pid=1&client=s6&qry=";

private final static String    SOURCERER_FPATH = "/sourcerer/repodata/resource";
private final static String    SOURCERER_FQUERY = "client=s6&rp=";
		
private final static String    SOURCE_PREFIX = "SOURCERER:";




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

KeySearchSourcerer()
{ }



/********************************************************************************/
/*										*/
/*	Search method								*/
/*										*/
/********************************************************************************/

protected void queueInitialSolutions(S6SolutionSet ss,int ct,Queue<Future<Boolean>> waitfors) throws S6Exception
{
   S6Request.Search sr = ss.getRequest();

   for (S6Request.KeywordSet kws : sr.getKeywordSets()) {
      String q = SOURCERER_QUERY;
      for (String s : kws.getWords()) {
	 q += " \"" + s + "\"";
       }
      ScanSolution ssol = new ScanSolution(ss,q,waitfors);
      Future<Boolean> fb = ss.getEngine().executeTask(S6TaskType.IO,ssol);
      synchronized (waitfors) {
	 waitfors.add(fb);
       }
    }
}



protected void queueSpecificSolution(S6SolutionSet ss,String src,Queue<Future<Boolean>> wq)
{
   if (!src.startsWith(SOURCE_PREFIX)) return;
   src = src.substring(SOURCE_PREFIX.length());

   StringTokenizer tok = new StringTokenizer(src,"@");

   String fp = null;
   String pp = null;
   String fn = null;

   if (tok.hasMoreTokens()) fp = tok.nextToken();
   if (tok.hasMoreTokens()) pp = tok.nextToken();
   if (tok.hasMoreTokens()) fn = tok.nextToken();

   LoadSolution ls = new LoadSolution(ss,fp,fn,pp,0,null);
   Future<Boolean> fb = ss.getEngine().executeTask(S6TaskType.IO,ls);
   synchronized (wq) {
      wq.add(fb);
    }
}




/********************************************************************************/
/*										*/
/*	Task to load a search result page					*/
/*										*/
/********************************************************************************/

private class ScanSolution implements Runnable {

   private Queue<Future<Boolean>> wait_fors;
   private S6SolutionSet solution_set;
   private String using_query;

   ScanSolution(S6SolutionSet ss,String q,Queue<Future<Boolean>> waitfors) {
      wait_fors = waitfors;
      solution_set = ss;
      using_query = q;
    }

   public void run() {
      String q = using_query;
      String rslt = null;
      try {
         rslt = loadURI(SOURCERER_SCHEME,SOURCERER_AUTHORITY,SOURCERER_PATH,q,SOURCERER_FRAGMENT,false);
       }
      catch (S6Exception e) {
         System.err.println("S6: KEYSEARCH: Problem scanning: " + e);
         return;
       }
   
      Element e = IvyXml.convertStringToXml(rslt);
      int sidx = 0;
      for (Element ent : IvyXml.elementsByTag(e,"entries")) {
         String path = IvyXml.getTextElement(ent,"filePath");
         String name = IvyXml.getTextElement(ent,"entityName");
         String psrc = IvyXml.getTextElement(ent,"projectSrcLinkAtOrigin");
         if (path == null || name == null) continue;
         switch (solution_set.getScopeType()) {
            case PACKAGE :
            case PACKAGE_UI :
            case SYSTEM :
               String base = path;
               int idx = base.lastIndexOf("/");
               if (idx >= 0) {
                  base = base.substring(0,idx);
                  buildPackageFragment(solution_set,base,base,wait_fors,sidx++);
                }
               break;
            case FILE :
               LoadSolution ls = new LoadSolution(solution_set,path,name,psrc,sidx++,null);
               Future<Boolean> fb = solution_set.getEngine().executeTask(S6TaskType.IO,ls);
               synchronized (wait_fors) {
        	  wait_fors.add(fb);
        	}
               break;
          }
       }
    }
   
}




/********************************************************************************/
/*										*/
/*	Task to load an individual results page 				*/
/*										*/
/********************************************************************************/

private class LoadSolution implements Runnable {

   private S6SolutionSet solution_set;
   private String file_path;
   private String file_name;
   private String proj_src;
   private int item_index;
   private S6Fragment package_fragment;

   LoadSolution(S6SolutionSet ss,String path,String name,String src,int idx,S6Fragment pf) {
      solution_set = ss;
      file_path = path;
      file_name = name;
      proj_src = src;
      package_fragment = pf;
    }

   public void run() {
      String q = SOURCERER_FQUERY + file_path;
   
      try {
         String r = loadURI(SOURCERER_SCHEME,SOURCERER_AUTHORITY,SOURCERER_FPATH,q,SOURCERER_FRAGMENT,true);
         SourcererSource ss = new SourcererSource(file_path,r,file_name,proj_src,item_index);
         KeySearchBase.getSolutions(solution_set,r,ss,package_fragment);
       }
      catch (S6Exception e) {
         System.err.println("S6: KEYSEARCH: Problem loading sourcerer file: " + e);
       }
    }
}



/********************************************************************************/
/*										*/
/*	Package methods 							*/
/*										*/
/********************************************************************************/

protected CoseSource createPackageSource(String id,int priority)
{
   return new SourcererSource(id,null,id,null,priority);
}



protected void queuePackageSolutions(S6SolutionSet ss,String id,Queue<Future<Boolean>> wq,
					S6Fragment pf,int priority)
        throws S6Exception
{
   String q = SOURCERER_FQUERY + id;
   String r = loadURI(SOURCERER_SCHEME,SOURCERER_AUTHORITY,SOURCERER_FPATH,q,SOURCERER_FRAGMENT,true);
   StringTokenizer t1 = new StringTokenizer(r,"\n");
   while (t1.hasMoreTokens()) {
      String ln = t1.nextToken();
      StringTokenizer t2 = new StringTokenizer(ln," \t|");
      if (t2.hasMoreTokens()) {
	 String what = t2.nextToken();
	 if (t2.hasMoreTokens()) {
	    String fnm = t2.nextToken();
	    if (what.equals("F") && fnm.endsWith(".java")) {
	       String nnm = id + "/" + fnm;
	       LoadSolution ls = new LoadSolution(ss,nnm,nnm,nnm,priority,pf);
	       Future<Boolean> fb = ss.getEngine().executeTask(S6TaskType.IO,ls);
	       synchronized (wq) {
		  wq.add(fb);
		}
	     }
	  }	
       }
    }
}




/********************************************************************************/
/*										*/
/*	Sourcerer Source							*/
/*										*/
/********************************************************************************/

private static class SourcererSource extends KeySearchSource implements CoseSource {

   private String file_path;
   private String file_name;
   private String proj_path;

   SourcererSource(String path,String code,String name,String proj,int idx) {
      super(code,idx);
      file_path = path;
      file_name = name;
      proj_path = proj;
    }

   public String getName() {
      return SOURCE_PREFIX + file_path + "@" + proj_path + "@" + file_name;
    }

   public String getDisplayName()	{ return file_name; }

}	// end of subclass SourcererSource


}	// end of class KeySearchSourcerer



/* end of KeySearchSourcerer.java */
