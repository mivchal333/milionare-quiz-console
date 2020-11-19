package service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ConfigurationService {
    public String readPort() throws IOException {
        BufferedReader br;
        br = new BufferedReader(new FileReader("port.txt"));
        String port = br.readLine();
        br.close();
        return port;
    }
}
