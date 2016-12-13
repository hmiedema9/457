#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <stdio.h>
#include <time.h>
#include <stdlib.h>
#include <string.h>

#define TYPE_A 1
#define CLASS_IN 1

struct dnshdr{
  u_int16_t id;
  u_int8_t rd:1; // the 1 after the colon is how much space is being allocated for the variable
  u_int8_t tc:1; // these are in opposite order for each bit, only within one byte. 
  u_int8_t aa:1;
  u_int8_t opcode:4;
  u_int8_t qr:1;
  u_int8_t rcode:4;
  u_int8_t cd:1;
  u_int8_t ad:1;
  u_int8_t z:1;
  u_int16_t qcount;
  u_int16_t ancount;
  u_int16_t authcount;
  u_int16_t addcount;
};

void encodename(char* src, char* dst);
int printreply(int pos,char* buf);
int printquery(int pos,char* buf);
int decodename(char* buf, int pos, char* dst);

int main(int argc, char** argv){
  int mysock = socket(AF_INET,SOCK_DGRAM,0);
  if(mysock<0){
    printf("There was an error creating the socket\n");
    return 1;
  }
  int port = htons(53);
  int addr = inet_addr("8.8.8.8");
  struct sockaddr_in dnsserver;
  dnsserver.sin_port = port;
  dnsserver.sin_addr.s_addr = addr;
  dnsserver.sin_family=AF_INET;

  srand(time(NULL));
  dnshdr queryheader ={};
  queryheader.id=(u_int16_t)rand();
  queryheader.rd=1;
  queryheader.qcount=htons(1);

  char buf[512] = {};
  memcpy(buf,&queryheader,12);
  char qname[] = "www.google.com";
  int namelength=strlen(qname)+2;
  char *encodedname = (char*)malloc(namelength);
  encodename(qname,encodedname);
  memcpy(&buf[12],encodedname,namelength);
  buf[12+namelength+1]=TYPE_A;
  buf[12+namelength+3]=CLASS_IN;
  sendto(mysock,buf,16+namelength,0,(struct sockaddr*)&dnsserver,sizeof(dnsserver));

  char recvbuf[512];
  socklen_t len = sizeof(dnsserver);
  recvfrom(mysock,recvbuf,512,0,(struct sockaddr*)&dnsserver,&len);
  dnshdr replyheader;
  memcpy(&replyheader,recvbuf,12);
  if(replyheader.rcode!=0){
    printf("error, rcode was %d\n",replyheader.rcode);
    return 0;
  }
  u_int16_t ancount = ntohs(replyheader.ancount);
  if(ancount==0){
    printf("got no answers, :(\n");
    return 0;
  }
  int curpos=12;
  curpos=printquery(curpos,recvbuf);
  for(int i=0; i<ancount; i++){
    printf("Answer %d\n",i);
    curpos=printreply(curpos,recvbuf);
  }

  return 0;
}

void encodename(char* src, char* dst){
  int i=0;
  int pos=0;
  while(src[i]!='\0'){
    if(src[i]=='.'){
      dst[pos]=i-pos;
      pos=i+1;
    } else {
    dst[i+1]=src[i];
    }
    ++i;
  }
  dst[pos]=i-pos;
  dst[i+1]=0;
}

int decodename(char* buf, int pos, char* dst){
  int start=pos;
  int ret=0;
  int j=0;
  while(buf[pos]!=0){
    if((buf[pos]&0xC0)==0xC0){ //pointer & is a bitwise and, | is a bitwise or, ^ is a bitwise xor, ~ flips all bits
      if(ret==0){
	ret=(pos-start)+2;
      }
      pos = (buf[pos]&(~0xC0))<<8+buf[pos];
    } else {
      int len = buf[pos];
      if(j!=0){
	dst[j]='.';
	j++;
      }
      for(int i=0; i<len; i++){
	dst[j+i]=buf[pos+i+1];
      }
      j+=len;
      pos+=len+1;
    }
  }
  dst[j]='\0';
  if(ret==0){
    ret=(pos-start)+1;
  }
  return ret;
}

int printreply(int pos,char* buf){
  //this is where i would print the reply, but i have not written it
  // records are name, type, class, ttl, datalength, data
  // address of .com is stored in one of the records of the additional section
}

int printquery(int pos,char* buf){
  char name[256];
  int namelen = decodename(buf,pos,name);
  int dnstype = buf[pos+namelen+1];
  int dnsclass = buf[pos+namelen+3];
  printf("Query: %s\t%d\t%d\n",name,dnstype,dnsclass);
  return pos+namelen+4;
}
