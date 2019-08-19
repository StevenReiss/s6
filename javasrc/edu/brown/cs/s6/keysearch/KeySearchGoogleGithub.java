/********************************************************************************/
/*										*/
/*		KeySearchGoogleGithub.java					*/
/*										*/
/*	Search github using google as a front end				*/
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



package edu.brown.cs.s6.keysearch;


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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.brown.cs.cose.cosecommon.CoseSource;
import edu.brown.cs.s6.common.S6Exception;
import edu.brown.cs.s6.common.S6Fragment;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6SolutionSet;


class KeySearchGoogleGithub extends KeySearchBase
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private final static String	GOOGLE_SCHEME = "https";
private final static String	GOOGLE_AUTHORITY = "www.googleapis.com";
private final static String	GOOGLE_PATH = "/customsearch/v1";
private final static String	GOOGLE_FRAGMENT = null;

private final static String	GITHUB_SCHEME = "https";
private final static String	GITHUB_AUTHORITY = "github.com";
private final static String	GITHUB_PATH = "/search";
private final static String	GITHUB_FRAGMENT = null;
private final static String	GITHUB_FILE_AUTHORITY = "raw.github.com";

private final static String	SOURCE_PREFIX = "GOOGIT:";

private final static int	MAX_PAGES=15;		// 10 per page
private final static int	RESULTS_PER_PAGE = 10;

private static String DEFAULT_API_KEY	   = "AIzaSyBIzSSGjuHFOK0qec1Qt1-TJrnM-GMlxFI";
private static String DEFAULT_ENGINE_ID    = "017842604113442278703:gmfsdi7xocg";
private static int    DEFAULT_RESULT_COUNT = 10;

private String		api_key;
private String		engine_id;
private int		result_count;





/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

KeySearchGoogleGithub()
{
   api_key = DEFAULT_API_KEY;
   engine_id = DEFAULT_ENGINE_ID;
   result_count = DEFAULT_RESULT_COUNT;
}



/********************************************************************************/
/*										*/
/*	Search methods								*/
/*										*/
/********************************************************************************/

