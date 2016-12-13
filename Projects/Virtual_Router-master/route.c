#include <sys/socket.h> 
#include <netpacket/packet.h> 
#include <net/ethernet.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <string>
#include <vector>
#include <iostream>
#include <errno.h>
#include <sys/types.h>
#include <ifaddrs.h>
#include <fstream>
#include <stdint.h>
#include <arpa/inet.h>
#include <netinet/in.h>



#define ETH_HTYPE 1
#define IPV4_PTYPE 2048
#define ETHER_HLEN 6
#define IPV4_PLEN 4
#define OPER_REQUEST 1
#define OPER_REPLY 2
#define ARP_SIZE 28
#define ARP_TYPE 2054
#define ICMP_TYPE 2048
#define R1_TABLE 1
#define R2_TABLE 2
#define TABLE_ROWS 6
#define TABLE_COLS 3
#define BUF_SIZE 150

using namespace std;

typedef struct eth_hdr{
	char dst_addr[6];
	char src_addr[6];
	uint16_t _type;
}eth_hdr;

typedef struct arp_hdr{
	uint16_t htype;// ETH_HTYPE;
	uint16_t ptype;// IPV4_PTYPE;
	uint8_t hlen;// ETH_HLEN;
	uint8_t plen;// IPV4_PLEN;
	uint16_t oper;
	char sha[6];
	char spa[4];
	char tha[6];
	char tpa[4];
}arp_hdr;

typedef struct icmp_hdr{
	uint8_t _type;
	uint8_t code;
	uint16_t checksum;
	uint16_t identifier;
	uint16_t seq_num;
	char tmstmp[8];
	char data[48];
}icmp_hdr;

typedef struct ip_hdr{
	uint8_t version: 4;
	uint8_t ihl: 4;
	uint8_t dscb;
	uint16_t total_length;
	uint16_t id;
	uint16_t frag_offset;
	uint8_t ttl;
	uint8_t protocol;
	uint16_t checksum;
	char src_addr[4];
	char dst_addr[4];
}ip_hdr;

typedef struct routing_column{
	string net_prefix;
	string hop_addr;
	string interface;
}routing_column;

void print_ip(ip_hdr& ip){
	int i;
	printf("Version: %02x\n",ip.version);
	printf("ihl: %02x\n",ip.ihl);
	printf("DSCB: %02x\n",ip.dscb);
	printf("Total Length: %02x\n",ip.total_length);
	printf("ID: %02x\n",ip.id);
	printf("Frag Offset: %02x\n",ip.frag_offset);
	printf("TTL: %02x\n",ip.ttl);
	printf("Protocol: %02x\n",ip.protocol);
	printf("Checksum: %02x\n",ip.checksum);
	printf("SRC: ");
	for(i=0; i<4; i++){
		printf("%02x", (unsigned char) ip.src_addr[i]);
	}
	printf("\nDST: ");
	for(i=0; i<4; i++){
		printf("%02x", (unsigned char) ip.dst_addr[i]);
	}
}

void print_eth(eth_hdr& eth){
	int i;
	cout << endl << endl << "ETHERNET HEADER" << endl << endl;
	cout << "Destination Address: "; 
	for(i=0; i<6; i++){
		printf("%02x ", (unsigned char)eth.dst_addr[i] );
	}
	printf("\n");

	cout << "Source Address: ";
	for(i=0; i<6; i++){
		printf("%02x ", (unsigned char)eth.src_addr[i]);
	}
	printf("\n");

	cout << "Type: " << (unsigned char)eth._type << endl;
}

void print_arp(arp_hdr& arp){
	int i;
	cout << endl << endl << "ARP HEADER" << endl << endl;
	cout << "Sender IP Address: ";
	for(i=0; i<4; i++){
		printf("%02x ", (unsigned char)arp.spa[i] );
	}
	printf("\n");
	cout << "Target IP Address: ";
	for(i=0; i<4; i++){
		printf("%02x ", (unsigned char)arp.tpa[i] );
	}
}

void print_buf(char* buf){
	int i, j;
	printf("\n\nBUFFER CONTENTS\n\n");
	for(i=0; i<BUF_SIZE; i++){
		printf("%02x ", (unsigned char)buf[i]);
		if(i%16 == 0 && i != 0){
			printf("\n");
		}
	}
}

void pull_eth(eth_hdr& eth, char* buf, int eth_size){
	memcpy(&eth, buf, eth_size);
}


void pull_arp(arp_hdr& arp, char* buf, int eth_size){
	memcpy(&arp, &buf[eth_size], 28);
}

void pull_ip(ip_hdr& ip, char* buf, int eth_size){
	memcpy(&ip, &buf[eth_size], 20);
}

void pull_icmp(icmp_hdr& icmp, char* buf, int eth_size){
	memcpy(&icmp, &buf[eth_size+20], 64);
}

