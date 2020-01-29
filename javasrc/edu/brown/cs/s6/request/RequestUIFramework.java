/********************************************************************************/
/*										*/
/*		RequestUIFramework.java 					*/
/*										*/
/*	Handle UI Framework requests						*/
/*										*/
/********************************************************************************/
/*	Copyright 2012 Brown University -- Steven P. Reiss		      */
/*	Copyright 2012 Brown University -- Danny Schneider		      */
/*********************************************************************************
 *  Copyright 2012, Brown University, Providence, RI.				 *
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/request/RequestUIFramework.java,v 1.6 2015/09/23 17:58:08 spr Exp $ */


/*********************************************************************************
 *
 * $Log: RequestUIFramework.java,v $
 * Revision 1.6  2015/09/23 17:58:08  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.5  2013/09/13 20:33:09  spr
 * Add calls for UI search.
 *
 * Revision 1.4  2013-05-09 12:26:23  spr
 * Minor changes to start ui fixups.
 *
 * Revision 1.3  2012-07-20 22:15:32  spr
 * UI search request
 *
 * Revision 1.2  2012-06-20 12:21:35  spr
 * Initial fixes for UI search
 *
 * Revision 1.1  2012-06-11 18:18:29  spr
 * Include changed/new files for package/ui search
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.request;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.w3c.dom.Element;

import edu.brown.cs.cose.cosecommon.CoseSignature;
import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.s6.common.S6Exception;
import edu.brown.cs.s6.common.S6Request;


class RequestUIFramework extends RequestSignature implements S6Request.UISignature
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private CompDescription 	root_component;
private String			package_name;
private String			class_name;
private Map<String,CompDescription> comp_map;
private List<S6Request.ClassSignature> base_classes;







/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

RequestUIFramework(Element xml) throws S6Exception
{
   package_name = IvyXml.getTextElement(xml,"PACKAGE");
   class_name = IvyXml.getTextElement(xml,"NAME");

   base_classes = new ArrayList<S6Request.ClassSignature>();
   for (Element cml : IvyXml.children(xml,"CLASS")) {
      RequestClass rq = new RequestClass(cml);
      base_classes.add(rq);
    }

   String fnm = IvyXml.getTextElement(xml,"FILE");
   Element svg = IvyXml.getChild(xml,"SVG");

   if (fnm != null) {
      if (fnm.endsWith(".odg")) {       // open office draw
	 readOfficeDrawFile(fnm);
       }
      else if (fnm.endsWith(".svg")) {  // handle svg file
	 svg = IvyXml.loadXmlFromFile(fnm);
	 processSvg(svg);
       }
    }
   else if (svg != null) {
      processSvg(svg);
    }
   else {
      Element celt = IvyXml.getChild(xml,"COMPONENT");
      comp_map = new HashMap<String,CompDescription>();
      root_component = loadComponents(celt);
    }

}




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override public String getName()				{ return package_name; }

@Override public List<S6Request.ClassSignature> getClasses()	{ return base_classes; }

@Override public List<CoseSignature.CoseClassSignature> getCoseClasses()
{ 
   return new ArrayList<>(base_classes);
}




public String getClassName()					{ return class_name; }

public S6Request.MethodSignature getMethod(String name) 	{ return null; }

public S6Request.UIComponent getHierarchy()			{ return root_component; }




/********************************************************************************/
/*										*/
/*	Methods to load component descriptions					*/
/*										*/
/********************************************************************************/

private CompDescription loadComponents(Element xml)
{
   if (xml == null) return null;

   CompDescription cd = new CompDescription(xml);
   comp_map.put(cd.getId(),cd);
   for (Element celt : IvyXml.children(xml,"COMPONENT")) {
      CompDescription ccd = loadComponents(celt);
      cd.addChild(ccd);
    }

   loadComponentData(xml);

   return cd;
}



private void loadComponentData(Element xml)
{
   String id = IvyXml.getAttrString(xml,"ID");
   CompDescription cd = comp_map.get(id);
   cd.loadXml(xml);

   for (Element celt : IvyXml.children(xml,"COMPONENT")) {
      loadComponentData(celt);
    }
}






/********************************************************************************/
/*										*/
/*	Handle parsing Open Office Draw Files					*/
/*										*/
/********************************************************************************/


