package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Database Entity for storing Hardware, Plumbing, and Sanitary products.
 */
@Entity(tableName = "products")
data class Product(
    @PrimaryKey val id: String,
    val name: String,
    val category: String, // "Hardware", "Plumbing", "Sanitary", "Tools", "Construction"
    val price: Double,
    val stock: Boolean = true, // Stock status
    val emoji: String = "🔧",
    val photo: String = "",
    val photo2: String = "",
    val featured: Boolean = false,
    val popular: Boolean = false,
    val discount: Int = 0,
    val description: String = ""
) {
    val finalPrice: Double
        get() {
            val d = discount.coerceIn(0, 90)
            return (price * (1.0 - d / 100.0)).coerceAtLeast(0.0)
        }
}

typealias ProductEntity = Product

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey val productId: String,
    val quantity: Int
)

@Entity(tableName = "wishlist_items")
data class WishlistItemEntity(
    @PrimaryKey val productId: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "coupons")
data class CouponEntity(
    @PrimaryKey val id: String,
    val code: String,
    val type: String, // "percent" or "flat"
    val value: Double,
    val minOrder: Double = 500.0,
    val active: Boolean = true
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: String,
    val invoiceNo: String,
    val customerName: String,
    val customerPhone: String,
    val customerAddress: String,
    val orderType: String, // "delivery" or "pickup"
    val itemsSummary: String, // e.g. "PVC Pipe 1 inch x 2, Ball Valve x 1"
    val itemsDetailsJson: String, // simple formatted breakdown
    val subtotal: Double,
    val discount: Double,
    val couponCode: String = "",
    val total: Double,
    val status: String = "Pending", // "Pending", "Confirmed", "Ready", "Delivered", "Cancelled"
    val createdAt: Long = System.currentTimeMillis()
)

data class CartItemWithProduct(
    val product: ProductEntity,
    val quantity: Int
) {
    val itemTotal: Double
        get() = product.finalPrice * quantity
}

data class CustomerDetails(
    val name: String = "",
    val phone: String = "",
    val address: String = "",
    val orderType: String = "delivery" // "delivery" or "pickup"
)
