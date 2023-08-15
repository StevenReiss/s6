/********************************************************************************/
/*										*/
/*		TgenOhloh.java							*/
/*										*/
/*	Ohloh interface for getting candidate test case files			*/
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



package edu.brown.cs.s6.tgen;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import edu.brown.cs.ivy.exec.IvyExec;
import edu.brown.cs.cose.keysearch.KeySearchCache;


class TgenOhloh implements TgenConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private Map<String,String>	project_id_map;
private KeySearchCache		tgen_cache;
private int			current_page;

private final static String	OHLOH_SCHEME = "http";
private final static String	OHLOH_AUTHORITY = "code.openhub.net";
private final static String	OHLOH_PATH = "/search";
private final static String	OHLOH_QUERY = "s=";
private final static String	OHLOH_QUERY_TAIL = "&fl=Java";
private final static String	OHLOH_PROJECT_PATH = "/project";
private final static String	OHLOH_PROJECT_QUERY = "pid=";





/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

TgenOhloh()
{
   project_id_map = new HashMap<String,String>();
   tgen_cache = KeySearchCache.getCache();
   current_page = 0;
}





/********************************************************************************/
/*										*/
/*	Get a set of sources							*/
/*										*/
/********************************************************************************/

List<TgenSource> getNextSourceSet(String keys,TgenSource orig,TgenProject proj)
{
   return getSourceSetPaged(keys,orig,proj,current_page++);
}


List<TgenSource> getSpecificSourceSet(String keys,int pg,TgenSource orig,TgenProject proj)
{
   return getSourceSetPaged(keys,orig,proj,pg);
}

void skipNextSourceSet()
{
   ++current_page;
}

void setStartPage(int pno)
{
   current_page = pno;
}


List<TgenSource> getSourceSet(String keys,TgenSource orig,TgenProject proj)
{
   return getSourceSetPaged(keys,orig,proj,0);
}


private List<TgenSource> getSourceSetPaged(String keys,TgenSource orig,TgenProject proj,int pno)
{
   List<String> toks = IvyExec.tokenize(keys);
   List<URI> uris = generateSearchURIs(toks,proj,pno);
   if (uris == null) return null;

   List<TgenSource> rslt = new ArrayList<TgenSource>();

   for (URI uri : uris) {
      try {
	 String text = loadURL(uri.toURL(),true);
	 if (text == null) break;
	 Element doc = Jsoup.parse(text,uri.toString());
	 if (!addSources(uri,doc,orig,proj,rslt)) break;
       }
      catch (IOException e) {
	 System.err.println("TGEN: Problem converting url " + uri + ": " + e);
       }
      catch (Exception e) {
	 System.err.println("TGEN: Problem loading url " + uri + ": " + e);
       }
    }

   return rslt;
}



private boolean addSources(URI base,Element doc,TgenSource orig,
      TgenProject proj,List<TgenSource> rslts)
{
   Elements pmap = doc.select("#fp div.facetList");
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
	 project_id_map.put(text,val);
       }
    }

   Elements keys = doc.select("div.snippetResult div.projectNameLabel a");
   Elements pths = doc.select("div.snippetResult div.filePath a");
   int ct = Math.min(keys.size(),pths.size());
   if (ct == 0) return false;
   for (int i = 0; i < ct; ++i) {
      Element pelt = keys.get(i);
      Element felt = pths.get(i);
      String pnam = pelt.attr("title").replace("/","@").replace("&","_");
      String url = pelt.attr("href");
      int idx = url.indexOf("pid=");
      int idx1 = url.indexOf("&",idx);
      String pid = null;
      if (idx > 0 && idx1 > 0) pid = url.substring(idx+4,idx1);
      else if (idx > 0) pid = url.substring(idx+4);
      url = felt.attr("href");
      String key = felt.attr("title");
      String src = null;
      URI href = null;
      String fid = getParam(url,"fid");
      String cid = getParam(url,"cid");
      if (fid != null && cid != null) {
         src = "OHLOH:/file?fid=" + fid + "&cid=" + cid;
         int idx2 = url.indexOf("?");
         String nurl = url.substring(0,idx2+1);
         nurl += "fid=" + fid + "&cid=" + cid + "&dl=undefined";
         href = base.resolve(nurl);
       }

      TgenSource fs = new TgenSource(this,pnam,pid,key,src,href,orig);
      if (fs.isValid()) rslts.add(fs);
    }

   return true;
}


private String getParam(String url,String id) {
   int idx = url.indexOf(id + "=");
   if (idx < 0) return null;
   idx += id.length() + 1;
   int idx1 = url.indexOf("&",idx);
   if (idx1 < 0) return url.substring(idx);
   return url.substring(idx,idx1);
}



