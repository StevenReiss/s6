/********************************************************************************/
/*										*/
/*		KeySearchKoders.java						*/
/*										*/
/*	Keyword-based initial search using Koders.com				*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/keysearch/KeySearchKoders.java,v 1.10 2015/12/23 15:45:01 spr Exp $ */


/*********************************************************************************
 *
 * $Log: KeySearchKoders.java,v $
 * Revision 1.10  2015/12/23 15:45:01  spr
 * Update search (for UI at least)
 *
 * Revision 1.9  2015/09/23 17:57:59  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.8  2013/09/13 20:32:31  spr
 * Add calls for UI search.
 *
 * Revision 1.7  2013-05-09 12:26:18  spr
 * Minor changes to start ui fixups.
 *
 * Revision 1.6  2012-07-20 22:15:02  spr
 * Cleaup code.
 *
 * Revision 1.5  2012-06-11 18:18:26  spr
 * Include changed/new files for package/ui search
 *
 * Revision 1.4  2012-06-11 14:07:30  spr
 * Code cleanup
 *
 * Revision 1.3  2009-09-18 01:41:06  spr
 * Handle full class input.
 *
 * Revision 1.2  2008-11-12 13:51:37  spr
 * Performance and bug updates.
 *
 * Revision 1.1.1.1  2008-06-03 12:59:22  spr
 * Initial version of S6
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.keysearch;


import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Future;

import org.w3c.dom.Element;

import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.s6.common.S6Exception;
import edu.brown.cs.s6.common.S6Fragment;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6SolutionSet;
import edu.brown.cs.s6.common.S6Source;


class KeySearchKoders extends KeySearchBase {



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private final static String	KODERS_SCHEME = "http";
private final static String	KODERS_AUTHORITY = "www.koders.com";
private final static String	KODERS_PATH = "/default.aspx";
private final static String	KODERS_FRAGMENT = null;
// private final static String	KODERS_QUERY = "la=java&li=*&s=";
private final static String	KODERS_XML_QUERY = "output=xml&la=Java&li=*&s=";

private final static String	SOURCE_PREFIX = "KODERS:";

private final static int	MAX_PAGES=4;	// @ 25 per page





/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

KeySearchKoders()
{ }



/********************************************************************************/
/*										*/
/*	Search method								*/
/*										*/
/********************************************************************************/

