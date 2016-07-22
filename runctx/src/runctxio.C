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


/* RCS: $Header: /pro/spr_cvs/pro/s6/runctx/src/runctxio.C,v 1.1 2016/07/18 23:06:46 spr Exp $ */

/*********************************************************************************
 *
 * $Log: runctxio.C,v $
 * Revision 1.1  2016/07/18 23:06:46  spr
 * Uodate run monitor so it will compile.
 *
 *
 ********************************************************************************/


#include "runctx_local.H"

#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>
#include <stdarg.h>


/********************************************************************************/
/*										*/
/*	Local storage								*/
/*										*/
/********************************************************************************/

static bool		io_inited = false;
static bool		map_inited = false;

static S6FileMap *	file_map = NULL;
static int		file_map_size = 0;


static int (*open_addr)(const char *,int,mode_t);
static int (*open64_addr)(const char *,int,mode_t);
static int (*creat_addr)(const char *,mode_t);
static int (*creat64_addr)(const char *,mode_t);
static int (*stat_addr)(const char *,struct stat *);
static int (*xstat_addr)(int,const char *,struct stat *);
static int (*stat64_addr)(const char *,struct stat64 *);
static int (*xstat64_addr)(int,const char *,struct stat64 *);
static int (*lstat_addr)(const char *,struct stat *);
static int (*lxstat_addr)(int,const char *,struct stat *);

#ifdef APPLE
#define THROW
#else
#define THROW throw()
#endif



/********************************************************************************/
/*										*/
/*	Local routines								*/
/*										*/
/********************************************************************************/

static const char * mapName(const char *);
static void setupMap();



/********************************************************************************/
/*										*/
/*	Entry points for shared library 					*/
/*										*/
/********************************************************************************/

extern "C" {

   int open(const char *,int,...);
   int open64(const char *,int,...);
   int creat(const char *,mode_t);
   int creat64(const char *,mode_t);
   int stat(const char *,struct stat *) THROW;
   int __xstat(int,const char *,struct stat *) THROW;
   int stat64(const char *,struct stat64 *);
   int __xstat64(int v,const char *,struct stat64 *);
   int lstat(const char *,struct stat *) THROW;
   int __lxstat(int,const char *,struct stat *) THROW;

}



int
open(const char * nm,int fg,...)
{
   va_list ap;

   va_start(ap,fg);
   int md = va_arg(ap,int);

   const char * nnm = mapName(nm);

   int sts = (*open_addr)(nnm,fg,md);

   return sts;
}




int
open64(const char * nm,int fg,...)
{
   va_list ap;

   va_start(ap,fg);
   int md = va_arg(ap,int);

   const char * nnm = mapName(nm);

   int sts = (*open64_addr)(nnm,fg,md);

   return sts;
}




int
creat(const char * nm,mode_t md)
{
   const char * nnm = mapName(nm);

   int sts = (*creat_addr)(nnm,md);

   return sts;
}




int
creat64(const char * nm,mode_t md)
{
   const char * nnm = mapName(nm);

   int sts = (*creat64_addr)(nnm,md);

   return sts;
}




int
stat(const char * nm,struct stat * st) THROW
{
   const char * nnm = mapName(nm);

   int sts = (*stat_addr)(nnm,st);

   return sts;
}





int
__xstat(int v,const char * nm,struct stat * st) THROW
{
   const char * nnm = mapName(nm);

   int sts = (*xstat_addr)(v,nm,st);

   return sts;
}





int
stat64(const char * nm,struct stat64 * st) THROW
{
   const char * nnm = mapName(nm);

   int sts = (*stat64_addr)(nnm,st);

   return sts;
}





int
__xstat64(int v,const char * nm,struct stat64 * st) THROW
{
   const char * nnm = mapName(nm);

   int sts = (*xstat64_addr)(v,nnm,st);

   return sts;
}





int
lstat(const char * nm,struct stat * st) THROW
{
   const char * nnm = mapName(nm);

   int sts = (*lstat_addr)(nnm,st);

   return sts;
}




int
__lxstat(int v,const char * nm,struct stat * st) THROW
{
   const char * nnm = mapName(nm);

   int sts = (*lxstat_addr)(v,nnm,st);

   return sts;
}




/********************************************************************************/
/*										*/
/*	Name mapping methods							*/
/*										*/
/********************************************************************************/

const char *
mapName(const char * orig)
{
   if (!io_inited) RUNCTXio_init();

   if (strprefix(orig,"/s6/")) {
      if (!map_inited) setupMap();
      for (int i = 0; i < file_map_size; ++i) {
	 if (strcmp(&orig[4],file_map[i].user_name) == 0) {
	    // fprintf(stderr,"RUNCTX: FOUND %s\n",file_map[i].actual_name);
	    return file_map[i].actual_name;
	  }
       }
    }

   return orig;
}




void
setupMap()
{
   if (map_inited) return;
   map_inited = true;

   const char * mapstr = getenv("S6_CONTEXTMAP");
   file_map_size = 0;
   if (mapstr != NULL && *mapstr != 0) {
      file_map_size = 1;
      for (const char * p = mapstr; *p != 0; ++p) {
	 if (*p == '&') ++file_map_size;
       }
    }
   file_map = new S6FileMap[file_map_size];

   if (file_map_size == 0) return;
   char * ms = (char *) malloc(strlen(mapstr)+1);
   strcpy(ms,mapstr);

   int i = 0;
   while (*ms != 0) {
      file_map[i].actual_name = ms;
      while (*ms != '>' && *ms != 0) ++ms;
      if (*ms == '>') *ms++ = 0;
      file_map[i].user_name = ms;
      while (*ms != '>' && *ms != 0) ++ms;
      if (*ms == '>') *ms++ = 0;
      file_map[i].map_type = *ms;
      while (*ms != 0 && *ms != '&') ++ms;
      if (*ms == '&') *ms++ = 0;
      ++i;
    }
}




void
S6::RUNCTXio_init()
{
   open_addr = (int (*)(const char *,int,mode_t)) RUNCTXfind_symbol("open");
   open64_addr = (int (*)(const char *,int,mode_t)) RUNCTXfind_symbol("open64");
   creat_addr = (int (*)(const char *,mode_t)) RUNCTXfind_symbol("creat");
   creat64_addr = (int (*)(const char *,mode_t)) RUNCTXfind_symbol("creat64");
   stat_addr = (int (*)(const char *,struct stat *)) RUNCTXfind_symbol("stat");
   xstat_addr = (int (*)(int,const char *,struct stat *)) RUNCTXfind_symbol("__xstat");
   stat64_addr = (int (*)(const char *,struct stat64 *)) RUNCTXfind_symbol("stat64");
   xstat64_addr = (int (*)(int,const char *,struct stat64 *)) RUNCTXfind_symbol("__xstat64");
   lstat_addr = (int (*)(const char *,struct stat *)) RUNCTXfind_symbol("lstat");
   lxstat_addr = (int (*)(int,const char *,struct stat *)) RUNCTXfind_symbol("__lxstat");

   io_inited = true;
}




/* end of runctxio.C */
