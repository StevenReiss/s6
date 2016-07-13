/********************************************************************************/
/*										*/
/*		TransformMapNames.java						*/
/*										*/
/*	Map non-standard library names to standard ones 			*/
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


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6Solution;
import edu.brown.cs.s6.common.S6SolutionSet;


public class TransformMapNames extends TransformJava implements S6Constants, JavaConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private static Map<String,String> name_map;
static {
   name_map = new HashMap<String,String>();

   name_map.put("org.jdesktop.layout","javax.swing");
   name_map.put("org.jdesktop.layout.GroupLayout.LEADING","javax.swing.GroupLayout.Alignment.LEADING");
   name_map.put("GroupLayout.LEADING","GroupLayout.Alignment.LEADING");
   name_map.put("org.jdesktop.layout.GroupLayout.CENTER","javax.swing.GroupLayout.Alignment.CENTER");
   name_map.put("GroupLayout.CENTER","GroupLayout.Alignment.CENTER");
   name_map.put("org.jdesktop.layout.GroupLayout.BASELINE","javax.swing.GroupLayout.Alignment.BASELINE");
   name_map.put("GroupLayout.BASELINE","GroupLayout.Alignment.BASELINE");
   name_map.put("org.jdesktop.layout.GroupLayout.TRAILING","javax.swing.GroupLayout.Alignment.TRAILING");
   name_map.put("GroupLayout.TRAILING","GroupLayout.Alignment.TRAILING");

   name_map.put("java.util.ResourceBundle.getBundle","edu.brown.cs.s6.runner.RunnerResourceBundle.getDummyBundle");
   name_map.put("ResourceBundle.getBundle","edu.brown.cs.s6.runner.RunnerResourceBundle.getDummyBundle");
}


/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public TransformMapNames(String name)
{
   super(name);
}



/********************************************************************************/
/*										*/
/*	Handle checking and mapping names here					*/
/*										*/
/********************************************************************************/

@Override protected TreeMapper findPackageMapping(S6SolutionSet ss,
      CompilationUnit cu,
      S6Request.PackageSignature psg,S6Solution sol)
{
   MapFinder mf = new MapFinder();
   cu.accept(mf);
   if (!mf.hasChanges()) return null;

   NameMapFixup fix = new NameMapFixup(mf);

   return fix;
}


/********************************************************************************/
/*										*/
/*	Tree walker to find name changes					*/
/*										*/
/********************************************************************************/

private static class MapFinder extends ASTVisitor {

   private Set<ASTNode> to_change;

   MapFinder() {
      to_change = new HashSet<ASTNode>();
    }

   boolean hasChanges() {
      return to_change.size() > 0;
    }

   @Override public void endVisit(QualifiedName n) {
      String qn = n.getFullyQualifiedName();
      if (name_map.containsKey(qn))
	 to_change.add(n);
    }

   @Override public void endVisit(MethodInvocation n) {
      Expression ex = n.getExpression();
      if (ex != null && ex instanceof Name) {
	 Name nm = (Name) ex;
	 String pfx = nm.getFullyQualifiedName();
	 String mthd = pfx + "." + n.getName().getIdentifier();
	 if (name_map.containsKey(mthd))
	    to_change.add(n);
       }
    }

   @Override public void endVisit(FieldAccess n) {
      Expression ex = n.getExpression();
      if (ex != null && ex instanceof Name) {
	 Name nm = (Name) ex;
	 String pfx = nm.getFullyQualifiedName();
	 String mthd = pfx + "." + n.getName().getIdentifier();
	 if (name_map.containsKey(mthd))
	    to_change.add(n);
       }
    }

   @Override public void endVisit(QualifiedType n) {
      Type t = n.getQualifier();
      if (t instanceof SimpleType) {
	 SimpleType st = (SimpleType) t;
	 String pfx = st.getName().getFullyQualifiedName();
	 String typ = pfx + "." + n.getName().getIdentifier();
	 if (name_map.containsKey(typ))
	    to_change.add(n);
       }
    }

   boolean fixNode(ASTNode n) {
      return to_change.contains(n);
    }

}	// end of inner class MapFinder


/********************************************************************************/
/*										*/
/*	Class to do the tree rewriting						*/
/*										*/
/********************************************************************************/

private class NameMapFixup extends TreeMapper {

   private MapFinder map_finder;

   NameMapFixup(MapFinder mf) {
      map_finder = mf;
    }

   @Override void rewriteTree(ASTNode orig,ASTRewrite rw) {
      if (map_finder.fixNode(orig)) {
	 switch (orig.getNodeType()) {
	    case ASTNode.QUALIFIED_NAME :
	       fixQualifiedName((QualifiedName) orig,rw);
	       break;
	    case ASTNode.METHOD_INVOCATION :
	       fixMethodInvocation((MethodInvocation) orig,rw);
	       break;
	    case ASTNode.FIELD_ACCESS :
	       fixFieldAccess((FieldAccess) orig,rw);
	       break;
	    case ASTNode.QUALIFIED_TYPE :
	       fixQualifiedType((QualifiedType) orig,rw);
	       break;
	  }
       }
    }

