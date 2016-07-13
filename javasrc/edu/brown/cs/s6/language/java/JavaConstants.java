/********************************************************************************/
/*										*/
/*		JavaConstants.java						*/
/*										*/
/*	Constants for Java implementation for S6				*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/language/java/JavaConstants.java,v 1.9 2015/12/23 15:45:09 spr Exp $ */


/*********************************************************************************
 *
 * $Log: JavaConstants.java,v $
 * Revision 1.9  2015/12/23 15:45:09  spr
 * Minor fixes.
 *
 * Revision 1.8  2015/09/23 17:54:52  spr
 * Version to handle andriod UI applications.
 *
 * Revision 1.7  2013/09/13 20:33:03  spr
 * Add calls for UI search.
 *
 * Revision 1.6  2009-05-12 22:28:48  spr
 * Fix ups to make user context work.
 *
 * Revision 1.5  2008-08-28 00:32:56  spr
 * Next version of S6.	Lots of bug fixes, some new functionality.
 *
 * Revision 1.4  2008-07-18 22:27:09  spr
 * Handle remove compilation calls; update transforms to include code to use ASTrewrite.
 *
 * Revision 1.3  2008-07-17 13:46:45  spr
 * Bug fixes and speed ups.
 *
 * Revision 1.2  2008-06-12 17:47:52  spr
 * Next version of S6.
 *
 * Revision 1.1.1.1  2008-06-03 12:59:22  spr
 * Initial version of S6
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.language.java;


import java.io.File;

import edu.brown.cs.ivy.file.IvyFile;
import edu.brown.cs.s6.language.LanguageConstants;



interface JavaConstants extends LanguageConstants {



/********************************************************************************/
/*										*/
/*	Java attribute names							*/
/*										*/
/********************************************************************************/

String PROP_S6_JAVA_SOURCE = "S6$Source";
String PROP_S6_JAVA_REQUEST = "S6$Request";
String PROP_S6_JAVA_CONTEXT = "S6$Context";




/********************************************************************************/
/*										*/
/*	Special Type names							*/
/*										*/
/********************************************************************************/

String TYPE_ANY_CLASS = "*ANY*";
String TYPE_ERROR = "*ERROR*";


/********************************************************************************/
/*										*/
/*	Rename names								*/
/*										*/
/********************************************************************************/

String S6_NAME_PREFIX = "S6NameChange_";
String S6_CHUNK_PREFIX = "S6ChunkFunction_";



/********************************************************************************/
/*										*/
/*	Path Names								*/
/*										*/
/********************************************************************************/

String S6_PACKAGE_PREFIX = "s6test_";
String S6_TEST_CLASS = "S6TestClass";
String S6_USER_CLASS = "S6UserClass";
String ANT_FILE = "build.xml";
String S6_BINARY_DIR = "bin";

String S6_CONTRACT_DIR = "s6contract";
String S6_CONTRACT_PREFIX = "s6contract_";
String JML_CHECK_COMMAND = IvyFile.expandName("$(ROOT)/s6/public/bin/jmlc --generic --nowrite --Quiet ");

String ANT_COMMAND = IvyFile.expandName("$(ROOT)/s6/bin/runant");

String JAVA_TEST_PROTO = IvyFile.expandName("$(ROOT)/s6/lib/JavaMethodTest.proto");
String JAVA_ANT_PROTO = IvyFile.expandName("$(ROOT)/s6/lib/build.xml.proto");
String JAVA_ANDROID_ANT_PROTO = IvyFile.expandName("$(ROOT)/s6/lib/android.build.xml.proto");
String JUNIT_CLASSPATH = IvyFile.expandName("$(ROOT)/s6/lib/junit.jar");
String JUNIT_RUNNER = "junit.textui.TestRunner";
String JUNIT_OUT = "test.out.xml";
String S6_RUNNER_JAR = IvyFile.expandName("$(ROOT)/s6/lib/runner.jar");

String S6_SECURITY_POLICY = "s6_Test_Policy";
String S6_SECURITY_PERMITS = "s6_Test_Permissions";
String S6_SECURITY_POLICY_CLASS = "edu.brown.cs.s6.slim.SlimSecurityPolicy";
String S6_SECURITY_PERMIT_CLASS = "edu.brown.cs.s6.slim.SlimPermission";
String S6_SECURITY_PERMIT_FILE = "edu.brown.cs.s6.slim.SlimFilePermission";
String S6_SECURITY_PERMIT_SOCKET = "edu.brown.cs.s6.slim.SlimSocketPermission";
String S6_SECURITY_PERMIT_AWT = "edu.brown.cs.s6.slim.SlimAwtPermission";
String S6_SECURITY_PERMIT_PROPERTY = "edu.brown.cs.s6.slim.SlimPropertyPermission";
String S6_SECURITY_PERMIT_RUNTIME = "edu.brown.cs.s6.slim.SlimRuntimePermission";

String CTX_LIBRARY = "$(S6LIB)/$(ARCH)/librunctx.$(SHARED_EXT)";




/********************************************************************************/
/*										*/
/*	Formatting constants							*/
/*										*/
/********************************************************************************/

String S6_FORMAT_HOST = "localhost";
int    S6_FORMAT_PORT_BROWN = 17551;
int    S6_FORMAT_PORT_ECLIPSE = 17552;
int    S6_FORMAT_PORT_SUN = 17553;

File S6_FORMAT_DIR = new File(IvyFile.expandName("$(ROOT)/s6/tmp"));



}	// end of interface JavaConstants



/* end of JavaConstants.java */
