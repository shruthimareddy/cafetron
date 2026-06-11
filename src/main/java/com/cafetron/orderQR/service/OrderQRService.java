package com.cafetron.orderQR.service;

import com.cafetron.order.Order;

public interface OrderQRService {

    String generateAndStoreQR(Order order);

}
