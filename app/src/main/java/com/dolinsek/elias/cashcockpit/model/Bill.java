package com.dolinsek.elias.cashcockpit.model;

/**
 * Created by elias on 06.01.2018.
 */

public class Bill {

    public static final int TPYE_OUTPUT = 0;
    public static final int TYPE_INPUT = 1;
    public static final int TYPE_TRANSFER = 2;

    private long amount; //In cents
    private String description;
    private Subcategory subcategory;
    private long creationDate;

    public Bill(long amount, String description, Subcategory subcategory, long creationDate) {
        this.amount = amount;
        this.description = description;
        this.subcategory = subcategory;
        this.creationDate = creationDate;
    }

    public Bill(long amount, String description, Subcategory subcategory) {
        this.amount = amount;
        this.description = description;
        this.subcategory = subcategory;
        creationDate = System.currentTimeMillis();
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String descriptions) {
        this.description = descriptions;
    }

    public Subcategory getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(Subcategory subcategory) {
        this.subcategory = subcategory;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }
}
