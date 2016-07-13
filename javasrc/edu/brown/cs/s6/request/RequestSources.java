/********************************************************************************/
/*										*/
/*		RequestSources.java						*/
/*										*/
/*	Basic implementation of a S6 set of sources				*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/request/RequestSources.java,v 1.4 2015/09/23 17:58:08 spr Exp $ */


/*********************************************************************************
 *
 * $Log: RequestSources.java,v $
 * Revision 1.4  2015/09/23 17:58:08  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.3  2013/09/13 20:33:08  spr
 * Add calls for UI search.
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


import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Element;

import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.s6.common.S6Constants;



class RequestSources implements S6Constants, RequestConstants {



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private Set<String>	include_set;
private Set<String>	exclude_set;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

RequestSources(Element xml)
{
   include_set = null;
   exclude_set = new HashSet<String>();

   if (xml == null) return;

   for (Element e : IvyXml.children(xml,"SOURCE")) {
      boolean use = IvyXml.getAttrBool(e,"USE");
      String s = IvyXml.getAttrString(e,"FILE");
      if (s == null) s = IvyXml.getText(e);
      if (use) {
	 if (include_set == null) include_set = new HashSet<String>();
	 include_set.add(s);
       }
      else {
	 exclude_set.add(s);
       }
    }
}




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

boolean useSource(String src)
{
   if (include_set != null && include_set.contains(src)) return true;
   if (include_set != null) return false;

   if (exclude_set.contains(src)) return false;

   return true;
}



Iterable<String> getSpecificSources()
{
   return include_set;
}



}	// end of class RequestSources




/* end of RequestSources.java */
