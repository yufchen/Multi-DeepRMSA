package tool;

import java.io.File;
import java.io.FileWriter;
import java.util.Random;

public class GenerateTimeto {
    public int lambda_req = 12;

    private double rand_exp(double lambda) {
        Random r = new Random();
        double z = r.nextDouble();
        double result = -(1 / lambda) * Math.log(z);
        return result;
    }

    public static void main(String[] args) throws Exception {

//        FileOutputStream fos = new FileOutputStream("./t.txt");
//        int t_num = 1;
//        long startTime = System.currentTimeMillis();
//        for(int i=0; i<100; i++){
//            System.out.println("Broker->DRL: {\"dst\":\"26\",\"1000\":\"1111111111111110000000000000000000000000000000011111111110000100000000111110000001110000000000000000\",\"TTL\":\"10.014445074620827\",\"req_id\":\"17416\",\"1017\":\"1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111\",\"1016\":\"0000011111111111111111111111111111111111111111111111111111111111111111111000001000000001111111111111\",\"1015\":\"1111111111111111111000000000000011111111111111000000011111111111111111111111111111111111111000000000\",\"1014\":\"1111111111111111111111111000001111111111111111111111111111110000011111111111111111111100000001111111\",\"1013\":\"1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111\",\"2003\":\"0000011100010000000000000001111100000000000000000000000000000000000000110000000000000001111110000001\",\"1012\":\"1111111111111110000111111111111111111111111111111111111111111100000000000110000001111111111111111111\",\"2002\":\"0000011100011000000000000001110000000000000000000000000000000000000000111000001100000000000110000001\",\"src\":\"8\",\"bandwidth\":\"49\",\"1011\":\"1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111\",\"2001\":\"0000011100011000000000000000000000001111110000100000001000000000000111111000000111110000000110000011\",\"1010\":\"1111111111111111111111111111111111111111100000011111111111111111111111111111111111111111111111111111\",\"2000\":\"0000011100011000000000000001110000000000001111100000000000000000000000111000001000000000000110000001\",\"1008\":\"0000000000111111111111100000000011111111100000011111111111111100000111110000000011111100000001111111\",\"0\":\"0000000000000000000000000000000000000000000000000000000001110000000000000110000111110000000000000001\",\"1007\":\"0000100000000000000000000000000011111111111111000000000001111100001111111110000111110000011000000000\",\"1\":\"0000000000000000000001111110000000000000011111111111111111111111110000111111111111110000000111111111\",\"1006\":\"0000011100000111111000011100001100000000000000100000000001110000010000000000001000000001111000000111\",\"2\":\"1111000000000000000001111111111100000000001111111111111000000011110000111111111111111111111111111111\",\"1005\":\"0000011100000000000100000000000000000011110000000001111000000000000000000100000100000001111110000011\",\"3\":\"1111100000000000000000000000001111110000001111100000000001110000000000000111111111110000011000000111\",\"1004\":\"1111111111111000000000000000000000011111111100000001111111111111110000111110000001111111111111111111\",\"1003\":\"1111111100000000001111111110000000000000001111000000011100000000011111111111111111111100000001111111\",\"1002\":\"0000011100001000000000000000001100000100001111111110000111111000001111111000001000000001111110000001\",\"1001\":\"0000000000000000000000111000001111111000000011111110001111110000011111111000001111111111110000000011\",\"1009\":\"1111111111111111111111111111111111111111111111111111111111111111111111111000001111111111111111111111\"}\n");
//        }
//
//        long endTime = System.currentTimeMillis();
//        String writestring = "Requests_Total: " + String.valueOf(t_num) + "  Time(ms): " + String.valueOf(endTime-startTime) + "\n";
//        fos.write(writestring.getBytes());
//        fos.close();

        GenerateTimeto generateTimeto = new GenerateTimeto();
        int length = 100;

        double[] timeto = new double[length];
        double[] arr2 = new double[length];
        for(int i=0; i<length; i++){
            timeto[i] = generateTimeto.rand_exp(generateTimeto.lambda_req);
        }

        File file = new File("./tools/RandExp_TimeTo.txt");
        FileWriter out = new FileWriter(file);

        for(int i=0; i<length; i++){
            out.write(timeto[i]+"\t");
        }
        out.write("\r\n");

        out.close();

//        BufferedReader in = new BufferedReader(new FileReader(file));
//        String line;
//        int row = 0;
//        while((line = in.readLine()) != null){
//            String[] temp = line.split("\t");
//            for(int j =0; j<temp.length; j++)
//            arr2[j] = Double.parseDouble(temp[j]);
//            row += 1;
//        }
//        in.close();

    }
}