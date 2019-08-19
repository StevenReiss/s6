/********************************************************************************/
/*										*/
/*		JavaChecker.java						*/
/*										*/
/*	Class to handle syntax checking of user input				*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/language/java/JavaChecker.java,v 1.15 2016/07/18 23:05:25 spr Exp $ */


/*********************************************************************************
 *
 * $Log: JavaChecker.java,v $
 * Revision 1.15  2016/07/18 23:05:25  spr
 * Update transforms for applications and UI.
 *
 * Revision 1.14  2015/09/23 17:54:52  spr
 * Version to handle andriod UI applications.
 *
 * Revision 1.13  2015/02/14 19:40:18  spr
 * Add test case generation.
 *
 * Revision 1.12  2014/08/29 15:16:08  spr
 * Updates for suise, testcases.
 *
 * Revision 1.11  2013/09/13 20:33:03  spr
 * Add calls for UI search.
 *
 * Revision 1.10  2012-06-20 12:21:32  spr
 * Initial fixes for UI search
 *
 * Revision 1.9  2012-06-11 14:07:48  spr
 * add framework search; fix bugs
 *
 * Revision 1.8  2009-09-18 01:41:35  spr
 * Handle user testing.
 *
 * Revision 1.7  2009-05-12 22:28:48  spr
 * Fix ups to make user context work.
 *
 * Revision 1.6  2008-11-12 13:52:14  spr
 * Performance and bug updates.
 *
 * Revision 1.5  2008-08-28 00:32:56  spr
 * Next version of S6.	Lots of bug fixes, some new functionality.
 *
 * Revision 1.4  2008-07-18 22:27:09  spr
 * Handle remove compilation calls; update transforms to include code to use ASTrewrite.
 *
 * Revision 1.3  2008-07-17 13:46:45  spr
 * Bug fixes and speed ups.
 *
 * Revision 1.2  2008-06-12 17:47:52  spr
 * Next version of S6.
 *
 * Revision 1.1.1.1  2008-06-03 12:59:23  spr
 * Initial version of S6
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.language.java;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.WildcardType;
import org.w3c.dom.Element;

import edu.brown.cs.ivy.jcomp.JcompAst;
import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.ivy.jcomp.JcompTyper;
import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.ivy.xml.IvyXmlWriter;
import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Exception;
import edu.brown.cs.s6.common.S6Request;
import edu.brown.cs.s6.slim.SlimFilePermission;
import edu.brown.cs.s6.slim.SlimPropertyPermission;
import edu.brown.cs.s6.slim.SlimSocketPermission;


class JavaChecker implements S6Constants, JavaConstants {



/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private LanguageJava	for_language;
private S6Request	for_request;
private JcompTyper	java_typer;
private int		var_counter;
private String		current_package;
private String		current_class;
private List<String>	import_packages;
private List<String>	import_types;
private String		full_classname;
private JcompType	current_type;
private Map<String,String> active_vars;
private JavaContracts	contract_helper;


private static String [] prefix_set = new String[] {
   "java.lang.",
   "java.util.",
   "java.io.",
   "java.awt.",
   "java.awt.event.",
   "java.lang.reflect.",
   "java.net.",
   "java.text.",
   "java.util.regex.",
   "java.util.zip.",
   "java.util.jar.",
   "javax.swing.",
   "javax.swing.event.",
   "javax.swing.tree.",
   "javax.swing.table.",
   "javax.xml.",
   "org.w3c.dom."
};




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

JavaChecker(LanguageJava lj,S6Request creq) throws S6Exception
{
   for_language = lj;
   for_request = creq;
   java_typer = null;
   var_counter = 0;
   full_classname = null;
   current_class = null;
   current_package = null;
   import_packages = new ArrayList<String>();
   import_types = new ArrayList<String>();
   current_type = null;
   active_vars = new HashMap<String,String>();
   contract_helper = new JavaContracts();
}




/********************************************************************************/
/*										*/
/*	Routines to check method signatures					*/
/*										*/
/********************************************************************************/

void checkMethodSignature(S6Request.Check creq,IvyXmlWriter xw) throws S6Exception
{
   String cls = creq.getUserInput("CLASS");
   String txt = creq.getUserInput("METHOD");

   setupCurrentClass(cls,creq);

   StreamTokenizer stok = getTokenizer(txt);

   AST ast = AST.newAST(AST.JLS8);
   MethodDeclaration md = ast.newMethodDeclaration();

   parseModifiers(stok,md);
   parseReturnType(stok,md);

   if (checkNextToken(stok,'(') && current_class != null &&
	  md.getReturnType2().toString().equals(full_classname)) {
	  stok.pushBack();
      md.setReturnType2(null);
      md.setName(JavaAst.getSimpleName(ast,current_class));
      md.setConstructor(true);
    }
   else {
      parseMethodName(stok,md);
    }

   parseArguments(stok,md);
   parseExceptions(stok,md);
   parseEnd(stok);

   generateMethodSignature(md,xw);
}




/********************************************************************************/
/*										*/
/*	Methods to check test cases						*/
/*										*/
/********************************************************************************/

