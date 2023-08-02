package com.example.myapplicationserver;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class MainActivityServer extends AppCompatActivity {
    private TextView tvServerName, tvServerPort, tvStatus;
    private String serverIP = "10.0.2.16"; //check your own IP address in the emulator or real device
    private int serverPort = 1234; // choose your own port number, >1023, avoid reserved ones, like 8080...

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_server);

        tvServerName = findViewById(R.id.tvServerIP);
        tvServerPort = findViewById(R.id.tvServerPort);
        tvStatus = findViewById(R.id.tvStatusServer);

        tvServerName.setText(serverIP);
        tvServerPort.setText(String.valueOf(serverPort));
    }//on create

    private ServerThread serverThread;

    public void onClickStartServer (View view){
        serverThread = new ServerThread();
        serverThread.startServer();
    }

    public void onClickStopServer (View view){

        serverThread.stopServer();
    }

    class ServerThread extends Thread implements Runnable {
        private boolean serverRunning;
        private ServerSocket serverSocket;
        private int count = 0;

        public void startServer() {
            serverRunning = true;
            start();
        }

        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(serverPort);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvStatus.setText("Waiting for Clients");
                    }
                });

                while (serverRunning) {
                    Socket socket = serverSocket.accept();
                    count++;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvStatus.setText("Connected to " + socket.getInetAddress() + " : " + socket.getLocalPort());
                        }
                    });

                    PrintWriter outputServer = new PrintWriter(socket.getOutputStream());
                    outputServer.write("Welcome to server: " + count);
                    outputServer.flush();
                    socket.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public void stopServer() {
            serverRunning = false;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (serverSocket != null) {
                        try {
                            serverSocket.close();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tvStatus.setText("Server Stopped");
                                }
                            });
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }).start();
        }//stopserver

    }//serverthread
}//main