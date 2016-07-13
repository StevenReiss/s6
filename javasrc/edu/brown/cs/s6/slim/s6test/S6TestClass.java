package edu.brown.cs.s6.slim.s6test;

import javax.swing.JOptionPane;

import junit.framework.TestCase;



public class S6TestClass extends TestCase {

static edu.brown.cs.s6.slim.SlimSecurityPolicy s6_Test_Policy = new edu.brown.cs.s6.slim.SlimSecurityPolicy("/tmp/s6test/s6test_65241");
static edu.brown.cs.s6.slim.SlimPermission[] s6_Test_Permissions = new edu.brown.cs.s6.slim.SlimPermission[] {
new edu.brown.cs.s6.slim.SlimAwtPermission(null),
new edu.brown.cs.s6.slim.SlimFilePermission("*","read,write,execute,delete")
};



private static class S6UserClass {

private static final int M=1000, C=100, X=10;

/**
 * Convert a four digit year to its equivalent as a Roman number.
 * @param yearStr, a String object containing digits
 * @return a String object with Roman numbers of value equivalent to
 * yearStr
 */
public String convertToRoman(String yearStr){
  int year=Integer.parseInt(yearStr);
  String roman=convert(year);
  year%=M;
  roman+=convertToRoman(year,M,MDC);
  year%=C;
  roman+=convertToRoman(year,C,CLX);
  year%=X;
  roman+=convertToRoman(year,X,XVI);
  return roman;
}

/**
 * Find the Roman numeral for year using values tenV and a String object
 * str.
 * @param year, an integer year
 * @param tenV, an integer multiple of ten, one of M, C or X
 * @param str, a String object of three successive Roman numerals, one of
 * MDC, CLX, XVI
 * @return a String object of Roman numerals required to represent year
 * with the characters in str
 */
private String convertToRoman(int year,int tenV,String str){
  String ans="", tenS="" + str.charAt(0), fiveS="" + str.charAt(1), unitS="" + str.charAt(2);
  int fiveV=tenV / 2, unitV=tenV / X;
  if (year >= tenV - unitV) {
    ans=unitS + tenS;
    year-=tenV - unitV;
  }
 else	if (year >= fiveV) {
    ans=fiveS;
    year-=fiveV;
  }
 else	if (year >= fiveV - unitV) {
    ans+=unitS + fiveS;
    year-=fiveV - unitV;
  }
  int units=year / unitV;
  if (units == 3)   ans+=unitS + unitS + unitS;
 else	if (units == 2)   ans+=unitS + unitS;
 else	if (units == 1)   ans+=unitS;
  return ans;
}

private static final String YEAR_PROMPT="Enter a four digit year.", MDC="MDC", CLX="CLX", XVI="XVI";

/**
 * Construct a RomanConverter object.
 * Ask client for a year.  COnvert to its Roman numeral equivalent.
 * Display the result.
 */
public S6UserClass(){
  String yearStr=JOptionPane.showInputDialog(null,YEAR_PROMPT);
  String romanStr=convertToRoman(yearStr);
  JOptionPane.showMessageDialog(null,yearStr + " = " + romanStr);
}


/**
 * Set the number of Ms in a four digit year.
 * @param year, an integer year
 * @return a String object with the number of Ms equal to the thousands
 * digit of year
 */
private String convert(int year){
  String ans="";
  int ms=year / M;
  if (ms >= 5) {
    ans="MMMMM";
    ms-=5;
  }
  if (ms >= 3) {
    ans+="MMM";
    ms-=3;
  }
  if (ms == 2)	 ans+="MM";
 else	if (ms == 1)   ans+="M";
  return ans;
}

}	//end of class S6UserClass




public void test_SVIWEB_1() throws Exception
{
try {
s6_Test_Policy.runMethod(s6_Test_Permissions,
new java.security.PrivilegedExceptionAction<java.lang.Object>() {
public java.lang.Object run() throws Exception {
S6UserClass __object = new S6UserClass();
assertEquals("Result of call","XVII",__object.convert(17));
return null;
} } ); }
catch (SecurityException t) { fail("Security failure: " + t.toString()); }}



// static private void failEquals(String message, Object expected, Object actual)
// {
   // String formatted= "";
   // if (message != null) formatted= message+" ";
   // fail(formatted+"expected:<"+expected+"> but was:<"+actual+">");
// }



}
