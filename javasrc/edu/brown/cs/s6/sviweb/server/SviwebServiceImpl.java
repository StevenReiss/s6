/********************************************************************************/
/*										*/
/*		SviwebServiceImpl.java						*/
/*										*/
/*	Server to send commands through firewall to S6 engine			*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/sviweb/server/SviwebServiceImpl.java,v 1.4 2015/09/23 17:58:18 spr Exp $ */


/*********************************************************************************
 *
 * $Log: SviwebServiceImpl.java,v $
 * Revision 1.4  2015/09/23 17:58:18  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.3  2008-11-12 13:52:28  spr
 * Performance and bug updates.
 *
 * Revision 1.2  2008-06-12 17:47:58  spr
 * Next version of S6.
 *
 * Revision 1.1.1.1  2008-06-03 12:59:23  spr
 * Initial version of S6
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.sviweb.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.net.Socket;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import edu.brown.cs.s6.sviweb.client.SviwebConstants;
import edu.brown.cs.s6.sviweb.client.SviwebService;


public class SviwebServiceImpl extends RemoteServiceServlet
		implements SviwebService, SviwebConstants {




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

public SviwebServiceImpl() {
}


/********************************************************************************/
/*										*/
/*	Action methods								*/
/*										*/
/********************************************************************************/

@SuppressWarnings("resource")
public String sendToServer(String cmd)
{
   String host = "localhost";

   log("SEND TO SERVER " + host + ": " + cmd);

   try {
      Socket s = new Socket(host,17241);
      s.setSoTimeout(1200*1000);
      OutputStreamWriter os = new OutputStreamWriter(s.getOutputStream());
      os.write(cmd);
      os.write("\n");
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
      return rslt.toString();
    }
   catch (IOException e) {
      log("S6 Server problem: " + e);
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      e.printStackTrace(pw);
      log("S6: " + sw.toString());
    }

   return null;
}



}	// end of SviwebServiceImpl




/* end of SviwebServiceImpl.java */































































































































































































































































































































































































































































































































































































































































































































































































































































































































