
import java.util.*;
import java.util.Arrays;
import static java.lang.System.*;

public class Page {
  //I'm going to keep internal java-primitive representations of the various page fields 
  //(such as a Short for the cell-region-begin offset)
  //so that I don't have to re-implement add/subtract for multibyte arrays

  public Integer pageNumber;
  public byte[] bytes;
  public RAF raf;

//  public String pageType;
//  public Integer numCells;
//  public Integer startOfContent;
//  public Integer rightPointer;
//  public ArrayList<Integer> offsets;
//  public Cells cells;

  public void setPageNumber(Integer n){ pageNumber = n;}
  public int getPageNumber(){ return pageNumber;}
  public void setPageSize(Integer n) throws Exception { 
    //If setting page-size for one page, 
    //then need to set page-size for all pages in the same file
    //this is why pageSize is a field of RAF and not PAGE
    //and why accessing page-size outside RAF requires the use
    //of the RAF getters-setters
    if(raf == null) throw new Exception("Pages RAF not attached yet");
    raf.setPageSize(n); 
    bytes = new byte[n];
  }
  public Integer getPageSize() throws Exception { 
    if(raf == null) throw new Exception("Pages RAF not attached yet");
    return raf.getPageSize(); 
  }

  public void setDefaults() throws Exception {

    //On-Disk representation
    if(raf == null) throw new Exception("RAF (and by extension pageSize) need to be set before allowing default values to be set. Otherwise they might be overwritten");

    //In-Memory representation
    Integer pageSize = getPageSize();
    if(pageSize == null) setPageNumber(512);
    pageSize = getPageSize();
    this.bytes = new byte[pageSize];

    if(pageNumber == null) setPageNumber(0);
    String pageType = getPageType();
    if(pageType == null) setPageType("TABLE_LEAF");
    Integer numCells = getNumCells();
    if(numCells == null) setNumCells(0);

    //Page Offset - Not Address/File-Offset
    Integer startOfContent = getStartOfContent();
    if(startOfContent == 0) setStartOfContent(pageSize);
    Integer rightPointer = getRightPointer();
    if(rightPointer == 0) setRightPointer(0xFFFFFFFF);

    //Should be empty - all 0s
    //offsets = new ArrayList<Integer>();
    //cells = new Cells();
  }

  public boolean tryAddCell(Cell cell) throws Exception{
    String cpt = cell.getPageType();
    String pt = this.getPageType();
    if( pt != cpt) throw new Exception( "Cannot add a "+cpt+" cell to a "+pt+" page");
    
    return tryAddCellBytes(cell.toBytes());
  }
  public boolean tryAddCellBytes(byte[] cell) throws Exception{
    boolean added = true;
    try{
      this.addCellBytes(cell);
    } catch(Exception e){
      //Only want to catch the out of space exception, no other exception
      if(!e.toString().contains("not enough space in page"))throw e;
      added = false;
    }
    return added;
  }
  public void addCellBytes(byte[] cell) throws Exception{
    //Create - C
    Cells cells = getCells();
    Test.log("Retrieved Cells: "+cells.toString());
    cells.add(cell);
    setCells(cells);
  }
  public Cells getCells() throws Exception{
    //Read - R
    Cells cells = new Cells();
    ArrayList<Integer> offsets = GetOffsets();
    Test.log("Page.getCells: getting offsets: "+offsets.toString());
    if(offsets.size() == 0) return cells;
    int pageSize = getPageSize();
    Integer previousOffset = pageSize;
    for(Integer i=0; i<offsets.size(); i++){
      Integer offset = offsets.get(i);
      Test.log("offset: "+i+":"+offset+"->"+previousOffset);
      byte[] cellBytes = Arrays.copyOfRange(bytes,offset,previousOffset); 
      Bytes.print("Getting cell bytes",cellBytes);
      cells.add(cellBytes);
      previousOffset = offset;
    }
    Test.log("returning cells: ");
    cells.print();
    return cells;
  }
  public void setCells(Cells cells) throws Exception{
    //Validate the instance members with respect to one another
    //and persist each in their respective locations if valid
    Test.log("Setting These Cells: "); 
    cells.print();
    byte[] cellBytes = cells.toBytes();
    Test.log("Setting these cell bytes:");
    Bytes.print(cellBytes);
    Integer cellsLength = cellBytes.length;
    Test.log("setCells: cellsLength: "+cellsLength);
    int pageSize = getPageSize();
    Integer destination = pageSize-cellsLength;
    Test.log("destination: "+Bytes.toString(Bytes.fromInt(destination,2)));
    
    ArrayList<Integer> offsets = cells.toOffsets(pageSize);
    Integer headerEnd = getBaseHeaderSize() + 2*offsets.size();
    Test.log("setCells: headerEnd: "+headerEnd);
    if (destination < headerEnd ) {
      throw new Exception(
        "\n" + 
        "Page.setCells: \n" + 
        "not enough space in page to add cell bytes \n" +
        "when setting cells: \n" + 
        cells.toString() + " \n" +
        "on page: \n" + 
        this.toString()
      );
    }

    //Copy cell bytes to buffer
    Bytes.clear(bytes,headerEnd,pageSize);
    Bytes.copy(cellBytes,bytes,destination); 

    Integer numCells = cells.size();
    setNumCells(numCells);

    Test.log("Getting offsets for these cells: "); 
    cells.print();
    Bytes.clear(bytes,8,destination);
    SetOffsets(offsets);

    Integer size = offsets.size();
    Integer soc = offsets.get(size-1);
    setStartOfContent(soc);
  }
  public void updateCellByOffset(Integer cellOffset,byte[] cell) throws Exception{
    //Update - U
    ArrayList<Integer> offsets = GetOffsets();
    Cells cells = getCells();

    Integer index = offsets.indexOf(cellOffset);
    if(index < 0) throw new Exception("Tried to update non-existent record/cell at " + Integer.toString(cellOffset));
    cells.set(index,cell);

    setCells(cells);
  }
  public void deleteCellByOffset(Integer cellOffset) throws Exception{
    //Delete - D

    ArrayList<Integer> offsets = GetOffsets();
    Cells cells = getCells();

    Integer index = offsets.indexOf(cellOffset);
    if(index < 0) throw new Exception("Tried to remove non-existent record/cell at " + Integer.toString(cellOffset));
    cells.remove(index);

    setCells(cells);
  }
  public void DeleteCellByIndex(Integer index) throws Exception{
    if(index < 0) throw new Exception("Tried to remove non-existent record/cell at index: " + index);
    Cells cells = getCells();
    Test.log("cells before deletion: ");
    cells.print();
    cells.remove((int)index);
    Test.log("cells after deletion: ");
    cells.print();
    setCells(cells);
  }



