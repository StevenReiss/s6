/********************************************************************************/
/*										*/
/*		KeySearchOhloh.java						*/
/*										*/
/*	Keyword-based initial search using Ohloh.net				*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/keysearch/KeySearchOhloh.java,v 1.9 2016/07/18 23:05:04 spr Exp $ */


/*********************************************************************************
 *
 * $Log: KeySearchOhloh.java,v $
 * Revision 1.9  2016/07/18 23:05:04  spr
 * Keysearch updates for applications, ui search.
 *
 * Revision 1.8  2015/09/23 17:57:59  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.7  2015/03/31 02:22:39  spr
 * Format updates
 *
 * Revision 1.6  2015/03/11 18:04:13  spr
 * Update search for proper caching and error checking.
 *
 * Revision 1.5  2015/02/19 23:33:04  spr
 * Fix warnings.
 *
 * Revision 1.4  2015/02/18 23:23:42  spr
 * Fix retry problems with ohloh.
 *
 * Revision 1.3  2015/02/14 19:40:12  spr
 * Add test case generation.
 *
 * Revision 1.2  2014/08/29 15:16:05  spr
 * Updates for suise, testcases.
 *
 * Revision 1.1  2013/09/20 20:58:59  spr
 * Missing code files.
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.keysearch;


import java.net.URI;
import java.net.URISyntaxException;
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

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.Jsoup;




class KeySearchOhloh extends KeySearchBase {



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private final static String	OHLOH_SCHEME = "http";
private final static String	OHLOH_AUTHORITY = "code.openhub.net";
private final static String	OHLOH_PATH = "/search";
private final static String	OHLOH_FRAGMENT = null;
private final static String	OHLOH_QUERY = "s=";
private final static String	OHLOH_QUERY_TAIL = "&fl=Java";
private final static String     OHLOH_FILE_PATH = "/file";

private final static String	SOURCE_PREFIX = "OHLOH:";

private final static int	MAX_PAGES=15;	// @ 10 per page

private static Object		search_lock = new Object();



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

KeySearchOhloh()
{ 
   setMaximumSimultaneousQueries(1);
}




/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

@Override protected int getResultsPerPage()             { return 10; }


@Override protected URI createSearchURI(S6Request.Search req,String projectid,int page)
{
   String q = OHLOH_QUERY;
   int i = 0;
   for (S6Request.KeywordSet kws : req.getKeywordSets()) {
      for (String s : kws.getWords()) {
         if (i++ > 0) q += " ";
         if (s.contains(" ")) q += "\"" + s + "\"";
         else q += s;
       }
    }
   if (projectid !=null) {
      q += "&fp=" + projectid;
    }
   q += OHLOH_QUERY_TAIL;
   if (page > 0) q += "&p=" + page;
   
   try {
      URI uri = new URI(OHLOH_SCHEME,OHLOH_AUTHORITY,OHLOH_PATH,q,null);
      return uri;
    }
   catch (URISyntaxException e) { }
   return null;
}



@Override protected boolean shouldRetry(Exception e) 
{
   return false;
}



@Override protected boolean shouldRetry(String cnts)
{
   if (cnts.contains("0 of about 0 results found for")) return true;
   if (cnts.contains("No code results were found.")) return true;
   
   return false;
}


@Override protected SearchPageScanner getSearchPageScanner(URI uri,String text)
{
   return new OhlohSearchPageScanner(uri,text);
}


@Override protected CoseSource createSource(URI baseuri,String code,int idx)
{
   return new OhlohSource(baseuri.toString(),null,null,code,idx);
}


@Override protected URI getURIFromSource(String src)
{
   if (!src.startsWith(SOURCE_PREFIX)) return null;
   String urisrc = src.substring(SOURCE_PREFIX.length());
   try {
      URI uri = new URI(urisrc);
      return uri;
    }
   catch (URISyntaxException e) { }
   return null;
}



@Override protected FilePageScanner getFilePageScanner(String cnts,URI uri)
{
   return new OhlohFileScanner(cnts,uri);
}


@Override protected List<URI> getURIsForPackage(URI baseuri,String txt)
{
   Element doc = Jsoup.parse(txt,baseuri.toString());
   List<URI> rslt = new ArrayList<URI>();
   Elements links = doc.select("div.exp_items a");
   for (Element link : links) {
      String href = link.attr("href");
      int idx = href.indexOf("?");
      String path = href.substring(0,idx);
      String q = href.substring(idx+1);
      int idx1 = q.indexOf("&s=");
      if (idx1 > 0) q = q.substring(0,idx1);
      try {
         URI uri = new URI(OHLOH_SCHEME,OHLOH_AUTHORITY,path,q,null);
         rslt.add(uri);
       }
      catch (URISyntaxException e) { }
    }
   return rslt;
}


@Override protected URI getFileContentsURI(URI base)
{
   return base;
}


@Override protected URI createPackageURI(String pkg)
{
   String q = "s=\"" + pkg + "\"";
   try {
      URI uri = new URI(OHLOH_SCHEME,OHLOH_AUTHORITY,OHLOH_PATH,q,null);
      return uri;
    }
   catch (URISyntaxException e) { }
   return null;
}



private static class OhlohSearchPageScanner extends SearchPageScanner {

   OhlohSearchPageScanner(URI uri,String text) {
      super(uri.toString(),text);
    }
   
   @Override List<URI> getFileUris() {
      List<URI> rslt = new ArrayList<URI>();
      
      Map<String,String> pidmap = new HashMap<String,String>();
      Elements pmap = jsoup_doc.select("#fp div.facetList");
      for (Element melt : pmap) {
         String val = null;
         Elements e1 = melt.getElementsByTag("input");
         for (Element ielt : e1) {
            val = ielt.val();
            break;
          }
         Elements e2 = melt.getElementsByClass("tileText");
         for (Element nelt : e2) {
            String text = nelt.text();
            pidmap.put(text,val);
          }
       }
      
      Elements keys = jsoup_doc.select("div.snippetResult");
      for (Element sresult : keys) {
         Elements refs = sresult.select("div.fileNameLable a");
         Element ref = refs.get(0);
         Elements prefs = sresult.select("div.porojectNameLable a");
         Element pref = prefs.get(0);
         StringBuffer code = new StringBuffer();
         Elements lines = sresult.select("div.snippet pre");
         for (Element line : lines) {
            code.append(line.text());
            code.append("\n");
          }
         String rawpath = ref.attr("href");
         String fid = getParam(rawpath,"fid");
         String cid = getParam(rawpath,"cid");
         String q = "fid=" + fid + "&cid=" + cid;
         String pname = pref.attr("title");
         String pid = pidmap.get(pname);
         String path = ref.attr("title");
         String dpath = getParam(pref.attr("href"),"did");
         dpath = dpath.replace("&2F","/");
         path = dpath + "/" + path;
         try {
            URI uri = new URI(OHLOH_SCHEME,OHLOH_AUTHORITY,OHLOH_FILE_PATH,q,null);
            if (pid != null) project_map.put(uri,pid);
            path_map.put(uri,path);
            code_map.put(uri,code.toString());
            rslt.add(uri);
          }
         catch (URISyntaxException e) { }
       }
      
      return rslt;
    }
   
   private String getParam(String url,String id) {
      int idx = url.indexOf(id + "=");
      if (idx < 0) return null;
      idx += id.length() + 1;
      int idx1 = url.indexOf("&",idx);
      if (idx1 < 0) return url.substring(idx);
      return url.substring(idx,idx1);
    }

}       // end of inner class OhlohSearchPageScanner



private static class OhlohFileScanner extends FilePageScanner {
   
   OhlohFileScanner(String cnts,URI uri) {
      super(cnts,uri.toString());
    }
   
   @Override String getFileContents() {
      Elements cvs = jsoup_doc.select("div.code_view");
      String elt = cvs.get(0).text();
      return elt;
    }
   
}       // end of inner class OhlohFileScanner




/********************************************************************************/
/*										*/
/*	Search method								*/
/*										*/
/********************************************************************************/

