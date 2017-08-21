import bluetooth
import time
import sys

name = "HC-05"      # Device name
addr = "98:D3:31:FD:3C:05"      # Device Address
port = 1         # RFCOMM port
end = "END"
data = ""
received = 0
#fetch all data
dataType = "4"
# Now, connect in the same way as always with PyBlueZ
s = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
try :
	s.connect((addr,port))
	s.send(dataType);
	received = 0;
	while received == 0:
		data += s.recv(64).decode('UTF-8')
		if(end in data):
			print (data[:-3])
			data = ""
			received = 1
except:
	print("Error in socket connection")
finally:
	s.close()	

