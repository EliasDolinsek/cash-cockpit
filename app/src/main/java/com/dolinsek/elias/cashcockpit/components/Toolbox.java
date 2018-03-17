package com.dolinsek.elias.cashcockpit.components;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by elias on 16.03.2018.
 */

public class Toolbox {

    public static final int TYPE_YEAR = 876;
    public static final int TYPE_MONTH = 298;
    public static final int TYPE_DAY = 323;

    public static ArrayList<Bill> getBills(long creationTime, int type){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(creationTime);

        int year = 0, month = 0, day = 0;
        switch (type){
            case TYPE_DAY: year = calendar.get(Calendar.YEAR); month = calendar.get(Calendar.MONTH); day = calendar.get(Calendar.DAY_OF_MONTH);
                break;
            case TYPE_MONTH: year = calendar.get(Calendar.YEAR); month = calendar.get(Calendar.MONTH);
                break;
            case TYPE_YEAR: year = calendar.get(Calendar.YEAR);
                break;
        }

        ArrayList<Bill> bills = new ArrayList<>();
        for (BankAccount bankAccount:Database.getBankAccounts()){
            for (Bill bill:bankAccount.getBills()){
                calendar.setTimeInMillis(bill.getCreationDate());

                switch (type){
                    case TYPE_DAY: {
                        if (year == calendar.get(Calendar.YEAR) && month == calendar.get(Calendar.MONTH) && day == calendar.get(Calendar.DAY_OF_MONTH)){
                            bills.add(bill);
                        }
                    }
                        break;
                    case TYPE_MONTH:{
                        if (year == calendar.get(Calendar.YEAR) && month == calendar.get(Calendar.MONTH)){
                            bills.add(bill);
                        }
                    }
                        break;
                    case TYPE_YEAR:{
                        if (year == calendar.get(Calendar.YEAR)){
                            bills.add(bill);
                        }
                    }
                        break;
                }
            }
        }

        return bills;
    }

    public static ArrayList<Goal> getSubcategoriesGoals(){
        ArrayList<Goal> goals = new ArrayList<>();
        for (PrimaryCategory primaryCategory:Database.getPrimaryCategories()){
            if (primaryCategory.getGoal().getAmount() != 0){
                for (Subcategory subcategory:primaryCategory.getSubcategories()){
                    if (subcategory.getGoal().getAmount() != 0){
                        goals.add(subcategory.getGoal());
                    }
                }
            }
        }

        return goals;
    }
}
