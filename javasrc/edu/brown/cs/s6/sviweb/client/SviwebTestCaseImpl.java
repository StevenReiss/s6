/********************************************************************************/
/*										*/
/*		SviwebTestCaseImpl.java 					*/
/*										*/
/*	Single test case element implementation 				*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/sviweb/client/SviwebTestCaseImpl.java,v 1.10 2015/09/23 17:58:17 spr Exp $ */


/*********************************************************************************
 *
 * $Log: SviwebTestCaseImpl.java,v $
 * Revision 1.10  2015/09/23 17:58:17  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.9  2014/08/29 15:16:19  spr
 * Updates for suise, testcases.
 *
 * Revision 1.8  2013/09/13 20:33:16  spr
 * Add calls for UI search.
 *
 * Revision 1.7  2012-06-11 14:08:23  spr
 * Add framework search; fix bugs
 *
 * Revision 1.6  2009-09-18 01:42:37  spr
 * Updates for new gwt and support user feedback and full class.
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

import java.util.List;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.Widget;



abstract class SviwebTestCaseImpl implements SviwebConstants, ChangeHandler {




/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

protected SviwebMain svi_web;
protected SviwebTestArea test_area;
protected SviwebTestCase test_case;

protected TextBoxBase test_args;
protected TextBoxBase test_result;
protected ListBox test_op;



/********************************************************************************/
/*										*/
/*	Static creation methods 						*/
/*										*/
/********************************************************************************/

static SviwebTestCaseImpl create(SviwebMain sm,SviwebTestArea ta,SviwebTestCase tc,TestType tt)
{
   SviwebTestCaseImpl ti = null;

   if (tt == TestType.CALL) ti = new CallTest(sm,ta,tc);
   else if (tt == TestType.USERCODE) ti = new UserCodeTest(sm,ta,tc);
   else if (tt == TestType.TESTFILE) ti = new TestFileTest(sm,ta,tc);
   else if (tt == TestType.CLASSCALL) ti = new ClassCallTest(sm,ta,tc);
   else if (tt == TestType.CALLSET) ti = new CallSetTest(sm,ta,tc,false);
   else if (tt == TestType.CALLSETCONT) ti = new CallSetTest(sm,ta,tc,true);

   return ti;
}



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

