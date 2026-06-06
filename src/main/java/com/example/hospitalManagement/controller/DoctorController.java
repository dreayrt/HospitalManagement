package com.example.hospitalManagement.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@Controller
public class DoctorController {

    @GetMapping("/bac-si")
    public String bacSi() {
        return "pages/bac_si";
    }

    @GetMapping("/ho-so-bac-si/{slug}")
    public String hoSoBacSi(@PathVariable String slug, Model model) {

        Map<String, String[]> data = Map.of(
                "cardiology", new String[]{
                        "BS CKII.", "Bùi Cao Mỹ Ái", "Khoa Tim mạch can thiệp", "Cardiology",
                        "https://randomuser.me/api/portraits/women/44.jpg",
                        "Tim mạch, can thiệp tim mạch", "Thứ hai, Thứ năm"
                },
                "neurology", new String[]{
                        "TS BS.", "Đặng Trung An", "Khoa Thần kinh", "Neurology",
                        "https://randomuser.me/api/portraits/men/32.jpg",
                        "Thần kinh, đau đầu, đột quỵ", "Thứ ba, Thứ sáu"
                },
                "pediatrics", new String[]{
                        "ThS.", "Đặng Khánh An", "Khoa Nhi", "Pediatrics",
                        "https://randomuser.me/api/portraits/men/45.jpg",
                        "Nhi khoa, khám tổng quát trẻ em", "Thứ hai, Thứ bảy"
                },
                "orthopedics", new String[]{
                        "ThS BS.", "Nguyễn Hoàng Minh", "Khoa Xương khớp", "Orthopedics",
                        "https://randomuser.me/api/portraits/men/52.jpg",
                        "Xương khớp, chấn thương chỉnh hình", "Thứ tư, Thứ bảy"
                },
                "dermatology", new String[]{
                        "BS.", "Trần Ngọc Linh", "Khoa Da liễu", "Dermatology",
                        "https://randomuser.me/api/portraits/women/68.jpg",
                        "Da liễu, dị ứng da, thẩm mỹ da", "Thứ ba, Thứ năm"
                }
        );

        String[] doctor = data.get(slug);

        if (doctor == null) {
            return "redirect:/bac-si";
        }

        model.addAttribute("degree", doctor[0]);
        model.addAttribute("name", doctor[1]);
        model.addAttribute("department", doctor[2]);
        model.addAttribute("specialty", doctor[3]);
        model.addAttribute("image", doctor[4]);
        model.addAttribute("expertise", doctor[5]);
        model.addAttribute("schedule", doctor[6]);

        return "pages/ho_so_bac_si";
    }
}