protected void queueInitialSolutions(S6SolutionSet ss,int ct,Queue<Future<Boolean>> waitfors) throws S6Exception
{
   S6Request.Search sr = ss.getRequest();

   for (S6Request.KeywordSet kws : sr.getKeywordSets()) {
      String q = OHLOH_QUERY;
      int i = 0;
      for (String s : kws.getWords()) {
	 if (i ++ > 0) q += " ";
	 q += s;
       }
      q += OHLOH_QUERY_TAIL;
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

   switch (ss.getSearchType()) {
      case PACKAGE :
      case UIFRAMEWORK :
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



protected CoseSource createPackageSource(String id,int priority)
{
   return new OhlohSource(id,id,null,null,priority);
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
      if (page_number > 0) q += "&p=" + page_number;
      SearchScanner scanner = new SearchScanner();
      try {
         scanOhlohURI(OHLOH_PATH,q,scanner);
       }
      catch (S6Exception e) {
         System.err.println("S6: KEYSEARCH: Problem scanning: " + e);
         return;
       }
   
      int idx = scanner.getResultStart();
      for (String furl : scanner.getFileUrls()) {
         String fdir = scanner.getUrlPath(furl);
   
         switch (solution_set.getSearchType()) {
            case PACKAGE :
            case UIFRAMEWORK :
            case ANDROIDUI :
            case APPLICATION :
               // fdir should be the directory, not the full path if scope is not FILE
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
            int ppg = scanner.getResultSize();
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
   private int result_size;
   private int result_start;
   private boolean use_code;

   SearchScanner() {
      file_urls = new ArrayList<String>();
      url_path = new HashMap<String,String>();
      url_code = new HashMap<String,String>();
      code_buf = null;
      use_code = false;
      result_count = -1;
      result_size = -1;
      result_start = -1;
    }

   List<String> getFileUrls()			{ return file_urls; }
   String getUrlPath(String url)		{ return url_path.get(url); }
   String getUrlCode(String url)		{ return url_code.get(url); }
   int getResultSize()				{ return result_size; }
   int getResultCount() 			{ return result_count; }
   int getResultStart() 			{ return result_start; }

   @Override public void handleStartTag(HTML.Tag t,MutableAttributeSet a,int pos) {
      if (t == HTML.Tag.A) {
	 String href = (String) a.getAttribute(HTML.Attribute.HREF);
	 String ttl = (String) a.getAttribute(HTML.Attribute.TITLE);
	 if (href != null && href.startsWith("/file?") && ttl != null && ttl.endsWith(".java")) {
	    int idx = href.indexOf("&s");
	    if (idx > 0) href = href.substring(0,idx);
	    if (href.equals(last_url)) {
	       url_path.put(last_url,ttl);
	       if (code_buf != null) url_code.put(last_url,code_buf.toString());
	     }
	    else {
	       file_urls.add(href);
	       last_url = href;
	       code_buf = new StringBuffer();
	     }
	  }
       }
      else if (t == HTML.Tag.SPAN) {
	 String id = (String) a.getAttribute(HTML.Attribute.ID);
	 if (id != null && id.equalsIgnoreCase("resultsfound")) result_count = -2;
       }
      else if (t == HTML.Tag.DIV) {
	 String id = (String) a.getAttribute(HTML.Attribute.ID);
	 if (id != null && id.equalsIgnoreCase("resultSize")) result_size = -2;
       }
      else if (t == HTML.Tag.PRE && code_buf != null) use_code = true;
      else {
	 if (result_count == -2) result_count = -1;
	 if (result_size == -2) result_size = -1;
       }
    }

   @Override public void handleEndTag(HTML.Tag t,int pos) {
      if (t == HTML.Tag.PRE && use_code) {
	 code_buf.append("\n");
	 use_code = false;
       }
    }

   @Override public void handleText(char [] text,int pos) {
      if (result_count == -2) {
	 String s = new String(text);
	 int idx1 = s.indexOf("Results ");
	 if (idx1 >= 0) {
	    String s1 = s.substring(idx1+8);
	    idx1 = s1.indexOf(" ");
	    s1 = s1.substring(0,idx1);
	    try {
	       result_start = Integer.parseInt(s1);
	     }
	    catch (NumberFormatException e) { }
	  }
	 int idx = s.indexOf(" of about ");
	 if (idx >= 0) {
	    s = s.substring(idx+10);
	    idx = s.indexOf(" ");
	    if (idx > 0) s = s.substring(0,idx);
	    s = s.replace(",","");
	    try {
	       result_count = Integer.parseInt(s);
	     }
	    catch (NumberFormatException e) { }
	  }
       }
      else if (result_size == -2) {
	 String s = new String(text);
	 try {
	    result_size = Integer.parseInt(s);
	  }
	 catch (NumberFormatException e) { }
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
      int idx = p.indexOf("?");
      String q = p.substring(idx+1);
      p = p.substring(0,idx);
   
      FileScanner scanner = new FileScanner();
   
      try {
         scanOhlohURI(p,q,scanner);
       }
      catch (S6Exception e) {
         System.err.println("S6: KEYSEARCH: Problem loading solution: " + e);
         return;
       }
   
      Map<String,String> rel = scanner.getRelatedFiles();
      String ipth = scanner.getParentPath();
      String s = scanner.getText();
   
      if (package_fragment != null && solution_set.getScopeType() != CoseScopeType.FILE) {
         for (String rurl : rel.values()) {
            int ridx = rurl.indexOf("&s");
            if (ridx > 0) rurl = rurl.substring(0,ridx);
            if (rurl == for_item) continue;
            FileScanner fs = new FileScanner();
            try {
               int widx = rurl.indexOf("?");
               String wp = rurl.substring(0,widx);
               String wq = rurl.substring(widx+1);
               scanOhlohURI(wp,wq,fs);
             }
            catch (S6Exception e) {
               System.err.println("S6: KEYSEARCH: problem loading related solution: " + e);
               return;
             }
            String rs = fs.getText();
            KeySearchBase.getSolutions(solution_set,rs,null,package_fragment);
          }
       }
      else {
         OhlohSource ks = new OhlohSource(for_item,ipth,rel,s,item_index);
         KeySearchBase.getSolutions(solution_set,s,ks,package_fragment);
       }
    }
}


private static class FileScanner extends HTMLEditorKit.ParserCallback {

   private StringBuffer string_buffer;
   private Map<String,String> rel_files;
   private String parent_file;
   private int in_code;
   private boolean in_file;
   private boolean in_title;
   private String file_href;

   FileScanner() {
      string_buffer = new StringBuffer();
      rel_files = new HashMap<String,String>();
      parent_file = null;
      in_code = 0;
      in_file = false;
      in_title = false;
      file_href = null;
    }

   String getText() {
      int ln = string_buffer.length();
      if (ln == 0) return null;
      if (string_buffer.charAt(ln-1) != '\n') string_buffer.append("\n");
      return string_buffer.toString();
    }

   Map<String,String> getRelatedFiles() {
      return rel_files;
    }

   String getParentPath()			{ return parent_file; }

   @Override public void handleStartTag(HTML.Tag t,MutableAttributeSet a,int pos) {
      if (t == HTML.Tag.DIV) {
	 String cls = (String) a.getAttribute(HTML.Attribute.CLASS);
	 String id = (String) a.getAttribute(HTML.Attribute.ID);
	 if (cls != null && cls.equals("code_view")) in_code = 1;
	 else if (in_code > 0) ++in_code;
	 else if (cls != null && cls.equals("exp_items")) in_file = true;
	 else if (id != null && id.equals("folderEllipse")) in_title = true;
       }
      else if (t == HTML.Tag.A && in_file) {
	 file_href = (String) a.getAttribute(HTML.Attribute.HREF);
       }
      else if (t == HTML.Tag.SPAN && in_title) {
	 String id = (String) a.getAttribute(HTML.Attribute.TITLE);
	 if (id != null) parent_file = id;
       }
    }

   @Override public void handleEndTag(HTML.Tag t,int pos) {
      if (t == HTML.Tag.DIV && in_code > 0) --in_code;
      else if (in_file) in_file = false;
    }

   @Override public void handleText(char [] text,int pos) {
      if (in_code > 0) {
	 string_buffer.append(text);
       }
      else if (file_href != null) {
	 String id = new String(text);
	 rel_files.put(id,file_href);
	 file_href = null;
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
      String q= "s=\"" + for_package + "\"";
      SearchScanner ss = new SearchScanner();
      try {
         scanOhlohURI(OHLOH_PATH,q,ss);
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
            FileScanner fs = new FileScanner();
            try {
               int idx = iurl.indexOf("?");
               String p1 = iurl.substring(0,idx);
               String q1 = iurl.substring(idx+1);
               scanOhlohURI(p1,q1,fs);
               String rs = fs.getText();
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
/*	Ohloh Source								*/
/*										*/
/********************************************************************************/

private static class OhlohSource extends KeySearchSource implements CoseSource {

   private String base_link;
   private String base_path;

   OhlohSource(String base,String path,Map<String,String> rel,String code,int idx) {
      super(code,idx);
      base_link = base;
      base_path = path;
    }

   public String getName()		{ return SOURCE_PREFIX + base_link; }
   public String getDisplayName()	{ return base_path; }

}	// end of subclass OhlohSource




/********************************************************************************/
/*										*/
/*	Handle OHLOH quirks							*/
/*										*/
/********************************************************************************/

private String loadOhlohURI(String p,String q) throws S6Exception
{
   URI uri = null;
   try {
       uri = new URI(OHLOH_SCHEME,OHLOH_AUTHORITY,p,q,OHLOH_FRAGMENT);
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
	  }
	 catch (S6Exception e) {
	    if (!e.getMessage().contains(": XXX for URL")) throw e;
	    if (i == 2) throw e;
	  }
	 if (page != null) {
	    boolean isbad = false;
	    if (page.contains("0 of about 0 results found for")) isbad = true;
	    if (page.contains("No code results were found.")) isbad = true;
	    if (!isbad) break;
	    if (i == 2) {
	       markForced(uri);
	       break;
	     }
	    else if (checkIfForced(uri)) break;
	  }
	 try {
	    System.err.println("S6: KEYSEARCH: No results found for " + uri);
	    Thread.sleep(10000);
	  }
	 catch (InterruptedException e) { }
	 retry = true;
       }
    }

   return page;
}


private void scanOhlohURI(String p,String q,HTMLEditorKit.ParserCallback cb)
	throws S6Exception
{
   String cnts = loadOhlohURI(p,q);
   scanString(cnts,cb);
}




}	// end of class KeySearchOhloh



/* end of KeySearchOhloh.java */

