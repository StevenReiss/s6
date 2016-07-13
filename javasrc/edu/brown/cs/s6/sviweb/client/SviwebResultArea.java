/********************************************************************************/
/*										*/
/*		SviwebResultArea.java						*/
/*										*/
/*	Area to hold results of a search					*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/sviweb/client/SviwebResultArea.java,v 1.8 2015/09/23 17:58:17 spr Exp $ */


/*********************************************************************************
 *
 * $Log: SviwebResultArea.java,v $
 * Revision 1.8  2015/09/23 17:58:17  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.7  2013/09/13 20:33:16  spr
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;



class SviwebResultArea implements SviwebConstants, ChangeHandler, ClickHandler {


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private SviwebMain	svi_web;
private StatusCallback	status_callback;

private VerticalPanel	result_region;
private HTML		result_head;
private SimplePanel	error_panel;
private Grid		result_display;
private List<String>	source_set;
private HorizontalPanel result_query;

private ListBox 	order_by;
private ListBox 	format_by;

private Timer		result_timer;
private int		result_wait;

private List<Element>	result_set;

private int		tab_index;
private int		result_counter;

private VerticalPanel	user_display;
private FlexTable	user_table;
private Button		user_button;
private UserInput	user_input;

private boolean 	doing_format;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

SviwebResultArea(SviwebMain sm,StatusCallback cb)
{
   svi_web = sm;
   status_callback = cb;
   source_set = new ArrayList<String>();
   int tab = RESULT_TAB_START;

   result_timer = null;
   result_wait = 0;
   result_set = new ArrayList<Element>();
   result_counter = 0;
   doing_format = false;

   result_region = new VerticalPanel();

   result_head = new HTML("Results:");
   result_head.setStyleName("resulthead");
   result_region.add(result_head);

   error_panel = new SimplePanel();
   error_panel.setStyleName("resulterror");
   result_region.add(error_panel);

   result_query = new HorizontalPanel();
   result_region.add(result_query);
   Label l0 = new Label("");
   l0.setStyleName("rsltoptindent");
   result_query.add(l0);
   Label l1 = new Label("Order By:\u00a0");
   l1.setStyleName("resultoptions");
   result_query.add(l1);

   order_by = new ListBox();
   order_by.addItem("Code Size","SIZE");
   order_by.addItem("Code Complexity","COMPLEXITY");
   order_by.addItem("Efficiency","COST");
   order_by.setItemSelected(0,true);
   order_by.setTabIndex(tab++);
   order_by.addChangeHandler(this);
   result_query.add(order_by);

   Label l2 = new Label("\u00a0\u00a0Format Using:\u00a0");
   l2.setStyleName("resultoptions");
   result_query.add(l2);

   format_by = new ListBox();
   format_by.addItem("None","NONE");
   format_by.addItem("Sun J2SE","SUN");
   format_by.addItem("Eclipse JDT","ECLIPSE");
   format_by.addItem("Brown","BROWN");
   format_by.addItem("Personal","SELF");
   format_by.addChangeHandler(this);
   result_query.add(format_by);

   user_display = new VerticalPanel();
   user_display.setVisible(false);
   user_table = new FlexTable();
   user_display.add(user_table);
   user_button = new Button("CONTINUE SEARCH",this);
   user_button.setStyleName("findit");
   user_display.add(user_button);

   result_region.add(user_display);

   result_display = new Grid(0,1);
   // DOM.setElementAttribute(result_display.getElement(),"rules","rows");
   // DOM.setElementAttribute(result_display.getElement(),"frame","hsides");
   result_display.setCellPadding(4);
   result_display.setCellSpacing(4);
   result_display.setStyleName("resulttable");

   result_region.add(result_display);

   tab_index = tab;

   user_input = null;
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

Widget getResultWidget()			{ return result_region; }

String getFormatMethod()
{
   return format_by.getValue(format_by.getSelectedIndex());
}



/********************************************************************************/
/*										*/
/*	Status methods								*/
/*										*/
/********************************************************************************/

private void setStatus(Status sts)
{
   if (status_callback != null) status_callback.setStatus(sts);
}




