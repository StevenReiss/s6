/********************************************************************************/
/*										*/
/*		TransformJava.java						*/
/*										*/
/*	Common methods for Java-based transforms				*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/language/java/TransformJava.java,v 1.23 2016/07/22 13:31:07 spr Exp $ */


/*********************************************************************************
 *
 * $Log: TransformJava.java,v $
 * Revision 1.23  2016/07/22 13:31:07  spr
 * Fixups for framework search.
 *
 * Revision 1.22  2016/07/18 23:05:26  spr
 * Update transforms for applications and UI.
 *
 * Revision 1.21  2015/09/23 17:54:53  spr
 * Version to handle andriod UI applications.
 *
 * Revision 1.20  2015/02/19 23:33:18  spr
 * Parallelize setting up new solutions for classes.
 *
 * Revision 1.19  2015/02/18 23:24:01  spr
 * Fixup test case search and generation.	Flag lack of concurrency.
 *
 * Revision 1.18  2015/02/14 19:40:19  spr
 * Add test case generation.
 *
 * Revision 1.17  2014/08/29 15:16:09  spr
 * Updates for suise, testcases.
 *
 * Revision 1.16  2013/09/13 20:33:05  spr
 * Add calls for UI search.
 *
 * Revision 1.15  2013-05-09 12:26:21  spr
 * Minor changes to start ui fixups.
 *
 * Revision 1.14  2012-08-13 16:51:51  spr
 * Add new transforms, clean up code, bug fixes.
 *
 * Revision 1.13  2012-07-20 22:15:20  spr
 * New transforms and resolution for UI search
 *
 * Revision 1.12  2012-06-20 12:21:33  spr
 * Initial fixes for UI search
 *
 * Revision 1.11  2012-06-11 18:40:26  spr
 * Updated transform for ui serach
 *
 * Revision 1.10  2012-06-11 14:07:49  spr
 * add framework search; fix bugs
 *
 * Revision 1.9  2009-09-18 01:41:36  spr
 * Handle user testing.
 *
 * Revision 1.8  2009-05-12 22:28:48  spr
 * Fix ups to make user context work.
 *
 * Revision 1.7  2008-11-12 13:52:14  spr
 * Performance and bug updates.
 *
 * Revision 1.6  2008-08-28 00:32:56  spr
 * Next version of S6.	Lots of bug fixes, some new functionality.
 *
 * Revision 1.5  2008-07-18 22:27:09  spr
 * Handle remove compilation calls; update transforms to include code to use ASTrewrite.
 *
 * Revision 1.4  2008-07-17 13:46:46  spr
 * Bug fixes and speed ups.
 *
 * Revision 1.3  2008-06-27 15:45:41  spr
 * Minor bug fixes to transforms.
 *
 * Revision 1.2  2008-06-12 17:47:52  spr
 * Next version of S6.
 *
 * Revision 1.1.1.1  2008-06-03 12:59:23  spr
 * Initial version of S6
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.language.java;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ChildPropertyDescriptor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;

import edu.brown.cs.ivy.jcomp.JcompSymbol;
import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.cose.cosecommon.CoseConstants.CoseResultType;
import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Fragment;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6Solution;
import edu.brown.cs.s6.common.S6SolutionSet;
import edu.brown.cs.s6.common.S6Transform;
import edu.brown.cs.s6.language.TransformBase;


abstract class TransformJava extends TransformBase implements S6Constants, JavaConstants {




/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private static Set<String> KEEP_PROPERTIES;


static {
   KEEP_PROPERTIES = new HashSet<String>();
}



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

protected TransformJava(String name)
{
   super(name);
}








/********************************************************************************/
/*										*/
/*	Methods to construct set of fragments already done			*/
/*										*/
/********************************************************************************/

protected void findFragmentsDone(S6SolutionSet ss,Iterable<S6Solution> sols,Set<S6Fragment> done)
{
   addPreviousTransforms(ss,sols,done);
}



