package main;


import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.locks.LockSupport;

public class myServer {
    /*
    //variables for DRL
    private PrintStream send_DRL = null;
    private BufferedReader recv_DRL = null;


    //variables for controllers
    private PrintStream send_CTRLer1 = null;
    private BufferedReader recv_CTRLer1 = null;

    private PrintStream send_CTRLer2 = null;
    private BufferedReader recv_CTRLer2 = null;
*/
    //Status Setup
    //REQ(if generate request)_DRL(if send req to DRL)(if recv result from DRL)_BROKER(if send)(if recv)_RWD(if send reward to DRL)(if recv OK from DRL)
    private static int REQ0_DRL00_BROKER00_RWD00 = 0;
    private static int REQ1_DRL00_BROKER00_RWD00 = 1;
    private static int REQ1_DRL10_BROKER00_RWD00 = 2;
    private static int REQ1_DRL11_BROKER00_RWD00 = 3;
    private static int REQ1_DRL11_BROKER10_RWD00 = 4;
    private static int REQ1_DRL11_BROKER11_RWD00 = 5;
    private static int REQ1_DRL11_BROKER11_RWD10 = 6;
    private static int REQ1_DRL11_BROKER11_RWD11 = 7;
    private static int current_status = REQ0_DRL00_BROKER00_RWD00;

    private static boolean is_DRL_Ready = false;
    private static boolean is_Broker_Ready = false;

    private static int DRL_SEND_TOPOLOGY = 0;
    private static int DRL_SEND_REQLINKMAP = 1;
    private static int DRL_send_Req_Status = DRL_SEND_TOPOLOGY;

    private static int DRL_RECV_OK = 0;
    private static int DRL_RECV_RESULT = 1;
    private static int DRL_recv_Req_Status = DRL_RECV_OK;


    private static int req_id = 0;
    private static int current_src = 0;
    private static int current_dst = 0;
    private static int current_bandwidth = 0;
    private static double current_TTL = 0;

    private static List<List> request_set = null;

    private static Map<String, char[]> current_slot_map = null;
    private static Map<String, List<String>> current_netTopology = null;

    private static List<myServer.DRLThread.DRLrecv_req> DRLrecvreqs = null;
    private static double[] timeto = null;
    private static Map FinalReward = null;

    private static FileOutputStream loger = null;

    private static Thread mainThread = Thread.currentThread();

    //serverthread for multi-client
    private class DRLThread implements Runnable{
        private Socket client = null;

        //public boolean recv_ready = false;
        public String recv_str = null;
        //public String send_str = null;
        public Map send_map_data = null;
        private DataOutputStream send_json_stream = null;

        private DRLThread(Socket client) throws IOException{
            this.client = client;
            this.send_json_stream = new DataOutputStream(client.getOutputStream());
        }

        public class DRLrecv_req{
            private int req_id;
            private List<Integer> path_links;
            private int f_start;
            private int f_end;
            private double TTL;
            private int src;
            private int dst;
            private int new_virtual_link_id;

            public int getReq_id(){
                return req_id;
            }
            public void setReq_id(int req_id){
                this.req_id = req_id;
            }
            public List<Integer> getPath_links(){
                return path_links;
            }
            public void setPath_links(List<Integer> path_links){
                this.path_links = path_links;
            }
            public int getF_start(){
                return f_start;
            }
            public void setF_start(int f_start){
                this.f_start = f_start;
            }
            public int getF_end(){
                return f_end;
            }
            public void setF_end(int f_end){
                this.f_end = f_end;
            }
            public double getTTL(){
                return TTL;
            }
            public void setTTL(double TTL){
                this.TTL = TTL;
            }
            public int getSrc(){return src;}
            public void setSrc(int src){this.src = src;}
            public int getDst(){return dst;}
            public void setDst(int dst){this.dst = dst;}
            public int getNew_virtual_link_id(){return new_virtual_link_id;}
            public void setNew_virtual_link_id(int new_virtual_link_id){this.new_virtual_link_id = new_virtual_link_id;}
        }

        public class DRLsend_req{
            private int src;
            private int dst;
            private int bandwidth;
            private double TTL;

