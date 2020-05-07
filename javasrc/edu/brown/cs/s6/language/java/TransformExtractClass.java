/********************************************************************************/
/*										*/
/*		TransformExtractClass.java					*/
/*										*/
/*	Transformation that handles extracting a class from inside to top level */
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/language/java/TransformExtractClass.java,v 1.5 2015/09/23 17:54:53 spr Exp $ */


/*********************************************************************************
 *
 * $Log: TransformExtractClass.java,v $
 * Revision 1.5  2015/09/23 17:54:53  spr
 * Version to handle andriod UI applications.
 *
 * Revision 1.4  2013/09/13 20:33:04  spr
 * Add calls for UI search.
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


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import edu.brown.cs.ivy.file.IvyLog;
import edu.brown.cs.ivy.jcomp.JcompSymbol;
import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6Solution;
import edu.brown.cs.s6.common.S6SolutionSet;



public class TransformExtractClass extends TransformJava implements S6Constants, JavaConstants {




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public TransformExtractClass(String name)
{
   super(name);
}



/********************************************************************************/
/*										*/
/*	Handle package renamings						*/
/*										*/
/********************************************************************************/

@Override protected Collection<TreeMapper> findPackageMappings(S6SolutionSet sols,S6Solution sol,
      CompilationUnit cu,S6Request.PackageSignature psg)
{
   JavaFragment fj = (JavaFragment) sol.getFragment();
   List<TreeMapper> rslt = new ArrayList<TreeMapper>();
   PackageDeclaration pd = cu.getPackage();
   if (pd == null) return null;
   String pnm = pd.getName().getFullyQualifiedName();
   if (!psg.getName().equals(pnm)) return null;

   for (Object o : cu.types()) {
      if (o instanceof TypeDeclaration) {
	 TypeDeclaration td = (TypeDeclaration) o;
	 Set<TypeDeclaration> itds = new HashSet<TypeDeclaration>();
	 for (TypeDeclaration itd : td.getTypes()) {
	    if (checkExtract(psg,itd)) {
	       itds.add(itd);
	       Set<TypeDeclaration> stds = new HashSet<TypeDeclaration>();
	       stds.add(itd);
	       ClassExtracter tm = extractClass(td,stds);
	       if (tm != null) {
                  tm.createTextMemo(fj);
		  rslt.add(tm);
		}
	     }
	  }
	 if (itds.size() > 1) {
	    ClassExtracter tm = extractClass(td,itds);
	    if (tm != null) {
               tm.createTextMemo(fj);
	       rslt.add(tm);
	     }
	  }
       }
    }

   if (rslt.size() == 0) return null;

   return rslt;
}


private boolean checkExtract(S6Request.PackageSignature ps,TypeDeclaration td)
{
   boolean fnd = false;
   for (S6Request.ClassSignature cs : ps.getClasses()) {
      if (td.getSuperclassType() != null || td.superInterfaceTypes().size() > 0) {
	 if (cs.getInterfaces().size() > 0 || cs.getSuperClass() != null) {
	    if (JavaAst.checkTypeSignature(td,cs,S6SignatureType.EXTRACTABLE,ps.getName()))
	       fnd = true;
	  }
       }
      else if (cs.getInterfaces().size() == 0 && cs.getSuperClass() == null) fnd = true;
    }

   return fnd;
}



private ClassExtracter extractClass(TypeDeclaration otd,Set<TypeDeclaration> itds)
{
   return new ClassExtracter(otd,itds);
}


private TypeDeclaration createCopy(AST ast,TypeDeclaration td,Map<Object,String> names,Map<JcompType,Boolean> inits)
{
   TypeDeclaration ntd = (TypeDeclaration) ASTNode.copySubtree(ast,td);
   for (Iterator<?> it = ntd.modifiers().iterator(); it.hasNext(); ) {
      IExtendedModifier iem = (IExtendedModifier) it.next();
      if (iem.isAnnotation()) it.remove();
      else if (iem.isModifier()) {
	 Modifier m = (Modifier) iem;
	 if (m.isFinal() || m.isProtected() || m.isPrivate() || m.isStatic()) it.remove();
       }
    }
   SimpleName nnm = JavaAst.getSimpleName(ast,names.get(td));
   ntd.setName(nnm);

   fixSubtree(td,ntd,names,inits,td);

   return ntd;
}



