/********************************************************************************/
/*										*/
/*		KeySearchGoogle.java						*/
/*										*/
/*	Keyword-based initial search using Google/codesearch			*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/keysearch/KeySearchGoogle.java,v 1.10 2015/12/23 15:45:01 spr Exp $ */


/*********************************************************************************
 *
 * $Log: KeySearchGoogle.java,v $
 * Revision 1.10  2015/12/23 15:45:01  spr
 * Update search (for UI at least)
 *
 * Revision 1.9  2015/09/23 17:57:58  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.8  2013/09/13 20:32:31  spr
 * Add calls for UI search.
 *
 * Revision 1.7  2012-06-11 18:18:26  spr
 * Include changed/new files for package/ui search
 *
 * Revision 1.6  2012-06-11 14:07:30  spr
 * Code cleanup
 *
 * Revision 1.5  2009-05-12 22:28:23  spr
 * Add scan string routine and use for google.
 *
 * Revision 1.4  2008-11-12 13:51:37  spr
 * Performance and bug updates.
 *
 * Revision 1.3  2008-08-28 00:32:52  spr
 * Next version of S6.	Lots of bug fixes, some new functionality.
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
import java.util.Queue;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.concurrent.Future;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Element;

import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.s6.common.S6Exception;
import edu.brown.cs.s6.common.S6Fragment;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6SolutionSet;
import edu.brown.cs.s6.common.S6Source;

class KeySearchGoogle extends KeySearchBase {



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private final static String    GOOGLE_SCHEME = "http";
private final static String    GOOGLE_AUTHORITY = "www.google.com";
private final static String    GOOGLE_PATH = "/codesearch/feeds/search";
private final static String    GOOGLE_FRAGMENT = null;
private final static String    GOOGLE_QUERY = "max-results=200&q=lang:java";
private final static String    GOOGLE_FPATH = "/codesearch/json";

private final static String    SOURCE_PREFIX = "GOOGLE:";




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

KeySearchGoogle()
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
      String q = GOOGLE_QUERY;
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

   String key = null;
   String furl = null;
   String file = null;
   String proj = null;

   if (tok.hasMoreTokens()) key = tok.nextToken();
   if (tok.hasMoreTokens()) {
      furl = tok.nextToken();
      if (furl.startsWith("*")) furl = null;
    }
   if (tok.hasMoreTokens()) file = tok.nextToken().trim();
   if (tok.hasMoreTokens()) proj = tok.nextToken().trim();

   LoadSolution ls = new LoadSolution(ss,furl,proj,file,key,0);
   Future<Boolean> fb = ss.getEngine().executeTask(S6TaskType.IO,ls);
   synchronized (wq) {
      wq.add(fb);
    }
}




protected void queuePackageSolutions(S6SolutionSet ss,String id,Queue<Future<Boolean>> wq,S6Fragment pfrag,int priority)
{
   
}

protected S6Source createPackageSource(String id,int priority)
{
   return new GoogleSource(id,null,null,null,priority);
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
         rslt = loadURI(GOOGLE_SCHEME,GOOGLE_AUTHORITY,GOOGLE_PATH,q,GOOGLE_FRAGMENT,true);
       }
      catch (S6Exception e) {
         System.err.println("S6: KEYSEARCH: Problem scanning: " + e);
         return;
       }
   
      Element e = IvyXml.convertStringToXml(rslt);
      int sidx = 0;
      for (Element ent : IvyXml.elementsByTag(e,"entry")) {
         String id = IvyXml.getTextElement(ent,"id");
         try {
            id = id.replace(' ','+');
            URI u = new URI(id);
            Element ep = IvyXml.getChild(ent,"gcs:package");
            if (ep == null) continue;
            String qp = IvyXml.getAttrString(ep,"name");
            String purl = IvyXml.getAttrString(ep,"uri");
            Element fp = IvyXml.getChild(ent,"gcs:file");
            String qf = IvyXml.getAttrString(fp,"name");
            if (!qf.endsWith(".java")) continue;      
            switch (solution_set.getScopeType()) {
               case PACKAGE : 
               case PACKAGE_UI :
               case SYSTEM :
                  String xid = IvyXml.convertXmlToString(ent);
                  buildPackageFragment(solution_set,xid,qp,wait_fors,sidx);
                  break;
               default :
                  String url = purl + "/" + qf;
                  String key = u.getFragment();
                  LoadSolution ls = new LoadSolution(solution_set,url,qp,qf,key,sidx++);
                  Future<Boolean> fb = solution_set.getEngine().executeTask(S6TaskType.IO,ls);
                  synchronized (wait_fors) {
                     wait_fors.add(fb);
                   }
                  break;
             }
          }
         catch (URISyntaxException ex) {
            System.err.println("GOOGLE: URI ERROR: " + ex);
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
   private String file_url;
   private String file_name;
   private String google_key;
   private int item_index;

   LoadSolution(S6SolutionSet ss,String url,String proj,String fil,String key,int idx) {
      solution_set = ss;
      file_url = url;
      item_index = idx;
      google_key = key;
      if (proj != null && fil != null) file_name = fil + "@" + proj;
      else if (fil != null) file_name = fil;
      else if (proj != null) file_name = proj;
    }

   public void run() {
      String s = null;
   
      if (file_url != null && google_key == null) {
         // Try to load the file directly from its original source
         try {
            if (file_url.startsWith("http://")) {
               URI uri = new URI(file_url);
               s = loadURI(uri,true);
             }
          }
         catch (Exception e) {
            file_url = null;
          }
       }
   
      if (s == null) {
         String pkey = google_key.substring(0,11);
         String path = google_key.substring(12);
         String q = "file_info_request=b&package_id=" + pkey;
         q += "&path=" + path;
         try {
            ItemParser ic = new ItemParser();
            String t = loadURI(GOOGLE_SCHEME,GOOGLE_AUTHORITY,GOOGLE_FPATH,q,GOOGLE_FRAGMENT,true);
            JSONObject jo = new JSONObject(t);
            JSONArray ja = jo.getJSONArray("file_info_response");
            JSONObject j1 = ja.getJSONObject(0);
            JSONObject j2 = j1.getJSONObject("file_info");
            String html = j2.getString("html_content");
            ic.parse(html);
            s = ic.getCode();
          }
         catch (JSONException e) {
            System.err.println("S6: KEYSEARCH: JSON Problem with solution + : " + e);
            System.err.println("\tKEY = " + google_key);
          }
         catch (S6Exception e) {
            System.err.println("S6: KEYSEARCH: Problem loading solution: " + e);
            e.printStackTrace();
          }
       }
   
      if (s == null) return;
   
      GoogleSource gs = new GoogleSource(file_name,google_key,file_url,s,item_index);
      KeySearchBase.getSolutions(solution_set,s,gs,null);
    }
}



/********************************************************************************/
/*										*/
/*	Methods to parse koders item page					*/
/*										*/
/********************************************************************************/

enum ElementType {
   OTHER,
   LINE,
   SPAN
}


private static class ItemParser {

   private StringBuffer code_buffer;
   private Stack<ElementType> cur_stack;

   ItemParser() {
      code_buffer = new StringBuffer();
    }

   void parse(String cnts) {
      cur_stack = new Stack<ElementType>();
      parseBody(cnts,0);
    }

   void parseBody(String cnts,int pos) {
      while (pos < cnts.length()) {
	 char ch = cnts.charAt(pos);
	 if (ch == '<') {
	    if (cnts.charAt(pos+1) == '/') pos = parseEndElement(cnts,pos+2);
	    else pos = parseElement(cnts,pos+1);
	  }
	 else if (ch == '&') {
	    StringBuffer tok = new StringBuffer();
	    ++pos;
	    while (cnts.charAt(pos) != ';') {
	       tok.append(cnts.charAt(pos));
	       ++pos;
	     }
	    ++pos;
	    String cnm = tok.toString();
	    if (cnm.equals("amp")) code_buffer.append("&");
	    else if (cnm.equals("quot")) code_buffer.append("\"");
	    else if (cnm.equals("apos")) code_buffer.append("'");
	    else if (cnm.equals("lt")) code_buffer.append("<");
	    else if (cnm.equals("gt")) code_buffer.append(">");
	    else if (cnm.startsWith("#") && Character.isDigit(cnm.charAt(1))) {
	       char xch = (char)(Integer.parseInt(cnm.substring(1)));
	       code_buffer.append(xch);
	     }
	    else System.err.println("S6: KEYSEARCH: Illegal escape sequence " + cnm);
	  }
	 else {
	    if (ch != '\n') code_buffer.append(ch);
	    ++pos;
	  }
       }
    }

   int parseElement(String cnts,int pos) {
      while (Character.isWhitespace(cnts.charAt(pos))) ++pos;
      StringBuffer tok = new StringBuffer();
      while (pos < cnts.length()) {
	 char ch = cnts.charAt(pos);
	 if (Character.isWhitespace(ch) || ch == '>') break;
	 tok.append(ch);
	 ++pos;
       }
      ElementType typ = ElementType.OTHER;
      String enm = tok.toString();
      tok.setLength(0);
      while (Character.isWhitespace(cnts.charAt(pos))) ++pos;
      if (enm.equalsIgnoreCase("SPAN") &&
	     cnts.charAt(pos) == 'i' && cnts.charAt(pos+1) == 'd' && cnts.charAt(pos+2) == '=') {
	 typ = ElementType.LINE;
       }
      else if (enm.equalsIgnoreCase("SPAN")) typ = ElementType.SPAN;
      while (cnts.charAt(pos) != '>') ++pos;
      cur_stack.push(typ);
      return pos+1;
    }

   int parseEndElement(String cnts,int pos) {
      while (cnts.charAt(pos) != '>') ++pos;
      ElementType typ = cur_stack.pop();
      if (typ == ElementType.LINE) code_buffer.append("\n");
      return pos+1;
    }

   public String getCode() {
      int idx = code_buffer.length();
      if (idx == 0) return null;
      return code_buffer.toString();
    }

}	// end of subclass ItemParser





/********************************************************************************/
/*										*/
/*	Google Source								*/
/*										*/
/********************************************************************************/

private static class GoogleSource extends KeySearchSource implements S6Source {

   private String file_name;
   private String google_key;
   private String file_url;

   GoogleSource(String fil,String key,String furl,String code,int idx) {
      super(code,idx);
      file_name = fil;
      google_key = key;
      file_url = furl;
    }

   public String getName() {
      String furl = (file_url == null ? "*" : file_url);
      return SOURCE_PREFIX + google_key + "@" + furl + "@" + file_name;
    }

   public String getDisplayName()	{ return file_name; }

}	// end of subclass GoogleSource


}	// end of class KeySearchGoogle



/* end of KeySearchGoogle.java */

