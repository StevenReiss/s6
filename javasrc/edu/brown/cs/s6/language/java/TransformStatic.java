/********************************************************************************/
/*										*/
/*		TransformStatic.java						*/
/*										*/
/*	Transformation that handles making a method static			*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/language/java/TransformStatic.java,v 1.13 2015/09/23 17:54:54 spr Exp $ */


/*********************************************************************************
 *
 * $Log: TransformStatic.java,v $
 * Revision 1.13  2015/09/23 17:54:54  spr
 * Version to handle andriod UI applications.
 *
 * Revision 1.12  2014/08/29 15:16:10  spr
 * Updates for suise, testcases.
 *
 * Revision 1.11  2013/09/13 20:33:05  spr
 * Add calls for UI search.
 *
 * Revision 1.10  2013-05-09 12:26:22  spr
 * Minor changes to start ui fixups.
 *
 * Revision 1.9  2012-06-11 14:07:50  spr
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
 * Revision 1.3  2008-07-17 13:46:46  spr
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


import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import edu.brown.cs.ivy.jcomp.JcompSymbol;
import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6Solution;
import edu.brown.cs.s6.common.S6SolutionSet;


public class TransformStatic extends TransformJava implements S6Constants, JavaConstants {




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public TransformStatic(String name)
{
   super(name);
}




/********************************************************************************/
/*										*/
/*	Method to create mapper to do actual return type changes		*/
/*										*/
/********************************************************************************/

@Override protected TreeMapper findMethodMapping(S6SolutionSet ss,MethodDeclaration md,
						    S6Request.MethodSignature ms,
                                                    S6Solution sol)
{
   if (!JavaAst.checkMethodSignature(md,ms,S6SignatureType.ALL)) return null;
   if (Modifier.isStatic(md.getModifiers())) return null;
   if (md.isConstructor()) return null;

   if (ss.getSearchType() != S6SearchType.METHOD) {
      TypeDeclaration td = null;
      for (ASTNode n = md; n != null; n = n.getParent()) {
	 if (n instanceof TypeDeclaration) {
	    td = (TypeDeclaration) n;
	    break;
	  }
       }
      if (td != null) {
	 if (td.getSuperclassType() != null) return null;
	 if (td.superInterfaceTypes().size() > 0) return null;
       }
    }

   StaticChecker sc = new StaticChecker(md);
   md.accept(sc);

   if (!sc.getCanBeStatic()) return null;

   return new StaticMapper(ms.getName(),md);
}



/********************************************************************************/
/*										*/
/*	Class to find return statements in a method				*/
/*										*/
/********************************************************************************/

private static class StaticChecker extends ASTVisitor {

   private boolean canbe_static;

   StaticChecker(MethodDeclaration md) {
      JavaAst.getDefinition(md);
      canbe_static = true;
    }

   boolean getCanBeStatic()			{ return canbe_static; }

   public @Override void endVisit(MethodInvocation n) {
      Expression e = n.getExpression();
      if (e == null) canbe_static = false;
    }

   public @Override void endVisit(SuperMethodInvocation n) {
      canbe_static = false;
    }

   public @Override void endVisit(ThisExpression n) {
      canbe_static = false;
    }

   public @Override void endVisit(FieldAccess n) {
      JcompSymbol js = JavaAst.getReference(n.getName());
      if (js != null && !js.isStatic()) canbe_static = false;
    }

   public @Override void endVisit(SimpleName n) {
      JcompSymbol js = JavaAst.getReference(n);
      if (js != null && js.isFieldSymbol() && !js.isStatic()) {
	 if (n.getParent() instanceof QualifiedName) ;
	 else canbe_static = false;
       }
    }

}	// end of subclass StaticChecker



/********************************************************************************/
/*										*/
/*	Class to handle return type mappings					*/
/*										*/
/********************************************************************************/

private class StaticMapper extends TreeMapper {

   private String for_whom;
   private MethodDeclaration change_method;

   StaticMapper(String whom,MethodDeclaration md) {
      for_whom = whom;
      change_method = md;
    }

   @Override protected String getSpecificsName()   { return for_whom; }

   void rewriteTree(ASTNode orig,ASTRewrite rw) {
      if (orig != change_method) return;

      MethodDeclaration nmd = (MethodDeclaration) orig;
      ListRewrite lrw = rw.getListRewrite(orig,nmd.getModifiersProperty());
      Modifier mod = nmd.getAST().newModifier(Modifier.ModifierKeyword.STATIC_KEYWORD);
      lrw.insertLast(mod,null);
    }

}	// end of subclass StaticMapper




}	// end of class TransformStatic





/* end of TransformStatic.java */
