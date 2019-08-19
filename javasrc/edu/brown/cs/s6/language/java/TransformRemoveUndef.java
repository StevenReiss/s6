/********************************************************************************/
/*										*/
/*		TransformRemoveUndef.java					*/
/*										*/
/*	Transformation to remove statements with undefined symbols		*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/language/java/TransformRemoveUndef.java,v 1.22 2016/07/22 13:31:07 spr Exp $ */


/*********************************************************************************
 *
 * $Log: TransformRemoveUndef.java,v $
 * Revision 1.22  2016/07/22 13:31:07  spr
 * Fixups for framework search.
 *
 * Revision 1.21  2016/07/18 23:05:27  spr
 * Update transforms for applications and UI.
 *
 * Revision 1.20  2015/12/23 15:45:10  spr
 * Minor fixes.
 *
 * Revision 1.19  2015/09/23 17:54:54  spr
 * Version to handle andriod UI applications.
 *
 * Revision 1.18  2015/02/14 19:40:19  spr
 * Add test case generation.
 *
 * Revision 1.17  2014/08/29 15:16:10  spr
 * Updates for suise, testcases.
 *
 * Revision 1.16  2014/03/17 18:23:54  spr
 * Minor fixups for transforms.
 *
 * Revision 1.15  2014/02/26 14:06:46  spr
 * Add transformations for user interfaces.
 *
 * Revision 1.14  2013/09/13 20:33:05  spr
 * Add calls for UI search.
 *
 * Revision 1.13  2013-05-09 12:26:21  spr
 * Minor changes to start ui fixups.
 *
 * Revision 1.12  2012-08-13 16:51:51  spr
 * Add new transforms, clean up code, bug fixes.
 *
 * Revision 1.11  2012-07-20 22:15:21  spr
 * New transforms and resolution for UI search
 *
 * Revision 1.10  2012-06-20 12:21:33  spr
 * Initial fixes for UI search
 *
 * Revision 1.9  2012-06-11 14:07:49  spr
 * add framework search; fix bugs
 *
 * Revision 1.8  2009-05-12 22:28:48  spr
 * Fix ups to make user context work.
 *
 * Revision 1.7  2008-11-12 13:52:14  spr
 * Performance and bug updates.
 *
 * Revision 1.6  2008-08-28 00:32:56  spr
 * Next version of S6.	Lots of bug fixes, some new functionality.
 *
 * Revision 1.5  2008-07-18 22:27:09  spr
 * Handle remove compilation calls; update transforms to include code to use ASTrewrite.
 *
 * Revision 1.4  2008-07-17 13:46:46  spr
 * Bug fixes and speed ups.
 *
 * Revision 1.3  2008-06-27 15:45:41  spr
 * Minor bug fixes to transforms.
 *
 * Revision 1.2  2008-06-12 18:15:18  spr
 * Add check for null type.
 *
 * Revision 1.1  2008-06-12 17:47:52  spr
 * Next version of S6.
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.language.java;


import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import edu.brown.cs.ivy.jcomp.JcompSymbol;
import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.ivy.jcomp.JcompTyper;
import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6Solution;
import edu.brown.cs.s6.common.S6SolutionSet;



public class TransformRemoveUndef extends TransformJava implements S6Constants, JavaConstants {




/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private static int undef_counter = 0;
private static boolean do_debug = false;


/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public TransformRemoveUndef(String name)
{
   super(name);
}




/********************************************************************************/
/*										*/
/*	Package transform -- remove everything that is undefined		*/
/*										*/
/********************************************************************************/

@Override protected Collection<TreeMapper> findPackageMappings(S6SolutionSet ss,
      S6Solution sol,CompilationUnit cu,S6Request.PackageSignature sg)
{
   if (!JavaAst.checkPackageSignature(cu,sg,S6SignatureType.NO_METHODS,sol.getFragment()))
      return null;

   Set<ASTNode> undefs = null;
   boolean android = ss.getSearchType() == S6SearchType.ANDROIDUI;
   UndefFinder ufn = new UndefFinder(true,android);
   for ( ; ; ) {
      cu.accept(ufn);
      undefs = ufn.getUndefined();
      if (!ufn.hasChanged()) break;
    }

   if (undefs == null || undefs.size() == 0) {
      sol.setFlag(S6SolutionFlag.COMPILES);
      switch (ss.getSearchType()) {
	 case UIFRAMEWORK :
	 case ANDROIDUI :
	    return cleanupClasses(ss,sol,cu,sg);
	 default :
	    break;
       }
      return null;
    }

   TreeMapper tm = new UndefMapper("package" + (++undef_counter),undefs,ufn.getCheckSyms());

   return Collections.singletonList(tm);
}




