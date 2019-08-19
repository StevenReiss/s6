/********************************************************************************/
/*										*/
/*		SviwebMain.java 						*/
/*										*/
/*	Main program for S6 search engine web front end 			*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/sviweb/client/SviwebMain.java,v 1.15 2016/11/02 19:00:29 spr Exp $ */


/*********************************************************************************
 *
 * $Log: SviwebMain.java,v $
 * Revision 1.15  2016/11/02 19:00:29  spr
 * Update keysearch for modified github.
 *
 * Revision 1.14  2016/07/18 23:06:04  spr
 * Update available search engines.
 *
 * Revision 1.13  2015/09/23 17:58:16  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.12  2014/08/29 15:16:18  spr
 * Updates for suise, testcases.
 *
 * Revision 1.11  2013/09/13 20:33:15  spr
 * Add calls for UI search.
 *
 * Revision 1.10  2013-05-09 12:26:27  spr
 * Minor changes to start ui fixups.
 *
 * Revision 1.9  2012-07-20 22:16:10  spr
 * Use new version of gwt.
 *
 * Revision 1.8  2012-06-11 14:08:22  spr
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

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;



public class SviwebMain implements SviwebConstants, EntryPoint, ChangeHandler, ClickHandler {




/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private ListBox 	look_for;
private Button		find_button;
private SviwebResultArea result_area;

private ListBox 	look_in;
private Label		error_box;
private String		error_message;

private HTML		keyword_label;
private TextBox 	keyword_box;
private Status		search_status;
private boolean 	restart_search;

private Label		remote_label;
private ListBox 	remote_type;

private SviwebSearch current_search;
private SviwebMethodSearch method_search;
private SviwebClassSearch class_search;
private SviwebUISearch	  ui_search;

private SviwebServiceAsync engine_service;




/********************************************************************************/
/*										*/
/*	Main entry point							*/
/*										*/
/********************************************************************************/

public void onModuleLoad()
{
   VerticalPanel vp = new VerticalPanel();
   vp.setSpacing(10);

   RootPanel.get().add(vp);
   error_message = null;
   search_status = Status.UNCHECKED;

   vp.add(createTopRegion());

   method_search = new SviwebMethodSearch(this,new SearchChange());
   class_search = new SviwebClassSearch(this,new SearchChange());
   ui_search = new SviwebUISearch(this,new SearchChange());
   vp.add(method_search.getPanel());
   vp.add(class_search.getPanel());
   vp.add(ui_search.getPanel());
   class_search.getPanel().setVisible(false);
   ui_search.getPanel().setVisible(false);
   current_search = method_search;

   find_button = new Button("Find it!",this);
   find_button.setStyleName("findit");
   find_button.setTabIndex(FIND_TAB_START);
   vp.add(find_button);
   vp.setCellHorizontalAlignment(find_button,HasHorizontalAlignment.ALIGN_CENTER);

   vp.add(new HTML("<hr class='resultsep' />"));

   result_area = new SviwebResultArea(this,new ResultChange());
   vp.add(result_area.getResultWidget());

   engine_service = (SviwebServiceAsync) GWT.create(SviwebService.class);
   ServiceDefTarget endpoint = (ServiceDefTarget) engine_service;
   String mrurl = GWT.getModuleBaseURL() + "sviweb";
   endpoint.setServiceEntryPoint(mrurl);
}





/********************************************************************************/
/*										*/
/*	Methods to create the top region					*/
/*										*/
/********************************************************************************/

