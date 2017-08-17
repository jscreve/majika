<?php


$PDO = new PDO("mysql:host=localhost; dbname=majika", "root","" );
if(isset($_POST["submit"]))
       {
	
         $nom_prenom=htmlentities(trim($_POST["nom_prenom"])); // trim : supression espace devant et derieur et htmlentities: page html devient variable 
         $login=htmlentities(trim($_POST["login"]));
         $email=htmlentities(trim($_POST["email"]));
         $password=htmlentities(trim($_POST["password"]));
         $vpassword=htmlentities(trim($_POST["vpassword"]));
		 $etat=0;
		 
		 $error=array();
		 if(empty($nom_prenom) || strlen($nom_prenom)<4 ){

		  $error []= "nom et prénom obligatoire </br>";
		 }
		  if(empty($login) || strlen($login)<4 ){

		  $error[]= "longin doit avoir six caractère obligatoire </br>";
		 }
		  if(!filter_var($email, FILTER_VALIDATE_EMAIL) ){

		  $error[]= "Email invalide </br>";
		 }
		  if(empty($password) || strlen($password)<6 || $password != $vpassword ){

		  $error[]= "Mot de passe est trop court ou ne correspons pas!</br>";
		 } 
		 if(verifaction_login($PDO, $login)){

		  $error[]= "Cet login existe deja!</br>";
		 }
		 if(verifaction_email($PDO, $email)){

		  $error[]= "Cet email est deja utilise!</br>";
		 }
		 
		 if(empty($error)){
			 $password=sha1($password);// criptage sha1
			 $stm = $PDO->prepare('INSERT INTO utilisateurs(nom_prenom, login, email, password, etat, date) VALUES(:nom_prenom, :login, :email, :password, :etat, NOW())');
			
			 $stm->bindValue(':nom_prenom',$nom_prenom, PDO::PARAM_STR);
			 $stm->bindValue(':login',$login, PDO::PARAM_STR);
			 $stm->bindValue(':email',$email, PDO::PARAM_STR);
			 $stm->bindValue(':password',$password, PDO::PARAM_STR);
			 $stm->bindValue(':etat',$etat, PDO::PARAM_INT);
			
			 if($stm->execute()){
				 echo 'inscription terminer avec successe <a href="connexion.html">connectez-vous ici!!! </a> </br>';
			 } else{
				 echo " Echec d'inscription  <a href='inscription.html'> Re-essaye </a>";
			 }
		 } else {
			 
			foreach($error as $ls){
				echo $ls; 
				
			}
			echo " <a href='inscription.html'> incription </a>";
			
		 }

        }

function verifaction_login($PDO, $login){
	 
	
	$stm=$PDO->prepare('SELECT COUNT(*) AS nbr FROM  utilisateurs WHERE login=:login');
	
	
	 $stm->bindValue(':login',$login, PDO::PARAM_STR); 
	  
	
	$stm->execute();
	$compte=($stm->fetchColumn()==0)?1:0; // expression ternier comme si et else
		if(!$compte){
			return true;
		}else {
			return false;
		}
	
}
function verifaction_email($PDO, $email){
	 
	
	$stm=$PDO->prepare('SELECT COUNT(*) AS nbr FROM  utilisateurs WHERE email=:email');
	
	
	 $stm->bindValue(':email',$email, PDO::PARAM_STR); 
	  
	
	$stm->execute();
	$compte=($stm->fetchColumn()==0)?1:0; // expression ternier comme si et else
		if(!$compte){
			return true;
		}else {
			return false;
		}
	
}
?>