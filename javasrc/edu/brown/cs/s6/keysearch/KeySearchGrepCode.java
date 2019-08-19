/********************************************************************************/
/*										*/
/*		KeySearchGrepCode.java						*/
/*										*/
/*	Keyword search for code using GrepCode					*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/keysearch/KeySearchGrepCode.java,v 1.5 2016/07/18 23:05:04 spr Exp $ */

/*********************************************************************************
 *
 * $Log: KeySearchGrepCode.java,v $
 * Revision 1.5  2016/07/18 23:05:04  spr
 * Keysearch updates for applications, ui search.
 *
 * Revision 1.4  2015/12/23 15:45:01  spr
 * Update search (for UI at least)
 *
 * Revision 1.3  2015/09/23 17:57:59  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.2  2014/08/29 15:16:05  spr
 * Updates for suise, testcases.
 *
 * Revision 1.1  2013/09/20 20:58:58  spr
 * Missing code files.
 *
 *
 ********************************************************************************/




package edu.brown.cs.s6.keysearch;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Future;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;

import edu.brown.cs.cose.cosecommon.CoseSource;
import edu.brown.cs.s6.common.S6Exception;
import edu.brown.cs.s6.common.S6Fragment;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6SolutionSet;



class KeySearchGrepCode extends KeySearchBase
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private final static String	GREPCODE_SCHEME = "http";
private final static String	GREPCODE_AUTHORITY = "grepcode.com";
private final static String	GREPCODE_PATH = "/search/";
private final static String	GREPCODE_FRAGMENT = null;
private final static String	GREPCODE_QUERY = "query=";
private final static String	GREPCODE_QUERY_TAIL = "&entry=type";

private final static String	SOURCE_PREFIX = "GREPCODE:";

private final static int	MAX_PAGES=15;		// 10 per page
private final static int	RESULTS_PER_PAGE = 10;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

KeySearchGrepCode()
{ }



/********************************************************************************/
/*										*/
/*	Search Methods								*/
/*										*/
/********************************************************************************/

