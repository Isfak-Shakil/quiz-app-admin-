package com.example.quizadmin;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.example.quizadmin.CategoryActivity.catList;
import static com.example.quizadmin.CategoryActivity.selectedCatInd;
import static com.example.quizadmin.SetsActivity.selected_set_ind;

public class SetsAdapter extends RecyclerView.Adapter<SetsAdapter.Viewholder> {
        private List<String> setIDs;

    public SetsAdapter(List<String> setIDs) {
        this.setIDs = setIDs;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.cat_item_layout,parent,false);
        return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {
        String setId=setIDs.get(position);
        holder.setData(position,setId,this);

    }

    @Override
    public int getItemCount() {
        return setIDs.size();
    }

    public  class Viewholder extends RecyclerView.ViewHolder{
        private TextView   setSetName;
        private ImageButton setDeleteBtn;
        private Dialog loadingDialog;

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        public Viewholder(@NonNull View itemView) {
            super(itemView);
            setSetName=itemView.findViewById(R.id.textView);
            setDeleteBtn=itemView.findViewById(R.id.deleteBtn);


            loadingDialog=new Dialog(itemView.getContext());
            loadingDialog.setContentView(R.layout.loading_progressbar);
            loadingDialog.setCancelable(false);
            Objects.requireNonNull(loadingDialog.getWindow()).setBackgroundDrawableResource(R.drawable.progress_background);
            loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        public void setData(final int position, final String setId, final SetsAdapter adapter) {

            setSetName.setText("SET"+String.valueOf(position+1));

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selected_set_ind=position;
                    Intent intent=new Intent(itemView.getContext(),QuestionActivity.class);
                    itemView.getContext().startActivity(intent);
                }
            });
            setDeleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog alertDialog=new AlertDialog.Builder(itemView.getContext())
                            .setTitle("Delete this Set")
                            .setMessage("Do you want to delete this set")
                            .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                   deleteSet(position,setId,itemView.getContext(),adapter);
                                }
                            }).setNegativeButton("CANCEL",null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();

                    alertDialog.getButton(alertDialog.BUTTON_POSITIVE).setBackgroundColor(Color.RED);
                    alertDialog.getButton(alertDialog.BUTTON_NEGATIVE).setBackgroundColor(Color.RED);

                    LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.
                            WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
                    params.setMargins(0,0,50,0);
                    alertDialog.getButton(alertDialog.BUTTON_NEGATIVE).setLayoutParams(params);
                }
            });

        }
        private void deleteSet(final int position, String setId, final Context context, final SetsAdapter adapter){
            loadingDialog.show();
            final FirebaseFirestore firestore=FirebaseFirestore.getInstance();
            firestore.collection("QUIZ").document(catList.get(selectedCatInd).getId())
                    .collection(setId)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            WriteBatch batch=firestore.batch();
                            for (QueryDocumentSnapshot doc:queryDocumentSnapshots){
                                batch.delete(doc.getReference());
                            }
                            batch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Map<String,Object> catDoc=new ArrayMap<>();
                                    int index=1;
                                    for (int i=0;i<setIDs.size();i++){
                                        if (i!=position){
                                            catDoc.put("SET"+String.valueOf(index)+"_ID",setIDs.get(position));
                                            index++;
                                        }
                                    }
                                    catDoc.put("SETS",index-1);
                                    firestore.collection("QUIZ").document(catList.get(selectedCatInd).getId())
                                            .update(catDoc)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Toast.makeText(context,"Deleted Succesfully",Toast.LENGTH_SHORT).show();
                                                    SetsActivity.setIDs.remove(position);
                                                    catList.get(selectedCatInd).setNoOfSets(String.valueOf(SetsActivity.setIDs.size()));
                                                    adapter.notifyDataSetChanged();
                                                    loadingDialog.dismiss();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(context,e.getMessage(),Toast.LENGTH_SHORT).show();
                                            loadingDialog.dismiss();
                                        }
                                    });

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(context,e.getMessage(),Toast.LENGTH_SHORT).show();
                                    loadingDialog.dismiss();
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(context,e.getMessage(),Toast.LENGTH_SHORT).show();
                    loadingDialog.dismiss();
                }
            });
        }
    }
}
