/********************************************************************************/
/*										*/
/*		TransformFixVariables.java					*/
/*										*/
/*	Fix variables without assignments					*/
/*										*/
/********************************************************************************/
/*	Copyright 2013 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2013, Brown University, Providence, RI.				 *
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



package edu.brown.cs.s6.language.java;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import edu.brown.cs.ivy.jcomp.JcompSymbol;
import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6Solution;
import edu.brown.cs.s6.common.S6SolutionSet;

public class TransformFixVariables extends TransformJava implements S6Constants, JavaConstants
{


/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public TransformFixVariables(String name)
{
   super(name);
}



/********************************************************************************/
/*										*/
/*	Top-level checking methods						*/
/*										*/
/********************************************************************************/

@Override protected List<TreeMapper> findClassMappings(S6SolutionSet sols,
      TypeDeclaration td,
      S6Request.ClassSignature csg,S6Solution sol)
{
   if (!JavaAst.checkTypeSignature(td,csg,S6SignatureType.FULL,null)) return null;

   return findMapping(td);
}



@Override protected boolean checkApplyMethodForClass(S6SolutionSet sols,
      S6Solution sol,TypeDeclaration td,S6Request.MethodSignature msg,
      MethodDeclaration md)
{
   return false;
}



@Override protected List<TreeMapper> findMethodMappings(S6SolutionSet sols,
      MethodDeclaration md,S6Request.MethodSignature msg,S6Solution sol)
{
   return findMapping(md);
}




private List<TreeMapper> findMapping(ASTNode n)
{
   VarFinder vf = new VarFinder();
   n.accept(vf);
   Set<ASTNode> fix = vf.getVariablesToFix();
   if (fix.isEmpty()) return null;

   List<TreeMapper> rslt = new ArrayList<TreeMapper>();
   TreeMapper tm = new VarFixup(fix,false);
   rslt.add(tm);

   boolean needed = false;
   for (ASTNode an : fix) {
      if (an instanceof VariableDeclarationFragment) {
	 VariableDeclarationFragment vdf = (VariableDeclarationFragment) an;
	 JcompSymbol def = JavaAst.getDefinition(vdf.getName());
	 JcompType jt = def.getType();
	 if (!jt.isAbstract() && !jt.isPrimitiveType()) needed = true;
       }
    }
   if (needed) {
      tm = new VarFixup(fix,true);
      rslt.add(tm);
    }

   return rslt;
}




/********************************************************************************/
/*										*/
/*	Methods to find variables to fix					*/
/*										*/
/********************************************************************************/

private static class VarFinder extends ASTVisitor {

   private Set<JcompSymbol> assigned_vars;
   private Set<JcompSymbol> used_vars;
   private Map<JcompSymbol,ASTNode> var_defs;
   private boolean is_assign;

   VarFinder() {
      assigned_vars = new HashSet<JcompSymbol>();
      used_vars = new HashSet<JcompSymbol>();
      var_defs = new HashMap<JcompSymbol,ASTNode>();
      is_assign = false;
    }

   Set<ASTNode> getVariablesToFix() {
      Set<ASTNode> rslt = new HashSet<ASTNode>();
      for (JcompSymbol js : var_defs.keySet()) {
	 if (!used_vars.contains(js)) continue;
	 if (assigned_vars.contains(js)) continue;
	 rslt.add(var_defs.get(js));
       }
      return rslt;
    }

   @Override public void postVisit(ASTNode n) {
      JcompSymbol rs = JavaAst.getReference(n);
      JcompSymbol ds = JavaAst.getDefinition(n);
      if (rs != null && rs != ds) {
	 if (is_assign) assigned_vars.add(rs);
	 else used_vars.add(rs);
       }
    }

   @Override public boolean visit(Assignment as) {
      is_assign = true;
      as.getLeftHandSide().accept(this);
      is_assign = false;
      as.getRightHandSide().accept(this);
      return false;
    }

   @Override public void endVisit(SingleVariableDeclaration svd) {
      JcompSymbol js = JavaAst.getDefinition(svd.getName());
      var_defs.put(js,svd);
      assigned_vars.add(js);
    }

   @Override public void endVisit(VariableDeclarationFragment vdf) {
      JcompSymbol js = JavaAst.getDefinition(vdf.getName());
      var_defs.put(js,vdf);
      if (vdf.getInitializer() != null) assigned_vars.add(js);
    }

}	// end of inner class VarFinder



/********************************************************************************/
/*										*/
/*	Add assignments 							*/
/*										*/
/********************************************************************************/

private class VarFixup extends TreeMapper {

   private Set<ASTNode> fix_defs;
   private boolean non_null;

   VarFixup(Set<ASTNode> fixes,boolean nonnull) {
      fix_defs = fixes;
      non_null = nonnull;
    }

   @Override protected String getSpecificsName() {
      if (non_null) return "NonNull";
      else return "";
   }

   @Override void rewriteTree(ASTNode orig,ASTRewrite rw) {
      if (fix_defs.contains(orig)) {
	 if (orig instanceof VariableDeclarationFragment) {
	    VariableDeclarationFragment vdf = (VariableDeclarationFragment) orig;
	    JcompSymbol def = JavaAst.getDefinition(vdf.getName());
	    if (def == null) return;
	    JcompType jt = def.getType();
	    Expression ex = null;
	    if (non_null && !jt.isAbstract())
	       ex = jt.createNonNullValue(rw.getAST());
	    else
	       ex = jt.createDefaultValue(rw.getAST());
	    rw.set(orig,VariableDeclarationFragment.INITIALIZER_PROPERTY,ex,null);
	  }
       }
    }

}	// end of inner class VarFixup



}	// end of class TransformFixVariables




/* end of TransformFixVariables.java */

