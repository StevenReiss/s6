/********************************************************************************/
/*										*/
/*		TransformFrameworkNames.java					*/
/*										*/
/*	Transform names of class/methods/fields for a framework search		*/
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import edu.brown.cs.ivy.file.IvyLog;
import edu.brown.cs.ivy.jcomp.JcompAst;
import edu.brown.cs.ivy.jcomp.JcompSymbol;
import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.ivy.jcomp.JcompTyper;
import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6Solution;
import edu.brown.cs.s6.common.S6SolutionSet;

public class TransformFrameworkNames extends TransformJava implements S6Constants, JavaConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private static AtomicInteger	name_counter = new AtomicInteger();



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public TransformFrameworkNames(String name)
{
   super(name);
}



/********************************************************************************/
/*										*/
/*	Package mapping interface						*/
/*										*/
/********************************************************************************/

@Override protected Collection<TreeMapper> findPackageMappings(S6SolutionSet sols,
      S6Solution sol,CompilationUnit cu,S6Request.PackageSignature psg)
{
   UsageMappings um = getDependencies(cu);
   List<NameMapper> rslt = new ArrayList<NameMapper>();
   List<TreeMapper> finalresult = new ArrayList<TreeMapper>();

   Map<S6Request.ClassSignature,List<TypeDeclaration>> worklist =
      new HashMap<S6Request.ClassSignature,List<TypeDeclaration>>();
   Set<TypeDeclaration> done = new HashSet<TypeDeclaration>();
   for (S6Request.ClassSignature csg : psg.getClasses()) {
      String nm = csg.getName();
      boolean fnd = false;
      for (Object o : cu.types()) {
	 if (o instanceof TypeDeclaration) {
	    TypeDeclaration td = (TypeDeclaration) o;
	    String tnm = td.getName().getIdentifier();
	    if (tnm.equals(nm)) {
	       done.add(td);
	       fnd = true;
	     }
	  }
       }
      if (!fnd) worklist.put(csg,new ArrayList<TypeDeclaration>());
    }
   if (worklist.size() == 0) return finalresult;

   for (Object o : cu.types()) {
      if (o instanceof TypeDeclaration) {
	 TypeDeclaration td = (TypeDeclaration) o;
	 if (done.contains(td)) continue;
	 for (Map.Entry<S6Request.ClassSignature,List<TypeDeclaration>> ent : worklist.entrySet()) {
	    S6Request.ClassSignature workon = ent.getKey();
	    if (!checkApplicable(workon,td,psg.getName())) continue;
	    JcompSymbol js = JavaAst.getDefinition(td);
	    if (js != null) {
	       ent.getValue().add(td);
	     }
	  }
       }
    }

   for (Map.Entry<S6Request.ClassSignature,List<TypeDeclaration>> ent : worklist.entrySet()) {
      if (ent.getValue().size() == 0) return finalresult;
    }
   List<S6Request.ClassSignature> todos = new LinkedList<S6Request.ClassSignature>(worklist.keySet());
   findClassMappings(todos,worklist,null,rslt,psg,um);

   // next, for each result mapping, extend that mappings with fields and methods
   for (NameMapper nm : rslt) {
      List<NameMapper> nrslt = addFieldMappings(nm,sols,sol,cu,psg);
      if (nrslt != null && nrslt.size() > 0) {
	 for (NameMapper nm1 : nrslt) {
	    List<NameMapper> mrslt = addMethodMappings(nm1,sols,sol,cu,psg);
	    if (mrslt != null) {
	       finalresult.addAll(mrslt);
	     }
	  }
       }
    }
   // finally remove those that aren't interconnected and set up to remove unneeded classes
   for (Iterator<TreeMapper> it = finalresult.iterator(); it.hasNext(); ) {
      NameMapper tm = (NameMapper) it.next();
      if (!checkDepends(psg,tm,um)) it.remove();
      else {
	 findDependentTypes(cu,tm);
       }
    }

   return finalresult;
}




/********************************************************************************/
/*										*/
/*	Only work for framework search						*/
/*										*/
/********************************************************************************/

protected boolean cheS6LocationhodForClass(S6SolutionFlag sols,S6Solution sol,
      TypeDeclaration td,S6Request.MethodSignature msg,MethodDeclaration md)
{
   return false;
}


