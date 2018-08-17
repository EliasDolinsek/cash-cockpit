package com.dolinsek.elias.cashcockpit.components;

/**
 * Represents a category
 * Created by elias on 06.01.2018.
 */

public abstract class Category {

    /**
     * Name of the category what helps the user to identify it
     */
    private String name;

    /**
     * Goal for the category
     */
    private Goal goal;

    public Category(){

    }

    /**
     * Creates a new Category
     * @param name name of the category
     * @param goal goal of the category
     */
    public Category(String name, Goal goal) {
        this.name = name;
        if(goal == null)
            this.goal = new Goal(0);
        else
            this.goal = goal;
    }

    /**
     * Creates a new Category and sets a new goal
     * @param name name of the category
     */
    public Category(String name) {
        this.name = name;
        goal = new Goal(0);
    }

    public abstract void deleteCategory();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Goal getGoal() {
        return goal;
    }

    public void setGoal(Goal goal) {
        this.goal = goal;
    }
}
