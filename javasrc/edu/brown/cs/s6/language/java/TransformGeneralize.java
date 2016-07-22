/********************************************************************************/
/*										*/
/*		TransformGeneralize.java					*/
/*										*/
/*	Transformation that generalizes external types in a class		*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/language/java/TransformGeneralize.java,v 1.10 2016/07/18 23:05:26 spr Exp $ */


/*********************************************************************************
 *
 * $Log: TransformGeneralize.java,v $
 * Revision 1.10  2016/07/18 23:05:26  spr
 * Update transforms for applications and UI.
 *
 * Revision 1.9  2015/09/23 17:54:53  spr
 * Version to handle andriod UI applications.
 *
 * Revision 1.8  2014/08/29 15:16:09  spr
 * Updates for suise, testcases.
 *
 * Revision 1.7  2013/09/13 20:33:05  spr
 * Add calls for UI search.
 *
 * Revision 1.6  2012-06-11 14:07:49  spr
 * add framework search; fix bugs
 *
 * Revision 1.5  2009-05-12 22:28:48  spr
 * Fix ups to make user context work.
 *
 * Revision 1.4  2008-11-12 13:52:14  spr
 * Performance and bug updates.
 *
 * Revision 1.3  2008-08-28 00:32:56  spr
 * Next version of S6.	Lots of bug fixes, some new functionality.
 *
 * Revision 1.2  2008-07-18 22:27:09  spr
 * Handle remove compilation calls; update transforms to include code to use ASTrewrite.
 *
 * Revision 1.1  2008-07-17 13:46:45  spr
 * Bug fixes and speed ups.
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.language.java;


import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import edu.brown.cs.ivy.jcomp.JcompSymbol;
import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.ivy.jcomp.JcompTyper;
import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6Solution;
import edu.brown.cs.s6.common.S6SolutionSet;



public class TransformGeneralize extends TransformJava implements S6Constants, JavaConstants {




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public TransformGeneralize(String name)
{
   super(name);
}




/********************************************************************************/
/*										*/
/*	Method to handle actual transformations 				*/
/*										*/
/********************************************************************************/

@Override protected boolean applyClassTransform(S6SolutionSet sols,S6Solution sol,
						   TypeDeclaration td,
						   S6Request.ClassSignature csg)
{
   FragmentJava f = (FragmentJava) sol.getFragment();
   Map<JcompType,JcompType> potmaps = new HashMap<JcompType,JcompType>();

   // first compute the set of potential matches

   for (S6Request.MethodSignature msg : csg.getMethods()) {
      for (MethodDeclaration md : td.getMethods()) {
	 findPotentialMatches(md,msg,f,potmaps);
       }
    }

   if (potmaps.size() == 0) return false;

   for (Iterator<Map.Entry<JcompType,JcompType>> it = potmaps.entrySet().iterator(); it.hasNext(); ) {
      Map.Entry<JcompType,JcompType> ent = it.next();
      if (!checkMatch(td,csg,ent.getKey(),ent.getValue())) it.remove();
    }

   if (potmaps.size() == 0) return false;

   boolean chng = false;

   for (Map.Entry<JcompType,JcompType> ent : potmaps.entrySet()) {
      GenMapper gm = new GenMapper(ent.getKey(),ent.getValue());
      if (!usesTransform(sol,gm.getMapName())) {
	 chng |= addNewSolution(sols,sol,gm);
       }
    }

   return chng;
}



@Override protected boolean applyMethodTransform(S6SolutionSet solset,S6Solution sol)
{
   return false;
}



/********************************************************************************/
/*										*/
/*	Find potential generalizations						*/
/*										*/
/********************************************************************************/

