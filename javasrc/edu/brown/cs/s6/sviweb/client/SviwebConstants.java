/********************************************************************************/
/*										*/
/*		SviwebConstants.java						*/
/*										*/
/*	Constants for web search front end					*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/sviweb/client/SviwebConstants.java,v 1.7 2013/09/13 20:33:15 spr Exp $ */


/*********************************************************************************
 *
 * $Log: SviwebConstants.java,v $
 * Revision 1.7  2013/09/13 20:33:15  spr
 * Add calls for UI search.
 *
 * Revision 1.6  2013-05-09 12:26:27  spr
 * Minor changes to start ui fixups.
 *
 * Revision 1.5  2009-09-18 01:42:37  spr
 * Updates for new gwt and support user feedback and full class.
 *
 * Revision 1.4  2008-08-28 00:33:03  spr
 * Next version of S6.	Lots of bug fixes, some new functionality.
 *
 * Revision 1.3  2008-07-17 13:47:46  spr
 * User interface fixups, mainly for class search.
 *
 * Revision 1.2  2008-06-12 17:47:57  spr
 * Next version of S6.
 *
 * Revision 1.1.1.1  2008-06-03 12:59:23  spr
 * Initial version of S6
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.sviweb.client;



import java.util.HashMap;
import java.util.Map;



public interface SviwebConstants {


/********************************************************************************/
/*										*/
/*	Status type								*/
/*										*/
/********************************************************************************/

enum Status {
   UNCHECKED,
   UNUSED,
   CHECKING,
   BAD,
   OK
}



/********************************************************************************/
/*										*/
/*	TestCase type								*/
/*										*/
/********************************************************************************/

enum TestType {
   CALL,
   USERCODE,
   TESTFILE,
   CLASSCALL,
   CALLSET,
   CALLSETCONT;

   public static TestType get(String s) {
      try {
	 return valueOf(s);
       }
      catch (IllegalArgumentException e) { }
      return null;
    }

}	// end of enum TestType




/********************************************************************************/
/*										*/
/*	TestOp type								*/
/*										*/
/********************************************************************************/

class TestOp {
   private static Map<String,TestOp> op_map = new HashMap<String,TestOp>();
   
   public final static TestOp EQL = new TestOp("EQL","==");
   public final static TestOp NEQ = new TestOp("NEQ","!=");
   public final static TestOp SAVE = new TestOp("SAVE","==>");
   public final static TestOp IGNORE = new TestOp("IGNORE","???");
   public final static TestOp THROW = new TestOp("THROW","throws");
   public final static TestOp SAME = new TestOp("SAME","===");
   public final static TestOp DIFF = new TestOp("DIFF","!===");
   public final static TestOp SHOW = new TestOp("SHOW","< ?? >");
   
   private String op_name;
   private String op_text;

   private TestOp(String s,String t) {
      op_name = s;
      op_text = t;
      op_map.put(s,this);
      op_map.put(t,this);
    }

   public String getDisplay()		{ return op_text; }
   public String toString()		{ return op_name; }

   public static TestOp get(String s)	{ return op_map.get(s); }

}	// end of enum TestOp



/********************************************************************************/
/*										*/
/*	ResultSort type 							*/
/*										*/
/********************************************************************************/

class ResultSort {

   public final static int SIZE = 0;
   public final static int COMPLEXITY = 1;
   public final static int COST = 2;

}	// end of enum ResultSort




/********************************************************************************/
/*										*/
/*	Tab index starting points						*/
/*										*/
/********************************************************************************/

int	TOP_TAB_INDEX = 1;

int	METHOD_TAB_START = 100;

int	CLASS_TAB_START = 200;

int     UI_TAB_START = 300;

int	FIND_TAB_START = 500;

int	RESULT_TAB_START = 600;




/********************************************************************************/
/*										*/
/*	Local callback types							*/
/*										*/
/********************************************************************************/

interface StatusCallback {

   public void setStatus(Status sts);

}




}	// end of interface SviwebConstants




/* end of SviwebConstants.java */
