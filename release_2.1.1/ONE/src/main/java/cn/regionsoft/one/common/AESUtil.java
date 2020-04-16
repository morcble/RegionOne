package cn.regionsoft.one.common;  
  
import java.security.Security;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import cn.regionsoft.one.core.CommonUtil;

public class AESUtil {
	static {
        Security.addProvider(new BouncyCastleProvider());
    }
    /**
     * 加密，输出Base64字符串密文
     *
     * @param data           明文
     * @param key            密钥
     * @param transformation 类型，格式为：加密算法/加密模式/填充方式，举例：AES/CBC/PKCS5Padding，
     *                       相关取值可以查看下列两个文档：
     *                       <ul>
     *                       <li><a href="https://docs.oracle.com/javase/8/docs/api">JavaSE 8 API</a>
     *                       中的 javax.crypto.Cipher</li>
     *                       <li><a href="https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#Cipher">
     *                       Standard Algorithm Name Documentation</a></li>
     *                       </ul>
     * @param iv             偏移量，ECB模式不需要，传null
     * @return 密文
     * @throws Exception 异常
     */
    public static String encrypt(String data, String key, String transformation, String iv) throws Exception {
    	byte[] zipedBytes = CommonUtil.gzip(data.getBytes(Constants.UTF8));
    	byte[] encriptedBytes = handle(zipedBytes, key, transformation, iv, true);
    	return Base64.byteArrayToBase64(encriptedBytes);
        //return byteToHex(encriptedBytes);
    }
 
    public static byte[] decrypt(String data, String key, String transformation, String iv) throws Exception {
    	//byte[] encriptedBytes = hexToByte(data);
    	byte[] encriptedBytes = Base64.base64ToByteArray(data);
    	byte[] zipedBytes = handle(encriptedBytes, key, transformation, iv, false);
        return CommonUtil.ungzip(zipedBytes);
    }
 
    /**
     * 加密，输出十六进制字符串密文
     *
     * @param data           明文
     * @param key            密钥
     * @param transformation 类型，格式为：加密算法/加密模式/填充方式，举例：AES/CBC/PKCS5Padding，
     *                       相关取值可以查看下列两个文档：
     *                       <ul>
     *                       <li><a href="https://docs.oracle.com/javase/8/docs/api">JavaSE 8 API</a>
     *                       中的 javax.crypto.Cipher</li>
     *                       <li><a href="https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#Cipher">
     *                       Standard Algorithm Name Documentation</a></li>
     *                       </ul>
     * @param iv             偏移量，ECB模式不需要，传null
     * @return 密文
     * @throws Exception 异常
     */
    public static String encryptHex(String data, String key, String transformation, String iv) throws Exception {
        return byteToHex(handle(data.getBytes(Constants.UTF8), key, transformation, iv, true));
    }
    
    public static String byteToHex(byte[] bytes){
        String strHex = "";
        StringBuilder sb = new StringBuilder("");
        for (int n = 0; n < bytes.length; n++) {
            strHex = Integer.toHexString(bytes[n] & 0xFF);
            sb.append((strHex.length() == 1) ? "0" + strHex : strHex); // 每个字节由两个字符表示，位数不够，高位补0
        }
        return sb.toString().trim();
    }
  	
  	public static byte[] hexToByte(String hex){
        int m = 0, n = 0;
        int byteLen = hex.length() / 2; // 每两个字符描述一个字节
        byte[] ret = new byte[byteLen];
        for (int i = 0; i < byteLen; i++) {
            m = i * 2 + 1;
            n = m + 1;
            int intVal = Integer.decode("0x" + hex.substring(i * 2, m) + hex.substring(m, n));
            ret[i] = Byte.valueOf((byte)intVal);
        }
        return ret;
    }
    