/********************************************************************************/
/*										*/
/*	Cleanup unneeeded classes after all undefs are removed			*/
/*										*/
/********************************************************************************/

private Collection<TreeMapper> cleanupClasses(S6SolutionSet ss,
      S6Solution sol,CompilationUnit cu,S6Request.PackageSignature psg)
{
   DependenceVisitor dv = new DependenceVisitor();
   boolean chng = true;
   Set<JcompType> done = new HashSet<JcompType>();
   while (chng) {
      chng = false;
      for (Object o : cu.types()) {
	 AbstractTypeDeclaration atd = (AbstractTypeDeclaration) o;
	 JcompSymbol js = JavaAst.getDefinition(atd);
	 if (js == null) continue;
	 String nm = js.getName();
	 JcompType jt = JavaAst.getJavaType(atd);
	 if (jt == null || done.contains(jt)) continue;
	 boolean rqd = false;
	 for (S6Request.ClassSignature csg : psg.getClasses()) {
	    if (csg.getName().equals(nm)) rqd = true;
	  }
	 if (atd instanceof EnumDeclaration) rqd = true;
	 if (ss.getSearchType() == S6SearchType.ANDROIDUI) {
	    JcompTyper typer = JavaAst.getTyper(atd);
	    JcompType pt = typer.findSystemType("android.app.Activity");
	    if (pt == null) rqd = true;
	    else if (jt.isCompatibleWith(pt)) rqd = true;
	  }
	 if (rqd) dv.getTypes().add(jt);
	 if (dv.getTypes().contains(jt)) {
	    done.add(jt);
	    atd.accept(dv);
	    chng = true;
	  }
       }
    }

   Set<ASTNode> remove = new HashSet<ASTNode>();
   for (Object o : cu.types()) {
      AbstractTypeDeclaration atd = (AbstractTypeDeclaration) o;
      JcompType jt = JavaAst.getJavaType(atd);
      if (jt != null && !dv.getTypes().contains(jt)) remove.add(atd);
    }

   if (remove.size() == 0) return null;

   TreeMapper tm = new UndefMapper("package_CLEANUP",remove,null);
   return Collections.singletonList(tm);
}



private class DependenceVisitor extends ASTVisitor {

   Set<JcompType> type_defs;
   private Stack<Boolean> collect_stack;

   DependenceVisitor() {
      type_defs = new HashSet<JcompType>();
      collect_stack = new Stack<Boolean>();
      collect_stack.push(Boolean.FALSE);
    }

   Collection<JcompType> getTypes()			{ return type_defs; }

   @Override public boolean visit(ClassInstanceCreation n) {
      collect_stack.push(Boolean.TRUE);
      return true;
    }

   @Override public void endVisit(ClassInstanceCreation n) {
      collect_stack.pop();
    }

   @Override public boolean visit(FieldAccess n) {
      Expression exp = n.getExpression();
      boolean fg = false;
      if (exp.getNodeType() == ASTNode.TYPE_LITERAL) fg = true;
      collect_stack.push(fg);
      return true;
    }

   @Override public void endVisit(FieldAccess n) {
      collect_stack.pop();
    }

   @Override public boolean visit(QualifiedName n) {
      Name nm = n.getQualifier();
      JcompType jt = JavaAst.getJavaType(nm);
      boolean fg = (jt != null);
      collect_stack.push(fg);
      return true;
    }
   @Override public void endVisit(QualifiedName n) {
      collect_stack.pop();
    }

   @Override public void endVisit(TypeDeclaration n) {
      collect_stack.push(true);
      Type t = n.getSuperclassType();
      if (t != null) t.accept(this);
      for (Object o : n.superInterfaceTypes()) {
	 ASTNode x = (ASTNode) o;
	 x.accept(this);
       }
      collect_stack.pop();
    }

