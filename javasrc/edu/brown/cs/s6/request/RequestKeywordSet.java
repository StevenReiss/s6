/********************************************************************************/
/*										*/
/*		RequestKeywordSet.java						*/
/*										*/
/*	Basic implementation of a S6 request keyword set			*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/request/RequestKeywordSet.java,v 1.4 2015/09/23 17:58:07 spr Exp $ */


/*********************************************************************************
 *
 * $Log: RequestKeywordSet.java,v $
 * Revision 1.4  2015/09/23 17:58:07  spr
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


import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import edu.brown.cs.cose.cosecommon.CoseRequest;
import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Exception;
import edu.brown.cs.s6.common.S6Request;



class RequestKeywordSet implements S6Request.KeywordSet, S6Constants, RequestConstants, CoseRequest.CoseKeywordSet {




/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private List<String> keyword_list;
private S6SearchLanguage search_language;
private List<String> required_words;


/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

RequestKeywordSet(Element xml) throws S6Exception
{
   keyword_list = new ArrayList<String>();
   required_words = null;
   search_language = IvyXml.getAttrEnum(xml,"LANGUAGE",S6SearchLanguage.JAVA);

   for (Element e : IvyXml.elementsByTag(xml,"KEYWORD")) {
      String k = IvyXml.getText(e);
      if (k.startsWith("*")) {
         k = k.substring(1);
         if (required_words == null) required_words = new ArrayList<String>();
         required_words.add(k);
       }
      keyword_list.add(k);
    }

   if (keyword_list.isEmpty()) throw new S6Exception("Empty keyword list");
}




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override public List<String> getWords()	        { return keyword_list; }

@Override public S6SearchLanguage getLanguage()         { return search_language; }

@Override public List<String> getRequiredWords()        { return required_words; }




}	// end of class RequestKeywordSet



/* end of RequestKeywordSet.java */