    /**
     * 解密，密文为十六进制字符串
     *
     * @param data           密文
     * @param key            密钥
     * @param transformation 类型，格式为：加密算法/加密模式/填充方式，举例：AES/CBC/PKCS5Padding，
     *                       相关取值可以查看下列两个文档：
     *                       <ul>
     *                       <li><a href="https://docs.oracle.com/javase/8/docs/api">JavaSE 8 API</a>
     *                       中的 javax.crypto.Cipher</li>
     *                       <li><a href="https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#Cipher">
     *                       Standard Algorithm Name Documentation</a></li>
     *                       </ul>
     * @param iv             偏移量，ECB模式不需要，传null
     * @return 明文
     * @throws Exception 异常
     */
    public static String decryptHex(String data, String key, String transformation, String iv) throws Exception {
        return new String(handle(hexToByte(data), key, transformation, iv, false), Constants.UTF8);
    }
 
    /**
     * 处理数据，加密或解密
     *
     * @param data           数据
     * @param key            密钥
     * @param transformation 类型，格式为：加密算法/加密模式/填充方式，举例：<i>AES/CBC/PKCS5Padding</i>。<br/>
     *                       相关取值可以查看下列两个文档：
     *                       <ul>
     *                       <li><a href="https://docs.oracle.com/javase/8/docs/api">JavaSE 8 API</a>
     *                       中的 javax.crypto.Cipher</li>
     *                       <li><a href="https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#Cipher">
     *                       Standard Algorithm Name Documentation</a></li>
     *                       </ul>
     * @param iv             偏移量，ECB模式不需要，传null
     * @param isEncrypt      如果是加密，则为true；如果为解密，则为false
     * @return 加密后或解密后的字节数组
     * @throws Exception 异常
     */
    private static byte[] handle(byte[] data, String key, String transformation, String iv,
                                 boolean isEncrypt) throws Exception {
        SecretKey secretKey = new SecretKeySpec(key.getBytes(Constants.UTF8), "AES"); // 构造密钥
        Cipher cipher = Cipher.getInstance(transformation);
        if (iv == null || iv.length() == 0) {
            cipher.init(isEncrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE, secretKey);
        } else {
            IvParameterSpec ips = new IvParameterSpec(iv.getBytes(Constants.UTF8));
            cipher.init(isEncrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE, secretKey, ips);
        }
        return cipher.doFinal(data);
    }
    
	public static byte[] handle(byte[] data, byte[] key, String transformation, byte[] iv, boolean isEncrypt)
			throws Exception {
		SecretKey secretKey = new SecretKeySpec(key, "AES"); // 构造密钥
		Cipher cipher = Cipher.getInstance(transformation);
		if (iv == null || iv.length == 0) {
			cipher.init(isEncrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE, secretKey);
		} else {
			IvParameterSpec ips = new IvParameterSpec(iv);
			cipher.init(isEncrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE, secretKey, ips);
		}
		return cipher.doFinal(data);
	}
 
    
    
    private static final String TRANSFORMATION = "AES/CBC/PKCS7Padding";
    public static String encriptCBC(String data,String key,String iv) throws Exception {
    	return encrypt(data, key, TRANSFORMATION, iv);
    }
    

    public static byte[] decriptCBC(String data,String key,String iv) throws Exception {
    	return decrypt(data, key, TRANSFORMATION, iv);
    }
    
    public static void main(String[] args) throws Exception {
    	String transformation = "AES/CBC/PKCS7Padding";
    	String key = "1234123412ABCDEF"; // 密钥长度16位、24位、32位
    	String iv = "1234123412ABCDEF"; // IV偏移量的长度必须为16位
    	String data = "asdasdasasdsdasddaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"; // 待加密的数据
    	 
    	// 加密结果：QXvBCdQDgTB446IOFukkPA==
    	String result1 = encrypt(data, key, transformation, iv);
    	 System.out.println(result1);
    	// 解密结果：blogDemo123
    	//String result2 = decrypt(result1, key, transformation, iv);
    	//System.out.println(result2);
    
	}
  
} 