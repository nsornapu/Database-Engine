
import java.io.RandomAccessFile;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;
import java.util.SortedMap;
import java.util.ArrayList;
import java.util.Arrays;
import static java.lang.System.out;

public class PromptEx {

  /* This can be changed to whatever you like */
  static String prompt = "davisql> ";
  static String version = "v1.0b(example)";
  static String copyright = "2016 Chris Irwin Davis";
  static boolean isExit = false;
  
  //Page size for all files is 512 bytes by default.
  //You may choose to make it user modifiable
  static long pageSize = 512; 
  static short TINYINT = 0x4;
  static short SMALLINT = 0x5;
  static short INT = 0x6;
  static short DOUBLE = 0x9;
  static short DATE = 0x0B;
  static short DATETIME = 0x0A;
  static short REAL = 0x08;
  static short BIGINT = 0x07;
  static short TEXT = 0x0C;

  /* 
   *  The Scanner class is used to collect user commands from the prompt
   *  There are many ways to do this. This is just one.
   *
   *  Each time the semicolon (;) delimiter is entered, the userCommand 
   *  String is re-populated.
   */
  static Scanner scanner = new Scanner(System.in).useDelimiter(";");

  /** ***********************************************************************
   *  Main method
   * @throws IOException 
   */
    public static void main(String[] args) throws IOException {

    /* Display the welcome screen */
    splashScreen();

    /* Variable to collect user input from the prompt */
    String userCommand = ""; 

    while(!isExit) {
      out.print(prompt);
      /* toLowerCase() renders command case insensitive */
      userCommand = scanner.next().replace("\n", " ").replace("\r", "").trim().toLowerCase();
      // userCommand = userCommand.replace("\n", "").replace("\r", "");
      parseUserCommand(userCommand);
    }
    out.println("Exiting...");

  }

  /** ***********************************************************************
   *  Static method definitions
   */

  /**
   *  Display the splash screen
   */
  public static void splashScreen() {
    out.println(line("-",80));
    out.println("Welcome to DavisBaseLite"); // Display the string.
    out.println("DavisBaseLite Version " + getVersion());
    out.println(getCopyright());
    out.println("\nType \"help;\" to display supported commands.");
    out.println(line("-",80));
  }

  /**
   * @param s The String to be repeated
   * @param num The number of time to repeat String s.
   * @return String A String object, which is the String s appended to itself num times.
   */
  public static String line(String s,int num) {
    String a = "";
    for(int i=0;i<num;i++) {
      a += s;
    }
    return a;
  }

  public static void printCmd(String s) {
    out.println("\n\t" + s + "\n");
  }
  public static void printDef(String s) {
    out.println("\t\t" + s);
  }

  //Help: Display supported commands
  public static void help() {
    out.println(line("*",80));
    out.println("SUPPORTED COMMANDS\n");
    out.println("All commands below are case insensitive\n");
    out.println("SHOW TABLES;");
    out.println("\tDisplay the names of all tables.\n");
    //printCmd("SELECT * FROM <table_name>;");
    //printDef("Display all records in the table <table_name>.");
    out.println("SELECT <column_list> FROM <table_name> [WHERE <condition>];");
    out.println("\tDisplay table records whose optional <condition>");
    out.println("\tis <column_name> = <value>.\n");
    out.println("DROP TABLE <table_name>;");
    out.println("\tRemove table data (i.e. all records) and its schema.\n");
    out.println("UPDATE TABLE <table_name> SET <column_name> = <value> [WHERE <condition>];");
    out.println("\tModify records data whose optional <condition> is\n");
    out.println("VERSION;");
    out.println("\tDisplay the program version.\n");
    out.println("HELP;");
    out.println("\tDisplay this help information.\n");
    out.println("EXIT;");
    out.println("\tExit the program.\n");
    out.println(line("*",80));
  }

  /** return the DavisBase version */
  public static String getVersion() {
    return version;
  }

  public static String getCopyright() {
    return copyright;
  }

  public static void displayVersion() {
    out.println("DavisBaseLite Version " + getVersion());
    out.println(getCopyright());
  }

