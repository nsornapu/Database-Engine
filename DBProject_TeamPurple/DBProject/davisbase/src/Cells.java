
import java.util.*;
import static java.lang.System.*;

class Cells {

  public ArrayList<byte[]> albs;

  Cells(){
    this(null);
  }
  Cells(ArrayList<byte[]> albs){
    if(albs == null) this.albs = new ArrayList<byte[]>();
  }

  public String toString(){
    String result = "[";
    for(byte[] c : albs){
      String bytesString = Bytes.toString(c);
      result += bytesString;
    }
    result += "]";
    return result;
  }

  public void print(){
    String cellString = toString();
    Test.log("Cells Content: \n"+cellString);
  }

  public Cells clone(){
    ArrayList<byte[]> others = this.albs;
    ArrayList<byte[]> clones = new ArrayList<byte[]>(others);
    Cells cs = new Cells();
    cs.setAlbs(clones);
    return cs;
  }

  public void setAlbs(ArrayList<byte[]> albs){
    this.albs = albs;
  }

  public void add(byte[] bs){
    Test.log("Adding bytes as cell:" + Bytes.toString(bs));
    albs.add(bs);
  }

  public Cells subList(int startInclusive,int endExclusive){
    //subList returns a view, not a new datastructure...thanks Java!
    List<byte[]> partView = this.albs.subList(startInclusive,endExclusive);
    ArrayList<byte[]> clone = new ArrayList<byte[]>(partView);
    Cells cs = new Cells(clone);
    return cs;
  }

  public byte[] toBytes() throws Exception {
    ArrayList<byte[]> clone = Util.clone(this.albs);
    Collections.reverse(clone);
    return Bytes.fromArrayList(clone);

//  ArrayList<byte[]> albs = this.albs;
//
//  //Serialize the albs member to a single byte array for page storage
//  Integer cellsLength = 0;
//  for(int i = albs.size()-1; i>=0; i--){
//    byte[] b = albs.get(i);
//    Bytes.print("cell record: ",b);
//    cellsLength += b.length;
//  }
//  Test.log("cellsLength: "+cellsLength);
//  byte[] bytes = new byte[cellsLength];
//  //Integer index = cellsLength;
//  Integer index = 0;//cellsLength;
//  for(int i=albs.size()-1; i>=0; i--){
//    //Iterate forward, but keep an index moving backward since the stack grows from pagesize->'down'
//    byte[] b = albs.get(i);
//    Bytes.print("cell record 2: ",b);
//    Bytes.copy(b,bytes,index); 
//    index = index + b.length;
//  }
//  return bytes;
  }
  public ArrayList<Integer> toOffsets(int pageSize) throws Exception {
    //Offset data functionally depends on Cell data
    //So derive offset member from cells member
    ArrayList<Integer> offsets = new ArrayList<Integer>();
    Integer pointer = pageSize;
    for(Integer i=0; i<this.albs.size(); i++){
      //Iterate forward, but keep an index moving backward since the stack grows from pagesize->'down'
      byte[] cell = this.albs.get(i);
      pointer = pointer - cell.length;
      offsets.add(pointer);
    }
    return offsets;
  }

  //  ArrayList Deferral Methods
  //
  public void set(int index,byte[] cellBytes) throws Exception {
    this.albs.set(index,cellBytes);
  }
  public int size(){
    return this.albs.size();
  }
  public byte[] get(int index){
    return this.albs.get(index);
  }
  public void remove(int index){
    this.albs.remove(index);
  }
  public void addAll(Cells newCells){
    this.albs.addAll(newCells.albs);
  }

}
