package com.example.equiussd.ussdController;

import org.json.JSONObject;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import java.util.Scanner;

@RestController
public class UssdController {

    @PostMapping("/ussd")
    public String handleUssdRequest(@RequestBody String requestBody) {
        Map<String, String> body = Arrays
                .stream(requestBody.split("&"))
                .map(entry -> entry.split("="))
                .collect(Collectors.toMap(entry -> entry[0], entry -> entry.length == 2 ? entry[1] : ""));
        System.out.println("response body is:...."+ body);
        String sessionId = body.get("sessionId");
        String serviceCode = body.get("serviceCode");
        String phoneNumber = body.get("phoneNumber");
        String text = body.get("text");

        StringBuilder response = new StringBuilder("");

        if (text.isEmpty()) {
            response.append("CON Dear customer,kindly select your service:\n" +
                    "1. Register\n" +
                    "2. Buy policy\n" +
                    "3.Claims\n" +
                    "4.Contact Us\n" +
                    "5.Exit");
        } else if (text.equals("1")) {
            response.append("CON Enter your Details\n" +
                    "1.Details\n" +
                    "2.Exit"
            );
        } else  if (text.startsWith("1*1")) {
            String[] parts = text.split("\\*");
            int numberOfStars = parts.length - 1; // Subtract 1 because parts array includes an empty string at index 0
            if (numberOfStars == 1) {
                response.append("CON Enter First Name:");
            } else if (numberOfStars == 2) {
                response.append("CON Enter Last Name:");
            } else if (numberOfStars == 3) {
                response.append("CON Enter Email:");
            } else if (numberOfStars == 4) {
                response.append("CON Enter ID Number:");
            } else if (numberOfStars == 5) {
                response.append("CON Enter Password:");
            } else if (numberOfStars == 6) {
                response.append("CON Enter role:");
            } else if (numberOfStars == 7) {
                response.append("CON Enter phoneNo:");
            } else if (numberOfStars == 8) {
                JSONObject jsonObject = new JSONObject();
                String email = parts[4].replace("%40", "@");
                jsonObject.put("firstName", parts[2]);
                jsonObject.put("lastName", parts[3]);
                jsonObject.put("email", email);
                jsonObject.put("nationalId", parts[5]);
                jsonObject.put("password", parts[6]);
                jsonObject.put("role", parts[7]);
                jsonObject.put("phoneNo", parts[8]);
                System.out.println(jsonObject);

                // Prepare headers
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                // Prepare HTTP entity with JSON body and headers
                HttpEntity<String> requestEntity = new HttpEntity<>(jsonObject.toString(), headers);

                // Send POST request to the endpoint URL
                RestTemplate restTemplate = new RestTemplate();
                String endpointUrl = "http://52.15.152.26:8082/api/v1/auth/register";

                String responseFromEndpoint =  restTemplate.postForObject(endpointUrl, requestEntity, String.class);
                System.out.println(responseFromEndpoint);
                // Use the response from the endpoint as the USSD response
                response.append(responseFromEndpoint);
            }
        }
        else if (text.equals("2")) {
            response.append("CON 1.Third party \n 2.Domestic");
        } else if (text.equals("2*1")) {
            response.append("CON 1.Enter ID number\n 2.Exit");
        } else if (text.equals("2*1*1")) {
            response.append("CON 1.Enter car plate number\n 2.exit");
        } else if (text.equals("2*1*1*1")) {
            response.append("CON 1.Enter start date\n 2.Exit");
        } else if (text.equals("2*1*1*1*1")) {
            response.append("CON select mode of payment:\n 1.Mobile money\n 2.Pay from Account");
        } else if (text.equals("2*1*1*1*1*1")) {
            response.append("CON 1.Enter mobile number: \n 2.exit");
        } else if (text.equals("2*1*1*1*1*1*1")) {
            response.append("END Do you wish to sh.1000 ?\n 1.Accept\n 2.Decline");
        } else if (text.equals("2*1*1*1*1*2")) {
            response.append("CON Select accoun to pay from: \n 1.ACC1 \n 2.ACC 2");
        } else if (text.equals("2*1*1*1*1*2*1")) {
            response.append("END Confirm sh.1000 policy purchase:\n 1.Accept \n 2.Decline");
        } else if (text.equals("2*2")) {
            response.append("CON 1.Enter ID number\n 2.Exit");
        } else if (text.equals("2*1*1")) {
            response.append("CON 1.Enter car plate number\n 2.exit");
        } else if (text.equals("2*2*1*1")) {
            response.append("CON 1.Enter start date\n 2.Exit");
        } else if (text.equals("2*2*1*1*1")) {
            response.append("CON select mode of payment:\n 1.Mobile money\n 2.Pay from Account");
        } else if (text.equals("2*2*1*1*1*1")) {
            response.append("CON 1.Enter mobile number: \n 2.exit");
        } else if (text.equals("2*2*1*1*1*1*1")) {
            response.append("END Do you wish to sh.1000 ?\n 1.Accept\n 2.Decline");
        } else if (text.equals("2*2*1*1*1*2")) {
            response.append("CON Select account to pay from: \n 1.ACC1 \n 2.ACC 2");
        } else if (text.equals("2*2*1*1*1*2*1")) {
            response.append("END Confirm sh.1000 policy purchase:\n 1.Accept \n 2.Decline");
        } else if (text.equals("3")) {
            response.append("CON 1.Enter Incident date\n 2.Exit");
        } else if (text.equals("3*1")) {
            response.append("CON 1.Enter Incident details \n 2.Exit");
        } else if (text.equals("3*1*1")) {
            response.append("CON 1.Enter your Email Address\n 2.Exit");
        } else if (text.equals("3*1*1*1")) {
            response.append("CON kindly confirm claim submission\n 1.Accept\n 2.Decline");
        } else if (text.equals("4"))
            response.append("END Reach us at:\n Tel:+(254)763000000\n Email:info@equitybank.co.ke");

        return response.toString();
    }
}