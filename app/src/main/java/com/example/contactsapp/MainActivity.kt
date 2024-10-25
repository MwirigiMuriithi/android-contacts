package com.example.contactsapp

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.widget.Space
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.R
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import coil.compose.rememberAsyncImagePainter
import com.example.contactsapp.ui.theme.ContactsAppTheme
import com.example.contactsapp.ui.theme.GreenJC
import java.io.File
import java.io.FileOutputStream
import androidx.compose.foundation.lazy.items

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = Room.databaseBuilder(applicationContext,
            ContactDatabase::class.java, "contact_database").build()

        val repository = ContactRepository(database.contactDao())
        val viewModel: ContactViewModel by viewModels { ContactViewModelFactory(repository) }

        setContent {
           val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "contactList") {
                composable("contactList") { ContactListScreen(viewModel, navController)}
                composable("addContact") { AddContactScreen(viewModel, navController )
                }
                
            }
        }
    }
}

@Composable
fun ContactItem(contact: Contact, onClick:() -> Unit) {
    Card (
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 8.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ){
        Row (modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(painter = rememberAsyncImagePainter(contact.image), contentDescription = contact.name,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop)
            Spacer(modifier = Modifier.width(16.dp))
            Text(contact.name)
        }
    }
}
@Composable
fun ContactListScreen(viewModel: ContactViewModel, navController: NavController) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.height(48.dp),
                title = {
                    Box(modifier = Modifier
                        .fillMaxHeight()
                        .wrapContentHeight(Alignment.CenterVertically)) {
                        Text("Contacts", fontSize = 18.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        Toast.makeText(context, "Contacts", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(painter = painterResource(id = R.drawable.contacticon), contentDescription = "Contact Icon")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GreenJC,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(containerColor = GreenJC, onClick = { navController.navigate("addContact")
            }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Contact"  )
            }
        }
    ) { paddingValues ->
        val contacts by viewModel.allContacts.observeAsState(initial = emptyList())
        LazyColumn (modifier = Modifier.padding(paddingValues)
        ) {
            items(contacts){ contact ->
                ContactItem(contact = contact) {
                    navController.navigate("contactDetail/${contact.id}")
                }

            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactScreen(viewModel: ContactViewModel, navController: NavController) {
    val context = LocalContext.current

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var name by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add contact", fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { Toast.makeText(context, "Add Contact", Toast.LENGTH_SHORT).show() }) {
                        Icon(painter = painterResource(id = R.drawable.addcontact), contentDescription = "Add Contact")
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = GreenJC,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            imageUri?.let {
                Image(
                    painter = rememberAsyncImagePainter(it),
                    contentDescription = null,
                    modifier = Modifier
                        .size(128.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(onClick = { launcher.launch("image/*") }, colors = ButtonDefaults.buttonColors(GreenJC)) {
                Text(text = "Choose Image")
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("Phone Number") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )

            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                imageUri?.let {
                    val internalPath = copyUriToInternalStorage(context, it, "$name.jpg")
                    internalPath?.let { path ->
                        viewModel.addContact(path, name, phoneNumber, email)
                        navController.navigate("contactList") {
                            popUpTo(0)
                        }
                    }
                }
            }, colors = ButtonDefaults.buttonColors(GreenJC)) {
                Text(text = "Add Contact")
            }
        }
    }
}

fun copyUriToInternalStorage(context: Context, uri:Uri, fileName: String): String? {
    val file = File(context.filesDir, fileName)
    return try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(file).use{ outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        file.absolutePath
    } catch ( e: Exception) {
        e.printStackTrace()
        null
    }
}


//package com.example.contactsapp
//
//import android.content.Context
//import android.media.Image
//import android.net.Uri
//import android.os.Bundle
//import android.widget.Toast
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.rememberLauncherForActivityResult
//import androidx.activity.compose.setContent
//import androidx.activity.enableEdgeToEdge
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.activity.viewModels
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxHeight
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.layout.wrapContentHeight
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material3.Button
//import androidx.compose.material3.ButtonDefaults
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.material3.TextField
//import androidx.compose.material3.TextFieldColors
//import androidx.compose.material3.TextFieldDefaults
//import androidx.compose.material3.TopAppBar
//import androidx.compose.material3.TopAppBarDefaults
//import androidx.compose.material3.rememberTopAppBarState
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.modifier.modifierLocalMapOf
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.navigation.NavController
//import androidx.room.Room
//import coil.compose.rememberAsyncImagePainter
//import com.example.contactsapp.ui.theme.ContactsAppTheme
//import com.example.contactsapp.ui.theme.GreenJC
//import com.example.thecontactsapp.R
//import java.io.File
//import java.io.FileOutputStream
//
//class MainActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        val database = Room.databaseBuilder(applicationContext,
//            ContactDatabase::class.java, "contact_database").build()
//
//        val repository = ContactRepository(database.contactDao())
//        val viewModel: ContactViewModel by viewModels { ContactViewModelFactory(repository)}
//        setContent {
//
//        }
//    }
//}
//
//@Composable
//fun AddContactScreen(viewModel: ContactViewModel, navController: NavController){
//    val context = LocalContext.current.applicationContext
//
//    var imageUri by remember { mutableStateOf<Uri?>(null) }
//    var name by remember { mutableStateOf("") }
//    var phoneNumber by remember { mutableStateOf("") }
//    var email by remember { mutableStateOf("") }
//
//    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) {
//        uri: Uri? ->
//        imageUri = uri
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                modifier = Modifier.height(48.dp),
//                title = {
//                    Box(
//                        modifier = Modifier
//                            .fillMaxHeight()
//                            .wrapContentHeight(Alignment.CenterVertically)
//                    ) {
//                        Text(text = "Add contact", fontSize = 18.sp)
//                    }
//                },
//                navigationIcon = {
//                    IconButton(onClick = { Toast.makeText(context, "Add Contact", Toast.LENGTH_SHORT).show() }) {
//                        Icon(painter = painterResource(id = R.drawable.addcontact), contentDescription = "Add Contact")
//                    }
//                },
//                colors = TopAppBarDefaults.smallTopAppBarColors(
//                    containerColor = GreenJC,
//                    titleContentColor = Color.White,
//                    navigationIconContentColor = Color.White
//                )
//            )
//        }
//    ) {paddingValues ->
//
//        Column (
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(paddingValues)
//                .padding(16.dp),
//            verticalArrangement = Arrangement.Center,
//            horizontalAlignment = Alignment.CenterHorizontally,
//
//        ){
//            imageUri?.let {
//                Image(painter = rememberAsyncImagePainter(Uri) ,
//                    contentDescription = null,
//                    modifier = Modifier
//                        .size(128.dp)
//                        .clip(CircleShape),
//                    contentScale = ContentScale.Crop)
//            }
//            Spacer(modifier = Modifier.height(12.dp))
//
//            Button(onClick = { launcher.launch("image/*")},
//                colors = ButtonDefaults.buttonColors(GreenJC)
//                ) {
//                Text(text = "Choose Image")
//            }
//            Spacer(modifier = Modifier.height(16.dp))
//            TextField(value = name, onValueChange = { name = it },
//                label = { Text(text = "Name")},
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .clip(RoundedCornerShape(8.dp)),
//                colors = TextFieldDefaults.colors(
//                    focusedContainerColor = Color.White,
//                    unfocusedContainerColor = Color.White,
//                    focusedTextColor = Color.Black,
//                    unfocusedTextColor = Color.Black
//                )
//            )
//            Spacer(modifier = Modifier.height(8.dp))
//            TextField(value = phoneNumber, onValueChange = { phoneNumber = it },
//                label = { Text(text = "Phone Number")},
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .clip(RoundedCornerShape(8.dp)),
//                colors = TextFieldDefaults.colors(
//                    focusedContainerColor = Color.White,
//                    unfocusedContainerColor = Color.White,
//                    focusedTextColor = Color.Black,
//                    unfocusedTextColor = Color.Black
//                )
//            )
//
//            TextField(value = email, onValueChange = { email = it },
//                label = { Text(text = "Email")},
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .clip(RoundedCornerShape(8.dp)),
//                colors = TextFieldDefaults.colors(
//                    focusedContainerColor = Color.White,
//                    unfocusedContainerColor = Color.White,
//                    focusedTextColor = Color.Black,
//                    unfocusedTextColor = Color.Black
//                )
//            )
//            Spacer(modifier = Modifier.height(16.dp))
//
//            Button(onClick = { }) {
//                imageUri?.let {
//                    val internalPath = copyUriToInternalStorage(context, it, "$name.jpg")
//                    internalPath?.let { path ->
//                        viewModel.addContact(path, name, phoneNumber, email)
//                        navController.navigate("contactList"){
//                            popUpTo(0)
//                        }
//                    }
//                }
//            }, colors = ButtonDefaults.buttonColors(GreenJC)) {
//                Text(text = "Add Contact")
//        }
//        }
//
//    }
//}
//
//fun copyUriToInternalStorage(context: Context, uri:Uri, fileName: String): String? {
//    val file = File(context.filesDir, fileName)
//    return try {
//        context.contentResolver.openInputStream(uri)?.use { inputStream ->
//            FileOutputStream(file).use{ outputStream ->
//                inputStream.copyTo(outputStream)
//            }
//        }
//        file.absolutePath
//    } catch ( e: Exception) {
//        e.printStackTrace()
//        null
//    }
//}
//
