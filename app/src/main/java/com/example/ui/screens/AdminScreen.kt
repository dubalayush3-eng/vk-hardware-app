package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CouponEntity
import com.example.data.model.OrderEntity
import com.example.data.model.ProductEntity
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.StoreGreen
import com.example.ui.theme.StoreRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    isUnlocked: Boolean,
    products: List<ProductEntity>,
    coupons: List<CouponEntity>,
    orders: List<OrderEntity>,
    onUnlock: (String) -> Boolean,
    onUpdatePin: (String) -> Boolean,
    onLock: () -> Unit,
    onAddProduct: (ProductEntity) -> Unit,
    onUpdateProduct: (ProductEntity) -> Unit,
    onDeleteProduct: (String) -> Unit,
    onAddCoupon: (CouponEntity) -> Unit,
    onToggleCouponActive: (CouponEntity) -> Unit,
    onDeleteCoupon: (String) -> Unit,
    onUpdateOrderStatus: (String, String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var passwordInput by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = NavyPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Store Management",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isUnlocked) {
                        TextButton(onClick = onLock) {
                            Text("🔒 Lock", fontSize = 12.sp, color = StoreRed, fontWeight = FontWeight.Bold)
                        }
                    }
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            if (!isUnlocked) {
                // Password prompt (Zero hints, masked input)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🔐 Restricted Verification",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Store owner & manager verification",
                        fontSize = 12.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = {
                            passwordInput = it
                            passwordError = false
                        },
                        label = { Text("Security PIN") },
                        placeholder = { Text("••••••") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        isError = passwordError,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_pin_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    if (passwordError) {
                        Text(
                            text = "❌ Invalid PIN. Please try again.",
                            color = StoreRed,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val success = onUnlock(passwordInput)
                            if (!success) {
                                passwordError = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("admin_login_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Verify & Continue", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                // Admin Dashboard Tabs
                var selectedTab by remember { mutableIntStateOf(0) }
                val tabs = listOf(
                    "Products (${products.size})",
                    "Coupons (${coupons.size})",
                    "Orders (${orders.size})",
                    "Security 🔒"
                )

                SecondaryTabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                when (selectedTab) {
                    0 -> AdminProductsTab(
                        products = products,
                        onAddProduct = onAddProduct,
                        onUpdateProduct = onUpdateProduct,
                        onDeleteProduct = onDeleteProduct
                    )
                    1 -> AdminCouponsTab(
                        coupons = coupons,
                        onAddCoupon = onAddCoupon,
                        onToggleCouponActive = onToggleCouponActive,
                        onDeleteCoupon = onDeleteCoupon
                    )
                    2 -> AdminOrdersTab(
                        orders = orders,
                        onUpdateOrderStatus = onUpdateOrderStatus
                    )
                    3 -> AdminSecurityTab(
                        onUpdatePin = onUpdatePin,
                        onLock = onLock
                    )
                }
            }
        }
    }
}

@Composable
fun AdminSecurityTab(
    onUpdatePin: (String) -> Boolean,
    onLock: () -> Unit
) {
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "🔒 Set Private Secret PIN",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = NavyPrimary
                )
                Text(
                    text = "Create a custom secret PIN that only you know. The admin panel is completely hidden from the public and unlocks only with your private PIN.",
                    fontSize = 12.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(vertical = 6.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = newPin,
                    onValueChange = {
                        newPin = it
                        errorMessage = null
                        successMessage = null
                    },
                    label = { Text("New PIN") },
                    placeholder = { Text("Enter 4-8 characters") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = {
                        confirmPin = it
                        errorMessage = null
                        successMessage = null
                    },
                    label = { Text("Confirm New PIN") },
                    placeholder = { Text("Re-enter new PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = StoreRed,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                if (successMessage != null) {
                    Text(
                        text = successMessage ?: "",
                        color = StoreGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (newPin.length < 4) {
                            errorMessage = "PIN must be at least 4 characters."
                        } else if (newPin != confirmPin) {
                            errorMessage = "PINs do not match."
                        } else {
                            val success = onUpdatePin(newPin)
                            if (success) {
                                successMessage = "✅ Secret PIN successfully updated!"
                                errorMessage = null
                                newPin = ""
                                confirmPin = ""
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("Save Secret PIN", fontWeight = FontWeight.Bold)
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("🤫 Hidden Panel Access", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    text = "• The Admin option is removed from all customer menus.\n• To access: Tap the top store title ('V K Traders & Hardware') 5 times rapidly.\n• Enter your secret PIN to unlock.",
                    fontSize = 12.sp,
                    color = TextMuted,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(top = 6.dp, bottom = 12.dp)
                )

                OutlinedButton(
                    onClick = onLock,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("🔒 Lock Dashboard Now")
                }
            }
        }
    }
}

@Composable
fun AdminProductsTab(
    products: List<ProductEntity>,
    onAddProduct: (ProductEntity) -> Unit,
    onUpdateProduct: (ProductEntity) -> Unit,
    onDeleteProduct: (String) -> Unit
) {
    var isAdding by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val filtered = remember(products, searchQuery) {
        if (searchQuery.isBlank()) products else products.filter {
            it.name.contains(searchQuery, ignoreCase = true) || it.category.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search products...", fontSize = 12.sp) },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                shape = RoundedCornerShape(8.dp)
            )

            Button(
                onClick = { isAdding = true },
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filtered, key = { it.id }) { product ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(product.emoji, fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(product.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                text = "${product.category} • ₹${product.price.toInt()}${if (product.discount > 0) " (${product.discount}% off)" else ""} • ${if (product.stock) "✅ In Stock" else "⛔ Out of Stock"}",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }

                        IconButton(onClick = { editingProduct = product }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = NavyPrimary, modifier = Modifier.size(18.dp))
                        }

                        IconButton(onClick = { onDeleteProduct(product.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = StoreRed, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }

    if (isAdding) {
        ProductFormDialog(
            initial = null,
            onDismiss = { isAdding = false },
            onSave = {
                onAddProduct(it)
                isAdding = false
            }
        )
    }

    if (editingProduct != null) {
        ProductFormDialog(
            initial = editingProduct,
            onDismiss = { editingProduct = null },
            onSave = {
                onUpdateProduct(it)
                editingProduct = null
            }
        )
    }
}

@Composable
fun ProductFormDialog(
    initial: ProductEntity?,
    onDismiss: () -> Unit,
    onSave: (ProductEntity) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var price by remember { mutableStateOf(initial?.price?.toInt()?.toString() ?: "") }
    var category by remember { mutableStateOf(initial?.category ?: "Hardware") }
    var emoji by remember { mutableStateOf(initial?.emoji ?: "🔧") }
    var discount by remember { mutableStateOf(initial?.discount?.toString() ?: "0") }
    var featured by remember { mutableStateOf(initial?.featured ?: false) }
    var popular by remember { mutableStateOf(initial?.popular ?: false) }
    var inStock by remember { mutableStateOf(initial?.stock ?: true) }
    var description by remember { mutableStateOf(initial?.description ?: "") }

    val blockedWords = listOf("electric", "electrical", "wire", "wiring", "switchboard", "mcb", "fan", "bulb", "led")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "➕ Add Product" else "✏️ Edit Product", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Price (₹) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = discount,
                        onValueChange = { discount = it.filter { ch -> ch.isDigit() }.take(2) },
                        label = { Text("Discount %") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category *") },
                        placeholder = { Text("Hardware, Plumbing, etc.") },
                        modifier = Modifier.weight(2f)
                    )
                    OutlinedTextField(
                        value = emoji,
                        onValueChange = { emoji = it },
                        label = { Text("Emoji") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = inStock, onCheckedChange = { inStock = it })
                    Text("✅ In Stock", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Checkbox(checked = featured, onCheckedChange = { featured = it })
                    Text("⭐ Featured", fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = popular, onCheckedChange = { popular = it })
                    Text("🔥 Popular", fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank() || price.isBlank() || category.isBlank()) {
                        Toast.makeText(context, "Please fill required fields", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (blockedWords.any { (name + " " + category).lowercase().contains(it) }) {
                        Toast.makeText(context, "Electrical items are restricted in this catalog", Toast.LENGTH_LONG).show()
                        return@Button
                    }
                    val prod = ProductEntity(
                        id = initial?.id ?: "prod_${UUID.randomUUID().toString().take(6)}",
                        name = name.trim(),
                        price = price.toDoubleOrNull() ?: 0.0,
                        category = category.trim(),
                        emoji = emoji.ifBlank { "🔧" },
                        discount = discount.toIntOrNull() ?: 0,
                        featured = featured,
                        popular = popular,
                        stock = inStock,
                        description = description.trim()
                    )
                    onSave(prod)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AdminCouponsTab(
    coupons: List<CouponEntity>,
    onAddCoupon: (CouponEntity) -> Unit,
    onToggleCouponActive: (CouponEntity) -> Unit,
    onDeleteCoupon: (String) -> Unit
) {
    var code by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("percent") } // percent or flat
    var value by remember { mutableStateOf("") }
    var minOrder by remember { mutableStateOf("500") }

    Column(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("🎟️ Create New Coupon", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it.uppercase() },
                        label = { Text("Code (e.g. SAVE10)") },
                        modifier = Modifier.weight(1.5f),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters)
                    )
                    OutlinedTextField(
                        value = value,
                        onValueChange = { value = it.filter { ch -> ch.isDigit() } },
                        label = { Text(if (type == "percent") "% Value" else "₹ Value") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = type == "percent", onClick = { type = "percent" })
                        Text("%", fontSize = 12.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = type == "flat", onClick = { type = "flat" })
                        Text("Flat ₹", fontSize = 12.sp)
                    }

                    OutlinedTextField(
                        value = minOrder,
                        onValueChange = { minOrder = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Min Order ₹") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        if (code.isNotBlank() && value.isNotBlank()) {
                            onAddCoupon(
                                CouponEntity(
                                    id = "coup_${UUID.randomUUID().toString().take(6)}",
                                    code = code.trim().uppercase(),
                                    type = type,
                                    value = value.toDoubleOrNull() ?: 0.0,
                                    minOrder = minOrder.toDoubleOrNull() ?: 500.0,
                                    active = true
                                )
                            )
                            code = ""
                            value = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("➕ Add Coupon", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(coupons, key = { it.id }) { c ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("🎟️ ${c.code}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                text = "${if (c.type == "flat") "₹${c.value.toInt()} OFF" else "${c.value.toInt()}% OFF"} • Min. ₹${c.minOrder.toInt()} • ${if (c.active) "Active" else "Inactive"}",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }

                        Switch(
                            checked = c.active,
                            onCheckedChange = { onToggleCouponActive(c) }
                        )

                        IconButton(onClick = { onDeleteCoupon(c.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = StoreRed, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrdersTab(
    orders: List<OrderEntity>,
    onUpdateOrderStatus: (String, String) -> Unit
) {
    val statuses = listOf("Pending", "Confirmed", "Ready", "Delivered", "Cancelled")

    if (orders.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No customer orders recorded yet.", color = TextMuted)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(orders, key = { it.id }) { order ->
                var expanded by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(order.invoiceNo, fontWeight = FontWeight.Black, fontSize = 14.sp, color = NavyPrimary)
                                Text("${order.customerName} • 📞 ${order.customerPhone}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Text("₹${order.total.toInt()}", fontWeight = FontWeight.Black, fontSize = 16.sp)
                        }

                        Text(
                            text = "${if (order.orderType == "delivery") "🏠 Home Delivery: ${order.customerAddress}" else "🏪 Self Pickup"}",
                            fontSize = 11.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Text(
                            text = order.itemsSummary,
                            fontSize = 11.sp,
                            maxLines = 2,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // Status Selector Dropdown
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Status:", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = !expanded },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = order.status,
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                )

                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    statuses.forEach { st ->
                                        DropdownMenuItem(
                                            text = { Text(st) },
                                            onClick = {
                                                expanded = false
                                                onUpdateOrderStatus(order.id, st)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
