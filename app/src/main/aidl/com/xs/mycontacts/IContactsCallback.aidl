package com.xs.mycontacts;

interface IContactsCallback {
    void onSuccess();
    void onNotFound();
    void onError(in String message);
}