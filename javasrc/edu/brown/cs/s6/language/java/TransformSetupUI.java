/********************************************************************************/
/*										*/
/*		TransformSetupUI.java						*/
/*										*/
/*	Transformation that handles UI transform setup				*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/language/java/TransformSetupUI.java,v 1.8 2015/12/23 15:45:10 spr Exp $ */


/*********************************************************************************
 *
 * $Log: TransformSetupUI.java,v $
 * Revision 1.8  2015/12/23 15:45:10  spr
 * Minor fixes.
 *
 * Revision 1.7  2015/09/23 17:54:54  spr
 * Version to handle andriod UI applications.
 *
 * Revision 1.6  2014/08/29 15:16:10  spr
 * Updates for suise, testcases.
 *
 * Revision 1.5  2014/02/26 14:06:46  spr
 * Add transformations for user interfaces.
 *
 * Revision 1.4  2013/09/13 20:33:05  spr
 * Add calls for UI search.
 *
 * Revision 1.3  2013-05-09 12:26:22  spr
 * Minor changes to start ui fixups.
 *
 * Revision 1.2  2012-07-20 22:15:21  spr
 * New transforms and resolution for UI search
 *
 * Revision 1.1  2012-06-20 12:21:33  spr
 * Initial fixes for UI search
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.language.java;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import edu.brown.cs.ivy.jcomp.JcompScope;
import edu.brown.cs.ivy.jcomp.JcompSymbol;
import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.ivy.jcomp.JcompTyper;
import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6Solution;
import edu.brown.cs.s6.common.S6SolutionSet;



public class TransformSetupUI extends TransformJava implements S6Constants, JavaConstants {




/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private static final String UI_CLASS_NAME = "S6_UI_CLASS";




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public TransformSetupUI(String name)
{
   super(name);
}




/********************************************************************************/
/*										*/
/*	Handle package mappings 						*/
/*										*/
/********************************************************************************/

@Override
protected Collection<TreeMapper> findPackageMappings(S6SolutionSet sols,
      S6Solution sol,
      CompilationUnit cu,
      S6Request.PackageSignature psg)
{
   if (!JavaAst.checkPackageSignature(cu,psg,S6SignatureType.NO_CLASSES,sol.getFragment())) return null;

   // first check if we are done
   for (Object o : cu.types()) {
      AbstractTypeDeclaration atd = (AbstractTypeDeclaration) o;
      String id = atd.getName().getIdentifier();
      if (id.equals(UI_CLASS_NAME)) return null;
    }

   List<TreeMapper> rslt = new ArrayList<TreeMapper>();

   if (sols.getSearchType() == S6SearchType.ANDROIDUI) {
      rslt.add(new AndroidMapper());
      return rslt;
    }

   // first attempt to find classes extending Component with default constructor
   for (Object o : cu.types()) {
      if (!(o instanceof TypeDeclaration)) continue;
      TypeDeclaration td = (TypeDeclaration) o;
      List<MethodDeclaration> cnsta = isCandidateUIClass(td);
      List<MethodDeclaration> cnstb = isCandidateUIClassChanged(td);
      if (cnsta == null && cnstb == null) continue;
      if (cnsta != null) {
	 if (cnsta.size() == 0) {
	    addTransforms(td,null,false,rslt);
	  }
	 else {
	    for (MethodDeclaration md : cnsta) {
	       addTransforms(td,md,false,rslt);
	     }
	  }
       }
      if (cnstb != null) {
	 for (MethodDeclaration md : cnstb) {
	    addTransforms(td,md,true,rslt);
	  }
       }
    }

   // next find a class that has a accessor method for a Component
   for (Object o : cu.types()) {
      if (!(o instanceof TypeDeclaration)) continue;
      TypeDeclaration td = (TypeDeclaration) o;
      for (MethodDeclaration md :td.getMethods()) {
	 if (isCandidateUIAccessClass(td,md)) {
	    ClassMapper cm = new ClassMapper(td,md,null,false);
	    rslt.add(cm);
	  }
       }
    }

   if (rslt.size() == 0) return null;

   return rslt;
}



private void addTransforms(TypeDeclaration td,MethodDeclaration cnst,boolean rem,List<TreeMapper> rslt)
{
   ClassMapper cm = new ClassMapper(td,null,cnst,rem);
   rslt.add(cm);

   for (MethodDeclaration md : td.getMethods()) {
      if (isSetupMethod(md)) {
	 cm = new ClassMapper(td,md,cnst,rem);
	 rslt.add(cm);
       }
    }
}






