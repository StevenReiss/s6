/********************************************************************************/
/*										*/
/*		SolutionBase.java						*/
/*										*/
/*	Basic implementation of a potential search solution			*/
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
a*  SOFTWARE, INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND		 *
 *  FITNESS FOR ANY PARTICULAR PURPOSE.  IN NO EVENT SHALL BROWN UNIVERSITY	 *
 *  BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY 	 *
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,		 *
 *  WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS		 *
 *  ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE 	 *
 *  OF THIS SOFTWARE.								 *
 *										 *
 ********************************************************************************/

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/solution/SolutionBase.java,v 1.14 2016/07/18 23:05:54 spr Exp $ */


/*********************************************************************************
 *
 * $Log: SolutionBase.java,v $
 * Revision 1.14  2016/07/18 23:05:54  spr
 * Update solutions for ui search.
 *
 * Revision 1.13  2015/09/23 17:58:14  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.12  2015/02/14 19:40:22  spr
 * Add test case generation.
 *
 * Revision 1.11  2014/08/29 15:16:15  spr
 * Updates for suise, testcases.
 *
 * Revision 1.10  2013/09/13 20:33:13  spr
 * Add calls for UI search.
 *
 * Revision 1.9  2012-07-20 22:15:51  spr
 * Augment solution for UI search.
 *
 * Revision 1.8  2012-06-11 14:08:20  spr
 * Add framework search; fix bugs
 *
 * Revision 1.7  2008-11-12 13:52:23  spr
 * Performance and bug updates.
 *
 * Revision 1.6  2008-08-28 00:33:01  spr
 * Next version of S6.	Lots of bug fixes, some new functionality.
 *
 * Revision 1.5  2008-07-18 22:27:16  spr
 * Handle remove compilation.
 *
 * Revision 1.4  2008-07-17 13:47:37  spr
 * Put encoding in solution to avoid recomputing.
 *
 * Revision 1.3  2008-06-27 15:45:47  spr
 * Add source access.
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
import java.util.EnumSet;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicLong;

import edu.brown.cs.ivy.xml.IvyXmlWriter;
import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Fragment;
import edu.brown.cs.s6.common.S6Solution;
import edu.brown.cs.s6.common.S6SolutionSet;
import edu.brown.cs.s6.common.S6Source;
import edu.brown.cs.s6.common.S6TestResults;
import edu.brown.cs.s6.common.S6Transform;



class SolutionBase implements S6Solution, SolutionConstants, S6Constants {


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private S6Fragment base_node;
private S6Fragment current_node;
private S6Fragment parent_node;
private List<S6Transform.Memo> transform_set;
private S6Source for_source;
private EnumSet<S6SolutionFlag> flag_set;
private String	source_encoding;
private String formatted_code;
private long solution_id;
private double solution_score;

private static AtomicLong soln_counter = new AtomicLong(0);




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

SolutionBase(S6Fragment n,S6Source src)
{
   base_node = n;
   current_node = base_node;
   parent_node = null;
   transform_set = null;
   for_source = src;
   formatted_code = null;
   flag_set = EnumSet.noneOf(S6SolutionFlag.class);
   solution_id = soln_counter.incrementAndGet();
   solution_score = src.getScore();

   computeEncoding();
}



private SolutionBase(S6Fragment b,S6Fragment p,S6Fragment n,List<S6Transform.Memo> ot,
			S6Transform.Memo t,S6Source src,double score)
{
   base_node = b;
   current_node = n;
   parent_node = p;
   for_source = src;
   formatted_code = null;
   solution_id = soln_counter.incrementAndGet();
   solution_score = score;

   if (t == null) transform_set = ot;
   else {
      if (ot == null) transform_set = new ArrayList<S6Transform.Memo>();
      else transform_set = new ArrayList<S6Transform.Memo>(ot);
      transform_set.add(t);
    }

   flag_set = EnumSet.noneOf(S6SolutionFlag.class);

   computeEncoding();
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

public S6Fragment getBaseFragment()			{ return base_node; }
public S6Fragment getFragment() 			{ return current_node; }
public S6Fragment getParentFragment()			{ return parent_node; }
public Collection<S6Transform.Memo> getTransforms()	{ return transform_set; }


public boolean checkFlag(S6SolutionFlag fg)		{ return flag_set.contains(fg); }
public void setFlag(S6SolutionFlag fg)			{ flag_set.add(fg); }
public void clearFlag(S6SolutionFlag fg)		{ flag_set.remove(fg); }

public S6Source getSource()				{ return for_source; }

public String	getEncoding()				{ return source_encoding; }

public String getId()					{ return Long.toString(solution_id); }
public void setFormattedText(String txt)		{ formatted_code = txt; }

public double	getScore()				{ return solution_score; }



/********************************************************************************/
/*										*/
/*	Resolution methods							*/
/*										*/
/********************************************************************************/

