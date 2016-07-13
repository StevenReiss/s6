/********************************************************************************/
/*										*/
/*		SviwebTestArea.java						*/
/*										*/
/*	Table to hold an extensible set of tests				*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/sviweb/client/SviwebTestArea.java,v 1.11 2015/09/23 17:58:17 spr Exp $ */


/*********************************************************************************
 *
 * $Log: SviwebTestArea.java,v $
 * Revision 1.11  2015/09/23 17:58:17  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.10  2013/09/13 20:33:16  spr
 * Add calls for UI search.
 *
 * Revision 1.9  2012-07-20 22:16:10  spr
 * Use new version of gwt.
 *
 * Revision 1.8  2012-06-11 14:08:23  spr
 * Add framework search; fix bugs
 *
 * Revision 1.7  2009-09-18 01:42:37  spr
 * Updates for new gwt and support user feedback and full class.
 *
 * Revision 1.6  2009-05-12 22:29:43  spr
 * Update front end of user context.
 *
 * Revision 1.5  2008-11-12 13:52:27  spr
 * Performance and bug updates.
 *
 * Revision 1.4  2008-08-28 00:33:03  spr
 * Next version of S6.	Lots of bug fixes, some new functionality.
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

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;



abstract class SviwebTestArea implements SviwebConstants {



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

protected SviwebMain	svi_web;
protected int		tab_index;

protected FlexTable	test_box;

protected List<SviwebTestCase> test_cases;
private   Status	test_status;
private   Element	test_xml;

private  StatusCallback status_callback;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

protected SviwebTestArea(SviwebMain sm,int tab,StatusCallback scb)
{
   svi_web = sm;
   tab_index = tab;
   status_callback = scb;

   test_cases = new ArrayList<SviwebTestCase>();
   test_status = Status.UNCHECKED;
   test_xml = null;

   test_box = new FlexTable();
   FlexTable.FlexCellFormatter fmt = test_box.getFlexCellFormatter();
   HTMLTable.ColumnFormatter cfmt = test_box.getColumnFormatter();

   test_box.setCellPadding(2);
   test_box.setWidth("100%");
   test_box.setStyleName("testbox");

   cfmt.setWidth(0,"10px");
   cfmt.setWidth(1,"100*");
   cfmt.setWidth(2,"10px");
   cfmt.setWidth(3,"100*");
   cfmt.setWidth(4,"10px");

   test_box.setText(0,0,"Test Description");
   fmt.setStyleName(0,0,"testheader");
   fmt.setColSpan(0,0,4);
   test_box.setText(0,1,"Type");
   fmt.setStyleName(0,1,"testheader");
}




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

FlexTable getWidget()			{ return test_box; }

int getTabIndex()			{ return tab_index; }

Element getTestDescriptions()		{ return test_xml; }


abstract List<SviwebDeclaration> getMethods();

List<String> getVariables(SviwebTestCase fortc)
{
   List<String> vars = new ArrayList<String>();

   for (SviwebTestCase tc : test_cases) {
      if (tc.getTestType() != TestType.CALLSETCONT) {
	 vars.clear();
       }
      if (tc == fortc) break;
      String var = tc.getResultVariable();
      if (var != null && !vars.contains(var)) {
	 vars.add(var);
       }
    }

   return vars;
}




/********************************************************************************/
/*										*/
/*	Row manipluation							*/
/*										*/
/********************************************************************************/

int getTestRow(SviwebTestCase tc)	{ return test_cases.indexOf(tc); }


int clearTestRow(SviwebTestCase tc)
{
   int row = getTestRow(tc);
   if (row < 0) return row;

   if (test_box.getRowCount() > row) {
      test_box.removeCells(row,0,test_box.getCellCount(row));
    }
   else {
      test_box.insertRow(test_box.getRowCount());
    }

   return row;
}



int addTestCell(SviwebTestCase tc,Widget w,int span)
{
   int row = getTestRow(tc);
   int cel = test_box.getCellCount(row);
   test_box.setWidget(row,cel,w);
   if (span > 1) {
      FlexTable.FlexCellFormatter fmt = test_box.getFlexCellFormatter();
      fmt.setColSpan(row,cel,span);
    }

   return cel;
}



