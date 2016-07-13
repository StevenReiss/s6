/********************************************************************************/
/*										*/
/*		JavaAstClassName.java						*/
/*										*/
/*	AST node and support for the current class name 			*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/language/java/JavaAstClassName.java,v 1.6 2015/09/23 17:54:52 spr Exp $ */


/*********************************************************************************
 *
 * $Log: JavaAstClassName.java,v $
 * Revision 1.6  2015/09/23 17:54:52  spr
 * Version to handle andriod UI applications.
 *
 * Revision 1.5  2013/09/13 20:33:03  spr
 * Add calls for UI search.
 *
 * Revision 1.4  2012-06-11 14:07:48  spr
 * add framework search; fix bugs
 *
 * Revision 1.3  2008-11-12 13:52:14  spr
 * Performance and bug updates.
 *
 * Revision 1.2  2008-06-12 17:47:52  spr
 * Next version of S6.
 *
 * Revision 1.1.1.1  2008-06-03 12:59:23  spr
 * Initial version of S6
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.language.java;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SimpleName;

import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.s6.common.S6Constants;




class JavaAstClassName implements S6Constants, JavaConstants {



private String	package_name;
private String	orig_name;
private List<Name> changed_names;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

JavaAstClassName(JcompType jt)
{
   package_name = null;
   changed_names = new ArrayList<Name>();
}




/********************************************************************************/
/*										*/
/*	Methods to create a name node						*/
/*										*/
/********************************************************************************/

void noteName(Name n)
{
   changed_names.add(n);

   if (orig_name == null) {
      orig_name = n.getFullyQualifiedName();
      int idx = orig_name.lastIndexOf(".");
      if (idx > 0) orig_name = orig_name.substring(idx+1);
    }
}


void setPackage(String nm)
{
   package_name = nm;
}


void clearNames()
{
   changed_names.clear();
   orig_name = null;
}



/********************************************************************************/
/*										*/
/*	Methods to update names 						*/
/*										*/
/********************************************************************************/

void setClassName(String pkg,String cls)
{
   if (cls == null) return;

   for (Name n : changed_names) {
      if (n instanceof SimpleName) {
	 SimpleName sn = (SimpleName) n;
	 sn.setIdentifier(cls);
       }
      else {
	 // handle qualified names
       }
    }
}


void resetClassName()
{
   setClassName(package_name,orig_name);
}



}	// end of class JavaAstClassName



/* end of JavaAstClassName.java */
