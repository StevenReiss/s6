/********************************************************************************/
/*										*/
/*		TransformSetupTesting.java					*/
/*										*/
/*	Find mappings from search tests to user code				*/
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.QualifiedType;
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

public class TransformSetupTesting extends TransformJava implements S6Constants, JavaConstants
{


/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public TransformSetupTesting(String name)
{
   super(name);
}


/********************************************************************************/
/*										*/
/*	Handle test case setup							*/
/*										*/
/********************************************************************************/

@Override protected List<TreeMapper> findClassMappings(S6SolutionSet solset,
      TypeDeclaration td,S6Request.ClassSignature cs,S6Solution sol)
{
   S6Request.TestingSignature ts = (S6Request.TestingSignature) solset.getRequest().getSignature();

   if (!validateClass(td)) {
      sol.setFlag(S6SolutionFlag.REMOVE);
      return null;
    }

   JcompTyper typer = JavaAst.getTyper(td);
   MethodFinder mf = new MethodFinder(ts,typer);
   td.accept(mf);
   Set<MapCandidate> cands = mf.getCandidates();
   if (cands == null || cands.size() == 0) {
      sol.setFlag(S6SolutionFlag.REMOVE);
      return null;
    }
   handleEqualNames(cands);

   sol.setFlag(S6SolutionFlag.REMOVE);

   if (solset.doDebug()) System.err.println("S6: Setup TESTING size " + cands.size());

   if (cands.size() > 64) return null;

   List<TreeMapper> rslt = new ArrayList<TreeMapper>();
   Set<MapCandidate> done = new HashSet<MapCandidate>();
   addCandidates(cands,done,rslt,ts,td);

   if (solset.doDebug()) System.err.println("S6: Setup TESTING size1 " + rslt.size());

   if (rslt.size() < 512) {
      filterCandiates(cands,rslt);
    }
   else return null;

   return rslt;
}



private boolean validateClass(TypeDeclaration td)
{
   if (!Modifier.isPublic(td.getModifiers())) return false;

   for (Object o : td.modifiers()) {
      if (o instanceof Annotation) {
	 Annotation annot = (Annotation) o;
	 String nm = annot.getTypeName().getFullyQualifiedName();
	 int idx = nm.lastIndexOf(".");
	 if (idx > 0) nm = nm.substring(idx+1);
	 if (nm.equals("RunWith")) return false;
       }
    }

   boolean havecnstr = false;
   boolean havepubcnstr = false;
   for (Object o : td.bodyDeclarations()) {
      if (o instanceof MethodDeclaration) {
	 MethodDeclaration md = (MethodDeclaration) o;
	 if (md.isConstructor()) {
	    if (md.parameters().size() > 0) havecnstr = true;
	    else if (Modifier.isPublic(md.getModifiers())) havepubcnstr = true;
	  }
       }
      if (havecnstr && !havepubcnstr) return false;
    }

   return true;
}



private void handleEqualNames(Set<MapCandidate> cands)
{
   Set<MapCandidate> samename = new HashSet<MapCandidate>();
   for (MapCandidate mc : cands) {
      String nm1 = mc.getMethod().getName();
      String nm2 = mc.getSourceMethod();
      if (nm1.equals(nm2)) {
	 samename.add(mc);
       }
    }

   if (samename.isEmpty()) return;
   for (Iterator<MapCandidate> it = cands.iterator(); it.hasNext(); ) {
      MapCandidate mc = it.next();
      for (MapCandidate same : samename) {
	 if (same.getSourceMethod().equals(mc.getSourceMethod()) &&
	       same.getSourceClass().equals(mc.getSourceClass())) {
	    it.remove();
	    break;
	  }
       }
    }

   for (MapCandidate same : samename) {
      boolean have = false;
      for (MapCandidate mc : cands) {
	 if (mc.getSourceClass() == same.getSourceClass()) have = true;
       }
      if (!have)
	 cands.add(same);
    }

   if (cands.size() == 0) {
      for (MapCandidate mc : samename) {
	 cands.add(mc);
	 break;
       }
    }
  //  cands.addAll(samename);
}


private int addCandidates(Set<MapCandidate> cands,Set<MapCandidate> done,List<TreeMapper> rslt,
      S6Request.TestingSignature ts,TypeDeclaration td)
{
   Set<MapCandidate> nset = new HashSet<MapCandidate>(cands);
   int nadd = 0;

   for (MapCandidate mc : cands) {
      if (done.contains(mc)) continue;
      nset.remove(mc);
      boolean use = true;
      String sclass = mc.getSourceClass();
      Map<String,String> omap = new HashMap<String,String>();
      for (MapCandidate dmc : done) {
	 if (mc.getMethod() == dmc.getMethod()) use = false;
	 if (mc.getSourceMethod().equals(dmc.getSourceMethod())) {
	    if (sclass == null || dmc.getSourceClass() == null ||
		   sclass.equals(dmc.getSourceClass())) {
	       if (dmc.getTargetClass().equals(mc.getTargetClass())) {
		  String m1 = mc.getMethod().getName();
		  String m2 = dmc.getMethod().getName();
		  if (!m1.equals(m2)) use = false;
		}
	     }
	  }
	 if (sclass != null && dmc.getSourceClass() != null &&
	       !sclass.equals(dmc.getSourceClass()))
	    use = false;
	 omap.putAll(dmc.getClassMap());
       }
      if (!use) continue;
      if (!isCompatibleMap(mc.getClassMap(),omap)) continue;
      done.add(mc);
      TestingMapper tm = new TestingMapper(done,ts,td);
      int newadd = addCandidates(nset,done,rslt,ts,td);
      nadd += newadd;
      if (newadd == 0) {
	 rslt.add(tm);
	 ++nadd;
       }
      done.remove(mc);
    }

   return nadd;
}



private void filterCandiates(Set<MapCandidate> cands,List<TreeMapper> rslt)
{
   Map<MapCandidate,MapCandidate> overloads = new HashMap<MapCandidate,MapCandidate>();

   for (MapCandidate mc : cands) {
      for (MapCandidate xmc : cands) {
	 if (xmc == mc) break;
	 if (xmc.getTargetClass().equals(mc.getTargetClass()) &&
	       xmc.getMethod() != mc.getMethod() &&
	       xmc.getMethod().getName().equals(mc.getMethod().getName()) &&
	       xmc.getSourceMethod().equals(mc.getSourceMethod()) &&
	       isCompatibleMap(mc.getClassMap(),xmc.getClassMap())) {
	    overloads.put(mc,xmc);
	    overloads.put(xmc,mc);
	  }
       }
    }

   if (overloads.size() > 0) {
      for (Iterator<TreeMapper> it = rslt.iterator(); it.hasNext(); ) {
	 TestingMapper tm = (TestingMapper) it.next();
	 Set<MapCandidate> use = tm.getCandidateSet();
	 boolean valid = true;
	 for (MapCandidate mc : use) {
	    MapCandidate amc = overloads.get(mc);
	    if (amc != null && !use.contains(amc)) valid = false;
	  }
	 if (!valid) {
	    it.remove();
	  }
       }
    }

   for (Iterator<TreeMapper> it = rslt.iterator(); it.hasNext(); ) {
      TestingMapper tm0 = (TestingMapper) it.next();
      Set<MapCandidate> use0 = tm0.getCandidateSet();
      int sz0 = use0.size();
      for (TreeMapper tmap : rslt) {
	 TestingMapper tm1 = (TestingMapper) tmap;
	 if (tm1 == tm0) continue;
	 Set<MapCandidate> use1 = tm1.getCandidateSet();
	 if (use1.size() > sz0 && use1.containsAll(use0)) {
	    it.remove();
	    break;
	  }
       }
    }
}


private boolean isCompatibleMap(Map<String,String> m1,Map<String,String> m2)
{
   if (m1 == null || m2 == null) return true;

   Set<String> vals2 = new HashSet<String>(m2.values());
   for (Map.Entry<String,String> ent1 : m1.entrySet()) {
      String v2 = m2.get(ent1.getKey());
      if (v2 != null && !v2.equals(ent1.getValue())) return false;
      else if (v2 == null && vals2.contains(ent1.getValue())) return false;
    }

   return true;
}


private String getClassName(MethodInvocation mi)
{
   String cn = null;
   JcompType et = null;
   if (mi.getExpression() != null) {
      et = JavaAst.getExprType(mi.getExpression());
    }
   if (et != null && !et.isErrorType()) {
      cn = et.getName();
    }
   else if (et != null) {
      Expression exp = mi.getExpression();
      if (exp instanceof Name) {
	 Name nm =  (Name) exp;
	 cn = nm.getFullyQualifiedName();
       }
    }
   return cn;
}



private boolean checkAppropriate(MethodInvocation mi,S6Request.TestingSignature ts,
      S6Request.MethodSignature ms)
{
   List<JcompType> exps = new ArrayList<JcompType>();
   for (Object ano : mi.arguments()) {
      Expression ex = (Expression) ano;
      JcompType ext = JavaAst.getExprType(ex);
      if (ext.isErrorType()) return false;
      exps.add(ext);
    }
   Map<String,String> clsmap = new HashMap<String,String>();
   JcompTyper typer = JavaAst.getTyper(mi);
   if (!checkTarget(ms,exps,clsmap,typer)) return false;
   return true;
}



private boolean checkTarget(S6Request.MethodSignature ms,List<JcompType> args,
      Map<String,String> clsmap,JcompTyper typer) {
   if (ms.getName().startsWith("<")) return false;
   List<String> msargs = ms.getParameterTypeNames();
   if (msargs.size() != args.size()) return false;
   for (int i = 0; i < args.size(); ++i) {
      String ma = msargs.get(i);
      ma = ma.replace(" ","");
      JcompType styp = typer.findSystemType(ma);
      JcompType atyp = args.get(i);
      String ta = atyp.getName();
      String ta1 = clsmap.get(ta);
      if (ta1 != null) {
	 atyp = typer.findSystemType(ta1);
	 ta = ta1;
       }
      if (!ta.equals(ma)) {
	 if (atyp != null && styp != null) {
	    if (!atyp.isCompatibleWith(styp)) return false;
	  }
	 else return false;
       }
    }
   return true;
}




/********************************************************************************/
/*										*/
/*	MethodeFinder -- find candidate target methods				*/
/*										*/
/********************************************************************************/

private class MethodFinder extends ASTVisitor {

