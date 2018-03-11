package com.dolinsek.elias.cashcockpit;


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
import com.dolinsek.elias.cashcockpit.components.PrimaryCategory;
import com.dolinsek.elias.cashcockpit.components.Subcategory;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;


/**
 * A simple {@link Fragment} subclass.
 */
public class GoalsStatisticsFragment extends Fragment {

    private RecyclerView mRvCategories;
    private ProgressBar mPgbMonth, mPgbOverall;
    private FloatingActionButton mFbtnBack, mFbtnForward;
    private TextView mTxvMonth, mTxvOverall, mTxvCurrentMonth;
    private Calendar calendar;
    private ArrayList<Subcategory> subcategoriesToRestore = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflatedView = inflater.inflate(R.layout.fragment_goals_statistics, container, false);

        mRvCategories = (RecyclerView) inflatedView.findViewById(R.id.rv_goals_statistics_categories);
        mRvCategories.setLayoutManager(new LinearLayoutManager(getContext()));

        mPgbMonth = (ProgressBar) inflatedView.findViewById(R.id.pgb_goals_statistics_month);
        mPgbOverall = (ProgressBar) inflatedView.findViewById(R.id.pgb_goals_statistics_overall);

        mTxvMonth = (TextView) inflatedView.findViewById(R.id.txv_goals_statistics_month);
        mTxvOverall = (TextView) inflatedView.findViewById(R.id.txv_goals_statistics_overall);
        mTxvCurrentMonth = (TextView) inflatedView.findViewById(R.id.txv_goals_statistics_current_month);

        calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        mTxvCurrentMonth.setText(getResources().getStringArray(R.array.months_array)[calendar.get(Calendar.MONTH)] + " " + calendar.get(Calendar.YEAR));

        mFbtnBack = (FloatingActionButton) inflatedView.findViewById(R.id.fbtn_goals_statistics_back);
        mFbtnForward = (FloatingActionButton) inflatedView.findViewById(R.id.fbtn_goals_statistics_forward);

        manageButtonStates();

        mFbtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calendar.add(Calendar.MONTH, -1);

                int percent = getMonthStatistic(calendar.getTimeInMillis());
                mTxvMonth.setText(percent + "%");
                mPgbMonth.setProgress(percent);
                mTxvCurrentMonth.setText(getResources().getStringArray(R.array.months_array)[calendar.get(Calendar.MONTH)] + " " + calendar.get(Calendar.YEAR));
                manageButtonStates();
            }
        });

        mFbtnForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calendar.add(Calendar.MONTH, 1);

                int percent = getMonthStatistic(calendar.getTimeInMillis());
                mTxvMonth.setText(percent + "%");
                mPgbMonth.setProgress(percent);
                mTxvCurrentMonth.setText(getResources().getStringArray(R.array.months_array)[calendar.get(Calendar.MONTH)] + " " + calendar.get(Calendar.YEAR));
                manageButtonStates();
            }
        });
        return inflatedView;
    }

    private void manageButtonStates(){
        calendar.add(Calendar.MONTH, -1);
        if (calendar.getTimeInMillis() < getFirstBillCreationDate()){
            mFbtnBack.setEnabled(false);
        } else {
            mFbtnBack.setEnabled(true);
        }

        calendar.add(Calendar.MONTH, 1);

        Calendar currentTimeCalendar = Calendar.getInstance();
        currentTimeCalendar.setTimeInMillis(System.currentTimeMillis());

        if (calendar.get(Calendar.MONTH) + calendar.get(Calendar.YEAR) == currentTimeCalendar.get(Calendar.MONTH) + currentTimeCalendar.get(Calendar.YEAR)){
            mFbtnForward.setEnabled(false);
        } else {
            mFbtnForward.setEnabled(true);
        }
    }

    private int getMonthStatistic(long timeStamp){
        long amount = 0;

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeStamp);
        int currentTime = calendar.get(Calendar.YEAR) + calendar.get(Calendar.MONTH);

        for (BankAccount bankAccount:Database.getBankAccounts()){
            for (Bill bill:bankAccount.getBills()){
                for (PrimaryCategory primaryCategory:Database.getPrimaryCategories()){
                    for (Subcategory subcategory:primaryCategory.getSubcategories()){
                        calendar.setTimeInMillis(bill.getCreationDate());
                        ;
                        if (subcategory.getGoal().getAmount() != 0 && bill.getSubcategory().equals(subcategory) && calendar.get(Calendar.YEAR) + calendar.get(Calendar.MONTH) == currentTime &&
                                subcategory.getGoal().getCreationDate() < bill.getCreationDate()){

                            amount += bill.getAmount();
                        }
                    }
                }
            }
        }

        return (int)(100.0 / ((double) getGoalAmountSum()) * ((double) amount));
    }

    private long getFirstBillCreationDate(){
        long firstCreationDate = System.currentTimeMillis();

        for (PrimaryCategory primaryCategory:Database.getPrimaryCategories()){
            for (Subcategory subcategory:primaryCategory.getSubcategories()){
                if (subcategory.getGoal().getCreationDate() < System.currentTimeMillis()){
                    firstCreationDate = subcategory.getGoal().getCreationDate();
                }
            }
        }

        return firstCreationDate;
    }

    private long getGoalAmountSum(){
        long totalAmount = 0;
        for (PrimaryCategory primaryCategory:Database.getPrimaryCategories()){
            for(Subcategory subcategory:primaryCategory.getSubcategories()){
                totalAmount += subcategory.getGoal().getAmount();
            }
        }

        return totalAmount;
    }

    private int getOverallStatistics (){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(getFirstBillCreationDate());

        int loops = 0;
        int result = 0;

        while (true){
            if (calendar.getTimeInMillis() > System.currentTimeMillis())
                break;

            loops++;
            result += getMonthStatistic(calendar.getTimeInMillis());
            calendar.add(Calendar.MONTH, 1);
        }

        return (int) (result / loops);
    }

    private ArrayList<PrimaryCategory> getCategories(){
        ArrayList<PrimaryCategory> primaryCategories = new ArrayList<>();

        for(PrimaryCategory primaryCategory:Database.getPrimaryCategories()){
            if(primaryCategory.getGoal().getAmount() != 0){
                primaryCategories.add(primaryCategory);
            }
        }

        return primaryCategories;
    }

    @Override
    public void onStart() {
        super.onStart();

        int thisMonth = getMonthStatistic(System.currentTimeMillis());
        mPgbMonth.setProgress(thisMonth);
        mTxvMonth.setText(thisMonth + "%");

        int overall = getOverallStatistics();
        mPgbOverall.setProgress(overall);
        mTxvOverall.setText(overall + "%");

        mRvCategories.setAdapter(new PrimaryCategoryItemAdapter(getCategories(), PrimaryCategoryItemAdapter.TYPE_GOAL_STATISTICS));
    }
}
