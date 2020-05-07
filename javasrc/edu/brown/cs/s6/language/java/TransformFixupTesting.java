/********************************************************************************/
/*										*/
/*		TransformFixupTesting.java					*/
/*										*/
/*	Remove duplicate solutions						*/
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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import edu.brown.cs.cose.cosecommon.CoseSource;
import edu.brown.cs.ivy.file.IvyLog;
import edu.brown.cs.ivy.jcomp.JcompSymbol;
import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Fragment;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6Solution;
import edu.brown.cs.s6.common.S6SolutionSet;
import edu.brown.cs.s6.common.S6TestResults;

public class TransformFixupTesting extends TransformJava implements S6Constants, JavaConstants
{


/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public TransformFixupTesting(String name)
{
   super(name);
}



/********************************************************************************/
/*										*/
/*	Transform Setup 							*/
/*										*/
/********************************************************************************/

@Override protected TreeMapper findClassMapping(S6SolutionSet sols,TypeDeclaration td,
      S6Request.ClassSignature csg,S6Solution sol)
{
   if (!sols.doTestFixup()) {
      return null;
    }

   TestFixupMapper tfm = new TestFixupMapper(sols,td);

   return tfm;
}


/********************************************************************************/
/*										*/
/*	Actual Mapping classes							*/
/*										*/
/********************************************************************************/

private class TestFixupMapper extends TreeMapper {

   private S6SolutionSet solution_set;
   private TypeDeclaration first_type;
   private Map<CoseSource,S6Solution> use_sources;

   TestFixupMapper(S6SolutionSet sols,TypeDeclaration td) {
      solution_set = sols;
      first_type = td;
      use_sources = new HashMap<>();
      for (S6Solution sol : sols.getSolutions()) {
	 if (!sol.checkFlag(S6SolutionFlag.PASS)) continue;
	 CoseSource src = sol.getSource();
	 S6Solution altsol = use_sources.get(src);
	 if (altsol == null) {
	    use_sources.put(src,sol);
	  }
	 else {
	    double t1 = getScore(sol);
	    double t2 = getScore(altsol);
	    if (t1 > t2) use_sources.put(src,sol);
	    else sol.setFlag(S6SolutionFlag.REMOVE);
	  }
       }
    }



