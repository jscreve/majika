/*
 * Copyright (c) 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package cmdline.zip;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.media.MediaHttpDownloader;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Preconditions;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Properties;

/**
 * A sample application that runs multiple requests against the Drive API. The requests this sample
 * makes are:
 * <ul>
 * <li>Does a resumable media upload</li>
 * <li>Updates the uploaded file by renaming it</li>
 * <li>Does a resumable media download</li>
 * <li>Does a direct media upload</li>
 * <li>Does a direct media download</li>
 * </ul>
 *
 * @author rmistry@google.com (Ravi Mistry)
 */
public class DriveSampleZip {

    /**
     * Be sure to specify the name of your application. If the application name is {@code null} or
     * blank, the application will log a warning. Suggested format is "MyCompany-ProductName/1.0".
     */
    private static final String APPLICATION_NAME = "Majika_Ampasindava_App";
    //################## WARNING, ceci a ete AJOUTE PAR ETIENNE, IL EST POSSIBLE QUE CELA FASSE BUGUER #########
    private static ZipCreation zipPath = new ZipCreation();
    private static final Logger logger = LogManager.getLogger();
    private static String zipPath2 =zipPath.setZipPath();
    //private static final String UPLOAD_FILE_PATH = "/home/pi/test.txt";

    private static String UPLOAD_FILE_PATH = zipPath2;
    private static final String DIR_FOR_DOWNLOADS = "/Users/etienne/Documents/";
    private static final java.io.File UPLOAD_FILE = new java.io.File(UPLOAD_FILE_PATH);

    /**
     * Directory to store user credentials.
     */
    private static final java.io.File DATA_STORE_DIR =
            new java.io.File(System.getProperty("user.home"), ".store/drive_sample");

    /**
     * Global instance of the {@link DataStoreFactory}. The best practice is to make it a single
     * globally shared instance across your application.
     */
    private static FileDataStoreFactory dataStoreFactory;

    /**
     * Global instance of the HTTP transport.
     */
    private static HttpTransport httpTransport;

    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    /**
     * Global Drive API client.
     */
    private static Drive drive;

    public DriveSampleZip() throws IOException {
        Preconditions.checkArgument(
                !UPLOAD_FILE_PATH.startsWith("Enter ") && !DIR_FOR_DOWNLOADS.startsWith("Enter "),
                "Please enter the upload file path and download directory in %s", DriveSampleZip.class);


        try {
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR);
            // authorization
            Credential credential = authorize();
            // set up the global Drive instance
            drive = new Drive.Builder(httpTransport, JSON_FACTORY, credential).setApplicationName(
                    APPLICATION_NAME).build();

            // run commands
            ViewZip.header1("Starting Resumable Media Upload");
            uploadFile(false);

            ViewZip.header1("Success!");
           //downloadFile(false);
            try {
                System.in.read();
            } catch (IOException e) {
                logger.error("Main Method",e);
            }
            return;
        } catch (IOException e) {
            logger.error("Main Method",e);
        } catch (Throwable t) {
            logger.error("Main Method",t);
        }

