package com.example.ivalid_compose.ui.product

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ivalid_compose.ui.cart.CartViewModel
import com.example.ivalid_compose.ui.home.Product
import com.example.ivalid_compose.ui.theme.GreenAccent
import com.example.ivalid_compose.ui.theme.RedPrimary
import com.example.ivalid_compose.ui.theme.RedPrimaryDark
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailsScreen(
    product: Product?,
    onBack: () -> Unit,
    onAddedToCart: () -> Unit,
    cartViewModel: CartViewModel
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()


    if (product == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Detalhes") },
                    navigationIcon = {
                        IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, contentDescription = "Voltar") }
                    }
                )
            }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Produto não encontrado", color = MaterialTheme.colorScheme.error)
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(product.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        var quantity by remember { mutableStateOf(1) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF5F5F5))
            ) {
                Image(
                    painter = painterResource(id = product.imageRes),
                    contentDescription = product.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(RedPrimary)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "-${product.discountPercent}%",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                val (bg, fg) = when {
                    product.expiresInDays <= 2 -> RedPrimary.copy(alpha = 0.15f) to RedPrimary
                    product.expiresInDays <= 7 -> Color(0xFFFFA000).copy(alpha = 0.18f) to Color(0xFFFFA000)
                    else -> GreenAccent.copy(alpha = 0.16f) to GreenAccent
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(50))
                        .background(bg)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Vence em ${product.expiresInDays}d",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = fg, fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                product.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                product.brand,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "${product.storeName} • ${"%.1f".format(product.distanceKm)} km",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    "R$ ${"%.2f".format(product.priceNow)}",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = RedPrimary
                    )
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    "R$ ${"%.2f".format(product.priceOriginal)}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textDecoration = TextDecoration.LineThrough
                    )
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                    .padding(10.dp)
            ) {
                Text("Quantidade", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                QuantityStepper(
                    value = quantity,
                    onIncrement = { quantity += 1 },
                    onDecrement = { if (quantity > 1) quantity -= 1 }
                )
            }

            Spacer(Modifier.height(16.dp))


            Text(
                "Retirada na loja até o dia ${calcPickupDeadline(product.expiresInDays)}.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(24.dp))


            val snackbarHostState = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()

            GradientRedButton(
                text = "Adicionar ao carrinho",
                enabled = quantity > 0,
                onClick = {
                    cartViewModel.add(product, quantity)
                    scope.launch { snackbarHostState.showSnackbar("Adicionado ao carrinho") }
                    onAddedToCart()
                },
                modifier = Modifier.fillMaxWidth()
            )


            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun QuantityStepper(
    value: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onDecrement, enabled = value > 1) {
            Icon(Icons.Outlined.Remove, contentDescription = "Diminuir")
        }
        Text(
            value.toString(),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.widthIn(min = 32.dp),
        )
        IconButton(onClick = onIncrement) {
            Icon(Icons.Outlined.Add, contentDescription = "Aumentar")
        }
    }
}

private fun calcPickupDeadline(expiresInDays: Int): String {
    return when {
        expiresInDays <= 0 -> "hoje"
        expiresInDays == 1 -> "amanhã"
        else -> "em até $expiresInDays dias"
    }
}

@Composable
private fun GradientRedButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(14.dp)
    val gradient = Brush.horizontalGradient(listOf(RedPrimary, RedPrimaryDark))
    val contentAlpha = if (enabled) 1f else 0.5f

    Box(
        modifier = modifier
            .height(50.dp)
            .clip(shape)
            .background(brush = gradient)
            .alpha(contentAlpha)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall.copy(
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}