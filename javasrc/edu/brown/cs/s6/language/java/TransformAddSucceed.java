/********************************************************************************/
/*										*/
/*		TransformAddSucceed.java					*/
/*										*/
/*	description of class							*/
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import edu.brown.cs.ivy.jcomp.JcompSymbol;
import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6Solution;
import edu.brown.cs.s6.common.S6SolutionSet;


public class TransformAddSucceed extends TransformJava implements S6Constants, JavaConstants
{


/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public TransformAddSucceed(String name)
{
   super(name);
}



/********************************************************************************/
/*										*/
/*	<comment here>								*/
/*										*/
/********************************************************************************/

@Override protected TreeMapper findClassMapping(S6SolutionSet ss,TypeDeclaration td,
      S6Request.ClassSignature csf,S6Solution sol)
{
   if (!JavaAst.checkTypeSignature(td,csf,S6SignatureType.FULL,null)) return null;

   SucceedNeededFinder snf = new SucceedNeededFinder();
   td.accept(snf);
   if (!snf.doTransform()) return null;

   SucceedMapper sm = new SucceedMapper(snf.getMethods());

   return sm;
}


@Override protected TreeMapper findMethodMapping(S6SolutionSet ss,MethodDeclaration md,
      S6Request.MethodSignature ms,S6Solution sol)
{
   SucceedNeededFinder snf = new SucceedNeededFinder();
   md.accept(snf);
   if (!snf.doTransform()) return null;

   SucceedMapper sm = new SucceedMapper(snf.getMethods());

   return sm;
}


protected boolean checkApplyMethodForClass(S6SolutionSet sols,S6Solution sol,
      TypeDeclaration td,S6Request.MethodSignature msg,MethodDeclaration md)
{
   return false;
}



/********************************************************************************/
/*										*/
/*	Check if we need to add succeeds calls					*/
/*										*/
/********************************************************************************/

private static class SucceedNeededFinder extends ASTVisitor {

   private boolean is_test;
   private boolean is_fail;
   private int fail_count;
   private int assert_count;
   private Map<MethodDeclaration,Boolean> fix_methods;

   SucceedNeededFinder() {
      is_test = false;
      is_fail = false;
      fail_count = 0;
      assert_count = 0;
      fix_methods = new HashMap<MethodDeclaration,Boolean>();
    }

   boolean doTransform()			{ return fix_methods.size() > 0; }
   Map<MethodDeclaration,Boolean> getMethods()	{ return fix_methods; }

   @Override public boolean visit(MethodDeclaration md) {
      is_test = false;
      is_fail = false;
      fail_count = 0;
      assert_count = 0;
      return true;
    }

   @Override public void endVisit(MethodDeclaration md) {
      if (is_fail) fix_methods.put(md,Boolean.TRUE);
      else if (is_test && fail_count > 0 && assert_count == 0) {
	 fix_methods.put(md,Boolean.FALSE);
       }
    }

   @Override public boolean visit(AnonymousClassDeclaration atd) {
      return false;
    }

   @Override public void endVisit(MarkerAnnotation ma) {
      Name nm = ma.getTypeName();
      JcompType jt = JavaAst.getJavaType(nm);
      if (jt != null && jt.getName().equals("org.junit.Test")) is_test = true;
    }

   @Override public void endVisit(NormalAnnotation na) {
      Name nm = na.getTypeName();
      JcompType jt = JavaAst.getJavaType(nm);
      if (jt != null && jt.getName().equals("org.junit.Test")) {
	 is_test = true;
	 for (Object o : na.values()) {
	    MemberValuePair mvp = (MemberValuePair) o;
	    String enm = mvp.getName().getIdentifier();
	    if (enm.equals("expected")) {
	       is_fail = true;
	     }	
	  }
       }
   }

   @Override public void endVisit(AssertStatement as) {
      ++fail_count;
    }

   @Override public void endVisit(MethodInvocation mi) {
      JcompSymbol js = JavaAst.getReference(mi);
      if (js == null) return;
      String fqn = js.getFullName();
      if (fqn.startsWith("edu.brown.cs.s6.runner.RunnerAssert.assert")) {
	 ++assert_count;
       }
      else if (fqn.startsWith("edu.brown.cs.s6.runner.RunnerAssert.fail")) {
	 ++fail_count;
       }
    }

}	// end of inner class SucceedNeededFinder



/********************************************************************************/
/*										*/
/*	Mapper to add succeed calls						*/
/*										*/
/********************************************************************************/

private class SucceedMapper extends TreeMapper {

   private Map<MethodDeclaration,Boolean> fix_methods;

   SucceedMapper(Map<MethodDeclaration,Boolean> mds) {
      fix_methods = mds;
    }

   

