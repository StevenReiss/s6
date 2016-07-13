/********************************************************************************/
/*                                                                              */
/*              SuiseSearchProcessor.java                                       */
/*                                                                              */
/*      Setup and perform a search                                              */
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

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import edu.brown.cs.ivy.exec.IvyExec;
import edu.brown.cs.ivy.xml.IvyXmlWriter;


class SuiseSearchProcessor implements SuiseConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private SuiseRawComponent       root_component;
private String                  search_scope;
private String                  key_words;
private String                  search_source;
private boolean                 for_android;


/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

SuiseSearchProcessor(SuiseRawComponent c,String mode,String keys,String src,boolean and)
{
   root_component = c;
   search_scope = mode;
   key_words = keys;
   search_source = src;
   for_android = and;
}



/********************************************************************************/
/*                                                                              */
/*      Start a search                                                          */
/*                                                                              */
/********************************************************************************/

Element doSearch()
{
   String cnts = setupSearch();
   if (cnts == null) return null;
   Element rslt = SuiseMain.sendMessageToS6(cnts);

   return rslt;
}



/********************************************************************************/
/*                                                                              */
/*      Methods to setup the search                                             */
/*                                                                              */
/********************************************************************************/

private String setupSearch()
{
   String baseclass = (for_android ? "android.view.View" : "java.awt.Component");

   IvyXmlWriter xw = new IvyXmlWriter();
   xw.begin("SEARCH");
   xw.field("WHAT","UIFRAMEWORK");
   xw.field("FORMAT","NONE");
   outputSource(xw);
   xw.field("SCOPE",search_scope);

   xw.begin("SIGNATURE");
   xw.begin("UI");
   xw.field("PACKAGE","spr.sampler.uitest");
   xw.field("CLASS","S6_UI_CLASS");
   outputComponents(root_component,xw);
   xw.begin("CLASS");
   xw.field("NAME","S6_UI_CLASS");
   xw.begin("METHOD");
   xw.field("NAME","S6_UI");
   xw.field("RETURN",baseclass);
   xw.end("METHOD");
   xw.end("CLASS");
   xw.end("UI");
   xw.end("SIGNATURE");

   xw.begin("REQUIRE");
   xw.end("REQUIRE");

   xw.begin("TESTS");
   xw.begin("TESTCASE");
   xw.field("NAME","SVIUI_1");
   xw.field("TYPE","CALLS");
   xw.begin("CALL");
   xw.field("METHOD","S6_UI_CLASS");
   xw.field("NEW",true);
   xw.field("OP","SAVE");
   xw.begin("OUTPUT");
   xw.field("TYPE","SAVE");
   xw.field("VALUE","x");
   xw.cdataElement("CODE","S6_UI_CLASS x;");
   xw.end("OUTPUT");
   xw.end("CALL");
   xw.begin("CALL");
   xw.field("METHOD","S6_UI");
   xw.field("THIS","x");
   xw.field("OP","SAVE");
   xw.begin("OUTPUT");
   xw.field("TYPE","SAVE");
   xw.field("VALUE","y");
   xw.cdataElement("CODE",baseclass + " y;");
   xw.end("OUTPUT");
   xw.end("CALL");
   xw.begin("CALL");
   xw.field("OP","SCOREHIER");
   xw.field("THIS","y");
   xw.begin("OUTPUT");
   xw.field("TYPE","SAVE");
   xw.field("VALUE","__score__");
   xw.cdataElement("CODE","double __score__;");
   xw.end("OUTPUT");
   xw.end("CALL");
   xw.begin("CALL");
   xw.field("OP","INTERACT");
   xw.field("THIS","y");
   xw.begin("INPUT");
   xw.field("TYPE","VARIABLE");
   xw.field("VALUE","__score__");
   xw.end("INPUT");
   xw.end("CALL");
   xw.end("TESTCASE");
   xw.end("TESTS");

   outputKeywords(xw);

   xw.end("SEARCH");

   System.err.println("SEARCH STRING = " + xw.toString());

   return xw.toString();
}



/********************************************************************************/
/*                                                                              */
/*      Determine where to search                                               */
/*                                                                              */
/********************************************************************************/

private void outputSource(IvyXmlWriter xw)
{
   switch (search_source) {
      case "LOCAL" :
         xw.field("LOCAL",true);
         break;
      default :
      case "KODERS" :
      case "OHLOH" :
      case "GOOGLE" :
         xw.field("REMOTE",true);
         xw.field("OHLOH",true);
         break;
      case "GITHUB" :
      case "GOOGIT" :
         xw.field("GITHUB",true);
         break;
      case "SOURCERER" :
         xw.field("SOURCERER",true);
         break;
    }
}




/********************************************************************************/
/*                                                                              */
/*      Handle component output                                                 */
/*                                                                              */
/********************************************************************************/


