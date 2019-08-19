/********************************************************************************/
/*										*/
/*		KeySearchMaster.java						*/
/*										*/
/*	General implementation of keyword search of repositories		*/
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



package edu.brown.cs.s6.keysearch;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.regex.*;

import org.w3c.dom.Element;

import edu.brown.cs.cose.cosecommon.CoseConstants;
import edu.brown.cs.cose.cosecommon.CoseResource;
import edu.brown.cs.cose.cosecommon.CoseSource;
import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.s6.common.S6Fragment;
import edu.brown.cs.s6.common.S6KeySearch;
import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.common.S6SolutionSet;

public class KeySearchMaster implements KeySearchConstants, S6KeySearch, S6Constants, CoseConstants 
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private List<KeySearchRepo>	search_repos;
private KeySearchQueue		work_queue;
private S6SolutionSet		solution_set;
private Map<CoseSource,Map<String,Set<String>>> package_items;

private static final Set<String> RESOURCE_EXTENSIONS;

static {
   RESOURCE_EXTENSIONS = new HashSet<String>();
   RESOURCE_EXTENSIONS.add(".png");
   RESOURCE_EXTENSIONS.add(".jpg");
   RESOURCE_EXTENSIONS.add(".jpeg");
   RESOURCE_EXTENSIONS.add(".gif");
   RESOURCE_EXTENSIONS.add(".xml");
   RESOURCE_EXTENSIONS.add(".mp3");
   RESOURCE_EXTENSIONS.add(".wav");
   RESOURCE_EXTENSIONS.add(".ogg");
   RESOURCE_EXTENSIONS.add(".bmp");
   RESOURCE_EXTENSIONS.add(".db");
   RESOURCE_EXTENSIONS.add(".lnk");
}



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public KeySearchMaster(S6SolutionSet sol)
{
   solution_set = sol;
   S6Request.Search sr = solution_set.getRequest();
   search_repos = new ArrayList<KeySearchRepo>();
   for (S6Location loc : sr.getLocations()) {
      KeySearchRepo next = null;
      switch (loc) {
	 case KODERS :
	 case OHLOH :
	 case OPENHUB :
	    next = new KeySearchRepoOpenHub(sr);
	    break;
	 case GITHUB :
	    next = new KeySearchRepoGithub(sr);
	    break;
	 case LOCAL :
	    next = new KeySearchRepoLabrador(sr);
	    break;
	 case HUNTER :
	    next = new KeySearchRepoHunter(sr);
	    break;
	 case CODEEX :
	    next = new KeySearchRepoCodeExchange(sr);
	    break;
	 case GITZIP :
	    next = new KeySearchRepoGitZip(sr);
	    break;
         case SEARCHCODE :
            next = new KeySearchRepoSearchCode(sr);
            break;
	 default :
	    System.err.println("Search engine " + loc + " no longer supported");
	    break;
       }
      if (next != null) search_repos.add(next);
    }
   if (search_repos.size() == 0) {
      // search_repos.add(new KeySearchRepoGithub(sr));
    }
   work_queue = new KeySearchQueue();
   package_items = new HashMap<>();
}



/********************************************************************************/
/*										*/
/*	Get solutions for a search request					*/
/*										*/
/********************************************************************************/

@Override public void getInitialSolutions(S6SolutionSet sol)
{
   if (solution_set == null) solution_set = sol;

   S6Request.Search srch = solution_set.getRequest();

   Iterable<String> spsrc = srch.getSpecificSources();
   if (spsrc != null) {
      for (String src : spsrc) {
	 for (KeySearchRepo repo : search_repos) {
	    if (getSpecificSolutionFromRepo(repo,src)) break;
	  }
       }
    }
   else {
      int ct = search_repos.size();
      int lclrslts = (TARGET_RESULTS + ct - 1)/ct;
      int sz = srch.getKeywordSets().size();
      if (sz > 1) lclrslts = (lclrslts + sz -1)/sz;

      for (KeySearchRepo repo : search_repos) {
	 getSolutionsFromRepo(repo,lclrslts);
       }
    }

   solution_set.getEngine().waitForAll(work_queue);
}



