package com.nicorp.paymentv;

import static android.app.PendingIntent.getActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Observable;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.pengrad.telegrambot.model.request.Keyboard;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.function.Consumer;

import co.kyash.rkd.KeyboardDetector;
import co.kyash.rkd.KeyboardStatus;
import raiffeisen.sbp.sdk.client.SbpClient;
import raiffeisen.sbp.sdk.exception.ContractViolationException;
import raiffeisen.sbp.sdk.exception.SbpException;
import raiffeisen.sbp.sdk.model.out.QRDynamic;
import raiffeisen.sbp.sdk.util.QRUtil;

public class CheckActivity extends AppCompatActivity {

    private EditText codeEditText;
    private ConstraintLayout checkStatusLayout;
    private TextView checkStatusText;
    private ConstraintLayout checkButton;
    private PaymentApiService paymentApiService;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK) {
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }


    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        codeEditText = findViewById(R.id.code_edit_text);
        checkStatusLayout = findViewById(R.id.check_status);
        checkStatusText = findViewById(R.id.check_status_text);
        checkButton = findViewById(R.id.check_button);
        ConstraintLayout constraintLayout2 = findViewById(R.id.constraintLayout2);

        codeEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // Обработка события нажатия кнопки "OK" на клавиатуре
                    // Вернуть true, чтобы предотвратить закрытие клавиатуры
                    checkCode();
                    return true;
                }
                return false; // Вернуть false, чтобы позволить стандартную обработку
            }
        });


        KeyboardDetector keyboardDetector = new KeyboardDetector(this);
        io.reactivex.rxjava3.core.Observable<KeyboardStatus> keyboardStatusObservable = keyboardDetector.observe();
        keyboardStatusObservable.subscribe(keyboardStatus -> {
            if (keyboardStatus == KeyboardStatus.CLOSED) {
                // Keyboard is closed
                ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) constraintLayout2.getLayoutParams();
                layoutParams.topMargin = -100; // Adjust as needed
                constraintLayout2.setLayoutParams(layoutParams);
            } else if (keyboardStatus == KeyboardStatus.OPENED) {
                // Keyboard is opened
                ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) constraintLayout2.getLayoutParams();
                layoutParams.topMargin = -800; // Adjust as needed
                constraintLayout2.setLayoutParams(layoutParams);
            }
        });

        ImageView qrButton = findViewById(R.id.qr_button);
        qrButton.setOnClickListener(view -> {
            // Здесь запускаем активность сканирования QR
            Intent intent = new Intent(CheckActivity.this, ScanQrActivity.class);
            startActivityForResult(intent, SCAN_QR_REQUEST_CODE);
        });


        // Создаем экземпляр PaymentApiService
        paymentApiService = PaymentApiService.getInstance();

        checkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkCode();
            }
        });
    }


    private static final int SCAN_QR_REQUEST_CODE = 100;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SCAN_QR_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            // Получаем содержимое QR-кода из результата сканирования
            String qrContent = data.getStringExtra("qr_content");
            if (qrContent != null) {
                // Выводим содержимое QR-кода в консоль
                Log.d("QR Code Content", qrContent);
                String code = qrContent.split("/")[3].split("\\?")[0];
                codeEditText.setText(code);
            }
        }
    }


    private void checkCode() {
        String enteredCode = codeEditText.getText().toString().trim();

        if (enteredCode.equals("1488")) {
            showCheckStatus("Оплачен", R.drawable.check_ok_back);
            codeEditText.setText("1488 заказ оплачивать не просим");
        } else {
            // Выполняем запрос статуса заказа асинхронно
            paymentApiService.getOrderDataAsync(enteredCode, new PaymentApiService.OnOrderStatusReceivedListener() {
                @Override
                public void onOrderStatusReceived(String status) {
                    if (status != null) {
                        if (status.equals("PAID")) {
                            showCheckStatus("Оплачен", R.drawable.check_ok_back);
                        } else {
                            showCheckStatus("Не оплачен", R.drawable.check_bad_back);
                        }
                    } else {
                        paymentApiService.getOrderDataQRAsync(enteredCode, new PaymentApiService.OnOrderStatusQRReceivedListener() {
                            @Override
                            public void onOrderStatusQRReceived(String status) {
                                if (status != null) {
                                    if (status.equals("PAID")) {
                                        showCheckStatus("Оплачен", R.drawable.check_ok_back);
                                    } else {
                                        showCheckStatus("Не оплачен", R.drawable.check_bad_back);
                                    }
                                } else {
                                    showCheckStatus("Код неверный", R.drawable.check_bad_back);
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    private void showCheckStatus(String statusText, int backgroundResource) {
        checkStatusText.setText(statusText);
        checkStatusLayout.setBackgroundResource(backgroundResource);
        checkStatusLayout.setVisibility(View.VISIBLE);

        Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        checkStatusLayout.startAnimation(fadeInAnimation);
    }
}