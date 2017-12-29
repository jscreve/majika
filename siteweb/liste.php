<?php
$d = dir(".");
echo "Pointeur: ".$d->handle."<br>\n";
echo "Chemin: ".$d->path."<br>\n";
while($entry = $d->read()) {
    echo $entry."<br>\n";
}
$d->close();
?>