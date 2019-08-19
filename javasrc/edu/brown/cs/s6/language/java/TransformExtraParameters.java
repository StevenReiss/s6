/********************************************************************************/
/*										*/
/*		TransformExtraParameters.java					*/
/*										*/
/*	Transformation that handles extra parameters				*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/language/java/TransformExtraParameters.java,v 1.16 2016/07/18 23:05:26 spr Exp $ */


/*********************************************************************************
 *
 * $Log: TransformExtraParameters.java,v $
 * Revision 1.16  2016/07/18 23:05:26  spr
 * Update transforms for applications and UI.
 *
 * Revision 1.15  2015/09/23 17:54:53  spr
 * Version to handle andriod UI applications.
 *
 * Revision 1.14  2014/08/29 15:16:08  spr
 * Updates for suise, testcases.
 *
 * Revision 1.13  2014/02/26 14:06:44  spr
 * Add transformations for user interfaces.
 *
 * Revision 1.12  2013/09/13 20:33:04  spr
 * Add calls for UI search.
 *
 * Revision 1.11  2013-05-09 12:26:20  spr
 * Minor changes to start ui fixups.
 *
 * Revision 1.10  2012-06-20 12:21:33  spr
 * Initial fixes for UI search
 *
 * Revision 1.9  2012-06-11 14:07:49  spr
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
 * Revision 1.3  2008-07-17 13:46:45  spr
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


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
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



public class TransformExtraParameters extends TransformJava implements S6Constants, JavaConstants {




/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private static final int	MAX_REMOVE = 1;





/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public TransformExtraParameters(String name)
{
   super(name);
}



/********************************************************************************/
/*										*/
/*	Prevent using this transform for a class fragment			*/
/*										*/
/********************************************************************************/

@Override protected boolean applyClassTransform(S6SolutionSet sols,S6Solution sol,
						   TypeDeclaration td,
						   S6Request.ClassSignature csg)
{
   // return super.applyClassTransform(sols,sol,td,csg);
   return false;
}




/********************************************************************************/
/*										*/
/*	Method to create mapper to actually remove first extra parameters	*/
/*										*/
/********************************************************************************/

@Override protected Collection<TreeMapper> findMethodMappings(S6SolutionSet ss,MethodDeclaration md,
								 S6Request.MethodSignature ms,
								 S6Solution sol)
{
   if (JavaAst.checkMethodSignature(md,ms,S6SignatureType.PARAMETERS)) return null;
   if (!JavaAst.checkMethodSignature(md,ms,S6SignatureType.RETURN_EXCEPTIONS)) return null;
   if (ss.getRequest().getSearchType() != S6SearchType.METHOD) {
      if (!ms.getName().equals("<init>")) return null;
    }

   JcompTyper jt = JavaAst.getTyper(md);
   if (jt == null) return null;

   List<SingleVariableDeclaration> elims = new ArrayList<SingleVariableDeclaration>();
   Set<SingleVariableDeclaration> arrays = new HashSet<SingleVariableDeclaration>();

   for (Iterator<?> it = md.parameters().iterator(); it.hasNext(); ) {
      SingleVariableDeclaration svd = (SingleVariableDeclaration) it.next();
      HashSet<SingleVariableDeclaration> used = new HashSet<SingleVariableDeclaration>();
      boolean findall = true;
      for (String ptyp : ms.getParameterTypeNames()) {
	 JcompType t1 = jt.findSystemType(ptyp);
	 if (t1 == null) return null;
	 boolean fnd = false;
	 for (Iterator<?> it1 = md.parameters().iterator(); !fnd && it1.hasNext(); ) {
	    SingleVariableDeclaration nvd = (SingleVariableDeclaration) it1.next();
	    if (nvd == svd) continue;
	    if (used.contains(nvd)) continue;
	    JcompType t2 = JavaAst.getJavaType(nvd);
	    if (t2 != null && compatibleParams(t1,t2)) {
	       used.add(nvd);
	       fnd = true;
	       break;
	     }
	  }
	 if (!fnd) {
	    findall = false;
	    break;
	  }
       }
      JcompSymbol jd = JavaAst.getDefinition(svd);
      if (jd == null) continue;
      JcompType t3 = jd.getType();
      if (findall) {
	 if (checkRemove(t3)) {
	    elims.add(svd);
	  }
       }
      else {
	 if (t3.isArrayType() || t3.isCompatibleWith(jt.findSystemType("java.util.Collection")))
	    arrays.add(svd);
       }
    }

   if (elims.size() == 0) return null;

   Collection<TreeMapper> solns = new ArrayList<TreeMapper>();

   if (ms.getParameterTypeNames().size() == 1 && md.parameters().size() <= 3 &&
	 JavaAst.checkIfRecursive(md)) {
      for (String ptyp : ms.getParameterTypeNames()) {
	 JcompType t1 = jt.findSystemType(ptyp);
	 if (t1 == null) continue;
	 for (Iterator <?> it = md.parameters().iterator(); it.hasNext(); ) {
	    SingleVariableDeclaration svd = (SingleVariableDeclaration) it.next();
	    JcompType t2 = JavaAst.getJavaType(svd);
	    if (t2 == null) continue;
	    if (compatibleParams(t1,t2)) {
	       addCallSolutions(md,ms,svd,elims,arrays,solns);
	     }
	  }
       }
    }

   if (elims.size() > MAX_REMOVE) {
      // too many -- check only the last
      while (elims.size() > 1) elims.remove(0);
    }

   for (SingleVariableDeclaration svd : elims) {
      addSolutions(md,svd,arrays,solns);
    }

   if (solns.size() == 0) return null;

   return solns;
}



