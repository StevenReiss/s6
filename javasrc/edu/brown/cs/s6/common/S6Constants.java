/********************************************************************************/
/*										*/
/*		S6Constants.java						*/
/*										*/
/*	Global Constants for S6 search engine					*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/common/S6Constants.java,v 1.19 2016/07/18 23:04:43 spr Exp $ */


/*********************************************************************************
 *
 * $Log: S6Constants.java,v $
 * Revision 1.19  2016/07/18 23:04:43  spr
 * Updates for cleanup.
 *
 * Revision 1.18  2015/12/23 15:44:32  spr
 * Add S6Resource.java, minor fixes.
 *
 * Revision 1.17  2015/09/23 17:57:46  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.16  2015/02/14 19:40:02  spr
 * Add test case generation.
 *
 * Revision 1.15  2014/08/29 15:15:46  spr
 * Updates for suise, testcases.
 *
 * Revision 1.14  2013/09/13 20:31:52  spr
 * Add UI search.
 *
 * Revision 1.13  2013-05-09 12:26:12  spr
 * Minor changes to start ui fixups.
 *
 * Revision 1.12  2012-07-20 22:14:48  spr
 * Additions for UI search
 *
 * Revision 1.11  2012-06-20 12:21:19  spr
 * Initial fixes for UI search
 *
 * Revision 1.10  2012-06-11 18:18:20  spr
 * Include changed/new files for package/ui search
 *
 * Revision 1.9  2012-06-11 14:07:27  spr
 * Code cleanup
 *
 * Revision 1.8  2009-09-18 01:40:29  spr
 * Add full class option; add support for user interaction.
 *
 * Revision 1.7  2009-05-12 22:26:06  spr
 * Editing fixups.
 *
 * Revision 1.6  2008-11-12 13:50:47  spr
 * Performance and bug updates.
 *
 * Revision 1.5  2008-08-28 00:32:45  spr
 * Next version of S6.	Lots of bug fixes, some new functionality.
 *
 * Revision 1.4  2008-07-18 22:25:58  spr
 * Update flags, minor bug fixes and extensions.
 *
 * Revision 1.3  2008-07-17 13:45:51  spr
 * Various bug fixes and clean ups.
 *
 * Revision 1.2  2008-06-12 17:47:46  spr
 * Next version of S6.
 *
 * Revision 1.1.1.1  2008-06-03 12:59:21  spr
 * Initial version of S6
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.common;


import edu.brown.cs.ivy.file.IvyFile;




public interface S6Constants {


/********************************************************************************/
/*										*/
/*	XML definitions 							*/
/*										*/
/********************************************************************************/

// String S6_NAMESPACE = "http://www.cs.brown.edu/S6";



/********************************************************************************/
/*										*/
/*	File locations								*/
/*										*/
/********************************************************************************/

String S6_ENGINE_SERVER_SOCKET = IvyFile.expandName("$(S6)/tmp/s6_engine_socket");
String S6_ENGINE_SERVER_SOCKET_WEB = "scp " + S6_ENGINE_SERVER_SOCKET +
	" conifer:/vol/web/html/s6/data/.s6_engine_socket";
String S6_ENGINE_WEB_SERVER = "conifer.cs.brown.edu";
int S6_ENGINE_WEB_PORT = 17240;
int S6_ENGINE_WEB_CLIENT_PORT = 17241;

String S6_TEST_DIR = "s6test";
String S6_CONTEXT_DIR = "s6context";
String S6_CONTEXT_PREFIX = "s6context_";
String S6_CONTEXT_FILE = "S6.CONTEXT";
String S6_CONTEXT_SOURCE = "S6.SOURCE";

String S6_FILE_DIRECTORY = IvyFile.expandName("$(S6)/tmp/files");

String S6_TRANSFORM_DATA = IvyFile.expandName("$(S6)/lib/transformdata.xml");




/********************************************************************************/
/*										*/
/*	Limitations								*/
/*										*/
/********************************************************************************/

int	S6_MAX_SOURCES = 500;
int	S6_MAX_INITIAL = 2000;
int	S6_MAX_ACTIVE = 1000;
int	S6_MAX_TEST = 500;
int	S6_MAX_SOLUTIONS = 100000;

