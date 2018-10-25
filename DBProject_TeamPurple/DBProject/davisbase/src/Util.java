
import java.util.*;
import static java.lang.System.*;

class Util {
  public static int log2(int exp){
    double logA = Math.log(exp);
    double logB = Math.log(2);
    int logC = (int)(logA/logB);
    return logC;
  }
  public static ArrayList<byte[]> clone(ArrayList<byte[]> a){
    ArrayList<byte[]> c = new ArrayList<byte[]>(a);
    return c;
  }
}
