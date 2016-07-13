/********************************************************************************/
/*										*/
/*		SviwebXml.java							*/
/*										*/
/*	XML processing methods							*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/sviweb/client/SviwebXml.java,v 1.5 2015/09/23 17:58:17 spr Exp $ */


/*********************************************************************************
 *
 * $Log: SviwebXml.java,v $
 * Revision 1.5  2015/09/23 17:58:17  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.4  2013/09/13 20:33:16  spr
 * Add calls for UI search.
 *
 * Revision 1.3  2008-08-28 00:33:03  spr
 * Next version of S6.  Lots of bug fixes, some new functionality.
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

import com.google.gwt.xml.client.Attr;
import com.google.gwt.xml.client.CDATASection;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.NamedNodeMap;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.Text;



class SviwebXml implements SviwebConstants {





/********************************************************************************/
/*										*/
/*	Methods to get XML elements						*/
/*										*/
/********************************************************************************/

static Element getXmlElement(Document n,String nm)
{
   NodeList nl = n.getElementsByTagName(nm);
   if (nl == null || nl.getLength() == 0) nl = n.getElementsByTagName("S6:" + nm);
   if (nl == null || nl.getLength() == 0) return null;
   return (Element) nl.item(0);
}


static Element getXmlElement(Element n,String nm)
{
   if (n == null || nm == null) return null;

   NodeList nl = n.getElementsByTagName(nm);
   if (nl == null || nl.getLength() == 0) nl = n.getElementsByTagName("S6:" + nm);
   if (nl == null || nl.getLength() == 0) return null;
   return (Element) nl.item(0);
}


static String getXmlText(Element e)
{
   if (e == null) return "";

   StringBuffer buf = new StringBuffer();

   for (Node n = e.getFirstChild(); n != null; n = n.getNextSibling()) {
      if (n.getNodeType() == Node.CDATA_SECTION_NODE ||
	     n.getNodeType() == Node.TEXT_NODE) {
	 buf.append(n.getNodeValue());
       }
    }

   return buf.toString().trim();
}



static NodeList getXmlElements(Document n,String nm)
{
   NodeList nl = n.getElementsByTagName(nm);
   if (nl == null || nl.getLength() == 0) nl = n.getElementsByTagName("S6:" + nm);

   return nl;
}



static NodeList getXmlElements(Element n,String nm)
{
   NodeList nl = n.getElementsByTagName(nm);
   if (nl == null || nl.getLength() == 0) nl = n.getElementsByTagName("S6:" + nm);

   return nl;
}



static String getXmlAttr(Element e,String id)
{
   String v = e.getAttribute(id);
   if (v == null) v = e.getAttribute("S6:" + id);
   return v;
}



static String getTextElement(Element e,String id)
{
   String s = getXmlAttr(e,id);
   if (s != null) return s;
   Element c = getXmlElement(e,id);
   if (c == null) return null;
   return getXmlText(c);
}



/********************************************************************************/
/*										*/
/*	Methods to build XML elements						*/
/*										*/
/********************************************************************************/

static Element addChild(Document doc,String typ)
{
   Element ne = doc.createElement(typ);
   doc.appendChild(ne);
   return ne;
}




static Element addChild(Element e,String typ)
{
   Document doc = e.getOwnerDocument();
   Element ne = doc.createElement(typ);
   e.appendChild(ne);
   return ne;
}




static Element addChild(Element e,Element c)
{
   Document doc = e.getOwnerDocument();
   Element ne = (Element) doc.importNode(c,true);

   e.appendChild(ne);

   return ne;
}



static Element addTextElement(Element e,String key,String txt)
{
   if (txt == null) return null;

   Document doc = e.getOwnerDocument();

   Element elt = doc.createElement(key);
   e.appendChild(elt);
   elt.appendChild(doc.createCDATASection(txt));

   return elt;
}




static void addText(Element e,String txt)
{
   if (txt == null) return;

   Document doc = e.getOwnerDocument();

   e.appendChild(doc.createCDATASection(txt));
}



/********************************************************************************/
/*										*/
/*	Methods to convert XML to a string					*/
/*										*/
/********************************************************************************/


static String convertXmlToString(Node xml)
{
   if (xml == null) return null;

   StringBuffer sw = new StringBuffer();

   addXml(xml,sw);

   return sw.toString();
}



private static void addXml(Node n,StringBuffer w)
{
   if (n instanceof Element) {
      Element e = (Element) n;
      w.append("<" + e.getNodeName());
      NamedNodeMap nnm = e.getAttributes();
      for (int i = 0; ; ++i) {
	 Node na = nnm.item(i);
	 if (na == null) break;
	 Attr a = (Attr) na;
	 if (a.getSpecified()) {
	    w.append(" " + a.getName() + "='");
	    outputXmlString(a.getValue(),w);
	    w.append("'");
	  }
       }
      if (e.getFirstChild() == null) w.append(" />");
      else {
	 w.append(">");
	 for (Node cn = n.getFirstChild(); cn != null; cn = cn.getNextSibling()) {
	    addXml(cn,w);
	  }
	 w.append("</" + e.getNodeName() + ">");
       }
    }
   else if (n instanceof CDATASection) {
      w.append("<![CDATA[");
      w.append(n.getNodeValue());
      w.append("]]>");
    }
   else if (n instanceof Text) {
      String s = n.getNodeValue();
      if (s != null) outputXmlString(s,w);
    }
   else if (n instanceof Document) {
      Document d = (Document) n;
      addXml(d.getDocumentElement(),w);
    }
}



private static void outputXmlString(String s,StringBuffer pw)
{
   if (s == null) return;

   for (int i = 0; i < s.length(); ++i) {
      char c = s.charAt(i);
      switch (c) {
	 case '&' :
	    pw.append("&amp;");
	    break;
	 case '<' :
	    pw.append("&lt;");
	    break;
	 case '>' :
	    pw.append("&gt;");
	    break;
	 case '"' :
	    pw.append("&quot;");
	    break;
	 case '\'' :
	    pw.append("&apos;");
	    break;
	 default :
	    pw.append(c);
	    break;
       }
    }
}




}	// end of class SviwebXml




/* end of SviwebXml.java */