   @Override public boolean visit(VariableDeclarationStatement st) {
      collect_stack.push(true);
      Type t = st.getType();
      if (t != null) t.accept(this);
      collect_stack.pop();
      return super.visit(st);
    }

   @Override public boolean visit(FieldDeclaration n) {
      collect_stack.push(true);
      Type t = n.getType();
      if (t != null) t.accept(this);
      collect_stack.pop();
      return super.visit(n);
    }

   @Override public boolean visit(SingleVariableDeclaration n) {
      collect_stack.push(true);
      Type t = n.getType();
      if (t != null) t.accept(this);
      collect_stack.pop();
      return super.visit(n);
    }

   @Override public boolean visit(CastExpression n) {
      collect_stack.push(true);
      Type t = n.getType();
      if (t != null) t.accept(this);
      collect_stack.pop();
      return super.visit(n);
    }

   @Override public void endVisit(SimpleType n) {
      if (collect_stack.peek()) {
	 JcompType jt = JavaAst.getJavaType(n);
	 if (jt != null && !jt.isBinaryType() && jt.isClassType() && !jt.isArrayType()) {
	    type_defs.add(jt);
	  }
       }
    }

   @Override public void endVisit(SimpleName n) {
      if (collect_stack.peek()) {
	 JcompType jt = JavaAst.getJavaType(n);
	 if (jt != null && !jt.isBinaryType() && jt.isClassType() && !jt.isArrayType()) {
	    type_defs.add(jt);
	  }
       }
    }

   @Override public boolean visit(MethodInvocation n) {
      JcompSymbol js = JavaAst.getReference(n.getName());
      if (js != null && js.isStatic() && n.getExpression() != null) {
	 collect_stack.push(true);
	 n.getExpression().accept(this);
	 collect_stack.pop();
       }
      return super.visit(n);
    }

}	// end of inner class DependenceVisitor




/********************************************************************************/
/*										*/
/*	Method to create mapper to do actual return type changes		*/
/*										*/
/********************************************************************************/

@Override protected TreeMapper findClassMapping(S6SolutionSet ss,TypeDeclaration td,
      S6Request.ClassSignature csg,
      S6Solution sol)
{
   if (!JavaAst.checkTypeSignature(td,csg,S6SignatureType.FULL,null)) return null;

   UndefFinder ufn = new UndefFinder(false,false);
   td.accept(ufn);

   Set<ASTNode> undefs = ufn.getUndefined();
   if (undefs == null || undefs.size() == 0) {
      sol.setFlag(S6SolutionFlag.COMPILES);
      // System.err.println("SOLUTION COMPILES:" + sol);
      return null;
    }

   return new UndefMapper(csg.getName(),undefs,null);
}




@Override protected TreeMapper findMethodMapping(S6SolutionSet ss,MethodDeclaration md,
						    S6Request.MethodSignature ms,
						    S6Solution sol)
{
   if (!JavaAst.checkMethodSignature(md,ms,S6SignatureType.FULL)) return null;

   if (md.getBody() == null) return null;

   UndefFinder ufn = new UndefFinder(false,false);
   md.accept(ufn);

   Set<ASTNode> undefs = ufn.getUndefined();
   if (undefs == null || undefs.size() == 0) {
      sol.setFlag(S6SolutionFlag.COMPILES);
      return null;
    }
   undefs.remove(md);
   if (undefs.size() == md.getBody().statements().size()) return null;

   return new UndefMapper(ms.getName(),undefs,null);
}







/********************************************************************************/
/*										*/
/*	Class to find package elements that contain undefined symbols		*/
/*										*/
/********************************************************************************/

