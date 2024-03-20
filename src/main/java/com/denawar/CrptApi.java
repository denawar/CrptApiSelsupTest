package com.denawar;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import okhttp3.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CrptApi {
    private final TimeUnit TIME_UNIT;
    private final int REQUESTS_LIMIT;
    private final String URL = "https://ismp.crpt.ru/api/v3/lk/documents/create";

    private LocalDateTime startTime;
    private int requestsLeft;

    private Lock requestsLock;
    private Condition requestsAreInStock;

    public CrptApi(TimeUnit timeunit, int requestLimit) {
        this.TIME_UNIT = timeunit;
        this.REQUESTS_LIMIT = requestLimit;
        this.startTime = null;
        this.requestsLeft = 0;
        requestsLock = new ReentrantLock();
        requestsAreInStock = requestsLock.newCondition();
    }

    public String SendDocument(Document document, String signature) throws IOException, InterruptedException {
        if (startTime == null) {
            startTime = LocalDateTime.now();
            requestsLeft = REQUESTS_LIMIT;
        }
        String result="";
        requestsLock.lock();
        try {
            while (requestsLeft > 0) {
                requestsAreInStock.await();
                String json = document.toJSON();
                OkHttpClient client = new OkHttpClient();
                RequestBody body = RequestBody.create(json, MediaType.get("application/json"));

                Request request = new Request.Builder()
                        .url(URL)
                        .post(body)
                        .build();


                try (Response response = client.newCall(request).execute()) {

                    long diffMillis = ChronoUnit.MILLIS.between(startTime, LocalDateTime.now());
                    long diffTimeUnits = TIME_UNIT.convert(diffMillis, TimeUnit.MILLISECONDS);
                    if (diffTimeUnits > 0) {
                        startTime = LocalDateTime.now();
                        requestsLeft = REQUESTS_LIMIT;
                    } else {
                        requestsLeft--;
                    }
                    requestsAreInStock.signalAll();
                    result =  response.body() != null ? response.body().string() : "";
                } catch (IOException e) {
                    IOException exception = new IOException("problem with input/output during sending document");
                    exception.initCause(e);
                    throw exception;
                }

            }
            return result;
        } catch (InterruptedException e) {
            InterruptedException exception = new InterruptedException("thread was interrupted during sending document");
            exception.initCause(e);
            throw exception;
        } finally {
            requestsLock.unlock();
        }
    }

    public class Document {
        private Participant description;
        private String docId;
        private String docStatus;
        private String docType;
        private boolean importRequest;
        private String ownerInn;
        private String participantInn;
        private String producerInn;
        private LocalDate productionDate;
        private String productionType;

        private List<Product> products;

        private LocalDate regDate;
        private String regNumber;


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
            SimpleModule module = new SimpleModule();
            module.addSerializer(CrptApi.Document.class, new DocumentSerializer());
            module.addSerializer(CrptApi.Product.class, new ProductSerializer());
            objectMapper.registerModule(module);

            String json = null;
            try {
                json = objectMapper.writeValueAsString(this);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            return json;
        }

        public class DocumentSerializer extends JsonSerializer<Document> {
            @Override
            public void serialize(Document document, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
                jsonGenerator.writeStartObject();
                jsonGenerator.writeObjectField("description", document.description);
                jsonGenerator.writeStringField("doc_id", document.docId);
                jsonGenerator.writeStringField("doc_status", document.docStatus);
                jsonGenerator.writeStringField("doc_type", document.docType);
                jsonGenerator.writeBooleanField("importRequest", document.importRequest);
                jsonGenerator.writeStringField("owner_inn", document.ownerInn);
                jsonGenerator.writeStringField("participant_inn", document.participantInn);
                jsonGenerator.writeStringField("producer_inn", document.producerInn);
                jsonGenerator.writeStringField("production_date", document.productionDate.toString());
                jsonGenerator.writeStringField("production_type", document.productionType);
                jsonGenerator.writeArrayFieldStart("products");
                for (Product product : document.products) {
                    jsonGenerator.writeObject(product);
                }
                jsonGenerator.writeEndArray();
                jsonGenerator.writeStringField("reg_date", document.regDate.toString());
                jsonGenerator.writeStringField("reg_number", document.regNumber);
                jsonGenerator.writeEndObject();
            }
        }
    }

    public class Participant {
        private String participantInn;

        public Participant(String participantInn) {
            this.participantInn = participantInn;
        }

        public String getParticipantInn() {
            return participantInn;
        }
    }


    public class Product {
        private String certificateDocument;
        private LocalDate certificateDocumentDate;
        private String certificateDocumentNumber;
        private String ownerInn;
        private String producerInn;
        private LocalDate productionDate;
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

    public class ProductSerializer extends JsonSerializer<Product> {
        @Override
        public void serialize(Product product, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField("certificate_document", product.certificateDocument);
            jsonGenerator.writeStringField("ertificate_document_date", product.certificateDocumentDate.toString());
            jsonGenerator.writeStringField("certificate_document_number", product.certificateDocumentNumber);
            jsonGenerator.writeStringField("owner_inn", product.ownerInn);
            jsonGenerator.writeStringField("producer_inn", product.producerInn);
            jsonGenerator.writeStringField("production_date", product.productionDate.toString());
            jsonGenerator.writeStringField("tnved_code", product.tnvedCode);
            jsonGenerator.writeStringField("uit_code", product.uitCode);
            jsonGenerator.writeStringField("uitu_code", product.uituCode);
            jsonGenerator.writeEndObject();
        }
    }


}
