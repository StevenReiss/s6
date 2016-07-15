/********************************************************************************/
/*										*/
/*		KeySearchRepo.java						*/
/*										*/
/*	Repository-specific information for search				*/
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

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;

import edu.brown.cs.s6.common.S6Exception;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6Source;



abstract class KeySearchRepo implements KeySearchConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private static Semaphore request_sema = null;
private long	  retry_delay;

private static final int MAX_RETRY = 5;

private static boolean do_debug;
protected static KeySearchCache url_cache = new KeySearchCache();



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

protected KeySearchRepo(S6Request.Search sr,int max)
{
   do_debug = sr.doDebug();

   retry_delay = 20000;
   if (max > 0 && request_sema == null) {
      request_sema = new Semaphore(max);
    }
}


/********************************************************************************/
/*										*/
/*	Abstract methods							*/
/*										*/
/********************************************************************************/

abstract URI getURIFromSourceString(String source);

int getResultsPerPage() 			{ return 10; }

String getAuthorization()			{ return null; }

abstract URI getURIForSearch(List<String> keywords,S6SearchLanguage lang,String projectid,int page);

abstract S6Source createSource(URI uri,String text,int idx);




/********************************************************************************/
/*										*/
/*	Search page scanning methods						*/
/*										*/
/********************************************************************************/

List<URI> getSearchPageResults(URI uri,String cnts)
{
   if (cnts == null) {
      return new ArrayList<URI>();
    }
   Element jsoup = Jsoup.parse(cnts,uri.toString());

   return getSearchPageResults(jsoup);
}


List<URI> getSearchPageResults(Element jsoup)		{ return null; }



boolean hasMoreSearchPages(URI uri,String cnts,int page)
{
   if (cnts == null) return false;

   Element jsoup = Jsoup.parse(cnts,uri.toString());

   return hasMoreSearchPages(jsoup,page);
}


boolean hasMoreSearchPages(Element jsoup,int page)	{ return true; }



/********************************************************************************/
/*										*/
/*	Page retrieval methods							*/
/*										*/
/********************************************************************************/

final String getSourcePage(URI uri)
{
   URI rawuri = getRawFileURI(uri);

   if (rawuri != null) {
      return getResultPage(rawuri);
    }

   String cnts = getRawSourcePage(uri);
   if (cnts != null) return cnts;

   cnts = getResultPage(uri);
   if (cnts == null) return null;

   cnts = getRawFileContents(uri,cnts);

   return cnts;
}


protected String getRawSourcePage(URI uri)	{ return null; }



final byte [] getBinaryPage(URI uri)
{
   URI rawuri = getRawFileURI(uri);

   if (rawuri != null) {
      return getResultBinary(rawuri);
    }

   String cnts = getResultPage(uri);
   if (cnts == null) return null;

   cnts = getRawFileContents(uri,cnts);

   return cnts.getBytes();
}


// the actual repo should overwrite one or more of these

protected URI getRawFileURI(URI u)		{ return null; }

protected String getRawFileContents(URI uri,String cnts)
{
   if (cnts == null)
      return null;

   Element jsoup = Jsoup.parse(cnts,uri.toString());

   return getRawFileContents(uri,jsoup);
}


String getRawFileContents(URI uri,Element jsoup) { return null; }



/********************************************************************************/
/*										*/
/*	Path access methods							*/
/*										*/
/********************************************************************************/

abstract URI getURIForPath(S6Source source,String path);

List<URI> getDirectoryContentsURIs(URI baseuri,S6Source src)
{
   return null;
}



List<URI> getDirectoryContentsURIs(URI baseuri,S6Source src,String cnts)
{
   if (cnts == null) return new ArrayList<URI>();

   Element jsoup = Jsoup.parse(cnts,baseuri.toString());

   return getDirectoryContentsURIs(baseuri,src,jsoup);
}


List<URI> getDirectoryContentsURIs(URI baseuri,S6Source src,Element jsoup)
{
   return new ArrayList<URI>();
}



/********************************************************************************/
/*                                                                              */
/*      Class search methods                                                    */
/*                                                                              */
/********************************************************************************/

boolean getClassesInPackage(String pkg,String project,int page,List<URI> rslt)
{
   System.err.println("SEARCH FOR PACKAGE " + pkg);
   List<String> keys = new ArrayList<String>();
   keys.add("package " + pkg);
   URI uri = getURIForSearch(keys,S6SearchLanguage.JAVA,project,page);
   String rslts = getResultPage(uri);
   if (rslts == null) return false;
   List<URI> uris = getSearchPageResults(uri,rslts);
   if (uris != null) rslt.addAll(uris);
   
   return hasMoreSearchPages(uri,rslts,page);
}




