/********************************************************************************/
/*										*/
/*		RunnerSwingMatcher.java 					*/
/*										*/
/*	Class to let do matching of Swing components				*/
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


import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.TreeSet;

import javax.swing.AbstractButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import edu.brown.cs.ivy.file.IvyLog;


class RunnerSwingMatcher
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private HierData	root_data;
private Component	root_component;
private CompSoln	root_candidates;
private double		best_score;
private int		num_soln;
private int		num_dummy;

private static final int	NUM_DUMMY = 2;

private static final int	MAX_SOLNS = 1000;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

RunnerSwingMatcher(RunnerS6HierData [] userdata,Component root)
{
   root_data = null;
   root_component = root;
   root_candidates = null;
   num_soln = 0;

   num_dummy = NUM_DUMMY;
   num_dummy = Math.min(NUM_DUMMY,userdata.length / 5);

   setupHierarchyData(userdata);
   setupPossibleMappings();
}



/********************************************************************************/
/*										*/
/*	Matching interface							*/
/*										*/
/********************************************************************************/

double computeMatch()
{
   best_score = -1;

   if (root_candidates.getComponents(root_data) == null) return -1;

   root_candidates.removeDummys();

   for (int i = 0; i <= num_dummy; ++i) {
      HierSoln hs = new HierSoln();
      if (quickCheck(i)) {
	 match(hs,root_candidates,0);
       }
      if (best_score >= 0) {
	 // best_score -= 100*i;
	 // if (best_score < 0) best_score = 0;
	 break;
       }
      DummyComponent dc = new DummyComponent();
      root_candidates.addDummy(dc);
    }

  IvyLog.logD("RUNNER","COMPUTE " + best_score + " " + num_soln);

   if (best_score < 0) {
      // org.junit.Assert.fail("S6UIMatch: HIERARCHY");
    }
   if (best_score == 0) best_score = 1;

   return best_score;
}





/********************************************************************************/
/*										*/
/*	Setup methods								*/
/*										*/
/********************************************************************************/

private void setupHierarchyData(RunnerS6HierData [] userdata)
{
   Stack<HierData> pars = new Stack<HierData>();
   Map<String,HierData> cmap = new HashMap<String,HierData>();

   pars.push(null);
   HierData par = null;
   for (int i = 0; i < userdata.length; ++i) {
      HierData hd = new HierData(userdata[i]);
      cmap.put(userdata[i].getId(),hd);
      if (par == null) root_data = hd;
      else {
	 par.addChild(hd);
       }
      if (!hd.childrenGiven()) {
	 pars.push(par);
	 par = hd;
       }
      else while (par != null && par.childrenGiven()) {
	 par = pars.pop();
       }
    }

   root_data.fixup(cmap);
}



private void setupPossibleMappings()
{
   root_candidates = new CompSoln(root_data,null);
   CompSoln base = findCandidates(root_candidates,root_data,root_component);
   if (base == null) return;
   root_candidates.addMatch(root_data,root_component,base);
}



private CompSoln findCandidates(CompSoln par,HierData hd,Component c)
{
   CompSoln cs = new CompSoln(hd,par);
   if (!hd.hasChildren()) return cs;

   for (HierData cd : hd.children()) {
      List<Component> cands = getAllChildren(c,null,cd);
      int ct = 0;
      if (!cd.hasChildren()) {
	  for (Component xc : cands) {
	     ++ct;
	     cs.addMatch(cd,xc,null);
	   }
       }
      else {
	 for (Component xc : cands) {
	    CompSoln smat = findCandidates(cs,cd,xc);
	    if (smat != null) {
	       cs.addMatch(cd,xc,smat);
	       ++ct;
	     }
	  }
       }
      if (ct == 0) {
	 // allow empty sub solutinos.	These should be eliminated
	 // pretty quickly unless enough dummy components are available.
	 // return null;
       }
    }

   return cs;
}





private List<Component> getAllChildren(Component c,List<Component> rslt,HierData hd)
{
   if (rslt != null && hd.classCompatible(c)) rslt.add(c);

   if (rslt == null) rslt = new ArrayList<Component>();

   if (c instanceof Container) {
      Container p = (Container) c;
      for (int i = 0; i < p.getComponentCount(); ++i) {
	 getAllChildren(p.getComponent(i),rslt,hd);
       }
    }
   return rslt;
}




