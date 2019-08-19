/********************************************************************************/
/*                                                                              */
/*              JavaClassFragment.java                                          */
/*                                                                              */
/*      Implementation of a Class fragment based on COSE                        */
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

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import edu.brown.cs.cose.cosecommon.CoseResult;
import edu.brown.cs.s6.common.S6Language;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6Solution;
import edu.brown.cs.s6.common.S6SolutionSet;

class JavaClassFragment extends JavaCoseFragment
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private boolean full_class;


/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

JavaClassFragment(S6Language lj,S6Request.Search rqst,CoseResult rslt)
{
   super(lj,rqst,rslt);
   full_class = false;
   if (rqst.getSearchType() == S6SearchType.FULLCLASS) full_class = true;
}



/********************************************************************************/
/*                                                                              */
/*      Checking methods                                                        */
/*                                                                              */
/********************************************************************************/

public boolean checkInitial(S6Request.Signature sg)
{
   AbstractTypeDeclaration td = (AbstractTypeDeclaration) getAstNode();
   if (!Modifier.isStatic(td.getModifiers())) return false;
   if (Modifier.isAbstract(td.getModifiers())) return false;
   return true;
}

public boolean checkSignature(S6Request.Signature rsg,S6SignatureType styp) 
{
   AbstractTypeDeclaration td = (AbstractTypeDeclaration) getAstNode();
   S6Request.ClassSignature cs = rsg.getClassSignature();
   if (cs == null) return false;
   return JavaAst.checkTypeSignature(td,cs,styp,null);
}



/********************************************************************************/
/*                                                                              */
/*      Dependency management                                                   */
/*                                                                              */
/********************************************************************************/

@Override public boolean fixDependencies(S6SolutionSet ss,S6Solution sol) 
{
   AbstractTypeDeclaration astnode = (AbstractTypeDeclaration) getAstNode();
   JavaDepends jd = new JavaDepends(ss,sol,astnode);
   
   S6Request.ClassSignature csg = ss.getRequest().getSignature().getClassSignature();
   for (Iterator<?> it = astnode.bodyDeclarations().iterator(); it.hasNext(); ) {
      BodyDeclaration bd = (BodyDeclaration) it.next();
      if (full_class) {
         if (Modifier.isPublic(bd.getModifiers()) || Modifier.isProtected(bd.getModifiers())) {
            jd.addDeclaration(bd);
          }
       }
      else if (bd instanceof MethodDeclaration) {
         MethodDeclaration md = (MethodDeclaration) bd;
         boolean used = false;
         for (S6Request.MethodSignature msg : csg.getMethods()) {
            if (JavaAst.checkMethodSignature(md,msg,S6SignatureType.FULL)) {
               jd.addDeclaration(md);
               used = true;
               break;
             }
          }
         if (!used && csg.includeTestCases()) {
            if (md.getBody() != null && md.getBody().statements().size() > 0) {
               if (md.getName().getIdentifier().startsWith("test")) {
                  jd.addDeclaration(bd);
                }
               else {
                  for (Object o : md.modifiers()) {
                     IExtendedModifier am = (IExtendedModifier) o;
                     if (am.isAnnotation()) {
                        Annotation aa = (Annotation) am;
                        String fqn = aa.getTypeName().getFullyQualifiedName();
                        if (fqn.startsWith("org.junit.")) {
                           jd.addDeclaration(bd);
                           break;
                         }
                      }
                   }
                }
             }
          }
         
       }
    }
   if (!jd.findDependencies()) return false;
   
   Collection<BodyDeclaration> uses = jd.getDeclarations();
   boolean chng = false;
   int usect = 0;
   ASTRewrite rw = ASTRewrite.create(astnode.getAST());
   ITrackedNodePosition basepos = rw.track(astnode);
   ListRewrite lrw = null;
   
   for (Iterator<?> it = astnode.bodyDeclarations().iterator(); it.hasNext(); ) {
      BodyDeclaration bd = (BodyDeclaration) it.next();
      if (!uses.contains(bd)) {
         if (lrw == null) {
            lrw = rw.getListRewrite(astnode,astnode.getBodyDeclarationsProperty());
          }
         lrw.remove(bd,null);
         // it.remove();
         chng = true;
       }
      else {
         uses.remove(bd);
         ++usect;
       }
    }
   uses.remove(astnode);
   usect += uses.size();
   
   if (chng && usect == 0) {
      // System.err.println("DEPEND REMOVED ALL = false");
      return false;
    }
   
   JavaClassFragment rsltfrag = this;
   
   if (lrw != null) {
      rsltfrag = (JavaClassFragment) cloneFragment(rw,basepos);
      sol.updateFragment(rsltfrag);
    }
   
   for (BodyDeclaration bd : uses) {
      rsltfrag.addHelper(bd);
    }
   
   rsltfrag.use_constructor = false; // set only if we need something outside of the class
   
   rsltfrag.import_set = jd.getImportTypes();
   
   return true;
}



}       // end of class JavaClassFragment




/* end of JavaClassFragment.java */

