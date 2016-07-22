/********************************************************************************/
/*                                                                              */
/*              RequestTesting.java                                             */
/*                                                                              */
/*      description of class                                                    */
/*                                                                              */
/********************************************************************************/
/*      Copyright 2013 Brown University -- Steven P. Reiss                    */
/*********************************************************************************
 *  Copyright 2013, Brown University, Providence, RI.                            *
 *                                                                               *
 *                        All Rights Reserved                                    *
 *                                                                               *
 *  Permission to use, copy, modify, and distribute this software and its        *
 *  documentation for any purpose other than its incorporation into a            *
 *  commercial product is hereby granted without fee, provided that the          *
 *  above copyright notice appear in all copies and that both that               *
 *  copyright notice and this permission notice appear in supporting             *
 *  documentation, and that the name of Brown University not be used in          *
 *  advertising or publicity pertaining to distribution of the software          *
 *  without specific, written prior permission.                                  *
 *                                                                               *
 *  BROWN UNIVERSITY DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS                *
 *  SOFTWARE, INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND            *
 *  FITNESS FOR ANY PARTICULAR PURPOSE.  IN NO EVENT SHALL BROWN UNIVERSITY      *
 *  BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY          *
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,              *
 *  WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS               *
 *  ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE          *
 *  OF THIS SOFTWARE.                                                            *
 *                                                                               *
 ********************************************************************************/



package edu.brown.cs.s6.request;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.w3c.dom.Element;

import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.s6.common.S6Exception;
import edu.brown.cs.s6.common.S6Request;


class RequestTesting extends RequestSignature implements S6Request.TestingSignature
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private String          package_name;
private String          testclass_name;
private S6Request.ClassSignature test_class;
private TestingClass    testing_class;




/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

RequestTesting(Element xml) throws S6Exception
{
   package_name = IvyXml.getTextElement(xml,"PACKAGE");
   testclass_name = IvyXml.getTextElement(xml,"NAME");
   
   Element texml = IvyXml.getChild(xml,"TESTEE");
   Element tcxml = IvyXml.getChild(texml,"CLASS");
   test_class = new RequestClass(tcxml);
   
   testing_class = new TestingClass();
}



/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

@Override public String getName()
{
   return package_name;
}

@Override public String getPackage()            { return package_name; }



@Override public S6Request.MethodSignature getMethod(String nm)
{
   return null;
}

@Override public S6Request.ClassSignature getClassSignature()
{
   return testing_class;
}


@Override public S6Request.ClassSignature getClassToTest()
{
   return test_class;
}



/********************************************************************************/
/*                                                                              */
/*      Signature for resultant method                                          */
/*                                                                              */
/********************************************************************************/

class TestingClass extends RequestSignature implements S6Request.ClassSignature {
   
   private TestingMethod test_method;
   
   TestingClass() {
      test_method = new TestingMethod();
    }
   
   @Override public String getName()            { return testclass_name; }
   @Override public List<S6Request.MethodSignature> getMethods() {
      List<S6Request.MethodSignature> rslt = new ArrayList<S6Request.MethodSignature>();
      // rslt.add(test_method);
      return rslt;
    }
   @Override public List<S6Request.FieldSignature> getFields() {
      return Collections.emptyList(); 
    }
   @Override public boolean isInterface()       { return false; }
   @Override public boolean isMain()            { return false; }
   @Override public String getSuperClass()      { return null; }
   @Override public List<String> getInterfaces() { return Collections.emptyList(); }
   @Override public List<String> getKeywords()  { return Collections.emptyList(); }
   @Override public boolean includeTestCases()  { return true; }
   
   @Override public S6Request.MethodSignature getMethod(String nm) {
      if (nm == null) return null;
      if (nm.equals(test_method.getName())) return test_method;
      return null;
    }
   
   @Override public S6Request.ClassSignature getClassSignature() {
      return this;
    }
   
}       // end of inner class TestingMethod



class TestingMethod extends RequestSignature implements S6Request.MethodSignature {
   
   private String method_name;
   
   TestingMethod() {
      method_name = "s6TestMethod";
    }
   
   @Override public String getName()            { return method_name; }
   @Override public String getReturnTypeName()  { return "void"; }
   @Override public List<String> getParameterTypeNames() {
      return Collections.emptyList();
    }
   @Override public List<String> getExceptionTypeNames() {
      return Collections.emptyList();
    }
   @Override public List<String> getParameterNames() {
      return Collections.emptyList();
    }
   
   @Override public boolean isStatic()          { return false; }
   @Override public boolean isAbstract()        { return false; }
   @Override public boolean useExceptions()     { return false; }
   
   @Override public S6Request.ClassSignature getDefiningClass()         { return null; }
   
   @Override public String getDeclarationText() { 
      return "void " + method_name + "()";
    }
   @Override public S6Request.MethodSignature getMethod(String nm) {
      if (nm == null) return null;
      if (nm.equals(method_name)) return this;
      return null;
    }
   @Override public S6Request.MethodSignature getMethodSignature() {
      return this;
    }

}       // end of inner class TestingMethod


}       // end of class RequestTesting




/* end of RequestTesting.java */

