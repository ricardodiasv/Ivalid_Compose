package com.example.ivalid_compose.ui.cart

import android.content.res.Resources
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ivalid_compose.ui.theme.RedPrimary
import com.example.ivalid_compose.ui.theme.RedPrimaryDark
import java.util.Locale




@Composable
private fun PriceTextStruck(
    text: String,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    lineColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    thickness: Dp = 1.3.dp,
    yFactor: Float = 0.56f,
    style: TextStyle = MaterialTheme.typography.bodySmall,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    Text(
        text = text,
        style = style,
        color = textColor,
        maxLines = 1,
        overflow = TextOverflow.Clip,
        modifier = modifier.drawWithContent {

            drawContent()

            val y = size.height * yFactor
            drawLine(
                color = lineColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = with(density) { thickness.toPx() }
            )
        }
    )
}





@Composable
private fun safePainterResource(@DrawableRes id: Int): Painter? {
    val context = LocalContext.current
    val exists = remember(id) {
        try {
            context.resources.getResourceName(id)
            true
        } catch (_: Resources.NotFoundException) {
            false
        }
    }
    return if (exists) painterResource(id) else null
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    cartViewModel: CartViewModel,
    onBack: () -> Unit,
    onCheckout: () -> Unit
) {
    val state = cartViewModel.uiState

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Carrinho (${state.count})") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    if (state.items.isNotEmpty()) {
                        TextButton(onClick = { cartViewModel.clear() }) {
                            Text("Limpar", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (state.items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Seu carrinho está vazio", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 12.dp)
                ) {
                    items(state.items, key = { it.product.id }) { item ->
                        CartItemRow(
                            item = item,
                            onIncrement = {
                                cartViewModel.setQuantity(item.product.id, item.quantity + 1)
                            },
                            onDecrement = {
                                cartViewModel.setQuantity(item.product.id, item.quantity - 1)
                            },
                            onRemove = { cartViewModel.remove(item.product.id) }
                        )
                    }
                }

                SummaryBox(
                    total = state.total,
                    onCheckout = onCheckout
                )
            }
        }
    }
}
@Composable
private fun CartItemRow(
    item: CartItem,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    Surface(
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp), // Adicionado padding para não colar nas bordas
            verticalAlignment = Alignment.CenterVertically
        ) {
            val painter = safePainterResource(item.product.imageRes)

            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF2F2F2)),
                contentAlignment = Alignment.Center
            ) {
                if (painter != null) {
                    Image(
                        painter = painter,
                        contentDescription = item.product.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Text("Sem imagem", style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.product.name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = item.product.storeName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "R$ ${"%.2f".format(java.util.Locale("pt", "BR"), item.product.priceNow)}",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = RedPrimary
                        ),
                        maxLines = 1
                    )

                    Spacer(Modifier.width(8.dp))

                    if (item.product.priceOriginal > item.product.priceNow) {
                        PriceTextStruck(
                            text = "R$ ${"%.2f".format(java.util.Locale("pt", "BR"), item.product.priceOriginal)}",
                            textColor = Color.Gray,
                            lineColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            thickness = 1.3.dp,
                            yFactor = 0.56f,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }

            Spacer(Modifier.width(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDecrement, enabled = item.quantity > 1) {
                    Icon(Icons.Outlined.Remove, contentDescription = "Diminuir")
                }
                Text(
                    text = item.quantity.toString(),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.widthIn(min = 28.dp)
                )
                IconButton(onClick = onIncrement) {
                    Icon(Icons.Outlined.Add, contentDescription = "Aumentar")
                }
            }

            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Remover",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
@Composable
private fun SummaryBox(
    total: Double,
    onCheckout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp)
            .padding(bottom = 12.dp)
    ) {
        Divider()
        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Total", style = MaterialTheme.typography.titleMedium)
            Text(
                "R$ ${"%.2f".format(Locale("pt", "BR"), total)}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = RedPrimary
                )
            )
        }

        Spacer(Modifier.height(12.dp))

        GradientRedButton(
            text = "Finalizar compra",
            enabled = total > 0.0,
            onClick = onCheckout,
            modifier = Modifier.fillMaxWidth()
        )
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
            .clickable(enabled = enabled, onClick = onClick)
            .semantics { role = Role.Button },
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