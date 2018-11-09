package com.dolinsek.elias.cashcockpit;


import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.dolinsek.elias.cashcockpit.components.Database;
import com.dolinsek.elias.cashcockpit.components.PrimaryCategory;
import com.dolinsek.elias.cashcockpit.components.Subcategory;
import com.dolinsek.elias.cashcockpit.components.Toolkit;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class SelectCategoryFragment extends Fragment {

    private static final int RQ_SELECT_CATEGORY = 35;

    private OnCategorySelectedListener onCategorySelectedListener;
    private TextView txvSelectedSubcategory;
    private ImageView imvSelectedPrimaryCategoryIcon;
    private Subcategory selectedSubcategory;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflatedView = inflater.inflate(R.layout.fragment_select_category, container, false);

        txvSelectedSubcategory = inflatedView.findViewById(R.id.txv_select_category_subcategory_name);
        imvSelectedPrimaryCategoryIcon = inflatedView.findViewById(R.id.imv_select_category_category_icon);
        txvSelectedSubcategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), SelectCategoryActivity.class);
                if (selectedSubcategory != null){
                    try {
                        intent.putExtra(SelectCategoryActivity.SELECTED_PRIMARY_CATEGORY_INDEX, Toolkit.getIndexOfPrimaryCategoryInDatabase(selectedSubcategory.getPrimaryCategory()));
                        intent.putExtra(SelectCategoryActivity.SELECTED_SUBCATEGORY_INDEX, Toolkit.getIndexOfSubcategoryInPrimaryCategory(selectedSubcategory));
                    } catch (Exception e){
                        selectedSubcategory = null;
                    }
                }

                startActivityForResult(intent, RQ_SELECT_CATEGORY);
            }
        });

        return inflatedView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RQ_SELECT_CATEGORY && resultCode == RESULT_OK){
            selectedSubcategory = Database.getPrimaryCategories().get(data.getIntExtra(SelectCategoryActivity.EXTRA_PRIMARY_CATEGORY_INDEX, 0)).getSubcategories().get(data.getIntExtra(SelectCategoryActivity.EXTRA_SUBCATEGORY_INDEX, 0));
            onCategorySelectedListener.onSubcategorySelected(selectedSubcategory);
            displaySelectedCategoryDetails();
        }
    }

    public void setOnCategorySelectedListener(OnCategorySelectedListener onCategorySelectedListener){
        this.onCategorySelectedListener = onCategorySelectedListener;
    }


    public void setSelectedSubcategory(Subcategory selectedSubcategory){
        this.selectedSubcategory = selectedSubcategory;
        displaySelectedCategoryDetails();
    }

    private void displaySelectedCategoryDetails(){
        txvSelectedSubcategory.setText(selectedSubcategory.getName());
        displayPrimaryCategoryIcon();
    }

    private void displayPrimaryCategoryIcon(){
        imvSelectedPrimaryCategoryIcon.setImageDrawable(null);

        try{
            String primaryCategoryIconName = selectedSubcategory.getPrimaryCategory().getIconName();
            String packageName = getActivity().getPackageName();

            int resource = getResources().getIdentifier(primaryCategoryIconName, "drawable", packageName);
            imvSelectedPrimaryCategoryIcon.setBackgroundResource(resource);
        } catch (Exception e) {
            imvSelectedPrimaryCategoryIcon.setBackgroundResource(R.drawable.ic_select_category);
        }
    }

    public OnCategorySelectedListener getOnCategorySelectedListener() {
        return onCategorySelectedListener;
    }

    public TextView getTxvSelectedSubcategory() {
        return txvSelectedSubcategory;
    }

    public ImageView getImvSelectedPrimaryCategoryIcon() {
        return imvSelectedPrimaryCategoryIcon;
    }

    public Subcategory getSelectedSubcategory() {
        return selectedSubcategory;
    }

    public static interface OnCategorySelectedListener {
         void onSubcategorySelected(Subcategory selectedSubcategory);
    }

}
