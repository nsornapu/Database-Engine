package bTreeImplementation;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
@SuppressWarnings("unused")
public class BTree {

	private RandomAccessFile binaryFile;

	private static final int pageSize = 512;

	private String tableName;
	private String tableKey = "rowid";
	
	private int currentPage = 1;
	private int lastPage = 1;
	private long PageHeaderOffsetnumberOfCells = 0;
	private long PageOffsetStartOfCell = 0;
	private long PageHeaderOffsetRightPagePointer = 0;
	private long PageHeaderArrayOffset = 0;
	private long PageHeaderOffset = 0;
	
	private ZoneId zoneId = ZoneId.of("America/Chicago");

	private boolean isLeafPage = false;
	private boolean isColSchema;
	private boolean isTableSchema;

	private ArrayList<Integer> routeOfLeafPage = new ArrayList<>();

	private BTree davisbaseColumnFileTree;

	public BTree(RandomAccessFile file, String tableName) {
		binaryFile = file;
		this.tableName = tableName;
		try {
			if (file.length() > 0) {
				lastPage = (int) (file.length() / 512);
				currentPage = lastPage;
			}

			if (!tableName.equals("davisbase_columns")
					&& !tableName.equals("davisbase_tables")) {
				davisbaseColumnFileTree = new BTree(new RandomAccessFile(
						CommandPromptMain.davisbase_columns, "rw"), "davisbase_columns",
						true, false);

				for (String key : davisbaseColumnFileTree.getSchema(tableName).keySet()) {
					tableKey = key;

					break;
				}
			}
		} catch (Exception e) {
			 System.out.println(" "+e);
		}
	}
	
	public LinkedHashMap<String, ArrayList<String>> getSchema(String tableName) {
		ArrayList<String> array = new ArrayList<String>();
		array.add("1");
		array.add("TEXT");
		array.add(tableName);
		List<LinkedHashMap<String, ArrayList<String>>> output = SearchNonPrimaryCol(array);

		LinkedHashMap<String, ArrayList<String>> finalResult = new LinkedHashMap<String, ArrayList<String>>();

		for (LinkedHashMap<String, ArrayList<String>> map : output) {
			ArrayList<String> val = map.get("column_name");
			String key = val.get(0);

			ArrayList<String> valuee = new ArrayList<String>();

			ArrayList<String> dataTypeList = map.get("dataType");
			String dataType = dataTypeList.get(0);
			valuee.add(dataType);

			ArrayList<String> nullStringList = map.get("is_nullable");
			String isNull = nullStringList.get(0);
			if (isNull.equalsIgnoreCase("yes"))
				valuee.add("NULL");
			else
				valuee.addAll(nullStringList);

			finalResult.put(key, valuee);
		}

		return finalResult;

	}
	
	public List<LinkedHashMap<String, ArrayList<String>>> SearchNonPrimaryCol(
			ArrayList<String> value) {
		currentPage = 1;
		List<LinkedHashMap<String, ArrayList<String>>> result = new ArrayList<LinkedHashMap<String, ArrayList<String>>>();
		SearchLeftMostLeafNode();
		while (currentPage > 0) {
			try {
				ReadPageHeader(currentPage);
				SearchRecordsCurrentPage(value, result);

				binaryFile.seek(PageHeaderOffsetRightPagePointer);

				currentPage = binaryFile.readInt();

			} catch (Exception e) {
				 System.out.println(" ");
			}
		}

		return result;

	}
	
	private void SearchLeftMostLeafNode() {

		 

		routeOfLeafPage.add(currentPage);
		ReadPageHeader(currentPage);
		if (isLeafPage) {

			routeOfLeafPage.remove(routeOfLeafPage.size() - 1);
			return;
		} else {
			try {
				binaryFile.seek(PageHeaderOffsetnumberOfCells);

				int numberOfColumns = binaryFile.readUnsignedByte();

				binaryFile.seek(PageHeaderArrayOffset);
				int address;
				if (numberOfColumns > 0) {

					address = binaryFile.readUnsignedShort();

					binaryFile.seek(address);
					int pageNumber = binaryFile.readInt();

					currentPage = pageNumber;
					SearchLeftMostLeafNode();

				}
			} catch (IOException e) {
				 
				 System.out.println(" ");
			}
		}

	}
	
	private void ReadPageHeader(int pageLocation) {
		try {

			binaryFile.seek((currentPage * pageSize) - pageSize);

			int flag = binaryFile.readUnsignedByte();

			if (flag == 13)
				isLeafPage = true;
			else
				isLeafPage = false;

			PageHeaderOffsetnumberOfCells = ((currentPage * pageSize) - pageSize) + 1;
			int noOfCells = binaryFile.readUnsignedByte();
			PageOffsetStartOfCell = binaryFile.getFilePointer();
			binaryFile.readUnsignedShort();
			PageHeaderOffsetRightPagePointer = binaryFile.getFilePointer();
			binaryFile.readInt();
			PageHeaderArrayOffset = binaryFile.getFilePointer();
			PageHeaderOffset = binaryFile.getFilePointer() + (2 * noOfCells);

		} catch (Exception e) {
			 System.out.println(" ");

		}

	}
	
	private void SearchRecordsCurrentPage(ArrayList<String> search_Cond,
			List<LinkedHashMap<String, ArrayList<String>>> result)
					throws Exception {
		binaryFile.seek(PageHeaderOffsetnumberOfCells);
		int noOfCol = binaryFile.readUnsignedByte();

		binaryFile.seek(PageHeaderArrayOffset);
		long point = binaryFile.getFilePointer();
		int address = binaryFile.readUnsignedShort();

		for (int i = 0; i < noOfCol; i++) {

			binaryFile.seek(address);

			binaryFile.readUnsignedShort();
			int currentRowID = binaryFile.readInt();
			LinkedHashMap<String, ArrayList<String>> tokenValue = null;
			if (isColSchema) {
				tokenValue = new LinkedHashMap<String, ArrayList<String>>();
				tokenValue.put("rowid", null);
				tokenValue.put("table_name", null);
				tokenValue.put("column_name", null);
				tokenValue.put("dataType", null);
				tokenValue.put("ordinal_position", null);
				tokenValue.put("is_nullable", null);
			} else if (isTableSchema) {
				tokenValue = new LinkedHashMap<String, ArrayList<String>>();
				tokenValue.put("rowid", null);
				tokenValue.put("table_name", null);

			} else {
				tokenValue = davisbaseColumnFileTree.getSchema(tableName);
			}
			tokenValue = PopulateDataSearch(search_Cond, address, tokenValue);
			if (tokenValue != null)
				result.add(tokenValue);

			point = (point + 2);
			binaryFile.seek(point);
			address = binaryFile.readUnsignedShort();

		}

	}
	
