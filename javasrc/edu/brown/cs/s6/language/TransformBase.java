/********************************************************************************/
/*										*/
/*		TransformBase.java						*/
/*										*/
/*	Basic implementation of a S6 transformation				*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/language/TransformBase.java,v 1.9 2015/09/23 17:58:04 spr Exp $ */


/*********************************************************************************
 *
 * $Log: TransformBase.java,v $
 * Revision 1.9  2015/09/23 17:58:04  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.8  2013/09/13 20:32:51  spr
 * Add calls for UI search.
 *
 * Revision 1.7  2009-05-12 22:28:44  spr
 * Fix ups to make user context work.
 *
 * Revision 1.6  2008-11-12 13:51:54  spr
 * Performance and bug updates.
 *
 * Revision 1.5  2008-08-28 00:32:53  spr
 * Next version of S6.	Lots of bug fixes, some new functionality.
 *
 * Revision 1.4  2008-07-18 22:26:49  spr
 * Handle remove compilation calls.
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


import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Transform;


public abstract class TransformBase implements S6Transform, S6Constants, LanguageConstants {



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private String		transform_name;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

protected TransformBase(String name)
{
   transform_name = name;
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

public String getName() 		{ return transform_name; }



/********************************************************************************/
/*										*/
/*	Basic memo implementation						*/
/*										*/
/********************************************************************************/

protected static class MemoImpl implements S6Transform.Memo {

   private String memo_transform;
   private String base_name;

   public MemoImpl(String name,String base) {
      memo_transform = name;
      base_name = name;
    }

   public String getBaseName()			 { return base_name; }

   public String getTransformName() {
      if (memo_transform != null) return memo_transform;
      return base_name;
    }

   public String toString() {
      return "MEMO[" + getTransformName() + "]";
    }

}	// end of subclass MemoImpl



}	// end of class TransformBase



/* end of TransformBase.java */
