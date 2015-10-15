<?php

$SQL_FREQ_MAX = ( isset($_GET["freqMax"]) && is_numeric($_GET["freqMax"]) )	 ? ((float)$_GET["freqMax"]) : 999; 
$SQL_FREQ_MIN = ( isset($_GET["freqMin"]) && is_numeric($_GET["freqMin"]) ) 	 ? ((float)$_GET["freqMin"]) : 0;
$SQL_SEXO     = ( isset($_GET["sexo"]) && preg_match('/^[HM]$/',$_GET["sexo"]) ) ? "'".$_GET["sexo"]."'" : "'H'";
$SQL_COMP_NAM = ( isset($_GET["multiName"]) ) ? "" : " AND nombre not like '% %' ";

$mysqli=new mysqli("localhost","names","como1cerda=)","names") or die('Could not connect to the database server' . $mysqli->connect_error);
$query = "SELECT COUNT(id) as id FROM nombres where sexo like $SQL_SEXO and frecuencia>=$SQL_FREQ_MIN and frecuencia <=$SQL_FREQ_MAX $SQL_COMP_NAM";

if ($stmt = $mysqli->prepare($query)) {
	$stmt->execute();
	$stmt->bind_result($id);

	$string='';
    	while ($stmt->fetch()) {
		$string = $id;
    	}
    	$stmt->close();

} else {
	echo "Error en la query " . $mysqli->connect_error;
	http_response_code(400);
	exit();
}

$mysqli->close();

echo htmlspecialchars($string);
?>
