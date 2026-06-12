package com.example.hospitalManagement.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

@Controller
public class HoTroController {

    @GetMapping("/ho-tro")
    public String hoTro() {
        return "pages/ho_tro";
    }

    @GetMapping("/ho-tro/chi-tiet")
    public String chiTietHoTro(@RequestParam("id") String slug, Model model) {

        Map<String, String[]> data = new HashMap<>();

        data.put("huong-dan-dat-lich", new String[]{
                "Hướng dẫn đặt lịch khám bệnh trực tuyến",
                "01/01/2025",
                "Nhằm giúp người bệnh chủ động sắp xếp lịch khám, giảm thời gian chờ đợi và nâng cao trải nghiệm khám chữa bệnh, MediCore Health triển khai hình thức đặt lịch khám trực tuyến thông qua website và ứng dụng MediCore Care.",
                "1. Hướng dẫn các bước đặt lịch khám|1.1. Bước 1: Chọn Đặt khám|1.2. Bước 2: Chọn hồ sơ người bệnh|1.3. Bước 3: Chọn thông tin khám|1.4. Bước 4: Xác nhận thông tin đặt khám|1.5. Bước 5: Thanh toán|1.6. Bước 6: Hoàn thành và nhận phiếu khám|2. Khi đến Bệnh viện khám bệnh"
        });

        data.put("bao-hiem-y-te", new String[]{
                "Hướng dẫn thủ tục khám chữa bệnh bảo hiểm y tế",
                "18/11/2025",
                "Để đảm bảo được hưởng đầy đủ quyền lợi BHYT khi khám chữa bệnh, người bệnh cần xuất trình đầy đủ giấy tờ theo quy định như thẻ BHYT, CCCD, giấy chuyển tuyến hoặc giấy hẹn tái khám.",
                "1. Thủ tục BHYT|2. Mức thanh toán BHYT|3. Một số lưu ý"
        });

        data.put("bao-lanh-vien-phi", new String[]{
                "Hướng dẫn thực hiện thủ tục bảo lãnh viện phí",
                "20/11/2025",
                "Người bệnh có thể thực hiện thủ tục bảo lãnh viện phí tại quầy tiếp nhận theo hướng dẫn của nhân viên bệnh viện. Việc chuẩn bị đầy đủ giấy tờ giúp quá trình xử lý hồ sơ nhanh chóng hơn.",
                "1. Chuẩn bị giấy tờ|2. Liên hệ quầy tiếp nhận|3. Xác nhận thông tin bảo lãnh|4. Hoàn tất thủ tục"
        });

        data.put("noi-tru", new String[]{
                "Thông tin cần biết cho người bệnh nội trú",
                "22/11/2025",
                "Người bệnh nội trú cần nắm rõ các quy định về giờ thăm bệnh, vật dụng cá nhân, chế độ dinh dưỡng và quy trình chăm sóc trong thời gian điều trị tại bệnh viện.",
                "1. Quy định nhập viện|2. Vật dụng cần chuẩn bị|3. Giờ thăm bệnh|4. Quy định trong thời gian điều trị"
        });

        data.put("thanh-toan-vien-phi", new String[]{
                "Hướng dẫn thanh toán viện phí không dùng tiền mặt",
                "25/11/2025",
                "MediCore Health hỗ trợ nhiều hình thức thanh toán không dùng tiền mặt như thẻ ngân hàng, ví điện tử, mã QR nhằm giúp người bệnh thanh toán nhanh chóng và thuận tiện.",
                "1. Thanh toán bằng thẻ|2. Thanh toán bằng mã QR|3. Thanh toán qua ví điện tử|4. Lưu ý khi thanh toán"
        });

        data.put("hoa-don-vat", new String[]{
                "Hướng dẫn phát hành hóa đơn VAT",
                "28/11/2025",
                "Người bệnh có nhu cầu xuất hóa đơn VAT cần cung cấp đầy đủ thông tin cá nhân hoặc thông tin đơn vị theo quy định tại quầy thu ngân.",
                "1. Thông tin cần cung cấp|2. Thời gian xuất hóa đơn|3. Nhận hóa đơn điện tử"
        });

        data.put("hoan-tien", new String[]{
                "Quy trình hoàn tiền phí dịch vụ",
                "30/11/2025",
                "Trong một số trường hợp phát sinh, người bệnh có thể thực hiện quy trình hoàn tiền phí dịch vụ theo hướng dẫn của bệnh viện.",
                "1. Điều kiện hoàn tiền|2. Hồ sơ cần chuẩn bị|3. Thời gian xử lý"
        });

        data.put("xac-nhan-bhyt", new String[]{
                "Xác nhận bảo hiểm y tế khi đặt khám",
                "02/12/2025",
                "Người bệnh có thể xác nhận thông tin bảo hiểm y tế khi đặt lịch khám để quá trình tiếp nhận và thanh toán diễn ra thuận tiện hơn.",
                "1. Chọn thông tin BHYT|2. Kiểm tra thông tin|3. Xác nhận khi đặt khám"
        });

        data.put("mua-thuoc", new String[]{
                "Hướng dẫn mua thuốc tại nhà thuốc bệnh viện",
                "05/12/2025",
                "Sau khi khám bệnh, người bệnh có thể mua thuốc tại nhà thuốc bệnh viện theo đơn thuốc được bác sĩ chỉ định.",
                "1. Nhận đơn thuốc|2. Đến nhà thuốc bệnh viện|3. Thanh toán và nhận thuốc"
        });

        String[] item = data.get(slug);

        if (item == null) {
            return "redirect:/ho-tro";
        }

        model.addAttribute("title", item[0]);
        model.addAttribute("date", item[1]);
        model.addAttribute("description", item[2]);
        model.addAttribute("toc", item[3].split("\\|"));

        return "pages/chi_tiet_ho_tro";
    }
}