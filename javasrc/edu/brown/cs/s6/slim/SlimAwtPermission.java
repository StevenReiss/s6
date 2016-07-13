/********************************************************************************/
/*										*/
/*		SlimAwtPermission.java						*/
/*										*/
/*	Awt permission for S6 testing						*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/slim/SlimAwtPermission.java,v 1.4 2015/09/23 17:58:12 spr Exp $ */


/*********************************************************************************
 *
 * $Log: SlimAwtPermission.java,v $
 * Revision 1.4  2015/09/23 17:58:12  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.3  2008-11-12 13:52:22  spr
 * Performance and bug updates.
 *
 * Revision 1.2  2008-08-28 00:32:59  spr
 * Next version of S6.  Lots of bug fixes, some new functionality.
 *
 * Revision 1.1  2008-06-12 18:33:29  spr
 * Add module for security checking.
 *
 *
 ********************************************************************************/




package edu.brown.cs.s6.slim;

import java.awt.AWTPermission;
import java.security.Permission;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;


public class SlimAwtPermission extends SlimPermission {




/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private Set<String> operation_set;

private static Set<String> valid_ops;

private static Map<String,String> op_map;

static {
   valid_ops = new HashSet<String>();
   valid_ops.add("clipboard");
   valid_ops.add("events");
   valid_ops.add("system_access");

   op_map = new HashMap<String,String>();
   op_map.put("accessClipboard","clipboard");
   op_map.put("accessEventQueue","events");
   op_map.put("accessSystemTray","system_access");
   op_map.put("createRobot","system_access");
   op_map.put("fullScreenExclusive","system_access");
   op_map.put("listenToAllAWTEvents","system_access");
   op_map.put("readDisplayPixels","system_access");
   op_map.put("replaceKeyboardFocusManager","system_access");
   op_map.put("setAppletStub","system_access");
   op_map.put("setWindowAlwaysOnTop","system_access");
   op_map.put("showWindowWithoutWarningBanner","system_access");
   op_map.put("toolkitModality","system_access");
   op_map.put("watchMousePointer","system_access");
}




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public SlimAwtPermission(String ops)
{
   operation_set = new HashSet<String>();

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
   if (!(p instanceof AWTPermission)) return best;

   return this;
}



Boolean allows(Permission p)
{
   if (!(p instanceof AWTPermission)) return null;

   String op = op_map.get(p.getName());
   if (op == null) op = "system_access";

   if (!operation_set.contains(op)) return Boolean.FALSE;

   return Boolean.TRUE;
}




/********************************************************************************/
/*										*/
/*	Output methods								*/
/*										*/
/********************************************************************************/

public String toString()
{
   StringBuffer buf = new StringBuffer();
   buf.append("SlimAwtPermission(");
   int ct = 0;
   for (String s : operation_set) {
      if (ct++ != 0) buf.append(",");
      buf.append(s);
    }
   buf.append(")");

   return buf.toString();
}




}	// end of class SlimAwtPermission




/* end of SlimAwtPermission.java */