   @Override void rewriteTree(ASTNode orig,ASTRewrite rw) {
       if (orig == first_type) {
          CompilationUnit cu = (CompilationUnit) orig.getRoot();
          ListRewrite outers = null;
          for (Object o : cu.types()) {
             if (o == first_type) continue;
             AbstractTypeDeclaration atd = (AbstractTypeDeclaration) o;
             if (outers == null) {
        	outers = rw.getListRewrite(cu,CompilationUnit.TYPES_PROPERTY);
              }
             outers.remove(atd,null);
           }
          TestTypeBuilder ttd = new TestTypeBuilder(rw.getAST());
          ttd.setup(first_type);
          Set<S6Solution> use = new HashSet<S6Solution>(use_sources.values());
          Set<JcompType> imports = new HashSet<JcompType>();
          for (S6Solution sol : solution_set.getSolutions()) {
             if (use.contains(sol)) {
        	JavaFragment f = (JavaFragment) sol.getFragment();
        	f.resolveFragment();
        	Collection<JcompType> imp = f.getImportTypes();
        	imports.addAll(imp);
        	TypeDeclaration std = (TypeDeclaration) f.getAstNode();
        	ttd.addItems(std);
              }
             sol.setFlag(S6SolutionFlag.REMOVE);
           }
   
          TypeDeclaration ntd = ttd.getTypeDeclaration();
          TypeDeclaration otd = (TypeDeclaration) orig;
          for (Object o : otd.modifiers()) {
             if (o instanceof Modifier) {
        	Modifier md = (Modifier) o;
        	if (md.isStatic()) {
        	   ListRewrite mlrw = rw.getListRewrite(otd,TypeDeclaration.MODIFIERS2_PROPERTY);
        	   mlrw.remove(md,null);
        	   break;
        	 }
              }
           }
          if (otd.getSuperclassType() != null) {
             JcompType jt = JavaAst.getJavaType(otd.getSuperclassType());
             if (jt != null && jt.getName().equals("org.junit.Assert")) {
        	rw.remove(otd.getSuperclassType(),null);
              }
           }
          ListRewrite blrw = rw.getListRewrite(otd,TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
          for (Object o : otd.bodyDeclarations()) {
             BodyDeclaration bd = (BodyDeclaration) o;
             // System.err.println("REMOVE: " + bd.hashCode() + " " + bd);
             blrw.remove(bd,null);
           }
          for (Object o : ntd.bodyDeclarations()) {
             BodyDeclaration bd = (BodyDeclaration) o;
             blrw.insertLast(bd,null);
             // System.err.println("INSERT: " + bd.hashCode() + " " + bd);
           }
          // rw.replace(orig,ntd,null);
   
          ListRewrite lrw = rw.getListRewrite(cu,CompilationUnit.IMPORTS_PROPERTY);
          for (Object o : cu.imports()) {
             lrw.remove((ASTNode) o,null);
           }
          for (JcompType jt : imports) {
             String tnm = jt.getName();
             int idx = tnm.lastIndexOf(".");
             if (idx < 0) continue;
             String pkg = tnm.substring(0,idx);
             if (pkg.equals("java.lang")) continue;
             if (tnm.equals("org.junit.Assert")) continue;
             ImportDeclaration id = rw.getAST().newImportDeclaration();
             Name nm = JavaAst.getQualifiedName(rw.getAST(),tnm);
             id.setName(nm);
             lrw.insertLast(id,null);
           }
   
          // System.err.println("TRANSFORM: " + rw);
        }
    }

   private double getScore(S6Solution sol) {
      S6Fragment frag = sol.getFragment();
      if (sol.getFragment() == null) {
         System.err.println("SOLUTION LACKS FRAGMENT: " + sol);
         return 0;
       }
   
      S6TestResults tr = frag.getTestResults();
      int nsucc = 0;
      int nfail = 0;
      for (String s : tr.getMessageResults()) {
         StringTokenizer tok = new StringTokenizer(s);
         try {
            int ns = Integer.parseInt(tok.nextToken());
            int nf = Integer.parseInt(tok.nextToken());
            nsucc += ns;
            nfail += nf;
          }
         catch (Throwable t) { }
       }
      int cm = frag.getCodeComplexity();
   
      double score = nsucc * 1024 - nfail * 512 - cm / 64.0;
   
      return score;
    }


}	// end of inner class TestFixupMapper




/********************************************************************************/
/*										*/
/*	Class to build and fixup result 					*/
/*										*/
/********************************************************************************/

private class TestTypeBuilder {

   private AST		use_ast;
   private Set<String>	used_names;
   private TypeDeclaration result_type;
   private MethodDeclaration result_constructor;
   private Map<String,String> name_map;

   TestTypeBuilder(AST ast) {
      use_ast = ast;
      used_names = new HashSet<String>();
      result_type = null;
      result_constructor = null;
      name_map = new HashMap<String,String>();
    }

   TypeDeclaration getTypeDeclaration() {
      return result_type;
    }

   @SuppressWarnings("unchecked")
   void setup(TypeDeclaration td) {
      result_type = use_ast.newTypeDeclaration();
      result_type.setName((SimpleName) ASTNode.copySubtree(use_ast,td.getName()));
      for (Object o : td.modifiers()) {
         ASTNode iem = (ASTNode) o;
         IExtendedModifier iem1 = (IExtendedModifier) ASTNode.copySubtree(use_ast,iem);
         if (iem1.isModifier()) {
            Modifier md = (Modifier) iem1;
            if (md.isStatic()) continue;
          }
         else continue;
         result_type.modifiers().add(iem1);
   
       }
      if (td.getSuperclassType() != null) {
         JcompType jt = JavaAst.getJavaType(td.getSuperclassType());
         if (jt != null && jt.getName().equals("org.junit.Assert")) ;
         else {
            result_type.setSuperclassType((Type) ASTNode.copySubtree(use_ast,td.getSuperclassType()));
          }
       }
      for (Object o : td.superInterfaceTypes()) {
         Type t = (Type) o;
         Type t1 = (Type) ASTNode.copySubtree(use_ast,t);
         result_type.superInterfaceTypes().add(t1);
       }
      IvyLog.logI("JAVA","Set test result: " + result_type);
    }