private void outputComponents(SuiseRawComponent c,IvyXmlWriter xw)
{
   if (!ignoreItem(c)) {
      xw.begin("COMPONENT");
      xw.field("ID",c.getId());
      xw.field("X",Math.round(c.getX()));
      xw.field("Y",Math.round(c.getY()));
      xw.field("WIDTH",Math.round(c.getWidth()));
      xw.field("HEIGHT",Math.round(c.getHeight()));
      outputAnchor(c.getTopAnchor(),"TOP",xw);
      outputAnchor(c.getBottomAnchor(),"BOTTOM",xw);
      outputAnchor(c.getLeftAnchor(),"LEFT",xw);
      outputAnchor(c.getRightAnchor(),"RIGHT",xw);
      if (c.getText() != null) xw.field("DATA",c.getText());
      outputTypes(c,xw);
    }

   for (SuiseRawComponent chld : c.getChildren()) {
      outputComponents(chld,xw);
    }

   if (!ignoreItem(c)) {
      xw.end("COMPONENT");
    }
}


private void outputTypes(SuiseRawComponent c,IvyXmlWriter xw)
{
   List<String> typs = new ArrayList<String>();

   if (c.isText() || c.isIcon()) {
      typs.add("javax.swing.JLabel");
      typs.add("android.view.ImageView");
      typs.add("android.widget.TextView");
      if (c.isMultiline()) {
         typs.add("javax.swing.text.JTextComponent");
       }
    }
   if (c.isChoice()) {
      typs.add("javax.swing.JComboBox");
      typs.add("android.widget.BaseAdapter");
    }
   if (c.isTable()) {
      typs.add("javax.swing.JTable");
      typs.add("android.widget.GridView");
    }
   if (c.isList()) {
      typs.add("javax.swing.JList");
      typs.add("javax.swing.JTable");           // one-column table would work as list
      typs.add("android.widget.ListView");
      typs.add("android.widget.GridView");
    }
   if (c.isTree()) typs.add("javax.swing.JTree");
   if (c.isScrollBar() && !c.isDrawing()) {
      typs.add("javax.swing.JScrollBar");
      typs.add("javax.swing.JScrollPane");
      typs.add("android.widget.Scroller");
      typs.add("android.widget.ScrollView");
    }
   if (c.isSlider()) {
      typs.add("javax.swing.JSlider");
      typs.add("android.widget.Switch");
      typs.add("android.widget.RatingBar");
      typs.add("android.widget.ProgressBar");
      typs.add("android.widget.NumberPicker");
    }
   if (c.isNumeric()) {
      typs.add("javax.swing.JSpinner");
      typs.add("android.widget.Spinner");
      typs.add("android.widget.NumberPicker");
    }
   if (c.isMenuBar()) {
      typs.add("javax.swing.JMenuBar");
      typs.add("java.awt.MenuBar");
      typs.add("javax.swing.JPanel");
      typs.add("android.widget.ActionMenuView");
    }
   if (c.isToolBar()) {
      typs.add("javax.swing.JToolBar");
      typs.add("android.widget.Toolbar");
    }
   if (c.isDrawing()) {
      typs.add("java.swing.JPanel");
      typs.add("java.swing.JLabel");
      typs.add("android.view.View");
      if (c.isScrollBar()) {
         typs.add("javax.swing.JScrollPane");
         typs.add("android.widget.ScrollView");
       }
    }
   if (c.isOption()) {
      typs.add("javax.swing.JToggleButton");
      typs.add("javax.swing.JCheckBoxMenuItem");
      typs.add("javax.swing.JRadioButtonMenuItem");
      typs.add("android.widget.CompoundButton");
    }
   if (c.isButton() && !c.isOption()) {
      typs.add("javax.swing.JButton");
      typs.add("javax.swing.JMenuItem");
      typs.add("android.widget.Button");
    }
   if (c.isInput()) {
      if (c.isMultiline()) {
         typs.add("javax.swing.JTextArea");
         typs.add("javax.swing.JEditorPane");
         typs.add("android.widget.TextView");
       }
      else if (c.isPassword()) {
         typs.add("javax.swing.JPasswordField");
         typs.add("android.widget.TextView");
       }
      else {
         typs.add("javax.swing.JTextField");
         typs.add("android.widget.TextView");
       }
    }
   if (typs.isEmpty()) {
      typs.add("java.awt.Container");
      typs.add("android.view.View");
    }

   StringBuffer buf = new StringBuffer();
   for (int i = 0; i < typs.size(); ++i) {
      if (i > 0) buf.append(",");
      buf.append(typs.get(i));
    }

   xw.field("TYPES",buf.toString());
}



private void outputAnchor(SuiseRawComponent c,String what,IvyXmlWriter xw)
{
   if (c == null) return;

   xw.field(what,c.getId());
}


private boolean ignoreItem(SuiseRawComponent c)
{
   if (c.isLine()) return true;
   if (c.isGroup()) return true;
   if (c.isSymbol()) return true;
   return false;
}




/********************************************************************************/
/*                                                                              */
/*      Generate keyword set                                                    */
/*                                                                              */
/********************************************************************************/

private void outputKeywords(IvyXmlWriter xw)
{
   // take care of quotes
   List<String> toks = IvyExec.tokenize(key_words);

   xw.begin("KEYWORDS");
   for (String s : toks) {
      xw.cdataElement("KEYWORD",s);
    }
   xw.end("KEYWORDS");
}







}       // end of class SuiseSearchProcessor




/* end of SuiseSearchProcessor.java */