void checkCallTest(S6Request.Check creq,IvyXmlWriter xw) throws S6Exception
{
   S6Request.MethodSignature msig = (S6Request.MethodSignature) creq.getSignature();
   if (msig == null) throw new S6Exception("No signature given");

   xw.begin("TESTCASE");
   xw.field("TYPE","CALLS");
   xw.field("NAME",creq.getUserInput("TESTNAME"));
   xw.begin("CALL");
   xw.field("METHOD",msig.getName());

   StreamTokenizer stok = getTokenizer(creq.getUserInput("ARGS"));
   checkNextToken(stok,'(');
   int act = 0;
   for (String ptyp : msig.getParameterTypeNames()) {
      if (act++ != 0) checkNextToken(stok,',');
      parseTypedValue(stok,ptyp,xw,"INPUT");
    }
   if (act == 0) checkNextToken(stok,"void");
   checkNextToken(stok,')');
   if (nextToken(stok) != StreamTokenizer.TT_EOF) throw new S6Exception("Parameter mismatch");

   stok = getTokenizer(creq.getUserInput("RESULT"));
   parseTypedValue(stok,msig.getReturnTypeName(),xw,"OUTPUT");
   if (nextToken(stok) != StreamTokenizer.TT_EOF) throw new S6Exception("Return mismatch call");

   xw.end("CALL");
   xw.end("TESTCASE");
}




/********************************************************************************/
/*										*/
/*	Method to check generic test set					*/
/*										*/
/********************************************************************************/

void checkTests(S6Request.Check creq,IvyXmlWriter xw) throws S6Exception
{
   S6Request.Signature sig = creq.getSignature();
   if (sig instanceof S6Request.MethodSignature) {
      checkMethodTests(creq,xw);
    }
   else {
      checkClassTests(creq,xw);
    }
}



/********************************************************************************/
/*										*/
/*	Methods to check Method tests						*/
/*										*/
/********************************************************************************/

void checkMethodTests(S6Request.Check creq,IvyXmlWriter xw) throws S6Exception
{
   S6Request.MethodSignature msig = (S6Request.MethodSignature) creq.getSignature();
   if (msig == null) throw new S6Exception("No signature given");

   int state = 0;
   xw.begin("TESTS");

   for (Element telt : creq.getUserElements("TEST")) {
      String typ = IvyXml.getAttrString(telt,"TYPE");
      String tid = IvyXml.getAttrString(telt,"TESTID");
      String tnm = IvyXml.getAttrString(telt,"TESTNAME");
      if (tnm == null) tnm = "test_" + tid;
      String err = null;
      if (typ == null) throw new S6Exception("No type given for test");
      try {
	 if (typ.equals("CALL")) {
	    state = startTestCase("CALLS",tnm,state,xw);
	    err = handleMethodCallTest(msig,telt,xw);
	  }
	 else if (typ.equals("USERCODE")) {
	    state = startTestCase("USERCODE",tnm,state,xw);
	    handleMethodUserTest(telt,xw);
	  }
	 else if (typ.equals("TESTFILE")) {
	    // handle file-based test
	  }
       }
      catch (S6Exception e) {
	 err = e.getMessage();
       }

      if (err != null) {
	 xw.begin("ERROR");
	 xw.field("TESTID",tid);
	 xw.field("MESSAGE",err);
	 xw.end("ERROR");
       }
    }

   state = startTestCase(null,null,state,xw);

   xw.end("TESTS");
}



private String handleMethodCallTest(S6Request.MethodSignature msig,Element telt,IvyXmlWriter xw)
   throws S6Exception
{
   xw.begin("CALL");
   xw.field("METHOD",msig.getName());

   String op = IvyXml.getTextElement(telt,"OP");
   xw.field("OP",op);

   try {
      StreamTokenizer stok = getTokenizer(IvyXml.getTextElement(telt,"INPUT"));
      checkNextToken(stok,'(');
      int act = 0;
      for (String ptyp : msig.getParameterTypeNames()) {
	 if (act++ != 0) checkNextToken(stok,',');
	 parseTypedValue(stok,ptyp,xw,"INPUT");
       }
      if (act == 0) checkNextToken(stok,"void");
      checkNextToken(stok,')');
      if (nextToken(stok) != StreamTokenizer.TT_EOF) throw new S6Exception("Parameter mismatch");

      if (op.equals("THROW")) {
	 String tnm = IvyXml.getTextElement(telt,"OUTPUT");
	 String xnm = findTypeName(tnm);
	 // System.err.println("CHECK TYPE " + tnm + " " + xnm);
	 if (xnm != null) tnm = xnm;
	 xw.textElement("THROW",tnm);
       }
      else if (op.equals("SHOW") || op.equals("IGNORE") ||
	    op.equals("INTERACT") || op.equals("HIERARCHY")) ;
      else {
	 stok = getTokenizer(IvyXml.getTextElement(telt,"OUTPUT"));
	 parseTypedValue(stok,msig.getReturnTypeName(),xw,"OUTPUT");
	 if (nextToken(stok) != StreamTokenizer.TT_EOF) throw new S6Exception("Return mismatch " + op);
       }
    }
   finally {
      xw.end("CALL");
    }

   return null;
}



private void handleMethodUserTest(Element telt,IvyXmlWriter xw)
{
   String code = IvyXml.getTextElement(telt,"OUTPUT");

   xw.cdataElement("CODE",code);
}


/********************************************************************************/
/*										*/
/*	Methods to check Class tests						*/
/*										*/
/********************************************************************************/

