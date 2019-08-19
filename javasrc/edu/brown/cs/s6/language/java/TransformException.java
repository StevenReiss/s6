/********************************************************************************/
/*										*/
/*		TransformException.java 					*/
/*										*/
/*	Transformation that handles exception type changes			*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/language/java/TransformException.java,v 1.13 2015/09/23 17:54:53 spr Exp $ */


/*********************************************************************************
 *
 * $Log: TransformException.java,v $
 * Revision 1.13  2015/09/23 17:54:53  spr
 * Version to handle andriod UI applications.
 *
 * Revision 1.12  2014/08/29 15:16:08  spr
 * Updates for suise, testcases.
 *
 * Revision 1.11  2013-05-09 12:26:20  spr
 * Minor changes to start ui fixups.
 *
 * Revision 1.10  2012-07-20 22:15:20  spr
 * New transforms and resolution for UI search
 *
 * Revision 1.9  2012-06-11 14:07:49  spr
 * add framework search; fix bugs
 *
 * Revision 1.8  2009-09-18 01:41:36  spr
 * Handle user testing.
 *
 * Revision 1.7  2009-05-12 22:28:48  spr
 * Fix ups to make user context work.
 *
 * Revision 1.6  2008-11-12 13:52:14  spr
 * Performance and bug updates.
 *
 * Revision 1.5  2008-08-28 00:32:56  spr
 * Next version of S6.	Lots of bug fixes, some new functionality.
 *
 * Revision 1.4  2008-07-18 22:27:09  spr
 * Handle remove compilation calls; update transforms to include code to use ASTrewrite.
 *
 * Revision 1.3  2008-07-17 13:46:45  spr
 * Bug fixes and speed ups.
 *
 * Revision 1.2  2008-06-12 17:47:52  spr
 * Next version of S6.
 *
 * Revision 1.1.1.1  2008-06-03 12:59:23  spr
 * Initial version of S6
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.language.java;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import edu.brown.cs.ivy.jcomp.JcompSymbol;
import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.ivy.jcomp.JcompTyper;
import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6Solution;
import edu.brown.cs.s6.common.S6SolutionSet;



public class TransformException extends TransformJava implements S6Constants, JavaConstants {




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public TransformException(String name)
{
   super(name);
}




/********************************************************************************/
/*										*/
/*	Method to create mapper to remove thrown exceptions in declaration	*/
/*										*/
/********************************************************************************/

@Override protected TreeMapper findMethodMapping(S6SolutionSet ss,MethodDeclaration md,
						    S6Request.MethodSignature ms,
                                                    S6Solution sol)
{
   if (JavaAst.checkMethodSignature(md,ms,S6SignatureType.EXCEPTIONS)) return null;
   if (!JavaAst.checkMethodSignature(md,ms,S6SignatureType.PARAMETERS)) return null;
   if (!JavaAst.checkMethodSignature(md,ms,S6SignatureType.RETURN)) return null;
   
   JcompTyper typr = JavaAst.getTyper(md);
   if (typr == null) return null;

   ExceptionFinder ef = new ExceptionFinder();
   md.accept(ef);

   for (JcompType jt : ef.getExceptions()) {
      boolean fnd = false;
      for (String enm : ms.getExceptionTypeNames()) {
	 JcompType t1 = typr.findSystemType(enm);
	 if (jt == t1) fnd = true;
         if (t1.isCompatibleWith(jt)) fnd = true;
       }
      if (!fnd) return null;
    }

   if (md.thrownExceptionTypes().size() == ms.getExceptionTypeNames().size()) {
      // is this code necessary and will it work wrt context-based exceptions
      boolean ok = true;
      for (String enm : ms.getExceptionTypeNames()) {
	 JcompType t1 = typr.findSystemType(enm);
	 if (t1 == null) return null;
	 boolean fnd = false;
	 for (Iterator<?> it = md.thrownExceptionTypes().iterator(); it.hasNext(); ) {
	    Type n = (Type) it.next();
	    JcompType t2 = JavaAst.getJavaType(n);
	    if (t2 == t1) fnd = true;
	  }
	 if (!fnd) ok = false;
       }
      if (ok) return null;
    }

   ExceptionMapper exmap = new ExceptionMapper(md);
   for (String enm : ms.getExceptionTypeNames()) {
      JcompType t0 = typr.findSystemType(enm);
      if (t0 == null) return null;
      exmap.addExceptionType(md,t0);
    }
   for (MethodDeclaration mdc : ef.getMethodsCalled()) {
      if (mdc == md) continue;
      exmap.addExceptionType(mdc,null);
      for (Object o : mdc.thrownExceptionTypes()) {
         Type n = (Type) o;
         JcompType t2 = JavaAst.getJavaType(n);
         boolean fnd = false;
         for (String enm : ms.getExceptionTypeNames()) {
            JcompType t1 = typr.findSystemType(enm);
            if (t2 == t1) fnd = true;
          }
         if (fnd) exmap.addExceptionType(md,t2);
       }
    }

   return exmap;
}



