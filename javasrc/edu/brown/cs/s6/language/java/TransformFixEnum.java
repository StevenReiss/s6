/********************************************************************************/
/*										*/
/*		TransformFixEnum.java						*/
/*										*/
/*	Transformation that handles use of enum as a variable			*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/language/java/TransformFixEnum.java,v 1.4 2015/09/23 17:54:53 spr Exp $ */


/*********************************************************************************
 *
 * $Log: TransformFixEnum.java,v $
 * Revision 1.4  2015/09/23 17:54:53  spr
 * Version to handle andriod UI applications.
 *
 * Revision 1.3  2013/09/13 20:33:04  spr
 * Add calls for UI search.
 *
 * Revision 1.2  2013-05-09 12:26:20  spr
 * Minor changes to start ui fixups.
 *
 * Revision 1.1  2012-07-20 22:15:20  spr
 * New transforms and resolution for UI search
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.language.java;


import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6Solution;
import edu.brown.cs.s6.common.S6SolutionSet;



public class TransformFixEnum extends TransformJava implements S6Constants, JavaConstants {




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public TransformFixEnum(String name)
{
   super(name);
}



/********************************************************************************/
/*										*/
/*	Method to create mapper to do any changes				*/
/*										*/
/********************************************************************************/

@Override protected TreeMapper findPackageMapping(S6SolutionSet ss,CompilationUnit cu,
      S6Request.PackageSignature sg,S6Solution sol)
{
   if (!JavaAst.checkPackageSignature(cu,sg,S6SignatureType.NAME,sol.getFragment())) return null;
   
   return getEnumMapping(cu,"Package",sol);
}

@Override protected boolean checkApplyClassForPackage(S6SolutionSet sols,CompilationUnit cu,
      S6Request.ClassSignature csg,AbstractTypeDeclaration td)
{
   return false;
}


@Override protected TreeMapper findClassMapping(S6SolutionSet sols,
      TypeDeclaration td,S6Request.ClassSignature csg,S6Solution sol)
{
   return getEnumMapping(td,td.getName().getIdentifier(),sol);
}



protected boolean checkApplyMethodForClass(S6SolutionSet ss,S6Solution sol,
      TypeDeclaration td,S6Request.MethodSignature msg,MethodDeclaration md)
{
   return false;
}



@Override protected TreeMapper findMethodMapping(S6SolutionSet ss,MethodDeclaration md,
						    S6Request.MethodSignature ms,
						    S6Solution sol)
{
   return getEnumMapping(md,md.getName().getIdentifier(),sol);
}



private TreeMapper getEnumMapping(ASTNode nd,String nm,S6Solution sol)
{
   EnumFinder ef = new EnumFinder();
   nd.accept(ef);
   Set<ASTNode> fixes = ef.getReturns();
   if (fixes.size() == 0) return null;
   if (sol != null) sol.setFlag(S6SolutionFlag.REMOVE);

   return new EnumNameMapper(nm,fixes);
}




/********************************************************************************/
/*										*/
/*	Class to find enum used as a variable in a method			*/
/*										*/
/********************************************************************************/

private static class EnumFinder extends ASTVisitor {

   private Set<ASTNode> return_nodes;

   EnumFinder() {
      return_nodes = new HashSet<ASTNode>();
    }

   Set<ASTNode> getReturns()			{ return return_nodes; }

   public @Override boolean visit(SimpleName nd) {
      if (nd.getIdentifier().equals("enum")) return_nodes.add(nd);
      return false;
    }

}	// end of inner class EnumFinder





/********************************************************************************/
/*										*/
/*	Class to map the names							*/
/*										*/
/********************************************************************************/

private class EnumNameMapper extends TreeMapper {

   private String	for_whom;
   private Set<ASTNode> fix_names;

   EnumNameMapper(String nm,Set<ASTNode> fixes) {
      for_whom = nm;
      fix_names = fixes;
    }

   @Override protected String getSpecificsName()   { return for_whom; }

   void rewriteTree(ASTNode orig,ASTRewrite rw) {
      if (fix_names.contains(orig)) {
	 SimpleName sn = JavaAst.getSimpleName(rw.getAST(),"s6enum");
	 rw.replace(orig,sn,null);
       }
    }

}	// end of inner class EnumNameMapper




}	// end of class TransformFixEnum




/* end of TransformFixEnum.java */
