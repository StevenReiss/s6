/********************************************************************************/
/*										*/
/*		RequestCose.java						*/
/*										*/
/*	Search Request using COSE						*/
/*										*/
/********************************************************************************/
/*	Copyright 2013 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2013, Brown University, Providence, RI.				 *
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



package edu.brown.cs.s6.request;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Element;

import edu.brown.cs.cose.cosecommon.CoseMaster;
import edu.brown.cs.cose.cosecommon.CoseRequest;
import edu.brown.cs.cose.cosecommon.CoseResult;
import edu.brown.cs.cose.cosecommon.CoseResultSet;
import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.s6.common.S6Contracts;
import edu.brown.cs.s6.common.S6Engine;
import edu.brown.cs.s6.common.S6Exception;
import edu.brown.cs.s6.common.S6Fragment;
import edu.brown.cs.s6.common.S6KeySearch;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6Security;
import edu.brown.cs.s6.common.S6SolutionSet;

public class RequestCose extends RequestBase implements CoseRequest, S6Request.Search,
	S6KeySearch
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private int		target_results;
private S6SearchType	search_type;
private CoseSearchType	cose_type;
private S6FormatType	format_type;
private CoseScopeType	scope_type;
private List<RequestKeywordSet> keyword_sets;
private RequestTests	test_data;
private EnumSet<CoseSearchEngine> engine_set;
private RequestSources	 source_set;
private RequestSecurity security_holder;
private RequestContracts contract_holder;
private List<String>	required_keywords;

private static final CoseSearchEngine DEFAULT_ENGINE = CoseSearchEngine.SEARCHCODE;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public RequestCose(S6Engine eng,Element xml) throws S6Exception
{
   super(eng,xml);
   loadRequest(xml);
}


/********************************************************************************/
/*										*/
/*	Basic Access methods for COSE						*/
/*										*/
/********************************************************************************/

@Override public int getNumberOfResults()		{ return target_results; }

@Override public int getNumberOfThreads()
{
   return getEngine().getNumberOfSearchThreads();
}


@Override public int getMaxPackageFiles()               { return 50; }

@Override public boolean doDebug()
{
   return getEngine().doDebug();
}

@Override public CoseSearchType getCoseSearchType()	{ return cose_type; }

@Override public CoseScopeType getCoseScopeType()	{ return scope_type; }

@Override public List<CoseKeywordSet> getCoseKeywordSets()
{
   return new ArrayList<CoseKeywordSet>(keyword_sets);
}

@Override public CoseSearchLanguage getLanguage()
{
   RequestKeywordSet rws = keyword_sets.get(0);
   switch (rws.getLanguage()) {
      default :
      case JAVA :
	 return CoseSearchLanguage.JAVA;
      case XML :
	 return CoseSearchLanguage.XML;
    }
}

@Override public Set<CoseSearchEngine> getEngines()
{
   return engine_set;
}

@Override public Set<String> getSpecificSources()
{
   return source_set.getSpecificSources();
}



/********************************************************************************/
/*										*/
/*     Access methods for S6							*/
/*										*/
/********************************************************************************/

@Override public S6SearchType getSearchType()		{ return search_type; }

@Override public CoseScopeType getScopeType()
{
   return scope_type;
}

@Override public Collection<KeywordSet> getKeywordSets()
{
   return new ArrayList<KeywordSet>(keyword_sets);
}

@Override public Tests getTests()			{ return test_data; }

@Override public Set<S6Location> getLocations()
{
   return EnumSet.allOf(S6Location.class);
}

@Override public boolean useSource(String source)
{
   return source_set.useSource(source);
}

@Override public S6Security getSecurity()		{ return security_holder; }

@Override public S6Contracts getContracts()		{ return contract_holder; }

@Override public S6FormatType getFormatType()		{ return format_type; }

@Override public List<String> getRequiredWords()	{ return required_keywords; }

@Override public List<String> getKeyTerms()		{ return required_keywords; }

@Override public String editSource(String orig)         { return orig; } 



