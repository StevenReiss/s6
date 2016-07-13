/********************************************************************************/
/*										*/
/*		PencilHierData.java					      */
/*										*/
/*	Pencil user interface hierarchy data representation				*/
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
import java.util.ArrayList;
import java.util.List;

public class RunnerPencilHierData
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
private int screenWidth;
private int screenHeight;
private List<RunnerPencilHierData> child_elements;


/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public RunnerPencilHierData(){
	child_elements = new ArrayList<RunnerPencilHierData>();
}


/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/


int getChildCount()				{ return num_children; }
String getId()					{ return comp_id; }
String getUserData()				{ return comp_data; }
String getTypes()				{ return comp_types; }

String getTopAnchor()				{ return top_anchor; }
String getBottomAnchor()			{ return bottom_anchor; }
String getLeftAnchor()				{ return left_anchor; }
String getRightAnchor() 			{ return right_anchor; }

int getScreenWidth()				{ return screenWidth; }
int getScreenHeight()			{ return screenHeight; }

Rectangle2D getUserPos() {
   return new Rectangle2D.Double(comp_x,comp_y,comp_w,comp_h);
}

void setChildCount(int num_children)				{ this.num_children = num_children; }
void setId(String comp_id)					{ this.comp_id = comp_id; }
void setUserData(String comp_data)				{ this.comp_data = comp_data; }
void setTypes(String comp_types)				{ this.comp_types = comp_types; }

void setTopAnchor(String top_anchor)				{ this.top_anchor = top_anchor; }
void setBottomAnchor(String bottom_anchor)			{ this.bottom_anchor = bottom_anchor; }
void setLeftAnchor(String left_anchor)				{ this.left_anchor = left_anchor; }
void setRightAnchor(String right_anchor)			{ this.right_anchor = right_anchor; }

void setX(double comp_x)			{ this.comp_x = comp_x; }
void setY(double comp_y)			{ this.comp_y = comp_y; }
void setWidth(double comp_w)			{ this.comp_w = comp_w; }
void setHeight(double comp_h)			{ this.comp_h = comp_h; }

void setScreenWidth(int screenWidth){
	this.screenWidth = screenWidth;
}
void setScreenHeight(int screenHeight){
	this.screenHeight = screenHeight;
}

void addChild(RunnerPencilHierData e)	{ child_elements.add(e); }
List<RunnerPencilHierData> getChildren()	{ return child_elements; }
boolean hasChildren(){
	if(child_elements.size() > 0)	return true;
	else return false;
}

}	// end of class PencilHierData


/* end of PencilHierData.java */

