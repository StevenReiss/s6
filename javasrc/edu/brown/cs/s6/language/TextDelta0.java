/********************************************************************************/
/*										*/
/*		TextDelta0.java 						*/
/*										*/
/*	Very simple text delta implementation					*/
/*										*/
/********************************************************************************/
/*	Copyright 2013 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2013, Brown University, Providence, RI.				 *
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



package edu.brown.cs.s6.language;



class TextDelta0 extends TextDelta
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private int	initial_match;
private int	final_match;
private String	changed_string;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

TextDelta0(String rslt,String orig)
{
   computeDelta(rslt,orig);
}



/********************************************************************************/
/*										*/
/*	Methods to apply the delta						*/
/*										*/
/********************************************************************************/

@Override public String apply(String orig)
{
   StringBuilder buf = new StringBuilder();

   buf.append(orig,0,initial_match);
   if (changed_string != null) buf.append(changed_string);

   if (final_match > 0) {
      int ln = orig.length();
      buf.append(orig,ln-final_match,ln);
    }

   return buf.toString();
}




/********************************************************************************/
/*										*/
/*	Methods to compute the delta						*/
/*										*/
/********************************************************************************/

private void computeDelta(String rslt,String orig)
{
   initial_match = 0;
   final_match = 0;
   changed_string = null;

   int rln = rslt.length();
   int oln = orig.length();

   initial_match = 0;
   while (initial_match < rln && initial_match < oln &&
	     rslt.charAt(initial_match) == orig.charAt(initial_match))
      ++initial_match;

   if (initial_match == rln && rln == oln) {
      return;
    }

   final_match = 0;
   while (final_match < rln-initial_match &&
	     final_match < oln-initial_match &&
	     rslt.charAt(rln-final_match-1) == orig.charAt(oln-final_match-1))
      ++final_match;

   if (final_match + initial_match == rln) {
      return;
    }

   changed_string = rslt.substring(initial_match,rln-final_match);
}





/********************************************************************************/
/*										*/
/*	Test cases								*/
/*										*/
/********************************************************************************/

public static void main(String [] args)
{
   TextDelta0 td;

   td = new TextDelta0("abcdef","abxef");
   System.err.println("TEST: " + td.apply("abxef"));

   td = new TextDelta0("abcdef","abcde");
   System.err.println("TEST: " + td.apply("abcde"));

   td = new TextDelta0("abcdef","bcdef");
   System.err.println("TEST: " + td.apply("bcdef"));

   td = new TextDelta0("abcdef","abcdef");
   System.err.println("TEST: " + td.apply("abcdef"));

   td = new TextDelta0("abcdef","axxxxxxxxxxxf");
   System.err.println("TEST: " + td.apply("axxxxxxxxxxxf"));

   td = new TextDelta0("abcdef","abcxxdef");
   System.err.println("TEST: " + td.apply("abcxxdef"));

   td = new TextDelta0("abcdef","abef");
   System.err.println("TEST: " + td.apply("abef"));

   String a = "private ArrayList<String> parse(String s) {" + '\n' +
   "ArrayList<String> ret = new ArrayList<String>();" + '\n' +
   "int tmp = 0;" + '\n' +
   "for(int i = 0; i < s.length(); i++) {" + '\n' +
   "if(s.charAt(i) == ',') {" + '\n' +
   "ret.add(s.substring(tmp+1, i-1));" + '\n' +
   "tmp = i + 1;" + '\n' +
   "}" + '\n' +
   "}" + '\n' +
   "return ret;" + '\n' +
   "}";
   String b = "private ArrayList<String> parse(String s) {" + '\n' +
   "ArrayList<String> ret = new ArrayList<String>();" + '\n' +
   "int thing = 0;" + '\n' +
   "for(int i = 0; i < s.length(); i++) {" + '\n' +
   "if(s.charAt(i) == 's') {" + '\n' +
   "ret.add(s.substring(tmp+1, i-1));" + '\n' +
   "tmp = i + 1;" + '\n' +
   "}" + '\n' +
   "}" + '\n' +
   "return ret;" + '\n' +
   "}";

   td = new TextDelta0(a, b);
   System.err.println("Test " + td.apply(b));

}



}	// end of class TextDelta0




/* end of TextDelta0.java */

