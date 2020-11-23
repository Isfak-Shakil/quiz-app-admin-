package com.example.quizadmin;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.util.ArrayMap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.example.quizadmin.CategoryActivity.catList;
import static com.example.quizadmin.CategoryActivity.selectedCatInd;

public class SetsActivity extends AppCompatActivity {
    private TextView setToolbarText;
    private RecyclerView setRecyclerView;
    private Button setAddBtn;
    private ImageButton  backBtn;
    public static List<String> setIDs=new ArrayList<>();
    public  static  int selected_set_ind =0;

    private SetsAdapter adapter;
    private FirebaseFirestore firestore;
    private Dialog loadingDialog,addCategoryDialog;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sets);

        setToolbarText=findViewById(R.id.setToolbarTextId);
        setRecyclerView=findViewById(R.id.setRecyclerViewId);
        setAddBtn=findViewById(R.id.setAddBtnId);
        backBtn=findViewById(R.id.backBtn);




        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        loadingDialog=new Dialog(SetsActivity.this);
        loadingDialog.setContentView(R.layout.loading_progressbar);
        loadingDialog.setCancelable(false);
        Objects.requireNonNull(loadingDialog.getWindow()).setBackgroundDrawableResource(R.drawable.progress_background);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        setAddBtn.setText("Add new sets");
        firestore=FirebaseFirestore.getInstance();
        setAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewSet();
            }
        });
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        setRecyclerView.setLayoutManager(layoutManager);
        loadSets();
    }



    private void loadSets() {
        setIDs.clear();
        loadingDialog.show();
            firestore.collection("QUIZ").document(catList.get(selectedCatInd).getId())
                    .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                   long noOfSets=(long)documentSnapshot.get("SETS");
                    for (int i=1;i<=noOfSets;i++){
                        setIDs.add(documentSnapshot.getString("SET"+String.valueOf(i)+"_ID"));
                    }
                    catList.get(selectedCatInd).setSetCounter(documentSnapshot.getString("COUNTER"));
                    catList.get(selectedCatInd).setNoOfSets(String.valueOf(noOfSets));

                    adapter=new SetsAdapter(setIDs);
                    setRecyclerView.setAdapter(adapter);
                    loadingDialog.dismiss();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Toast.makeText(SetsActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                    loadingDialog.dismiss();
                }
            });


    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void addNewSet() {
        loadingDialog.show();
        final String curr_Cat_Id=catList.get(selectedCatInd).getId();
        final String curr_Counter=catList.get(selectedCatInd).getSetCounter();
        Map<String,Object> qData=new ArrayMap<>();
        qData.put("COUNT","0");
        firestore.collection("QUIZ").document(curr_Cat_Id)
                .collection(curr_Counter).document("QUESTIONS_LIST")
                .set(qData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Map<String,Object> catDoc=new ArrayMap<>();
                        catDoc.put("COUNTER",String.valueOf(Integer.valueOf(curr_Counter)+1));
                        catDoc.put("SET"+String.valueOf(setIDs.size()+1)+"_ID",curr_Counter);
                        catDoc.put("SETS",setIDs.size()+1);
                        firestore.collection("QUIZ").document(curr_Cat_Id)
                                .update(catDoc)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                      Toast.makeText(SetsActivity.this,"Successfull",Toast.LENGTH_SHORT).show();
                                      setIDs.add(curr_Counter);
                                      catList.get(selectedCatInd).setNoOfSets(String.valueOf(setIDs.size()));
                                      catList.get(selectedCatInd).setSetCounter(String.valueOf(Integer.valueOf(curr_Counter)+1));
                                      adapter.notifyItemInserted(setIDs.size());
                                      loadingDialog.dismiss();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(SetsActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                                loadingDialog.dismiss();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SetsActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                loadingDialog.dismiss();
            }
        });


    }
}