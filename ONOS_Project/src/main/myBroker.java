package main;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.locks.LockSupport;

public class myBroker {
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
    //CTR1L(if recv req)(if send reward)_CTR2L(if send req for status)(if recv status)(if send reward)_DRL(if send)(if recv result)
    private static int BROKER_NOT_READY = -1;
    private static int CTR1L00_CTR2L000_DRL00 = 0;
    private static int CTR1L10_CTR2L000_DRL00 = 1;
    private static int CTR1L10_CTR2L100_DRL00 = 2;
    private static int CTR1L10_CTR2L110_DRL00 = 3;
    private static int CTR1L10_CTR2L110_DRL10 = 4;
    private static int CTR1L10_CTR2L110_DRL11 = 5;
    private static int CTR1L11_CTR2L111_DRL10 = 6;
    private static int CTR1L11_CTR2L111_DRL11 = 7;
    private static int current_status = BROKER_NOT_READY;

    private static boolean is_DRL_Ready = false;
    private static boolean is_CTRL1_Ready = false;
    private static boolean is_CTRL2_Ready = false;


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

    private static Map<String, char[]> inter_slot_map = null;
    private static Map<String, char[]> current_slot_map = null;
    private static Map<String, List<String>> inter_netTopology = null;
    private static Map<String, List<String>> current_netTopology = null;

    private static List<DRLThread.DRLrecv_req> DRLrecvreqs = null;

    private static double[] timeto = null;

    private static Thread mainThread = Thread.currentThread();

    private static FileOutputStream loger = null;



    //serverthread for multi-client
    private class DRLThread implements Runnable {
        private Socket client = null;

        public boolean recv_ready = false;
        public String recv_str = null;
        public String send_str = null;
        public Map send_map_data = null;
        private DataOutputStream send_json_stream = null;

        private DRLThread(Socket client) throws IOException {
            this.client = client;
            this.send_json_stream = new DataOutputStream(client.getOutputStream());
        }

        public class DRLrecv_req {
            private int req_id;
            private List<Integer> path_links;
            private int f_start;
            private int f_end;
            private double TTL;
            private int src;
            private int dst;

            public int getReq_id() {
                return req_id;
            }

            public void setReq_id(int req_id) {
                this.req_id = req_id;
            }

            public List<Integer> getPath_links() {
                return path_links;
            }

            public void setPath_links(List<Integer> path_links) {
                this.path_links = path_links;
            }

            public int getF_start() {
                return f_start;
            }

            public void setF_start(int f_start) {
                this.f_start = f_start;
            }

            public int getF_end() {
                return f_end;
            }

            public void setF_end(int f_end) {
                this.f_end = f_end;
            }

            public double getTTL() {
                return TTL;
            }

            public void setTTL(double TTL) {
                this.TTL = TTL;
            }

            public int getSrc() {
                return src;
            }

            public void setSrc(int src) {
                this.src = src;
            }

            public int getDst() {
                return dst;
            }

            public void setDst(int dst) {
                this.dst = dst;
            }
        }

        public class DRLsend_req {
            private int src;
            private int dst;
            private int bandwidth;
            private double TTL;

            public int getSrc() {
                return src;
            }

            public void setSrc(int src) {
                this.src = src;
            }

            public int getDst() {
                return dst;
            }

            public void setDst(int dst) {
                this.dst = dst;
            }

            public int getBandwidth() {
                return bandwidth;
            }

            public void setBandwidth(int bandwidth) {
                this.bandwidth = bandwidth;
            }

            public double getTTL() {
                return TTL;
            }

            public void setTTL(double TTL) {
                this.TTL = TTL;
            }
        }

        private void send_map() throws IOException {
            JSONObject send_json = JSONObject.fromObject(this.send_map_data);
            String send_json_string = send_json.toString();
            byte[] send_json_byte = send_json_string.getBytes();
            this.send_json_stream.write(send_json_byte);
            this.send_json_stream.flush();
            //System.out.println("Broker->DRL: " + send_json_string);
            //String logstr = "Broker->DRL: " + send_json_string + "\n";
            //loger.write(logstr.getBytes());
        }


