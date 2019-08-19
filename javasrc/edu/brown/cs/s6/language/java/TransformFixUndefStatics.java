/********************************************************************************/
/*										*/
/*		TransformFixUndefStatics.java					*/
/*										*/
/*	Replace undefined final static definitions with something		*/
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
import java.util.Set;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.ivy.jcomp.JcompTyper;
import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6Solution;
import edu.brown.cs.s6.common.S6SolutionSet;

public class TransformFixUndefStatics extends TransformJava implements JavaConstants, S6Constants
{




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public TransformFixUndefStatics(String name)
{
   super(name);
}



/********************************************************************************/
/*										*/
/*	Entry points for transfomration 					*/
/*										*/
/********************************************************************************/

@Override protected TreeMapper findClassMapping(S6SolutionSet solset,
      TypeDeclaration td,S6Request.ClassSignature cs,S6Solution sol)
{
   if (!JavaAst.checkTypeSignature(td,cs,S6SignatureType.FULL,null)) return null;

   StaticFinder sf = new StaticFinder();
   td.accept(sf);

   Set<ASTNode> fixes = sf.getFixes();
   if (fixes == null || fixes.size() == 0) return null;

   StaticMapper sm = new StaticMapper(fixes);

   return sm;
}




/********************************************************************************/
/*										*/
/*	Class to find fix candidates						*/
/*										*/
/********************************************************************************/

private class StaticFinder extends  ASTVisitor {

   private Set<ASTNode> fix_nodes;
   private boolean check_defs;

   StaticFinder() {
      fix_nodes = new HashSet<ASTNode>();
      check_defs = false;
    }

   Set<ASTNode> getFixes()			{ return fix_nodes; }

   @Override public boolean visit(FieldDeclaration fd) {
      if (Modifier.isFinal(fd.getModifiers()) &&
	    Modifier.isStatic(fd.getModifiers())) {
	 check_defs = true;
       }
      return true;
    }

   @Override public void endVisit(FieldDeclaration fd) {
      check_defs = false;
    }

   @Override public void endVisit(VariableDeclarationFragment vdf) {
      Expression init = vdf.getInitializer();
      if (init != null && check_defs) {
	 JcompType etyp = JavaAst.getExprType(init);
	 if (etyp != null && etyp.isErrorType() && vdf.getExtraDimensions() == 0) {
	    fix_nodes.add(vdf);
	  }
       }
    }

}	// end of inner class StaticFinder




/********************************************************************************/
/*										*/
/*	Tree mapper to fix undefined final statics				*/
/*										*/
/********************************************************************************/

private class StaticMapper extends TreeMapper {

   private Set<ASTNode> fix_nodes;

   StaticMapper(Set<ASTNode> fix) {
      fix_nodes = new HashSet<ASTNode>(fix);
    }

   

   @Override void rewriteTree(ASTNode orig,ASTRewrite rw) {
      if (!fix_nodes.contains(orig)) return;
      VariableDeclarationFragment vdf = (VariableDeclarationFragment) orig;
      JcompType jt = null;
      FieldDeclaration fd = null;
      VariableDeclarationStatement vds = null;
      if (vdf.getParent() instanceof FieldDeclaration) {
         fd = (FieldDeclaration) vdf.getParent();
         jt = JavaAst.getJavaType(fd.getType());
       }
      else if (vdf.getParent() instanceof VariableDeclarationStatement) {
         vds = (VariableDeclarationStatement) vdf.getParent();
         jt = JavaAst.getJavaType(vds.getType());
       }
      if (jt == null) return;
      Expression ex = vdf.getInitializer();
      AST ast = rw.getAST();
      Expression newex = null;
      Type newty = null;
      if (jt.isCompiledType()) {
         JcompTyper typer = JavaAst.getTyper(orig);
         JcompType otyp = typer.findSystemType("java.lang.Object");
         Name nm = JavaAst.getQualifiedName(ast,"java.lang.Object");
         newty = ast.newSimpleType(nm);
         newex = otyp.createNonNullValue(ast);
       }
      else {
         newex = jt.createDefaultValue(ast);
       }
   
      if (newty != null) {
         if (fd != null) rw.replace(fd.getType(),newty,null);
         else if (vds != null) rw.replace(vds.getType(),newty,null);
       }
      if (newex != null) rw.replace(ex,newex,null);
    }

}


}	// end of class TransformFixUndefStatics




/* end of TransformFixUndefStatics.java */

