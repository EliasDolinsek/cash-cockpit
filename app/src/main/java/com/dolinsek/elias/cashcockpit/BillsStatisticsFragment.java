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
    private RecyclerView rvBillsOfSelectedMonth;
    private CardView cvNotEnoughData, cvCategoryUsageStatisticContainer, cvDetailsContainer, cvHistoryChartContainer;

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
        rvBillsOfSelectedMonth = inflatedView.findViewById(R.id.rv_bills_statistics_bills_of_month);

        cvNotEnoughData = inflatedView.findViewById(R.id.cv_bills_statistics_not_enough_data);
        cvCategoryUsageStatisticContainer = inflatedView.findViewById(R.id.cv_bills_statistics_category_usage_statistic_container);
        cvDetailsContainer = inflatedView.findViewById(R.id.cv_bills_statistics_details_container);
        cvHistoryChartContainer = inflatedView.findViewById(R.id.cv_bills_statistics_history_chart_container);

        txvBillsTypeInputUsageMonth = inflatedView.findViewById(R.id.txv_bills_statistics_bills_type_input_usage_month);
        txvBillsTypeOutputUsageMonth = inflatedView.findViewById(R.id.txv_bills_statistics_bills_type_output_usage_month);
        txvBillsTypeTransferUsageMonth = inflatedView.findViewById(R.id.txv_bills_statistics_bills_type_transfer_usage_month);

        txvBillsTypeInputUsageOverall = inflatedView.findViewById(R.id.txv_bills_statistics_bills_type_input_usage_overall);
        txvBillsTypeOutputUsageOverall = inflatedView.findViewById(R.id.txv_bills_statistics_bills_type_output_usage_overall);
        txvBillsTypeTransferUsageOverall = inflatedView.findViewById(R.id.txv_bills_statistics_bills_type_transfer_usage_overall);

        rvBillsOfSelectedMonth.setLayoutManager(new LinearLayoutManager(getContext()));
        rvBillsOfSelectedMonth.setHasFixedSize(false);

        if (Database.Toolkit.getAllBillsInDatabase().size() != 0){
            displayBillsTypeUsage(Database.Toolkit.getAllBillsInDatabase(), DISPLAY_BILL_USAGE_TYPE_OVERALL);
            setupBillTypeUsageChart();
            setupHistoryOfPaymentsChart();
        } else {
            cvNotEnoughData.setVisibility(View.VISIBLE);
            llSelectMonthFragment.setVisibility(View.GONE);
            cvCategoryUsageStatisticContainer.setVisibility(View.GONE);
            cvDetailsContainer.setVisibility(View.GONE);
            cvHistoryChartContainer.setVisibility(View.GONE);
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
            cvNotEnoughData.setVisibility(View.VISIBLE);
            cvCategoryUsageStatisticContainer.setVisibility(View.GONE);
            cvDetailsContainer.setVisibility(View.GONE);
            cvHistoryChartContainer.setVisibility(View.GONE);

        } else {
            cvNotEnoughData.setVisibility(View.GONE);
            cvCategoryUsageStatisticContainer.setVisibility(View.VISIBLE);
            cvDetailsContainer.setVisibility(View.VISIBLE);
            cvHistoryChartContainer.setVisibility(View.VISIBLE);
        }
    }
    private void setupSelectMonthFragment(){
        selectMonthFragment = new SelectMonthFragment();
        selectMonthFragment.setTimeStampsOfDates(timeStampsWithBills);
        selectMonthFragment.setSelectLastItemAfterCreate(true);
        selectMonthFragment.setOnItemSelectedListener(getOnMonthSelectedItemListener());

        displaySelectMonthFragment();
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

    private void setupHistoryOfPaymentsChart(){
        bcHistoryOfPayments.getAxisRight().setEnabled(false);
        bcHistoryOfPayments.getLegend().setEnabled(false);
        bcHistoryOfPayments.getDescription().setEnabled(false);

        XAxis xAxis = bcHistoryOfPayments.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new HistoryOfPaymentsXAxisValueFormatter());
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

    private void setupBarData(BarData barData){
        barData.setValueFormatter(new HistoryOfPaymentsValueFormatter());
        barData.setValueTextSize(15f);
    }

    private void setupPieDataSetColors(PieDataSet pieDataSet){
        int[] colors = new int[]{getResources().getColor(R.color.colorGreen), getResources().getColor(android.R.color.holo_red_light), getResources().getColor(R.color.colorOrange)};
        pieDataSet.setColors(colors);
    }

    private void loadPaymentsHistory(long timeStampOfMonth){
        ArrayList<Bill> billsOfSelectedMonth = Database.Toolkit.getBillsOfMonth(timeStampOfMonth);
        ArrayList<BarEntry> barEntries = billsToBarEntryForDailyStatistic(billsOfSelectedMonth, timeStampOfMonth);

        BarDataSet barDataSet = new BarDataSet(barEntries, "");
        barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);

        BarData barData = new BarData(barDataSet);
        setupBarData(barData);

        bcHistoryOfPayments.setData(barData);
        bcHistoryOfPayments.invalidate();
    }

    private void loadBillTypeUsageStatistic(long timeStampToLoadStatistics){
        ArrayList<Bill> billsOfMonth = Database.Toolkit.getBillsOfMonth(timeStampToLoadStatistics);
        loadUsageOfBillTypeChart(
                getBillsAsPieEntriesForBillUsageStatistics(billsOfMonth)
        );
    }

    private ArrayList<PieEntry> getBillsAsPieEntriesForBillUsageStatistics(ArrayList<Bill> bills){
        ArrayList<PieEntry> pieEntries = new ArrayList<>();

        ArrayList<Bill> inputTypeBills = Database.Toolkit.filterBillsOfBillType(bills, Bill.TYPE_INPUT);
        ArrayList<Bill> outputTypeBills = Database.Toolkit.filterBillsOfBillType(bills, Bill.TYPE_OUTPUT);
        ArrayList<Bill> transferTypeBills = Database.Toolkit.filterBillsOfBillType(bills, Bill.TYPE_TRANSFER);

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
        setupPieDataSet(pieDataSet);

        PieData pieData = new PieData();
        pieData.setDataSet(pieDataSet);

        pcUsageOfBillTypes.setData(pieData);
        pcUsageOfBillTypes.invalidate();
    }

    private void loadTimeStampsWithBills(){
        ArrayList<Bill> allBillsInDatabase = Database.Toolkit.getAllBillsInDatabase();
        timeStampsWithBills = arrayListToLongArray(getTimeStampsWithBills(allBillsInDatabase));
    }

    private PieEntry usageOfBillTypeToPieEntry(int usageOfBillTypeInPercent, int billType){
        PieEntry pieEntry = new PieEntry(usageOfBillTypeInPercent);
        String billTypeAsString = billTypeToString(billType);
        pieEntry.setLabel(billTypeAsString);

        return pieEntry;
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

    private AdapterView.OnItemSelectedListener getOnMonthSelectedItemListener(){
        return new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                long selectionTimeStamp = timeStampsWithBills[position];
                ArrayList<Bill> billsOfSelectedMonth = Database.Toolkit.getBillsOfMonth(selectionTimeStamp);

                HistoryItemAdapter historyItemAdapter = HistoryItemAdapter.getBillsStatisticsHisotryItemAdapter(billsOfSelectedMonth);
                rvBillsOfSelectedMonth.setAdapter(historyItemAdapter);

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