/********************************************************************************/
/*										*/
/*	Handle searching and getting results					*/
/*										*/
/********************************************************************************/

void startSearch(Document request)
{
   doing_format = false;

   clearResults();
   clearResultError();
   startResultWait();

   svi_web.sendToServer(request,new ResultCallback());
}



private void handleResultBad(String msg)
{
   stopResultWait();
   result_head.setStyleName("resultheadbad");
   setResultError(msg);
   setStatus(Status.BAD);
}



private void handleResultOk(Element rslts)
{
   stopResultWait();

   NodeList nl = SviwebXml.getXmlElements(rslts,"SOURCE");
   for (int i = 0; i < nl.getLength(); ++i) {
      Element selt = (Element) nl.item(i);
      source_set.add(SviwebXml.getXmlText(selt));
    }

   nl = SviwebXml.getXmlElements(rslts,"SOLUTION");
   for (int i = 0; i < nl.getLength(); ++i) {
      Element selt = (Element) nl.item(i);
      String id = Integer.toString(++result_counter);
      selt.setAttribute("ID",id);
      result_set.add(selt);
    }

   if (nl.getLength() == 0) {
      setResultError("No results found.");
      setStatus(Status.BAD);
    }
   else {
      result_head.setStyleName("resultheadgood");
      setStatus(Status.OK);
      displayResults();
    }
}



/********************************************************************************/
/*										*/
/*	Result error handling							*/
/*										*/
/********************************************************************************/

private void clearResultError()
{
   error_panel.setWidget(null);
}



private void setResultError(String msg)
{
   error_panel.setWidget(new Label(msg));
}



/********************************************************************************/
/*										*/
/*	Result animation							*/
/*										*/
/********************************************************************************/

private void startResultWait()
{
   ++result_wait;
   if (result_timer != null) return;
   result_timer = new ResultTimer();
   result_timer.scheduleRepeating(250);
}



private void stopResultWait()
{
   --result_wait;
   if (result_wait > 0) return;
   result_wait = 0;

   if (result_timer != null) {
      result_timer.cancel();
      result_timer = null;
    }

   result_head.setHTML("Results:");
}



private class ResultTimer extends Timer {

   private int animate_count;

   ResultTimer() {
      animate_count = 0;
    }

   public void run() {
      String x = "Results:&nbsp;";
      for (int i = 0; i < 32; ++i) {
	 x += "<span class='result";
	 if (doing_format) x += "format";
	 else x += "anim";
	 if (i == animate_count) x += "high";
	 else x += "low";
	 x += "'>.&nbsp;</span>";
       }
      result_head.setHTML(x);
      animate_count = (animate_count + 1) % 32;
    }

}	// end of subclass ResultTimer




/********************************************************************************/
/*										*/
/*	Methods for displaying the results					*/
/*										*/
/********************************************************************************/

private void displayResults()
{
   result_display.resizeRows(result_set.size());

   String on = order_by.getValue(order_by.getSelectedIndex());
   Collections.sort(result_set,new ResultComparator(on));

   for (int i = 0; i < result_set.size(); ++i) {
      Element r = result_set.get(i);
      Widget w = getResultDisplay(r);
      result_display.setWidget(i,0,w);
    }
}



private static class ResultComparator implements Comparator<Element> {

   private String attr_name;

   ResultComparator(String on) {
      int type = ResultSort.SIZE;
      if (on != null) {
	 if (on.equals("SIZE")) type = ResultSort.SIZE;
	 else if (on.equals("COMPLEXITY")) type = ResultSort.COMPLEXITY;
	 else if (on.equals("COST")) type = ResultSort.COST;
       }
      switch (type) {
	 default :
	 case ResultSort.SIZE :
	    attr_name = "LINES";
	    break;
	 case ResultSort.COMPLEXITY :
	    attr_name = "CODE";
	    break;
	 case ResultSort.COST :
	    attr_name = "TESTTIME";
	    break;
       }
    }

