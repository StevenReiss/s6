/********************************************************************************/
/*										*/
/*		TransformFixSwing.java						*/
/*										*/
/*	Handle changes to ensure swing code can compile 			*/
/*										*/
/********************************************************************************/
/*	Copyright 2013 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2013,
 Brown University, Providence, RI.			      *
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
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import edu.brown.cs.ivy.jcomp.*;
import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6Solution;
import edu.brown.cs.s6.common.S6SolutionSet;



public class TransformFixSwing extends TransformJava implements S6Constants, JavaConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private static Set<String> fix_methods;
private static JcompType    component_type = null;
private static JcompType    android_type = null;

static {
   fix_methods = new HashSet<String>();
   fix_methods.add("setText");
   fix_methods.add("setIcon");
   fix_methods.add("setIconImage");
   fix_methods.add("createTitledBorder");
}



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public TransformFixSwing(String name)
{
   super(name);
}




/********************************************************************************/
/*										*/
/*	Handle a set of swing-based fixes					*/
/*										*/
/********************************************************************************/

@Override protected TreeMapper findPackageMapping(S6SolutionSet ss,
      CompilationUnit cu,
      S6Request.PackageSignature psg,S6Solution sol)
{
   setComponentType(cu);

   SwingFinder sf = new SwingFinder();
   cu.accept(sf);

   if (!sf.hasChanges()) return null;

   SwingFixup fix = new SwingFixup(sf);

   return fix;
}



private synchronized static void setComponentType(ASTNode n)
{
   if (component_type != null) return;
   JcompTyper typer = JavaAst.getTyper(n);
   component_type = typer.findSystemType("java.awt.Component");
   android_type = typer.findSystemType("android.view.View");
}



/********************************************************************************/
/*										*/
/*	Tree walker to find swing-based changes 				*/
/*										*/
/********************************************************************************/

private static class SwingFinder extends ASTVisitor {

   private boolean have_undef;
   private Stack<Boolean> undef_stack;
   private Set<ASTNode> to_fix;
   private Set<ASTNode> to_remove;

   SwingFinder() {
      have_undef = false;
      undef_stack = new Stack<Boolean>();
      to_fix = new HashSet<ASTNode>();
      to_remove = new HashSet<ASTNode>();
    }

   boolean hasChanges() {
      return to_fix.size() > 0 || to_remove.size() > 0;
    }
   boolean fixNode(ASTNode n)		{ return to_fix.contains(n); }
   boolean removeNode(ASTNode n)	{ return to_remove.contains(n); }

   @Override public void endVisit(TypeDeclaration n) {
      Type t = n.getSuperclassType();
      if (t == null) return;
      JcompType jt = JcompAst.getJavaType(t);
      if (jt != null && jt.isUndefined()) {
	 String nm = jt.getName();
	 if (nm.endsWith("Activity") || nm.endsWith("Fragment")) {
	    to_fix.add(n);
	  }
       }
    }


   @Override public void endVisit(MethodInvocation n) {
      JcompSymbol js = JavaAst.getReference(n);
      if (have_undef && js != null) {
	 JcompType jt = js.getClassType();
	 if (jt != null) {
	    String tnm = jt.getName();
	    if (tnm.startsWith("javax.swing.") || tnm.startsWith("java.awt.")) {
	       if (canFixMethod(js)) {		      // can't fix add(...)
		  // System.err.println("SWING FIX CALL " + js.getName() + " " + tnm + " @ " + n);
		  to_fix.add(n);
		  have_undef = false;
		}
	     }
	  }
       }
      else if (js != null && js.getName().equals("setVisible")) {
	 JcompType jt = js.getClassType();
	 if (jt != null) {
	    String tnm = jt.getName();
	    if (tnm.equals("java.awt.Dialog")) {
	       to_remove.add(n);
	     }
	  }
       }
      else if (js != null &&  js.getName().equals("load")) {
	 JcompType jt = js.getClassType();
	 if (jt != null) {
	    String tnm = jt.getName();
	    if (tnm.startsWith("java.util.Properties")) {
	       // System.err.println("SWING REM CALL " + js.getName() + " " + tnm + " @ " + n);
	       to_remove.add(n);
	     }
	  }
       }
      else if (js != null && js.getName().equals("setIconImage")) {
	 JcompType jt = js.getClassType();
	 if (jt != null) {
	    String tnm = jt.getName();
	    if (tnm.startsWith("javax.swing.JFrame")) {
	       to_fix.add(n);
	     }
	  }
       }
    }