protected boolean checkApplyClassForPackage(S6Solution sols,CompilationUnit cu,
      S6Request.ClassSignature csg,
      AbstractTypeDeclaration td)
{
   return false;
}



/********************************************************************************/
/*										*/
/*	Check initial applicability of a class					*/
/*										*/
/********************************************************************************/

private boolean checkApplicable(S6Request.ClassSignature csg,TypeDeclaration td,String pkg)
{
   if (!JavaAst.checkTypeSignature(td,csg,S6SignatureType.NO_METHODS,pkg)) return false;

   Set<ASTNode> used = new HashSet<ASTNode>();
   for (S6Request.FieldSignature fs : csg.getFields()) {
      boolean fnd = false;
      for (Iterator<?> it = td.bodyDeclarations().iterator(); it.hasNext(); ) {
	 ASTNode cnod = (ASTNode) it.next();
	 if (used.contains(cnod)) continue;
	 if (cnod instanceof FieldDeclaration) {
	    FieldDeclaration fd = (FieldDeclaration) cnod;
	    for (Object o : fd.fragments()) {
	       VariableDeclarationFragment vdf = (VariableDeclarationFragment) o;
	       if (used.contains(vdf)) continue;
	       // should check for conformable types
	       if (JavaAst.checkFieldSignature(vdf,fs,S6SignatureType.MODS)) {
		  fnd = true;
		  used.add(vdf);
		  break;
		}
	     }
	    if (fnd) break;
	  }
       }
      if (!fnd) return false;
    }

   for (S6Request.MethodSignature ms : csg.getMethods()) {
      boolean fnd = false;
      for (Iterator<?> it = td.bodyDeclarations().iterator(); it.hasNext(); ) {
	 ASTNode cnod = (ASTNode) it.next();
	 if (cnod instanceof MethodDeclaration) {
	    if (used.contains(cnod)) continue;
	    MethodDeclaration md = (MethodDeclaration) cnod;
	    // should check for conformable parameters and return
	
	    if (md.isConstructor()) {
	       if (!ms.getName().equals("<init>")) continue;
	     }
	    else if (ms.getName().equals("<init>")) continue;
	
	    if (JavaAst.checkMethodSignature(md,ms,S6SignatureType.MODS)) {
	       fnd = true;
	       used.add(cnod);
	       break;
	     }
	  }
       }
      if (!fnd) return false;
    }

   Type st = td.getSuperclassType();
   if (st != null && csg.getSuperClass() == null && csg.getInterfaces().size() == 0) {
      JcompType jt = JavaAst.getJavaType(st);
      if (jt != null) {
	 if (!jt.getName().startsWith(pkg) && !jt.isBinaryType()) return false;
       }
    }

   String txt = null;
   for (String k : csg.getKeywords()) {
      if (txt == null) txt = td.toString().toLowerCase();
      k = k.toLowerCase();
      if (!txt.contains(k)) return false;
    }

   // check # fields and candidate methods against signature here

   return true;
}




/********************************************************************************/
/*										*/
/*	Find all possible class mappings to start with				*/
/*										*/
/********************************************************************************/

private void findClassMappings(List<S6Request.ClassSignature> todos,
      Map<S6Request.ClassSignature,List<TypeDeclaration>> workmap,
      Map<TypeDeclaration,String> done,
      List<NameMapper> rslt,S6Request.PackageSignature psg,UsageMappings um)
{
   if (todos.size() == 0) {
      // extend to include fields and methods
      NameMapper nm = new NameMapper("package",true);
      for (Map.Entry<TypeDeclaration,String> ent : done.entrySet()) {
	 JcompSymbol js = JavaAst.getDefinition(ent.getKey());
	 if (js == null) return;
	 nm.addMapping(js,ent.getValue());
       }
      if (checkDepends(psg,nm,um)) {
	 rslt.add(nm);
       }
      return;
    }

   if (done == null) done = new HashMap<TypeDeclaration,String>();

   S6Request.ClassSignature workon = todos.remove(0);
   for (TypeDeclaration td : workmap.get(workon)) {
      if (done.containsKey(td)) continue;
      done.put(td,workon.getName());
      findClassMappings(todos,workmap,done,rslt,psg,um);
      done.remove(td);
    }
   todos.add(0,workon);
}




