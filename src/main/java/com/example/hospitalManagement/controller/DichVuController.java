package com.example.hospitalManagement.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DichVuController {

    @GetMapping("/dich-vu")
    public String dichVu() {
        return "pages/dich_vu";
    }

    // ===== 6 Gói dịch vụ chi tiết =====

    @GetMapping("/chi-tiet-tong-quat")
    public String chiTietTongQuat(Model model) {
        model.addAttribute("tenGoi", "Gói khám tổng quát cơ bản");
        model.addAttribute("loaiDichVu", "Khám sức khỏe định kỳ");
        model.addAttribute("emoji", "🩺");
        model.addAttribute("moTaNgan",
            "Gói khám toàn diện giúp đánh giá tổng thể tình trạng sức khỏe, " +
            "phát hiện sớm các nguy cơ bệnh lý thường gặp.");
        model.addAttribute("thoiGianThucHien", "2–3 giờ");
        model.addAttribute("soHangMuc", "15+ hạng mục");
        model.addAttribute("giaHienThi", "Liên hệ báo giá");
        model.addAttribute("gioiThieu",
            "Gói khám tổng quát cơ bản được thiết kế nhằm đánh giá toàn diện " +
            "tình trạng sức khỏe của người bệnh thông qua các xét nghiệm và thăm khám lâm sàng cơ bản.");
        model.addAttribute("gioiThieu2",
            "Đây là lựa chọn lý tưởng cho người muốn kiểm tra sức khỏe định kỳ " +
            "hàng năm nhằm phát hiện sớm các vấn đề tiềm ẩn.");
        model.addAttribute("doiTuong",
            "Phù hợp với mọi độ tuổi, đặc biệt người trưởng thành từ 18 tuổi trở lên " +
            "chưa có triệu chứng bệnh cụ thể và muốn kiểm tra sức khỏe định kỳ.");
        model.addAttribute("soBacSi", "25+");
        model.addAttribute("soLuongPhucVu", "8.000+");
        model.addAttribute("danhGia", "4.9/5");
        return "pages/chi_tiet_dich_vu";
    }

    @GetMapping("/chi-tiet-ung-thu")
    public String chiTietUngThu(Model model) {
        model.addAttribute("tenGoi", "Gói tầm soát ung thư toàn diện");
        model.addAttribute("loaiDichVu", "Tầm soát ung thư");
        model.addAttribute("emoji", "🔬");
        model.addAttribute("moTaNgan",
            "Gói tầm soát chuyên sâu giúp phát hiện sớm các dấu hiệu ung thư " +
            "phổ biến trước khi có triệu chứng lâm sàng.");
        model.addAttribute("thoiGianThucHien", "3–4 giờ");
        model.addAttribute("soHangMuc", "20+ hạng mục");
        model.addAttribute("giaHienThi", "Liên hệ báo giá");
        model.addAttribute("gioiThieu",
            "Gói tầm soát ung thư toàn diện kết hợp các xét nghiệm marker ung thư, " +
            "chẩn đoán hình ảnh và nội soi nhằm phát hiện sớm các loại ung thư phổ biến " +
            "như ung thư gan, phổi, đại tràng, dạ dày, tuyến giáp và cổ tử cung.");
        model.addAttribute("gioiThieu2",
            "Phát hiện ung thư ở giai đoạn sớm giúp tăng đáng kể tỷ lệ điều trị thành công " +
            "và cải thiện chất lượng cuộc sống.");
        model.addAttribute("doiTuong",
            "Người từ 35 tuổi trở lên, có tiền sử gia đình mắc bệnh ung thư, " +
            "người hút thuốc lá, uống rượu nhiều hoặc có lối sống ít vận động.");
        model.addAttribute("soBacSi", "30+");
        model.addAttribute("soLuongPhucVu", "5.000+");
        model.addAttribute("danhGia", "4.9/5");
        return "pages/chi_tiet_dich_vu";
    }

    @GetMapping("/chi-tiet-lay-mau")
    public String chiTietLayMau(Model model) {
        model.addAttribute("tenGoi", "Dịch vụ lấy mẫu xét nghiệm tại nhà");
        model.addAttribute("loaiDichVu", "Xét nghiệm tại nhà");
        model.addAttribute("emoji", "🏠");
        model.addAttribute("moTaNgan",
            "Đội ngũ điều dưỡng chuyên nghiệp đến tận nơi lấy mẫu xét nghiệm, " +
            "trả kết quả online trong vòng 24 giờ.");
        model.addAttribute("thoiGianThucHien", "15–30 phút");
        model.addAttribute("soHangMuc", "Theo yêu cầu");
        model.addAttribute("giaHienThi", "Liên hệ báo giá");
        model.addAttribute("gioiThieu",
            "Dịch vụ lấy mẫu xét nghiệm tại nhà mang đến sự tiện lợi tối đa cho người bệnh, " +
            "đặc biệt người cao tuổi, người bận rộn hoặc khó di chuyển. " +
            "Mẫu được vận chuyển về phòng lab đạt chuẩn ISO để phân tích chính xác.");
        model.addAttribute("gioiThieu2",
            "Kết quả xét nghiệm được gửi trực tiếp qua ứng dụng, email hoặc SMS, " +
            "đi kèm với tư vấn sơ bộ từ bác sĩ.");
        model.addAttribute("doiTuong",
            "Người cao tuổi, người bệnh khó di chuyển, phụ nữ mang thai, " +
            "người bận rộn không có thời gian đến cơ sở y tế.");
        model.addAttribute("soBacSi", "15+");
        model.addAttribute("soLuongPhucVu", "3.000+");
        model.addAttribute("danhGia", "4.8/5");
        return "pages/chi_tiet_dich_vu";
    }

    @GetMapping("/chi-tiet-tim-mach")
    public String chiTietTimMach(Model model) {
        model.addAttribute("tenGoi", "Gói tầm soát bệnh lý tim mạch");
        model.addAttribute("loaiDichVu", "Tim mạch");
        model.addAttribute("emoji", "❤️");
        model.addAttribute("moTaNgan",
            "Gói chuyên sâu đánh giá toàn diện chức năng tim, mạch máu và các yếu tố " +
            "nguy cơ bệnh lý tim mạch.");
        model.addAttribute("thoiGianThucHien", "3–4 giờ");
        model.addAttribute("soHangMuc", "18+ hạng mục");
        model.addAttribute("giaHienThi", "Liên hệ báo giá");
        model.addAttribute("gioiThieu",
            "Gói tầm soát bệnh lý tim mạch bao gồm đo điện tâm đồ, siêu âm tim, " +
            "xét nghiệm mỡ máu, đường huyết và các chỉ số nguy cơ tim mạch nhằm " +
            "phát hiện sớm nhồi máu cơ tim, suy tim, rối loạn nhịp tim và bệnh mạch vành.");
        model.addAttribute("gioiThieu2",
            "Bệnh lý tim mạch là nguyên nhân tử vong hàng đầu tại Việt Nam. " +
            "Tầm soát sớm giúp kiểm soát nguy cơ và điều trị kịp thời.");
        model.addAttribute("doiTuong",
            "Người từ 40 tuổi trở lên, người có tiền sử gia đình mắc bệnh tim mạch, " +
            "người bị tăng huyết áp, tiểu đường, béo phì hoặc hút thuốc lá.");
        model.addAttribute("soBacSi", "20+");
        model.addAttribute("soLuongPhucVu", "6.000+");
        model.addAttribute("danhGia", "5.0/5");
        return "pages/chi_tiet_dich_vu";
    }

    @GetMapping("/chi-tiet-tien-thai-san")
    public String chiTietTienThaiSan(Model model) {
        model.addAttribute("tenGoi", "Gói khám sức khỏe tiền thai sản");
        model.addAttribute("loaiDichVu", "Sức khỏe sinh sản");
        model.addAttribute("emoji", "🤰");
        model.addAttribute("moTaNgan",
            "Gói khám toàn diện dành cho các cặp vợ chồng chuẩn bị mang thai, " +
            "đảm bảo sức khỏe tốt nhất trước khi thụ thai.");
        model.addAttribute("thoiGianThucHien", "3–5 giờ");
        model.addAttribute("soHangMuc", "22+ hạng mục");
        model.addAttribute("giaHienThi", "Liên hệ báo giá");
        model.addAttribute("gioiThieu",
            "Gói khám sức khỏe tiền thai sản được thiết kế đặc biệt cho cả vợ và chồng " +
            "chuẩn bị sinh con. Bao gồm kiểm tra di truyền, sàng lọc bệnh lây truyền, " +
            "đánh giá khả năng sinh sản và tư vấn dinh dưỡng tiền thai kỳ.");
        model.addAttribute("gioiThieu2",
            "Chuẩn bị sức khỏe tốt trước khi mang thai giúp giảm nguy cơ dị tật bẩm sinh, " +
            "đảm bảo thai kỳ khỏe mạnh và em bé phát triển toàn diện.");
        model.addAttribute("doiTuong",
            "Các cặp vợ chồng đang có kế hoạch mang thai, " +
            "đặc biệt những cặp đôi lần đầu chuẩn bị làm cha mẹ hoặc có tiền sử thai kỳ bất thường.");
        model.addAttribute("soBacSi", "18+");
        model.addAttribute("soLuongPhucVu", "4.000+");
        model.addAttribute("danhGia", "4.9/5");
        return "pages/chi_tiet_dich_vu";
    }

    @GetMapping("/chi-tiet-tiem-chung")
    public String chiTietTiemChung(Model model) {
        model.addAttribute("tenGoi", "Dịch vụ tiêm chủng vắc xin trọn gói");
        model.addAttribute("loaiDichVu", "Tiêm chủng");
        model.addAttribute("emoji", "💉");
        model.addAttribute("moTaNgan",
            "Chương trình tiêm chủng đầy đủ với vắc xin chất lượng cao, " +
            "bảo vệ toàn diện cho trẻ em và người lớn.");
        model.addAttribute("thoiGianThucHien", "30–60 phút/lần");
        model.addAttribute("soHangMuc", "Theo lịch tiêm");
        model.addAttribute("giaHienThi", "Liên hệ báo giá");
        model.addAttribute("gioiThieu",
            "Dịch vụ tiêm chủng vắc xin trọn gói cung cấp đầy đủ các loại vắc xin " +
            "theo khuyến cáo của Bộ Y tế và WHO, từ vắc xin cơ bản đến vắc xin dịch vụ " +
            "như cúm, viêm gan B, HPV, thủy đậu, phế cầu và nhiều loại khác.");
        model.addAttribute("gioiThieu2",
            "Đội ngũ y tá và bác sĩ giàu kinh nghiệm thực hiện tiêm chủng trong môi trường " +
            "vô khuẩn, theo dõi phản ứng sau tiêm trong 30 phút để đảm bảo an toàn tuyệt đối.");
        model.addAttribute("doiTuong",
            "Trẻ sơ sinh, trẻ em trong độ tuổi tiêm chủng mở rộng, " +
            "người lớn cần bổ sung miễn dịch, phụ nữ chuẩn bị mang thai và người cao tuổi.");
        model.addAttribute("soBacSi", "12+");
        model.addAttribute("soLuongPhucVu", "10.000+");
        model.addAttribute("danhGia", "4.9/5");
        return "pages/chi_tiet_dich_vu";
    }
}