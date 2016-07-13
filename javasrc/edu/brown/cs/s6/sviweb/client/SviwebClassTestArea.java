/********************************************************************************/
/*										*/
/*		SviwebClassTestArea.java					*/
/*										*/
/*	Class test area table							*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/sviweb/client/SviwebClassTestArea.java,v 1.8 2015/09/23 17:58:16 spr Exp $ */


/*********************************************************************************
 *
 * $Log: SviwebClassTestArea.java,v $
 * Revision 1.8  2015/09/23 17:58:16  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.7  2013/09/13 20:33:15  spr
 * Add calls for UI search.
 *
 * Revision 1.6  2012-06-11 14:08:22  spr
 * Add framework search; fix bugs
 *
 * Revision 1.5  2009-09-18 01:42:37  spr
 * Updates for new gwt and support user feedback and full class.
 *
 * Revision 1.4  2008-11-12 13:52:27  spr
 * Performance and bug updates.
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

import java.util.List;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.xml.client.Element;



public class SviwebClassTestArea extends SviwebTestArea implements SviwebConstants {



/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private SviwebClassSearch	class_search;



private static String [][] test_types = {
   { "CALLSET", "CALL SET" },
   { "CLASSCALL", "CALL" },
   { "USERCODE", "USER CODE" },
   { "TESTFILE", "TEST FILE" }
};



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

SviwebClassTestArea(SviwebMain sm,int tab,SviwebClassSearch cs,StatusCallback sb)
{
   super(sm,tab,sb);

   class_search = cs;
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

List<SviwebDeclaration> getMethods()
{
   return class_search.getMethods();
}



/********************************************************************************/
/*										*/
/*	Test case methods							*/
/*										*/
/********************************************************************************/

protected void setupTestCase(SviwebTestCase tc)
{
   tc.setTestType(TestType.CALLSET);
}


/********************************************************************************/
/*										*/
/*	Test options								*/
/*										*/
/********************************************************************************/

ListBox addTestSelector(SviwebTestCase tc)
{
   TestType tt = tc.getTestType();
   if (tt == TestType.CALLSETCONT) {
      int row = getTestRow(tc);
      while (row > 0) {
	 SviwebTestCase tca = test_cases.get(row);
	 if (tca.getTestType() == TestType.CALLSET) {
	    FlexTable.FlexCellFormatter fmt = test_box.getFlexCellFormatter();
	    int col = test_box.getCellCount(row) - 1;
	    fmt.setRowSpan(row,col,fmt.getRowSpan(row,col)+1);
	    fmt.setVerticalAlignment(row,col,HasVerticalAlignment.ALIGN_MIDDLE);
	    break;
	  }
	 --row;
       }
    }
   else {
      int row = getTestRow(tc);
      ListBox lb = SviwebWidget.createSelection(tc.getTestType().toString(),test_types);
      lb.addChangeHandler(new TestChange(tc));
      test_box.setWidget(row,test_box.getCellCount(row),lb);

      return lb;
    }

   return null;
}




/********************************************************************************/
/*										*/
/*	Methods to add signature to request					*/
/*										*/
/********************************************************************************/

protected void addSignature(Element root)
{
   Element sign = SviwebXml.addChild(root,"SIGNATURE");
   Element celt = SviwebXml.addChild(sign,"CLASS");
   celt.setAttribute("NAME",class_search.getClassName());
   String sup = class_search.getSuperclassName();
   if (sup != null) celt.setAttribute("SUPERCLASS",sup);
   String ifc = class_search.getInterfaceNames();
   if (ifc != null) celt.setAttribute("IMPLEMENTS",ifc);

   for (SviwebDeclaration sd : getMethods()) {
      Element sdx = sd.getSignature();
      if (sdx != null) {
	 Element sdm = SviwebXml.getXmlElement(sdx,"METHOD");
	 SviwebXml.addChild(celt,sdm);
       }
    }
}




}	// end of class SviwebClassTestArea




/* end of SviwebClassTestArea.java */

