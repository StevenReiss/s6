/********************************************************************************/
/*										*/
/*		EngineTester.java						*/
/*										*/
/*	Junit tests for S6 search Engine					*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/engine/EngineTester.java,v 1.8 2015/09/23 17:57:55 spr Exp $ */


/*********************************************************************************
 *
 * $Log: EngineTester.java,v $
 * Revision 1.8  2015/09/23 17:57:55  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.7  2015/03/11 18:04:03  spr
 * Clean up names.
 *
 * Revision 1.6  2013/09/13 20:32:17  spr
 * Handle UI search.
 *
 * Revision 1.5  2013-05-09 12:26:17  spr
 * Minor changes to start ui fixups.
 *
 * Revision 1.4  2009-09-18 01:40:41  spr
 * Handle user interaction.
 *
 * Revision 1.3  2008-11-12 13:51:31  spr
 * Performance and bug updates.
 *
 * Revision 1.2  2008-06-12 17:47:48  spr
 * Next version of S6.
 *
 * Revision 1.1.1.1  2008-06-03 12:59:21  spr
 * Initial version of S6
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.engine;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.StringTokenizer;

import junit.framework.TestCase;

import org.w3c.dom.Element;

import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.ivy.xml.IvyXmlReader;
import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Exception;
import edu.brown.cs.s6.common.S6Factory;



public class EngineTester extends TestCase implements S6Constants, EngineConstants {



/********************************************************************************/
/*										*/
/*	Main program								*/
/*										*/
/********************************************************************************/

public static void main(String [] args)
{
   try {
      allTests();
    }
   catch (Throwable t) {
      System.err.println("ERROR: " + t);
      t.printStackTrace();
    }

   System.exit(0);
}



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public EngineTester()
{
}




/********************************************************************************/
/*										*/
/*	Load test case								*/
/*										*/
/********************************************************************************/

public static void XtestLoad() throws S6Exception
{
   EngineMain engine = new EngineMain(new String[]{ "-t","1" });
   S6Factory s6f = S6Factory.createS6Factory();
   // this is a test this is a test this is a test
   // of simple editing
   Element xml = IvyXml.loadXmlFromFile("/pro/s6/engine/src/test00.s6",true);

   s6f.createSearchRequest(engine,xml);
}





public static void testMethod() throws S6Exception
{
   EngineMain em = new EngineMain(new String[]{ "-t","1" });

   Element xml = IvyXml.loadXmlFromFile("/pro/s6/engine/src/test00.s6",true);

   String r = em.handleSearchRequest(xml);

   System.err.println("SEARCH RESULT = " + r);
}



public static void simpleTest() throws S6Exception
{
   EngineMain em = new EngineMain(new String[]{ "-t","1" });

   Element xml = IvyXml.loadXmlFromFile("/pro/s6/engine/src/test10.s6",true);
   // Element xml = IvyXml.loadXmlFromFile("/pro/s6/engine/src/test04.s6",true);
   // Element xml = IvyXml.loadXmlFromFile("/pro/s6/engine/src/test01.s6",true);

   String r = em.handleSearchRequest(xml);

   System.err.println("SEARCH RESULT = " + r);
}



public static void testClass() throws S6Exception
{
   EngineMain em = new EngineMain(new String[]{ "-t","1" });

   Element xml = IvyXml.loadXmlFromFile("/pro/s6/engine/src/test02.s6",true);

   String r = em.handleSearchRequest(xml);

   System.err.println("SEARCH RESULT = " + r);
}



public static void XtestTypes() throws S6Exception
{
   EngineMain em = new EngineMain(new String[]{ });

   Element xml = IvyXml.loadXmlFromFile("/pro/s6/engine/src/test01.s6",true);

   String r = em.handleSearchRequest(xml);

   System.err.println("SEARCH RESULT = " + r);
}



public static void testConnect() throws S6Exception, IOException
{
   EngineMain em = new EngineMain(new String[]{"-server", "-Xnum", "1" });
   em.start();

   File f = new File(S6_ENGINE_SERVER_SOCKET);
   String ln = null;
   for (int i = 0; i < 10; ++i) {
      if (f.exists()) {
	 BufferedReader br = new BufferedReader(new FileReader(f));
	 ln = br.readLine();
	 br.close();
	 if (ln != null) break;
       }
      try {
	 Thread.sleep(1000);
       }
      catch (InterruptedException e) { }
    }
   StringTokenizer tok = new StringTokenizer(ln);
   String host = tok.nextToken();
   int port = Integer.parseInt(tok.nextToken());
   Socket s = new Socket(host,port);
   OutputStreamWriter w = new OutputStreamWriter(s.getOutputStream());
   w.write("<PING/>");
   w.flush();
   IvyXmlReader xr = new IvyXmlReader(s.getInputStream());
   String rslt = xr.readXml();
   Element r = IvyXml.convertStringToXml(rslt,true);
   if (!IvyXml.isElement(r,"RESULT")) {
      xr.close();
      s.close();
      throw new S6Exception("Error return: " + rslt);
   }
   System.err.println("CONNECT RESULT: " + rslt);
   xr.close();
   s.close();
}




public static void testFinder() throws S6Exception, IOException
{
   EngineFinderTester.testFinder();
}




/********************************************************************************/
/*										*/
/*	Code to run all S6 test cases						*/
/*										*/
/********************************************************************************/

private static void allTests()
{
   File dir = new File("/pro/s6/engine/src");
   File [] tests = dir.listFiles(new TestFilter());

   File f = new File(S6_ENGINE_SERVER_SOCKET);
   String ln = null;
   for (int i = 0; i < 10; ++i) {
      if (f.exists()) {
	 try {
	    BufferedReader br = new BufferedReader(new FileReader(f));
	    ln = br.readLine();
	    br.close();
	    if (ln != null) break;
	  }
	 catch (IOException e) { }
       }
      try {
	 Thread.sleep(1000);
       }
      catch (InterruptedException e) { }
    }
   if (ln == null) {
      System.err.println("SERVER NOT AVAILABLE");
      System.exit(1);
    }

   StringTokenizer tok = new StringTokenizer(ln);
   String host = tok.nextToken();
   int port = Integer.parseInt(tok.nextToken());

   Socket s = null;
   OutputStreamWriter w = null;
   IvyXmlReader xr = null;

   try {
      s = new Socket(host,port);
      w = new OutputStreamWriter(s.getOutputStream());
      xr = new IvyXmlReader(s.getInputStream());
    }
   catch (IOException e) {
      System.err.println("PROBLEM connecting to server: " + e);
      System.exit(1);
      return;
    }

   char [] buf = new char[8192];
   for (File t : tests) {
      try {
	 FileReader fr = new FileReader(t);
	 for ( ; ; ) {
	    int len = fr.read(buf);
	    if (len < 0) break;
	    w.write(buf,0,len);
	  }
	 w.flush();
	 fr.close();
	 String rslt = xr.readXml();
	 System.err.println("TEST " + t + ": " + rslt);
       }
      catch (IOException e) {
	 System.err.println("TESTER: Problem with " + t + ": " + e);
       }
    }

   try {
      if (xr != null) xr.close();
      if (s != null) s.close();
    }
   catch (IOException e) { }
}




private static class TestFilter implements FilenameFilter {

   public boolean accept(File dir,String name) {
      return name.endsWith(".s6");
    }

}	// end of inner class TestFilter



}	// end of class EngineTester




/* end of EngineTester.java */
