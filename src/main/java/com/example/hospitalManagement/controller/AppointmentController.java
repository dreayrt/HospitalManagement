package com.example.hospitalManagement.controller;

import com.example.hospitalManagement.dto.AppointmentRequest;
import com.example.hospitalManagement.service.AppointmentServiceV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AppointmentController {

    @Autowired
    private AppointmentServiceV2 appointmentServiceV2;

    @PostMapping("/booking")
    public String createBooking(@ModelAttribute("appointmentRequest") AppointmentRequest appointmentRequest) {
        appointmentServiceV2.createAppointment(appointmentRequest);
        return "redirect:/booking-success";
    }

    @GetMapping("/booking-success")
    public String bookingSuccess() {
        return "pages/booking-success";
    }
}
