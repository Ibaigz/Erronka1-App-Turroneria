package com.example.r1_turroneriaivanez

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter

@Composable
fun CarritoScreen(
    navController: NavHostController,
    carritoViewModel: CarritoViewModel
) {
    val context = LocalContext.current
    val resources = context.resources
    val productos by carritoViewModel.listaProductosDetalle.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var showConfirmed by remember { mutableStateOf(false) }
    var segundos by remember { mutableStateOf(20) } // Estado para los segundos
    var timerThread: Thread? = remember { null } // Para manejar el hilo del temporizador

    // Lógica para el temporizador
    if (showDialog && timerThread == null) {
        timerThread = Thread {
            while (segundos > 0) {
                try {
                    Thread.sleep(1000) // Espera 1 segundo
                    segundos -= 1 // Resta un segundo

                    // Actualiza el estado en el hilo principal
                    (context as? android.app.Activity)?.runOnUiThread {
                    }
                } catch (e: InterruptedException) {
                    // Manejar la excepción si el hilo se interrumpe
                    e.printStackTrace()
                    break // Salir del bucle si el hilo es interrumpido
                }
            }
            // Cerrar el diálogo si el temporizador se completa
            if (segundos == 0) {
                (context as? android.app.Activity)?.runOnUiThread {
                    showDialog = false
                }
            }
        }.apply { start() } // Inicia el hilo
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        // Si el carrito está vacío evitar
        if (productos.isEmpty()) {
            Text(text = resources.getString(R.string.carrito_msg_vacio))
        } else {
            var precioTotal: Double = 0.00
            productos.forEach { producto ->
                val nombre = producto.nombre
                val cantXPreStr = producto.obtenerTxtPreXCant()
                val pTotal = producto.calcularTotal()

                precioTotal += pTotal // Acumulamos el precio total

                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp).fillMaxSize()) {
                    Row(modifier = Modifier.clickable {
                        navController.navigate("producto/${producto.id}") {
                            val currentDestination = navController.currentBackStackEntry?.destination?.route
                            if (currentDestination == "carrito") {
                                launchSingleTop = true
                                restoreState = true
                            } else {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = producto.imageUrl ?: R.drawable.turron_chocolate
                            ),
                            contentDescription = resources.getString(R.string.carrito_img_desc),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(50.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(text = nombre, fontWeight = FontWeight.Bold)
                    }
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.align(Alignment.End)) {
                        Image(
                            painter = painterResource(id = R.drawable.basura),
                            contentDescription = resources.getString(R.string.contentDes_mail),
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.inverseSurface ),
                            modifier = Modifier.size(25.dp)
                                .padding(horizontal = 2.dp)
                                .clickable {
                                    carritoViewModel.eliminarProducto(producto.id)
                                }
                        )
                        Text(text = cantXPreStr)
                    }
                }
            }
            val precioTFormateado = String.format("%.2f", precioTotal)
            val totalTextP1 = stringResource(R.string.carrito_total_p1)
            val totalTextP2 = stringResource(R.string.carrito_total_p2)

            Column(modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.End)
                .padding(10.dp)) {
                Text(
                    text = "$totalTextP1 $precioTFormateado $totalTextP2",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.End)
                )
                OutlinedButton(
                    onClick = {
                        showDialog = true
                        segundos = 20 // Reinicia el temporizador a 20 segundos
                    },
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 10.dp)
                        .width(180.dp)
                        .height(60.dp)
                        .border(
                            BorderStroke(
                                2.dp, color=MaterialTheme.colorScheme.primary,
                            ),
                            RectangleShape
                        ),
                    shape = RectangleShape,
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent
                    )
                ) {
                    Text(resources.getString(R.string.carrito_tramitar_btn), color = MaterialTheme.colorScheme.primary)
                }
            }

            // Mostrar el AlertDialog si se necesita
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showDialog = false
                        timerThread?.interrupt() // Detener el hilo al cerrar el diálogo
                        timerThread = null // Reiniciar el hilo
                    },
                    title = {
                        Text(resources.getString(R.string.carrito_confirmar_btn))
                    },
                    text = {
                        Text(resources.getString(R.string.tiempo_restante) + segundos+resources.getString(R.string.segundos)) // Mostrar el tiempo restante aquí
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showDialog = false
                                timerThread?.interrupt() // Detener el hilo
                                timerThread = null // Reiniciar el hilo
                            }
                        ) {
                            Text(resources.getString(R.string.carrito_cancelar_btn))
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = {

                                showConfirmed = true

                            }
                        ) {
                            Text(resources.getString(R.string.carrito_confirmar_popu))
                        }
                    }
                )
            }

            if (showConfirmed) {
                AlertDialog(
                    onDismissRequest = { showConfirmed = false }, // Cerrar al tocar fuera
                    title = {
                        Text(resources.getString(R.string.compra_finalizada))
                    },

                    confirmButton = {
                        TextButton(onClick = {
                            showDialog = false
                            timerThread?.interrupt() // Detener el hilo
                            timerThread = null // Reiniciar el hilo
                            carritoViewModel.vaciarCarrito()
                            showConfirmed = false
                            navController.navigate("home") {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = false // No restaurar estado previo
                                }
                                launchSingleTop = false
                                restoreState = false // Asegurarte de que no restaura el estado
                            }
                            carritoViewModel.vaciarCarrito()
                        }) {
                            Text(resources.getString(R.string.carrito_confirmar_popu))
                        }
                    }
                )
            }
        }
    }

    // Restablecer el estado de segundos y el hilo cuando se cierra el diálogo
    if (!showDialog && timerThread != null) {
        timerThread?.interrupt() // Detener el hilo cuando se cierra el diálogo
        segundos = 20 // Reinicia el temporizador a 20 segundos
        timerThread = null // Reinicia el hilo
    }
}

