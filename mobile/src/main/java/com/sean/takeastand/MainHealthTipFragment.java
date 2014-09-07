package com.sean.takeastand;

/**
 * Created by Sean on 2014-09-03.
 */
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MainHealthTipFragment
        extends Fragment
{
    private static final String TAG = "MainHealthTipFragment";
    TextView healthTip;

    private void setUpTextView()
    {
        this.healthTip.setSelected(true);
    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle)
    {
        Log.i("MainHealthTipFragment", "onCreateView");
        View localView = layoutInflater.inflate(2130903068, viewGroup, false);
        this.healthTip = ((TextView)localView.findViewById(2131034211));
        return localView;
    }

    public void onViewCreated(View view, Bundle bundle)
    {
        setUpTextView();
        super.onViewCreated(view, bundle);
    }
}
