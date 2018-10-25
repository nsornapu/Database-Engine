
import java.util.*;
import java.util.Arrays;
import static java.lang.System.*;

abstract class Cell {
  public String toString(){
    String s = "";
    try{
      byte[] bs = this.toBytes();
      s = Bytes.toString(bs); 
    } catch(Exception e) {
      s = "Serialization failed for Cell: " + Test.getStackTrace();
    }
    return s;
  }
  public void print(){
    String cellString = toString();
    Test.log(cellString);
  }
  public abstract byte[] toBytes() throws Exception ;
  public abstract String getPageType();
}

class CellTableLeaf extends Cell {
  public static String pageType = "TABLE_LEAF";
  public int rowId;
  public byte[] payload;
  CellTableLeaf(byte[] payload){
    this.payload = payload;
  }
  public byte[] toBytes() throws Exception {
    ArrayList<byte[]> a = new ArrayList<byte[]>();
    byte[] payloadSize = Bytes.fromInt(payload.length,2);
    a.add(payloadSize);
    byte[] r = Bytes.fromInt(rowId,4);
    a.add(r);
    a.add(payload);
    byte[] bs = Bytes.fromArrayList(a);
    return bs;
  }
  public String getPageType(){ return pageType; }
}

  class CellTableInner extends Cell {
  public static String pageType = "TABLE_INNER";
  public int leftChildPageNumber;
  public int rowId;
  public byte[] toBytes() throws Exception {
    ArrayList<byte[]> a = new ArrayList<byte[]>();
    byte[] lcpn = Bytes.fromInt(leftChildPageNumber,4);
    a.add(lcpn);
    byte[] r = Bytes.fromInt(rowId,4);
    a.add(r);
    byte[] bs = Bytes.fromArrayList(a);
    return bs;
  }
  public String getPageType(){ return pageType; }
  }

  class CellIndexLeaf extends Cell {
  public String pageType = "INDEX_INNER";
  public byte[] payload;
  CellIndexLeaf(byte[] payload){
    this.payload = payload;
  }
  public byte[] toBytes() throws Exception {
    ArrayList<byte[]> a = new ArrayList<byte[]>();
    byte[] payloadSize = Bytes.fromInt(payload.length,2);
    a.add(payloadSize);
    a.add(payload);
    byte[] bs = Bytes.fromArrayList(a);
    return bs;
  }
  public String getPageType(){ return this.pageType; }
  }
  
  class CellIndexInner extends Cell {
  public String pageType = "INDEX_LEAF";
  public int leftChildPageNumber;
  public byte[] payload;
  CellIndexInner(byte[] payload){
    this.payload = payload;
  }
  public byte[] toBytes() throws Exception {
    ArrayList<byte[]> a = new ArrayList<byte[]>();
    byte[] lcpn = Bytes.fromInt(leftChildPageNumber,4);
    a.add(lcpn);
    byte[] payloadSize = Bytes.fromInt(payload.length,2);
    a.add(payloadSize);
    a.add(payload);
    byte[] bs = Bytes.fromArrayList(a);
    return bs;
  }
  public String getPageType(){ return this.pageType; }
  }
