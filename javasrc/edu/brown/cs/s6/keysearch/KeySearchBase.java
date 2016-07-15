/********************************************************************************/
/*										*/
/*		KeySearchBase.java						*/
/*										*/
/*	Generic class for keyword based initial search				*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/keysearch/KeySearchBase.java,v 1.15 2015/09/23 17:57:58 spr Exp $ */


/*********************************************************************************
 *
 * $Log: KeySearchBase.java,v $
 * Revision 1.15  2015/09/23 17:57:58  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.14  2015/02/18 23:23:42  spr
 * Fix retry problems with ohloh.
 *
 * Revision 1.13  2015/02/14 19:40:12  spr
 * Add test case generation.
 *
 * Revision 1.12  2013/09/13 20:32:31  spr
 * Add calls for UI search.
 *
 * Revision 1.11  2013-05-09 12:26:18  spr
 * Minor changes to start ui fixups.
 *
 * Revision 1.10  2012-07-20 22:15:02  spr
 * Cleaup code.
 *
 * Revision 1.9  2012-06-20 12:21:28  spr
 * Initial fixes for UI search
 *
 * Revision 1.8  2012-06-11 14:07:30  spr
 * Code cleanup
 *
 * Revision 1.7  2009-05-12 22:28:23  spr
 * Add scan string routine and use for google.
 *
 * Revision 1.6  2008-11-12 13:51:37  spr
 * Performance and bug updates.
 *
 * Revision 1.5  2008-08-28 00:32:52  spr
 * Next version of S6.	Lots of bug fixes, some new functionality.
 *
 * Revision 1.4  2008-07-18 22:26:39  spr
 * Don't compile initially.
 *
 * Revision 1.3  2008-07-17 13:46:29  spr
 * Add labrador-based searching.
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
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;

import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Exception;
import edu.brown.cs.s6.common.S6Fragment;
import edu.brown.cs.s6.common.S6KeySearch;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6SolutionSet;
import edu.brown.cs.s6.common.S6Source;


import java.util.List;
import java.util.ArrayList;

abstract public class KeySearchBase implements S6KeySearch, S6Constants, KeySearchConstants {



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private ParserDelegator parser_delegator;
private Map<S6SolutionSet,Set<String>> package_items;
private Semaphore     request_sema;


private static KeySearchCache	url_cache = new KeySearchCache();

protected static boolean do_debug = false;
private static final int MAX_RETRY = 5;





/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

protected KeySearchBase()
{
   parser_delegator = new ParserDelegator();
   package_items = new WeakHashMap<S6SolutionSet,Set<String>>();
   request_sema = null;
}




/********************************************************************************/
/*										*/
/*	Creation methods							*/
/*										*/
/********************************************************************************/

public static S6KeySearch createKeySearch(S6SolutionSet ss)
{
   S6Request.Search sr = ss.getRequest();
   KeySearchSet kb;

   do_debug = sr.doDebug();

   kb = new KeySearchSet();

   int ct = 0;
   for (S6Location loc : sr.getLocations()) {
      KeySearchBase next = null;
      switch (loc) {
	 case KODERS :
	 case OHLOH :
	 case OPENHUB :
	    next = new KeySearchOhloh();
	    break;
	 case GITHUB :
	    next = new KeySearchGithub();
	    break;
	 case LOCAL :
	    next = new KeySearchLabrador();
	    break;
	 default :
	    System.err.println("Search engine " + loc + " no longer supported");
	    break;
       }
      if (next != null) {
	++ct;
	kb.add(next);
       }
    }
   if (ct == 0) kb.add(new KeySearchGithub());

   return kb;
}



/********************************************************************************/
/*										*/
/*	Top level method to get solutions					*/
/*										*/
/********************************************************************************/

public final void getInitialSolutions(S6SolutionSet ss) throws S6Exception
{
   Queue<Future<Boolean>> waitfors = new LinkedList<Future<Boolean>>();

   Iterable<String> spsrc = ss.getRequest().getSpecificSources();

   if (spsrc != null) {
      for (String s : spsrc) {
	 queueSpecificSolution(ss,s,waitfors);
       }
    }
   else {
      queueInitialSolutions(ss,TARGET_RESULTS,waitfors);
    }

   ss.getEngine().waitForAll(waitfors);
}



