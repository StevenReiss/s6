/********************************************************************************/
/*                                                                              */
/*              SearchWordRun.java                                              */
/*                                                                              */
/*      Generate the word list for a file or other input                        */
/*                                                                              */
/********************************************************************************/
/*      Copyright 2013 Brown University -- Steven P. Reiss                    */
/*********************************************************************************
 *  Copyright 2013, Brown University, Providence, RI.                            *
 *                                                                               *
 *                        All Rights Reserved                                    *
 *                                                                               *
 *  Permission to use, copy, modify, and distribute this software and its        *
 *  documentation for any purpose other than its incorporation into a            *
 *  commercial product is hereby granted without fee, provided that the          *
 *  above copyright notice appear in all copies and that both that               *
 *  copyright notice and this permission notice appear in supporting             *
 *  documentation, and that the name of Brown University not be used in          *
 *  advertising or publicity pertaining to distribution of the software          *
 *  without specific, written prior permission.                                  *
 *                                                                               *
 *  BROWN UNIVERSITY DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS                *
 *  SOFTWARE, INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND            *
 *  FITNESS FOR ANY PARTICULAR PURPOSE.  IN NO EVENT SHALL BROWN UNIVERSITY      *
 *  BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY          *
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,              *
 *  WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS               *
 *  ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE          *
 *  OF THIS SOFTWARE.                                                            *
 *                                                                               *
 ********************************************************************************/



package edu.brown.cs.s6.search;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import edu.brown.cs.ivy.file.IvyFile;

public class SearchWordRun implements SearchWordConstants
{



/********************************************************************************/
/*                                                                              */
/*      Main program                                                            */
/*                                                                              */
/********************************************************************************/

public static void main(String [] args)
{
   SearchWordRun swr = new SearchWordRun(args);
   
   swr.process();
}




/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private List<File>  input_files;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

private SearchWordRun(String [] args)
{
   input_files = null;
   
   scanArgs(args);
}



/********************************************************************************/
/*                                                                              */
/*      Processing methods                                                      */
/*                                                                              */
/********************************************************************************/

private void process()
{
   SearchWordFactory swf = SearchWordFactory.getFactory();
   swf.clear();
   
   if (input_files != null) {
      for (File f : input_files) {
         try {
            String txt = IvyFile.loadFile(f);
            swf.loadSource(txt,true);
          }
         catch (IOException e) {
            System.err.println("SEARCH: Problem reading file " + f + ": " + e);
          }
       }
    }
   else {
      try {
         BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
         for ( ; ; ) {
            String ln = br.readLine();
            if (ln == null) break;
            swf.loadSource(ln,true);
          }
       }
      catch (IOException e) { }
    }
   
   List<String> q = swf.getQuery();
   System.err.println("RESULT:");
   for (String qw : q) {
      System.err.println("   " + qw);
    }
}



/********************************************************************************/
/*                                                                              */
/*      Argument scanning                                                       */
/*                                                                              */
/********************************************************************************/

private void scanArgs(String [] args) 
{
   for (int i = 0; i < args.length; ++i) {
      if (args[i].startsWith("-")) {
         badArgs();
       }
      else {
         if (input_files == null) input_files = new ArrayList<File>();
         input_files.add(new File(args[i]));
       }
    }
}



private void badArgs()
{
   System.err.println("SEARCHWORDRUN: <input file ...>");
   System.exit(1);
}




}       // end of class SearchWordRun




/* end of SearchWordRun.java */

