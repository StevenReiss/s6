/********************************************************************************/
/*										*/
/*		S6TestResults.java						*/
/*										*/
/*	Global representation for results of all test cases			*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/common/S6TestResults.java,v 1.7 2015/12/23 15:44:32 spr Exp $ */


/*********************************************************************************
 *
 * $Log: S6TestResults.java,v $
 * Revision 1.7  2015/12/23 15:44:32  spr
 * Add S6Resource.java, minor fixes.
 *
 * Revision 1.6  2014/08/29 15:15:46  spr
 * Updates for suise, testcases.
 *
 * Revision 1.5  2013/09/13 20:31:52  spr
 * Add UI search.
 *
 * Revision 1.4  2009-09-18 01:40:29  spr
 * Add full class option; add support for user interaction.
 *
 * Revision 1.3  2008-11-12 13:50:47  spr
 * Performance and bug updates.
 *
 * Revision 1.2  2008-08-28 00:32:45  spr
 * Next version of S6.	Lots of bug fixes, some new functionality.
 *
 * Revision 1.1.1.1  2008-06-03 12:59:21  spr
 * Initial version of S6
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.common;


import java.util.List;




public interface S6TestResults extends S6Constants {


   boolean allPassed();

   S6SolutionFlag getSummaryResult();

   double getRequiredTime();

   double getTime(String test);
   boolean getPassed(String test);
   boolean getFailed(String test);
   boolean getError(String test);
   String getErrorMessage(String test);
   String getUserType(String test);
   String getUserValue(String test);
   byte [] getJarFile(String test);

   void setTestStatus(String test,S6SolutionFlag sts);

   List<S6ErrorResult> getErrorResults();
   List<String> getMessageResults();
   
   String printSummary();

   interface S6ErrorResult {
      String getExpected();
      String getActual();
    }

}	// end of interface S6TestResults




/* end of S6TestResults.java */
