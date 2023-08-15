/********************************************************************************/
/*										*/
/*		SearchJava.java 						*/
/*										*/
/*	Implementation of a Java analyzer for LUCENE				*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/search/SearchJava.java,v 1.4 2015/09/23 17:58:11 spr Exp $ */


/*********************************************************************************
 *
 * $Log: SearchJava.java,v $
 * Revision 1.4  2015/09/23 17:58:11  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.3  2013-05-09 12:26:25  spr
 * Minor changes to start ui fixups.
 *
 * Revision 1.2  2008-06-12 17:47:55  spr
 * Next version of S6.
 *
 * Revision 1.1.1.1  2008-06-03 12:59:22  spr
 * Initial version of S6
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.search;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import edu.brown.cs.ivy.file.IvyLog;
import edu.brown.cs.s6.common.S6Constants;



public class SearchJava extends Analyzer implements S6Constants, SearchConstants {



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private static final Set<String> word_set;

private static final Set<String> stop_words;

private static final Map<String,String> abbrev_map;

private static final String BAD_MAP = "**BAD**";


static {
   stop_words = new HashSet<String>();
   for (String s : StandardAnalyzer.STOP_WORDS) {
      stop_words.add(s);
    }
   try (BufferedReader br = new BufferedReader(new FileReader(KEYWORD_FILE))) {
      for ( ; ; ) {
	 String ln = br.readLine();
	 if (ln == null) break;
	 StringTokenizer tok = new StringTokenizer(ln);
	 while (tok.hasMoreTokens()) {
	    stop_words.add(tok.nextToken());
	  }
       }
    }
   catch (IOException e) {
      IvyLog.logE("SEARCH","Problem reading keyword file: " + e);
    }
}



static {
   word_set = new HashSet<String>();
   String file = "/usr/share/dict/words";

   try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      for ( ; ; ) {
	 String s = br.readLine();
	 if (s == null) break;
	 int ln = s.length();
	 if (ln == 0) continue;
	 boolean fg = true;
	 for (int i = 0; i < ln; ++i) {
	    char c = s.charAt(i);
	    if (!Character.isLowerCase(c)) {
	       fg = false;
	       break;
	     }
	  }
	 if (fg) word_set.add(s);
       }
    }
   catch (IOException _e) {
      IvyLog.logE("SEARCH","Unable to read word file " + file);
    }

   abbrev_map = new HashMap<String,String>();
   for (String s : word_set) {
      String s1 = removeVowels(s);
      if (s1 != null) {
	 if (abbrev_map.containsKey(s1)) abbrev_map.put(s1,BAD_MAP);
	 else abbrev_map.put(s1,s);
       }
    }
}



private static String removeVowels(String s)
{
   if (s.length() <= 3) return null;

   StringBuilder sb = new StringBuilder();
   for (int i = 0; i < s.length(); ++i) {
      char c = s.charAt(i);
      switch (c) {
	 case 'a' : case 'e' : case 'i' : case 'o' : case 'u' :
	 case 'A' : case 'E' : case 'I' : case 'O' : case 'U' :
	    break;
	 default :
	    sb.append(c);
	    break;
       }
    }

   return sb.toString();
}



/********************************************************************************/
/*										*/
/*	Methods to handle token streaming					*/
/*										*/
/********************************************************************************/

public TokenStream tokenStream(String field,Reader r)
{
   return null;
}



Tokenizer createTokenizer(Reader r)
{
   return new JavaTokenizer(r);
}




/********************************************************************************/
/*										*/
/*	Methods to find alternative names from a token				*/
/*										*/
/********************************************************************************/

