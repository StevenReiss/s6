/********************************************************************************/
/*										*/
/*		SviwebThreadingArea.java					*/
/*										*/
/*	Threading information for a method					*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/sviweb/client/SviwebThreadingArea.java,v 1.5 2015/09/23 17:58:17 spr Exp $ */


/*********************************************************************************
 *
 * $Log: SviwebThreadingArea.java,v $
 * Revision 1.5  2015/09/23 17:58:17  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.4  2012-06-11 14:08:23  spr
 * Add framework search; fix bugs
 *
 * Revision 1.3  2009-09-18 01:42:37  spr
 * Updates for new gwt and support user feedback and full class.
 *
 * Revision 1.2  2008-11-12 13:52:27  spr
 * Performance and bug updates.
 *
 * Revision 1.1  2008-06-12 17:47:57  spr
 * Next version of S6.
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.sviweb.client;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Element;



class SviwebThreadingArea implements SviwebConstants, ChangeHandler {




private ListBox  threading_box;
private HorizontalPanel threading_panel;
private ChangeHandler change_handler;
private TextBox threading_input;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

SviwebThreadingArea(SviwebMain sm,ChangeHandler cb)
{
   change_handler = cb;

   threading_panel = new HorizontalPanel();

   threading_box = new ListBox();
   threading_box.addItem("Any","ANY");
   threading_box.addItem("Thread Safe","SAFE");
   threading_box.addItem("Synchronized","SYNC");
   threading_box.addItem("Syncrhonize On ...","SYNCON");
   threading_box.addChangeHandler(this);
   threading_panel.add(threading_box);

   threading_input = new TextBox();
   threading_input.setMaxLength(256);
   threading_input.setVisibleLength(48);
   threading_input.setVisible(false);
   threading_input.addChangeHandler(this);
   threading_panel.add(threading_input);
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

Widget getWidget()			{ return threading_panel; }



/********************************************************************************/
/*										*/
/*	Output methods								*/
/*										*/
/********************************************************************************/

void addThreading(Element root)
{
   int idx = threading_box.getSelectedIndex();
   String val = threading_box.getValue(idx);
   if (val.equals("ANY")) return;

   Element telt = SviwebXml.addChild(root,"THREADING");
   telt.setAttribute("TYPE",val);
   if (val.equals("SYNCON")) {
      SviwebXml.addTextElement(telt,"SYNC",threading_input.getText());
    }
}



/********************************************************************************/
/*										*/
/*	Change methods								*/
/*										*/
/********************************************************************************/

public void onChange(ChangeEvent evt)
{
   if (evt.getSource() == threading_box) {
      int idx = threading_box.getSelectedIndex();
      String val = threading_box.getValue(idx);
      if (val.equals("SYNCON")) {
	 threading_input.setVisible(true);
       }
      else {
	 threading_input.setVisible(false);
       }
    }

   if (change_handler != null) {
      change_handler.onChange(evt);
    }
}



}	// end of class SviwebThreadingArea




/* end of SviwebThreadingArea.java */
