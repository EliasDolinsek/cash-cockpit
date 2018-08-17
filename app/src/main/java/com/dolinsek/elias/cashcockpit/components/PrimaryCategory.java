package com.dolinsek.elias.cashcockpit.components;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Represents a primary category
 * Created by elias on 06.01.2018.
 */

public class PrimaryCategory extends Category{

    /**
     * Name of the default icon
     */
    private static final String DEFAULT_ICON_NAME = "ic_default_category_image";

    /**
     * List of all subcategories what belong to this primary category
     */
    private ArrayList<Subcategory> subcategories;

    /**
     * Name of the icon for this primary category
     */
    private String iconName;

    public PrimaryCategory(){

    }

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
