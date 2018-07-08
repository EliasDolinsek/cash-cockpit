package com.dolinsek.elias.cashcockpit;


import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class NotEnoughDataFragment extends Fragment {

    private TextView mTxvTextToDisplay;
    private String textToDisplay;
    public NotEnoughDataFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View inflatedView = inflater.inflate(R.layout.fragment_not_enough_data, container, false);

        mTxvTextToDisplay = inflatedView.findViewById(R.id.txv_not_enough_data_text_to_display);
        mTxvTextToDisplay.setText(textToDisplay);

        return inflatedView;
    }

    @Override
    public void onInflate(Context context, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(context, attrs, savedInstanceState);

        TypedArray typedArray = getActivity().obtainStyledAttributes(attrs, R.styleable.NotEnoughDataFragment);
        textToDisplay = typedArray.getString(R.styleable.NotEnoughDataFragment_text);
    }

    public void hide(){
        getChildFragmentManager().beginTransaction().hide(this).commit();
    }

    public void show(){
        getChildFragmentManager().beginTransaction().show(this).commit();
    }

}
