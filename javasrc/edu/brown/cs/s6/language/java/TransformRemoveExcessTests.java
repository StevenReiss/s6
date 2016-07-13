/********************************************************************************/
/*										*/
/*		TransformRemoveExcessTests.java 				*/
/*										*/
/*	Remove test code not relevant to user calls				*/
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.ChildPropertyDescriptor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import edu.brown.cs.ivy.jcomp.JcompSymbol;
import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6Solution;
import edu.brown.cs.s6.common.S6SolutionSet;
public class TransformRemoveExcessTests extends TransformJava implements S6Constants
{


/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private enum State { DELETE, NEUTRAL, KEEP };



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public TransformRemoveExcessTests(String name)
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

   S6Request.TestingSignature tsg = (S6Request.TestingSignature) solset.getRequest().getSignature();
   String pkg = tsg.getPackage();

   FindRelevant fr = new FindRelevant(pkg);
   
   for ( ; ; ) {
      td.accept(fr);
      if (!fr.hasChanged()) break;
    }
   if (fr.getRelevantNames().size() == 0) return null;

   NodeMarker nm = new NodeMarker(fr.getRelevantNames());
   td.accept(nm);
   Set<ASTNode> dels = nm.getDeletedNodes();
   if (dels == null || dels.size() == 0) return null;

   for (Iterator<ASTNode> it = dels.iterator(); it.hasNext(); ) {
      ASTNode n = it.next();
      for (ASTNode p = n.getParent(); p != null; p = p.getParent()) {
	 if (dels.contains(p)) {
	    it.remove();
	    break;
	  }
       }
    }

   return new NodeRemover(dels);
}



/********************************************************************************/
/*										*/
/*	Utility methods 							*/
/*										*/
/********************************************************************************/

private boolean isRelevant(ASTNode n)
{
   if (n instanceof Statement) return true;
   if (n instanceof BodyDeclaration) return true;

   return false;
}




/********************************************************************************/
/*										*/
/*	Visitor to find relevant variables					*/
/*										*/
/********************************************************************************/

private class FindRelevant extends ASTVisitor {

   private String user_package;
   private Set<JcompSymbol> relevant_names;
   private Set<ASTNode> relevant_nodes;
   private boolean relevant_expr;
   private boolean relevant_stmt;
   private boolean relevant_method;
   private int statement_level;
   private boolean has_changed;

   FindRelevant(String pkg) {
      user_package = pkg;
      relevant_names = new HashSet<JcompSymbol>();
      relevant_nodes = new HashSet<ASTNode>();
      has_changed = false;
      relevant_expr = false;
      relevant_stmt = false;
      relevant_method = false;
      statement_level = 0;
    }

   Set<JcompSymbol> getRelevantNames()	{ return relevant_names; }
   boolean hasChanged() {
      boolean fg = has_changed;
      has_changed = false;
      return fg;
    }

   @Override public boolean visit(MethodInvocation mi) {
      // if mi is a call to user function, set relevant_expr
      // if method (ref) is relevant, then set relevant_expr
      JcompSymbol js = JavaAst.getReference(mi);
      if (js == null) js = JavaAst.getReference(mi.getName());
      if (js == null) return true;
      String fn = js.getFullName();
   
      if (fn.startsWith(user_package) && js.getDefinitionNode() == null) {
         relevant_expr = true;
         if (relevant_names.add(js)) has_changed = true;
       }
      
      return true;
    }
   
    @Override public boolean visit(ClassInstanceCreation mi) {
      // if mi is a call to user function, set relevant_expr
      // if method (ref) is relevant, then set relevant_expr
      JcompSymbol js = JavaAst.getReference(mi);
      if (js == null) js = JavaAst.getReference(mi.getType());
      if (js == null) return true;
      String fn = js.getFullName();
     
      if (fn.startsWith(user_package) && js.getDefinitionNode() == null) {
         relevant_expr = true;
         if (relevant_names.add(js)) has_changed = true;
       }
      
      return true;
    }

   @Override public void preVisit(ASTNode n) {
      if (n instanceof Statement) ++statement_level;
      if (relevant_nodes.contains(n)) relevant_expr = true;
      markSymbol(n);
    }

   @Override public void postVisit(ASTNode n) {
      markSymbol(n);
      if (n instanceof Statement) {
         --statement_level;
         relevant_stmt |= relevant_expr;
         relevant_expr = false;
       }
      else if (n instanceof BodyDeclaration) {
         relevant_stmt = false;
         relevant_expr = false;
       }
      else if (n instanceof Expression) {
         if (relevant_expr) {
            if (relevant_nodes.add(n)) has_changed = true;
          }
       }
    }

   @Override public boolean visit(MethodDeclaration md) {
      JcompSymbol js = JavaAst.getDefinition(md);
      if (relevant_names.contains(js)) {
         relevant_method = true;
         for (Object o : md.parameters()) {
            SingleVariableDeclaration svd = (SingleVariableDeclaration) o;
            JcompSymbol par = JavaAst.getDefinition(svd);
            if (par != null) relevant_names.add(par);
          }
       }
      return true;
    }

   @Override public void endVisit(MethodDeclaration md) {
      relevant_method = false;
    }

   @Override public boolean visit(ReturnStatement rs) {
      if (relevant_method) relevant_expr = true;
      return true;
    }
   
   @Override public boolean visit(ThrowStatement ts) {
      if (relevant_method) relevant_expr = true;
      return true;
    }
   
