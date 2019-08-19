/********************************************************************************/
/*										*/
/*		KeySearchRepoOpenHub.java					*/
/*										*/
/*	Interface to OpenHub repository 					*/
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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.ByteArrayOutputStream;

import edu.brown.cs.cose.cosecommon.CoseSource;
import edu.brown.cs.s6.common.S6Request;

import org.jsoup.nodes.*;
import org.jsoup.select.Elements;



class KeySearchRepoOpenHub extends KeySearchRepo
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private Map<URI,String> 	project_map;
private Map<URI,String> 	pid_map;
private Map<URI,String> 	path_map;

private final static String	OPENHUB_SCHEME = "http";
private final static String	OPENHUB_AUTHORITY = "code.openhub.net";
private final static String	OPENHUB_PATH = "/search";
private final static String	OPENHUB_FRAGMENT = null;
private final static String	OPENHUB_QUERY = "s=";
private final static String	OPENHUB_FILE_PATH = "/file";

private final static String	SOURCE_PREFIX = "OPENHUB:";

private final static int	RESULTS_PER_PAGE = 10;
private final static int	SIMULTANEOUS_SEARCH = 1;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

KeySearchRepoOpenHub(S6Request.Search sr)
{
   super(sr,SIMULTANEOUS_SEARCH);

   project_map = new HashMap<URI,String>();
   pid_map = new HashMap<URI,String>();
   path_map = new HashMap<URI,String>();
}



/********************************************************************************/
/*										*/
/*	Abstract Method Implementations 					*/
/*										*/
/********************************************************************************/

@Override public URI getURIFromSourceString(String src)
{
   if (!src.startsWith(SOURCE_PREFIX)) return null;

   try {
      return new URI(src.substring(SOURCE_PREFIX.length()));
    }
   catch (URISyntaxException e) { }

   return null;
}


@Override public int getResultsPerPage()	{ return RESULTS_PER_PAGE; }



@Override protected URI getURIForSearch(List<String> keys,S6SearchLanguage lang,String projectid,int page)
{
   String q = OPENHUB_QUERY;
   int i = 0;
   for (String s : keys) {
      if (i++ > 0) q += " ";
      if (s.contains(" ") || s.contains(".")) q += "\"" + s + "\"";
      else q += s;
    }
   if (projectid !=null) {
      q += "&fp=" + projectid;
    }

   switch (lang) {
      case JAVA :
	 q += "&fl=Java";
	 break;
      case XML :
	 q += "&fl=Xml";
	 break;
    }

   if (page > 0) q += "&p=" + page;

   try {
      URI uri = new URI(OPENHUB_SCHEME,OPENHUB_AUTHORITY,OPENHUB_PATH,q,OPENHUB_FRAGMENT);
      return uri;
    }
   catch (URISyntaxException e) { }
   return null;
}



@Override CoseSource createSource(URI uri,String cnts,int idx)
{
   String proj = project_map.remove(uri);
   String path = path_map.remove(uri);
   String pid = pid_map.remove(uri);
   return new OpenHubSource(uri.toString(),cnts,proj,pid,path,idx);
}



/********************************************************************************/
/*										*/
/*	Search page scanning							*/
/*										*/
/********************************************************************************/

@Override List<URI> getSearchPageResults(Element jsoup)
{
   List<URI> rslt = new ArrayList<URI>();

   Elements keys = jsoup.select("div.snippetResult");
   for (Element sresult : keys) {
      Elements refs = sresult.select("div.fileNameLabel a");
      if (refs.size() == 0) {
	 System.err.println("BAD PAGE");
	 continue;
       }
      Element ref = refs.get(0);
      Elements prefs = sresult.select("div.projectNameLabel a");
      if (prefs.size() == 0) {
	 System.err.println("BAD PAGE");
	 continue;
       }
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
      String path = ref.attr("title");
      String dpath = getParam(pref.attr("href"),"did");
      if (dpath != null) {
	 dpath = dpath.replace("&2F","/");
	 path = dpath + "/" + path;
       }
      try {
	 URI uri = new URI(OPENHUB_SCHEME,OPENHUB_AUTHORITY,OPENHUB_FILE_PATH,q,null);
	 rslt.add(uri);
       }
      catch (URISyntaxException e) { }
    }

   return rslt;
}



@Override boolean hasMoreSearchPages(URI uri,String cnts,int page)
{
   if (cnts == null) return false;
   return cnts.contains("class='next'");
}



private String getParam(String url,String id)
{
   int idx = url.indexOf(id + "=");
   if (idx < 0) return null;
   idx += id.length() + 1;
   int idx1 = url.indexOf("&",idx);
   if (idx1 < 0) return url.substring(idx);
   return url.substring(idx,idx1);
}



/********************************************************************************/
/*										*/
/*	Path access methods							*/
/*										*/
/********************************************************************************/