   @SuppressWarnings("unchecked")
   void addItems(TypeDeclaration td) {
      setupNameMappings(td);
      for (Object o : td.bodyDeclarations()) {
	 BodyDeclaration bd0 = (BodyDeclaration) o;
	 BodyDeclaration bd1 = (BodyDeclaration) ASTNode.copySubtree(use_ast,bd0);
	 propogateSymbols(bd0,bd1);
	 NameFixer nf = new NameFixer(name_map);
	 bd1.accept(nf);
	 if (bd1 instanceof MethodDeclaration) {
	    MethodDeclaration md = (MethodDeclaration) bd1;
	    if (md.isConstructor() && md.parameters().isEmpty()) {
	       if (result_constructor == null) {
		  result_constructor = md;
		  for (Iterator<?> it = md.getBody().statements().iterator(); it.hasNext(); ) {
		     Statement s = (Statement) it.next();
		     if (s instanceof SuperConstructorInvocation ||
			   s instanceof ConstructorInvocation) {
			it.remove();
		      }
		   }
		}
	       else {
		  addToConstructor(md);
		  continue;
		}
	     }
	    else if (md.isConstructor()) {
	       continue;
	     }
	    // handle internal name conflicts with field names here
	  }
	 else if (bd1 instanceof FieldDeclaration) {
	    FieldDeclaration fd = (FieldDeclaration) bd0;
	    JcompType jt = JavaAst.getJavaType(fd.getType());
	    FieldDeclaration fd1 = (FieldDeclaration) bd1;
	    if (jt != null) {
	       for (Object o1: fd1.fragments()) {
		  VariableDeclarationFragment vdf = (VariableDeclarationFragment) o1;
		  if (vdf.getInitializer() == null) {
		int ct = vdf.getExtraDimensions();
		JcompType djt = jt;
		for (int i = 0; i < ct; ++i) {
		   djt = JcompType.createArrayType(djt);
		 }
		     Expression ex = djt.createDefaultValue(use_ast);
		     vdf.setInitializer(ex);
		   }
		}
	     }
	  }
	 // System.err.println("S6 TEST: Add declaration " + bd1);
	 result_type.bodyDeclarations().add(bd1);
       }
    }

   private void setupNameMappings(TypeDeclaration td) {
      name_map.clear();
      name_map.put("*TYPE*",td.getName().getIdentifier());
      String pkgnm = getPackageName(td);
      if (pkgnm != null) name_map.put("*PACKAGE*",pkgnm);
      for (Object o : td.bodyDeclarations()) {
	 BodyDeclaration bd = (BodyDeclaration) o;
	 if (bd instanceof AbstractTypeDeclaration) {
	    AbstractTypeDeclaration atd = (AbstractTypeDeclaration) bd;
	    defineName(atd.getName());
	  }
	 else if (bd instanceof MethodDeclaration) {
	    MethodDeclaration md = (MethodDeclaration) bd;
	    defineName(md.getName());

	  }
	 else if (bd instanceof FieldDeclaration) {
	    FieldDeclaration fd = (FieldDeclaration) bd;
	    for (Object o1 : fd.fragments()) {
	       VariableDeclarationFragment vdf = (VariableDeclarationFragment) o1;
	       defineName(vdf.getName());
	     }
	  }
       }
    }

   private String getPackageName(ASTNode n) {
      if (n instanceof CompilationUnit) {
	 CompilationUnit cu = (CompilationUnit) n;
	 PackageDeclaration pd = cu.getPackage();
	 if (pd == null) return null;
	 else return pd.getName().getFullyQualifiedName();
       }
      return getPackageName(n.getParent());
    }

   private void defineName(SimpleName sn) {
      defineName(sn.getIdentifier());
    }

   private void defineName(String nm) {
      if (used_names.add(nm)) return;
      for (int i = 1; ; ++i) {
	 String nm1 = nm + "_" + i;
	 if (used_names.add(nm1)) {
	    name_map.put(nm,nm1);
	    break;
	  }
       }
    }