/********************************************************************************/
/*										*/
/*	Extend mapping with fields						*/
/*										*/
/********************************************************************************/

private List<NameMapper> addFieldMappings(NameMapper nm,S6SolutionSet sols,
      S6Solution sol,CompilationUnit cu,S6Request.PackageSignature psg)
{
   List<NameMapper> rslt = new ArrayList<NameMapper>();
   List<S6Request.FieldSignature> fields = new ArrayList<S6Request.FieldSignature>();
   for (S6Request.ClassSignature csg : psg.getClasses()) {
      fields.addAll(csg.getFields());
    }
   computeFieldMappings(nm,psg,fields,rslt);
   return rslt;
}


private void computeFieldMappings(NameMapper nm,S6Request.PackageSignature psg,List<S6Request.FieldSignature> flds,
      List<NameMapper> rslt)
{
   if (flds.isEmpty()) {
      rslt.add(nm);
      return;
    }
   List<S6Request.FieldSignature> localflds = new ArrayList<S6Request.FieldSignature>(flds);
   S6Request.FieldSignature fld = localflds.remove(0);
   S6Request.ClassSignature csg = fld.getDefiningClass();
   String fnm = csg.getName();
   String pkgnm = psg.getName();
   if (fnm.startsWith(pkgnm)) {
      fnm = fnm.substring(pkgnm.length()+1);
    }
   JcompSymbol sym = nm.findMappedSymbol(fnm);
   TypeDeclaration td = (TypeDeclaration) sym.getDefinitionNode();
   JcompTyper typer = JcompAst.getTyper(td);
   List<JcompSymbol> candidates = new ArrayList<JcompSymbol>();
   for (FieldDeclaration fd : td.getFields()) {
      JcompType jtyp = JcompAst.getJavaType(fd.getType());
      int mods = fd.getModifiers();
      if (!checkCompatibleField(fld,jtyp,mods,pkgnm,typer,nm)) continue;
      for (Object o : fd.fragments()) {
	 VariableDeclarationFragment vdf = (VariableDeclarationFragment) o;
	 JcompSymbol fldsym = JcompAst.getDefinition(vdf);
	 if (nm.getMapping().keySet().contains(fldsym)) continue;
	 candidates.add(fldsym);
       }
    }
   if (candidates.isEmpty()) return;
   restrictByName(candidates,fld.getName());

   for (JcompSymbol csym : candidates) {
      NameMapper nm1 = new NameMapper(nm);
      if (!csym.getName().equals(fld.getName()))
	 nm1.addMapping(csym,fld.getName());
      computeFieldMappings(nm1,psg,localflds,rslt);
    }
}


private boolean checkCompatibleField(S6Request.FieldSignature fld,
      JcompType typ,int mods,String pkg,JcompTyper typer,NameMapper nm)
{
   boolean staticfg = (mods & Modifier.STATIC) != 0;
   if (staticfg != fld.isStatic()) return false;
   if (!checkCompatibleType(typ,fld.getTypeName(),pkg,typer,nm)) return false;
   return true;
}



private void restrictByName(List<JcompSymbol> syms,String name)
{
   if (syms.size() < 2) return;

   boolean exactmatch = false;
   boolean contains = false;

   for (JcompSymbol sym : syms) {
      String snm = sym.getName();
      snm = getMatchName(snm);
      if (snm.equals(name)) exactmatch = true;
      else if (nameContains(snm,name)) contains = true;
    }

   for (Iterator<JcompSymbol> it = syms.iterator(); it.hasNext(); ) {
      JcompSymbol sym = it.next();
      String snm = getMatchName(sym.getName());
      if (exactmatch && !snm.equals(name)) it.remove();
      else if (contains && !nameContains(snm,name)) it.remove();
    }
}


