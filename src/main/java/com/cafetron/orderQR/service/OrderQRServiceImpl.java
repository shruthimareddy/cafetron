package com.cafetron.orderQR.service;

import com.cafetron.order.Order;
import com.cafetron.orderQR.entity.OrderQR;
import com.cafetron.orderQR.exception.QRGenerationException;
import com.cafetron.orderQR.repository.OrderQRRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Slf4j
@Service
public class OrderQRServiceImpl implements OrderQRService {

    @Autowired
    private OrderQRRepository orderQRRepository;


    @Override
    public String generateAndStoreQR(Order order) {

        try {
            String token = UUID.randomUUID().toString();
            String qrData = encodeTokenToQR(token);

            log.info("Token successfully generated!\ntoken: {}\nbase64QR: {}", token, qrData);
            log.info("QR generated successfully for the given Order[id: {}]", order.getId());

            OrderQR orderQR = new OrderQR();

            orderQR.setOrder(order);
            orderQR.setQrData(qrData);
            orderQR.setCreatedAt(LocalDateTime.now());

            orderQRRepository.save(orderQR);
            log.info("Entry for OrderQR successfully created for Order[id: {}]", order.getId());

            return token;

        } catch (QRGenerationException e) {
            log.error("Failed to encode token to QR for Order[id: {}]. Exception: {}", order.getId(), e.getMessage());
        } catch (DataAccessException e) {
            log.error("Failed to save OrderQR to DB for Order[id: {}]. Exception: {}", order.getId(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected failure during QR generation for Order[id: {}]. Exception: {}", order.getId(), e.getMessage());
        }

        return null;
    }

     private String encodeTokenToQR( String token ) {

        try {
            BitMatrix matrix = new MultiFormatWriter().encode(token, BarcodeFormat.QR_CODE, 250, 250);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();

            MatrixToImageWriter.writeToStream(matrix, "PNG", stream);

            return Base64.getEncoder().encodeToString(stream.toByteArray());
        } catch (WriterException | IOException e) {
            throw new QRGenerationException(e.getMessage());
        }

    }

}
