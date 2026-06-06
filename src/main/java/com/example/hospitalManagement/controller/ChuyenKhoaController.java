package com.example.hospitalManagement.controller;

import com.example.hospitalManagement.entity.Departments;
import com.example.hospitalManagement.entity.Enum.DepartmentStatus;
import com.example.hospitalManagement.repository.DepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class ChuyenKhoaController {

    @Autowired
    private DepartmentRepository departmentRepository;

    /**
     * Chuyển tên khoa thành slug URL-friendly.
     * VD: "KHOA TIM MẠCH" → "khoa-tim-mach"
     */
    private String toSlug(String name) {
        if (name == null) return "";
        String normalized = Normalizer.normalize(name, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replace("đ", "d").replace("Đ", "d");
        return normalized.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-");
    }

    @GetMapping("/chuyen-khoa")
    public String chuyenKhoa(Model model) {
        // Chỉ lấy các khoa ACTIVE từ database
        List<Departments> departments = departmentRepository.findAll()
                .stream()
                .filter(d -> d.getStatus() == DepartmentStatus.ACTIVE)
                .collect(Collectors.toList());

        // Gán thêm slug cho mỗi khoa để dùng trong link href
        model.addAttribute("departments", departments);
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

        // Thử tìm bằng slug cố định trước, nếu không có thì tìm từ DB theo id
        String[] specialty = data.get(slug);
        if (specialty != null) {
            model.addAttribute("title", specialty[0]);
            model.addAttribute("slug", specialty[1]);
            model.addAttribute("intro", specialty[2]);
            model.addAttribute("description", specialty[3]);
            model.addAttribute("strength", specialty[4]);
            return "pages/chi_tiet_chuyen_khoa";
        }

        // Thử tìm theo id nếu slug là số
        try {
            long id = Long.parseLong(slug);
            return departmentRepository.findById(id).map(dept -> {
                model.addAttribute("title", dept.getName());
                model.addAttribute("slug", toSlug(dept.getName()));
                model.addAttribute("intro", dept.getDescription());
                model.addAttribute("description", dept.getDescription());
                model.addAttribute("strength", "Chuyên điều trị và chăm sóc sức khỏe toàn diện cho người bệnh.");
                return "pages/chi_tiet_chuyen_khoa";
            }).orElse("redirect:/chuyen-khoa");
        } catch (NumberFormatException e) {
            return "redirect:/chuyen-khoa";
        }
    }
}