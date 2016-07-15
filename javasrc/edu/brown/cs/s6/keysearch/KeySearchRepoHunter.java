/********************************************************************************/
/*										*/
/*		KeySearchRepoHunter.java					*/
/*										*/
/*	Interface to Hunter repository						*/
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

import edu.brown.cs.s6.common.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


class KeySearchRepoHunter extends KeySearchRepo
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private S6Request.MethodSignature for_method;

private final static String	HUNTER_SCHEME = "http";
private final static String	HUNTER_AUTHORITY = "utopia2.csres.utexas.edu:9099";
private final static String	HUNTER_PATH = "/HunterService/rest/hunter/web";
private final static String	HUNTER_KPATH = "/HunterService/rest/hunter/gname";
private final static String	HUNTER_FILEPATH = "/HunterService/rest/hunter/file";
private final static String	HUNTER_FRAGMENT = null;

private final static String	SOURCE_PREFIX = "HUNTER:";

private final static int	RESULTS_PER_PAGE = 100;
private final static int	SIMULTANEOUS_SEARCH = 20;





/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

KeySearchRepoHunter(S6Request.Search sr)
{
   super(sr,SIMULTANEOUS_SEARCH);

   S6Request.Signature sgn = sr.getSignature();
   S6Request.MethodSignature msgn = null;
   while (msgn == null && sgn != null) {
      if (sgn instanceof S6Request.MethodSignature) {
	 msgn = sgn.getMethodSignature();
	 sgn = null;
       }
      else if (sgn instanceof S6Request.ClassSignature) {
	 S6Request.ClassSignature csg = sgn.getClassSignature();
	 for (S6Request.MethodSignature ms : csg.getMethods()) {
	    sgn = ms;
	    break;
	  }
       }
      else break;
      /****************
      else if (sgn instanceof S6Request.PackageSignature) {
	 S6Request.PackageSignature psgn = (S6Request.PackageSignature) sgn;
	 for (S6Request.ClassSignature csgn : psgn.getClasses()) {
	    sgn = csgn;
	    break;
	  }
       }
      else if (sgn instanceof S6Request.TestingSignature) {
	 S6Request.TestingSignature tsgn = (S6Request.TestingSignature) sgn;
	 sgn = tsgn.getClassToTest();
       }
      **************/
    }

   for_method = msgn;
}




/********************************************************************************/
/*										*/
/*	Search property methods 						*/
/*										*/
/********************************************************************************/

@Override public int getResultsPerPage()	{ return RESULTS_PER_PAGE; }


@Override boolean hasMoreSearchPages(URI uri,String cnts,int page)
{
   return false;
}



/********************************************************************************/
/*										*/
/*	Abstract Method Implementations 					*/
/*										*/
/********************************************************************************/

@Override URI getURIForSearch(List<String> keywords,S6SearchLanguage lang,String projectid,int page)
{
   StringBuffer buf = new StringBuffer();
   String path = null;
   if (for_method == null) {
      path = HUNTER_KPATH;
      buf.append("name=");
    }
   else {
      path = HUNTER_PATH;
      buf.append("desc=");
    }
   int ct = 0;
   for (String s : keywords) {
      if (ct++ > 0) buf.append(" ");
      buf.append(s);
    }
   if (for_method != null) {
      StringBuffer sbuf = new StringBuffer();
      if (for_method.isStatic()) sbuf.append("static ");
      sbuf.append(for_method.getReturnTypeName());
      sbuf.append(" ");
      sbuf.append(for_method.getName());
      sbuf.append("(");
      ct = 0;
      for (String p : for_method.getParameterTypeNames()) {
	 if (ct++ > 0) sbuf.append(",");
	 sbuf.append(p);
       }
      sbuf.append(")");
      buf.append("&sign=" + sbuf.toString());
    }
   buf.append("&topk=150");

   try {
      return new URI(HUNTER_SCHEME,HUNTER_AUTHORITY,HUNTER_PATH,buf.toString(),HUNTER_FRAGMENT);
    }
   catch (URISyntaxException e) {
      System.err.println("S6: Problem with hunter URI: " + e);
    }

   return null;
}



