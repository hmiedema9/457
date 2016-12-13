import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.lang.*;
import java.util.ArrayList;

/**********************************************************************************
 * CIS 457
 * Project 2
 *
 * This class is a helper class that extracts info from Datagram Packets
 *
 * @author Jonathan Powers, Brett Greenman, Kevin Anderson
 * @version Fall 2015
 **********************************************************************************/

class PacketInfo {

    /** bit masks */
    private static final int QR_FLAG = 0x8000;
    private static final int AA_FLAG = 0x0400;
    private static final int TC_FLAG = 0x0200;
    private static final int RD_FLAG = 0x0100;
    private static final int RA_FLAG = 0x0080;
    private static final int RCODE_FLAG = 0x000F;

    /** packet information */
    private byte[] data;
    private int offset;
    private int length;

    /** header variables */
    private int id;

    private int flags;
    private int qr;
    private int opcode;
    private int aa;
    private int tc;
    private int rd;
    private int ra;
    private int z;
    private int rcode;

    private int qdcount;
    private int ancount;
    private int nscount;
    private int arcount;

    //variables for question
    private int qtype;
    private int qclass;
    private String parsedName;

    /** variables to hold return values containing useful data */
    private boolean validQuestion = true;
    private String ipAddress = "";
    private ArrayList<String> authority = new ArrayList<String>();
    private String nameRequested = "";
    private ArrayList<String> answerRecords = new ArrayList<String>();

