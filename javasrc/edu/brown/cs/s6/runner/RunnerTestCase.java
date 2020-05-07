/********************************************************************************/
/*										*/
/*		RunnerTestCase.java						*/
/*										*/
/*	Extensions to junit.TestCase for handling s6 test cases 		*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/runner/RunnerTestCase.java,v 1.11 2015/12/23 15:45:13 spr Exp $ */


/*********************************************************************************
 *
 * $Log: RunnerTestCase.java,v $
 * Revision 1.11  2015/12/23 15:45:13  spr
 * Minor fixes.
 *
 * Revision 1.10  2015/09/23 17:58:10  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.9  2014/08/29 15:16:14  spr
 * Updates for suise, testcases.
 *
 * Revision 1.8  2014/02/26 14:07:04  spr
 * Update swing checking.
 *
 * Revision 1.7  2013/09/13 20:33:10  spr
 * Add calls for UI search.
 *
 * Revision 1.6  2013-05-09 12:26:24  spr
 * Minor changes to start ui fixups.
 *
 * Revision 1.5  2012-08-13 16:52:06  spr
 * Clean up UI handling with hierarchy output.
 *
 * Revision 1.4  2012-07-20 22:15:39  spr
 * Add ui search matching.
 *
 * Revision 1.3  2012-06-20 12:21:37  spr
 * Initial fixes for UI search
 *
 * Revision 1.2  2012-06-11 14:08:18  spr
 * Add framework search; fix bugs
 *
 * Revision 1.1  2009-09-18 01:42:02  spr
 * Update jml and new junit.
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.runner;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.junit.Assert;

import edu.brown.cs.ivy.file.IvyLog;



public class RunnerTestCase extends Assert {


/********************************************************************************/
/*										*/
/*	Status variables							*/
/*										*/
/********************************************************************************/

private static boolean show_result = false;




/********************************************************************************/
/*										*/
/*	Not equal assertions							*/
/*										*/
/********************************************************************************/

public static void assertNotEquals(String msg,Object exp,Object act)
{
   if (exp == null && act != null) return;
   else if (exp != null && act == null) return;
   else if (exp == null && act == null) failNotEquals(msg,exp,act);
   else if (exp != null && exp.equals(act)) failNotEquals(msg,exp,act);
}


public static void assertNotEquals(String msg,double exp,double act,double delta)
{
   if (Double.isInfinite(exp)) {
      if ((exp == act))
	 failNotEquals(msg,Double.valueOf(exp),Double.valueOf(act));
    }
   else if (!(Math.abs(exp-act) > delta))
      failNotEquals(msg,Double.valueOf(exp),Double.valueOf(act));
}



public static void assertNotEquals(String msg,float exp,float act,float delta)
{
   if (Float.isInfinite(exp)) {
      if ((exp == act))
	 failNotEquals(msg,Float.valueOf(exp),Float.valueOf(act));
    }
   else if (!(Math.abs(exp-act) > delta))
      failNotEquals(msg,Float.valueOf(exp),Float.valueOf(act));
}


public static void assertNotEquals(String msg,long exp,long act)
{
   assertNotEquals(msg,Long.valueOf(exp),Long.valueOf(act));
}


public static void assertNotEquals(String msg,boolean exp,boolean act)
{
   assertNotEquals(msg,Boolean.valueOf(exp),Boolean.valueOf(act));
}


public static void assertNotEquals(String msg,byte exp,byte act)
{
   assertNotEquals(msg,Byte.valueOf(exp),Byte.valueOf(act));
}


public static void assertNotEquals(String msg,char exp,char act)
{
   assertNotEquals(msg,Character.valueOf(exp),Character.valueOf(act));
}


public static void assertNotEquals(String msg,short exp,short act)
{
   assertNotEquals(msg,Short.valueOf(exp),Short.valueOf(act));
}


public static void assertNotEquals(String msg,int exp,int act)
{
   assertNotEquals(msg,Integer.valueOf(exp),Integer.valueOf(act));
}

