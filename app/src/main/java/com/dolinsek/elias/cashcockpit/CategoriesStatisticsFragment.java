package com.dolinsek.elias.cashcockpit;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.chip.ChipGroup;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dolinsek.elias.cashcockpit.components.Bill;
import com.dolinsek.elias.cashcockpit.components.Database;
import com.dolinsek.elias.cashcockpit.components.PrimaryCategory;
import com.dolinsek.elias.cashcockpit.components.Toolkit;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;

import java.util.ArrayList;
import java.util.Calendar;


/**
 * A simple {@link Fragment} subclass.
 */
public class CategoriesStatisticsFragment extends Fragment {

    private static final String EXTRA_SELECTED_MONTH_INDEX = "selected_month";
    private static final int STEP_ONE_MONTH_FORWARD = 1;
    private static final int NO_BILL_TYPE_SELECTED = -1;
    private static final String EXTRA_SELECTED_BILL_TYPE = "bill_type";

    private static final int[] COLORS = new int[]{
            Color.parseColor("#e57373"),
            Color.parseColor("#ffd54f"),
            Color.parseColor("#ba68c8"),
            Color.parseColor("#9575cd"),
            Color.parseColor("#7986cb"),
            Color.parseColor("#64b5f6"),
            Color.parseColor("#4fc3f7"),
            Color.parseColor("#4dd0e1"),
            Color.parseColor("#4db6ac"),
            Color.parseColor("#81c784"),
            Color.parseColor("#aed581"),
            Color.parseColor("#dce775"),
            Color.parseColor("#fff176"),
            Color.parseColor("#f06292"),
            Color.parseColor("#ffb74d"),
            Color.parseColor("#ff8a65"),
            Color.parseColor("#a1887f"),
            Color.parseColor("#e0e0e0"),
            Color.parseColor("#90a4ae")
    };


    private RecyclerView rvCategories;
    private PieChart pcStatistics;
    private ChipGroup cgMonthSelection, cgBillTypeSelection;

    private PrimaryCategoryItemAdapter primaryCategoryItemAdapter;
    private ArrayList<Bill> billsToUse;
    private ArrayList<Long> timeStampsWithBills;
    private int selectedMonth, selectedBillType = NO_BILL_TYPE_SELECTED;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflatedView = inflater.inflate(R.layout.fragment_categories_statistics, container, false);
        rvCategories = (RecyclerView) inflatedView.findViewById(R.id.rv_categories_statistics);
        pcStatistics = (PieChart) inflatedView.findViewById(R.id.pc_categories_statistics);

        cgMonthSelection = inflatedView.findViewById(R.id.cg_categories_statistics_month_selection);
        cgBillTypeSelection = inflatedView.findViewById(R.id.cg_categories_statistics_bill_type_selection);

        timeStampsWithBills = getTimeStampsWithBills();
        billsToUse = Toolkit.getAllBills();

        if (enoughDataForStatistic()){
            setupCgBillTypeSelection();
            setupChartStatistics();

            if (savedInstanceState != null){
                cgMonthSelection.removeAllViews();
                setupCgMonthSelection();
                loadDataFromSavedInstanceState(savedInstanceState);
            } else {
                cgMonthSelection.removeAllViews();
                setupCgMonthSelection();
                Toolkit.ActivityToolkit.checkChipOfChipGroup(cgMonthSelection, cgMonthSelection.getChildCount() - 1);
            }
        } else {
            setupForNotEnoughData(inflatedView);
        }

