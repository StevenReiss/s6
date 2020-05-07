/********************************************************************************/
/*										*/
/*		EngineMain.java 						*/
/*										*/
/*	Main program for S6 search engine processing				*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/engine/EngineMain.java,v 1.20 2016/07/18 23:04:53 spr Exp $ */


/*********************************************************************************
 *
 * $Log: EngineMain.java,v $
 * Revision 1.20  2016/07/18 23:04:53  spr
 * Handle applications better.
 *
 * Revision 1.19  2015/12/23 15:44:48  spr
 * Minor fixes.
 *
 * Revision 1.18  2015/09/23 17:57:55  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.17  2015/02/14 19:40:10  spr
 * Add test case generation.
 *
 * Revision 1.16  2014/08/29 15:15:52  spr
 * Updates for suise, testcases.
 *
 * Revision 1.15  2013/10/10 18:01:04  spr
 * Minor updates
 *
 * Revision 1.14  2013/09/13 20:32:17  spr
 * Handle UI search.
 *
 * Revision 1.13  2013-05-09 12:26:17  spr
 * Minor changes to start ui fixups.
 *
 * Revision 1.12  2012-08-13 16:50:31  spr
 * Add solution to fixDependencies
 *
 * Revision 1.11  2012-07-20 22:14:52  spr
 * Additions for UI search
 *
 * Revision 1.10  2012-06-20 12:21:23  spr
 * Initial fixes for UI search
 *
 * Revision 1.9  2012-06-11 14:07:29  spr
 * Code cleanup
 *
 * Revision 1.8  2009-09-18 01:40:40  spr
 * Handle user interaction.
 *
 * Revision 1.7  2009-05-12 22:27:23  spr
 * Add server to finder.  Fix up firewall recovery.  Fix max thread setting.
 *
 * Revision 1.6  2008-11-12 13:51:31  spr
 * Performance and bug updates.
 *
 * Revision 1.5  2008-08-28 00:32:49  spr
 * Next version of S6.	Lots of bug fixes, some new functionality.
 *
 * Revision 1.4  2008-07-18 22:26:26  spr
 * Do transformations so we can remove AST annotations.
 *
 * Revision 1.3  2008-07-17 13:46:19  spr
 * Remove unneeded solutions earlier.  Fix alltests.
 *
 * Revision 1.2  2008-06-12 17:47:48  spr
 * Next version of S6.
 *
 * Revision 1.1.1.1  2008-06-03 12:59:21  spr
 * Initial version of S6
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.engine;


import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.w3c.dom.Element;

import edu.brown.cs.cose.cosecommon.CoseResult;
import edu.brown.cs.cose.cosecommon.CoseSource;
import edu.brown.cs.ivy.exec.IvyExec;
import edu.brown.cs.ivy.file.IvyLog;
import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.ivy.xml.IvyXmlReader;
import edu.brown.cs.ivy.xml.IvyXmlReaderThread;
import edu.brown.cs.ivy.xml.IvyXmlWriter;
import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Engine;
import edu.brown.cs.s6.common.S6Exception;
import edu.brown.cs.s6.common.S6Factory;
import edu.brown.cs.s6.common.S6Fragment;
import edu.brown.cs.s6.common.S6Language;
import edu.brown.cs.s6.common.S6License;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6Solution;
import edu.brown.cs.s6.common.S6SolutionSet;
import edu.brown.cs.s6.common.S6TestCase;
import edu.brown.cs.s6.common.S6TestResults;


public class EngineMain implements S6Engine, S6Constants, EngineConstants {



/********************************************************************************/
/*										*/
/*	Main program								*/
/*										*/
/********************************************************************************/

public static void main(String [] args)
{
   EngineMain em = new EngineMain(args);

   em.start();
}



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private S6Factory	s6_factory;
private boolean 	run_server;
private boolean 	force_run;
private int		num_request;
private S6Language	s6_language;
private String		project_name;
private String		language_name;
private String		user_path;
private int		num_thread;
private EnginePool	exec_pool;
private ThreadPoolExecutor thread_pool;
private String		base_request;
private int		pool_counter;
private int		num_connect;
private boolean 	do_debug;
private AtomicLong	user_input_counter;
private Map<String,UserResponse> user_handlers;

private static boolean	local_pool = true;

static {
   IvyLog.setupLogging("S6",true);
   IvyLog.useStdErr(true);
   IvyLog.setLogLevel(IvyLog.LogLevel.INFO);
}




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public EngineMain(String [] args)
{
   run_server = false;
   force_run = false;
   num_request = -1;
   project_name = null;
   language_name = null;
   user_path = null;
   num_thread = 10;
   thread_pool = null;
   exec_pool = null;
   base_request = null;
   pool_counter = 0;
   num_connect = 10;
   do_debug = false;

   user_input_counter = new AtomicLong(0);
   user_handlers = new HashMap<String,UserResponse>();

   scanArgs(args);

   s6_factory = S6Factory.createS6Factory();
   s6_language = s6_factory.createLanguage(this,language_name);
   if (user_path != null) s6_language.setPath(user_path);
   if (project_name != null) s6_language.setProject(project_name);

   // The following code ensures IVY is loaded prior to any dyvise monitoring
   Element e = IvyXml.convertStringToXml("<TEST/>");
   IvyXml.getChildren(e);
   IvyXml.getElementsByTag(e,"TEST");

   IvyExec.usePolling(true);
}




