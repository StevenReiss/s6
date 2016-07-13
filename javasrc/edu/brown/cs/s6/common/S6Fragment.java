/********************************************************************************/
/*										*/
/*		S6Fragment.java 						*/
/*										*/
/*	Public interface for code fragments					*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/common/S6Fragment.java,v 1.10 2015/09/23 17:57:47 spr Exp $ */


/*********************************************************************************
 *
 * $Log: S6Fragment.java,v $
 * Revision 1.10  2015/09/23 17:57:47  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.9  2014/08/29 15:15:46  spr
 * Updates for suise, testcases.
 *
 * Revision 1.8  2013/09/13 20:31:52  spr
 * Add UI search.
 *
 * Revision 1.7  2012-08-13 16:50:13  spr
 * Add solution to fixDependencies
 *
 * Revision 1.6  2012-06-11 14:07:27  spr
 * Code cleanup
 *
 * Revision 1.5  2009-09-18 01:40:29  spr
 * Add full class option; add support for user interaction.
 *
 * Revision 1.4  2009-05-12 22:26:06  spr
 * Editing fixups.
 *
 * Revision 1.3  2008-08-28 00:32:45  spr
 * Next version of S6.	Lots of bug fixes, some new functionality.
 *
 * Revision 1.2  2008-07-18 22:25:58  spr
 * Update flags, minor bug fixes and extensions.
 *
 * Revision 1.1.1.1  2008-06-03 12:59:21  spr
 * Initial version of S6
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.common;


import java.util.Collection;


public interface S6Fragment extends S6Constants {

   S6FragmentType getFragmentType();
   S6Request.Search getSearchRequest();

   String getKeyText();
   String getText();
   String getFinalText(S6SearchType st);

   boolean checkSignature(S6Request.Signature r,S6SignatureType st);
   boolean checkName(S6Request.Signature r);
   boolean checkSignature(S6Request.Signature r);	  // full
   boolean checkParameters(S6Request.Signature r);
   boolean checkReturn(S6Request.Signature r);
   boolean checkExceptions(S6Request.Signature r);
   boolean checkInitial(S6Request.Signature sgn);

   boolean fixDependencies(S6SolutionSet ss,S6Solution sol);

   S6SolutionFlag checkTestCases(S6Request.Search r,S6Source src);

   Collection<S6Fragment> getFragments(S6SearchType typ);
   Collection<S6Fragment> getFileFragments();
   Collection<String> getPackages();
   Collection<S6Resource> getResources();
   
   String getBasePackage();
   boolean addPackage(String pkg);

   void resolveFragment();
   void clearResolve();

   void makeLocal(S6SolutionSet ss);

   S6TestResults getTestResults();

   int getCodeComplexity();

   void addInnerFragment(S6Fragment f);
   
   void addResource(S6Resource resource);

}	// end of interface S6Language



/* end of S6Language.java */