   public int compare(Element e1,Element e2) {
      Element c1 = SviwebXml.getXmlElement(e1,"COMPLEXITY");
      Element c2 = SviwebXml.getXmlElement(e2,"COMPLEXITY");
      double v1 = Double.parseDouble(SviwebXml.getXmlAttr(c1,attr_name));
      double v2 = Double.parseDouble(SviwebXml.getXmlAttr(c2,attr_name));
      if (v1 < v2) return -1;
      if (v1 > v2) return 1;
      return 0;
    }

}	// end of subclass ResultCompator




/********************************************************************************/
/*										*/
/*	Methods for setting up a result display 				*/
/*										*/
/********************************************************************************/

private Widget getResultDisplay(Element r)
{
   VerticalPanel vp = new VerticalPanel();
   vp.setStyleName("rsltpanel");

   HTML h0 = new HTML("<hr class='resultitemsep'>");
   vp.add(h0);

   HorizontalPanel hp = new HorizontalPanel();
   vp.add(hp);
   hp.setStyleName("rslthead");
   Label l1 = new Label("Source:\u00a0");
   l1.setStyleName("rsltsrclbl");
   hp.add(l1);
   Label l2 = new Label(SviwebXml.getTextElement(r,"NAME"));
   l2.setStyleName("rsltsrcname");
   hp.add(l2);

   String s = SviwebXml.getTextElement(r,"LICENSE");
   if (s != null && s.length() > 0) {
      Label l3 = new Label("");
      l3.setStyleName("rsltlicspace");
      hp.add(l3);
      Button b = new Button("License",new LicenseShow(r,s));
      b.setStyleName("rsltlicense");
      b.setTabIndex(tab_index);
      hp.setHorizontalAlignment(HorizontalPanel.ALIGN_RIGHT);
      hp.add(b);
    }

   String code = SviwebXml.getTextElement(r,"CODE");
   code = code.replace("<","&lt;");
   HTML h1 = new HTML("<hr><pre class='resultcode'>" + code + "</pre>");
   vp.add(h1);

   return vp;
}



private void displayLicense(String uid,String lic)
{
   String html = "<html>\n";
   html += "<head>\n";
   html += "<title>S6 License " + uid + "</title>";
   html += "<link rel='stylesheet' type='text/css' href='s6.css' />\n";
   html += "</head>\n";
   html += "<body>\n";
   html += "<pre class='licensetext'>\n";
   html += lic;
   html += "</pre>\n";
   html += "</body>\n";
   html += "</html>\n";

   SviwebMain.newWindow(html);
}


private class LicenseShow implements ClickHandler {

   private String license_uid;

   LicenseShow(Element r,String uid) {
      license_uid = uid;
    }

   public void onClick(ClickEvent evt) {
      Document doc = XMLParser.createDocument();
      Element root = SviwebXml.addChild(doc,"CHECK");
      root.setAttribute("WHAT","LICENSE");
      SviwebXml.addTextElement(root,"UID",license_uid);
      svi_web.sendToServer(doc,new LicenseCallback(license_uid));
    }

}


private class LicenseCallback implements AsyncCallback<String> {

   private String license_uid;

   LicenseCallback(String uid) {
      license_uid = uid;
    }

   public void onSuccess(String rslt) {
      if (rslt != null) {
	 Document r = XMLParser.parse(rslt);
	 Element lic = SviwebXml.getXmlElement(r,"LICENSE");
	 if (lic != null) {
	    String v = SviwebXml.getTextElement(lic,"TEXT");
	    displayLicense(license_uid,v);
	  }
       }
    }

   public void onFailure(Throwable t) { }

}	// end of subclass LicenseCallback




/********************************************************************************/
/*										*/
/*	Methods for managing results						*/
/*										*/
/********************************************************************************/

private void clearResults()
{
   result_set.clear();
   result_display.resizeRows(0);
   result_head.setStyleName("resulthead");
}



/********************************************************************************/
/*										*/
/*	Callback for result options						*/
/*										*/
/********************************************************************************/

public void onChange(ChangeEvent evt)
{
   if (evt.getSource() == order_by) {
      displayResults();
    }
   else if (evt.getSource() == format_by) {
      reformatResults();
    }
}




/********************************************************************************/
/*										*/
/*	Methods for reformatting results					*/
/*										*/
/********************************************************************************/

