package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderEntity
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.StoreGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun InvoiceView(
    order: OrderEntity,
    onClose: () -> Unit,
    onSendWhatsApp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sdfDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val sdfTime = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val dateStr = sdfDate.format(Date(order.createdAt))
    val timeStr = sdfTime.format(Date(order.createdAt))

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Bar with Close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🧾 Tax Invoice / Cash Memo",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = NavyPrimary
                )
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Bill Paper Container
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    // Store Title
                    Text(
                        text = "V K TRADERS & HARDWARE",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Good Quality Material • Hardware & Plumbing Store\nA/P Retre Karkhana, Shivnagar, Maharashtra\nPhone: 9623009626",
                        fontSize = 11.sp,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center,
                        lineHeight = 15.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, bottom = 10.dp)
                    )

                    HorizontalDivider(color = Color.Black, thickness = 1.5.dp)

                    // Meta Info
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Invoice: ${order.invoiceNo}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(text = "Date: $dateStr  Time: $timeStr", fontSize = 11.sp, color = Color.DarkGray)
                            Text(
                                text = "Status: ${order.status.uppercase()}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (order.status == "Delivered") StoreGreen else NavyPrimary
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = order.customerName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(text = "Mob: ${order.customerPhone}", fontSize = 11.sp, color = Color.DarkGray)
                            Text(
                                text = if (order.orderType == "delivery") "🏠 Home Delivery" else "🏪 Self Pickup",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (order.customerAddress.isNotBlank()) {
                        Text(
                            text = "Address: ${order.customerAddress}",
                            fontSize = 11.sp,
                            color = Color.DarkGray,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    HorizontalDivider(color = Color.LightGray)

                    // Items Breakdown Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFEEEEEE))
                            .padding(vertical = 6.dp, horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Description of Goods", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(2f))
                        Text(text = "Amount", fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                    }

                    // Items List
                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                        order.itemsDetailsJson.lines().filter { it.isNotBlank() }.forEach { line ->
                            Text(
                                text = line,
                                fontSize = 12.sp,
                                color = Color.Black,
                                modifier = Modifier.padding(vertical = 3.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = Color.LightGray)

                    // Totals
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Subtotal", fontSize = 12.sp)
                            Text("₹${order.subtotal.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        if (order.discount > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Coupon Discount (${order.couponCode})", fontSize = 12.sp, color = StoreGreen)
                                Text("-₹${order.discount.toInt()}", fontSize = 12.sp, color = StoreGreen, fontWeight = FontWeight.Bold)
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 6.dp),
                            color = Color.Black,
                            thickness = 1.dp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Grand Total", fontSize = 15.sp, fontWeight = FontWeight.Black)
                            Text("₹${order.total.toInt()}", fontSize = 16.sp, fontWeight = FontWeight.Black, color = NavyPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Thank you for shopping with V K Traders & Hardware!",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val shareText = buildString {
                            append("--- V K TRADERS & HARDWARE ---\n")
                            append("Invoice: ${order.invoiceNo}\n")
                            append("Customer: ${order.customerName} (${order.customerPhone})\n")
                            append("Order: ${if (order.orderType == "delivery") "Home Delivery" else "Self Pickup"}\n\n")
                            append(order.itemsDetailsJson)
                            append("\n\nSubtotal: ₹${order.subtotal.toInt()}\n")
                            if (order.discount > 0) append("Discount: -₹${order.discount.toInt()}\n")
                            append("Grand Total: ₹${order.total.toInt()}\n")
                            append("Store Phone: 9623009626")
                        }
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Invoice"))
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("share_invoice_button")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share")
                }

                Button(
                    onClick = onSendWhatsApp,
                    modifier = Modifier
                        .weight(1.3f)
                        .testTag("invoice_whatsapp_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = StoreGreen)
                ) {
                    Text("💬 WhatsApp Order", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