  //GETTERS AND SETTERS

  //  PAGE-TYPE : BYTES[0]
  //
  public String getPageType(){
    Integer pageTypeInt = (Integer)(0x000000FF & bytes[0]);
    String pt = PageType.getKeyFromValue(pageTypeInt);
    return pt;
  }
  public void setPageType(String s) throws Exception {
    int num = getNumCells();
    if(num !=0) throw new Exception("Cannot change page type after adding cells of a given page-type");
    Integer pt = PageType.get(s);
    bytes[0] = Bytes.fromInt(pt,1)[0];
  }

  //  CELL-COUNT : BYTES[1]
  //
  public Integer getNumCells() throws Exception {
    if(bytes == null) throw new Exception("Page bytes ref is null, need to initialize first");
    byte numCellData = bytes[1];
    Integer numCells = 0x000000FF & numCellData;
    return numCells;
  }
  public void setNumCells(Integer newNum) throws Exception {
    byte[] n = Bytes.fromInt(newNum,1);
    bytes[1] = n[0];
  }

  //  START-OF-CONTENT-POINTER : BYTES[2-3]
  //
  public Integer getStartOfContent() throws Exception{
    byte[] cellsBeginData = Arrays.copyOfRange(bytes,2,4); 
    Integer b = Bytes.toInt(cellsBeginData);
    return b;
  }
  public void setStartOfContent(Integer b) throws Exception {
    //startOfContent = b;
    byte[] numCells_B = Bytes.fromInt(b,2);
    Bytes.copy(numCells_B,bytes,2);
  }

  //  RIGHT-REFERENCE : BYTES[4-8]
  //
  public Integer getRightPointer() throws Exception{
    //Needed by B-Tree Implementation 
    byte[] rightData = Arrays.copyOfRange(bytes,4,8); 
    Integer r = Bytes.toInt(rightData);
    return r;
  }
  public void setRightPointer(Integer r) throws Exception {
    //rightPointer = r;
    //Needed by B-Tree Implementation 
    byte[] right_B = Bytes.fromInt(r);
    Bytes.copy(right_B,bytes,4);
  }

