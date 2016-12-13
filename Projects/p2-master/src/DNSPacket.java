import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Douglas on 2/17/2016.
 *
 */

public class DNSPacket {
    public static final short TYPE_A = 1;
    public static final short CLASS_IN = 1;
    private byte[] packetBytes;
    private int offset;
    private int flags;
    private int rCode;
    private int anCount;
    private int nsCount;
    private int arCount;
    private int packetTTL;
    private boolean validQuestion = true;
    private String ipAddress = "";
    private ArrayList<String> answerRecords = new ArrayList<>();
    DNSQuestion question;
    DNSAnswer answer;
    final protected static char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};

    public DNSPacket( byte[] packetBytes) throws IOException {
        ByteBuffer b = ByteBuffer.wrap(packetBytes);
        this.packetBytes = packetBytes;
        offset = 12; //set for start of packet
        int id = b.getShort();
        flags = b.getShort();
        int qCount = b.getShort();
        anCount = b.getShort();
        nsCount = b.getShort();
        arCount = b.getShort();
        question = new DNSQuestion(readDNSName(b),b.getShort(),b.getShort());
        if(question.getDnsType() != TYPE_A) validQuestion = false;
        if(question.getDnsClass() != CLASS_IN) validQuestion = false;
        offset += 1+ question.getQName().length() + 1 + 4;

        parsePacket();
        answer = new DNSAnswer();

    }
    private void hexDump(byte[] bytes) {
        System.out.println(bytesToHex(bytes));
    }
    private  String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for ( int j = 0; j < bytes.length; j++ ) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
    private String readDNSName(ByteBuffer b){
        //slightly untested for pointers, but looks correct
        byte labelLength = b.get();
        String result="";
        while(labelLength!=0){
            if((labelLength&0xC0)==0xC0){
                byte nextbyte = b.get();
                int target = ((labelLength&0x3F)<<8)+nextbyte;
                ByteBuffer nb = b.duplicate();
                nb.position(target);
                if(!result.equals(""))
                    result +=".";
                result += readDNSName(nb);
            }
            byte[] tmp = new byte[labelLength];
            b.get(tmp);
            String token = new String(tmp);
            if(!result.equals("")){
                result +=".";
            }
            result += token;
            labelLength = b.get();
        }
        return result;
    }
    private void parsePacket()throws  IOException{
        parseResponseRecords(anCount, true);
        parseResponseRecords(nsCount, false);
        parseResponseRecords(arCount, true);
    }
    private void parseResponseRecords(int typeCount, boolean answer) throws IOException{
        boolean pointer;
        int count;
        //loop for every response record
        for(int i = 0; i < typeCount; i++) {

            if(byteIsPointer()) count = 1;
            else count = packetBytes[offset++];
            //loop names
            while(count != 0) {
                for(int j = 0; j < count; j++)
                    if(byteIsPointer()) offset+=2;
                    else offset++;
                    if(byteIsPointer()) count = 1;
                else if(packetBytes[offset] == 0) count = 0;
                else count = packetBytes[offset++];
            }
            pointer = false;
            int qtype =  ((packetBytes[offset++] & 0xff) << 8) | (packetBytes[offset++] & 0xff);
            int qclass = ((packetBytes[offset++] & 0xff) << 8) | (packetBytes[offset++] & 0xff);
            int ttl = readTTL();
            this.packetTTL = ttl;
            int rdlength = ((packetBytes[offset++] & 0xff) << 8) | (packetBytes[offset++] & 0xff);

            if(qtype == 1) ipAddress = "";
            if(answer) count = rdlength;
            else count = packetBytes[offset++];
            //parse ip
            while(count != 0) {
                for(int j = 0; j < count; j++) {
                    if(answer && qtype == 1) {
                        int nextNum= packetBytes[offset++] & 0xFF;
                        ipAddress += nextNum;
                        if(j < (rdlength -1)) ipAddress += ".";
                        else answerRecords.add(ipAddress);
                    }else offset++;
                    if(!answer && byteIsPointer()) {
                        offset+=2;
                        pointer = true;
                    }
                } //end for
                if(pointer || answer) {
                    count = 0;
                    pointer = false;
                }else count = packetBytes[offset++];
            }//end while count
        }//end for
    }
    public String getQuestionName(){
        return this.question.getQName();
    }
    public boolean isAnswer() {
        return(anCount >= 1);
    }
    public void setAnswer(){
        packetBytes[2] ^= 0x800;
    }
    public boolean isError() {
        return(rCode >= 1);
    }
    public String getNextIP() {
        return ipAddress;
    }
    public ArrayList<String> getAnswerRecords() {
        return answerRecords;
    }
    public boolean isValidQuestion() {
        return validQuestion;
    }
    public byte[] getQueryBytes() {
        return Arrays.copyOfRange(packetBytes, 0, offset);
    }
    public void unsetRD() {
        packetBytes[2] ^= 0x01;
        int rd = 0;
    }
    public void setQRCode(){
        packetBytes[2] ^= 0x8000;
        int qr = 0;
    }
    public void setRD(){
        flags = (short)(flags | (1<<8));
    }
    public void setRCode() {
        packetBytes[3] ^= 0x04;
        rCode = 4;
    }
    public int getRCode(){
        return rCode;
    }
    public boolean byteIsPointer(){
        return ((packetBytes[offset] & 0xC0) == 0xC0);
    }
    public int readTTL(){
        return (((packetBytes[offset++] & 0xff) << 8) | (packetBytes[offset++] & 0xff) << 16) +
                (((packetBytes[offset++] & 0xff) << 8) | (packetBytes[offset++] & 0xff));


    }
    public int getTTL(){
        return this.packetTTL;
    }

}
