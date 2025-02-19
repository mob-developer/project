package ir.mk.learnx.quiz;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.ArrayList;

import ir.mk.learnx.Home;
import ir.mk.learnx.R;
import ir.mk.learnx.model.Server;
import okhttp3.CacheControl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class QuizActivity extends AppCompatActivity {


    private int thisStep;
    private int allStep;
    private int lesson;

    private String question;
    private String option1;
    private String option2;
    private String option3;
    private String option4;
    private final Integer correctOption = 1;
    private boolean ended = false;
    private boolean ended2 = false;

    private Handler handler;
    private static final int GET_QUESTIONS = 1;
    OkHttpClient client = new OkHttpClient();
    private ImageView loadingImageView;
    private int iq;
    private TextView iqTextView;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint({"HandlerLeak", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        SharedPreferences sharedPreferences = getSharedPreferences(Server.MY_PREFS_NAME, MODE_PRIVATE);
        iq = sharedPreferences.getInt("iq", 0);
        iqTextView = findViewById(R.id.iq_textView);
        iqTextView.setText("IQ " + iq);

        lesson = getIntent().getIntExtra("lesson", -1);
        thisStep = getIntent().getIntExtra("step", 1);
        allStep = getIntent().getIntExtra("allStep", 1);

        ProgressBar progressBar = findViewById(R.id.progressBarQuiz);
        progressBar.setMax(allStep);
        progressBar.setProgress(thisStep, true);

        Button report = findViewById(R.id.report);
        report.setOnClickListener(v -> {
            Toast.makeText(this, "گزارش شما با موفقیت ثبت شد", Toast.LENGTH_LONG).show();
        });

        Button positive = findViewById(R.id.positive);
        positive.setOnClickListener(v -> {
            Toast.makeText(this, "نظر شما ثبت شد", Toast.LENGTH_LONG).show();
        });

        Button negative = findViewById(R.id.negative);
        negative.setOnClickListener(v -> {
            Toast.makeText(this, "نظر شما ثبت شد", Toast.LENGTH_LONG).show();
        });


        loadingImageView = findViewById(R.id.learn_quiz_loading);
        Glide.with(this).load(R.mipmap.loading_gif).into(loadingImageView);


        handler = new Handler() {
            @SuppressLint("HandlerLeak")
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == GET_QUESTIONS) {
                    if (msg.arg1 == 1) {
                        showQuestion();
                    } else {
                        Log.e("mylog", "error in handle msg");
                    }
                }
            }
        };


        Button goNext = findViewById(R.id.learn_quiz_go_next);
        goNext.setOnClickListener(v -> {
            if (thisStep < allStep) {
                Intent intent = new Intent(this, QuizActivity.class);
                intent.putExtra("lesson", lesson);
                intent.putExtra("step", thisStep + 1);
                intent.putExtra("allStep", allStep);
                startActivity(intent);
            } else {
                Intent intent = new Intent(this, EndQuiz.class);
                startActivity(intent);
            }
        });


        Thread threadGetQuestion = new Thread(new Runnable() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = GET_QUESTIONS;
                try {
                    question = QuizActivity.this.run(Server.SERVER_URL_QUIZ + lesson);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                message.arg1 = 1;
                handler.sendMessage(message);
            }
        });
        threadGetQuestion.start();
    }


    private void showQuestion() {
        String[] temp = question.split("==@@");
        question = temp[1];
        option1 = temp[2];
        option2 = temp[3];
        option3 = temp[4];
        option4 = temp[5];


        TextView QTextView = findViewById(R.id.learn_quiz_Question);
        QTextView.setText(question);

        Button button1 = findViewById(R.id.btn_quiz_ans1);
        Button button2 = findViewById(R.id.btn_quiz_ans2);
        Button button3 = findViewById(R.id.btn_quiz_ans3);
        Button button4 = findViewById(R.id.btn_quiz_ans4);

        ArrayList<Button> questionArrayList = new ArrayList<>();
        questionArrayList.add(button1);
        questionArrayList.add(button2);
        questionArrayList.add(button3);
        questionArrayList.add(button4);
        ArrayList<Button> questionArrayList2 = new ArrayList<>();
        questionArrayList2.add(button1);
        questionArrayList2.add(button2);
        questionArrayList2.add(button3);
        questionArrayList2.add(button4);


        ConstraintLayout constraintLayout = findViewById(R.id.activity_question);
        constraintLayout.setOnClickListener(v -> {
            if (ended && !ended2) {
                ConstraintLayout constraintLayout1 = findViewById(R.id.quiz_end);
                constraintLayout1.setVisibility(View.VISIBLE);
                ended2 = true;
            }
        });

        for (Button button : questionArrayList) {
            button.setVisibility(View.VISIBLE);
            button.setOnClickListener(v -> {
                if (!ended && button.getTag() == correctOption) {
                    iq += 5;
                    button.setBackgroundColor(Color.rgb(0, 255, 0));
                    TextView iqAddTextView = findViewById(R.id.addIQ);
                    iqAddTextView.setVisibility(View.VISIBLE);
                    Log.d("asdasd", "" + iqAddTextView.getHeight());
                    iqAddTextView.animate().setDuration(1000).translationY(-(int) constraintLayout.getHeight() / 2 + 100);
                    iqAddTextView.animate().setDuration(1000).translationX((int) constraintLayout.getWidth() / 2 - 100);
                    iqAddTextView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            iqAddTextView.setVisibility(View.GONE);
                            iqTextView.setText("IQ "+iq);
                        }
                    }, 980);
                    SharedPreferences.Editor editor = getSharedPreferences(Server.MY_PREFS_NAME, MODE_PRIVATE).edit();
                    editor.putInt("iq", iq);
                    editor.apply();
                } else if (!ended) {
                    button.setBackgroundColor(Color.rgb(255, 0, 0));
                    for (Button b : questionArrayList2) {
                        if (b.getTag() == correctOption) {
                            b.setBackgroundColor(Color.rgb(0, 255, 0));
                        }
                    }
                }
                if (ended && !ended2) {
                    ConstraintLayout constraintLayout1 = findViewById(R.id.quiz_end);
                    constraintLayout1.setVisibility(View.VISIBLE);
                    ended2 = true;
                }
                ended = true;


            });
        }

        int random = (int) (Math.random() * 4);
        questionArrayList.get(random).setText(option1);
        questionArrayList.get(random).setTag(correctOption);
        questionArrayList.remove(random);

        random = (int) (Math.random() * 3);
        questionArrayList.get(random).setText(option2);
        questionArrayList.remove(random);

        random = (int) (Math.random() * 2);
        questionArrayList.get(random).setText(option3);
        questionArrayList.remove(random);

        random = 0;
        questionArrayList.get(random).setText(option4);
        questionArrayList.remove(random);


        ImageView imageView = findViewById(R.id.learn_quiz_loading);
        imageView.setVisibility(View.GONE);
        TextView textView = findViewById(R.id.learn_quiz_loading_text);
        textView.setVisibility(View.GONE);
    }


    private String run(String url) throws IOException {
        Request request = new Request.Builder()
                .cacheControl(new CacheControl
                        .Builder()
                        .noCache()
                        .build())
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }


    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("خروج")
                .setMessage("آیا می خواهید از مسابقه خارج شوید؟")
                .setNegativeButton("خیر", null)
                .setPositiveButton("بله", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        Intent intent = new Intent(QuizActivity.this, Home.class);
                        QuizActivity.this.startActivity(intent);
                        finish();
                    }
                }).create().show();
    }

}