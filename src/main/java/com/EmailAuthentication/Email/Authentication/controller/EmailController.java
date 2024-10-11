package com.EmailAuthentication.Email.Authentication.controller;

import com.EmailAuthentication.Email.Authentication.dto.EmailCheck;
import com.EmailAuthentication.Email.Authentication.dto.EmailRequest;
import com.EmailAuthentication.Email.Authentication.service.EmailSendService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class EmailController {
    private final EmailSendService emailSendService;

    @PostMapping("/mailSend")
    public String mailSend(@RequestBody @Valid EmailRequest emailRequest) {
        System.out.println("인증 이메일: " + emailRequest.getEmail());

        return emailSendService.joinEmail(emailRequest.getEmail());
    }

    @PostMapping("/mailCheck")
    public String AuthCheck(@RequestBody @Valid EmailCheck emailCheck) {
        Boolean checked = emailSendService.CheckAuthNum(emailCheck.getEmail(), emailCheck.getAuthNum());

        if (checked) return "ok";
        else throw new NullPointerException("잘못됨");
    }
}
