package com.dolinsek.elias.cashcockpit;


import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.dolinsek.elias.cashcockpit.components.BankAccount;
import com.dolinsek.elias.cashcockpit.components.Bill;
import com.dolinsek.elias.cashcockpit.components.Currency;
import com.dolinsek.elias.cashcockpit.components.Database;
import com.dolinsek.elias.cashcockpit.components.PrimaryCategory;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;

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
    private GraphView gvStatistic;
    private RecyclerView rvCategories;
    private ScrollView scrollView;

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
        gvStatistic = (GraphView) inflatedView.findViewById(R.id.gv_categories_statistics);
        rvCategories = (RecyclerView) inflatedView.findViewById(R.id.rv_categories_statistics);
        scrollView = (ScrollView) inflatedView.findViewById(R.id.scv_categories_statistics);

        setupGraphView();
        loadCurrentMonthText();
        loadGraphViewWithStatistics(timestampOfCurrentDisplayedMonth);

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

    @Override
    public void onResume() {
        super.onResume();
        loadRecyclerViewAdapter();
    }

    private void changeMonthAndReloadData(int monthsToStep){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestampOfCurrentDisplayedMonth);
        calendar.add(Calendar.MONTH, monthsToStep);

        timestampOfCurrentDisplayedMonth = calendar.getTimeInMillis();

        loadRecyclerViewAdapter();
        loadCurrentMonthText();
        setupGraphView();
        loadGraphViewWithStatistics(timestampOfCurrentDisplayedMonth);

        scrollView.scrollTo(0,0);
    }

    private void loadRecyclerViewAdapter(){
        primaryCategoryItemAdapter = PrimaryCategoryItemAdapter.getCategoriesStatisticsPrimaryCategoryItemAdapter();
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

    private void loadGraphViewWithStatistics(long timeStampOfMonth){
        ArrayList<DataPoint> dataPoints = getDataPointsWithDataOfMonth(timeStampOfMonth);
        DataPoint[] dataPointInterface = new DataPoint[dataPoints.size()];

        for (int i = 0; i<dataPoints.size(); i++){
            dataPointInterface[i] = dataPoints.get(i);
        }

        BarGraphSeries<DataPoint> barGraphSeries = new BarGraphSeries(dataPointInterface);
        barGraphSeries.setSpacing(30);
        barGraphSeries.setDrawValuesOnTop(true);
        barGraphSeries.setValuesOnTopColor(getResources().getColor(R.color.colorAccent));
        barGraphSeries.setValuesOnTopSize(50);
        barGraphSeries.setAnimated(true);

        gvStatistic.removeAllSeries();
        gvStatistic.addSeries(barGraphSeries);
    }

    private ArrayList<DataPoint> getDataPointsWithDataOfMonth(long timeStampMonth){
        ArrayList<DataPoint> dataPoints = new ArrayList<>();
        for (int i = 0; i<Database.getPrimaryCategories().size(); i++){
            long primaryCategoryBillsTotalAmount = 0;
            PrimaryCategory currentPrimaryCategory = Database.getPrimaryCategories().get(i);

            ArrayList<Bill> bills = getAllBillsOfPrimaryCategory(currentPrimaryCategory);
            bills = filterBillsToMonth(bills, timeStampMonth);

            for (Bill bill:bills){
                primaryCategoryBillsTotalAmount += bill.getAmount();
            }

            DataPoint dataPoint = new DataPoint(i, primaryCategoryBillsTotalAmount);
            dataPoints.add(dataPoint);
        }

        return dataPoints;
    }

    private ArrayList<Bill> filterBillsToMonth(ArrayList<Bill> bills, long timeStampOfMonth){
        ArrayList<Bill> filteredBills = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeStampOfMonth);

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);

        for (Bill bill:bills){
            calendar.setTimeInMillis(bill.getCreationDate());

            int currentYear = calendar.get(Calendar.YEAR);
            int currentMonth = calendar.get(Calendar.MONTH);

            if (year == currentYear && month == currentMonth){
                filteredBills.add(bill);
            }
        }

        return filteredBills;
    }

    private ArrayList<Bill> getAllBillsOfPrimaryCategory(PrimaryCategory primaryCategory){
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

    private void setupGraphView(){
        gvStatistic.getViewport().setMinX(0);
        gvStatistic.getViewport().setMinY(0);

        int primaryCategoriesInDatabase = Database.getPrimaryCategories().size();
        gvStatistic.getViewport().setMaxX(primaryCategoriesInDatabase);

        long biggestAmount = getBiggestAmountOfCategories();
        gvStatistic.getViewport().setMaxY(biggestAmount + biggestAmount / 10);

        gvStatistic.getViewport().setXAxisBoundsManual(true);
        gvStatistic.getViewport().setYAxisBoundsManual(true);
        displayGraphViewLabels();
    }

    private void displayGraphViewLabels(){
        gvStatistic.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter(){
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX){
                    int primaryCategoriesInDatabase = Database.getPrimaryCategories().size();
                    if (value % 1.0 == 0 && value < primaryCategoriesInDatabase){
                        return Database.getPrimaryCategories().get((int)value).getName();
                    } else {
                        return "";
                    }
                } else {
                    return Currency.getActiveCurrency(getContext()).formatAmountToReadableStringWithCurrencySymbol((long)value);
                }
            }
        });
    }

    private long getBiggestAmountOfCategories(){
        long biggestAmount = 0;
        for (PrimaryCategory primaryCategory:Database.getPrimaryCategories()){
            for (BankAccount bankAccount:Database.getBankAccounts()){
                long amountOfCurrentPrimaryCategory = 0;

                for (Bill bill:bankAccount.getBills()){
                    if (bill.getSubcategory().getPrimaryCategory().equals(primaryCategory)){
                        amountOfCurrentPrimaryCategory += bill.getAmount();
                    }
                }

                if (biggestAmount < amountOfCurrentPrimaryCategory){
                    biggestAmount = amountOfCurrentPrimaryCategory;
                }
            }
        }

        return biggestAmount;
    }
}
