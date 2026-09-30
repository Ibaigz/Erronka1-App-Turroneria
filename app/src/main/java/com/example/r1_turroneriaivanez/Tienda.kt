package com.example.r1_turroneriaivanez
import android.annotation.SuppressLint
import android.widget.Toast
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun TiendaScreen(navController: NavController, viewModel: TiendaViewModel, carritoViewModel: CarritoViewModel) {
    // Obtenemos la lista de artículos y posibles errores desde el ViewModel
    val articulos by viewModel.articulos.observeAsState(initial = emptyList())
    val error by viewModel.error.observeAsState()

    // Si hay un error, mostrar un Toast
    if (error != null) {
        Toast.makeText(LocalContext.current, error, Toast.LENGTH_SHORT).show()
    }

    // Mostrar el contenido de la tienda
    Scaffold(
        content = {
            TiendaContent(
                isSingleColumn = viewModel.isSingleColumn,
                articulos = articulos,
                navController = navController // Pasamos el NavController para manejar navegación
            )
        }
    )
}



@Composable
fun TiendaContent(
    isSingleColumn: MutableState<Boolean>,
    articulos: List<Articulos>,
    navController: NavController

) {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp
    val context = LocalContext.current
    val resources = context.resources
    val columns = when {
        screenWidthDp < 530.dp -> 1 // Pantallas más pequeñas como teléfonos
        else -> 2 // Pantallas más grandes como tabletas
    }
    if(columns == 1) {

        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier
                .fillMaxSize()
        ) {

            items(
                count = articulos.size, // Zerrendako elementu kopurua
                key = { index -> articulos[index].name } // Arte-lan bakoitzaren gako bakarra
            ) { index ->
                val articulo = articulos[index] // Uneko arte-lana lortu
                ArticuloCardSmall(articulo, navController, resources)
            }
        }
    }else {


        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier
                .fillMaxSize()
        ) {

            items(
                count = articulos.size, // Zerrendako elementu kopurua
                key = { index -> articulos[index].name } // Arte-lan bakoitzaren gako bakarra
            ) { index ->
                val articulo = articulos[index] // Uneko arte-lana lortu
                ArticuloCardBig(articulo, navController, resources)
            }
        }
    }

}


@Composable
fun ArticuloCardBig(articulos: Articulos,navController: NavController, resources: android.content.res.Resources) {
    Column {
        // Ikonoa irudiaren gainean jartzeko edukiontzia
        Box(
            modifier = Modifier
                //.background(colorResource(id = R.color.azul))
                .align(Alignment.CenterHorizontally)
                .height(200.dp) // Opcional: Ajusta la altura de la imagen
                .padding(top = 8.dp)
        ) {
            // Arte-lanaren irudia
            Image(
                painter = rememberAsyncImagePainter(
                    model = articulos.image
                        ?: R.drawable.offline  // Usa la imagen predeterminada si la URL es null
                ),
                contentDescription = resources.getString(R.string.contdesc_general),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .clickable {
                        navController.navigate("producto/${articulos.id}")
                    }
                    .size(200.dp)
                    .clip(RoundedCornerShape(16.dp)) // Irudia biribildu
                    .align(Alignment.Center) // Centra la imagen dentro del Box
            )
        }

        // Izenburuaren atzealdea
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp), // Espaciado horizontal
            horizontalArrangement = Arrangement.Start, // Alineación horizontal
            verticalAlignment = Alignment.CenterVertically // Alineación vertical
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterVertically) // Alinea la columna con la imagen
            ) {
                // Arte-lanaren izenburua
                Text(
                    text = articulos.name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        lineHeight = 20.sp // Ajusta el espaciado de las líneas
                    ),
                    fontSize = 18.sp,
                    modifier = Modifier,
                    color = MaterialTheme.colorScheme.primary // Testuaren kolorea
                )
                Text(
                    text = articulos.price + resources.getString(R.string.carrito_total_p2),
                    style = MaterialTheme.typography.bodyLarge, // EB Garamond iturria erabilita
                    fontSize = 18.sp,
                    modifier = Modifier
                        .padding(top = 2.dp),
                    color = MaterialTheme.colorScheme.secondary // Testuaren kolorea
                )
            }
        }
    }
}
    @Composable
    fun ArticuloCardSmall(articulos: Articulos, navController: NavController, resources: android.content.res.Resources) {
        Column {
            // Ikonoa irudiaren gainean jartzeko edukiontzia
            Box(

                modifier = Modifier
                    //.background(colorResource(id = R.color.azul))
                    .align(Alignment.CenterHorizontally)
                    .height(330.dp)
                    .fillMaxWidth(0.85f)// Opcional: Ajusta la altura de la imagen
                    .padding(top = 8.dp)
            ) {
                // Arte-lanaren irudia
                Image(
                    painter = rememberAsyncImagePainter(
                        model = articulos.image
                            ?: R.drawable.offline  // Usa la imagen predeterminada si la URL es null
                    ),
                    contentDescription = resources.getString(R.string.contdesc_general),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .clickable {
                            navController.navigate("producto/${articulos.id}")
                        }
                        .fillMaxSize() //Antes era fillMaxWidth()
                        .padding(top = 6.dp)
                        .clip(RoundedCornerShape(16.dp)) // Irudia biribildu
                        .align(Alignment.Center) // Centra la imagen dentro del Box
                )
            }

            // Izenburuaren atzealdea
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp), // Espaciado horizontal
                horizontalArrangement = Arrangement.Start, // Alineación horizontal
                verticalAlignment = Alignment.CenterVertically // Alineación vertical
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterVertically) // Alinea la columna con la imagen

                ) {
                    Row(
                        modifier = Modifier
                            .padding(top = 3.dp, bottom = 8.dp)
                            .fillMaxWidth(),

                    horizontalArrangement = Arrangement.SpaceBetween,
                        // Espacio igual entre los elementos
                    ){
                    // Arte-lanaren izenburua
                    Text(
                        text = articulos.name,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            lineHeight = 20.sp // Ajusta el espaciado de las líneas
                        ),
                        fontSize = 18.sp,
                        modifier = Modifier
                            .fillMaxWidth(0.6f),
                        color = MaterialTheme.colorScheme.primary // Testuaren kolorea
                    )
                    Text(
                        text = articulos.price + resources.getString(R.string.carrito_total_p2),
                        style = MaterialTheme.typography.bodyLarge, // EB Garamond iturria erabilita
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.secondary // Testuaren kolorea
                    )
                    }
                }
            }
        }

    }


