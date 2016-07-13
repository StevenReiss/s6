/********************************************************************************/
/*										*/
/*		ContextUserFile.java						*/
/*										*/
/*	Holder for user file information					*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/context/ContextUserFile.java,v 1.4 2015/09/23 17:57:52 spr Exp $ */


/*********************************************************************************
 *
 * $Log: ContextUserFile.java,v $
 * Revision 1.4  2015/09/23 17:57:52  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.3  2015/02/19 03:14:42  spr
 * Ensure current
 *
 * Revision 1.2  2013-05-09 12:26:15  spr
 * Minor changes to start ui fixups.
 *
 * Revision 1.1  2012-06-11 14:07:28  spr
 * Code cleanup
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.context;


import java.io.File;

import edu.brown.cs.ivy.xml.IvyXmlWriter;



class ContextUserFile implements ContextConstants
{



/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private File user_file;
private String access_name;
private String context_name;
private S6UserFileType file_mode;

private static int file_counter = 0;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

ContextUserFile(File f,String unm,S6UserFileType ft)
{
   this();
   set(f,unm,ft);
}




ContextUserFile()
{
   user_file = null;
   access_name = null;
   file_mode = S6UserFileType.READ;

   ++file_counter;
   context_name = CONTEXT_USER_JAR_NAME + file_counter;
}



/********************************************************************************/
/*										*/
/*	Setup methods								*/
/*										*/
/********************************************************************************/

void set(File local,String name,S6UserFileType ft)
{
   user_file = local;

   if (user_file != null && name == null || name.length() == 0) name = user_file.getName();

   if (name.startsWith(S6_DIRECTORY)) {
      name = name.substring(S6_DIRECTORY.length());
    }
   else if (name.startsWith(S6_WINDOWS)) {
      name = name.substring(S6_WINDOWS.length());
    }

   access_name = name;

   file_mode = ft;
}




/********************************************************************************/
/*										*/
/*	Access methoeds 							*/
/*										*/
/********************************************************************************/

File getFile()			     { return user_file; }
String getFileName()
{
   if (user_file == null) return null;
   return user_file.getPath();
}


String getAccessName()			{ return access_name; }

String getJarName()			{ return context_name; }

S6UserFileType getFileMode()		{ return file_mode; }


boolean isValid()
{
   return user_file != null && access_name != null;
}



/********************************************************************************/
/*										*/
/*	Output methods								*/
/*										*/
/********************************************************************************/

void addEntry(IvyXmlWriter xw)
{
   xw.begin("USERFILE");
   xw.field("NAME",access_name);
   xw.field("JARNAME",context_name);
   xw.field("ACCESS",file_mode);
   xw.end("USERFILE");
}


public String toString()
{
   return access_name + " <= " + user_file.getPath() + " (" + file_mode + ")";
}



}	// end of class ContextUserFile




/* end of ContextUserFile.java */
