/********************************************************************************/
/*										*/
/*		JavaDepends.java						*/
/*										*/
/*	Code to handle dependency analysis for Java fragments			*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/language/java/JavaDepends.java,v 1.16 2016/07/18 23:05:25 spr Exp $ */


/*********************************************************************************
 *
 * $Log: JavaDepends.java,v $
 * Revision 1.16  2016/07/18 23:05:25  spr
 * Update transforms for applications and UI.
 *
 * Revision 1.15  2015/09/23 17:54:52  spr
 * Version to handle andriod UI applications.
 *
 * Revision 1.14  2014/08/29 15:16:08  spr
 * Updates for suise, testcases.
 *
 * Revision 1.13  2014/02/26 14:06:42  spr
 * Add transformations for user interfaces.
 *
 * Revision 1.12  2013/09/13 20:33:03  spr
 * Add calls for UI search.
 *
 * Revision 1.11  2013-05-09 12:26:19  spr
 * Minor changes to start ui fixups.
 *
 * Revision 1.10  2012-08-13 16:51:51  spr
 * Add new transforms, clean up code, bug fixes.
 *
 * Revision 1.9  2012-07-20 22:15:20  spr
 * New transforms and resolution for UI search
 *
 * Revision 1.8  2012-06-11 14:07:49  spr
 * add framework search; fix bugs
 *
 * Revision 1.7  2009-09-18 01:41:35  spr
 * Handle user testing.
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
 * Revision 1.3  2008-07-17 13:46:45  spr
 * Bug fixes and speed ups.
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
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.*;

import edu.brown.cs.ivy.jcomp.JcompSymbol;
import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Solution;
import edu.brown.cs.s6.common.S6SolutionSet;
import edu.brown.cs.s6.common.S6Transform;



class JavaDepends implements S6Constants, JavaConstants {




/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private S6SolutionSet		solution_set;
private S6Solution		base_solution;
private AbstractTypeDeclaration base_type;
private CompilationUnit 	package_unit;
private Set<BodyDeclaration>	use_nodes;
private CheckTypes		check_types;
private CheckDefs		check_defs;
private boolean 		use_class;
private boolean 		use_package;

private static final Pattern ANON_PATTERN = Pattern.compile(".*\\.\\$\\d+");



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

JavaDepends(S6SolutionSet ss,S6Solution sol,BodyDeclaration nd)
{
   solution_set = ss;
   base_solution = sol;
   use_nodes = new HashSet<BodyDeclaration>();

   if (nd instanceof AbstractTypeDeclaration) {
      base_type = (AbstractTypeDeclaration) nd;
      use_class = true;
    }
   else {
      ASTNode tn = nd;
      while (tn != null && !(tn instanceof AbstractTypeDeclaration)) tn = tn.getParent();
      base_type = (AbstractTypeDeclaration) tn;
      use_nodes.add(nd);
      use_class = false;
    }

   use_package = false;
   check_types = new CheckTypes(base_type);
   check_defs = new CheckDefs(ss.getSearchType(),base_type);
}


JavaDepends(S6SolutionSet ss,S6Solution sol,CompilationUnit cu)
{
   solution_set = ss;
   base_solution = sol;
   use_nodes = new HashSet<BodyDeclaration>();
   use_package = true;
   package_unit = cu;

   check_types = new CheckTypes(cu);
   check_defs = new CheckDefs(ss.getSearchType(),cu);
}




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

void addDeclaration(BodyDeclaration bd) 	{ use_nodes.add(bd); }


boolean getUseConstructor()			{ return check_defs.useConstructor(); }

Collection<JcompType> getImportTypes()		{ return check_types.getImports(); }

Collection<BodyDeclaration> getDeclarations()	{ return use_nodes; }




/********************************************************************************/
/*										*/
/*	Dependency resolution methods						*/
/*										*/
/********************************************************************************/

