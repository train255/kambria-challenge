package com.covid19.health;

import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
//import android.support.v4.app.Fragment;
//import android.support.v4.app.FragmentTransaction;
//import androidx.cardview.widget.CardView;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class InfoFragment extends Fragment {

    private CardView healthcareCard;
    private CardView vaccineCard;
    private CardView coronaCard;
    private CardView rapidTestCard;
    private CardView treatmentCard;
    private CardView longTermEffectsCard;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_info, container, false);

        coronaCard = (CardView) view.findViewById(R.id.corona_virus);
        coronaCard.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((MainActivity) getActivity()).changeFragment(new HomeCoronaFragment());
                    }
                });

        healthcareCard = (CardView) view.findViewById(R.id.health_care);
        healthcareCard.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((MainActivity) getActivity()).changeFragment(new HomeHealthCareFragment());
//                        ((MainActivity) getActivity()).removeHomeSelected();
                    }
                });

        vaccineCard = (CardView) view.findViewById(R.id.vaccine);
        vaccineCard.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((MainActivity) getActivity()).changeFragment(new HomeVaccineFragment());
                    }
                });

        rapidTestCard = (CardView) view.findViewById(R.id.rapid_test);
        rapidTestCard.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((MainActivity) getActivity()).changeFragment(new HomeRapidTestFragment());
                    }
                });

        treatmentCard = (CardView) view.findViewById(R.id.treatment);
        treatmentCard.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((MainActivity) getActivity()).changeFragment(new HomeTreatmentFragment());
                    }
                });

        longTermEffectsCard = (CardView) view.findViewById(R.id.long_term_effects);
        longTermEffectsCard.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((MainActivity) getActivity()).changeFragment(new HomeLongTermEffectsFragment());
                    }
                });

        return view;
    }
}