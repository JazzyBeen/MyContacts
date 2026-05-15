package com.xs.mycontacts;

import com.xs.mycontacts.IContactsCallback;

interface IContactsManager {
    void removeDuplicates(IContactsCallback callback);
}