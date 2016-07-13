/********************************************************************************/
/*										*/
/*		RequestFormat.java						*/
/*										*/
/*	Basic implementation of a S6 formatting request 			*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/request/RequestFormat.java,v 1.3 2015/09/23 17:58:07 spr Exp $ */


/*********************************************************************************
 *
 * $Log: RequestFormat.java,v $
 * Revision 1.3  2015/09/23 17:58:07  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.2  2008-11-12 13:52:19  spr
 * Performance and bug updates.
 *
 * Revision 1.1  2008-08-28 00:32:58  spr
 * Next version of S6.  Lots of bug fixes, some new functionality.
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.request;


import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.s6.common.S6Engine;
import edu.brown.cs.s6.common.S6Exception;
import edu.brown.cs.s6.common.S6Request;


public class RequestFormat extends RequestBase implements S6Request.Format {



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private List<FmtElement> to_format;
private S6SearchType search_type;
private S6FormatType format_type;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public RequestFormat(S6Engine eng,Element xml) throws S6Exception
{
   super(eng,xml);

   search_type = IvyXml.getAttrEnum(xml,"WHAT",S6SearchType.METHOD);
   format_type = IvyXml.getAttrEnum(xml,"TYPE",S6FormatType.NONE);

   to_format = new ArrayList<FmtElement>();

   for (Element e : IvyXml.children(xml,"ITEM")) {
      FmtElement fe = new FmtElement(e);
      to_format.add(fe);
    }
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

public S6SearchType getSearchType()		{ return search_type; }


public S6FormatType getFormatType()		{ return format_type; }


public Iterable<S6Request.FormatItem> getItems()
{
   return new ArrayList<S6Request.FormatItem>(to_format);
}


/********************************************************************************/
/*										*/
/*	Class to hold information about an item to format			*/
/*										*/
/********************************************************************************/

private static class FmtElement implements S6Request.FormatItem {

   private String element_id;
   private String element_text;

   FmtElement(Element e) {
      element_id = IvyXml.getAttrString(e,"ID");
      element_text = IvyXml.getText(e);
    }

   public String getId()			{ return element_id; }
   public String getText()			{ return element_text; }

}	// end of subclass FmtElement



}	// end of class RequestFormat



/* end of RequestFormat.java */

