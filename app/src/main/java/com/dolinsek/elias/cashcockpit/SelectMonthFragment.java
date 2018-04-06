package com.dolinsek.elias.cashcockpit;


import android.graphics.PorterDuff;
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
    private boolean selectLastItemAfterCreate;
    private AdapterView.OnItemSelectedListener onItemSelectedListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View inflatedView = inflater.inflate(R.layout.fragment_select_month, container, false);
        spnSelectMonth = inflatedView.findViewById(R.id.spn_select_month);

        if (timeStampsOfDates != null){
            setupSpinner();
        }

        if (selectLastItemAfterCreate){
            int lastIndexOfItems = timeStampsOfDates.length - 1;
            setSpinnerSelection(lastIndexOfItems);
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
        ArrayAdapter<String> filterItems = new ArrayAdapter<String>(getContext(), R.layout.costum_spinner_layout, getTimeStampsAsReadableString());
        filterItems.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spnSelectMonth.getBackground().setColorFilter(getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_ATOP);
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

    public void setSpinnerSelection(int index){
        spnSelectMonth.setSelection(index, true);
    }

    public boolean isSelectLastItemAfterCreate() {
        return selectLastItemAfterCreate;
    }

    public void setSelectLastItemAfterCreate(boolean selectLastItemAfterCreate) {
        this.selectLastItemAfterCreate = selectLastItemAfterCreate;
    }
}
