package com.dolinsek.elias.cashcockpit;


import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.dolinsek.elias.cashcockpit.components.BalanceChange;
import com.dolinsek.elias.cashcockpit.components.BankAccount;
import com.dolinsek.elias.cashcockpit.components.Bill;
import com.dolinsek.elias.cashcockpit.components.Currency;
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

    private static final String EXTRA_BANK_ACCOUNT_INDEX_IN_DATABASE = "bankAccountIndex";

    private RecyclerView rvBills;
    private LineChart lcStatistics;
    private BankAccount currentBankAccount;
    private LinearLayout llNotEnoughData;
    private Spinner spnSelectBankAccount;

    private BankAccount selectedBankAccount;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View inflatedView =  inflater.inflate(R.layout.fragment_bank_accounts_statistics, container, false);

        rvBills = inflatedView.findViewById(R.id.rv_bank_account_statistics);
        lcStatistics = inflatedView.findViewById(R.id.lc_bank_accounts_statistics);
        llNotEnoughData = inflatedView.findViewById(R.id.ll_bank_accounts_statistics_not_enough_data);
        spnSelectBankAccount = inflatedView.findViewById(R.id.spn_bank_accounts_statistics_select_bank_account);

        rvBills.setLayoutManager(new LinearLayoutManager(getContext()));

        if (Database.getBankAccounts().size() != 0){
            llNotEnoughData.setVisibility(View.GONE);
            loadBankAccount(savedInstanceState);

            setupSelectBankAccountSpinner();
        } else {
            llNotEnoughData.setVisibility(View.VISIBLE);
            lcStatistics.setVisibility(View.GONE);
        }

        return inflatedView;
    }

    private void loadChartData(BankAccount bankAccount){
        LineData lineData = new LineData();

        ArrayList<BalanceChange> balanceChanges = getLastBalanceChangesOfMonthsOfBankAccount(bankAccount);
        LineDataSet lineDataSet = getBalanceChangesAsLineDataSet(balanceChanges);
        lineData.addDataSet(lineDataSet);

        lcStatistics.setData(lineData);
        lcStatistics.invalidate();
    }

    private LineDataSet getBalanceChangesAsLineDataSet(ArrayList<BalanceChange> balanceChanges){
        ArrayList<Entry> balanceChangesAsEntries = new ArrayList<>();
        for (int i = 0; i<balanceChanges.size(); i++){
            Entry entry = getBalanceChangeAsEntry(balanceChanges.get(i), i);
            balanceChangesAsEntries.add(entry);
        }

        return getBalanceChangeEntriesAsLineDataSet(balanceChangesAsEntries);
    }

    private LineDataSet getBalanceChangeEntriesAsLineDataSet(ArrayList<Entry> entries){
        LineDataSet lineDataSet = new LineDataSet(entries, "");
        lineDataSet.setValues(entries);

        return lineDataSet;
    }

    private ArrayList<String> getNamesOfBankAccountsInDatabase(){
        ArrayList<String> bankAccountsNames = new ArrayList<>();
        for (BankAccount bankAccount:Database.getBankAccounts()){
            bankAccountsNames.add(bankAccount.getName());
        }

        return bankAccountsNames;
    }

    private void setupSelectBankAccountSpinner(){
        ArrayAdapter<String> bankAccountsAdapter = new ArrayAdapter<>(getContext(), R.layout.costum_spinner_layout, getNamesOfBankAccountsInDatabase());
        bankAccountsAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        spnSelectBankAccount.getBackground().setColorFilter(getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_ATOP);
        spnSelectBankAccount.setAdapter(bankAccountsAdapter);

        spnSelectBankAccount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int index, long l) {
                selectedBankAccount = Database.getBankAccounts().get(index);
                displayBillsOfBankAccount(selectedBankAccount);

                if (selectedBankAccount.getBalanceChanges().size() != 0){
                    loadChartData(selectedBankAccount);
                } else {
                    //TODO display not enough data
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private int getIndexOfBankAccountInDatabase(BankAccount bankAccountToSearch){
        for (int i = 0; i<Database.getBankAccounts().size(); i++){
            if (Database.getBankAccounts().get(i).equals(bankAccountToSearch)){
                return i;
            }
        }

        throw new IllegalArgumentException("Couldn't find bank account in database!");
    }

    private void displayBillsOfBankAccount(BankAccount bankAccount){
        HistoryItemAdapter historyItemAdapter = HistoryItemAdapter.getBankAccountHistoryItemAdapter(bankAccount);
        rvBills.setAdapter(historyItemAdapter);
    }

    private ArrayList<BalanceChange> getLastBalanceChangesOfMonthsOfBankAccount(BankAccount bankAccount){
        ArrayList<BalanceChange> balanceChanges = new ArrayList<>();
        sortBalanceChangesOfCreationDates(bankAccount.getBalanceChanges());

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(calendar.getTimeInMillis());

        int firstBalanceChangeYear = calendar.get(Calendar.YEAR);
        int firstBalanceChangeMonth = calendar.get(Calendar.MONTH);

        while (calendar.get(Calendar.YEAR) <= firstBalanceChangeYear && calendar.get(Calendar.MONTH) <= firstBalanceChangeMonth){
            BalanceChange lastBalanceChangeOfMonth = getLastBalanceChangeOfBankAccountAndMonth(bankAccount, calendar.getTimeInMillis());
            balanceChanges.add(lastBalanceChangeOfMonth);
            calendar.add(Calendar.MONTH, 1);
        }

        return balanceChanges;
    }

    private Entry getBalanceChangeAsEntry(BalanceChange balanceChange, int x){
        long formattedAmountOfBalanceChange = balanceChange.getNewBalance() / 100;

        return new Entry(x, formattedAmountOfBalanceChange);
    }

    private BalanceChange getLastBalanceChangeOfBankAccountAndMonth(BankAccount bankAccount, long timeStampOfMonth){
        ArrayList<BalanceChange> balanceChangesOfBankAccount = bankAccount.getBalanceChanges();
        sortBalanceChangesOfCreationDates(balanceChangesOfBankAccount);

        ArrayList<BalanceChange> balanceChangesOfBankAccountAndMonth = filterBalanceChangesOfMonth(balanceChangesOfBankAccount, timeStampOfMonth);
        return balanceChangesOfBankAccountAndMonth.get(balanceChangesOfBankAccountAndMonth.size() - 1);
    }

    private ArrayList<BalanceChange> filterBalanceChangesOfMonth(ArrayList<BalanceChange> balanceChangesToFilter, long timeStampOfMonth){
        ArrayList<BalanceChange> balanceChangesOfMonth = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeStampOfMonth);

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);

        for (BalanceChange balanceChange:balanceChangesToFilter){
            calendar.setTimeInMillis(balanceChange.getTimeStampOfChange());

            int currentYear = calendar.get(Calendar.YEAR);
            int currentMonth = calendar.get(Calendar.MONTH);

            if (currentYear == year && currentMonth == month){
                balanceChangesOfMonth.add(balanceChange);
            }
        }

        return balanceChangesOfMonth;
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

    private ArrayList<BalanceChange> getAndSortBalanceChangesOfCreationDates(ArrayList<BalanceChange> balanceChangesToSort){
        Collections.sort(balanceChangesToSort, new Comparator<BalanceChange>() {
            @Override
            public int compare(BalanceChange balanceChange, BalanceChange t1) {
                return Long.compare(balanceChange.getTimeStampOfChange(), t1.getTimeStampOfChange());
            }
        });

        return balanceChangesToSort;
    }
    private void sortBalanceChangesOfCreationDates(ArrayList<BalanceChange> balanceChangesToSort){
        getAndSortBalanceChangesOfCreationDates(balanceChangesToSort);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(EXTRA_BANK_ACCOUNT_INDEX_IN_DATABASE, getIndexOfBankAccountInDatabase(currentBankAccount));
    }

    private void loadBankAccount(Bundle savedInstanceState){
        if (savedInstanceState != null){
            int indexOfBankAccountInDatabase = savedInstanceState.getInt(EXTRA_BANK_ACCOUNT_INDEX_IN_DATABASE);
            currentBankAccount = Database.getBankAccounts().get(indexOfBankAccountInDatabase);
        } else {
            currentBankAccount = Database.getBankAccounts().get(0);
        }
    }
}
