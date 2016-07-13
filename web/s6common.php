<?php
/********************************************************************************/
/*										*/
/*		s6common.php							*/
/*										*/
/*	Support methods for using the S6 web interface				*/
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
/*	Methods for communicating with S6 Server				*/
/*										*/
/********************************************************************************/

$host = "conifer";
$port = 17241;



/********************************************************************************/
/*										*/
/*	Session management methods						*/
/*										*/
/********************************************************************************/

function handleSession()
{
   session_start();
}




/********************************************************************************/
/*										*/
/*	Methods to communicate with S6 Server					*/
/*										*/
/********************************************************************************/

function sendToServer($msg)
{
   global $host,$port;

   $sock = @fsockopen($host,$port,$errno,$errstr,60);
   if (!$sock) {
      return null;
    }

   fwrite($sock,"$msg\n");
   fflush($sock);
   stream_set_timeout($sock,600);

   $msg = readMessage($sock);
   fclose($sock);

   return $msg;
}



function readMessage($ss)
{
   $msg = "";
   while (1) {
      $txt = fgets($ss);
      if (!$txt) break;
      if ($txt == "***EOM***" || $txt == "***EOM***\n") break;
      $msg .= $txt;
    }

   return $msg;
}




/* end of s6common.php */
?>
