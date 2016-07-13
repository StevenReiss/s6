/********************************************************************************/
/*										*/
/*		searchtest.js							*/
/*										*/
/*	Javascript code for generating test case tables 			*/
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



var	test_count = 0;



/********************************************************************************/
/*										*/
/*	Top level test case methods						*/
/*										*/
/********************************************************************************/

function clearTests()
{
   var lbl = search_for + "TestTableBody";
   var ttbl = document.getElementById(lbl);

   while (ttbl.childNodes.length > 0) {
      ttbl.removeChild(ttbl.childNodes[0]);
    }
}



function ensureEmptyTest(elt)
{
   var lbl = search_for + "TestTableBody";
   var ttbl = document.getElementById(lbl);
   var lrow = ttbl.lastChild;
   var focus = null;

   if (lrow == null || !isEmptyLastTest(lrow)) {
      var tid = createNewTest();
      if (search_for == "METHOD") {
	 focus = setupCallTest(tid);
       }
      else {
	 setupCallSetTest(tid,false);
       }
    }

   if (elt != null) {
      var tid = elt.test_index;
      var typ = getTestType(tid);
      if (typ == "CALLSET" || typ == "CALLSETCONT") {
	 var nrow = elt.nextSibling;
	 if (nrow == null || getTestType(nrow.test_index) != 'CALLSETCONT') {
	    var ntid = createNewTest(nrow);
	    setupCallSetTest(ntid,true);
	  }
       }
    }

   if (lrow == null && search_for == 'METHOD') {
      if (focus != null) focus.focus();
    }
}



/********************************************************************************/
/*										*/
/*	Utility methods for test management					*/
/*										*/
/********************************************************************************/

function createNewTest(before)
{
   var lbl = search_for + "TestTableBody";
   var ttbl = document.getElementById(lbl);

   var idx = 0;
   if (before == null) idx = ttbl.rows.length;
   else {
      for (idx = 0; idx < ttbl.rows.length; ++idx) {
	 if (before == ttbl.rows[idx]) break;
       }
    }

   var trow = ttbl.insertRow(idx);
   var tid = ++test_count;

   trow.id = "Test" + search_for + "Row" + tid;
   trow.className = "tablerow";
   trow.test_index = tid;

   return tid;
}




function getTestType(tid)
{
   var tsel = document.getElementById("Test" + search_for + "Type" + tid);

   if (tsel == null) return 'NONE';

   return tsel.value;
}




function getTestOp(tid)
{
   var tsel = document.getElementById("Test" + search_for + "Op" + tid);

   if (tsel == null) return 'NONE';

   return tsel.value;
}




function getTestOutput(tid)
{
   var tsel = document.getElementById("Test" + search_for + "Out" + tid);

   if (tsel == null) return '';

   return tsel.value.trim();
}




function getTestInput(tid)
{
   var tsel = document.getElementById("Test" + search_for + "In" + tid);

   if (tsel == null) return '';

   return tsel.value.trim();
}




function getTestMethod(tid)
{
   var tsel = document.getElementById("Test" + search_for + "Mthd" + tid);

   if (tsel == null) return '';

   return tsel.value;
}




function isEmptyLastTest(elt)
{
   var tid = elt.test_index;
   var trow = document.getElementById("Test" + search_for + "Row" + tid);
   if (trow == null) return false;
   var typ = getTestType(tid);
   if (typ == 'CALLSETCONT') return false;

   return !checkForUsedTest(elt);
}



/********************************************************************************/
/*										*/
/*	Methods to handle callbacks to change test type 			*/
/*										*/
/********************************************************************************/

function resetTest(elt)
{
   var tid = elt.test_index;
   var trow = document.getElementById("Test" + search_for + "Row" + tid);
   if (trow == null) return;
   var typ = getTestType(tid);
   var focus = null;

   if (typ == 'CALL') {
      focus = setupCallTest(tid);
    }
   else if (typ == 'USERCODE') {
      focus = setupUserCodeTest(tid);
    }
   else if (typ == 'TESTFILE') {
      focus = setupTestFileTest(tid);
    }
   else if (typ == 'CLASSCALL') {
      focus = setupClassCallTest(tid);
    }
   else if (typ == 'CALLSET') {
      focus = setupCallSetTest(tid,false);
    }
   else if (typ == 'CALLSETCONT') {
      focus = setupCallSetTest(tid,true);
    }
   else {
      focus = setupCallTest(tid);
    }

   return focus
}




