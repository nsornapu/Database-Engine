import java.util.ArrayList;

public class Offsets {
  public ArrayList<Short> offset;
  public int numberOfOffsets;

  public ArrayList<Short> getOffset() {
    return offset;
  }
  public void setOffset(ArrayList<Short> offset) {
    this.offset = offset;
  }
  public int getNumberOfOffsets() {
    return numberOfOffsets;
  }
  public void setNumberOfOffsets(int numberOfOffsets) {
    this.numberOfOffsets = numberOfOffsets;
  }

  Offsets() {
    setOffset(new ArrayList<Short>());
  }

}




