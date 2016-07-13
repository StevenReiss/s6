/********************************************************************************/
/*										*/
/*		RequestField.java						*/
/*										*/
/*	Information about a requested field					*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/request/RequestField.java,v 1.2 2015/09/23 17:58:07 spr Exp $ */


/*********************************************************************************
 *
 * $Log: RequestField.java,v $
 * Revision 1.2  2015/09/23 17:58:07  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.1  2012-06-11 14:08:10  spr
 * Add framework search; fix bugs
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.request;


import org.eclipse.jdt.core.dom.Modifier;
import org.w3c.dom.Element;

import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Exception;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6Request.ClassSignature;




class RequestField extends RequestSignature implements S6Request.FieldSignature,
		S6Constants, RequestConstants {


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private String	field_name;
private String	field_type;
private int	modifier_flags;
private RequestClass in_class;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

RequestField(RequestClass csg,Element xml) throws S6Exception
{
   in_class = csg;
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
   field_name = IvyXml.getTextElement(xml,"NAME");
   field_type = IvyXml.getTextElement(xml,"TYPE");
}




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

public String getName() 	        	{ return field_name; }

public String getTypeName()		        { return field_type; }

public ClassSignature getDefiningClass()        { return in_class; }

public boolean isStatic()		{ return Modifier.isStatic(modifier_flags); }

public S6Request.MethodSignature getMethod(String nm)	{ return null; }



/********************************************************************************/
/*										*/
/*	Debug methods								*/
/*										*/
/********************************************************************************/

public String toString()
{
   StringBuilder buf = new StringBuilder();

   if (isStatic()) buf.append("static ");
   buf.append(field_type);
   if (field_name != null) {
      buf.append(" ");
      buf.append(field_name);
    }
   else buf.append(" *");

   return buf.toString();
}




}	// end of class RequestField




/* end of RequestField.java */
