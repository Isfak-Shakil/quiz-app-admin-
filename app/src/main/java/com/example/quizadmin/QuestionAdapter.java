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

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.example.quizadmin.CategoryActivity.catList;
import static com.example.quizadmin.CategoryActivity.selectedCatInd;
import static com.example.quizadmin.QuestionActivity.questionList;
import static com.example.quizadmin.SetsActivity.selected_set_ind;
import static com.example.quizadmin.SetsActivity.setIDs;

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.ViewHolder> {
        private List<QuestionModel> ques_List;

    public QuestionAdapter(List<QuestionModel> ques_List) {
        this.ques_List = ques_List;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.cat_item_layout,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setData(position,this);
    }

    @Override
    public int getItemCount() {
        return ques_List.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private ImageButton deleteB;
        private Dialog loadingDialog;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title=itemView.findViewById(R.id.textView);
            deleteB=itemView.findViewById(R.id.deleteBtn);

            loadingDialog=new Dialog(itemView.getContext());
            loadingDialog.setContentView(R.layout.loading_progressbar);
            loadingDialog.setCancelable(false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Objects.requireNonNull(loadingDialog.getWindow()).setBackgroundDrawableResource(R.drawable.progress_background);
            }
            loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        private void setData(final int position, final QuestionAdapter adapter) {
            title.setText("QUESTION "+String.valueOf(position+1));
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(itemView.getContext(),QuestionDetailsActivity.class);
                    intent.putExtra("ACTION","EDIT");
                    intent.putExtra("Q_ID",position);
                    itemView.getContext().startActivity(intent);
                }
            });
            deleteB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog alertDialog=new AlertDialog.Builder(itemView.getContext())
                            .setTitle("Delete this Question")
                            .setMessage("Do you want to delete this Question")
                            .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                   deleteQuestion(position,itemView.getContext(),adapter);
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
        private  void deleteQuestion(final int position, final Context context, final QuestionAdapter adapter){
                loadingDialog.show();
            final FirebaseFirestore firestore =FirebaseFirestore.getInstance();
            firestore.collection("QUIZ").document(catList.get(selectedCatInd).getId())
                    .collection(setIDs.get(selected_set_ind)).document(questionList.get(position).getQuesID())
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                        @Override
                        public void onSuccess(Void aVoid) {
                            Map<String,Object> quesDoc=new ArrayMap<>();
                            int index=0;
                            for (int i=0;i<questionList.size();i++){
                                if (i !=position){
                                    quesDoc.put("Q" +String.valueOf(index)+"_ID",questionList.get(i).getQuesID());
                                    index++;
                                }
                            }
                            quesDoc.put("COUNT",String.valueOf(index-1));
                            firestore.collection("QUIZ").document(catList.get(selectedCatInd).getId())
                                    .collection(setIDs.get(selected_set_ind)).document("QUESTIONS_LIST")
                                    .set(quesDoc)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(context,"Deleted Succesfully",Toast.LENGTH_SHORT).show();
                                           questionList.remove(position);
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
    }
}
