package com.example.suiverliverson

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.suiverliverson.ui.theme.AppSurface
import com.example.suiverliverson.ui.theme.DarkGray
import com.example.suiverliverson.ui.theme.Green
import com.example.suiverliverson.ui.theme.LightGray
import com.example.suiverliverson.ui.theme.Red
import com.example.suiverliverson.ui.theme.White

@Composable
fun SettingsScreen(store: AccountStore, onBack: () -> Unit) {
    val s = LocalStrings.current
    val context = LocalContext.current
    val account = store.current()
    val currentLang = LocalLang.current
    val setLang = LocalSetLang.current

    var editFirstName by remember { mutableStateOf(account?.firstName ?: "") }
    var editLastName by remember { mutableStateOf(account?.lastName ?: "") }
    var editPhone by remember { mutableStateOf(account?.phone?.ifBlank { account.login } ?: "") }
    var editVehicle by remember { mutableStateOf(account?.vehicle ?: "سكوتر / Scooter 🛵") }
    var showVehicleDropdown by remember { mutableStateOf(false) }

    var editImageUri by remember { mutableStateOf<Uri?>(account?.imageUri?.let { Uri.parse(it) }) }
    var enableNotifs by remember { mutableStateOf(true) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) editImageUri = uri
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = White)
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = s.settings,
                color = White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = AppSurface)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(DarkGray)
                        .clickable { photoPicker.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (editImageUri != null) {
                        val bitmap = remember(editImageUri) {
                            try {
                                val pathStr = editImageUri.toString()
                                if (pathStr.startsWith("content://")) {
                                    context.contentResolver.openInputStream(editImageUri!!).use {
                                        BitmapFactory.decodeStream(it)
                                    }
                                } else {
                                    BitmapFactory.decodeFile(pathStr)
                                }
                            } catch (e: Exception) { null }
                        }
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    } else {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = Red)
                    }
                }

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = editFirstName,
                    onValueChange = { editFirstName = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(s.firstName, color = LightGray) },
                    colors = appFieldColors(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = editLastName,
                    onValueChange = { editLastName = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(s.lastName, color = LightGray) },
                    colors = appFieldColors(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = editPhone,
                    onValueChange = { editPhone = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(s.phoneLabel, color = LightGray) },
                    colors = appFieldColors(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(10.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = editVehicle,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(s.vehicleLabel, color = LightGray) },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.DirectionsBike, null, tint = LightGray) },
                        trailingIcon = {
                            IconButton(onClick = { showVehicleDropdown = !showVehicleDropdown }) {
                                Icon(Icons.Default.ArrowDropDown, null, tint = LightGray)
                            }
                        },
                        enabled = false,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledContainerColor = AppSurface,
                            disabledBorderColor = DarkGray,
                            disabledTextColor = White,
                            disabledPlaceholderColor = LightGray,
                            disabledLeadingIconColor = LightGray,
                            disabledTrailingIconColor = LightGray
                        )
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showVehicleDropdown = true }
                    )

                    DropdownMenu(
                        expanded = showVehicleDropdown,
                        onDismissRequest = { showVehicleDropdown = false },
                        modifier = Modifier.background(AppSurface)
                    ) {
                        listOf(
                            "سيارة / Car 🚗",
                            "سكوتر / Scooter 🛵",
                            "سكوتر كهربائي / Electric Scooter 🛴"
                        ).forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option, color = White, fontWeight = FontWeight.Bold) },
                                onClick = {
                                    editVehicle = option
                                    showVehicleDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                Text("Langue / اللغة", color = LightGray, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("en" to "EN", "fr" to "FR", "ar" to "عربي").forEach { (code, name) ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (currentLang == code) Red else DarkGray)
                                .clickable { setLang(code) }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(name, color = White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(s.enableNotifications, color = White, fontSize = 14.sp)
                    Switch(
                        checked = enableNotifs,
                        onCheckedChange = { enableNotifs = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = Green)
                    )
                }

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (account != null) {
                            val newImagePath = if (editImageUri != null) {
                                val str = editImageUri.toString()
                                if (str.startsWith("content://")) saveUriToInternalStorage(context, editImageUri!!) else str
                            } else account.imageUri

                            val updated = account.copy(
                                firstName = editFirstName,
                                lastName = editLastName,
                                phone = editPhone,
                                vehicle = editVehicle,
                                imageUri = newImagePath
                            )
                            store.updateAccount(updated)
                        }
                        onBack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Red),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(s.save, color = White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}