	private LinkedHashMap<String, ArrayList<String>> PopulateDataSearch(
			ArrayList<String> searchCondition, long cellOffset,
			LinkedHashMap<String, ArrayList<String>> tokenValue) {
		 

		ArrayList<String> arrayValues = new ArrayList<String>();
		try {
			binaryFile.seek(cellOffset);
			int payLoadSize = binaryFile.readUnsignedShort();
			Integer actualRowID = binaryFile.readInt();
			short numberOfColumns = binaryFile.readByte();
			payLoadSize -= 1;
			long offsetForSerialType = binaryFile.getFilePointer();
			long offSetForData = (offsetForSerialType + numberOfColumns);

			boolean isMatch = false;
			int i = 0;

			String seachCol = searchCondition.get(0);
			String searchDataType = searchCondition.get(1);
			String serachVal = searchCondition.get(2);

			String value = null;

			long offsetForSerialTypeMatch = offsetForSerialType;
			long offSetForDataMatch = (offSetForData);

			int colIndex = Integer.parseInt(seachCol);

			int currentColIndex = 1;

			for (String key : tokenValue.keySet()) {

				binaryFile.seek(offsetForSerialType);
				short b = binaryFile.readByte();
				offsetForSerialType = binaryFile.getFilePointer();
				if (b == 0) {
					binaryFile.seek(offSetForData);
					int p = (binaryFile.readUnsignedByte());
					value = "NULL";
					offSetForData = binaryFile.getFilePointer();
					tokenValue.put(key, new ArrayList<String>(arrayValues));
				} else if (b == 1) {
					binaryFile.seek(offSetForData);
					int p = (binaryFile.readUnsignedShort());
					value = "NULL";
					offSetForData = binaryFile.getFilePointer();
					tokenValue.put(key, new ArrayList<String>(arrayValues));
				} else if (b == 2) {
					binaryFile.seek(offSetForData);
					int p = (binaryFile.readInt());
					value = "NULL";
					offSetForData = binaryFile.getFilePointer();
					tokenValue.put(key, new ArrayList<String>(arrayValues));
				} else if (b == 3) {
					binaryFile.seek(offSetForData);
					int p = (int) (binaryFile.readDouble());
					value = "NULL";
					offSetForData = binaryFile.getFilePointer();
					tokenValue.put(key, new ArrayList<String>(arrayValues));
				} else if (b == 12) {
					value = "NULL";
				} else if (b == 4) {
					binaryFile.seek(offSetForData);
					value = Integer.toString(binaryFile.readUnsignedByte());
					offSetForData = binaryFile.getFilePointer();
				} else if (b == 5) {
					binaryFile.seek(offSetForData);
					value = (Integer.toString(binaryFile.readUnsignedShort()));
					offSetForData = binaryFile.getFilePointer();
				} else if (b == 6) {
					binaryFile.seek(offSetForData);
					value = (Integer.toString(binaryFile.readInt()));
					offSetForData = binaryFile.getFilePointer();
				} else if (b == 7) {
					binaryFile.seek(offSetForData);
					value = (Long.toString(binaryFile.readLong()));
					offSetForData = binaryFile.getFilePointer();
				} else if (b == 8) {
					binaryFile.seek(offSetForData);
					value = (Float.toString(binaryFile.readFloat()));
					offSetForData = binaryFile.getFilePointer();
				} else if (b == 9) {
					binaryFile.seek(offSetForData);
					value = (Double.toString(binaryFile.readDouble()));
					offSetForData = binaryFile.getFilePointer();
				} else if (b == 10) {
					binaryFile.seek(offSetForData);
					long timeInEpoch = binaryFile.readLong();
					value = Long.toString(timeInEpoch);
					offSetForData = binaryFile.getFilePointer();
				} else if (b == 11) {
					binaryFile.seek(offSetForData);
					long timeInEpoch = binaryFile.readLong();
					value = Long.toString(timeInEpoch);
					offSetForData = binaryFile.getFilePointer();
				} else {
					byte[] text = new byte[b - 12];
					binaryFile.seek(offSetForData);
					binaryFile.read(text);
					value = (new String(text));
					offSetForData = binaryFile.getFilePointer();
				}

				if (currentColIndex == colIndex) {
					switch (searchDataType.trim().toLowerCase()) {
					case "tinyint":
						if (value == null && value == serachVal) {
							isMatch = true;
						} else if (value != null
								&& value.equalsIgnoreCase("null")
								&& value.equalsIgnoreCase(serachVal)) {
							isMatch = true;
						} else if (value != null
								&& serachVal != null
								&& !value.equalsIgnoreCase("null")
								&& Integer.parseInt(serachVal) == Integer
								.parseInt(value)) {
							isMatch = true;
						}
						break;
					case "smallint":
						if (value == null && value == serachVal) {
							isMatch = true;
						} else if (value != null
								&& value.equalsIgnoreCase("null")
								&& value.equalsIgnoreCase(serachVal)) {
							isMatch = true;
						} else if (value != null
								&& serachVal != null
								&& !value.equalsIgnoreCase("null")
								&& Integer.parseInt(serachVal) == Integer
								.parseInt(value)) {
							isMatch = true;
						}
						break;
					case "int":
						if (value == null && value == serachVal) {
							isMatch = true;
						} else if (value != null
								&& value.equalsIgnoreCase("null")
								&& value.equalsIgnoreCase(serachVal)) {
							isMatch = true;
						} else if (value != null
								&& serachVal != null
								&& !value.equalsIgnoreCase("null")
								&& Integer.parseInt(serachVal) == Integer
								.parseInt(value)) {
							isMatch = true;
						}
						break;
					case "bigint":
						if (value == null && value == serachVal) {
							isMatch = true;
						} else if (value != null
								&& value.equalsIgnoreCase("null")
								&& value.equalsIgnoreCase(serachVal)) {
							isMatch = true;
						} else if (value != null
								&& serachVal != null
								&& !value.equalsIgnoreCase("null")
								&& Long.parseLong(serachVal) == Long
								.parseLong(value)) {
							isMatch = true;
						}
						break;
					case "real":
						if (value == null && value == serachVal) {
							isMatch = true;
						} else if (value != null
								&& value.equalsIgnoreCase("null")
								&& value.equalsIgnoreCase(serachVal)) {
							isMatch = true;
						} else if (value != null
								&& serachVal != null
								&& !value.equalsIgnoreCase("null")
								&& Float.parseFloat(serachVal) == Float
								.parseFloat(value)) {
							isMatch = true;
						}
						break;
					case "double":
						if (value == null && value == serachVal) {
							isMatch = true;
						} else if (value != null
								&& value.equalsIgnoreCase("null")
								&& value.equalsIgnoreCase(serachVal)) {
							isMatch = true;
						} else if (value != null
								&& serachVal != null
								&& !value.equalsIgnoreCase("null")
								&& Double.parseDouble(serachVal) == Double
								.parseDouble(value)) {
							isMatch = true;
						}
						break;
					case "datetime":
						long epochSeconds = 0;

						if (value != null && value.equalsIgnoreCase("null")
								&& value.equalsIgnoreCase(serachVal)) {
							isMatch = true;
							break;
						}
						if (value != null && !value.equalsIgnoreCase("null")) {
							SimpleDateFormat df = new SimpleDateFormat(
									"yyyy-MM-dd_HH:mm:ss");
							Date date;
							try {
								date = df.parse(serachVal);
								ZonedDateTime zdt = ZonedDateTime.ofInstant(
										date.toInstant(), zoneId);
								epochSeconds = zdt.toInstant().toEpochMilli() / 1000;
							} catch (Exception e) {
							}
						}
						if (value == null && value == serachVal) {
							isMatch = true;
						} else if (value != null && serachVal != null
								&& !value.equalsIgnoreCase("null")
								&& (epochSeconds) == Long.parseLong(value)) {
							Instant ii = Instant.ofEpochSecond(epochSeconds);
							ZonedDateTime zdt2 = ZonedDateTime.ofInstant(ii,zoneId);
							SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
							Date date = Date.from(zdt2.toInstant());
							value = sdf.format(date);
							isMatch = true;
						}
						break;
					case "date":
						long epochSecondss = 0;
						if (value != null && value.equalsIgnoreCase("null")
								&& value.equalsIgnoreCase(serachVal)) {
							isMatch = true;
							break;
						}

						if (value != null && !value.equalsIgnoreCase("null")) {
							SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

							Date date;
							try {
								date = df.parse(serachVal);
								ZonedDateTime zdt = ZonedDateTime.ofInstant(
										date.toInstant(), zoneId);
								epochSecondss = zdt.toInstant().toEpochMilli() / 1000;
							} catch (Exception e) {
							}
						}

						if (value == null && value == serachVal) {
							isMatch = true;
						} else if (value != null && serachVal != null
								&& !value.equalsIgnoreCase("null")
								&& (epochSecondss) == Long.parseLong(value)) {

							Instant ii = Instant.ofEpochSecond(epochSecondss);
							ZonedDateTime zdt2 = ZonedDateTime.ofInstant(ii,
									zoneId);
							SimpleDateFormat sdf = new SimpleDateFormat(
									"yyyy-MM-dd");
							Date date = Date.from(zdt2.toInstant());
							value = sdf.format(date);
							isMatch = true;
						}
						break;
					case "text":
						if (value == null && value == serachVal) {
							isMatch = true;
						} else if (value != null && serachVal != null
								&& serachVal.equalsIgnoreCase(value)) {
							isMatch = true;
						}

						break;
					}

					break;
				}
				currentColIndex++;
			}

			if (isMatch) {
				offsetForSerialType = offsetForSerialTypeMatch;
				offSetForData = offSetForDataMatch;

				for (String key : tokenValue.keySet()) {

					if (i == 0) {
						arrayValues.add(actualRowID.toString());
						tokenValue.put(key, new ArrayList<String>(arrayValues));
						i++;
						arrayValues.clear();
						continue;
					}

					binaryFile.seek(offsetForSerialType);
					short b = binaryFile.readByte();
					offsetForSerialType = binaryFile.getFilePointer();
					if (b == 0) {
						binaryFile.seek(offSetForData);
						int p = (binaryFile.readUnsignedByte());
						arrayValues.add("NULL");
						offSetForData = binaryFile.getFilePointer();
						tokenValue.put(key, new ArrayList<String>(arrayValues));
					} else if (b == 1) {
						binaryFile.seek(offSetForData);
						int p = (binaryFile.readUnsignedShort());
						arrayValues.add("NULL");
						offSetForData = binaryFile.getFilePointer();
						tokenValue.put(key, new ArrayList<String>(arrayValues));
					} else if (b == 2) {
						binaryFile.seek(offSetForData);
						int p = (binaryFile.readInt());
						arrayValues.add("NULL");
						offSetForData = binaryFile.getFilePointer();
						tokenValue.put(key, new ArrayList<String>(arrayValues));
					} else if (b == 3) {
						binaryFile.seek(offSetForData);
						int p = (int) (binaryFile.readDouble());
						arrayValues.add("NULL");
						offSetForData = binaryFile.getFilePointer();
						tokenValue.put(key, new ArrayList<String>(arrayValues));
					} else if (b == 12) {
						arrayValues.add("NULL");
						tokenValue.put(key, new ArrayList<String>(arrayValues));
					} else if (b == 4) {
						binaryFile.seek(offSetForData);
						arrayValues.add(Integer.toString(binaryFile
								.readUnsignedByte()));
						offSetForData = binaryFile.getFilePointer();
						tokenValue.put(key, new ArrayList<String>(arrayValues));
					} else if (b == 5) {
						binaryFile.seek(offSetForData);
						arrayValues.add(Integer.toString(binaryFile
								.readUnsignedShort()));
						offSetForData = binaryFile.getFilePointer();
						tokenValue.put(key, new ArrayList<String>(arrayValues));
					} else if (b == 6) {
						binaryFile.seek(offSetForData);
						arrayValues
						.add(Integer.toString(binaryFile.readInt()));
						offSetForData = binaryFile.getFilePointer();
						tokenValue.put(key, new ArrayList<String>(arrayValues));
					} else if (b == 7) {
						binaryFile.seek(offSetForData);
						arrayValues.add(Long.toString(binaryFile.readLong()));
						offSetForData = binaryFile.getFilePointer();
						tokenValue.put(key, new ArrayList<String>(arrayValues));
					} else if (b == 8) {
						binaryFile.seek(offSetForData);
						arrayValues
						.add(Float.toString(binaryFile.readFloat()));
						offSetForData = binaryFile.getFilePointer();
						tokenValue.put(key, new ArrayList<String>(arrayValues));
					} else if (b == 9) {
						binaryFile.seek(offSetForData);
						arrayValues.add(Double.toString(binaryFile
								.readDouble()));
						offSetForData = binaryFile.getFilePointer();
						tokenValue.put(key, new ArrayList<String>(arrayValues));
					} else if (b == 10) {
						binaryFile.seek(offSetForData);
						Instant ii = Instant.ofEpochSecond(binaryFile.readLong());
						ZonedDateTime zdt2 = ZonedDateTime.ofInstant(ii, zoneId);
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
						Date date = Date.from(zdt2.toInstant());
						arrayValues.add(sdf.format(date));
						offSetForData = binaryFile.getFilePointer();
						tokenValue.put(key, new ArrayList<String>(arrayValues));
					} else if (b == 11) {
						binaryFile.seek(offSetForData);
						Instant ii = Instant.ofEpochSecond(binaryFile.readLong());
						ZonedDateTime zdt2 = ZonedDateTime.ofInstant(ii, zoneId);
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
						Date date = Date.from(zdt2.toInstant());
						arrayValues.add(sdf.format(date));
						offSetForData = binaryFile.getFilePointer();
						tokenValue.put(key, new ArrayList<String>(arrayValues));
					} else {
						byte[] text = new byte[b - 12];
						binaryFile.seek(offSetForData);
						binaryFile.read(text);
						arrayValues.add(new String(text));
						offSetForData = binaryFile.getFilePointer();
						tokenValue.put(key, new ArrayList<String>(arrayValues));
					}
					arrayValues.clear();
				}
			}
			if (!isMatch)
				tokenValue = null;
		} catch (Exception e) {
			 System.out.println(" ");
		}
		return tokenValue;
	}

	
	public BTree(RandomAccessFile file, String tableName, boolean isColSchema, boolean isTableSchema) {

		this(file, tableName);
		this.isColSchema = isColSchema;
		this.isTableSchema = isTableSchema;

	}

