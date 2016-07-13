/********************************************************************************/
/*										*/
/*		TransformRemoveSpecials.java					*/
/*										*/
/*	Handle removing special calls such as System.exit/JOptionPane.xxx	*/
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
 * $Log: TransformRemoveSpecials.java,v $
 * Revision 1.5  2015/09/23 17:54:54  spr
 * Version to handle andriod UI applications.
 *
 * Revision 1.4  2014/08/29 15:16:09  spr
 * Updates for suise, testcases.
 *
 * Revision 1.3  2013/09/13 20:33:05  spr
 * Add calls for UI search.
 *
 * Revision 1.2  2013-05-09 12:26:21  spr
 * Minor changes to start ui fixups.
 *
 * Revision 1.1  2012-08-13 16:52:47  spr
 * Add code for new transforms.
 *
 * Revision 1.1  2012-07-20 22:15:20  spr
 * New transforms and resolution for UI search
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.language.java;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import edu.brown.cs.ivy.jcomp.JcompSymbol;
import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6Solution;
import edu.brown.cs.s6.common.S6SolutionSet;


public class TransformRemoveSpecials extends TransformJava implements S6Constants, JavaConstants
{


/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public TransformRemoveSpecials(String name)
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
   if (!JavaAst.checkPackageSignature(cu,sg,S6SignatureType.NAME,sol.getFragment())) return null;

   return getRemoveSpecialMapping(cu,"Package",sol);
}


@Override protected boolean checkApplyClassForPackage(S6SolutionSet sols,CompilationUnit cu,
      S6Request.ClassSignature csg,AbstractTypeDeclaration td)
{
   return false;
}


@Override protected TreeMapper findClassMapping(S6SolutionSet sols,
      TypeDeclaration td,S6Request.ClassSignature csg,S6Solution sol)
{
   if (!JavaAst.checkTypeSignature(td,csg,S6SignatureType.FULL,null)) return null;
   
   return getRemoveSpecialMapping(td,td.getName().getIdentifier(),sol);
}



protected boolean checkApplyMethodForClass(S6SolutionSet ss,S6Solution sol,
      TypeDeclaration td,S6Request.MethodSignature msg,MethodDeclaration md)
{
   return false;
}



@Override protected TreeMapper findMethodMapping(S6SolutionSet ss,MethodDeclaration md,
      S6Request.MethodSignature ms,S6Solution sol)
{
   return getRemoveSpecialMapping(md,md.getName().getIdentifier(),null);
}




/********************************************************************************/
/*										*/
/*	Actual mapping finder							*/
/*										*/
/********************************************************************************/

private TreeMapper getRemoveSpecialMapping(ASTNode nd,String nm,S6Solution sol)
{
   FindSpecials tcf = new FindSpecials(nd);

   nd.accept(tcf);
   if (!tcf.hasChanges()) return null;

   SpecialsMapper tcm = new SpecialsMapper(nm,tcf);

   return tcm;
}



/********************************************************************************/
/*										*/
/*	Visitor to set up mapping						*/
/*										*/
/********************************************************************************/

enum SpecialCase {
   REMOVE_STATEMENT,
   REMOVE_UNNESTED_STATEMENT
}

private static Map<String,SpecialCase> special_map;

static {
   special_map = new HashMap<String,SpecialCase>();
   special_map.put("java.lang.System.exit",SpecialCase.REMOVE_UNNESTED_STATEMENT);
   special_map.put("javax.swing.JOptionPane.showConfirmDialog",SpecialCase.REMOVE_STATEMENT);
   special_map.put("javax.swing.JOptionPane.showInputDialog",SpecialCase.REMOVE_STATEMENT);
   special_map.put("javax.swing.JOptionPane.showInternalConfirmDialog",SpecialCase.REMOVE_STATEMENT);
   special_map.put("javax.swing.JOptionPane.showInternalInputDialog",SpecialCase.REMOVE_STATEMENT);
   special_map.put("javax.swing.JOptionPane.showInternalMessageDialog",SpecialCase.REMOVE_STATEMENT);
   special_map.put("javax.swing.JOptionPane.showMessageDialog",SpecialCase.REMOVE_STATEMENT);
   special_map.put("javax.swing.JOptionPane.showOptionDialog",SpecialCase.REMOVE_STATEMENT);
}


private static class FindSpecials extends ASTVisitor {

   private Set<ASTNode> remove_nodes;

   FindSpecials(ASTNode n) {
      remove_nodes = new HashSet<ASTNode>();
    }

   boolean hasChanges() {
      if (!remove_nodes.isEmpty()) return true;
      return false;
    }

   Set<ASTNode> getRemoveNodes()			{ return remove_nodes; }

   @Override public void endVisit(MethodInvocation n) {
      JcompSymbol js = JavaAst.getReference(n);
      if (js == null) return;
      String nm = js.getFullName();
      SpecialCase cs = special_map.get(nm);
      if (cs == null) return;
      ASTNode nd = null;
      switch (cs) {
	 case REMOVE_STATEMENT :
	    nd = getRemoveNode(n,false);
	    break;
	 case REMOVE_UNNESTED_STATEMENT :
	    nd = getRemoveNode(n,true);
	    break;
       }
      if (nd != null) remove_nodes.add(nd);
    }


   private ASTNode getRemoveNode(ASTNode n,boolean nest)
   {
      for (ASTNode p = n; p != null; p = p.getParent()) {
	 if (p instanceof Statement) {
	    if (p.getParent() instanceof Block) return p;
	    else if (nest) return null;
	  }
       }

      return null;
   }

}	// end of inner class FindSpecials




/********************************************************************************/
/*										*/
/*	Mapper to fix up try-catch statements					*/
/*										*/
/********************************************************************************/

private class SpecialsMapper extends TreeMapper {

   private Set<ASTNode> remove_nodes;
   private String map_name;

   SpecialsMapper(String id,FindSpecials fs) {
      remove_nodes = fs.getRemoveNodes();
      map_name = id;
    }

   @Override protected String getSpecificsName()        { return map_name; }

   @Override void rewriteTree(ASTNode orig,ASTRewrite rw) {
      if (orig instanceof Block) {
	 Block b = (Block) orig;
	 List<ASTNode> rem = null;
	 for (Object o : b.statements()) {
	    if (remove_nodes.contains(o)) {
	       if (rem == null) rem = new ArrayList<ASTNode>();
	       rem.add((ASTNode) o);
	     }
	  }
	 if (rem == null) return;
	 ListRewrite lrw = rw.getListRewrite(b,Block.STATEMENTS_PROPERTY);
	 for (ASTNode nd : rem) {
	    lrw.remove(nd,null);
	  }
       }
    }

}	// end of inner class SpecialsMapper




}	// end of class TransformFixTryCatch




/* end of TransformRemoveSpecials.java */
