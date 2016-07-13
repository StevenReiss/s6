/********************************************************************************/
/*										*/
/*		TransformContext.java						*/
/*										*/
/*	Transformation that handles parameter types matching user context	*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/language/java/TransformContext.java,v 1.9 2015/09/23 17:54:53 spr Exp $ */


/*********************************************************************************
 *
 * $Log: TransformContext.java,v $
 * Revision 1.9  2015/09/23 17:54:53  spr
 * Version to handle andriod UI applications.
 *
 * Revision 1.8  2014/08/29 15:16:08  spr
 * Updates for suise, testcases.
 *
 * Revision 1.7  2013/09/13 20:33:04  spr
 * Add calls for UI search.
 *
 * Revision 1.6  2013-05-09 12:26:20  spr
 * Minor changes to start ui fixups.
 *
 * Revision 1.5  2012-06-11 14:07:49  spr
 * add framework search; fix bugs
 *
 * Revision 1.4  2009-09-18 01:41:36  spr
 * Handle user testing.
 *
 * Revision 1.3  2009-05-12 22:28:48  spr
 * Fix ups to make user context work.
 *
 * Revision 1.2  2008-11-12 13:52:14  spr
 * Performance and bug updates.
 *
 * Revision 1.1  2008-08-28 00:32:56  spr
 * Next version of S6.	Lots of bug fixes, some new functionality.
 *
 *
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

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import edu.brown.cs.ivy.jcomp.JcompScope;
import edu.brown.cs.ivy.jcomp.JcompSymbol;
import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.ivy.jcomp.JcompTyper;
import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6Solution;
import edu.brown.cs.s6.common.S6SolutionSet;



public class TransformContext extends TransformJava implements S6Constants, JavaConstants {




/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private static final int MAX_MAPS = 64;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public TransformContext(String name)
{
   super(name);
}




/********************************************************************************/
/*										*/
/*	Method to create mapper to do actual context type changes		*/
/*										*/
/********************************************************************************/

@Override protected Collection<TreeMapper> findMethodMappings(S6SolutionSet ss,
								 MethodDeclaration md,
								 S6Request.MethodSignature ms,
                                                                 S6Solution sol)
{
   List<String> mstyp = ms.getParameterTypeNames();
   List<?> mdtyp = md.parameters();

   if (mstyp.isEmpty()) return null;
   if (mstyp.size() > mdtyp.size()) return null;

   JcompTyper typer = JavaAst.getTyper(md);
   if (typer == null) return null;

   Collection<TreeMapper> solns = null;

   Set<JcompType> typs = new HashSet<JcompType>();

   for (String tnm : mstyp) {
      JcompType jt = typer.findSystemType(tnm);
      if (jt != null && jt.isContextType()) typs.add(jt);
    }

   // For each context type in the signature
   for (JcompType jt : typs) {
      ContextChecker cc = new ContextChecker(ss,md,jt);
      if (cc.doChecks()) {
	 if (solns == null) solns = new ArrayList<TreeMapper>();
	 solns.addAll(cc.getMappings());
       }
    }

   return solns;
}



/********************************************************************************/
/*										*/
/*	ContextChecker -- class to check if context is applicable		*/
/*										*/
/********************************************************************************/

private class ContextChecker {

   private MethodDeclaration for_method;
   private JcompType context_type;		// the type to map into
   private JcompTyper java_typer;
   private List<MemberSet> all_maps;

   ContextChecker(S6SolutionSet ss,MethodDeclaration md,JcompType ctxtyp) {
      for_method = md;
      java_typer = JavaAst.getTyper(md);
      context_type = ctxtyp;
      all_maps = null;
    }

   boolean doChecks() {
      Set<JcompType> done = new HashSet<JcompType>();

      // For each parameter type
      for (Iterator<?> it = for_method.parameters().iterator(); it.hasNext(); ) {
	 SingleVariableDeclaration svd = (SingleVariableDeclaration) it.next();
	 JcompSymbol js = JavaAst.getDefinition(svd);
	 if (js == null) continue;
	 JcompType t1 = js.getType();
	 if (done.contains(t1)) continue;
	 done.add(t1);
	 checkContextType(t1);
       }

      return all_maps != null && all_maps.size() > 0;
    }

