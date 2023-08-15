/********************************************************************************/
/*                                                                              */
/*              SuiseMain.java                                                  */
/*                                                                              */
/*      Main Program for S6 User Interface Search Environment                   */
/*                                                                              */
/********************************************************************************/
/*      Copyright 2013 Brown University -- Steven P. Reiss                    */
/*********************************************************************************
 *  Copyright 2013, Brown University, Providence, RI.                            *
 *                                                                               *
 *                        All Rights Reserved                                    *
 *                                                                               *
 *  Permission to use, copy, modify, and distribute this software and its        *
 *  documentation for any purpose other than its incorporation into a            *
 *  commercial product is hereby granted without fee, provided that the          *
 *  above copyright notice appear in all copies and that both that               *
 *  copyright notice and this permission notice appear in supporting             *
 *  documentation, and that the name of Brown University not be used in          *
 *  advertising or publicity pertaining to distribution of the software          *
 *  without specific, written prior permission.                                  *
 *                                                                               *
 *  BROWN UNIVERSITY DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS                *
 *  SOFTWARE, INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND            *
 *  FITNESS FOR ANY PARTICULAR PURPOSE.  IN NO EVENT SHALL BROWN UNIVERSITY      *
 *  BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY          *
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,              *
 *  WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS               *
 *  ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE          *
 *  OF THIS SOFTWARE.                                                            *
 *                                                                               *
 ********************************************************************************/



package edu.brown.cs.s6.suise;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;

import org.w3c.dom.Element;

import edu.brown.cs.ivy.file.IvyLog;
import edu.brown.cs.ivy.xml.IvyXml;


public class SuiseMain implements SuiseConstants
{


/********************************************************************************/
/*                                                                              */
/*      Main Program                                                            */
/*                                                                              */
/********************************************************************************/

public static void main(String [] args)
{
   SuiseMain sm = new SuiseMain(args);

   sm.process();
}



/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private SuiseSvgProcessor svg_handler;
private String            default_file;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

SuiseMain(String [] args)
{
   default_file = null;

   scanArgs(args);

   svg_handler = new SuiseSvgProcessor();
}



/********************************************************************************/
/*                                                                              */
/*      Argument processing                                                     */
/*                                                                              */
/********************************************************************************/

private void scanArgs(String [] args)
{
   for (int i = 0; i < args.length; ++i) {
      if (args[i].startsWith("-")) {
         badArgs();
       }
      else if (default_file == null) {
         default_file = args[i];
       }
      else {
         badArgs();
       }
    }
}



private void badArgs()
{
   System.err.println("SUISE: suise");
   System.exit(1);
}


/********************************************************************************/
/*                                                                              */
/*      Processing methods                                                      */
/*                                                                              */
/********************************************************************************/

private void process()
{
   SuiseInputPanel pnl = new SuiseInputPanel(this,default_file);
   pnl.setVisible(true);
}



/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

SuiseRawComponent setupSvg(String uri)
{
   return svg_handler.setSvg(uri);
}



/********************************************************************************/
/*                                                                              */
/*      S6 Communication methods                                                */
/*                                                                              */
/********************************************************************************/

static Element sendMessageToS6(String cnts)
{
   byte [] cntb = cnts.getBytes();

   Element rslt = null;

   try {
      URI u = new URI("http://bubbles.cs.brown.edu/s6web/dosearch1.php");
      HttpURLConnection huc = (HttpURLConnection) u.toURL().openConnection();
      huc.setDoInput(true);
      huc.setDoOutput(true);
      huc.setUseCaches(false);
      huc.setRequestMethod("POST");
      huc.setRequestProperty("Accept","application/xml");
      huc.setRequestProperty("Content-Length",Integer.toString(cntb.length));
      huc.setRequestProperty("Content-Type","text/xml");
      huc.connect();
      OutputStream ots = huc.getOutputStream();
      ots.write(cntb);
      ots.close();
      InputStream ins = huc.getInputStream();
      // rslt = IvyXml.loadXmlFromStream(ins);
      Reader r = new InputStreamReader(ins);
      StringBuffer buf = new StringBuffer();
      char [] cbuf = new char[8192];
      for ( ; ; ) {
         int ct = r.read(cbuf);
         if (ct <= 0) break;
         buf.append(cbuf,0,ct);
       }
      // System.err.println("S6 RESULT: " + buf.toString());
      rslt = IvyXml.convertStringToXml(buf.toString());
      ins.close();
    }
   catch (IOException | URISyntaxException e) {
      IvyLog.logE("SUISE","Error sending to S6: " + e);
      return null;
    }

   return rslt;
}



}       // end of class SuiseMain




/* end of SuiseMain.java */