            public int getSrc(){
                return src;
            }
            public void setSrc(int src){
                this.src = src;
            }
            public int getDst(){
                return dst;
            }
            public void setDst(int dst){
                this.dst = dst;
            }
            public int getBandwidth(){return bandwidth;}
            public void setBandwidth(int bandwidth){this.bandwidth = bandwidth;}
            public double getTTL(){
                return TTL;
            }
            public void setTTL(double TTL){
                this.TTL = TTL;
            }
        }

        private void send_map() throws IOException{
            JSONObject send_json = JSONObject.fromObject(this.send_map_data);
            String send_json_string = send_json.toString();
            byte[] send_json_byte = send_json_string.getBytes();
            this.send_json_stream.write(send_json_byte);
            this.send_json_stream.flush();
            //System.out.println("CTRL1->DRL: " + send_json_string);
            //String logstr = "CTRL1->DRL: " + send_json_string + "\n";
            //loger.write(logstr.getBytes());
        }



        private void parse_recv() throws IOException{
            JSONArray recvjsonArray = JSONArray.fromObject(recv_str);
            JSONObject recvjsonObj = null;
            DRLrecvreqs = null;
            DRLrecvreqs = new ArrayList<DRLThread.DRLrecv_req>();

            for(int i=0; i<recvjsonArray.size(); i++){
                recvjsonObj = recvjsonArray.getJSONObject(i);

                int req_id = recvjsonObj.getInt("req_id");
                if(req_id == -1){
                    continue;
                }


                JSONArray path_linksArray = recvjsonObj.getJSONArray("path_links");
                List<Integer> path_links = new ArrayList<>();
                for(int j=0; j<path_linksArray.size(); j++){
                    path_links.add(path_linksArray.getInt(j));
                }

                int f_start = recvjsonObj.getInt("f_start");
                int f_end = recvjsonObj.getInt("f_end");
                double TTL = recvjsonObj.getDouble("TTL");
                int src = recvjsonObj.getInt("src");
                int dst = recvjsonObj.getInt("dst");
                int vlinkid = recvjsonObj.getInt("virtual_linkid");

                DRLThread.DRLrecv_req DRLrecv_req = new DRLThread.DRLrecv_req();
                DRLrecv_req.setReq_id(req_id);
                DRLrecv_req.setPath_links(path_links);
                DRLrecv_req.setF_start(f_start);
                DRLrecv_req.setF_end(f_end);
                DRLrecv_req.setTTL(TTL);
                DRLrecv_req.setSrc(src);
                DRLrecv_req.setDst(dst);
                DRLrecv_req.setNew_virtual_link_id(vlinkid);
                DRLrecvreqs.add(DRLrecv_req);
            }
            //System.out.println("DRL->CTRL1: " + recv_str);
            //String logstr = "DRL->CTRL1: " + recv_str + "\n";
            //loger.write(logstr.getBytes());
        }


