/********************************************************************************/
/*										*/
/*		search.js							*/
/*										*/
/*	Javascript code for use on the S6 search page				*/
/*										*/
/********************************************************************************/
/*	Copyright 2008 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2008, Brown University, Providence, RI.				 *
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
 *  BROWN widthRSITY DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS		 *
 *  SOFTWAwidthNCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND		 *
 *  FITNESS FOR ANY PARTICULAR PURPOSE.  IN NO EVENT SHALL BROWN UNIVERSITY	 *
 *  BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY 	 *
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,		 *
 *  WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS		 *
 *  ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE 	 *
 *  OF THIS SOFTWARE.								 *
 *										 *
 ********************************************************************************/



var nullerror = null;
var newerror = null;
var dofind = false;

var method_data = new Array();

var search_for = 'METHOD';




/********************************************************************************/
/*										*/
/*	Initialization methods							*/
/*										*/
/********************************************************************************/

function initializeSearch()
{
   handleSearchIn();
}



/********************************************************************************/
/*										*/
/*	Methods to validate the search locally					*/
/*										*/
/********************************************************************************/

function validateSearch()
{
   clearError();

   if (!options[OPTION_REMOTE] && !options[OPTION_LOCAL]) {
      addError("Select REMOTE or LOCAL");
    }

   var desc = document.getElementById("Keywords").value.trim();
   if (desc == "") {
      document.getElementById("KeywordLabel").style.color = "red";
      addError("Must provide keywords");
    }

   if (search_for == "METHOD") {
      var sts = finalMethodCheck(0);
      if (sts == "END" || sts == "IGN") addError("Must define method");
      else if (sts == "WAIT") {
	 setError("Waiting for validation");
	 dofind = true;
	 return false;
       }
      else if (sts == "OK") {
	 var ttot = 0;
	 for (var i = 1; i <= method_data[0].max_test; ++i) {
	   if (validateTest(0,i,false)) ++ttot;
	  }
	 if (ttot == 0) addError("No tests defined");
       }
    }
   else if (search_for == "CLASS") {
      var cdata = class_data;
      if (!cdata.name_ok) addError("Fix class name");
      var nmethd = 0;
      for (var i = 1; ; ++i) {
	 var mdata = method_data[i];
	 if (mdata == null) break;
	 var sts = finalMethodCheck(i);
	 if (sts == "WAIT") {
	    setError("Waiting for validataion");
	    dofind = true;
	    return false;
	  }
	 else if (sts == "OK") ++nmethd;
       }
      if (nmethd == 0) addError("Must define at least one method");
      else {
	 validateClassTests();
       }
    }

   if (reportError()) return false;

   if (search_for == "METHOD") {
      startMethodSearch();
    }
   else if (search_for == "CLASS") {
      startClassSearch();
    }

   return false;
}




function restartFind()
{
   dofind = false;
   var form = document.getElementById("FindForm");
   form.submit();
}



/********************************************************************************/
/*										*/
/*	Methods to handle error messages					*/
/*										*/
/********************************************************************************/

function addError(msg)
{
   if (newerror == null) newerror = "PLEASE FIX: ";
   else newerror += "; ";
   newerror += msg;
}



function clearError()
{
   setError("");
   newerror = null;
}


function reportError()
{
   if (newerror == null) return false;
   setError(newerror);
   newerror = null;
   return true;
}



function setError(msg)
{
   if (nullerror == null) {
      nullerror = getTextContent(document.getElementById("ErrorMessage"));
    }
   if (msg == null) msg = nullerror;

   setTextContent(document.getElementById("ErrorMessage"),msg);
}



/********************************************************************************/
/*										*/
/*	Methods to change what we are searching for				*/
/*										*/
/********************************************************************************/

function handleSearchFor()
{
   var type = document.getElementById("LookFor").value;
   search_for = type;
   if (type == "METHOD") {
      var div = document.getElementById("MethodDiv");
      div.style.display = 'block';
      div = document.getElementById("ClassDiv");
      div.style.display = 'none';
    }
   else if (type == 'CLASS') {
      var div = document.getElementById("MethodDiv");
      div.style.display = 'none';
      div = document.getElementById("ClassDiv");
      div.style.display = 'block';
    }
   else {
      document.getElementById("LookFor").selectedIndex = 0;
    }
}



function handleSearchIn()
{
   var val = document.getElementById("LookIn").value;
   options[OPTION_REMOTE] = (val == 'RL' || val == 'R');
   options[OPTION_LOCAL] = (val == 'RL' || val == 'L');
}




/********************************************************************************/
/*										*/
/*	Methods to check keywords						*/
/*										*/
/********************************************************************************/