List<URI> generateSearchURIs(List<String> toks,TgenProject proj,int pno)
{
   List<URI> rslt = new ArrayList<URI>();

   String q = OHLOH_QUERY;
   int wct = 0;
   for (String s : toks) {
      if (wct++ > 0) q += " ";
      if (s.contains(" ")) {
	 q += "\"" + s + "\"";
       }
      else q += s;
    }
   if (proj != null) {
      String pid = project_id_map.get(proj.getName());
      if (pid == null) pid = project_id_map.get(proj.getId());
      if (pid == null) pid = findPidForProject(proj);
      if (pid != null) q += "&fp=" + pid;
    }
   q += OHLOH_QUERY_TAIL;
   if (pno > 0) q += "&p=" + pno;
   try {
      URI uri = new URI(OHLOH_SCHEME,OHLOH_AUTHORITY,OHLOH_PATH,q,null);
      rslt.add(uri);
    }
   catch (URISyntaxException e) {
      System.err.printf("TGEN: Problem with ohloh url: " + e);
    }

   return rslt;
}


private String findPidForProject(TgenProject rp)
{
   String q = OHLOH_PROJECT_QUERY + rp.getId();
   try {
      URI uri = new URI(OHLOH_SCHEME,OHLOH_AUTHORITY,OHLOH_PROJECT_PATH,q,null);
      URL url = uri.toURL();
      String pstr = loadURL(url,true);
      Element doc = Jsoup.parse(pstr,url.toString());
      Element inp = doc.getElementById("selectedProject");
      if (inp != null) {
	 String val = inp.val();
	 if (val != null) project_id_map.put(rp.getId(),val);
	 return val;
       }
    }
   catch (Exception e) {
      System.err.println("TGEN: Problem with ohloh url: " + e);
    }

   return null;
}



/********************************************************************************/
/*										*/
/*	Network routines							*/
/*										*/
/********************************************************************************/

String loadURL(URL url,boolean cache) throws IOException
{
   System.err.println("TGEN: LOAD URI " + url + " " + cache);

   String cnts = null;
   boolean retry = false;
   for (int i = 0; i < 3; ++i) {
      StringBuilder buf = new StringBuilder();
      try {
	 BufferedReader br = tgen_cache.getReader(url,cache,retry);
	 for ( ; ; ) {
	    String ln = br.readLine();
	    if (ln == null) break;
	    buf.append(ln);
	    buf.append("\n");
	  }
	 br.close();
       }
      catch (IOException e) {
	 System.err.println("TGEN: Problem accessing url " + url + ": " + e);
	 return null;
       }
      cnts = buf.toString();
      retry = false;
      boolean isbad = false;
      if (cnts.contains("0 of about 0 results found for")) isbad = true;
      if (cnts.contains("No code results were found.")) isbad = true;
      if (!isbad) break;
      if (tgen_cache.checkIfForced(url)) break;
      try {
	 Thread.sleep(10000);
       }
      catch (InterruptedException e) { }
      retry = true;
    }

   if (retry) {
      System.err.println("TGEN: Mark forced: " + url);
      tgen_cache.markForced(url);
    }

   return cnts;
}




/********************************************************************************/
/*										*/
/*	Common methods for getting source information				*/
/*										*/
/********************************************************************************/

static String findFileName(String text,String path)
{
   if (text == null || path == null) return null;
   String pats = "^\\s*package\\s+([A-Za-z0-9]+(\\s*\\.\\s*[A-Za-z0-9]+)*)\\s*\\;";
   Pattern pat = Pattern.compile(pats);
   Matcher mat = pat.matcher(text);
   int idx = path.lastIndexOf("/");
   String file = (idx < 0 ? path : path.substring(idx+1));
   if (mat.find()) {
      String pkg = mat.group(1);
      StringTokenizer tok = new StringTokenizer(pkg,". \t\n\f");
      StringBuffer buf = new StringBuffer();
      while (tok.hasMoreTokens()) {
	 String elt = tok.nextToken();
	 buf.append(elt);
	 buf.append("/");
       }
      buf.append(file);
      file = buf.toString();
    }
   return file;
}




static String findPackageName(String text)
{
   if (text == null) return null;
   String pats = "^\\s*package\\s+([A-Za-z0-9]+(\\s*\\.\\s*[A-Za-z0-9]+)*)\\s*\\;";
   Pattern pat = Pattern.compile(pats,Pattern.MULTILINE);
   Matcher mat = pat.matcher(text);
   if (!mat.find()) return "";

   String pkg = mat.group(1);
   StringTokenizer tok = new StringTokenizer(pkg,". \t\n\f");
   StringBuffer buf = new StringBuffer();
   int ctr = 0;
   while (tok.hasMoreTokens()) {
      String elt = tok.nextToken();
      if (ctr++ > 0) buf.append(".");
      buf.append(elt);
    }
   return buf.toString();
}



}	// end of class TgenOhloh




/* end of TgenOhloh.java */

