/********************************************************************************/
/*										*/
/*		FragmentJava.java						*/
/*										*/
/*	Basic implementation of java code fragment				*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/language/java/FragmentJava.java,v 1.20 2016/07/18 23:05:25 spr Exp $ */


/*********************************************************************************
 *
 * $Log: FragmentJava.java,v $
 * Revision 1.20  2016/07/18 23:05:25  spr
 * Update transforms for applications and UI.
 *
 * Revision 1.19  2015/12/23 15:45:09  spr
 * Minor fixes.
 *
 * Revision 1.18  2015/09/23 17:54:51  spr
 * Version to handle andriod UI applications.
 *
 * Revision 1.17  2015/02/14 19:40:18  spr
 * Add test case generation.
 *
 * Revision 1.16  2014/08/29 15:16:08  spr
 * Updates for suise, testcases.
 *
 * Revision 1.15  2014/02/26 14:06:41  spr
 * Add transformations for user interfaces.
 *
 * Revision 1.14  2013/10/10 18:01:18  spr
 * Github updates
 *
 * Revision 1.13  2013/09/13 20:33:03  spr
 * Add calls for UI search.
 *
 * Revision 1.12  2012-08-13 16:51:50  spr
 * Add new transforms, clean up code, bug fixes.
 *
 * Revision 1.11  2012-07-20 22:15:19  spr
 * New transforms and resolution for UI search
 *
 * Revision 1.10  2012-06-20 12:21:32  spr
 * Initial fixes for UI search
 *
 * Revision 1.9  2012-06-11 14:07:48  spr
 * add framework search; fix bugs
 *
 * Revision 1.8  2009-09-18 01:41:35  spr
 * Handle user testing.
 *
 * Revision 1.7  2009-05-12 22:28:48  spr
 * Fix ups to make user context work.
 *
 * Revision 1.6  2008-11-12 13:52:13  spr
 * Performance and bug updates.
 *
 * Revision 1.5  2008-08-28 00:32:56  spr
 * Next version of S6.	Lots of bug fixes, some new functionality.
 *
 * Revision 1.4  2008-07-18 22:27:09  spr
 * Handle remove compilation calls; update transforms to include code to use ASTrewrite.
 *
 * Revision 1.3  2008-07-17 13:46:45  spr
 * Bug fixes and speed ups.
 *
 * Revision 1.2  2008-06-12 17:47:52  spr
 * Next version of S6.
 *
 * Revision 1.1.1.1  2008-06-03 12:59:22  spr
 * Initial version of S6
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.language.java;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.ChildPropertyDescriptor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;

import edu.brown.cs.cose.cosecommon.CoseSource;
import edu.brown.cs.cose.cosecommon.CoseConstants.CoseResultType;
import edu.brown.cs.ivy.jcomp.JcompSymbol;
import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Fragment;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6Solution;
import edu.brown.cs.s6.common.S6SolutionSet;
import edu.brown.cs.s6.common.S6TestResults;
import edu.brown.cs.s6.language.FragmentBase;
import edu.brown.cs.s6.language.TextDelta;


abstract class FragmentJava extends FragmentBase implements S6Constants, JavaConstants, JavaFragment {



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

protected List<ASTNode>   context_fragments;
private Set<ASTNode>	helper_fragments;
private List<ASTNode>	helper_order;
protected JavaAstClassName class_namer;
protected boolean	  use_constructor;
protected Collection<JcompType> import_set;


private static boolean use_deltas = true;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

private FragmentJava(LanguageJava lj,S6Request.Search rqst)
{
   super(lj,rqst);
   initialize();
}



private FragmentJava(FragmentJava p)
{
   super(p);
   initialize();
}



private void initialize()
{
   helper_fragments = new HashSet<ASTNode>();
   context_fragments = new ArrayList<ASTNode>();
   helper_order = new LinkedList<ASTNode>();
   class_namer = null;
   use_constructor = false;
   import_set = null;
}



/********************************************************************************/
/*										*/
/*	Factory methods 							*/
/*										*/
/********************************************************************************/

static FragmentJava createFileFragment(LanguageJava lj,String text,CoseSource src,S6Request.Search sr)
{
   if (text == null) return null;

   CompilationUnit cu = JavaAst.parseSourceFile(text);
   if (cu == null) return null;

   FileFragment ff = new FileFragment(lj,sr,cu,text);

   if (src != null) JavaAst.setS6Source(cu,src);
   if (sr != null) JavaAst.setSearchRequest(cu,sr);

   return ff;
}







static FragmentJava createPackageFragment(LanguageJava lj,S6Request.Search sr)
{
   PackageFragment pf = new PackageFragment(lj,sr);

   return pf;
}




/********************************************************************************/
/*										*/
/*	Access	methods 							*/
/*										*/
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



public String getKeyText()
{
   ASTNode n = getAstNode();
   if (n == null) return "";

   String s0 = n.toString();
   ASTNode r = n.getRoot();
   if (r != getAstNode()) {
      s0 += " @ " + r.toString();
    }

   return s0;
}



@Override abstract public ASTNode getAstNode();
abstract ASTNode checkAstNode();
@Override abstract public String getOriginalText();

protected FragmentJava getJavaParent()
{
   return (FragmentJava) parent_fragment;
}

protected LanguageJava getJavaBase()
{
   return (LanguageJava) language_base;
}



FragmentJava cloneFragment(ASTNode n)					{ return null; }
@Override public FragmentJava cloneFragment(ASTRewrite rw,ITrackedNodePosition pos)	{ return null; }
@Override public FragmentJava cloneFragment(String newtext)				{ return null; }

void saveAst()						{ }

@Override public JavaAstClassName getClassNamer()
{
   return class_namer;
}


@Override public boolean getUseConstructor()		{ return use_constructor; }

