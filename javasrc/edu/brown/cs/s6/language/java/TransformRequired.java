/********************************************************************************/
/*										*/
/*		TransformRequired.java						*/
/*										*/
/*	Transformation that removing code not relevant to UI			*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/language/java/TransformRequired.java,v 1.5 2015/09/23 17:54:54 spr Exp $ */


/*********************************************************************************
 *
 * $Log: TransformRequired.java,v $
 * Revision 1.5  2015/09/23 17:54:54  spr
 * Version to handle andriod UI applications.
 *
 * Revision 1.4  2014/08/29 15:16:10  spr
 * Updates for suise, testcases.
 *
 * Revision 1.3  2013/09/13 20:33:05  spr
 * Add calls for UI search.
 *
 * Revision 1.2  2013-05-09 12:26:22  spr
 * Minor changes to start ui fixups.
 *
 * Revision 1.1  2012-08-13 16:52:47  spr
 * Add code for new transforms.
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.language.java;


import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import edu.brown.cs.ivy.jcomp.JcompSymbol;
import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.ivy.jcomp.JcompTyper;
import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6Solution;
import edu.brown.cs.s6.common.S6SolutionSet;



public class TransformRequired extends TransformJava implements S6Constants, JavaConstants {




/********************************************************************************/
/*										*/
/*	Consructors								*/
/*										*/
/********************************************************************************/

public TransformRequired(String name)
{
   super(name);
}



/********************************************************************************/
/*										*/
/*	Handle package mappings 						*/
/*										*/
/********************************************************************************/

@Override
protected TreeMapper findPackageMapping(S6SolutionSet ss,CompilationUnit cu,
      S6Request.PackageSignature sg,
      S6Solution sol)
{
   Required rqd = new Required();

   for (Object o : cu.types()) {
      AbstractTypeDeclaration atd = (AbstractTypeDeclaration) o;
      if (atd.getName().getIdentifier().equals("S6_UI_CLASS")) {
	 for (Object o1 : atd.bodyDeclarations()) {
	    BodyDeclaration bd = (BodyDeclaration) o1;
	    JcompSymbol js = JavaAst.getDefinition(bd);
	    rqd.add(js);
	  }
       }
    }
   if (rqd.isEmpty()) return null;

   while (addRequired(cu,rqd)) ;
   if (!rqd.hasExclude()) return null;

   RemoveUnneeded ru = new RemoveUnneeded(rqd);

   return ru;
}



/********************************************************************************/
/*										*/
/*	Disable all other mappings						*/
/*										*/
/********************************************************************************/

@Override protected boolean checkApplyClassForPackage(S6SolutionSet sols,
							 CompilationUnit cu,
							 S6Request.ClassSignature csg,
							 AbstractTypeDeclaration td)
{
   return false;
}




@Override protected boolean checkApplyMethodForClass(S6SolutionSet sols,
							S6Solution sol,
							TypeDeclaration td,
							S6Request.MethodSignature msg,
							MethodDeclaration md)
{
   return false;
}



/********************************************************************************/
/*										*/
/*	Dependency finding methods						*/
/*										*/
/********************************************************************************/

private boolean addRequired(CompilationUnit cu,Required rqd)
{
   RequireWalk rw = new RequireWalk(cu,rqd);
   rqd.setExclude(false);
   cu.accept(rw);
   return rw.hasChanged();
}





private class RequireWalk extends ASTVisitor {

   private boolean has_change;
   private Required required_items;
   private JcompTyper java_typer;
   private int anon_depth;

   RequireWalk(CompilationUnit cu,Required rqd) {
      has_change = false;
      required_items = rqd;
      java_typer = JavaAst.getTyper(cu);
      anon_depth = 0;
    }

   boolean hasChanged() 			{ return has_change; }

   @Override public boolean visit(MethodDeclaration md) {
      if (required_items.contains(md)) return false;	// already done
      JcompSymbol js = JavaAst.getDefinition(md);
      boolean use = anon_depth > 0;
      if (js != null && required_items.contains(java_typer,js)) use = true;
      if (isOverride(md)) use = true;
      if (isNeededConstructor(md)) use = true;
      if (use) {
	 has_change |= required_items.add(md);
	 return true;
       }
      else required_items.setExclude(true);
      return false;
    }

   @Override public boolean visit(VariableDeclarationFragment n) {
      if (n.getParent().getNodeType() == ASTNode.FIELD_DECLARATION) {
	 if (required_items.contains(n)) return false;
	 JcompSymbol js = JavaAst.getDefinition(n);
	 boolean use = anon_depth > 0;
	 if (js != null && required_items.contains(java_typer,js)) use = true;
	 if (use) {
	    has_change |= required_items.add(n);
	    return true;
	  }
	 else required_items.setExclude(true);
	 return false;
       }
      return true;
    }

   @Override public boolean visit(AnonymousClassDeclaration n) {
      ++anon_depth;
      return true;
    }
   @Override public void endVisit(AnonymousClassDeclaration n) {
      --anon_depth;
    }

   @Override public void postVisit(ASTNode n) {
      JcompSymbol js = JavaAst.getReference(n);
      if (js != null) {
	 if (js.isMethodSymbol() || js.isConstructorSymbol() ||
	       js.isFieldSymbol()) {
	    if (!js.isKnown())
	       has_change |= required_items.add(js);
	  }
       }
    }

