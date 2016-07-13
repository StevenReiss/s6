/********************************************************************************/
/*										*/
/*		JavaContext.java						*/
/*										*/
/*	Class to handle represent a Java user context				*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/language/java/JavaContext.java,v 1.8 2015/09/23 17:54:52 spr Exp $ */


/*********************************************************************************
 *
 * $Log: JavaContext.java,v $
 * Revision 1.8  2015/09/23 17:54:52  spr
 * Version to handle andriod UI applications.
 *
 * Revision 1.7  2015/02/14 19:40:18  spr
 * Add test case generation.
 *
 * Revision 1.6  2014/08/29 15:16:08  spr
 * Updates for suise, testcases.
 *
 * Revision 1.5  2013/09/13 20:33:03  spr
 * Add calls for UI search.
 *
 * Revision 1.4  2012-06-11 14:07:49  spr
 * add framework search; fix bugs
 *
 * Revision 1.3  2009-09-18 01:41:35  spr
 * Handle user testing.
 *
 * Revision 1.2  2009-05-12 22:28:48  spr
 * Fix ups to make user context work.
 *
 * Revision 1.1  2008-11-12 13:53:07  spr
 * Start of context management.
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.language.java;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.w3c.dom.Element;

import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.language.ContextBase;


class JavaContext extends ContextBase implements S6Constants, JavaConstants {



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private String			context_package;
private String			context_class;
private List<String>		context_imports;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

JavaContext(String file)
{
   super(file);

   context_package = null;
   context_class = null;
   context_imports = new ArrayList<String>();
}




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

boolean isUserContext() 			{ return true; }

String getContextPackage()			
{ 
   checkOpen();
   return context_package;
}
String getContextClass() 
{ 
   checkOpen();
   return context_class; 
}
Collection<String> getContextImports()		{ return context_imports; }



/********************************************************************************/
/*										*/
/*	Setup methods								*/
/*										*/
/********************************************************************************/

@Override protected void loadLanguageContext(Element e)
{
   context_package = IvyXml.getTextElement(e,"PACKAGE");
   context_class = IvyXml.getTextElement(e,"CLASS");

   Element imps = IvyXml.getChild(e,"IMPORTS");
   if (imps != null) {
      for (Element ie : IvyXml.children(imps,"IMPORT")) {
	 String s = IvyXml.getText(ie);
	 context_imports.add(s);
       }
    }
}




}




/* end of JavaContext.java */




