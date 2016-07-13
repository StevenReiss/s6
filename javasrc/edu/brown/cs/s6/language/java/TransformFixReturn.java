/********************************************************************************/
/*										*/
/*		TransformFixReturn.java 					*/
/*										*/
/*	Transformation that handles return value changes based on tests 	*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/language/java/TransformFixReturn.java,v 1.12 2015/09/23 17:54:53 spr Exp $ */


/*********************************************************************************
 *
 * $Log: TransformFixReturn.java,v $
 * Revision 1.12  2015/09/23 17:54:53  spr
 * Version to handle andriod UI applications.
 *
 * Revision 1.11  2014/08/29 15:16:08  spr
 * Updates for suise, testcases.
 *
 * Revision 1.10  2013/09/13 20:33:04  spr
 * Add calls for UI search.
 *
 * Revision 1.9  2013-05-09 12:26:20  spr
 * Minor changes to start ui fixups.
 *
 * Revision 1.8  2012-06-11 14:07:49  spr
 * add framework search; fix bugs
 *
 * Revision 1.7  2009-09-18 01:41:36  spr
 * Handle user testing.
 *
 * Revision 1.6  2009-05-12 22:28:48  spr
 * Fix ups to make user context work.
 *
 * Revision 1.5  2008-11-12 13:52:14  spr
 * Performance and bug updates.
 *
 * Revision 1.4  2008-08-28 00:32:56  spr
 * Next version of S6.	Lots of bug fixes, some new functionality.
 *
 * Revision 1.3  2008-07-18 22:27:09  spr
 * Handle remove compilation calls; update transforms to include code to use ASTrewrite.
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


import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6Solution;
import edu.brown.cs.s6.common.S6SolutionSet;
import edu.brown.cs.s6.common.S6TestCase;
import edu.brown.cs.s6.common.S6TestResults;



public class TransformFixReturn extends TransformJava implements S6Constants, JavaConstants {




/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

enum ChangeType {
   NONE,
   BOOLEAN_FLIP,
   STRING_UPPERCASE,
   STRING_LOWERCASE,
   DELTA
};


/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public TransformFixReturn(String name)
{
   super(name);
}




/********************************************************************************/
/*										*/
/*	Method to create mapper to do actual return type changes		*/
/*										*/
/********************************************************************************/

@Override protected boolean applyMethodTransform(S6SolutionSet solset,S6Solution sol)
{
   FragmentJava f = (FragmentJava) sol.getFragment();
   S6Request.Search sr = solset.getRequest();
   S6Request.MethodSignature ms = sr.getSignature().getMethodSignature();
   if (ms == null) return false;
   MethodDeclaration md = (MethodDeclaration) f.getAstNode();

   S6TestResults tr = f.getTestResults();
   if (tr == null || tr.allPassed()) return false;
   List<S6TestResults.S6ErrorResult> errs = tr.getErrorResults();
   String ret = ms.getReturnTypeName();
   ChangeType ct = ChangeType.NONE;
   long delta = 0;

   int npass = 0;
   int nfail = 0;
   for (S6TestCase tc : sr.getTests().getTestCases()) {
      String tcn = tc.getName();
      if (tr.getError(tcn)) return false;
      if (tr.getPassed(tcn)) ++npass;
      else {
	 String msg = tr.getErrorMessage(tcn);
	 if (msg == null) return false;
	 String vals [] = getValues(msg);
	 if (vals == null) return false;
	 ++nfail;
       }
    }

   if (ret.equals("boolean")) {
      if (npass == 0) ct = ChangeType.BOOLEAN_FLIP;
    }
   else if (ret.equals("java.lang.String") && errs != null && errs.size() > 0) {
      int toupp = 0;
      int tolow = 0;
      for (S6TestResults.S6ErrorResult ers : errs) {
	 if (ers.getActual().toUpperCase().equals(ers.getExpected())) ++toupp;
	 if (ers.getActual().toLowerCase().equals(ers.getExpected())) ++tolow;
       }
      if (toupp == errs.size()) ct = ChangeType.STRING_UPPERCASE;
      else if (tolow == errs.size()) ct = ChangeType.STRING_LOWERCASE;
    }
   else if (ret.equals("int") || ret.equals("long") || ret.equals("short") ||
	       ret.equals("char") || ret.equals("byte")) {
      if (npass == 0 && nfail > 1 && errs != null) {
	 boolean okay = true;
	 for (S6TestResults.S6ErrorResult ers : errs) {
	    long v0 = Long.parseLong(ers.getActual());
	    long v1 = Long.parseLong(ers.getExpected());
	    if (delta == 0) delta = v0 - v1;
	    else if (delta != v0 - v1) okay = false;
	  }
	 if (delta != 0 && okay) ct = ChangeType.DELTA;
       }
    }

   if (ct == ChangeType.NONE) return false;

   ReturnFinder rf = new ReturnFinder();
   md.accept(rf);
   Set<ASTNode> fixes = rf.getReturns();
   if (fixes == null || fixes.size() == 0) return false;

   TreeMapper tm = new ReturnValueMapper(ct,delta,ms.getName(),md,fixes);
   if (addNewSolution(solset,sol,tm)) return false;

   return true;
}



