package jp.techacademy.katsuhito.muratomi.qa_app;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class QuestionDetailActivity extends AppCompatActivity {
    private ListView mListView;
    private Question mQuestion;
    private QuestionDetailListAdapter mAdapter;
    private Button button;
    private FirebaseAuth Auth;
    private DatabaseReference mAnswerRef;
    private boolean favoriteflag;
    FloatingActionButton favorite;

    private ChildEventListener mEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap) dataSnapshot.getValue();

            String answerUid = dataSnapshot.getKey();

            for (Answer answer : mQuestion.getAnswers()) {
                // 同じAnswerUidのものが存在しているときは何もしない
                if (answerUid.equals(answer.getAnswerUid())) {
                    return;
                }
            }

            String body = (String) map.get("body");
            String name = (String) map.get("name");
            String uid = (String) map.get("uid");

            Answer answer = new Answer(body, name, uid, answerUid);
            mQuestion.getAnswers().add(answer);
            Log.d("test","回答が追加された");
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_detail);
        Log.d("test", "オンクリエイトが呼ばれている");

        favorite = (FloatingActionButton) findViewById(R.id.favorite);


        // 渡ってきたQuestionのオブジェクトを保持する
        Bundle extras = getIntent().getExtras();
        mQuestion = (Question) extras.get("question");
        favoriteflag = (boolean) extras.get("qflag");
        Log.d("test", "渡されたフラグは" + String.valueOf(favoriteflag));


        if (favoriteflag == true) {
            favorite.setImageResource(R.drawable.btn_pressed);
            Log.d("test", "お気に入り登録はすでにされているよ");
        } else {
            Log.d("test", "お気に入り登録はされていないよ");
            favorite.setImageResource(R.drawable.btn);
        }


        favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (favoriteflag == false) {
                    Log.d("test", String.valueOf(favoriteflag));
                    int mgenre = mQuestion.getGenre();
                    Auth = FirebaseAuth.getInstance();
                    FirebaseUser user = Auth.getCurrentUser();
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                    DatabaseReference ref = reference.child(Const.UsersPATH).child(user.getUid()).child("contents");
                    String body = mQuestion.getBody();
                    String name = mQuestion.getName();
                    String title = mQuestion.getTitle();
                    String qid = mQuestion.getQuestionUid();
                    int genre = mQuestion.getGenre();
                    String uid = mQuestion.getUid();
                    ArrayList answers = mQuestion.getAnswers();
                    Map<String, String> data = new HashMap<String, String>();

                    data.put("uid", uid);
                    data.put("body", body);
                    data.put("name", name);
                    data.put("title", title);
                    data.put("genre", String.valueOf(genre));
                    String key = ref.push().getKey();
                    ref.child(qid).setValue(data);
                    Log.d("test", "pushされたのは" + String.valueOf(key));
                    DatabaseReference answerref = ref.child(qid).child(Const.AnswersPATH);
                    for (int i=0;i<answers.size();i++){
                        answerref.push().setValue(answers.get(i));

                    }


                    favorite.setImageResource(R.drawable.btn_pressed);
                    favoriteflag = true;
                    //answerref.push().setValue(answers);

                }


            }
        });


        setTitle(mQuestion.getTitle());

        // ListViewの準備
        mListView = (ListView) findViewById(R.id.listView);
        mAdapter = new QuestionDetailListAdapter(this, mQuestion);
        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ログイン済みのユーザーを取得する
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (user == null) {
                    // ログインしていなければログイン画面に遷移させる
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                } else {
                    // Questionを渡して回答作成画面を起動する
                    // TODO:
                    Intent intent = new Intent(getApplicationContext(), AnswerSendActivity.class);
                    intent.putExtra("question", mQuestion);
                    startActivity(intent);
                }
            }
        });

        DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();
        mAnswerRef = dataBaseReference.child(Const.ContentsPATH).child(String.valueOf(mQuestion.getGenre())).child(mQuestion.getQuestionUid()).child(Const.AnswersPATH);
        mAnswerRef.addChildEventListener(mEventListener);
    }
}