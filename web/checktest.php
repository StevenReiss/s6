<?php
/********************************************************************************/
/*										*/
/*		checktest.php							*/
/*										*/
/*	General checking utility for AJAX use in search 			*/
/*										*/
/********************************************************************************/

require("s6common.php");

error_reporting(E_ALL);

handleSession();


global $HTTP_RAW_POST_DATA;

trigger_error("RAW $HTTP_RAW_POST_DATA",E_USER_WARNING);

$rslt = "";
$fh = fopen("php://input","r");
for ( ; ; ) {
   $x = fread($fh,1024);
   $rslt .= $x;
   if (feof($fh)) break;
}



trigger_error("CHECK " . $_REQUEST["TYPE"] . " " . gettype($HTTP_RAW_POST_DATA) . " " .
		 $rslt,E_USER_ERROR);



/* end of checktest.php */
?>

