/********************************************************************************/
/*										*/
/*		ContextConstants.java						*/
/*										*/
/*	Constants for creating an S6 context					*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/context/ContextConstants.java,v 1.2 2015/02/19 03:14:42 spr Exp $ */


/*********************************************************************************
 *
 * $Log: ContextConstants.java,v $
 * Revision 1.2  2015/02/19 03:14:42  spr
 * Ensure current
 *
 * Revision 1.1  2012-06-11 14:07:28  spr
 * Code cleanup
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.context;


import edu.brown.cs.s6.common.S6Constants;




interface ContextConstants extends S6Constants {



/********************************************************************************/
/*										*/
/*	File locations								*/
/*										*/
/********************************************************************************/

String	S6_DIRECTORY = "/s6/";
String	S6_WINDOWS = "s:";

String CONTEXT_USER_JAR_NAME = "6sUserFile_";



}	// end of interface ContextConstants




/* end of ContextConstants.java */