/********************************************************************************/
/*										*/
/*	Manage a CALL test							*/
/*										*/
/********************************************************************************/

function setupCallTest(tid)
{
   var focus = null;

   var trow = document.getElementById('Test' + search_for + 'Row' + tid);

   while (trow.cells.length > 0) trow.deleteCell(0);

   var td = trow.insertCell(0);
   td.colSpan = 2;

   td.appendChild(document.createTextNode("("));
   var tarea = createTextArea(tid,'In',30,'Method Arguments');
   focus = tarea;
   td.appendChild(tarea);
   td.appendChild(document.createTextNode(")"));

   td = trow.insertCell(1);
   var tsel = createSelection(tid,'Op','eql',[['eql','=='],['neq','!=']]);
   td.appendChild(tsel);

   td = trow.insertCell(2);
   td.appendChild(createTextArea(tid,'Out',20,'Method Result'));

   td = trow.insertCell(3);
   tsel = createTestSelection(tid,'CALL');
   td.appendChild(tsel);

   return focus;
}




/********************************************************************************/
/*										*/
/*	Manage a USER CODE test 						*/
/*										*/
/********************************************************************************/

function setupUserCodeTest(tid)
{
   var trow = document.getElementById('Test' + search_for + 'Row' + tid);

   while (trow.cells.length > 0) trow.deleteCell(0);

   var td = trow.insertCell(0);
   td.colSpan = 4;
   var focus = createTextRegion(tid,'Out',72,4);
   td.appendChild(focus);

   td = trow.insertCell(1);
   tsel = createTestSelection(tid,'USERCODE');
   td.appendChild(tsel);

   return focus;
}




/********************************************************************************/
/*										*/
/*	Manage a TEST FILE test 						*/
/*										*/
/********************************************************************************/

function setupTestFileTest(tid)
{
   var trow = document.getElementById('Test' + search_for + 'Row' + tid);

   while (trow.cells.length > 0) trow.deleteCell(0);

   var td = trow.insertCell(0);
   td.colSpan = 4;
   var focus = createFileRegion(tid,'Out',48,'Test file name');
   td.appendChild(focus);

   td = trow.insertCell(1);
   tsel = createTestSelection(tid,'TESTFILE');
   td.appendChild(tsel);

   return focus;
}




/********************************************************************************/
/*										*/
/*	Manage a CLASS CALL test						*/
/*										*/
/********************************************************************************/

function setupClassCallTest(tid)
{
   var focus = null;

   var trow = document.getElementById('Test' + search_for + 'Row' + tid);

   while (trow.cells.length > 0) trow.deleteCell(0);

   var td = trow.insertCell(0);
   var msel = createSelection(tid,'Mthd','',getMethodSelections(true,null));
   td.appendChild(msel);

   var td = trow.insertCell(1);
   td.appendChild(document.createTextNode("("));
   var tarea = createTextArea(tid,'In',30,'Method Arguments');
   focus = tarea;
   td.appendChild(tarea);
   td.appendChild(document.createTextNode(")"));

   td = trow.insertCell(2);
   var tsel = createSelection(tid,'Op','eql',[['eql','=='],['neq','!=']]);
   td.appendChild(tsel);

   td = trow.insertCell(3);
   td.appendChild(createTextArea(tid,'Out',20,'Method Result'));

   td = trow.insertCell(4);
   tsel = createTestSelection(tid,'CLASSCALL');
   td.appendChild(tsel);

   return focus;
}




/********************************************************************************/
/*										*/
/*	Manage a CALL SET or CALL SET CONTINUED test				*/
/*										*/
/********************************************************************************/

