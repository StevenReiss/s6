/********************************************************************************/
/*										*/
/*		SviwebSearch.java						*/
/*										*/
/*	Interface to handle search strategies					*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/sviweb/client/SviwebSearch.java,v 1.9 2015/09/23 17:58:17 spr Exp $ */


/*********************************************************************************
 *
 * $Log: SviwebSearch.java,v $
 * Revision 1.9  2015/09/23 17:58:17  spr
 * Updates for Andriod UI and better keysearch.
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

import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;


abstract class SviwebSearch implements SviwebConstants, ClickHandler {



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

protected SviwebMain	svi_web;
protected StatusCallback status_callback;

private VerticalPanel	search_panel;
protected Grid		search_grid;

private Button		context_btn;
private Button		security_btn;
private Button		contract_btn;
private Button		environ_btn;
private Button		threads_btn;

protected SviwebSecurityArea security_area;
protected SviwebContractArea contract_area;
protected SviwebContextArea context_area;
protected SviwebEnvironArea environ_area;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

protected SviwebSearch(SviwebMain sm,StatusCallback cb)
{
   svi_web = sm;
   status_callback = cb;

   search_panel = new VerticalPanel();

   Label lbl = new Label(getSearchType());
   lbl.setStyleName("searchcaption");
   search_panel.add(lbl);

   search_grid = new Grid(getBottomRow()+1,2);
   search_grid.setStyleName("searchgrid");
   search_panel.add(search_grid);

   HTMLTable.ColumnFormatter cfmt = search_grid.getColumnFormatter();
   cfmt.setWidth(0,"10px");
   cfmt.setWidth(1,"100*");
}




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

Panel getPanel()			{ return search_panel; }

void addCommonProperties(Element root)
{
   root.setAttribute("FORMAT",svi_web.getFormatMethod());
}


/********************************************************************************/
/*										*/
/*	Context management methods						*/
/*										*/
/********************************************************************************/

Status checkContext(StatusCallback cb)
{
   if (context_area == null) return Status.OK;

   return context_area.validate(cb);
}


void addContext(Element root)
{
   if (context_area != null) context_area.generateOutput(root);
}



/********************************************************************************/
/*										*/
/*	Setup methods								*/
/*										*/
/********************************************************************************/

protected void setupContext(int tab)
{
   Label lbl = new Label("Context:\u00a0");
   lbl.setStyleName("contextlabel");
   search_grid.setWidget(getContextRow(),0,lbl);
   context_area = new SviwebContextArea(svi_web,new ContextChange());
   search_grid.setWidget(getContextRow(),1,context_area.getWidget());
   showContext(false);
}



protected void setupSecurity(int tab)
{
   Label lbl = new Label("Security:\u00a0");
   lbl.setStyleName("securitylabel");
   search_grid.setWidget(getSecurityRow(),0,lbl);
   security_area = new SviwebSecurityArea(svi_web,this,tab,new SecurityChange());
   search_grid.setWidget(getSecurityRow(),1,security_area.getWidget());
   showSecurity(false);
}



protected void setupContracts(int tab)
{
   Label lbl = new Label("Contracts:\u00a0");
   lbl.setStyleName("contractlabel");
   search_grid.setWidget(getContractRow(),0,lbl);
   contract_area = new SviwebContractArea(svi_web,this,tab,new ContractChange());
   search_grid.setWidget(getContractRow(),1,contract_area.getWidget());
   showContract(false);
}



protected void setupEnvironment(int tab)
{
   Label lbl = new Label("Environment:\u00a0");
   lbl.setStyleName("environmentlabel");
   search_grid.setWidget(getEnvironmentRow(),0,lbl);
   environ_area = new SviwebEnvironArea(svi_web,this,tab,null);
   search_grid.setWidget(getEnvironmentRow(),1,environ_area.getWidget());
   showEnvironment(false);
}