   Collection<TreeMapper> getMappings() {
      return new ArrayList<TreeMapper>(all_maps);
    }

   private void checkContextType(JcompType typ) {
      // check if parameter type (typ) can match context type (context_type)
      if (!typ.isClassType()) return;
      if (typ.isKnownType()) return;
   
      MemberData md = getItemsUsed(typ,for_method,java_typer);
   
      context_type.defineAll(java_typer);
      Map<MemberInstance,List<JcompSymbol>> cands = new HashMap<MemberInstance,List<JcompSymbol>>();
      for (MemberInstance mi : md.getMembersUsed()) {
         List<JcompSymbol> pots = findCandidates(mi);
         if (pots == null || pots.isEmpty()) return;
         cands.put(mi,pots);
       }
   
      all_maps = new ArrayList<MemberSet>();
      Map<MemberInstance,JcompSymbol> mmap = new HashMap<MemberInstance,JcompSymbol>();
      Set<JcompSymbol> used = new HashSet<JcompSymbol>();
      LinkedList<MemberInstance> work = new LinkedList<MemberInstance>(md.getMembersUsed());
      computeCandidates(typ,cands,mmap,used,work);
      if (all_maps.size() == 0 || all_maps.size() > MAX_MAPS) all_maps = null;
    }

   private void computeCandidates(JcompType ptyp,
				     Map<MemberInstance,List<JcompSymbol>> cands,
				     Map<MemberInstance,JcompSymbol> curmap,
				     Set<JcompSymbol> used,LinkedList<MemberInstance> todo) {
      if (todo.isEmpty()) {
	 MemberSet mset = new MemberSet(ptyp,context_type,curmap);
	 all_maps.add(mset);
	 return;
       }

      MemberInstance work = todo.removeFirst();
      for (JcompSymbol js : cands.get(work)) {
	 if (!used.contains(js)) {
	    used.add(js);
	    curmap.put(work,js);
	    computeCandidates(ptyp,cands,curmap,used,todo);
	    curmap.remove(work);
	    used.remove(js);
	  }
       }
      todo.addFirst(work);
    }

   private List<JcompSymbol> findCandidates(MemberInstance mi) {
      JcompScope scp = context_type.getScope();
      if (scp == null) return null;
      List<JcompSymbol> cands = new ArrayList<JcompSymbol>();
      if (mi.isField()) {
	 for (JcompSymbol js : scp.getDefinedFields()) {
	    if (mi.isCompatibleWith(js)) cands.add(js);
	  }
       }
      else {
	 for (JcompSymbol js : scp.getDefinedMethods()) {
	    if (mi.isCompatibleWith(js)) cands.add(js);
	  }
       }
      return cands;
    }

}	// end of subclass ContextChecker




/********************************************************************************/
/*										*/
/*	MethodFinder :: visitor to find methods used from given type		*/
/*										*/
/********************************************************************************/

private MemberData getItemsUsed(JcompType base,ASTNode start,JcompTyper typer)
{
   MemberData result = new MemberData(base);
   LinkedList<ASTNode> todo = new LinkedList<ASTNode>();
   todo.add(start);

   while (!todo.isEmpty()) {
      ASTNode nxt = todo.removeFirst();
      if (result.addRoot(nxt)) {
	 MethodFinder mf = new MethodFinder(base);
	 nxt.accept(mf);
	 todo.addAll(mf.getOtherNodes());
	 for (ASTNode an : mf.getUsedNodes()) result.addUse(an,typer);
       }
    }

   return result;
}



private static class MethodFinder extends ASTVisitor {

   private JcompType base_type;
   private Collection<ASTNode> used_nodes;
   private Set<ASTNode> other_nodes;

