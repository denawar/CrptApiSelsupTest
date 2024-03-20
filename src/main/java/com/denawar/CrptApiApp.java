package com.denawar;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class CrptApiApp {
    public static void main(String[] args) {

        CrptApi sender = new CrptApi(TimeUnit.SECONDS, 2);
        CrptApi.Document document = sender.new Document();

        Runnable task = () -> {
            for (int i = 0; i < 3; i++) {
                try {
                    String response = sender.sendDocument(document, "");
                    System.out.println("response: " + response);

                } catch (IOException | InterruptedException e) {
                    System.out.println("Problem with sending document:" + e);
                }
            }
        };
        new Thread(task).start();
        new Thread(task).start();

    }


}
