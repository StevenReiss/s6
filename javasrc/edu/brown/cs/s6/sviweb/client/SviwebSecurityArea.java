/********************************************************************************/
/*										*/
/*		SviwebSecurityArea.java 					*/
/*										*/
/*	Interactive box to elicit and store security policy information 	*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/sviweb/client/SviwebSecurityArea.java,v 1.5 2015/09/23 17:58:17 spr Exp $ */


/*********************************************************************************
 *
 * $Log: SviwebSecurityArea.java,v $
 * Revision 1.5  2015/09/23 17:58:17  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.4  2012-06-11 14:08:22  spr
 * Add framework search; fix bugs
 *
 * Revision 1.3  2009-09-18 01:42:37  spr
 * Updates for new gwt and support user feedback and full class.
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

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Element;



class SviwebSecurityArea extends SviwebMultiArea implements SviwebConstants {



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private static final AreaType [] permit_table = {
   new AreaType("-- NONE --", TYPE_NONE),
   new AreaType("FILE", "FILE"),
   new AreaType("SOCKET", "SOCKET"),
   new AreaType("WINDOWS", "AWT", true),
   new AreaType("PROPERTY", "PROPERTY"),
   new AreaType("SYSTEM", "RUNTIME", true)
};

private static final String USER_VALUE = "@";
private static final String ALL_VALUE = "*";



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

SviwebSecurityArea(SviwebMain sm,SviwebSearch ss,int tab,StatusCallback scb)
{
   super(sm,ss,tab,scb);
}



/********************************************************************************/
/*										*/
/*	Specialization methods							*/
/*										*/
/********************************************************************************/

protected String getOutputName()		{ return "SECURITY"; }

protected AreaElement createNewElement(int r)	{ return new SecPermit(r); }

protected AreaType [] getTypeOptions()		{ return permit_table; }




/********************************************************************************/
/*										*/
/*	Holder of security permissions						*/
/*										*/
/********************************************************************************/

private class SecPermit extends AreaElement implements ChangeHandler {

   private SecPermitImpl cur_impl;

   SecPermit(int row) {
      super(row);
      cur_impl = new SecPermitNone(this);
    }

   String getTypeName() {
      return cur_impl.getName();
    }

   Widget getFirstWidget()			{ return cur_impl.getFirstWidget(); }
   Widget getSecondWidget()			{ return cur_impl.getSecondWidget(); }

   void generateOutput(Element root)		{ cur_impl.addSecurity(root); }

   boolean isEmpty()				{ return cur_impl.isEmpty(); }

   boolean setTypeName(String typ) {
      if (typ.equals(getTypeName())) return false;
      if (typ.equals(TYPE_NONE)) cur_impl = new SecPermitNone(this);
      else if (typ.equals("FILE")) cur_impl = new SecPermitFile(this);
      else if (typ.equals("SOCKET")) cur_impl = new SecPermitSocket(this);
      else if (typ.equals("AWT")) cur_impl = new SecPermitAwt(this);
      else if (typ.equals("PROPERTY")) cur_impl = new SecPermitProperty(this);
      else if (typ.equals("RUNTIME")) cur_impl = new SecPermitRuntime(this);
      return true;
    }

   public void onChange(ChangeEvent evt) {
      if (evt.getSource() instanceof ListBox) {
	 ListBox lbx = (ListBox) evt.getSource();
	 String v = lbx.getValue(lbx.getSelectedIndex());
	 if (v.equals(USER_VALUE)) {
	    TextBox tbx = new TextBox();
	    tbx.setMaxLength(256);
	    tbx.setVisibleLength(24);
	    tbx.addChangeHandler(this);
	    replaceWidget(row_number,1,tbx);
	  }
       }
      else if (evt.getSource() instanceof TextBox) {
	 TextBox tbx = (TextBox) evt.getSource();
	 String val = tbx.getText().trim();
	 if (val != null && val.length() > 0) cur_impl.setUserValue(val);
	 replaceWidget(row_number,1,getFirstWidget());
       }
    }

}	// end of subclass SecPermit



/********************************************************************************/
/*										*/
/*	Class for different types of security permissions			*/
/*										*/
/********************************************************************************/

private abstract class SecPermitImpl {

   private SecPermit for_permit;

   protected SecPermitImpl(SecPermit sp) {
      for_permit = sp;
    }