private void getSolutionsFromRepo(KeySearchRepo repo,int ct)
{
   int rpp = repo.getResultsPerPage();
   int npages = (ct + rpp -1)/rpp;
   ScanSearchResults ssr = new ScanSearchResults(repo,0,npages);
   addTask(ssr);
}


private boolean getSpecificSolutionFromRepo(KeySearchRepo repo,String src)
{
   URI uri = repo.getURIFromSourceString(src);
   if (uri != null) {
      buildFragmentsForURI(repo,uri,0);
      return true;
    }

   return false;
}



/********************************************************************************/
/*										*/
/*	Class to get and scan a results page					*/
/*										*/
/********************************************************************************/

private class ScanSearchResults implements Runnable {

   private KeySearchRepo for_repo;
   private int page_number;
   private int max_page;

   ScanSearchResults(KeySearchRepo repo,int page,int maxpage) {
      for_repo = repo;
      page_number = page;
      max_page = maxpage;
    }

   @Override public void run() {
      S6Request.Search req = solution_set.getRequest();
      int idx = for_repo.getResultsPerPage() * page_number;
      boolean cont = true;
      for (S6Request.KeywordSet kws : req.getKeywordSets()) {
         URI uri = for_repo.getURIForSearch(kws.getWords(),kws.getLanguage(),null,page_number);
         String txt = null;
         if (uri == null)
            txt = for_repo.getResultPage(kws.getWords(),kws.getLanguage(),null,page_number);
         else
            txt = for_repo.getResultPage(uri);
         if (txt == null) continue;
   
         if (page_number < max_page && for_repo.hasMoreSearchPages(uri,txt,page_number) && cont) {
            ScanSearchResults ssr1 = new ScanSearchResults(for_repo,page_number+1,max_page);
            addTask(ssr1);
            cont = false;
          }
         List<URI> uris = for_repo.getSearchPageResults(uri,txt);
         for (URI u : uris) {
            buildFragmentsForURI(for_repo,u,idx);
            ++idx;
          }
       }
    }

}	// end of inner class ScanSearchResults




/********************************************************************************/
/*										*/
/*	Fragment builder task							*/
/*										*/
/********************************************************************************/

private void buildFragmentsForURI(KeySearchRepo repo,URI uri,int idx)
{
   FragmentBuilder fb = new FragmentBuilder(repo,uri,idx);
   addTask(fb);
}



private class FragmentBuilder implements Runnable {

   private KeySearchRepo for_repo;
   private URI initial_uri;
   private int result_index;

   FragmentBuilder(KeySearchRepo repo,URI uri,int idx) {
      for_repo = repo;
      initial_uri = uri;
      result_index = idx;
    }

   @Override public void run() {
      String txt = for_repo.getSourcePage(initial_uri);
      if (txt == null) return;
      CoseSource src = for_repo.createSource(initial_uri,txt,result_index);
      if (src == null) return;
      S6Fragment pfrag = null;
      switch (solution_set.getSearchType()) {
         case METHOD :
         case CLASS :
         case FULLCLASS :
         case TESTCASES :
            addSolutions(txt,src);
            break;
         case PACKAGE :
         case UIFRAMEWORK :
         case APPLICATION :
            pfrag = solution_set.getEngine().createPackageFragment(solution_set.getRequest());
            addPackageSolutions(for_repo,pfrag,src,txt);
            break;
         case ANDROIDUI :
            addInitialAndroidSolutions(for_repo,src,txt);
            break;
       }
    }


}	// end of inner class FragmentBuilder




/********************************************************************************/
/*										*/
/*	Add solutions for a given result file (non-package)			*/
/*										*/
/********************************************************************************/

private void addSolutions(String code,CoseSource source)
{
   if (source != null && !solution_set.useSource(source)) return;

   S6Fragment ff = solution_set.getEngine().createFileFragment(code,source,
	 solution_set.getRequest());
   if (ff == null) return;

   // ff.resolveFragment();

   for (S6Fragment cf : ff.getFragments(solution_set.getSearchType())) {
      solution_set.addInitialSolution(cf,source);
    }
}



