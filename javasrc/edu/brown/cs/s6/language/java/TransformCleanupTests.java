/********************************************************************************/
/*										*/
/*		TransformCleanupTests.java					*/
/*										*/
/*	Remove unneeded code							*/
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
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import edu.brown.cs.ivy.jcomp.JcompSymbol;
import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6Solution;
import edu.brown.cs.s6.common.S6SolutionSet;


public class TransformCleanupTests extends TransformJava implements S6Constants, JavaConstants
{


/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public TransformCleanupTests(String name)
{
   super(name);
}



/********************************************************************************/
/*										*/
/*	TransformSetup								*/
/*										*/
/********************************************************************************/

@Override protected TreeMapper findClassMapping(S6SolutionSet sols,TypeDeclaration td,
      S6Request.ClassSignature csg,S6Solution sol)
{
   if (!JavaAst.checkTypeSignature(td,csg,S6SignatureType.FULL,null)) return null;
   S6Request.TestingSignature tsg = (S6Request.TestingSignature) sols.getRequest().getSignature();
   String pkg = tsg.getPackage();

   // or (S6Transform.Memo m : sol.getTransforms()) {
      // if (m.getBaseName().startsWith("RemoveSpecials")) return null;
      // if (m.getBaseName().startsWith("RemoveExcessTests")) return null;
    // }

   UserFinder uf = new UserFinder(pkg);
   int sz = 0;
   for ( ; ; ) {
      td.accept(uf);
      int nsz = uf.getUserMethods().size();
      if (nsz == sz) break;
      sz = nsz;
    }
   if (sz == 0) {
      sol.setFlag(S6SolutionFlag.REMOVE);
      // System.err.print("REMOVE NO USER: " );
      // if (sol.getTransforms() != null) {
	 // int ct = 0;
	 // for (S6Transform.Memo m : sol.getTransforms()) {
	    // if (m.getTransformName().contains("SetupTesting_UtilMDE.fieldDescriptorToBinary")) ++ct;
	    // if (m.getTransformName().contains("ChangeRemoveUndef")) ++ct;
	    // System.err.print(" " + m.getTransformName());
	 //  }
	 // if (ct >= 2) {
	    // System.err.println("CHECK SOLUTION");
	  // }
       // }
      // System.err.println();
      return null;
    }

   MethodFinder mf = new MethodFinder(pkg);
   sz = 0;
   for ( ; ; ) {
      td.accept(mf);
      int nsz = mf.getRelevantMethods().size();
      if (nsz == sz) break;
      sz = nsz;
    }

   if (!mf.callsUserCode()) {
      sol.setFlag(S6SolutionFlag.REMOVE);
      return null;
    }

   Set<JcompSymbol> umthds = uf.getUserMethods();
   Set<JcompSymbol> tmthds = mf.getTestMethods();
   Set<JcompSymbol> use = mf.getRelevantMethods();

   for (Iterator<JcompSymbol> it = tmthds.iterator(); it.hasNext(); ) {
      JcompSymbol js = it.next();
      if (use.contains(js) && umthds.contains(js)) it.remove();
    }
   if (tmthds.isEmpty()) return null;

   RemoveTests rt = new RemoveTests(tmthds);

   return rt;
}



/********************************************************************************/
/*										*/
/*	Visitor to find methods calling user code				*/
/*										*/
/********************************************************************************/

private static class UserFinder extends ASTVisitor {

   private String user_package;
   private Set<JcompSymbol> user_methods;
   private boolean calls_user;
   private Stack<Boolean> calls_stack;

   UserFinder(String pkg) {
      user_package = pkg;
      user_methods = new HashSet<JcompSymbol>();
      calls_user = false;
      calls_stack = new Stack<Boolean>();
    }

   Set<JcompSymbol> getUserMethods()		{ return user_methods; }

   @Override public boolean visit(MethodDeclaration md) {
      calls_stack.push(calls_user);
      calls_user = false;
      JcompSymbol js = JavaAst.getDefinition(md);
      if (js == null) return true;
      if (user_methods.contains(js)) return false;
      return true;
    }

   @Override public void endVisit(MethodDeclaration md) {
      if (calls_user) {
	 JcompSymbol js = JavaAst.getDefinition(md);
	 if (js != null) user_methods.add(js);
       }
      calls_user = calls_stack.pop();
    }

   @Override public void endVisit(QualifiedName qn) {
      String nm = qn.getFullyQualifiedName();
      JcompSymbol js = JavaAst.getReference(qn);
      if (js != null && nm.startsWith(user_package) && js.getDefinitionNode() == null)
	 calls_user = true;
    }

