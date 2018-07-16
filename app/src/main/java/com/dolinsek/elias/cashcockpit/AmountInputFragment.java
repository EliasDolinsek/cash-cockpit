package com.dolinsek.elias.cashcockpit;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.dolinsek.elias.cashcockpit.components.Currency;


/**
 * A simple {@link Fragment} subclass.
 */
public class AmountInputFragment extends Fragment {


    private EditText edtAmount;
    private TextView txvCurrencyShortcut;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflatedView = inflater.inflate(R.layout.fragment_amount_input, container, false);

        edtAmount = inflatedView.findViewById(R.id.edt_amount_input_amount);
        txvCurrencyShortcut = inflatedView.findViewById(R.id.txv_amount_input_active_currency_shortcut);

        Currency activeCurrency = Currency.getActiveCurrency(getContext());
        TextWatcher currencyTextWatcher = activeCurrency.getCurrencyTextWatcher(edtAmount);
        edtAmount.addTextChangedListener(currencyTextWatcher);

        String currencyShortcut = activeCurrency.getCurrencyShortcut();
        txvCurrencyShortcut.setText(currencyShortcut);

        return inflatedView;
    }

    public String getEnteredAmountAsString(){
        return edtAmount.getText().toString();
    }

    public long getEnteredAmountAsLong(){
        return (long) (Double.valueOf(getEnteredAmountAsString()) * 100);
    }

    public EditText getEdtAmount() {
        return edtAmount;
    }

    public TextView getTxvCurrencyShortcut() {
        return txvCurrencyShortcut;
    }
}
