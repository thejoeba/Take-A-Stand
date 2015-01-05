package com.sean.takeastand.ui;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sean.takeastand.R;


/**
 * Created by Joey on 1/2/2015.
 */
public class ScienceActivityRecyler extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_recycler);
        this.setTitle(getResources().getStringArray(R.array.ActivityTitle)[3]);
        Toolbar toolbar = (Toolbar) findViewById(R.id.recycler_toolbar);
        setSupportActionBar(toolbar);
        if (toolbar != null) {
            toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
        RecyclerView recList = (RecyclerView) findViewById(R.id.cardList);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        recList.setAdapter(new ScienceAdapter());
    }

    public class ScienceAdapter extends RecyclerView.Adapter<ScienceAdapter.ScienceCardViewHolder> {

        @Override
        public int getItemCount() {
            return getResources().getStringArray(R.array.MedicalTitle).length;
        }

        @Override
        public void onBindViewHolder(ScienceCardViewHolder scienceCardViewHolder, final int i) {
            final Resources resources = getResources();
            scienceCardViewHolder.vTitle.setText(resources.getStringArray(R.array.MedicalTitle)[i]);
            scienceCardViewHolder.vText.setText(resources.getStringArray(R.array.MedicalText)[i]);
            scienceCardViewHolder.vLink.setText(resources.getStringArray(R.array.MedicalLink)[i]);
            scienceCardViewHolder.vLink.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(resources.getStringArray(R.array.MedicalURL)[i])));
                }
            });
        }

        @Override
        public ScienceCardViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View itemView = LayoutInflater.
                    from(viewGroup.getContext()).
                    inflate(R.layout.cardview, viewGroup, false);

            return new ScienceCardViewHolder(itemView);
        }

        public class ScienceCardViewHolder extends RecyclerView.ViewHolder {

            protected CardView vCard;
            protected TextView vTitle;
            protected TextView vText;
            protected TextView vLink;

            public ScienceCardViewHolder(View v) {
                super(v);
                vCard = (CardView) v.findViewById(R.id.cvCard);
                vTitle = (TextView) v.findViewById(R.id.txtTitle);
                vText = (TextView) v.findViewById(R.id.txtText);
                vLink = (TextView) v.findViewById(R.id.txtLink);
            }
        }
    }
}