void setCell(SviwebTestCase tc,int cel,Widget w)
{
   int row = getTestRow(tc);
   test_box.setWidget(row,cel,w);
}



abstract ListBox addTestSelector(SviwebTestCase tc);



void setError(SviwebTestCase tc)
{
   int row = getTestRow(tc);
   HTMLTable.RowFormatter fmt = test_box.getRowFormatter();
   fmt.setStyleName(row,"testrowerror");
}


void clearError(SviwebTestCase tc)
{
   int row = getTestRow(tc);
   HTMLTable.RowFormatter fmt = test_box.getRowFormatter();
   fmt.setStyleName(row,"testrowok");
}



/********************************************************************************/
/*										*/
/*	Methods to handle test setup						*/
/*										*/
/********************************************************************************/

void reset()
{
   ensureEmptyTest();

   for (SviwebTestCase tc : test_cases) {
      tc.reset();
    }
}



void resetSubsequentTests(SviwebTestCase stc)
{
   int idx = test_cases.indexOf(stc);
   if (idx < 0) return;
   for (int i = idx+1; i < test_cases.size(); ++i) {
      SviwebTestCase tc = test_cases.get(i);
      if (tc.getTestType() != TestType.CALLSETCONT) break;
      tc.reset();
    }
}



protected void ensureEmptyTest()
{
   boolean needtest = false;

   int idx = test_cases.size()-1;
   if (idx < 0) needtest = true;
   else {
      SviwebTestCase tc = test_cases.get(idx);
      if (!tc.isEmpty()) needtest = true;
    }
   if (needtest) {
      SviwebTestCase tc = new SviwebTestCase(svi_web,this);
      test_cases.add(tc);
      setupTestCase(tc);
    }

   boolean needcont = false;
   for (int i = 0; i < test_cases.size(); ++i) {
      SviwebTestCase tc = test_cases.get(i);
      if (needcont) {
	 if (tc.getTestType() == TestType.CALLSETCONT) {
	    if (tc.isEmpty()) needcont = false;
	  }
	 else {
	    test_box.insertRow(i);
	    SviwebTestCase ntc = new SviwebTestCase(svi_web,this);
	    test_cases.add(i,ntc);
	    ntc.setTestType(TestType.CALLSETCONT);
	    needcont = false;
	    ++i;
	  }
       }
      if (tc.getTestType() == TestType.CALLSET && !tc.isEmpty()) needcont = true;
    }
}



protected abstract void setupTestCase(SviwebTestCase tc);



/********************************************************************************/
/*										*/
/*	Method to handle test type change					*/
/*										*/
/********************************************************************************/

protected class TestChange implements ChangeHandler {

   private SviwebTestCase test_case;

   TestChange(SviwebTestCase tc) {
      test_case = tc;
    }

   public void onChange(ChangeEvent evt) {
      ListBox lb = (ListBox) evt.getSource();
      TestType tt = TestType.get(lb.getValue(lb.getSelectedIndex()));
      test_case.setTestType(tt);
    }

}	// end of subclass TestChange




/********************************************************************************/
/*										*/
/*	Validation methods							*/
/*										*/
/********************************************************************************/

void invalidate()
{
   test_status = Status.UNCHECKED;

   for (SviwebTestCase tc : test_cases) {
      tc.invalidate();
      clearError(tc);
    }
}



Status validate()
{
   if (test_status == Status.CHECKING) return test_status;
   Status tsts = svi_web.checkContext(new ContextChange());
   if (tsts == Status.CHECKING) return Status.CHECKING;

   test_status = Status.OK;
   int ntest = 0;
   for (SviwebTestCase tc : test_cases) {
      if (!tc.isEmpty()) {
	 ++ntest;
	 Status sts = tc.getTestStatus();
	 if (sts == Status.UNCHECKED) {
	    test_status = Status.UNCHECKED;
	    break;
	  }
	 else if (sts != Status.OK) test_status = Status.BAD;
       }
    }

   if (ntest == 0) {
      test_status = Status.UNUSED;
      svi_web.addError("No tests defined");
      return test_status;
    }

   if (test_status != Status.UNCHECKED) {
      // add error messages
      return test_status;
    }

   checkTests();

   return Status.CHECKING;
}



