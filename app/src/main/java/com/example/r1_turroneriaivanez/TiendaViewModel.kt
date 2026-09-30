package com.example.r1_turroneriaivanez

import android.content.Context
import android.widget.Toast
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.storage
import com.google.firebase.storage.FirebaseStorage

class TiendaViewModel() : ViewModel() {


    var isSingleColumn = mutableStateOf(false)

    private val _articulos = MutableLiveData<List<Articulos>>(emptyList())
    val articulos: LiveData<List<Articulos>> = _articulos

    private val _error = MutableLiveData<String>(null)
    val error: LiveData<String> = _error


    // Aquí directamente hacemos la consulta cuando el ViewModel es creado
    init {

        //cargarArticulosPrueba()
        cargarArticulos()
    }

    private fun cargarArticulos() {
        // Realizamos la consulta a la colección "Productos" de Firestore
        val db = FirebaseFirestore.getInstance()
        val storage = FirebaseStorage.getInstance() // Inicializa FirebaseStorage
        val storageRef = storage.reference // Obtén la referencia

        db.collection("Productos").get()
            .addOnSuccessListener { documents ->
                val articulosList = mutableListOf<Articulos>()

                for (document in documents) {
                    val name = document.getString("Nombre") ?: "Desconocido"
                    val description = document.getString("Descripcion") ?: "Sin descripción"
                    val price = document.getString("Precio") ?: "0"
                    val _id = document.id

                    // Crear referencia de imagen usando el ID del documento
                    val imageRef = storageRef.child("${_id}_1.png") // Usa storageRef
                    println(imageRef)

                    // Agregar un Articulo a la lista sin la imagen aún
                    val articulo = Articulos(
                        id = _id,
                        name = name,
                        description = description,
                        image = null, // Inicialmente sin imagen
                        price = price
                    )

                    // Intentar descargar la imagen
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        articulo.image = uri.toString() // Asignar la URL de la imagen al Articulo
                        // Actualizar la lista de artículos
                        _articulos.value = _articulos.value?.map {
                            if (it.id == articulo.id) articulo else it
                        }
                    }.addOnFailureListener {
                        // Manejar errores si la imagen no se encuentra
                        articulo.image = null // O asigna una imagen por defecto
                    }

                    articulosList.add(articulo)
                }

                // Actualiza la lista de artículos (sin imágenes en este momento)
                _articulos.value = articulosList
            }
            .addOnFailureListener { exception ->
                _error.value = "Error al obtener los artículos: ${exception.message}"
            }
    }

}
