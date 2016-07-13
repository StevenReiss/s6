/********************************************************************************/
/*										*/
/*		SviwebMethodSearch.java 					*/
/*										*/
/*	Search area and actions for searching for methods			*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/sviweb/client/SviwebMethodSearch.java,v 1.9 2015/09/23 17:58:16 spr Exp $ */


/*********************************************************************************
 *
 * $Log: SviwebMethodSearch.java,v $
 * Revision 1.9  2015/09/23 17:58:16  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.8  2012-06-11 14:08:22  spr
 * Add framework search; fix bugs
 *
 * Revision 1.7  2009-09-18 01:42:37  spr
 * Updates for new gwt and support user feedback and full class.
 *
 * Revision 1.6  2009-05-12 22:29:43  spr
 * Update front end of user context.
 *
 * Revision 1.5  2008-11-12 13:52:27  spr
 * Performance and bug updates.
 *
 * Revision 1.4  2008-08-28 00:33:03  spr
 * Next version of S6.	Lots of bug fixes, some new functionality.
 *
 * Revision 1.3  2008-07-17 13:47:46  spr
 * User interface fixups, mainly for class search.
 *
 * Revision 1.2  2008-06-12 17:47:57  spr
 * Next version of S6.
 *
 * Revision 1.1.1.1  2008-06-03 12:59:23  spr
 * Initial version of S6
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.sviweb.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.XMLParser;


class SviwebMethodSearch extends SviwebSearch implements SviwebConstants {


/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private Label		decl_label;
private SviwebDeclaration method_decl;
private List<SviwebDeclaration> method_list;
private SviwebMethodTestArea test_area;
private int		tab_index;
private SviwebThreadingArea thread_area;


private static final int	DECL_ROW = 0;
private static final int	TEST_ROW = 1;
private static final int	CONTEXT_ROW = 2;
private static final int	SECURITY_ROW = 3;
private static final int	CONTRACT_ROW = 4;
private static final int	THREADING_ROW = 5;
private static final int	ENVIRONMENT_ROW = 6;
private static final int	BOTTOM_ROW = 7;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

SviwebMethodSearch(SviwebMain sm,StatusCallback cb)
{
   super(sm,cb);

   tab_index = METHOD_TAB_START;

   decl_label = new Label("Declaration:\u00a0");
   decl_label.setStyleName("decllabelok");
   search_grid.setWidget(DECL_ROW,0,decl_label);
   method_decl = new SviwebDeclaration(svi_web,new DeclStatusChange(),null);
   method_list = new ArrayList<SviwebDeclaration>();
   method_list.add(method_decl);
   tab_index = method_decl.setTabIndex(tab_index);
   search_grid.setWidget(DECL_ROW,1,method_decl.getWidget());

   search_grid.setText(TEST_ROW,0,"Tests:");

   test_area = new SviwebMethodTestArea(svi_web,tab_index,method_decl,new TestStatusChange());
   ++tab_index;
   search_grid.setWidget(TEST_ROW,1,test_area.getWidget());

   setupContext(tab_index++);
   setupSecurity(tab_index++);
   setupContracts(tab_index++);

   Label l0 = new Label("Threading:\u00a0");
   l0.setStyleName("threadinglabel");
   search_grid.setWidget(THREADING_ROW,0,l0);

   thread_area = new SviwebThreadingArea(svi_web,null);
   search_grid.setWidget(THREADING_ROW,1,thread_area.getWidget());
   showThreading(false);

   setupBottomPanel(tab_index++);
}




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

protected String getSearchType()	{ return "Method"; }
protected int getBottomRow()		{ return BOTTOM_ROW; }
protected int getSecurityRow()		{ return SECURITY_ROW; }
protected int getThreadingRow() 	{ return THREADING_ROW; }
protected int getContractRow()		{ return CONTRACT_ROW; }
protected int getContextRow()		{ return CONTEXT_ROW; }
protected int getEnvironmentRow()	{ return ENVIRONMENT_ROW; }


List<SviwebDeclaration> getMethods()	{ return method_list; }

void addSignature(Element root) 	{ test_area.addSignature(root); }



/********************************************************************************/
/*										*/
/*	Status methods								*/
/*										*/
/********************************************************************************/

Status getStatus()
{
   Status sts = method_decl.getStatus();
   if (sts != Status.OK) return sts;

   sts = test_area.validate();
   if (sts != Status.OK) return sts;

   sts = security_area.validate();
   if (sts != Status.OK) return sts;

   sts = contract_area.validate();
   if (sts != Status.OK) return sts;

   sts = context_area.validate();
   if (sts != Status.OK) return sts;

   return Status.OK;
}



/********************************************************************************/
/*										*/
/*	Methods to build a search request					*/
/*										*/
/********************************************************************************/

Document getSearchRequest()
{
   Document doc = XMLParser.createDocument();
   Element root = SviwebXml.addChild(doc,"SEARCH");
   root.setAttribute("WHAT","METHOD");
   addCommonProperties(root);
   addSignature(root);
   SviwebXml.addChild(root,test_area.getTestDescriptions());
   security_area.generateOutput(root);
   contract_area.generateOutput(root);
   context_area.generateOutput(root);
   thread_area.addThreading(root);

   return doc;
}




/********************************************************************************/
/*										*/
/*	Handle description and test changes					*/
/*										*/
/********************************************************************************/

private class DeclStatusChange implements StatusCallback {

   public void setStatus(Status sts) {
      if (sts != Status.OK) test_area.invalidate();
      if (sts == Status.BAD) {
	 decl_label.setStyleName("decllabelerror");
       }
      else if (sts == Status.OK) {
	 decl_label.setStyleName("decllabelok");
	 test_area.reset();
	 contract_area.reset();
       }
      else {
	 decl_label.setStyleName("decllabelok");
       }
    }

}	// end of subclass DeclStatusChange



}	// end of class SviwebMethodSearch



/* end of SviwebMethodSearch.java */








