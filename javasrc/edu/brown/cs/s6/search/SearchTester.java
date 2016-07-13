/********************************************************************************/
/*										*/
/*		SearchTester.java						*/
/*										*/
/*	Junit tests for S6 search interface to Lucene				*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/search/SearchTester.java,v 1.3 2015/09/23 17:58:11 spr Exp $ */


/*********************************************************************************
 *
 * $Log: SearchTester.java,v $
 * Revision 1.3  2015/09/23 17:58:11  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.2  2008-06-12 17:47:55  spr
 * Next version of S6.
 *
 * Revision 1.1.1.1  2008-06-03 12:59:22  spr
 * Initial version of S6
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.search;


import java.io.FileReader;
import java.io.IOException;

import junit.framework.TestCase;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.Tokenizer;



public class SearchTester extends TestCase implements SearchConstants {



/********************************************************************************/
/*										*/
/*	Main program								*/
/*										*/
/********************************************************************************/

public static void main(String [] args)
{
   try {
      testSimple();
    }
   catch (Throwable t) {
      System.err.println("ERROR: " + t);
      t.printStackTrace();
    }

   System.exit(0);
}




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public SearchTester()
{ }



/********************************************************************************/
/*										*/
/*	Simple test case							*/
/*										*/
/********************************************************************************/

public static void testSimple()
{
   try {
      FileReader fr = new FileReader("/pro/s6/search/src/SearchJava.java");
      SearchJava sj = new SearchJava();
      Tokenizer tok = sj.createTokenizer(fr);

      for ( ; ; ) {
	 Token t = tok.next();
	 if (t == null) break;
	 System.err.println("FOUND : " + t);
       }

      fr.close();
    }
   catch (IOException e) {
      System.err.println("S6: TEST: Problem reading file: " + e);
    }
}





}	// end of SearchTester.java





/* end of SearchTester.java */
