/********************************************************************************/
/*										*/
/*		TransformAddReturn.java 					*/
/*										*/
/*	Transformation to add a return statement if needed to compile		*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/language/java/TransformAddReturn.java,v 1.4 2016/01/14 17:03:29 spr Exp $ */


/*********************************************************************************
 *
 * $Log: TransformAddReturn.java,v $
 * Revision 1.4  2016/01/14 17:03:29  spr
 * Fix up testing for android.  Fix bug in addreturn transform.
 *
 * Revision 1.3  2015/09/23 17:54:52  spr
 * Version to handle andriod UI applications.
 *
 * Revision 1.2  2014/08/29 15:16:08  spr
 * Updates for suise, testcases.
 *
 * Revision 1.1  2013/09/13 20:34:35  spr
 * New transforms for swing.
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.language.java;


import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6Solution;
import edu.brown.cs.s6.common.S6SolutionSet;



public class TransformAddReturn extends TransformJava implements S6Constants, JavaConstants {



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public TransformAddReturn(String name)
{
   super(name);
}




/********************************************************************************/
/*										*/
/*	Package Transform							*/
/*										*/
/********************************************************************************/

@Override protected Collection<TreeMapper> findPackageMappings(S6SolutionSet ss,
	S6Solution sol,CompilationUnit cu,S6Request.PackageSignature sg)
{
   TreeMapper tm = checkReturns(sol,cu);
   if (tm == null) return null;

   return Collections.singletonList(tm);
}



/********************************************************************************/
/*										*/
/*	Method transform							*/
/*										*/
/********************************************************************************/

@Override protected TreeMapper findMethodMapping(S6SolutionSet ss,MethodDeclaration md,
						    S6Request.MethodSignature ms,
						    S6Solution sol)
{
   return checkReturns(sol,md);
}



/********************************************************************************/
/*										*/
/*	Handle check and transform						*/
/*										*/
/********************************************************************************/

TreeMapper checkReturns(S6Solution sol,ASTNode nd)
{
   ReturnNeededFinder rnf = new ReturnNeededFinder();
   nd.accept(rnf);
   Set<ASTNode> rqs = rnf.getMethods();
   if (rqs == null || rqs.size() == 0) return null;

   TreeMapper tm = new ReturnAdder("package",rqs);

   return tm;
}



/********************************************************************************/
/*										*/
/*	Find where returns are needed						*/
/*										*/
/********************************************************************************/

private class ReturnNeededFinder extends ASTVisitor {

   Set<ASTNode> return_methods;

   ReturnNeededFinder() {
      return_methods = new HashSet<ASTNode>();
    }

   Set<ASTNode> getMethods()		{ return return_methods; }

   @Override public void endVisit(MethodDeclaration md) {
      Type t = md.getReturnType2();
      if (t == null) return;
      String rnm = JavaAst.getJavaTypeName(t);
      if (rnm.equals("void")) return;
   
      Block b = md.getBody();
      if (b == null) return;
   
      boolean needreturn = false;
      if (b.statements().size() == 0) needreturn = true;
      else {
         ASTNode last = (ASTNode) b.statements().get(b.statements().size()-1);
         if (!JavaAst.checkCanReturn(last)) needreturn = true;
       }
      if (!needreturn) return;
   
      return_methods.add(md);
    }

}	// end of inner class ReturnNeededFinder



/********************************************************************************/
/*										*/
/*	Mapper to add returns							*/
/*										*/
/********************************************************************************/

private class ReturnAdder extends TreeMapper {

   private String for_whom;
   private Set<ASTNode> for_methods;

   ReturnAdder(String whom,Set<ASTNode> returns) {
      for_whom = whom;
      for_methods = returns;
    }

   @Override protected String getSpecificsName()   { return for_whom; }

   @Override void rewriteTree(ASTNode orig,ASTRewrite rw) {
      if (for_methods.contains(orig)) {
	 MethodDeclaration md = (MethodDeclaration) orig;
	 Type t = md.getReturnType2();
	 if (t == null) return;
	 JcompType jt = JavaAst.getJavaType(t);
	 if (jt == null) return;

	 Block b = md.getBody();
	 ListRewrite lrw = rw.getListRewrite(b,Block.STATEMENTS_PROPERTY);
	 AST ast = rw.getAST();
	 ReturnStatement r = ast.newReturnStatement();
	 Expression ex = jt.createDefaultValue(ast);
	 if (ex != null) r.setExpression(ex);
	 lrw.insertLast(r,null);
       }
    }

}	// end of inner class ReturnAdder




}	// end of class TransformAddReturn



/* end of TransformAddReturn.java */