protected final void addPreviousTransforms(S6SolutionSet solset,Iterable<S6Solution> sols,Set<S6Fragment> done)
{
   for (S6Solution ss : sols) {
      int dup = 0;
      S6Fragment fg = ss.getFragment();
      if (ss.getTransforms() != null) {
	 for (S6Transform.Memo m : ss.getTransforms()) {
	    if (getName().equals(m.getTransformName())) ++dup;
	  }
       }
      if (dup >= 1) {
	 done.add(fg);
	 if (ss.getParentFragment() != null) done.add(ss.getParentFragment());
	 done.add(ss.getBaseFragment());
       }
      else if (!(fg instanceof JavaFragment)) done.add(fg);
    }
}



protected final void removeNonMethods(Iterable<S6Solution> sols,Set<S6Fragment> done)
{
   for (S6Solution ss : sols) {
      S6Fragment fg = ss.getFragment();
      if (fg.getFragmentType() != CoseResultType.METHOD) done.add(fg);
    }
}


protected final boolean usesTransform(S6Solution ss,String nm)
{
   if (ss.getTransforms() == null) return false;

   for (S6Transform.Memo m : ss.getTransforms()) {
      if (nm.equals(m.getTransformName())) return true;
    }

   return false;
}




/********************************************************************************/
/*										*/
/*	Methods to apply transform to a particular solution			*/
/*										*/
/********************************************************************************/

@Override public boolean applyTransform(S6SolutionSet solset,S6Solution sol)
{
   boolean chng = false;
   S6Fragment fg = sol.getFragment();

   switch (fg.getFragmentType()) {
      case FILE :
	 chng = applyFileTransform(solset,sol);
	 break;
      case CLASS :
	 chng = applyClassTransform(solset,sol);
	 break;
      case METHOD :
	 chng = applyMethodTransform(solset,sol);
	 break;
      case PACKAGE :
	 switch (solset.getSearchType()) {
	    case PACKAGE :
	    case APPLICATION :
	       chng = applyPackageTransform(solset,sol);
	       break;
	    case UIFRAMEWORK :
	    case ANDROIDUI :
	       chng = applyPackageTransform(solset,sol);
	       break;
	    default :
	       break;
	  }
	 break;
    }

   return chng;
}



protected boolean applyFileTransform(S6SolutionSet solset,S6Solution sol)	{ return false; }




/********************************************************************************/
/*										*/
/*	Handle package transformation						*/
/*										*/
/********************************************************************************/

protected boolean applyPackageTransform(S6SolutionSet solset,S6Solution sol)
{
   JavaFragment f = (JavaFragment) sol.getFragment();
   S6Request.Search sr = solset.getRequest();
   S6Request.PackageSignature psg = (S6Request.PackageSignature) sr.getSignature();
   CompilationUnit cu = (CompilationUnit) f.getAstNode();
   boolean chng = false;

   Collection<TreeMapper> tms = findPackageMappings(solset,sol,cu,psg);
   if (tms != null) {
      for (TreeMapper tm : tms) {
	 chng |= addNewSolution(solset,sol,tm);
       }
      return chng;
    }

   for (S6Request.ClassSignature csg : psg.getClasses()) {
      boolean isok = false;
      for (Object otd : cu.types()) {
	 if (!(otd instanceof TypeDeclaration)) continue;
	 TypeDeclaration td = (TypeDeclaration) otd;
	 boolean fg = JavaAst.checkTypeSignature(td,csg,S6SignatureType.NAME,null);
	 if (fg) {
            if (!checkApplyClassForPackage(solset,cu,csg,td)) continue;
            chng |= applyClassTransform(solset,sol,td,csg);
            isok = true;
            break;
          }
       }
      if (!isok) break;
    }

   return chng;
}



