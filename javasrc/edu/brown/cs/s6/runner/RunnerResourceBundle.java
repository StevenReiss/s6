/********************************************************************************/
/*                                                                              */
/*              RunnerResourceBundle.java                                       */
/*                                                                              */
/*      Dummy resource bundle for UI testing                                    */
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



package edu.brown.cs.s6.runner;


import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;


public class RunnerResourceBundle extends ResourceBundle
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private Hashtable<String,Object>      known_resources;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

RunnerResourceBundle()
{
   known_resources = new Hashtable<String,Object>();
}



/********************************************************************************/
/*                                                                              */
/*      Basic Implementation                                                    */
/*                                                                              */
/********************************************************************************/

@Override public Enumeration<String> getKeys()
{
   return known_resources.keys();
}


@Override protected Object handleGetObject(String key)
{
   Object rslt = known_resources.get(key);
   if (rslt == null) {
      if (key.endsWith(".png") || key.endsWith(".gif")) {
         rslt = "/research/s6/lib/sample.png";
       }
      else {
         rslt = "S6:" + key;
       }
      known_resources.put(key,rslt);
    }
   return rslt;
}

@Override protected Set<String> handleKeySet()
{
   return known_resources.keySet();
}

@Override public boolean containsKey(String key)
{
   handleGetObject(key);
   return true;
}

@Override public Locale getLocale()
{
   return Locale.getDefault();
}


/********************************************************************************/
/*                                                                              */
/*      Creation methods                                                        */
/*                                                                              */
/********************************************************************************/

public static ResourceBundle getDummyBundle()
{
   return new RunnerResourceBundle();
}

public static ResourceBundle getDummyBundle(String name)
{
   return new RunnerResourceBundle();
}

public static ResourceBundle getDummyBundle(String name,Locale l)
{
   return new RunnerResourceBundle();
}

public static ResourceBundle getDummyBundle(String name,Locale l,ClassLoader ld)
{
   return new RunnerResourceBundle();
}

public static ResourceBundle getDummyBundle(String name,Locale l,ClassLoader ld,ResourceBundle.Control ctrl)
{
   return new RunnerResourceBundle();
}




}       // end of class RunnerResourceBundle




/* end of RunnerResourceBundle.java */

