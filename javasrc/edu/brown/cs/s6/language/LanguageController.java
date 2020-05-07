/********************************************************************************/
/*										*/
/*		LanguageController.java 					*/
/*										*/
/*	Tranform Heuristics implementation					*/
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



package edu.brown.cs.s6.language;


import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;

import edu.brown.cs.cose.cosecommon.CoseConstants;
import edu.brown.cs.ivy.file.IvyLog;
import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.s6.common.S6Solution;
import edu.brown.cs.s6.common.S6SolutionSet;
import edu.brown.cs.s6.common.S6Transform;

class LanguageController implements LanguageConstants, CoseConstants

{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private S6SolutionSet	 solution_set;
private S6Solution	 for_solution;
private S6TransformType  transform_phase;

private static Map<String,TransformData>	transform_data;
private static List<TransformData>		transform_order;


static {
   transform_data = new HashMap<String,TransformData>();
   Element xml = IvyXml.loadXmlFromFile(S6_TRANSFORM_DATA);
   for (Element txml : IvyXml.children(xml,"TRANSFORM")) {
      try {
	 TransformData td = new TransformData(txml);
	 transform_data.put(td.getName(),td);
       }
      catch (Throwable t) {
	 IvyLog.logE("LANGUAGE","Problem creating transform: " + IvyXml.convertXmlToString(txml),t);
       }
    }
   toposort();
}



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

LanguageController(S6SolutionSet solset,S6Solution sol,S6TransformType phase)
{
   solution_set = solset;
   for_solution = sol;
   transform_phase = phase;
}



/********************************************************************************/
/*										*/
/*	Working methods 							*/
/*										*/
/********************************************************************************/

boolean applyTransforms()
{
   boolean chng = false;

   for (TransformData td : transform_order) {
      if (!shouldApply(td)) continue;
      S6Transform tr = td.getTransform();
      if (tr == null) continue;
      boolean fnd = tr.applyTransform(solution_set,for_solution);
      if (fnd) {
	 chng = true;
	 if (td.isRemovePrevious()) {
	    for_solution.setFlag(S6SolutionFlag.REMOVE);
	    break;
	  }	
       }
    }

   return chng;
}



private boolean shouldApply(TransformData td)
{
   if (!td.checkUse(solution_set,for_solution,transform_phase)) return false;

   return true; 
}




/********************************************************************************/
/*										*/
/*	Information about a transform						*/
/*										*/
/********************************************************************************/

private static class TransformData {

   private String transform_name;
   private String class_name;
   private EnumSet<S6TransformType> use_types;
   private EnumSet<S6SearchType> search_types;
   private EnumSet<CoseScopeType> scope_types;
   private Set<TransformReference> comes_before;
   private Set<TransformReference> comes_after;
   private EnumSet<S6SolutionFlag> require_flags;
   private boolean multiple_allowed;
   private boolean remove_previous;
   private S6Transform use_transform;
   private boolean can_be_first;

   TransformData(Element xml) {
      transform_name = IvyXml.getAttrString(xml,"NAME");
      class_name = IvyXml.getAttrString(xml,"CLASS");
      use_types = IvyXml.getAttrEnumSet(xml,"PHASE",S6TransformType.class,true);
      search_types = IvyXml.getAttrEnumSet(xml,"SEARCH",S6SearchType.class,true);
      scope_types = IvyXml.getAttrEnumSet(xml,"SCOPE",CoseScopeType.class,true);
      require_flags = IvyXml.getAttrEnumSet(xml,"REQUIRE",S6SolutionFlag.class,false);
      multiple_allowed = IvyXml.getAttrBool(xml,"MULTIPLE",true);
      remove_previous = IvyXml.getAttrBool(xml,"REMOVE");
      comes_before = readSet(xml,"BEFORE");
      comes_after = readSet(xml,"AFTER");
      can_be_first = IvyXml.getAttrBool(xml,"FIRST",true);
      try {
	 Class<?> c = Class.forName(class_name);
	 Constructor<?> cnst = c.getConstructor(String.class);
	 use_transform = (S6Transform) cnst.newInstance(transform_name);
       }
      catch (Exception e) {
	 IvyLog.logE("LANGUAGE","Problem creating transform " + class_name);
	 use_transform = null;
       }
    }

   String getName()			{ return transform_name; }
   boolean isRemovePrevious()		{ return remove_previous; }

   S6Transform getTransform() {
      return use_transform;
    }

   Set<String> getDepends(boolean before) {
      Set<TransformReference> trset = (before ? comes_before : comes_after);
      if (trset == null) return null;
      Set<String> rslt = new HashSet<String>();
      for (TransformReference tr : trset) {
	 String s = tr.getTransform();
	 if (s != null) rslt.add(s);
       }
      return rslt;
    }

