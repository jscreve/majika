{\rtf1\ansi\ansicpg1252\cocoartf1504\cocoasubrtf830
{\fonttbl\f0\fswiss\fcharset0 Helvetica;}
{\colortbl;\red255\green255\blue255;}
{\*\expandedcolortbl;;}
\paperw11900\paperh16840\margl1440\margr1440\vieww10800\viewh8400\viewkind0
\pard\tx566\tx1133\tx1700\tx2267\tx2834\tx3401\tx3968\tx4535\tx5102\tx5669\tx6236\tx6803\pardirnatural\partightenfactor0

\f0\fs24 \cf0 \
function manageCounting(event, contactSheet, contactSheetValues) \{\
  \
  var t1 = new Date().getTime();\
  //extract data from event\
  if(LOGGING)\
    Logger.log(event.values);\
  var inputDate = event.values[4];\
  var date;\
  if(inputDate) \{\
    var dateParts = inputDate.split("/");\
    if(LOGGING)\
      Logger.log(dateParts);\
    date = new Date(dateParts[2], dateParts[0] - 1, dateParts[1]);\
  \} else \{\
    date = new Date();\
  \}\
  var formattedDate = Utilities.formatDate(date, "EAT", "dd/MM/yyyy");\
  var month = date.getMonth();\
  var year = date.getYear();\
  var clientReference = event.values[1].toUpperCase();\
  var kWh = event.values[2];\
  var isFirstCounting = (event.values[3] === 'Oui');\
  var billSheetName = generateMonthYearString(month, year);\
  var previousBillSheetName = generatePreviousMonthYearString(month, year, 1);\
  //bill for current month\
  var billForMonthSheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName(billSheetName);\
  var previousBillForMonthSheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName(previousBillSheetName);\
  \
  if(contactSheet == null) \{\
    contactSheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName(CLIENT_CONTACT_SHEET);\
    contactSheetValues = contactSheet.getDataRange().getValues();\
  \}\
\
  var isNewlyCreatedSheet = false;\
  \
  if(billForMonthSheet == null) \{\
    //create a new month sheet with a first row, it's based on the previous month.\
    //we need either a current month or a previous month defined correctly\
    billForMonthSheet = copySheet(billSheetName, previousBillForMonthSheet, 2);\
    isNewlyCreatedSheet = true;\
  \} else \{\
    //if only one row, delete sheet\
    if(billForMonthSheet.getMaxRows() == 1) \{\
      SpreadsheetApp.getActiveSpreadsheet().deleteSheet(billForMonthSheet);\
      billForMonthSheet = copySheet(billSheetName, previousBillForMonthSheet, 2);\
      isNewlyCreatedSheet = true;\
    \}\
  \}\
  var billForMonthSheetValues = billForMonthSheet.getDataRange().getValues();\
  \
  //we reference kWh from previous month, fetch values\
  var previousBillForMonthSheetValues = null;\
  if(previousBillForMonthSheet != null) \{\
    previousBillForMonthSheetValues = previousBillForMonthSheet.getDataRange().getValues();\
  \}\
  \
  if(checkClient(clientReference) < 0) \{\
    sendErrorMail(clientReference);\
    return -1;\
  \}\
  \
  //add row for new client\
  addRowInBillingIfRequired(billForMonthSheet, billForMonthSheetValues, clientReference, isNewlyCreatedSheet);\
  billForMonthSheetValues = billForMonthSheet.getDataRange().getValues();\
  \
  //get row and range\
  var rowRange = getColumnRangeFromReference('Reference', clientReference, billForMonthSheet, billForMonthSheetValues);\
  var rowValues = rowRange.getValues();\
  var rowFormulas = rowRange.getFormulas();\
  \
  //update kWh in Bill\
  updateToKWh(billForMonthSheet, billForMonthSheetValues, rowValues, clientReference, kWh);\
\
  //update client name in bill  \
  updateClientName(clientReference, billForMonthSheet, billForMonthSheetValues, rowValues, contactSheet, contactSheetValues);\
  \
  //update to date\
  updateCellByKeyOnColumn('ToDate', billForMonthSheet, billForMonthSheetValues, rowValues, formattedDate);\
  \
  //update month\
  updateCellByKeyOnColumn('Month', billForMonthSheet, billForMonthSheetValues, rowValues, getMonthAsAString(month));\
  \
  //update FormattedDate\
  var billDate = Utilities.formatDate(date, "EAT", "dd/MM/yyyy");\
  updateCellByKeyOnColumn('FormattedDate', billForMonthSheet, billForMonthSheetValues, rowValues, billDate);\
    \
  //update Document Title\
  var documentTitle = 'Facture_'.concat(clientReference);\
  updateCellByKeyOnColumn('Document Title', billForMonthSheet, billForMonthSheetValues, rowValues, documentTitle);\
  \
  //update from date and from kWh\
  updateFromFields(billForMonthSheet, billForMonthSheetValues, previousBillForMonthSheet, previousBillForMonthSheetValues, rowValues, clientReference, formattedDate, kWh, isFirstCounting);\
    \
  //clean up cells for PDF generation plugin\
  cleanUpCellsForPDFPlugin(billForMonthSheet, billForMonthSheetValues, rowValues, clientReference);\
\
  var clientAccountSheet = getClientAccountSheet(month, year);  \
  var clientAccountSheetValues = clientAccountSheet.getDataRange().getValues();\
\
  //update bill sheet from user account\
  updateBillFromUserAccount(clientReference, billForMonthSheet, billForMonthSheetValues, clientAccountSheet, clientAccountSheetValues, rowValues, month, year);\
  \
  //save bill data\
  saveRangeValues(rowRange, rowValues, rowFormulas);\
  billForMonthSheetValues = billForMonthSheet.getDataRange().getValues();\
\
  //update user account sheet\
  updateUserAccount(clientReference, billForMonthSheet, billForMonthSheetValues, clientAccountSheet, clientAccountSheetValues, month, year);\
    \
  //send a mail with client amount due, paid, energy usage for the current month\
  if(SEND_EMAIL)\
    sendUserAccountStatus(billForMonthSheet, billForMonthSheetValues, clientReference, month, year);\
\}\
\
function updateClientName(clientReference, billForMonthSheet, billForMonthSheetValues, rowValues, contactSheet, contactSheetValues) \{\
  var clientName = getCellByKey('Reference', 'Name', clientReference, contactSheet, contactSheetValues);\
  var clientLastName = getCellByKey('Reference', 'Last Name', clientReference, contactSheet, contactSheetValues);\
  updateCellByKeyOnColumn('Name', billForMonthSheet, billForMonthSheetValues, rowValues, clientName);\
  updateCellByKeyOnColumn('Last Name', billForMonthSheet, billForMonthSheetValues, rowValues, clientReference);\
\}\
\
function addRowInBillingIfRequired(billForMonthSheet, billForMonthSheetValues, clientReference, isNewlyCreatedSheet) \{\
   //update ToKwh\
  if(LOGGING)\
    Logger.log('client Reference : ' + clientReference);\
  if(getCellByKey('Reference', 'TokWh', clientReference, billForMonthSheet, billForMonthSheetValues) == null) \{\
    //client not found, create a row \
    copyLastRow(billForMonthSheet); \
    if(isNewlyCreatedSheet === true) \{\
      //remove first row\
      billForMonthSheet.deleteRow(2);\
    \}\
    //update client reference\
    var clientRefCell = billForMonthSheet.getRange(billForMonthSheet.getLastRow(), getColumnIndexByName(billForMonthSheet.getName(), 'Reference', billForMonthSheetValues) + 1);\
    clientRefCell.setValue(clientReference);\
  \}\
\}\
\
function cleanUpCellsForPDFPlugin(billForMonthSheet, billForMonthSheetValues, rowValues, clientReference) \{\
  updateCellByKeyOnColumn('Data Merge Status', billForMonthSheet, billForMonthSheetValues, rowValues, '');\
  updateCellByKeyOnColumn('Document URL', billForMonthSheet, billForMonthSheetValues, rowValues, '');\
\}\
\
function updateToKWh(billForMonthSheet, billForMonthSheetValues, rowValues, clientReference, kWh) \{\
  //update ToKwh\
  if(LOGGING)\
    Logger.log('updateToKWh, client Reference : ' + clientReference);\
  updateCellByKeyOnColumn('TokWh', billForMonthSheet, billForMonthSheetValues, rowValues, kWh);\
\}\
\
function updateFromFields(billForMonthSheet, billForMonthSheetValues, previousBillForMonthSheet, previousBillForMonthSheetValues, rowValues, clientReference, currentDate, countedKwh, firstCounting)\{\
  //update from kwH and from date from previous sheet\
  var foundPreviousValue = false;\
  if(previousBillForMonthSheet != null) \{\
    if(LOGGING)\
      Logger.log('get previous bill, client reference : ' + clientReference);\
    //fetch to date and to kwh\
    var toKWh = getCellByKey('Reference', 'TokWh', clientReference, previousBillForMonthSheet, previousBillForMonthSheetValues);\
    var toDate = getCellByKey('Reference', 'ToDate', clientReference, previousBillForMonthSheet, previousBillForMonthSheetValues);\
    if(LOGGING) \{\
      Logger.log('get previous bill, toKWh : ' + toKWh);\
      Logger.log('get previous bill, toDate : ' + toDate);\
    \}\
    if(toKWh != null && toDate != null) \{\
      updateCellByKeyOnColumn('FromkWh', billForMonthSheet, billForMonthSheetValues, rowValues, toKWh);\
      updateCellByKeyOnColumn('FromDate', billForMonthSheet, billForMonthSheetValues, rowValues, toDate);\
      foundPreviousValue = true;\
    \}\
  \}\
  //if not previous value found and if first couting, set previous values to current values.\
  if(foundPreviousValue == false && firstCounting == true) \{\
    updateCellByKeyOnColumn('FromkWh', billForMonthSheet, billForMonthSheetValues, rowValues, countedKwh);\
    updateCellByKeyOnColumn('FromDate', billForMonthSheet, billForMonthSheetValues, rowValues, currentDate);\
  \}\
\}\
\
function getClientAccountSheet(month, year) \{\
  //create new sheet for client account if required\
  var out = getOrCreateAccountSheet(month, year);\
  return out.sheet;\
\}\
\
function updateBillFromUserAccount(clientReference, billForMonthSheet, billForMonthSheetValues, clientAccountSheet, clientAccountSheetValues, rowValues, month, year) \{\
  var amountDueInClient = getCellByKey('Reference', 'Due', clientReference, clientAccountSheet, clientAccountSheetValues);\
  if(amountDueInClient == null) \{\
    var previousMonthSold = getPreviousMonthSoldAccount(clientReference, month, year);\
    if(previousMonthSold == null) \{\
      previousMonthSold = 0;\
    \}\
  \} else \{\
    previousMonthSold = getCellByKey('Reference', 'Previous Sold', clientReference, clientAccountSheet, clientAccountSheetValues);\
  \}\
  \
  //get paid amount\
  var paidAmount = getCellByKey('Reference', 'Paid', clientReference, clientAccountSheet, clientAccountSheetValues);\
\
  //update previous on current bill sheet\
  updateCellByKeyOnColumn('Previous Sold', billForMonthSheet, billForMonthSheetValues, rowValues, previousMonthSold);\
\
  //update paid amount on current bill sheet\
  updateCellByKeyOnColumn('Paid', billForMonthSheet, billForMonthSheetValues, rowValues, paidAmount);\
\}\
\
function updateUserAccount(clientReference, billForMonthSheet, billForMonthSheetValues, clientAccountSheet, clientAccountSheetValues, month, year) \{\
  //update client account sheet\
  var amountDue = getCellByKey('Reference', 'TotalPrice', clientReference, billForMonthSheet, billForMonthSheetValues);\
  var previousMonthSold = 0;\
  var clientRowRange;\
  var clientRowValues;\
  //create it if not stored yet for current month\
  var amountDueInClient = getCellByKey('Reference', 'Due', clientReference, clientAccountSheet, clientAccountSheetValues);\
  if(amountDueInClient == null) \{\
    //client not found, create a row \
    copyLastRow(clientAccountSheet);\
    clientRowRange = getColumnRangeFromIndex(clientAccountSheet, clientAccountSheet.getLastRow() - 1);\
    clientRowValues = clientRowRange.getValues();\
    clientAccountSheetValues = clientAccountSheet.getDataRange().getValues();\
    //fetch previous total amount if any for previous month\
    var previousMonthSold = getPreviousMonthSoldAccount(clientReference, month, year);\
    if(previousMonthSold == null) \{\
      previousMonthSold = 0;\
    \}\
    updateCellByKeyOnColumn('Previous Sold', clientAccountSheet, clientAccountSheetValues, clientRowValues, previousMonthSold);\
    \
    //update client reference\
    updateCellByKeyOnColumn('Reference', clientAccountSheet, clientAccountSheetValues, clientRowValues, clientReference);\
    \
    //update paid amount\
    updateCellByKeyOnColumn('Paid', clientAccountSheet, clientAccountSheetValues, clientRowValues, 0);\
    \
    //update amount due\
    updateCellByKeyOnColumn('Due', clientAccountSheet, clientAccountSheetValues, clientRowValues, amountDue);\
    \
    //save values\
    clientRowRange.setValues(clientRowValues);\
    \
    //refresh values\
    clientAccountSheetValues = clientAccountSheet.getDataRange().getValues();\
  \} else \{\
    clientRowRange = getColumnRangeFromReference('Reference', clientReference, clientAccountSheet, clientAccountSheetValues);\
    clientRowValues = clientRowRange.getValues();\
    updateCellByKeyOnColumn('Due', clientAccountSheet, clientAccountSheetValues, clientRowValues, amountDue);\
    previousMonthSold = getCellByKey('Reference', 'Previous Sold', clientReference, clientAccountSheet, clientAccountSheetValues);\
  \}\
  \
  //get paid amount\
  var paidAmount = getCellByKey('Reference', 'Paid', clientReference, clientAccountSheet, clientAccountSheetValues);\
  \
  //update total\
  updateCellByKeyOnColumn('Total', clientAccountSheet, clientAccountSheetValues, clientRowValues, amountDue+previousMonthSold-paidAmount);\
  clientRowRange.setValues(clientRowValues);\
\}\
\
function sendUserAccountStatus(billForMonthSheet, billForMonthSheetValues, clientReference, month, year) \{\
  var clientAccountSheetName = getClientAccountSheetName(generateMonthYearString(month, year));\
  var clientAccountSheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName(clientAccountSheetName);\
  var clientAccountSheetValues = clientAccountSheet.getDataRange().getValues();\
  \
  //fetch paid, due, sold \
  var clientRefCell = clientAccountSheet.getRange(clientAccountSheet.getLastRow(), getColumnIndexByName(clientAccountSheet.getName(), 'Paid', clientAccountSheetValues) + 1);\
  var paid = clientRefCell.getValue();\
\
  clientRefCell = clientAccountSheet.getRange(clientAccountSheet.getLastRow(), getColumnIndexByName(clientAccountSheet.getName(), 'Due', clientAccountSheetValues) + 1);\
  var due = clientRefCell.getValue();\
  \
  //fetch current kWh usage\
  var fromKWh = getCellByKey('Reference', 'FromkWh', clientReference, billForMonthSheet, billForMonthSheetValues);\
  var toKWh = getCellByKey('Reference', 'TokWh', clientReference, billForMonthSheet, billForMonthSheetValues);\
  \
  if(LOGGING) \{\
    Logger.log('Paid : ' + paid);\
    Logger.log('Due : ' + due);\
    Logger.log('From kWh : ' + fromKWh);\
    Logger.log('To kWh : ' + toKWh);\
  \}\
  \
  var email = EMAIL_MAJIKA;//Session.getActiveUser().getEmail();\
  var subject = "Statut du compte utilisateur pour le mois en cours";\
  var message = "R\'e9f\'e9rence client : " + clientReference + "\\n";\
  message += "Montant d\'e9j\'e0 pay\'e9 : " + paid + " Ariary\\n";\
  message += "Montant restant \'e0 payer : " + parseInt(due-paid) + " Ariary\\n";\
  message += "Consommation : " + parseInt(toKWh-fromKWh) + " kWh\\n";\
  if(SEND_EMAIL) \{\
    for(var i = 0; i < email.length; i++)\
      MailApp.sendEmail(email[i], subject, message);\
  \}\
\}\
}