   MethodFinder(JcompType bt) {
      base_type = bt;
      used_nodes = new HashSet<ASTNode>();
      other_nodes = new HashSet<ASTNode>();
    }

   Collection<ASTNode> getUsedNodes()			{ return used_nodes; }
   Set<ASTNode> getOtherNodes() 			{ return other_nodes; }

   public void endVisit(MethodInvocation n) {
      JcompType bt = null;
      Expression e = n.getExpression();
      if (e != null) bt = JavaAst.getExprType(e);
      checkMethod(n,bt);
    }

   public void endVisit(SuperMethodInvocation n) {
      JcompType bt = null;
      Name mn = n.getQualifier();
      if (mn != null) {
	 bt = JavaAst.getJavaType(mn);
	 if (bt != null) bt = bt.getSuperType();
       }
      checkMethod(n,bt);
    }

   public void endVisit(ClassInstanceCreation n) {
      JcompType bt = JavaAst.getJavaType(n.getType());
      checkMethod(n,bt);
    }

   public void endVisit(ConstructorInvocation n) {
      checkMethod(n,null);
    }

   public void endVisit(SuperConstructorInvocation n) {
      checkMethod(n,null);
    }

   public void endVisit(FieldAccess n) {
      JcompType bt = JavaAst.getExprType(n.getExpression());
      checkField(n,bt);
    }

   public void endVisit(QualifiedName n) {
      JcompType bt = JavaAst.getJavaType(n.getQualifier());
      if (bt == null) bt = JavaAst.getExprType(n.getQualifier());
      checkField(n,bt);
    }

   private void checkMethod(ASTNode n,JcompType bt) {
      JcompSymbol js = JavaAst.getReference(n);
      if (js != null) {
	 ASTNode nn = js.getDefinitionNode();
	 if (nn != null) other_nodes.add(nn);
	 JcompType ctyp = js.getClassType();
	 if (ctyp == base_type) used_nodes.add(n);
       }
      else if (bt == base_type) {
	 used_nodes.add(n);
       }
    }

   private void checkField(ASTNode n,JcompType bt) {
      JcompSymbol js = JavaAst.getReference(n);
      if (js != null) {
	 ASTNode nn = js.getDefinitionNode();
	 if (nn != null && nn instanceof FieldDeclaration) {
	    FieldDeclaration fd = (FieldDeclaration) nn;
	    for (Iterator<?> it = fd.fragments().iterator(); it.hasNext(); ) {
	       VariableDeclarationFragment vdf = (VariableDeclarationFragment) it.next();
	       Expression in = vdf.getInitializer();
	       if (in != null) other_nodes.add(in);
	     }
	  }
	 JcompType ctyp = js.getClassType();
	 if (ctyp == base_type) used_nodes.add(n);
       }
      else if (bt == base_type) used_nodes.add(n);
    }

}	// end of subclass MethodFinder




/********************************************************************************/
/*										*/
/*	Representation of the result of the initial search for a type		*/
/*										*/
/********************************************************************************/

private static class MemberData {

   private Set<ASTNode> root_nodes;
   private Set<MemberInstance> members_used;

   MemberData(JcompType base) {
      root_nodes = new HashSet<ASTNode>();
      members_used = new HashSet<MemberInstance>();
    }

   boolean addRoot(ASTNode n)			{ return root_nodes.add(n); }

   void addUse(ASTNode u,JcompTyper typer) {
      MemberInstance mi = new MemberInstance(u,typer);
      if (!members_used.add(mi)) {
	 for (MemberInstance omi : members_used) {
	    if (omi.equals(mi)) {
	       omi.addUse(typer,u);
	       break;
	     }
	  }
       }
    }

   Set<MemberInstance> getMembersUsed() 	{ return members_used; }

}	// end of subclass MemberData




/********************************************************************************/
/*										*/
/*	Representation of a member instance and its uses			*/
/*										*/
/********************************************************************************/

private static class MemberInstance {

