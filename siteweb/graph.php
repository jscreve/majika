<?php
include("./library/class/pData.class.php");
include("./library/class/pDraw.class.php");
include("./library/class/pImage.class.php");
include("./library/class/pPie.class.php");
include("./library/class/pIndicator.class.php");
?>
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
</head>
<body>
<form method="POST" action="graph.php">
    <?php
    include 'html-php-date-picker.php';
    ?><input type="submit" value="Choisir" name="choisir">
    <?php
    $year = (isset($_POST['year']) ? $_POST['year'] : null);
    $month = (isset($_POST['month']) ? $_POST['month'] : null);
    $day = (isset($_POST['day']) ? $_POST['day'] : null);
    ?>

    <?php

    $csvFile = extractCsv($day, $month, $year);
    if($csvFile != null) {
        $data = computeDisplayedValues($csvFile);
        displayGraph($data["time"], $data["power"], $data["usage"]);
        echo '<div class="container" id="block_page"><img src="graph.png" /></div>';
    }

    function extractCsv($day, $month, $year)
    {
        //extract zip archive
        $zip = new ZipArchive;
        $folderName = $month . "_" . $year;
        $zipFileName = $day . "_" . $month . "_" . $year;
        $csvFileName = "UPS_" . $day . "_" . $month . "_" . $year;
        $zipFile = "../majika/CsvZip/" . $folderName . "/" . $zipFileName . ".zip";
        $csvFile = "../majika/CsvZip/" . $folderName . "/" . $csvFileName . ".csv";
        $extractFolder = "../majika/CsvZip/" . $folderName . "/";
        if ($zip->open($zipFile) === TRUE) {
            if (is_writeable($extractFolder)) {
                $success = $zip->extractTo($extractFolder);
                $zip->close();
                if($success === TRUE) {
                    return $csvFile;
                }
            }
        }
        return null;
    }

    function computeDisplayedValues($csvFile)
    {
        //extract data
        $line = 1; // compteur de ligne
        $fic = fopen($csvFile, "a+");
        while ($tab = fgetcsv($fic, 1024, ';')) {
            $line++;
            if ($line % 5 == 0) { //display every 5 lines
                $date = date_parse_from_format('H:0', $tab[1]);
                $timestamp = mktime($date['hour'], $date['minute'], 0, 0, 0, 2000);
                $timeValues[] = $timestamp;
                if (isset($tab[47]) && isset($tab[48]) && isset($tab[49]) && isset($tab[60]) && isset($tab[61]) && isset($tab[62])) {
                    $powerValues[] = $tab[47] + $tab[48] + $tab[49] + $tab[60] + $tab[61] + $tab[62];
                } else {
                    $powerValues[] = 0;
                }
                $powerUsage[] = $tab[9];
            }
        }
        return array("time" => $timeValues, "power" => $powerValues, "usage" => $powerUsage);
    }

    function displayGraph($timeValues, $powerValues, $powerUsage)
    {
        //display data in a graph
        $DataSet = new pData;
        $DataSet->addPoints($timeValues, "Labels");
        $DataSet->setSerieDescription("Labels", "Time");
        $DataSet->setAbscissa("Labels");
        $DataSet->setXAxisName("Time");
        $DataSet->setXAxisDisplay(AXIS_FORMAT_TIME, "H");

        $DataSet->addPoints($powerValues, "Power");
        $DataSet->setSerieOnAxis("Power", 0);
        $DataSet->setAxisName(0, "Puissance solaire");
        $DataSet->setAxisUnit(0, "W");

        $DataSet->addPoints($powerUsage, "Power usage");
        $DataSet->setSerieOnAxis("Power usage", 1);
        $DataSet->setAxisName(1, "Consommation");
        $DataSet->setAxisUnit(1, "VA");

        $myPicture = new pImage(900, 230, $DataSet);
        $myPicture->setFontProperties(array("FontName" => "./library/fonts/Forgotte.ttf", "FontSize" => 15));
        $myPicture->setGraphArea(160, 40, 900, 190);
        $AxisBoundaries = array(0 => array("Min" => 0, "Max" => 15000), 1 => array("Min" => 0, "Max" => 5000));
        $scaleSettings = array(
            "LabelingMethod" => LABELING_DIFFERENT, "XMargin" => 10, "YMargin" => 0,
            "DrawSubTicks" => TRUE, "CycleBackground" => TRUE, "Mode" => SCALE_MODE_MANUAL, "ManualScale" => $AxisBoundaries);
        $myPicture->drawScale($scaleSettings);
        $myPicture->drawLineChart();
        $myPicture->Render("graph.png");
    }
    ?>
</body>

</html>