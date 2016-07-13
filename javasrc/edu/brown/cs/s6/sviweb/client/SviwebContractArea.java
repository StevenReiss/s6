/********************************************************************************/
/*										*/
/*		SviwebContractArea.java 					*/
/*										*/
/*	Interactive box to elicit and store contract information		*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/sviweb/client/SviwebContractArea.java,v 1.3 2015/09/23 17:58:16 spr Exp $ */


/*********************************************************************************
 *
 * $Log: SviwebContractArea.java,v $
 * Revision 1.3  2015/09/23 17:58:16  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.2  2008-08-28 00:33:03  spr
 * Next version of S6.  Lots of bug fixes, some new functionality.
 *
 * Revision 1.1  2008-06-12 17:47:57  spr
 * Next version of S6.
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.sviweb.client;

import java.util.List;

import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Element;


class SviwebContractArea extends SviwebMultiArea implements SviwebConstants {



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private static final AreaType [] permit_table = {
   new AreaType("-- NONE --", TYPE_NONE),
   new AreaType("Require", "REQUIRE"),
   new AreaType("Ensure", "ENSURE")
   // add class condition here
};



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

SviwebContractArea(SviwebMain sm,SviwebSearch ss,int tab,StatusCallback scb)
{
   super(sm,ss,tab,scb);
}



/********************************************************************************/
/*										*/
/*	Specialization methods							*/
/*										*/
/********************************************************************************/

protected String getOutputName()		{ return "CONTRACTS"; }

protected AreaElement createNewElement(int r)	{ return new Contract(r); }

protected AreaType [] getTypeOptions()		{ return permit_table; }



/********************************************************************************/
/*										*/
/*	Update methods								*/
/*										*/
/********************************************************************************/

void reset()
{
   for (int i = 0; i < cur_elements.size(); ++i) {
      Contract c = (Contract) cur_elements.get(i);
      c.reset();
    }
}




/********************************************************************************/
/*										*/
/*	Holder of contract information						*/
/*										*/
/********************************************************************************/

private class Contract extends AreaElement {

   private String contract_type;
   private TextArea contract_text;
   private ListBox contract_methods;
   private String contract_method;

   Contract(int row) {
      super(row);
      contract_type = TYPE_NONE;
      contract_text = new TextArea();
      contract_text.setCharacterWidth(64);
      contract_text.setVisibleLines(1);
      contract_methods = new ListBox();
      contract_method = null;
      reset();
    }

   String getTypeName() 			{ return contract_type; }
   Widget getFirstWidget()			{ return contract_methods; }
   Widget getSecondWidget()			{ return contract_text; }

   boolean isEmpty() {
      if (contract_type.equals(TYPE_NONE)) return true;
      String txt = contract_text.getText().trim();
      if (txt.length() == 0) return true;
      return false;
    }

   boolean setTypeName(String typ) {
      if (typ.equals(getTypeName())) return false;
      contract_type = typ;
      return true;
    }

   void generateOutput(Element root) {
      if (contract_type.equals("NONE")) return;
      String txt = contract_text.getText().trim();
      if (txt.length() == 0) return;
      Element c = SviwebXml.addChild(root,"CONTRACT");
      addIndex(c);
      c.setAttribute("TYPE",contract_type);
      SviwebXml.addTextElement(c,"CODE",txt);
      SviwebXml.addTextElement(c,"METHOD",contract_method);
    }

   void reset() {
      List<SviwebDeclaration> mlst = search_area.getMethods();
      contract_methods.clear();
      int idx = 0;
      for (int i = 0; i < mlst.size(); ++i) {
	 SviwebDeclaration sd = mlst.get(i);
	 String nm = sd.getName();
	 if (nm == null) nm = "<NONE>";
	 contract_methods.addItem(nm);
	 if (nm.equals(contract_method)) idx = i;
       }
      contract_method = contract_methods.getItemText(idx);
      contract_methods.setSelectedIndex(idx);
    }

}	// end of subclass Contract





}	// end of class SviwebContractArea




/* end of SviwebContractArea.java */
