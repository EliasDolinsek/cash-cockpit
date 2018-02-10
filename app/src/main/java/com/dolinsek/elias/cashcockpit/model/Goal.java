package com.dolinsek.elias.cashcockpit.model;

/**
 * Created by elias on 06.01.2018.
 */

public class Goal {

    private long amount; //In Cents

    public Goal(long amount) {
        this.amount = amount;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }
}