protected Collection<TreeMapper> findPackageMappings(S6SolutionSet ss,S6Solution sol,CompilationUnit cu,S6Request.PackageSignature sg)
{
   TreeMapper tm = findPackageMapping(ss,cu,sg,sol);
   if (tm == null) return null;
   Collection<TreeMapper> tms = new ArrayList<TreeMapper>();
   tms.add(tm);
   return tms;
}


protected TreeMapper findPackageMapping(S6SolutionSet sset,CompilationUnit cu,S6Request.PackageSignature sg,S6Solution sol)
{
   return null;
}


protected boolean checkApplyClassForPackage(S6SolutionSet sols,CompilationUnit cu,
					       S6Request.ClassSignature csg,
					       AbstractTypeDeclaration td)
{
   return true;
}





/********************************************************************************/
/*										*/
/*	Default transform methods						*/
/*										*/
/********************************************************************************/

protected boolean applyMethodTransform(S6SolutionSet solset,S6Solution sol)
{
   JavaFragment f = (JavaFragment) sol.getFragment();
   S6Request.Search sr = solset.getRequest();
   S6Request.MethodSignature ms = sr.getSignature().getMethodSignature();
   if (ms == null) return false;

   MethodDeclaration md = (MethodDeclaration) f.getAstNode();

   Collection<TreeMapper> tmaps = findMethodMappings(solset,md,ms,sol);
   if (tmaps == null || tmaps.size() == 0) return false;

   boolean chng = false;
   for (TreeMapper tm : tmaps) {
      if (!usesTransform(sol,tm.getMapName())) {
	 chng |= addNewSolution(solset,sol,tm);
       }
    }

   return chng;
}



private boolean applyClassTransform(S6SolutionSet sols,S6Solution sol)
{
   FragmentJava f = (FragmentJava) sol.getFragment();
   S6Request.Search sr = sols.getRequest();
   S6Request.ClassSignature csg = sr.getSignature().getClassSignature();
   TypeDeclaration td = (TypeDeclaration) f.getAstNode();

   return applyClassTransform(sols,sol,td,csg);
}



protected boolean applyClassTransform(S6SolutionSet sols,S6Solution sol,
					 TypeDeclaration td,S6Request.ClassSignature csg)
{
   boolean chng = false;

   if (td == null) return false;

   Collection<TreeMapper> ctms = findClassMappings(sols,td,csg,sol);
   Queue<Future<Boolean>> waitfors = new LinkedList<Future<Boolean>>();

   if (ctms != null && ctms.size() > 0) {
      // TODO: this should be parallelized
      for (TreeMapper ctm : ctms) {
	 if (!usesTransform(sol,ctm.getMapName())) {
	    SolutionAdder sadder = new SolutionAdder(sols,sol,ctm);
	    Future<Boolean> fb = sols.getEngine().executeTask(S6TaskType.COMPUTE,sadder);
	    synchronized (waitfors) {
	       waitfors.add(fb);
	     }
	    // chng |= addNewSolution(sols,sol,ctm);
	  }
       }
      if (waitfors.size() > 0) {
	 chng |= sols.getEngine().waitForAll(waitfors);
       }
    }

   for (S6Request.MethodSignature msg : csg.getMethods()) {
      boolean isok = false;
      for (MethodDeclaration md : td.getMethods()) {
	 if (Modifier.isAbstract(md.getModifiers())) continue;
	 if (!checkApplyMethodForClass(sols,sol,td,msg,md)) continue;
         boolean fg =  JavaAst.checkMethodSignature(md,msg,S6SignatureType.FULL);
         if (fg) {
            Collection<TreeMapper> tmaps = findMethodMappings(sols,md,msg,sol);
            if (tmaps != null) {
               for (TreeMapper tm : tmaps) {
                  if (!usesTransform(sol,tm.getMapName())) {
                     chng |= addNewSolution(sols,sol,tm);
                   }
                }
             }
            isok = true;
            break;
          }
       }
      if (!isok) break;
    }

   for (S6Request.FieldSignature fsg : csg.getFields()) {
      boolean isok =false;
      for (FieldDeclaration fd : td.getFields()) {
	 for (Object o : fd.fragments()) {
	    VariableDeclarationFragment vdf = (VariableDeclarationFragment) o;
            boolean fg = JavaAst.checkFieldSignature(vdf,fsg,S6SignatureType.FULL);
	    if (!checkApplyFieldForClass(sols,td,csg,fsg,vdf)) continue;
            if (fg) {
               Collection<TreeMapper> tmaps = findFieldMappings(sols,vdf,fsg);
               isok = true;
               if (tmaps != null) {
                  for (TreeMapper tm : tmaps) {
                     if (!usesTransform(sol,tm.getMapName())) {
                        chng |= addNewSolution(sols,sol,tm);
                      }
                   }
                }
               break;
             }
	  }
       }
      if (!isok) break;
    }

   return chng;
}


