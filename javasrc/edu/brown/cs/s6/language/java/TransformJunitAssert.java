/********************************************************************************/
/*										*/
/*		TransformJunitAssert.java					*/
/*										*/
/*	Replace assert statements with assertTrue in testing situations 	*/
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6Solution;
import edu.brown.cs.s6.common.S6SolutionSet;


public class TransformJunitAssert extends TransformJava implements S6Constants, JavaConstants
{






/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public TransformJunitAssert(String name)
{
   super(name);
}



/********************************************************************************/
/*										*/
/*	Entry points for transformation 					*/
/*										*/
/********************************************************************************/

@Override protected TreeMapper findClassMapping(S6SolutionSet solset,
      TypeDeclaration td,S6Request.ClassSignature cs,S6Solution sol)
{
   if (!JavaAst.checkTypeSignature(td,cs,S6SignatureType.FULL,null)) return null;

   AssertFinder af = new AssertFinder();
   td.accept(af);
   Set<ASTNode> rslt = af.getStatements();
   if (rslt == null || rslt.isEmpty()) return null;

   return new AssertFixer(rslt);
}




/********************************************************************************/
/*										*/
/*	Class to find assert statements to fix					*/
/*										*/
/********************************************************************************/

private class AssertFinder extends ASTVisitor {

   private Set<ASTNode> assert_statements;

   AssertFinder() {
      assert_statements = new HashSet<ASTNode>();
    }

   Set<ASTNode> getStatements() 		{ return assert_statements; }

   @Override public void endVisit(AssertStatement as) {
      assert_statements.add(as);
    }

}	// end of inner class AssertFinder



/********************************************************************************/
/*										*/
/*	Class to convert assert statements					*/
/*										*/
/********************************************************************************/

private class AssertFixer extends TreeMapper {

   private Set<ASTNode> to_fix;

   AssertFixer(Set<ASTNode> fix) {
      to_fix = fix;
    }



   @Override void rewriteTree(ASTNode orig,ASTRewrite rw) {
      if (to_fix.contains(orig)) {
	 if (orig instanceof AssertStatement) {
	    AssertStatement as = (AssertStatement) orig;
	    Expression ex = as.getExpression();
	    boolean done = false;
	    if (ex instanceof InfixExpression) {
	       InfixExpression ine = (InfixExpression) ex;
	       if (ine.getOperator() == InfixExpression.Operator.EQUALS) {
		  Expression e1 = ine.getLeftOperand();
		  Expression e2 = ine.getRightOperand();
		  JcompType jt1 = JavaAst.getJavaType(e1);
		  JcompType jt2 = JavaAst.getJavaType(e2);
		  if (e1 instanceof NullLiteral) {
		     rewriteAs(rw,as,"assertNull",e2,null);
		   }
		  else if (e2 instanceof NullLiteral) {
		     rewriteAs(rw,as,"assertNull",e1,null);
		   }
		  else if (jt1 != null && jt2 != null && jt1.isPrimitiveType() && jt2.isPrimitiveType()) {
		     rewriteAs(rw,as,"assertEquals",e1,e2);
		   }
		  else {
		     rewriteAs(rw,as,"assertSame",e1,e2);
		   }
		  done = true;
		}
	       else if (ine.getOperator() == InfixExpression.Operator.NOT_EQUALS) {
		  Expression e1 = ine.getLeftOperand();
		  Expression e2 = ine.getRightOperand();
		  JcompType jt1 = JavaAst.getJavaType(e1);
		  JcompType jt2 = JavaAst.getJavaType(e2);
		  if (e1 instanceof NullLiteral) {
		     rewriteAs(rw,as,"assertNotNull",e2,null);
		     done = true;
		   }
		  else if (e2 instanceof NullLiteral) {
		     rewriteAs(rw,as,"assertNotNull",e1,null);
		     done = true;
		   }
		  else if (!jt1.isPrimitiveType() || !jt2.isPrimitiveType()) {
		     rewriteAs(rw,as,"assertNotSame",e1,e2);
		     done = true;
		   }
		}
	     }
	    else if (ex instanceof MethodInvocation) {
	       MethodInvocation mi = (MethodInvocation) ex;
	       if (mi.getName().getIdentifier().equals("equals") && mi.getExpression() != null &&
		     mi.arguments().size() == 1) {
		  List<?> args = mi.arguments();
		  Expression arg = (Expression) args.get(0);
		  rewriteAs(rw,as,"assertEquals",mi.getExpression(),arg);
		  done = true;
		}
	     }
	    if (!done) {
	       rewriteAs(rw,as,"assertTrue",ex,null);
	     }
	  }
       }
    }

   @SuppressWarnings("unchecked")
   private void rewriteAs(ASTRewrite rw,AssertStatement as,String m,Expression e1,Expression e2)
   {
      AST ast = rw.getAST();
      Name qn = JavaAst.getQualifiedName(ast,"edu.brown.cs.s6.runner.RunnerAssert");
      SimpleName mn = JavaAst.getSimpleName(ast,m);
      MethodInvocation mi = ast.newMethodInvocation();
      mi.setExpression(qn);
      mi.setName(mn);
      if (as.getMessage() != null) {
	 ASTNode n1 = ASTNode.copySubtree(ast,as.getMessage());
	 mi.arguments().add(n1);
       }
      if (e1 != null) {
	 ASTNode n2 = ASTNode.copySubtree(ast,e1);
	 mi.arguments().add(n2);
       }
      if (e2 != null) {
	 ASTNode n3 = ASTNode.copySubtree(ast,e2);
	 mi.arguments().add(n3);
       }
      ExpressionStatement es = ast.newExpressionStatement(mi);
      rw.replace(as,es,null);
   }

}

}	// end of class TransformJunitAssert




/* end of TransformJunitAssert.java */

