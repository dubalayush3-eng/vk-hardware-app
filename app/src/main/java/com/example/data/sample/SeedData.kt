package com.example.data.sample

import com.example.data.model.CouponEntity
import com.example.data.model.ProductEntity

object SeedData {
    val initialProducts = listOf(
        ProductEntity(
            id = "prod_1",
            name = "CPVC Pipe 1 Inch (3 Meter)",
            price = 450.0,
            category = "Plumbing",
            emoji = "🚰",
            featured = true,
            popular = true,
            stock = true,
            discount = 10,
            description = "High quality SDR 11 CPVC pipe for hot and cold potable water distribution."
        ),
        ProductEntity(
            id = "prod_2",
            name = "Brass Ball Valve 1/2 Inch",
            price = 280.0,
            category = "Plumbing",
            emoji = "🔩",
            featured = true,
            popular = true,
            stock = true,
            discount = 5,
            description = "Heavy duty forged brass ball valve with chrome plating and rust resistant lever."
        ),
        ProductEntity(
            id = "prod_3",
            name = "Stainless Steel Tap (Bib Cock)",
            price = 390.0,
            category = "Sanitary",
            emoji = "🚿",
            featured = true,
            popular = false,
            stock = true,
            discount = 12,
            description = "Durable SS 304 long body tap with quarter turn ceramic disc cartridge."
        ),
        ProductEntity(
            id = "prod_4",
            name = "Claw Hammer with Rubber Grip (500g)",
            price = 320.0,
            category = "Tools",
            emoji = "🔨",
            featured = false,
            popular = true,
            stock = true,
            discount = 8,
            description = "Drop-forged carbon steel head with anti-slip ergonomic rubber handle."
        ),
        ProductEntity(
            id = "prod_5",
            name = "Adjustable Pipe Wrench 12 Inch",
            price = 480.0,
            category = "Tools",
            emoji = "🔧",
            featured = true,
            popular = true,
            stock = true,
            discount = 15,
            description = "Heavy-duty cast iron body with induction-hardened forged jaws for plumbing grip."
        ),
        ProductEntity(
            id = "prod_6",
            name = "UPVC Solvent Cement 250ml",
            price = 140.0,
            category = "Plumbing",
            emoji = "🧪",
            featured = false,
            popular = true,
            stock = true,
            discount = 0,
            description = "Fast setting high strength solvent cement for UPVC pipe joints."
        ),
        ProductEntity(
            id = "prod_7",
            name = "Stainless Steel Door Hinges 4 Inch (Pair)",
            price = 160.0,
            category = "Hardware",
            emoji = "🚪",
            featured = false,
            popular = false,
            stock = true,
            discount = 10,
            description = "Rust-proof SS bearing butt hinges for wooden and metal doors."
        ),
        ProductEntity(
            id = "prod_8",
            name = "Brass Mortise Door Lock with Handle",
            price = 1450.0,
            category = "Hardware",
            emoji = "🔐",
            featured = true,
            popular = true,
            stock = true,
            discount = 15,
            description = "Premium double-throw brass lock mechanism with stylish zinc alloy handles and 3 keys."
        ),
        ProductEntity(
            id = "prod_9",
            name = "Teflon Thread Seal Tape (Box of 10)",
            price = 180.0,
            category = "Plumbing",
            emoji = "🧵",
            featured = false,
            popular = true,
            stock = true,
            discount = 5,
            description = "Professional 12mm x 10m PTFE thread sealing tape for leak-proof pipe joints."
        ),
        ProductEntity(
            id = "prod_10",
            name = "Overhead Shower Rose 4 Inch SS",
            price = 340.0,
            category = "Sanitary",
            emoji = "🚿",
            featured = false,
            popular = true,
            stock = true,
            discount = 10,
            description = "Chrome-finished stainless steel rain shower head with anti-clog silicone nozzles."
        ),
        ProductEntity(
            id = "prod_11",
            name = "Steel Measuring Tape 5 Meter",
            price = 150.0,
            category = "Tools",
            emoji = "📏",
            featured = false,
            popular = false,
            stock = true,
            discount = 0,
            description = "Impact-resistant rubberized casing with clear metric & inch dual markings and auto-lock."
        ),
        ProductEntity(
            id = "prod_12",
            name = "Waterproof Tile Adhesive 20kg",
            price = 420.0,
            category = "Construction",
            emoji = "🧱",
            featured = true,
            popular = false,
            stock = true,
            discount = 5,
            description = "Polymer modified cementitious tile adhesive for fixing ceramic & vitrified tiles."
        ),
        ProductEntity(
            id = "prod_13",
            name = "Heavy Duty Combination Plier 8 Inch",
            price = 260.0,
            category = "Tools",
            emoji = "🛠️",
            featured = false,
            popular = true,
            stock = true,
            discount = 8,
            description = "High grade alloy steel plier with insulated double-color soft grip."
        ),
        ProductEntity(
            id = "prod_14",
            name = "Angle Cock with Flange (Chrome Plated)",
            price = 290.0,
            category = "Sanitary",
            emoji = "🚰",
            featured = true,
            popular = false,
            stock = true,
            discount = 10,
            description = "Brass quarter-turn angle valve with wall flange for geysers and health faucets."
        ),
        ProductEntity(
            id = "prod_15",
            name = "Galvanized GI Wire 16 Gauge (1kg)",
            price = 110.0,
            category = "Hardware",
            emoji = "⛓️",
            featured = false,
            popular = false,
            stock = true,
            discount = 0,
            description = "High tensile zinc-coated binding wire for construction and general purpose use."
        ),
        ProductEntity(
            id = "prod_16",
            name = "Hacksaw Frame with Bi-Metal Blade 12\"",
            price = 220.0,
            category = "Tools",
            emoji = "🪚",
            featured = false,
            popular = false,
            stock = true,
            discount = 10,
            description = "Sturdy steel tube frame with tension wing-nut and 24 TPI metal cutting blade."
        )
    )

    val initialCoupons = listOf(
        CouponEntity(
            id = "coup_1",
            code = "SAVE10",
            type = "percent",
            value = 10.0,
            minOrder = 500.0,
            active = true
        ),
        CouponEntity(
            id = "coup_2",
            code = "VK100",
            type = "flat",
            value = 100.0,
            minOrder = 1000.0,
            active = true
        ),
        CouponEntity(
            id = "coup_3",
            code = "WELCOME50",
            type = "flat",
            value = 50.0,
            minOrder = 400.0,
            active = true
        )
    )
}
