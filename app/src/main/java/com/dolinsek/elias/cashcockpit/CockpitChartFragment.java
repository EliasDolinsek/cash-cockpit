package com.dolinsek.elias.cashcockpit;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dolinsek.elias.cashcockpit.components.AutoPay;
import com.dolinsek.elias.cashcockpit.components.Bill;
import com.dolinsek.elias.cashcockpit.components.Currency;
import com.dolinsek.elias.cashcockpit.components.Database;
import com.dolinsek.elias.cashcockpit.components.Toolbox;
import com.dolinsek.elias.cashcockpit.components.Toolkit;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.Calendar;


/**
 * A simple {@link Fragment} subclass.
 */
public class CockpitChartFragment extends Fragment {

    private PieChart pieChart;
    private TextView txvTotalOutputsAmount, txvCashAmount, txvDailyLimitAmount, txvCreditRateAmount;
    private LinearLayout llCockpitChartRoot;
    private NotEnoughDataFragment fgmNotEnoughData;
    private GridLayout glTextsContainer;
    private ImageView imvCockpitChartSettings;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View inflatedView = inflater.inflate(R.layout.fragment_cockpit_chart, container, false);;

        pieChart = inflatedView.findViewById(R.id.pc_cockpit);
        txvTotalOutputsAmount =  inflatedView.findViewById(R.id.txv_cockpit_chart_total_outputs_amount);
        txvCashAmount = inflatedView.findViewById(R.id.txv_cockpit_chart_cash_amount);
        txvDailyLimitAmount = inflatedView.findViewById(R.id.txv_cockpit_chart_daily_limit_amount);
        txvCreditRateAmount = inflatedView.findViewById(R.id.txv_cockpit_chart_credit_rate_amount);

        fgmNotEnoughData = (NotEnoughDataFragment) getChildFragmentManager().findFragmentById(R.id.fgm_cockpit_chart_not_enough_data);
        glTextsContainer = inflatedView.findViewById(R.id.gl_cockpit_chart_texts_container);
        llCockpitChartRoot = inflatedView.findViewById(R.id.ll_cockpit_chart_root);

