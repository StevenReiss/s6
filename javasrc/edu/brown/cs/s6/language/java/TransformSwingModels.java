/********************************************************************************/
/*										*/
/*		TransformSwingModels.java					*/
/*										*/
/*	Replace user models with internal models for lists, trees, tables	*/
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
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import edu.brown.cs.ivy.jcomp.JcompSymbol;
import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6Solution;
import edu.brown.cs.s6.common.S6SolutionSet;

public class TransformSwingModels extends TransformJava implements S6Constants, JavaConstants
{


/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public TransformSwingModels(String name)
{
   super(name);
}



/********************************************************************************/
/*										*/
/*	Replace user models with local models					*/
/*										*/
/********************************************************************************/

@Override protected TreeMapper findPackageMapping(S6SolutionSet ss,
      CompilationUnit cu,
      S6Request.PackageSignature psg,S6Solution sol)
{
   ModelFinder mf = new ModelFinder();
   cu.accept(mf);

   if (!mf.hasChanges()) return null;

   return new ModelFixup(mf);
}


/********************************************************************************/
/*										*/
/*	Visitor to find items to replace					*/
/*										*/
/********************************************************************************/

private static class ModelFinder extends ASTVisitor {

   private  Map<ASTNode,String> to_fix;

   ModelFinder() {
      to_fix = new HashMap<ASTNode,String>();
    }

   boolean hasChanges() 		{ return !to_fix.isEmpty(); }
   String getFixType(ASTNode n) 	{ return to_fix.get(n); }

   @Override public void endVisit(MethodInvocation n) {
      JcompSymbol js = JavaAst.getReference(n);
      List<?> args = n.arguments();
      if (js != null && js.getName().equals("setModel") && args.size() == 1) {
	 ASTNode a1 = (ASTNode) args.get(0);
	 JcompType jt = js.getClassType();
	 if (jt != null) {
	    String tnm = jt.getName();
	    if (tnm.equals("javax.swing.JList")) {
	       to_fix.put(a1,"List");
	     }
	    else if (tnm.equals("javax.swing.JTable")) {
	       to_fix.put(a1,"Table");
	     }
	    else if (tnm.equals("javax.swing.JTree")) {
	       to_fix.put(a1,"Tree");
	     }
	  }
       }
    }

   @Override public void endVisit(ClassInstanceCreation n) {
      JcompType jt = JavaAst.getJavaType(n.getType());
      List<?> args = n.arguments();
      JcompSymbol js = JavaAst.getReference(n);
      if (js == null) return;
      JcompType mty = js.getType();
      List<JcompType> atys = mty.getComponents();
      if (atys.size() == 0 || args == null || args.size() == 0) return;
      ASTNode arg = (ASTNode) args.get(0);
      String atnm = atys.get(0).getName();
      if (!atnm.contains("Model")) return;

      if (jt != null && n.getAnonymousClassDeclaration() == null) {
	 String tnm = jt.getName();
	 if (tnm.equals("javax.swing.JList")) {
	    to_fix.put(arg,"List");
	  }
	 else if (tnm.equals("javax.swing.JTable")) {
	    to_fix.put(arg,"Table");
	    // need to handle column and selection models here if needed
	  }
	 else if (tnm.equals("javax.swing.JTree")) {
	    to_fix.put(arg,"Tree");
	  }
       }
    }

}	// end of inner class ModelFinder



/********************************************************************************/
/*										*/
/*	Mapper to use our models						*/
/*										*/
/********************************************************************************/

private class ModelFixup extends TreeMapper {

   private ModelFinder model_finder;

   ModelFixup(ModelFinder mf) {
      model_finder = mf;
    }

   @Override void rewriteTree(ASTNode orig,ASTRewrite rw) {
      String typ = model_finder.getFixType(orig);
      if (typ == null) return;
      String cnm = "edu.brown.cs.s6.runner.RunnerSwingModels";
      String nm = "get" + typ + "Model";
      AST ast = rw.getAST();
      MethodInvocation mi = ast.newMethodInvocation();
      Name n = JavaAst.getQualifiedName(ast,cnm);
      mi.setExpression(n);
      SimpleName sn = JavaAst.getSimpleName(ast,nm);
      mi.setName(sn);
      String snm = null;
      switch (typ) {
         case "List" :
            snm = "javax.swing.ListModel";
            break;
         case "Table" :
            snm = "javax.swing.table.TableModel";
            break;
         case "Tree" :
            snm = "javax.swing.tree.TreeModel";
            break;
       }
      if (snm != null) {
         CastExpression ce = ast.newCastExpression();
         Name sn1 = JavaAst.getQualifiedName(ast,snm);
         Type st1 = ast.newSimpleType(sn1);
         ce.setType(st1);
         ce.setExpression(mi);
         rw.replace(orig,ce,null);
       }
      else {
         rw.replace(orig,mi,null);
       }
    }
}	// end of inner class ModelFixup




}	// end of class TransformSwingModels




/* end of TransformSwingModels.java */

