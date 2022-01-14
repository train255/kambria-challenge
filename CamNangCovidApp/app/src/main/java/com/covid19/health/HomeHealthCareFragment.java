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

public class HomeHealthCareFragment extends Fragment {
    private TextView source1;
    private TextView source2;
    private TextView source3;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home_health_care, container, false);

        source1 = (TextView) view.findViewById(R.id.health_care_source_1);
        source1.setText(Html.fromHtml("<a href=\"http://kiemsoatbenhtatphutho.gov.vn/Chuyen-muc-tin/Chi-tiet-tin/tabid/92/t/tap-the-duc-tai-nha-nang-cao-suc-khoe-trong-mua-dich/title/14489/ctitle/191/language/vi-VN/Default.aspx?AspxAutoDetectCookieSupport=1\">Tập thể dục tại nhà nâng cao sức khỏe trong mùa dịch - Trung tâm kiểm soát bệnh tật tỉnh Phú Thọ</a>"));
        source1.setMovementMethod(LinkMovementMethod.getInstance());

        source2 = (TextView) view.findViewById(R.id.health_care_source_2);
        source2.setText(Html.fromHtml("<a href=\"https://tuoitre.vn/an-uong-gi-de-khoe-trong-mua-dich-20210630015239507.htm\">Ăn uống gì để khỏe trong mùa dịch? - Báo tuổi trẻ</a>"));
        source2.setMovementMethod(LinkMovementMethod.getInstance());

        source3 = (TextView) view.findViewById(R.id.health_care_source_3);
        source3.setText(Html.fromHtml("<a href=\"https://www.vinmec.com/vi/tin-tuc/thong-tin-suc-khoe/dich-2019-ncov/thong-tin-suc-khoe/thuc-pham-giup-tang-suc-de-khang-phong-dich-benh-do-virus-corona\">Thực phẩm giúp tăng sức đề kháng phòng dịch bệnh do virus corona - Bệnh viện Vinmec</a>"));
        source3.setMovementMethod(LinkMovementMethod.getInstance());

        return view;
    }
}
