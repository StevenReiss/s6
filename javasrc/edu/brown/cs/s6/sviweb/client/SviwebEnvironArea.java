/********************************************************************************/
/*										*/
/*		SviwebEnvironArea.java						*/
/*										*/
/*	Environment area for a class or method search				*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/sviweb/client/SviwebEnvironArea.java,v 1.2 2015/09/23 17:58:16 spr Exp $ */


/*********************************************************************************
 *
 * $Log: SviwebEnvironArea.java,v $
 * Revision 1.2  2015/09/23 17:58:16  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.1  2012-06-11 20:13:59  spr
 * Add newer files to repository.
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.sviweb.client;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Element;



class SviwebEnvironArea implements SviwebConstants, ChangeHandler {




private ListBox  language_box;
private HorizontalPanel language_panel;
private ChangeHandler change_handler;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

SviwebEnvironArea(SviwebMain sm,SviwebSearch ss,int tab,ChangeHandler cb)
{
   change_handler = cb;

   language_panel = new HorizontalPanel();

   language_box = new ListBox();
   language_box.addItem("Any","ANY");
   language_box.addItem("Java 1.3","J3");
   language_box.addItem("Java 1.4","J4");
   language_box.addItem("Java 5","J5");
   language_box.addItem("Java 6","J6");
   language_box.addChangeHandler(this);
   language_box.setTabIndex(tab);
   language_panel.add(language_box);
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

Widget getWidget()			{ return language_panel; }



/********************************************************************************/
/*										*/
/*	Output methods								*/
/*										*/
/********************************************************************************/

void addEnvironment(Element root)
{
   int idx = language_box.getSelectedIndex();
   String val = language_box.getValue(idx);
   if (val.equals("ANY")) return;

   Element telt = SviwebXml.addChild(root,"LANGUAGE");
   telt.setAttribute("TYPE",val);
}



/********************************************************************************/
/*										*/
/*	Change methods								*/
/*										*/
/********************************************************************************/

public void onChange(ChangeEvent evt)
{
   if (change_handler != null) {
      change_handler.onChange(evt);
    }
}



}	// end of class SviwebEnvironArea




/* end of SviwebEnvironArea.java */