protected abstract void queueInitialSolutions(S6SolutionSet ss,int ct,Queue<Future<Boolean>> waitfors)
	throws S6Exception;

protected abstract void queueSpecificSolution(S6SolutionSet ss,String src,Queue<Future<Boolean>> w)
	throws S6Exception;



/********************************************************************************/
/*										*/
/*	Handle package fragments						*/
/*										*/
/********************************************************************************/

protected void buildPackageFragment(S6SolutionSet ss,String id,String key,Queue<Future<Boolean>> waitfors,int priority)
{
   synchronized (package_items) {
      Set<String> used = package_items.get(ss);
      if (used == null) {
	 used = new HashSet<String>();
	 package_items.put(ss,used);
       }
      if (!used.add(key)) return;
    }
   S6Fragment pfrag = ss.getEngine().createPackageFragment(ss.getRequest());
   S6Source psrc = createPackageSource(id,priority);
   if (psrc == null) return;

   Queue<Future<Boolean>> subwaits = new LinkedList<Future<Boolean>>();

   try{
      queuePackageSolutions(ss,id,subwaits,pfrag,priority);
    }
   catch (S6Exception e) {
      System.err.println("S6: KEYSEARCH: Problem building package fragment: " + e);
    }

   PkgTask pt  = new PkgTask(ss,pfrag,psrc,subwaits,waitfors);
   Future<Boolean> fb = ss.getEngine().executeTask(S6TaskType.IO,pt);
   synchronized (waitfors) {
      waitfors.add(fb);
    }
}



private class PkgTask implements Callable<Boolean> {

   private S6SolutionSet solution_set;
   private Queue<Future<Boolean>> wait_queue;
   private Queue<Future<Boolean>> master_queue;
   private S6Source package_source;
   private S6Fragment package_fragment;

   PkgTask(S6SolutionSet ss,S6Fragment pf,S6Source ps,Queue<Future<Boolean>> wq,
	      Queue<Future<Boolean>> mq) {
      solution_set = ss;
      wait_queue = wq;
      master_queue = mq;
      package_source = ps;
      package_fragment = pf;
    }

   @Override public Boolean call() {
      int waitct = 0;
      synchronized (wait_queue) {
         for (Future<Boolean> fb : wait_queue) {
            if (!fb.isDone()) ++waitct;
          }
       }
      if (waitct > 0) {
         Future<Boolean> fb = solution_set.getEngine().executeTask(S6TaskType.IO,this);
         synchronized (master_queue) {
            master_queue.add(fb);
          }
         return false;
       }
   
      Collection<S6Fragment> ifs = package_fragment.getFileFragments();
      if (ifs == null || ifs.size() == 0) return false;
   
      // here is were we extend the solution to include other packages
      Set<String> pkgs = null;
      if (solution_set.getScopeType() == S6ScopeType.SYSTEM) {
         pkgs = solution_set.getEngine().getRelatedProjects(package_fragment);
       }
      if (pkgs != null) {
         for (Iterator<String> it = pkgs.iterator(); it.hasNext(); ) {
            String p = it.next();
            if (!package_fragment.addPackage(p)) it.remove();
          }
         if (pkgs.size() > 0) {
            if (addPackages(solution_set,package_fragment,package_source,pkgs,wait_queue)) {
               Future<Boolean> fb = solution_set.getEngine().executeTask(S6TaskType.IO,this);
               synchronized (master_queue) {
        	  master_queue.add(fb);
        	}
               return false;
             }
          }
       }
   
      solution_set.addInitialSolution(package_fragment,package_source);
      return true;
    }

}	// end of inner class PkgTask




protected abstract void queuePackageSolutions(S6SolutionSet ss,String id,
      Queue<Future<Boolean>> waitfors,S6Fragment pfrag,int priority) throws S6Exception;

protected S6Source createPackageSource(String id,int priority)
{
   return null;
}


protected boolean addPackages(S6SolutionSet ss,S6Fragment frag,S6Source src,Set<String> pkgs,Queue<Future<Boolean>> wq)
{
   return false;
}