private boolean compatibleParams(JcompType tgt,JcompType act)
{
   if (act == null || tgt == null) return false;

   if (act.isCompatibleWith(tgt)) return true;
   if (tgt.isCompatibleWith(act)) {
      if (tgt.isArrayType() && tgt.getBaseType().isNumericType()) return true;
      if (tgt.isNumericType()) return true;
    }

   return false;
}




/********************************************************************************/
/*										*/
/*	Check if we want to remove this parameter				*/
/*										*/
/********************************************************************************/

private boolean checkRemove(JcompType jt)
{
   if (jt == null) return true;
   if (jt.isPrimitiveType()) return true;
   if (jt.getName().equals("java.lang.String")) return true;

   return false;
}




/********************************************************************************/
/*										*/
/*	Top level transformation method 					*/
/*										*/
/********************************************************************************/

private void addSolutions(MethodDeclaration md,SingleVariableDeclaration svd,
			     Set<SingleVariableDeclaration> arrays,
			     Collection<TreeMapper> solns)
{
   List<Expression> values = getCandidateValues(svd,md,arrays);

   if (values.size() == 0) return;

   for (Expression e : values) {
      TreeMapper tm = new ElimParam(md,svd,e);
      solns.add(tm);
    }
}



private void addCallSolutions(MethodDeclaration md,S6Request.MethodSignature sign,
      SingleVariableDeclaration keep,
      List<SingleVariableDeclaration> elims,Set<SingleVariableDeclaration> arrays,
      Collection<TreeMapper> solns)
{
   Map<SingleVariableDeclaration,List<Expression>> valuemap = new HashMap<SingleVariableDeclaration,List<Expression>>();
   List<SingleVariableDeclaration> upds = new ArrayList<SingleVariableDeclaration>();
   for (Iterator <?> it = md.parameters().iterator(); it.hasNext(); ) {
      SingleVariableDeclaration svd = (SingleVariableDeclaration) it.next();
      if (svd == keep) continue;
      if (!elims.contains(svd)) return;
      List<Expression> values = getCandidateValues(svd,md,arrays);
      if (values.size() == 0) return;
      valuemap.put(svd,values);
      upds.add(svd);
    }

   List<Expression> vals = new ArrayList<Expression>();
   addCallSolutions(md,sign,keep,upds,valuemap,vals,solns);
}


private void addCallSolutions(MethodDeclaration md,S6Request.MethodSignature sign,
      SingleVariableDeclaration keep,List<SingleVariableDeclaration> upds,
      Map<SingleVariableDeclaration,List<Expression>> valuemap,List<Expression> vals,
      Collection<TreeMapper> solns)
{
   int idx = vals.size();
   if (idx >= upds.size()) {
      StubParam sp = new StubParam(md,sign,keep,upds,vals);
      solns.add(sp);
      return;
    }

   SingleVariableDeclaration next = upds.get(idx);
   List<Expression> cands = valuemap.get(next);
   for (Expression c : cands) {
      vals.add(c);
      addCallSolutions(md,sign,keep,upds,valuemap,vals,solns);
      vals.remove(idx);
    }
}



/********************************************************************************/
/*										*/
/*	Method to compute candidate values					*/
/*										*/
/********************************************************************************/

