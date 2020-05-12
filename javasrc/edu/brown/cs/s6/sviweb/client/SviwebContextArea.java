/********************************************************************************/
/*										*/
/*		SviwebContextArea.java						*/
/*										*/
/*	Interactive box to elicit and store context information 		*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/sviweb/client/SviwebContextArea.java,v 1.6 2015/09/23 17:58:16 spr Exp $ */


/*********************************************************************************
 *
 * $Log: SviwebContextArea.java,v $
 * Revision 1.6  2015/09/23 17:58:16  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.5  2012-06-11 14:08:22  spr
 * Add framework search; fix bugs
 *
 * Revision 1.4  2009-09-18 01:42:37  spr
 * Updates for new gwt and support user feedback and full class.
 *
 * Revision 1.3  2009-05-12 22:29:42  spr
 * Update front end of user context.
 *
 * Revision 1.2  2008-08-28 00:33:03  spr
 * Next version of S6.	Lots of bug fixes, some new functionality.
 *
 * Revision 1.1  2008-06-12 17:47:57  spr
 * Next version of S6.
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.sviweb.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Element;


class SviwebContextArea implements SviwebConstants, ClickHandler
{


private SviwebFileWidget file_widget;
private HorizontalPanel context_panel;
private Button	       context_button;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

SviwebContextArea(SviwebMain sm,StatusCallback cb)
{
   context_panel = new HorizontalPanel();

   file_widget = new SviwebFileWidget("Context",cb);
   context_panel.add(file_widget.getWidget());

   HTML h = new HTML("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
   context_panel.add(h);

   context_button = new Button("Create Context",this);
   context_button.setStyleName("optionpanelbtn");
   context_panel.add(context_button);
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

Widget getWidget()				{ return context_panel; }



/********************************************************************************/
/*										*/
/*	Callback methods							*/
/*										*/
/********************************************************************************/

public void onClick(ClickEvent evt)
{
   Window.open("http://bubbles.cs.brown.edu/s6/Context/context.jnlp","Create Context",null);
}



/********************************************************************************/
/*										*/
/*	Output methods								*/
/*										*/
/********************************************************************************/

void generateOutput(Element root)
{
   String rnm = file_widget.getRemoteFile();
   Element e = SviwebXml.addChild(root,"CONTEXT");
   if (rnm != null) {
      e.setAttribute("FILE",rnm);
    }
}


void invalidate()
{
   file_widget.invalidate();
}



Status validate(StatusCallback cb)
{
   return file_widget.validate(cb);
}



Status validate()
{
   return file_widget.validate();
}



}	// end of class SviwebContextArea




/* end of SviwebContextArea.java */

