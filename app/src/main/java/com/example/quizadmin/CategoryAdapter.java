package com.example.quizadmin;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.collection.ArrayMap;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private List<CategoryModel> cat_List;

    public CategoryAdapter(List<CategoryModel> catList) {
        this.cat_List = catList;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.cat_item_layout,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String title=cat_List.get(position).getName();
        holder.setData(title,position,this);
    }

    @Override
    public int getItemCount() {
        return cat_List.size();
    }

    public  class  ViewHolder extends RecyclerView.ViewHolder{
        private TextView catName;
        private ImageButton deleteBtn;
        private Dialog loadingDialog;
        private Dialog editDialog;
        private EditText editCatName;
        private Button editCatButton;
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            catName=itemView.findViewById(R.id.textView);
            deleteBtn=itemView.findViewById(R.id.deleteBtn);

            loadingDialog=new Dialog(itemView.getContext());
            loadingDialog.setContentView(R.layout.loading_progressbar);
            loadingDialog.setCancelable(false);
            Objects.requireNonNull(loadingDialog.getWindow()).setBackgroundDrawableResource(R.drawable.progress_background);
            loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);


           editDialog=new Dialog(itemView.getContext());
           editDialog.setContentView(R.layout.edit_category_dialog);
           editDialog.setCancelable(true);
           editDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

            editCatName=editDialog.findViewById(R.id.editCatName);
            editCatButton=editDialog. findViewById(R.id.editCatButtonId);
        }

      private void setData(String title, final int position, final CategoryAdapter adapter) {
        catName.setText(title);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CategoryActivity.selectedCatInd=position;
                Intent intent=new Intent(itemView.getContext(),SetsActivity.class);
                itemView.getContext().startActivity(intent);
            }
        });
        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                editCatName.setText(cat_List.get(position).getName());
                editDialog.show();

                return false;
            }
        });
          editCatButton.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                  if (editCatName.getText().toString().isEmpty()){
                      editCatName.setError("Enter Category Name");
                      return;
                  }
                  updateCategorName(editCatName.getText().toString(),position,itemView.getContext(),adapter);
              }
          });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
       AlertDialog alertDialog=new AlertDialog.Builder(itemView.getContext())
               .setTitle("Delete this category")
               .setMessage("Do you want to delete this category")
               .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                    deleteCategory(position,itemView.getContext(),adapter);
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

        private void deleteCategory(final int id, final Context context, final CategoryAdapter adapter){
            loadingDialog.show();
            FirebaseFirestore firestore=FirebaseFirestore.getInstance();
            Map<String,Object> catDoc=new ArrayMap<>();
            int index=1;
            for (int i=0;i<cat_List.size();i++){
                if (i !=id){
                    catDoc.put("CAT"+String.valueOf(index)+"_ID",cat_List.get(i).getId());
                    catDoc.put("CAT"+String.valueOf(index)+"_NAME",cat_List.get(i).getName());
                    index++;
                }
            }
            catDoc.put("COUNT",index-1);
            firestore.collection("QUIZ").document("Categories")
                    .set(catDoc)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(context,"Deleted Successfully",Toast.LENGTH_SHORT).show();
                            CategoryActivity.catList.remove(id);
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
        private  void   updateCategorName(final String catNewName, final int positon, final Context context, final CategoryAdapter adapter){
            editDialog.dismiss();
            loadingDialog.show();
            final Map<String,Object> catData=new ArrayMap<>();
            catData.put("NAME",catNewName);
            final FirebaseFirestore firestore=FirebaseFirestore.getInstance();
            firestore.collection("QUIZ").document(cat_List.get(positon).getId())
                    .update(catData)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Map<String,Object> catDoc=new ArrayMap<>();
                            catDoc.put("CAT"+String.valueOf(positon+1)+"_NAME",catNewName);
                            firestore.collection("QUIZ").document("Categories")
                                    .update(catDoc)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(context,"Category name changed Successfully",Toast.LENGTH_SHORT).show();
                                            CategoryActivity.catList.get(positon).setName(catNewName);
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