void checkClassTests(S6Request.Check creq,IvyXmlWriter xw) throws S6Exception
{
   S6Request.ClassSignature csig = creq.getSignature().getClassSignature();
   if (csig == null) throw new S6Exception("No signature given");

   setupCurrentClass(csig.getName(),creq);

   int state = 0;
   xw.begin("TESTS");

   for (Element telt : creq.getUserElements("TEST")) {
      String typ = IvyXml.getAttrString(telt,"TYPE");
      String tid = IvyXml.getAttrString(telt,"TESTID");
      String tnm = IvyXml.getAttrString(telt,"TESTNAME");
      if (tnm == null) tnm = "test_" + tid;
      String err = null;
      if (typ == null) throw new S6Exception("No type given for test");
      try {
	 if (typ.equals("CALLSET")) {
	    state = startTestCase("CALLS",tnm,state,xw);
	    err = handleClassCallTest(csig,telt,xw);
	  }
	 else if (typ.equals("CALLSETCONT")) {
	    err = handleClassCallTest(csig,telt,xw);
	  }
	 else if (typ.equals("CLASSCALL")) {
	    state = startTestCase("CALLS",tnm,state,xw);
	    err = handleClassCallTest(csig,telt,xw);
	  }
	 else if (typ.equals("USERCODE")) {
	    state = startTestCase("USERCODE",tnm,state,xw);
	    // handle user code test
	  }
	 else if (typ.equals("TESTFILE")) {
	  }
       }
      catch (S6Exception e) {
	 err = e.getMessage();
       }

      if (err != null) {
	 xw.begin("ERROR");
	 xw.field("TESTID",tid);
	 xw.field("MESSAGE",err);
	 xw.end("ERROR");
       }
    }

   state = startTestCase(null,null,state,xw);

   xw.end("TESTS");
}




private int startTestCase(String type,String name,int state,IvyXmlWriter xw)
{
   if (state != 0) {
      xw.end("TESTCASE");
    }

   if (type == null) state = 0;
   else {
      state = 1;
      xw.begin("TESTCASE");
      xw.field("NAME",name);
      xw.field("TYPE",type);
      active_vars.clear();
    }

   return state;
}




String handleClassCallTest(S6Request.ClassSignature csig,
				       Element test,
				       IvyXmlWriter xw) throws S6Exception
{
   String m = IvyXml.getTextElement(test,"METHOD");
   String var = null;
   boolean isnew = false;
   if (m != null) {
      int idx = m.indexOf(".");
      if (idx >= 0) {
	 var = m.substring(0,idx);
	 m = m.substring(idx+1);
       }
      isnew = m.equals("<init>");
    }

   if (var == null) var = IvyXml.getTextElement(test,"THIS");

   String op = IvyXml.getTextElement(test,"OP");
   String input = IvyXml.getTextElement(test,"INPUT");
   String output = IvyXml.getTextElement(test,"OUTPUT");

   S6Request.MethodSignature msig = null;
   for (S6Request.MethodSignature ms : csig.getMethods()) {
      if (ms.getName().equals(m)) {
	 msig = ms;
	 break;
       }
    }
   if (m != null && msig == null) return "Method not defined";

   String rtyp = (msig == null ? null : msig.getReturnTypeName());

   if (var != null) {
      if (isnew) return "Can't call constructor with 'this' specified";
      String vtyp = active_vars.get(var);
      if (vtyp == null) return "Unknown variable " + var;
      if (!vtyp.equals(csig.getName())) return "Illegal this pointer";
      if (msig == null) rtyp = vtyp;
    }
   else if (isnew) {
      if (active_vars.get("*CNST") != null) return "Implicit and explict constructor used";
    }
   else if (msig == null || msig.isStatic()) ;
   else {
      active_vars.put("*CNST","TRUE");
    }

   xw.begin("CALL");

   if (isnew) {
      xw.field("METHOD",csig.getName());
      xw.field("NEW",true);
    }
   else if (msig != null) {
      xw.field("METHOD",msig.getName());
    }

   if (var != null) xw.field("THIS",var);

   xw.field("OP",op);

   try {
      // next check arguments
      StreamTokenizer stok = getTokenizer(input);
      checkNextToken(stok,'(');
      int act = 0;
      if (msig != null) {
	 for (String ptyp : msig.getParameterTypeNames()) {
	    if (act++ != 0) checkNextToken(stok,',');
	    parseTypedValue(stok,ptyp,xw,"INPUT");
	  }
       }
      if (act == 0) checkNextToken(stok,"void");
      checkNextToken(stok,')');
      if (nextToken(stok) != StreamTokenizer.TT_EOF) throw new S6Exception("Parameter mismatch");

      // now check return or save value
      if (op.equalsIgnoreCase("EQL") || op.equalsIgnoreCase("NEQ") || op.equalsIgnoreCase("SAME") ||
	     op.equalsIgnoreCase("DIFF")) {
	 stok = getTokenizer(output);
	 parseTypedValue(stok,rtyp,xw,"OUTPUT");
	 if (nextToken(stok) != StreamTokenizer.TT_EOF) throw new S6Exception("Return mismatch class");
       }
      else if (op.equalsIgnoreCase("SAVE")) {
	 if (output == null || output.length() == 0)
	    throw new S6Exception("Save without variable name");
	 for (int i = 0; i < output.length(); ++i) {
	    if (i == 0 && !Character.isJavaIdentifierStart(output.charAt(i)))
	       throw new S6Exception("Illegal variable name");
	    else if (i > 0 && !Character.isJavaIdentifierPart(output.charAt(i)))
	       throw new S6Exception("Illegal character in variable");
	  }
	 xw.begin("OUTPUT");
	 xw.field("TYPE",S6TestArgType.SAVE);
	 xw.field("VALUE",output);
	 if (!active_vars.containsKey(output)) {
	    String typ = (isnew ? csig.getName() : rtyp);
	    active_vars.put(output,typ);
	    xw.cdataElement("CODE",typ + " " + output + ";");
	  }
	 xw.end("OUTPUT");
       }
      else if (op.equalsIgnoreCase("IGNORE")) {
       }
      else if (op.equalsIgnoreCase("THROW")) {
	 xw.textElement("THROW",output);
       }
    }
   finally {
      xw.end("CALL");
    }

   return null;
}