  public static void parseUserCommand (String userCommand) throws IOException {

    /* commandTokens is an array of Strings that contains one token per array element 
     * The first token can be used to determine the type of command 
     * The other tokens can be used to pass relevant parameters to each command-specific
     * method inside each case statement */
    // String[] commandTokens = userCommand.split(" ");
    ArrayList<String> commandTokens = new ArrayList<String>(Arrays.asList(userCommand.split(" ")));

    if(!tableExists("davisbase_tables")||!tableExists("davisbase_columns")) {
      createDavisBaseTable();
      createDavisBaseColumn();
    }

    /*
    *  This switch handles a very small list of hardcoded commands of known syntax.
    *  You will want to rewrite this method to interpret more complex commands. 
    */
    switch (commandTokens.get(0)) {
      case "select":
        out.println("CASE: SELECT");
        parseQuery(userCommand);
        break;
      case "drop":
        out.println("CASE: DROP");
        dropTable(userCommand);
        break;
      case "create":
        out.println("CASE: CREATE");
        parseCreateTable(userCommand);
        break;
      case "update":
        out.println("CASE: UPDATE");
        parseUpdate(userCommand);
        break;
      case "help":
        help();
        break;
      case "version":
        displayVersion();
        break;
      case "exit":
        isExit = true;
        break;
      case "quit":
        isExit = true;
        break;
      default:
        out.println("I didn't understand the command: \"" + userCommand + "\"");
        break;
    }
  }

  /**
   *  Stub method for dropping tables
   *  @param dropTableString is a String of the user input
   */
  public static void dropTable(String dropTableString) {
    out.println("STUB: This is the dropTable method.");
    out.println("\tParsing the string:\"" + dropTableString + "\"");
    ArrayList<String> dropTableTokens = new ArrayList<String> (Arrays.asList(dropTableString.split(" ")));
    String tableFileName = dropTableTokens.get(2) + ".tbl";
    File file = new File(tableFileName);
    file.delete();
  }

  /**
   *  Stub method for executing queries
   *  @param queryString is a String of the user input
   */
  public static void parseQuery(String queryString) {
    //out.println("STUB: This is the parseQuery method");
    //out.println("\tParsing the string:\"" + queryString + "\"");
  }

  /**
   *  Stub method for updating records
   *  @param updateString is a String of the user input
   */
  public static void parseUpdate(String updateString) {
    //out.println("STUB: This is the dropTable method");
    //out.println("Parsing the string:\"" + updateString + "\"");
  }


  /**
   *  Stub method for creating new tables
   *  @param queryString is a String of the user input
   */
  public static void parseCreateTable(String createTableString) {

    out.println("STUB: Calling your method to create a table");
    out.println("Parsing the string:\"" + createTableString + "\"");
    //ArrayList<String> createTableTokens = new ArrayList<String>(Arrays.asList(createTableString.split(" ")));

    /* Define table file name */
    //String tableFileName = createTableTokens.get(2) + ".tbl";

    /* YOUR CODE GOES HERE */

    /*  Code to create a .tbl file to contain table data */
//    try {
//      /*  Create RandomAccessFile tableFile in read-write mode.
//       *  Note that this doesn't create the table file in the correct directory structure
//       */
//      RandomAccessFile tableFile = new RandomAccessFile(tableFileName, "rw");
//      tableFile.setLength(pageSize);
//      tableFile.seek(0);
//      tableFile.writeInt(63);
//    }
//    catch(Exception e) {
//      out.println(e);
//    }

    /*  Code to insert a row in the davisbase_tables table 
     *  i.e. database catalog meta-data 
     */

    /*  Code to insert rows in the davisbase_columns table  
     *  for each column in the new table 
     *  i.e. database catalog meta-data 
     */
    ArrayList<String> createTableTokens = new ArrayList<String>(Arrays.asList(createTableString.replace(")"," )").replace("(","( ").replaceAll(" +"," ").split(" ")));
    createTable(createTableTokens);
  }

  /**
   *  Stub method for inserting records
   *  @param insertString is a String of the user input
   */
  public static void parseInsert(String insertString) {
    ArrayList<String> commandTokens = new ArrayList<String>(Arrays.asList(insertString.replace(","," ").replace(")"," )").replace("(","( ").replaceAll(" +"," ").split(" ")));
    insertIntoTable(commandTokens);
  }

  public static void parseSelect(String selectString) throws IOException {
    ArrayList<String> commandTokens = new ArrayList<String>(Arrays.asList(selectString.replace(","," ").replace(")"," )").replace("(","( ").replaceAll(" +"," ").split(" ")));
    selectStar(commandTokens);
  }

