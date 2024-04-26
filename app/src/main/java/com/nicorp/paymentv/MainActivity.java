package com.nicorp.paymentv;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        String appLinkAction = intent.getAction();
        Uri appLinkData = intent.getData();
        Log.d("AppLinkAction", String.valueOf(appLinkData));
//        if (Intent.ACTION_VIEW.equals(appLinkAction) && appLinkData != null){
//            String recipeId = appLinkData.getLastPathSegment();
//            Uri appData = Uri.parse("content://com.recipe_app/recipe/").buildUpon()
//                    .appendPath(recipeId).build();
//            Log.d("AppData", String.valueOf(appData));
//        }
    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        handleIntent(getIntent());

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

        // Go to activity_check.xml
         setContentView(R.layout.activity_check);

//        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


//        String secretKey = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJNQTYyMjk3NiIsImp0aSI6ImI1OTNkODRkLTk1MWYtNGIyZi05ZGViLTcxOWExNDM4NWVmZCJ9.si-87k3Aw5GN67orgJpoyTXC0C2OpWwRCKzLogRWawU"; // change this to your secretKey
//        String sbpMerchantId = "MA622976"; // change this to your sbpMerchantId
//        SbpClient client = new SbpClient(SbpClient.TEST_URL, sbpMerchantId, secretKey);
//        try {
//            String order = QRUtil.generateOrderNumber();
//            // save order in a database;
//            QR qrCode = new QRDynamic(order, new BigDecimal(100));
//            qrCode.setAccount("40700000000000000000");
//            qrCode.setAdditionalInfo("Доп информация");
//            qrCode.setPaymentDetails("Назначение платежа");
//            qrCode.setQrExpirationDate(ZonedDateTime.now().plusDays(1));
//            QRUrl response = client.registerQR(qrCode);
//            Log.d("response", response.getQrId());
//            //response.getOrUrl();
//            Log.d("response", response.getPayload());
//        }
//        catch (IOException networkException) {
//            networkException.getMessage();
//        }
//        catch (SbpException sbpException) {
//            sbpException.getCode(); // Error id
//            sbpException.getMessage();
//        }
//        catch (ContractViolationException contractException) {
//            contractException.getHttpCode();
//            contractException.getMessage();
//        } catch (URISyntaxException e) {
//            throw new RuntimeException(e);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
    }
}