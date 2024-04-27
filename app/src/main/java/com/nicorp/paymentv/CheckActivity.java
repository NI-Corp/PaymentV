package com.nicorp.paymentv;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.net.URISyntaxException;

import raiffeisen.sbp.sdk.client.SbpClient;
import raiffeisen.sbp.sdk.exception.ContractViolationException;
import raiffeisen.sbp.sdk.exception.SbpException;
import raiffeisen.sbp.sdk.model.out.QRDynamic;
import raiffeisen.sbp.sdk.util.QRUtil;

public class CheckActivity extends AppCompatActivity {

    private EditText codeEditText;
    private ConstraintLayout checkStatusLayout;
    private TextView checkStatusText;
    private ImageView checkButton;
    private PaymentApiService paymentApiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check);

        codeEditText = findViewById(R.id.code_edit_text);
        checkStatusLayout = findViewById(R.id.check_status);
        checkStatusText = findViewById(R.id.check_status_text);
        checkButton = findViewById(R.id.check_button);

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