protected void queueInitialSolutions(S6SolutionSet ss,int tgtct,Queue<Future<Boolean>> waitfors) throws S6Exception
{
   S6Request.Search sr = ss.getRequest();

   for (S6Request.KeywordSet kws : sr.getKeywordSets()) {
      String q = GREPCODE_QUERY;
      int i = 0;
      for (String s : kws.getWords()) {
	 if (i ++ > 0) q += " ";
	 q += s;
       }
      q += GREPCODE_QUERY_TAIL;
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
      case SYSTEM :
      case PACKAGE :
      case PACKAGE_UI :
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



protected CoseSource createPackageSource(String id,int priority)
{
   return new GrepCodeSource(id,id,priority);
}



protected void queuePackageSolutions(S6SolutionSet ss,String id,Queue<Future<Boolean>> wq,
      S6Fragment pf,int priority)
{
   LoadPackageSolution ls = new LoadPackageSolution(ss,id,priority,pf);
   Future<Boolean> fb = ss.getEngine().executeTask(S6TaskType.IO,ls);
   synchronized (wq) {
      wq.add(fb);
    }
}



protected boolean addPackages(S6SolutionSet ss,S6Fragment frag,CoseSource src,Set<String> pkgs,Queue<Future<Boolean>> wq)
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
      if (page_number > 0) q += "&start=" + (page_number*RESULTS_PER_PAGE);
   
      SearchScanner scanner = null;
   
      try {
         URI uri = new URI(GREPCODE_SCHEME,GREPCODE_AUTHORITY,GREPCODE_PATH,q,GREPCODE_FRAGMENT);
         URL url = uri.toURL();
         scanner = new SearchScanner(url);
         scanURI(uri,false,scanner);
       }
      catch (Exception e) {
         System.err.println("S6: KEYSEARCH: Problem scanning: " + e);
         return;
       }
   
      int idx = page_number * RESULTS_PER_PAGE;
      for (String furl : scanner.getFileUrls()) {
         String fdir = scanner.getUrlPath(furl);
         switch (solution_set.getSearchType()) {
            case PACKAGE :
            case UIFRAMEWORK :
            case ANDROIDUI :
            case APPLICATION :
               buildPackageFragment(solution_set,furl,fdir,wait_fors,idx++);
               break;
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
            int totn = scanner.getResultCount();
            int ppg = RESULTS_PER_PAGE;
            np = (totn + ppg - 1)/ppg;
            if (np >= MAX_PAGES-1) np = MAX_PAGES-1;
            // System.err.println("PAGES: " + totn + " " + ppg + " " + np);
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


private class SearchScanner extends HTMLEditorKit.ParserCallback {

   private URL base_url;
   private List<String> file_urls;
   private Map<String,String> url_path;
   private int result_count;
   private int in_result;
   private String last_ttl;

   SearchScanner(URL base) {
      file_urls = new ArrayList<String>();
      url_path = new HashMap<String,String>();
      in_result = 0;
      result_count = -1;
    }

   List<String> getFileUrls()			{ return file_urls; }
   String getUrlPath(String url)		{ return url_path.get(url); }
   int getResultCount() 			{ return 100; }

   @Override public void handleStartTag(HTML.Tag t,MutableAttributeSet a,int pos) {
      if (t == HTML.Tag.DIV) {
         String cls = (String) a.getAttribute(HTML.Attribute.CLASS);
         if (cls != null && cls.contains("result-list")) {
            in_result = 1;
          }
         else if (cls != null && cls.contains("search-result-item-head")) {
            in_result = 2;
          }
       }
      else if (in_result == 1 && t == HTML.Tag.A) {
         String href = (String) a.getAttribute(HTML.Attribute.HREF);
         String cls = (String) a.getAttribute(HTML.Attribute.CLASS);
         if (href != null && cls != null && cls.contains("container-name")) {
            in_result = 0;
            try {
               URL ptr = new URL(base_url,href);
               href = ptr.toString();
               int idx = href.lastIndexOf("#");
               if (idx > 0) href = href.substring(0,idx);
               href += "?v=source";
               url_path.put(href,last_ttl);
               file_urls.add(href);
             }
            catch (MalformedURLException e) { }
          }
       }
      else if (in_result == 2 && t == HTML.Tag.SPAN) {
         String cls = (String) a.getAttribute(HTML.Attribute.CLASS);
         if (cls != null && cls.contains("entity-name")) {
            in_result = 3;
          }
       }
    }

   @Override public void handleText(char [] text,int pos) {
      if (result_count == -2) {
	 String s = new String(text);
	 int idx = s.indexOf("We've found ");
	 if (idx >= 0) {
	    s = s.substring(idx+12);
	    idx = s.indexOf(" ");
	    if (idx > 0) s = s.substring(0,idx);
	    s = s.replace(",","");
	    try {
	       result_count = Integer.parseInt(s);
	     }
	    catch (NumberFormatException e) {
	       result_count = 100;
	     }
	  }
       }
    }

}	// end of inner class SearchScanner





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
      String txt = loadFile(for_item);
      if (txt == null) return;

      GrepCodeSource ks = new GrepCodeSource(for_item,txt,item_index);
      KeySearchBase.getSolutions(solution_set,txt,ks,package_fragment);
    }

}


private String loadFile(String url)
{
   String txt = null;
   try {
      URI uri = new URI(url.toString());
      txt = loadURI(uri,true);
    }
   catch (Exception e) {
      System.err.println("S6: KEYSEARCH: Problem loading solution: " + e);
      return null;
    }

   return txt;
}




private class LoadPackageSolution implements Runnable {

   private S6SolutionSet solution_set;
   private String for_item;
   private int item_index;
   private S6Fragment package_fragment;

   LoadPackageSolution(S6SolutionSet ss,String itm,int idx,S6Fragment pf) {
      solution_set = ss;
      for_item = itm;
      item_index = idx;
      package_fragment = pf;
    }

   public void run() {
      String furl = for_item;
      int idx = furl.lastIndexOf("/");
      furl = furl.substring(0,idx+1);
      String txt = loadFile(for_item);
      if (txt == null) return;


      GrepCodeSource ks = new GrepCodeSource(for_item,txt,item_index);
      KeySearchBase.getSolutions(solution_set,txt,ks,package_fragment);
    }

}	// end of inner class LoadPackageSolution








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
      String q= "q=" + for_package + "&type=Code";
      SearchScanner ss = null;
      try {
         URI uri = new URI(GREPCODE_SCHEME,GREPCODE_AUTHORITY,GREPCODE_PATH,q,GREPCODE_FRAGMENT);
         ss = new SearchScanner(uri.toURL());
         scanURI(uri,true,ss);
       }
      catch (Exception e) {
         System.err.println("S6: KEYSEARCH: Problem scanning: " + e);
         return;
       }
   
      String url = null;
      for (String iurl : ss.getFileUrls()) {
         String rs = loadFile(iurl);
         if (rs != null && rs.contains("package " + for_package + ";")) {
            url = iurl;
            break;
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
/*	GrepCode Source 							 */
/*										*/
/********************************************************************************/

private static class GrepCodeSource extends KeySearchSource implements CoseSource {

   private String base_link;
   private String base_path;

   GrepCodeSource(String base,String code,int idx) {
      super(code,idx);
      base_link = base;
      int pos = 1;
      for (int i = 0; i < 2; ++i) {
	 pos = base.indexOf("/",pos);
       }
      base_path = base.substring(pos+1);
    }

   public String getName()		{ return SOURCE_PREFIX + base_link; }
   public String getDisplayName()	{ return base_path; }

}	// end of subclass OhlohSource



}	// end of class KeySearchGrepCode




/* end of KeySearchGrepCode.java */
