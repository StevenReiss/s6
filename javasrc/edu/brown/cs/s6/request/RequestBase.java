/********************************************************************************/
/*										*/
/*		RequestBase.java						*/
/*										*/
/*	Basic implementation of a S6 request					*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/request/RequestBase.java,v 1.8 2016/07/18 23:05:37 spr Exp $ */


/*********************************************************************************
 *
 * $Log: RequestBase.java,v $
 * Revision 1.8  2016/07/18 23:05:37  spr
 * Update request to contain more info for applications.
 *
 * Revision 1.7  2015/09/23 17:58:07  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.6  2014/08/29 15:16:13  spr
 * Updates for suise, testcases.
 *
 * Revision 1.5  2013/09/13 20:33:08  spr
 * Add calls for UI search.
 *
 * Revision 1.4  2012-06-11 18:18:29  spr
 * Include changed/new files for package/ui search
 *
 * Revision 1.3  2012-06-11 14:08:10  spr
 * Add framework search; fix bugs
 *
 * Revision 1.2  2008-08-28 00:32:58  spr
 * Next version of S6.	Lots of bug fixes, some new functionality.
 *
 * Revision 1.1.1.1  2008-06-03 12:59:22  spr
 * Initial version of S6
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.request;


import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.w3c.dom.Element;

import edu.brown.cs.cose.cosecommon.CoseSignature;
import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Engine;
import edu.brown.cs.s6.common.S6Exception;
import edu.brown.cs.s6.common.S6Request;



public class RequestBase implements S6Request, S6Constants, RequestConstants {



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private S6Engine using_engine;
private RequestSignature request_signature;
private RequestContext user_context;
private boolean use_android;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

protected RequestBase(S6Engine eng,Element xml) throws S6Exception
{
   using_engine = eng;

   request_signature = null;
   Element sxml = IvyXml.getChild(xml,"SIGNATURE");
   if (sxml != null) {
      Element pelt = IvyXml.getChild(sxml,"PACKAGE");
      Element celt = IvyXml.getChild(sxml,"CLASS");
      Element melt = IvyXml.getChild(sxml,"METHOD");
      // Element felt = IvyXml.getChild(sxml,"FIELD");
      Element uelt = IvyXml.getChild(sxml,"UI");
      Element telt = IvyXml.getChild(sxml,"TESTING");
      if (pelt != null) request_signature = new RequestPackage(pelt);
      else if (celt != null) request_signature = new RequestClass(celt);
      else if (melt != null) request_signature = new RequestMethod(null,melt);
      else if (uelt != null) request_signature = new RequestUIFramework(uelt);
      else if (telt != null) request_signature = new RequestTesting(telt);
      else throw new S6Exception("Illegal signature");
    }

   Element cxml = IvyXml.getChild(xml,"CONTEXT");
   user_context = new RequestContext(cxml);
   use_android = IvyXml.getAttrBool(xml,"ANDROID");
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

public S6Engine getEngine()			{ return using_engine; }

public S6Request.Signature getSignature()	{ return request_signature; }
public CoseSignature getCoseSignature()         { return request_signature; }

public S6Request.Context getUserContext()	{ return user_context; }

public boolean doDebug()			
{ 
   if (using_engine == null) return false;
   
   return using_engine.doDebug();
}

public String getPackage()                      { return null; }

public boolean useAndroid()                     { return use_android; }



/********************************************************************************/
/*										*/
/*	Helper method for type parsing						*/
/*										*/
/********************************************************************************/

static List<String> getTypesFromString(String s)
{
   List<String> rslt = new ArrayList<String>();

   if (s == null) return rslt;
   if (s.startsWith("(") && s.endsWith(")")) {
      int ln = s.length();
      if (ln == 2) return rslt;
      else s = s.substring(1,ln-1);
    }

   StringTokenizer tok = new StringTokenizer(s,"<>,",true);
   int lvl = 0;
   StringBuffer buf = new StringBuffer();
   while (tok.hasMoreTokens()) {
      String t = tok.nextToken();
      if (t.equals("<")) {
	 buf.append(t);
	 ++lvl;
       }
      else if (t.equals(">")) {
	 if (lvl > 0) --lvl;
	 buf.append(t);
       }
      else if (t.equals(",") && lvl == 0) {
	 rslt.add(buf.toString());
	 buf = new StringBuffer();
       }
      else buf.append(t);
    }
   if (buf.length() > 0) rslt.add(buf.toString());

   return rslt;
}



}	// end of class RequestBase




/* end of RequestBase.java */