int	S6_MIN_SOLUTION = 5;

int	S6_MAX_SIZE = 6000000;



/********************************************************************************/
/*										*/
/*	Basic enumerations							*/
/*										*/
/********************************************************************************/

enum S6SearchType {
   METHOD,
   CLASS,
   FULLCLASS,
   PACKAGE,
   UIFRAMEWORK,
   ANDROIDUI,
   TESTCASES,
   APPLICATION,
};



enum S6FragmentType {
   FILE,
   CLASS,
   METHOD,
   PACKAGE
};



enum S6ScopeType {
   FILE,		// only look at given file
   PACKAGE,		// look at all files in package of given file
   PACKAGE_UI,          // all files in package relevant to the UI
   SYSTEM		// consider all files in package and related packages
}

enum S6TestType {
   USERCODE,		// user provides a complete test case
   CALLS,		// one or more calls
   JUNIT,		// junit testcase from user context
};


enum S6SearchLanguage {
   JAVA,                // java files
   XML                  // xml (resource, manifest) files
}



enum S6TestArgType {
   LITERAL,		// value should match literal
   STRING,		// value should match string
   VARIABLE,		// value should match contents of variable
   SAVE 		// store value in variable, no checking
};



enum S6Location {
   LOCAL,
   KODERS,
   KRUGLE,
   GOOGLE,
   SOURCERER,
   OHLOH,
   GITHUB,
   GOOGIT,
   OPENHUB,
   HUNTER,
   CODEEX,
   GITZIP,
};



enum S6TransformType {
   INITIAL,		// must transforms done before all others
   NORMAL,		// transforms applied to all items before adding dependencies
   DETAIL,		// transforms applied to composite fragments w/ dependencies
   COMPILE,		// transforms applied if compilation fails ???
   TEST,		// transforms applied if testing fails	   ???
   FINAL,		// transforms to apply at the end
};




class S6SignatureType {


   static private final int NAME_FG = 1;
   static private final int PARAMETERS_FG = 2;
   static private final int EXCEPTIONS_FG = 4;
   static private final int RETURN_FG = 8;
   static private final int MODS_FG = 16;
   static private final int NO_METHODS_FG = 32;
   static private final int NO_CLASSES_FG = 64;
   static private final int EXTRACTABLE_FG = 128;
   static private final int USAGE_FG = 256;
   static private final int TYPE_FG = 512;
   static private final int UITYPES_FG = 1024;
   static private final int DOES_TEST_FG = 2048;
   
   static public final S6SignatureType FULL = new S6SignatureType(
         NAME_FG|PARAMETERS_FG|EXCEPTIONS_FG|RETURN_FG|MODS_FG|TYPE_FG);
   static public final S6SignatureType ALL = new S6SignatureType(
         NAME_FG|PARAMETERS_FG|EXCEPTIONS_FG|RETURN_FG);
   static public final S6SignatureType NAME = new S6SignatureType(NAME_FG);
   static public final S6SignatureType RETURN = new S6SignatureType(RETURN_FG);
   static public final S6SignatureType PARAMETERS = new S6SignatureType(PARAMETERS_FG);
   static public final S6SignatureType EXCEPTIONS = new S6SignatureType(EXCEPTIONS_FG);
   static public final S6SignatureType MODS = new S6SignatureType(MODS_FG);
   static public final S6SignatureType RETURN_EXCEPTIONS_PARAMETERS = new S6SignatureType(
         RETURN_FG|EXCEPTIONS_FG|PARAMETERS_FG);
   static public final S6SignatureType RETURN_EXCEPTIONS = new S6SignatureType(
         RETURN_FG|EXCEPTIONS_FG);
   static public final S6SignatureType RETURN_MODS = new S6SignatureType(
         RETURN_FG|MODS_FG);
   static public final S6SignatureType NO_CLASSES = new S6SignatureType(NO_CLASSES_FG);
   static public final S6SignatureType NO_METHODS = new S6SignatureType(NO_METHODS_FG);
   static public final S6SignatureType EXTRACTABLE = new S6SignatureType(EXTRACTABLE_FG);
   static public final S6SignatureType PACKAGE_FULL = new S6SignatureType(
         NAME_FG|PARAMETERS_FG|EXCEPTIONS_FG|RETURN_FG|MODS_FG|TYPE_FG|USAGE_FG);
   static public final S6SignatureType UIFRAMEWORK_FULL = new S6SignatureType(
         NAME_FG|PARAMETERS_FG|EXCEPTIONS_FG|RETURN_FG|MODS_FG|TYPE_FG|UITYPES_FG);
   static public final S6SignatureType TESTING_FULL = new S6SignatureType(
         NAME_FG|PARAMETERS_FG|EXCEPTIONS_FG|RETURN_FG|MODS_FG|TYPE_FG|DOES_TEST_FG);
   static public final S6SignatureType DOES_TEST = new S6SignatureType(DOES_TEST_FG);
   