/********************************************************************************/
/*										*/
/*	Methods to handle security checking					*/
/*										*/
/********************************************************************************/

void checkSecurity(S6Request.Check creq,IvyXmlWriter xw)
{
   xw.begin("SECURITY");

   for (Element p : creq.getUserElements("PERMISSION")) {
      int idx = IvyXml.getAttrInt(p,"INDEX");
      S6SecurityType typ = IvyXml.getAttrEnum(p,"TYPE",S6SecurityType.NONE);
      String val = IvyXml.getTextElement(p,"VALUE");
      String acts = IvyXml.getTextElement(p,"ACTIONS");
      String err = null;
      try {
	 switch (typ) {
	    default :
	       break;
	    case FILE :
	       new SlimFilePermission(val,acts);
	       break;
	    case SOCKET :
	       new SlimSocketPermission(val,acts);
	       break;
	    case PROPERTY :
	       new SlimPropertyPermission(val,acts);
	       break;
	  }
       }
      catch (Exception e) {
	 err = e.getMessage();
	 if (err == null) err = e.toString();
       }
      if (err != null) {
	 xw.begin("ERROR");
	 xw.field("INDEX",idx);
	 xw.field("MESSAGE",err);
	 xw.end("ERROR");
       }
    }

   xw.end();
}




/********************************************************************************/
/*										*/
/*	Methods to handle contract checking					*/
/*										*/
/********************************************************************************/

void checkContracts(S6Request.Check creq,IvyXmlWriter xw) throws S6Exception
{
   contract_helper.checkContracts(creq,xw);
}




/********************************************************************************/
/*										*/
/*	Methods to handle context checking					*/
/*										*/
/********************************************************************************/

void checkContext(S6Request.Check creq,IvyXmlWriter xw)
{
   xw.begin("CONTEXT");
   xw.end();
}




/********************************************************************************/
/*										*/
/*	Methods to handle class name checking					*/
/*										*/
/********************************************************************************/

void checkClassNames(S6Request.Check creq,boolean mult,IvyXmlWriter xw)
{
   String txt = creq.getUserInput("NAME");

   AST ast = AST.newAST(AST.JLS8);
   StreamTokenizer tok = getTokenizer(txt);
   StringBuffer buf = new StringBuffer();

   try {
      for (int i =0; ; ++i) {
	 Type typ = parseType(tok,ast);
	 if (typ == null) throw new S6Exception("Type not found");
	 if (i > 0) buf.append(" ; ");
	 buf.append(typ.toString());
	 if (!mult) {
	    parseEnd(tok);
	    break;
	  }
	 if (checkEnd(tok)) break;
	 if (checkNextToken(tok,',')) ;
	 else checkNextToken(tok,';');
       }
    }
   catch (S6Exception e) {
      xw.begin("ERROR");
      xw.field("MESSAGE",e.getMessage());
      xw.end("ERROR");
      return;
    }

   xw.begin("CLASSES");
   xw.textElement("NAME",buf.toString());
   xw.end("CLASSES");
}



/********************************************************************************/
/*										*/
/*	Utility methods 							*/
/*										*/
/********************************************************************************/

private StreamTokenizer getTokenizer(String s)
{
   if (s == null) s = "";

   StreamTokenizer stok = new StreamTokenizer(new StringReader(s));

   stok.slashStarComments(true);
   stok.slashSlashComments(true);

   return stok;
}



private void setupCurrentClass(String cls,S6Request.Check creq)
{
   full_classname = cls;

   current_type = null;

   if (cls == null) {
      current_package = null;
      current_class = null;
      if (creq != null && creq.getUserContext() != null) {
	 JavaContext jctx = new JavaContext(creq.getUserContext().getContextFile());
	 current_class = jctx.getContextClass();
	 current_package = jctx.getContextPackage();
       }
    }
   else {
      int idx = cls.lastIndexOf('.');
      if (idx < 0) {
	 current_package = null;
	 current_class = cls;
       }
      else {
	 current_package = cls.substring(0,idx+1);
	 current_class = cls.substring(idx+1);
       }
    }
}




/********************************************************************************/
/*										*/
/*	Parsing methods for Method Signatures					*/
/*										*/
/********************************************************************************/

@SuppressWarnings("unchecked")
private void parseModifiers(StreamTokenizer stok,BodyDeclaration md)
{
   int mods = 0;

   for ( ; ; ) {
      if (nextToken(stok) != StreamTokenizer.TT_WORD) {
	 stok.pushBack();
	 break;
       }
      if (stok.sval.equals("public")) mods |= Modifier.PUBLIC;
      else if (stok.sval.equals("protected")) mods |= Modifier.PROTECTED;
      else if (stok.sval.equals("private")) mods |= Modifier.PRIVATE;
      else if (stok.sval.equals("static")) mods |= Modifier.STATIC;
      else if (stok.sval.equals("abstract")) mods |= Modifier.ABSTRACT;
      else if (stok.sval.equals("final")) mods |= Modifier.FINAL;
      else if (stok.sval.equals("native")) mods |= Modifier.NATIVE;
      else if (stok.sval.equals("synchronized")) mods |= Modifier.SYNCHRONIZED;
      else if (stok.sval.equals("transient")) mods |= Modifier.TRANSIENT;
      else if (stok.sval.equals("volatile")) mods |= Modifier.VOLATILE;
      else if (stok.sval.equals("strictfp")) mods |= Modifier.STRICTFP;
      else {
	 stok.pushBack();
	 break;
       }
    }

   if (mods != 0) {
      List<?> newmods = md.getAST().newModifiers(mods);
      md.modifiers().addAll(newmods);
    }
}