/********************************************************************************/
/*										*/
/*	Candidate class handling						*/
/*										*/
/********************************************************************************/


List<MethodDeclaration> isCandidateUIClass(TypeDeclaration td)
{
   if (td.isInterface()) return null;
   JcompType jt = JavaAst.getJavaType(td);
   if (!checkUIClass(jt,td)) return null;
   if (Modifier.isAbstract(td.getModifiers())) return null;

   List<MethodDeclaration> rslt = new ArrayList<MethodDeclaration>();

   boolean havecnst = false;
   for (MethodDeclaration md : td.getMethods()) {
      if (md.isConstructor()) havecnst = true;
      if (isConstructorValid(md)) rslt.add(md);
    }
   if (havecnst && rslt.size() == 0) return null;

   return rslt;
}



private boolean isConstructorValid(MethodDeclaration md)
{
   if (!md.isConstructor()) return false;
   if (Modifier.isPrivate(md.getModifiers())) return false;
   // if (Modifier.isProtected(md.getModifiers())) return false;
   if (md.parameters().size() == 0) return true;

   for (Object o : md.parameters()) {
      SingleVariableDeclaration svd = (SingleVariableDeclaration) o;
      JcompType pjt = JavaAst.getJavaType(svd);
      if (pjt == null) return false;
      if (pjt.isPrimitiveType()) continue;
      JcompScope js = pjt.getScope();
      if (js == null) continue;
      boolean havedflt = false;
      for (JcompSymbol sym : js.getDefinedMethods()) {
	 if (sym.isConstructorSymbol()) {
	    MethodDeclaration ja = (MethodDeclaration) sym.getDefinitionNode();
	    if (ja != null) {
	       if (!Modifier.isPrivate(ja.getModifiers()) &&
		     !Modifier.isProtected(ja.getModifiers()) &&
		     ja.parameters().size() == 0)
		  havedflt = true;
	     }
	  }
       }
      if (!havedflt) return false;
    }

   return true;
}



List<MethodDeclaration> isCandidateUIClassChanged(TypeDeclaration td)
{
   if (td.isInterface()) return null;
   JcompType jt = JavaAst.getJavaType(td);
   if (!checkUIClass(jt,td)) return null;

   List<MethodDeclaration> rslt = new ArrayList<MethodDeclaration>();
   for (MethodDeclaration md : td.getMethods()) {
      if (!md.isConstructor()) continue;
      int psz = md.parameters().size();
      if (psz == 0) return null;
      if (Modifier.isPrivate(md.getModifiers())) continue;
      rslt.add(md);
    }

   if (rslt.isEmpty()) return null;

   return rslt;
}


boolean isCandidateUIAccessClass(TypeDeclaration td,MethodDeclaration md)
{
   if (td.isInterface()) return false;
   boolean topcom = false;
   JcompType baset = JavaAst.getJavaType(td);
   if (!td.isInterface() && checkUIClass(baset,td))
      topcom = true;

   if (md.parameters().size() > 0) return false;
   // if ((md.getModifiers() & Modifier.PRIVATE) != 0) return false;
   Type rt = md.getReturnType2();
   JcompType jt = JavaAst.getJavaType(rt);
   if (jt == null) return false;
   if (jt.isPrimitiveType()) return false;
   if (!checkUIClass(jt,td)) return false;
   if (!checkUIAccessClass(jt,td)) return false;
   if (topcom && Modifier.isPrivate(md.getModifiers())) return false;

   boolean cnstok = false;
   boolean havecnst = false;
   for (MethodDeclaration xmd : td.getMethods()) {
      if (!xmd.isConstructor()) continue;
      havecnst = true;
      if (xmd.parameters().size() == 0) cnstok = true;
    }
   if (havecnst && !cnstok) return false;

   return true;
}




private List<JcompType> ui_classes = null;
private List<JcompType> ui_access_classes = null;

private boolean checkUIClass(JcompType t1,ASTNode n)
{
   buildUIClasses(n);

   for (JcompType jt : ui_classes) {
      if (t1.isCompatibleWith(jt)) return true;
    }

   return false;
}



private boolean checkUIAccessClass(JcompType t1,ASTNode n)
{
   buildUIClasses(n);

   for (JcompType jt : ui_access_classes) {
      if (t1.isCompatibleWith(jt)) return true;
    }

   return false;
}