	public boolean isprimaryKey(String keyName) {
		return keyName.equals(tableKey);
	}
	
	public void CreateNewInterior(int pageNumber, int rowID, int pageRight) 
	{
		try {
			binaryFile.seek(0);
			binaryFile.write(5);
			binaryFile.write(1);
			binaryFile.writeShort(pageSize - 8);
			binaryFile.writeInt(pageRight);
			binaryFile.writeShort(pageSize - 8);
			binaryFile.seek(pageSize - 8);
			binaryFile.writeInt(pageNumber);
			binaryFile.writeInt(rowID);

		} catch (IOException e) {
			 System.out.println(" ");
		}
	}

	private void WriteCellInterior(int pageLocation, int pageNumber, int rowID, int pageRight) {
		try {

			binaryFile.seek(pageLocation * pageSize - pageSize + 1);
			short cellCount = binaryFile.readByte();
			if (cellCount < 49) {
				binaryFile.seek(pageLocation * pageSize - pageSize + 4);
				if (binaryFile.readInt() == pageNumber && pageRight != -1) {
					binaryFile.seek(pageLocation * pageSize - pageSize + 4);
					binaryFile.writeInt(pageRight);
					long cellStartOffset = (pageLocation * (pageSize))
							- (8 * (cellCount + 1));
					binaryFile.seek(pageLocation * pageSize - pageSize + 2);
					binaryFile.writeShort((int) cellStartOffset);
					binaryFile.seek(pageLocation * pageSize - pageSize + 1);
					binaryFile.write(cellCount + 1);
					binaryFile.seek(cellStartOffset);
					binaryFile.writeInt(pageNumber);
					binaryFile.writeInt(rowID);
					binaryFile.seek((pageLocation * pageSize - pageSize + 8)
							+ (2 * cellCount));
					binaryFile.writeShort((short) cellStartOffset);
				} else {
					int flag = 0;
					for (int i = 0; i < cellCount; i++) {
						binaryFile
						.seek((pageLocation * pageSize - pageSize + 8)
								+ (2 * i));
						binaryFile.seek(binaryFile.readUnsignedShort());
						if (binaryFile.readInt() == pageNumber) {
							flag = 1;
							int tempRowID = binaryFile.readInt();
							binaryFile
							.seek((pageLocation * pageSize - pageSize + 8)
									+ (2 * i));
							binaryFile.seek(binaryFile.readUnsignedShort() + 4);
							binaryFile.writeInt(rowID);
							long cellStartOffset = (pageLocation * (pageSize))
									- (8 * (cellCount + 1));
							binaryFile.seek(pageLocation * pageSize - pageSize
									+ 2);
							binaryFile.writeShort((int) cellStartOffset);
							binaryFile.seek(pageLocation * pageSize - pageSize
									+ 1);
							binaryFile.write(cellCount + 1);
							binaryFile.seek(cellStartOffset);
							binaryFile.writeInt(pageRight);
							binaryFile.writeInt(tempRowID);
							binaryFile.seek(pageLocation * pageSize - pageSize
									+ 8 + 2 * cellCount);
							binaryFile.writeShort((short) cellStartOffset);
						}
					}
					if (flag == 0) {
						long cellStartOffset = (pageLocation * (pageSize))
								- (8 * (cellCount + 1));
						binaryFile.seek(pageLocation * pageSize - pageSize + 2);
						binaryFile.writeShort((int) cellStartOffset);
						binaryFile.seek(pageLocation * pageSize - pageSize + 1);
						binaryFile.write(cellCount + 1);
						binaryFile.seek(cellStartOffset);
						binaryFile.writeInt(pageNumber);
						binaryFile.writeInt(rowID);
						binaryFile.seek(pageLocation * pageSize - pageSize + 8
								+ 2 * cellCount);
						binaryFile.writeShort((short) cellStartOffset);
					}
				}
				int tempAddi, tempAddj, tempi, tempj;
				for (int i = 0; i <= cellCount; i++)
					for (int j = i + 1; j <= cellCount; j++) {
						binaryFile
						.seek((pageLocation * pageSize - pageSize + 8)
								+ (2 * i));
						tempAddi = binaryFile.readUnsignedShort();
						binaryFile
						.seek((pageLocation * pageSize - pageSize + 8)
								+ (2 * j));
						tempAddj = binaryFile.readUnsignedShort();
						binaryFile.seek(tempAddi + 4);
						tempi = binaryFile.readInt();
						binaryFile.seek(tempAddj + 4);
						tempj = binaryFile.readInt();
						if (tempi > tempj) {
							binaryFile
							.seek((pageLocation * pageSize - pageSize + 8)
									+ (2 * i));
							binaryFile.writeShort(tempAddj);
							binaryFile
							.seek((pageLocation * pageSize - pageSize + 8)
									+ (2 * j));
							binaryFile.writeShort(tempAddi);

						}
					}
			} else {
				binaryFile.seek(pageLocation * pageSize - pageSize + 4);
				if (binaryFile.readInt() == pageNumber && pageRight != -1) {
					binaryFile.seek(pageLocation * pageSize - pageSize + 4);
					binaryFile.writeInt(pageRight);
					long cellStartOffset = (pageLocation * (pageSize))
							- (8 * (cellCount + 1));
					binaryFile.seek(pageLocation * pageSize - pageSize + 2);
					binaryFile.writeShort((int) cellStartOffset);
					binaryFile.seek(pageLocation * pageSize - pageSize + 1);
					binaryFile.write(cellCount + 1);
					binaryFile.seek(cellStartOffset);
					binaryFile.writeInt(pageNumber);
					binaryFile.writeInt(rowID);
					binaryFile.seek((pageLocation * pageSize - pageSize + 8)
							+ (2 * cellCount));
					binaryFile.writeShort((short) cellStartOffset);
				} else {
					int flag = 0;
					for (int i = 0; i < cellCount; i++) {
						binaryFile
						.seek((pageLocation * pageSize - pageSize + 8)
								+ (2 * i));
						binaryFile.seek(binaryFile.readUnsignedShort());
						if (binaryFile.readInt() == pageNumber) {
							flag = 1;
							int tempRowID = binaryFile.readInt();
							binaryFile
							.seek((pageLocation * pageSize - pageSize + 8)
									+ (2 * i));
							binaryFile.seek(binaryFile.readUnsignedShort() + 4);
							binaryFile.writeInt(rowID);
							long cellStartOffset = (pageLocation * (pageSize))
									- (8 * (cellCount + 1));
							binaryFile.seek(pageLocation * pageSize - pageSize
									+ 2);
							binaryFile.writeShort((int) cellStartOffset);
							binaryFile.seek(pageLocation * pageSize - pageSize
									+ 1);
							binaryFile.write(cellCount + 1);
							binaryFile.seek(cellStartOffset);
							binaryFile.writeInt(pageRight);
							binaryFile.writeInt(tempRowID);
							binaryFile.seek(pageLocation * pageSize - pageSize
									+ 8 + 2 * cellCount);
							binaryFile.writeShort((short) cellStartOffset);
						}
					}
					if (flag == 0) {
						long cellStartOffset = (pageLocation * (pageSize))
								- (8 * (cellCount + 1));
						binaryFile.seek(pageLocation * pageSize - pageSize + 2);
						binaryFile.writeShort((int) cellStartOffset);
						binaryFile.seek(pageLocation * pageSize - pageSize + 1);
						binaryFile.write(cellCount + 1);
						binaryFile.seek(cellStartOffset);
						binaryFile.writeInt(pageNumber);
						binaryFile.writeInt(rowID);
						binaryFile.seek(pageLocation * pageSize - pageSize + 8
								+ 2 * cellCount);
						binaryFile.writeShort((short) cellStartOffset);
					}
				}
				int tempAddi, tempAddj, tempi, tempj;
				for (int i = 0; i <= cellCount; i++)
					for (int j = i + 1; j <= cellCount; j++) {
						binaryFile
						.seek((pageLocation * pageSize - pageSize + 8)
								+ (2 * i));
						tempAddi = binaryFile.readUnsignedShort();
						binaryFile
						.seek((pageLocation * pageSize - pageSize + 8)
								+ (2 * j));
						tempAddj = binaryFile.readUnsignedShort();
						binaryFile.seek(tempAddi + 4);
						tempi = binaryFile.readInt();
						binaryFile.seek(tempAddj + 4);
						tempj = binaryFile.readInt();
						if (tempi > tempj) {
							binaryFile
							.seek((pageLocation * pageSize - pageSize + 8)
									+ (2 * i));
							binaryFile.writeShort(tempAddj);
							binaryFile
							.seek((pageLocation * pageSize - pageSize + 8)
									+ (2 * j));
							binaryFile.writeShort(tempAddi);

						}
					}
				if (pageLocation == 1) {
					int x, y;
					binaryFile.seek((pageLocation * pageSize - pageSize + 8)
							+ (2 * 25));
					binaryFile.seek(binaryFile.readUnsignedShort());
					x = binaryFile.readInt();
					y = binaryFile.readInt();
					WritePageHeader(lastPage + 1, false, 0, x);
					for (int i = 0; i < 25; i++) {
						binaryFile
						.seek((pageLocation * pageSize - pageSize + 8)
								+ (2 * i));
						binaryFile.seek(binaryFile.readUnsignedShort());
						WriteCellInterior(lastPage + 1, binaryFile.readInt(),
								binaryFile.readInt(), -1);
					}
					binaryFile.seek(pageLocation * pageSize - pageSize + 4);
					WritePageHeader(lastPage + 2, false, 0,
							binaryFile.readInt());
					for (int i = 26; i < 50; i++) {
						binaryFile
						.seek((pageLocation * pageSize - pageSize + 8)
								+ (2 * i));
						binaryFile.seek(binaryFile.readUnsignedShort());
						WriteCellInterior(lastPage + 2, binaryFile.readInt(),
								binaryFile.readInt(), -1);
					}
					WritePageHeader(1, false, 0, lastPage + 2);

					WriteCellInterior(1, lastPage + 1, y, lastPage + 2);
					lastPage += 2;

				} else {

					int x, y;
					binaryFile.seek((pageLocation * pageSize - pageSize + 8)
							+ (2 * 25));
					binaryFile.seek(binaryFile.readUnsignedShort());
					x = binaryFile.readInt();
					y = binaryFile.readInt();
					binaryFile.seek(pageLocation * pageSize - pageSize + 4);
					WritePageHeader(lastPage + 1, false, 0,
							binaryFile.readInt());
					binaryFile.seek(pageLocation * pageSize - pageSize + 4);
					binaryFile.writeInt(x);
					for (int i = 26; i < 50; i++) {
						binaryFile
						.seek((pageLocation * pageSize - pageSize + 8)
								+ (2 * i));
						binaryFile.seek(binaryFile.readUnsignedShort());
						WriteCellInterior(lastPage + 1, binaryFile.readInt(),
								binaryFile.readInt(), -1);

					}

					binaryFile.seek(pageLocation * pageSize - pageSize + 1);
					binaryFile.write(25);

					int lastInteriorPage = routeOfLeafPage
							.remove(routeOfLeafPage.size() - 1);

					WriteCellInterior(lastInteriorPage, pageLocation, y,
							lastPage + 1);
					lastPage++;

				}
			}

		} catch (IOException e1) {
			 
			e1.printStackTrace();
		}
	}

