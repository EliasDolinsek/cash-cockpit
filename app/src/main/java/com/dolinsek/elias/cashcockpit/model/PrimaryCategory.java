package com.dolinsek.elias.cashcockpit.model;

import android.graphics.drawable.Icon;

import java.util.ArrayList;

/**
 * Created by elias on 06.01.2018.
 */

public class PrimaryCategory extends Category{

    private static final String DEFAULT_ICON_NAME = "ic_default_category_image";

    private ArrayList<Subcategory> subcategories;
    private String iconName;

    public PrimaryCategory(String name, Goal goal) {
        super(name, goal);
        subcategories = new ArrayList<>();
        iconName = DEFAULT_ICON_NAME;
    }

    public PrimaryCategory(String name) {
        super(name);
        subcategories = new ArrayList<>();
    }

    @Override
    public void deleteCategory() {
        for(Subcategory subcategory:subcategories){
            subcategory.deleteCategory();
        }
    }

    public void addSubcategory(Subcategory subcategory){
        subcategories.add(subcategory);
    }

    public void addSubcategories(ArrayList<Subcategory> subcategories){
        for(Subcategory subcategory:subcategories){
            this.subcategories.add(subcategory);
        }
    }

    public ArrayList<Subcategory> getSubcategories() {
        return subcategories;
    }

    public void setSubcategories(ArrayList<Subcategory> subcategories) {
        this.subcategories = subcategories;
    }

    public void setIconName(String iconName) {
        if(iconName != null || iconName.equals(""))
            this.iconName = iconName;
    }

    public String getIconName() {
        return iconName;
    }
}
