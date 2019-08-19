/********************************************************************************/
/*										*/
/*		TransformIntParams.java 					*/
/*										*/
/*	Transformation that handles parameter type changes for numerics 	*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/language/java/TransformIntParams.java,v 1.7 2015/09/23 17:54:53 spr Exp $ */


/*********************************************************************************
 *
 * $Log: TransformIntParams.java,v $
 * Revision 1.7  2015/09/23 17:54:53  spr
 * Version to handle andriod UI applications.
 *
 * Revision 1.6  2014/08/29 15:16:09  spr
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
 * Revision 1.2  2009-05-12 22:28:48  spr
 * Fix ups to make user context work.
 *
 * Revision 1.1  2008-11-12 13:53:07  spr
 * Start of context management.
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.language.java;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
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



public class TransformIntParams extends TransformJava implements S6Constants, JavaConstants {




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public TransformIntParams(String name)
{
   super(name);
}




/********************************************************************************/
/*										*/
/*	Method to create mapper to do actual parameter type changes		*/
/*										*/
/********************************************************************************/

@Override protected Collection<TreeMapper> findMethodMappings(S6SolutionSet ss,MethodDeclaration md,
						    S6Request.MethodSignature ms,S6Solution sol)
{
   if (JavaAst.checkMethodSignature(md,ms,S6SignatureType.PARAMETERS)) return null;
   if (md.isConstructor()) return null;

   List<String> mstyp = ms.getParameterTypeNames();
   List<?> mdtyp = md.parameters();

   if (mstyp.size() == 0) return null;
   if (mstyp.size() != mdtyp.size()) return null;

   JcompTyper jt = JavaAst.getTyper(md);
   if (jt == null) return null;

   Collection<TreeMapper> solns = new ArrayList<TreeMapper>();

   for (int i = 0; i < mstyp.size(); ++i) {
      JcompType t0 = jt.findSystemType(mstyp.get(i));
      SingleVariableDeclaration svd = (SingleVariableDeclaration) mdtyp.get(i);
      JcompSymbol js = JavaAst.getDefinition(svd);
      if (js == null) {
	 // System.err.println("EMPTY DEFINITION FOR " + svd);
	 return null;
       }
      JcompType t1 = js.getType();
      if (t0 == null || t1 == null) return null;
      if (t0 == t1) continue;
      if (checkComformable(t1,t0,ss,md,svd)) {
	 if (t1.isNumericType() && t0.isNumericType()) {
	    addSolutions(md,svd,t0,ms.getName(),solns);
	  }
       }
      else return null;
    }

   if (solns.size() == 0) return null;

   return solns;
}



/********************************************************************************/
/*										*/
/*	Method to find solutions						*/
/*										*/
/********************************************************************************/

private void addSolutions(MethodDeclaration md,SingleVariableDeclaration svd,JcompType tt,
			     String whom,Collection<TreeMapper> solns)
{
   NumericTypeMapper ntm = new NumericTypeMapper(whom,md,svd,tt,true);
   solns.add(ntm);
   ntm = new NumericTypeMapper(whom,md,svd,tt,false);
   solns.add(ntm);
}



/********************************************************************************/
/*										*/
/*	Class to handle return type mappings					*/
/*										*/
/********************************************************************************/

private class NumericTypeMapper extends TreeMapper {

   private String for_whom;
   private MethodDeclaration change_method;
   private JcompType new_type;
   private SingleVariableDeclaration for_variable;
   private String new_name;
   private String old_name;
   private String and_const;

   NumericTypeMapper(String whom,MethodDeclaration md,
        		SingleVariableDeclaration var,JcompType tt,boolean signed) {
      for_whom = whom;
      change_method = md;
      new_type = tt;
      for_variable = var;
      old_name = var.getName().getIdentifier();
      new_name = "s6__" + old_name;
      and_const = null;
      if (!signed) {
         if (tt.getName().equals("int")) and_const = "0xffffffff";
         else if (tt.getName().equals("short") || tt.getName().equals("char")) and_const = "0xffff";
         else if (tt.getName().equals("byte")) and_const = "0xff";
         JcompSymbol js = JavaAst.getDefinition(var);
         if (js != null) {
            JcompType t1 = js.getType();
            if (t1.getName().equals("long")) and_const += "L";
          }
       }
    }

   @Override protected String getSpecificsName()   { return for_whom; }

   @SuppressWarnings("unchecked")
   void rewriteTree(ASTNode orig,ASTRewrite rw) {
      if (orig == change_method.getBody()) {
	 AST ast = orig.getAST();
	 VariableDeclarationFragment vdf = ast.newVariableDeclarationFragment();
	 vdf.setName(JavaAst.getSimpleName(ast,old_name));
	 if (and_const == null) {
	    vdf.setInitializer(JavaAst.getSimpleName(ast,new_name));
	  }
	 else {
	    InfixExpression ifx = ast.newInfixExpression();
	    ifx.setOperator(InfixExpression.Operator.AND);
	    ifx.setLeftOperand(JavaAst.getSimpleName(ast,new_name));
	    ifx.setRightOperand(JavaAst.newNumberLiteral(ast,and_const));
	    vdf.setInitializer(ifx);
	  }
	 VariableDeclarationStatement vds = ast.newVariableDeclarationStatement(vdf);
	 Type t = for_variable.getType();
	 t = (Type) rw.createCopyTarget(t);
	 vds.setType(t);
	 vdf.extraDimensions().addAll(for_variable.extraDimensions());
	 ListRewrite lrw = rw.getListRewrite(orig,Block.STATEMENTS_PROPERTY);
	 lrw.insertFirst(vds,null);
       }
      else if (orig == for_variable) {
	 Type t = new_type.createAstNode(orig.getAST());
	 rw.set(orig,SingleVariableDeclaration.TYPE_PROPERTY,t,null);
	 SimpleName snm = JavaAst.getSimpleName(orig.getAST(),new_name);
	 rw.set(orig,SingleVariableDeclaration.NAME_PROPERTY,snm,null);
       }
    }

}	// end of subclass NumericTypeMapper




}	// end of class TransformIntParams





/* end of TransformIntParams.java */
