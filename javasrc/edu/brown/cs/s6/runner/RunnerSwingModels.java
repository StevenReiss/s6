/********************************************************************************/
/*										*/
/*		RunnerSwingModels.java						*/
/*										*/
/*	Default list, table, tree models for UI interaction			*/
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

import javax.swing.DefaultListModel;
import javax.swing.ListModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;




public class RunnerSwingModels
{



/********************************************************************************/
/*										*/
/*	Access methods to get models						*/
/*										*/
/********************************************************************************/

public static ListModel<String> getListModel()
{
   return new RunnerListModel();
}



public static TableModel getTableModel()
{
   return new RunnerTableModel();
}


public static TreeModel getTreeModel()
{
   return new RunnerTreeModel();
}




/********************************************************************************/
/*										*/
/*	Simple List Model							*/
/*										*/
/********************************************************************************/

private static class RunnerListModel extends DefaultListModel<String> {

   private static final long serialVersionUID = 1;

   RunnerListModel() {
      add(0,"First Element");
      add(1,"Second Element");
      add(2,"Third Element");
      add(3,"Fourth Element");
    }

}	// end of inner class RunnerListModel




/********************************************************************************/
/*										*/
/*	Simple Table Model							*/
/*										*/
/********************************************************************************/


private static class RunnerTableModel extends DefaultTableModel {

   static private final long serialVersionUID = 1;


   RunnerTableModel() {
      addColumn("Column I");
      addColumn("Column II");
      addColumn("Column III");
      addColumn("Column IV");
      addRow(new String [] { "Row 1", "Value 1", "Type 1", "Data 1" });
      addRow(new String [] { "Row 2", "Value 2", "Type 2", "Data 2" });
      addRow(new String [] { "Row 3", "Value 3", "Type 3", "Data 3" });
      addRow(new String [] { "Row 4", "Value 4", "Type 4", "Data 4" });
      addRow(new String [] { "Row 5", "Value 5", "Type 5", "Data 5" });
    }

}	// end of inner class RunnerTableModel



/********************************************************************************/
/*										*/
/*	Simple Tree Model							*/
/*										*/
/********************************************************************************/

private static class RunnerTreeModel extends DefaultTreeModel {

   private static final long serialVersionUID = 1;

   RunnerTreeModel() {
      super(new DefaultMutableTreeNode("Root",true));
      DefaultMutableTreeNode root = (DefaultMutableTreeNode) getRoot();
      addChildren(root,4,3);
    }

   private void addChildren(DefaultMutableTreeNode par,int ct,int lvls) {
      String pnm = par.getUserObject().toString();
      if (pnm.equals("Root")) pnm = "Level";

      for (int i = 1; i <= ct; ++i) {
	 String nm = pnm + "_" + i;
	 DefaultMutableTreeNode tn = new DefaultMutableTreeNode(nm,(lvls > 0));
	 par.add(tn);
	 if (lvls > 0) {
	    int nct = (int) Math.random()*5;
	    addChildren(tn,nct,lvls-1);
	  }
       }
    }

}	// end of inner class RunnerTreeModel




}	// end of class RunnerSwingModels




/* end of RunnerSwingModels.java */
