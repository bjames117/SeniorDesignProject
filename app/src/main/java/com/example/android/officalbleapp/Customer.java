package com.example.android.officalbleapp;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by Kirkland on 7/3/17.
 */

public class Customer implements Serializable{

    private String customerName;
    private String accountBalance;
    private String language;


    public Customer(String customerName,String accountBalance,String language) {
        this.customerName = customerName;
        this.accountBalance = accountBalance;
        this.language = language ;

    }



    public void setAccountBalance(String accountBalance) {
        this.accountBalance = accountBalance;
    }

    public String getLanguage() {
        return language;
    }

    public String getAccountBalance() {
        return accountBalance;
    }

    public String getCustomerName() {
        return customerName;
    }


}
