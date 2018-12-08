package com.dolinsek.elias.cashcockpit;


import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dolinsek.elias.cashcockpit.components.Bill;
import com.dolinsek.elias.cashcockpit.components.Database;
import com.dolinsek.elias.cashcockpit.components.Toolkit;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class BillsStatisticsFragment extends Fragment {

    private static final int DISPLAY_BILL_USAGE_TYPE_OVERALL = 172;
    private static final int DISPLAY_BILL_USAGE_TYPE_SELECTED_MONTH = 862;

    private SelectMonthFragment selectMonthFragment;
    private LinearLayout llSelectMonthFragment;
    private PieChart pcUsageOfBillTypes;
    private BarChart bcHistoryOfPayments;
    private LinearLayout llBillTypeOverallUsageTextsContainer, llBillTypeSelectedMonthUsageTextsContainer;
    private View vwSeparationOne, vwSeparationTwo;
    private NotEnoughDataFragment fgmNotEnoughData;

    private TextView txvBillsTypeInputUsageMonth, txvBillsTypeOutputUsageMonth, txvBillsTypeTransferUsageMonth;
    private TextView txvBillsTypeInputUsageOverall, txvBillsTypeOutputUsageOverall, txvBillsTypeTransferUsageOverall;
    private long[] timeStampsWithBills;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflatedView = inflater.inflate(R.layout.fragment_bills_statistics, container, false);
        loadTimeStampsWithBills();

        llSelectMonthFragment = inflatedView.findViewById(R.id.ll_bills_statistics_select_month_fragment_container);
        pcUsageOfBillTypes = inflatedView.findViewById(R.id.pc_bills_statistics_bill_type_usage);
        bcHistoryOfPayments = inflatedView.findViewById(R.id.bc_bills_statistics_history_of_payments);
        fgmNotEnoughData = (NotEnoughDataFragment) getChildFragmentManager().findFragmentById(R.id.fgm_bills_statistics_not_enough_data);

        llBillTypeOverallUsageTextsContainer = inflatedView.findViewById(R.id.ll_bills_statistics_bill_type_overall_texts_container);
        llBillTypeSelectedMonthUsageTextsContainer = inflatedView.findViewById(R.id.ll_bills_statistics_bill_type_usage_selected_month_texts_container);

        vwSeparationOne = inflatedView.findViewById(R.id.vw_bills_statistics_separation_one);
        vwSeparationTwo = inflatedView.findViewById(R.id.vw_bills_statistics_separation_two);

        txvBillsTypeInputUsageMonth = inflatedView.findViewById(R.id.txv_bills_statistics_bills_type_input_usage_month);
        txvBillsTypeOutputUsageMonth = inflatedView.findViewById(R.id.txv_bills_statistics_bills_type_output_usage_month);
        txvBillsTypeTransferUsageMonth = inflatedView.findViewById(R.id.txv_bills_statistics_bills_type_transfer_usage_month);

        txvBillsTypeInputUsageOverall = inflatedView.findViewById(R.id.txv_bills_statistics_bills_type_input_usage_overall);
        txvBillsTypeOutputUsageOverall = inflatedView.findViewById(R.id.txv_bills_statistics_bills_type_output_usage_overall);
        txvBillsTypeTransferUsageOverall = inflatedView.findViewById(R.id.txv_bills_statistics_bills_type_transfer_usage_overall);

        ArrayList<Bill> allBillsInDatabase = Toolkit.getAllBills();
        if (allBillsInDatabase.size() != 0){
            displayBillsTypeUsage(allBillsInDatabase, DISPLAY_BILL_USAGE_TYPE_OVERALL);
            setupBillTypeUsageChart();
            setupHistoryOfPaymentsChart();
            fgmNotEnoughData.hide();
        } else {
            vwSeparationOne.setVisibility(View.GONE);
            vwSeparationTwo.setVisibility(View.GONE);
            llSelectMonthFragment.setVisibility(View.GONE);
            pcUsageOfBillTypes.setVisibility(View.GONE);
            bcHistoryOfPayments.setVisibility(View.GONE);
            hideBillTypeUsageContainers();
            fgmNotEnoughData.show();
        }

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

    private void displayStatisticsIfEnoughData(int sizeOfBills){
        if (sizeOfBills == 0){
            hideBillTypeUsageContainers();

        } else {
            showBillTypeUsageContainers();
        }
    }

    private void hideBillTypeUsageContainers(){
        llBillTypeOverallUsageTextsContainer.setVisibility(View.GONE);
        llBillTypeSelectedMonthUsageTextsContainer.setVisibility(View.GONE);
    }

    private void showBillTypeUsageContainers(){
        llBillTypeOverallUsageTextsContainer.setVisibility(View.VISIBLE);
        llBillTypeSelectedMonthUsageTextsContainer.setVisibility(View.VISIBLE);
    }
    private void setupSelectMonthFragment(){
        selectMonthFragment = new SelectMonthFragment();
        selectMonthFragment.setTimeStampsOfDates(timeStampsWithBills);
        selectMonthFragment.setSelectLastItemAfterCreate(true);
        selectMonthFragment.setOnItemSelectedListener(getOnMonthSelectedItemListener());

        displaySelectMonthFragment();
    }

    private void setupBillTypeUsageChart(){
        pcUsageOfBillTypes.setDescription(null);
        pcUsageOfBillTypes.setUsePercentValues(true);
        pcUsageOfBillTypes.setDrawEntryLabels(false);
        pcUsageOfBillTypes.setHoleRadius(70f);
        pcUsageOfBillTypes.setExtraOffsets(2f,2f,2f,-8f);
        pcUsageOfBillTypes.invalidate();
    }

    private void setupHistoryOfPaymentsChart(){
        bcHistoryOfPayments.getAxisRight().setEnabled(false);
        bcHistoryOfPayments.getLegend().setEnabled(false);
        bcHistoryOfPayments.getDescription().setEnabled(true);
        bcHistoryOfPayments.getDescription().setText(getString(R.string.label_added_bills_history));
        bcHistoryOfPayments.getDescription().setTextSize(14f);
        bcHistoryOfPayments.getDescription().setYOffset(4f);

        XAxis xAxis = bcHistoryOfPayments.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new HistoryOfPaymentsXAxisValueFormatter());
    }

    private void setupPieDataSet(PieDataSet pieDataSet, ArrayList<PieEntry> entries){
        boolean areInputsGreaterThanNull = isUsageOfBillTypeGreaterThanNullFromEntries(entries, Bill.TYPE_INPUT), areOutputsGreaterThanNull = isUsageOfBillTypeGreaterThanNullFromEntries(entries, Bill.TYPE_OUTPUT),
                areTransfersGreaterThanNull = isUsageOfBillTypeGreaterThanNullFromEntries(entries, Bill.TYPE_TRANSFER);

        setupPieDataSetColorsDependingOnAvailableData(pieDataSet, areTransfersGreaterThanNull, areInputsGreaterThanNull, areOutputsGreaterThanNull);
        pieDataSet.setValueTextSize(15f);
        pieDataSet.setSliceSpace(3f);
        pieDataSet.setValueLineWidth(2f);
        pieDataSet.setValueTextColor(getResources().getColor(android.R.color.white));
        pieDataSet.setValueFormatter(new PercentFormatter());
    }

    private boolean isUsageOfBillTypeGreaterThanNullFromEntries(ArrayList<PieEntry> entries, int billType){
        for (PieEntry entry:entries){
            String billTypeAsString = billTypeToString(billType);
            if (billTypeAsString.equals(entry.getLabel())){
                return entry.getValue() != 0;
            }
        }

        return false;
    }

    private void setupBarData(BarData barData){
        barData.setValueFormatter(new HistoryOfPaymentsValueFormatter());
        barData.setValueTextSize(15f);
    }

    private void setupPieDataSetColorsDependingOnAvailableData(PieDataSet pieDataSet, boolean amountOfTransferGreaterThanNull, boolean amountOfInputsGreaterThanNull, boolean amountOfOutputGreaterThanNull){
        ArrayList<Integer> colors = new ArrayList<>();

        if (amountOfInputsGreaterThanNull){
            colors.add(getResources().getColor(R.color.colorBillTypeInput));
        }

        if (amountOfOutputGreaterThanNull){
            colors.add(getResources().getColor(R.color.colorBillTypeOutput));
        }

        if (amountOfTransferGreaterThanNull){
            colors.add(getResources().getColor(R.color.colorBillTypeTransfer));
        }

        pieDataSet.setColors(colors);
    }

    private void loadPaymentsHistory(long timeStampOfMonth){
        ArrayList<Bill> billsOfSelectedMonth = Toolkit.getBillsByMonth(timeStampOfMonth);
        ArrayList<BarEntry> barEntries = billsToBarEntryForDailyStatistic(billsOfSelectedMonth, timeStampOfMonth);

        BarDataSet barDataSet = new BarDataSet(barEntries, "");
        barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);

        BarData barData = new BarData(barDataSet);
        setupBarData(barData);

        bcHistoryOfPayments.setData(barData);
        bcHistoryOfPayments.invalidate();
    }

    private void loadBillTypeUsageStatistic(long timeStampToLoadStatistics){
        ArrayList<Bill> billsOfMonth =  Toolkit.getBillsByMonth(timeStampToLoadStatistics);
        loadUsageOfBillTypeChart(
                getBillsAsPieEntriesForBillUsageStatistics(billsOfMonth)
        );
    }

    private ArrayList<PieEntry> getBillsAsPieEntriesForBillUsageStatistics(ArrayList<Bill> bills){
        ArrayList<PieEntry> pieEntries = new ArrayList<>();

        ArrayList<Bill> inputTypeBills = Toolkit.filterBillsByType(bills, Bill.TYPE_INPUT);
        ArrayList<Bill> outputTypeBills = Toolkit.filterBillsByType(bills, Bill.TYPE_OUTPUT);
        ArrayList<Bill> transferTypeBills = Toolkit.filterBillsByType(bills, Bill.TYPE_TRANSFER);

        int allBillsSize = inputTypeBills.size() + outputTypeBills.size() + transferTypeBills.size();
        int usageOfInputTypesInPercent = getPercentOfBillUsage(allBillsSize, inputTypeBills.size());
        int usageOfOutputTypesInPercent = getPercentOfBillUsage(allBillsSize, outputTypeBills.size());
        int usageOfTransferTypesInPercent = getPercentOfBillUsage(allBillsSize, transferTypeBills.size());

        if (usageOfInputTypesInPercent != 0){
            pieEntries.add(usageOfBillTypeToPieEntry(usageOfInputTypesInPercent, Bill.TYPE_INPUT));
        }

        if(usageOfOutputTypesInPercent != 0){
            pieEntries.add(usageOfBillTypeToPieEntry(usageOfOutputTypesInPercent, Bill.TYPE_OUTPUT));
        }

        if (usageOfTransferTypesInPercent != 0){
            pieEntries.add(usageOfBillTypeToPieEntry(usageOfTransferTypesInPercent, Bill.TYPE_TRANSFER));
        }

        return pieEntries;
    }

    private void loadUsageOfBillTypeChart(ArrayList<PieEntry> entries){
        PieDataSet pieDataSet = new PieDataSet(entries, "");
        setupPieDataSet(pieDataSet, entries);

        PieData pieData = new PieData();
        pieData.setDataSet(pieDataSet);

        pcUsageOfBillTypes.setData(pieData);
        pcUsageOfBillTypes.invalidate();
    }

    private void loadTimeStampsWithBills(){
        timeStampsWithBills = arrayListToLongArray(getTimeStampsWithBills(Toolkit.getAllBills()));
    }

    private PieEntry usageOfBillTypeToPieEntry(int usageOfBillTypeInPercent, int billType){
        PieEntry pieEntry = new PieEntry(usageOfBillTypeInPercent);
        String billTypeAsString = billTypeToString(billType);
        pieEntry.setLabel(billTypeAsString);

        return pieEntry;
    }

    private void displayBillsTypeUsage(ArrayList<Bill> bills, int type){
        int billsTypeInputSize = Toolkit.filterBillsByType(bills, Bill.TYPE_INPUT).size();
        int billsTypeOutputSize = Toolkit.filterBillsByType(bills, Bill.TYPE_OUTPUT).size();
        int billsTypeTransferSize = Toolkit.filterBillsByType(bills, Bill.TYPE_TRANSFER).size();

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

    private ArrayList<BarEntry> billsToBarEntryForDailyStatistic(ArrayList<Bill> bills, long timeStampOfMonth){
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();

        calendar.setTimeInMillis(timeStampOfMonth);
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        int startMonth = calendar.get(Calendar.MONTH);
        int index = 0;
        while (calendar.get(Calendar.MONTH) == startMonth){
            ArrayList<Bill> billsOfDay = filterBillsOfDay(bills, calendar.getTimeInMillis());
            if (billsOfDay.size() != 0){
                barEntries.add(billsToBarEntry(billsOfDay, index));
            } else {
                barEntries.add(new BarEntry(index,0));
            }

            index++;
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        return barEntries;
    }

    private ArrayList<Bill> filterBillsOfDay(ArrayList<Bill> bills, long timeStampOfDay){
        ArrayList<Bill> filteredBills = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeStampOfDay);
        int year = calendar.get(Calendar.YEAR), month = calendar.get(Calendar.MONTH), day = calendar.get(Calendar.DATE);

        for (Bill bill:bills){
            calendar.setTimeInMillis(bill.getCreationDate());
            int currentYear = calendar.get(Calendar.YEAR), currentMonth = calendar.get(Calendar.MONTH), currentDay = calendar.get(Calendar.DATE);

            if (year == currentYear && month == currentMonth && day == currentDay){
                filteredBills.add(bill);
            }
        }

        return filteredBills;
    }

    private BarEntry billsToBarEntry(ArrayList<Bill> bills, int x){
        return billsSizeToBarEntry(bills.size(), x);
    }

    private BarEntry billsSizeToBarEntry(int size, int x){
        return new BarEntry(x, size);
    }

    private String billUsageToReadableString(int billType, int billsOfTypeSize, int usageOfTypeInPercent){
        String billTypeAsString = Toolkit.getBillTypeAsString(billType, getContext());
        return billTypeAsString + ": " + billsOfTypeSize + " (" + usageOfTypeInPercent + "%)";
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

    private void displaySelectMonthFragment(){
        getFragmentManager().beginTransaction().add(R.id.ll_bills_statistics_select_month_fragment_container, selectMonthFragment).commit();
    }

    private ArrayList<Long> getTimeStampsWithBills(ArrayList<Bill> bills){
        ArrayList<Long> monthsWithBills = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        long firstCreationDateOfBill = getCreationDateOfFirstAddedBillBill(bills);
        calendar.setTimeInMillis(firstCreationDateOfBill);

        while (!doesMonthExceedsCurrentTime(calendar)){
            long currentMonthTimesStamp = calendar.getTimeInMillis();
            ArrayList<Bill> billsOfMonth = Toolkit.getBillsByMonth(currentMonthTimesStamp);

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

        return currentYear < calendar.get(Calendar.YEAR) && currentMonth < calendar.get(Calendar.MONTH);
    }

    private long[] arrayListToLongArray(ArrayList<Long> arrayList){
        long[] longsToReturn = new long[arrayList.size()];

        for (int i = 0; i<longsToReturn.length; i++){
            longsToReturn[i] = arrayList.get(i);
        }

        return longsToReturn;
    }

    private AdapterView.OnItemSelectedListener getOnMonthSelectedItemListener(){
        return new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                long selectionTimeStamp = timeStampsWithBills[position];
                ArrayList<Bill> billsOfSelectedMonth = Toolkit.getBillsByMonth(selectionTimeStamp);

                loadBillTypeUsageStatistic(selectionTimeStamp);
                loadPaymentsHistory(selectionTimeStamp);

                displayBillsTypeUsage(billsOfSelectedMonth, DISPLAY_BILL_USAGE_TYPE_SELECTED_MONTH);
                displayStatisticsIfEnoughData(billsOfSelectedMonth.size());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };
    }

    public static long getCreationDateOfFirstAddedBillBill(ArrayList<Bill> bills){
        long firstCreationDate = System.currentTimeMillis();
        for (Bill bill:bills){
            if (bill.getCreationDate() < firstCreationDate){
                firstCreationDate = bill.getCreationDate();
            }
        }

        return firstCreationDate;
    }

    private static class HistoryOfPaymentsValueFormatter implements IValueFormatter {

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            if (entry.getY() == 0){
                return "";
            } else {
                return String.valueOf(Math.round(entry.getY()));
            }
        }
    }

    private static class HistoryOfPaymentsXAxisValueFormatter implements IAxisValueFormatter{

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            return String.valueOf(Math.round(value + 1) + ".");
        }
    }

}