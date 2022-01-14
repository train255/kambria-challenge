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

public class HomeVaccineFragment extends Fragment {
    private TextView source1;
    private TextView source2;
    private TextView source3;
    private TextView source4;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home_vaccine, container, false);

        source1 = (TextView) view.findViewById(R.id.vaccine_source_1);
        source1.setText(Html.fromHtml("<a href=\"https://www.unicef.org/vietnam/vi/nh%E1%BB%AFng-%C4%91i%E1%BB%81u-c%E1%BA%A7n-bi%E1%BA%BFt-v%E1%BB%81-v%E1%BA%AFc-xin-covid-19\">Những điều cần biết về vaccine COVID-19 - UNICEF Việt Nam</a>"));
        source1.setMovementMethod(LinkMovementMethod.getInstance());

        source2 = (TextView) view.findViewById(R.id.vaccine_source_2);
        source2.setText(Html.fromHtml("<a href=\"https://hcdc.vn/public/img/02bf8460bf0d6384849ca010eda38cf8e9dbc4c7/images/mod1/images/tim-hieu-ve-cac-loai-vacxin-phong-covid19-duoc-phe-duyet-tai-viet-nam/files/C%C3%A1c%20lo%E1%BA%A1i%20v%E1%BA%AFc-xin%20COVID-19.pdf\">VẮC-XIN COVID-19 HOẠT ĐỘNG NHƯ THẾ NÀO? - Trung tâm kiểm soát bệnh tật TP. HCM</a>"));
        source2.setMovementMethod(LinkMovementMethod.getInstance());

        source3 = (TextView) view.findViewById(R.id.vaccine_source_3);
        source3.setText(Html.fromHtml("<a href=\"https://vnvc.vn/tiem-vaccine-covid-19-nen-an-gi-kieng-gi/\">Trước và sau tiêm vaccine Covid-19 nên ăn gì, kiêng gì? - VNVC</a>"));
        source2.setMovementMethod(LinkMovementMethod.getInstance());

        source4 = (TextView) view.findViewById(R.id.vaccine_source_4);
        source4.setText(Html.fromHtml("<a href=\"https://tuoitre.vn/co-quan-y-te-chau-au-tiem-tang-cuong-qua-thuong-xuyen-co-the-lam-yeu-he-mien-dich-202201121205157.htm\">Cơ quan Y tế châu Âu: Tiêm tăng cường quá thường xuyên có thể làm yếu hệ miễn dịch - Báo tuổi trẻ</a>"));
        source4.setMovementMethod(LinkMovementMethod.getInstance());

        return view;
    }
}
