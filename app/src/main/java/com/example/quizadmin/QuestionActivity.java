package com.example.quizadmin;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.example.quizadmin.CategoryActivity.catList;
import static com.example.quizadmin.CategoryActivity.selectedCatInd;
import static com.example.quizadmin.SetsActivity.selected_set_ind;
import static com.example.quizadmin.SetsActivity.setIDs;

public class QuestionActivity extends AppCompatActivity {
    private ImageButton backBtn;
    private RecyclerView qRecyclerView;
    private Button qAddBtn;

   public static List<QuestionModel> questionList=new ArrayList<>();
    private QuestionAdapter adapter;

    private FirebaseFirestore firestore;
    private Dialog loadingDialog;

    @RequiresApi(api = VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);
        backBtn=findViewById(R.id.backBtn);
        qRecyclerView=findViewById(R.id.qRecyclerViewId);
        qAddBtn=findViewById(R.id.qAddBtnId);

        firestore=FirebaseFirestore.getInstance();


        loadingDialog=new Dialog(QuestionActivity.this);
        loadingDialog.setContentView(R.layout.loading_progressbar);
        loadingDialog.setCancelable(false);
        if (Build.VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
            Objects.requireNonNull(loadingDialog.getWindow()).setBackgroundDrawableResource(R.drawable.progress_background);
        }
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        qAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(QuestionActivity.this,QuestionDetailsActivity.class);
                intent.putExtra("ACTION","ADD");
                startActivity(intent);
            }
        });

        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        qRecyclerView.setLayoutManager(layoutManager);
        loadQuestion();
    }
    private void loadQuestion()
    {
       questionList.clear();

        loadingDialog.show();

        firestore.collection("QUIZ").document(catList.get(selectedCatInd).getId())
                .collection(setIDs.get(selected_set_ind)).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @RequiresApi(api = VERSION_CODES.KITKAT)
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        Map<String, QueryDocumentSnapshot> docList = new ArrayMap<>();

                        for(QueryDocumentSnapshot doc : queryDocumentSnapshots)
                        {
                            docList.put(doc.getId(),doc);
                        }

                        QueryDocumentSnapshot quesListDoc  = docList.get("QUESTIONS_LIST");

                        String count = quesListDoc.getString("COUNT");

                        for(int i=0; i < Integer.valueOf(count); i++)
                        {
                            String quesID = quesListDoc.getString("Q" + String.valueOf(i+1) + "_ID");

                            QueryDocumentSnapshot quesDoc = docList.get(quesID);

                            questionList.add(new QuestionModel(
                                    quesID,
                                    quesDoc.getString("QUESTION"),
                                    quesDoc.getString("A"),
                                    quesDoc.getString("B"),
                                    quesDoc.getString("C"),
                                    quesDoc.getString("D"),
                                    Integer.valueOf(quesDoc.getString("ANSWER"))
                            ));

                        }

                        adapter = new QuestionAdapter(questionList);
                        qRecyclerView.setAdapter(adapter);

                        loadingDialog.dismiss();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(QuestionActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                        loadingDialog.dismiss();
                    }
                });

    }
//    private void loadQuestion() {
//        questionList.clear();
//        loadingDialog.show(); // quiz name e to kono collection e nai tor
//        Log.i("id_check",catList.get(selectedCatInd).getId());
//       firestore.collection("QUIZ").document(catList.get(selectedCatInd).getId())
//               .collection(setIDs.get(selected_set_ind)).get()
//               .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                   @Override
//                   public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                      if (android.os.Build.VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
//                           Map<String,QueryDocumentSnapshot> docList=new ArrayMap<>();
//                           for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
//                               docList.put(doc.getId(),doc);
//                           }
//                           QueryDocumentSnapshot quesListDoc=docList.get("QUESTIONS_LIST");
//                           assert quesListDoc != null;
//                           String count=quesListDoc.getString("COUNT");
//                           for (int i = 0; i<Integer.parseInt(Objects.requireNonNull(count)); i++){
//                               String quesID=quesListDoc.getString("Q"+String.valueOf (i + 1) +"_ID");
//                               QueryDocumentSnapshot quesDoc=docList.get(quesID);
//                               assert quesDoc != null;
//                               questionList.add(new QuestionModel(
//                                       quesID,
//                                       quesDoc.getString("QUESTION"),
//                                       quesDoc.getString("A"),
//                                       quesDoc.getString("B"),
//                                       quesDoc.getString("C"),
//                                       quesDoc.getString("D"),
//                                      Integer.valueOf(quesDoc.getString("ANSWER"))
//
//                               ));
//                           }
//                           adapter=new QuestionAdapter(questionList);
//                           qRecyclerView.setAdapter(adapter);
//                           loadingDialog.dismiss();
//                       }
//
//                }
//               })
//               .addOnFailureListener(new OnFailureListener() {
//           @Override
//           public void onFailure(@NonNull Exception e) {
//               Toast.makeText(QuestionActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
//               loadingDialog.dismiss();
//           }
//       });
//    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter !=null) {
            adapter.notifyDataSetChanged();
        }
    }
}