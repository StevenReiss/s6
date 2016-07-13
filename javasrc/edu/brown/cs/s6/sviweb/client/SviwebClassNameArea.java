/********************************************************************************/
/*										*/
/*		SviwebClassNameArea.java					*/
/*										*/
/*	Area to hold and validate one or more class names			*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/sviweb/client/SviwebClassNameArea.java,v 1.3 2015/09/23 17:58:16 spr Exp $ */


/*********************************************************************************
 *
 * $Log: SviwebClassNameArea.java,v $
 * Revision 1.3  2015/09/23 17:58:16  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.2  2013/09/13 20:33:15  spr
 * Add calls for UI search.
 *
 * Revision 1.1  2012-06-11 20:13:59  spr
 * Add newer files to repository.
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.sviweb.client;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.XMLParser;


class SviwebClassNameArea implements SviwebConstants, ChangeHandler {


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private SviwebMain	svi_web;
private boolean 	allow_multiple;
private boolean 	allow_empty;
private TextBox 	name_box;

private Status		cur_status;
private String		last_error;
private StatusCallback	status_callback;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

SviwebClassNameArea(SviwebMain sm,boolean mult,boolean emptyok,int tab,StatusCallback sb)
{
   svi_web = sm;
   allow_multiple = mult;
   allow_empty = emptyok;
   status_callback = sb;
   last_error = null;

   name_box = new TextBox();
   name_box.setMaxLength((mult ? 256 : 64));
   name_box.setVisibleLength(32);
   name_box.addChangeHandler(this);
   name_box.setStyleName("classdecl");
   name_box.setTabIndex(tab);

   if (allow_empty) cur_status = Status.OK;
   else cur_status = Status.UNCHECKED;
}




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

Widget getWidget()
{
   return name_box;
}




/********************************************************************************/
/*										*/
/*	Handle editing changes							*/
/*										*/
/********************************************************************************/

public void onChange(ChangeEvent evt)
{
   if (evt.getSource() != name_box) return;

   cur_status = Status.UNCHECKED;
}




/********************************************************************************/
/*										*/
/*	Validation methods							*/
/*										*/
/********************************************************************************/

void invalidate()
{
   cur_status = Status.UNCHECKED;
}



Status validate()
{
   if (cur_status == Status.CHECKING) return cur_status;
   if (cur_status == Status.OK) return cur_status;
   if (cur_status == Status.BAD) {
      if (last_error != null) svi_web.addError(last_error);
      return cur_status;
    }

   cur_status = Status.OK;

   String txt = name_box.getText();
   if (txt != null) txt = txt.trim();
   if (txt != null && txt.length() == 0) {
      if (allow_empty) return cur_status;
      last_error = "Class must be specified";
      svi_web.addError(last_error);
      cur_status = Status.BAD;
      return cur_status;
    }

   Document doc = XMLParser.createDocument();
   Element root = SviwebXml.addChild(doc,"CHECK");
   if (allow_multiple) root.setAttribute("WHAT","CLASSLIST");
   else root.setAttribute("WHAT","CLASS");
   SviwebXml.addTextElement(root,"NAME",txt);
   svi_web.addContext(root);

   cur_status = Status.CHECKING;

   svi_web.sendToServer(doc,new NameCallback());

   return cur_status;
}



private void setStatus(Status sts)
{
   if (cur_status == sts) return;

   cur_status = sts;
   if (status_callback != null) status_callback.setStatus(sts);
}



/********************************************************************************/
/*										*/
/*	Checking callbacks							*/
/*										*/
/********************************************************************************/

private void handleNamesOkay(String nms)
{
   name_box.setText(nms);

   setStatus(Status.OK);
   last_error = null;
}


private void handleNamesBad(String msg)
{
   last_error = msg;
   svi_web.addError(msg);
   setStatus(Status.BAD);
}




/********************************************************************************/
/*										*/
/*	Asynchronous check call 						*/
/*										*/
/********************************************************************************/

private class NameCallback implements AsyncCallback<String> {

   public void onSuccess(String rslt) {
      if (rslt == null) handleNamesBad("No response from S6 server");
      else {
	 Document r = XMLParser.parse(rslt);
	 Element nms = SviwebXml.getXmlElement(r,"NAME");
	 if (nms != null) handleNamesOkay(SviwebXml.getXmlText(nms));
	 else {
	    String emsg = null;
	    Element err = SviwebXml.getXmlElement(r,"PROBLEM");
	    if (err == null) err = SviwebXml.getXmlElement(r,"ERROR");
	    if (err == null) emsg = "S6 Server problem";
	    else emsg = SviwebXml.getXmlText(err);
	    handleNamesBad(emsg);
	  }
       }
    }

   public void onFailure(Throwable t) {
      String emsg = t.getMessage();
      if (emsg == null) emsg = "S6 Server failure";
      handleNamesBad(emsg);
    }

}	// end of subclass NameCallback




}	// end of class SviwebClassNameArea




/* end of SviwebClassNameArea.java */