function checkKeywords()
{
   var keys = document.getElementById("Keywords").value.trim();
   if (keys != "") {
      // reset error
      document.getElementById("KeywordLabel").style.color = "black";
    }
}



/********************************************************************************/
/*										*/
/*	Methods to check declarations						*/
/*										*/
/********************************************************************************/

var METHOD_UNCHECKED = 0;
var METHOD_UNUSED = 1;
var METHOD_CHECKING = 2;
var METHOD_BAD = 3;
var METHOD_OK = 4;



function MethodDecl(id)
{
   this.index = id;
   this.state = METHOD_UNCHECKED;
   this.request = new XMLHttpRequest();
   this.last_value = "";
   this.signature = null;
   this.error = null;
   this.tests = new Array();
   this.max_test = 1;
   this.tests_setup = false;
   this.name = null;
   this.is_static = false;
   this.request.onreadystatechange = function() {
      handleMethodDeclaration(id);
    }
}



function checkMethodDeclaration(id,async)
{
   var mdata = method_data[id];
   if (mdata == null) {
      mdata = new MethodDecl(id);
      method_data[id] = mdata;
    }

   var mid = "Mthddesc" + id;
   var x = document.getElementById(mid).value;
   if (x == "") {
      mdata.state = METHOD_UNUSED;
      return;
    }
   if (x == mdata.last_value) {
      if (mdata.state == METHOD_BAD) addError(mdata.error);
      return;
    }

   if (mdata.state == METHOD_CHECKING) {
      mdata.request.abort();
      mdata.state = METHOD_UNCHECKED;
    }

   var cls = "";
   if (search_for == "CLASS" && class_data.name_ok) cls = "&CLASS=" + class_data.name;

   mdata.request.open("GET","checkitem.php?METHOD=" + x + cls,async);
   mdata.state = METHOD_CHECKING;
   mdata.request.send();
}



function handleMethodDeclaration(id)
{
   var mdata = method_data[id];
   var emsg = null;

   if (mdata.request.readyState == 4 && mdata.request.status == 200) {
      var rslt = mdata.request.responseXML;
      if (rslt == null) emsg = "S6 Server not reachable";
      else {
	 var sgn = getXmlElement(rslt,"SIGNATURE");
	 if (sgn == null) {
	    var msg = getXmlElement(rslt,"PROBLEM");
	    if (msg == null) msg = getXmlElement(rslt,"ERROR");
	    if (msg == null) emsg = "S6 Server problem";
	    else emsg = msg.firstChild.nodeValue;
	  }
	 else {
	    var mm = getXmlElement(sgn,"METHOD");
	    var nmm = getXmlElement(mm,"NAME");
	    mdata.is_static = getXmlAttr(sgn,"STATIC");
	    mdata.name = getTextContent(nmm);
	    var txt = getXmlElement(sgn,"TEXT");
	    var str = getTextContent(txt);
	    var mid = "Mthddesc" + mdata.index;
	    document.getElementById(mid).value = str;
	    mdata.last_value = str;
	    mdata.signature = sgn;
	    mdata.state = METHOD_OK;
	    mdata.error = null;
	    emsg = null;
	  }
       }
    }
   else if (mdata.request.readyState == 4) {
      emsg = "S6 Server not responding: " + mdata.request.status;
    }

   var lblid;
   if (search_for == 'METHOD') lblid = "MthddescLabel" + id;
   else lblid = "MethodRow" + id;

   if (emsg != null) {
      mdata.state = METHOD_BAD;
      mdata.error = emsg;
      addError(emsg);
      if (search_for == 'METHOD') {
	 document.getElementById(lblid).style.color = "red";
	 clearTests();
       }
      else {
	 var elt = document.getElementById(lblid);
	 elt.style.backgroundColor = "red";
       }
    }
   else if (mdata.state == METHOD_OK) {
      if (search_for == 'METHOD') {
	 document.getElementById(lblid).style.color = "black";
	 setupMethodTests();
	 ensureEmptyTest(null);
       }
      else {
	 var elt = document.getElementById(lblid);
	 elt.style.backgroundColor = "transparent";
	 var foc = setupClassMethod(id+1);
	 if (focus != null) foc.focus();
	 if (id == 1) setupClassTests();
	 updateMethods();
       }
    }

   if (dofind) restartFind();
}



function setupMethodTests()
{
   var mdata = method_data[0];

   if (!mdata.tests_setup) {
      clearTests();
      mdata.tests_setup = true;
    }
}



