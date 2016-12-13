#Sean Crowley

import socket

#Create a UDP Socket
s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

#Get hostname of computer
#host = socket.gethostname()
host = "10.0.0.1"
#Get user input for port to connect
port = raw_input("Enter a port to connect to : ")

#File name to receive
fileToSend = raw_input("Enter filename to receive : ")

#Connect to the server
s.connect((host, int(port)))

#Creates a new file to store data
with open('received_file', 'wb') as f:
	print('receiving data...')

	#Sends an empty request so server can get client details
	s.send('')

	#Send the name of the file requested to server	
	s.send(fileToSend)
	
	#Number of packets to send
	packetsToSend = s.recv(1024)
	print 'Total packets to receive: ', packetsToSend

	#Receive the first batch of data
	#data = s.recv(1024)

	#5 second timeout
	s.settimeout(5)

	#Number of packets sent
	packetNumber = 1

	#Checks number of packets compared to windowsize
	packetsSent = 0

	#Size of the sliding window
	windowSize = 5

	#While there is data continue to loop
	while (packetNumber == 1 or data0):

		#Stores backup of file
		if (packetsSent == 0):
			backupFile = f

		#Attempts to retrieve 5 packets of data
		try:
			data0 = s.recv(1024)
			packetNumber += 1
			packetsSent += 1

			data1 = s.recv(1024)
			packetNumber += 1
			packetsSent += 1

			data2 = s.recv(1024)
			packetNumber += 1
			packetsSent += 1

			data3 = s.recv(1024)
			packetNumber += 1
			packetsSent += 1

			data4 = s.recv(1024)
			packetNumber += 1
			packetsSent += 1

			print 'Received packets: ', (packetNumber-5), '-', packetNumber

			#Send acknowledgement packet
			s.send('Client received packets successfully')
			print 'Sent acknowledgement packet'

			#Write data to file
			f.write(data0)
			f.write(data1)
			f.write(data2)
			f.write(data3)
			f.write(data4)
		except:
			#If all packets retreived break loop
			if int(packetNumber) > int(packetsToSend):
				break;

			#Send acknowledgement packet
			s.send('Client did not receive packets successfully')
			print 'Error receiving packets'
			
			#Subtract packets that were not retrieved from counter
			packetNumber -= packetsSent
			droppedPackets = True
			
			#Remove junk packets waiting to be sent
			if(packetsSent == 1):
				junk = s.recv(1024)
				junk = s.recv(1024)
				junk = s.recv(1024)
				junk = s.recv(1024)
				packetsSent = 0
			if(packetsSent == 2):
				junk = s.recv(1024)
				junk = s.recv(1024)
				junk = s.recv(1024)
				packetsSent = 0
			if(packetsSent == 3):
				junk = s.recv(1024)
				junk = s.recv(1024)
				packetsSent = 0
			if(packetsSent == 4):
				junk = s.recv(1024)
				packetsSent = 0

			#While packets have still not been retrieved, loop
			while(droppedPackets):
				try:
					print 'retrying'
				
					#Attempts to retrieve data packets
					data0 = s.recv(1024)
					data1 = s.recv(1024)
					data2 = s.recv(1024)
					data3 = s.recv(1024)
					data4 = s.recv(1024)

					#If packets retrieved break loop
					droppedPackets = False
					packetNumber += 5

					#Send acknowledgement packet
					s.send('Client received packets successfully')

					#Write data to file
					f.write(data0)
					f.write(data1)
					f.write(data2)
					f.write(data3)
					f.write(data4)
				except:
					#Send acknowledgement packet
					s.send('Client did not receive packets successfully')

		#Reset counter
		packetsSent = 0

	#Closes file
	f.close()
	print('Successfully received the file')

	#Closes socket
	s.close()
	print('Connection closed')