private class SolutionAdder implements Callable<Boolean> {

   private S6SolutionSet solution_set;
   private S6Solution original_solution;
   private TreeMapper tree_mapper;

   SolutionAdder(S6SolutionSet sols,S6Solution sol,TreeMapper tm) {
      solution_set = sols;
      original_solution = sol;
      tree_mapper = tm;
    }

   @Override public Boolean call() {
      boolean chng = addNewSolution(solution_set,original_solution,tree_mapper);
      return chng;
    }

}	// end of inner class SolutionAdder




protected List<TreeMapper> findClassMappings(S6SolutionSet sols,
      TypeDeclaration td,S6Request.ClassSignature csg,S6Solution sol)
{
   TreeMapper tm = findClassMapping(sols,td,csg,sol);
   if (tm == null) return null;
   List<TreeMapper> rslt = new ArrayList<TreeMapper>();
   rslt.add(tm);
   return rslt;
}



protected TreeMapper findClassMapping(S6SolutionSet sols,
      TypeDeclaration td,S6Request.ClassSignature csg,S6Solution sol)
{
   return null;
}



protected boolean checkApplyMethodForClass(S6SolutionSet sols,S6Solution sol,
      TypeDeclaration td,S6Request.MethodSignature msg,MethodDeclaration md)
{
   return true;
}


protected Collection<TreeMapper> findMethodMappings(S6SolutionSet solset,MethodDeclaration md,
						       S6Request.MethodSignature ms,
						       S6Solution sol)
{
   TreeMapper tm = findMethodMapping(solset,md,ms,sol);
   if (tm == null) return null;

   ArrayList<TreeMapper> tms = new ArrayList<TreeMapper>();
   tms.add(tm);
   return tms;
}



protected TreeMapper findMethodMapping(S6SolutionSet solset,MethodDeclaration md,
					  S6Request.MethodSignature ms,S6Solution sol)
{
   return null;
}



protected boolean checkApplyFieldForClass(S6SolutionSet sols,TypeDeclaration td,
      S6Request.ClassSignature cs,
      S6Request.FieldSignature fs,
      VariableDeclarationFragment vdf)
{
   return true;
}


protected Collection<TreeMapper> findFieldMappings(S6SolutionSet solset,
						      VariableDeclarationFragment vdf,
						      S6Request.FieldSignature fs)
{
   TreeMapper tm = findFieldMapping(solset,vdf,fs);
   if (tm == null) return null;

   ArrayList<TreeMapper> tms = new ArrayList<TreeMapper>();
   tms.add(tm);
   return tms;
}



protected TreeMapper findFieldMapping(S6SolutionSet solset,
					 VariableDeclarationFragment vdf,
					 S6Request.FieldSignature fs)
{
   return null;
}




/********************************************************************************/
/*										*/
/*	Methods for adding a new solution					*/
/*										*/
/********************************************************************************/

