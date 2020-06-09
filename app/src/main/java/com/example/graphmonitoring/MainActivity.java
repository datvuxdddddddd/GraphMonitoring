package com.example.graphmonitoring;

import androidx.appcompat.app.AppCompatActivity;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;


import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    GraphView tempGraph, ambGraph;
    Button LED1, LED2;
    final int[] msgCount = {0};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tempGraph = findViewById(R.id.tempGraph);
        ambGraph = findViewById(R.id.ambGraph);

        LED1 = findViewById(R.id.LED1);
        LED2 = findViewById(R.id.LED2);


        MainActivity.this.setTitle("Message sent: 0");
        getDataFromThingSpeak();
        setupBlinkyTimer();
        startMQTT();

        LED1.setOnClickListener((View v) -> {
            MqttMessage message = new MqttMessage();
            message.setPayload("TOGGLE1".getBytes());
            try {
                mqttHelper.mqttAndroidClient.publish("callback", message);
                runOnUiThread(() -> MainActivity.this.setTitle("Message sent: " + ++msgCount[0]));
            } catch (MqttException e) {
                e.printStackTrace();
            }
        });

        LED2.setOnClickListener((View v) -> {
            MqttMessage message = new MqttMessage();
            message.setPayload("TOGGLE2".getBytes());
            try {
                mqttHelper.mqttAndroidClient.publish("callback", message);
                runOnUiThread(() -> MainActivity.this.setTitle("Message sent: " + ++msgCount[0]));
            } catch (MqttException e) {
                e.printStackTrace();
            }
        });

    }


    private void showDataOnGraph(LineGraphSeries<DataPoint> series, GraphView graph){
        if(graph.getSeries().size() > 0){
            graph.getSeries().remove(0);
        }
        graph.addSeries(series);
        series.setDrawDataPoints(true);
        series.setDataPointsRadius(10);
    }

    private void getDataFromThingSpeak(){ //run this once upon startup
        OkHttpClient okHttpClient = new OkHttpClient();
        Request.Builder builder = new Request.Builder();

        String apiURL = "https://api.thingspeak.com/channels/1007655/feeds.json?results=9";
        Request request = builder.url(apiURL).build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                double[] temp = new double[9];
                double[] amb = new double[9];
                int i;
                String jsonString = response.body().string();
                try{
                    JSONObject jsonDataTemp = new JSONObject(jsonString);
                    JSONArray jsonArray1 = jsonDataTemp.getJSONArray("feeds");

                    //TEMP
                    for (i = 0; i < temp.length; i++){
                        if (jsonArray1.getJSONObject(i).getString("field1").equals("null"))
                            temp[i] = 0;
                        else temp[i] = jsonArray1.getJSONObject(i).getDouble("field1");
                    }

                    LineGraphSeries<DataPoint> seriesTemp = new LineGraphSeries<>(new DataPoint[]
                            {   new DataPoint(0, temp[0]),
                                    new DataPoint(1, temp[1]),
                                    new DataPoint(2, temp[2]),
                                    new DataPoint(3, temp[3]),
                                    new DataPoint(4, temp[4]),
                                    new DataPoint(5, temp[5]),
                                    new DataPoint(6, temp[6]),
                                    new DataPoint(7, temp[7]),
                                    new DataPoint(8, temp[8])

                            });
                    setGraphLimit(tempGraph, 40);
                    showDataOnGraph(seriesTemp, tempGraph);

                    //AMBIANCE
                    JSONObject jsonDataAmb = new JSONObject(jsonString);
                    JSONArray jsonArray = jsonDataAmb.getJSONArray("feeds");
                    for (i = 0; i < temp.length; i++){
                        if (jsonArray.getJSONObject(i).getString("field2").equals("null"))
                            amb[i] = 0;
                        else amb[i] = jsonArray.getJSONObject(i).getDouble("field2");
                    }
                    LineGraphSeries<DataPoint> seriesAmb = new LineGraphSeries<>(new DataPoint[]
                            {   new DataPoint(0, amb[0]),
                                    new DataPoint(1, amb[1]),
                                    new DataPoint(2, amb[2]),
                                    new DataPoint(3, amb[3]),
                                    new DataPoint(4, amb[4]),
                                    new DataPoint(5, amb[5]),
                                    new DataPoint(6, amb[6]),
                                    new DataPoint(7, amb[7]),
                                    new DataPoint(8, amb[8])

                            });

                    setGraphLimit(ambGraph, 100);
                    showDataOnGraph(seriesAmb, ambGraph);

                }catch (Exception e){
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }
        });
    }

    private void setupBlinkyTimer(){
        Timer mTimer = new Timer();
        TimerTask mTask = new TimerTask() {
            @Override
            public void run() {
                getDataFromThingSpeak();
            }
        };
        mTimer.schedule(mTask, 5000, 5000);
    }

    public void setGraphLimit(GraphView graph, int y) {
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(y);
        graph.getGridLabelRenderer().setNumHorizontalLabels(9);
        graph.getGridLabelRenderer().setNumVerticalLabels(7);
        graph.getViewport().setYAxisBoundsManual(true);
    }


    MQTTHelper mqttHelper;
    private void startMQTT(){
        mqttHelper = new MQTTHelper(getApplicationContext());
        mqttHelper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {

            }

            @Override
            public void connectionLost(Throwable throwable) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                //Log.d("received", mqttMessage.toString());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                Log.d("SENT", "Sent message");
            }
        });

    }
}