/********************************************************************************/
/*										*/
/*	Package fragment methods						*/
/*										*/
/********************************************************************************/

void addPackageSolutions(KeySearchRepo repo,S6Fragment pfrag,CoseSource source,String code)
{
   if (code == null) return;

   if (solution_set.getRequest().getScopeType() == CoseScopeType.FILE) {
      addPackageSolution(code,source,source,pfrag);
      solution_set.addInitialSolution(pfrag,source);
      return;
    }

   String pkg = findPackageName(code);
   if (pkg == null || pkg.equals("")) return;

   addPackageSolution(code,source,source,pfrag);

   KeySearchQueue subwaits = new KeySearchQueue();

   // Get the package name
   // Ensure package is unique
   synchronized (package_items) {
      Map<String,Set<String>> sitms = package_items.get(source);
      if (sitms == null) {
	 sitms = new HashMap<String,Set<String>>();
	 package_items.put(source,sitms);
       }
      Set<String> itms = sitms.get(pkg);
      if (itms != null) {
         return;
       }
      itms = new HashSet<String>();
      itms.add(source.getName());
      sitms.put(pkg,itms);
    }

   // now expand to include the rest of the package
   ScanPackageSearchResults spsr = new ScanPackageSearchResults(repo,pkg,source,pfrag,subwaits);
   addSubtask(spsr,subwaits);

   FinishPackageTask fpt = new FinishPackageTask(repo,pfrag,source,subwaits);
   addTask(fpt);
}




private void addPackageSolution(String code,CoseSource source,CoseSource lclsrc,S6Fragment pfrag)
{
   String pkg = findPackageName(code);
   synchronized (package_items) {
      Map<String,Set<String>> sitms = package_items.get(source);
      if (sitms == null) {
	 sitms = new HashMap<String,Set<String>>();
	 package_items.put(source,sitms);
       }
      Set<String> items = sitms.get(pkg);
      if (items != null) {
	 synchronized (items) {
	    if (!items.add(lclsrc.getName())) return;
	  }
       }
    }

   S6Fragment ff = solution_set.getEngine().createFileFragment(code,source,
	 solution_set.getRequest());
   if (ff == null) return;
   // ff.resolveFragment();

   pfrag.addInnerFragment(ff);
}




/********************************************************************************/
/*										*/
/*	Handle android ui search						*/
/*										*/
/********************************************************************************/

private void addInitialAndroidSolutions(KeySearchRepo repo,CoseSource src,String code)
{
   System.err.println("MANIFEST START: " + src.getDisplayName());
   findAndroidManifest(repo,src);
}


private void addAndroidSolutions(KeySearchRepo repo,S6Fragment pfrag,CoseSource source,String code)
{
   if (source == null || source.getDisplayName() == null) {
      System.err.println("BAD SOURCE " + source);
      return;
    }

   if (source.getDisplayName().endsWith(".xml")) {
      if (!source.getDisplayName().endsWith("AndroidManifest.xml")) return;
      // could edit the manifest here
      code = cleanAndroidManifest(code);
      if (code == null) {
	 System.err.println("NULL CODE");
	 return;
       }
      AndroidResource rsrc = new AndroidResource("AndroidManifest.xml",code.getBytes(CHAR_SET));
      pfrag.addResource(rsrc);
      addManifestClasses(repo,pfrag,source,code);
    }
}



private String cleanAndroidManifest(String code)
{
   if (solution_set.getSearchType() != S6SearchType.ANDROIDUI) return code;

   Element xml = IvyXml.convertStringToXml(code);
   if (xml == null) 
      return code;

   boolean chng = false;
   for (Element appxml : IvyXml.children(xml,"application")) {
      List<Element> rems = new ArrayList<Element>();
      for (Element actxml : IvyXml.children(appxml)) {
	 switch (actxml.getNodeName()) {
	    case "service":
	    case "receiver" :
	    case "provider" :
	       rems.add(actxml);
	       chng = true;
	       break;
	    case "activity_alias" :
	       // check android:targetActivity
	       break;
	    default :
	       break;
	  }
       }
      for (Element e : rems) {
	 appxml.removeChild(e);
       }
    }

   if (chng) {
      code = IvyXml.convertXmlToString(xml);
    }

   return code;
}