   private S6Request.TestingSignature test_signature;
   private Set<MapCandidate> map_candidates;
   private JcompTyper java_typer;

   MethodFinder(S6Request.TestingSignature ts,JcompTyper typer) {
      test_signature = ts;
      java_typer = typer;
      map_candidates = new HashSet<MapCandidate>();
    }

   Set<MapCandidate> getCandidates() {
      return map_candidates;
    }

   @Override public void endVisit(MethodInvocation mi) {
      String mname = mi.getName().getIdentifier();
      JcompSymbol js = JavaAst.getReference(mi.getName());
      if (js != null) return;
      boolean isvoid = false;
      switch (mi.getParent().getNodeType()) {
	 case ASTNode.EXPRESSION_STATEMENT :
	    isvoid = true;
	    break;
       }
      List<JcompType> exps = new ArrayList<JcompType>();
      for (Object ano : mi.arguments()) {
	 Expression ex = (Expression) ano;
	 JcompType ext = JavaAst.getExprType(ex);
	 if (ext == null || ext.isErrorType()) return;
	 exps.add(ext);
       }
      String cn = getClassName(mi);
      if (cn == null) return;
      Map<String,String> clsmap = new HashMap<String,String>();
      List<S6Request.MethodSignature> targets = getTargets(exps,clsmap,cn,isvoid);
      if (targets == null || targets.size() == 0) return;
      for (S6Request.MethodSignature ms : targets) {
	 map_candidates.add(new MapCandidate(test_signature,ms,cn,mname,clsmap));
       }
      // System.err.println("LOOK AT METHOD " + mi);
    }