/********************************************************************************/
/*										*/
/*	Quick check to see if match is possible 				*/
/*										*/
/********************************************************************************/

private boolean quickCheck(int ndummy)
{
   List<HierData> allhd = new ArrayList<HierData>();
   List<Component> allcomp = new ArrayList<Component>();
   setupHDList(root_data,allhd);
   setupCompList(root_component,allcomp);
   if (allhd.size() > allcomp.size()) return false;

   Set<String> done = new HashSet<String>();
   for (HierData hd : allhd) {
      String what = hd.getTypeString();
      if (done.contains(what)) continue;
      done.add(what);
      Set<String> typs = hd.getTypes();
      int ct = 0;
      for (HierData hd1 : allhd) {
	 if (typs.containsAll(hd1.getTypes())) ++ct;
       }
      int cct = 0;
      for (Component c : allcomp) {
	 if (hd.classCompatible(c)) ++cct;
       }
      if (IvyLog.isDebug()) IvyLog.logD("RUNNER","QUICK " + what + " " + ct + " " + cct);
      if (cct+ndummy < ct) return false;
    }

   return true;
}


private void  setupHDList(HierData hd,List<HierData> rslt)
{
   rslt.add(hd);
   if (hd.hasChildren()) {
      for (HierData chd : hd.children()) setupHDList(chd,rslt);
    }
}

private void setupCompList(Component c,List<Component> rslt)
{
   rslt.add(c);
   if (c instanceof Container) {
      Container p = (Container) c;
      for (int i = 0; i < p.getComponentCount(); ++i) {
	 setupCompList(p.getComponent(i),rslt);
       }
    }
}


/********************************************************************************/
/*										*/
/*	Build an actual match							*/
/*										*/
/********************************************************************************/

private void match(HierSoln start,CompSoln soln,int idx)
{
    IvyLog.logD("RUNNER","MATCH " + soln + " " + idx);

   if (soln == null) {
      double v = start.getValue(root_component);
      if (v > best_score) {
	 best_score = v;
       }
      ++num_soln;
      IvyLog.logD("RUNNER","HIERARCHY SOLUTION " + start + " VALUE = " + v);
      return;
    }
   if (num_soln >= MAX_SOLNS) return;

   List<HierData> lhd = soln.getElements();
   if (idx >= lhd.size()) {
      CompSoln psol = soln.getParent();
      int nidx = 0;
      if (psol != null) nidx = psol.findIndexOf(soln) + 1;
      match(start,psol,nidx);
      return;
    }

   HierData hd = lhd.get(idx);
   for (CompSet cs : soln.getComponents(hd)) {
      // what to do if this is a second dummy comopnent
      if (start.isUsed(cs.getComponent()))
	 continue;
      start.addToSolution(hd,cs.getComponent());
      CompSoln ssoln = cs.getSubSolution();
      if (ssoln != null) {
	 match(start,ssoln,0);
       }
      else {
	 match(start,soln,idx+1);
       }
      start.removeFromSolution(hd,cs.getComponent());
    }
}



private static String componentName(Component c)
{
   StringBuffer buf = new StringBuffer();

   String ctyp = c.getClass().getName();
   int idx = ctyp.lastIndexOf(".");
   if (idx >= 0) ctyp = ctyp.substring(idx+1);
   buf.append(ctyp);
   buf.append(" ");
   buf.append(c.getX());
   buf.append(",");
   buf.append(c.getY());
   buf.append(" ");
   buf.append(c.getWidth());
   buf.append("x");
   buf.append(c.getHeight());

   return buf.toString();

}



/********************************************************************************/
/*										*/
/*	Representation of a solution						*/
/*										*/
/********************************************************************************/

private static class HierSoln {

   private Map<HierData,Component> solution_map;
   private Set<Component>	   components_used;

   HierSoln() {
      solution_map = new HashMap<HierData,Component>();
      components_used = new HashSet<Component>();
    }

   boolean isUsed(Component c) {
      return components_used.contains(c);
    }

