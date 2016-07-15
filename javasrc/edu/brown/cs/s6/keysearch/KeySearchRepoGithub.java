/********************************************************************************/
/*										*/
/*		KeySearchRepoGithub.java					*/
/*										*/
/*	Interface to GITHUB repository						*/
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

import edu.brown.cs.ivy.file.IvyFile;
import edu.brown.cs.s6.common.*;

import java.io.*;
import java.net.*;
import java.util.*;

import org.json.*;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;


class KeySearchRepoGithub extends KeySearchRepo
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

protected final static String	GITHUB_SCHEME = "https";
protected final static String	GITHUB_AUTHORITY = "github.com";
private final static String	GITHUB_PATH = "/search";
protected final static String	GITHUB_FRAGMENT = null;
private final static String	GITHUB_QUERY_TAIL = "&ref=advsearch&type=Code";
private final static String	GITHUB_FILE_AUTHORITY = "raw.github.com";

private final static String	SOURCE_PREFIX = "GITHUB:";

private final static int	RESULTS_PER_PAGE = 10;
private final static int	SIMULTANEOUS_SEARCH = 1;

private static OAuthData	oauth_token = null;
private static String		github_auth;

private final static String	S6_CLIENT_ID = "92367cf10da5b70932fa";
private final static String	S6_CLIENT_SECRET = "53e04859dec97346e3cd9f886b4e847c4d7cc2dc";
private final static String	S6_FINGERPRINT;
private static boolean	  use_github_api = false;

static {
   S6_FINGERPRINT = "S6_" + Math.round(Math.random()*1000000000);
   getOAuthToken();
}



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

KeySearchRepoGithub(S6Request.Search sr)
{
   super(sr,SIMULTANEOUS_SEARCH);
}


/********************************************************************************/
/*										*/
/*	Abstract Method Implementations 					*/
/*										*/
/********************************************************************************/

@Override URI getURIFromSourceString(String src)
{
   if (!isRelevantSource(src)) return null;

   try {
      int idx = src.indexOf(":");
      return new URI(src.substring(idx+1));
    }
   catch (URISyntaxException e) { }

   return null;
}



protected boolean isRelevantSource(String src)
{
   return src.startsWith(SOURCE_PREFIX);
}


@Override int getResultsPerPage()
{
   if (oauth_token != null && use_github_api) return 100;

   return RESULTS_PER_PAGE;
}



@Override String getAuthorization()
{
   if (oauth_token != null && use_github_api) {
      return "token " + oauth_token.getToken();
    }

   if (github_auth != null) {
      return github_auth;
    }

   return null;
}


@Override protected URI getURIForSearch(List<String> keys,S6SearchLanguage lang,String projectid,int page)
{
   String q = "";
   String langstr = null;
   switch (lang) {
      case JAVA :
	 langstr = "java";
	 break;
      case XML :
	 langstr = "xml";
	 break;
    }

   q += "q=";
   int i = 0;
   for (String s : keys) {
      if (i++ > 0) q += " ";
      if (s.contains(" ")) q += "\"" + s + "\"";
      else q += s;
    }
   if (projectid != null) q += " repo:" + projectid;


   try {
      URI uri = null;
      if (oauth_token != null && use_github_api) {
	 if (lang != null) q += " language:" + langstr;
	 if (page > 0) q+= "&page=" + (page+1);
	 // q += "&per_page=100";
	 // q += "&access_token=" + oauth_token.getToken();
	 uri = new URI(GITHUB_SCHEME,"api.github.com","/search/code",q,null);
       }
      else {
	 if (page > 0) q += "&p=" + (page+1);
	 if (lang != null) q += "&l=" + langstr;
	 q += GITHUB_QUERY_TAIL;
	 uri = new URI(GITHUB_SCHEME,GITHUB_AUTHORITY,GITHUB_PATH,q,null);
       }
      return uri;
    }
   catch (URISyntaxException e) { }

   return null;
}



@Override S6Source createSource(URI uri,String cnts,int idx)
{
   return new GithubSource(uri.toString(),cnts,idx);
}



/********************************************************************************/
/*										*/
/*	Search page scanning							*/
/*										*/
/********************************************************************************/

List<URI> getSearchPageResults(URI uri,String cnts)
{
   if (oauth_token == null || !use_github_api) return super.getSearchPageResults(uri,cnts);

   ArrayList<URI> rslt = new ArrayList<URI>();
   try {
      JSONArray jarr = new JSONArray(cnts);
      for (int i = 0; i < jarr.length(); ++i) {
	 JSONObject jobj = jarr.getJSONObject(i);
	 System.err.println("RESULT: " + jobj);
	 // work with jobj
       }
    }
   catch (JSONException e) {
      System.err.println("S6: Problem parsing github json return: " + e);
    }

   return rslt;
}


