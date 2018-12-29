package com.dolinsek.elias.cashcockpit;

import android.os.Bundle;
import android.support.design.chip.ChipGroup;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dolinsek.elias.cashcockpit.components.Bill;
import com.dolinsek.elias.cashcockpit.components.Database;
import com.dolinsek.elias.cashcockpit.components.Goal;
import com.dolinsek.elias.cashcockpit.components.PrimaryCategory;
import com.dolinsek.elias.cashcockpit.components.Toolkit;

import java.util.ArrayList;
import java.util.Calendar;


/**
 * A simple {@link Fragment} subclass.
 */
public class GoalsStatisticsFragment extends Fragment {

    private static final int STEP_ONE_MONTH_FORWARD = 1;

    private static final String EXTRA_SELECTED_MONTH = "month";

    private RecyclerView mRvCategories;
    private ProgressBar mPgbMonth, mPgbAverage;
    private TextView mTxvMonth, mTxvAverage;
    private LinearLayout mLLContent;
    private ChipGroup cgMonthSelection;
    private PrimaryCategoryItemAdapter primaryCategoryItemAdapter = PrimaryCategoryItemAdapter.getGoalsStatisticsPrimaryCategoryItemAdapter(Database.getPrimaryCategories(), System.currentTimeMillis());
    private ArrayList<Long> timeStampsWithStatistics;
    private int selectedMonth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflatedView = inflater.inflate(R.layout.fragment_goals_statistics, container, false);

        mRvCategories = (RecyclerView) inflatedView.findViewById(R.id.rv_goals_statistics_categories);
        mRvCategories.setLayoutManager(new LinearLayoutManager(getContext()));

        mPgbMonth = (ProgressBar) inflatedView.findViewById(R.id.pgb_goals_statistics_month);
        mPgbAverage = (ProgressBar) inflatedView.findViewById(R.id.pgb_goals_statistics_average);

        mTxvMonth = (TextView) inflatedView.findViewById(R.id.txv_goals_statistics_month);
        mTxvAverage = (TextView) inflatedView.findViewById(R.id.txv_goals_statistics_average);

        mLLContent = (LinearLayout) inflatedView.findViewById(R.id.ll_goals_statistics_content);
        cgMonthSelection = inflatedView.findViewById(R.id.cg_goals_statistics_month_selection);

        loadTimeStampsWithStatistics();
        if (enoughDataForStatistic()){
            loadAverageStatistics();
            if (savedInstanceState != null){
                cgMonthSelection.removeAllViews();
                setupCgMonthSelection();
                loadDataFromSavedInstanceState(savedInstanceState);
            } else {
                setupCgMonthSelection();
                Toolkit.ActivityToolkit.checkChipOfChipGroup(cgMonthSelection, cgMonthSelection.getChildCount() - 1);
            }
        }

        mRvCategories.setAdapter(primaryCategoryItemAdapter);
        mRvCategories.setNestedScrollingEnabled(false);