private void parseReturnType(StreamTokenizer stok,MethodDeclaration md) throws S6Exception
{
   Type typ = parseType(stok,md.getAST());
   md.setReturnType2(typ);
}



private void parseMethodName(StreamTokenizer stok,MethodDeclaration md) throws S6Exception
{
   if (nextToken(stok) != StreamTokenizer.TT_WORD) {
      if (stok.ttype == '(') {
	 // handle constructor
       }
      throw new S6Exception("Method name missing");
    }

   SimpleName sn = JavaAst.getSimpleName(md.getAST(),stok.sval);
   md.setName(sn);
}




@SuppressWarnings("unchecked")
private void parseArguments(StreamTokenizer stok,MethodDeclaration md) throws S6Exception
{
   if (!checkNextToken(stok,'(')) throw new S6Exception("Parameter list missing");

   int anum = 1;
   for ( ; ; ) {
      int arrct = 0;
      if (checkNextToken(stok,')')) break;
      Type typ = parseType(stok,md.getAST());
      String anm = "a" + anum;
      ++anum;
      if (checkNextToken(stok,',') || checkNextToken(stok,')')) {
	 stok.pushBack();
       }
      else if (nextToken(stok) == StreamTokenizer.TT_WORD) {
	 anm = stok.sval;
	 while (checkNextToken(stok,'[')) {
	    if (!checkNextToken(stok,']')) throw new S6Exception("Bad array parameter");
	    ++arrct;
	  }
       }
      else throw new S6Exception("Expected agrument name");
      SingleVariableDeclaration svd = md.getAST().newSingleVariableDeclaration();
      for (int i = 0; i < arrct; ++i) {
         svd.extraDimensions().add(md.getAST().newDimension());
      }
      svd.setName(JavaAst.getSimpleName(md.getAST(),anm));
      svd.setType(typ);
      md.parameters().add(svd);
      if (checkNextToken(stok,')')) break;
      else if (!checkNextToken(stok,',')) throw new S6Exception("Illegal argument name");
    }
}



@SuppressWarnings("unchecked")
private void parseExceptions(StreamTokenizer stok,MethodDeclaration md) throws S6Exception
{
   if (!checkNextToken(stok,"throws")) return;

   for ( ; ; ) {
      Type typ = parseType(stok,md.getAST());
      String str = typ.toString();
      Name nm = JavaAst.getQualifiedName(md.getAST(),str);
      Type tnm = md.getAST().newSimpleType(nm);
      md.thrownExceptionTypes().add(tnm);
      if (!checkNextToken(stok,',')) break;
    }
}




/********************************************************************************/
/*										*/
/*	Parsing methods for argument types					*/
/*										*/
/********************************************************************************/

private void parseTypedValue(StreamTokenizer stok,String typ,IvyXmlWriter xw,String item)
	throws S6Exception
{
   Value v = parseTypedValue(stok,typ);

   if (v == null) return;

   v.outputXml(xw,item);
}



/********************************************************************************/
/*										*/
/*	Parsing utilities							*/
/*										*/
/********************************************************************************/

private int nextToken(StreamTokenizer stok)
{
   try {
      return stok.nextToken();
    }
   catch (IOException e) {
      return StreamTokenizer.TT_EOF;
    }
}




@SuppressWarnings("unchecked")
private Type parseType(StreamTokenizer stok,AST ast) throws S6Exception
{
   Type typ = null;

   if (checkNextToken(stok,"byte")) typ = ast.newPrimitiveType(PrimitiveType.BYTE);
   else if (checkNextToken(stok,"short")) typ = ast.newPrimitiveType(PrimitiveType.SHORT);
   else if (checkNextToken(stok,"char")) typ = ast.newPrimitiveType(PrimitiveType.CHAR);
   else if (checkNextToken(stok,"int")) typ = ast.newPrimitiveType(PrimitiveType.INT);
   else if (checkNextToken(stok,"long")) typ = ast.newPrimitiveType(PrimitiveType.LONG);
   else if (checkNextToken(stok,"float")) typ = ast.newPrimitiveType(PrimitiveType.FLOAT);
   else if (checkNextToken(stok,"double")) typ = ast.newPrimitiveType(PrimitiveType.DOUBLE);
   else if (checkNextToken(stok,"boolean")) typ = ast.newPrimitiveType(PrimitiveType.BOOLEAN);
   else if (checkNextToken(stok,"void")) typ = ast.newPrimitiveType(PrimitiveType.VOID);
   else if (checkNextToken(stok,'?')) {
      WildcardType wtyp = ast.newWildcardType();
      typ = wtyp;
      if (nextToken(stok) != StreamTokenizer.TT_WORD) {
	 stok.pushBack();
       }
      else if (checkNextToken(stok,"extends") || checkNextToken(stok,"super")) {
	 boolean ext = stok.sval.equals("extends");
	 Type ntyp = parseType(stok,ast);
	 wtyp.setBound(ntyp,ext);
       }
      else {
	 stok.pushBack();
       }
    }
   else if (nextToken(stok) == StreamTokenizer.TT_WORD) {
      String tnam = stok.sval;
      for ( ; ; ) {
	 if (!checkNextToken(stok,'.')) break;
	 if (nextToken(stok) != StreamTokenizer.TT_WORD)
	    throw new S6Exception("Illegal qualified name");
	 tnam += "." + stok.sval;
       }
      String s = findTypeName(tnam);
      if (s == null) throw new S6Exception("Type '" + tnam + "' not found");
      Name nm = JavaAst.getQualifiedName(ast,s);
      typ = ast.newSimpleType(nm);
    }
   else throw new S6Exception("Type expected");

   if (checkNextToken(stok,'<')) {
      ParameterizedType ptyp = ast.newParameterizedType(typ);
      typ = ptyp;
      for ( ; ; ) {
	 Type atyp = parseType(stok,ast);
	 ptyp.typeArguments().add(atyp);
	 if (checkNextToken(stok,'>')) break;
	 else if (!checkNextToken(stok,',')) throw new S6Exception("Bad parameterized argument");
       }
    }

   int ndim = 0;
   while (checkNextToken(stok,'[')) {
      if (!checkNextToken(stok,']')) throw new S6Exception("Missing right bracket");
      ++ndim;
    }
   if (ndim > 0) typ = ast.newArrayType(typ,ndim);

   return typ;
}




