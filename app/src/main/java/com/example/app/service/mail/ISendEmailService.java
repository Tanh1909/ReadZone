package com.example.app.service.mail;

import com.example.app.data.message.OrderMessage;
import com.example.app.data.response.order.OrderItemResponse;
import com.example.app.data.tables.pojos.OrderItem;
import com.example.app.data.tables.pojos.User;
import io.reactivex.rxjava3.core.Single;

import java.util.Collection;

public interface ISendEmailService {

    Single<Boolean> sendConfirmationEmail(String toEmail, String username, String confirmationCode);


    void sendConfirmationEmailAsync(String toEmail, String fullName, String confirmationCode);

    void sendChangeOrderStatusAsync(String toEmail, User user, OrderMessage order, Collection<OrderItemResponse> orderItems);


}
