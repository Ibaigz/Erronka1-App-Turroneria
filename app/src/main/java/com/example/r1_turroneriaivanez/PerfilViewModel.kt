package com.example.r1_turroneriaivanez

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.recreate
import androidx.core.app.ActivityOptionsCompat
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PerfilViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private lateinit var googleSignInClient: GoogleSignInClient

    // Variable para almacenar el estado del usuario
    private val _user = MutableStateFlow<FirebaseUser?>(null)
    val user: StateFlow<FirebaseUser?> get() = _user

    private val _errorMsg = MutableLiveData<Int?>()
    val errorMsg: LiveData<Int?> = _errorMsg

    init {
        // Verifica el estado de autenticación al iniciar el ViewModel
        _user.value = auth.currentUser
        _errorMsg.value = null
    }

    fun validarContrasena(password: String): Boolean {
        // Verificar que la contraseña cumpla con los requisitos de seguridad
        val passwordPattern = Regex("^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*]).{8,}\$")
        return passwordPattern.matches(password)
    }

    fun validarEmail(email: String): Boolean {
        // Verificar que el email tiene un formato válido
        val emailPattern = Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\$")
        return emailPattern.matches(email)
    }

    fun login(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _errorMsg.value = R.string.error_empty_fields
            return
        }
        if (!validarEmail(email)) {
            _errorMsg.value = R.string.error_invalid_email
            return
        }
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                _user.value = auth.currentUser
                _errorMsg.value = null
            } else {
                _errorMsg.value = R.string.error_login
            }
        }
    }



    fun logout(context: Context) {
        auth.signOut()
        _user.value = null
        googleSignInClient = GoogleSignIn.getClient(context, GoogleSignInOptions.DEFAULT_SIGN_IN)
        googleSignInClient.signOut().addOnCompleteListener {
            _user.value = null // Actualiza el estado del usuario en el ViewModel
        }.addOnFailureListener {
        }
        //ActivityCompat.recreate(Activity())
    }

    fun register(name: String, surname: String, email: String, password: String) {
        if (name.isEmpty() || surname.isEmpty() || email.isEmpty() || password.isEmpty()) {
            _errorMsg.value = R.string.error_empty_fields
            return
        }

        if (!validarEmail(email)) {
            _errorMsg.value = R.string.error_invalid_email
            return
        }

        if (!validarContrasena(password)) {
            _errorMsg.value = R.string.error_weak_password
            return
        }
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userId = auth.currentUser?.uid
                val userData = hashMapOf(
                    "name" to name,
                    "surname" to surname,
                    "email" to email
                )
                // Guarda los datos del usuario en Firestore
                if (userId != null) {
                    firestore.collection("users").document(userId).set(userData)
                        .addOnSuccessListener {
                            _user.value = auth.currentUser // Actualiza el estado del usuario
                            _errorMsg.value = null
                        }.addOnFailureListener {
                        _errorMsg.value = R.string.error_registro
                    }
                }
            } else {
                _errorMsg.value = R.string.error_email_exists
            }
        }
    }

    fun iniciarSesionConGoogle(
        activity: Context,
        signInLauncher: ActivityResultLauncher<Intent>,
        context: Context
    ) {

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(activity, gso)
        val signInIntent = googleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }

    fun manejarResultadoInicioSesion(data: Intent?) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            auth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _user.value = auth.currentUser

                        val userId = auth.currentUser?.uid
                        val userData = hashMapOf(
                            "name" to account.displayName,
                            "surname" to account.displayName,
                            "email" to account.email
                        )
                        if (userId != null) {
                            firestore.collection("users").document(userId).get().addOnSuccessListener { document ->
                                if (!document.exists()) {
                                    // Si el documento no existe, lo creamos
                                    firestore.collection("users").document(userId).set(userData).addOnSuccessListener {
                                        Log.d("Firestore", "Usuario guardado correctamente en Firestore")
                                    }.addOnFailureListener {
                                        Log.w("Firestore", "Error al guardar el usuario en Firestore", it)
                                    }
                                } else {
                                    Log.d("Firestore", "El usuario ya existe en Firestore")
                                }
                            }.addOnFailureListener {
                                Log.w("Firestore", "Error al verificar el usuario en Firestore", it)
                            }
                        }

                    } else {
                        // Maneja el error
                        Log.w("FirebaseAuth", "Inicio de sesión fallido", task.exception)
                    }
                }
        } catch (e: ApiException) {
            Log.w("GoogleSignIn", "Google sign in failed", e)
        }
    }
    companion object {
        const val RC_SIGN_IN = 9001
    }

}