private static void failNotEquals(String msg,Object e,Object a)
{
   String f = "";
   if (msg != null) f = msg + " ";
   f += "expected:<" + e + "> but was not:<" + a + ">";
   fail(f);
}



/********************************************************************************/
/*										*/
/*	Additional equals assertions for handling arrays			*/
/*										*/
/********************************************************************************/

public static void assertEquals(String msg,Object [] exp,Object [] act)
{
   assertArrayEquals(msg,exp,act);
}



public static void assertEquals(String msg,double [] exp,double [] act,double delta)
{
   assertArrayEquals(msg,exp,act,delta);
}



public static void assertEquals(String msg,float [] exp,float [] act,float delta)
{
   assertArrayEquals(msg,exp,act,delta);
}



public static void assertEquals(String msg,int [] exp,int [] act)
{
   assertArrayEquals(msg,exp,act);
}



public static void assertEquals(String msg,short [] exp,short [] act)
{
   assertArrayEquals(msg,exp,act);
}



public static void assertEquals(String msg,byte [] exp,byte [] act)
{
   assertArrayEquals(msg,exp,act);
}



public static void assertEquals(String msg,char [] exp,char [] act)
{
   assertArrayEquals(msg,exp,act);
}



public static void assertEquals(String msg,long [] exp,long [] act)
{
   assertArrayEquals(msg,exp,act);
}



public static void assertEquals(String msg,double [][] exp,double [][] act,double delta)
{
   if (exp == null && act == null) return;
   else if (exp == null || act == null) failEquals(msg,exp,act);
   else if (exp.length != act.length) failEquals(msg,exp,act);
   else {
      for (int i = 0; i < exp.length; ++i) {
	 assertEquals(msg,exp[i],act[i],delta);
       }
    }
}



public static void assertEquals(String msg,float [][] exp,float [][] act,float delta)
{
   if (exp == null && act == null) return;
   else if (exp == null || act == null) failEquals(msg,exp,act);
   else if (exp.length != act.length) failEquals(msg,exp,act);
   else {
      for (int i = 0; i < exp.length; ++i) {
	 assertEquals(msg,exp[i],act[i],delta);
       }
    }
}





public static void assertNotEquals(String msg,double [] exp,double [] act,double delta)
{
   if (exp == null && act == null) return;
   else if (exp == null || act == null) failNotEquals(msg,exp,act);
   else if (exp.length != act.length) failNotEquals(msg,exp,act);
   else {
      for (int i = 0; i < exp.length; ++i) {
	 assertNotEquals(msg,exp[i],act[i],delta);
       }
    }
}



public static void assertNotEquals(String msg,float [] exp,float [] act,float delta)
{
   if (exp == null && act == null) return;
   else if (exp == null || act == null) failNotEquals(msg,exp,act);
   else if (exp.length != act.length) failNotEquals(msg,exp,act);
   else {
      for (int i = 0; i < exp.length; ++i) {
	 assertNotEquals(msg,exp[i],act[i],delta);
       }
    }
}



public static void assertNotEquals(String msg,int [] exp,int [] act)
{
   if (exp == null && act == null) return;
   else if (exp == null || act == null) failNotEquals(msg,exp,act);
   else if (exp.length != act.length) failNotEquals(msg,exp,act);
   else {
      for (int i = 0; i < exp.length; ++i) {
	 assertNotEquals(msg,exp[i],act[i]);
       }
    }
}



public static void assertNotEquals(String msg,short [] exp,short [] act)
{
   if (exp == null && act == null) return;
   else if (exp == null || act == null) failNotEquals(msg,exp,act);
   else if (exp.length != act.length) failNotEquals(msg,exp,act);
   else {
      for (int i = 0; i < exp.length; ++i) {
	 assertNotEquals(msg,exp[i],act[i]);
       }
    }
}



