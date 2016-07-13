/********************************************************************************/
/*                                                                              */
/*              SviwebUISearch.java                                             */
/*                                                                              */
/*	Initial search interface for UI search          			*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/sviweb/client/SviwebUISearch.java,v 1.2 2015/09/23 17:58:17 spr Exp $ */


/*********************************************************************************
 *
 * $Log: SviwebUISearch.java,v $
 * Revision 1.2  2015/09/23 17:58:17  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.1  2013/09/23 14:02:57  spr
 * Add missing file
 *
 *
 ********************************************************************************/





package edu.brown.cs.s6.sviweb.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.XMLParser;



class SviwebUISearch extends SviwebSearch implements SviwebConstants
{


/********************************************************************************/
/*                                                                              */
/*     Private Storage                                                          */
/*                                                                              */
/********************************************************************************/

private Label           file_label;
private SviwebFileWidget file_widget;
private Label           package_label;
private TextBox         package_box;
private Label           class_label;
private TextBox         class_box;
private Label           method_label;
private TextBox         method_box;

private UIStatusCheck   status_checker;
private Status          cur_status;

private int             tab_index;


private static final int PACKAGE_ROW = 1;
private static final int CLASS_ROW = 2;
private static final int METHOD_ROW = 3;
private static final int FILE_ROW = 4;
private static final int CONTEXT_ROW = 5;
private static final int SECURITY_ROW = 6;
private static final int BOTTOM_ROW = 7;

private RegExp package_pattern = RegExp.compile("([A-Za-z]\\w*)(\\.[A-Za-z]\\w*)*");
private RegExp name_pattern = RegExp.compile("[A-Za-z]\\w*");


   
/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

SviwebUISearch(SviwebMain sm,StatusCallback cb)
{
   super(sm,cb);
   
   status_checker = new UIStatusCheck();
   cur_status = Status.BAD;
   
   tab_index = UI_TAB_START;
   
   package_label = new Label("Package:\u00a0");
   package_box = new TextBox();
   package_box.setMaxLength(128);
   package_box.setVisibleLength(48);
   package_box.addChangeHandler(status_checker);
   package_box.setStyleName("classdecl");
   package_box.setTabIndex(tab_index++);
   search_grid.setWidget(PACKAGE_ROW,0,package_label);
   search_grid.setWidget(PACKAGE_ROW,1,package_box);
   
   class_label = new Label("Class\u00a0Name:\u00a0");
   class_box = new TextBox();
   class_box.setMaxLength(64);
   class_box.setVisibleLength(24);
   class_box.addChangeHandler(status_checker);
   class_box.setStyleName("classdecl");
   class_box.setTabIndex(tab_index++);
   search_grid.setWidget(CLASS_ROW,0,class_label);
   search_grid.setWidget(CLASS_ROW,1,class_box);
 
   method_label = new Label("Method\u00a0Name:\u00a0");
   method_box = new TextBox();
   method_box.setMaxLength(64);
   method_box.setVisibleLength(24);
   method_box.addChangeHandler(status_checker);
   method_box.setStyleName("classdecl");
   method_box.setTabIndex(tab_index++);
   search_grid.setWidget(METHOD_ROW,0,method_label);
   search_grid.setWidget(METHOD_ROW,1,method_box);   
   
   
   file_label = new Label("UI\u00a0Picture\u00a0File:\u00a0");
   file_label.setStyleName("classdecllabel");
   
   file_widget = new SviwebFileWidget("UIFILE",cb);
   ++tab_index;
   
   search_grid.setWidget(FILE_ROW,0,file_label);
   search_grid.setWidget(FILE_ROW,1,file_widget.getWidget());
   
   setupContext(tab_index++);
   setupSecurity(tab_index++);
   
   
  
}



/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

@Override protected String getSearchType()      { return "UI"; }
@Override protected int getBottomRow()          { return BOTTOM_ROW; }
@Override protected int getContextRow()         { return CONTEXT_ROW; }
@Override protected int getSecurityRow()        { return SECURITY_ROW; }

@Override protected List<SviwebDeclaration> getMethods()
{
   return new ArrayList<SviwebDeclaration>();
}



/********************************************************************************/
/*                                                                              */
/*      Search request methods                                                  */
/*                                                                              */
/********************************************************************************/

@Override Document getSearchRequest()
{
   Document doc = XMLParser.createDocument();
   Element root = SviwebXml.addChild(doc,"SEARCH");
   root.setAttribute("WHAT","UIFRAMEWORK");
   addCommonProperties(root);
   
   String cnm = class_box.getText();
   String mnm = method_box.getText();
   String vnm = "uicls";
   String rnm = "uirslt";
   
   Element tests = SviwebXml.addChild(root,"TESTS");
   Element tcase = SviwebXml.addChild(tests,"TESTCASE");
   tcase.setAttribute("NAME","SVIWEB_1");
   tcase.setAttribute("TYPE","CALLS");
   Element call = SviwebXml.addChild(tcase,"CALL");
   call.setAttribute("METHOD",cnm);
   call.setAttribute("NEW","true");
   call.setAttribute("OP","SAVE");
   Element oute = SviwebXml.addChild(call,"OUTPUT");
   oute.setAttribute("TYPE","SAVE");
   oute.setAttribute("VALUE",vnm);
   SviwebXml.addTextElement(oute,"CODE",cnm + " " + vnm + ";");
   call = SviwebXml.addChild(tcase,"CALL");
   call.setAttribute("METHOD",mnm);
   call.setAttribute("THIS",vnm);
   call.setAttribute("OP","SAVE");
   oute = SviwebXml.addChild(call,"OUTPUT");
   oute.setAttribute("TYPE","SAVE");
   oute.setAttribute("VALUE",rnm);
   SviwebXml.addTextElement(oute,"CODE","java.awt.component " + rnm + ";");
   call = SviwebXml.addChild(tcase,"CALL");
   call.setAttribute("OP","HIERARCHY");
   call.setAttribute("THIS",rnm);
   call = SviwebXml.addChild(tcase,"CALL");
   call.setAttribute("OP","SHOW");
   call.setAttribute("THIS",rnm);
   
   if (security_area != null) security_area.generateOutput(root);
   if (context_area != null) context_area.generateOutput(root);
   if (contract_area != null) contract_area.generateOutput(root);
   
   return doc;
}

@Override void addSignature(Element root)                 
{
   String pnm = package_box.getText();
   if (pnm == null || pnm.length() == 0) pnm = null;
   String cnm = class_box.getText();
   String mnm = method_box.getText();

   Element sign = SviwebXml.addChild(root,"SIGNATURE");
   Element uelt = SviwebXml.addChild(sign,"UI");
   if (pnm != null) uelt.setAttribute("PACKAGE",pnm);
   uelt.setAttribute("NAME",cnm);
   uelt.setAttribute("FILE","user file");
   Element celt = SviwebXml.addChild(uelt,"CLASS");
   celt.setAttribute("NAME",cnm);
   Element melt = SviwebXml.addChild(celt,"METHOD");
   melt.setAttribute("NAME",mnm);
   melt.setAttribute("RETURN","java.awt.Component");
}


@Override Status getStatus()
{
   return cur_status;
}



/********************************************************************************/
/*                                                                              */
/*      Callback methods                                                        */
/*                                                                              */
/********************************************************************************/

private class UIStatusCheck implements ChangeHandler {
   
   public void onChange(ChangeEvent evt) {
      checkStatus();
    }
   
   private void checkStatus() {
      String s0 = package_box.getText();
      String s1 = class_box.getText();
      String s2 = method_box.getText();
      
      if (s0 != null && s0.length() > 0 && !package_pattern.test(s0)) cur_status = Status.BAD;
      else if (s1 == null || s1.length() == 0 || !name_pattern.test(s1)) cur_status = Status.BAD;
      else if (s2 == null || s2.length() == 0 || !name_pattern.test(s2)) cur_status = Status.BAD;
      else cur_status = Status.OK;
    }
      
}       // end of inner class UIStatusCheck




}       // end of class SviwebUISearch




/* end of SviwebUISearch.java */
