/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.netbit.binancechecker;


import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.media.SoundPool;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.text.SpannableString;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import vn.netbit.beans.Bean;
import vn.netbit.caches.PriceCache;
import vn.netbit.utils.NotifyIdGenerator;
import vn.netbit.utils.SequenceNumber;

/**
 *
 * @author lufiv
 */
public class Run implements Runnable{
    private String symbol;
    private Gson gson;
    private OkHttpClient client;
    private int period;
    private double tempPrice;
    private double startPrice;
    private int stepCheck;
    private double stepPrice;
    private View v;
    private TextView result;
    private Activity activity;
    private double profit;
    private double buyPrice;
    private NotificationManager mNotificationManager;

    public Run(String symbol, Gson gson, OkHttpClient client, int period, double startPrice, int stepCheck, double stepPrice, View v, TextView result, double buyPrice, double profit, NotificationManager mNotificationManager){
        this.symbol = symbol;
        this.gson = gson;
        this.client = client;
        this.period = period;
        this.startPrice = startPrice;
        this.stepCheck = stepCheck;
        this.stepPrice = stepPrice;
        this.profit = profit;
        this.buyPrice = buyPrice;
        this.v = v;
        this.result = result;
        this.mNotificationManager = mNotificationManager;
        this.activity = (Activity) v.getContext();
    }

    @Override
    public void run() {
        getPrice(symbol);
    }




    public void getPrice(String symbol){
        try {
            Request request = new Request.Builder()
                    .url("https://www.binance.com/api/v1/ticker/allPrices")
                    .build();

            Response response = client.newCall(request).execute();
            String body = response.body().string();

            List<Bean> list = gson.fromJson(body, new TypeToken<ArrayList<Bean>>(){}.getType());

            for(final Bean b:list){
                if(symbol.equals(b.getSymbol())){
                    int current = SequenceNumber.getInstance().next();
                    PriceCache.getInstance().set(current, b);

                    final double change = b.getPrice() - tempPrice;

                    activity.runOnUiThread(new Runnable() {
                        public void run(){
                            String color;
                            if(change>0){
                                color = "03ff47";
                            } else if (change == 0){
                                color = "000000";
                            } else {
                                color = "ff0303";
                            }

                            CharSequence ed = result.getText();
                            final String text = "<font color=#" + color + ">" + String.format("%.9f\n", b.getPrice()) + "</font><br> " + Html.toHtml(new SpannableString(ed)).toString();;

                            result.setText(Html.fromHtml(text), TextView.BufferType.EDITABLE);
                        }
                    });

                    if(b.getPrice() == tempPrice){
                        return;
                    }
                    tempPrice = b.getPrice();
                    if(tempPrice <= buyPrice){
                        showNotification("Giá " + String.format("%.9f", tempPrice), "Mua ngay", R.drawable.ic_check_circle);
                    }
                    checkPrice(b.getPrice()-startPrice, true);
                    Bean pre = PriceCache.getInstance().get(getPre(current, stepCheck));
                    if(null!=pre){
                        checkPrice(b.getPrice() - pre.getPrice(), false);
                    }

                }
            }
        } catch (IOException ex) {
            System.err.println(ex);
        }

    }

    private void checkPrice(double change, boolean compareWithStartPrice){
        String status;
        if(change > 0){
            status = "Tăng ";
        } else{
            status = "Giảm ";
        }
        int timeChange = stepCheck*period;
        status += compareWithStartPrice?"so với giá mua: " + String.format("%.9f", change):"so với " + timeChange + "s trước: " + String.format("%.9f", change);
        if(stepPrice <= Math.abs(change)){
            showToast(status, change>0);
        }
        if(compareWithStartPrice && change >= profit ){
            showNotification("Tăng " + String.format("%.9f", change), "Bán ngay", R.drawable.ic_check_circle);
        }

    }

    public static int getPre(int current, int step){
        if(current < step){
            return SequenceNumber.MAX_VALUE + current - step;
        }
        return current - step;
    }

    private void showToast(final String message, final boolean increase ){
        activity.runOnUiThread(new Runnable() {
            public void run()
            {
                LayoutInflater inflater = (LayoutInflater) v.getContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );;
                View layout = inflater.inflate(R.layout.toast_view,
                        (ViewGroup) v.findViewById(R.id.custom_toast));

                TextView text = (TextView) layout.findViewById(R.id.text);
                ImageView img = (ImageView) layout.findViewById(R.id.image);
                Drawable draw = increase?v.getResources().getDrawable(R.drawable.ic_check_circle):v.getResources().getDrawable(R.drawable.ic_warning);
                img.setImageDrawable(draw);

                text.setText(message);

                Toast toast = new Toast(v.getContext());
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.setView(layout);
                toast.show();
            }
        });

    }

    private void showNotification(final String message, final String title, final int icon){
        activity.runOnUiThread(new Runnable() {
            public void run(){
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(v.getContext())
                                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                                .setSmallIcon(icon)
                                .setContentTitle(title)
                                .setContentText(message);
                mNotificationManager.notify(NotifyIdGenerator.getInstance().next(), mBuilder.build());
            }
        });

    }
    
}
