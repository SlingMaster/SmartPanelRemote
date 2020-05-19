/*
 * Copyright (c) 2020.
 * Jeneral Samopal Company
 * Design and Programming by Alex Dovby
 */

package com.jsc.smartpanelremote;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

class TCPClient {
    private OnMessageReceived mMessageListener = null;
    private boolean mRun = false;
    private PrintWriter out;

    //OnMessagedReceived listens Connection state msg
    TCPClient(OnMessageReceived listener) {
        mMessageListener = listener;
    }

    void run(String ip, int port, String sendData) {
        try {
            //here you must put your computer's IP address.
            InetAddress serverAddr = InetAddress.getByName(ip);
            Log.e("TCP Client", "C: Connecting...");
            //create a socket to make the connection with the server
            Log.e("TCP Client", serverAddr + ":" + port);
            Socket socket = new Socket(serverAddr, port);
            //send the message to the server
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
            Log.e("TCP Client", "Sent Done");
            out.println(sendData);
            socket.close();
        } catch (Exception e) {
            Log.e("TCP", "Error", e);
            mMessageListener.messageReceived("Connect Error");
        } finally {
            // the socket must be closed. It is not possible to reconnect to this socket
            // after it is closed, which means a new socket instance has to be created.
            mMessageListener.messageReceived("Client Connect Close");
        }
    }

    //  Declare the interface. The method messageReceived(String message)
    //  will must be implemented in the MainActivity
    //  class at on asynckTask doInBackground
    public interface OnMessageReceived {
        public void messageReceived(String message);
    }
}