/********************************************************************************/
/*										*/
/*	Argument scanning methods						*/
/*										*/
/********************************************************************************/

private void scanArgs(String [] args)
{
   for (int i = 0; i < args.length; ++i) {
      if (args[i].startsWith("-")) {
	 if (args[i].startsWith("-s")) {                                // -server [# connect]
	    run_server = true;
	    if (i+1 < args.length) {
	       try {
		  num_connect = Integer.parseInt(args[i+1]);
		  ++i;
		}
	       catch (NumberFormatException e) { }
	     }
	  }
	 else if (args[i].startsWith("-f")) {                           // -force
	    force_run = true;
	  }
	 else if (args[i].startsWith("-Xn") && i+1 < args.length) {     // -Xnumrequest
	    num_request = Integer.parseInt(args[++i]);
	  }
	 else if (args[i].startsWith("-p") && i+1 < args.length) {      // -project <proj>
	    project_name = args[++i];
	  }
	 else if (args[i].startsWith("-l") && i+1 < args.length) {      // -language <lang>
	    language_name = args[++i];
	  }
	 else if (args[i].startsWith("-cp") && i+1 < args.length) {     // -cp <path>
	    user_path = args[++i];
	  }
	 else if (args[i].startsWith("-t") && i+1 < args.length) {       // -t <threads>
	    try {
	       num_thread = Integer.parseInt(args[++i]);
	     }
	    catch (NumberFormatException e) {
	       badArgs();
	     }
	  }
	 else if (args[i].startsWith("-D")) {                           // -DEBUG
	    do_debug = true;
	    IvyLog.setLogLevel(IvyLog.LogLevel.DEBUG);
	  }
	 else badArgs();
       }
      else {
	 if (base_request == null) base_request = args[i];
	 else badArgs();
       }
    }

   if (run_server && base_request != null) badArgs();
}




private void badArgs()
{
   System.err.println("S6: ENGINE: s6engine");
   System.exit(1);
}



/********************************************************************************/
/*										*/
/*	Processing methods							*/
/*										*/
/********************************************************************************/

void start()
{
   if (run_server) {
      EngineServer es = new EngineServer();
      es.start();
    }
   else if (base_request != null) {
      IvyXmlReader xr = null;
      try {
	 xr = new IvyXmlReader(new FileReader(base_request));
	 for ( ; ; ) {
	    String nxtmsg = xr.readXml();
	    if (nxtmsg == null) break;
	    Element xml = IvyXml.convertStringToXml(nxtmsg,true);
	    if (xml == null) continue;
	    String result = handleCommand(xml);
	    System.out.println(result);
	  }
       }
      catch (IOException e) {
	 IvyLog.logE("ENGINE","I/O problem with request" + base_request + ": " + e);
       }
      catch (S6Exception e) {
	IvyLog.logE("ENGINE","Problem with request " + base_request + ": " + e);
       }
      catch (Throwable t) {
	 IvyLog.logE("ENGINE","Internal problem with request " + base_request,t);
       }
      catch (Throwable t) {
	 System.err.println("S6: ENGINE: Internal problem with request " + base_request);
	 t.printStackTrace();
       }
      finally {
	 try {
	    if (xr != null) xr.close();
	 }
	 catch (IOException e) { }
      }
    }
}




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override public boolean doDebug()		{ return do_debug; }

@Override public int getNumberOfSearchThreads()
{
   return num_thread;
}

public S6Factory getFactory()			{ return s6_factory; }


/********************************************************************************/
/*										*/
/*	Search request handling methods 					*/
/*										*/
/********************************************************************************/

@Override public String handleSearchRequest(Element xml) throws S6Exception
{
   if (xml == null) return null;

   S6Request.Search sr = s6_factory.createSearchRequest(this,xml);

   S6SolutionSet ss = s6_factory.createSolutionSet(sr);

   s6_factory.getInitialSolutions(ss);

   if (ss.getSearchType() == S6SearchType.TESTCASES) {
      for (S6Solution s : ss.getSolutions()) {
	 S6Fragment f = s.getFragment();
	 if (!f.checkSignature(sr.getSignature(),S6SignatureType.DOES_TEST)) {
	    s.setFlag(S6SolutionFlag.REMOVE);
	  }
       }
    }

   ss.pruneSolutions(S6_MAX_INITIAL);

   IvyLog.logI("ENGINE",ss.getSourceCount() + " sources used");

   outputSolutionCount(ss,"start");

   for ( ; ; ) {
      String s = processSolutions(ss);
      if (s != null) return s;
      if (ss.getSolutionCount() >= S6_MIN_SOLUTION) break;
      if (sr.getSearchType() == S6SearchType.TESTCASES &&
	    ss.getSolutionCount() > 0) {
	 break;
       }

      if (!ss.restoreSolutions(S6_MAX_ACTIVE)) break;
      outputSolutionCount(ss,"restore");
    }

   if (sr.getFormatType() != S6FormatType.NONE) {
      s6_language.formatCode(sr,ss);
    }

   IvyXmlWriter xw = new IvyXmlWriter();

   ss.output(xw);

   s6_language.finish(sr);

   return xw.toString();
}



