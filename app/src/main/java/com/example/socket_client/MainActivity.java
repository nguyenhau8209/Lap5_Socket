package com.example.socket_client;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    private TextView tvReceivedData, tvShowMessage;
    private EditText etServerName, etServerPort, etSendMessageToServer;
    private Button buttonClientConnect;
    private String serverName;
    private int serverPort;
    Socket socket;
    private Boolean connect = false;
    BufferedReader br_input;
    private PrintWriter output_writer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvReceivedData = findViewById(R.id.tvReceivedData);
        tvShowMessage = findViewById(R.id.tvshowtinnhan);
        etServerName = findViewById(R.id.edtServerIP);
        etServerPort = findViewById(R.id.edtServerPort);
        buttonClientConnect = findViewById(R.id.btnConnect);
        etSendMessageToServer = findViewById(R.id.etSendMessageToServer);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("notification", "notification", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }//on create

    public void onClickConnect(View view) {
        serverName = etServerName.getText().toString();
        serverPort = Integer.valueOf(etServerPort.getText().toString());

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new Socket(serverName, serverPort);


                    br_input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    output_writer = new PrintWriter(socket.getOutputStream(), true);
                    // Cập nhật giao diện khi kết nối thành công
//
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvReceivedData.setText("Connected to Server");
                        }
                    });

                    //nhan tin nhan tu server va hien thi len giao dien
                    String messageFromServer;
                    while ((messageFromServer = br_input.readLine()) != null) {
                        final String messageFinal = messageFromServer;
                        Log.d("TAG", "run: " + messageFinal);
                        if (messageFinal != null) {
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this, "notification");
                            builder.setContentTitle("Message from Server");
                            builder.setContentText(messageFinal);
                            builder.setSmallIcon(R.drawable.baseline_notifications_active_24);
                            builder.setAutoCancel(true);

                            NotificationManagerCompat managerCompat = NotificationManagerCompat.from(MainActivity.this);
                            if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                // TODO: Consider calling
                                //    ActivityCompat#requestPermissions
                                // here to request the missing permissions, and then overriding
                                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                //                                          int[] grantResults)
                                // to handle the case where the user grants the permission. See the documentation
                                // for ActivityCompat#requestPermissions for more details.
                                return;
                            }
                            managerCompat.notify(1, builder.build());
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvShowMessage.setText("Message from server: " + messageFinal);

                            }
                        });
                    }

                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }//onClickConnect

    public void onClickSendMessageToServer(View view) {
        String messageToSend = etSendMessageToServer.getText().toString();
        if (!messageToSend.isEmpty()) {
            sendMessageToServer(messageToSend);
            Toast.makeText(this, "Send message to server successfully", Toast.LENGTH_SHORT).show();
            etSendMessageToServer.setText("");
        } else {
            Toast.makeText(this, "Send message to server false", Toast.LENGTH_SHORT).show();
        }
    }//onclick sendmessagetoserver

    public void sendMessageToServer(String message) {
        if (output_writer != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    output_writer.println(message);
                }
            }).start();
        }
    }//sendMessageToServer


}
