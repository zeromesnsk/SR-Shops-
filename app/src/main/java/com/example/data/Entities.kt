package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val email: String,
    val username: String,
    val passwordHash: String,
    val phone: String = "",
    val address: String = "",
    val profileImage: String = ""
) : Serializable

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val price: Double,
    val originalPrice: Double,
    val category: String,
    val rating: Float,
    val reviewCount: Int,
    val stock: Int,
    val imageUrl: String,
    val isFlashSale: Boolean = false,
    val flashSaleDiscount: Int = 0 // percent e.g. 15 for 15% off
) : Serializable

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productId: String,
    val selectQuantity: Int,
    val userEmail: String
) : Serializable

@Entity(tableName = "wishlist_items")
data class WishlistItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productId: String,
    val userEmail: String
) : Serializable

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val orderId: String,
    val userEmail: String,
    val itemsSummary: String, // format: "Product Name xQty, Product 2 xQty"
    val totalPrice: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "Processing", // Processing, Shipped, Delivered
    val shippingAddress: String = ""
) : Serializable

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productId: String,
    val reviewerName: String,
    val rating: Float,
    val comment: String,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable
