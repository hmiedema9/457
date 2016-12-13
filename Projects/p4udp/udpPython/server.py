#Sean Crowley

import socket
import os

#Get user input for port to watch
port = raw_input("Enter a port to watch : ")

#Creates a UDP socket
s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

#Get hostname of computer
host = "10.0.0.1"
print host

#Bind the host to port specified
s.bind((host, int(port)))

#Size of the sliding window
windowSize = 5

print 'Server listening....'

while True:
	
	#Get client IP and Port
	conn, addr = s.recvfrom(1024)
	print 'Connection from : ', addr

	#Get the filename to send from client
	data = s.recv(1024)

	#Removes quotes from filename
	filename = repr(data)[1:-1]
	print 'Requested file : ', filename

	#Gets the size of the file to send
	statinfo = os.stat(filename)
	packetsToSend = (statinfo.st_size / 1024) + 1
	print 'Size : ', statinfo.st_size, ' Bytes, ', packetsToSend, ' Packets'
	s.sendto(str(packetsToSend), addr)

	#Opens the file to send
	f = open(filename,'rb')

	#Reads the first batch of data from file
	subData = f.read(1024)

	#Number of packets sent
	packetNumber = 1

	#While there is data continue to loop
	while (subData):
		
		#Resets the packetSent counter
		packetsSent = 0

		#Resets backup data on success
		backupData0 = None
		backupData1 = None
		backupData2 = None
		backupData3 = None
		backupData4 = None

		#While packetsSent is less than 5 loop
		while (packetsSent < windowSize):

			#Send the data to the client 
			s.sendto(subData, addr)
			print 'Sending Packet : ', packetNumber
			
			#Saves backup data incase of packet loss		
			if (packetsSent == 0):
				backupData0 = subData
			elif (packetsSent == 1):
				backupData1 = subData
			elif (packetsSent == 2):
				backupData2 = subData
			elif (packetsSent == 3):
				backupData3 = subData
			elif (packetsSent == 4):
				backupData4 = subData
				
			#Reads in the next batch of data
			subData = f.read(1024)

			#Increments the packetNumber and packetsSent
			packetNumber += 1
			packetsSent += 1

		#Get the acknowledgement packet from the client
		received = s.recv(1024)
		print received
		
		#Loop until a successful acknowledgement packet
		while (received == 'Client did not receive packets successfully'):
			s.sendto(backupData0, addr)
			s.sendto(backupData1, addr)
			s.sendto(backupData2, addr)
			s.sendto(backupData3, addr)
			s.sendto(backupData4, addr)
			received = s.recv(1024)
			print received

	#Closes the file being read
	f.close()
	print('File sent successfully!')