protected boolean addNewSolution(S6SolutionSet sols,S6Solution sol,TreeMapper tmap)
{
   JavaFragment f = (JavaFragment) sol.getFragment();
   JavaFragment nf1 = null;

   if (usesTransform(sol,tmap.getMapName())) {
      return false;
    }

   JavaMemo memo = tmap.getMapMemo(f,getName());
   if (memo.getNewText() == null) {
      nf1 = f.cloneFragment(memo.getRewrite(),memo.getPosition());
      // System.err.println("RESULT: " + nf1.getText());
      memo.clear();
    }
   else {
      nf1 = f.cloneFragment(memo.getNewText());
    }

   S6Solution ns = sol.createNewSolution(nf1,memo);
   S6SolutionFlag sfg = tmap.getSolutionFlag();
   if (sfg != null) ns.setFlag(sfg);
   if (sol.checkFlag(S6SolutionFlag.COMPILES)) {
      ns.setFlag(S6SolutionFlag.COMPILES);
    }

   S6Solution xs = sols.add(ns);
   if (xs != null && xs != ns) {
      if (sols.checkClearResolve()) ns.clearResolve();
      if (sols.doDebug()) {
	 System.err.println("DUPLICATE TRANSFORMS: ");
	 System.err.print("    NEW: " + ns.checkFlag(S6SolutionFlag.REMOVE) + " ");
	 for (S6Transform.Memo m : ns.getTransforms()) {
	    System.err.print(" " + m.getTransformName());
	  }
	 System.err.println();
	 System.err.print("    OLD: " + xs.checkFlag(S6SolutionFlag.REMOVE) + " ");
	 if (xs.getTransforms() != null) {
	    for (S6Transform.Memo m : xs.getTransforms()) {
	       System.err.print(" " + m.getTransformName());
	     }
	  }
	 System.err.println();
       }
      return false;
    }

   if (sols.doDebug() && xs != null) {
      System.err.println("TRANSFORM " + tmap.getMapName() + ": " + f.getLocalText());
      System.err.println("RESULT = " + nf1.getLocalText());
      System.err.print("USING:" );
      for (S6Transform.Memo m : ns.getTransforms()) {
	 System.err.print(" " + m.getTransformName());
       }
      System.err.println();
      System.err.println("SOURCE: " + sol.getSource().getName() + " " + sol.getScore());
    }

   if (sols.checkClearResolve()) {
      ns.clearResolve();
    }

   return true;
}




static ASTNode copyAst(ASTNode base)
{
   TreeCopy tc = new TreeCopy(base);

   tc.copyTree(base.getRoot());

   return tc.getNewBase();
}



static ASTNode dupNode(AST ast,ASTNode n)
{
   TreeCopy tc = new TreeCopy(ast);
   return tc.copyTree(n);
}




/********************************************************************************/
/*										*/
/*	Class for actually duplicating and modifying an AST			*/
/*										*/
/********************************************************************************/

private static class TreeCopy {

   private AST new_ast;
   private ASTNode old_base;
   private ASTNode new_base;

   TreeCopy(ASTNode base) {
      new_ast = AST.newAST(AST.JLS8);
      old_base = base; 
      new_base = null;
    }

   TreeCopy(AST ast) {
      new_ast = ast;
      old_base = null;
      new_base = null;
    }

   ASTNode getNewBase() 		{ return new_base; }

