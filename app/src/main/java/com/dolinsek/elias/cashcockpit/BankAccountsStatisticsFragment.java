package com.dolinsek.elias.cashcockpit;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.chip.Chip;
import android.support.design.chip.ChipGroup;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dolinsek.elias.cashcockpit.components.BalanceChange;
import com.dolinsek.elias.cashcockpit.components.BankAccount;
import com.dolinsek.elias.cashcockpit.components.Database;
import com.dolinsek.elias.cashcockpit.components.Toolkit;
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
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 */
public class
BankAccountsStatisticsFragment extends Fragment {

    private static final String EXTRA_BANK_ACCOUNT_INDEX = "bankAccountIndex";
    private static final int ERROR_NO_BANK_ACCOUNT_INDEX = -1;

    private RecyclerView rvBills;
    private LineChart lcStatistics;
    private ChipGroup cgBankAccountSelection;
    int selectedBankAccountIndex;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View inflatedView =  inflater.inflate(R.layout.fragment_bank_accounts_statistics, container, false);

        rvBills = inflatedView.findViewById(R.id.rv_bank_account_statistics);
        lcStatistics = inflatedView.findViewById(R.id.lc_bank_accounts_statistics);
        cgBankAccountSelection = inflatedView.findViewById(R.id.cg_bank_accounts_statistics);

        rvBills.setLayoutManager(new LinearLayoutManager(getContext()));
        rvBills.setNestedScrollingEnabled(false);

        if (enoughDataForStatistic()){
            if (savedInstanceState != null){
                cgBankAccountSelection.removeAllViews();
                setupCgBankAccountSelection();
                loadDataFromSavedInstanceState(savedInstanceState);
            } else {
                selectedBankAccountIndex = 0;
                loadStatistics(Database.getBankAccounts().get(selectedBankAccountIndex));
                setupCgBankAccountSelection();
                Toolkit.ActivityToolkit.checkChipOfChipGroup(cgBankAccountSelection, cgBankAccountSelection.getChildCount() - 1);
            }
        }

        return inflatedView;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(EXTRA_BANK_ACCOUNT_INDEX, selectedBankAccountIndex);
    }

    private void loadDataFromSavedInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null){
            selectedBankAccountIndex = savedInstanceState.getInt(EXTRA_BANK_ACCOUNT_INDEX, 0);
            loadStatistics(Database.getBankAccounts().get(selectedBankAccountIndex));
            Toolkit.ActivityToolkit.checkChipOfChipGroup(cgBankAccountSelection, selectedBankAccountIndex);
        }
    }

    private boolean enoughDataForStatistic() {
        return Database.getBankAccounts().size() != 0 && getSizeOfAllBalanceChanges() != 0;
    }

    private int getSizeOfAllBalanceChanges(){
        int balanceChangesSize = 0;
        for (BankAccount bankAccount:Database.getBankAccounts()){
            balanceChangesSize += bankAccount.getBalanceChanges().size();
        }

        return balanceChangesSize;
    }

    private void setupCgBankAccountSelection(){
        Toolkit.ActivityToolkit.addBankAccountChipsToChipGroup(cgBankAccountSelection, getContext());
        cgBankAccountSelection.setOnCheckedChangeListener((chipGroup, i) -> {
            selectedBankAccountIndex = Toolkit.ActivityToolkit.getIndexOfSelectedChipInChipGroup(cgBankAccountSelection);
            loadStatistics(Database.getBankAccounts().get(selectedBankAccountIndex));
        });
    }

    private void loadStatistics(BankAccount selectedBankAccount){
        loadChartStatistics(selectedBankAccount);
        rvBills.setAdapter(HistoryItemAdapter.getBankAccountHistoryItemAdapter(selectedBankAccount));
    }

    private void loadChartStatistics(BankAccount bankAccount){
        LineData lineData = new LineData();

        ArrayList<BalanceChange> balanceChanges = getLastBalanceChangesOfMonthsOfBankAccount(bankAccount);
        LineDataSet lineDataSet = getBalanceChangesAsLineDataSet(balanceChanges);
        lineData.addDataSet(lineDataSet);
        setupLineDataSet(lineDataSet);

        displayTimeStampsOfBalanceChangesOnChart(getTimeStampsOfBalanceChanges(balanceChanges));
        lcStatistics.setData(lineData);
        setupChartStyle();
        lcStatistics.invalidate();
    }

    private ArrayList<Long> getTimeStampsOfBalanceChanges(ArrayList<BalanceChange> balanceChanges){
        ArrayList<Long> timeStamps = new ArrayList<>();
        for (BalanceChange balanceChange:balanceChanges){
            timeStamps.add(balanceChange.getTimeStampOfChange());
        }

        return timeStamps;
    }

    private void displayTimeStampsOfBalanceChangesOnChart(ArrayList<Long> timeStampsOfMonths){
        XAxis xAxis = lcStatistics.getXAxis();
        xAxis.setValueFormatter(new DateValueFormatter(timeStampsOfMonths));
    }


    private LineDataSet getBalanceChangesAsLineDataSet(ArrayList<BalanceChange> balanceChanges){
        ArrayList<Entry> balanceChangesAsEntries = new ArrayList<>();
        for (int i = 0; i<balanceChanges.size(); i++){
            Entry entry = getBalanceChangeAsEntry(balanceChanges.get(i), i);
            balanceChangesAsEntries.add(entry);
        }

        return getBalanceChangeEntriesAsLineDataSet(balanceChangesAsEntries);
    }

    private Entry getBalanceChangeAsEntry(BalanceChange balanceChange, int x){
        long formattedAmountOfBalanceChange = balanceChange.getNewBalance() / 100;
        return new Entry(x, formattedAmountOfBalanceChange);
    }

    private LineDataSet getBalanceChangeEntriesAsLineDataSet(ArrayList<Entry> entries){
        LineDataSet lineDataSet = new LineDataSet(entries, "");
        lineDataSet.setValues(entries);

        return lineDataSet;
    }

    private void setupChartStyle(){
        lcStatistics.getLegend().setEnabled(false);
        lcStatistics.getAxisLeft().setEnabled(false);
        lcStatistics.getAxisRight().setEnabled(false);
        lcStatistics.getData().setHighlightEnabled(false);
        lcStatistics.getXAxis().setDrawAxisLine(false);
        lcStatistics.getDescription().setEnabled(false);
        lcStatistics.setExtraOffsets(40f,10f,40f,10f);
        lcStatistics.setTouchEnabled(false);
        lcStatistics.invalidate();

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

    private ArrayList<BalanceChange> getLastBalanceChangesOfMonthsOfBankAccount(BankAccount bankAccount){
        ArrayList<BalanceChange> balanceChanges = new ArrayList<>();

        Toolkit.sortBalanceChangesOfCreationDates(bankAccount.getBalanceChanges());
        long firstBalanceChangeDate = bankAccount.getBalanceChanges().get(0).getTimeStampOfChange();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(firstBalanceChangeDate);

        while (!Toolkit.doesMonthExceedsCurrentTime(calendar)){
            int balanceChangesOfMonthSize = getSizeOfBalanceChangesOfMonth(bankAccount, calendar.getTimeInMillis());
            if (balanceChangesOfMonthSize != 0){
                BalanceChange lastBalanceChangeOfMonth = Toolkit.getLastBalanceChangeOfBankAccountAndMonth(bankAccount, calendar.getTimeInMillis());
                balanceChanges.add(lastBalanceChangeOfMonth);
            }

            calendar.add(Calendar.MONTH, 1);
        }

        return balanceChanges;
    }

    private int getSizeOfBalanceChangesOfMonth(BankAccount bankAccount, long timeStampOfMonth){
        ArrayList<BalanceChange> balanceChangesOfMonth = Toolkit.filterBalanceChangesOfMonth(bankAccount.getBalanceChanges(), timeStampOfMonth);
        return balanceChangesOfMonth.size();
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
