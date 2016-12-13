#include <sys/socket.h> //sockets are ways that programs interface the network 
#include <netinet/in.h>
#include <stdio.h>
#include <string.h>

int main(int argc, char** argv){
	int sockfd = socket(AF_INET,SOCK_DGRAM,0);
	if(sockfd < 0){
		printf("There was an errorr creating the socket\n");
		return 1;
	}
	fd_set sockets;
	FD_ZERO(&sockets);
	
	
	struct sockaddr_in serveraddr, clientaddr;
	serveraddr.sin_family=AF_INET;
	serveraddr.sin_port=htons(9876);
	serveraddr.sin_addr.s_addr=INADDR_ANY;

	int e = bind(sockfd,(struct sockaddr*)&serveraddr,sizeof(serveraddr));
	listen(sockfd,10); //listen for connections from any clients, 10 is parameter for how many connections to allow program to take befor refusing connects
	FD_SET(sockfd,&sockets);
	
	while(1){
		int len=sizeof(clientaddr);
		char line[5000];
		int n = recvfrom(sockfd,line,5000,0, (struct sockaddr*)&clientaddr,&len);
		line[n] = '\0';
		printf("Got from client: %s\n", line);
	}	

	return 0;

}
		


