package com.nicorp.paymentv;

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

        // Создаем экземпляр PaymentApiService
        paymentApiService = PaymentApiService.getInstance();

        checkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkCode();
            }
        });
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
                        showCheckStatus("Код неверный", R.drawable.check_bad_back);
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