<?php
//SELECT nombre, frecuencia, user, score, count FROM votos v LEFT JOIN nombres n ON v.`idName` like n.`id`;
//"GET /names/sendData.php?clicked=7&nonClicked=4;8;4&userName=Dan
//score clicked = 10;
//	nonClicked = -1;


//TODO: cambiar para que solo se envíen votos positivos


if ( isset($_GET["clicked"]) && isset($_GET["nonClicked"]) && isset($_GET["userName"]) && is_numeric($_GET["clicked"]) ){
	//INSERT INTO table (a,b,c) VALUES (1,2,3) ON DUPLICATE KEY UPDATE c=c+1;

	$userName = htmlspecialchars( $_GET["userName"] );
	$clicked = (int) $_GET["clicked"];

	$nonClicked = array();
	foreach( split(";", $_GET["nonClicked"]) as $nc ){
		if ( is_numeric($nc) ){
			$nonClicked[] = (int) $nc;
		
		} else {
			http_response_code(400);	//400: bad request
			exit();
		}
	}

	$db=new mysqli("localhost","names","como1cerda=)","names");
	if ($db->connect_errno) {
    		printf("Connect failed: %s\n", $db->connect_error);
		http_response_code(500);	//500: internal server error
    		exit();

	} else {
		try {
			$db->begin_transaction();

			$query = "INSERT INTO votos (id, idName, user, score, count) VALUES (?, ?, '$userName', ?, 1) ON DUPLICATE KEY UPDATE score=score+?, count=count+1";
			
//			error_log(var_dump($query));
//			echo $stmt;
//			$stmt = $db->prepare($query);
			if ( !($stmt = $db->prepare($query)) ) {
	    			echo "Prepare failed: (" . $db->errno . ") " . $db->error;
			}


			$stmt->bind_param('siii', $i, $in, $s, $s);
		
			//clicked
			$i  = "'$userName$clicked'";
			$in = $clicked;
			$s  = 10;
			$stmt->execute();

			//non clicked
			foreach($nonClicked as $nc){
				$i  = "'$userName$nc'";
	        	        $in = $nc;
        	        	$s  = -1;
				$stmt->execute();
        	        }
		
			$stmt->close();	
			$db->commit();

			echo "ok";

		} catch (Exception $e) {
			$db->rollBack();
			echo "<br>";
			echo $db->error;
		}

        	$db->close();
	}

} else {
	echo "bad request";
	http_response_code(400);
}

?>
