package com.example.app.service.mail;

import com.example.app.data.constant.AppErrorResponse;
import com.example.app.data.message.OrderMessage;
import com.example.app.data.response.order.OrderItemResponse;
import com.example.app.data.tables.pojos.User;
import io.reactivex.rxjava3.core.Single;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import vn.tnteco.common.core.exception.ApiException;

import java.util.Collection;

import static vn.tnteco.common.core.template.RxTemplate.rxSchedulerIo;

@Log4j2
@Service
@RequiredArgsConstructor
public class SendEmailServiceImpl implements ISendEmailService {

    private final JavaMailSender mailSender;

    private final TemplateEngine templateEngine;


    @Override
    public Single<Boolean> sendConfirmationEmail(String toEmail, String username, String confirmationCode) {
        return rxSchedulerIo(() -> {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("confirmationCode", confirmationCode);
            sendMailBlocking(toEmail, "[ReadZone] Xác nhận đăng ký tài khoản", context, "confirmation-email");
            return true;
        });
    }

    private void sendMailBlocking(String toEmail, String subject, Context context, String template) throws MessagingException {
        String htmlContent = templateEngine.process(template, context);
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        mailSender.send(message);
    }

    @Async
    @Override
    public void sendConfirmationEmailAsync(String toEmail, String fullName, String confirmationCode) {
        try {
            Context context = new Context();
            context.setVariable("username", fullName);
            context.setVariable("confirmationCode", confirmationCode);
            sendMailBlocking(toEmail, "[ReadZone] Xác nhận đăng ký tài khoản", context, "confirmation-email");
        } catch (MessagingException e) {
            throw new ApiException(AppErrorResponse.BUSINESS_ERROR);
        }
    }

    @Override
    public void sendChangeOrderStatusAsync(String toEmail, User user, OrderMessage order, Collection<OrderItemResponse> orderItems) {
        try {

            Context context = new Context();
            context.setVariable("order", order);
            context.setVariable("shippingAddress", order.getShippingAddress());
            context.setVariable("user", user);
            context.setVariable("orderItems", orderItems);
            sendMailBlocking(toEmail, "[ReadZone] Cập nhật trạng thái đơn hàng #" + order.getId(), context, "order-status-email");
        } catch (Exception e) {
            throw new ApiException(AppErrorResponse.BUSINESS_ERROR);
        }
    }
}