        System.exit(1);
    }

    /**
     * Authorizes the installed application to access user's protected data.
     */
    private static Credential authorize() throws Exception {
        // load client secrets
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
                new InputStreamReader(DriveSampleZip.class.getResourceAsStream("/client_secrets.json")));
        if (clientSecrets.getDetails().getClientId().startsWith("Enter")
                || clientSecrets.getDetails().getClientSecret().startsWith("Enter ")) {
            System.out.println(
                    "Enter Client ID and Secret from https://code.google.com/apis/console/?api=drive "
                            + "into drive-cmdline-sample/src/main/resources/client_secrets.json");
            System.exit(1);
        }
        // set up authorization code flow
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets,
                Collections.singleton(DriveScopes.DRIVE_FILE)).setDataStoreFactory(dataStoreFactory)
                .build();
        // authorize
        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }

    /**
     *
     * @param args
     */
    public static void main(String[] args) throws IOException {
            DriveSampleZip zip = new DriveSampleZip();

    }

    /**
     * Uploads a file using either resumable or direct media upload.
     */
    private static File uploadFile(boolean useDirectUpload) throws IOException {
        ///////////////////////////////

        Date dateMonth = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("dd");
        Date dateName = new Date();
        SimpleDateFormat ftN = new SimpleDateFormat("MM_yyyy");

        if (ft.format(dateMonth).toString().equals("01")) {

            File fileMetadataFolder = new File(); //Init of the folder creatin
            ArrayList pathFolderDrive = new ArrayList();//creation of the path
            pathFolderDrive.add("0B3OcPk9CLrpYVUZlZmNxbFVCV0U"); // This is the first
            //Those lines were dedicated to the creation and the collect of the ID's folders
            fileMetadataFolder.setName(ftN.format(dateName).toString());
            fileMetadataFolder.setMimeType("application/vnd.google-apps.folder");
            fileMetadataFolder.setParents(pathFolderDrive);
            drive.files().create(fileMetadataFolder).execute();
        }
        File fileMetadata = new File();
        fileMetadata.setName(UPLOAD_FILE.getName());

        String pageToken = null;
        do {
            FileList result = drive.files().list()
                    .setQ("mimeType='application/vnd.google-apps.folder'")
                    .setSpaces("drive")
                    .setFields("nextPageToken, files(id, name)")
                    .setPageToken(pageToken)
                    .execute();
            for(File filou: result.getFiles()) {
                if(filou.getName().equals(ftN.format(dateMonth).toString())) {
                    fileMetadata.setParents(Collections.singletonList(filou.getId()));
                }
            }
            pageToken = result.getNextPageToken();
        } while (pageToken != null);
        ///////////////////////////////
        FileContent mediaContent = new FileContent("application/zip", UPLOAD_FILE);

        Drive.Files.Create insert = drive.files().create(fileMetadata, mediaContent);
        MediaHttpUploader uploader = insert.getMediaHttpUploader();
        uploader.setDirectUploadEnabled(useDirectUpload);
        uploader.setProgressListener(new FileUploadProgressListenerZip());

        return insert.execute();
    }

    private static void downloadFile(boolean useDirectDownload)
            throws IOException {
        // create parent directory (if necessary)
        java.io.File parentDir = new java.io.File(DIR_FOR_DOWNLOADS);
        if (!parentDir.exists() && !parentDir.mkdirs()) {
            throw new IOException("Unable to create parent directory");
        }
         //SiteWeb's folder Id
        File filou = new File();
        File file = drive.files().get("0B3OcPk9CLrpYd2dtckRra0ptcm8").execute();
        String pageToken = null;
        do {
            FileList result = drive.files().list()
                    .setQ("mimeType='application/java-archive'")
                    .setSpaces("drive")
                    .setFields("nextPageToken, files(id, name)")
                    .setPageToken(pageToken)
                    .execute();
            for(File fil: result.getFiles()) {
                logger.info(fil.getName().substring(0,6));
                if(fil.getName().substring(0,6).equals("majika")) { //only execute this line if the file is named ups.json
                    logger.info("Found it :" +fil.getId() +" Name:" +fil.getName());
                    filou=fil;
                }
            }
            pageToken = result.getNextPageToken();
        } while (pageToken != null);

        OutputStream out = new FileOutputStream(new java.io.File(parentDir,filou.getName()));

        MediaHttpDownloader downloader =
                new MediaHttpDownloader(httpTransport, drive.getRequestFactory().getInitializer());
        downloader.setDirectDownloadEnabled(useDirectDownload);
        downloader.setProgressListener(new FileDownloadProgressListener());
        downloader.download(new GenericUrl(filou.getWebContentLink()),out);
    }


}