   void addToSolution(HierData hd,Component c) {
      IvyLog.logD("RUNNER","ADD TO SOLUTION " + hd + " " + c);
      solution_map.put(hd,c);
      components_used.add(c);
    }

   void removeFromSolution(HierData hd,Component c) {
      IvyLog.logD("RUNNER","REMOVE FROM SOLUTION " + hd + " " + c);
      solution_map.remove(hd);
      components_used.remove(c);
    }

   double getValue(Component root) {
      double val = 0;
      Set<Component> used = new HashSet<Component>();
      for (Map.Entry<HierData,Component> ent : solution_map.entrySet()) {
	 HierData hd = ent.getKey();
	 Component c = ent.getValue();
	 val += hd.getValue(c,this);
	 used.add(c);
       }

      double cnt = countUnusedComponents(root,used);
      val -= cnt*20;
      if (val < 0) val = 0;

      return val;
    }

   private double countUnusedComponents(Component c,Set<Component> used) {
      double rslt = 0;
      if (c instanceof Container) {
	 Container cc = (Container) c;
	 for (int i = 0; i < cc.getComponentCount(); ++i) {
	    rslt += countUnusedComponents(cc.getComponent(i),used);
	  }
       }

      if (used.contains(c)) return rslt;

      if (c instanceof JLabel) ++rslt;
      else if (c instanceof AbstractButton) rslt += 2;
      else if (c instanceof JComboBox) rslt += 2;
      else if (c instanceof JTextComponent) rslt += 3;
      else if (c instanceof JList) rslt += 3;
      else if (c instanceof JTable) rslt += 3;
      else if (c instanceof JTree) rslt += 3;

      return rslt;
   }

   Component getComponent(HierData hd)		{ return solution_map.get(hd); }

   @Override public String toString() {
      StringBuffer buf = new StringBuffer();
      buf.append("SOLN:[ ");
      for (Map.Entry<HierData,Component> ent : solution_map.entrySet()) {
	 buf.append(ent.getKey().getName());
	 buf.append(" = ");
	 buf.append(componentName(ent.getValue()));
	 buf.append(" ;  ");
       }
      buf.append("]");
      return buf.toString();
    }

}	// end of inner class HierSoln




/********************************************************************************/
/*										*/
/*	Representation of Alternatives						*/
/*										*/
/********************************************************************************/

private static class CompSoln {

   private HierData for_hier;
   private Map<HierData,List<CompSet>> cand_space;
   private CompSoln parent_soln;
   private List<HierData> element_list;

   CompSoln(HierData hd,CompSoln par) {
      for_hier = hd;
      cand_space = new HashMap<HierData,List<CompSet>>();
      parent_soln = par;
      element_list = null;
    }

   void addMatch(HierData hd,Component c,CompSoln sub) {
      List<CompSet> ls = cand_space.get(hd);
      if (ls == null) {
         ls = new ArrayList<CompSet>();
         cand_space.put(hd,ls);
         element_list = null;
       }
      ls.add(new CompSet(c,sub));
      if (IvyLog.isDebug()) {
         IvyLog.logD("RUNNER","ADD MATCH " + hd + " " + ls.size() + " " + hashCode() + " " + c);
       }
    }


   List<HierData> getElements() {
      if (element_list == null) {
	 element_list = new ArrayList<HierData>(cand_space.keySet());
       }
      return element_list;
    }

   List<CompSet> getComponents(HierData hd) {
      return cand_space.get(hd);
    }

   CompSoln getParent() 		{ return parent_soln; }

   int findIndexOf(CompSoln child) {
      for (int i = 0; i < element_list.size(); ++i) {
	 List<CompSet> csl = cand_space.get(element_list.get(i));
	 for (CompSet cs : csl) {
	    if (cs.getSubSolution() == child) return i;
	  }
       }
      return element_list.size();
    }

   void removeDummys() {
      for (List<CompSet> lcs : cand_space.values()) {
	 for (Iterator<CompSet> it = lcs.iterator(); it.hasNext(); ) {
	    CompSet cs = it.next();
	    if (cs.getComponent() instanceof DummyComponent) it.remove();
	    else if (cs.getSubSolution() != null) {
	       cs.getSubSolution().removeDummys();
	     }
	  }
       }
    }

