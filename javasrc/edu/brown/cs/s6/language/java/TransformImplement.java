/********************************************************************************/
/*										*/
/*		TransformImplement.java 					*/
/*										*/
/*	Transformation that handles removing spurious implements clauses	*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/language/java/TransformImplement.java,v 1.14 2015/09/23 17:54:53 spr Exp $ */


/*********************************************************************************
 *
 * $Log: TransformImplement.java,v $
 * Revision 1.14  2015/09/23 17:54:53  spr
 * Version to handle andriod UI applications.
 *
 * Revision 1.13  2015/02/14 19:40:19  spr
 * Add test case generation.
 *
 * Revision 1.12  2014/08/29 15:16:09  spr
 * Updates for suise, testcases.
 *
 * Revision 1.11  2013/09/13 20:33:05  spr
 * Add calls for UI search.
 *
 * Revision 1.10  2013-05-09 12:26:21  spr
 * Minor changes to start ui fixups.
 *
 * Revision 1.9  2012-06-11 18:18:28  spr
 * Include changed/new files for package/ui search
 *
 * Revision 1.8  2012-06-11 14:07:49  spr
 * add framework search; fix bugs
 *
 * Revision 1.7  2009-05-12 22:28:48  spr
 * Fix ups to make user context work.
 *
 * Revision 1.6  2008-11-12 13:52:14  spr
 * Performance and bug updates.
 *
 * Revision 1.5  2008-08-28 00:32:56  spr
 * Next version of S6.	Lots of bug fixes, some new functionality.
 *
 * Revision 1.4  2008-07-18 22:27:09  spr
 * Handle remove compilation calls; update transforms to include code to use ASTrewrite.
 *
 * Revision 1.3  2008-07-17 13:46:46  spr
 * Bug fixes and speed ups.
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


import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6Solution;
import edu.brown.cs.s6.common.S6SolutionSet;



public class TransformImplement extends TransformJava implements S6Constants, JavaConstants {




/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private static boolean allow_known_supertypes = false;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public TransformImplement(String name)
{
   super(name);
}



/********************************************************************************/
/*										*/
/*	Method to create Mapper to do actual type changes			*/
/*										*/
/********************************************************************************/

@Override protected boolean applyClassTransform(S6SolutionSet sols,S6Solution sol,
						   TypeDeclaration td,
						   S6Request.ClassSignature csg)
{
   // see if csg expects any implements clauses
   boolean chng = false;
   Type sup = td.getSuperclassType();
   List<?> ints = td.superInterfaceTypes();

   boolean known = allow_known_supertypes;
   known |= sols.getSearchType() == S6SearchType.PACKAGE;
   known |= sols.getSearchType() == S6SearchType.APPLICATION;
   known |= sols.getSearchType() == S6SearchType.UIFRAMEWORK;
   known |= sols.getSearchType() == S6SearchType.ANDROIDUI;

   if (sup != null && known) {
      JcompType jt = JavaAst.getJavaType(sup);
      if (jt != null && jt.isKnownType()) sup = null;
    }
   
   if (sup == null && ints.size() == 0) return false;
   
   switch (sols.getSearchType()) {
      case PACKAGE :
      case UIFRAMEWORK :
      case ANDROIDUI :
      case APPLICATION :
	 String sgnm = csg.getName();
	 String xnm = td.getName().getIdentifier();
	 if (sgnm != null && !sgnm.equals(xnm)) return false;
	 break;
      default :
	 break;
    }
   
   boolean rqd = false;
   if (sup != null) {
      if (csg.getSuperClass() == null) rqd = true;
      else {
         JcompType jt = JavaAst.getJavaType(sup);
         if (!csg.getSuperClass().equals(jt.getName())) rqd = true;
       }
    }
   if (ints.size() > 0) {
      for (Object o : ints) {
         ASTNode n = (ASTNode) o;
         JcompType jt = JavaAst.getJavaType(n);
         if (!csg.getInterfaces().contains(jt.getName())) rqd = true;
       }
    }
   if (!rqd) return false;

   TreeMapper tm = new ImplementMapper(csg,td,known);

   if (!usesTransform(sol,tm.getMapName())) {
      chng |= addNewSolution(sols,sol,tm);
    }

   return chng;
}



/********************************************************************************/
/*										*/
/*	Class to handle mappings						*/
/*										*/
/********************************************************************************/

private class ImplementMapper extends TreeMapper {

   private S6Request.ClassSignature class_signature;
   private TypeDeclaration change_type;
   private boolean allow_known;

   ImplementMapper(S6Request.ClassSignature csg,TypeDeclaration td,boolean kn) {
      class_signature = csg;
      change_type = td;
      allow_known = kn;
    }

   @Override protected String getSpecificsName() {
      return class_signature.getName();
   }

   void rewriteTree(ASTNode orig,ASTRewrite rw) {
      if (orig != change_type) return;
   
      boolean chng = false;
      TypeDeclaration otd = (TypeDeclaration) orig;
   
      Type st = otd.getSuperclassType();
      if (st != null) {
         JcompType jt = JavaAst.getJavaType(st);
         String supnm = class_signature.getSuperClass();
         if (!jt.getName().equals(supnm)) {
            if (jt == null || !jt.isKnownType() || !allow_known) {
               rw.set(otd,TypeDeclaration.SUPERCLASS_TYPE_PROPERTY,null,null);
               chng = true;
             }
          }
       }
   
      ListRewrite lrw = rw.getListRewrite(otd,TypeDeclaration.SUPER_INTERFACE_TYPES_PROPERTY);
      List<?> oints = otd.superInterfaceTypes();
      List<String> ifcs = class_signature.getInterfaces();
      for (Iterator<?> it = oints.iterator(); it.hasNext(); ) {
         ASTNode n = (ASTNode) it.next();
         JcompType jt = JavaAst.getJavaType(n);
         if (jt != null && ifcs.contains(jt.getName())) continue;
         lrw.remove(n,null);
         chng = true;
       }
   
      if (chng) {
         for (MethodDeclaration md : otd.getMethods()) {
            for (Object o : md.modifiers()) {
               IExtendedModifier mod = (IExtendedModifier) o;
               if (mod.isAnnotation()) {
        	  Annotation ann = (Annotation) mod;
        	  String tnm = ann.getTypeName().getFullyQualifiedName();
        	  if (tnm.contains("Overrides")) {
        	     ListRewrite mlrw = rw.getListRewrite(md,MethodDeclaration.MODIFIERS2_PROPERTY);
        	     mlrw.remove(ann,null);
        	     break;
        	   }
        	}
             }
          }
       }
    }

}	// end of subtype ImplementMapper




}	// end of class TransformImplement



/* end of TransformImplement.java */
