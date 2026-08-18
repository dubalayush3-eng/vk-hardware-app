package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.CartItemEntity
import com.example.data.model.CartItemWithProduct
import com.example.data.model.CouponEntity
import com.example.data.model.OrderEntity
import com.example.data.model.ProductEntity
import com.example.data.model.WishlistItemEntity
import com.example.data.sample.SeedData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext

class HardwareRepository(private val database: AppDatabase) {
    private val productDao = database.productDao()
    private val cartDao = database.cartDao()
    private val wishlistDao = database.wishlistDao()
    private val couponDao = database.couponDao()
    private val orderDao = database.orderDao()

    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()
    val allCoupons: Flow<List<CouponEntity>> = couponDao.getAllCoupons()
    val allOrders: Flow<List<OrderEntity>> = orderDao.getAllOrders()
    val wishlistItems: Flow<List<WishlistItemEntity>> = wishlistDao.getWishlistItems()

    val cartWithProducts: Flow<List<CartItemWithProduct>> = combine(
        cartDao.getCartItems(),
        productDao.getAllProducts()
    ) { cartItems, products ->
        val productMap = products.associateBy { it.id }
        cartItems.mapNotNull { cartItem ->
            val product = productMap[cartItem.productId]
            if (product != null && cartItem.quantity > 0) {
                CartItemWithProduct(product, cartItem.quantity)
            } else {
                null
            }
        }
    }

    suspend fun ensureDataSeeded() = withContext(Dispatchers.IO) {
        if (productDao.getProductCount() == 0) {
            productDao.insertProducts(SeedData.initialProducts)
        }
        if (couponDao.getCouponCount() == 0) {
            couponDao.insertCoupons(SeedData.initialCoupons)
        }
    }

    // Product actions
    suspend fun insertProduct(product: ProductEntity) = withContext(Dispatchers.IO) {
        productDao.insertProduct(product)
    }

    suspend fun updateProduct(product: ProductEntity) = withContext(Dispatchers.IO) {
        productDao.updateProduct(product)
    }

    suspend fun deleteProduct(productId: String) = withContext(Dispatchers.IO) {
        productDao.deleteProductById(productId)
        cartDao.deleteCartItem(productId)
        wishlistDao.removeFromWishlist(productId)
    }

    // Cart actions
    suspend fun addToCart(productId: String, delta: Int = 1) = withContext(Dispatchers.IO) {
        val existing = database.cartDao()
        // We can handle add/increment
        cartDao.insertCartItem(CartItemEntity(productId, delta))
    }

    suspend fun updateCartQuantity(productId: String, newQuantity: Int) = withContext(Dispatchers.IO) {
        if (newQuantity <= 0) {
            cartDao.deleteCartItem(productId)
        } else {
            cartDao.insertCartItem(CartItemEntity(productId, newQuantity))
        }
    }

    suspend fun removeFromCart(productId: String) = withContext(Dispatchers.IO) {
        cartDao.deleteCartItem(productId)
    }

    suspend fun clearCart() = withContext(Dispatchers.IO) {
        cartDao.clearCart()
    }

    // Wishlist actions
    suspend fun toggleWishlist(productId: String) = withContext(Dispatchers.IO) {
        if (wishlistDao.isWishlisted(productId)) {
            wishlistDao.removeFromWishlist(productId)
        } else {
            wishlistDao.addToWishlist(WishlistItemEntity(productId))
        }
    }

    // Coupon actions
    suspend fun getCouponByCode(code: String): CouponEntity? = withContext(Dispatchers.IO) {
        couponDao.getActiveCouponByCode(code.trim().uppercase())
    }

    suspend fun insertCoupon(coupon: CouponEntity) = withContext(Dispatchers.IO) {
        couponDao.insertCoupon(coupon.copy(code = coupon.code.trim().uppercase()))
    }

    suspend fun updateCoupon(coupon: CouponEntity) = withContext(Dispatchers.IO) {
        couponDao.updateCoupon(coupon)
    }

    suspend fun deleteCoupon(couponId: String) = withContext(Dispatchers.IO) {
        couponDao.deleteCouponById(couponId)
    }

    // Order actions
    fun getOrdersByPhone(phone: String): Flow<List<OrderEntity>> {
        return orderDao.getOrdersByPhone(phone.trim())
    }

    suspend fun createOrder(order: OrderEntity) = withContext(Dispatchers.IO) {
        orderDao.insertOrder(order)
        cartDao.clearCart()
    }

    suspend fun updateOrderStatus(orderId: String, status: String) = withContext(Dispatchers.IO) {
        orderDao.updateOrderStatus(orderId, status)
    }

    suspend fun deleteOrder(orderId: String) = withContext(Dispatchers.IO) {
        orderDao.deleteOrder(orderId)
    }
}
