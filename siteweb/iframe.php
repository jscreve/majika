<?php
include("./library/class/pData.class.php");
include("./library/class/pDraw.class.php");
include("./library/class/pImage.class.php");
include("./library/class/pPie.class.php");
include("./library/class/pIndicator.class.php");

function extractCsv($day, $month, $year)
{
    //extract zip archive
    $zip = new ZipArchive;
    $folderName = $month . "_" . $year;
    $day = sprintf('%02d', $day);
    $zipFileName = $day . "_" . $month . "_" . $year;
    $csvFileName = "UPS_" . $day . "_" . $month . "_" . $year;
    $zipFile = "../majika/CsvZip/" . $folderName . "/" . $zipFileName . ".zip";
    $csvFile = "../majika/CsvZip/" . $folderName . "/" . $csvFileName . ".csv";
    $extractFolder = "../majika/CsvZip/" . $folderName . "/";
    if(file_exists($csvFile) === TRUE) {
        return $csvFile;
    } else {
        if ($zip->open($zipFile) === TRUE) {
            if (is_writeable($extractFolder)) {
                $success = $zip->extractTo($extractFolder);
                $zip->close();
                if ($success === TRUE) {
                    return $csvFile;
                }
            }
        }
    }
    return null;
}

function extractCsvMonth($month, $year)
{
    for($day = 1; $day <= 31; $day++) {
        $csvFiles[$day] = extractCsv($day, $month, $year);
    }
    return $csvFiles;
}

function extractCsvYear($year)
{
    for($month = 1; $month <=12; $month++) {
        $csvFiles[$month] = extractCsvMonth($month, $year);
    }
    return $csvFiles;
}

