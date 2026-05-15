package com.xs.mycontacts.presentation

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.xs.mycontacts.domain.model.Contact
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    viewModel: ContactsViewModel,
    onDeleteDuplicatesClick: () -> Unit
) {
    val context = LocalContext.current
    val contacts by viewModel.contacts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val groupedContacts = remember(contacts) {
        contacts.sortedBy { it.name }.groupBy { it.name.firstOrNull()?.uppercaseChar() ?: '#' }
    }
    val alphabet = remember(groupedContacts) {
        groupedContacts.keys.toList().sorted()
    }

    val duplicatePhoneNumbers = remember(contacts) {
        contacts.groupBy { it.phoneNumber.replace(Regex("[^0-9+]"), "") }
            .filter { it.value.size > 1 }
            .keys
    }
    var hasPermissions by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasPermissions = permissions[Manifest.permission.READ_CONTACTS] == true &&
                permissions[Manifest.permission.WRITE_CONTACTS] == true
        if (hasPermissions) viewModel.loadContacts()
    }

    LaunchedEffect(Unit) {
        if (!hasPermissions) {
            permissionLauncher.launch(arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS))
        } else {
            viewModel.loadContacts()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Контакты", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            if (hasPermissions && contacts.isNotEmpty() && !isLoading) {
                ExtendedFloatingActionButton(
                    onClick = onDeleteDuplicatesClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    icon = { Icon(Icons.Default.Delete, contentDescription = null) },
                    text = { Text("Удалить дубликаты") }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            if (hasPermissions) {
                Row(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        groupedContacts.forEach { (initial, contactsInGroup) ->
                            item(key = initial) {
                                Text(
                                    text = initial.toString(),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
                                )
                            }
                            items(contactsInGroup, key = { it.id }) { contact ->
                                val isDuplicate = contact.phoneNumber.replace(Regex("[^0-9+]"), "") in duplicatePhoneNumbers
                                ContactItem(contact, isDuplicate)
                            }
                        }
                    }

                    AlphabetScrollbar(
                        alphabet = alphabet,
                        onLetterClick = { letter ->
                            val keysList = groupedContacts.keys.toList()
                            val index = keysList.indexOf(letter)
                            if (index != -1) {
                                var scrollIndex = 0
                                for (i in 0 until index) {
                                    val key = keysList[i]
                                    scrollIndex += 1
                                    scrollIndex += groupedContacts[key]?.size ?: 0
                                }
                                scope.launch {
                                    listState.animateScrollToItem(scrollIndex)
                                }
                            }
                        }
                    )
                }
            }
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
fun AlphabetScrollbar(
    alphabet: List<Char>,
    onLetterClick: (Char) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(32.dp)
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        alphabet.forEach { letter ->
            Text(
                text = letter.toString(),
                modifier = Modifier
                    .padding(vertical = 2.dp)
                    .clickable { onLetterClick(letter) },
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun ContactItem(contact: Contact, isDuplicate: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDuplicate) Color(0xFFFFEBEE) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isDuplicate) Color(0xFFEF5350) else MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = contact.name.firstOrNull()?.uppercase() ?: "?",
                    color = if (isDuplicate) Color.White else MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = if (isDuplicate) Color(0xFFC62828) else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = contact.phoneNumber,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isDuplicate) Color(0xFFE57373) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}