private boolean nameContains(String sname,String uname)
{
   int len = uname.length();
   if (len <= 3 || len > 48) return false;
   sname = sname.toLowerCase();

   int [] breaks = new int[64];
   int breakct = 0;

   char prev = 0;
   for (int i = 0; i < len; ++i) {
      char ch = uname.charAt(i);
      if (Character.isUpperCase(ch) && Character.isLowerCase(prev)) {
	 breaks[breakct++] = i;
       }
      else if (ch == '_') {
	 breaks[breakct++] = i;
	 breaks[breakct++] = i+1;
       }
      prev = ch;
    }

   int mct = 0;
   int tct = 0;
   if (breakct > 0) {
      int lbrk = 0;
      for (int i = 0; i < breakct; ++i) {
	 if (breaks[i] - lbrk > 3) {
	    String snm = uname.substring(lbrk,breaks[i]);
	    snm = snm.toLowerCase();
	    if (sname.contains(snm)) ++mct;
	    ++tct;
	  }
	 lbrk = breaks[i];
       }
      String snm = uname.substring(lbrk);
      if (snm.length() > 3) {
	 snm = snm.toLowerCase();
	 if (sname.contains(snm)) ++mct;
	 ++tct;
       }
    }
   else {
      mct = sname.contains(uname) ? 1 : 0;
      tct = 1;
    }

   if (mct == 0 || mct < tct/2) return false;

   return true;
}


private String getMatchName(String snm)
{
   int idx1 = snm.indexOf("(");
   if (idx1 > 0) snm = snm.substring(0,idx1).trim();

   int idx = snm.lastIndexOf(".");
   if (idx > 0) {
      snm = snm.substring(idx+1);
    }

   return snm;
}


private boolean checkCompatibleType(JcompType typ,String target,String pkg,
      JcompTyper typer,NameMapper nm)
{
   if (target.startsWith(pkg)) {
      String ntarget = target.substring(pkg.length() + 1);
      JcompSymbol sym = nm.findMappedSymbol(ntarget);
      if (sym != null) target = sym.getFullName();
    }
   JcompType ttype = typer.findType(target);
   if (!ttype.isCompatibleWith(typ)) return false;

   return true;
}

/********************************************************************************/
/*										*/
/*	Extend mapping with methods						*/
/*										*/
/********************************************************************************/

private List<NameMapper> addMethodMappings(NameMapper nm,S6SolutionSet sols,
      S6Solution sol,CompilationUnit cu,S6Request.PackageSignature psg)
{
   List<NameMapper> rslt = new ArrayList<NameMapper>();
   List<S6Request.MethodSignature> mthds = new ArrayList<S6Request.MethodSignature>();
   for (S6Request.ClassSignature csg : psg.getClasses()) {
      mthds.addAll(csg.getMethods());
    }
   computeMethodMappings(nm,psg,mthds,rslt);
   return rslt;
}



private void computeMethodMappings(NameMapper nm,S6Request.PackageSignature psg,
      List<S6Request.MethodSignature> methods,List<NameMapper> rslt)
{
   if (methods.isEmpty()) {
      rslt.add(nm);
      return;
    }

   List<S6Request.MethodSignature> localmethods = new ArrayList<S6Request.MethodSignature>(methods);
   S6Request.MethodSignature msg = localmethods.remove(0);
   S6Request.ClassSignature csg = msg.getDefiningClass();
   String fnm = csg.getName();
   String pkgnm = psg.getName();
   if (fnm.startsWith(pkgnm)) {
      fnm = fnm.substring(pkgnm.length()+1);
    }
   JcompSymbol sym = nm.findMappedSymbol(fnm);
   TypeDeclaration td = (TypeDeclaration) sym.getDefinitionNode();
   JcompTyper typer = JcompAst.getTyper(td);

   List<JcompSymbol> candidates = new ArrayList<JcompSymbol>();
   for (MethodDeclaration md : td.getMethods()) {
      boolean iscnst = msg.getName().equals("<init>");
      if (iscnst != md.isConstructor()) continue;
      JcompType jtyp = JcompAst.getJavaType(md);
      int mods = md.getModifiers();
      if (checkCompatibleMethod(msg,jtyp,mods,pkgnm,typer,nm)) {
	 JcompSymbol msym = JcompAst.getDefinition(md);
	 if (!nm.isMapped(msym))
	    candidates.add(msym);
       }
    }

   if (candidates.isEmpty()) return;
   restrictByName(candidates,msg.getName());

   for (JcompSymbol msym : candidates) {
      NameMapper nm1 = new NameMapper(nm);
      if (!msym.isConstructorSymbol() && !msym.getName().equals(msg.getName())) {
         if (msym.isPublic()) nm1.addWrapper(msym,msg.getName());
	 else nm1.addMapping(msym,msg.getName());
       }
      computeMethodMappings(nm1,psg,localmethods,rslt);
    }
}



