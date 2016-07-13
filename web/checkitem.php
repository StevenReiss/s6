<?php
/********************************************************************************/
/*										*/
/*		checkitem.php							*/
/*										*/
/*	General checking utility for AJAX use in search 			*/
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




$rply = null;

if (array_key_exists('CLASS',$_REQUEST)) {
   $rply = checkMethodDeclaration($_REQUEST['METHOD'],$_REQUEST['CLASS']);
}
else if (array_key_exists('METHOD',$_REQUEST)) {
   $rply = checkMethodDeclaration($_REQUEST['METHOD'],null);
}
else if (array_key_exists('CALLIN',$_REQUEST)) {
   $rply = checkCallTest(stripslashes($_REQUEST['CALLIN']),
			    stripslashes($_REQUEST['CALLOUT']),
			    $_REQUEST['CALLOP'],
			    $_REQUEST['TESTNAME'],
			    $HTTP_RAW_POST_DATA);
}
else if (array_key_exists('CLASSTESTS',$_REQUEST)) {
   $rply = checkClassTests($HTTP_RAW_POST_DATA);
}



header("Content-type: text/xml");

if ($rply == null) {
   echo "<NULL />\n";
 }
else {
   echo "$rply\n";
}



function checkMethodDeclaration($mthd,$cls)
{
   $msg = "<S6:CHECK xmlns:S6='http://www.cs.brown.edu/S6' xmlns='http://www.cs.brown.edu/S6'";
   $msg .= " WHAT='METHOD'><METHOD><![CDATA[$mthd]]></METHOD>";
   if ($cls != null) $msg .= "<CLASS><![CDATA[$cls]]></CLASS>";
   $msg .= "</S6:CHECK>";

   $rslt = sendToServer($msg);

   return $rslt;
}



function checkCallTest($in,$out,$op,$tname,$sgn)
{
   $msg = "<S6:CHECK xmlns:S6='http://www.cs.brown.edu/S6' xmlns='http://www.cs.brown.edu/S6'";
   $msg .= " WHAT='CALLTEST'>";
   $msg .= "$sgn";
   $msg .= "<S6:ARGS><![CDATA[$in]]></S6:ARGS>";
   $msg .= "<S6:OP>$op</S6:OP>";
   $msg .= "<S6:RESULT><![CDATA[$out]]></S6:RESULT>";
   $msg .= "<S6:TESTNAME>$tname</S6:TESTNAME>";
   $msg .= "</S6:CHECK>";

   $rslt = sendToServer($msg);

   return $rslt;
}




function checkClassTests($data)
{
   $rslt = sendToServer($data);

   return $rslt;
}





/* end of checkitem.php */
?>