        rvCategories.setLayoutManager(new LinearLayoutManager(getContext()));
        return inflatedView;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(EXTRA_SELECTED_MONTH_INDEX, selectedMonth);
        outState.putInt(EXTRA_SELECTED_BILL_TYPE, selectedBillType);
    }

    private void setupForNotEnoughData(View inflatedView){
        inflatedView.findViewById(R.id.txv_categories_statistics_no_data).setVisibility(View.VISIBLE);
        inflatedView.findViewById(R.id.sv_categories_statistics_content).setVisibility(View.GONE);
    }

    private void loadDataFromSavedInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null){
            selectedMonth = savedInstanceState.getInt(EXTRA_SELECTED_MONTH_INDEX, 0);
            selectedBillType = savedInstanceState.getInt(EXTRA_SELECTED_BILL_TYPE, NO_BILL_TYPE_SELECTED);

            Toolkit.ActivityToolkit.checkChipOfChipGroup(cgMonthSelection, selectedMonth);
            if (selectedBillType != NO_BILL_TYPE_SELECTED){
                Toolkit.ActivityToolkit.checkChipOfChipGroup(cgBillTypeSelection, selectedBillType);
            }
        }
    }

    private boolean enoughDataForStatistic() {
        return Database.getPrimaryCategories().size() != 0 && Toolkit.getAllBills().size() != 0 && Toolkit.getBillsByMonth(System.currentTimeMillis()).size() != 0;
    }

    private void setupCgMonthSelection(){
        Toolkit.ActivityToolkit.addTimeChipsToChipGroup(timeStampsWithBills, cgMonthSelection, getContext());
        cgMonthSelection.setOnCheckedChangeListener((chipGroup, i) -> {
            int selectedChipIndex = Toolkit.ActivityToolkit.getIndexOfSelectedChipInChipGroup(cgMonthSelection);
            if (selectedChipIndex != Toolkit.ActivityToolkit.NO_CHIP_SELECTED){
                selectedMonth = selectedChipIndex;
                loadStatistics();
            } else {
                chipGroup.getChildAt(selectedMonth).performClick();
            }
        });
    }

    private void setupCgBillTypeSelection(){
        cgBillTypeSelection.setOnCheckedChangeListener((chipGroup, i) -> {
            switch (Toolkit.ActivityToolkit.getIndexOfSelectedChipInChipGroup(cgBillTypeSelection)){
                case 0: selectedBillType = 0; billsToUse = Toolkit.getBillsByType(Bill.TYPE_INPUT); break;
                case 1: selectedBillType = 1; billsToUse = Toolkit.getBillsByType(Bill.TYPE_OUTPUT); break;
                default: selectedBillType = NO_BILL_TYPE_SELECTED; billsToUse = Toolkit.getAllBills(); break;
            }

            loadStatistics();
        });
    }

    private void loadStatistics(){
        loadRecyclerViewAdapter();
        loadChartStatistics();
    }

    private void loadRecyclerViewAdapter(){
        primaryCategoryItemAdapter = PrimaryCategoryItemAdapter.getCategoriesStatisticsPrimaryCategoryItemAdapter(Database.getPrimaryCategories(), billsToUse, timeStampsWithBills.get(selectedMonth));
        rvCategories.setAdapter(primaryCategoryItemAdapter);
    }

    private void loadChartStatistics(){
        PieDataSet pieDataSet = new PieDataSet(getChartStatisticsData(), "");
        setupPieDataSet(pieDataSet);

        if (pieDataSet.getEntryCount() == 0){
            pcStatistics.setVisibility(View.GONE);
        } else {
            pcStatistics.setVisibility(View.VISIBLE);
        }

        PieData pieData = new PieData();
        pieData.addDataSet(pieDataSet);

        pcStatistics.setData(pieData);
        pcStatistics.invalidate(); //Refreshes data
    }

    private void setupPieDataSet(PieDataSet pieDataSet){
        pieDataSet.setColors(COLORS);
        pieDataSet.setValueTextColor(getResources().getColor(android.R.color.black));
        pieDataSet.setValueTextSize(15f);
        pieDataSet.setValueFormatter(new PercentFormatter());
    }

    private void setupChartStatistics(){
        pcStatistics.setDescription(null);
        pcStatistics.setHoleRadius(70f);
        pcStatistics.setUsePercentValues(true);
        pcStatistics.setDrawEntryLabels(false);
        pcStatistics.getLegend().setEnabled(true);
        pcStatistics.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        pcStatistics.getLegend().setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        pcStatistics.getLegend().setTextSize(14f);
        pcStatistics.setExtraOffsets(0,-4,0,-4);

        pcStatistics.invalidate();
    }

    private ArrayList<PieEntry> getChartStatisticsData(){
        ArrayList<PieEntry> entries = new ArrayList<>();
        for (PrimaryCategory primaryCategory:Database.getPrimaryCategories()){
            ArrayList<Bill> billsOfMonth = Toolkit.filterBillsByMonth(billsToUse, timeStampsWithBills.get(selectedMonth));
            ArrayList<Bill> billsOfCategoryAndMonth = Toolkit.filterBillsByCategory(billsOfMonth, primaryCategory);

            long amountOfBills = Toolkit.getBillsTotalAmount(billsOfCategoryAndMonth);
            if (amountOfBills != 0){
                entries.add(new PieEntry(amountOfBills, primaryCategory.getName()));
            }
        }

        return entries;
    }

    private ArrayList<Long> getTimeStampsWithBills(){
        ArrayList<Long> monthsWithBills = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(getTimeStampOfCreationDateOfFirstBillInDatabase());

        while (!Toolkit.doesMonthExceedsCurrentTime(calendar)){
            long currentMonthTimesStamp = calendar.getTimeInMillis();
            ArrayList<Bill> billsOfMonth = Toolkit.getBillsByMonth(currentMonthTimesStamp);

            if (billsOfMonth.size() != 0){
                monthsWithBills.add(currentMonthTimesStamp);
            }

            calendar.add(Calendar.MONTH, STEP_ONE_MONTH_FORWARD);
        }

        return monthsWithBills;
    }

    private long getTimeStampOfCreationDateOfFirstBillInDatabase(){
        long firstCreationDate = System.currentTimeMillis();

        ArrayList<Bill> billsInDatabase = Toolkit.getAllBills();
        for (Bill bill:billsInDatabase){
            if (bill.getCreationDate() < firstCreationDate){
                firstCreationDate = bill.getCreationDate();
            }
        }

        return firstCreationDate;
    }
}
