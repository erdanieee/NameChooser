<?php
//TODO:seleccionar 75 nombres votados, 25 del total y barajar
//TODO: eliminar tamaño del buffer (dejarlo solo en la parte del servidor)


$SQL_LIMIT_VOTED  = 75;
$SQL_LIMIT_ALL  = 25;

$SQL_FREQ_MAX = ( isset($_GET["freqMax"]) && is_numeric($_GET["freqMax"]) )	 ? ((float)$_GET["freqMax"]) : 999; 
$SQL_FREQ_MIN = ( isset($_GET["freqMin"]) && is_numeric($_GET["freqMin"]) ) 	 ? ((float)$_GET["freqMin"]) : 0;
$SQL_SEXO     = ( isset($_GET["sexo"]) && preg_match('/^[HM]$/',$_GET["sexo"]) ) ? "'".$_GET["sexo"]."'" : "'H'";
$SQL_COMP_NAM = ( isset($_GET["multiName"]) ) ? "" : " AND nombre not like '% %' ";
$SQL_COUNT    = ( isset($_GET["count"]) ) ? true : false;

if ($SQL_COUNT){
	header( 'Location: ./getCount.php' ) ;	
	exit();
}


$mysqli=new mysqli("localhost","names","como1cerda=)","names") or die('Could not connect to the database server' . $mysqli->connect_error);
$string= 'No results found!';
$query = "";

if (){
	$query = "SELECT COUNT(id) as id FROM nombres where sexo like $SQL_SEXO and frecuencia>=$SQL_FREQ_MIN and frecuencia <=$SQL_FREQ_MAX $SQL_COMP_NAM";
} else {
	$query = "";
}

//echo $query;
if ($stmt = $mysqli->prepare($query)) {
	$stmt->execute();
	if ($SQL_COUNT){
		$stmt->bind_result($id);
	} else {
    		$stmt->bind_result($id, $nombre);
	}

	$string='';
    	while ($stmt->fetch()) {
		if($SQL_COUNT){
			$string = $id . ":";
		} else {
        		$string .= $id .  ":" . $nombre . ";";
		}
    	}
    	$stmt->close();

} else {
	echo "Error en la query " . $mysqli->connect_error;
	http_response_code(400);
	exit();
}

$mysqli->close();
$content='';

//header("Content-Type: plain/text");
if(strlen($string)>0){
	$content = substr($string,0,-1);

} else {
	$content = 'No result founds!';
//	http_response_code(500);
}
echo htmlspecialchars($content);
?>
