package com.example.myapplicationserver;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class MainActivityServer extends AppCompatActivity {
    private TextView tvServerName, tvServerPort, tvStatus, tvMessageFromClient;
    private EditText etMessage;
    private ArrayList<Socket> connectedSockets; // Danh sách các Socket đã kết nối
    private String serverIP = "10.0.2.16"; //check your own IP address in the emulator or real device
    private int serverPort = 1234; // choose your own port number, >1023, avoid reserved ones, like 8080...

    private ServerThread serverThread;
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_server);

        tvServerName = findViewById(R.id.tvServerIP);
        tvServerPort = findViewById(R.id.tvServerPort);
        tvStatus = findViewById(R.id.tvStatusServer);
        etMessage = findViewById(R.id.etMessage);
        tvMessageFromClient = findViewById(R.id.tvMessageFromClient);

        tvServerName.setText(serverIP);
        tvServerPort.setText(String.valueOf(serverPort));
    }//on create


    public void onClickStartServer(View view) {
        serverThread = new ServerThread();
        Toast.makeText(this, "Server started", Toast.LENGTH_SHORT).show();
        serverThread.startServer();
    }

    public void onClickStopServer(View view) {

        serverThread.stopServer();
        Toast.makeText(this, "server stopped", Toast.LENGTH_SHORT).show();
    }

    public void onClickSend(View view) {
        //Lay noi dung tu edittext
        String messageToSend = etMessage.getText().toString();
        //gui noi dung toi tat ca client da ket noi
        if (serverThread != null && !messageToSend.isEmpty()) {
            serverThread.sendMessageToAllClient(messageToSend);
            Toast.makeText(this, "Send message to client successfully", Toast.LENGTH_SHORT).show();
            etMessage.setText("");
        } else {
            Toast.makeText(this, "Send message to client false", Toast.LENGTH_SHORT).show();
        }
    }

    class ServerThread extends Thread implements Runnable {
        private boolean serverRunning;
        private ServerSocket serverSocket;
        private int count = 0;

        public void startServer() {
            serverRunning = true;
            start();
        }


        public void stopServer() {
            serverRunning = false;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (serverSocket != null) {
                        try {
                            serverSocket.close();
//                            for (Socket socket : connectedSockets) {
//                                socket.close(); // Đóng tất cả các Socket đã kết nối
//                            }
//                            connectedSockets.clear(); // Xóa danh sách các Socket đã kết nối
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tvStatus.setText("Server Stopped");
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }//stopserver

        public void sendMessageToAllClient(String message) {
            if (serverSocket != null) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (ConnectedSockets connectedSockets1 : clientsList) {
                            connectedSockets1.sendMessageToClient(message);
                        }
                    }
                }).start();
            }
        }//sendmessage

        // mảng các thiết bị đã kết nối
        private ArrayList<ConnectedSockets> clientsList = new ArrayList<>();

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
                    ConnectedSockets client = new ConnectedSockets(socket);
                    client.start();
                    clientsList.add(client);
                    count++;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvStatus.setText("Connected to " + socket.getInetAddress() + " : " + socket.getLocalPort());
                        }
                    });

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        class ConnectedSockets extends Thread {
            private Socket clientSocket;
            private BufferedReader br_input;
            private PrintWriter output_Client;

            public ConnectedSockets(Socket socket) {
                clientSocket = socket;
                try {
                    // Lấy luồng đầu vào và luồng đầu ra của Client
                    br_input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    output_Client = new PrintWriter(clientSocket.getOutputStream(), true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // Phương thức gửi tin nhắn đến Client
            public void sendMessageToClient(String message) {
                output_Client.println(message);
            }

            @Override
            public void run() {
                try {
                    // Hiển thị tin nhắn từ Client lên giao diện
                    String messageFromClient;
                    while ((messageFromClient = br_input.readLine()) != null) {
                        // Hiển thị tin nhắn từ Client lên giao diện
                        final String finalMessage = messageFromClient;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvMessageFromClient.setText("Tin nhắn từ client: " + finalMessage);
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }//class connected socket
    }//serverthread
}//main