
<!DOCTYPE html>

<html>
<br/>
<head>
    <meta charset='utf-8'/>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="shortcut icon" type="image/x-icon" href="images/majika.png"/>
    <link rel="stylesheet" href="css/bootstrap.min.css" type="text/css" media="screen"/>
    <link rel="stylesheet" href="css/bootstrap-theme.min.css" type="text/css" media="screen"/>
    <link href="css/css/font-awesome.min.css" rel="stylesheet">
    <link rel="stylesheet" href="css/my.css" type="text/css" media="screen"/>
    <title> monitoring ampasindava </title>
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/jquery.colorbox/1.6.4/jquery.colorbox-min.js"></script>
    <style>
        #colorbox {
            background-color: #FFF; /* Add a color of your choice */
        }
    </style>
</head>
<body>
<div class="container" id="block_page">  <!-- En-tête du page -->
    <header>
        <section>
            <div class="row">
                <div class="col-sm-6">
                    <div id="logo"><img src="images/majika_logo.png" alt="logo_de_majika"/></div>
                    <h2 id="titre_principal">Monitoring Ampasindava</h2>
                </div>
                <nav class="navbar navbar-inverse">
                    <ul class="nav navbar-nav">
                        <li><a href="index.php">Accueil</a></li>  <!--index.php-->
                        <li class="active"><a href="graph.php">Graphiques</a></li>
                        <li><a href="download.html">Downloads</a></li>
                        <li><a href="deconnexion.php">Deconnexion</a></li>
                        <li><a href="autre.html">Autre...</a></li>
                    </ul>
                    <form class="navbar-form pull-right">
                        <input type="text" style="width:150px" class="input-small" placeholder="Recherche">
                        <button type="submit" class="btn btn-primary btn-xs"><span
                                    class="glyphicon glyphicon-eye-open"></span> Chercher
                        </button>
                    </form>
                </nav>
            </div>
        </section>
    </header>

    <section id="day">
        <div class="container">
            <div class=row>
                <div class="col-sm-6"> <!-- Les tableaux mesure SPS -->
                    <h4> Consommation - production quotidiennes</h4>

                    <form action="iframe.php" method="GET" onSubmit='$.colorbox({ opacity:1, width:"1000px", height:"300px", iframe:false, href:"iframe.php?" + $(this).serialize()}); return false;'>
                        <?php
                        include 'html-php-date-picker.php';
                        ?>
                        <input type="submit" name="submit" id="submit" value="Choisir" />
                    </form>
                </div>
            </div>
        </div>
    </section>

    <section id="month">
        <div class="container">
            <div class=row>
                <div class="col-sm-6">
                    <h4> Consommation - production mensuelles</h4>

                    <form action="iframe.php" method="GET" onSubmit='$.colorbox({ opacity:1, width:"1000px", height:"300px", iframe:false, href:"iframe.php?" + $(this).serialize()}); return false;'>
                        <?php
                        include 'html-php-month-picker.php';
                        ?>
                        <input type="submit" name="submit" id="submit" value="Choisir" />
                    </form>
                </div>
            </div>
        </div>
    </section>

    <section id="year">
        <div class="container">
            <div class=row>
                <div class="col-sm-6">
                    <h4> Consommation - production annuelles</h4>

                    <form action="iframe.php" method="GET" onSubmit='$.colorbox({ opacity:1, width:"1000px", height:"300px", iframe:false, href:"iframe.php?" + $(this).serialize()}); return false;'>
                        <?php
                        include 'html-php-year-picker.php';
                        ?>
                        <input type="submit" name="submit" id="submit" value="Choisir" />
                    </form>
                </div>
            </div>
        </div>
    </section>

    <!-- Footer -->
    <footer class="footer text-center" class="row col-sm-12">
        <div class="panel panel-body">
            </br> </br>
            <p><i class="fa fa-mobile fa-1x">:(+261) 32 71 425 26 /034 061 55 44 </i></p>
            <p><i class="fa fa-envelope-o fa-1x">:majika.madagascar@gmail.com</i></p>
            <p><i class="fa fa-cogs fa-1x">:majiakasolution.org</i></p>
            <p><i class="fa fa-facebook fa-1x">:Majikasolutions</i></p>
            </br>
            <p>Copyright Majika - Tous droits réservés <br/><br/>
        </div>
    </footer>
</div>



</body>

</html>