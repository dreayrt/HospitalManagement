package com.example.hospitalManagement.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class SucKhoeController {

    @GetMapping("/suc-khoe/{slug}")
    public String chiTietSucKhoe(@PathVariable String slug, Model model) {

        if (slug.equals("benh-ly-tim-mach")) {
            model.addAttribute("title", "Bệnh lý tim mạch");
            model.addAttribute("content",
                    "Bệnh lý tim mạch là nhóm bệnh liên quan đến tim và mạch máu như tăng huyết áp, đau thắt ngực, suy tim. Người bệnh nên đi khám khi có triệu chứng đau ngực, khó thở, hồi hộp hoặc mệt nhiều.");
        }

        else if (slug.equals("benh-ly-ho-hap")) {
            model.addAttribute("title", "Bệnh lý hô hấp");
            model.addAttribute("content",
                    "Bệnh lý hô hấp liên quan đến phổi và đường thở như viêm phế quản, hen suyễn, viêm phổi.");
        }

        else if (slug.equals("benh-ly-than-kinh")) {
            model.addAttribute("title", "Bệnh lý thần kinh");
            model.addAttribute("content",
                    "Bệnh lý thần kinh có thể gây đau đầu, chóng mặt, tê yếu tay chân, rối loạn giấc ngủ hoặc mất thăng bằng.");
        }

        else {
            model.addAttribute("title", "Thông tin sức khỏe");
            model.addAttribute("content", "Nội dung đang được cập nhật.");
        }

        return "HealthDetails";
    }
}