function computePowerValues($csvFile)
{
    //extract data
    $line = 1; // compteur de ligne
    $fic = fopen($csvFile, "a+");
    while ($tab = fgetcsv($fic, 1024, ';')) {
        if(isset($tab[1])) {
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
    }
    return array("time" => $timeValues, "power" => $powerValues, "usage" => $powerUsage);
}

function computeAveragedDisplayedValuesPerDay($csvFiles, $dataKey)
{
    for($day = 1; $day <=31; $day++) {
        if($csvFiles[$day] != null) {
            $data = computePowerValues($csvFiles[$day]);
            if ($data != null) {
                //average the value through the day
                $average[$day] = array_sum($data[$dataKey]) / count($data[$dataKey]);
            } else {
                $average[$day] = 0;
            }
        } else {
            $average[$day] = 0;
        }
    }
    return $average;
}

function computeAveragedDisplayedValuesPerMonth($csvFiles, $dataKey)
{
    for($month = 1; $month <=12; $month++) {
        if($csvFiles[$month] != null) {
            $data = computeAveragedDisplayedValuesPerDay($csvFiles[$month], $dataKey);
            if ($data != null) {
                //average the value through the day
                $average[$month] = array_sum($data) / count($data);
            } else {
                $average[$month] = 0;
            }
        } else {
            $average[$month] = 0;
        }
    }
    return $average;
}

function computeAverageEnergyPerMonth($average)
{
    for($day = 1; $day <=31; $day++) {
        if($average[$day] != null) {
            $average[$day] = $average[$day] * 24 /1000;
        } else {
            $average[$day] = 0;
        }
    }
    return $average;
}

function computeAverageEnergyPerYear($average)
{
    for($month = 1; $month <=12; $month++) {
        if($average[$month] != null) {
            $average[$month] = $average[$month] * 24 * 30 /1000;
        } else {
            $average[$month] = 0;
        }
    }
    return $average;
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

    $DataSet->addPoints($powerValues, "Puissance solaire");
    $DataSet->setSerieOnAxis("Puissance solaire", 0);
    $DataSet->setAxisName(0, "Puissance solaire");
    $DataSet->setAxisUnit(0, "W");

    $DataSet->addPoints($powerUsage, "Consommation");
    $DataSet->setSerieOnAxis("Consommation", 1);
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
    $myPicture->drawLegend(475,25,array("Style"=>LEGEND_NOBORDER,"Mode"=>LEGEND_HORIZONTAL));
    $myPicture->drawLineChart();
    $myPicture->Render("graph.png");
}

function displayMonthBarChart($powerValues, $powerUsage)
{
    //display data in a graph
    $DataSet = new pData;
    $DataSet->addPoints(range(1,31),"Labels", "Labels");
    $DataSet->setSerieDescription("Labels", "Jour");
    $DataSet->setAbscissa("Labels");
    $DataSet->setXAxisName("Jour");

    $DataSet->addPoints($powerValues, "Puissance solaire");
    $DataSet->setSerieOnAxis("Puissance solaire", 0);
    $DataSet->setAxisName(0, "Puissance solaire");
    $DataSet->setAxisUnit(0, "kWh");

    $DataSet->addPoints($powerUsage, "Consommation");
    $DataSet->setSerieOnAxis("Consommation", 1);
    $DataSet->setAxisName(1, "Consommation");
    $DataSet->setAxisUnit(1, "kVAh");

    $myPicture = new pImage(900, 230, $DataSet);
    $myPicture->setFontProperties(array("FontName" => "./library/fonts/Forgotte.ttf", "FontSize" => 15));
    $myPicture->setGraphArea(190, 40, 900, 190);
    $AxisBoundaries = array(0 => array("Min" => 0, "Max" => 200), 1 => array("Min" => 0, "Max" => 100));
    $scaleSettings = array(
        "LabelingMethod" => LABELING_DIFFERENT, "XMargin" => 10, "YMargin" => 0,
        "DrawSubTicks" => TRUE, "CycleBackground" => TRUE, "Mode" => SCALE_MODE_MANUAL, "ManualScale" => $AxisBoundaries);
    $myPicture->drawScale($scaleSettings);
    $myPicture->drawLegend(475,25,array("Style"=>LEGEND_NOBORDER,"Mode"=>LEGEND_HORIZONTAL));
    $myPicture->drawBarChart();
    $myPicture->Render("graph-month.png");
}

function displayYearBarChart($powerValues, $powerUsage)
{
    //display data in a graph
    $DataSet = new pData;
    $DataSet->addPoints(array('Jan', 'Fev', 'Mars', 'Avr', 'Mai', 'Juin', 'Juil', 'Aôut', 'Sept', 'Oct', 'Nov', 'Déc'),"Labels", "Labels");
    $DataSet->setSerieDescription("Labels", "Mois");
    $DataSet->setAbscissa("Labels");
    $DataSet->setXAxisName("Mois");

    $DataSet->addPoints($powerValues, "Puissance solaire");
    $DataSet->setSerieOnAxis("Puissance solaire", 0);
    $DataSet->setAxisName(0, "Puissance solaire");
    $DataSet->setAxisUnit(0, "kWh");

    $DataSet->addPoints($powerUsage, "Consommation");
    $DataSet->setSerieOnAxis("Consommation", 1);
    $DataSet->setAxisName(1, "Consommation");
    $DataSet->setAxisUnit(1, "kVAh");

    $myPicture = new pImage(900, 230, $DataSet);
    $myPicture->setFontProperties(array("FontName" => "./library/fonts/Forgotte.ttf", "FontSize" => 15));
    $myPicture->setGraphArea(190, 40, 900, 190);
    $AxisBoundaries = array(0 => array("Min" => 0, "Max" => 3000), 1 => array("Min" => 0, "Max" => 1500));
    $scaleSettings = array(
        "LabelingMethod" => LABELING_DIFFERENT, "XMargin" => 30, "YMargin" => 0,
        "DrawSubTicks" => TRUE, "CycleBackground" => TRUE, "Mode" => SCALE_MODE_MANUAL, "ManualScale" => $AxisBoundaries);
    $myPicture->drawScale($scaleSettings);
    $myPicture->drawLegend(475,25,array("Style"=>LEGEND_NOBORDER,"Mode"=>LEGEND_HORIZONTAL));
    $myPicture->drawBarChart();
    $myPicture->Render("graph-year.png");
}
?>
<!DOCTYPE html>

<html>
<body>
<div class="container" id="block_page">  <!-- En-tête du page -->
    <?php
    $year = (isset($_GET['year']) ? $_GET['year'] : null);
    $month = (isset($_GET['month']) ? $_GET['month'] : null);
    $day = (isset($_GET['day']) ? $_GET['day'] : null);
    ?>
    <?php
    if($year != null && $month != null && $day != null) {
        $csvFile = extractCsv($day, $month, $year);
        if ($csvFile != null) {
            $data = computePowerValues($csvFile);
            displayGraph($data["time"], $data["power"], $data["usage"]);
            echo '<h4>Jour : ' . $day . ' Mois : ' . $month . ' Année : ' . $year . '</h4>';
            echo '<div class="container" id="block_page"><img src="graph.png" /></div>';
        } else {
            echo '<h4>Pas de données pour cette date</h4>';
        }
    }
    ?>

    <?php
    $year = (isset($_GET['year_month']) ? $_GET['year_month'] : null);
    $month = (isset($_GET['month_month']) ? $_GET['month_month'] : null);
    ?>
    <?php
    if($year != null && $month != null) {
        $csvFiles = extractCsvMonth($month, $year);
        if ($csvFiles != null) {
            $dataPower = computeAverageEnergyPerMonth(computeAveragedDisplayedValuesPerDay($csvFiles, "power"));
            $dataUsage = computeAverageEnergyPerMonth(computeAveragedDisplayedValuesPerDay($csvFiles, "usage"));
            displayMonthBarChart($dataPower, $dataUsage);
            echo '<h4>Mois : ' . $month . ' Année : ' . $year . '</h4>';
            echo '<div class="container" id="block_page"><img src="graph-month.png" /></div>';
        } else {
            echo '<h4>Pas de données pour cette date</h4>';
        }
    }
    ?>
    <?php
    $year = (isset($_GET['year_year']) ? $_GET['year_year'] : null);
    ?>
    <?php
    if($year != null) {
        $csvFiles = extractCsvYear($year);
        if ($csvFiles != null) {
            $dataPower = computeAverageEnergyPerYear(computeAveragedDisplayedValuesPerMonth($csvFiles, "power"));
            $dataUsage = computeAverageEnergyPerYear(computeAveragedDisplayedValuesPerMonth($csvFiles, "usage"));
            displayYearBarChart($dataPower, $dataUsage);
            echo '<h4>Année : ' . $year . '</h4>';
            echo '<div class="container" id="block_page"><img src="graph-year.png" /></div>';
        } else {
            echo '<h4>Pas de données pour cette date</h4>';
        }
    }
    ?>
</div>
</body>
</html>

</body>

</html>