package com.example.funun.Model;

import java.util.HashMap;

/**This class Product represent the Product Model in firebase database. */

public class Product {

    String productTitle;
    String productUserUpdate;
    int productAmount;
    float productPrice;

    public Product(String productTitle, String productUserUpdate, int productAmount, float productPrice) {
        this.productTitle = productTitle;
        this.productUserUpdate = productUserUpdate;
        this.productAmount = productAmount;
        this.productPrice = productPrice;
    }

    public Product(){

    }

    public String getProductTitle() {
        return productTitle;
    }

    public void setProductTitle(String productTitle) {
        this.productTitle = productTitle;
    }

    public int getProductAmount() {
        return productAmount;
    }

    public void setProductAmount(int productAmount) {
        this.productAmount = productAmount;
    }

    public float getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(float productPrice) {
        this.productPrice = productPrice;
    }

    public String getProductUserUpdate() {
        return productUserUpdate;
    }

    public void setProductUserUpdate(String productUserUpdate) {
        this.productUserUpdate = productUserUpdate;
    }

    public HashMap<String, Object> toMap()
    {
        HashMap<String, Object> result = new HashMap<>();

        result.put("productTitle", this.productTitle);
        result.put("productUserUpdate", this.productUserUpdate);
        result.put("productAmount", this.productAmount);
        result.put("productPrice", this.productPrice);
        return result;
    }

}
