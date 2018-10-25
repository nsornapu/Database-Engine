
import static java.lang.System.*;
import java.util.*;

class Test {
  public static String toString(int i){
    return Integer.toString(i);
  }
  public static String toString(boolean b){
    return Boolean.toString(b);
  }
  public static String toString(String s){
    return s;
  }
  public static void equals(int a, int b) throws Exception {
    Test.equals(Test.toString(a), Test.toString(b)); 
  } 
  public static void equals(boolean a, boolean b) throws Exception {
    Test.equals(Test.toString(a), Test.toString(b)); 
  } 
  public static void equals(String a, String b) throws Exception {
    if( !a.equals(b) ){
      String message = "Assert Failure: \n"
        + "Left Val: \n"
        + Test.toString(a) + "\n"
        + "Right Val: \n"
        + Test.toString(b) + "\n"
        ;
      throw new Exception(message);
    }
  } 
  public static int logLevel = 1;
  public static void log(){
    Test.log("");
  }
  public static void log(String s){
    if(logLevel <= 1) out.println(s);
  }
  public static void log(Exception e){
    if(logLevel <= 1) Test.printStackTrace(e);
  }
  public static void printStackTrace(Exception e){
    try{
      //log(e.toString());
      if(logLevel <= 1) e.printStackTrace(System.out);
    }catch(Exception e2){
      log("Error printing exception");
    }
  }
  public static String getStackTrace(){
    StackTraceElement[] e = Thread.currentThread().getStackTrace();
    String t = Arrays.toString(e);
    return t;
  }

//public static void throwPrintable(String s) throws Exception{
//  //An alternative way to print the stack trace
//  StackTraceElement[] e = Thread.currentThread().getStackTrace();
//  String t = Arrays.toString(e);
//  //log(s);
//  throw new Exception(s+"\n"+t);
////  try{
////  }catch(Exception e){
////    Test.printStackTrace(e); 
////    throw e;
////  }
//}

}