        private void parse_recv() throws IOException {
            JSONArray recvjsonArray = JSONArray.fromObject(recv_str);
            JSONObject recvjsonObj = null;
            DRLrecvreqs = null;
            DRLrecvreqs = new ArrayList<DRLThread.DRLrecv_req>();

            for (int i = 0; i < recvjsonArray.size(); i++) {
                recvjsonObj = recvjsonArray.getJSONObject(i);

                int req_id = recvjsonObj.getInt("req_id");

                JSONArray path_linksArray = recvjsonObj.getJSONArray("path_links");
                List<Integer> path_links = new ArrayList<>();
                for (int j = 0; j < path_linksArray.size(); j++) {
                    path_links.add(path_linksArray.getInt(j));
                }

                int f_start = recvjsonObj.getInt("f_start");
                int f_end = recvjsonObj.getInt("f_end");
                double TTL = recvjsonObj.getDouble("TTL");
                int src = recvjsonObj.getInt("src");
                int dst = recvjsonObj.getInt("dst");


                DRLThread.DRLrecv_req DRLrecv_req = new DRLThread.DRLrecv_req();
                DRLrecv_req.setReq_id(req_id);
                DRLrecv_req.setPath_links(path_links);
                DRLrecv_req.setF_start(f_start);
                DRLrecv_req.setF_end(f_end);
                DRLrecv_req.setTTL(TTL);
                DRLrecv_req.setSrc(src);
                DRLrecv_req.setDst(dst);
                DRLrecvreqs.add(DRLrecv_req);
            }
            //System.out.println("DRL->Broker: " + recv_str);
            //String logstr = "DRL->Broker: " + recv_str + "\n";
            //loger.write(logstr.getBytes());
        }


