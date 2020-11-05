package com.example.funun.Fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.funun.Adapter.ProductAdapter;
import com.example.funun.Model.Helper;
import com.example.funun.Model.Product;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.example.funun.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


/**This Fragment - ShoppingListFragment - initialize the view of shopping/products list.
 * 1. initialize recyclerview list of products relevant to the specific event.
 * 2. notify the adapter when a product is added */
public class ShoppingList extends Fragment {

    public static List<Product> productItemList;
    FloatingActionButton btn_add_new_product;
    private TextView tv3;
    private ImageView tv2;
    TextView tv_products_list_text;
    TextView sum_products;
    ProductAdapter productDataAdapter;
    RecyclerView productRecyclerView;
    GridLayoutManager gridLayoutManager;

    //shared pref items.
    String myUsername;
    boolean no_info;
    SharedPreferences sharedPref;
    SharedPreferences sharedPref2;
    boolean eventHost;
    String eventName;

    //firebase references
    DatabaseReference productRef;
    DatabaseReference eventRef;

    public ShoppingList() {
        // Required empty public constructor
    }

    /**This Method - onCreate - Called to do initial creation of a fragment.
     * initialize shared pref and firebase objects. */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref=getActivity().getSharedPreferences("Event",Context.MODE_PRIVATE);
        sharedPref2=getActivity().getSharedPreferences("Username",Context.MODE_PRIVATE);
        eventName=sharedPref.getString("eventName","");
        myUsername=sharedPref2.getString("MyUsername","");
        if(sharedPref.contains("eventHost")) eventHost=true;
        else eventHost=false;
        productRef=FirebaseDatabase.getInstance().getReference("products");
        eventRef = FirebaseDatabase.getInstance().getReference("events");

    }

    /**This Method - onCreateView - creates and returns the view associated with the fragment.
     this method makes sure that if the shared pref file is empty,  the view will not be created.
     * @return Return the View for the fragment's UI, or null.
     * @param inflater - The LayoutInflater object that can be used to inflate views in the fragment
     * @param container - the parent view that the fragment's UI should be attached to.
     *@param savedInstanceState - If non-null, this fragment is being re-constructed from a previous saved state as given here.*/
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = null;
        if (sharedPref.getAll().size() == 0) {
            no_info = true;
        }
        else {
            no_info = false;
            //init product lists.
            view = inflater.inflate(R.layout.fragment_shopping_list, container, false);
        }
        return view;
    }

    /**This Method - onViewCreated - initialize the view elements and attach listeners with values.
     * Called immediately after onCreateView
     * check if your a host or a guest in the specific event
     * attach adapter to this fragment.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (!no_info) {
            productItemList =new ArrayList<>();
            tv2=view.findViewById(R.id.tvemptyprod2);
            tv3=view.findViewById(R.id.tvemptyprod3);
            tv2.setVisibility(View.GONE);
            tv3.setVisibility(View.GONE);
            sum_products = view.findViewById(R.id.sum_products);
            tv_products_list_text= view.findViewById(R.id.tv_products_list_text);
            sharedPref=getActivity().getSharedPreferences("Event",Context.MODE_PRIVATE);
            sharedPref2=getActivity().getSharedPreferences("Username",Context.MODE_PRIVATE);
            eventName=sharedPref.getString("eventName","");
            myUsername=sharedPref2.getString("MyUsername","");
            if(sharedPref.contains("eventHost")) eventHost=true;
            else eventHost=false;
            // Create the recyclerview.
            productRecyclerView = (RecyclerView) view.findViewById(R.id.card_view_product_list);
            // Create the grid layout manager with 1 columns.
            gridLayoutManager = new GridLayoutManager(getParentFragment().getContext(), 1);
            // Set layout manager.
            productRecyclerView.setLayoutManager(gridLayoutManager);
            // Create event recycler view data adapter with event item list.
            productDataAdapter = new ProductAdapter(this.getActivity(), productItemList, this,tv2,tv3,sum_products);
            // Set data adapter.
            productRecyclerView.setAdapter(productDataAdapter);
            btn_add_new_product = (FloatingActionButton) view.findViewById(R.id.btn_float_add_new_product_item);
            btn_add_new_product.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addProduct();                }
            });
        }
    }

    public void notifyDataChanged() {
        productDataAdapter.notifyDataSetChanged();
    }

    /**This Method - setProductsInFirebase - add the product to the firebase database and updates the sum.
     * @param product - the product to add. */
    public void setProductsInFirebase(final Product product){
        final HashMap<String,Object> hash = product.toMap();
        final float newPrice1 =product.getProductPrice();
        final int newAmount1 =product.getProductAmount();

        eventRef.child(eventName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                float sumtemp1 = newAmount1*newPrice1;
                productRef.child(eventName).child(product.getProductTitle()).setValue(hash);
                float sumtemp2 = Float.parseFloat(snapshot.child("sum").getValue().toString());
                float sum3 = sumtemp1+sumtemp2;
                eventRef.child(eventName).child("sum").setValue(sum3);
                sum_products.setText("Event cost: "+sum3+" \u20AA");
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    /**This Method addProduct - Creates a new dialog for adding a new product.
     * defines a new custom layout for the alert dialog.
     * set cancel and save buttons
     * attach listeners to the save and cancel buttons */
    public void addProduct() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // set the custom layout
        final View customLayout = getLayoutInflater().inflate(R.layout.alert_box_add_product, null);
        builder.setView(customLayout);
        final EditText et_product_title = (EditText) customLayout.findViewById(R.id.et_alert_product_title);
        final EditText et_product_units = customLayout.findViewById(R.id.et_product_units);
        final EditText et_product_price = (EditText) customLayout.findViewById(R.id.et_alert_product_price);
        final Button btn_add_product_cancel = customLayout.findViewById(R.id.btn_add_product_cancel);
        final Button btn_add_product_save = customLayout.findViewById(R.id.btn_add_product_save);
        final AlertDialog dialog = builder.create();
        btn_add_product_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btn_add_product_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkProductInput(et_product_title, et_product_price, et_product_units, dialog);
            }
        });
        dialog.show();
    }

    /**This Method - checkProductInput - checks the input from the user according to the rules of the app
     * makes an error message if mistake was found
     * else build a new product in database and update the UI.
     * @param dialog - the add product dialog that we want to remove from the view after adding the product.
     * @param et_product_price - the product price
     * @param et_product_units - how many units of the specific product.
     * @param et_product_title  the name of the product. */
    public void checkProductInput(EditText et_product_title, EditText et_product_price, EditText et_product_units, AlertDialog dialog) {

        if (TextUtils.isEmpty(et_product_title.getText().toString()) ||
                TextUtils.isEmpty(et_product_price.getText().toString()) || TextUtils.isEmpty(et_product_units.getText().toString())) {
            Helper.makeABuilder("שגיאה!"+"\n"+"חלק מהשדות ריקים.",getActivity());
        } else {
            //take the input and translate it to numbers and title.
            String newTitle = et_product_title.getText().toString();
            float newPrice = Float.parseFloat(et_product_price.getText().toString());
            int newAmount = Integer.parseInt(et_product_units.getText().toString());
            //check if some of the fields are empty
            if ((newAmount <= 0) || (newAmount >= 100) || (newPrice < 0.0) || (newPrice > 20000.0) || TextUtils.isEmpty(newTitle)) {
                Helper.makeABuilder("שגיאה! \n" +
                        "מחיר הוא מספר בין 0 ל- 20000" +"\n"+
                        "הכמות מוגבלת בין 1 ל -100.",getActivity());
            }
            else if(!TextUtils.isEmpty(newTitle)) {
                //check if the title dosnt have special chars
                boolean check = Helper.checkLetters(newTitle,this.getActivity());
                if (!check) {
                    int flagOfDup = 0;
                    for (Product product : productItemList) {
                        if (newTitle.equals(product.getProductTitle())) {
                            Helper.makeABuilder("שגיאה! המוצר קיים במערכת",getActivity());
                            flagOfDup = 1;
                        }
                    }
                    if (flagOfDup != 1) {
                        Product product = new Product(newTitle, myUsername, newAmount, newPrice);
                        productItemList.add(product);
                        setProductsInFirebase(product);
                        updateProductAmountInEvent();
                        sum_products.setVisibility(View.VISIBLE);
                        tv2.setVisibility(View.GONE);
                        tv3.setVisibility(View.GONE);
                        notifyDataChanged();
                        dialog.dismiss();//move fragment
                    }
                }
            }
        }
    }
/**This Method - updateProductAmountInEvent - Update the amount of product in event firebase database */
    public void updateProductAmountInEvent() {

        final DatabaseReference eventProductValue = eventRef.child(eventName).child("products");
        eventProductValue.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int amountProducts = Integer.valueOf(String.valueOf(snapshot.getValue().toString().trim())) + 1;
                eventProductValue.setValue(amountProducts);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

}