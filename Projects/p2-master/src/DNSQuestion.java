/**
 * Created by Douglas on 2/17/2016.
 *
 */
public class DNSQuestion {

    String dnsName;
    short dnsType;
    short dnsClass;

    public DNSQuestion(){
        dnsName ="";
        dnsType = DNSPacket.TYPE_A;
        dnsClass = DNSPacket.CLASS_IN;
    }

    public DNSQuestion(String dnsName,short dnsType,short dnsClass){
        this.dnsName = dnsName;
        this.dnsType = dnsType;
        this.dnsClass = dnsClass;
    }

    public short getDnsType(){
        return dnsType;
    }

    public short getDnsClass(){
        return dnsClass;
    }

    public String getQName(){
        return dnsName;
    }

    public void setQName(String name){
        dnsName = name;
    }

    public void setDnsType(short type){
        dnsType =type;
    }

    public void setDnsClass(short clas){
        dnsClass = clas;
    }

}