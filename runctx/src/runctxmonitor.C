/********************************************************************************/
/*										*/
/*		runctxmonitor.C 						*/
/*										*/
/*	Main monitoring code for S6 test harnass running context		*/
/*										*/
/********************************************************************************/
/*	Copyright 1997 Brown University -- Steven P. Reiss			*/
/*********************************************************************************
 *  Copyright 1997, Brown University, Providence, RI.				 *
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
 *  WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION,	 *
 *  ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS 	 *
 *  SOFTWARE.									 *
 *										 *
 ********************************************************************************/


/* RCS: $Header$ */

/*********************************************************************************
 *
 * $Log$
 *
 ********************************************************************************/


#include "runctx_local.H"




/********************************************************************************/
/*										*/
/*	Entry points for shared library 					*/
/*										*/
/********************************************************************************/

extern "C" {
   void 	RUNCTX__init();
}




#pragma init(RUNCTX__init);




/********************************************************************************/
/*										*/
/*	Code to set up the definitions						*/
/*										*/
/********************************************************************************/

void
RUNCTX__init()
{
   RUNCTXio_init();
}




/********************************************************************************/
/*										*/
/*	General routines							*/
/*										*/
/********************************************************************************/

void *
S6::RUNCTXfind_symbol(const char * name)
{
   return dlsym(RTLD_NEXT,name);
}




/* end of runctxmonitor.C */