   private boolean isOverride(MethodDeclaration md) {
      if (Modifier.isPrivate(md.getModifiers())) return false;
      if (md.isConstructor()) return false;
      JcompSymbol js = JavaAst.getDefinition(md);
      return isDefinedIn(js.getClassType(),js,false);
    }

   private boolean isDefinedIn(JcompType jt,JcompSymbol js,boolean cur) {
      if (jt == null || js == null) return false;
      JcompSymbol njs = jt.lookupMethod(java_typer,js.getName(),js.getType());
      if (cur && njs != null) return true;
      if (isDefinedIn(jt.getSuperType(),js,true)) return true;
      if (jt.getInterfaces() != null) {
         for (JcompType jit : jt.getInterfaces()) {
            if (isDefinedIn(jit,js,true)) return true;
         }
       }
      return false;
    }

   private boolean isNeededConstructor(MethodDeclaration md) {
      if (!md.isConstructor()) return false;

      // constructors for inner classes seem to be missed
      ASTNode gp = md.getParent().getParent();
      if (gp != null && gp.getNodeType() == ASTNode.TYPE_DECLARATION) return true;

      if (md.parameters().size() > 0) return false;
      if (Modifier.isPrivate(md.getModifiers())) return false;
      // check if there is a subclass with no constructor or with
      // a constructor with no super(...) or with a super().
      return true;
   }


}	// end of inner class ReuqireWalk







private class Required {

   private Set<ASTNode> required_nodes;
   private Set<JcompSymbol> required_methods;
   private boolean has_exclude;

   Required() {
      required_nodes = new HashSet<ASTNode>();
      required_methods = new HashSet<JcompSymbol>();
      has_exclude = false;
    }

   boolean isEmpty() {
      return required_nodes.isEmpty() && required_methods.isEmpty();
    }
   void setExclude(boolean fg)		{ has_exclude = fg; }
   boolean hasExclude() 		{ return has_exclude; }

   boolean add(ASTNode n)		{ return required_nodes.add(n); }

   boolean add(JcompSymbol js)		{ return required_methods.add(js); }

   boolean contains(ASTNode n)		{ return required_nodes.contains(n); }

   boolean contains(JcompTyper typr,JcompSymbol js) {
      if (required_methods.contains(js)) return true;
      if (js.isConstructorSymbol()) return false;
      if (js.isMethodSymbol()) {
	 JcompType jt = js.getClassType();
	 for ( ; ; ) {
	    jt = jt.getSuperType();
	    if (jt == null) break;
	    JcompSymbol njs = jt.lookupMethod(typr,js.getName(),js.getType());
	    if (required_methods.contains(njs)) return true;
	  }
       }
      return false;
    }

}	// end of inner class Required



/********************************************************************************/
/*										*/
/*	Mapping to remove unneeded items					*/
/*										*/
/********************************************************************************/

private class RemoveUnneeded extends TreeMapper {

   private Required required_items;

   RemoveUnneeded(Required rqd) {
      required_items = rqd;
    }

   @Override void rewriteTree(ASTNode orig,ASTRewrite rw) {
      if (orig instanceof AbstractTypeDeclaration) {
	 AbstractTypeDeclaration atd = (AbstractTypeDeclaration) orig;
	 boolean keep = false;
	 for (Object o : atd.bodyDeclarations()) {
	    BodyDeclaration bd = (BodyDeclaration) o;
	    keep |= keep(bd);
	  }
	 if (keep) {
	    required_items.add(atd);		// in case this is an inner type
	    for (Object o : atd.bodyDeclarations()) {
	       BodyDeclaration bd = (BodyDeclaration) o;
	       remove(bd,rw);
	     }
	  }
	 else {
	    rw.remove(atd,null);
	  }
       }
    }

  private void remove(BodyDeclaration bd,ASTRewrite rw) {
     if (bd instanceof MethodDeclaration) {
	MethodDeclaration md = (MethodDeclaration) bd;
	if (!required_items.contains(md)) {
	   rw.remove(md,null);
	 }
      }
     else if (bd instanceof TypeDeclaration) {
	TypeDeclaration td = (TypeDeclaration) bd;
	if (!required_items.contains(td)) {
	   rw.remove(td,null);
	 }
      }
     else if (bd instanceof FieldDeclaration) {
	FieldDeclaration fd = (FieldDeclaration) bd;
	if (keep(fd)) {
	   for (Object o : fd.fragments()) {
	      VariableDeclarationFragment vdf = (VariableDeclarationFragment) o;
	      if (!required_items.contains(vdf)) rw.remove(vdf,null);
	    }
	 }
	else {
	   rw.remove(fd,null);
	 }
      }
   }

  private boolean keep(BodyDeclaration bd) {
     if (bd instanceof MethodDeclaration) {
	return required_items.contains(bd);
      }
     else if (bd instanceof FieldDeclaration) {
	FieldDeclaration fd = (FieldDeclaration) bd;
	for (Object o : fd.fragments()) {
	   VariableDeclarationFragment vdf = (VariableDeclarationFragment) o;
	   if (required_items.contains(vdf)) return true;
	 }
	return false;
      }
     else if (bd instanceof TypeDeclaration) {
	return required_items.contains(bd);
      }
     else return true;
  }

}	// end of inner class RemoveUnneeded



}	// end of class TransformRequired




/* end of TransformRequired.java */