private String processSolutions(S6SolutionSet ss)
{
   handleTransforms(ss,S6TransformType.INITIAL,S6_MAX_INITIAL);

   outputSolutionCount(ss,"initial");
   ss.saveCounts("INITIAL");

   handleTransforms(ss,S6TransformType.NORMAL,S6_MAX_ACTIVE);

   outputSolutionCount(ss,"transformed");

   handleDependencies(ss);

   outputSolutionCount(ss,"dependent");
   ss.saveCounts("DEPENDENT");

   ss.pruneSolutions(S6_MAX_TEST);
   outputSolutionCount(ss,"to_test");

   handleTestCases(ss);

   outputSolutionCount(ss,"tested");

   String s = handleUserFeedback(ss);
   if (s != null) return s;

   handleTransforms(ss,S6TransformType.FINAL,S6_MAX_ACTIVE);
   outputSolutionCount(ss,"final");

   return null;
}



private void outputSolutionCount(S6SolutionSet ss,String when)
{
   int ct = ss.getSolutionCount();
   int rct = ss.getNumberRemoved();
   int tct = ct + rct;

   IvyLog.logI("ENGINE","SOLUTIONS: " + ct + " (" + rct + ") = [" + tct + "] " + when);
}




/********************************************************************************/
/*										*/
/*	Check request handling methods						*/
/*										*/
/********************************************************************************/

@Override public String handleCheckRequest(Element xml) throws S6Exception
{
   if (xml == null) return null;

   IvyXmlWriter xw = new IvyXmlWriter();
   S6Request.Check creq = s6_factory.createCheckRequest(this,xml);

   switch (creq.getCheckType()) {
      default :
	 s6_language.checkInput(creq,xw);
	 break;
      case LICENSE :
	 String uid = creq.getUserInput("UID");
	 S6License lc = s6_factory.createLicenseManager();
	 xw.begin("LICENSE");
	 xw.cdataElement("TEXT",lc.getLicense(uid));
	 xw.end("LICENSE");
	 break;
    }

   return xw.toString();
}




/********************************************************************************/
/*										*/
/*	Format request handling methods 					*/
/*										*/
/********************************************************************************/

public String handleFormatRequest(Element xml) throws S6Exception
{
   if (xml == null) return null;

   IvyXmlWriter xw = new IvyXmlWriter();
   S6Request.Format freq = s6_factory.createFormatRequest(this,xml);

   s6_language.formatCode(freq,xw);

   return xw.toString();
}



/********************************************************************************/
/*										*/
/*	File request handling methods						*/
/*										*/
/********************************************************************************/

public String handleFileRequest(Element xml) throws S6Exception
{
   String ext = IvyXml.getAttrString(xml,"EXTENSION");
   if (ext == null) ext = ".data";
   boolean embed = IvyXml.getAttrBool(xml,"EMBED");
   int len = IvyXml.getAttrInt(xml,"LENGTH");

   File tdir = new File(S6_FILE_DIRECTORY);
   if (!tdir.exists()) tdir.mkdirs();

   String cnts = IvyXml.getTextElement(xml,"CONTENTS");
   if (!embed) {
      cnts = cnts.replace("\n","");
      cnts = cnts.replace("\r","");
    }
   if (len <= 0) len = cnts.length()/2;
   int pos = 0;

   if (embed) {
      for ( ; pos < len; ++pos) {
	 int c0 = Character.digit(cnts.charAt(2*pos),16);
	 int c1 = Character.digit(cnts.charAt(2*pos+1),16);
	 int ch = (((c0 & 0xf) << 4) + (c1 & 0xf));
	 if (ch == '\n') break;
       }
      pos++;
      len = len - pos - 4;
      StringBuffer hdr = new StringBuffer();
      int state = 0;
      for ( ; pos < len; ++pos) {
	 int c0 = Character.digit(cnts.charAt(2*pos),16);
	 int c1 = Character.digit(cnts.charAt(2*pos+1),16);
	 int ch = (((c0 & 0xf) << 4) + (c1 & 0xf));
	 hdr.append((char) ch);
	 if (state == 0 && ch == '\r') state = 1;
	 else if (state == 1 && ch == '\n') state = 2;
	 else if (state == 2 && ch == '\r') state = 3;
	 else if (state == 3 && ch == '\n') break;
	 else state = 0;
       }
      ++pos;
      String hdrs = hdr.toString();
      int idx = hdrs.indexOf("filename=");
      if (idx >= 0) {
	 int idx1 = hdrs.indexOf('"',idx);
	 int idx2 = hdrs.indexOf('"',idx1+1);
	 String fnm = hdrs.substring(idx1+1,idx2);
	 int idx3 = fnm.indexOf(".");
	 if (idx3 > 0) ext = fnm.substring(idx3);
       }
    }

   try {
      File tmp = File.createTempFile("s6data",ext,tdir);
      BufferedOutputStream ots = new BufferedOutputStream(new FileOutputStream(tmp));

      for ( ; pos < len; ++pos) {
	 int c0 = Character.digit(cnts.charAt(2*pos),16);
	 int c1 = Character.digit(cnts.charAt(2*pos+1),16);
	 byte b = (byte)(((c0 & 0xf) << 4) + (c1 & 0xf));
	 ots.write(b);
       }

      ots.close();

      return tmp.getPath();
    }
   catch (IOException e) {
      throw new S6Exception("Problem creating user file",e);
    }
}




