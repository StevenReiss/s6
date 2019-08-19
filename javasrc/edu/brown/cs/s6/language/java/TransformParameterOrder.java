/********************************************************************************/
/*										*/
/*		TransformParameterOrder.java					*/
/*										*/
/*	Transformation that handles parameter order changes			*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/language/java/TransformParameterOrder.java,v 1.11 2015/09/23 17:54:54 spr Exp $ */


/*********************************************************************************
 *
 * $Log: TransformParameterOrder.java,v $
 * Revision 1.11  2015/09/23 17:54:54  spr
 * Version to handle andriod UI applications.
 *
 * Revision 1.10  2014/08/29 15:16:09  spr
 * Updates for suise, testcases.
 *
 * Revision 1.9  2013/09/13 20:33:05  spr
 * Add calls for UI search.
 *
 * Revision 1.8  2013-05-09 12:26:21  spr
 * Minor changes to start ui fixups.
 *
 * Revision 1.7  2012-06-11 14:07:49  spr
 * add framework search; fix bugs
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


import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import edu.brown.cs.ivy.jcomp.JcompSymbol;
import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.ivy.jcomp.JcompTyper;
import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6Solution;
import edu.brown.cs.s6.common.S6SolutionSet;



public class TransformParameterOrder extends TransformJava implements S6Constants, JavaConstants {




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public TransformParameterOrder(String name)
{
   super(name);
}




/********************************************************************************/
/*										*/
/*	Method to create mapper to do actual return type changes		*/
/*										*/
/********************************************************************************/

@Override protected Collection<TreeMapper> findMethodMappings(S6SolutionSet ss,MethodDeclaration md,
								  S6Request.MethodSignature ms,
								  S6Solution sol)
{
   if (JavaAst.checkMethodSignature(md,ms,S6SignatureType.PARAMETERS)) return null;
   if (!JavaAst.checkMethodSignature(md,ms,S6SignatureType.RETURN_EXCEPTIONS)) return null;

   List<String> mstyp = ms.getParameterTypeNames();
   List<?> mdtyp = md.parameters();

   if (mstyp.size() != mdtyp.size()) return null;
   if (mstyp.size() < 2) return null;

   JcompTyper jt = JavaAst.getTyper(md);
   if (jt == null) return null;

   List<List<SingleVariableDeclaration>> orders = new ArrayList<List<SingleVariableDeclaration>>();

   findOrders(0,jt,mstyp,mdtyp,orders,null);

   if (orders.size() == 0) return null;

   Collection<TreeMapper> solns = new ArrayList<TreeMapper>();

   for (List<SingleVariableDeclaration> order : orders) {
      ParameterOrderMapper pm = new ParameterOrderMapper(ms.getName(),md,order);
      solns.add(pm);
    }

   return solns;
}



private void findOrders(int idx,JcompTyper jt,List<String> mstyp,List<?> mdtyp,
			   List<List<SingleVariableDeclaration>> orders,
			   List<SingleVariableDeclaration> curorder)
{
   JcompType t0 = jt.findSystemType(mstyp.get(idx));
   if (t0 == null) return;

   for (Iterator<?> it = mdtyp.iterator(); it.hasNext(); ) {
      SingleVariableDeclaration svd = (SingleVariableDeclaration) it.next();
      if (curorder != null && curorder.contains(svd)) continue;
      JcompSymbol js = JavaAst.getDefinition(svd);
      if (js == null) return;
      JcompType t1 = js.getType();
      if (t1 == null) return;
      if (!t0.isCompatibleWith(t1)) continue;
      if (curorder == null) curorder = new ArrayList<SingleVariableDeclaration>();
      curorder.add(svd);
      if (idx+1 == mstyp.size()) {
	 if (!curorder.equals(mdtyp))
	    orders.add(new ArrayList<SingleVariableDeclaration>(curorder));
       }
      else {
	 findOrders(idx+1,jt,mstyp,mdtyp,orders,curorder);
       }
      curorder.remove(idx);
    }
}




/********************************************************************************/
/*										*/
/*	Class to handle return type mappings					*/
/*										*/
/********************************************************************************/

private class ParameterOrderMapper extends TreeMapper {

   private String for_whom;
   private MethodDeclaration change_method;
   private List<ASTNode> use_order;

   ParameterOrderMapper(String whom,MethodDeclaration md,
			   List<SingleVariableDeclaration> order) {
      for_whom = whom;
      change_method = md;
      use_order = new ArrayList<ASTNode>(order);
    }

   @Override protected String getSpecificsName()   { return for_whom; }

   void rewriteTree(ASTNode orig,ASTRewrite rw) {
      if (orig == change_method) {
	 ListRewrite lrw = rw.getListRewrite(orig,MethodDeclaration.PARAMETERS_PROPERTY);
	 List<?> olrw = lrw.getOriginalList();
	 int i = 0;
	 for (ListIterator<ASTNode> it = use_order.listIterator(); it.hasNext(); ) {
	    ASTNode svd = it.next();
	    SingleVariableDeclaration osvd = (SingleVariableDeclaration) olrw.get(i);
	    if (osvd != svd) it.set(lrw.createCopyTarget(svd,svd));
	    ++i;
	  }
	 i = 0;
	 for (ASTNode svd : use_order) {
	    SingleVariableDeclaration osvd = (SingleVariableDeclaration) olrw.get(i);
	    if (osvd != svd) lrw.replace(osvd,svd,null);
	    ++i;
	  }
	 while (i < olrw.size()) {
	    SingleVariableDeclaration osvd = (SingleVariableDeclaration) olrw.get(i);
	    lrw.remove(osvd,null);
	    ++i;
	  }
       }
    }

}	// end of subclass ParameterTypeMapper




}	// end of class TransformParameterOrder





/* end of TransformParameterOrder.java */
