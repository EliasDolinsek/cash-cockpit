package com.dolinsek.elias.cashcockpit;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;

import com.dolinsek.elias.cashcockpit.components.Goal;
import com.dolinsek.elias.cashcockpit.components.PrimaryCategory;
import com.dolinsek.elias.cashcockpit.components.Subcategory;

/**
 * DialogFragment for creating, editing and deleting subcategory
 * Created by elias on 24.01.2018.
 */

public class SubcategoryEditorDialogFragment extends DialogFragment{

    private PrimaryCategory mPrimaryCategory;
    private Subcategory mSubcategory;
    private boolean mEditMode = false;

    private TextInputLayout mTilSubcategoryName, mTilGoalAmount;
    private EditText mEdtSubcategoryName, mEdtGoalAmount;
    private CheckBox mChbGoalEnabled;
    private ImageView mImvFavored;

    //Interface what gets called when the dialog dismisses
    private DialogInterface.OnDismissListener mOnDismissListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if(mSubcategory == null)
            throw new IllegalStateException("No subcategory set (setSubcategory)");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View inflatedView = inflater.inflate(R.layout.dialog_subcategory_editor, null);

        mTilSubcategoryName = (TextInputLayout) inflatedView.findViewById(R.id.til_item_subcategory_editor_name);
        mTilGoalAmount = (TextInputLayout) inflatedView.findViewById(R.id.til_item_subcategory_editor_goal_amount);

        mEdtSubcategoryName = (EditText) inflatedView.findViewById(R.id.edt_item_subcategory_editor_name);
        mEdtGoalAmount = (EditText) inflatedView.findViewById(R.id.edt_item_subcategory_editor_goal_amount);

        mChbGoalEnabled = (CheckBox) inflatedView.findViewById(R.id.chb_item_subcategory_editor_goal_enabled);
        mImvFavored = (ImageView) inflatedView.findViewById(R.id.imv_item_subcategory_editor_favored);
        setupImage(mSubcategory.isFavoured());

        //Enable/Disable goal
        mChbGoalEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                mEdtGoalAmount.setEnabled(checked);
            }
        });

        //Changes favored state on click
        mImvFavored.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSubcategory.setFavoured(!mSubcategory.isFavoured());
                setupImage(mSubcategory.isFavoured());
            }
        });

        //If edit mode is active it displays details
        if(mEditMode){
            mEdtSubcategoryName.setText(mSubcategory.getName());
            if(mSubcategory.getGoal().getAmount() != 0){
                mEdtGoalAmount.setText(String.valueOf(mSubcategory.getGoal().getAmount() / 100));
                mChbGoalEnabled.setChecked(true);
            }


        }

        //Sets View
        builder.setView(inflatedView);

        //Setups buttons depending if the edit mode is active
        if(mEditMode){
            builder.setPositiveButton(getResources().getString(R.string.dialog_action_save), null);
            builder.setNegativeButton(getResources().getString(R.string.dialog_action_delete), null);
            builder.setTitle(getResources().getString(R.string.dialog_title_edit_subcategory));
        } else {
            builder.setPositiveButton(getResources().getString(R.string.dialog_action_create), null);
            builder.setTitle(getResources().getString(R.string.dialog_title_create_subcategory));
        }

        //Sets up button clicks
        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button mBtnPositive = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                mBtnPositive.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Remove error
                        mEdtSubcategoryName.setError(null);

                        //Checks if name already exists
                        boolean nameAlreadyExits = false;
                        if(!mEditMode){
                            for(int i = 0; i<mPrimaryCategory.getSubcategories().size(); i++){
                                if(mPrimaryCategory.getSubcategories().get(i).getName().equals(mEdtSubcategoryName.getText().toString()))
                                    nameAlreadyExits = true;
                            }
                        }

                        if(mEdtSubcategoryName.getText().toString().trim().equals("")){
                            mTilSubcategoryName.setError(getResources().getString(R.string.label_enter_category_name));
                        } else if(nameAlreadyExits) {
                            mTilSubcategoryName.setError(getResources().getString(R.string.label_category_name_already_exists));
                        } else if(mChbGoalEnabled.isChecked() && mEdtGoalAmount.getText().toString().equals("")){
                            mTilGoalAmount.setError(getResources().getString(R.string.label_enter_euros));
                        } else {
                            //Saves name
                            mSubcategory.setName(mEdtSubcategoryName.getText().toString());

                            //Saves goal-status
                            if(mChbGoalEnabled.isChecked())
                                mSubcategory.setGoal(new Goal(Long.valueOf(mEdtGoalAmount.getText().toString()) * 100));
                            else
                                mSubcategory.setGoal(new Goal(0));

                            //Saves goal-amount
                            if(mPrimaryCategory.getGoal().getAmount() < mSubcategory.getGoal().getAmount()){
                                long difference = mSubcategory.getGoal().getAmount() - mPrimaryCategory.getGoal().getAmount();

                                if(mPrimaryCategory.getGoal() != null)
                                    mPrimaryCategory.getGoal().setAmount(mPrimaryCategory.getGoal().getAmount() + difference);
                                else
                                    mPrimaryCategory.setGoal(new Goal(difference));
                            }

                            //Adds a new Subcategory if it isn't in edit mode
                            if(!mEditMode)
                                mPrimaryCategory.addSubcategory(mSubcategory);

                            dialog.dismiss();
                        }
                    }
                });

                Button mBtnNegative = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                if(mBtnNegative != null){
                    mBtnNegative.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //Deletes Subcategory
                            mPrimaryCategory.getSubcategories().remove(mSubcategory);

                            dialog.dismiss();
                        }
                    });
                }
            }
        });

        dialog.setOnDismissListener(mOnDismissListener);

        return dialog;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        //Calls interface in activity if it implements the right inteface
        final Activity activity = getActivity();
        if (activity instanceof DialogInterface.OnDismissListener) {
            ((DialogInterface.OnDismissListener) activity).onDismiss(dialog);
        }
    }

    /**
     * Sets up the icons what display if the subcategory is favored or not
     * @param favored if the subcategory is favored or not
     */
    private void setupImage(boolean favored){
        if(favored)
            mImvFavored.setImageResource(R.drawable.ic_favorite);
        else
            mImvFavored.setImageResource(R.drawable.ic_not_favorite);
    }

    /**
     * Sets primary category including subcategory to edit and enables edit mode
     * @param primaryCategory primary category
     * @param subcategoryIndex index in array for subcategories in primary category
     */
    public void setPrimaryCategory(PrimaryCategory primaryCategory, int subcategoryIndex){
        mSubcategory = primaryCategory.getSubcategories().get(subcategoryIndex);
        mPrimaryCategory = primaryCategory;

        mEditMode = true;
    }

    /**
     * Sets primary category and edit mode keeps disabled
     * @param primaryCategory primary category
     */
    public void setPrimaryCategory(PrimaryCategory primaryCategory){
        mSubcategory = new Subcategory("", new Goal(0), primaryCategory, false);
        mPrimaryCategory = primaryCategory;
    }
}
