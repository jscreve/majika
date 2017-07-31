{\rtf1\ansi\ansicpg1252\cocoartf1504\cocoasubrtf830
{\fonttbl\f0\fswiss\fcharset0 Helvetica;}
{\colortbl;\red255\green255\blue255;}
{\*\expandedcolortbl;;}
\paperw11900\paperh16840\margl1440\margr1440\vieww10800\viewh8400\viewkind0
\pard\tx566\tx1133\tx1700\tx2267\tx2834\tx3401\tx3968\tx4535\tx5102\tx5669\tx6236\tx6803\pardirnatural\partightenfactor0

\f0\fs24 \cf0 function onEdit(event) \{\
  var lock = LockService.getScriptLock();\
  Logger.log(event);\
  try \{\
    //attempt to lock for 10 secs then \
    lock.tryLock(10000);\
    if (!lock.hasLock()) \{\
      //abnormal termination\
      Logger.log('Could not obtain lock after 10 seconds, processing anyway');\
      lock.releaseLock();\
      lock.tryLock(2000);\
    \}\
    //check event source\
    if(event.namedValues && event.namedValues['R\'e9f\'e9rence client ?']) \{\
      if(LOGGING)\
        Logger.log('Manage counting');\
      manageCounting(event);\
    \} else if(event.namedValues && event.namedValues['Quel est votre nom ?']) \{\
      if(LOGGING)\
        Logger.log('Manage new client');\
      manageNewClient(event);\
    \} else \{\
      if(LOGGING)\
        Logger.log('Manage payment');\
      managePayment(event);\
    \}  \
  \} finally \{\
    lock.releaseLock();\
  \}\
\}\
\
function importPayment() \{\
  var fSource = DriveApp.getFolderById(IMPORT_FOLDER_ID);\
  var fi = fSource.getFilesByName('payment.csv');\
  var ss = SpreadsheetApp.openById(SHEET_ID);\
  SpreadsheetApp.setActiveSpreadsheet(ss);\
  var currentMonth = new Date().getMonth();\
  var countingDate = new Date();\
  if(fi.hasNext()) \{\
    var file = fi.next();\
    var csv = file.getBlob().getDataAsString();\
    var csvData = CSVToArray(csv, ';');        \
    for (var i = 3, lenCsv=csvData.length; i<lenCsv; i++ ) \{\
      var event = \{\};\
      event.values = [new Date(), csvData[i][1], csvData[i][currentMonth+2], Utilities.formatDate(countingDate, "EAT", "MM/dd/yyyy")];\
      if(event.values[2] == null || event.values[2].length === 0) \{\
        event.values[2] = "0";\
      \} else \{\
        //trim spaces\
        event.values[2] = event.values[2].replace(/\\s/g, "");\
        Logger.log('payment read ; ' + event.values[2]);\
      \}\
      managePayment(event);\
    \}\
  \}\
\};\
\
function importCounterData() \{\
  var t1 = new Date().getTime();\
  \
  var fSource = DriveApp.getFolderById(IMPORT_FOLDER_ID);\
  var fi = fSource.getFilesByName('counter.csv');\
  var ss = SpreadsheetApp.openById(SHEET_ID);\
  SpreadsheetApp.setActiveSpreadsheet(ss);\
  var currentMonth = new Date().getMonth();\
  var countingDate = new Date();\
  countingDate.setDate(20);\
  \
  var contactSheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName(CLIENT_CONTACT_SHEET);\
  var contactSheetValues = contactSheet.getDataRange().getValues();\
  \
  if(fi.hasNext()) \{\
    var file = fi.next();\
    var csv = file.getBlob().getDataAsString();\
    var csvData = CSVToArray(csv, ';');\
    for (var i= 3, lenCsv=csvData.length; i<lenCsv; i++ ) \{\
      var event = \{\};\
      event.values = [new Date(), csvData[i][1], csvData[i][currentMonth+2], 'Non', Utilities.formatDate(countingDate, "EAT", "MM/dd/yyyy")];\
      if(event.values[2] !== null && event.values[2].length !== 0) \{\
        manageCounting(event, contactSheet, contactSheetValues);\
      \}\
    \}    \
  \}\
  \
  var t2 = new Date().getTime();\
  \
  Logger.log('Total time : ' + (t2-t1)/1000);\
\};\
}