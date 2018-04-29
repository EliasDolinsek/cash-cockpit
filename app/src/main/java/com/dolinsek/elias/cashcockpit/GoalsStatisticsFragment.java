package com.dolinsek.elias.cashcockpit;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dolinsek.elias.cashcockpit.components.BankAccount;
import com.dolinsek.elias.cashcockpit.components.Bill;
import com.dolinsek.elias.cashcockpit.components.Database;
import com.dolinsek.elias.cashcockpit.components.Goal;
import com.dolinsek.elias.cashcockpit.components.PrimaryCategory;
import com.dolinsek.elias.cashcockpit.components.Subcategory;

import java.util.ArrayList;
import java.util.Calendar;


/**
 * A simple {@link Fragment} subclass.
 */
public class GoalsStatisticsFragment extends Fragment {

    private static final int STEP_ONE_MONTH_FORWARD = 1;
    private static final int STEP_ONE_MONTH_BACK = -1;

    private static final String EXTRA_MONTH = "month";

    private RecyclerView mRvCategories;
    private ProgressBar mPgbMonth, mPgbAverage;
    private TextView mTxvMonth, mTxvAverage;
    private LinearLayout mLlNotEnoughData, mLLContent;
    private long timeStampOfMonthToLoadStatistics;
    private SelectMonthFragment mSelectMonthFragment;
    private LinearLayout mLlSelectMonthFragmentContainer;
    private PrimaryCategoryItemAdapter primaryCategoryItemAdapter = PrimaryCategoryItemAdapter.getGoalsStatisticsPrimaryCategoryItemAdapter(Database.getPrimaryCategories(), System.currentTimeMillis());

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

        mLlNotEnoughData = (LinearLayout) inflatedView.findViewById(R.id.ll_goals_statistics_not_enough_data);
        mLLContent = (LinearLayout) inflatedView.findViewById(R.id.ll_goals_statistics_content);
        mLlSelectMonthFragmentContainer = (LinearLayout) inflatedView.findViewById(R.id.ll_goals_statistics_select_month_container);

        manageViews();
        initTimestampOfMonthToLoadStatistics(savedInstanceState);

        mRvCategories.setAdapter(primaryCategoryItemAdapter);

