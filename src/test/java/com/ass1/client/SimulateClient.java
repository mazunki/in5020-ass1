package com.ass1.client;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;

public class SimulateClient {

    public static void main(String[] args) {
        String inputFile = "src/main/resources/exercise_1_input.txt";

        try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
            String line;
            int lineno = 0;
            while ((line = br.readLine()) != null) {
                lineno++;
                System.out.println("Processing line " + lineno + ": " + line);
                line = line.replaceAll("\\s+", " "); // Normalize spaces
                String zone = line.split("Zone:")[1].trim();

                // Extract method and rest of the line (arguments)
                String[] parts = line.split(" ", 2);
                String method = parts[0].trim(); // Method name
                String rest = parts[1].replace("Zone:" + zone, "").trim(); // Arguments part, remove zone

                // Split the remaining part (method args) by space
                String[] tokens = rest.split(" ");

                // Determine where the country name ends (first number starts method args)
                StringBuilder countryName = new StringBuilder();
                int i = 0;
                while (i < tokens.length && !tokens[i].matches("\\d+")) {
                    countryName.append(tokens[i]).append(" ");
                    i++;
                }

                // Trim the country name and capture remaining as arguments
                String country = countryName.toString().trim();
                String[] methodArgs = new String[tokens.length - i];
                System.arraycopy(tokens, i, methodArgs, 0, methodArgs.length);

                LinkedList<String> clientArgs = new LinkedList<String>(); // method + country + zone
                clientArgs.add(method);

                if (!country.isEmpty()) {
                    clientArgs.add(country);
                }

                clientArgs.addAll(Arrays.asList(methodArgs));
                clientArgs.add(zone);

                // Log and invoke Client.main()
                System.out.print("Invoking Client.main() with arguments: " + String.join(", ", clientArgs) + "... ");
                Client.main(clientArgs.toArray(new String[0]));
            }
        } catch (IOException e) {
            System.err.println("Error reading input file: " + e.getMessage());
        }
    }
}
