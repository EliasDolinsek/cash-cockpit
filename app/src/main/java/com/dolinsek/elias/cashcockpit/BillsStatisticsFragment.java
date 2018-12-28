package com.dolinsek.elias.cashcockpit;


import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.chip.Chip;
import android.support.design.chip.ChipGroup;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.dolinsek.elias.cashcockpit.components.Bill;
import com.dolinsek.elias.cashcockpit.components.Toolkit;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;
import java.util.Calendar;


public class BillsStatisticsFragment extends Fragment {

    private static final int DISPLAY_BILL_USAGE_TYPE_OVERALL = 172;
    private static final int DISPLAY_BILL_USAGE_TYPE_SELECTED_MONTH = 862;
    private static final String EXTRA_SELECTED_MONTH = "selected_month";

    private PieChart pcUsageOfBillTypes;
    private BarChart bcHistoryOfPayments;
    private LinearLayout llBillTypeOverallUsageTextsContainer, llBillTypeSelectedMonthUsageTextsContainer;
    private View vwSeparationOne, vwSeparationTwo;

    private ChipGroup cgMonthSelection;
    private Chip chipInputOverall, chipOutputOverall, chipInputMonth, chipOutputMonth;

    private ArrayList<Long> timeStampsWithBills;
    private int selectedMonth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflatedView = inflater.inflate(R.layout.fragment_bills_statistics, container, false);

        cgMonthSelection = inflatedView.findViewById(R.id.cg_bills_statistics_month_selection);
        pcUsageOfBillTypes = inflatedView.findViewById(R.id.pc_bills_statistics_bill_type_usage);
        bcHistoryOfPayments = inflatedView.findViewById(R.id.bc_bills_statistics_history_of_payments);

        llBillTypeOverallUsageTextsContainer = inflatedView.findViewById(R.id.ll_bills_statistics_bill_type_overall_texts_container);
        llBillTypeSelectedMonthUsageTextsContainer = inflatedView.findViewById(R.id.ll_bills_statistics_bill_type_usage_selected_month_texts_container);

        vwSeparationOne = inflatedView.findViewById(R.id.vw_bills_statistics_separation_one);
        vwSeparationTwo = inflatedView.findViewById(R.id.vw_bills_statistics_separation_two);

        chipInputOverall = inflatedView.findViewById(R.id.chip_bills_statistics_input_usage_overall);
        chipOutputOverall = inflatedView.findViewById(R.id.chip_bills_statistics_output_usage_overall);
        chipInputMonth = inflatedView.findViewById(R.id.chip_bills_statistics_input_usage_month);
        chipOutputMonth = inflatedView.findViewById(R.id.chip_bills_statistics_output_usage_month);

        loadTimeStampsWithBills();
        if (enoughDataForStatistic()){
            setupBillTypeUsageChart();
            setupHistoryOfPaymentsChart();
            displayBillTypeUsage(Toolkit.getAllBills(), DISPLAY_BILL_USAGE_TYPE_OVERALL);

            if (savedInstanceState != null){
                cgMonthSelection.removeAllViews();
                setupCgMonthSelection();
                loadDataFromSavedInstanceState(savedInstanceState);
            } else {
                selectedMonth = timeStampsWithBills.size() -1;
                displayStatistics(timeStampsWithBills.get(selectedMonth));
                setupCgMonthSelection();
                Toolkit.ActivityToolkit.checkChipOfChipGroup(cgMonthSelection, cgMonthSelection.getChildCount() - 1);
            }
        } else {

        }

