/********************************************************************************/
/*										*/
/*		TransformSplitClass.java					*/
/*										*/
/*	Transformation that splits a class into two clases			*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/language/java/TransformSplitClass.java,v 1.4 2015/09/23 17:54:54 spr Exp $ */


/*********************************************************************************
 *
 * $Log: TransformSplitClass.java,v $
 * Revision 1.4  2015/09/23 17:54:54  spr
 * Version to handle andriod UI applications.
 *
 * Revision 1.3  2014/08/29 15:16:10  spr
 * Updates for suise, testcases.
 *
 * Revision 1.2  2013/09/13 20:33:05  spr
 * Add calls for UI search.
 *
 * Revision 1.1  2012-06-11 14:07:49  spr
 * add framework search; fix bugs
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.language.java;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import edu.brown.cs.ivy.jcomp.JcompSymbol;
import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.ivy.jcomp.JcompTyper;
import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6Solution;
import edu.brown.cs.s6.common.S6SolutionSet;
import edu.brown.cs.s6.common.S6Transform;



public class TransformSplitClass extends TransformJava implements S6Constants, JavaConstants {




/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private static	int	name_counter = 0;

private static boolean	use_interfaces = false;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public TransformSplitClass(String name)
{
   super(name);
}



/********************************************************************************/
/*										*/
/*	Methods to get a unique name						*/
/*										*/
/********************************************************************************/

synchronized static String newName(String orig)
{
   return S6_NAME_PREFIX + orig + "_" + (++name_counter);
}




/********************************************************************************/
/*										*/
/*	Handle package renamings						*/
/*										*/
/********************************************************************************/

@Override protected Collection<TreeMapper> findPackageMappings(S6SolutionSet sols,S6Solution sol,
      CompilationUnit cu,S6Request.PackageSignature psg)
{
   List<TreeMapper> rslt = new ArrayList<TreeMapper>();
   PackageDeclaration pd = cu.getPackage();
   if (pd == null) return null;
   String pnm = pd.getName().getFullyQualifiedName();
   if (!psg.getName().equals(pnm)) return null;

   String last = null;
   for (S6Transform.Memo m : sol.getTransforms()) {
      String nm = m.getTransformName();
      if (nm.startsWith(getName() + "@")) {
	 int idx = nm.indexOf("@");
	 String cnm = nm.substring(idx+1);
	 if (last == null || last.compareTo(cnm) < 0) last = cnm;
       }
    }

   for (Object o : cu.types()) {
      AbstractTypeDeclaration atd = (AbstractTypeDeclaration) o;
      List<S6Request.ClassSignature> clsgs = new ArrayList<S6Request.ClassSignature>();
      for (S6Request.ClassSignature cs : psg.getClasses()) {
	 if (cs.isMain() || cs.getInterfaces().size() > 0) {
	    if (JavaAst.checkTypeSignature(atd,cs,S6SignatureType.ALL,psg.getName())) {
	       clsgs.add(cs);
	     }
	  }
       }
      String anm = atd.getName().getIdentifier();
      if (last != null && last.compareTo(anm) >= 0) continue;
      if (clsgs.size() > 1) {
	 addMappingsForClass((TypeDeclaration) atd,clsgs,rslt);
       }
    }

   if (rslt.size() == 0) return null;

   return rslt;
}


