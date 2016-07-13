/********************************************************************************/
/*										*/
/*		TransformFixOldJunit.java					*/
/*										*/
/*	Convert junit 2.x, 3.x into junit 4.x where needed			*/
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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6Solution;
import edu.brown.cs.s6.common.S6SolutionSet;

public class TransformFixOldJunit extends TransformJava implements S6Constants, JavaConstants
{


/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public TransformFixOldJunit(String name)
{
   super(name);
}



/********************************************************************************/
/*										*/
/*	Entry points for transformation 					*/
/*										*/
/********************************************************************************/

@Override protected List<TreeMapper> findClassMappings(S6SolutionSet solset,
      TypeDeclaration td,S6Request.ClassSignature cs,S6Solution sol)
{
   MethodDeclaration constructorfix = null;
   if (td.getSuperclassType() != null) {
      boolean havedflt = false;
      JcompType jt = JavaAst.getJavaType(td.getSuperclassType());
      if (jt != null && jt.getName().equals("junit.framework.TestCase")) {
	 for (MethodDeclaration md: td.getMethods()) {
	    if (md.isConstructor()) {
	       if (!Modifier.isPublic(md.getModifiers())) continue;
	       if (md.parameters().size() == 0) havedflt = true;
	       else {
		  for (Object o : md.getBody().statements()) {
		     if (o instanceof SuperConstructorInvocation) {
			if (constructorfix == null) constructorfix = md;
			else if (md.parameters().size() < constructorfix.parameters().size())
			   constructorfix = md;
		      }
		   }
		}
	
	     }
	  }
	 if (havedflt) constructorfix = null;
       }
    }
	
   List<TreeMapper> rslt = new ArrayList<TreeMapper>();
   if (constructorfix != null) rslt.add(new TestCaseMapper(constructorfix));

   if (rslt.isEmpty()) return null;

   return rslt;
}





/********************************************************************************/
/*										*/
/*	Mapping to fix TestCase constructor					*/
/*										*/
/********************************************************************************/

private class TestCaseMapper extends TreeMapper {

   private MethodDeclaration fix_constructor;

   TestCaseMapper(MethodDeclaration mthds) {
      fix_constructor = mthds;
    }

   @SuppressWarnings("unchecked")
   @Override void rewriteTree(ASTNode orig,ASTRewrite rw) {
      if (orig == fix_constructor) {
	 AST ast = rw.getAST();
	 JcompType jt = JavaAst.getJavaType(orig);
	 ConstructorInvocation ci = ast.newConstructorInvocation();
	 for (JcompType pt : jt.getComponents()) {
	    Expression ex = pt.createNonNullValue(ast);
	    ci.arguments().add(ex);
	  }
	 Block blk = ast.newBlock();
	 blk.statements().add(ci);
	 MethodDeclaration md = ast.newMethodDeclaration();
	 md.setConstructor(true);
	 SimpleName sn = JavaAst.getSimpleName(ast,fix_constructor.getName().getIdentifier());
	 md.setName(sn);
	 md.setBody(blk);
	 Modifier mod = ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD);
	 md.modifiers().add(mod);
	 ListRewrite lrw = rw.getListRewrite(orig.getParent(),TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
	 lrw.insertBefore(md,orig,null);
       }
    }
}



}	// end of class TransformFixOldJunit




/* end of TransformFixOldJunit.java */