/********************************************************************************/
/*										*/
/*	New scanning methods							*/
/*										*/
/********************************************************************************/

protected void setMaximumSimultaneousQueries(int ct)
{
   request_sema = new Semaphore(ct);
}


protected void newQueueInitialSolutions(S6SolutionSet ss,int nresult,Queue<Future<Boolean>> waitfors)
	throws S6Exception
{
   int rpp = getResultsPerPage();
   int npages = (nresult + rpp - 1)/rpp;
   for (int i = 0; i < npages; ++i) {
      ScanSearchResults ssr = new ScanSearchResults(ss,waitfors,i);
      Future<Boolean> fb = ss.getEngine().executeTask(S6TaskType.IO,ssr);
      synchronized (waitfors) {
	 waitfors.add(fb);
       }
    }
}


protected void newQueueSpecificSolution(S6SolutionSet ss,String src,Queue<Future<Boolean>> waits)
{
   URI uri = getURIFromSource(src);
   if (uri == null) return;
   switch (ss.getSearchType()) {
      case PACKAGE :
      case UIFRAMEWORK :
      case ANDROIDUI :
      case APPLICATION :
	 String u = uri.toString();;
	 buildPackageFragment(ss,u,u,waits,0);
	 break;
      default :
	 LoadSolution ls = new LoadSolution(ss,uri,0,null);
	 Future<Boolean> fb = ss.getEngine().executeTask(S6TaskType.IO,ls);
	 synchronized (waits) {
	    waits.add(fb);
	  }
	 break;
    }
}


private void newQueuePackageSolutions(S6SolutionSet ss,URI id,Queue<Future<Boolean>> wq,
      S6Fragment pkgfrag,int priority)
{
   LoadSolution ls = new LoadSolution(ss,id,priority,pkgfrag);
   Future<Boolean> fb = ss.getEngine().executeTask(S6TaskType.IO,ls);
   synchronized (wq) {
      wq.add(fb);
    }
}


private boolean newAddPackages(S6SolutionSet ss,S6Fragment frag,S6Source src,
      Set<String> pkgs,Queue<Future<Boolean>> wq)
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



protected int getResultsPerPage()		{ return 10; }
protected URI getURIFromSource(String src)	{ return null; }


private class ScanSearchResults implements Runnable {

   private Queue<Future<Boolean>> wait_fors;
   private S6SolutionSet solution_set;
   private int page_number;

   ScanSearchResults(S6SolutionSet ss,Queue<Future<Boolean>> waitfors,int page) {
      wait_fors = waitfors;
      solution_set = ss;
      page_number = page;
    }

   @Override public void run() {
      S6Request.Search sr = solution_set.getRequest();
      URI uri = createSearchURI(sr,null,page_number);
      if (uri == null) return;
      try {
         String txt = getResultPage(uri);
         SearchPageScanner ssp = getSearchPageScanner(uri,txt);
         int idx = getResultsPerPage() * page_number;
         for (URI fileuri : ssp.getFileUris()) {
            String fdir = ssp.getUrlPath(fileuri);
            switch (solution_set.getSearchType()) {
               case PACKAGE :
               case UIFRAMEWORK :
               case ANDROIDUI :
               case APPLICATION :
        	  buildPackageFragment(solution_set,fileuri.toString(),fdir,wait_fors,idx++);
        	  break;
               default :
        	  LoadSolution ls = new LoadSolution(solution_set,fileuri,idx++,null);
        	  Future<Boolean> fb = solution_set.getEngine().executeTask(S6TaskType.IO,ls);
        	  synchronized (wait_fors) {
        	     wait_fors.add(fb);
        	   }
        	  break;
             }
          }
       }
      catch (S6Exception e) { }
    }

}	// end of inner class ScanSearchResults



protected String getResultPage(URI uri) throws S6Exception
{
   String page = null;

   if (request_sema != null) {
      request_sema.acquireUninterruptibly();
    }
   try {
      boolean retry = false;
      for (int i = 0; i < MAX_RETRY; ++i) {
	 try {
	    page = loadURI(uri,true,retry);
	    if (!shouldRetry(page)) break;
	  }
	 catch (S6Exception e) {
	    if (i == MAX_RETRY-1) throw e;
	    if (!shouldRetry(e)) throw e;
	  }
       }
    }
   finally {
      if (request_sema != null) request_sema.release();
    }

   return page;
}



