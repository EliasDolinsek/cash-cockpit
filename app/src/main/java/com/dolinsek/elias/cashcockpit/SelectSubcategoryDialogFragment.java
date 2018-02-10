package com.dolinsek.elias.cashcockpit;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.dolinsek.elias.cashcockpit.model.Subcategory;

/**
 * Created by elias on 05.02.2018.
 */

public class SelectSubcategoryDialogFragment extends DialogFragment {

    private PrimaryCategoryLightItemAdapter.SubcategorySelectionListener subcategorySelectionListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getContext());

        RecyclerView recyclerView = new RecyclerView(getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new PrimaryCategoryLightItemAdapter(subcategorySelectionListener));

        alertBuilder.setView(recyclerView);
        return alertBuilder.create();
    }

    public void setOnSubcategorySelected(PrimaryCategoryLightItemAdapter.SubcategorySelectionListener subcategorySelectionListener){
        this.subcategorySelectionListener = subcategorySelectionListener;
    }
}
