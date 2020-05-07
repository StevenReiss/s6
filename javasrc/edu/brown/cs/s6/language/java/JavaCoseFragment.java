/********************************************************************************/
/*                                                                              */
/*              JavaCoseFragment.java                                           */
/*                                                                              */
/*      Fragments based on COSE results                                         */
/*                                                                              */
/********************************************************************************/
/*      Copyright 2013 Brown University -- Steven P. Reiss                    */
/*********************************************************************************
 *  Copyright 2013, Brown University, Providence, RI.                            *
 *                                                                               *
 *                        All Rights Reserved                                    *
 *                                                                               *
 *  Permission to use, copy, modify, and distribute this software and its        *
 *  documentation for any purpose other than its incorporation into a            *
 *  commercial product is hereby granted without fee, provided that the          *
 *  above copyright notice appear in all copies and that both that               *
 *  copyright notice and this permission notice appear in supporting             *
 *  documentation, and that the name of Brown University not be used in          *
 *  advertising or publicity pertaining to distribution of the software          *
 *  without specific, written prior permission.                                  *
 *                                                                               *
 *  BROWN UNIVERSITY DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS                *
 *  SOFTWARE, INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND            *
 *  FITNESS FOR ANY PARTICULAR PURPOSE.  IN NO EVENT SHALL BROWN UNIVERSITY      *
 *  BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY          *
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,              *
 *  WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS               *
 *  ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE          *
 *  OF THIS SOFTWARE.                                                            *
 *                                                                               *
 ********************************************************************************/



package edu.brown.cs.s6.language.java;

import edu.brown.cs.s6.language.LanguageBase;
import edu.brown.cs.s6.language.LanguageCoseFragment;
import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Language;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6SolutionSet;
import edu.brown.cs.s6.common.S6TestResults;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;

import edu.brown.cs.cose.cosecommon.CoseConstants;
import edu.brown.cs.cose.cosecommon.CoseResult;
import edu.brown.cs.cose.cosecommon.CoseSource;
import edu.brown.cs.ivy.jcomp.JcompSymbol;
import edu.brown.cs.ivy.jcomp.JcompType;

abstract class JavaCoseFragment extends LanguageCoseFragment implements S6Constants, CoseConstants, JavaConstants, JavaFragment
{


// This needs subtypes for File,Package,Method,Class fragments
// This needs subtypes for cloned fragments for the various types
//
//   Nothing to do: File

/********************************************************************************/
/*                                                                              */
/*      Static factory methods                                                  */
/*                                                                              */
/********************************************************************************/

static JavaCoseFragment createCoseFragment(CoseResult cr,S6Language lang,S6Request.Search sreq)
{
   if (cr == null) {
      System.err.println("NULL RESULT");
    }
   switch (cr.getResultType()) {
      case PACKAGE :
         return new JavaPackageFragment(lang,sreq,cr);
      case FILE :
         return new JavaFileFragment(lang,sreq,cr);
      case CLASS : 
         return new JavaClassFragment(lang,sreq,cr);
      case METHOD :
         return new JavaMethodFragment(lang,sreq,cr);
    }
   
   return null;
}



/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private Set<ASTNode>            helper_fragments;
private List<ASTNode>           helper_order;
private JavaAstClassName        class_namer;
protected boolean               use_constructor;
protected Collection<JcompType> import_set;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

protected JavaCoseFragment(S6Language lj,S6Request.Search rqst,CoseResult rslt)
{
   super(lj,rqst,rslt);
   initialize();
}





private void initialize() 
{
   helper_fragments = new HashSet<>();
   helper_order = null;
   class_namer = null;
   use_constructor = false;
   import_set = null;
}



/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

public String getText()
{
   String s = null;
   
   if (helper_fragments.size() > 0 && helper_order == null) getHelpers();
   
   if (helper_order != null) {
      for (ASTNode hn : helper_order) {
         if (s == null) s = hn.toString();
         else s += "\n" + hn.toString();
       }
    }
   
   if (s == null) s = getAstNode().toString();
   else s += "\n\n" + getAstNode().toString();
   
   return s;
}

public String getFinalText(S6SearchType st)
{
   if (st == S6SearchType.TESTCASES) {
      ASTNode an = getAstNode().getRoot();
      return an.toString();
    }
   
   return getText();
}


@Override public String getOriginalText()
{
   return cose_result.getEditText();
}



@Override public ASTNode getAstNode()
{
   return (ASTNode) cose_result.getStructure();
}





protected JavaCoseFragment getJavaParent()
{
   return (JavaCoseFragment) parent_fragment;
}

protected LanguageJava getJavaBase()
{
   return (LanguageJava) language_base; 
}

@Override public JavaAstClassName getClassNamer()
{
   return class_namer;
}

@Override public boolean getUseConstructor()	        { return use_constructor; }

@Override public Collection<JcompType> getImportTypes() { return import_set; }

@Override public String getLocalText()
{
   ASTNode cfn = (ASTNode) cose_result.checkStructure();
   if (cfn != null) return cfn.toString();
   
   ASTNode xn = getAstNode();
   String r = null;
   if (xn != null) r = xn.toString();
   clearResolve();
   return r;
}



/********************************************************************************/
/*                                                                              */
/*      AST clean up methods  :: localize the results                           */
/*                                                                              */
/********************************************************************************/

@Override public void makeLocal(S6SolutionSet ss)
{
   isolate(ss); 		// first isolate it wrt ss
   
   resolveFragment();
   
   fixupAst();
}



@Override public void fixupAst()
{
   ASTNode n = getAstNode();
   while (n != null) {
      if (n instanceof AbstractTypeDeclaration) break;
      n = n.getParent();
    }
   if (n == null) return;
   
   JcompType jt = JavaAst.getJavaType(n);
   if (jt == null) return;
   class_namer = new JavaAstClassName(jt);
   
   Localizer lcl = new Localizer(class_namer,jt);
   n.getRoot().accept(lcl);
}


private void isolate(S6SolutionSet ss)
{
   if (isIsolated()) return;
   
   setIsolated(true);
}


protected void localIsolate()			{ }




private static class Localizer extends ASTVisitor {

