package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ReadCfg {
    public String Broker_IPv4 = null;
    public String Broker_Port_CTRL1 = null;
    public String Broker_Port_CTRL2 = null;
    public String Broker_Port_DRL = null;

    public String Server_IPv4 = null;
    public String Server_Port_DRL = null;

    public String Serverp_IPv4 = null;
    public String Serverp_Port_DRL = null;

    public String Timeto_path = null;

    public void getinfo_from_cfg(String str){
        if(str.startsWith("Broker_IPv4")){
            Broker_IPv4 = str.substring(str.indexOf("=")+1).trim();
        }else if(str.startsWith("Broker_Port_CTRL1")){
            Broker_Port_CTRL1 = str.substring(str.indexOf("=")+1).trim();
        }else if(str.startsWith("Broker_Port_CTRL2")){
            Broker_Port_CTRL2 = str.substring(str.indexOf("=")+1).trim();
        }else if(str.startsWith("Broker_Port_DRL")){
            Broker_Port_DRL = str.substring(str.indexOf("=")+1).trim();
        }else if(str.startsWith("Server_IPv4")){
            Server_IPv4 = str.substring(str.indexOf("=")+1).trim();
        }else if(str.startsWith("Server_Port_DRL")){
            Server_Port_DRL = str.substring(str.indexOf("=")+1).trim();
        }else if(str.startsWith("Serverp_IPv4")){
            Serverp_IPv4 = str.substring(str.indexOf("=")+1).trim();
        }else if(str.startsWith("Serverp_Port_DRL")){
            Serverp_Port_DRL = str.substring(str.indexOf("=")+1).trim();
        }else if(str.startsWith("Timeto_path")){
            Timeto_path = str.substring(str.indexOf("=")+1).trim();
        }
    }

    ReadCfg() throws IOException {
        BufferedReader in = new BufferedReader(new FileReader("conf.cfg"));
        String str;
        while ((str = in.readLine()) != null) {
            if('#' == str.charAt(0)){
                continue; //annotations
            }
            this.getinfo_from_cfg(str);
        }

    }
}