function finalMethodCheck(id)
{
   checkMethodDeclaration(id,false);

   var mdata = method_data[id];

   if (mdata.state == METHOD_CHECKING) return "WAIT";
   if (mdata.state == METHOD_UNUSED) return "IGN"
   if (mdata.state == METHOD_BAD) return "BAD";

   return "OK";
}




/********************************************************************************/
/*										*/
/*	Methods to handle option buttons					*/
/*										*/
/********************************************************************************/

var OPTION_REMOTE = 0;
var OPTION_LOCAL = 1;

var options = new Array();

options[OPTION_REMOTE] = true;
options[OPTION_LOCAL] = true;


function handleOptionButton(btn)
{
   var elt = document.getElementById('OptionButton' + btn);
   var cls = elt.className;

   if (cls == 'buttonon') {
      elt.className = 'buttonoff';
      options[btn] = false;
    }
   else {
      elt.className = 'buttonon';
      options[btn] = true;
    }
}




/********************************************************************************/
/*										*/
/*	Methods to validate a Test case 					*/
/*										*/
/********************************************************************************/

function validateTest(mid,tid,async)
{
   var typ = document.getElementById('Test' + search_for + 'Type' + tid).value;

   if (typ == "CALL") return validateCallTest(mid,tid,async);

   return false;
}




/********************************************************************************/
/*										*/
/*	Methods for call tests							*/
/*										*/
/********************************************************************************/

var TEST_UNCHECKED = 0;
var TEST_UNUSED = 1;
var TEST_CHECKING = 2;
var TEST_BAD = 3;
var TEST_OK = 4;


function CallTestData(mid,tid)
{
   this.method = mid;
   this.index = tid;
   this.state = TEST_UNCHECKED;
   this.request = new XMLHttpRequest();
   this.last_input = "";
   this.last_output = "";
   this.last_op = "";
   this.test_case = null;
   this.error = null;
   this.request.onreadystatechange = function() {
      handleCallTestValidation(mid,tid);
    }
}



function validateCallTest(mid,tid,async)
{
   var arg = document.getElementById('Test' + search_for + 'In' + tid).value;
   var rslt = document.getElementById('Test' + search_for + 'Out' + tid).value;
   var op = document.getElementById('Test' + search_for + 'Op' + tid).value;

   var mdata = method_data[mid];
   if (mdata == null) return false;

   var tdata = mdata.tests[tid];
   if (tdata == null) {
      tdata = new CallTestData(mid,tid);
      mdata.tests[tid] = tdata;
    }

   if (arg == "" && rslt == "") {
      tdata.state = TEST_UNUSED;
      return false;
    }

   if (arg == tdata.last_input && rslt == tdata.last_output && op == tdata.last_op) {
      if (tdata.state == TEST_BAD) {
	 addError(tdata.error);
	 return false;
       }
      if (tdata.state == TEST_OK) return true;
      return false;
    }

   tdata.last_input = arg;
   tdata.last_output = rslt;
   tdata.last_op = op;
   var tname = "test_" + tid;

   tdata.request.open("POST","checkitem.php?CALLIN=" + arg + "&CALLOUT=" + rslt +
		     "&CALLOP=" + op + "&TESTNAME=" + tname,async);
   tdata.state = TEST_CHECKING;
   var mdoc = createDocument(mdata.signature);
   tdata.request.send(mdoc);

   return tdata.state == TEST_OK;
}



function handleCallTestValidation(mid,tid)
{
   var mdata = method_data[mid];
   var tdata = mdata.tests[tid];
   var emsg = null;
   var lblid = "Test" + search_for + "Row" + tid;

   if (tdata.request.readyState == 4 && tdata.request.status == 200) {
      var rslt = tdata.request.responseXML;
      if (rslt == null) emsg = "S6 Server not reachable";
      else {
	 var tcase = getXmlElement(rslt,'TESTCASE');
	 if (tcase == null) {
	    var msg = getXmlElement(rslt,'PROBLEM');
	    if (msg == null) msg = getXmlElement(rslt,'ERROR');
	    if (msg == null) emsg = "S6 Server problem";
	    else emsg = msg.firstChild.nodeValue;
	  }
	 else {
	    tdata.test_case = tcase;
	    tdata.state = TEST_OK;
	    tdata.error = null;
	    document.getElementById(lblid).style.backgroundColor = "transparent";
	  }
       }
    }
   else if (tdata.request.readyState == 4) {
      emsg = "S6 Server not responding for call: " + tdata.request.status;
    }

   if (emsg != null) {
      tdata.state = TEST_BAD;
      tdata.error = emsg;
      addError(emsg);
      document.getElementById(lblid).style.backgroundColor = "red";
    }

   if (dofind) restartFind();
}





/* end of search.js */

