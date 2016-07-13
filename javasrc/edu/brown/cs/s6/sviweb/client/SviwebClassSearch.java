/********************************************************************************/
/*										*/
/*		SviwebClassSearch.java						*/
/*										*/
/*	Search area and actions for searching for classes			*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/sviweb/client/SviwebClassSearch.java,v 1.10 2015/09/23 17:58:16 spr Exp $ */


/*********************************************************************************
 *
 * $Log: SviwebClassSearch.java,v $
 * Revision 1.10  2015/09/23 17:58:16  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.9  2013/09/13 20:33:15  spr
 * Add calls for UI search.
 *
 * Revision 1.8  2012-06-11 14:08:22  spr
 * Add framework search; fix bugs
 *
 * Revision 1.7  2009-09-18 01:42:37  spr
 * Updates for new gwt and support user feedback and full class.
 *
 * Revision 1.6  2009-05-12 22:29:42  spr
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

import java.util.List;
import java.util.Vector;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.XMLParser;



class SviwebClassSearch extends SviwebSearch implements SviwebConstants, ChangeHandler {


/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private SviwebClassTestArea test_area;
private int		tab_index;
private boolean 	is_full;

private Label		name_label;
private TextBox 	name_box;
private boolean 	name_ok;
private Label		super_label;
private SviwebClassNameArea super_box;
private Label		iface_label;
private SviwebClassNameArea iface_box;

private Grid		method_table;
private String		class_name;
private String		superclass_name;
private String		iface_names;

private Vector<SviwebDeclaration> class_methods;


private static final int NAME_ROW = 0;
private static final int SUPER_ROW = 1;
private static final int IFACE_ROW = 2;
private static final int METHOD_ROW = 3;
private static final int TEST_ROW = 4;
private static final int SECURITY_ROW = 5;
private static final int ENVIRONMENT_ROW = 6;
private static final int BOTTOM_ROW = 7;

private final String CLASS_NAME_REGEX = "^\\w+$";



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

SviwebClassSearch(SviwebMain sm,StatusCallback cb)
{
   super(sm,cb);

   tab_index = CLASS_TAB_START;
   name_ok = false;
   class_name = null;
   class_methods = new Vector<SviwebDeclaration>();
   is_full = false;

   name_label = new Label("Class\u00a0name:\u00a0");
   name_label.setStyleName("classdecllabel");
   search_grid.setWidget(NAME_ROW,0,name_label);

   name_box = new TextBox();
   name_box.setMaxLength(64);
   name_box.setVisibleLength(32);
   name_box.addChangeHandler(this);
   name_box.setStyleName("classdecl");
   name_box.setTabIndex(tab_index++);
   search_grid.setWidget(NAME_ROW,1,name_box);

   super_label = new Label("Superclass\u00a0name:\u00a0");
   super_label.setStyleName("classdecllabel");
   search_grid.setWidget(SUPER_ROW,0,super_label);
   super_box = new SviwebClassNameArea(svi_web,false,true,tab_index++,cb);
   search_grid.setWidget(SUPER_ROW,1,super_box.getWidget());

   iface_label = new Label("Interface\u00a0names:\u00a0");
   iface_label.setStyleName("classdecllabel");
   search_grid.setWidget(IFACE_ROW,0,iface_label);
   iface_box = new SviwebClassNameArea(svi_web,true,true,tab_index++,cb);
   search_grid.setWidget(IFACE_ROW,1,iface_box.getWidget());

   Label lbl = new Label("Methods:\u00a0");
   lbl.setStyleName("classmethods");
   search_grid.setWidget(METHOD_ROW,0,lbl);
   method_table = new Grid(0,1);
   method_table.setStyleName("classmethodtable");
   DOM.setElementAttribute(method_table.getElement(),"frame","box");
   method_table.setCellPadding(2);
   tab_index++;
   search_grid.setWidget(METHOD_ROW,1,method_table);

   search_grid.setText(TEST_ROW,0,"Tests:");
   test_area = new SviwebClassTestArea(svi_web,tab_index++,this,new TestStatusChange());
   search_grid.setWidget(TEST_ROW,1,test_area.getWidget());

   // setupContext(tab_index++);
   setupSecurity(tab_index++);
   // setupContracts(tab_index++);

   setupBottomPanel(tab_index++);
}




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

protected String getSearchType()	{ return "Class"; }
protected int getBottomRow()		{ return BOTTOM_ROW; }
protected int getSecurityRow()		{ return SECURITY_ROW; }
protected int getEnvironmentRow()	{ return ENVIRONMENT_ROW; }

List<SviwebDeclaration> getMethods()	{ return class_methods; }

String getClassName()
{
   if (!name_ok) return null;
   return class_name;
}

void addSignature(Element root) 	{ test_area.addSignature(root); }

String getSuperclassName()
{
   return superclass_name;
}


String getInterfaceNames()
{
   return iface_names;
}

void setIsFull(boolean fg)		{ is_full = fg; }




/********************************************************************************/
/*										*/
/*	Methods to check the status						*/
/*										*/
/********************************************************************************/

