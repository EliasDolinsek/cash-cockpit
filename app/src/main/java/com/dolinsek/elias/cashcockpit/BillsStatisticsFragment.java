package com.dolinsek.elias.cashcockpit;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dolinsek.elias.cashcockpit.components.Bill;
import com.dolinsek.elias.cashcockpit.components.Database;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class BillsStatisticsFragment extends Fragment {

    private SelectMonthFragment selectMonthFragment;
    private LinearLayout llSelectMonthFragment;
    private ProgressBar pgbInput, pgbOutput, pgbTransfer;
    private TextView txvInputPercent, txvOutputPersont, txvTransferPercent;
    private long[] timeStampsWithBills;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflatedView = inflater.inflate(R.layout.fragment_bills_statistics, container, false);;
        loadTimeStampsWithBills();

        llSelectMonthFragment = inflatedView.findViewById(R.id.ll_bills_statistics_select_month_fragment_container);

        pgbInput = inflatedView.findViewById(R.id.pgb_bills_statistics_type_input);
        pgbOutput = inflatedView.findViewById(R.id.pgb_bills_statistics_type_output);
        pgbTransfer = inflatedView.findViewById(R.id.pgb_bills_statistics_type_transfer);

        txvInputPercent = inflatedView.findViewById(R.id.txv_bills_statistics_type_input_percent);
        txvOutputPersont = inflatedView.findViewById(R.id.txv_bills_statistics_type_output_percent);
        txvTransferPercent = inflatedView.findViewById(R.id.txv_bills_statistics_type_transfer_percent);

        selectMonthFragment = new SelectMonthFragment();
        displaySelectMonthFragment();
        setupSelectMonthFragment();

        return inflatedView;
    }

    @Override
    public void onPause() {
        super.onPause();

        //Removes Fragment because otherwise it would be added twice
        getFragmentManager().beginTransaction().remove(selectMonthFragment).commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        setupSelectMonthFragment();
    }

    private void loadBillTypeUsageStatistic(long timeStampToLoadStatistics){
        ArrayList<Bill> billsOfMonth = Database.Toolkit.getBillsOfMonth(timeStampToLoadStatistics);

        ArrayList<Bill> inputTypeBills = Database.Toolkit.filterBillsOfBillType(billsOfMonth, Bill.TYPE_INPUT);
        ArrayList<Bill> outputTypeBills = Database.Toolkit.filterBillsOfBillType(billsOfMonth, Bill.TYPE_OUTPUT);
        ArrayList<Bill> transferTypeBills = Database.Toolkit.filterBillsOfBillType(billsOfMonth, Bill.TYPE_TRANSFER);

        int allBillsSize = inputTypeBills.size() + outputTypeBills.size() + transferTypeBills.size();
        int usageOfInputTypesInPercent = getPercentOfBillUsage(allBillsSize, inputTypeBills.size());
        int usageOfOutputTypesInPercent = getPercentOfBillUsage(allBillsSize, outputTypeBills.size());
        int usageOfTransferTypesInPercent = getPercentOfBillUsage(allBillsSize, transferTypeBills.size());

        displayUsageOfBillsType(usageOfInputTypesInPercent, pgbInput, txvInputPercent);
        displayUsageOfBillsType(usageOfOutputTypesInPercent, pgbOutput, txvOutputPersont);
        displayUsageOfBillsType(usageOfTransferTypesInPercent, pgbTransfer, txvTransferPercent);
    }

    private void displayUsageOfBillsType(int usageInPercent, ProgressBar pgbToDisplayPercent, TextView txvToDisplayPercent){
        pgbToDisplayPercent.setProgress(usageInPercent);

        String textToDisplay = usageInPercent + "%";
        txvToDisplayPercent.setText(textToDisplay);
    }

    private int getPercentOfBillUsage(int allBillsSize, int billTypeSize){
        return 100 / allBillsSize * billTypeSize;
    }

    private void loadTimeStampsWithBills(){
        ArrayList<Bill> allBillsInDatabase = Database.Toolkit.getAllBillsInDatabase();
        timeStampsWithBills = arrayListToLongArray(getTimeStampsWithBills(allBillsInDatabase));
    }

    private void displaySelectMonthFragment(){
        if (selectMonthFragment.isAdded()){
            getFragmentManager().beginTransaction().remove(selectMonthFragment).commit();
        }

        getFragmentManager().beginTransaction().add(R.id.ll_bills_statistics_select_month_fragment_container, selectMonthFragment).commit();
    }

    private void setupSelectMonthFragment(){
        selectMonthFragment.setTimeStampsOfDates(timeStampsWithBills);
        selectMonthFragment.setSelectLastItemAfterCreate(true);
        selectMonthFragment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadBillTypeUsageStatistic(timeStampsWithBills[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private ArrayList<Long> getTimeStampsWithBills(ArrayList<Bill> bills){
        ArrayList<Long> monthsWithBills = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        long firstCreationDateOfBill = Database.Toolkit.getCreationDateOfFirstBill(bills);
        calendar.setTimeInMillis(firstCreationDateOfBill);

        while (!doesMonthExceedsCurrentTime(calendar)){
            long currentMonthTimesStamp = calendar.getTimeInMillis();
            ArrayList<Bill> billsOfMonth = Database.Toolkit.getBillsOfMonth(currentMonthTimesStamp);

            if (billsOfMonth.size() != 0){
                monthsWithBills.add(currentMonthTimesStamp);
            }

            calendar.add(Calendar.MONTH, 1);
        }

        return monthsWithBills;
    }

    private boolean doesMonthExceedsCurrentTime(Calendar calendar){
        Calendar currentMonthCalendar = Calendar.getInstance();
        currentMonthCalendar.setTimeInMillis(System.currentTimeMillis());

        int currentYear = currentMonthCalendar.get(Calendar.YEAR);
        int currentMonth = currentMonthCalendar.get(Calendar.MONTH);

        return currentYear < calendar.get(Calendar.YEAR) || currentMonth < calendar.get(Calendar.MONTH);
    }

    private long[] arrayListToLongArray(ArrayList<Long> arrayList){
        long[] longsToReturn = new long[arrayList.size()];

        for (int i = 0; i<longsToReturn.length; i++){
            longsToReturn[i] = arrayList.get(i);
        }

        return longsToReturn;
    }

}