private synchronized void buildUIClasses(ASTNode n)
{
   if (ui_classes != null) return;

   JcompTyper typer = JavaAst.getTyper(n);

   ui_classes = new ArrayList<JcompType>();
   ui_access_classes = new ArrayList<JcompType>();

   JcompType t1 = typer.findSystemType("java.awt.Component");
   if (t1 != null) ui_classes.add(t1);
   t1 = typer.findSystemType("android.view.View");
   if (t1 != null) ui_classes.add(t1);

   t1 = typer.findSystemType("javax.swing.JPanel");
   if (t1 != null) ui_access_classes.add(t1);
   t1 = typer.findSystemType("java.awt.Window");
   if (t1 != null) ui_access_classes.add(t1);
}




/********************************************************************************/
/*										*/
/*	Check for utility method						*/
/*										*/
/********************************************************************************/

boolean isSetupMethod(MethodDeclaration md)
{
   if (md.isConstructor()) return false;
   // if (md.parameters().size() > 0) return false;

   CheckSetupMethod csm = new CheckSetupMethod();

   md.accept(csm);

   return csm.isSetupMethod();
}



private static class CheckSetupMethod extends ASTVisitor {

   private boolean is_setup;

   CheckSetupMethod() {
      is_setup = false;
    }

   boolean isSetupMethod()		{ return is_setup; }

   @Override public boolean visit(MethodInvocation mi) {
      Expression lhs = mi.getExpression();
      if (lhs == null || lhs.getNodeType() == ASTNode.THIS_EXPRESSION) {
	  String nm = mi.getName().getIdentifier();
	  if (nm.equals("setVisible") && mi.arguments().size() == 1) {
	     ASTNode an = (ASTNode) mi.arguments().get(0);
	     if (an.getNodeType() == ASTNode.BOOLEAN_LITERAL &&
		    ((BooleanLiteral) an).booleanValue()) {
		is_setup = true;
	      }
	   }
       }
      return true;
    }

   public boolean visit(AnonymousClassDeclaration t)	{ return false; }

}	// end of subclass CheckSetupMethod




/********************************************************************************/
/*										*/
/*	The mapper to create calling instance for ui for standard UI search	*/
/*										*/
/********************************************************************************/

private class ClassMapper extends TreeMapper {

   private String map_name;
   private TypeDeclaration use_type;
   private MethodDeclaration use_method;
   private MethodDeclaration fix_constructor;
   private boolean remove_params;
   private Set<JcompSymbol> rename_syms;	// record parameter symbols and
						// rename any instance to XXXXXXXX

   ClassMapper(TypeDeclaration td,MethodDeclaration md,MethodDeclaration cnst,boolean rem) {
      use_type = td;
      use_method = md;
      if (cnst != null && cnst.parameters().size() == 0) cnst = null;
      fix_constructor = cnst;
      remove_params = rem;
      map_name =  use_type.getName().getIdentifier();
      rename_syms = null;
    }

   @Override protected String getSpecificsName()	{ return map_name; }

   @Override void rewriteTree(ASTNode orig,ASTRewrite rw) {
      if (orig.getNodeType() == ASTNode.COMPILATION_UNIT) {
	 fixCompilationUnit((CompilationUnit) orig,rw);
       }
      else if (orig == fix_constructor && remove_params) {
	 fixConstructor((MethodDeclaration) orig,rw);
       }
      else if (rename_syms != null && orig instanceof SimpleName) {
	 JcompSymbol js = JavaAst.getReference(orig);
	 if (js != null && rename_syms.contains(js)) {
	    fixRemovedParameter((SimpleName) orig,js,rw);
	  }
       }
      else if (orig == use_method) {
	 if ((use_method.getModifiers() & Modifier.PRIVATE) != 0) {
	    ListRewrite lrw = rw.getListRewrite(orig,MethodDeclaration.MODIFIERS2_PROPERTY);
	    for (Object o : lrw.getOriginalList()) {
	       IExtendedModifier iem = (IExtendedModifier) o;
	       if (iem.isModifier()) {
		  Modifier md = (Modifier) iem;
		  if (md.isPrivate()) {
		     lrw.remove(md,null);
		     break;
		   }
		}
	     }
	    // need to remove private here
	  }
       }
    }

   @Override void preVisit(ASTNode n) {
      if (n == fix_constructor && remove_params) {
	 MethodDeclaration md = (MethodDeclaration) n;
	 if (md.parameters().size() > 0) {
	    rename_syms = new HashSet<JcompSymbol>();
	    for (Object o : md.parameters()) {
	       ASTNode nd = (ASTNode) o;
	       JcompSymbol js = JavaAst.getDefinition(nd);
	       if (js != null) rename_syms.add(js);
	     }
	  }
       }
    }


