/********************************************************************************/
/*                                                                              */
/*              LanguageCoseFragment.java                                       */
/*                                                                              */
/*      Abstract fragment implementation using CoseResults                      */
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



package edu.brown.cs.s6.language;

import edu.brown.cs.s6.common.S6Fragment;
import edu.brown.cs.s6.common.S6Language;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6Solution;
import edu.brown.cs.s6.common.S6SolutionSet;
import edu.brown.cs.s6.common.S6TestResults;
import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Exception;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import edu.brown.cs.cose.cosecommon.CoseConstants;
import edu.brown.cs.cose.cosecommon.CoseResource;
import edu.brown.cs.cose.cosecommon.CoseResult;

abstract public class LanguageCoseFragment implements S6Fragment, S6Constants, LanguageConstants, CoseConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

protected LanguageCoseFragment  parent_fragment;
protected S6Language            language_base;
protected CoseResult            cose_result;
protected S6Request.Search      search_request;

private S6TestResults           test_results;
private boolean                 is_isolated;

private static Map<CoseResult,LanguageCoseFragment> lookup_map = new WeakHashMap<>();




/********************************************************************************/
/*                                                                              */
/*      Creation methods                                                        */
/*                                                                              */
/********************************************************************************/

public static LanguageCoseFragment findCoseFragment(S6Language lang,S6Request.Search rqst,CoseResult rslt)
{
   synchronized (lookup_map) {
      LanguageCoseFragment lcf = lookup_map.get(rslt);
      if (lcf == null) {
         lcf = (LanguageCoseFragment) lang.createCoseFragment(rslt,rqst);
         lookup_map.put(rslt,lcf);
       }
      return lcf;
    }
}



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

protected LanguageCoseFragment(S6Language lang,S6Request.Search rqst,CoseResult rslt)
{
   language_base = lang;
   search_request = rqst;
   cose_result = rslt;
   parent_fragment = null;
   test_results = null;
   is_isolated = false;
   if (rslt.getParent() != null) {
      parent_fragment = findCoseFragment(lang,rqst,rslt.getParent());
    }
}


protected LanguageCoseFragment(CoseResult rslt) throws S6Exception
{
   if (rslt.getParent() == null) throw new S6Exception("Can't create root fragment");
   parent_fragment = findCoseFragment(null,null,rslt.getParent());
   language_base = parent_fragment.language_base;
   search_request = parent_fragment.search_request;
   cose_result = rslt;
   if (language_base == null || search_request == null) throw new S6Exception("Can't create root fragment");
   test_results = null;
   is_isolated = false;
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

@Override public void addResource(CoseResource rsrc)
{
   throw new IllegalArgumentException("Can't add resource to cose fragment");
}

@Override public Collection<CoseResource> getResources()
{
   return cose_result.getResources();
}


public Collection<S6Fragment> getFragments(S6SearchType typ)
{
   throw new IllegalArgumentException("getFragments not needed for COSE");
}

@Override public Collection<S6Fragment> getFileFragments() 
{
   Collection<CoseResult> inner = cose_result.getInnerResults();
   if (inner == null) return null;
   
   List<S6Fragment> rslt = new ArrayList<>();
   for (CoseResult cr : inner) {
      S6Fragment sf = findCoseFragment(language_base,search_request,cr);
      rslt.add(sf);
    }
   return rslt;
}




@Override public Collection<String> getPackages()              
{
   return cose_result.getPackages();
}

@Override public String getBasePackage()  
{
   return cose_result.getBasePackage();
}


@Override public String getText()
{
   return cose_result.getText();
}


@Override public String getKeyText()
{
   return cose_result.getKeyText();
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




@Override public boolean addPackage(String pkg)                
{
   throw new IllegalArgumentException("Can't add package to cose fragment"); 
}

@Override public void addInnerFragment(S6Fragment f)	
{
   throw new IllegalArgumentException("Can't add inner fragment to cose fragment"); 
}

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

@Override public CoseResultType getFragmentType()
{
   return cose_result.getResultType();
}











}       // end of class LanguageCoseFragment




/* end of LanguageCoseFragment.java */

