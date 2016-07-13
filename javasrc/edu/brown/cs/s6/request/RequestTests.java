/********************************************************************************/
/*										*/
/*		RequestTests.java						*/
/*										*/
/*	Basic implementation of a S6 set of tests				*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/request/RequestTests.java,v 1.5 2015/09/23 17:58:08 spr Exp $ */


/*********************************************************************************
 *
 * $Log: RequestTests.java,v $
 * Revision 1.5  2015/09/23 17:58:08  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.4  2013/09/13 20:33:08  spr
 * Add calls for UI search.
 *
 * Revision 1.3  2012-06-11 14:08:10  spr
 * Add framework search; fix bugs
 *
 * Revision 1.2  2008-08-28 00:32:58  spr
 * Next version of S6.	Lots of bug fixes, some new functionality.
 *
 * Revision 1.1.1.1  2008-06-03 12:59:22  spr
 * Initial version of S6
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.request;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Element;

import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Exception;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6TestCase;



class RequestTests implements S6Request.Tests, S6Constants, RequestConstants {




/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private List<S6TestCase> test_cases;
private List<String> import_types;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

RequestTests(Element xml) throws S6Exception
{
   Set<String> testnames = new HashSet<String>();

   test_cases = new ArrayList<S6TestCase>();
   import_types = new ArrayList<String>();

   if (xml != null) {
      for (Element e : IvyXml.elementsByTag(xml,"TESTCASE")) {
	 RequestTestCase rc = RequestTestCase.createTestCase(e);
	 if (rc != null) {
	    if (!testnames.add(rc.getName()))
	       throw new S6Exception("Duplicate test " + rc.getName());
	    test_cases.add(rc);
	  }
       }
      for (Element e : IvyXml.elementsByTag(xml,"IMPORT")) {
	 String s = IvyXml.getAttrString(e,"TYPE");
	 import_types.add(s);
       }
    }
}




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

public Iterable<S6TestCase> getTestCases()		{ return test_cases; }


public Iterable<String> getImportTypes()		{ return import_types; }


int getTestCount()					{ return test_cases.size(); }



}	// end of class RequestTests




/* end of RequestTests.java */
