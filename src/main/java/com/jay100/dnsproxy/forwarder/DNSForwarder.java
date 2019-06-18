package com.jay100.dnsproxy.forwarder;

import com.jay100.dnsproxy.ConfigureLoad;
import org.xbill.DNS.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
/**
 *
 * @author jay100
 */
public class DNSForwarder extends Thread{
    private InetAddress clientAddr;
    private int clientPort;
    private DatagramSocket serverSocket;
    private DatagramSocket clientSocket;
    private DatagramPacket sendData;


    public DNSForwarder(DatagramPacket packet, DatagramSocket clientSocket, InetAddress clientAddr, int clientPort){
        try {
            this.clientAddr = InetAddress.getByAddress(clientAddr.getAddress());
            this.clientPort = clientPort;
            this.clientSocket = clientSocket;
            byte[] sendmsg = Arrays.copyOf(packet.getData(),packet.getLength());
            sendData = new DatagramPacket(sendmsg,packet.getLength(),packet.getSocketAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            //解析客户端dns的请求数据
            Message req = new Message(sendData.getData());
            //获取域名
            String domain = req.getQuestion().getName().toString();
            String proxyAddress = ConfigureLoad.getHostByDomain(domain); //如果存在本地域名，则直接返回本地host 映射
            if(proxyAddress!=null && req.getQuestion().getType()==Type.A){
                Message outdata = (Message)req.clone();
                Record reqQuestion = req.getQuestion();
                InetAddress answerIpAddr = Address.getByName(proxyAddress);
                Record answer = new ARecord(reqQuestion.getName(), reqQuestion.getDClass(), reqQuestion.getTTL(), answerIpAddr);
                outdata.addRecord(answer, Section.ANSWER);
                byte[] buf = outdata.toWire();
                DatagramPacket response = new DatagramPacket(buf,0,buf.length);

                response.setAddress(clientAddr);
                response.setPort(clientPort);
                clientSocket.send(response);
                return;
            }

            // 从远程服务器接收数据
            DatagramPacket response = new DatagramPacket(new byte[12800], 12800);
            serverSocket = new DatagramSocket();
            serverSocket.setSoTimeout(5000);
            serverSocket.send(sendData);
            if(serverSocket.isClosed())return;
            serverSocket.receive(response);

            //发送数据包给客户端
            response.setAddress(clientAddr);
            response.setPort(clientPort);
            clientSocket.send(response);
            close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close(){
        if(serverSocket.isClosed())return;
        serverSocket.close();
    }
}
