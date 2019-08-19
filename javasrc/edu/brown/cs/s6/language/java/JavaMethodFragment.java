/********************************************************************************/
/*                                                                              */
/*              JavaMethodFragment.java                                         */
/*                                                                              */
/*      description of class                                                    */
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

import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import edu.brown.cs.cose.cosecommon.CoseResult;
import edu.brown.cs.s6.common.S6Language;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6Solution;
import edu.brown.cs.s6.common.S6SolutionSet;

class JavaMethodFragment extends JavaCoseFragment
{


/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

JavaMethodFragment(S6Language lang,S6Request.Search sreq,CoseResult rslt)
{
   super(lang,sreq,rslt);
}



/********************************************************************************/
/*                                                                              */
/*      Checking methods                                                        */
/*                                                                              */
/********************************************************************************/

public boolean checkSignature(S6Request.Signature rsg,S6SignatureType styp)
{
   MethodDeclaration md = (MethodDeclaration) getAstNode();
   S6Request.MethodSignature ms = rsg.getMethodSignature();
   if (ms == null) return false;
   return JavaAst.checkMethodSignature(md,ms,styp);
}



/********************************************************************************/
/*                                                                              */
/*      Dependency methods                                                      */
/*                                                                              */
/********************************************************************************/

@Override public boolean fixDependencies(S6SolutionSet ss,S6Solution sol) {
   MethodDeclaration astnode = (MethodDeclaration) getAstNode();
   JavaDepends jd = new JavaDepends(ss,sol,astnode);
   jd.addDeclaration(astnode);
   if (!jd.findDependencies()) return false;
   
   for (BodyDeclaration bd : jd.getDeclarations()) {
      if (bd != astnode) addHelper(bd);
    }
   use_constructor = jd.getUseConstructor();
   import_set = jd.getImportTypes();
   return true;
}


}       // end of class JavaMethodFragment




/* end of JavaMethodFragment.java */

