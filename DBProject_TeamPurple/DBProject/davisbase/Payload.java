import java.util.ArrayList;

public class Payload {

  public ArrayList<Integer> getDataType() {
    return dataType;
  }
  public void setDataType(ArrayList<Integer> dataType) {
    this.dataType = dataType;
  }
  public ArrayList<String> getData() {
    return data;
  }
  public void setData(ArrayList<String> data) {
    this.data = data;
  }

  public ArrayList<Integer> dataType;
  public ArrayList<String> data;
  public int numberOfRecords;
  public int rowId;
  public int header;

  Payload() {
    numberOfRecords =0;
    rowId=0;
    header=0;
    dataType = new ArrayList<Integer>();
    data = new ArrayList<String>();
  }
}




