{\rtf1\ansi\ansicpg1252\cocoartf1504\cocoasubrtf830
{\fonttbl\f0\fswiss\fcharset0 Helvetica;}
{\colortbl;\red255\green255\blue255;}
{\*\expandedcolortbl;;}
\paperw11900\paperh16840\margl1440\margr1440\vieww10800\viewh8400\viewkind0
\pard\tx566\tx1133\tx1700\tx2267\tx2834\tx3401\tx3968\tx4535\tx5102\tx5669\tx6236\tx6803\pardirnatural\partightenfactor0

\f0\fs24 \cf0 \
function manageCounting(event) \{\
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
  if(LOGGING)\
    Logger.log('formatted Date : ' + formattedDate);\
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
  //update kWh in Bill\
  updateToKWh(billForMonthSheet, billForMonthSheetValues, clientReference, kWh);\
\
  //update client name in bill  \
  updateClientName(clientReference, billForMonthSheet, billForMonthSheetValues);\
\
  //update to date\
  updateCellByKey('Reference', 'ToDate', clientReference, formattedDate, billForMonthSheet, billForMonthSheetValues);\
\
  //update month\
  updateCellByKey('Reference', 'Month', clientReference, getMonthAsAString(month), billForMonthSheet, billForMonthSheetValues);\
  \
  //update FormattedDate\
  var billDate = Utilities.formatDate(date, "EAT", "dd/MM/yyyy");\
  updateCellByKey('Reference', 'FormattedDate', clientReference, billDate, billForMonthSheet, billForMonthSheetValues);\
    \
  //update Document Title\
  var documentTitle = 'Facture_'.concat(clientReference);\
  updateCellByKey('Reference', 'Document Title', clientReference, documentTitle, billForMonthSheet, billForMonthSheetValues);\
\
  //update from date and from kWh\
  updateFromFields(billForMonthSheet, billForMonthSheetValues, previousBillForMonthSheet, previousBillForMonthSheetValues, clientReference, formattedDate, kWh, isFirstCounting);\
  \
  //update hardware rental prices\
  //updateHardwarePrices(billForMonthSheet, billForMonthSheetValues, clientReference, isFirstCounting);\
  \
  //clean up cells for PDF generation plugin\
  cleanUpCellsForPDFPlugin(billForMonthSheet, billForMonthSheetValues, clientReference);\
\
  //update user account\
  updateUserAccount(clientReference, billForMonthSheet, billForMonthSheetValues, month, year);\
  \
  //send a mail with client amount due, paid, energy usage for the current month\
  sendUserAccountStatus(billForMonthSheet, billForMonthSheetValues, clientReference, month, year);\
\}\
\
function updateClientName(clientReference, billForMonthSheet, billForMonthSheetValues) \{\
  //update client name\
  var contactSheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName(CLIENT_CONTACT_SHEET);\
  var clientName = getCellByKey('Reference', 'Name', clientReference, contactSheet);\
  var clientLastName = getCellByKey('Reference', 'Last Name', clientReference, contactSheet);\
  updateCellByKey('Reference', 'Name', clientReference, clientName, billForMonthSheet, billForMonthSheetValues);\
  updateCellByKey('Reference', 'Last Name', clientReference, clientLastName, billForMonthSheet, billForMonthSheetValues);\
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
/*\
function updateHardwarePrices(billForMonthSheet, billForMonthSheetValues, clientReference, isFirstCounting) \{\
  var counterRental = COUNTER_RENTAL_PRICE;\
  var redevance = REDEVANCE_PRICE;\
  if(isFirstCounting == true) \{\
    counterRental = 0;\
    redevance = 0;\
  \}\
  updateCellByKey('Reference', 'Location compteur', clientReference, counterRental, billForMonthSheet, billForMonthSheetValues);\
  updateCellByKey('Reference', 'Redevance', clientReference, redevance, billForMonthSheet, billForMonthSheetValues);\
\}*/\
\
function cleanUpCellsForPDFPlugin(billForMonthSheet, billForMonthSheetValues, clientReference) \{\
  updateCellByKey('Reference', 'Data Merge Status', clientReference, '', billForMonthSheet, billForMonthSheetValues);\
  updateCellByKey('Reference', 'Document URL', clientReference, '', billForMonthSheet, billForMonthSheetValues);\
\}\
\
function updateToKWh(billForMonthSheet, billForMonthSheetValues, clientReference, kWh) \{\
  //update ToKwh\
  if(LOGGING)\
    Logger.log('updateToKWh, client Reference : ' + clientReference);\
  updateCellByKey('Reference', 'TokWh', clientReference, kWh, billForMonthSheet, billForMonthSheetValues);\
\}\
\
function updateFromFields(billForMonthSheet, billForMonthSheetValues, previousBillForMonthSheet, previousBillForMonthSheetValues, clientReference, currentDate, countedKwh, firstCounting)\{\
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
      updateCellByKey('Reference', 'FromkWh', clientReference, toKWh, billForMonthSheet, billForMonthSheetValues);\
      updateCellByKey('Reference', 'FromDate', clientReference, toDate, billForMonthSheet, billForMonthSheetValues);\
      foundPreviousValue = true;\
    \}\
  \}\
  //if not previous value found and if first couting, set previous values to current values.\
  if(foundPreviousValue == false && firstCounting == true) \{\
    updateCellByKey('Reference', 'FromkWh', clientReference, countedKwh, billForMonthSheet, billForMonthSheetValues);\
    updateCellByKey('Reference', 'FromDate', clientReference, currentDate, billForMonthSheet, billForMonthSheetValues);\
  \}\
\}\
\
function updateUserAccount(clientReference, billForMonthSheet, billForMonthSheetValues, month, year) \{\
  //create new sheet for client account if required\
  var out = getOrCreateAccountSheet(month, year);\
  var clientAccountSheet = out.sheet;\
  var isNewClientAccountSheet = out.isNewSheet;\
  var clientAccountSheetValues = clientAccountSheet.getDataRange().getValues();\
  \
  //update client account sheet\
  var amountDue = getCellByKey('Reference', 'TotalPrice', clientReference, billForMonthSheet, billForMonthSheetValues);\
  var previousMonthSold = 0;\
  //create it if not stored yet for current month\
  if(updateCellByKey('Reference', 'Due', clientReference, amountDue, clientAccountSheet, clientAccountSheetValues) == null) \{\
    //client not found, create a row \
    copyLastRow(clientAccountSheet);\
    clientAccountSheetValues = clientAccountSheet.getDataRange().getValues();\
    //fetch previous total amount if any for previous month\
    var previousMonthSold = getPreviousMonthSoldAccount(clientReference, month, year);\
    if(previousMonthSold == null) \{\
      previousMonthSold = 0;\
    \}\
    var clientRefCell = clientAccountSheet.getRange(clientAccountSheet.getLastRow(), getColumnIndexByName(clientAccountSheet.getName(), 'Previous Sold', clientAccountSheetValues) + 1);\
    clientRefCell.setValue(previousMonthSold);\
    //update client reference\
    clientRefCell = clientAccountSheet.getRange(clientAccountSheet.getLastRow(), getColumnIndexByName(clientAccountSheet.getName(), 'Reference', clientAccountSheetValues) + 1);\
    clientRefCell.setValue(clientReference);\
    //update paid amount\
    clientRefCell = clientAccountSheet.getRange(clientAccountSheet.getLastRow(), getColumnIndexByName(clientAccountSheet.getName(), 'Paid', clientAccountSheetValues) + 1);\
    clientRefCell.setValue(0);\
    //update amount due\
    clientRefCell = clientAccountSheet.getRange(clientAccountSheet.getLastRow(), getColumnIndexByName(clientAccountSheet.getName(), 'Due', clientAccountSheetValues) + 1);\
    clientRefCell.setValue(amountDue);\
    //refresh values\
    clientAccountSheetValues = clientAccountSheet.getDataRange().getValues();\
  \} else \{\
    previousMonthSold = getCellByKey('Reference', 'Previous Sold', clientReference, clientAccountSheet, clientAccountSheetValues);\
  \}\
  \
  //get paid amount\
  var paidAmount = getCellByKey('Reference', 'Paid', clientReference, clientAccountSheet, clientAccountSheetValues);\
  \
  //update total\
  updateCellByKey('Reference', 'Total', clientReference, amountDue+previousMonthSold-paidAmount, clientAccountSheet, clientAccountSheetValues);  \
  \
  //update previous on current bill sheet\
  updateCellByKey('Reference', 'Previous Sold', clientReference, previousMonthSold, billForMonthSheet, billForMonthSheetValues);  \
\
  //update paid amount on current bill sheet\
  updateCellByKey('Reference', 'Paid', clientReference, paidAmount, billForMonthSheet, billForMonthSheetValues); \
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