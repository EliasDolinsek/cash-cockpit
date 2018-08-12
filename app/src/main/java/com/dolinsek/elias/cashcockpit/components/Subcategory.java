package com.dolinsek.elias.cashcockpit.components;

import com.google.firebase.database.Exclude;

/**
 * This represents a subcategory
 * Created by elias on 06.01.2018.
 */

public class Subcategory extends Category{

    /**
     * Determines if the subcategory is favored
     */
    private boolean favoured;

    /**
     * Primary category what this belong to
     */
    private String primaryCategoryName;

    /**
     * Creates a new subcategory
     * @param name name of the subcategory
     * @param goal goal of the subcategory
     * @param primaryCategoryName primary category what this belongs to
     * @param favoured if the subcategory is favored or not
     */
    public Subcategory(String name, Goal goal, String primaryCategoryName, boolean favoured) {
        super(name, goal);
        this.favoured = favoured;
        this.primaryCategoryName = primaryCategoryName;
    }

    /**
     * Creates a new subcategory
     * @param name name of the subcategory
     * @param primaryCategoryName primary category what this belongs to
     * @param favoured if the subcategory is favored or not
     */
    public Subcategory(String name, boolean favoured, String primaryCategoryName) {
        super(name);
        this.favoured = favoured;
        this.primaryCategoryName = primaryCategoryName;
    }

    /**
     * Removes this subcategory from the primary category
     */
    @Override
    public void deleteCategory() {
        getPrimaryCategoryByName().getSubcategories().remove(this);
    }

    public boolean isFavoured() {
        return favoured;
    }

    public void setFavoured(boolean favoured) {
        this.favoured = favoured;
    }

    @Exclude
    public PrimaryCategory getPrimaryCategory() {
        return getPrimaryCategoryByName();
    }

    public void setPrimaryCategory(PrimaryCategory primaryCategory) {
        this.primaryCategoryName = primaryCategory.getName();
    }

    @Exclude
    private PrimaryCategory getPrimaryCategoryByName(){
        for (PrimaryCategory primaryCategory:Database.getPrimaryCategories()){
            if (primaryCategory.getName().equals(primaryCategoryName)){
                return primaryCategory;
            }
        }

        throw new IllegalArgumentException("Couldn't find primary category by name!");
    }

}