	public void DeleteCellInterior(int pageLocation, int pageNumber) {
		try {
			binaryFile.seek(pageLocation * pageSize - pageSize + 1);
			short cellCount = binaryFile.readByte();
			int pos = cellCount;
			for (int i = 0; i < cellCount; i++) {
				binaryFile.seek((pageLocation * pageSize - pageSize + 8)
						+ (2 * (i)));
				binaryFile.seek(binaryFile.readUnsignedShort());
				if (pageNumber == binaryFile.readInt()) {
					pos = i;
					binaryFile.seek(pageLocation * pageSize - pageSize + 1);
					binaryFile.write(cellCount - 1);
					break;
				}
			}
			int temp;
			while (pos < cellCount) {
				binaryFile.seek((pageLocation * pageSize - pageSize + 8)
						+ (2 * (pos + 1)));
				temp = binaryFile.readUnsignedShort();
				binaryFile.seek((pageLocation * pageSize - pageSize + 8)
						+ (2 * (pos)));
				binaryFile.writeShort(temp);
				pos++;
			}
			temp = 0;
			for (int i = 0; i < cellCount - 1; i++) {
				binaryFile.seek((pageLocation * pageSize - pageSize + 8)
						+ (2 * (i)));
				if (temp < binaryFile.readUnsignedShort()) {
					binaryFile.seek((pageLocation * pageSize - pageSize + 8)
							+ (2 * (i)));
					temp = binaryFile.readUnsignedShort();
				}
			}
			binaryFile.seek(pageLocation * pageSize - pageSize + 2);
			binaryFile.writeShort(temp);
		} catch (IOException e1) {
			 
			e1.printStackTrace();
		}
	}

	public void CreateEmptyTable() {

		try {
			currentPage = 1;
			binaryFile.setLength(0);

			binaryFile.setLength(pageSize);

			WritePageHeader(currentPage, true, 0, -1);

		} catch (IOException e) {
			 
			 System.out.println(" ");
		}

	}

	public void CreateNewTableLeaf(Map<String, ArrayList<String>> tokenValue) {
		try {
			currentPage = 1;
			binaryFile.setLength(0);

			binaryFile.setLength(pageSize);

			WritePageHeader(currentPage, true, 0, -1);

			long numberOfBytes = PayloadSizeInBytes(tokenValue);
			long cellStartOffset = (currentPage * (pageSize))
					- (numberOfBytes + 6);

			WriteCell(currentPage, tokenValue, cellStartOffset, numberOfBytes);

		} catch (IOException e) {
			 
			 System.out.println(" ");
		}
	}

	public void UpdateRecord(Map<String, ArrayList<String>> tokenValue) {

	}

