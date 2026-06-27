package com.sanda.truckdoc.client.integration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanda.truckdoc.client.api.SynchronizeRequest;
import com.sanda.truckdoc.client.api.v2.SynchronizeResponseNew;
import com.sanda.truckdoc.client.api.v3.configuration.api.RegisterRequest;
import com.sanda.truckdoc.client.api.v3.configuration.api.RegisterResponse;
import com.sanda.truckdoc.client.api.v3.configuration.model.app.AppInfo;
import com.sanda.truckdoc.client.api.v3.configuration.model.device.DeviceInfo;
import com.sanda.truckdoc.client.api.v3.configuration.model.sim.SimInfo;
import com.sanda.truckdoc.network.AuthorizedBackend;
import com.sanda.truckdoc.network.Backend;
import com.sanda.truckdoc.network.api.ProgressRequestBody;
import com.sanda.truckdoc.network.api.UserKey;
import com.sanda.truckdoc.network.api.interceptors.AuthorizationInterceptor;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.EasyCallAdapterFactory;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class ApiIntegrationTest {

    private static final String BASE_URL = "https://mobile-api.truckdoc.ru/mobile-api/";
    private static final int PROTOCOL_VERSION = 2;

    public static void main(String[] args) {
        try {
            runTest();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void runTest() throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter Registration Code:");
        String registrationCode = scanner.nextLine().trim();

        // 1. Setup Retrofit
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(s -> System.out.println("API: " + s));
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC); // Changed to BASIC to reduce noise, use BODY for full details

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(JacksonConverterFactory.create(mapper))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addCallAdapterFactory(EasyCallAdapterFactory.create())
                .build();

        Backend backend = retrofit.create(Backend.class);

        // 2. Register
        System.out.println("\n--- 2. Register ---");
        RegisterRequest registerRequest = createRegisterRequest(registrationCode);
        Response<RegisterResponse> regResponse = backend.registerV3(registerRequest).executeUnchecked();

        if (!regResponse.isSuccessful()) {
            System.err.println("Registration failed: " + regResponse.code() + " " + regResponse.message());
            if (regResponse.errorBody() != null) {
                System.err.println(regResponse.errorBody().string());
            }
            return;
        }

        RegisterResponse regData = regResponse.body();
        if (regData == null) {
            System.err.println("Registration response body is null");
            return;
        }

        System.out.println("Registration successful! User: " + regData.getName());
        UserKey userKey = new UserKey(regData.getName(), regData.getLoginKey(), regData.getSecretKey());

        // Setup Authorized Backend
        OkHttpClient authClient = client.newBuilder()
                .addInterceptor(new AuthorizationInterceptor(userKey))
                .build();

        Retrofit authRetrofit = retrofit.newBuilder()
                .client(authClient)
                .build();

        AuthorizedBackend authorizedBackend = authRetrofit.create(AuthorizedBackend.class);

        // 3. Sync
        System.out.println("\n--- 3. Sync ---");
        SynchronizeRequest syncRequest = new SynchronizeRequest();
        syncRequest.setCurrentClientTime(new Date());
        syncRequest.setDataToGet(Collections.singletonList(SynchronizeRequest.GET_NEW_MESSAGES));
        
        Response<SynchronizeResponseNew> syncResp = authorizedBackend.synchronize(syncRequest).executeUnchecked();
        
        if (syncResp.isSuccessful()) {
            System.out.println("Sync successful!");
            // System.out.println("Messages: " + syncResp.body());
        } else {
            System.err.println("Sync failed: " + syncResp.code());
             if (syncResp.errorBody() != null) {
                System.err.println(syncResp.errorBody().string());
            }
        }

        // 4. Send Message (Text)
        System.out.println("\n--- 4. Send Message (Text) ---");
        System.out.println("Enter Recipient ID (long):");
        long recipientId = 0;
        try {
            recipientId = Long.parseLong(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID, skipping message send.");
        }

        if (recipientId > 0) {
            String messageText = "Integration Test Message " + new Date();
            String recipientIdType = "USER"; // Assuming USER, could be TRUCK or GROUP

            Response<Void> msgResponse = authorizedBackend.sendMessage(messageText, recipientIdType, PROTOCOL_VERSION, recipientId).executeUnchecked();
            if (msgResponse.isSuccessful()) {
                System.out.println("Message sent successfully!");
            } else {
                System.err.println("Message send failed: " + msgResponse.code());
            }
        }

        // 5. Upload File
        System.out.println("\n--- 5. Upload File ---");
        File tempFile = File.createTempFile("test_upload", ".txt");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("This is a test file content for integration test.");
        }
        
        ProgressRequestBody requestBody = new ProgressRequestBody(tempFile, percentage -> {});
        
        // uploadImage Observable
        Long serverFileId = null;
        try {
            Response<Long> uploadResponse = authorizedBackend.uploadImage(
                    requestBody,
                    tempFile.getName(),
                    "DOC", // fileType
                    "OTHER", // docType/metadata
                    null, // conversionType
                    1, // convertedByClient
                    "MESSAGE" // designationType
            ).toBlocking().first(); // Blocking get

            if (uploadResponse.isSuccessful() && uploadResponse.body() != null) {
                serverFileId = uploadResponse.body();
                System.out.println("File uploaded successfully! Server ID: " + serverFileId);
            } else {
                System.err.println("File upload failed: " + uploadResponse.code());
            }
        } catch (Exception e) {
            System.err.println("File upload exception: " + e.getMessage());
        }

        // 6. Send Message with File
        if (recipientId > 0 && serverFileId != null) {
            System.out.println("\n--- 6. Send Message with File ---");
            String recipientIdType = "USER";
            List<Long> fileIds = new ArrayList<>();
            fileIds.add(serverFileId);

            try {
                Response<Void> msgFileResponse = authorizedBackend.sendMessage(
                        recipientId,
                        recipientIdType,
                        PROTOCOL_VERSION,
                        fileIds
                ).toBlocking().first();

                if (msgFileResponse.isSuccessful()) {
                    System.out.println("Message with file sent successfully!");
                } else {
                    System.err.println("Message with file send failed: " + msgFileResponse.code());
                }
            } catch (Exception e) {
                System.err.println("Message with file exception: " + e.getMessage());
            }
        }

        // Cleanup
        tempFile.delete();
        System.out.println("\nTest Finished.");
    }

    private static RegisterRequest createRegisterRequest(String token) {
        RegisterRequest request = new RegisterRequest();
        request.setRegistrationToken(token);
        request.setGeneratedName("TestConsoleClient_" + System.currentTimeMillis());
        
        SimInfo simInfo = new SimInfo();
        simInfo.setPhoneNumber("0000000000");
        simInfo.setSimSerialNumber("0000000000");
        request.setSimInfo(simInfo);

        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setAndroidVersion("10");
        deviceInfo.setPhoneModel("IntegrationTest");
        deviceInfo.setPhoneManufacturer("JavaConsole");
        deviceInfo.setDeviceId("test-device-" + System.currentTimeMillis());
        request.setDeviceInfo(deviceInfo);

        AppInfo appInfo = new AppInfo();
        appInfo.setAppVersion("IntegrationTest");
        appInfo.setAppId("com.sanda.truckdoc.client.integration");
        appInfo.setAppPackage("com.sanda.truckdoc.client");
        request.setAppInfo(appInfo);

        return request;
    }
}