   abstract String getName();
   boolean isEmpty()				{ return false; }

   ListBox getFirstWidget()			{ return null; }
   ListBox getSecondWidget()			{ return null; }

   SecPermit getPermit()			{ return for_permit; }

   void setUserValue(String v)			{ }

   void addSecurity(Element e)			{ }

   protected void addOps(Element e,String [] ops,int val) {
      getPermit().addIndex(e);
      StringBuffer buf = new StringBuffer();
      for (int i = 0; i < ops.length; ++i) {
	 if ((val & (1 << i)) != 0) {
	    if (buf.length() > 0) buf.append(",");
	    buf.append(ops[i]);
	  }
       }
      SviwebXml.addTextElement(e,"ACTIONS",buf.toString());
    }

   protected ListBox setupOpWidget(String [] ops,int acc) {
      ListBox lbx = new ListBox(true);
      for (int i = 0; i < ops.length; ++i) {
	 lbx.addItem(ops[i]);
	 if ((acc & (1 << i)) != 0) lbx.setItemSelected(i,true);
       }
      return lbx;
    }

   protected ListBox setupDetailWidget(String all,String other) {
      ListBox lbx = new ListBox();
      lbx.addItem(all,ALL_VALUE);
      lbx.setItemSelected(0,true);
      lbx.addItem(other,USER_VALUE);
      lbx.addChangeHandler(getPermit());
      return lbx;
    }

   protected void updateDetailWidget(ListBox lbx,String curval) {
      String v = lbx.getValue(1);
      if (curval.equals(ALL_VALUE)) {
	 if (!v.equals(USER_VALUE)) lbx.removeItem(1);
	 lbx.setItemSelected(0,true);
       }
      else {
	 if (v.equals(USER_VALUE)) {
	    lbx.insertItem(curval,curval,1);
	    lbx.setItemSelected(1,true);
	  }
	 else {
	    lbx.setItemText(1,curval);
	    lbx.setValue(1,curval);
	  }
       }
    }

}	// end of abstract subclass SecPermitImpl




/********************************************************************************/
/*										*/
/*	Empty security permission						*/
/*										*/
/********************************************************************************/

private class SecPermitNone extends SecPermitImpl {

   SecPermitNone(SecPermit sp)		{ super(sp); }

   String getName()			{ return TYPE_NONE; }

   boolean isEmpty()			{ return true; }

}	// end of subclass SecPermitNone



/********************************************************************************/
/*										*/
/*	File security permission						*/
/*										*/
/********************************************************************************/

private static final String [] FILE_OPS = {
   "read", "write", "execute", "delete"
};
private static final int DFLT_FILE_VALUE = 15;



private class SecPermitFile extends SecPermitImpl {

   private String file_path;
   private int cur_access;
   private ListBox detail_widget;
   private ListBox op_widget;

   SecPermitFile(SecPermit sp) {
      super(sp);
      file_path = ALL_VALUE;
      cur_access = DFLT_FILE_VALUE;

      detail_widget = setupDetailWidget("ALL FILES","Other Path");
      op_widget = setupOpWidget(FILE_OPS,cur_access);
    }

   String getName()			{ return "FILE"; }

   void setUserValue(String v) {
      file_path = v;
      updateDetailWidget(detail_widget,file_path);
    }

   boolean isEmpty()			{ return false; }

   ListBox getFirstWidget()		{ return detail_widget; }
   ListBox getSecondWidget()		{ return op_widget; }

   void addSecurity(Element root) {
      Element e = SviwebXml.addChild(root,"PERMISSION");
      e.setAttribute("TYPE","FILE");
      SviwebXml.addTextElement(e,"VALUE",file_path);
      addOps(e,FILE_OPS,cur_access);
    }

}	// end of subclass SecPermitFile




/********************************************************************************/
/*										*/
/*	Socket security permission						*/
/*										*/
/********************************************************************************/

private static final String [] SOCKET_OPS = {
   "accept", "connect", "listen", "resolve"
};
private static final int DFLT_SOCKET_VALUE = 15;



private class SecPermitSocket extends SecPermitImpl {

   private String addr_path;
   private int cur_access;
   private ListBox detail_widget;
   private ListBox op_widget;

