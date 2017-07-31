{\rtf1\ansi\ansicpg1252\cocoartf1504\cocoasubrtf830
{\fonttbl\f0\fswiss\fcharset0 Helvetica;}
{\colortbl;\red255\green255\blue255;}
{\*\expandedcolortbl;;}
\paperw11900\paperh16840\margl1440\margr1440\vieww10800\viewh8400\viewkind0
\pard\tx566\tx1133\tx1700\tx2267\tx2834\tx3401\tx3968\tx4535\tx5102\tx5669\tx6236\tx6803\pardirnatural\partightenfactor0

\f0\fs24 \cf0 function manageNewClient(event) \{\
  Logger.log(event.values);\
  var village = event.values[1];\
  var clientLastName = event.values[4];\
  var clientName = event.values[2];\
  var phoneNumber = event.values[3];\
  var phoneNumber2 = event.values[5];\
  var counterNumber = event.values[6];\
  var clientSheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName(CLIENT_REFERENCE_SHEET);\
  var contactSheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName(CLIENT_CONTACT_SHEET);\
  \
  //get new reference\
  var lastReferenceNumber = parseInt(getCellByKey('Village', 'MaxRef', village, clientSheet));\
  if(LOGGING)\
    Logger.log('last reference : ' + lastReferenceNumber);\
  var newReferenceNumber = lastReferenceNumber + 1;\
  var newReference = village.substring(0, 3).toUpperCase().concat(newReferenceNumber);\
  updateCellByKey('Village', 'MaxRef', village, newReferenceNumber, clientSheet);\
  \
  //update contact info\
  contactSheet.appendRow([newReference, clientLastName, clientName, phoneNumber, phoneNumber2, counterNumber]);\
  \
  //send email with contact infos\
  var email = EMAIL_MAJIKA;//Session.getActiveUser().getEmail();\
  var subject = "Nouveau client cr\'e9\'e9";\
  var message = "La r\'e9f\'e9rence client est : " + newReference;\
  if(SEND_EMAIL) \{\
    for(var i = 0; i < email.length; i++)\
      MailApp.sendEmail(email[i], subject, message);\
  \}\
\}}