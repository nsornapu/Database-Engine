
import java.io.RandomAccessFile;
import java.io.File;
import java.io.FileReader;
import java.util.Scanner;
import java.util.SortedMap;
import java.lang.Math.*;
import static java.lang.System.out;

public class TedsTests {
  public static RAF raf = new RAF();

  public static void main(String[] args) throws Exception { 
    //try {
    Test.logLevel = 2;
  //SuppliedExampleTest();
    ByteTest1();
    ByteTest2();
    ByteTest3();
    ByteTest4();
    PageTest();
    PageResizeTest();
    AddCellTest();
    RemoveCellTest();
    mergeCellTest();
    pageMergeOverflowTest();
    pageOverflowTest();
//  pageUnderflowTest();
    pageAddCellTest();
    pageAddCellOverflowTest();
  //} catch (Exception e) { Test.log(e); } 
  }

  public static void pageUnderflowTest() throws Exception {
    throw new Exception("NEEDS IMPLEMENTATION");

//  Test.logLevel = 0;
//  p.print();
//  Test.logLevel = 2;
  }

  public static void pageAddCellOverflowTest() throws Exception {

    RAF r = new RAF();
    r.clear();
    r.setPageSize(32);

    Page p = new Page();
    p.setRaf(r);
    p.setDefaults();
    p.setPageType("TABLE_LEAF");

    boolean added;
    added = p.tryAddCell(new CellTableLeaf(new byte[]{0x01,0x02,0x03,0x04}));
    Test.equals(added,true);
    added = p.tryAddCell(new CellTableLeaf(new byte[]{0x05,0x06,0x07}));
    Test.equals(added,true);
    added = p.tryAddCell(new CellTableLeaf(new byte[]{0x08,0x09}));
    Test.equals(added,false);

  }

  public static void pageAddCellTest() throws Exception {

    RAF r = new RAF();
    r.clear();
    r.setPageSize(32);

    Page p = new Page();
    p.setRaf(r);
    p.setPageType("TABLE_LEAF");
    p.setDefaults();

    boolean added;
    added = p.tryAddCell(new CellTableLeaf(new byte[]{0x01,0x02,0x03,0x04}));
    Test.equals(added,true);
    added = p.tryAddCell(new CellTableLeaf(new byte[]{0x05,0x06,0x07}));
    Test.equals(added,true);

    String pageByteString = p.toString();
    Test.equals(pageByteString,
     "[0D,02,00,0D,FF,FF,FF,FF,00,16,00,0D,00,00,03,00,"+"\n"+
      "00,00,00,05,06,07,00,04,00,00,00,00,01,02,03,04]"
    );

  }

  public static void pageOverflowTest() throws Exception {

    RAF r = new RAF();
    r.clear();
    r.setPageSize(16);

    Page p = new Page();
    p.setRaf(r);
    p.setDefaults();
    boolean added;
    added = p.tryAddCellBytes(new byte[]{0x01,0x02,0x03,0x04});
    Test.equals(added,true);
    added = p.tryAddCellBytes(new byte[]{0x05,0x06,0x07});
    Test.equals(added,false);

  }
  public static void pageMergeOverflowTest() throws Exception {

    RAF r = new RAF();
    r.clear();
    r.setPageSize(32);

    Page pLeft = new Page();
    pLeft.setRaf(r);
    pLeft.setDefaults();
    pLeft.addCellBytes(new byte[]{0x01,0x02,0x03,0x04});
    pLeft.addCellBytes(new byte[]{0x05,0x06,0x07});
    pLeft.addCellBytes(new byte[]{0x08,0x09,0x0a,0x0b,0x0c});
//  pLeft.print();

    Page pRight = new Page();
    pRight.setRaf(r);
    pRight.setDefaults();
    pRight.addCellBytes(new byte[]{0x19});
    pRight.addCellBytes(new byte[]{0x18,0x17,0x16});
    pRight.addCellBytes(new byte[]{0x15,0x14});
//  pRight.print();

    Page pMerged = null;
    boolean thrown = false;
    try{
      pMerged = pLeft.mergeWith(pRight);
    } catch(Exception e){
      if(!e.toString().contains("not enough space in page"))throw e;
      thrown = true;
    }

    if(!thrown){
      String pageByteString = pMerged.toString();
      throw new Exception(
        "Overflow not caught!" +"\n"+
        "Resulting incorrect page: \n"+pageByteString
      );
    }


  }
  public static void mergeCellTest() throws Exception {
    RAF r = new RAF();
    r.clear();
    r.setPageSize(64);

    Page pLeft = new Page();
    pLeft.setRaf(r);
    pLeft.setDefaults();
    pLeft.addCellBytes(new byte[]{0x01,0x02,0x03,0x04});
    pLeft.addCellBytes(new byte[]{0x05,0x06,0x07});
    pLeft.addCellBytes(new byte[]{0x08,0x09,0x0a,0x0b,0x0c});
//  pLeft.print();

    Page pRight = new Page();
    pRight.setRaf(r);
    pRight.setDefaults();
    pRight.addCellBytes(new byte[]{0x19});
    pRight.addCellBytes(new byte[]{0x18,0x17,0x16});
    pRight.addCellBytes(new byte[]{0x15,0x14});
//  pRight.print();

    Page pMerged = pLeft.mergeWith(pRight);
//  pMerged.print();
//  pMerged.save();

    String pageByteString = pMerged.toString();
    Test.equals(pageByteString,
     "[0D,06,00,2E,FF,FF,FF,FF,00,3C,00,39,00,34,00,33,"+"\n"+
      "00,30,00,2E,00,00,00,00,00,00,00,00,00,00,00,00,"+"\n"+
      "00,00,00,00,00,00,00,00,00,00,00,00,00,00,15,14,"+"\n"+
      "18,17,16,19,08,09,0A,0B,0C,05,06,07,01,02,03,04]"
    );
  }

