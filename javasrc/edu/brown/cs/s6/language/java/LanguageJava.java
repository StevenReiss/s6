/********************************************************************************/
/*										*/
/*		LanguageJava.java						*/
/*										*/
/*	Basic implementation of java for S6					*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/language/java/LanguageJava.java,v 1.20 2016/07/18 23:05:26 spr Exp $ */


/*********************************************************************************
 *
 * $Log: LanguageJava.java,v $
 * Revision 1.20  2016/07/18 23:05:26  spr
 * Update transforms for applications and UI.
 *
 * Revision 1.19  2015/12/23 15:45:09  spr
 * Minor fixes.
 *
 * Revision 1.18  2015/09/23 17:54:52  spr
 * Version to handle andriod UI applications.
 *
 * Revision 1.17  2015/02/19 23:33:18  spr
 * Parallelize setting up new solutions for classes.
 *
 * Revision 1.16  2015/02/14 19:40:18  spr
 * Add test case generation.
 *
 * Revision 1.15  2014/08/29 15:16:08  spr
 * Updates for suise, testcases.
 *
 * Revision 1.14  2014/02/26 14:06:43  spr
 * Add transformations for user interfaces.
 *
 * Revision 1.13  2013/09/13 20:33:04  spr
 * Add calls for UI search.
 *
 * Revision 1.12  2013-05-09 12:26:20  spr
 * Minor changes to start ui fixups.
 *
 * Revision 1.11  2012-08-13 16:51:51  spr
 * Add new transforms, clean up code, bug fixes.
 *
 * Revision 1.10  2012-07-20 22:15:20  spr
 * New transforms and resolution for UI search
 *
 * Revision 1.9  2012-06-20 12:21:33  spr
 * Initial fixes for UI search
 *
 * Revision 1.8  2012-06-11 14:07:49  spr
 * add framework search; fix bugs
 *
 * Revision 1.7  2009-09-18 01:41:35  spr
 * Handle user testing.
 *
 * Revision 1.6  2009-05-12 22:28:48  spr
 * Fix ups to make user context work.
 *
 * Revision 1.5  2008-11-12 13:52:14  spr
 * Performance and bug updates.
 *
 * Revision 1.4  2008-08-28 00:32:56  spr
 * Next version of S6.	Lots of bug fixes, some new functionality.
 *
 * Revision 1.3  2008-07-17 13:46:45  spr
 * Bug fixes and speed ups.
 *
 * Revision 1.2  2008-06-12 17:47:52  spr
 * Next version of S6.
 *
 * Revision 1.1.1.1  2008-06-03 12:59:22  spr
 * Initial version of S6
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.language.java;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;

import edu.brown.cs.cose.cosecommon.CoseResult;
import edu.brown.cs.cose.cosecommon.CoseSource;
import edu.brown.cs.ivy.jcomp.JcompControl;
import edu.brown.cs.ivy.jcomp.JcompExtendedSource;
import edu.brown.cs.ivy.jcomp.JcompProject;
import edu.brown.cs.ivy.jcomp.JcompSource;
import edu.brown.cs.ivy.jcomp.JcompSymbol;
import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.ivy.jcomp.JcompTyper;
import edu.brown.cs.ivy.xml.IvyXmlWriter;
import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Exception;
import edu.brown.cs.s6.common.S6FileLocation;
import edu.brown.cs.s6.common.S6Fragment;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6Solution;
import edu.brown.cs.s6.common.S6SolutionSet;
import edu.brown.cs.s6.language.LanguageBase;


public class LanguageJava extends LanguageBase implements S6Constants, JavaConstants {



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private JcompControl	jcomp_main;
private Map<S6Request.Context,JavaContext> context_map;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public LanguageJava()
{
   jcomp_main = new JcompControl();

   List<String> jars = new ArrayList<String>();
   jars.add(S6_RUNNER_JAR);
   jcomp_main.addBaseJars(jars);

   context_map = new HashMap<S6Request.Context,JavaContext>();
}



/********************************************************************************/
/*										*/
/*	Fragment methods							*/
/*										*/
/********************************************************************************/