   @Override public void endVisit(ClassInstanceCreation n) {
      JcompType jt = JavaAst.getJavaType(n.getType());
      if (jt != null && n.getAnonymousClassDeclaration() == null) {
	 String tnm = jt.getName();
	 if (tnm.startsWith("javax.swing.") || tnm.startsWith("java.awt.")) {
	    boolean haveundef = have_undef;
	    if (tnm.contains("Icon")) haveundef = true;
	    if (haveundef && canFixNew(jt,n)) {
	       // System.err.println("SWING FIX NEW " + tnm + " @ " + n);
	       to_fix.add(n);
	       have_undef = false;
	     }
	  }
       }
    }

   @Override public void preVisit(ASTNode n) {
      undef_stack.push(have_undef);
      have_undef = false;
    }

   @Override public void postVisit(ASTNode n) {
      JcompType jt = JavaAst.getExprType(n);
      if (jt != null && jt.isErrorType()) have_undef = true;
      if (jt != null && jt.isUndefined())
	 have_undef = true;
      have_undef |= undef_stack.pop();
    }

   private boolean canFixMethod(JcompSymbol js) {
      String mnm = js.getName();
      if (!fix_methods.contains(mnm)) return false;

      JcompType mty = js.getType();
      boolean known = true;
      for (JcompType jt : mty.getComponents()) {
	 if (jt.isPrimitiveType()) continue;
	 String jnm = jt.getName();
	 if (!jnm.equals("java.lang.String") &&
	       !jnm.equals("java.awt.Color") &&
	       !jnm.equals("javax.swing.Icon") &&
	       !jnm.equals("java.awt.Image") &&
	       !jt.isCompatibleWith(component_type)) {
	    // TODO: also check
	    boolean fnd = false;
	    if (android_type != null) {
	       if (jt.isCompatibleWith(android_type)) fnd = true;
	       if (jnm.startsWith("android.media.")) fnd = true;
	       if (jnm.startsWith("android.graphics.")) fnd = true;
	       if (jnm.startsWith("android.app.Activity")) fnd = true;
	       if (jnm.startsWith("android.app.Framework")) fnd = true;
	     }
	    if (!fnd) known = false;
	  }
       }
      // System.err.println("SWING: CHECK CAN FIX " + mnm + " " + known);
      if (known) return true;

      return false;
    }

   private boolean canFixNew(JcompType jt,ClassInstanceCreation cic) {
      if (cic.getAnonymousClassDeclaration() != null) return false;
      if (jt.getName().contains("Icon")) return true;
      if (jt.getName().contains("TextField")) return true;
      if (jt.getName().contains("JTextArea")) return true;
      if (jt.getName().contains("JPassword")) return true;
      if (jt.getName().contains("JCheckBox")) return true;
      if (jt.getName().contains("JLabel")) return true;
      if (jt.getName().contains("Button")) return true;
      if (jt.getName().contains("JMenu")) return true;
      if (jt.getName().contains("JPanel")) return true;
      return false;
    }

}	// end of inner class SwingFinder



/********************************************************************************/
/*										*/
/*	Class to do the tree rewriting						*/
/*										*/
/********************************************************************************/

private class SwingFixup extends TreeMapper {

   private SwingFinder swing_finder;

   SwingFixup(SwingFinder sf) {
      swing_finder = sf;
    }

   @Override void rewriteTree(ASTNode orig,ASTRewrite rw) {
      if (swing_finder.fixNode(orig)) {
	 switch (orig.getNodeType()) {
	    case ASTNode.METHOD_INVOCATION :
	       fixMethodInvocation((MethodInvocation) orig,rw);
	       break;
	    case ASTNode.CLASS_INSTANCE_CREATION :
	       fixInstanceCreation((ClassInstanceCreation) orig,rw);
	       break;
	    case ASTNode.TYPE_DECLARATION :
	       fixTypeDeclaration((TypeDeclaration) orig,rw);
	       break;
	  }
       }
      else if (swing_finder.removeNode(orig)) {
	 ASTNode n = orig;
	 for ( ; n != null; n = n.getParent()) {
	    if (!(n instanceof Expression)) break;
	  }
	 StructuralPropertyDescriptor spd = n.getLocationInParent();
	 if (spd.isChildListProperty()) {
	    ListRewrite lrw = rw.getListRewrite(n.getParent(),(ChildListPropertyDescriptor) spd);
	    lrw.remove(n,null);
	  }
	 else {
	    rw.remove(n,null);
	  }
	 // System.err.println("SWING: REMOVE " + n);
	 // if parent is ExpressionStatement, remove parent
       }
    }

   private void fixMethodInvocation(MethodInvocation orig,ASTRewrite rw) {
      JcompSymbol js = JavaAst.getReference(orig);
      JcompType jt = js.getType();
      ListRewrite lrw = rw.getListRewrite(orig,MethodInvocation.ARGUMENTS_PROPERTY);
      fixArguments(orig,lrw,jt);
    }

   private void fixTypeDeclaration(TypeDeclaration td,ASTRewrite rw) {
      Type t = td.getSuperclassType();
      if (t != null) {
	 JcompType jt = JcompAst.getJavaType(t);
	 if (jt != null && jt.isUndefined()) {
	    String repnm = null;
	    String nm = jt.getName();
	    if (nm.endsWith("Activity")) repnm = "android.app.Activity";
	    else if (nm.endsWith("Fragment")) repnm = "android.app.Fragment";
	    if (repnm != null) {
	       Name nnm = JcompAst.getQualifiedName(rw.getAST(),repnm);
	       Type rept = rw.getAST().newSimpleType(nnm);
	       rw.replace(t,rept,null);
	     }
	  }
       }
    }

   private void fixInstanceCreation(ClassInstanceCreation orig,ASTRewrite rw) {
      JcompType jt = JavaAst.getJavaType(orig.getType());
      boolean fixicon = false;
      boolean useargs = true;
      if (jt.getName().contains("Icon")) fixicon = true;
      else if (jt.getName().equals("javax.swing.JLabel")) useargs = true;
      else if (jt.getName().equals("javax.swing.JButton") ||
	    jt.getName().equals("javax.swing.JMenuItem") ||
	    jt.getName().equals("javax.swing.JToggleButton")) {
	 JcompSymbol js = JavaAst.getReference(orig);
	 if (js != null) {
	    JcompType cjt = js.getType();
	    for (JcompType atyp : cjt.getComponents()) {
	       if (atyp.getName().equals("javax.swing.Action")) useargs = false;
	     }
	  }
	 else useargs = false;
       }
      else {
	 JcompScope jscp = jt.getScope();
	 for (JcompSymbol js : jscp.getDefinedMethods()) {
	    if (js.isConstructorSymbol()) {
	       JcompType cnty = js.getType();
	       if (cnty.getComponents().size() == 0) useargs = false;
	     }
	  }

       }
      ListRewrite lrw = rw.getListRewrite(orig,ClassInstanceCreation.ARGUMENTS_PROPERTY);
      if (useargs && JavaAst.getReference(orig) == null)
	 useargs = false;

      if (fixicon) {
	 AST ast = rw.getAST();
	 StringLiteral sl = ast.newStringLiteral();
	 sl.setLiteralValue("/research/s6/lib/sample.png");
	 int act = 0;
	 for (Object o : lrw.getOriginalList()) {
	    ASTNode on = (ASTNode) o;
	    if (act++ == 0) lrw.replace(on,sl,null);
	    else lrw.remove(on,null);
	  }
       }
      else if (useargs) {
	 JcompSymbol js = JavaAst.getReference(orig);
	 JcompType cjt = js.getType();
	 fixArguments(orig,lrw,cjt);
       }
      else {
	 for (Object o : lrw.getOriginalList()) {
	    ASTNode on = (ASTNode) o;
	    lrw.remove(on,null);
	  }
       }
    }