private static Set<StructuralPropertyDescriptor> delete_froms;
static {
   delete_froms = new HashSet<StructuralPropertyDescriptor>();
   delete_froms.add(Block.STATEMENTS_PROPERTY);
   delete_froms.add(MethodDeclaration.THROWN_EXCEPTION_TYPES_PROPERTY);
   delete_froms.add(MethodDeclaration.MODIFIERS2_PROPERTY);
   delete_froms.add(FieldDeclaration.MODIFIERS2_PROPERTY);
   delete_froms.add(FieldDeclaration.FRAGMENTS_PROPERTY);
   delete_froms.add(TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
   delete_froms.add(TypeDeclaration.SUPER_INTERFACE_TYPES_PROPERTY);
   delete_froms.add(TypeDeclaration.SUPERCLASS_TYPE_PROPERTY);
   delete_froms.add(TypeDeclaration.MODIFIERS2_PROPERTY);
   delete_froms.add(CompilationUnit.IMPORTS_PROPERTY);
   delete_froms.add(CompilationUnit.TYPES_PROPERTY);
}


private static class UndefFinder extends ASTVisitor {

   private Set<ASTNode> undef_items;
   private boolean has_undef;
   private boolean return_undef;
   private boolean ignore_undef;
   private boolean force_undef;
   private Set<JcompSymbol> undef_syms;
   private Set<JcompType> undef_types;
   private Set<JcompSymbol> check_syms;
   private Stack<Boolean> undef_stack;
   private boolean do_package;
   private boolean do_android;
   private boolean has_changed;

   UndefFinder(boolean pkg,boolean android) {
      undef_items = new HashSet<ASTNode>();
      undef_stack = new Stack<Boolean>();
      has_undef = false;
      return_undef = false;
      ignore_undef = false;
      force_undef = false;
      undef_syms = new HashSet<JcompSymbol>();
      undef_types = new HashSet<JcompType>();
      check_syms = new HashSet<JcompSymbol>();
      do_package = pkg;
      do_android = android;
      has_changed = false;
    }

   Set<ASTNode> getUndefined()			{ return undef_items; }
   Set<JcompSymbol> getCheckSyms()		{ return check_syms; }

   boolean hasChanged() {
      boolean fg = has_changed;
      has_changed = false;
      return fg;
    }

   @Override public void preVisit(ASTNode n) {
      undef_stack.push(has_undef);
      undef_stack.push(return_undef);
      undef_stack.push(force_undef);
      has_undef = false;
      return_undef = false;
      force_undef = false;
      if (!(n instanceof Expression)) {
	 ignore_undef = false;
	 force_undef = false;
       }
    }

   @Override public void postVisit(ASTNode node) {
      JcompType jt = JavaAst.getExprType(node);
      if (jt != null && jt.isErrorType() && !has_undef) {
	 has_undef = true;
       }
      if (jt != null && undef_types.contains(jt) && !has_undef) {
	 has_undef = true;
       }

      JcompSymbol jd = JavaAst.getDefinition(node);
      JcompSymbol js = JavaAst.getReference(node);
      if (jd == null && js != null && undef_syms.contains(js)) {
	 if (do_debug) System.err.println("UNDEF NAME " + js.getName());
	 // has_undef = true;
	 force_undef = true;
       }
      if (node instanceof Expression) {
	 if (ignore_undef)
	    has_undef = false;
       }
      else {
	 has_undef |= force_undef;
	 force_undef = false;
	 ignore_undef = false;
       }
      if (force_undef) {
	 if (do_debug) System.err.println("SET UNDEF FORCE");
	 has_undef = true;
       }
      force_undef |= has_undef;

      if (do_debug) System.err.println("UDF: " + has_undef + " " + ignore_undef + " " + force_undef +
	    " " + node);

      if (has_undef) {
	 if (jd != null) {
	    if (undef_syms.add(jd)) {
	       if (do_debug) System.err.println("MAKE UNDEF " + jd + " " + jd.hashCode());
	       has_changed = true;
	     }
	    if (do_package && jd.isTypeSymbol()) {
	       if (undef_types.add(jd.getType())) {
		  has_changed = true;
		  if (do_debug) System.err.println("MAKE TUNDEF " + jd + " " + jd.hashCode());
		}
	     }
	  }
	 if (do_debug) System.err.println("CHECK REMOVE " + jd + " " + return_undef + " :: " + node);

	 if (!return_undef && JavaAst.checkHasReturn(node))
	    return_undef = true;

	 StructuralPropertyDescriptor spd = node.getLocationInParent();
	 if (delete_froms.contains(spd)) {
	    if (undef_items.add(node)) {
	       if (do_debug) System.err.println("REMOVE " + node);
	       has_changed = true;
	     }
	    ASTNode par = node.getParent();
	    switch (par.getNodeType()) {
	       case ASTNode.TYPE_DECLARATION :
	       case ASTNode.METHOD_DECLARATION :
		  return_undef = false;
		  has_undef = false;
		  force_undef = false;
		  break;
	       default :
		  has_undef = return_undef;
		  force_undef = false;
		  break;
	     }
	  }
       }

      force_undef |= undef_stack.pop();
      return_undef |= undef_stack.pop();
      has_undef |= undef_stack.pop();
    }

   @Override public boolean visit(TryStatement n) {
      safeVisit(n.getBody());
      safeVisit(n.catchClauses());
      safeVisit(n.getFinally());
      return false;
    }

   @Override public boolean visit(CatchClause n) {
      safeVisit(n.getException());
      safeVisit(n.getBody());
      return false;
    }

   @Override public void endVisit(TryStatement ts) {
      // remove empty try statements
      if (isEmptyTryStatement(ts.getBody())) has_undef = true;
    }

   @Override public boolean visit(MethodDeclaration md) {
      safeVisit(md.modifiers());
      safeVisit(md.typeParameters());
      safeVisit(md.getReturnType2());
      safeVisit(md.getName());
      safeVisit(md.parameters());
      safeVisit(md.thrownExceptionTypes());
      boolean fg = has_undef;
      safeVisit(md.getBody());
      fg = (!fg && has_undef);

      if (do_package) {
	 // remove empty or unused private methods
	 if (Modifier.isPrivate(md.getModifiers())) {
	    if (isEmptyStatement(md.getBody())) has_undef = true;
	    else {
	       JcompSymbol js = JavaAst.getDefinition(md);
	       if (js != null && !js.isUsed()) {
		  has_undef = true;
		}
	     }
	  }
	 else if (fg && Modifier.isPublic(md.getModifiers())) {
	    JcompSymbol js = JavaAst.getDefinition(md);
	    if (js != null) check_syms.add(js);
	    has_undef = false;
	    force_undef = false;
	  }
       }

      return false;
    }

   @Override public boolean visit(Initializer n) {
      safeVisit(n.getBody());
      if (isEmptyStatement(n.getBody())) {
	 has_undef = true;
	 if (do_debug) System.err.println("SET UNDEF INITER EMPTY: " + n);
       }
      return false;
    }

   @Override public void endVisit(Block b) {
      // remove empty blocks in statement lists
      if (isEmptyStatement(b)) {
	 ASTNode par = b.getParent();
	 if (par.getNodeType() == ASTNode.BLOCK) has_undef = true;
       }
    }

   @Override public boolean visit(IfStatement n) {
      safeVisit(n.getExpression());
      boolean rtn = return_undef;
      safeVisit(n.getThenStatement());
      safeVisit(n.getElseStatement());
      return_undef = rtn;
      return false;
    }

   @Override public boolean visit(AnonymousClassDeclaration n) {
      boolean rtn = return_undef;
      safeVisit(n.bodyDeclarations());
      return_undef = rtn;
      return false;
    }

   @Override public void endVisit(IfStatement n) {
      // remove empty else clause
      // remove if statement if not else or then
      boolean thenempty = isEmptyStatement(n.getThenStatement());
      Statement elsestmt = n.getElseStatement();
      boolean elseempty = isEmptyStatement(elsestmt);
      if (thenempty && elseempty) has_undef = true;
      else if (elseempty && !has_undef && elsestmt != null) {
	 undef_items.add(elsestmt);
       }
    }

   @Override public void endVisit(EnhancedForStatement n) {
      // remove empty for statements
      if (isEmptyStatement(n.getBody())) has_undef = true;
    }

   @Override public void endVisit(ForStatement n) {
      // remove empty for statements
      if (isEmptyStatement(n.getBody())) {
	 boolean ok = false;
	 List<?> inits = n.initializers();
	 if (inits.size() == 0) ok = true;
	 if (inits.size() == 1) {
	    ASTNode sn = (ASTNode) inits.get(0);
	    if (sn.getNodeType() == ASTNode.VARIABLE_DECLARATION_EXPRESSION) ok = true;
	  }
	 List<?> upds = n.updaters();
	 if (upds.size() != 1) ok = false;
	 if (ok) has_undef = true;
       }
    }

   @Override public void endVisit(WhileStatement n) {
      // remove empty while statements (might want to take condition into account)
      if (isEmptyStatement(n.getBody())) {
	 has_undef = true;
       }
    }

   @Override public boolean visit(TypeDeclaration td) {
      safeVisit(td.modifiers());
      safeVisit(td.typeParameters());
      safeVisit(td.getName());
      safeVisit(td.getSuperclassType());
      safeVisit(td.superInterfaceTypes());
      safeVisit(td.bodyDeclarations());

      JcompType jt = JavaAst.getJavaType(td);
      if (jt == null) return false;
      if (jt.isAbstract() || jt.isInterfaceType()) return false;
      if (td.getParent() instanceof CompilationUnit) return false;
      JcompTyper typer = JavaAst.getTyper(td);
      if (jt.definesAllNeededMethods(typer)) return false;
      if (do_debug) System.err.println("SET UNDEF EMPTY INNER TYPE " + td);
      has_undef = true;

      return false;
    }

   @Override public void endVisit(VariableDeclarationFragment vdf) {
      // remove unused static or private fields
      JcompSymbol js = JavaAst.getDefinition(vdf);
      if (js == null) return;
      if (js.getType().isUndefined())
	 has_undef = true;
      if (!js.isFieldSymbol()) return;
      if (!js.isUsed() && !js.isBinarySymbol()) {
	 if (js.isStatic() || js.isPrivate()) {
	    has_undef = true;
	    if (do_debug) System.err.println("SET UNDEF UNUSED " + js);
	  }
       }
      else if (!js.isRead() && !js.isBinarySymbol()) {
	 Expression exp = vdf.getInitializer();
	 if (exp != null) {
	    if (exp.getNodeType() == ASTNode.METHOD_INVOCATION) return;
	    if (exp.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION) return;
	  }
	 if (do_android) return;
	 if (js.isStatic() || js.isPrivate()) {
	    if (js.getName().equals("serialVersionUID")) return;
	    // System.err.println("FIELD " + js + " NOT READ");
	    has_undef = true;
	    if (do_debug) System.err.println("SET UNDEF UNREAD " + js);
	  }
       }
    }

   @Override public void endVisit(FieldDeclaration fd) {
      int ctr = 0;
      for (Object o : fd.fragments()) {
	 VariableDeclarationFragment vdf = (VariableDeclarationFragment) o;
	 if (!undef_items.contains(vdf)) ++ctr;
       }
      if (ctr == 0) {
	 has_undef = true;
	 if (do_debug) System.err.println("SET UNDEF EMPTY DECL: " + fd);
       }
    }

   @Override public void endVisit(VariableDeclarationStatement vd) {
      int ctr = 0;
      for (Object o : vd.fragments()) {
	 VariableDeclarationFragment vdf = (VariableDeclarationFragment) o;
	 if (!undef_items.contains(vdf)) ++ctr;
       }
      if (ctr == 0) {
	 has_undef = true;
	 if (do_debug) System.err.println("SET UNDEF EMPTY VDS: " + vd);
       }
    }

   @Override public void endVisit(SimpleType n) {
      JcompType jt = JavaAst.getJavaType(n);
      if (jt != null && jt.isUndefined()) {
	 has_undef = true;
       }
    }

   @Override public void endVisit(Assignment n) {
      if (has_undef) {
	 Expression ex = n.getLeftHandSide();
	 JcompSymbol js = JavaAst.getReference(ex);
	 if (js != null && !js.isFieldSymbol()) check_syms.add(js);
       }
    }

   @Override public void endVisit(SimpleName n) {
      if (do_android && n.getIdentifier().equals("R")) {
	 ignore_undef = true;
       }
    }

   @Override public void endVisit(SingleMemberAnnotation n) {
      JcompType jt = JavaAst.getJavaType(n.getTypeName());
      if (jt == null || jt.isUndefined()) {
	 has_undef = true;
	 force_undef = true;
       }
      return;
    }



   @Override public void endVisit(NormalAnnotation n) {
      JcompType jt = JavaAst.getJavaType(n.getTypeName());
      if (jt == null || jt.isUndefined()) {
	 has_undef = true;
	 force_undef = true;
       }
      return;
    }

   @Override public void endVisit(MarkerAnnotation n) {
      JcompType jt = JavaAst.getJavaType(n.getTypeName());
      if (jt == null || jt.isUndefined()) {
	 has_undef = true;
       }
      return;
    }


   private void safeVisit(ASTNode n) {
      if (n == null) return;
      boolean udf = has_undef;
      boolean rtn = return_undef;
      boolean fudf = force_undef;
      has_undef = false;
      force_undef = false;
      return_undef = false;
      n.accept(this);
      has_undef |= udf;
      return_undef |= rtn;
      force_undef |= fudf;
    }

   private void safeVisit(List<?> nl) {
      if (nl == null) return;
      for (Object o : nl) {
	 ASTNode n = (ASTNode) o;
	 safeVisit(n);
       }
    }

   private boolean isEmptyStatement(ASTNode st) {
      if (st == null) return true;
      switch (st.getNodeType()) {
	 case ASTNode.EMPTY_STATEMENT :
	    return true;
	 case ASTNode.BLOCK :
	    Block thb = (Block) st;
	    int ctr = 0;
	    for (Object o : thb.statements()) {
	       ASTNode n = (ASTNode) o;
	       if (!undef_items.contains(n)) {
		  if (ctr > 0) ++ctr;
		  else if (!doesNothing(n)) ++ctr;
		}
	     }
	    if (ctr == 0) return true;
	    break;
       }
      return false;
    }


   private boolean isEmptyTryStatement(ASTNode st) {
      if (st == null) return true;
      switch (st.getNodeType()) {
	 case ASTNode.EMPTY_STATEMENT :
	    return true;
	 case ASTNode.BLOCK :
	    Block thb = (Block) st;
	    int ctr = 0;
	    for (Object o : thb.statements()) {
	       ASTNode n = (ASTNode) o;
	       if (!undef_items.contains(n)) {
		  if (ctr > 0) ++ctr;
		  else if (n instanceof ThrowStatement) ;
		  else if (!doesNothing(n)) ++ctr;
		}
	     }
	    if (ctr == 0) return true;
	    break;
       }
      return false;
   }


   private boolean doesNothing(ASTNode n)
   {
      switch (n.getNodeType()) {
	 case ASTNode.EXPRESSION_STATEMENT :
	    return doesNothing(((ExpressionStatement) n).getExpression());
	 case ASTNode.VARIABLE_DECLARATION_STATEMENT :
	    VariableDeclarationStatement vds = (VariableDeclarationStatement) n;
	    for (Object o : vds.fragments()) {
	       VariableDeclarationFragment vdf = (VariableDeclarationFragment) o;
	       if (vdf.getInitializer() != null) return false;
	     }
	    return true;
	 case ASTNode.EMPTY_STATEMENT :
	    return true;
	 case ASTNode.BOOLEAN_LITERAL :
	 case ASTNode.CHARACTER_LITERAL :
	 case ASTNode.NULL_LITERAL :
	 case ASTNode.NUMBER_LITERAL :
	 case ASTNode.TYPE_LITERAL :
	    return true;
	 case ASTNode.PARENTHESIZED_EXPRESSION :
	    return doesNothing(((ParenthesizedExpression) n).getExpression());
	 case ASTNode.METHOD_INVOCATION :
	    MethodInvocation mi = (MethodInvocation) n;
	    JcompSymbol js = JavaAst.getReference(mi.getName());
	    if (js != null) {
	       String nm = js.getFullName();
	       switch (nm) {
		  case "edu.brown.cs.s6.runner.RunnerAssert.fail" :
		     ASTNode nx = mi.getParent().getParent().getParent();
		     if (nx.getNodeType() == ASTNode.TRY_STATEMENT)
			return true;
		     break;
		  default :
		     break;
		}
	     }
	    break;
	 default :
	    break;
       }
      return false;
   }

}	// end of subclass UndefFinder






/********************************************************************************/
/*										*/
/*	Class to handle return type mappings					*/
/*										*/
/********************************************************************************/

private class UndefMapper extends TreeMapper {

   private String for_whom;
   private Set<ASTNode> remove_nodes;
   private Set<JcompSymbol> check_syms;
   private boolean fix_override;

   UndefMapper(String whom,Set<ASTNode> undefs,Set<JcompSymbol> checks) {
      for_whom = whom;
      remove_nodes = undefs;
      check_syms = checks;
      fix_override = false;
      for (ASTNode n : undefs) {
	 if (n.getLocationInParent() == TypeDeclaration.SUPERCLASS_TYPE_PROPERTY ||
		n.getLocationInParent() == TypeDeclaration.SUPER_INTERFACE_TYPES_PROPERTY)
	    fix_override = true;
	 else if (n.getNodeType() == ASTNode.METHOD_DECLARATION) {
	    // might want to restrict to methods with this name
	    fix_override = true;
	  }
       }
    }

   @Override protected String getSpecificsName()   { return for_whom; }

   @Override S6SolutionFlag getSolutionFlag()		{ return S6SolutionFlag.COMPILES; }

   @Override void rewriteTree(ASTNode orig,ASTRewrite rw) {
      if (remove_nodes.contains(orig)) {
	 StructuralPropertyDescriptor spd = orig.getLocationInParent();
	 if (do_debug) System.err.println("TRANSFORM DELETE " + spd + " : " + orig);
	 ASTNode par = orig.getParent();
	 if (delete_froms.contains(spd)) {
	    if (spd.isChildListProperty()) {
	       ListRewrite lrw = rw.getListRewrite(par,(ChildListPropertyDescriptor) spd);
	       lrw.remove(orig,null);
	     }
	    else if (spd.isChildProperty()) {
	       rw.set(par,spd,null,null);
	     }
	  }
	 else if (spd == IfStatement.THEN_STATEMENT_PROPERTY) {
	    EmptyStatement es = rw.getAST().newEmptyStatement();
	    rw.set(par,spd,es,null);
	  }
	 else if (spd == IfStatement.ELSE_STATEMENT_PROPERTY) {
	    rw.set(par,spd,null,null);
	  }
	 else  {
	    if (do_debug) System.err.println("CHECK REWRITE FOR " + orig.getParent());
	    // replace with something appropriate
	  }
       }
      else if (orig.getNodeType() == ASTNode.VARIABLE_DECLARATION_FRAGMENT) {
	 VariableDeclarationFragment vdf = (VariableDeclarationFragment) orig;
	 JcompSymbol js = JavaAst.getDefinition(orig);
	 if (js != null && check_syms != null && check_syms.contains(js)) {
	    JcompType jt = js.getType();
	    if (vdf.getInitializer() == null) {
	       Expression ex = jt.createDefaultValue(rw.getAST());
	       rw.set(orig,VariableDeclarationFragment.INITIALIZER_PROPERTY,ex,null);
	     }
	  }
       }
      else if (orig.getNodeType() == ASTNode.METHOD_DECLARATION) {
	 MethodDeclaration md = (MethodDeclaration) orig;
	 JcompSymbol js = JavaAst.getDefinition(orig);
	 if (js != null && check_syms != null && check_syms.contains(js)) {
	    Block b = md.getBody();
	    JcompType jt = js.getType();
	    JcompType rt = jt.getBaseType();
	    if (b != null && rt != null && !rt.isVoidType()) {
	       if (needsReturn(b)) {
		  AST ast = rw.getAST();
		  ReturnStatement rs = ast.newReturnStatement();
		  Expression ex = rt.createDefaultValue(ast);
		  rs.setExpression(ex);
		  ListRewrite lrw = rw.getListRewrite(b,Block.STATEMENTS_PROPERTY);
		  lrw.insertLast(rs,null);
		}
	     }
	  }
	 if (fix_override) {
	    if (Modifier.isPublic(md.getModifiers()) || Modifier.isProtected(md.getModifiers())) {
	       for (Object o : md.modifiers()) {
		  IExtendedModifier iem = (IExtendedModifier) o;
		  if (iem.isAnnotation()) {
		     Annotation ann = (Annotation) iem;
		     String tnm = ann.getTypeName().getFullyQualifiedName();
		     if (tnm.contains("Override")) {
			ListRewrite lrw = rw.getListRewrite(md,MethodDeclaration.MODIFIERS2_PROPERTY);
			lrw.remove((ASTNode) o,null);
			break;
		      }
		   }
		}
	     }
	  }
       }
    }

   private boolean needsReturn(Block b) {
      if (JavaAst.checkHasReturn(b)) return false;
      List<?> stmts = b.statements();
      int ln = stmts.size();
      if (ln == 0) return true;
      ASTNode n = (ASTNode) stmts.get(ln-1);
      if (n.getNodeType() == ASTNode.THROW_STATEMENT) return false;
      return true;
    }

}	// end of subclass UndefMapper




}	// end of class TransformRemoveUndef





/* end of TransformRemoveUndef.java */