public S6Fragment createFileFragment(String text,CoseSource src,S6Request.Search sreq)
{
   return FragmentJava.createFileFragment(this,text,src,sreq);
}


public S6Fragment createCoseFragment(CoseResult cr,S6Request.Search sreq)
{
   return JavaCoseFragment.createCoseFragment(cr,this,sreq);
}



public S6Fragment createPackageFragment(S6Request.Search sr)
{
   return FragmentJava.createPackageFragment(this,sr);
}


public Set<String> getRelatedProjects(S6Fragment sfj)
{
   JavaFragment fj = (JavaFragment) sfj;
   Set<String> rslt = new HashSet<String>();
   CompilationUnit cu = (CompilationUnit) fj.getAstNode();
   PackageDeclaration pd = cu.getPackage();
   if (pd == null) return rslt;
   String nm = pd.getName().getFullyQualifiedName();
   if (fj.getPackages() != null) {
      for (String s : fj.getPackages()) {
	 if (s.length() < nm.length())
	    nm = s;
       }
    }

   for (Object o : cu.imports()) {
      ImportDeclaration id = (ImportDeclaration) o;
      if (id.isStatic()) continue;
      String inm = id.getName().getFullyQualifiedName();
      if (!id.isOnDemand()) {
	 int idx = inm.lastIndexOf(".");
	 if (idx < 0) continue;
	 inm = inm.substring(0,idx);
       }
      if (inm.equals(nm)) continue;
      if (inm.startsWith(nm)) rslt.add(inm);
      else if (nm.startsWith(inm)) rslt.add(inm);
      else {
	 int idx = -1;
	 for (int i = 0; i < 3; ++i) {
	    idx = nm.indexOf(".",idx+1);
	    if (idx < 0) break;
	  }
	 if (idx >= 0 && idx < inm.length() &&
		nm.substring(0,idx).equals(inm.substring(0,idx))) {
	    rslt.add(inm);
	  }
       }
    }

   System.err.println("S6: Related projects for " + nm + ": ");
   for (String s : rslt) System.err.println("S6:\t" + s);

   return rslt;
}



public Set<String> getUsedProjects(S6Fragment sfj)
{
   FragmentJava fj = (FragmentJava) sfj;
   Set<String> rslt = new HashSet<String>();
   for (S6Fragment ffrag : fj.getFileFragments()) {
      FragmentJava fj1 = (FragmentJava) ffrag;
      CompilationUnit cu = (CompilationUnit) fj1.getAstNode();
      PackageDeclaration pd = cu.getPackage();
      if (pd == null) continue;
      String nm = pd.getName().getFullyQualifiedName();
      rslt.add(nm);
    }

   return rslt;
}


/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

public void setProject(String project)
{
   // possibly handle eclipse projects here
}


public void setPath(String path)
{
}



JcompTyper createTyper(S6Request sr)
{
   JavaContext ctx = getContext(sr);
   List<String> jars = new ArrayList<String>();
   if (sr instanceof S6Request.Search) {
      S6Request.Search sreq = (S6Request.Search) sr;
      if (sreq.getSearchType() == S6SearchType.TESTCASES) {
	 jars.add(JUNIT_CLASSPATH);
       }
    }
   if (ctx != null && ctx.getJarFileName() != null) jars.add(ctx.getJarFileName());

   // return new JcompTyper(base_path,user_path);
   // return new JcompTyper(base_path,user_path);

   return jcomp_main.getTyper(jars,sr.useAndroid());
}



JavaContext getContext(S6Request sreq)
{
   if (sreq == null) return null;

   S6Request.Context ctx = sreq.getUserContext();
   if (ctx.getContextFile() == null) return null;

   JavaContext sctx = context_map.get(ctx);
   if (sctx != null) return sctx;

   sctx = new JavaContext(ctx.getContextFile());

   context_map.put(ctx,sctx);

   return sctx;
}



/********************************************************************************/
/*										*/
/*	Type and name resolution methods					*/
/*										*/
/********************************************************************************/

