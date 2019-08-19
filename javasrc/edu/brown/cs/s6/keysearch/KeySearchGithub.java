/********************************************************************************/
/*										*/
/*		KeySearchGithub.java						*/
/*										*/
/*	Keyword search for code using GITHUB					*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/keysearch/KeySearchGithub.java,v 1.7 2016/07/18 23:05:04 spr Exp $ */

/*********************************************************************************
 *
 * $Log: KeySearchGithub.java,v $
 * Revision 1.7  2016/07/18 23:05:04  spr
 * Keysearch updates for applications, ui search.
 *
 * Revision 1.6  2015/09/23 17:57:58  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.5  2015/03/11 18:04:13  spr
 * Update search for proper caching and error checking.
 *
 * Revision 1.4  2015/02/19 23:33:04  spr
 * Fix warnings.
 *
 * Revision 1.3  2015/02/14 19:40:12  spr
 * Add test case generation.
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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
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

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.Jsoup;

class KeySearchGithub extends KeySearchBase
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private final static String	GITHUB_SCHEME = "https";
private final static String	GITHUB_AUTHORITY = "github.com";
private final static String	GITHUB_PATH = "/search";
private final static String	GITHUB_FRAGMENT = null;
private final static String	GITHUB_QUERY = "l=Java&q=";
private final static String	GITHUB_QUERY_TAIL = "&ref=advsearch&type=Code";
private final static String	GITHUB_FILE_AUTHORITY = "raw.github.com";

private final static String	SOURCE_PREFIX = "GITHUB:";

private final static int	MAX_PAGES=15;		// 10 per page
private final static int	RESULTS_PER_PAGE = 10;

private static Object		search_lock = new Object();




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

KeySearchGithub()
{ 
   setMaximumSimultaneousQueries(1);
}



/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

@Override protected int getResultsPerPage()             { return RESULTS_PER_PAGE; }


@Override protected URI createSearchURI(S6Request.Search req,String projectid,int page)
{
   String q = GITHUB_QUERY;
   int i = 0;
   for (S6Request.KeywordSet kws : req.getKeywordSets()) {
      for (String s : kws.getWords()) {
          if (i++ > 0) q += " ";
          if (s.contains(" ")) q += "\"" + s + "\"";
          else q += s;
       }
    }
   if (projectid != null) q += "repo:" + projectid;
   q += GITHUB_QUERY_TAIL;
   if (page > 0) q += "&p=" + (page+1);
   try {
      URI uri = new URI(GITHUB_SCHEME,GITHUB_AUTHORITY,GITHUB_PATH,q,null);
      return uri;
    }
   catch (URISyntaxException e) {
      return null;
    }
}



@Override protected boolean shouldRetry(Exception e)
{
   if (e.getMessage().contains(": 429")) return true;
   return false;
}



@Override protected boolean shouldRetry(String cnts)
{
   return false;
}


@Override protected SearchPageScanner getSearchPageScanner(URI uri,String text)
{
   return new GithubSearchPageScanner(uri,text);
}


@Override protected CoseSource createSource(URI baseuri,String code,int idx)
{
   return new GithubSource(baseuri.toString(),code,idx);
}


@Override protected URI getURIFromSource(String src)
{
   if (!isRelevantSoure(src)) return null;
   int idx = src.indexOf(":");
   String urisrc = src.substring(idx+1);
   try {
      URI uri = new URI(urisrc);
      return uri;
    }
   catch (URISyntaxException e) { }
   return null;
}



@Override protected FilePageScanner getFilePageScanner(String cnts,URI uri)
{
   // we return raw file pages, so this isn't needed
   return null;
}


@Override protected List<URI> getURIsForPackage(URI baseuri,String txt)
{
   List<URI> rslt = new ArrayList<URI>();
   
   String urlstr = baseuri.toString();
   int idx = urlstr.lastIndexOf("/");
   String furl = urlstr.substring(0,idx);
   try {
      URI duri = new URI(furl);
      String cnts = loadURI(duri,true);
      Element jsoup = Jsoup.parse(cnts,furl);
      Elements refs = jsoup.select("a.js-directory-link");
      for (Element e : refs) {
         String href = e.attr("href");
         String ttl = e.attr("title");
         if (href != null && ttl != null && ttl.endsWith(".java")) {
            if (!href.equals(furl)) {
               try {
                  URI ruri = new URI(href);
                  rslt.add(ruri);
                }
               catch (URISyntaxException ex) { }
             }
          }
       }
    } 
   catch (URISyntaxException e) { }
   catch (S6Exception e) { }
   
   return rslt;
}



@Override protected URI getFileContentsURI(URI base)
{
   String url = base.getPath();
   String blob = url.replace("/blob","");
   try {
      URI uri = new URI(GITHUB_SCHEME,GITHUB_FILE_AUTHORITY,blob,null,null);
      return uri;
    }
   catch (URISyntaxException e) { }
   
   return base;
}


@Override protected URI createPackageURI(String pkg)
{
   String q = "q=" + pkg + "&type=Code";
   try {
      return new URI(GITHUB_AUTHORITY,GITHUB_PATH,q,null);
    }
   catch (URISyntaxException e) { }
   
   return null;
}



private static class GithubSearchPageScanner extends SearchPageScanner {
   
   GithubSearchPageScanner(URI uri,String text) {
      super(uri.toString(),text);
    }
   
   @Override List<URI> getFileUris() {
      List<URI> rslt = new ArrayList<URI>();
      Elements results = jsoup_doc.select("div.code-list-item");
      for (Element result : results) {
         Elements uris = result.select("p.title a:eq(1)");
         Element tag = uris.get(0);
         String href = tag.attr("href");
         String ttl = tag.attr("title");
         int idx = href.indexOf("/blob/");
         if (idx < 0) continue;
         String proj = href.substring(1,idx);
         String rem = href.substring(idx+6);
         try {
            String path = "/" + proj + "/" + rem;
            URI uri = new URI(GITHUB_SCHEME,GITHUB_AUTHORITY,path,null);
            rslt.add(uri);
            project_map.put(uri,proj);
            path_map.put(uri,ttl);
            Elements codes = result.select("td.blob-code");
            StringBuffer buf = new StringBuffer();
            for (Element codeelt : codes) {
               buf.append(codeelt.text());
               buf.append("\n");
             }
            code_map.put(uri,buf.toString());
          }
         catch (URISyntaxException e) { }
       }
      return rslt;
    }
   
}       // end of inner class GithubSearchPageScanner






/********************************************************************************/
/*										*/
/*	Search Methods								*/
/*										*/
/********************************************************************************/

