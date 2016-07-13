/********************************************************************************/
/*										*/
/*		TgenConstants.java						*/
/*										*/
/*	Constants for generating tests for test case generation 		*/
/*										*/
/********************************************************************************/
/*	Copyright 2013 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2013, Brown University, Providence, RI.				 *
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



package edu.brown.cs.s6.tgen;


interface TgenConstants
{


/********************************************************************************/
/*										*/
/*	Search Data								*/
/*										*/
/********************************************************************************/

String	SEARCH_FOR = "org.junit @Test assertEquals";
String [] OTHER_WORDS = { "assertEquals","algorithm","" };
double	USE_PROB = 1.0;
double	PAGE_PROB = 1.0;
int	MAX_PAGE = 1000;



/********************************************************************************/
/*										*/
/*	Caching data								*/
/*										*/
/********************************************************************************/

String CACHE_DIRECTORY = "/ws/volfred/s6/cache";
String CACHE_URL_FILE = "URL";
String CACHE_DATA_FILE = "DATA";


/********************************************************************************/
/*										*/
/*	Ouptut Data								*/
/*										*/
/********************************************************************************/

String TGEN_DIRECTORY = "/ws/volfred/tmp/s6tgen";
String TGEN_PACKAGE_PREFIX = "s6tgen_";
String TGEN_BINARY_DIR = "bin";
String TGEN_JUNIT_CLASSPATH = "/pro/s6/lib/junit.jar";
String TGEN_ANT_TEMPLATE = "/pro/s6/tgen/src/build.xml.proto";
String TGEN_ANT_COMMAND = "/pro/s6/bin/runant";

String TGEN_KEYWORDS = "/pro/s6/tgen/src/keywords.txt";



/********************************************************************************/
/*										*/
/*	Search terms								*/
/*										*/
/********************************************************************************/


}	// end of interface TgenConstants




/* end of TgenConstants.java */

