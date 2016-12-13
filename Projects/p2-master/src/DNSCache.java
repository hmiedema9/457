import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.HashMap;

class DNSCache {
    HashMap<String,ArrayList<String>> completeDomainCache;
    HashMap<String,String[]> subDomainCache;

    public DNSCache(){
	    completeDomainCache = new HashMap<String,ArrayList<String>>();
        subDomainCache = new HashMap<String,String[]>();
    }
    public ArrayList<String> getIP(String domain){
	    return completeDomainCache.get(domain);
    }

    public void appendComplete(String domain, ArrayList<String> ip){
        //TODO NEED TO ADD TTL -- COMBINE IP ADDRESS AND TTL INTO CACHEELEMENT, USE THAT AS ARGUMENT HERE.
	    completeDomainCache.put(domain, ip);
    }	

    public ArrayList<String> getValue(String key){
        return completeDomainCache.get(key);
    }

    public boolean checkCompleteDomain(String s){
	    //TODO NEED TO ADD CHECK FOR TTL
	    return completeDomainCache.containsKey(s); //tells whether or not complete name is stored in cache
    }

    public boolean checkTopLevel(String s){
        return subDomainCache.containsKey(s);
    }
}
/* USE THIS WHEN ADDING TTL000000000000000000000000000000000000000000000000000000000000000000000
class CacheElement {
    public CacheElement(String domain)
} */