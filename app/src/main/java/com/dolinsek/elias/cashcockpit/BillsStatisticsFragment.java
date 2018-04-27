package com.dolinsek.elias.cashcockpit;


import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dolinsek.elias.cashcockpit.components.Bill;
import com.dolinsek.elias.cashcockpit.components.Database;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class BillsStatisticsFragment extends Fragment {

    private static final int DISPLAY_BILL_USAGE_TYPE_OVERALL = 172;
    private static final int DISPLAY_BILL_USAGE_TYPE_SELECTED_MONTH = 862;

    private SelectMonthFragment selectMonthFragment;
    private LinearLayout llSelectMonthFragment;
    private PieChart pcUsageOfBillTypes;

    private TextView txvBillsTypeInputUsageMonth, txvBillsTypeOutputUsageMonth, txvBillsTypeTransferUsageMonth;
    private TextView txvBillsTypeInputUsageOverall, txvBillsTypeOutputUsageOverall, txvBillsTypeTransferUsageOverall;
    private long[] timeStampsWithBills;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflatedView = inflater.inflate(R.layout.fragment_bills_statistics, container, false);;
        loadTimeStampsWithBills();

        llSelectMonthFragment = inflatedView.findViewById(R.id.ll_bills_statistics_select_month_fragment_container);
        pcUsageOfBillTypes = inflatedView.findViewById(R.id.pc_bills_statistics_bill_type_usage);

        txvBillsTypeInputUsageMonth = inflatedView.findViewById(R.id.txv_bills_statistics_bills_type_input_usage_month);
        txvBillsTypeOutputUsageMonth = inflatedView.findViewById(R.id.txv_bills_statistics_bills_type_output_usage_month);
        txvBillsTypeTransferUsageMonth = inflatedView.findViewById(R.id.txv_bills_statistics_bills_type_transfer_usage_month);

        txvBillsTypeInputUsageOverall = inflatedView.findViewById(R.id.txv_bills_statistics_bills_type_input_usage_overall);
        txvBillsTypeOutputUsageOverall = inflatedView.findViewById(R.id.txv_bills_statistics_bills_type_output_usage_overall);
        txvBillsTypeTransferUsageOverall = inflatedView.findViewById(R.id.txv_bills_statistics_bills_type_transfer_usage_overall);

        displayBillsTypeUsage(Database.Toolkit.getAllBillsInDatabase(), DISPLAY_BILL_USAGE_TYPE_OVERALL);
        setupBillTypeUsageChart();

        return inflatedView;
    }

    @Override
    public void onPause() {
        super.onPause();

        //Removes Fragment because otherwise it would be added twice
        getFragmentManager().beginTransaction().remove(selectMonthFragment).commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        setupSelectMonthFragment();
    }

    private void displayBillsTypeUsage(ArrayList<Bill> bills, int type){
        int billsTypeInputSize = Database.Toolkit.filterBillsOfBillType(bills, Bill.TYPE_INPUT).size();
        int billsTypeOutputSize = Database.Toolkit.filterBillsOfBillType(bills, Bill.TYPE_OUTPUT).size();
        int billsTypeTransferSize = Database.Toolkit.filterBillsOfBillType(bills, Bill.TYPE_TRANSFER).size();

        int usageInputPercent = getPercentOfBillUsage(bills.size(), billsTypeInputSize);
        int usageOutputPercent = getPercentOfBillUsage(bills.size(), billsTypeOutputSize);
        int usageTransferPercent = getPercentOfBillUsage(bills.size(), billsTypeTransferSize);

        String textInput = billUsageToReadableString(Bill.TYPE_INPUT, billsTypeInputSize, usageInputPercent);
        String textOutput = billUsageToReadableString(Bill.TYPE_OUTPUT, billsTypeOutputSize, usageOutputPercent);
        String textTransfer = billUsageToReadableString(Bill.TYPE_TRANSFER, billsTypeTransferSize, usageTransferPercent);

        if (type == DISPLAY_BILL_USAGE_TYPE_OVERALL){
            txvBillsTypeInputUsageOverall.setText(textInput);
            txvBillsTypeOutputUsageOverall.setText(textOutput);
            txvBillsTypeTransferUsageOverall.setText(textTransfer);
        } else if (type == DISPLAY_BILL_USAGE_TYPE_SELECTED_MONTH){
            txvBillsTypeInputUsageMonth.setText(textInput);
            txvBillsTypeOutputUsageMonth.setText(textOutput);
            txvBillsTypeTransferUsageMonth.setText(textTransfer);
        } else {
            throw new IllegalArgumentException("Couldn't resolve " + type + " as a valid type");
        }
    }

    private String billUsageToReadableString(int billType, int billsOfTypeSize, int usageOfTypeInPercent){
        String billTypeAsString = getBillTypeAsString(billType);
        return billTypeAsString + ": " + billsOfTypeSize + " (" + usageOfTypeInPercent + "%)";
    }

    private String getBillTypeAsString(int billType){
        switch (billType){
            case Bill.TYPE_INPUT: return getResources().getString(R.string.label_input);
            case Bill.TYPE_OUTPUT: return getResources().getString(R.string.label_output);
            case Bill.TYPE_TRANSFER: return getResources().getString(R.string.label_transfer);
            default: throw new IllegalArgumentException("Couldn't resolve " + billType + " as a bill-type");
        }
    }

    private void setupBillTypeUsageChart(){
        Description description = new Description();
        description.setText("");
        pcUsageOfBillTypes.setDescription(description);

        pcUsageOfBillTypes.setUsePercentValues(true);
        pcUsageOfBillTypes.setEntryLabelTextSize(17f);
        pcUsageOfBillTypes.setEntryLabelColor(getResources().getColor(R.color.colorPrimary));
        pcUsageOfBillTypes.getLegend().setEnabled(false);
        pcUsageOfBillTypes.invalidate();
    }

    private void setupPieDataSet(PieDataSet pieDataSet){
        setupPieDataSetColors(pieDataSet);
        pieDataSet.setValueTextSize(15f);
        pieDataSet.setValueTextColor(Color.WHITE);
        pieDataSet.setValueLineColor(getResources().getColor(R.color.colorPrimary));
        pieDataSet.setValueLineWidth(2f);
        pieDataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        pieDataSet.setValueFormatter(new PercentFormatter());
    }

    private void setupPieDataSetColors(PieDataSet pieDataSet){
        int[] colors = new int[]{getResources().getColor(R.color.colorGreen), getResources().getColor(android.R.color.holo_red_light), getResources().getColor(R.color.colorOrange)};
        pieDataSet.setColors(colors);
    }

    private void loadBillTypeUsageStatistic(long timeStampToLoadStatistics){
        ArrayList<Bill> billsOfMonth = Database.Toolkit.getBillsOfMonth(timeStampToLoadStatistics);

        ArrayList<Bill> inputTypeBills = Database.Toolkit.filterBillsOfBillType(billsOfMonth, Bill.TYPE_INPUT);
        ArrayList<Bill> outputTypeBills = Database.Toolkit.filterBillsOfBillType(billsOfMonth, Bill.TYPE_OUTPUT);
        ArrayList<Bill> transferTypeBills = Database.Toolkit.filterBillsOfBillType(billsOfMonth, Bill.TYPE_TRANSFER);

        int allBillsSize = inputTypeBills.size() + outputTypeBills.size() + transferTypeBills.size();
        int usageOfInputTypesInPercent = getPercentOfBillUsage(allBillsSize, inputTypeBills.size());
        int usageOfOutputTypesInPercent = getPercentOfBillUsage(allBillsSize, outputTypeBills.size());
        int usageOfTransferTypesInPercent = getPercentOfBillUsage(allBillsSize, transferTypeBills.size());

        ArrayList<PieEntry> pieEntries = new ArrayList<>();
        pieEntries.add(usageOfBillTypeToPieEntry(usageOfInputTypesInPercent, Bill.TYPE_INPUT));
        pieEntries.add(usageOfBillTypeToPieEntry(usageOfOutputTypesInPercent, Bill.TYPE_OUTPUT));
        pieEntries.add(usageOfBillTypeToPieEntry(usageOfTransferTypesInPercent, Bill.TYPE_TRANSFER));

        loadUsageOfBillTypeChart(pieEntries);
    }

    private void loadUsageOfBillTypeChart(ArrayList<PieEntry> entries){
        PieDataSet pieDataSet = new PieDataSet(entries, "");
        setupPieDataSet(pieDataSet);

        PieData pieData = new PieData();
        pieData.setDataSet(pieDataSet);

        pcUsageOfBillTypes.setData(pieData);
        pcUsageOfBillTypes.invalidate();
    }

    private PieEntry usageOfBillTypeToPieEntry(int usageOfBillTypeInPercent, int billType){
        PieEntry pieEntry = new PieEntry(usageOfBillTypeInPercent);
        String billTypeAsString = billTypeToString(billType);
        pieEntry.setLabel(billTypeAsString);

        return pieEntry;
    }

    private String billTypeToString(int billType){
        if (billType == Bill.TYPE_INPUT){
            return getResources().getString(R.string.label_input);
        } else if (billType == Bill.TYPE_OUTPUT){
            return getResources().getString(R.string.label_output);
        } else if (billType == Bill.TYPE_TRANSFER){
            return getResources().getString(R.string.label_transfer);
        } else {
            throw new IllegalArgumentException("Couldn't resolve " + billType + " as a bill type");
        }
    }

    private int getPercentOfBillUsage(int allBillsSize, int billTypeSize){
        return (int) Math.round((100.0 / allBillsSize * billTypeSize));
    }

    private void loadTimeStampsWithBills(){
        ArrayList<Bill> allBillsInDatabase = Database.Toolkit.getAllBillsInDatabase();
        timeStampsWithBills = arrayListToLongArray(getTimeStampsWithBills(allBillsInDatabase));
    }

    private void displaySelectMonthFragment(){
        getFragmentManager().beginTransaction().add(R.id.ll_bills_statistics_select_month_fragment_container, selectMonthFragment).commit();
    }

    private void setupSelectMonthFragment(){
        selectMonthFragment = new SelectMonthFragment();
        selectMonthFragment.setTimeStampsOfDates(timeStampsWithBills);
        selectMonthFragment.setSelectLastItemAfterCreate(true);
        selectMonthFragment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                long selectionTimeStamp = timeStampsWithBills[position];
                ArrayList<Bill> billsOfSelectedMonth = Database.Toolkit.getBillsOfMonth(selectionTimeStamp);

                loadBillTypeUsageStatistic(selectionTimeStamp);
                displayBillsTypeUsage(billsOfSelectedMonth, DISPLAY_BILL_USAGE_TYPE_SELECTED_MONTH);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        displaySelectMonthFragment();
    }

    private ArrayList<Long> getTimeStampsWithBills(ArrayList<Bill> bills){
        ArrayList<Long> monthsWithBills = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        long firstCreationDateOfBill = Database.Toolkit.getCreationDateOfFirstBill(bills);
        calendar.setTimeInMillis(firstCreationDateOfBill);

        while (!doesMonthExceedsCurrentTime(calendar)){
            long currentMonthTimesStamp = calendar.getTimeInMillis();
            ArrayList<Bill> billsOfMonth = Database.Toolkit.getBillsOfMonth(currentMonthTimesStamp);

            if (billsOfMonth.size() != 0){
                monthsWithBills.add(currentMonthTimesStamp);
            }

            calendar.add(Calendar.MONTH, 1);
        }

        return monthsWithBills;
    }

    private boolean doesMonthExceedsCurrentTime(Calendar calendar){
        Calendar currentMonthCalendar = Calendar.getInstance();
        currentMonthCalendar.setTimeInMillis(System.currentTimeMillis());

        int currentYear = currentMonthCalendar.get(Calendar.YEAR);
        int currentMonth = currentMonthCalendar.get(Calendar.MONTH);

        return currentYear < calendar.get(Calendar.YEAR) || currentMonth < calendar.get(Calendar.MONTH);
    }

    private long[] arrayListToLongArray(ArrayList<Long> arrayList){
        long[] longsToReturn = new long[arrayList.size()];

        for (int i = 0; i<longsToReturn.length; i++){
            longsToReturn[i] = arrayList.get(i);
        }

        return longsToReturn;
    }

}
