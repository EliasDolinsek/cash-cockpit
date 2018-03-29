package com.dolinsek.elias.cashcockpit;


import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.dolinsek.elias.cashcockpit.components.BalanceChange;
import com.dolinsek.elias.cashcockpit.components.BankAccount;
import com.dolinsek.elias.cashcockpit.components.Bill;
import com.dolinsek.elias.cashcockpit.components.Database;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.DefaultValueFormatter;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 */
public class BankAccountsStatisticsFragment extends Fragment {

    private FloatingActionButton fbtnNextBankAccount, fbtnPreviousBankAccount;
    private TextView txvCurrentBankAccount;
    private RecyclerView rvBills;
    private LineChart lcStatistics;
    private BankAccount currentBankAccount;
    private LinearLayout llNotEnoughData;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View inflatedView =  inflater.inflate(R.layout.fragment_bank_accounts_statistics, container, false);

        fbtnNextBankAccount = inflatedView.findViewById(R.id.fbtn_bank_accounts_statistics_next);
        fbtnPreviousBankAccount = inflatedView.findViewById(R.id.fbtn_bank_accounts_statistics_previous);
        txvCurrentBankAccount = inflatedView.findViewById(R.id.txv_bank_accounts_statistics_current_bank_account);
        rvBills = inflatedView.findViewById(R.id.rv_bank_account_statistics);
        lcStatistics = inflatedView.findViewById(R.id.lc_bank_accounts_statistics);
        llNotEnoughData = inflatedView.findViewById(R.id.ll_bank_accounts_statistics_not_enough_data);

        rvBills.setLayoutManager(new LinearLayoutManager(getContext()));
        setupLineChart();

        if (Database.getBankAccounts().size() != 0){
            llNotEnoughData.setVisibility(View.GONE);
            currentBankAccount = Database.getBankAccounts().get(0);
            loadStatisticsIfThereIsEnoughData();
        } else {
            llNotEnoughData.setVisibility(View.VISIBLE);
            lcStatistics.setVisibility(View.GONE);
            fbtnNextBankAccount.setEnabled(false);
            fbtnPreviousBankAccount.setEnabled(false);
        }

        fbtnNextBankAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stepOneBankAccountForward();
                loadStatisticsIfThereIsEnoughData();
            }
        });

        fbtnPreviousBankAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stepOneBankAccountBack();
                loadStatisticsIfThereIsEnoughData();
            }
        });

        return inflatedView;
    }

    private void loadStatisticsIfThereIsEnoughData(){
        loadCurrentBankAccountText(currentBankAccount);
        if (currentBankAccount.getBalanceChanges().size() > 1){
            loadStatisticsOfBankAccount(currentBankAccount);
            loadPcStatistics(currentBankAccount);

            lcStatistics.setVisibility(View.VISIBLE);
            llNotEnoughData.setVisibility(View.GONE);
        } else {
            lcStatistics.setVisibility(View.GONE);
            llNotEnoughData.setVisibility(View.VISIBLE);
            rvBills.setAdapter(null);
        }
    }

    private void stepOneBankAccountForward(){
        int currentIndexOfBankAccount = getIndexOfBankAccountInDatabase(currentBankAccount);

        if (currentIndexOfBankAccount + 1 >= Database.getBankAccounts().size()){
            currentBankAccount = Database.getBankAccounts().get(0);
        } else {
            currentBankAccount = Database.getBankAccounts().get(currentIndexOfBankAccount + 1);
        }
    }

    private void stepOneBankAccountBack(){
        int currentIndexOfBankAccount = getIndexOfBankAccountInDatabase(currentBankAccount);

        if (currentIndexOfBankAccount - 1 < 0){
            int lasBankAccountInDatabase = Database.getBankAccounts().size() - 1;
            currentBankAccount = Database.getBankAccounts().get(lasBankAccountInDatabase);
        } else {
            int previousBankAccountInDatabase = currentIndexOfBankAccount - 1;
            currentBankAccount = Database.getBankAccounts().get(previousBankAccountInDatabase);
        }
    }

    private void loadCurrentBankAccountText(BankAccount bankAccountToDisplay){
        txvCurrentBankAccount.setText(bankAccountToDisplay.getName());
    }

    private int getIndexOfBankAccountInDatabase(BankAccount bankAccountToSearch){
        for (int i = 0; i<Database.getBankAccounts().size(); i++){
            if (Database.getBankAccounts().get(i).equals(bankAccountToSearch)){
                return i;
            }
        }

        throw new IllegalArgumentException("Couldn't find bank account in database!");
    }

    private void loadStatisticsOfBankAccount(BankAccount bankAccount){
        HistoryItemAdapter historyItemAdapter = HistoryItemAdapter.getBankAccountHistoryItemAdapter(bankAccount);
        rvBills.setAdapter(historyItemAdapter);
    }

    private void loadPcStatistics(BankAccount bankAccount){
        ArrayList<Entry> balanceChangesOfLastMonthsAsPieEntries = getLastBalanceChangesOfMonthsAsEntriesOfBankAccountAndSetupLabelsForLineCharts(bankAccount);
        LineDataSet lineDataSet = new LineDataSet(balanceChangesOfLastMonthsAsPieEntries, "null");
        setupLineDataSet(lineDataSet);

        LineData lineData = new LineData(lineDataSet);
        lcStatistics.setData(lineData);
        lcStatistics.invalidate();
    }

    private void setupLineDataSet(LineDataSet lineDataSet){
        lineDataSet.setDrawCircles(false);
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        lineDataSet.setLineWidth(5f);
        lineDataSet.setValueTextSize(15f);
        lineDataSet.setValueTextColor(getResources().getColor(R.color.colorAccent));
        lineDataSet.setColor(getResources().getColor(R.color.colorPrimary));
    }

    private void setupLineChart(){
        Description description = new Description();
        description.setText(getString(R.string.label_monthly_balance));
        description.setTextSize(15f);

        lcStatistics.setDescription(description);
        lcStatistics.setDrawGridBackground(false);
        lcStatistics.setDrawBorders(false);
        lcStatistics.setAutoScaleMinMaxEnabled(true);

        removeAxisesFromChart();
        hideLegendFromChart();

        lcStatistics.invalidate();
    }

    private void removeAxisesFromChart(){
        YAxis leftAxis = lcStatistics.getAxisLeft();
        YAxis rightAxis = lcStatistics.getAxisRight();
        XAxis xAxis = lcStatistics.getXAxis();

        leftAxis.setEnabled(false);
        rightAxis.setEnabled(false);
        xAxis.setEnabled(false);
    }

    private void hideLegendFromChart(){
        Legend legend = lcStatistics.getLegend();
        legend.setEnabled(false);
    }

    private ArrayList<Entry> getLastBalanceChangesOfMonthsAsEntriesOfBankAccountAndSetupLabelsForLineCharts(BankAccount bankAccount){
        sortBalanceChangesOfCreationDates(bankAccount.getBalanceChanges());
        long creationDateOfFirstBalanceChange = bankAccount.getBalanceChanges().get(0).getTimeStampOfChange();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(creationDateOfFirstBalanceChange);

        int loops = 0;
        ArrayList<Entry> entries = new ArrayList<>();
        while (calendar.getTimeInMillis() < System.currentTimeMillis()){
            ArrayList<BalanceChange> balanceChangesOfThisMonth = getBalanceChangesOfMonth(bankAccount, calendar.getTimeInMillis());
            sortBalanceChangesOfCreationDates(balanceChangesOfThisMonth);

            int lastBalanceChangeOfMonthIndex = balanceChangesOfThisMonth.size() - 1;
            if (lastBalanceChangeOfMonthIndex != -1){
                BalanceChange lastBalanceChangeOfMonth = balanceChangesOfThisMonth.get(lastBalanceChangeOfMonthIndex);
                entries.add(getBalanceChangesAsEntry(loops, lastBalanceChangeOfMonth));
                loops++;
            }

            calendar.add(Calendar.MONTH, 1);
        }

        return entries;
    }

    private Entry getBalanceChangesAsEntry(int xPosition, BalanceChange balanceChange){
        return new Entry(xPosition, balanceChange.getNewBalance() / 100);
    }

    private ArrayList<BalanceChange> getBalanceChangesOfMonth(BankAccount bankAccount, long timestampOfMonth){
        ArrayList<BalanceChange> balanceChanges = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestampOfMonth);

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);

        for (BalanceChange balanceChange:bankAccount.getBalanceChanges()){
            calendar.setTimeInMillis(balanceChange.getTimeStampOfChange());

            int currentYear = calendar.get(Calendar.YEAR);
            int currentMonth = calendar.get(Calendar.MONTH);

            if (year == currentYear && month == currentMonth){
                balanceChanges.add(balanceChange);
            }
        }

        return balanceChanges;
    }

    private void sortBalanceChangesOfCreationDates(ArrayList<BalanceChange> balanceChangesToSort){
        Collections.sort(balanceChangesToSort, new Comparator<BalanceChange>() {
            @Override
            public int compare(BalanceChange balanceChange, BalanceChange t1) {
                return Long.compare(balanceChange.getTimeStampOfChange(), t1.getTimeStampOfChange());
            }
        });
    }
}