protected SviwebTestCaseImpl(SviwebMain sm,SviwebTestArea ta,SviwebTestCase tc)
{
   svi_web = sm;
   test_area = ta;
   test_case = tc;

   test_args = null;
   test_result = null;
   test_op = null;
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

abstract void setup();


abstract TestType getTestType();
abstract SviwebDeclaration getTestMethod();
abstract FocusWidget getFirstFocus();


void reset()					{ }



TestOp getTestOp()
{
   if (test_op == null) return null;
   return TestOp.get(test_op.getValue(test_op.getSelectedIndex()));
}



String getTestOutput()
{
   if (test_result == null) return null;
   return test_result.getText().trim();
}


String getTestInput()
{
   if (test_args == null) return null;
   return test_args.getText().trim();
}



boolean isEmpty()
{
   String ta = getTestInput();
   String tb = getTestOutput();
   TestOp op = getTestOp();

   if (ta != null && !ta.equals("") && !ta.equals("void")) return false;

   if (op != TestOp.IGNORE) {
      if (tb != null && !tb.equals("")) return false;
    }

   return true;
}



String getThisName()
{
   return null;
}



/********************************************************************************/
/*										*/
/*	Helper methods for creating widgets					*/
/*										*/
/********************************************************************************/

protected TextBox createTextBox(int len)
{
   TextBox tb = new TextBox();

   tb.setVisibleLength(len);
   tb.setMaxLength(256);
   tb.addChangeHandler(this);
   tb.setTabIndex(test_area.getTabIndex());

   return tb;
}



protected TextArea createTextArea(int len,int rows)
{
   TextArea ta = new TextArea();

   ta.setCharacterWidth(len);
   ta.setVisibleLines(rows);
   ta.addChangeHandler(this);
   ta.setTabIndex(test_area.getTabIndex());

   return ta;
}



protected ListBox createSelection(String dflt,String [][] opts)
{
   ListBox lb = SviwebWidget.createSelection(dflt,opts);
   lb.addChangeHandler(this);
   lb.setTabIndex(test_area.getTabIndex());

   return lb;
}



protected FileUpload createFileRegion(int len)
{
   FileUpload fl = new FileUpload();
   fl.setWidth(Integer.toString(len) + "en");

   return fl;
}



/********************************************************************************/
/*										*/
/*	Methods to set up the layout						*/
/*										*/
/********************************************************************************/

protected int addCell(Widget w)
{
   return test_case.addCell(w,1);
}


protected int addCell(Widget w,int span)
{
   return test_case.addCell(w,span);
}


protected void addTestSelector()
{
   test_case.addTestSelector();
}



protected void setCell(int idx,Widget w)
{
   test_case.setCell(idx,w);
}




/********************************************************************************/
/*										*/
/*	Callbacks to handle updates to fields					*/
/*										*/
/********************************************************************************/

public void onChange(ChangeEvent evt)
{
   test_case.invalidate();

   if (!isEmpty()) test_case.ensureEmptyTest();

   if ((evt.getSource() == test_op || evt.getSource() == test_result) &&
	  (getTestType() == TestType.CALLSET || getTestType() == TestType.CALLSETCONT)) {
      test_case.resetSubsequentTests();
    }
}




/********************************************************************************/
/*										*/
/*	CALL test implementation						*/
/*										*/
/********************************************************************************/

private static String [][]  call_ops = {
   { "EQL", "==" }, { "NEQ", "!=" }, { "THROW", "throws"}, { "SAME", "===" }, { "DIFF", "!===" },
	{ "SHOW", "< ?? >" }
};


private static String [][]  void_call_ops = {
   { "THROW", "throws"}, { "IGNORE", "???" }
};


private static class CallTest extends SviwebTestCaseImpl {

   private SviwebDeclaration method_decl;
   private int op_index;
   private boolean no_return;
   private boolean no_args;

   CallTest(SviwebMain sm,SviwebTestArea ta,SviwebTestCase tc) {
      super(sm,ta,tc);
      method_decl = ta.getMethods().get(0);
    }

   void setup() {
      HorizontalPanel hp = new HorizontalPanel();
      hp.add(new Label("("));
      test_args = createTextBox(30);
      hp.add(test_args);
      hp.add(new Label(")"));
      addCell(hp,2);
      no_args = false;
      test_op = createSelection("EQL",call_ops);
      no_return = false;
      op_index = addCell(test_op);
      test_result = createTextBox(20);
      addCell(test_result);
      addTestSelector();
      reset();
    }

   TestType getTestType()			{ return TestType.CALL; }

   SviwebDeclaration getTestMethod()		{ return method_decl; }

   FocusWidget getFirstFocus() {
      if (!method_decl.hasNoArguments()) return test_args;
      if (!method_decl.hasNoReturn()) return test_result;
      return null;
    }

   void reset() {
      if (no_args != method_decl.hasNoArguments()) {
	 no_args = method_decl.hasNoArguments();
	 test_args.setReadOnly(no_args);
	 if (no_args) test_args.setText("void");
       }
      if (no_return != method_decl.hasNoReturn()) {
	 no_return = method_decl.hasNoReturn();
	 if (no_return) test_op = createSelection("IGNORE", void_call_ops);
	 else test_op = createSelection("EQL",call_ops);
	 setCell(op_index,test_op);
	 test_result.setReadOnly(no_return);
	 if (no_return) test_result.setText("void");
       }
    }

}	// end of subclass CallTest



/********************************************************************************/
/*										*/
/*	USER CODE test implementation						*/
/*										*/
/********************************************************************************/

private static class UserCodeTest extends SviwebTestCaseImpl {

   UserCodeTest(SviwebMain sm,SviwebTestArea ta,SviwebTestCase tc) {
      super(sm,ta,tc);
    }

   void setup() {
      test_result = createTextArea(72,4);
      addCell(test_result,4);
      addTestSelector();
    }

   TestType getTestType()			{ return TestType.USERCODE; }

   SviwebDeclaration getTestMethod()		{ return null; }

   FocusWidget getFirstFocus()			{ return test_result; }

}	// end of subclass UserCodeTest




/********************************************************************************/
/*										*/
/*	TEST FILE test implementation						*/
/*										*/
/********************************************************************************/

private static class TestFileTest extends SviwebTestCaseImpl {

   private FileUpload file_box;

   TestFileTest(SviwebMain sm,SviwebTestArea ta,SviwebTestCase tc) {
      super(sm,ta,tc);
    }

   void setup() {
      file_box = createFileRegion(48);
      addCell(file_box,4);
      addTestSelector();
    }

   TestType getTestType()			{ return TestType.TESTFILE; }

   SviwebDeclaration getTestMethod()		{ return null; }

   FocusWidget getFirstFocus()			{ return null; }

}	// end of subclass TestFileTest




/********************************************************************************/
/*										*/
/*	General class call test methods 					*/
/*										*/
/********************************************************************************/

private static abstract class GenericClassCall extends SviwebTestCaseImpl {

   private ListBox method_selector;

   protected GenericClassCall(SviwebMain sm,SviwebTestArea ta,SviwebTestCase tc) {
      super(sm,ta,tc);
    }

   protected ListBox getMethodSelector() {
      if (method_selector == null) {
	 method_selector = createSelection(null,null);
	 reset();
       }
      return method_selector;
    }

   SviwebDeclaration getTestMethod() {
      int sel = method_selector.getSelectedIndex();
      if (sel < 0) return null;
      String v0 = method_selector.getValue(sel);
      int idx1 = v0.indexOf(' ');
      if (idx1 >= 0) v0 = v0.substring(0,idx1);
      int idx = Integer.parseInt(v0);
      if (idx < 0) return null;
      return test_area.getMethods().get(idx);
    }

   String getThisName() {
      int sel = method_selector.getSelectedIndex();
      if (sel < 0) return null;
      String v0 = method_selector.getValue(sel);
      int idx1 = v0.indexOf(' ');
      if (idx1 < 0) return null;
      v0 = v0.substring(idx1+1).trim();
      return v0;
    }

   void reset() {
      int idx = method_selector.getSelectedIndex();
      String selv = null;
      if (idx >= 0) selv = method_selector.getValue(idx);

      method_selector.clear();
      method_selector.addItem("--NONE--","-1");
      List<SviwebDeclaration> mthds = test_area.getMethods();
      List<String> vars = test_area.getVariables(test_case);
      int sidx = 0;
      for (int i = 0; i < mthds.size(); ++i) {
	 SviwebDeclaration sd = mthds.get(i);
	 if (sd.getStatus() != Status.OK) continue;
	 String mnm = sd.getName();
	 if (sd.isStatic() || mnm.equals("<init>")) {
	    String val = Integer.toString(i);
	    if (val.equals(selv)) sidx = method_selector.getItemCount();
	    method_selector.addItem(sd.getName(),val);
	  }
	 else {
	    for (int j = 0; j < vars.size(); ++j) {
	       String lbl = vars.get(j) + "." + mnm;
	       String val = Integer.toString(i) + " " + vars.get(j);
	       if (val.equals(selv)) sidx = method_selector.getItemCount();
	       method_selector.addItem(lbl,val);
	     }
	  }
       }
      for (String v : vars) {
	 method_selector.addItem(v,"-2 " + v);
       }

      method_selector.setSelectedIndex(sidx);
    }

   FocusWidget getFirstFocus()			{ return method_selector; }

}	// end of abstract subclass GenericClassCall




/********************************************************************************/
/*										*/
/*	CLASS CALL test implementation						*/
/*										*/
/********************************************************************************/

private static class ClassCallTest extends GenericClassCall {

   ClassCallTest(SviwebMain sm,SviwebTestArea ta,SviwebTestCase tc) {
      super(sm,ta,tc);
    }

   void setup() {
      addCell(getMethodSelector());

      HorizontalPanel hp = new HorizontalPanel();
      hp.add(new Label("("));
      test_args = createTextBox(30);
      hp.add(test_args);
      hp.add(new Label(")"));
      addCell(hp);

      test_op = createSelection("EQL",call_ops);
      addCell(test_op);

      test_result = createTextBox(20);
      addCell(test_result);

      addTestSelector();
    }

   TestType getTestType()			{ return TestType.CALL; }

}	// end of subclass CallTest




/********************************************************************************/
/*										*/
/*	CALLSET test implementation						*/
/*										*/
/********************************************************************************/

private static String [][]  callset_ops = { {"SAVE", "==>"},
					    { "EQL", "==" },
					    { "NEQ", "!=" },
					    { "THROW", "throws" },
					    { "IGNORE", "???" },
					    { "SAME", "===" },
					    { "DIFF", "!===" },
					    { "SHOW", "< ?? >" }
};


private static String [][]  init_callset_ops = { {"SAVE", "==>"},
						 { "THROW", "throws" },
						 { "IGNORE", "???" },
						 { "SHOW", "< ?? >" }
};


private static String [][]  void_callset_ops = { { "THROW", "throws" },
						 { "IGNORE", "???" }
};


private static class CallSetTest extends GenericClassCall {

   boolean is_continue;
   boolean no_args;
   boolean no_return;
   boolean is_constructor;
   int op_index;

   CallSetTest(SviwebMain sm,SviwebTestArea ta,SviwebTestCase tc,boolean cont) {
      super(sm,ta,tc);
      is_continue = cont;
    }

   void setup() {
      addCell(getMethodSelector());

      HorizontalPanel hp = new HorizontalPanel();
      hp.add(new Label("("));
      test_args = createTextBox(30);
      hp.add(test_args);
      hp.add(new Label(")"));
      addCell(hp);

      no_args = false;
      no_return = false;

      test_op = createSelection((is_continue ? "EQL" : "SAVE"),callset_ops);
      op_index = addCell(test_op);

      test_result = createTextBox(20);
      addCell(test_result);

      addTestSelector();
    }

   TestType getTestType() {
      if (is_continue) return TestType.CALLSETCONT;
      return TestType.CALLSET;
    }

   public void onChange(ChangeEvent evt) {
      super.onChange(evt);

      if (evt.getSource() == getMethodSelector()) {
	 fixForMethod();
       }
    }

   private void fixForMethod() {
      SviwebDeclaration sd = getTestMethod();
      boolean narg = (sd == null || sd.hasNoArguments());

      if (no_args != narg) {
	 no_args = narg;
	 test_args.setReadOnly(no_args);
	 if (no_args) test_args.setText("void");
       }

      if (sd != null && sd.getName() != null && sd.getName().equals("<init>")) {
	 if (!is_constructor) {
	    is_constructor = true;
	    test_op = createSelection("SAVE",init_callset_ops);
	    setCell(op_index,test_op);
	  }
       }
      else if (is_constructor && sd != null) no_return = !sd.hasNoReturn();   // force update
      else if (is_constructor) no_return = true;

      boolean nret = (sd != null && sd.hasNoReturn());

      if (no_return != nret) {
	 no_return = nret;
	 if (no_return) test_op = createSelection("IGNORE",void_callset_ops);
	 else test_op = createSelection("EQL",call_ops);
	 setCell(op_index,test_op);
	 test_result.setReadOnly(no_return);
	 if (no_return) test_result.setText("void");
       }
    }

}	// end of subclass CallSetTest



}	// end of class SviwebTestCaseImpl




/* end of SviwebTestCaseImpl.java */