   private int signature_flags;

   private S6SignatureType(int fgs) {
      signature_flags = fgs;
    }

   public boolean checkName()		{ return (signature_flags & NAME_FG) != 0; }
   public boolean checkParameters()	{ return (signature_flags & PARAMETERS_FG) != 0; }
   public boolean checkExceptions()	{ return (signature_flags & EXCEPTIONS_FG) != 0; }
   public boolean checkReturn() 	{ return (signature_flags & RETURN_FG) != 0; }
   public boolean checkType()		{ return (signature_flags & TYPE_FG) != 0; }
   public boolean checkMods()		{ return (signature_flags & MODS_FG) != 0; }
   public boolean noClasses()		{ return (signature_flags & NO_CLASSES_FG) != 0; }
   public boolean noMethods()		{ return (signature_flags & NO_METHODS_FG) != 0; }
   public boolean checkUsage()		{ return (signature_flags & USAGE_FG) != 0; }
   public boolean checkExtractable()	{ return (signature_flags & EXTRACTABLE_FG) != 0; }
   public boolean checkUITypes()	{ return (signature_flags & UITYPES_FG) != 0; }
   public boolean checkDoesTest()	{ return (signature_flags & DOES_TEST_FG) != 0; }
   public boolean ignoreSuper() {
      return (signature_flags & (NO_CLASSES_FG|NO_METHODS_FG|EXTRACTABLE_FG)) != 0;
    }

}




enum S6CheckType {
   NONE,
   METHOD,		// method signature
   CALLTEST,		// single call test
   TESTS,		// all tests for a method or class
   METHODTESTS, 	// all tests for a method
   CLASSTESTS,		// all tests for a class
   SECURITY,		// security specifications
   CONTRACTS,		// contract specifications
   CONTEXT,		// context information
   CLASS,		// class name
   CLASSLIST,		// list of class names
   LICENSE		// license for an id
};


enum S6SolutionFlag {
   KEEP,		// keep this solution around
   DONE_TRANSFORM,	// transforms done on solution
   DEPEND_FAIL, 	// the solution failed dependencies
   DEPEND_PASS, 	// the solution passed dependencies
   FAIL,		// the solution failed tests
   USER,		// show user image or value and ask if ok
   PASS,		// the solution passed tests
   COMPILES,		// no undefined symbols
   REMOVE,		// force removal of the solution
   NONE                 // dummy flag
};


enum S6TestOp {        // should match SviwebConstants.TestOp
   NONE,
   EQL,
   NEQ,
   SAVE,
   IGNORE,
   THROW,
   SAME,
   DIFF,
   SHOW,
   INTERACT,
   HIERARCHY,
   SCOREHIER
};


enum S6SecurityType {
   NONE,
   FILE,			// FilePermission
   SOCKET,			// SocketPermission
   AWT, 			// AwtPermission
   PROPERTY,			// PropertyPermission
   RUNTIME			// RuntimePermission
}




enum S6ContractType {
   NONE,
   REQUIRE,
   ENSURE
}



enum S6FormatType {
   NONE,
   BROWN,			// SPR
   ECLIPSE,
   SUN,
   SELF
}



enum S6TaskType {
   COMPUTE,				// computational task
   EXEC,				// exec a separate process
   IO					// wait for IO
}



enum S6UserFileType {
   READ,
   WRITE,
   DIRECTORY
}



}	// end of interface S6Constants



/* end of S6Constants.java */
