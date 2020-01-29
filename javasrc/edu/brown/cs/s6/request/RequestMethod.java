/********************************************************************************/
/*										*/
/*		RequestMethod.java						*/
/*										*/
/*	Information about a requested method					*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/request/RequestMethod.java,v 1.9 2016/07/18 23:05:37 spr Exp $ */


/*********************************************************************************
 *
 * $Log: RequestMethod.java,v $
 * Revision 1.9  2016/07/18 23:05:37  spr
 * Update request to contain more info for applications.
 *
 * Revision 1.8  2015/09/23 17:58:07  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.7  2015/02/14 19:40:20  spr
 * Add test case generation.
 *
 * Revision 1.6  2014/08/29 15:16:13  spr
 * Updates for suise, testcases.
 *
 * Revision 1.5  2012-06-11 14:08:10  spr
 * Add framework search; fix bugs
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


import java.util.List;

import org.eclipse.jdt.core.dom.Modifier;
import org.w3c.dom.Element;

import edu.brown.cs.cose.cosecommon.CoseSignature;
import edu.brown.cs.ivy.file.IvyFormat;
import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Exception;
import edu.brown.cs.s6.common.S6Request;



class RequestMethod extends RequestSignature implements S6Request.MethodSignature,
		S6Constants, RequestConstants, CoseSignature.CoseMethodSignature { 


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private String	method_name;
private String	return_type;
private List<String> arg_types;
private List<String> exception_types;
private List<String> arg_names;
private int	modifier_flags;
private String	method_text;
private RequestClass for_class;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

RequestMethod(RequestClass cls,Element xml) throws S6Exception
{
   for_class = cls;
   
   modifier_flags = IvyXml.getAttrInt(xml,"MODS",0);
   if (modifier_flags == 0) {
      for (int i = 1; i != 0; i = i*2) {
	 Modifier.ModifierKeyword mmk = Modifier.ModifierKeyword.fromFlagValue(i);
	 if (mmk != null) {
	    String s = mmk.toString().toUpperCase();
	    if (IvyXml.getAttrBool(xml,s)) modifier_flags |= i;
	  }
       }
    }

   method_name = IvyXml.getTextElement(xml,"NAME");

   return_type = IvyXml.getTextElement(xml,"RETURN");

   String args = IvyXml.getTextElement(xml,"ARGS");
   arg_types = RequestBase.getTypesFromString(args);

   String argnames = IvyXml.getTextElement(xml,"ARGNAMES");
   arg_names = RequestBase.getTypesFromString(argnames);

   if (args == null && return_type == null) {
      String sgn = IvyXml.getTextElement(xml,"SIGNATURE");
      if (sgn != null) {
	 int idx = sgn.lastIndexOf(")");
	 String ret = sgn.substring(idx+1);
	 if (idx > 0 && ret != null && ret.length() > 0) {
	    return_type = IvyFormat.formatTypeName(ret);
	  }
	 String sarg = sgn.substring(0,idx+1);
	 args = IvyFormat.formatTypeName(sarg);
	 arg_types = RequestBase.getTypesFromString(args);
       }
    }
   String excs = IvyXml.getTextElement(xml,"EXCEPTIONS");
   exception_types = RequestBase.getTypesFromString(excs);

   if (arg_names.size() != arg_types.size()) {
      String anms = "";
      for (int i = 0; i < arg_types.size(); ++i) {
	 if (i > 0) anms += ",";
	 anms += "a" + i;
       }
      arg_names = RequestBase.getTypesFromString(anms);
    }

   method_text = IvyXml.getTextElement(xml,"TEXT");
}




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

public String getName() 		                { return method_name; }

public String getReturnTypeName()	                { return return_type; }

public List<String> getParameterTypeNames()	        { return arg_types; }

public List<String> getExceptionTypeNames()	        { return exception_types; }

public List<String> getParameterNames() 	        { return arg_names; }

public S6Request.ClassSignature getDefiningClass()      { return for_class; }

public boolean isStatic()		{ return Modifier.isStatic(modifier_flags); }
public boolean isAbstract()		{ return Modifier.isAbstract(modifier_flags); }
public boolean useExceptions()		{ return true; }

public String getDeclarationText()	{ return method_text; }


public S6Request.MethodSignature getMethod(String name)
{
   if (name == null) return null;
   if (name.equals(method_name)) return this;
   return null;
}

@Override public S6Request.MethodSignature getMethodSignature()
{
   return this;
}





/********************************************************************************/
/*										*/
/*	Debug methods								*/
/*										*/
/********************************************************************************/

public String toString()
{
   if (method_text != null) return method_text;

   StringBuilder buf = new StringBuilder();
   buf.append("{");
   if (isStatic()) buf.append("static ");
   if (isAbstract()) buf.append("abstract ");
   buf.append(return_type);
   buf.append(" ");
   buf.append(method_name);
   buf.append("(");
   for (int i = 0; i < arg_types.size(); ++i) {
      if (i != 0) buf.append(",");
      buf.append(arg_types.get(i));
      if (arg_names != null && arg_names.size() > i) {
	 buf.append(" ");
	 buf.append(arg_names.get(i));
       }
    }
   buf.append(")");
   if (exception_types != null && exception_types.size() > 0) {
      buf.append("throws ");
      for (int i = 0; i < exception_types.size(); ++i) {
	 if (i != 0) buf.append(",");
	 buf.append(exception_types.get(i));
       }
    }

   return buf.toString();
}



}	// end of class RequestMethod




/* end of RequestMethod.java */