   private List<S6Request.MethodSignature> getTargets(List<JcompType> args,
	 Map<String,String> clsmap,String cls,boolean isvoid) {
      List<S6Request.MethodSignature> rslt = new ArrayList<S6Request.MethodSignature>();
      String pnm = test_signature.getPackage();
      S6Request.ClassSignature cs = test_signature.getClassToTest();
      String cnm = cs.getName();
      if (!cnm.contains(".")) cnm = pnm + "." + cnm;
      clsmap.put(cls,cnm);

      for (S6Request.MethodSignature ms : cs.getMethods()) {
	 String ret = ms.getReturnTypeName();
	 if (ret != null) {
	    boolean msvoid = ret.equals("void");
	    if (msvoid && !isvoid && !ms.getName().contains("<")) continue;
	  }
	 if (checkTarget(ms,args,clsmap,java_typer)) {
	    rslt.add(ms);
	  }
       }
      return rslt;
    }



}	// end of inner class MethodFinder



/********************************************************************************/
/*										*/
/*	MapCandidate -- candidate mapping					*/
/*										*/
/********************************************************************************/

private static class MapCandidate {

   S6Request.TestingSignature test_data;
   S6Request.MethodSignature target_method;
   String source_class;
   String source_method;
   Map<String,String> class_maps;