/********************************************************************************/
/*										*/
/*	User reply requests							*/
/*										*/
/********************************************************************************/

public String handleUserReply(Element xml) throws S6Exception
{
   String uid = IvyXml.getAttrString(xml,"UID");
   UserResponse ur = null;
   synchronized (user_handlers) {
      ur = user_handlers.remove(uid);
    }

   if (ur == null) throw new S6Exception("User reply for unknown UID");

   S6SolutionSet ss = ur.getSolutionSet();
   S6Request.Search sr = ss.getRequest();

   for (Element tc : IvyXml.children(xml,"TESTCASE")) {
      Map<String,S6SolutionFlag> status = new HashMap<String,S6SolutionFlag>();
      String nm = IvyXml.getAttrString(tc,"NAME");
      for (Element rc : IvyXml.children(tc,"USERCASE")) {
	 S6SolutionFlag sf = IvyXml.getAttrEnum(rc,"STATE",S6SolutionFlag.FAIL);
	 String rslt = IvyXml.getTextElement(rc,"IDS");
	 StringTokenizer tok = new StringTokenizer(rslt," ,;");
	 while (tok.hasMoreTokens()) {
	    String id = tok.nextToken();
	    status.put(id,sf);
	  }
       }
      for (S6Solution sol : ss.getSolutions()) {
	 S6SolutionFlag sf = status.get(sol.getId());
	 S6Fragment fg = sol.getFragment();
	 S6TestResults tr = fg.getTestResults();
	 tr.setTestStatus(nm,sf);
       }
    }

   for (S6Solution sc : ss) {
      S6Fragment sf = sc.getFragment();
      S6TestResults tr = sf.getTestResults();
      sc.clearFlag(S6SolutionFlag.FAIL);
      sc.clearFlag(S6SolutionFlag.PASS);
      sc.clearFlag(S6SolutionFlag.USER);
      sc.setFlag(tr.getSummaryResult());
      if (sc.checkFlag(S6SolutionFlag.PASS)) ;
      else if (sc.checkFlag(S6SolutionFlag.USER)) ;
      else if (sc.checkFlag(S6SolutionFlag.FAIL)) ss.remove(sc);
    }

   String s = handleUserFeedback(ss);
   if (s != null) return s;

   if (sr.getFormatType() != S6FormatType.NONE) {
      s6_language.formatCode(sr,ss);
    }

   IvyXmlWriter xw = new IvyXmlWriter();

   ss.output(xw);

   s6_language.finish(sr);

   return xw.toString();
}




/********************************************************************************/
/*										*/
/*	Methods to handle language dependencies 				*/
/*										*/
/********************************************************************************/

@Override public S6Fragment createFragment(CoseResult cr,S6Request.Search sreq)
{
   return s6_language.createCoseFragment(cr,sreq);
}



@Override public S6Fragment createFileFragment(String text,CoseSource src,S6Request.Search sreq)
{
   return s6_language.createFileFragment(text,src,sreq);
}


@Override public Set<String> getRelatedProjects(S6Fragment src)
{
   return s6_language.getRelatedProjects(src);
}


@Override public Set<String> getUsedProjects(S6Fragment src)
{
   return s6_language.getUsedProjects(src);
}


@Override public S6Fragment createPackageFragment(S6Request.Search sr)
{
   return s6_language.createPackageFragment(sr);
}




/********************************************************************************/
/*										*/
/*	Methods for concurrent tasks						*/
/*										*/
/********************************************************************************/

@Override public Future<Boolean> executeTask(S6TaskType tt,Callable<Boolean> c)
{
   startPool();

   FutureTask<Boolean> ft = null;

   if (thread_pool != null) {
      ft = new FutureTask<Boolean>(c);
      thread_pool.execute(ft);
    }
   else if (exec_pool != null) {
      ft = exec_pool.addTask(tt,c);
    }

   return ft;
}


@Override public Future<Boolean> executeTask(S6TaskType tt,Runnable r)
{
   startPool();

   FutureTask<Boolean> ft = null;

   if (thread_pool != null) {
      ft = new FutureTask<Boolean>(r,Boolean.TRUE);
      thread_pool.execute(ft);
    }
   else if (exec_pool != null) {
      ft = exec_pool.addTask(tt,r);
    }

   return ft;
}


private synchronized void startPool()
{
   if (local_pool) {
      if (exec_pool != null) return;
      exec_pool = new EnginePool(this,num_thread);
    }
   else {
      if (thread_pool != null) return;
      IvyLog.logI("ENGINE","STARTING THREAD POOL WITH " + num_thread);
      thread_pool = new ThreadPoolExecutor(num_thread,num_thread,10,TimeUnit.MINUTES,
					      new LinkedBlockingQueue<Runnable>(),
					      new PoolFactory());
    }
}


private synchronized String getPoolThreadName()
{
   return "S6EnginePoolThread_" + (++pool_counter);
}