   private JcompSymbol base_symbol;
   private String member_name;
   private JcompType member_type;
   private List<ASTNode> member_uses;
   private JcompType implied_type;
   private boolean can_write;
   private boolean static_access;

   MemberInstance(ASTNode n,JcompTyper typer) {
      JcompSymbol js = JavaAst.getReference(n);
      if (js != null) {
	 base_symbol = js;
	 member_name = js.getName();
	 member_type = js.getType();
	 if (js.isFieldSymbol()) implied_type = js.getType();
	 else implied_type = js.getType().getBaseType();	// return type
       }
      else if (n instanceof MethodInvocation) {
	 MethodInvocation mn = (MethodInvocation) n;
	 JcompType jt = JavaAst.getJavaType(mn.getExpression());
	 if (jt != null) static_access = true;
	 setupMethod(mn.getName().getIdentifier(),mn.arguments(),typer);
       }
      else if (n instanceof SuperMethodInvocation) {
	 SuperMethodInvocation mn = (SuperMethodInvocation) n;
	 setupMethod(mn.getName().getIdentifier(),mn.arguments(),typer);
       }
      else if (n instanceof ClassInstanceCreation) {
	 ClassInstanceCreation mn = (ClassInstanceCreation) n;
	 setupMethod("<init>",mn.arguments(),typer);
       }
      else if (n instanceof FieldAccess) {
	 FieldAccess mn = (FieldAccess) n;
	 JcompType jt = JavaAst.getJavaType(mn.getExpression());
	 if (jt != null) static_access = true;
	 setupField(mn.getName().getIdentifier());
       }
      else if (n instanceof QualifiedName) {
	 QualifiedName qn = (QualifiedName) n;
	 setupField(qn.getName().getIdentifier());
       }
      member_uses = new ArrayList<ASTNode>();
      member_uses.add(n);
      can_write = false;
      static_access = false;
      computeImpliedType(typer,n);
    }

   void addUse(JcompTyper typer,ASTNode n) {
      member_uses.add(n);
      computeImpliedType(typer,n);
    }
   Collection<ASTNode> getUses()		{ return member_uses; }

   boolean isField() {
      if (base_symbol != null) return base_symbol.isFieldSymbol();
      return member_type == null;
    }

   boolean isCompatibleWith(JcompSymbol js) {
      if (isField()) {
	 if (member_type != null && !member_type.isCompatibleWith(js.getType())) return false;
	 if (static_access && !js.isStatic()) return false;
	 if (can_write && js.isFinal()) return false;
	 if (implied_type != null && !js.getType().isCompatibleWith(implied_type)) return false;
       }
      else {
	 if (!member_type.isCompatibleWith(js.getType())) return false;
	 if (static_access && !js.isStatic()) return false;
	 if (implied_type != null && !js.getType().getBaseType().isCompatibleWith(implied_type))
	    return false;
	 if (member_name.equals("<init>")) {
	    if (!js.isConstructorSymbol()) return false;
	  }
	 else {
	    if (js.isConstructorSymbol()) return false;
	  }
       }
      return true;
    }

   private void setupMethod(String id,List<?> args,JcompTyper typer) {
      List<JcompType> atys = new ArrayList<JcompType>();
      for (Iterator<?> it = args.iterator(); it.hasNext(); ) {
	 Expression e = (Expression) it.next();
	 atys.add(JavaAst.getExprType(e));
       }
      member_type = JcompType.createMethodType(null,atys,false);
      member_type = typer.fixJavaType(member_type);
      member_name = id;
    }

   private void setupField(String id) {
      member_name = id;
      member_type = null;
    }