Status getStatus()
{
   if (!name_ok) {
      svi_web.addError("Invalid or empty class name");
      return Status.BAD;
    }

   Status sts = Status.OK;

   sts = super_box.validate();
   if (sts != Status.OK) return sts;
   sts = iface_box.validate();
   if (sts != Status.OK) return sts;

   int dct = 0;
   for (SviwebDeclaration sd : class_methods) {
      Status dsts = sd.getStatus();
      if (dsts == Status.OK) dct++;
      else if (dsts != Status.UNUSED) return dsts;
    }
   if (dct == 0) {
      svi_web.addError("No methods defined");
      return Status.BAD;
    }

   sts = test_area.validate();
   if (sts != Status.OK) return sts;

   sts = security_area.validate();
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
   if (is_full) root.setAttribute("WHAT","FULLCLASS");
   else root.setAttribute("WHAT","CLASS");
   addCommonProperties(root);
   addSignature(root);
   SviwebXml.addChild(root,test_area.getTestDescriptions());
   if (security_area != null) security_area.generateOutput(root);
   if (context_area != null) context_area.generateOutput(root);
   if (contract_area != null) contract_area.generateOutput(root);

   return doc;
}



/********************************************************************************/
/*										*/
/*	Handle class name changes						*/
/*										*/
/********************************************************************************/

public void onChange(ChangeEvent evt)
{
   if (evt.getSource() == name_box) {
      name_ok = false;
      String nm = name_box.getText();
      if (nm == null) return;
      nm = nm.trim();
      if (nm.length() == 0) return;

      if (!nm.matches(CLASS_NAME_REGEX)) {
	 name_label.setStyleName("classdecllabelerror");
       }
      else {
	 name_label.setStyleName("classdecllabel");
	 class_name = nm;
	 name_ok = true;
	 for (SviwebDeclaration sd : class_methods) {
	    sd.setClassName(nm);
	  }
	 addClassMethod(0);
       }
    }
}



/********************************************************************************/
/*										*/
/*	Methods to handle method table						*/
/*										*/
/********************************************************************************/

private void addClassMethod(int ct)
{
   int rct = method_table.getRowCount();

   if (rct > ct) return;

   SviwebDeclaration sd = new SviwebDeclaration(svi_web,new MethodChange(ct),class_name);

   method_table.resizeRows(ct+1);
   method_table.setWidget(ct,0,sd.getWidget());

   class_methods.setSize(ct+1);
   class_methods.set(ct,sd);
}




/********************************************************************************/
/*										*/
/*	Methods to handle the class table					*/
/*										*/
/********************************************************************************/


private void updateTests()
{
   test_area.reset();			// ensure there is a test
					// and update methods in tests
   if (contract_area != null) contract_area.reset();
}




/********************************************************************************/
/*										*/
/*	Handle description and test changes					*/
/*										*/
/********************************************************************************/

private class MethodChange implements StatusCallback {

   int method_index;

   MethodChange(int idx) {
      method_index = idx;
    }

   public void setStatus(Status sts) {
      SviwebDeclaration sd = class_methods.get(method_index);
      Widget w = sd.getWidget();
      w.setStyleName("classmethodok");

      if (sts != Status.UNUSED && sts != Status.UNCHECKED) {
	 if (class_methods.size() == method_index+1) addClassMethod(method_index+1);
	 if (sts == Status.OK) {
	    updateTests();
	  }
	 else if (sts == Status.BAD) {
	    w.setStyleName("classmethoderror");
	  }
	 if (status_callback != null) status_callback.setStatus(sts);
       }
    }

}	// end of subclass MethodChange




}	// end of class SviwebMethodSearch



/* end of SviwebClassSearch.java */









