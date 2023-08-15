/********************************************************************************/
/*										*/
/*		TextDelta.java							*/
/*										*/
/*	Representation of changes between two text versions			*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/language/TextDelta.java,v 1.2 2015/09/23 17:58:03 spr Exp $ */


/*********************************************************************************
 *
 * $Log: TextDelta.java,v $
 * Revision 1.2  2015/09/23 17:58:03  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.1  2013/09/20 21:02:04  spr
 * Add missing files
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.language;



import java.util.List;

public abstract class TextDelta {




/********************************************************************************/
/*										*/
/*	Creation methods							*/
/*										*/
/********************************************************************************/

public static TextDelta getDelta(String rslt,String orig)
{
   return new TextDelta2(rslt,orig);
}


public static TextDelta getDelta(List<String> rslt,List<String> orig)
{
   return new TextDelta2(rslt,orig);
}



/********************************************************************************/
/*										*/
/*	Application methods							*/
/*										*/
/********************************************************************************/

public abstract String apply(String orig);









}	// end of abstract class TextDelta




/* end of TextDelta.java */
