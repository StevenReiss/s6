/********************************************************************************/
/*										*/
/*		RequestSecurity.java						*/
/*										*/
/*	Basic implementation of a S6 search request security description	*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/request/RequestSecurity.java,v 1.3 2015/09/23 17:58:08 spr Exp $ */


/*********************************************************************************
 *
 * $Log: RequestSecurity.java,v $
 * Revision 1.3  2015/09/23 17:58:08  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.2  2008-11-12 13:52:20  spr
 * Performance and bug updates.
 *
 * Revision 1.1  2008-06-12 17:47:54  spr
 * Next version of S6.
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.request;


import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.s6.common.S6Security;



class RequestSecurity implements S6Security {



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private List<Description> permission_list;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

RequestSecurity(Element xml)
{
   permission_list = new ArrayList<Description>();
   if (xml != null) {
      for (Element e : IvyXml.elementsByTag(xml,"PERMISSION")) {
	 Description d = new Description(e);
	 permission_list.add(d);
       }
    }
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

public boolean isEmpty()
{
   return permission_list.isEmpty();
}




public List<S6Security.Permit> getPermissions()
{
   return new ArrayList<S6Security.Permit>(permission_list);
}




/********************************************************************************/
/*										*/
/*	Holder of a security permissions description				*/
/*										*/
/********************************************************************************/

private static class Description implements S6Security.Permit {

   private S6SecurityType security_type;
   private String argument_value;
   private String operation_list;

   Description(Element xml) {
      security_type = IvyXml.getAttrEnum(xml,"TYPE",S6SecurityType.NONE);
      operation_list = IvyXml.getTextElement(xml,"ACTIONS");
      argument_value = IvyXml.getTextElement(xml,"VALUE");
    }

   public S6SecurityType getType()		{ return security_type; }
   public String getArgument()			{ return argument_value; }
   public String getOperations()		{ return operation_list; }

}	// end of subclass Description




}	// end of class RequestSecurity




/* end of RequestSecurity.java */