boolean findDependencies()
{
   if (solution_set.doDebug()) {
      if (base_solution.getTransforms() != null) {
	 System.err.println("DEPENDENCIES FOR: ");
	 for (S6Transform.Memo m : base_solution.getTransforms()) {
	    System.err.print(" " + m.getTransformName());
	  }
	 System.err.println();
       }
      System.err.println("SOURCE: " + base_solution.getSource().getName() + " " +
			    base_solution.getScore()); 

      if (use_class) System.err.println("WORK ON CLASS " + base_type);
      else if (use_package) System.err.println("WORK ON PACKAGE " + package_unit);
      else {
	 for (BodyDeclaration bd : use_nodes) {
	    System.err.println("WORK ON METHOD: " + bd);
	  }
       }
    }

   boolean rslt = checkDependencies(check_types,check_defs);

   if (rslt && check_defs.hasError()) {
      if (solution_set.doDebug()) {
	 System.err.println("S6: ERRORS found during successful dependency check");
	 check_defs.printErrors();
       }
      rslt = false;
    }

   if (solution_set.doDebug()) System.err.println("DEPEND RESULT = " + rslt);

   return rslt;
}



private boolean checkDependencies(CheckTypes typs,CheckDefs defs)
{
   boolean chng = true;
   while (chng) {
      chng = false;
      if (use_class) {
	 if (base_type instanceof TypeDeclaration) {
	    TypeDeclaration td = (TypeDeclaration) base_type;
	    if (td.getSuperclassType() != null) td.getSuperclassType().accept(typs);
	    for (Iterator<?> it = td.superInterfaceTypes().iterator(); it.hasNext(); ) {
	       Type t = (Type) it.next();
	       t.accept(typs);
	     }
	  }
       }
      else if (use_package) {
	  package_unit.accept(typs);
	  package_unit.accept(defs);
	  boolean fg1 = typs.finalCheck();
	  if (!fg1) return false;
	  return true;
       }
      for (BodyDeclaration bd : use_nodes) bd.accept(typs);
      Collection<BodyDeclaration> newnodes = typs.fixup();
      chng |= use_nodes.addAll(newnodes);
      defs.clearError();
      for (BodyDeclaration bd : use_nodes) bd.accept(defs);
      newnodes = defs.fixup();
      chng |= use_nodes.addAll(newnodes);
    }

   return typs.finalCheck();
}




/********************************************************************************/
/*										*/
/*	Visitor to handle type references					*/
/*										*/
/********************************************************************************/

private class CheckTypes extends ASTVisitor {

   private ASTNode base_node;
   private CompilationUnit ast_root;
   private Set<JcompType> import_types;
   private Set<JcompType> defined_types;
   private Set<JcompType> refd_types;

   CheckTypes(ASTNode n) {
      base_node = n;
      ast_root = (CompilationUnit) n.getRoot();
      import_types = new HashSet<JcompType>();
      refd_types = new HashSet<JcompType>();
      defined_types = new HashSet<JcompType>();
      ASTNode tn = n;
      while (tn != null && !(tn instanceof TypeDeclaration)) tn = tn.getParent();
      if (tn != null) {
	 JcompType jt = JavaAst.getJavaType(tn);
	 defined_types.add(jt);
       }
    }

   Collection<JcompType> getImports()		{ return import_types; }

   public boolean preVisit2(ASTNode n) {
      JcompType jt = JavaAst.getJavaType(n);
      if (jt != null) {
	 if (jt.isClassType() || jt.isEnumType() || jt.isInterfaceType()) {
	    switch (n.getNodeType()) {
	       case ASTNode.TYPE_DECLARATION :
	       case ASTNode.ENUM_DECLARATION :
		  defined_types.add(jt);
		  break;
	       case ASTNode.QUALIFIED_NAME :
		  QualifiedName qname = (QualifiedName) n;
		  String id1 = qname.getFullyQualifiedName();
		  if (jt.getName().equals(id1))
		     return false;
		  JcompType jt1 = JavaAst.getJavaType(qname.getQualifier());
		  addType(jt1);
		  return false;
	       case ASTNode.QUALIFIED_TYPE :
		  return false;
	       case ASTNode.SIMPLE_TYPE :
		  // handle via underlying name
		  break;
	       default :
		  addType(jt);
		  break;
	     }
	  }
       }
      return true;
   }


   private void addType(JcompType jt) {
      if (!jt.isTypeVariable()) {
	 refd_types.add(jt);
	 int idx = jt.getName().indexOf(".");
	 if (idx >= 0) {
	    import_types.add(jt);
	  }
       }
   }