private void checkTests()
{
   invalidate();

   Document doc = XMLParser.createDocument();
   Element root = SviwebXml.addChild(doc,"CHECK");
   root.setAttribute("WHAT","TESTS");
   addSignature(root);

   for (int i = 0; i < test_cases.size(); ++i) {
      SviwebTestCase tc = test_cases.get(i);
      if (tc.isEmpty()) continue;
      tc.setChecking();
      Element telt = SviwebXml.addChild(root,"TEST");
      telt.setAttribute("TESTID",Integer.toString(i));
      telt.setAttribute("TESTNAME",tc.getTestName());
      telt.setAttribute("TYPE",tc.getTestType().toString());
      SviwebDeclaration sd = tc.getTestMethod();
      if (sd != null) telt.setAttribute("METHOD",sd.getName());
      String thnm = tc.getThisName();
      if (thnm != null) telt.setAttribute("THIS",thnm);
      TestOp op = tc.getTestOp();
      if (op != null) telt.setAttribute("OP",op.toString());
      SviwebXml.addTextElement(telt,"INPUT",tc.getTestInput());
      SviwebXml.addTextElement(telt,"OUTPUT",tc.getTestOutput());
    }

   svi_web.addContext(root);

   setStatus(Status.CHECKING);
   svi_web.sendToServer(doc,new TestsCallback());
}



protected abstract void addSignature(Element root);




private class ContextChange implements StatusCallback {

   public void setStatus(Status sts) {
      validate();
    }

}




/********************************************************************************/
/*										*/
/*	Test result status change methods					*/
/*										*/
/********************************************************************************/

private void handleTestsBad(String msg)
{
   if (msg != null) svi_web.addError(msg);

   setStatus(Status.BAD);
}



private void handleTestsOk(Element rslt)
{
   test_xml = rslt;

   for (SviwebTestCase tc : test_cases) {
      tc.setOk();
    }

   setStatus(Status.OK);
}



private void setStatus(Status s)
{
   test_status = s;
   if (status_callback != null) status_callback.setStatus(s);
}




/********************************************************************************/
/*										*/
/*	Asynchronous checking callback						*/
/*										*/
/********************************************************************************/

private class TestsCallback implements AsyncCallback<String> {

   public void onSuccess(String rslt) {
      if (rslt == null) handleTestsBad("No response from S6 server");
      else {
	 Document r = XMLParser.parse(rslt);

	 Element tsts = SviwebXml.getXmlElement(r,"TESTS");
	 if (tsts == null) {
	    String emsg = null;
	    Element err = SviwebXml.getXmlElement(r,"PROBLEM");
	    if (err == null) err = SviwebXml.getXmlElement(r,"ERROR");
	    if (err == null) emsg = "S6 Server problem";
	    else emsg = SviwebXml.getXmlText(err);
	    handleTestsBad(emsg);
	  }
	 else {
	    NodeList nl = SviwebXml.getXmlElements(tsts,"ERROR");
	    if (nl == null || nl.getLength() == 0) {
	       handleTestsOk(tsts);
	     }
	    else {
	       for (int i = 0; i < nl.getLength(); ++i) {
		  Element terr = (Element) nl.item(i);
		  int tid = Integer.parseInt(SviwebXml.getXmlAttr(terr,"TESTID"));
		  String msg = SviwebXml.getXmlAttr(terr,"MESSAGE");
		  SviwebTestCase tc = test_cases.get(tid);
		  setError(tc);
		  tc.setError(msg);
		  svi_web.addError(msg);
		}
	       handleTestsBad(null);
	     }
	  }
       }
    }

   public void onFailure(Throwable t) {
      String emsg = t.getMessage();
      if (emsg == null) emsg = "S6 Server failure";
      handleTestsBad(emsg);
    }

}	// end of subclass TestsCallback





}	// end of class SviwebTestArea




/* end of SviwebTestArea.java */
