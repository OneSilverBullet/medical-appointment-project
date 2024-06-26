package Sequencer.Sequencer;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import Replica2.com.service.dhms.Appointment;
import Sequencer.Sequencer.com.example.webservice.Center;

public class Sequencer {
    // Port numbers for server replicas
    private static final int[] REPLICA_PORTS = {9001, 9002, 9003, 9004};
    // IP addresses of server replicas
    private static final String[] REPLICA_IPS = {"replica1_ip", "replica2_ip", "replica3_ip", "replica4_ip"};
    private static final AtomicInteger sequenceNumber = new AtomicInteger(0);

    private static Sequencer instance = null;
    public static Sequencer Instance() {
        if(instance == null){
            instance = new Sequencer();
        }
        return instance;
    }

    //replica 1
    public Center replica1;

    //replica 2
    public Appointment replica2_mtl;
    public Appointment replica2_que;
    public Appointment replica2_she;

    //replica 3
    public Appointment replica3_mtl;
    public Appointment replica3_que;
    public Appointment replica3_she;

    //replica 4
    public Appointment replica4_mtl;
    public Appointment replica4_que;
    public Appointment replica4_she;

    public Appointment Replica_default(int index){
        if(index == 2) return replica2_mtl;
        else if(index == 3) return replica3_mtl;
        else return replica4_mtl;
    }

    public Appointment Replica_userID(int index, String userID)
    {
        Type.UserEntity userInstance=  new Type.UserEntity();
        userInstance.DeserializeUser(userID);
        if(userInstance.city == Type.CityType.MTL){

            if(index == 2) return replica2_mtl;
            else if(index == 3) return replica3_mtl;
            else return replica4_mtl;
        }
        else if(userInstance.city == Type.CityType.QUE){
            if(index == 2) return replica2_que;
            else if(index == 3) return replica3_que;
            else return replica4_que;
        }
        else {
            if(index == 2) return replica2_she;
            else if(index == 3) return replica3_she;
            else return replica4_she;
        }
    }

    public Appointment Replica_appID(int index, String appID)
    {
        Type.AppointmentEntity appInstance = new Type.AppointmentEntity();
        appInstance.DeserializeAppointmentEntity(appID);
        if(appInstance.city == Type.CityType.MTL){
            System.out.println("replica user id:" + replica2_mtl);
            if(index == 2) return replica2_mtl;
            else if(index == 3) return replica3_mtl;
            else return replica4_mtl;
        }
        else if(appInstance.city == Type.CityType.QUE){
            System.out.println("replica user id:" + replica2_mtl);
            if(index == 2) return replica2_que;
            else if(index == 3) return replica3_que;
            else return replica4_que;
        }
        else{
            System.out.println("replica user id:" + replica2_mtl);
            if(index == 2) return replica2_she;
            else if(index == 3) return replica3_she;
            else return replica4_she;
        }
    }

    static public List<String> SplitMessage(String mess, Type.AppointmentType appType){
        String typeStr1 = "PHYS";
        String typeStr2 = "SURG";
        String typeStr3 = "DENT";
        String curType = typeStr1;
        if(appType == Type.AppointmentType.SURG){
            curType = typeStr2;
        }
        else if(appType == Type.AppointmentType.DENT){
            curType = typeStr3;
        }
        List<String> ret = new ArrayList<>();
        mess = mess.substring(1, mess.length()-1);
        //System.out.println(mess.length());
        if(mess.length() == 0) return ret;
        String[] clips = mess.split(",");
        for(int i = 0; i < clips.length; i++) {
            String tmp = curType + ":" + clips[i].trim();
            ret.add(tmp);
        }
        return ret;
    }

