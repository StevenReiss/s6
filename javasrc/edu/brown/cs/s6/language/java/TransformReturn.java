/********************************************************************************/
/*										*/
/*		TransformReturn.java						*/
/*										*/
/*	Transformation that handles return type changes 			*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/language/java/TransformReturn.java,v 1.15 2015/09/23 17:54:54 spr Exp $ */


/*********************************************************************************
 *
 * $Log: TransformReturn.java,v $
 * Revision 1.15  2015/09/23 17:54:54  spr
 * Version to handle andriod UI applications.
 *
 * Revision 1.14  2014/08/29 15:16:10  spr
 * Updates for suise, testcases.
 *
 * Revision 1.13  2013/09/13 20:33:05  spr
 * Add calls for UI search.
 *
 * Revision 1.12  2013-05-09 12:26:22  spr
 * Minor changes to start ui fixups.
 *
 * Revision 1.11  2012-07-20 22:15:21  spr
 * New transforms and resolution for UI search
 *
 * Revision 1.10  2012-06-20 12:21:33  spr
 * Initial fixes for UI search
 *
 * Revision 1.9  2012-06-11 14:07:49  spr
 * add framework search; fix bugs
 *
 * Revision 1.8  2009-05-12 22:28:48  spr
 * Fix ups to make user context work.
 *
 * Revision 1.7  2008-11-12 13:52:14  spr
 * Performance and bug updates.
 *
 * Revision 1.6  2008-08-28 00:32:56  spr
 * Next version of S6.	Lots of bug fixes, some new functionality.
 *
 * Revision 1.5  2008-07-18 22:27:09  spr
 * Handle remove compilation calls; update transforms to include code to use ASTrewrite.
 *
 * Revision 1.4  2008-07-17 13:46:46  spr
 * Bug fixes and speed ups.
 *
 * Revision 1.3  2008-06-27 15:45:41  spr
 * Minor bug fixes to transforms.
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import edu.brown.cs.ivy.jcomp.JcompSymbol;
import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.ivy.jcomp.JcompTyper;
import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6Solution;
import edu.brown.cs.s6.common.S6SolutionSet;



public class TransformReturn extends TransformJava implements S6Constants, JavaConstants {




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public TransformReturn(String name)
{
   super(name);
}




/********************************************************************************/
/*										*/
/*	Method to create mapper to do actual return type changes		*/
/*										*/
/********************************************************************************/

@Override protected Collection<TreeMapper> findMethodMappings(S6SolutionSet ss,MethodDeclaration md,
      S6Request.MethodSignature ms,
      S6Solution sol)
{
   if (JavaAst.checkMethodSignature(md,ms,S6SignatureType.RETURN)) return null;
   if (!JavaAst.checkMethodSignature(md,ms,S6SignatureType.PARAMETERS)) return null;

   JcompTyper jt = JavaAst.getTyper(md);
   if (jt == null) return null;

   String rtyp = ms.getReturnTypeName();
   if (rtyp == null) return null;

   JcompType t0 = jt.findSystemType(ms.getReturnTypeName());
   JcompType t1 = JavaAst.getJavaType(md.getReturnType2());

   if (t0 == null || t1 == null || t0 == t1) return null;

   Set<ASTNode> fixes = null;
   JcompSymbol conv = null;
   if (t0.getName().equals("void")) {
      // remove return value throughout
      ReturnFinder rf = new ReturnFinder();
      md.accept(rf);
      fixes = rf.getReturns();
    }
   else if (!t1.isCompatibleWith(t0)) {
      if (t0.isNumericType() && t1.isNumericType()) {
	 ReturnFinder rf = new ReturnFinder();
	 md.accept(rf);
	 fixes = rf.getReturns();
       }
      else {
	 JcompTyper typer = JavaAst.getTyper(md);
	 // THIS SHOULD RETURN A LIST OF OBJECTS
	 List<JcompSymbol> maps = t0.isConformableFrom(typer,t1);
	 if (maps != null) {
	    ReturnFinder rf = new ReturnFinder();
	    md.accept(rf);
	    fixes = rf.getReturns();
	    if (maps.size() == 1) conv = maps.get(0);
	    else {
	       List<TreeMapper> rslt = new ArrayList<TreeMapper>();
	       for (JcompSymbol jsm : maps) {
		  TreeMapper tm = new ReturnTypeMapper(ms.getName(),md,t0,fixes,jsm);
		  rslt.add(tm);
		}
	       return rslt;
	     }
	  }
	 else return null;
       }
    }

   TreeMapper tm = new ReturnTypeMapper(ms.getName(),md,t0,fixes,conv);
   return Collections.singletonList(tm);
}