        imvCockpitChartSettings = inflatedView.findViewById(R.id.imv_cockpit_chart_settings);
        imvCockpitChartSettings.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CockpitChartPreferencesActivity.class);
            startActivity(intent);
        });

        setupPieChart();
        loadPieChart();

        displayTextsOnTextFields();
        manageViewsDependingOnAvailableData();

        return inflatedView;
    }

    private void manageViewsDependingOnAvailableData(){
        if (isAmountOfFixCostsGreaterThanNull() || isAmountOfInputsGreaterThanNull() || isAmountOfOutputsGreaterThanNull()){
            fgmNotEnoughData.hide();
            pieChart.setVisibility(View.VISIBLE);
            glTextsContainer.setVisibility(View.VISIBLE);
        } else {
            fgmNotEnoughData.show();
            pieChart.setVisibility(View.GONE);
            glTextsContainer.setVisibility(View.GONE);
        }
    }

    public void refreshData(){
        manageViewsDependingOnAvailableData();
        displayTextsOnTextFields();
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

        long amountOfFixedCosts = getAmountOfFixedCosts();
        long amountOfOutputs = Math.abs(getAmountOfBillsOfBillTypeOfMonth(Bill.TYPE_OUTPUT));
        long amountOfInputs = getAmountOfBillsOfBillTypeOfMonth(Bill.TYPE_INPUT);
        long amountOfTransfers = getAmountOfBillsOfBillTypeOfMonth(Bill.TYPE_TRANSFER);

        addNewPieEntryToPieEntriesIfValueIsNotNull(amountOfInputs, getString(R.string.label_inputs), pieEntries);
        addNewPieEntryToPieEntriesIfValueIsNotNull(Math.abs(amountOfOutputs), getString(R.string.label_outputs), pieEntries);
        addNewPieEntryToPieEntriesIfValueIsNotNull(Math.abs(amountOfTransfers), getString(R.string.label_transfers), pieEntries);
        addNewPieEntryToPieEntriesIfValueIsNotNull(amountOfFixedCosts, getString(R.string.label_fixed_costs), pieEntries);

        return pieEntries;
    }

    private void addNewPieEntryToPieEntriesIfValueIsNotNull(long amount, String label, ArrayList<PieEntry> pieEntries){
        if (amount != 0){
            String textToDisplay = label + " (" + Currency.getActiveCurrency(getContext()).formatAmountToReadableStringWithoutCentsWithCurrencySymbol(amount) + ") ";
            pieEntries.add(new PieEntry(amount, textToDisplay));
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
        pieChart.setDescription(null);
        pieChart.setDrawEntryLabels(false);
        pieChart.getLegend().setTextSize(12f);
        pieChart.setHoleRadius(78f);
        pieChart.setExtraOffsets(0,0,0,-10);
        pieChart.invalidate();
    }

    private void setupPieDataSet(PieDataSet pieDataSet){
        setupPieDataSetColorsDependingOnAvailableData(pieDataSet, isAmountOfFixCostsGreaterThanNull(), isAmountOfInputsGreaterThanNull(), isAmountOfOutputsGreaterThanNull(), isAmountOfTransfersGreaterThanNull());
        pieDataSet.setSliceSpace(3f);
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
        return getAmountOfFixedCosts() != 0;
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

    private void displayTextsOnTextFields(){
        Currency activeCurrency = Currency.getActiveCurrency(getContext());

        long totalOutputsAmount = Math.abs(getAmountOfTotalOutputsOfMonths());
        String formattedTotalOutputsAmount = activeCurrency.formatAmountToReadableStringWithoutCentsWithCurrencySymbol(totalOutputsAmount);
        txvTotalOutputsAmount.setText(formattedTotalOutputsAmount);

        String formattedCash = activeCurrency.formatAmountToReadableStringWithoutCentsWithCurrencySymbol(getAmountOfCash());
        txvCashAmount.setText(formattedCash);

        String formattedDailyLimit = activeCurrency.formatAmountToReadableStringWithoutCentsWithCurrencySymbol(getAmountOfDailyLimitOfMonth());
        txvDailyLimitAmount.setText(formattedDailyLimit);

        String formattedCreditRate = activeCurrency.formatAmountToReadableStringWithoutCentsWithCurrencySymbol(getAmountOfCreditRateOfMonth());
        txvCreditRateAmount.setText(formattedCreditRate);

    }

    private long getAmountOfTotalOutputsOfMonths(){
        return getAmountOfFixedCosts() - getAmountOfOutputsOfMonth();
    }

    private long getAmountToSaveEveryMonth(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        return sharedPreferences.getLong("preference_amount_to_save", 100);
    }

    private long getAmountOfCash(){
        long amountOfTotalInputsOfMonth = getAmountOfTotalInputsOfMonthIncludingAutoPays();
        long amountOfFixedCostsOfMonth = getAmountOfFixedCosts();
        long amountOfOutputsOfMonth = Math.abs(getAmountOfOutputsOfMonth());

        return Database.Toolkit.getTotalBalanceOfAllBankAccounts() + amountOfTotalInputsOfMonth - amountOfFixedCostsOfMonth - amountOfOutputsOfMonth;
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

        long creditRate = amountOfCashOfMonth - amountToSaveEveryMonth;

        if (creditRate < 0){
            return 0;
        } else {
            return creditRate;
        }
    }

    private long getAmountOfDailyLimitOfMonth(){
        long totalInputsOfMonthIncludingAutoPays = getAmountOfTotalInputsOfMonthIncludingAutoPays();
        long fixedCostsOfMonth = Math.abs(getAmountOfFixedCosts());
        long outputsOfMonth = Math.abs(getAmountOfOutputsOfMonth());
        long amountToSaveEveryMonth = Math.abs(getAmountToSaveEveryMonth());

        long dailyLimit = (Database.Toolkit.getTotalBalanceOfAllBankAccounts() + totalInputsOfMonthIncludingAutoPays - fixedCostsOfMonth - outputsOfMonth - amountToSaveEveryMonth) / getRemainingDaysUntilMonthEnds();
        if (dailyLimit < 0){
            return 0;
        } else {
            return dailyLimit;
        }
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
}
