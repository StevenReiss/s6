/********************************************************************************/
/*										*/
/*		EngineFirewall.java						*/
/*										*/
/*	Program to run on web server outside firewall to connect to S6 engine	*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/engine/EngineFirewall.java,v 1.8 2015/09/23 17:57:55 spr Exp $ */


/*********************************************************************************
 *
 * $Log: EngineFirewall.java,v $
 * Revision 1.8  2015/09/23 17:57:55  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.7  2015/02/14 19:40:10  spr
 * Add test case generation.
 *
 * Revision 1.6  2013/09/13 20:32:17  spr
 * Handle UI search.
 *
 * Revision 1.5  2013-05-09 12:26:16  spr
 * Minor changes to start ui fixups.
 *
 * Revision 1.4  2009-05-12 22:27:23  spr
 * Add server to finder.  Fix up firewall recovery.  Fix max thread setting.
 *
 * Revision 1.3  2008-11-12 13:51:31  spr
 * Performance and bug updates.
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


import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import edu.brown.cs.ivy.xml.IvyXmlReader;
import edu.brown.cs.ivy.xml.IvyXmlReaderThread; 										



public class EngineFirewall {



/********************************************************************************/
/*										*/
/*	Main program								*/
/*										*/
/********************************************************************************/

public static void main(String [] args)
{
   EngineFirewall ef = new EngineFirewall(args);

   ef.start();
}



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private int		engine_port;
private int		client_port;
private Random		random_gen;
private List<EngineClient> engine_queue;
private Set<EngineClient>  active_clients;
private Timer		ping_timer;

private static final int	SOCKET_TIMEOUT = 5*60*1000;
private static final int	SOCKET_TIMEOUT_CHECK = 10*1000;

private static final long	PING_DELAY = 10*60*1000;

private final static int S6_ENGINE_WEB_PORT = 17240;
private final static int S6_ENGINE_WEB_CLIENT_PORT = 17241;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

private EngineFirewall(String [] args)
{
   engine_port = S6_ENGINE_WEB_PORT;
   client_port = S6_ENGINE_WEB_CLIENT_PORT;
   engine_queue = new LinkedList<EngineClient>();
   active_clients = new HashSet<EngineClient>();
   random_gen = new Random();

   ping_timer = new Timer("ENGINE_PINGER",true);
   ping_timer.schedule(new EnginePinger(),PING_DELAY,PING_DELAY);

   scanArgs(args);
}




/********************************************************************************/
/*										*/
/*	Argument methods							*/
/*										*/
/********************************************************************************/

private void scanArgs(String [] args)
{
   for (int i = 0; i < args.length; ++i) {
      if (args[i].startsWith("-")) {
	 if (args[i].startsWith("-e") && i+1 < args.length) {           // -engine <port>
	    engine_port = Integer.parseInt(args[++i]);
	  }
	 else if (args[i].startsWith("-c") && i+1 < args.length) {      // -client <port>
	    client_port = Integer.parseInt(args[++i]);
	  }
	 else badArgs();
       }
      else badArgs();
    }
}



private void badArgs()
{
   System.err.println("S6: ENGINE: s6firewall");
   System.exit(1);
}




/********************************************************************************/
/*										*/
/*	Processing methods							*/
/*										*/
/********************************************************************************/

private void start()
{
   EngineServer es = new EngineServer();
   es.start();

   ClientServer cs = new ClientServer();
   cs.start();
}



/********************************************************************************/
/*										*/
/*	Server socket for talking to the engine 				*/
/*										*/
/********************************************************************************/

private void setupEngine(Socket s)
{
   try {
      EngineClient ec = new EngineClient(s);
      makeActive(ec);
    }
   catch (IOException e) {
      try {
	 s.close();
       }
      catch (IOException ex) { }
    }
}




private class EngineServer extends Thread {

   private ServerSocket server_socket;

   EngineServer() {
      super("S6_FIREWALL_ENGINE_ACCEPT");
      try {
	 server_socket = new ServerSocket(engine_port);
       }
      catch (IOException e) {
	 System.err.println("S6: FIREWALL: Problem creating engine server socket: " + e);
	 System.exit(1);
       }
    }

   public void run() {
      try {
	 for ( ; ; ) {
	    Socket s = server_socket.accept();
	    setupEngine(s);
	  }
       }
      catch (IOException e) {
	 System.err.println("S6: ENGINE: Problem with engine socket accept: " + e);
       }
    }

}	// end of subclass EngineServer




/********************************************************************************/
/*										*/
/*	Methods to handle list of active connections				*/
/*										*/
/********************************************************************************/

private void makeActive(EngineClient ec)
{
   synchronized (engine_queue) {
      int qsz = engine_queue.size();
      int where = random_gen.nextInt(qsz+1);
      engine_queue.add(where,ec);
      active_clients.add(ec);
      if (qsz == 0) engine_queue.notifyAll();
    }
}


private void removeActive(EngineClient ec)
{
   synchronized (engine_queue) {
      active_clients.remove(ec);
      if (engine_queue.size() == 0 && active_clients.size() == 0) engine_queue.notifyAll();
    }
}



private EngineClient getActiveClient()
{
   synchronized (engine_queue) {
      for ( ; ; ) {
	 if (engine_queue.size() > 0) {
	    return engine_queue.remove(0);
	  }
	 else if (active_clients.size() > 0) {
	    try {
	       engine_queue.wait();
	     }
	    catch (InterruptedException e) { }
	  }
	 else return null;
       }
    }
}



