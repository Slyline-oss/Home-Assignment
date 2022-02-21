package com.melihovs;

import java.sql.Timestamp;

public class Record {

    private static int count = 0;
    private final int id;
    private String msgType;
    private String msgText;
    private String excData;
    private Timestamp time;

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public void setMsgText(String msgText) {
        this.msgText = msgText;
    }

    public void setExcData(String excData) {
        this.excData = excData;
    }

    public int getId() {
        return id;
    }

    public String getMsgType() {
        return msgType;
    }

    public String getMsgText() {
        return msgText;
    }

    public String getExcData() {
        return excData;
    }

    public Timestamp getTime() {
        return time;
    }

    public Record() {
        this.id = count;
        count++;
    }

    @Override
    public String toString() {
        String info = "Record id #" + this.id + "\n " +
                "\tTime: " + this.time.toString() + "\n" +
                "\tType: " + msgType + "\n" +
                 "\tMessage: " + msgText + "\n" +
                "\tException Data: " + excData + "\n";
        return info;
    }
}