private void addManifestClasses(KeySearchRepo repo,S6Fragment pfrag,CoseSource src,String code)
{
   KeySearchQueue subwaits = new KeySearchQueue();

   while (code.startsWith("?")) code = code.substring(1);

   org.w3c.dom.Element xml = IvyXml.convertStringToXml(code);
   if (!IvyXml.isElement(xml,"manifest")) {
      System.err.println("NON_MANIFEST RETURNED: " + src.getDisplayName() + " :: " + code);
      return;
    }
   String pkg = IvyXml.getAttrString(xml,"package");
   synchronized (package_items) {
      Map<String,Set<String>> sitms = package_items.get(src);
      if (sitms == null) {
	 sitms = new HashMap<String,Set<String>>();
	 package_items.put(src,sitms);
       }
      Set<String> items = sitms.get(pkg);
      if (items != null) {
	 System.err.println("DUPLICATE MANIFEST " + src.getDisplayName() + " " + pkg);
	 return;
       }
      sitms.put(pkg,new HashSet<String>());
    }

   System.err.println("WORK ON MANIFEST " + src.getDisplayName());

   for (org.w3c.dom.Element appxml : IvyXml.children(xml,"application")) {
      String icls = IvyXml.getAttrString(appxml,"android:name");
      if (icls == null) icls = IvyXml.getAttrString(appxml,"name");
      if (icls != null) {
	 loadAndroidClass(repo,pfrag,src,subwaits,pkg,icls);
       }
      for (org.w3c.dom.Element actxml : IvyXml.children(appxml)) {
	 switch (actxml.getNodeName()) {
	    case "activity" :
	    case "service" :
	    case "provider" :
	    case "activity-alias":
	       String cls = IvyXml.getAttrString(actxml,"android:name");
	       if (cls == null) cls = IvyXml.getAttrString(actxml,"name");
	       if (cls != null) {
		  loadAndroidClass(repo,pfrag,src,subwaits,pkg,cls);
		}
	       break;
	    default :
	       break;
	  }
       }
    }

   AndroidResourceLoader arl = new AndroidResourceLoader(repo,pfrag,src,0,subwaits);
   addSubtask(arl,subwaits);

   FinishPackageTask fpt = new FinishPackageTask(repo,pfrag,src,subwaits);
   addTask(fpt);
}



private void loadAndroidClass(KeySearchRepo repo,S6Fragment pfrag,CoseSource src,KeySearchQueue subwaits,
      String pkg,String cls)
{
   String lpkg = pkg;
   if (cls.startsWith(".")) cls = cls.substring(1);
   else {
      int idx1 = cls.lastIndexOf(".");
      if (idx1 > 0) {
	 lpkg = cls.substring(0,idx1);
	 cls = cls.substring(idx1+1);
       }
    }
   System.err.println("ADD CLASS " + pkg + " " + cls);
   AndroidClassLoader acl = new AndroidClassLoader(repo,src,lpkg,cls,pfrag,subwaits);
   addSubtask(acl,subwaits);
}



private class AndroidResourceLoader implements Runnable {

   private KeySearchRepo for_repo;
   private S6Fragment package_fragment;
   private CoseSource for_source;
   private KeySearchQueue sub_waits;
   private URI load_uri;

   AndroidResourceLoader(KeySearchRepo repo,S6Fragment pfrag,CoseSource src,int page,KeySearchQueue subwaits) {
      for_repo = repo;
      package_fragment = pfrag;
      for_source = src;
      sub_waits = subwaits;
      load_uri = null;
    }

   AndroidResourceLoader(AndroidResourceLoader arl,URI u) {
      for_repo = arl.for_repo;
      package_fragment = arl.package_fragment;
      for_source = arl.for_source;
      sub_waits = arl.sub_waits;
      load_uri = u;
    }