    static public String MergeAllResults(String phys, String surg, String dent){
        List<String> physicsStr = SplitMessage(phys, Type.AppointmentType.PHYS);
        List<String> surgenStr = SplitMessage(surg, Type.AppointmentType.SURG);
        List<String> dentsStr = SplitMessage(dent, Type.AppointmentType.DENT);

        String res = "{";
        for(int i = 0; i < physicsStr.size(); i++) {
            res += physicsStr.get(i);
            if(!(i == physicsStr.size() - 1 && (surgenStr.size() == 0) && (dentsStr.size() == 0)))
                res += ", ";
        }
        for(int i = 0; i < surgenStr.size(); i++) {
            res += surgenStr.get(i);
            if(!(i == physicsStr.size() - 1 && (dentsStr.size() == 0)))
                res += ", ";
        }
        for(int i = 0; i < dentsStr.size(); i++) {
            res += dentsStr.get(i);
            if(i != dentsStr.size()-1) {
                res += ", ";
            }
        }
        res += "}";
        return res;
    }

    private Sequencer() {
        try {
            //replica1: Yulin
            String replica1_ip = "172.20.10.4";
            URL url = new URL("http://"+replica1_ip+":8080/center?wsdl");
            QName qName = new QName("http://webservice.example.com.Replica1/", "CenterImplService");
            QName qnamec2 = new QName("http://webservice.example.com.Replica1/", "CenterImplPort");
            Service service = Service.create(url, qName);
            replica1 = service.getPort(qnamec2, Center.class);

            //replica2: Yajing
            {
                String replica2_ip = "172.20.10.9";
                URL urlMTL = new URL("http://" + replica2_ip + ":8080/appointment/mtl?wsdl");
                QName qnameMTL = new QName("http://dhms.service.com.Replica2/", "MontrealServerService");
                Service serviceMTL = Service.create(urlMTL, qnameMTL);
                QName qnameMTL2 = new QName("http://dhms.service.com.Replica2/", "MontrealServerPort");
                replica2_mtl = serviceMTL.getPort(qnameMTL2, Appointment.class);
                System.out.println("initialize:" + replica2_mtl);

                URL urlQUE = new URL("http://" + replica2_ip + ":8080/appointment/que?wsdl");
                QName qnameQUE = new QName("http://dhms.service.com.Replica2/", "QuebecServerService");
                Service serviceQUE = Service.create(urlQUE, qnameQUE);
                QName qnameQUE2 = new QName("http://dhms.service.com.Replica2/", "QuebecServerPort");
                replica2_que = serviceQUE.getPort(qnameQUE2, Appointment.class);

                URL urlSHE = new URL("http://" + replica2_ip + ":8080/appointment/she?wsdl");
                QName qnameSHE = new QName("http://dhms.service.com.Replica2/", "SherbrookeServerService");
                Service serviceSHE = Service.create(urlSHE, qnameSHE);
                QName qnameSHE2 = new QName("http://dhms.service.com.Replica2/", "SherbrookeServerPort");
                replica2_she = serviceSHE.getPort(qnameSHE2, Appointment.class);
            }

            //replica3: Mridul
            {
                String replica3_ip = "172.20.10.3"; // Need input replica 3 ip address

                URL urlMTL = new URL("http://" + replica3_ip + ":8080/appointment/mtl?wsdl");
                QName qnameMTL = new QName("http://servers.Replica3/", "HospitalMTLService");
                Service serviceMTL = Service.create(urlMTL, qnameMTL);
                QName qnameMTL2 = new QName("http://servers.Replica3/", "HospitalMTLPort");
                replica3_mtl = serviceMTL.getPort(qnameMTL2, Appointment.class);
                // mtl.addAppointment(appointmentID, appointmentType, capacity);

                URL urlQUE = new URL("http://" + replica3_ip + ":8080/appointment/que?wsdl");
                QName qnameQUE = new QName("http://servers.Replica3/", "HospitalQUEService");
                Service serviceQUE = Service.create(urlQUE, qnameQUE);
                QName qnameQUE2 = new QName("http://servers.Replica3/", "HospitalQUEPort");
                replica3_que = serviceQUE.getPort(qnameQUE2, Appointment.class);

                URL urlSHE = new URL("http://" + replica3_ip + ":8080/appointment/she?wsdl");
                QName qnameSHE = new QName("http://servers.Replica3/", "HospitalSHEService");
                Service serviceSHE = Service.create(urlSHE, qnameSHE);
                QName qnameSHE2 = new QName("http://servers.Replica3/", "HospitalSHEPort");
                replica3_she = serviceSHE.getPort(qnameSHE2, Appointment.class);
            }

            //replica4: Yuhang
            {
                String replica4_ip = "172.20.10.10"; // Need input replica 4 ip address

                URL urlMTL = new URL("http://"+replica4_ip+":8080/appointment/mtl?wsdl");
                QName qnameMTL = new QName("http://main.Replica4/", "ServerMTLService");
                Service serviceMTL = Service.create(urlMTL, qnameMTL);
                QName qnameMTL2 = new QName("http://main.Replica4/", "ServerMTLPort");
                replica4_mtl = serviceMTL.getPort(qnameMTL2, Appointment.class);
                // mtl.addAppointment(appointmentID, appointmentType, capacity);

                URL urlQUE = new URL("http://"+replica4_ip+":8080/appointment/que?wsdl");
                QName qnameQUE = new QName("http://main.Replica4/", "ServerQUEService");
                Service serviceQUE = Service.create(urlQUE, qnameQUE);
                QName qnameQUE2 = new QName("http://main.Replica4/", "ServerQUEPort");
                replica4_que = serviceQUE.getPort(qnameQUE2, Appointment.class);

                URL urlSHE = new URL("http://"+replica4_ip+":8080/appointment/she?wsdl");
                QName qnameSHE = new QName("http://main.Replica4/", "ServerSHEService");
                Service serviceSHE = Service.create(urlSHE, qnameSHE);
                QName qnameSHE2 = new QName("http://main.Replica4/", "ServerSHEPort");
                replica4_she = serviceSHE.getPort(qnameSHE2, Appointment.class);
            }
        }
        catch (Exception e) {
            System.out.println("Client exception: " + e);
            e.printStackTrace();
        }
    }