private List<Expression> getCandidateValues(SingleVariableDeclaration svd,MethodDeclaration md,
					       Set<SingleVariableDeclaration> arrays)
{
   AST ast = md.getAST();

   JcompType jt = JavaAst.getJavaType(svd);

   if (jt.isBooleanType()) {
      List<Expression> rslt = new ArrayList<Expression>();
      rslt.add(ast.newBooleanLiteral(false));
      rslt.add(ast.newBooleanLiteral(true));
      return rslt;
    }

   ValueFinder vf = new ValueFinder(svd);
   md.accept(vf);

   List<Expression> vals = vf.getResults();
   if (jt.isNumericType()) {
      vals.add(JavaAst.newNumberLiteral(ast,1));
      vals.add(JavaAst.newNumberLiteral(ast,0));
    }
   else if (!jt.isPrimitiveType()) {
      vals.add(ast.newNullLiteral());
    }

   if (jt.getName().equals("int") && !arrays.isEmpty()) {
      for (SingleVariableDeclaration pvd : arrays) {
	 JcompSymbol jd = JavaAst.getDefinition(pvd);
	 JcompType t0 = jd.getType();
	 if (t0.isArrayType()) {
	    FieldAccess facc = ast.newFieldAccess();
	    facc.setName(JavaAst.getSimpleName(ast,"length"));
	    facc.setExpression(JavaAst.getSimpleName(ast,jd.getName()));
	    vals.add(facc);
	  }
	 else { 	// Collection type
	    MethodInvocation miv = ast.newMethodInvocation();
	    miv.setName(JavaAst.getSimpleName(ast,"size"));
	    miv.setExpression(JavaAst.getSimpleName(ast,jd.getName()));
	    vals.add(miv);
	  }
       }
    }

   return vals;
}




/********************************************************************************/
/*										*/
/*	Tree walker to find candidate values					*/
/*										*/
/********************************************************************************/

private class ValueFinder extends ASTVisitor {

   private List<Expression> value_exprs;
   private JcompSymbol match_value;

   ValueFinder(SingleVariableDeclaration svd) {
      value_exprs = new ArrayList<Expression>();
      match_value = JavaAst.getDefinition(svd);
    }

   List<Expression> getResults()		{ return value_exprs; }

   public void endVisit(Assignment v) {
      if (JavaAst.getReference(v.getLeftHandSide()) == match_value) {
	 addConstant(v.getRightHandSide());
       }
    }

   public void endVisit(InfixExpression v) {
      InfixExpression.Operator op = v.getOperator();
      if (op == InfixExpression.Operator.EQUALS ||
	     op == InfixExpression.Operator.NOT_EQUALS ||
	     op == InfixExpression.Operator.GREATER ||
	     op == InfixExpression.Operator.GREATER_EQUALS ||
	     op == InfixExpression.Operator.LESS ||
	     op == InfixExpression.Operator.LESS_EQUALS) {
	 if (JavaAst.getReference(v.getLeftOperand()) == match_value) {
	    addConstant(v.getRightOperand());
	  }
	 else if (JavaAst.getReference(v.getRightOperand()) == match_value) {
	    addConstant(v.getLeftOperand());
	  }
       }
    }

   public void endVisit(SwitchStatement v) {
      if (JavaAst.getReference(v.getExpression()) == match_value) {
	 for (Iterator<?> it = v.statements().iterator(); it.hasNext(); ) {
	    Statement s = (Statement) it.next();
	    if (s instanceof SwitchCase) {
	       SwitchCase sc = (SwitchCase) s;
	       Expression ex = sc.getExpression();
	       if (ex != null) value_exprs.add(ex);
	     }
	  }
       }
    }

   // handle x.equals("string")

   private void addConstant(Expression e) {
      if (e instanceof CharacterLiteral || e instanceof StringLiteral) {
	 value_exprs.add(e);
       }
      else if (e instanceof NumberLiteral) {
	 NumberLiteral nl = (NumberLiteral) e;
	 String s = nl.getToken();
	 if (!s.equals("0")) value_exprs.add(e);
       }
      // handle e being an enumeration constant
      // handle e being a reference to a static variable
    }

}	// end of subclass ValueFinder




/********************************************************************************/
/*										*/
/*	TreeMapper for single parameter and single value			*/
/*										*/
/********************************************************************************/

private class ElimParam extends TreeMapper {

   private SingleVariableDeclaration param_decl;
   private Expression new_value;
   private Block method_body;

   ElimParam(MethodDeclaration md,SingleVariableDeclaration svd,Expression e) {
      param_decl = svd;
      new_value = e;
      method_body = md.getBody();
    }

   @Override protected String getSpecificsName() {
      return param_decl.getName().getIdentifier();
   }

   @SuppressWarnings("unchecked")
   void rewriteTree(ASTNode orig,ASTRewrite rw) {
      if (orig == param_decl) {
	 rw.remove(orig,null);
       }
      else if (orig == method_body) {
	 AST ast = orig.getAST();
	 Block bdy = (Block) orig;
	 List<?> stmts = bdy.statements();
	 int idx = 0;
	 if (stmts.size() > 0) {
	    if (stmts.get(0) instanceof SuperConstructorInvocation) ++idx;
	    else if (stmts.get(0) instanceof ConstructorInvocation) ++idx;
	    // TODO: This fails if the variable appears in the this/super statement
	  }
	 VariableDeclarationFragment vdf = ast.newVariableDeclarationFragment();
	 vdf.setName((SimpleName) rw.createCopyTarget(param_decl.getName()));
	 if (new_value.getParent() != null)
	    new_value = (Expression) rw.createCopyTarget(new_value);
	 vdf.setInitializer(new_value);
	 for (Object o : param_decl.extraDimensions()) {
	    vdf.extraDimensions().add(o);
	  }
	 VariableDeclarationStatement vds = ast.newVariableDeclarationStatement(vdf);
	 vds.setType((Type) rw.createCopyTarget(param_decl.getType()));
	 ListRewrite lrw = rw.getListRewrite(bdy,Block.STATEMENTS_PROPERTY);
	 lrw.insertAt(vds,idx,null);
       }
    }

}	// end of subclass ElimParam