    /*********************************************************************
     * Constructor that populates all instance variable fields with
     * information about the packet that was sent as a parameter
     *********************************************************************/
    public PacketInfo(DatagramPacket packet) {

        data = packet.getData();
        offset = packet.getOffset();
        length = packet.getLength();

        try {

            //read headers
            id = readUnsignedShort();
            flags = readUnsignedShort();
            qdcount = readUnsignedShort();
            ancount = readUnsignedShort();
            nscount = readUnsignedShort(); //authcount
            arcount = readUnsignedShort(); //addcount

            //apply masks to flag values
            if((flags & QR_FLAG) == QR_FLAG)
                qr = 1;
            else qr = 0;

            if((flags & AA_FLAG) == AA_FLAG)
                aa = 1;
            else aa = 0;

            if((flags & TC_FLAG) == TC_FLAG)
                tc = 1;
            else tc = 0;

            if((flags & RD_FLAG) == RD_FLAG)
                rd = 1;
            else rd = 0;

            if((flags & RA_FLAG) == RA_FLAG)
                ra = 1;
            else ra = 0;

        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }

    /*****************************************************************
     * Method that concatenates two consecutive bytes in a byte array
     * simulating a 16 bit unsigned number
     *****************************************************************/
    private int readUnsignedShort() throws IOException {

        return (get(offset++) << 8) + get(offset++);
    }

    /***********************************************************
     * Method that reads one byte as an unsigned number
     ***********************************************************/
    private int get(int offset) throws IOException {
        if ((offset < 0) || (offset >= length))
        {
            throw new IOException("offset out of range error: offset=" + offset);
        }
        return data[offset] & 0xFF;
    }

    /******************************************************************
     * Parses packet questions and returns a string represeting this
     * information
     ******************************************************************/
    public String getQuestions() throws IOException {

        String qString = "";

        if(qdcount == 0)
            return "Packet contains no question records\n";

        //loop for every question record
        for(int i = 0; i < qdcount; i++) {

            qString += ("Question " + i + " record\n" + "Name: ");

            int count = data[offset++];
            qString += count;
            nameRequested += count;

            //loop through characters in qname record
            while(count != 0) {
                for(int j = 0; j < count; j++) {
                    char nextLetter = (char)data[offset++];
                    qString += nextLetter;
                    nameRequested += nextLetter;
                    parsedName += nextLetter;
                    if((j+1) == count){
                        parsedName += ".";
                    }
                }
                count = data[offset++];
                qString += count;
                nameRequested += count;
            }


            //read qtype record
            qtype = readUnsignedShort();
            qString += ("\n" + "Type: " + qtype);
            if(qtype != 1)
                validQuestion = false;

            //read qclass record
            qclass = readUnsignedShort();
            qString += ("\n" + "Class: " + qclass);
            if(qclass != 1)
                validQuestion = false;
        }
        return qString;
    }

    /****************************************************************************************
     * Parses packet non question responses and returns a string represeting the information
     ****************************************************************************************/
    private String getResponse(int typeCount, boolean ansFlag, boolean fillAnswer) throws IOException{

        String aString = "";
        boolean pointerFlag = false;
        int pointerStart = 0;
        int count;

        //loop for every answer record
        for(int i = 0; i < typeCount; i++) {

            aString += ("\n\nRecord " + i + "\n" + "Name: ");

            //Check if name record contains a pointer instead of actual data
            if(((int)(data[offset] & 0xC0) == 0xC0)) {
                count = 1;
            }
            else {
                count = data[offset++];
                aString += count;
            }

            /** loop through characters in qname record */
            while(count != 0) {
                for(int j = 0; j < count; j++) {

                    //Before each character read, check to see if next byte is pointer
                    if((int)(data[offset] & 0xC0) == 0xC0) {
                        pointerStart = readUnsignedShort();
                        pointerStart = (pointerStart ^ 0xC000);
                        aString += readPointer(pointerStart);
                    }

                    else {
                        char nextLetter;
                        nextLetter = (char)data[offset++];
                        aString += nextLetter;
                    }
                }
                if((int)(data[offset] & 0xC0) == 0xC0)
                    count = 1;
                else if(aString.charAt(aString.length() -1) == '0') {
                    count = 0;
                }
                else {
                    count = data[offset++];
                    aString += count;
                }
            }

            //reset pointer flag
            pointerFlag = false;

            /** read type record */
            int qtype = readUnsignedShort();
            aString += ("\n" + "Type: " + qtype);

            /** read class record */
            int qclass = readUnsignedShort();
            aString += ("\n" + "Class: " + qclass);

            /** read ttl record */
            int firstHalfTTL = readUnsignedShort();
            int secondHalfTTL = readUnsignedShort();
            int ttl = ((firstHalfTTL << 16) | secondHalfTTL);
            aString += ("\n" + "TTL: " + ttl);

            /** read rdlength record */
            int rdlength = readUnsignedShort();
            aString += ("\n" + "RDLENGTH: " + rdlength);

            /** read rdata record */
            aString += ("\n" + "RDATA: ");
            if(qtype == 1)
                ipAddress = "";

            if(ansFlag)
                count = rdlength;
            else {
                count = data[offset++];
                aString += count;
            }

            while(count != 0) {
                for(int j = 0; j < count; j++) {

                    if(ansFlag && qtype == 1) {
                        int nextNum;
                        nextNum = data[offset++];
                        nextNum = (nextNum & 0xFF);
                        aString += (nextNum + " ");
                        ipAddress += nextNum;
                        if(j < (rdlength -1))
                            ipAddress += ".";
                        else {
                            if(fillAnswer)
                                answerRecords.add(ipAddress);
                            authority.add(ipAddress);
                        }
                    }

                    else {
                        char nextLetter;
                        nextLetter = (char)data[offset++];
                        aString += nextLetter;
                    }

                    //After each character read, check to see if next byte is pointer
                    if(!ansFlag && ((int)(data[offset] & 0xC0) == 0xC0)) {
                        pointerStart = readUnsignedShort();
                        pointerStart = (pointerStart ^ 0xC000);
                        aString += readPointer(pointerStart);
                        pointerFlag = true;
                    }
                }

                if(pointerFlag || ansFlag) {
                    count = 0;
                    pointerFlag = false;
                }
                else {
                    count = data[offset++];
                    aString += count;
                }
            }

        }
        aString += "\n";
        return aString;

    }

    /********************************************************************
     * Reads information in a pointer and returns as a string
     ********************************************************************/
    private String readPointer(int start) throws IOException{

        int startVal = start;
        String retVal = "";
        char nextLetter;
        int count = 0;
        int pointerStart;

        //Check if record contains a pointer instead of actual data
        if(((int)(data[startVal] & 0xC0) == 0xC0)) {
            int temp = offset;
            offset = startVal;
            pointerStart = readUnsignedShort();
            pointerStart = (pointerStart ^ 0xC000);
            offset = temp;
            retVal += readPointer(pointerStart);
        }
        else
            count = data[startVal++];
        retVal += count;

        //read record contained in pointer
        while(count != 0) {
            for(int j = 0; j < count; j++) {

                nextLetter = (char)data[startVal++];
                retVal += nextLetter;

                //After each character read, check to see if next byte is pointer
                if((int)(data[startVal] & 0xC0) == 0xC0) {
                    int temp2 = offset;
                    offset = startVal;
                    pointerStart = readUnsignedShort();
                    pointerStart = (pointerStart ^ 0xC000);
                    offset = temp2;
                    retVal += readPointer(pointerStart);
                }
            }

            if(retVal.charAt(retVal.length() -1) == '0') {
                count = 0;
            }
            else {
                count = data[startVal++];
                retVal += count;
            }
        }
        return retVal;
    }

    /******************************************************************
     * Parses packet answers and returns a string representing this
     * information
     ******************************************************************/
    public String getAnswers() throws IOException{

        if(ancount == 0)
            return "Packet contains no answer records\n";
        else {
            return ("Answer\n" + "*********\n" + getResponse(ancount, true, true));
        }
    }

    /***********************************************************************
     * Parses packet authority records and returns a string represeting this
     * information
     ***********************************************************************/
    public String getAuthority() throws IOException{

        if(nscount == 0)
            return "Packet contains no authority records\n";
        else
            return ("Authority\n" + "*********\n" + getResponse(nscount, false, false));
    }

    /******************************************************************
     * Parses packet additional records and returns a string represeting
     * this information
     ******************************************************************/
    public String getAdditional() throws IOException{

        if(arcount == 0)
            return "Packet contains no Additional records\n";
        else
            return ("Additional\n" + "*********\n" + getResponse(arcount, true, false));
    }

    /******************************************************************
     * Method to check if packet contains an answer record
     ******************************************************************/
    public boolean isAnswer() {
        return(ancount >= 1);
    }

    /*****************************************************************
     * Method to check if there is an error code in the packet
     *****************************************************************/
    public boolean isError() {
        return(rcode >= 1);
    }

    /*****************************************************************
     * Method to return an IP Address as a string for recursion
     *****************************************************************/
    public String nextIP() {
        return ipAddress;
    }

    /****************************************************************
     * Method to return an ArrayList of all IPv4 addresses found
     ****************************************************************/
    public ArrayList<String> getResults() {
        return authority;
    }

    /***************************************************************
     * Method to return the name of the original website requested
     ***************************************************************/
    public String getNameRequested() {
        return nameRequested;
    }

    /**************************************************************
     * Method to return an ArrayList containing all answer record
     * IP addresses
     ***************************************************************/
    public ArrayList<String> getAnswerRecords() {
        return answerRecords;
    }

    /**************************************************
     * Getter for private boolean validQuestion
     **************************************************/
    public boolean getValidQuestion() {

        return validQuestion;
    }

    /******************************************************
     * Getter for data byte array
     ******************************************************/
    public byte[] getByteArray() {

        return Arrays.copyOfRange(data, 0, offset);
    }

    /******************************************************
     * Unsets recursion desired bit flag in data array
     ******************************************************/
    public void unsetRecursion() {

        data[2] = (byte)(data[2] ^ 0x01);
        rd = 0;
    }

    /*****************************************************
     * Sets rcode error flag to show unsupported query
     *****************************************************/
    public void setErrorCode() {
        data[3] = (byte)(data[3] ^ 0x04);
        rcode = 4;
    }

    /******************************************************************
     * Returns packet information as a string.
     *******************************************************************/
    public String getValues() {
        return ("" + "ID: " + id + "\nFlags: " + flags +
                "\n\tQR: " + qr + "\n\tAA: " + aa + "\n\tTC: " + tc + "\n\tRD: "
                + rd + "\n\tRA: " + ra + "\n\tRCODE: " + rcode + "\nQDCOUNT: " + qdcount +
                "\nANCOUNT: " + ancount + "\nNSCOUNT: " + nscount + "\nARCOUNT: " +
                arcount + "\nCurrent Offset: " + offset + "\n");
    }

    public int getID(){
        return id;
    }

    public int getQType(){
        return qtype;
    }

    public int getQClass(){
        return qclass;
    }

    public String getParsedName(){
        return parsedName;
    }
}