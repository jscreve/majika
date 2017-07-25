{\rtf1\ansi\ansicpg1252\cocoartf1504\cocoasubrtf830
{\fonttbl\f0\fswiss\fcharset0 Helvetica;}
{\colortbl;\red255\green255\blue255;}
{\*\expandedcolortbl;;}
\paperw11900\paperh16840\margl1440\margr1440\vieww10800\viewh8400\viewkind0
\pard\tx566\tx1133\tx1700\tx2267\tx2834\tx3401\tx3968\tx4535\tx5102\tx5669\tx6236\tx6803\pardirnatural\partightenfactor0

\f0\fs24 \cf0 function getSSData(ss) \{\
  var sheet = ss.getSheets()[0];\
  var range = sheet.getDataRange();\
  var values = range.getValues();\
  return values;\
\}\
\
function managePaymentFromExcel(event) \{\
  Logger.log(event);\
  \
  var folders = DriveApp.getFoldersByName(BILL_FOLDER);\
  while (folders.hasNext()) \{\
    var folder = folders.next();\
    Logger.log(folder.getName());\
    var sheet = SpreadsheetApp.getActiveSheet();\
    //SpreadsheetApp.openById(''); \
    Logger.log('active sheet : ' + sheet.getName());\
    var files = folder.getFilesByType('application/vnd.google-apps.spreadsheet');\
    while (files.hasNext()) \{\
      var file = files.next();\
      var ss = SpreadsheetApp.open(file);\
      Logger.log(file.getName());\
      \
      // This logs the spreadsheet in CSV format with a trailing comma\
      var values = getSSData(ss);\
      for (var i = 0; i < values.length; i++) \{\
        var row = "";\
        for (var j = 0; j < values[i].length; j++) \{\
          if (values[i][j]) \{\
            row = row + values[i][j];\
          \}\
          row = row + ",";\
        \}\
        Logger.log(row);\
      \}\
    \}\
  \}\
    \
  return 0;\
    \
  var clientReference = event.values[1];\
  var paidAmount = event.values[2];\
  var date = new Date(event.values[0]);\
  var month = date.getMonth();\
  \
  //update paid value for current user\
  var clientBaseSheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName(CLIENTS_ACCOUNTS_SHEET);\
  var clientBaseSheetValues = clientBaseSheet.getDataRange().getValues();\
  \
  //send error mail if client does not exist\
  if(checkClient(clientReference) < 0) \{\
    sendErrorMail(clientReference);\
    return -1;\
  \}\
  \
  var alreadyPaid = getCellByKey('Reference', 'Paid', clientReference, clientBaseSheet, clientBaseSheetValues);\
  \
  //create it if not stored yet for current month\
  if(alreadyPaid == null) \{\
    Logger.log('client not found in current month');\
    //client not found, create a row \
    copyLastRow(clientBaseSheet); \
    clientBaseSheetValues = clientBaseSheet.getDataRange().getValues();\
    //update client reference\
    var clientRefCell = clientBaseSheet.getRange(clientBaseSheet.getLastRow(), getColumnIndexByName(clientBaseSheet.getName(), 'Reference', clientBaseSheetValues) + 1);\
    clientRefCell.setValue(clientReference);\
    //update month\
    Logger.log('Month : ' + parseInt(getColumnIndexByName(clientBaseSheet, 'Month') + 1));\
    Logger.log('Month value : ' + month);\
    clientRefCell = clientBaseSheet.getRange(clientBaseSheet.getLastRow(), getColumnIndexByName(clientBaseSheet.getName(), 'Month', clientBaseSheetValues) + 1);\
    clientRefCell.setValue(month);\
    //update amount due\
    clientRefCell = clientBaseSheet.getRange(clientBaseSheet.getLastRow(), getColumnIndexByName(clientBaseSheet.getName(), 'Due', clientBaseSheetValues) + 1);\
    clientRefCell.setValue(0);\
    //update paid amount\
    clientRefCell = clientBaseSheet.getRange(clientBaseSheet.getLastRow(), getColumnIndexByName(clientBaseSheet.getName(), 'Paid', clientBaseSheetValues) + 1);\
    clientRefCell.setValue(paidAmount);\
  \} else \{\
    paidAmount = parseInt(paidAmount)+parseInt(alreadyPaid);\
    updateCellByKeyAndSecondaryKey('Reference', 'Month', 'Paid', clientReference, month, paidAmount, clientBaseSheet, clientBaseSheetValues);\
  \}\
  \
  //get amount due\
  clientRefCell = clientBaseSheet.getRange(clientBaseSheet.getLastRow(), getColumnIndexByName(clientBaseSheet.getName(), 'Due', clientBaseSheetValues) + 1);\
  var amountDue = clientRefCell.getValue();\
  \
  //update total\
  clientRefCell = clientBaseSheet.getRange(clientBaseSheet.getLastRow(), getColumnIndexByName(clientBaseSheet.getName(), 'Total', clientBaseSheetValues) + 1);\
  clientRefCell.setValue(paidAmount-amountDue);\
\}}