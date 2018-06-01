package com.dolinsek.elias.cashcockpit;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dolinsek.elias.cashcockpit.components.Bill;
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


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View inflatedView = inflater.inflate(R.layout.fragment_cockpit_chart, container, false);;

        pieChart = (PieChart) inflatedView.findViewById(R.id.pc_cockpit);

        setupPieChart();
        loadPieChart();

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

    private void getCreditRate(){
        ArrayList<Bill> allBillsOfMonth = Database.Toolkit.getBillsOfMonth(System.currentTimeMillis());
        ArrayList<Bill> autoPayBillsOfMonth = Database.Toolkit.filterBillsOfAutoPayBill(allBillsOfMonth);

        ArrayList<Bill> filteredBillsOfMonth = allBillsOfMonth;
        filteredBillsOfMonth.removeAll(autoPayBillsOfMonth);

        long totalAutoPayBillsAmount = Database.Toolkit.getTotalAmountOfBills(autoPayBillsOfMonth);
        long totalAmountOfBill = Database.Toolkit.getTotalAmountOfBills(filteredBillsOfMonth);
    }
}
