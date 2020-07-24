package com.project.FireApp_guest;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.example.fireapp_guest.R;
import com.google.android.material.snackbar.Snackbar;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;

import pl.polidea.view.ZoomView;

public class MainActivity extends AppCompatActivity implements BeaconConsumer {
    TextView r11_red, r21_red, r22_red, r23_red, r24_red;
    TextView time11, time12, time21, time22, time23, time24;

    private static final String TAG_fire11 = "fire1", TAG_doppler11 = "doppler1";
    private static final String TAG_time11 = "time";
    private static final String TAG_window1 = "W1", TAG_window2 = "W2", TAG_door1 = "D1", TAG_door2 = "D2";

    private static final String TAG_fire21 = "f1", TAG_fire22 = "f2", TAG_fire23 = "f3", TAG_fire24 = "f4";
    private static final String TAG_time21 = "t1", TAG_time22 = "t2", TAG_time23 = "t3", TAG_time24 = "t4";

    String myJSON1 = null, myJSON2 = null, myJSON3 = null;
    JSONArray signals = null;
    String url1 = "http://155.230.15.87/DB_A1.php";
    String url2 = "http://155.230.15.87/DB_A2.php";
    String url3 = "http://155.230.15.87/DB_A3.php";

    FrameLayout container;
    LinearLayout.LayoutParams layoutParams;
    Button floor1, floor2;
    LayoutInflater inflator;
    View screen1, screen2;
    ZoomView zoomView1, zoomView2;
//   ------------------------------------------------------------------------------------------
    private String TAG = MainActivity.class.getSimpleName();
    private BeaconManager beaconManager;
//   -----------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //        ----------Beacon--------------------------------------------------------------------------
        // 실제로 비콘을 탐지하기 위한 비콘매니저 객체를 초기화
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(
                "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));

        beaconManager.bind(this); // 비콘 탐지를 시작한다. 실제로는 서비스를 시작하는것.
//        -------------------------------------------------------------------------------------------
        //자동 새로고침
        (new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.interrupted())
                    try {
                        Thread.sleep(1000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                getData(url1, url2, url3);
                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
            }
        })).start();


        container = findViewById(R.id.container);
        inflator = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        screen1 = inflator.inflate(R.layout.fragment_server1, null);
        screen2 = inflator.inflate(R.layout.fragment_server2, null);

        r11_red = screen1.findViewById(R.id.r11_red);
        time11 = screen1.findViewById(R.id.time11);
        time12 = screen1.findViewById(R.id.time12);

        r21_red = screen2.findViewById(R.id.r21);
        r22_red = screen2.findViewById(R.id.r22);
        r23_red = screen2.findViewById(R.id.r23);
        r24_red = screen2.findViewById(R.id.r24);
        time21 = screen2.findViewById(R.id.time21);
        time22 = screen2.findViewById(R.id.time22);
        time23 = screen2.findViewById(R.id.time23);
        time24 = screen2.findViewById(R.id.time24);

        layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        zoomView1 = new ZoomView(MainActivity.this);
        zoomView1.addView(screen1);
        zoomView1.setLayoutParams(layoutParams);
        zoomView1.setMiniMapEnabled(true); //좌측 상단 미니맵
        zoomView1.setMaxZoom(4f); //줌 Max 배율 설정. 1f로 설정 시 줌 안됨
        zoomView1.setMiniMapCaption("Mini Map");  //미니맵 내용
        zoomView1.setMiniMapCaptionSize(20); //미니맵 내용 글씨 크기

        zoomView2 = new ZoomView(MainActivity.this);
        zoomView2.addView(screen2);
        zoomView2.setLayoutParams(layoutParams);
        zoomView2.setMiniMapEnabled(true);
        zoomView2.setMaxZoom(4f);
        zoomView2.setMiniMapCaption("Mini Map");
        zoomView2.setMiniMapCaptionSize(20);

        container.addView(zoomView1);

        //1,2층 버튼 코드
        floor1 = (Button) findViewById(R.id.floor1);
        floor1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                container.removeAllViews();
                container.addView(zoomView1);
                getData(url1, url2, url3);
            }
        });

        floor2 = (Button) findViewById(R.id.floor2);
        floor2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                container.removeAllViews();
                container.addView(zoomView2);
                getData(url1, url2, url3);
            }
        });
    }


    ////////////Beacon////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    protected void onStop(){
        super.onStop();
        beaconManager.unbind(this);
    }

    @Override
    public void onBeaconServiceConnect() {
        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId",null, null, null));    //비콘 거리 측정하는 메소드
        } catch (RemoteException e) { }

        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    for (Beacon b : beacons) {
                        String uuid = b.getId1().toString();
                        Log.d(TAG, "UUID : " + uuid);
                        if (uuid.equals("00000101-b644-4520-8f0c-720eaf059935")) {
                            createNotification("102호 화재 발생", "비콘 감지. 초록색 빛을 따라 신속히 대피하시길 바랍니다.", 1);
                        }
                        else if (uuid.equals("00000201-b644-4520-8f0c-720eaf059935")) {
                            createNotification("201호 화재 발생", "비콘 감지. 초록색 빛을 따라 신속히 대피하시길 바랍니다.", 2);
                        }
                        else if (uuid.equals("00000202-b644-4520-8f0c-720eaf059935")) {
                            createNotification("202호 화재 발생", "비콘 감지. 초록색 빛을 따라 신속히 대피하시길 바랍니다.", 3);
                        }
                        else if (uuid.equals("00000203-b644-4520-8f0c-720eaf059935")) {
                            createNotification("203호 화재 발생", "비콘 감지. 초록색 빛을 따라 신속히 대피하시길 바랍니다.", 4);
                        }
                        else if (uuid.equals("00000204-b644-4520-8f0c-720eaf059935")) {
                            createNotification("204호 화재 발생", "비콘 감지. 초록색 빛을 따라 신속히 대피하시길 바랍니다.", 5);
                        }
                    }
                }
            }
        });

        beaconManager.setMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                Log.i(TAG, "I just saw an beacon for the first time!");
            }
            @Override
            public void didExitRegion(Region region) {
                Log.i(TAG, "I no longer see an beacon");
            }
            @Override
            public void didDetermineStateForRegion(int state, Region region) {
                Log.i(TAG, "I have just switched from seeing/not seeing beacons: " + state);
            }
        });

        try {
            beaconManager.startMonitoringBeaconsInRegion(new Region("myMonitoringUniqueId", null, null, null));
        } catch (RemoteException e) {
        }
    }