/********************************************************************************/
/*										*/
/*	Tree mapper to add a stub routine for extra parameters			*/
/*										*/
/********************************************************************************/

private class StubParam extends TreeMapper {

   private List<SingleVariableDeclaration> replace_decls;
   private List<Expression> replace_values;
   private SingleVariableDeclaration keep_decl;
   private MethodDeclaration for_method;
   private S6Request.MethodSignature using_signature;
   private String new_name;
   private JcompTyper use_typer;

   StubParam(MethodDeclaration md,S6Request.MethodSignature sgn,SingleVariableDeclaration keep,
	 List<SingleVariableDeclaration> repl,
	 List<Expression> vals) {
      for_method = md;
      use_typer = JavaAst.getTyper(md);
      if (use_typer == null) {
	 System.err.println("TYPER UNDEFINED FOR " + md);
       }
      using_signature = sgn;
      keep_decl = keep;
      replace_decls = new ArrayList<SingleVariableDeclaration>(repl);
      replace_values = new ArrayList<Expression>(vals);
      new_name = null;
      String onm = for_method.getName().getIdentifier();
      if (onm.equals(using_signature.getName())) {
	 new_name = "S6_" + using_signature.getName();
       }
    }

   @Override protected String getSpecificsName() {
      return "Call_" + keep_decl.getName().getIdentifier();
   }

   @SuppressWarnings("unchecked")
   void rewriteTree(ASTNode orig,ASTRewrite rw) {
      if (orig == for_method) {
	 AST ast = rw.getAST();

	 MethodDeclaration mdcopy = (MethodDeclaration) ASTNode.copySubtree(ast,orig);
	 if (new_name != null) {
	    SimpleName sn = JavaAst.getSimpleName(ast,new_name);
	    mdcopy.setName(sn);
	  }
	 ListRewrite lrw = rw.getListRewrite(orig.getParent(),(ChildListPropertyDescriptor) orig.getLocationInParent());
	 lrw.insertAfter(mdcopy,orig,null);

	 String ret = using_signature.getReturnTypeName();
	 JcompType rtyp = use_typer.findSystemType(ret);

	 if (new_name == null) {
	    rw.set(orig,MethodDeclaration.NAME_PROPERTY,JavaAst.getSimpleName(ast,using_signature.getName()),null);
	  }
	 Block blk = ast.newBlock();
	 MethodInvocation call = ast.newMethodInvocation();
	 SimpleName cnm = null;
	 if (new_name != null) cnm = JavaAst.getSimpleName(ast,new_name);
	 else cnm = JavaAst.getSimpleName(ast,for_method.getName().getIdentifier());
	 call.setName(cnm);
	 ListRewrite plrw = rw.getListRewrite(orig,MethodDeclaration.PARAMETERS_PROPERTY);
	 for (Iterator<?> it = for_method.parameters().iterator(); it.hasNext(); ) {
	    SingleVariableDeclaration p = (SingleVariableDeclaration) it.next();
	    Expression ex = null;
	    if (p == keep_decl) {
	       ex = JavaAst.getSimpleName(ast,p.getName().getIdentifier());
	     }
	    else {
	       plrw.remove(p,null);
	       for (int i = 0; i < replace_decls.size(); ++i) {
		  if (replace_decls.get(i) == p) {
		     ex = replace_values.get(i);
		     if (ex.getParent() != null)
			ex = (Expression) rw.createCopyTarget(ex);
		     else
			ex = (Expression) ASTNode.copySubtree(ast,ex);
		     break;
		   }
		}
	     }
	    call.arguments().add(ex);
	  }
	 if (rtyp.isVoidType()) {
	    blk.statements().add(ast.newExpressionStatement(call));
	  }
	 else {
	    ReturnStatement rts = ast.newReturnStatement();
	    rts.setExpression(call);
	    blk.statements().add(rts);
	  }
	 rw.set(orig,MethodDeclaration.BODY_PROPERTY,blk,null);
       }
    }

}	// end of subclass ElimParam



}	// end of class TransformExtraParameters




/* end of TransformExtraParameters.java */
