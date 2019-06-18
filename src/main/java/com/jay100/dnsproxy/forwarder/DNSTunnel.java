package com.jay100.dnsproxy.forwarder;

import com.jay100.dnsproxy.ConfigureLoad;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
/**
 * @author jay100
 */
public class DNSTunnel extends Thread{

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MMM.dd HH:mm:ss");
    private boolean active = false;
    private DatagramSocket clientSocket;

    private String remoteHost;
    private int remotePort;

    DatagramSocket serverSocket = null;
    public DNSTunnel( DatagramSocket clientSocket,String remoteHost,int remotePort)
    {
        this.remotePort = remotePort;
        this.remoteHost = remoteHost;
        this.clientSocket = clientSocket;
    }

    public void run() {

        System.out.println("allHosts:"+ConfigureLoad.getAllHosts());

        InetAddress address = null;
        try {
            serverSocket = new DatagramSocket();
            address = InetAddress.getByName(this.remoteHost);
            if(ConfigureLoad.isPrint)
            System.err.println("Cannot connect to UDP Destination on " + this.remoteHost);
        } catch (Exception e1) {
            e1.printStackTrace();
            return;
        }
        byte[] buffer = new byte[65536];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        String dateStr = sdf.format(new Date());
        while (true) {
            try {
                packet.setData(buffer);
                packet.setLength(buffer.length);
                clientSocket.receive(packet);
                //监听来着客户端的请求
                active = true;
                InetAddress sourceAddress = packet.getAddress();
                int srcPort = packet.getPort();
                if(ConfigureLoad.isPrint) {
                    System.out.println("\n\n\nget new UDP request,length==" + packet.getLength());
                    String result1 = new String(packet.getData(), 0, packet.getLength(), "ASCII");
                    System.out.println(dateStr + ": UDP Forwarding " + sourceAddress + ":" + srcPort + " <--> " + address.getHostAddress() + ":" + this.remotePort);
                    System.out.println("request content is ：：" + result1);
                }
                packet.setPort(this.remotePort);
                packet.setAddress(address);
                // 转发到远程服务器 解析
                Thread tunnel = new DNSForwarder(packet,clientSocket,sourceAddress,srcPort);
                tunnel.start();

            } catch (Throwable e) {
                e.printStackTrace();
                String remoteAddr = this.remoteHost + ":" + this.remotePort;
                if(ConfigureLoad.isPrint)
                System.err.println(dateStr + ": Failed to connect to remote host (" + remoteAddr + ")");
                connectionBroken();
            }
        }


}
    private String toStr(DatagramSocket socket) {
        String host = socket.getInetAddress().getHostAddress();
        int port = socket.getPort();
        return host + ":" + port;
    }
    public void close() {
        connectionBroken();
    }

    public synchronized void connectionBroken() {
        try {
            serverSocket.close();
        } catch (Exception e) {}
        try {
            clientSocket.close();
        } catch (Exception e) {}

        if (active) {
            String dateStr = sdf.format(new Date());
            if(ConfigureLoad.isPrint)
            System.out.println(dateStr+": DNS Forwarding " + toStr(clientSocket) + " <--> " + toStr(serverSocket) + " stopped.");

            active = false;
        }
    }
}
