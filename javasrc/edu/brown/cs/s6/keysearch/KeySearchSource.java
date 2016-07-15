/********************************************************************************/
/*										*/
/*		KeySearchSource.java						*/
/*										*/
/*	Default implementation of a source					*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/keysearch/KeySearchSource.java,v 1.4 2015/09/23 17:58:00 spr Exp $ */


/*********************************************************************************
 *
 * $Log: KeySearchSource.java,v $
 * Revision 1.4  2015/09/23 17:58:00  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.3  2013/09/13 20:32:32  spr
 * Add calls for UI search.
 *
 * Revision 1.2  2008-11-12 13:51:37  spr
 * Performance and bug updates.
 *
 * Revision 1.1.1.1  2008-06-03 12:59:22  spr
 * Initial version of S6
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.keysearch;


import edu.brown.cs.s6.common.S6Factory;
import edu.brown.cs.s6.common.S6License;
import edu.brown.cs.s6.common.S6Source;




abstract class KeySearchSource implements S6Source {


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private String license_uid;
private int    search_index;

private static S6License license_manager;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

KeySearchSource(String code,int idx)
{
   license_uid = null;
   search_index = idx+1;

   if (code != null) {
      synchronized (KeySearchSource.class) {
	 if (license_manager == null) {
	    license_manager = S6Factory.createS6Factory().createLicenseManager();
	  }
       }
      license_uid = license_manager.getLicenseUidFromSource(code);
    }
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

public String getLicenseUid()			{ return license_uid; }

public double getScore()			{ return search_index; }

public String getProjectId()                    { return null; }



abstract public String getName();

abstract public String getDisplayName();

public String getPathName()                     { return getDisplayName(); }




}	// end of abstract class KeySearchSource




/* end of KeySearchSource.java */
