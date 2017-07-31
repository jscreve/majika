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
    if(LOGGING)\
      Logger.log('documentURL :' + documentURL);\
    if(documentURL !== null && documentURL !== '') \{\
      if(LOGGING)\
        Logger.log('Following client bill is already existing, cannot modify it afterwards :' + clientReference);\
      sendErrorMail(clientReference);\
      return -1;\
    \}\
  \}\
  var oldPaidAmount = getCellByKey('Reference', 'Paid', clientReference, clientAccountSheet, clientAccountSheetValues);\
  var rowRange;\
  var rowValues;\
  //client reference cannot be found\
  if(oldPaidAmount == null) \{\
    copyLastRow(clientAccountSheet); \
 \
    rowRange = getColumnRangeFromIndex(clientAccountSheet, clientAccountSheet.getLastRow() - 1);\
    rowValues = rowRange.getValues();\
    clientAccountSheetValues = clientAccountSheet.getDataRange().getValues();\
    \
    //fetch previous total amount if any for previous month\
    var previousMonthSold = getPreviousMonthSoldAccount(clientReference, month, year);\
    if(previousMonthSold == null) \{\
      previousMonthSold = 0;\
    \}\
    updateCellByKeyOnColumn('Previous Sold', clientAccountSheet, clientAccountSheetValues, rowValues, previousMonthSold);\
\
    //update client reference\
    updateCellByKeyOnColumn('Reference', clientAccountSheet, clientAccountSheetValues, rowValues, clientReference);\
    \
    //update amount due\
    //fetch amount due from bill if any\
    var amountDue = 0;\
    if(billForMonthSheetValues != null) \{\
      amountDue = getCellByKey('Reference', 'TotalPrice', clientReference, billForMonthSheet, billForMonthSheetValues);\
    \}\
    updateCellByKeyOnColumn('Due', clientAccountSheet, clientAccountSheetValues, rowValues, amountDue);\
    \
    //update paid amount to 0\
    oldPaidAmount = 0;\
    updateCellByKeyOnColumn('Paid', clientAccountSheet, clientAccountSheetValues, rowValues, oldPaidAmount);\
    \
    //refresh client account sheet values after update\
    rowRange.setValues(rowValues);\
    clientAccountSheetValues = clientAccountSheet.getDataRange().getValues();\
  \} else \{\
    Logger.log('getting old paid amount');\
    oldPaidAmount = parseInt(oldPaidAmount);\
    rowRange = getColumnRangeFromReference('Reference', clientReference, clientAccountSheet, clientAccountSheetValues);\
    rowValues = rowRange.getValues();\
  \}\
  \
  //get amount due\
  var amountDue = parseInt(getCellByKey('Reference', 'Due', clientReference, clientAccountSheet, clientAccountSheetValues));\
  if(LOGGING)\
    Logger.log('amountDue : ' + amountDue);\
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
  updateCellByKeyOnColumn('Paid', clientAccountSheet, clientAccountSheetValues, rowValues, paidAmount);\
  \
  //update total sold\
  var total = amountDue+previousSold-paidAmount;\
  if(LOGGING)\
    Logger.log('Total : ' + total);\
  updateCellByKeyOnColumn('Total', clientAccountSheet, clientAccountSheetValues, rowValues, total);\
  \
  rowRange.setValues(rowValues);\
  \
  if(billForMonthSheet != null) \{\
    var billForMonthRange = getColumnRangeFromReference('Reference', clientReference, billForMonthSheet, billForMonthSheetValues);\
    var billForMonthRangeValues = billForMonthRange.getValues();\
    var billForMonthRangeFormulas = billForMonthRange.getFormulas();\
    //update paid amount on bill\
    updateCellByKeyOnColumn('Paid', billForMonthSheet, billForMonthSheetValues, billForMonthRangeValues, paidAmount);\
    //update previous sold on bill\
    updateCellByKeyOnColumn('Previous Sold', billForMonthSheet, billForMonthSheetValues, billForMonthRangeValues, previousSold);\
    saveRangeValues(billForMonthRange, billForMonthRangeValues, billForMonthRangeFormulas);\
  \}\
\}\
\
\
\
\
\
}