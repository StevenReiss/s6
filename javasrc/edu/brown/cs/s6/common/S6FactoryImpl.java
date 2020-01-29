/********************************************************************************/
/*										*/
/*		S6FactoryImpl.java						*/
/*										*/
/*	S6 factory implementaton						*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/common/S6FactoryImpl.java,v 1.5 2016/07/18 23:04:43 spr Exp $ */


/*********************************************************************************
 *
 * $Log: S6FactoryImpl.java,v $
 * Revision 1.5  2016/07/18 23:04:43  spr
 * Updates for cleanup.
 *
 * Revision 1.4  2015/09/23 17:57:47  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.3  2013/09/13 20:31:52  spr
 * Add UI search.
 *
 * Revision 1.2  2008-08-28 00:32:45  spr
 * Next version of S6.  Lots of bug fixes, some new functionality.
 *
 * Revision 1.1.1.1  2008-06-03 12:59:21  spr
 * Initial version of S6
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.common;


import org.w3c.dom.Element;



class S6FactoryImpl extends S6Factory implements S6Constants {


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private static S6FactoryImpl   default_factory = null;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

private S6FactoryImpl()
{ }



/********************************************************************************/
/*										*/
/*	Singleton creation methods						*/
/*										*/
/********************************************************************************/

synchronized static S6Factory getDefaultFactory()
{
   if (default_factory == null) {
      default_factory = new S6FactoryImpl();
    }

   return default_factory;
}



/********************************************************************************/
/*										*/
/*	Request factory methods 						*/
/*										*/
/********************************************************************************/

public S6Request.Search createSearchRequest(S6Engine eng,Element xml) throws S6Exception
{
   return new edu.brown.cs.s6.request.RequestCose(eng,xml);
}



public S6Request.Check createCheckRequest(S6Engine eng,Element xml) throws S6Exception
{
   return new edu.brown.cs.s6.request.RequestCheck(eng,xml);
}



public S6Request.Format createFormatRequest(S6Engine eng,Element xml) throws S6Exception
{
   return new edu.brown.cs.s6.request.RequestFormat(eng,xml);
}



/********************************************************************************/
/*										*/
/*	Keyword search factory methods						*/
/*										*/
/********************************************************************************/

public void getInitialSolutions(S6SolutionSet ss) throws S6Exception
{
   if (ss.getRequest() instanceof S6KeySearch) {
      S6KeySearch cose = (S6KeySearch) ss.getRequest();
      cose.getInitialSolutions(ss);
    }
}



/********************************************************************************/
/*										*/
/*	Solution creation methods						*/
/*										*/
/********************************************************************************/

public S6SolutionSet createSolutionSet(S6Request.Search r)
{
   return new edu.brown.cs.s6.solution.SolutionSet(r);
}



/********************************************************************************/
/*										*/
/*	Language creation methods						*/
/*										*/
/********************************************************************************/

public S6Language createLanguage(S6Engine se,String name)
{
   return edu.brown.cs.s6.language.LanguageBase.createLanguage(se,name);
}



/********************************************************************************/
/*										*/
/*	License manager creation methods					*/
/*										*/
/********************************************************************************/

public S6License createLicenseManager()
{
   return edu.brown.cs.s6.license.LicenseManager.getLicenseManager();
}





}	// end of class S6FactoryImpl



/* end of S6FactoryImpl.java */
































































































































































































































































































































































































