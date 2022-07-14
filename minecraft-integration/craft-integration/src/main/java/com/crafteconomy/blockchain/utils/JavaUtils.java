package com.crafteconomy.blockchain.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class JavaUtils {

    public static String streamToString(java.io.InputStream inStream) {
        StringBuffer response = new StringBuffer();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(inStream));
            String inputLine;
            
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }            
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response.toString();
    }
    
}
