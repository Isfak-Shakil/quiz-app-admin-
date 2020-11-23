package com.example.quizadmin;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.collection.ArrayMap;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CategoryActivity extends AppCompatActivity {
    private ImageButton backBtn;
    private RecyclerView recyclerView;
    private Button adCatButton;
   public static List<CategoryModel> catList=new ArrayList<>();
   public static  int selectedCatInd=0;
    private FirebaseFirestore firestore;
    private Dialog loadingDialog,addCategoryDialog;

    private EditText catDialogName;
    private Button catDialogDoneButton;
    private  CategoryAdapter adapter;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        backBtn=findViewById(R.id.backBtn);
        recyclerView=findViewById(R.id.recyclerViewId);
        adCatButton=findViewById(R.id.addBtnId);

        loadingDialog=new Dialog(CategoryActivity.this);
        loadingDialog.setContentView(R.layout.loading_progressbar);
        loadingDialog.setCancelable(false);
        Objects.requireNonNull(loadingDialog.getWindow()).setBackgroundDrawableResource(R.drawable.progress_background);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
// category dialog for adding new category
       addCategoryDialog=new Dialog(CategoryActivity.this);
       addCategoryDialog.setContentView(R.layout.add_category_dialog);
       addCategoryDialog.setCancelable(false);
       addCategoryDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
// edit text of category dialog box
        catDialogName=addCategoryDialog.findViewById(R.id.catDialogName);
        catDialogDoneButton=addCategoryDialog.findViewById(R.id.catDialogDoneButton);


        firestore=FirebaseFirestore.getInstance();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        adCatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                catDialogName.getText().clear();
                addCategoryDialog.show();
            }
        });
        // done button of category dialog
        catDialogDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (catDialogName.getText().toString().isEmpty()){
                    catDialogName.setError("Enter Category Name:");
                    return;
                }
                addCatName(catDialogName.getText().toString());
            }
        });

        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        loadData();

        //test comment

    }
    private void loadData() {
       loadingDialog.show();
        catList.clear();
        firestore.collection("QUIZ").document("Categories")
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        long count = (long) doc.get("COUNT");
                        for (int i = 1; i <= count; i++) {
                            String catName = doc.getString("CAT" + String.valueOf(i)+"_NAME");
                            String catId = doc.getString("CAT" + String.valueOf(i)+"_ID");
                            catList.add(new CategoryModel(catId,catName,"0","1"));
                        }
                        adapter = new CategoryAdapter(catList);
                        recyclerView.setAdapter(adapter);
                    } else {
                        Toast.makeText(CategoryActivity.this, "No Category Found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
                else {
                    Toast.makeText(CategoryActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
                loadingDialog.dismiss();
            }
        });

    }
    private void addCatName(final String title) {
        addCategoryDialog.dismiss();
        loadingDialog.show();
       final Map<String,Object> catData=new ArrayMap<>();
        catData.put("NAME",title);
        catData.put("SETS",0);
        catData.put("COUNTER","1");
        final String doc_id=firestore.collection("QUIZ").document().getId();
        firestore.collection("QUIZ").document(doc_id)
                .set(catData).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Map<String,Object> catDoc=new ArrayMap<>();
                catDoc.put("CAT" +String.valueOf(catList.size()+1)+"_NAME",title);
                catDoc.put("CAT" +String.valueOf(catList.size()+1)+"_ID",doc_id);
                catDoc.put("COUNT",catList.size()+1);
                firestore.collection("QUIZ").document("Categories").update(catDoc)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                               Toast.makeText(CategoryActivity.this,"Category added Successfully",Toast.LENGTH_SHORT).show();
                               catList.add(new CategoryModel(doc_id,title,"0","1"));
                              adapter.notifyItemInserted(catList.size());
                              loadingDialog.dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(CategoryActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                        loadingDialog.dismiss();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CategoryActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                loadingDialog.dismiss();
            }
        });
    }


}