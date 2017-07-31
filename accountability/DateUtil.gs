{\rtf1\ansi\ansicpg1252\cocoartf1504\cocoasubrtf830
{\fonttbl\f0\fswiss\fcharset0 Helvetica;}
{\colortbl;\red255\green255\blue255;}
{\*\expandedcolortbl;;}
\paperw11900\paperh16840\margl1440\margr1440\vieww10800\viewh8400\viewkind0
\pard\tx566\tx1133\tx1700\tx2267\tx2834\tx3401\tx3968\tx4535\tx5102\tx5669\tx6236\tx6803\pardirnatural\partightenfactor0

\f0\fs24 \cf0 function getMonthAsAString(monthIndex) \{\
  return MONTHS[monthIndex]\
\}\
\
/**\
* Generate date with the following format : Janvier2017\
**/\
function generateMonthYearString(monthIndex, year) \{\
  if(LOGGING)\
    Logger.log('generate month year string : ' + monthIndex + ' ' + year);\
  return MONTHS[monthIndex].concat(year);\
\}\
\
/**\
* Generate date with the following format : Janvier2017. We can specify how many months back from the current month the date must be.\
**/\
function generatePreviousMonthYearString(currentMonthIndex, currentYear, nbMonthsBack) \{\
  for(var i = 0; i < nbMonthsBack; i++) \{\
    currentMonthIndex--;\
    if(currentMonthIndex == -1) \{\
      currentMonthIndex = 11;\
      currentYear--;\
    \}\
  \}\
  return generateMonthYearString(currentMonthIndex, currentYear);\
\}\
}