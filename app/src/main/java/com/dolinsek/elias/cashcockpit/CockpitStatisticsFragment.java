package com.dolinsek.elias.cashcockpit;


import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dolinsek.elias.cashcockpit.components.AutoPay;
import com.dolinsek.elias.cashcockpit.components.Bill;
import com.dolinsek.elias.cashcockpit.components.Currency;
import com.dolinsek.elias.cashcockpit.components.Toolkit;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;


/**
 * A simple {@link Fragment} subclass.
 */
public class CockpitStatisticsFragment extends Fragment {

    private TextView txvCash, txvTotalOutputs, txvInstallment, txvDailyLimit;
    private ProgressBar pgbCash, pgbTotalOutputs, pgbInstallment, pgbDailyLimit;
    private PieChart pieChart;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflatedView = inflater.inflate(R.layout.fragment_cockpit_statistics, container, false);

        pieChart = inflatedView.findViewById(R.id.pc_cockpit_statistics);
        txvCash = inflatedView.findViewById(R.id.txv_cockpit_statistics_cash_amount);
        txvTotalOutputs = inflatedView.findViewById(R.id.txv_cockpit_statistics_total_outputs_amount);
        txvInstallment = inflatedView.findViewById(R.id.txv_cockpit_statistics_installment_amount);
        txvDailyLimit = inflatedView.findViewById(R.id.txv_cockpit_statistics_daily_limit_amount);

        pgbCash = inflatedView.findViewById(R.id.pgb_cockpit_statistics_cash);
        pgbTotalOutputs = inflatedView.findViewById(R.id.pgb_cockpit_statistics_total_outputs);
        pgbInstallment = inflatedView.findViewById(R.id.pgb_cockpit_statistics_installment);
        pgbDailyLimit = inflatedView.findViewById(R.id.pgb_cockpit_statistics_daily_limit);

        setupPieChart();