   @SuppressWarnings("unchecked")
   private void fixCompilationUnit(CompilationUnit orig,ASTRewrite rw)
   {
      AST ast = rw.getAST();
      TypeDeclaration td = ast.newTypeDeclaration();
      td.setInterface(false);
      td.setName(JavaAst.getSimpleName(ast,UI_CLASS_NAME));
      List<?> nmods = ast.newModifiers(Modifier.PUBLIC);
      td.modifiers().addAll(nmods);
      MethodDeclaration md = ast.newMethodDeclaration();
      td.bodyDeclarations().add(md);
      nmods = ast.newModifiers(Modifier.PUBLIC|Modifier.STATIC);
      md.modifiers().addAll(nmods);
      Name nm = JavaAst.getQualifiedName(ast,"java.awt.Component");
      Type tr = ast.newSimpleType(nm);
      md.setReturnType2(tr);
      SimpleName mn = JavaAst.getSimpleName(ast,"S6_UI");
      md.setName(mn);
      Block b1 = ast.newBlock();
      md.setBody(b1);

      TryStatement tryer = ast.newTryStatement();
      Block b = ast.newBlock();
      tryer.setBody(b);
      CatchClause cc = ast.newCatchClause();
      Block b2 = ast.newBlock();
      cc.setBody(b2);
      SingleVariableDeclaration tsvd = ast.newSingleVariableDeclaration();
      tsvd.setName(JavaAst.getSimpleName(ast,"tex"));
      tsvd.setType(ast.newSimpleType(JavaAst.getQualifiedName(ast,"java.lang.Throwable")));
      cc.setException(tsvd);
      ThrowStatement ts = ast.newThrowStatement();
      ClassInstanceCreation tcic = ast.newClassInstanceCreation();
      Name tnm = JavaAst.getQualifiedName(ast,"java.lang.Error");
      SimpleType tty = ast.newSimpleType(tnm);
      tcic.setType(tty);
      tcic.arguments().add(JavaAst.getSimpleName(ast,"tex"));
      ts.setExpression(tcic);
      b2.statements().add(ts);
      tryer.catchClauses().add(cc);

      b1.statements().add(tryer);

      boolean isstatic = false;
      boolean skipmethod = false;
      if (use_method != null) {
	 isstatic = Modifier.isStatic(use_method.getModifiers());
	 Type t = use_method.getReturnType2();
	 if (t == null || t.isPrimitiveType()) skipmethod = true;
	 else {
	    JcompType jt = JavaAst.getJavaType(t);
	    JcompTyper typer = JavaAst.getTyper(orig);
	    JcompType comp = typer.findSystemType("java.awt.Component");
	    if (jt == null || !jt.isCompatibleWith(comp)) skipmethod = true;
	  }
       }

      if (!isstatic) {
	 ClassInstanceCreation cic = ast.newClassInstanceCreation();
	 Type xn = ast.newSimpleType(JavaAst.getSimpleName(ast,use_type.getName().getIdentifier()));
	 cic.setType(xn);
	 if (fix_constructor != null && !remove_params) {
	    for (Object o : fix_constructor.parameters()) {
	       SingleVariableDeclaration svd = (SingleVariableDeclaration) o;
	       JcompType jt = JavaAst.getJavaType(svd);
	       Expression ex = jt.createDefaultValue(ast);
	       if (jt.isClassType()) {
		  ClassInstanceCreation pcic = ast.newClassInstanceCreation();
		  Type pxn = ast.newSimpleType(JavaAst.getQualifiedName(ast,jt.getName()));
		  pcic.setType(pxn);
		  ex = pcic;
		}
	       cic.arguments().add(ex);
	     }

	  }
	 VariableDeclarationFragment vdf = ast.newVariableDeclarationFragment();
	 vdf.setName(JavaAst.getSimpleName(ast,"temp"));
	 vdf.setInitializer(cic);
	 VariableDeclarationStatement vds = ast.newVariableDeclarationStatement(vdf);
	 xn = ast.newSimpleType(JavaAst.getSimpleName(ast,use_type.getName().getIdentifier()));
	 vds.setType(xn);
	 b.statements().add(vds);
       }

      // possibly call setup routine here

      ReturnStatement rs = ast.newReturnStatement();
      Name sn1 = JavaAst.getSimpleName(ast,"temp");

      if (use_method == null) {
	 rs.setExpression(sn1);
       }
      else {
	 MethodInvocation mi = ast.newMethodInvocation();
	 if (isstatic) {
	    Name nm1 = JavaAst.getSimpleName(ast,use_type.getName().getIdentifier());
	    mi.setExpression(nm1);
	  }
	 else {
	    mi.setExpression(sn1);
	  }

	 for (Object o : use_method.parameters()) {
	    SingleVariableDeclaration svd = (SingleVariableDeclaration) o;
	    Type pt = svd.getType();
	    JcompType jt = JavaAst.getJavaType(pt);
	    Expression ex = jt.createDefaultValue(ast);
	    mi.arguments().add(ex);
	  }

	 mi.setName(JavaAst.getSimpleName(ast,use_method.getName().getIdentifier()));
	 if (skipmethod) {
	    ExpressionStatement es = ast.newExpressionStatement(mi);
	    b.statements().add(es);
	    sn1 = JavaAst.getSimpleName(ast,"temp");
	    rs.setExpression(sn1);
	  }
	 else {
	    rs.setExpression(mi);
	  }
       }
      b.statements().add(rs);

      ListRewrite lrw = rw.getListRewrite(orig,CompilationUnit.TYPES_PROPERTY);
      lrw.insertLast(td,null);
    }

