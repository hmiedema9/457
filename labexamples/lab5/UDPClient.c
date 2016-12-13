#include <sys/socket.h>
#include <netinet/in.h>
#include <stdio.h>
#include <string.h>

int main(int argc, char** argv) {
  int sockfd = socket(AF_INET,SOCK_DGRAM,0);
  struct sockaddr_in serveraddr;
  serveraddr.sin_family=AF_INET;
  serveraddr.sin_port=htons(9876);
  serveraddr.sin_addr.s_addr=inet_addr("127.0.0.1");

  printf("Enter a line:");
  char line[5000];
  fgets(line, 5000, stdin);
  sendto(sockfd, line, strlen(line),0, (struct sockaddr*)&serveraddr, sizeof(struct sockaddr_in));
}
