/********************************************************************************/
/*										*/
/*		S6Language.java 						*/
/*										*/
/*	Public interface for Language dependencies				*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/common/S6Language.java,v 1.10 2015/12/23 15:44:32 spr Exp $ */


/*********************************************************************************
 *
 * $Log: S6Language.java,v $
 * Revision 1.10  2015/12/23 15:44:32  spr
 * Add S6Resource.java, minor fixes.
 *
 * Revision 1.9  2015/09/23 17:57:47  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.8  2012-07-20 22:14:48  spr
 * Additions for UI search
 *
 * Revision 1.7  2012-06-11 14:07:27  spr
 * Code cleanup
 *
 * Revision 1.6  2009-05-12 22:26:06  spr
 * Editing fixups.
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
import java.util.Set;

import edu.brown.cs.ivy.xml.IvyXmlWriter;



public interface S6Language extends S6Constants {


boolean applyTransforms(S6SolutionSet solset,S6Solution sol,S6TransformType phase);


S6Fragment createFileFragment(String text,S6Source src,S6Request.Search sr);
S6Fragment createPackageFragment(S6Request.Search sr);
Set<String> getRelatedProjects(S6Fragment src);
Set<String> getUsedProjects(S6Fragment src);

void setProject(String project);
void setPath(String path);



Collection<S6FileLocation> findAll(Iterable<S6Fragment> files,S6Fragment f,int start,int end,boolean defsonly);
void listDefinitions(S6Fragment f,String file,IvyXmlWriter xw);

void checkInput(S6Request.Check rq,IvyXmlWriter xw) throws S6Exception;

void formatCode(S6Request.Format rq,IvyXmlWriter xw) throws S6Exception;
void formatCode(S6Request.Search rq,S6SolutionSet ss) throws S6Exception;

void finish(S6Request.Search rq);

void resolveAll(Iterable<S6Fragment> frags);




}	// end of interface S6Language



/* end of S6Language.java */