public static void assertNotEquals(String msg,byte [] exp,byte [] act)
{
   if (exp == null && act == null) return;
   else if (exp == null || act == null) failNotEquals(msg,exp,act);
   else if (exp.length != act.length) failNotEquals(msg,exp,act);
   else {
      for (int i = 0; i < exp.length; ++i) {
	 assertNotEquals(msg,exp[i],act[i]);
       }
    }
}



public static void assertNotEquals(String msg,char [] exp,char [] act)
{
   if (exp == null && act == null) return;
   else if (exp == null || act == null) failNotEquals(msg,exp,act);
   else if (exp.length != act.length) failNotEquals(msg,exp,act);
   else {
      for (int i = 0; i < exp.length; ++i) {
	 assertNotEquals(msg,exp[i],act[i]);
       }
    }
}



public static void assertNotEquals(String msg,long [] exp,long [] act)
{
   if (exp == null && act == null) return;
   else if (exp == null || act == null) failNotEquals(msg,exp,act);
   else if (exp.length != act.length) failNotEquals(msg,exp,act);
   else {
      for (int i = 0; i < exp.length; ++i) {
	 assertNotEquals(msg,exp[i],act[i]);
       }
    }
}



public static void assertNotEquals(String msg,double [][] exp,double [][] act,double delta)
{
   if (exp == null && act == null) return;
   else if (exp == null || act == null) failNotEquals(msg,exp,act);
   else if (exp.length != act.length) failNotEquals(msg,exp,act);
   else {
      for (int i = 0; i < exp.length; ++i) {
	 assertNotEquals(msg,exp[i],act[i],delta);
       }
    }
}



public static void assertNotEquals(String msg,float [][] exp,float [][] act,float delta)
{
   if (exp == null && act == null) return;
   else if (exp == null || act == null) failNotEquals(msg,exp,act);
   else if (exp.length != act.length) failNotEquals(msg,exp,act);
   else {
      for (int i = 0; i < exp.length; ++i) {
	 assertNotEquals(msg,exp[i],act[i],delta);
       }
    }
}



public static void assertNotEquals(String msg,int [][] exp,int [][] act)
{
   if (exp == null && act == null) return;
   else if (exp == null || act == null) failNotEquals(msg,exp,act);
   else if (exp.length != act.length) failNotEquals(msg,exp,act);
   else {
      for (int i = 0; i < exp.length; ++i) {
	 assertNotEquals(msg,exp[i],act[i]);
       }
    }
}



public static void assertNotEquals(String msg,short [][] exp,short [][] act)
{
   if (exp == null && act == null) return;
   else if (exp == null || act == null) failNotEquals(msg,exp,act);
   else if (exp.length != act.length) failNotEquals(msg,exp,act);
   else {
      for (int i = 0; i < exp.length; ++i) {
	 assertNotEquals(msg,exp[i],act[i]);
       }
    }
}



public static void assertNotEquals(String msg,byte [][] exp,byte [][] act)
{
   if (exp == null && act == null) return;
   else if (exp == null || act == null) failNotEquals(msg,exp,act);
   else if (exp.length != act.length) failNotEquals(msg,exp,act);
   else {
      for (int i = 0; i < exp.length; ++i) {
	 assertNotEquals(msg,exp[i],act[i]);
       }
    }
}



public static void assertNotEquals(String msg,char [][] exp,char [][] act)
{
   if (exp == null && act == null) return;
   else if (exp == null || act == null) failNotEquals(msg,exp,act);
   else if (exp.length != act.length) failNotEquals(msg,exp,act);
   else {
      for (int i = 0; i < exp.length; ++i) {
	 assertNotEquals(msg,exp[i],act[i]);
       }
    }
}



public static void assertNotEquals(String msg,long [][] exp,long [][] act)
{
   if (exp == null && act == null) return;
   else if (exp == null || act == null) failNotEquals(msg,exp,act);
   else if (exp.length != act.length) failNotEquals(msg,exp,act);
   else {
      for (int i = 0; i < exp.length; ++i) {
	 assertNotEquals(msg,exp[i],act[i]);
       }
    }
}