void push_arp(arp_hdr& arp, eth_hdr& eth, char* buf, int eth_size){
	memset(buf, 0, BUF_SIZE);
	memcpy(buf, &eth, eth_size);
	memcpy(&buf[eth_size], &arp, 28);
}

void push_icmp(icmp_hdr& icmp, ip_hdr& ip, eth_hdr& eth, char* buf, int eth_size){
	memset(buf, 0, BUF_SIZE);
	memcpy(buf, &eth, eth_size);
	memcpy(&buf[eth_size], &ip, 20);
	memcpy(&buf[eth_size+20], &icmp, 64);
}



void fill_table(vector<struct routing_column>& table){
	int i, j, k;
	string cell;
	vector<string> elements;
	routing_column col;
	//if(type == R1_TABLE){
	ifstream file ("r1_table.txt");

	if(file.is_open()){
		while(file >> cell){
			elements.push_back(cell);
		}
		for(i=0; i<elements.size(); i+=3){
			col.net_prefix = elements[i];		
			col.hop_addr = elements[i+1];
			col.interface = elements[i+2];
			table.push_back(col);
		}
	}
	else{
		printf("\nError reading routing table\n");
	}

}	
uint16_t ip_calc_checksum(void* vdata,size_t length) {
    // Cast the data pointer to one that can be indexed.
    char* data=(char*)vdata;

    // Initialise the accumulator.
    uint32_t acc=0xffff;

    // Handle complete 16-bit blocks.
    for (size_t i=0;i+1<length;i+=2) {
        uint16_t word;
        memcpy(&word,data+i,2);
        acc+=ntohs(word);
        if (acc>0xffff) {
            acc-=0xffff;
        }
    }

    // Handle any partial block at the end of the data.
    if (length&1) {
        uint16_t word=0;
        memcpy(&word,data+length-1,1);
        acc+=ntohs(word);
        if (acc>0xffff) {
            acc-=0xffff;
        }
    }

    // Return the checksum in network byte order.
    return htons(~acc);
}


/*unsigned short icmp_calc_checksum(struct icmp_hdr *icmp, int n){
	unsigned short sum = 0;
	int i;
	for(i=0; i<n; i++)
		sum += (unsigned short)* (icmp+i);

	return ~sum;
}*/


