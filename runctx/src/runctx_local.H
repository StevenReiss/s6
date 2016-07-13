/********************************************************************************/
/*										*/
/*		runctx_local.H							*/
/*										*/
/*	Definitions for the S6 test harnass running context			*/
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


#ifndef RUNCTX_LOCAL_INCLUDED_ALREADY
#define RUNCTX_LOCAL_INCLUDED_ALREADY


#include <ivy_datatypes.H>

#include <dlfcn.h>




namespace S6 {


/********************************************************************************/
/*										*/
/*	Standard routines							*/
/*										*/
/********************************************************************************/

extern void * RUNCTXfind_symbol(const char * name);

extern void   RUNCTXio_init();



/********************************************************************************/
/*										*/
/*	Mapping information							*/
/*										*/
/********************************************************************************/

typedef struct _S6FileMap {
   char * actual_name;
   char * user_name;
   char map_type;
} S6FileMap;




}	// end of namespace S6

using namespace std;
using namespace S6;
using namespace Ivy;


#endif


/* end of runctx_local.H */