private void findPotentialMatches(MethodDeclaration md,S6Request.MethodSignature ms,
				     FragmentJava fj,
				     Map<JcompType,JcompType> potmaps)
{
   List<String> mstyp = ms.getParameterTypeNames();
   List<?> mdtyp = md.parameters();

   if (mstyp.size() == 0 || mstyp.size() != mdtyp.size()) return;

   JcompTyper jt = JavaAst.getTyper(md);
   if (jt == null) return;

   Map<JcompType,JcompType> newtyp = new HashMap<JcompType,JcompType>();

   for (int i = 0; i < mstyp.size(); ++i) {
      JcompType t0 = jt.findSystemType(mstyp.get(i));
      SingleVariableDeclaration svd = (SingleVariableDeclaration) mdtyp.get(i);
      JcompSymbol js = JavaAst.getDefinition(svd);
      JcompType t1 = js.getType();
      if (t0 == null || t1 == null) return;
      if (t0 == t1 || t0.isCompatibleWith(t1)) continue;
      if (t0.isPrimitiveType() || t1.isPrimitiveType()) return;
      if (t1.isArrayType() && !t0.isArrayType()) return;
      if (!t1.isCompatibleWith(t0)) {
	 if (t1.isClassType() && t0.isClassType() && t0.isKnownType() &&
		!t1.isKnownType() && checkNoInternals(fj.getAstNode(),svd)) ;
	 else return;
       }
      if (t1.isKnownType()) continue;
      newtyp.put(t1,t0);
    }

   potmaps.putAll(newtyp);
}



/********************************************************************************/
/*										*/
/*	Check if a generalization is okay					*/
/*										*/
/********************************************************************************/

private boolean checkMatch(TypeDeclaration td,S6Request.ClassSignature csg,JcompType frm,JcompType to)
{
   for (TypeDeclaration std : td.getTypes()) {
      JcompType t0 = JavaAst.getJavaType(std);
      if (t0 == frm) return false;
    }

   for (S6Request.MethodSignature msg : csg.getMethods()) {
      boolean fnd = false;
      for (MethodDeclaration md : td.getMethods()) {
	 if (checkMatch(md,msg,frm,to)) {
	    fnd = true;
	    break;
	  }
       }
      if (!fnd) return false;
    }

   return true;
}



private boolean checkMatch(MethodDeclaration md,S6Request.MethodSignature msg,JcompType frm,JcompType to)
{
   List<String> mstyp = msg.getParameterTypeNames();
   List<?> mdtyp = md.parameters();

   if (mstyp.size() != mdtyp.size()) return false;
   if (mstyp.size() == 0) return true;

   JcompTyper jt = JavaAst.getTyper(md);
   if (jt == null) return false;

   /****** Don't check return types since these might depend on this transform being applied first
   JcompType r0 = jt.findSystemType(msg.getReturnTypeName());
   JcompType r1 = JavaAst.getJcompType(md.getReturnType2());
   if (r0 == null || r1 == null) return false;
   if (!r1.isCompatibleWith(r0)) return false;
   **********/

   for (int i = 0; i < mstyp.size(); ++i) {
      JcompType t0 = jt.findSystemType(mstyp.get(i));
      SingleVariableDeclaration svd = (SingleVariableDeclaration) mdtyp.get(i);
      JcompSymbol js = JavaAst.getDefinition(svd);
      JcompType t1 = js.getType();
      if (t0 == null || t1 == null) return false;
      if (t0 == t1 || t0.isCompatibleWith(t1)) continue;
      if (t0 == to && t1 == frm) {
	 if (!checkNoInternals(md,svd)) return false;
       }
    }

   return true;
}




/********************************************************************************/
/*										*/
/*	Tree mapper for type generalization					*/
/*										*/
/********************************************************************************/

private class GenMapper extends TreeMapper {

   private JcompType from_type;
   private JcompType to_type;

   GenMapper(JcompType frm,JcompType to) {
      from_type = frm;
      to_type = to;
    }

   void rewriteTree(ASTNode orig,ASTRewrite rw) {
      if (orig instanceof Name) {
         JcompType t0 = JavaAst.getJavaType(orig);
         if (t0 == from_type) {
            if (orig.getParent() instanceof TypeParameter) {
               rw.remove(orig.getParent(),null);
             }
            else {
               ASTNode nn = JavaAst.getQualifiedName(orig.getAST(),to_type.getName());
               rw.replace(orig,nn,null);
             }
          }
       }
      else if (orig instanceof Type) {
         JcompType t0 = JavaAst.getJavaType(orig);
         if (t0 == from_type) {
            ASTNode nn = to_type.createAstNode(orig.getAST());
            rw.replace(orig,nn,null);
          }
       }
    }

}	// end of subclass GenMapper




}	// end of class TransformGeneralize




/* end of TransformGeneralize.java */
