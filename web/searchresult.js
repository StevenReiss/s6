/********************************************************************************/
/*										*/
/*		searchresult.js 						*/
/*										*/
/*	Javascript code for handling search results				*/
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

var source_set = new Array();



/********************************************************************************/
/*										*/
/*	Top-level search methods						*/
/*										*/
/********************************************************************************/

function startMethodSearch()
{
   var doc = createNewDocument("S6:SEARCH");
   var req = doc.documentElement;
   req.setAttribute("WHAT","METHOD");
   req.setAttribute("REMOTE",options[OPTION_REMOTE]);
   req.setAttribute("LOCAL",options[OPTION_LOCAL]);
   var desc = document.getElementById("Keywords").value.trim();
   var keyset = desc.split(";");
   for (var j = 0; j < keyset.length; ++j) {
      var keyselt = doc.createElement("S6:KEYWORDS");
      req.appendChild(keyselt);
      var keys = keyset[j].split(" ");
      for (var i = 0; i < keys.length; ++i) {
	 if (keys[i] != "") {
	    var keyelt = doc.createElement("KEYWORD");
	    setTextContent(keyelt,keys[i]);
	    keyselt.appendChild(keyelt);
	  }
       }
    }
   addXmlNode(doc,req,method_data[0].signature);

   var testselt = doc.createElement("S6:TESTS");
   req.appendChild(testselt);
   for (var i = 1; i <= method_data[0].max_test; ++i) {
      var tdata = method_data[0].tests[i];
      if (tdata.test_case != null) {
	 addXmlNode(doc,testselt,tdata.test_case);
       }
    }

   var xmlh = new XMLHttpRequest();
   xmlh.onreadystatechange = function() {
      handleSearchResult(xmlh);
    }

   clearResults();

   xmlh.open("POST","dosearch.php?TYPE=METHOD",true);
   xmlh.send(convertXmlToString(doc));

   clearResultError();
   startResultWait();
}



function startClassSearch()
{
   var doc = createNewDocument("S6:SEARCH");
   var req = doc.documentElement;
   req.setAttribute("WHAT","CLASS");
   req.setAttribute("REMOTE","FALSE");
   req.setAttribute("LOCAL","TRUE");
   var desc = document.getElementById("Keywords").value.trim();
   var keyset = desc.split(";");
   for (var j = 0; j < keyset.length; ++j) {
      var keyselt = doc.createElement("S6:KEYWORDS");
      req.appendChild(keyselt);
      var keys = keyset[j].split(" ");
      for (var i = 0; i < keys.length; ++i) {
	 if (keys[i] != "") {
	    var keyelt = doc.createElement("KEYWORD");
	    setTextContent(keyelt,keys[i]);
	    keyselt.appendChild(keyelt);
	  }
       }
    }

   var sign = createClassSignature(doc);
   req.appendChild(sign);

   addXmlNode(doc,req,class_data.test_cases);

   var xmlh = new XMLHttpRequest();
   xmlh.onreadystatechange = function() {
      handleSearchResult(xmlh);
    }

   clearResults();

   xmlh.open("POST","dosearch.php?TYPE=METHOD",true);
   xmlh.send(convertXmlToString(doc));

   clearResultError();
   startResultWait();
}



function handleSearchResult(xmlh)
{
   var lbl = document.getElementById("ResultHead");

   if (xmlh.readyState != 4) {
      lbl.style.color = "black";
      return;
    }

   stopResultWait();
   if (xmlh.status != 200) {
      lbl.style.color = "red";
      setResultError("Bad status from server: " + xmlh.status);
      return;
    }

   lbl.style.color = "green";

   var rslts = xmlh.responseXML;
   if (rslts == null) {
      alert("NO RESPONSE RETURNED");
      return;
    }

   // alert("RESPONSE = " + convertXmlToString(rslts));

   var solns = getXmlElement(rslts,"SOLUTIONS");
   if (solns == null) {
      var err = null;
      var prob = getXmlElement(rslts,'PROBLEM');
      if (prob != null) err = getTextContent(prob);
      prob = getXmlElement(rslts,'ERROR');
      if (err != null && prob != null) err = getTextContent(prob);
      if (err == null) err = "No solutions provided by server";
      lbl.style.color = "red";
      setResultError(err);
      return;
    }

   var srcs = getXmlElements(rslts,'SOURCE');
   for (var i = 0; i < srcs.length; ++i) {
      var snam = getTextContent(srcs[i]);
      source_set.push(snam);
    }

   var nsol = 0;
   var sset = getXmlElements(rslts,"SOLUTION");
   for (var i = 0; i < sset.length; ++i) {
      var codee = getXmlElement(sset[i],'CODE');
      var code = getTextContent(codee);
      addResult(code);
      ++nsol;
    }

   // error if nothing found (or message at least)
}



/********************************************************************************/
/*										*/
/*	Handle animation while waiting for resuls				*/
/*										*/
/********************************************************************************/

var result_timer = null;
var result_wait = 0;
var result_animate_count = 0;

function startResultWait()
{
   ++result_wait;
   if (result_timer != null) return;
   result_timer = setInterval("resultAnimate()",250);
}


function stopResultWait()
{
   --result_wait;
   if (result_wait > 0) return;
   clearInterval(result_timer);
   var rarea = document.getElementById("ResultHead");
   rarea.innerHTML = "Results:";
   result_timer = null;
   result_wait = 0;
}



function resultAnimate()
{
   x = "Results: ";
   for (var i = 0; i < 32; ++i) {
      x += "<span class='";
      if (i == result_animate_count) x += 'resultanimhigh';
      else x += 'resultanimlow';
      x += "'>.&nbsp;</span>";
   }
   var rarea = document.getElementById("ResultHead");
   rarea.innerHTML = x;
   result_animate_count = (result_animate_count+1)%32;
}



/********************************************************************************/
/*										*/
/*	Handle result error display						*/
/*										*/
/********************************************************************************/

function clearResultError()
{
   var earea = document.getElementById("ResultError");
   earea.innerHTML = "";
}



function setResultError(err)
{
   var earea = document.getElementById("ResultError");
   earea.innerHTML = err;
}



/********************************************************************************/
/*										*/
/*	Results display 							*/
/*										*/
/********************************************************************************/

function clearResults()
{
   var rtbl = document.getElementById("ResultTable");
   while (rtbl.rows.length > 0) {
      rtbl.deleteRow(0);
    }
}



function addResult(rslt)
{
   var rtbl = document.getElementById("ResultTable");
   var tr = rtbl.insertRow(rtbl.rows.length);
   var td = tr.insertCell(0);
   var pre = document.createElement("PRE");
   setTextContent(pre,rslt);
   td.appendChild(pre);
}




/* end of searchresult.js */

