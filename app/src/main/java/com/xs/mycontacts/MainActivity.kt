package com.xs.mycontacts

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.xs.mycontacts.presentation.ContactsScreen
import dagger.hilt.android.AndroidEntryPoint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.widget.Toast
import androidx.activity.viewModels
import com.xs.mycontacts.presentation.ContactsViewModel
import com.xs.mycontacts.service.DuplicateRemovalService
import com.xs.mycontacts.ui.theme.MyContactsTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: ContactsViewModel by viewModels()
    private var contactsManager: IContactsManager? = null

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            contactsManager = IContactsManager.Stub.asInterface(service)
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            contactsManager = null
        }
    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(this, DuplicateRemovalService::class.java)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        unbindService(connection)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyContactsTheme {
                ContactsScreen(
                    viewModel = viewModel,
                    onDeleteDuplicatesClick = { startDeletion() }
                )
            }
        }
    }

    private fun startDeletion() {
        if (contactsManager == null) {
            Toast.makeText(this, "Сервис еще не подключен", Toast.LENGTH_SHORT).show()
            return
        }
        viewModel.setLoading(true)
        contactsManager?.removeDuplicates(object : IContactsCallback.Stub() {

            override fun onSuccess() {
                runOnUiThread {
                    viewModel.setLoading(false)
                    Toast.makeText(this@MainActivity, "Дубликаты удалены успешно", Toast.LENGTH_SHORT).show()
                    viewModel.loadContacts()
                }
            }

            override fun onNotFound() {
                runOnUiThread {
                    viewModel.setLoading(false)
                    Toast.makeText(this@MainActivity, "Повторяющиеся контакты не найдены", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onError(msg: String) {
                runOnUiThread {
                    viewModel.setLoading(false)
                    Toast.makeText(this@MainActivity, "Произошла ошибка: $msg", Toast.LENGTH_LONG).show()
                }
            }
        })
    }
}