void resolveFragment(JavaFragment frag)
{
   List<JcompSource> srcs = new ArrayList<JcompSource>();
   Iterable<S6Fragment> frags = frag.getFileFragments();
   if (frag.getAstNode() == null && frags != null) {
       for (S6Fragment s6f : frags) {
	  FragmentSource src = new FragmentSource(s6f);
	  srcs.add(src);
	}
    }
   else {
      FragmentSource src = new FragmentSource(frag);
      srcs.add(src);
    }

   S6Request.Search sreq = frag.getSearchRequest();
   JavaContext ctx = getContext(sreq);
   List<String> jars = new ArrayList<String>();
   switch (sreq.getSearchType()) {
      case TESTCASES :
	 jars.add(JUNIT_CLASSPATH);
	 break;
      case ANDROIDUI :
	 jars.add(S6_RUNNER_JAR);
	 break;
      default :
	 break;
    }
   if (ctx != null && ctx.getJarFileName() != null) jars.add(ctx.getJarFileName());

   JcompProject proj = jcomp_main.getProject(jars,srcs,sreq.useAndroid());
   try {
      ASTNode node = frag.getAstNode();
      if (node == null) return;
      ASTNode root = node.getRoot();
      synchronized (root) {
	 proj.resolve();
       }
      jcomp_main.freeProject(proj);
    }
   catch (Throwable t) {
      ASTNode node = frag.getAstNode();
      ASTNode root = (node == null ? null : node.getRoot());
      System.err.println("S6: PROBLEM with resolve: " + System.identityHashCode(node) +
			    " " + System.identityHashCode(root) + " " +
			    Thread.currentThread() + " " + t);
      t.printStackTrace();
      System.err.println("FRAGMENT: " + frag);
      if (proj != null) jcomp_main.freeProject(proj);
    }
}


private static class FragmentSource implements JcompExtendedSource {

   private JavaFragment for_fragment;

   FragmentSource(S6Fragment fj) {
      for_fragment = (JavaFragment) fj;
    }

   @Override public String getFileContents() {
      return for_fragment.getOriginalText();
    }

   @Override public String getFileName() {
      return "*S6*";
    }

   @Override public ASTNode getAstRootNode() {
      ASTNode nd = for_fragment.getAstNode();
      return nd.getRoot();
    }

}	// end of inner class FragmentSource








/********************************************************************************/
/*										*/
/*	File resolution methods 						*/
/*										*/
/********************************************************************************/

public void resolveAll(Iterable<S6Fragment> files)
{
   List<JcompSource> srcs = new ArrayList<JcompSource>();
   for (S6Fragment s6f : files) {
      FragmentSource src = new FragmentSource(s6f);
      srcs.add(src);
    }

   JcompProject proj = jcomp_main.getProject(srcs,false);

   try {
      proj.resolve();
      jcomp_main.freeProject(proj);
    }
   catch (Throwable t) {
      System.err.println("S6: PROBLEM with resolve: " + t);
      t.printStackTrace();
    }
}




/********************************************************************************/
/*										*/
/*	Definition/Reference location methods					*/
/*										*/
/********************************************************************************/

public Collection<S6FileLocation> findAll(Iterable<S6Fragment> files,S6Fragment f,
					     int startoffset,int endoffset,boolean defs)
{
   JavaFragment fj = (JavaFragment) f;
   ASTNode root = fj.getAstNode();
   ASTNode n = findActualNode(root,startoffset,endoffset);

   if (n == null) return null;

   JcompSymbol js = JavaAst.getReference(n);
   if (js == null && n.getParent() != null) js = JavaAst.getReference(n.getParent());
   if (js == null) return null;

   LocationFinder lf = new LocationFinder(js,defs);
   for (S6Fragment sf : files) {
      JavaFragment sfj = (JavaFragment) sf;
      ASTNode sn = sfj.getAstNode();
      sn.accept(lf);
    }

   return lf.getLocations();
}