private boolean checkCompatibleMethod(S6Request.MethodSignature msg,
      JcompType typ,int mods,String pkg,JcompTyper typer,NameMapper nm)
{
   boolean staticfg = (mods & Modifier.STATIC) != 0;
   if (staticfg != msg.isStatic()) return false;
   if (!checkCompatibleMethodType(typ,msg,pkg,typer,nm)) return false;

   return true;
}



private boolean checkCompatibleMethodType(JcompType typ,S6Request.MethodSignature msg,
      String pkg,JcompTyper typer,NameMapper nm)
{
   List<String> pnames = msg.getParameterTypeNames();
   List<JcompType> ptypes = typ.getComponents();
   if (pnames.size() != ptypes.size()) return false;
   for (int i = 0; i < pnames.size(); ++i) {
      if (!checkCompatibleType(ptypes.get(i),pnames.get(i),pkg,typer,nm)) return false;
    }
   JcompType rtype = typ.getBaseType();
   if (rtype != null) {
      String rname = msg.getReturnTypeName();
      if (!checkCompatibleType(rtype,rname,pkg,typer,nm)) return false;
    }

   return true;
}



/********************************************************************************/
/*										*/
/*	Utility methods 							*/
/*										*/
/********************************************************************************/

static String newName()
{
   return S6_NAME_PREFIX + "_P_" + name_counter.incrementAndGet();
}


/********************************************************************************/
/*										*/
/*	Dependency information for checking classes chosen are related		*/
/*										*/
/********************************************************************************/

private UsageMappings getDependencies(CompilationUnit cu)
{
   UsageMappings uv = new UsageMappings(cu);
   cu.accept(uv);
   return uv;
}




private boolean checkDepends(S6Request.PackageSignature psg,NameMapper nm,UsageMappings um)
{
   Map<JcompSymbol,String> mp = nm.getMapping();
   Map<JcompType,S6Request.ClassSignature> omp = new HashMap<JcompType,S6Request.ClassSignature>();
   Set<JcompType> done = new HashSet<JcompType>();

   for (Map.Entry<JcompSymbol,String> ent : mp.entrySet()) {
      JcompSymbol js = ent.getKey();
      JcompType jt = js.getType();
      for (S6Request.ClassSignature csg : psg.getClasses()) {
	 if (csg.getName().equals(ent.getValue())) {
	    if (csg.isMain()) done.add(jt);
	    omp.put(jt,csg);
	    break;
	  }
       }
    }

   if (!done.isEmpty()) {
      boolean chng = true;
      while (chng) {
	 chng = false;
	 Set<JcompType> add = new HashSet<JcompType>();
	 for (JcompType jt0 : done) {
	    for (JcompType jt1 : um.getReferencesFrom(jt0)) {
	       if (omp.containsKey(jt1) && !done.contains(jt1)) add.add(jt1);
	     }
	  }
	 if (add.size() > 0) {
	    done.addAll(add);
	    chng = true;
	  }
       }
      if (done.size() != omp.size()) return false;
    }
   else {
      for (JcompType jt0 : omp.keySet()) {
	 for (JcompType jt1 : um.getReferencesFrom(jt0)) {
	    if (omp.containsKey(jt1)) done.add(jt1);
	  }
       }
      if (done.size() != omp.size()) return false;
    }

   return true;
}




private static class UsageMappings extends ASTVisitor {

   private TypeDeclaration current_class;
   private JcompType current_type;
   private Set<JcompType> relevant_types;
   private Map<JcompType,Set<JcompType>> ref_map;
   private Map<JcompType,Set<JcompType>> deref_map;

   UsageMappings(CompilationUnit cu) {
      relevant_types = new HashSet<JcompType>();
      for (Object o : cu.types()) {
	 if (o instanceof TypeDeclaration) {
	    TypeDeclaration td = (TypeDeclaration) o;
	    JcompType jt = JavaAst.getJavaType(td);
	    if (jt != null) relevant_types.add(jt);
	  }
       }
      ref_map = new HashMap<JcompType,Set<JcompType>>();
      deref_map = new HashMap<JcompType,Set<JcompType>>();
      current_type = null;
      current_class = null;
    }

