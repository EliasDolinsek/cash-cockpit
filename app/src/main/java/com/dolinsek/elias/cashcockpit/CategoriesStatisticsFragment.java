package com.dolinsek.elias.cashcockpit;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.dolinsek.elias.cashcockpit.components.BankAccount;
import com.dolinsek.elias.cashcockpit.components.Bill;
import com.dolinsek.elias.cashcockpit.components.Database;
import com.dolinsek.elias.cashcockpit.components.PrimaryCategory;
import com.dolinsek.elias.cashcockpit.components.Toolkit;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;


/**
 * A simple {@link Fragment} subclass.
 */
public class CategoriesStatisticsFragment extends Fragment {

    private static final String EXTRA_TIME_STAMP_OF_MONTH = "timeStampOfMonth";

    private static final int STEP_ONE_MONTH_FORWARD = 1;

    private RecyclerView rvCategories;
    private PieChart pcStatistics;
    private LinearLayout llFilterFragmentsContainer;
    private SelectMonthFragment mSelectMonthFragment;
    private Spinner spnBillTypeFilter;
    private NestedScrollView scrollView;
    private NotEnoughDataFragment fgmNotEnoughData;

    private PrimaryCategoryItemAdapter primaryCategoryItemAdapter;
    private ArrayList<Bill> billsToUse;
    private long timestampOfCurrentDisplayedMonth;
    private long[] timeStampsWithBills;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflatedView = inflater.inflate(R.layout.fragment_categories_statistics, container, false);

        loadTimeStampOfMonth(savedInstanceState);

        rvCategories = (RecyclerView) inflatedView.findViewById(R.id.rv_categories_statistics);
        pcStatistics = (PieChart) inflatedView.findViewById(R.id.pc_categories_statistics);
        llFilterFragmentsContainer = (LinearLayout) inflatedView.findViewById(R.id.ll_categories_statistics_filters_container);
        spnBillTypeFilter = (Spinner) inflatedView.findViewById(R.id.spn_categories_statistics_bill_type);
        fgmNotEnoughData = (NotEnoughDataFragment) getChildFragmentManager().findFragmentById(R.id.fgm_categories_statistics_not_enough_data);

        scrollView = inflatedView.findViewById(R.id.sv_categories_statistics);
        scrollView.scrollTo(0,0);

        timeStampsWithBills = arrayListToLongArray(getTimeStampsWithBills());
        billsToUse = getAllBillsInDatabase();

        setupBillTypeFilter();
        setupChartStatistics();
        manageViews();

        rvCategories.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCategories.setNestedScrollingEnabled(false);

