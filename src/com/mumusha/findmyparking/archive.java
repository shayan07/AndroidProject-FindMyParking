package com.mumusha.findmyparking;

public class archive {

    int _id;
    String _la;
    String _lo;
    String _name;
    String _addr; 
    public archive(){}
    
    public archive(int id, String la, String lo,String name,String addr){
        this._id = id;
        this._la = la;
        this._lo = lo;
        this._name = name;
        this._addr=addr;
        
    }
    
    public archive( String la, String lo,String name,String addr){
        this._la = la;
        this._lo = lo;
        this._name = name;
        this._addr=addr;
    }
    public int getID(){
        return this._id;
    }
    
    public void setID(int id){
        this._id = id;
    }
    
    // getting name
    public String getLa(){
        return this._la;
    }
     
    // setting name
    public void setLa(String la){
        this._la = la;
    }
    
    // getting name
    public String getLo(){
        return this._lo;
    }
     
    // setting name
    public void setLo(String lo){
        this._lo = lo;
    }
    
    // getting name
    public String getName(){
        return this._name;
    }
     
    // setting name
    public void setName(String name){
        this._name = name;
    }
    // getting name
    public String getAddr(){
        return this._addr;
    }
     
    // setting name
    public void setAddr(String addr){
        this._addr = addr;
    }
    
}
