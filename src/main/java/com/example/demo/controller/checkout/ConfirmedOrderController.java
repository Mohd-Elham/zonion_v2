package com.example.demo.controller.checkout;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("order")
public class ConfirmedOrderController {

    @GetMapping("confirmation")
    public String confirmation() {


        return "checkout/order_confirmation";
    }

}