@Override URI getURIForPath(CoseSource src,String path)
{
   if (!(src instanceof OpenHubSource)) return null;

   OpenHubSource osrc = (OpenHubSource) src;
   String spath = src.getDisplayName();
   if (path == null || !path.startsWith("/")) {
      int idx = spath.lastIndexOf("/");
      if (idx > 0) spath = spath.substring(0,idx);
      else spath = "";
      if (path == null) path = spath;
      else if (spath.equals("")) spath = path;
      else path = spath + "/" + path;
    }
   URI u = osrc.getPathURI(path);

   project_map.put(u,src.getProjectId());
   pid_map.put(u,src.getProjectId());
   path_map.put(u,path);

   return u;
}


@Override List<URI> getDirectoryContentsURIs(URI baseuri,CoseSource src,Element jsoup)
{
   // TODO: OpenHub doesn't seem to index or provide access to .png and other binary files
   // as part of their repository.
   List<URI> rslt = new ArrayList<URI>();
   OpenHubSource osrc = (OpenHubSource) src;

   Elements results = jsoup.select("td.column_B a");
   for (Element result : results) {
      String href = result.attr("href");
      int idx = href.indexOf("?");
      String q = href.substring(idx+1);
      q = q.replace("%2F","/");
      href = href.substring(0,idx);
      try {
	 URI u = new URI(OPENHUB_SCHEME,OPENHUB_AUTHORITY,href,q,null);
	 rslt.add(u);
	 project_map.put(u,osrc.getProjectId());
	 pid_map.put(u,osrc.getProjectPid());
	 String path = getParam(u.toString(),"did");
	 if (path == null) {
	    path = getParam(baseuri.toString(),"did");
	    String fnm = result.attr("title");
	    path = path + "/" + fnm;
	  }
	 path_map.put(u,path);
	 // need to set path_map, project_map,pid_mpa
       }
      catch (URISyntaxException e) { }
    }

   return rslt;
}




/********************************************************************************/
/*										*/
/*	Result page scanning							*/
/*										*/
/********************************************************************************/

@Override protected String getRawFileContents(URI uri,Element jsoup)
{
   Elements projs = jsoup.select("#selectedProject");
   String pid = projs.get(0).attr("value");
   project_map.put(uri,pid);

   projs = jsoup.select("#projectName a");
   String href = projs.get(0).attr("href");
   int idx1 = href.indexOf("pid=");
   int idx2 = href.indexOf("&",idx1);
   pid = href.substring(idx1+4,idx2);
   pid_map.put(uri,pid);

   Elements dirs = jsoup.select("#folderEllipse span");
   String dir = dirs.get(0).attr("title");
   Elements files = jsoup.select("span.project_info_text");
   String file = files.get(0).attr("title");
   if (file != null) {
      if (dir != null) file = dir + "/" + file;
      path_map.put(uri,file);
    }

   Elements cvs = jsoup.select("div.code_view");
   String elt = cvs.get(0).text();
   if (!elt.endsWith("\n")) elt += "\n";
   return elt;
}



@Override protected boolean shouldRetry(ByteArrayOutputStream xcnts,long delta)
{
   boolean fg = false;
   String cnts = getString(xcnts);
   if (cnts.contains("No code results were found.")) fg = true;
   if (cnts.contains("0 of about 0 results found for")) fg = true;

   if (fg) System.err.println("RETRY CHECK " + delta);

   if (delta < 2000 && fg) {
      fg = false;
    }

   return fg;
}




/********************************************************************************/
/*										*/
/*	OpenHub Source								*/
/*										*/
/********************************************************************************/

private static class OpenHubSource extends KeySearchSource implements CoseSource {

   private String base_link;
   private String base_path;
   private String project_id;
   private String project_pid;

   OpenHubSource(String base,String code,String proj,String pid,String path,int idx) {
      super(code,idx);
      base_link = base;
      base_path = path;
      project_id = proj;
      project_pid = pid;
    }

   @Override public String getName()		{ return SOURCE_PREFIX + base_link; }
   @Override public String getDisplayName()	{ return base_path; }
   @Override public String getProjectId()	{ return project_id; }

   String getProjectPid()			{ return project_pid; }

   URI getPathURI(String path) {
      String q = "pid=" + project_pid;
      q += "&cid=" + getParam(base_link,"cid");
      q += "&fp=" + project_id;
      q += "&did=" + path;
      try {
	 URI u = new URI(OPENHUB_SCHEME,OPENHUB_AUTHORITY,"/project",q,null);
	 return u;
       }
      catch (URISyntaxException e) { }
      return null;
    }

   private String getParam(String url,String id) {
      int idx = url.indexOf(id + "=");
      if (idx < 0) return null;
      idx += id.length() + 1;
      int idx1 = url.indexOf("&",idx);
      if (idx1 < 0) return url.substring(idx);
      return url.substring(idx,idx1);
    }

}	// end of subclass OpenHubSource


}	// end of class KeySearchRepoOpenHub




/* end of KeySearchRepoOpenHub.java */

