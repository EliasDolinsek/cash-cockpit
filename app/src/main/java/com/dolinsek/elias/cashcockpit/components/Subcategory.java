package com.dolinsek.elias.cashcockpit.components;

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
    private PrimaryCategory primaryCategory;

    @Override
    public String toString() {
        return "Subcategory{" +
                "favoured=" + favoured +
                ", primaryCategory=" + primaryCategory +
                '}';
    }

    /**
     * Creates a new subcategory
     * @param name name of the subcategory
     * @param goal goal of the subcategory
     * @param primaryCategory primary category what this belongs to
     * @param favoured if the subcategory is favored or not
     */
    public Subcategory(String name, Goal goal, PrimaryCategory primaryCategory, boolean favoured) {
        super(name, goal);
        this.favoured = favoured;
        this.primaryCategory = primaryCategory;
    }

    /**
     * Creates a new subcategory
     * @param name name of the subcategory
     * @param primaryCategory primary category what this belongs to
     * @param favoured if the subcategory is favored or not
     */
    public Subcategory(String name, boolean favoured, PrimaryCategory primaryCategory) {
        super(name);
        this.favoured = favoured;
        this.primaryCategory = primaryCategory;
    }

    /**
     * Removes this subcategory from the primary category
     */
    @Override
    public void deleteCategory() {
        primaryCategory.getSubcategories().remove(this);
    }

    /**
     * Changes the primary category what this belongs to
     * @param newPrimaryCategory new primary category
     */
    public void changePrimaryCategory(PrimaryCategory newPrimaryCategory){
        this.primaryCategory.getSubcategories().remove(this);
        this.primaryCategory = newPrimaryCategory;
    }

    public boolean isFavoured() {
        return favoured;
    }

    public void setFavoured(boolean favoured) {
        this.favoured = favoured;
    }

    public PrimaryCategory getPrimaryCategory() {
        return primaryCategory;
    }

    public void setPrimaryCategory(PrimaryCategory primaryCategory) {
        this.primaryCategory = primaryCategory;
    }
}