   private JavaAstClassName class_namer;
   private JcompType	given_class;
   
   Localizer(JavaAstClassName cn,JcompType jt) {
      class_namer = cn;
      given_class = jt;
      cn.clearNames();
    }
   
   public void endVisit(PackageDeclaration n) {
      class_namer.setPackage(n.getName().getFullyQualifiedName());
    }
   
   public void endVisit(SimpleName n) {
      checkName(n);
    }
   
   public void endVisit(QualifiedName n) {
      checkName(n);
    }
   
   private void checkName(Name cn) {
      if (JavaAst.getJavaType(cn) == given_class) {
         class_namer.noteName(cn);
       }
    }

}	// end of subclass Localizer



/********************************************************************************/
/*                                                                              */
/*      Code complexity                                                         */
/*                                                                              */
/********************************************************************************/

public int getCodeComplexity()
{
   ComplexityMeasure cm = new ComplexityMeasure();
   
   getAstNode().accept(cm);
   for (ASTNode nd : getHelpers()) {
      nd.accept(cm);
    }
   
   return cm.getNodeCount();
}


private static class ComplexityMeasure extends ASTVisitor {
   
   private int num_nodes;
   
   ComplexityMeasure() {
      num_nodes = 0;
    }
   
   int getNodeCount()				{ return num_nodes; }
   
   public void preVisit(ASTNode n)		{ ++num_nodes; }
   
}	// end of subclass ComplexityMeasure




/********************************************************************************/
/*										*/
/*	Methods to handle helper nodes						*/
/*										*/
/********************************************************************************/

@Override public Iterable<ASTNode> getHelpers()
{
   if (helper_order == null) orderHelpers();
   
   return helper_order;
}

boolean addHelper(ASTNode n)
{
   if (helper_fragments.contains(n)) return false;
   
   helper_fragments.add(n);
   helper_order = null;
   return true;
}




private void orderHelpers()
{
   Map<ASTNode,Set<ASTNode>> precedes = new HashMap<>();
   
   setupHelperPartialOrder(precedes);
   
   Set<ASTNode> todo = new HashSet<>();
   Set<ASTNode> methods = new HashSet<>();
   for (ASTNode an : helper_fragments) {
      if (an instanceof MethodDeclaration) methods.add(an);
      else todo.add(an);
    }
   
   helper_order = helperSort(todo,precedes);
   for (ASTNode an : methods) {
      helper_order.add(an);
    }
}



private void setupHelperPartialOrder(Map<ASTNode,Set<ASTNode>> precedes)
{
   Map<JcompSymbol,ASTNode> defmap = new HashMap<>();
   Map<ASTNode,Set<JcompSymbol>> usesmap = new HashMap<>();
   
   for (ASTNode an : helper_fragments) {
      if (an instanceof FieldDeclaration) {
         FieldDeclaration fd = (FieldDeclaration) an;
         for (Object o : fd.fragments()) {
            VariableDeclarationFragment vdf = (VariableDeclarationFragment) o;
            JcompSymbol js = JavaAst.getDefinition(vdf);
            defmap.put(js,an);
          }
       }
    }
   for (ASTNode an : helper_fragments) {
      if (an instanceof FieldDeclaration) {
         FieldDeclaration afd = (FieldDeclaration) an;
         FieldFinder ff = new FieldFinder();
         for (Object o : afd.fragments()) {
            VariableDeclarationFragment vdf = (VariableDeclarationFragment) o;
            Expression e = vdf.getInitializer();
            if (e != null) e.accept(ff);
          }
         usesmap.put(an,ff.getUsedFields());
       }
      else if (an instanceof Initializer) {
         FieldFinder ff = new FieldFinder();
         an.accept(ff);
         usesmap.put(an,ff.getUsedFields());
       }
    }
   
   for (ASTNode an : helper_fragments) {
      if (an instanceof MethodDeclaration) continue;
      else if (an instanceof FieldDeclaration) {
         for (JcompSymbol js : usesmap.get(an)) {
            ASTNode refnode = defmap.get(js);
            // if the initializer of a field refers to another field,
            // then other field must be declared first
            if (refnode != null) addPrecedes(refnode,an,precedes);
          }
       }
      else if (an instanceof Initializer) {
         Set<JcompSymbol> uses = usesmap.get(an);
         for (JcompSymbol js : uses) {
            ASTNode refnode = defmap.get(js);
            // if the static initializer refers to a symbol, the def for that
            // must precede the initializer
            if (refnode != null) addPrecedes(refnode,an,precedes);
          }
         for (Map.Entry<ASTNode,Set<JcompSymbol>> ent : usesmap.entrySet()) {
            ASTNode en = ent.getKey();
            if (en == an) continue;
            for (JcompSymbol js : ent.getValue()) {
               // if static initializer refers to a symbol used in the initializer of en
               // this means that an (static init) must precede en
               if (uses.contains(js)) addPrecedes(an,en,precedes);
             }
          }
       }
    }
}


private void addPrecedes(ASTNode before,ASTNode after,Map<ASTNode,Set<ASTNode>> precedes) 
{
   if (before == null || after == null || before == after) return;
   
   Set<ASTNode> val = precedes.get(after);
   if (val == null) {
      val = new HashSet<>();
      precedes.put(after,val);
    }
   val.add(before);
}



private List<ASTNode> helperSort(Set<ASTNode> todo,Map<ASTNode,Set<ASTNode>> precedes)
{
   List<ASTNode> rslt = new ArrayList<>();
   
   boolean chng = true;
   while (chng) {
      chng = false;
      Set<ASTNode> added = new HashSet<>();
      for (ASTNode an : todo) {
         Set<ASTNode> prec = precedes.get(an);
         if (prec == null || prec.isEmpty()) {
            rslt.add(an);
            for (Set<ASTNode> pset : precedes.values()) {
               pset.remove(an);
             }
            added.add(an);
          }
       }
      if (!added.isEmpty()) {
         todo.removeAll(added);
         chng = true;
       }
    }
   
   rslt.addAll(todo);
   
   return rslt;
}
private class FieldFinder extends ASTVisitor {
   
