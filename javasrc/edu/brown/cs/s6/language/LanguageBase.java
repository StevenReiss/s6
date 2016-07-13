/********************************************************************************/
/*										*/
/*		LanguageBase.java						*/
/*										*/
/*	Basic implementation of a S6 language					*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/language/LanguageBase.java,v 1.11 2015/12/23 15:45:07 spr Exp $ */


/*********************************************************************************
 *
 * $Log: LanguageBase.java,v $
 * Revision 1.11  2015/12/23 15:45:07  spr
 * Minor fixes.
 *
 * Revision 1.10  2015/09/23 17:58:03  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.9  2013/09/13 20:32:51  spr
 * Add calls for UI search.
 *
 * Revision 1.8  2012-07-20 22:15:08  spr
 * Additions for UI search
 *
 * Revision 1.7  2012-06-11 14:07:31  spr
 * Code cleanup
 *
 * Revision 1.6  2009-05-12 22:28:44  spr
 * Fix ups to make user context work.
 *
 * Revision 1.5  2008-11-12 13:51:54  spr
 * Performance and bug updates.
 *
 * Revision 1.4  2008-08-28 00:32:53  spr
 * Next version of S6.	Lots of bug fixes, some new functionality.
 *
 * Revision 1.3  2008-07-17 13:46:42  spr
 * Bug fixes and speed ups.
 *
 * Revision 1.2  2008-06-12 17:47:51  spr
 * Next version of S6.
 *
 * Revision 1.1.1.1  2008-06-03 12:59:22  spr
 * Initial version of S6
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.language;


import java.util.Collection;
import java.util.Set;

import edu.brown.cs.ivy.xml.IvyXmlWriter;
import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Engine;
import edu.brown.cs.s6.common.S6Exception;
import edu.brown.cs.s6.common.S6FileLocation;
import edu.brown.cs.s6.common.S6Fragment;
import edu.brown.cs.s6.common.S6Language;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6Solution;
import edu.brown.cs.s6.common.S6SolutionSet;
import edu.brown.cs.s6.common.S6Source;
import edu.brown.cs.s6.language.java.LanguageJava;



public abstract class LanguageBase implements S6Language, S6Constants, LanguageConstants {



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

protected S6Engine for_engine;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

protected LanguageBase()
{
   for_engine = null;
}



/********************************************************************************/
/*										*/
/*	Static creation methods 						*/
/*										*/
/********************************************************************************/

public static S6Language createLanguage(S6Engine se,String name)
{
   LanguageBase lb = null;

   if (name == null || name.equalsIgnoreCase("Java")) {
      lb = new LanguageJava();
      lb.for_engine = se;
    }

   return lb;
}



/********************************************************************************/
/*										*/
/*	Transform methods							*/
/*										*/
/********************************************************************************/

@Override public boolean applyTransforms(S6SolutionSet solset,S6Solution sol,S6TransformType phase)
{
   LanguageController ctrl = new LanguageController(solset,sol,phase);
   return ctrl.applyTransforms();
}



/********************************************************************************/
/*										*/
/*	Language resolution methods						*/
/*										*/
/********************************************************************************/




public Collection<S6FileLocation> findAll(Iterable<S6Fragment> files,S6Fragment f,
					     int startoffset,int endoffset,boolean defs)
{
   return null;
}


public void listDefinitions(S6Fragment f,String file,IvyXmlWriter xw)	{ }



/********************************************************************************/
/*										*/
/*	Checking methods							*/
/*										*/
/********************************************************************************/

abstract public void checkInput(S6Request.Check req,IvyXmlWriter out) throws S6Exception;




/********************************************************************************/
/*										*/
/*	Formatting methods							*/
/*										*/
/********************************************************************************/

abstract public void formatCode(S6Request.Format req,IvyXmlWriter out) throws S6Exception;
abstract public void formatCode(S6Request.Search req,S6SolutionSet ss) throws S6Exception;



/********************************************************************************/
/*										*/
/*	Generic methods 							*/
/*										*/
/********************************************************************************/

abstract public S6Fragment createFileFragment(String text,S6Source src,S6Request.Search sr);
abstract public S6Fragment createPackageFragment(S6Request.Search sr);
abstract public Set<String> getRelatedProjects(S6Fragment src);
abstract public Set<String> getUsedProjects(S6Fragment src);




/********************************************************************************/
/*										*/
/*	Clean up methods							*/
/*										*/
/********************************************************************************/

public void finish(S6Request.Search rq) 				{ }




/********************************************************************************/
/*										*/
/*	Debugging methods							*/
/*										*/
/********************************************************************************/

public boolean doDebug()
{
   if (for_engine == null) return false;

   return for_engine.doDebug();
}



}	// end of class LanguageBase




/* end of LanguageBase.java */