private ASTNode findActualNode(ASTNode n,int start,int end)
{
   ASTNode rslt = null;

   int spos = n.getStartPosition();
   int nlen = n.getLength();
   if (start > spos + nlen) return null;
   if (end < spos) return null;

   for (Iterator<?> it = n.structuralPropertiesForType().iterator(); it.hasNext(); ) {
      StructuralPropertyDescriptor spd = (StructuralPropertyDescriptor) it.next();
      if (spd.isSimpleProperty()) ;
      else if (spd.isChildProperty()) {
	 ASTNode cn = (ASTNode) n.getStructuralProperty(spd);
	 if (cn != null) rslt = findActualNode(cn,start,end);
       }
      else {
	 List<?> lcn = (List<?>) n.getStructuralProperty(spd);
	 for (Iterator<?> it1 = lcn.iterator(); it1.hasNext(); ) {
	    ASTNode cn = (ASTNode) it1.next();
	    rslt = findActualNode(cn,start,end);
	    if (rslt != null) break;
	  }
       }
      if (rslt != null) break;
    }

   if (rslt == null) rslt = n;

   return rslt;
}



private static class LocationFinder extends ASTVisitor implements S6Constants {

   List<S6FileLocation> found_locs;
   JcompSymbol for_symbol;
   boolean defs_only;

   LocationFinder(JcompSymbol js,boolean defs) {
      found_locs = new ArrayList<S6FileLocation>();
      for_symbol = js;
      defs_only = defs;
    }

   List<S6FileLocation> getLocations()		{ return found_locs; }

   public void preVisit(ASTNode n) {
      JcompSymbol js = JavaAst.getDefinition(n);
      if (js == for_symbol) addLocation(n);
      else if (!defs_only) {
	 js = JavaAst.getReference(n);
	 if (js == for_symbol) addLocation(n);
       }
    }

   private void addLocation(ASTNode n) {
      found_locs.add(new Location(n));
    }

}	// end of subclass LocationFinder



private static class Location implements S6FileLocation {

   private ASTNode ast_node;

   Location(ASTNode n) {
      ast_node = n;
    }

   public String getFileName() {
      return JavaAst.getS6Source(ast_node.getRoot()).getName();
    }

   public int getStartOffset() {
      return ast_node.getStartPosition();
    }

   public int getEndOffset() {
      return ast_node.getStartPosition() + ast_node.getLength() - 1;
    }

}	// end of subclass Location




/********************************************************************************/
/*										*/
/*	Testing methods 							*/
/*										*/
/********************************************************************************/

JavaTester createTester(S6Request.Search r,JavaFragment frag,CoseSource src)
{
   JavaContext ctx = getContext(r);

   return new JavaTester(frag,r,ctx,src);
}




/********************************************************************************/
/*										*/
/*	Input checking and reformatting methods 				*/
/*										*/
/********************************************************************************/

public @Override void checkInput(S6Request.Check creq,IvyXmlWriter xw) throws S6Exception
{
   JavaChecker jc = new JavaChecker(this,creq);

   switch (creq.getCheckType()) {
      case NONE :
      default :
	 throw new S6Exception("Illegal check type " + creq.getCheckType());
      case METHOD :
	 jc.checkMethodSignature(creq,xw);
	 break;
      case CALLTEST :
	 jc.checkCallTest(creq,xw);
	 break;
      case TESTS :
	 jc.checkTests(creq,xw);
	 break;
      case METHODTESTS :
	 jc.checkMethodTests(creq,xw);
	 break;
      case CLASSTESTS :
	 jc.checkClassTests(creq,xw);
	 break;
      case SECURITY :
	 jc.checkSecurity(creq,xw);
	 break;
      case CONTRACTS :
	 jc.checkContracts(creq,xw);
	 break;
      case CONTEXT :
	 jc.checkContext(creq,xw);
	 break;
      case CLASS :
	 jc.checkClassNames(creq,false,xw);
	 break;
      case CLASSLIST :
	 jc.checkClassNames(creq,true,xw);
	 break;
    }
}




/********************************************************************************/
/*										*/
/*	Formatting methods							*/
/*										*/
/********************************************************************************/

