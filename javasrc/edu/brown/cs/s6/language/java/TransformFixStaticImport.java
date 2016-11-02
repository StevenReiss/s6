/********************************************************************************/
/*										*/
/*		TransformFixStaticImport.java					*/
/*										*/
/*	Replace static import items with full names				*/
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

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import edu.brown.cs.ivy.jcomp.JcompSymbol;
import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6Solution;
import edu.brown.cs.s6.common.S6SolutionSet;

public class TransformFixStaticImport extends TransformJava implements S6Constants, JavaConstants
{



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public TransformFixStaticImport(String name)
{
   super(name);
}



/********************************************************************************/
/*										*/
/*	Top level mapping methods						*/
/*										*/
/********************************************************************************/

@Override protected TreeMapper findPackageMapping(S6SolutionSet ss,CompilationUnit cu,
      S6Request.PackageSignature sg,S6Solution sol)
{
   if (!JavaAst.checkPackageSignature(cu,sg,S6SignatureType.NAME,sol.getFragment())) return null;

   return getStaticMapping(cu,sol);
}


@Override protected boolean checkApplyClassForPackage(S6SolutionSet sols,CompilationUnit cu,
      S6Request.ClassSignature csg,AbstractTypeDeclaration td)
{
   return false;
}


@Override protected TreeMapper findClassMapping(S6SolutionSet sols,
      TypeDeclaration td,S6Request.ClassSignature csg,S6Solution sol)
{
   return getStaticMapping(td,sol);
}



protected boolean checkApplyMethodForClass(S6SolutionSet ss,S6Solution sol,
      TypeDeclaration td,S6Request.MethodSignature msg,MethodDeclaration md)
{
   return false;
}



@Override protected TreeMapper findMethodMapping(S6SolutionSet ss,MethodDeclaration md,
      S6Request.MethodSignature ms,
      S6Solution sol)
{
   return getStaticMapping(md,sol);
}



/********************************************************************************/
/*										*/
/*	Get set of statics to qualify						*/
/*										*/
/********************************************************************************/

private TreeMapper getStaticMapping(ASTNode nd,S6Solution sol)
{
   StaticFinder ef = new StaticFinder(nd);
   nd.accept(ef);
   Set<ASTNode> fixes = ef.getNames();
   if (fixes == null || fixes.size() == 0) return null;

   return new StaticNameMapper(fixes);
}




private static class StaticFinder extends ASTVisitor {

   private Set<ASTNode> return_nodes;
   private JcompType outer_type;
   private String package_name;
   private Set<String> imported_names;

   StaticFinder(ASTNode base) {
      return_nodes = new HashSet<ASTNode>();
      outer_type = null;
      if (base instanceof CompilationUnit) {
	 CompilationUnit cu = (CompilationUnit) base;
	 for (Object o : cu.types()) {
	    base = (ASTNode) o;
	    break;
	  }
       }
      TypeDeclaration td = null;
      for (ASTNode an = base; an != null; an = an.getParent()) {
	 if (an instanceof TypeDeclaration) td = (TypeDeclaration) an;
       }
      if (td != null) outer_type = JavaAst.getJavaType(td);
      
      package_name = null;
      imported_names = new HashSet<String>();
      for (ASTNode an = base; an != null; an = an.getParent()) {
         if (an instanceof CompilationUnit) {
            CompilationUnit cu = (CompilationUnit) an;
            PackageDeclaration pd = cu.getPackage();
            if (pd != null) {
               package_name = pd.getName().getFullyQualifiedName();
             }
            for (Object o : cu.imports()) {
               ImportDeclaration id = (ImportDeclaration) o;
               if (!id.isOnDemand() && !id.isStatic()) {
                  String nm = id.getName().getFullyQualifiedName();
                  imported_names.add(nm);
                }
             }
            break;
          }
       }
    }

   Set<ASTNode> getNames()			{ return return_nodes; }

   public @Override boolean visit(TypeDeclaration td) {
      if (td.getParent().getNodeType() == ASTNode.COMPILATION_UNIT) {
	 outer_type = JavaAst.getJavaType(td);
       }
      return true;
    }
   public @Override boolean visit(QualifiedName nd) {
      return false;
    }

   public @Override boolean visit(SimpleName nd) {
      JcompSymbol js = JavaAst.getReference(nd);
      if (js == null) return false;
      JcompType jt = js.getClassType();
      if (jt == null && js.isTypeSymbol()) jt = js.getType();
      if (jt == null) return false;
      if (outer_type != null) {
         if (jt == outer_type) return false;
         if (jt.getName().startsWith(outer_type.getName())) return false;
       }
      if (nd.getParent() != null &&
            nd.getParent() instanceof MethodInvocation) {
         MethodInvocation mi = (MethodInvocation) nd.getParent();
         if (mi.getExpression() != null) return false;
       }
      if (!js.isStatic()) {
         if (!js.isTypeSymbol()) return false;
         return false;
       }
      if (jt.getName().startsWith("java.lang.")) return false;
      if (package_name != null && jt.getName().startsWith(package_name)) return false;
      if (imported_names.contains(jt.getName())) return false;
   
      // System.err.println("STATIC IMPORT: Add " + nd + " " + nd.getParent().getParent().getParent());
      return_nodes.add(nd);
      return false;
    }

}	// end of inner class StaticFinder



/********************************************************************************/
/*										*/
/*	Actual mapping for static import names					*/
/*										*/
/********************************************************************************/

private class StaticNameMapper extends TreeMapper {

   private Set<ASTNode> fix_names;

   StaticNameMapper(Set<ASTNode> fixes) {
      fix_names = fixes;
    }



   void rewriteTree(ASTNode orig,ASTRewrite rw) {
      if (fix_names.contains(orig)) {
         JcompSymbol js = JavaAst.getReference(orig);
         if (js == null) return;
         if (orig.getParent() instanceof MethodInvocation) {
            MethodInvocation mi = (MethodInvocation) orig.getParent();
            if (mi.getName() == orig) {
               String nm1 = js.getClassType().getName();
               Name xnm = JavaAst.getQualifiedName(rw.getAST(),nm1);
               rw.set(mi,MethodInvocation.EXPRESSION_PROPERTY,xnm,null);
               return;
             }
          }
         else {
            String nm = js.getFullName();
            Name xnm = JavaAst.getQualifiedName(rw.getAST(),nm);
            rw.replace(orig,xnm,null);
          }
       }
    }

}	// end of inner class StaticNameMapper




}	// end of class TransformFixStaticImport




/* end of TransformFixStaticImport.java */































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