protected void queueInitialSolutions(S6SolutionSet ss,int tgtct,Queue<Future<Boolean>> waitfors) throws S6Exception
{
   S6Request.Search sr = ss.getRequest();

   for (S6Request.KeywordSet kws : sr.getKeywordSets()) {
      String q = GITHUB_QUERY;
      int i = 0;
      for (String s : kws.getWords()) {
	 if (i ++ > 0) {
	    if (i < 5) q += " ";
	    // if (i < 5) q += " OR ";
	    else q += " ";
	  }
	 q += s;
       }
      q += GITHUB_QUERY_TAIL;
      ScanSolution ssol = new ScanSolution(ss,q,0,waitfors);
      Future<Boolean> fb = ss.getEngine().executeTask(S6TaskType.IO,ssol);
      synchronized (waitfors) {
	 waitfors.add(fb);
       }
    }
}



protected void queueSpecificSolution(S6SolutionSet ss,String src,Queue<Future<Boolean>> wq)
{
   if (!isRelevantSoure(src)) return;
   int idx = src.indexOf(":");
   src = src.substring(idx+1);

   switch (ss.getSearchType()) {
      case UIFRAMEWORK :
      case PACKAGE :
      case ANDROIDUI :
      case APPLICATION :
	 String u = src;
	 buildPackageFragment(ss,src,u,wq,0);
	 break;
      default :
	 LoadSolution ls = new LoadSolution(ss,src,0,null);
	 Future<Boolean> fb = ss.getEngine().executeTask(S6TaskType.IO,ls);
	 synchronized (wq) {
	    wq.add(fb);
	  }
	 break;
    }
}



protected boolean isRelevantSoure(String src)
{
   return src.startsWith(SOURCE_PREFIX);
}



