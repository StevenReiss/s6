<?php
/********************************************************************************/
/*										*/
/*		search.php							*/
/*										*/
/*	Main page for S6 search 						*/
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

require("s6common.php");


error_reporting(E_ALL);

handleSession();

srand(time());


function script($file)
{
   $rint = (rand() % 32768);
   echo "<script language='javascript.12' type='text/javascript' src='$file?v$rint'></script>\n";
}




?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
<title>S6 Search Page</title>
<link rel="stylesheet" type="text/css" href="s6.css" />
<?php
script("search.js");
script("searchtest.js");
script("searchclass.js");
script("searchresult.js");
script("util.js");
?>
</head>
<body onload='initializeSearch()'>

<form id="FindForm" action="search.html" method="post" onsubmit='return false' >

<table id="TableGeneral" width="100%" cellpadding='0' cellspacing='0' class="toptable"
			summary="Top-level search information">
  <colgroup width='1px'></colgroup>
  <colgroup width='20px'></colgroup>
  <colgroup width='1%'></colgroup>
  <colgroup width="1*"></colgroup>
  <colgroup width='0px'></colgroup>
  <tr>
    <td rowspan='5' >
      <div id="Logo" class="logo">S<span class="logosup">6</span></div>
    </td>
    <td colspan='6' class="logotopbar" align='right' valign='top'> </td>
  </tr>
  <tr>
    <td rowspan='3'></td>
    <td class="lookfor" height='40px'>
      <label for="LookFor">Look for: </label>
    </td>
    <td class="lookfor">
      <select name="lookfor" id="LookFor" size="1" onchange='handleSearchFor()'>
	<option selected="selected" value="METHOD">METHOD</option>
	<option value="CLASS">CLASS</option>
      </select>
    </td>
    <td class="lookfor" height='40px'>
      <label for="In">In: </label>
    </td>
    <td class="lookin">
      <select name="lookin" id="LookIn" size="1" onchange='handleSearchIn()'>
	<option value="RL">Remote & Local</option>
	<option value="R">Remote</option>
	<option selected='selected' value="L">Local</option>
      </select>
      Archives
    </td>
  </tr>
  <tr>
    <td class="description">
      <label id="KeywordLabel" for="Keywords">
	 Description:&nbsp;&nbsp;<span class="subdesc"><br>&nbsp;&nbsp;&nbsp;(keywords)</span>
      </label>
    </td>
    <td valign='top' colspan='3' class='descriptioninput'>
      <input type='text' id="Keywords" name="keywords" size='60' maxlength='512'
		alt="keywords describing search"
		onblur="checkKeywords()" />
    </td>
  </tr>
  <tr> <td colspan='5' class="errormessage" id="ErrorMessage">&nbsp;</td> </tr>
  <tr>
    <td colspan='6' class="logobar" height='20px'></td>
  </tr>
</table>

<br/>

<div id="MethodDiv">
<table width='100%' class="methodtable" id="Method0" frame='box' rules='none'
		summary="Information for a method">
  <caption align="top" class="methodcaption">Method</caption>
  <colgroup width="1*"></colgroup>
  <colgroup width="10000*"></colgroup>
  <tr>
    <td class="methoddecl">
      <label id="MthddescLabel0" for="Mthddesc0">Declaration:&nbsp;</label>
    </td>
    <td>
      <input type='text' name="Mthddesc0" id="Mthddesc0" size='64' maxlength='256'
		onblur='checkMethodDeclaration(0,true)' />
    </td>
  </tr>
  <tr>
    <td class="methodtest">Tests: </td>
    <td>
      <table class="tests" border='3' frame='box' cellpadding='2px' id='METHODTestTable'
			width='100%' summary="Information for method tests">
	<colgroup width="1*"></colgroup>
	<colgroup width="10000*"></colgroup>
	<colgroup width="1*"></colgroup>
	<colgroup width="10000*"></colgroup>
	<colgroup width="1*"></colgroup>
	<thead>
	   <tr>
	     <th colspan='4'>Test Description</th>
	     <th>Type</th>
	   </tr>
	</thead>
	<tbody id='METHODTestTableBody'><tr><td/></tr>
	</tbody>
      </table>
    </td>
  </tr>
</table>
</div>

<div id="ClassDiv" style='display:none'>
<table width='100%' class="classtable" id="ClassTable" frame='box' rules='none'
		summary="Information for a class">
  <caption align="top" class="classcaption">Class</caption>
  <colgroup width="1*"></colgroup>
  <colgroup width="10000*"></colgroup>
  <tr>
     <td class="classdecl">
	<label for="ClassName" id='ClassNameLabel'>Class&nbsp;name:&nbsp;</label>
     </td>
     <td>
       <input id="ClassName" name="ClassName" size='32' maxlength='64'
		onchange='checkClassName()' />
     </td>
  </tr>
  <tr>
    <td class="classmethods">Methods:&nbsp;</td>
    <td>
       <table class="methods" border='3' frame='box' cellpadding='2px' id='ClassMethods'
			width='100%' summary='Methods defined for class'>
	 <tr>
	   <td>
	      <tr />
	   </td>
	 </tr>
       </table>
     </td>
  </tr>
  <tr>
    <td class="classtests">Tests:&nbsp;</td>
    <td>
      <table class="tests" border='3' frame='box' cellpadding='2px' id='CLASSTestTable'
			width='100%' summary="Information for method tests">
	<colgroup width="1*"></colgroup>
	<colgroup width="10000*"></colgroup>
	<colgroup width="1*"></colgroup>
	<colgroup width="10000*"></colgroup>
	<colgroup width="1*"></colgroup>
	<thead>
	   <tr>
	     <th colspan='4'>Test Description</th>
	     <th>Type</th>
	   </tr>
	</thead>
	<tbody id='CLASSTestTableBody'><tr><td/></tr>
	 </tbody>
      </table>
    </td>
  </tr>
</table>
</div>


<br/>
<center>
<input type='button' class='findit' value='Find It!' onclick='validateSearch()' />
</center>
</form>
<hr class="resultsep" />
<div id="ResultHead" class="resulthead">Results:</div>
<br/>
<div id="ResultError" class="resulterror"></div>
<div id="ResultArea" class="results">
<table id="ResultTable" width="100%" cellpadding="4" cellspacing='4' class="resulttable"
			summary="Results listed" rules="rows" frame="hsides">
<tr><td></td></tr>
</table>

</div>
</body>
</html>

