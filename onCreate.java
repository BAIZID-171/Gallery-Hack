Intent intent = new Intent(this, TelegramManager.BackgroundService.class);
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    startForegroundService(intent);
} else {
    startService(intent);
}