private void findAlternatives(CharSequence txt,int spos,int epos,List<String> rslt)
{
   rslt.add(getLowerString(txt,spos,epos));

   boolean last_upper = true;

   int start = spos;
   char ch = txt.charAt(start);
   while (!Character.isLetter(ch) && start+1 < epos) {
      ++start;
      ch = txt.charAt(start);
    }
   if (start+1 >= epos) return;

   int pos = start;
   while (pos < epos) {
      ch = txt.charAt(pos);
      if (!Character.isLetter(ch)) {
	 findAlternatives(txt,start,pos,rslt);
	 findAlternatives(txt,pos,epos,rslt);
	 return;
       }
      else if (!last_upper && Character.isUpperCase(ch)) {
	 last_upper = true;
	 findAlternatives(txt,start,pos,rslt);
	 findAlternatives(txt,pos,epos,rslt);
	 return;
       }
      else if (Character.isLowerCase(ch)) last_upper = false;
      ++pos;
    }

   splitWords(txt,spos,epos,rslt);
}



private boolean splitWords(CharSequence txt,int spos,int epos,List<String> pfx)
{
   String s0 = getLowerString(txt,spos,epos);

   if (word_set.contains(getLowerString(txt,spos,epos))) return false;
   String s1 = abbrev_map.get(s0);
   if (s1 != null && s1 != BAD_MAP) {
      pfx.add(s1);
      return false;
    }

   int ln = epos-spos;
   for (int i = 3; i < ln-3; ++i) {
      String w0 = getLowerString(txt,spos,spos+i);
      if (word_set.contains(w0)) {
	 String w = getLowerString(txt,spos+i,epos);
	 if (word_set.contains(w)) {
	    pfx.add(w0);
	    pfx.add(w);
	    return true;
	  }
	 String w1 = abbrev_map.get(w);
	 if (w1 != null && w1 != BAD_MAP) {
	    pfx.add(w0);
	    pfx.add(w1);
	    pfx.add(w);
	    return true;
	  }
	 if (splitWords(txt,spos+i,epos,pfx)) {
	    pfx.add(w0);
	    return true;
	  }
       }
    }

   return false;
}



private String getLowerString(CharSequence txt,int spos,int epos)
{
   StringBuilder sb = new StringBuilder();
   for (int i = spos; i < epos; ++i) {
      char ch = txt.charAt(i);
      if (Character.isUpperCase(ch)) ch = Character.toLowerCase(ch);
      sb.append(ch);
    }
   return sb.toString();
}




/********************************************************************************/
/*										*/
/*	Class to hold Java tokenizer						*/
/*										*/
/********************************************************************************/

class JavaTokenizer extends Tokenizer {

   private LinkedList<String> to_add;
   private int start_offset;
   private int end_offset;
   private int tok_counter;

   JavaTokenizer() {
      to_add = new LinkedList<String>();
    }

   JavaTokenizer(Reader in) {
      super(in);
      to_add = new LinkedList<String>();
      start_offset = 0;
      end_offset = 0;
      tok_counter = 0;
    }

   public void reset(Reader in) {
      start_offset = 0;
      end_offset = 0;
      tok_counter = 0;
    }

   public Token next() {
      if (to_add.size() == 0 && !getNextTokenSet()) return null;

      String s = to_add.removeFirst();
      Token result = new Token(s,start_offset,end_offset);
      // result.clear();
      // result.setTermText(s);
      // result.setStartOffset(start_offset);
      // result.setEndOffset(end_offset);
      result.setPositionIncrement(tok_counter == 0 ? 1 : 0);
      ++tok_counter;

      return result;
    }

   private boolean getNextTokenSet() {
      char ch;
      to_add.clear();
   
      for ( ; ; ) {
         ch = nextChar();
         if (ch < 0 || ch == (char) -1) return false;
         if (Character.isLetter(ch)) break;
       }
      StringBuffer buf = new StringBuffer();
      while (Character.isLetter(ch)) {
         buf.append(ch);
         ch = nextChar();
       }
   
      findAlternatives(buf,0,buf.length(),to_add);
   
      tok_counter = 0;
      
      return true;
    }

   private char nextChar() {
      try {
	 int ch = input.read();
	 if (ch < 0) return (char) -1;
	 return (char) ch;
       }
      catch (IOException e) {
         return (char) -1;
       }
    }

}	// end of subclass JavaTokenizer





}	// end of SearchJava



/* end of SearchJava.java */
