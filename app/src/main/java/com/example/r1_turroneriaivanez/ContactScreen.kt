package com.example.r1_turroneriaivanez

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import android.os.Bundle
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.LocalConfiguration


@Composable

fun ContactoScreen(navController: NavHostController) {

    val context = LocalContext.current
    val resources = context.resources
    val configuration = LocalConfiguration.current
    val screenHeighting = configuration.screenHeightDp
    Column(modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(top=15.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(0.95f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = resources.getString(R.string.contact_header_1).uppercase(),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 24.sp,
                    modifier = Modifier.padding(top = 0.dp)
                )
                Text(
                    text = resources.getString(R.string.contact_header_2).uppercase(),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 0.dp)
                )
                Text(
                    text = resources.getString(R.string.contacto_body),
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 5.dp)
                )
                //INICIO++ Contacto email + tlf
                Column(modifier = Modifier
                    .fillMaxWidth(0.9f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse(resources.getString(R.string.uriparse_mailto))
                            }
                            try {
                                context.startActivity(
                                    Intent.createChooser(
                                        emailIntent,
                                        resources.getString(R.string.intentCreate_mail)
                                    )
                                )
                            } catch (e: Exception) {
                                Toast.makeText(context, resources.getString(R.string.intentFail_mail), Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = resources.getString(R.string.contentDes_mail),
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = resources.getString(R.string.contacto_email),
                            modifier = Modifier.padding(start = 8.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            val callIntent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse(resources.getString(R.string.uriparse_call))
                            }
                            try {
                                context.startActivity(Intent.createChooser(callIntent, resources.getString(R.string.intentCreate_call)))
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    resources.getString(R.string.intentFail_call),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = resources.getString(R.string.contentDes_call),
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = resources.getString(R.string.contacto_tlf),
                            modifier = Modifier.padding(start = 8.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            val mapIntent = Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse(resources.getString(R.string.uriparse_map))
                            }
                            try {
                                context.startActivity(mapIntent)
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    resources.getString(R.string.intentFail_map),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = resources.getString(R.string.contentDes_map),
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = resources.getString(R.string.contacto_direccion),
                            modifier = Modifier.padding(start = 8.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                //FINAL-- Contacto email + tlf
                if (screenHeighting > 800) {
                    Text(
                        text = resources.getString(R.string.contacto_webmes),
                        modifier = Modifier.padding(top = 16.dp),
                        fontWeight = FontWeight.Bold
                    )
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = resources.getString(R.string.contdesc_general),
                        //colorFilter = ColorFilter.tint(colorResource(R.color.azul)),
                        modifier = Modifier
                            .fillMaxSize(0.7f)
                            .clickable {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(resources.getString(R.string.uriparse_turroweb))
                                )
                                ContextCompat.startActivity(context, intent, null)
                            }
                    )
                }
                //INICIO++ Sociales
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = resources.getString(R.string.contacto_sociales),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    //color = colorResource(R.color.azul)
                    )
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .padding(horizontal = 15.dp),
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Image(
                        painter = painterResource(id = R.drawable.svg_tripadvisor_logo),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.inverseSurface),
                        contentDescription = resources.getString(R.string.contdesc_general),
                        //colorFilter = ColorFilter.tint(colorResource(R.color.azul)),
                        modifier = Modifier
                            .size(50.dp)
                            .clickable {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(resources.getString(R.string.uriparse_tripad))
                                )
                                ContextCompat.startActivity(context, intent, null)
                            }
                    )
                    Image(
                        painter = painterResource(id = R.drawable.svg_x_logo),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.inverseSurface),
                        contentDescription = resources.getString(R.string.contdesc_general),
                        //colorFilter = ColorFilter.tint(colorResource(R.color.azul)),
                        modifier = Modifier
                            .size(50.dp)
                            .clickable {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(resources.getString(R.string.uriparse_twitterx))
                                )
                                ContextCompat.startActivity(context, intent, null)
                            }
                    )
                    Image(
                        painter = painterResource(id = R.drawable.svg_instagram_logo),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.inverseSurface),
                        contentDescription = resources.getString(R.string.contdesc_general),
                        //colorFilter = ColorFilter.tint(colorResource(R.color.azul)),
                        modifier = Modifier
                            .size(50.dp)
                            .clickable {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(resources.getString(R.string.uriparse_ig))
                                )
                                ContextCompat.startActivity(context, intent, null)
                            }
                    )
                    Image(
                        painter = painterResource(id = R.drawable.svg_facebook_logo),
                        contentDescription = resources.getString(R.string.contdesc_general),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.inverseSurface),
                        //colorFilter = ColorFilter.tint(colorResource(R.color.azul)),
                        modifier = Modifier
                            .size(50.dp)
                            .clickable {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(resources.getString(R.string.uriparse_fb))
                                )
                                ContextCompat.startActivity(context, intent, null)
                            }
                    )
                }
                //FINAL-- Sociales
            }
        }
    }
}