<?php
/* session_start();
if(!isset($_SESSION["user"])){
	header("Location:connexion.html");
}	*/

	
// indiqué le chemin de votre fichier JSON, il peut s'agir d'une URL
	$json = file_get_contents("ups.json", true);
	$data = json_decode($json);           // decode le fichier json

?>


<!DOCTYPE html>

<html>
	<br/>
<head>
      <meta http-equiv='refresh' content='30'/>
      <meta charset='utf-8'/>
      <meta name="viewport" content="width=device-width, initial-scale=1">
	  <link rel="shortcut icon" type="image/x-icon" href="images/majika.png" />
      <link rel="stylesheet" href="css/bootstrap.min.css" type="text/css" media="screen" />
      <link rel="stylesheet" href="css/bootstrap-theme.min.css" type="text/css" media="screen" />
	  <link href="css/css/font-awesome.min.css" rel="stylesheet"> 
      <link rel="stylesheet" href="css/my.css" type="text/css" media="screen" />
      <title> monitoring ampasindava </title>
</head>	
	<body>
		<div class="container" id="block_page">  <!-- En-tête du page -->
		 <header>
			 <section>
                 <div class="row">
                   <div class="col-sm-6">
                     <div id="logo">  <img src="images/majika_logo.png" alt="logo_de_majika" /> </div>
                        <h2 id="titre_principal">Monitoring Ampasindava</h2>
                    </div>
			<nav class="navbar navbar-inverse">
				<ul class="nav navbar-nav">
					<li class="active"> <a href="index.php">Accueil</a> </li>  <!--index.php-->
					<li> <a href="#">Graphiques</a> </li>
					<li> <a href="download.html">Downloads</a> </li>
					<li> <a href="deconnection.php">Deconnexion</a> </li> 
					<li> <a href="autre.html">Autre...</a> </li>
				</ul>
			<form class="navbar-form pull-right">
				<input type="text" style="width:150px" class="input-small" placeholder="Recherche">
				<button type="submit" class="btn btn-primary btn-xs"><span class="glyphicon glyphicon-eye-open"></span> Chercher</button>
			</form>
			</nav>
		    </div>
			 </section>
		</header>
		<section>
		   <div class="container">
			<div class=row>
			<div class="col-sm-3">
				<h1><span class="pull-right small"><?php echo $data->{'Date'}[0] ; ?></h1> <i class="fa fa-hourglass-half fa-2x fa-fw"></i></span> 
				
			</div>
			</div>
		   </div>
		</section>
		