/********************************************************************************/
/*										*/
/*	Class to find exceptions thrown 					*/
/*										*/
/********************************************************************************/

private class ExceptionFinder extends ASTVisitor
{
   private Set<JcompType> found_exceptions;
   private Set<MethodDeclaration> found_calls;

   ExceptionFinder() {
      found_exceptions = new HashSet<JcompType>();
      found_calls = new HashSet<MethodDeclaration>();
    }

   Iterable<JcompType> getExceptions()		        { return found_exceptions; }
   Iterable<MethodDeclaration> getMethodsCalled()       { return found_calls; }

   public @Override void endVisit(ThrowStatement n) {
      JcompType jt = JavaAst.getExprType(n.getExpression());
      if (jt != null) found_exceptions.add(jt);
    }

   public @Override void endVisit(MethodInvocation n) {
      JcompSymbol js = JavaAst.getReference(n);
      if (js != null) {
         for (JcompType jt : js.getExceptions()) {
            found_exceptions.add(jt);
          }
         if (js.getDefinitionNode() != null) {
            ASTNode an = js.getDefinitionNode();
            if (an.getNodeType() == ASTNode.METHOD_DECLARATION) {
               MethodDeclaration md = (MethodDeclaration) an;
               if (md.thrownExceptionTypes().size() > 0) found_calls.add(md);
             }
          }
       }
    }
   
   @Override public void endVisit(ClassInstanceCreation n) {
      JcompSymbol js = JavaAst.getReference(n);
      if (js != null) {
         for (JcompType jt : js.getExceptions()) {
            found_exceptions.add(jt);
            if (js.getDefinitionNode() != null) {
               ASTNode an = js.getDefinitionNode();
               if (an.getNodeType() == ASTNode.METHOD_DECLARATION) {
                  MethodDeclaration md = (MethodDeclaration) an;
                  if (md.thrownExceptionTypes().size() > 0) found_calls.add(md);
                }
             }
          }
       }
    }
   
}	// end of subclass ExceptionFinder





/********************************************************************************/
/*										*/
/*	Class to handle return type mappings					*/
/*										*/
/********************************************************************************/

private class ExceptionMapper extends TreeMapper {
   
   private Map<MethodDeclaration,List<JcompType>> change_map;
   private MethodDeclaration base_method;

   ExceptionMapper(MethodDeclaration md) {
      base_method = md;
      change_map = new HashMap<MethodDeclaration,List<JcompType>>();
      addExceptionType(md,null);
    }

   @Override protected String getSpecificsName() {
      return base_method.getName().getIdentifier();
   }

   void addExceptionType(MethodDeclaration md,JcompType t0) {
      List<JcompType> ljt = change_map.get(md);
      if (ljt == null) {
         ljt = new ArrayList<JcompType>();
         change_map.put(md,ljt);
       }
       if (t0 != null) ljt.add(t0);
    }

   void rewriteTree(ASTNode orig,ASTRewrite rw) {
      List<JcompType> ljt = change_map.get(orig);
      if (ljt == null) return;
   
      MethodDeclaration nmd = (MethodDeclaration) orig;
      ListRewrite lrw = rw.getListRewrite(orig,MethodDeclaration.THROWN_EXCEPTION_TYPES_PROPERTY);
      List<?> exl = nmd.thrownExceptionTypes();
      for (Iterator<?> it = exl.iterator(); it.hasNext(); ) {
         ASTNode n = (ASTNode) it.next();
         lrw.remove(n,null);
       }
      for (JcompType jt : ljt) {
         Name nm = JavaAst.getQualifiedName(orig.getAST(),jt.getName());
         lrw.insertLast(nm,null);
       }
    }

}	// end of subclass ExceptionMapper




}	// end of class TransformException





/* end of TransformException.java */