        @Override
        public void run() {
            try {
                BufferedReader recv = new BufferedReader(new InputStreamReader(client.getInputStream()));
                boolean run_flag = true;
                while (run_flag) {
                    recv_str = recv.readLine();
                    if (is_DRL_Ready == false) {
                        is_DRL_Ready = true;
                    }
                    else if (DRL_recv_Req_Status == DRL_RECV_OK) {
                        LockSupport.unpark(mainThread);
                    }
                    else if (DRL_recv_Req_Status == DRL_RECV_RESULT) {
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
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private class CTRLlerhread implements Runnable {
        private Socket client = null;
        private boolean is_Req_Sender = false;

        public boolean recv_ready = false;
        public String recv_str = null;
        public String send_str = null;
        public Map send_map_data = null;
        public List<Map> send_list_data = null;
        private DataOutputStream send_json_stream = null;
        private networkInfo networkInfo = null;

        public CTRLlerhread(Socket client, boolean is_Req_Sender, networkInfo networkInfo) throws IOException {
            this.client = client;
            this.is_Req_Sender = is_Req_Sender;
            this.send_json_stream = new DataOutputStream(client.getOutputStream());
            this.networkInfo = networkInfo;
        }

        private void send_map() throws IOException {
            JSONObject send_json = JSONObject.fromObject(this.send_map_data);
            String send_json_string = send_json.toString();
            send_json_string += "\n";
            byte[] send_json_byte = send_json_string.getBytes();
            this.send_json_stream.write(send_json_byte);
            this.send_json_stream.flush();
//            if(is_Req_Sender){System.out.println("Broker->CTRL1: " + send_json_string);}
//            else{System.out.println("Broker->CTRL2: " + send_json_string);}

//            if(is_Req_Sender){
//                String logstr = "Broker->CTRL1: " + send_json_string + "\n";
//                loger.write(logstr.getBytes());
//            }
//            else{
//                String logstr = "Broker->CTRL2: " + send_json_string + "\n";
//                loger.write(logstr.getBytes());
//            }
        }

        private void send_list() throws IOException {
            JSONArray send_json = JSONArray.fromObject(this.send_list_data);
            String send_json_string = send_json.toString();
            send_json_string += "\n";
            byte[] send_json_byte = send_json_string.getBytes();
            this.send_json_stream.write(send_json_byte);
            this.send_json_stream.flush();
        }

        private void parse_Recv_Req(networkInfo networkInfo) throws IOException{

            JSONArray recvjsonArray = JSONArray.fromObject(recv_str);
            JSONObject recvjsonObj = null;
            for (int i = 0; i < recvjsonArray.size(); i++) {
                recvjsonObj = recvjsonArray.getJSONObject(i);
                if (recvjsonObj.has("final_src")) {
                    req_id= Integer.valueOf(recvjsonObj.getInt("final_req_id"));
                    current_src = Integer.valueOf(recvjsonObj.getInt("final_src"));
                    current_dst = Integer.valueOf(recvjsonObj.getInt("final_dst"));
                    current_bandwidth = Integer.valueOf(recvjsonObj.getInt("final_bw"));
                    current_TTL = Double.valueOf(recvjsonObj.getDouble("final_TTL"));
                } else {
                    List<String> linkinfo = new ArrayList<>();
                    String link_id = String.valueOf(recvjsonObj.getInt("link_id"));
                    String src = String.valueOf(recvjsonObj.getInt("src"));
                    String dst = String.valueOf(recvjsonObj.getInt("dst"));
                    String len = String.valueOf(recvjsonObj.getInt("len"));
                    linkinfo.add(src);
                    linkinfo.add(dst);
                    linkinfo.add(len);
                    current_netTopology.put(String.valueOf(link_id), linkinfo);

                    char[] link_slot = recvjsonObj.getString("link_slot").toCharArray();
                    current_slot_map.put(String.valueOf(link_id), link_slot);

                }
            }
//            if(is_Req_Sender){System.out.println("CTRL1->Broker: " + recv_str);}
//            else{System.out.println("CTRL2->Broker: " + recv_str);}

//            if(is_Req_Sender){
//                String logstr = "CTRL1->Broker: " + recv_str + "\n";
//                loger.write(logstr.getBytes());
//            }
//            else{
//                String logstr = "CTRL2->Broker: " + recv_str + "\n";
//                loger.write(logstr.getBytes());
//            }
        }

        private void parse_recv(networkInfo networkInfo) throws IOException{

            JSONArray recvjsonArray = JSONArray.fromObject(recv_str);
            JSONObject recvjsonObj = null;
            for (int i = 0; i < recvjsonArray.size(); i++) {
                recvjsonObj = recvjsonArray.getJSONObject(i);
                if (recvjsonObj.has("final_src")) {
                    continue;
                } else {
                    List<String> linkinfo = new ArrayList<>();
                    String link_id = String.valueOf(recvjsonObj.getInt("link_id")+ networkInfo.link_offset);
                    String src = String.valueOf(recvjsonObj.getInt("src")+ networkInfo.node_offset) ;
                    String dst = String.valueOf(recvjsonObj.getInt("dst")+ networkInfo.node_offset) ;
                    String len = String.valueOf(recvjsonObj.getInt("len"));
                    linkinfo.add(src);
                    linkinfo.add(dst);
                    linkinfo.add(len);
                    current_netTopology.put(String.valueOf(link_id), linkinfo);

                    char[] link_slot = recvjsonObj.getString("link_slot").toCharArray();
                    current_slot_map.put(String.valueOf(link_id), link_slot);

                }
            }
//            if(is_Req_Sender){System.out.println("CTRL1->Broker: " + recv_str);}
//            else{System.out.println("CTRL2->Broker: " + recv_str);}
//            if(is_Req_Sender){
//                String logstr = "CTRL1->Broker: " + recv_str + "\n";
//                loger.write(logstr.getBytes());
//            }
//            else{
//                String logstr = "CTRL2->Broker: " + recv_str + "\n";
//                loger.write(logstr.getBytes());
//            }

        }

        @Override
        public void run() {
            try {
                BufferedReader recv = new BufferedReader(new InputStreamReader(client.getInputStream()));
                boolean run_flag = true;
                //assume CTRL1 is the request sender
                if (is_Req_Sender) {
                    while (run_flag) {
                        recv_str = recv.readLine();
                        if (is_CTRL1_Ready == false) {
                            is_CTRL1_Ready = true;
                        } else {
                            parse_Recv_Req(this.networkInfo);
                            LockSupport.unpark(mainThread);
                        }
                        LockSupport.park();
                        //blocking for send reward from mainthread
                        //will be unparked by mainthread after knowing send_str
                        send_map();//send reward
                    }
                }
                else {
                    while (run_flag) {
                        recv_str = recv.readLine();
                        if (is_CTRL2_Ready == false) {
                            is_CTRL2_Ready = true;
                        } else {
                            parse_recv(networkInfo);
                            LockSupport.unpark(mainThread);
                        }
                        LockSupport.park();
                        //blocking for send reward from mainthread
                        //will be unparked by mainthread after knowing send_str
                        send_map();
                    }
                }
                //send.close();
                client.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


    private class networkInfo {
        //{src,dst,idx,len(km)}
        //idx: <1000:domain1, >2000:domain2, 1000<>2000:inter domain
        //len: -1:unknown
        public List<List<Integer>> netTop_Domain1 = null;
        public List<List<Integer>> netTop_Domain2 = null;
        public int[][] netTopology_border = {
                {2, 19, 1000, 600},
                {19, 2, 1009, 600},

                {2, 28, 1001, 1200},
                {28, 2, 1010, 1200},

                {6, 23, 1002, 1200},
                {23, 6, 1011, 1200},

                {8, 19, 1003, 1400},
                {19, 8, 1012, 1400},

                {8, 24, 1004, 2000},
                {24, 8, 1013, 2000},

                {8, 28, 1005, 1400},
                {28, 8, 1014, 1400},

                {11, 19, 1006, 800},
                {19, 11, 1015, 800},

                {11, 23, 1007, 600},
                {23, 11, 1016, 600},

                {12, 28, 1008, 800},
                {28, 12, 1017, 800},

                //temp for test

//                {1, 2, 0, 1000},
//                {1, 6, 1, 2000},
//                {1, 8, 2, 1200},
//                {1, 11, 3, 1200},
//                {1, 12, 4, 800},
//
//                {19, 15, 2000, 1100},
//                {23, 15, 2001, 1200},
//                {24, 15, 2002, 1800},
//                {28, 15, 2003, 1400}
        };
        public int node_offset = 14;
        public int link_offset = 2000;

        public int[] bnodes_2domain2 = {2, 6, 8, 11, 12}; //border nodes from domain1 -> domain2
        public int[] bnodes_2domain1 = {19, 23, 24, 28}; //border nodes from domain2 -> domain1


        public int domain1_NODE_NUM = 14; //domain1 14nodes
        public int domain2_NODE_NUM = 14; //domain2 14nodes

        public int TOTAL_LINK_NUM = 0;

        public int SLOT_TOTAL = 100;

        public int lambda_req = 12;


        public void init_TopandSlotmap(){
            current_netTopology = new HashMap<>();
            current_netTopology.putAll(inter_netTopology);


            current_slot_map = new HashMap<>();
            current_slot_map.putAll(inter_slot_map);


        }

        private double rand_exp(double lambda) {
            Random r = new Random();
            double z = r.nextDouble();
            double result = -(1 / lambda) * Math.log(z);
            return result;
        }

        public double cal_resource_til(){
            double total_num = 0;
            double total_inuse_num = 0;
            for(char[] linkslot : inter_slot_map.values()){
                for(int i=0; i<linkslot.length; i++){
                    if(linkslot[i] == '0'){
                        total_inuse_num += 1;
                    }
                    total_num += 1;
                }
            }
            return total_inuse_num/total_num;
        }

        public Map pack_sendmsg2DRL() {

            Map<String, String> send_valuemap = new HashMap<String, String>();
            for (String link_id : current_slot_map.keySet()) {
                String link_slotmap = String.valueOf(current_slot_map.get(link_id));
                send_valuemap.put(link_id, link_slotmap);
            }
            send_valuemap.put("req_id", String.valueOf(req_id));
            send_valuemap.put("src", String.valueOf(current_src));
            send_valuemap.put("dst", String.valueOf(current_dst));
            send_valuemap.put("bandwidth", String.valueOf(current_bandwidth));
            send_valuemap.put("TTL", String.valueOf(current_TTL));

            //broker DRL need info about netTop/linkmap

            return send_valuemap;
        }

        public Map pack_sendmsg2CTRL2() {

            Map<String, String> send_valuemap = new HashMap<String, String>();
            send_valuemap.put("req_id", String.valueOf(req_id));

            //send_valuemap.put("src", String.valueOf(current_src));
            for (int i = 0; i < bnodes_2domain1.length; i++) {
                String bnodename = "bnode" + String.valueOf(i);
                if (bnodes_2domain1[i] != current_dst) {
                    send_valuemap.put(bnodename, String.valueOf(bnodes_2domain1[i] - node_offset));
                }
            }

            send_valuemap.put("dst", String.valueOf(current_dst - node_offset));
            send_valuemap.put("bandwidth", String.valueOf(current_bandwidth));
            send_valuemap.put("TTL", String.valueOf(current_TTL));


            return send_valuemap;
        }


        public Map pack_reward(DRLThread.DRLrecv_req DRLrecv_req, boolean is_Sender, boolean is_failed){
            Map<String, String> send_valuemap = new HashMap<>();
            if(DRLrecv_req != null) {
                List<Integer> path_links = DRLrecv_req.getPath_links();
                int f_start = DRLrecv_req.getF_start();
                int f_end = DRLrecv_req.getF_end();
                if (is_Sender) {
                    int link_id = path_links.get(0);
                    if (link_id >= 1000) {
                        //will be dropped by ctrl1
                        //this situation should not increase blocking
                        send_valuemap.put("Status", String.valueOf(-2));
                        send_valuemap.put("vlink_id", String.valueOf(-1));
                        send_valuemap.put("f_start", String.valueOf(f_start));
                        send_valuemap.put("f_end", String.valueOf(f_end));
                    }
                    else {
                        if (is_failed) {
                            send_valuemap.put("Status", String.valueOf(-1));
                            send_valuemap.put("vlink_id", String.valueOf(link_id));
                            send_valuemap.put("f_start", String.valueOf(f_start));
                            send_valuemap.put("f_end", String.valueOf(f_end));
                        } else {
                            send_valuemap.put("Status", String.valueOf(1));
                            send_valuemap.put("vlink_id", String.valueOf(link_id));
                            send_valuemap.put("f_start", String.valueOf(f_start));
                            send_valuemap.put("f_end", String.valueOf(f_end));
                        }
                    }

                } else {
                    int link_id = path_links.get(path_links.size() - 1);
                    if (link_id < 2000) {
                        send_valuemap.put("Status", String.valueOf(-2));
                        send_valuemap.put("vlink_id", String.valueOf(-1));
                        send_valuemap.put("f_start", String.valueOf(f_start));
                        send_valuemap.put("f_end", String.valueOf(f_end));
                    }
                    else {
                        if (is_failed) {
                            send_valuemap.put("Status", String.valueOf(-1));
                            send_valuemap.put("vlink_id", String.valueOf(link_id - link_offset));
                            send_valuemap.put("f_start", String.valueOf(f_start));
                            send_valuemap.put("f_end", String.valueOf(f_end));
                        } else {
                            send_valuemap.put("Status", String.valueOf(1));
                            send_valuemap.put("vlink_id", String.valueOf(link_id - link_offset));
                            send_valuemap.put("f_start", String.valueOf(f_start));
                            send_valuemap.put("f_end", String.valueOf(f_end));
                        }
                    }
                }
            }
            else{
                send_valuemap.put("Status", String.valueOf(-2));
                send_valuemap.put("vlink_id", String.valueOf(-2));
                send_valuemap.put("f_start", String.valueOf(-2));
                send_valuemap.put("f_end", String.valueOf(-2));
            }
            return send_valuemap;
        }

        public void release_slot_map(double time_to) {

            if (request_set.isEmpty() == false) {
                List<List> del_req = new ArrayList<>();
                for (int i = 0; i < request_set.size(); i++) {
                    List<List> req = request_set.get(i);
                    List<Double> new_TTL = req.get(4);//index 4 is TTL
                    new_TTL.set(0, new_TTL.get(0) - time_to);
                    req.get(4).set(0, new_TTL.get(0));

                    if (new_TTL.get(0) <= 0) {
                        List<Double> current_wp_link = req.get(1);
                        List<Double> current_fs = req.get(2);
                        List<Double> current_fe = req.get(3);
                        update_slot_map_for_releasing_wp(current_wp_link, current_fs, current_fe);
                        del_req.add(req);
                    }
                }

                for (int i = 0; i < del_req.size(); i++) {
                    List<List> req = del_req.get(i);
                    request_set.remove(req);
                }
            }

        }

        private void update_slot_map_for_releasing_wp(List<Double> current_wp_link, List<Double> current_fs, List<Double> current_fe) {
            int f_start = current_fs.get(0).intValue();
            int f_end = current_fe.get(0).intValue();
            for (int i = 0; i < current_wp_link.size(); i++) {
                int link_idx = current_wp_link.get(i).intValue();
                char[] linkslotmap = inter_slot_map.get(String.valueOf(link_idx));

                for (int f = f_start; f < f_end + 1; f++) {
                    if (linkslotmap[f] == '1') {
                        System.out.println("Release Slotmap Error");
                    }
                    linkslotmap[f] = '1';
                }

                inter_slot_map.put(String.valueOf(link_idx), linkslotmap);
            }
        }

        public void update_slot_map_for_commiting_wp(List<List> result_req) {
            if (result_req.isEmpty() == false) {
                List<Double> req_id = result_req.get(0);
                List<Double> current_wp_link = result_req.get(1);
                List<Double> current_fs = result_req.get(2);
                List<Double> current_fe = result_req.get(3);
                List<Double> current_TTL = result_req.get(4);

                int f_start = current_fs.get(0).intValue();
                int f_end = current_fe.get(0).intValue();
                for (int i = 0; i < current_wp_link.size(); i++) {
                    int link_idx = current_wp_link.get(i).intValue();
                    if(link_idx >= 1000 && link_idx <2000) {
                        char[] linkslotmap = inter_slot_map.get(String.valueOf(link_idx));

                        for (int f = f_start; f < f_end + 1; f++) {
                            if (linkslotmap[f] == '0') {
                                System.out.println("Update Slotmap Error");
                            }
                            linkslotmap[f] = '0';
                        }
                        inter_slot_map.put(String.valueOf(link_idx), linkslotmap);
                    }
                }

            }
        }

        public void update_Net_and_ReqSet(DRLThread.DRLrecv_req DRLrecv_req){
            //from return value
            List<Double> req_idx = new ArrayList<>();
            req_idx.add(Double.valueOf(DRLrecv_req.getReq_id()));

            List<Integer> path_links = DRLrecv_req.getPath_links();
            List<Double> new_path_links = new ArrayList<>();//only contain inter pathlinks
            for(int i=0; i<path_links.size(); i++){
                Integer path_id = path_links.get(i);
                if(path_id >= 1000 && path_id < 2000) {
                    new_path_links.add(Double.valueOf(path_id));
                }
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

        public networkInfo() {
            TOTAL_LINK_NUM = (domain1_NODE_NUM - 1) * bnodes_2domain2.length + (domain2_NODE_NUM - 1) * bnodes_2domain1.length;

            inter_slot_map = new HashMap<>();
            //add border link to slotmap
            for (int i = 0; i < netTopology_border.length; i++) {
                char[] link_slot = new char[SLOT_TOTAL];
                for (int j = 0; j < SLOT_TOTAL; j++) {
                    link_slot[j] = '1';
                }
                int link_id = netTopology_border[i][2];
                inter_slot_map.put(String.valueOf(link_id), link_slot);
            }

            inter_netTopology = new HashMap<>();
            //add border link to netTopology
            for (int i = 0; i < netTopology_border.length; i++) {
                List<String> linkinfo = new ArrayList<>();
                linkinfo.add(String.valueOf(netTopology_border[i][0])); //src node
                linkinfo.add(String.valueOf(netTopology_border[i][1])); //dst node
                linkinfo.add(String.valueOf(netTopology_border[i][3])); //link length
                int link_id = netTopology_border[i][2];
                inter_netTopology.put(String.valueOf(link_id), linkinfo);
            }


            request_set = new ArrayList<>();


        }


    }


    public void main(String[] args) throws Exception {
        //read cfg
        ReadCfg readCfg = new ReadCfg();

        myBroker maintest = new myBroker();
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



        is_CTRL1_Ready = true;
        is_CTRL2_Ready = false;

        //For both ctrl and DRL, broker is the server
        ServerSocket Server_DRL = new ServerSocket(Integer.parseInt(readCfg.Broker_Port_DRL));
        Socket Client_DRL = null;
        Client_DRL = Server_DRL.accept(); //blocking until DRL connected
        System.out.println("DRL Connected");
        DRLThread t_DRL = maintest.new DRLThread(Client_DRL);
        Thread t_DRL_th = new Thread(t_DRL);
        t_DRL_th.start();

        //For CTRLer2
        ServerSocket Server_CTRLler2 = new ServerSocket(Integer.parseInt(readCfg.Broker_Port_CTRL2));
        Socket Client_CTRLler2 = null;
        Client_CTRLler2 = Server_CTRLler2.accept();
        System.out.println("CTRLer2 Connected");
        CTRLlerhread t_CTRLler2 = maintest.new CTRLlerhread(Client_CTRLler2, false, networkInfo);
        Thread t_CTRLer2_th = new Thread(t_CTRLler2);
        t_CTRLer2_th.start();

        //For CTRLer1
        ServerSocket Server_CTRLler1 = new ServerSocket(Integer.parseInt(readCfg.Broker_Port_CTRL1));
        Socket Client_CTRLler1 = null;
        Client_CTRLler1 = Server_CTRLler1.accept();
        System.out.println("CTRLer1 Connected");
        CTRLlerhread t_CTRLler1 = maintest.new CTRLlerhread(Client_CTRLler1, true, networkInfo);
        Thread t_CTRLer1_th = new Thread(t_CTRLler1);
        t_CTRLer1_th.start();


        int total_num = 0;
        loger = new FileOutputStream("./log/Broker.log");
        while (true) {
            if (current_status == BROKER_NOT_READY) {
                while (is_DRL_Ready == false || is_CTRL1_Ready == false || is_CTRL2_Ready == false) {
                    Thread.sleep(100);
                }

                String total_info = "Total Reqs: " + String.valueOf(total_num);
                String logstr = "\n--------------------------------------------------------------------------------\n" + total_info + "\n";
                loger.write(logstr.getBytes());


                if(total_num % 2000 == 0){
                    System.out.println("Requests_Total: " + String.valueOf(total_num) );
                    System.out.println("Resource_util: " + String.valueOf(networkInfo.cal_resource_til()) + "\n");
                    logstr = "Resource_util: " + String.valueOf(networkInfo.cal_resource_til()) + "\n";
                    loger.write(logstr.getBytes());
                }

                networkInfo.release_slot_map(timeto[(int)total_num%100]);
                networkInfo.init_TopandSlotmap();
                total_num += 1;
                current_status = CTR1L00_CTR2L000_DRL00;
            }
            else if (current_status == CTR1L00_CTR2L000_DRL00) {
                LockSupport.park();
                //will be unparked by CTRLer1 thread after recv inter-domain request

                current_status = CTR1L10_CTR2L000_DRL00;
            }
            else if (current_status == CTR1L10_CTR2L000_DRL00) {
                t_CTRLler2.send_map_data = networkInfo.pack_sendmsg2CTRL2();
                current_status = CTR1L10_CTR2L100_DRL00;
                LockSupport.unpark(t_CTRLer2_th);
                LockSupport.park();
                //will be unparked by CTLer2 thread after recv slotmap&Top of domain2
                current_status = CTR1L10_CTR2L110_DRL00;
            }
            else if (current_status == CTR1L10_CTR2L110_DRL00) {
                //now broker know all info needed to calculate a final path
                //this status contains 2 send&recv
                //1stSend:current_Topology
                //2ndSend:Req&slotmap
                if (DRL_send_Req_Status == DRL_SEND_TOPOLOGY) {
                    DRL_send_Req_Status = DRL_SEND_REQLINKMAP;
                    DRL_recv_Req_Status = DRL_RECV_OK;
                    t_DRL.send_map_data = current_netTopology;
                    LockSupport.unpark(t_DRL_th);
                    LockSupport.park();
                    //will be unparked by DRLThread after recv OK
                }
                else if (DRL_send_Req_Status == DRL_SEND_REQLINKMAP) {
                    DRL_send_Req_Status = DRL_SEND_TOPOLOGY;
                    DRL_recv_Req_Status = DRL_RECV_RESULT;
                    t_DRL.send_map_data = networkInfo.pack_sendmsg2DRL();
                    LockSupport.unpark(t_DRL_th);
                    LockSupport.park();
                    //will be unparked by DRLThread after recv result;

                    current_status = CTR1L10_CTR2L110_DRL11;
                }
            }
            else if (current_status == CTR1L10_CTR2L110_DRL11) {
                //now know the final path
                DRLThread.DRLrecv_req DRLrecv_req = DRLrecvreqs.get(0);
                if(DRLrecv_req.getReq_id() == -2){
                    //no path available drop all
                    t_CTRLler1.send_map_data = networkInfo.pack_reward(null, true, true);
                    t_CTRLler2.send_map_data = networkInfo.pack_reward(null, false, true);
                }
                else if (DRLrecv_req.getReq_id() == -1){
                    //fail beacause of blocking
                    t_CTRLler1.send_map_data = networkInfo.pack_reward(DRLrecv_req, true, true);
                    t_CTRLler2.send_map_data = networkInfo.pack_reward(DRLrecv_req, false, true);
                }
                else{
                    //success
                    networkInfo.update_Net_and_ReqSet(DRLrecv_req);
                    t_CTRLler1.send_map_data = networkInfo.pack_reward(DRLrecv_req, true, false);
                    t_CTRLler2.send_map_data = networkInfo.pack_reward(DRLrecv_req, false, false);

                }

                is_CTRL2_Ready = false;
                DRL_recv_Req_Status = DRL_RECV_OK;
                LockSupport.unpark(t_CTRLer1_th);
                LockSupport.unpark(t_CTRLer2_th);
                //LockSupport.park();
                //LockSupport.park();
                //will be unparked when both CTRLer1, CTRLer2 send over ????
                current_status = BROKER_NOT_READY;
            }
        }

    }
}
