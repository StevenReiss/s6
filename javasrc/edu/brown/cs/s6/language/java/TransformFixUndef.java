/********************************************************************************/
/*										*/
/*		TransformFixUndef.java						*/
/*										*/
/*	Transformation that handles undefined fixups where possible		*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/language/java/TransformFixUndef.java,v 1.9 2015/12/23 15:45:10 spr Exp $ */


/*********************************************************************************
 *
 * $Log: TransformFixUndef.java,v $
 * Revision 1.9  2015/12/23 15:45:10  spr
 * Minor fixes.
 *
 * Revision 1.8  2015/09/23 17:54:53  spr
 * Version to handle andriod UI applications.
 *
 * Revision 1.7  2014/08/29 15:16:09  spr
 * Updates for suise, testcases.
 *
 * Revision 1.6  2013/09/13 20:33:04  spr
 * Add calls for UI search.
 *
 * Revision 1.5  2013-05-09 12:26:21  spr
 * Minor changes to start ui fixups.
 *
 * Revision 1.4  2012-08-13 16:51:51  spr
 * Add new transforms, clean up code, bug fixes.
 *
 * Revision 1.3  2012-07-20 22:15:20  spr
 * New transforms and resolution for UI search
 *
 * Revision 1.2  2012-06-20 12:21:33  spr
 * Initial fixes for UI search
 *
 * Revision 1.1  2012-06-11 18:18:28  spr
 * Include changed/new files for package/ui search
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.language.java;


import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.ivy.jcomp.JcompTyper;
import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6Solution;
import edu.brown.cs.s6.common.S6SolutionSet;



public class TransformFixUndef extends TransformJava implements S6Constants, JavaConstants {




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public TransformFixUndef(String name)
{
   super(name);
}



/********************************************************************************/
/*										*/
/*	Do the actual undef fix up for a method 				*/
/*										*/
/********************************************************************************/

@Override protected TreeMapper findPackageMapping(S6SolutionSet ss,
      CompilationUnit cu,S6Request.PackageSignature psg,S6Solution sol)
{
   NameFixer nf = new NameFixer(psg.getName(),ss);
   cu.accept(nf);

   if (!nf.hasChanges()) return null;

   UndefFixup uf = new UndefFixup(nf);

   return uf;
}



/********************************************************************************/
/*										*/
/*	Class to find name fixups						*/
/*										*/
/********************************************************************************/

private static class NameFixer extends ASTVisitor {

   private String package_name;
   private S6SolutionSet solution_set;
   private Set<ASTNode> remove_qualifiers;
   private Set<ASTNode> remove_finals;
   private boolean has_enum;

   NameFixer(String pnm,S6SolutionSet ss) {
      package_name = pnm;
      solution_set = ss;
      remove_qualifiers = new HashSet<ASTNode>();
      remove_finals = new HashSet<ASTNode>();
      has_enum = false;
    }

   boolean hasChanges() {
      if (has_enum) return false;
      if (remove_qualifiers.size() > 0) return true;
      if (remove_finals.size() > 0) return true;
      return false;
    }

   boolean removeQualifier(ASTNode n)		{ return remove_qualifiers.contains(n); }
   boolean removeFinal(ASTNode n)		{ return remove_finals.contains(n); }

   public @Override boolean visit(FieldDeclaration n) {
      int mods = n.getModifiers();
      if (!Modifier.isStatic(mods) && Modifier.isFinal(mods)) {
         // we might remove the assignment in the constructor, so remove final
         remove_finals.add(n);
       }
      return true;
    }

   public @Override void endVisit(SimpleName sn) {
      if (sn.getIdentifier().equals("enum")) has_enum = true;
    }

   public @Override boolean visit(QualifiedName qn) {
      JcompTyper typr = JavaAst.getTyper(qn);
      JcompType jt = JavaAst.getExprType(qn);
      if (jt == null) return false;
      if (jt.isErrorType() || jt.isUnknown()) {
         if (solution_set.getSearchType() == S6SearchType.ANDROIDUI) {
            if (qn.getFullyQualifiedName().startsWith("R.")) return false;
          }
         String tnm = qn.getName().getIdentifier();
         String xnm = package_name + "." + tnm;
         JcompType njt = typr.findType(xnm);
         if (njt != null && !njt.isErrorType()) {
            remove_qualifiers.add(qn);
          }
       }
   
      return false;
    }

   public @Override boolean visit(QualifiedType qn) {
      JcompTyper typr = JavaAst.getTyper(qn);
      JcompType jt = JavaAst.getExprType(qn);
      if (jt == null) return false;
      if (jt.isErrorType() || jt.isUnknown()) {
	 String tnm = qn.getName().getIdentifier();
	 JcompType njt = typr.findType(tnm);
	 if (njt != null && !njt.isErrorType() && !njt.isUnknown()) {
	    remove_qualifiers.add(qn);
	  }
       }

      return false;
   }

}	// end of inner class NameFixer



/********************************************************************************/
/*										*/
/*	Tree Transformer for undef fixups					*/
/*										*/
/********************************************************************************/

private class UndefFixup extends TreeMapper {

   private NameFixer name_fixer;

   UndefFixup(NameFixer nf) {
      name_fixer = nf;
    }

   @Override protected String getSpecificsName()   { return "@Undef"; }
   // TODO: this needs to be a separate transformation that is invoked when N
   // when name NameFixer fails
   
   @Override void rewriteTree(ASTNode orig,ASTRewrite rw) {
      if (name_fixer.removeQualifier(orig)) {
         if (orig instanceof QualifiedName) {
            QualifiedName qn = (QualifiedName) orig;
            AST ast = orig.getAST();
            SimpleName sn0 = qn.getName();
            SimpleName sn1 = ast.newSimpleName(sn0.getIdentifier());
            rw.replace(orig,sn1,null);
          }
         else if (orig instanceof QualifiedType) {
            QualifiedType qt = (QualifiedType) orig;
            AST ast = orig.getAST();
            SimpleName sn0 = qt.getName();
            SimpleName sn1 = ast.newSimpleName(sn0.getIdentifier());
            rw.replace(orig,sn1,null);
          }
       }
      else if (name_fixer.removeFinal(orig)) {
         FieldDeclaration fd = (FieldDeclaration) orig;
         ListRewrite lrw = rw.getListRewrite(fd,FieldDeclaration.MODIFIERS2_PROPERTY);
         for (Object o : fd.modifiers()) {
            if (o instanceof Modifier) {
               Modifier md = (Modifier) o;
               if (md.isFinal()) lrw.remove(md,null);
             }
          }
       }
    }

}	// end of inner class UndefFixup




}	// end of class TransformFixUndef




/* end of TransformFixUndef.java */
