
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class DavisBaseColumn {

  public void createDavisBaseColumn() throws IOException {

    ArrayList<Page> pages = new ArrayList<Page>();
    Page p = new Page();
    Offsets o = new Offsets();
    ArrayList<Payload> pls = new ArrayList<Payload>();
    DataType dt = new DataType();

    for(int i=0;i<8;i++) {
      pls.add(new Payload());
      pls.get(i).dataType.add(dt.dataType.get("TEXT"));
      pls.get(i).dataType.add(dt.dataType.get("TEXT"));
      pls.get(i).dataType.add(dt.dataType.get("TEXT"));
      pls.get(i).dataType.add(dt.dataType.get("INT"));
      pls.get(i).dataType.add(dt.dataType.get("TEXT"));
      pls.get(i).numberOfRecords = pls.get(0).dataType.size();
    }

    pls.get(0).data.add("davisbase_tables");
    pls.get(0).data.add("rowid");
    pls.get(0).data.add("INT");
    pls.get(0).data.add("1");
    pls.get(0).data.add("NO");

    pls.get(1).data.add("davisbase_tables");
    pls.get(1).data.add("tableName");
    pls.get(1).data.add("TEXT");
    pls.get(1).data.add("2");
    pls.get(1).data.add("NO");

    pls.get(2).data.add("davisbase_columns");
    pls.get(2).data.add("rowid");
    pls.get(2).data.add("INT");
    pls.get(2).data.add("1");
    pls.get(2).data.add("NO");

    pls.get(3).data.add("davisbase_columns");
    pls.get(3).data.add("table_name");
    pls.get(3).data.add("TEXT");
    pls.get(3).data.add("2");
    pls.get(3).data.add("NO");

    pls.get(4).data.add("davisbase_columns");
    pls.get(4).data.add("column_name");
    pls.get(4).data.add("TEXT");
    pls.get(4).data.add("3");
    pls.get(4).data.add("NO");

    pls.get(5).data.add("davisbase_columns");
    pls.get(5).data.add("data_type");
    pls.get(5).data.add("TEXT");
    pls.get(5).data.add("4");
    pls.get(5).data.add("NO");

    pls.get(6).data.add("davisbase_columns");
    pls.get(6).data.add("ordinal_position");
    pls.get(6).data.add("INT");
    pls.get(6).data.add("5");
    pls.get(6).data.add("NO");

    pls.get(7).data.add("davisbase_columns");
    pls.get(7).data.add("is_nullable");
    pls.get(7).data.add("TEXT");
    pls.get(7).data.add("6");
    pls.get(7).data.add("NO");

    for(int i =0; i< pls.size();i++) {
      pls.get(i).rowId = i;
      for(int j=0;j<pls.get(i).dataType.size();j++) {
        if(pls.get(i).dataType.get(j).equals(0x0C)) {       
          int initialSize = pls.get(i).dataType.get(j);
          int textsize = pls.get(i).data.get(j).length();
          initialSize+=textsize;
          pls.get(i).dataType.set(j, initialSize);
          pls.get(i).header+=textsize+1;
        } else {
          pls.get(i).header+=dt.dataTypeWidth.get(pls.get(i).dataType.get(j))+1;
        }
      }
    }

    p.setPayloads(pls);
    p.numberOfRecords= (byte) pls.size();
    int pageSize = 512;
    for(int i =0; i<pls.size(); i++) {
      int payloadSize = pls.get(i).header + 4 + 3;
      o.offset.add((short) (pageSize - payloadSize));
      pageSize=pageSize-(payloadSize);
    }
    o.setNumberOfOffsets(o.offset.size());
    p.setOffsets(o);
    pages.add(p);
    RandomAccessFile r = new RandomAccessFile("data/davisbase_columns.tbl","rw");
    ReadWritePage w = new ReadWritePage();
    w.WritePage(r, pages);
  }

  public void createDavisBaseTable() throws IOException {
    ArrayList<Page> pages = new ArrayList<Page>();
    Page p = new Page();
    Offsets o = new Offsets();
    ArrayList<Payload> pls = new ArrayList<Payload>();
    DataType dt = new DataType();

    for(int i=0;i<2;i++) {
      pls.add(new Payload());
      pls.get(i).dataType.add(dt.dataType.get("TEXT"));
      pls.get(i).numberOfRecords = pls.get(0).dataType.size();
    }

    pls.get(0).data.add("davisbase_tables");
    pls.get(1).data.add("davisbase_columns");

    for(int i =0; i< pls.size();i++) {
      pls.get(i).rowId = i;
      for(int j=0;j<pls.get(i).dataType.size();j++) {
        if(pls.get(i).dataType.get(j).equals(0x0C)) {       
          int initialSize = pls.get(i).dataType.get(j);
          int textsize = pls.get(i).data.get(j).length();
          initialSize+=textsize;
          pls.get(i).dataType.set(j, initialSize);
          pls.get(i).header+=textsize+1;
        } else {
          pls.get(i).header+=dt.dataTypeWidth.get(pls.get(i).dataType.get(j))+1;
        }
      }
    }

    p.setPayloads(pls);
    p.numberOfRecords= (byte) pls.size();
    int pageSize = 512;
    for(int i =0; i<pls.size(); i++) {
      int payloadSize = pls.get(i).header + 4 + 3;
      o.offset.add((short) (pageSize - payloadSize));
      pageSize=pageSize-(payloadSize);
    }
    o.setNumberOfOffsets(o.offset.size());
    p.setOffsets(o);
    pages.add(p);
    RandomAccessFile r = new RandomAccessFile("data/davisbase_tables.tbl","rw");
    ReadWritePage w = new ReadWritePage();
    w.WritePage(r, pages);
  }
}








