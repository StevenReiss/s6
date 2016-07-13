/********************************************************************************/
/*                                                                              */
/*              SuiseRawComponent.java                                          */
/*                                                                              */
/*      Information about a discovered user interface component                 */
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


class SuiseRawComponent implements SuiseConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private double comp_width;
private double comp_height;
private double x_pos;
private double y_pos;
private String comp_text;
private boolean is_input;               // text input region
private boolean is_button;              // button (w/wo text)
private boolean is_table;               // table 
private boolean is_list;                // list;
private boolean is_choice;              // choice box
private boolean is_text;                // simple text label
private boolean is_symbol;              // simple symbol
private boolean is_line;                // line
private boolean is_group;               // grouping only
private boolean is_option;              // option button
private boolean is_multiline;           // multiline text
private boolean is_scrollbar;           // is a scrollbar
private boolean is_password;            // is a password field
private boolean is_numeric;             // is a numeric field
private boolean is_slider;              // is a slider
private boolean is_drawing;             // is a drawing area
private boolean is_icon;                // is an icon/logo area
private boolean is_menubar;             // is a menu bar
private boolean is_toolbar;             // is a tool bar
private boolean is_tree;                // is a tree widget


private List<SuiseRawComponent> child_elements;
private String elt_id;
private SuiseRawComponent top_anchor;
private SuiseRawComponent bottom_anchor;
private SuiseRawComponent left_anchor;
private SuiseRawComponent right_anchor;

private static int id_counter = 0;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

SuiseRawComponent(double w,double h,double x,double y) 
{
   comp_width = w;
   comp_height = h;
   x_pos = x;
   y_pos = y;
   comp_text = null;
   child_elements = new ArrayList<SuiseRawComponent>();
   top_anchor = null;
   bottom_anchor = null;
   left_anchor = null;
   right_anchor = null;
   is_input = false;
   is_button = false;
   is_table = false;
   is_list = false;
   is_choice = false;
   is_text = false;
   is_symbol = false;
   is_line = false;
   is_group = false;
   is_option = false;
   is_multiline = false;
   is_scrollbar = false;
   is_password = false;
   is_numeric = false;
   is_slider = false;
   is_drawing = false;
   is_icon = false;
   is_menubar = false;
   is_toolbar = false;
   is_tree = false;
   
   elt_id = "U_" + (++id_counter);
}



/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

double getWidth()	                { return comp_width; }
double getHeight()			{ return comp_height; }
double getX()			        { return x_pos; }
double getY()			        { return y_pos; }
boolean hasChildren()			{ return child_elements.size() > 0; }
String getId()			        { return elt_id; }
String getText()                        { return comp_text; }

void setText(String s)                  { comp_text = s; }

boolean isInput()                       { return is_input; }
void setIsInput(boolean fg)             { is_input = fg; }
boolean isButton()                      { return is_button; }
void setIsButton(boolean fg)            { is_button = fg; }
boolean isTable()                       { return is_table; }
void setIsTable(boolean fg)             { is_table = fg; }
boolean isList()                        { return is_list; }
void setIsList(boolean fg)              { is_list = fg; }
boolean isChoice()                      { return is_choice; }
void setIsChoice(boolean fg)            { is_choice = fg; }
boolean isText()                        { return is_text; }
void setIsText(boolean fg)              { is_text = fg; }
boolean isSymbol()                      { return is_symbol; }
void setIsSymbol(boolean fg)            { is_symbol = fg; }
boolean isLine()                        { return is_line; }
void setIsLine(boolean fg)              { is_line = fg; }
boolean isGroup()                       { return is_group; }
void setIsGroup(boolean fg)             { is_group = fg; }
boolean isOption()                      { return is_option; }
void setIsOption(boolean fg)            { is_option = fg; }
boolean isMultiline()                   { return is_multiline; }
void setIsMultiline(boolean fg)         { is_multiline = fg; }
boolean isScrollBar()                   { return is_scrollbar; }
void setIsScrollBar(boolean fg)         { is_scrollbar = fg; }
boolean isPassword()                    { return is_password; }
void setIsPassword(boolean fg)          { is_password = fg; }
boolean isNumeric()                     { return is_numeric; }
void setIsNumeric(boolean fg)           { is_numeric = fg; }
boolean isSlider()                      { return is_slider; }
void setIsSlider(boolean fg)            { is_slider = fg; }
boolean isDrawing()                     { return is_drawing; }
void setIsDrawing(boolean fg)           { is_drawing = fg; }
boolean isIcon()                        { return is_icon; }
void setIsIcon(boolean fg)              { is_icon = fg; }
boolean isMenuBar()                     { return is_menubar; }
void setIsMenuBar(boolean fg)           { is_menubar = fg; }
boolean isToolBar()                     { return is_toolbar; }
void setIsToolBar(boolean fg)           { is_toolbar = fg; }
boolean isTree()                        { return is_tree; }
void setIsTree(boolean fg)              { is_tree = fg; }

SuiseRawComponent getTopAnchor()                { return top_anchor; }
void setTopAnchor(SuiseRawComponent c)          { top_anchor = c; }
SuiseRawComponent getBottomAnchor()             { return bottom_anchor; }
void setBottomAnchor(SuiseRawComponent c)       { bottom_anchor = c; }
SuiseRawComponent getLeftAnchor()               { return left_anchor; }
void setLeftAnchor(SuiseRawComponent c)         { left_anchor = c; }
SuiseRawComponent getRightAnchor()              { return right_anchor; }
void setRightAnchor(SuiseRawComponent c)        { right_anchor = c; }




/********************************************************************************/
/*                                                                              */
/*      Hierarchy management methods                                            */
/*                                                                              */
/********************************************************************************/

boolean isChild(SuiseRawComponent e)
{
   double x0 = e.getX();
   double x1 = e.getX() + e.getWidth();
   double y0 = e.getY();
   double y1 = e.getY() + e.getHeight();
   if (e.isSymbol() || e.isLine() || e.isIcon() || e.isText()) {
      double delta = 0.05;
      if (e.isSymbol() || e.isIcon()) delta = 0.25;
      double dx = (x1-x0)*delta;
      x0 += dx;
      x1 -= dx;
      double dy = (y1-y0)*delta;
      y0 += dy;
      y1 -= dy;
    }
   
   if (x_pos <= x0 && x_pos+comp_width >= x1 &&
         y_pos <= y0 && y_pos+comp_height >= y1)
      return true;
   return false;
}

void addChild(SuiseRawComponent e)	{ child_elements.add(e); }
List<SuiseRawComponent> getChildren()	{ return child_elements; }





}       // end of class SuiseRawComponent




/* end of SuiseRawComponent.java */