	public int GetNextMaxRowID() {
		currentPage = 1;
		SearchRightMostLeafNode();
		ReadPageHeader(currentPage);
		try {
			binaryFile.seek(PageHeaderOffsetnumberOfCells);
			int noOfCells = binaryFile.readUnsignedByte();
			binaryFile.seek(PageHeaderArrayOffset + (2 * (noOfCells - 1)));
			long address = binaryFile.readUnsignedShort();
			binaryFile.seek(address);
			binaryFile.readShort();
			return binaryFile.readInt();

		} catch (IOException e) {
			 
			 System.out.println(" ");
		}
		return -1;

	}

	

	public boolean IsExistsPrimaryKey(int newKey) {

		currentPage = 1;

		int rowId = newKey;
		SearchLeafPage(rowId, false);
		ReadPageHeader(currentPage);
		long[] result = GetCellOffset(rowId);
		long cell_Offset = result[1];
		if (cell_Offset > 0) {
			try {
				binaryFile.seek(cell_Offset);
				binaryFile.readUnsignedShort();
				int actualRowID = binaryFile.readInt();
				if (actualRowID == rowId) {

					return true;
				}

			} catch (IOException e) {
				 
				 System.out.println(" ");
			}

		} else {
			return false;
		}

		return false;
	}

	public LinkedHashMap<String, ArrayList<String>> SearchWithPrimaryKey(
			LinkedHashMap<String, ArrayList<String>> tokenValue) {

		currentPage = 1;

		LinkedHashMap<String, ArrayList<String>> value = null;
		int rowId = Integer.parseInt(tokenValue.get(tableKey).get(1));
		SearchLeafPage(rowId, false);
		ReadPageHeader(currentPage);
		long[] result = GetCellOffset(rowId);
		long cellOffset = result[1];
		if (cellOffset > 0) {
			try {
				binaryFile.seek(cellOffset);
				binaryFile.readUnsignedShort();
				int actualRowID = binaryFile.readInt();
				if (actualRowID == rowId) {

					if (isColSchema) {
						tokenValue = new LinkedHashMap<String, ArrayList<String>>();
						tokenValue.put("rowid", null);
						tokenValue.put("table_name", null);
						tokenValue.put("column_name", null);
						tokenValue.put("dataType", null);
						tokenValue.put("ordinal_position", null);
						tokenValue.put("is_nullable", null);
					} else if (isTableSchema) {
						tokenValue = new LinkedHashMap<String, ArrayList<String>>();
						tokenValue.put("rowid", null);
						tokenValue.put("table_name", null);

					} else {
						tokenValue = davisbaseColumnFileTree.getSchema(tableName);
					}

					value = PopulateData(cellOffset, tokenValue);

				}

			} catch (IOException e) {
				 
				 System.out.println(" ");
			}

			return value;

		} else {
			System.out.println(" No rows matches");
			return null;
		}

	}

	public boolean DeleteRecord(LinkedHashMap<String, ArrayList<String>> tokenValue) {
		currentPage = 1;
		boolean isDone = false;

		int rowId = Integer.parseInt(tokenValue.get(tableKey).get(1));
		SearchLeafPage(rowId, false);
		ReadPageHeader(currentPage);
		long[] retVal = GetCellOffset(rowId);
		long cellOffset = retVal[1];
		if (cellOffset > 0) {
			try {
				binaryFile.seek(cellOffset);
				binaryFile.readUnsignedShort();
				int actualRowID = binaryFile.readInt();
				if (actualRowID == rowId) {

					binaryFile.seek(PageOffsetStartOfCell);
					long startOfCell = binaryFile.readUnsignedShort();
					if (cellOffset == startOfCell) {

						binaryFile.seek(cellOffset);
						int payLoadSize = binaryFile.readUnsignedShort();
						binaryFile.seek(PageOffsetStartOfCell);
						binaryFile
						.writeShort((int) (startOfCell - payLoadSize - 6));

					}

					binaryFile.seek(PageHeaderOffsetnumberOfCells);

					int cellCount = binaryFile.readUnsignedByte();

					int temp;
					long pos = retVal[0];
					while (pos < cellCount) {
						binaryFile.seek((currentPage * pageSize - pageSize + 8)
								+ (2 * (pos + 1)));
						temp = binaryFile.readUnsignedShort();
						binaryFile.seek((currentPage * pageSize - pageSize + 8)
								+ (2 * (pos)));
						binaryFile.writeShort(temp);
						pos++;
					}

					binaryFile.seek(PageHeaderOffsetnumberOfCells);
					int col = binaryFile.readUnsignedByte();
					binaryFile.seek(PageHeaderOffsetnumberOfCells);
					binaryFile.writeByte(--col);
					if (col == 0) {

						binaryFile.seek(PageOffsetStartOfCell);
						binaryFile.writeShort((int) (currentPage * pageSize));

					}
					isDone = true;
				} else {

					System.out.println("No row matches");
				}

			} catch (IOException e) {
				 
				 System.out.println(" ");
			}

		} else {
			System.out.println(" No rows matches");
		}
		return isDone;
	}

	

	private void SearchRightMostLeafNode() {

		 

		routeOfLeafPage.add(currentPage);
		ReadPageHeader(currentPage);
		if (isLeafPage) {

			routeOfLeafPage.remove(routeOfLeafPage.size() - 1);
			return;
		} else {
			try {
				binaryFile.seek(PageHeaderOffsetRightPagePointer);

				currentPage = binaryFile.readInt();

				SearchRightMostLeafNode();

			} catch (IOException e) {
				 
				 System.out.println(" ");
			}
		}

	}

	

	public List<LinkedHashMap<String, ArrayList<String>>> PrintAll() {
		currentPage = 1;
		List<LinkedHashMap<String, ArrayList<String>>> result = new ArrayList<LinkedHashMap<String, ArrayList<String>>>();
		SearchLeftMostLeafNode();
		while (currentPage > 0) {
			try {
				ReadPageHeader(currentPage);
				PrintRecordsCurrentPage(result);

				binaryFile.seek(PageHeaderOffsetRightPagePointer);

				currentPage = binaryFile.readInt();

			} catch (Exception e) {
				 
				 System.out.println(" ");
			}
		}
		return result;

	}

