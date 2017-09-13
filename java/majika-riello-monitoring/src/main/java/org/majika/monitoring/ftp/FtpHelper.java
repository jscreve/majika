package org.majika.monitoring.ftp;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.Properties;
import java.util.Scanner;

public class FtpHelper {

    private static Logger logger = org.apache.logging.log4j.LogManager.getLogger(FtpHelper.class);

    public static boolean storeRemoteFile(FTPClient ftpClient, String fileContent, String remoteFileName) throws IOException {
        return ftpClient.storeFile(remoteFileName, new ByteArrayInputStream(fileContent.getBytes("UTF-8")));
    }

    public static String getRemoteFileContent(FTPClient ftpClient, String remoteFileName) throws IOException{
        InputStream is = ftpClient.retrieveFileStream(remoteFileName);
        String result = convertInputStreamToString(is);
        return result;
    }

    public static boolean removeRemoteFile(FTPClient ftpClient, String remoteFileName) throws IOException{
        return ftpClient.deleteFile(remoteFileName);
    }

    public static FTPClient connectToFTP(Properties properties) throws IOException {
        String ftpServer = properties.getProperty("ftpServer");
        String ftpUser = properties.getProperty("ftpUser");
        String ftpPass = properties.getProperty("ftpPass");
        return connectToFTP(ftpServer, ftpUser, ftpPass);
    }

    public static boolean isSubDirectory(FTPClient ftpClient, String parent, String child) throws IOException {
        FTPFile[] files = ftpClient.listDirectories(parent);
        for(FTPFile file : files) {
            if(file.getName().equals(child)) {
                return true;
            }
        }
        return false;
    }

    public static FTPClient connectToFTP(String ftpServer, String ftpUser, String ftpPass) throws IOException {
        FTPClient ftpClient = new FTPClient();
        int port = 21;
        ftpClient.connect(ftpServer, port);
        ftpClient.login(ftpUser, ftpPass);
        ftpClient.enterLocalPassiveMode();
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        return ftpClient;
    }

    public static void disconnectFTP(FTPClient ftpClient) {
        try {
            if (ftpClient != null && ftpClient.isConnected()) {
                ftpClient.logout();
                ftpClient.disconnect();
            }
        } catch (IOException ex) {
            logger.error("Error in ftp disconnection", ex);
        }
    }

    private static String convertInputStreamToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        String result = s.hasNext() ? s.next() : "";
        return result;
    }
}
