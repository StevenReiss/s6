/********************************************************************************/
/*										*/
/*		KeySearchKrugle.java						*/
/*										*/
/*	Keyword-based initial search using Krugle.com				*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/keysearch/KeySearchKrugle.java,v 1.14 2015/12/23 15:45:01 spr Exp $ */


/*********************************************************************************
 *
 * $Log: KeySearchKrugle.java,v $
 * Revision 1.14  2015/12/23 15:45:01  spr
 * Update search (for UI at least)
 *
 * Revision 1.13  2015/09/23 17:57:59  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.12  2015/02/19 23:33:04  spr
 * Fix warnings.
 *
 * Revision 1.11  2014/08/29 15:16:05  spr
 * Updates for suise, testcases.
 *
 * Revision 1.10  2013/10/10 18:01:14  spr
 * Github updates
 *
 * Revision 1.9  2013/09/13 20:32:31  spr
 * Add calls for UI search.
 *
 * Revision 1.8  2012-08-13 16:51:21  spr
 * Clean up krugle search code
 *
 * Revision 1.7  2012-07-23 19:51:34  djs
 * Updated to allow package-searching
 *
 * Revision 1.6  2012-06-26 19:09:42  djs
 * *** empty log message ***
 *
 * Revision 1.5  2012-06-11 14:07:30  spr
 * Code cleanup
 *
 * Revision 1.4  2009-09-18 01:41:06  spr
 * Handle full class input.
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


import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Future;

import edu.brown.cs.s6.common.S6Exception;
import edu.brown.cs.s6.common.S6Fragment;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6SolutionSet;
import edu.brown.cs.s6.common.S6Source;


class KeySearchKrugle extends KeySearchBase {



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private String		krugle_query;

private final static String	KRUGLE_SCHEME = "http";
private final static String	KRUGLE_AUTHORITY = "opensearch.krugle.org";
private final static String	KRUGLE_PATH = "/search/#query=";

private final static String	SOURCE_PREFIX = "KRUGLE:";

private final int		MAX_RESULTS = 100;


private final static List<String> valid_prefixes;


static {
   valid_prefixes = new ArrayList<String>();
   valid_prefixes.add("http://www.krugle.org/kse/files/");
   valid_prefixes.add("http://www.krugle.org/files/");
   valid_prefixes.add("/kse/files/");
   valid_prefixes.add("/files/");
}



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

KeySearchKrugle()
{
  // krugle_query = KRUGLE_QUERY;
}


/********************************************************************************/
/*										*/
/*	Search method								*/
/*										*/
/********************************************************************************/