@Override protected boolean checkApplyMethodForClass(S6SolutionSet ss,S6Solution sol,TypeDeclaration td,
      S6Request.MethodSignature msg,MethodDeclaration md)
{
   for (MethodDeclaration xmd : td.getMethods()) {
      if (JavaAst.checkMethodSignature(xmd,msg,S6SignatureType.FULL)) return false;
    }

   return super.checkApplyMethodForClass(ss,sol,td,msg,md);
}


@Override protected boolean checkApplyClassForPackage(S6SolutionSet ss,CompilationUnit cu,
      S6Request.ClassSignature csg,AbstractTypeDeclaration td)
{
   boolean fg = JavaAst.checkTypeSignature(td,csg,S6SignatureType.NAME,null);
   return fg;
}



/********************************************************************************/
/*										*/
/*	Class to find return statements in a method				*/
/*										*/
/********************************************************************************/

private static class ReturnFinder extends ASTVisitor {

   private Set<ASTNode> return_nodes;

   ReturnFinder() {
      return_nodes = new HashSet<ASTNode>();
    }

   Set<ASTNode> getReturns()			{ return return_nodes; }

   public @Override boolean visit(ReturnStatement nd) {
      return_nodes.add(nd);
      return false;
    }

   public @Override boolean visit(ClassInstanceCreation nd) {
      return false;
    }

}	// end of inner class ReturnFinder



/********************************************************************************/
/*										*/
/*	Class to handle return type mappings					*/
/*										*/
/********************************************************************************/

private class ReturnTypeMapper extends TreeMapper {

   private String for_whom;
   private MethodDeclaration change_method;
   private JcompSymbol convert_method;
   private JcompType new_type;
   private Set<ASTNode> fix_returns;

   ReturnTypeMapper(String whom,MethodDeclaration md,JcompType t1,Set<ASTNode> fixes,JcompSymbol conv) {
      for_whom = whom;
      change_method = md;
      new_type = t1;
      fix_returns = fixes;
      convert_method = conv;
    }

   @Override protected String getSpecificsName()   { return for_whom; }

   @SuppressWarnings("unchecked")
   void rewriteTree(ASTNode orig,ASTRewrite rw) {
      if (fix_returns != null && fix_returns.contains(orig)) {
	 ReturnStatement rst = (ReturnStatement) orig;
	 if (convert_method != null) {
	    if (rst.getExpression().getNodeType() == ASTNode.NULL_LITERAL) return;
	    if (convert_method.getName().equals("<init>")) {
	       Expression old = (Expression) ASTNode.copySubtree(rw.getAST(),rst.getExpression());
	       ClassInstanceCreation cic = rw.getAST().newClassInstanceCreation();
	       JcompType jt = convert_method.getClassType();
	       SimpleType sty = rw.getAST().newSimpleType(JavaAst.getQualifiedName(rw.getAST(),jt.getName()));
	       cic.setType(sty);
	       cic.arguments().add(old);
	       rw.set(rst,ReturnStatement.EXPRESSION_PROPERTY,cic,null);
	     }
	    else {
	       Expression old = (Expression) ASTNode.copySubtree(rw.getAST(),rst.getExpression());
	       MethodInvocation mi = rw.getAST().newMethodInvocation();
	       mi.setExpression(old);
	       mi.setName(JavaAst.getSimpleName(rw.getAST(),convert_method.getName()));
	       rw.set(rst,ReturnStatement.EXPRESSION_PROPERTY,mi,null);
	     }
	  }
	 else if (new_type.isNumericType()) {
	    Expression old = (Expression) rw.createCopyTarget(rst.getExpression());
	    CastExpression cst = rst.getAST().newCastExpression();
	    cst.setExpression(old);
	    Type st = new_type.createAstNode(rst.getAST());
	    cst.setType(st);
	    rw.set(rst,ReturnStatement.EXPRESSION_PROPERTY,cst,null);
	  }
	 else { 		// handle void return type
	    rw.set(orig,ReturnStatement.EXPRESSION_PROPERTY,null,null);
	  }
       }
      else if (orig == change_method) {
	 Type tnode = new_type.createAstNode(orig.getAST());
	 rw.set(orig,MethodDeclaration.RETURN_TYPE2_PROPERTY,tnode,null);
       }
    }

}	// end of subclass ReturnTypeMapper




}	// end of class TransformReturn





/* end of TransformReturn.java */