/********************************************************************************/
/*										*/
/*	Simplified assertions for handling real values				*/
/*										*/
/********************************************************************************/

public static void assertEquals(String msg,double exp,double act)
{
   assertEquals(msg,exp,act,1e-12);
}

public static void assertEquals(String msg,float exp,float act)
{
   assertEquals(msg,exp,act,1e-6f);
}

public static void assertNotEquals(String msg,double exp,double act)
{
   assertNotEquals(msg,exp,act,1e-12);
}

public static void assertNotEquals(String msg,float exp,float act)
{
   assertNotEquals(msg,exp,act,1e-6f);
}



public static void assertEquals(String msg,double [] exp,double [] act)
{
   assertEquals(msg,exp,act,1e-12);
}

public static void assertEquals(String msg,float [] exp,float [] act)
{
   assertEquals(msg,exp,act,1e-6f);
}

public static void assertNotEquals(String msg,double [] exp,double [] act)
{
   assertNotEquals(msg,exp,act,1e-12);
}

public static void assertNotEquals(String msg,float [] exp,float [] act)
{
   assertNotEquals(msg,exp,act,1e-6f);
}



public static void assertEquals(String msg,double [][] exp,double [][] act)
{
   assertEquals(msg,exp,act,1e-12);
}

public static void assertEquals(String msg,float [][] exp,float [][] act)
{
   assertEquals(msg,exp,act,1e-6f);
}

public static void assertNotEquals(String msg,double [][] exp,double [][] act)
{
   assertNotEquals(msg,exp,act,1e-12);
}

public static void assertNotEquals(String msg,float [][] exp,float [][] act)
{
   assertNotEquals(msg,exp,act,1e-6f);
}




/********************************************************************************/
/*										*/
/*	Utility routines for helping with equals/not equals output		*/
/*										*/
/********************************************************************************/

private static void failEquals(String message, Object expected, Object actual)
{
   String formatted= "";
   if (message != null) formatted= message+" ";
   fail(formatted+"expected:<"+expected+"> but was:<"+actual+">");
}



/********************************************************************************/
/*										*/
/*	Show assertions for user interaction					*/
/*										*/
/********************************************************************************/

public static void assertShow(Component w)
{
   assertShow(w,0);
}


public static void assertShow(Component w,double score)
{
   Dimension d = setupComponent(w);

   BufferedImage bi = new BufferedImage(d.width,d.height,BufferedImage.TYPE_4BYTE_ABGR);
   Graphics2D g = bi.createGraphics();

   Color cl = g.getColor();
   g.setColor(Color.WHITE);
   g.fillRect(0,0,d.width,d.height);
   g.setColor(cl);
   w.paint(g);
   g.dispose();

   File f;
   for (int i = 0; ; ++i) {
      f = new File("image" + i + ".png");
      try {
	 if (f.createNewFile()) break;
       }
      catch (Throwable t) {
	 fail("WIDGET FILE CREATION FAILED: " + t);
       }
    }

   try {
      ImageIO.write(bi,"png",f);
    }
   catch (Throwable t) {
      fail("WIDGET SAVE FAILED: " + t);
    }

   fail("S6AskUser: IMAGE: " + f.getAbsolutePath() + " @@@ " + score);
}



public static void assertInteract(Component w)
{
   assertInteract(w,0);
}



public static void assertInteract(Component w,double score)
{
   if (show_result) {
      RunnerSwingShow rss = new RunnerSwingShow("S6TestCase Interactor",w);
      rss.process();
      return;
    }

   try {
      ProcessBuilder pb = new ProcessBuilder("ant","jar");
      Process p = pb.start();
      int sts = p.waitFor();
      IvyLog.logI("RUNNER","ANT JAR STATUS = " + sts);
    }
   catch (Throwable e) {
      fail("Problem building jar");
    }

   assertShow(w,score);
}


public static void assertAndroidInteract(RunnerAndroid and,double score)
{
   // handle android cases
}


public static void setShowInteract(boolean fg)
{
   show_result = fg;
}

