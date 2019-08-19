/********************************************************************************/
/*										*/
/*		ContextEclipse.java						*/
/*										*/
/*	Eclipse interface for creating and S6 Context				*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/context/ContextEclipse.java,v 1.3 2015/09/23 17:57:52 spr Exp $ */


/*********************************************************************************
 *
 * $Log: ContextEclipse.java,v $
 * Revision 1.3  2015/09/23 17:57:52  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.2  2015/02/19 03:14:42  spr
 * Ensure current
 *
 * Revision 1.1  2012-06-11 14:07:28  spr
 * Code cleanup
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.context;


import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.w3c.dom.Element;

import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.s6.common.S6Constants;




class ContextEclipse implements ContextConstants, S6Constants
{


/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private File		current_workspace;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

ContextEclipse()
{
   current_workspace = null;
}



/********************************************************************************/
/*										*/
/*	Workspace methods							*/
/*										*/
/********************************************************************************/

void setWorkspace(File f)
{
   if (!f.exists() || !f.isDirectory()) f = null;
   current_workspace = f;
}



Collection<String> getProjects()
{
   if (current_workspace == null) return null;

   List<String> rslt = new ArrayList<String>();

   File [] fls = current_workspace.listFiles();
   if (fls != null) {
      for (File f : fls) {
	 if (f.isDirectory()) {
	    File [] pfls = f.listFiles();
	    if (pfls == null) continue;
	    int fnd = 0;
	    for (File pf : pfls) {
	       if (pf.getName().equals(".project") || pf.getName().equals(".classpath")) ++fnd;
	     }
	    if (fnd == 2) rslt.add(f.getName());
	  }
       }
    }

   return rslt;
}




/********************************************************************************/
/*										*/
/*	Classpath methods							*/
/*										*/
/********************************************************************************/

Collection<File> getClassPath(String proj)
{
   Collection<File> rslt = new ArrayList<File>();

   if (current_workspace == null) return rslt;

   File pf = new File(current_workspace,proj);
   File cpf = new File(pf,".classpath");
   if (!cpf.exists()) return rslt;

   Element e = IvyXml.loadXmlFromFile(cpf);
   if (e == null) return rslt;

   for (Element ent : IvyXml.children(e,"classpathentry")) {
      String k = IvyXml.getAttrString(ent,"kind");
      if (k.equals("lib")) {
	 String lib = IvyXml.getAttrString(ent,"path");
	 File f = new File(lib);
	 if (f.exists()) rslt.add(f);
       }
      else if (k.equals("output")) {
	 String out = IvyXml.getAttrString(ent,"path");
	 File f = null;
	 if (out.startsWith("/") || out.startsWith("\\")) f = new File(out);
	 else f = new File(pf,out);
	 if (f.exists()) rslt.add(f);
       }
    }

   return rslt;
}




}	// end of class ContextEclipse




/* end of ContextEclipse.java */