private String [] getValues(String msg)
{
   if (msg == null) return null;

   int idx0 = msg.indexOf("expected:<");
   if (idx0 < 0) return null;
   idx0 += 10;

   int idx1 = msg.indexOf("> but was:<",idx0);
   if (idx1 < 0) return null;
   int idx2 = idx1 + 11;
   int idx3 = msg.lastIndexOf(">");

   String v0 = msg.substring(idx0,idx1);
   String v1 = msg.substring(idx2,idx3);

   return new String [] { v0,v1 };
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

}



/********************************************************************************/
/*										*/
/*	Class to handle return type mappings					*/
/*										*/
/********************************************************************************/

private class ReturnValueMapper extends TreeMapper {

   private ChangeType change_type;
   private long change_delta;
   private String for_whom;
   private Set<ASTNode> fix_returns;

   ReturnValueMapper(ChangeType ct,long delta,String whom,MethodDeclaration md,Set<ASTNode> fixes) {
      for_whom = whom;
      change_type = ct;
      change_delta = delta;
      fix_returns = fixes;
    }

   @Override protected String getSpecificsName()   { return for_whom; }

   void rewriteTree(ASTNode orig,ASTRewrite rw) {
      if (fix_returns.contains(orig)) {
         ReturnStatement rst = (ReturnStatement) orig;
         switch (change_type) {
            case BOOLEAN_FLIP :
               rewriteBooleanFlip(rst,rw);
               break;
            case STRING_UPPERCASE :
               rewriteStringMap(rst,"toUpperCase",rw);
               break;
            case STRING_LOWERCASE :
               rewriteStringMap(rst,"toLowerCase",rw);
               break;
            case DELTA :
               rewriteDelta(rst,rw);
               break;
            case NONE :
               break;
          }
       }
    }

   private void rewriteBooleanFlip(ReturnStatement rst,ASTRewrite rw) {
      PrefixExpression pfx = rst.getAST().newPrefixExpression();
      Expression old = (Expression) rw.createCopyTarget(rst.getExpression());
      pfx.setOperator(PrefixExpression.Operator.NOT);
      pfx.setOperand(old);
      rw.set(rst,ReturnStatement.EXPRESSION_PROPERTY,pfx,null);
    }

   private void rewriteStringMap(ReturnStatement rst,String fct,ASTRewrite rw) {
      MethodInvocation mi = rst.getAST().newMethodInvocation();
      Expression old = (Expression) rw.createCopyTarget(rst.getExpression());
      mi.setName(JavaAst.getSimpleName(rst.getAST(),fct));
      ParenthesizedExpression pe = rst.getAST().newParenthesizedExpression();
      pe.setExpression(old);
      mi.setExpression(pe);
      rw.set(rst,ReturnStatement.EXPRESSION_PROPERTY,mi,null);
    }

   private void rewriteDelta(ReturnStatement rst,ASTRewrite rw) {
      Expression old = (Expression) rw.createCopyTarget(rst.getExpression());
      ParenthesizedExpression pe = rst.getAST().newParenthesizedExpression();
      pe.setExpression(old);
      NumberLiteral nl = JavaAst.newNumberLiteral(rst.getAST(),Math.abs(change_delta));
      InfixExpression ie = rst.getAST().newInfixExpression();
      if (change_delta > 0) ie.setOperator(InfixExpression.Operator.PLUS);
      else ie.setOperator(InfixExpression.Operator.MINUS);
      ie.setLeftOperand(pe);
      ie.setRightOperand(nl);
      rw.set(rst,ReturnStatement.EXPRESSION_PROPERTY,ie,null);
    }

}	// end of subclass ReturnValueMapper




}	// end of class TransformFixReturn





/* end of TransformFixReturn.java */
