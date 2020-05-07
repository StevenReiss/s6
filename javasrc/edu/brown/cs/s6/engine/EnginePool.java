/********************************************************************************/
/*										*/
/*		EnginePool.java 						*/
/*										*/
/*	Intelligent thread pool implementation for search engine		*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/engine/EnginePool.java,v 1.10 2015/12/23 15:44:48 spr Exp $ */


/*********************************************************************************
 *
 * $Log: EnginePool.java,v $
 * Revision 1.10  2015/12/23 15:44:48  spr
 * Minor fixes.
 *
 * Revision 1.9  2015/09/23 17:57:55  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.8  2015/02/19 23:32:51  spr
 * Allow wait in a worker thread.
 *
 * Revision 1.7  2013/09/13 20:32:17  spr
 * Handle UI search.
 *
 * Revision 1.6  2013-05-09 12:26:17  spr
 * Minor changes to start ui fixups.
 *
 * Revision 1.5  2012-07-20 22:14:53  spr
 * Additions for UI search
 *
 * Revision 1.4  2012-06-11 14:07:29  spr
 * Code cleanup
 *
 * Revision 1.3  2009-09-18 01:40:41  spr
 * Handle user interaction.
 *
 * Revision 1.2  2009-05-12 22:27:23  spr
 * Add server to finder.  Fix up firewall recovery.  Fix max thread setting.
 *
 * Revision 1.1  2008-11-12 14:11:57  spr
 * Add our own thread pool class.
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.engine;


import java.text.DecimalFormat;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import edu.brown.cs.ivy.file.IvyLog;

import edu.brown.cs.s6.common.S6Constants;



class EnginePool implements S6Constants, EngineConstants {



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private Queue<Task>	compute_queue;
private Queue<Task>	io_queue;
private Queue<Task>	exec_queue;

private int		num_exec;
private int		num_io;
private int		num_compute;

private int		thread_count;
private int		ready_count;

private Object		queue_lock;

private static final DecimalFormat FORMAT = new DecimalFormat("00");

private static final long	RANDOM_VALUE = 10000;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

EnginePool(EngineMain em,int maxthread)
{
   Runtime rt = Runtime.getRuntime();
   num_compute = rt.availableProcessors()-2;

   if (maxthread < num_compute) num_compute = maxthread;
   if (num_compute <= 0) num_compute = 1;

   num_exec = num_compute * 3 / 2;
   num_io = num_compute * 6;
   if (em.doDebug() && num_compute == 1) num_io = 1;

   compute_queue = new PriorityQueue<Task>();
   io_queue = new PriorityQueue<Task>();
   exec_queue = new PriorityQueue<Task>();

   queue_lock = new Object();

   thread_count = 0;
   ready_count = 0;

   IvyLog.logI("ENGINE","Starting pool with " + num_compute + " " + num_exec + " " + num_io);
}



/********************************************************************************/
/*										*/
/*	Methods to add tasks							*/
/*										*/
/********************************************************************************/

FutureTask<Boolean> addTask(S6TaskType typ,Callable<Boolean> c)
{
   Task t = new Task(typ,c);

   enqueue(t);

   return t;
}



FutureTask<Boolean> addTask(S6TaskType typ,Runnable r)
{
   Task t = new Task(typ,r);

   enqueue(t);

   return t;
}




/********************************************************************************/
/*										*/
/*	Methods to wait for tasks						*/
/*										*/
/********************************************************************************/

boolean waitForAll(Queue<Future<Boolean>> waitq)
{
   boolean rslt = false;

   for ( ; ; ) {
      Future<Boolean> fb = null;
      synchronized (waitq) {
	 if (waitq.size() == 0) break;
	 fb = waitq.remove();
       }
      for ( ; ; ) {
	 try {
	    Task t = dequeue(fb);
	    if (t != null && !t.isDone()) {
	       t.run();
	     }
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

   return rslt;
}






/********************************************************************************/
/*										*/
/*	Actual queueing methods 						*/
/*										*/
/********************************************************************************/

private void enqueue(Task t)
{
   Queue<Task> queue = getQueueForTask(t);
   int ct = getCountForTask(t);

   synchronized (queue_lock) {
      if (ready_count == 0 && thread_count < ct) {
	 Worker w = new Worker(thread_count++);
	 w.start();
       }
      queue.add(t);
      queue_lock.notifyAll();
    }
}



private Task dequeue(Future<Boolean> fb)
{
   if (!(Thread.currentThread() instanceof Worker)) return null;
   if (!(fb instanceof Task)) return null;

   Task t = (Task) fb;
   Queue<Task> queue = getQueueForTask(t);
   synchronized (queue_lock) {
      if (queue.remove(t)) return t;
    }

   return null;
}



private Queue<Task> getQueueForTask(Task t)
{
   Queue<Task> q = null;

   switch (t.getTaskType()) {
      case COMPUTE :
	 q = compute_queue;
	 break;
      case EXEC :
	 q = exec_queue;
	 break;
      case IO :
	 q = io_queue;
	 break;
    }

   return q;
}




private int getCountForTask(Task t)
{
   int ct = 0;

   switch (t.getTaskType()) {
      case COMPUTE :
	 ct = num_compute;
	 break;
      case EXEC :
	 ct = num_exec;
	 break;
      case IO :
	 ct = num_io;
	 break;
    }

   return ct;
}




/********************************************************************************/
/*										*/
/*	Class holding a task							*/
/*										*/
/********************************************************************************/

private class Task extends FutureTask<Boolean> implements Comparable<Task> {

   private S6TaskType task_type;
   private long task_priority;

   Task(S6TaskType tt,Callable<Boolean> c) {
      super(c);
      task_type = tt;
      setPriority();
    }

   Task(S6TaskType tt,Runnable r) {
      super(r,Boolean.TRUE);
      task_type = tt;
      setPriority();
    }

   S6TaskType getTaskType()			{ return task_type; }

   private void setPriority() {
      long now = System.currentTimeMillis();
      task_priority = now + (long)(-RANDOM_VALUE * Math.log(Math.random())) + 1;
    }

   public int compareTo(Task t) {
      if (task_priority < t.task_priority) return -1;
      if (task_priority > t.task_priority) return 1;
      return 0;
    }

}	// end of subclass Task




/********************************************************************************/
/*										*/
/*	Class to hold worker thread						*/
/*										*/
/********************************************************************************/

private class Worker extends Thread implements IvyLog.LoggerThread {

   private int worker_index;

   Worker(int idx) {
      super("S6EnginePoolThread_" + FORMAT.format(idx));
      setDaemon(true);
      worker_index = idx;
    }

   @Override public int getLogId()                      { return worker_index; }
   
   @Override public void run() {
      for ( ; ; ) {
	 Task t = null;
	 while (t == null) {
	    synchronized (queue_lock) {
	       if (worker_index < num_compute) {
		  t = compute_queue.poll();
		}
	       if (t == null && worker_index < num_compute + num_exec) {
		  t = exec_queue.poll();
		}
	       if (t == null) {
		  t = io_queue.poll();
		}
	       if (t == null) {
		  try {
		     queue_lock.wait();
		   }
		  catch (InterruptedException e) { }
		}
	     }
  }
	 try {
	    t.run();
	  }
	 catch (Throwable e) {
	    IvyLog.logE("ENGINE","Problem running task: " + e,e);
	  }
       }
    }

}	// end of subclass Worker



}	// end of class EnginePool




/* end of EnginePool.java */