   void addDummy(DummyComponent dc) {
      if (IvyLog.isDebug())
	 IvyLog.logD("RUNNER","ADD DUMMY " + for_hier + " " + hashCode() + " " + parent_soln);

      if (for_hier.hasChildren()) {
	 if (parent_soln != null) {
	    for (HierData hd : for_hier.children()) {
	       if (!hd.hasChildren()) addMatch(hd,dc,null);
	     }
	  }
	 for (List<CompSet> lcs : cand_space.values()) {
	    for (CompSet cs : lcs) {
	       if (cs.getSubSolution() != null) {
		  cs.getSubSolution().addDummy(dc);
		}
	     }
	  }
       }
    }

   @Override public String toString() {
      StringBuffer buf = new StringBuffer();
      buf.append("CSOL: ");
      for (Map.Entry<HierData,List<CompSet>> ent : cand_space.entrySet()) {
	 buf.append(ent.getKey().getName());
	 buf.append(" => ");
	 buf.append(ent.getValue().size());
	 buf.append(", ");
       }
      return buf.toString();
    }

}	// end of inner class CompSoln



private static class CompSet {

   private Component for_component;
   private CompSoln  sub_soln;

   CompSet(Component c,CompSoln s) {
      for_component = c;
      sub_soln = s;
    }

   Component getComponent()		{ return for_component; }
   CompSoln getSubSolution()		{ return sub_soln; }

   @Override public String toString() {
      StringBuffer buf = new StringBuffer();
      buf.append("{ ");
      buf.append(componentName(for_component));
      buf.append(" :: ");
      if (sub_soln == null) buf.append("DONE");
      else buf.append(sub_soln.toString());
      buf.append(" }");
      return buf.toString();
    }

}	// end of inner class CompSet




/********************************************************************************/
/*										*/
/*	Internal representation of hierarchy data				*/
/*										*/
/********************************************************************************/

private class HierData {

   private Rectangle2D	user_pos;
   private HierData	top_anchor;
   private HierData	bottom_anchor;
   private HierData	left_anchor;
   private HierData	right_anchor;
   private String	user_data;
   private Set<String>	comp_types;
   private List<HierData> child_nodes;
   private RunnerS6HierData base_data;

   HierData(RunnerS6HierData base) {
      base_data = base;
      user_pos = base.getUserPos();
      top_anchor = null;
      bottom_anchor = null;
      right_anchor = null;
      user_data = base.getUserData();
      comp_types = new TreeSet<String>();
      StringTokenizer tok = new StringTokenizer(base.getTypes()," ,;");
      while (tok.hasMoreTokens()) comp_types.add(tok.nextToken());
      child_nodes = null;
    }

   void addChild(HierData hd) {
      if (child_nodes == null) child_nodes = new ArrayList<HierData>();
      child_nodes.add(hd);
    }

   boolean childrenGiven() {
      if (base_data.getChildCount() == 0) return true;
      if (child_nodes == null) return false;
      return base_data.getChildCount() == child_nodes.size();
    }

   void fixup(Map<String,HierData> cmap) {
      top_anchor = getData(base_data.getTopAnchor(),cmap);
      bottom_anchor = getData(base_data.getBottomAnchor(),cmap);
      left_anchor = getData(base_data.getLeftAnchor(),cmap);
      right_anchor = getData(base_data.getRightAnchor(),cmap);
      if (child_nodes != null) {
	 for (HierData hd : child_nodes) hd.fixup(cmap);
       }
    }

   private HierData getData(String d,Map<String,HierData> cmap) {
      if (d == null) return null;
      return cmap.get(d);
    }

   Set<String> getTypes()		{ return comp_types; }

   String getTypeString() {
      StringBuffer buf = new StringBuffer();
      for (String ct : comp_types) {
	 buf.append(ct);
	 buf.append(",");
       }
      return buf.toString();
    }

