/********************************************************************************/
/*										*/
/*		SlimPermissionSet.java						*/
/*										*/
/*	General class representing a set of Slim permissions			*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/slim/SlimPermissionSet.java,v 1.2 2008-11-12 13:52:22 spr Exp $ */


/*********************************************************************************
 *
 * $Log: SlimPermissionSet.java,v $
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
import java.util.ArrayList;
import java.util.List;


class SlimPermissionSet implements SlimConstants {



/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private List<SlimPermission> permission_list;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

SlimPermissionSet()
{
   permission_list = new ArrayList<SlimPermission>();
}



/********************************************************************************/
/*										*/
/*	Setup methods								*/
/*										*/
/********************************************************************************/

void addPermission(SlimPermission rp)
{
   permission_list.add(rp);
}



/********************************************************************************/
/*										*/
/*	Testing methods 							*/
/*										*/
/********************************************************************************/

Boolean allows(Permission p)
{
   SlimPermission best = null;

   for (SlimPermission rp : permission_list) {
      best = rp.testMatch(p,best);
    }

   if (best == null) return null;

   return best.allows(p);
}



}	// end of class SlimPermissionSet




/* end of SlimPermissionSet.java */