   private void fixArguments(ASTNode orig,ListRewrite lrw,JcompType jt) {
      AST ast = lrw.getASTRewrite().getAST();
      int act = 0;

      List<?> args = lrw.getOriginalList();
      for (JcompType aty : jt.getComponents()) {
	 Expression ex = null;
	 String tnm = aty.getName();
	 if (aty.isPrimitiveType()) {
	    ex = aty.createDefaultValue(ast);
	  }
	 else if (tnm.equals("java.lang.String")) {
	    String lbl = JavaAst.getUniqueString("S6_Label_");
	    StringLiteral sl = ast.newStringLiteral();
	    sl.setLiteralValue(lbl);
	    ex = sl;
	  }
	 else if (tnm.equals("java.awt.Color")) {
	    ex = JavaAst.getQualifiedName(ast,"java.awt.Color.RED");
	  }
	 else if (tnm.equals("javax.swing.Icon")) {
	    ClassInstanceCreation cic = ast.newClassInstanceCreation();
	    Name nm = JavaAst.getQualifiedName(ast,"javax.swing.ImageIcon");
	    Type nty = ast.newSimpleType(nm);
	    cic.setType(nty);
	    StringLiteral sl = ast.newStringLiteral();
	    sl.setLiteralValue("/research/s6/lib/sample.png");
	    @SuppressWarnings("unchecked") List<Expression> cargs = cic.arguments();
	    cargs.add(sl);
	    ex = cic;
	  }
	 else if (tnm.equals("java.awt.Image")) {
	    ClassInstanceCreation cic = ast.newClassInstanceCreation();
	    Name nm = JavaAst.getQualifiedName(ast,"javax.swing.ImageIcon");
	    Type nty = ast.newSimpleType(nm);
	    cic.setType(nty);
	    StringLiteral sl = ast.newStringLiteral();
	    sl.setLiteralValue("/research/s6/lib/sample.png");
	    @SuppressWarnings("unchecked") List<Expression> cargs = cic.arguments();
	    cargs.add(sl);
	    MethodInvocation mi = ast.newMethodInvocation();
	    mi.setExpression(cic);
	    mi.setName(JavaAst.getSimpleName(ast,"getImage"));
	    ex = mi;
	  }
	 else if (aty.isCompatibleWith(component_type)) {
	    ClassInstanceCreation cic = ast.newClassInstanceCreation();
	    Name nm = JavaAst.getQualifiedName(ast,"javax.swing.JLabel");
	    Type nty = ast.newSimpleType(nm);
	    cic.setType(nty);
	    StringLiteral sl = ast.newStringLiteral();
	    sl.setLiteralValue("S6:Dummy");
	    @SuppressWarnings("unchecked") List<Expression> cargs = cic.arguments();
	    cargs.add(sl);
	    ex = cic;
	    // ex = aty.createNonNullValue(ast);
	    // if (ex == null) ex = aty.createDefaultValue(ast);
	  }
	 else {
	    ex = aty.createDefaultValue(ast);
	  }
	 ASTNode oex = (ASTNode) args.get(act++);
	 if (ex == null) lrw.remove(oex,null);
	 else lrw.replace(oex,ex,null);
       }
    }

}	// end of inner class SwingFixup




}	// end of class TransformFixSwing




/* end of TransformFixSwing.java */

