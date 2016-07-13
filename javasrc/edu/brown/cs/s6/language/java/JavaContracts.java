/********************************************************************************/
/*										*/
/*		JavaContracts.java						*/
/*										*/
/*	Class to handle contracts						*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/language/java/JavaContracts.java,v 1.6 2015/09/23 17:54:52 spr Exp $ */


/*********************************************************************************
 *
 * $Log: JavaContracts.java,v $
 * Revision 1.6  2015/09/23 17:54:52  spr
 * Version to handle andriod UI applications.
 *
 * Revision 1.5  2013/09/13 20:33:03  spr
 * Add calls for UI search.
 *
 * Revision 1.4  2012-06-11 14:07:49  spr
 * add framework search; fix bugs
 *
 * Revision 1.3  2009-09-18 01:41:35  spr
 * Handle user testing.
 *
 * Revision 1.2  2008-11-12 13:52:14  spr
 * Performance and bug updates.
 *
 * Revision 1.1  2008-07-17 13:46:45  spr
 * Bug fixes and speed ups.
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.language.java;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;
import org.w3c.dom.Element;

import edu.brown.cs.ivy.exec.IvyExec;
import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.ivy.xml.IvyXmlWriter;
import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Contracts;
import edu.brown.cs.s6.common.S6Exception;
import edu.brown.cs.s6.common.S6Request;



class JavaContracts implements S6Constants, JavaConstants {



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private File		contract_root;

private S6Contracts	contract_set;
private FragmentJava	base_fragment;
private Map<ASTNode,ASTNode> contract_nodes;

private static String JML_PAT = "(/\\*\\*\\s*\\*\\s*@jml\\s([^*]|(\\*+[^*/]))*\\*/)";
private static Pattern	jml_pattern = Pattern.compile(JML_PAT);




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

JavaContracts() throws S6Exception
{
   contract_root = new File(System.getProperty("java.io.tmpdir") + File.separator +
			       S6_CONTRACT_DIR);
   base_fragment = null;
   contract_set = null;
   contract_nodes = null;

   if (!contract_root.exists() && !contract_root.mkdir())
      throw new S6Exception("Can't create S6 contract directory: " + contract_root);
}



JavaContracts(S6Contracts cs,FragmentJava jf)
{
   contract_root = null;
   base_fragment = jf;
   contract_set = cs;
   contract_nodes = null;
}



/********************************************************************************/
/*										*/
/*	Checking methods							*/
/*										*/
/********************************************************************************/

void checkContracts(S6Request.Check creq,IvyXmlWriter xw) throws S6Exception
{
   Map<Integer,Integer> errmap = new HashMap<Integer,Integer>();
   Map<Integer,String> errors = new HashMap<Integer,String>();

   File otf = generateCheckFile(creq,errmap);

   try {
      String cmd = JML_CHECK_COMMAND + otf;
      System.err.println("S6: RUN " + cmd);
      IvyExec ex = new IvyExec(cmd,IvyExec.READ_ERROR);
      InputStream ins = ex.getErrorStream();
      BufferedReader br = new BufferedReader(new InputStreamReader(ins));
      for ( ; ; ) {
	 String line = br.readLine();
	 if (line == null) break;
	 if (line.startsWith("File ")) {
	    int idx = line.indexOf(" line ");
	    if (idx >= 0) {
	       idx += 6;
	       int idx1 = line.indexOf(",",idx);
	       if (idx1 >= 0) {
		  int idx2 = line.indexOf("error: ");
		  if (idx2 >= 0) {
		     idx2 += 7;
		     int lno = Integer.parseInt(line.substring(idx,idx1));
		     int cidx = errmap.get(lno);
		     String msg = line.substring(idx2);
		     errors.put(cidx,msg);
		   }
		}
	     }
	  }
       }
      br.close();
      ex.destroy();
      // otf.delete();
    }
   catch (IOException e) {
      throw new S6Exception("Contracts failed " + e);
    }

   xw.begin("CONTRACTS");
   for (Element ct : creq.getUserElements("CONTRACT")) {
      int idx = IvyXml.getAttrInt(ct,"INDEX");
      String emsg = errors.get(idx);
      if (emsg == null) {
	 xw.writeXml(ct);
       }
      else {
	 xw.begin("ERROR");
	 xw.field("INDEX",idx);
	 xw.field("MESSAGE",emsg);
	 xw.end();
       }
    }
   xw.end();
}



/********************************************************************************/
/*										*/
/*	Methods to generate a contract test file				*/
/*										*/
/********************************************************************************/

