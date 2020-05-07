/********************************************************************************/
/*										*/
/*		SlimSecurityPolicy.java 					*/
/*										*/
/*	Java security policy imposed by Slim					*/
/*										*/
/********************************************************************************/
/*	Copyright 2003 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2003, Brown University, Providence, RI.				 *
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/slim/SlimSecurityPolicy.java,v 1.3 2015/09/23 17:58:12 spr Exp $ */


/*********************************************************************************
 *
 * $Log: SlimSecurityPolicy.java,v $
 * Revision 1.3  2015/09/23 17:58:12  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.2  2008-11-12 13:52:22  spr
 * Performance and bug updates.
 *
 * Revision 1.1  2008-06-12 18:33:30  spr
 * Add module for security checking.
 *
 *
 ********************************************************************************/




package edu.brown.cs.s6.slim;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;
import java.util.Map;
import java.util.WeakHashMap;

import edu.brown.cs.ivy.file.IvyFile;
import edu.brown.cs.ivy.file.IvyLog;


final public class SlimSecurityPolicy extends SecurityManager implements SlimConstants
{



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private SecurityManager parent_manager;
private CodeSource	code_source;
private SlimSecurityDomain base_domain;

private Map<Object,SlimSecurityDomain> security_map;

private SlimPermission []   junit_perms;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public SlimSecurityPolicy(String src)
{
   parent_manager = System.getSecurityManager();

   try {
      code_source = new CodeSource(new URL("file:/pro/s6/java"),(Certificate []) null);
    }
   catch (MalformedURLException e) {
      IvyLog.logE("SLIM","Bad source directory");
    }

   // These could be generated directly
   int idx = src.lastIndexOf(File.separator);
   String dir = src.substring(0,idx);
   String jur = IvyFile.expandName("$(S6)/lib/junit.jar");

   junit_perms = new SlimPermission[] {
      new SlimFilePermission(dir,"read"),
	 new SlimFilePermission(src + "-","read,write"),
	 new SlimFilePermission(jur,"read")
       };

   base_domain = new SlimSecurityDomain(null);
   base_domain.addPermission(new SlimAllPermission());

   security_map = new WeakHashMap<Object,SlimSecurityDomain>();
   security_map.put(AccessController.getContext(),base_domain);

   try {
      System.setSecurityManager(this);
    }
   catch (SecurityException e) {
      IvyLog.logE("SLIM","Can't set our own security manager: " + e.getMessage());
      System.exit(1);
    }
}



/********************************************************************************/
/*										*/
/*	Methods to do a call within a restricted environment			*/
/*										*/
/********************************************************************************/

public <T> T runMethod(SlimPermission [] perms,
			  PrivilegedExceptionAction<T> action) throws Exception
{
   if (perms == null) {
      return action.run();
    }

   AccessControlContext acc;

   // acc = new AccessControlContext(AccessController.getContext(), new SlimDomainCombiner());
   acc = buildContext(perms);

   if (acc == null) return null;

   try {
      return AccessController.doPrivileged(action,acc);
    }
   catch (PrivilegedActionException e) {
      Exception ex = e.getException();
      if (ex != null) throw ex;
      throw e;
    }
}



/********************************************************************************/
/*										*/
/*	Methods to set up the access control context from permissions		*/
/*										*/
/********************************************************************************/

private AccessControlContext buildContext(SlimPermission [] p)
{
   SlimSecurityDomain rsd = new SlimSecurityDomain(getSecurityDomain());

   for (int i = 0; i < p.length; ++i) rsd.addPermission(p[i]);
   for (int i = 0; i < junit_perms.length; ++i) rsd.addPermission(junit_perms[i]);

   PermissionCollection pc = new Permissions();
   pc.add(rsd.getLocalPermission());

   AccessControlContext acc = null;

   try {
      ProtectionDomain pd = new ProtectionDomain(code_source,pc,
						    this.getClass().getClassLoader(),null);
      pd = new ProtectionDomain(code_source,pc);

      acc = new AccessControlContext(new ProtectionDomain[] { pd });
      acc = new AccessControlContext(acc,new SlimDomainCombiner());
      synchronized (security_map) {
	 security_map.put(acc,rsd);
       }
    }
   catch (Throwable t) {
      IvyLog.logE("SLIM","Problem setting up security context: " + t);
    }

   return acc;
}



/********************************************************************************/
/*										*/
/*	Methods to find the security domain					*/
/*										*/
/********************************************************************************/

private SlimSecurityDomain getSecurityDomain()
{
   return getSecurityDomain(AccessController.getContext());
}




private SlimSecurityDomain getSecurityDomain(AccessControlContext acc)
{
   if (acc == null) return base_domain;

   SlimSecurityDomain sd = security_map.get(acc);
   if (sd == null) {
      synchronized (security_map) {
	 for (SlimSecurityDomain nsd : security_map.values()) {
	    try {
	       acc.checkPermission(nsd.getLocalPermission());
	       sd = nsd;
	       security_map.put(acc,sd);
	       break;
	     }
	    catch (SecurityException e) { }
	  }
       }
    }
   if (sd == null) {
      synchronized (security_map) {
	 sd = base_domain;
	 security_map.put(acc,sd);
       }
    }

   return sd;
}




/********************************************************************************/
/*										*/
/*	Basic security checking methods 					*/
/*										*/
/********************************************************************************/

public void checkPermission(Permission p,Object ctx)
{
   if (parent_manager != null) parent_manager.checkPermission(p,ctx);

   if (ctx instanceof AccessControlContext) {
      AccessControlContext acc = (AccessControlContext) ctx;
      SlimSecurityDomain rsd = getSecurityDomain(acc);
      if (!rsd.checkPermission(p)) throw new SecurityException("Access violation: " + p);
    }
   else throw new SecurityException("Attempt to check permissioon with bad context");
}




public void checkPermission(Permission p)
{
   if (parent_manager != null) parent_manager.checkPermission(p);

   SlimSecurityDomain rsd = getSecurityDomain();
   if (!rsd.checkPermission(p)) {
      throw new SecurityException("Access violation: " + p);
    }
}





}	// end of class SlimSecurityPolicy

