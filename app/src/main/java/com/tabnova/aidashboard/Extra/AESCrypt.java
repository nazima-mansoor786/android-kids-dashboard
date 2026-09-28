package com.tabnova.aidashboard.Extra;

import android.content.Context;
import android.util.Base64;

import com.tabnova.aidashboard.CustomDashboardApplication;

import java.nio.charset.StandardCharsets;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AESCrypt {

    private static final String ALGORITHM = "AES";
    private static final String KEY = "1Hbfh667adfDEJ78";


    public static void encrypt(Context context, String value) throws Exception
    {
        Key key = generateKey();
        Cipher cipher = Cipher.getInstance(AESCrypt.ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte [] encryptedByteValue = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
        String encryptedValue64 = Base64.encodeToString(encryptedByteValue, Base64.DEFAULT);
        CustomDashboardApplication.setString(context, Consts.PWD_ENCRY, encryptedValue64);
        CustomDashboardApplication.setInt(context, Consts.IS_PWD, 1);
    }

    public static String decrypt(Context context, String value) throws Exception
    {
        Key key = generateKey();
        Cipher cipher = Cipher.getInstance(AESCrypt.ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decryptedValue64 = Base64.decode(value, Base64.DEFAULT);
        byte [] decryptedByteValue = cipher.doFinal(decryptedValue64);
        return new String(decryptedByteValue, StandardCharsets.UTF_8);

    }

    private static Key generateKey() {
        return new SecretKeySpec(AESCrypt.KEY.getBytes(), AESCrypt.ALGORITHM);
    }
}
