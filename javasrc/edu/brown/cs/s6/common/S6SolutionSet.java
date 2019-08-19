/********************************************************************************/
/*										*/
/*		S6SolutionSet.java						*/
/*										*/
/*	Global representation of a potential solution to report back		*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/common/S6SolutionSet.java,v 1.9 2015/09/23 17:57:48 spr Exp $ */


/*********************************************************************************
 *
 * $Log: S6SolutionSet.java,v $
 * Revision 1.9  2015/09/23 17:57:48  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.8  2015/02/14 19:40:02  spr
 * Add test case generation.
 *
 * Revision 1.7  2013/09/13 20:31:52  spr
 * Add UI search.
 *
 * Revision 1.6  2012-07-20 22:14:48  spr
 * Additions for UI search
 *
 * Revision 1.5  2012-06-11 14:07:27  spr
 * Code cleanup
 *
 * Revision 1.4  2008-11-12 13:50:47  spr
 * Performance and bug updates.
 *
 * Revision 1.3  2008-08-28 00:32:45  spr
 * Next version of S6.	Lots of bug fixes, some new functionality.
 *
 * Revision 1.2  2008-07-17 13:45:51  spr
 * Various bug fixes and clean ups.
 *
 * Revision 1.1.1.1  2008-06-03 12:59:21  spr
 * Initial version of S6
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.common;


import java.util.Iterator;
import java.util.List;

import edu.brown.cs.cose.cosecommon.CoseConstants;
import edu.brown.cs.cose.cosecommon.CoseSource;
import edu.brown.cs.ivy.xml.IvyXmlWriter;



public interface S6SolutionSet extends S6Constants, CoseConstants, Iterable<S6Solution> {

boolean useSource(CoseSource src);

S6SearchType getSearchType();
CoseScopeType getScopeType();
S6Request.Search getRequest();
S6Engine getEngine();

S6Solution addInitialSolution(S6Fragment n,CoseSource src);

S6Solution add(S6Solution s);
void remove(S6Solution s);

void pruneSolutions(int max);
boolean restoreSolutions(int max);

Iterator<S6Solution> iterator();
int getSolutionCount();
boolean checkClearResolve();
List<S6Solution> getSolutions();
boolean doTestFixup();

void output(IvyXmlWriter xw);

int getNumberRemoved();
int getSourceCount();

void saveCounts(String key);

boolean doDebug();


}	// end of interface S6Solution



/* end of S6Solution.java */



