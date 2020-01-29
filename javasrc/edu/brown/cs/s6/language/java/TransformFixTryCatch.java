/********************************************************************************/
/*										*/
/*		TransformFixTryCatch.java					*/
/*										*/
/*	Handle removing unneeded catch clauses and try statements		*/
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

/* RCS: $Headers$ */


/*********************************************************************************
 *
 * $Log: TransformFixTryCatch.java,v $
 * Revision 1.9  2016/10/01 01:57:50  spr
 * Fix up transforms for framework search
 *
 * Revision 1.8  2016/07/22 13:31:06  spr
 * Fixups for framework search.
 *
 * Revision 1.7  2016/07/18 23:05:26  spr
 * Update transforms for applications and UI.
 *
 * Revision 1.6  2015/09/23 17:54:53  spr
 * Version to handle andriod UI applications.
 *
 * Revision 1.5  2014/08/29 15:16:09  spr
 * Updates for suise, testcases.
 *
 * Revision 1.4  2013/09/13 20:33:04  spr
 * Add calls for UI search.
 *
 * Revision 1.3  2013-05-09 12:26:20  spr
 * Minor changes to start ui fixups.
 *
 * Revision 1.2  2012-08-13 16:51:51  spr
 * Add new transforms, clean up code, bug fixes.
 *
 * Revision 1.1  2012-07-20 22:15:20  spr
 * New transforms and resolution for UI search
 *
 *
 ********************************************************************************/





package edu.brown.cs.s6.language.java;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6Solution;
import edu.brown.cs.s6.common.S6SolutionSet;


public class TransformFixTryCatch extends TransformJava implements S6Constants, JavaConstants
{


/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public TransformFixTryCatch(String name)
{
   super(name);
}


/********************************************************************************/
/*										*/
/*	Top level mapping methods						*/
/*										*/
/********************************************************************************/

@Override protected TreeMapper findPackageMapping(S6SolutionSet ss,CompilationUnit cu,
      S6Request.PackageSignature sg,S6Solution sol)
{
   return getTryCatchMapping(cu,sol);
}

@Override protected boolean checkApplyClassForPackage(S6SolutionSet sols,CompilationUnit cu,
      S6Request.ClassSignature csg,AbstractTypeDeclaration td)
{
   return false;
}


@Override protected TreeMapper findClassMapping(S6SolutionSet sols,
      TypeDeclaration td,S6Request.ClassSignature csg,S6Solution sol)
{
   return getTryCatchMapping(td,sol);
}



protected boolean checkApplyMethodForClass(S6SolutionSet ss,S6Solution sol,
      TypeDeclaration td,S6Request.MethodSignature msg,MethodDeclaration md)
{
   return false;
}



@Override protected TreeMapper findMethodMapping(S6SolutionSet ss,MethodDeclaration md,
      S6Request.MethodSignature ms,S6Solution sol)
{
   return getTryCatchMapping(md,null);
}




/********************************************************************************/
/*										*/
/*	Actual mapping finder							*/
/*										*/
/********************************************************************************/

private TreeMapper getTryCatchMapping(ASTNode nd,S6Solution sol)
{
   if (JavaAst.getTyper(nd) == null) return null;

   FindTryCatchFixes tcf = new FindTryCatchFixes(nd);

   nd.accept(tcf);
   if (!tcf.hasChanges()) return null;

   TryCatchMapper tcm = new TryCatchMapper(tcf);

   if (tcf.getDoRemove()) sol.setFlag(S6SolutionFlag.REMOVE);

   return tcm;
}



/********************************************************************************/
/*										*/
/*	Visitor to set up mapping						*/
/*										*/
/********************************************************************************/

private static class FindTryCatchFixes extends ASTVisitor {

   private Set<ASTNode> remove_cases;
   private Set<ASTNode> remove_trys;
   private Set<ASTNode> remove_throws;
   private JcompType exception_type;
   private JcompType runtime_type;
   private boolean do_remove;

   FindTryCatchFixes(ASTNode n) {
      remove_cases = new HashSet<ASTNode>();
      remove_trys = new HashSet<ASTNode>();
      remove_throws = new HashSet<ASTNode>();
      do_remove = true;

      exception_type = JavaAst.getTyper(n).findSystemType("java.lang.Exception");
      runtime_type = JavaAst.getTyper(n).findSystemType("java.lang.RuntimeException");
    }

   boolean hasChanges() {
      if (!remove_cases.isEmpty()) return true;
      if (!remove_trys.isEmpty()) return true;
      if (!remove_throws.isEmpty()) return true;
      return false;
    }

   Set<ASTNode> getRemoveCases()			{ return remove_cases; }
   Set<ASTNode> getRemoveTrys() 			{ return remove_trys; }
   Set<ASTNode> getRemoveThrows()			{ return remove_throws; }
   boolean getDoRemove()				{ return do_remove; }

