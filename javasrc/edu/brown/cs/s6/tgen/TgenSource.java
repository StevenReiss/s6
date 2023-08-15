/********************************************************************************/
/*                                                                              */
/*              TgenSource.java                                                 */
/*                                                                              */
/*      Representation of a source file from code search                        */
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



package edu.brown.cs.s6.tgen;

import java.net.URI;

import edu.brown.cs.ivy.jcomp.JcompSource;

class TgenSource implements TgenConstants, JcompSource
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private TgenOhloh search_access;

private String project_id;
private String project_name;
private URI file_href;
private String s6_source;
private String source_text;
private String file_name;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

TgenSource(TgenOhloh toh,String pname,String pid,String fnm,String src,URI href,
      TgenSource orig)
{
   search_access = toh;
   project_name = pname;
   project_id = pid;
   file_name = fnm;
   file_href = href;
   s6_source = src;
   source_text = null;
}





/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

String getProjectId()		        { return project_id; }
String getProjectName()	        { return project_name; }

String getS6Source()                    { return s6_source; }

@Override public String getFileName()   { return file_name; }

String getClassFileName()
{
   int idx = file_name.lastIndexOf("/");
   if (idx >= 0) return file_name.substring(idx+1);
   return file_name;
}


synchronized String getText() {
   if (source_text == null) {
      try {
         source_text = search_access.loadURL(file_href.toURL(),true);
       }
      catch (Exception e) {
         System.err.println("TGEN: Problem loading file " + file_href + ": " + e);
         source_text = "";
       }
    }
   
   if (source_text == null || source_text.length() == 0) return null;
   
   return source_text;
}



boolean isValid()                       { 
   return file_href != null && project_id != null;
}


/********************************************************************************/
/*                                                                              */
/*      JcompSource methods                                                     */
/*                                                                              */
/********************************************************************************/

@Override public String getFileContents()               { return getText(); }


/********************************************************************************/
/*                                                                              */
/*      Change package name                                                     */
/*                                                                              */
/********************************************************************************/

TgenSource getPackageSource(String opkg,String npkg)
{
   String cnts = getFileContents();
   cnts = cnts.replace(opkg,npkg);
   TgenSource nsrc = new TgenSource(search_access,project_name,project_id,
         file_name,s6_source,file_href,this);
   nsrc.source_text = cnts;
   return nsrc;
}




}       // end of class TgenSource




/* end of TgenSource.java */