   private Set<JcompSymbol> use_fields;
   
   FieldFinder() {
      use_fields = new HashSet<JcompSymbol>();
    }
   
   Set<JcompSymbol> getUsedFields()			{ return use_fields; }
   
   public @Override void postVisit(ASTNode n) {
      JcompSymbol js = JavaAst.getReference(n);
      if (js != null && js.isFieldSymbol()) use_fields.add(js);
    }

}	// end of subclass FieldFinder



/********************************************************************************/
/*										*/
/*	Resolution methods							*/
/*										*/
/********************************************************************************/

public synchronized void resolveFragment()
{
   getJavaBase().resolveFragment(this);
   if (cose_result.getResultType() == CoseResultType.FILE) JavaAst.setKeep(getAstNode(),true);
}



public void clearResolve()
{
   ASTNode n = (ASTNode) cose_result.clearStructure();
   if (n != null) {
      clearResolvedData(n);
    }
}




protected void clearResolvedData(ASTNode n)
{
   if (n == null) return;
   ASTNode r = n.getRoot();
   if (r == null) return;
   
   synchronized (r) {
      JavaAst.setSource(r,null);
      if (JavaAst.isKeep(r))
	 return;
      ClearAll ca = new ClearAll();
      r.accept(ca);
    }
}



private static class ClearAll extends ASTVisitor {
   
   @Override public void postVisit(ASTNode n) {
      JavaAst.clearAll(n);
    }
   
}	// end of subclass ClearAll


/********************************************************************************/
/*										*/
/*	Testing methods 							*/
/*										*/
/********************************************************************************/

public S6SolutionFlag checkTestCases(S6Request.Search r,CoseSource src)
{
   JavaTester mt = getJavaBase().createTester(r,this,src);
   S6TestResults trslt = mt.run();
   setTestResults(trslt);
   if (trslt == null) return S6SolutionFlag.FAIL;
   
   S6SolutionFlag fgrslt = getTestResults().getSummaryResult();
   if (fgrslt == S6SolutionFlag.PASS && ((LanguageBase) language_base).doDebug()) {
      System.err.println("TEST RESULT: " + getTestResults().printSummary());
      
    }
   return fgrslt;
}

/********************************************************************************/
/*                                                                              */
/*      Cloning methods                                                         */
/*                                                                              */
/********************************************************************************/

@Override public JavaCoseFragment cloneFragment(ASTRewrite rw,ITrackedNodePosition pos)
{
   CoseResult newresult = cose_result.cloneResult(rw,pos);
   return createCoseFragment(newresult,language_base,search_request);
}


@Override public JavaCoseFragment cloneFragment(String newtext)		
{
   CoseResult newresult = cose_result.cloneResult(newtext,null);
   return createCoseFragment(newresult,language_base,search_request);
}




}       // end of class JavaCoseFragment




/* end of JavaCoseFragment.java */

