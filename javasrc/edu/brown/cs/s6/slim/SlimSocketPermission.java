/********************************************************************************/
/*										*/
/*		SlimSocketPermission.java					*/
/*										*/
/*	Socket permission for S6 testing					*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/slim/SlimSocketPermission.java,v 1.4 2015/09/23 17:58:12 spr Exp $ */


/*********************************************************************************
 *
 * $Log: SlimSocketPermission.java,v $
 * Revision 1.4  2015/09/23 17:58:12  spr
 * Updates for Andriod UI and better keysearch.
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

import java.net.SocketPermission;
import java.security.Permission;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;


public class SlimSocketPermission extends SlimPermission {




/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private String host_path;
private Set<String> operation_set;
private SocketPermission dummy_permission;

private static Set<String> valid_ops;

static {
   valid_ops = new HashSet<String>();
   valid_ops.add("accept");
   valid_ops.add("connect");
   valid_ops.add("listen");
   valid_ops.add("resolve");
   valid_ops.add("none");
}



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public SlimSocketPermission(String host,String ops)
{
   host_path = host;
   operation_set = new HashSet<String>();
   dummy_permission = new SocketPermission(host,"accept");

   if (ops != null) {
      for (StringTokenizer tok = new StringTokenizer(ops,", "); tok.hasMoreTokens(); ) {
	 String t = tok.nextToken().toLowerCase();
	 if (!valid_ops.contains(t)) throw new Error("Bad permission operator: " + t);
	 operation_set.add(t);
       }
    }
}



/********************************************************************************/
/*										*/
/*	Relevance methods							*/
/*										*/
/********************************************************************************/

SlimPermission testMatch(Permission p,SlimPermission best)
{
   if (!(p instanceof SocketPermission)) return best;

   if (!dummy_permission.implies(new SocketPermission(p.getName(),"accept"))) return best;

   if (best != null && best instanceof SlimSocketPermission) {
      SlimSocketPermission rfp = (SlimSocketPermission) best;
      if (!isMoreSpecificThan(rfp)) return rfp;
    }

   return this;
}



Boolean allows(Permission p)
{
   if (!(p instanceof SocketPermission)) return null;

   if (!dummy_permission.implies(new SocketPermission(p.getName(),"read"))) return null;

   String acts = p.getActions();
   for (StringTokenizer tok = new StringTokenizer(acts,","); tok.hasMoreTokens(); ) {
      String act = tok.nextToken();
      if (!operation_set.contains(act)) return Boolean.FALSE;
    }

   return Boolean.TRUE;
}




private boolean isMoreSpecificThan(SlimSocketPermission rfp)
{
   if (rfp.dummy_permission.implies(dummy_permission)) return true;
   if (dummy_permission.implies(rfp.dummy_permission)) return false;

   if (host_path.length() >= rfp.host_path.length()) return true;

   return false;
}




/********************************************************************************/
/*										*/
/*	Output methods								*/
/*										*/
/********************************************************************************/

public String toString()
{
   StringBuffer buf = new StringBuffer();
   buf.append("SlimSocketPermission(" + host_path + "::");
   int ct = 0;
   for (String s : operation_set) {
      if (ct++ != 0) buf.append(",");
      buf.append(s);
    }
   buf.append(")");

   return buf.toString();
}




}	// end of class SlimSocketPermission




/* end of SlimSocketPermission.java */