   boolean checkUse(S6SolutionSet solset,S6Solution sol,S6TransformType phase) {
      S6SearchType srchtype = solset.getSearchType();
      if (search_types != null &&
	    !search_types.contains(srchtype))
	 return false;

      if (scope_types != null &&
	    !scope_types.contains(solset.getScopeType()))
	 return false;

      if (use_types != null && !use_types.contains(phase)) return false;

      for (S6SolutionFlag sflag : require_flags) {
	 if (!sol.checkFlag(sflag)) return false;
       }

      Set<TransformReference> afterset = null;
      if (comes_after != null) {
	 for (TransformReference tr : comes_after) {
	    if (tr.appliesTo(srchtype)) {
	       if (afterset == null) afterset = new HashSet<TransformReference>();
	       afterset.add(tr);
	     }
	  }
       }

      int ct = 0;
      if (sol.getTransforms() != null) {
	 boolean hasafter = true;
	 if (afterset != null) hasafter = false;
	
	 for (S6Transform.Memo m : sol.getTransforms()) {
	    ++ct;
	    String tid = m.getTransformName();
	    String id = tid;
	    int idx = id.indexOf("@");
	    if (idx >= 0) id = id.substring(0,idx);
	    if (tid.equals(transform_name)) return false;
	    if (!multiple_allowed && id.equals(transform_name)) return false;
	    if (comes_before != null && setContains(id,srchtype,comes_before)) return false;
	    if (afterset != null && setContains(id,srchtype,afterset)) hasafter = true;
	  }
	 if (!hasafter) return false;
       }
      else if (afterset != null) return false;

      if (ct == 0 && !can_be_first) return false;

      return true;
    }

   private Set<TransformReference> readSet(Element xml,String key) {
      Set<TransformReference> rslt = new HashSet<TransformReference>();
      for (Element cxml : IvyXml.children(xml,key)) {
	 TransformReference tr = new TransformReference(cxml);
	 if (tr != null) rslt.add(tr);
       }
      if (rslt.isEmpty()) return null;
      return rslt;
    }

   private boolean setContains(String id,S6SearchType styp,Set<TransformReference> set) {
      if (set == null) return true;
      for (TransformReference tr : set) {
	 if (tr.match(id,styp)) return true;
       }
      return false;
    }

   @Override public String toString() {
      return getName();
    }

}


private static class TransformReference {

   private String transform_name;
   private EnumSet<S6SearchType> search_type;

   TransformReference(Element xml) {
      transform_name = IvyXml.getAttrString(xml,"NAME");
      search_type = null;
      if (IvyXml.getAttrPresent(xml,"SEARCH")) {
	 search_type = IvyXml.getAttrEnumSet(xml,"SEARCH",S6SearchType.class,true);
       }
    }

   String getTransform()		{ return transform_name; }

   boolean appliesTo(S6SearchType styp) {
      if (search_type == null) return true;
      if (search_type.contains(styp)) return true;
      return false;
    }

   boolean match(String id,S6SearchType styp) {
      if (!appliesTo(styp)) return false;
      if (transform_name == null) return true;
      return transform_name.equals(id);
    }

}




/********************************************************************************/
/*										*/
/*	Compute transform order 						*/
/*										*/
/********************************************************************************/

private static void toposort()
{
   Map<TransformData,Set<TransformData>> depends;
   transform_order = new ArrayList<TransformData>();

   depends = new HashMap<TransformData,Set<TransformData>>();
   for (TransformData td : transform_data.values()) {
      depends.put(td,new HashSet<TransformData>());
    }

   // create set of dependencies
   for (TransformData td : transform_data.values()) {
      Set<String> before = td.getDepends(true);
      if (before != null) {
	 for (String s : before) {
	    TransformData btd = transform_data.get(s);
	    if (btd == null) {
	       IvyLog.logE("LANGUAGE","TRANSFORM " + s + " not found");
	     }
	    else depends.get(btd).add(td);
	  }
       }
      Set<String> after = td.getDepends(false);
      if (after != null) {
	 for (String s : after) {
	    TransformData atd = transform_data.get(s);
	    if (atd == null) {
	       IvyLog.logE("LANGUAGE","TRANSFORM " + s + " not found");
	     }
	    else depends.get(td).add(atd);
	  }
       }
    }

   while (depends.size() > 0) {
      Set<TransformData> delrem = new HashSet<TransformData>();
      Set<TransformData> delother = new HashSet<TransformData>();
      TransformData any = null;
      for (Map.Entry<TransformData,Set<TransformData>> ent : depends.entrySet()) {
	 if (ent.getValue().isEmpty()) {
	    TransformData td = ent.getKey();
	    if (td.isRemovePrevious()) delrem.add(td);
	    else delother.add(td);
	  }
	 else if (any == null) any = ent.getKey();
       }
      if (!delrem.isEmpty()) {
	 for (TransformData td : delrem) {
	    transform_order.add(td);
	    depends.remove(td);
	  }
       }
      else if (!delother.isEmpty()) {
	 for (TransformData td : delother) {
	    transform_order.add(td);
	    depends.remove(td);
	  }
	 delrem = delother;
       }
      else {
	 IvyLog.logE("LANGUAGE","Transformation TOPO SORT FAILED");
	 delrem.add(any);
	 transform_order.add(any);
       }
      for (Map.Entry<TransformData,Set<TransformData>> ent : depends.entrySet()) {
	 Set<TransformData> std = ent.getValue();
	 std.removeAll(delrem);
       }
    }
}

}	// end of class LanguageController




/* end of LanguageController.java */

