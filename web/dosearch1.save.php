<?php
/********************************************************************************/
/*										*/
/*		dosearch1.php							 */
/*										*/
/*	General checking utility for AJAX use in search 			*/
/*										*/
/********************************************************************************/

error_reporting(E_ALL);

$host = "conifer";
$port = 17241;

session_start();


global $HTTP_RAW_POST_DATA;


function echoToServer($msg)
{
   global $host,$port;

   $sock = @fsockopen($host,$port,$errno,$errstr,60);
   if (!$sock) {
      return null;
    }

   fwrite($sock,"$msg\n");
   fflush($sock);
   stream_set_timeout($sock,600);

   echoMessage($sock);
   fclose($sock);

   return "OK";
}



function echoMessage($ss)
{
   while (1) {
      $txt = fgets($ss);
      if (!$txt) break;
      if ($txt == "***EOM***" || $txt == "***EOM***\n") break;
      echo $txt;
    }
}





header("Content-type: text/xml");

$rslt = echoToServer($HTTP_RAW_POST_DATA);


if ($rslt == null) {
   echo "<S6:ERROR>Server not available</S6:ERROR>\n";
 }
else {
   echo "\n";
}



/* end of dosearch.php */
?>