   Collection<BodyDeclaration> fixup() {
      Set<BodyDeclaration> newnodes = new HashSet<BodyDeclaration>();
      ASTNode par = base_node;
      while (par != null) {
	 if (par instanceof TypeDeclaration) {
	    TypeDeclaration ptyp = (TypeDeclaration) par;
	    for (TypeDeclaration td : ptyp.getTypes()) {
	       JcompType jt = JavaAst.getJavaType(td);
	       if (jt != null && refd_types.contains(jt) &&!defined_types.contains(jt) &&
		     !jt.isTypeVariable()) {
		  if (solution_set.doDebug()) System.err.println("ADD TYPE " + td);
		  newnodes.add(td);
		}
	     }
	  }
	 par = par.getParent();
       }

      return newnodes;
    }

   boolean finalCheck() {
      boolean allok = true;
      String pkg = null;
      PackageDeclaration pd = ast_root.getPackage();
      if (pd != null) pkg = pd.getName().getFullyQualifiedName() + ".";

      for (JcompType jt : refd_types) {
	 if (jt.getName().contains("Anon@")) {
	    import_types.remove(jt);
	    continue;
	  }
	 Matcher match = ANON_PATTERN.matcher(jt.getName());
	 if (match.matches()) {
	    import_types.remove(jt);
	    continue;
	  }
	 if (defined_types.contains(jt)) {
	    import_types.remove(jt);
	  }
	 else if (pkg != null && jt.getName().startsWith(pkg)) {
	    if (jt.isBinaryType()) {
	       import_types.remove(jt);
	     }
	    else {
	       if (solution_set.doDebug()) System.err.println("DEPEND: Missing Package Type " + jt.getName());
	       allok = false;
	     }
	  }
	 else if (!import_types.contains(jt)) {
	    if (solution_set.doDebug()) System.err.println("DEPEND: Missing Type " + jt.getName());
	    allok = false;
	 }
       }

      return allok;
    }

}	// end of subclass CheckTypes




/********************************************************************************/
/*										*/
/*	Visitor to check definitions						*/
/*										*/
/********************************************************************************/

private class CheckDefs extends ASTVisitor {

   private S6SearchType search_type;
   private ASTNode base_node;
   private Set<JcompSymbol> defined_names;
   private Set<JcompSymbol> ref_names;
   private boolean have_constructor;
   private boolean need_constructor;
   private boolean have_error;
   private Collection<ASTNode> error_nodes;

   CheckDefs(S6SearchType st,ASTNode n) {
      search_type = st;
      base_node = n;
      defined_names = new HashSet<JcompSymbol>();
      ref_names = new HashSet<JcompSymbol>();
      have_constructor = false;
      need_constructor = false;
      have_error = false;
      error_nodes = new ArrayList<ASTNode>();
    }

   boolean useConstructor()		{ return need_constructor; }
   void clearError()			{ have_error = false; error_nodes.clear(); }
   boolean hasError()			{ return have_error; }

   void printErrors() {
      for (ASTNode n : error_nodes) {
	 System.err.println("ERROR IN: " + n);
       }
    }