        return inflatedView;
    }

    private void loadDataFromSavedInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null){
            selectedMonth = savedInstanceState.getInt(EXTRA_SELECTED_MONTH, 0);
            if (selectedMonth != Toolkit.ActivityToolkit.NO_CHIP_SELECTED){
                Toolkit.ActivityToolkit.checkChipOfChipGroup(cgMonthSelection, selectedMonth);
            }
        }
    }

    private boolean enoughDataForStatistic() {
        return timeStampsWithStatistics.size() != 0;
    }

    private void loadTimeStampsWithStatistics() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(getTimeStampOfFirstMonthWithGoalStatistics());

        timeStampsWithStatistics = new ArrayList<>();
        while (!Toolkit.doesMonthExceedsCurrentTime(calendar)){
            timeStampsWithStatistics.add(calendar.getTimeInMillis());
            calendar.add(Calendar.MONTH, 1);
        }
    }

    private void loadStatisticsOfMonth(long timeStampOfMonth) {
        primaryCategoryItemAdapter = PrimaryCategoryItemAdapter.getGoalsStatisticsPrimaryCategoryItemAdapter(Database.getPrimaryCategories(), timeStampOfMonth);
        mRvCategories.setAdapter(primaryCategoryItemAdapter);

        long totalAmountOfAllGoals = getTotalAmountOfAllGoalsOfPrimaryCategoriesInDatabase();
        long totalAmountOfBillsOfMonth = getTotalAmountOfBillsOfMonthWithGoals(timeStampOfMonth);
        int percent = Math.abs(Math.round((int) (100 / (double) totalAmountOfAllGoals * totalAmountOfBillsOfMonth)));

        if (totalAmountOfBillsOfMonth > 0){
            mPgbMonth.setProgress(0);
            displayPercentCorrectly(mTxvMonth, 0);
        } else {
            mPgbMonth.setProgress(percent);
            displayPercentCorrectly(mTxvMonth, percent);
        }
    }

    private long getTotalAmountOfAllGoalsOfPrimaryCategoriesInDatabase(){
        ArrayList<Goal> goals = new ArrayList<>();
        for (PrimaryCategory primaryCategory:Database.getPrimaryCategories()){
            goals.add(primaryCategory.getGoal());
        }

        return getTotalAmountOfGoals(goals);
    }

    private long getTotalAmountOfGoals(ArrayList<Goal> goals){
        long totalAmount = 0;
        for (Goal goal:goals){
            totalAmount += goal.getAmount();
        }

        return totalAmount;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(EXTRA_SELECTED_MONTH, selectedMonth);
    }

    private long getTotalAmountOfBillsOfMonthWithGoals(long timeStampOfMonth) {
        ArrayList<Bill> allBillsInDatabaseOfMonth = Toolkit.getBillsByMonth(timeStampOfMonth);
        ArrayList<Bill> allBillsInDatabaseOfMonthWhatHaveGoals = filterBillsWithPrimaryCategoriesWhichHaveGoals(allBillsInDatabaseOfMonth);

        return Toolkit.getBillsAmount(allBillsInDatabaseOfMonthWhatHaveGoals);
    }

    private ArrayList<Bill> filterBillsWithPrimaryCategoriesWhichHaveGoals(ArrayList<Bill> bills){
        ArrayList<Bill> filteredBills = new ArrayList<>();

        for (Bill bill:bills){
            if (bill.getSubcategory().getPrimaryCategory().getGoal().getAmount() != 0){
                filteredBills.add(bill);
            }
        }

        return filteredBills;
    }

    private int getAveragePercentOfAllMonths(){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(getTimeStampOfFirstMonthWithGoalStatistics());

        int months = 0;
        long totalAmount = 0;
        while (!Toolkit.doesMonthExceedsCurrentTime(calendar)){
            ArrayList<Bill> billsOfCurrentMonth = Toolkit.getBillsByMonth(calendar.getTimeInMillis());
            ArrayList<Bill> filteredBillsWhatSubcategoriesHaveGoals = filterBillsWithPrimaryCategoriesWhichHaveGoals(billsOfCurrentMonth);

            long totalAmountOfBills = Toolkit.getBillsAmount(filteredBillsWhatSubcategoriesHaveGoals);
            totalAmount += totalAmountOfBills;

            calendar.add(Calendar.MONTH, 1);
            months++;
        }

        long totalAmountOfGoals = getTotalAmountOfAllGoalsOfPrimaryCategoriesInDatabase();
        int percent = Math.round((int)(100 / (double) (totalAmountOfGoals * months) * totalAmount));

        if (percent >= 0){
            return 0;
        } else {
            return Math.abs(percent);
        }
    }

    private void loadAverageStatistics(){
        int percent = getAveragePercentOfAllMonths();

        mPgbAverage.setProgress(percent);
        displayPercentCorrectly(mTxvAverage, percent);
    }

    private void displayPercentCorrectly(TextView textView, int percent){
        if (percent <= 0) {
            textView.setText("0%");
        } else {
            textView.setText(percent + "%");
        }
    }

    private void setupCgMonthSelection(){
        Toolkit.ActivityToolkit.addTimeChipsToChipGroup(timeStampsWithStatistics, cgMonthSelection, getContext());
        cgMonthSelection.setOnCheckedChangeListener((chipGroup, i) -> {
            selectedMonth = Toolkit.ActivityToolkit.getIndexOfSelectedChipInChipGroup(chipGroup);
            loadStatisticsOfMonth(timeStampsWithStatistics.get(selectedMonth));
        });
    }

    private long getTimeStampOfFirstMonthWithGoalStatistics(){
        ArrayList<Long> timeStamps = getTimeStampsOfAllMonthsWithGoalStatistics();
        if (timeStamps.size() != 0){
            return timeStamps.get(0);
        } else {
            return System.currentTimeMillis();
        }
    }

    private ArrayList<Long> getTimeStampsOfAllMonthsWithGoalStatistics(){
        ArrayList<Long> timeStamps = new ArrayList<>();
        long firstBillCreationDate = getTimeStampOfCreationDateOfFirstBillInDatabase();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(firstBillCreationDate);

        while (!Toolkit.doesMonthExceedsCurrentTime(calendar)){
            ArrayList<Bill> listOfBillsOfCurrentMonth = Toolkit.filterBillsByMonth(Toolkit.getAllBills(), calendar.getTimeInMillis());
            ArrayList<Bill> listOfBillsOfCurrentMonthWhatBelongToGoals = filterBillsWhatBelongToGoals(listOfBillsOfCurrentMonth);

            if (listOfBillsOfCurrentMonthWhatBelongToGoals.size() != 0){
                timeStamps.add(calendar.getTimeInMillis());
            }

            calendar.add(Calendar.MONTH, STEP_ONE_MONTH_FORWARD);
        }

        return timeStamps;
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

    private ArrayList<Bill> filterBillsWhatBelongToGoals(ArrayList<Bill> billsToFilter){
        ArrayList<Bill> filteredBills = new ArrayList<>();

        for (Bill bill:billsToFilter){
            if (bill.getSubcategory().getGoal().getAmount() != 0){
                filteredBills.add(bill);
            }
        }

        return filteredBills;
    }
}
