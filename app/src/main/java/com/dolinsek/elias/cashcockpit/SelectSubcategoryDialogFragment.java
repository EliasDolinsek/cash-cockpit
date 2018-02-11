package com.dolinsek.elias.cashcockpit;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.dolinsek.elias.cashcockpit.components.Database;

/**
 * Created by elias on 05.02.2018.
 */

public class SelectSubcategoryDialogFragment extends DialogFragment {

    private PrimaryCategoryLightItemAdapter.SubcategorySelectionListener subcategorySelectionListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getContext());

        if(Database.getPrimaryCategories().size() == 0){
            alertBuilder.setMessage(getResources().getString(R.string.label_no_categories));
            alertBuilder.setPositiveButton(getResources().getString(R.string.dialog_action_create), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    //Start CategoryActivity
                    Intent intent = new Intent(getContext(), CategoryActivity.class);
                    startActivity(intent);
                }
            });

            alertBuilder.setNeutralButton(getResources().getString(R.string.dialog_action_restore), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    //Add default categories to categories
                    for(int y = 0; y<Database.getDefaultPrimaryCategories().size(); y++){
                        Database.getPrimaryCategories().add(Database.getDefaultPrimaryCategories().get(y));
                    }

                    try {
                        //Save data
                        Database.save(getContext());

                        //Reload data
                        Database.load(getContext());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            RecyclerView recyclerView = new RecyclerView(getContext());
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(new PrimaryCategoryLightItemAdapter(subcategorySelectionListener));

            alertBuilder.setView(recyclerView);
        }

        return alertBuilder.create();
    }

    public void setOnSubcategorySelected(PrimaryCategoryLightItemAdapter.SubcategorySelectionListener subcategorySelectionListener){
        this.subcategorySelectionListener = subcategorySelectionListener;
    }
}
