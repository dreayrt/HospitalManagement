package com.example.hospitalManagement.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class HashUtil {
    public static String sha256(String password) {
        try{
            //MessageDigest là class Java chuyên hash dữ liệu. SHA-256 là thuật toán hash.
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            //digest chay thuat toan sha256, sau khi chuyển chuỗi password thành mảng byte theo chuẩn mã hóa UTF-8.
            byte [] hash=md.digest(password.getBytes(StandardCharsets.UTF_8));
            //String tao object moi khi append phan tu moi rat ton bo nho =>StringBuilder chỉ sửa trên cùng buffer.
            StringBuilder hex=new StringBuilder();
            for(byte b:hash){
                hex.append(String.format("%02x",b));
            }

            return hex.toString();

        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }
}