  /*
   * show table
   */
  public static void show() {
    File dataDir = new File("data");
    dataDir.mkdir();
    String[] oldTableFiles = dataDir.list();
    for (int i=0; i<oldTableFiles.length; i++) {
      if(oldTableFiles[i].contains(".tbl"))
      out.println(oldTableFiles[i]);
    }
  }

  public static void createTable(ArrayList<String> commandTokens) {
    try {

      StringBuilder insideBracketData= new StringBuilder();
      String tableName = commandTokens.get(2);
      //out.println(commandTokens);
      /*
       * Check for the existance of the table
       */
      if(tableExists(tableName)) {
        out.println("This table exists");
        return;
      }
      /*
       * Check if there is an open bracket after table name. If not it is a syntax error
       */
      if(!commandTokens.get(3).equals("(")) {
        out.println("incorrect query statement.\n create table tablename ( col_name data_type [NOT NULL],*);");
        return;
      }
      /*
       * Get all the contents within the brackets. 
       */
      for(int i = 4 ; !commandTokens.get(i).equals(")"); i++) {
        insideBracketData.append(commandTokens.get(i)+" ");
      }
      /*
       * from the content, we get all the column information, like name, data type and the not null
       */
      ArrayList<String> columns = new ArrayList<String>(Arrays.asList(insideBracketData.toString().split(",")));

      ArrayList<String> insertQueriesDbc = new ArrayList<String>();
      for(int i=0;i<columns.size();i++) {
        //out.println(columns.get(i));
        ArrayList<String>colInfo = new ArrayList<String>(Arrays.asList(columns.get(i).replaceAll(" +"," ").split(" ")));

        //a hack to remove the initial whitespace
        if(colInfo.get(0).equals("")) {
          colInfo.remove(0);
        }
        if (colInfo.size()<=1 || colInfo.size()>4 ||colInfo.size() == 3) {
          out.println("1 incorrect query statement.\n create table tablename ( col_name data_type [NOT NULL],*);");
          return;
        }
        if(colInfo.size()==2) {
          String insertDbc =("insert into davisbase_columns ( table_name,column_name, data_type, ordinal_position , is_nullable) values ( " + tableName + " , "+colInfo.get(0)+ " , "+colInfo.get(1).toUpperCase() + " , "+ (i+1) + " , "+ "YES" + " )");
          //out.println(insertDbc);
          //insert into davisbasecolumn (tablename,colname,datatype,ordinalPosition,isnull);
          insertQueriesDbc.add(insertDbc);
        } else if(colInfo.size()==4) {
          if(!colInfo.get(2).equals("not")) {
            out.println("2 incorrect query statement.\n create table tablename ( col_name data_type [NOT NULL],*);");
            return;
          }
          if(!colInfo.get(3).equals("null")) {
            out.println("3 incorrect query statement.\n create table tablename ( col_name data_type [NOT NULL],*);");
            return;
          }
          String insertDbc =("insert into davisbase_columns ( table_name,column_name, data_type, ordinal_position , is_nullable) values ( " + tableName + " , "+colInfo.get(0)+ " , "+colInfo.get(1) + " , "+ (i+1) + " , "+ "NO" + " )");
          //out.println(insertDbc);  
          insertQueriesDbc.add(insertDbc);
        }
      }
      for(int i = 0 ; i < insertQueriesDbc.size();i++) {
        parseInsert(insertQueriesDbc.get(i));
      }
      String insertQueryDbt = "insert into davisbase_tables (tableName) values ( "+tableName+ " )";
      parseInsert(insertQueryDbt);

        RandomAccessFile tableFile = new RandomAccessFile("data/"+tableName+".tbl", "rw");
        Page p = new Page();
        p.numberOfRecords=0;
        Offsets o = new Offsets();
        o.numberOfOffsets = 0;
        p.offsets = o;
        ArrayList<Page> pages = new ArrayList<Page>();
        pages.add(p);
        ReadWritePage rw = new ReadWritePage();
        rw.WritePage(tableFile, pages);
    }
    catch(IndexOutOfBoundsException e) {
      e.printStackTrace();
      out.println("Query is Too short");
    } catch (IOException e) {
      e.printStackTrace();
    } 

  }

