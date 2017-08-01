FOLDER Monitoring_ampasindava ID = 0B3OcPk9CLrpYX3NDMlRoWDJDd0U
SUBFOLDER SiteWeb ID = 0B3OcPk9CLrpYd2dtckRra0ptcm8

Arrivée prevu entre 8H et 11h

Variables et commentaire à changer pour l'installation à Ampasindava:
Installation Ampasindava. Routeur à besoin d'une prise adaptée, le code wifi est :Tulastropecrasecesarceportsalut
connecter le raspberry au routeur et les 3onduleur au routeur. // 10min
-Changer le pathDirFile dans le config.properties
-Changer adresse ip des onduleurs solaire en ://5min
        -ipAdresseSolWest=192.168.0.223
        -ipAdresseSPS=192.168.0.204
        -ipAdresseSolEast=192.168.0.205
-Mettre du crédit sur la clé 3G (#322*6#) // 5min
-Installer la clé 3G Telma (sudo nano /etc/rc.local)//5min
-Tester le programmeArduino sur le raspberry et modifier les Noms et adresses du config.properties //20min
-Installer le prgrm version 1.1 qui est la version sans Arduino
    -Installer les 3 crontabs et envoyer le zip au bout de 15min //25min
    -Envoyer le Json toute les minutes

-Installer le prgrm version 1.2 qui est la version avec Arduino
    -Changer le nom du dossier utilisé juste avant
    -prendre le config.file dans le dossier ArduinoConfigFile
    -Modifier les 3crontabs et envoyer le zip au bout de 15min //25min
            -Si ne fonctionne pas, prendre 1h pour le faire fonctionner, si ne fonctionne toujours pas, passer à la suite. //1h
            -Si tout fonctionne, changer le nom du dossier utilisé juste avant(arduino).

-Debut relevé donnéee prévu entre 9h35 et 13h35 avec actualisation du Json toutes les 10mins
-Prendre une photo de tout le montage

Ideal entre 4 et 6h de donnée
Envoyer en fin de journée 16h ou 17h le zip
Fin des relevé au plus tard à 17h35

crontab -e
* * * * * sudo /pi/CentraleSolaireData/Programmes/
*/10 * * * * sudo /pi/CentraleSolaireData/Programmes/
0 0 * * * sudo /pi/CentraleSolaireData/Programmes/


ACTUALISER LA DATE TOUS LES JOURS CRONTAB ET NTPDATE, il faut internet pour pouvoir faire cette manipulation.
crontab -e
0 0 * * * sudo ntpdate -u pool.ntp.org