import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class ReadWritePage {
  Page p;

  static int pageSize=512;
  public ReadWritePage() {

  }

  public void WritePage(RandomAccessFile r,ArrayList<Page> pa) throws IOException
  {
    int numberOfPages;
    numberOfPages  = pa.size();

    for(int iterator = 0; iterator<numberOfPages;iterator++)
    {
      Page p = pa.get(iterator);
      DataType dt= new DataType();

        if(r.length()<pageSize-1)
        {
          r.setLength(pageSize);
        }
        r.seek(pageSize*(iterator));
        r.write(0x0d);
        r.write(p.numberOfRecords);
        if(p.offsets.offset.size()==0)
        {
          r.writeShort(512);
        }
        else {
        r.writeShort(p.offsets.offset.get(p.offsets.offset.size()-1));
        }

        r.writeInt(0xFFFFFFFF);
        for(int i =0; i<p.offsets.offset.size();i++)
        {
          r.writeShort(p.offsets.offset.get(i));
        }
        for(int i = 0; i<p.offsets.offset.size();i++)
        {
          r.seek(p.offsets.offset.get(i));
          r.writeShort(p.payloads.get(i).header);
          r.writeInt(p.payloads.get(i).rowId);
          r.writeByte(p.payloads.get(i).numberOfRecords);
          for(int k=0;k<p.payloads.get(i).numberOfRecords;k++)
          {
            r.writeByte(p.payloads.get(i).dataType.get(k));
          }
          for(int k=0;k<p.payloads.get(i).numberOfRecords;k++)
          {
            if(dt.dataType.containsValue(p.payloads.get(i).dataType.get(k)))
            {
              if(p.payloads.get(i).dataType.get(k).equals(6))
              {
                r.writeInt(Integer.parseInt(p.payloads.get(i).data.get(k)));
              }
              if(p.payloads.get(i).dataType.get(k)==0x04)
              {
                r.writeByte(Integer.parseInt(p.payloads.get(i).data.get(k)));
              }
              if(p.payloads.get(i).dataType.get(k)==0x05)
              {
                r.writeShort(Short.parseShort(p.payloads.get(i).data.get(k)));
              }
              if(p.payloads.get(i).dataType.get(k)==0x07)
              {
                r.writeLong(Long.parseLong(p.payloads.get(i).data.get(k)));
              }
              if(p.payloads.get(i).dataType.get(k)==0x08)
              {
                r.writeFloat(Float.parseFloat(p.payloads.get(i).data.get(k)));
              }
              if(p.payloads.get(i).dataType.get(k)==0x09)
              {
                r.writeDouble(Double.parseDouble(p.payloads.get(i).data.get(k)));
              }
              if(p.payloads.get(i).dataType.get(k)==0x0A)
              {
                p.payloads.get(i).data.set(k,p.payloads.get(i).data.get(k).substring(0, 7));
                r.writeBytes(p.payloads.get(i).data.get(k));
              }
              if(p.payloads.get(i).dataType.get(k)==0x0B)
              {
                p.payloads.get(i).data.set(k,p.payloads.get(i).data.get(k).substring(0, 7));
                r.writeBytes(p.payloads.get(i).data.get(k));
              }
            }
            else {
              r.writeBytes(p.payloads.get(i).data.get(k));  
            }
          }
        }

    }
  }


  public ArrayList<Page> ReadTable(RandomAccessFile r){
    try {
      ArrayList<Page> pages = new ArrayList<Page>();
      int numberOfPages = (int) (r.length()/pageSize);
      if (numberOfPages == 0)
      {
        return pages; 
      }
      else {
        for(int it=0;it<numberOfPages;it++)
        {
          int pageLocation = pageSize * it;
          Page p = new Page();
          Offsets o = new Offsets();
          ArrayList<Payload> payloads = new ArrayList<Payload>();
          r.seek(pageLocation+1);
          p.numberOfRecords =r.readByte();
          r.seek(pageLocation+8);

          //taking care of the offsets for that particular page
          for(int i = 0 ; i < p.numberOfRecords ; i++)
          {
            o.offset.add(r.readShort());
          }
          o.numberOfOffsets = o.offset.size();

          //taking care of all payloads
          for(int i = 0 ; i < o.numberOfOffsets ; i++)
          {
            Payload payload = new Payload();
            ArrayList<Integer> dataType = new ArrayList<Integer>();
            ArrayList<String> data = new ArrayList<String>();
            r.seek(o.offset.get(i));
            int payloadLength = r.readShort();
            int rowId = r.readInt();
            int numberOfColumns = r.readByte();

            payload.header = payloadLength;
            payload.numberOfRecords = numberOfColumns;
            payload.rowId= rowId;
            for(int j = 0 ; j < numberOfColumns ; j++)
            {
              int rr = (int)r.readByte();
              dataType.add(rr);
            }
            for(int j = 0; j < numberOfColumns ; j++)
            {
              int dt = dataType.get(j);
              if(dt==0x00) {
                data.add("");
              }
              else if(dt==0x01) {
                data.add("");
              }
              else if(dt==0x02) {
                data.add("");
              }
              else if(dt==0x03) {
                data.add("");
              }
              else if(dt==0x04) {
                Byte b = r.readByte();
                data.add(b.toString());
              }
              else if(dt==0x05) {
                Short s = r.readShort();
                data.add(s.toString());
              }
              else if(dt==0x06) {
                Integer integer = r.readInt();
                data.add(integer.toString());
              }
              else if(dt==0x07) {
                Long l = r.readLong();
                data.add(l.toString());
              }
              else if(dt==0x08) {
                Float f = r.readFloat();
                data.add(f.toString());
              }
              else if(dt==0x09) {
                Double d = r.readDouble();
                data.add(d.toString());
              }
              else if(dt==0x0A) {
                StringBuilder content_creator = new StringBuilder();
                String content = new String();
                for(int k = 0;k<8;k++)
                {
                  Character c = (char) r.readByte();
                  content_creator.append(c);
                }
                content = content_creator.toString();
                data.add(content);
              }
              else if(dt==0x0B) {
                StringBuilder content_creator = new StringBuilder();
                String content = new String();
                for(int k = 0;k<8;k++)
                {
                  Character c = (char) r.readByte();
                  content_creator.append(c);
                }
                content = content_creator.toString();
                data.add(content);
              }
              else {
                dt=dt-0x0C;
                StringBuilder content_creator = new StringBuilder();
                String content = new String();
                for(int k = 0;k<dt;k++)
                {
                  Character c = (char) r.readByte();
                  content_creator.append(c);
                }
                content = content_creator.toString();
                data.add(content);
              }
            }
            payload.data = data;
            payload.dataType = dataType;
            payloads.add(payload);
          }
          p.setOffsets(o);
          p.setPayloads(payloads);
          pages.add(p);
        }
        return pages;
      }

    } catch (IOException e) {
      ArrayList<Page> pages = new ArrayList<Page>();
      e.printStackTrace();
      return pages;
    }

  }

}




