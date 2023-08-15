/********************************************************************************/
/*										*/
/*		TextDelta2.java 						*/
/*										*/
/*	Unix diff-style text delta algorithm					*/
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


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class TextDelta2 extends TextDelta
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private DiffStruct	diff_list;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

TextDelta2(String rslt,String orig)
{
   this(parse(rslt),parse(orig));
}


TextDelta2(List<String> rslt,List<String> orig)
{
   computeDiffs(orig,rslt);

/******
   int sz = diff_list.getSize();
   int osz = 0;
   for (String s : rslt) osz += s.length();
   System.err.println("TEXTDELTA: " + sz + " " + osz + " " + rslt.size() + " " + orig.size());
******/
}



/********************************************************************************/
/*										*/
/*	Application methods -- use the delta					*/
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
   for (DiffStruct ds = diff_list; ds != null; ds = ds.getNext()) {
      while(ds.getIndex() > count) {
	 ret.add(orig.get(count));
	 count++;
       }
      int ndel = ds.getNumDelete();

      if (ndel == 0) ret.add(ds.getData());
      else count += ndel;
    }

   while (count < orig.size()) {
      ret.add(orig.get(count));
      ++count;
    }

   return ret;
}



/********************************************************************************/
/*										*/
/*	Difference computation							*/
/*										*/
/********************************************************************************/

private void computeDiffs(List<String> a,List<String> b)
{
   int m = a.size();
   int n = b.size();
   int maxd = m + n;
   int origin = maxd;
   int [] lastd = new int[2*maxd+2];
   DiffStruct [] script = new DiffStruct [2*maxd+2];
   diff_list = null;

   int row = 0;
   while (row < m && row < n && a.get(row).equals(b.get(row))) row++;

   int col = 0;
   lastd[0+origin] = row;
   script[0+origin] = null;

   int lower = (row == m ? origin+1 : origin-1);
   int upper = (row == n ? origin-1 : origin+1);
   if (lower > upper) return;

   for (int d = 1; d <= maxd; ++d) {
      for (int k = lower; k <= upper; k+= 2) {
	 if (k == origin-d || (k != origin+d && lastd[k+1] >= lastd[k-1])) {
	    row = lastd[k+1] + 1;
	    script[k] = new DiffStruct(script[k+1],true,null,row-1);
	  }
	 else {
	    row = lastd[k-1];
	    // script[k] = new DiffStruct(script[k-1],false,b.get(row+k-origin-1),row-1);
	    script[k] = new DiffStruct(script[k-1],false,b.get(row+k-origin-1),row);
	  }
	 col = row + k - origin;
	 while (row < m && col < n && a.get(row).equals(b.get(col))) {
	    ++row;
	    ++col;
	  }
	 lastd[k] = row;
	 if (row == m && col == n) {
	    diff_list = script[k].createEdits();
	    return;
	  }
	 if (row == m) lower = k+2;
	 if (col == n) upper = k-2;
       }
      lower = lower-1;
      upper = upper+1;
    }

   return;
}




/********************************************************************************/
/*										*/
/*	Create array of lines to compare					*/
/*										*/
/********************************************************************************/

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
   if (tmp < s.length() - 1) {
      ret.add(s.substring(tmp, s.length()));
    }

   return ret;
}




/********************************************************************************/
/*										*/
/*	Hold difference information						*/
/*										*/
/********************************************************************************/

private static class DiffStruct {

   private int delete_count;
   private String replace_data;
   private int line_index;
   private DiffStruct next_edit;

   public DiffStruct(DiffStruct prior,boolean del, String dat, int i) {
      next_edit = prior;
      delete_count = (del ? 1 : 0);
      replace_data = dat;
      line_index = i;
    }

   public int getNumDelete()		{ return delete_count; }

   public String getData()		{ return replace_data; }

   public int getIndex()		{ return line_index; }

   public DiffStruct getNext()		{ return next_edit; }

   DiffStruct createEdits() {
      DiffStruct shead = this;
      DiffStruct ep = null;
      DiffStruct behind = null;
      while (shead != null) {
	 behind = ep;
	 if (ep != null && ep.delete_count > 0 && shead.delete_count > 0 &&
	       ep.line_index == shead.line_index + 1) {
	    shead.delete_count += ep.delete_count;
	    behind = ep.next_edit;
	  }
	 ep = shead;
	 shead = shead.next_edit;
	 ep.next_edit = behind;
       }
      return ep;
    }

   private void stringify(StringBuilder buf) {
      buf.append("@");
      buf.append(line_index);
      buf.append(" ");
      if (delete_count > 0) {
	 buf.append("DELETE ");
	 buf.append(delete_count);
	 buf.append(" ");
       }
      if (replace_data != null) {
	 buf.append("INSERT ");
	 buf.append(replace_data);
       }
      buf.append("\n");
      if (next_edit != null) next_edit.stringify(buf);
    }

   @Override public String toString() {
      StringBuilder buf = new StringBuilder();
      stringify(buf);
      return buf.toString();
    }

}	// end of inner class DiffStruct




/********************************************************************************/
/*										*/
/*	Output methods								*/
/*										*/
/********************************************************************************/

@Override public String toString()
{
   return diff_list.toString();
}



/********************************************************************************/
/*										*/
/*	Test code								*/
/*										*/
/********************************************************************************/

public static void main(String [] args)
{
   TextDelta2 td;

   String a = "private ArrayList<String> parse(String s) {" + '\n' +
   "New line goes here" + '\n' +
   "ArrayList<String> ret = new ArrayList<String>();" + '\n' +
   "int tmp = 0;" + '\n' +
   "for(int i = 0; i < s.length(); i++) {" + '\n' +
   "if(s.charAt(i) == ',') {" + '\n' +
   "ret.add(s.substring(tmp+1, i-1));" + '\n' +
   "ret.add(s.substring(tmp+1, i-1));" + '\n' +
   "tmp = i + 1;" + '\n' +
   "}" + '\n' +
   "}" + '\n' +
   "return ret;" + '\n' +
   "}\n";
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
   "return ret;" + '\n' +
   "}\n";

   td = new TextDelta2(a, b);
   String c = td.apply(b);
   System.err.println("Test " + c.equals(a) + "\n" + td);


   a = readFile("Delta.new");
   b = readFile("Delta.orig");
   td = new TextDelta2(a,b);
   c = td.apply(b);
   System.err.println("Test " + c.equals(a) + "\n" + td);
}


private static String readFile(String fn)
{
   StringBuffer buf = new StringBuffer();

   try (FileReader fr = new FileReader(fn)) {
      BufferedReader br = new BufferedReader(fr);
      for ( ; ; ) {
	 String ln = br.readLine();
	 if (ln == null) break;
	 buf.append(ln);
	 buf.append("\n");
       }
    }
   catch (IOException e) { }

   return buf.toString();
}




}	// end of class TextDelta2




/* end of TextDelta2.java */
























