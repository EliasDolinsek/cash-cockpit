package com.dolinsek.elias.cashcockpit;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.dolinsek.elias.cashcockpit.components.AutoPay;
import com.dolinsek.elias.cashcockpit.components.Bill;
import com.dolinsek.elias.cashcockpit.components.Currency;
import com.dolinsek.elias.cashcockpit.components.Database;
import com.dolinsek.elias.cashcockpit.components.Toolbox;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;

import java.time.Year;
import java.util.ArrayList;
import java.util.Calendar;


/**
 * A simple {@link Fragment} subclass.
 */
public class CockpitChartFragment extends Fragment {

    private PieChart pieChart;
    private TextView txvTotalOutputsAmount, txvCashAmount, txvDailyLimitAmount, txvCreditRateAmount;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View inflatedView = inflater.inflate(R.layout.fragment_cockpit_chart, container, false);;

        pieChart = inflatedView.findViewById(R.id.pc_cockpit);
        txvTotalOutputsAmount =  inflatedView.findViewById(R.id.txv_cockpit_chart_total_outputs_amount);
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

    private long getAmountOfAutoPaysWhichBelongToCurrentMonth(){
        ArrayList<AutoPay> autoPays = new ArrayList<>();

        ArrayList<AutoPay> autoPaysYearly = Database.Toolkit.getAutoPaysOfType(AutoPay.TYPE_YEARLY);
        ArrayList<AutoPay> autoPaysMonthly = Database.Toolkit.getAutoPaysOfType(AutoPay.TYPE_MONTHLY);
        ArrayList<AutoPay> autoPaysWeekly = Database.Toolkit.getAutoPaysOfType(AutoPay.TYPE_WEEKLY);

        autoPays.addAll(autoPaysMonthly);

        for (int i = 0; i < 4; i++){
            autoPays.addAll(autoPaysWeekly);
        }

        if (isCurrentMonthDescember()){
            autoPays.addAll(autoPaysYearly);
        }

        autoPays = Database.Toolkit.removeAutoPayBillTypeInputFromCollection(autoPays);
        return Database.Toolkit.getAmountOfAutoPays(autoPays);
    }

    private boolean isCurrentMonthDescember(){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        return calendar.get(Calendar.MONTH) == Calendar.DECEMBER;
    }

    private ArrayList<PieEntry> getUsageOfBillsAsPieEntries(long timeStampOfMonth){
        ArrayList<PieEntry> pieEntries = new ArrayList<>();

        long amountOfAutoPays = getAmountOfAutoPaysWhichBelongToCurrentMonth() / 100;
        long amountOfOutput = getAmountOfOutputBillsOfMonth(timeStampOfMonth) / 100;

        ArrayList<Bill> billsOfMonth = Database.Toolkit.getBillsOfMonth(System.currentTimeMillis());
        ArrayList<Bill> inputBillsOfMonth = Database.Toolkit.filterBillsOfBillType(billsOfMonth, Bill.TYPE_INPUT);
        long amountOfInput = Database.Toolkit.getTotalAmountOfBills(inputBillsOfMonth) / 100;

        pieEntries.add(new PieEntry(amountOfAutoPays, getString(R.string.label_fixed_costs)));
        pieEntries.add(new PieEntry(amountOfInput, getString(R.string.label_input)));
        pieEntries.add(new PieEntry(Math.abs(amountOfOutput), getString(R.string.label_output)));

        return pieEntries;
    }

    private void loadPieChart(){
        ArrayList<PieEntry> pieEntries = getUsageOfBillsAsPieEntries(System.currentTimeMillis());

        PieDataSet pieDataSet = new PieDataSet(pieEntries, "");
        setupPieDataSet(pieDataSet);
        PieData pieData = new PieData(pieDataSet);

        pieChart.setData(pieData);
    }

    private void setupPieChart(){
        Description description = new Description();
        description.setText("");
        pieChart.setDescription(description);

        pieChart.setEntryLabelTextSize(12f);
        pieChart.setEntryLabelColor(getResources().getColor(android.R.color.darker_gray));
        pieChart.getLegend().setEnabled(false);
        pieChart.setHoleRadius(78f);
        pieChart.invalidate();
    }

    private void setupPieDataSet(PieDataSet pieDataSet){
        setupPieDataSetColors(pieDataSet);
        pieDataSet.setValueTextSize(15f);
        pieDataSet.setValueTextColor(getResources().getColor(android.R.color.darker_gray));
        pieDataSet.setSliceSpace(5f);

        CurrencyEntryValueFormatter currencyEntryValueFormatter = new CurrencyEntryValueFormatter(getContext());
        pieDataSet.setValueFormatter(currencyEntryValueFormatter);
    }

    private void setupPieDataSetColors(PieDataSet pieDataSet){
        int[] colors = new int[]{getResources().getColor(R.color.colorAccentLight), getResources().getColor(R.color.colorAccent), getResources().getColor(R.color.colorAccentDark)};
        pieDataSet.setColors(colors);
    }

    private void displayTextsOnTextFields(){
        Currency activeCurrency = Currency.getActiveCurrency(getContext());

        long totalOutputsAmount = Math.abs(getAmountOfTotalOutputsOfMonths());
        String formattedTotalOutputsAmount = activeCurrency.formatAmountToReadableStringWithCurrencySymbol(totalOutputsAmount);
        txvTotalOutputsAmount.setText(formattedTotalOutputsAmount);

        String formattedCash = activeCurrency.formatAmountToReadableStringWithCurrencySymbol(getAmountOfCash());
        txvCashAmount.setText(formattedCash);

        String formattedDailyLimit = activeCurrency.formatAmountToReadableStringWithCurrencySymbol(getDailyLimit());
        txvDailyLimitAmount.setText(formattedDailyLimit);

        String formattedCreditRate = activeCurrency.formatAmountToReadableStringWithCurrencySymbol(getCreditRate());
        txvCreditRateAmount.setText(formattedCreditRate);

    }

    private long getAmountOfTotalOutputsOfMonths(){
        ArrayList<Bill> billsOfMonth = Database.Toolkit.getBillsOfMonth(System.currentTimeMillis());
        ArrayList<Bill> filteredBillsOfMonth = Database.Toolkit.removeAutoPayBillsFromCollection(billsOfMonth);

        ArrayList<AutoPay> autoPaysWithBillTypeOutputTransfer = getAutoPaysWithBillTypeOutputTransfer();
        long amountOfAutoPays = Database.Toolkit.getAmountOfAutoPays(autoPaysWithBillTypeOutputTransfer);

        long amountOfTotalOutputsOfMonth = Database.Toolkit.getTotalAmountOfBills(filteredBillsOfMonth);
        amountOfTotalOutputsOfMonth -= amountOfAutoPays;

        return amountOfTotalOutputsOfMonth;
    }

    private ArrayList<AutoPay> getAutoPaysWithBillTypeOutputTransfer(){
        ArrayList<AutoPay> autoPays = new ArrayList<>();

        for (AutoPay autoPay:Database.getAutoPays()){
            int autoPayBillType = autoPay.getBill().getType();

            if (autoPayBillType == Bill.TYPE_OUTPUT || autoPayBillType == Bill.TYPE_TRANSFER){
                autoPays.add(autoPay);
            }
        }

        return autoPays;
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
