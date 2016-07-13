/********************************************************************************/
/*										*/
/*		S6Context.java							*/
/*										*/
/*	Context information for S6 searching					*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/common/S6Context.java,v 1.2 2009-05-12 22:26:06 spr Exp $ */


/*********************************************************************************
 *
 * $Log: S6Context.java,v $
 * Revision 1.2  2009-05-12 22:26:06  spr
 * Editing fixups.
 *
 * Revision 1.1  2008-11-12 13:52:48  spr
 * Start of context management.
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.common;


import java.io.File;
import java.io.InputStream;
import java.util.Collection;

public interface S6Context extends S6Constants {

   public boolean contains(String name);
   public InputStream getInputStream(String name);
   public void close(S6Request rqst);

   public String getJarFileName();
   public File getContextDirectory() throws S6Exception;
   public Collection<UserFile> getUserFiles() throws S6Exception;

   interface UserFile {
      String getLocalName();
      String getUserName();
      S6UserFileType getFileType();
    }

}	// en dof interface S6Context




/* end of S6Context.java */
