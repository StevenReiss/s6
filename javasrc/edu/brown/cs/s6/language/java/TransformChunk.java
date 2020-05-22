/********************************************************************************/
/*										*/
/*		TransformChunk.java						*/
/*										*/
/*	Transformation that finds function chunks that might be useful		*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/language/java/TransformChunk.java,v 1.14 2015/09/23 17:54:53 spr Exp $ */


/*********************************************************************************
 *
 * $Log: TransformChunk.java,v $
 * Revision 1.14  2015/09/23 17:54:53  spr
 * Version to handle andriod UI applications.
 *
 * Revision 1.13  2014/08/29 15:16:08  spr
 * Updates for suise, testcases.
 *
 * Revision 1.12  2013/09/13 20:33:04  spr
 * Add calls for UI search.
 *
 * Revision 1.11  2013-05-09 12:26:20  spr
 * Minor changes to start ui fixups.
 *
 * Revision 1.10  2012-06-11 14:07:49  spr
 * add framework search; fix bugs
 *
 * Revision 1.9  2009-09-18 01:41:35  spr
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
 * Revision 1.4  2008-07-17 13:46:45  spr
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import edu.brown.cs.ivy.jcomp.JcompSymbol;
import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.ivy.jcomp.JcompTyper;
import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6Solution;
import edu.brown.cs.s6.common.S6SolutionSet;



public class TransformChunk extends TransformJava implements S6Constants, JavaConstants {




/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/


private static int	chunk_counter = 0;

private static final int MIN_CHUNK_SIZE = 4;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public TransformChunk(String name)
{
   super(name);
}




/********************************************************************************/
/*										*/
/*	Method to find transforms for a given method and signature		*/
/*										*/
/********************************************************************************/

@Override protected boolean applyClassTransform(S6SolutionSet solset,S6Solution sol,
						   TypeDeclaration td,S6Request.ClassSignature csg)
{
   return false;
}




@Override protected Collection<TreeMapper> findMethodMappings(S6SolutionSet ss,MethodDeclaration md,
								 S6Request.MethodSignature ms,
								 S6Solution sol)
{
   Block blk = md.getBody();
   if (blk == null) return null;
   if (md.getName().getIdentifier().startsWith(S6_CHUNK_PREFIX)) return null;
   if (md.getName().getIdentifier().startsWith(S6_NAME_PREFIX)) return null;

   if (sol.getTransforms() == null) return null;

   JcompTyper jt = JavaAst.getTyper(md);
   if (ms.getReturnTypeName() == null) return null;
   if (ms.getReturnTypeName().equals("void")) return null;
   if (jt == null) return null;
   JcompType rt = jt.findSystemType(ms.getReturnTypeName());
   if (rt == null) return null;
   List<JcompType> ptyps = new ArrayList<JcompType>();
   for (String ptn : ms.getParameterTypeNames()) {
      JcompType pt = jt.findSystemType(ptn);
      if (pt == null) return null;
      ptyps.add(pt);
    }

   // First build the set of viable variables

   VarFinder vf = new VarFinder();
   md.accept(vf);

   // next check if there are variables of the input and output types present

   if (!checkVariableTypes(rt,ptyps,vf)) return null;

   // Next build the set of statements
   //	and go through each statement to build the list of variables that
   //	are read or written or called

   List<ChunkStmt> stmts = new ArrayList<ChunkStmt>();
   for (Iterator<?> it = blk.statements().iterator(); it.hasNext(); ) {
      Statement st = (Statement) it.next();
      ChunkStmt cs = new ChunkStmt(st);
      StmtChecker sc = new StmtChecker(cs,vf);
      st.accept(sc);
      stmts.add(cs);
    }

   // Next build the set of potential starting points

   List<ChunkStart> startlist = findStartingPoints(stmts,rt,vf);

   if (startlist == null || startlist.size() == 0) return null;

   // Expand those starting points to potential solutions

   Collection<TreeMapper> solnlist = new ArrayList<TreeMapper>();
   for (ChunkStart cs : startlist) {
      expandSolution(ms.getName(),cs,ptyps,stmts,solnlist);
    }

   return solnlist;
}