@Override public boolean waitForAll(Queue<Future<Boolean>> waitq)
{
   boolean rslt = false;

   if (exec_pool != null) {
      rslt = exec_pool.waitForAll(waitq);
    }
   else if (thread_pool != null) {
      for ( ; ; ) {
	 Future<Boolean> fb = null;
	 synchronized (waitq) {
	    if (waitq.size() == 0) break;
	    fb = waitq.remove();
	  }
	 while (fb != null) {
	    try {
	       Boolean bv = fb.get();
	       if (bv != null) rslt |= bv.booleanValue();
	       break;
	     }
	    catch (InterruptedException e) { }
	    catch (ExecutionException e) {
	       IvyLog.logE("ENGINE","Problem with runnable: " + fb + ": " + e);
	       e.printStackTrace();
	       break;
	     }
	  }
       }
    }

   return rslt;
}




private class PoolFactory implements ThreadFactory {

   public Thread newThread(Runnable r) {
      Thread th = new Thread(r,getPoolThreadName());
      th.setDaemon(true);
      return th;
    }

}	// end of subclass PoolFactory



/********************************************************************************/
/*										*/
/*	Transformation methods							*/
/*										*/
/********************************************************************************/

private void handleTransforms(S6SolutionSet ss,S6TransformType tt,int max)
{
   Queue<Future<Boolean>> waits = new LinkedList<Future<Boolean>>();

   for (S6Solution sol : ss) {
      sol.clearFlag(S6SolutionFlag.DONE_TRANSFORM);
    }

   int pass = 0;
   boolean chng = true;
   while (chng) {
      chng = false;

      for (S6Solution sol : ss) {
	 if (!sol.checkFlag(S6SolutionFlag.DONE_TRANSFORM)) {
	    if (tt == S6TransformType.FINAL || !sol.checkFlag(S6SolutionFlag.PASS)) {
	       sol.setFlag(S6SolutionFlag.DONE_TRANSFORM);
	       Future<Boolean> fb = executeTask(S6TaskType.COMPUTE,new TransformSolution(ss,sol,tt));
	       synchronized (waits) {
		  waits.add(fb);
		}
	     }
	  }
       }

      chng = waitForAll(waits);

      ss.pruneSolutions(max);

      outputSolutionCount(ss,"Transform_" + tt + "(" + pass + ")");
      ++pass;
    }
}



private boolean checkViable(S6SolutionSet ss,S6Solution s,S6TransformType tt)
{
   if (s.checkFlag(S6SolutionFlag.REMOVE)) return false;

   List<String> rq = ss.getRequest().getRequiredWords();
   if (rq != null && rq.size() > 0) {
      String s3 = s.getFragment().getText();
      for (String rwd : rq) {
	 if (!s3.contains(rwd)) return false;
       }
    }

   S6Fragment f = s.getFragment();
   if (tt == S6TransformType.INITIAL) {
      return f.checkInitial(ss.getRequest().getSignature());
    }

   S6SignatureType chk = S6SignatureType.FULL;
   switch (ss.getSearchType()) {
      case PACKAGE :
      case APPLICATION :
	 chk = S6SignatureType.PACKAGE_FULL;
	 break;
      case UIFRAMEWORK :
      case ANDROIDUI :
	 chk = S6SignatureType.UIFRAMEWORK_FULL;
	 break;
      case TESTCASES :
	 chk = S6SignatureType.TESTING_FULL;
	 break;
      default :
	 break;
    }

   if (!f.checkSignature(ss.getRequest().getSignature(),chk))
      return false;

   if (s.checkFlag(S6SolutionFlag.FAIL))
      return false;

   if (!s.checkFlag(S6SolutionFlag.COMPILES))
      return false;

   return true;
}



private class TransformSolution implements Callable<Boolean> {

   private S6SolutionSet solution_set;
   private S6Solution for_solution;
   private S6TransformType transform_type;
   private String solution_id;

   TransformSolution(S6SolutionSet ss,S6Solution sol,S6TransformType tt) {
      solution_set = ss;
      for_solution = sol;
      solution_id = sol.getId();
      transform_type = tt;
    }

   public Boolean call() {
      boolean chng = false;

      for_solution.resolve();

      chng = s6_language.applyTransforms(solution_set,for_solution,transform_type);

      // for (S6Transform s6 : s6_language.getTransforms()) {
	 // if (s6.isUsedFor(transform_type)) {
	    // chng |= s6.findTransforms(solution_set,for_solution);
	  // }
       // }

      if (!checkViable(solution_set,for_solution,transform_type)) {
	 if (IvyLog.isDebug()) {
	    IvyLog.logD("ENGINE","REMOVE SOLUTION " + for_solution + " " + transform_type);
	  }
	 solution_set.remove(for_solution);
	 for_solution.clearResolve();
       }

      if (solution_set.checkClearResolve())
	 for_solution.clearResolve();

      for_solution = null;

      return chng;
    }

   public String toString() {
      return "TRANSFORM:" + transform_type + ":" + solution_id + ":" +
	 for_solution.getSource().toString();
    }

}	// end of TransformSolution




/********************************************************************************/
/*										*/
/*	Methods to handle adding dependencies					*/
/*										*/
/********************************************************************************/