@Override URI getURIFromSourceString(String src)
{
   if (!src.startsWith(SOURCE_PREFIX)) return null;

   try {
      return new URI(src.substring(SOURCE_PREFIX.length()));
    }
   catch (URISyntaxException e) { }

   return null;
}



@Override URI getURIForPath(S6Source src,String path)
{
   // method body goes here

   return null;
}



/********************************************************************************/
/*										*/
/*	Search processing							*/
/*										*/
/********************************************************************************/

@Override List<URI> getSearchPageResults(URI uri,String cnts)
{
   ArrayList<URI> rslt = new ArrayList<URI>();
   if (cnts.startsWith("hunter(")) {
      int idx0 = cnts.lastIndexOf(")");
      cnts = cnts.substring(7,idx0);
    }

   try {
      JSONObject top = new JSONObject(cnts);
      JSONArray jarr = top.getJSONArray("candidates");
      for (int i = 0; i < jarr.length(); ++i) {
	 JSONObject jobj = jarr.getJSONObject(i);
	 // System.err.println("RESULT: " + jobj);
	 String path = jobj.optString("pathId");
	 if (path == null || path.equals("")) {
	    System.err.println("No pathid for candidate: " + jobj);
	    continue;
	  }
	 String q = "id=" + path;
	 try {
	    URI u = new URI(HUNTER_SCHEME,HUNTER_AUTHORITY,HUNTER_FILEPATH,q,HUNTER_FRAGMENT);
	    rslt.add(u);
	  }
	 catch (URISyntaxException e) {
	    System.err.println("S6: Problem with hunter uri: " + e);
	  }
       }
    }
   catch (JSONException e) {
      System.err.println("S6: Problem parsing hunter json return: " + e);
      System.err.println("S6: Contents returned: " + cnts);
    }

   return rslt;
}



@Override List<URI> getDirectoryContentsURIs(URI baseuri,S6Source src,String cnts)
{
   return null;
}


@Override protected String getRawSourcePage(URI uri)
{
   ByteArrayOutputStream baos = getResultBytes(uri);
   if (baos == null) return null;
   byte [] data = baos.toByteArray();
   if (data.length > 4 && data[0] == 'P' && data[1] == 'K' && data[2] == 3 && data[4] == '4') {
      ByteArrayInputStream ins = new ByteArrayInputStream(data);
      JarInputStream jis = null;
      try {
	 jis = new JarInputStream(ins);
	 for ( ; ; ) {
	    JarEntry ent = jis.getNextJarEntry();
	    if (ent == null) break;
	    if (!ent.getName().endsWith(".java")) continue;
	    ByteArrayOutputStream rslt = new ByteArrayOutputStream();
	    byte [] rbuf = new byte[8192];
	    for ( ; ; ) {
	       int ln = jis.read(rbuf);
	       if (ln < 0) break;
	       rslt.write(rbuf,0,ln);
	     }
	    return getString(rslt);
	  }
       }
      catch (IOException e) {
	 System.err.println("S6: Problem reading result jar: " + e);
       }
      finally {
	 if (jis != null) {
	    try {
	       jis.close();
	     }
	    catch (IOException e) { }
	  }
       }
    }
   else {
      return getString(baos);
    }

   return null;
}




/********************************************************************************/
/*										*/
/*	Source for hunter results						*/
/*										*/
/********************************************************************************/

@Override S6Source createSource(URI uri,String cnts,int idx)
{
   // want to include the extra files in the source object
   return new HunterSource(uri.toString(),cnts,idx);
}




private static class HunterSource extends KeySearchSource implements S6Source {

   private String base_link;
   private String base_path;

   HunterSource(String base,String code,int idx) {
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
      int idx = base.indexOf("id=JavaCode/");
      idx = base.indexOf("/",idx+2);
      int idx1 = base.indexOf("/",idx+1);
      return base.substring(idx+1,idx1);
    }

   @Override public String getPathName() {
      return null;
    }

}	// end of subclass HunterSource




}	// end of class KeySearchRepoHunter




/* end of KeySearchRepoHunter.java */


