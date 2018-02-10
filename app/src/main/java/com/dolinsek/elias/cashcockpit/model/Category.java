package com.dolinsek.elias.cashcockpit.model;

/**
 * Created by elias on 06.01.2018.
 */

public abstract class Category {

    private String name;
    private Goal goal;

    public Category(String name, Goal goal) {
        this.name = name;
        if(goal == null)
            this.goal = new Goal(0);
        else
            this.goal = goal;
    }

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
