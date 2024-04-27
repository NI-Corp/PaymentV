package com.nicorp.paymentv;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.os.AsyncTask;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.widget.TextView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String TELEGRAM_TOKEN = "7180668744:AAGX346vRjqsJX_LUNo48FYZdHsy785fU2U";
    private static final int CHAT_ID = 947630051;
    private String key;
    private boolean animationFinished = false;
    private boolean checkPassed = false;
    private boolean checkAvailable = true;
    private ConstraintLayout loadingPB;
    private AnimationSet animation;
    private TextView loadingText;


    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    @SuppressLint("StaticFieldLeak")
    private void handleIntent(Intent intent) {
        String appLinkAction = intent.getAction();
        Uri appLinkData = intent.getData();
        Log.d("AppLinkAction", String.valueOf(appLinkData));
        if (Intent.ACTION_VIEW.equals(appLinkAction) && appLinkData != null){
            Log.d("AppData", String.valueOf(appLinkData));
            // Get key from appLinkData https://paymentv.netlify.app/main?key=12345678
            key = appLinkData.getQueryParameter("key");
            Log.d("Key", key);

            TelegramBotService botService = new TelegramBotService(TELEGRAM_TOKEN, this);
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    botService.sendMessageAndWait(CHAT_ID, "/check_auth_mobile " + key);
                    return null;
                }
            }.execute();
        }
    }

    @SuppressLint("StaticFieldLeak")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        handleIntent(getIntent());

        // Go to activity_check.xml
        setContentView(R.layout.activity_main);
        loadingPB = findViewById(R.id.loadingPB);
        loadingText = findViewById(R.id.loading_text);


        // Initialize fade out animation
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setStartOffset(1500);
        fadeOut.setDuration(1000);
        animation = new AnimationSet(false);
        animation.addAnimation(fadeOut);



        // Get key from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("com.nicorp.paymentv", MODE_PRIVATE);
        String sharedKey = sharedPreferences.getString("key", null);
        System.out.println("Shared key: " + sharedKey);
        if (sharedKey != null && key == null) {
            TelegramBotService botService = new TelegramBotService(TELEGRAM_TOKEN, this);
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    botService.sendMessageAndWait(CHAT_ID, "/check_auth_mobile " + sharedKey);
                    return null;
                }
            }.execute();
        }

        // Создаем экземпляр PaymentApiService
        PaymentApiService paymentApiService = PaymentApiService.getInstance();

        // Выполняем запрос статуса заказа асинхронно
        paymentApiService.getOrderDataAsync("c5b3fd07-c66b-4f13-99599-1cc5d319f9e3", new PaymentApiService.OnOrderStatusReceivedListener() {
            @Override
            public void onOrderStatusReceived(String status) {
                // Получен статус заказа
                Log.d("OrderStatus", "Received order status: " + status);
                // Здесь вы можете выполнить дополнительные действия на основе статуса заказа
            }
        });



        // Set a listener to be notified when the animation ends
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // Not needed for this example
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // Set the visibility of loadingPB to "gone" after the animation ends
                loadingPB.setVisibility(View.GONE);
                animationFinished = true;
                tryOpenNewActivity();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // Not needed for this example
            }
        });

        //        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    }

    public void setCheckPassed() {
        checkPassed = true;
    }

    public void setCheckAvailable() {
        checkAvailable = false;
    }

    void tryOpenNewActivity() {
        // Check if checkPassed is true
        if (animationFinished && checkPassed && !checkAvailable) {
            loadingText.setText("У вас нет доступа к приложению");
        }
        else if (animationFinished && checkPassed) {
            // wait 2 seconds before opening new activity
            loadingText.setText("Добрый день");
            new Handler().postDelayed(() -> {
                Intent intent = new Intent(MainActivity.this, CheckActivity.class);
                startActivity(intent);
            } , 2000);
        } else if (checkPassed) {
            // Start fade out animation
            loadingPB.startAnimation(animation);
        }
    }
}