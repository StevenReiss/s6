/********************************************************************************/
/*										*/
/*		RequestTestCase.java						*/
/*										*/
/*	Abstract class for test case information				*/
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

/* RCS: $Header: /pro/spr_cvs/pro/s6/javasrc/edu/brown/cs/s6/request/RequestTestCase.java,v 1.8 2015/09/23 17:58:08 spr Exp $ */


/*********************************************************************************
 *
 * $Log: RequestTestCase.java,v $
 * Revision 1.8  2015/09/23 17:58:08  spr
 * Updates for Andriod UI and better keysearch.
 *
 * Revision 1.7  2013/09/13 20:33:08  spr
 * Add calls for UI search.
 *
 * Revision 1.6  2013-05-09 12:26:23  spr
 * Minor changes to start ui fixups.
 *
 * Revision 1.5  2012-06-20 12:21:35  spr
 * Initial fixes for UI search
 *
 * Revision 1.4  2012-06-11 14:08:10  spr
 * Add framework search; fix bugs
 *
 * Revision 1.3  2009-09-18 01:41:54  spr
 * Handle full class option.
 *
 * Revision 1.2  2008-06-12 17:47:54  spr
 * Next version of S6.
 *
 * Revision 1.1.1.1  2008-06-03 12:59:22  spr
 * Initial version of S6
 *
 *
 ********************************************************************************/



package edu.brown.cs.s6.request;


import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.s6.common.S6Constants;
import edu.brown.cs.s6.common.S6Exception;
import edu.brown.cs.s6.common.S6TestCase;



abstract class RequestTestCase implements S6TestCase, S6Constants, RequestConstants {



/********************************************************************************/
/*										*/
/*	Creation methods							*/
/*										*/
/********************************************************************************/

static RequestTestCase createTestCase(Element xml) throws S6Exception
{
   String typ = IvyXml.getAttrString(xml,"TYPE");
   if (typ == null) return null;
   if (typ.equalsIgnoreCase("CALLS")) return new CallSetTest(xml);
   else if (typ.equalsIgnoreCase("USERCODE")) return new UserCodeTest(xml);
   else if (typ.equalsIgnoreCase("JUNIT")) return new UserJunitTest(xml);
   return null;
}



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private String		test_name;
private boolean 	is_optional;
protected boolean	user_input;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

protected RequestTestCase(Element xml) throws S6Exception
{
   test_name = IvyXml.getAttrString(xml,"NAME");
   is_optional = IvyXml.getAttrBool(xml,"OPTIONAL",false);
   user_input = false;

   if (test_name == null) throw new S6Exception("TESTCASE must be named");
}




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

public String getName() 			{ return test_name; }

public boolean isOptional()			{ return is_optional; }

abstract public S6TestType getTestType();

public String getUserCode()			{ return null; }

public List<CallTest> getCalls()		{ return null; }

public String getJunitClass()                   { return null; }
public String getJunitName()                    { return null; }

public boolean getNeedsUserInput()		{ return user_input; }



/********************************************************************************/
/*										*/
/*	UserCodeTest -- test case where user defines the whole method		*/
/*										*/
/********************************************************************************/

private static class UserCodeTest extends RequestTestCase {

   private String user_code;

   UserCodeTest(Element xml) throws S6Exception {
      super(xml);
      user_code = IvyXml.getTextElement(xml,"CODE");
    }

   public S6TestType getTestType()		{ return S6TestType.USERCODE; }

   public String getUserCode()			{ return user_code; }

}	// end of subclass UserCodeTest



/********************************************************************************/
/*                                                                              */
/*      Context-based Junit test                                                */
/*                                                                              */
/********************************************************************************/

private static class UserJunitTest extends RequestTestCase implements S6TestCase.JunitTest {
   
   private String junit_class;
   private String junit_method;
   private String junit_name;

   UserJunitTest(Element xml) throws S6Exception {
      super(xml);
      junit_class = IvyXml.getTextElement(xml,"CLASS");
      junit_method = IvyXml.getTextElement(xml,"METHOD");
      junit_name = IvyXml.getTextElement(xml,"TESTNAME");
    }
   
   public S6TestType getTestType()              { return S6TestType.JUNIT; }
   
   public String getJunitClass()                { return junit_class; }
   public String getJunitName()                 { return junit_name; }
   
