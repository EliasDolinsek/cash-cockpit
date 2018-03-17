package com.dolinsek.elias.cashcockpit;


import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dolinsek.elias.cashcockpit.components.BankAccount;
import com.dolinsek.elias.cashcockpit.components.Bill;
import com.dolinsek.elias.cashcockpit.components.Database;
import com.dolinsek.elias.cashcockpit.components.Goal;
import com.dolinsek.elias.cashcockpit.components.PrimaryCategory;
import com.dolinsek.elias.cashcockpit.components.Subcategory;
import com.dolinsek.elias.cashcockpit.components.Toolbox;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;


/**
 * A simple {@link Fragment} subclass.
 */
public class GoalsStatisticsFragment extends Fragment {

    private static final String EXTRA_MONTH = "month";

    private RecyclerView mRvCategories;
    private ProgressBar mPgbMonth;
    private FloatingActionButton mFbtnBack, mFbtnForward;
    private TextView mTxvMonth, mTxvCurrentMonth, mTxvDetails;
    private Calendar calendar;
    private PrimaryCategoryItemAdapter primaryCategoryItemAdapter = new PrimaryCategoryItemAdapter(Database.getPrimaryCategories(), PrimaryCategoryItemAdapter.TYPE_GOAL_STATISTICS);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflatedView = inflater.inflate(R.layout.fragment_goals_statistics, container, false);

        mRvCategories = (RecyclerView) inflatedView.findViewById(R.id.rv_goals_statistics_categories);
        mRvCategories.setLayoutManager(new LinearLayoutManager(getContext()));

        mPgbMonth = (ProgressBar) inflatedView.findViewById(R.id.pgb_goals_statistics_month);

        mTxvMonth = (TextView) inflatedView.findViewById(R.id.txv_goals_statistics_month);
        mTxvCurrentMonth = (TextView) inflatedView.findViewById(R.id.txv_goals_statistics_current_month);
        mTxvDetails = (TextView) inflatedView.findViewById(R.id.txv_goals_statistics_details);

        calendar = Calendar.getInstance();
        if (savedInstanceState != null){
            calendar.setTimeInMillis(Long.parseLong(savedInstanceState.getString(EXTRA_MONTH)));
        } else {
            calendar.setTimeInMillis(System.currentTimeMillis());
        }
        updateCurrentMonthTextView();

        primaryCategoryItemAdapter.setGoalStatisticsTime(calendar.getTimeInMillis());
        mRvCategories.setAdapter(primaryCategoryItemAdapter);

        mFbtnBack = (FloatingActionButton) inflatedView.findViewById(R.id.fbtn_goals_statistics_back);
        mFbtnForward = (FloatingActionButton) inflatedView.findViewById(R.id.fbtn_goals_statistics_forward);

        loadStatisticsMonth(calendar.getTimeInMillis());

        mFbtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                calendar.add(Calendar.MONTH, -1);
                loadStatisticsMonth(calendar.getTimeInMillis());
                updateCurrentMonthTextView();
            }
        });

        mFbtnForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                calendar.add(Calendar.MONTH, 1);
                loadStatisticsMonth(calendar.getTimeInMillis());
                updateCurrentMonthTextView();
            }
        });
        return inflatedView;
    }

    /**
     * Loads statistics of a specific month
     * @param timeStamp of month
     */
    private void loadStatisticsMonth(long timeStamp) {

        primaryCategoryItemAdapter = new PrimaryCategoryItemAdapter(Database.getPrimaryCategories(), PrimaryCategoryItemAdapter.TYPE_GOAL_STATISTICS);
        primaryCategoryItemAdapter.setGoalStatisticsTime(calendar.getTimeInMillis());
        mRvCategories.setAdapter(primaryCategoryItemAdapter);

        int percent =  (int) (100 / (double)getGoalsTotalAmount() * getUsedMoneyOfMonth(timeStamp));
        mPgbMonth.setProgress(percent);

        if (percent <= 0){
            mTxvMonth.setText("0%");
        } else {
            mTxvMonth.setText(percent + "%");
        }
    }

    /**
     * @return sum of all goal-amounts
     */
    private long getGoalsTotalAmount(){
       ArrayList<Goal> goals = Toolbox.getSubcategoriesGoals();

       long goalsTotalAmount = 0;
       for (Goal goal:goals){
           goalsTotalAmount += goal.getAmount();
       }

       return goalsTotalAmount;
    }

    /**
     * Displays date of current month of statistics
     */
    private void updateCurrentMonthTextView(){
        String date = getResources().getStringArray(R.array.months_array)[calendar.get(Calendar.MONTH)] + " " + calendar.get(Calendar.YEAR);
        mTxvCurrentMonth.setText(date);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(EXTRA_MONTH, String.valueOf(calendar.getTimeInMillis()));
    }

    /**
     * @return creation date of first goal created
     */
    private long getFirstCreationDateOfGoals(){
        ArrayList<Goal> goals = Toolbox.getSubcategoriesGoals();
        long firstCreationDate = System.currentTimeMillis();

        for (Goal goal:goals){
            if (goal.getCreationDate() < firstCreationDate)
                firstCreationDate = goal.getCreationDate();
        }

        return firstCreationDate;
    }

    private long getUsedMoneyOfMonth(long timeStamp){
        ArrayList<Bill> bills = Toolbox.getBills(timeStamp, Toolbox.TYPE_MONTH);
        long monthAmount = 0;

        for (PrimaryCategory primaryCategory:Database.getPrimaryCategories()){
            if (primaryCategory.getGoal().getAmount() != 0){
                for (Subcategory subcategory:primaryCategory.getSubcategories()){
                    if (subcategory.getGoal().getAmount() != 0){
                        for (Bill bill:bills){
                            if (bill.getSubcategory().equals(subcategory)){
                                if (bill.getType() == Bill.TYPE_INPUT){
                                    monthAmount -= bill.getAmount();
                                } else {
                                    monthAmount += bill.getAmount();
                                }
                            }
                        }
                    }
                }
            }
        }

        return monthAmount;
    }
}
