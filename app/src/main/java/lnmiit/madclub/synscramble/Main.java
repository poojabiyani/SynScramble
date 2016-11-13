package lnmiit.madclub.synscramble;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;


/**
 * Created by pooja on 20/10/16.
 */
public class Main extends Activity implements OnClickListener, OnTouchListener {
    String k= "score";
    SharedPreferences sc;
    public static final String MyPREFERENCES = "myprefs";
    public static final  String value = "key";

    int updated_score;
    private static final String DEBUG_TAG = "Response";
    final Context context = this;
    private String word = new String();
    HashMap<String, Boolean> hashMap = new HashMap<>();
    ArrayList<String> arr= new ArrayList();
    private String wordList[] = new String[10000];
    private Random generator = new Random();
    private String answerString = new String();
    private EditText answerText;
    private TextView info, hint_txt, score;
    private LinearLayout scrambledLayout;
    private MediaPlayer correctSound;
    private MediaPlayer wrongSound;
    private Button nextButton;
    private Button clearButton, hint, hint1 ;
    private String str, hint_str, key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Bundle bundle = getIntent().getExtras();

        sc = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        int myIntValue = sc.getInt(value, 200);

        str = bundle.getString("var");
        nextButton = (Button) findViewById(R.id.nextButton);
        score = (TextView) findViewById(R.id.score);
        updated_score= myIntValue;
        score.setText("SCORE : "+ updated_score);
        hint = (Button) findViewById(R.id.button);
        hint1 = (Button) findViewById(R.id.button4);
        nextButton.setOnClickListener(this);
        clearButton = (Button) findViewById(R.id.clear);
        hint_txt = (TextView) findViewById(R.id.hint_text);
        clearButton.setOnClickListener(this);
        answerText = (EditText) findViewById(R.id.answer);
        correctSound = MediaPlayer.create(getApplicationContext(), R.raw.correctsound);
        wrongSound = MediaPlayer.create(getApplicationContext(), R.raw.wrongsound);

        //Populating List of Words from dictionary
        Scanner scan = new Scanner(getResources().openRawResource(R.raw.words));
        try {
            int i = 0;
            while (scan.hasNext()) {
                String s = scan.next();
                wordList[i++]= s;
                hashMap.put(s,false);
            }
        } catch (Exception e) {
            //e.printStackTrace();
        } finally {
            scan.close();
        }

        initializeGame();

        hint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

                // set title
                alertDialogBuilder.setTitle("HINT");

                // set dialog message
                alertDialogBuilder
                        .setMessage("Do you want to get the synonyms of the word for 60 coins?")
                        .setCancelable(false)
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        })
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                // if this button is clicked, just close
                                if(updated_score<60)
                                {
                                    Toast.makeText(Main.this, "You have insufficient coins", Toast.LENGTH_SHORT).show();
                                    dialog.cancel();
                                }
                                else
                                {
                                    dialog.cancel();
                                    Log.d(DEBUG_TAG, "The word is: " + word);
                                    String link= "https://words.bighugelabs.com/api/2/4923dbd89f7fc91a953a716e04fb4dec/"+word+"/json";
                                    Log.d(DEBUG_TAG, "The link is: " + link);
                                    updated_score= updated_score- 60;
                                    score.setText("SCORE : " +updated_score);
                                    SavePreferences(updated_score);
                                    ConnectivityManager connMgr = (ConnectivityManager)
                                            getSystemService(Context.CONNECTIVITY_SERVICE);
                                    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                                    if (networkInfo != null && networkInfo.isConnected()) {
                                        new DownloadWebpageTask().execute(link);
                                    } else {
                                        hint_txt.setText("No network connection available.");
                                    }
                                }
                            }
                        });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();
            }
        });

        hint1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

                // set title
                alertDialogBuilder.setTitle("HINT");

                // set dialog message
                alertDialogBuilder
                        .setMessage("Do you want to reveal the first letter for 40 coins?")
                        .setCancelable(false)
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        })
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                // if this button is clicked, just close
                                if(updated_score<40)
                                {
                                    Toast.makeText(Main.this, "You have insufficient coins", Toast.LENGTH_SHORT).show();
                                    dialog.cancel();
                                }
                                else
                                {
                                    updated_score= updated_score- 40;
                                    score.setText("SCORE : " +updated_score);
                                    SavePreferences(updated_score);

                                    String s= word.substring(0,1);
                                    Toast.makeText(Main.this, s+" is the first letter", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();

            }
        });

    }


    public void SavePreferences(int v){
        //SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sc.edit();

        editor.putInt(value, v);
        editor.commit();

    }

    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);

            } catch (IOException e) {
                return "Synonyms not available";
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String res) {

            hint_txt.setText(res);
        }
    }

    // Given a URL, establishes an HttpUrlConnection and retrieves
