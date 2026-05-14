package com.xs.mycontacts;

interface IContactCallback {
    void onSuccess();
    void onNotFound();
    void inError(String message);
}