<?php

session_start();

$PDO = new PDO("mysql:host=localhost; dbname=majika", "root","" );
if(isset($_POST["submit"]))
       {
	
          
         $login=htmlentities(trim($_POST["login"]));
         $password=htmlentities(trim($_POST["password"]));
        
		 $error=array();
		
		  if(!verifaction_log_mdp($PDO, $login, $password) ){

		  $error[]= "longin ou mot de passe incorrect </br>";
		 } else{
			 $utilisateur=recup_user($PDO, $login, $password);
			 if($utilisateur['etat'] !=0){
				 $_SESSION["user"]=$utilisateur;
				 header("Location:index.php");
				 
			 }else{
				 echo 'votre compte est encour de validation par administrateur </br>';
			 }
		 }
		
		 
		 if(!empty($error)){
			 
					 
			foreach($error as $ls){
				echo $ls; 
				
			}
			
			
		 }

        }

function verifaction_log_mdp($PDO, $login, $password){
	 
	$password=sha1($password);
	$stm=$PDO->prepare('SELECT COUNT(*) AS nbr FROM  utilisateurs WHERE (login=:login || email=:login) AND password=:password');
	
	
	 $stm->bindValue(':login',$login, PDO::PARAM_STR); 
	 $stm->bindValue(':password',$password, PDO::PARAM_STR); 
	
	$stm->execute();
	$compte=($stm->fetchColumn()==0)?1:0; // expression ternier comme si et else
		if(!$compte){
			return true;
		}else {
			return false;
		}
	
}

function recup_user($PDO, $login, $password){
	 
	$password=sha1($password);
	$stm=$PDO->prepare('SELECT * FROM  utilisateurs WHERE (login=:login || email=:login) AND password=:password');
	
	
	 $stm->bindValue(':login',$login, PDO::PARAM_STR); 
	 $stm->bindValue(':password',$password, PDO::PARAM_STR); 
	
	$stm->execute();
	
			return $stm->fetch();
		
	
}
?>