   @SuppressWarnings("unchecked")
   ASTNode copyTree(ASTNode n) {
      if (n == null) return null;
      ASTNode nn = new_ast.createInstance(n.getNodeType());
      nn.setFlags(n.getFlags());
      nn.setSourceRange(n.getStartPosition(),n.getLength());
      for (Iterator<?> it = n.properties().entrySet().iterator(); it.hasNext(); ) {
	 Map.Entry<?,?> ent = (Map.Entry<?,?>) it.next();
	 String k = (String) ent.getKey();
	 if (KEEP_PROPERTIES.contains(k)) nn.setProperty(k,ent.getValue());
       }

      for (Iterator<?> it = n.structuralPropertiesForType().iterator(); it.hasNext(); ) {
	 StructuralPropertyDescriptor spd = (StructuralPropertyDescriptor) it.next();
	 if (spd.isSimpleProperty()) {
	    nn.setStructuralProperty(spd,n.getStructuralProperty(spd));
	  }
	 else if (spd.isChildProperty()) {
	    ChildPropertyDescriptor cpd = (ChildPropertyDescriptor) spd;
	    ASTNode cn = (ASTNode) n.getStructuralProperty(spd);
	    if (cn == null) nn.setStructuralProperty(spd,null);
	    else {
	       ASTNode ncn = copyTree(cn);
	       if (ncn != null) nn.setStructuralProperty(spd,ncn);
	       else if (!cpd.isMandatory()) nn.setStructuralProperty(spd,null);
	     }
	  }
	 else {
	    List<ASTNode> lncn = (List<ASTNode>) nn.getStructuralProperty(spd);
	    List<?> lcn = (List<?>) n.getStructuralProperty(spd);
	    for (Iterator<?> it1 = lcn.iterator(); it1.hasNext(); ) {
	       ASTNode cn = (ASTNode) it1.next();
	       ASTNode ncn = copyTree(cn);
	       if (ncn != null) lncn.add(ncn);
	     }
	  }
       }

      if (n == old_base) new_base = nn;

      return nn;
    }

}	// end of subclass TreeCopy




/********************************************************************************/
/*										*/
/*	Class for finding rewrites for an AST					*/
/*										*/
/********************************************************************************/

private static class TreeRewrite extends ASTVisitor {

   private TreeMapper tree_mapper;
   private ASTRewrite tree_rewrite;
   private ITrackedNodePosition base_pos;

   TreeRewrite(ASTNode base,TreeMapper tm) {
      tree_mapper = tm;
      tree_rewrite = ASTRewrite.create(base.getAST());
      base_pos = tree_rewrite.track(base);
    }

   public void preVisit(ASTNode n) {
      tree_mapper.preVisit(n);
    }

   public void postVisit(ASTNode n) {
      tree_mapper.rewriteTree(n,tree_rewrite);
    }

   JavaMemo createMemo(String nm) {
      // System.err.println("REWRITE: " + tree_rewrite);
      return new JavaMemo(tree_mapper.getMapName(),nm,tree_rewrite,base_pos);
    }

}	// end of subclass TreeRewrite




/********************************************************************************/
/*										*/
/*	Class for doing AST-based transforms					*/
/*										*/
/********************************************************************************/

protected abstract class TreeMapper {

   protected JavaMemo saved_memo;

   protected TreeMapper() {
      saved_memo = null;
    }

   final String getMapName() {
      String nm = getName();
      String sn = getSpecificsName();
      if (sn == null) return nm;
      return nm + "@" + sn;
    }
   protected String getSpecificsName()		{ return null; }

   void preVisit(ASTNode n)					{ }

   S6SolutionFlag getSolutionFlag()		{ return null; }

   abstract void rewriteTree(ASTNode orig,ASTRewrite rw);

   JavaMemo getMapMemo(JavaFragment fj,String nm) {
      if (saved_memo == null) {
         ASTNode base = fj.getAstNode();
         try {
            TreeRewrite tr = new TreeRewrite(base,this);
            base.getRoot().accept(tr);
            saved_memo = tr.createMemo(nm);
          }
         catch (IllegalArgumentException e) {
            // try again
            TreeRewrite tr = new TreeRewrite(base,this);
            base.getRoot().accept(tr);
            saved_memo = tr.createMemo(nm);
          }
       }
      return saved_memo;
    }