   @Override public void endVisit(MethodInvocation mi) {
      JcompSymbol js = JavaAst.getReference(mi);
      if (js == null) js = JavaAst.getReference(mi.getName());
      if (js == null) return;
      String fn = js.getFullName();

      if (fn.startsWith(user_package) && js.getDefinitionNode() == null)
	 calls_user = true;
      if (user_methods.contains(js))
	 calls_user = true;
    }

}	// end of inner class UserFinder



/********************************************************************************/
/*										*/
/*	Visitor to find relevant methods					*/
/*										*/
/********************************************************************************/

private static class MethodFinder extends ASTVisitor {

   private Set<JcompSymbol> use_methods;
   private Set<JcompSymbol> test_methods;
   private boolean is_relevant;
   private boolean is_test;
   private boolean calls_user;
   private String call_package;
   private Stack<Boolean> relevant_stack;

   MethodFinder(String pkg) {
      use_methods = new HashSet<JcompSymbol>();
      test_methods = new HashSet<JcompSymbol>();
      relevant_stack = new Stack<Boolean>();
      is_relevant = false;
      is_test = false;
      calls_user = false;
      call_package = pkg;
    }

   Set<JcompSymbol> getRelevantMethods() 	{ return use_methods; }
   Set<JcompSymbol> getTestMethods()		{ return test_methods; }
   boolean callsUserCode()			{ return calls_user; }

   @Override public boolean visit(MethodDeclaration md) {
      relevant_stack.push(is_relevant);
      relevant_stack.push(is_test);
      is_relevant = false;
      is_test = false;
      return true;
    }

   @Override public void endVisit(MethodDeclaration md) {
      JcompSymbol js = JavaAst.getDefinition(md);
      if (!is_relevant) is_relevant = checkIfCallback(js);
      if (is_relevant) use_methods.add(js);
      if (is_test) test_methods.add(js);
      is_test = relevant_stack.pop();
      is_relevant = relevant_stack.pop();
    }

   @Override public void endVisit(MethodInvocation mi) {
      JcompSymbol js = JavaAst.getReference(mi);
      if (js == null) js = JavaAst.getReference(mi.getName());
      if (js == null) return;
      if (use_methods.contains(js)) is_relevant = true;
      String fn = js.getFullName();
      if (fn.startsWith("edu.brown.cs.s6.runner.RunnerAssert")) is_relevant = true;
      if (fn.startsWith(call_package)) calls_user = true;
    }

   @Override public void endVisit(AssertStatement st) {
      is_relevant = true;
    }

   @Override public void endVisit(NormalAnnotation an) {
      String nm = an.getTypeName().getFullyQualifiedName();
      String qnm = nm;
      int idx = nm.lastIndexOf(".");
      if (idx > 0) nm = nm.substring(idx+1);
      if (nm.equals("Test")) is_test = true;
      else if (qnm.startsWith("org.junit.")) is_relevant = true;
    }

   @Override public void endVisit(MarkerAnnotation an) {
      String nm = an.getTypeName().getFullyQualifiedName();
      String qnm = nm;
      int idx = nm.lastIndexOf(".");
      if (idx > 0) nm = nm.substring(idx+1);
      if (nm.equals("Test")) is_test = true;
      else if (qnm.startsWith("org.junit.")) is_relevant = true;
    }

   private boolean checkIfCallback(JcompSymbol js) {
      if (!js.isPublic()) return false;
      JcompType jt = js.getType().getBaseType();
      if (jt == null) return false;
      if (jt.isVoidType()) return true;

      return true;
    }
}	// end of inner class MethodFinder




/********************************************************************************/
/*										*/
/*	Actual transformer							*/
/*										*/
/********************************************************************************/

private class RemoveTests extends TreeMapper {

   private Set<JcompSymbol> bad_tests;

   RemoveTests(Set<JcompSymbol> bt) {
      bad_tests = new HashSet<JcompSymbol>(bt);
    }

   

   @Override void rewriteTree(ASTNode orig,ASTRewrite rw) {
      if (orig instanceof MethodDeclaration) {
	 MethodDeclaration md = (MethodDeclaration) orig;
	 JcompSymbol js = JavaAst.getDefinition(md);
	 if (bad_tests.contains(js)) {
	    rw.remove(md,null);
	  }
       }
    }
}	// end of inner class removeTests




}	// end of class TransformCleanupTests




/* end of TransformCleanupTests.java */

