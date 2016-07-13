/********************************************************************************/
/*										*/
/*		RunnerS6HierData.java                                           */
/*										*/
/*	User interface hierarchy data representation				*/
/*										*/
/********************************************************************************/
/*	Copyright 2013 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2013, Brown University, Providence, RI.				 *
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



package edu.brown.cs.s6.runner;

import java.awt.geom.Rectangle2D;


public class RunnerS6HierData
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private String comp_id;
private double comp_x;
private double comp_y;
private double comp_w;
private double comp_h;
private String comp_types;
private String top_anchor;
private String bottom_anchor;
private String left_anchor;
private String right_anchor;
private String comp_data;
private int num_children;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public RunnerS6HierData(String id,double x,double y,double w,double h,String typs,
      String ta,String ba,String la,String ra,String data,int c)
{
   comp_id = id;
   comp_x = x;
   comp_y = y;
   comp_w = w;
   comp_h = h;
   comp_types = typs;
   top_anchor = ta;
   bottom_anchor = ba;
   left_anchor = la;
   right_anchor = ra;
   comp_data = data;
   num_children = c;
}


/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

boolean classCompatible(Class<?> c)
{
   for (Class<?> pc = c; pc != null; pc = pc.getSuperclass()) {
      String nm = pc.getName();
      if (comp_types.contains(nm)) return true;
    }
   for (Class<?> pc : c.getInterfaces()) {
      String nm = pc.getName();
      if (comp_types.contains(nm)) return true;
    }
   return false;
}



int getChildCount()				{ return num_children; }
String getId()					{ return comp_id; }
String getUserData()				{ return comp_data; }
String getTypes()				{ return comp_types; }

String getTopAnchor()				{ return top_anchor; }
String getBottomAnchor()			{ return bottom_anchor; }
String getLeftAnchor()				{ return left_anchor; }
String getRightAnchor() 			{ return right_anchor; }

Rectangle2D getUserPos() {
   return new Rectangle2D.Double(comp_x,comp_y,comp_w,comp_h);
}

}	// end of class RunnerS6HierData




/* end of RunnerS6HierData.java */