   @Override public void run() {
      loadByDirectory();
    }

   private void loadByDirectory() {
      if (load_uri == null) load_uri = for_repo.getURIForPath(for_source,"res");
      CoseSource nsrc = for_repo.createSource(load_uri,null,0);
      String sfx = nsrc.getDisplayName();
      int idx1 = sfx.lastIndexOf("/");
      if (idx1 > 0) sfx = sfx.substring(idx1+1);
      int idx = sfx.lastIndexOf(".");
      if (idx > 0) sfx = sfx.substring(idx);
      else sfx = "";
      // might need others here
      if (RESOURCE_EXTENSIONS.contains(sfx)) {
         loadResourceData(nsrc,null);
       }
      else {
         List<URI> uris = for_repo.getDirectoryContentsURIs(load_uri,for_source);
         ByteArrayOutputStream rslts = null;
         if (uris == null) {
            rslts = for_repo.getResultBytes(load_uri);
            if (rslts == null) return;
            String cnts = KeySearchRepo.getString(rslts);
            uris = for_repo.getDirectoryContentsURIs(load_uri,for_source,cnts);
          }
         if (uris == null || uris.size() == 0) {
            if (!loadResourceData(nsrc,rslts))
               System.err.println("NO CONTENTS FOUND FOR " + load_uri);
            return;
          }
         for (URI ux : uris) {
            AndroidResourceLoader arl = new AndroidResourceLoader(this,ux);
            System.err.println("ADD RESOURCE SUBTASK FOR " + ux);
            addSubtask(arl,sub_waits);
          }
       }
   }


   private boolean loadResourceData(CoseSource nsrc,ByteArrayOutputStream ots)
   {
      byte [] cnts = null;
      if (ots != null) cnts = ots.toByteArray();
      if (cnts == null || cnts.length == 0) cnts = for_repo.getBinaryPage(load_uri);
      if (cnts == null || cnts.length == 0) return false;
      String path = nsrc.getDisplayName();
      int idx2 = path.indexOf("/res/");
      if (idx2 > 0) path = path.substring(idx2+1);
      AndroidResource arsrc = new AndroidResource(path,cnts);
      package_fragment.addResource(arsrc);
      System.err.println("RESOURCE: " + nsrc.getName() + " " + nsrc.getProjectId() + " " +
            nsrc.getDisplayName());
      if (path.contains("layout/") && path.endsWith(".xml")) {
         String xmlstr = new String(cnts);
         Element xml = IvyXml.convertStringToXml(xmlstr);
         addResourceReferencedClasses(xml);
       }
      return true;
   }



   private void addResourceReferencedClasses(Element xml)
   {
      String eltnam = xml.getNodeName();
      if (eltnam.contains(".") && !eltnam.startsWith(".")) {
	 loadAndroidClass(for_repo,package_fragment,for_source,sub_waits,null,eltnam);
       }
      for (Element frag : IvyXml.elementsByTag(xml,"fragment")) {
	 String cnm = IvyXml.getAttrString(frag,"android:name");
	 if (cnm == null) cnm = IvyXml.getAttrString(frag,"name");
	 if (cnm == null) cnm = IvyXml.getAttrString(frag,"class");
	 if (cnm != null && cnm.contains(".") && !cnm.startsWith(".") &&
	     !cnm.startsWith("com.google.android.")) {
	    loadAndroidClass(for_repo,package_fragment,for_source,sub_waits,null,cnm);
	  }
       }
   }

}	// end of inner class AndroidResourceLoader


private class AndroidClassLoader implements Runnable {

   private KeySearchRepo for_repo;
   private CoseSource manifest_source;
   private S6Fragment package_fragment;
   private String package_name;
   private String class_name;
   private Set<String> local_items;
   private int page_number;
   private KeySearchQueue sub_waits;

