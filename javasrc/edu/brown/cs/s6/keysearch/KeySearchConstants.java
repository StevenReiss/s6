/********************************************************************************/
/*										*/
/*		KeySearchConstants.java 					*/
/*										*/
/*	Constants for keyword based initial search				*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/keysearch/KeySearchConstants.java,v 1.4 2015/09/23 17:57:58 spr Exp $ */


/*********************************************************************************
 *
 * $Log: KeySearchConstants.java,v $
 * Revision 1.4  2015/09/23 17:57:58  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.3  2015/03/11 18:04:13  spr
 * Update search for proper caching and error checking.
 *
 * Revision 1.2  2013/09/13 20:32:31  spr
 * Add calls for UI search.
 *
 * Revision 1.1.1.1  2008-06-03 12:59:22  spr
 * Initial version of S6
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.keysearch;


import java.util.LinkedList;
import java.util.concurrent.Future;
import java.nio.charset.Charset;
import java.net.URI;



import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Source;



interface KeySearchConstants extends S6Constants {



/********************************************************************************/
/*										*/
/*	File Definitions							*/
/*										*/
/********************************************************************************/

String CACHE_DIRECTORY = "/ws/volfred/s6/cache";

String CACHE_URL_FILE = "URL";
String CACHE_DATA_FILE = "DATA";

long CACHE_TIME_OUT = 1000L*60L*60L*24L*365L;

String ZIPCACHE_DIRECTORY = "/ws/volfred/s6/zips";

int TARGET_RESULTS = 200;

Charset CHAR_SET = Charset.forName("UTF-8");



/********************************************************************************/
/*										*/
/*	Wait queur								*/
/*										*/
/********************************************************************************/

class KeySearchQueue extends LinkedList<Future<Boolean>> {

   private static final long serialVersionUID = 1;

}


class KeySearchClassData {
   
   URI class_uri;
   String class_path;
   S6Source class_source;
   String class_code;
   
   KeySearchClassData(URI u,String p,S6Source src,String cd) {
      class_uri = u;
      class_path = p;
      class_source = src;
      class_code = cd;
    }
   
   URI getURI()                         { return class_uri; }
   String getPath()                     { return class_path; }
   S6Source getSource()                 { return class_source; }
   String getCode()                     { return class_code; }
   
}       // end of inner class KeySearchClassData




}	// end of interface KeySearchConstants



/*   end of KeySearchConstants.java */

