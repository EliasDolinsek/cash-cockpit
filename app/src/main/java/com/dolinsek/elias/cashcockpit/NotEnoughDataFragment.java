package com.dolinsek.elias.cashcockpit;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 */
public class NotEnoughDataFragment extends Fragment {


    public NotEnoughDataFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_not_enough_data, container, false);
    }

    public void hide(){
        getChildFragmentManager().beginTransaction().hide(this).commit();
    }

    public void show(){
        getChildFragmentManager().beginTransaction().show(this).commit();
    }

}