KeySearchClassData getPackageClassResult(S6Source base,String pkg,String cls,int page)
{
   List<String> keys = new ArrayList<String>();
   keys.add("package " + pkg);
   keys.add("class " + cls);
   URI uri = getURIForSearch(keys,S6SearchLanguage.JAVA,base.getProjectId(),page);
   String rslts = getResultPage(uri);
   List<URI> uris =  getSearchPageResults(uri,rslts);
   String basepath = base.getPathName();
   int idx = basepath.lastIndexOf("/");
   if (idx >= 0) basepath= basepath.substring(0,idx+1);
   else basepath = "";
   
   URI besturi = null;
   String bestpath = null;
   S6Source bestsrc = null;
   String bestcode = null;
   
   for (URI u : uris) {
      String code = getSourcePage(u);
      S6Source nsrc = createSource(u,code,0);
      String npkg = KeySearchMaster.findPackageName(code);
      if (npkg == null || !npkg.equals(pkg)) continue;
      String ncls = KeySearchMaster.findClassName(code);
      if (ncls == null || !ncls.equals(cls)) continue;
      String path = nsrc.getPathName();
      if (useClass(basepath,path,bestpath)) {
         besturi = u;
         bestsrc = nsrc;
         bestcode = code;
         bestpath = path;
       }
    }
   
   if (besturi == null) {
      if (!hasMoreSearchPages(uri,rslts,page)) return null;
    }
   
   return new KeySearchClassData(besturi,bestpath,bestsrc,bestcode);
}


private boolean useClass(String basepath,String newpath,String oldpath)
{
   if (oldpath == null) return true;
   if (newpath.startsWith(basepath) && !oldpath.startsWith(basepath)) return true;
   if (newpath.length() < oldpath.length()) return true;
   
   return false;
}



/********************************************************************************/
/*										*/
/*	Page loading methods							*/
/*										*/
/********************************************************************************/

protected boolean shouldRetry(S6Exception e)		{ return false; }

protected boolean shouldRetry(ByteArrayOutputStream cnts,long delta)	{ return false; }


final String getResultPage(URI uri)
{
   ByteArrayOutputStream baos = getResultBytes(uri);
   return getString(baos);
}


String getResultPage(List<String> keywords,S6SearchLanguage lang,String projectid,int page)
{
   return null;
}



final byte [] getResultBinary(URI uri)
{
   ByteArrayOutputStream baos = getResultBytes(uri);
   return baos.toByteArray();
}




final ByteArrayOutputStream getResultBytes(URI uri)
{
   ByteArrayOutputStream page = null;

   if (request_sema != null) {
      request_sema.acquireUninterruptibly();
    }
   try {
      boolean retry = false;
      for (int i = 0; i < MAX_RETRY; ++i) {
	 if (i > 0) System.err.println("S6: Retry " + i + " " + uri);
	 try {
	    long start = System.currentTimeMillis();
	    page = loadURIBinary(uri,getAuthorization(),true,retry);
	    long delta = System.currentTimeMillis() - start;
	    if (!shouldRetry(page,delta)) break;
	  }
	 catch (S6Exception e) {
	    if (i == MAX_RETRY-1) return null;
	    if (!shouldRetry(e)) {
	       if (i == 0) System.err.println("S6: Exception on load : " + e);
	       return null;
	     }
	  }
	 long delay = retry_delay;
	 if (i > 1) delay *= i;
	 try {
	    Thread.sleep(delay);
	  }
	 catch (InterruptedException e) { }
       }
    }
   finally {
      if (request_sema != null) request_sema.release();
    }

   return page;
}



static String getString(ByteArrayOutputStream baos)
{
   if (baos == null) return null;

   try {
      String rslt = baos.toString("UTF-8");
      if (!rslt.endsWith("\n"))
	 rslt += "\n";
      return rslt;
    }
   catch (UnsupportedEncodingException e) {
      return baos.toString();
    }
}



protected ByteArrayOutputStream loadURIBinary(URI uri,String auth,boolean cache,boolean reread)
	throws S6Exception
{
   if (do_debug) System.err.println("S6: KEYSEARCH: LOAD: " + uri + " " + cache);

   ByteArrayOutputStream baos = new ByteArrayOutputStream();

   try {
      URL url = uri.toURL();
      InputStream br = url_cache.getInputStream(url,auth,cache,reread);
      byte [] buf = new byte[8192];
      for ( ; ; ) {
	 int ln = br.read(buf);
	 if (ln <= 0) break;
	 baos.write(buf,0,ln);
       }
      br.close();
    }
   catch (IOException e) {
      throw new S6Exception("Problem accessing url " + uri + ": " + e,e);
    }

   return baos;
}



}	// end of class KeySearchRepo




/* end of KeySearchRepo.java */