private File generateCheckFile(S6Request.Check creq,Map<Integer,Integer> errmap)
	throws S6Exception
{
   String fnm = null;
   Random r = new Random();

   File otf = null;
   for (int i = 0; i < 100; ++i) {
      fnm = S6_CONTRACT_PREFIX + r.nextInt(65536);
      otf = new File(contract_root.getPath() + File.separator + fnm + ".java");
      try {
	 if (otf.createNewFile()) break;
       }
      catch (IOException e) { }
    }
   if (otf == null) throw new S6Exception("Problem creating contract file");

   try {
      PrintWriter pw = new PrintWriter(otf);
      int lno = 1;
      pw.println("public abstract class " + fnm + " {");
      ++lno;

      S6Request.Signature sgn = creq.getSignature();
      S6Request.MethodSignature msgn = null;

      Set<String> done = new HashSet<String>();

      for ( ; ; ) {
	 String workon = null;
	 for (Element ct : creq.getUserElements("CONTRACT")) {
	    int idx = IvyXml.getAttrInt(ct,"INDEX");
	    String method = IvyXml.getTextElement(ct,"METHOD");
	    if (workon == null && !done.contains(method)) {
	       done.add(method);
	       msgn = sgn.getMethod(method);
	       if (msgn == null) continue;
	       workon = method;
	       pw.println("/*@");
	       ++lno;
	     }
	    if (method.equals(workon)) {
	       String chk = IvyXml.getTextElement(ct,"CODE");
	       S6ContractType typ = IvyXml.getAttrEnum(ct,"TYPE",S6ContractType.NONE);
	       String what = null;
	       switch (typ) {
		  case REQUIRE :
		     what = "requires";
		     break;
		  case ENSURE :
		     what = "ensures";
		     break;
		  default :
		     continue;
		}
	       chk.replace('\n',' ');
	       errmap.put(lno,idx);
	       pw.println("  @ " + what + " " + chk + ";");
	       ++lno;
	     }
	  }
	 if (workon == null || msgn == null) break;
	 pw.println("  @*/");
	 ++lno;
	 pw.println("abstract " + msgn.getDeclarationText() + ";");
	 ++lno;
       }
      pw.println("}");
      ++lno;
      pw.close();
    }
   catch (IOException e) {
      throw new S6Exception("Problem setting upt contract file: " + e);
    }

   return otf;
}



/********************************************************************************/
/*										*/
/*	Methods to insert contracts into AST for code				*/
/*										*/
/********************************************************************************/

boolean insertContracts()
{
   if (contract_set == null || contract_set.isEmpty()) return false;

   ContractAdder ca = new ContractAdder(base_fragment.getAstNode().getAST());
   base_fragment.getAstNode().accept(ca);
   for (ASTNode hn : base_fragment.getHelpers()) {
      hn.accept(ca);
    }
   if (contract_nodes == null || contract_nodes.isEmpty()) return false;

   return true;
}


void removeContracts()
{
   if (contract_nodes == null) return;

   for (Map.Entry<ASTNode,ASTNode> ent : contract_nodes.entrySet()) {
      ASTNode jdnew = ent.getKey();
      BodyDeclaration bd = (BodyDeclaration) jdnew.getParent();
      ASTNode jdold = ent.getValue();
      bd.setJavadoc((Javadoc) jdold);
    }

   contract_nodes = null;
}




/********************************************************************************/
/*										*/
/*	Methods to fix up jml code fragments					*/
/*										*/
/********************************************************************************/

String fixupJmlCode(String code)
{
   Matcher m = jml_pattern.matcher(code);
   StringBuffer rslt = new StringBuffer();
   int nfnd = 0;

   while (m.find()) {
      ++nfnd;
      String s = m.group(0);
      StringBuffer buf = new StringBuffer();
      boolean start = true;
      buf.append("/*");
      for (int i = 2; i < s.length(); ++i) {
	 char ch = s.charAt(i);
	 if (ch == '@' && s.charAt(i+1) == 'j' && s.charAt(i+2) == 'm' && s.charAt(i+3) == 'l')
	    i += 4;
	 else if (ch == '*' && start) {
	    buf.append("@");
	    if (s.charAt(i+1) == '/') buf.append("*");
	    start = false;
	  }
	 else {
	    if (ch == '\\' || ch == '$') buf.append('\\');
	    buf.append(ch);
	    if (ch == '\n') start = true;
	    else if (start && !Character.isWhitespace(ch)) start = false;
	  }
       }

      m.appendReplacement(rslt,buf.toString());
    }

   if (nfnd == 0) return code;

   m.appendTail(rslt);
   return rslt.toString();
}