   Collection<JcompType> getReferencesFrom(JcompType jt) {
      Collection<JcompType> rslt = ref_map.get(jt);
      if (rslt == null) rslt = new ArrayList<JcompType>();
      return rslt;
    }

   @Override public void preVisit(ASTNode n) {
      if (n instanceof TypeDeclaration && current_class == null) {
	 current_class = (TypeDeclaration) n;
	 current_type = JavaAst.getJavaType(n);
       }
    }

   @Override public void postVisit(ASTNode n) {
      JcompType jt = JavaAst.getJavaType(n);
      jt = getRelevantType(jt);
      if (jt == null) {
	 JcompSymbol js = JavaAst.getReference(n);
	 if (js != null) {
	    jt = js.getType();
	    jt = getRelevantType(jt);
	  }
       }
      if (jt != null) addRef(jt);
      if (n == current_class) current_class = null;
    }

   private void addRef(JcompType jt) {
      if (current_type == null || jt == null) return;
      if (current_type == jt) return;
      addToMap(ref_map,current_type,jt);
      addToMap(deref_map,jt,current_type);
    }

   private JcompType getRelevantType(JcompType jt) {
      if (jt == null) return null;
      while (jt != null && jt.isArrayType()) {
	 jt = jt.getBaseType();
       }
      if (jt == null) return null;
      if (jt == current_type) return null;
      if (relevant_types.contains(jt)) return jt;
      return jt;
    }

   private void addToMap(Map<JcompType,Set<JcompType>> m,JcompType s,JcompType t) {
      Set<JcompType> sjt = m.get(s);
      if (sjt == null) {
	 sjt = new HashSet<JcompType>();
	 m.put(s,sjt);
       }
      sjt.add(t);
    }

}	// end of inner class UsageMappings



/********************************************************************************/
/*										*/
/*	Handle removing types that aren't used                                  */
/*										*/
/********************************************************************************/

private void findDependentTypes(CompilationUnit cu,NameMapper nm)
{
   DependenceVisitor dv = new DependenceVisitor();
   boolean chng = true;
   Set<JcompType> done = new HashSet<JcompType>();

   while (chng) {
      chng = false;
      for (Object o : cu.types()) {
	 AbstractTypeDeclaration atd = (AbstractTypeDeclaration) o;
	 JcompSymbol js = JavaAst.getDefinition(atd);
	 JcompType jt = JavaAst.getJavaType(atd);
	 if (done.contains(jt)) continue;
	 String newname = nm.getMapping().get(js);
	 if (newname != null || dv.getTypes().contains(jt)) {
	    done.add(jt);
	    atd.accept(dv);
	    chng = true;
	  }
       }
    }

   nm.addDependentTypes(dv.getTypes());
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
      // SimpleName fld = n.getName();
      JcompType jt = JavaAst.getJavaType(nm);
      boolean fg = (jt != null);
      // System.err.println("QUAL NAME " + nm + " . " + fld + " " + fg);
      collect_stack.push(fg);
      return true;
    }
   @Override public void endVisit(QualifiedName n) {
      collect_stack.pop();
    }
   @Override public void endVisit(SimpleType n) {
      if (collect_stack.peek()) {
         JcompType jt = JavaAst.getJavaType(n);
         if (jt != null && !jt.isBinaryType() && jt.isClassType() && !jt.isArrayType()) {
            type_defs.add(jt);
          }
       }
    }

}	// end of inner class DependenceVisitor




/********************************************************************************/
/*										*/
/*	Class to handle name mappings						*/
/*										*/
/********************************************************************************/

private class NameMapper extends TreeMapper {

   private String map_name;
   private Map<JcompSymbol,String> sym_mapping;
   private Map<JcompSymbol,String> sym_wrappers;
   private boolean remove_classes;
   private Set<JcompType> dependent_types;

   NameMapper(String nm,boolean rmc) {
      map_name = nm;
      remove_classes = rmc;
      sym_mapping = new HashMap<JcompSymbol,String>();
      sym_wrappers = new HashMap<JcompSymbol,String>();
      dependent_types = null;
      if (rmc) dependent_types = new HashSet<JcompType>();
    }

