package be.ephec.padel_backend.controller;

import be.ephec.padel_backend.model.Payment;
import be.ephec.padel_backend.service.PaymentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // Payer
    @PostMapping("/pay/{id}")
    public Payment pay(@PathVariable Integer id) {
        return paymentService.payer(id);
    }
}