   private void markSymbol(ASTNode n) {
      JcompSymbol js = JavaAst.getDefinition(n);
      if (js == null) js = JavaAst.getReference(n);
      if (js != null && js.getNameNode() != null) {
         if (relevant_names.contains(js)) {
            if (statement_level > 0) relevant_expr = true;
            else relevant_stmt = true;
          }
         else if (relevant_expr) {
            if (relevant_names.add(js)) has_changed = true;
          }
         else if (relevant_stmt && statement_level == 0) {
            if (relevant_names.add(js)) has_changed = true;
          }
       }
    }

}	// end of inner class FindRelevant



/********************************************************************************/
/*										*/
/*	Tree visitor to mark relevant nodes					*/
/*										*/
/********************************************************************************/

private class NodeMarker extends ASTVisitor {

   private Set<JcompSymbol> relevant_syms;
   private Map<ASTNode,State> node_states;
   private Stack<Boolean> val_stack;
   private boolean has_needed;
   private boolean has_unneeded;
   private boolean has_other;

   NodeMarker(Set<JcompSymbol> rqd) {
      relevant_syms = rqd;
      val_stack = new Stack<Boolean>();
      has_needed = false;
      has_unneeded = false;
      has_other = false;
      node_states = new HashMap<ASTNode,State>();
    }

   Set<ASTNode> getDeletedNodes() {
      boolean havekeep = false;
      Set<ASTNode> rslt = new HashSet<ASTNode>();
      for (Map.Entry<ASTNode,State> ent : node_states.entrySet()) {
         if (ent.getValue() == State.DELETE) {
            rslt.add(ent.getKey());
          }
         else havekeep = true;
       }
      if (!havekeep) return null;
      return rslt;
    }

   @Override public void preVisit(ASTNode n) {
      if (isRelevant(n)) {
	 val_stack.push(has_needed);
	 val_stack.push(has_unneeded);
	 val_stack.push(has_other);
	 has_needed = false;
	 has_unneeded = false;
	 has_other = false;
       }

      markSymbol(JavaAst.getDefinition(n));
      markSymbol(JavaAst.getReference(n));
    }

   @Override public void postVisit(ASTNode n) {
      if (isRelevant(n)) {
         if (node_states.get(n) == null) {
            if (has_needed) node_states.put(n,State.KEEP);
            else if (has_unneeded) node_states.put(n,State.DELETE);
            else node_states.put(n,State.NEUTRAL);
          }
         has_other = val_stack.pop();
         has_unneeded = val_stack.pop();
         has_needed |= val_stack.pop();
       }
    }

   @Override public void endVisit(Block b) {
      int need = 0;
      int dele = 0;
      for (Object o : b.statements()) {
         Statement stmt = (Statement) o;
         State s = node_states.get(stmt);
         if (s == State.KEEP) ++need;
         else if (s == State.DELETE) ++dele;
       }
   
      if (dele == 0) need = 1;
      else need = 0;
   
      for (Object o : b.statements()) {
         Statement stmt = (Statement) o;
         State s = node_states.get(stmt);
         if (s == State.KEEP) ++need;
         else if (s == State.NEUTRAL) {
            if (stmt instanceof TypeDeclarationStatement)
               node_states.put(stmt,State.DELETE);
            else if (need > 0) node_states.put(stmt,State.KEEP);
            else node_states.put(stmt,State.DELETE);
          }
       }
   
      if (need == 0) node_states.put(b,State.DELETE);
    }

   @Override public void endVisit(IfStatement ifs) {
      if (ifs.getElseStatement() != null) {
	 State ths = node_states.get(ifs.getThenStatement());
	 State els = node_states.get(ifs.getElseStatement());
	 if (ths != els) {
	    if (ths == State.NEUTRAL) node_states.put(ifs.getThenStatement(),els);
	    if (els == State.NEUTRAL) node_states.put(ifs.getElseStatement(),ths);
	  }
       }
    }

   @Override public void endVisit(MethodDeclaration md) {
      if (md.isConstructor() && Modifier.isPublic(md.getModifiers())) {
         node_states.put(md,State.KEEP);
       }
    }

   private void markSymbol(JcompSymbol js) {
      if (js == null) return;
      if (relevant_syms.contains(js)) has_needed = true;
      else if (js.getDefinitionNode() != null) has_unneeded = true;
      else if (!js.isKnown()) has_unneeded = true;
      else has_other = true;
    }

}	// end of inner class NodeMarker




/********************************************************************************/
/*										*/
/*	Tree transformer to remove unneeded items				*/
/*										*/
/********************************************************************************/

private class NodeRemover extends TreeMapper {

   private Set<ASTNode> deleted_nodes;

   NodeRemover(Set<ASTNode> del) {
      deleted_nodes = del;
    }

   

   @Override void rewriteTree(ASTNode orig,ASTRewrite rw) {
      if (deleted_nodes.contains(orig)) {
	 StructuralPropertyDescriptor spd = orig.getLocationInParent();
	 if (spd.isChildListProperty()) {
	    rw.remove(orig,null);
	  }
	 else if (spd.isChildProperty()) {
	    ChildPropertyDescriptor cpd = (ChildPropertyDescriptor) spd;
	    if (!cpd.isMandatory()) rw.remove(orig,null);
	    else {
	       if (orig instanceof Statement) {
		  AST ast = rw.getAST();
		  EmptyStatement es = ast.newEmptyStatement();
		  rw.replace(orig,es,null);
		}
	     }
	  }
       }
    }

}	// end of inner class NodeRemover


}	// end of class TransformRemoveExcessTests




/* end of TransformRemoveExcessTests.java */

