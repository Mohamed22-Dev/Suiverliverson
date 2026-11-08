package com.example.suiverliverson

enum class AvatarType {
    MAN, WOMAN, OLD_MAN, YOUNG
}

data class Order(
    val id: String,
    val customerName: String,
    val distanceKm: Double,
    val price: Double,
    val pickup: String,
    val client: String,
    val orderType: String = "Food / طعام",
    val avatarType: AvatarType = AvatarType.MAN
)

data class ActiveTripData(
    val orderId: String,
    val customerName: String,
    val distanceKm: Double,
    val pickup: String,
    val delivery: String,
    val statusText: String
)

data class CustomerReview(
    val id: String,
    val customerName: String,
    val rating: Float,
    val comment: String,
    val timeAgo: String,
    val avatarType: AvatarType
)

var defaultActiveTrip = ActiveTripData(
    orderId = "#HTO28704",
    customerName = "Karim Touati",
    distanceKm = 5.2,
    pickup = "Pizza Hut المعاريف",
    delivery = "شارع أنفا، الدار البيضاء",
    statusText = "In Transit"
)

fun generateRandomOrdersList(): List<Order> {
    val customers = listOf(
        Pair("Sarah Larbi", AvatarType.WOMAN),
        Pair("Karim Touati", AvatarType.MAN),
        Pair("El-Hadj Mohamed", AvatarType.OLD_MAN),
        Pair("Yassine Mansouri", AvatarType.MAN),
        Pair("Lina Benali", AvatarType.WOMAN),
        Pair("Ammi Rachid", AvatarType.OLD_MAN),
        Pair("Nadia Khelil", AvatarType.WOMAN)
    )
    val orderTypes = listOf("Food / طعام", "Fast Food / وجبات سريعة", "Groceries / بقالة", "Pharmacy / دواء")
    val pickups = listOf("Pizza Hut المعاريف", "Tacos de Lyon أنفا", "Burger King غوتييه", "KFC توين سنتر", "McDo عين الذئاب")
    val clients = listOf("شارع أنفا، الدار البيضاء", "حي المعاريف", "شارع غوتييه", "حي بورغون", "عين الذئاب", "حي النخيل")

    return (1..3).map {
        val (name, avatarType) = customers.random()
        Order(
            id = "#" + (10000..99999).random(),
            customerName = name,
            distanceKm = (1.0 + (6.0 - 1.0) * Math.random()).let { "%.1f".format(it).toDouble() },
            price = (5.0 + (25.0 - 5.0) * Math.random()).let { "%.2f".format(it).toDouble() },
            pickup = pickups.random(),
            client = clients.random(),
            orderType = orderTypes.random(),
            avatarType = avatarType
        )
    }
}

fun generateRandomIncomingOrder(): Order {
    val customers = listOf(
        Pair("Yasmine Bennani", AvatarType.WOMAN),
        Pair("Sofiane Touati", AvatarType.MAN),
        Pair("El-Hadj Hassan", AvatarType.OLD_MAN),
        Pair("Nour-El-Houda", AvatarType.WOMAN),
        Pair("Bilal Khelil", AvatarType.YOUNG)
    )
    val orderTypes = listOf("Food 🍔", "Fast Food 🍕", "Groceries 🛒", "Pharmacy 💊")
    val pickups = listOf("Pizza Hut المعاريف", "Tacos de Lyon أنفا", "Burger King غوتييه", "KFC توين سنتر", "McDo عين الذئاب")
    val clients = listOf("شارع أنفا، الدار البيضاء", "حي المعاريف", "شارع غوتييه", "حي بورغون", "عين الذئاب", "حي النخيل")

    val (name, avatarType) = customers.random()
    return Order(
        id = "#HTO" + (10000..99999).random(),
        customerName = name,
        distanceKm = (1.2 + (7.5 - 1.2) * Math.random()).let { "%.1f".format(it).toDouble() },
        price = (6.5 + (30.0 - 6.5) * Math.random()).let { "%.2f".format(it).toDouble() },
        pickup = pickups.random(),
        client = clients.random(),
        orderType = orderTypes.random(),
        avatarType = avatarType
    )
}

