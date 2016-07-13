/********************************************************************************/
/*										*/
/*		SviwebFileImpl.java						*/
/*										*/
/*	Server to handle file uploads through firewall to S6 engine		*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/sviweb/server/SviwebFileImpl.java,v 1.3 2015/09/23 17:58:18 spr Exp $ */


/*********************************************************************************
 *
 * $Log: SviwebFileImpl.java,v $
 * Revision 1.3  2015/09/23 17:58:18  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.2  2012-06-11 14:08:24  spr
 * Add framework search; fix bugs
 *
 * Revision 1.1  2009-05-12 23:01:09  spr
 * Add file implementation.
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.sviweb.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.brown.cs.s6.sviweb.client.SviwebConstants;



public class SviwebFileImpl extends HttpServlet
		implements SviwebConstants {




/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private final static long serialVersionUID = 1;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public SviwebFileImpl()
{
}




/********************************************************************************/
/*										*/
/*	Servlet interface							*/
/*										*/
/********************************************************************************/

public void destroy()
{ }


protected void doGet(HttpServletRequest req,HttpServletResponse rsp) throws IOException
{
   rsp.setContentType("text/xml");
   PrintWriter out = rsp.getWriter();
   out.println("<EMPTY />");
   out.close();
}




protected void doPost(HttpServletRequest req,HttpServletResponse rsp) throws IOException
{
   ServletInputStream ins = req.getInputStream();
   byte [] buf = new byte[8192];

   Socket s = new Socket("conifer.cs.brown.edu",17241);
   s.setSoTimeout(1200*1000);
   OutputStreamWriter os = new OutputStreamWriter(s.getOutputStream());
   os.write("<FILE EMBED='TRUE'>\n");
   os.write("<CONTENTS><![CDATA[");

   for ( ; ; ) {
      int rln = ins.read(buf);
      if (rln < 0) break;
      for (int i = 0; i < rln; ++i) {
	 int v = buf[i] & 0xff;
	 String s1 = Integer.toHexString(v);
	 if (s1.length() == 1) os.write("0");
	 os.write(s1);
       }
    }

   os.write("]]></CONTENTS>\n</FILE>\n");
   os.flush();

   BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
   StringBuffer rslt = new StringBuffer();
   for ( ; ; ) {
      String v = br.readLine();
      if (v == null) break;
      if (v.equals("***EOM***")) break;
      rslt.append(v);
      rslt.append("\n");
    }

   s.close();

   rsp.setContentType("text/html");
   PrintWriter out = rsp.getWriter();
   out.println("<HTML><BODY>" + rslt + "</BODY></HTML>");
   out.close();
}




}	// end of class SviwebFileImpl




/* end of SviwebFileImpl.java */