	private void PrintRecordsCurrentPage(
			List<LinkedHashMap<String, ArrayList<String>>> result)
					throws Exception {
		binaryFile.seek(PageHeaderOffsetnumberOfCells);
		int noOfCol = binaryFile.readUnsignedByte();

		binaryFile.seek(PageHeaderArrayOffset);
		long point = binaryFile.getFilePointer();
		int address = binaryFile.readUnsignedShort();

		for (int i = 0; i < noOfCol; i++) {

			binaryFile.seek(address);

			binaryFile.readUnsignedShort();
			int currentRowID = binaryFile.readInt();

			LinkedHashMap<String, ArrayList<String>> tokenValue = null;
			if (isColSchema) {
				tokenValue = new LinkedHashMap<String, ArrayList<String>>();
				tokenValue.put("rowid", null);
				tokenValue.put("table_name", null);
				tokenValue.put("column_name", null);
				tokenValue.put("dataType", null);
				tokenValue.put("ordinal_position", null);
				tokenValue.put("is_nullable", null);
			} else if (isTableSchema) {
				tokenValue = new LinkedHashMap<String, ArrayList<String>>();
				tokenValue.put("rowid", null);
				tokenValue.put("table_name", null);

			} else {
				tokenValue = davisbaseColumnFileTree.getSchema(tableName);
			}

			result.add(PopulateData(address, tokenValue));

			point = (point + 2);
			binaryFile.seek(point);
			address = binaryFile.readUnsignedShort();

		}

	}

	

	
	private LinkedHashMap<String, ArrayList<String>> PopulateData(
			long cellOffset, LinkedHashMap<String, ArrayList<String>> tokenValue) {
		 

		ArrayList<String> arrayValues = new ArrayList<String>();
		try {
			binaryFile.seek(cellOffset);
			int payLoadSize = binaryFile.readUnsignedShort();
			Integer actualRowID = binaryFile.readInt();
			short numberOfColumns = binaryFile.readByte();
			payLoadSize -= 1;
			long offsetForSerialType = binaryFile.getFilePointer();
			long offSetForData = (offsetForSerialType + numberOfColumns);
			int i = 0;
			for (String key : tokenValue.keySet()) {

				if (i == 0) {
					arrayValues.add(actualRowID.toString());
					tokenValue.put(key, new ArrayList<String>(arrayValues));
					i++;
					arrayValues.clear();
					continue;
				}

				binaryFile.seek(offsetForSerialType);
				short b = binaryFile.readByte();
				offsetForSerialType = binaryFile.getFilePointer();

				if (b == 0) {

					binaryFile.seek(offSetForData);
					int p = (binaryFile.readUnsignedByte());
					arrayValues.add("NULL");
					offSetForData = binaryFile.getFilePointer();
					tokenValue.put(key, new ArrayList<String>(arrayValues));

				} else if (b == 1) {

					binaryFile.seek(offSetForData);
					int p = (binaryFile.readUnsignedShort());
					arrayValues.add("NULL");
					offSetForData = binaryFile.getFilePointer();
					tokenValue.put(key, new ArrayList<String>(arrayValues));

				} else if (b == 2) {
					binaryFile.seek(offSetForData);
					int p = (binaryFile.readInt());
					arrayValues.add("NULL");
					offSetForData = binaryFile.getFilePointer();
					tokenValue.put(key, new ArrayList<String>(arrayValues));
				} else if (b == 3) {

					binaryFile.seek(offSetForData);
					int p = (int) (binaryFile.readDouble());
					arrayValues.add("NULL");
					offSetForData = binaryFile.getFilePointer();
					tokenValue.put(key, new ArrayList<String>(arrayValues));

				} else if (b == 12) {
					arrayValues.add("NULL");
					tokenValue.put(key, new ArrayList<String>(arrayValues));
				} else if (b == 4) {
					binaryFile.seek(offSetForData);
					arrayValues.add(Integer.toString(binaryFile
							.readUnsignedByte()));
					offSetForData = binaryFile.getFilePointer();
					tokenValue.put(key, new ArrayList<String>(arrayValues));
				} else if (b == 5) {
					binaryFile.seek(offSetForData);
					arrayValues.add(Integer.toString(binaryFile
							.readUnsignedShort()));
					offSetForData = binaryFile.getFilePointer();
					tokenValue.put(key, new ArrayList<String>(arrayValues));
				} else if (b == 6) {
					binaryFile.seek(offSetForData);
					arrayValues.add(Integer.toString(binaryFile.readInt()));
					offSetForData = binaryFile.getFilePointer();
					tokenValue.put(key, new ArrayList<String>(arrayValues));
				} else if (b == 7) {
					binaryFile.seek(offSetForData);
					arrayValues.add(Long.toString(binaryFile.readLong()));
					offSetForData = binaryFile.getFilePointer();
					tokenValue.put(key, new ArrayList<String>(arrayValues));
				} else if (b == 8) {

					binaryFile.seek(offSetForData);
					arrayValues.add(Float.toString(binaryFile.readFloat()));
					offSetForData = binaryFile.getFilePointer();
					tokenValue.put(key, new ArrayList<String>(arrayValues));
				} else if (b == 9) {

					binaryFile.seek(offSetForData);
					arrayValues.add(Double.toString(binaryFile.readDouble()));
					offSetForData = binaryFile.getFilePointer();
					tokenValue.put(key, new ArrayList<String>(arrayValues));

				} else if (b == 10) {
					binaryFile.seek(offSetForData);

					long timeInEpoch = binaryFile.readLong();
					Instant ii = Instant.ofEpochSecond(timeInEpoch);
					ZonedDateTime zdt2 = ZonedDateTime.ofInstant(ii, zoneId);
					SimpleDateFormat sdf = new SimpleDateFormat(
							"yyyy-MM-dd_HH:mm:ss");
					Date date = Date.from(zdt2.toInstant());
					arrayValues.add(sdf.format(date));

					offSetForData = binaryFile.getFilePointer();
					tokenValue.put(key, new ArrayList<String>(arrayValues));
				} else if (b == 11) {
					binaryFile.seek(offSetForData);
					long timeInEpoch = binaryFile.readLong();
					Instant ii = Instant.ofEpochSecond(timeInEpoch);
					ZonedDateTime zdt2 = ZonedDateTime.ofInstant(ii, zoneId);
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

					Date date = Date.from(zdt2.toInstant());
					arrayValues.add(sdf.format(date));
					offSetForData = binaryFile.getFilePointer();
					tokenValue.put(key, new ArrayList<String>(arrayValues));
				} else {
					byte[] text = new byte[b - 12];
					binaryFile.seek(offSetForData);

					binaryFile.read(text);
					arrayValues.add(new String(text));
					offSetForData = binaryFile.getFilePointer();

					tokenValue.put(key, new ArrayList<String>(arrayValues));

				}
				arrayValues.clear();
			}

		} catch (Exception e) {
			 System.out.println(" ");
		}

		return tokenValue;
	}

	private long[] GetCellOffset(int rowId) {
		 
		long[] retVal = new long[2];
		int cellOffset = -1;
		try {
			binaryFile.seek(PageHeaderOffsetnumberOfCells);

			int numberOfColumns = binaryFile.readUnsignedByte();

			binaryFile.seek(PageHeaderArrayOffset);
			long point = binaryFile.getFilePointer();
			int address = binaryFile.readUnsignedShort();
			for (int i = 0; i < numberOfColumns; i++) {

				binaryFile.seek(address);

				binaryFile.readUnsignedShort();
				int currentRowID = binaryFile.readInt();

				if (rowId == currentRowID) {
					cellOffset = address;
					retVal[0] = i;
					retVal[1] = cellOffset;
					return retVal;

				} else {

					point = (point + 2);
					binaryFile.seek(point);
					address = binaryFile.readUnsignedShort();
				}

			}

		} catch (IOException e) {
			 
			 System.out.println(" ");
		}

		return retVal;
	}

	public boolean isTableEmpty() throws IOException {
		return binaryFile.length() == 0;
	}

	public void InsertNewRecord(Map<String, ArrayList<String>> tokenValue)
			throws Exception {
		currentPage = 1;
		int rowId = -1;
		if (isColSchema || isTableSchema) {
			tableKey = "rowid";
		}
		rowId = Integer.parseInt(tokenValue.get(tableKey).get(1));
		if (rowId < 0)
			throw new Exception("Insert failed");

		SearchLeafPage(rowId, false);

		InsertNewRecordInPage(tokenValue, rowId, currentPage);

		routeOfLeafPage.clear();

	}

	private void InsertNewRecordInPage(Map<String, ArrayList<String>> tokenValue,
			int rowId, int pageNumber) {
		ReadPageHeader(pageNumber);

		long numberOfBytes = PayloadSizeInBytes(tokenValue);
		long cellStartOffset = 0;
		try {
			binaryFile.seek(PageOffsetStartOfCell);

			cellStartOffset = ((long) binaryFile.readUnsignedShort())
					- (numberOfBytes + 6);

		} catch (IOException e) {
			 
			 System.out.println(" "+e);
		}
		if (cellStartOffset < PageHeaderOffset + 2) {

			LinkedList<byte[]> page1Cells = new LinkedList<>();
			LinkedList<byte[]> page2Cells = new LinkedList<>();
			try {
				binaryFile.seek(PageHeaderOffsetnumberOfCells);
				int numberOfCells = binaryFile.readUnsignedByte();

				int splitCells = numberOfCells / 2;
				int loc = 0;
				splitCells = 1;

				long point = PageHeaderOffset - 2;

				binaryFile.seek(point);

				binaryFile.seek(binaryFile.readUnsignedShort());

				binaryFile.readUnsignedShort();

				int currenRowID = binaryFile.readInt();
				while ((currenRowID > rowId)) {
					splitCells++;
					point = point - 2;
					binaryFile.seek(point);
					binaryFile.seek(binaryFile.readUnsignedShort());
					binaryFile.readUnsignedShort();
					currenRowID = binaryFile.readInt();

				}

				if (point == PageHeaderOffset - 2) {
					splitCells = 0;
					if (currentPage == 1) {
						point = PageHeaderArrayOffset;
						for (int i = 1; i <= numberOfCells; i++) {

							binaryFile.seek(point);
							loc = binaryFile.readUnsignedShort();

							binaryFile.seek(point);
							binaryFile.writeShort(0);

							point = binaryFile.getFilePointer();

							binaryFile.seek(loc);
							binaryFile.readUnsignedShort();

							binaryFile.seek(loc);
							byte[] cell = ReadCell(loc);

							page1Cells.add(cell);
						}

					}

				} else {
					if (currentPage == 1) {
						point = PageHeaderArrayOffset;
						for (int i = 1; i <= numberOfCells - splitCells; i++) {

							binaryFile.seek(point);
							loc = binaryFile.readUnsignedShort();

							binaryFile.seek(point);
							binaryFile.writeShort(0);

							point = binaryFile.getFilePointer();

							binaryFile.seek(loc);
							binaryFile.readUnsignedShort();

							binaryFile.seek(loc);
							byte[] cell = ReadCell(loc);

							page1Cells.add(cell);

						}
					}

					for (int i = splitCells; i <= 1; i--) {

						point = PageHeaderOffset - (2 * i);
						binaryFile.seek(point);
						loc = binaryFile.readUnsignedShort();

						binaryFile.seek(point);
						binaryFile.writeShort(0);

						binaryFile.seek(loc);
						byte[] cell = ReadCell(loc);

						page2Cells.add(cell);
					}
				}

				int rowIdMiddle = 0;
				if (currenRowID > rowId) {
					rowIdMiddle = currenRowID;
				} else {
					rowIdMiddle = rowId;
				}
				if (splitCells > 0) {
					binaryFile.seek(PageHeaderOffsetnumberOfCells);
					int noOfcells = binaryFile.readUnsignedByte();
					binaryFile.seek(PageHeaderOffsetnumberOfCells);
					binaryFile.writeByte(noOfcells - splitCells);
				}

				int[] pageNumbers = SplitLeafPage(page1Cells, page2Cells);

				binaryFile.seek(((pageNumbers[0] * pageSize) - pageSize) + 4);
				int prevRight = binaryFile.readInt();
				binaryFile.seek(((pageNumbers[0] * pageSize) - pageSize) + 4);
				binaryFile.writeInt(pageNumbers[1]);
				binaryFile.seek(((pageNumbers[1] * pageSize) - pageSize) + 4);
				binaryFile.writeInt(prevRight);


				if (routeOfLeafPage.size() > 0
						&& routeOfLeafPage.get(routeOfLeafPage.size() - 1) > 0) {

					WriteCellInterior(
							routeOfLeafPage.remove(routeOfLeafPage.size() - 1),
							pageNumbers[0], rowIdMiddle, pageNumbers[1]);

				} else {
					currentPage = 1;
					CreateNewInterior(pageNumbers[0], rowIdMiddle,
							pageNumbers[1]);

				}

				if (rowId < rowIdMiddle) {
					currentPage = pageNumbers[0];
				} else {
					currentPage = pageNumbers[1];
				}
				InsertNewRecordInPage(tokenValue, rowId, currentPage);

			} catch (IOException e) {
				 
				 System.out.println(" ");

			}

		} else {

			WriteCell(currentPage, tokenValue, cellStartOffset, numberOfBytes);
		}
	}

