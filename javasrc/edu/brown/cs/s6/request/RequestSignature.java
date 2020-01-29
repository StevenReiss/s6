/********************************************************************************/
/*										*/
/*		RequestSignature.java						*/
/*										*/
/*	Abstract class for signature information				*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/request/RequestSignature.java,v 1.3 2014/08/29 15:16:13 spr Exp $ */


/*********************************************************************************
 *
 * $Log: RequestSignature.java,v $
 * Revision 1.3  2014/08/29 15:16:13  spr
 * Updates for suise, testcases.
 *
 * Revision 1.2  2008-07-17 13:47:05  spr
 * Add contracts, augment request information in general.
 *
 * Revision 1.1.1.1  2008-06-03 12:59:22  spr
 * Initial version of S6
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.request;


import edu.brown.cs.cose.cosecommon.CoseSignature;
import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Request;



abstract class RequestSignature implements S6Request.Signature, S6Constants, RequestConstants, CoseSignature {




/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

protected RequestSignature()
{ }


/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

abstract public String getName();

abstract public S6Request.MethodSignature getMethod(String name);

public S6Request.MethodSignature getMethodSignature()           { return null; }

public S6Request.ClassSignature getClassSignature()             { return null; }

public String getPackage()                                      { return null; }



}	// end of class RequestSignature




/* end of RequestSignature.java */