private boolean checkNextToken(StreamTokenizer stok,String tok)
{
   if (nextToken(stok) == StreamTokenizer.TT_WORD && stok.sval.equals(tok)) return true;

   stok.pushBack();
   return false;
}




private boolean checkNextToken(StreamTokenizer stok,char tok)
{
   if (nextToken(stok) == tok) return true;

   stok.pushBack();
   return false;
}




private boolean checkEnd(StreamTokenizer stok)
{
   if (nextToken(stok) == StreamTokenizer.TT_EOF) return true;

   stok.pushBack();
   return false;
}




private void parseEnd(StreamTokenizer stok) throws S6Exception
{
   if (nextToken(stok) != StreamTokenizer.TT_EOF) throw new S6Exception("Excess at end");
}




/********************************************************************************/
/*										*/
/*	Name lookup								*/
/*										*/
/********************************************************************************/

private String findTypeName(String nm)
{
   createTyper();

   if (current_class != null) {
      if (nm.equals(current_class)) return full_classname;
      else if (nm.equals(full_classname)) return full_classname;
    }

   if (checkSystemClass(nm)) return nm;

   int idx = nm.lastIndexOf('.');
   if (idx >= 0) return nm;

   if (current_package != null) {
      String t = current_package + "." + nm;
      if (checkSystemClass(t)) return t;
    }

   for (String p : prefix_set) {
      String t = p + nm;
      if (checkSystemClass(t)) return t;
    }

   for (String p : import_types) {
      if (p.endsWith("." + nm)) return p;
    }

   for (String p : import_packages) {
      String t = p + nm;
      if (checkSystemClass(t)) return t;
    }

   return null;
}



private synchronized boolean checkSystemClass(String nm)
{
   createTyper();

   if (java_typer.findSystemType(nm) != null) return true;

   return false;
}



private synchronized JcompType findType(String nm)
{
   createTyper();

   if (full_classname != null && nm.equals(full_classname)) {
      if (current_type == null) {
	 current_type = java_typer.defineUserType(full_classname,false,false,false);
       }
      return current_type;
    }

   return java_typer.findSystemType(nm);
}



private void createTyper()
{
   if (java_typer != null) return;

   java_typer = for_language.createTyper(for_request);
   JavaContext ctx = for_language.getContext(for_request);

   if (current_package == null && current_class == null) {
      current_package = for_request.getPackage();
      if (current_package == null && ctx != null) current_package = ctx.getContextPackage();
      if (ctx != null) current_class = ctx.getContextClass();
      if (current_package == null) full_classname = current_class;
      else full_classname = current_package + "." + current_class;
      if (ctx != null) {
	 for (String s : ctx.getContextImports()) {
	    if (s.endsWith(".*")) {
	       int ln = s.length();
	       import_packages.add(s.substring(0,ln));
	     }
	    else import_types.add(s);
	  }
       }
    }
}




/********************************************************************************/
/*										*/
/*	Output methods								*/
/*										*/
/********************************************************************************/

private void generateMethodSignature(MethodDeclaration md,IvyXmlWriter xw)
{
   StringBuffer buf = new StringBuffer();
   StringBuffer nbuf = new StringBuffer();
   for (Iterator<?> it = md.parameters().iterator(); it.hasNext(); ) {
      SingleVariableDeclaration svd = (SingleVariableDeclaration) it.next();
      if (buf.length() > 0) buf.append(",");
      buf.append(svd.getType().toString());
      for (int i = 0; i < svd.getExtraDimensions(); ++i) buf.append("[]");
      if (nbuf.length() > 0) nbuf.append(",");
      nbuf.append(svd.getName().getIdentifier());
    }
   StringBuffer ebuf = new StringBuffer();
   for (Iterator<?> it = md.thrownExceptionTypes().iterator(); it.hasNext(); ) {
      Type tnm = (Type) it.next();
      if (ebuf.length() > 0) ebuf.append(",");
      JcompType jty = JcompAst.getJavaType(tnm);
      ebuf.append(jty.getName());
    }

   xw.begin("SIGNATURE");

   xw.begin("METHOD");

   xw.field("MODS",md.getModifiers());
   for (Iterator<?> it = md.modifiers().iterator(); it.hasNext(); ) {
      Modifier mod = (Modifier) it.next();
      xw.field(mod.getKeyword().toString().toUpperCase(),true);
    }

   if (md.getReturnType2() == null) {
      xw.field("CONSTRUCTOR",true);
    }
   else if (md.getReturnType2().isPrimitiveType()) {
      PrimitiveType pty = (PrimitiveType) md.getReturnType2();
      PrimitiveType.Code ptc = pty.getPrimitiveTypeCode();
      xw.field(ptc.toString().toUpperCase(),true);
    }
   else if (md.getReturnType2().toString().equals("java.lang.String")) {
      xw.field("STRING",true);
    }

   if (md.isConstructor()) {
      xw.textElement("NAME","<init>");
    }
   else {
      xw.textElement("NAME",md.getName().getIdentifier());
      xw.textElement("RETURN",md.getReturnType2().toString());
    }
   xw.textElement("ARGS",buf.toString());
   xw.textElement("ARGNAMES",nbuf.toString());
   if (ebuf.length() > 0) xw.textElement("EXCEPTIONS",ebuf.toString());

   String s = md.toString().trim();
   if (s.endsWith(";")) s = s.substring(0,s.length()-1);
   xw.textElement("TEXT",s);

   xw.end("METHOD");

   xw.end("SIGNATURE");
}