fun generateRandomReview(lang: String = "en"): CustomerReview {
    val enComments = listOf(
        "Excellent driver, super fast delivery! Thank you 👌",
        "Food arrived fresh and warm. Great service! 🌟",
        "Very polite driver and smooth delivery! 👍",
        "Fast delivery right on time! 🚗💨",
        "Polite and professional, highly recommended! ❤️"
    )
    val frComments = listOf(
        "Livreur très rapide et très poli, merci ! 👌",
        "Commande arrivée chaude et bien emballée. Parfait ! 🌟",
        "Service impeccable, très satisfait. 👍",
        "Livraison rapide et à l'heure exacte. 🚗💨",
        "Très professionnel et sympathique ! ❤️"
    )
    val arComments = listOf(
        "سائق ممتاز وسريع جداً في التوصيل! شكراً لك 👌",
        "وصل الطعام ساخناً وطازجاً، خدمة ممتازة! 🌟",
        "سائق محترم وسريع التعامل. 👍",
        "توصيل ممتاز وفي الوقت المحدد بالضبط 🚗💨",
        "تعامل راقي وسريع جداً، أنصح به! ❤️"
    )

    val comments = when (lang) {
        "ar" -> arComments
        "fr" -> frComments
        else -> enComments
    }

    val names = listOf(
        Pair("Yassine Khelifi", AvatarType.MAN),
        Pair("Sonia Mehdi", AvatarType.WOMAN),
        Pair("El-Hadj Mohamed", AvatarType.OLD_MAN),
        Pair("Amine Touati", AvatarType.MAN),
        Pair("Meriem Benali", AvatarType.WOMAN),
        Pair("Walid Ziani", AvatarType.YOUNG)
    )

    val (name, avatarType) = names.random()
    val ratings = listOf(5.0f, 5.0f, 4.5f, 5.0f, 4.0f)

    return CustomerReview(
        id = "#REV" + (1000..9999).random(),
        customerName = name,
        rating = ratings.random(),
        comment = comments.random(),
        timeAgo = if (lang == "ar") "الآن" else if (lang == "fr") "À l'instant" else "Just now",
        avatarType = avatarType
    )
}

fun getInitialReviewsList(lang: String = "en"): List<CustomerReview> {
    return when (lang) {
        "ar" -> listOf(
            CustomerReview("#REV101", "Kamel Ouali", 5.0f, "توصيل في القمة وسريع جداً!", "قبل 15 دقيقة", AvatarType.MAN),
            CustomerReview("#REV102", "Ania Hadj", 4.5f, "سائق خلوق وخدمة جيدة.", "قبل ساعة", AvatarType.WOMAN),
            CustomerReview("#REV103", "Ammi Ahmed", 5.0f, "ماشاء الله تعامل راقي وسريع.", "قبل ساعتين", AvatarType.OLD_MAN)
        )
        "fr" -> listOf(
            CustomerReview("#REV101", "Kamel Ouali", 5.0f, "Livraison au top et très rapide !", "Il y a 15 min", AvatarType.MAN),
            CustomerReview("#REV102", "Ania Hadj", 4.5f, "Livreur très sympa et bon service.", "Il y a 1h", AvatarType.WOMAN),
            CustomerReview("#REV103", "Ammi Ahmed", 5.0f, "Service parfait et rapide.", "Il y a 2h", AvatarType.OLD_MAN)
        )
        else -> listOf(
            CustomerReview("#REV101", "Kamel Ouali", 5.0f, "Top notch and super fast delivery!", "15 mins ago", AvatarType.MAN),
            CustomerReview("#REV102", "Ania Hadj", 4.5f, "Very polite driver and great service.", "1 hour ago", AvatarType.WOMAN),
            CustomerReview("#REV103", "Ammi Ahmed", 5.0f, "Great attitude and prompt delivery.", "2 hours ago", AvatarType.OLD_MAN)
        )
    }
}
