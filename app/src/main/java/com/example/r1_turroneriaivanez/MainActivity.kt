package com.example.r1_turroneriaivanez

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.r1_turroneriaivanez.ui.theme.IvanezTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale


class MainActivity : ComponentActivity() {

    private var perfilViewModel: PerfilViewModel? by mutableStateOf(null)
    private var tiendaViewModel: TiendaViewModel? by mutableStateOf(null)
    private lateinit var auth: FirebaseAuth
    private var isDarkTheme by mutableStateOf(false)


    override fun onCreate(savedInstanceState: Bundle?) {

        val sharedPreferences = getSharedPreferences("preferencias", Context.MODE_PRIVATE)
        val idiomaGuardado = sharedPreferences.getString("idioma_seleccionado", "es") // "es" por defecto
        isDarkTheme = sharedPreferences.getBoolean("modo_oscuro", false) // false como valor por defecto

        // Cambiar el idioma si es necesario
        val locale = Locale(idiomaGuardado ?: "es")
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, true)
        FirebaseApp.initializeApp(this)
        val auth: FirebaseAuth = FirebaseAuth.getInstance()

        tiendaViewModel = TiendaViewModel()
        perfilViewModel = PerfilViewModel()

        // Escucha cambios en la autenticación
        auth.addAuthStateListener { firebaseAuth ->
            val currentUser = firebaseAuth.currentUser
            // Aquí puedes hacer que el ViewModel se actualice
            val carritoViewModel = CarritoViewModel(currentUser)
            setContent {
                IvanezTheme(darkTheme = isDarkTheme) {
                    // Usa 'remember' para los ViewModels
                    val perfilViewModelState = remember { perfilViewModel }
                    val tiendaViewModelState = remember { tiendaViewModel }

                    ViewContainer(
                        window,
                        perfilViewModelState!!,
                        tiendaViewModelState!!,
                        carritoViewModel,
                        onThemeChange = {
                            isDarkTheme = !isDarkTheme // Cambiar el estado del tema
                            // Guardar la preferencia del tema
                            val editor = sharedPreferences.edit()
                            editor.putBoolean("modo_oscuro", isDarkTheme)
                            editor.apply() },
                        isDarkTheme = isDarkTheme
                    )
                }
            }
        }

        // Inicializa el carrito para el usuario actual (si hay uno)
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val carritoViewModel = CarritoViewModel(currentUser)
            setContent {
                IvanezTheme {
                    // Usa 'remember' para los ViewModels
                    val perfilViewModelState = remember { perfilViewModel }
                    val tiendaViewModelState = remember { tiendaViewModel }

                    ViewContainer(
                        window,
                        perfilViewModelState!!,
                        tiendaViewModelState!!,
                        carritoViewModel,
                        onThemeChange = {
                            isDarkTheme = !isDarkTheme // Cambiar el estado del tema
                            // Guardar la preferencia del tema
                            val editor = sharedPreferences.edit()
                            editor.putBoolean("modo_oscuro", isDarkTheme)
                            editor.apply() },
                        isDarkTheme = isDarkTheme
                    )
                }
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ViewContainer(
    window: Window,
    perfilViewModel: PerfilViewModel,
    tiendaViewModel: TiendaViewModel,
    carritoViewModel: CarritoViewModel,
    onThemeChange: () -> Unit,
    isDarkTheme: Boolean
) {
    val navController = rememberNavController()
    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    Scaffold(
        topBar = { TopBar(navController, onThemeChange, isDarkTheme)  },
        bottomBar = { BottomNavBar(navController) },
        content = { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("home") { HomeScreen(navController) }
                composable("Tienda") {
                    TiendaScreen(
                        navController,
                        tiendaViewModel,
                        carritoViewModel
                    )
                }
                composable("Perfil") { PerfilScreen(perfilViewModel) }
                composable("producto/{articuloId}") { backStackEntry ->
                    val articuloId = backStackEntry.arguments?.getString("articuloId")
                    MostrarProducto(navController, articuloId, carritoViewModel)
                    /*Box(modifier = Modifier.fillMaxSize(.95f)) {
                        Column(modifier = Modifier.align(Alignment.BottomEnd)) {
                            BotonCarrito(navController, articuloId)
                            }
                        }

                     */
                }
                composable("Contacto") { ContactoScreen(navController) }


                // Modificamos el bloque del carrito
                composable("carrito") {
                    when (auth.currentUser) {
                        null -> PerfilScreen(perfilViewModel)
                        else -> CarritoScreen(navController, carritoViewModel)
                    }
                }
            }
        }
    )

}


@Composable
fun TopBar(navController: NavHostController, onThemeChange: () -> Unit, isDarkTheme: Boolean) {

    data class Idioma(
        val nombre: String,
        val codigo: String // Código de idioma (es, en, eu, etc.)
    )

    val context = LocalContext.current
    val resources = context.resources
    val idiomas = listOf(
        Idioma(resources.getString(R.string.main_esp), "es"),
        Idioma(resources.getString(R.string.main_ing), "en"),
        Idioma(resources.getString(R.string.main_eus), "eu")
    )

    var expanded by remember { mutableStateOf(false) } // Estado del menú desplegable
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = resources.getString(R.string.main_header_dir),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        // Franja azul para logo y carrito
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(85.dp)
                .background(color = MaterialTheme.colorScheme.primary)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Imagen del logo
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = resources.getString(R.string.main_Logo),
                    modifier = Modifier.height(40.dp),
                    contentScale = ContentScale.Fit
                )