/********************************************************************************/
/*										*/
/*	Value parsing								*/
/*										*/
/********************************************************************************/

private Value parseTypedValue(StreamTokenizer stok,String typ) throws S6Exception
{
   Value rslt = null;

   if (typ == null || typ.equals("void")) return null;

   JcompType jt = findType(typ);
   if (jt == null) throw new S6Exception("Type " + typ + " not defined");
   JcompType ajty = jt.getAssociatedType();

   nextToken(stok);

   if (ajty != null) {
      if (stok.ttype == StreamTokenizer.TT_NUMBER && ajty.isNumericType()) jt = ajty;
      else if (stok.ttype == StreamTokenizer.TT_WORD && ajty.isBooleanType()) jt = ajty;
    }

   if (stok.ttype == StreamTokenizer.TT_WORD && active_vars.containsKey(stok.sval)) {
      String vsty = active_vars.get(stok.sval);
      JcompType vjt = findType(vsty);
      if (vjt == null) throw new S6Exception("Variable type " + vsty + " not found");
      if (!vjt.isCompatibleWith(jt)) throw new S6Exception("Variable type incompatible");
      rslt = new ValueVariable(stok.sval,null);
    }
   else if (jt.isNumericType()) {
      if (stok.ttype == StreamTokenizer.TT_NUMBER) {
	 if (typ.equals("float")) rslt = new ValueLiteral(Double.toString(stok.nval) + "f");
	 else if (typ.equals("double")) rslt = new ValueLiteral(Double.toString(stok.nval));
	 else if (typ.equals("byte")) {
	    byte cv = (byte) stok.nval;
	    rslt = new ValueLiteral("((byte) " + cv + ")");
	  }
	 else if (typ.equals("short")) {
	    short cv = (short) stok.nval;
	    rslt = new ValueLiteral("((short) " + cv + ")");
	  }
	 else if (typ.equals("char")) {
	    char cv = (char) stok.nval;
	    rslt = new ValueLiteral("((char) " + cv + ")");
	  }
	 else rslt = new ValueLiteral(Long.toString((long) stok.nval));
       }
      else if (stok.ttype == '\'') {
	 rslt = new ValueLiteral("'" + stok.sval + "'");
       }
      else {
	 throw new S6Exception("Number expected");
       }
    }
   else if (jt.isBooleanType()) {
      boolean val = false;
      if (stok.ttype == StreamTokenizer.TT_WORD) {
	 if (stok.sval.startsWith("t") || stok.sval.startsWith("T")) val = true;
       }
      else throw new S6Exception("Boolean expected");
      rslt = new ValueLiteral(Boolean.toString(val));
    }
   else if (typ.equals("java.lang.String") ||
	       (typ.equals("java.lang.Object") && stok.ttype == '"')) {
      if (stok.ttype == '"' || stok.ttype == '\'' || stok.ttype == StreamTokenizer.TT_WORD) {
	 rslt = new ValueString(stok.sval);
       }
      else throw new S6Exception("String expected");
    }
   else if (stok.ttype == '[' &&
	       (jt.isArrayType() || jt.isCompatibleWith(findType("java.util.Collection")))) {
      JcompType bjt = jt.getBaseType();
      String st = "java.lang.Object";
      if (jt.isParameterizedType()) {
	 bjt = bjt.getBaseType();
	 JcompType argty = jt.getComponents().get(0);
	 st = argty.getName();
       }
      if (bjt != null) st = bjt.getName();
      List<Value> vals = new ArrayList<Value>();
      while (!checkNextToken(stok,']')) {
	 vals.add(parseTypedValue(stok,st));
	 checkNextToken(stok,',');
	 if (stok.ttype == StreamTokenizer.TT_EOF) throw new S6Exception("Array or list unterminated");
       }
      StringBuffer cbuf = new StringBuffer();
      for (Value v : vals) {
	 if (v.getCode() != null) {
	    cbuf.append(v.getCode());
	    cbuf.append(";\n");
	  }
       }
      if (jt.isArrayType()) {
	 StringBuffer lbuf = new StringBuffer();
	 lbuf.append("new " + st + "[] {");
	 int ct = 0;
	 for (Value v : vals) {
	    if (ct++ != 0) lbuf.append(" , ");
	    lbuf.append(v.getValue());
	  }
	 lbuf.append("}");
	 rslt = new ValueLiteral(lbuf.toString(),cbuf.toString());
       }
      else {
	 String var = getVariable();
	 cbuf.append(jt.getName() + " " + var + " = new java.util.ArrayList<" + st + ">();\n");
	 for (Value v : vals) {
	    cbuf.append(var + ".add(" + v.getValue() + ");\n");
	  }
	 rslt = new ValueVariable(var,cbuf.toString());
       }
    }
   else if (stok.ttype == '[' && jt.isCompatibleWith(findType("java.util.Map"))) {
      String kst = "java.lang.Object";
      String vst = "java.lang.Object";
      if (jt.isParameterizedType()) {
	 JcompType argty = jt.getComponents().get(0);
	 kst = argty.getName();
	 argty = jt.getComponents().get(1);
	 vst = argty.getName();
       }
      Map<Value,Value> vals = new LinkedHashMap<Value,Value>();
      while (!checkNextToken(stok,']')) {
	 Value v0 = parseTypedValue(stok,kst);
	 if (!checkNextToken(stok,':')) throw new S6Exception("Map without key : value");
	 Value v1 = parseTypedValue(stok,vst);
	 vals.put(v0,v1);
	 checkNextToken(stok,',');
	 if (stok.ttype == StreamTokenizer.TT_EOF) throw new S6Exception("Map unterminated");
       }
      StringBuffer cbuf = new StringBuffer();
      for (Map.Entry<Value,Value> ent : vals.entrySet()) {
	 if (ent.getKey().getCode() != null) {
	    cbuf.append(ent.getKey().getCode());
	    cbuf.append(";\n");
	  }
	 if (ent.getValue().getCode() != null) {
	    cbuf.append(ent.getValue().getCode());
	    cbuf.append(";\n");
	  }
       }

      String var = getVariable();
      cbuf.append(jt.getName() + " " + var + " = new java.util.HashMap<" + kst + "," + vst + ">();\n");
      for (Map.Entry<Value,Value> ent : vals.entrySet()) {
	 cbuf.append(var + ".put(" + ent.getKey().getValue() + "," + ent.getValue().getValue() +
			");\n");
       }
      rslt = new ValueVariable(var,cbuf.toString());
    }
   else {
      StringBuffer buf = new StringBuffer();
      String var = null;
      if (stok.ttype == StreamTokenizer.TT_WORD) {
	 var = stok.sval;
	 nextToken(stok);
	 if (stok.ttype != '{') {
	    buf.append(var);
	    buf.append(" ");
	    var = null;
	  }
       }
      int lvl = 0;
      for ( ; ; ) {
	 int ttyp = stok.ttype;
	 if (ttyp == '{' || ttyp == '(') {
	    ++lvl;
	    buf.append((char) ttyp);
	  }
	 else if (ttyp == '}' || ttyp == ')') {
	    --lvl;
	    buf.append((char) ttyp);
	    if (lvl == 0) break;
	  }
	 else if (ttyp == '"' || ttyp == '\'') {
	    buf.append((char) ttyp);
	    buf.append(stok.sval);
	    buf.append((char) ttyp);
	  }
	 else if (ttyp == ',' && lvl == 0) {
	   stok.pushBack();
	   break;
	  }
	 else if (ttyp == StreamTokenizer.TT_EOF) {
	    stok.pushBack();
	    break;
	  }
	 else if (ttyp == StreamTokenizer.TT_WORD) {
	    buf.append(" ");
	    buf.append(stok.sval);
	    buf.append(" ");
	  }
	 else if (ttyp == StreamTokenizer.TT_NUMBER) {
	    Double db = Double.valueOf(stok.nval);
	    if (db.doubleValue() == db.longValue()) {
	       buf.append(db.longValue());
	     }
	    else buf.append(db.doubleValue());
	  }
	 else {
	    buf.append((char) ttyp);
	  }
	 nextToken(stok);
       }
      String s = buf.toString().trim();
      if (s.startsWith("{")) s = s.substring(1,s.length()-1);
      if (var == null) {
	 rslt = new ValueLiteral(s);
       }
      else {
	 rslt = new ValueVariable(var,s);
       }
    }

   return rslt;
}