    //ip:port:bookAppointment:userID,appointmentID,appointmentType
    public List<String> UnmarshallingFrontEndRequest(String requestStr)
    {
        List<String> results = new ArrayList<>();
        String[] splittedRequest = requestStr.split(":");
        results.add(splittedRequest[0]);
        if(splittedRequest.length > 1){
            String[] splittedParams = splittedRequest[1].split(",");
            for(int i=0; i<splittedParams.length; ++i){
                results.add(splittedParams[i]);
            }
        }
        return results;
    }

    public static void main(String[] args) {

        try {
            DatagramSocket sequencerSocket = new DatagramSocket(8000);
            while (true) {
                //receive the data from the front-end
                byte[] receiveData = new byte[1024];

                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                sequencerSocket.receive(receivePacket);
                String frontendRequest = new String(receivePacket.getData()).trim();
                System.out.println("the raw request:" + frontendRequest);
                List<String> requestInformations = Sequencer.Instance().UnmarshallingFrontEndRequest(frontendRequest);

                String frontEndMethod = requestInformations.get(0);


                System.out.println("the method:" + frontEndMethod );

                // Assign unique sequence number
                int seqNum = sequenceNumber.getAndIncrement();

                //call the constructor
                Sequencer.Instance();

                String resCode = "";
                if(frontEndMethod.equals("IsValidUserName")){
                    boolean replica1Res =  Sequencer.Instance().replica1.CheckUser((short)Integer.parseInt(requestInformations.get(1)),
                            requestInformations.get(2));
                    resCode = replica1Res + "&" + replica1Res + "&" + replica1Res + "&" + replica1Res;
                }
                else if(frontEndMethod.equals("RegisterUser")){

                    String replica1Res =  Sequencer.Instance().replica1.RegisterUser((short)Integer.parseInt(requestInformations.get(1)),
                            (short)Integer.parseInt(requestInformations.get(2)));
                    resCode = replica1Res + "&" + replica1Res + "&" + replica1Res + "&" + replica1Res;
                }
                else if(frontEndMethod.equals("BookAppointment")){
                    //todo: replica 1, 2, 3, 4
                    String replica1Res = Sequencer.Instance().replica1.bookAppointment(requestInformations.get(1), requestInformations.get(2), requestInformations.get(3));
                    String replica2Res = Sequencer.Instance().Replica_userID(2, requestInformations.get(1)).bookAppointment(requestInformations.get(1), requestInformations.get(2), requestInformations.get(3));
                    String replica3Res = Sequencer.Instance().Replica_userID(3, requestInformations.get(1)).bookAppointment(requestInformations.get(1), requestInformations.get(2), requestInformations.get(3));
                    String replica4Res = Sequencer.Instance().Replica_userID(4, requestInformations.get(1)).bookAppointment(requestInformations.get(1), requestInformations.get(2), requestInformations.get(3));
                    resCode = replica1Res + "&" + replica2Res + "&" + replica3Res + "&" + replica4Res;
                }
                else if(frontEndMethod.equals("CancelAppointment")){
                    String replica1Res = Sequencer.Instance().replica1.cancelAppointment(requestInformations.get(1), requestInformations.get(2));
                    String replica2Res = Sequencer.Instance().Replica_userID(2, requestInformations.get(1)).cancelAppointment(requestInformations.get(1), requestInformations.get(2));
                    String replica3Res = Sequencer.Instance().Replica_userID(3, requestInformations.get(1)).cancelAppointment(requestInformations.get(1), requestInformations.get(2));
                    String replica4Res = Sequencer.Instance().Replica_userID(4, requestInformations.get(1)).cancelAppointment(requestInformations.get(1), requestInformations.get(2));
                    resCode = replica1Res + "&" + replica2Res + "&" + replica3Res + "&" + replica4Res;
                }
                else if(frontEndMethod.equals("ViewBookedAppointments")){
                    String replica1Res = Sequencer.Instance().replica1.getAppointmentSchedule(requestInformations.get(1));
                    String replica2Res = Sequencer.Instance().Replica_userID(2, requestInformations.get(1)).getAppointmentSchedule(requestInformations.get(1));
                    String replica3Res = Sequencer.Instance().Replica_userID(3, requestInformations.get(1)).getAppointmentSchedule(requestInformations.get(1));
                    String replica4Res = Sequencer.Instance().Replica_userID(4, requestInformations.get(1)).getAppointmentSchedule(requestInformations.get(1));
                    resCode = replica1Res + "&" + replica2Res + "&" + replica3Res + "&" + replica4Res;
                }
                else if(frontEndMethod.equals("AddAppointment")){
                    String replica1Res = Sequencer.Instance().replica1.addAppointment(requestInformations.get(1), requestInformations.get(2), Integer.parseInt(requestInformations.get(3)));
                    System.out.println("the parameters:" + requestInformations.get(1) + requestInformations.get(2) + Integer.parseInt(requestInformations.get(3)));
                    String replica2Res = Sequencer.Instance().Replica_appID(2, requestInformations.get(1)).addAppointment(requestInformations.get(1), requestInformations.get(2), Integer.parseInt(requestInformations.get(3)));
                    String replica3Res = Sequencer.Instance().Replica_appID(3, requestInformations.get(1)).addAppointment(requestInformations.get(1), requestInformations.get(2), Integer.parseInt(requestInformations.get(3)));
                    String replica4Res = Sequencer.Instance().Replica_appID(4, requestInformations.get(1)).addAppointment(requestInformations.get(1), requestInformations.get(2), Integer.parseInt(requestInformations.get(3)));
                    resCode = replica1Res + "&" + replica2Res + "&" + replica3Res + "&" + replica4Res;
                }
                else if(frontEndMethod.equals("RemoveAppointment")){
                    String replica1Res = Sequencer.Instance().replica1.removeAppointment(
                            requestInformations.get(1),
                            requestInformations.get(2)
                    );
                    String replica2Res = Sequencer.Instance().Replica_appID(2, requestInformations.get(1)).removeAppointment(requestInformations.get(1), requestInformations.get(2));
                    String replica3Res = Sequencer.Instance().Replica_appID(3, requestInformations.get(1)).removeAppointment(requestInformations.get(1), requestInformations.get(2));
                    String replica4Res = Sequencer.Instance().Replica_appID(4, requestInformations.get(1)).removeAppointment(requestInformations.get(1), requestInformations.get(2));
                    resCode = replica1Res + "&" + replica2Res + "&" + replica3Res + "&" + replica4Res;
                }
                else if(frontEndMethod.equals("ViewAvailableAppointments")){
                    String changedPhys = Type.ExchangeAppointTypeCompatibility(Type.AppointmentType.PHYS);
                    String changedSurg = Type.ExchangeAppointTypeCompatibility(Type.AppointmentType.SURG);
                    String changedDent = Type.ExchangeAppointTypeCompatibility(Type.AppointmentType.DENT);
                    String replica1Res = Sequencer.Instance().replica1.listAppointmentAvailability(changedPhys);

                    String replica2Res1 = Sequencer.Instance().Replica_default(2).listAppointmentAvailability(changedPhys);
                    String replica2Res2 = Sequencer.Instance().Replica_default(2).listAppointmentAvailability(changedSurg);
                    String replica2Res3 = Sequencer.Instance().Replica_default(2).listAppointmentAvailability(changedDent);
                    String replica2 = MergeAllResults(replica2Res1, replica2Res2, replica2Res3);

                    String replica3Res1 = Sequencer.Instance().Replica_default(3).listAppointmentAvailability(changedPhys);
                    String replica3Res2 = Sequencer.Instance().Replica_default(3).listAppointmentAvailability(changedSurg);
                    String replica3Res3 = Sequencer.Instance().Replica_default(3).listAppointmentAvailability(changedDent);
                    String replica3 = MergeAllResults(replica3Res1, replica3Res2, replica3Res3);

                    String replica4Res1 = Sequencer.Instance().Replica_default(4).listAppointmentAvailability(changedPhys);
                    String replica4Res2 = Sequencer.Instance().Replica_default(4).listAppointmentAvailability(changedSurg);
                    String replica4Res3 = Sequencer.Instance().Replica_default(4).listAppointmentAvailability(changedDent);
                    String replica4 = MergeAllResults(replica4Res1, replica4Res2, replica4Res3);

                    resCode = replica1Res + "&" + replica2 + "&" + replica3 + "&" + replica4;
                }
                else if(frontEndMethod.equals("SwapAppointment")){
                    String replica1Res = Sequencer.Instance().replica1.swapAppointment(
                            requestInformations.get(1),
                            requestInformations.get(2),
                            requestInformations.get(3),
                            requestInformations.get(4),
                            requestInformations.get(5)
                    );
                    String replica2Res = Sequencer.Instance().Replica_userID(2, requestInformations.get(1)).swapAppointment(
                            requestInformations.get(1),
                            requestInformations.get(2),
                            requestInformations.get(3),
                            requestInformations.get(4),
                            requestInformations.get(5)
                    );
                    String replica3Res = Sequencer.Instance().Replica_userID(3, requestInformations.get(1)).swapAppointment(
                            requestInformations.get(1),
                            requestInformations.get(2),
                            requestInformations.get(3),
                            requestInformations.get(4),
                            requestInformations.get(5)
                    );
                    String replica4Res = Sequencer.Instance().Replica_userID(4, requestInformations.get(1)).swapAppointment(
                            requestInformations.get(1),
                            requestInformations.get(2),
                            requestInformations.get(3),
                            requestInformations.get(4),
                            requestInformations.get(5)
                    );
                    resCode = replica1Res + "&" + replica2Res + "&" + replica3Res + "&" + replica4Res;
                }
                System.out.println("Sent Data:" + resCode);
                DatagramPacket reply = new DatagramPacket(resCode.getBytes(), resCode.length(),
                        receivePacket.getAddress(), receivePacket.getPort());
                sequencerSocket.send(reply);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
