package org.majika.monitoring.ftpZip;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Zip {

    private static final String separateur = "/";
    private static final Logger logger = LogManager.getLogger(Zip.class);
    private String pathDir;
    private SimpleDateFormat month;
    private SimpleDateFormat df;

    public Zip() {
        try {
            InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties");
            Properties prop = new Properties();
            prop.load(input);
            input.close();
            pathDir = prop.getProperty("pathDirFile");
            if (!pathDir.endsWith("/")) {
                pathDir += "/";
            }
            month = new SimpleDateFormat("dd_MM_yyyy");
            df = new SimpleDateFormat("MM_yyyy");
        } catch(IOException e) {
            logger.error("Error reading properties file", e);
        }
    }

    public String getZipDir(String inputPathDir, Date date) {
        String zipPath = inputPathDir + df.format(date) + separateur + month.format(date);
        return zipPath;
    }

    public String getZipPath(Date date) throws IOException {
        String zipPath = getZipDir(pathDir, date) + ".zip";
        return zipPath;
    }

    /**
     * Cette méthode prend tous les fichiers dans un dossier indiqué depuis la methode FileOutputStream et les compresse dans un zip
     *
     * @return, la methode retourne un String qui est le chemin retourné
     */

    public String getOrCreateZipFile(Date date) throws IOException {
        //Path pour créer un dossier chaque mois
        String zipPath = getZipPath(date);
        File zipFile = new File(zipPath);
        if(!zipFile.exists()) {
            String sourceDirString = pathDir + df.format(date) + separateur + month.format(date);
            final Path sourceDir = Paths.get(sourceDirString);
            String zipFileName = sourceDirString.concat(".zip");
            //Cette partie compresse le fichier .csv dans le dossier indiquer
            try {
                final ZipOutputStream outputStream = new ZipOutputStream(new FileOutputStream(zipFileName));
                Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
                        try {
                            Path targetFile = sourceDir.relativize(file);
                            outputStream.putNextEntry(new ZipEntry(targetFile.toString()));
                            byte[] bytes = Files.readAllBytes(file);
                            outputStream.write(bytes, 0, bytes.length);
                            outputStream.closeEntry();
                        } catch (IOException e) {
                            logger.error(e);
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
                outputStream.close();
            } catch (IOException e) {
                logger.error("zip", e);
            }
        }
        return zipPath;
    }

    public String getPathDir() {
        return pathDir;
    }

    public void setPathDir(String pathDir) {
        this.pathDir = pathDir;
    }

    public SimpleDateFormat getMonth() {
        return month;
    }

    public void setMonth(SimpleDateFormat month) {
        this.month = month;
    }

    public SimpleDateFormat getDf() {
        return df;
    }

    public void setDf(SimpleDateFormat df) {
        this.df = df;
    }


}