public static void assertShow(Object o)
{
   fail("S6AskUser: OBJECT: " + o);
}



public static void assertShow(String o)
{
   fail("S6AskUser: STRING: \"" + o + "\"");
}


public static void assertShow(double v)
{
   fail("S6AskUser: NUMBER: " + v);
}



public static void assertShow(long v)
{
   fail("S6AskUser: NUMBER: " + v);
}


public static void assertShowThrow(Throwable t)
{
   if (t.getMessage().startsWith("S6AskUser:")) {
      fail(t.getMessage());
    }
   else {
      fail("S6AskUser: THROW: " + t);
    }
}



private static Dimension setupComponent(Component w)
{
   Dimension d1 = w.getSize();

   if (w instanceof Dialog) {
      Dialog jd = (Dialog) w;
      jd.setModal(false);
    }
   if (w instanceof Window) {
      Window w1 = (Window) w;
      w1.pack();
    }
   else {
      Frame w1 = new Frame();
      w1.add(w);
      w1.pack();
      w1.validate();
      w = w1;
    }

   fixSizes(w,30,30);

   Dimension d = w.getPreferredSize();
   if (d.width < d1.width) d.width = d1.width;
   if (d.height < d1.height) d.height = d1.height;
   if (d.width <= 1) d.width = 200;
   if (d.height <= 1) d.height = 200;

   w.setSize(d);
   w.setPreferredSize(d);
   w.setVisible(true);

   return d;
}



private static Dimension fixSizes(Component c,int minw,int minh)
{
   Dimension d = c.getPreferredSize();
   Dimension d2 = c.getSize();
   d.width = Math.max(d.width,d2.width);
   d.height = Math.max(d.height,d2.height);

   // System.err.println("START " + d + " : " + c);
   if (d.width > minw && d.height > minh) return d;
   if (c instanceof Container) {
      Container cc = (Container) c;
      int nc = cc.getComponentCount();
      for (int i = 0; i < nc; ++i) {
	 Component xc = cc.getComponent(i);
	 Dimension d1 = fixSizes(xc,4,4);
	 if (xc.getX() + d1.width > d.width) d.width = xc.getX() + d1.width;
	 if (xc.getY() + d1.height > d.height) d.height = xc.getY() + d1.height;
       }
      c.setSize(d);
      c.setPreferredSize(d);
    }

   return d;
}




public static void assertShowHierarchy(Component w,RunnerS6HierData[] data)
{
   setupComponent(w);

   StringBuilder buf = new StringBuilder();
   try {
      dumpHierarchy(w,buf,0);
    }
   catch (Throwable t) {
      buf.append("ERROR: " + t);
      t.printStackTrace();
    }

   fail("S6AskUser: HIERARCHY: " + buf.toString());
}



public static void assertMatchHierarchy(Component w,RunnerS6HierData[] data)
{
   setupComponent(w);

   if (match(w,data,0,new HashSet<Component>())) {
      StringBuilder buf = new StringBuilder();
      displayHierarchy(w,buf,0);
      try {
	 FileWriter fw = new FileWriter("hierarchy.txt");
	 fw.write(buf.toString());
	 fw.close();
       }
      catch (IOException e) { }
      System.err.println(buf);
      return;
    }

   StringBuilder buf = new StringBuilder();
   try {
      dumpHierarchy(w,buf,0);
    }
   catch (Throwable t) {
      buf.append("ERROR: " + t);
      t.printStackTrace();
    }

   fail("S6UIMatch: HIERARCHY: " + buf.toString());
}




