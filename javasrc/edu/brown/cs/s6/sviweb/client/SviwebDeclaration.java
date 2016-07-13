/********************************************************************************/
/*										*/
/*		SviwebMethodDeclaration.java					*/
/*										*/
/*	Interface for handling method declaration input and checking		*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/sviweb/client/SviwebDeclaration.java,v 1.9 2015/09/23 17:58:16 spr Exp $ */


/*********************************************************************************
 *
 * $Log: SviwebDeclaration.java,v $
 * Revision 1.9  2015/09/23 17:58:16  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.8  2013/09/13 20:33:15  spr
 * Add calls for UI search.
 *
 * Revision 1.7  2012-06-11 14:08:22  spr
 * Add framework search; fix bugs
 *
 * Revision 1.6  2009-09-18 01:42:37  spr
 * Updates for new gwt and support user feedback and full class.
 *
 * Revision 1.5  2009-05-12 22:29:43  spr
 * Update front end of user context.
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

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.XMLParser;



public class SviwebDeclaration implements SviwebConstants, ChangeHandler {



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private SviwebMain	svi_web;
private StatusCallback	status_callback;

private String		for_class;

private VerticalPanel	decl_panel;
private TextBox 	decl_box;
private Label		decl_error;

private Status		decl_status;
private String		last_text;
private String		last_error;
private boolean 	recheck_needed;

private Element 	method_signature;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

SviwebDeclaration(SviwebMain sm,StatusCallback sb,String cls)
{
   svi_web = sm;
   status_callback = sb;
   for_class = cls;

   last_text = null;
   last_error = null;
   decl_status = Status.UNCHECKED;
   recheck_needed = false;
   method_signature = null;

   decl_panel = new VerticalPanel();

   decl_box = new TextBox();
   decl_box.setVisibleLength(64);
   decl_box.setMaxLength(256);
   decl_box.setStyleName("methoddecl");
   decl_box.addChangeHandler(this);
   decl_panel.add(decl_box);

   decl_error = new Label();
   decl_error.setStyleName("methoddeclerror");
   decl_panel.add(decl_error);
   decl_error.setVisible(false);
}




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

Widget getWidget()			{ return decl_panel; }

Status getStatus()
{
   if (decl_status == Status.UNCHECKED) check();

   return decl_status;
}


private void setStatus(Status sts)
{
   if (decl_status == sts) return;

   decl_status = sts;
   if (status_callback != null) status_callback.setStatus(sts);
}


int setTabIndex(int idx)
{
   decl_box.setTabIndex(idx++);

   return idx;
}


Element getSignature()			{ return method_signature; }


String getName()
{
   if (method_signature == null) return null;
   return SviwebXml.getTextElement(method_signature,"NAME");
}


void setClassName(String nm)
{
   for_class = nm;
   if (decl_status != Status.UNCHECKED && decl_status != Status.UNUSED) check();
}



boolean isStatic()
{
   if (method_signature == null) return false;
   String s = SviwebXml.getXmlAttr(method_signature,"STATIC");
   if (s != null && (s.startsWith("t") || s.startsWith("T"))) return true;
   return false;
}


boolean hasNoReturn()
{
   if (method_signature == null) return false;
   String ret = SviwebXml.getTextElement(method_signature,"RETURN");
   if (ret != null && ret.equals("void")) return true;
   return false;
}



boolean hasNoArguments()
{
   if (method_signature == null) return false;
   String args = SviwebXml.getTextElement(method_signature,"ARGS");
   if (args == null || args.equals("") || args.equals("void")) return true;
   return false;
}






/********************************************************************************/
/*										*/
/*	Methods to explicitly check the declaration				*/
/*										*/
/********************************************************************************/

void check()
{
   clearError();

   Status sts = svi_web.checkContext(new ContextChange());
   if (sts == Status.CHECKING) {
      recheck_needed = true;
      decl_status = Status.UNCHECKED;
      return;
    }

   String t = decl_box.getText().trim();
   if (t.equals("")) setStatus(Status.UNUSED);
   else if (t.equals(last_text)) {
      if (decl_status == Status.BAD) svi_web.addError(last_error);
    }
   else if (decl_status == Status.CHECKING) recheck_needed = true;
   else {
      Document doc = XMLParser.createDocument();

      Element root = SviwebXml.addChild(doc,"CHECK");
      root.setAttribute("WHAT","METHOD");
      SviwebXml.addTextElement(root,"METHOD",t);
      if (for_class != null) SviwebXml.addTextElement(root,"CLASS",for_class);
      svi_web.addContext(root);

      setStatus(Status.CHECKING);
      svi_web.sendToServer(doc,new DeclCallback());
    }
}



/********************************************************************************/
/*										*/
/*	Callback methods							*/
/*										*/
/********************************************************************************/

public void onChange(ChangeEvent evt)
{
   if (evt.getSource() == decl_box) check();
}



/********************************************************************************/
/*										*/
/*	Checking callbacks							*/
/*										*/
/********************************************************************************/

private void handleDeclarationOkay(Element sgn)
{
   if (handleDeclarationRecheck()) return;

   method_signature = sgn;
   Element melt = SviwebXml.getXmlElement(sgn,"METHOD");
   Element txte = SviwebXml.getXmlElement(melt,"TEXT");
   String txt = SviwebXml.getXmlText(txte);
   last_text = txt;
   decl_box.setText(txt);
   setStatus(Status.OK);
   last_error = null;
   clearError();
}



private void handleDeclarationBad(String msg)
{
   if (handleDeclarationRecheck()) return;

   method_signature = null;
   setStatus(Status.BAD);
   last_error = msg;
   svi_web.addError(msg);
   setError(msg);
}




private boolean handleDeclarationRecheck()
{
   if (!recheck_needed) return false;

   recheck_needed = false;
   setStatus(Status.UNCHECKED);
   check();

   return true;
}



/********************************************************************************/
/*										*/
/*	Asynchronous check call 						*/
/*										*/
/********************************************************************************/

private class DeclCallback implements AsyncCallback<String> {

   public void onSuccess(String rslt) {
      if (rslt == null) handleDeclarationBad("No response from S6 server");
      else {
	 Document r = XMLParser.parse(rslt);
	 Element sgn = SviwebXml.getXmlElement(r,"SIGNATURE");
	 if (sgn != null) handleDeclarationOkay(sgn);
	 else {
	    String emsg = null;
	    Element err = SviwebXml.getXmlElement(r,"PROBLEM");
	    if (err == null) err = SviwebXml.getXmlElement(r,"ERROR");
	    if (err == null) emsg = "S6 Server problem";
	    else emsg = SviwebXml.getXmlText(err);
	    handleDeclarationBad(emsg);
	  }
       }
    }

   public void onFailure(Throwable t) {
      String emsg = t.getMessage();
      if (emsg == null) emsg = "S6 Server failure";
      handleDeclarationBad(emsg);
    }

}	// end of subclass DeclCallback




/********************************************************************************/
/*										*/
/*	Handle check from the context						*/
/*										*/
/********************************************************************************/

private class ContextChange implements StatusCallback {

   public void setStatus(Status sts) {
      handleDeclarationRecheck();
    }

}	// end of subclass ContextChange




/********************************************************************************/
/*										*/
/*	Error message handling							*/
/*										*/
/********************************************************************************/

private void clearError()
{
   decl_error.setText("");
   decl_error.setVisible(false);
}


private void setError(String msg)
{
   decl_error.setText(msg);
   decl_error.setVisible(true);
}




}	// end of class SviwebDeclaration




/* end of SviwebDeclaration.java */