        @Override
        public void run() {
            try{
                BufferedReader recv = new BufferedReader(new InputStreamReader(client.getInputStream()));
                boolean run_flag =true;
                while(run_flag){
                    recv_str = recv.readLine();
                    if(is_DRL_Ready == false){
                        is_DRL_Ready = true;
                    }
                    else if(DRL_recv_Req_Status == DRL_RECV_OK){
                        LockSupport.unpark(mainThread);
                    }
                    else if(DRL_recv_Req_Status == DRL_RECV_RESULT){
                        parse_recv();
                        LockSupport.unpark(mainThread);
                    }
                    LockSupport.park();

                    //blocking for send from mainthread
                    //will be unparked by mainthread after knowing send_str
                    send_map();
                }

                //send.close();
                client.close();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }


    private class BrokerThread implements Runnable {
        private Socket client = null;

        public boolean recv_ready = false;
        public String recv_str = null;
        public String send_str = null;
        public Map send_map_data = null;
        public List<Map> send_list_data = null;
        private DataOutputStream send_json_stream = null;

        public BrokerThread(Socket client) throws IOException{
            this.client = client;
            this.send_json_stream = new DataOutputStream(client.getOutputStream());
        }

        public class Brokersend_req{
            private int src;
            private int dst;
            private int linkid;
            private int linklen;
            private String link_slot;
        }

        public class Broker_recv{
            private int req_id;
            private List<Integer> path_links;
            private int f_start;
            private int f_end;
            private double TTL;

            public int getReq_id(){
                return req_id;
            }
            public void setReq_id(int req_id){
                this.req_id = req_id;
            }

            public List<Integer> getPath_links(){
                return path_links;
            }
            public void setPath_links(List<Integer> path_links){
                this.path_links = path_links;
            }


            public int getF_start(){
                return f_start;
            }
            public void setF_start(int f_start){
                this.f_start = f_start;
            }
            public int getF_end(){
                return f_end;
            }
            public void setF_end(int f_end){
                this.f_end = f_end;
            }
            public double getTTL(){
                return TTL;
            }
            public void setTTL(double TTL){
                this.TTL = TTL;
            }
        }

        private void send_map() throws IOException{
            JSONObject send_json = JSONObject.fromObject(this.send_map_data);
            String send_json_string = send_json.toString();
            send_json_string += "\n";
            byte[] send_json_byte = send_json_string.getBytes();
            this.send_json_stream.write(send_json_byte);
            this.send_json_stream.flush();
        }

        private void send_list() throws IOException{
            JSONArray send_json = JSONArray.fromObject(this.send_list_data);
            String send_json_string = send_json.toString();
            send_json_string += "\n";
            byte[] send_json_byte = send_json_string.getBytes();
            this.send_json_stream.write(send_json_byte);
            this.send_json_stream.flush();
            //System.out.println("CTRL1->Broker: " + send_json_string);
            //String logstr = "CTRL1->Broker: " + send_json_string + "\n";
            //loger.write(logstr.getBytes());
        }

        private void parse_recv_reward() throws IOException{
            JSONObject recvjsonObj = JSONObject.fromObject(recv_str);
            FinalReward = new HashMap<>();
            Integer status = Integer.parseInt(recvjsonObj.getString("Status"));
            int link_id = Integer.valueOf(recvjsonObj.getString("vlink_id"));
            int f_start = Integer.valueOf(recvjsonObj.getString("f_start"));
            int f_end = Integer.valueOf(recvjsonObj.getString("f_end"));
            if(status == -2){
                FinalReward.put("Status", String.valueOf(status));
                FinalReward.put("DRLrecv_req", null);
            }
            else {
                for (DRLThread.DRLrecv_req DRLrecv_req : DRLrecvreqs) {
                    if (DRLrecv_req.getNew_virtual_link_id() == link_id) {
                        DRLrecv_req.setF_start(f_start);
                        DRLrecv_req.setF_end(f_end);
                        FinalReward.put("Status", String.valueOf(status));
                        FinalReward.put("DRLrecv_req", DRLrecv_req);
                        break;
                    }
                }
            }
            //System.out.println("Broker->CTRL1(RT): " + recv_str);
            //String logstr = "Broker->CTRL1(RT): " + recv_str + "\n";
            //loger.write(logstr.getBytes());
        }


        @Override
        public void run() {
            try{
                BufferedReader recv = new BufferedReader(new InputStreamReader(client.getInputStream()));
                boolean run_flag =true;
                //boolean is_first_recv = true;
                //int recv_status = 0;
                while(run_flag) {

                    LockSupport.park();
                    //blocking for send from mainthread
                    //will be unparked by mainthread after knowing send_str
                    send_list();

                    recv_str = recv.readLine();
                    if(is_Broker_Ready == false){
                        is_Broker_Ready = true;
                    }
                    else{
                        parse_recv_reward();
                        LockSupport.unpark(mainThread);
                    }

                }
                client.close();
            }catch(Exception e){
                e.printStackTrace();
            }
        }

    }




    private class networkInfo{
        //{src,dst,idx,len(km)}
        public int[][] netTopology = {
                {1, 2, 0, 1050},
                {2, 1, 3, 1050},
                {1, 3, 1, 1500},
                {3, 1, 6, 1500},
                {1, 8, 2, 2400},
                {8, 1, 22, 2400},

                {2, 3, 4, 600},
                {3, 2, 7, 600},
                {2, 4, 5, 750},
                {4, 2, 9, 750},
                {3, 6, 8, 1800},
                {6, 3, 15, 1800},

                {4, 5, 10, 600},
                {5, 4, 12, 600},
                {4, 11, 11, 1950},
                {11, 4, 32, 1950},
                {5, 6, 13, 1200},
                {6, 5, 16, 1200},
                {5, 7, 14, 600},
                {7, 5, 19, 600},

                {6, 10, 17, 1050},
                {10, 6, 29, 1050},
                {6, 14, 18, 1800},
                {14, 6, 41, 1800},
                {7, 8, 20, 750},
                {8, 7, 23, 750},
                {7, 10, 21, 1350},
                {10, 7, 30, 1350},

                {8, 9, 24, 750},
                {9, 8, 25, 750},
                {9, 10, 26, 750},
                {10, 9, 31, 750},
                {9, 12, 27, 300},
                {12, 9, 35, 300},
                {9, 13, 28, 300},
                {13, 9, 38, 300},

                {11, 12, 33, 600},
                {12, 11, 36, 600},
                {11, 13, 34, 750},
                {13, 11, 39, 750},
                {12, 14, 37, 300},
                {14, 12, 42, 300},
                {13, 14, 40, 150},
                {14, 13, 43, 150}
        };

        public int[] bnodes_2domain2 = {2,6,8,11,12}; //border nodes to domain2
        public int intra_NODE_NUM = 14; //domain1 14nodes
        public int domain2_NODE_NUM = 14; //domain2 14nodes

        public List<List> intra_Src_Dst_Pair = new ArrayList<>();
        public int intra_Num_Src_Dst_Pair = 0;

        public List<List> inter_Src_Dst_Pair = new ArrayList<>();
        public int inter_Num_Src_Dst_Pair = 0;

        public int SLOT_TOTAL = 100;
        public int intra_LINK_NUM = 44;

        public int lambda_req = 12;


        public void generate_Src_Dst_Pair(){
            for(int i=0; i<intra_NODE_NUM; i++){
                for(int j=0; j<intra_NODE_NUM; j++){
                    if(i!=j) {
                        List<Integer> pair = new ArrayList<>();
                        pair.add(i + 1);
                        pair.add(j + 1);
                        intra_Src_Dst_Pair.add(pair);
                        intra_Num_Src_Dst_Pair++;
                    }
                }
            }
            for(int i=0; i<intra_NODE_NUM; i++){
                for(int j= intra_NODE_NUM; j<intra_NODE_NUM + domain2_NODE_NUM; j++){
                    List<Integer> pair = new ArrayList<>();
                    pair.add(i + 1);
                    pair.add(j + 1);
                    inter_Src_Dst_Pair.add(pair);
                    inter_Num_Src_Dst_Pair++;
                }
            }
        };

        public void generate_Request(boolean is_inter){
            Random r = new Random();
            req_id += 1;

            if (is_inter) {
                int idx = r.nextInt(inter_Num_Src_Dst_Pair);
                current_src = Integer.parseInt(String.valueOf(inter_Src_Dst_Pair.get(idx).get(0)));
                current_dst = Integer.parseInt(String.valueOf(inter_Src_Dst_Pair.get(idx).get(1)));

            }
            else {
                int idx = r.nextInt(intra_Num_Src_Dst_Pair);
                current_src = Integer.parseInt(String.valueOf(intra_Src_Dst_Pair.get(idx).get(0)));
                current_dst = Integer.parseInt(String.valueOf(intra_Src_Dst_Pair.get(idx).get(1)));
            }
            current_bandwidth = r.nextInt(76) + 25;
            current_TTL = 0;
            double service_time = 7;
            while(current_TTL == 0 || current_TTL >= service_time*2){
                current_TTL = rand_exp((1/service_time));
            }
        }

        private double rand_exp(double lambda){
            Random r = new Random();
            double z = r.nextDouble();
            double result = -(1/lambda)*Math.log(z);
            return result;
        }

        /*
        private List<Double> pack_request(int src, int dst, int bandwidth, double TTL){
            List<Double> item = new ArrayList<Double>();
            item.add(Double.valueOf(src));
            item.add(Double.valueOf(dst));
            item.add(Double.valueOf(bandwidth));
            item.add(Double.valueOf(TTL));
            return item;
        }
        */

        public Map pack_sendmsg2DRL(boolean is_inter){
            Map<String, String> send_valuemap = new HashMap<String, String>();
            for(String link_id:current_slot_map.keySet()){
                String link_slotmap = String.valueOf(current_slot_map.get(link_id));
                send_valuemap.put(link_id, link_slotmap);
            }

            send_valuemap.put("req_id", String.valueOf(req_id));
            send_valuemap.put("src", String.valueOf(current_src));

            if(is_inter) {
                for (int i = 0; i < bnodes_2domain2.length; i++) {
                    String bnodename = "bnode" + String.valueOf(i);
                    if(bnodes_2domain2[i] != current_src) {
                        send_valuemap.put(bnodename, String.valueOf(bnodes_2domain2[i]));
                    }
                }
            }

            send_valuemap.put("bandwidth", String.valueOf(current_bandwidth));
            send_valuemap.put("TTL", String.valueOf(current_TTL));
            return send_valuemap;
        }

        public Map pack_reward2DRL(DRLThread.DRLrecv_req DRLrecv_req, boolean is_failed){
            Map<String, String> send_valuemap = new HashMap<>();
            if(DRLrecv_req != null){
                if(is_failed) {
                    send_valuemap.put("Blocking", "1");
                    send_valuemap.put("vlinkid", String.valueOf(DRLrecv_req.getNew_virtual_link_id()));
                }
                else{
                    send_valuemap.put("Blocking", "0");
                    send_valuemap.put("vlinkid", String.valueOf(DRLrecv_req.getNew_virtual_link_id()));
                }
            }
            else{
                //drop will not take the train into consideration
                send_valuemap.put("Blocking", "-1");
                send_valuemap.put("vlinkid", "-1");
            }
            return send_valuemap;
        }

        private String combine_LinkSlot(String s1, String s2){
            if(s1.length() != s2.length()){
                System.out.println("Error computing linkslot");
            }
            char[] result = new char[s1.length()];
            for(int i=0; i<s1.length(); i++){
                char ch1 = s1.charAt(i);
                char ch2 = s2.charAt(i);
                if(ch1 == '1' && ch2 == '1'){
                    result[i] = '1';
                }
                else{
                    result[i] = '0';
                }
            }
            return String.valueOf(result);
        }

        private int cal_len(int linkid){
            int link_len = 0;
            for(int i=0; i<netTopology.length; i++){
                if(netTopology[i][2] == linkid){
                    link_len = netTopology[i][3];
                    break;
                }
            }
            return link_len;
        }

        private List<Map> pack_sendmsg2Broker(){
            List<Map> send_valuelist = new ArrayList<>();

            Map<String, String> totalsend = new HashMap<String, String>();
            totalsend.put("final_req_id", String.valueOf(req_id));
            totalsend.put("final_src", String.valueOf(current_src));
            totalsend.put("final_dst", String.valueOf(current_dst));
            totalsend.put("final_bw", String.valueOf(current_bandwidth));
            totalsend.put("final_TTL", String.valueOf(current_TTL));
            send_valuelist.add(totalsend);

            //int new_link_id = 0;
            for(myServer.DRLThread.DRLrecv_req DRLrecvreq:DRLrecvreqs){
                Map<String, String> send_valuemap = new HashMap<String, String>();

                send_valuemap.put("link_id", String.valueOf(DRLrecvreq.getNew_virtual_link_id()));
                DRLrecvreq.setNew_virtual_link_id(DRLrecvreq.getNew_virtual_link_id());

                send_valuemap.put("src", String.valueOf(DRLrecvreq.getSrc()));
                send_valuemap.put("dst", String.valueOf(DRLrecvreq.getDst()));

                //calculate total linkslot & linklength
                StringBuilder sb = new StringBuilder(SLOT_TOTAL);
                for(int i=0; i<SLOT_TOTAL; i++){
                    sb.append('1');
                }
                String f_linkslot = sb.toString();
                int link_len = 0;

                List<Integer> pathlinks = DRLrecvreq.getPath_links();
                for(int i=0; i<pathlinks.size(); i++){
                    Integer link_id = pathlinks.get(i);
                    String link_slotmap = String.valueOf(current_slot_map.get(String.valueOf(link_id)));
                    f_linkslot = combine_LinkSlot(f_linkslot, link_slotmap);
                    link_len += cal_len(link_id);
                }
                send_valuemap.put("len", String.valueOf(link_len));
                send_valuemap.put("link_slot", String.valueOf(f_linkslot));
                send_valuelist.add(send_valuemap);
            }
            return send_valuelist;
        }

        public void release_slot_map(double time_to){

            if(request_set.isEmpty() == false){
                List<List> del_req = new ArrayList<>();
                for(int i=0; i<request_set.size(); i++){
                    List<List> req = request_set.get(i);
                    List<Double> new_TTL = req.get(4);//index 4 is TTL
                    new_TTL.set(0, new_TTL.get(0)-time_to);
                    req.get(4).set(0, new_TTL.get(0));

                    if(new_TTL.get(0) <= 0){
                        List<Double> current_wp_link = req.get(1);
                        List<Double> current_fs = req.get(2);
                        List<Double> current_fe = req.get(3);
                        update_slot_map_for_releasing_wp(current_wp_link, current_fs, current_fe);
                        del_req.add(req);
                    }
                }

                for(int i=0; i<del_req.size(); i++){
                    List<List> req = del_req.get(i);
                    request_set.remove(req);
                }
            }

        }

        private void update_slot_map_for_releasing_wp(List<Double> current_wp_link, List<Double> current_fs, List<Double> current_fe){
            int f_start  = current_fs.get(0).intValue();
            int f_end = current_fe.get(0).intValue();
            for(int i=0; i<current_wp_link.size(); i++){
                int link_idx = current_wp_link.get(i).intValue();
                char[] linkslotmap = current_slot_map.get(String.valueOf(link_idx));

                for(int f = f_start; f<f_end+1; f++){
                    if(linkslotmap[f] == '1'){
                        System.out.println("Release Slotmap Error");
                    }
                    linkslotmap[f] = '1';
                }

                current_slot_map.put(String.valueOf(link_idx), linkslotmap);
            }
        }

        public void update_slot_map_for_commiting_wp(List<List> result_req){
            if(result_req.isEmpty() == false){
                //List<Double> req_id = result_req.get(0);
                List<Double> current_wp_link = result_req.get(1);
                List<Double> current_fs = result_req.get(2);
                List<Double> current_fe = result_req.get(3);
                //List<Double> current_TTL = result_req.get(4);
                int f_start = current_fs.get(0).intValue();
                int f_end = current_fe.get(0).intValue();
                for(int i=0; i<current_wp_link.size(); i++){
                    int link_idx = current_wp_link.get(i).intValue();
                    char[] linkslotmap = current_slot_map.get(String.valueOf(link_idx));

                    for(int f = f_start; f<f_end+1; f++){
                        if(linkslotmap[f] == '0'){
                            System.out.println("Update Slotmap Error");
                        }
                        linkslotmap[f] = '0';
                    }

                    current_slot_map.put(String.valueOf(link_idx), linkslotmap);
                }

            }
        }

        public void update_Net_and_ReqSet(DRLThread.DRLrecv_req DRLrecv_req){
            //from return value
            List<Double> req_idx = new ArrayList<>();
            req_idx.add(Double.valueOf(DRLrecv_req.getReq_id()));

            List<Integer> path_links = DRLrecv_req.getPath_links();
            List<Double> new_path_links = new ArrayList<>();
            for(int i=0; i<path_links.size(); i++){
                new_path_links.add(Double.valueOf(path_links.get(i)));
            }

            List<Double> fs_start = new ArrayList<>();
            fs_start.add(Double.valueOf(DRLrecv_req.getF_start()));

            List<Double> fs_end = new ArrayList<>();
            fs_end.add(Double.valueOf(DRLrecv_req.getF_end()));

            List<Double> TTL = new ArrayList<>();
            TTL.add(Double.valueOf(DRLrecv_req.getTTL()));

            List<List> item = new ArrayList<>();
            item.add(req_idx);
            item.add(new_path_links);
            item.add(fs_start);
            item.add(fs_end);
            item.add(TTL);

            request_set.add(item);
            update_slot_map_for_commiting_wp(item);

        }

        public networkInfo(){
            this.generate_Src_Dst_Pair();
            current_slot_map = new HashMap<>();
            //add border link to slotmap
            for(int i=0; i<netTopology.length; i++){
                char[] link_slot = new char[SLOT_TOTAL];
                for(int j=0; j<SLOT_TOTAL; j++){
                    link_slot[j] = '1';
                }
                int link_id = netTopology[i][2];
                current_slot_map.put(String.valueOf(link_id), link_slot);
            }

            current_netTopology = new HashMap<>();
            for(int i=0; i<netTopology.length; i++){
                List<String> linkinfo = new ArrayList<>();
                linkinfo.add(String.valueOf(netTopology[i][0])); //src node
                linkinfo.add(String.valueOf(netTopology[i][1])); //dst node
                linkinfo.add(String.valueOf(netTopology[i][3])); //link length
                int link_id = netTopology[i][2];
                current_netTopology.put(String.valueOf(link_id), linkinfo);
            }
            request_set = new ArrayList<>();
        }


    }





    public void main(String[] args) throws Exception{
        //read cfg
        ReadCfg readCfg = new ReadCfg();

        myServer maintest = new myServer();
        networkInfo networkInfo = maintest.new networkInfo();
        //Get rand_exp timeto
        timeto = new double[100]; //timeto max length = 100
        File file = new File(readCfg.Timeto_path);
        BufferedReader in = new BufferedReader(new FileReader(file));
        String line;
        int row = 0;
        while((line = in.readLine()) != null){
            String[] temp = line.split("\t");
            for(int j =0; j<temp.length; j++)
                timeto[j] = Double.parseDouble(temp[j]);
            row += 1;
        }
        in.close();
        /*
        //from return value
        List<Double> req_idx = new ArrayList<>();
        req_idx.add(1.0);

        List<Double> path_links = new ArrayList<>();
        path_links.add(15.0);
        path_links.add(14.0);
        path_links.add(2.0);

        List<Double> fs_start = new ArrayList<>();
        fs_start.add(0.0);

        List<Double> fs_end = new ArrayList<>();
        fs_end.add(6.0);

        List<Double> TTL = new ArrayList<>();
        TTL.add(15.245);

        List<List> temp_item = new ArrayList<>();
        temp_item.add(req_idx);
        temp_item.add(path_links);
        temp_item.add(fs_start);
        temp_item.add(fs_end);
        temp_item.add(TTL);



        request_set.add(temp_item);
        networkInfo.update_slot_map_for_commiting_wp(temp_item);

        while(request_set.isEmpty() == false) {
            networkInfo.release_slot_map();
        }


        while(true) {
            networkInfo.generate_Request(false);

        }
        */
        //For DRL, the CTRLler is server
        //DRL port:20007
        ServerSocket Server_DRL = new ServerSocket(Integer.parseInt(readCfg.Server_Port_DRL));
        Socket Client_DRL = null;
        Client_DRL = Server_DRL.accept(); //blocking until DRL connected
        System.out.println("DRL Connected");
        DRLThread t_DRL = maintest.new DRLThread(Client_DRL);
        Thread t_DRL_th = new Thread(t_DRL);
        t_DRL_th.start();

        //For broker, the CTRLler is client
        Socket Client_Broker = new Socket(readCfg.Broker_IPv4, Integer.parseInt(readCfg.Broker_Port_CTRL1));
        System.out.println("Connected to Broker");
        BrokerThread t_Broker = maintest.new BrokerThread(Client_Broker);
        Thread t_Broker_th = new Thread(t_Broker);
        t_Broker_th.start();

        is_Broker_Ready = true;
        int total_num = 0;
        loger = new FileOutputStream("./log/CTRLer1.log");
        FileOutputStream fos = new FileOutputStream("./log/ReqsTime.log");
        long startTime = System.currentTimeMillis();
        //main loop
        while(true) {
            if(current_status == REQ0_DRL00_BROKER00_RWD00) {
                //blocking until all sockets are ready
                while(is_Broker_Ready == false || is_DRL_Ready == false) {
                    Thread.sleep(100);
                }

                String total_info = "Total Reqs: " + String.valueOf(total_num);
                String logstr = "\n--------------------------------------------------------------------------------\n" + total_info + "\n";
                loger.write(logstr.getBytes());
                if(total_num % 2000 == 0){
                    long endTime = System.currentTimeMillis();
                    String writestring = "Requests_Total: " + String.valueOf(total_num) + "  Time(ms): " + String.valueOf(endTime-startTime) + "\n";
                    fos.write(writestring.getBytes());
                    System.out.println(writestring);
                }

                networkInfo.release_slot_map(timeto[(int)total_num%100]);

                total_num += 1;
                networkInfo.generate_Request(true);
                current_status = REQ1_DRL00_BROKER00_RWD00;
            }
            else if(current_status == REQ1_DRL00_BROKER00_RWD00){
                //this status contains 2 send&recv
                //1stSend:current_Topology
                //2ndSend:Req&slotmap
                if(DRL_send_Req_Status == DRL_SEND_TOPOLOGY){
                    DRL_send_Req_Status = DRL_SEND_REQLINKMAP;
                    DRL_recv_Req_Status = DRL_RECV_OK;
                    t_DRL.send_map_data = current_netTopology;
                    LockSupport.unpark(t_DRL_th);
                    LockSupport.park();
                    //will be unparked by DRLThread after recv OK
                }
                else if(DRL_send_Req_Status == DRL_SEND_REQLINKMAP){
                    DRL_send_Req_Status = DRL_SEND_TOPOLOGY;
                    DRL_recv_Req_Status = DRL_RECV_RESULT;
                    t_DRL.send_map_data = networkInfo.pack_sendmsg2DRL(true);
                    LockSupport.unpark(t_DRL_th);
                    LockSupport.park();
                    //will be unparked by DRLThread after recv result;
                    current_status = REQ1_DRL11_BROKER00_RWD00;
                }
            }
            else if(current_status == REQ1_DRL11_BROKER00_RWD00){
                t_Broker.send_list_data = networkInfo.pack_sendmsg2Broker();
                LockSupport.unpark(t_Broker_th);
                LockSupport.park();
                //will be unparked by BrokerThread after recv reward
                current_status = REQ1_DRL11_BROKER11_RWD00;
            }
            else if(current_status == REQ1_DRL11_BROKER11_RWD00){

                Integer status = Integer.parseInt((String) FinalReward.get("Status"));
                if(status == -2){
                    //drop
                    t_DRL.send_map_data = networkInfo.pack_reward2DRL(null, true);
                }
                else if(status == -1){
                    //blocking
                    DRLThread.DRLrecv_req DRLrecv_req = (DRLThread.DRLrecv_req)FinalReward.get("DRLrecv_req");
                    t_DRL.send_map_data = networkInfo.pack_reward2DRL(DRLrecv_req, true);
                }
                else if(status == 1){
                    //success
                    DRLThread.DRLrecv_req DRLrecv_req = (DRLThread.DRLrecv_req)FinalReward.get("DRLrecv_req");
                    networkInfo.update_Net_and_ReqSet(DRLrecv_req);
                    t_DRL.send_map_data = networkInfo.pack_reward2DRL(DRLrecv_req, false);
                }

                is_DRL_Ready = false;
                DRL_recv_Req_Status = DRL_RECV_OK;
                //is_Broker_Ready =false;
                LockSupport.unpark(t_DRL_th);
                //LockSupport.unpark(t_Broker_th);
                //LockSupport.park();
                //will be unparked when t_DRL return OK
                current_status = REQ0_DRL00_BROKER00_RWD00;
            }
        }


    }
}
