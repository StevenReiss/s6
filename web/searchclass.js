/********************************************************************************/
/*										*/
/*		searchclass.js							*/
/*										*/
/*	Javascript code for generating class tests				*/
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



/********************************************************************************/
/*										*/
/*	Global variables							*/
/*										*/
/********************************************************************************/

var class_data = null;

var class_name_regex = new RegExp("^\\w*$");



/********************************************************************************/
/*										*/
/*	Methods to provide space for methods					*/
/*										*/
/********************************************************************************/

function checkClassName()
{
   var lbl = document.getElementById('ClassNameLabel');

   var val = document.getElementById('ClassName').value.trim();
   if (val == null) return;

   if (class_data == null) {
      class_data = new ClassDecl();
    }

   if (val.search(class_name_regex) == -1) {
      lbl.style.color = 'red';
      class_data.name_ok = false;
    }
   else {
      lbl.style.color = 'black';
      class_data.name_ok = true;
      class_data.name = val;
      addClassMethod(1);
    }
}



/********************************************************************************/
/*										*/
/*	Class Declaration definitions						*/
/*										*/
/********************************************************************************/

function ClassDecl()
{
   this.name = null;
   this.name_ok = false;
   this.error = null;
   this.tests = new Array();
   this.max_test = 1;
   this.methods_setup = false;
   this.tests_setup = false;

   this.state = TEST_UNCHECKED;
   this.request = new XMLHttpRequest();
   this.test_cases = null;
   this.error = null;
   var x = this;
   this.request.onreadystatechange = function() {
      handleClassTestValidation(x);
    }
}




/********************************************************************************/
/*										*/
/*	Methods to handle class methods 					*/
/*										*/
/********************************************************************************/

function addClassMethod(mid)
{
   if (!class_data.methods_setup) {
      var mtbl = document.getElementById("ClassMethods");
      while (mtbl.childNodes.length > 0) {
	 mtbl.removeChild(mtbl.childNodes[0]);
       }
      class_data.methods_setup = true;
    }

   var mrow = document.getElementById('Mthddesc' + mid);
   if (mrow != null) return;

   var focus = setupClassMethod(mid);

   if (mid == 1 && focus != null) focus.focus();
}




function setupClassMethod(mid)
{
   var focus = null;
   var mdata = method_data[mid];
   if (mdata == null) {
      mdata = new MethodDecl(mid);
      method_data[mid] = mdata;
    }

   mrow = document.getElementById('MethodRow' + mid);
   if (mrow == null) {
      var tbl = document.getElementById('ClassMethods');
      mrow = tbl.insertRow(mid-1);
      mrow.id = 'MethodRow' + mid;
      mrow.classname = 'methodrow';
      var td = mrow.insertCell(0);
      var tarea = createClassMethodArea(mid);
      focus = tarea;
      td.appendChild(tarea);
    }

   return focus;
}



function createClassMethodArea(mid)
{
   var tdin = document.createElement("INPUT");
   tdin.type = 'text';
   tdin.name = 'Mthddesc' + mid;
   tdin.id = 'Mthddesc' + mid;
   tdin.size = 64;
   tdin.maxlength = 256;
   addListener(tdin,"change",function() { checkClassMethod(mid,true); });
   return tdin;
}




function checkClassMethod(mid,async)
{
   checkMethodDeclaration(mid,async);
}


function getMethodSelections(first,vars)
{
   var mthd = new Array();
   var ct = 0;
   mthd[ct++] = ['none','--NONE--'];

   for (var i = 1; ; ++i) {
      var mdata = method_data[i];
      if (mdata == null) break;
      if (mdata.name != null && mdata.name != "") {
	 if (mdata.is_static || mdata.name == "<init>") {
	    var e = new Array();
	    e[0] = mdata.name;
	    e[1] = mdata.name;
	    mthd[ct++] = e;
	  }
	 else if (vars != null) {
	    for (var j = 0; j < vars.length; ++j) {
	       var e = new Array();
	       e[0] = vars[j] + "." + mdata.name;
	       e[1] = e[0];
	       mthd[ct++] = e;
	     }
	  }
       }
    }

   return mthd;
}