        return inflatedView;
    }

    @Override
    public void onStart() {
        super.onStart();

        loadPieChart();
        displayAmounts();
        displayProgresses();
    }

    private void displayAmounts(){
        Currency activeCurrency = Currency.getActiveCurrency(getContext());

        String formattedCash = activeCurrency.formatAmountToReadableStringWithoutCentsWithCurrencySymbol(getDisplayCash()),
                formattedTotalOutputs = activeCurrency.formatAmountToReadableStringWithoutCentsWithCurrencySymbol(getTotalOutputs()),
                formattedInstallment = activeCurrency.formatAmountToReadableStringWithoutCentsWithCurrencySymbol(getInstallment()),
                formattedDailyLimit = activeCurrency.formatAmountToReadableStringWithoutCentsWithCurrencySymbol(getDailyLimit());

        txvCash.setText(formattedCash);
        txvTotalOutputs.setText(formattedTotalOutputs);
        txvInstallment.setText(formattedInstallment);
        txvDailyLimit.setText(formattedDailyLimit);
    }

    private void displayProgresses(){
        pgbCash.setProgress(getProgressOfDisplayAmount(getDisplayCash()));
        pgbTotalOutputs.setProgress(getProgressOfDisplayAmount(getTotalOutputs()));
        pgbInstallment.setProgress(getProgressOfDisplayAmount(getInstallment()));
        pgbDailyLimit.setProgress(getProgressOfDisplayAmount(getDailyLimit()));
    }

    private int getProgressOfDisplayAmount(long value){
        if (value != 0){
            return (int) (value * 100 / getTotalOfAllDisplayAmounts());
        } else {
            return 0;
        }
    }

    private long getTotalOfAllDisplayAmounts(){
        return getDisplayCash() + getTotalOutputs() + getInstallment() + getDailyLimit();
    }

    private long convertToNonNegativeAmountToDisplay(long amount){
        return amount >= 0 ? amount : 0;
    }

    private long getDailyLimit(){
        long dailyLimit = (getAllInputs() + Toolkit.getBankAccountsBalance() - getTotalOutputs() - getAmountToSaveEveryMonth()) / getRemainingDaysUntilMonthEnds();
        return convertToNonNegativeAmountToDisplay(dailyLimit);
    }

    private long getInstallment(){
        long installment = getCash() - getAmountToSaveEveryMonth();
        return convertToNonNegativeAmountToDisplay(installment);
    }

    private long getCash(){
        return getAllInputs() - getTotalOutputs();
    }

    private long getDisplayCash(){
        long cash = getCash() + Toolkit.getBankAccountsBalance();
        return convertToNonNegativeAmountToDisplay(cash);
    }

    private long getTotalOutputs(){
        ArrayList<Bill> outputTransferBillsOfMonth = new ArrayList<>();
        outputTransferBillsOfMonth.addAll(Toolkit.getBillsByTypeAndMonth(Bill.TYPE_OUTPUT, System.currentTimeMillis()));
        outputTransferBillsOfMonth.addAll(Toolkit.getBillsByTypeAndMonth(Bill.TYPE_TRANSFER, System.currentTimeMillis()));

        long totalOutputs = getFixedCosts() + Toolkit.getBillsTotalAmount(outputTransferBillsOfMonth);
        return convertToNonNegativeAmountToDisplay(totalOutputs);
    }

    private long getFixedCosts(){
        ArrayList<AutoPay> outputTransferAutoPays = new ArrayList<>();
        outputTransferAutoPays.addAll(Toolkit.getAutoPaysByBillType(Bill.TYPE_OUTPUT));
        outputTransferAutoPays.addAll(Toolkit.getAutoPaysByBillType(Bill.TYPE_TRANSFER));

        outputTransferAutoPays = getAutoPaysOfMonth(outputTransferAutoPays);
        return Toolkit.getAutoPaysAmount(outputTransferAutoPays);
    }

    private long getAllInputs(){
        ArrayList<Bill> inputBills = Toolkit.getBillsByTypeAndMonth(Bill.TYPE_INPUT, System.currentTimeMillis());
        long billInputs = Toolkit.getBillsTotalAmount(inputBills);

        ArrayList<AutoPay> inputAutoPays = Toolkit.getAutoPaysByBillType(Bill.TYPE_INPUT);
        inputAutoPays = getAutoPaysOfMonth(inputAutoPays);
        long autoPayInputs = Toolkit.getAutoPaysAmount(inputAutoPays);

        return billInputs + autoPayInputs;
    }

    private long getAmountToSaveEveryMonth(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        return sharedPreferences.getLong("preference_amount_to_save", 100);
    }

    private int getRemainingDaysUntilMonthEnds(){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        int days = 0, month = calendar.get(Calendar.MONTH);
        while (calendar.get(Calendar.MONTH) == month){
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            days++;
        }

        return days;
    }

    private ArrayList<AutoPay> getAutoPaysOfMonth(ArrayList<AutoPay> autoPays){
        ArrayList<AutoPay> autoPaysToReturn = new ArrayList<>();

        ArrayList<AutoPay> weeklyAutoPays = Toolkit.filterAutoPaysByType(autoPays, AutoPay.TYPE_WEEKLY);
        ArrayList<AutoPay> monthlyAutoPays = Toolkit.filterAutoPaysByType(autoPays, AutoPay.TYPE_MONTHLY);
        ArrayList<AutoPay> yearlyAutoPays = Toolkit.filterAutoPaysByType(autoPays, AutoPay.TYPE_YEARLY);

        for (int i = 0; i<4; i++){
            autoPaysToReturn.addAll(weeklyAutoPays);
        }

        autoPaysToReturn.addAll(monthlyAutoPays);

        if (isCurrentMonthJanuary()){
            autoPaysToReturn.addAll(yearlyAutoPays);
        }

        return autoPaysToReturn;
    }

    private boolean isCurrentMonthJanuary(){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        return calendar.get(Calendar.MONTH) == Calendar.JANUARY;
    }

    /*
     *
     * PieChart
     *
    */

    private void setupPieChart(){
        pieChart.setDescription(null);
        pieChart.setHoleRadius(70f);
        pieChart.setDrawEntryLabels(false);
        pieChart.getLegend().setEnabled(true);
        pieChart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        pieChart.getLegend().setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        pieChart.getLegend().setTextSize(14f);
        pieChart.setExtraOffsets(0,-4,0,-4);

        pieChart.invalidate();
    }

    private void loadPieChart(){
        ArrayList<PieEntry> pieEntries = getUsageOfBillsAsPieEntries();

        PieDataSet pieDataSet = new PieDataSet(pieEntries, "");
        setupPieDataSet(pieDataSet);
        PieData pieData = new PieData(pieDataSet);

        pieChart.setData(pieData);
    }

    private void setupPieDataSet(PieDataSet pieDataSet){
        setupPieDataSetColorsDependingOnAvailableData(pieDataSet, isAmountOfFixCostsGreaterThanNull(), isAmountOfInputsGreaterThanNull(), isAmountOfOutputsGreaterThanNull(), isAmountOfTransfersGreaterThanNull());
        pieDataSet.setDrawValues(false);
    }

    private void setupPieDataSetColorsDependingOnAvailableData(PieDataSet pieDataSet, boolean amountOfFixCostsGreaterThanNull, boolean amountOfInputsGreaterThanNull, boolean amountOfOutputGreaterThanNull, boolean amountOfTransfersGreaterThanNull){
        ArrayList<Integer> colors = new ArrayList<>();

        if (amountOfInputsGreaterThanNull){
            colors.add(getResources().getColor(R.color.colorCockpitChartEntriesInput));
        }

        if (amountOfOutputGreaterThanNull){
            colors.add(getResources().getColor(R.color.colorCockpitChartEntriesOutput));
        }

        if (amountOfTransfersGreaterThanNull){
            colors.add(getResources().getColor(R.color.colorCockpitChartEntriesTransfer));
        }

        if (amountOfFixCostsGreaterThanNull){
            colors.add(getResources().getColor(R.color.colorCockpitChartEntriesFixedCosts));
        }

        pieDataSet.setColors(colors);
    }

    private boolean isAmountOfFixCostsGreaterThanNull(){
        return getFixedCosts() != 0;
    }

    private boolean isAmountOfOutputsGreaterThanNull(){
        return Math.abs(getAmountOfBillsOfBillTypeOfMonth(Bill.TYPE_OUTPUT)) != 0;
    }

    private boolean isAmountOfInputsGreaterThanNull(){
        return getAmountOfBillsOfBillTypeOfMonth(Bill.TYPE_INPUT) != 0;
    }

    private boolean isAmountOfTransfersGreaterThanNull(){
        return getAmountOfBillsOfBillTypeOfMonth(Bill.TYPE_TRANSFER) != 0;
    }

    private long getAmountOfBillsOfBillTypeOfMonth(int billType){
        ArrayList<Bill> bills = Toolkit.getBillsByTypeAndMonth(billType, System.currentTimeMillis());
        return Toolkit.getBillsAmount(bills);
    }

    private ArrayList<PieEntry> getUsageOfBillsAsPieEntries(){
        ArrayList<PieEntry> pieEntries = new ArrayList<>();

        long amountOfFixedCosts = getFixedCosts() / 100;
        long amountOfOutputs = Math.abs(getAmountOfBillsOfBillTypeOfMonth(Bill.TYPE_OUTPUT)) / 100;
        long amountOfInputs = getAmountOfBillsOfBillTypeOfMonth(Bill.TYPE_INPUT) / 100;
        long amountOfTransfers = getAmountOfBillsOfBillTypeOfMonth(Bill.TYPE_TRANSFER) / 100;

        addNewPieEntryToPieEntriesIfValueIsNotNull(amountOfInputs, getString(R.string.label_inputs), pieEntries);
        addNewPieEntryToPieEntriesIfValueIsNotNull(Math.abs(amountOfOutputs), getString(R.string.label_outputs), pieEntries);
        addNewPieEntryToPieEntriesIfValueIsNotNull(Math.abs(amountOfTransfers), getString(R.string.label_transfers), pieEntries);
        addNewPieEntryToPieEntriesIfValueIsNotNull(amountOfFixedCosts, getString(R.string.label_fixed_costs), pieEntries);

        return pieEntries;
    }

    private void addNewPieEntryToPieEntriesIfValueIsNotNull(long amount, String label, ArrayList<PieEntry> pieEntries){
        if (amount != 0){
            String textToDisplay = label + " " + Currency.getActiveCurrency(getContext()).formatAmountToReadableStringWithoutCentsWithCurrencySymbol(amount * 100);
            pieEntries.add(new PieEntry(amount, textToDisplay));
        }
    }

}