int main(){
	int packet_socket;
	int eth_size;
	arp_hdr arp;
	eth_hdr eth;
	ip_hdr ip;
	icmp_hdr icmp;
	uint32_t temp1;
	uint16_t temp2;
	sockaddr_in *sa;
	char *router_ip;
	char macp[6];
	struct sockaddr_ll *s;
	int i;
	int j = 0;
	vector<struct routing_column> routing_table;
	//get list of interfaces (actually addresses)
	struct ifaddrs *ifaddr, *tmp;
	if(getifaddrs(&ifaddr)==-1){
		perror("getifaddrs");
		return 1;
	}
	//have the list, loop over the list
	for(tmp = ifaddr; tmp!=NULL; tmp=tmp->ifa_next){
		//Check if this is a packet address, there will be one per
		//interface.  There are IPv4 and IPv6 as well, but we don't care
		//about those for the purpose of enumerating interfaces. We can
		//use the AF_INET addresses in this list for example to get a list
		//of our own IP addresses
		if(tmp->ifa_addr->sa_family==AF_PACKET){		

			//create a packet socket on interface r?-eth1
			if(!strncmp(&(tmp->ifa_name[3]),"eth1",4)){
				printf("Interface: %s\n",tmp->ifa_name);
				printf("Creating Socket on interface %s\n",tmp->ifa_name);

				//Harvest MAC address
				s = (struct sockaddr_ll *)tmp->ifa_addr;
				
	            int len = 0;
	            for(i = 0; i < 6; i++)
	                macp[i] = s->sll_addr[i];
	
				
				//create a packet socket
				//AF_PACKET makes it a packet socket
				//SOCK_RAW makes it so we get the entire packet
				//could also use SOCK_DGRAM to cut off link layer header
				//ETH_P_ALL indicates we want all (upper layer) protocols
				//we could specify just a specific one
				packet_socket = socket(AF_PACKET, SOCK_RAW, htons(ETH_P_ALL));
				if(packet_socket<0){
					perror("socket");
					return 2;
				}
				//Bind the socket to the address, so we only get packets
				//recieved on this specific interface. For packet sockets, the
				//address structure is a struct sockaddr_ll (see the man page
				//for "packet"), but of course bind takes a struct sockaddr.
				//Here, we can use the sockaddr we got from getifaddrs (which
				//we could convert to sockaddr_ll if we needed to)
				
				if(bind(packet_socket,tmp->ifa_addr,sizeof(struct sockaddr_ll))==-1){
					perror("bind");
				}
			}
		}

		if(tmp->ifa_addr->sa_family==AF_INET){
			if(!strncmp(&(tmp->ifa_name[3]),"eth1",4)){
				sa = (struct sockaddr_in *) tmp->ifa_addr;
				router_ip = inet_ntoa(sa->sin_addr);
				printf("IP addr: %s\n", router_ip);
			}
		}
	}
	//free the interface list when we don't need it anymore
	freeifaddrs(ifaddr);

	//loop and recieve packets. We are only looking at one interface,
	//for the project you will probably want to look at more (to do so,
	//a good way is to have one socket per interface and use select to
	//see which ones have data)

	//Variables used in loop
	

	printf("Ready to recieve now\n");
	


	while(1){
		char buf[BUF_SIZE];
		memset(buf, 0, BUF_SIZE);
		struct sockaddr_ll recvaddr;
		unsigned int recvaddrlen=sizeof(struct sockaddr_ll);
		//we can use recv, since the addresses are in the packet, but we
		//use recvfrom because it gives us an easy way to determine if
		//this packet is incoming or outgoing (when using ETH_P_ALL, we
		//see packets in both directions. Only outgoing can be seen when
		//using a packet socket with some specific protocol)
		int n = recvfrom(packet_socket, buf, 1500,0,(struct sockaddr*)&recvaddr, &recvaddrlen);
		//ignore outgoing packets (we can't disable some from being sent
		//by the OS automatically, for example ICMP port unreachable
		//messages, so we will just ignore them here)
		if(recvaddr.sll_pkttype==PACKET_OUTGOING)
			continue;
		//start processing all others
		printf("\n\n\nGot a %d byte packet\n", n);

		//what else to do is up to you, you can send packets with send,
		//just like we used for TCP sockets (or you can use sendto, but it
		//is not necessary, since the headers, including all addresses,
		//need to be in the buffer you are sending)
		


		

		pull_eth(eth, buf, 14);

		printf("\nType: %x\n", eth._type);	

		if(htons(eth._type) == ARP_TYPE){
			printf("\nARP\n");

			//Determine Size of Ethernet Header
			eth_size = n - ARP_SIZE;
			//Fill arp header struct with received data
			pull_arp(arp, buf, eth_size);
			//print_buf(buf);
			//print_eth(eth);
			//print_arp(arp);

			fill_table(routing_table);

			//Print Routing Table
			/*
			for(int i=0; i<routing_table.size(); i++){
				cout << routing_table[i].net_prefix << " ";
				cout << routing_table[i].hop_addr << " ";
				cout << routing_table[i].interface << endl;
			}*/	

			//Reply to MAC address where message came from
			memcpy(&eth.dst_addr, &eth.src_addr, 6);
			//Set our MAC address as the source
			memcpy(&eth.src_addr, macp, 6);	

			memcpy(&arp.tha, &arp.sha, 6);

			memcpy(&arp.sha, macp, 6);

			memcpy(&arp.tpa, &arp.spa, 4);

			char new_ip[4];
			j=0;
			printf("\n%s\n",router_ip);
			printf("IPADDR: ");
			for(i=0; i<sizeof(router_ip); i++){		
				if(router_ip[i] != '.'){
					if(i==1)
						continue;					
					new_ip[j] = atoi(&router_ip[i]);
					printf("%02x ", (unsigned char) new_ip[j]);
					j++;
				}
			}
			printf("\n");

			memcpy(&arp.spa, new_ip , 4);
			arp.oper = htons(OPER_REPLY);
			//print_eth(eth);
			//print_arp(arp);
			push_arp(arp, eth, buf, eth_size);
			send(packet_socket, buf, n, 0);
		}
		else if(htons(eth._type) == ICMP_TYPE){
			//Determine Size of Ethernet Header
			
			printf("\nICMP\n");
			eth_size = 14;
			pull_ip(ip, buf, eth_size);
			pull_icmp(icmp, buf, eth_size);

			//Construct Ethernet header
			//Reply to MAC address where message came from
			memcpy(&eth.dst_addr, &eth.src_addr, 6);
			//Set our MAC address as the source
			memcpy(&eth.src_addr, macp, 6);

			//Construct IP header
			//Set correct IP adresses in IP header
			ip.checksum = 0;
			ip.checksum = ip_calc_checksum(&ip, 20);
			memcpy(&ip.dst_addr, &ip.src_addr, 4);
			char new_ip[4];
			j=0;
			for(i=0; i<sizeof(router_ip); i++){		
				if(router_ip[i] != '.'){
					if(i==1)
						continue;
					printf("%c", (unsigned char) router_ip[i]);
					new_ip[j] = atoi(&router_ip[i]);
					j++;
				}
			}
			memcpy(&ip.src_addr, new_ip , 4);


			//Construct ICMP header
			icmp.checksum = 0;
			icmp.checksum = ip_calc_checksum(&icmp, 64);
			icmp._type = 0;
			

			push_icmp(icmp, ip, eth, buf, eth_size);
			print_ip(ip);
			send(packet_socket, buf, n, 0);
		}

		
	}
	//exit
	return 0;
}