List<URI> getSearchPageResults(Element jsoup)
{
   List<URI> rslt = new ArrayList<URI>();
   Elements results = jsoup.select("div.code-list-item");
   for (Element result : results) {
      Elements uris = result.select("p.title a:eq(1)");
      Element tag = uris.get(0);
      String href = tag.attr("href");
      try {
	 href = href.replace("%2524","$");
	 URI uri = new URI(GITHUB_SCHEME,GITHUB_AUTHORITY,href,GITHUB_FRAGMENT);
	 rslt.add(uri);
	 Elements codes = result.select("td.blob-code");
	 StringBuffer buf = new StringBuffer();
	 for (Element codeelt : codes) {
	    buf.append(codeelt.text());
	    buf.append("\n");
	  }
       }
      catch (URISyntaxException e) { }
    }
   return rslt;
}



@Override boolean hasMoreSearchPages(URI uri,String cnts,int page)
{
   if (cnts == null) return false;
   return cnts.contains("class=\"next_page\"");
}




/********************************************************************************/
/*										*/
/*	Path access methods							*/
/*										*/
/********************************************************************************/

@Override URI getURIForPath(S6Source src,String path)
{
   if (!(src instanceof GithubSource)) return null;

   GithubSource gsrc = (GithubSource) src;
   String spath = src.getDisplayName();
   if (path == null || !path.startsWith("/")) {
      int idx1 = spath.indexOf("/blob/");
      if (idx1 > 0) {
	 int idx2 = spath.indexOf("/",idx1+7);
	 spath = spath.substring(idx2); 	// skip /blob/<key>/ :: path remoaint
       }
      int idx3 = spath.lastIndexOf("/");
      if (idx3 > 0) spath = spath.substring(0,idx3);	  // remove AndroidManifest.xml
      else spath = "";
      if (path == null) path = spath;
      else path = spath + "/" + path;
    }
   return gsrc.getPathURI(path);
}