  //  OFFSETS-ARRAY : BYTES[8-N]
  //
  public ArrayList<Integer> GetOffsets() throws Exception {
    //Retrieve instance member from persistance byte array
    Integer numCells = getNumCells();
    Integer offsetArrayLength = numCells * 2;
    int base = getBaseHeaderSize(); 
    byte[] offsetBytes = Arrays.copyOfRange(bytes,base,base+offsetArrayLength); 
    Bytes.print("offsetBytes: ",offsetBytes);

    ArrayList<Integer> offsets = new ArrayList<Integer>();
    for(Integer i = 0; i<offsetBytes.length; i=i+2){
      byte highByte = offsetBytes[i];
      byte lowByte  = offsetBytes[i+1];
      byte[] offsetPair = new byte[]{highByte,lowByte};
      Bytes.print("offsetPair: ",offsetPair);
      Integer offset = Bytes.toInt(offsetPair);
      offsets.add(offset);
    }
    return offsets;
  }
  public void SetOffsets(ArrayList<Integer> offsets) throws Exception {
    //Serialize and Persist Offset data
    ArrayList<byte[]> a = new ArrayList<byte[]>();
    for(Integer i: offsets){
      byte[] offsetBytePair = Bytes.fromInt(i,2);
      a.add(offsetBytePair);
    }
    byte[] b = Bytes.fromArrayList(a);
    Bytes.print("setting these offsets: ",b);
    Integer baseHeaderSize = getBaseHeaderSize();
    Bytes.copy(b,this.bytes,(baseHeaderSize-1)+1);

//  Integer offsetBytesLength = offsets.size() * 2;
//  byte[] offsetBytes = new byte[offsetBytesLength];
//  Integer position = getBaseHeaderSize();
//  for(Integer i: offsets){
//    byte[] offsetBytePair = Bytes.fromInt(i,2);
//    Bytes.print("offsetBYtePair: ",offsetBytePair);
//    Bytes.copy(offsetBytePair,this.bytes,position);
//    position += 2;
//  }
  }

  //Utility methods
  public Integer getBaseHeaderSize() throws Exception {
    Integer baseHeaderSize = 1 + 1 + 2 + 4;
    return baseHeaderSize;
  }
  public Integer getHeaderSize() throws Exception {
    Integer numCells = getNumCells();
    Test.log("getHeaderSize: "+numCells);
    Integer baseHeaderSize = getBaseHeaderSize();
    Integer headerSize = baseHeaderSize + numCells*2;
    return headerSize;
  }
  public void save() throws Exception { 
    raf.seekPage(this.pageNumber);
    raf.writeBytesToPage(this.bytes,this.pageNumber); 
  }
  public Integer address() throws Exception { 
    int pageSize = getPageSize();
    return pageNumber*pageSize; 
  }
  public Integer rafAddressToPageOffset(Integer address) throws Exception {
    int pageSize = getPageSize();
    Integer offset = address - (pageSize*pageNumber);
    return offset;
  }
  public void DeleteCellByAddress(Integer cellAddress) throws Exception{
    Integer cellOffset = rafAddressToPageOffset(cellAddress);
    DeleteCellByOffset(cellOffset);
  }
  public void DeleteCellByOffset(Integer cellOffset) throws Exception{
    throw new Exception("Not implemented");
  }
  public String toString(){
    String s = Bytes.toString(bytes);
    return s;
  }
  public void print(){
    String s = toString();
    Test.log("Page Contents:\n" + s);
  }
  public Page mergeWith(Page p) throws Exception {
    Cells newCells = p.getCells();
    Cells cells = this.getCells();
    cells.addAll(newCells);
    this.setCells(cells);
    return this;
  }
  public PageList splitLeftOf(int i) throws Exception {
    Cells cells = this.getCells();
    Cells cellsLeft = cells.subList(0,i);
    Cells cellsRight = cells.subList(i,cells.size());

    PageList pl = new PageList();

    Page pLeft = new Page();
    pLeft.setDefaults();
    pLeft.setCells(cellsLeft);
    pl.add(pLeft);

    Page pRight = new Page();
    pRight.setDefaults();
    pRight.setCells(cellsRight);
    pl.add(pRight);

    return pl;
  }
  public void setRaf(RAF r) throws Exception {
    if(raf != null) throw new Exception("RAF already set for page, make sure are not overwriting defaults. Set RAF before setting defaults.");
    raf = r;
    //Need to refresh the local buffer size when basing off of raf-page-size
    setPageSize(r.getPageSize());
  }
}


////Probably won't need this or any other similar sub class
//class InnerTablePage extends Page {
//public PageType pageType = PageType.TABLE_INNER;
//public InnerTablePage(){
//  super();
//}
//}