   @SuppressWarnings("unchecked")
   private void addToConstructor(MethodDeclaration md) {
      Block db = result_constructor.getBody();
      for (Object o : md.getBody().statements()) {
	 Statement s = (Statement) o;
	 if (s instanceof SuperConstructorInvocation) continue;
	 if (s instanceof ConstructorInvocation) continue;
	 Statement s1 = (Statement) ASTNode.copySubtree(use_ast,s);
	 db.statements().add(s1);
       }
    }

}	// end of inner class TestTypeBuilder



private static class NameFixer extends ASTVisitor {

   private Map<String,String> name_map;
   private String package_name;
   private String class_name;

   NameFixer(Map<String,String> nm) {
      name_map = nm;
      package_name = name_map.get("*PACKAGE*");
      class_name = name_map.get("*TYPE*");
    }

   @Override public boolean visit(QualifiedName qn) {
      String fqn = qn.getFullyQualifiedName();
      String match = class_name;
      if (package_name != null) match = package_name + match;
      if (!fqn.startsWith(match)) return false;
      return true;
    }

   @Override public void endVisit(QualifiedName qn) {
      String fqn = qn.getFullyQualifiedName();
      if (fqn.equals("edu.brown.cs.s6.runner.RunnerAssert")) {
	 Name nm1 = JavaAst.getQualifiedName(qn.getAST(),"org.junit");
	 qn.setQualifier(nm1);
	 SimpleName sn1 = JavaAst.getSimpleName(qn.getAST(),"Assert");
	 qn.setName(sn1);
       }
    }

   @Override public void endVisit(SimpleName sn) {
      String id = sn.getIdentifier();
      String nid = name_map.get(id);
      if (nid == null) return;

      JcompSymbol js = JavaAst.getReference(sn);
      if (js == null) js = JavaAst.getDefinition(sn);
      if (js != null) {
	 String fnm = js.getFullName();
	 String match = class_name;
	 if (package_name != null) match = package_name + "." + match;
	 if (!fnm.startsWith(match)) return;
       }

      sn.setIdentifier(nid);
    }

   @Override public boolean visit(ExpressionStatement es) {
      if (es.getExpression() instanceof MethodInvocation) {
	 MethodInvocation mi = (MethodInvocation) es.getExpression();
	 String mnm = mi.getName().getIdentifier();
	 if (mnm.equals("succeed") || mnm.equals("failDefault")) {
	    Expression exp = mi.getExpression();
	    if (exp != null && exp instanceof QualifiedName) {
	       QualifiedName qn = (QualifiedName) exp;
	       if (qn.getFullyQualifiedName().equals("edu.brown.cs.s6.runner.RunnerAssert")) {
		  Block blk = (Block) es.getParent();
		  blk.statements().remove(es);
		  return false;
		}
	     }
	  }
       }
      return true;
   }

}	// end of inner class NameFixer



/********************************************************************************/
/*										*/
/*	Propogate names to a copied subtree					*/
/*										*/
/********************************************************************************/

private void propogateSymbols(ASTNode n1,ASTNode n2)
{
   n1.subtreeMatch(new PropMatcher(),n2);
}

private static class PropMatcher extends ASTMatcher {

   @Override public boolean match(SimpleName n,Object o) {
      boolean fg = super.match(n,o);
      if (fg) copy(n,(ASTNode) o);
      return fg;
    }

   @Override public boolean match(QualifiedName n,Object o) {
      boolean fg = super.match(n,o);
      if (fg) copy(n,(ASTNode) o);
      return fg;
    }

   @Override public boolean match(MethodInvocation n,Object o) {
      boolean fg = super.match(n,o);
      if (fg) copy(n,(ASTNode) o);
      return fg;
    }

   private void copy(ASTNode n1,ASTNode n2) {
      JcompSymbol js = JavaAst.getDefinition(n1);
      if (js != null) JavaAst.setDefinition(n2,js);
      js = JavaAst.getReference(n1);
      if (js != null) JavaAst.setReference(n2,js);
    }

}	// end of inner class PropMatcher



}	// end of class TransformFixupTesting




/* end of TransformFixupTesting.java */