protected CoseSource createPackageSource(String id,int priority)
{
   return new GithubSource(id,id,priority);
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
      if (page_number > 0)
         q += "&p=" + (page_number+1);
      SearchScanner scanner = new SearchScanner();
      try {
         scanGithubURI(GITHUB_AUTHORITY,GITHUB_PATH,q,scanner);
       }
      catch (S6Exception e) {
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

   private List<String> file_urls;
   private Map<String,String> url_path;
   private Map<String,String> url_code;
   private String last_url;
   private StringBuffer code_buf;
   private int result_count;
   private boolean use_code;
   private int in_result;

   SearchScanner() {
      file_urls = new ArrayList<String>();
      url_path = new HashMap<String,String>();
      url_code = new HashMap<String,String>();
      code_buf = null;
      use_code = false;
      in_result = 0;
      result_count = -1;
    }

   List<String> getFileUrls()			{ return file_urls; }
   String getUrlPath(String url)		{ return url_path.get(url); }
   String getUrlCode(String url)		{ return url_code.get(url); }
   int getResultCount() 			{ return result_count; }

   @Override public void handleStartTag(HTML.Tag t,MutableAttributeSet a,int pos) {
      if (t == HTML.Tag.DIV) {
         String cls = (String) a.getAttribute(HTML.Attribute.CLASS);
         if (cls != null && cls.contains("code-list-item")) {
            in_result = 1;
          }
         else if (cls != null && cls.contains("sort-bar")) result_count = -2;
       }
      else if (in_result == 1 && t == HTML.Tag.P) {
         String cls = (String) a.getAttribute(HTML.Attribute.CLASS);
         if (cls.contains("title")) {
            in_result = 2;
          }
       }
      else if (in_result == 2 && t == HTML.Tag.A) {
         String href = (String) a.getAttribute(HTML.Attribute.HREF);
         String ttl = (String) a.getAttribute(HTML.Attribute.TITLE);
         if (href != null && ttl != null && ttl.endsWith(".java")) {
            in_result = 3;
            url_path.put(href,ttl);
            file_urls.add(href);
            last_url = href;
            code_buf = new StringBuffer();
          }
       }
      else if (t == HTML.Tag.PRE && code_buf != null) use_code = true;
    }

   @Override public void handleEndTag(HTML.Tag t,int pos) {
      if (t == HTML.Tag.TABLE && use_code) {
	 code_buf.append("\n");
	 url_code.put(last_url,code_buf.toString());
	 use_code = false;
       }
    }

   @Override public void handleText(char [] text,int pos) {
      if (result_count == -2) {
	 String s = new String(text);
	 int delta = 12;
	 int idx = s.indexOf("We've found ");
	 if (idx < 0) {
	    idx = s.indexOf("Showing ");
	    if (idx >= 0) delta = 8;
	  }
	 if (idx >= 0) {
	    s = s.substring(idx+delta);
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
      else if (code_buf != null && use_code) {
	 code_buf.append(text);
       }
    }

}	// end of inner class SearchScanner





/********************************************************************************/
/*										*/
/*	Task to load an individual results page 				*/
/*										*/
/********************************************************************************/

protected class LoadSolution implements Runnable {

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
   
      GithubSource ks = new GithubSource(for_item,txt,item_index);
      KeySearchBase.getSolutions(solution_set,txt,ks,package_fragment);
   
      if (package_fragment != null && solution_set.getScopeType() != CoseScopeType.FILE) {
         int idx = for_item.lastIndexOf("/");
         String furl = for_item.substring(0,idx);
         FileScanner fs = new FileScanner(for_item);
         try {
            // scanURI(GITHUB_SCHEME,GITHUB_AUTHORITY,furl,null,null,true,fs,search_delay);
            scanGithubURI(GITHUB_AUTHORITY,furl,null,fs);
   
            for (String rurl : fs.getUrls()) {
               if (rurl.equals(for_item)) continue;
               String rs = loadFile(rurl);
               if (txt != null) {
        	  KeySearchBase.getSolutions(solution_set,rs,null,package_fragment);
        	}
             }
          }
         catch (S6Exception e) { }
       }
    }

}



private String loadFile(String url)
{
   String txt = null;
   String blob = url.replace("/blob","");
   try {
      // txt = loadURI(GITHUB_SCHEME,GITHUB_FILE_AUTHORITY,blob,null,GITHUB_FRAGMENT,true,search_delay);
      txt = loadGithubURI(GITHUB_FILE_AUTHORITY,blob,null);
    }
   catch (S6Exception e) {
      System.err.println("S6: KEYSEARCH: Problem loading solution: " + e);
      return null;
    }

   return txt;
}


private static class FileScanner extends HTMLEditorKit.ParserCallback {

   private String base_url;
   List<String> found_urls;

   FileScanner(String baseurl) {
      base_url = baseurl;
      found_urls = new ArrayList<String>();
    }

   Collection<String> getUrls() 		{ return found_urls; }

   @Override public void handleStartTag(HTML.Tag t,MutableAttributeSet a,int pos) {
      if (t == HTML.Tag.A) {
         String href = (String) a.getAttribute(HTML.Attribute.HREF);
         String cls = (String) a.getAttribute(HTML.Attribute.CLASS);
         String ttl = (String) a.getAttribute(HTML.Attribute.TITLE);
         if (href != null && cls != null && ttl != null &&
               ttl.endsWith(".java") && cls.contains("js-directory-link")) {
            if (!href.equals(base_url)) found_urls.add(href);
          }
       }
    }

}	// end of inner class FileScanner







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
      SearchScanner ss = new SearchScanner();
      try {
         // scanURI(GITHUB_SCHEME,GITHUB_AUTHORITY,GITHUB_PATH,q,GITHUB_FRAGMENT,true,ss,search_delay);
         scanGithubURI(GITHUB_AUTHORITY,GITHUB_PATH,q,ss);
       }
      catch (S6Exception e) {
         System.err.println("S6: KEYSEARCH: Problem scanning: " + e);
         return;
       }
   
      String url = null;
      for (String iurl : ss.getFileUrls()) {
         String code = ss.getUrlCode(iurl);
         if (code.contains("package ")) {
            if (code.contains("package " + for_package + ";")) {
               url = iurl;
               break;
             }
          }
         else {
            String rs = loadFile(iurl);
            if (rs != null && rs.contains("package " + for_package + ";")) {
               url = iurl;
               break;
             }
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
/*	Github Source								*/
/*										*/
/********************************************************************************/

private static class GithubSource extends KeySearchSource implements CoseSource {

   private String base_link;
   private String base_path;

   GithubSource(String base,String code,int idx) {
      super(code,idx);
      base_link = base;
      int pos = 1;
      for (int i = 0; i < 3; ++i) {
         pos = base.indexOf("/",pos);
       }
      base_path = base.substring(pos+1);
    }

   public String getName()		{ return SOURCE_PREFIX + base_link; }
   public String getDisplayName()	{ return base_path; }

}	// end of subclass GithubSource




/********************************************************************************/
/*										*/
/*	Handle GITHUB quirks							*/
/*										*/
/********************************************************************************/

private String loadGithubURI(String auth,String p,String q) throws S6Exception
{
   URI uri = null;
   try {
       uri = new URI(GITHUB_SCHEME,auth,p,q,GITHUB_FRAGMENT);
    }
   catch (URISyntaxException e) {
      throw new S6Exception("Problem creating uri: " + e,e);
    }

   String page = null;
   synchronized (search_lock) {
      boolean retry = false;
      for (int i = 0; i < 3; ++i) {
	 try {
	    page = loadURI(uri,true,retry);
	    break;
	  }
	 catch (S6Exception e) {
	    if (!e.getMessage().contains(": 429 ")) throw e;
	    if (i == 2) throw e;
	  }
	 try {
	    Thread.sleep(10000);
	  }
	 catch (InterruptedException e) { }
	 retry = true;
       }
    }

   return page;
}


private void scanGithubURI(String auth,String p,String q,HTMLEditorKit.ParserCallback cb)
	throws S6Exception
{
   String cnts = loadGithubURI(auth,p,q);
   scanString(cnts,cb);
}


}	// end of class KeySearchGithub




/* end of KeySearchGithub.java */
