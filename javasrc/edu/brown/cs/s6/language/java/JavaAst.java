/********************************************************************************/
/*										*/
/*		JavaAst.java							*/
/*										*/
/*	Auxilliary methods for using ASTs in S6 				*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/language/java/JavaAst.java,v 1.19 2016/10/01 01:57:50 spr Exp $ */


/*********************************************************************************
 *
 * $Log: JavaAst.java,v $
 * Revision 1.19  2016/10/01 01:57:50  spr
 * Fix up transforms for framework search
 *
 * Revision 1.18  2016/07/22 13:31:06  spr
 * Fixups for framework search.
 *
 * Revision 1.17  2016/07/18 23:05:25  spr
 * Update transforms for applications and UI.
 *
 * Revision 1.16  2015/12/23 15:45:09  spr
 * Minor fixes.
 *
 * Revision 1.15  2015/09/23 17:54:52  spr
 * Version to handle andriod UI applications.
 *
 * Revision 1.14  2015/02/14 19:40:18  spr
 * Add test case generation.
 *
 * Revision 1.13  2014/08/29 15:16:08  spr
 * Updates for suise, testcases.
 *
 * Revision 1.12  2014/02/26 14:06:42  spr
 * Add transformations for user interfaces.
 *
 * Revision 1.11  2013/09/13 20:33:03  spr
 * Add calls for UI search.
 *
 * Revision 1.10  2013-05-09 12:26:19  spr
 * Minor changes to start ui fixups.
 *
 * Revision 1.9  2012-07-20 22:15:20  spr
 * New transforms and resolution for UI search
 *
 * Revision 1.8  2012-06-20 12:21:32  spr
 * Initial fixes for UI search
 *
 * Revision 1.7  2012-06-11 14:07:48  spr
 * add framework search; fix bugs
 *
 * Revision 1.6  2009-09-18 01:41:35  spr
 * Handle user testing.
 *
 * Revision 1.5  2009-05-12 22:28:48  spr
 * Fix ups to make user context work.
 *
 * Revision 1.4  2008-11-12 13:52:14  spr
 * Performance and bug updates.
 *
 * Revision 1.3  2008-08-28 00:32:56  spr
 * Next version of S6.	Lots of bug fixes, some new functionality.
 *
 * Revision 1.2  2008-07-17 13:46:45  spr
 * Bug fixes and speed ups.
 *
 * Revision 1.1.1.1  2008-06-03 12:59:23  spr
 * Initial version of S6
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.language.java;


import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import org.w3c.dom.Element;
import edu.brown.cs.ivy.xml.*;
import edu.brown.cs.cose.cosecommon.CoseResource;
import edu.brown.cs.cose.cosecommon.CoseSource;
import edu.brown.cs.ivy.file.IvyLog;
import edu.brown.cs.ivy.jcomp.JcompAst;
import edu.brown.cs.ivy.jcomp.JcompSymbol;
import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.ivy.jcomp.JcompTyper;
import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Fragment;
import edu.brown.cs.s6.common.S6Request;



abstract class JavaAst extends JcompAst implements S6Constants, JavaConstants {



/********************************************************************************/
/*										*/
/*	Source methods								*/
/*										*/
/********************************************************************************/

static CoseSource getS6Source(ASTNode n)
{
   return (CoseSource) n.getProperty(PROP_S6_JAVA_SOURCE);
}



static void setS6Source(ASTNode n,CoseSource s)
{
   n.setProperty(PROP_JAVA_SOURCE,s);
}




/********************************************************************************/
/*										*/
/*	Request methods 							*/
/*										*/
/********************************************************************************/

static S6Request.Search getSearchRequest(ASTNode n)
{
   return (S6Request.Search) n.getProperty(PROP_S6_JAVA_REQUEST);
}



static void setSearchRequest(ASTNode n,S6Request.Search s)
{
   n.setProperty(PROP_S6_JAVA_REQUEST,s);
}


static JavaContext getS6Context(ASTNode n)
{
   n = n.getRoot();
   return (JavaContext) n.getProperty(PROP_S6_JAVA_CONTEXT);
}


static void setS6Context(ASTNode n,JavaContext ctx)
{
   n = n.getRoot();
   n.setProperty(PROP_S6_JAVA_CONTEXT,ctx);
}