private void readOfficeDrawFile(String fnm) throws S6Exception
{
   try {
      ZipFile zf = new ZipFile(fnm);
      ZipEntry ze = zf.getEntry("content.xml");
      if (ze == null) {
	 zf.close();
	 throw new S6Exception("Invalid open office draw file");
       }
      InputStream ins = zf.getInputStream(ze);
      Element xml = IvyXml.loadXmlFromStream(ins);
      loadOfficeDraw(xml);
      ins.close();
      zf.close();
    }
   catch (IOException e) {
      throw new S6Exception("Problem reading open office draw file",e);
    }
}



private void loadOfficeDraw(Element xml)
{
   List<UIElement> elts = new ArrayList<UIElement>();

   for (Element ce : IvyXml.elementsByTag(xml,"draw:custom-shape")) {
      double w = convertUnits(IvyXml.getAttrString(ce,"svg:width"));
      double h = convertUnits(IvyXml.getAttrString(ce,"svg:height"));
      double x = convertUnits(IvyXml.getAttrString(ce,"svg:x"));
      double y = convertUnits(IvyXml.getAttrString(ce,"svg:y"));
      String name = IvyXml.getTextElement(ce,"text:p");
      UIElement elt = new UIElement(w,h,x,y,name);
      elts.add(elt);
    }

   if (elts.size() == 0) return;

   // compute hierarchy
   for (int i = 0; i < elts.size(); ++i) {
      for (int j = 0; j < elts.size(); ++j) {
	 if (i != j && elts.get(i).isChild(elts.get(j))) {
	    elts.get(i).addChild(elts.get(j));
	  }
       }
    }

   // remove children of children
   for (int i = 0; i < elts.size(); ++i) {
      List<UIElement> children = elts.get(i).getChildren();
      for (Iterator<UIElement> it = children.iterator(); it.hasNext(); ) {
	 UIElement e = it.next();
	 boolean del = false;
	 for (UIElement e1 : children) {
	    if (e1 != e && e1.isChild(e)) del = true;
	  }
	 if (del) it.remove();
       }
    }

   if (elts.size() > 1) {
      UIElement u0 = elts.get(0);
      double x = u0.getX();
      double y = u0.getY();
      double w = u0.getWidth();
      double h = u0.getHeight();
      for (int i = 1; i < elts.size(); ++i) {
	 UIElement ue = elts.get(i);
	 if (ue.getX() < x) x = ue.getX();
	 if (ue.getX() + ue.getWidth() > x + w) {
	    w = ue.getX() + ue.getWidth() - x;
	  }
	 if (ue.getY() < y) y = ue.getY();
	 if (ue.getY() + ue.getHeight() > y + h) {
	    h = ue.getY() + ue.getHeight() - y;
	  }
       }
      u0 = new UIElement(x,y,w,h,null);
      for (UIElement ue : elts) u0.addChild(ue);
      elts.clear();
      elts.add(u0);
    }

   root_component = new CompDescription(elts.get(0));
}



private double convertUnits(String v)
{
   if (v == null) return 0;

   int ln = v.length();
   double scale = 1.0;
   if (v.endsWith("cm")) v = v.substring(0,ln-2);

   return scale * Double.valueOf(v);
}




private static class UIElement {

   private double elt_width;
   private double elt_height;
   private double x_pos;
   private double y_pos;
   private boolean is_label;
   private List<UIElement> child_elements;
   private String elt_id;

   private static int id_counter = 0;

   UIElement(double w,double h,double x,double y,String nm) {
      elt_width = w;
      elt_height = h;
      x_pos = x;
      y_pos = y;
      is_label = (nm != null);
      child_elements = new ArrayList<UIElement>();
      elt_id = "U_" + (++id_counter);
    }

   double getWidth()			{ return elt_width; }
   double getHeight()			{ return elt_height; }
   double getX()			{ return x_pos; }
   double getY()			{ return y_pos; }
   boolean isLabel()			{ return (isPanel() ? false : is_label); }
   boolean isPanel()			{ return child_elements.size() > 0; }
   String getId()			{ return elt_id; }

   boolean isChild(UIElement e) {
      if (x_pos <= e.getX() && x_pos+elt_width >= e.getX() + e.getWidth() &&
	    y_pos <= e.getY() && y_pos+elt_height >= e.getY() + e.getHeight())
	 return true;
      return false;
    }

