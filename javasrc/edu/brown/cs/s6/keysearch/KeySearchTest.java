package edu.brown.cs.s6.keysearch;

import java.util.ArrayList;
import java.util.List;
import java.net.*;
import java.io.*;


public class KeySearchTest {



/********************************************************************************/
/*										*/
/*	Main program								*/
/*										*/
/********************************************************************************/

public static void main(String [] args)
{
   KeySearchTest kst = new KeySearchTest(args);

   kst.process();
}




/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private KeySearchCache	key_cache;
private List<URL>	url_list;
private long		read_delay;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

KeySearchTest(String [] args)
{
   key_cache = new KeySearchCache();
   url_list = new ArrayList<URL>();
   read_delay = 0;

   getGithubKey();

   scanArgs(args);
}





/********************************************************************************/
/*										*/
/*	Argument scanning							*/
/*										*/
/********************************************************************************/

private void scanArgs(String [] args)
{
   File urlf = null;

   for (int i = 0; i < args.length; ++i) {
      if (args[i].startsWith("-")) {
	 if (args[i].startsWith("-f") && i+1 < args.length) {                   // -file <file>
	    urlf = new File(args[++i]);
	  }
	 else if (args[i].startsWith("-d") && i+1 < args.length) {              // -delay #
	    try {
	       read_delay = Long.parseLong(args[++i]);
	     }
	    catch (NumberFormatException e) {
	       badArgs();
	     }
	  }
	 else badArgs();
       }
      else {
	 try {
	    URL u = new URL(args[i]);
	    url_list.add(u);
	  }
	 catch (MalformedURLException e) {
	    if (urlf == null) urlf = new File(args[i]);
	    else badArgs();
	  }
       }
    }

   if (urlf != null) {
      try {
	 BufferedReader br = new BufferedReader(new FileReader(urlf));
	 for ( ; ; ) {
	    String ln = br.readLine();
	    if (ln == null) break;
	    ln = ln.trim();
	    if (ln.length() == 0) continue;
	    try {
	       URL u = new URL(ln);
	       url_list.add(u);
	     }
	    catch (MalformedURLException e) {
	       System.err.println("KEYSEARCHTEST: BAD URL from file: " + ln);
	     }
	  }
	 br.close();
       }
      catch (IOException e) {
	 System.err.println("KEYSEARCHTEST: Problem reading url file: " + e);
	 System.exit(1);
       }
    }
}



private void badArgs()
{
   System.err.println("KEYSEARCHTEST [-f <url file>] [-d delay] url ...");
   System.exit(1);
}





/********************************************************************************/
/*										*/
/*	Processing								*/
/*										*/
/********************************************************************************/

private void process()
{
   for (URL u : url_list) {
      System.err.println("WORK ON: " + u);
      boolean retry = false;
      for (int i = 0; i < 5; ++i) {
	 try {
	    BufferedReader br = key_cache.getReader(u,true,retry);
	    br.close();
	    break;
	  }
	 catch (IOException e) {
	    System.err.println("GOT EXCEPTION: " + e);
	    if (!e.getMessage().contains(": 429") && !e.getMessage().contains(": 400")) break;
	    if (i == 4) {
	       System.err.println("KEYSEARCHTEST: Fail for url: " + u);
	       break;
	     }
	  }
	 if (read_delay == 0) {
	    try {
	       Thread.sleep(10000);
	     }
	    catch (InterruptedException e) { }
	  }
	 retry = true;
       }
    }
}


/********************************************************************************/
/*										*/
/*	Authentication								*/
/*										*/
/********************************************************************************/

private void getGithubKey()
{
   // look at	 https://github.com/settings/tokens to get a token directly

   String creds = "StevenReiss:West2east";
   String c1 = javax.xml.bind.DatatypeConverter.printBase64Binary(creds.getBytes());
   String auth = "Basic " + c1;

   try {
      URL url1 = new URL("https://api.github.com/authorizations");
      HttpURLConnection hc1 = (HttpURLConnection) url1.openConnection();
      hc1.setDoOutput(false);
      hc1.setDoInput(true);
      hc1.setRequestProperty("Authorization",auth);
      hc1.setRequestProperty("User-Agent","s6");
      InputStream ins = hc1.getInputStream();
      BufferedReader r = new BufferedReader(new InputStreamReader(ins));
      for ( ; ; ) {
	 String ln = r.readLine();
	 if (ln == null) break;
	 System.err.println("OUTPUT: " + ln);
       }
      hc1.disconnect();
    }
   catch (Throwable t) {
      System.err.println("EXCEPTION: " + t);
      t.printStackTrace();
      creds = null;
    }

   if (creds != null) return;

   try {
      URL url1 = new URL("https://api.github.com/authorizations");
      HttpURLConnection hc1 = (HttpURLConnection) url1.openConnection();
      hc1.setDoOutput(true);
      hc1.setDoInput(true);
      hc1.setRequestProperty("Authorization",auth);
      hc1.setRequestProperty("User-Agent","s6");
      hc1.setRequestMethod("POST");
      String cnts = "{ \"scopes\" : [ \"public_repo\" ], \"note\" : \"s6_access\", \"client_id\" : \"92367cf10da5b70932fa\", " +
	 " \"note_url\" : \"http://conifer.cs.brown.edu/s6\", " +
	 " \"client_secret\" : \"53e04859dec97346e3cd9f886b4e847c4d7cc2dc\", \"fingerprint\" : \"test\" }\n";
      System.err.println("INPUT: " + cnts);
      OutputStream ots = hc1.getOutputStream();
      ots.write(cnts.getBytes());
      ots.close();
      InputStream ins = hc1.getInputStream();
      BufferedReader r = new BufferedReader(new InputStreamReader(ins));
      for ( ; ; ) {
	 String ln = r.readLine();
	 if (ln == null) break;
	 System.err.println("OUTPUT: " + ln);
       }
      hc1.disconnect();
    }
   catch (Throwable t) {
      System.err.println("EXCEPTION: " + t);
      t.printStackTrace();
    }
}




}	// end of class KeySearchTest



/* end of KeySearchTest.java */
