package com.dolinsek.elias.cashcockpit.model;

/**
 * Created by elias on 06.01.2018.
 */

public class Subcategory extends Category{

    private boolean favoured;
    private PrimaryCategory primaryCategory;

    public Subcategory(String name, Goal goal, PrimaryCategory primaryCategory, boolean favoured) {
        super(name, goal);
        this.favoured = favoured;
        this.primaryCategory = primaryCategory;
    }

    public Subcategory(String name, boolean favoured, PrimaryCategory primaryCategory) {
        super(name);
        this.favoured = favoured;
        this.primaryCategory = primaryCategory;
    }

    @Override
    public void deleteCategory() {
        primaryCategory.getSubcategories().remove(this);
    }

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

    @Override
    public Object clone() {
        Subcategory subcategory = new Subcategory(this.getName(), new Goal(this.getGoal().getAmount()), this.getPrimaryCategory(), this.isFavoured());
        return subcategory;
    }
}