   private void computeImpliedType(JcompTyper typer,ASTNode n) {
      ASTNode p = n.getParent();
      StructuralPropertyDescriptor spd = n.getLocationInParent();
      JcompType ntyp = null;

      switch (p.getNodeType()) {
	 case ASTNode.EXPRESSION_STATEMENT :
	    ntyp = typer.findType("void");
	    break;
	 case ASTNode.ARRAY_ACCESS :
	    if (spd == ArrayAccess.INDEX_PROPERTY) ntyp = typer.findType("int");
	    // else handle generic array type;
	    break;
	 case ASTNode.ARRAY_CREATION :
	    if (spd == ArrayCreation.DIMENSIONS_PROPERTY) ntyp = typer.findType("int");
	    // else handle array content type;
	    break;
	 case ASTNode.ARRAY_INITIALIZER :
	    // handle array content type
	    break;
	 case ASTNode.ASSIGNMENT :
	    Assignment asgn = (Assignment) p;
	    if (spd == Assignment.LEFT_HAND_SIDE_PROPERTY) {
	       ntyp = JavaAst.getExprType(asgn.getRightHandSide());
	       can_write = true;
	     }
	    else
	       ntyp = JavaAst.getExprType(asgn.getLeftHandSide());
	    break;
	 case ASTNode.CAST_EXPRESSION :
	    CastExpression cast = (CastExpression) p;
	    ntyp = JavaAst.getJavaType(cast.getType());
	    if (ntyp == null) ntyp = JavaAst.getExprType(cast.getType());
	    break;
	 case ASTNode.CLASS_INSTANCE_CREATION :
	    break;
	 case ASTNode.CONDITIONAL_EXPRESSION :
	    if (spd == ConditionalExpression.EXPRESSION_PROPERTY)
	       ntyp = typer.findType("boolean");
	    else computeImpliedType(typer,p);
	    break;
	 case ASTNode.FIELD_ACCESS :
	    ntyp = typer.findType("java.lang.Object");
	    break;
	 case ASTNode.INFIX_EXPRESSION :
	    break;
	 case ASTNode.INSTANCEOF_EXPRESSION :
	    ntyp = typer.findType("java.lang.Object");
	    break;
	 case ASTNode.METHOD_INVOCATION :
	 case ASTNode.SUPER_METHOD_INVOCATION :
	    // handle parameter checking
	    break;
	 case ASTNode.PARENTHESIZED_EXPRESSION :
	    computeImpliedType(typer,p);
	    break;
	 case ASTNode.POSTFIX_EXPRESSION :
	    ntyp = typer.findType("int");
	    break;
	 case ASTNode.PREFIX_EXPRESSION :
	    PrefixExpression pfx = (PrefixExpression) p;
	    if (pfx.getOperator() == PrefixExpression.Operator.NOT)
	       ntyp = typer.findType("boolean");
	    else
	       ntyp = typer.findType("int");
	    break;
	 case ASTNode.VARIABLE_DECLARATION_FRAGMENT :
	    computeImpliedType(typer,p);
	    break;
	 case ASTNode.VARIABLE_DECLARATION_STATEMENT :
	    VariableDeclarationStatement vds = (VariableDeclarationStatement) p;
	    ntyp = JavaAst.getJavaType(vds.getType());
	    break;
	 case ASTNode.VARIABLE_DECLARATION_EXPRESSION :
	    VariableDeclarationExpression vde = (VariableDeclarationExpression) p;
	    ntyp = JavaAst.getJavaType(vde.getType());
	    break;
	 case ASTNode.RETURN_STATEMENT :
	    ntyp = implied_type;
	    break;
	 case ASTNode.IF_STATEMENT :
	    ntyp = typer.findType("boolean");
	    break;
	 default :
	    System.err.println("CONTEXT: Unexpected parent: " + p);
	    break;
       }

      if (ntyp != null && base_symbol == null)
	 implied_type = JcompType.mergeTypes(typer,implied_type,ntyp);
    }

   public boolean equals(Object o) {
      if (o instanceof MemberInstance) {
	 MemberInstance mi = (MemberInstance) o;
	 if (mi.member_name.equals(member_name)) {
	    if (mi.member_type == null) return member_type == null;
	    else return mi.member_type.equals(member_type);
	  }
       }
      return false;
    }

   public int hashCode() {
      int hc = member_name.hashCode();
      if (member_type != null) hc ^= member_type.hashCode();
      return hc;
    }