   void addChild(UIElement e)		{ child_elements.add(e); }
   List<UIElement> getChildren()	{ return child_elements; }

   List<String> getTypes() {
      List<String> rslt = new ArrayList<String>();
      if (isLabel()) rslt.add("javax.swing.JLabel");
      if (child_elements.size() == 0) {
	 rslt.add("javax.swing.JTextField");
       }
      else {
	 rslt.add("javax.swing.Container");
       }
      return rslt;
    }

}	// end of inner class UIElement




/********************************************************************************/
/*										*/
/*	Handle svg files							*/
/*										*/
/********************************************************************************/

private void processSvg(Element xml)
{
}




/********************************************************************************/
/*										*/
/*	Component description							*/
/*										*/
/********************************************************************************/

private class CompDescription implements S6Request.UIComponent {

   private double comp_x;
   private double comp_y;
   private double comp_width;
   private double comp_height;
   private CompDescription top_anchor;
   private CompDescription bottom_anchor;
   private CompDescription left_anchor;
   private CompDescription right_anchor;
   private String comp_id;
   private List<String> type_names;
   private List<S6Request.UIComponent> child_components;
   private String comp_data;

   CompDescription(Element xml) {
      comp_id = IvyXml.getAttrString(xml,"ID");
      child_components = new ArrayList<S6Request.UIComponent>();
    }

   CompDescription(UIElement ue) {
      comp_id = ue.getId();
      comp_map.put(ue.getId(),this);
      comp_x = (int)(ue.getX()*10);
      comp_y = (int)(ue.getY()*10);
      comp_width = (int)(ue.getWidth()*10);
      comp_height = (int)(ue.getHeight()*10);
      top_anchor = null;
      bottom_anchor = null;
      left_anchor = null;
      right_anchor = null;
      type_names = ue.getTypes();
      child_components = new ArrayList<S6Request.UIComponent>();
      for (UIElement u0 : ue.getChildren()) {
	 CompDescription c0 = new CompDescription(u0);
	 child_components.add(c0);
       }
    }

   void addChild(CompDescription cd)				{ child_components.add(cd); }
   void loadXml(Element xml) {
      comp_x = IvyXml.getAttrDouble(xml,"X",-1);
      comp_y = IvyXml.getAttrDouble(xml,"Y",-1);
      comp_width = IvyXml.getAttrDouble(xml,"WIDTH",-1);
      comp_height = IvyXml.getAttrDouble(xml,"HEIGHT",-1);
      top_anchor = getComponent(xml,"TOP");
      bottom_anchor = getComponent(xml,"BOTTOM");
      left_anchor = getComponent(xml,"LEFT");
      right_anchor = getComponent(xml,"RIGHT");
      comp_data = IvyXml.getTextElement(xml,"DATA");
      String typs = IvyXml.getTextElement(xml,"TYPES");
      if (typs == null) type_names = null;
      else {
	 StringTokenizer tok = new StringTokenizer(typs,",");
	 type_names = new ArrayList<String>();
	 while (tok.hasMoreTokens()) type_names.add(tok.nextToken());
       }
    }


   private CompDescription getComponent(Element xml,String what) {
      String id = IvyXml.getAttrString(xml,what);
      if (id == null) return null;
      return comp_map.get(id);
    }

   @Override public String getId()				{ return comp_id; }
   @Override public List<S6Request.UIComponent> getChildren()	{ return child_components; }

   @Override public List<String> getTypes()			{ return type_names; }
   @Override public double getXposition()			{ return comp_x; }
   @Override public double getYposition()			{ return comp_y; }
   @Override public double getWidth()				{ return comp_width; }
   @Override public double getHeight()				{ return comp_height; }
   @Override public CompDescription getTopAnchor()		{ return top_anchor; }
   @Override public CompDescription getBottomAnchor()		{ return bottom_anchor; }
   @Override public CompDescription getLeftAnchor()		{ return left_anchor; }
   @Override public CompDescription getRightAnchor()		{ return right_anchor; }
   @Override public String getData()				{ return comp_data; }

}	// end of inner class CompDescription



}	// end of class RequestUIFramework




/* end of RequestUIFramework.java */
