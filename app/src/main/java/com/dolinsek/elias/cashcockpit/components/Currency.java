package com.dolinsek.elias.cashcockpit.components;


import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.dolinsek.elias.cashcockpit.R;

/**
 * Created by elias on 10.01.2018.
 */

public abstract class Currency {

    private static final String EURO_SYMBOL = "€";
    private static final String DOLLAR_SYMBOL = "$";
    private static final String POUND_SYMBOL = "£";

    private String symbol;
    protected Currency(String symbol){
        this.symbol = symbol;
    }

    public abstract String formatAmountToString(long amount);

    public String getSymbol() {
        return symbol;
    }

    public static class Factory {

        public static Currency getActiveCurrency(Context context){
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            String currency = sharedPreferences.getString("preference_currency", context.getResources().getString(R.string.euro));

            if(currency.equals(context.getResources().getString(R.string.euro)))
                return getEuroCurrency();
            else if(currency.equals(context.getResources().getString(R.string.dollar)))
                return getDollarCurrency();
            else
                return getPoundCurrency();
        }

        public static Currency getEuroCurrency(){
            Currency currency = new Currency(EURO_SYMBOL) {
                @Override
                public String formatAmountToString(long amount) {
                    long euros = amount / 100, cents = amount % 100;

                    String amountInString = String.valueOf(euros);
                    amountInString = amountInString + "." + cents;

                    if(cents % 10 == 0)
                        amountInString = amountInString + "0";

                    amountInString = amountInString + EURO_SYMBOL;
                    return amountInString;
                }
            };

            return currency;
        }

        public static Currency getDollarCurrency(){
            Currency currency = new Currency(DOLLAR_SYMBOL) {
                @Override
                public String formatAmountToString(long amount) {
                    long dollars = amount / 100, cents = amount % 100;

                    String amountInString = String.valueOf(dollars);
                    amountInString = amountInString + "." + cents;

                    if(cents % 10 == 0)
                        amountInString = amountInString + "0";

                    amountInString = DOLLAR_SYMBOL + amountInString;
                    return amountInString;
                }
            };

            return currency;
        }

        public static Currency getPoundCurrency(){
            Currency currency = new Currency(POUND_SYMBOL) {
                @Override
                public String formatAmountToString(long amount) {
                    long pounds = amount / 100, cents = amount % 100;

                    String amountInString = String.valueOf(pounds);
                    amountInString = amountInString + "." + cents;

                    if(cents % 10 == 0)
                        amountInString = amountInString + "0";

                    amountInString = POUND_SYMBOL + amountInString;
                    return amountInString;
                }
            };

            return currency;
        }

        public static TextWatcher getCurrencyTextWatcher(final EditText editText){
            return new TextWatcher() {
                public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                    String text = arg0.toString();
                    if (text.contains(".") && text.substring(text.indexOf(".") + 1).length() > 2) {
                        editText.setText(text.substring(0, text.length() - 1));
                        editText.setSelection(editText.getText().length());
                    }
                }

                public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

                }

                public void afterTextChanged(Editable arg0) {
                }
            };
        }
    }
}
