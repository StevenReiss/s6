/********************************************************************************/
/*										*/
/*		SlimRuntimePermission.java					*/
/*										*/
/*	Runtime permission for S6 testing					*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/slim/SlimRuntimePermission.java,v 1.4 2015/09/23 17:58:12 spr Exp $ */


/*********************************************************************************
 *
 * $Log: SlimRuntimePermission.java,v $
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

import java.io.SerializablePermission;
import java.lang.reflect.ReflectPermission;
import java.security.Permission;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;



public class SlimRuntimePermission extends SlimPermission {




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
   valid_ops.add("class_loader");
   valid_ops.add("security");
   valid_ops.add("environment");
   valid_ops.add("exit");
   valid_ops.add("system_io");
   valid_ops.add("threads");
   valid_ops.add("native_code");
   valid_ops.add("reflection");
   valid_ops.add("stack");
   valid_ops.add("preferences");
   valid_ops.add("serialize");

   op_map = new HashMap<String,String>();
   op_map.put("createClassLoader","class_loader");
   op_map.put("getClassLoader","class_loader");
   op_map.put("setContextClassLoader","class_loader");
   op_map.put("enableContextClassLoaderOverride","class_loader");
   op_map.put("setSecurityManager","security");
   op_map.put("createSecurityManager","security");
   op_map.put("getenv","environment");
   op_map.put("exitVM","NONE");
   op_map.put("shutdownHooks","exit");
   op_map.put("setFactory","system_io");
   op_map.put("setIO","system_io");
   op_map.put("modifyThread","thread");
   op_map.put("stopThread","thread");
   op_map.put("modifyThreadGroup","thread");
   op_map.put("getProtectionDomain","security");
   op_map.put("readFileDescriptor","system_io");
   op_map.put("writeFileDescriptor","system_io");
   op_map.put("loadLibrary","native_code");
   op_map.put("accessClassInPackage","reflection");
   op_map.put("accessDeclaredMembers","reflection");
   op_map.put("queuePrintJob","system_io");
   op_map.put("getStackTrace","stack");
   op_map.put("setDefaultUncaughtExceptionHandler","NONE");
   op_map.put("preferences","preferences");
}




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public SlimRuntimePermission(String ops)
{
   operation_set = new HashSet<String>();

   operation_set.add("ALL");
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
   if (!(p instanceof RuntimePermission) &&
	  !(p instanceof ReflectPermission) &&
	  !(p instanceof SerializablePermission)) return best;

   return this;
}



Boolean allows(Permission p)
{
   String op = null;

   if (p instanceof ReflectPermission) op = "reflection";
   else if (p instanceof SerializablePermission) op = "serialize";
   else if (p instanceof RuntimePermission) {
      String nm = p.getName();
      int idx = nm.indexOf(".");
      if (idx > 0) nm = nm.substring(0,idx);
      op = op_map.get(p.getName());
    }
   if (op == null) return null;

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
   buf.append("SlimRuntimePermission(");
   int ct = 0;
   for (String s : operation_set) {
      if (ct++ != 0) buf.append(",");
      buf.append(s);
    }
   buf.append(")");

   return buf.toString();
}




}	// end of class SlimRuntimePermission




/* end of SlimRuntimePermission.java */





