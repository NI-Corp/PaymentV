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

public class CheckActivity extends AppCompatActivity {

    private EditText codeEditText;
    private ConstraintLayout checkStatusLayout;
    private TextView checkStatusText;
    private Button checkButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check);

        // Инициализация компонентов
        codeEditText = findViewById(R.id.code_edit_text);
        checkStatusLayout = findViewById(R.id.check_status);
        checkStatusText = findViewById(R.id.check_status_text);
        checkButton = findViewById(R.id.button);

        checkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(),"Button was Clicked", Toast.LENGTH_SHORT).show();
            }
        });

        // Обработчик нажатия на кнопку проверки
//        checkButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.d("CheckButton", "Button clicked");
//                // Получение введенного текста
//                String enteredCode = codeEditText.getText().toString();
//
//                // Проверка введенного текста
//                if (enteredCode.equals("1488")) {
//                    // Код верный: показываем статус "Оплачен" с анимацией
//                    showStatus(true);
//                } else {
//                    // Код неверный: показываем статус "Не оплачен" с анимацией
//                    showStatus(false);
//                }
//            }
//        });
    }

    // Метод для показа статуса с анимацией
    private void showStatus(boolean isPaid) {
        // Установка текста и фона статуса
        if (isPaid) {
            checkStatusText.setText("Оплачен");
            checkStatusLayout.setBackgroundResource(R.drawable.check_ok_back);
        } else {
            checkStatusText.setText("Не оплачен");
            checkStatusLayout.setBackgroundResource(R.drawable.check_bad_back);
        }

        // Показываем статус с анимацией
        checkStatusLayout.setVisibility(View.VISIBLE);
        Animation fadeIn = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_in);
        checkStatusLayout.startAnimation(fadeIn);

        // Прячем статус через 3 секунды
        checkStatusLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkStatusLayout.setVisibility(View.INVISIBLE);
            }
        }, 3000);
    }
}