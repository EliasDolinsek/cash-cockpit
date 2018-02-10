package com.dolinsek.elias.cashcockpit.components;


/**
 * Created by elias on 10.01.2018.
 */

public class Currency {

    public static final int ALIGNMENT_RIGHT = 0;
    public static final int ALIGHNMENT_LEFT = 1;

    //Name of the currency
    private String name;
    //Symbol of the currency
    private String symbol;
    //Alignment of the symbol of the currency
    private int alignment;

    public Currency(String name, String symbol, int alignment) {
        this.name = name;
        this.symbol = symbol;
        this.alignment = alignment;
    }

    public String format(long amount){

        int cents = (int) (amount % 100);
        long euro = (amount / 100);

        String centsString = String.valueOf(cents);
        if(cents < 10){
            centsString = centsString + "0";
        }

        if(alignment == ALIGHNMENT_LEFT){
            return symbol + String.valueOf(euro) + "." + centsString;
        } else {
            return String.valueOf(euro) + "." + centsString + "€";
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public int getAlignment() {
        return alignment;
    }

    public void setAlignment(int alignment) {
        this.alignment = alignment;
    }
}