private Panel createTopRegion()
{
   int tab = TOP_TAB_INDEX;

   FlexTable ft = new FlexTable();
   FlexTable.FlexCellFormatter fmt = ft.getFlexCellFormatter();

   ft.setCellPadding(0);
   ft.setCellSpacing(0);
   ft.setStyleName("toptable");
   ft.getColumnFormatter().setWidth(0,"10px");
   ft.getColumnFormatter().setWidth(1,"10px");
   ft.getColumnFormatter().setWidth(2,"10px");
   ft.getColumnFormatter().setWidth(3,"1*");
   ft.getColumnFormatter().setWidth(4,"5px");
   ft.getColumnFormatter().setWidth(5,"0px");

   ft.setHTML(0,0,"<div id='Logo' class='logo'>S<span class='logosup'>6</span></div>");
   fmt.setRowSpan(0,0,4);

   fmt.setColSpan(0,1,4);
   fmt.setStyleName(0,1,"logotopbar");

   // ft.setText(1,0,"");
   // fmt.setRowSpan(1,0,3);
   // fmt.setWidth(1,0,"5px");
   // fmt.setStyleName(1,0,"logoleftbar");
   ft.setHTML(1,0,"&nbsp;&nbsp;");
   fmt.setRowSpan(1,0,3);
   fmt.setStyleName(1,0,"logoleftspace");

   Label lfl = new Label("Look for: ",false);
   lfl.setStyleName("lookfor");
   ft.setWidget(1,1,lfl);

   HorizontalPanel hp = new HorizontalPanel();
   ft.setWidget(1,2,hp);

   look_for = new ListBox();
   look_for.addItem("METHOD");
   look_for.setItemSelected(0,true);
   look_for.addItem("CLASS");
   look_for.addItem("FULL CLASS");
   look_for.addItem("UI");
   look_for.addChangeHandler(this);
   look_for.setTabIndex(tab++);
   hp.add(look_for);

   lfl = new Label("\u00a0 In \u00a0");
   lfl.setStyleName("lookfor");
   hp.add(lfl);

   look_in = new ListBox();
   // look_in.addItem("Local","L");
   look_in.addItem("Remote","R");
   // look_in.addItem("Both","RL");
   look_in.setItemSelected(0,true);
   look_in.addChangeHandler(this);
   look_in.setTabIndex(tab++);
   hp.add(look_in);
   lfl = new Label("\u00a0 Archives ");
   lfl.setStyleName("lookin");
   hp.add(lfl);

   remote_label = new Label("\u00a0 Using \u00a0");
   remote_label.setStyleName("lookfor");
   remote_label.setVisible(false);
   hp.add(remote_label);
   remote_type = new ListBox();
   // remote_type.addItem("Google","G");
   // remote_type.addItem("Koders","D");
   // remote_type.addItem("GitHub","T");
   remote_type.addItem("SearchCode","C");
   remote_type.addItem("GitHub","Z");
   // remote_type.addItem("OpenHub","O");
   // remote_type.addItem("Sourcerer","S");
   // remote_type.addItem("Krugle","K");
   remote_type.addItem("Hunter","H");
   remote_type.addItem("CodeExchange","X");
   remote_type.addItem("GitZip","Z");
   remote_type.setItemSelected(0,true);
   remote_type.setVisible(true);
   hp.add(remote_type);

   keyword_label = new HTML("Description:\u00a0\u00a0<span class='subdesc'><br>" +
				"\u00a0\u00a0\u00a0(keywords)</span>");
   keyword_label.setStyleName("description");
   ft.setWidget(2,0,keyword_label);

   keyword_box = new TextBox();
   keyword_box.setVisibleLength(64);
   keyword_box.setMaxLength(512);
   keyword_box.addChangeHandler(this);
   keyword_box.setTabIndex(tab++);
   ft.setWidget(2,1,keyword_box);
   fmt.setStyleName(2,1,"descriptioninput");

   error_box = new Label(" ",true);
   error_box.setStyleName("errormessage");
   ft.setWidget(3,0,error_box);
   fmt.setColSpan(3,0,2);

   // fmt.setColSpan(4,0,4);
   // fmt.setStyleName(4,0,"logobar");

   hp = new HorizontalPanel();
   hp.setStyleName("logobar");
   hp.setHorizontalAlignment(HorizontalPanel.ALIGN_RIGHT);
   ft.setWidget(4,0,hp);
   fmt.setColSpan(4,0,5);

   Button help = new Button("HELP",new HelpHandler());
   help.setStyleName("help");
   hp.add(help);

   return ft;
}




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