  public static void insertIntoTable(ArrayList<String> commandTokens ) {
    try {
      int valueStartingPosition=0;
      ArrayList<String> columns = new ArrayList<String>();
      ArrayList<String> values = new ArrayList<String>();
      String tableName = commandTokens.get(2);
      if(!tableExists(tableName)) {
        out.println("Table does not exist in the catalog");
        return;
      }
      if(!commandTokens.get(3).equals("(")) {
        //out.print(commandTokens.get(3));
        out.println("incorrect query statement. You have to mention the columns");
        return;
      }
      for(int i = 4; !commandTokens.get(i).equals(")");i++) {
        columns.add(commandTokens.get(i));  
      }
      valueStartingPosition = commandTokens.indexOf(columns.get(columns.size()-1))+4;
      for(int i=valueStartingPosition; !commandTokens.get(i).equals(")");i++) {
        values.add(commandTokens.get(i));
      }
      if(values.size()!=columns.size()) {
        out.println("Value column size mismatch");
        return;
      }


      RandomAccessFile table = new RandomAccessFile("data/"+tableName+".tbl", "rw");
      ReadWritePage reader = new ReadWritePage();
      ArrayList<Page> pages = reader.ReadTable(table);
      Page p = pages.get(pages.size()-1);
      Offsets o = p.getOffsets();
      int b;

      // help //
      if(o.offset.size()==0) {
        b=512;
      } else { 
        b =o.offset.get(o.offset.size()-1);
      }
      int a = (int) ((pageSize*(pages.size()-1))+8+(2*o.offset.size()));
      int freeSpace = b-a;
      ArrayList<String>tableColumns = getColumns(tableName);
      ArrayList<String>tableDataTypes = getDataTypes(tableName);
      if(tableColumns.size()==0) {
        out.println("columns are incorrect");
        return;
      }
      for(int i=0;i<tableColumns.size();i++) {
        if(!tableColumns.get(i).equals(columns.get(i))) {
          out.println("columns are incorrect");
          return;
        }
      }

      int payloadSize = calculatePayloadSize(values,tableDataTypes);

      if(freeSpace<payloadSize+2)//2 represents the size for that particular offset {
        //get the rowid
        int rowId = p.payloads.get(p.payloads.size()-1).rowId;
        //create the new page
        p = new Page();
        p.numberOfRecords++;
        //figure out the offset
        p.offsets.offset.add((short)(pageSize*(pages.size()+1) - payloadSize ));
        p.offsets.numberOfOffsets = 1;
        //figure out the payload
        Payload payload = generateSinglePayload(values,tableDataTypes,rowId,payloadSize);
        p.payloads.add(payload);
        pages.add(p);
      } else {
        //get row id
        //help//
        int rowId=0;
        if(p.payloads.size()==0) {

        }else {
         rowId = p.payloads.get(p.payloads.size()-1).rowId;
        }
        //get the new offset
        //help//
        int old_offset = 512;
        if(p.offsets.getNumberOfOffsets()==0) {

        } else {
        old_offset = p.offsets.offset.get(p.offsets.getNumberOfOffsets()-1);
        }
        int new_offset = old_offset - payloadSize;
        o.numberOfOffsets++;
        o.offset.add((short)new_offset);
        p.setOffsets(o);
        Payload payload = generateSinglePayload(values,tableDataTypes,rowId,payloadSize);
        p.payloads.add(payload);
        p.numberOfRecords++;

      }
      reader.WritePage(table, pages);
    }catch(IndexOutOfBoundsException e) {
      e.printStackTrace();
      out.println("Query is Too short");
    }
    catch(FileNotFoundException e) {
      out.println("File not found");
    } 
    catch (IOException e) {
      out.println("IOException");
      e.printStackTrace();
    }
  }


  public static boolean tableExists(String tableName) {
    File dataDir = new File("data");
    dataDir.mkdir();
    String[] oldTableFiles;
    oldTableFiles = dataDir.list();
    for (int i=0; i<oldTableFiles.length; i++) {
      if(oldTableFiles[i].equals(tableName+".tbl")) {
        return true;
      }
    }
    return false;
  } 

  public static void createDavisBaseTable() {
    try {
      DavisBaseColumn c = new DavisBaseColumn();
      c.createDavisBaseTable();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }   
  }

  public static void createDavisBaseColumn() throws IOException {
    try {
      DavisBaseColumn c = new DavisBaseColumn();
      c.createDavisBaseColumn();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } 
  }

