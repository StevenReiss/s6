/********************************************************************************/
/*										*/
/*		TransformThrow.java						*/
/*										*/
/*	Transformation that handles removing unwanted throws			*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/language/java/TransformThrow.java,v 1.13 2015/09/23 17:54:54 spr Exp $ */


/*********************************************************************************
 *
 * $Log: TransformThrow.java,v $
 * Revision 1.13  2015/09/23 17:54:54  spr
 * Version to handle andriod UI applications.
 *
 * Revision 1.12  2015/02/14 19:40:19  spr
 * Add test case generation.
 *
 * Revision 1.11  2014/08/29 15:16:10  spr
 * Updates for suise, testcases.
 *
 * Revision 1.10  2013-05-09 12:26:22  spr
 * Minor changes to start ui fixups.
 *
 * Revision 1.9  2012-06-11 14:07:50  spr
 * add framework search; fix bugs
 *
 * Revision 1.8  2009-09-18 01:41:36  spr
 * Handle user testing.
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


import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Stack;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.ivy.jcomp.JcompTyper;
import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6Solution;
import edu.brown.cs.s6.common.S6SolutionSet;



public class TransformThrow extends TransformJava implements S6Constants, JavaConstants {




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public TransformThrow(String name)
{
   super(name);
}




/********************************************************************************/
/*										*/
/*	Method to find transformer for unwanted throws				*/
/*										*/
/********************************************************************************/

@Override protected TreeMapper findMethodMapping(S6SolutionSet ss,MethodDeclaration md,
						    S6Request.MethodSignature ms,
						    S6Solution sol)
{
   if (!JavaAst.checkMethodSignature(md,ms,S6SignatureType.FULL)) return null;

   Collection<ThrowStatement> lth = findUnwantedThrows(md);
   if (lth == null || lth.isEmpty()) return null;
   JcompTyper jt = JavaAst.getTyper(md);
   if (jt == null) return null;

   JcompType rtyp = null;
   for (Iterator<?> it = md.thrownExceptionTypes().iterator(); it.hasNext(); ) {
      Type nm = (Type) it.next();
      rtyp = JavaAst.getJavaType(nm);
      if (rtyp != null) break;
    }
   if (rtyp == null) {
      rtyp = jt.findSystemType("java.lang.Error");
      // set rtyp = null to cause throw to be replaced with empty statement
    }

   return new ThrowMapper(md,lth,rtyp);
}




/********************************************************************************/
/*										*/
/*	Method to check code for unwanted throws				*/
/*										*/
/********************************************************************************/

private Collection<ThrowStatement> findUnwantedThrows(ASTNode n)
{
   ThrowVisitor tv = new ThrowVisitor(n);

   n.accept(tv);

   return tv.remove_throws;
}




private static class ThrowVisitor extends ASTVisitor {

   private JcompType error_exception;
   private Stack<JcompType> catch_items;
   private Collection<ThrowStatement> remove_throws;

   ThrowVisitor(ASTNode n) {
      catch_items = new Stack<JcompType>();
      remove_throws = new HashSet<ThrowStatement>();
      JcompTyper jt = JavaAst.getTyper(n);
      if (jt != null) error_exception = jt.findSystemType("java.lang.Error");
    }

   public boolean visit(MethodDeclaration md) {
      int ln = catch_items.size();
      for (Iterator<?> it = md.thrownExceptionTypes().iterator(); it.hasNext(); ) {
	 Type nm = (Type) it.next();
	 JcompType jt = JavaAst.getJavaType(nm);
	 if (jt != null) catch_items.push(jt);
       }
      if (md.getBody() != null) md.getBody().accept(this);
      while (catch_items.size() > ln) catch_items.pop();
      return false;
    }

   public boolean visit(TryStatement st) {
      int ln = catch_items.size();
      for (Iterator<?> it = st.catchClauses().iterator(); it.hasNext(); ) {
	 CatchClause cc = (CatchClause) it.next();
	 cc.getBody().accept(this);
       }
      if (st.getFinally() != null) {
	 st.getFinally().accept(this);
       }
      for (Iterator<?> it = st.catchClauses().iterator(); it.hasNext(); ) {
	 CatchClause cc = (CatchClause) it.next();
	 JcompType jt = JavaAst.getJavaType(cc.getException().getType());
	 if (jt != null) catch_items.push(jt);
       }
      st.getBody().accept(this);
      while (catch_items.size() > ln) catch_items.pop();
      return false;
    }

   public boolean visit(ThrowStatement st) {
      JcompType jt = JavaAst.getExprType(st.getExpression());
      if (error_exception == null) return false;
      if (jt != null) {
	 for (JcompType cty : catch_items) {
	    if (jt.isCompatibleWith(cty)) return false;
	  }
	 if (jt.isCompatibleWith(error_exception)) return false;
       }
      remove_throws.add(st);
      return false;
    }

}	// end of subclass ThrowVisitor



/********************************************************************************/
/*										*/
/*	Class to handle return type mappings					*/
/*										*/
/********************************************************************************/

private class ThrowMapper extends TreeMapper {

   private MethodDeclaration change_method;
   private Collection<ThrowStatement> remove_throws;
   private JcompType replace_type;

   ThrowMapper(MethodDeclaration md,Collection<ThrowStatement> rem,JcompType rep) {
      change_method = md;
      remove_throws = rem;
      replace_type = rep;
    }

   @Override protected String getSpecificsName() {
      return change_method.getName().getIdentifier();
   }

   @SuppressWarnings("unchecked")
   void rewriteTree(ASTNode orig,ASTRewrite rw) {
      if (!remove_throws.contains(orig)) return;

      Expression arg = null;
      ThrowStatement origth = (ThrowStatement) orig;
      if (origth.getExpression() instanceof ClassInstanceCreation) {
	 ClassInstanceCreation cic = (ClassInstanceCreation) origth.getExpression();
	 for (Iterator<?> it = cic.arguments().iterator(); it.hasNext(); ) {
	    Expression aex = (Expression) it.next();
	    JcompType jt = JavaAst.getExprType(aex);
	    if (jt != null && jt.getName().equals("java.lang.String")) {
	       arg = (Expression) rw.createCopyTarget(aex);
	       break;
	     }
	  }
       }

      AST ast = orig.getAST();
      ClassInstanceCreation exc = ast.newClassInstanceCreation();
      exc.setType(replace_type.createAstNode(ast));
      if (arg != null) {
	 exc.arguments().add(arg);
       }
      rw.set(origth,ThrowStatement.EXPRESSION_PROPERTY,exc,null);
    }

}	// end of subclass ThrowMapper



}	// end of class TransformThrow





/* end of TransformThrow.java */