//////////데이터 받고 정보 띄우기/////////////////////////////////////////////////////////////////////////////////////////
    protected void showpic() {
        ArrayList Rooms = new ArrayList();
        ArrayList Roomp = new ArrayList();
        /////////////////////////////////JSON1//////////////////////////////////////////////////////
        if (myJSON1 != null) {
            try {
                signals = new JSONArray(myJSON1);
                Log.d("Main", "JSON size : " + signals.length());

                Integer num;
                if (signals.length() > 20) {
                    num = signals.length() - 20;
                } else num = 0;

                for (int i = num; i < signals.length(); i++) {
                    JSONObject c = signals.getJSONObject(i);
                    Integer fire11 = c.getInt(TAG_fire11);
                    Integer dopp11 = c.getInt(TAG_doppler11);
                    String text11 = c.getString(TAG_time11);

                    if (text11 == null) {
                        time11.setText(text11);
                    }

                    if (dopp11 == 0 && fire11 == 0) {
                        r11_red.setVisibility(View.INVISIBLE);
                        if(Roomp.contains("Room1")==true) Roomp.remove("Room1");
                        if(Rooms.contains("Room1")==true) Rooms.remove("Room1");
                    } else if (dopp11 == 1 && fire11 == 0) {
                        r11_red.setVisibility(View.INVISIBLE);
                        if(Roomp.contains("Room1")==false) Roomp.add("Room1");
                        if(Rooms.contains("Room1")==true) Rooms.remove("Room1");
                    } else if (dopp11 == 0 && fire11 == 1) {
                        r11_red.setVisibility(View.VISIBLE);
                        if(Roomp.contains("Room1")==true) Roomp.remove("Room1");
                        if(Rooms.contains("Room1")==false) Rooms.add("Room1");
                        if (time11.getText().length() < 5) {
                            time11.setText(text11);
                        }
                    } else if (dopp11 == 1 && fire11 == 1) {
                        r11_red.setVisibility(View.INVISIBLE);
                        if(Roomp.contains("Room1")==false) Roomp.add("Room1");
                        if(Rooms.contains("Room1")==false) Rooms.add("Room1");
                        if (time11.getText().length() < 5) {
                            time11.setText(text11);
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        /////////////////////////////JSON3/////////////////////////////////////////////
        if (myJSON3 != null) {
            try {
                signals = new JSONArray(myJSON3);
                Log.d("Main", "JSON size : " + signals.length());

                for (int i = 0; i < signals.length(); i++) {
                    JSONObject c = signals.getJSONObject(i);
                    Integer fire21 = c.getInt(TAG_fire21);
                    Integer fire22 = c.getInt(TAG_fire22);
                    Integer fire23 = c.getInt(TAG_fire23);
                    Integer fire24 = c.getInt(TAG_fire24);
                    String text21 = c.getString(TAG_time21);
                    String text22 = c.getString(TAG_time22);
                    String text23 = c.getString(TAG_time23);
                    String text24 = c.getString(TAG_time24);

                    if (text21 == null) time21.setText(text21);
                    if (text22 == null) time22.setText(text22);
                    if (text23 == null) time23.setText(text23);
                    if (text24 == null) time24.setText(text24);

                    if (fire21 == 1) {
                        r21_red.setVisibility(View.VISIBLE);
                        if(Rooms.contains("Room3")==false) Rooms.add("Room3");
                        if (time21.getText().length() < 5) {
                            time21.setText(text21);
                        }
                    } else if (fire21 == 0) {
                        r21_red.setVisibility(View.INVISIBLE);
                        if(Rooms.contains("Room3")==true) Rooms.remove("Room3");
                    }

                    if (fire22 == 1) {
                        r22_red.setVisibility(View.VISIBLE);
                        if(Rooms.contains("Room4")==false) Rooms.add("Room4");
                        if (time22.getText().length() < 5) {
                            time22.setText(text22);
                        }
                    } else if (fire22 == 0) {
                        r22_red.setVisibility(View.INVISIBLE);
                        if(Rooms.contains("Room4")==true) Rooms.remove("Room4");
                    }

                    if (fire23 == 1) {
                        r23_red.setVisibility(View.VISIBLE);
                        if(Rooms.contains("Room5")==false) Rooms.add("Room5");
                        if (time23.getText().length() < 5) {
                            time23.setText(text23);
                        }
                    } else if (fire23 == 0) {
                        r23_red.setVisibility(View.INVISIBLE);
                        if(Rooms.contains("Room5")==true) Rooms.remove("Room5");
                    }

                    if (fire24 == 1) {
                        r24_red.setVisibility(View.VISIBLE);
                        if(Rooms.contains("Room6")==false) Rooms.add("Room6");
                        if (time24.getText().length() < 5) {
                            time24.setText(text24);
                        }
                    } else if (fire24 == 0) {
                        r24_red.setVisibility(View.INVISIBLE);
                        if(Rooms.contains("Room6")==true) Rooms.remove("Room6");
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if(Rooms.isEmpty()==false && Roomp.isEmpty()==true){
            Snackbar snackbar = Snackbar.make(container, Rooms+" 화재 발생", Snackbar.LENGTH_INDEFINITE);
            View sb = snackbar.getView();
            sb.setBackgroundColor(Color.rgb(204, 000, 000));
            snackbar.show();
        }else if(Rooms.isEmpty()==true && Roomp.isEmpty()==false) {
            Snackbar snackbar = Snackbar.make(container,Roomp+" 사람 감지" , Snackbar.LENGTH_INDEFINITE);
            View sb = snackbar.getView();
            sb.setBackgroundColor(Color.rgb(000, 153, 000));
            snackbar.show();
        }else if(Rooms.isEmpty()==true && Roomp.isEmpty()==true) {
            Snackbar snackbar = Snackbar.make(container,  null, Snackbar.LENGTH_INDEFINITE);
            View sb = snackbar.getView();
            sb.setBackgroundColor(Color.TRANSPARENT);
            snackbar.show();
        }else if(Rooms.isEmpty()==false && Roomp.isEmpty()==false){
            Snackbar snackbar = Snackbar.make(container,  Roomp +" 사람 감지\n"+ Rooms + " 화재 발생", Snackbar.LENGTH_INDEFINITE);
            View sb = snackbar.getView();
            sb.setBackgroundColor(Color.rgb(051, 102, 255));
            snackbar.show();
        }
    }

    //////////////////////////Notification///////////////////////////////////////////////////////////////////////////////////
    private void createNotification(String title, String context, int id) {

        PendingIntent mPendingIntent = PendingIntent.getActivity(
                MainActivity.this,
                0,
                new Intent(getApplicationContext(), MainActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default");
        builder.setSmallIcon(R.drawable.lights);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.fire));
        builder.setContentTitle(title);
        builder.setContentText(context);
        builder.setDefaults(Notification.FLAG_SHOW_LIGHTS);
        builder.setLights(Color.RED, 1000, 1000);
        builder.setPriority(Notification.PRIORITY_MAX);
        builder.setAutoCancel(true);
        builder.setContentIntent(mPendingIntent);

        // 알림 표시
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(new NotificationChannel("channel_id", "channel_name", NotificationManager.IMPORTANCE_HIGH));
        }
        // id: 정의해야하는 각 알림의 고유한 int값
        notificationManager.notify(id, builder.build());
    }

    ////////////////////////////////getData/////////////////////////////////////////////////////////////////////////////////
    public void getData(String url1, String url2, String url3) {
        class GetDataJSON extends AsyncTask<String, Void, ArrayList<String>> {
            @Override
            protected ArrayList<String> doInBackground(String... params) {
                Log.d("Test", "doinbackground");
                ArrayList<String> returnlist = new ArrayList<String>();
                String uri1 = params[0];
                String uri2 = params[1];
                String uri3 = params[2];

                BufferedReader bufferedReader = null;
                try {
                    URL url1 = new URL(uri1);
                    URL url2 = new URL(uri2);
                    URL url3 = new URL(uri3);
                    String json1, json2, json3;

                    HttpURLConnection con1 = (HttpURLConnection) url1.openConnection();
                    bufferedReader = new BufferedReader(new InputStreamReader(con1.getInputStream()));
                    StringBuilder sb1 = new StringBuilder();
                    while ((json1 = bufferedReader.readLine()) != null) {
                        sb1.append(json1 + "\n");
                    }
                    returnlist.add(sb1.toString().trim());

                    HttpURLConnection con2 = (HttpURLConnection) url2.openConnection();
                    bufferedReader = new BufferedReader(new InputStreamReader(con2.getInputStream()));
                    StringBuilder sb2 = new StringBuilder();
                    while ((json2 = bufferedReader.readLine()) != null) {
                        sb2.append(json2 + "\n");
                    }
                    returnlist.add(sb2.toString().trim());

                    HttpURLConnection con3 = (HttpURLConnection) url3.openConnection();
                    bufferedReader = new BufferedReader(new InputStreamReader(con3.getInputStream()));
                    StringBuilder sb3 = new StringBuilder();
                    while ((json3 = bufferedReader.readLine()) != null) {
                        sb3.append(json3 + "\n");
                    }
                    returnlist.add(sb3.toString().trim());
                    return returnlist;
                } catch (IOException e) {
                    e.printStackTrace();
                    return new ArrayList<String>();
                }
            }

            @Override
            protected void onPostExecute(ArrayList<String> result){
//                Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
//                Log.d("MySql_result", result);
                myJSON1 = result.get(0);
                myJSON2 = result.get(1);
                myJSON3 = result.get(2);
                showpic();
            }
        }
        GetDataJSON g = new GetDataJSON();
        g.execute(url1, url2, url3);
    }
    ////////////////////////////////////send Data//////////////////////////////////////////////////////////
    private void insertToDatabase(String W1, String W2, String D1, String D2) {
        class InsertData extends AsyncTask<String, Void, String> {
            ProgressDialog loading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(MainActivity.this, "기다려 주세요", null, true, true);
            }

            @Override
            protected String doInBackground(String... params) {
                try {
                    String W1 = params[0];
                    String W2 = params[1];
                    String D1 = params[2];
                    String D2 = params[3];

                    String link = "http://155.230.15.87/DB_AW.php";
                    String data = URLEncoder.encode("W1", "UTF-8") + "=" + URLEncoder.encode(W1, "UTF-8");
                    data += "&" + URLEncoder.encode("W2", "UTF-8") + "=" + URLEncoder.encode(W2, "UTF-8");
                    data += "&" + URLEncoder.encode("D1", "UTF-8") + "=" + URLEncoder.encode(D1, "UTF-8");
                    data += "&" + URLEncoder.encode("D2", "UTF-8") + "=" + URLEncoder.encode(D2, "UTF-8");

                    URL url = new URL(link);
                    URLConnection conn = url.openConnection();
                    conn.setDoOutput(true);
                    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

                    wr.write(data);
                    wr.flush();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    StringBuilder sb = new StringBuilder();
                    String line = null;

                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                        break;
                    }
                    return sb.toString();
                } catch (Exception e) {
                    return new String("Exception: " + e.getMessage());
                }
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();
            }

        }
        InsertData task = new InsertData();
        task.execute(W1, W2, D1, D2);
    }
}