/********************************************************************************/
/*										*/
/*	Check if the set of variables has the proper types present		*/
/*										*/
/********************************************************************************/

private boolean checkVariableTypes(JcompType rt,List<JcompType> ptyps,VarFinder vf)
{
   Collection<ChunkVar> vars = vf.getVariables();

   boolean rfnd = false;
   for (ChunkVar cv : vars) {
      if (cv.canWrite() && cv.getType() != null && cv.getType().isCompatibleWith(rt)) rfnd = true;
    }
   if (!rfnd) return false;

   Set<ChunkVar> done = new HashSet<ChunkVar>();
   for (JcompType pt : ptyps) {
      boolean pfnd = false;
      for (ChunkVar cv : vars) {
	 if (cv.canRead() && !done.contains(cv) && cv.getType() != null &&
		pt.isCompatibleWith(cv.getType())) {
	    pfnd = true;
	    done.add(cv);
	    break;
	  }
       }
      if (!pfnd) return false;
    }

   return true;
}




/********************************************************************************/
/*										*/
/*	Find the set of potential solutions by identifying starting points	*/
/*										*/
/********************************************************************************/

private List<ChunkStart> findStartingPoints(List<ChunkStmt> stmts,JcompType rt,VarFinder vf)
{
   // first process for potential variable writes

   if (stmts.size() == 0) return null;

   ChunkStmt st0 = stmts.get(0);
   Block blk = (Block) st0.getSource().getParent();

   List<ChunkStart> solnlist = new ArrayList<ChunkStart>();
   Set<ChunkVar> ovars = new HashSet<ChunkVar>();
   for (ChunkStmt st : stmts) {
      if (st.isBad()) continue;
      for (ChunkVar cv : st.getWriteVars()) {
	 if (cv.getType() != null && cv.getType().isCompatibleWith(rt)) {
	    if (cv.getType().isClassType() || cv.getType().isInterfaceType() ||
		   cv.getType().isParameterizedType())
	       ovars.add(cv);
	    ChunkStart soln = new ChunkStart(st,cv,blk);
	    solnlist.add(soln);
	  }
       }
    }

   // then add case if return can be used

   ChunkVar rv = vf.getReturnVariable();
   if (rv != null && rv.getType() != null && rv.getType().isCompatibleWith(rt)) {
      ChunkStart soln = new ChunkStart(null,rv,blk);
      solnlist.add(soln);
    }

   // then, if the target is an object, add all call sites as potential chunk starts

   if (ovars.size() > 0) {
      for (ChunkStmt st : stmts) {
	 if (st.isBad()) continue;
	 for (ChunkVar cv : st.getCallVars()) {
	     if (ovars.contains(cv)) {
		ChunkStart soln = new ChunkStart(st,cv,blk);
		solnlist.add(soln);
	      }
	   }
       }
    }

   return solnlist;
}



/********************************************************************************/
/*										*/
/*	Methods to expand a solution by adding other statements 		*/
/*										*/
/********************************************************************************/

private void expandSolution(String base,ChunkStart start,List<JcompType> ptyps,
			       List<ChunkStmt> stmts,Collection<TreeMapper> solns)
{
   int lidx;
   if (start.getEndStatement() != null) lidx = stmts.indexOf(start.getEndStatement());
   else lidx = stmts.size()-1;
   ListIterator<ChunkStmt> lit = stmts.listIterator(lidx+1);

   Set<ChunkStmt> used = new HashSet<ChunkStmt>();

   if (!lit.hasPrevious()) return;
   ChunkStmt stmt = lit.previous();
   used.add(stmt);
   Set<ChunkVar> active = new HashSet<ChunkVar>();
   active.addAll(stmt.getReadVars());

   checkDone(base,used,active,ptyps,start,solns);

   while (lit.hasPrevious()) {
      stmt = lit.previous();
      int sz = active.size();
      active.removeAll(stmt.getWriteVars());
      if (active.size() == sz) {
	 // nothing removed
	 if (!containsAny(active,stmt.getCallVars())) continue; // ignore this statement
       }
      used.add(stmt);
      active.addAll(stmt.getReadVars());
      checkDone(base,used,active,ptyps,start,solns);
    }
}




