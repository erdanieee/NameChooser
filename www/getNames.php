<?php
//TODO:seleccionar 75 nombres votados, 25 del total y barajar
//TODO: eliminar tamaño del buffer (dejarlo solo en la parte del servidor)


function queryToArray($mysqli, $query, &$a){
	//echo "<br><br>";
	//var_dump($query);
	if ($stmt = $mysqli->prepare($query)) {
		$stmt->execute();
		$stmt->bind_result($id, $nombre);

	    	while ($stmt->fetch()) {
			$a[$id] = $nombre;
			//echo "<br>".$a[$id];
	    	}
	    	$stmt->close();

	} else {
		echo "Error en la query " . $mysqli->connect_error;
		http_response_code(400);
		exit();
	}
}

function shuffle_with_keys(&$array) {
    /* Auxiliary array to hold the new order */
    $aux = array();
    /* We work with an array of the keys */
    $keys = array_keys($array);
    /* We shuffle the keys */
    shuffle($keys);
    /* We iterate thru' the new order of the keys */
    foreach($keys as $key) {
      /* We insert the key, value pair in its new order */
      $aux[$key] = $array[$key];
      /* We remove the element from the old array to save memory */
      unset($array[$key]);
    }
    /* The auxiliary array with the new order overwrites the old variable */
    $array = $aux;
  } 




$SQL_LIMIT_VOTED  = 40;
$SQL_LIMIT_ALL  = 10;

$SQL_SEXO     = ( isset($_GET["sexo"]) && preg_match('/^[HM]$/',$_GET["sexo"]) ) ? "'".$_GET["sexo"]."'"				: "'H'";
$SQL_USER     = ( isset($_GET["user"]) ) 					 ? "'".$_GET["user"]."'" 				: "'Dan'";
$SQL_FREQ_MAX = ( isset($_GET["freqMax"]) && is_numeric($_GET["freqMax"]) )	 ? " AND frecuencia <=".((float)$_GET["freqMax"]) 	: ""; 
$SQL_FREQ_MIN = ( isset($_GET["freqMin"]) && is_numeric($_GET["freqMin"]) ) 	 ? " AND frecuencia >=".((float)$_GET["freqMin"]) 	: "";
$SQL_COMP_NAM = ( ! isset($_GET["multiName"]) ) 				 ? " AND nombre not like '% %'" 			: "";
$SQL_COUNT    = ( isset($_GET["count"]) ) 					 ? true : false;

$query1 = "SELECT n.id, n.nombre ".
		" from votos v ".
		" left join nombres n on v.idName like n.id ".
		" where v.user like $SQL_USER ".
		" and n.sexo like $SQL_SEXO ".
 		$SQL_FREQ_MIN .
		$SQL_FREQ_MAX .
		$SQL_COMP_NAM .
		" order by rand() ".
		" limit $SQL_LIMIT_VOTED";
$query2 = "SELECT id, nombre ".
		" from nombres ".
		" where sexo like $SQL_SEXO ".
 		$SQL_FREQ_MIN .
		$SQL_FREQ_MAX .
		$SQL_COMP_NAM .
		" order by rand() ".
		" limit $SQL_LIMIT_ALL";

//Si se realiza una consulta de número de nombres disponible, se redirige la consulta a getCount.php
if ($SQL_COUNT){
	header( "Location: ./getCount.php".
		$_SERVER['QUERY_STRING']) ;	
	exit();
}


$items = array();

$mysqli = new mysqli("localhost","names","como1cerda=)","names") or die('Could not connect to the database server' . $mysqli->connect_error);
//obtenemos una sublista de nombres votados
queryToArray($mysqli, $query1, $items);
//obtenemos una sublista de nombres (votados y no votados)
queryToArray($mysqli, $query2, $items);
$mysqli->close();

//randomizamos el order
shuffle_with_keys($items);

//formateamos la salida
$string= '';
foreach($items as $k => $v) {
	$string.= $k . ":" . $v . ";";
}


echo htmlspecialchars(substr($string,0,-1));
?>
