/********************************************************************************/
/*										*/
/*		S6Request.java							*/
/*										*/
/*	Global representation of a user request for a code fragment		*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/common/S6Request.java,v 1.12 2015/09/23 17:57:47 spr Exp $ */


/*********************************************************************************
 *
 * $Log: S6Request.java,v $
 * Revision 1.12  2015/09/23 17:57:47  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.11  2014/08/29 15:15:46  spr
 * Updates for suise, testcases.
 *
 * Revision 1.10  2013/09/13 20:31:52  spr
 * Add UI search.
 *
 * Revision 1.9  2012-07-20 22:14:48  spr
 * Additions for UI search
 *
 * Revision 1.8  2012-06-20 12:21:19  spr
 * Initial fixes for UI search
 *
 * Revision 1.7  2012-06-11 18:18:20  spr
 * Include changed/new files for package/ui search
 *
 * Revision 1.6  2012-06-11 14:07:27  spr
 * Code cleanup
 *
 * Revision 1.5  2008-11-12 13:50:47  spr
 * Performance and bug updates.
 *
 * Revision 1.4  2008-08-28 00:32:45  spr
 * Next version of S6.	Lots of bug fixes, some new functionality.
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


import java.util.Collection;
import java.util.List;

import org.w3c.dom.Element;


import java.util.Set;

public interface S6Request extends S6Constants {


public S6Engine getEngine();

public Signature getSignature();

public Context getUserContext();

public String getPackage();

public boolean useAndroid();


public boolean doDebug();



interface Search extends S6Request {
   public S6SearchType getSearchType();
   
   public S6ScopeType getScopeType();

   public Collection<KeywordSet> getKeywordSets();

   public Tests getTests();

   public Set<S6Location> getLocations();

   public boolean useSource(String source);

   public Iterable<String> getSpecificSources();

   public S6Security getSecurity();

   public S6Contracts getContracts();

   public S6FormatType getFormatType();
}


interface Check extends S6Request {
   public S6CheckType getCheckType();
   public String getUserInput(String what);
   public Iterable<Element> getUserElements(String what);
}


interface Format extends S6Request {
   S6SearchType getSearchType();
   S6FormatType getFormatType();
   public Iterable<FormatItem> getItems();
}



interface Signature {
   String getName();
   MethodSignature getMethod(String name);
   MethodSignature getMethodSignature();
   ClassSignature getClassSignature();
   String getPackage();
}


interface PackageSignature extends Signature {
   List<ClassSignature> getClasses();
}


interface ClassSignature extends Signature {
   List<MethodSignature> getMethods();
   List<FieldSignature> getFields();
   boolean isInterface();
   boolean isMain();
   String getSuperClass();
   List<String> getInterfaces();
   List<String> getKeywords();
   boolean includeTestCases();
}


interface UIComponent {
   String getId();
   List<String> getTypes();
   double getXposition();
   double getYposition();
   double getWidth();
   double getHeight();
   UIComponent getTopAnchor();
   UIComponent getBottomAnchor();
   UIComponent getLeftAnchor();
   UIComponent getRightAnchor();
   String getData();

   List<UIComponent> getChildren();
}



interface UISignature extends PackageSignature {
   UIComponent getHierarchy();
}

interface TestingSignature extends Signature {
   
   ClassSignature getClassToTest();
   String getPackage();
   
}

interface MethodSignature extends Signature {
   String getReturnTypeName();
   List<String> getParameterTypeNames();
   List<String> getExceptionTypeNames();
   List<String> getParameterNames();
   boolean isStatic();
   boolean isAbstract();
   String getDeclarationText();
   boolean useExceptions();
}


interface FieldSignature extends Signature {
   ClassSignature getDefiningClass();
   String getTypeName();
   boolean isStatic();
}


interface Tests {
   Iterable<String> getImportTypes();
   Iterable<S6TestCase> getTestCases();
}


interface KeywordSet {
   List<String> getWords();
   S6SearchLanguage getLanguage();
}


interface FormatItem {
   String getId();
   String getText();
}



interface Context {
   String getContextFile();
}


}	// end of interface S6Request



/* end of S6Request.java */


