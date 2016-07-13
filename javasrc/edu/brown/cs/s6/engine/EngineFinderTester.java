/********************************************************************************/
/*										*/
/*		EngineEngineFinderTester.java					      */
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/engine/EngineFinderTester.java,v 1.5 2015/09/23 17:57:55 spr Exp $ */


/*********************************************************************************
 *
 * $Log: EngineFinderTester.java,v $
 * Revision 1.5  2015/09/23 17:57:55  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.4  2013-05-09 12:26:16  spr
 * Minor changes to start ui fixups.
 *
 * Revision 1.3  2009-05-12 22:27:23  spr
 * Add server to finder.  Fix up firewall recovery.  Fix max thread setting.
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


import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;

import junit.framework.TestCase;
import edu.brown.cs.ivy.xml.IvyXmlReader;
import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Exception;



public class EngineFinderTester extends TestCase implements S6Constants, EngineConstants {



/********************************************************************************/
/*										*/
/*	Main program								*/
/*										*/
/********************************************************************************/

public static void main(String [] args)
{
   if (args.length > 0) {
      create_server = false;
      port_number = Integer.parseInt(args[0]);
    }

   try {
      testFinder();
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

private static boolean	       create_server = true;
private static int	       port_number = 1091;


/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public EngineFinderTester()
{
}




/********************************************************************************/
/*										*/
/*	Load test case								*/
/*										*/
/********************************************************************************/

static final String [] FILES = new String[] {
   "SolarBaseObject", "SolarCommonObject", "SolarConstants", "SolarGravity",
   "SolarGroupObject", "SolarMain", "SolarModel", "SolarObject", "SolarRootObject",
   "SolarSystem", "SolarVector" };


public static void testFinder() throws S6Exception, IOException
{
   EngineFinder ef = null;

   if (create_server) {
      ef = new EngineFinder(new String []{"-port", Integer.toString(port_number) });
    }

   if (ef != null) ef.start();

   Socket s = new Socket(InetAddress.getLocalHost(),port_number);
   OutputStreamWriter w = new OutputStreamWriter(s.getOutputStream());
   IvyXmlReader xr = new IvyXmlReader(s.getInputStream());

   try {
      w.write("<Initialize><JavaVersion value='1.6' /></Initialize>\n");
      w.flush();
      String rslt = xr.readXml();
      System.err.println("Initialize = " + rslt);

      w.write("<LoadFile name='/pro/s6/public/Example.java' />\n");
      w.flush();
      rslt = xr.readXml();
      System.err.println("Load = " + rslt);

      w.write("<GetAllNames/>\n");
      w.flush();
      rslt = xr.readXml();
      System.err.println("List Names =\n" + rslt);

      w.write("<Exit/>\n");
      w.flush();
    }
   finally {
      xr.close();
      s.close();
    }

   if(ef != null) ef.start();
   s = new Socket(InetAddress.getLocalHost(),port_number);
   w = new OutputStreamWriter(s.getOutputStream());
   xr = new IvyXmlReader(s.getInputStream());

   try {
      File dir = new File("/home/spr/sampler/example2");
      String [] fils = dir.list();

      w.write("<Initialize><JavaVersion value='1.6' /></Initialize>\n");
      w.flush();
      String rslt = xr.readXml();
      System.err.println("Initialize = " + rslt);

      for (String fnm : fils) {
	 if (fnm.endsWith(".java")) {
	    w.write("<LoadFile name='/home/spr/sampler/example2/" + fnm + "' />\n");
	    w.flush();
	    rslt = xr.readXml();
	    System.err.println("Load " + fnm + " = " + rslt);
	  }
       }

      w.write("<GetAllNames/>\n");
      w.flush();
      rslt = xr.readXml();
      System.err.println("List Names =\n" + rslt);

      w.write("<Exit/>\n");
      w.flush();
    }
   finally {
      xr.close();
      s.close();
    }

   if (ef != null) ef.start();
   s = new Socket(InetAddress.getLocalHost(),port_number);
   w = new OutputStreamWriter(s.getOutputStream());
   xr = new IvyXmlReader(s.getInputStream());

   try {
      w.write("<Initialize><JavaVersion value='1.6' /></Initialize>\n");
      w.flush();
      String rslt = xr.readXml();
      System.err.println("Initialize = " + rslt);

      for (String fnm : FILES) {
	 w.write("<LoadFile name='/u/spr/solar/src/" + fnm + ".java' />\n");
	 w.flush();
	 rslt = xr.readXml();
	 System.err.println("Load " + fnm + " = " + rslt);
       }

      w.write("<FindFunctionDefinition><FileName value='/u/spr/solar/src/SolarGravity.java' /> ");
      w.write("<FileCharOffset start='4753' end='4782' /></FindFunctionDefinition>\n");
      w.flush();
      rslt = xr.readXml();
      System.err.println("Find def = " + rslt);

      w.write("<FindFunctionReferences><FileName value='/u/spr/solar/src/SolarGravity.java' /> ");
      w.write("<FileCharOffset start='4753' end='4782' /></FindFunctionReferences >\n");
      w.flush();
      rslt = xr.readXml();
      System.err.println("Find ref = " + rslt);

      w.write("<GetAllNames/>\n");
      w.flush();
      rslt = xr.readXml();
      System.err.println("List Names =\n" + rslt);

      w.write("<Exit/>\n");
      w.flush();
    }
   finally {
      xr.close();
      s.close();
    }

}




}	// end of class EngineFinderTester




/* end of EngineFinderTester.java */