                IconButton(onClick = onThemeChange) {
                    Icon(
                        painter = if (isDarkTheme) {
                            painterResource(id = R.drawable.ic_moon) // Cambia por tu recurso de luna
                        } else {
                            painterResource(id = R.drawable.ic_sun) // Cambia por tu recurso de sol
                        },
                        contentDescription = resources.getString(R.string.Cambiar_tema),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }


                //Boton con logo de idioma
                Image(
                    painter = painterResource(id = R.drawable.idioma), // Cambia por tu recurso
                    contentDescription = resources.getString(R.string.cambiar_idioma),
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { expanded = true }, // Abre el menú al hacer clic
                    contentScale = ContentScale.Fit
                )

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier =  Modifier.background(MaterialTheme.colorScheme.secondaryContainer),
                    offset = DpOffset(120.dp, 0.dp), // Ajustar
                ) {
                    idiomas.forEach { idioma ->
                        DropdownMenuItem(
                            text = { Text(idioma.nombre
                                , color = MaterialTheme.colorScheme.onSecondaryContainer
                                ) },
                            onClick = {
                                expanded = false // Cierra el menú
                                // Guardar el idioma seleccionado en SharedPreferences
                                val sharedPreferences = context.getSharedPreferences(
                                    "preferencias",
                                    Context.MODE_PRIVATE
                                )
                                val editor = sharedPreferences.edit()
                                editor.putString("idioma_seleccionado", idioma.codigo)
                                editor.apply()

                                // Cambiar el idioma de la aplicación
                                cambiarIdioma(context, idioma.codigo)
                            }
                        )
                    }
                }

                // Icono del carrito
                IconButton(onClick = {
                    navController.navigate("carrito") {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart, // Uso de ícono de carrito predeterminado
                        contentDescription = resources.getString(R.string.Carrito),
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

    }
}


fun cambiarIdioma(context: Context, codigoIdioma: String) {
    // Crear un objeto Locale con el código de idioma
    val locale = java.util.Locale(codigoIdioma)

    // Establecer el Locale global
    java.util.Locale.setDefault(locale)

    // Crear un objeto Configuration y establecer el Locale
    val config = context.resources.configuration
    config.setLocale(locale)

    // Actualizar la configuración de recursos
    context.resources.updateConfiguration(config, context.resources.displayMetrics)

    // Reiniciar la actividad para aplicar el cambio de idioma
    val intent = Intent(context, MainActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
    context.startActivity(intent)
}

@Composable
@Preview
fun NavBarPreview() {
    IvanezTheme(false) {
        BottomNavBar(navController = rememberNavController())
    }
}

@Composable
fun BottomNavBar(navController: NavHostController) {
    val context = LocalContext.current
    val resources = context.resources
    NavigationBar(
        modifier = Modifier.height(100.dp),
        containerColor = MaterialTheme.colorScheme.primary
    ) {
        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Default.Home,
                    contentDescription = resources.getString(R.string.main_home),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            },
            selected = navController.currentBackStackEntry?.destination?.route == "home",
            onClick = {
                navController.navigate("home") {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )

        NavigationBarItem(
            icon = {
                Image(
                    painter = painterResource(id = R.drawable.baseline_shopping_bag_24),
                    contentDescription = resources.getString(R.string.Tienda),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary)
                )
            },
            selected = navController.currentBackStackEntry?.destination?.route == "Tienda",
            onClick = {
                navController.navigate("Tienda") {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Default.Email,
                    contentDescription = resources.getString(R.string.Contacto),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            },
            selected = navController.currentBackStackEntry?.destination?.route == "Contacto",
            onClick = {
                navController.navigate("Contacto") {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Default.AccountCircle,
                    contentDescription = resources.getString(R.string.Perfil),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            },
            selected = navController.currentBackStackEntry?.destination?.route == "Perfil",
            onClick = {
                navController.navigate("Perfil") {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
    }
}
