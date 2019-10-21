package jp.pioneer.carsync.presentation.model;

import java.util.Date;

/**
 * ContactsHistoryItem
 */

public class ContactsHistoryItem {
    public int callType;
    public String displayName;
    public int count;
    public Date date;
    public String number;

    public ContactsHistoryItem() {
        this.callType = 0;
        this.displayName = null;
        this.count = 0;
        this.date = null;
        this.number = null;
    }

    public ContactsHistoryItem(int callType, String displayName, int count, Date date, String number) {
        this.callType = callType;
        this.displayName = displayName;
        this.count = count;
        this.date = (Date)date.clone();
        this.number = number;
    }
}
