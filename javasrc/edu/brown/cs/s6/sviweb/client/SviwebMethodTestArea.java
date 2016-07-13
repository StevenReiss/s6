/********************************************************************************/
/*										*/
/*		SviwebMethodTestArea.java					*/
/*										*/
/*	Method test area table							*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/sviweb/client/SviwebMethodTestArea.java,v 1.6 2015/09/23 17:58:16 spr Exp $ */


/*********************************************************************************
 *
 * $Log: SviwebMethodTestArea.java,v $
 * Revision 1.6  2015/09/23 17:58:16  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.5  2013/09/13 20:33:15  spr
 * Add calls for UI search.
 *
 * Revision 1.4  2009-09-18 01:42:37  spr
 * Updates for new gwt and support user feedback and full class.
 *
 * Revision 1.3  2008-08-28 00:33:03  spr
 * Next version of S6.	Lots of bug fixes, some new functionality.
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

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.xml.client.Element;



public class SviwebMethodTestArea extends SviwebTestArea implements SviwebConstants {



/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private List<SviwebDeclaration> method_decl;


private static String [][] test_types = {
   { "CALL", "CALL" },
   { "USERCODE", "USER CODE" },
   { "TESTFILE", "TEST FILE" } };



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

SviwebMethodTestArea(SviwebMain sm,int tab,SviwebDeclaration mthd,StatusCallback cb)
{
   super(sm,tab,cb);

   method_decl = new ArrayList<SviwebDeclaration>();
   method_decl.add(mthd);
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

List<SviwebDeclaration> getMethods()
{
   return method_decl;
}


protected void addSignature(Element root)
{
   SviwebDeclaration decl = method_decl.get(0);
   SviwebXml.addChild(root,decl.getSignature());
}



/********************************************************************************/
/*										*/
/*	Test case methods							*/
/*										*/
/********************************************************************************/

protected void setupTestCase(SviwebTestCase tc)
{
   tc.setTestType(TestType.CALL);

   int row = getTestRow(tc);
   if (row == 0) {
      FocusWidget fw = tc.getFirstFocus();
      if (fw != null) fw.setFocus(true);
    }
}



/********************************************************************************/
/*										*/
/*	Test options								*/
/*										*/
/********************************************************************************/

ListBox addTestSelector(SviwebTestCase tc)
{
   int row = getTestRow(tc);

   ListBox lb = SviwebWidget.createSelection(tc.getTestType().toString(),test_types);
   lb.addChangeHandler(new TestChange(tc));

   test_box.setWidget(row,test_box.getCellCount(row),lb);

   return lb;
}




}	// end of class SviwebMethodTestArea




/* end of SviwebMethodTestArea.java */