/********************************************************************************/
/*										*/
/*	Methods for checking method signatures					*/
/*										*/
/********************************************************************************/

static boolean checkMethodSignature(MethodDeclaration md,S6Request.MethodSignature ms,
				       S6SignatureType styp)
{
   if (md == null) return false;

   if (styp.checkName()) {
      String nm = ms.getName();
      if (md.isConstructor()) {
	 if (!nm.equals("<init>")) return false;
       }
      else {
	 String mnm = md.getName().getIdentifier();
	 if (!nm.equals(mnm)) return false;
       }
    }

   if (styp.checkReturn()) {
      if (md.isConstructor()) {
	 if (!ms.getName().equals("<init>")) return false;
       }
      else {
	 Type rt = md.getReturnType2();
	 if (!compareTypes(ms.getReturnTypeName(),rt)) return false;
       }
    }

   if (styp.checkParameters()) {
      List<String> pnms = ms.getParameterTypeNames();
      List<?> pls = md.parameters();
      if (pnms.size() != pls.size()) return false;
      int i = 0;
      for (Iterator<?> it = pls.iterator(); it.hasNext(); ) {
	 SingleVariableDeclaration svd = (SingleVariableDeclaration) it.next();
	 JcompSymbol js = JavaAst.getDefinition(svd);
	 if (js == null) return false;
	 JcompType jt = js.getType();
	 if (!compareTypes(pnms.get(i),jt)) return false;
	 ++i;
       }
    }

   if (styp.checkExceptions() && ms.useExceptions()) {
      List<String> enms = ms.getExceptionTypeNames();
      List<?> els = md.thrownExceptionTypes();
      if (enms.size() != els.size()) return false;
      int i = 0;
      for (Iterator<?> it = els.iterator(); it.hasNext(); ) {
	 Name n = (Name) it.next();
	 if (!compareTypes(enms.get(i),n)) return false;
	 ++i;
       }
    }

   if (styp.checkMods()) {
      int mds = md.getModifiers();
      if (ms.isStatic()) {
	 if (!Modifier.isStatic(mds)) return false;
       }
      if (!ms.isAbstract()) {
	 if (Modifier.isAbstract(mds)) return false;
       }
    }

   if (styp.checkDoesTest()) {
      int mds = md.getModifiers();
      if (md.getBody().statements().isEmpty()) return false;
      if (!ms.isStatic() && Modifier.isStatic(mds)) return false;
    }

   return true;
}



private static boolean compareTypes(String nm,ASTNode t)
{
   String tnm = getJavaTypeName(t);

   if (nm == null && tnm == null) return true;
   else if (nm == null || tnm == null) return false;

   if (tnm.equals(nm)) return true;

   int idx = tnm.indexOf("<");
   if (idx >= 0) {
      String xtnm = tnm.substring(0,idx);
      if (xtnm.equals(nm)) return true;
    }

   return false;
}



private static boolean compareTypes(String nm,JcompType jt)
{
   String tnm = jt.getName();

   if (nm == null && tnm == null) return true;
   else if (nm == null || tnm == null) return false;

   return tnm.equals(nm);
}



/********************************************************************************/
/*										*/
/*	Methods for checking field signatures					*/
/*										*/
/********************************************************************************/

static boolean checkFieldSignature(VariableDeclarationFragment vdf,
				      S6Request.FieldSignature fs,S6SignatureType styp)
{
   if (styp.checkName()) {
      String nm = fs.getName();
      if (nm != null) {
	 String mnm = vdf.getName().getIdentifier();
	 if (!nm.equals(mnm)) return false;
       }
    }

   FieldDeclaration fd = (FieldDeclaration) vdf.getParent();
   if (styp.checkType()) {
      Type ft = fd.getType();
      if (!compareTypes(fs.getTypeName(),ft)) return false;
    }

   if (styp.checkMods()) {
      int mds = fd.getModifiers();
      if (fs.isStatic() != Modifier.isStatic(mds)) return false;
    }

   return true;
}



/********************************************************************************/
/*										*/
/*	Methods for checking class signature					*/
/*										*/
/********************************************************************************/

