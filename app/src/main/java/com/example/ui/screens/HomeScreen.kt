package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProductEntity
import com.example.ui.components.HeroBanner
import com.example.ui.components.InvoiceView
import com.example.ui.components.ProductCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.StoreHeader
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.StoreGreen
import com.example.ui.theme.StoreRed
import com.example.ui.theme.TextMuted
import com.example.ui.viewmodel.ActiveModal
import com.example.ui.viewmodel.HardwareViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HardwareViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val products by viewModel.products.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val wishlistIds by viewModel.wishlistIds.collectAsState()
    val coupons by viewModel.coupons.collectAsState()
    val allOrders by viewModel.allOrders.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val activeModal by viewModel.activeModal.collectAsState()
    val appliedCoupon by viewModel.appliedCoupon.collectAsState()
    val couponMessage by viewModel.couponMessage.collectAsState()
    val isCouponSuccess by viewModel.isCouponSuccess.collectAsState()
    val customerDetails by viewModel.customerDetails.collectAsState()
    val isAdminUnlocked by viewModel.isAdminUnlocked.collectAsState()
    val lookupPhone by viewModel.lookupPhone.collectAsState()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    BackHandler(enabled = activeModal !is ActiveModal.None) {
        viewModel.closeModal()
    }

    val categories = listOf("All", "Hardware", "Plumbing", "Sanitary", "Tools", "Construction")

    val filteredProducts = products.filter { product ->
        val matchesSearch = searchQuery.isBlank() ||
                product.name.contains(searchQuery, ignoreCase = true) ||
                product.category.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategory.isBlank() || selectedCategory == "All" ||
                product.category.equals(selectedCategory, ignoreCase = true)
        matchesSearch && matchesCategory
    }

    val featuredProducts = products.filter { it.featured && it.stock }.take(4)
    val popularProducts = products.filter { it.popular && it.stock }.take(4)

    val totals = viewModel.calculateTotals()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            StoreHeader(
                cartCount = cartItems.sumOf { it.quantity },
                onLogoTapped = { viewModel.onLogoTapped() },
                onOpenCart = { viewModel.showModal(ActiveModal.Cart) },
                onOpenOrders = { viewModel.showModal(ActiveModal.MyOrders) },
                onOpenWishlist = { viewModel.showModal(ActiveModal.Wishlist) },
                onOpenRequest = { viewModel.showModal(ActiveModal.ProductRequest) },
                onOpenAdmin = { viewModel.showModal(ActiveModal.Admin) },
                onQuickCall = { viewModel.dialStore(context) },
                onGetDirections = { viewModel.openMaps(context) }
            )
        },
        bottomBar = {
            // Sticky quick contact action bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { viewModel.dialStore(context) },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("bottom_call_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("📞 Call Store", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { viewModel.openWhatsAppChat(context, "Hello V K Traders, I would like to inquire about products.") },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("bottom_whatsapp_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = StoreGreen),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("💬 WhatsApp", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Hero Banner
            item(span = { GridItemSpan(2) }) {
                HeroBanner(
                    onBrowseClick = { viewModel.setCategory("All") },
                    onDirectionsClick = { viewModel.openMaps(context) },
                    onCallClick = { viewModel.dialStore(context) }
                )
            }

            // 2. Search & Filter Controls
            item(span = { GridItemSpan(2) }) {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    // Search Field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearch(it) },
                        placeholder = { Text("Search hardware, plumbing, tools...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { viewModel.setSearch("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("main_search_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NavyPrimary,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Category Chips & Quick Action Shortcuts
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        categories.forEach { cat ->
                            val isSelected = (selectedCategory == cat) || (selectedCategory.isBlank() && cat == "All")
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setCategory(if (cat == "All") "" else cat) },
                                label = { Text(cat, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NavyPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }

                        // Wishlist shortcut chip
                        OutlinedButton(
                            onClick = { viewModel.showModal(ActiveModal.Wishlist) },
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("❤️ Wishlist (${wishlistIds.size})", fontSize = 12.sp)
                        }

                        // Request shortcut chip
                        OutlinedButton(
                            onClick = { viewModel.showModal(ActiveModal.ProductRequest) },
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("📦 Request Product", fontSize = 12.sp)
                        }
                    }
                }
            }

            // 3. Featured Section (when no active search)
            if (searchQuery.isBlank() && selectedCategory.isBlank() && featuredProducts.isNotEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    SectionHeader(
                        title = "⭐ Featured Products",
                        subtitle = "Top recommended hardware and plumbing items"
                    )
                }

                items(featuredProducts, key = { "feat_${it.id}" }) { prod ->
                    Box(modifier = Modifier.padding(horizontal = 8.dp)) {
                        ProductCard(
                            product = prod,
                            isWishlisted = wishlistIds.contains(prod.id),
                            onProductClick = { viewModel.showModal(ActiveModal.ProductDetail(prod)) },
                            onAddToCart = { viewModel.addToCart(prod.id) },
                            onToggleWishlist = { viewModel.toggleWishlist(prod.id) }
                        )
                    }
                }
            }

            // 4. Popular Section (when no active search)
            if (searchQuery.isBlank() && selectedCategory.isBlank() && popularProducts.isNotEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    SectionHeader(
                        title = "🔥 Popular Products",
                        subtitle = "Frequently purchased tools and fixtures"
                    )
                }

                items(popularProducts, key = { "pop_${it.id}" }) { prod ->
                    Box(modifier = Modifier.padding(horizontal = 8.dp)) {
                        ProductCard(
                            product = prod,
                            isWishlisted = wishlistIds.contains(prod.id),
                            onProductClick = { viewModel.showModal(ActiveModal.ProductDetail(prod)) },
                            onAddToCart = { viewModel.addToCart(prod.id) },
                            onToggleWishlist = { viewModel.toggleWishlist(prod.id) }
                        )
                    }
                }
            }

            // 5. Full Catalog Header
            item(span = { GridItemSpan(2) }) {
                SectionHeader(
                    title = if (selectedCategory.isNotBlank() && selectedCategory != "All") "$selectedCategory Catalog" else "All Products",
                    subtitle = "${filteredProducts.size} items available"
                )
            }

            // 6. Main Products Grid
            if (filteredProducts.isEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🔍", fontSize = 40.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No matching products found", fontWeight = FontWeight.Bold, color = TextMuted)
                            Text("Try searching for pipes, valves, locks, or tools.", fontSize = 12.sp, color = TextMuted)
                        }
                    }
                }
            } else {
                items(filteredProducts, key = { it.id }) { product ->
                    Box(modifier = Modifier.padding(horizontal = 8.dp)) {
                        ProductCard(
                            product = product,
                            isWishlisted = wishlistIds.contains(product.id),
                            onProductClick = { viewModel.showModal(ActiveModal.ProductDetail(product)) },
                            onAddToCart = { viewModel.addToCart(product.id) },
                            onToggleWishlist = { viewModel.toggleWishlist(product.id) }
                        )
                    }
                }
            }

            // 7. Store Business Info & Contact Card
            item(span = { GridItemSpan(2) }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "V K Traders & Hardware",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = NavyPrimary
                        )
                        Text(
                            text = "Hardware, plumbing, sanitary products, construction material and tools with good quality material and home delivery availability in Maharashtra.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        Text("📞 Phone: 9623009626", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text("📍 Address: A/P Retre Karkhana, Shivnagar, Maharashtra", fontSize = 12.sp)
                        Text("🕒 Hours: 9:00 AM – 8:30 PM • Sunday Open • Saturday Closed", fontSize = 12.sp)
                        Text("✉️ Email: dhairyashilduba9626@gmaip.com", fontSize = 12.sp, color = TextMuted)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("© 2026 V K Traders & Hardware. All rights reserved.", fontSize = 10.sp, color = TextMuted)
                    }
                }
            }
        }
    }

    // Modal Bottom Sheets
    when (val modal = activeModal) {
        is ActiveModal.None -> {}
        is ActiveModal.Cart -> {
            ModalBottomSheet(
                onDismissRequest = { viewModel.closeModal() },
                sheetState = sheetState
            ) {
                CartSheet(
                    cartItems = cartItems,
                    customerDetails = customerDetails,
                    appliedCoupon = appliedCoupon,
                    couponMessage = couponMessage,
                    isCouponSuccess = isCouponSuccess,
                    totals = totals,
                    onQuantityChange = { id, qty -> viewModel.updateCartQuantity(id, qty) },
                    onRemoveItem = { viewModel.removeFromCart(it) },
                    onApplyCoupon = { code -> viewModel.applyCoupon(code, totals.subtotal) },
                    onRemoveCoupon = { viewModel.removeCoupon() },
                    onCustomerDetailsChange = { name, phone, address, type ->
                        viewModel.updateCustomerDetails(name, phone, address, type)
                    },
                    onGenerateBill = {
                        val preview = viewModel.generateBillPreview()
                        if (preview != null) {
                            viewModel.showModal(ActiveModal.Invoice(preview))
                        }
                    },
                    onOrderWhatsApp = {
                        viewModel.placeOrderWhatsApp(context)
                    },
                    onClose = { viewModel.closeModal() }
                )
            }
        }
        is ActiveModal.MyOrders -> {
            ModalBottomSheet(
                onDismissRequest = { viewModel.closeModal() },
                sheetState = sheetState
            ) {
                OrdersSheet(
                    allOrders = allOrders,
                    initialPhone = lookupPhone,
                    onReorder = { viewModel.reorder(it) },
                    onViewInvoice = { viewModel.showModal(ActiveModal.Invoice(it)) },
                    onClose = { viewModel.closeModal() }
                )
            }
        }
        is ActiveModal.Wishlist -> {
            val wishlistProducts = products.filter { wishlistIds.contains(it.id) }
            ModalBottomSheet(
                onDismissRequest = { viewModel.closeModal() },
                sheetState = sheetState
            ) {
                WishlistSheet(
                    wishlistProducts = wishlistProducts,
                    onAddToCart = { viewModel.addToCart(it) },
                    onRemoveFromWishlist = { viewModel.toggleWishlist(it) },
                    onClose = { viewModel.closeModal() }
                )
            }
        }
        is ActiveModal.ProductRequest -> {
            ModalBottomSheet(
                onDismissRequest = { viewModel.closeModal() },
                sheetState = sheetState
            ) {
                RequestSheet(
                    onSubmit = { name, qty, note ->
                        viewModel.sendProductRequestWhatsApp(context, name, qty, note)
                    },
                    onClose = { viewModel.closeModal() }
                )
            }
        }
        is ActiveModal.Admin -> {
            ModalBottomSheet(
                onDismissRequest = { viewModel.closeModal() },
                sheetState = sheetState
            ) {
                AdminScreen(
                    isUnlocked = isAdminUnlocked,
                    products = products,
                    coupons = coupons,
                    orders = allOrders,
                    onUnlock = { viewModel.unlockAdmin(it) },
                    onUpdatePin = { viewModel.updateAdminPin(it) },
                    onLock = { viewModel.lockAdmin() },
                    onAddProduct = { viewModel.addProduct(it) },
                    onUpdateProduct = { viewModel.updateProduct(it) },
                    onDeleteProduct = { viewModel.deleteProduct(it) },
                    onAddCoupon = { viewModel.addCoupon(it) },
                    onToggleCouponActive = { viewModel.toggleCouponActive(it) },
                    onDeleteCoupon = { viewModel.deleteCoupon(it) },
                    onUpdateOrderStatus = { id, st -> viewModel.updateOrderStatus(id, st) },
                    onClose = { viewModel.closeModal() }
                )
            }
        }
        is ActiveModal.ProductDetail -> {
            ModalBottomSheet(
                onDismissRequest = { viewModel.closeModal() },
                sheetState = sheetState
            ) {
                ProductDetailSheet(
                    product = modal.product,
                    isWishlisted = wishlistIds.contains(modal.product.id),
                    onAddToCart = {
                        viewModel.addToCart(modal.product.id)
                        viewModel.closeModal()
                    },
                    onToggleWishlist = { viewModel.toggleWishlist(modal.product.id) },
                    onClose = { viewModel.closeModal() }
                )
            }
        }
        is ActiveModal.Invoice -> {
            ModalBottomSheet(
                onDismissRequest = { viewModel.closeModal() },
                sheetState = sheetState
            ) {
                InvoiceView(
                    order = modal.order,
                    onClose = { viewModel.closeModal() },
                    onSendWhatsApp = {
                        viewModel.placeOrderWhatsApp(context)
                    }
                )
            }
        }
    }
}
