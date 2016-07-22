/********************************************************************************/
/*										*/
/*		SolutionSet.java						*/
/*										*/
/*	Basic implementation of a set of potential solutions			*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/solution/SolutionSet.java,v 1.13 2016/07/18 23:05:55 spr Exp $ */


/*********************************************************************************
 *
 * $Log: SolutionSet.java,v $
 * Revision 1.13  2016/07/18 23:05:55  spr
 * Update solutions for ui search.
 *
 * Revision 1.12  2015/09/23 17:58:14  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.11  2015/02/14 19:40:23  spr
 * Add test case generation.
 *
 * Revision 1.10  2014/08/29 15:16:16  spr
 * Updates for suise, testcases.
 *
 * Revision 1.9  2013/09/13 20:33:13  spr
 * Add calls for UI search.
 *
 * Revision 1.8  2012-07-20 22:15:51  spr
 * Augment solution for UI search.
 *
 * Revision 1.7  2012-06-11 14:08:20  spr
 * Add framework search; fix bugs
 *
 * Revision 1.6  2009-05-12 22:29:27  spr
 * Commpare scores along several dimensions.
 *
 * Revision 1.5  2008-11-12 13:52:23  spr
 * Performance and bug updates.
 *
 * Revision 1.4  2008-08-28 00:33:01  spr
 * Next version of S6.	Lots of bug fixes, some new functionality.
 *
 * Revision 1.3  2008-07-17 13:47:37  spr
 * Put encoding in solution to avoid recomputing.
 *
 * Revision 1.2  2008-06-12 17:47:55  spr
 * Next version of S6.
 *
 * Revision 1.1.1.1  2008-06-03 12:59:22  spr
 * Initial version of S6
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.solution;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import edu.brown.cs.ivy.xml.IvyXmlWriter;
import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Engine;
import edu.brown.cs.s6.common.S6Fragment;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6Solution;
import edu.brown.cs.s6.common.S6SolutionSet;
import edu.brown.cs.s6.common.S6Source;


public class SolutionSet implements S6SolutionSet, SolutionConstants, S6Constants {




/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private S6Request.Search	for_request;
private Map<String,S6Solution>	solution_set;
private Set<String>		used_sources;
private Set<String>		all_solutions;
private int			num_removed;
private MessageDigest		md5_digest;
private Map<String,Counter>	stat_values;
private PriorityQueue<S6Solution>   excess_solutions;
private boolean                 test_fixup;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public SolutionSet(S6Request.Search r)
{
   for_request = r;
   solution_set = new HashMap<String,S6Solution>();
   used_sources = new HashSet<String>();
   all_solutions = new HashSet<String>();
   num_removed = 0;
   stat_values = new HashMap<String,Counter>();
   excess_solutions = new PriorityQueue<S6Solution>(100,new ScoreComparator());

   md5_digest = null;
   try {
      md5_digest = MessageDigest.getInstance("MD5");
    }
   catch (NoSuchAlgorithmException e) { }
}




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

public S6SearchType getSearchType()		{ return for_request.getSearchType(); }

public S6ScopeType getScopeType()		{ return for_request.getScopeType(); }
public S6Request.Search getRequest()		{ return for_request; }
public S6Engine getEngine()			{ return for_request.getEngine(); }

public int getSolutionCount()			{ return solution_set.size(); }
public int getSourceCount()			{ return used_sources.size(); }

public int getNumberRemoved()			{ return num_removed; }

public synchronized boolean doTestFixup() 
{
   if (test_fixup == true) return false;
   test_fixup = true;
   return true;
}

public boolean checkClearResolve()
{
   int ct = getSolutionCount();
   switch (getSearchType()) {
      case CLASS :
      case FULLCLASS :
      case TESTCASES :
	 if (ct > 500)
            return true;
	 break;
      case METHOD :
	 if (ct > 500) return true;
	 break;
      case PACKAGE :
      case UIFRAMEWORK :
      case ANDROIDUI :
      case APPLICATION :
	 return true;
    }

   return false;
}



public void saveCounts(String key)
{
   stat_values.put(key,new Counter(key,this));
}

public boolean doDebug()			{ return for_request.doDebug(); }



/********************************************************************************/
/*										*/
/*	Factory methods 							*/
/*										*/
/********************************************************************************/

public S6Solution addInitialSolution(S6Fragment n,S6Source src)
{
   if (n == null) return null;
   
   String s = n.getText();
   System.err.println("S6: Initial solution " + src + " " + s.length());
   
   if (s.length() > S6_MAX_SIZE) return null;
   
   SolutionBase sb = new SolutionBase(n,src);
   
   S6Solution rslt = null;
   
   synchronized (this) {
      if (src != null) used_sources.add(src.getName());
      rslt = add(sb);
    }
   
   return rslt;
}




/********************************************************************************/
/*										*/
/*	Source manipulation methods						*/
/*										*/
/********************************************************************************/

public synchronized boolean useSource(S6Source src)
{
   String name = src.getName();

   if (!for_request.useSource(name)) return false;

   if (used_sources.size() > S6_MAX_SOURCES) return false;

   if (!used_sources.add(name)) return false;

   return true;
}



