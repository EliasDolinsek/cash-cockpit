package com.dolinsek.elias.cashcockpit;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dolinsek.elias.cashcockpit.components.BankAccount;
import com.dolinsek.elias.cashcockpit.components.Bill;
import com.dolinsek.elias.cashcockpit.components.Database;
import com.dolinsek.elias.cashcockpit.components.PrimaryCategory;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Calendar;


/**
 * A simple {@link Fragment} subclass.
 */
public class CategoriesStatisticsFragment extends Fragment {

    private static final int STEP_ONE_MONTH_FORWARD = 1;
    private static final int STEP_ONE_MONTH_BACKWARD = -1;

    private FloatingActionButton fbtnBack, fbtnForward;
    private TextView txvCurrentMonth;
    private RecyclerView rvCategories;
    private PieChart pcStatistics;
    private LinearLayout llNotEnoughDataForStatistic;

    private PrimaryCategoryItemAdapter primaryCategoryItemAdapter;
    private long timestampOfCurrentDisplayedMonth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflatedView = inflater.inflate(R.layout.fragment_categories_statistics, container, false);

        timestampOfCurrentDisplayedMonth = System.currentTimeMillis();

        fbtnBack = (FloatingActionButton) inflatedView.findViewById(R.id.fbtn_categories_statistics_back);
        fbtnForward = (FloatingActionButton) inflatedView.findViewById(R.id.fbtn_categories_statistics_forward);
        txvCurrentMonth = (TextView) inflatedView.findViewById(R.id.txv_categories_statistics_current_month);
        rvCategories = (RecyclerView) inflatedView.findViewById(R.id.rv_categories_statistics);
        pcStatistics = (PieChart) inflatedView.findViewById(R.id.pc_categories_statistics);
        llNotEnoughDataForStatistic = (LinearLayout) inflatedView.findViewById(R.id.ll_categorise_statistics_not_enough_data);

        setupChartStatistics();
        loadCurrentMonthText();
        loadRecyclerViewAdapter();
        loadChartStatistics();
        manageViews();

        rvCategories.setLayoutManager(new LinearLayoutManager(getContext()));

        fbtnForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeMonthAndReloadData(STEP_ONE_MONTH_FORWARD);
            }
        });

        fbtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeMonthAndReloadData(STEP_ONE_MONTH_BACKWARD);
            }
        });

        return inflatedView;
    }

    private void changeMonthAndReloadData(int monthsToStep){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestampOfCurrentDisplayedMonth);
        calendar.add(Calendar.MONTH, monthsToStep);

        timestampOfCurrentDisplayedMonth = calendar.getTimeInMillis();

        loadRecyclerViewAdapter();
        loadCurrentMonthText();
        loadChartStatistics();
        manageViews();
    }

    private void loadRecyclerViewAdapter(){
        primaryCategoryItemAdapter = PrimaryCategoryItemAdapter.getCategoriesStatisticsPrimaryCategoryItemAdapter(Database.getPrimaryCategories(), timestampOfCurrentDisplayedMonth);
        rvCategories.setAdapter(primaryCategoryItemAdapter);
    }

    private void loadCurrentMonthText(){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestampOfCurrentDisplayedMonth);

        int monthInCalendar = calendar.get(Calendar.MONTH);
        String month = getResources().getStringArray(R.array.months_array)[monthInCalendar];
        String year = String.valueOf(calendar.get(Calendar.YEAR));

        txvCurrentMonth.setText(month + " " + year);
    }

    private void loadChartStatistics(){
        PieDataSet pieDataSet = new PieDataSet(getChartStatisticsData(), "");
        pieDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        pieDataSet.setDrawValues(false);
        pieDataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);

        PieData pieData = new PieData();
        pieData.addDataSet(pieDataSet);

        pcStatistics.setData(pieData);
        pcStatistics.invalidate(); //Refreshes data
    }

    private void setupChartStatistics(){
        Description description = new Description();
        description.setText("");

        pcStatistics.setDescription(description);
        pcStatistics.setUsePercentValues(true);
        pcStatistics.setEntryLabelTextSize(17f);
        pcStatistics.setEntryLabelColor(getResources().getColor(R.color.colorPrimary));
        pcStatistics.invalidate(); //Refreshes data
    }

    private ArrayList<PieEntry> getChartStatisticsData(){
        ArrayList<PieEntry> entries = new ArrayList<>();
        for (PrimaryCategory primaryCategory:Database.getPrimaryCategories()){
            ArrayList<Bill> billsOfPrimaryCategory = getBillsOfPrimaryCategory(primaryCategory);
            ArrayList<Bill> filteredBillsOfPrimaryCategory = filterBillsToMonth(billsOfPrimaryCategory, timestampOfCurrentDisplayedMonth);

            long amountOfBills = getTotalAmountOfBills(filteredBillsOfPrimaryCategory);
            if (amountOfBills != 0){
                entries.add(new PieEntry(amountOfBills, primaryCategory.getName()));
            }
        }

        return entries;
    }

    private long getTotalAmountOfBills(ArrayList<Bill> bills){
        long totalAmount = 0;
        for (Bill bill:bills){
            if (bill.getType() == Bill.TYPE_INPUT){
                totalAmount -= bill.getAmount();
            } else {
                totalAmount += bill.getAmount();
            }
        }

        return totalAmount;
    }

    private ArrayList<Bill> filterBillsToMonth(ArrayList<Bill> billsToFilter, long timestampOfMonth){
        ArrayList<Bill> filteredBills = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestampOfMonth);

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);

        for (Bill bill:billsToFilter){
            calendar.setTimeInMillis(bill.getCreationDate());

            int currentYear = calendar.get(Calendar.YEAR);
            int currentMonth = calendar.get(Calendar.MONTH);

            if (currentYear == year && currentMonth == month){
                filteredBills.add(bill);
            }
        }

        return filteredBills;
    }

    private ArrayList<Bill> getBillsOfPrimaryCategory(PrimaryCategory primaryCategory){
        ArrayList<Bill> bills = new ArrayList<>();

        for (BankAccount bankAccount:Database.getBankAccounts()){
            for (Bill bill:bankAccount.getBills()){
                if (bill.getSubcategory().getPrimaryCategory().equals(primaryCategory)){
                    bills.add(bill);
                }
            }
        }

        return bills;
    }

    private void manageViews(){
        ArrayList<Bill> allBillsInDatabase = new ArrayList<>();
        for (PrimaryCategory primaryCategory:Database.getPrimaryCategories()){
            allBillsInDatabase.addAll(getBillsOfPrimaryCategory(primaryCategory));
        }

        ArrayList<Bill> allBillsInDatabaseOfMonth = filterBillsToMonth(allBillsInDatabase, timestampOfCurrentDisplayedMonth);
        if (allBillsInDatabaseOfMonth.size() == 0){
            llNotEnoughDataForStatistic.setVisibility(View.VISIBLE);
            pcStatistics.setVisibility(View.GONE);
        } else {
            llNotEnoughDataForStatistic.setVisibility(View.GONE);
            pcStatistics.setVisibility(View.VISIBLE);
        }
    }
}
