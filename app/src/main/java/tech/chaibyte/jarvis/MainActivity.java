package tech.chaibyte.jarvis;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity implements TextToSpeech.OnInitListener {
    private static final int SPEECH = 100, MIC = 101;
    private static final String WORKER = "https://jarvis-784d.chaitanya6077.workers.dev/";
    private TextView status, transcript;
    private TextToSpeech tts;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        tts = new TextToSpeech(this, this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL); root.setGravity(Gravity.CENTER);
        root.setPadding(40,40,40,40); root.setBackgroundColor(Color.rgb(5,7,10));
        TextView title = text("J A R V I S", 34, Color.CYAN); root.addView(title);
        status = text("ONLINE • TAP TO SPEAK", 14, Color.LTGRAY); root.addView(status);
        transcript = text("", 18, Color.WHITE); transcript.setPadding(0,60,0,60); root.addView(transcript);
        Button talk = new Button(this); talk.setText("TALK TO JARVIS"); talk.setTextSize(18); talk.setOnClickListener(v -> listen()); root.addView(talk);
        setContentView(root);
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, MIC);
    }

    private TextView text(String s,int size,int color){ TextView v=new TextView(this); v.setText(s); v.setTextSize(size); v.setTextColor(color); v.setGravity(Gravity.CENTER); return v; }
    private void listen(){
        Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"en-IN");
        i.putExtra(RecognizerIntent.EXTRA_PROMPT,"Speak to Jarvis");
        try { startActivityForResult(i,SPEECH); } catch(Exception e){ say("Speech recognition is not available."); }
    }
    @Override protected void onActivityResult(int req,int result,Intent data){
        super.onActivityResult(req,result,data);
        if(req==SPEECH && result==RESULT_OK && data!=null){
            ArrayList<String> r=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if(r!=null&&!r.isEmpty()){ String cmd=r.get(0); transcript.setText("YOU: "+cmd); send(cmd); }
        }
    }
    private void send(String command){
        status.setText("THINKING...");
        new Thread(() -> {
            String reply;
            try {
                HttpURLConnection c=(HttpURLConnection)new URL(WORKER).openConnection();
                c.setRequestMethod("POST"); c.setRequestProperty("Content-Type","application/json"); c.setDoOutput(true);
                byte[] body=new JSONObject().put("command",command).toString().getBytes(StandardCharsets.UTF_8);
                try(OutputStream os=c.getOutputStream()){os.write(body);}
                InputStream in=c.getResponseCode()<400?c.getInputStream():c.getErrorStream();
                BufferedReader br=new BufferedReader(new InputStreamReader(in,StandardCharsets.UTF_8));
                StringBuilder sb=new StringBuilder(); String line; while((line=br.readLine())!=null)sb.append(line);
                JSONObject json=new JSONObject(sb.toString()); reply=json.optString("reply",sb.toString()); c.disconnect();
            } catch(Exception e){ reply="I couldn't reach the Jarvis server."; }
            String finalReply=reply;
            runOnUiThread(() -> { status.setText("ONLINE • TAP TO SPEAK"); transcript.setText("JARVIS: "+finalReply); say(finalReply); });
        }).start();
    }
    private void say(String s){ if(tts!=null) tts.speak(s,TextToSpeech.QUEUE_FLUSH,null,"jarvis"); }
    @Override public void onInit(int status){ if(status==TextToSpeech.SUCCESS) tts.setLanguage(new Locale("en","IN")); }
    @Override protected void onDestroy(){ if(tts!=null){tts.stop();tts.shutdown();} super.onDestroy(); }
}