protected void setupBottomPanel(int tab)
{
   HorizontalPanel btnpnl = new HorizontalPanel();
   btnpnl.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
   btnpnl.setStyleName("optionpanel");

   if (getContextRow() > 0) {
      context_btn = new Button("User Context",this);
      context_btn.setStyleName("optionpanelbtn");
      context_btn.setTabIndex(tab);
      btnpnl.add(context_btn);
    }
   if (getSecurityRow() > 0) {
      security_btn = new Button("Security",this);
      security_btn.setStyleName("optionpanelbtn");
      security_btn.setTabIndex(tab);
      btnpnl.add(security_btn);
    }
   if (getContractRow() > 0) {
      contract_btn = new Button("Contracts",this);
      contract_btn.setStyleName("optionpanelbtn");
      contract_btn.setTabIndex(tab);
      btnpnl.add(contract_btn);
    }
   if (getEnvironmentRow() > 0) {
      environ_btn = new Button("Environment",this);
      environ_btn.setStyleName("optionpanelbtn");
      environ_btn.setTabIndex(tab);
      btnpnl.add(environ_btn);
    }
   if (getThreadingRow() > 0) {
      threads_btn = new Button("Threading",this);
      threads_btn.setStyleName("optionpanelbtn");
      threads_btn.setTabIndex(tab);
      btnpnl.add(threads_btn);
    }

   search_grid.setWidget(getBottomRow(),1,btnpnl);
}



/********************************************************************************/
/*										*/
/*	Abstract methods							*/
/*										*/
/********************************************************************************/

protected abstract String getSearchType();
protected abstract int getBottomRow();
protected int getSecurityRow()				{ return -1; }
protected int getThreadingRow() 			{ return -1; }
protected int getContractRow()				{ return -1; }
protected int getEnvironmentRow()			{ return -1; }
protected int getContextRow()				{ return -1; }

abstract Status getStatus();

abstract Document getSearchRequest();

abstract List<SviwebDeclaration> getMethods();

abstract void addSignature(Element root);




/********************************************************************************/
/*										*/
/*	Action methods								*/
/*										*/
/********************************************************************************/

protected void showContext(boolean fg)
{
   HTMLTable.RowFormatter rfmt = search_grid.getRowFormatter();
   rfmt.setVisible(getContextRow(),fg);
}


protected void showSecurity(boolean fg)
{
   HTMLTable.RowFormatter rfmt = search_grid.getRowFormatter();
   rfmt.setVisible(getSecurityRow(),fg);
}


protected void showThreading(boolean fg)
{
   HTMLTable.RowFormatter rfmt = search_grid.getRowFormatter();
   rfmt.setVisible(getThreadingRow(),fg);
}



protected void showContract(boolean fg)
{
   HTMLTable.RowFormatter rfmt = search_grid.getRowFormatter();
   rfmt.setVisible(getContractRow(),fg);
}



protected void showEnvironment(boolean fg)
{
   HTMLTable.RowFormatter rfmt = search_grid.getRowFormatter();
   rfmt.setVisible(getEnvironmentRow(),fg);
}



/********************************************************************************/
/*										*/
/*	Methods to handle option spec buttons					*/
/*										*/
/********************************************************************************/

public void onClick(ClickEvent evt)
{
   if (evt.getSource() == security_btn) {
      security_btn.setVisible(false);
      showSecurity(true);
    }
   else if (evt.getSource() == contract_btn) {
      contract_btn.setVisible(false);
      showContract(true);
    }
   else if (evt.getSource() == environ_btn) {
      environ_btn.setVisible(false);
      showEnvironment(true);
    }
   else if (evt.getSource() == threads_btn) {
      threads_btn.setVisible(false);
      showThreading(true);
    }
   else if (evt.getSource() == context_btn) {
      context_btn.setVisible(false);
      showContext(true);
    }

}



/********************************************************************************/
/*										*/
/*	Description change classes						*/
/*										*/
/********************************************************************************/

protected class TestStatusChange implements StatusCallback {

   public void setStatus(Status sts) {
      if (status_callback != null) status_callback.setStatus(sts);
    }

}	// end of subclass TestStatusChange



protected class SecurityChange implements StatusCallback {

   public void setStatus(Status sts) {
      if (status_callback != null) status_callback.setStatus(sts);
    }

}	// end of subclass SecurityChange



protected class ContractChange implements StatusCallback {

   public void setStatus(Status sts) {
      if (status_callback != null) status_callback.setStatus(sts);
    }

}	// end of subclass ContractChange



protected class ContextChange implements StatusCallback {

   public void setStatus(Status sts) {
      if (status_callback != null) status_callback.setStatus(sts);
    }

}	// end of subclass ContextChange



}	// end of abstract class SviwebSearch




/* end of SviwebSearch.java */