private void addMappingsForClass(TypeDeclaration atd,List<S6Request.ClassSignature> clsgs,
      List<TreeMapper> rslt)
{
   Map<S6Request.ClassSignature,Set<MethodDeclaration>> rtns =
      new HashMap<S6Request.ClassSignature,Set<MethodDeclaration>>();

   for (S6Request.ClassSignature cs : clsgs) {
      if (cs.isMain()) {
	  for (MethodDeclaration md : atd.getMethods()) {
	     if (md.getName().getIdentifier().equals("main") &&
		   Modifier.isStatic(md.getModifiers())) {
		Set<MethodDeclaration> rset = new HashSet<MethodDeclaration>();
		rset.add(md);
		rtns.put(cs,rset);
		break;
	      }
	   }
       }
    }

   if (use_interfaces) {
      JcompTyper jtr = JavaAst.getTyper(atd);
      for (S6Request.ClassSignature cs : clsgs) {
	 Set<MethodDeclaration> rset = new HashSet<MethodDeclaration>();
	 for (String s : cs.getInterfaces()) {
	    List<JcompSymbol> mthds = jtr.findKnownMethods(s);
	    if (mthds == null) {
	       rset = null;
	       break;
	     }
	    for (MethodDeclaration md : atd.getMethods()) {
	       boolean match = false;
	       for (JcompSymbol js : mthds) {
		  if (matchMethod(md,js)) match = true;
		}
	       if (match) rset.add(md);
	     }
	  }
	 if (rset != null && rset.size() > 0) {
	    Set<MethodDeclaration> mds = rtns.get(cs);
	    if (mds != null) rset.addAll(mds);
	    rtns.put(cs,rset);
	  }
       }
    }

   for (Map.Entry<S6Request.ClassSignature,Set<MethodDeclaration>> ent : rtns.entrySet()) {
      S6Request.ClassSignature csg = ent.getKey();
      Set<MethodDeclaration> mds = ent.getValue();
      ClassSplitter cs = new ClassSplitter(csg,atd,mds);
      if (extendMethods(cs)) rslt.add(cs);
    }
}


private boolean matchMethod(MethodDeclaration md,JcompSymbol js)
{
   String mnm = md.getName().getIdentifier();
   if (!mnm.equals(js.getName())) return false;
   // check arguments here

   return true;
}



/********************************************************************************/
/*										*/
/*	Compute additional methods to move by defining dependencies		*/
/*										*/
/********************************************************************************/

private boolean extendMethods(ClassSplitter cs)
{
   boolean chng = true;
   while (chng) {
      chng = false;
      for (BodyDeclaration bd : cs.getItems()) {
	 DependCheck dc = new DependCheck(cs);
	 bd.accept(dc);
	 chng |= dc.getChanged();
       }
    }
   return true;
}


private class DependCheck extends ASTVisitor {

   private ClassSplitter class_splitter;
   private boolean has_changed;
   private Set<JcompSymbol> defined_syms;
   private boolean is_static;

   DependCheck(ClassSplitter cs) {
      class_splitter = cs;
      has_changed = false;
      defined_syms = new HashSet<JcompSymbol>(cs.getDefinedItems());
      is_static = false;
    }

   @Override public void preVisit(ASTNode n) {
      JcompSymbol js = JavaAst.getDefinition(n);
      if (js != null) defined_syms.add(js);
      if (n instanceof BodyDeclaration) {
	 BodyDeclaration md = (BodyDeclaration) n;
	 is_static = Modifier.isStatic(md.getModifiers());
       }
    }

   @Override public void postVisit(ASTNode n) {
      JcompSymbol js = JavaAst.getReference(n);
      if (js == null || defined_syms.contains(js)) return;
      if (js.isBinarySymbol()) return;
      ASTNode an = js.getDefinitionNode();
      if (an == null) return;
      if (an instanceof TypeDeclaration) {
         String fnm = js.getFullName();
         if (!fnm.contains("$")) return;
       }
      if (js.isConstructorSymbol()) {
         if (is_static) return;
         JcompType jt = js.getClassType();
         JcompType jt1 = class_splitter.getBaseType();
         if (jt != jt1) {
            if (jt.getName().contains("$")) {
               while (an != null && an.getNodeType() != ASTNode.TYPE_DECLARATION) {
        	  an = an.getParent();
        	}
               js = JavaAst.getDefinition(an);
               String fnm = js.getFullName();
               if (!fnm.contains("$")) return;
             }
          }
       }
      if (an instanceof BodyDeclaration) {
         BodyDeclaration bd = (BodyDeclaration) an;
         ASTNode xn = bd;
         while (xn != null && xn.getNodeType() != ASTNode.TYPE_DECLARATION) {
            xn = xn.getParent();
          }
         if (xn == null || xn != class_splitter.getTypeDeclaration()) return;
         if (class_splitter.addToMove(bd)) has_changed = true;
       }
    }