private void handleDependencies(S6SolutionSet ss)
{
   boolean chng = true;

   while (chng) {
      chng = false;
      Queue<Future<Boolean>> waitq = new LinkedList<Future<Boolean>>();

      for (S6Solution sc : ss) {
	 if (!sc.checkFlag(S6SolutionFlag.DEPEND_PASS) && !sc.checkFlag(S6SolutionFlag.DEPEND_FAIL)) {
	    sc.clearFlag(S6SolutionFlag.KEEP);
	    DependWorker dw = new DependWorker(ss,sc);
	    Future<Boolean> fb = executeTask(S6TaskType.COMPUTE,dw);
	    synchronized (waitq) {
	       waitq.add(fb);
	     }
	  }
       }

      waitForAll(waitq);

      handleTransforms(ss,S6TransformType.DETAIL,S6_MAX_ACTIVE);

      for (S6Solution sc : ss) {
	 if (sc.checkFlag(S6SolutionFlag.DEPEND_PASS)) ;
	 else if (sc.checkFlag(S6SolutionFlag.DEPEND_FAIL)) {
	    if (!sc.checkFlag(S6SolutionFlag.KEEP)) {
	       if (ss.checkClearResolve()) sc.clearResolve();
	       ss.remove(sc);
	     }
	  }
	 else chng = true;
       }
    }
}



private class DependWorker implements Runnable {

   private S6SolutionSet solution_set;
   private S6Solution for_solution;
   private String solution_id;

   DependWorker(S6SolutionSet solset,S6Solution sol) {
      solution_set = solset;
      for_solution = sol;
      solution_id = sol.getId();
    }

   public void run() {
      S6Fragment f = for_solution.getFragment();
      f.makeLocal(solution_set);		// does a resolve
      f.resolveFragment();

      if (!f.checkSignature(solution_set.getRequest().getSignature(),S6SignatureType.FULL)) {
	 for_solution.clearResolve();
	 solution_set.remove(for_solution);
	 return;
       }

      if (!f.fixDependencies(solution_set,for_solution)) {
	 for_solution.setFlag(S6SolutionFlag.DEPEND_FAIL);
	 for_solution.clearResolve();
       }
      else {
	 for_solution.setFlag(S6SolutionFlag.DEPEND_PASS);
       }

      if (solution_set.checkClearResolve()) for_solution.clearResolve();

      for_solution = null;
    }

   public String toString() {
      return "DEPEND:" + solution_id;
    }

}	// end of subclass DependWorker





/********************************************************************************/
/*										*/
/*	Methods to handle testing						*/
/*										*/
/********************************************************************************/

private void handleTestCases(S6SolutionSet ss)
{
   boolean chng = true;

   while (chng) {
      chng = false;
      Queue<Future<Boolean>> waitq = new LinkedList<Future<Boolean>>();

      for (S6Solution sc : ss) {
	 if (!sc.checkFlag(S6SolutionFlag.PASS) && !sc.checkFlag(S6SolutionFlag.FAIL)) {
	    sc.clearFlag(S6SolutionFlag.KEEP);
	    TestWorker tw = new TestWorker(ss,sc);
	    Future<Boolean> fb = executeTask(S6TaskType.EXEC,tw);
	    synchronized (waitq) {
	       waitq.add(fb);
	     }
	  }
       }

      waitForAll(waitq);

      handleTransforms(ss,S6TransformType.TEST,S6_MAX_ACTIVE);

      for (S6Solution sc : ss) {
	 if (sc.checkFlag(S6SolutionFlag.PASS)) ;
	 else if (sc.checkFlag(S6SolutionFlag.USER)) ;
	 else if (sc.checkFlag(S6SolutionFlag.FAIL)) {
	    if (!sc.checkFlag(S6SolutionFlag.KEEP)) {
	       if (ss.checkClearResolve()) sc.clearResolve();
	       ss.remove(sc);
	     }
	  }
	 else chng = true;
       }

      if (chng) handleDependencies(ss);
    }
}




private class TestWorker implements Runnable {

   private S6SolutionSet solution_set;
   private S6Solution for_solution;
   private String solution_id;

   TestWorker(S6SolutionSet sset,S6Solution sol) {
      solution_set = sset;
      for_solution = sol;
      solution_id = sol.getId();
    }

   public void run() {
      S6Fragment f = for_solution.getFragment();
      for_solution.resolve();

      if (IvyLog.isDebug()) {
	 IvyLog.logD("ENGINE","TEST " + f.getText());
	 IvyLog.logD("ENGINE","SOURCE: " + for_solution.getSource().getName());
       }

      S6SolutionFlag sf = f.checkTestCases(solution_set.getRequest(),for_solution.getSource());
      if (sf == S6SolutionFlag.PASS) IvyLog.logD("ENGINE","TEST PASSED");
      else IvyLog.logD("ENGINE","TEST FAILED");

      for_solution.clearFlag(S6SolutionFlag.FAIL);
      for_solution.clearFlag(S6SolutionFlag.PASS);
      for_solution.clearFlag(S6SolutionFlag.USER);
      for_solution.setFlag(sf);

      if (solution_set.checkClearResolve()) for_solution.clearResolve();

      for_solution = null;
    }

   public String toString() {
      return "TestWorker:" + solution_id;
    }

}	// end of subclass TestWorker





/********************************************************************************/
/*										*/
/*	Methods to handle user feedback 					*/
/*										*/
/********************************************************************************/

