{\rtf1\ansi\ansicpg1252\cocoartf1504\cocoasubrtf830
{\fonttbl\f0\fswiss\fcharset0 Helvetica;}
{\colortbl;\red255\green255\blue255;}
{\*\expandedcolortbl;;}
\paperw11900\paperh16840\margl1440\margr1440\vieww10800\viewh8400\viewkind0
\pard\tx566\tx1133\tx1700\tx2267\tx2834\tx3401\tx3968\tx4535\tx5102\tx5669\tx6236\tx6803\pardirnatural\partightenfactor0

\f0\fs24 \cf0 \
function managePayment(event) \{\
  var clientReference = event.values[1].toUpperCase();\
  var paidAmount = parseInt(event.values[2]);\
  var date = new Date(event.values[3]);\
  var year = date.getYear();\
  var month = date.getMonth();\
  var billSheetName = generateMonthYearString(month, year);\
  var billForMonthSheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName(billSheetName);\
  var billForMonthSheetValues = null;\
  if(billForMonthSheet != null) \{\
    billForMonthSheetValues = billForMonthSheet.getDataRange().getValues();\
  \}\
  sheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName('CAJuin2017');\
  Logger.log('sheet 2 : ' + sheet);\
\
  var out = getOrCreateAccountSheet(month, year);\
  var clientAccountSheet = out.sheet;\
  var isNewSheet = out.isNewSheet;\
  var clientAccountSheetValues = clientAccountSheet.getDataRange().getValues();\
  \
  //send error mail if client does not exist\
  if(checkClient(clientReference) < 0) \{\
    sendErrorMail(clientReference);\
    return -1;\
  \}\
\
  //send error mail if bill is already filled up for this client\
  if(billForMonthSheetValues != null) \{\
    var documentURL = getCellByKey('Reference', 'Document URL', clientReference, billForMonthSheet, billForMonthSheetValues);\
    Logger.log('documentURL :' + documentURL);\
    if(documentURL !== null && documentURL !== '') \{\
      if(LOGGING)\
        Logger.log('Following client bill is already existing, cannot modify it afterwards :' + clientReference);\
      sendErrorMail(clientReference);\
      return -1;\
    \}\
  \}\
    \
  var oldPaidAmount = getCellByKey('Reference', 'Paid', clientReference, clientAccountSheet, clientAccountSheetValues);\
  //client reference cannot be found\
  if(oldPaidAmount == null) \{\
    copyLastRow(clientAccountSheet); \
    clientAccountSheetValues = clientAccountSheet.getDataRange().getValues();\
    \
    //fetch previous total amount if any for previous month\
    var previousMonthSold = getPreviousMonthSoldAccount(clientReference, month, year);\
    if(previousMonthSold == null) \{\
      previousMonthSold = 0;\
    \}\
    var clientRefCell = clientAccountSheet.getRange(clientAccountSheet.getLastRow(), getColumnIndexByName(clientAccountSheet.getName(), 'Previous Sold', clientAccountSheetValues) + 1);\
    clientRefCell.setValue(previousMonthSold);\
    //update client reference\
    clientRefCell = clientAccountSheet.getRange(clientAccountSheet.getLastRow(), getColumnIndexByName(clientAccountSheet.getName(), 'Reference', clientAccountSheetValues) + 1);\
    clientRefCell.setValue(clientReference);   \
    \
    //update amount due\
    var clientRefCell = clientAccountSheet.getRange(clientAccountSheet.getLastRow(), getColumnIndexByName(clientAccountSheet.getName(), 'Due', clientAccountSheetValues) + 1);\
    //fetch amount due from bill if any\
    var amountDue = 0;\
    if(billForMonthSheetValues != null) \{\
      amountDue = getCellByKey('Reference', 'TotalPrice', clientReference, billForMonthSheet, billForMonthSheetValues);\
    \}\
    clientRefCell.setValue(amountDue);\
    \
    //update paid amount to 0\
    oldPaidAmount = 0;\
    var clientRefCell = clientAccountSheet.getRange(clientAccountSheet.getLastRow(), getColumnIndexByName(clientAccountSheet.getName(), 'Paid', clientAccountSheetValues) + 1);\
    clientRefCell.setValue(oldPaidAmount);\
  \} else \{\
    oldPaidAmount = parseInt(oldPaidAmount);\
  \}\
  \
  //refresh client account sheet values after update\
  clientAccountSheetValues = clientAccountSheet.getDataRange().getValues();\
  \
  //get amount due\
  var amountDue = parseInt(getCellByKey('Reference', 'Due', clientReference, clientAccountSheet, clientAccountSheetValues));\
  if(LOGGING) \{\
    Logger.log('amountDue : ' + amountDue);\
  \}\
    \
  //get previous sold\
  var previousSold = parseInt(getCellByKey('Reference', 'Previous Sold', clientReference, clientAccountSheet, clientAccountSheetValues));\
  if(LOGGING)\
    Logger.log('previousSold : ' + previousSold);\
  \
  //update paid amount\
  var paidAmount = oldPaidAmount + paidAmount;\
  if(LOGGING)\
    Logger.log('paidAmount : ' + paidAmount);\
  updateCellByKey('Reference', 'Paid', clientReference, paidAmount, clientAccountSheet, clientAccountSheetValues);  \
  \
  //update total sold\
  var total = amountDue+previousSold-paidAmount;\
  if(LOGGING)\
    Logger.log('Total : ' + total);\
  updateCellByKey('Reference', 'Total', clientReference, total, clientAccountSheet, clientAccountSheetValues);  \
  \
  if(billForMonthSheet != null) \{\
    //update paid amount on bill\
    updateCellByKey('Reference', 'Paid', clientReference, paidAmount, billForMonthSheet, billForMonthSheetValues);  \
    //update previous sold on bill\
    updateCellByKey('Reference', 'Previous Sold', clientReference, previousSold, billForMonthSheet, billForMonthSheetValues);\
  \}\
\}\
\
\
\
\
\
}