protected boolean shouldRetry(Exception e)
{
   return false;
}


protected boolean shouldRetry(String cnts)
{
   return false;
}


protected URI createSearchURI(S6Request.Search req,String projectid,int page)
{
   return null;
}


protected SearchPageScanner getSearchPageScanner(URI uri,String text)
{
   return null;
}


protected static abstract class SearchPageScanner {

   protected Element jsoup_doc;
   protected Map<URI,String> path_map;
   protected Map<URI,String> project_map;
   protected Map<URI,String> code_map;

   SearchPageScanner(String uri,String text) {
       jsoup_doc = Jsoup.parse(text,uri);
       path_map = new HashMap<URI,String>();
       project_map = new HashMap<URI,String>();
       code_map = new HashMap<URI,String>();
    }

   abstract List<URI> getFileUris();
   String getUrlPath(URI uri)		{ return path_map.get(uri); }
   String getProjectId(URI uri) 	{ return project_map.get(uri); }
   String getUrlCode(URI uri)		{ return code_map.get(uri); }

}	// end of inner class SearchPageScan



protected abstract static class FilePageScanner {

   protected Element jsoup_doc;

   FilePageScanner(String text,String uri) {
       jsoup_doc = Jsoup.parse(text,uri);
    }

   abstract String getFileContents();

}	// end of inner class SearchPageScan



private class LoadSolution implements Runnable {

   private S6SolutionSet solution_set;
   private URI for_item;
   private int item_index;
   private S6Fragment package_fragment;

   LoadSolution(S6SolutionSet ss,URI uri,int idx,S6Fragment pf) {
      solution_set = ss;
      for_item = uri;
      item_index = idx;
      package_fragment = pf;
    }

   public void run() {
      String txt = loadFile(for_item);
      if (txt == null) return;
      S6Source src = createSource(for_item,txt,item_index);
      getSolutions(solution_set,txt,src,package_fragment);
   
      if (package_fragment != null && solution_set.getScopeType() != S6ScopeType.FILE) {
         for (URI pkguri : getURIsForPackage(for_item,txt)) {
            if (pkguri.equals(for_item)) continue;
            String rs = loadFile(pkguri);
            if (txt != null) {
               getSolutions(solution_set,rs,null,package_fragment);
             }
          }
       }
    }


}	// end of inner class LoadSolution



private String loadFile(URI uri)
{
   try {
      URI cntsuri = getFileContentsURI(uri);
      String page = getResultPage(cntsuri);
      FilePageScanner fps = getFilePageScanner(page,cntsuri);
      if (fps != null) {
	 page = fps.getFileContents();
       }
      return page;
    }
   catch (S6Exception e) {
      return null;
    }
}

protected FilePageScanner getFilePageScanner(String cnts,URI uri)
{
   return null;
}



protected S6Source createSource(URI baseuri,String text,int idx)	{ return null; }

protected URI getFileContentsURI(URI base)
{
   return base;
}

protected List<URI> getURIsForPackage(URI baseuri,String txt)
{
   return new ArrayList<URI>();
}



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
      URI uri = createPackageURI(for_package);
      String txt = null;
      try {
         txt = getResultPage(uri);
       }
      catch (S6Exception e) { }
      if (txt == null) return;
      SearchPageScanner ssp = getSearchPageScanner(uri,txt);
      URI rslt = null;
      for (URI fileuri : ssp.getFileUris()) {
         String code = ssp.getUrlCode(fileuri);
         if (code.contains("package ")) {
            if (code.contains("package " + for_package + ";")) {
               rslt = fileuri;
               break;
             }
          }
         else {
            String rs = loadFile(fileuri);
            if (rs != null && rs.contains("package " + for_package + ";")) {
               rslt = fileuri;
               break;
             }
          }
       }
   
      if (rslt == null) return;
   
      LoadSolution ls = new LoadSolution(solution_set,rslt,0,package_fragment);
      Future<Boolean> fb = solution_set.getEngine().executeTask(S6TaskType.IO,ls);
      synchronized (wait_fors) {
         wait_fors.add(fb);
       }
    }

}