static boolean checkTypeSignature(AbstractTypeDeclaration td,S6Request.ClassSignature cs,
				     S6SignatureType styp,String pkg)
{
   if (td == null || cs == null) return false;

   if (td instanceof TypeDeclaration) {
      TypeDeclaration ttd = (TypeDeclaration) td;
      if (!cs.isInterface() && ttd.isInterface()) return false;
      else if (cs.isInterface()) {
	 if (!ttd.isInterface() && !Modifier.isAbstract(ttd.getModifiers())) {
	    return false;
	  }
       }
    }

   if (td.getName().getFullyQualifiedName().contains("$") && !styp.checkExtractable()) {
      if (!Modifier.isStatic(td.getModifiers())) return false;
    }
   if (Modifier.isAbstract(td.getModifiers())) {
      if (!cs.isInterface()) return false;
    }

   if (styp.checkName()) {
      String nm1 = td.getName().getIdentifier();
      String nm2 = cs.getName();
      if (!nm1.equals(nm2)) {
	 return false;
       }
    }

   JcompTyper jtr = JavaAst.getTyper(td);

   String sup = cs.getSuperClass();
   if (sup != null && pkg != null && sup.startsWith(pkg) && styp.ignoreSuper()) sup = null;
   if (sup != null) {
      Type tsup = null;
      if (td instanceof TypeDeclaration) {
	 tsup = ((TypeDeclaration) td).getSuperclassType();
	 JcompType jt = JavaAst.getJavaType(tsup);
	 boolean compat = false;
	 if (jtr != null && jt != null) {
	    JcompType uty = jtr.findSystemType(sup);
	    if (uty != null) {
	       if (jt.isCompatibleWith(uty)) {
		  compat = true;
		}
	     }
	  }
	 if (!compat) {
	    if (jt == null || !jt.getName().equals(sup)) return false;
	  }
       }
      else return false;
    }

   List<String> ifcs = cs.getInterfaces();
   if (pkg != null && styp.noMethods()) {
      for (Iterator<String> it = ifcs.iterator(); it.hasNext(); ) {
	 String finm = it.next();
	 if (finm.startsWith(pkg)) it.remove();
       }
    }

   List<?> ifts = null;
   JcompType supty = null;
   if (td instanceof TypeDeclaration) {
      TypeDeclaration ttd = (TypeDeclaration) td;
      ifts = ttd.superInterfaceTypes();
      Type tsup = ttd.getSuperclassType();
      supty = JavaAst.getJavaType(tsup);
    }
   if (ifcs == null || ifcs.size() == 0) {
      // Allow the class to implement other things if it wants to: possibly only with FULLCLASS
      // if (ifts != null && ifts.size() > 0) return false;
    }
   else {
      for (String utnm : ifcs) {
	 boolean fnd = false;
	 if (jtr != null) {
	    JcompType uty = jtr.findSystemType(utnm);
	    if (uty != null) {
	       if (supty != null && supty.isCompatibleWith(uty)) fnd = true;
	       if (ifts != null) {
		  for (Iterator<?> it = ifts.iterator(); it.hasNext(); ) {
		     Type ift = (Type) it.next();
		     JcompType ity = JavaAst.getJavaType(ift);
		     if (ity.isCompatibleWith(uty)) fnd = true;
		   }
		}
	     }
	  }
	 if (!fnd) return false;
       }
      // if (ifts == null) return false;
      // if (ifts.size() != ifcs.size()) return false;
      // for (Iterator<?> it = ifts.iterator(); it.hasNext(); ) {
	 // Type ift = (Type) it.next();
	 // JcompType jt = JavaAst.getJcompType(ift);
	 // JcompType jt = JavaAst.getJcompType(ift);
	 // if (jt == null) return false;
	 // if (!ifcs.contains(jt.getName())) return false;
       // }
    }

   if (cs.isMain()) {
      boolean fnd = false;
      for (Object o : td.bodyDeclarations()) {
	 if (o instanceof MethodDeclaration) {
	    MethodDeclaration md = (MethodDeclaration) o;
	    if (md.getName().getIdentifier().equals("main") &&
		  md.getReturnType2() != null &&
		  md.getReturnType2().isPrimitiveType() &&
		  Modifier.isStatic(md.getModifiers()) &&
		  Modifier.isPublic(md.getModifiers()) &&
		  md.parameters().size() == 1) {
	       PrimitiveType pt = (PrimitiveType) md.getReturnType2();
	       if (pt.getPrimitiveTypeCode() == PrimitiveType.VOID) {
		  Object o1 = md.parameters().get(0);
		  SingleVariableDeclaration svd = (SingleVariableDeclaration) o1;
		  Type t = svd.getType();
		  String tnm = t.toString();
		  if (svd.getExtraDimensions() > 0) tnm += "[]";
		  if (tnm.equals("java.lang.String[]") || tnm.equals("String[]"))
		     fnd = true;
		}
	     }
	  }
       }
      if (!fnd) return false;
    }

   if (styp.noMethods()) return true;

   Set<ASTNode> used = new HashSet<ASTNode>();

   for (S6Request.MethodSignature ms : cs.getMethods()) {
      boolean fnd = false;
      for (Iterator<?> it = td.bodyDeclarations().iterator(); it.hasNext(); ) {
	 ASTNode cnod = (ASTNode) it.next();
	 if (used.contains(cnod)) continue;
	 if (cnod instanceof MethodDeclaration) {
	    MethodDeclaration md = (MethodDeclaration) cnod;
	    if (checkMethodSignature(md,ms,styp)) {
	       fnd = true;
	       used.add(cnod);
	       break;
	     }
	  }
       }
      if (!fnd)
	 return false;
    }

   for (S6Request.FieldSignature fs : cs.getFields()) {
      boolean fnd = false;
      for (Iterator<?> it = td.bodyDeclarations().iterator(); it.hasNext(); ) {
	 ASTNode cnod = (ASTNode) it.next();
	 if (used.contains(cnod)) continue;
	 if (cnod instanceof FieldDeclaration) {
	    FieldDeclaration fd = (FieldDeclaration) cnod;
	    for (Object o : fd.fragments()) {
	       VariableDeclarationFragment vdf = (VariableDeclarationFragment) o;
	       if (used.contains(vdf)) continue;
	       if (checkFieldSignature(vdf,fs,styp)) {
		  fnd = true;
		  used.add(vdf);
		  break;
		}
	     }
	    if (fnd) break;
	  }
       }
      if (!fnd)
	 return false;
    }

   if (styp.checkDoesTest()) {
      TestVisitor tv = new TestVisitor();
      td.accept(tv);
      if (!tv.doesTest())
	 return false;
    }

   return true;
}




