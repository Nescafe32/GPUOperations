package com.rr.pho;

import com.aparapi.Kernel;
import com.aparapi.Range;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class Vernam {
    @Getter @Setter
    private String key;

    /*
        commented blocks of code in methods will also give you correct result, but without using GPU
    */

    private void prepareKey(String textForEncryption) {
//        StringBuilder keySb = new StringBuilder();
//        for (int i = 0; i < textForEncryption.length(); ++i)
//            keySb.append(key.charAt(i % key.length()));
//        this.key = keySb.toString();
        char[] keyArr = key.toCharArray();
        char[] newKeyArr = new char[textForEncryption.length()];
        Kernel kernel = new Kernel() {
            @Override
            public void run() {
                int gid = getGlobalId();
                newKeyArr[gid] = keyArr[gid % keyArr.length];
            }
        };

        kernel.setExecutionModeWithoutFallback(Kernel.EXECUTION_MODE.GPU);
        kernel.execute(Range.create(textForEncryption.length()));

        this.key = new String(newKeyArr);
    }

    public String encryptText(String textForEncryption) {
//        prepareKey(textForEncryption);
//        StringBuilder encryptedTextSb = new StringBuilder();
//
//        for (int i = 0; i < textForEncryption.length(); i++)
//            encryptedTextSb.append((char) (textForEncryption.charAt(i) ^ key.charAt(i)));
//        return encryptedTextSb.toString();
        prepareKey(textForEncryption);

        char[] keyArr = key.toCharArray();
        char[] textForEncryptionArr = textForEncryption.toCharArray();
        char[] encryptedText = new char[textForEncryption.length()];
        Kernel kernel = new Kernel() {
            @Override
            public void run() {
                int gid = getGlobalId();
                encryptedText[gid] = (char) (textForEncryptionArr[gid] ^ keyArr[gid]);
            }
        };
        kernel.setExecutionModeWithoutFallback(Kernel.EXECUTION_MODE.GPU);
        kernel.execute(Range.create(textForEncryption.length()));

        return new String(encryptedText);
    }

    public String decryptText(String encryptedText) {
//        StringBuilder decryptedTextSb = new StringBuilder();
//
//        for (int i = 0; i < encryptedText.length(); i++)
//            decryptedTextSb.append((char) (encryptedText.charAt(i) ^ key.charAt(i)));
//        return decryptedTextSb.toString();
        char[] keyArr = key.toCharArray();
        char[] encryptedTextArr = encryptedText.toCharArray();
        char[] decryptedText = new char[encryptedText.length()];
        Kernel kernel = new Kernel() {
            @Override
            public void run() {
                int gid = getGlobalId();
                decryptedText[gid] = (char) (encryptedTextArr[gid] ^ keyArr[gid]);
            }
        };
        kernel.setExecutionModeWithoutFallback(Kernel.EXECUTION_MODE.GPU);
        kernel.execute(Range.create(encryptedText.length()));
        //System.out.println(kernel.getExecutionMode());

        return new String(decryptedText);
    }
}
