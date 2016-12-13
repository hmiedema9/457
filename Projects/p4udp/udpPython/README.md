# CIS457_Project5

Sean Crowley

CIS 457

Project 5: Reliable File Transfer over UDP

You must write a client and server to support file transfer. The client should send to the server a request to initiate a transfer of a specific file. Upon recieving the request, if the file exists, it should be sent to the client. Be sure your program works with all file types, not just text files.

The client and server both must be written in C, C++, Python, or Java using sockets. 

- When the server starts, it should wait for a client connection (on a port number either obtained as a command line option or input by the user). 
- When the client starts, it should connect to the server and send a request for a file. This request may be formatted in any way your group finds convenient. 
- The server IP address, server port number, and file name may either be taken by the client program as user input, or as command line options. 
- Upon recieving a request, the server should read the requested file if it exists, and send it to the client, which should write the file locally. The file transfer must meet the following requirements:
  - UDP sockets must be used.
  - The data in each packet, including any header information you add, must be 1024 bytes or less. 
  - You must implement a sliding window of 5 packets for your file transfer. Stop-and-wait protocols will not receive credit. 
  - The file transfer must be reliable (recover from any packet loss, packet corruption, and packet reordering).
- The client and server should each print to the screen each time they send or recieve a packet. 
- Do not load the entire file you will be sending into memory at once. This is not realistic for sending large files. 
