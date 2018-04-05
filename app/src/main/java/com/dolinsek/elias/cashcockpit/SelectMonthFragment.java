package com.dolinsek.elias.cashcockpit;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Calendar;


/**
 * A simple {@link Fragment} subclass.
 */
public class SelectMonthFragment extends Fragment {

    private Spinner spnSelectMonth;
    private long[] timeStampsOfDates;
    private AdapterView.OnItemSelectedListener onItemSelectedListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View inflatedView = inflater.inflate(R.layout.fragment_select_month, container, false);
        spnSelectMonth = inflatedView.findViewById(R.id.spn_select_month);

        if (timeStampsOfDates != null){
            setupSpinner();
        }

        return inflatedView;
    }

    private ArrayList<String> getTimeStampsAsReadableString(){
        Calendar calendar = Calendar.getInstance();
        ArrayList<String> formattedTimeStamps = new ArrayList<>();

        for (long timeStamp:timeStampsOfDates){
            calendar.setTimeInMillis(timeStamp);

            int monthOfTimeStamp = calendar.get(Calendar.MONTH);
            String formattedMonth = getResources().getStringArray(R.array.months_array)[monthOfTimeStamp];

            formattedTimeStamps.add(formattedMonth + " " + calendar.get(Calendar.YEAR));
        }

        return formattedTimeStamps;
    }

    private void setupSpinner(){
        ArrayAdapter<String> filterItems = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, getTimeStampsAsReadableString());
        filterItems.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spnSelectMonth.setAdapter(filterItems);
        if (onItemSelectedListener != null){
            spnSelectMonth.setOnItemSelectedListener(onItemSelectedListener);
        }
    }

    public void setOnItemSelectedListener(AdapterView.OnItemSelectedListener onItemSelectedListener) {
        this.onItemSelectedListener = onItemSelectedListener;
    }

    public void setTimeStampsOfDates(long[] timeStampsOfDates) {
        this.timeStampsOfDates = timeStampsOfDates;
    }

}