@Override List<URI> getDirectoryContentsURIs(URI baseuri,S6Source src,Element jsoup)
{
   List<URI> rslt = new ArrayList<URI>();

   Elements results = jsoup.select("a.js-directory-link");
   if (results.size() == 0) {
      results = jsoup.select("table.files td.content a.js-navigation-open");
    }
   for (Element result : results) {
      String href = result.attr("href");
      href = href.replace("%2524","$");
      try {
	 URI u = new URI(GITHUB_SCHEME,GITHUB_AUTHORITY,href,null);
	 rslt.add(u);
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

@Override protected URI getRawFileURI(URI base)
{
   String url = base.getPath();
   String blob = url.replace("/blob","");
   blob = blob.replace("%2524","$");
   try {
      URI uri = new URI(GITHUB_SCHEME,GITHUB_FILE_AUTHORITY,blob,null,null);
      return uri;
    }
   catch (URISyntaxException e) { }

   return null;
}




@Override protected boolean shouldRetry(S6Exception e)
{
   if (e.getMessage().contains(": 429")) return true;
   return false;
}



/********************************************************************************/
/*										*/
/*	Github Source								*/
/*										*/
/********************************************************************************/

private static class GithubSource extends KeySearchSource implements S6Source {

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

   @Override public String getName()		{ return SOURCE_PREFIX + base_link; }
   @Override public String getDisplayName()	{ return base_path; }

   @Override public String getProjectId() {
      String base = base_link;
      int idx = base.indexOf("//");
      idx = base.indexOf("/",idx+2);
      int idx1 = base.indexOf("/blob/",idx);
      return base.substring(idx+1,idx1);
    }

   URI getPathURI(String path) {
      String pid = getProjectId();
      int idx = base_link.indexOf("/blob/");
      int idx1 = base_link.indexOf("/",idx+7);
      String hex = base_link.substring(idx+6,idx1);
      String qp = "/" + pid + "/tree/" + hex + path;
      try {
	 URI u = new URI(GITHUB_SCHEME,GITHUB_AUTHORITY,qp,null);
	 return u;
       }
      catch (URISyntaxException e) { }
      return null;
    }

   @Override public String getPathName() {
      String spath = base_path;
      int idx1 = spath.indexOf("/blob/");
      if (idx1 > 0) {
         int idx2 = spath.indexOf("/",idx1+7);
         spath = spath.substring(idx2); 	// skip /blob/<key>/ :: path remoaint
       }
      return spath;
    }

}	// end of subclass GithubSource


/********************************************************************************/
/*										*/
/*	GitHub authentication							*/
/*										*/
/********************************************************************************/

private synchronized static void getOAuthToken()
{
   if (oauth_token != null) return;

   if (github_auth == null) {
      String userpass = loadGithubUserInfo();
      if (userpass == null) return;
      github_auth = "Basic " + userpass;
    }

   Object o1 = doGithubAuthenticate(null,"GET",null);
   JSONArray j1 = (JSONArray) o1;
   // System.err.println("RESULT IS " + j1);
   try {
      for (int i = 0; i < j1.length(); ++i) {
	 JSONObject key = j1.getJSONObject(i);
	 OAuthData od = new OAuthData(key);
	 if (od.isValid()) {
	    oauth_token = od;
	    Runtime.getRuntime().addShutdownHook(new OAuthRemover(od));
	    return;
	  }
       }
    }
   catch (JSONException e) { }

   Map<String,Object> q1 = new HashMap<String,Object>();
   q1.put("scopes",new String [] { "public_repo" });
   q1.put("note","s6_access");
   q1.put("note_url","http://conifer.cs.brown.edu/s6");
   q1.put("client_id",S6_CLIENT_ID);
   q1.put("client_secret",S6_CLIENT_SECRET);
   q1.put("fingerprint",S6_FINGERPRINT);
   Object o2 = doGithubAuthenticate(null,"POST",q1);
   JSONObject j2 = (JSONObject) o2;
   OAuthData od = new OAuthData(j2);
   if (od.isValid()) {
      oauth_token = od;
      Runtime.getRuntime().addShutdownHook(new OAuthRemover(od));
      return;
    }
}


private static String loadGithubUserInfo()
{
   try {
      File path = IvyFile.expandFile("$(HOME)/.github");
      BufferedReader fr = new BufferedReader(new FileReader(path));
      for ( ; ; ) {
	 String ln = fr.readLine();
	 if (ln == null) break;
	 ln = ln.trim();
	 if (ln.length() == 0) continue;
	 if (ln.startsWith("#") || ln.startsWith("%")) continue;
	 fr.close();
	 return javax.xml.bind.DatatypeConverter.printBase64Binary(ln.getBytes());
       }
      fr.close();
    }
   catch (IOException e) { }
   return null;
}



private static Object doGithubAuthenticate(String path,String type,Map<String,Object> input)
{
   String ustr = "https://api.github.com";
   if (path != null) ustr += path;
   else ustr += "/authorizations";

   String inps = null;
   if (input != null) {
      JSONObject jobj = new JSONObject(input);
      inps = jobj.toString();
    }

   try {
      URL url1 = new URL(ustr);
      HttpURLConnection hc1 = (HttpURLConnection) url1.openConnection();
      hc1.setDoInput(true);
      hc1.setRequestProperty("Authorization",github_auth);
      hc1.setRequestProperty("User-Agent","s6");
      if (type != null) hc1.setRequestMethod(type);

      if (inps != null) {
	 hc1.setDoOutput(true);
	 OutputStream ots = hc1.getOutputStream();
	 ots.write(inps.getBytes());
	 ots.close();
       }
      else {
	 hc1.setDoOutput(false);
       }

      InputStream ins = hc1.getInputStream();
      BufferedReader r = new BufferedReader(new InputStreamReader(ins));
      StringBuffer buf = new StringBuffer();
      for ( ; ; ) {
	 String ln = r.readLine();
	 if (ln == null) break;
	 buf.append(ln);
	 buf.append("\n");
       }
      hc1.disconnect();

      String cnts = buf.toString().trim();
      if (cnts.startsWith("[")) return new JSONArray(cnts);
      else if (cnts.startsWith("{")) return new JSONObject(cnts);
      else if (cnts.equals("")) ;
      else {
	 System.err.println("S6: bad json contents: " + cnts);
       }
    }
   catch (IOException e) {
      System.err.println("I/O Error accessing github: " + e);
    }
   catch (JSONException e) {
      System.err.println("JSON Error accessing github: " + e);
    }

   return null;
}








@SuppressWarnings("unused")
private static class OAuthData {

   private String oauth_id;
   private boolean is_s6token;
   private String token_id;
   private String hash_token;
   private String token_last;
   private String token_url;

   OAuthData(JSONObject obj) {
      oauth_id = obj.optString("id");
      token_id = obj.optString("token");
      hash_token = obj.optString("hashed_token");
      token_last = obj.optString("token_last_eight");
      token_url = obj.optString("url");
      is_s6token = false;
      JSONObject app = obj.optJSONObject("app");
      if (app == null) return;
      String appnm = app.optString("name");
      if (appnm == null || !appnm.equals("S6")) return;
      if (token_id == null || token_id.equals("")) return;
      is_s6token = true;
    }

   String getToken()			{ return token_id; }
   boolean isValid()			{ return is_s6token; }

}	// end of inner class OAuthData


private static class OAuthRemover extends Thread
{
   private OAuthData auth_data;

   OAuthRemover(OAuthData ad) {
      auth_data = ad;
    }

   @Override public void run() {
      String auth = S6_CLIENT_ID + ":" +S6_CLIENT_SECRET;
      auth = javax.xml.bind.DatatypeConverter.printBase64Binary(auth.getBytes());
      github_auth = "Basic " + auth;
      doGithubAuthenticate("/applications/" + S6_CLIENT_ID + "/tokens/" + auth_data.getToken(),
	    "DELETE",null);
    }

}	// end of inner class OAuthRemover




}	// end of class KeySearchRepoGithub




/* end of KeySearchRepoGithub.java */

