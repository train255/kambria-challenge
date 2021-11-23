package covid19.detection;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class FAQFragment extends Fragment {
    private TextView textView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_faq, container, false);
        textView = (TextView) view.findViewById(R.id.textView);

        textView.setText(Html.fromHtml("" +
                "<h3>1. Thắc mắc về triệu chứng</h3>" +
                "<h4>Bệnh Covid-19 và bệnh cảm thông thường khác nhau như thế nào?</h4>" +
                "<p>Bệnh COVID-19 biểu hiện chủ yếu là phát sốt; đau đầu và toàn thân đau nhức, mất sức, ho khan, ít đàm; một số người bệnh có triệu chứng thở gấp, khó thở, số ít diễn biến thành hội chứng ngạt thở; thời kỳ đầu tế bào bạch cầu bình thường hoặc giảm nhẹ, X-quang phổi cho thấy diễn biến viêm phổi. Bệnh cảm thông thường triệu chứng bao gồm phát sốt, ho, đau đầu, bệnh sau vài ngày có thể diễn biến tốt, cũng thường không có dấu hiệu viêm phổi.</p>" +
                "<h4>Phát sốt; ho phải làm sao?</h4>" +
                "<p>Phương pháp đầu tiên để phân biệt bệnh cảm và bệnh viêm đường hô hấp cấp trước tiên là có hay không việc từng tiếp xúc với người mắc bệnh, hoặc đã từng đi tới vùng dịch; nếu đã có tiền sử như vậy lại xuất hiện triệu chứng sốt cao, ho... phải đi điều trị sớm; có cách ly hay không do bác sĩ quyết định. Nếu không có tình trạng này thì không nên căng thẳng một cách vô cớ, không nên cho rằng một khi bị cảm, trong người khó chịu thì khẳng định là mắc bệnh COVID-19.<br/>" +
                "Nếu nghi ngờ một người mắc bệnh COVID-19, phải chăng chụp X-quang mới chứng minh là lây nhiễm?<br/>" +
                "Nếu nghi ngờ mắc bệnh COVID-19 nên sớm đi bệnh viện điều trị, chụp X-quang trợ giúp cho việc chẩn đoán." +
                "</p>" +
                "<h4>Sau khi lây nhiễm, thời gian khoảng bao lâu virus corona chủng mới sẽ gây bệnh?</h4>" +
                "<p>Xét về góc độ y học, vấn đề chính là thời kỳ tiềm ẩn, nói cách khác là khoảng thời gian bị nhiễm bệnh cho đến thời gian bộc phát, ngắn nhất là 1 ngày, dài nhất là 14 ngày hoặc hơn, thường là 3 - 4 ngày.</p>" +
                "<h4>Hơi có chút ho hoặc cảm mạo, nghi ngờ mình đã mắc bệnh COVID-19, vậy có đúng không?</h4>" +
                "<p>Cảm mạo hoặc ho thì nghi ngờ đã mắc bệnh COVID-19 là không đúng. Vào mùa đông xuân, bệnh đường hô hấp, viêm đường hô hấp trên hoặc một số bệnh viêm phổi điển hình (viêm phổi do vi khuẩn), viêm phế quản... là bệnh thường gặp. Cho nên, khi xảy ra các triệu chứng trên thì sợ hãi, nghi ngờ mình đã mắc bệnh COVID-19 là không nên. Nếu như người sốt rất cao, đồng thời kèm ho, thậm chí xuất hiện triệu chứng toàn thân mất sức, đau cơ, đau đầu, đặc biệt là đã từng đi qua vùng có dịch, tiếp xúc qua người đã mắc bệnh, thì nên đến bệnh viện thăm khám, kiểm tra.</p>" +
                "<h3>2. Thắc mắc về phương pháp điều trị</h3>" +
                "<h4>Có phương pháp điều trị bệnh COVID-19 không?</h4>" +
                "<p>Trước mắt chưa có thuốc đặc hiệu và phương pháp điều trị, tuy nhiên, sau khi điều trị triệu chứng và qua điều trị bảo tồn kịp thời, đại đa số người bệnh hồi phục và lành bệnh.</p>"+
                "<h4>Khẩu trang thông thường dùng bao nhiêu lần thì loại bỏ?</h4>" +
                "<p>Mang khẩu trang tùy theo tình hình; mang khẩu trang trong bệnh viện trong vòng 3 - 4 giờ nên thay khẩu trang mới.</p>" +
                "<h4>Ngồi xe công cộng có cần mang khẩu trang không?</h4>" +
                "<p>Nếu tuyến đường này chưa phát hiện ca bệnh COVID-19, khi ngồi các phương tiện công cộng, không cần mang khẩu trang; nếu tuyến đường này đã có nhiều ca bệnh COVID-19 xảy ra, ngoài ra, nếu sức đề kháng của bạn hơi kém cũng có thể mang khẩu trang để phòng vệ.</p>" +
                "<h4>Theo nghiên cứu hiện nay, các nhà chuyên môn phải chăng đưa ra kết luận người nào dễ mắc bệnh?Những người nào có tỷ lệ tử vong cao?</h4>" +
                "<p>Tính đến nay, nghành y tế đã có một tổng kết sơ bộ số ca tử vong nguyên nhân do COVID-19 (và những đợt dịch trước kia). Căn cứ phân tích cho thấy, những ca tử vong có nguyên nhân như sau: một số người sau khi mắc bệnh thời gian đầu không được quan tâm, khi bệnh trạng diễn biến nghiêm trọng mới đến bệnh viện, sau cùng dẫn đến tử vong; nhiều hơn nữa là số người đã mắc một số bệnh khác, chẳng hạn như bệnh tim mạch, bệnh đái tháo đường, đồng thời đã lớn tuổi. Khi bệnh viêm đường hô hấp cấp xảy ra, vài căn bệnh xảy ra cùng một lúc, tình trạng này càng dễ dẫn đến tử vong.Xem xét tình hình đại đa số bệnh nhân, với những ca bệnh này, sau khi điều trị hiệu quả đều rất tốt, rất nhanh chóng hồi phục xuất viện.</p>" +
                "<h4>Người dân làm thế nào dự phòng bệnh COVID-19?</h4>" +
                "<p>Người dân chủ yếu tận dụng những biện pháp phòng ngừa như sau: đảm bảo vệ sinh cá nhân tốt; hắt hơi, ho khạc và sau khi vệ sinh mũi phải rửa tay; sau khi rửa tay, dùng khăn lau sạch và khăn giấy lau khô; không dùng khăn lông chung; chú ý ăn uống cân bằng; căn cứ mùa để tăng giảm áo mặc; vận động định kỳ; nghỉ ngơi đầy đủ; giảm nhẹ áp lực và tránh hút thuốc, tăng sức đề kháng của cơ thể; đảm bảo không khí trong nhà thông thoáng, thường xuyên mở hết các cửa sổ, cho không khí lưu thông; đảm bảo máy điều hòa tính năng tốt, thường xuyên tẩy rửa lưới lọc; tránh đi đến nơi công cộng có người đông đúc, không khí không thông thoáng.</p>" +
                "<h4>Mang khẩu trang có thể phòng ngừa lây bệnh không?</h4>" +
                "<p>Có trợ giúp phòng ngừa lây nhiễm, nhưng trước khi mang khẩu trang phải rửa tay. Những đối tượng cần mang khẩu trang:<br/>" +
                "<ul>" +
                "<li>Người có triệu chứng viêm đường hô hấp cấp.</li>" +
                "<li>Người chăm sóc người bệnh viêm đường hô hấp cấp.</li>" +
                "<li>Sau khi tiếp xúc với người đã xác định mắc bệnh viêm đường hô hấp cấp, tối thiểu trong 10 ngày phải mang khẩu trang, tính từ ngày sau cùng tiếp xúc với người bệnh.</li>" +
                "<li>Nhân viên y tế.</li>" +
                "</ul>" +
                "</p>" +
                "<h4>Xung quanh có ca bệnh nghi ngờ, phải làm thế nào?</h4>" +
                "<h4><i>Không thăm viếng</i></h4>" +
                "<p>Do bệnh COVID-19 thông qua con đường lan truyền bằng bọt khí khoảng cách gần, tính lây nhiễm mạnh, người đang cách ly rất có khả năng trong thời kỳ tiềm ẩn, để tránh bệnh lây lan, đồng thời giúp người cách ly an tâm nghỉ ngơi, nên không thích hợp có người đến thăm viếng.</p>" +
                "<h4><i>Cách ly sớm</i></h4>" +
                "<p>Cách ly đối với người nghi mắc bệnh viêm đường hô hấp cấp và người từng tiếp xúc mật thiết với người bệnh, theo thời kỳ tiềm ẩn lớn nhất của bệnh mà đưa ra biện pháp cách ly; theo dõi tình trạng sức khỏe người bệnh, đánh giá phải chăng có khả năng lây bệnh, theo đó dựa vào thời kỳ tiềm ẩn và thời kỳ tiến triển của người bệnh để chẩn đoán, điều trị sớm và cứu hộ. Đây là một biện pháp bảo vệ đối với người nghi mắc bệnh; người từng tiếp xúc mật thiết với người bệnh và người xung quanh.</p>" +
                "<h4><i>Nhập viện sớm</i></h4>" +
                "<p>Khi xuất hiện triệu chứng bệnh đường hô hấp như phát sốt ho, nên sớm chuyển bệnh nhân đến bệnh viện bằng xe cấp cứu chuyên nghiệp. Đối với người bệnh không thể loại trừ triệu chứng viêm đường hô hấp trên lâm sàng, phải phối hợp với nhân viên y tế có biện pháp cách ly. Có như vậy mới giúp ích cho việc theo dõi bệnh trạng và điều trị kịp thời, đồng thời cũng có thể phòng ngừa bệnh lây lan.</p>" +
                "<h3>3. Thắc mắc về điều trị</h3>" +
                "<h4>Đã phát sốt phải làm như thế nào?</h4>" +
                "<p>Sau khi phát sốt, bất kể đang nghi hoặc có triệu chứng viêm đường hô hấp cấp hay không, nhất định bệnh nhân không đi bằng phương tiện giao thông công cộng (xe bus, xe khách...), để giảm khả năng lây bệnh cho người khác. Lúc này bạn cũng không nên hoảng sợ, có thể xác nhận bản thân phải chăng có sốt thật sự, phải chăng có triệu chứng ho khan, nhớ lại mình gần đây phải chăng đi đến nơi đông đúc có nhiều khả năng lây truyền virus corona, hoặc không nhớ khả năng từng tiếp xúc mật thiết với người bệnh hoặc “nghi ngờ” mắc bệnh. Sau khi suy nghĩ các tình huống này, lập tức chúng ta gọi đến đường dây nóng của bệnh viện để được tham vấn, phối hợp nhân viên tư vấn, cố gắng diễn tả rõ về tình trạng của mình một cách chính xác, nghe sự hướng dẫn của chuyên viên, hoặc nghỉ ngơi tại nhà theo dõi tình hình, hoặc chờ xe chuyên dụng đến khu cách ly.</p>" +
                "<h4>Đi chữa sốt cần lưu ý những gì?</h4>" +
                "<p>" +
                "<ul>" +
                "<li>Trước tiên, chuẩn bị trả lời những câu hỏi về phát sốt, tránh phải suy nghĩ từng câu từng câu khi được tham vấn, vừa lãng phí thời gian của bác sĩ, kéo dài thời gian khám bệnh của mình, vừa tăng cơ hội lây bệnh. Cần nhớ rõ thời gian phát bệnh, tốt nhất đo thân nhiệt chính xác tại nhà, ghi lại chỉ số, báo cáo với bác sĩ. Ngoài ra, nói rõ với bác sĩ triệu chứng bản thân, có ho khan hay không; đau nhức toàn thân hay không. Địa chỉ cụ thể, số CMND/căn cước đều có thể được đề cập khi tham vấn, cho nên phải nhớ rõ.</li>" +
                "<li>Tìm hiểu đường đến bệnh viện trước khi đi, không đi đường vòng vo.</li>" +
                "<li>Đến bệnh viện trực tiếp gặp phòng tham vấn, trước tiên xác định rõ vị trí không hỏi tới hỏi lui mất thời giờ.</li>" +
                "<li>Tự mình mang theo nhiệt kế cá nhân sẽ tự tin hơn về mặt tâm lý.</li>" +
                "<li>Nhất định phải mang khẩu trang, không chỉ bảo vệ chính mình mà còn bảo vệ nhân viên y tế và cả những người bệnh khác.</li>" +
                "<li>Tốt nhất không để người nhà đi kèm, giảm nguy cơ lây nhiễm cho người nhà.</li>" +
                "<li>Trong khi chờ đợi đến lượt khám, cố gắng đứng nơi thoáng gió, giảm tiếp xúc với người bệnh khác ở khoảng cách gần.</li>" +
                "<li>Không tùy tiện sờ mó, cố gắng ít tiếp xúc các đồ vật công cộng, chẳng hạn như tay vịn cầu thang, tay nắm cửa, nút bấm..., có thể mang khăn giấy tiệt trùng, mọi lúc chùi tay.</li>" +
                "<li>Tại nơi khám bệnh không dụi mắt, móc lỗ mũi, hắt hơi; khạc đàm phải che kín, không đối mặt với người khác, phải dùng khăn giấy che lại.</li>" +
                "</ul>" +
                "</p>" +
                "<h3>4. Thắc mắc về đường lây truyền</h3>" +
                "<h4>Bệnh COVID lây truyền qua con đường nào?</h4>" +
                "<p>Bệnh COVID-19 thông qua con đường lan truyền bằng bọt khí của người mắc bệnh, đến nay, bệnh được xác định là có lây truyền từ người sang người qua đường tiếp xúc trực tiếp với chất tiết từ đường hô hấp của người bệnh. Bệnh còn lây qua đường gián tiếp khi bàn tay người lành tiếp xúc với các đồ vật bị nhiễm virus, sau đó đưa vào mắt, mũi, miệng và gây nhiễm bệnh.</p>" +
                "<h4>Làm thế nào tránh lây nhiễm tại phòng làm việc?</h4>" +
                "<p>Mọi người nếu thấy cơ thể bất ổn, nên khám bệnh sớm, nghỉ ngơi tại nhà, không đi làm việc; tất cả người lao động phải lưu ý vệ sinh cá nhân và bồi dưỡng thói quen sống lành mạnh; đảm bảo không khí phòng ốc thông thoáng, thường xuyên mở cửa sổ, thường xuyên tẩy rửa lưới lọc máy điều hòa; đảm bảo các vật dụng trong phòng làm việc sạch sẽ.</p>" +
                "<h4>Làm thế nào tránh lây nhiễm trong thang máy?</h4>" +
                "<p>Mọi người cần lưu ý vệ sinh cá nhân, thường xuyên rửa tay; khi ho hoặc hắt hơi cần che mũi miệng. Nếu có triệu chứng viêm đường hô hấp, phải mang khẩu trang. Nhân viên quản lý tòa nhà phải đảm bảo vệ sinh thang máy; cửa, tay cầm, nút bấm của thang máy phải thường xuyên dùng thuốc tẩy gia đình pha loãng để làm sạch.</p>" +
                "<h4>Đến bệnh viện có bị lây nhiễm không?</h4>" +
                "<p>Người đến bệnh viện phải chú ý vệ sinh cá nhân.Mang khẩu trang giúp phòng ngừa lây nhiễm bệnh.</p>" +
                "<h4>Người nhà và bạn bè đã thật sự mắc bệnh, biện pháp phòng ngừa như thế nào?</h4>" +
                "<p>Không nên đi thăm viếng người bệnh. Người có tiếp xúc mật thiết với người bệnh phải lưu ý những điều như sau:</p>" +
                "<ul>" +
                "<li>Không đi làm hoặc đi học; nghỉ ngơi tại nhà, nhưng hàng ngày phải đến trung tâm y tế địa phương báo cáo, tiếp nhận kiểm tra, thời gian là 10 ngày.</li>" +
                "<li>Khi cần ra khỏi nhà, phải mang khẩu trang, đảm bảo thói quen vệ sinh cá nhân tốt.</li>"+
                "<li>Nếu nghi ngờ bản thân từng tiếp xúc với người mắc bệnh, nên đi khám tầm soát; trong vòng 10 ngày phải mang khẩu trang.</li>"+
                "<li>Vật dụng và đồ chơi trong nhà người mắc bệnh phải dùng thuốc tẩy gia đình pha loãng để rửa sạch.</li>"+
                "<li>Lưu ý tình trạng sức khỏe bản thân, chú ý vệ sinh cá nhân, thường xuyên rửa tay.</li>"+
                "<li>Khi cảm thấy khó chịu, lập tức đi khám bệnh.</li>"+
                "</ul>" +
                "<h4>Sau khi đến bệnh viện thăm bệnh, quần áo đang mặc phải giặt sạch ngay lập tức?</h4>" +
                "<p>Lập tức giặt sạch.</p>" +
                "<h4>Khi dùng cơm chung cần lưu ý những gì?</h4>" +
                "<p>Khuyến khích thực hiện hình thức ăn riêng.</p>" +
                "<h4>Thời điểm này đi du lịch có an toàn không?</h4>" +
                "<p>Tổ chức Y tế Thế giới (WHO) khuyến cáo mọi người tránh đi đến những nơi đang có dịch bùng phát. Người từng đi du lịch nếu có triệu chứng nghi ngờ, nên kịp thời tham vấn với chuyên gia.</p>"));
        textView.setMovementMethod(new ScrollingMovementMethod());
        return view;
    }

}
