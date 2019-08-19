/********************************************************************************/
/*                                                                              */
/*              TransformRemoveOverrides.java                                   */
/*                                                                              */
/*      Remove @Overrides throughout                                            */
/*                                                                              */
/********************************************************************************/
/*      Copyright 2013 Brown University -- Steven P. Reiss                    */
/*********************************************************************************
 *  Copyright 2013, Brown University, Providence, RI.                            *
 *                                                                               *
 *                        All Rights Reserved                                    *
 *                                                                               *
 *  Permission to use, copy, modify, and distribute this software and its        *
 *  documentation for any purpose other than its incorporation into a            *
 *  commercial product is hereby granted without fee, provided that the          *
 *  above copyright notice appear in all copies and that both that               *
 *  copyright notice and this permission notice appear in supporting             *
 *  documentation, and that the name of Brown University not be used in          *
 *  advertising or publicity pertaining to distribution of the software          *
 *  without specific, written prior permission.                                  *
 *                                                                               *
 *  BROWN UNIVERSITY DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS                *
 *  SOFTWARE, INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND            *
 *  FITNESS FOR ANY PARTICULAR PURPOSE.  IN NO EVENT SHALL BROWN UNIVERSITY      *
 *  BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY          *
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,              *
 *  WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS               *
 *  ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE          *
 *  OF THIS SOFTWARE.                                                            *
 *                                                                               *
 ********************************************************************************/



package edu.brown.cs.s6.language.java;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6Solution;
import edu.brown.cs.s6.common.S6SolutionSet;

public class TransformRemoveOverrides extends TransformJava implements S6Constants
{



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

public TransformRemoveOverrides(String name)
{
   super(name);
}



/********************************************************************************/
/*                                                                              */
/*      Top Level Mapping Methods                                               */
/*                                                                              */
/********************************************************************************/

@Override protected TreeMapper findPackageMapping(S6SolutionSet ss,CompilationUnit cu,
      S6Request.PackageSignature sg,S6Solution sol)
{
   if (!JavaAst.checkPackageSignature(cu,sg,S6SignatureType.NAME,sol.getFragment())) return null;
   
   return getOverridesMapping(cu,sol);
}

@Override protected boolean checkApplyClassForPackage(S6SolutionSet sols,CompilationUnit cu,
      S6Request.ClassSignature csg,AbstractTypeDeclaration td)
{
   return false;
}


@Override protected TreeMapper findClassMapping(S6SolutionSet sols,
      TypeDeclaration td,S6Request.ClassSignature csg,S6Solution sol)
{
   return getOverridesMapping(td,sol);
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
   return getOverridesMapping(md,sol);
}



/********************************************************************************/
/*                                                                              */
/*      Check if there should be amapping                                       */
/*                                                                              */
/********************************************************************************/

private TreeMapper getOverridesMapping(ASTNode nd,S6Solution sol)
{
   OverrideFinder ef = new OverrideFinder();
   
   // ASTNode rnd = nd.getRoot();
   // rnd.accept(ef);
   nd.accept(ef);
   Set<ASTNode> fixes = ef.getNames();
   if (fixes == null || fixes.size() == 0) return null;
   
   return new OverrideMapper(nd,fixes);
}



private static class OverrideFinder extends ASTVisitor {

   private Set<ASTNode> return_nodes;
   
   OverrideFinder() {
      return_nodes = new HashSet<ASTNode>();
    }
   
   Set<ASTNode> getNames()			{ return return_nodes; }
   
   public @Override boolean visit(MarkerAnnotation nd) {
      String nm = nd.getTypeName().getFullyQualifiedName();
      if (nm.equals("Override") || nm.equals("java.lang.Override")) {
         return_nodes.add(nd);
       }
      
      return false;
    }
   
}	// end of inner class OverrideFinder



/********************************************************************************/
/*                                                                              */
/*      Mapping to remove overrides                                             */
/*                                                                              */
/********************************************************************************/

private class OverrideMapper extends TreeMapper {
   
   private ASTNode base_node;
   private Set<ASTNode> fix_names;
   
   OverrideMapper(ASTNode n,Set<ASTNode> fixes) {
      base_node = n;
      fix_names = fixes;
    }
   
   
   
   void rewriteTree(ASTNode orig,ASTRewrite rw) {
      if (orig == base_node) {
         for (ASTNode n : fix_names) {
            rw.remove(n,null);
          }
       }
    }

}	// end of inner class OverrideMapper


}       // end of class TransformRemoveOverrides




/* end of TransformRemoveOverrides.java */

