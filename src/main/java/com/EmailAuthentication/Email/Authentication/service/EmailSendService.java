package com.EmailAuthentication.Email.Authentication.service;

import com.EmailAuthentication.Email.Authentication.util.RedisUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Struct;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class EmailSendService {
    private JavaMailSender mailSender;
    private RedisUtil redisUtil;
    private static final long AUTH_NUM_EXPIRE_TIME = 60L;

    // 임의의 6자리 양수를 반환
    public int makeRandomNum() {
        Random num = new Random();
        StringBuilder randomNum = new StringBuilder();

        for (int i = 0; i < 6; i++) {
            randomNum.append(num.nextInt(10));
        }

        return Integer.parseInt(randomNum.toString());
    }

    // 이메일 인증 과정을 시작하는 메서드
    // makeRandomNum을 호출하여 해당 숫자를 포함한 이메일 내용을 작성하고 전송
    public String joinEmail(String email) {
        int authNum = makeRandomNum();
        String setFrom = "nextconnect.lab@gmail.com";
        String toMail = email;
        String title = "Genshin Flow 인증코드";
        String content = readHtmlTemplate().replace("{{authNum}}", Integer.toString(authNum));

        mailSend(setFrom, toMail, title, content, authNum);
        return Integer.toString(authNum);
    }

    // HTML 템플릿 파일을 읽어오는 메서드
    private String readHtmlTemplate() {
        try {
            ClassPathResource resource = new ClassPathResource("EmailSend.html");
            return new String(Files.readAllBytes(Paths.get(resource.getURI())), StandardCharsets.UTF_8);
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to read HTML template", e);
        }
    }

    // 이메일을 실제로 전송하는 메서드
    // MimeMessage 객체를 생성하고, MimeMessageHelper를 사용해 발신자, 수신자, 제목, 본문을 설정함
    public void mailSend(String setFrom, String toMail, String title, String content, int authNum) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            // 이메일 메시지와 관련된 설정을 수행함
            // true를 전달하여 multipart 형식의 메시지를 지원함
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");
            helper.setFrom(setFrom);
            helper.setTo(toMail);
            helper.setSubject(title);
            helper.setText(content, true); // true를 설정하여 html 설정으로 함

            mailSender.send(message);
        }
        catch (MessagingException e) {
            throw new RuntimeException("Failed to wend email", e);
        }

        // 인증번호는 1분동안 유효함
        redisUtil.setDataExpire(Integer.toString(authNum), toMail, AUTH_NUM_EXPIRE_TIME);
    }

    // 사용자가 입력한 인증번호를 검증하는 메서드
    // Redis에서 저장된 인증 번호를 가져와, 입력한 인증 번호와 비교한다.
    public boolean CheckAuthNum(String email, String authNum) {
        String storedEmail = redisUtil.getData(authNum);

        if(storedEmail != null && storedEmail.equals(email)) return true;
        else return false;
    }
}