   boolean classCompatible(Component comp) {
      Class<?> c = comp.getClass();
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

   boolean hasChildren() {
      return child_nodes != null && child_nodes.size() > 0;
    }

   Iterable<HierData> children()		{ return child_nodes; }

   double getValue(Component c,HierSoln cs) {
      double val = 200; 			// base score for a match
      if (c instanceof DummyComponent) val = 0;
      if (user_pos.getWidth() >= 0) {
	 double w = Math.abs(c.getWidth() - user_pos.getWidth());
	 if (w < 100) val += 100-w;
       }
      if (user_pos.getHeight() >= 0) {
	 double h = Math.abs(c.getHeight() - user_pos.getHeight());
	 if (h < 100) val += 100-h;
       }
      if (user_data != null && user_data.length()  > 0) {
	 try {
	    Class<?> cls = c.getClass();
	    Method m = cls.getMethod("getText");
	    String s = (String) m.invoke(c);
	    double delta = stringDiff(s,user_data);
	    delta = delta/user_data.length();
	    if (delta > 1) delta = 1;
	    val += 100 * (1-delta);
	  }
	 catch (Throwable t) { }
       }
      Rectangle r = getBounds(c);
      if (top_anchor != null) {
	 double v = -1;
	 Component cx = cs.getComponent(top_anchor);
	 if (cx != null) {
	    Rectangle rx = getBounds(cx);
	    if (rx.contains(r)) v = r.y - rx.y;
	    else v = r.y - (rx.y + rx.height);
	    if (v >= 0 && v <= 10) val += 50;
	  }
       }
      if (bottom_anchor != null) {
	 double v = -1;
	 Component cx = cs.getComponent(bottom_anchor);
	 if (cx != null) {
	    Rectangle rx = getBounds(cx);
	    if (rx.contains(r)) v = (rx.y + rx.height) - (r.y + r.height);
	    else v = rx.y - (r.y + r.height);
	    if (v >= 0 && v <= 10) val += 50;
	  }
       }
      if (left_anchor != null) {
	 double v = -1;
	 Component cx = cs.getComponent(left_anchor);
	 if (cx != null) {
	    Rectangle rx = getBounds(cx);
	    if (rx.contains(r)) v = r.x - rx.x;
	    else v = r.x - (rx.x + rx.width);
	    if (v >= 0 && v <= 15) val += 50;
	  }
       }
      if (right_anchor != null) {
	 double v = -1;
	 Component cx = cs.getComponent(right_anchor);
	 if (cx != null) {
	    Rectangle rx = getBounds(cx);
	    if (rx.contains(r)) v = (rx.x + rx.width) - (r.x + r.width);
	    else v = rx.x - (r.x + r.width);
	    if (v >= 0 && v <= 15) val += 50;
	  }
       }
      return val;
    }

   String getName() {
      return base_data.getId();
    }

   @Override public String toString() {
      return getName();
    }

}	// end of inner class HierData





/********************************************************************************/
/*										*/
/*	Utility methods 							*/
/*										*/
/********************************************************************************/

private Rectangle getBounds(Component c)
{
   Rectangle r = c.getBounds();
   Point p = SwingUtilities.convertPoint(c,0,0,root_component);
   r.x = p.x;
   r.y = p.y;
   return r;
}



private static int stringDiff(CharSequence s,CharSequence t)
{
   int n = s.length();
   int m = t.length();
   if (n == 0) return m;
   if (m == 0) return n;

   int [][] d = new int[n+1][m+1];
   for (int i = 0; i <= n; i++) d[i][0] = i;
   for (int j = 0; j <= m; j++) d[0][j] = j;

   for (int i = 1; i <= n; ++i) {
      char s_i = s.charAt(i-1);
      for (int j = 1; j <= m; ++j) {
	 char t_j = t.charAt (j - 1);
	 int cost = (s_i == t_j ? 0 : 1);
	 d[i][j] = min3(d[i-1][j]+1,d[i][j-1]+1,d[i-1][j-1]+cost);
       }
    }

   return d[n][m];
}



private static int min3(int a, int b, int c)
{
   if (b < a) a = b;
   if (c < a) a = c;
   return a;
}


/********************************************************************************/
/*										*/
/*	Dummy component management						*/
/*										*/
/********************************************************************************/

private static class DummyComponent extends Component {

   private static final long serialVersionUID = 1;

}	// end of inner class DummyComponent



}	// end of class RunnerSwingMatcher




/* end of RunnerSwingMatcher.java */
