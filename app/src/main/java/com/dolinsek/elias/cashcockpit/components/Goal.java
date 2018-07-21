package com.dolinsek.elias.cashcockpit.components;

/**
 * Represents a goal. A goal is basically a budget of a category what the user doesn't want to exceed
 * Created by elias on 06.01.2018.
 */

public class Goal {

    /**
     * Amount what the user doesn't want to exceed
     */
    private long amount;

    private long creationDate;

    @Override
    public String toString() {
        return "Goal{" +
                "amount=" + amount +
                ", creationDate=" + creationDate +
                '}';
    }

    /**
     * Creates a new Goal
     * @param amount amount
     */
    public Goal(long amount, long creationDate) {
        this.amount = amount;
        this.creationDate = creationDate;
    }

    public Goal(long amount){
        this.amount = amount;
        this.creationDate = System.currentTimeMillis();
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }
}
