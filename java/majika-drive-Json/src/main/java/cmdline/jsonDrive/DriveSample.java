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

package cmdline.jsonDrive;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.http.FileContent;
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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
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
public class DriveSample {

    /**
     * Be sure to specify the name of your application. If the application name is {@code null} or
     * blank, the application will log a warning. Suggested format is "MyCompany-ProductName/1.0".
     */
    private static final String APPLICATION_NAME = "Majika_Ampasindava_App";
    //################## WARNING, ceci a ete AJOUTE PAR ETIENNE, IL EST POSSIBLE QUE CELA FASSE BUGUER #########
    private static Logger logger = LogManager.getLogger();
    private static Properties prop = new Properties();
    FileInputStream input = new FileInputStream("config.properties");
    //private static final String UPLOAD_FILE_PATH = "/home/pi/test.txt";
    private static FileJson path = new FileJson();
    private static String UPLOAD_FILE_PATH= path.setPath();
    private static final String DIR_FOR_DOWNLOADS = "/Users/etienne/Documents";
    private static java.io.File UPLOAD_FILE = new java.io.File(UPLOAD_FILE_PATH);

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

    public DriveSample() throws IOException {
        Preconditions.checkArgument(
                !UPLOAD_FILE_PATH.startsWith("Enter ") && !DIR_FOR_DOWNLOADS.startsWith("Enter "),
                "Please enter the upload file path and download directory in %s", DriveSample.class);

        FileJson fileJsonCreated = new FileJson();
        fileJsonCreated.setFileJson();

        try {

            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR);
            // authorization
            Credential credential = authorize();
            // set up the global Drive instance
            drive = new Drive.Builder(httpTransport, JSON_FACTORY, credential).setApplicationName(
                    APPLICATION_NAME).build();

            // run commands
            View.header1("Starting Resumable Media Upload");
            //TODO supprimer ou utiliser
            File uploadedFile = uploadFile(false);

            View.header1("Success!");
            try {
                System.in.read();
            } catch (IOException e) {
                logger.error("explication", e);
            }
            return;
        } catch (IOException e) {
            logger.error(e);
        } catch (Throwable t) {
            logger.error(t);
        }


    }

    /**
     * Authorizes the installed application to access user's protected data.
     */
    private static Credential authorize() throws Exception {
        // load client secrets
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
                new InputStreamReader(DriveSample.class.getResourceAsStream("/client_secrets.json")));
        if (clientSecrets.getDetails().getClientId().startsWith("Enter")
                || clientSecrets.getDetails().getClientSecret().startsWith("Enter ")) {
            System.out.println(
                    "Enter Client ID and Secret from https://code.google.com/apis/console/?api=drive "
                            + "into drive-cmdline-sample/src/main/resources/client_secrets.jsonDrive");
            System.exit(1);
        }

        // set up authorization code flow
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY,clientSecrets,
                Collections.singleton(DriveScopes.DRIVE_FILE)).setDataStoreFactory(dataStoreFactory)
                .build();
        // authorize
        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }

    /**
     * Uploads a file using either resumable or direct media upload.
     */
    private static File uploadFile(boolean useDirectUpload) throws IOException {

        //Creation d'un nouveau dossier dans le drive et getID
       /* File fileMetadataFolder = new File(); //Init of the folder creatin
        ArrayList pathFolderDrive = new ArrayList();//creation of the path
        pathFolderDrive.add("0B3OcPk9CLrpYX3NDMlRoWDJDd0U"); // This is the first
        //Those lines were dedicated to the creation and the collect of the ID's folders
        fileMetadataFolder.setName("LOG");
        fileMetadataFolder.setMimeType("application/vnd.google-apps.folder");
        fileMetadataFolder.setParents(pathFolderDrive);
        System.out.println("Folder Id : "+ file.getId() + " Folder Name:  "  + file.getName());*/
        ////////////////////////////// Don't Delete the lines upward, this is for setting up the folder and get the id.
        //TODO mettre en config
        File file = drive.files().get("0B3OcPk9CLrpYd2dtckRra0ptcm8").execute(); //SiteWeb's folder Id

        String pageToken = null;
        do {
            FileList result = drive.files().list()
                    .setQ("mimeType='text/plain'")
                    .setSpaces("drive")
                    .setFields("nextPageToken, files(id, name)")
                    .setPageToken(pageToken)
                    .execute();
            for(File filou: result.getFiles()) {
                if(filou.getName().equals("ups.jsonDrive")) { //only execute this line if the file is named ups.jsonDrive
                    drive.files().delete(filou.getId()).execute();

                }
            }
            pageToken = result.getNextPageToken();
        } while (pageToken != null);
        ////#### End of the lignes added

        File fileMetadata = new File();
        fileMetadata.setName(UPLOAD_FILE.getName());
        fileMetadata.setParents(Collections.singletonList(file.getId())); // this one got added to, this is for setting a parent to the file ups.jsonDrive
        FileContent mediaContent = new FileContent("text/plain", UPLOAD_FILE);
        Drive.Files.Create insert = drive.files().create(fileMetadata, mediaContent);
        MediaHttpUploader uploader = insert.getMediaHttpUploader();
        uploader.setDirectUploadEnabled(useDirectUpload);
        uploader.setProgressListener(new FileUploadProgressListener());
        return insert.execute();
    }
}
