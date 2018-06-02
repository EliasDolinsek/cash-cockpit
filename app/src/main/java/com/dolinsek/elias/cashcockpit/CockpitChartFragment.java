package com.dolinsek.elias.cashcockpit;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dolinsek.elias.cashcockpit.components.AutoPay;
import com.dolinsek.elias.cashcockpit.components.Bill;
import com.dolinsek.elias.cashcockpit.components.Currency;
import com.dolinsek.elias.cashcockpit.components.Database;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class CockpitChartFragment extends Fragment {

    private PieChart pieChart;
    private TextView txvInputAmount, txvCashAmount, txvDailyLimitAmount, txvCreditRateAmount;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View inflatedView = inflater.inflate(R.layout.fragment_cockpit_chart, container, false);;

        pieChart = inflatedView.findViewById(R.id.pc_cockpit);
        txvInputAmount =  inflatedView.findViewById(R.id.txv_cockpit_chart_input_amount);
        txvCashAmount = inflatedView.findViewById(R.id.txv_cockpit_chart_cash_amount);
        txvDailyLimitAmount = inflatedView.findViewById(R.id.txv_cockpit_chart_daily_limit_amount);
        txvCreditRateAmount = inflatedView.findViewById(R.id.txv_cockpit_chart_credit_rate_amount);

        setupPieChart();
        loadPieChart();

        displayTextsOnTextFields();

        return inflatedView;
    }

    private long getAmountOfOutputBillsOfMonth(long timeStampOfMonth){
        ArrayList<Bill> bills = Database.Toolkit.filterBillsOfBillType(Database.Toolkit.getAllBillsInDatabase(), Bill.TYPE_OUTPUT);
        return Database.Toolkit.getTotalAmountOfBills(bills);
    }

    private long getAmountOfTransferBillsOfMonth(long timeStampOfMonth){
        ArrayList<Bill> bills = Database.Toolkit.filterBillsOfBillType(Database.Toolkit.getAllBillsInDatabase(), Bill.TYPE_TRANSFER);
        return Database.Toolkit.getTotalAmountOfBills(bills);
    }

    private long getAmountOfInputBillsOfMonth(long timeStampOfMonth){
        ArrayList<Bill> bills = Database.Toolkit.filterBillsOfBillType(Database.Toolkit.getAllBillsInDatabase(), Bill.TYPE_INPUT);
        return Database.Toolkit.getTotalAmountOfBills(bills);
    }

    private ArrayList<PieEntry> getUsageOfBillsAsPieEntries(long timeStampOfMonth){
        ArrayList<PieEntry> pieEntries = new ArrayList<>();

        long amountOfInput = getAmountOfInputBillsOfMonth(timeStampOfMonth);
        long amountOfTransfer = getAmountOfTransferBillsOfMonth(timeStampOfMonth);
        long amountOfOutput = getAmountOfOutputBillsOfMonth(timeStampOfMonth);

        pieEntries.add(new PieEntry(amountOfInput));
        pieEntries.add(new PieEntry(Math.abs(amountOfTransfer)));
        pieEntries.add(new PieEntry(Math.abs(amountOfOutput)));

        return pieEntries;
    }

    private void loadPieChart(){
        ArrayList<PieEntry> pieEntries = getUsageOfBillsAsPieEntries(System.currentTimeMillis());

        PieDataSet pieDataSet = new PieDataSet(pieEntries, "TODO");
        setupPieDataSet(pieDataSet);
        PieData pieData = new PieData(pieDataSet);

        pieChart.setData(pieData);
    }

    private void setupPieChart(){
        Description description = new Description();
        description.setText("");
        pieChart.setDescription(description);

        pieChart.setHoleRadius(75f);
        pieChart.setUsePercentValues(false);
        pieChart.getLegend().setEnabled(false);
        pieChart.invalidate();
    }

    private void setupPieDataSet(PieDataSet pieDataSet){
        setupPieDataSetColors(pieDataSet);
        pieDataSet.setValueTextSize(0f); //Removes value text-view
    }

    private void setupPieDataSetColors(PieDataSet pieDataSet){
        int[] colors = new int[]{getResources().getColor(R.color.colorGreen), getResources().getColor(android.R.color.holo_red_dark), getResources().getColor(R.color.colorOrange)};
        pieDataSet.setColors(colors);
    }

    private void displayTextsOnTextFields(){
        Currency activeCurrency = Currency.getActiveCurrency(getContext());
        String formattedInputAmount = activeCurrency.formatAmountToReadableStringWithCurrencySymbol(getTotalAmountOfInputBillsOfMonth());
        txvInputAmount.setText(formattedInputAmount);

        String formattedCash = activeCurrency.formatAmountToReadableStringWithCurrencySymbol(getAmountOfCash());
        txvCashAmount.setText(formattedCash);

        String formattedDailyLimit = activeCurrency.formatAmountToReadableStringWithCurrencySymbol(getDailyLimit());
        txvDailyLimitAmount.setText(formattedDailyLimit);

        String formattedCreditRate = activeCurrency.formatAmountToReadableStringWithCurrencySymbol(getCreditRate());
        txvCreditRateAmount.setText(formattedCreditRate);

    }

    private long getTotalAmountOfAutoPayBills(){
        ArrayList<Bill> payedAutoPayBillsOfMonths = Database.Toolkit.filterBillsOfAutoPayBill(Database.Toolkit.getBillsOfMonth(System.currentTimeMillis()));
        return Database.Toolkit.getTotalAmountOfBills(payedAutoPayBillsOfMonths);
    }

    private long getTotalAmountOfOutputBillsOfMonth(){
        ArrayList<Bill> outputBillsOfMonth = Database.Toolkit.filterBillsOfBillType(Database.Toolkit.getBillsOfMonth(System.currentTimeMillis()), Bill.TYPE_OUTPUT);
        return Database.Toolkit.getTotalAmountOfBills(outputBillsOfMonth);
    }

    private long getTotalAmountOfInputBillsOfMonth(){
        ArrayList<Bill> inputBillsOfMonth = Database.Toolkit.filterBillsOfBillType(Database.Toolkit.getBillsOfMonth(System.currentTimeMillis()), Bill.TYPE_INPUT);
        return Database.Toolkit.getTotalAmountOfBills(inputBillsOfMonth);
    }

    private long getAmountOfCash(){
        long amountOfInputOfMonth = getTotalAmountOfInputBillsOfMonth();
        long amountOfAutoPayBillsOfMonth = getTotalAmountOfAutoPayBills();
        long amountOfOutputOfMonth = getTotalAmountOfOutputBillsOfMonth();

        return amountOfInputOfMonth - amountOfAutoPayBillsOfMonth - amountOfOutputOfMonth;
    }

    private long getAmountToSaveEveryMonth(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        return Long.parseLong(sharedPreferences.getString("preference_amount_to_save", "0"));
    }

    private long getCreditRate(){
        long amountOfCash = getAmountOfCash();
        long amountToSave = getAmountToSaveEveryMonth();

        return amountOfCash - amountToSave;
    }

    private long getDailyLimit(){
        long amountOfInputOfMonth = getTotalAmountOfInputBillsOfMonth();
        long amountOfAutoPayBillsOfMonth = getTotalAmountOfAutoPayBills();
        long amountToSave = getAmountToSaveEveryMonth();

        return amountOfInputOfMonth - amountOfAutoPayBillsOfMonth - amountToSave;
    }
}
