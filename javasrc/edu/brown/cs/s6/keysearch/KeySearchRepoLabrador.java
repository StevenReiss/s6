/********************************************************************************/
/*                                                                              */
/*              KeySearchRepoLabrador.java                                      */
/*                                                                              */
/*      Interface for labrador-based local search                               */
/*                                                                              */
/********************************************************************************/
/*      Copyright 2013 Brown University -- Steven P. Reiss                    */
/*********************************************************************************
 *  Copyright 2013, Brown University, Providence, RI.                            *
 *                                                                               *
 *                        All Rights Reserved                                    *
 *                                                                               *
 *  Permission to use, copy, modify, and distribute this software and its        *
 *  documentation for any purpose other than its incorporation into a            *
 *  commercial product is hereby granted without fee, provided that the          *
 *  above copyright notice appear in all copies and that both that               *
 *  copyright notice and this permission notice appear in supporting             *
 *  documentation, and that the name of Brown University not be used in          *
 *  advertising or publicity pertaining to distribution of the software          *
 *  without specific, written prior permission.                                  *
 *                                                                               *
 *  BROWN UNIVERSITY DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS                *
 *  SOFTWARE, INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND            *
 *  FITNESS FOR ANY PARTICULAR PURPOSE.  IN NO EVENT SHALL BROWN UNIVERSITY      *
 *  BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY          *
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,              *
 *  WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS               *
 *  ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE          *
 *  OF THIS SOFTWARE.                                                            *
 *                                                                               *
 ********************************************************************************/



package edu.brown.cs.s6.keysearch;

import edu.brown.cs.s6.common.*;
import edu.brown.cs.cose.cosecommon.CoseSource;
import edu.brown.cs.ivy.exec.IvyExec;
import edu.brown.cs.ivy.file.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.util.*;


class KeySearchRepoLabrador extends KeySearchRepo
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private final static String    SOURCE_PREFIX = "LOCAL:";

private final static String    LABRADOR_CMD = IvyFile.expandName("$(S6)/bin/labradorquery.sh");
private final static String    LABRADOR_JAVA = "ext:java";
private final static String     LABRADOR_XML = "ext:xml";

private final static int	MAX_FILES = 150;


/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

KeySearchRepoLabrador(S6Request.Search sr)
{
   super(sr,100);
}



/********************************************************************************/
/*                                                                              */
/*      Abstract Method Implementations                                         */
/*                                                                              */
/********************************************************************************/

@Override URI getURIForSearch(List<String> keywords,S6SearchLanguage lang,String projectid,int page)
{
   return null;
}


@Override String getResultPage(List<String> keywords,S6SearchLanguage lang,String projectid,int page)
{
   StringBuffer cmd = new StringBuffer();
   
   cmd.append(LABRADOR_CMD);
   cmd.append(" ");
   cmd.append("\"");
   
   int ct = 0;
   for (String s : keywords) {
      if (ct++ > 0) cmd.append(" AND ");
      cmd.append(s);
    }
   switch (lang) {
      case JAVA :
         cmd.append(" AND ");
         cmd.append(LABRADOR_JAVA);
         break;
      case XML :
         cmd.append(" AND ");
         cmd.append(LABRADOR_XML);
         break;
      default :
         break;
    }
   cmd.append("\"");
   System.err.println("S6: KEYSEARCH: " + cmd.toString());
   
   StringBuffer rslt = new StringBuffer();
   try {
      IvyExec ex = new IvyExec(cmd.toString(),IvyExec.READ_OUTPUT);
      InputStream ins = ex.getInputStream();
      BufferedReader br = new BufferedReader(new InputStreamReader(ins));
      for (int i = 0; i < MAX_FILES; ++i) {
         String s = br.readLine();
         if (s == null) break;
         System.err.println("S6: KEYSEARCH: READ: " + s);
         rslt.append(s);
         rslt.append("\n");
       }
      br.close();
    }
   catch (IOException e) {
      System.err.println("S6: KEYSEARCH: Problem running labrador query: " + e);
    }   
   
   return rslt.toString();
}



List<URI> getSearchPageResults(URI uri,String cnts)
{
   List<URI> rslt = new ArrayList<URI>();
   StringTokenizer tok = new StringTokenizer(cnts,"\n");
   while (tok.hasMoreTokens()) {
      String file = tok.nextToken();
      URI u = null;
      try {
         u = new URI(file);
         rslt.add(u);
       }
      catch (URISyntaxException e) { 
         System.err.println("S6: Problem converting file to URI: " + file);
       }
    }
   
   return rslt;
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



@Override URI getURIForPath(CoseSource src,String path)
{
   if (!(src instanceof LabradorSource)) return null;
   try {
      return new URI("file://" + path);
    }
   catch (URISyntaxException e) {
      System.err.println("S6: Problem creating labrador path: " + src + " + " + path);
    }

   return null;
}



@Override CoseSource createSource(URI uri,String cnts,int idx)
{
   return new LabradorSource(uri.toString(),cnts,idx);
}



@Override boolean hasMoreSearchPages(URI uri,String cnts,int page)      { return false; }
@Override int getResultsPerPage()                                       { return MAX_FILES; }
@Override protected URI getRawFileURI(URI uri)                          { return uri; }




/********************************************************************************/
/*										*/
/*	Class to hold a labrador-related source 				*/
/*										*/
/********************************************************************************/

private static class LabradorSource extends KeySearchSource implements CoseSource {
   
   private String file_name;
   
   LabradorSource(String file,String code,int idx) {
      super(code,idx);
      file_name = file;
    }
   
   @Override public String getName()	        { return SOURCE_PREFIX + file_name; }
   @Override public String getDisplayName()	{ return file_name; }
   
}	// end of subclass LabradorSource



}       // end of class KeySearchRepoLabrador




/* end of KeySearchRepoLabrador.java */

