package com.denawar;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CrptApi {
    private TimeUnit timeUnit;
    private int requestlimit;
    private final String URL = "https://ismp.crpt.ru/api/v3/lk/documents/create";

    public CrptApi(TimeUnit timeunit, int requestLimit) {
        this.timeUnit = timeunit;
        this.requestlimit = requestLimit;
    }

    public String SendDocument(Document document, String signature) throws IOException {
        String json = document.toJSON();
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(json, MediaType.get("application/json"));

        Request request = new Request.Builder()
                .url(URL)
                .post(body)
                .build();


        try (Response response = client.newCall(request).execute()) {
            return response.body() != null ? response.body().string() : "response body is empty";
        } catch (IOException e) {
            IOException exception = new IOException("problem with input/output during sending document");
            exception.initCause(e);
            throw exception;
        }
    }

    public class Document {
        private Participant description;
        private String docId;
        private String docStatus;
        private String docType;// = "LP_INTRODUCE_GOODS";
        private boolean importRequest;// = true;
        private String ownerInn;
        private String participantInn;
        private String producerInn;
        private LocalDate productionDate;// = LocalDate.of(2020, 01, 23)
        private String productionType;

        private List<Product> products;

        private LocalDate regDate;
        private String regNumber;

        /*
        {
        "description":{ "participantInn": "string" },
        "doc_id": "string",
        "doc_status": "string",
        "doc_type": "LP_INTRODUCE_GOODS",
        "importRequest": true,
        "owner_inn": "string",
        "participant_inn": "string",
        "producer_inn":"string",
        "production_date": "2020-01-23",
        "production_type": "string",
        "products": [
                        {
                            "certificate_document": "string",
                            "certificate_document_date": "2020-01-23",
                            "certificate_document_number": "string",
                            "owner_inn": "string",
                            "producer_inn": "string",
                            "production_date": "2020-01-23",
                            "tnved_code": "string",
                            "uit_code": "string",
                            "uitu_code": "string"
                        }
                    ],
        "reg_date": "2020-01-23",
        "reg_number": "string"
        }
         */

        public Document() {
            this.description = new Participant("string");
            this.docId = "string";
            this.docStatus = "string";
            this.docType = "LP_INTRODUCE_GOODS";
            this.importRequest = true;
            this.ownerInn = "string";
            this.participantInn = "string";
            this.producerInn = "string";
            this.productionDate = LocalDate.of(2020, 01, 23);
            this.productionType = "string";
            this.products = new ArrayList<>();
            products.add(new Product());
            this.regDate = LocalDate.of(2020, 01, 23);
            this.regNumber = "string";
        }

        public Document(Participant description,
                        String docId,
                        String docStatus,
                        String docType,
                        boolean importRequest,
                        String ownerInn,
                        String participantInn,
                        String producerInn,
                        LocalDate productionDate,
                        String productionType,
                        List<Product> products,
                        LocalDate regDate,
                        String regNumber) {
            this.description = description;
            this.docId = docId;
            this.docStatus = docStatus;
            this.docType = (docType == null || docType.isEmpty()) ? "LP_INTRODUCE_GOODS" : docType;
            this.importRequest = importRequest;
            this.ownerInn = ownerInn;
            this.participantInn = participantInn;
            this.producerInn = producerInn;
            this.productionDate = productionDate != null ? productionDate : LocalDate.of(2020, 01, 23);
            this.productionType = productionType;
            this.products = products;
            this.regDate = regDate;
            this.regNumber = regNumber;

        }

        public String toJSON() {
            ObjectMapper objectMapper = new ObjectMapper();
            String json = null;
            try {
                json = objectMapper.writeValueAsString(this);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            return json;
        }

        public class Participant {
            private String participantInn;

            public Participant(String participantInn) {
                this.participantInn = participantInn;
            }
        }

        public class Product {
            private String certificateDocument;
            private LocalDate certificateDocumentDate;// = LocalDate.of(2020, 01, 23);
            private String certificateDocumentNumber;
            private String ownerInn;
            private String producerInn;
            private LocalDate productionDate;// = LocalDate.of(2020, 01, 23);
            private String tnvedCode;
            private String uitCode;
            private String uituCode;

            public Product() {
                this.certificateDocument = "string";
                this.certificateDocumentDate = LocalDate.of(2020, 01, 23);
                this.certificateDocumentNumber = "string";
                this.ownerInn = "string";
                this.producerInn = "string";
                this.productionDate = LocalDate.of(2020, 01, 23);
                this.tnvedCode = "string";
                this.uitCode = "string";
                this.uituCode = "string";
            }

            public Product(String certificateDocument,
                           LocalDate certificateDocumentDate,
                           String certificateDocumentNumber,
                           String ownerInn,
                           String producerInn,
                           LocalDate productionDate,
                           String tnvedCode,
                           String uitCode,
                           String uituCode) {
                this.certificateDocument = certificateDocument;
                this.certificateDocumentDate = certificateDocumentDate != null ? certificateDocumentDate : LocalDate.of(2020, 01, 23);
                this.certificateDocumentNumber = certificateDocumentNumber;
                this.ownerInn = ownerInn;
                this.producerInn = producerInn;
                this.productionDate = productionDate != null ? productionDate : LocalDate.of(2020, 01, 23);
                this.tnvedCode = tnvedCode;
                this.uitCode = uitCode;
                this.uituCode = uituCode;
            }

        }


    }
}
