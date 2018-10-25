package bTreeImplementation;

import static java.lang.System.out;
import bTreeImplementation.BTree;

import java.io.RandomAccessFile;
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;;

public class CommandPromptMain {
	private static final String prompt = "davissql> ";
	private static final String version = "v1.0";
	private static final String copyright = "@2018 Team Purple";
	static long pageSize = 512;

	private static final String davisbase_tables = "data\\catalog\\davisbase_tables.tbl";
	public static final String davisbase_columns = "data\\catalog\\davisbase_columns.tbl";
	private static final String table_Location = "data\\user_data\\";
	private static final String table_Format = ".tbl";

	private static boolean isExit = false;
	private static RandomAccessFile davisBaseColumnFile;
	private static RandomAccessFile davisBaseTableFile;

	static Scanner scanner = new Scanner(System.in).useDelimiter(";");

	/**
	 * *********************************************************************** Main
	 * method
	 */
	
	public static boolean CompareValues(String type, String value) {
		switch (type.toLowerCase()) {
		case "text":
			if ((value.charAt(0) == '\'' && value.charAt(value.length() - 1) == '\'')
					|| (value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"'))
				return true;
			break;
		case "tinyint":
			if (Integer.parseInt(value) >= Byte.MIN_VALUE && Integer.parseInt(value) <= Byte.MAX_VALUE)
				return true;
			break;
		case "smallint":
			if (Integer.parseInt(value) >= Short.MIN_VALUE && Integer.parseInt(value) <= Short.MAX_VALUE)
				return true;
			break;
		case "int":
			if (Integer.parseInt(value) >= Integer.MIN_VALUE && Integer.parseInt(value) <= Integer.MAX_VALUE)
				return true;
			break;
		case "bigint":
			if (Long.parseLong(value) >= Long.MIN_VALUE && Long.parseLong(value) <= Long.MAX_VALUE)
				return true;
			break;
		case "real":
			if (Float.parseFloat(value) >= Float.MIN_VALUE && Float.parseFloat(value) <= Float.MAX_VALUE)
				return true;
			break;
		case "double":
			if (Double.parseDouble(value) >= Double.MIN_VALUE && Double.parseDouble(value) <= Double.MAX_VALUE)
				return true;
			break;
		case "datetime":
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
			try {
				Date date = df.parse(value);
			} catch (ParseException e) {
				return false;
			}
			return true;
		case "date":
			SimpleDateFormat d = new SimpleDateFormat("yyyy-MM-dd");
			try {
				
				Date date = d.parse(value);
			} catch (ParseException e) {
				return false;
			}
			return true;
		default:
			return false;
		}
		return false;

	}

	
	public static void PrintTable(List<LinkedHashMap<String, ArrayList<String>>> table) {
		
		if (table.isEmpty()) {
			System.out.println("0 Rows Returned. The table is empty.");
			return;
		}
		
		int size[] = new int[table.get(0).size()];
		Arrays.fill(size, -1);
		int k = 0;
		for (LinkedHashMap<String, ArrayList<String>> x : table) {
			k = 0;
			for (String y : x.keySet()) {
				if (size[k] < y.length()) {
					size[k] = y.length();
				}
				if (size[k] < x.get(y).get(0).length()) {
					size[k] = x.get(y).get(0).length();
				}
				k++;
			}
		}
		for (int i : size)
			System.out.print("+" + Line("-", i));
		System.out.print("+\n");
		k = 0;
		for (String x : table.get(0).keySet())
			System.out.print("|" + format(x.toUpperCase(), size[k++]));
		System.out.print("|\n");
		for (int i : size)
			System.out.print("+" + Line("-", i));
		System.out.print("+\n");
		for (LinkedHashMap<String, ArrayList<String>> x : table) {
			k = 0;
			for (String y : x.keySet())
				System.out.print("|" + format(x.get(y).get(0), size[k++]));
			System.out.print("|\n");
			for (int i : size)
				System.out.print("+" + Line("-", i));
			System.out.print("+\n");
		}

	}

	public static String format(String string, int length) {
		return String.format("%1$" + length + "s", string);
	}

	public static void main(String[] args) throws Throwable {

		File folder = new File("data");
		if (!folder.exists()) {
			folder.mkdir();
			folder = new File("data/catalog");
			folder.mkdir();
			folder = new File("data/user_data");
			folder.mkdir();
		}
		splash_Screen();
		String userCommand = "";
		BTree davisBaseTableTree = null;
		try {
			davisBaseTableFile = new RandomAccessFile(davisbase_tables, "rw");
			davisBaseTableTree = new BTree(davisBaseTableFile, "davisbase_tables", false, true);
			if (davisBaseTableFile.length() == 0) {
				LinkedHashMap<String, ArrayList<String>> token = new LinkedHashMap<String, ArrayList<String>>();
				ArrayList<String> arrayValues = new ArrayList<String>();
				arrayValues.add("int");
				arrayValues.add("1");
				token.put("rowid", new ArrayList<String>(arrayValues));
				arrayValues.clear();
				arrayValues.add(" text");
				arrayValues.add("davisbase_tables");
				token.put("table_name", new ArrayList<String>(arrayValues));
				davisBaseTableTree.CreateNewTableLeaf(token);
				token.clear();
				arrayValues.clear();
				arrayValues.add("int");
				arrayValues.add("2");
				token.put("rowid", new ArrayList<String>(arrayValues));
				arrayValues.clear();
				arrayValues.add(" text");
				arrayValues.add("davisbase_columns");
				token.put("table_name", new ArrayList<String>(arrayValues));
				davisBaseTableTree.InsertNewRecord(token);
			}

		} catch (Exception e) {
			System.out.println(" ");
		}

		try {
			davisBaseColumnFile = new RandomAccessFile(davisbase_columns, "rw");
			BTree davisBaseColumnFileTree = new BTree(davisBaseColumnFile, "davisbase_columns", true, false);

			if (davisBaseColumnFile.length() == 0) {
				LinkedHashMap<String, ArrayList<String>> token = new LinkedHashMap<String, ArrayList<String>>();
				ArrayList<String> arrayValues = new ArrayList<String>();

				arrayValues.add("INT");
				arrayValues.add("1");
				token.put("rowid", new ArrayList<String>(arrayValues));

				arrayValues.clear();
				arrayValues.add(" TEXT");
				arrayValues.add("davisbase_tables");
				token.put("table_name", new ArrayList<String>(arrayValues));

				arrayValues.clear();
				arrayValues.add(" TEXT");
				arrayValues.add("rowid");
				token.put("column_name", new ArrayList<String>(arrayValues));

				arrayValues.clear();
				arrayValues.add(" TEXT");
				arrayValues.add("INT");
				token.put("data_type", new ArrayList<String>(arrayValues));

				arrayValues.clear();
				arrayValues.add("TINYINT");
				arrayValues.add("1");
				token.put("ordinal_position", new ArrayList<String>(arrayValues));

				arrayValues.clear();
				arrayValues.add("TEXT");
				arrayValues.add("NO");
				token.put("is_nullable", new ArrayList<String>(arrayValues));

				davisBaseColumnFileTree.CreateNewTableLeaf(token);

				arrayValues.clear();
				token.clear();

				arrayValues.add("INT");
				arrayValues.add("2");
				token.put("rowid", new ArrayList<String>(arrayValues));

				arrayValues.clear();
				arrayValues.add(" TEXT");
				arrayValues.add("davisbase_tables");
				token.put("table_name", new ArrayList<String>(arrayValues));

				arrayValues.clear();
				arrayValues.add(" TEXT");
				arrayValues.add("table_name");
				token.put("column_name", new ArrayList<String>(arrayValues));

				arrayValues.clear();
				arrayValues.add(" TEXT");
				arrayValues.add("TEXT");
				token.put("data_type", new ArrayList<String>(arrayValues));

				arrayValues.clear();
				arrayValues.add("TINYINT");
				arrayValues.add("2");
				token.put("ordinal_position", new ArrayList<String>(arrayValues));

				arrayValues.clear();
				arrayValues.add("TEXT");
				arrayValues.add("NO");
				token.put("is_nullable", new ArrayList<String>(arrayValues));
				davisBaseColumnFileTree.InsertNewRecord(token);

				arrayValues.clear();
				token.clear();

				arrayValues.add("INT");
				arrayValues.add("3");
				token.put("rowid", new ArrayList<String>(arrayValues));

				arrayValues.clear();
				arrayValues.add(" TEXT");
				arrayValues.add("davisbase_columns");
				token.put("table_name", new ArrayList<String>(arrayValues));

				arrayValues.clear();
				arrayValues.add(" TEXT");
				arrayValues.add("rowid");
				token.put("column_name", new ArrayList<String>(arrayValues));

				arrayValues.clear();
				arrayValues.add(" TEXT");
				arrayValues.add("INT");
				token.put("data_type", new ArrayList<String>(arrayValues));

				arrayValues.clear();
				arrayValues.add("TINYINT");
				arrayValues.add("1");
				token.put("ordinal_position", new ArrayList<String>(arrayValues));

				arrayValues.clear();
				arrayValues.add("TEXT");
				arrayValues.add("NO");
				token.put("is_nullable", new ArrayList<String>(arrayValues));
				davisBaseColumnFileTree.InsertNewRecord(token);

				arrayValues.clear();
				token.clear();

				arrayValues.add("INT");
				arrayValues.add("4");
				token.put("rowid", new ArrayList<String>(arrayValues));

				arrayValues.clear();
				arrayValues.add(" TEXT");
				arrayValues.add("davisbase_columns");
				token.put("table_name", new ArrayList<String>(arrayValues));

				arrayValues.clear();
				arrayValues.add(" TEXT");
				arrayValues.add("table_name");
				token.put("column_name", new ArrayList<String>(arrayValues));

				arrayValues.clear();
				arrayValues.add(" TEXT");
				arrayValues.add("TEXT");
				token.put("data_type", new ArrayList<String>(arrayValues));

				arrayValues.clear();
				arrayValues.add("TINYINT");
				arrayValues.add("2");
				token.put("ordinal_position", new ArrayList<String>(arrayValues));

				arrayValues.clear();
				arrayValues.add("TEXT");
				arrayValues.add("NO");
				token.put("is_nullable", new ArrayList<String>(arrayValues));
				davisBaseColumnFileTree.InsertNewRecord(token);

				arrayValues.clear();
				token.clear();

				arrayValues.add("INT");
				arrayValues.add("5");
				token.put("rowid", new ArrayList<String>(arrayValues));

				arrayValues.clear();
				arrayValues.add(" TEXT");
				arrayValues.add("davisbase_columns");
				token.put("table_name", new ArrayList<String>(arrayValues));

				arrayValues.clear();
				arrayValues.add(" TEXT");
				arrayValues.add("column_name");
				token.put("column_name", new ArrayList<String>(arrayValues));

				arrayValues.clear();
				arrayValues.add(" TEXT");
				arrayValues.add("TEXT");
				token.put("data_type", new ArrayList<String>(arrayValues));

				arrayValues.clear();
				arrayValues.add("TINYINT");
				arrayValues.add("3");
				token.put("ordinal_position", new ArrayList<String>(arrayValues));

				arrayValues.clear();
				arrayValues.add("TEXT");
				arrayValues.add("NO");
				token.put("is_nullable", new ArrayList<String>(arrayValues));
				davisBaseColumnFileTree.InsertNewRecord(token);

				arrayValues.clear();
				token.clear();

				arrayValues.add("INT");
				arrayValues.add("6");
				token.put("rowid", new ArrayList<String>(arrayValues));

				arrayValues.clear();
				arrayValues.add(" TEXT");
				arrayValues.add("davisbase_columns");
				token.put("table_name", new ArrayList<String>(arrayValues));

				arrayValues.clear();
				arrayValues.add(" TEXT");
				arrayValues.add("data_type");
				token.put("column_name", new ArrayList<String>(arrayValues));

				arrayValues.clear();
				arrayValues.add(" TEXT");
				arrayValues.add("TEXT");
				token.put("data_type", new ArrayList<String>(arrayValues));

				arrayValues.clear();
				arrayValues.add("TINYINT");
				arrayValues.add("4");
				token.put("ordinal_position", new ArrayList<String>(arrayValues));

				arrayValues.clear();
				arrayValues.add("TEXT");
				arrayValues.add("NO");
				token.put("is_nullable", new ArrayList<String>(arrayValues));
				davisBaseColumnFileTree.InsertNewRecord(token);

				arrayValues.clear();
				token.clear();

				arrayValues.add("INT");
				arrayValues.add("7");
				token.put("rowid", new ArrayList<String>(arrayValues));

				arrayValues.clear();
				arrayValues.add(" TEXT");
				arrayValues.add("davisbase_columns");
				token.put("table_name", new ArrayList<String>(arrayValues));

				arrayValues.clear();
				arrayValues.add(" TEXT");
				arrayValues.add("ordinal_position");
				token.put("column_name", new ArrayList<String>(arrayValues));

				arrayValues.clear();
				arrayValues.add(" TEXT");
				arrayValues.add("TINYINT");
				token.put("data_type", new ArrayList<String>(arrayValues));

				arrayValues.clear();
				arrayValues.add("TINYINT");
				arrayValues.add("5");
				token.put("ordinal_position", new ArrayList<String>(arrayValues));

				arrayValues.clear();
				arrayValues.add("TEXT");
				arrayValues.add("NO");
				token.put("is_nullable", new ArrayList<String>(arrayValues));
				davisBaseColumnFileTree.InsertNewRecord(token);

				arrayValues.clear();
				token.clear();

				arrayValues.add("INT");
				arrayValues.add("8");
				token.put("rowid", new ArrayList<String>(arrayValues));

				arrayValues.clear();
				arrayValues.add(" TEXT");
				arrayValues.add("davisbase_columns");
				token.put("table_name", new ArrayList<String>(arrayValues));

				arrayValues.clear();
				arrayValues.add(" TEXT");
				arrayValues.add("is_nullable");
				token.put("column_name", new ArrayList<String>(arrayValues));

				arrayValues.clear();
				arrayValues.add(" TEXT");
				arrayValues.add("TEXT");
				token.put("data_type", new ArrayList<String>(arrayValues));

				arrayValues.clear();
				arrayValues.add("TINYINT");
				arrayValues.add("6");
				token.put("ordinal_position", new ArrayList<String>(arrayValues));

				arrayValues.clear();
				arrayValues.add("TEXT");
				arrayValues.add("NO");
				token.put("is_nullable", new ArrayList<String>(arrayValues));
				davisBaseColumnFileTree.InsertNewRecord(token);

			}

		} catch (Exception e) {
			System.out.println(" ");
		}

		while (!isExit) {
			System.out.print(prompt);
			userCommand = scanner.next().replace("\n", "").replace("\r", "").trim().toLowerCase();
			ParseUserCommand(userCommand);
		}
		System.out.println("Exiting...");

	}

	public static void splash_Screen() {
		System.out.println(Line("-", 80));
		System.out.println("Welcome to DavisBase"); 
		System.out.println("DavisBase Version " + GetVersion());
		System.out.println(GetCopyrights());
		System.out.println("\nType \"help;\" to display supported commands.");
		System.out.println(Line("-", 80));
	}

	public static String Line(String s, int num) {
		String a = "";
		for (int i = 0; i < num; i++) {
			a += s;
		}
		return a;
	}

	public static void help() {
		System.out.println(Line("=", 80));
		System.out.println("SUPPORTED COMMANDS");
		System.out.println("All commands below are case insensitive");
		System.out.println(Line("-", 80));
		System.out.println("\tCREATE TABLE table_name (column_name1 INT PRIMARY KEY, column_name2 data_type2 [NOT NULL],... );");
		System.out.println("\t Create a new table schema if not already exist.");
		System.out.println("\tCREATE INDEX index_name <table_name> (column_name);");
		System.out.println("\t Create a new index on the table.");
		System.out.println("\tSELECT * FROM table_name;");
		System.out.println("\tDisplay all records in the table.");
		System.out.println("\tSELECT * FROM table_name WHERE <column_name> = <value>;");
		System.out.println("\tDisplay records whose column is <value>.");
		System.out.println("\tUPDATE <table_name> SET column_name = value WHERE primary_key=value;");
		System.out.println("\tModifies one or more records in a table.");
		System.out.println("\tINSERT INTO <table_name> (column_list) VALUES (value1, value2, value3,..);");
		System.out.println("\tInsert a new record into the indicated table.");
		System.out.println("\tDELETE FROM <table_name> WHERE primary_key = key_value; ");
		System.out.println("\tDelete a single row/record from a table given the row_id primary key.");
		System.out.println("\tDROP TABLE table_name;");
		System.out.println("\tRemove table data and its schema.");
		System.out.println("\tDROP INDEEX ON table_name;");
		System.out.println("\tRemove index from table data and its schema.");
		System.out.println("\tSHOW TABLES;");
		System.out.println("\tDisplays a list of all tables in the Database.");
		System.out.println("\tVERSION;");
		System.out.println("\tShows version.");
		System.out.println("\tHELP;");
		System.out.println("\tShow help");
		System.out.println("\tEXIT;");
		System.out.println("\tExits the program");
		System.out.println();
		System.out.println();
		System.out.println(Line("=", 80));
	}

	public static String GetVersion() {
		return version;
	}

	public static String GetCopyrights() {
		return copyright;
	}

	public static void ShowVersion() {
		System.out.println("DavisBase Version " + GetVersion());
		System.out.println(GetCopyrights());
	}

	public static void ParseUserCommand(String userCommand) throws Throwable {

		ArrayList<String> tokenCommands = new ArrayList<String>(Arrays.asList(userCommand.split(" ")));
		switch (tokenCommands.get(0)) {
		case "select":
			ParseQuery(userCommand);
			break;
		case "drop":
			switch (tokenCommands.get(1)) {
			case "table":
				DropTable(tokenCommands.get(2));
				break;
			case "index":
				DropIndex(userCommand);
				break;
		}
			if (tokenCommands.get(1).equals("table"))
				DropTable(tokenCommands.get(2));
			else
				System.out.println("Syntax Error Encountered");
			break;
		case "create":
			switch (tokenCommands.get(1)) {
				case "table":
					CreateTable(userCommand);
					break;
				case "index":
					CreateIndex(userCommand);
					break;
			}
			
			break;
		case "insert":
			InsertRecords(userCommand);
			break;
		case "delete":
			DeleteRecords(userCommand);
			break;
		case "update":
			UpdateRecords(userCommand);
			break;
		case "show":
			ShowTables(userCommand);
			break;
		case "help":
			help();
			break;
		case "version":
			ShowVersion();
			break;
		case "exit":
			isExit = true;
			break;
		case "quit":
			isExit = true;
		default:
			System.out.println("Invalid User Command: \"" + userCommand + "\"");
			break;
		}
	}




	private static void CreateIndex(String userCommand) throws Exception {
		ArrayList<String> tokenCommands = new ArrayList<String>(Arrays.asList(userCommand.split(" ")));
		File folder = new File("data/user_data");
		File[] listOfFiles = folder.listFiles();
		boolean flag = false;
		boolean isColumnExists = true;
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].getName().equals(tokenCommands.get(3) + ".tbl")) {
				flag = true;
			}
		}
		if (flag == false) {
			System.out.println("Table doesnt exist");
		}
		if (flag == true) {
			BTree columnBTree = new BTree(davisBaseColumnFile, "davisbase_columns", true, false);
			userCommand = userCommand.replace("(", " ( ");
			userCommand = userCommand.replace(")", " ) ");
			//userCommand = userCommand.replace(",", " , ");
			Map<String, ArrayList<String>> tableInfo = new LinkedHashMap<String, ArrayList<String>>();
			Map<String, ArrayList<String>> tableVal = new LinkedHashMap<String, ArrayList<String>>();
			ArrayList<String> colName = new ArrayList<String>();
			for (String x : tableInfo.keySet())
				if (tableInfo.get(x).contains("no"))
					colName.add(x);
			for (String x : colName) {
				if (tokenCommands.get(5)!=x) {
					System.out.println("Coulmn doesn't exist");
					isColumnExists = false;
				}
			}
			if (isColumnExists == true) {
				System.out.println("Index created for the column");
			}
		}
		/*
		if (!createTableTokens.get(3).equals("(") || !createTableTokens.get(1).equals("table")
				|| !createTableTokens.get(2).matches("^[a-zA-Z][a-zA-Z0-9_]*$")
				|| !createTableTokens.get(4).matches("^[a-zA-Z][a-zA-Z0-9_]*$")
				|| !createTableTokens.get(5).equals("int") || !createTableTokens.get(6).equals("primary")
				|| !createTableTokens.get(7).equals("key"))
			flag = 0;
		if (flag == 1) {
			newTable.put("rowid", new ArrayList<String>(
					Arrays.asList("int", String.valueOf(davisBaseTableTree.GetNextMaxRowID() + 1))));
			newTable.put("table_name", new ArrayList<String>(Arrays.asList("text", createTableTokens.get(2))));
			newColumns.put("rowid", new ArrayList<String>(Arrays.asList("int", String.valueOf(cr++))));
			newColumns.put("table_name", new ArrayList<String>(Arrays.asList("text", createTableTokens.get(2))));
			newColumns.put("column_name", new ArrayList<String>(Arrays.asList("text", createTableTokens.get(4))));
			newColumns.put("data_type", new ArrayList<String>(Arrays.asList("text", "int")));
			newColumns.put("ordinal_position",
					new ArrayList<String>(Arrays.asList("tinyint", String.valueOf(op++))));
			newColumns.put("is_nullable", new ArrayList<String>(Arrays.asList("text", "no")));
			schematableColList.add(new LinkedHashMap<String, ArrayList<String>>(newColumns));
			newColumns.clear();
		}

		// TODO Auto-generated method stub
		*/
	}


	public static void CreateTable(String createTableString) {

		try {

			int flag = 1, op = 1;
			try {
				davisBaseTableFile = new RandomAccessFile(davisbase_tables, "rw");
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
			BTree davisBaseTableTree = new BTree(davisBaseTableFile, "davisbase_tables", false, true);
			BTree davisBaseColumnTree = new BTree(davisBaseColumnFile, "davisbase_columns", true, false);
			int cr = davisBaseColumnTree.GetNextMaxRowID() + 1;
			createTableString = createTableString.replace("(", " ( ");
			createTableString = createTableString.replace(")", " ) ");
			createTableString = createTableString.replace(",", " , ");
			List<LinkedHashMap<String, ArrayList<String>>> schematableColList = new ArrayList<LinkedHashMap<String, ArrayList<String>>>();
			LinkedHashMap<String, ArrayList<String>> newTable = new LinkedHashMap<String, ArrayList<String>>();
			LinkedHashMap<String, ArrayList<String>> newColumns = new LinkedHashMap<String, ArrayList<String>>();
			ArrayList<String> dataType = new ArrayList<String>(Arrays.asList("tinyint", "smallint", "int", "bigint","real", "double", "datetime", "date", "text"));
			ArrayList<String> createTableTokens = new ArrayList<String>(Arrays.asList(createTableString.split("\\s+")));
			if (createTableTokens.get(2).trim().equals("davisbase_columns")) {
				System.out.println("Schema table name is not allowed");
				return;
			} else if (createTableTokens.get(2).trim().equals("davisbase_tables")) {
				System.out.println("Schema table name is not allowed");
				return;
			}

			File folder = new File("data/user_data");
			File[] listOfFiles = folder.listFiles();
			for (int i = 0; i < listOfFiles.length; i++)
				if (listOfFiles[i].getName().equals(createTableTokens.get(2) + ".tbl")) {
					System.out.println("Table already exists!!!");
					flag = 0;
					return;

				}
			if (!createTableTokens.get(3).equals("(") || !createTableTokens.get(1).equals("table")
					|| !createTableTokens.get(2).matches("^[a-zA-Z][a-zA-Z0-9_]*$")
					|| !createTableTokens.get(4).matches("^[a-zA-Z][a-zA-Z0-9_]*$")
					|| !createTableTokens.get(5).equals("int") || !createTableTokens.get(6).equals("primary")
					|| !createTableTokens.get(7).equals("key"))
				flag = 0;
			if (flag == 1) {
				newTable.put("rowid", new ArrayList<String>(
						Arrays.asList("int", String.valueOf(davisBaseTableTree.GetNextMaxRowID() + 1))));
				newTable.put("table_name", new ArrayList<String>(Arrays.asList("text", createTableTokens.get(2))));
				newColumns.put("rowid", new ArrayList<String>(Arrays.asList("int", String.valueOf(cr++))));
				newColumns.put("table_name", new ArrayList<String>(Arrays.asList("text", createTableTokens.get(2))));
				newColumns.put("column_name", new ArrayList<String>(Arrays.asList("text", createTableTokens.get(4))));
				newColumns.put("data_type", new ArrayList<String>(Arrays.asList("text", "int")));
				newColumns.put("ordinal_position",
						new ArrayList<String>(Arrays.asList("tinyint", String.valueOf(op++))));
				newColumns.put("is_nullable", new ArrayList<String>(Arrays.asList("text", "no")));
				schematableColList.add(new LinkedHashMap<String, ArrayList<String>>(newColumns));
				newColumns.clear();
			}
			if (createTableTokens.get(8).equals(","))
				for (int i = 9; i < createTableTokens.size() && flag == 1; i++) {
					newColumns.clear();
					if (createTableTokens.get(i).matches("^[a-zA-Z][a-zA-Z0-9_]*$")
							&& dataType.contains(createTableTokens.get(i + 1))) {
						newColumns.put("rowid", new ArrayList<String>(Arrays.asList("int", String.valueOf(cr++))));
						newColumns.put("table_name",
								new ArrayList<String>(Arrays.asList("text", createTableTokens.get(2))));
						newColumns.put("column_name",
								new ArrayList<String>(Arrays.asList("text", createTableTokens.get(i))));
						newColumns.put("data_type",
								new ArrayList<String>(Arrays.asList("text", createTableTokens.get(i + 1))));
						newColumns.put("ordinal_position",
								new ArrayList<String>(Arrays.asList("tinyint", String.valueOf(op++))));
						i++;
						if (createTableTokens.get(i + 1).equals("not") && createTableTokens.get(i + 2).equals("null")
								&& createTableTokens.get(i + 3).equals(",")) {
							i = i + 3;
							newColumns.put("is_nullable", new ArrayList<String>(Arrays.asList("text", "no")));
							schematableColList.add(new LinkedHashMap<String, ArrayList<String>>(newColumns));
						} else if (createTableTokens.get(i + 1).equals("not")
								&& createTableTokens.get(i + 2).equals("null")
								&& createTableTokens.get(i + 3).equals(")")) {
							i = i + 3;
							newColumns.put("is_nullable", new ArrayList<String>(Arrays.asList("text", "no")));
							schematableColList.add(new LinkedHashMap<String, ArrayList<String>>(newColumns));
						} else if (createTableTokens.get(i + 1).equals(",")) {
							i++;
							newColumns.put("is_nullable", new ArrayList<String>(Arrays.asList("text", "yes")));
							schematableColList.add(new LinkedHashMap<String, ArrayList<String>>(newColumns));
						} else if (createTableTokens.get(i + 1).equals(")")) {
							newColumns.put("is_nullable", new ArrayList<String>(Arrays.asList("text", "yes")));
							schematableColList.add(new LinkedHashMap<String, ArrayList<String>>(newColumns));
							i++;
						} else
							flag = 0;
					} else
						flag = 0;
				}
			if (flag == 0)
				System.out.println("Syntax Error Encountered");
			else {
				try {

					BTree columnBTree = new BTree(davisBaseColumnFile, "davisbase_columns", true, false);
					BTree tableBTree = new BTree(davisBaseTableFile, "davisbase_tables", false, true);
					for (LinkedHashMap<String, ArrayList<String>> rows : schematableColList) {
						columnBTree.InsertNewRecord(rows);
					}
					tableBTree.InsertNewRecord(newTable);
					RandomAccessFile tableFile = new RandomAccessFile(
							"data/user_data/" + createTableTokens.get(2) + ".tbl", "rw");
					tableFile.setLength(0);

					new BTree(tableFile, createTableTokens.get(2)).CreateEmptyTable();
					if (tableBTree != null)
						tableFile.close();
					System.out.println("Table Created");
				} catch (Exception e) {
					System.out.println(" ");
				}

			}
		} catch (Exception e) {
			System.out.println("Syntax Error Encountered");
		}
	}
	

	public static void ShowTables(String showTableString) {
		try {
			ArrayList<String> showTableTokens = new ArrayList<String>(Arrays.asList(showTableString.split("\\s+")));
			if (showTableTokens.get(1).equals("tables"))
				ShowAllTables();
			else
				System.out.println("Syntax Error Encountered");
		} catch (Exception e) {
			System.out.println("Syntax Error Encountered");
		}
	}
	
	public static boolean DropIndex(String userCommand) {
		ArrayList<String> tokenCommands = new ArrayList<String>(Arrays.asList(userCommand.split(" ")));
		File folder = new File("data/user_data");
		File[] listOfFiles = folder.listFiles();
		boolean flag = false;
		boolean isIndexDropped = true;
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].getName().equals(tokenCommands.get(3) + ".tbl")) {
				flag = true;
			}
		}
		if (flag == false) {
			System.out.println("Table doesnt exist");
		}
		if (flag == true) {
			BTree columnBTree = new BTree(davisBaseColumnFile, "davisbase_columns", true, false);
			userCommand = userCommand.replace("(", " ( ");
			userCommand = userCommand.replace(")", " ) ");
			//userCommand = userCommand.replace(",", " , ");
			Map<String, ArrayList<String>> tableInfo = new LinkedHashMap<String, ArrayList<String>>();
			Map<String, ArrayList<String>> tableVal = new LinkedHashMap<String, ArrayList<String>>();
			ArrayList<String> colName = new ArrayList<String>();
			for (String x : tableInfo.keySet())
				if (tableInfo.get(x).contains("no"))
					colName.add(x);
			for (String x : colName) {
				if (tokenCommands.get(5)!=x) {
					System.out.println("Coulmn doesn't exist");
					isIndexDropped = false;
				}
			}
			if (isIndexDropped == true) {
				System.out.println("Index dropped for the table");
			}
		}
		return isIndexDropped;
		
		
		// TODO Auto-generated method stub
		
	}
	
	public static boolean DropTable(String tableName) {
		boolean isDropped = false;
		int flag = 1;
		RandomAccessFile dropTableFile = null;
		File folder = new File("data/user_data");
		File[] listOfFiles = folder.listFiles();
		for (int i = 0; i < listOfFiles.length; i++)
			if (listOfFiles[i].getName().equals(tableName + ".tbl")) {
				flag = 0;
			}
		if (flag == 1) {
			System.out.println("Table does not exist");
			return false;
		}
		try {
			davisBaseColumnFile = new RandomAccessFile(davisbase_columns, "rw");
			davisBaseTableFile = new RandomAccessFile(davisbase_tables, "rw");
			dropTableFile = new RandomAccessFile(table_Location + tableName + table_Format, "rw");
		} catch (FileNotFoundException e1) {
			System.out.println(" Table file not found");
			return false;
		}
		try {
			dropTableFile.setLength(0);
			if (dropTableFile != null)
				dropTableFile.close();
			File f = new File(table_Location + tableName + table_Format);
			f.delete();
			System.out.println("Table Dropped");
		} catch (IOException e) {
			return false;
		}
		BTree tableBTree = new BTree(davisBaseTableFile, "davisbase_tables", false, true);
		BTree columnBTree = new BTree(davisBaseColumnFile, "davisbase_columns", true, false);
		ArrayList<String> arryL = new ArrayList<String>();
		arryL.add(new Integer(1).toString()); 
		arryL.add("text"); 
		arryL.add(tableName); 
		List<LinkedHashMap<String, ArrayList<String>>> op = tableBTree.SearchNonPrimaryCol(arryL);
		for (LinkedHashMap<String, ArrayList<String>> map : op) {
			Integer rowId = Integer.parseInt(map.get("rowid").get(0));
			LinkedHashMap<String, ArrayList<String>> token = new LinkedHashMap<String, ArrayList<String>>();
			ArrayList<String> arrayValues = new ArrayList<String>();
			arrayValues.add("int");
			arrayValues.add(rowId.toString());
			token.put("rowid", new ArrayList<String>(arrayValues));
			isDropped = tableBTree.DeleteRecord(token);

		}
		arryL = new ArrayList<String>();
		arryL.add(new Integer(1).toString()); 
		arryL.add("text"); 
		arryL.add(tableName); 
		List<LinkedHashMap<String, ArrayList<String>>> opp = columnBTree.SearchNonPrimaryCol(arryL);
		for (LinkedHashMap<String, ArrayList<String>> map : opp) {
			Integer rowId = Integer.parseInt(map.get("rowid").get(0));
			LinkedHashMap<String, ArrayList<String>> token = new LinkedHashMap<String, ArrayList<String>>();
			ArrayList<String> arrayValues = new ArrayList<String>();
			arrayValues.add("int");
			arrayValues.add(rowId.toString());
			token.put("rowid", new ArrayList<String>(arrayValues));
			isDropped = columnBTree.DeleteRecord(token);
		}
		return isDropped;

	}

	public static void InsertRecords(String insertTableString) {
		try {
			BTree columnBTree = new BTree(davisBaseColumnFile, "davisbase_columns", true, false);
			insertTableString = insertTableString.replace("(", " ( ");
			insertTableString = insertTableString.replace(")", " ) ");
			insertTableString = insertTableString.replace(",", " , ");
			Map<String, ArrayList<String>> tableInfo = new LinkedHashMap<String, ArrayList<String>>();
			Map<String, ArrayList<String>> tableVal = new LinkedHashMap<String, ArrayList<String>>();
			ArrayList<String> colName = new ArrayList<String>();
			ArrayList<String> insertTableTokens = new ArrayList<String>(Arrays.asList(insertTableString.split("\\s+")));
			int flag = 1;
			File folder = new File("data/user_data");
			File[] listOfFiles = folder.listFiles();
			for (int i = 0; i < listOfFiles.length; i++)
				if (listOfFiles[i].getName().equals(insertTableTokens.get(2) + ".tbl"))
					flag = 0;
			if (flag == 1 || !insertTableTokens.get(1).equals("into"))
				System.out.println("Table does not exist/Syntax Error");
			else
				tableInfo = columnBTree.getSchema(insertTableTokens.get(2));
			if (insertTableTokens.get(3).equals("(")) {
				for (String x : tableInfo.keySet())
					if (tableInfo.get(x).contains("no"))
						colName.add(x);
				int k = insertTableTokens.indexOf(")");
				if (insertTableTokens.get(k + 1).equals("values") && insertTableTokens.get(k + 2).equals("(")
						&& insertTableTokens.get(insertTableTokens.size() - 1).equals(")")) {
					for (int i = 4; !insertTableTokens.get(i).equals(")") && flag == 0; i++) {
						if (colName.contains(insertTableTokens.get(i)))
							colName.remove(insertTableTokens.get(i));
						if (tableInfo.keySet().contains(insertTableTokens.get(i))
								&& (CompareValues(tableInfo.get(insertTableTokens.get(i)).get(0),
										insertTableTokens.get(k + i - 1)))
								&& insertTableTokens.get(i + 1).equals(",")) {
							tableVal.put(insertTableTokens.get(i),
									new ArrayList<String>(Arrays.asList(tableInfo.get(insertTableTokens.get(i)).get(0),
											insertTableTokens.get(k + i - 1))));
							i++;
						} else if (tableInfo.keySet().contains(insertTableTokens.get(i))
								&& (CompareValues(tableInfo.get(insertTableTokens.get(i)).get(0),
										insertTableTokens.get(k + i - 1)))
								&& insertTableTokens.get(i + 1).equals(")"))
							tableVal.put(insertTableTokens.get(i),
									new ArrayList<String>(Arrays.asList(tableInfo.get(insertTableTokens.get(i)).get(0),
											insertTableTokens.get(k + i - 1))));
						else
							flag = 1;
					}
				} else
					flag = 1;
				if (colName.size() != 0)
					flag = 1;
			} else if (insertTableTokens.get(3).equals("values") && insertTableTokens.get(4).equals("(")
					&& insertTableTokens.get(insertTableTokens.size() - 1).equals(")")) {
				int k = 5;
				for (String x : tableInfo.keySet()) {
					if (tableInfo.get(x).get(1).equals("no")) {
						if (CompareValues(tableInfo.get(x).get(0), insertTableTokens.get(k))
								&& insertTableTokens.get(k + 1).equals(",")) {
							tableVal.put(x, new ArrayList<String>(
									Arrays.asList(tableInfo.get(x).get(0), insertTableTokens.get(k))));
							k += 2;
						} else if (CompareValues(tableInfo.get(x).get(0), insertTableTokens.get(k))
								&& insertTableTokens.get(k + 1).equals(")")) {
							tableVal.put(x, new ArrayList<String>(
									Arrays.asList(tableInfo.get(x).get(0), insertTableTokens.get(k))));
							k++;
						} else
							flag = 1;
					} else {
						if (!insertTableTokens.get(k).equals(",") && !insertTableTokens.get(k).equals(")")) {
							if (CompareValues(tableInfo.get(x).get(0), insertTableTokens.get(k))
									&& insertTableTokens.get(k + 1).equals(",")) {
								tableVal.put(x, new ArrayList<String>(
										Arrays.asList(tableInfo.get(x).get(0), insertTableTokens.get(k))));
								k += 2;
							} else if (CompareValues(tableInfo.get(x).get(0), insertTableTokens.get(k))
									&& insertTableTokens.get(k + 1).equals(")")) {
								tableVal.put(x, new ArrayList<String>(
										Arrays.asList(tableInfo.get(x).get(0), insertTableTokens.get(k))));
								k++;
							}
						} else {
							tableVal.put(x, new ArrayList<String>(Arrays.asList(tableInfo.get(x).get(0), "NULL")));
						}

					}
				}
			} else
				flag = 1;
			if (flag == 0) {
				int primaryKeyVal = -1;
				for (String key : tableVal.keySet()) {
					String primaryKey = tableVal.get(key).get(1);
					primaryKeyVal = Integer.parseInt(primaryKey);
					break;
				}
				for (String key : tableVal.keySet()) {
					tableInfo.put(key, tableVal.get(key));
				}
				String fileName = table_Location + insertTableTokens.get(2) + table_Format;
				try {
					RandomAccessFile newTable = new RandomAccessFile(fileName, "rw");
					BTree tableTree = new BTree(newTable, insertTableTokens.get(2));

					if (tableTree.isTableEmpty()) {
						tableTree.CreateNewTableLeaf(tableInfo);

					} else {
						if (tableTree.IsExistsPrimaryKey(primaryKeyVal)) {
							System.out.println(" Primary key with value " + primaryKeyVal + " already exists");
							return;
						} else {
							for (String x : tableInfo.keySet())
								if ((tableInfo.get(x).get(1).charAt(0) == '\''
										&& tableInfo.get(x).get(1).charAt(tableInfo.get(x).get(1).length() - 1) == '\'')
										|| (tableInfo.get(x).get(1).charAt(0) == '"' && tableInfo.get(x).get(1)
												.charAt(tableInfo.get(x).get(1).length() - 1) == '"'))
									tableInfo.put(x, new ArrayList<String>(Arrays.asList("text", tableInfo.get(x).get(1)
											.substring(1, tableInfo.get(x).get(1).length() - 1))));
							tableTree.InsertNewRecord(tableInfo);
							if (newTable != null)
								newTable.close();

						}
					}

				} catch (Exception e) {
					System.out.println(" ");
				}

				System.out.println("1 row inserted");

			} else
				System.out.println("Error in syntax");
		} catch (Exception e) {
			System.out.println("Syntax Error Encountered");
		}
	}

	public static void UpdateRecords(String updateTableString) {
		try {
			updateTableString = updateTableString.replace("=", " = ");
			BTree columnBTree = new BTree(davisBaseColumnFile, "davisbase_columns", true, false);
			ArrayList<String> updateTableTokens = new ArrayList<String>(Arrays.asList(updateTableString.split("\\s+")));
			Map<String, ArrayList<String>> tableInfo = new LinkedHashMap<String, ArrayList<String>>();
			int flag = 1;
			if (updateTableTokens.get(2).equals("set") && updateTableTokens.get(4).equals("=")) {
				if (updateTableTokens.size() > 6 && updateTableTokens.get(6).equals("where")
						&& updateTableTokens.get(8).equals("=")) {
					tableInfo = columnBTree.getSchema(updateTableTokens.get(1));
					if (tableInfo != null) {
						if (tableInfo.keySet().contains(updateTableTokens.get(3))
								&& tableInfo.keySet().contains(updateTableTokens.get(7))) {
							if (CompareValues(tableInfo.get(updateTableTokens.get(3)).get(0), updateTableTokens.get(5))
									&& CompareValues(tableInfo.get(updateTableTokens.get(7)).get(0),
											updateTableTokens.get(9))) {
								ArrayList arrayValues = new ArrayList<String>();
								LinkedHashMap<String, ArrayList<String>> token = new LinkedHashMap<String, ArrayList<String>>();

								BTree tableTree = null;
								RandomAccessFile newTable = null;
								try {
									newTable = new RandomAccessFile(
											table_Location + updateTableTokens.get(1) + table_Format, "rw");
									tableTree = new BTree(newTable, updateTableTokens.get(1));
								} catch (FileNotFoundException e1) {
									System.out.println(" Table not found during update");
									return;
								}
								String dataType = tableInfo.get(updateTableTokens.get(7)).get(0);
								arrayValues.add(dataType);
								if ((updateTableTokens.get(9).charAt(0) == '\'' && updateTableTokens.get(9)
										.charAt(updateTableTokens.get(9).length() - 1) == '\'')
										|| (updateTableTokens.get(9).charAt(0) == '"' && updateTableTokens.get(9)
												.charAt(updateTableTokens.get(9).length() - 1) == '"'))
									arrayValues.add(updateTableTokens.get(9).substring(1,
											updateTableTokens.get(9).length() - 1));
								else
									arrayValues.add(new String(updateTableTokens.get(9)));

								token.put(updateTableTokens.get(7), new ArrayList<String>(arrayValues));

								LinkedHashMap<String, ArrayList<String>> result = tableTree.SearchWithPrimaryKey(token);
								if (result == null) {
									return;
								}
								LinkedHashMap<String, ArrayList<String>> table = columnBTree
										.getSchema(updateTableTokens.get(1));
								for (String column : result.keySet()) {
									if (column.equals(updateTableTokens.get(3)))
									{
										ArrayList<String> value = table.get(column);
										value.remove(value.size() - 1);
										if ((updateTableTokens.get(5).charAt(0) == '\'' && updateTableTokens.get(5)
												.charAt(updateTableTokens.get(5).length() - 1) == '\'')
												|| (updateTableTokens.get(5).charAt(0) == '"' && updateTableTokens
														.get(5).charAt(updateTableTokens.get(5).length() - 1) == '"'))
											value.add(updateTableTokens.get(5).substring(1,
													updateTableTokens.get(5).length() - 1)); 
										else
											value.add(updateTableTokens.get(5));
										table.put(column, value);
									} else {
										ArrayList<String> value = table.get(column);
										ArrayList<String> res = result.get(column);
										value.remove(value.size() - 1);
										String val = res.get(0);
										value.add(val);
										table.put(column, value);
									}

								}
								token.clear();
								arrayValues.clear();
								dataType = tableInfo.get(updateTableTokens.get(7)).get(0);
								arrayValues.add(dataType);
								arrayValues.add(new String(updateTableTokens.get(9)));
								token.put(updateTableTokens.get(7), new ArrayList<String>(arrayValues));
								tableTree.DeleteRecord(token);
								try {
									tableTree.InsertNewRecord(table);
									System.out.println(" 1 row updated");
									if (newTable != null)
										newTable.close();
								} catch (Exception e) {

									System.out.println(" Update Failed");
								}

							} else
								flag = 0;
						} else
							flag = 0;
					} else
						flag = 0;
				} else {
					tableInfo = columnBTree.getSchema(updateTableTokens.get(1));
					if (tableInfo != null) {
						if (tableInfo.keySet().contains(updateTableTokens.get(3))) {
							if (CompareValues(tableInfo.get(updateTableTokens.get(3)).get(0), updateTableTokens.get(5))) {

								int noOfRows = 0;
								BTree tableTree = null;
								RandomAccessFile newTable = null;
								try {
									newTable = new RandomAccessFile(
											table_Location + updateTableTokens.get(1) + table_Format, "rw");
									tableTree = new BTree(newTable, updateTableTokens.get(1));
								} catch (FileNotFoundException e1) {
									System.out.println(" Table not found during update");
									return;
								}

								List<LinkedHashMap<String, ArrayList<String>>> list_result = tableTree.PrintAll();
								for (LinkedHashMap<String, ArrayList<String>> result : list_result) {
									LinkedHashMap<String, ArrayList<String>> table = columnBTree
											.getSchema(updateTableTokens.get(1));
									LinkedHashMap<String, ArrayList<String>> token = new LinkedHashMap<String, ArrayList<String>>();
									ArrayList<String> arrayValues = new ArrayList<String>();

									for (String column : result.keySet()) {
										if (column.equals(updateTableTokens.get(3)))
										{
											ArrayList<String> value = table.get(column);
											value.remove(value.size() - 1);
											if ((updateTableTokens.get(5).charAt(0) == '\'' && updateTableTokens.get(5)
													.charAt(updateTableTokens.get(5).length() - 1) == '\'')
													|| (updateTableTokens.get(5).charAt(0) == '"'
															&& updateTableTokens.get(5).charAt(
																	updateTableTokens.get(5).length() - 1) == '"'))
												value.add(updateTableTokens.get(5).substring(1,
														updateTableTokens.get(5).length() - 1)); 
											else
												value.add(updateTableTokens.get(5));
											table.put(column, value);
										} else {
											ArrayList<String> value = table.get(column);
											ArrayList<String> res = result.get(column);
											value.remove(value.size() - 1);
											String val = res.get(0);
											value.add(val);
											table.put(column, value);
										}

									}
									token.clear();
									arrayValues.clear();

									arrayValues.add("int");
									arrayValues.add(new String(table.get(tableTree.get_PrimaryKey()).get(1)));
									token.put(tableTree.get_PrimaryKey(), new ArrayList<String>(arrayValues));
									tableTree.DeleteRecord(token);
									try {
										tableTree.InsertNewRecord(table);
										noOfRows++;
									} catch (Exception e) {

										System.out.println(" Update Failed");
									}

								}
								if (newTable != null)
									try {
										newTable.close();
									} catch (IOException e) {
										System.out.println(" ");
									}

								System.out.println(noOfRows + " row(s) updated.");

							} else
								flag = 0;
						} else
							flag = 0;
					} else
						flag = 0;
				}
			} else
				flag = 0;
			if (flag == 0)
				System.out.println("Syntax Error Encountered");
		} catch (Exception e) {
			System.out.println("Syntax Error Encountered");
		}
	}

	public static void DeleteRecords(String deleteTableString) {
		try {
			BTree columnBTree = new BTree(davisBaseColumnFile, "davisbase_columns", true, false);
			deleteTableString = deleteTableString.replace("=", " = ");
			ArrayList<String> deleteTableTokens = new ArrayList<String>(Arrays.asList(deleteTableString.split("\\s+")));
			Map<String, ArrayList<String>> tableInfo = new LinkedHashMap<String, ArrayList<String>>();
			int flag = 1;
			if (deleteTableTokens.get(1).equals("from")) {
				if (deleteTableTokens.size() > 3 && deleteTableTokens.get(3).equals("where")
						&& deleteTableTokens.get(5).equals("=")) {
					tableInfo = columnBTree.getSchema(deleteTableTokens.get(2));
					if (tableInfo != null) {
						for (String x : tableInfo.keySet()) {
							if (!deleteTableTokens.get(4).equals(x))
								flag = 0;
							break;
						}
						String dataTypeOfDeleteKey = "int";
						if (tableInfo != null) {
							for (String x : tableInfo.keySet()) {
								if (deleteTableTokens.get(4).equals(x)) {
									dataTypeOfDeleteKey = tableInfo.get(x).get(0);
								}

								break;
							}
						}

						if (flag == 1) {
							LinkedHashMap<String, ArrayList<String>> token = new LinkedHashMap<String, ArrayList<String>>();
							ArrayList<String> arrayValues = new ArrayList<String>();
							arrayValues.add(dataTypeOfDeleteKey);
							if ((deleteTableTokens.get(6).charAt(0) == '\''
									&& deleteTableTokens.get(6).charAt(deleteTableTokens.get(6).length() - 1) == '\'')
									|| (deleteTableTokens.get(6).charAt(0) == '"' && deleteTableTokens.get(6)
											.charAt(deleteTableTokens.get(6).length() - 1) == '"'))
								arrayValues.add(deleteTableTokens.get(6).substring(1, deleteTableTokens.get(6).length() - 1));
							else
								arrayValues.add(deleteTableTokens.get(6));
							token.put(deleteTableTokens.get(4), new ArrayList<String>(arrayValues));
							RandomAccessFile filename;
							try {
								filename = new RandomAccessFile(table_Location + deleteTableTokens.get(2) + table_Format,
										"rw");

								BTree davisBaseColumnFileTree = new BTree(filename, deleteTableTokens.get(2));
								davisBaseColumnFileTree.DeleteRecord(token);
								System.out.println("1 row deleted");

								if (filename != null)
									filename.close();
							} catch (Exception e) {
								System.out.println("Table does not exists");
							}
						}
					} else
						System.out.println("Table does not exist!!!");
				} else {
					tableInfo = columnBTree.getSchema(deleteTableTokens.get(2));
					if (tableInfo != null)
						System.out.println("Schema doesn't exists");
					else
						System.out.println("Table does not exist!!!");
				}
			}
		} catch (Exception e) {
			System.out.println("Syntax Error Encountered");
		}
	}

	
	public static void ParseQuery(String queryString) {
		try {
			queryString = queryString.replace("*", " * ");
			queryString = queryString.replace("=", " = ");
			queryString = queryString.replace(",", " , ");
			ArrayList<String> queryTokens = new ArrayList<String>(Arrays.asList(queryString.split("\\s+")));
			String tableName = "";
			ArrayList<String> cols = new ArrayList<String>();
			Map<String, ArrayList<String>> tableInfo = new LinkedHashMap<String, ArrayList<String>>();
			RandomAccessFile newTable = null;
			try {
				davisBaseColumnFile = new RandomAccessFile(davisbase_columns, "rw");
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
			BTree columnBTree = new BTree(davisBaseColumnFile, "davisbase_columns", true, false);
			int flag = 1;
			if (queryTokens.get(1).equals("*") && queryTokens.get(2).equals("from") && queryTokens.size() == 4) {
				tableName = queryTokens.get(3);
				tableInfo = columnBTree.getSchema(queryTokens.get(3));
				if (tableInfo != null) {
					BTree tableBTree;
					try {
						if (tableName.trim().equals("davisbase_tables")) {
							tableBTree = new BTree(davisBaseTableFile, tableName, false, true);

						} else if (tableName.trim().equals("davisbase_columns")) {
							tableBTree = columnBTree;
						} else {
							newTable = new RandomAccessFile(table_Location + tableName + table_Format, "rw");
							tableBTree = new BTree(newTable, tableName);
						}

						PrintTable(tableBTree.PrintAll());
						try {
							if (newTable != null)
								newTable.close();
						} catch (IOException e) {
							System.out.println(" ");
						}

					}

					catch (FileNotFoundException e) {
						System.out.println("Error while reading table, Corresponding table.tbl not found.");
					}
				}
			} else if (queryTokens.get(1).equals("*") && queryTokens.get(2).equals("from")
					&& queryTokens.get(4).equals("where") && queryTokens.get(6).equals("=")) {
				tableName = queryTokens.get(3);
				tableInfo = columnBTree.getSchema(queryTokens.get(3));
				if (tableInfo != null && tableInfo.keySet().contains(queryTokens.get(5))) {
					BTree tableBTree;
					try {

						if (tableName.trim().equals("davisbase_tables")) {
							tableBTree = new BTree(davisBaseTableFile, tableName, false, true);

						} else if (tableName.trim().equals("davisbase_columns")) {
							tableBTree = new BTree(new RandomAccessFile(davisbase_columns, "rw"), tableName, true,
									false);

						} else {
							newTable = new RandomAccessFile(table_Location + tableName + table_Format, "rw");
							tableBTree = new BTree(newTable, tableName);
						}

					} catch (FileNotFoundException e) {
						System.out.println("Error while reading table, Corresponding table.tbl not found.");
						return;
					}
					ArrayList<String> arryL = new ArrayList<String>();
					Integer ordinalPos = 0;
					String dataType = "";
					for (String key : tableInfo.keySet()) {
						if (key.equals(queryTokens.get(5))) {
							dataType = tableInfo.get(key).get(0);
							break;
						}
						ordinalPos++;
					}
					arryL.add(ordinalPos.toString()); 
					arryL.add(dataType); 
					if ((queryTokens.get(7).charAt(0) == '\''
							&& queryTokens.get(7).charAt(queryTokens.get(7).length() - 1) == '\'')
							|| (queryTokens.get(7).charAt(0) == '"'
									&& queryTokens.get(7).charAt(queryTokens.get(7).length() - 1) == '"'))
						arryL.add(queryTokens.get(7).substring(1, queryTokens.get(7).length() - 1));
					else
						arryL.add(queryTokens.get(7));

					if (ordinalPos == 0) {
						LinkedHashMap<String, ArrayList<String>> token = new LinkedHashMap<String, ArrayList<String>>();
						ArrayList<String> arrayValues = new ArrayList<String>();
						arrayValues.add(dataType);
						if ((queryTokens.get(7).charAt(0) == '\''
								&& queryTokens.get(7).charAt(queryTokens.get(7).length() - 1) == '\'')
								|| (queryTokens.get(7).charAt(0) == '"'
										&& queryTokens.get(7).charAt(queryTokens.get(7).length() - 1) == '"'))
							arrayValues.add(queryTokens.get(7).substring(1, queryTokens.get(7).length() - 1));
						else
							arrayValues.add(queryTokens.get(7));

						token.put(queryTokens.get(5), new ArrayList<String>(arrayValues));
						LinkedHashMap<String, ArrayList<String>> op = tableBTree.SearchWithPrimaryKey(token);
						List<LinkedHashMap<String, ArrayList<String>>> temp = new ArrayList<LinkedHashMap<String, ArrayList<String>>>();
						temp.add(op);
						PrintTable(temp);

					} else {
						List<LinkedHashMap<String, ArrayList<String>>> op = tableBTree.SearchNonPrimaryCol(arryL);
						PrintTable(op);

					}
					try {
						if (newTable != null)
							newTable.close();
					} catch (IOException e) {
						System.out.println(" ");
					}

				}
			} else {
				tableName = queryTokens.get(queryTokens.indexOf("from") + 1);
				tableInfo = columnBTree.getSchema(queryTokens.get(queryTokens.indexOf("from") + 1));
				if (tableInfo != null) {
					for (int i = 1; i < queryTokens.indexOf("from") && flag == 1; i++) {
						if (tableInfo.keySet().contains(queryTokens.get(i)) && queryTokens.get(i + 1).equals(",")) {
							cols.add(queryTokens.get(i));
							i++;
						} else if (tableInfo.keySet().contains(queryTokens.get(i))
								&& queryTokens.get(i + 1).equals("from"))
							cols.add(queryTokens.get(i));
						else
							flag = 0;
					}

					if (flag == 1) 
					{
						ArrayList<String> removeColumn = new ArrayList<String>(tableInfo.keySet());
						removeColumn.removeAll(cols);
						BTree tableBTree;
						try {

							if (tableName.trim().equals("davisbase_tables")) {
								tableBTree = new BTree(davisBaseTableFile, tableName, false, true);

							} else if (tableName.trim().equals("davisbase_columns")) {
								tableBTree = new BTree(new RandomAccessFile(davisbase_columns, "rw"), tableName, true,
										false);

							} else {
								newTable = new RandomAccessFile(table_Location + tableName + table_Format, "rw");
								tableBTree = new BTree(newTable, tableName);
							}

							List<LinkedHashMap<String, ArrayList<String>>> op = tableBTree.PrintAll();
							for (LinkedHashMap<String, ArrayList<String>> map : op) {

								for (String x : removeColumn) {
									map.remove(x);
								}

							}
							if (!queryTokens.contains("where"))
							{
								PrintTable(op);
							}
							if (newTable != null)
								newTable.close();
						}

						catch (Exception e) {
							System.out.println("Error while reading table, Corresponding table.tbl not found.");
						}

					}
					if (queryTokens.size() > queryTokens.indexOf("from") + 2
							&& queryTokens.get(queryTokens.indexOf("from") + 2).equals("where")
							&& queryTokens.get(queryTokens.indexOf("from") + 4).equals("=")
							&& tableInfo.keySet().contains(queryTokens.get(queryTokens.indexOf("from") + 3))) {
						ArrayList<String> removeColumn = new ArrayList<String>(tableInfo.keySet());
						removeColumn.removeAll(cols);
						BTree tableBTree;
						try {

							if (tableName.trim().equals("davisbase_tables")) {
								tableBTree = new BTree(davisBaseTableFile, tableName, false, true);

							} else if (tableName.trim().equals("davisbase_columns")) {
								tableBTree = new BTree(new RandomAccessFile(davisbase_columns, "rw"), tableName, true,
										false);

							} else {
								newTable = new RandomAccessFile(table_Location + tableName + table_Format, "rw");
								tableBTree = new BTree(newTable, tableName);
							}

						} catch (FileNotFoundException e) {
							System.out.println("Error while reading table, Corresponding table.tbl not found.");
							return;
						}
						ArrayList<String> arryL = new ArrayList<String>();
						Integer ordinalPos = 0;
						String dataType = "";
						for (String key : tableInfo.keySet()) {
							if (key.equals(queryTokens.get(queryTokens.size() - 3))) {
								dataType = tableInfo.get(key).get(0);
								break;
							}
							ordinalPos++;
						}
						arryL.add(ordinalPos.toString()); 
						arryL.add(dataType); 
						if ((queryTokens.get(queryTokens.size() - 1).charAt(0) == '\''
								&& queryTokens.get(queryTokens.size() - 1)
										.charAt(queryTokens.get(queryTokens.size() - 1).length() - 1) == '\'')
								|| (queryTokens.get(queryTokens.size() - 1).charAt(0) == '"'
										&& queryTokens.get(queryTokens.size() - 1)
												.charAt(queryTokens.get(queryTokens.size() - 1).length() - 1) == '"'))
							queryTokens.get(queryTokens.size() - 1).substring(1,
									queryTokens.get(queryTokens.size() - 1).length() - 1);
						arryL.add(queryTokens.get(queryTokens.size() - 1)); 
						if (ordinalPos == 0) {
							LinkedHashMap<String, ArrayList<String>> token = new LinkedHashMap<String, ArrayList<String>>();
							ArrayList<String> arrayValues = new ArrayList<String>();
							arrayValues.add(dataType);
							arrayValues.add(queryTokens.get(7));

							token.put(queryTokens.get(5), new ArrayList<String>(arrayValues));
							LinkedHashMap<String, ArrayList<String>> op = tableBTree.SearchWithPrimaryKey(token);
							List<LinkedHashMap<String, ArrayList<String>>> temp = new ArrayList<LinkedHashMap<String, ArrayList<String>>>();
							temp.add(op);

							for (LinkedHashMap<String, ArrayList<String>> map : temp) {

								for (String x : removeColumn) {
									map.remove(x);
								}

							}
							PrintTable(temp);

						} else {
							List<LinkedHashMap<String, ArrayList<String>>> op = tableBTree.SearchNonPrimaryCol(arryL);
							for (LinkedHashMap<String, ArrayList<String>> map : op) {

								for (String x : removeColumn) {
									map.remove(x);
								}

							}
							PrintTable(op);

						}
						try {

							if (newTable != null)
								newTable.close();
						} catch (IOException e) {
							System.out.println(" ");
						}

					}

				}
			}
			if (flag == 0)
				System.out.println("Syntax Error Encountered");
		} catch (Exception e) {
			System.out.println("Syntax Error Encountered");
		}

	}

	
	
	
	
	public static void ShowAllTables() {

		try {
			davisBaseTableFile = new RandomAccessFile(davisbase_tables, "rw");
		} catch (FileNotFoundException e1) {
			System.out.println(" Table file not found");
		}
		BTree tableBTree = new BTree(davisBaseTableFile, "davisbase_tables", false, true);
		PrintTable(tableBTree.PrintAll());
	}

	

}