   @Override public boolean preVisit2(ASTNode n) {
      boolean goon = true;
      JcompSymbol js = JavaAst.getDefinition(n);
      if (js != null) {
	 // System.err.println("DEF " + js.getName() + " " + n);
	 defined_names.add(js);
       }
      js = JavaAst.getReference(n);
      if (js != null) {
	 ref_names.add(js);
	 // System.err.println("REF " + js.getName());
       }
      JcompType jt = JavaAst.getExprType(n);
      if (jt != null && jt.isErrorType()) {
	 boolean err = true;
	 if (solution_set.getSearchType() == S6SearchType.ANDROIDUI) {
	    if (n instanceof QualifiedName) {
	       String qn = ((QualifiedName) n).getFullyQualifiedName();
	       if (qn.startsWith("R.") || qn.contains(".R.")) {
		  err = false;
		  goon = false;
		}
	     }
	    else if (n instanceof SimpleName) {
	       if (((SimpleName) n).getIdentifier().equals("R")) err = false;
	       if (((SimpleName) n).getIdentifier().equals("edit_contact"))
		  System.err.println("CHECK EDIT_CONTACT");
	     }
	    else if (n instanceof MethodInvocation) {
	       MethodInvocation mi = (MethodInvocation) n;
	       boolean argerr = false;
	       for (Object o : mi.arguments()) {
		  ASTNode n1 = (ASTNode) o;
		  JcompType argtyp = JavaAst.getExprType(n1);
		  if (argtyp != null && argtyp.isErrorType()) argerr = true;
		}
	       if (argerr)
		  err = false;
	     }
	    else if (n instanceof ConditionalExpression) {
	       ConditionalExpression cexp = (ConditionalExpression) n;
	       boolean argerr = false;
	       JcompType t1 = JavaAst.getExprType(cexp.getThenExpression());
	       if (t1 != null && t1.isErrorType()) argerr = true;
	       t1 = JavaAst.getExprType(cexp.getElseExpression());
	       if (t1 != null && t1.isErrorType()) argerr = true;
	       if (argerr) err = false;
	     }
	    else {
	       System.err.println("ANDROID ERROR: " + n);
	     }
	  }
	 if (err) {
	    error_nodes.add(n);
	    have_error = true;
	  }
       }

      return goon;
    }

   Collection<BodyDeclaration> fixup() {
      Set<BodyDeclaration> newnodes = new HashSet<BodyDeclaration>();
      for (JcompSymbol ds : defined_names) {
	 ASTNode dn = ds.getDefinitionNode();
	 while (dn != null && !(dn instanceof BodyDeclaration)) dn = dn.getParent();
	 if (dn != null) {
	    BodyDeclaration bd = (BodyDeclaration) dn;
	    if (!Modifier.isStatic(bd.getModifiers())) {
	       ASTNode par = bd.getParent();
	       if (par == base_node || (par.getParent() instanceof CompilationUnit && par.getParent() == base_node))
		  need_constructor = true;
	     }
	  }
       }

      for (JcompSymbol rs : ref_names) {
	 if (defined_names.contains(rs)) continue;
	 ASTNode dn = rs.getDefinitionNode();
	 while (dn != null && !(dn instanceof BodyDeclaration)) dn = dn.getParent();
	 if (dn != null) {
	    BodyDeclaration bd = (BodyDeclaration) dn;
	    if (Modifier.isAbstract(bd.getModifiers())) {
	       if (solution_set.doDebug()) {
		  System.err.println("ATTEMPT TO USE ABSTRACT DEF " + dn);
		}
	     }
	    else {
	       newnodes.add(bd);
	       if (solution_set.doDebug()) System.err.println("ADD DEF " + dn);
	     }
	  }
	 else if (!rs.isBinarySymbol()) {
	    if (solution_set.doDebug()) System.err.println("DEF NOT FOUND " + rs.getName());
	  }
       }

      for (Iterator<BodyDeclaration> it = newnodes.iterator(); it.hasNext(); ) {
	 BodyDeclaration bd = it.next();
	 if (bd.getParent() == base_node) continue;
	 if (base_node instanceof CompilationUnit && bd.getParent().getParent() == base_node) continue;
	 it.remove();
       }

      if (need_constructor && !have_constructor && search_type == S6SearchType.METHOD) {
	 boolean cnstfnd = false;
	 have_constructor = true;
	 ASTNode cn = base_node;
	 while (cn != null && !(cn instanceof TypeDeclaration)) cn = cn.getParent();
	 TypeDeclaration td = (TypeDeclaration) cn;
	 int ncnst = 0;
	 if (td != null && !td.isInterface()) {
	    for (MethodDeclaration md : td.getMethods()) {
	       if (md.isConstructor() && md.parameters().size() == 0) {
		  cnstfnd = true;
		  JcompSymbol js = JavaAst.getDefinition(md);
		  if (!defined_names.contains(js)) {
		     newnodes.add(md);
		     if (solution_set.doDebug()) System.err.println("ADD CONSTRUCTOR " + md);
		   }
		}
	       else if (md.isConstructor()) ++ncnst;
	     }

	  }
	 if (!cnstfnd && ncnst > 0) {
	    have_error = true;
	  }
       }

      return newnodes;
    }


}	// end of subclass CheckDefs




}	// end of class JavaDepends



/* end of JavaDepends.java */
