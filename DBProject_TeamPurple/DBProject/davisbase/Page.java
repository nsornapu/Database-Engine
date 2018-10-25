import java.util.ArrayList;

public class Page {

  public int pageSize = 512;
  public Byte pageType = 0x0D;
  public Byte numberOfRecords;
  public int pageNumber = 0xFFFFFFFF;
  public Offsets offsets;
  public ArrayList<Payload> payloads;

  Page() {
    numberOfRecords = 0;
    offsets = new Offsets();
    payloads = new ArrayList<Payload>();
    pageSize = 512;
    pageType = 0x0D;
  }
  public Byte getPageType() {
    return pageType;
  }
  public void setPageType(Byte pageType) {
    this.pageType = pageType;
  }
  public Byte getNumberOfRecords() {
    return numberOfRecords;
  }
  public void setNumberOfRecords(Byte numberOfRecords) {
    this.numberOfRecords = numberOfRecords;
  }
  public int getPageNumber() {
    return pageNumber;
  }
  public void setPageNumber(int pageNumber) {
    this.pageNumber = pageNumber;
  }
  public Offsets getOffsets() {
    return offsets;
  }
  public void setOffsets(Offsets offsets) {
    this.offsets = offsets;
  }
  public ArrayList<Payload> getPayloads() {
    return payloads;
  }
  public void setPayloads(ArrayList<Payload> payloads) {
    this.payloads = payloads;
  }

}