        return inflatedView;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(EXTRA_SELECTED_MONTH, selectedMonth);
    }

    private void loadDataFromSavedInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null){
            selectedMonth = savedInstanceState.getInt(EXTRA_SELECTED_MONTH, 0);
            displayStatistics(timeStampsWithBills.get(selectedMonth));
            Toolkit.ActivityToolkit.checkChipOfChipGroup(cgMonthSelection, selectedMonth);
        }
    }

    private boolean enoughDataForStatistic() {
        return Toolkit.getAllBills().size() != 0 && timeStampsWithBills.size() != 0;
    }


    private void setupCgMonthSelection(){
        Toolkit.ActivityToolkit.addTimeChipsToChipGroup(timeStampsWithBills, cgMonthSelection, getContext());
        cgMonthSelection.setOnCheckedChangeListener((chipGroup, i) -> {
            selectedMonth = Toolkit.ActivityToolkit.getIndexOfSelectedChipInChipGroup(chipGroup);
            displayStatistics(timeStampsWithBills.get(selectedMonth));
        });
    }

    private void displayStatistics(long timeStamp){
        loadBillTypeUsageStatistic(timeStamp);
        loadPaymentsHistory(timeStamp);
        displayBillTypeUsage(Toolkit.getBillsByMonth(timeStamp), DISPLAY_BILL_USAGE_TYPE_SELECTED_MONTH);
    }

    private void setupBillTypeUsageChart(){
        pcUsageOfBillTypes.setDescription(null);
        pcUsageOfBillTypes.setHoleRadius(70f);
        pcUsageOfBillTypes.setDrawEntryLabels(false);
        pcUsageOfBillTypes.getLegend().setEnabled(true);
        pcUsageOfBillTypes.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        pcUsageOfBillTypes.getLegend().setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        pcUsageOfBillTypes.getLegend().setTextSize(14f);
        pcUsageOfBillTypes.setExtraOffsets(0,-4,0,-4);

        pcUsageOfBillTypes.invalidate();
    }

    private void setupHistoryOfPaymentsChart(){
        bcHistoryOfPayments.getAxisRight().setEnabled(false);
        bcHistoryOfPayments.getAxisLeft().setEnabled(false);
        bcHistoryOfPayments.getXAxis().setDrawGridLines(false);
        bcHistoryOfPayments.getXAxis().setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        bcHistoryOfPayments.getXAxis().setTextSize(14f);

        bcHistoryOfPayments.getLegend().setEnabled(false);
        bcHistoryOfPayments.getDescription().setEnabled(false);


        XAxis xAxis = bcHistoryOfPayments.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new HistoryOfPaymentsXAxisValueFormatter());
    }

    private void setupPieDataSet(PieDataSet pieDataSet, ArrayList<PieEntry> entries){
        boolean areInputsGreaterThanNull = isUsageOfBillTypeGreaterThanNullFromEntries(entries, Bill.TYPE_INPUT), areOutputsGreaterThanNull = isUsageOfBillTypeGreaterThanNullFromEntries(entries, Bill.TYPE_OUTPUT);

        setupPieDataSetColorsDependingOnAvailableData(pieDataSet, areInputsGreaterThanNull, areOutputsGreaterThanNull);
        pieDataSet.setDrawValues(false);
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
        barData.setValueTextSize(14f);
    }

    private void setupPieDataSetColorsDependingOnAvailableData(PieDataSet pieDataSet, boolean amountOfInputsGreaterThanNull, boolean amountOfOutputGreaterThanNull){
        ArrayList<Integer> colors = new ArrayList<>();

        if (amountOfInputsGreaterThanNull){
            colors.add(getResources().getColor(R.color.colorBillTypeInput));
        }

        if (amountOfOutputGreaterThanNull){
            colors.add(getResources().getColor(R.color.colorBillTypeOutput));
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

        int allBillsSize = inputTypeBills.size() + outputTypeBills.size();
        int usageOfInputTypesInPercent = getPercentOfBillUsage(allBillsSize, inputTypeBills.size());
        int usageOfOutputTypesInPercent = getPercentOfBillUsage(allBillsSize, outputTypeBills.size());

        if (usageOfInputTypesInPercent != 0){
            pieEntries.add(usageOfBillTypeToPieEntry(usageOfInputTypesInPercent, Bill.TYPE_INPUT));
        }

        if(usageOfOutputTypesInPercent != 0){
            pieEntries.add(usageOfBillTypeToPieEntry(usageOfOutputTypesInPercent, Bill.TYPE_OUTPUT));
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
        timeStampsWithBills = getTimeStampsWithBills(Toolkit.getAllBills());
    }

    private PieEntry usageOfBillTypeToPieEntry(int usageOfBillTypeInPercent, int billType) {
        return new PieEntry(usageOfBillTypeInPercent, billTypeToString(billType));
    }

    private void displayBillTypeUsage(ArrayList<Bill> bills, int type){
        int billsTypeInputSize = Toolkit.filterBillsByType(bills, Bill.TYPE_INPUT).size();
        int billsTypeOutputSize = Toolkit.filterBillsByType(bills, Bill.TYPE_OUTPUT).size();

        int usageInputPercent = getPercentOfBillUsage(bills.size(), billsTypeInputSize);
        int usageOutputPercent = getPercentOfBillUsage(bills.size(), billsTypeOutputSize);

        String textInput = billUsageToReadableString(Bill.TYPE_INPUT, billsTypeInputSize, usageInputPercent);
        String textOutput = billUsageToReadableString(Bill.TYPE_OUTPUT, billsTypeOutputSize, usageOutputPercent);

        if (type == DISPLAY_BILL_USAGE_TYPE_OVERALL){
            chipInputOverall.setText(textInput);
            chipOutputOverall.setText(textOutput);
        } else if (type == DISPLAY_BILL_USAGE_TYPE_SELECTED_MONTH){
            chipInputMonth.setText(textInput);
            chipOutputMonth.setText(textOutput);
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
        } else {
            throw new IllegalArgumentException("Couldn't resolve " + billType + " as a bill type");
        }
    }

    private int getPercentOfBillUsage(int allBillsSize, int billTypeSize){
        return (int) Math.round((100.0 / allBillsSize * billTypeSize));
    }

    private ArrayList<Long> getTimeStampsWithBills(ArrayList<Bill> bills){
        ArrayList<Long> monthsWithBills = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        long firstCreationDateOfBill = getCreationDateOfFirstAddedBillBill(bills);
        calendar.setTimeInMillis(firstCreationDateOfBill);

        while (!Toolkit.doesMonthExceedsCurrentTime(calendar)){
            long currentMonthTimesStamp = calendar.getTimeInMillis();
            ArrayList<Bill> billsOfMonth = Toolkit.getBillsByMonth(currentMonthTimesStamp);

            if (billsOfMonth.size() != 0){
                monthsWithBills.add(currentMonthTimesStamp);
            }

            calendar.add(Calendar.MONTH, 1);
        }

        return monthsWithBills;
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