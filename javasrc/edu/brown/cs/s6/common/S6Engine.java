/********************************************************************************/
/*										*/
/*		S6Engine.java							*/
/*										*/
/*	Public interface for S6 search code search controller			*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/common/S6Engine.java,v 1.9 2015/12/23 15:44:32 spr Exp $ */


/*********************************************************************************
 *
 * $Log: S6Engine.java,v $
 * Revision 1.9  2015/12/23 15:44:32  spr
 * Add S6Resource.java, minor fixes.
 *
 * Revision 1.8  2015/09/23 17:57:46  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.7  2013/09/13 20:31:52  spr
 * Add UI search.
 *
 * Revision 1.6  2012-07-20 22:14:48  spr
 * Additions for UI search
 *
 * Revision 1.5  2012-06-11 14:07:27  spr
 * Code cleanup
 *
 * Revision 1.4  2008-11-12 13:50:47  spr
 * Performance and bug updates.
 *
 * Revision 1.3  2008-08-28 00:32:45  spr
 * Next version of S6.	Lots of bug fixes, some new functionality.
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


import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.w3c.dom.Element;

import edu.brown.cs.cose.cosecommon.CoseResult;
import edu.brown.cs.cose.cosecommon.CoseSource;



public interface S6Engine extends S6Constants {


boolean doDebug();
int getNumberOfSearchThreads();

String handleSearchRequest(Element xml) throws S6Exception;
String handleCheckRequest(Element xml) throws S6Exception;

S6Fragment createFileFragment(String text,CoseSource src,S6Request.Search sr);
Set<String> getRelatedProjects(S6Fragment src);
Set<String> getUsedProjects(S6Fragment src);
S6Fragment createPackageFragment(S6Request.Search sr);

S6Fragment createFragment(CoseResult cr,S6Request.Search req);

Future<Boolean> executeTask(S6TaskType tt,Callable<Boolean> c);
Future<Boolean> executeTask(S6TaskType tt,Runnable r);
boolean waitForAll(Queue<Future<Boolean>> waitq);


}	// end of interface S6Engine



/* end of S6Engine.java */
