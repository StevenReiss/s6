/********************************************************************************/
/*										*/
/*		TransformFlatten.java						*/
/*										*/
/*	Transformation that handles removing unwanted blocks at top level	*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/language/java/TransformFlatten.java,v 1.12 2015/09/23 17:54:53 spr Exp $ */


/*********************************************************************************
 *
 * $Log: TransformFlatten.java,v $
 * Revision 1.12  2015/09/23 17:54:53  spr
 * Version to handle andriod UI applications.
 *
 * Revision 1.11  2014/08/29 15:16:09  spr
 * Updates for suise, testcases.
 *
 * Revision 1.10  2013/09/13 20:33:04  spr
 * Add calls for UI search.
 *
 * Revision 1.9  2013-05-09 12:26:21  spr
 * Minor changes to start ui fixups.
 *
 * Revision 1.8  2012-06-20 12:21:33  spr
 * Initial fixes for UI search
 *
 * Revision 1.7  2012-06-11 14:07:49  spr
 * add framework search; fix bugs
 *
 * Revision 1.6  2009-05-12 22:28:48  spr
 * Fix ups to make user context work.
 *
 * Revision 1.5  2008-11-12 13:52:14  spr
 * Performance and bug updates.
 *
 * Revision 1.4  2008-08-28 00:32:56  spr
 * Next version of S6.	Lots of bug fixes, some new functionality.
 *
 * Revision 1.3  2008-07-18 22:27:09  spr
 * Handle remove compilation calls; update transforms to include code to use ASTrewrite.
 *
 * Revision 1.2  2008-07-17 13:46:45  spr
 * Bug fixes and speed ups.
 *
 * Revision 1.1  2008-06-12 17:47:52  spr
 * Next version of S6.
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.language.java;


import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.ivy.jcomp.JcompTyper;
import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6Solution;
import edu.brown.cs.s6.common.S6SolutionSet;



public class TransformFlatten extends TransformJava implements S6Constants, JavaConstants {




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public TransformFlatten(String name)
{
   super(name);
}




/********************************************************************************/
/*										*/
/*	Prevent using this transform for a class fragment			*/
/*										*/
/********************************************************************************/

@Override protected boolean applyClassTransform(S6SolutionSet sols,S6Solution sol,
						   TypeDeclaration td,
						   S6Request.ClassSignature csg)
{
   return false;
}




/********************************************************************************/
/*										*/
/*	Method to find transformer for unwanted throws				*/
/*										*/
/********************************************************************************/

@Override protected TreeMapper findMethodMapping(S6SolutionSet ss,MethodDeclaration md,
						    S6Request.MethodSignature ms,
                                                    S6Solution sol)
{
   Collection<Statement> lth = findUnwantedFlattens(md);
   if (lth == null || lth.isEmpty()) return null;
   JcompTyper jt = JavaAst.getTyper(md);
   if (jt == null) return null;

   return new FlattenMapper(md,lth);
}




/********************************************************************************/
/*										*/
/*	Method to check code for unwanted throws				*/
/*										*/
/********************************************************************************/

private Collection<Statement> findUnwantedFlattens(ASTNode n)
{
   FlattenVisitor tv = new FlattenVisitor(n);

   n.accept(tv);

   return tv.getRemoveFlattens();
}




private static class FlattenVisitor extends ASTVisitor {

   private Collection<Statement> remove_blocks;
   private int block_level;

   FlattenVisitor(ASTNode n) {
      remove_blocks = new HashSet<Statement>();
      block_level = 0;
    }

   Collection<Statement> getRemoveFlattens()		  { return remove_blocks; }

   public boolean visit(Block st) {
      if (block_level > 0) {
	 remove_blocks.add(st);
	 return false;
       }
      ++block_level;
      return true;
    }

   public boolean visit(DoStatement stmt)		{ return false; }
   public boolean visit(EnhancedForStatement st)	{ return false; }
   public boolean visit(ForStatement st)		{ return false; }
   public boolean visit(IfStatement st) 		{ return false; }
   public boolean visit(SwitchStatement st)		{ return false; }
   public boolean visit(SynchronizedStatement st) {
      remove_blocks.add(st);
      return false;
    }
   public boolean visit(TryStatement st) {
      remove_blocks.add(st);
      return false;
    }
   public boolean visit(WhileStatement st)		{ return false; }

}	// end of subclass FlattenVisitor




/********************************************************************************/
/*										*/
/*	Class to handle flattening mappings					*/
/*										*/
/********************************************************************************/

private class FlattenMapper extends TreeMapper {

   private MethodDeclaration change_method;
   private Collection<Statement> remove_blocks;

   FlattenMapper(MethodDeclaration md,Collection<Statement> rem) {
      change_method = md;
      remove_blocks = rem;
    }

   @Override protected String getSpecificsName() {
      return change_method.getName().getIdentifier();
   }

   
   void rewriteTree(ASTNode orig,ASTRewrite rw) {
      if (orig != change_method) return;
      Map<String,Type> ths = new HashMap<>();
      Block blk = ((MethodDeclaration) orig).getBody();
      List<?> stmts = blk.statements();
      ListRewrite lrw = rw.getListRewrite(blk,Block.STATEMENTS_PROPERTY);
      
      for (int idx = 0; idx < stmts.size(); ++idx) {
         Statement st = (Statement) stmts.get(idx);
         if (!remove_blocks.contains(st)) continue;
         ListRewrite sls = null;
         switch (st.getNodeType()) {
            case ASTNode.BLOCK :
               sls = rw.getListRewrite(st,Block.STATEMENTS_PROPERTY);
               break;
            case ASTNode.SYNCHRONIZED_STATEMENT :
               sls = rw.getListRewrite(((SynchronizedStatement) st).getBody(),Block.STATEMENTS_PROPERTY);
               break;
            case ASTNode.TRY_STATEMENT :
               TryStatement tst = (TryStatement) st;
               if (tst.getFinally() != null) break;
               for (Iterator<?> it1 = tst.catchClauses().iterator(); it1.hasNext(); ) {
                  CatchClause cc = (CatchClause) it1.next();
                  SingleVariableDeclaration svd = cc.getException();
                  JcompType jt = JavaAst.getJavaType(svd);
                  if (jt != null) {
                     Name qnm = JavaAst.getQualifiedName(orig.getAST(),jt.getName());
                     Type qt = orig.getAST().newSimpleType(qnm);
                     ths.put(jt.getName(),qt);
                   }
                  else {
                     System.err.println("FAILED TO GET EXCEPTION FROM " + cc);
                   }
                }
               sls = rw.getListRewrite(tst.getBody(),Block.STATEMENTS_PROPERTY);
               break;
          }
         if (sls == null) continue;
         List<?> nsts = sls.getOriginalList();
         ASTNode tgt = null;
         if (nsts.size() > 0) {
            tgt = sls.createCopyTarget((ASTNode) nsts.get(0),(ASTNode) nsts.get(nsts.size()-1));
            lrw.insertAfter(tgt,st,null);
          }
         rw.remove(st,null);
       }
   
      if (ths.size() > 0) {
         ListRewrite elr = rw.getListRewrite(orig,MethodDeclaration.THROWN_EXCEPTION_TYPES_PROPERTY);
         for (Type nm : ths.values()) {
            elr.insertLast(nm,null);
          }
       }
    }

}	// end of subclass FlattenMapper




}	// end of class TransformFlatten





/* end of TransformFlatten.java */
