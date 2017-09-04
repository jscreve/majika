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
    private static final Logger logger = LogManager.getLogger();
    private String pathDir;
    private Date date;
    private SimpleDateFormat month;
    private SimpleDateFormat df;
    static final int BUFFER = 2048;
    private Properties prop = new Properties();
    public static void FilePath() {
    }


    public String setZipPath() {
        String dir;
        try {
            InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties");
            prop.load(input); // open the connection to the properties file
            pathDir = prop.getProperty("pathDirFile");
            date = new Date();
            month = new SimpleDateFormat("dd_MM_yyyy");
            //Path pour créer un dossier chaque mois

            df = new SimpleDateFormat("MM_yyyy");
            dir = pathDir + df.format(date).toString() + separateur + month.format(date).toString();

            final Path sourceDir = Paths.get(dir);
            String zipFileName = dir.concat(".zip");
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
                logger.error("zip",e);
            }
            input.close(); // close the connection to the properties file
        }catch(IOException i){logger.error("Properties",i);}
        return pathDir + df.format(date).toString() + separateur + month.format(date).toString() + ".zip";
    }
}