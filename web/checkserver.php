<?php


$host = "localhost";
$port = 17241;


$sock = @fsockopen($host,$port,$errno,$errstr,10);
if (!$sock) echo "<br>OPEN FAILED $errno $errstr\n";
else {
   echo "<br>OPEN OK\n";
   fwrite($sock,"<PING />\n");
   fflush($sock);
   echo "<br>SEND OK\n";
   $msg = readMessage($sock);
   echo "<br>REPLY = $msg\n";
}

function readMessage($ss)
{
   $msg = "";
   while (1) {
      $txt = fgets($ss);
      if (!$txt) break;
      echo "<br>READNEXT $txt\n";
      if ($txt == "***EOM***" || $txt == "***EOM***\n") break;
      $msg .= $txt;
    }

   return $msg;
}



?>
