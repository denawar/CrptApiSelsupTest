package com.denawar;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class CrptApiApp {
    public static void main(String[] args) {

        CrptApi sender = new CrptApi(TimeUnit.MINUTES, 1);
        CrptApi.Document document = sender.new Document();
        try {
            String response = sender.SendDocument(document, "");
            System.out.println("response: "+response);
        } catch (IOException e) {
            System.out.println("Problem with sending document:" + e);
        }

    }


}
