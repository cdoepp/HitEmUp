package cdoepp.hitemup;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by cdoepp on 9/23/18.
 */

public class Message implements Serializable {
    public static final int TYPE_SMS = 0;
    public static final int TYPE_MMS = 1;

    public static final int TYPE_RECEIVED = 1;
    public static final int TYPE_SENT = 2;

    private String id;

    private int type; // 1 = received, 2 = sent

    private String address;

    private Date date;

    private String text;


    public Message(String id, int type, String address, Date date, String text) {
        this.id = id;
        this.type = type;
        this.address = address;
        this.text = text;
        this.date = date;
    }

    public Message(String id, int type, String address, long timestamp, String text) {
        this.id = id;
        this.type = type;
        this.address = address;
        this.text = text;
        date = new Date(timestamp);
    }

    public int getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public Date getDate() {
        return date;
    }

    public String getAddress() {
        return address;
    }

    public String getText() {
        return text;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setDate(long timestamp) {
        this.date = new Date(timestamp);
    }

    public long getTimestamp() {
        return date.getTime();
    }

    public void setText(String text) {
        this.text = text;
    }

    public String toString() {
        if (type == Message.TYPE_RECEIVED)
            return "Message received from " + address + ": " + text + ", at " + date.toString() + ", id = " + id;
        else
            return "Message sent to " + address + ": " + text + ", at " + date.toString() + ", id = " + id;
    }

}