private String handleUserFeedback(S6SolutionSet ss)
{
   S6Request.Search sr = ss.getRequest();
   S6Request.Tests st = sr.getTests();

   List<String> testcases = new ArrayList<String>();

   for (S6TestCase tc : st.getTestCases()) {
      if (tc.getNeedsUserInput()) testcases.add(tc.getName());
    }

   if (testcases.isEmpty()) return null;

   List<S6Solution> feedbacks = new ArrayList<S6Solution>();

   for (S6Solution sc : ss) {
      if (sc.checkFlag(S6SolutionFlag.USER)) {
	 feedbacks.add(sc);
       }
    }

   if (feedbacks.isEmpty()) return null;

   IvyXmlWriter xw = new IvyXmlWriter();
   xw.begin("USERINPUT");
   String uid = "UID" + user_input_counter.incrementAndGet();
   xw.field("UID",uid);

   for (String tcn : testcases) {
      xw.begin("TESTCASE");
      xw.field("NAME",tcn);
      Set<S6Solution> done = new HashSet<S6Solution>();
      for (S6Solution sc : feedbacks) {
	 if (done.contains(sc)) continue;
	 done.add(sc);
	 S6Fragment fg = sc.getFragment();
	 S6TestResults tr = fg.getTestResults();
	 String ut = tr.getUserType(tcn);
	 String uv = tr.getUserValue(tcn);
	 byte [] jarf = tr.getJarFile(tcn);
	 xw.begin("USERCASE");
	 String sols = sc.getId();
	 for (S6Solution xsc : feedbacks) {
	    if (done.contains(xsc)) continue;
	    S6Fragment xfg = xsc.getFragment();
	    S6TestResults xtr = xfg.getTestResults();
	    String xut = xtr.getUserType(tcn);
	    String xuv = xtr.getUserValue(tcn);
	    if (ut.equals(xut) && uv.equals(xuv)) {
	       done.add(xsc);
	       sols += "," + xsc.getId();
	       if (jarf == null) jarf = xtr.getJarFile(tcn);
	     }
	  }
	 xw.field("IDS",sols);
	 xw.field("TYPE",ut);
	 xw.cdataElement("VALUE",uv);
	 if (jarf != null) {
	    try {
	       String d = Base64.getEncoder().encodeToString(jarf);
	       xw.textElement("RUNJAR",d);
	     }
	    catch (IllegalArgumentException e) { }
	  }
	 xw.end("USERCASE");
       }
      xw.end("TESTCASE");
    }

   xw.end("USERINPUT");

   UserResponse ur = new UserResponse(uid,ss);
   synchronized (user_handlers) {
      user_handlers.put(uid,ur);
    }

   String rslt = xw.toString();
   xw.close();
   return rslt;
}



private class UserResponse {

   private S6SolutionSet solution_set;
   private long created_at;

   UserResponse(String uid,S6SolutionSet ss) {
      solution_set = ss;
      created_at = System.currentTimeMillis();
    }

   S6SolutionSet getSolutionSet()		{ return solution_set; }

   boolean isExpired() {
      long time = System.currentTimeMillis() - created_at;
      return time > 60*60*1000;
    }

}	// end of subclass UserResponse




/********************************************************************************/
/*										*/
/*	Engine Client management						*/
/*										*/
/********************************************************************************/

private EngineClient createClient(Socket s)
{
   try {
      EngineClient c = new EngineClient(s);
      c.start();
      return c;
    }
   catch (IOException e) {
      IvyLog.logE("ENGINE","Problem create server client: " + e);
    }

   return null;
}




/********************************************************************************/
/*										*/
/*	Provide a port for handling requests					*/
/*										*/
/********************************************************************************/

private class EngineServer extends Thread {

   private ServerSocket server_socket;
   private Collection<EngineClient> firewall_clients;

   EngineServer() {
      super("S6_ENGINE_ACCEPT");
      firewall_clients = new ArrayList<EngineClient>();
      try {
	 server_socket = new ServerSocket(0);
       }
      catch (IOException e) {
	 IvyLog.logE("ENGINE","Problem creating server socket: " + e);
	 System.exit(1);
       }
      Timer t = new Timer("UserResponseTimer");
      t.schedule(new ResponseChecker(),300*1000);
    }

   public void run() {
      File f = new File(S6_ENGINE_SERVER_SOCKET);

      try {
	 if (!force_run) {
	    while (!f.createNewFile()) {
	       try {
		  BufferedReader br = new BufferedReader(new FileReader(f));
		  String s = br.readLine();
		  br.close();
		  if (s != null) {
		     StringTokenizer tok = new StringTokenizer(s);
		     String host = tok.nextToken();
		     int port = Integer.parseInt(tok.nextToken());
		     Socket xs = new Socket(host,port);
		     xs.close();
		     IvyLog.logE("ENGINE","ENGINE: Server already Running");
		     return;
		   }
		}
	       catch (Throwable t) {
		  IvyLog.logW("ENGINE","ENGINE: Server lock file seems bad: " + t);

		  f.delete();
		}
	     }
	  }
	 FileWriter fw = new FileWriter(f);
	 PrintWriter pw = new PrintWriter(fw);
	 InetAddress iad = InetAddress.getLocalHost();
	 pw.println(iad.getHostName() + "\t" + server_socket.getLocalPort());
	 pw.close();
	 f.deleteOnExit();
	 IvyLog.logI("ENGINE","Engine running on port " + server_socket.getLocalPort());
       }
      catch (IOException e) {
	 IvyLog.logE("ENGINE","Problem creationg server socket file: " + e);
	 System.exit(1);
       }

      for (int i = 0; i < num_connect; ++i) {
	 setupFirewallClient();
       }

      try {
	 server_socket.setSoTimeout(1*60*1000);
	 for ( ; ; ) {
	    try {
	       Socket s = server_socket.accept();
	       createClient(s);
	       if (num_request > 0) {
		  --num_request;
		  if (num_request == 0) break;
		}
	     }
	    catch (SocketTimeoutException e) { }
	    checkFirewallClients();
	  }
       }
      catch (IOException e) {
	 IvyLog.logE("ENGINE","Problem with server socket accept: " + e);
       }

      IvyLog.logI("ENGINE","Exiting");

      f.delete();
    }

