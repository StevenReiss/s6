/********************************************************************************/
/*										*/
/*		S6Security.java 						*/
/*										*/
/*	Definitions for security checking					*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/common/S6Security.java,v 1.2 2008-11-12 13:50:47 spr Exp $ */


/*********************************************************************************
 *
 * $Log: S6Security.java,v $
 * Revision 1.2  2008-11-12 13:50:47  spr
 * Performance and bug updates.
 *
 * Revision 1.1  2008-06-12 17:47:46  spr
 * Next version of S6.
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.common;


import java.util.List;



public interface S6Security extends S6Constants {


List<Permit> getPermissions();
boolean isEmpty();


public interface Permit {

   public S6SecurityType getType();
   public String getArgument();
   public String getOperations();

}	// end of subinterface Permit




}	// end of interface S6Security




/* end of S6Security.java */
