/********************************************************************************/
/*                                                                              */
/*              SuiseResultPanel.java                                           */
/*                                                                              */
/*      Class to display the results of a search                                */
/*                                                                              */
/********************************************************************************/
/*      Copyright 2013 Brown University -- Steven P. Reiss                    */
/*********************************************************************************
 *  Copyright 2013, Brown University, Providence, RI.                            *
 *                                                                               *
 *                        All Rights Reserved                                    *
 *                                                                               *
 *  Permission to use, copy, modify, and distribute this software and its        *
 *  documentation for any purpose other than its incorporation into a            *
 *  commercial product is hereby granted without fee, provided that the          *
 *  above copyright notice appear in all copies and that both that               *
 *  copyright notice and this permission notice appear in supporting             *
 *  documentation, and that the name of Brown University not be used in          *
 *  advertising or publicity pertaining to distribution of the software          *
 *  without specific, written prior permission.                                  *
 *                                                                               *
 *  BROWN UNIVERSITY DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS                *
 *  SOFTWARE, INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND            *
 *  FITNESS FOR ANY PARTICULAR PURPOSE.  IN NO EVENT SHALL BROWN UNIVERSITY      *
 *  BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY          *
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,              *
 *  WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS               *
 *  ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE          *
 *  OF THIS SOFTWARE.                                                            *
 *                                                                               *
 ********************************************************************************/



package edu.brown.cs.s6.suise;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;

import org.w3c.dom.Element;

import edu.brown.cs.ivy.exec.IvyExec;
import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.ivy.xml.IvyXmlWriter;


class SuiseResultPanel extends JFrame implements SuiseConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private String          result_uid;
private String          testcase_uid;

private List<SearchResult> result_list;

private JPanel          display_panel;
private JPanel          result_panel;
private JScrollPane     result_pane;
private JLabel          status_area;
private JButton         finish_button;
private Finisher        finish_handler;

private static int      IMAGE_WIDTH = 300;
private static int      IMAGE_HEIGHT = 300;

private static final long serialVersionUID = 1;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

SuiseResultPanel(SuiseMain sm)
{
   super("User Interface Search Results");

   setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

   result_list = new ArrayList<SearchResult>();
   result_uid = null;
   finish_handler = new Finisher();

   setupPanel();
}



/********************************************************************************/
/*                                                                              */
/*      Process the search result                                               */
/*                                                                              */
/********************************************************************************/

Element processResult(Element rslt)
{
   for ( ; ; ) {
      if (!IvyXml.isElement(rslt,"RESULT")) {
         status_area.setText("Error occurred during search");
         status_area.setBackground(Color.RED);
         status_area.setOpaque(true);
         return null;
       }

      Element ue = IvyXml.getChild(rslt,"USERINPUT");
      if (ue == null) {
         Element se = IvyXml.getChild(rslt,"SOLUTIONS");
         if (se == null) return rslt;
         int ct = IvyXml.getAttrInt(se,"COUNT");
         if (ct == 0) {
            status_area.setText("Nothing found during search");
            status_area.setBackground(Color.YELLOW);
            status_area.setOpaque(true);
            return null;
          }
         return rslt;
       }

      if (finish_handler.isClosed()) return null;

      result_uid = IvyXml.getAttrString(ue,"UID");
      Element tce = IvyXml.getChild(ue,"TESTCASE");
      testcase_uid = IvyXml.getAttrString(tce,"NAME");

      for (Element te : IvyXml.children(tce,"USERCASE")) {
         SearchResult sr = new SearchResult(te);
         result_list.add(sr);
       }

      if (result_list.isEmpty()) {
         status_area.setText("Nothing found during search");
         status_area.setBackground(Color.YELLOW);
         status_area.setOpaque(true);
         return null;
       }
      else {
         Collections.sort(result_list);

         status_area.setVisible(false);

         result_pane.setVisible(true);
         display_panel.add(result_pane,BorderLayout.CENTER);
         for (SearchResult sr : result_list) {
            JPanel pnl = sr.getResultDisplay();
            result_panel.add(pnl);
            System.err.println("ADD " + pnl.getPreferredSize() + " " +
                  result_panel.getPreferredSize() + " " +
                  result_pane.getPreferredSize());
          }
         finish_button.setEnabled(true);

         pack();
       }

      rslt = finish_handler.waitForResults();
    }
}



/********************************************************************************/
/*                                                                              */
/*      Panel setup methods                                                     */
/*                                                                              */
/********************************************************************************/

