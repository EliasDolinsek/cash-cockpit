package com.dolinsek.elias.cashcockpit;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.design.chip.ChipGroup;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;

import com.dolinsek.elias.cashcockpit.components.BankAccount;
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
import java.util.Random;


/**
 * A simple {@link Fragment} subclass.
 */
public class CategoriesStatisticsFragment extends Fragment {

    private static final String EXTRA_TIME_STAMP_OF_MONTH = "timeStampOfMonth";
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

    private static final int STEP_ONE_MONTH_FORWARD = 1;

    private RecyclerView rvCategories;
    private PieChart pcStatistics;
    private NestedScrollView scrollView;
    private ChipGroup cgMonthSelection, cgBillTypeSelection;

    private PrimaryCategoryItemAdapter primaryCategoryItemAdapter;
    private ArrayList<Bill> billsToUse;
    private long timestampOfCurrentDisplayedMonth;
    private ArrayList<Long> timeStampsWithBills;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflatedView = inflater.inflate(R.layout.fragment_categories_statistics, container, false);

        loadTimeStampOfMonth(savedInstanceState);

        rvCategories = (RecyclerView) inflatedView.findViewById(R.id.rv_categories_statistics);
        pcStatistics = (PieChart) inflatedView.findViewById(R.id.pc_categories_statistics);
        scrollView = inflatedView.findViewById(R.id.sv_categories_statistics);

        cgMonthSelection = inflatedView.findViewById(R.id.cg_categories_statistics_month_selection);
        cgBillTypeSelection = inflatedView.findViewById(R.id.cg_categories_statistics_bill_type_selection);

        timeStampsWithBills = getTimeStampsWithBills();
        billsToUse = getAllBillsInDatabase();

        setupCgBillTypeSelection();
        setupCgMonthSelection();

        setupChartStatistics();
        manageViews();

        rvCategories.setLayoutManager(new LinearLayoutManager(getContext()));

        loadStatistics();

        return inflatedView;
    }

    private void setupCgMonthSelection(){
        Toolkit.ActivityToolkit.addTimeChipsToChipGroup(timeStampsWithBills, cgMonthSelection, getContext());
        cgMonthSelection.setOnCheckedChangeListener((chipGroup, i) -> {
            timestampOfCurrentDisplayedMonth = timeStampsWithBills.get(Toolkit.ActivityToolkit.getIndexOfSelectedChipInChipGroup(cgMonthSelection));
            loadRecyclerViewAdapter();
            loadChartStatistics();
        });
    }

    private void setupCgBillTypeSelection(){
        cgBillTypeSelection.setOnCheckedChangeListener((chipGroup, i) -> {
            switch (cgBillTypeSelection.getCheckedChipId()){
                case R.id.chip_categories_statistics_input: billsToUse = Toolkit.filterBillsByType(Toolkit.getAllBills(), Bill.TYPE_INPUT);
                case R.id.chip_categories_statistics_output: billsToUse = Toolkit.filterBillsByType(Toolkit.getAllBills(), Bill.TYPE_OUTPUT);
            }

            loadStatistics();
        });
    }

    private void loadStatistics(){
        loadRecyclerViewAdapter();
        loadChartStatistics();
    }

    private void loadRecyclerViewAdapter(){
        primaryCategoryItemAdapter = PrimaryCategoryItemAdapter.getCategoriesStatisticsPrimaryCategoryItemAdapter(Database.getPrimaryCategories(), billsToUse, timestampOfCurrentDisplayedMonth);
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
            ArrayList<Bill> billsOfMonth = Toolkit.getBillsByMonth(timestampOfCurrentDisplayedMonth);
            ArrayList<Bill> billsOfCategoryAndMonth = Toolkit.filterBillsByCategory(billsOfMonth, primaryCategory);

            long amountOfBills = Toolkit.getBillsTotalAmount(billsOfCategoryAndMonth);
            if (amountOfBills != 0){
                entries.add(new PieEntry(amountOfBills, primaryCategory.getName()));
            }
        }

        return entries;
    }

    private void manageViews(){
        if (getAllBillsInDatabase().size() == 0){
            pcStatistics.setVisibility(View.GONE);
        } else {
            pcStatistics.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(EXTRA_TIME_STAMP_OF_MONTH, timestampOfCurrentDisplayedMonth);
    }

    private void loadTimeStampOfMonth(Bundle savedInstanceState){
        if (savedInstanceState != null){
            timestampOfCurrentDisplayedMonth = savedInstanceState.getLong(EXTRA_TIME_STAMP_OF_MONTH);
        } else {
            timestampOfCurrentDisplayedMonth = System.currentTimeMillis();
        }
    }

    private ArrayList<Long> getTimeStampsWithBills(){
        ArrayList<Long> monthsWithBills = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(getTimeStampOfCreationDateOfFirstBillInDatabase());

        while (!Toolkit.doesMonthExceedsCurrentTime(calendar)){
            long currentMonthTimesStamp = calendar.getTimeInMillis();
            ArrayList<Bill> billsOfMonth = getBillsOfMonth(currentMonthTimesStamp);

            if (billsOfMonth.size() != 0){
                monthsWithBills.add(currentMonthTimesStamp);
            }

            calendar.add(Calendar.MONTH, STEP_ONE_MONTH_FORWARD);
        }

        return monthsWithBills;
    }

    private long getTimeStampOfCreationDateOfFirstBillInDatabase(){
        long firstCreationDate = System.currentTimeMillis();

        ArrayList<Bill> billsInDatabase = getAllBillsInDatabase();
        for (Bill bill:billsInDatabase){
            if (bill.getCreationDate() < firstCreationDate){
                firstCreationDate = bill.getCreationDate();
            }
        }

        return firstCreationDate;
    }

    private ArrayList<Bill> getAllBillsInDatabase(){
        ArrayList<Bill> allBillsInDatabase = new ArrayList<>();

        for (BankAccount bankAccount:Database.getBankAccounts()){
            for (Bill bill:bankAccount.getBills()){
                allBillsInDatabase.add(bill);
            }
        }

        return allBillsInDatabase;
    }

    private ArrayList<Bill> getBillsOfMonth(long timeStampOfMonth){
        ArrayList<Bill> billsOfMonth = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeStampOfMonth);

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);

        for (Bill bill:getAllBillsInDatabase()){
            calendar.setTimeInMillis(bill.getCreationDate());

            int billYear = calendar.get(Calendar.YEAR);
            int billMonth = calendar.get(Calendar.MONTH);

            if (year == billYear && month == billMonth){
                billsOfMonth.add(bill);
            }
        }

        return  billsOfMonth;
    }
}