   AndroidClassLoader(KeySearchRepo repo,CoseSource src,String pkg,String cls,S6Fragment pfrag,
         KeySearchQueue subwaits) {
      for_repo = repo;
      manifest_source = src;
      package_fragment = pfrag;
      sub_waits = subwaits;
      Map<String,Set<String>> sitms = package_items.get(src);
      local_items = sitms.get(pkg);
   
      String pfx = pkg;
      if (cls.startsWith(".")) cls = cls.substring(1);
      int idx = cls.lastIndexOf(".");
      if (idx > 0) {
         if (pkg == null) pfx = cls.substring(0,idx);
         else pfx = pkg + "." + cls.substring(0,idx);
         cls = cls.substring(idx+1);
       }
      package_name = pfx;
      class_name = cls;
      page_number = 0;
    }

   @Override public void run() {
      KeySearchClassData kcd = for_repo.getPackageClassResult(manifest_source,
            package_name,class_name,page_number);
   
      if (kcd != null && kcd.getURI() != null) {
         synchronized (local_items) {
            if (!local_items.add(kcd.getSource().getName())) {
               return;
             }      
          }
         try {
            S6Fragment ff = solution_set.getEngine().createFileFragment(kcd.getCode(),
                  kcd.getSource(),solution_set.getRequest());
            if (ff == null) return;
            // ff.resolveFragment();
            package_fragment.addInnerFragment(ff);
          }
         catch (Throwable t) {
            System.err.println("PACKAGE PROBLEM: " + t);
            t.printStackTrace();
          }
       }
      else {
         System.err.println("S6: ANDROID class " + package_name + "." + class_name + " not found on page " + page_number);
         if (kcd != null) {
            ++page_number;
            addSubtask(this,sub_waits);
          }
       }
    }

   private boolean useClass(String basepath,String newpath,String oldpath)
   {
      if (oldpath == null) return true;
      if (newpath.startsWith(basepath) && !oldpath.startsWith(basepath)) return true;
      if (newpath.length() < oldpath.length()) return true;
   
      return false;
   }

}	// end of inner class AndroicClassLoader



private void findAndroidManifest(KeySearchRepo repo,CoseSource src)
{
   List<String> keys = new ArrayList<String>();
   keys.add("manifest");
   keys.add("application");
   keys.add("activity");
   keys.add("android");
   // should wee add the original class name here?
   // keys.add("schemas.android.com/apk/res/android");
   URI uri = repo.getURIForSearch(keys,S6SearchLanguage.XML,src.getProjectId(),0);
   String rslts = repo.getResultPage(uri);
   List<URI> uris = repo.getSearchPageResults(uri,rslts);

   Map<CoseSource,String> usesrc = new HashMap<>();
   for (URI u : uris) {
      String code = repo.getSourcePage(u);
      CoseSource nsrc = repo.createSource(u,code,0);
      checkUseManifest(nsrc,code,usesrc);
    }
   for (Map.Entry<CoseSource,String> ent : usesrc.entrySet()) {
      CoseSource nsrc = ent.getKey();
      String  code = ent.getValue();
      if (code == null) continue;
      S6Fragment nfrag = solution_set.getEngine().createPackageFragment(solution_set.getRequest());
      addAndroidSolutions(repo,nfrag,nsrc,code);
    }
}


private void checkUseManifest(CoseSource nsrc,String ncode,Map<CoseSource,String> osrcs)
{
   if (nsrc == null) return;

   String ns1 = nsrc.getDisplayName();
   if (!ns1.endsWith("AndroidManifest.xml")) return;
   int nidx1 = ns1.lastIndexOf("/");
   String ns2 = ns1.substring(0,nidx1);

   for (Iterator<CoseSource> it = osrcs.keySet().iterator(); it.hasNext(); ) {
      CoseSource osrc = it.next();
      String os1 = osrc.getDisplayName();
      int oidx1 = os1.lastIndexOf("/");
      String os2 = os1.substring(0,oidx1);
      if (os2.startsWith(ns2)) {
	 it.remove();
	 continue;
       }
      if (ns2.startsWith(os2))
	 return;
    }

   osrcs.put(nsrc,ncode);
}



/********************************************************************************/
/*										*/
/*	Load package solutions							*/
/*										*/
/********************************************************************************/

private class ScanPackageSearchResults implements Runnable {

