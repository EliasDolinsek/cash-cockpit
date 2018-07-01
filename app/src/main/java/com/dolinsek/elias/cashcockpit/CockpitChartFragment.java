package com.dolinsek.elias.cashcockpit;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.icu.text.UnicodeSetSpanner;
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

    public void refreshData(){
        loadPieChart();
        pieChart.notifyDataSetChanged();
        pieChart.invalidate();
    }

    private long getAmountOfBillsOfBillTypeOfMonth(int billType){
        ArrayList<Bill> allBillsOfMonth = Database.Toolkit.getBillsOfMonth(System.currentTimeMillis());
        ArrayList<Bill> filteredBillsOfMonth = Database.Toolkit.filterBillsOfBillType(allBillsOfMonth, billType);
        return Database.Toolkit.getTotalAmountOfBills(filteredBillsOfMonth);
    }

    private long getAmountOfFixedCosts(){
        ArrayList<AutoPay> autoPaysYearly = Database.Toolkit.getAutoPaysOfType(AutoPay.TYPE_YEARLY);
        ArrayList<AutoPay> autoPaysMonthly = Database.Toolkit.getAutoPaysOfType(AutoPay.TYPE_MONTHLY);
        ArrayList<AutoPay> autoPaysWeekly = Database.Toolkit.getAutoPaysOfType(AutoPay.TYPE_WEEKLY);

        ArrayList<AutoPay> autoPays = new ArrayList<>(autoPaysMonthly);

        for (int i = 0; i < 4; i++){
            autoPays.addAll(autoPaysWeekly);
        }

        if (isCurrentMonthJanuary()){
            autoPays.addAll(autoPaysYearly);
        }

        ArrayList<AutoPay> autoPaysWithBillTypeInput = Database.Toolkit.filterAutoPaysOfAutoPayBillType(autoPays, Bill.TYPE_INPUT);
        autoPays.removeAll(autoPaysWithBillTypeInput);

        return Database.Toolkit.getAmountOfAutoPays(autoPays);
    }

    private boolean isCurrentMonthJanuary(){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        return calendar.get(Calendar.MONTH) == Calendar.JANUARY;
    }

    private ArrayList<PieEntry> getUsageOfBillsAsPieEntries(){
        ArrayList<PieEntry> pieEntries = new ArrayList<>();

        long amountOfFixedCosts = getAmountOfFixedCosts() / 100;
        long amountOfOutput = Math.abs(getAmountOfBillsOfBillTypeOfMonth(Bill.TYPE_OUTPUT)) / 100;
        long amountOfInput = getAmountOfBillsOfBillTypeOfMonth(Bill.TYPE_INPUT) / 100;

        addNewPieEntryToPieEntriesIfValueIsNotNull(amountOfFixedCosts, getString(R.string.label_fixed_costs), pieEntries);
        addNewPieEntryToPieEntriesIfValueIsNotNull(amountOfInput, getString(R.string.label_input), pieEntries);
        addNewPieEntryToPieEntriesIfValueIsNotNull(Math.abs(amountOfOutput), getString(R.string.label_output), pieEntries);

        return pieEntries;
    }

    private void addNewPieEntryToPieEntriesIfValueIsNotNull(long amount, String label, ArrayList<PieEntry> pieEntries){
        if (amount != 0){
            pieEntries.add(new PieEntry(amount, label));
        }
    }

    private void loadPieChart(){
        ArrayList<PieEntry> pieEntries = getUsageOfBillsAsPieEntries();

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
        pieChart.setEntryLabelColor(getResources().getColor(R.color.colorAccentTextColor));
        pieChart.getLegend().setEnabled(false);
        pieChart.setHoleRadius(78f);
        pieChart.invalidate();
    }

    private void setupPieDataSet(PieDataSet pieDataSet){
        setupPieDataSetColors(pieDataSet);
        pieDataSet.setValueTextSize(15f);
        pieDataSet.setValueTextColor(getResources().getColor(R.color.colorAccentTextColor));
        pieDataSet.setSliceSpace(5f);

        CurrencyEntryValueFormatter currencyEntryValueFormatter = new CurrencyEntryValueFormatter(getContext());
        pieDataSet.setValueFormatter(currencyEntryValueFormatter);
    }

    private void setupPieDataSetColors(PieDataSet pieDataSet){
        int[] colors = new int[]{getResources().getColor(R.color.colorCockpitChartEntriesFixedCosts), getResources().getColor(R.color.colorCockpitChartEntriesInput), getResources().getColor(R.color.colorCockpitChartEntriesOutput)};
        pieDataSet.setColors(colors);
    }

    private void displayTextsOnTextFields(){
        Currency activeCurrency = Currency.getActiveCurrency(getContext());

        long totalOutputsAmount = Math.abs(getAmountOfTotalOutputsOfMonths());
        String formattedTotalOutputsAmount = activeCurrency.formatAmountToReadableStringWithCurrencySymbol(totalOutputsAmount);
        txvTotalOutputsAmount.setText(formattedTotalOutputsAmount);

        String formattedCash = activeCurrency.formatAmountToReadableStringWithCurrencySymbol(getAmountOfCash());
        txvCashAmount.setText(formattedCash);

        String formattedDailyLimit = activeCurrency.formatAmountToReadableStringWithCurrencySymbol(getAmountOfDailyLimitOfMonth());
        txvDailyLimitAmount.setText(formattedDailyLimit);

        String formattedCreditRate = activeCurrency.formatAmountToReadableStringWithCurrencySymbol(getAmountOfCreditRateOfMonth());
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

    private long getAmountToSaveEveryMonth(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        return Long.parseLong(sharedPreferences.getString("preference_amount_to_save", "0")) * 100;
    }

    private long getAmountOfCash(){
        long amountOfTotalInputsOfMonth = getAmountOfTotalInputsOfMonthIncludingAutoPays();
        long amountOfFixedCostsOfMonth = getAmountOfFixedCosts();
        long amountOfOutputsOfMonth = Math.abs(getAmountOfOutputsOfMonth());

        return amountOfTotalInputsOfMonth - amountOfFixedCostsOfMonth - amountOfOutputsOfMonth;
    }

    private long getAmountOfTotalInputsOfMonthIncludingAutoPays(){
        ArrayList<AutoPay> autoPaysWithBillTypeInput = Database.Toolkit.filterAutoPaysOfAutoPayBillType(Database.getAutoPays(), Bill.TYPE_INPUT);

        long amountOfInputsOfMonth = getAmountOfBillsOfBillTypeOfMonth(Bill.TYPE_INPUT);
        long amountOfInputTypeAutoPaysOfMonth = Database.Toolkit.getAmountOfAutoPays(autoPaysWithBillTypeInput);
        return amountOfInputsOfMonth + amountOfInputTypeAutoPaysOfMonth;
    }

    private long getAmountOfOutputsOfMonth(){
        ArrayList<Bill> billsOfMonth = Database.Toolkit.getBillsOfMonth(System.currentTimeMillis());
        ArrayList<Bill> filteredBillsOfMonth = new ArrayList<>(billsOfMonth);
        filteredBillsOfMonth.removeAll(Database.Toolkit.filterBillsOfBillType(filteredBillsOfMonth, Bill.TYPE_INPUT));

        return Database.Toolkit.getTotalAmountOfBills(filteredBillsOfMonth);
    }

    private long getAmountOfCreditRateOfMonth(){
        long amountOfCashOfMonth = getAmountOfCash();
        long amountToSaveEveryMonth = getAmountToSaveEveryMonth();

        long creditRate =  amountOfCashOfMonth - amountToSaveEveryMonth;

        if (creditRate < 0){
            return 0;
        } else {
            return creditRate;
        }
    }

    private long getAmountOfDailyLimitOfMonth(){
        long totalInputsOfMonthIncludingAutoPays = getAmountOfTotalInputsOfMonthIncludingAutoPays();
        long fixedCostsOfMonth = getAmountOfFixedCosts();
        long amountToSaveEveryMonth = getAmountToSaveEveryMonth();

        long dailyLimit = totalInputsOfMonthIncludingAutoPays - fixedCostsOfMonth - amountToSaveEveryMonth;

        if (dailyLimit < 0){
            return 0;
        } else {
            return dailyLimit;
        }
    }
}
