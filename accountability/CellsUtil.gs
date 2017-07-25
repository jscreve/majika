{\rtf1\ansi\ansicpg1252\cocoartf1504\cocoasubrtf830
{\fonttbl\f0\fswiss\fcharset0 Helvetica;}
{\colortbl;\red255\green255\blue255;}
{\*\expandedcolortbl;;}
\paperw11900\paperh16840\margl1440\margr1440\vieww10800\viewh8400\viewkind0
\pard\tx566\tx1133\tx1700\tx2267\tx2834\tx3401\tx3968\tx4535\tx5102\tx5669\tx6236\tx6803\pardirnatural\partightenfactor0

\f0\fs24 \cf0 Array.prototype.findIndex = function(search)\{\
  if(search == "") return false;\
  for (var i=0; i<this.length; i++)\
    if (this[i] == search) return i;\
  return -1;\
\} \
\
Number.prototype.mod = function(n) \{\
	var m = (( this % n) + n) % n;\
	return m < 0 ? m + Math.abs(n) : m;\
\};\
\
ArrayLib.indexOf = \
function indexOf(data, columnIndex, value) \{\
    if (data.length > 0) \{\
        if (typeof columnIndex != "number" || columnIndex > data[0].length) \{\
            throw "Choose a valide column index";\
        \}\
        var r = -1;\
        var reg = new RegExp(value);\
        for (var i = 0; i < data.length; i++) \{\
            if (data[0][0] == undefined) \{\
                if (data[i].toString().search(reg) != -1) \{\
                    return i;\
                \}\
            \} else \{\
                if (columnIndex < 0 && data[i].toString().search(reg) != -1 || columnIndex >= 0 && data[i][columnIndex].toString().search(reg) != -1) \{\
                    return i;\
                \}\
            \}\
        \}\
        return r;\
    \} else \{\
        return data;\
    \}\
\}\
\
ArrayLib.twiceIndexOf = \
function twiceIndexOf(data, columnIndex, secondaryColumnIndex, value1, value2) \{\
    if (data.length > 0) \{\
        if (typeof columnIndex != "number" || columnIndex > data[0].length) \{\
            Logger.log ('columnIndex : ' + columnIndex + ' length: ' + data[0].length);\
            throw "Choose a valid column index";\
        \}\
        if (typeof secondaryColumnIndex != "number" || secondaryColumnIndex > data[0].length) \{\
            Logger.log ('secondaryColumnIndex : ' + secondaryColumnIndex + ' length: ' + data[0].length);\
            throw "Choose a valid column index";\
        \}\
        var r = -1;\
        var reg1 = new RegExp(value1);\
        var reg2 = new RegExp(value2);\
        for (var i = 0; i < data.length; i++) \{\
          if(columnIndex >= 0 && data[i][columnIndex].toString().search(reg1) != -1 &&\
            secondaryColumnIndex >= 0 && data[i][secondaryColumnIndex].toString().search(reg2) != -1) \{\
              return i;\
           \} \
        \}\
        return r;\
    \} else \{\
        return data;\
    \}\
\}\
\
function escapeRegExp(str) \{\
  return str.replace(/[\\-\\[\\]\\/\\\{\\\}\\(\\)\\*\\+\\?\\.\\\\\\^\\$\\|]/g, "\\\\$&");\
\}\
\
function getColumnIndexByName(sheetName, name, sheetValues) \{\
  var cache = CacheService.getScriptCache();\
  var cachedValue = cache.get(getColumnIndexCacheKey(sheetName, name));\
  if(cachedValue !== null) \{\
    return parseInt(cachedValue);\
  \}\
  for (var j = 0; j < sheetValues[0].length; j++) \{\
    if (sheetValues[0][j]) \{\
       if(sheetValues[0][j] === name) \{\
         cache.put(getColumnIndexCacheKey(sheetName, name), j);\
         return j;\
       \}\
    \}\
  \}\
  return -1;\
\}\
\
function getColumnIndexCacheKey(sheetName, columnName) \{\
  return 'getColumnIndexByName' + sheetName + '_' + columnName;\
\}\
\
function updateCellByKey(clientRefColumnName, columnToUpdateName, clientReference, value, sheet, sheetValues) \{\
  if(sheetValues == undefined) \{\
    sheetValues = sheet.getDataRange().getValues();\
  \}\
  var columnToUpdateIndex = getColumnIndexByName(sheet.getName(), columnToUpdateName, sheetValues);\
  var clientReferenceColumnIndex = getColumnIndexByName(sheet.getName(), clientRefColumnName, sheetValues);\
  var regexp = '^' + escapeRegExp(clientReference) + '$';\
  var clientRowIndex = ArrayLib.indexOf(sheetValues, clientReferenceColumnIndex, regexp);\
  if(clientRowIndex < 0 || columnToUpdateIndex < 0) \{\
    return null;\
  \}\
  var cell = sheet.getRange(clientRowIndex + 1, columnToUpdateIndex + 1);\
  cell.setValue(value);\
  return 0;\
\}\
\
function updateCellByKeyAndSecondaryKey(clientRefColumnName, keyColumnName, columnToUpdateName, clientReference, key, value, sheet, sheetValues) \{\
  if(sheetValues == undefined) \{\
    sheetValues = sheet.getDataRange().getValues();\
  \}\
  var sheetName = sheet.getName();\
  var columnToUpdateIndex = getColumnIndexByName(sheetName, columnToUpdateName, sheetValues);\
  var clientReferenceColumnIndex = getColumnIndexByName(sheetName, clientRefColumnName, sheetValues);\
  var keyColumnIndex = getColumnIndexByName(sheetName, keyColumnName, sheetValues);\
  var regexp1 = '^' + escapeRegExp(clientReference) + '$';\
  var regexp2 = '^' + escapeRegExp(key) + '$';\
  var clientRowIndex = ArrayLib.twiceIndexOf(sheetValues, clientReferenceColumnIndex, keyColumnIndex, regexp1, regexp2);\
  if(clientRowIndex < 0 || columnToUpdateIndex < 0) \{\
    return null;\
  \}\
  var cell = sheet.getRange(clientRowIndex + 1, columnToUpdateIndex + 1);\
  cell.setValue(value);\
  return 0;\
\}\
\
function getCellByKeyAndSecondaryKey(clientRefColumnName, keyColumnName, columnToGetName, clientReference, key, sheet, sheetValues) \{\
  if(sheetValues == undefined) \{\
    sheetValues = sheet.getDataRange().getValues();\
  \}\
  var sheetName = sheet.getName();\
  var columnToGetIndex = getColumnIndexByName(sheetName, columnToGetName, sheetValues);\
  var clientReferenceColumnIndex = getColumnIndexByName(sheetName, clientRefColumnName, sheetValues);\
  var keyColumnIndex = getColumnIndexByName(sheetName, keyColumnName, sheetValues);\
  var regexp1 = '^' + escapeRegExp(clientReference) + '$';\
  var regexp2 = '^' + escapeRegExp(key) + '$';\
  var clientRowIndex = ArrayLib.twiceIndexOf(sheetValues, clientReferenceColumnIndex, keyColumnIndex, regexp1, regexp2);\
  if(clientRowIndex < 0 || columnToGetIndex < 0) \{\
    return null;\
  \}\
  var cell = sheet.getRange(clientRowIndex + 1, columnToGetIndex + 1);\
  return cell.getValue();\
\}\
\
function getCellByKey(clientRefColumnName, columnName, clientReference, sheet, sheetValues) \{\
  if(sheetValues == undefined) \{\
    sheetValues = sheet.getDataRange().getValues();\
  \}\
  var sheetName = sheet.getName();\
  var columnIndex = getColumnIndexByName(sheetName, columnName, sheetValues);\
  var clientReferenceColumnIndex = getColumnIndexByName(sheetName, clientRefColumnName, sheetValues);\
  var regexp = '^' + escapeRegExp(clientReference) + '$';\
  var clientRowIndex = ArrayLib.indexOf(sheetValues, clientReferenceColumnIndex, regexp);\
  if(clientRowIndex < 0 || columnIndex < 0) \{\
    return null;\
  \}\
  var cell = sheet.getRange(clientRowIndex + 1, columnIndex + 1);\
  return cell.getValue();\
\}\
\
function copyLastRow(sheet) \{\
  var lRow = sheet.getLastRow(); \
  var lCol = sheet.getLastColumn(), range = sheet.getRange(lRow, 1, 1, lCol);\
  sheet.insertRowsAfter(lRow, 1);\
  range.copyTo(sheet.getRange(lRow+1, 1, 1, lCol), \{contentsOnly:false\});\
\}\
\
function getLastRow(sheet) \{\
  var lRow = sheet.getLastRow(); \
  var lCol = sheet.getLastColumn(), range = sheet.getRange(lRow, 1, 1, lCol);\
  return range;\
\}\
}