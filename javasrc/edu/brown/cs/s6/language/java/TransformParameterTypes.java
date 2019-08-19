/********************************************************************************/
/*										*/
/*		TransformParameterTypes.java					*/
/*										*/
/*	Transformation that handles parameter type changes			*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/language/java/TransformParameterTypes.java,v 1.12 2015/09/23 17:54:54 spr Exp $ */


/*********************************************************************************
 *
 * $Log: TransformParameterTypes.java,v $
 * Revision 1.12  2015/09/23 17:54:54  spr
 * Version to handle andriod UI applications.
 *
 * Revision 1.11  2014/08/29 15:16:09  spr
 * Updates for suise, testcases.
 *
 * Revision 1.10  2013/09/13 20:33:05  spr
 * Add calls for UI search.
 *
 * Revision 1.9  2013-05-09 12:26:21  spr
 * Minor changes to start ui fixups.
 *
 * Revision 1.8  2012-06-11 14:07:49  spr
 * add framework search; fix bugs
 *
 * Revision 1.7  2009-09-18 01:41:36  spr
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
 * Revision 1.3  2008-07-18 22:27:09  spr
 * Handle remove compilation calls; update transforms to include code to use ASTrewrite.
 *
 * Revision 1.2  2008-07-17 13:46:46  spr
 * Bug fixes and speed ups.
 *
 * Revision 1.1  2008-06-12 17:47:52  spr
 * Next version of S6.
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.language.java;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import edu.brown.cs.ivy.jcomp.JcompSymbol;
import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.ivy.jcomp.JcompTyper;
import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6Solution;
import edu.brown.cs.s6.common.S6SolutionSet;



public class TransformParameterTypes extends TransformJava implements S6Constants, JavaConstants {




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public TransformParameterTypes(String name)
{
   super(name);
}




/********************************************************************************/
/*										*/
/*	Method to create mapper to do actual parameter type changes		*/
/*										*/
/********************************************************************************/

@Override protected TreeMapper findMethodMapping(S6SolutionSet ss,MethodDeclaration md,
						    S6Request.MethodSignature ms,
						    S6Solution sol)
{
   if (JavaAst.checkMethodSignature(md,ms,S6SignatureType.PARAMETERS)) return null;

   List<String> mstyp = ms.getParameterTypeNames();
   List<?> mdtyp = md.parameters();

   if (mstyp.size() == 0) return null;
   if (mstyp.size() != mdtyp.size()) return null;

   JcompTyper jt = JavaAst.getTyper(md);
   if (jt == null) return null;

   Map<SingleVariableDeclaration,JcompType> changes = new HashMap<SingleVariableDeclaration,JcompType>();

   for (int i = 0; i < mstyp.size(); ++i) {
      JcompType t0 = jt.findSystemType(mstyp.get(i));
      SingleVariableDeclaration svd = (SingleVariableDeclaration) mdtyp.get(i);
      JcompSymbol js = JavaAst.getDefinition(svd);
      if (js == null) {
	 // System.err.println("EMPTY DEFINITION FOR " + svd);
	 return null;
       }
      JcompType t1 = js.getType();
      if (t0 == null || t1 == null) return null;
      if (t0 == t1) continue;
      if (t1.getName().equals("java.lang.Object") && ss.getSearchType() != S6SearchType.METHOD) continue;
      if (checkComformable(t1,t0,ss,md,svd)) {
	 changes.put(svd,t0);
       }
      else return null;
    }

   if (changes.size() == 0) return null;

   return new ParameterTypeMapper(ms.getName(),md,changes);
}



/********************************************************************************/
/*										*/
/*	Class to handle return type mappings					*/
/*										*/
/********************************************************************************/

private class ParameterTypeMapper extends TreeMapper {

   private String for_whom;
   private Map<SingleVariableDeclaration,JcompType> change_map;

   ParameterTypeMapper(String whom,MethodDeclaration md,
			       Map<SingleVariableDeclaration,JcompType> ch) {
      for_whom = whom;
      change_map = ch;
    }

   @Override protected String getSpecificsName()   { return for_whom; }

   void rewriteTree(ASTNode orig,ASTRewrite rw) {
      JcompType jt = change_map.get(orig);
      if (jt != null) {
	 Type t = jt.createAstNode(orig.getAST());
	 rw.set(orig,SingleVariableDeclaration.TYPE_PROPERTY,t,null);
       }
    }

}	// end of subclass ParameterTypeMapper




}	// end of class TransformParameterTypes





/* end of TransformParameterTypes.java */
