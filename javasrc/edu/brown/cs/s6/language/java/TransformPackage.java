/********************************************************************************/
/*										*/
/*		TransformPackage.java						*/
/*										*/
/*	Transformation that fixes package statements from classes		*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/language/java/TransformPackage.java,v
1.1 2009-05-12 22:33:25 spr Exp $ */


/*********************************************************************************
 *
 * $Log: TransformPackage.java,v $
 * Revision 1.6  2015/09/23 17:54:54  spr
 * Version to handle andriod UI applications.
 *
 * Revision 1.5  2014/08/29 15:16:09  spr
 * Updates for suise, testcases.
 *
 * Revision 1.4  2013/09/13 20:33:05  spr
 * Add calls for UI search.
 *
 * Revision 1.3  2012-07-20 22:15:21  spr
 * New transforms and resolution for UI search
 *
 * Revision 1.2  2012-06-11 14:07:49  spr
 * add framework search; fix bugs
 *
 * Revision 1.1  2009-05-12 22:33:25  spr
 * Add new transforms use for context mappings.
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.language.java;


import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6Solution;
import edu.brown.cs.s6.common.S6SolutionSet;




public class TransformPackage extends TransformJava implements S6Constants, JavaConstants {




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public TransformPackage(String name)
{
   super(name);
}



/********************************************************************************/
/*										*/
/*	Method to create mapper to change package for package search		*/
/*										*/
/********************************************************************************/

@Override protected TreeMapper findPackageMapping(S6SolutionSet ss,CompilationUnit cu,S6Request.PackageSignature sg,S6Solution sol)
{
   TreeMapper tm = null;
   String pnm = sg.getName();
   String onm = null;
   if (cu.getPackage() != null)
      onm = cu.getPackage().getName().getFullyQualifiedName();

   if (pnm == null) {
      if (onm == null) return null;
      tm = new RemovePackageMapper("package",cu);
    }
   else if (!pnm.equals(onm)) {
      tm = new ChangePackageMapper(pnm,onm,cu);
    }

   return tm;
}



@Override protected boolean checkApplyClassForPackage(S6SolutionSet ss,CompilationUnit cu,
							  S6Request.ClassSignature csg,
							  AbstractTypeDeclaration td)
{
   return false;
}



/********************************************************************************/
/*										*/
/*	Method to create Mapper to do remove package statement for classes	*/
/*										*/
/********************************************************************************/

@Override protected boolean applyClassTransform(S6SolutionSet sols,S6Solution sol,
						   TypeDeclaration td,
						   S6Request.ClassSignature csg)
{
   CompilationUnit cu = (CompilationUnit) td.getRoot();
   if (cu.getPackage() == null) return false;

   TreeMapper tm = new RemovePackageMapper(csg.getName(),cu);

   boolean chng = false;

   chng |= addNewSolution(sols,sol,tm);

   return chng;
}




/********************************************************************************/
/*										*/
/*	Class to handle removing package declaration mapping			*/
/*										*/
/********************************************************************************/

private class RemovePackageMapper extends TreeMapper {

   private String map_name;
   private CompilationUnit base_unit;

   RemovePackageMapper(String nm,CompilationUnit cu) {
      map_name = nm;
      base_unit = cu;
    }

   @Override protected String getSpecificsName()   { return map_name; }

   void rewriteTree(ASTNode orig,ASTRewrite rw) {
      if (orig != base_unit) return;

      CompilationUnit ocu = (CompilationUnit) orig;

      PackageDeclaration opd = ocu.getPackage();
      if (opd == null) return;

      Name pnm = (Name) rw.createCopyTarget(opd.getName());
      ImportDeclaration nid = ocu.getAST().newImportDeclaration();
      nid.setName(pnm);
      nid.setOnDemand(true);
      nid.setStatic(false);

      rw.set(ocu,CompilationUnit.PACKAGE_PROPERTY,null,null);
      ListRewrite lrw = rw.getListRewrite(ocu,CompilationUnit.IMPORTS_PROPERTY);
      lrw.insertFirst(nid,null);
    }

}	// end of subtype RemovePackageMapper




/********************************************************************************/
/*										*/
/*	Class to handle changing package declaration mapping			*/
/*										*/
/********************************************************************************/

private class ChangePackageMapper extends TreeMapper {

   private String pkg_name;
   private String orig_name;
   private CompilationUnit base_unit;

   ChangePackageMapper(String nm,String onm,CompilationUnit cu) {
      pkg_name = nm;
      orig_name = onm;
      base_unit = cu;
    }

   @Override protected String getSpecificsName()   { return pkg_name; }

   void rewriteTree(ASTNode orig,ASTRewrite rw) {
      if (orig == base_unit) {
	 CompilationUnit ocu = (CompilationUnit) orig;
	
	 PackageDeclaration npd = ocu.getAST().newPackageDeclaration();
	 Name nnm = JavaAst.getQualifiedName(ocu.getAST(),pkg_name);
	 npd.setName(nnm);
	
	 rw.set(ocu,CompilationUnit.PACKAGE_PROPERTY,npd,null);
       }
      else if (orig_name != null && orig instanceof ImportDeclaration) {
	 ImportDeclaration id = (ImportDeclaration) orig;
	 int oln = orig_name.length();
	 String nm = id.getName().getFullyQualifiedName();
	 if (!id.isOnDemand() && nm.startsWith(orig_name) &&
	       nm.charAt(oln) == '.') {
	    String nnm = pkg_name + nm.substring(oln);
	    Name qn = JavaAst.getQualifiedName(rw.getAST(),nnm);
	    rw.replace(id.getName(),qn,null);
	  }
       }
    }

}	// end of subtype RemovePackageMapper




}	// end of class TransformPackage



/* end of TransformPackage.java */





