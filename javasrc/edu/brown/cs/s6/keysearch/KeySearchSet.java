/********************************************************************************/
/*										*/
/*		KeySearchSet.java						*/
/*										*/
/*	Keyword-based initial search using a set of search engines		*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/keysearch/KeySearchSet.java,v 1.6 2015/09/23 17:58:00 spr Exp $ */


/*********************************************************************************
 *
 * $Log: KeySearchSet.java,v $
 * Revision 1.6  2015/09/23 17:58:00  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.5  2013/09/13 20:32:32  spr
 * Add calls for UI search.
 *
 * Revision 1.4  2012-06-11 14:07:31  spr
 * Code cleanup
 *
 * Revision 1.3  2008-11-12 13:51:37  spr
 * Performance and bug updates.
 *
 * Revision 1.2  2008-08-28 00:32:52  spr
 * Next version of S6.	Lots of bug fixes, some new functionality.
 *
 * Revision 1.1.1.1  2008-06-03 12:59:22  spr
 * Initial version of S6
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.keysearch;


import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Future;

import edu.brown.cs.s6.common.S6Exception;
import edu.brown.cs.s6.common.S6Fragment;
import edu.brown.cs.s6.common.S6SolutionSet;


class KeySearchSet extends KeySearchBase {



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private List<KeySearchBase>	search_engines;


/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

KeySearchSet()
{
   search_engines = new ArrayList<KeySearchBase>();
}




/********************************************************************************/
/*										*/
/*	Setup methods								*/
/*										*/
/********************************************************************************/

void add(KeySearchBase sb)
{
   search_engines.add(sb);
}



/********************************************************************************/
/*										*/
/*	Search method								*/
/*										*/
/********************************************************************************/

protected void queueInitialSolutions(S6SolutionSet ss,int tgtct,Queue<Future<Boolean>> waits) throws S6Exception
{
   int ct = search_engines.size();
   tgtct = (tgtct + ct -1)/ct;
   for (KeySearchBase sb : search_engines) {
      RunSearch rs = new RunSearch(ss,sb,tgtct,waits);
      Future<Boolean> fb = ss.getEngine().executeTask(S6TaskType.IO,rs);
      synchronized (waits) {
	 waits.add(fb);
       }
    }
}


protected void newQueueInitialSolutions(S6SolutionSet ss,int tgtct,Queue<Future<Boolean>> waits) throws S6Exception
{
   int ct = search_engines.size();
   tgtct = (tgtct + ct -1)/ct;
   for (KeySearchBase sb : search_engines) {
      sb.newQueueInitialSolutions(ss,tgtct,waits);
    }
}


protected void queueSpecificSolution(S6SolutionSet ss,String src,Queue<Future<Boolean>> wq)
	throws S6Exception
{
   for (KeySearchBase sb : search_engines) {
      sb.queueSpecificSolution(ss,src,wq);
    }
}



protected void queuePackageSolutions(S6SolutionSet ss,String id,Queue<Future<Boolean>> wq,S6Fragment pf,int priority)
{
}



/********************************************************************************/
/*										*/
/*	RunSearch -- task to run a search					*/
/*										*/
/********************************************************************************/

private static class RunSearch implements Runnable {

   private S6SolutionSet solution_set;
   private KeySearchBase search_engine;
   private Queue<Future<Boolean>> wait_queue;
   private int target_count;

   RunSearch(S6SolutionSet ss,KeySearchBase kb,int ct,Queue<Future<Boolean>> waitq) {
      solution_set = ss;
      search_engine = kb;
      wait_queue = waitq;
      target_count = ct;
    }

   public void run() {
      try {
         search_engine.queueInitialSolutions(solution_set,target_count,wait_queue);
       }
      catch (S6Exception e) {
         System.err.println("S6: KEYSEARCH: Problelem running search: " + e);
       }
    }

}	// end of subclass RunSearch




}	// end of class KeySearchSet



/* end of KeySearchSet.java */