private void checkDone(String base,Set<ChunkStmt> used,Set<ChunkVar> vars,List<JcompType> ptyps,
			  ChunkStart start,Collection<TreeMapper> solns)
{
   // check if this represents a complete solution

   if (vars.size() != ptyps.size()) return;

   Map<ChunkVar,Integer> done = new HashMap<ChunkVar,Integer>();
   checkSoln(base,used,vars,ptyps,done,start,solns);
}


private void checkSoln(String base,Set<ChunkStmt> used,Set<ChunkVar> vars,List<JcompType> ptyps,
			  Map<ChunkVar,Integer> done,ChunkStart start,
			  Collection<TreeMapper> solns)
{
   // ensure we aren't duplicating a function by a null transform

   Block b = start.getBlock();
   if (b.statements().size() == used.size()) return;
   if (b.statements().size() == used.size() + 1) {
      ChunkVar rv = start.getResultVariable();
      if (rv != null && !rv.isReturn()) {
	 for (Iterator<?> it = b.statements().iterator(); it.hasNext(); ) {
	    Statement st = (Statement) it.next();
	    if (st.getNodeType() == ASTNode.RETURN_STATEMENT) {
               boolean fnd = false;
               for (ChunkStmt cs :used) {
                  if (cs.getSource() == st) fnd = true;
                }
               if (!fnd) {
                  ReturnStatement rst = (ReturnStatement) st;
                  Expression rex = rst.getExpression();
                  if (rex != null && rex.toString().equals(rv.getName())) return;
                }
	     }
	  }
       }
    }

   // check if we are using everything but a return of the solution variable

   if (vars.size() == done.size()) {
      if (used.size() < MIN_CHUNK_SIZE) return;
      ChunkVar [] params = new ChunkVar[ptyps.size()];
      for (Map.Entry<ChunkVar,Integer> ent : done.entrySet()) {
	 params[ent.getValue()] = ent.getKey();
       }
      solns.add(new ChunkSoln(base,used,start.getResultVariable(),params));
      return;
    }

   // recursive check to match open variables with parameter types

   for (ChunkVar cv : vars) {
      if (done.containsKey(cv)) continue;
      for (int i = 0; i < ptyps.size(); ++i) {
	 if (done.containsValue(i)) continue;
	 JcompType pt = ptyps.get(i);
	 if (pt.isCompatibleWith(cv.getType())) {
	    done.put(cv,i);
	    checkSoln(base,used,vars,ptyps,done,start,solns);
	    done.remove(cv);
	  }
       }
    }
}



private boolean containsAny(Collection<?> c1,Collection<?> c2)
{
   for (Object o : c1) {
      if (c2.contains(o)) return true;
    }

   return false;
}




/********************************************************************************/
/*										*/
/*	ChunkVar -- representation of a variable				*/
/*										*/
/********************************************************************************/

private class VarFinder extends ASTVisitor {

   private Map<JcompSymbol,ChunkVar> var_set;
   private ChunkVar return_var;

   VarFinder() {
      var_set = new HashMap<JcompSymbol,ChunkVar>();
      return_var = null;
    }

   Collection<ChunkVar> getVariables()		{ return var_set.values(); }
   ChunkVar getVariable(JcompSymbol js) 	{ return var_set.get(js); }
   ChunkVar getReturnVariable() 		{ return return_var; }

   public void preVisit(ASTNode n) {
      JcompSymbol js = JavaAst.getDefinition(n);
      if (js != null) {
	 ChunkVar cv = findVar(js);
	 if (cv != null) cv.setDefinition(n);
       }
      js = JavaAst.getReference(n);
      if (js != null) {
	 findVar(js);
       }
    }