// the web page content as a InputStream, which it returns as
// a string.
    private String downloadUrl(String myurl) throws IOException {
        InputStream is = null;
        // Only display the first 500 characters of the retrieved
        // web page content.
        int len = 500;

        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            Log.d(DEBUG_TAG, "The response is: " + response);
            is = conn.getInputStream();

            // Convert the InputStream into a string
            String contentAsString = readIt(is, len);
            //  parseResult(contentAsString);
            return contentAsString;

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }


    // Reads an InputStream and converts it to a String.
    // Reads an InputStream and converts it to a String.
    public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);;
        return ((new String(buffer)));
    }


    /**
     * Parsing the feed results and get the list
     *
     * @param //result
     */
//   private String parseResult(String result) {
//        try {
//           JSONObject json1 = new JSONObject(result);
//            JSONObject json2 = json1.getJSONObject("noun");
//            JSONArray posts = json2.optJSONArray("syn");
//           // JSONObject post = posts.optJSONObject(0);
//                /*JSONArray attachments = post.getJSONArray("attachments");
//                if (null != attachments && attachments.length() > 0) {
//                    JSONObject attachment = attachments.getJSONObject(0);
//                    if (attachment != null) */
//                // item.setImage(attachment.getString("url"));
//            for(int i=0; i<posts.length(); i++) {
//                arr.set(i, posts.getString(i));
//            }
//            Log.d(DEBUG_TAG, "The response is: " + arr);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        return key;
//    }



    public void initializeGame(){

        clearButton.setEnabled(Boolean.FALSE);

        //getting the word to scramble
        while(true) {
            word = getNewWord();
            if(hashMap.get(word)==false)
                break;
        }
        hashMap.put(word, true);
        //scrambling the word
        String scrambledWord = scramble(word);

        //getting Layout for Scrambled word
        scrambledLayout = (LinearLayout) findViewById(R.id.scrambled);

        //initializing information TextView
        info = (TextView) findViewById(R.id.inforamtion);


        //Populating TextViews
        for(int i = 0; i < scrambledWord.length(); i++) {
            TextView letter = new TextView(this);
            letter.setText("");
            letter.setText(Character.toString(scrambledWord.charAt(i)));
            letter.setTextSize(75);
            letter.setPadding(7, 7, 7, 7);
            letter.setOnClickListener(this);
            letter.setId(i);
            letter.setOnTouchListener(this);
            scrambledLayout.addView(letter);
        }
    }

    public void onClick(View v){

        TextView clicked = (TextView) findViewById(v.getId());

        if(answerText.getText().toString().length() == 0){
            clearButton.setEnabled(Boolean.FALSE);
        }

        if(clearButton.getId() == v.getId()){
            if(answerText.getText().toString().length() == 1){
                clearButton.setEnabled(Boolean.FALSE);
            }
            answerText.setText(answerText.getText().toString().substring(0, answerText.getText().toString().length()-1));
            answerString = answerString.substring(0, answerString.length()-1);
        }

        else if(nextButton.getId() == v.getId()) {
            clearButton.setEnabled(Boolean.FALSE);
            scrambledLayout.removeAllViews();
            answerText.setText("");
            answerString = "";
            hint_txt.setText("");
            answerText.setTextColor(Color.rgb(0, 0, 0));
            info.setTextColor(Color.DKGRAY);
            info.setText("Unscramble the below word");
            info.setBackgroundColor(Color.TRANSPARENT);
            initializeGame();
        }
        else {
            clearButton.setEnabled(Boolean.TRUE);
            answerText.setText(answerText.getText().toString() + clicked.getText());
            //clicked.setTextColor(Color.RED);
            answerString += clicked.getText();

            try {
                if(answerString.length() == word.length()){
                    if(answerString.equalsIgnoreCase(word)){
                        clearButton.setEnabled(Boolean.FALSE);
                        info.setBackgroundColor(Color.rgb(33, 196, 18));
                        info.setTextColor(Color.WHITE);
                        info.setText("Correct!");
                        answerText.setTextColor(Color.rgb(33, 196, 18));
                        correctSound.start();
                        updated_score= updated_score+ 20;
                        score.setText("SCORE : " +updated_score);
                        SavePreferences(updated_score);
                        new Handler().postDelayed(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                scrambledLayout.removeAllViews();
                                answerText.setText("");
                                answerString = "";
                                answerText.setTextColor(Color.rgb(0, 0, 0));
                                info.setTextColor(Color.DKGRAY);
                                info.setText("Unscramble the below word");
                                info.setBackgroundColor(Color.TRANSPARENT);
                                initializeGame();
                            }
                        }, 1200);
                    }
                    else {
                        clearButton.setEnabled(Boolean.FALSE);
                        info.setBackgroundColor(Color.rgb(255, 0, 0));
                        info.setTextColor(Color.WHITE);
                        info.setText("Incorrect!");
                        answerText.setTextColor(Color.rgb(255, 0, 0));
                        wrongSound.start();
                        //vibrator.vibrate(400);

                        new Handler().postDelayed(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                answerString = "";
                                answerText.setText("");
                                answerText.setTextColor(Color.rgb(0, 0, 0));
                                info.setTextColor(Color.DKGRAY);
                                info.setText("Give it another shot!");
                                info.setBackgroundColor(Color.TRANSPARENT);
                            }
                        }, 1200);
                    }
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /*
     * Returns a random word from the Populated WordList Array of Strings
     */
    public String getNewWord(){
        String temp;
        if(str.equals("easy"))
        {
            while(true) {
                int randomWord = generator.nextInt(wordList.length);
                temp = wordList[randomWord];
                if(temp.length()>=3 && temp.length()<=4)
                    return temp;
            }
        }
        else if(str.equals("medium"))
        {
            while(true) {
                int randomWord = generator.nextInt(wordList.length);
                temp = wordList[randomWord];
                if(temp.length()>=4 && temp.length()<=5)
                    return temp;
            }
        }
        else
        {
            while(true) {
                int randomWord = generator.nextInt(wordList.length);
                temp = wordList[randomWord];
                if(temp.length()>=5 && temp.length()<=7)
                    return temp;
            }
        }
    }

    /*
     * Accepts String, and returns a scrambled String
     */
    public String scramble(String wordToScramble){
        String scrambled = "";
        int randomNumber;

        boolean letter[] = new boolean[wordToScramble.length()];

        do {
            randomNumber = generator.nextInt(wordToScramble.length());
            if(letter[randomNumber] == false){
                scrambled += wordToScramble.charAt(randomNumber);
                letter[randomNumber] = true;
            }
        } while(scrambled.length() < wordToScramble.length());

        if(scrambled.equals(wordToScramble))
            scramble(word);

        return scrambled;
    }

    @Override
    public boolean onTouch(View v, MotionEvent motion) {
        TextView touched = (TextView) findViewById(v.getId());

        if(motion.getAction() == MotionEvent.ACTION_DOWN){
            touched.setTextColor(Color.rgb(0, 189, 252));
        }
        else if(motion.getAction() == MotionEvent.ACTION_UP){
            touched.setTextColor(Color.rgb(0, 0, 0));
        }
        return false;
    }
}
