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
import edu.brown.cs.s6.common.S6Fragment;
import edu.brown.cs.s6.common.S6Language;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6Solution;
import edu.brown.cs.s6.common.S6SolutionSet;
import edu.brown.cs.s6.common.S6TestResults;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
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
   helper_order = new LinkedList<>();
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
   
   for (ASTNode hn : helper_order) {
      if (s == null) s = hn.toString();
      else s += "\n" + hn.toString();
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
   
   if (getAstNode() == null) {
      setIsolated(true);
      return;
    }
   
   ASTNode rn = getAstNode().getRoot();
   
   boolean dup = false;
   for (S6Solution sol : ss) {
      S6Fragment f = sol.getFragment();
      if (f == this) continue;
      if (!(f instanceof FragmentJava)) continue;
      FragmentJava fj = (FragmentJava) f;
      ASTNode an = fj.getAstNode();
      if (an == null) continue;
      if (an.getRoot() == rn) dup = true;
    }
   
   if (!dup) {
      setIsolated(true);
      return;
    }
   
   localIsolate();
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

@Override public Iterable<ASTNode> getHelpers()	{ return helper_order; }

boolean addHelper(ASTNode n)
{
   if (helper_fragments.contains(n)) return false;
   helper_fragments.add(n);
   
   int idx = 0;
   if (n instanceof FieldDeclaration) {
      FieldDeclaration fd = (FieldDeclaration) n;
      FieldFinder ff = new FieldFinder();
      for (Iterator<?> it = fd.fragments().iterator(); it.hasNext(); ) {
	 VariableDeclarationFragment vdf = (VariableDeclarationFragment) it.next();
	 Expression e = vdf.getInitializer();
	 if (e != null) e.accept(ff);
       }
      Set<JcompSymbol> uf = ff.getUsedFields();
      if (uf.size() > 0) {
	 for (int i = 0; i < helper_order.size(); ++i) {
	    ASTNode an = helper_order.get(i);
	    if (an instanceof FieldDeclaration) {
	       FieldDeclaration afd = (FieldDeclaration) an;
	       for (Iterator<?> it = afd.fragments().iterator(); it.hasNext(); ) {
		  VariableDeclarationFragment vdf = (VariableDeclarationFragment) it.next();
		  JcompSymbol js = JavaAst.getDefinition(vdf);
		  if (uf.contains(js)) idx = i+1;
		}
	     }
	  }
       }
    }
   helper_order.add(idx,n);
   
   return true;
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