private static class TestVisitor extends ASTVisitor {

   private boolean does_test;

   TestVisitor() {
      does_test = false;
    }

   boolean doesTest()			{ return does_test; }

   @Override public boolean preVisit2(ASTNode n) {
      return !does_test;
    }

   @Override public boolean visit(MethodDeclaration md) {
      String nm = md.getName().getIdentifier();
      if (nm.startsWith("test")) does_test = true;
      return true;
    }

   @Override public void endVisit(MarkerAnnotation n) {
      Name nm = n.getTypeName();
      String x = nm.getFullyQualifiedName();
      if (x.endsWith("Test")) does_test = true;
    }

   @Override public void endVisit(NormalAnnotation n) {
      Name nm = n.getTypeName();
      String x = nm.getFullyQualifiedName();
      if (x.endsWith("Test")) does_test = true;
    }

}




/********************************************************************************/
/*										*/
/*	Methods for checking package signature					*/
/*										*/
/********************************************************************************/

static boolean checkPackageSignature(CompilationUnit cu,S6Request.PackageSignature ps,
      S6SignatureType typ,S6Fragment frag)
{
   PackageDeclaration pd = cu.getPackage();
   if (pd == null) {
      return ps.getName() == null;
    }
   if (ps.getName() == null) return false;
   String pnm = pd.getName().getFullyQualifiedName();
   if (!ps.getName().equals(pnm)) return false;

   if (typ.noClasses()) return true;

   List<AbstractTypeDeclaration> types = new ArrayList<AbstractTypeDeclaration>();
   for (Object o : cu.types()) {
      if (o instanceof AbstractTypeDeclaration) {
	 AbstractTypeDeclaration td = (AbstractTypeDeclaration) o;
	 types.add(td);
       }
    }
   List<S6Request.ClassSignature> sgns = new LinkedList<S6Request.ClassSignature>();
   sgns.addAll(ps.getClasses());

   if (!checkPackage(types,sgns,typ,ps.getName()))
      return false;

   if (typ.checkUsage()) {
      if (!checkClassUsage(cu,ps)) return false;
    }

   if (typ.checkUITypes()) {
      if (ps instanceof S6Request.UISignature) {
	 S6Request.UISignature uis = (S6Request.UISignature) ps;
	 if (!checkUITypes(cu,uis,frag)) return false;
       }
    }

   return true;
}