    void createTextMemo(JavaFragment f,String nm) {
       JavaFragment nf1 = null;
       JavaMemo memo = getMapMemo(f,nm);
       if (memo.getNewText() != null) return;
       nf1 = f.cloneFragment(memo.getRewrite(),memo.getPosition());
       memo.clear();
       if (nf1 == null) return;
       saved_memo = new JavaMemo(getMapName(),nm,nf1.getOriginalText());
     }

}	// end of subinterface TreeMapper





/********************************************************************************/
/*										*/
/*	Check for whether a parameter can be generalized			*/
/*										*/
/********************************************************************************/

protected boolean checkNoInternals(ASTNode n,SingleVariableDeclaration svd)
{
   JcompSymbol js = JavaAst.getDefinition(svd);
   if (js == null) return true;

   InternalChecker ic = new InternalChecker(js);
   n.accept(ic);

   return ic.useNoInternals();
}



private static class InternalChecker extends ASTVisitor {

   private boolean use_internals;
   private JcompSymbol for_symbol;
   private JcompType for_type;

   InternalChecker(JcompSymbol js) {
      use_internals = false;
      for_symbol = js;
      for_type = js.getType();
    }

   boolean useNoInternals()		{ return !use_internals; }

   public boolean visit(FieldAccess n) {
      JcompSymbol js = JavaAst.getReference(n.getExpression());
      if (js == for_symbol) use_internals = true;
      return true;
    }

   public boolean visit(MethodInvocation n) {
      Expression e = n.getExpression();
      if (e != null) {
	 JcompSymbol js = JavaAst.getReference(e);
	 if (js == for_symbol) use_internals = true;
       }
      return true;
    }

   public boolean visit(ArrayCreation n) {
      JcompType bt = JavaAst.getExprType(n);
      while (bt != null) {
	 if (bt == for_type) use_internals = true;
	 bt = bt.getBaseType();
       }
      return true;
    }

   public boolean visit(TypeDeclaration n) {
      JcompType bt = JavaAst.getJavaType(n);
      if (bt == for_type) use_internals = true;
      return true;
    }

   public boolean visit(TypeParameter n) {
      JcompType bt = JavaAst.getJavaType(n);
      if (bt == for_type) use_internals = true;
      return true;
    }

}	// end of subclass InternalChecker




/********************************************************************************/
/*										*/
/*	Methods to check for comformable type					*/
/*										*/
/********************************************************************************/

boolean checkComformable(JcompType cur,JcompType tgt,S6SolutionSet ss,
			    MethodDeclaration md,SingleVariableDeclaration svd)
{
   if (cur == null || tgt == null) return false;
   if (cur == tgt) return true;

   if (tgt.isCompatibleWith(cur)) return true;
   else  if (tgt.isClassType() && cur.isClassType() && checkNoInternals(md,svd)) {
      if (ss.getSearchType() == S6SearchType.METHOD) {
	 if (cur.isCompatibleWith(tgt)) return true;
	 else if (!cur.isBinaryType()) return true;
       }
      else {
	 // restrict this to useful cases in some way
       }
    }

   return false;
}



/********************************************************************************/
/*										*/
/*	Memo with editing deltas						*/
/*										*/
/********************************************************************************/

static class JavaMemo extends MemoImpl {

   private ASTRewrite use_rewrite;
   private ITrackedNodePosition use_position;
   private String new_text;

   JavaMemo(String name,String base,ASTRewrite rw,ITrackedNodePosition pos) {
      super(name,base);
      use_rewrite = rw;
      use_position = pos;
      new_text = null;
    }

   JavaMemo(String name,String base,String newtext) {
      super(name,base);
      use_rewrite = null;
      use_position = null;
      new_text = newtext;
    }

   ASTRewrite getRewrite()			{ return use_rewrite; }
   ITrackedNodePosition getPosition()		{ return use_position; }
   String getNewText()				{ return new_text; }

   void clear() {
      use_rewrite = null;
      use_position = null;
    }

}	// end of subclass JavaMemo




}	// end of class TransformJava



/* end of TransformJava.java */