private synchronized String getVariable()
{
   ++var_counter;
   return "v_" + var_counter;
}




/********************************************************************************/
/*										*/
/*	Class representing values						*/
/*										*/
/********************************************************************************/

private abstract static class Value {

   String getCode()			{ return null; }
   abstract String getValue();
   abstract void outputXml(IvyXmlWriter xw,String itm);

}	// end of subclass Value




private static class ValueLiteral extends Value {

   private String literal_value;
   private String code_value;

   ValueLiteral(String s) {
      literal_value = s;
      code_value = null;
    }

   ValueLiteral(String s,String c) {
      literal_value = s;
      if (c != null && c.trim().length() == 0) c = null;
      code_value = c;
    }

   String getCode()			{ return code_value; }
   String getValue()			{ return literal_value; }

   void outputXml(IvyXmlWriter xw,String itm) {
      xw.begin(itm);
      xw.field("TYPE",S6TestArgType.LITERAL);
      xw.cdataElement("VALUE",literal_value);
      if (code_value != null) xw.cdataElement("CODE",code_value);
      xw.end(itm);
    }

}	// end of subclass ValueLiteral



private static class ValueString extends Value {

   private String string_value;

   ValueString(String s) {
      string_value = s;
    }

   String getValue() {
      // handle special characters in the string
      return "\"" + string_value + "\"";
    }

   void outputXml(IvyXmlWriter xw,String itm) {
      xw.begin(itm);
      xw.field("TYPE",S6TestArgType.STRING);
      xw.cdataElement("VALUE",string_value);
      xw.end(itm);
    }

}	// end of subclass ValueString



private static class ValueVariable extends Value {

   private String var_name;
   private String gen_code;

   ValueVariable(String nm,String cod) {
      var_name = nm;
      gen_code = cod;
    }

   String getCode()				{ return gen_code; }

   String getValue()				{ return var_name; }

   void outputXml(IvyXmlWriter xw,String itm) {
      xw.begin(itm);
      xw.field("TYPE",S6TestArgType.VARIABLE);
      xw.field("VALUE",var_name);
      if (gen_code != null) xw.cdataElement("CODE",gen_code);
      xw.end(itm);
    }

}	// end of subclass ValueVariable



}	// end of class JavaChecker




/* end of JavaChecker.java */