   public String getUserCode() {
      return "// " + junit_name + " " + junit_class + "." + junit_method + "\n";
    }
   
}       // end of innter class UserJunitTest 





/********************************************************************************/
/*										*/
/*	CallSetTest -- test where user provides input and output		*/
/*										*/
/********************************************************************************/

private static class CallSetTest extends RequestTestCase {

   private List<CallTest> call_set;
   private String setup_code;

   CallSetTest(Element xml) throws S6Exception {
      super(xml);
      call_set = new ArrayList<CallTest>();
      for (Element e : IvyXml.elementsByTag(xml,"CALL")) {
	 if (!isValidCallTest(e)) continue;
	 CallTestImpl cti = new CallTestImpl(e);
	 call_set.add(cti);
	 if (cti.getNeedsUserInput()) user_input = true;
       }
      setup_code = IvyXml.getTextElement(xml,"CODE");
    }

   public S6TestType getTestType()		{ return S6TestType.CALLS; }
   public String getUserCode()			{ return setup_code; }

   public List<CallTest> getCalls()		{ return call_set; }

}	// end of subclass CallSetTest


private static boolean isValidCallTest(Element e)
{
   if (IvyXml.getAttrString(e,"THIS") != null) return true;
   if (IvyXml.getAttrString(e,"METHOD") != null) return true;
   if (IvyXml.getAttrBool(e,"NEW")) return true;

   return false;
}



private static class CallTestImpl implements S6TestCase.CallTest, RequestConstants {

   private String call_name;
   private List<CallArg> call_args;
   private CallArg	 result_code;
   private boolean	is_new;
   private S6TestOp	test_op;
   private String	throw_type;
   private boolean	user_input;
   private boolean	is_access;

   CallTestImpl(Element xml) throws S6Exception {
      result_code = null;
      user_input = false;
      is_new = IvyXml.getAttrBool(xml,"NEW");
      call_name = IvyXml.getTextElement(xml,"METHOD");
      test_op = IvyXml.getAttrEnum(xml,"OP",S6TestOp.NONE);
      if (test_op == S6TestOp.SHOW || test_op == S6TestOp.HIERARCHY ||
             test_op == S6TestOp.SCOREHIER || test_op == S6TestOp.INTERACT) 
         user_input = true;
      String cthis = IvyXml.getTextElement(xml,"THIS");
      if (call_name == null) {
         is_access = true;
         call_name = cthis;
       }
      else if (cthis != null) call_name = cthis + "." + call_name;
   
      call_args = new ArrayList<CallArg>();
      for (Element e : IvyXml.elementsByTag(xml,"INPUT")) {
         call_args.add(new CallArgImpl(e));
       }
   
      Element oe = IvyXml.getElementByTag(xml,"OUTPUT");
      if (oe != null) result_code = new CallArgImpl(oe);
   
      if (test_op == S6TestOp.NONE) {
         if (result_code == null) test_op = S6TestOp.IGNORE;
         else test_op = S6TestOp.EQL;
       }
   
      throw_type = IvyXml.getTextElement(xml,"THROW");
    }

   public String getMethod()			{ return call_name; }
   public List<CallArg> getArguments()		{ return call_args; }
   public CallArg getReturnValue()		{ return result_code; }
   public boolean isConstructor()		{ return is_new; }
   public S6TestOp getOperator()		{ return test_op; }
   public String getThrows()			{ return throw_type; }
   public boolean getNeedsUserInput()		{ return user_input; }
   public boolean isAccess()			{ return is_access; }

}	// end of subclass CallTestImpl




private static class CallArgImpl implements S6TestCase.CallArg, RequestConstants {

   private S6TestArgType arg_type;
   private String arg_value;
   private String arg_code;

   CallArgImpl(Element xml) throws S6Exception {
      arg_type = IvyXml.getAttrEnum(xml,"TYPE",S6TestArgType.LITERAL);
      arg_code = IvyXml.getTextElement(xml,"CODE");
      arg_value = IvyXml.getTextElement(xml,"VALUE");
      if (arg_value == null) arg_value = IvyXml.getText(xml);
    }

   public S6TestArgType getArgType()		{ return arg_type; }
   public String getArgValue()			{ return arg_value; }
   public String getArgCode()			{ return arg_code; }

}	// end of subclass CallArgImpl


}	// end of class RequestTestCase



/* end of RequestTestCase.java */