  public static void RemoveCellTest() throws Exception { 
    Page p = new Page();
    RAF r = new RAF();
    r.clear();
    p.setRaf(r);
    p.setPageSize(32);
    p.setDefaults();
    p.addCellBytes(new byte[]{0x01,0x02,0x03,0x04});
    p.addCellBytes(new byte[]{0x05,0x06,0x07});
    p.addCellBytes(new byte[]{0x08,0x09,0x0a,0x0b,0x0c});
    p.DeleteCellByIndex(1);
    //p.save();
    String pageByteString = p.toString();
    Test.equals(pageByteString,
     "[0D,02,00,17,FF,FF,FF,FF,00,1C,00,17,00,00,00,00,"+"\n"+
      "00,00,00,00,00,00,00,08,09,0A,0B,0C,01,02,03,04]"
    );
  }

  public static void AddCellTest() throws Exception {
    Page p = new Page();
    RAF r = new RAF();
    r.clear();
    p.setRaf(r);
    p.setPageSize(32);
    p.setDefaults();
    p.addCellBytes(new byte[]{0x01,0x02,0x03,0x04});
    p.addCellBytes(new byte[]{0x05,0x06,0x07});
  //p.save();
    String s = p.toString();
    Test.equals(s,
     "[0D,02,00,19,FF,FF,FF,FF,00,1C,00,19,00,00,00,00,"+"\n"+
      "00,00,00,00,00,00,00,00,00,05,06,07,01,02,03,04]"
    );
  }

  public static void PageResizeTest() throws Exception {
    Page p = new Page();
    RAF r = new RAF();
    r.clear();
    p.setRaf(r);
    p.setPageSize(32);
    p.setDefaults();
  //p.save();
    String s = p.toString();
    Test.equals(s,
      "[0D,00,00,20,FF,FF,FF,FF,00,00,00,00,00,00,00,00,"+"\n"+
      "00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00]"
    );
  }

  public static void PageTest() throws Exception {
    Page p = new Page();
    p.setRaf(new RAF());
    p.setDefaults();
  //p.save();
    byte[] b = p.bytes;
    String s = Bytes.toString(b);
    Test.equals(s,
     "[0D,00,02,00,FF,FF,FF,FF,00,00,00,00,00,00,00,00,"+"\n"+
      "00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,"+"\n"+
      "00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,"+"\n"+
      "00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,"+"\n"+
      "00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,"+"\n"+
      "00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,"+"\n"+
      "00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,"+"\n"+
      "00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,"+"\n"+
      "00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,"+"\n"+
      "00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,"+"\n"+
      "00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,"+"\n"+
      "00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,"+"\n"+
      "00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,"+"\n"+
      "00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,"+"\n"+
      "00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,"+"\n"+
      "00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,"+"\n"+
      "00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,"+"\n"+
      "00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,"+"\n"+
      "00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,"+"\n"+
      "00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,"+"\n"+
      "00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,"+"\n"+
      "00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,"+"\n"+
      "00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,"+"\n"+
      "00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,"+"\n"+
      "00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,"+"\n"+
      "00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,"+"\n"+
      "00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,"+"\n"+
      "00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,"+"\n"+
      "00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,"+"\n"+
      "00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,"+"\n"+
      "00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,"+"\n"+
      "00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00]"
    );
  }

  public static void ByteTest1() throws Exception {
    int test = (int)Math.pow(2,4);
    byte b = (byte)test;
    int i = Bytes.toInt(b);
    Test.equals(i,test);
  }

  public static void ByteTest2() throws Exception {
    int test = (int)Math.pow(2,4);
    byte[] bs = new byte[]{ (byte)test };
    int i = Bytes.toInt(bs);
    Test.equals(i,test);
  }

  public static void ByteTest3() throws Exception {
    byte[] bs = new byte[]{ 
        (byte)0x00
      , (byte)0x01
      , (byte)0x00
      , (byte)0x00
    };
    int i = Bytes.toInt(bs);
    int test = (int)Math.pow(2,16);
    Test.equals(i,test);
  }

  public static void ByteTest4() throws Exception {
    int i = (int)Math.pow(2,10);
    byte[] bs = Bytes.fromInt(i);
    String s = Bytes.toString(bs);
    Test.equals(s,"[00,00,04,00]");
  }

  public static void SuppliedExampleTest() throws Exception {
    RAF r = new RAF("some_user_table");
    /* This method will initialize the database storage if it doesn't exit */
    r.initializeDataStore();

    int pageSize = r.getPageSize();
    Test.log("The database table file page size is: " + pageSize);

    /* Increase the file size to be 1024, i.e. 2 x 512B */
    r.resizeToNumPages(2);
    r.printInfo();

    // Re-locate the address pointer at the beginning of page 1 and 
    r.seek(0);
    //write 0x05 (b-tree interior node) to the first header byte 
    r.write(0x05);

    /* Re-locate the address pointer at the beginning of page 2 and write 
     * 0x0D (b-tree leaf node) to the first header byte */
    r.seekPage(1);
    r.write(0x0D);

    /* Re-locate the address pointer at the beginning of page 3 and write 
     * 0x0D (b-tree leaf node) to the first header byte */
    r.seekPage(2);
    r.write(0x0D);

    /* Increase the size of the binaryFile by exactly one page, regardless of how
     * long it currently is. The new bytes will be appended to the end and be all zeros */
    r.addPage();
    r.printInfo();

    r.printHexDump();
    r.close();
  }

}






