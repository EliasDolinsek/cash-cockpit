package com.dolinsek.elias.cashcockpit;


import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;


/**
 * A simple {@link Fragment} subclass.
 */
public class DescriptionInputFragment extends Fragment {

    private EditText edtDescription;
    private String hintText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflatedView = inflater.inflate(R.layout.fragment_description_input, container, false);

        edtDescription = inflatedView.findViewById(R.id.edt_description_input_description);

        if (hintText != null && !hintText.equals("")){
            edtDescription.setHint(hintText);
        }

        return inflatedView;
    }

    @Override
    public void onInflate(Context context, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(context, attrs, savedInstanceState);

        TypedArray typedArray = getActivity().obtainStyledAttributes(attrs, R.styleable.DescriptionInputFragment);
        hintText = typedArray.getString(R.styleable.DescriptionInputFragment_hintText);
    }

    public String getEnteredDescriptionAsString(){
        return edtDescription.getText().toString();
    }

    public EditText getEdtDescription() {
        return edtDescription;
    }
}
