package com.rr.pho;

import org.apache.log4j.Logger;

import java.time.Duration;
import java.time.Instant;
import java.util.Scanner;

import static org.jocl.CL.CL_DEVICE_MAX_WORK_ITEM_SIZES;

public class MainApp {
    private static final Logger LOGGER = Logger.getLogger(MainApp.class);

    public static void main(String[] args) {
        String key;

        Instant start1 = Instant.now();
        try {
          //  key = new JOCLDeviceInfoApproach().getDeviceMaxWorkItemSizes(CL_DEVICE_MAX_WORK_ITEM_SIZES);
//            or
            key = new AparapiDeviceInfoApproach().getDeviceMaxWorkItemSizes();
            if (key.equals("-102"))
                throw new Exception();
        } catch (Exception e) {
            LOGGER.error("Smth gonna wrong!");
            return;
        }
        Instant end1 = Instant.now();

        Scanner scanner = new Scanner(System.in);
        LOGGER.debug("");
        LOGGER.info("Type some data: ");
        String textForEncryption = scanner.nextLine();
        LOGGER.debug("");

        Vernem vernem = new Vernem(key);

        Instant start2 = Instant.now();
        String encryptedText = vernem.encryptText(textForEncryption);
        LOGGER.info("Encrypted message: " + encryptedText);
        String decryptedText = vernem.decryptText(encryptedText);
        LOGGER.info("Decrypted message: " + decryptedText);

        if (!textForEncryption.equals(decryptedText)) {
            LOGGER.error("Encrypted and decrypted messages are not equal to each other!");
            return;
        }

        Instant end2 = Instant.now();

        LOGGER.debug("");
        LOGGER.info("Total execution time: " + (Duration.between(start1, end1).toMillis() + Duration.between(start2,
                end2).toMillis()) + " ms");
    }
}