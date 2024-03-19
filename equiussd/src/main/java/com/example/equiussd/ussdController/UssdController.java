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
    public JSONArray fetchMarketplaceData(String endpoint) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(endpoint, String.class);
            JSONObject jsonResponse = new JSONObject(response);
            return jsonResponse.getJSONArray("entity"); // Adjust the key according to your JSON structure
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
            response.append("CON Welcome to Equifarm:\n" +
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
                response.append("END you have  been registered successfully");
            }
        } else if (text.equals("2")) {
            // Display marketplace menu
            response.append("CON Welcome to the Marketplace. Select a category:\n");
            response.append("1. Buy\n");
            response.append("2. Sell\n");
            response.append("0. Back");
        } else if (text.equals("2*1")) {
            // Display Agrodealer menu
            response.append("CON Select Agrodealer category:\n");
            response.append("1. Farm Inputs\n");
            response.append("2. Farm Tools\n");
            response.append("0. Back");
        } else if (text.equals("2*1*1")) {
            // Display Farm Inputs menu
            response.append("CON Select Farm Inputs category:\n");
            response.append("1. Seeds\n");
            response.append("2. Fertilizers\n");
            response.append("3. Chemicals\n");
            response.append("4. Feeds\n");
            response.append("5. Seedlings\n");
            response.append("0. Back");
        } else if (text.equals("2*1*1*1")) {
            // Display Seeds menu
            response.append("CON Select Seeds category:\n");
            response.append("1. Cereals\n");
            response.append("2. Vegetables\n");
            response.append("3. Legumes\n");
            response.append("4. Fruits\n");
            response.append("0. Back");
        } else if (text.equals("2*1*1*1*1")) {
            // Display Cereals menu
            response.append("CON Select Cereals type:\n");
            response.append("1. Maize\n");
            response.append("2. Wheat\n");
            response.append("3. Rice\n");
            response.append("4. Sorghum\n");
            response.append("5. Oats\n");
            response.append("0. Back");
        } else if (text.equals("2*1*1*1*1*1")) {
            // Fetch data first
            JSONArray productsArray = fetchMarketplaceData("http://localhost:8082/api/v1/marketproducts/get/marketproduducts");
            if (productsArray != null && productsArray.length() > 0) {
                // Initialize response
                StringBuilder marketResponse = new StringBuilder("CON Available Products:\n");

                // Process fetched data and build response
                for (int i = 0; i < productsArray.length(); i++) {
                    JSONObject product = productsArray.getJSONObject(i);
                    marketResponse.append((i + 1) + ". ");
                    marketResponse.append(" ").append(product.getString("businessName")).append(" ");
                    marketResponse.append("   -: ").append(product.getString("description")).append("");
                    marketResponse.append(" Ksh ").append(product.getInt("pricePerUnit"));
                    marketResponse.append("/").append(product.getString("unit")).append(" ");
                }
                marketResponse.append("0. Back");

                // Print fetched products
                System.out.println("Fetched products: " + marketResponse.toString());

                // Return response
                return marketResponse.toString();
            } else {
                // No products available
                return "CON No products available.\n 0. Back";
            }
        } else {
            response.append("END Invalid input. Please try again.");
        }

        // Return the USSD response
        return response.toString();
    }
}