   MapCandidate(S6Request.TestingSignature ts,S6Request.MethodSignature ms,String cn,String mn,
	 Map<String,String> clsmap) {
      test_data = ts;
      target_method = ms;
      source_class = cn;
      source_method = mn;
      class_maps = clsmap;
    }

   Map<String,String> getClassMap()		{ return class_maps; }

   @Override public boolean equals(Object o) {
      if (o instanceof MapCandidate) {
	 MapCandidate mc = (MapCandidate) o;
	 if (mc.target_method != target_method) return false;
	 if (source_class == null) {
	    if (mc.source_class != null) return false;
	  }
	 else if (!source_class.equals(mc.source_class)) return false;
	 if (!source_method.equals(mc.source_method)) return false;
	 return true;
       }
      return false;
    }

   @Override public int hashCode() {
      int hc = target_method.hashCode();
      if (source_class != null) hc += source_class.hashCode();
      hc += source_method.hashCode();
      return hc;
    }

   String getSourceClass()		{ return source_class; }
   String getSourceMethod()		{ return source_method; }

   String getTargetClass() {
      S6Request.ClassSignature cs = test_data.getClassToTest();
      return cs.getName();
    }
   S6Request.MethodSignature getMethod()	{ return target_method; }

   @Override public String toString() {
      StringBuffer buf = new StringBuffer();
      buf.append(getSourceClass());
      buf.append(".");
      buf.append(getSourceMethod());
      buf.append(" => ");
      buf.append(getTargetClass());
      buf.append(".");
      buf.append(getMethod().getName());
      buf.append("(");
      int ct = 0;
      for (String s : getMethod().getParameterTypeNames()) {
	 if (ct++ > 0) buf.append(",");
	 buf.append(s);
       }
      buf.append(")");
      buf.append(getMethod().getReturnTypeName());
      return buf.toString();
    }

}	// end of inner class MapCandidate




/********************************************************************************/
/*										*/
/*	Actual testing mapping							*/
/*										*/
/********************************************************************************/

private class TestingMapper extends TreeMapper {

   private Set<MapCandidate> using_candidates;
   private S6Request.TestingSignature test_signature;
   private String from_class;
   private String to_class;
   private TypeDeclaration top_class;
   private Map<String,String> class_maps;
   private String map_name;

   TestingMapper(Set<MapCandidate> mcs,S6Request.TestingSignature ts,TypeDeclaration td) {
      using_candidates = new HashSet<MapCandidate>(mcs);
      test_signature = ts;
      top_class = td;
      class_maps = new HashMap<String,String>();
      for (MapCandidate mc : using_candidates) {
	 if (to_class == null) to_class = mc.getTargetClass();
	 if (from_class == null) from_class = mc.getSourceClass();
	 class_maps.putAll(mc.getClassMap());
       }
      StringBuffer buf = new StringBuffer();
      for (MapCandidate mc : using_candidates) {
	 if (buf.length() > 0) buf.append("+");
	 buf.append(mc.getSourceClass());
	 buf.append(".");
	 buf.append(mc.getSourceMethod());
	 buf.append("_");
	 buf.append(mc.getMethod().getName());
       }
      map_name = buf.toString();
    }

   @Override protected String getSpecificsName()	{ return map_name; }

   Set<MapCandidate> getCandidateSet()			{ return using_candidates; }

