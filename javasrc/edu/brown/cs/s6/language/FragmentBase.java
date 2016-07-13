/********************************************************************************/
/*										*/
/*		FragmentBase.java						*/
/*										*/
/*	Basic implementation of generic code fragment				*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/language/FragmentBase.java,v 1.6 2015/09/23 17:58:03 spr Exp $ */


/*********************************************************************************
 *
 * $Log: FragmentBase.java,v $
 * Revision 1.6  2015/09/23 17:58:03  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.5  2012-08-13 16:51:38  spr
 * Add solution to fixDependencies
 *
 * Revision 1.4  2012-06-11 14:07:31  spr
 * Code cleanup
 *
 * Revision 1.3  2009-09-18 01:41:33  spr
 * Handle user testing.
 *
 * Revision 1.2  2008-08-28 00:32:53  spr
 * Next version of S6.	Lots of bug fixes, some new functionality.
 *
 * Revision 1.1.1.1  2008-06-03 12:59:22  spr
 * Initial version of S6
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.language;


import java.util.Collection;

import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Fragment;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6Solution;
import edu.brown.cs.s6.common.S6SolutionSet;
import edu.brown.cs.s6.common.S6TestResults;


import edu.brown.cs.s6.common.S6Resource;
import java.util.HashSet;

abstract public class FragmentBase implements S6Fragment, S6Constants, LanguageConstants {



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

protected FragmentBase          parent_fragment;
protected LanguageBase          language_base;

private S6TestResults           test_results;
private S6Request.Search        search_request;
private boolean                 is_isolated;
private Collection<S6Resource> resource_set;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

protected FragmentBase(LanguageBase lang,S6Request.Search rqst)
{ 
   language_base = lang;
   search_request = rqst;
   parent_fragment = null;
   test_results = null;
   is_isolated = false;
   resource_set = null;
}



protected FragmentBase(FragmentBase parent)
{
   this(parent.language_base,parent.search_request);
   parent_fragment = parent;
}



/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

@Override public S6Request.Search getSearchRequest()
{
   return search_request;
}


protected void setIsolated(boolean fg)		{ is_isolated = fg; }
protected boolean isIsolated()			{ return is_isolated; }

@Override public S6TestResults getTestResults() { return test_results; }

protected void setTestResults(S6TestResults trslt)
{
   test_results = trslt;
}

@Override public void addResource(S6Resource rsrc)
{
   if (resource_set == null) resource_set = new HashSet<S6Resource>();
   resource_set.add(rsrc);
}

@Override public Collection<S6Resource> getResources()
{
   Collection<S6Resource> rslt = null;
   if (parent_fragment != null) rslt = parent_fragment.getResources();
   if (resource_set != null) {
      if (rslt == null) rslt = new HashSet<S6Resource>();
      rslt.addAll(resource_set);
    }
   return rslt;
}



/********************************************************************************/
/*                                                                              */
/*      Overridable work methods                                                */
/*                                                                              */
/********************************************************************************/

@Override public boolean checkInitial(S6Request.Signature sg)
{
   return true;
}


@Override public boolean checkSignature(S6Request.Signature sgn,S6SignatureType type)
{
   return false;
}




/********************************************************************************/
/*                                                                              */
/*      Checking methods                                                        */
/*                                                                              */
/********************************************************************************/

public boolean checkSignature(S6Request.Signature r)
{
   return checkSignature(r,S6SignatureType.FULL);
}

public boolean checkName(S6Request.Signature r)
{
   return checkSignature(r,S6SignatureType.NAME);
}

public boolean checkParameters(S6Request.Signature r)
{
   return checkSignature(r,S6SignatureType.PARAMETERS);
}
public boolean checkExceptions(S6Request.Signature r)
{
   return checkSignature(r,S6SignatureType.EXCEPTIONS);
}
public boolean checkReturn(S6Request.Signature r)
{
   return checkSignature(r,S6SignatureType.RETURN);
}

public S6SolutionFlag checkTestCases(S6Request.Search r)
{
   return S6SolutionFlag.PASS;
}



public abstract Collection<S6Fragment> getFragments(S6SearchType typ);

@Override public Collection<S6Fragment> getFileFragments()      { return null; }
@Override public Collection<String> getPackages()               { return null; }
@Override public String getBasePackage()                        { return null; }
@Override public boolean addPackage(String pkg)                 { return false; }

@Override public void addInnerFragment(S6Fragment f)		{ }

@Override public boolean fixDependencies(S6SolutionSet ss,S6Solution sol)
{
   return false; 
}



/********************************************************************************/
/*										*/
/*	Abstract methods							*/
/*										*/
/********************************************************************************/

public abstract void makeLocal(S6SolutionSet ss);

public abstract void resolveFragment();

public abstract int getCodeComplexity();

public abstract S6FragmentType getFragmentType();

public abstract String getText();

public abstract String getKeyText();



}	// end of class FragmentBase




/* end of FragmentBase.java */



