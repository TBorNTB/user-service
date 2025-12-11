package com.sejong.userservice.client.email;

import static com.sejong.userservice.support.common.exception.ExceptionType.EMAIL_TRANSFER_ERROR;
import static com.sejong.userservice.support.common.exception.ExceptionType.UNEXPECTED_ERROR;
import static com.sejong.userservice.support.common.exception.ExceptionType.UNSUPPORTED_EMAIL_ADDRESS;

import com.sejong.userservice.support.common.exception.BaseException;
import com.sejong.userservice.support.common.util.EmailContentBuilder;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailSenderImpl implements EmailSender {

    private final JavaMailSender mailSender;
    private final EmailContentBuilder emailContentBuilder;


    @Override
    @Async
    @Retryable(
            value = { MessagingException.class, UnsupportedEncodingException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public String send(String to, String code) {
        try {
            log.info("Sending email to " + to);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setTo(to);
            helper.setFrom("ssg-info-hub@noreply.com", "SSG Info Hub");
            helper.setSubject("[비밀번호 재설정] 이메일 인증을 진행해주세요.");
            helper.setText(emailContentBuilder.buildVerificationHtml(code) , true);

            mailSender.send(message);
            log.info("Email sent");
            return to;
        }catch (MessagingException e) {
            log.error("MessagingException occurred while sending email to {}: {}", to, e.getMessage(), e);
            throw new BaseException(EMAIL_TRANSFER_ERROR);
        } catch (UnsupportedEncodingException e) {
            log.error("UnsupportedEncodingException occurred while setting sender address: {}", e.getMessage(), e);
            throw new BaseException(UNSUPPORTED_EMAIL_ADDRESS);
        } catch (Exception e) {
            log.error("Unexpected error occurred while sending email to {}: {}", to, e.getMessage(), e);
            throw new BaseException(UNEXPECTED_ERROR);
        }
    }
}
