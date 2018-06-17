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
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.time.Year;
import java.util.ArrayList;
import java.util.Calendar;


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
        long amountOfCash = getAmountOfCash() / 100;

        pieEntries.add(new PieEntry(amountOfAutoPays, getString(R.string.auto_pay)));
        pieEntries.add(new PieEntry(amountOfCash, getString(R.string.label_cash)));
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

        pieChart.setEntryLabelTextSize(17f);
        pieChart.setEntryLabelColor(getResources().getColor(R.color.colorPrimary));
        pieChart.getLegend().setEnabled(false);
        pieChart.setHoleRadius(75f);
        //pieChart.setHoleColor(getContext().getResources().getColor(R.color.colorCockpitChartHole));
        pieChart.setTouchEnabled(false);
        pieChart.invalidate();

        setupPieChartSizes();
    }

    private void setupPieDataSet(PieDataSet pieDataSet){
        setupPieDataSetColors(pieDataSet);
        pieDataSet.setValueTextSize(12f);
        pieDataSet.setValueTextColor(Color.WHITE);

        CurrencyEntryValueFormatter currencyEntryValueFormatter = new CurrencyEntryValueFormatter(getContext());
        pieDataSet.setValueFormatter(currencyEntryValueFormatter);
    }

    private void setupPieDataSetColors(PieDataSet pieDataSet){
        int[] colors = new int[]{getResources().getColor(R.color.colorOrange), getResources().getColor(android.R.color.holo_blue_dark), getResources().getColor(android.R.color.holo_red_dark)};
        pieDataSet.setColors(colors);
    }

    private void setupPieChartSizes(){
        int margin = 100;
        int height = (int) (getDefaultDisplayWidth() - margin);

        pieChart.getLayoutParams().height = height;
        pieChart.requestLayout();
    }

    private double getDefaultDisplayWidth(){
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        Point size = new Point();
        display.getSize(size);

        return size.x;
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