//public static void selectStarWhere(ArrayList<String> commandTokens) throws IOException {
//  String tableName = commandTokens.get(3);
//  ReadWritePage readWrite = new ReadWritePage();
//  RandomAccessFile r = new RandomAccessFile("data/"+tableName+".tbl","r");
//  RandomAccessFile r2 = new RandomAccessFile("data/davisbase_columns.tbl","r");
//  ArrayList<Page> dbcPages = readWrite.ReadTable(r2);
//  if(tableName.equals("davisbase_columns")||tableName.equals("davisbase_tables")) {
//
//  } else {
//    out.print("rowId\t");
//  }
//
//  for (int i = 0; i < dbcPages.size(); i++) {
//    Page p = dbcPages.get(i);
//    ArrayList<Payload> payloads = p.payloads;
//    for(int j=0;j<payloads.size();j++) {
//      Payload payload = payloads.get(j);
//      if(payload.data.get(0).equals(tableName)) {
//        out.print(payload.data.get(1)+"\t");
//      }
//    }
//  }
//  out.println();
//
//
//  ArrayList<Page> pages =  readWrite.ReadTable(r);
//  for (int i = 0; i < pages.size(); i++) {
//    Page p = pages.get(i);
//    ArrayList<Payload> payloads = p.payloads;
//    for(int j=0;j<payloads.size();j++) {
//      Payload payload = payloads.get(j);
//      int rid = payloads.get(j).rowId;
//      out.print(rid+"\t");
//      for(int k=0;k<payload.data.size();k++) {
//        out.print(payload.data.get(k) + "\t");
//      }
//      out.println();
//    }
//  }
//  r.close();
//  r2.close();
//}


  public static void selectStar(ArrayList<String> commandTokens) throws IOException {
    String tableName = commandTokens.get(3);
    ReadWritePage readWrite = new ReadWritePage();
    RandomAccessFile r = new RandomAccessFile("data/"+tableName+".tbl","r");
    RandomAccessFile r2 = new RandomAccessFile("data/davisbase_columns.tbl","r");
    if(tableName.equals("davisbase_columns")||tableName.equals("davisbase_tables")) {

    } else {
      out.print("rowId\t");
    }
    ArrayList<Page> dbcPages = readWrite.ReadTable(r2);
    for (int i = 0; i < dbcPages.size(); i++) {
      Page p = dbcPages.get(i);
      ArrayList<Payload> payloads = p.payloads;
      for(int j=0;j<payloads.size();j++) {
        Payload payload = payloads.get(j);
        if(payload.data.get(0).equals(tableName)) {
          out.print(payload.data.get(1)+"\t");
        }
      }
    }
    out.println();


    ArrayList<Page> pages =  readWrite.ReadTable(r);
    for (int i = 0; i < pages.size(); i++) {
      Page p = pages.get(i);
      ArrayList<Payload> payloads = p.payloads;
      for(int j=0;j<payloads.size();j++) {
        Payload payload = payloads.get(j);
        int rid = payloads.get(j).rowId;
        out.print(rid+"\t");
        for(int k=0;k<payload.data.size();k++) {
          out.print(payload.data.get(k) + "\t");
        }
        out.println();
      }
    }
    r.close();
    r2.close();
  }

  public static Payload generateSinglePayload(ArrayList<String> data,ArrayList<String> types,int rid,int payloadSize) {
    //This looks like Payload constructor code
    Payload p = new Payload();
    ArrayList<Integer> dataType = new ArrayList<Integer>();
    int numberOfRecords = data.size();
    int rowId = rid+1;
    int header = payloadSize ;
    DataType dt = new DataType();

    for(int i = 0; i<types.size();i++) {
      int x = dt.dataType.get(types.get(i));
      if (x==0x0C) {
        x+=values.get(i).length();
      }
      dataType.add(x);
    }
    p.rowId = rowId;
    p.header = header;
    p.data = data;
    p.dataType = dataType;
    p.numberOfRecords = numberOfRecords;
    return p;
  }