   NameMapper(NameMapper orig) {
      map_name = orig.map_name;
      remove_classes = orig.remove_classes;
      sym_mapping = new HashMap<JcompSymbol,String>(orig.sym_mapping);
      sym_wrappers = new HashMap<JcompSymbol,String>(orig.sym_wrappers);
      if (orig.dependent_types != null)
	 dependent_types = new HashSet<JcompType>(orig.dependent_types);
      else dependent_types = null;
    }

   @Override protected String getSpecificsName()	{ return map_name; }

   void addMapping(JcompSymbol from,String to) {
      sym_mapping.put(from,to);
    }
   
   void addWrapper(JcompSymbol from,String to) {
      sym_wrappers.put(from,to);
    } 

   Map<JcompSymbol,String> getMapping() 	{ return sym_mapping; }
   
   boolean isMapped(JcompSymbol js) {
      if (sym_mapping.get(js) != null) return true;
      if (sym_wrappers.get(js) != null) return true;
      return false;
    }

   JcompSymbol findMappedSymbol(String nm) {
      for (Map.Entry<JcompSymbol,String> ent : sym_mapping.entrySet()) {
         if (ent.getValue().equals(nm)) return ent.getKey();
       }
      for (Map.Entry<JcompSymbol,String> ent : sym_wrappers.entrySet()) {
         if (ent.getValue().equals(nm)) return ent.getKey();
       }
      return null;
    }

   void addDependentTypes(Collection<JcompType> typs) {
      if (dependent_types != null) dependent_types.addAll(typs);
    }

   void rewriteTree(ASTNode orig,ASTRewrite rw) {
      JcompSymbol js = JavaAst.getDefinition(orig);
      if (js != null) {
         String newname = sym_mapping.get(js);
         if (newname == null && js.getName().equals("<init>")) {
            for (ASTNode p = orig; p != null; p = p.getParent()) {
               if (p instanceof TypeDeclaration) {
        	  JcompSymbol tjs = JavaAst.getDefinition(p);
        	  newname = sym_mapping.get(tjs);
        	  break;
        	}
             }
          }
         if (newname != null) {
            rewriteName(orig,rw,newname);
          }
         else if (sym_wrappers.get(js) != null) {
            if (orig instanceof MethodDeclaration)
            createWrapper(orig,rw);
          }
         return;
       }
      js = JavaAst.getReference(orig);
      if (js == null) {
         JcompType jt = JavaAst.getJavaType(orig);
         if (jt != null) js = jt.getDefinition();
       }
      if (js != null) {
         String newname = sym_mapping.get(js);
         if (newname != null) {
            rewriteName(orig,rw,newname);
          }
       }
      if (orig.getNodeType() == ASTNode.COMPILATION_UNIT && remove_classes) {
         CompilationUnit cu = (CompilationUnit) orig;
         ListRewrite lrw = rw.getListRewrite(orig,CompilationUnit.TYPES_PROPERTY);
         for (Object o : cu.types()) {
            AbstractTypeDeclaration atd = (AbstractTypeDeclaration) o;
            if (keepType(atd)) continue;
            js = JavaAst.getDefinition(atd);
            if (js != null) {
               String newname = sym_mapping.get(js);
               if (newname == null) lrw.remove(atd,null);
             }
          }
       }
    }

   private boolean keepType(AbstractTypeDeclaration atd) {
      if (atd instanceof EnumDeclaration) return true;
      if (atd instanceof TypeDeclaration) {
	 TypeDeclaration td = (TypeDeclaration) atd;
	 if (dependent_types != null) {
	    JcompType jt = JavaAst.getJavaType(td);
	    if (jt != null && dependent_types.contains(jt)) return true;
	  }
	 if (td.getMethods().length == 0) return true;
	 FieldDeclaration [] fds = td.getFields();
	 if (td.isInterface() && fds.length > 0) return true;
	 else if (td.isInterface()) return true;
	
	 int stat = 0;
	 int nstat = 0;
	 for (Object o : td.bodyDeclarations()) {
	    BodyDeclaration bd = (BodyDeclaration) o;
	    if (Modifier.isStatic(bd.getModifiers()) || Modifier.isAbstract(bd.getModifiers()))
	       ++stat;
	    else
	       ++nstat;
	  }
	 if (stat > 0 && nstat == 0) return true;
       }
      return false;
    }

