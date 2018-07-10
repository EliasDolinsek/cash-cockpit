package com.dolinsek.elias.cashcockpit.components;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.support.v7.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.dolinsek.elias.cashcockpit.R;

/**
 * Created by Elias Dolinsek on 21.03.2018 for cash-cockpit.
 */

public class Currency {

    private static final int CURRENCY_INDEX_EURO = 810;
    private static final int CURRENCY_INDEX_DOLLAR = 193;
    private static final int CURRENCY_INDEX_POUND = 54;

    public static final String CURRENCY_SYMBOL_EURO = "€";
    public static final String CURRENCY_SYMBOL_DOLLAR = "$";
    public static final String CURRENCY_SYMBOL_POUND = "£";

    public static final String CURRENCY_SHORTCUT_EURO = "EUR";
    public static final String CURRENCY_SHORTCUT_DOLLAR = "USD";
    public static final String CURRENCY_SHORTCUT_POUND = "GBP";

    private String currencySymbol, currencyShortcut;
    private int currencyIndex;

    public Currency(){

    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public int getCurrencyIndex() {
        return currencyIndex;
    }

    private void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

    private void setCurrencyIndex(int currencyIndex) {
        this.currencyIndex = currencyIndex;
    }

    public String getCurrencyShortcut() {
        return currencyShortcut;
    }

    public void setCurrencyShortcut(String currencyShortcut) {
        this.currencyShortcut = currencyShortcut;
    }

    public String formatAmountToReadableString(long amount){
        if (getCurrencyIndex() == CURRENCY_INDEX_EURO){
            return formatAmountToReadableStringCurrencyEuro(amount);
        } else if (getCurrencyIndex() == CURRENCY_INDEX_DOLLAR){
            return formatAmountToReadableStringCurrencyDollar(amount);
        } else if (getCurrencyIndex() == CURRENCY_INDEX_POUND){
            return formatAmountToReadableStringCurrencyPound(amount);
        } else {
            throw new Resources.NotFoundException("Couldn't find currency with index " + getCurrencyIndex());
        }
    }

    public String formatAmountToReadableStringWithCurrencySymbol(long amount){
        return formatAmountToReadableString(amount) + getCurrencySymbol();
    }

    public static Currency getActiveCurrency(Context context){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int activeCurrency = Integer.valueOf(sharedPreferences.getString("preference_currency", "1"));

        if (activeCurrency == 1){
            return getEuroCurrency();
        } else if(activeCurrency == 2){
            return getDollarCurrency();
        } else if(activeCurrency == 3){
            return getPoundCurrency();
        } else {
            throw new Resources.NotFoundException("Couldn't find an active currency!");
        }
    }

    public static Currency getEuroCurrency(){
        Currency euroCurrency = new Currency();

        euroCurrency.setCurrencySymbol(CURRENCY_SYMBOL_EURO);
        euroCurrency.setCurrencyIndex(CURRENCY_INDEX_EURO);
        euroCurrency.setCurrencyShortcut(CURRENCY_SHORTCUT_EURO);

        return euroCurrency;
    }

    public static Currency getDollarCurrency(){
        Currency dollarCurrency = new Currency();

        dollarCurrency.setCurrencySymbol(CURRENCY_SYMBOL_DOLLAR);
        dollarCurrency.setCurrencyIndex(CURRENCY_INDEX_DOLLAR);
        dollarCurrency.setCurrencyShortcut(CURRENCY_SHORTCUT_DOLLAR);

        return dollarCurrency;
    }

    public static Currency getPoundCurrency() {
        Currency poundCurrency = new Currency();

        poundCurrency.setCurrencySymbol(CURRENCY_SYMBOL_POUND);
        poundCurrency.setCurrencyIndex(CURRENCY_INDEX_POUND);
        poundCurrency.setCurrencyShortcut(CURRENCY_SHORTCUT_POUND);

        return poundCurrency;
    }

    public static Currency getCurrencyByIndex(int currencyIndex){
        if (currencyIndex == CURRENCY_INDEX_EURO){
            return getEuroCurrency();
        } else if (currencyIndex == CURRENCY_INDEX_DOLLAR){
            return getDollarCurrency();
        } else if (currencyIndex == CURRENCY_INDEX_POUND){
            return getPoundCurrency();
        } else {
            throw new Resources.NotFoundException("Couldn't find currency with this index: " + currencyIndex);
        }
    }

    public static TextWatcher getCurrencyTextWatcherByCurrencyIndex(final int currencyIndex, final EditText editText){
        return new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String input = charSequence.toString();

                if (currencyIndex == CURRENCY_INDEX_EURO){
                    manageTextForCurrencyEuro(editText, input);
                } else if (currencyIndex == CURRENCY_INDEX_DOLLAR){
                    manageTextForCurrencyDollar(editText, input);
                } else if (currencyIndex == CURRENCY_INDEX_POUND){
                    manageTextForCurrencyPound(editText, input);
                } else {
                    throw new Resources.NotFoundException("Couldn't find Currency by Currency-ID");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };
    }

    public TextWatcher getCurrencyTextWatcher(EditText editText){
        return getCurrencyTextWatcherByCurrencyIndex(this.getCurrencyIndex(), editText);
    }

    private static void manageTextForCurrencyEuro(EditText editText, String input){
        int allowedLengthAfterComma = 2;

        if (input.contains(".") && getLengthOfNumbersAfterComma(input) > allowedLengthAfterComma){
            editText.setText(removeLastCharacterFromString(input));
            editText.setSelection(input.length() - 1);
        }
    }

    private static void manageTextForCurrencyPound(EditText editText, String input){
        int allowedLengthAfterComma = 2;

        if (input.contains(".") && getLengthOfNumbersAfterComma(input) > allowedLengthAfterComma){
            editText.setText(removeLastCharacterFromString(input));
            editText.setSelection(input.length() - 1);
        }
    }

    private static void manageTextForCurrencyDollar(EditText editText, String input){
        int allowedLengthAfterComma = 2;

        if (input.contains(".") && getLengthOfNumbersAfterComma(input) > allowedLengthAfterComma){
            editText.setText(removeLastCharacterFromString(input));
            editText.setSelection(input.length() - 1);
        }
    }

    private static int getLengthOfNumbersAfterComma(String input){
        String convertedInput = input.substring(input.indexOf(".") + 1);
        return convertedInput.length();
    }

    private static String removeLastCharacterFromString(String input){
        return input.substring(0, input.length() - 1);
    }

    private static String formatAmountToReadableStringCurrencyEuro(long amount){
        long euros = amount / 100;
        long cents = Math.abs(amount % 100);

        String result;
        result = String.valueOf(euros) + "." + String.valueOf(cents);
        result = addZeroIfCentsAreLessThanTen(result);
        return result;
    }

    private static String formatAmountToReadableStringCurrencyDollar(long amount){
        long dollar = amount / 100;
        long cents = Math.abs(amount % 100);

        String result;
        result = String.valueOf(dollar) + "." + String.valueOf(cents);
        result = addZeroIfCentsAreLessThanTen(result);

        return result;
    }

    private static String formatAmountToReadableStringCurrencyPound(long amount){
        long pounds = amount / 100;
        long cents = Math.abs(amount % 100);

        String result;
        result = String.valueOf(pounds) + "." + String.valueOf(cents);
        result = addZeroIfCentsAreLessThanTen(result);

        return result;
    }

    private static String addZeroIfCentsAreLessThanTen(String input){
        String amountBeforeComma = input.substring(0, input.indexOf("."));
        String cents = getCentsOutOfAmount(input);
        long centsAsLong = Long.valueOf(cents);

        if (centsAsLong < 10){
            return amountBeforeComma + ".0" + cents;
        } else {
            return input;
        }
    }

    private static String getCentsOutOfAmount(String amount){
        return amount.substring(amount.indexOf(".") + 1, amount.length());
    }
}
