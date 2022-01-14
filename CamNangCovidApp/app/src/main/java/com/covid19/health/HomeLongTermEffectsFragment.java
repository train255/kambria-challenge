package com.covid19.health;

import android.os.Bundle;
//import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class HomeLongTermEffectsFragment extends Fragment {
    private TextView source1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home_long_term_effects, container, false);

        source1 = (TextView) view.findViewById(R.id.treatment_source_1);
        source1.setText(Html.fromHtml("<a href=\"http://benhvien115.com.vn/tin-tuc-va-hoat-dong/song-chung-cung-hoi-chung-covid-keo-dai/20211117091032427\">Sống chung cùng Hội chứng Covid kéo dài - Bệnh viện nhân dân 115</a>"));
        source1.setMovementMethod(LinkMovementMethod.getInstance());

        return view;
    }
}