   public void endVisit(ReturnStatement rs) {
      if (rs.getExpression() != null && return_var == null) {
	 JcompType jt = JavaAst.getExprType(rs.getExpression());
	 return_var = new ChunkVar(jt);
       }
    }

   private ChunkVar findVar(JcompSymbol js) {
      if (js == null) return null;
      if (js.isMethodSymbol()) return null;
      if (js.isTypeSymbol()) return null;
      ChunkVar cv = var_set.get(js);
      if (cv == null) {
	 cv = new ChunkVar(js);
	 var_set.put(js,cv);
       }
      return cv;
    }

}	// end of subclass VarFinder


private static class ChunkVar {

   private JcompSymbol for_symbol;
   private JcompType symbol_type;
   private boolean can_read;
   private boolean can_write;
   private ASTNode decl_site;
   @SuppressWarnings("unused")
   private boolean is_parameter;
   private boolean is_return;
   private boolean is_nested;

   ChunkVar(JcompSymbol js) {
      for_symbol = js;
      symbol_type = js.getType();
      can_read = true;
      can_write = true;
      decl_site = null;
      is_parameter = false;
      is_return = false;
      is_nested = false;
    }

   ChunkVar(JcompType jt) {
      for_symbol = null;
      symbol_type = jt;
      can_read = false;
      can_write = true;
      decl_site = null;
      is_parameter = false;
      is_return = true;
      is_nested = false;
    }

   boolean canRead()				{ return can_read; }
   boolean canWrite()				{ return can_write; }
   boolean isReturn()				{ return is_return; }
   boolean isNested()				{ return is_nested; }
   JcompType getType()				{ return symbol_type; }
   JcompSymbol getSymbol()			{ return for_symbol; }
   String getName()				{ return getSymbol().getName(); }
   ASTNode getDeclaration()			{ return decl_site; }

   void setDefinition(ASTNode n) {
      decl_site = n;
      if (n instanceof SingleVariableDeclaration && n.getParent() instanceof MethodDeclaration) {
	 is_parameter = true;
       }
      if (n instanceof VariableDeclarationFragment) {
	 if (n.getParent() instanceof VariableDeclarationStatement &&
		n.getParent().getParent() instanceof Block &&
		n.getParent().getParent().getParent() instanceof MethodDeclaration)
	    is_nested = false;
	 else
	    is_nested = true;
       }
    }

   public String toString() {
      String s = (for_symbol == null ? "*RETURN*" : for_symbol.getName());
      if (symbol_type != null) s +=  " : " + symbol_type.getName();
      return s;
    }

}	// end of subclass ChunkVar




/********************************************************************************/
/*										*/
/*	Statement information							*/
/*										*/
/********************************************************************************/

private class StmtChecker extends ASTVisitor {

   private ChunkStmt for_stmt;
   private VarFinder var_finder;
   private boolean is_write;
   private Stack<Boolean> write_stack;

   StmtChecker(ChunkStmt cs,VarFinder vf) {
      for_stmt = cs;
      var_finder = vf;
      is_write = false;
      write_stack = new Stack<Boolean>();
    }

   public void endVisit(SimpleName n) {
      addReadWrite(n);
    }

   public boolean visit(QualifiedName n) {
      addReadWrite(n);
      return false;
    }

   public boolean visit(Assignment n) {
      n.getRightHandSide().accept(this);
      is_write = true;
      n.getLeftHandSide().accept(this);
      is_write = false;
      return false;
    }

   public boolean visit(ArrayAccess n) {
      boolean ow = is_write;
      n.getArray().accept(this);
      is_write = false;
      n.getIndex().accept(this);
      is_write = ow;
      return false;
    }

   public boolean visit(FieldAccess n) {
      boolean ow = is_write;
      is_write = false;
      n.getExpression().accept(this);
      if (ow) {
	 JcompSymbol js = JavaAst.getReference(n.getExpression());
	 if (js != null) for_stmt.addWrite(var_finder.getVariable(js));
       }
      is_write = ow;
      n.getName().accept(this);
      return false;
    }