   @Override public boolean visit(MethodDeclaration md) {
      if (md.thrownExceptionTypes().size() > 0 && md.getBody() != null) {
	 if (!Modifier.isPublic(md.getModifiers())) {
	    Set<JcompType> excs = JavaAst.findExceptions(md.getBody());
	    for (Object o : md.thrownExceptionTypes()) {
	       Type n = (Type) o;
	       JcompType jt = JavaAst.getJavaType(n);
	       boolean fnd = false;
	       if (jt != null) {
		  for (JcompType et : excs) {
		     if (et.isCompatibleWith(jt)) fnd = true;
		   }
		}
	       if (!fnd) {
		  if (!Modifier.isPrivate(md.getModifiers())) do_remove = false;
		  remove_throws.add(n);
		}
	     }
	  }
       }
      return true;
    }

   @Override public boolean visit(TryStatement n) {
      Set<JcompType> excs = JavaAst.findExceptions(n.getBody());
      Set<ASTNode> rems = new HashSet<ASTNode>();
      int ctr = 0;
      for (Object o : n.catchClauses()) {
	 CatchClause cc = (CatchClause) o;
	 SingleVariableDeclaration svd = cc.getException();
	 Type t = svd.getType();
	 JcompType jt = JavaAst.getJavaType(t);
	 if (jt == null) continue;
	 ++ctr;
	 if (!jt.isCompatibleWith(exception_type)) continue;
	 if (jt.isCompatibleWith(runtime_type)) continue;
	 if (jt.equals(exception_type)) continue;
	 if (jt.isCompiledType()) continue;

	 boolean fnd = false;
	 for (JcompType jt1 : excs) {
	    if (jt1.isCompatibleWith(jt)) fnd = true;
	  }
	 if (!fnd) rems.add(cc);
       }
      if (rems.size() > 0) {
	 if (rems.size() == ctr && n.getFinally() == null) {
	    remove_trys.add(n);
	  }
	 else remove_cases.addAll(rems);
       }

      return true;
    }
}	// end of inner class FindTryCatchFixes





/********************************************************************************/
/*										*/
/*	Mapper to fix up try-catch statements					*/
/*										*/
/********************************************************************************/

private class TryCatchMapper extends TreeMapper {

   private Set<ASTNode> remove_catches;
   private Set<ASTNode> remove_trys;
   private Set<ASTNode> remove_throws;

   TryCatchMapper(FindTryCatchFixes tcf) {
      remove_catches = tcf.getRemoveCases();
      remove_trys = tcf.getRemoveTrys();
      remove_throws = tcf.getRemoveThrows();
    }



   @Override void rewriteTree(ASTNode orig,ASTRewrite rw) {
      if (orig instanceof TryStatement) {
	 TryStatement ts = (TryStatement) orig;
	 if (remove_trys.contains(orig)) {
	    ASTNode n1 = rw.createCopyTarget(ts.getBody());
	    rw.replace(ts,n1,null);
	    Block rslt = ts.getBody();
	    List<?> rstmts = rslt.statements();
	    if (rstmts.size() > 0) {
	       Statement laststmt = (Statement) rstmts.get(rstmts.size()-1);
	       if (laststmt instanceof ReturnStatement) {
		  if (ts.getParent() instanceof Block) {
		     Block blk = (Block) ts.getParent();
		     List<?> stmts = blk.statements();
		     int idx = stmts.indexOf(orig);
		     if (idx != stmts.size()-1) {
			ListRewrite lrw = rw.getListRewrite(blk,Block.STATEMENTS_PROPERTY);
			for (int i = stmts.size()-1; i > idx; --i) {
			   ASTNode rmst = (ASTNode) stmts.get(i);
			   lrw.remove(rmst,null);
			 }
		      }
		   }
		}
	     }
	  }
	 else {
	    boolean fix = false;
	    for (Object o : ts.catchClauses()) {
	       if (remove_catches.contains(o)) fix = true;
	     }
	    if (!fix) return;
	    ListRewrite lrw = rw.getListRewrite(ts,TryStatement.CATCH_CLAUSES_PROPERTY);
	    for (Object o : ts.catchClauses()) {
	       if (remove_catches.contains(o)) lrw.remove((ASTNode) o,null);
	     }
	  }
       }
      else if (orig instanceof MethodDeclaration) {
	 MethodDeclaration md = (MethodDeclaration) orig;
	 for (Object o : md.thrownExceptionTypes()) {
	    if (remove_throws.contains(o)) rw.remove((ASTNode) o,null);
	  }
       }
    }

}	// end of inner class TryCatchMapper




}	// end of class TransformFixTryCatch




/* end of TransformFixTryCatch.java */
