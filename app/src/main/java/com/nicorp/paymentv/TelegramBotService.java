package com.nicorp.paymentv;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;

public class TelegramBotService {

    private final TelegramBot bot;
    private final Context context;

    public TelegramBotService(String botToken, Context context) {
        this.bot = new TelegramBot(botToken);
        this.context = context;
    }

    public interface LoadingAnimationCallback {
        void onAnimationEnd();
    }

    public void sendMessageAndWait(long chatId, String message) {
        // Send message
        SendMessage request = new SendMessage(chatId, message);
        bot.execute(request);

        // Wait for "/update_auth_mobile" command
        bot.setUpdatesListener(updates -> {
            for (Update update : updates) {
                if (update.message() != null && update.message().text().contains("/update_auth_mobile")) {
                    // Check that time of the message is not older than 1 minute
                    long timeDiff = (System.currentTimeMillis() - update.message().date() * 1000L) / 1000L;
                    if (timeDiff < 15) {
                        Log.d("TelegramBot", "Received message: " + update.message().text());
                        // Handle the command here
                        System.out.println("Received /update_auth_mobile {"+update.message().text().replace("/update_auth_mobile ", "")+"}");
                        // You can call another method or perform actions based on the command
                        // Go to activity_check.xml
                        String[] parts = update.message().text().split(" ");
                        if (parts.length > 1 && parts[1].equals(message.split(" ")[1])) {
                            if (parts.length > 2 && parts[2].equals("true")) {
                                System.out.println("Success");
                                // Save key to SharedPreferences
                                SharedPreferences prefs = context.getSharedPreferences("com.nicorp.paymentv", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putString("key", message.split(" ")[1]);
                                editor.apply();
                                // Go to activity_check.xml
                                ((MainActivity) context).runOnUiThread(() -> ((MainActivity) context).setCheckPassed());
                                // Go to activity_check.xml
                                ((MainActivity) context).runOnUiThread(() -> ((MainActivity) context).tryOpenNewActivity());
                                bot.shutdown();
                                bot.removeGetUpdatesListener();
                            } else {
                                // Go to activity_check.xml
                                ((MainActivity) context).runOnUiThread(() -> ((MainActivity) context).setCheckPassed());

                                ((MainActivity) context).runOnUiThread(() -> ((MainActivity) context).setCheckAvailable());
                                // Go to activity_check.xml
                                ((MainActivity) context).runOnUiThread(() -> ((MainActivity) context).tryOpenNewActivity());
                                bot.shutdown();
                                bot.removeGetUpdatesListener();

                            }

                        bot.shutdown();
                        bot.removeGetUpdatesListener();

                        }
                    }
                    return UpdatesListener.CONFIRMED_UPDATES_ALL;
                }
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }
}