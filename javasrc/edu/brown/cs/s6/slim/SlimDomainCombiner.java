/********************************************************************************/
/*										*/
/*		SlimDomainCombiner.java 					*/
/*										*/
/*	Domain combiner for S6 security management				*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/slim/SlimDomainCombiner.java,v 1.2 2008-11-12 13:52:22 spr Exp $ */


/*********************************************************************************
 *
 * $Log: SlimDomainCombiner.java,v $
 * Revision 1.2  2008-11-12 13:52:22  spr
 * Performance and bug updates.
 *
 * Revision 1.1  2008-06-12 18:33:29  spr
 * Add module for security checking.
 *
 *
 ********************************************************************************/




package edu.brown.cs.s6.slim;

import java.security.DomainCombiner;
import java.security.ProtectionDomain;


@SuppressWarnings("removal")
class SlimDomainCombiner implements DomainCombiner, SlimConstants {



/********************************************************************************/
/*										*/
/*	Contructors								*/
/*										*/
/********************************************************************************/

SlimDomainCombiner()
{ }




/********************************************************************************/
/*										*/
/*	Combining methods							*/
/*										*/
/********************************************************************************/

public ProtectionDomain [] combine(ProtectionDomain [] cur,ProtectionDomain [] asg)
{
   ProtectionDomain [] rslt;

   if (cur.length == 1 && cur[0].getClassLoader() != null) {
      rslt = asg;
    }
   else {
      rslt = new ProtectionDomain[cur.length + asg.length];
      int ct = 0;
      for (int i = 0; i < cur.length; ++i) rslt[ct++] = cur[i];
      for (int i = 0; i < asg.length; ++i) rslt[ct++] = asg[i];
    }

   return rslt;
}




}	// end of class SlimDomainCombiner




/* end of SlimDomainCombiner.java */