private void fixSubtree(ASTNode orig,ASTNode copy,Map<Object,String> names,Map<JcompType,Boolean> inits,
      TypeDeclaration td)
{
   for (Object o : orig.structuralPropertiesForType()) {
      StructuralPropertyDescriptor spd = (StructuralPropertyDescriptor) o;
      if (spd.isChildListProperty()) {
	 List<?> ls1 = (List<?>) orig.getStructuralProperty(spd);
	 List<?> ls2 = (List<?>) copy.getStructuralProperty(spd);
	 if (ls1.size() == ls2.size()) {
	    for (int i = 0; i < ls1.size(); ++i) {
	       fixSubtree((ASTNode) ls1.get(i),(ASTNode) ls2.get(i),names,inits,td);
	     }
	  }
       }
      else if (spd.isChildProperty()) {
	 ASTNode n1 = (ASTNode) orig.getStructuralProperty(spd);
	 ASTNode n2 = (ASTNode) copy.getStructuralProperty(spd);
	 if (n1 != null) fixSubtree(n1,n2,names,inits,td);
       }
    }

   if (copy.getNodeType() == ASTNode.SIMPLE_NAME) {
      JcompSymbol js = JavaAst.getReference(orig);
      if (js == null) js = JavaAst.getDefinition(orig);
      if (js != null) {
	 String newname = names.get(js);
	 if (newname != null) {
	    SimpleName sn = (SimpleName) copy;
	    sn.setIdentifier(newname);
	  }
	 else {
	    JcompType jt = js.getClassType();
	    if (jt == JavaAst.getJavaType(td)) {
	       // need to qualify expression here to use new field or class name
	     }
	  }
       }
    }
   else if (copy.getNodeType() == ASTNode.METHOD_INVOCATION) {
      MethodInvocation mi1 = (MethodInvocation) orig;
      // MethodInvocation mi2 = (MethodInvocation) copy;
      JcompSymbol js = JavaAst.getReference(mi1.getName());
      if (js != null && js.isConstructorSymbol()) {
	 JcompType jt = js.getClassType();
	 if (inits.get(jt) == Boolean.TRUE) {
	    // need to add argument to constructor call
	  }
       }
    }
   else if (copy.getNodeType() == ASTNode.THIS_EXPRESSION) {
      // need to replace this with the appropriate local variable
    }
   else if (copy.getNodeType() == ASTNode.METHOD_DECLARATION) {
      MethodDeclaration md1 = (MethodDeclaration) orig;
      // MethodDeclaration md2 = (MethodDeclaration) copy;
      JcompSymbol js = JavaAst.getDefinition(md1);
      if (js != null && js.isConstructorSymbol()) {
	 JcompType jt = js.getClassType();
	 if (inits.get(jt) == Boolean.TRUE) {
	    // need to add argument to constructor definition
	  }
       }
    }
   else if (orig == td) {
      JcompType jt = JavaAst.getJavaType(orig);
      if (inits.get(jt) == Boolean.TRUE) {
	 // add new local variable for this
       }
      // if there is no constructor, add a new constructor
    }
}




/********************************************************************************/
/*										*/
/*	Class to handle name mappings						*/
/*										*/
/********************************************************************************/

private class ClassExtracter extends TreeMapper {

   private TypeDeclaration outer_type;
   private Set<TypeDeclaration> extract_types;
   private Map<JcompType,Boolean> init_types;
   private Map<Object,String> sym_mapping;
   private boolean ignore_tree;

   ClassExtracter(TypeDeclaration otd,Set<TypeDeclaration> itds) {
      outer_type = otd;
      extract_types = itds;
      sym_mapping = new HashMap<>();
      ignore_tree = false;
      init_types = new HashMap<>();
      for (TypeDeclaration td : itds) {
	 JcompSymbol js = JavaAst.getDefinition(td);
	 JcompType jt = JavaAst.getJavaType(td);
	 boolean stat = Modifier.isStatic(td.getModifiers());
	 init_types.put(jt,!stat);
	 String nm = S6_NAME_PREFIX + js.getName();
	 sym_mapping.put(js,nm);
	 sym_mapping.put(jt,nm);
	 sym_mapping.put(td,nm);
	 sym_mapping.put(js.getName(),nm);
       }
    }

   void createTextMemo(JavaFragment f) {
      super.createTextMemo(f,getName());
      outer_type = null;
      extract_types = null;
      init_types = null;
      sym_mapping = null;
    }

   void preVisit(ASTNode n) {
      if (extract_types.contains(n)) ignore_tree = true;
    }

   void rewriteTree(ASTNode orig,ASTRewrite rw) {
      if (orig == outer_type) {
	 ListRewrite lw = rw.getListRewrite(orig,TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
	 for (TypeDeclaration td : extract_types) {
	    lw.remove(td,null);
	  }
	 ListRewrite lw1 = rw.getListRewrite(orig.getParent(),CompilationUnit.TYPES_PROPERTY);
	 for (TypeDeclaration td : extract_types) {
	    lw1.insertAfter(createCopy(orig.getAST(),td,sym_mapping,init_types),orig,null);
	  }
	 return;
       }
      if (extract_types.contains(orig)) {
	 ignore_tree = false;
	 return;
       }
      if (ignore_tree) return;

      JcompSymbol js = JavaAst.getDefinition(orig);

      if (js != null && orig.getNodeType() == ASTNode.QUALIFIED_NAME ||
	    orig.getNodeType() == ASTNode.QUALIFIED_TYPE) {
	 System.err.println("QUAL: " + orig + " " + js);
       }
	
      if (js != null) {
	 String newname = sym_mapping.get(js);
	 if (newname != null) {
	    rewriteName(orig,rw,newname);
	  }
       }
      js = JavaAst.getReference(orig);
      if (js != null) {
	 String newname = sym_mapping.get(js);
	 if (newname == null && js.isConstructorSymbol()) {
	    JcompType jt = js.getClassType();
	    newname = sym_mapping.get(jt);
	    if (newname != null && init_types.get(jt) == Boolean.TRUE) {
	       fixConstructorCall(orig,rw);
	     }
	  }
	 if (newname != null) {
	    rewriteName(orig,rw,newname);
	  }
       }
    }

   private void fixConstructorCall(ASTNode n,ASTRewrite rw) {
    }

   private void rewriteName(ASTNode nd,ASTRewrite rw,String name) {
      if (nd instanceof SimpleName) {
         try {
            rw.set(nd,SimpleName.IDENTIFIER_PROPERTY,name,null);
          }
         catch (IllegalArgumentException e) {
            IvyLog.logE("JAVA","Transform name problem with new name " + name + ": " + e);
          }
       }
    }

}	// end of subclass ClassExtracter



}	// end of class TransformExtractClass



/* end of TransformExtractClass.java */
