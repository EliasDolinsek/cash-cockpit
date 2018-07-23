package com.dolinsek.elias.cashcockpit;

import android.content.Context;

import com.dolinsek.elias.cashcockpit.components.Currency;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

/**
 * Created by Elias Dolinsek on 20.04.2018 for cash-cockpit.
 */
public class CurrencyEntryValueFormatter implements IValueFormatter{

    Context context;

    public CurrencyEntryValueFormatter(Context context){
        this.context = context;
    }

    @Override
    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
        long formattedLongValue = ((long) value) * 100;
        return Currency.getActiveCurrency(context).formatAmountToReadableStringWithoutCentsWithCurrencySymbol(formattedLongValue);
    }
}
