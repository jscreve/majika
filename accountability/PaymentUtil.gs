{\rtf1\ansi\ansicpg1252\cocoartf1504\cocoasubrtf830
{\fonttbl\f0\fswiss\fcharset0 Helvetica;}
{\colortbl;\red255\green255\blue255;}
{\*\expandedcolortbl;;}
\paperw11900\paperh16840\margl1440\margr1440\vieww10800\viewh8400\viewkind0
\pard\tx566\tx1133\tx1700\tx2267\tx2834\tx3401\tx3968\tx4535\tx5102\tx5669\tx6236\tx6803\pardirnatural\partightenfactor0

\f0\fs24 \cf0 function getPreviousMonthSoldAccount(clientReference, month, year) \{\
  for(var i = 1; i < 12; i++) \{\
    var clientAccountSheet = getClientAccountSheet(month, year, i);\
    if(LOGGING) \{\
      Logger.log('previous account sheet : ' + clientAccountSheet + 'i, : ' + i);\
    \}\
    if(clientAccountSheet == null) \{\
      // we did not find previous sold\
      return null;\
    \}\
    var clientAccountSheetValues = clientAccountSheet.getDataRange().getValues();\
    var previousMonthSold = getCellByKey('Reference', 'Total', clientReference, clientAccountSheet, clientAccountSheetValues);\
    if(previousMonthSold !== null)\
      return previousMonthSold;\
  \}\
  return null;\
\}\
\
//date has the following format Janvier2017\
function getClientAccountSheetName(dateAsString) \{\
  return 'CA'.concat(dateAsString);\
\}\
\
function getClientAccountSheet(month, year, nbMonthsBack) \{\
  var previousDateString = generatePreviousMonthYearString(month, year, nbMonthsBack);\
  var clientAccountSheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName(getClientAccountSheetName(previousDateString));\
  return clientAccountSheet;\
\}\
\
function getOrCreateAccountSheet(month, year) \{\
  var clientAccountSheetName = getClientAccountSheetName(generateMonthYearString(month, year));\
  if(LOGGING)\
    Logger.log('Looking for client account sheet name : ' + clientAccountSheetName);\
  var clientAccountSheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName(clientAccountSheetName);\
  if(LOGGING)\
    Logger.log('Client account sheet : ' + clientAccountSheet);\
  var previousClientAccountSheetName = getClientAccountSheetName(generatePreviousMonthYearString(month, year, 1));\
  if(LOGGING)\
    Logger.log('Previous client account sheet name : ' + previousClientAccountSheetName);\
  var previousClientAccountSheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName(previousClientAccountSheetName);\
  if(LOGGING)\
    Logger.log('Previous client account sheet : ' + previousClientAccountSheet);\
  \
  //create sheet if not existing\
  var isNewSheet = false;\
  if(clientAccountSheet == null) \{\
    clientAccountSheet = copySheet(clientAccountSheetName, previousClientAccountSheet, 1);\
    isNewSheet = true;\
  \}\
  return \{\
        sheet: clientAccountSheet,\
        isNewSheet: isNewSheet\
  \};\
\}\
function getPreviousMonthSoldAccount(clientReference, month, year) \{\
  for(var i = 1; i < 12; i++) \{\
    var clientAccountSheet = getClientAccountSheet(month, year, i);\
    if(LOGGING) \{\
      Logger.log('previous account sheet : ' + clientAccountSheet + 'i, : ' + i);\
    \}\
    if(clientAccountSheet == null) \{\
      // we did not find previous sold\
      return null;\
    \}\
    var clientAccountSheetValues = clientAccountSheet.getDataRange().getValues();\
    var previousMonthSold = getCellByKey('Reference', 'Total', clientReference, clientAccountSheet, clientAccountSheetValues);\
    if(previousMonthSold !== null)\
      return previousMonthSold;\
  \}\
  return null;\
\}\
\
//date has the following format Janvier2017\
function getClientAccountSheetName(dateAsString) \{\
  return 'CA'.concat(dateAsString);\
\}\
\
function getClientAccountSheet(month, year, nbMonthsBack) \{\
  var previousDateString = generatePreviousMonthYearString(month, year, nbMonthsBack);\
  var clientAccountSheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName(getClientAccountSheetName(previousDateString));\
  return clientAccountSheet;\
\}\
\
function getOrCreateAccountSheet(month, year) \{\
  var clientAccountSheetName = getClientAccountSheetName(generateMonthYearString(month, year));\
  if(LOGGING)\
    Logger.log('Looking for client account sheet name : ' + clientAccountSheetName);\
  var clientAccountSheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName(clientAccountSheetName);\
  if(LOGGING)\
    Logger.log('Client account sheet : ' + clientAccountSheet);\
  var previousClientAccountSheetName = getClientAccountSheetName(generatePreviousMonthYearString(month, year, 1));\
  if(LOGGING)\
    Logger.log('Previous client account sheet name : ' + previousClientAccountSheetName);\
  var previousClientAccountSheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName(previousClientAccountSheetName);\
  if(LOGGING)\
    Logger.log('Previous client account sheet : ' + previousClientAccountSheet);\
  \
  //create sheet if not existing\
  var isNewSheet = false;\
  if(clientAccountSheet == null) \{\
    clientAccountSheet = copySheet(clientAccountSheetName, previousClientAccountSheet, 1);\
    isNewSheet = true;\
  \}\
  return \{\
        sheet: clientAccountSheet,\
        isNewSheet: isNewSheet\
  \};\
\}\
}