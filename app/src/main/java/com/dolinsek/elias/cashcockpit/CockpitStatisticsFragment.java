package com.dolinsek.elias.cashcockpit;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dolinsek.elias.cashcockpit.components.AutoPay;
import com.dolinsek.elias.cashcockpit.components.Bill;
import com.dolinsek.elias.cashcockpit.components.Currency;
import com.dolinsek.elias.cashcockpit.components.Toolkit;

import java.util.ArrayList;
import java.util.Calendar;


/**
 * A simple {@link Fragment} subclass.
 */
public class CockpitStatisticsFragment extends Fragment {

    private TextView txvCash, txvTotalOutputs, txvInstallment, txvDailyLimit;
    private ProgressBar pgbCash, pgbTotalOutputs, pgbInstallment, pgbDailyLimit;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflatedView = inflater.inflate(R.layout.fragment_cockpit_statistics, container, false);

        txvCash = inflatedView.findViewById(R.id.txv_cockpit_statistics_cash_amount);
        txvTotalOutputs = inflatedView.findViewById(R.id.txv_cockpit_statistics_total_outputs_amount);
        txvInstallment = inflatedView.findViewById(R.id.txv_cockpit_statistics_installment_amount);
        txvDailyLimit = inflatedView.findViewById(R.id.txv_cockpit_statistics_daily_limit_amount);

        pgbCash = inflatedView.findViewById(R.id.pgb_cockpit_statistics_cash);
        pgbTotalOutputs = inflatedView.findViewById(R.id.pgb_cockpit_statistics_total_outputs);
        pgbInstallment = inflatedView.findViewById(R.id.pgb_cockpit_statistics_installment);
        pgbDailyLimit = inflatedView.findViewById(R.id.pgb_cockpit_statistics_daily_limit);

        displayAmounts();
        displayProgresses();

        return inflatedView;
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
        return (int) (value * 100 / getTotalOfAllDisplayAmounts());
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
}
