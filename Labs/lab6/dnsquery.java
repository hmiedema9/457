fimport java.io.*;
import java.net.*;
import java.util.Random;
import java.nio.ByteBuffer;
import java.util.StringTokenizer;

class dnsquery{
    public static void main(String args[]) throws Exception{
	DatagramSocket mySocket = new DatagramSocket();
	InetAddress serverAddress = InetAddress.getByName("8.8.8.8");
	int serverPort = 53;
	
	dnspacket googlequery = new dnspacket("www.google.com");
	byte[] sendData = googlequery.getQueryBytes();
	DatagramPacket sendPacket = new DatagramPacket(sendData,sendData.length,serverAddress,serverPort);
	mySocket.send(sendPacket);
	byte[] recvData = new byte[512];
	DatagramPacket recvPacket = new DatagramPacket(recvData,recvData.length);
	mySocket.receive(recvPacket);
	dnspacket responsepacket = new dnspacket(recvData);
	if(responsepacket.getRCode()!=0){
	    System.out.println("Got an error, rcode is "+responsepacket.getRCode());
	    System.exit(0);
	}
	if(responsepacket.getAnswerCount()==0){
	    System.out.println("We got no answers, must be some problem");
	    System.exit(0);
	}
	dnsquestion responsequestion = responsepacket.getQuestion();
	System.out.println("Query: "+responsequestion.getQName() + "\t" + responsequestion.getDnsType() + "\t" + responsequestion.getDnsClass());
    }
}

class dnsquestion{
    String dnsname;
    short dnstype;
    short dnsclass;

    public dnsquestion(){
	dnsname="";
	dnstype=dnspacket.TYPE_A;
	dnsclass=dnspacket.CLASS_IN;
    }

    public short getDnsType(){
	return dnstype;
    }
    
    public short getDnsClass(){
	return dnsclass;
    }

    public String getQName(){
	return dnsname;
    }

    public void setQName(String name){
	dnsname = name;
    }

    public void setDnsType(short type){
	dnstype=type;
    }

    public void setDnsClass(short clas){
	dnsclass = clas;
    }
}



class dnspacket{

    public static final short TYPE_A = 1;
    public static final short CLASS_IN = 1;

    short id;
    short flags;
    short qcount;
    short ancount;
    short authcount;
    short addcount;
    dnsquestion question;
    
    public dnspacket(){
	id=(short)(new Random()).nextInt();
	flags=0;
	qcount=1;
	ancount=0;
	authcount=0;
	addcount=0;
	question = new dnsquestion();
    }

    public dnspacket(String name){
	this();
	this.question.setQName(name);
	this.setRD();
    }

    public dnspacket(byte[] packetbytes){
	//make the byte buffer
	ByteBuffer b = ByteBuffer.wrap(packetbytes);
	//read the basic header fields
	id = b.getShort();
	flags = b.getShort();
	qcount = b.getShort();
	ancount = b.getShort();
	authcount = b.getShort();
	addcount = b.getShort();
	//read the query name
	question = new dnsquestion();
	question.setQName(readDNSName(b));
	question.setDnsType(b.getShort());
	question.setDnsClass(b.getShort());
	//should now read the resource records in a loop
	//probably make a separate class for them
	//and put them in a list or something like that
    }

    private String readDNSName(ByteBuffer b){
	//slightly untested for pointers, but looks correct 
	byte labellength = b.get();
	String result="";
	while(labellength!=0){
	    if((labellength&0xC0)==0xC0){
		byte nextbyte = b.get();
		int target = ((labellength&0x3F)<<8)+nextbyte;
		ByteBuffer nb = b.duplicate();
		nb.position(target);
		if(result!=""){
		    result +=".";
		}
		result += readDNSName(nb);
	    }
	    byte[] tmp = new byte[labellength];
	    b.get(tmp);
	    String token = new String(tmp);
	    if(result!=""){
		result +=".";
	    }
	    result += token;
	    labellength = b.get();
	}
	return result;
    }

    public byte[] getQueryBytes(){
	ByteBuffer b = ByteBuffer.allocate(512);
	b.putShort(id);
	b.putShort(flags);
	b.putShort(qcount);
	b.putShort(ancount);
	b.putShort(authcount);
	b.putShort(addcount);
	StringTokenizer st = new StringTokenizer(question.getQName(),".");
	while(st.hasMoreTokens()){
	    String token = st.nextToken();
	    b.put((byte)token.length());
	    b.put(token.getBytes());
	}
	b.put((byte)0); //end of domain name marker
	b.putShort(querypacket.TYPE_A);
	b.putShort(querypacket.CLASS_IN);
	b.flip();
	byte[] ba = new byte[b.limit()];
	b.get(ba);
	return ba;
    }
    
    public void setRD(){
	//set the RD bit. Unlike in C, in Java this is exactly where
	//we would expect based on the documentation
	flags = (short)(flags | (1<<8));
    }

    public short getAnswerCount(){
	return ancount;
    }

    public short getRCode(){
	return (short)(flags&(short)0b0000000000001111);
    }

    public dnsquestion getQuestion(){
	return question;
    }
}