private static boolean checkPackage(List<AbstractTypeDeclaration> typs,
      List<S6Request.ClassSignature> sgns,S6SignatureType typ,String pkg)
{
   if (sgns.size() == 0) return true;
   S6Request.ClassSignature csgn = sgns.get(0);
   sgns.remove(0);

   for (AbstractTypeDeclaration td : typs) {
      String tnm = td.getName().getFullyQualifiedName();
      // JcompType jt = JavaAst.getJavaType(td);
      // tnm = jt.getName();
      if (!tnm.equals(csgn.getName())) continue;
      if (checkTypeSignature(td,csgn,typ,pkg)) {
	 List<AbstractTypeDeclaration> todo = new ArrayList<AbstractTypeDeclaration>(typs);
	 todo.remove(td);
	 if (checkPackage(todo,sgns,typ,pkg)) return true;
       }
    }

   sgns.add(0,csgn);
   return false;
}



private static boolean checkClassUsage(CompilationUnit cu,S6Request.PackageSignature ps)
{
   UsageVisitor uv = new UsageVisitor(ps.getName());
   cu.accept(uv);
   Collection<String> used = uv.getTypesUsed();
   for (S6Request.ClassSignature cg : ps.getClasses()) {
      String nm = cg.getName();
      if (!cg.isMain() && !used.contains(nm)) return false;
    }
   return true;
}



private static class UsageVisitor extends ASTVisitor {

   private String package_name;
   private Set<String> items_used;
   private TypeDeclaration current_class;
   private JcompType current_type;

   UsageVisitor(String pnm) {
      package_name = pnm;
      items_used = new HashSet<String>();
      current_class = null;
      current_type = null;
    }

   Collection<String> getTypesUsed()		{ return items_used; }

   @Override public void preVisit(ASTNode n) {
      if (n instanceof TypeDeclaration && current_class == null) {
	 current_class = (TypeDeclaration) n;
	 current_type = getJavaType(n);
       }
    }

   @Override public void postVisit(ASTNode n) {
      JcompSymbol js = getReference(n);
      if (js != null) {
         JcompType jt = js.getType();
         if (jt != null && jt.isClassType() && !jt.isBinaryType() && jt != current_type) {
            String cnm = jt.getName();
            if (package_name != null) {
               if (cnm.startsWith(package_name)) {
        	  int idx = cnm.lastIndexOf(".");
        	  cnm = cnm.substring(idx+1);
        	}
             }
            int idx1 = cnm.indexOf("$");
            if (idx1 >= 0) cnm = cnm.substring(0,idx1);
            items_used.add(cnm);
          }
       }
      if (n == current_class) current_class = null;
    }

}	// end of inner class UsageVisitor




/********************************************************************************/
/*										*/
/*	Methods for checking UI signature types 				*/
/*										*/
/********************************************************************************/

static boolean checkUITypes(CompilationUnit cu,S6Request.UISignature us,S6Fragment frag)
{
   UIUsageVisitor uuv = new UIUsageVisitor();
   cu.accept(uuv);

   if (frag != null && frag.getResources() != null) {
      for (CoseResource rsrc : frag.getResources()) {
	 if (rsrc.getPathName().endsWith(".xml") &&
	       rsrc.getPathName().contains("layout")) {
	    byte [] cnts = rsrc.getContents();
	    String xmls = null;
	    try {
	       xmls = new String(cnts,"UTF-8");
	     }
	    catch (UnsupportedEncodingException e) {
	       xmls = new String(cnts);
	     }
	    try {
	       Element xml = IvyXml.convertStringToXml(xmls);
	       uuv.addAndroidTypes(xml);
	     }
	    catch (Throwable t) {
	       return false;
	     }
	  }
       }
    }

   boolean fg = checkUITypes(us.getHierarchy(),uuv.getTypesUsed());

   if (fg) {
      // IvyLog.logI("JAVA","UICHECK PASSED: " + cu);
    }
   else {
      IvyLog.logI("JAVA","UICHECK FAILED: " + cu);
    }


   return fg;
}



