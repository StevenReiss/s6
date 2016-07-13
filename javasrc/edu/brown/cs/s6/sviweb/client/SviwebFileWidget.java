/********************************************************************************/
/*										*/
/*		SviwebFileWidget.java						*/
/*										*/
/*	File Upload widget with associated mechanisms				*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/sviweb/client/SviwebFileWidget.java,v 1.6 2015/09/23 17:58:16 spr Exp $ */


/*********************************************************************************
 *
 * $Log: SviwebFileWidget.java,v $
 * Revision 1.6  2015/09/23 17:58:16  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.5  2013/09/13 20:33:15  spr
 * Add calls for UI search.
 *
 * Revision 1.4  2012-06-11 14:12:14  spr
 * Make things compile
 *
 * Revision 1.3  2012-06-11 14:08:22  spr
 * Add framework search; fix bugs
 *
 * Revision 1.2  2009-09-18 01:42:37  spr
 * Updates for new gwt and support user feedback and full class.
 *
 * Revision 1.1  2009-05-12 22:45:57  spr
 * Add our extensions for file selector.
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.sviweb.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.DOMException;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.XMLParser;


class SviwebFileWidget implements SviwebConstants
{




/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private StatusCallback	status_change;
private FormPanel	outer_form;
private FileUpload	file_upload;
private String		file_label;
private Status		current_status;
private String		current_error;
private String		remote_name;
private String		prior_file;
private List<StatusCallback> temp_callbacks;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

SviwebFileWidget(String name,StatusCallback cb)
{
   file_label = name;
   status_change = cb;
   remote_name = null;
   prior_file = null;

   temp_callbacks = new ArrayList<StatusCallback>();

   outer_form = new FormPanel();
   outer_form.setAction(GWT.getModuleBaseURL() + "/sviwebfile");
   outer_form.setEncoding(FormPanel.ENCODING_MULTIPART);
   outer_form.setMethod(FormPanel.METHOD_POST);
   outer_form.addSubmitCompleteHandler(new FileHandler());

   HorizontalPanel pnl = new HorizontalPanel();
   outer_form.setWidget(pnl);

   file_upload = new FileUpload();
   file_upload.setName(file_label);
   pnl.add(file_upload);
   current_status = Status.UNCHECKED;
   current_error = null;
}




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

public Widget getWidget()		{ return outer_form; }

public String getRemoteFile()		{ return remote_name; }

public String getError()		{ return current_error; }



/********************************************************************************/
/*										*/
/*	Form Checker								*/
/*										*/
/********************************************************************************/

void invalidate()
{
   if (prior_file != null && prior_file.equals(file_upload.getFilename())) return;

   current_status = Status.UNCHECKED;
   current_error = null;
   remote_name = null;
}




Status validate(StatusCallback cb)
{
   Status sts = validate();
   if (sts == Status.CHECKING && cb != null) {
      temp_callbacks.add(cb);
    }
   return sts;
}



Status validate()
{
   if (current_status == Status.CHECKING) return current_status;
   if (current_status == Status.OK || current_status == Status.BAD) {
      String fn = file_upload.getFilename();
      if (fn != null && !fn.equals(prior_file)) {
	 prior_file = null;
	 invalidate();
       }
    }
   if (current_status == Status.OK) return current_status;
   if (current_status == Status.BAD) return current_status;

   prior_file = file_upload.getFilename();
   if (prior_file == null || prior_file.length() == 0) {
      current_status = Status.OK;
    }
   else {
      current_status = Status.CHECKING;
      outer_form.submit();
    }

   return current_status;
}




/********************************************************************************/
/*										*/
/*	Callback handler							*/
/*										*/
/********************************************************************************/

private class FileHandler implements FormPanel.SubmitCompleteHandler {

   public void onSubmitComplete(FormPanel.SubmitCompleteEvent evt) {
      String rslt = evt.getResults();
      remote_name = null;
      current_status = Status.OK;

      try {
	 Document doc = XMLParser.parse(rslt);
	 Element re = SviwebXml.getXmlElement(doc,"result");
	 Element ee = SviwebXml.getXmlElement(doc,"problem");
	 if (ee == null) ee = SviwebXml.getXmlElement(doc,"error");
	 if (re != null) {
	    remote_name = SviwebXml.getXmlText(re);
	    current_status = Status.OK;
	  }
	 else if (ee != null) {
	    current_error = SviwebXml.getXmlText(ee);
	    remote_name = "BAD: " + current_error;
	    current_status = Status.BAD;
	  }
	 else {
	    current_error = "Can't connect to server to send file";
	    remote_name = "BAD: " + current_error;
	    current_status = Status.BAD;
	  }
       }
      catch (DOMException e) {
	 current_status = Status.BAD;
	 current_error = e.toString();
       }

      if (status_change != null) status_change.setStatus(current_status);
      for (StatusCallback sc : temp_callbacks) {
	 sc.setStatus(current_status);
       }
      temp_callbacks.clear();
    }

}



}	// end of class SviwebFileWidget




/* end of SviwebFileWidget.java */