private void reformatResults()
{
   String type = format_by.getValue(format_by.getSelectedIndex());

   Document doc = XMLParser.createDocument();
   Element root = SviwebXml.addChild(doc,"FORMAT");
   root.setAttribute("WHAT",svi_web.getCurrentSearch().getSearchType());
   root.setAttribute("TYPE",type);
   for (int i = 0; i < result_set.size(); ++i) {
      Element r = result_set.get(i);
      Element e = SviwebXml.addChild(root,"ITEM");
      e.setAttribute("ID",r.getAttribute("ID"));
      SviwebXml.addText(e,SviwebXml.getTextElement(r,"CODE"));
    }

   doing_format = true;

   svi_web.sendToServer(doc,new FormatCallback());

   startResultWait();
}




private void handleFormatOk(Element rslt)
{
   if (doing_format) stopResultWait();
   doing_format = false;

   NodeList nl = SviwebXml.getXmlElements(rslt,"ITEM");

   for (int i = 0; i < nl.getLength(); ++i) {
      Element selt = (Element) nl.item(i);
      String id = selt.getAttribute("ID");
      String code = SviwebXml.getXmlText(selt);

      for (int j = 0; j < result_set.size(); ++j) {
	 Element r0 = result_set.get(j);
	 if (r0.getAttribute("ID").equals(id)) {
	    Element ce = SviwebXml.getXmlElement(r0,"CODE");
	    if (ce != null) r0.removeChild(ce);
	    SviwebXml.addTextElement(r0,"CODE",code);
	  }
       }
    }

   displayResults();
}



private void handleFormatBad(String msg)
{
   if (doing_format) stopResultWait();
   doing_format = false;

   GWT.log("S6: FORMAT PROBLEM: " + msg,null);
}



/********************************************************************************/
/*										*/
/*	Methods to handle user input requests					*/
/*										*/
/********************************************************************************/

private void handleUserInput(Element users)
{
   user_table.clear();
   while (user_table.getRowCount() > 0) user_table.removeRow(0);

   user_table.setStyleName("usertable");
   FlexTable.FlexCellFormatter fmt = user_table.getFlexCellFormatter();
   HTMLTable.RowFormatter rfmt = user_table.getRowFormatter();

   user_input = new UserInput(SviwebXml.getXmlAttr(users,"UID"));

   NodeList nl = SviwebXml.getXmlElements(users,"TESTCASE");
   for (int i = 0; i < nl.getLength(); ++i) {
      Element tc = (Element) nl.item(i);
      int row = user_table.getRowCount();
      // TODO: Replace with a real name or description of the test case
      String name = SviwebXml.getXmlAttr(tc,"NAME");
      user_table.setHTML(row,0,"TestCase:&nbsp;" + name);
      fmt.setColSpan(row,0,3);
      ++row;
      user_table.setText(row,0," ");
      UserTest ut = new UserTest(name);
      user_input.addTest(ut);

      NodeList cnl = SviwebXml.getXmlElements(tc,"USERCASE");
      for (int j = 0; j < cnl.getLength(); ++j) {
	 Element uc = (Element) cnl.item(j);
	 String ids = SviwebXml.getXmlAttr(uc,"IDS");
	 String typ = SviwebXml.getXmlAttr(uc,"TYPE");
         String data = SviwebXml.getTextElement(uc,"VALUE");

	 ListBox lb = new ListBox();
	 lb.addItem("Reject");
	 lb.addItem("Accept");
	 lb.setVisibleItemCount(1);
	 user_table.setWidget(row,1,lb);
	 lb.setStyleName("usertablecell");
	 UserCase ucas	= new UserCase(ids,lb);
	 ut.addCase(ucas);

	 Widget w = createUserFeedback(typ,data);
	 if (w != null) {
	    user_table.setWidget(row,2,w);
	    w.setStyleName("usertablecell");
	  }
	 rfmt.setStyleName(row,"usertablerow");

	 ++row;
       }
    }

   stopResultWait();
   user_display.setVisible(true);
   result_query.setVisible(false);
}



Widget createUserFeedback(String typ,String value)
{
   Widget rslt = null;

   if (typ.equals("IMAGE")) {
      rslt = new HTML(value);
    }
   else {
      rslt = new Label(value);
    }

   return rslt;
}



