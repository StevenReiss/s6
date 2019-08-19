/********************************************************************************/
/*                                                                              */
/*              JavaPackageFragment.java                                        */
/*                                                                              */
/*      Implementation of Cose Package fragment for Java                        */
/*                                                                              */
/********************************************************************************/
/*      Copyright 2013 Brown University -- Steven P. Reiss                    */
/*********************************************************************************
 *  Copyright 2013, Brown University, Providence, RI.                            *
 *                                                                               *
 *                        All Rights Reserved                                    *
 *                                                                               *
 *  Permission to use, copy, modify, and distribute this software and its        *
 *  documentation for any purpose other than its incorporation into a            *
 *  commercial product is hereby granted without fee, provided that the          *
 *  above copyright notice appear in all copies and that both that               *
 *  copyright notice and this permission notice appear in supporting             *
 *  documentation, and that the name of Brown University not be used in          *
 *  advertising or publicity pertaining to distribution of the software          *
 *  without specific, written prior permission.                                  *
 *                                                                               *
 *  BROWN UNIVERSITY DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS                *
 *  SOFTWARE, INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND            *
 *  FITNESS FOR ANY PARTICULAR PURPOSE.  IN NO EVENT SHALL BROWN UNIVERSITY      *
 *  BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY          *
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,              *
 *  WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS               *
 *  ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE          *
 *  OF THIS SOFTWARE.                                                            *
 *                                                                               *
 ********************************************************************************/



package edu.brown.cs.s6.language.java;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.PackageDeclaration;

import edu.brown.cs.cose.cosecommon.CoseResult;
import edu.brown.cs.s6.common.S6Language;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6Solution;
import edu.brown.cs.s6.common.S6SolutionSet;

class JavaPackageFragment extends JavaCoseFragment
{


/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

JavaPackageFragment(S6Language lj,S6Request.Search rqst,CoseResult rslt) 
{
   super(lj,rqst,rslt);
} 


/********************************************************************************/
/*                                                                              */
/*      Checking methods                                                        */
/*                                                                              */
/********************************************************************************/

public boolean checkInitial(S6Request.Signature sgn) 
{
   CompilationUnit cu = (CompilationUnit) getAstNode();
   if (cu == null) return false;
   PackageDeclaration pd = cu.getPackage();
   String tnm = sgn.getName();
   if (pd == null) return (tnm == null);
   if (tnm == null) return false;
   String pnm = pd.getName().getFullyQualifiedName();
   if (!pnm.equals(tnm)) return false;
   if (sgn instanceof S6Request.UISignature) {
      S6Request.UISignature usg = (S6Request.UISignature) sgn;
      if (!JavaAst.checkUITypes(cu,usg,this)) return false;
    }
   return true;
}

@Override public boolean checkSignature(S6Request.Signature rsg,S6SignatureType styp) 
{
   S6Request.PackageSignature ps = (S6Request.PackageSignature) rsg;
   CompilationUnit root = (CompilationUnit) getAstNode();
   return JavaAst.checkPackageSignature(root,ps,styp,this);
}



/********************************************************************************/
/*                                                                              */
/*      Dependency handling                                                     */
/*                                                                              */
/********************************************************************************/


@Override public boolean fixDependencies(S6SolutionSet ss,S6Solution sol)
{
   CompilationUnit root = (CompilationUnit) getAstNode();
   JavaDepends jd = new JavaDepends(ss,sol,root);
   if (!jd.findDependencies()) return false;
   import_set = jd.getImportTypes();
   return true;
}



}       // end of class JavaPackageFragment




/* end of JavaPackageFragment.java */

