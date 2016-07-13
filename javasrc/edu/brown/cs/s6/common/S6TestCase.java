/********************************************************************************/
/*										*/
/*		S6TestCase.java 						*/
/*										*/
/*	Global representation of a test case					*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/common/S6TestCase.java,v 1.4 2013/09/13 20:31:52 spr Exp $ */


/*********************************************************************************
 *
 * $Log: S6TestCase.java,v $
 * Revision 1.4  2013/09/13 20:31:52  spr
 * Add UI search.
 *
 * Revision 1.3  2013-05-09 12:26:12  spr
 * Minor changes to start ui fixups.
 *
 * Revision 1.2  2009-09-18 01:40:29  spr
 * Add full class option; add support for user interaction.
 *
 * Revision 1.1.1.1  2008-06-03 12:59:21  spr
 * Initial version of S6
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.common;


import java.util.List;




public interface S6TestCase extends S6Constants {

String getName();
S6TestType getTestType();
boolean isOptional();
String getUserCode();
List<CallTest> getCalls();
boolean getNeedsUserInput();

interface CallTest {
   boolean isConstructor();
   String getMethod();
   List<CallArg> getArguments();
   CallArg getReturnValue();
   S6TestOp getOperator();
   String getThrows();
   boolean getNeedsUserInput();
   boolean isAccess();			// access, no call
}


interface CallArg {
   S6TestArgType getArgType();
   String getArgValue();
   String getArgCode();
}

interface JunitTest {
   String getJunitClass();
   String getJunitName();
}



}	// end of interface S6TestCase



/* end of S6TestCase.java */