   private void fixConstructor(MethodDeclaration md,ASTRewrite rw) {
      if (md.parameters().size() > 0) {
	 ListRewrite lrw = rw.getListRewrite(md,MethodDeclaration.PARAMETERS_PROPERTY);
	 for (Object o : md.parameters()) {
	    ASTNode nd = (ASTNode) o;
	    lrw.remove(nd,null);
	  }
	 rename_syms = null;
       }

      if (Modifier.isPrivate(md.getModifiers()) || Modifier.isProtected(md.getModifiers())) {
	 ListRewrite lrw = rw.getListRewrite(md,MethodDeclaration.MODIFIERS2_PROPERTY);
	 for (Object o : md.modifiers()) {
	    IExtendedModifier iem = (IExtendedModifier) o;
	    if (iem.isAnnotation()) {
	       lrw.remove((ASTNode) o,null);
	     }
	    else if (iem.isModifier()) {
	       Modifier mod = (Modifier) iem;
	       if (mod.isPrivate() || mod.isProtected()) {
		  lrw.remove(mod,null);
		}
	     }
	  }
       }
    }

   private void fixRemovedParameter(SimpleName nm,JcompSymbol js,ASTRewrite rw) {
      JcompType jt = js.getType();
      Expression ex = null;
      if (nm.getParent().getNodeType() == ASTNode.ASSIGNMENT &&
	    ((Assignment) nm.getParent()).getLeftHandSide() == nm) {
	 ex = JavaAst.getSimpleName(rw.getAST(),"S6_DUMMY_NAME_PARAM");
       }
      else {
	 ex = jt.createDefaultValue(rw.getAST());
       }
      rw.replace(nm,ex,null);
    }

}    // end of inner class ClassMapper



/********************************************************************************/
/*										*/
/*	Transformation for Android UI Search					*/
/*										*/
/********************************************************************************/

private class AndroidMapper extends TreeMapper {


   AndroidMapper() {
    }

   @Override protected String getSpecificsName()	{ return "Android"; }

   @Override void rewriteTree(ASTNode orig,ASTRewrite rw) {
      if (orig.getNodeType() == ASTNode.COMPILATION_UNIT) {
	 fixCompilationUnit((CompilationUnit) orig,rw);
       }
    }

   @SuppressWarnings("unchecked")
   private void fixCompilationUnit(CompilationUnit orig,ASTRewrite rw)
   {
      AST ast = rw.getAST();
      TypeDeclaration td = ast.newTypeDeclaration();
      td.setInterface(false);
      td.setName(JavaAst.getSimpleName(ast,UI_CLASS_NAME));
      List<?> nmods = ast.newModifiers(Modifier.PUBLIC);
      td.modifiers().addAll(nmods);
      Name nm = JavaAst.getQualifiedName(ast,"edu.brown.cs.s6.runner.RunnerAndroid");
      Type tr = ast.newSimpleType(nm);
      td.setSuperclassType(tr);

      ListRewrite lrw = rw.getListRewrite(orig,CompilationUnit.TYPES_PROPERTY);
      lrw.insertLast(td,null);
    }

}	// end of inner class AndroidMapper

}	// end of class TransformSetupUI




/* end of TransformSetupUI.java */
