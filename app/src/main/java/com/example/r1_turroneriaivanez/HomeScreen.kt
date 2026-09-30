package com.example.r1_turroneriaivanez

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
//responsive
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.TextUnit

//START - Botón con bordes
@Composable
fun home_to_store_bt(navController: NavController) {
    val context = LocalContext.current
    val resources = context.resources
    OutlinedButton(
        onClick = {
            navController.navigate("Tienda") {
                popUpTo(navController.graph.startDestinationId) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        },
        modifier = Modifier
            .padding(top = 10.dp)
            .width(180.dp)
            .height(60.dp)
            .border(
                BorderStroke(2.dp, Color.White),
                RectangleShape
            ),
        shape = RectangleShape,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent
        )
    ) {
        Text(resources.getString(R.string.home_button), color = Color.White)
    }
}
//END- Botón con borde

//START- Caroussel
@Composable
fun infiniteScroll(imagesIds: List<Int>, resources: android.content.res.Resources) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            while (true) {
                delay(5000)
                var nextIndex = listState.firstVisibleItemIndex
                nextIndex += 1
                if (nextIndex == imagesIds.size - 1) {
                    listState.animateScrollToItem(nextIndex)
                    delay(5000)
                    nextIndex = 0
                    listState.animateScrollToItem(nextIndex)
                } else {
                    listState.animateScrollToItem(nextIndex)
                }
            }
        }
    }

    LazyRow(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        items(imagesIds.size) { index ->
            Image(
                painter = painterResource(id = imagesIds[index]),
                contentDescription = resources.getString(R.string.contdesc_general),
                modifier = Modifier
                    .size(300.dp)
                    .padding(50.dp)
            )
        }
    }
}
//END- Caroussel
//START- Texto header auto resize
@Composable
fun auto_resz_msg(
    text: String,
    fontWeight: FontWeight = FontWeight.Normal,
    textColor: Color = Color.Black,
    fontSize: TextUnit
) {
    var currentFontSize by remember { mutableStateOf(fontSize) } //Tamaño inicial del txt, si cabe es el que se usará
    var readyToDraw by remember { mutableStateOf(false) } //Check para cuando se va a imprimir

    Text(
        text = text,
        maxLines = 1,
        color = textColor,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodyLarge.copy(fontSize = currentFontSize, fontWeight = fontWeight), // Usa el fontWeight proporcionado
        lineHeight = 40.sp,
        modifier = Modifier.drawWithContent {
            if (readyToDraw) drawContent()
        },
        onTextLayout = { textLayoutResult ->
            if (textLayoutResult.hasVisualOverflow) {
                currentFontSize *= 0.9f
            } else {
                readyToDraw = true
            }
        }
    )
}


//END- Texto header auto resize

//START- Home
@Composable
fun HomeScreen(navController: NavHostController) {
    val context = LocalContext.current
    val resources = context.resources
    //++responsive
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    //--responsive
    var readyToDraw by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        //Imagen + texto + botón
        Box(modifier = Modifier
            .fillMaxWidth()
            .sizeIn( maxHeight = screenHeight * 0.4f)
        ){
            val bgImg: Painter = painterResource(id = R.drawable.home_img1)

            Image(
                painter = bgImg,
                contentDescription = resources.getString(R.string.contdesc_general),
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.tint(Color.Black.copy(alpha = 0.4f), blendMode = BlendMode.Darken),
                modifier = Modifier
                    .fillMaxWidth()
                    .blur(2.dp)
                    .background(Color.Black.copy(alpha = 0.3f))
            )

            // Texto sobre la imagen
            Column(
                modifier = Modifier
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                auto_resz_msg(resources.getString(R.string.turroneria_ibanez).uppercase(), FontWeight.Normal, Color.White, 38.sp)

                auto_resz_msg(resources.getString(R.string.en_bilbao_desde).uppercase(), FontWeight.Bold, Color.White, 24.sp)
                //Botón con bordes
                home_to_store_bt(navController = navController)
            }
        }
        //Resto de home v
        Box(modifier = Modifier.padding(all = 15.dp)) {
            auto_resz_msg(resources.getString(R.string.turrones_ibanez).uppercase(), FontWeight.Bold, MaterialTheme.colorScheme.primary, 30.sp)
        }

        BasicText(
            modifier = Modifier.padding(start = 15.dp, end = 15.dp),
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                    append(resources.getString(R.string.home_texto_turrones))
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(resources.getString(R.string.home_seccionbold))
                    }
                    append(resources.getString(R.string.home_texto_turrones2))
                }
            },
            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 20.sp)
        )

        val hImg1: Painter = painterResource(id = R.drawable.home_carr_2)
        val hImg2: Painter = painterResource(id = R.drawable.home_carr_1)

        Box(modifier = Modifier.padding(start = 15.dp, end = 15.dp, top = 30.dp, bottom = 10.dp)) {
            HorizontalDivider(thickness = 2.dp)
        }
        val carImages = listOf(
            R.drawable.home_carr_1,
            R.drawable.home_carr_2,
            R.drawable.home_carr_3,
            R.drawable.home_carr_4
        )
        infiniteScroll(imagesIds = carImages, resources)

    }




}
//END- Home