   private void setupFirewallClient() {
      try {
	 Socket s = new Socket(S6_ENGINE_WEB_SERVER,S6_ENGINE_WEB_PORT);
	 s.setSoTimeout(2*60*1000);
	 EngineClient ec = createClient(s);
	 if (ec != null) firewall_clients.add(ec);
       }
      catch (IOException e) {
	 IvyLog.logE("ENGINE","Firewall connection not running: " + e);
       }
    }

   private void checkFirewallClients() {
      int ct = 0;
      for (Iterator<EngineClient> it = firewall_clients.iterator(); it.hasNext(); ) {
	 EngineClient ec = it.next();
	 if (ec.isAlive() && ec.isActive()) ++ct;
	 else it.remove();
       }
      if (ct < num_connect) {
	 IvyLog.logI("ENGINE","Reconnecting to firewall " + (num_connect-ct));
	 for (int i = ct; i < num_connect; ++i) {
	    setupFirewallClient();
	  }
       }
    }

}	// end of subclass EngineServer




private class ResponseChecker extends TimerTask {

   @Override public void run() {
       synchronized (user_handlers) {
	  for (Iterator<UserResponse> it = user_handlers.values().iterator(); it.hasNext(); ) {
	     UserResponse uh = it.next();
	     if (uh.isExpired()) it.remove();
	   }
	}
    }

}	// end of inner class ResponseChecker





/********************************************************************************/
/*										*/
/*	Client handler								*/
/*										*/
/********************************************************************************/

private String handleCommand(Element xml) throws S6Exception
{
   IvyLog.logI("ENGINE","Process client command: " + IvyXml.convertXmlToString(xml));

   if (IvyXml.isElement(xml,"SEARCH")) {
      return handleSearchRequest(xml);
    }
   else if (IvyXml.isElement(xml,"PING")) {
      return "<PONG />";
    }
   else if (IvyXml.isElement(xml,"CHECK")) {
      return handleCheckRequest(xml);
    }
   else if (IvyXml.isElement(xml,"FORMAT")) {
      return handleFormatRequest(xml);
    }
   else if (IvyXml.isElement(xml,"FILE")) {
      return handleFileRequest(xml);
    }
   else if (IvyXml.isElement(xml,"USERREPLY")) {
      return handleUserReply(xml);
    }
   else {
      IvyLog.logE("ENGINE","COMMAND NOT FOUND: " + IvyXml.convertXmlToString(xml));
    }

   return null;
}



private class EngineClient extends IvyXmlReaderThread {

  private Socket client_socket;
  private PrintWriter print_writer;
  private long	 last_used;


  EngineClient(Socket s) throws IOException {
     super("EngineClient_" + s.getRemoteSocketAddress() + "_" + s.getLocalSocketAddress(),
	      new InputStreamReader(s.getInputStream()));
     client_socket = s;
     print_writer = new PrintWriter(s.getOutputStream());
     last_used = System.currentTimeMillis();
   }

  protected void processXmlMessage(String msg) {
     Element xml = IvyXml.convertStringToXml(msg,true);
     last_used = System.currentTimeMillis();

     StringBuffer buf = new StringBuffer();
     try {
	String rslt = handleCommand(xml);
	startElement("RESULT",buf);
	if (rslt != null) buf.append(rslt);
	endElement("RESULT",buf);
      }
     catch (S6Exception e) {
	startElement("PROBLEM",buf);
	buf.append(e.getMessage());
	endElement("PROBLEM",buf);
      }
     catch (Throwable t) {
	startElement("ERROR",buf);
	buf.append(t.toString());
	endElement("ERROR",buf);
      }

     sendReply(buf.toString());
   }

  protected synchronized void processDone() {
     if (client_socket == null) return;
     try {
	client_socket.close();
	client_socket = null;
      }
     catch (IOException e) { }
   }

  protected void processIoError(IOException e) {
     IvyLog.logE("ENGINE","XML reader error for " + getName() + ": " + e);
   }

  boolean isActive() {
     long delta = System.currentTimeMillis() - last_used;
     if (delta < 30*60*1000) return true;
     processDone();
     return false;
   }

  void sendReply(String msg) {
     print_writer.print(msg);
     print_writer.flush();
     IvyLog.logI("ENGINE","REPLY: " + msg);
   }

  private void startElement(String name,StringBuffer buf) {
     buf.append("<" + name + ">");
   }

  private void endElement(String name,StringBuffer buf) {
     buf.append("</" + name + ">\n");
   }

}	// end of subclass EngineClient



}	// end of class EngineMain




/* end of EngineMain.java */