/********************************************************************************/
/*										*/
/*	Method to replace arguments in contract text				*/
/*										*/
/********************************************************************************/

private String replaceArguments(S6Contracts.Contract c,MethodDeclaration md)
{
   Map<String,String> argmap = new HashMap<String,String>();
   List<String> arglst = c.getParameterNames();
   int idx = 0;
   for (Iterator<?> it = md.parameters().iterator(); it.hasNext(); ) {
      SingleVariableDeclaration svd = (SingleVariableDeclaration) it.next();
      argmap.put(arglst.get(idx),svd.getName().getIdentifier());
      ++idx;
    }

   if (argmap.size() == 0) return c.getCode();

   StreamTokenizer stok = new StreamTokenizer(new StringReader(c.getCode()));
   stok.resetSyntax();
   stok.quoteChar('"');
   stok.quoteChar('\'');
   stok.slashSlashComments(true);
   stok.slashStarComments(true);
   stok.wordChars('\\','\\');
   stok.wordChars('a','z');
   stok.wordChars('A','Z');
   stok.wordChars('0','9');
   stok.wordChars(128+32,255);
   stok.eolIsSignificant(true);

   StringBuffer buf = new StringBuffer();

   try {
      while (stok.nextToken() != StreamTokenizer.TT_EOF) {
	 switch (stok.ttype) {
	    case StreamTokenizer.TT_EOF :
	       break;
	    case StreamTokenizer.TT_EOL :
	       buf.append("\n");
	       break;
	    case StreamTokenizer.TT_WORD :
	       String id = argmap.get(stok.sval);
	       if (id == null) buf.append(stok.sval);
	       else buf.append(id);
	       break;
	    case '\'' :
	       buf.append("'" + fixString(stok.sval,"'") + "'");
	       break;
	    case '"' :
	       buf.append("\"" + fixString(stok.sval,"\"") + "\"");
	       break;
	    default :
	       buf.append((char) stok.ttype);
	       break;
	  }
       }
    }
   catch (IOException e) {
      System.err.println("S6: Unexpected I/O Error: " + e);
      return c.getCode();
    }

   return buf.toString();
}



private String fixString(String s,String dlm)
{
   s = s.replace("\n","\\n");
   s = s.replace("\t","\\t");
   s = s.replace("\r","\\r");
   s = s.replace("\f","\\f");
   s = s.replace(dlm,"\\"+dlm);

   return s;
}




/********************************************************************************/
/*										*/
/*	Class to handle adding contracts					*/
/*										*/
/********************************************************************************/

private class ContractAdder extends ASTVisitor {

   private AST for_ast;

   ContractAdder(AST ast) {
      for_ast = ast;
    }

   public boolean visit(MethodDeclaration md) {
      String nm = md.getName().getIdentifier();
      Javadoc jdoc = findContracts(nm,md);
      if (jdoc != null) {
	 Javadoc jdold = md.getJavadoc();
	 md.setJavadoc(jdoc);
	 if (contract_nodes == null) contract_nodes = new HashMap<ASTNode,ASTNode>();
	 contract_nodes.put(jdoc,jdold);
       }
      return false;
    }

   @SuppressWarnings("unchecked")
   private Javadoc findContracts(String nm,MethodDeclaration md) {
      Javadoc jd = null;
      TagElement jmltag = null;

      for (S6Contracts.Contract c : contract_set.getContracts()) {
	 if (nm.equals(c.getMethod())) {
	    if (jd == null) {
	       jd = for_ast.newJavadoc();
	       List<TagElement> tags = jd.tags();
	       jmltag = for_ast.newTagElement();
	       jmltag.setTagName("@jml");
	       tags.add(jmltag);
	     }
	    String chk = replaceArguments(c,md);
	    S6ContractType typ = c.getType();
	    String what = null;
	    switch (typ) {
	       case REQUIRE :
		  what = "requires";
		  break;
	       case ENSURE :
		  what = "ensures";
		  break;
	       default :
		  continue;
	     }
	    TextElement te = for_ast.newTextElement();
	    te.setText(" " + what + " " + chk + ";");
	    if (jmltag != null) jmltag.fragments().add(te);
	  }
       }

      return jd;
    }

}	// end of subclass ContractAdder





}	// end of class JavaContracts



/* end of JavaContracts.java */