public static double scoreMatchHierarchy(Component w,RunnerS6HierData[] data)
{
   setupComponent(w);

   RunnerSwingMatcher sm = new RunnerSwingMatcher(data,w);
   double score = sm.computeMatch();

   System.err.println("SCORE " + score);

   if (score >= 0) {
      StringBuilder buf = new StringBuilder();
      displayHierarchy(w,buf,0);
      try {
	 FileWriter fw = new FileWriter("hierarchy.txt");
	 fw.write(buf.toString());
	 fw.close();
       }
      catch (IOException e) { }
      System.err.println(buf);
      return score;
    }

   StringBuilder buf = new StringBuilder();
   try {
      // dumpHierarchy(w,buf,0);
      displayHierarchy(w,buf,0);
    }
   catch (Throwable t) {
      buf.append("ERROR: " + t);
      t.printStackTrace();
    }

   fail("S6UIMatch: HIERARCHY: " + buf.toString());

   return -1;
}



private static void dumpHierarchy(Component c,StringBuilder buf,int depth)
{
   buf.append(c.getClass().getName());
   buf.append(",");
   buf.append(c.getWidth());
   buf.append(",");
   buf.append(c.getHeight());
   buf.append(",");
   buf.append(c.getX());
   buf.append(",");
   buf.append(c.getY());

   if (c instanceof Container) {
      Container p = (Container) c;
      buf.append(",");
      buf.append(p.getComponentCount());
      buf.append("; ");
      for (Component pc : p.getComponents()) {
	 if (pc != null) dumpHierarchy(pc,buf,depth+1);
       }
    }
   else {
      buf.append(",0\n");
    }
}


private static void displayHierarchy(Component c,StringBuilder buf,int depth)
{
   if (buf.length() > 0) buf.append("\n");

   for (int i = 0; i < depth; ++i) buf.append(" ");
   buf.append(c.getClass().getName());
   buf.append(" (");
   buf.append(c.getWidth());
   buf.append("x");
   buf.append(c.getHeight());
   buf.append("+");
   buf.append(c.getX());
   buf.append("+");
   buf.append(c.getY());
   buf.append(") ");

   if (c instanceof JLabel) {
      buf.append("[");
      buf.append(((JLabel) c).getText());
      buf.append("] ");
    }
   if (c instanceof JButton) {
      JButton jb = (JButton) c;
      buf.append("[");
      buf.append(jb.getText());
      buf.append("] ");
    }
   if (c instanceof JTextField) {
      JTextField tf = (JTextField) c;
      buf.append("[");
      buf.append(tf.getColumns());
      buf.append("]");
    }

   if (c instanceof Container) {
      Container p = (Container) c;
      for (Component pc : p.getComponents()) {
	 if (pc != null) displayHierarchy(pc,buf,depth+1);
       }
    }
}



private static boolean match(Component c,RunnerS6HierData[] data,int idx,Set<Component> indone)
{
   if (data[idx].classCompatible(c.getClass())) {
      if (data[idx].getChildCount() == 0) {
	 if (indone.contains(c)) return false;
	 indone.add(c);
	 return true;
       }
      if (c instanceof Container) {
	 Container p = (Container) c;
	 Set<Component> done = new HashSet<Component>(indone);
	 int ctr = idx+1;
	 boolean allfnd = true;
	 for (int i = 0; i < data[idx].getChildCount(); ++i) {
	    boolean fnd = false;
	    for (Component pc : p.getComponents()) {
	       if (done.contains(pc)) continue;
	       if (match(pc,data,ctr,done)) {
		  fnd = true;
		  break;
		}
	     }
	    allfnd &= fnd;
	    if (!allfnd) break;
	    ctr = nextSibling(data,ctr);
	  }
	 if (allfnd) {
	    indone.add(c);
	    indone.addAll(done);
	    return true;
	  }
       }
    }

   if (c instanceof Container) {
      Container p = (Container) c;
      for (int i = 0; i < p.getComponentCount(); ++i) {
	 if (match(p.getComponent(i),data,idx,indone)) return true;
       }
    }

   return false;
}






private static int nextSibling(RunnerS6HierData [] data,int idx)
{
   int rslt = idx+1;
   for (int i = 0; i < data[idx].getChildCount(); ++i) {
      rslt = nextSibling(data,rslt);
    }
   return rslt;
}





}	// end of class RunnerTestCase




/* end of RunnerTestCase.java */
