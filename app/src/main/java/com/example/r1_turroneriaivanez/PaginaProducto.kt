package com.example.r1_turroneriaivanez

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.storage.storage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


@Composable
fun BotonCarrito(navController: NavController, articuloId: String?, carritoViewModel: CarritoViewModel) {
    val context = LocalContext.current
    val resources = context.resources
    var showDialog by remember { mutableStateOf(false) }
    val db = FirebaseFirestore.getInstance()
    val userEmail = FirebaseAuth.getInstance().currentUser?.email
    println(userEmail)// Recuperamos el email del usuario autenticado

    if (userEmail != null) {

        OutlinedButton(
            onClick = {
                showDialog = true
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser != null) {
                    if (articuloId != null) {
                        carritoViewModel.agregarAlCarrito(articuloId, 1)
                    }

                }
            },
            modifier = Modifier
                .padding(bottom = 8.dp)
                .width(180.dp)
                .height(60.dp)
                .border(
                    BorderStroke(
                        2.dp, color = MaterialTheme.colorScheme.primary,
                    ),
                    RectangleShape
                ),
            shape = RectangleShape,
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.Transparent
            )
        ) {
            Text(resources.getString(R.string.carrito_button),  color = MaterialTheme.colorScheme.primary)
        }
        Box(modifier = Modifier.padding(start = 10.dp, end = 10.dp, top = 10.dp, bottom = 14.dp)) {
            HorizontalDivider(thickness = 2.dp)
        }
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = {
                    Text(text = resources.getString(R.string.pagpro_prod_anadido))
                },

                confirmButton = {
                    TextButton(
                        onClick = {
                            // Action when the confirm button is clicked
                            showDialog = false
                        }
                    ) {
                        Text(resources.getString(R.string.pagpro_seguir_comp))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            // Action when the dismiss button is clicked
                            showDialog = false
                            navController.navigate("carrito") {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    ) {
                        Text(resources.getString(R.string.pagpro_tramitar))
                    }
                }
            )
        }
    }else{
        Box(modifier = Modifier.padding(start = 15.dp, end = 15.dp, top = 15.dp, bottom = 10.dp)) {
            HorizontalDivider(thickness = 2.dp)
        }
        Text(
            text =  resources.getString(R.string.iniciaSesion),
            color = MaterialTheme.colorScheme.error,
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.wrapContentSize(Alignment.Center)

        )

        Box(modifier = Modifier.padding(start = 15.dp, end = 15.dp, top = 10.dp, bottom = 10.dp)) {
            HorizontalDivider(thickness = 2.dp)
        }

    }}



@Composable
fun MostrarProducto(navController: NavController, articuloId: String?, carritoViewModel: CarritoViewModel) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val storage = Firebase.storage
    val resources = context.resources

    if (articuloId != null) {
        var imagen by remember { mutableStateOf("") }
        var nombre by remember { mutableStateOf("") }
        var precio by remember { mutableStateOf("") }
        var desc by remember { mutableStateOf("") }
        var isLoading by remember { mutableStateOf(true) }

        LaunchedEffect(articuloId) {
            // Cambia el estado de carga antes de hacer la consulta
            isLoading = true

            // Consultar Firestore para obtener el artículo con el ID dado
            val docRef = db.collection("Productos").document(articuloId)

            docRef.get().addOnSuccessListener { document ->
                isLoading = false // Establece isLoading en false después de la consulta

                if (document.exists()) {
                    // Obtén los detalles del producto
                    nombre = document.getString("Nombre") ?: ""
                    precio = document.getString("Precio") ?: ""
                    desc = document.getString("Descripcion") ?: ""

                    // Obtener la lista de imágenes que comienzan con articuloId_


                    // Obtener la lista de todas las imágenes
                    val itemRef = storage.reference.child("${articuloId}_1.png")
                    itemRef.downloadUrl.addOnSuccessListener { uri ->
                        imagen = uri.toString() // Establecer solo la URL de la imagen deseada
                    }
                } else {
                    isLoading = false // Cambia el estado de carga si no se encuentra el documento
                }
            }.addOnFailureListener { exception ->
                isLoading = false // Cambia el estado de carga si hay un error
            }
        }


        // Mostrar el contenido del producto
        if (isLoading) {
            Text(text = resources.getString(R.string.pagpro_cargando), fontSize = 20.sp)
        } else {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                OutlinedButton(onClick = { navController.navigateUp() }) {
                    Text(resources.getString(R.string.pagpro_volver))
                }

                if (imagen.isNotEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(model = imagen),
                        contentDescription = resources.getString(R.string.contdesc_general),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(vertical = 4.dp)
                            .wrapContentSize(Alignment.Center)
                    )
                }
                Text(text = nombre, fontSize = 30.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold

                )

                val euro = resources.getString(R.string.carrito_total_p2)
                Text(text = "$precio $euro", fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.inverseSurface,
                    fontWeight = FontWeight.Bold

                )
                Spacer(modifier = Modifier.height(16.dp))
                BotonCarrito(navController = navController, articuloId = articuloId, carritoViewModel)
                Text(text = desc, fontSize = 20.sp)
            }
        }
    } else {
        Text(resources.getString(R.string.pagpro_id_inva))
    }


}
fun agregarProductoAlCarrito(db: FirebaseFirestore, userEmail: String, articuloId: String) {
    val carritoRef = db.collection("Carrito").document(userEmail)
    val productoRef = carritoRef.collection("productos").document(articuloId)

    val nuevoprod = hashMapOf(
        "Producto_id" to articuloId,
        "cantidad_id" to 1
    )
    db.collection("Carrito").document(carritoRef.toString()).get()
        .addOnSuccessListener { document ->
            if (document.exists()) {
            } else {
                db.collection("Carrito").document(userEmail).set(nuevoprod)
                    .addOnSuccessListener {
                    }
                    .addOnFailureListener { exception ->
                    }
            }
        }

}