private void setupPanel()
{
   display_panel = new JPanel(new BorderLayout());
   JLabel ttl = new JLabel("User Interface Search Results",JLabel.CENTER);
   ttl.setFont(ttl.getFont().deriveFont(Font.BOLD,16f));
   display_panel.add(ttl,BorderLayout.NORTH);

   status_area = new JLabel("Awaiting search results ...",JLabel.CENTER);
   display_panel.add(status_area,BorderLayout.CENTER);

   result_panel = new JPanel(new GridLayout(0,3,10,10));
   result_pane = new JScrollPane(result_panel);
   // pnl.add(result_pane,BorderLayout.CENTER);
   result_pane.setVisible(false);

   finish_button = new JButton("Get The Code");
   finish_button.setEnabled(false);
   finish_button.addActionListener(finish_handler);

   display_panel.add(finish_button,BorderLayout.SOUTH);

   setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
   addWindowListener(finish_handler);

   setContentPane(display_panel);
   pack();
}




/********************************************************************************/
/*                                                                              */
/*      Class to hold a search result                                           */
/*                                                                              */
/********************************************************************************/

private class SearchResult implements Comparable<SearchResult>, ActionListener {

   private String for_ids;
   private BufferedImage user_image;
   private ImageIcon scaled_image;
   private JToggleButton accept_button;
   private JToggleButton reject_button;
   private byte [] jar_runner;
   private double panel_score;
   private boolean is_accepted;

   SearchResult(Element xml) {
      for_ids = IvyXml.getAttrString(xml,"IDS");
      panel_score = 0;
      is_accepted = false;
      String cd = IvyXml.getTextElement(xml,"VALUE");
      Element x1 = IvyXml.convertStringToXml(cd);       // <IMG ...></IMG>
      String src = IvyXml.getAttrString(x1,"SRC");
      int idx = src.indexOf(",");
      src = src.substring(idx+1);
      byte [] img = Base64.getDecoder().decode(src);
      ByteArrayInputStream bas = new ByteArrayInputStream(img);
      try {
         user_image = ImageIO.read(bas);
       }
      catch (IOException e) {
         System.err.println("SUISE: Problem converting image: " + e);
       }
      scaled_image = null;
      String alt = IvyXml.getAttrString(x1,"ALT");
      idx = alt.indexOf(":");
      if (idx > 0) {
         String sc1 = alt.substring(idx+1).trim();
         try {
            panel_score = Double.parseDouble(sc1);
          }
         catch (NumberFormatException e) { }
       }
      String jtx = IvyXml.getTextElement(xml,"RUNJAR");
      if (jtx != null) {
         jar_runner = Base64.getDecoder().decode(jtx);
       }
      else jar_runner = null;
    }

   JPanel getResultDisplay() {
      JPanel pnl = new JPanel(new BorderLayout());
      ImageIcon icn = getScaledImage();
      JButton mainbtn = new JButton(icn);
      pnl.add(mainbtn,BorderLayout.CENTER);
      mainbtn.addActionListener(new Interactor(this));
      Box bx = Box.createHorizontalBox();
      bx.add(Box.createHorizontalGlue());
      accept_button = new JToggleButton("ACCEPT");
      accept_button.addActionListener(this);
      accept_button.setSelected(false);
      bx.add(accept_button);
      bx.add(Box.createHorizontalGlue());
      reject_button = new JToggleButton("REJECT");
      reject_button.setSelected(true);
      reject_button.addActionListener(this);
      bx.add(reject_button);
      bx.add(Box.createHorizontalGlue());
      pnl.add(bx,BorderLayout.SOUTH);

      return pnl;
    }

   private ImageIcon getScaledImage() {
      if (scaled_image == null && user_image != null) {
         double uw = user_image.getWidth();
         double uh = user_image.getHeight();
         double ws = IMAGE_WIDTH/uw;
         double hs = IMAGE_HEIGHT/uh;
         double scale = Math.min(ws,hs);
         if (scale > 1) scale = 1;
         if (scale != 1) {
            int nw = (int) (uw*scale);
            int nh = (int) (uh*scale);
            BufferedImage out = new BufferedImage(nw,nh,BufferedImage.TYPE_INT_ARGB);
            Graphics g = out.createGraphics();
            g.drawImage(user_image,0,0,nw,nh,null);
            scaled_image = new ImageIcon(out);
          }
         else scaled_image = new ImageIcon(user_image);
       }
      return scaled_image;
    }

