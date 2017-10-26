package vn.netbit.binancechecker;

import android.app.NotificationManager;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class MainActivity extends AppCompatActivity {

    private boolean isRunning = false;
    private OkHttpClient client;
    private Gson gson;
    private Button btn;
    private EditText symbol;
    private EditText period;
    private EditText startPrice;
    private EditText stepPrice;
    private EditText buyPrice;
    private EditText profit;
    private EditText stepCheck;
    private ScheduledExecutorService exeService;
    private String symbolVal;
    private int periodVal;
    private double startPriceVal;
    private double stepPriceVal;
    private double profitVal;
    private double buyPriceVal;
    private int stepCheckVal;
    private TextView result;
    private NotificationManager mNotificationManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn = (Button) findViewById(R.id.btn);
        symbol = (EditText) findViewById(R.id.Symbol);
        period = (EditText) findViewById(R.id.Period);
        startPrice = (EditText) findViewById(R.id.StartPrice);
        stepPrice = (EditText) findViewById(R.id.StepPrice);
        stepCheck = (EditText) findViewById(R.id.StepCheck);
        buyPrice = (EditText) findViewById(R.id.BuyPrice);
        profit = (EditText) findViewById(R.id.Profit);
        result = (TextView) findViewById(R.id.Result);
        client = new OkHttpClient();
        gson = new Gson();
        mNotificationManager =

                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

    }

    public void btnClick(View v){
        symbolVal = symbol.getText().toString();
        periodVal = Integer.parseInt(period.getText().toString());
        startPriceVal = Double.parseDouble(startPrice.getText().toString());
        stepPriceVal = Double.parseDouble(stepPrice.getText().toString());
        stepCheckVal = Integer.parseInt(stepCheck.getText().toString());


        buyPriceVal = Double.parseDouble(buyPrice.getText().toString());
        profitVal = Double.parseDouble(profit.getText().toString());

        if(isRunning){
            btn.setText("Start");
            isRunning = false;
            exeService.shutdownNow();
        } else {
            btn.setText("Stop");
            isRunning = true;
            exeService = Executors.newScheduledThreadPool(1);
            exeService.scheduleAtFixedRate(new Run(symbolVal, gson, client, periodVal, startPriceVal, stepCheckVal, stepPriceVal, v, result, buyPriceVal, profitVal, mNotificationManager), 0, periodVal, TimeUnit.SECONDS);
        }
    }
}