private void pingEngineClients()
{
   synchronized (engine_queue) {
      for (Iterator<EngineClient> it = engine_queue.iterator(); it.hasNext(); ) {
	 EngineClient ec = it.next();
	 try {
	    ec.send(null);
	  }
	 catch (IOException e) {
	    it.remove();
	    ec.close();
	  }
       }
    }
}







/********************************************************************************/
/*										*/
/*	Methods to handle waiting for clients					*/
/*										*/
/********************************************************************************/

private class ClientServer extends Thread {

   private ServerSocket server_socket;

   ClientServer() {
      super("S6_FIREWALL_CLIENT_ACCEPT");
      try {
	 server_socket = new ServerSocket(client_port);
       }
      catch (IOException e) {
	 System.err.println("S6: FIREWALL: Problem creating client server socket: " + e);
	 System.exit(1);
       }
    }

   public void run() {
      try {
	 for ( ; ; ) {
	    Socket s = server_socket.accept();
	    setupClient(s);
	  }
       }
      catch (IOException e) {
	 System.err.println("S6: FIREWALL: Problem with client socket accept: " + e);
       }
    }

}	// end of subclass ClientServer




/********************************************************************************/
/*										*/
/*	Methods to handle client connections					*/
/*										*/
/********************************************************************************/

private void setupClient(Socket s)
{
   try {
      FirewallClient c = new FirewallClient(s);
      c.start();
    }
   catch (IOException e) {
      System.err.println("S6: FIREWALL: Problem creating web client connection: " + e);
    }
}




private class FirewallClient extends IvyXmlReaderThread {

   private Socket client_socket;
   private PrintWriter print_writer;
   private EngineClient engine_client;

   FirewallClient(Socket s) throws IOException {
      super("FirewallClient_" + s.getRemoteSocketAddress(),
	       new InputStreamReader(s.getInputStream()));
      System.err.println("S6: Starting engine client " + getName());
      engine_client = null;
      client_socket = s;
      print_writer = new PrintWriter(s.getOutputStream());
    }

   protected void processXmlMessage(String msg) {
      System.err.println("S6: FIREWALL: Sending: " + msg);
      String rslt = null;
      for (int i = 0; i < 3; ++i) {
	 if (engine_client == null) engine_client = getActiveClient();
	 try {
	    if (engine_client != null) rslt = engine_client.send(msg);
	    break;
	  }
	 catch (IOException e) {
	    removeActive(engine_client);
	    engine_client.close();
	    engine_client = null;
	  }
       }
      if (rslt != null) {
	 rslt = rslt.trim();
	 print_writer.println(rslt);
       }
      print_writer.println("***EOM***");
      print_writer.flush();
      System.err.println("S6: FIREWALL: Recieved: " + rslt);
    }

   protected synchronized void processDone() {
      System.err.println("S6: FIREWALL: Done");
      if (client_socket == null) return;
      try {
	 client_socket.close();
	 client_socket = null;
	 if (engine_client != null) makeActive(engine_client);
       }
      catch (IOException e) { }
    }

   protected void processIoError(IOException e) {
      System.err.println("S6: FIREWALL: XML reader error for " + getName() + ": " + e);
      if (engine_client != null) makeActive(engine_client);
    }

}	// end of subclass FirewallClient




/********************************************************************************/
/*										*/
/*	Subclass representing a connection to the engine			*/
/*										*/
/********************************************************************************/

private static class EngineClient {

   private Socket	engine_socket;
   private IvyXmlReader engine_reader;
   private PrintWriter	engine_writer;

   EngineClient(Socket s) throws IOException {
      engine_reader = new IvyXmlReader(new InputStreamReader(s.getInputStream()));
      engine_writer = new PrintWriter(new OutputStreamWriter(s.getOutputStream()));
      engine_socket = s;
      System.err.println("ENGINE: FIREWALL: Engine connected to " + s);
    }

   String send(String msg) throws IOException {
      String rslt = null;

      if (engine_socket == null) throw new IOException("Socket closed");

      engine_socket.setSoTimeout(SOCKET_TIMEOUT_CHECK);
      engine_writer.println("<PING/>");
      engine_writer.flush();
      if (engine_writer.checkError()) throw new IOException("Socket ping error");
      rslt = engine_reader.readXml(true);
      if (rslt == null) throw new IOException("Socket error");
      if (msg == null) return "OK";

      engine_socket.setSoTimeout(SOCKET_TIMEOUT);
      engine_writer.println(msg);
      engine_writer.flush();
      if (engine_writer.checkError()) throw new IOException("Socket error");
      rslt = engine_reader.readXml();
      if (rslt == null) throw new IOException("Null result");

      return rslt;
    }

   void close() {
      System.err.println("ENGINE: FIREWALL: Disconnecting " + engine_socket);
      if (engine_socket != null) {
	 try {
	    engine_socket.close();
	  }
	 catch (IOException e) { }
       }
      engine_socket = null;
      engine_reader = null;
      engine_writer = null;
    }

}	// end of subclass EngineClient



/********************************************************************************/
/*										*/
/*	Class for pinging engine connections					*/
/*										*/
/********************************************************************************/

private class EnginePinger extends TimerTask {

   public void run() {
      pingEngineClients();
    }

}	// end of subclass EnginePinger


}	// end of EngineFirewall



/* end of EngineFirewall.java */