public @Override void formatCode(S6Request.Format freq,IvyXmlWriter xw) throws S6Exception
{
   JavaFormatter fmt = new JavaFormatter(this,freq.getSearchType(),freq.getFormatType());

   for (S6Request.FormatItem itm : freq.getItems()) {
      fmt.addFile(itm.getId(),itm.getText());
    }

   fmt.reformatAll();

   for (S6Request.FormatItem itm : freq.getItems()) {
      String r = fmt.getText(itm.getId());
      if (r != null) {
	 xw.begin("ITEM");
	 xw.field("ID",itm.getId());
	 xw.cdata(r);
	 xw.end("ITEM");
       }
    }

   fmt.cleanup();
}



public void formatCode(S6Request.Search req,S6SolutionSet ss) throws S6Exception
{
   if (req.getFormatType() == S6FormatType.NONE) return;

   JavaFormatter fmt = new JavaFormatter(this,req.getSearchType(),req.getFormatType());

   for (S6Solution sol : ss) {
      fmt.addFile(sol.getId(),sol.getFragment().getText());
    }

   fmt.reformatAll();

   for (S6Solution sol : ss) {
      String r = fmt.getText(sol.getId());
      if (r != null) {
	 sol.setFormattedText(r);
       }
    }

   fmt.cleanup();
}




/********************************************************************************/
/*										*/
/*	Cleanup methods 							*/
/*										*/
/********************************************************************************/

public @Override void finish(S6Request.Search rq)
{
   if (rq == null) return;

   S6Request.Context ctx = rq.getUserContext();
   if (ctx == null || ctx.getContextFile() == null) return;

   JavaContext sctx = context_map.remove(ctx);
   if (sctx == null) return;

   sctx.close(rq);
}




/********************************************************************************/
/*										*/
/*	Methods to dump symbols 						*/
/*										*/
/********************************************************************************/

public @Override void listDefinitions(S6Fragment sf,String file,IvyXmlWriter xw)
{
   JavaFragment fj = (JavaFragment) sf;
   DefFinder df = new DefFinder(xw,file);
   ASTNode root = fj.getAstNode();
   root.accept(df);
}




private static class DefFinder extends ASTVisitor implements S6Constants {

   private IvyXmlWriter xml_writer;
   private String file_name;
   private Set<JcompSymbol> syms_done;

   DefFinder(IvyXmlWriter xw,String file) {
      xml_writer = xw;
      file_name = file;
      syms_done = new HashSet<JcompSymbol>();
      syms_done = new HashSet<JcompSymbol>();
    }

   public void preVisit(ASTNode n) {
      JcompSymbol js = JavaAst.getDefinition(n);
      if (js != null && !syms_done.contains(js)) {
	 syms_done.add(js);
	 outputSymbol(js);
       }
    }

   private void outputSymbol(JcompSymbol js) {
      JcompType jt = js.getType();

      String what = null;
      if (js.isTypeSymbol()) {
	 if (jt.isInterfaceType()) what = "Interface";
	 else if (jt.isEnumType()) what = "Enum";
	 else what = "Class";
       }
      else if (js.isMethodSymbol()) {
	 String nm = js.getName();
	 if (nm.equals("<init>")) what = "Constructor";
	 else if (nm.equals("<clinit>")) what = "StaticInitializer";
	 else what = "Function";
       }
      else if (js.isEnumSymbol()) what = "EnumConstant";
      else if (js.isFieldSymbol()) what = "Field";
      else return;

      xml_writer.begin("Item");
      xml_writer.field("Type",what);
      xml_writer.field("Name",js.getFullName());

      ASTNode dn = js.getDefinitionNode();
      if (dn != null) {
	 xml_writer.field("StartOffset",dn.getStartPosition());
	 xml_writer.field("EndOffset",dn.getStartPosition() + dn.getLength());
       }
      if (js.isBinarySymbol()) xml_writer.field("Source","JavaSystem");
      else xml_writer.field("Source","UserSource");
      xml_writer.field("File",file_name);
      xml_writer.end("Item");
    }
}




}	// end of class LanguageJava




/* end of LanguageJava.java */

