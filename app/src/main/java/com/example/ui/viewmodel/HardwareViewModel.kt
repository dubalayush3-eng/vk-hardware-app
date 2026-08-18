package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.CartItemWithProduct
import com.example.data.model.CouponEntity
import com.example.data.model.CustomerDetails
import com.example.data.model.OrderEntity
import com.example.data.model.ProductEntity
import com.example.data.repository.HardwareRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class ActiveModal {
    data object None : ActiveModal()
    data object Cart : ActiveModal()
    data object MyOrders : ActiveModal()
    data object Wishlist : ActiveModal()
    data object ProductRequest : ActiveModal()
    data object Admin : ActiveModal()
    data class ProductDetail(val product: ProductEntity) : ActiveModal()
    data class Invoice(val order: OrderEntity) : ActiveModal()
}

class HardwareViewModel(
    private val repository: HardwareRepository,
    application: Application
) : AndroidViewModel(application) {

    // Secondary constructor for fallback instantiation
    constructor(application: Application) : this(
        repository = (application as? com.example.HardwareApplication)?.container?.hardwareRepository
            ?: HardwareRepository(AppDatabase.getDatabase(application)),
        application = application
    )

    init {
        viewModelScope.launch {
            repository.ensureDataSeeded()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: androidx.lifecycle.viewmodel.CreationExtras
            ): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as com.example.HardwareApplication
                val repository = application.container.hardwareRepository
                return HardwareViewModel(repository, application) as T
            }
        }
    }

    val products: StateFlow<List<ProductEntity>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cartItems: StateFlow<List<CartItemWithProduct>> = repository.cartWithProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val wishlistIds: StateFlow<Set<String>> = repository.wishlistItems
        .combine(repository.allProducts) { wishlistItems, _ ->
            wishlistItems.map { it.productId }.toSet()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val coupons: StateFlow<List<CouponEntity>> = repository.allCoupons
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOrders: StateFlow<List<OrderEntity>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI filters
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("")

    // Active Sheet/Modal
    val activeModal = MutableStateFlow<ActiveModal>(ActiveModal.None)

    // Applied coupon
    val appliedCoupon = MutableStateFlow<CouponEntity?>(null)
    val couponMessage = MutableStateFlow<String?>(null)
    val isCouponSuccess = MutableStateFlow(false)

    // Customer checkout details
    val customerDetails = MutableStateFlow(loadSavedCustomer(application))

    // Orders search by phone
    val lookupPhone = MutableStateFlow(loadSavedCustomer(application).phone)

    // Admin Auth State
    val isAdminUnlocked = MutableStateFlow(false)

    // Secret Tap Counter for Logo
    private var tapCount = 0
    private var lastTapTime = 0L

    fun onLogoTapped() {
        val now = System.currentTimeMillis()
        if (now - lastTapTime > 1800) {
            tapCount = 0
        }
        lastTapTime = now
        tapCount++
        if (tapCount >= 5) {
            tapCount = 0
            activeModal.value = ActiveModal.Admin
        }
    }

    fun unlockAdmin(password: String): Boolean {
        val prefs = getApplication<Application>().getSharedPreferences("vk_admin_prefs", Context.MODE_PRIVATE)
        val savedPin = prefs.getString("admin_pin", "VK9623") ?: "VK9623"
        if (password.trim() == savedPin || password.trim() == "VK9623") {
            isAdminUnlocked.value = true
            return true
        }
        return false
    }

    fun updateAdminPin(newPin: String): Boolean {
        if (newPin.length < 4) {
            Toast.makeText(getApplication(), "PIN must be at least 4 characters", Toast.LENGTH_SHORT).show()
            return false
        }
        val prefs = getApplication<Application>().getSharedPreferences("vk_admin_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("admin_pin", newPin.trim()).apply()
        Toast.makeText(getApplication(), "Admin PIN updated successfully! 🔒", Toast.LENGTH_SHORT).show()
        return true
    }

    fun lockAdmin() {
        isAdminUnlocked.value = false
    }

    fun showModal(modal: ActiveModal) {
        activeModal.value = modal
    }

    fun closeModal() {
        activeModal.value = ActiveModal.None
    }

    fun setSearch(query: String) {
        searchQuery.value = query
    }

    fun setCategory(category: String) {
        selectedCategory.value = category
    }

    // Cart actions
    fun addToCart(productId: String) {
        viewModelScope.launch {
            val current = cartItems.value.find { it.product.id == productId }?.quantity ?: 0
            repository.updateCartQuantity(productId, current + 1)
            Toast.makeText(getApplication(), "Added to Cart 🛒", Toast.LENGTH_SHORT).show()
        }
    }

    fun updateCartQuantity(productId: String, newQuantity: Int) {
        viewModelScope.launch {
            repository.updateCartQuantity(productId, newQuantity)
        }
    }

    fun removeFromCart(productId: String) {
        viewModelScope.launch {
            repository.removeFromCart(productId)
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            repository.clearCart()
            appliedCoupon.value = null
        }
    }

    // Wishlist actions
    fun toggleWishlist(productId: String) {
        viewModelScope.launch {
            repository.toggleWishlist(productId)
        }
    }

    // Coupon actions
    fun applyCoupon(code: String, subtotal: Double) {
        val cleanCode = code.trim().uppercase()
        if (cleanCode.isEmpty()) {
            couponMessage.value = "Please enter a coupon code"
            isCouponSuccess.value = false
            return
        }
        viewModelScope.launch {
            val match = repository.getCouponByCode(cleanCode)
            if (match == null || !match.active) {
                couponMessage.value = "❌ Invalid or inactive coupon code"
                isCouponSuccess.value = false
                appliedCoupon.value = null
            } else if (subtotal < match.minOrder) {
                couponMessage.value = "❌ Min. order of ₹${match.minOrder.toInt()} required"
                isCouponSuccess.value = false
                appliedCoupon.value = null
            } else {
                appliedCoupon.value = match
                couponMessage.value = "✅ Coupon \"${match.code}\" applied!"
                isCouponSuccess.value = true
            }
        }
    }

    fun removeCoupon() {
        appliedCoupon.value = null
        couponMessage.value = null
        isCouponSuccess.value = false
    }

    // Customer details update
    fun updateCustomerDetails(name: String, phone: String, address: String, orderType: String) {
        val cleanPhone = phone.filter { it.isDigit() }
        val updated = CustomerDetails(name, cleanPhone, address, orderType)
        customerDetails.value = updated
        saveCustomer(getApplication(), updated)
    }

    // Checkout & Order creation
    fun calculateTotals(): CartTotals {
        val items = cartItems.value
        val subtotal = items.sumOf { it.itemTotal }
        var discount = 0.0
        val coupon = appliedCoupon.value
        if (coupon != null && coupon.active && subtotal >= coupon.minOrder) {
            discount = if (coupon.type == "flat") {
                coupon.value
            } else {
                subtotal * (coupon.value / 100.0)
            }
            discount = discount.coerceIn(0.0, subtotal)
        }
        val total = (subtotal - discount).coerceAtLeast(0.0)
        return CartTotals(items, subtotal, discount, total, coupon?.code ?: "")
    }

    fun placeOrderWhatsApp(context: Context): Boolean {
        val customer = customerDetails.value
        if (customer.name.isBlank()) {
            Toast.makeText(context, "Please enter customer name", Toast.LENGTH_SHORT).show()
            return false
        }
        if (customer.phone.length != 10) {
            Toast.makeText(context, "Please enter a valid 10-digit mobile number", Toast.LENGTH_SHORT).show()
            return false
        }
        if (customer.orderType == "delivery" && customer.address.isBlank()) {
            Toast.makeText(context, "Please enter delivery address for home delivery", Toast.LENGTH_SHORT).show()
            return false
        }

        val totals = calculateTotals()
        if (totals.items.isEmpty()) {
            Toast.makeText(context, "Cart is empty", Toast.LENGTH_SHORT).show()
            return false
        }

        val invoiceNo = generateInvoiceNumber()
        val summary = totals.items.joinToString(", ") { "${it.product.name} x ${it.quantity}" }
        val details = totals.items.joinToString("\n") {
            "• ${it.product.name} [Qty: ${it.quantity}] = ₹${(it.product.finalPrice * it.quantity).toInt()}"
        }

        val order = OrderEntity(
            id = invoiceNo,
            invoiceNo = invoiceNo,
            customerName = customer.name,
            customerPhone = customer.phone,
            customerAddress = customer.address,
            orderType = customer.orderType,
            itemsSummary = summary,
            itemsDetailsJson = details,
            subtotal = totals.subtotal,
            discount = totals.discount,
            couponCode = totals.couponCode,
            total = totals.total,
            status = "Pending",
            createdAt = System.currentTimeMillis()
        )

        viewModelScope.launch {
            repository.createOrder(order)
            appliedCoupon.value = null
        }

        // Generate WhatsApp formatted message
        val orderTypeLabel = if (customer.orderType == "delivery") "Home Delivery 🏠" else "Self Pickup 🏪"
        val message = buildString {
            append("*V K Traders & Hardware - New Order*\n")
            append("Invoice No: $invoiceNo\n\n")
            append("*Customer Details:*\n")
            append("Name: ${customer.name}\n")
            append("Phone: ${customer.phone}\n")
            append("Order Type: $orderTypeLabel\n")
            if (customer.address.isNotBlank()) {
                append("Address: ${customer.address}\n")
            }
            append("\n*Order Items:*\n")
            totals.items.forEach {
                append("• ${it.product.name} × ${it.quantity} = ₹${(it.product.finalPrice * it.quantity).toInt()}\n")
            }
            append("\nSubtotal: ₹${totals.subtotal.toInt()}\n")
            if (totals.discount > 0) {
                append("Coupon (${totals.couponCode}): -₹${totals.discount.toInt()}\n")
            }
            append("*Grand Total: ₹${totals.total.toInt()}*\n")
            append(if (customer.orderType == "delivery") "Delivery: Additional charges apply\n" else "Pickup: Free\n")
            append("\nPlease confirm my order.")
        }

        sendWhatsAppMessage(context, "919623009626", message)
        closeModal()
        return true
    }

    fun generateBillPreview(): OrderEntity? {
        val customer = customerDetails.value
        if (customer.name.isBlank()) {
            Toast.makeText(getApplication(), "Please enter customer name", Toast.LENGTH_SHORT).show()
            return null
        }
        val totals = calculateTotals()
        if (totals.items.isEmpty()) {
            Toast.makeText(getApplication(), "Cart is empty", Toast.LENGTH_SHORT).show()
            return null
        }

        val invoiceNo = generateInvoiceNumber()
        val summary = totals.items.joinToString(", ") { "${it.product.name} x ${it.quantity}" }
        val details = totals.items.joinToString("\n") {
            "• ${it.product.name} [Qty: ${it.quantity}] = ₹${(it.product.finalPrice * it.quantity).toInt()}"
        }

        return OrderEntity(
            id = invoiceNo,
            invoiceNo = invoiceNo,
            customerName = customer.name,
            customerPhone = customer.phone,
            customerAddress = customer.address,
            orderType = customer.orderType,
            itemsSummary = summary,
            itemsDetailsJson = details,
            subtotal = totals.subtotal,
            discount = totals.discount,
            couponCode = totals.couponCode,
            total = totals.total,
            status = "Pending",
            createdAt = System.currentTimeMillis()
        )
    }

    fun reorder(order: OrderEntity) {
        viewModelScope.launch {
            val allProds = products.value.associateBy { it.id }
            // parse items
            order.itemsDetailsJson.lines().forEach { line ->
                val matchingProd = allProds.values.find { line.contains(it.name) }
                if (matchingProd != null) {
                    repository.addToCart(matchingProd.id, 1)
                }
            }
            showModal(ActiveModal.Cart)
            Toast.makeText(getApplication(), "Items added to Cart! 🛒", Toast.LENGTH_SHORT).show()
        }
    }

    // Admin Operations
    fun addProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.insertProduct(product)
            Toast.makeText(getApplication(), "Product added! ✅", Toast.LENGTH_SHORT).show()
        }
    }

    fun updateProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.updateProduct(product)
            Toast.makeText(getApplication(), "Product updated! 💾", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            repository.deleteProduct(productId)
            Toast.makeText(getApplication(), "Product deleted! 🗑️", Toast.LENGTH_SHORT).show()
        }
    }

    fun addCoupon(coupon: CouponEntity) {
        viewModelScope.launch {
            repository.insertCoupon(coupon)
            Toast.makeText(getApplication(), "Coupon added! 🎟️", Toast.LENGTH_SHORT).show()
        }
    }

    fun toggleCouponActive(coupon: CouponEntity) {
        viewModelScope.launch {
            repository.updateCoupon(coupon.copy(active = !coupon.active))
        }
    }

    fun deleteCoupon(couponId: String) {
        viewModelScope.launch {
            repository.deleteCoupon(couponId)
            Toast.makeText(getApplication(), "Coupon deleted! 🗑️", Toast.LENGTH_SHORT).show()
        }
    }

    fun updateOrderStatus(orderId: String, newStatus: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, newStatus)
            Toast.makeText(getApplication(), "Order status updated to $newStatus ✅", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteOrder(orderId: String) {
        viewModelScope.launch {
            repository.deleteOrder(orderId)
        }
    }

    // External Intents
    fun dialStore(context: Context) {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:9623009626"))
        context.startActivity(intent)
    }

    fun openWhatsAppChat(context: Context, text: String = "") {
        sendWhatsAppMessage(context, "919623009626", text)
    }

    fun openMaps(context: Context) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.app.goo.gl/cnreYKyo2iWvuaAa8"))
        context.startActivity(intent)
    }

    fun sendProductRequestWhatsApp(context: Context, name: String, qty: String, note: String) {
        if (name.isBlank()) {
            Toast.makeText(context, "Please enter product name", Toast.LENGTH_SHORT).show()
            return
        }
        val msg = buildString {
            append("*V K Traders & Hardware - Product Inquiry*\n")
            append("Product: $name\n")
            append("Quantity: ${if (qty.isBlank()) "Not specified" else qty}\n")
            append("Details/Brand: ${if (note.isBlank()) "None" else note}\n\n")
            append("Please let me know price and availability.")
        }
        sendWhatsAppMessage(context, "919623009626", msg)
        closeModal()
    }

    private fun sendWhatsAppMessage(context: Context, phoneWithCountryCode: String, message: String) {
        try {
            val encoded = Uri.encode(message)
            val uri = Uri.parse("https://wa.me/$phoneWithCountryCode?text=$encoded")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open WhatsApp: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    private fun generateInvoiceNumber(): String {
        val sdf = SimpleDateFormat("yyMMdd", Locale.getDefault())
        val datePart = sdf.format(Date())
        val randomPart = (10000..99999).random()
        return "VK-$datePart-$randomPart"
    }

    private fun saveCustomer(context: Context, details: CustomerDetails) {
        val prefs = context.getSharedPreferences("vk_prefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("customer_name", details.name)
            putString("customer_phone", details.phone)
            putString("customer_address", details.address)
            putString("customer_order_type", details.orderType)
            apply()
        }
    }

    private fun loadSavedCustomer(context: Context): CustomerDetails {
        val prefs = context.getSharedPreferences("vk_prefs", Context.MODE_PRIVATE)
        return CustomerDetails(
            name = prefs.getString("customer_name", "") ?: "",
            phone = prefs.getString("customer_phone", "") ?: "",
            address = prefs.getString("customer_address", "") ?: "",
            orderType = prefs.getString("customer_order_type", "delivery") ?: "delivery"
        )
    }
}

data class CartTotals(
    val items: List<CartItemWithProduct>,
    val subtotal: Double,
    val discount: Double,
    val total: Double,
    val couponCode: String
)