private static final Set<String> SIMPLE_TYPES;
static {
   SIMPLE_TYPES = new HashSet<String>();
   SIMPLE_TYPES.add("javax.swing.JLabel");
   SIMPLE_TYPES.add("java.awt.Label");
   SIMPLE_TYPES.add("android.view.TextView");
}


private static boolean checkUITypes(S6Request.UIComponent c,Set<String> types)
{
   if (c.getTypes() != null) {
      boolean fg = false;
      boolean crit = false;
      for (String s : c.getTypes()) {
	 if (types.contains(s)) {
	    fg = true;
	  }
	 if (!SIMPLE_TYPES.contains(s)) crit = true;
       }
      if (!fg && crit) {
         StringBuffer buf = new StringBuffer();
	 for (String s : c.getTypes()) {
            buf.append(" ");
            buf.append(s);
          }
         IvyLog.logE("JAVA","UICHECK FAILED: " + buf.toString());
	 return false;
       }
    }

   if (c.getChildren() != null) {
      for (S6Request.UIComponent cc : c.getChildren()) {
	 if (!checkUITypes(cc,types)) return false;
       }
    }

   return true;
}




private static class UIUsageVisitor extends ASTVisitor {

   private Set<String> items_used;
   private JcompTyper jcomp_typer;
   private boolean have_activity;

   UIUsageVisitor() {
      items_used = new HashSet<String>();
      jcomp_typer = null;
      have_activity = false;
    }

   Set<String> getTypesUsed()			{ return items_used; }

   @Override public void postVisit(ASTNode n) {
      if (jcomp_typer == null) jcomp_typer = JavaAst.getTyper(n);
      JcompSymbol js = getReference(n);
      if (js != null) addType(js.getType());
      JcompType jt = JavaAst.getJavaType(n);
      if (jt != null) addType(jt);
      jt = JavaAst.getExprType(n);
      if (jt != null) addType(jt);
    }

   @Override public boolean visit(TypeDeclaration n) {
      Type t = n.getSuperclassType();
      if (t != null) {
	 JcompType jt = getJavaType(t);
	 if (jt.getName().contains("Activity")) have_activity = true;
	 addType(jt);
       }
      for (Object o : n.superInterfaceTypes()) {
	 addType(getJavaType((ASTNode) o));
       }

      return true;
    }

   @Override public boolean visit(ImportDeclaration d) {
      return false;
    }

   void addAndroidTypes(Element xml) {
      if (!have_activity || xml == null) return;
      String nm = xml.getNodeName();
      String tnm = "android.widget." + nm;
      if (jcomp_typer == null) return;
      JcompType xtyp = jcomp_typer.findSystemType(tnm);
      if (xtyp != null) addType(xtyp);
      for (Element cxml : IvyXml.children(xml)) {
	 addAndroidTypes(cxml);
       }
    }

   private void addType(JcompType jt) {
      if (jt == null) return;
      if (!jt.isClassType()) return;
      if (!jt.isBinaryType()) return;
      String nm = jt.getName();
      if (items_used.contains(nm)) return;
      items_used.add(nm);
      addType(jt.getSuperType());
      if (jt.getInterfaces() != null) {
         for (JcompType jt1 : jt.getInterfaces()) addType(jt1);
         for (JcompType jt1 : jt.getInterfaces()) addType(jt1);
       }
    }

}	// end of inner class UIUsageVisitor




/********************************************************************************/
/*										*/
/*	Map package names							*/
/*										*/
/********************************************************************************/

static void mapPackageNames(ASTNode n,String from,String to)
{
   PackageNameMapper pnm = new PackageNameMapper(from,to);
   n.accept(pnm);
}


private static class PackageNameMapper extends ASTVisitor {

   private String from_name;
   private String to_name;

   PackageNameMapper(String frm,String to) {
      from_name = frm;
      to_name = to;
    }

   @Override public void endVisit(QualifiedName n) {
      String qnm = n.getQualifier().getFullyQualifiedName();
      // if (qnm.startsWith("spr.sampler")) {
	 // System.err.println("PACKAGE WORK ON " + n + " " + from_name);
       // }
      if (qnm.equals(from_name)) {
	 AST ast = n.getAST();
	 Name nqn = getQualifiedName(ast,to_name);
	 n.setQualifier(nqn);
	 // System.err.println("UPDATE TO " + n);
       }
    }

}	// end of inner class PackageNameMapper



}	// end of abstract class JavaAst




/* end of JavaAst.java */
