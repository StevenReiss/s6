<?php
/********************************************************************************/
/*										*/
/*		showlicense.php 						*/
/*										*/
/*	Routine to show a license page						*/
/*										*/
/********************************************************************************/

require("s6common.php");


$uid = $_REQUEST['UID'];

$cmd = "<CHECK WHAT='LICENSE'><UID>$uid</UID></CHECK>";

$rslt = sendToServer($cmd);

$parser = xml_parser_create();
xml_parse_into_struct($parser,$rslt,$vals);

$lic = '';

foreach ($vals as $k=>$v) {
   if ($v['tag'] == 'TEXT') {
      $lic = $v['value'];
      break;
    }
}

?>

<html>
<head>
<title>S6 License <?php echo $uid; ?></title>
<link rel='stylesheet' type='text/css' href='s6.css' />
</head>

<body>

<pre class='licensetext'>
<?php echo $lic; ?>
</pre>

</body>
</html>
<?php
/* end of showlicense.php */
?>