   @Override void rewriteTree(ASTNode orig,ASTRewrite rw) {
      if (orig instanceof MethodInvocation) {
	 MethodInvocation mi = (MethodInvocation) orig;
	 String mname = mi.getName().getIdentifier();
	 String cname = getClassName(mi);
	 boolean fnd = false;
	 for (MapCandidate mc : using_candidates) {
	    if (!mc.getSourceMethod().equals(mname)) continue;
	    if (cname == null) {
	       if (mc.getSourceClass() != null) continue;
	     }
	    else if (!cname.equals(mc.getSourceClass())) continue;
	    if (checkAppropriate(mi,test_signature,mc.getMethod())) {
	       Name sn = JavaAst.getSimpleName(rw.getAST(),mc.getMethod().getName());
	       rw.set(mi,MethodInvocation.NAME_PROPERTY,sn,null);
	       fnd = true;
	     }
	  }
	 if (!fnd) fixJunitCalls(mi,rw);
       }
      else if (orig instanceof Name && to_class != null) {
	 Name qn = (Name) orig;
	 String mapnm = class_maps.get(qn.getFullyQualifiedName());
	 if (mapnm != null) {
	    Name nm = JavaAst.getQualifiedName(rw.getAST(),mapnm);
	    rw.replace(qn,nm,null);
	  }
	 else if (qn.getFullyQualifiedName().equals(from_class)) {
	    Name nm = JavaAst.getQualifiedName(rw.getAST(),to_class);
	    rw.replace(qn,nm,null);
	  }
       }
      else if (orig instanceof QualifiedType && to_class != null) {
	 QualifiedType qt = (QualifiedType) orig;
	 String q1 = qt.getQualifier().toString();
	 String q2 = qt.getName().getFullyQualifiedName();
	 String q = q1 + "." + q2;
	 String mapnm = class_maps.get(q);
	 if (mapnm != null) {
	    Name nm = JavaAst.getQualifiedName(rw.getAST(),mapnm);
	    rw.replace(qt,nm,null);
	  }
	 else if (q.equals(from_class)) {
	    Name nm = JavaAst.getQualifiedName(rw.getAST(),to_class);
	    rw.replace(qt,nm,null);
	  }
       }
      else if (orig instanceof Annotation) {
	 Annotation an = (Annotation) orig;
	 Name nm = an.getTypeName();
	 String qn = nm.getFullyQualifiedName();
	 String nqn = getAnnotationName(an);
	 if (nqn != null && !nqn.equals(qn)) {
	    Name nmx = JavaAst.getQualifiedName(rw.getAST(),nqn);
	    rw.set(an,an.getTypeNameProperty(),nmx,null);
	  }
       }
      else if (orig instanceof MethodDeclaration) {
	 MethodDeclaration md = (MethodDeclaration) orig;
	 JcompSymbol js = JavaAst.getDefinition(md);
	 if (js == null || !js.isPublic()) return;
	 String nm = md.getName().getIdentifier();
	 if (nm.startsWith("test")) {
	    addAnnotationIfNeeded(md,"Test",rw);
	  }
	 else if (stdName("setUp",md)) {
	    addAnnotationIfNeeded(md,"Before",rw);
	  }
	 else if (stdName("tearDown",md)) {
	    addAnnotationIfNeeded(md,"After",rw);
	  }
	 else if (stdName("setUpBeforeClass",md)) {
	    addAnnotationIfNeeded(md,"BeforeClass",rw);
	  }
	 else if (stdName("tearDownAfterClass",md)) {
	    addAnnotationIfNeeded(md,"AfterClass",rw);
	  }
       }
      else if (orig instanceof FieldDeclaration) {
	 if (orig.getParent() == top_class) {
	    FieldDeclaration fd = (FieldDeclaration) orig;
	    if (!Modifier.isPrivate(fd.getModifiers())) {
	       ListRewrite lrw = rw.getListRewrite(fd,fd.getModifiersProperty());
	       for (Object o : fd.modifiers()) {
		  IExtendedModifier iem = (IExtendedModifier) o;
		  if (iem.isModifier()) {
		     Modifier md = (Modifier) o;
		     if (md.isPublic() || md.isProtected()) {
			lrw.remove(md,null);
		      }
		   }
		}
	       Modifier md = rw.getAST().newModifier(Modifier.ModifierKeyword.PRIVATE_KEYWORD);
	       lrw.insertLast(md,null);
	     }
	  }
       }
      else if (orig == top_class) {
	 TypeDeclaration td = (TypeDeclaration) orig;
	 Type supt = td.getSuperclassType();
	 if (supt != null) {
	    JcompType jty = JavaAst.getJavaType(supt);
	    if (jty != null) {
	       String nm = jty.getName();
	       if (nm.equals("junit.framework.TestCase")) {
		  rw.set(td,TypeDeclaration.SUPERCLASS_TYPE_PROPERTY,null,null);
		}
	     }
	  }
	 CompilationUnit cu = (CompilationUnit) orig.getRoot();
	 String pkg = test_signature.getPackage();
	 PackageDeclaration pd = rw.getAST().newPackageDeclaration();
	 Name pname = JavaAst.getQualifiedName(rw.getAST(),pkg);
	 pd.setName(pname);
	 rw.set(cu,CompilationUnit.PACKAGE_PROPERTY,pd,null);
	 ListRewrite lrw = null;
	 for (Object o : cu.imports()) {
	    ImportDeclaration id = (ImportDeclaration) o;
	    if (id.isStatic()) {
	     }
	    else if (!id.isOnDemand()) {
	       Name nm = id.getName();
	       JcompType jt = JavaAst.getJavaType(nm);
	       if (jt == null) {
		  if (lrw == null) lrw = rw.getListRewrite(cu,CompilationUnit.IMPORTS_PROPERTY);
		  lrw.remove(id,null);
		}
	     }
	  }
       }
    }