@Override public Collection<JcompType> getImportTypes() { return import_set; }



@Override public String getLocalText()
{
   ASTNode n = checkAstNode();
   if (n != null) return n.toString();

   ASTNode xn = getAstNode();
   String r = null;
   if (xn != null) r = xn.toString();
   clearResolve();
   return r;
}




/********************************************************************************/
/*										*/
/*	Methods to handle helper nodes						*/
/*										*/
/********************************************************************************/

@Override public Iterable<ASTNode> getHelpers() { return helper_order; }

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
   else if (n instanceof Initializer) {
      for (int i = 0; i < helper_order.size(); ++i) {
	 ASTNode an = helper_order.get(i);
	 if (an instanceof FieldDeclaration) {
	    idx = i+1;
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
}



public void clearResolve()
{
   ASTNode n = checkAstNode();
   if (n != null)
      clearResolvedData(n);
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
   if (fgrslt == S6SolutionFlag.PASS && language_base.doDebug()) {
      System.err.println("TEST RESULT: " + getTestResults().printSummary());

    }
   return fgrslt;
}




/********************************************************************************/
/*										*/
/*	Methods to localize the fragment					*/
/*										*/
/********************************************************************************/

public void makeLocal(S6SolutionSet ss)
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




/********************************************************************************/
/*										*/
/*	Methods to find interior fragments					*/
/*										*/
/********************************************************************************/

public Collection<S6Fragment> getFragments(S6SearchType typ)
{
   FindVisitor fv = new FindVisitor(this,typ);
   getAstNode().accept(fv);

   return fv.getFragments();
}


private static class FindVisitor extends ASTVisitor {

   private S6SearchType search_type;
   private Collection<S6Fragment> found_fragments;
   private FragmentJava parent_fragment;
   private boolean is_test;

   FindVisitor(FragmentJava par,S6SearchType st) {
      search_type = st;
      parent_fragment = par;
      found_fragments = new ArrayList<S6Fragment>();
    }

   Collection<S6Fragment> getFragments()		{ return found_fragments; }

   @Override public boolean visit(TypeDeclaration n) {
      switch (search_type) {
	 case METHOD :
	    return true;
	 case CLASS :
	 case FULLCLASS :
	 case UIFRAMEWORK :
	 case ANDROIDUI :
	 case PACKAGE :
	 case APPLICATION :
	    break;
	 case TESTCASES :
	    is_test = false;
	    break;
       }
      if (n.isInterface()) return false;
      found_fragments.add(new ClassFragment(parent_fragment,n,search_type == S6SearchType.FULLCLASS));
      return true;	// allow nested types to be used
    }


   @Override public void endVisit(TypeDeclaration n) {
      if (!is_test) return;
      if (search_type != S6SearchType.TESTCASES) return;
      if (n.isInterface()) return;
      found_fragments.add(new ClassFragment(parent_fragment,n,false));
   }

   @Override public boolean visit(MethodDeclaration n) {
      if (n.getBody() == null) return false;

      switch (search_type) {
	 case METHOD :
	    break;
	 case CLASS :
	 case FULLCLASS :
	 case UIFRAMEWORK :
	 case PACKAGE :
	 case ANDROIDUI :
	 case APPLICATION :
	    return false;
	 case TESTCASES :
	    if (n.getName().getIdentifier().startsWith("test")) is_test = true;
	    if (is_test) return false;
	    return true;
       }
      found_fragments.add(new MethodFragment(parent_fragment,n));
      return false;
    }



   @Override public void endVisit(MethodInvocation mi) {
      String nid = mi.getName().getFullyQualifiedName();
      if (nid.contains("assert") || nid.contains("assume") ||
            nid.contains("Assert") || nid.contains("Assume")) {
         is_test = true;
       }
    }

   @Override public boolean visit(MarkerAnnotation ma) {
      String nm = ma.getTypeName().getFullyQualifiedName();
      if (nm.endsWith("Test")) is_test = true;
      return false;
    }

   @Override public boolean visit(NormalAnnotation na) {
      String nm = na.getTypeName().getFullyQualifiedName();
      if (nm.endsWith("Test")) is_test = true;
      return false;
   }

}	// end of subclass FindVisitor



/********************************************************************************/
/*										*/
/*	Methods to handle user source file specifications			*/
/*										*/
/********************************************************************************/

protected void handleUserSource(S6SolutionSet ss)	// never used
{
   S6Request.Context ctx = ss.getRequest().getUserContext();
   if (ctx == null) return;
   JavaContext jctx = new JavaContext(ctx.getContextFile());
   String src = jctx.getSourceFile();
   if (src == null) return;

   CompilationUnit cu = JavaAst.parseSourceFile(src);
   if (cu == null) return;
   AbstractTypeDeclaration st = null;

   for (Iterator<?> it = cu.types().iterator(); it.hasNext(); ) {
      AbstractTypeDeclaration atd = (AbstractTypeDeclaration) it.next();
      SimpleName sn = atd.getName();
      if (sn != null && sn.getIdentifier().equals(jctx.getContextClass())) {
	 st = atd;
	 break;
       }
    }
   if (st == null) return;

   for (Iterator<?> it = st.bodyDeclarations().iterator(); it.hasNext(); ) {
      BodyDeclaration bd = (BodyDeclaration) it.next();
      context_fragments.add(bd);
    }
}




/********************************************************************************/
/*										*/
/*	Methods to handle output						*/
/*										*/
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
/*	Package fragment (for set of files)					*/
/*										*/
/********************************************************************************/

private static class PackageFragment extends FragmentJava {

   protected List<FileFragment> file_fragments;
   protected CompilationUnit root_node;
   private String source_text;
   protected Set<String> used_packages;
   private String base_package;

   PackageFragment(LanguageJava lj,S6Request.Search sr) {
      super(lj,sr);
      file_fragments = new ArrayList<FileFragment>();
      used_packages = new HashSet<String>();
      root_node = null;
      source_text = null;
    }

   PackageFragment(FragmentJava par,CompilationUnit cu) {
      super(par);
      root_node = cu;
      if (cu != null) JavaAst.setSearchRequest(cu,par.getSearchRequest());
      source_text = null;
      file_fragments = new ArrayList<FileFragment>();
      used_packages = new HashSet<String>();
      base_package = null;
      for (S6Fragment ff : par.getFileFragments()) {
	 file_fragments.add((FileFragment) ff);
       }
      if (cu != null) {
	 PackageDeclaration pd = cu.getPackage();
	 if (pd != null) {
	    String nm = pd.getName().getFullyQualifiedName();
	    base_package = nm;
	    used_packages.add(nm);
	  }
       }
    }

   public CoseResultType getFragmentType()	{ return CoseResultType.PACKAGE; }

   ASTNode checkAstNode()			{ return root_node; }
   @Override public ASTNode getAstNode() {
      buildRoot();
      return root_node;
    }

   @Override public String getOriginalText() {
      buildRoot();
      return source_text;
    }

   public Collection<S6Fragment> getFileFragments() {
      return new ArrayList<S6Fragment>(file_fragments);
    }
   public Collection<String> getPackages() {
      return new ArrayList<String>(used_packages);
    }
   public String getBasePackage() {
      if (base_package == null && root_node == null) buildRoot();
      return base_package;
    }
   public boolean addPackage(String pkg) {
      if (root_node == null) buildRoot();
      return used_packages.add(pkg);
    }

   public synchronized void addInnerFragment(S6Fragment sf) {
      if (sf instanceof FileFragment) {
	 FileFragment ff = (FileFragment) sf;
	 file_fragments.add(ff);
	 root_node = null;
	 ASTNode rn = ff.getAstNode();
	 if (rn != null) JavaAst.setKeep(rn,false);
       }
    }

   public boolean checkInitial(S6Request.Signature sgn) {
      CompilationUnit cu = (CompilationUnit) getAstNode();
      if (cu == null) return false;
      PackageDeclaration pd = cu.getPackage();
      String tnm = sgn.getName();
      if (pd == null) return (tnm == null);
      if (tnm == null) return false;
      String pnm = pd.getName().getFullyQualifiedName();
      if (!pnm.equals(tnm)) return false;
      if (sgn instanceof S6Request.UISignature) {
	 S6Request.UISignature usg = (S6Request.UISignature) sgn;
	 if (!JavaAst.checkUITypes(cu,usg,this)) return false;
       }
      return true;
    }

   private void buildRoot() {
      if (root_node != null) return;
      if (file_fragments.size() == 0) return;
   
      FileSorter fs = new FileSorter(file_fragments);
      file_fragments = fs.sort();
   
      for (FileFragment ff : file_fragments) {
         CompilationUnit fn = (CompilationUnit) ff.getAstNode();
         if (root_node == null) {
            AST nast = AST.newAST(AST.JLS17,true);
            root_node = (CompilationUnit) ASTNode.copySubtree(nast,fn);
            // root_node = fn;
          }
         else root_node = mergeIntoAst(root_node,fn,used_packages);
       }
      if (used_packages.size() == 0) {
         PackageDeclaration pd = root_node.getPackage();
         if (pd != null) {
            String pnm = pd.getName().getFullyQualifiedName();
            base_package = pnm;
            used_packages.add(pnm);
          }
       }
      source_text = root_node.toString();
      ASTParser parser = ASTParser.newParser(AST.JLS17);
      parser.setKind(ASTParser.K_COMPILATION_UNIT);
      Map<String,String> options = JavaCore.getOptions();
      JavaCore.setComplianceOptions(JavaCore.VERSION_1_7,options);
      parser.setCompilerOptions(options);
      parser.setSource(source_text.toCharArray());
      root_node = (CompilationUnit) parser.createAST(null);
    }

   @Override public FragmentJava cloneFragment(ASTRewrite rw,ITrackedNodePosition pos) {
      return new ClonedPackageFragment(this,rw,pos);
    }

   @Override public FragmentJava cloneFragment(String newtext) {
      return new ClonedPackageFragment(this,newtext);
   }

   @Override public boolean checkSignature(S6Request.Signature rsg,S6SignatureType styp) {
      S6Request.PackageSignature ps = (S6Request.PackageSignature) rsg;
      if (root_node == null) buildRoot();
      return JavaAst.checkPackageSignature(root_node,ps,styp,this);
    }

   @Override public boolean fixDependencies(S6SolutionSet ss,S6Solution sol) {
      if (root_node == null) buildRoot();
      JavaDepends jd = new JavaDepends(ss,sol,root_node);
      if (!jd.findDependencies()) return false;

      import_set = jd.getImportTypes();

      return true;
    }

}	// end of inner class PackageFragment








@SuppressWarnings("unchecked")
private static CompilationUnit mergeIntoAst(CompilationUnit rn,CompilationUnit nn,
      Set<String> pkgs)
{
   String pnm = null;
   PackageDeclaration pd = rn.getPackage();
   if (pd != null) {
      pnm = pd.getName().getFullyQualifiedName();
    }

   Set<String> imps = new HashSet<String>();
   for (Iterator<?> it = rn.imports().iterator(); it.hasNext(); ) {
      ImportDeclaration id = (ImportDeclaration) it.next();
      if (importFromPackage(id,pkgs)) it.remove();
      else {
	 String fq = getImportRename(id,pnm,pkgs);
	 if (fq == null) fq = id.getName().getFullyQualifiedName();
	 imps.add(fq);
       }
    }

   fixNameConflicts(rn,nn);

   for (Object nimp : nn.imports()) {
      ImportDeclaration id = (ImportDeclaration) nimp;
      if (importFromPackage(id,pkgs)) continue;
      String nfq = getImportRename(id,pnm,pkgs);
      if (nfq != null && imps.contains(nfq)) continue;
      String fq = id.getName().getFullyQualifiedName();
      if (imps.contains(fq)) continue;
      ImportDeclaration nid = (ImportDeclaration) ASTNode.copySubtree(rn.getAST(),id);
      if (nfq != null) {
	 Name nnm = JavaAst.getQualifiedName(rn.getAST(),nfq);
	 nid.setName(nnm);
       }
      rn.imports().add(nid);
    }

   for (Object ntyp : nn.types()) {
      AbstractTypeDeclaration td = (AbstractTypeDeclaration) ntyp;
      ASTNode ntd = ASTNode.copySubtree(rn.getAST(),td);
      rn.types().add(ntd);
    }

   return rn;
}




private static boolean importFromPackage(ImportDeclaration id,Set<String> pkgs)
{
   for (String pkg : pkgs) {
      String fq = id.getName().getFullyQualifiedName();
      if (id.isOnDemand() && pkg.equals(fq)) return true;

      if (!fq.startsWith(pkg)) continue;
      int idx = pkg.length();
      if (fq.length() <= idx) continue;
      if (fq.charAt(idx) != '.') continue;
      if (fq.length() <= idx+2) continue;
      String rnm = fq.substring(idx+1);
      if (Character.isUpperCase(rnm.charAt(0)) && rnm.indexOf(".") < 0) return true;
    }

   return false;
}



private static String getImportRename(ImportDeclaration id,String pnm,Set<String> pkgs)
{
   String bnm = null;
   for (String pkg : pkgs) {
      if (pkg.equals(pnm)) continue;
      String fq = id.getName().getFullyQualifiedName();
      if (id.isOnDemand()) continue;
      if (!fq.startsWith(pkg)) continue;
      int idx = pkg.length();
      if (fq.charAt(idx) != '.') continue;
      if (fq.length() <= idx+2) continue;
      String rnm = fq.substring(idx+1);
      if (!Character.isUpperCase(rnm.charAt(0))) continue;
      if (bnm == null || bnm.length() > rnm.length()) bnm = rnm;
    }

   if (bnm == null) return null;
   if (pnm == null) return bnm;

   return pnm + "." + bnm;
}



private class FileSorter {

   private List<FileFragment> base_list;
   private Map<FileFragment,List<FileFragment>> depend_names;
   private Map<String,FileFragment> class_names;

   FileSorter(List<FileFragment> frags) {
      base_list = frags;
      depend_names = new HashMap<FileFragment,List<FileFragment>>();
      class_names = new HashMap<String,FileFragment>();
      for (FileFragment ff : frags) {
	 CompilationUnit cu = (CompilationUnit) ff.getAstNode();
	 for (Object o : cu.types()) {
	    if (o instanceof TypeDeclaration) {
	       TypeDeclaration td = (TypeDeclaration) o;
	       String nm = td.getName().getIdentifier();
	       class_names.put(nm,ff);
	     }
	    break;
	  }
       }
      for (FileFragment ff : frags) {
	 CompilationUnit cu = (CompilationUnit) ff.getAstNode();
	 addDepends(ff,cu);
       }
    }

   List<FileFragment> sort() {
      if (depend_names.isEmpty()) return base_list;
      List<FileFragment> rslt = new ArrayList<FileFragment>();
      Set<FileFragment> done = new HashSet<FileFragment>();
      while (rslt.size() < base_list.size()) {
	 boolean chng = false;
	 FileFragment fst = null;
	 for (FileFragment ff : base_list) {
	    if (done.contains(ff)) continue;
	    if (fst == null) fst = ff;
	    boolean allok = true;
	    List<FileFragment> rqs = depend_names.get(ff);
	    if (rqs != null) {
	       for (FileFragment xf : rqs) {
		  if (!done.contains(xf)) allok = false;
		}
	     }
	    if (allok) {
	       rslt.add(ff);
	       done.add(ff);
	       chng = true;
	     }
	  }
	 if (!chng) {
	    rslt.add(fst);
	    done.add(fst);
	  }
       }
      return rslt;
    }

   private void addDepends(FileFragment ff,CompilationUnit cu) {
      String pkg = null;
      if (cu.getPackage() != null) {
	 pkg = cu.getPackage().getName().getFullyQualifiedName();
       }
      for (Object o : cu.types()) {
	 if (o instanceof TypeDeclaration) {
	    TypeDeclaration td = (TypeDeclaration) o;
	    addDepends(ff,pkg,td);
	  }
       }
    }

   private void addDepends(FileFragment to,String pkg,TypeDeclaration td) {
      addDepend(td.getSuperclassType(),pkg,to);
      for (Object o : td.superInterfaceTypes()) {
	 Type it = (Type) o;
	 addDepend(it,pkg,to);
       }
    }

   private void addDepend(Type t,String pkg,FileFragment to) {
      if (t == null) return;
      if (t.isSimpleType()) {
	 SimpleType st = (SimpleType) t;
	 String tnm = st.getName().getFullyQualifiedName();
	 if (pkg != null && tnm.startsWith(pkg)) tnm = tnm.substring(pkg.length() + 1);
	 int idx = tnm.indexOf(".");
	 if (idx >= 0) tnm = tnm.substring(0,idx);
	 FileFragment frm = class_names.get(tnm);
	 if (frm != null || frm == to) {
	    List<FileFragment> dps = depend_names.get(to);
	    if (dps == null) {
	       dps = new ArrayList<FileFragment>();
	       depend_names.put(to,dps);
	     }
	    dps.add(frm);
	  }
       }
    }

}	// end of inner class FileSorter




/********************************************************************************/
/*										*/
/*	Handle cleanup prior to merge						*/
/*										*/
/********************************************************************************/

private static void fixNameConflicts(CompilationUnit orig,CompilationUnit add)
{
   Set<String> imptyps = new HashSet<String>();
   for (Iterator<?> it = orig.imports().iterator(); it.hasNext(); ) {
      ImportDeclaration id = (ImportDeclaration) it.next();
      if (id.isOnDemand()) continue;
      if (id.isStatic()) continue;
      String nm = id.getName().getFullyQualifiedName();
      imptyps.add(nm);
    }

   Set<String> checks = new HashSet<String>(check_names);
   for (Iterator<?> it = add.imports().iterator(); it.hasNext(); ) {
      ImportDeclaration id = (ImportDeclaration) it.next();
      if (id.isOnDemand()) continue;
      if (id.isStatic()) continue;
      String nm = id.getName().getFullyQualifiedName();
      if (imptyps.contains(nm)) {
	 it.remove();
       }
      else {
	 int idx = nm.lastIndexOf(".");
	 if (idx < 0) continue;
	 String tnm = nm.substring(idx);
	 for (String origtyp : imptyps) {
	    if (origtyp.endsWith(tnm)) {
	       checks.add(nm);
	       it.remove();
	       break;
	     }
	  }
       }
    }
   // check if add uses java.awt.List, javax.swing.filechooser.FileFilter, ...
   // if so, remove any explicit imports and change all internal type references
   // to be fully qualified

   NameChecker nc = new NameChecker(checks);
   add.accept(nc);
}


private static Set<String> check_names;
static {
   check_names = new HashSet<String>();
   check_names.add("java.awt.List");
   check_names.add("javax.swing.filechooser.FileFilter");
   check_names.add("java.sql.Date");
}


private static class NameChecker extends ASTVisitor {

   private Set<String> name_checks;

   NameChecker(Set<String> chks) {
      name_checks = chks;
    }

   @Override public boolean visit(ImportDeclaration d) {
      if (!d.isOnDemand() && !d.isStatic()) {
	 String nm = d.getName().getFullyQualifiedName();
	 if (name_checks.contains(nm)) {
	    CompilationUnit cu = (CompilationUnit) d.getParent();
	    cu.imports().remove(d);
	  }
       }
      return false;
    }

   @Override public boolean visit(QualifiedName n) {
      JcompType jt = JavaAst.getJavaType(n);
      if (jt == null) return true;
      if (!name_checks.contains(jt.getName())) return true;
      return false;
    }

   @SuppressWarnings("unchecked")
   @Override public boolean visit(SimpleName n) {
      JcompType jt = JavaAst.getJavaType(n);
      if (jt != null) {
	 String nm = jt.getName();
	 if (name_checks.contains(nm)) {
	    Name qn = JavaAst.getQualifiedName(n.getAST(),nm);
	    ASTNode par = n.getParent();
	    StructuralPropertyDescriptor spd = n.getLocationInParent();
	    if (spd.isChildProperty()) {
	       Class<?> c = ((ChildPropertyDescriptor) spd).getChildType();
	       if (c != SimpleName.class) {
		  par.setStructuralProperty(spd,qn);
		}
	     }
	    else if (spd.isChildListProperty()) {
	       List<Object> l = (List<Object>) par.getStructuralProperty(spd);
	       int idx = l.indexOf(n);
	       if (idx >= 0) l.set(idx,qn);
	     }
	  }
       }
      return true;
    }

}	// end of inner class NameChecker





/********************************************************************************/
/*										*/
/*	Cloned package fragment 						*/
/*										*/
/********************************************************************************/

private static class ClonedPackageFragment extends PackageFragment {

   private FragmentDelta package_delta;

   ClonedPackageFragment(PackageFragment n,ASTRewrite rw,ITrackedNodePosition pos) {
      super(n,null);
      package_delta = new FragmentDelta(CoseResultType.PACKAGE,n,rw,pos);
      setIsolated(true);
    }

   ClonedPackageFragment(PackageFragment n,String newtext) {
      super(n,null);
      package_delta = new FragmentDelta(CoseResultType.PACKAGE,n,newtext);
      setIsolated(true);
    }

   public void clearResolve() {
      clearResolvedData(root_node);
      root_node = null;
    }

   @Override public String getOriginalText() {
      return package_delta.getSourceText();
    }

   @Override public synchronized ASTNode getAstNode() {
      if (root_node == null) {
	 root_node = (CompilationUnit) package_delta.getAstNode();
	 JavaAst.setSearchRequest(root_node.getRoot(),getSearchRequest());
       }
      return root_node;
    }

   public String getKeyText() {
      String val = super.getKeyText();
      return val;
    }

}	// end of subclass ClonedPackageFragment




/********************************************************************************/
/*										*/
/*	Top-level file fragment (for original source file)			*/
/*										*/
/********************************************************************************/

private static class FileFragment extends FragmentJava {

   private CompilationUnit ast_node;
   private String orig_text;

   FileFragment(LanguageJava lj,S6Request.Search sr,CompilationUnit cu,String text) {
      super(lj,sr);
      ast_node = cu;
      orig_text = text;
    }

   public CoseResultType getFragmentType()	{ return CoseResultType.FILE; }

   ASTNode checkAstNode()			{ return ast_node; }
   @Override public ASTNode getAstNode()	{ return ast_node; }
   @Override public String getOriginalText()	{ return orig_text; }

   public synchronized void resolveFragment() {
      super.resolveFragment();
      JavaAst.setKeep(ast_node,true);
    }

}	// end of subclass FileFragment




/********************************************************************************/
/*										*/
/*	Class fragment from original file					*/
/*										*/
/********************************************************************************/

private static class ClassFragment extends FragmentJava {

   protected AbstractTypeDeclaration ast_node;
   protected boolean full_class;

   ClassFragment(FragmentJava par,AbstractTypeDeclaration td,boolean full) {
      super(par);
      ast_node = td;
      full_class = full;
    }

   public CoseResultType getFragmentType()	{ return CoseResultType.CLASS; }

   ASTNode checkAstNode()			{ return ast_node; }
   @Override public ASTNode getAstNode()	{ return ast_node; }

   void saveAst()				{ }

   public boolean checkInitial(S6Request.Signature sg) {
      AbstractTypeDeclaration td = (AbstractTypeDeclaration) getAstNode();
      if (!Modifier.isStatic(td.getModifiers())) return false;
      if (Modifier.isAbstract(td.getModifiers())) return false;
      return true;
    }

   public boolean checkSignature(S6Request.Signature rsg,S6SignatureType styp) {
      S6Request.ClassSignature cs = rsg.getClassSignature();
      if (cs == null) return false;
      return JavaAst.checkTypeSignature(ast_node,cs,styp,null);
    }

   @Override public boolean fixDependencies(S6SolutionSet ss,S6Solution sol) {
      JavaDepends jd = new JavaDepends(ss,sol,ast_node);
   
      S6Request.ClassSignature csg = ss.getRequest().getSignature().getClassSignature();
      for (Iterator<?> it = ast_node.bodyDeclarations().iterator(); it.hasNext(); ) {
         BodyDeclaration bd = (BodyDeclaration) it.next();
         if (full_class) {
            if (Modifier.isPublic(bd.getModifiers()) || Modifier.isProtected(bd.getModifiers())) {
               jd.addDeclaration(bd);
             }
          }
         else if (bd instanceof MethodDeclaration) {
            MethodDeclaration md = (MethodDeclaration) bd;
            boolean used = false;
            for (S6Request.MethodSignature msg : csg.getMethods()) {
               if (JavaAst.checkMethodSignature(md,msg,S6SignatureType.FULL)) {
        	  jd.addDeclaration(md);
        	  used = true;
        	  break;
        	}
             }
            if (!used && csg.includeTestCases()) {
               if (md.getBody() != null && md.getBody().statements().size() > 0) {
        	  if (md.getName().getIdentifier().startsWith("test")) {
        	     jd.addDeclaration(bd);
        	   }
        	  else {
        	     for (Object o : md.modifiers()) {
        		IExtendedModifier am = (IExtendedModifier) o;
        		if (am.isAnnotation()) {
        		   Annotation aa = (Annotation) am;
        		   String fqn = aa.getTypeName().getFullyQualifiedName();
        		   if (fqn.startsWith("org.junit.")) {
        		      jd.addDeclaration(bd);
        		      break;
        		    }
        		 }
        	      }
        	   }
        	}
             }
   
          }
       }
      if (!jd.findDependencies()) return false;
   
      Collection<BodyDeclaration> uses = jd.getDeclarations();
      boolean chng = false;
      int usect = 0;
      ASTRewrite rw = ASTRewrite.create(ast_node.getAST());
      ITrackedNodePosition basepos = rw.track(ast_node);
      ListRewrite lrw = null;
   
      for (Iterator<?> it = ast_node.bodyDeclarations().iterator(); it.hasNext(); ) {
         BodyDeclaration bd = (BodyDeclaration) it.next();
         if (!uses.contains(bd)) {
            if (lrw == null) {
               lrw = rw.getListRewrite(ast_node,ast_node.getBodyDeclarationsProperty());
             }
            lrw.remove(bd,null);
            // it.remove();
            chng = true;
          }
         else {
            uses.remove(bd);
            ++usect;
          }
       }
      uses.remove(ast_node);
      usect += uses.size();
   
      if (chng && usect == 0) {
         // System.err.println("DEPEND REMOVED ALL = false");
         return false;
       }
   
      ClassFragment rsltfrag = this;
   
      if (lrw != null) {
         rsltfrag = (ClassFragment) cloneFragment(rw,basepos);
         sol.updateFragment(rsltfrag);
       }
   
      for (BodyDeclaration bd : uses) {
         rsltfrag.addHelper(bd);
       }
   
      rsltfrag.saveAst();
   
      rsltfrag.use_constructor = false; // set only if we need something outside of the class
   
      rsltfrag.import_set = jd.getImportTypes();
   
      return true;
   }

   FragmentJava cloneFragment(ASTNode n) {
      ClassFragment cf = new ClassFragment(getJavaParent(),(AbstractTypeDeclaration) n,full_class);
      setIsolated(true);
      return cf;
    }

   @Override public FragmentJava cloneFragment(ASTRewrite rw,ITrackedNodePosition pos) {
      return new ClonedClassFragment(this,rw,pos,full_class);
    }

   @Override public String getOriginalText() {
      return getJavaParent().getOriginalText();
    }

   protected void localIsolate() {
      ast_node = (AbstractTypeDeclaration) TransformJava.copyAst(ast_node);
      JavaAst.setSearchRequest(ast_node.getRoot(),getSearchRequest());
      // resolveFragment();
    }

}	// end of subclass ClassFragment





/********************************************************************************/
/*										*/
/*	Cloned class fragment							*/
/*										*/
/********************************************************************************/

private static class ClonedClassFragment extends ClassFragment {

   private FragmentDelta class_delta;
   private boolean save_ast;

   ClonedClassFragment(ClassFragment n,ASTRewrite rw,ITrackedNodePosition pos,boolean full) {
      super(n.getJavaParent(),null,full);
      class_delta = new FragmentDelta(CoseResultType.CLASS,n,rw,pos);
      setIsolated(true);
      save_ast = false;
    }

   public void clearResolve() {
      clearResolvedData(ast_node);
      if (!save_ast)
	 ast_node = null;
    }

   @Override void saveAst()				{ save_ast = true; }

   @Override public String getOriginalText() {
      return class_delta.getSourceText();
    }

   @Override public synchronized ASTNode getAstNode() {
      if (ast_node == null) {
	 ast_node = (AbstractTypeDeclaration) class_delta.getAstNode();
	 if (ast_node != null) {
	    JavaAst.setSearchRequest(ast_node.getRoot(),getSearchRequest());
	  }
       }
      return ast_node;
    }

   public String getKeyText() {
      boolean clr = (ast_node == null);
      String val = super.getKeyText();
      if (clr && !save_ast) ast_node = null;
      return val;
    }

}	// end of subclass ClonedClassFragment



/********************************************************************************/
/*										*/
/*	Method fragment from original file					*/
/*										*/
/********************************************************************************/

private static class MethodFragment extends FragmentJava {

   protected MethodDeclaration ast_node;

   MethodFragment(FragmentJava par,MethodDeclaration md) {
      super(par);
      ast_node = md;
    }

   public CoseResultType getFragmentType()	{ return CoseResultType.METHOD; }

   ASTNode checkAstNode()			{ return ast_node; }
   @Override public ASTNode getAstNode()	{ return ast_node; }

   public boolean checkSignature(S6Request.Signature rsg,S6SignatureType styp) {
      S6Request.MethodSignature ms = rsg.getMethodSignature();
      if (ms == null) return false;
      return JavaAst.checkMethodSignature(ast_node,ms,styp);
    }

   @Override public boolean fixDependencies(S6SolutionSet ss,S6Solution sol) {
      JavaDepends jd = new JavaDepends(ss,sol,ast_node);
      jd.addDeclaration(ast_node);
      if (!jd.findDependencies()) return false;
   
      for (BodyDeclaration bd : jd.getDeclarations()) {
         if (bd != ast_node) addHelper(bd);
       }
      use_constructor = jd.getUseConstructor();
      import_set = jd.getImportTypes();
      return true;
    }

   FragmentJava cloneFragment(ASTNode n) {
      MethodFragment mf = new MethodFragment(getJavaParent(),(MethodDeclaration) n);
      setIsolated(true);
      return mf;
    }

   @Override public FragmentJava cloneFragment(ASTRewrite rw,ITrackedNodePosition pos) {
      return new ClonedMethodFragment(this,rw,pos);
    }

   @Override public String getOriginalText() {
      return getJavaParent().getOriginalText();
    }

   protected void localIsolate() {
      ast_node = (MethodDeclaration) TransformJava.copyAst(ast_node);
      JavaAst.setSearchRequest(ast_node.getRoot(),getSearchRequest());
      // resolveFragment();
    }

}	// end of subclass MethodFragment





/********************************************************************************/
/*										*/
/*	Cloned method fragment							*/
/*										*/
/********************************************************************************/

private static class ClonedMethodFragment extends MethodFragment {

   private FragmentDelta method_delta;

   ClonedMethodFragment(MethodFragment n,ASTRewrite rw,ITrackedNodePosition pos) {
      super(n.getJavaParent(),null);
      method_delta = new FragmentDelta(CoseResultType.METHOD,n,rw,pos);
      setIsolated(true);
    }

   @Override public void clearResolve() {
      super.clearResolve();
      ast_node = null;
    }

   @Override public String getOriginalText() {
      return method_delta.getSourceText();
    }

   @SuppressWarnings("unchecked")
   @Override public synchronized ASTNode getAstNode() {
      if (ast_node == null) {
         ast_node = (MethodDeclaration) method_delta.getAstNode();
         if (context_fragments != null && context_fragments.size() > 0) {
            ASTNode p = ast_node;
            while (p != null && !(p instanceof AbstractTypeDeclaration)) p = p.getParent();
            if (p == null) return null;
            AbstractTypeDeclaration atd = (AbstractTypeDeclaration) p;
            List<ASTNode> decls = atd.bodyDeclarations();
            for (ASTNode n : context_fragments) {
               ASTNode nbd = ASTNode.copySubtree(ast_node.getAST(),n);
               decls.add(nbd);
             }
          }
         if (ast_node != null) JavaAst.setSearchRequest(ast_node.getRoot(),getSearchRequest());
         else System.err.println("NULL AST NODE");
       }
      return ast_node;
    }

   public String getKeyText() {
      boolean clr = (ast_node == null);
      String val = super.getKeyText();
      if (clr) ast_node = null;
      return val;
    }

}	// end of subclass ClonedMethodFragment




/********************************************************************************/
/*										*/
/*	Class to handle saving state and getting AST				*/
/*										*/
/********************************************************************************/

private static class FragmentDelta {

   private CoseResultType fragment_type;
   private ASTRewrite use_rewrite;
   private TextEdit text_edit;
   private ITrackedNodePosition node_position;
   private int node_start;
   private int node_length;
   private FragmentJava from_fragment;
   private String saved_text;
   private TextDelta text_delta;

   FragmentDelta(CoseResultType typ,FragmentJava n,ASTRewrite rw,ITrackedNodePosition pos) {
      fragment_type = typ;
      from_fragment = n;
      use_rewrite = rw;
      node_position = pos;
      text_edit = null;
      node_start = 0;
      node_length = 0;
      saved_text = null;
      text_delta = null;
      getSourceText();
    }

   FragmentDelta(CoseResultType typ,FragmentJava par,String newtext) {
      fragment_type = typ;
      from_fragment = par;
      use_rewrite = null;
      node_position = null;
      text_edit = null;
      node_start = 0;
      node_length = 0;
      text_delta = null;
      saved_text = null;
      if (use_deltas) {
	 String origtext = par.getOriginalText();
	 text_delta = TextDelta.getDelta(newtext,origtext);
	 String ntxt = text_delta.apply(origtext);
	 if (!ntxt.equals(newtext)) {
	    System.err.println("BAD DELTA");
	  }
       }
      else saved_text = newtext;
    }

   protected synchronized String getSourceText() {
      if (saved_text == null && text_delta == null) {
	 String origtext = from_fragment.getOriginalText();
	 Document d = new Document(origtext);

	 if (text_edit == null) {
	    try {
	       text_edit = use_rewrite.rewriteAST(d,null);
	       node_start = node_position.getStartPosition();
	       node_length = node_position.getLength();
	       use_rewrite = null;		// these are no longer needed
	       // node_position = null;
	       if (!use_deltas) from_fragment = null;
	     }
	    catch (Throwable t) {
	       System.err.println("FRAGMENT TEXT PROBLEM: " + t);
	     }
	  }
	 if (text_edit == null) return origtext;
	 try {
	    // System.err.println("EDITS: " + text_edit);
	    text_edit.apply(d);
	    node_start = node_position.getStartPosition();
	    node_length = node_position.getLength();
	    node_position = null;
	    text_edit = null;			// if we save doc, this isn't needed either
	  }
	 catch (Throwable e) {
	    System.err.println("FRAGMENT EDIT PROBLEM: " + e);
	    e.printStackTrace();
	  }

	 String newtext = d.get();

	 if (use_deltas) {
	    text_delta = TextDelta.getDelta(newtext,origtext);
	    String ntxt = text_delta.apply(origtext);
	    if (!ntxt.trim().equals(newtext.trim())) {
	       text_delta = TextDelta.getDelta(newtext,origtext);
	       ntxt = text_delta.apply(origtext);
	       System.err.println("BAD DELTA");
	       File f1 = new File("Delta.orig");
	       File f2 = new File("Delta.new");
	       File f3 = new File("Delta.result");
	       try {
		  FileWriter fw = new FileWriter(f1);
		  fw.write(origtext);
		  fw.close();
		  fw = new FileWriter(f2);
		  fw.write(newtext);
		  fw.close();
		  fw = new FileWriter(f3);
		  fw.write(ntxt);
		  fw.close();
		  System.exit(1);
		}
	       catch (IOException e) { }

	       System.err.println("DELTA1: " + ntxt);
	       System.err.println("DELTA2: " + newtext);
	     }
	  }
	 else saved_text = newtext;

	 return newtext;
       }
      if (saved_text == null && text_delta != null) {
	 String origtext = from_fragment.getOriginalText();
	 return text_delta.apply(origtext);
       }

      return saved_text;
    }



   ASTNode getAstNode() {
      String txt = getSourceText();
      ASTParser parser = ASTParser.newParser(AST.JLS17);
      parser.setKind(ASTParser.K_COMPILATION_UNIT);
      Map<String,String> options = JavaCore.getOptions();
      JavaCore.setComplianceOptions(JavaCore.VERSION_1_7,options);
      parser.setCompilerOptions(options);
      parser.setSource(txt.toCharArray());
      parser.setResolveBindings(false);
      CompilationUnit cu = (CompilationUnit) parser.createAST(null);
      PositionFinder pf = new PositionFinder(fragment_type,node_start,node_length);
      cu.accept(pf);
      if (pf.getAstNode() == null) {
         System.err.println("FRAGMENT: COULDN'T FIND AST NODE: " + node_start + " " + node_length + " " + txt);
         cu.accept(pf);
       }
   
      return pf.getAstNode();
    }

}	// end of subclass FragmentDelta





/********************************************************************************/
/*										*/
/*	Methods to find an AST node from a tree and position			*/
/*										*/
/********************************************************************************/

private static class PositionFinder extends ASTVisitor {

   private CoseResultType fragment_type;
   private ASTNode found_node;
   private int start_pos;
   private int node_len;

   PositionFinder(CoseResultType st,int pos,int len) {
      fragment_type = st;
      start_pos = pos;
      node_len = len;
      found_node = null;
    }

   ASTNode getAstNode() 		{ return found_node; }

   @Override public boolean visit(MethodDeclaration n) {
      if (fragment_type != CoseResultType.METHOD) return false;
      if (n.getBody() == null) return false;
      checkNode(n);
      return false;
    }

   @Override public boolean visit(TypeDeclaration n) {
      if (fragment_type != CoseResultType.CLASS) return true;
      if (n.isInterface()) return false;
      checkNode(n);
      return true;
    }

   @Override public boolean visit(CompilationUnit n) {
      if (fragment_type != CoseResultType.PACKAGE) return true;
      checkNode(n);
      return false;
    }

   private void checkNode(ASTNode n) {
      CompilationUnit cu = (CompilationUnit) n.getRoot();
      int s0 = cu.getExtendedStartPosition(n);
      int l0 = cu.getExtendedLength(n);
      int s1 = n.getStartPosition();
      n.getLength();

      int dl0 = start_pos - s0;
      if (dl0 == 0) found_node = n;
      else if (start_pos == s1) found_node = n;
      else if (found_node == null && dl0 < 0) found_node = n;
      else if (found_node == null) {
	 int d1a = start_pos+node_len;
	 int d1b = s0 + l0;
	 if (d1a > d1b) d1a = d1b;
	 int overlap = d1b - start_pos;
	 if (overlap > 0 && overlap > l0 / 2) {
	    found_node = n;
	  }
       }
    }

}	// end of subclass PositionFinder





/********************************************************************************/
/*										*/
/*	Visitor to handle localization						*/
/*										*/
/********************************************************************************/

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





}	// end of class FragmentJava




/* end of FragmentJava.java */