protected void queueInitialSolutions(S6SolutionSet ss,int tgtct,Queue<Future<Boolean>> waitfors) throws S6Exception
{

   S6Request.Search sr = ss.getRequest();

   for (S6Request.KeywordSet kws : sr.getKeywordSets()) {
      String q = krugle_query;
      int i = 0;
      for (String s : kws.getWords()) {
	 if (i ++ > 0) q += " ";
	 q += s;
       }
      q += "&language=java";
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

   LoadSolution ls = new LoadSolution(ss,src,0,wq, null, null);
   Future<Boolean> fb = ss.getEngine().executeTask(S6TaskType.IO,ls);
   synchronized (wq) {
      wq.add(fb);
    }
}


protected S6Source createPackageSource(String id,int priority)
{
    return new KrugleSource(id,null,priority);
}


protected void queuePackageSolutions(S6SolutionSet ss,String id,Queue<Future<Boolean>> wq,S6Fragment pf,int priority)
{
    LoadSolution ls = new LoadSolution(ss,id,priority,wq, pf, null);
    Future<Boolean> fb = ss.getEngine().executeTask(S6TaskType.IO,ls);
    synchronized (wq) {
	wq.add(fb);
    }
}

protected boolean addPackages(S6SolutionSet ss,S6Fragment frag,S6Source src,Set<String> pkgs,Queue<Future<Boolean>> wq)
{
    boolean chng = false;

    for (String pkg : pkgs) {
	//System.err.println("Searching for package " + pkg);
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

    //todo: is this ok?
   //private Collection<Future<Boolean>> wait_fors;
    private Queue<Future<Boolean>> wait_fors;
    private S6SolutionSet solution_set;
   private String using_query;


   ScanSolution(S6SolutionSet ss,String q,int pg,Queue<Future<Boolean>> waitfors) {
      wait_fors = waitfors;
      solution_set = ss;
      using_query = q;
    }

   public void run() {
       int j, k;
       //find the beginning of the query in using_query
       for(j = 0; j < using_query.length()-5; j++) {
           if(using_query.substring(j, j+5).equals("query"))
               break;
       }
       //find the end of the query
       for(k = 0; k < using_query.length()-8; k++) {
           if(using_query.substring(k, k+8).equals("language"))
               break;
       }
   
       String query = using_query.substring(j+6, k-1);
   
       //put the query into the base
       String s = "http://opensearch.krugle.org/document/download/?query=" + query + "&language=Java";
       s = s.replace(" ","%20");
   
       try {
          URI uri = new URI(s);
          String results = loadURI(uri,false);
        
          ArrayList<String> a = parseCSV(results);
          ArrayList<String> urls = new ArrayList<String>();
          int mx = Math.min(MAX_RESULTS+3,a.size());
        
          for(int i = 3; i < mx; i+=11 )
             urls.add(a.get(i));
        
          for(int i = 0; i < urls.size(); i++)
             urls.set(i, urls.get(i).replaceAll("view_filecontent", "download_file"));
        
        
          urls.remove(0); //remove the header cell "File URL"
        
          //print the urls we got
          //for(int i = 0; i < urls.size(); i++)
          //System.err.println(urls.get(i));
        
          for(int i = 0; i < urls.size(); i++) {
             switch(solution_set.getScopeType()) {
        	case PACKAGE:
           case PACKAGE_UI :
        	case SYSTEM :
        	   buildPackageFragment(solution_set,urls.get(i), urls.get(i), wait_fors, 0);
        	   break;
        	default:
        	   LoadSolution ls = new LoadSolution(solution_set,urls.get(i),0,wait_fors, null, null);
        	   Future<Boolean> fb = solution_set.getEngine().executeTask(S6TaskType.IO,ls);
        	   synchronized (wait_fors) {
        	      wait_fors.add(fb);
        	    }
        	   break;
              }
           }
        }
       catch (URISyntaxException e) {
          System.err.println("URISyntaxException" + e);
          return;
        }
       catch (S6Exception e) {
          System.err.println("S6Exception " + e);
          return;
        }
       catch (Exception e) {
          System.err.println("S6 error: " + e.getClass().toString());
          return;
        }
   
   }

   /*
   public void run() {
       int j, k;
       //find the beginning of the query in using_query
       for(j = 0; j < using_query.length()-5; j++) {
	   if(using_query.substring(j, j+5).equals("query"))
	       break;
       }
       //find the end of the query
       for(k = 0; k < using_query.length()-8; k++) {
	   if(using_query.substring(k, k+8).equals("language"))
	       break;
       }

       String query = using_query.substring(j+6, k-1);

       //put the query into the base
       String s = "http://opensearch.krugle.org/document/download/?query=" + query + "&language=Java";
       for(int i = 0; i < s.length(); i++) {
	   if(s.charAt(i) == ' ')
	       s = s.substring(0, i) + "%20" + s.substring(i+1, s.length());
       }

       //TODO debugging
       switch (solution_set.getSearchType()) {
       case PACKAGE :
	   System.err.println("case package");
	   break;
       default :
	   System.err.println("case default");
	   break;
       }

       try {
	   URI uri = new URI(s);
	   String results = loadURI(uri);

	   ArrayList<String> a = parse(results);
	   ArrayList<String> urls = new ArrayList<String>();

	   for(int i = 3; i < a.size(); i+=11 )
	       urls.add(a.get(i));

	   for(int i = 0; i < urls.size(); i++)
	       urls.set(i, urls.get(i).replaceAll("view_filecontent", "download_file"));


	   urls.remove(0); //remove the header cell "File URL"

	   //print the urls we got
	   //for(int i = 0; i < urls.size(); i++)
	       //System.err.println(urls.get(i));

	   System.err.println("urls size is " + urls.size());
	   for(int i = 0; i < urls.size(); i++) {
	       LoadSolution ls = new LoadSolution(solution_set,urls.get(i),0);
	       Future<Boolean> fb = solution_set.getEngine().executeTask(S6TaskType.IO,ls);
	       synchronized (wait_fors) {
		   wait_fors.add(fb);
	       }
	   }
       }
       catch (URISyntaxException e) {
	   System.err.println("URISyntaxException");
	   return;
       }
       catch (S6Exception e) {
	   System.err.println("S6Exception");
	   return;
       }
       catch (Exception e) {
	   System.err.println("S6 error: " + e.getClass().toString());
	   return;
       }




   }
 * */


}

//TODO should maybe make this parsing algorithm more general
//it parses a .csv file into its cells and returns them
//in an ArrayList<String>
private ArrayList<String> parseCSV(String s) {

    ArrayList<String> ret = new ArrayList<String>();

    int tmp = 0;
    for(int i = 0; i < s.length(); i++) {
	if(ret.size() % 11 == 10) { //the last cell in a row behaves differently, because
	    //it ends with a newline, not a comma
	    if(s.charAt(i) == '"') {
		if(s.length() > i+1 && s.charAt(i+1) == '\n') {
		    ret.add(s.substring(tmp+1, i));
		    tmp = i + 2;
		}
	    }
	}
	else {
	    if(s.charAt(i) == ',') {
		ret.add(s.substring(tmp+1, i-1));
		tmp = i + 1;
	    }
	}
    }
    return ret;
}

/********************************************************************************/
/*										*/
/*	Methods to parse the krugle result page 				*/
/*										*/
/********************************************************************************/

/**************************

private static Pattern MATCH_PATTERN = Pattern.compile("out of about (\\d*) matching files");


private class ResultCallback extends HTMLEditorKit.ParserCallback {

   private boolean in_results;
   private Set<String> found_urls;
   private int num_files;

   ResultCallback() {
      in_results = false;
      found_urls = new LinkedHashSet<String>();
      num_files = 0;
    }

   Iterable<String> getFoundUrls()		{ return found_urls; }

   public void handleStartTag(HTML.Tag t,MutableAttributeSet a,int pos) {
      if (in_results && t == HTML.Tag.A) {
	 String s = (String) a.getAttribute(HTML.Attribute.HREF);
	 found_urls.add(s);
       }
    }

   public void handleText(char [] data,int pos) {
      String s = new String(data);
      if (s.equals("__SPLIT__")) in_results = true;
      else if (!in_results) {
	 Matcher m = MATCH_PATTERN.matcher(s);
	 if (m.find()) {
	    String ct = m.group(1);
	    num_files = Integer.parseInt(ct);
	  }
       }
    }

}	// end of subclass ResultCallback






/********************************************************************************/
/*										*/
/*	Methods for handling page loads 					*/
/*										*/
/********************************************************************************/

private class LoadSolution implements Runnable {

   private S6SolutionSet solution_set;
   private String for_url;
   private int item_index;
   private Queue<Future<Boolean>> wait_fors;
   private S6Fragment package_fragment;
   private String target_package;

   LoadSolution(S6SolutionSet ss,String url,int idx, Queue<Future<Boolean>> waitfors, S6Fragment pf, String pkg) {
      solution_set = ss;
      for_url = url;
      item_index = idx;
      wait_fors = waitfors;
      package_fragment = pf;
      target_package = pkg;
    }

   public void run() {
      String qr = "http://" + for_url;
      String s = null;
   
      try {
         URI uri = new URI(qr);
         s = loadURI(uri,true);
         //s = loadURI(KRUGLE_SCHEME,KRUGLE_AUTHORITY,KRUGLE_RELATED,qr,KRUGLE_FRAGMENT);
       }
      catch (S6Exception e) {
         System.err.println("S6: KEYSEARCH: Problem loading solution: " + e);
         return;
       }
      catch (URISyntaxException e) {
         System.err.println("S6: KEYSEARCH: Invalid Syntax");
         return;
       }
      catch (Exception e) {
         System.err.println("Error");
         System.err.println(e.getClass().toString());
         return;
       }
   
      if(target_package != null) {
          if(!s.contains(target_package + ";"))
              return;
      }
   
      //debugging
      System.err.println("Adding file " + qr);
   
      KrugleSource src = new KrugleSource(for_url,s,item_index);
      KeySearchBase.getSolutions(solution_set,s,src,package_fragment);
   
      if(package_fragment == null) return;
   
      String pkg = "test";
      for(int i = 0; i < s.length() - 7; i++) {
         if(s.substring(i, i+7).equals("package")) {
            for(int j = i+7; j < s.length(); j++) {
               if(s.charAt(j) == '\n') {
        	  pkg = s.substring(i+8, j);
        	  break;
        	}
             }
          }
       }
   
      //TODO: this is really hackish, and should probably be changed sometime. it needs to be like
      //this because it's how scanSolution parses the input.
      pkg = "package " + pkg + ";";
      pkg = "query=" + pkg + " language";
   
      ScanSolution ssol = new ScanSolution(solution_set,pkg,0,wait_fors);
      Future<Boolean> fb = solution_set.getEngine().executeTask(S6TaskType.IO,ssol);
      synchronized (wait_fors) {
          wait_fors.add(fb);
      }
   }
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
        //put the query into the base
        String s = "http://opensearch.krugle.org/document/download/?query=" + for_package + "&language=Java";
        s = s.replace(" ","%20");
    
        try {
            URI uri = new URI(s);
            String results = loadURI(uri,true);
    
            ArrayList<String> a = parseCSV(results);
            ArrayList<String> urls = new ArrayList<String>();
    
            for(int i = 3; i < a.size(); i+=11 )
        	urls.add(a.get(i));
    
            for(int i = 0; i < urls.size(); i++)
        	urls.set(i, urls.get(i).replaceAll("view_filecontent", "download_file"));
    
    
            urls.remove(0); //remove the header cell "File URL"
    
            //print the urls we got
            //for(int i = 0; i < urls.size(); i++)
            //System.err.println(urls.get(i));
    
            for (int i = 0; i < urls.size(); i++) {
               LoadSolution ls = new LoadSolution(solution_set,urls.get(i),0,wait_fors, package_fragment, for_package);
               Future<Boolean> fb = solution_set.getEngine().executeTask(S6TaskType.IO,ls);
               synchronized (wait_fors) {
        	  wait_fors.add(fb);
        	}
    
             }
         }
        catch (URISyntaxException e) {
            System.err.println("S6: KEYSEARCH: URISyntaxException " + e);
            return;
        }
        catch (S6Exception e) {
            System.err.println("S6: KEYSEARCH: S6Exception " + e);
            return;
        }
        catch (Exception e) {
            System.err.println("S6 error: " + e.getClass().toString());
            return;
        }
    }


}


/********************************************************************************/
/*										*/
/*	Source class								*/
/*										*/
/********************************************************************************/

private static class KrugleSource extends KeySearchSource implements S6Source {

   private String source_key;
   KrugleSource(String key,String code,int idx) {
      super(code,idx);
      source_key = key;
    }

   public String getName()			{ return SOURCE_PREFIX + source_key; }
   public String getDisplayName()		{ return source_key; }

}	// end of subclass KrugleSource




}	// end of class KeySearchKrugle




/* end of KeySearchKrugle.java */
