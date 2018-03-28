package com.dolinsek.elias.cashcockpit;


import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dolinsek.elias.cashcockpit.components.BalanceChange;
import com.dolinsek.elias.cashcockpit.components.BankAccount;
import com.dolinsek.elias.cashcockpit.components.Bill;
import com.dolinsek.elias.cashcockpit.components.Database;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;


/**
 * A simple {@link Fragment} subclass.
 */
public class BankAccountsStatisticsFragment extends Fragment {

    private static final int STEP_ONE_BANK_ACCOUNT_FORWARD = 1;
    private static final int STEP_ONE_BANK_ACCOUNT_BACK = -1;

    private FloatingActionButton fbtnNextBankAccount, fbtnPreviousBankAccount;
    private TextView txvCurrentBankAccount;
    private RecyclerView rvBills;
    private LineChart lcStatistics;
    private BankAccount currentBankAccount;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View inflatedView =  inflater.inflate(R.layout.fragment_bank_accounts_statistics, container, false);

        fbtnNextBankAccount = inflatedView.findViewById(R.id.fbtn_bank_accounts_statistics_next);
        fbtnPreviousBankAccount = inflatedView.findViewById(R.id.fbtn_bank_accounts_statistics_previous);
        txvCurrentBankAccount = inflatedView.findViewById(R.id.txv_bank_accounts_statistics_current_bank_account);
        rvBills = inflatedView.findViewById(R.id.rv_bank_account_statistics);
        lcStatistics = inflatedView.findViewById(R.id.lc_bank_accounts_statistics);

        rvBills.setLayoutManager(new LinearLayoutManager(getContext()));

        if (Database.getBankAccounts().size() != 0){
            currentBankAccount = Database.getBankAccounts().get(0);
            loadStatistics();
        } else {
            //TODO display message
        }

        fbtnNextBankAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stepOneBankAccountForward();
                loadStatistics();
            }
        });

        fbtnPreviousBankAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stepOneBankAccountBack();
                loadStatistics();
            }
        });

        return inflatedView;
    }

    private void loadStatistics(){
        loadCurrentBankAccountText(currentBankAccount);
        loadStatisticsOfBankAccount(currentBankAccount);
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

        throw new IllegalArgumentException("Couldn't find bank account in databse!");
    }

    private void loadStatisticsOfBankAccount(BankAccount bankAccount){
        HistoryItemAdapter historyItemAdapter = HistoryItemAdapter.getBankAccountHistoryItemAdapter(bankAccount);
        rvBills.setAdapter(historyItemAdapter);
    }

    private void loadPcStatistics(BankAccount bankAccount){
        //ArrayList<PieEntry> balanceChangesOfLastMonthsAsPieEntries = getLastBalanceChangesOfMonthsAsEntriesOfBankAccount(bankAccount);
        //PieDataSet pieDataSet = new PieDataSet(balanceChangesOfLastMonthsAsPieEntries, "");
        //PieData pieData = new PieData(pieDataSet);
        //lcStatistics
    }

    private ArrayList<Entry> getLastBalanceChangesOfMonthsAsEntriesOfBankAccount(BankAccount bankAccount){
        sortBalanceChangesOfCreationDates(bankAccount.getBalanceChanges());
        long creationDateOfFirstBalanceChange = bankAccount.getBalanceChanges().get(0).getTimeStampOfChange();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(creationDateOfFirstBalanceChange);

        ArrayList<Entry> entries = new ArrayList<>();
        while (calendar.getTimeInMillis() < System.currentTimeMillis()){
            ArrayList<BalanceChange> balanceChangesOfThisMonth = getBalanceChangesOfMonth(bankAccount, calendar.getTimeInMillis());
            sortBalanceChangesOfCreationDates(balanceChangesOfThisMonth);

            int lastBalanceChangeOfMonthIndex = balanceChangesOfThisMonth.size() - 1;
            BalanceChange lastBalanceChangeOfMonth = balanceChangesOfThisMonth.get(lastBalanceChangeOfMonthIndex);
            entries.add(getBalanceChangesAsEntry(lastBalanceChangeOfMonth));

            calendar.add(Calendar.MONTH, 1);
        }

        return entries;
    }

    private Entry getBalanceChangesAsEntry(BalanceChange balanceChange){
        return new Entry(balanceChange.getNewBalance(), 0);
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
