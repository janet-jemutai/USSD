
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
                    "5. Insurance\n" +
                    "6. My Account\n" +
                    "7. Exit");
        } else if (text.equals("7")) {
            response.append("END Thank you for using Equifarm services. Goodbye!");
        } else if (text.equals("1")) {
            response.append("CON Enter your Details\n" +
                    "1. Details\n" +
                    "2. Exit"
            );
        } else if (text.equals("1*2")) {
            response.append("END You have chosen to exit. Goodbye!");
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
        }
        if (text.equals("2")) {
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
                return "CON No products available.\n0. Back";
            }
        }
        if (text.equals("3")) {
            response.append("CON Loan Options:\n");
            response.append("1. Check loan limit\n");
            response.append("2. Borrow\n");
            response.append("3. Pay loan\n");
        } else if (text.startsWith("3*1")) {
            // Handling loan limit check
            if (text.equals("3*1")) {
                response.append("CON Your current loan limit is Ksh 10,000.\n");
                response.append("2. Go back\n");
            } else if (text.equals("3*1*2")) {
                // User chose to go back from checking loan limit
                response.append("CON Loan Options:\n");
                response.append("1. Check loan limit\n");
                response.append("2. Borrow\n");
                response.append("3. Pay loan\n");
            }
        } else if (text.startsWith("3*2")) {
            // Handling loan borrowing
            if (text.equals("3*2")) {
                response.append("CON Enter loan amount:\n");
            } else {
                // Process user input for loan amount
                String[] parts = text.split("\\*");
                double loanAmount = Double.parseDouble(parts[2]); // Assuming amount is at index 2
                // Process the loan request
                // Add logic here to handle the loan request
                response.append("END Loan request submitted. You will receive a confirmation shortly.\n");
            }
        } else if (text.startsWith("3*3")) {
            // Handling loan payment
            if (text.equals("3*3")) {
                response.append("CON Enter amount to pay:\n");
            } else {
                // Process user input for loan payment amount
                String[] parts = text.split("\\*");
                double paymentAmount = Double.parseDouble(parts[2]); // Assuming amount is at index 2
                // Process the loan payment
                // Add logic here to handle the loan payment
                response.append("END Loan payment successful. Thank you.\n");
            }
        }
        if (text.equals("4")) {
            // User selected option 4, direct to services menu
            response.append("CON EquiFarm Services :\n");
            response.append("1. Agronomic Services\n");
            response.append("2. Soil Testing\n");
            response.append("3. Veterinary Services\n");
        } else if (text.startsWith("4*1")) {
            // Handling Agronomic services
            if (text.equals("4*1")) {
                response.append("CON Agronomic Services:\n");
                response.append("1. Crop consulting and advisory services\n");
                response.append("2. Fertilizer recommendation and application guidance\n");
                response.append("3. Pest and disease management recommendations\n");
            } else if (text.equals("4*1*1")) {
                // Process Crop consulting and advisory services
                response.append("CON Crop consulting and advisory services selected. Thank you!\n");
            } else if (text.equals("4*1*2")) {
                // Process Soil fertility management planning
                response.append("CON Fertilizer recommendation and application guidance selected. Thank you!\n");
            } else if (text.equals("4*1*3")) {
                // Process Pest and disease management recommendations
                response.append("CON Pest and disease management recommendations selected. Thank you!\n");
            }
        } else if (text.startsWith("4*2")) {
            // Handling Soil Testing
            if (text.equals("4*2")) {
                response.append("CON Soil Testing:\n");
                response.append("1. Soil nutrient analysis and recommendations\n");
                response.append("2. Soil pH testing and adjustment recommendations\n");
                response.append("3. Soil texture analysis and soil amendment suggestions\n");
            } else if (text.equals("4*2*1")) {
                // Process Basic soil nutrient analysis
                response.append("CON Soil nutrient analysis and recommendations selected. Thank you!\n");
            } else if (text.equals("4*2*2")) {
                // Process Soil pH testing and adjustment recommendations
                response.append("CON Soil pH testing and adjustment recommendations selected. Thank you!\n");
            } else if (text.equals("4*2*3")) {
                // Process Soil texture analysis and soil amendment suggestions
                response.append("CON Soil texture analysis and soil amendment suggestions selected. Thank you!\n");
            }
        } else if (text.startsWith("4*3")) {
            // Handling Veterinary services
            if (text.equals("4*3")) {
                response.append("CON Veterinary Services:\n");
                response.append("1. Routine health check-ups and vaccinations for livestock\n");
                response.append("2. Diagnosis and treatment of illnesses and injuries in animals\n");
                response.append("3. Reproductive health management and breeding assistance for livestock\n");
            } else if (text.equals("4*3*1")) {
                // Process Routine health check-ups and vaccinations for livestock
                response.append("CON Routine health check-ups and vaccinations for livestock selected. Thank you!\n");
            } else if (text.equals("4*3*2")) {
                // Process Diagnosis and treatment of illnesses and injuries in animals
                response.append("CON Diagnosis and treatment of illnesses and injuries in animals selected. Thank you!\n");
            } else if (text.equals("4*3*3")) {
                // Process Reproductive health management and breeding assistance for livestock
                response.append("CON Reproductive health management and breeding assistance for livestock selected. Thank you!\n");
            }
        } else if (text.equals("5")) {
            response.append("CON Insurance Menu:\n");
            response.append("1. Crop Insurance\n");
            response.append("2. Livestock Insurance\n");
            response.append("3. Farm Equipment Insurance\n");
            response.append("4. Weather Index Insurance\n");
            response.append("5. Insurance Claims\n");
            response.append("6. Contact Support\n");
        } else if (text.startsWith("5*1")) {
            response.append("CON Crop Insurance:\n");
            response.append("1. Enter insurance type:\n");
            response.append("2. Enter coverage amount:\n");
            response.append("3. Enter duration (in months):\n");
            response.append("4. Enter premium amount:\n");
        } else if (text.startsWith("5*2")) {
            response.append("CON Livestock Insurance:\n");
            response.append("1. Enter livestock type:\n");
            response.append("2. Enter number of livestock:\n");
            response.append("3. Enter total value of livestock:\n");
        } else if (text.startsWith("5*3")) {
            response.append("CON Farm Equipment Insurance:\n");
            response.append("1. Enter equipment type:\n");
            response.append("2. Enter equipment value:\n");
            response.append("3. Enter equipment age:\n");
            response.append("4. Enter deductible amount:\n");
        } else if (text.startsWith("5*4")) {
            response.append("CON Weather Index Insurance:\n");
            response.append("1. Enter weather parameter:\n");
            response.append("2. Enter coverage area (in acres):\n");
            //My Account
        }
        if (text.equals("6")) {
            response.append("CON My Account:\n");
            response.append("1. Manage my profile\n");
            response.append("2. My money\n");
        } else if (text.startsWith("6*1")) { // Manage my profile
            if (text.equals("6*1")) {
                response.append("CON Profile Management:\n");
                response.append("1. Update name\n");
                response.append("2. Update email\n");
                response.append("3. Update password\n");
            } else if (text.startsWith("6*1*1")) { // Update name
                response.append("CON Enter new name:\n");
            } else if (text.startsWith("6*1*2")) { // Update email
                response.append("CON Enter new email:\n");
            } else if (text.startsWith("6*1*3")) { // Update password
                response.append("CON Enter new password:\n");
            }
        } else if (text.startsWith("6*2")) { // My money
            if (text.equals("6*2")) {
                response.append("CON My Money Options:\n");
                response.append("1. Check balance\n");
                response.append("2. Transfer funds\n");
                response.append("3. Transaction history\n");
            } else if (text.startsWith("6*2*1")) { // Check balance
                response.append("CON Your current balance is Ksh 30,000.\n");
            } else if (text.startsWith("6*2*2")) { // Transfer funds
                if (text.equals("6*2*2")) {
                    response.append("CON Enter recipient account number:\n");
                } else {
                    String[] parts = text.split("\\*");
                    int numberOfStars = parts.length - 1;
                    if (numberOfStars == 3) {
                        response.append("CON Dear Customer, Your payment of Ksh 20,000 to Account EquityBank was Successful.");
                    }
                }
            } else if (text.startsWith("6*2*3")) { // Transaction history
                response.append("CON Here is your transaction history:\n");
                response.append("1. Transaction 1 details\n");
                response.append("2. Transaction 2 details\n");
                response.append("3. Transaction 3 details\n");
            }
        } else {
            // Handle other cases or invalid input
        }

// Return the USSD response
        return response.toString();
    }
}