   public boolean visit(MethodInvocation n) {
      write_stack.push(is_write);
      is_write = false;
      if (n.getExpression() != null) {
	 JcompSymbol js = JavaAst.getReference(n.getExpression());
	 if (js != null) for_stmt.addCall(var_finder.getVariable(js));
      }
      return true;
    }

   public void endVisit(MethodInvocation n) {
      is_write = write_stack.pop();
    }

   public void endVisit(ReturnStatement n) {
      for_stmt.markReturn();
    }

   public void endVisit(ThrowStatement n) {
      for_stmt.markThrow();
    }

   private void addReadWrite(ASTNode n) {
      JcompSymbol js = JavaAst.getDefinition(n);
      if (js != null) for_stmt.addWrite(var_finder.getVariable(js));
      js = JavaAst.getReference(n);
      if (js != null) {
	 ChunkVar cv = var_finder.getVariable(js);
	 if (is_write) for_stmt.addWrite(cv);
	 else for_stmt.addRead(cv);
       }
    }

}	// end of subclass StmtChecker



@SuppressWarnings("unused")
private static class ChunkStmt {

   private Statement source_node;
   private Set<ChunkVar> write_vars;
   private Set<ChunkVar> read_vars;
   private Set<ChunkVar> call_vars;
   boolean does_return;
   boolean does_throw;
   boolean must_ignore;

   ChunkStmt(Statement src) {
      source_node = src;
      write_vars = new HashSet<ChunkVar>();
      read_vars = new HashSet<ChunkVar>();
      call_vars = new HashSet<ChunkVar>();
      does_return = false;
      does_throw = false;
      must_ignore = false;
    }

   Set<ChunkVar> getWriteVars() 		{ return write_vars; }
   Set<ChunkVar> getReadVars()			{ return read_vars; }
   Set<ChunkVar> getCallVars()			{ return call_vars; }
   boolean isBad()				{ return must_ignore; }
   Statement getSource()			{ return source_node; }
   boolean doesReturn() 			{ return does_return; }

   void markReturn()				{ does_return = true; }
   void markThrow()				{ does_throw = true; }
   void markBad()				{ must_ignore = true; }

   void addWrite(ChunkVar cv) {
      if (cv != null) write_vars.add(cv);
    }

   void addRead(ChunkVar cv) {
      if (cv != null && !write_vars.contains(cv)) read_vars.add(cv);
    }

   void addCall(ChunkVar cv) {
      if (cv != null) call_vars.add(cv);
    }

   public String toString() {
      return source_node.toString();
    }

}	// end of subclass ChunkStmt




/********************************************************************************/
/*										*/
/*	ChunkStart -- represent a potential solution starting point		*/
/*										*/
/********************************************************************************/

private static class ChunkStart {

   private ChunkStmt end_stmt;
   private ChunkVar return_var;
   private Block for_block;

   ChunkStart(ChunkStmt st,ChunkVar cv,Block b) {
      end_stmt = st;
      return_var = cv;
      for_block = b;
    }

   ChunkStmt getEndStatement()			{ return end_stmt; }
   ChunkVar getResultVariable() 		{ return return_var; }
   Block getBlock()				{ return for_block; }

   public String toString() {
      if (end_stmt != null)
	 return return_var.toString() + " @ " + end_stmt.toString();
      else
	 return return_var.toString() + " @ ALL RETURNS";
    }

}	// end of subclass ChunkStart




/********************************************************************************/
/*										*/
/*	ChunkSoln -- represent a potential solution				*/
/*										*/
/********************************************************************************/

private class ChunkSoln extends TreeMapper {

   private MethodDeclaration method_decl;
   private ASTNode block_node;
   private Set<Statement> use_statements;
   private ChunkVar return_var;
   private ChunkVar [] param_vars;
   private String chunk_name;
   private Set<ChunkVar> used_vars;
   private String base_name;

