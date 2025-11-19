package com.sejong.userservice.application.common.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Component
@RequiredArgsConstructor
public class EmailContentBuilder {
    private final TemplateEngine templateEngine;

    public String buildVerificationHtml(String code) {
        Context context = new Context();
        context.setVariable("code", code);
        return templateEngine.process("email/verification", context);
    }
}