	private boolean SearchLeafPage(int rowId, boolean isFound) {
		 

		routeOfLeafPage.add(currentPage);
		ReadPageHeader(currentPage);
		if (isLeafPage) {

			routeOfLeafPage.remove(routeOfLeafPage.size() - 1);
			return true;
		} else {
			try {
				binaryFile.seek(PageHeaderOffsetnumberOfCells);

				int numberOfColumns = binaryFile.readUnsignedByte();

				binaryFile.seek(PageHeaderArrayOffset);
				long currentArrayElementOffset = binaryFile.getFilePointer();
				int address;
				for (int i = 0; i < numberOfColumns; i++) {
					binaryFile.seek(currentArrayElementOffset);
					address = binaryFile.readUnsignedShort();
					currentArrayElementOffset = binaryFile.getFilePointer();
					binaryFile.seek(address);
					int pageNumber = binaryFile.readInt();
					int delimiterRowId = binaryFile.readInt();
					if (rowId < delimiterRowId) {
						currentPage = pageNumber;
						isFound = SearchLeafPage(rowId, false);

						break;
					}
				}

				if (!isFound) {
					binaryFile.seek(PageHeaderOffsetRightPagePointer);
					currentPage = binaryFile.readInt();
					isFound = SearchLeafPage(rowId, false);
				}

			} catch (IOException e) {
				 
				 System.out.println(" ");
			}
			return isFound;
		}

	}

	private byte[] ReadCell(int location) {
		 

		try {
			binaryFile.seek(location);

			int payloadLength = binaryFile.readUnsignedShort();

			byte[] b = new byte[6 + payloadLength];
			binaryFile.seek(location);

			binaryFile.read(b);
			binaryFile.seek(location);
			binaryFile.write(new byte[6 + payloadLength]);

			return b;
		} catch (Exception e) {
			 System.out.println(" ");
		}

		return null;

	}

	private int[] SplitLeafPage(LinkedList<byte[]> cellsPage1,
			LinkedList<byte[]> cellsPage2) {
		 

		int[] pageNumbers = new int[2];

		try {
			if (currentPage != 1) {
				pageNumbers[0] = currentPage;
				PageHeaderOffset = PageHeaderArrayOffset;
				if (cellsPage1.size() > 0) {
					binaryFile.seek(PageOffsetStartOfCell);
					binaryFile.writeShort(currentPage * (pageSize));
				}
				for (byte[] s : cellsPage1) {

					long cellStartOffset = 0;

					binaryFile.seek(PageOffsetStartOfCell);

					cellStartOffset = ((long) binaryFile.readUnsignedShort())
							- (s.length);
					WriteCellInBytes(currentPage, s, cellStartOffset);

				}
			} else {

				lastPage += 1;

				pageNumbers[0] = lastPage;
				currentPage = lastPage;
				CreatePage(cellsPage1);
			}
		} catch (IOException e) {
			 
			 System.out.println(" ");

		}

		lastPage += 1;

		pageNumbers[1] = lastPage;
		currentPage = lastPage;
		CreatePage(cellsPage2);
		return pageNumbers;

	}

	private void WriteCell(int pageLocation,
			Map<String, ArrayList<String>> tokenValue, long startCell,
			long numberOfBytes) {

		try {
			binaryFile.seek(PageOffsetStartOfCell);
			binaryFile.writeShort((int) startCell);

			binaryFile.seek(PageHeaderOffsetnumberOfCells);
			short current_Cell_size = binaryFile.readByte();
			binaryFile.seek(PageHeaderOffsetnumberOfCells);
			binaryFile.write(current_Cell_size + 1);

		} catch (IOException e1) {
			 
			e1.printStackTrace();
		}

		WriteToHeaderArray(startCell,
				Integer.parseInt(tokenValue.get(tableKey).get(1)));
		try {
			binaryFile.seek(startCell);
		} catch (IOException e) {
			 
			 System.out.println(" ");
		}

		WriteCellHeader(pageLocation, true,
				Integer.parseInt(tokenValue.get(tableKey).get(1)), numberOfBytes);
		WriteCellContent(pageLocation, tokenValue);

	}

	private void WriteCellInBytes(int pageLocation, byte[] b,
			long cellStartOffset) {

		try {
			binaryFile.seek(PageOffsetStartOfCell);
			binaryFile.writeShort((int) cellStartOffset);
		} catch (IOException e1) {
			 
			e1.printStackTrace();
		}
		byte[] rowId = Arrays.copyOfRange(b, 2, 6);
		int id = java.nio.ByteBuffer.wrap(rowId).getInt();
		WriteToHeaderArray(cellStartOffset, id);
		try {
			binaryFile.seek(cellStartOffset);
			binaryFile.write(b);
		} catch (IOException e) {
			 
			 System.out.println(" ");
		}

	}

	private long PayloadSizeInBytes(Map<String, ArrayList<String>> tokenValue) {
		long numberOfBytes = 0;
		for (String key : tokenValue.keySet()) {
			if (key.equals(tableKey))
				continue; 
			ArrayList<String> dataType = tokenValue.get(key);

			switch (dataType.get(0).trim().toLowerCase()) {

			case "tinyint":
				numberOfBytes += 1;
				break;
			case "smallint":
				numberOfBytes += 2;
				break;
			case "int":
				numberOfBytes += 4;
				break;
			case "bigint":
				numberOfBytes += 8;
				break;
			case "real":
				numberOfBytes += 4;
				break;
			case "double":
				numberOfBytes += 8;
				break;
			case "datetime":
				numberOfBytes += 8;
				break;
			case "date":
				numberOfBytes += 8;
				break;
			case "text":
				numberOfBytes += dataType.get(1).length();
				break;
			}

		}
		numberOfBytes += tokenValue.size();
		return numberOfBytes;
	}

	private void WriteToHeaderArray(long startCell, int rowID) {
		 

		try {
			binaryFile.seek(PageHeaderOffsetnumberOfCells);
			int cellCount = binaryFile.readUnsignedByte();
			int pos = 0;
			for (int i = 0; i < cellCount; i++) {
				binaryFile.seek((currentPage * pageSize - pageSize + 8)
						+ (2 * i));
				binaryFile.seek(binaryFile.readUnsignedShort() + 2);
				if (rowID < binaryFile.readInt()) {
					pos = i;
					break;
				}
			}
			while (pos < cellCount) {
				binaryFile.seek((currentPage * pageSize - pageSize + 8)
						+ (2 * (cellCount - 1)));
				binaryFile.writeShort(binaryFile.readUnsignedShort());
				cellCount--;
			}
			binaryFile.seek((currentPage * pageSize - pageSize + 8)
					+ (2 * (pos)));
			binaryFile.writeShort((int) startCell);

		}

		catch (Exception e) {
			 System.out.println(" ");
		}
	}

	private void WriteCellContent(int pageLocation2,
			Map<String, ArrayList<String>> tokenValue) {
		 
		try {

			binaryFile.write(tokenValue.size() - 1);

			WriteSerialCodeType(tokenValue);

			WritePayLoad(tokenValue);
		} catch (Exception e) {
			 System.out.println(" ");
		}

	}