function setupCallSetTest(tid,cont)
{
   var focus = null;

   var trow = document.getElementById('Test' + search_for + 'Row' + tid);

   trow.test_index = tid;
   while (trow.cells.length > 0) trow.deleteCell(0);

   var td = trow.insertCell(0);
   var msel = createSelection(tid,'Mthd','',getTestMethods(tid));
   addListener(msel,'change',function() { checkCallSet(trow,1); });
   td.appendChild(msel);

   var td = trow.insertCell(1);
   td.appendChild(document.createTextNode("("));
   var tarea = createTextArea(tid,'In',30,'Method Arguments');
   addListener(tarea,"change",function() { checkCallSet(trow,1); });
   focus = tarea;
   td.appendChild(tarea);
   td.appendChild(document.createTextNode(")"));

   td = trow.insertCell(2);
   var dflt = (cont ? 'eql' : 'save');
   var tsel = createSelection(tid,'Op',dflt,[['save','==>'],
				 ['eql','=='],['neq','!='],['ignore','???']]);
   addListener(tsel,"change",function() { checkCallSet(trow,1); });
   td.appendChild(tsel);

   td = trow.insertCell(3);
   tarea = createTextArea(tid,'Out',20,'Method Result');
   addListener(tarea,"change",function() { checkCallSet(trow,1); });
   td.appendChild(tarea);

   if (!cont) {
      td = trow.insertCell(4);
      td.rowSpan = 1;
      td.vAlign = 'center';
      tsel = createTestSelection(tid,'CALLSET');
      td.appendChild(tsel);
    }
   else {
      var hin = createHiddenValue(tid,'Type','CALLSETCONT');
      td.appendChild(hin);
      for (var x = trow.previousSibling; x != null; x = x.previousSibling) {
	 if (getTestType(x.test_index) == 'CALLSET') {
	    var y = x.cells[4];
	    y.rowSpan = y.rowSpan + 1;
	    break;
	  }
       }
    }

   return focus;
}



function checkCallSet(elt,id)
{
   var tid = elt.test_index;

   ensureEmptyTest(elt);

   var op = getTestOp(tid);
   if (op == 'save' && getTestOutput(tid) != "") updateMethods();
}



function updateMethods()
{
   for (var i = 0; i <= test_count; ++i) {
      var tsel = document.getElementById('Test' + search_for + 'Mthd' + i);
      if (tsel != null) {
	 setupSelectOptions(tsel,tsel.value,getTestMethods(i));
       }
    }
}



function getTestMethods(tid)
{
   var first = false;
   var vars = new Array();
   var typ = getTestType(tid);

   if (typ == 'CALLSET' || typ == 'CLASSCALL') {
      first = true;
    }
   else {
      var trow = document.getElementById('Test' + search_for + 'Row' + tid);
      for (var tprv = trow.previousSibling; tprv != null; tprv = tprv.previousSibling) {
	 var pid = tprv.test_index;
	 var pop = getTestOp(pid);
	 if (pop == 'save') {
	    // check if the method called here returns a value of this type
	    var pout = getTestOutput(pid);
	    if (pout != null && pout != "") vars.push(getTestOutput(pid));
	  }
       }
    }

   return getMethodSelections(first,vars);
}



/********************************************************************************/
/*										*/
/*	Test field creation methods						*/
/*										*/
/********************************************************************************/

function createTextArea(tid,lbl,size,alt)
{
   var tdin = document.createElement("INPUT");
   tdin.type = 'text';
   tdin.id = 'Test' + search_for + lbl + tid;
   tdin.size = size;
   tdin.maxlength = 256;
   tdin.alt = alt;
   tdin.test_index = tid;

   addListener(tdin,"blur",function() { checkTest(tdin); });

   return tdin;
}



function createTextRegion(tid,lbl,wid,ht)
{
   var tdin = document.createElement("TEXTAREA");
   tdin.id = 'Test' + search_for + lbl + tid;
   tdin.cols = wid;
   tdin.rows = ht;
   tdin.test_index = tid;
   addListener(tdin,"blur",function() { checkTest(tdin); });
   return tdin;
}



function createFileRegion(tid,lbl,size,alt)
{
   var tdin = document.createElement("INPUT");
   tdin.type = 'file';
   tdin.id = 'Test' + search_for + lbl + tid;
   tdin.size = size;
   tdin.maxlength = 256;
   tdin.alt = alt;
   tdin.test_index = tid;
   addListener(tdin,"blur",function() { checkTest(tdin); });
   return tdin;
}




