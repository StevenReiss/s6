/********************************************************************************/
/*										*/
/*		TransformPrivate.java						*/
/*										*/
/*	Transformation that handles making a method non-private 		*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/language/java/TransformPrivate.java,v 1.4 2015/09/23 17:54:54 spr Exp $ */


/*********************************************************************************
 *
 * $Log: TransformPrivate.java,v $
 * Revision 1.4  2015/09/23 17:54:54  spr
 * Version to handle andriod UI applications.
 *
 * Revision 1.3  2014/08/29 15:16:09  spr
 * Updates for suise, testcases.
 *
 * Revision 1.2  2013/09/13 20:33:05  spr
 * Add calls for UI search.
 *
 * Revision 1.1  2013-05-09 12:26:21  spr
 * Minor changes to start ui fixups.
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.language.java;


import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6Solution;
import edu.brown.cs.s6.common.S6SolutionSet;


public class TransformPrivate extends TransformJava implements S6Constants, JavaConstants {




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public TransformPrivate(String name)
{
   super(name);
}




/********************************************************************************/
/*										*/
/*	Method to create mapper to do actual return type changes		*/
/*										*/
/********************************************************************************/

@Override protected TreeMapper findMethodMapping(S6SolutionSet ss,MethodDeclaration md,
						    S6Request.MethodSignature ms,
						    S6Solution sol)
{
   if (ss.getRequest().getUserContext() == null) return null;
   if (!JavaAst.checkMethodSignature(md,ms,S6SignatureType.ALL)) return null;
   if (!Modifier.isPrivate(md.getModifiers()) && !Modifier.isProtected(md.getModifiers())) return null;
   if (md.isConstructor()) return null;
   if (ss.getSearchType() != S6SearchType.METHOD) return null;

   sol.setFlag(S6SolutionFlag.REMOVE);

   return new PrivateMapper(ms.getName(),md);
}



/********************************************************************************/
/*										*/
/*	Class to handle removing private					*/
/*										*/
/********************************************************************************/

private class PrivateMapper extends TreeMapper {

   private String for_whom;
   private MethodDeclaration change_method;

   PrivateMapper(String whom,MethodDeclaration md) {
      for_whom = whom;
      change_method = md;
    }

   @Override protected String getSpecificsName()   { return for_whom; }

   void rewriteTree(ASTNode orig,ASTRewrite rw) {
      if (orig != change_method) return;

      MethodDeclaration md = (MethodDeclaration) orig;
      ListRewrite lrw = rw.getListRewrite(md,MethodDeclaration.MODIFIERS2_PROPERTY);
      for (Object o : md.modifiers()) {
	 IExtendedModifier iem = (IExtendedModifier) o;
	 if (iem.isAnnotation()) {
	    lrw.remove((ASTNode) o,null);
	  }
	 else if (iem.isModifier()) {
	    Modifier mod = (Modifier) iem;
	    if (mod.isPrivate() || mod.isProtected()) {
	       lrw.remove(mod,null);
	     }
	  }
       }
    }

}	// end of subclass StaticMapper




}	// end of class TransformStatic





/* end of TransformStatic.java */