	private void WritePayLoad(Map<String, ArrayList<String>> tokenValue)
			throws IOException, UnsupportedEncodingException {

		for (String key : tokenValue.keySet()) {
			if (key.equals(tableKey))
				continue; 

			ArrayList<String> dataType = tokenValue.get(key);

			switch (dataType.get(0).trim().toLowerCase()) {

			case "tinyint":
				if (dataType.get(1) != null
				&& !dataType.get(1).trim().equalsIgnoreCase("null")) {
					binaryFile.write(Integer.parseInt(dataType.get(1)));
				} else {
					binaryFile.write(128);

				}

				break;
			case "smallint":
				if (dataType.get(1) != null
				&& !dataType.get(1).trim().equalsIgnoreCase("null")) {
					binaryFile.writeShort(Integer.parseInt(dataType.get(1)));
				} else {
					binaryFile.writeShort(-1);
				}

				break;
			case "int":
				if (dataType.get(1) != null
				&& !dataType.get(1).trim().equalsIgnoreCase("null")) {
					binaryFile.writeInt(Integer.parseInt(dataType.get(1)));
				} else {
					binaryFile.writeInt(-1);
				}
				break;
			case "bigint":
				if (dataType.get(1) != null
				&& !dataType.get(1).trim().equalsIgnoreCase("null")) {
					binaryFile.writeLong(Long.parseLong(dataType.get(1)));
				} else {
					binaryFile.writeLong(-1);
				}

				break;
			case "real":
				if (dataType.get(1) != null
				&& !dataType.get(1).trim().equalsIgnoreCase("null")) {
					binaryFile.writeFloat(Float.parseFloat((dataType.get(1))));
				} else {
					binaryFile.writeFloat(-1);
				}

				break;
			case "double":
				if (dataType.get(1) != null
				&& !dataType.get(1).trim().equalsIgnoreCase("null")) {
					binaryFile
					.writeDouble(Double.parseDouble((dataType.get(1))));
				} else {
					binaryFile.writeDouble(-1);
				}

				break;
			case "datetime":
				if (dataType.get(1) != null
				&& !dataType.get(1).trim().equalsIgnoreCase("null")) {

					SimpleDateFormat df = new SimpleDateFormat(
							"yyyy-MM-dd_HH:mm:ss");

					Date date;
					try {
						date = df.parse(dataType.get(1));

						ZonedDateTime zdt = ZonedDateTime.ofInstant(
								date.toInstant(), zoneId);

						long epochSeconds = zdt.toInstant().toEpochMilli() / 1000;

						binaryFile.writeLong(epochSeconds);
					} catch (ParseException e) {
						 
						 System.out.println(" ");
					}
				} else {
					binaryFile.writeLong(-1);

				}

				break;
			case "date":
				if (dataType.get(1) != null
				&& !dataType.get(1).trim().equalsIgnoreCase("null")) {

					SimpleDateFormat d = new SimpleDateFormat("yyyy-MM-dd");

					Date date;
					try {
						date = d.parse(dataType.get(1));

						ZonedDateTime zdt = ZonedDateTime.ofInstant(
								date.toInstant(), zoneId);

						long epochSeconds = zdt.toInstant().toEpochMilli() / 1000;

						binaryFile.writeLong(epochSeconds);
					} catch (ParseException e) {
						 
						 System.out.println(" ");
					}
				} else {
					binaryFile.writeLong(-1);

				}

				break;
			case "text":
				if (dataType.get(1) != null) {
					String s = dataType.get(1);
					byte[] b = s.getBytes("UTF-8");
					for (byte bb : b)
						binaryFile.write(bb);
				}

				break;

			}

		}
	}

	private void WriteSerialCodeType(Map<String, ArrayList<String>> tokenValue)
			throws IOException {
		for (String key : tokenValue.keySet()) {
			if (key.equals(tableKey))
				continue; 
			ArrayList<String> dataType = tokenValue.get(key);

			switch (dataType.get(0).trim().toLowerCase()) {

			case "tinyint":
				if (dataType.get(1) != null
				&& !dataType.get(1).trim().equalsIgnoreCase("null")) {
					binaryFile.write(4);
				} else {
					binaryFile.write(0);
				}
				break;
			case "smallint":
				if (dataType.get(1) != null
				&& !dataType.get(1).trim().equalsIgnoreCase("null")) {
					binaryFile.write(5);
				} else {
					binaryFile.write(1);
				}
				break;
			case "int":
				if (dataType.get(1) != null
				&& !dataType.get(1).trim().equalsIgnoreCase("null")) {
					binaryFile.write(6);
				} else {
					binaryFile.write(2);
				}
				break;
			case "bigint":
				if (dataType.get(1) != null
				&& !dataType.get(1).trim().equalsIgnoreCase("null")) {
					binaryFile.write(7);
				} else {
					binaryFile.write(3);
				}
				break;
			case "real":
				if (dataType.get(1) != null
				&& !dataType.get(1).trim().equalsIgnoreCase("null")) {
					binaryFile.write(8);
				} else {
					binaryFile.write(2);
				}
				break;
			case "double":
				if (dataType.get(1) != null
				&& !dataType.get(1).trim().equalsIgnoreCase("null")) {
					binaryFile.write(9);
				} else {
					binaryFile.write(3);
				}
				break;
			case "datetime":
				if (dataType.get(1) != null
				&& !dataType.get(1).trim().equalsIgnoreCase("null")) {
					binaryFile.write(10);
				} else {
					binaryFile.write(3);
				}
				break;
			case "date":
				if (dataType.get(1) != null
				&& !dataType.get(1).trim().equalsIgnoreCase("null")) {
					binaryFile.write(11);
				} else {
					binaryFile.write(3);
				}
				break;
			case "text":
				if (dataType.get(1) != null
				&& !dataType.get(1).trim().equalsIgnoreCase("null")) {
					binaryFile.write(12 + (dataType.get(1).length()));
				} else {
					binaryFile.write(12);
				}
				break;

			}

		}
	}

	private void WriteCellHeader(int pageLocation2, boolean is_Leaf,
			int rowIdPageNumber, long numberOfBytes) {
		 
		try {
			if (is_Leaf) {
				binaryFile.writeShort((int) numberOfBytes);
				binaryFile.writeInt(rowIdPageNumber);

			} else {

			}
		} catch (Exception e) {
			 System.out.println(" ");
		}

	}

	private void WritePageHeader(int pageLocation, boolean is_Leaf,
			int numberOfCells, int right_Page) {
		 

		try {

			binaryFile.seek(pageLocation * pageSize - pageSize);

			if (is_Leaf) {
				binaryFile.write(13);

				PageHeaderOffsetnumberOfCells = binaryFile.getFilePointer();
				binaryFile.write(numberOfCells);

				PageOffsetStartOfCell = binaryFile.getFilePointer();
				binaryFile.writeShort((int) (pageLocation * pageSize));

				PageHeaderOffsetRightPagePointer = binaryFile
						.getFilePointer();

				binaryFile.writeInt(-1);

				PageHeaderArrayOffset = binaryFile.getFilePointer();
				PageHeaderOffset = PageHeaderArrayOffset;
			} else {
				binaryFile.write(5);
				PageHeaderOffsetnumberOfCells = binaryFile.getFilePointer();
				binaryFile.write(0);
				PageOffsetStartOfCell = binaryFile.getFilePointer();
				binaryFile.writeShort((int) (pageLocation * pageSize));
				binaryFile.writeInt(right_Page);

				PageHeaderArrayOffset = binaryFile.getFilePointer();
				PageHeaderOffset = PageHeaderArrayOffset;
			}

		}

		catch (Exception e) {
			System.out.println(" ");

		}
	}

	

	public boolean close_File() {
		boolean isSucess = false;

		try {
			binaryFile.close();
			isSucess = true;
		} catch (IOException e) {
			 
			 System.out.println(" ");

		}

		return isSucess;
	}

	private void CreatePage(LinkedList<byte[]> page_Cells) {

		try {
			binaryFile.setLength(pageSize * currentPage);
			WritePageHeader(currentPage, true, page_Cells.size(), -1);
			ReadPageHeader(currentPage);

			PageHeaderOffset = PageHeaderArrayOffset;
			ListIterator<byte[]> iterator = page_Cells.listIterator(page_Cells
					.size());

			long cellStartOffset = 0;
			try {

				binaryFile.seek(PageOffsetStartOfCell);
				binaryFile.writeShort(currentPage * (pageSize));
				while (iterator.hasPrevious()) {
					byte[] s = iterator.previous();

					binaryFile.seek(PageOffsetStartOfCell);

					cellStartOffset = ((long) binaryFile.readUnsignedShort())
							- (s.length);
					WriteCellInBytes(currentPage, s, cellStartOffset);

				}

			} catch (IOException e) {
				 
				 System.out.println(" ");
			}

		} catch (Exception e) {
			 
			 System.out.println(" ");
		}

	}

	public String get_PrimaryKey() {
		return tableKey;
	}

}
