/********************************************************************************/
/*										*/
/*		SlimPermission.java						*/
/*										*/
/*	General class representing a S6 testing permission			*/
/*										*/
/********************************************************************************/
/*	Copyright 2003 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2003, Brown University, Providence, RI.				 *
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/slim/SlimPermission.java,v 1.2 2008-11-12 13:52:22 spr Exp $ */


/*********************************************************************************
 *
 * $Log: SlimPermission.java,v $
 * Revision 1.2  2008-11-12 13:52:22  spr
 * Performance and bug updates.
 *
 * Revision 1.1  2008-06-12 18:33:30  spr
 * Add module for security checking.
 *
 *
 ********************************************************************************/




package edu.brown.cs.s6.slim;

import java.security.Permission;


public abstract class SlimPermission implements SlimConstants {



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

protected SlimPermission()
{ }




/********************************************************************************/
/*										*/
/*	Checking methods							*/
/*										*/
/********************************************************************************/

/**
 *	Check whether this permission is relevant to a given Java permission.
 *	If it is, then check if it is more specific than the permission specified
 *	by the second argument (if it is non-null).  Return the most specific
 *	relevant permission, either the current one or the best passed in.
 **/

abstract SlimPermission testMatch(Permission p,SlimPermission best);



/**
 *	Check whether this SlimPermission allows the corresponding Java
 *	permission.  Returns Boolean.TRUE or Boolean.FALSE if known true or false;
 *	returns null if not relevant.
 **/

abstract Boolean allows(Permission p);



}	// end of abstract class SlimPermission




/* end of SlimPermission.java */

