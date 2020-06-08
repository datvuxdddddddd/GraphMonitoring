package com.example.graphmonitoring;

import androidx.appcompat.app.AppCompatActivity;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;


import android.os.Bundle;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    GraphView tempGraph, ambGraph;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getDataFromThingSpeak();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tempGraph = findViewById(R.id.tempGraph);
        ambGraph = findViewById(R.id.ambGraph);
        setupBlinkyTimer();
        startMQTT();

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

        String apiURL = "https://api.thingspeak.com/channels/1007655/feeds.json?results=5";

        Request request = builder.url(apiURL).build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {

            }

            @Override
            public void onResponse(Response response) throws IOException {
                String jsonString = response.body().string();
                try{
                    JSONObject jsonDataTemp = new JSONObject(jsonString);
                    JSONArray jsonArray1 = jsonDataTemp.getJSONArray("feeds");
                    double temp0 = jsonArray1.getJSONObject(0).getDouble("field1");
                    double temp1 = jsonArray1.getJSONObject(1).getDouble("field1");
                    double temp2 = jsonArray1.getJSONObject(2).getDouble("field1");
                    double temp3 = jsonArray1.getJSONObject(3).getDouble("field1");
                    double temp4 = jsonArray1.getJSONObject(4).getDouble("field1");

                    LineGraphSeries<DataPoint> seriesTemp = new LineGraphSeries<>(new DataPoint[]
                            {   new DataPoint(0, temp0),
                                    new DataPoint(1, temp1),
                                    new DataPoint(2, temp2),
                                    new DataPoint(3, temp3),
                                    new DataPoint(4, temp4)
                            });
                    setGraphLimit(tempGraph);
                    showDataOnGraph(seriesTemp, tempGraph);

//                  aaa             ///

                    JSONObject jsonDataAmb = new JSONObject(jsonString);
                    JSONArray jsonArray = jsonDataAmb.getJSONArray("feeds");
                    double amb0 = jsonArray.getJSONObject(0).getDouble("field2");
                    double amb1 = jsonArray.getJSONObject(1).getDouble("field2");
                    double amb2 = jsonArray.getJSONObject(2).getDouble("field2");
                    double amb3 = jsonArray.getJSONObject(3).getDouble("field2");
                    double amb4 = jsonArray.getJSONObject(4).getDouble("field2");

                    LineGraphSeries<DataPoint> seriesAmb = new LineGraphSeries<>(new DataPoint[]
                            {   new DataPoint(0, amb0),
                                    new DataPoint(1, amb1),
                                    new DataPoint(2, amb2),
                                    new DataPoint(3, amb3),
                                    new DataPoint(4, amb4)
                                    //seriesAmb.appendData();
                            });

                    setGraphLimit(ambGraph);
                    showDataOnGraph(seriesAmb, ambGraph);

                }catch (Exception e){}

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
        mTimer.schedule(mTask, 2000, 5000);
    }

    public void setGraphLimit(GraphView graph) {
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(100);
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
                Log.d("MQTT", mqttMessage.toString());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }

        });

    }
}

