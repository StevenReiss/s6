/********************************************************************************/
/*										*/
/*		S6Factory.java							*/
/*										*/
/*	Public interface for creating various S6 entities			*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/common/S6Factory.java,v 1.3 2015/09/23 17:57:46 spr Exp $ */


/*********************************************************************************
 *
 * $Log: S6Factory.java,v $
 * Revision 1.3  2015/09/23 17:57:46  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.2  2008-08-28 00:32:45  spr
 * Next version of S6.  Lots of bug fixes, some new functionality.
 *
 * Revision 1.1.1.1  2008-06-03 12:59:21  spr
 * Initial version of S6
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.common;


import org.w3c.dom.Element;



public abstract class S6Factory implements S6Constants {



public static S6Factory createS6Factory() {
   return S6FactoryImpl.getDefaultFactory();
}


abstract public S6Request.Search createSearchRequest(S6Engine eng,Element xml) throws S6Exception;
abstract public S6Request.Check createCheckRequest(S6Engine eng,Element xml) throws S6Exception;
abstract public S6Request.Format createFormatRequest(S6Engine eng,Element xml) throws S6Exception;

abstract public void getInitialSolutions(S6SolutionSet ss) throws S6Exception;

abstract public S6SolutionSet createSolutionSet(S6Request.Search rq);

abstract public S6Language createLanguage(S6Engine eng,String name);

abstract public S6License createLicenseManager();

abstract public boolean useCose();



}	// end of interface S6Factory



/* end of S6Factory.java */


