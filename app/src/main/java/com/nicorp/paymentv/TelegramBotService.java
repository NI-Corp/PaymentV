package com.nicorp.paymentv;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

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

    public void sendMessageAndWait(long chatId, String message) {
        // Send message
        SendMessage request = new SendMessage(chatId, message);
        bot.execute(request);

        // Wait for "/update_auth_mobile" command
        bot.setUpdatesListener(updates -> {
            for (Update update : updates) {
                if (update.message() != null && update.message().text().contains("/update_auth_mobile")) {
                    // Handle the command here
                    System.out.println("Received /update_auth_mobile {"+update.message().text().replace("/update_auth_mobile ", "")+"}");
                    // You can call another method or perform actions based on the command
                    // Go to activity_check.xml
                    if (update.message().text().split(" ")[1].equals(message.split(" ")[1])) {
                        if (update.message().text().split(" ")[2].equals("true")) {
                            System.out.println("Success");
                            // Save key to SharedPreferences
                            SharedPreferences prefs = context.getSharedPreferences("com.nicorp.paymentv", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("key", message.split(" ")[1]);
                            editor.apply();
                            // Go to activity_check.xml
                            Intent intent = new Intent(context, CheckActivity.class);
                            context.startActivity(intent);
                        } else {
                            System.out.println("Error");
                        }
                    }

                    return UpdatesListener.CONFIRMED_UPDATES_ALL;
                }
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }
}