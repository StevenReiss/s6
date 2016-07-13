/********************************************************************************/
/*                                                                              */
/*              SuiseCodePanel.java                                             */
/*                                                                              */
/*      Panel to display the resultant code                                     */
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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;

import jsyntaxpane.DefaultSyntaxKit;

import org.w3c.dom.Element;

import edu.brown.cs.ivy.swing.SwingGridPanel;
import edu.brown.cs.ivy.xml.IvyXml;




class SuiseCodePanel extends JFrame implements SuiseConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private List<CodeSolution>      code_solutions;
private JPanel                  solution_panel;

private static final long serialVersionUID = 1;


static {
   DefaultSyntaxKit.initKit();
}


/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

SuiseCodePanel(SuiseMain sm,Element solns)
{
   super("User Interface Code Results");
   
   setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
   
   code_solutions = new ArrayList<CodeSolution>();
   
   if (IvyXml.isElement(solns,"RESULT")) solns = IvyXml.getChild(solns,"SOLUTIONS");
   for (Element ce : IvyXml.children(solns,"SOLUTION")) {
      CodeSolution cs = new CodeSolution(ce);
      code_solutions.add(cs);
    }
   
   setupPanel();
}



/********************************************************************************/
/*                                                                              */
/*      Panel setup methods                                                     */
/*                                                                              */
/********************************************************************************/

private void setupPanel()
{
   int pnlw = 1;
   SwingGridPanel pnl = new SwingGridPanel();
   JLabel ttl = new JLabel("User Interface Code Results",JLabel.CENTER);
   ttl.setFont(ttl.getFont().deriveFont(Font.BOLD,16f));
   int y = 0;
   pnl.addGBComponent(ttl,0,y++,pnlw,1,10,0);
   
   // add sort by and format by options
   
   if (code_solutions.isEmpty()) {
      JLabel zlbl = new JLabel("No Results Found",JLabel.CENTER);
      zlbl.setBackground(Color.RED);
      pnl.addGBComponent(zlbl,0,y++,pnlw,1,10,0);
    }
   else {
      solution_panel = new JPanel(new GridLayout(0,1,0,10));
      for (CodeSolution cs : code_solutions) {
         JComponent jc = cs.getPanel();
         if (jc != null) solution_panel.add(jc);
       }
      JScrollPane jsp = new JScrollPane(solution_panel);
      pnl.addGBComponent(jsp,0,y++,pnlw,1,10,10);
    }
   
   setContentPane(pnl);
   pack();
}






/********************************************************************************/
/*                                                                              */
/*      Solution representation                                                 */
/*                                                                              */
/********************************************************************************/

private class CodeSolution {
  
   private String source_code;
   private String solution_name;
   
   CodeSolution(Element xml) {
      source_code = IvyXml.getTextElement(xml,"CODE");
      solution_name = IvyXml.getTextElement(xml,"NAME");
    }
   
   JPanel getPanel() {
      JPanel pnl = new JPanel(new BorderLayout());
      Border bb1 = BorderFactory.createLineBorder(Color.GRAY);
      Border bb2 = BorderFactory.createTitledBorder(bb1,solution_name);
      pnl.setBorder(bb2);
      JEditorPane ep = new JEditorPane();
      Font oft = ep.getFont();
      Font ft = new Font(Font.MONOSPACED,Font.PLAIN,oft.getSize());
      ep.setFont(ft);
      ep.setEditable(false);
      JScrollPane jsp = new JScrollPane(ep);
      
      pnl.add(jsp,BorderLayout.CENTER);
      pnl.setPreferredSize(new Dimension(700,300));
      pnl.setMinimumSize(new Dimension(300,300));
      ep.setContentType("text/java");
      ep.setText(source_code);
      
      // add option to get license
      // add display of sizes
      // add option to goto source in browser
      // add tooltips
      return pnl;
    }
   
}       // end of inner class CodeSolution



/********************************************************************************/
/*                                                                              */
/*      Test program                                                            */
/*                                                                              */
/********************************************************************************/

public static void main(String [] args)
{
   SuiseMain sm = new SuiseMain(args);
   Element rslt = IvyXml.loadXmlFromFile("/pro/s6/suise/src/testrslt.s6");
   SuiseCodePanel pnl = new SuiseCodePanel(sm,rslt);
   pnl.setVisible(true);
}




}       // end of class SuiseCodePanel




/* end of SuiseCodePanel.java */

