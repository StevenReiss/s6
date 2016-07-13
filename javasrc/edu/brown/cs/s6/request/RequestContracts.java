/********************************************************************************/
/*										*/
/*		RequestContracts.java						*/
/*										*/
/*	Basic implementation of a S6 search request contract description	*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/request/RequestContracts.java,v 1.4 2015/09/23 17:58:07 spr Exp $ */


/*********************************************************************************
 *
 * $Log: RequestContracts.java,v $
 * Revision 1.4  2015/09/23 17:58:07  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.3  2008-11-12 13:52:19  spr
 * Performance and bug updates.
 *
 * Revision 1.2  2008-07-17 13:47:05  spr
 * Add contracts, augment request information in general.
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
import edu.brown.cs.s6.common.S6Contracts;
import edu.brown.cs.s6.common.S6Request;



class RequestContracts implements S6Contracts {



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private RequestSearch for_search;
private List<Specification> spec_list;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

RequestContracts(RequestSearch rs,Element xml)
{
   for_search = rs;

   spec_list = new ArrayList<Specification>();
   if (xml != null) {
      for (Element e : IvyXml.elementsByTag(xml,"CONTRACT")) {
	 Specification s = new Specification(e);
	 spec_list.add(s);
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
   return spec_list.isEmpty();
}




public List<S6Contracts.Contract> getContracts()
{
   return new ArrayList<S6Contracts.Contract>(spec_list);
}




/********************************************************************************/
/*										*/
/*	Holder of a security permissions description				*/
/*										*/
/********************************************************************************/

private class Specification implements S6Contracts.Contract {

   private S6ContractType contract_type;
   private String contract_method;
   private String contract_code;

   Specification(Element xml) {
      contract_type = IvyXml.getAttrEnum(xml,"TYPE",S6ContractType.NONE);
      contract_method = IvyXml.getTextElement(xml,"METHOD");
      contract_code = IvyXml.getTextElement(xml,"CODE");
    }

   public S6ContractType getType()		{ return contract_type; }
   public String getCode()			{ return contract_code; }
   public String getMethod()			{ return contract_method; }

   public List<String> getParameterNames() {
      S6Request.Signature rs = for_search.getSignature();
      S6Request.MethodSignature ms = rs.getMethod(contract_method);
      return ms.getParameterNames();
    }


}	// end of subclass Specification




}	// end of class RequestContracts




/* end of RequestContracts.java */


