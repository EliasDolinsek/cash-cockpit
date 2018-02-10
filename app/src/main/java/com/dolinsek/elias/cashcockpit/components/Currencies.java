package com.dolinsek.elias.cashcockpit.components;

/**
 * Created by elias on 10.01.2018.
 */

public enum Currencies {

    EURO (new Currency("Euro", "€", Currency.ALIGNMENT_RIGHT)), DOLLAR (new Currency("Dollar", "$", Currency.ALIGNMENT_RIGHT)), POUND (new Currency("Pund", "£", Currency.ALIGHNMENT_LEFT));

    private Currency currency;

    private Currencies(Currency currency){
        this.currency = currency;
    }

    public Currency getCurrency(){
        return currency;
    }
}
