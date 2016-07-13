/********************************************************************************/
/*										*/
/*		RunnerAssert.java						*/
/*										*/
/*	Implemention of JUnit ASSERT class for partial testing			*/
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
/* Derived from junit source */


package edu.brown.cs.s6.runner;

import java.util.Arrays;

import org.hamcrest.Matcher;


public class RunnerAssert {


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private static int		num_fail = 0;
private static int		num_success = 0;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

protected RunnerAssert() { }




/********************************************************************************/
/*										*/
/*	Assertion methods							*/
/*										*/
/********************************************************************************/

static public void assertTrue(String message, boolean condition)
{
   if (!condition) fail();
   else succeed();
}


static public void assertTrue(boolean condition)
{
   assertTrue(null, condition);
}


static public void assertFalse(String message, boolean condition)
{
   assertTrue(message, !condition);
}


static public void assertFalse(boolean condition)
{
   assertFalse(null, condition);
}

static public void fail(String message)
{
   fail();
}


static public void assertEquals(String message, Object expected, Object actual)
{
   if (expected == null && actual == null) succeed();
   else if (expected != null && expected.equals(actual)) succeed();
   else if (expected instanceof String && actual instanceof String) {
      failCompare(expected.toString(),actual.toString());
    }
   else
      fail();
}


static public void assertEquals(Object expected, Object actual)
{
   assertEquals(null, expected, actual);
}



public static void assertArrayEquals(String message, Object[] expecteds,
      Object[] actuals)
{
   if (Arrays.deepEquals(expecteds,actuals)) succeed();
   else fail(message);
}


public static void assertArrayEquals(Object[] expecteds, Object[] actuals)
{
   assertArrayEquals(null, expecteds, actuals);
}


public static void assertArrayEquals(String message, byte[] expecteds,
      byte[] actuals)
{
   if (Arrays.equals(expecteds,actuals)) succeed();
   else fail(message);
}


public static void assertArrayEquals(byte[] expecteds, byte[] actuals)
{
   assertArrayEquals(null, expecteds, actuals);
}


public static void assertArrayEquals(String message, char[] expecteds,
      char[] actuals)
{
   if (Arrays.equals(expecteds,actuals)) succeed();
   else fail(message);
}


public static void assertArrayEquals(char[] expecteds, char[] actuals)
{
   assertArrayEquals(null, expecteds, actuals);
}


public static void assertArrayEquals(String message, short[] expecteds,
      short[] actuals)
{
   if (Arrays.equals(expecteds,actuals)) succeed();
   else fail(message);
}


public static void assertArrayEquals(short[] expecteds, short[] actuals)
{
   assertArrayEquals(null, expecteds, actuals);
}



public static void assertArrayEquals(String message, int[] expecteds,
      int[] actuals)
{
   if (Arrays.equals(expecteds,actuals)) succeed();
   else fail(message);
}


public static void assertArrayEquals(int[] expecteds, int[] actuals)
{
   assertArrayEquals(null, expecteds, actuals);
}


public static void assertArrayEquals(String message, long[] expecteds,
      long[] actuals)
{
   if (Arrays.equals(expecteds,actuals)) succeed();
   else fail(message);
}


public static void assertArrayEquals(long[] expecteds, long[] actuals)
{
   assertArrayEquals(null, expecteds, actuals);
}


public static void assertArrayEquals(String message, double[] expecteds,
      double[] actuals, double delta)
{
   boolean ok = true;
   if (expecteds == null) ok = (actuals == null);
   else if (actuals == null) ok = false;
   else if (expecteds.length != actuals.length) ok = false;
   else {
      for (int i = 0; i < expecteds.length; ++i) {
	 if (expecteds[i] != actuals[i] && Math.abs(expecteds[i] - actuals[i]) > delta) {
	    ok = false;
	    break;
	  }
       }
    }

   if (ok) succeed();
   else fail(message);
}


public static void assertArrayEquals(double[] expecteds, double[] actuals, double delta)
{
   assertArrayEquals(null, expecteds, actuals, delta);
}


public static void assertArrayEquals(String message, float[] expecteds,
      float[] actuals, double delta)
{
   boolean ok = true;
   if (expecteds == null) ok = (actuals == null);
   else if (actuals == null) ok = false;
   else if (expecteds.length != actuals.length) ok = false;
   else {
      for (int i = 0; i < expecteds.length; ++i) {
	 if (expecteds[i] != actuals[i] && Math.abs(expecteds[i] - actuals[i]) > delta) {
	    ok = false;
	    break;
	  }
       }
    }

   if (ok) succeed();
   else fail(message);
}



public static void assertArrayEquals(float[] expecteds, float[] actuals, float delta)
{
   assertArrayEquals(null, expecteds, actuals, delta);
}



static public void assertEquals(String message, double expected,
      double actual, double delta)
{
   if (Double.compare(expected, actual) == 0) return;
   if (!(Math.abs(expected - actual) <= delta)) failCompare(expected,actual);
   else succeed();
}


static public void assertEquals(long expected, long actual)
{
   assertEquals(null, expected, actual);
}


static public void assertEquals(String message, long expected, long actual)
{
   if (expected != actual) failCompare(expected,actual);
   else succeed();
}


static public void assertEquals(double expected, double actual)
{
   assertEquals(null, expected, actual);
}


static public void assertEquals(String message, double expected,
      double actual)
{
   assertEquals(message,expected,actual,0.00001);
}


static public void assertEquals(double expected, double actual, double delta)
{
   assertEquals(null, expected, actual, delta);
}


static public void assertNotNull(String message, Object object)
{
   assertTrue(message, object != null);
}


static public void assertNotNull(Object object)
{
   assertNotNull(null, object);
}


static public void assertNull(String message, Object object)
{
   assertTrue(message, object == null);
}


static public void assertNull(Object object) {
   assertNull(null, object);
}


static public void assertSame(String message, Object expected, Object actual)
{
   if (expected != actual) {
      if (expected instanceof String && actual instanceof String)
	 failCompare(expected.toString(), actual.toString());
      else fail();
    }
   else succeed();
}


static public void assertSame(Object expected, Object actual)
{
   assertSame(null, expected, actual);
}


static public void assertNotSame(String message, Object unexpected, Object actual)
{
   if (unexpected == actual) fail();
   else succeed();
}


static public void assertNotSame(Object unexpected, Object actual)
{
   assertNotSame(null, unexpected, actual);
}



public static void assertEquals(String message, Object[] expecteds,
      Object[] actuals)
{
   assertArrayEquals(message, expecteds, actuals);
}


public static void assertEquals(Object[] expecteds, Object[] actuals)
{
   assertArrayEquals(expecteds, actuals);
}


public static <T> void assertThat(T actual, Matcher<T> matcher)
{
   assertThat("", actual, matcher);
}


public static <T> void assertThat(String reason, T actual, Matcher<T> matcher)
{
   if (!matcher.matches(actual)) fail();
   else succeed();
}



/********************************************************************************/
/*										*/
/*	Record success and failure						*/
/*										*/
/********************************************************************************/

static public void fail()
{
   ++num_fail;
}


static public void failDefault()
{
   fail();
}

static public void succeed()
{
   ++num_success;
}


static private void failCompare(String expect,String actual)
{
   // System.err.println("FAIL COMPARE " + expect + " " + actual);
   fail();
}

static private void failCompare(double expect,double actual)
{
   // System.err.println("FAIL COMPARE " + expect + " " + actual);
   fail();
}




/********************************************************************************/
/*										*/
/*	Report status								*/
/*										*/
/********************************************************************************/

static public void checkUseForTesting()
{
   // System.err.println("CHECK USE FOR TESTING " + num_success + " " + num_fail);

   if (num_success == 0) throw new AssertionError("No test passed");

// if (num_fail > 5*num_success)
// if (num_fail > num_success)
   if (num_fail > (num_success+4)/5)
      throw new AssertionError("Too many failures");

   throw new AssertionError("S6TestCount: " + num_success  + " " + num_fail);
}




}	// end of class RunnerAssert


/* end of RunnerAssert.java */