   @SuppressWarnings("unchecked")
   @Override void rewriteTree(ASTNode orig,ASTRewrite rw) {
      Boolean fg = fix_methods.get(orig);
      if (fg == Boolean.TRUE) {
	 rewriteThrowExpected(rw,(MethodDeclaration) orig);
       }
      else if (fg != null) {
	 MethodDeclaration md = (MethodDeclaration) orig;
	 Block b = md.getBody();
	 List<?> stmts = b.statements();
	 if (stmts.size() == 0) return;
	 Statement st = (Statement) stmts.get(stmts.size()-1);
	 switch (st.getNodeType()) {
	    case ASTNode.RETURN_STATEMENT :
	    case ASTNode.THROW_STATEMENT :
	    case ASTNode.BLOCK :
	       return;
	    default :
	       break;
	  }
	 MethodInvocation mi = getSucceed(rw.getAST(),false);
	 ListRewrite lrw = rw.getListRewrite(b,Block.STATEMENTS_PROPERTY);
	 ExpressionStatement es = rw.getAST().newExpressionStatement(mi);
	 lrw.insertLast(es,null);
       }
      else if (orig.getNodeType() == ASTNode.RETURN_STATEMENT) {
	 ReturnStatement rs = (ReturnStatement) orig;
	 for (ASTNode an = orig; an != null; an = an.getParent()) {
	    if (an instanceof MethodDeclaration) {
	       Boolean rfg = fix_methods.get(an);
	       if (rfg != Boolean.FALSE) return;
	       break;
	     }
	    if (an instanceof TypeDeclaration) return;
	  }
	 Block blk = rw.getAST().newBlock();
	 MethodInvocation mi = getSucceed(rw.getAST(),false);
	 ExpressionStatement es = rw.getAST().newExpressionStatement(mi);
	 blk.statements().add(es);
	 ReturnStatement nrs = rw.getAST().newReturnStatement();
	 if (rs.getExpression() != null) {
	    ASTNode nex = ASTNode.copySubtree(rw.getAST(),rs.getExpression());
	    nrs.setExpression((Expression) nex);
	  }
	 blk.statements().add(nrs);
	 rw.replace(rs,blk,null);
       }
   }

   @SuppressWarnings("unchecked")
   private void rewriteThrowExpected(ASTRewrite rw,MethodDeclaration md) {
      AST ast = rw.getAST();

      JcompType etyp = JavaAst.getTyper(md).findSystemType("java.lang.Exception");
      for (Object o : md.modifiers()) {
	 if (o instanceof NormalAnnotation) {
	    NormalAnnotation na = (NormalAnnotation) o;
	    for (Object o1 : na.values()) {
	       MemberValuePair mvp = (MemberValuePair) o1;
	       if (mvp.getName().getIdentifier().equals("expected")) {
		  if (mvp.getValue() instanceof TypeLiteral) {
		     TypeLiteral tl = (TypeLiteral) mvp.getValue();
		     JcompType jt = JavaAst.getJavaType(tl.getType());
		     if (jt != null && jt.isUndefined()) {
			Name nmx = JavaAst.getQualifiedName(ast,"java.lang.Exception");
			Type tpx = ast.newSimpleType(nmx);
			rw.replace(tl.getType(),tpx,null);
		      }
		     else etyp = jt;
		   }
		  break;
		}
	     }
	  }
       }

      Block b0 = (Block) ASTNode.copySubtree(ast,md.getBody());
      FailReturnFixer frf = new FailReturnFixer(rw);
      b0.accept(frf);
      Block bnew = ast.newBlock();
      TryStatement tstmt = ast.newTryStatement();
      tstmt.setBody(b0);
      CatchClause cc = ast.newCatchClause();
      SingleVariableDeclaration svd = ast.newSingleVariableDeclaration();
      svd.setName(JavaAst.getSimpleName(ast,"s6exception"));
      SimpleType st = ast.newSimpleType(JavaAst.getQualifiedName(ast,etyp.getName()));
      svd.setType(st);
      cc.setException(svd);
      Block eblk = ast.newBlock();
      cc.setBody(eblk);
      tstmt.catchClauses().add(cc);
      MethodInvocation mi = getSucceed(ast,false);
      Statement sts = ast.newExpressionStatement(mi);
      eblk.statements().add(sts);
      ThrowStatement thr = ast.newThrowStatement();
      thr.setExpression(JavaAst.getSimpleName(ast,"s6exception"));
      eblk.statements().add(thr);
      bnew.statements().add(tstmt);
      MethodInvocation fl = getSucceed(ast,true);
      Statement fsts = ast.newExpressionStatement(fl);
      bnew.statements().add(fsts);
      rw.replace(md.getBody(),bnew,null);
    }

}	// end of inner class SucceedMapper


private class FailReturnFixer extends ASTVisitor {

   private ASTRewrite ast_rewrite;

   FailReturnFixer(ASTRewrite rw) {
      ast_rewrite = rw;
    }

   @SuppressWarnings("unchecked")
   @Override public void endVisit(ReturnStatement rs) {
      AST ast = ast_rewrite.getAST();
      Block blk = ast.newBlock();
      MethodInvocation mi = getSucceed(ast,true);
      ExpressionStatement es = ast.newExpressionStatement(mi);
      blk.statements().add(es);
      ReturnStatement nrs = ast.newReturnStatement();
      if (rs.getExpression() != null) {
	 ASTNode nex = ASTNode.copySubtree(ast,rs.getExpression());
	 nrs.setExpression((Expression) nex);
       }
      blk.statements().add(nrs);
      ast_rewrite.replace(rs,blk,null); 
    }

}


private MethodInvocation getSucceed(AST ast,boolean fail)
{
   String what = (fail ? "failDefault" : "succeed");
   MethodInvocation mi = ast.newMethodInvocation();
   Name nm = JavaAst.getQualifiedName(ast,"edu.brown.cs.s6.runner.RunnerAssert");
   SimpleName sn = JavaAst.getSimpleName(ast,what);
   mi.setExpression(nm);
   mi.setName(sn);
   return mi;
}





}	// end of class TransformAddSucceed




/* end of TransformAddSucceed.java */