        return inflatedView;
    }

    @Override
    public void onPause() {
        super.onPause();

        //Removes Fragment because otherwise it would be added twice
        getFragmentManager().beginTransaction().remove(mSelectMonthFragment).commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        setupSelectMonthFragment();
    }

    private void setupSelectMonthFragment(){
        mSelectMonthFragment = new SelectMonthFragment();
        getFragmentManager().beginTransaction().add(R.id.ll_categories_statistics_select_date_fragment_container, mSelectMonthFragment).commit();

        mSelectMonthFragment.setSelectLastItemAfterCreate(true);
        mSelectMonthFragment.setTimeStampsOfDates(timeStampsWithBills);
        mSelectMonthFragment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int index, long l) {
                timestampOfCurrentDisplayedMonth = timeStampsWithBills[index];
                loadRecyclerViewAdapter();
                loadChartStatistics();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void setupBillTypeFilter(){
        ArrayAdapter<String> filterItems = new ArrayAdapter<String>(getContext(), R.layout.costum_spinner_layout, getBillsTypesAsStringIncludingAll());
        filterItems.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spnBillTypeFilter.getBackground().setColorFilter(getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_ATOP);
        spnBillTypeFilter.setAdapter(filterItems);
        spnBillTypeFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int index, long l) {
                billsToUse = getAllBillsInDatabase();

                if (index == 1){
                    billsToUse = filterBillsBillType(billsToUse, Bill.TYPE_INPUT);
                } else if (index == 2){
                    billsToUse = filterBillsBillType(billsToUse, Bill.TYPE_OUTPUT);
                } else if (index == 3){
                    billsToUse = filterBillsBillType(billsToUse, Bill.TYPE_TRANSFER);
                }

                loadChartStatistics();
                loadRecyclerViewAdapter();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private ArrayList<Bill> filterBillsBillType(ArrayList<Bill> billsToFilter, int billTypeToFilter){
        ArrayList<Bill> filteredBills = new ArrayList<>();

        for (Bill bill:billsToFilter){
            if (bill.getType() == billTypeToFilter){
                filteredBills.add(bill);
            }
        }

        return filteredBills;
    }

    private String[] getBillsTypesAsStringIncludingAll(){
        String[] billTypes = getResources().getStringArray(R.array.bill_types_array);
        String[] billTypesIncludingAll = new String[billTypes.length + 1];

        for (int i = 0; i<billTypesIncludingAll.length; i++){
            if (i == 0){
                billTypesIncludingAll[i] = getString(R.string.label_all_bill_types);
            } else {
                billTypesIncludingAll[i] = billTypes[i - 1];
            }
        }

        return billTypesIncludingAll;
    }

    private void loadRecyclerViewAdapter(){
        primaryCategoryItemAdapter = PrimaryCategoryItemAdapter.getCategoriesStatisticsPrimaryCategoryItemAdapter(Database.getPrimaryCategories(), billsToUse, timestampOfCurrentDisplayedMonth);
        rvCategories.setAdapter(primaryCategoryItemAdapter);
    }

    private void loadChartStatistics(){
        PieDataSet pieDataSet = new PieDataSet(getChartStatisticsData(), "");
        setupPieDataSet(pieDataSet);

        if (pieDataSet.getEntryCount() == 0){
            fgmNotEnoughData.show();
            pcStatistics.setVisibility(View.GONE);
        } else {
            fgmNotEnoughData.hide();
            pcStatistics.setVisibility(View.VISIBLE);
        }

        PieData pieData = new PieData();
        pieData.addDataSet(pieDataSet);

        pcStatistics.setData(pieData);
        pcStatistics.invalidate(); //Refreshes data
    }

    private void setupPieDataSet(PieDataSet pieDataSet){
        setupPieDataSetColors(pieDataSet);
        pieDataSet.setSliceSpace(3f);
        pieDataSet.setValueTextColor(getResources().getColor(android.R.color.white));
        pieDataSet.setValueTextSize(15f);
        pieDataSet.setValueFormatter(new PercentFormatter());
    }

    private void setupPieDataSetColors(PieDataSet pieDataSet){
        ArrayList<Integer> colors = new ArrayList<>();
        for (int i = 0; i<Database.getPrimaryCategories().size(); i++){
            colors.add(getRandomColor());
        }
        pieDataSet.setColors(colors);
    }

    private int getRandomColor(){
        Random random = new Random();
        final int baseColor = Color.WHITE;

        final int baseRed = Color.red(baseColor);
        final int baseGreen = Color.green(baseColor);
        final int baseBlue = Color.blue(baseColor);

        final int red = (baseRed + random.nextInt(256)) / 3;
        final int green = (baseGreen + random.nextInt(256)) / 3;
        final int blue = (baseBlue + random.nextInt(256)) / 3;

        return Color.rgb(red, green, blue);
    }

    private void setupChartStatistics(){
        pcStatistics.setDescription(null);
        pcStatistics.setUsePercentValues(true);
        pcStatistics.setDrawEntryLabels(false);
        pcStatistics.setHoleRadius(70f);
        pcStatistics.setExtraOffsets(0,0,0,-8f);
        pcStatistics.invalidate(); //Refreshes data
    }

    private ArrayList<PieEntry> getChartStatisticsData(){
        ArrayList<PieEntry> entries = new ArrayList<>();
        for (PrimaryCategory primaryCategory:Database.getPrimaryCategories()){
            ArrayList<Bill> billsOfMonth = Toolkit.getBillsByMonth(timestampOfCurrentDisplayedMonth);
            ArrayList<Bill> billsOfCategoryAndMonth = Toolkit.filterBillsByCategory(billsOfMonth, primaryCategory);

            long amountOfBills = Toolkit.getBillsTotalAmount(billsOfCategoryAndMonth);
            if (amountOfBills != 0){
                entries.add(new PieEntry(amountOfBills, primaryCategory.getName()));
            }
        }

        return entries;
    }

    private void manageViews(){
        if (getAllBillsInDatabase().size() == 0){
            fgmNotEnoughData.show();
            pcStatistics.setVisibility(View.GONE);
            llFilterFragmentsContainer.setVisibility(View.GONE);
        } else {
            fgmNotEnoughData.hide();
            pcStatistics.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(EXTRA_TIME_STAMP_OF_MONTH, timestampOfCurrentDisplayedMonth);
    }

    private void loadTimeStampOfMonth(Bundle savedInstanceState){
        if (savedInstanceState != null){
            timestampOfCurrentDisplayedMonth = savedInstanceState.getLong(EXTRA_TIME_STAMP_OF_MONTH);
        } else {
            timestampOfCurrentDisplayedMonth = System.currentTimeMillis();
        }
    }

    private ArrayList<Long> getTimeStampsWithBills(){
        ArrayList<Long> monthsWithBills = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(getTimeStampOfCreationDateOfFirstBillInDatabase());

        while (!doesMonthExceedsCurrentTime(calendar)){
            long currentMonthTimesStamp = calendar.getTimeInMillis();
            ArrayList<Bill> billsOfMonth = getBillsOfMonth(currentMonthTimesStamp);

            if (billsOfMonth.size() != 0){
                monthsWithBills.add(currentMonthTimesStamp);
            }

            calendar.add(Calendar.MONTH, STEP_ONE_MONTH_FORWARD);
        }

        return monthsWithBills;
    }

    private long[] arrayListToLongArray(ArrayList<Long> arrayList){
        long[] longsToReturn = new long[arrayList.size()];

        for (int i = 0; i<longsToReturn.length; i++){
            longsToReturn[i] = arrayList.get(i);
        }

        return longsToReturn;
    }

    private long getTimeStampOfCreationDateOfFirstBillInDatabase(){
        long firstCreationDate = System.currentTimeMillis();

        ArrayList<Bill> billsInDatabase = getAllBillsInDatabase();
        for (Bill bill:billsInDatabase){
            if (bill.getCreationDate() < firstCreationDate){
                firstCreationDate = bill.getCreationDate();
            }
        }

        return firstCreationDate;
    }

    private ArrayList<Bill> getAllBillsInDatabase(){
        ArrayList<Bill> allBillsInDatabase = new ArrayList<>();

        for (BankAccount bankAccount:Database.getBankAccounts()){
            for (Bill bill:bankAccount.getBills()){
                allBillsInDatabase.add(bill);
            }
        }

        return allBillsInDatabase;
    }

    private boolean doesMonthExceedsCurrentTime(Calendar calendar){
        Calendar currentMonthCalendar = Calendar.getInstance();
        currentMonthCalendar.setTimeInMillis(System.currentTimeMillis());

        int currentYear = currentMonthCalendar.get(Calendar.YEAR);
        int currentMonth = currentMonthCalendar.get(Calendar.MONTH);

        return currentYear < calendar.get(Calendar.YEAR) && currentMonth < calendar.get(Calendar.MONTH);
    }

    private ArrayList<Bill> getBillsOfMonth(long timeStampOfMonth){
        ArrayList<Bill> billsOfMonth = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeStampOfMonth);

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);

        for (Bill bill:getAllBillsInDatabase()){
            calendar.setTimeInMillis(bill.getCreationDate());

            int billYear = calendar.get(Calendar.YEAR);
            int billMonth = calendar.get(Calendar.MONTH);

            if (year == billYear && month == billMonth){
                billsOfMonth.add(bill);
            }
        }

        return  billsOfMonth;
    }
}
