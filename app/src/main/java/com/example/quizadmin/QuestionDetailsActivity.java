package com.example.quizadmin;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.util.ArrayMap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;
import java.util.Objects;

import static com.example.quizadmin.CategoryActivity.catList;
import static com.example.quizadmin.CategoryActivity.selectedCatInd;
import static com.example.quizadmin.QuestionActivity.questionList;
import static com.example.quizadmin.SetsActivity.selected_set_ind;
import static com.example.quizadmin.SetsActivity.setIDs;

public class QuestionDetailsActivity extends AppCompatActivity {
    private ImageButton backBtn;
    private TextView title;
    private EditText ques,optionA,optionB,optionC,optionD,answer;
    private Button addQB;
    private String qStr,aStr,bStr,cStr,dStr,ansStr;
    private Dialog loadingDialog;
    private String action;
    private int qID;

    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_details);
        backBtn=findViewById(R.id.backBtn);
        title=findViewById(R.id.titleQuestion);
        ques=findViewById(R.id.question);
        optionA=findViewById(R.id.optionA);
        optionB=findViewById(R.id.optionB);
        optionC=findViewById(R.id.optionC);
        optionD=findViewById(R.id.optionD);
        answer=findViewById(R.id.answer);
        addQB=findViewById(R.id.buttonAdd);

        firestore=FirebaseFirestore.getInstance();



        loadingDialog=new Dialog(QuestionDetailsActivity.this);
        loadingDialog.setContentView(R.layout.loading_progressbar);
        loadingDialog.setCancelable(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Objects.requireNonNull(loadingDialog.getWindow()).setBackgroundDrawableResource(R.drawable.progress_background);
        }
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);


        action=getIntent().getStringExtra("ACTION");
        if (action.compareTo("EDIT")==0){
        qID=getIntent().getIntExtra("Q_ID",0);
        loadData(qID);
            title.setText("Question "+String.valueOf (qID+1));
        addQB.setText("UPDATE");
        }else {

            title.setText("Question "+String.valueOf (questionList.size() + 1));
            addQB.setText("ADD");
        }



        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        addQB.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
            qStr=ques.getText().toString();
           aStr=optionA.getText().toString();
            bStr=optionB.getText().toString();
           cStr=optionC.getText().toString();
            dStr=optionD.getText().toString();
            ansStr=answer.getText().toString();

            if (qStr.isEmpty()){
                ques.setError("Enter Question");
                return;
            }
            if (aStr.isEmpty()){
                optionA.setError("Enter Option A");
                return;
            }
            if (bStr.isEmpty()){
                optionB.setError("Enter Option B");
                return;
            }
            if (cStr.isEmpty()){
                optionC.setError("Enter Option C");
                return;
            }
            if (dStr.isEmpty()){
                optionD.setError("Enter Option D");
                return;
            }
            if (ansStr.isEmpty()){
                answer.setError("Enter Answer ");
                return;
            }
                if (action.compareTo("EDIT")==0){
                    editQuestion();
                }
                else {
                    addNewQuestion();}



            }
        });


    }

    private void loadData(int id) {
        ques.setText(questionList.get(id).getQuestion());
        optionA.setText(questionList.get(id).getOptionA());
        optionB.setText(questionList.get(id).getOptionB());
        optionC.setText(questionList.get(id).getOptionC());
        optionD.setText(questionList.get(id).getOptionD());
        answer.setText(String.valueOf(questionList.get(id).getCorrectAns()));

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void addNewQuestion() {
        loadingDialog.show();
        Map<String,Object> quesDoc=new ArrayMap<>();
        quesDoc.put("QUESTION",qStr);
        quesDoc.put("A",aStr);
        quesDoc.put("B",bStr);
        quesDoc.put("C",cStr);
        quesDoc.put("D",dStr);
        quesDoc.put("ANSWER",ansStr);

        final String doc_id= firestore.collection("QUIZ").document(catList.get(selectedCatInd).getId())
                .collection(setIDs.get(selected_set_ind)).document().getId();

        firestore.collection("QUIZ").document(catList.get(selectedCatInd).getId())
                .collection(setIDs.get(selected_set_ind)).document(doc_id)
                .set(quesDoc)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Map<String,Object> quesDoc=new ArrayMap<>();
                        quesDoc.put("Q"+String.valueOf(questionList.size()+1) + "_ID",doc_id);
                        quesDoc.put("COUNT",String.valueOf(questionList.size() + 1));
                        firestore.collection("QUIZ").document(catList.get(selectedCatInd).getId())
                                .collection(setIDs.get(selected_set_ind)).document("QUESTIONS_LIST")
                                .update(quesDoc)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(QuestionDetailsActivity.this,"Question Added Successfully",Toast.LENGTH_SHORT).show();
                                        questionList.add(new QuestionModel(doc_id,qStr,aStr,bStr,cStr,dStr,Integer.valueOf(ansStr)));
                                        loadingDialog.dismiss();
                                        QuestionDetailsActivity.this.finish();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(QuestionDetailsActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                                        loadingDialog.dismiss();
                            }
                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(QuestionDetailsActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                loadingDialog.dismiss();
            }
        });
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void editQuestion(){
        loadingDialog.show();
        Map<String,Object> quesData=new ArrayMap<>();
       quesData.put("QUESTION",qStr);
       quesData.put("A",aStr);
       quesData.put("B",bStr);
       quesData.put("C",cStr);
       quesData.put("D",dStr);
       quesData.put("ANSWER",ansStr);

       firestore.collection("QUIZ").document(catList.get(selectedCatInd).getId())
               .collection(setIDs.get(selected_set_ind)).document(questionList.get(qID).getQuesID())
               .set(quesData)
               .addOnSuccessListener(new OnSuccessListener<Void>() {
                   @Override
                   public void onSuccess(Void aVoid) {
                    Toast.makeText(QuestionDetailsActivity.this,"Question Updated Successfully",Toast.LENGTH_SHORT).show();
                    questionList.get(qID).setQuestion(qStr);
                    questionList.get(qID).setOptionA(aStr);
                    questionList.get(qID).setOptionB(bStr);
                    questionList.get(qID).setOptionC(cStr);
                    questionList.get(qID).setOptionD(dStr);
                    questionList.get(qID).setCorrectAns(Integer.valueOf(ansStr));

                        loadingDialog.dismiss();
                        QuestionDetailsActivity.this.finish();
                   }
               }).addOnFailureListener(new OnFailureListener() {
           @Override
           public void onFailure(@NonNull Exception e) {
               Toast.makeText(QuestionDetailsActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
               loadingDialog.dismiss();
           }
       });

    }
}