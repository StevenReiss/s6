/********************************************************************************/
/*										*/
/*		SviwebMultiArea.java						*/
/*										*/
/*	Abstract class for areas with mutliple options				*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/sviweb/client/SviwebMultiArea.java,v 1.8 2015/09/23 17:58:17 spr Exp $ */


/*********************************************************************************
 *
 * $Log: SviwebMultiArea.java,v $
 * Revision 1.8  2015/09/23 17:58:17  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.7  2013/09/13 20:33:16  spr
 * Add calls for UI search.
 *
 * Revision 1.6  2012-06-11 14:08:22  spr
 * Add framework search; fix bugs
 *
 * Revision 1.5  2009-09-18 01:42:37  spr
 * Updates for new gwt and support user feedback and full class.
 *
 * Revision 1.4  2008-11-12 13:52:27  spr
 * Performance and bug updates.
 *
 * Revision 1.3  2008-08-28 00:33:03  spr
 * Next version of S6.	Lots of bug fixes, some new functionality.
 *
 * Revision 1.2  2008-07-17 13:47:46  spr
 * User interface fixups, mainly for class search.
 *
 * Revision 1.1  2008-06-12 17:47:57  spr
 * Next version of S6.
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.sviweb.client;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;



abstract class SviwebMultiArea implements SviwebConstants {




/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

protected SviwebMain	svi_web;
protected SviwebSearch	search_area;
protected int		tab_index;
protected StatusCallback status_callback;

protected List<AreaElement> cur_elements;

private Grid		area_box;
private Status		area_status;

protected static final String TYPE_NONE = "NONE";


protected abstract class AreaElement {

   protected int row_number;
   private Status element_status;
   protected AreaElement(int row) {
      row_number = row;
      element_status = Status.UNCHECKED;
    }

   final int getRowNumber()			{ return row_number; }

   abstract String getTypeName();
   abstract Widget getFirstWidget();
   abstract Widget getSecondWidget();

   abstract boolean isEmpty();

   abstract boolean setTypeName(String typ);

   abstract void generateOutput(Element root);
   void generateCheck(Element root)		{ generateOutput(root); }

   void invalidate()				{ element_status = Status.UNCHECKED; }

   final Status getStatus() {
      if (element_status == Status.UNCHECKED && isEmpty()) element_status = Status.UNUSED;
      return element_status;
    }

   final void setChecking()			{ element_status = Status.CHECKING; }

   final void setOk() {
      if (element_status == Status.CHECKING) element_status = Status.OK;
    }

   final void setError(String msg) {
      if (element_status == Status.CHECKING) {
	 element_status = Status.BAD;
       }
    }

   void addIndex(Element e) {
      e.setAttribute("INDEX",Integer.toString(row_number));
    }

}	// end of interface AreaElement




protected static class AreaType {

   private String type_label;
   private String type_value;
   private boolean is_singleton;

   AreaType(String l,String v) {
      this(l,v,false);
    }

   AreaType(String l,String v,boolean s) {
      type_label = l;
      type_value = v;
      is_singleton = s;
    }

   String getLabel()				{ return type_label; }
   String getValue()				{ return type_value; }
   boolean isSingleton()			{ return is_singleton; }

}	// end of subclass AreaType




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

protected SviwebMultiArea(SviwebMain sm,SviwebSearch ss,int tab,StatusCallback scb)
{
   svi_web = sm;
   search_area = ss;
   tab_index = tab;
   status_callback = scb;

   cur_elements = new ArrayList<AreaElement>();

   area_box = new Grid(0,3);
   area_box.setStyleName("areabox");
   area_box.setCellPadding(2);

   DOM.setElementAttribute(area_box.getElement(),"rules","rows");

   HTMLTable.ColumnFormatter cfmt = area_box.getColumnFormatter();
   cfmt.setWidth(0,"10px");
   cfmt.setWidth(1,"2*");
   cfmt.setWidth(2,"1*");

   setupRow(0);
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

Grid getWidget()				{ return area_box; }



protected void replaceWidget(int row,int idx,Widget w)
{
   area_box.setWidget(row,idx,w);
}




/********************************************************************************/
/*										*/
/*	Validation methods							*/
/*										*/
/********************************************************************************/

void invalidate()
{
   area_status = Status.UNCHECKED;

   for (AreaElement ae : cur_elements) {
      ae.invalidate();
      clearError(ae);
    }
}