   ChunkSoln(String nm,Set<ChunkStmt> used,ChunkVar rv,ChunkVar [] pvar) {
      base_name = nm;
      use_statements = new HashSet<Statement>();
      used_vars = new HashSet<ChunkVar>();
      block_node = null;
      for (ChunkStmt cs : used) {
         Statement st = cs.getSource();
         use_statements.add(st);
         if (block_node == null) block_node = st.getParent();
         used_vars.addAll(cs.getReadVars());
         used_vars.addAll(cs.getWriteVars());
         used_vars.addAll(cs.getCallVars());
       }
      method_decl = (MethodDeclaration) block_node.getParent();
      return_var = rv;
      param_vars = pvar;
      chunk_name = S6_CHUNK_PREFIX + (++chunk_counter);
    }

   @Override protected String getSpecificsName()	   { return base_name; }

   void rewriteTree(ASTNode orig,ASTRewrite rw) {
      if (orig != method_decl) return;
   
      MethodDeclaration nmd = (MethodDeclaration) orig;
      AST ast = nmd.getAST();
   
      ASTNode par = orig.getParent();
      ListRewrite clrw = null;
      if (par instanceof AbstractTypeDeclaration) {
         AbstractTypeDeclaration atd = (AbstractTypeDeclaration) par;
         clrw = rw.getListRewrite(atd,atd.getBodyDeclarationsProperty());
       }
      else if (par instanceof AnonymousClassDeclaration) {
         AnonymousClassDeclaration atd = (AnonymousClassDeclaration) par;
         clrw = rw.getListRewrite(atd,AnonymousClassDeclaration.BODY_DECLARATIONS_PROPERTY);
       }
      if (clrw != null) {
         MethodDeclaration toinsert = (MethodDeclaration) rw.createCopyTarget(orig);
         clrw.insertAfter(toinsert,orig,null);
       }
   
      Statement lstmt = null;		// last used statement
   
      ListRewrite lrw = rw.getListRewrite(nmd.getBody(),Block.STATEMENTS_PROPERTY);
      for (Iterator<?> it = nmd.getBody().statements().iterator(); it.hasNext(); ) {
         Statement s = (Statement) it.next();
         if (!use_statements.contains(s)) {
            lrw.remove(s,null);
          }
         else lstmt = s;
       }
   
      if (!return_var.isReturn()) {
         ReturnFixer rf = new ReturnFixer(return_var,rw);
         nmd.accept(rf);
         nmd.getBody().statements();
   
         if (lstmt == null || !(lstmt instanceof ReturnStatement)) {
            ReturnStatement rs = ast.newReturnStatement();
            JcompSymbol js = return_var.getSymbol();
            Name nm = JavaAst.getQualifiedName(ast,js.getName());
            rs.setExpression(nm);
            lrw.insertLast(rs,null);
          }
       }
   
      // ensure we have all needed declarations
      Set<JcompSymbol> needdecl = new HashSet<JcompSymbol>();
      for (int i = 0; i < param_vars.length; ++i) used_vars.remove(param_vars[i]);
      for (ChunkVar cv : used_vars) {
         ASTNode fd = cv.getDeclaration();
         while (fd != null && fd.getParent() != block_node) fd = fd.getParent();
         if (fd == null || !use_statements.contains(fd)) needdecl.add(cv.getSymbol());
       }
   
      // check if decl for return var is nested and fix if so
      if (!return_var.isReturn() && return_var.isNested()) {
         JcompSymbol rs = return_var.getSymbol();
         needdecl.add(rs);
         ASTNode decl = return_var.getDeclaration();
         while (!(decl instanceof VariableDeclarationFragment)) decl = decl.getParent();
         VariableDeclarationFragment vdf = (VariableDeclarationFragment) decl;
         Assignment as = null;
         if (vdf.getInitializer() != null) {
            as = ast.newAssignment();
            as.setLeftHandSide(JavaAst.getSimpleName(ast,rs.getName()));
            as.setRightHandSide((Expression) rw.createCopyTarget(vdf.getInitializer()));
          }
   
         ASTNode vpar = vdf.getParent();
         if (vpar instanceof VariableDeclarationStatement) {
            Statement sas = null;
            if (as != null) sas = ast.newExpressionStatement(as);
            VariableDeclarationStatement vds = (VariableDeclarationStatement) vpar;
            if (vds.fragments().size() == 1) {
               if (sas == null) rw.remove(vds,null);
               else rw.replace(vds,sas,null);
             }
            else {
               if (sas != null) {
        	  ListRewrite vlrw = rw.getListRewrite(vds.getParent(),
        						  (ChildListPropertyDescriptor) vds.getLocationInParent());
        	  vlrw.insertAfter(sas,vds,null);
        	}
               rw.remove(vdf,null);
             }
          }
         else if (vpar instanceof VariableDeclarationExpression) {
            VariableDeclarationExpression vde = (VariableDeclarationExpression) vpar;
            rw.replace(vde,as,null);
          }
       }
   
      for (JcompSymbol js : needdecl) {
         Statement dst = js.createDeclaration(ast);
         lrw.insertFirst(dst,null);
       }
   
      // fix up method signature
      JcompType jt = return_var.getType();
      Type t = jt.createAstNode(ast);
      rw.set(nmd,MethodDeclaration.RETURN_TYPE2_PROPERTY,t,null);
      rw.set(nmd,MethodDeclaration.CONSTRUCTOR_PROPERTY,false,null);
      rw.set(nmd,MethodDeclaration.NAME_PROPERTY,JavaAst.getSimpleName(ast,chunk_name),null);
      ListRewrite plr = rw.getListRewrite(nmd,MethodDeclaration.PARAMETERS_PROPERTY);
      List<?> plst = plr.getOriginalList();
      if (plst.size() > 0) {
         plr.createMoveTarget((ASTNode) plst.get(0),(ASTNode) plst.get(plst.size()-1),null,null);
       }
      for (ChunkVar cv : param_vars) {
         try {
            SingleVariableDeclaration svd = ast.newSingleVariableDeclaration();
            SimpleName nm = JavaAst.getSimpleName(ast,cv.getName());
            svd.setName(nm);
            svd.setType(cv.getType().createAstNode(ast));
            plr.insertLast(svd,null);
          }
         catch (IllegalArgumentException e) {
            System.err.println("BAD NAME GIVEN FOR CHUNK: " + cv + " " + cv.getName());
          }
       }
    }


}	// end of subclass ChunkSoln




