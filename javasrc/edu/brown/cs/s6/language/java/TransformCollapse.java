/********************************************************************************/
/*										*/
/*		TransformCollapse.java						*/
/*										*/
/*	Transformation that collapsing subclasses into classes			*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/language/java/TransformCollapse.java,v 1.3 2015/09/23 17:54:53 spr Exp $ */


/*********************************************************************************
 *
 * $Log: TransformCollapse.java,v $
 * Revision 1.3  2015/09/23 17:54:53  spr
 * Version to handle andriod UI applications.
 *
 * Revision 1.2  2013/09/13 20:33:04  spr
 * Add calls for UI search.
 *
 * Revision 1.1  2012-06-11 18:18:28  spr
 * Include changed/new files for package/ui search
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.language.java;


import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6Solution;
import edu.brown.cs.s6.common.S6SolutionSet;



public class TransformCollapse extends TransformJava implements S6Constants, JavaConstants {




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public TransformCollapse(String name)
{
   super(name);
}


/********************************************************************************/
/*										*/
/*	Handle package mappings 						*/
/*										*/
/********************************************************************************/

@Override protected Collection<TreeMapper> findPackageMappings(S6SolutionSet sols,S6Solution sol,
      CompilationUnit cu,
      S6Request.PackageSignature psg)
{
   // should be applied before name mappings
   String chk = null;
   for (S6Request.ClassSignature csg : psg.getClasses()) {
      chk = psg.getName() + "." + csg.getName();
      break;
    }
   Map<String,TypeDeclaration> typs = new HashMap<String,TypeDeclaration>();
   for (Object o : cu.types()) {
      TypeDeclaration td = (TypeDeclaration) o;
      String tnm = td.getName().getFullyQualifiedName();
      if (tnm.equals(chk)) return null;
      typs.put(tnm,td);
    }
   Map<TypeDeclaration,TypeDeclaration> cands = new HashMap<TypeDeclaration,TypeDeclaration>();
   for (TypeDeclaration td : typs.values()) {
      while (td != null) {
         Type tdt = td.getSuperclassType();
         if (tdt == null || !(tdt instanceof SimpleType)) break;
         SimpleType stt = (SimpleType) tdt;
	 Name nm = stt.getName();
	 if (nm == null) break;
	 String snm = nm.getFullyQualifiedName();
	 TypeDeclaration std = typs.get(snm);
	 if (std == null) break;
	 cands.put(td,std);
	 td = std;
       }
    }
   if (cands.size() == 0) return null;


   return null;
}




}	// end of class TransformCollapse




/* end of TransformCollapse.java */
