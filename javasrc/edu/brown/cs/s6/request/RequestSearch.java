/********************************************************************************/
/*										*/
/*		RequestSearch.java						*/
/*										*/
/*	Basic implementation of a S6 search request				*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/request/RequestSearch.java,v 1.15 2016/07/18 23:05:37 spr Exp $ */


/*********************************************************************************
 *
 * $Log: RequestSearch.java,v $
 * Revision 1.15  2016/07/18 23:05:37  spr
 * Update request to contain more info for applications.
 *
 * Revision 1.14  2015/12/23 15:45:12  spr
 * Minor fixes.
 *
 * Revision 1.13  2015/09/23 17:58:08  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.12  2014/08/29 15:16:13  spr
 * Updates for suise, testcases.
 *
 * Revision 1.11  2014/02/26 14:06:54  spr
 * Update search request.
 *
 * Revision 1.10  2013/09/13 20:33:08  spr
 * Add calls for UI search.
 *
 * Revision 1.9  2013-05-09 12:26:23  spr
 * Minor changes to start ui fixups.
 *
 * Revision 1.8  2012-06-11 18:40:29  spr
 * Updated transform for ui serach
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
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import org.w3c.dom.Element;

import edu.brown.cs.cose.cosecommon.CoseConstants;
import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.s6.common.S6Contracts;
import edu.brown.cs.s6.common.S6Engine;
import edu.brown.cs.s6.common.S6Exception;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6Security;


import java.util.Set;

public class RequestSearch extends RequestBase implements S6Request.Search, CoseConstants {



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private S6SearchType search_type;
private List<KeywordSet> keyword_sets;
private RequestTests test_data;
private EnumSet<S6Location> location_set;
private RequestSources source_set;
private RequestSecurity security_holder;
private RequestContracts contract_holder;
private S6FormatType format_type;
private CoseScopeType scope_type;
private List<String>     required_keywords;


private static EnumSet<S6Location> REMOTE_DEFAULT_SET =
   EnumSet.of(S6Location.OHLOH);





/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public RequestSearch(S6Engine eng,Element xml) throws S6Exception
{
   super(eng,xml);

   location_set = EnumSet.noneOf(S6Location.class);

   loadRequest(xml);
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

public S6SearchType getSearchType()		{ return search_type; }

public Collection<KeywordSet> getKeywordSets()
{
   return keyword_sets;
}

public S6Request.Tests getTests()
{
   return test_data;
}

public Set<S6Location> getLocations()           { return location_set; }

public boolean useSource(String source) 	{ return source_set.useSource(source); }

public Iterable<String> getSpecificSources()	{ return source_set.getSpecificSources(); }

public S6Security getSecurity() 		{ return security_holder; }

public S6Contracts getContracts()		{ return contract_holder; }

public S6FormatType getFormatType()		{ return format_type; }

public CoseScopeType getScopeType()		{ return scope_type; }

public List<String> getRequiredWords()          { return required_keywords; }

public String getPackage()
{
   return getSignature().getPackage();
}



/********************************************************************************/
/*										*/
/*	Methods to load xml based request					*/
/*										*/
/********************************************************************************/

private void loadRequest(Element xml) throws S6Exception
{
   if (IvyXml.isElement(xml,"S6")) {
      xml = IvyXml.getElementByTag(xml,"SEARCH");
    }

   if (!IvyXml.isElement(xml,"SEARCH"))
      throw new S6Exception("Attempt to do a search without SEARCH element");

   search_type = IvyXml.getAttrEnum(xml,"WHAT",S6SearchType.METHOD);

   format_type = IvyXml.getAttrEnum(xml,"FORMAT",S6FormatType.NONE);
   
   switch (search_type) {
      case METHOD :
      case CLASS :
      case FULLCLASS :
      case TESTCASES :
	 scope_type = CoseScopeType.FILE;
	 break;
      case UIFRAMEWORK :
      case ANDROIDUI :
	 scope_type = CoseScopeType.PACKAGE_UI;
	 break;
      case PACKAGE :
      case APPLICATION :
	 scope_type = CoseScopeType.SYSTEM;
	 break;
    }
   scope_type = IvyXml.getAttrEnum(xml,"SCOPE",scope_type);

   keyword_sets = new ArrayList<KeywordSet>();
   required_keywords = new ArrayList<String>();
   for (Element kws : IvyXml.elementsByTag(xml,"KEYWORDS")) {
      RequestKeywordSet rks = new RequestKeywordSet(kws);
      keyword_sets.add(rks);
      List<String> rq = rks.getRequiredWords();
      if (rq != null) required_keywords.addAll(rq);
    }

   Element tsts = IvyXml.getElementByTag(xml,"TESTS");
   test_data = new RequestTests(tsts);

   switch (search_type) {
      case METHOD :
	 if (getSignature() == null || !(getSignature() instanceof RequestMethod))
	    throw new S6Exception("METHOD request without signature");
	 if (test_data.getTestCount() == 0)
	    throw new S6Exception("At least one test case must be given");
	 break;
      case CLASS :
      case FULLCLASS :
	 if (getSignature() == null || !(getSignature() instanceof RequestClass))
	    throw new S6Exception("CLASS request without signature");
	 if (test_data.getTestCount() == 0)
	    throw new S6Exception("At least one test case must be given");
	 break;
      case PACKAGE :
      case APPLICATION :
	 if (getSignature() == null || !(getSignature() instanceof RequestPackage))
	    throw new S6Exception("PACKAGE request without signature");
	 break;
      case UIFRAMEWORK :
      case ANDROIDUI :
	 if (getSignature() == null || !(getSignature() instanceof RequestUIFramework))
	    throw new S6Exception("UIFRAMEWORK request without signature");
	 break;
      case TESTCASES :
         if (getSignature() == null || !(getSignature() instanceof RequestTesting))
	    throw new S6Exception("TESTING request without signature");     
         break;
    }

   Element srcs = IvyXml.getElementByTag(xml,"SOURCES");
   source_set = new RequestSources(srcs);

   Element secy = IvyXml.getElementByTag(xml,"SECURITY");
   security_holder = new RequestSecurity(secy);

   Element cntr = IvyXml.getElementByTag(xml,"CONTRACTS");
   contract_holder = new RequestContracts(getSignature(),cntr);

   boolean hasremote = false;
   for (S6Location loc : S6Location.values()) {
      if (IvyXml.getAttrBool(xml,loc.toString())) {
	 location_set.add(loc);
	 if (loc != S6Location.LOCAL) hasremote = true;
       }
    }

   if (source_set.getSpecificSources() != null) {
      for (String s : source_set.getSpecificSources()) {
	 for (S6Location loc : S6Location.values()) {
	    String pfx = loc.toString() + ":";
	    if (s.startsWith(pfx)) {
	       location_set.add(loc);
	       if (loc != S6Location.LOCAL) hasremote = true;
	     }
	  }
       }
    }

   if (!hasremote && IvyXml.getAttrBool(xml,"REMOTE")) location_set.addAll(REMOTE_DEFAULT_SET);
   if (location_set.isEmpty()) location_set = EnumSet.allOf(S6Location.class);
}



}	// end of class RequestSearch




/* end of RequestSearch.java */
