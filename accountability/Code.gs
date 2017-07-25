{\rtf1\ansi\ansicpg1252\cocoartf1504\cocoasubrtf830
{\fonttbl\f0\fswiss\fcharset0 Helvetica;}
{\colortbl;\red255\green255\blue255;}
{\*\expandedcolortbl;;}
\paperw11900\paperh16840\margl1440\margr1440\vieww10800\viewh8400\viewkind0
\pard\tx566\tx1133\tx1700\tx2267\tx2834\tx3401\tx3968\tx4535\tx5102\tx5669\tx6236\tx6803\pardirnatural\partightenfactor0

\f0\fs24 \cf0 function onEdit(event) \{\
  var lock = LockService.getScriptLock();\
  try \{\
    lock.waitLock(10000);\
    Logger.log(event);\
    //check event source\
    if(event.namedValues && event.namedValues['R\'e9f\'e9rence client ?']) \{\
      Logger.log('Manage counting');\
      manageCounting(event);\
    \} else if(event.namedValues && event.namedValues['Quel est votre nom ?']) \{\
      Logger.log('Manage new client');\
      manageNewClient(event);\
    \} else \{\
      Logger.log('Manage payment');\
      managePayment(event);\
    \}  \
  \} catch (e) \{\
    Logger.log('Could not obtain lock after 10 seconds.');\
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
  if(fi.hasNext()) \{\
    var file = fi.next();\
    var csv = file.getBlob().getDataAsString();\
    var csvData = CSVToArray(csv, ';');\
    for (var i=0, lenCsv=csvData.length; i<lenCsv; i++ ) \{\
      var event = \{\};\
      event.values = csvData[i];\
      managePayment(event);\
    \}\
  \}\
\};\
\
function importCounterData() \{\
  var fSource = DriveApp.getFolderById(IMPORT_FOLDER_ID);\
  var fi = fSource.getFilesByName('counter.csv');\
  var ss = SpreadsheetApp.openById(SHEET_ID);\
  SpreadsheetApp.setActiveSpreadsheet(ss);\
  if(fi.hasNext()) \{\
    var file = fi.next();\
    var csv = file.getBlob().getDataAsString();\
    var csvData = CSVToArray(csv, ';');\
    for (var i=0, lenCsv=csvData.length; i<lenCsv; i++ ) \{\
      var event = \{\};\
      event.values = csvData[i];\
      manageCounting(event);\
    \}\
  \}\
\};\
\
\
\
}