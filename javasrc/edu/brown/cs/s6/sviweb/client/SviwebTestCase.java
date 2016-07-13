/********************************************************************************/
/*										*/
/*		SviwebTestCase.java						*/
/*										*/
/*	Single test case element						*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/sviweb/client/SviwebTestCase.java,v 1.6 2013/09/13 20:33:16 spr Exp $ */


/*********************************************************************************
 *
 * $Log: SviwebTestCase.java,v $
 * Revision 1.6  2013/09/13 20:33:16  spr
 * Add calls for UI search.
 *
 * Revision 1.5  2009-09-18 01:42:37  spr
 * Updates for new gwt and support user feedback and full class.
 *
 * Revision 1.4  2008-11-12 13:52:27  spr
 * Performance and bug updates.
 *
 * Revision 1.3  2008-07-17 13:47:46  spr
 * User interface fixups, mainly for class search.
 *
 * Revision 1.2  2008-06-12 17:47:57  spr
 * Next version of S6.
 *
 * Revision 1.1.1.1  2008-06-03 12:59:23  spr
 * Initial version of S6
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.sviweb.client;

import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Widget;



public class SviwebTestCase implements SviwebConstants {



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private SviwebMain	svi_web;
private SviwebTestArea	test_area;

private SviwebTestCaseImpl test_impl;

private String test_name;
private Status test_status;
private String last_error;
private static int test_counter = 0;

private final String VARIABLE_REGEX = "^\\w+$";



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

SviwebTestCase(SviwebMain sm,SviwebTestArea ta)
{
   svi_web = sm;
   test_area = ta;

   test_name = "SVIWEB_" + (++test_counter);

   last_error = null;

   test_status = Status.UNCHECKED;
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

TestType getTestType()
{
   if (test_impl == null) return null;
   return test_impl.getTestType();
}


TestOp getTestOp()				{ return test_impl.getTestOp(); }

String getTestOutput()				{ return test_impl.getTestOutput(); }

String getTestInput()				{ return test_impl.getTestInput(); }

SviwebDeclaration getTestMethod()		{ return test_impl.getTestMethod(); }

int getTestRow()				{ return test_area.getTestRow(this); }

boolean isEmpty()				{ return test_impl.isEmpty(); }

FocusWidget getFirstFocus()			{ return test_impl.getFirstFocus(); }

String getTestName()				{ return test_name; }

String getThisName()				{ return test_impl.getThisName(); }



String getResultVariable()
{
   if (getTestOp() == TestOp.SAVE) {
      String v = getTestOutput().trim();
      if (v.matches(VARIABLE_REGEX)) return v;
    }

   return null;
}


/********************************************************************************/
/*										*/
/*	Widget methods								*/
/*										*/
/********************************************************************************/

int addCell(Widget w,int span)
{
   return test_area.addTestCell(this,w,span);
}



void setCell(int idx,Widget w)
{
   test_area.setCell(this,idx,w);
}


void addTestSelector()
{
   test_area.addTestSelector(this);
}



void ensureEmptyTest()
{
   // handle CALLSET and CALLSETCONT

   test_area.ensureEmptyTest();
}



void resetSubsequentTests()
{
   test_area.resetSubsequentTests(this);
}



/********************************************************************************/
/*										*/
/*	Creation methods							*/
/*										*/
/********************************************************************************/

void setTestType(TestType tt)
{
   if (getTestType() == tt) return;

   test_area.clearTestRow(this);

   createTest(tt);
}



void reset()
{
   test_impl.reset();
}



private void createTest(TestType tt)
{
   test_impl = SviwebTestCaseImpl.create(svi_web,test_area,this,tt);
   test_impl.setup();
}



/********************************************************************************/
/*										*/
/*	Status methods								*/
/*										*/
/********************************************************************************/

Status getTestStatus()
{
   if (test_status == Status.UNCHECKED && isEmpty()) {
      test_status = Status.UNUSED;
    }

   return test_status;
}


String getErrorMessage()
{
   return last_error;
}



void invalidate()
{
   test_status = Status.UNCHECKED;
}


void setChecking()
{
   test_status = Status.CHECKING;
}



void setOk()
{
   if (test_status == Status.CHECKING) test_status = Status.OK;
}



void setError(String msg)
{
   if (test_status == Status.CHECKING) {
      test_status = Status.BAD;
      last_error = msg;
    }
}



}	// end of class SviwebTestCase




/* end of SviwebTestCase.java */













