   private KeySearchRepo for_repo;
   private String package_name;
   private String project_id;
   private CoseSource package_source;
   private S6Fragment package_fragment;
   private int page_number;
   private KeySearchQueue package_queue;

   ScanPackageSearchResults(KeySearchRepo repo,String pkg,CoseSource pkgsrc,
         S6Fragment pkgfrag,KeySearchQueue pkgq) {
      for_repo = repo;
      project_id = pkgsrc.getProjectId();
      package_name = pkg;
      package_fragment = pkgfrag;
      package_source = pkgsrc;
      page_number = 0;
      package_queue = pkgq;
    }

   @Override public void run() {
      List<URI> uris = new ArrayList<URI>();
      boolean more = for_repo.getClassesInPackage(package_name,project_id,page_number,uris);
      if (uris == null || uris.size() == 0) return;
      for (URI u : uris) {
         LoadPackageResult lrp = new LoadPackageResult(for_repo,u,package_name,package_fragment,package_source);
         addSubtask(lrp,package_queue);
       }
   
      // if (for_repo.hasMoreResults(uri,rslts))
      if (page_number < 10 && more) {
         ++page_number;
         addSubtask(this,package_queue);
       }
    }

}	// end of inner class ScanPackageSearchResults



private class LoadPackageResult implements Runnable {

   private KeySearchRepo for_repo;
   private URI page_uri;
   private String package_name;
   private S6Fragment package_fragment;
   private CoseSource package_source;

   LoadPackageResult(KeySearchRepo repo,URI uri,String pkg,S6Fragment pkgfrag,
         CoseSource pkgsrc) {
      for_repo = repo;
      page_uri = uri;
      package_name = pkg;
      package_source = pkgsrc;
      package_fragment = pkgfrag;
    }

   @Override public void run() {
      String code = for_repo.getSourcePage(page_uri);
      if (code == null) return;
      CoseSource src = for_repo.createSource(page_uri,code,0);
      String pkg = findPackageName(code);
      if (!pkg.equals(package_name)) return;
      if (solution_set.getScopeType() == CoseScopeType.PACKAGE_UI) {
         String ext = findExtendsName(code);
         boolean isui = false;
         if (ext != null) {
            if (ext.equals("Fragment") || ext.equals("Activity")) {
               isui = true;
             }
          }
         if (!isui) return;
       }
      if (solution_set.getSearchType() == S6SearchType.ANDROIDUI) {
         String cls = findClassName(code);
         if (cls != null && cls.equals("R")) return;
         if (cls != null && cls.equals("BuildConfig")) return;
       }
      addPackageSolution(code,package_source,src,package_fragment);
    }


}	// end of inner class LoadPackageResult




/********************************************************************************/
/*										*/
/*	Finish building a package						*/
/*										*/
/********************************************************************************/

private class FinishPackageTask implements Callable<Boolean> {

   private KeySearchRepo for_repo;
   private KeySearchQueue package_queue;
   private CoseSource package_source;
   private S6Fragment package_fragment;
   private int retry_count;

   FinishPackageTask(KeySearchRepo repo,S6Fragment pkgfrag,CoseSource pkgsrc,KeySearchQueue pkgq) {
      for_repo = repo;
      package_queue = pkgq;
      package_source = pkgsrc;
      package_fragment = pkgfrag;
      retry_count = 0;
    }