<!-- Les tableau -->
		<section>
			<div class="container">
			 <div class=row>
			  <div class="col-sm-6"> <!-- Les tableaux mesure SPS -->
             <table  class="table table-bordered table-striped table-condensed">
                 <caption> <h3> SPS - Mesures </h3> </caption >
                   <thead> <!-- En-tête du tableau -->
                        <tr>
                          <th>Nom</th>
                          <th>Valeur</th>
                          <th>Unité</th>
                        </tr>
				   </thead>
	        <tbody> <!-- Corps du tableau -->
           <tr>
             <td>Etat Charge Batteries</td>
             <td><?php echo $data->{'UPS_SPS_Battery_'}[0]->{'Capacity Battery'}; ?></td>
             <td>[%]</td>
           </tr>
           <tr>
             <td>Courant Charge Parc Batterie</td>
             <td><?php echo $data->{'UPS_SPS_Battery_'}[0]->{'Battery Current'}; ?></td>
             <td>[±A]</td>
           </tr>
           <tr>
             <td>Tension Parc Batterie</td>
             <td><?php echo $data->{'UPS_SPS_Battery_'}[0]->{'Battery Voltage'}; ?></td>
             <td>[V]</td>
           </tr>
           <tr>
             <td>Autonomie Parc Batterie</td>
             <td><?php echo $data->{'UPS_SPS_Battery_'}[0]->{'Autonomy Battery'}; ?></td>
             <td>[mn]</td>  
           </tr>
           <tr>
             <td>OUT SPS</td>
             <td><?php echo $data->{'UPS_SPS_Battery_'}[0]->{'OUTSPS'}; ?></td>
             <td>[%VA]</td>
           </tr>
           <tr>
             <td>Température  Système</td>
             <td><?php echo $data->{'UPS_SPS_Battery_'}[0]->{'Temperature SPS'};?></td>
             <td>[°C]</td>
           </tr>
       <!-- division du tableau en 3 valeur par phase -->
           <tr>
             <td rowspan=3></br>Tension Réseaux </br> Respective Phase 1,2 et 3 </td>
             <td><?php echo $data->{'UPS_SPS_Battery_'}[0]->{'Output V1'};?></td>
             <td rowspan=3></br>[V]</td> <!--mofi-->
           </tr>
           <tr>
             <td><?php echo $data->{'UPS_SPS_Battery_'}[0]->{'Output V2'};  ?></td>
           </tr>
           <tr>
             <td><?php echo $data->{'UPS_SPS_Battery_'}[0]->{'Output V3'}; ?></td>
           </tr>
           <tr><!-- Courant reseau -->
             <td rowspan=3></br>Courent Réseaux  </br> Respective Phase 1,2 et 3</td>
			 <td><?php echo $data->{'UPS_SPS_Battery_'}[0]->{'Output I1'}; ?></td>
             <td rowspan=3></br>[A]</td>
           </tr>
           <tr>
             <td><?php echo $data->{'UPS_SPS_Battery_'}[0]->{'Output I2'}; ?></td>
           </tr>
           <tr>
             <td><?php echo $data->{'UPS_SPS_Battery_'}[0]->{'Output I3'}; ?></td>
           </tr>                               
           <tr>  <!-- Courant Crête -->
			 <td rowspan=3></br>Courant Crête  </br> Respective Phase 1,2 et 3</td>
             <td> <?php echo $data->{'UPS_SPS_Battery_'}[0]->{'Output Peak I1'}; ?></td>
             <td rowspan=3></br>[A]</td>
           </tr>
           <tr>
             <td><?php echo $data->{'UPS_SPS_Battery_'}[0]->{'Output Peak I2'}; ?></td>
           </tr>
           <tr>
             <td><?php echo $data->{'UPS_SPS_Battery_'}[0]->{'Output Peak I3'}; ?></td>
           </tr>                                           
       </tbody>
	         </table>
              </div>
              <div class="col-sm-6"> <!-- Les tableaux mesure des SIRIO -->
				<table class="table table-bordered table-striped table-condensed" >
				  <caption> <h3> Mesure des Sirio </h3> </caption>
		            <thead>
					  <tr>
						<th rowspan="2">Nom</th>
						<th colspan="2">Valeus</th> <!--tsy mandea &nbsp;  width=60% border=2 table -->
						<th rowspan="2">Unité</th>
					  </tr>
					  <tr>
						<th>SIRIO I Ouest</th>
						<th>SIRIO II Est</th>
					  </tr>
					</thead
					<tbody>
			          <tr>
						<td>Puissance</td>
						<td><?php echo $data->{'UPS_SolarWest_'}[0]->{'Power'}; ?> </td> <!-- SIRIO1 l'autre 2   -->
						<td><?php echo $data->{'UPS_SolarEast_'}[0]->{'Power'}; ?></td>
					    <td>[Kw]</td>
					  </tr>
					  <tr>
						<td> Temperature </td>
						<td><?php echo $data->{'UPS_SolarWest_'}[0]->{'Temperature Ups'}; ?> </td><!-- SIRIO1 west-->
						<td><?php echo $data->{'UPS_SolarEast_'}[0]->{'Temperature Ups'}; ?> </td>
						<td>[ºC]</td>
					  </tr>
					  <tr>
						<td> Frenquence Réseau</td>
						<td><?php echo $data->{'UPS_SolarWest_'}[0]->{'Grid Frequency'}; ?> </td><!-- SIRIO1 west-->
						<td><?php echo $data->{'UPS_SolarEast_'}[0]->{'Grid Frequency'}; ?> </td>
						<td>[Hz]</td>
					  </tr>
					  <tr>
					    <td> Énergie Totale Réseau </td>
						<td> <?php echo $data->{'UPS_SolarWest_'}[0]->{'Total Energy To Grid'}; ?>  </td><!-- SIRIO1 west-->
		                <td> <?php echo $data->{'UPS_SolarEast_'}[0]->{'Total Energy To Grid'}; ?> </td>
						<td>[kWh]</td>
					  </tr>
					  <tr>
					    <td> Heures Total de Fonctionnement </td>
						<td> <?php echo $data->{'UPS_SolarWest_'}[0]->{'Total Operating Hours'}; ?> </td><!-- SIRIO1 west-->
						<td> <?php echo $data->{'UPS_SolarEast_'}[0]->{'Total Operating Hours'}; ?>  </td>
						<td>[H]</td>
					  </tr>
					  <tr>
				        <td rowspan=3></br>Tension  Respective Phase 1,2 et 3</td>
					    <td> <?php echo $data->{'UPS_SolarWest_'}[0]->{'Voltage L1'}; ?> </td></td> <!-- SIRIO1 -->
					    <td> <?php echo $data->{'UPS_SolarEast_'}[0]->{'Voltage L1'}; ?> </td>
					    <td rowspan=3></br>[V]</td>
					  </tr>
					  <tr>
					    <td> <?php echo $data->{'UPS_SolarWest_'}[0]->{'Voltage L2'}; ?></td> <!-- SIRIO1 -->
					    <td> <?php echo $data->{'UPS_SolarEast_'}[0]->{'Voltage L2'}; ?> </td>
					  </tr>
					  <tr>
						<td> <?php echo $data->{'UPS_SolarWest_'}[0]->{'Voltage L3'}; ?> </td> <!-- SIRIO1 -->
						<td> <?php echo $data->{'UPS_SolarEast_'}[0]->{'Voltage L3'}; ?> </td>
					  </tr>
					  <tr>
						 <td rowspan=3></br>Courent  Respective Phase 1,2 et 3</td>
						 <td> <?php echo $data->{'UPS_SolarWest_'}[0]->{'Current L1'}; ?> </td> </td> <!-- SIRIO1 -->
						 <td> <?php echo $data->{'UPS_SolarEast_'}[0]->{'Current L1'}; ?></td>
						  <td rowspan=3></br>[A]</td>
					  </tr>
					  <tr>
						 <td><?php echo $data->{'UPS_SolarWest_'}[0]->{'Current L2'}; ?> </td> <!-- SIRIO1 -->
				         <td> <?php echo $data->{'UPS_SolarEast_'}[0]->{'Current L2'}; ?></td>
					  </tr>
					  <tr>
		 				 <td><?php echo $data->{'UPS_SolarWest_'}[0]->{'Current L3'}; ?></td> <!-- SIRIO1 -->
						 <td><?php echo $data->{'UPS_SolarEast_'}[0]->{'Current L3'}; ?></td>
					  </tr>
						<!-- Courent crÊte Respective Phase lien vers majika/courent crete.php-->
					</tbody>
				</table>
              </div>	  
            </div>
          </div>
        </section>
        <section>
			<div class="container">
				<div class=row>
					<div class="col-sm-6"> <!-- Les tableauxTemperature des Locals et autres -->
						<table class="table table-bordered table-striped table-condensed" >
							<caption> <h3>Temperature des Locaux et autres </h3> </caption>
							<thead>
								<tr>
									<th>Nom</th>
									<th>Valeus</th>
									<th>Unité</th>
								</tr>
							</thead>
							<tbody>
								<tr>
									<td> Local Batterie </td>
									<td><?php echo $data->{'UPS_SPS_Battery_'}[0]->{'Local Batterie'}; ?></td>
									<td>[ºC]</td>
								</tr>
								<tr>
									<td> Local Onduleurs</td>
								    <td><?php echo  $data->{'UPS_SPS_Battery_'}[0]->{'Local Onduleurs'}; ?></td>
									<td>[ºC]</td>
								</tr>
								<tr>
								    <td> Raspberry Pi </td>
								    <td><?php echo $data->{'UPS_SPS_Battery_'}[0]->{'RaspBerry Pi Temperature'}; ?></td>
								    <td>[ºC]</td>
								</tr>
							</tbody>
	
						</table>
					</div>
					<div class="col-sm-6">
						<table class="table table-bordered table-striped table-condensed" >
							<caption> <h3>Comptage des Compteurs du Central</h3> </caption>
							<thead>
								<tr>
									<th rowspan="2">Nom</th>
							 		<th colspan="5">Valeus</th>
								</tr>
								<tr>
									<th> HCHP </th>
			 					    <th>I1 [A]</th>
			 						<th>I2 [A]</th>
									<th>I3 [A]</th>
									<th>P [VA]</th>
								</tr>
							</thead>
							<tbody>
								<tr>
									<td> Compteur 1 </td>
			 						<td><?php echo  $data->{'UPS_SPS_Battery_'}[0]->{'HCHP'}; ?></td>
			 						<td><?php echo  $data->{'UPS_SPS_Battery_'}[0]->{'I1'}; ?></td>
			 						<td><?php echo  $data->{'UPS_SPS_Battery_'}[0]->{'I2'}; ?></td>
									<td><?php echo  $data->{'UPS_SPS_Battery_'}[0]->{'I3'}; ?></td>
									<td><?php echo  $data->{'UPS_SPS_Battery_'}[0]->{'P'}; ?></td>
								</tr>
								<tr>
									<td>Compteur 2</td>
			 						<td>.....</td>
			 						<td>.....</td>
			 						<td>.....</td>
									<td>....</td>
									<td>....</td>
								</tr>
							</tbody>
					  </table>
					</div>		
				</div>
		
			</div>	

        </section>
		 <!-- Footer -->
	   <footer  class="footer text-center" class="row col-sm-12">
               <div class="panel panel-body">
			   </br> </br>
			      <p> <i class="fa fa-mobile fa-1x">:(+261) 32 71 425 26 032 / 034 061 55 44 </i></p>
			      <p><i class="fa fa-envelope-o fa-1x">:majika.madagascar@gmail.com</i></p>
				  <p><i class="fa fa-cogs fa-1x">:majiakasolution.org</i></p>
			      <p><i class="fa fa-facebook fa-1x">:Majikasolutions</i></p>
			      </br> 
				  <p>Copyright Majika - Tous droits réservés <br /><br />
              </div>
       </footer>

	   </div>

	</body>

</html>