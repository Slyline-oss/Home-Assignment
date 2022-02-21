package com.melihovs;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class Main {

    //Datubāze tiks izvedeidota pēc momenta, kad skripts sāks strādāt

    private static Map<String, Integer> messageTypes = new HashMap<>();
    private static LinkedList<Record> records = new LinkedList<>();

    public static void main(String[] args) {

        String [] readableString;
        int [] messageCount = new int[5];
        messageTypes.put("INFO", 0);
        messageTypes.put("WARNING", 1);
        messageTypes.put("VERBOSE", 2);
        messageTypes.put("DEBUG",3);
        messageTypes.put("ERROR",4);

        try {
            FileInputStream fstream = new FileInputStream(args[0]);
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
            String strLine;
            while ((strLine = br.readLine()) != null)   {
                
                readableString = strLine.split(":");
                if (readableString.length == 1) continue; //string is not a record
                Record record = new Record();
                Timestamp timestamp = parseDate(readableString);
                record.setTime(timestamp);
                readableString[1] = readableString[1].replaceAll(" ", "");
                readableString[3] = readableString[3].replaceFirst(" ","");
                record.setMsgType(readableString[1]);
                record.setMsgText(readableString[3]);
                if (checkIfExistsException(readableString)) {
                    readableString[5] = readableString[5].replaceFirst(" ", "");
                    record.setExcData(readableString[5]);
                }

                records.add(record);
                incrementCountOfTypes(messageCount,readableString);

            }
            fstream.close();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

        for (Record record: records) {
            System.out.println(record.toString());
        }

        printSummary(messageCount);

        downloadToDatabase();
    }

    //takes the date and time from array and converts into timestamp format
    private static Timestamp parseDate(String[] readableString) throws ParseException {
        String date = readableString[0];
        String newPattern = "yyyyMMdd.HHmmssSSS";
        SimpleDateFormat formatter1=new SimpleDateFormat(newPattern);
        Date date1 = formatter1.parse(date);
        return new Timestamp(date1.getTime());
    }

    //This method checks if exist an exception (if yes returns true otherwise false)
    private static boolean checkIfExistsException(String [] readableString) {
        String data = readableString[3];
        if (data.contains("Exception")) return true;
        return false;
    }

    //Method that increases the amount of each unique message
    private static void incrementCountOfTypes(int[] messageCount, String[] readableString) {
        int option = messageTypes.get(readableString[1]);
        switch (option) {
            case 0:
                messageCount[0]++;
                break;
            case 1:
                messageCount[1]++;
                break;
            case 2:
                messageCount[2]++;
                break;
            case 3:
                messageCount[3]++;
                break;
            case 4:
                messageCount[4]++;
                break;
        }
    }


    //Method that prints in console summary according to the amount of each unique message
    private static void printSummary(int[] messageCount) {
        System.out.println("Found " + records.size() + " log records.");
        for (int i = 0; i < messageCount.length; i++) {
            switch (i) {
                case 0:
                    System.out.println("INFO - " + messageCount[i]);
                    break;
                case 1:
                    System.out.println("WARNING - " + messageCount[i]);
                    break;
                case 2:
                    System.out.println("VERBOSE - " + messageCount[i]);
                    break;
                case 3:
                    System.out.println("DEBUG - " + messageCount[i]);
                    break;
                case 4:
                    System.out.println("ERROR - " + messageCount[i]);
                    break;
            }
        }
    }


    //Method that connects to Database (SQLite) and inserts all the records
    //If database does not exist, it creates it
    //Avoid duplicates
    private static boolean downloadToDatabase() {
        try(Connection con = DriverManager.getConnection("jdbc:sqlite:src/recordsDatabase.db");
            Statement statement = con.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS records" +
                    "(id INTEGER PRIMARY key UNIQUE , time TEXT , MessageType TEXT, MessageText TEXT, ExceptionData TEXT )");

            PreparedStatement ps = null;
            String ins = "INSERT INTO records (id, time, MessageType, MessageText, ExceptionData) VALUES(?,?,?,?,?)";
            ps = con.prepareStatement(ins);
            for (int i = 0; i < records.size(); i++) {
                Record record = records.get(i);
                int id  = record.getId();
                String type = record.getMsgType();
                String text = record.getMsgText();
                Timestamp time = record.getTime();
                String exc = record.getExcData();
                ps.setInt(1,id);
                ps.setString(2,time.toString());
                ps.setString(3,type);
                ps.setString(4,text);
                ps.setString(5,exc);
                ps.execute();
            }
            System.out.println("Records were inserted");
            statement.close();
            con.close();
            return true;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }
}
