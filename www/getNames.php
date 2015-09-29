<?php

//if (){

//} else {

//}

$SQL_LIMIT    = ( isset($_GET["buffer"]) && is_numeric($_GET["buffer"]) ) ? ((int)$_GET["buffer"]) : 50;
$SQL_FREQ_MAX = ( isset($_GET["freq"]) && is_numeric($_GET["freq"]) ) ? ((int)$_GET["freq"]) : 10; 
$SQL_SEXO     = ( isset($_GET["sexo"]) && preg_match('/^[HM]$/',$_GET["sexo"]) ) ? "'".$_GET["sexo"]."'" : "'H'";
$SQL_COMP_NAM = ( isset($_GET["multiName"]) ) ? "" : " AND nombre not like '% %' "; 


$mysqli=new mysqli("localhost","names","como1cerda=)","names");

//var_dump($mysqli->get_charset());

$string = 'No results found!';
if ($mysqli->connect_errno) {
    printf("Connect failed: %s\n", $mysqli->connect_error);
    exit();

} else {
	$query = "SELECT id,nombre FROM nombres where sexo like $SQL_SEXO and frecuencia>=0.05 and frecuencia <=$SQL_FREQ_MAX $SQL_COMP_NAM order by RAND() limit $SQL_LIMIT";
	//error_log(print_r($query, TRUE)); 

	if ($result = $mysqli->query($query)) {
		$i = 1;
		$string = '';
		while ($row = $result->fetch_assoc()){			
			$string .= $row["id"] .  ":" . $row["nombre"] . ";"; 
		}
		$result->free();
	}
	$mysqli->close();

	header("Content-Type: plain/text");
	$content = substr($string,0,-1);
	echo htmlspecialchars($content);
}
?>
