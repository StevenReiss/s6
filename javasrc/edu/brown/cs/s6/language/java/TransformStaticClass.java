/********************************************************************************/
/*										*/
/*		TransformStaticClass.java					*/
/*										*/
/*	Transformation that handles ensuring classes are static 		*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/language/java/TransformStaticClass.java,v 1.7 2015/09/23 17:54:54 spr Exp $ */


/*********************************************************************************
 *
 * $Log: TransformStaticClass.java,v $
 * Revision 1.7  2015/09/23 17:54:54  spr
 * Version to handle andriod UI applications.
 *
 * Revision 1.6  2014/08/29 15:16:10  spr
 * Updates for suise, testcases.
 *
 * Revision 1.5  2013/09/13 20:33:05  spr
 * Add calls for UI search.
 *
 * Revision 1.4  2012-06-11 14:07:50  spr
 * add framework search; fix bugs
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


import java.util.Iterator;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6Solution;
import edu.brown.cs.s6.common.S6SolutionSet;



public class TransformStaticClass extends TransformJava implements S6Constants, JavaConstants {




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public TransformStaticClass(String name)
{
   super(name);
}



/********************************************************************************/
/*										*/
/*	Method to create Mapper to do actual type changes			*/
/*										*/
/********************************************************************************/

@Override protected boolean applyClassTransform(S6SolutionSet sols,S6Solution sol,
						   TypeDeclaration td,
						   S6Request.ClassSignature csg)
{
   if (Modifier.isStatic(td.getModifiers()) && !Modifier.isAbstract(td.getModifiers()))
      return false;
   
   TreeMapper tm = new ClassStaticMapper(csg,td);

   return addNewSolution(sols,sol,tm);
}



@Override protected boolean checkApplyClassForPackage(S6SolutionSet sols,CompilationUnit cu,
							 S6Request.ClassSignature csg,
							 AbstractTypeDeclaration td)
{
   return false;
}




/********************************************************************************/
/*										*/
/*	Class to handle mappings						*/
/*										*/
/********************************************************************************/

private class ClassStaticMapper extends TreeMapper {

   private S6Request.ClassSignature class_signature;
   private TypeDeclaration change_type;

   ClassStaticMapper(S6Request.ClassSignature csg,TypeDeclaration td) {
      class_signature = csg;
      change_type = td;
    }

   @Override protected String getSpecificsName() {
      return class_signature.getName();
   }

   void rewriteTree(ASTNode orig,ASTRewrite rw) {
      if (orig != change_type) return;

      TypeDeclaration otd = (TypeDeclaration) orig;

      ListRewrite lrw = rw.getListRewrite(otd,otd.getModifiersProperty());
      if (!Modifier.isStatic(otd.getModifiers())) {
	 Modifier mod = otd.getAST().newModifier(Modifier.ModifierKeyword.STATIC_KEYWORD);
	 lrw.insertLast(mod,null);
       }

      for (Iterator<?> it = otd.modifiers().iterator(); it.hasNext(); ) {
	 IExtendedModifier emod = (IExtendedModifier) it.next();
	 if (emod instanceof Modifier) {
	    Modifier mm = (Modifier) emod;
	    if (mm.isAbstract()) {
	       lrw.remove(mm,null);
	       break;
	     }
	  }
       }
    }

}	// end of subtype ClassStaticMapper




}	// end of class TransformStaticClass



/* end of TransformStaticClass.java */