/********************************************************************************/
/*										*/
/*	Set manipulation methods						*/
/*										*/
/********************************************************************************/

public S6Solution add(S6Solution sol)
{
   return doAdd(sol,false);
}



public void remove(S6Solution sol)
{
   doRemove(sol,false);
}



public synchronized List<S6Solution> getSolutions()
{
   return new ArrayList<S6Solution>(solution_set.values());
}



public synchronized Iterator<S6Solution> iterator()
{
   Collection<S6Solution> col = new ArrayList<S6Solution>(solution_set.values());

   return col.iterator();
}



public synchronized void pruneSolutions(int max)
{
   List<S6Solution> sortedset = new ArrayList<S6Solution>();
   sortedset.addAll(solution_set.values());
   Collections.sort(sortedset,new ScoreComparator());

   int idx = 0;
   for (S6Solution ss : sortedset) {
      if (ss.checkFlag(S6SolutionFlag.REMOVE)) {
         doRemove(ss,false);
       }
      else if (idx++ >= max) {
	 doRemove(ss,true);
       }
    }
}



public synchronized boolean restoreSolutions(int max)
{
   boolean chng = false;

   int tot = solution_set.size() + num_removed;
   if (tot > S6_MAX_SOLUTIONS) return false;

   while (solution_set.size() < max && excess_solutions.size() > 0) {
      S6Solution ss = excess_solutions.remove();
      doAdd(ss,true);
      chng = true;
    }

   return chng;
}



private S6Solution doAdd(S6Solution sol,boolean force)
{
   String senc = getSolutionEncoding(sol);

   synchronized (this) {
      S6Solution ns = solution_set.get(senc);

      if (ns != null) {
	 // possibly indicate that we have tried to add it again
	 return ns;
       }

      if (!force && md5_digest != null && all_solutions.contains(senc)) {
	 return null;
       }

      solution_set.put(senc,sol);
      all_solutions.add(senc);
    }

   return sol;
}



private void doRemove(S6Solution sol,boolean save)
{
   String s = getSolutionEncoding(sol);

   synchronized (this) {
      S6Solution ss = solution_set.remove(s);
      num_removed++;

      if (ss == null) {
	 for (Iterator<Map.Entry<String,S6Solution>> it = solution_set.entrySet().iterator();
	      it.hasNext(); ) {
	    Map.Entry<String,S6Solution> ent = it.next();
	    if (ent.getValue().equals(sol)) {
	       ss = sol;
	       it.remove();
	     }
	  }
       }
      if (ss != sol) {
	 System.err.println("S6: PROBLEM: Attempting to delete: " + sol);
       }
      if (ss != null) {
	 ss.clearResolve();
	 if (save) excess_solutions.add(ss);
	 else ss.clear();
       }
    }
}



private static class ScoreComparator implements Comparator<S6Solution> {

   public int compare(S6Solution s1,S6Solution s2) {
      double v = s1.getScore() - s2.getScore();

      if (v < 0) return -1;
      if (v > 0) return 1;

      int ln1 = 0;
      if (s1.getTransforms() != null) ln1 = s1.getTransforms().size();
      int ln2 = 0;
      if (s2.getTransforms() != null) ln2 = s2.getTransforms().size();
      if (ln1 < ln2) return -1;
      if (ln1 > ln2) return 1;

      return 0;
    }

}	// end of innerclass ScoreComparator




/********************************************************************************/
/*										*/
/*	Methods to handle solution encodings					*/
/*										*/
/********************************************************************************/

private String getSolutionEncoding(S6Solution sol)
{
   return sol.getEncoding();
}



/********************************************************************************/
/*										*/
/*	Output methods								*/
/*										*/
/********************************************************************************/

public void output(IvyXmlWriter xw)
{
   xw.begin("SOLUTIONS");
   xw.field("COUNT",solution_set.size());
   for (Counter c : stat_values.values()) {
      c.output(xw);
    }

   for (S6Solution ss : solution_set.values()) {
      ss.output(xw,this);
    }

   xw.begin("SOURCES");
   xw.field("COUNT",used_sources.size());
   for (String s : used_sources) {
      xw.textElement("SOURCE",s);
    }
   xw.end("SOURCES");

   xw.end("SOLUTIONS");
}



/********************************************************************************/
/*										*/
/*	Counter class for statistics						*/
/*										*/
/********************************************************************************/

private static class Counter {

   private String counter_name;
   private int num_solution;
   private int num_removed;

   Counter(String key,SolutionSet ss) {
      counter_name = key;
      num_solution = ss.getSolutionCount();
      num_removed = ss.getNumberRemoved();
    }

   void output(IvyXmlWriter xw) {
      xw.begin("COUNT");
      xw.field("WHAT",counter_name);
      xw.field("VALID",num_solution);
      xw.field("REMOVED",num_removed);
      xw.field("TOTAL",num_solution + num_removed);
      xw.end("COUNT");
    }

}	// end of subclass Counter



}	// end of class SolutionSet




/* end of SolutionSet.java */