   private void rewriteName(ASTNode nd,ASTRewrite rw,String name) {
      if (nd instanceof SimpleName) {
         try {
            rw.set(nd,SimpleName.IDENTIFIER_PROPERTY,name,null);
          }
         catch (IllegalArgumentException e) {
            IvyLog.logE("JAVA","Problem with new transform framework name " + name + ": " + e);
          }
       }
    }
   
   @SuppressWarnings({"rawtypes", "unchecked" }) 
   private void createWrapper(ASTNode nd,ASTRewrite rw) {
      JcompSymbol js = JavaAst.getDefinition(nd);
      String nm = sym_wrappers.get(js);
      if (nm == null) return;
      // won't handle enums or attribute types
      TypeDeclaration td = null;
      MethodDeclaration md = null;
      for (ASTNode n = nd; n != null; n = n.getParent()) {
         if (n instanceof MethodDeclaration) {
            md = (MethodDeclaration) n;
          }
         if (n instanceof AbstractTypeDeclaration) {
            td = (TypeDeclaration) n;
            break;
          }
       }
      if (td == null || md == null) return;
      AST ast = rw.getAST();
      MethodDeclaration newmd = (MethodDeclaration) ASTNode.copySubtree(ast,md);
      SimpleName name = newmd.getName();
      name.setIdentifier(nm);
      MethodInvocation mi = ast.newMethodInvocation();
      mi.setName(JavaAst.getSimpleName(ast,js.getName()));
      List args = mi.arguments();
      int idx = 0;
      List nparmas = newmd.parameters();
      for (Object param : md.parameters()) {
         SingleVariableDeclaration svd = (SingleVariableDeclaration) param;
         String id = svd.getName().getIdentifier();
         args.add(JavaAst.getSimpleName(ast,id));
         SingleVariableDeclaration nsvd = (SingleVariableDeclaration) nparmas.get(idx);
         NameTypeMapper ntm = new NameTypeMapper(sym_mapping,svd,nsvd);
         svd.accept(ntm);
         ++idx;
       }
      ExpressionStatement estmt = ast.newExpressionStatement(mi);
      Block b = ast.newBlock();
      b.statements().add(estmt);
      newmd.setBody(b);
      
      ListRewrite lrw = rw.getListRewrite(td,TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
      lrw.insertAfter(newmd,md,null);
    }
   
}	// end of subclass NameMapper


private static class NameTypeMapper extends ASTVisitor {
   
   Map<JcompSymbol,String> name_map;
   ASTNode new_node;
   ASTNode old_root;
   
   NameTypeMapper(Map<JcompSymbol,String> maps,ASTNode oldroot,ASTNode newroot) {
      name_map = maps;
      old_root = oldroot;
      new_node = newroot;
    }
   
   @Override public void endVisit(SimpleName n) {
      JcompSymbol js = JcompAst.getDefinition(n);
      if (js == null) {
         JcompType jt = JavaAst.getJavaType(n);
         if (jt != null) {
            js = jt.getDefinition();
          }
       }
      if (js == null) return;
      
      String nm = name_map.get(js);
      if (nm == null) return;
      ASTNode onm = findMatch(n);
      if (onm != null) {
         SimpleName snm = (SimpleName) onm;
         snm.setIdentifier(nm);
       }
    }
   
   private ASTNode findMatch(ASTNode n) {
      if (n == old_root) return new_node;
      ASTNode np = n.getParent();
      StructuralPropertyDescriptor spd = n.getLocationInParent();
      ASTNode nn = findMatch(np);
      if (nn == null) return null;
      if (spd.isChildListProperty()) {
         List<?> lst = (List<?>) nn.getStructuralProperty(spd);
         List<?> olst = (List<?>) np.getStructuralProperty(spd);
         int idx = olst.indexOf(n);
         return (ASTNode) lst.get(idx);
       }
      else {
         return (ASTNode) nn.getStructuralProperty(spd);
       }
    }
}



}	// end of class TransformFrameworkNames




/* end of TransformFrameworkNames.java */