//  public static int numberOfRecords(RandomAccessFile r) {
//    try {
//      int pagenumber = whatPage(r);
//      r.seek((pagenumber*pageSize)+1);
//      int number =r.readByte();
//      return number;
//    }
//    catch(IOException e) {
//      e.printStackTrace();
//      return -1;
//    }
//  }

//  public static int whatPage(RandomAccessFile r) {
//    try {
//      r.readLine();//need this line?
//      long page = r.length();
//      page = page/pageSize;
//      return (int)page;
//    } catch (IOException e) {
//      e.printStackTrace();
//      return -1;
//    }
//  }

  public static ArrayList<String> getColumns(String tableName) {
    try {
      ReadWritePage reader = new ReadWritePage();
      RandomAccessFile r;
      ArrayList<String> columns = new ArrayList<String>();
      r = new RandomAccessFile("data/davisbase_columns.tbl", "rw");
      ArrayList<Page> dbcPages = reader.ReadTable(r);
      for(int i = 0; i<dbcPages.size(); i++) {
        Page p = dbcPages.get(i);
        for(int j = 0; j< p.payloads.size(); j++) {
          Payload payload = p.payloads.get(j);
          String table = payload.data.get(0);
          if(table.equals(tableName)) {
            columns.add(payload.data.get(1));
          }
        }
      }
      if(tableName.equals("davisbase_tables")||tableName.equals("davisbase_columns"))
      columns.remove(0);
      return columns;
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      return new ArrayList<String>();
    }
  }

  public static ArrayList<String> getDataTypes(String tableName) {
    try {
      ReadWritePage reader = new ReadWritePage();
      RandomAccessFile r;
      ArrayList<String> dataTypes = new ArrayList<String>();
      r = new RandomAccessFile("data/davisbase_columns.tbl", "rw");
      ArrayList<Page> dbcPages = reader.ReadTable(r);
      for(int i = 0; i<dbcPages.size(); i++) {
        Page p = dbcPages.get(i);
        for(int j = 0; j< p.payloads.size(); j++) {
          Payload payload = p.payloads.get(j);
          String table = payload.data.get(0);
          if(table.equals(tableName)) {
            dataTypes.add(payload.data.get(2));
          }
        }
      }
      if(tableName.equals("davisbase_tables")||tableName.equals("davisbase_columns"))
      dataTypes.remove(0);
      return dataTypes;

    } catch (FileNotFoundException e) {
      e.printStackTrace();
      return new ArrayList<String>();
    }
  }

  public static int calculatePayloadSize(ArrayList<String> data, ArrayList<String> dataTypes) {
    int rowidLength = 4;
    int headerLength = 2;
    int numberOfColumnsLength = 1;
    int columnSizesLength = data.size();
    int total;
    int datasize=0;
    DataType dt = new DataType();
    for(int i =0; i <data.size();i++) {
      //we don't need to do this if we have dt.dataContent/dt.dataWidth
      if(dt.dataType.get(dataTypes.get(i))==(0x00)) {
        datasize+=1;
      } else if(dt.dataType.get(dataTypes.get(i))==(0x01)) {
        datasize+=1;
      } else if(dt.dataType.get(dataTypes.get(i))==(0x02)) {
        datasize+=1;
      } else if(dt.dataType.get(dataTypes.get(i))==(0x03)) {
        datasize+=1;
      } else if(dt.dataType.get(dataTypes.get(i))==(0x04)) {
        datasize+=1;
      } else if(dt.dataType.get(dataTypes.get(i))==(0x05)) {
        datasize+=2;
      } else if(dt.dataType.get(dataTypes.get(i))==(0x06)) {
        datasize+=4;
      } else if(dt.dataType.get(dataTypes.get(i))==(0x07)) {
        datasize+=4;
      } else if(dt.dataType.get(dataTypes.get(i))==(0x08)) {
        datasize+=8;
      } else if(dt.dataType.get(dataTypes.get(i))==(0x09)) {
        datasize+=8;
      } else if(dt.dataType.get(dataTypes.get(i))==(0x0A)) {
        datasize+=10;
      } else if(dt.dataType.get(dataTypes.get(i))==(0x0B)) {
        datasize+=10;
      } else {
        datasize+=data.get(i).length();
      }
    }
    total = headerLength + rowidLength + numberOfColumnsLength + columnSizesLength + datasize;
    return total;
  }

} 





