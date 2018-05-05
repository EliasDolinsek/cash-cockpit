package com.dolinsek.elias.cashcockpit;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.dolinsek.elias.cashcockpit.components.BalanceChange;
import com.dolinsek.elias.cashcockpit.components.BankAccount;
import com.dolinsek.elias.cashcockpit.components.Database;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;


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
    private TextView txvNoBills;

    private BankAccount selectedBankAccount;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View inflatedView =  inflater.inflate(R.layout.fragment_bank_accounts_statistics, container, false);

        rvBills = inflatedView.findViewById(R.id.rv_bank_account_statistics);
        lcStatistics = inflatedView.findViewById(R.id.lc_bank_accounts_statistics);
        llNotEnoughData = inflatedView.findViewById(R.id.ll_bank_accounts_statistics_not_enough_data);
        spnSelectBankAccount = inflatedView.findViewById(R.id.spn_bank_accounts_statistics_select_bank_account);
        txvNoBills = inflatedView.findViewById(R.id.txv_bank_accounts_statistics_no_bills);

        rvBills.setLayoutManager(new LinearLayoutManager(getContext()));
        rvBills.setNestedScrollingEnabled(false);

        if (Database.getBankAccounts().size() != 0){
            llNotEnoughData.setVisibility(View.GONE);
            loadBankAccount(savedInstanceState);
            setupSelectBankAccountSpinner();
        } else {
            llNotEnoughData.setVisibility(View.VISIBLE);
            lcStatistics.setVisibility(View.GONE);
            spnSelectBankAccount.setVisibility(View.GONE);
            txvNoBills.setVisibility(View.GONE);
        }

        return inflatedView;
    }

    private void setupChartStyle(){
        lcStatistics.getLegend().setEnabled(false);
        lcStatistics.getAxisLeft().setEnabled(false);
        lcStatistics.getAxisRight().setEnabled(false);
        lcStatistics.getData().setHighlightEnabled(false);
        lcStatistics.getDescription().setEnabled(false);
        lcStatistics.setExtraOffsets(25f,10f,25f,10f);
        lcStatistics.setTouchEnabled(false);

        XAxis xAxis = lcStatistics.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setYOffset(0f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularityEnabled(true);
        xAxis.setGranularity(1f);

        setupChartTextStyles();
    }

    private void setupLineDataSet(LineDataSet lineDataSet){
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        lineDataSet.setLineWidth(3f);
        lineDataSet.setColor(getResources().getColor(R.color.colorPrimary));
        lineDataSet.setCircleRadius(7f);
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setCircleColor(getResources().getColor(R.color.colorPrimary));
        lineDataSet.setValueFormatter(new CurrencyEntryValueFormatter(getContext()));
    }

    private void setupChartTextStyles(){
        lcStatistics.getXAxis().setTextSize(14f);
        lcStatistics.getData().setValueTextSize(14f);
    }

    private void displayTimeStampsOfBalanceChangesOnChart(ArrayList<Long> timeStampsOfMonths){
        XAxis xAxis = lcStatistics.getXAxis();
        xAxis.setValueFormatter(new DateValueFormatter(timeStampsOfMonths));
    }

    private void loadChartData(BankAccount bankAccount){
        LineData lineData = new LineData();

        ArrayList<BalanceChange> balanceChanges = getLastBalanceChangesOfMonthsOfBankAccount(bankAccount);
        LineDataSet lineDataSet = getBalanceChangesAsLineDataSet(balanceChanges);
        lineData.addDataSet(lineDataSet);
        setupLineDataSet(lineDataSet);

        displayTimeStampsOfBalanceChangesOnChart(getTimeStampsOfBalanceChanges(balanceChanges));
        lcStatistics.setData(lineData);
        lcStatistics.invalidate();
    }

    private ArrayList<Long> getTimeStampsOfBalanceChanges(ArrayList<BalanceChange> balanceChanges){
        ArrayList<Long> timeStamps = new ArrayList<>();
        for (BalanceChange balanceChange:balanceChanges){
            timeStamps.add(balanceChange.getTimeStampOfChange());
        }

        return timeStamps;
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
                    setupChartStyle();

                    lcStatistics.setVisibility(View.VISIBLE);
                    llNotEnoughData.setVisibility(View.GONE);
                } else {
                    lcStatistics.setVisibility(View.GONE);
                    llNotEnoughData.setVisibility(View.VISIBLE);
                }

                if (selectedBankAccount.getBills().size() == 0){
                    txvNoBills.setVisibility(View.VISIBLE);
                    rvBills.setVisibility(View.GONE);
                } else {
                    txvNoBills.setVisibility(View.GONE);
                    rvBills.setVisibility(View.VISIBLE);
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
        long firstBalanceChangeDate = bankAccount.getBalanceChanges().get(0).getTimeStampOfChange();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);

        calendar.setTimeInMillis(firstBalanceChangeDate);
        while (calendar.get(Calendar.YEAR) <= year && calendar.get(Calendar.MONTH) <= month){
            int balanceChangesOfMonthSize = getSizeOfBalanceChangesOfMonth(bankAccount, calendar.getTimeInMillis());
            if (balanceChangesOfMonthSize != 0){
                BalanceChange lastBalanceChangeOfMonth = getLastBalanceChangeOfBankAccountAndMonth(bankAccount, calendar.getTimeInMillis());
                balanceChanges.add(lastBalanceChangeOfMonth);
            }

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

    private int getSizeOfBalanceChangesOfMonth(BankAccount bankAccount, long timeStampOfMonth){
        ArrayList<BalanceChange> balanceChangesOfMonth = filterBalanceChangesOfMonth(bankAccount.getBalanceChanges(), timeStampOfMonth);
        return balanceChangesOfMonth.size();
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
        int index = 0;
        if (currentBankAccount != null){
            index = getIndexOfBankAccountInDatabase(currentBankAccount);
        }

        outState.putInt(EXTRA_BANK_ACCOUNT_INDEX_IN_DATABASE, index);
    }

    private void loadBankAccount(Bundle savedInstanceState){
        if (savedInstanceState != null){
            int indexOfBankAccountInDatabase = savedInstanceState.getInt(EXTRA_BANK_ACCOUNT_INDEX_IN_DATABASE);
            currentBankAccount = Database.getBankAccounts().get(indexOfBankAccountInDatabase);
        } else {
            currentBankAccount = Database.getBankAccounts().get(0);
        }
    }

    private class DateValueFormatter implements IAxisValueFormatter{

        private ArrayList<Long> timeStampsOfMonths;

        public DateValueFormatter(ArrayList<Long> timeStampsOfMonths){
            this.timeStampsOfMonths = timeStampsOfMonths;
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM");

            try {
                Date date = new Date(timeStampsOfMonths.get((int) value));
                return simpleDateFormat.format(date);
            } catch (Exception e){
                e.printStackTrace();
                return "";
            }
        }
    }
}