        return inflatedView;
    }

    @Override
    public void onResume() {
        super.onResume();

        loadAverageStatistics();
        loadStatisticsOfMonth(timeStampOfMonthToLoadStatistics);
        setupSelectMonthFragment();
    }

    @Override
    public void onPause() {
        super.onPause();

        //Removes Fragment because otherwise it would be added twice
        getFragmentManager().beginTransaction().remove(mSelectMonthFragment).commit();
    }

    private void loadStatisticsOfMonth(long timeStampOfMonth) {
        primaryCategoryItemAdapter = PrimaryCategoryItemAdapter.getGoalsStatisticsPrimaryCategoryItemAdapter(Database.getPrimaryCategories(), timeStampOfMonth);
        mRvCategories.setAdapter(primaryCategoryItemAdapter);

        long totalAmountOfAllGoals = getTotalAmountOfAllGoalsOfSubcategoriesInDatabase();
        long totalAmountOfBillsOfMonth = getTotalAmountOfBillsOfMonthWithGoals(timeStampOfMonth);
        int percent = Math.round((int) (100 / (double) totalAmountOfAllGoals * totalAmountOfBillsOfMonth));

        mPgbMonth.setProgress(percent);
        displayPercentCorrectly(mTxvMonth, percent);
    }

    private long getTotalAmountOfAllGoalsOfSubcategoriesInDatabase(){
        ArrayList<Goal> goals = new ArrayList<>();
        for (PrimaryCategory primaryCategory:Database.getPrimaryCategories()){
            for (Subcategory subcategory:primaryCategory.getSubcategories()){
                goals.add(subcategory.getGoal());
            }
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
        outState.putString(EXTRA_MONTH, String.valueOf(timeStampOfMonthToLoadStatistics));
    }

    private long getTotalAmountOfBillsOfMonthWithGoals(long timeStampOfMonth) {
        ArrayList<Bill> allBillsInDatabase = Database.Toolkit.getAllBillsInDatabase();
        ArrayList<Bill> allBillsInDatabaseOfMonth = filterBillsOfMonth(allBillsInDatabase, timeStampOfMonth);
        ArrayList<Bill> allBillsInDatabaseOfMonthWhatHaveGoals = filterBillsWithSubcategoriesWhatHaveGoals(allBillsInDatabaseOfMonth);

        long amountOfBills = getTotalAmountOfBills(allBillsInDatabaseOfMonthWhatHaveGoals);

        return amountOfBills;
    }

    private ArrayList<Bill> filterBillsOfMonth(ArrayList<Bill> billsToFilter, long timestampOfMonth){
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

    private ArrayList<Bill> filterBillsWithSubcategoriesWhatHaveGoals(ArrayList<Bill> bills){
        ArrayList<Bill> filteredBills = new ArrayList<>();

        for (Bill bill:bills){
            if (bill.getSubcategory().getGoal().getAmount() != 0){
                filteredBills.add(bill);
            }
        }

        return filteredBills;
    }

    private int getAveragePercentOfAllMonths(){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(getTimeStampOfCreationDateOfFirstBillInDatabase());
        ArrayList<Bill> allBillsInDatabase = Database.Toolkit.getAllBillsInDatabase();

        int months = 0;
        long totalAmount = 0;
        while (!doesMonthExceedsCurrentTime(calendar)){
            ArrayList<Bill> billsOfCurrentMonth = filterBillsOfMonth(allBillsInDatabase, calendar.getTimeInMillis());
            ArrayList<Bill> filteredBillsWhatSubcategoriesHaveGoals = filterBillsWithSubcategoriesWhatHaveGoals(billsOfCurrentMonth);

            totalAmount += getTotalAmountOfBills(filteredBillsWhatSubcategoriesHaveGoals);
            calendar.add(Calendar.MONTH, STEP_ONE_MONTH_FORWARD);
            months++;
        }

        long totalAmountOfGoals = getTotalAmountOfAllGoalsOfSubcategoriesInDatabase();
        int percent = (int)(100 / (double) (totalAmountOfGoals * months) * totalAmount);

        return Math.round(percent);
    }

    private void loadAverageStatistics(){
        int percent = getAveragePercentOfAllMonths();

        mPgbAverage.setProgress(percent);
        displayPercentCorrectly(mTxvAverage, percent);
    }

    private long getTimeStampOfCreationDateOfFirstBillInDatabase(){
        long firstCreationDate = System.currentTimeMillis();

        ArrayList<Bill> billsInDatabase = Database.Toolkit.getAllBillsInDatabase();
        for (Bill bill:billsInDatabase){
            if (bill.getCreationDate() < firstCreationDate){
                firstCreationDate = bill.getCreationDate();
            }
        }

        return firstCreationDate;
    }

    private void initTimestampOfMonthToLoadStatistics(Bundle savedInstanceState){
        if (savedInstanceState != null) {
            timeStampOfMonthToLoadStatistics = savedInstanceState.getLong(EXTRA_MONTH);
        } else {
            timeStampOfMonthToLoadStatistics = System.currentTimeMillis();
        }
    }

    private void displayPercentCorrectly(TextView textView, int percent){
        if (percent <= 0) {
            textView.setText("0%");
        } else {
            textView.setText(percent + "%");
        }
    }

    private void manageViews(){
        ArrayList<Bill> billsWhatBelongToGoals = filterBillsWhatBelongToGoals(Database.Toolkit.getAllBillsInDatabase());
        if (getTotalAmountOfAllGoalsOfSubcategoriesInDatabase() == 0 || billsWhatBelongToGoals.size() == 0){
            mLLContent.setVisibility(View.GONE);
            mLlNotEnoughData.setVisibility(View.VISIBLE);
            mLlSelectMonthFragmentContainer.setVisibility(View.GONE);
        } else {
            mLLContent.setVisibility(View.VISIBLE);
            mLlNotEnoughData.setVisibility(View.GONE);
        }
    }

    private void setupSelectMonthFragment(){
        mSelectMonthFragment = new SelectMonthFragment();
        getFragmentManager().beginTransaction().add(R.id.ll_goals_statistics_select_month_container, mSelectMonthFragment).commit();

        long[] timeStampsOfMonthsWithStatistics = arrayListToLongArray(getTimeStampsOfAllMonthsWithGoalStatistics());

        mSelectMonthFragment.setTimeStampsOfDates(timeStampsOfMonthsWithStatistics);
        mSelectMonthFragment.setOnItemSelectedListener(getOnMonthToLoadStatisticsSelectedOnSelectedListener());
        mSelectMonthFragment.setSelectLastItemAfterCreate(true);
    }

    private AdapterView.OnItemSelectedListener getOnMonthToLoadStatisticsSelectedOnSelectedListener(){
        return new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                timeStampOfMonthToLoadStatistics = getTimeStampsOfAllMonthsWithGoalStatistics().get(i);
                loadStatisticsOfMonth(timeStampOfMonthToLoadStatistics);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        };
    }

    private ArrayList<Long> getTimeStampsOfAllMonthsWithGoalStatistics(){
        ArrayList<Long> timeStamps = new ArrayList<>();
        long firstBillCreationDate = getTimeStampOfCreationDateOfFirstBillInDatabase();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(firstBillCreationDate);

        while (!doesMonthExceedsCurrentTime(calendar)){
            ArrayList<Bill> listOfBillsOfCurrentMonth = filterBillsOfMonth(Database.Toolkit.getAllBillsInDatabase(), calendar.getTimeInMillis());
            ArrayList<Bill> listOfBillsOfCurrentMonthWhatBelongToGoals = filterBillsWhatBelongToGoals(listOfBillsOfCurrentMonth);

            if (listOfBillsOfCurrentMonthWhatBelongToGoals.size() != 0){
                timeStamps.add(calendar.getTimeInMillis());
            }

            calendar.add(Calendar.MONTH, STEP_ONE_MONTH_FORWARD);
        }

        return timeStamps;
    }

    private boolean doesMonthExceedsCurrentTime(Calendar calendar){
        Calendar currentMonthCalendar = Calendar.getInstance();
        currentMonthCalendar.setTimeInMillis(System.currentTimeMillis());

        int currentYear = currentMonthCalendar.get(Calendar.YEAR);
        int currentMonth = currentMonthCalendar.get(Calendar.MONTH);

        return currentYear < calendar.get(Calendar.YEAR) || currentMonth < calendar.get(Calendar.MONTH);
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

    private long[] arrayListToLongArray(ArrayList<Long> arrayList){
        long[] longsToReturn = new long[arrayList.size()];

        for (int i = 0; i<longsToReturn.length; i++){
            longsToReturn[i] = arrayList.get(i);
        }

        return longsToReturn;
    }
}
