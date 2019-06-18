package com.jay100.dnsproxy;

import com.jay100.dnsproxy.forwarder.DNSTunnel;

import java.net.DatagramSocket;
import java.net.SocketException;

/**
 *
 * @author jay100
 */
public class StartServer {

    private int listenPort = 53;
    private String remoteHost = "8.8.8.8";
    private int remotePort = 53;

    public static void main(String[] args) {

        StartServer server = new StartServer();

        if(args.length>0){
            server.remoteHost = args[0];
            server.remotePort = Integer.parseInt(args[1]);
            if(args.length==3 && args[2].equals("--print")) ConfigureLoad.isPrint=true;
        }
        server.start();

    }
    private void start(){
        try {
            DatagramSocket udpServerSocket = new DatagramSocket(this.listenPort);
            DNSTunnel tunnel = new DNSTunnel(udpServerSocket,this.remoteHost,this.remotePort);
            tunnel.start();
        } catch (SocketException e) {
            e.printStackTrace();
        }

    }
}
