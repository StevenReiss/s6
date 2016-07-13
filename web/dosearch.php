<?php
/********************************************************************************/
/*										*/
/*		dosearch.php							*/
/*										*/
/*	General checking utility for AJAX use in search 			*/
/*										*/
/********************************************************************************/

require("s6common.php");

error_reporting(E_ALL);

handleSession();


global $HTTP_RAW_POST_DATA;


/* error_log("HANDLE COMMAND $HTTP_RAW_POST_DATA");     */


$rslt = sendToServer($HTTP_RAW_POST_DATA);

header("Content-type: text/xml");

if ($rslt == null) {
   echo "<S6:ERROR>Server not available</S6:ERROR>\n";
 }
else {
   echo "$rslt\n";
}



/* end of dosearch.php */
?>

