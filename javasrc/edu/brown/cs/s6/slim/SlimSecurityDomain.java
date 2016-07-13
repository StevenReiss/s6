/********************************************************************************/
/*										*/
/*		SlimSecurityDomain.java 					*/
/*										*/
/*	Security domain for s6 testing						*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/slim/SlimSecurityDomain.java,v 1.4 2013/09/13 20:33:11 spr Exp $ */


/*********************************************************************************
 *
 * $Log: SlimSecurityDomain.java,v $
 * Revision 1.4  2013/09/13 20:33:11  spr
 * Add calls for UI search.
 *
 * Revision 1.3  2008-11-12 13:52:22  spr
 * Performance and bug updates.
 *
 * Revision 1.2  2008-08-28 00:32:59  spr
 * Next version of S6.  Lots of bug fixes, some new functionality.
 *
 * Revision 1.1  2008-06-12 18:33:30  spr
 * Add module for security checking.
 *
 *
 ********************************************************************************/




package edu.brown.cs.s6.slim;

import java.security.Permission;


class SlimSecurityDomain implements SlimConstants {



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private SlimPermissionSet	permission_set;
private SlimSecurityDomain	previous_domain;
private Permission		local_permission;
private String			domain_id;

private static int domain_counter = 0;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

SlimSecurityDomain(SlimSecurityDomain prv)
{
   permission_set = new SlimPermissionSet();
   previous_domain = prv;
   local_permission = new SlimSecurityPermission(this);
   domain_id = "SLIM_" + (++domain_counter);
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

Permission getLocalPermission() 		{ return local_permission; }

String getId()					{ return domain_id; }



void addPermission(SlimPermission rp)
{
   permission_set.addPermission(rp);
}



/********************************************************************************/
/*										*/
/*	Checking methods							*/
/*										*/
/********************************************************************************/

boolean checkPermission(Permission p)
{
   // System.err.println("CHECK " + p + " " + permission_set.allows(p));

   Boolean fg = permission_set.allows(p);
   if (fg == Boolean.FALSE) return false;

   if (previous_domain == null) return true;

   return previous_domain.checkPermission(p);
}



/********************************************************************************/
/*										*/
/*	Local permission for checking against context				*/
/*										*/
/********************************************************************************/

private static class SlimSecurityPermission extends Permission {

   private static final long serialVersionUID = 1;


   SlimSecurityPermission(SlimSecurityDomain rd) {
      super("Security:" + rd.getId());
    }

   public boolean equals(Object o) {
      return o == this;
    }

   public int hashCode() {
      return System.identityHashCode(this);
    }

   public String getActions()			{ return "check"; }

   public boolean implies(Permission p) {
      if (p == this) return true;
      return false;
    }

}	// end of subclass SlimSecurityPermission



}	// end of class SlimSecurityDomain




/* end of SlimSecurityDomain.java */