   boolean getChanged() 			{ return has_changed; }

}




/********************************************************************************/
/*										*/
/*	Class to handle name mappings						*/
/*										*/
/********************************************************************************/

private class ClassSplitter extends TreeMapper {

   private S6Request.ClassSignature class_signature;
   private String map_name;
   private TypeDeclaration for_class;
   private Set<BodyDeclaration> move_items;
   private Set<JcompSymbol> move_defs;
   private Set<BodyDeclaration> initial_items;

   ClassSplitter(S6Request.ClassSignature cs,TypeDeclaration td,Set<MethodDeclaration> mthds) {
      class_signature = cs;
      map_name = td.getName().getFullyQualifiedName();
      for_class = td;
      move_items = new HashSet<BodyDeclaration>(mthds);
      initial_items = new HashSet<BodyDeclaration>(mthds);
      move_defs = new HashSet<JcompSymbol>();
      for (MethodDeclaration md : mthds) {
         JcompSymbol js = JavaAst.getDefinition(md);
         if (js != null) move_defs.add(js);
       }
    }

   Collection<BodyDeclaration> getItems() {
      return new ArrayList<BodyDeclaration>(move_items);
    }
   Collection<JcompSymbol> getDefinedItems() {
      return new ArrayList<JcompSymbol>(move_defs);
    }
   JcompType getBaseType() {
      JcompSymbol js = JavaAst.getDefinition(for_class);
      JcompType jt = js.getClassType();
      JcompType jt1 = JavaAst.getJavaType(for_class);
      if (jt == null) jt = jt1;
      return jt;
    }

   boolean addToMove(BodyDeclaration bd) {
      if (!move_items.add(bd)) return false;
      JcompSymbol js = JavaAst.getDefinition(bd);
      if (js != null) move_defs.add(js);
      return true;
    }

   @Override protected String getSpecificsName()	{ return map_name; }
   TypeDeclaration getTypeDeclaration() 	{ return for_class; }

   @SuppressWarnings("unchecked")
   void rewriteTree(ASTNode orig,ASTRewrite rw) {
      if (orig == for_class) {
         AST ast = orig.getAST();
         String cnm = for_class.getName().getIdentifier();
         cnm += "S6Copy";
         SimpleName snm = JavaAst.getSimpleName(ast,cnm);
         TypeDeclaration td = ast.newTypeDeclaration();
         td.setName(snm);
         td.setInterface(false);
         List<Object> nmods = td.modifiers();
         nmods.add(ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
         
         for (String s : class_signature.getInterfaces()) {
            Name nm = JavaAst.getQualifiedName(ast,s);
            Type st = ast.newSimpleType(nm);
            td.superInterfaceTypes().add(st);
          }
         for (Object o : for_class.bodyDeclarations()) {
            BodyDeclaration bd = (BodyDeclaration) o;
            if (move_items.contains(bd)) {
               BodyDeclaration bd1 = (BodyDeclaration) ASTNode.copySubtree(ast,bd);
               td.bodyDeclarations().add(bd1);
             }
          }
         ASTNode par = orig.getParent();
         ListRewrite lrw = rw.getListRewrite(par,(ChildListPropertyDescriptor) orig.getLocationInParent());
         lrw.insertAfter(td,orig,null);
         ListRewrite lrw1 = rw.getListRewrite(orig,TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
         for (BodyDeclaration bd : initial_items) {
            lrw1.remove(bd,null);
          }
       }
    }

}	// end of subclass ClassSplitter



}	// end of class TransformSplitClass



/* end of TransformSplitClass.java */
