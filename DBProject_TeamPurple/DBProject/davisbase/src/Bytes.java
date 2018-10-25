
import static java.lang.System.*;
import java.util.*;

class Bytes {

  public static byte[] fromInt(Integer n) throws Exception{
    return fromInt(n,4);
  }

  public static byte[] fromInt(Integer n,Integer width) throws Exception{
    byte[] b = new byte[width];
    if( width == 1 ){
      b[0] = (byte)(n >>> (0 * 8));
    } else if( width == 2 ){
      b[0] = (byte)(n >>> (1 * 8));
      b[1] = (byte)(n >>> (0 * 8));
    } else if ( width == 4 ) {
      b[0] = (byte)(n >>> (3 * 8));
      b[1] = (byte)(n >>> (2 * 8));
      b[2] = (byte)(n >>> (1 * 8));
      b[3] = (byte)(n >>> (0 * 8));
    } else {
      throw new Exception("Bytes.fromInt: invalid conversion width");
    }
    return b;
  }

  public static Integer toInt(byte[] b) throws Exception {
    //Make sure all integers are ZERO extended, not sign-extended as is the default in java
    if( b.length == 1 ){
      return (
        ((b[0] & 0x000000FF) << 0 )
      );
    } else if(b.length == 2){
      return (
        ((b[0] & 0x000000FF) << 8 )
      | ((b[1] & 0x000000FF)      )
      );
    } else if (b.length == 4 ) {
      return (
        ((b[0] & 0x000000FF) << 24 )
      | ((b[1] & 0x000000FF) << 16 )
      | ((b[2] & 0x000000FF) << 8  )
      | ((b[3] & 0x000000FF)       )
      );
    } else {
      throw new Exception("Bytes.ToInt: invalid input array length");
    }
  }

  public static Integer toInt(byte one) throws Exception {
    byte[] bs = new byte[]{ one };
    return toInt(bs);
  }

  public static void clear(byte[] bs,Integer from,Integer to){
    for(int i=from; i<to; i++){
      bs[i] = (byte)0x00;
    }
  }

  public static void copy(byte[] from,byte[] to, Integer toIndex){
    //TODO: exception handling
    Integer fromEnd = from.length - 1;
    Integer toEnd = to.length - 1;
    Integer fromIndex = 0;
    while(true){
      if(fromIndex > fromEnd) break;
      if(toIndex > toEnd) break;
      to[toIndex] = from[fromIndex];
      toIndex++;
      fromIndex++;
    }
  }

  public static String toString(byte b){
    return toString(new byte[]{b});
  }
  public static String toString(byte[] bs){
    String hexString = "0123456789ABCDEF";
    String s = "[";
    for(int i=0; i<bs.length; i++){
      s += (s=="["?"":",");
      if(i%16==0 && i!=0)s+="\n";
      byte b = bs[i];
      s += String.format("%02X", b);
    }
    s = s + ']';
    return s;
  }

  public static void print(String prefix,byte[] bytes){
    String s = toString(bytes);
    Test.log(prefix);
    Test.log(s);
  }

  public static void print(byte[] bytes){
    print("",bytes);
  }

  public static byte[] fromArrayList(ArrayList<byte[]> albs ) throws Exception {

    //Serialize the albs member to a single byte array for page storage

    //Get the length of all byte arrays to know what to initialize below
    //TODO: consider converting to ArrayList<ArrayList<Byte>>, may help reduce/eliminate algorithms like this
    Integer cellsLength = 0;
    for(int i = albs.size()-1; i>=0; i--){
      byte[] b = albs.get(i);
      cellsLength += b.length;
    }
    Test.log("cellsLength: "+cellsLength);

    byte[] bytes = new byte[cellsLength];
    Integer index = 0;
    for(byte[] b : albs){
      Bytes.copy(b,bytes,index); 
      index = index + b.length;
    }

    Bytes.print("Returning from Bytes.fromArrayList: ",bytes);
    return bytes;
  }
}