function createHiddenValue(tid,lbl,val)
{
   var tdin = document.createElement("INPUT");
   tdin.type = 'hidden';
   tdin.value = val;
   tdin.id = 'Test' + search_for + lbl + tid;
   return tdin;
}



function createTestSelection(tid,dflt)
{
   var tsel;

   if (search_for == "METHOD") {
      tsel = createSelection(tid,'Type',dflt,
				[['CALL','CALL'],
				['USERCODE','USER CODE'],['TESTFILE','TEST FILE']]);
    }
   else {
      tsel = createSelection(tid,'Type',dflt,
				[['CALLSET','CALL SET'],
				['CLASSCALL','CALL'],
				['USERCODE','USER CODE'],['TESTFILE','TEST FILE']]);
    }

   addListener(tsel,"change",function() { resetTest(tsel); });

   return tsel;
}



function createSelection(tid,lbl,dflt,opts)
{
   tsel = document.createElement("SELECT");
   tsel.id = 'Test' + search_for + lbl + tid;
   tsel.size = 1;
   tsel.test_index = tid;

   setupSelectOptions(tsel,dflt,opts);

   return tsel;
}




function setupSelectOptions(tsel,dflt,opts)
{
   while (tsel.options.length > 0) tsel.remove(0);

   sel = 0;
   for (var i = 0; i < opts.length; ++i) {
      var topt = document.createElement("OPTION");
      var key = opts[i][0];
      var val = opts[i][1];
      if (key == dflt) {
	 topt.defaultselected = true;
	 sel = i;
       }
      topt.value = key;
      setTextContent(topt,val);
      tsel.appendChild(topt);
    }
   tsel.selectedIndex = sel;
   tsel.value = opts[sel][0];
}




/********************************************************************************/
/*										*/
/*	Methods to Check a test case after it changes or loses focus		*/
/*										*/
/********************************************************************************/

function checkTest(elt)
{
   var tid = elt.test_index;
   var relt = document.getElementById('Test' + search_for + 'Row' + tid);
   var fg = checkForUsedTest(relt);

   if (fg) {
      var tid = relt.test_index;
      if (search_for == 'METHOD') {
	 var mdata = method_data[0];
	 if (mdata == null) return;
	 if (mdata.max_test < tid+1) mdata.max_test = tid+1;
       }
      else {
	 if (class_data == null) return;
	 if (class_data.max_test < tid+1) class_data.max_test = tid+1;
       }

      ensureEmptyTest(relt);
    }
}



/********************************************************************************/
/*										*/
/*	Methods to check if a test is used at all				*/
/*										*/
/********************************************************************************/

function checkForUsedTest(elt)
{
   var tid = elt.test_index;
   var type = getTestType(tid);
   var fg = false;

   if (type == "CALL") fg = checkCallTest(tid);
   else if (type == "USERCODE") fg = checkUserCodeTest(tid);
   else if (type == "TESTFILE") fg = checkTestFileTest(tid);
   else if (type == "CLASSCALL") fg = checkCallTest(tid);
   else if (type == "CALLSET") fg = checkCallSetTest(tid);
   else if (type == "CALLSETCONT") fg = checkCallSetTest(tid);

   return fg;
}



function checkCallTest(tid)
{
   var arg = getTestInput(tid);
   var rslt = getTestOutput(tid);
   var op = getTestOp(tid);
   if (arg == "" && rslt == "") return false;
   return true;
}




function checkUserCodeTest(tid)
{
   var rslt = getTestOuptut(tid);
   if (rslt == "") return false;
   return true;
}




function checkTestFileTest(tid)
{
   var rslt = getTestOutput(tid);
   if (rslt == "") return false;
   return true;
}



function checkCallSetTest(tid)
{
   var mthd = getTestMethod(tid);
   var arg = getTestInput(tid);
   var rslt = getTestOutput(tid);
   var op = getTestOp(tid);
   if (mthd == 'none' && arg == "" && rslt == "") return false;

   return true;
}



/* end of searchtest.js */