/********************************************************************************/
/*										*/
/*	Search method								*/
/*										*/
/********************************************************************************/

@Override public void getInitialSolutions(S6SolutionSet ss)
{
   CoseMaster cose = CoseMaster.createMaster(this);
   ResultHandler hdlr = new ResultHandler(ss);
   cose.computeSearchResults(hdlr);
}



/********************************************************************************/
/*										*/
/*	Handle results								*/
/*										*/
/********************************************************************************/

private class ResultHandler implements CoseResultSet {

   private S6SolutionSet solution_set;

   ResultHandler(S6SolutionSet ss) {
      solution_set = ss;
    }

   @Override public void addResult(CoseResult cr) {
      S6Fragment frag = getEngine().createFragment(cr,solution_set.getRequest());
      solution_set.addInitialSolution(frag,cr.getSource());
    }
   
   @Override public void removeResult(CoseResult cr) {
      // shouldn't be used here
    }

}	// end of inner class ResultHandler


/********************************************************************************/
/*										*/
/*	Load the search request from the XML					*/
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
   target_results = IvyXml.getAttrInt(xml,"RESULTS",200);

   switch (search_type) {
      case METHOD :
	 cose_type = CoseSearchType.METHOD;
	 scope_type = CoseScopeType.FILE;
	 break;
      case CLASS :
	 cose_type = CoseSearchType.CLASS;
	 scope_type = CoseScopeType.FILE;
	 break;
      case FULLCLASS :
	 cose_type = CoseSearchType.CLASS;
	 scope_type = CoseScopeType.FILE;
	 break;
      case TESTCASES :
	 cose_type = CoseSearchType.TESTCLASS;
	 scope_type = CoseScopeType.FILE;
	 break;
      case UIFRAMEWORK :
	 cose_type = CoseSearchType.PACKAGE;
	 scope_type = CoseScopeType.PACKAGE_UI;
	 break;
      case ANDROIDUI :
	 cose_type = CoseSearchType.ANDROIDUI;
	 scope_type = CoseScopeType.PACKAGE_UI;
	 break;
      case PACKAGE :
	 cose_type = CoseSearchType.PACKAGE;
	 scope_type = CoseScopeType.SYSTEM;
	 break;
      case APPLICATION :
	 cose_type = CoseSearchType.PACKAGE;
	 scope_type = CoseScopeType.SYSTEM;
	 break;
    }
   scope_type = IvyXml.getAttrEnum(xml,"SCOPE",scope_type);

   engine_set = EnumSet.noneOf(CoseSearchEngine.class);
   keyword_sets = new ArrayList<RequestKeywordSet>();
   required_keywords = new ArrayList<String>();
   for (Element kws : IvyXml.elementsByTag(xml,"KEYWORDS")) {
      RequestKeywordSet rks = new RequestKeywordSet(kws);
      keyword_sets.add(rks);
      List<String> rq = rks.getRequiredWords();
      if (rq != null) required_keywords.addAll(rq);
    }
   for (Element kws : IvyXml.elementsByTag(xml,"KEY")) {
      String txt = IvyXml.getText(kws);
      if (txt != null && txt.length() > 1) required_keywords.add(txt);
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

   for (CoseSearchEngine loc : CoseSearchEngine.values()) {
      if (IvyXml.getAttrBool(xml,loc.toString())) {
	 engine_set.add(loc);
       }
    }

   if (source_set.getSpecificSources() != null) {
      for (String s : source_set.getSpecificSources()) {
	 for (CoseSearchEngine loc : CoseSearchEngine.values()) {
	    String pfx = loc.toString() + ":";
	    if (s.startsWith(pfx)) {
	       engine_set.add(loc);
	     }
	  }
       }
    }

   if (engine_set.isEmpty() && IvyXml.getAttrBool(xml,"REMOTE")) {
      engine_set.add(DEFAULT_ENGINE);
    }
}



}	// end of class RequestCose




/* end of RequestCose.java */

