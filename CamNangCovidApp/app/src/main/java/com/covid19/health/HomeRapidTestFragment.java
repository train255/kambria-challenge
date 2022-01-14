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

public class HomeRapidTestFragment extends Fragment {
    private TextView source1;
    private TextView source2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home_rapid_test, container, false);

        source1 = (TextView) view.findViewById(R.id.test_source_1);
        source1.setText(Html.fromHtml("<a href=\"https://hcdc.vn/category/van-de-suc-khoe/covid19/tin-tuc-moi-nhat/7-buoc-tu-thuc-hien-xet-nghiem-nhanh-khang-nguyen-tai-nha-284fc0bad50fcba9baa64c9346aa2544.html\">7 bước tự thực hiện xét nghiệm nhanh kháng nguyên tại nhà - Trung tâm Kiểm soát bệnh tật TP. HCM</a>"));
        source1.setMovementMethod(LinkMovementMethod.getInstance());

        source2 = (TextView) view.findViewById(R.id.test_source_2);
        source2.setText(Html.fromHtml("<a href=\"https://www.qdnd.vn/y-te/suc-khoe-tu-van/huong-dan-cach-tu-lay-mau-xet-nghiem-covid-tai-nha-673652\">Hướng dẫn cách tự lấy mẫu xét nghiệm Covid tại nhà - Báo Quân Đội Nhân Dân</a>"));
        source2.setMovementMethod(LinkMovementMethod.getInstance());

        return view;
    }
}
