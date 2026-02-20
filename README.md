# App Components:
আপনার প্রোজেক্টের ডানদিকের ওপরের ৩-ডট মেনু থেকে Library-তে যান এবং Appcompat and Design অন করুন।

# Permission : 
```
FOREGROUND_SERVICE
POST_NOTIFICATIONS
INTERNET
READ_MEDIA_IMAGES
READ_EXTERNAL_STORAGE
```

# Android Manifest Manager

Application ট্যাগের ভেতরে এই লাইনটি যোগ করুন 

১. res: এর যায়গায় লিখুন app 

২. attr: এর যায়গায় লিখুন android:requestLegacyExternalStorage

৩. value: এর যায়গায় লিখুন true

# add source directly :

onCreate ইভেন্টের ভেতর অন্য কোনো ব্লক যেন এই কোডটির নিচে না থাকে।

# ভেরিয়েবল চেক : 

​String botToken = "Entar Your Bot Token";
String chatId = "Entar your Chat I'd";