String getFormatMethod()
{
   return result_area.getFormatMethod();
}



/********************************************************************************/
/*										*/
/*	Error management methods						*/
/*										*/
/********************************************************************************/

void addError(String msg)
{
   if (error_message == null) error_message = "PLEASE FIX: ";
   else error_message += "; ";
   error_message += msg;
}


void clearError()
{
   setErrorMessage("");
   error_message = null;
}



boolean reportError()
{
   if (error_message == null) return false;
   setErrorMessage(error_message);
   error_message = null;
   return true;
}



private void setErrorMessage(String msg)
{
   if (msg == null) msg = " ";
   error_box.setText(msg);
}



void clearKeywordError()
{
   keyword_label.setStyleName("description");
}



void setKeywordError()
{
   keyword_label.setStyleName("descriptionerror");
}



/********************************************************************************/
/*										*/
/*	Handle top-level changes						*/
/*										*/
/********************************************************************************/

public void onChange(ChangeEvent evt)
{
   if (evt.getSource() == look_for) {
      int what = look_for.getSelectedIndex();
      if (what == 0) setCurrentSearch(method_search);
      else if (what == 1) {
	 class_search.setIsFull(false);
	 setCurrentSearch(class_search);
       }
      else if (what == 2) {
	 class_search.setIsFull(true);
	 setCurrentSearch(class_search);
       }
      else if (what == 3) {
	 setCurrentSearch(ui_search);
       }
    }
   else if (evt.getSource() == look_in) {
      String v = look_in.getValue(look_in.getSelectedIndex());
      if (v.indexOf("R") >= 0) {
	 remote_label.setVisible(true);
	 remote_type.setVisible(true);
       }
      else {
	 remote_label.setVisible(false);
	 remote_type.setVisible(false);
       }
    }
   else if (evt.getSource() == remote_type) {
    }
   else if (evt.getSource() == keyword_box) {
      clearKeywordError();
    }
}



public void onClick(ClickEvent evt)
{
   if (evt.getSource() == find_button) {
      startSearch();
    }
}



private void setCurrentSearch(SviwebSearch ss)
{
   if (ss == current_search) return;

   if (current_search != null) current_search.getPanel().setVisible(false);

   current_search = ss;
   current_search.getPanel().setVisible(true);
}



SviwebSearch getCurrentSearch()
{
   return current_search;
}




/********************************************************************************/
/*										*/
/*	Search methods								*/
/*										*/
/********************************************************************************/

private void startSearch()
{
   if (search_status == Status.CHECKING) return;

   clearError();

   String desc = keyword_box.getText().trim();
   if (desc == null || desc.equals("")) {
      setKeywordError();
      addError("Must provide keywords");
    }

   Status lsts = current_search.getStatus();
   GWT.log("START SEARCH " + lsts,null);

   if (lsts == Status.BAD || lsts == Status.UNUSED || lsts == Status.UNCHECKED) {
      search_status = Status.BAD;
      return;
    }
   else if (lsts == Status.CHECKING) {
      search_status = Status.CHECKING;
      restart_search = true;
      return;
    }

   Document d = current_search.getSearchRequest();
   if (d != null) {
      Element req = d.getDocumentElement();
      addLookin(req);
      addKeywords(req);
    }

   if (d == null || reportError()) {
      search_status = Status.BAD;
      return;
    }

   search_status = Status.CHECKING;

   result_area.startSearch(d);
}



