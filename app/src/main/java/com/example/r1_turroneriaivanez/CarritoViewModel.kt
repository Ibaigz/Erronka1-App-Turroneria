package com.example.r1_turroneriaivanez

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.r1_turroneriaivanez.CarritoViewModel.DetalleProducto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.Serializable

class CarritoViewModel(
    pUser: FirebaseUser?) : ViewModel() {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var user: FirebaseUser? = pUser

    private var productoPrincipal = firestore.collection("Productos")

    private var carrito : DocumentReference? = null
    private var productos : CollectionReference? = null

    private val _listaProductosDetalle = MutableStateFlow<List<DetalleProducto>>(emptyList())
    val listaProductosDetalle: StateFlow<List<DetalleProducto>> = _listaProductosDetalle



    init {

        if(user != null){
            carrito = firestore.collection("Carrito").document(user!!.uid)
            productos = carrito!!.collection("productos")

            Log.d("CarritoViewModel","Carrito inicializado id: ${carrito!!.id} - ${carrito}")

            MostrarTodosLosProductos()
        }
    }


    fun agregarAlCarrito(productoId: String, cantidad: Int) {
        Log.d("CarritoViewModel", "Agregando al carrito ${user!!.uid}...producto $productoId con cantidad $cantidad")

        // Actualiza la cantidad si el producto ya existe, o lo crea si no
        productos!!.document(productoId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Si el producto ya existe, actualiza la cantidad
                    val nuevaCantidad = document.getLong("cantidad")?.toInt()?.plus(cantidad) ?: cantidad
                    productos!!.document(productoId).update("cantidad", nuevaCantidad)
                        .addOnSuccessListener {
                            Log.d("CarritoViewModel", "Cantidad actualizada: $productoId -> $nuevaCantidad")
                        }
                } else {
                    // Si el producto no existe, lo crea
                    val nuevoProducto = hashMapOf(
                        "cantidad" to cantidad
                    )
                    productos!!.document(productoId).set(nuevoProducto)
                        .addOnSuccessListener {
                            Log.d("CarritoViewModel", "Producto agregado: $productoId")
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("CarritoViewModel", "Error al acceder al producto: $productoId", e)
            }
    }

    fun vaciarCarrito() {
        Log.d("CarritoViewModel", "Vaciando carrito ${user!!.uid}")
        val batch = FirebaseFirestore.getInstance().batch()

        productos!!.get()
            .addOnSuccessListener { querySnapshot ->
                querySnapshot.documents.forEach {
                    batch.delete(productos!!.document(it.id))
                }

                batch.commit().addOnSuccessListener {
                    _listaProductosDetalle.value = emptyList()
                    productos = carrito!!.collection("productos")
                    MostrarTodosLosProductos()
                }.addOnFailureListener { e ->
                    Log.e("CarritoViewModel", "Error al vaciar el carrito", e)
                }
            }
            .addOnFailureListener { e ->
                Log.e("CarritoViewModel", "Error al obtener los productos del carrito", e)
            }
    }




    fun eliminarProducto(idProducto: String) {
        Log.d("CarritoViewModel", "Eliminando producto del carrito: $idProducto")

        // Referencia al documento del producto en el carrito
        val productoRef = productos!!.document(idProducto)

        // Obtiene el documento del producto para verificar la cantidad actual
        productoRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val cantidadActual = document.getLong("cantidad")?.toInt() ?: 0

                    if (cantidadActual > 0) {
                        // Restar uno a la cantidad
                        val nuevaCantidad = cantidadActual - 1

                        if (nuevaCantidad > 0) {
                            // Si la nueva cantidad es mayor que 0, solo actualiza
                            productoRef.update("cantidad", nuevaCantidad)
                                .addOnSuccessListener {

                                    Log.d("CarritoViewModel", "Cantidad actualizada para $idProducto: $nuevaCantidad")
                                }
                                .addOnFailureListener { e ->
                                    Log.e("CarritoViewModel", "Error al actualizar la cantidad del producto: $idProducto", e)
                                }
                        } else {
                            // Si la nueva cantidad es 0, elimina el documento
                            productoRef.delete()
                                .addOnSuccessListener {
                                    _listaProductosDetalle.value = emptyList()
                                    productos = carrito!!.collection("productos")
                                    MostrarTodosLosProductos()
                                    Log.d("CarritoViewModel", "Producto eliminado del carrito: $idProducto")
                                }
                                .addOnFailureListener { e ->
                                    Log.e("CarritoViewModel", "Error al eliminar el producto del carrito: $idProducto", e)
                                }
                        }
                    }
                } else {
                    Log.e("CarritoViewModel", "Producto no encontrado en el carrito: $idProducto")
                }
            }
            .addOnFailureListener { e ->
                Log.e("CarritoViewModel", "Error al obtener el producto del carrito: $idProducto", e)
            }
    }

    fun actualizarProducto(idProducto: String, cantidad: Int) {
        Log.d("CarritoViewModel", "Actualizando producto del carrito ${user?.uid}...producto $idProducto a cantidad $cantidad")

        // Referencia a la subcolección "productos!!"
        productos!!.document(idProducto)
            .update("cantidad", cantidad)
            .addOnSuccessListener {
                Log.d("CarritoViewModel", "Cantidad actualizada para $idProducto: $cantidad")
            }
            .addOnFailureListener { e ->
                Log.e("CarritoViewModel", "Error al actualizar el producto: $idProducto", e)
            }
    }

    abstract class DetalleProducto(
        val id: String,
        val nombre: String,
        val descripcion: String,
        val precio: String,
        var cantidad: Int,
        var imageUrl: String
    ) : Serializable {

        // DOUBLE OR NULL Pasar coma decimal a punto decimal y después a DoN
        // NOTA: Si dejamos la coma decimal y pasamos a DoN siempre devuelve Nulo
        fun obtenerPrecioDouble(): Double {
            return precio.replace(",", ".").toDoubleOrNull() ?: 0.0
        }

        // STRING Obtener el precio formateado a 2 decimales
        fun obtenerPrecioFormateado(): String {
            return String.format("%.2f", obtenerPrecioDouble())
        }

        // DOUBLE cantidad X precio
        fun calcularTotal(): Double {
            return obtenerPrecioDouble() * cantidad
        }

        // STRING cantidad X precio + formateo
        fun obtenerTxtPreXCant(): String {
            return "$cantidad x ${obtenerPrecioFormateado()} €"
        }

    }

    // Implementación de la clase concreta
    class ProductoDetalle(
        id: String,
        nombre: String,
        descripcion: String,
        precio: String,
        cantidad: Int,
        imageUrl: String
    ) : DetalleProducto(id, nombre, descripcion, precio, cantidad, imageUrl)

    fun MostrarTodosLosProductos() {
        Log.d("CarritoViewModel", "Recogiendo todos los productos del carrito para el usuario: ${user!!.uid}")

        productos!!.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("CarritoViewModel", "Listen failed.", e)
                return@addSnapshotListener
            }

            val source = if (snapshot != null && snapshot.metadata.hasPendingWrites()) {
                "Local"
            } else {
                "Server"
            }

            if (snapshot != null && !snapshot.isEmpty) {
                Log.d("CarritoViewModel", "$source data: ${snapshot.documents}")

                val productosList = mutableListOf<DetalleProducto>()
                var productosProcesados = 0 // Contador de productos procesados

                snapshot.documents.forEach { documento ->
                    val idProducto = documento.id
                    val cantidad = documento.getLong("cantidad")?.toInt() ?: 0

                    // Llamada a getProductoDetails
                    getProductoDetails(idProducto, cantidad, productosList) {
                        // Incrementamos el contador de productos procesados
                        productosProcesados++

                        // Si todos los productos han sido procesados, actualizamos el StateFlow
                        if (productosProcesados == snapshot.documents.size) {
                            _listaProductosDetalle.value = productosList
                            Log.d("CarritoViewModel", "Todos los productos procesados y lista actualizada.")
                        }
                    }
                }
            } else {
                Log.d("CarritoViewModel", "$source data: null")
            }
        }
    }


    fun getProductoDetails(idProducto: String, cantidad: Int, productosList: MutableList<DetalleProducto>, onComplete: () -> Unit) {
        productoPrincipal.document(idProducto).get()
            .addOnSuccessListener { productoSnapshot ->
                if (productoSnapshot.exists()) {
                    val nombre = productoSnapshot.getString("Nombre") ?: "Desconocido"
                    val descripcion = productoSnapshot.getString("Descripcion") ?: "Sin descripción"
                    val precio = productoSnapshot.getString("Precio") ?: "0.0"
                    val db = FirebaseFirestore.getInstance()
                    val docRef = db.collection("productos").document(idProducto)
                    val storage = FirebaseStorage.getInstance() // Inicializa FirebaseStorage
                    val storageRef = storage.reference // Obtén la referencia


                    // Crear un objeto DetalleProducto con la información completa
                    val detalleProducto = ProductoDetalle(
                        id = idProducto,
                        nombre = nombre,
                        descripcion = descripcion,
                        precio = precio.toString(),
                        cantidad = cantidad,
                        imageUrl = ""
                    )
                    val imageRef = storageRef.child("${detalleProducto.id}_1.png")
                    // Intentar descargar la imagen
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        detalleProducto.imageUrl = uri.toString() // Asignar la URL de la imagen al Articulo
                    }.addOnFailureListener {
                        // Manejar errores si la imagen no se encuentra
                        detalleProducto.imageUrl = "" // O asigna una imagen por defecto
                    }

                    productosList.add(detalleProducto)
                    Log.d("CarritoViewModel", "Producto detallado cargado: $detalleProducto")
                } else {
                    Log.e("CarritoViewModel", "Producto no encontrado: $idProducto")
                }

                // Llamamos al callback cuando se haya procesado el producto
                onComplete()
            }
            .addOnFailureListener { e ->
                Log.e("CarritoViewModel", "Error al obtener detalles del producto: $idProducto", e)
                // Llamamos al callback incluso si falla, para no bloquear el proceso
                onComplete()
            }
    }

}
