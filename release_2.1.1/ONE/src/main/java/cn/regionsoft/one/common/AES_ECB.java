package cn.regionsoft.one.common;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AES_ECB {
    public static byte[] Encrypt(byte[] src, byte[] key) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");//"算法/模式/补码方式"
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        return cipher.doFinal(src);
    }
    
    public static byte[] Decrypt(byte[] encrypted, byte[] key) throws Exception {
        try {
            SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            try {
            	return cipher.doFinal(encrypted);
            } catch (Exception e) {
               e.printStackTrace();
            }
        } catch (Exception ex) {
        	ex.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) throws Exception {
        String key = "1234567890123456";
        String src = "fdgdfgdfg";
        System.out.println(src);
        byte[] encrypted = AES_ECB.Encrypt(src.getBytes(), key.getBytes("utf-8"));
        String DeString = new String(AES_ECB.Decrypt(encrypted, key.getBytes("utf-8")));
        System.out.println("解密后的字串是：" + DeString);
    }
}

