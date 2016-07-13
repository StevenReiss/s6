/********************************************************************************/
/*										*/
/*		SviwebWidget.java						*/
/*										*/
/*	Generic widget code							*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/sviweb/client/SviwebWidget.java,v 1.2 2008-06-12 17:47:57 spr Exp $ */


/*********************************************************************************
 *
 * $Log: SviwebWidget.java,v $
 * Revision 1.2  2008-06-12 17:47:57  spr
 * Next version of S6.
 *
 * Revision 1.1.1.1  2008-06-03 12:59:23  spr
 * Initial version of S6
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.sviweb.client;

import com.google.gwt.user.client.ui.ListBox;



class SviwebWidget implements SviwebConstants {




/********************************************************************************/
/*										*/
/*	Methods to create a selection box					*/
/*										*/
/********************************************************************************/

static ListBox createSelection(String dflt,String [][] opts)
{
   ListBox lb = new ListBox();
   lb.setVisibleItemCount(1);

   if (opts != null) {
      int didx = 0;
      for (int i = 0; i < opts.length; ++i) {
	 lb.addItem(opts[i][1],opts[i][0]);
	 if (opts[i][0].equals(dflt) || opts[i][1].equals(dflt)) didx = i;
       }
      lb.setSelectedIndex(didx);
    }

   return lb;
}





}	// end of class SviwebWidget



/* end of SviwebWidget.java */