protected void queueInitialSolutions(S6SolutionSet ss,int tgtct,Queue<Future<Boolean>> waitfors) throws S6Exception
{
   S6Request.Search sr = ss.getRequest();

   for (S6Request.KeywordSet kws : sr.getKeywordSets()) {
      String q = "";
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



private String generateQuery(String search)
{
   StringBuilder sb = new StringBuilder();
   for (char c : search.toCharArray()) {
      if (c == ' ') {
	 sb.append('+');
       }
      else if (Character.isLetterOrDigit(c)) sb.append(c);
      else {
	  sb.append('%');
	  sb.append(Integer.toHexString(c));
	}
    }
   return sb.toString();
}




protected void queueSpecificSolution(S6SolutionSet ss,String src,Queue<Future<Boolean>> wq)
{
   if (!src.startsWith(SOURCE_PREFIX)) return;
   src = src.substring(SOURCE_PREFIX.length());

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




protected CoseSource createPackageSource(String id,int priority)
{
   return new GoogleGithubSource(id,id,priority);
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
      GoogleQuerier gq = new GoogleQuerier(q,page_number);
      gq.run();
      if (gq.getFileUrls() == null) return;
   
      int idx = page_number * result_count;
      for (String furl : gq.getFileUrls()) {
         String fdir = gq.getUrlPath(furl);
        
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
            int totn = gq.getResultCount();
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

}	// end of inner class ScanSolution




private class GoogleQuerier implements Runnable {

   private String query_string;
   private List<String> file_urls;
   private Map<String,String> url_path;
   private int total_results;

   GoogleQuerier(String query,int page) {
      String q1 = query;
      String s = "key="
	 + api_key + "&cx=" + engine_id + "&q=" + generateQuery(q1)
	 + "&fileType=java"
	 + "&siteSearch=github.com&alt=json";
      if (page > 0) s += "&startIndex=" + (page*result_count+1);
      query_string = s;
      file_urls = new ArrayList<String>();
      url_path = new HashMap<String,String>();
      total_results = -1;
    }

   List<String> getFileUrls()		{ return file_urls; }
   int getResultCount() 		{ return total_results; }

   String getUrlPath(String url) {
      return url_path.get(url);
    }

   @Override public void run()
   {
      try {
         String json = loadURI(GOOGLE_SCHEME,GOOGLE_AUTHORITY,GOOGLE_PATH,
               query_string,GOOGLE_FRAGMENT,true);
         JSONObject jo = new JSONObject(json);
         process(jo);
       }
      catch (Exception e) {
         e.printStackTrace();
       }
    }

   void process(JSONObject jo) {
      if (jo.has("queries")) {
	 JSONObject qjo = getJson(jo,"queries");
	 JSONObject rjo = getJson(qjo,"request");
	 total_results = getInt(rjo,"totalResults",-1);
       }
      JSONArray itms = getArray(jo,"items");
      if (itms != null) {
	 if (total_results < 0) result_count = itms.length();
	 for (int i = 0; i < itms.length(); ++i) {
	    try {
	       JSONObject ijo = itms.getJSONObject(i);
	       String href = ijo.getString("link");
	       if (href == null) continue;
	       if (href.endsWith(".java") && href.startsWith("https://github.com")) {
		  String x = href;
		  x = x.substring(18);
		  url_path.put(href,x);
		  file_urls.add(href);
		}	
	     }
	    catch (JSONException e) { }
	  }
       }
    }

   private JSONObject getJson(JSONObject jo,String q) {
      if (jo != null && jo.has(q)) {
	 Object o = jo.opt(q);
	 if (o == null) return null;
	 if (o instanceof JSONObject) return (JSONObject) o;
	 if (o instanceof JSONArray) {
	    JSONArray ja = (JSONArray) o;
	    return ja.optJSONObject(0);
	  }
       }
      return null;
    }

   private int getInt(JSONObject jo,String q,int dflt) {
      if (jo != null) return jo.optInt(q,dflt);
      return dflt;
    }

   private JSONArray getArray(JSONObject jo,String q) {
      if (jo != null) return jo.optJSONArray(q);
      return null;
    }

}	// end iof inner class GoogleQuerier


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
      result_count = -1;
      use_code = false;
      in_result = 0;
    }

   List<String> getFileUrls()			{ return file_urls; }
   String getUrlCode(String url)		{ return null; }

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
      String txt = loadFile(for_item);
      if (txt == null) return;
   
      GoogleGithubSource ks = new GoogleGithubSource(for_item,txt,item_index);
      KeySearchBase.getSolutions(solution_set,txt,ks,package_fragment);
   
      if (package_fragment != null && solution_set.getScopeType() != CoseScopeType.FILE) {
         int idx = for_item.lastIndexOf("/");
         String furl = for_item.substring(0,idx);
         FileScanner fs = new FileScanner(for_item);
         try {
            scanURI(GITHUB_SCHEME,GITHUB_AUTHORITY,furl,null,null,true,fs);
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
   if (url.startsWith("https://github.com")) {
      url = url.substring(18);
    }
   String blob = url.replace("/blob","");
   System.err.println("LOAD FROM " + url + " " + blob);
   try {
      txt = loadURI(GITHUB_SCHEME,GITHUB_FILE_AUTHORITY,blob,null,GITHUB_FRAGMENT,true);
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
         scanURI(GITHUB_SCHEME,GITHUB_AUTHORITY,GITHUB_PATH,q,GITHUB_FRAGMENT,true,ss);
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

private static class GoogleGithubSource extends KeySearchSource implements CoseSource {

   private String base_link;
   private String base_path;

   GoogleGithubSource(String base,String code,int idx) {
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

}	// end of subclass GoogleGithubSource




}	// end of class KeySearchGoogleGithub




/* end of KeySearchGoogleGithub.java */