   private boolean stdName(String match,MethodDeclaration md) {
      String nm = md.getName().getIdentifier();
      if (nm.equals(match)) {
	 int mods = md.getModifiers();
	 if (Modifier.isProtected(mods)) return true;
	 if (Modifier.isPublic(mods)) return true;
       }
      return false;
   }


   private void addAnnotationIfNeeded(MethodDeclaration md,String what,ASTRewrite rw) {
      for (Object o : md.modifiers()) {
	 if (o instanceof Annotation) {
	    Annotation an = (Annotation) o;
	    String qnm = getAnnotationName(an);
	    if (qnm.startsWith("org.junit.")) return;
	  }
       }
      Annotation an = rw.getAST().newMarkerAnnotation();
      Name tnm = JavaAst.getQualifiedName(rw.getAST(),"org.junit." + what);
      an.setTypeName(tnm);
      ListRewrite lrw = rw.getListRewrite(md,MethodDeclaration.MODIFIERS2_PROPERTY);
      lrw.insertFirst(an,null);
   }

   private void fixJunitCalls(MethodInvocation mi,ASTRewrite rw) {
      String nm = mi.getName().getIdentifier();
      if (nm.startsWith("assert") || nm.equals("fail")) {
	 JcompSymbol js = JavaAst.getReference(mi);
	 if (js != null) {
	    String fqn = js.getFullName();
	    if (!fqn.startsWith("org.junit.Assert.") &&
		  !fqn.startsWith("junit.framework.Assert."))
	       return;
	  }
	 Expression ex = mi.getExpression();
	 if (ex != null) {
	    if (ex instanceof Name) {
	       Name nm1 = (Name) ex;
	       String fq1 = nm1.getFullyQualifiedName();
	       if (fq1.equals("org.junit.Assert")) return;
	     }
	    else return;
	  }
	 Name nmx = JavaAst.getQualifiedName(rw.getAST(),"org.junit.Assert");
	 nmx = JavaAst.getQualifiedName(rw.getAST(),
	       "edu.brown.cs.s6.runner.RunnerAssert");
	 rw.set(mi,MethodInvocation.EXPRESSION_PROPERTY,nmx,null);
       }
      else if (nm.startsWith("assume")) {
	 JcompSymbol js = JavaAst.getReference(mi);
	 if (js != null) {
	    String fqn = js.getFullName();
	    if (!fqn.startsWith("org.junit.Assume.") &&
		  !fqn.startsWith("junit.framework.Assume."))
	       return;
	  }
	 Expression ex = mi.getExpression();
	 if (ex != null) {
	    if (ex instanceof Name) {
	       Name nm1 = (Name) ex;
	       String fq1 = nm1.getFullyQualifiedName();
	       if (fq1.equals("org.junit.Assume")) return;
	     }
	    else return;
	  }
	 Name nmx = JavaAst.getQualifiedName(rw.getAST(),"org.junit.Assume");
	 rw.set(mi,MethodInvocation.EXPRESSION_PROPERTY,nmx,null);
       }
    }

   private String getAnnotationName(Annotation an) {
      Name nm = an.getTypeName();
      String qn = nm.getFullyQualifiedName();
      JcompType jt = JavaAst.getJavaType(nm);
      if (qn.startsWith("org.junit.")) return qn;
      if (jt == null && !qn.contains(".")) {
	 switch (qn) {
	    case "Test" :
	    case "After" :
	    case "AfterClass" :
	    case "Before" :
	    case "BeforeClass" :
	    case "ClassRule" :
	    case "Ignore" :
	    case "Rule" :
	       qn = "org.junit." + qn;
	       break;
	    default :
	       break;
	  }
       }
      else if (jt != null && jt.getName().startsWith("org.junit.")) qn = jt.getName();
      return qn;
    }
}	// end of inner class TestingMapper






}	// end of class TransformSetupTesting




/* end of TransformSetupTesting.java */