private void addLookin(Element req)
{
   String v = look_in.getValue(look_in.getSelectedIndex());
   if (v.indexOf("L") >= 0) req.setAttribute("LOCAL","TRUE");

   if (v.indexOf("R") >= 0) {
      req.setAttribute("REMOTE","TRUE");
      v = remote_type.getValue(remote_type.getSelectedIndex());
      if (v.indexOf("G") >= 0) req.setAttribute("GOOGLE","TRUE");
      if (v.indexOf("D") >= 0) req.setAttribute("KODERS","TRUE");
      if (v.indexOf("O") >= 0) req.setAttribute("OHLOH","TRUE");
      if (v.indexOf("T") >= 0) req.setAttribute("GITHUB","TRUE");
      if (v.indexOf("K") >= 0) req.setAttribute("KRUGLE","TRUE");
      if (v.indexOf("S") >= 0) req.setAttribute("SOURCERER","TRUE");
      if (v.indexOf("X") >= 0) req.setAttribute("CODEEX","TRUE");
      if (v.indexOf("H") >= 0) req.setAttribute("HUNTER","TRUE");
      if (v.indexOf("Z") >= 0) req.setAttribute("GITZIP","TRUE");
      if (v.indexOf("C") >= 0) req.setAttribute("SEARCHCODE","TRUE");
    }
}



private void addKeywords(Element req)
{
   String desc = keyword_box.getText().trim();
   String [] tops = desc.split("[;:]");

   for (int i = 0; i < tops.length; ++i) {
      Element top = SviwebXml.addChild(req,"KEYWORDS");
      String phr = tops[i].trim();
      if (!phr.equals("")) {
	 String [] wds = phr.split("\\s");
	 for (int j = 0; j < wds.length; ++j) {
	    String wd = wds[j].trim();
	    if (!wd.equals("")) {
	       SviwebXml.addTextElement(top,"KEYWORD",wd);
	     }
	  }
       }
    }
}




/********************************************************************************/
/*										*/
/*	Search status change methods						*/
/*										*/
/********************************************************************************/

private class SearchChange implements StatusCallback {

   public void setStatus(Status sts) {
      if (sts == Status.CHECKING) return;
      if (sts == Status.OK) {
	 if (restart_search) {
	    restart_search = false;
	    search_status = Status.UNCHECKED;
	    startSearch();
	  }
	 else search_status = Status.OK;
       }
      else if (sts == Status.BAD) {
	 restart_search = false;
	 search_status = Status.BAD;
	 reportError();
       }
    }

}	// end of subclass SearchChange




private class ResultChange implements StatusCallback {

   public void setStatus(Status sts) {
      search_status = sts;
    }

}	// end of subclass ResultChange




/********************************************************************************/
/*										*/
/*	Methods to handle server commands					*/
/*										*/
/********************************************************************************/

void sendToServer(Document d,AsyncCallback<String> cb)
{
   String xml = SviwebXml.convertXmlToString(d);

   engine_service.sendToServer(xml,cb);
}



/********************************************************************************/
/*										*/
/*	General redirection methods						*/
/*										*/
/********************************************************************************/

Status checkContext(StatusCallback cb)
{
   if (current_search == null) return Status.OK;

   return current_search.checkContext(cb);
}



void addContext(Element root)
{
   if (current_search != null) current_search.addContext(root);
}



/********************************************************************************/
/*										*/
/*	Methods to pop up a window						*/
/*										*/
/********************************************************************************/

static native void newWindow(String html) /*-{
   var ne = $wnd._nativeExtensions;
   $wnd._nativeExtensions = false
   var x = $wnd.open("","licenseWindow","");
   if (x != null) {
      x.document.write(html);
      x.document.close();
    }
   $wnd._nativeExtensions = ne;
}-*/;


/********************************************************************************/
/*										*/
/*	Help methods								*/
/*										*/
/********************************************************************************/

private class HelpHandler implements ClickHandler {

   public void onClick(ClickEvent evt) {
      Window.open("s6help.html","_blank",null);
      // newWindow("<html><body>Help goes here</body></html>");
    }

}




}	// end of class SviwebMain




/* end of SviwebMain.java */