/********************************************************************************/
/*										*/
/*	Class to fix up return statements in used chunks			*/
/*										*/
/********************************************************************************/

private class ReturnFixer extends ASTVisitor {

   private ChunkVar return_var;
   private boolean have_def;
   private ASTRewrite ast_rewrite;

   ReturnFixer(ChunkVar rv,ASTRewrite rw) {
      return_var = rv;
      have_def = false;
      ast_rewrite = rw;
    }

   public boolean visit(ReturnStatement r) {
      JcompSymbol js = return_var.getSymbol();
      Name nm = JavaAst.getQualifiedName(r.getAST(),js.getName());
      if (have_def) {
	 ast_rewrite.set(r,ReturnStatement.EXPRESSION_PROPERTY,nm,null);
       }
      else {
	 // System.err.println("CHUNK RETURN WITHOUT DEFINITION");
	 JcompType jt = return_var.getType();
	 Expression ex = jt.createDefaultValue(r.getAST());
	 ast_rewrite.set(r,ReturnStatement.EXPRESSION_PROPERTY,ex,null);
       }

      return false;
    }

   public boolean visit(VariableDeclarationFragment v) {
      String nm = v.getName().getIdentifier();
      if (nm.equals(return_var.getName())) have_def = true;
      return false;
    }


}	// end of subclass ReturnFixer



}	// end of class TransformChunk




/* end of TransformChunk.java */