/********************************************************************************/
/*										*/
/*	Routines to handle class tests						*/
/*										*/
/********************************************************************************/

function setupClassTests()
{
   if (!class_data.tests_setup) {
      clearTests();
      class_data.tests_setup = true;
    }

   ensureEmptyTest(null);
}



function addClassTest(tid)
{
   var trow = document.getElementById('TestClass' + 'Row' + tid);
   if (trow != null) return;

   var focus = setupClassTest(tid);
   if (tid == 1 && focus != null) focus.focus();
}




/********************************************************************************/
/*										*/
/*	Methods to handle class test validation 				*/
/*										*/
/********************************************************************************/

function validateClassTests()
{
   var tdoc = createNewDocument("S6:CHECK");
   var tests = tdoc.documentElement;
   tests.setAttribute("WHAT","CLASSTESTS");

   var sign = createClassSignature(tdoc);
   tests.appendChild(sign);

   var ntest = 0;
   var ttbl = document.getElementById(search_for + "TestTableBody");
   for (trow = ttbl.firstChild; trow != null; trow = trow.nextSibling) {
      trow.style.backgroundColor = "transparent";
      if (checkForUsedTest(trow)) {
	 var tid = trow.test_index;
	 var typ = getTestType(tid);
	 var tmthd = getTestMethod(tid);
	 var tin = getTestInput(tid);
	 var top = getTestOp(tid);
	 var tout = getTestOutput(tid);
	 var telt = tdoc.createElement("TEST");
	 tests.appendChild(telt);
	 telt.setAttribute("TESTID",tid);
	 telt.setAttribute("TESTNAME","test_" + tid);
	 telt.setAttribute("TYPE",typ);
	 telt.setAttribute("METHOD",tmthd);
	 telt.setAttribute("OP",top);
	 if (tin != '') {
	    var inf = tdoc.createElement("INPUT");
	    setTextContent(inf,tin);
	    telt.appendChild(inf);
	  }
	 if (tout != '') {
	    var otf = tdoc.createElement("OUTPUT");
	    setTextContent(otf,tout);
	    telt.appendChild(otf);
	  }
	 ++ntest;
       }
    }

   if (ntest == 0) addError("No tests defined");

   var ctd = class_data;
   ctd.request.open("POST","checkitem.php?CLASSTESTS=1",false);
   ctd.state = TEST_CHECKING;
   ctd.request.send(tdoc);

   return ctd.state == TEST_OK;
}



function handleClassTestValidation(ctd)
{
   if (ctd.request.readyState == 4 && ctd.request.status == 200) {
      var rslt = ctd.request.responseXML;
      if (rslt == null) emsg = "S6 server error for class tests";
      else {
	 var errs = getXmlElements(rslt,"ERROR");
	 if (errs.length == 0) {
	    ctd.test_cases = getXmlElement(rslt,"TESTS");
	    ctd.state = TEST_OK;
	  }
	 else {
	    for (var i = 0; i < errs.length; ++i) {
	       var eid = getXmlAttr(err[i],"TESTID");
	       var emsg = getXmlAttr(err[i],"MESSAGE");
	       addError(emsg);
	       var tlbl = "Test" + search_for + "Row" + tid;
	       document.getElementById(tlbl).style.backgroundColor = "red";
	     }
	    ctd.state = TEST_BAD;
	  }
       }
    }
   else if (ctd.request.readyState == 4) {
      emsg = "S6 server not responding for class tests: " + ctd.request.status;
    }

   if (emsg != null) {
      ctd.state = TEST_BAD;
      ctd.error = emsg;
      addError(emsg);
    }

   if (dofind) restartFind();
}




function createClassSignature(tdoc)
{
   var sign = tdoc.createElement("S6:SIGNATURE");
   var csgn = tdoc.createElement("S6:CLASS");
   csgn.setAttribute("NAME",class_data.name);
   sign.appendChild(csgn);

   for (var i = 1; ; ++i) {
      var mdata = method_data[i];
      if (mdata == null) break;
      if (mdata.state == METHOD_OK) {
	 var x = getXmlElement(mdata.signature,"METHOD");
	 addXmlNode(tdoc,csgn,x);
       }
    }

   return sign;
}



/* end of searchclass.js */
