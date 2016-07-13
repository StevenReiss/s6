/********************************************************************************/
/*										*/
/*		TransformRemoveStatic.java					*/
/*										*/
/*	Convert a static method into a class method where appropriate		*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/language/java/TransformRemoveStatic.java,v 1.7 2015/09/23 17:54:54 spr Exp $ */


/*********************************************************************************
 *
 * $Log: TransformRemoveStatic.java,v $
 * Revision 1.7  2015/09/23 17:54:54  spr
 * Version to handle andriod UI applications.
 *
 * Revision 1.6  2014/08/29 15:16:10  spr
 * Updates for suise, testcases.
 *
 * Revision 1.5  2013/09/13 20:33:05  spr
 * Add calls for UI search.
 *
 * Revision 1.4  2013-05-09 12:26:21  spr
 * Minor changes to start ui fixups.
 *
 * Revision 1.3  2012-06-11 14:07:49  spr
 * add framework search; fix bugs
 *
 * Revision 1.2  2009-09-18 01:41:36  spr
 * Handle user testing.
 *
 * Revision 1.1  2009-05-12 22:33:25  spr
 * Add new transforms use for context mappings.
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.language.java;


import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import edu.brown.cs.ivy.jcomp.JcompSymbol;
import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.ivy.jcomp.JcompTyper;
import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6Solution;
import edu.brown.cs.s6.common.S6SolutionSet;



public class TransformRemoveStatic extends TransformJava implements S6Constants, JavaConstants {




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public TransformRemoveStatic(String name)
{
   super(name);
}



/********************************************************************************/
/*										*/
/*	Method to create mapper to actually remove first extra parameters	*/
/*										*/
/********************************************************************************/

@Override protected TreeMapper findMethodMapping(S6SolutionSet ss,MethodDeclaration md,
						    S6Request.MethodSignature ms,
						    S6Solution sol)
{
   if (JavaAst.checkMethodSignature(md,ms,S6SignatureType.PARAMETERS)) return null;
   if (!JavaAst.checkMethodSignature(md,ms,S6SignatureType.RETURN_EXCEPTIONS)) return null;

   int mds = md.getModifiers();
   if (!Modifier.isStatic(mds)) return null;
   if (ms.isStatic()) return null;

   JcompTyper jt = JavaAst.getTyper(md);

   String cnm = null;
   if (ss.getSearchType() != S6SearchType.METHOD) {
      ASTNode an = md;
      while (an != null && !(an instanceof AbstractTypeDeclaration)) an = an.getParent();
      if (an == null) return null;
      AbstractTypeDeclaration tan = (AbstractTypeDeclaration) an;
      cnm = JavaAst.getJavaTypeName(tan);
    }
   else {
      JavaContext ctx = JavaAst.getS6Context(md);
      if (ctx == null) return null;
      String cpkg = ss.getRequest().getPackage();
      if (cpkg == null) cpkg = ctx.getContextPackage();
      String ccls = ctx.getContextClass();
      if (ccls == null) return null;
      if (cpkg == null) cnm = ccls;
      else cnm = cpkg + "." + ccls;
    }
   if (cnm == null) return null;

   JcompSymbol param = null;
   SingleVariableDeclaration paramdecl = null;
   HashSet<SingleVariableDeclaration> used = new HashSet<SingleVariableDeclaration>();

   for (Iterator<?> it = md.parameters().iterator(); it.hasNext(); ) {
      SingleVariableDeclaration svd = (SingleVariableDeclaration) it.next();
      JcompType t2 = JavaAst.getJavaType(svd);
      if (t2.getName().equals(cnm)) {
	 used.add(svd);
	 param = JavaAst.getDefinition(svd);
	 paramdecl = svd;
	 break;
       }
    }
   if (param == null) return null;

   for (String ptyp : ms.getParameterTypeNames()) {
      JcompType t1 = jt.findSystemType(ptyp);
      if (t1 == null) return null;
      boolean fnd = false;
      for (Iterator<?> it1 = md.parameters().iterator(); !fnd && it1.hasNext(); ) {
	 SingleVariableDeclaration nvd = (SingleVariableDeclaration) it1.next();
	 if (used.contains(nvd)) continue;
	 JcompType t2 = JavaAst.getJavaType(nvd);
	 if (t2.isCompatibleWith(t1)) {
	    used.add(nvd);
	    fnd = true;
	    break;
	  }
       }
      if (!fnd) return null;
    }

   CheckAssigned ca = new CheckAssigned(param);
   md.accept(ca);
   boolean asg = ca.isAssigned();

   ElimThisParam tm = new ElimThisParam(md,paramdecl,param,asg);

   return tm;
}



/********************************************************************************/
/*										*/
/*	Tree walker to check if variable is assigned to 			*/
/*										*/
/********************************************************************************/

private class CheckAssigned extends ASTVisitor {

   private JcompSymbol match_value;
   private boolean is_assigned;

   CheckAssigned(JcompSymbol js) {
      match_value = js;
      is_assigned = false;
    }

   boolean isAssigned() 		{ return is_assigned; }

   public void endVisit(Assignment v) {
      if (JavaAst.getReference(v.getLeftHandSide()) == match_value) {
	 is_assigned = true;
       }
    }

}	// end of subclass ValueFinder




/********************************************************************************/
/*										*/
/*	TreeMapper for single parameter and single value			*/
/*										*/
/********************************************************************************/

private class ElimThisParam extends TreeMapper {

   private SingleVariableDeclaration param_decl;
   private JcompSymbol param_var;
   private Block method_body;
   private boolean use_assign;

   ElimThisParam(MethodDeclaration md,SingleVariableDeclaration svd,JcompSymbol js,boolean assign) {
      param_decl = svd;
      param_var = js;
      method_body = md.getBody();
      use_assign = assign;
    }

   @Override protected String getSpecificsName() {
      return param_decl.getName().getIdentifier();
   }

   void rewriteTree(ASTNode orig,ASTRewrite rw) {
      if (orig == param_decl) {
	 rw.remove(orig,null);
       }
      else if (orig == method_body && use_assign) {
	 AST ast = orig.getAST();
	 Block bdy = (Block) orig;
	 int idx = 0;
	 VariableDeclarationFragment vdf = ast.newVariableDeclarationFragment();
	 vdf.setName((SimpleName) rw.createCopyTarget(param_decl.getName()));
	 Expression newvalue = ast.newThisExpression();
	 vdf.setInitializer(newvalue);
	 VariableDeclarationStatement vds = ast.newVariableDeclarationStatement(vdf);
	 vds.setType((Type) rw.createCopyTarget(param_decl.getType()));
	 ListRewrite lrw = rw.getListRewrite(bdy,Block.STATEMENTS_PROPERTY);
	 lrw.insertAt(vds,idx,null);
       }
      else if (!use_assign && orig instanceof SimpleName &&
		  JavaAst.getReference(orig) == param_var) {
	 AST ast = orig.getAST();
	 Expression newvalue = ast.newThisExpression();
	 rw.replace(orig,newvalue,null);
       }
    }

}	// end of subclass ElimThisParam




}	// end of class TransformRemoveStatic




/* end of TransformRemoveStatic.java */