   byte [] getJarContents()                     { return jar_runner; }
   String getIds()                              { return for_ids; }
   boolean isAccepted()                         { return is_accepted; }

   void setAccept(boolean fg) {
      is_accepted = fg;
      updateButtons();
    }

   @Override public int compareTo(SearchResult sr) {
      double v = sr.panel_score - panel_score;
      if (v < 0) return -1;
      if (v > 0) return 1;
      return 0;
    }

   @Override public void actionPerformed(ActionEvent evt) {
      String cmd = evt.getActionCommand();
      if (cmd.equals("ACCEPT")) {
         is_accepted = true;
       }
      else if (cmd.equals("REJECT")) {
         is_accepted = false;
       }
      updateButtons();
    }

   private void updateButtons() {
      if (accept_button != null) accept_button.setSelected(is_accepted);
      if (reject_button != null) reject_button.setSelected(!is_accepted);
    }

}       // end of inner class SearchResult




/********************************************************************************/
/*                                                                              */
/*      Search Result Actions                                                   */
/*                                                                              */
/********************************************************************************/

private class Interactor implements ActionListener, Runnable {

   private SearchResult for_result;

   Interactor(SearchResult sr) {
      for_result = sr;
    }

   @Override public void actionPerformed(ActionEvent evt) {
      Thread th = new Thread(this,"TestRunner_" + for_result.getIds());
      th.start();
    }

   @Override public void run() {
      byte [] jarc = for_result.getJarContents();
      if (jarc == null) return;
      try {
         File tmpf = File.createTempFile("suiserun",".jar");
         FileOutputStream fw = new FileOutputStream(tmpf);
         fw.write(jarc);
         fw.close();
         IvyExec ex = new IvyExec("java -jar " + tmpf.getPath());
         int sts = ex.waitFor();
         for_result.setAccept(sts == 0);
       }
      catch (IOException e) {
         System.err.println("SUISE: problem running jar: " + e);
       }

    }

}       // end of inner class Interactor






/********************************************************************************/
/*                                                                              */
/*      Handle reporting the results                                            */
/*                                                                              */
/********************************************************************************/

private Element reportResults()
{
   IvyXmlWriter xw = new IvyXmlWriter();
   xw.begin("USERREPLY");
   xw.field("UID",result_uid);
   xw.begin("TESTCASE");
   xw.field("NAME",testcase_uid);
   for (SearchResult sr : result_list) {
      xw.begin("USERCASE");
      xw.field("IDS",sr.getIds());
      xw.field("STATE",(sr.isAccepted() ? "PASS" : "FAIL"));
      xw.end("USERCASE");
    }
   xw.end("TESTCASE");
   xw.end("USERREPLY");
   Element rslt = SuiseMain.sendMessageToS6(xw.toString());
   xw.close();

   if (rslt == null) {
      rslt = IvyXml.convertStringToXml("<FAIL />");
    }

   return rslt;
}



private class Finisher extends WindowAdapter implements ActionListener {

   private boolean have_results;
   private boolean window_closed;
   private Element search_results;

   Finisher() {
      have_results = false;
      window_closed = false;
      search_results = null;
    }

   boolean isClosed()                   { return window_closed; }

   @Override public void actionPerformed(ActionEvent evt) {
      SuiseResultPanel.this.setVisible(false);
      getResults();
    }

   @Override public void windowClosed(WindowEvent e) {
       window_closed = true;
       if (have_results) getResults();
    }

   private void getResults() {
      Element rslt = reportResults();
      synchronized (this) {
         if (search_results == null) {
            search_results = rslt;
            notifyAll();
          }
       }
    }

   Element waitForResults() {
      have_results = true;
      if (window_closed) getResults();
      synchronized (this) {
         while (search_results == null) {
            try {
               wait();
             }
            catch (InterruptedException e) { }
          }
       }
      return search_results;
    }

}       // end of inner class Finisher



/********************************************************************************/
/*                                                                              */
/*      Test program                                                            */
/*                                                                              */
/********************************************************************************/

public static void main(String [] args)
{
   SuiseMain sm = new SuiseMain(args);
   SuiseResultPanel pnl = new SuiseResultPanel(sm);
   pnl.setVisible(true);

   try {
      Thread.sleep(5000);
    }
   catch (InterruptedException e) { }

   Element rslt = IvyXml.loadXmlFromFile("/pro/s6/suise/src/testout2.s6");
   pnl.processResult(rslt);
}




}       // end of class SuiseResultPanel




/* end of SuiseResultPanel.java */