public void resolve()
{
   // System.err.println("START RESOLVE " + this);

   getFragment().resolveFragment();

   // System.err.println("END RESOLVE");
}



public void clearResolve()
{
   if (current_node == null) return;

   // System.err.println("CLEAR RESOLVE");
   getFragment().clearResolve();
}


public void clear()
{
   clearResolve();
   base_node = null;
   current_node = null;
   parent_node = null;
   formatted_code = null;
}



/********************************************************************************/
/*										*/
/*	Factory methods 							*/
/*										*/
/********************************************************************************/

public S6Solution createNewSolution(S6Fragment n,S6Transform.Memo m)
{
   double newscore = for_source.getScore() + scoreRandom();

   return new SolutionBase(base_node,current_node,n,transform_set,m,for_source,newscore);
}


private double scoreRandom()
{
   double v = Math.random() - 0.5;

   double scl = 10;
   if (transform_set != null) scl += 4*transform_set.size();
   v *= scl;

   return v;
}


public void updateFragment(S6Fragment n)
{
   current_node = n;
}



/********************************************************************************/
/*										*/
/*	Output methods								*/
/*										*/
/********************************************************************************/

public void output(IvyXmlWriter xw,S6SolutionSet ss)
{
   xw.begin("SOLUTION");
   String code = formatted_code;
   if (code == null) {
      code = getFragment().getFinalText(ss.getSearchType());

    }
   formatted_code = null;

// if (code.contains("]]>") || code.contains("<![")) xw.textElement("CODE",code);
   if (code.contains("]]>")) xw.textElement("CODE",code);
   else xw.cdataElement("CODE",code);
   xw.textElement("SOLSRC",for_source.getName());
   xw.textElement("NAME",for_source.getDisplayName());
   xw.textElement("LICENSE",for_source.getLicenseUid());

   xw.begin("COMPLEXITY");
   xw.field("LINES",getCodeLines(code));
   xw.field("CODE",getFragment().getCodeComplexity());
   S6TestResults tr = getFragment().getTestResults();
   if (tr != null) xw.field("TESTTIME",tr.getRequiredTime());
   xw.end("COMPLEXITY");

   if (transform_set != null) {
      xw.begin("TRANSFORMS");
      for (S6Transform.Memo m : transform_set) {
	 xw.textElement("TRANSFORM",m.getTransformName());
       }
      xw.end("TRANSFORMS");
    }

   xw.end("SOLUTION");
}




/********************************************************************************/
/*										*/
/*	Complexity routines							*/
/*										*/
/********************************************************************************/

private int getCodeLines(String code)
{
   StringTokenizer tok = new StringTokenizer(code,"\n");
   int codelines = 0;
   boolean incmmt = false;
   while (tok.hasMoreTokens()) {
      String lin = tok.nextToken();
      boolean hascode = false;
      for (int i = 0; i < lin.length() && !hascode; ++i) {
	 int ch = lin.charAt(i);
	 if (Character.isWhitespace(ch)) continue;
	 if (incmmt) {
	    if (ch == '*' && i+1 < lin.length() && lin.charAt(i+1) == '/') {
	       ++i;
	       incmmt = false;
	     }
	  }
	 else if (ch == '/' && i+1 < lin.length()) {
	    if (lin.charAt(i+1) == '/') break;
	    else if (lin.charAt(i+1) == '*') {
	       incmmt = true;
	     }
	  }
	 else hascode = true;
       }
      if (hascode) ++codelines;
    }

   return codelines;
}



/********************************************************************************/
/*										*/
/*	Encoding methods							*/
/*										*/
/********************************************************************************/

private void computeEncoding()
{
   source_encoding = current_node.getKeyText();

   try {
      MessageDigest md5 = MessageDigest.getInstance("MD5");
      byte [] dig = md5.digest(source_encoding.getBytes());
      StringBuffer buf = new StringBuffer();
      for (int i = 0; i < dig.length; ++i) {
	 int v = dig[i] & 0xff;
	 String s0 = Integer.toString(v,16);
	 if (s0.length() == 1) buf.append("0");
	 buf.append(s0);
       }
      // System.err.println("S6: ENCODE " + buf.toString());
      // System.err.println(source_encoding);
      // System.err.println("S6:-----------------------");
      source_encoding = buf.toString();
    }
   catch (NoSuchAlgorithmException e) { }
}



/********************************************************************************/
/*										*/
/*	Debugging routines							*/
/*										*/
/********************************************************************************/

public String toString()
{
   StringBuffer buf = new StringBuffer();
   buf.append("[SOURCE: " + for_source.getName() + ",\n");
   if (getTransforms() != null) {
      buf.append("TRANS:");
      for (S6Transform.Memo m : getTransforms()) {
	buf.append(" ");
	buf.append(m.getTransformName());
       }
    }
   else {
      buf.append(" TEXT: " + getFragment().getText());
    }
   buf.append("]");

   return buf.toString();
}



}	// end of class SolutionBase




/* end of SolutionBase.java */
