package tech.chaibyte.jarvis;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.os.*;
import android.provider.Settings;
import android.speech.*;
import android.speech.tts.TextToSpeech;
import android.view.*;
import android.view.animation.*;
import android.widget.*;
import org.json.JSONObject;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class MainActivity extends Activity implements TextToSpeech.OnInitListener {
  static final String WORKER="https://jarvis-784d.chaitanya6077.workers.dev/";
  TextView status, conversation; View orb; TextToSpeech tts; SpeechRecognizer sr; boolean listening=false;
  final int MIC=9; Handler h=new Handler(Looper.getMainLooper());
  @Override public void onCreate(Bundle b){super.onCreate(b); buildUI(); tts=new TextToSpeech(this,this); if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED) requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},MIC); else startHandsFree();}
  TextView tv(String s,int z,int c){TextView v=new TextView(this);v.setText(s);v.setTextSize(z);v.setTextColor(c);v.setGravity(Gravity.CENTER);return v;}
  GradientDrawable bg(int color,float radius,int stroke,String sc){GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(radius);if(stroke>0)g.setStroke((int)stroke,Color.parseColor(sc));return g;}
  void buildUI(){
    LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setGravity(Gravity.CENTER_HORIZONTAL);root.setPadding(38,65,38,40);root.setBackgroundColor(Color.rgb(2,7,13));
    TextView tiny=tv("A  R  C   R E A C T O R   //   ONLINE",11,Color.parseColor("#6B8795"));root.addView(tiny,new LinearLayout.LayoutParams(-1,-2));
    TextView title=tv("J A R V I S",36,Color.parseColor("#7DF9FF"));title.setPadding(0,28,0,8);root.addView(title);
    status=tv("INITIALIZING NEURAL INTERFACE",13,Color.parseColor("#8AEAFF"));root.addView(status);
    Space sp=new Space(this);root.addView(sp,new LinearLayout.LayoutParams(1,70));
    FrameLayout ring=new FrameLayout(this);LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(330,330);ring.setBackground(bg(Color.TRANSPARENT,200,3,"#164A5A"));
    orb=new View(this);GradientDrawable og=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{Color.parseColor("#BFFFFF"),Color.parseColor("#00CFE8"),Color.parseColor("#073D58")});og.setShape(GradientDrawable.OVAL);orb.setBackground(og);FrameLayout.LayoutParams op=new FrameLayout.LayoutParams(190,190,Gravity.CENTER);ring.addView(orb,op);root.addView(ring,rp);pulse();
    TextView hint=tv("Say  “HEY JARVIS”  then your command",14,Color.parseColor("#9EB9C3"));hint.setPadding(0,55,0,20);root.addView(hint);
    conversation=tv("Listening in hands-free mode…",18,Color.WHITE);conversation.setPadding(18,22,18,22);conversation.setBackground(bg(Color.parseColor("#07141D"),26,2,"#123A49"));root.addView(conversation,new LinearLayout.LayoutParams(-1,-2));
    Button b=new Button(this);b.setText("◉  SPEAK NOW");b.setTextSize(16);b.setTextColor(Color.parseColor("#BFFFFF"));b.setBackground(bg(Color.parseColor("#09202B"),50,2,"#19DDF2"));b.setOnClickListener(v->listenOnce());LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,145);bp.setMargins(0,45,0,0);root.addView(b,bp);
    setContentView(root);
  }
  void pulse(){ScaleAnimation a=new ScaleAnimation(.82f,1.08f,.82f,1.08f,Animation.RELATIVE_TO_SELF,.5f,Animation.RELATIVE_TO_SELF,.5f);a.setDuration(1200);a.setRepeatMode(Animation.REVERSE);a.setRepeatCount(Animation.INFINITE);a.setInterpolator(new AccelerateDecelerateInterpolator());orb.startAnimation(a);}
  void startHandsFree(){if(!SpeechRecognizer.isRecognitionAvailable(this)){status.setText("SPEECH SERVICE UNAVAILABLE");return;} if(sr!=null)sr.destroy();sr=SpeechRecognizer.createSpeechRecognizer(this);sr.setRecognitionListener(new RecognitionListener(){public void onReadyForSpeech(Bundle p){listening=true;status.setText("LISTENING • SAY HEY JARVIS");} public void onResults(Bundle b){listening=false;ArrayList<String> r=b.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);if(r!=null&&!r.isEmpty())handleHeard(r.get(0));restart();} public void onError(int e){listening=false;restart();} public void onPartialResults(Bundle b){} public void onBeginningOfSpeech(){} public void onRmsChanged(float r){} public void onBufferReceived(byte[] b){} public void onEndOfSpeech(){} public void onEvent(int t,Bundle b){} }); restart();}
  Intent speechIntent(){Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"en-IN");i.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS,true);i.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,3);return i;}
  void restart(){h.postDelayed(()->{try{if(sr!=null&&!listening)sr.startListening(speechIntent());}catch(Exception ignored){}},650);}
  void listenOnce(){try{if(sr!=null){sr.cancel();listening=false;} sr.startListening(speechIntent());status.setText("LISTENING TO COMMAND");}catch(Exception e){startHandsFree();}}
  void handleHeard(String heard){String low=heard.toLowerCase(Locale.ROOT);int p=low.indexOf("jarvis"); if(p>=0){String cmd=heard.substring(Math.min(heard.length(),p+6)).trim().replaceFirst("^[,.:;-]+","").trim();if(cmd.isEmpty()){say("Yes?");conversation.setText("JARVIS: Yes?");return;} send(cmd);} }
  void send(String cmd){status.setText("PROCESSING");conversation.setText("YOU: "+cmd);new Thread(()->{String reply;try{HttpURLConnection c=(HttpURLConnection)new URL(WORKER).openConnection();c.setRequestMethod("POST");c.setRequestProperty("Content-Type","application/json");c.setConnectTimeout(10000);c.setReadTimeout(20000);c.setDoOutput(true);byte[] body=new JSONObject().put("command",cmd).toString().getBytes(StandardCharsets.UTF_8);try(OutputStream o=c.getOutputStream()){o.write(body);}InputStream in=c.getResponseCode()<400?c.getInputStream():c.getErrorStream();BufferedReader br=new BufferedReader(new InputStreamReader(in,StandardCharsets.UTF_8));StringBuilder s=new StringBuilder();String l;while((l=br.readLine())!=null)s.append(l);JSONObject j=new JSONObject(s.toString());reply=j.optString("reply",s.toString());c.disconnect();}catch(Exception e){reply="I cannot reach the Jarvis server right now.";}String x=reply;runOnUiThread(()->{status.setText("ONLINE • LISTENING");conversation.setText("JARVIS: "+x);say(x);});}).start();}
  void say(String s){if(tts!=null){tts.setSpeechRate(.92f);tts.setPitch(.86f);tts.speak(s,TextToSpeech.QUEUE_FLUSH,null,"jarvis");}}
  public void onInit(int s){if(s==TextToSpeech.SUCCESS){tts.setLanguage(new Locale("en","GB"));tts.setSpeechRate(.92f);tts.setPitch(.86f);}}
  @Override public void onRequestPermissionsResult(int r,String[] p,int[] g){super.onRequestPermissionsResult(r,p,g);if(r==MIC&&g.length>0&&g[0]==PackageManager.PERMISSION_GRANTED)startHandsFree();}
  @Override protected void onResume(){super.onResume();if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED&&!listening)startHandsFree();}
  @Override protected void onDestroy(){h.removeCallbacksAndMessages(null);if(sr!=null)sr.destroy();if(tts!=null){tts.stop();tts.shutdown();}super.onDestroy();}
}
