{\rtf1\ansi\ansicpg1252\cocoartf1504\cocoasubrtf830
{\fonttbl\f0\fswiss\fcharset0 Helvetica;}
{\colortbl;\red255\green255\blue255;}
{\*\expandedcolortbl;;}
\paperw11900\paperh16840\margl1440\margr1440\vieww10800\viewh8400\viewkind0
\pard\tx566\tx1133\tx1700\tx2267\tx2834\tx3401\tx3968\tx4535\tx5102\tx5669\tx6236\tx6803\pardirnatural\partightenfactor0

\f0\fs24 \cf0 \
function checkClient(clientReference) \{\
  //update client name\
  var contactSheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName(CLIENT_CONTACT_SHEET);\
  var clientName = getCellByKey('Reference', 'Name', clientReference, contactSheet);\
  //if client cannot be found, we must stop everything here and send an email\
  if(clientName == null ) \{\
    return -1;\
  \}\
  return 0;\
\}\
\
function sendErrorMail(clientReference) \{\
  var email = EMAIL_MAJIKA;//Session.getActiveUser().getEmail();\
  var subject = "Utilisateur non valide";\
  var message = "La r\'e9f\'e9rence client n'est pas trouv\'e9e : " + clientReference;\
  if(SEND_EMAIL) \{\
    for(var i = 0; i < email.length; i++) \{\
      MailApp.sendEmail(email[i], subject, message);\
    \}\
  \}\
\}\
\
//create a new sheet based on an old one\
function copySheet(sheetName, referenceSheet, nbRemainingRows) \{\
  var ss = SpreadsheetApp.getActiveSpreadsheet();\
  var sheet = referenceSheet.copyTo(ss);\
  sheet.setName(sheetName);\
\
  if(sheet.getLastRow() > 0) \{  \
    sheet.deleteRows(parseInt(nbRemainingRows) + 1, sheet.getLastRow() - parseInt(nbRemainingRows));\
  \}\
  /* Make the new sheet active */\
  ss.setActiveSheet(sheet);\
  return sheet;\
\}\
\
// http://www.bennadel.com/blog/1504-Ask-Ben-Parsing-CSV-Strings-With-Javascript-Exec-Regular-Expression-Command.htm\
// This will parse a delimited string into an array of\
// arrays. The default delimiter is the comma, but this\
// can be overriden in the second argument.\
function CSVToArray(strData, strDelimiter) \{\
  // Check to see if the delimiter is defined. If not,\
  // then default to COMMA.\
  strDelimiter = (strDelimiter || ",");\
\
  // Create a regular expression to parse the CSV values.\
  var objPattern = new RegExp(\
    (\
      // Delimiters.\
      "(\\\\" + strDelimiter + "|\\\\r?\\\\n|\\\\r|^)" +\
\
      // Quoted fields.\
      "(?:\\"([^\\"]*(?:\\"\\"[^\\"]*)*)\\"|" +\
\
      // Standard fields.\
      "([^\\"\\\\" + strDelimiter + "\\\\r\\\\n]*))"\
    ),\
    "gi"\
  );\
\
  // Create an array to hold our data. Give the array\
  // a default empty first row.\
  var arrData = [[]];\
\
  // Create an array to hold our individual pattern\
  // matching groups.\
  var arrMatches = null;\
\
  // Keep looping over the regular expression matches\
  // until we can no longer find a match.\
  while (arrMatches = objPattern.exec( strData ))\{\
    // Get the delimiter that was found.\
    var strMatchedDelimiter = arrMatches[ 1 ];\
\
    // Check to see if the given delimiter has a length\
    // (is not the start of string) and if it matches\
    // field delimiter. If id does not, then we know\
    // that this delimiter is a row delimiter.\
    if (\
      strMatchedDelimiter.length &&\
      (strMatchedDelimiter != strDelimiter)\
    )\{\
      // Since we have reached a new row of data,\
      // add an empty row to our data array.\
      arrData.push([]);\
    \}\
    // Now that we have our delimiter out of the way,\
    // let's check to see which kind of value we\
    // captured (quoted or unquoted).\
    if (arrMatches[2])\{\
      // We found a quoted value. When we capture\
      // this value, unescape any double quotes.\
      var strMatchedValue = arrMatches[ 2 ].replace(\
        new RegExp( "\\"\\"", "g" ),\
        "\\""\
      );\
    \} else \{\
      // We found a non-quoted value.\
      var strMatchedValue = arrMatches[ 3 ];\
    \}\
\
    // Now that we have our value string, let's add\
    // it to the data array.\
    arrData[arrData.length - 1].push(strMatchedValue);\
  \}\
\
  // Return the parsed data.\
  return(arrData);\
\};\
}