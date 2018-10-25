
import static java.lang.System.*;
import java.util.*;

//enum PageType {
//INDEX_INNER(0x02),
//TABLE_INNER(0x05),
//INDEX_LEAF(0x0a),
//TABLE_LEAF(0x0d)
//;
//public byte pageTypeCode;
//private PageType(Integer pageTypeCode) {
//  //Default - had to do this due to 'pageTypeCode may have been assigned' compiler error...thanks Java!
//  byte tc = (byte)0x0d;
//  try{
//    byte[] code = Bytes.fromInt(pageTypeCode,1);
//    tc = code[0];
//  }catch(Exception e){ 
//    Test.log(e); 
//  }
//  this.pageTypeCode = tc;
//}
//}




public class PageType {

  public static Map<String, Integer> map = new HashMap<String, Integer>();
  static {
    map.put("INDEX_INNER",0x02);
    map.put("TABLE_INNER",0x05);
    map.put("INDEX_LEAF",0x0a);
    map.put("TABLE_LEAF",0x0d);
  }

  public static Integer get(String key) throws Exception {
    return map.get(key);
  }

  public static Integer tryGet(String key) {
    Integer i = null;
    try{
      i = PageType.get(key);
    } catch(Exception e) {
      // Do nothing ... i is null
    }
    return i;
  }

//public static byte getByte(String key) throws Exception {
//  Integer i = PageType.get(key);
//  bytes b = Bytes.fromInt(i,1)[0];
//  return b;
//}

  public static String getKeyFromValue(Integer value) {
    for (String o : map.keySet()) {
      if (map.get(o).equals(value)) {
        return o;
      }
    }
    return null;
  }

}
