package com.example.funun.Adapter;
import android.app.AlertDialog;
import android.app.Notification;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.funun.Model.Helper;
import com.example.funun.Model.Product;
import com.example.funun.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**This Class Adapter - ProductAdapterRV - provide a binding between the data,holder and the recycler view.
 *  set the views that are displayed within a RecyclerView.
 *  used also as a controller for the elements in background. */
public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductHolder> {

    private SharedPreferences sharedPref2;
    List<Product> productList;
    Context context;
    Fragment one;
    ProductHolder ret;

    //shared pref elements
    private SharedPreferences sharedPref;
    boolean eventHost;
    String myUsername;
    String eventName;

    //firebase elements
    DatabaseReference productRef;
    DatabaseReference eventRef;

    //view elements
    TextView tv3;
    ImageView tv2;
    TextView sum_products;

    /**This Constructor ProductAdapter - updates and initialize the params from the ShoppingListFragment.
     * initialize view element from frag
     * initialize pointer to shared pref files.
     * @param tv2 - part of view that is visible/gone accoridng to the size of the list.
     * @param tv3 - part of view that is visible/gone accoridng to the size of the list.
     * @param context - the relevant context of app.
     * @param productList the event list from all events.
     * @param frag - the fragment AllEventsFragment.
     * @param sum_products - part of the view - text view that holds the correct total price of all products. */
    public ProductAdapter(Context context, List<Product> productList, Fragment frag,ImageView tv2,TextView tv3,TextView sum_products) {
        this.productList = productList;
        this.context = context;
        one = frag;
        this.tv2=tv2;
        this.tv3=tv3;
        this.sum_products=sum_products;
        sharedPref = one.getActivity().getSharedPreferences("Event", Context.MODE_PRIVATE);
        sharedPref2 = one.getActivity().getSharedPreferences("Username", Context.MODE_PRIVATE);
        productRef = FirebaseDatabase.getInstance().getReference("products");
        eventName = sharedPref.getString("eventName", "");
        eventRef = FirebaseDatabase.getInstance().getReference().child("events");
        showAllProducts();
        if(productList.size()==0) {
            tv2.setVisibility(View.VISIBLE);
            tv3.setVisibility(View.VISIBLE);
            sum_products.setVisibility(View.GONE);

        }
        else{
            tv2.setVisibility(View.GONE);
            tv3.setVisibility(View.GONE);
            sum_products.setVisibility(View.VISIBLE);
        }
    }

    /**This Method - onCreateViewHolder - Called when RecyclerView needs a new RecyclerView.ViewHolder from the given type to represent an item.*/
    @Override
    public ProductHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Get LayoutInflater object.
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        // Inflate the RecyclerView item layout xml.
        View productItemView = layoutInflater.inflate(R.layout.view_item_product_card, parent, false);
        // Create and return our custom product Recycler View Item Holder object.
        ret = new ProductAdapter.ProductHolder(productItemView);
        return ret;
    }

    /**This Method -onBindViewHolder - Called by RecyclerView to display the data at the specified position.
     *this method update the content of the RecyclerView.ViewHolder.itemView to reflect the item at the given position.
     *this method initialize the shared pref files.
     * checks if the current user is a host in the event.
     * sets dialog(if needed, and listeners to the product items.
     * @param position - the position of the product item in the list (screen)
     * @param holder  - the holder that holds the view elements. */
    @Override
    public void onBindViewHolder(ProductHolder holder, final int position) {
        // Get product item dto in list.
        sharedPref = one.getActivity().getSharedPreferences("Event", Context.MODE_PRIVATE);
        sharedPref2 = one.getActivity().getSharedPreferences("Username", Context.MODE_PRIVATE);
        if(sharedPref.contains("eventHost")) eventHost=true;
        else eventHost=false;
        myUsername=sharedPref2.getString("MyUsername","");
        final Product productItem = productList.get(position);
        // Set product item details
        holder.itemView.setTag(productItem);
        holder.productTitle.setText(productItem.getProductTitle());
        holder.productUsername.setText(productItem.getProductUserUpdate());
        holder.productAmount.setText(String.valueOf(productItem.getProductAmount()));
        holder.productPrice.setText(String.valueOf(productItem.getProductPrice()));
        holder.iv_product_edit.setImageResource(R.drawable.edit_icon);
        holder.iv_product_delete.setImageResource(R.drawable.remove_icon);

        //set listeners

        holder.productTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buildProductBuilder(v,position);
            }
        });
        holder.iv_product_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (eventHost) {
                    productToRemoveFromFirebase(productItem.getProductTitle());
                    productList.remove(productList.get(position));
                    if(productList.size()==0) {
                        tv2.setVisibility(View.VISIBLE);
                        tv3.setVisibility(View.VISIBLE);
                        sum_products.setVisibility(View.GONE);

                    }
                    else{
                        tv2.setVisibility(View.GONE);
                        tv3.setVisibility(View.GONE);
                        sum_products.setVisibility(View.VISIBLE);
                    }

                    notifyDataSetChanged();
                } else {
                    Helper.makeABuilder("Guests Cant Remove Products",one.getActivity());
                }
            }
        });

        holder.iv_product_edit.setOnClickListener(new View.OnClickListener() {
            //if product item was pressed, open dialog and show details.
            @Override
            public void onClick(View v) {
                // Get product title text.
                buildProductBuilder(v, position);
            }
        });

    }

    /**This Method - buildProductBuilder - creates a dialog in order to edit a specific product.
     * @param v - the view from context
     * @param position the position of the item on screen. (and in list).*/
    public void buildProductBuilder(View v, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customLayout2 = inflater.inflate(R.layout.alert_box_edit_product, null);
        builder.setView(customLayout2);
        final AlertDialog dialog = builder.create();
        //init views from custom view.
        final TextView tv_edit_product_title = customLayout2.findViewById(R.id.tv_edit_product_title);
        final EditText et_edit_product_price = customLayout2.findViewById(R.id.et_edit_product_price);
        final EditText np_edit_product_amount = customLayout2.findViewById(R.id.np_edit_product_amount);
        Button btn_product_save = customLayout2.findViewById(R.id.btn_edit_product_save);
        Button btn_product_cancel = customLayout2.findViewById(R.id.btn_edit_product_cancel);

        //set current info of product.

        tv_edit_product_title.setText(productList.get(position).getProductTitle());
        et_edit_product_price.setText(String.valueOf(productList.get(position).getProductPrice()));
        np_edit_product_amount.setText(String.valueOf(productList.get(position).getProductAmount()));

        //set listeners

        btn_product_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (TextUtils.isEmpty(et_edit_product_price.getText().toString()) ||
                        TextUtils.isEmpty(np_edit_product_amount.getText().toString())){
                    Helper.makeABuilder("שים לב"+"\n"+ "חלק מהשדות ריקים",one.getActivity());
                }
                else if(!TextUtils.isEmpty(et_edit_product_price.getText().toString()) &&
                        !TextUtils.isEmpty(np_edit_product_amount.getText().toString())){
                    int newA= Integer.parseInt(np_edit_product_amount.getText().toString());
                    float newP = Float.parseFloat(et_edit_product_price.getText().toString());
                    if (newA<0 ||newP<0 ||newP>20000||newA>100) {
                        Helper.makeABuilder("שים לב. \n" +
                                "מחיר הוא מספר בין 0 ל- 20000" +"\n"+
                                "הכמות מוגבלת ל - 100.",one.getActivity());
                    }
                    else {
                        Product productOld = productList.get(position);
                        float productPrice2 = Float.parseFloat(et_edit_product_price.getText().toString());
                        int productAmount2 = Integer.parseInt(np_edit_product_amount.getText().toString());
                        String newUsername = myUsername;
                        Product new_product = new Product(tv_edit_product_title.getText().toString(), newUsername, productAmount2, productPrice2);
                        checkChangesFirebase(position, dialog, new_product, productOld);
                    }
                }
            }
        });
        btn_product_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    /**This Method - makeAToast - Makes a Toast on screen.
     * @param textToPut - the string to show on screen */
    private void makeAToast(String textToPut) {
        Toast.makeText(context, textToPut, Toast.LENGTH_SHORT).show();

    }

    /**This Method - productToRemoveFromFirebase - Removes the product from firebase database
     * remove from products database
     * update count of products in event
     * updates the sum of products
     * @param productTitle2 - the name of the product (key)*/
    private void productToRemoveFromFirebase(final String productTitle2) {

        productRef.child(eventName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int notExist=0;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    final String name =ds.getKey();
                    Product product = ds.getValue(Product.class);
                    //if the product if the one that was requested by the user
                    //calculate the sum and update+remove
                    if (name.equals(productTitle2)) {
                        notExist=1;
                        final float sumtemp1 =product.getProductAmount()*product.getProductPrice();
                        eventRef.child(eventName).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot2) {
                                float original =Float.parseFloat(snapshot2.child("sum").getValue().toString());
                                float sumnew = original-sumtemp1;
                                eventRef.child(eventName).child("sum").setValue(sumnew);
                                sum_products.setText("Event cost: "+sumnew+" \u20AA");
                                DatabaseReference delete = productRef.child(eventName).child(name);
                                delete.removeValue();
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            }
                        });
                    }
                }
                //update amount less 1 in event products.
                if(notExist==1){
                    eventRef.child(eventName).child("products").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot3) {
                            int amountProducts = Integer.parseInt(snapshot3.child("products").getValue().toString().trim());
                            amountProducts = amountProducts - 1;
                            eventRef.child(eventName).child("products").setValue(amountProducts);
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });


    }

    /**This Method - productToUpdateToFirebase - Updates the product details in firebase database
     * edit the event and products database
     * updates the sum of products
     * @param new_product - product that holds the new details of the product.
     * @param old  product that holds the old details of the product.*/
    private void productToUpdateToFirebase(Product new_product,Product old) {

        final float oldsumproduct = old.getProductAmount()*old.getProductPrice();
        final float newsumproduct = new_product.getProductAmount()*new_product.getProductPrice();

        Map<String, Object> hash = new HashMap<>();
        hash.put("productTitle", new_product.getProductTitle());
        hash.put("productUserUpdate", myUsername);
        hash.put("productAmount", new_product.getProductAmount());
        hash.put("productPrice", new_product.getProductPrice());
        productRef.child(eventName).child(new_product.getProductTitle()).updateChildren(hash)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        makeAToast("עודכן בהצלחה  ");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        makeAToast("הייתה בעיה עם העדכון.. אנא נסה שנית ");
                    }
                });
        eventRef.child(eventName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                float sumdb = Float.parseFloat(snapshot.child("sum").getValue().toString());
                sumdb = sumdb-oldsumproduct;
                sumdb=sumdb+newsumproduct;
                eventRef.child(eventName).child("sum").setValue(sumdb);
                sum_products.setText("Event cost : "+sumdb+" \u20AA");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        int ret = 0;
        if (productList != null) {
            ret = productList.size();
        }
        return ret;
    }

    public class ProductHolder extends RecyclerView.ViewHolder {

        private TextView productTitle;
        private TextView productUsername;
        private TextView productPrice;
        private TextView productAmount;
        private ImageView iv_product_delete;
        private ImageView iv_product_edit;
        private TextView tv_product_text1;
        private TextView tv_product_text2;
        private TextView tv_product_text4;

        public ProductHolder(View itemView) {
            super(itemView);
            productTitle = itemView.findViewById(R.id.tv_product_item_title);
            productPrice = itemView.findViewById(R.id.tv_product_price);
            productUsername = itemView.findViewById(R.id.tv_product_username);
            productAmount = itemView.findViewById(R.id.tv_product_amount);
            iv_product_delete = itemView.findViewById(R.id.iv_product_remove);
            iv_product_edit = itemView.findViewById(R.id.iv_product_edit);
            tv_product_text2 = itemView.findViewById(R.id.tv_product_text2);
            tv_product_text1 = itemView.findViewById(R.id.tv_product_text1);
            tv_product_text4 = itemView.findViewById(R.id.tv_product_text4);
        }
    }

    /**This Method - showAllProducts - Creates a local list of all products from the firebase database*/
    public void showAllProducts() {
        eventRef.child(eventName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot snapshot) {
                final int i = Integer.valueOf(String.valueOf(snapshot.child("products").getValue().toString()));
                if (i > 0) {
                    productRef.child(eventName).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull final DataSnapshot snapshot2) {
                            for (DataSnapshot ds : snapshot2.getChildren()) {
                                productList.add(ds.getValue(Product.class));
                                if(productList.size()==0) {
                                    tv2.setVisibility(View.VISIBLE);
                                    tv3.setVisibility(View.VISIBLE);
                                    sum_products.setVisibility(View.GONE);
                                }
                                else{
                                    tv2.setVisibility(View.GONE);
                                    tv3.setVisibility(View.GONE);
                                    sum_products.setVisibility(View.VISIBLE);
                                }
                            }
                            sum_products.setText("Event cost: "+snapshot.child("sum").getValue()+" \u20AA");
                            notifyDataSetChanged();
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            makeAToast("Cant Add To List");
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    /**This Method checkChangesFirebase - checks if a product that we want to change, has not been deleted or changed before the try
     * @param position the position of the item on screen (and list).
     * @param productOld the product that we want to change
     * @param new_product the new changes in the product that we want to put in the database.
     * @param dialog the open dialog that we want to close after action been taken. */
    public void checkChangesFirebase(final int position, final AlertDialog dialog, final Product new_product, final Product productOld){

        productRef.child(eventName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int notExist=1;
                for(DataSnapshot snap:snapshot.getChildren()) {
                    if(productOld.getProductTitle().equals(snap.getKey())) {
                        float price = Float.parseFloat(snap.child("productPrice").getValue().toString());
                        int amount = Integer.parseInt(snap.child("productAmount").getValue().toString());
                        if(price==productOld.getProductPrice()&&amount==productOld.getProductAmount()){
                            notExist = 0;
                        }
                    }
                }
                if(notExist==0){
                    //change the details of the product in list.
                    productList.set(position, new_product);
                    productToUpdateToFirebase(new_product, productOld);
                    notifyDataSetChanged();
                    dialog.dismiss();
                }
                else{
                    Helper.makeABuilder("Please refresh the event and try again. \nThe product was changed by someone else.",one.getActivity());
                    dialog.dismiss();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}