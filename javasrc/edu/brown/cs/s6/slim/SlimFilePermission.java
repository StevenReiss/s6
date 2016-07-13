/********************************************************************************/
/*										*/
/*		SlimFilePermission.java 					*/
/*										*/
/*	File permission for S6 testing						*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/slim/SlimFilePermission.java,v 1.4 2015/09/23 17:58:12 spr Exp $ */


/*********************************************************************************
 *
 * $Log: SlimFilePermission.java,v $
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

import java.io.FilePermission;
import java.security.Permission;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;


public class SlimFilePermission extends SlimPermission {




/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private String file_path;
private Set<String> operation_set;
private FilePermission dummy_permission;

private static Set<String> valid_ops;

private static String ALL_FILES = "<<ALL FILES>>";



static {
   valid_ops = new HashSet<String>();
   valid_ops.add("read");
   valid_ops.add("write");
   valid_ops.add("execute");
   valid_ops.add("delete");
   valid_ops.add("none");
}



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public SlimFilePermission(String path,String ops)
{
   file_path = path;
   operation_set = new HashSet<String>();
   dummy_permission = new FilePermission(path,"read");

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
   if (!(p instanceof FilePermission)) return best;

   if (!dummy_permission.implies(new FilePermission(p.getName(),"read"))) return best;

   if (best != null && best instanceof SlimFilePermission) {
      SlimFilePermission rfp = (SlimFilePermission) best;
      if (!isMoreSpecificThan(rfp)) return rfp;
    }

   return this;
}



Boolean allows(Permission p)
{
   if (!(p instanceof FilePermission)) return null;

   if (!dummy_permission.implies(new FilePermission(p.getName(),"read"))) return null;

   String acts = p.getActions();
   for (StringTokenizer tok = new StringTokenizer(acts,","); tok.hasMoreTokens(); ) {
      String act = tok.nextToken();
      if (!operation_set.contains(act)) return Boolean.FALSE;
    }

   return Boolean.TRUE;
}




private boolean isMoreSpecificThan(SlimFilePermission rfp)
{
   if (file_path.equals(ALL_FILES)) return false;
   if (rfp.file_path.equals(ALL_FILES)) return true;
   if (file_path.length() >= rfp.file_path.length()) return true;

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
   buf.append("SlimFilePermission(" + file_path + "::");
   int ct = 0;
   for (String s : operation_set) {
      if (ct++ != 0) buf.append(",");
      buf.append(s);
    }
   buf.append(")");

   return buf.toString();
}




}	// end of class SlimFilePermission




/* end of SlimFilePermission.java */

