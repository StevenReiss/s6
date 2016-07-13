/********************************************************************************/
/*										*/
/*		TextDelta1.java 						*/
/*										*/
/*	Representation of changes between two text versions			*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/language/TextDelta1.java,v 1.2 2015/09/23 17:58:03 spr Exp $ */


/*********************************************************************************
 *
 * $Log: TextDelta1.java,v $
 * Revision 1.2  2015/09/23 17:58:03  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.1  2013/09/20 21:02:04  spr
 * Add missing files
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.language;



import java.util.ArrayList;
import java.util.List;

class TextDelta1 extends TextDelta {



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private List<DiffStruct> diff;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

TextDelta1(String rslt,String orig)
{
    List<String> a = parse(rslt);
    List<String> b = parse(orig);

    computeDiff(a,b);
}



TextDelta1(List<String> rslt,List<String> orig) {
   computeDiff(rslt,orig);
}



/********************************************************************************/
/*										*/
/*	Methods to apply the delta						*/
/*										*/
/********************************************************************************/


@Override public String apply(String orig)
{
   List<String> tmp = new ArrayList<String>();
   tmp = parse(orig);
   tmp = applyDiff(tmp);
   StringBuilder ret = new StringBuilder();
   for (String s : tmp) {
      ret.append(s);
      ret.append("\n");
    }

   return ret.toString();
}




private List<String> applyDiff(List<String> orig)
{
    List<String> ret = new ArrayList<String>();

    int count = 0;

    for(int i = 0; i < diff.size(); i++) {
	while(diff.get(i).getIndex() != count) {
	    ret.add(orig.get(count));
	    count++;
	}
	if(!diff.get(i).getDelete())
	    ret.add(diff.get(i).getData());
	else
	    orig.remove(count);
    }

    return ret;
}


/********************************************************************************/
/*										*/
/*	Methods to compute the delta						*/
/*										*/
/********************************************************************************/

private void computeDiff(List<String> rslt,List<String> orig) {
    int origCount = 0;
    int rsltCount = 0;
    int LCSCount = 0;

    int delCount = 0;
    int addCount = 0;

    List<String> lcs = findLCS(rslt,orig);
    diff = new ArrayList<DiffStruct>();

    while(origCount < orig.size() || rsltCount < rslt.size() || LCSCount < lcs.size()) {
	if(LCSCount >= lcs.size()) {
	    while(origCount < orig.size()) {
		diff.add(new DiffStruct(true, orig.get(origCount), origCount-delCount));
		delCount++;
		origCount++;
	    }
	    while(rsltCount < rslt.size()) {
		diff.add(new DiffStruct(false, rslt.get(rsltCount), rsltCount-addCount));
		rsltCount++;
		addCount++;
	    }
	}
	else {
	   try {
	      while(origCount < orig.size() && !orig.get(origCount).equals(lcs.get(LCSCount))) {
		 diff.add(new DiffStruct(true, orig.get(origCount), origCount-delCount));
		 delCount++;
		 origCount++;
	       }
	    }
	   catch (IndexOutOfBoundsException e) {
	      System.err.println("IOB SIZE " + origCount + " " + orig.size() + " " + LCSCount + " " + lcs.size());
	    }
	    while(rsltCount < rslt.size() && !rslt.get(rsltCount).equals(lcs.get(LCSCount))){
		diff.add(new DiffStruct(false, rslt.get(rsltCount), rsltCount-addCount));
		rsltCount++;
		addCount++;
	    }
	    origCount++;
	    rsltCount++;
	    LCSCount++;
	}
    }
}




private List<String> findLCS(List<String> a,List<String> b) {
    int start = 0;
    int aend = a.size()-1;
    int bend = b.size()-1;

    while(start <= aend && start <= bend && a.get(start).equals(b.get(start))) {
	start++;
    }
    while(start <= aend && start <= bend && a.get(aend).equals(b.get(bend))) {
	aend--;
	bend--;
    }

    System.err.println("S6: DELTA LCS " + start + " " + aend + " " + bend);

    int[][] LCSMat = new int[aend-start+2][bend-start+2];
    for(int i = 0; i < LCSMat.length; i++) {
	for(int j = 0; j < LCSMat[i].length; j++) {
	    if(i == 0 || j == 0) {
		LCSMat[i][j] = 0;
	    }
	    else if(a.get(i+start-1).equals(b.get(j+start-1))) {
		LCSMat[i][j] = LCSMat[i-1][j-1] + 1;
	    }
	    else if(LCSMat[i][j-1] > LCSMat[i-1][j])
		LCSMat[i][j] = LCSMat[i][j-1];
	    else
		LCSMat[i][j] = LCSMat[i-1][j];
	}
    }

    List<String> lcs = new ArrayList<String>();
    ArrayList<String> tmp = new ArrayList<String>();

    int i = LCSMat.length-1;
    int j = LCSMat[0].length-1;

    while(i != 0 && j != 0) {
	if(LCSMat[i-1][j] == LCSMat[i][j])
	    i--;
	else if(LCSMat[i][j-1] == LCSMat[i][j])
	    j--;
	else if(LCSMat[i-1][j-1]+1 == LCSMat[i][j]) {
	    tmp.add(a.get(i+start-1));
	    i--;
	    j--;
	}
    }
    while(tmp.size() != 0)
	lcs.add(tmp.remove(tmp.size()-1));

    return lcs;
}




//each diffStruct object holds information on the data that
//has changed, what line it belongs on, and whether it
//should be added or deleted.
private static class DiffStruct {

    private boolean is_delete;
    private String replace_data;
    private int line_index;

    public DiffStruct(boolean del, String dat, int i) {
	is_delete = del;
	replace_data = dat;
	line_index = i;
    }

    public boolean getDelete() {
	return is_delete;
    }

    public String getData() {
	return replace_data;
    }

    public int getIndex() {
	return line_index;
    }

} //end of inner class diffStruct




//parses a string by newlines
private static ArrayList<String> parse(String s)
{
   ArrayList<String> ret = new ArrayList<String>();

   int tmp = 0;
   for(int i = 0; i < s.length(); i++) {
      if(s.charAt(i) == '\n') {
	 ret.add(s.substring(tmp, i));
	 tmp = i + 1;
       }
    }
   ret.add(s.substring(tmp, s.length()));
   return ret;
}



/********************************************************************************/
/*										*/
/*	Test cases								*/
/*										*/
/********************************************************************************/

public static void main(String [] args)
{
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

    TextDelta1 td = new TextDelta1(a, b);
    System.err.println("Test " + td.apply(b));
}




}	// end of class TextDelta1




/* end of TextDelta1.java */
