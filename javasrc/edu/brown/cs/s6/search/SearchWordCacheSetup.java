/********************************************************************************/
/*										*/
/*		SearchWordCacheSetup.java					*/
/*										*/
/*	Class to setup base word bag from cache information			*/
/*										*/
/********************************************************************************/
/*	Copyright 2011 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2011, Brown University, Providence, RI.				 *
 *										 *
 *			  All Rights Reserved					 *
 *										 *
 * This program and the accompanying materials are made available under the	 *
 * terms of the Eclipse Public License v1.0 which accompanies this distribution, *
 * and is available at								 *
 *	http://www.eclipse.org/legal/epl-v10.html				 *
 *										 *
 ********************************************************************************/

/* SVN: $Id: SearchWordCacheSetup.java,v 1.2 2015/09/23 17:58:11 spr Exp $ */



package edu.brown.cs.s6.search;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

import org.xeustechnologies.jtar.TarEntry;
import org.xeustechnologies.jtar.TarInputStream;

import edu.brown.cs.ivy.file.IvyFile;

public class SearchWordCacheSetup implements SearchWordConstants
{


/********************************************************************************/
/*										*/
/*	Main Program								*/
/*										*/
/********************************************************************************/

public static void main(String [] args)
{
   SearchWordCacheSetup wcs = new SearchWordCacheSetup(args);
   wcs.process();
}



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private File		cache_directory;
private File		word_file;
private SearchWordBag	word_bag;
private long		after_date;
private File		tar_file;
private ParserDelegator parser_delegator;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

private SearchWordCacheSetup(String [] args)
{
   cache_directory = new File("/ws/volfred/s6/cache");
   File f = IvyFile.expandFile("$(S6)/lib");
   word_file = new File(f,"allwords.bag.zip");
   word_bag = new SearchWordBag();
   after_date = 0;
   tar_file = null;
   parser_delegator = new ParserDelegator();

   scanArgs(args);
}




/********************************************************************************/
/*										*/
/*	Processing methods							*/
/*										*/
/********************************************************************************/

private void process()
{
   if (cache_directory != null) {
      if (!cache_directory.isDirectory() || !cache_directory.exists() ||
	     !cache_directory.canRead()) {
	 System.err.println("CacheSetup: Can't use cache directory " + cache_directory);
	 return;
       }
      processFiles(cache_directory);
    }
   else if (tar_file != null) {
      processTarFile();
    }

   try {
      word_bag.outputBag(word_file);
    }
   catch (IOException e) {
      System.err.println("CacheSetup: Problem saving word bag: " + e);
    }
}



private void processFiles(File dir)
{
   File f1 = new File(dir,"URL");
   if (f1.exists()) {
      processUrl(dir,f1);
    }
   else {
      for (File f : dir.listFiles()) {
	 if (f.isDirectory()) processFiles(f);
       }
    }
}



private void processUrl(File dir,File urlf)
{
   if (urlf.lastModified() < after_date) return;

   try {
      BufferedReader br = new BufferedReader(new FileReader(urlf));
      String url = br.readLine();
      br.close();
      if (url == null) return;
      String txt = null;
      File f1 = new File(dir,"DATA");
      if (url.endsWith(".java")) {
         txt = IvyFile.loadFile(f1);
       }
      else if (url.contains("code.ohloh.net.file?fid=") || url.contains("code.openhub.net/file?fid=")) {
         String cnts = IvyFile.loadFile(f1);
         if (cnts.startsWith("<")) {
            StringReader sr = new StringReader(cnts);
            OhlohScanner os = new OhlohScanner();
            parser_delegator.parse(sr,os,true);
            txt = os.getText();
          }
         else txt = cnts;
       }
            
      if (txt != null) {
         word_bag.addWords(txt);
       }
    }
   catch (IOException e) {
      System.err.println("CacheSetup: I/O problem on cache file: " + e);
    }
}



private void processTarFile()
{
   try {
      FileInputStream fis = new FileInputStream(tar_file);
      TarInputStream tis = new TarInputStream(new BufferedInputStream(fis));

      TarEntry ent = null;
      while ((ent = tis.getNextEntry()) != null) {
	 if (ent.getName().endsWith(".java")) {
	    InputStreamReader isr = new InputStreamReader(tis);
	    String txt = IvyFile.loadFile(isr);
	    word_bag.addWords(txt);
	  }
       }

      fis.close();
    }
   catch (IOException e) {
      System.err.println("CacheSetup: I/O problem on tar file: " + e);
    }
}



private static class OhlohScanner extends HTMLEditorKit.ParserCallback {

   private StringBuffer string_buffer;
   private int in_code;
   private boolean in_file;
   
   OhlohScanner() {
      string_buffer = new StringBuffer();
      in_code = 0;
      in_file = false;
    }
   
   String getText() {
      int ln = string_buffer.length();
      if (ln == 0) return null;
      if (string_buffer.charAt(ln-1) != '\n') string_buffer.append("\n");
      return string_buffer.toString();
    }
   
   @Override public void handleStartTag(HTML.Tag t,MutableAttributeSet a,int pos) {
      if (t == HTML.Tag.DIV) {
         String cls = (String) a.getAttribute(HTML.Attribute.CLASS);
         if (cls != null && cls.equals("code_view")) in_code = 1;
         else if (in_code > 0) ++in_code;
         else if (cls != null && cls.equals("exp_items")) in_file = true;
       }
    }
   
   @Override public void handleEndTag(HTML.Tag t,int pos) {
      if (t == HTML.Tag.DIV && in_code > 0) --in_code;
      else if (in_file) in_file = false;
    }
   
   @Override public void handleText(char [] text,int pos) {
      if (in_code > 0) {
         string_buffer.append(text);
       }
    }
   
}	// end of inner class FileScanner





/********************************************************************************/
/*										*/
/*	Argument processing methods						*/
/*										*/
/********************************************************************************/

private void scanArgs(String [] args)
{
   boolean havecache = false;
   boolean update = false;
   boolean havedate = false;

   for (int i = 0; i < args.length; ++i) {
      if (args[i].startsWith("-")) {
	 if (args[i].startsWith("-c") && i+1 < args.length) {           // -cache <cache>
	    if (havecache) badArgs();
	    havecache = true;
	    cache_directory = new File(args[++i]);
	  }
	 else if (args[i].startsWith("-w") && i+1 < args.length) {      // -words <word file>
	    word_file = new File(args[++i]);
	  }
	 else if (args[i].startsWith("-u")) {                           // -updated
	    update = true;
	  }
	 else if (args[i].startsWith("-d")) {                           // -date
	    after_date = 0;
	    havedate = true;
	  }
	 else if (args[i].startsWith("-t") && i+1 < args.length) {      // -tar <tar file>
	    tar_file = new File(args[++i]);
	    cache_directory = null;
	    update = false;
	  }
	 else badArgs();
       }
      else if (!havecache) {
	 havecache = true;
	 cache_directory = new File(args[++i]);
       }
      else badArgs();
    }

   if (update) {
      try {
	 word_bag.inputBag(word_file);
	 if (!havedate) after_date = word_file.lastModified();
       }
      catch (IOException e) {
	 System.err.println("CacheSetup: Problem reading old word bag: " + e);
	 System.exit(1);
       }
    }
}




private void badArgs()
{
   System.err.println("CacheSetup: cachesetup [-cache <cache>] [-words <wordfile>] [-update]");
}




}	// end of class SearchWordCacheSetup




/* end of SearchWordCacheSetup.java */

