{\rtf1\ansi\ansicpg1252\cocoartf1504\cocoasubrtf830
{\fonttbl\f0\fswiss\fcharset0 Helvetica;}
{\colortbl;\red255\green255\blue255;}
{\*\expandedcolortbl;;}
\paperw11900\paperh16840\margl1440\margr1440\vieww10800\viewh8400\viewkind0
\pard\tx566\tx1133\tx1700\tx2267\tx2834\tx3401\tx3968\tx4535\tx5102\tx5669\tx6236\tx6803\pardirnatural\partightenfactor0

\f0\fs24 \cf0 function makeCopy() \{\
\
  // generates the timestamp and stores in variable formattedDate as year-month-date hour-minute-second\
  var timeZone = Session.getScriptTimeZone();\
  var formattedDate = Utilities.formatDate(new Date(), timeZone, "yyyy-MM-dd' 'HH:mm:ss");\
  \
  // gets the name of the original file and appends the word "copy" followed by the timestamp stored in formattedDate\
  var name = SpreadsheetApp.getActiveSpreadsheet().getName() + " Copy " + formattedDate;\
  \
  // gets the destination folder by their ID\
  var source = DriveApp.getFolderById("0B6q_0j86VQHiOUp2WWxxWGd6NG8");\
  var destination = DriveApp.getFolderById("0B6q_0j86VQHiSWtfTFpMTWM4TUE");\
  \
  // gets the current Google Sheet file\
  var file = DriveApp.getFileById(SpreadsheetApp.getActiveSpreadsheet().getId())\
  \
  // makes copy of "file" with "name" at the "destination"\
  file.makeCopy(name, destination);\
  \
  //move form copy (this is a bug, we should not to that)\
  moveCopiedForms(source, destination);\
  \
  // send excel sheet by mail\
  var config = \{\
    to:EMAIL_MAJIKA,\
    subject:'Backup facturation',\
    body: 'Voici le backup'\
  \};\
  emailAsExcel(config);\
\}\
\
function moveCopiedForms(sourceFolder, destinationFolder) \{\
  var files = sourceFolder.getFilesByType('application/vnd.google-apps.form');\
  Logger.log('Moving form files, files ' + files);\
  while (files.hasNext()) \{\
    var file = files.next();\
    Logger.log('File : ' + file.getName());\
    //only move copied forms\
    if(file.getName().indexOf('Copy') != -1) \{\
      Logger.log('Moving : ' + file.getName());\
      destinationFolder.addFile(file);\
      sourceFolder.removeFile(file);\
    \}\
  \}\
\}\
\
/**\
 * Thanks to a few answers that helped me build this script\
 * Explaining the Advanced Drive Service must be enabled: http://stackoverflow.com/a/27281729/1385429\
 * Explaining how to convert to a blob: http://ctrlq.org/code/20009-convert-google-documents\
 * Explaining how to convert to zip and to send the email: http://ctrlq.org/code/19869-email-google-spreadsheets-pdf\
 */\
function emailAsExcel(config) \{\
  if (!config || !config.to || !config.subject || !config.body) \{\
    throw new Error('Configure "to", "subject" and "body" in an object as the first parameter');\
  \}\
\
  var spreadsheet   = SpreadsheetApp.getActiveSpreadsheet();\
  var spreadsheetId = spreadsheet.getId()\
  var file          = Drive.Files.get(spreadsheetId);\
  var url           = file.exportLinks[MimeType.MICROSOFT_EXCEL];\
  var token         = ScriptApp.getOAuthToken();\
  var response      = UrlFetchApp.fetch(url, \{\
    headers: \{\
      'Authorization': 'Bearer ' +  token\
    \}\
  \});\
\
  var fileName = (config.fileName || spreadsheet.getName()) + '.xlsx';\
  var blobs   = [response.getBlob().setName(fileName)];\
  if (config.zip) \{\
    blobs = [Utilities.zip(blobs).setName(fileName + '.zip')];\
  \}\
  GmailApp.sendEmail(\
    config.to,\
    config.subject,\
    config.body,\
    \{\
      attachments: blobs\
    \}\
  );\
\}}