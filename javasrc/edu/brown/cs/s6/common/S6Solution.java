/********************************************************************************/
/*										*/
/*		S6Solution.java 						*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/common/S6Solution.java,v 1.12 2015/09/23 17:57:47 spr Exp $ */


/*********************************************************************************
 *
 * $Log: S6Solution.java,v $
 * Revision 1.12  2015/09/23 17:57:47  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.11  2015/02/14 19:40:02  spr
 * Add test case generation.
 *
 * Revision 1.10  2014/08/29 15:15:46  spr
 * Updates for suise, testcases.
 *
 * Revision 1.9  2013/09/13 20:31:52  spr
 * Add UI search.
 *
 * Revision 1.8  2012-07-20 22:14:48  spr
 * Additions for UI search
 *
 * Revision 1.7  2008-11-12 13:50:47  spr
 * Performance and bug updates.
 *
 * Revision 1.6  2008-08-28 00:32:45  spr
 * Next version of S6.	Lots of bug fixes, some new functionality.
 *
 * Revision 1.5  2008-07-18 22:25:58  spr
 * Update flags, minor bug fixes and extensions.
 *
 * Revision 1.4  2008-07-17 13:45:51  spr
 * Various bug fixes and clean ups.
 *
 * Revision 1.3  2008-06-27 15:45:31  spr
 * Add source access.
 *
 * Revision 1.2  2008-06-12 17:47:46  spr
 * Next version of S6.
 *
 * Revision 1.1.1.1  2008-06-03 12:59:21  spr
 * Initial version of S6
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.common;

import java.util.Collection;

import edu.brown.cs.cose.cosecommon.CoseSource;
import edu.brown.cs.ivy.xml.IvyXmlWriter;


public interface S6Solution extends S6Constants {


S6Fragment getBaseFragment();
S6Fragment getFragment();
S6Fragment getParentFragment();

S6Solution createNewSolution(S6Fragment n,S6Transform.Memo m);
void updateFragment(S6Fragment n);

void output(IvyXmlWriter xw,S6SolutionSet ss);

Collection<S6Transform.Memo> getTransforms();

boolean checkFlag(S6SolutionFlag fg);
void setFlag(S6SolutionFlag fg);
void clearFlag(S6SolutionFlag fg);

CoseSource getSource();
String getEncoding();

void resolve();
void clearResolve();
void clear();

String getId();
void setFormattedText(String txt);

double getScore();


}	// end of interface S6Solution



/* end of S6Solution.java */