   private void fixQualifiedName(QualifiedName n,ASTRewrite rw) {
      String nm = n.getFullyQualifiedName();
      String rslt = name_map.get(nm);
      if (rslt == null) return;
      Name rep = JavaAst.getQualifiedName(rw.getAST(),rslt);
      rw.replace(n,rep,null);
    }

   private void fixMethodInvocation(MethodInvocation n,ASTRewrite rw) {
      Expression ex = n.getExpression();
      String rep = null;
      String pfx = null;
      String omthd = n.getName().getIdentifier();
      if (ex != null && ex instanceof Name) {
	 Name nm = (Name) ex;
	 pfx = nm.getFullyQualifiedName();
	 String fmthd = pfx + "." + omthd;
	 rep = name_map.get(fmthd);
       }
      if (rep == null) return;
      int idx = rep.lastIndexOf(".");
      String npfx = null;
      String nmthd = null;
      if (idx < 0) {
	 nmthd = rep;
       }
      else {
	 npfx = rep.substring(0,idx);
	 nmthd = rep.substring(idx+1);
       }
      // System.err.println("MAP NAME FIX " + n + " " + rep + " " + npfx + " " + pfx + " " + nmthd + " " + omthd);
      // System.err.println("FIX IN " + n.getParent());
      MethodInvocation nmi = rw.getAST().newMethodInvocation();
      if (npfx != null) {
	 Name nm = JavaAst.getQualifiedName(rw.getAST(),npfx);
	 nmi.setExpression(nm);
       }
      SimpleName snm = JavaAst.getSimpleName(rw.getAST(),nmthd);
      nmi.setName(snm);
      rw.replace(n,nmi,null);

      // System.err.println("REPLACE WITH " + nmi);
      // System.err.println("RW: " + rw);

      // if (npfx != null && !npfx.equals(pfx)) {
	 // Name nm = JavaAst.getQualifiedName(rw.getAST(),npfx);
	 // rw.set(n,MethodInvocation.EXPRESSION_PROPERTY,nm,null);
	 // System.err.println("SET EXPR " + nm);
       // }
      // if (!nmthd.equals(omthd)) {
	 // SimpleName nm = (SimpleName) JavaAst.getQualifiedName(rw.getAST(),nmthd);
	 // rw.set(n,MethodInvocation.NAME_PROPERTY,nm,null);
	 // System.err.println("SET NAME " + nm);
       // }
    }

   private void fixFieldAccess(FieldAccess n,ASTRewrite rw) {
      Expression ex = n.getExpression();
      String rep = null;
      String pfx = null;
      String omthd = n.getName().getIdentifier();
      if (ex != null && ex instanceof Name) {
	 Name nm = (Name) ex;
	 pfx = nm.getFullyQualifiedName();
	 String fmthd = pfx + "." + omthd;
	 rep = name_map.get(fmthd);
       }
      if (rep == null) return;
      int idx = rep.lastIndexOf(".");
      String npfx = null;
      String nmthd = null;
      if (idx < 0) {
	 nmthd = rep;
       }
      else {
	 npfx = rep.substring(0,idx);
	 nmthd = rep.substring(idx+1);
       }
      if (npfx != null && !npfx.equals(pfx)) {
	 Name nm = JavaAst.getQualifiedName(rw.getAST(),npfx);
	 rw.set(n,MethodInvocation.EXPRESSION_PROPERTY,nm,null);
       }
      if (!nmthd.equals(omthd)) {
	 SimpleName nm = (SimpleName) JavaAst.getQualifiedName(rw.getAST(),nmthd);
	 rw.set(n,MethodInvocation.NAME_PROPERTY,nm,null);
       }
    }

   private void fixQualifiedType(QualifiedType n,ASTRewrite rw) {
      Type t = n.getQualifier();
      String otnm = n.getName().getIdentifier();
      String opfx = null;
      String rep = null;
      if (t instanceof SimpleType) {
	 SimpleType st = (SimpleType) t;
	 opfx = st.getName().getFullyQualifiedName();
	 String typ = opfx + "." + otnm;
	 rep = name_map.get(typ);
       }
      if (rep == null) return;
      String npfx = null;
      String ntyp = null;
      int idx = rep.lastIndexOf(".");
      if (idx < 0) {
	 ntyp = rep;
       }
      else {
	 npfx = rep.substring(0,idx);
	 ntyp = rep.substring(idx+1);
       }
      if (npfx != null && !npfx.equals(opfx)) {
	 Name qn = JavaAst.getQualifiedName(rw.getAST(),npfx);
	 SimpleType st = rw.getAST().newSimpleType(qn);
	 rw.set(n,QualifiedType.QUALIFIER_PROPERTY,st,null);
       }
      if (ntyp != null && !ntyp.equals(otnm)) {
	 Name tn = JavaAst.getQualifiedName(rw.getAST(),ntyp);
	 rw.set(n,QualifiedType.NAME_PROPERTY,tn,null);
       }
    }

}	// end of inner class NameMapFixup



}	// end of class TransformMapNames




/* end of TransformMapNames.java */