Status validate()
{
   if (area_status == Status.CHECKING) return area_status;

   area_status = Status.OK;
   int nelt = 0;
   for (AreaElement ae : cur_elements) {
      if (ae.isEmpty()) continue;
      ++nelt;
      Status sts = ae.getStatus();
      if (sts == Status.UNCHECKED) {
	 area_status = Status.UNCHECKED;
	 break;
       }
      else if (sts != Status.OK) area_status = Status.BAD;
    }

   if (nelt == 0) {
      area_status = Status.OK;
      return area_status;
    }

   if (area_status != Status.UNCHECKED) {
      // add error messages
      return area_status;
    }

   checkElements();

   return Status.CHECKING;
}



private void setStatus(Status s)
{
   area_status = s;
   if (status_callback != null) status_callback.setStatus(s);
}



/********************************************************************************/
/*										*/
/*	Error methods								*/
/*										*/
/********************************************************************************/

void setError(AreaElement ae)
{
   int row = ae.getRowNumber();
   HTMLTable.RowFormatter fmt = area_box.getRowFormatter();
   fmt.setStyleName(row,"arearowerror");
}



void clearError(AreaElement ae)
{
   int row = ae.getRowNumber();
   HTMLTable.RowFormatter fmt = area_box.getRowFormatter();
   fmt.setStyleName(row,"arearowok");
}




/********************************************************************************/
/*										*/
/*	Output methods								*/
/*										*/
/********************************************************************************/

void generateOutput(Element root)
{
   Element selt = SviwebXml.addChild(root,getOutputName());
   for (AreaElement ae : cur_elements) {
      ae.generateOutput(selt);
    }
}




/********************************************************************************/
/*										*/
/*	Specialization methods							*/
/*										*/
/********************************************************************************/

abstract protected String getOutputName();
abstract protected AreaElement createNewElement(int idx);
abstract protected AreaType [] getTypeOptions();



/********************************************************************************/
/*										*/
/*	Row manipulation							*/
/*										*/
/********************************************************************************/

private void setupRow(int row)
{
   while (cur_elements.size() <= row) {
      cur_elements.add(createNewElement(cur_elements.size()));
    }
   if (area_box.getRowCount() <= row) {
      area_box.resizeRows(row+1);
    }

   AreaType [] pt = getTypeOptions();
   HashSet<AreaType> full = new HashSet<AreaType>();
   boolean havesingle = false;
   for (int j = 0; j < pt.length; ++j) {
      if (pt[j].isSingleton()) {
	 havesingle = true;
	 for (AreaElement ce : cur_elements) {
	    if (ce.getTypeName().equals(pt[j].getValue())) full.add(pt[j]);
	  }
       }
    }

   AreaElement ae = cur_elements.get(row);

   ListBox lbx = (ListBox) area_box.getWidget(row,0);
   if (lbx == null) {
      lbx = new ListBox();
      area_box.setWidget(row,0,lbx);
      for (int i = 0; i < pt.length; ++i) {
	 lbx.addItem(pt[i].getLabel(),pt[i].getValue());
       }
      lbx.addChangeHandler(new TypeChange(ae,row));
    }
   for (int i = 0; i < lbx.getItemCount(); ++i) {
      String v = lbx.getValue(i);
      boolean fg = ae.getTypeName().equals(v);
      lbx.setItemSelected(i,fg);
    }

   if (havesingle) resetTypes(pt,full);

   setupWidget(row,1,ae.getFirstWidget());
   setupWidget(row,2,ae.getSecondWidget());
}



private void setupWidget(int row,int col,Widget w)
{
   area_box.clearCell(row,col);
   area_box.setWidget(row,col,w);
   if (w != null) {
      if (w instanceof TextBoxBase) {
	 ((TextBoxBase) w).addChangeHandler(new WidgetChange());
       }
      else if (w instanceof ListBox) {
	 ((ListBox) w).addChangeHandler(new WidgetChange());
       }
    }
}




private void ensureEmptyRow()
{
   for (AreaElement ae : cur_elements) {
      if (ae.isEmpty()) return;
    }

   setupRow(cur_elements.size());
}



