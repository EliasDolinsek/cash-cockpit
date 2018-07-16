package com.dolinsek.elias.cashcockpit;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;


/**
 * A simple {@link Fragment} subclass.
 */
public class DescriptionInputFragment extends Fragment {

    private EditText edtDescription;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflatedView = inflater.inflate(R.layout.fragment_description_input, container, false);

        edtDescription = inflatedView.findViewById(R.id.edt_description_input_description);

        return inflatedView;
    }

    public String getEnteredDescriptionAsString(){
        return edtDescription.getText().toString();
    }

    public EditText getEdtDescription() {
        return edtDescription;
    }
}
