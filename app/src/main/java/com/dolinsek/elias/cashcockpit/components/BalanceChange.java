package com.dolinsek.elias.cashcockpit.components;

/**
 * Created by Elias Dolinsek on 28.03.2018 for cash-cockpit.
 */
public class BalanceChange {

    private long timeStampOfChange;
    private long newBalance;

    @Override
    public String toString() {
        return "BalanceChange{" +
                "timeStampOfChange=" + timeStampOfChange +
                ", newBalance=" + newBalance +
                '}';
    }

    public BalanceChange(long timeStampOfChange, long newBalance) {
        this.timeStampOfChange = timeStampOfChange;
        this.newBalance = newBalance;
    }

    public long getTimeStampOfChange() {
        return timeStampOfChange;
    }

    public void setTimeStampOfChange(long timeStampOfChange) {
        this.timeStampOfChange = timeStampOfChange;
    }

    public long getNewBalance() {
        return newBalance;
    }

    public void setNewBalance(long newBalance) {
        this.newBalance = newBalance;
    }
}