public void onClick(ClickEvent evt)
{
   if (evt.getSource() == user_button) {
      result_query.setVisible(true);
      user_display.setVisible(false);
      Document doc = user_input.generate();
      startSearch(doc);
      user_input = null;
    }
}



private static class UserInput {

   private String input_id;
   private List<UserTest> user_tests;

   UserInput(String id) {
      input_id = id;
      user_tests = new ArrayList<UserTest>();
    }

   void addTest(UserTest ut) {
      user_tests.add(ut);
    }

   Document generate() {
      Document doc = XMLParser.createDocument();
      Element root = SviwebXml.addChild(doc,"USERREPLY");
      root.setAttribute("UID",input_id);
      for (UserTest ut : user_tests) ut.generate(root);
      return doc;
    }

}	// end of innerclass UserInput




private static class UserTest {

   private String test_name;
   private List<UserCase> test_cases;

   UserTest(String nm) {
      test_name = nm;
      test_cases = new ArrayList<UserCase>();
    }

   void addCase(UserCase uc) {
      test_cases.add(uc);
    }

   void generate(Element root) {
      Element ut = SviwebXml.addChild(root,"TESTCASE");
      ut.setAttribute("NAME",test_name);
      for (UserCase uc : test_cases) uc.generate(ut);
    }

}	// end of innerclass UserTest



private static class UserCase {

   private String id_list;
   private ListBox user_choice;

   UserCase(String ids,ListBox uc) {
      id_list = ids;
      user_choice = uc;
    }

   void generate(Element ut) {
      Element uc = SviwebXml.addChild(ut,"USERCASE");
      uc.setAttribute("IDS",id_list);
      int idx = user_choice.getSelectedIndex();
      if (idx == 0) uc.setAttribute("STATE","FAIL");
      else uc.setAttribute("STATE","PASS");
    }

}	// end of innerclass UserCase




/********************************************************************************/
/*										*/
/*	Callback for test results						*/
/*										*/
/********************************************************************************/

private class ResultCallback implements AsyncCallback<String> {

   public void onSuccess(String rslt) {
      if (rslt == null) handleResultBad("No response from S6 server");
      else {
	 Document r = XMLParser.parse(rslt);
	 Element users = SviwebXml.getXmlElement(r,"USERINPUT");
	 if (users != null) {
	    handleUserInput(users);
	    return;
	  }

	 Element solns = SviwebXml.getXmlElement(r,"SOLUTIONS");
	 if (solns == null) {
	    String emsg = null;
	    Element err = SviwebXml.getXmlElement(r,"PROBLEM");
	    if (err == null) err = SviwebXml.getXmlElement(r,"ERROR");
	    if (err == null) emsg = "S6 Server problem";
	    else emsg = SviwebXml.getXmlText(err);
	    handleResultBad(emsg);
	  }
	 else {
	    handleResultOk(solns);
	  }
       }
    }

   public void onFailure(Throwable t) {
      handleResultBad(t.getMessage());
    }

}	// end of subclass ResultCallback



/********************************************************************************/
/*										*/
/*	Callback for reformatting						*/
/*										*/
/********************************************************************************/

private class FormatCallback implements AsyncCallback<String> {

   public void onSuccess(String rslt) {
      if (rslt == null) handleFormatBad("No response from S6 server");
      else {
	 Document r = XMLParser.parse(rslt);
	 Element erslt = SviwebXml.getXmlElement(r,"RESULT");
	 if (erslt == null) {
	    String emsg = null;
	    Element err = SviwebXml.getXmlElement(r,"PROBLEM");
	    if (err == null) err = SviwebXml.getXmlElement(r,"ERROR");
	    if (err == null) emsg = "S6 Server problem";
	    else emsg = SviwebXml.getXmlText(err);
	    handleFormatBad(emsg);
	  }
	 else {
	    handleFormatOk(erslt);
	  }
       }
    }

   public void onFailure(Throwable t) {
      handleFormatBad(t.getMessage());
    }

}	// end of sublcass FormatCallback



}	// end of class SviwebResultArea





/* end of SviwebResultArea.java */
