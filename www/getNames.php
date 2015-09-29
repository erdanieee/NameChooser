<?php

$SQL_LIMIT    = ( isset($_GET["buffer"]) && is_numeric($_GET["buffer"]) ) 	 ? ((int)$_GET["buffer"]) : 50;
$SQL_FREQ_MAX = ( isset($_GET["freq"]) && is_numeric($_GET["freq"]) ) 		 ? ((float)$_GET["freq"]) : 10; 
$SQL_FREQ_MIN = ( isset($_GET["freqMin"]) && is_numeric($_GET["freqMin"]) ) 	 ? ((float)$_GET["freqMin"]) : 0.05;
$SQL_SEXO     = ( isset($_GET["sexo"]) && preg_match('/^[HM]$/',$_GET["sexo"]) ) ? "'".$_GET["sexo"]."'" : "'H'";
$SQL_COMP_NAM = ( isset($_GET["multiName"]) ) ? "" : " AND nombre not like '% %' "; 

$mysqli=new mysqli("localhost","names","como1cerda=)","names") or die('Could not connect to the database server' . $mysqli->connect_error);

$string= 'No results found!';
$query = "SELECT id,nombre FROM nombres where sexo like $SQL_SEXO and frecuencia>=$SQL_FREQ_MIN and frecuencia <=$SQL_FREQ_MAX $SQL_COMP_NAM order by RAND() limit $SQL_LIMIT";

//echo $query;
if ($stmt = $mysqli->prepare($query)) {
	$stmt->execute();
    	$stmt->bind_result($id, $nombre);

	$string='';
    	while ($stmt->fetch()) {
        	$string .= $id .  ":" . $nombre . ";";
    	}
    	$stmt->close();

} else {
	echo "Error en la query " . $mysqli->connect_error;
	http_response_code(400);
	exit();
}

$mysqli->close();
$content='';

header("Content-Type: plain/text");
if(strlen($string)>2){
	$content = substr($string,0,-1);

} else {
	$content = 'No result founds!';
//	http_response_code(500);
}
echo htmlspecialchars($content);
?>
