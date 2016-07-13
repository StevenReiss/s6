/********************************************************************************/
/*										*/
/*		RequestClass.java						*/
/*										*/
/*	Information about a requested class					*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/request/RequestClass.java,v 1.9 2015/09/23 17:58:07 spr Exp $ */


/*********************************************************************************
 *
 * $Log: RequestClass.java,v $
 * Revision 1.9  2015/09/23 17:58:07  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.8  2014/08/29 15:16:13  spr
 * Updates for suise, testcases.
 *
 * Revision 1.7  2013/09/13 20:33:08  spr
 * Add calls for UI search.
 *
 * Revision 1.6  2012-06-11 14:08:10  spr
 * Add framework search; fix bugs
 *
 * Revision 1.5  2009-09-18 01:41:54  spr
 * Handle full class option.
 *
 * Revision 1.4  2008-08-28 00:32:58  spr
 * Next version of S6.	Lots of bug fixes, some new functionality.
 *
 * Revision 1.3  2008-07-17 13:47:05  spr
 * Add contracts, augment request information in general.
 *
 * Revision 1.2  2008-06-12 17:47:54  spr
 * Next version of S6.
 *
 * Revision 1.1.1.1  2008-06-03 12:59:22  spr
 * Initial version of S6
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.request;


import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.w3c.dom.Element;

import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Exception;
import edu.brown.cs.s6.common.S6Request;



class RequestClass extends RequestSignature implements S6Request.ClassSignature,
		S6Constants, RequestConstants {


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private boolean is_interface;
private boolean is_main;
private String	class_name;
private List<S6Request.MethodSignature> known_methods;
private List<S6Request.FieldSignature> known_fields;
private String superclass_name;
private List<String> interface_names;
private List<String> key_words;
private int    class_modifiers;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

RequestClass(Element xml) throws S6Exception
{
   is_interface = IvyXml.isElement(xml,"INTERFACE");
   is_main = IvyXml.getAttrBool(xml,"MAIN");

   class_modifiers = IvyXml.getAttrInt(xml,"MODS");

   class_name = IvyXml.getTextElement(xml,"NAME");

   // TODO: allow alternatives for superclass and implements
   superclass_name = IvyXml.getTextElement(xml,"SUPERCLASS");
   interface_names = new ArrayList<String>();
   for (Element ie : IvyXml.children(xml,"IMPLEMENTS")) {
      addInterfaces(IvyXml.getText(ie));
    }
   String iag = IvyXml.getAttrString(xml,"IMPLEMENTS");
   addInterfaces(iag);

   known_methods = new ArrayList<S6Request.MethodSignature>();
   for (Element me : IvyXml.children(xml,"METHOD")) {
      known_methods.add(new RequestMethod(me));
    }

   known_fields = new ArrayList<S6Request.FieldSignature>();
   for (Element fe : IvyXml.children(xml,"FIELD")) {
      known_fields.add(new RequestField(fe));
    }

   key_words = new ArrayList<String>();
   for (Element ke : IvyXml.children(xml,"KEY")) {
      key_words.add(IvyXml.getText(ke));
    }
}



private void addInterfaces(String s)
{
   if (s == null) return;

   StringTokenizer tok = new StringTokenizer(s,", ");
   while (tok.hasMoreTokens()) {
      String v = tok.nextToken();
      interface_names.add(v);
    }
}




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

public String getName() 				{ return class_name; }

public List<S6Request.MethodSignature> getMethods()	{ return known_methods; }

public List<S6Request.FieldSignature> getFields()	{ return known_fields; }

public S6Request.MethodSignature getMethod(String name)
{
   for (S6Request.MethodSignature ms : known_methods) {
      if (ms.getName().equals(name)) return ms;
    }

   return null;
}

public boolean isInterface()				{ return is_interface; }
public boolean isMain() 				{ return is_main; }

public String getSuperClass()				{ return superclass_name; }

public List<String> getInterfaces()			{ return interface_names; }

public int getModifiers()				{ return class_modifiers; }

public List<String> getKeywords()			{ return key_words; }

public boolean includeTestCases()			{ return false; }

public S6Request.ClassSignature getClassSignature()	{ return this; }



}	// end of class RequestClass




/* end of RequestClass.java */