   public String toString() {
      String r = member_name;
      if (member_type != null) r = "(" + r + ":" + member_type.toString() + ")";
      return r;
    }

}	// end of subclass MemberInstance




/********************************************************************************/
/*										*/
/*	MemberSet : class to contain method matchings				*/
/*										*/
/********************************************************************************/

private class MemberSet extends TreeMapper {

   private JcompType original_type;
   private JcompType mapped_type;
   private Map<MemberInstance,JcompSymbol> member_map;
   private Map<ASTNode,MemberInstance> affected_nodes;

   MemberSet(JcompType otyp,JcompType mtyp,Map<MemberInstance,JcompSymbol> mmap) {
      original_type = otyp;
      mapped_type = mtyp;
      member_map = new HashMap<MemberInstance,JcompSymbol>(mmap);
      affected_nodes = new HashMap<ASTNode,MemberInstance>();
      for (MemberInstance mi : member_map.keySet()) {
	 for (ASTNode n : mi.getUses()) {
	    affected_nodes.put(n,mi);
	  }
       }
    }

   @Override protected String getSpecificsName()   { return original_type.getName(); }

   void rewriteTree(ASTNode orig,ASTRewrite rw) {
      MemberInstance mi = affected_nodes.get(orig);
      if (mi != null) {
	 JcompSymbol rs = member_map.get(mi);
	 String rnm = rs.getName();
	 if (rnm.equals("<init>")) rnm = mapped_type.getName();
	 Name sn;

	 try {
	    sn = JavaAst.getQualifiedName(orig.getAST(),rnm);
	  }
	 catch (IllegalArgumentException e) {
	    System.err.println("CONTEXT: BAD NAME " + orig.getAST() + " => " + rs.getName());
	    return;
	  }
	 if (orig instanceof MethodInvocation) {
	    rw.set(orig,MethodInvocation.NAME_PROPERTY,sn,null);
	  }
	 else if (orig instanceof SuperMethodInvocation) {
	    rw.set(orig,SuperMethodInvocation.NAME_PROPERTY,sn,null);
	  }
	 else if (orig instanceof ClassInstanceCreation) {
	    Type t = orig.getAST().newSimpleType(sn);
	    rw.set(orig,ClassInstanceCreation.TYPE_PROPERTY,t,null);
	  }
	 else if (orig instanceof ConstructorInvocation) {
	    System.err.println("CONTEXT: NEED TO HANDLE ConstructorInvocation");
	  }
	 else if (orig instanceof SuperConstructorInvocation) {
	    System.err.println("CONTEXT: NEED TO HANDLE SuperConstructorInvocation");
	  }
	 else if (orig instanceof FieldAccess) {
	    rw.set(orig,FieldAccess.NAME_PROPERTY,sn,null);
	  }
	 else if (orig instanceof QualifiedName) {
	    if (sn.isSimpleName()) {
	       QualifiedName qn = (QualifiedName) orig;
	       rw.set(qn,QualifiedName.NAME_PROPERTY,sn,null);
	     }
	    else {
	       System.err.println("CONTEXT: NEED TO HANDLE QUALIFIED QUALIFIER");
	     }
	  }
	 else {
	    System.err.println("CONTEXT: UNKNOWN AST Map TYPE " + orig.getClass());
	  }
       }
      else {
	 JcompType jt = JavaAst.getJavaType(orig);
	 if (jt == original_type) {
	    if (orig instanceof Type) {
	       Type t = mapped_type.createAstNode(orig.getAST());
	       rw.replace(orig,t,null);
	     }
	    else if (orig instanceof SingleVariableDeclaration) { }
	    else if (orig instanceof SimpleName) { }
	    else if (orig instanceof TypeDeclaration) { }
	    else {
	       System.err.println("CONTEXT: UNKNOWN AST TYPE " + orig.getClass());
	     }
	  }
       }

    }


}	// end of subclass MemberSet




}	// end of class TransformContext




/* end of TransformContext.java */
