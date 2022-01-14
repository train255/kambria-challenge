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

public class HomeTreatmentFragment extends Fragment {
    private TextView source1;
    private TextView source2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home_treatment, container, false);

        source1 = (TextView) view.findViewById(R.id.treatment_source_1);
        source1.setText(Html.fromHtml("<a href=\"https://hcdc.vn/category/van-de-suc-khoe/covid19/tai-lieu-truyen-thong/huong-dan-goi-cham-soc-suc-khoe-tai-nha-cho-nguoi-f0-phien-ban-16-9d692c99f55ff1da0cce2014e681f21b.html\">Hướng dẫn gói chăm sóc sức khỏe tại nhà cho người F0 (phiên bản 1.6) - Trung tâm Kiểm soát bệnh tật TP. HCM</a>"));
        source1.setMovementMethod(LinkMovementMethod.getInstance());

        source2 = (TextView) view.findViewById(R.id.treatment_source_2);
        source2.setText(Html.fromHtml("<a href=\"https://moh.gov.vn/hoat-dong-cua-dia-phuong/-/asset_publisher/gHbla8vOQDuS/content/9-loai-thuoc-va-6-thiet-bi-f0-can-chuan-bi-e-cach-ly-ieu-tri-tai-nha\">9 loại thuốc và 6 thiết bị F0 cần chuẩn bị để cách ly, điều trị tại nhà\n - Bộ y tế</a>"));
        source2.setMovementMethod(LinkMovementMethod.getInstance());

        return view;
    }
}