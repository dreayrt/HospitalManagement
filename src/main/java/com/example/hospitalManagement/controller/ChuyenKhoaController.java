package com.example.hospitalManagement.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.HashMap;
import java.util.Map;

@Controller
public class ChuyenKhoaController {

    @GetMapping("/chuyen-khoa")
    public String chuyenKhoa() {
        return "pages/chuyen_khoa";
    }

    @GetMapping("/chuyen-khoa/{slug}")
    public String chiTietChuyenKhoa(@PathVariable String slug, Model model) {

        Map<String, String[]> data = new HashMap<>();

        data.put("cardiology", new String[]{
                "KHOA TIM MẠCH",
                "cardiology",
                "Tim mạch là chuyên khoa khám và điều trị các bệnh lý về tim, mạch máu, huyết áp, rối loạn nhịp tim và suy tim.",
                "Khoa Tim mạch được trang bị hệ thống thăm dò chức năng tim mạch hiện đại, hỗ trợ chẩn đoán và điều trị hiệu quả cho người bệnh.",
                "Điều trị tăng huyết áp, suy tim, rối loạn nhịp tim, bệnh mạch vành, siêu âm tim, điện tim."
        });

        data.put("neurology", new String[]{
                "KHOA THẦN KINH",
                "neurology",
                "Thần kinh là chuyên khoa khám và điều trị các bệnh liên quan đến não, tủy sống, dây thần kinh và hệ vận động.",
                "Khoa Thần kinh tập trung điều trị đau đầu, mất ngủ, tai biến mạch máu não, rối loạn tiền đình và các bệnh thần kinh ngoại biên.",
                "Điều trị đau đầu, mất ngủ, đột quỵ, rối loạn tiền đình, đau dây thần kinh, Parkinson."
        });

        data.put("pediatrics", new String[]{
                "KHOA NHI",
                "pediatrics",
                "Khoa Nhi chuyên khám, tư vấn và điều trị các bệnh lý thường gặp ở trẻ em từ sơ sinh đến tuổi thiếu niên.",
                "Đội ngũ bác sĩ nhi khoa giàu kinh nghiệm, tận tâm, hỗ trợ chăm sóc toàn diện cho trẻ nhỏ.",
                "Khám tổng quát trẻ em, sốt, ho, viêm họng, dinh dưỡng nhi, bệnh hô hấp và tiêu hóa."
        });

        data.put("orthopedics", new String[]{
                "KHOA XƯƠNG KHỚP",
                "orthopedics",
                "Khoa Xương khớp chuyên điều trị các bệnh về cơ, xương, khớp, chấn thương chỉnh hình và phục hồi vận động.",
                "Khoa hỗ trợ người bệnh giảm đau, phục hồi chức năng và cải thiện chất lượng vận động hằng ngày.",
                "Điều trị thoái hóa khớp, đau cột sống, chấn thương thể thao, viêm khớp, loãng xương."
        });

        data.put("dermatology", new String[]{
                "KHOA DA LIỄU",
                "dermatology",
                "Khoa Da liễu chuyên khám và điều trị các bệnh lý về da, tóc, móng và chăm sóc thẩm mỹ da.",
                "Khoa kết hợp điều trị bệnh lý da liễu và tư vấn chăm sóc da an toàn, hiệu quả.",
                "Điều trị mụn, viêm da, dị ứng da, nám, tàn nhang, rụng tóc và chăm sóc da."
        });

        String[] specialty = data.get(slug);

        if (specialty == null) {
            return "redirect:/chuyen-khoa";
        }

        model.addAttribute("title", specialty[0]);
        model.addAttribute("slug", specialty[1]);
        model.addAttribute("intro", specialty[2]);
        model.addAttribute("description", specialty[3]);
        model.addAttribute("strength", specialty[4]);

        return "pages/chi_tiet_chuyen_khoa";
    }
}