   SecPermitSocket(SecPermit sp) {
      super(sp);
      addr_path = ALL_VALUE;
      cur_access = DFLT_SOCKET_VALUE;

      detail_widget = setupDetailWidget("ALL ADDRESSES","Other Address");
      op_widget = setupOpWidget(SOCKET_OPS,cur_access);
    }

   String getName()			{ return "SOCKET"; }

   void setUserValue(String v) {
      addr_path = v;
      updateDetailWidget(detail_widget,addr_path);
    }

   ListBox getFirstWidget()		{ return detail_widget; }
   ListBox getSecondWidget()		{ return op_widget; }

   void addSecurity(Element root) {
      Element e = SviwebXml.addChild(root,"PERMISSION");
      e.setAttribute("TYPE","SOCKET");
      SviwebXml.addTextElement(e,"VALUE",addr_path);
      addOps(e,SOCKET_OPS,cur_access);
    }

}	// end of subclass SecPermitSocket




/********************************************************************************/
/*										*/
/*	Swing/Awt security permission						*/
/*										*/
/********************************************************************************/

private static final String [] AWT_OPS = {
   "clipboard", "events", "system_access"
};
private static final int DFLT_AWT_VALUE = 0;



private class SecPermitAwt extends SecPermitImpl {

   private int cur_access;
   private ListBox op_widget;

   SecPermitAwt(SecPermit sp) {
      super(sp);
      cur_access = DFLT_AWT_VALUE;
      op_widget = setupOpWidget(AWT_OPS,cur_access);
    }

   String getName()			{ return "AWT"; }

   ListBox getFirstWidget()		{ return op_widget; }

   void addSecurity(Element root) {
      Element e = SviwebXml.addChild(root,"PERMISSION");
      e.setAttribute("TYPE","AWT");
      addOps(e,AWT_OPS,cur_access);
    }

}	// end of subclass SecPermitAwt




/********************************************************************************/
/*										*/
/*	Property security permission						*/
/*										*/
/********************************************************************************/

private static final String [] PROPERTY_OPS = {
   "read", "write"
};
private static final int DFLT_PROP_VALUE = 4;



private class SecPermitProperty extends SecPermitImpl {

   private String prop_name;
   private int cur_access;
   private ListBox detail_widget;
   private ListBox op_widget;

   SecPermitProperty(SecPermit sp) {
      super(sp);
      prop_name = ALL_VALUE;
      cur_access = DFLT_PROP_VALUE;

      detail_widget = setupDetailWidget("ALL PROPERTIES","Other Property");
      op_widget = setupOpWidget(PROPERTY_OPS,cur_access);
    }

   String getName()			{ return "PROPERTY"; }

   void setUserValue(String v) {
      prop_name = v;
      updateDetailWidget(detail_widget,prop_name);
    }

   ListBox getFirstWidget()		{ return detail_widget; }
   ListBox getSecondWidget()		{ return op_widget; }

   void addSecurity(Element root) {
      Element e = SviwebXml.addChild(root,"PERMISSION");
      e.setAttribute("TYPE","PROPERTY");
      SviwebXml.addTextElement(e,"VALUE",prop_name);
      addOps(e,PROPERTY_OPS,cur_access);
    }

}	// end of subclass SecPermitProperty




/********************************************************************************/
/*										*/
/*	System and runtime and general security permissions			*/
/*										*/
/********************************************************************************/

private static final String [] RUNTIME_OPS = {
   "class_loader","security","environment","exit","system_io","threads","native_code",
   "reflection","stack","preferences","serialize"

};
private static final int DFLT_RUNTIME_VALUE = 0;



private class SecPermitRuntime extends SecPermitImpl {

   private int cur_access;
   private ListBox op_widget;

   SecPermitRuntime(SecPermit sp) {
      super(sp);
      cur_access = DFLT_RUNTIME_VALUE;
      op_widget = setupOpWidget(RUNTIME_OPS,cur_access);
    }

   String getName()			{ return "RUNTIME"; }

   ListBox getFirstWidget()		{ return op_widget; }

   void addSecurity(Element root) {
      Element e = SviwebXml.addChild(root,"PERMISSION");
      e.setAttribute("TYPE","RUNTIME");
      addOps(e,RUNTIME_OPS,cur_access);
    }

}	// end of subclass SecPermitRuntime




}	// end of class SviwebSecurityArea



/* end of SviwebSecurityArea.java */
