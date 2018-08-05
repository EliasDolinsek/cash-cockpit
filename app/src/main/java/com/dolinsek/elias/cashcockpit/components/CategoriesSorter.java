package com.dolinsek.elias.cashcockpit.components;

import android.content.Context;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Class which provides static methods for sorting categories
 * Created by elias on 13.02.2018.
 */

public class CategoriesSorter {

    public static void sortPrimaryCategoriesIfPreferenceIsChecked(Context context, ArrayList<PrimaryCategory> primaryCategories){
        boolean sortCategories = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("preference_sort_categories_by_name", true);

        if (sortCategories){
            sortPrimaryCategories(primaryCategories);
        }
    }

    public static void sortPrimaryCategories(ArrayList<PrimaryCategory> primaryCategories){
        Collections.sort(primaryCategories, new Comparator<PrimaryCategory>() {
            @Override
            public int compare(PrimaryCategory primaryCategory, PrimaryCategory t1) {
                return primaryCategory.getName().compareTo(t1.getName());
            }
        });

        for(int i = 0; i<primaryCategories.size(); i++){
            for(int y = 0; y<primaryCategories.get(i).getSubcategories().size(); y++){
                if(primaryCategories.get(i).getSubcategories().get(y).isFavoured()){
                    primaryCategories.get(i).getSubcategories().get(y).setName(" " + primaryCategories.get(i).getSubcategories().get(y).getName());
                }
            }

            sortSubcategories(primaryCategories.get(i).getSubcategories());

            for(int y = 0; y<primaryCategories.get(i).getSubcategories().size(); y++){
                if(primaryCategories.get(i).getSubcategories().get(y).getName().startsWith(" "))
                    primaryCategories.get(i).getSubcategories().get(y).setName(primaryCategories.get(i).getSubcategories().get(y).getName().substring(1, primaryCategories.get(i).getSubcategories().get(y).getName().length()));
            }
        }
    }

    public static void sortSubcategories(ArrayList<Subcategory> subcategories){
        Collections.sort(subcategories, new Comparator<Subcategory>() {
            @Override
            public int compare(Subcategory subcategory, Subcategory t1) {
                return subcategory.getName().compareTo(t1.getName());
            }
        });
    }
}
