import java.io.RandomAccessFile;
import java.io.File;
import java.io.FileReader;
import java.util.Scanner;
import java.util.SortedMap;
import java.lang.Math.*;
import static java.lang.System.out;


public class RAF {

  /* the page size is a power of 2. */
  int pageSizePower = 9;

  RandomAccessFile raf;
  String path;
  String fileName;

  boolean hasFileBeenWrittenTo = false;

  public RAF() {
    this(null);
  }
  public RAF(String fileName) {
    if(fileName == null){ fileName = "some_user_table.tbl";}
    if(path == null){ path = "./data/user_data/";}
    try {
      String fullFileName = path + fileName;
      raf = new RandomAccessFile(fullFileName, "rw");
      //Initially the file is one page in length 
      int pageSize = getPageSize();
      raf.setLength(pageSize);
      // Set file pointer to the beginnning of the file
      raf.seek(0);       
    } catch (Exception e) {
      Test.log("RAF: " + e.toString());
    }
  }
  public void setFileName(String f){
    fileName = f;
  }
  public void setPath(String p){
    path = p;
  }

  public void seek(int b){ try {
      raf.seek(b);
  } catch (Exception e) { Test.log(e); } }

  public void seekPage(int pNum){
    int pageSize = getPageSize();
    seek(pNum*pageSize);
  }

  public void resizeToNumPages(int pNum){
    int l = length();
    setLength( l * pNum);
  }

  public void addPage(){
    int l = length();
    int pageSize = getPageSize();
    setLength( l + pageSize);
  }

  public void setLength(int i){
    try {
      raf.setLength(i);
    } catch (Exception e) { Test.log(e); }
  }

  public void close(){ try {
      raf.close();
  } catch (Exception e) { Test.log(e); } }


  public int length(){
    int l = 0;
    try { // .length method throws apparently
      l = (int)raf.length();
    } catch (Exception e) { Test.log(e); }
    return l;
  }

  public int GetNumPages(){
    try {
      int l = (int) raf.length();
      int pageSize = getPageSize();
      return (l / pageSize);
    } catch (Exception e) {
      Test.log("GetNumPages: " + e.toString());
      return -1;
    }
  }

  //  WRITE
  //
  public void write(int hex){
    try {
      raf.write(hex);
    } catch (Exception e) { Test.log(e); }
    hasFileBeenWrittenTo = true;
  }

  public void writeBytesToPage(byte[] bytes,int pageNumber){ try {
    int pageSize = getPageSize();
    int offset = pageNumber*pageSize;
    int length = bytes.length;
    raf.write(bytes,offset,length);
    hasFileBeenWrittenTo = true;
  } catch (Exception e) { Test.log(e); } }

  //  Data Directory Methods
  //
  public void cleanDataStore(){ try {
      File dataDir = new File("data");
      String[] oldFiles = dataDir.list();
      for (int i=0; i<oldFiles.length; i++) {
        File anOldFile = new File(dataDir, oldFiles[i]); 
        anOldFile.delete();
      }
  } catch (Exception e) { Test.log(e); } }

  public static void initializeDataStore() {
    try {
      File dataDir = new File("data");
      dataDir.mkdir();
    } catch (Exception e) {
      Test.log("initializeDataStore");
      Test.log(e);
    }
  }

  public void setPageSizePower(int ps){
    pageSizePower = ps;
  }
  public void setPageSize(int newPageSize) throws Exception{
    clear();
    double logA = Math.log(newPageSize);
    double logB = Math.log(2);
    int logC = (int)(logA/logB);
    int ceil = (int)Math.ceil(logC);
    pageSizePower = ceil;
  }
  public int getPageSize() {
    int pageSize = (int)Math.pow(2, pageSizePower);
    return pageSize;
  }

  public void printInfo(){
    int pageSize = getPageSize();
    Test.log("The file is now " + length() + " bytes long");
    Test.log();
  }

  public void printHexDump(){
    int pageSize = getPageSize();
    HexDump.displayBinaryHex(raf, pageSize);
  }

  //Delete the contents of the file
  public void clear() throws Exception {
    clear(false);
  }
  public void clear(boolean force) throws Exception {
    if(hasFileBeenWrittenTo && !force) throw new Exception("Clearing Non Empty File !!! \n Use force=true to override");
    int l = length();
    int max = 1000000;
    int i=0;
    raf.seek(0);
    int fp = (int)raf.getFilePointer();
    while(fp < l && i < max){
      raf.write(0);
      fp = (int)raf.getFilePointer();
      i++;
    }
  }

//  public static void initializeTablesFile() {
//    try {
//      RAF tables = new RAF("davisbase_tables");
//      /* Write 0x0D to the page header to indicate that it's a leaf page. The file pointer will automatically increment to the next byte. */
//      tables.write(0x0D);
//      /* Write 0x00 (although its value is already 0x00) to indicate there are no cells on this page */
//      tables.write(0x00);
//      tables.close();
//    } catch (Exception e) {
//      Test.log("initializeTablesFile");
//      Test.log(e);
//    }
//  }

//  public static void initializeColumnsFile() {
//    try {
//      RAF cols = new RAF("davisbase_columns");
//      // 0x0D = leaf page. The file 
//      // pointer will automatically increment to next byte
//      cols.write(0x0D);
//      // 0x00 = there are no cells on this page 
//      // (although its value is already 0x00)
//      cols.write(0x00); 
//      cols.close();
//    } catch (Exception e) {
//      Test.log("initializeColumnsFile");
//      Test.log(e);
//    }
//  }

}