protected void queueInitialSolutions(S6SolutionSet ss,int tgtct,Queue<Future<Boolean>> waitfors) throws S6Exception
{
   S6Request.Search sr = ss.getRequest();

   for (S6Request.KeywordSet kws : sr.getKeywordSets()) {
      String q = KODERS_XML_QUERY;
      int i = 0;
      for (String s : kws.getWords()) {
	 if (i ++ > 0) q += " ";
	 q += s;
       }
      ScanSolution ssol = new ScanSolution(ss,q,0,waitfors);
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

   switch (ss.getScopeType()) {
      case PACKAGE :
      case PACKAGE_UI :
      case SYSTEM :
	 String u = src;
	 buildPackageFragment(ss,src,u,wq,0);
	 break;
      case FILE :
      default :
	 LoadSolution ls = new LoadSolution(ss,src,0,null);
	 Future<Boolean> fb = ss.getEngine().executeTask(S6TaskType.IO,ls);
	 synchronized (wq) {
	    wq.add(fb);
	  }
	 break;
    }
}



protected S6Source createPackageSource(String id,int priority)
{
   return new KoderSource(id,id,null,null,priority);
}



protected void queuePackageSolutions(S6SolutionSet ss,String id,Queue<Future<Boolean>> wq,
					S6Fragment pf,int priority)
{
    LoadSolution ls = new LoadSolution(ss,id,priority,pf);
    Future<Boolean> fb = ss.getEngine().executeTask(S6TaskType.IO,ls);
    synchronized (wq) {
       wq.add(fb);
     }
}



protected boolean addPackages(S6SolutionSet ss,S6Fragment frag,S6Source src,Set<String> pkgs,Queue<Future<Boolean>> wq)
{
   boolean chng = false;

   for (String pkg : pkgs) {
      PackageScanSolution pss = new PackageScanSolution(ss,frag,pkg,wq);
      Future<Boolean> fb = ss.getEngine().executeTask(S6TaskType.IO,pss);
      synchronized (wq) {
	 wq.add(fb);
       }
      chng = true;
    }

   return chng;
}




/********************************************************************************/
/*										*/
/*	Task to load a search result page					*/
/*										*/
/********************************************************************************/

private class ScanSolution implements Runnable {

   private Queue<Future<Boolean>> wait_fors;
   private S6SolutionSet solution_set;
   private int page_number;
   private String using_query;

   ScanSolution(S6SolutionSet ss,String q,int pg,Queue<Future<Boolean>> waitfors) {
      wait_fors = waitfors;
      solution_set = ss;
      page_number = pg;
      using_query = q;
    }

   public void run() {
      String q = using_query;
      if (page_number > 0) q += "&p=" + page_number;
      Element xml = null;
      try {
         String sxml = loadURI(KODERS_SCHEME,KODERS_AUTHORITY,KODERS_PATH,q,KODERS_FRAGMENT,false);
         xml = IvyXml.convertStringToXml(sxml);
       }
      catch (S6Exception e) {
         System.err.println("S6: KEYSEARCH: Problem scanning: " + e);
         return;
       }
   
      String sidx = IvyXml.getTextElement(xml,"startindex");
      int idx = Integer.parseInt(sidx);
      for (Element itm : IvyXml.children(xml,"item")) {
         String furl = IvyXml.getTextElement(itm,"link");
         if (furl == null) continue;
         int idx1 = furl.indexOf("?");
         if (idx1 > 0) furl = furl.substring(0,idx1);
         switch (solution_set.getScopeType()) {
            case PACKAGE :
            case PACKAGE_UI :
            case SYSTEM :
               String fp = IvyXml.getTextElement(itm,"filepath");
               fp = fp.replace('\\','/');
               int idx2 = fp.lastIndexOf("/");
               if (idx2 > 0) fp = fp .substring(0,idx2);
               buildPackageFragment(solution_set,furl,fp,wait_fors,idx++);
               break;
            case FILE :
            default :
               LoadSolution ls = new LoadSolution(solution_set,furl,idx++,null);
               Future<Boolean> fb = solution_set.getEngine().executeTask(S6TaskType.IO,ls);
               synchronized (wait_fors) {
        	  wait_fors.add(fb);
        	}
               break;
          }
       }
   
      if (page_number == 0) {
         int np = MAX_PAGES-1;
         try {
            String tots = IvyXml.getTextElement(xml,"totalresults");
            int totn = Integer.parseInt(tots);
            String ppgs = IvyXml.getTextElement(xml,"itemsperpage");
            int ppg = Integer.parseInt(ppgs);
            np = (totn + ppg - 1)/ppg;
            if (np >= MAX_PAGES-1) np = MAX_PAGES-1;
          }
         catch (NumberFormatException e) { }
         for (int p = 1; p <= np; ++p) {
            ScanSolution nss = new ScanSolution(solution_set,using_query,p,wait_fors);
            Future<Boolean> fb = solution_set.getEngine().executeTask(S6TaskType.IO,nss);
            synchronized (wait_fors) {
               wait_fors.add(fb);
             }
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
   private String for_item;
   private int item_index;
   private S6Fragment package_fragment;

   LoadSolution(S6SolutionSet ss,String itm,int idx,S6Fragment pf) {
      solution_set = ss;
      for_item = itm;
      item_index = idx;
      package_fragment = pf;
    }

   public void run() {
      String p = for_item;
      int idx = for_item.indexOf("?");
      if (idx >= 0) p = for_item.substring(0,idx);
   
      String q = "output=xml";
      Element xml = null;
   
      try {
         String sxml = loadURI(KODERS_SCHEME,KODERS_AUTHORITY,p,q,KODERS_FRAGMENT,true);
         // sxml = sxml.replace('\032','?');
         // sxml = sxml.replace('\u001a','?');
         // sxml = sxml.replace('\025','?');
         // sxml = sxml.replace('\u0015','?');
         xml = IvyXml.convertStringToXml(sxml);
       }
      catch (S6Exception e) {
         System.err.println("S6: KEYSEARCH: Problem loading solution: " + e);
         return;
       }
      String c1 = IvyXml.getTextElement(xml,"syntaxColoredSourceInHTML");
      if (c1 == null) return;
      String s = getCodeFromHtml(c1);
      Map<String,String> rel = new HashMap<String,String>();
      for (Element c : IvyXml.children(IvyXml.getChild(xml,"siblingFiles"),"file")) {
         String fnm = IvyXml.getAttrString(c,"name");
         String fur = IvyXml.getAttrString(c,"oldhref");
         if (fnm.endsWith(".java")) {
            rel.put(fnm,fur);
          }
       }
      String ipth = IvyXml.getTextElement(IvyXml.getChild(xml,"parentFolder"),"repositoryPath");
      String fnm = IvyXml.getTextElement(IvyXml.getChild(xml,"filemetadata"),"filename");
      ipth = ipth.replace("\\","/") + "/" + fnm;
   
      KoderSource ks = new KoderSource(p,ipth,rel,s,item_index);
      KeySearchBase.getSolutions(solution_set,s,ks,package_fragment);
   
      if (package_fragment != null) {
         for (String rurl : rel.values()) {
            if (rurl.equals(for_item)) continue;
            int ridx = rurl.indexOf("?");
            if (ridx > 0) rurl = rurl.substring(0,ridx);
            if (rurl == for_item) continue;
            Element rxml = null;
            try {
               String psxml = loadURI(KODERS_SCHEME,KODERS_AUTHORITY,rurl,"output=xml",KODERS_FRAGMENT,true);
               psxml = psxml.replace('\010',' ');
               rxml = IvyXml.convertStringToXml(psxml);
             }
            catch (S6Exception e) {
               System.err.println("S6: KEYSEARCH: problem loading related solution: " + e);
               return;
             }
            String rc1 = IvyXml.getTextElement(rxml,"syntaxColoredSourceInHTML");
            String rs = getCodeFromHtml(rc1);
            KeySearchBase.getSolutions(solution_set,rs,null,package_fragment);
          }
       }
    }
}




private String getCodeFromHtml(String html)
{
   if (html == null) return null;

   int idx = html.indexOf("<div id=\"CodeDiv\"");
   int idx1 = 0;
   int idx2 = html.length();
   if (idx >= 0) {
      idx1 = html.indexOf(">",idx);
      idx1 += 1;				// start of code
      idx2 = html.indexOf("</div>",idx1);
    }
   StringBuffer buf = new StringBuffer();
   int lvl = 0;
   for (int i = idx1; i < idx2; ++i) {
      char c = html.charAt(i);
      if (lvl == 0 && c == '<') ++lvl;
      else if (lvl > 0 && c == '>') --lvl;
      else if (lvl == 0) buf.append(c);
    }

   String s1 = buf.toString();
   s1 = s1.replace("&quot;","\"");
   s1 = s1.replace("&amp;","&");
   s1 = s1.replace("&lt;","<");
   s1 = s1.replace("&gt;",">");

   return s1;
}



/********************************************************************************/
/*										*/
/*	Task to load a package search result page				*/
/*										*/
/********************************************************************************/

private class PackageScanSolution implements Runnable {

   private Queue<Future<Boolean>> wait_fors;
   private S6SolutionSet solution_set;
   private S6Fragment package_fragment;
   private String for_package;

   PackageScanSolution(S6SolutionSet ss,S6Fragment pfg,String q,Queue<Future<Boolean>> waitfors) {
      wait_fors = waitfors;
      solution_set = ss;
      package_fragment = pfg;
      for_package = q;
    }

   public void run() {
      String q = "output=xml&s=cdef:" + for_package;
      Element xml = null;
      try {
         String sxml = loadURI(KODERS_SCHEME,KODERS_AUTHORITY,KODERS_PATH,q,KODERS_FRAGMENT,true);
         xml = IvyXml.convertStringToXml(sxml);
       }
      catch (S6Exception e) {
         System.err.println("S6: KEYSEARCH: Problem scanning: " + e);
         return;
       }
   
      String url = null;
      for (Element itm : IvyXml.children(xml,"item")) {
         String d = IvyXml.getTextElement(itm,"description");
         String code = getCodeFromHtml(d);
         String iurl = IvyXml.getTextElement(itm,"link");
         int idx1 = iurl.indexOf("?");
         if (idx1 > 0) iurl = iurl.substring(0,idx1);
         if (code.contains("package ")) {
            if (code.contains("package " + for_package + ";")) {
               url = iurl;
               break;
             }
          }
         else {
            try {
               String csxml = loadURI(KODERS_SCHEME,KODERS_AUTHORITY,iurl,"output=xml",KODERS_FRAGMENT,true);
               Element cxml = IvyXml.convertStringToXml(csxml);
               String rc1 = IvyXml.getTextElement(cxml,"syntaxColoredSourceInHTML");
               String rs = getCodeFromHtml(rc1);
               if (rs.contains("package " + for_package + ";")) {
        	  url = iurl;
        	  break;
        	}
             }
            catch (S6Exception e) { }
          }
       }
   
      if (url == null) return;
   
      LoadSolution ls = new LoadSolution(solution_set,url,0,package_fragment);
      Future<Boolean> fb = solution_set.getEngine().executeTask(S6TaskType.IO,ls);
      synchronized (wait_fors) {
         wait_fors.add(fb);
       }
    }

}



/********************************************************************************/
/*										*/
/*	Koders Source								*/
/*										*/
/********************************************************************************/

private static class KoderSource extends KeySearchSource implements S6Source {

   private String base_link;
   private String base_path;
   KoderSource(String base,String path,Map<String,String> rel,String code,int idx) {
      super(code,idx);
      base_link = base;
      base_path = path;
    }

   public String getName()		{ return SOURCE_PREFIX + base_link; }
   public String getDisplayName()	{ return base_path; }

}	// end of subclass KoderSource


}	// end of class KeySearchKoders



/* end of KeySearchKoders.java */