   @Override public Boolean call() {
      if (!checkIfDone(package_queue)) {
         // try again later
         addTask(this);
         return false;
       }
      Collection<S6Fragment> ifs = package_fragment.getFileFragments();
      if (ifs == null || ifs.size() == 0) return false;
   
      // here is were we extend the solution to include other packages
      Set<String> pkgs = null;
   
      switch (solution_set.getScopeType()) {
         case SYSTEM :
            pkgs = solution_set.getEngine().getRelatedProjects(package_fragment);
            break;
         case PACKAGE :
         case PACKAGE_UI :
            pkgs = solution_set.getEngine().getUsedProjects(package_fragment);
            break;
         default :
            break;
       }
      
      if (pkgs != null) {
         if (solution_set.getSearchType() != S6SearchType.ANDROIDUI && retry_count > 0) {
            for (Iterator<String> it = pkgs.iterator(); it.hasNext(); ) {
               String p = it.next();
               if (!package_fragment.addPackage(p)) it.remove();
             }
          }
         if (pkgs.size() > 0) {
            for (String pkg : pkgs) {
               ScanPackageSearchResults spsr = new ScanPackageSearchResults(for_repo,pkg,
        	     package_source,package_fragment,package_queue);
               addSubtask(spsr,package_queue);
             }
            ++retry_count;
            addTask(this);
            return false;
          }
       }
   
      solution_set.addInitialSolution(package_fragment,package_source);
      return true;
    }

}	// end of inner class FinishPackageTask




/********************************************************************************/
/*										*/
/*	Task management methods 						*/
/*										*/
/********************************************************************************/

private void addTask(Runnable r)
{
   Future<Boolean> fb = solution_set.getEngine().executeTask(S6TaskType.IO,r);
   synchronized (work_queue) {
      work_queue.add(fb);
    }
}


private void addTask(Callable<Boolean> r)
{
   Future<Boolean> fb = solution_set.getEngine().executeTask(S6TaskType.IO,r);
   synchronized (work_queue) {
      work_queue.add(fb);
    }
}


private void addSubtask(Runnable r,KeySearchQueue queue)
{
   Future<Boolean> fb = solution_set.getEngine().executeTask(S6TaskType.IO,r);
   synchronized (queue) {
      queue.add(fb);
    }
}



private boolean checkIfDone(KeySearchQueue queue)
{
   int waitct = 0;
   synchronized (queue) {
      for (Future<Boolean> fb : queue) {
	 if (!fb.isDone()) ++waitct;
       }
    }
   return waitct == 0;
}



/********************************************************************************/
/*										*/
/*	Common methods for getting source information				*/
/*										*/
/********************************************************************************/

static String findPackageName(String text)
{
   if (text == null) return null;
   String pats = "^\\s*package\\s+([A-Za-z_0-9]+(\\s*\\.\\s*[A-Za-z_0-9]+)*)\\s*\\;";
   Pattern pat = Pattern.compile(pats,Pattern.MULTILINE);
   Matcher mat = pat.matcher(text);
   if (!mat.find()) return "";

   String pkg = mat.group(1);
   StringTokenizer tok = new StringTokenizer(pkg,". \t\n\f");
   StringBuffer buf = new StringBuffer();
   int ctr = 0;
   while (tok.hasMoreTokens()) {
      String elt = tok.nextToken();
      if (ctr++ > 0) buf.append(".");
      buf.append(elt);
    }
   return buf.toString();
}



static String findClassName(String text)
{
   String pats = "\\s*((public|private|abstract|static)\\s+)*class\\s+(\\w+)\\s+((extends\\s)|(implements\\s)|\\{)";
   Pattern pat = Pattern.compile(pats,Pattern.MULTILINE);
   Matcher mat = pat.matcher(text);
   if (!mat.find()) return "";
   String cls = mat.group(3);
   return cls;
}



static String findExtendsName(String text)
{
   String pats = "\\s*((public|private|abstract|static)\\s+)*class\\s+(\\w+)\\s+extends\\s+(\\w+)";
   Pattern pat = Pattern.compile(pats,Pattern.MULTILINE);
   Matcher mat = pat.matcher(text);
   if (!mat.find()) return null;
   String cls = mat.group(4);
   return cls;
}




/********************************************************************************/
/*										*/
/*	Resource for android ui search						*/
/*										*/
/********************************************************************************/

private static class AndroidResource implements CoseResource {

   private String path_name;
   private byte [] file_contents;

   AndroidResource(String path,byte [] cnts) {
      path_name = path;
      file_contents = cnts;
    }

   @Override public byte [] getContents()	{ return file_contents; }
   @Override public String getPathName()	{ return path_name; }

}	// end of inner class AndroidResource




}	// end of class KeySearchMaster




/* end of KeySearchMaster.java */