protected URI createPackageURI(String pkg)		{ return null; }



/********************************************************************************/
/*										*/
/*	Method to scan a URL							*/
/*										*/
/********************************************************************************/

protected void scanURI(String sch,String host,String path,String query,String frag,
      boolean cache,HTMLEditorKit.ParserCallback cb)
		throws S6Exception
{
   try {
      URI uri = new URI(sch,host,path,query,frag);
      scanURI(uri,cache,cb);
    }
   catch (URISyntaxException e) {
      throw new S6Exception("Problem creating uri: " + e,e);
    }
}




protected void scanURI(URI uri,boolean cache,HTMLEditorKit.ParserCallback cb)
throws S6Exception
{
   scanURI(uri,cache,cb,false);
}


protected void scanURI(URI uri,boolean cache,HTMLEditorKit.ParserCallback cb,boolean reread)
	throws S6Exception
{
   if (do_debug) System.err.println("S6: KEYSEARCH: LOAD: " + uri + " " + cache);

   try {
      URL url = uri.toURL();
      BufferedReader br = url_cache.getReader(url,cache,reread);
      parser_delegator.parse(br,cb,true);
      br.close();
    }
   catch (IOException e) {
      throw new S6Exception("Problem accessing url " + uri + ": " + e,e);
    }
   catch (Throwable t) {
      throw new S6Exception("Problem parsing url " + uri + ": " + t,t);
    }
}






protected static String loadURI(String sch,String host,String path,String query,String frag,boolean cache)
	throws S6Exception
{
   try {
      URI uri = new URI(sch,host,path,query,frag);
      return loadURI(uri,cache);
    }
   catch (URISyntaxException e) {
      throw new S6Exception("Problem creating uri: " + e,e);
    }
}




protected static String loadURI(URI uri,boolean cache) throws S6Exception
{
   return loadURI(uri,cache,false);
}

protected static String loadURI(URI uri,boolean cache,boolean reread) throws S6Exception
{
   if (do_debug) System.err.println("S6: KEYSEARCH: LOAD: " + uri + " " + cache);

   StringBuilder buf = new StringBuilder();

   try {
      URL url = uri.toURL();
      BufferedReader br = url_cache.getReader(url,cache,reread);
      for ( ; ; ) {
	 String ln = br.readLine();
	 if (ln == null) break;
	 // if (do_debug) System.err.println("LINE: " + ln);
	 buf.append(ln);
	 buf.append("\n");
       }
      br.close();
    }
   catch (IOException e) {
      throw new S6Exception("Problem accessing url " + uri + ": " + e,e);
    }

   return buf.toString();
}



protected void scanString(String s,HTMLEditorKit.ParserCallback cb)
{
   if (s == null) return;

   try {
      StringReader sr = new StringReader(s);
      parser_delegator.parse(sr,cb,true);
    }
   catch (IOException e) {
      System.err.println("S6: KEYSEARCH: IO exception parsing string: " + e);
    }
}



protected void markForced(URI uri)
{
   try {
      url_cache.markForced(uri.toURL());
    }
   catch (IOException e) { }
}


protected boolean checkIfForced(URI uri)
{
   try {
      return url_cache.checkIfForced(uri.toURL());
    }
   catch (IOException e) {
      return false;
    }
}



/********************************************************************************/
/*										*/
/*	Methods to parse a source file and build solutions			*/
/*										*/
/********************************************************************************/

protected static void getSolutions(S6SolutionSet ss,String code,S6Source src,S6Fragment pkg)
{
   if (src != null && !ss.useSource(src) && pkg == null) return;

   S6Fragment ff = ss.getEngine().createFileFragment(code,src,ss.getRequest());
   if (ff == null) return;

   ff.resolveFragment();

   if (pkg == null) {
      for (S6Fragment cf : ff.getFragments(ss.getSearchType())) {
	 ss.addInitialSolution(cf,src);
       }
    }
   else {
      pkg.addInnerFragment(ff);
    }
}



}	// end of class KeySearchBase



/* end of KeySearchBase.java */