private void resetTypes(AreaType [] pt,Set<AreaType> full)
{
   for (int j = 0; j < pt.length; ++j) {
      if (pt[j].isSingleton()) {
	 for (int i = 0; i < cur_elements.size(); ++i) {
	    boolean ign = full.contains(pt[j]);
	    AreaElement ae = cur_elements.get(i);
	    ListBox lbx = (ListBox) area_box.getWidget(i,0);
	    if (ae == null || lbx == null) continue;
	    if (ae.getTypeName().equals(pt[j].getValue())) ign = false;
	    if (ign) {
	       for (int k = 0; k < lbx.getItemCount(); ++k) {
		  if (lbx.getValue(k).equals(pt[j].getValue())) {
		     lbx.removeItem(k);
		     break;
		   }
		}
	     }
	    else {
	       int l = 0;
	       for (int k = 0; k < pt.length; ++k) {
		  String lv = lbx.getValue(l);
		  if (lv.equals(pt[k].getValue())) {
		     if (k == j) break;
		     if (k > j) {
			lbx.insertItem(pt[j].getLabel(),pt[j].getValue(),l);
			break;
		      }
		     ++l;
		     if (l >= lbx.getItemCount()) {
			lbx.addItem(pt[j].getLabel(),pt[j].getValue());
			break;
		      }
		   }
		}
	     }
	  }
       }
    }
}




/********************************************************************************/
/*										*/
/*	Callback for changing element type					*/
/*										*/
/********************************************************************************/

private class TypeChange implements ChangeHandler {

   private AreaElement area_element;
   private int row_number;

   TypeChange(AreaElement ae,int row) {
      area_element = ae;
      row_number = row;
    }

   public void onChange(ChangeEvent evt) {
      ListBox lbx = (ListBox)(evt.getSource());
      int i = lbx.getSelectedIndex();
      String val = lbx.getValue(i);
      if (area_element.setTypeName(val)) {
	 setupRow(row_number);
	 ensureEmptyRow();
       }
    }

}	// end of subclass TypeChange




private class WidgetChange implements ChangeHandler {

   public void onChange(ChangeEvent evt) {
      ensureEmptyRow();
    }

}	// end of subclass WidgetChange



/********************************************************************************/
/*										*/
/*	Server checking methods 						*/
/*										*/
/********************************************************************************/

private void checkElements()
{
   invalidate();

   Document doc = XMLParser.createDocument();
   Element root = SviwebXml.addChild(doc,"CHECK");
   root.setAttribute("WHAT",getOutputName());
   search_area.addSignature(root);

   for (AreaElement ae : cur_elements) {
      if (ae.isEmpty()) continue;
      ae.setChecking();
      ae.generateCheck(root);
    }

   setStatus(Status.CHECKING);
   svi_web.sendToServer(doc,new AreaCallback());
}



private void handleCheckBad(String msg)
{
   if (msg != null) svi_web.addError(msg);

   setStatus(Status.BAD);
}



private void handleCheckOk(Element rslt)
{
   for (AreaElement ae : cur_elements) {
      ae.setOk();
    }

   setStatus(Status.OK);
}



private class AreaCallback implements AsyncCallback<String> {

   public void onSuccess(String srslt) {
      if (srslt == null) handleCheckBad("No response from S6 server");
      else {
	 Document r = XMLParser.parse(srslt);
	 Element rslt = SviwebXml.getXmlElement(r,getOutputName());
	 if (rslt == null) {
	    String emsg = null;
	    Element err = SviwebXml.getXmlElement(r,"PROBLEM");
	    if (err == null) err = SviwebXml.getXmlElement(r,"ERROR");
	    if (err == null) emsg = "S6 Server problem";
	    else emsg = SviwebXml.getXmlText(err);
	    handleCheckBad(emsg);
	  }
	 else {
	    NodeList nl = SviwebXml.getXmlElements(rslt,"ERROR");
	    if (nl == null || nl.getLength() == 0) {
	       handleCheckOk(rslt);
	     }
	    else {
	       for (int i = 0; i < nl.getLength(); ++i) {
		  Element terr = (Element) nl.item(i);
		  int row = Integer.parseInt(SviwebXml.getXmlAttr(terr,"INDEX"));
		  String msg = SviwebXml.getXmlAttr(terr,"MESSAGE");
		  AreaElement ae = cur_elements.get(row);
		  setError(ae);
		  ae.setError(msg);
		  svi_web.addError(msg);
		}
	       handleCheckBad(null);
	     }
	  }
       }
    }

   public void onFailure(Throwable t) {
      String emsg = t.getMessage();
      if (emsg == null) emsg = "S6 Server failure";
      handleCheckBad(emsg);
    }

}	// end of subclass AreaCallback




}	// end of abstract class SviwebMultiArea




/* end of SviwebMultiArea.java */
