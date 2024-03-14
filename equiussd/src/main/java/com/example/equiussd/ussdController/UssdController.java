package com.example.equiussd.ussdController;

import org.json.JSONArray;
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

@RestController
public class UssdController {

    // Function to fetch data from the endpoint
    public JSONObject fetchData(String endpoint) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(endpoint, String.class);
            return new JSONObject(response);
        } catch (Exception e) {
            // Print stack trace for debugging purposes
            e.printStackTrace();
            return null;
        }
    }

    @PostMapping("/ussd")
    public String handleUssdRequest(@RequestBody String requestBody) {
        Map<String, String> body = Arrays
                .stream(requestBody.split("&"))
                .map(entry -> entry.split("="))
                .collect(Collectors.toMap(entry -> entry[0], entry -> entry.length == 2 ? entry[1] : ""));
        System.out.println("response body is:...." + body);
        String text = body.get("text");

        StringBuilder response = new StringBuilder("");

        if (text.isEmpty()) {
            response.append("CON Dear customer, kindly select your service:\n" +
                    "1. Register\n" +
                    "2. Marketplace\n" +
                    "3. Loans \n" +
                    "4. Services\n" +
                    "5. Exit");
        } else if (text.equals("1")) {
            response.append("CON Enter your Details\n" +
                    "1. Details\n" +
                    "2. Exit"
            );
        } else if (text.startsWith("1*1")) {
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

                String responseFromEndpoint = restTemplate.postForObject(endpointUrl, requestEntity, String.class);
                System.out.println(responseFromEndpoint);
                // Use the response from the endpoint as the USSD response
                response.append("CON you have  been registered successfully");
            }
        } else if (text.equals("2")) {
            JSONObject marketplaceData = fetchData("http://52.15.152.26:8082/api/v1/typeOfProducts/get/all");
            if (marketplaceData != null) {
                JSONArray productTypesArray = getProductTypesArray(marketplaceData);
                if (productTypesArray != null && productTypesArray.length() > 0) {
                    response.append("CON Welcome to the Marketplace. Select farm product type:\n");
                    for (int i = 0; i < productTypesArray.length(); i++) {
                        response.append((i + 1) + ". ").append(productTypesArray.getString(i)).append("\n");
                    }
                    response.append("0. Back");
                } else {
                    response.append("CON No farm product types available.\n 1. Exit");
                }
            } else {
                response.append("CON Unable to fetch marketplace data.\n 1. Exit");
            }
        } else if (text.equals("3")) {
            // Handle claims logic
        } else if (text.equals("4")) {
            // Handle contact logic
        } else if (text.equals("5")) {
            // Handle exit logic
        }

        return response.toString();
    }

    // Method to retrieve product types from the marketplace data
    private JSONArray getProductTypesArray(JSONObject marketplaceData) {
        // Check if marketplaceData is not null and contains the "Farm Products" key
        if (marketplaceData != null && marketplaceData.has("Farm Products")) {
            // Get the JSONArray associated with the key "Farm Products"
            JSONArray farmProductsArray = marketplaceData.getJSONArray("Farm Products");

            // Check if the farmProductsArray is not null and not empty
            if (farmProductsArray != null && farmProductsArray.length() > 0) {
                // Create a new JSONArray to store the product types
                JSONArray productTypesArray = new JSONArray();

                // Iterate through the farmProductsArray and extract product types
                for (int i = 0; i < farmProductsArray.length(); i++) {
                    // Get the JSONObject at index i
                    JSONObject productObject = farmProductsArray.getJSONObject(i);

                    // Check if the productObject contains the "product_type" key
                    if (productObject.has("product_type")) {
                        // Get the value associated with the key "product_type" and add it to the productTypesArray
                        productTypesArray.put(productObject.getString("product_type"));
                    }
                }

                // Return the productTypesArray containing the extracted product types
                return productTypesArray;
            }
        }

        // If marketplaceData is null or does not contain "Farm Products" key, return null
        return null;
    }
}
