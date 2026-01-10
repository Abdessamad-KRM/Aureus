package com.example.aureus.data

import java.text.SimpleDateFormat
import java.util.*

/**
 * Static data for the Aureus Banking App
 * Contains sample data for demo purposes
 */

// ============================================
// User Data
// ============================================

data class User(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val profileImage: String? = null,
    val address: String? = null,
    val city: String? = null,
    val country: String = "Morocco"
)

object TestAccount {
    const val EMAIL = "yassir.hamzaoui@aureus.ma"
    const val PASSWORD = "Maroc2024!"
    const val PIN = "1234"
    const val USER_ID = "user_001"
    const val FIRST_NAME = "Yassir"
    const val LAST_NAME = "Hamzaoui"
    const val PHONE = "+212 6 61 23 45 67"
    const val ADDRESS = "Résidence Al Wifaq, Apt 12, Boulevard Zerktouni"
    const val CITY = "Casablanca"
    const val COUNTRY = "Maroc"
    
    val user = User(
        id = USER_ID,
        firstName = FIRST_NAME,
        lastName = LAST_NAME,
        email = EMAIL,
        phone = PHONE,
        address = ADDRESS,
        city = CITY,
        country = COUNTRY
    )
}

// ============================================
// Card Data
// ============================================

data class BankCard(
    val id: String,
    val cardNumber: String,
    val cardHolder: String,
    val expiryDate: String,
    val cvv: String,
    val cardType: CardType,
    val balance: Double,
    val currency: String = "MAD",
    val isDefault: Boolean = false,
    val cardColor: CardColor = CardColor.NAVY
)

enum class CardType {
    VISA, MASTERCARD, AMEX, DISCOVER
}

enum class CardColor {
    NAVY, GOLD, BLACK, BLUE, PURPLE, GREEN
}

object StaticCards {
    val cards = listOf(
        BankCard(
            id = "card_001",
            cardNumber = "4562 1122 4945 9852",
            cardHolder = "Yassir Hamzaoui",
            expiryDate = "12/26",
            cvv = "123",
            cardType = CardType.VISA,
            balance = 85545.00,
            isDefault = true,
            cardColor = CardColor.NAVY
        ),
        BankCard(
            id = "card_002",
            cardNumber = "4562 1122 4945 7823",
            cardHolder = "Yassir Hamzaoui",
            expiryDate = "09/27",
            cvv = "456",
            cardType = CardType.MASTERCARD,
            balance = 42180.50,
            cardColor = CardColor.GOLD
        ),
        BankCard(
            id = "card_003",
            cardNumber = "4562 1122 4945 3621",
            cardHolder = "Yassir Hamzaoui",
            expiryDate = "05/28",
            cvv = "789",
            cardType = CardType.VISA,
            balance = 18900.00,
            cardColor = CardColor.BLACK
        )
    )
}

// ============================================
// Transaction Data
// ============================================

data class Transaction(
    val id: String,
    val title: String,
    val description: String,
    val amount: Double,
    val type: TransactionType,
    val category: TransactionCategory,
    val date: Date,
    val cardId: String? = null,
    val recipientName: String? = null,
    val recipientAvatar: String? = null,
    val status: TransactionStatus = TransactionStatus.COMPLETED
)

enum class TransactionType {
    INCOME, EXPENSE, TRANSFER
}

enum class TransactionCategory {
    SALARY, SHOPPING, FOOD, TRANSPORT, ENTERTAINMENT, HEALTH, EDUCATION, BILLS, OTHER
}

enum class TransactionStatus {
    PENDING, COMPLETED, FAILED, CANCELLED
}

object StaticTransactions {
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    
    private fun createDate(dateStr: String): Date {
        return dateFormat.parse(dateStr) ?: Date()
    }
    
    val transactions = listOf(
        Transaction(
            id = "trx_001",
            title = "Marjane",
            description = "Courses alimentaires mensuelles",
            amount = -2850.00,
            type = TransactionType.EXPENSE,
            category = TransactionCategory.FOOD,
            date = createDate("09/01/2026 14:30"),
            recipientName = "Marjane Californie"
        ),
        Transaction(
            id = "trx_002",
            title = "Meditel",
            description = "Recharge téléphone mobile",
            amount = -200.00,
            type = TransactionType.EXPENSE,
            category = TransactionCategory.BILLS,
            date = createDate("08/01/2026 09:15"),
            recipientName = "Meditel"
        ),
        Transaction(
            id = "trx_003",
            title = "Salaire Mensuel",
            description = "OCP Group - Ingénieur",
            amount = 18500.00,
            type = TransactionType.INCOME,
            category = TransactionCategory.SALARY,
            date = createDate("05/01/2026 08:00"),
            recipientName = "OCP Group"
        ),
        Transaction(
            id = "trx_004",
            title = "Acima",
            description = "Achat électroménager",
            amount = -5400.00,
            type = TransactionType.EXPENSE,
            category = TransactionCategory.SHOPPING,
            date = createDate("04/01/2026 18:45"),
            recipientName = "Acima Anfa"
        ),
        Transaction(
            id = "trx_005",
            title = "Careem",
            description = "Course vers bureau",
            amount = -45.00,
            type = TransactionType.EXPENSE,
            category = TransactionCategory.TRANSPORT,
            date = createDate("04/01/2026 08:30"),
            recipientName = "Careem"
        ),
        Transaction(
            id = "trx_006",
            title = "Zara Maroc",
            description = "Achat vêtements",
            amount = -980.00,
            type = TransactionType.EXPENSE,
            category = TransactionCategory.SHOPPING,
            date = createDate("03/01/2026 16:20"),
            recipientName = "Zara Morocco Mall"
        ),
        Transaction(
            id = "trx_007",
            title = "Café Maure",
            description = "Restaurant Ain Diab",
            amount = -320.00,
            type = TransactionType.EXPENSE,
            category = TransactionCategory.FOOD,
            date = createDate("03/01/2026 13:30"),
            recipientName = "Café Maure Ain Diab"
        ),
        Transaction(
            id = "trx_008",
            title = "LYDEC",
            description = "Facture eau et électricité",
            amount = -890.00,
            type = TransactionType.EXPENSE,
            category = TransactionCategory.BILLS,
            date = createDate("02/01/2026 14:00"),
            recipientName = "LYDEC Casablanca"
        ),
        Transaction(
            id = "trx_009",
            title = "Mission Freelance",
            description = "Développement app mobile",
            amount = 8500.00,
            type = TransactionType.INCOME,
            category = TransactionCategory.OTHER,
            date = createDate("01/01/2026 11:30"),
            recipientName = "Client Rabat"
        ),
        Transaction(
            id = "trx_010",
            title = "Jumia",
            description = "Achat en ligne - smartphone",
            amount = -3200.00,
            type = TransactionType.EXPENSE,
            category = TransactionCategory.SHOPPING,
            date = createDate("30/12/2025 20:15"),
            recipientName = "Jumia Maroc"
        )
    )
}

// ============================================
// Contact Data
// ============================================

data class Contact(
    val id: String,
    val name: String,
    val phone: String,
    val email: String? = null,
    val avatar: String? = null,
    val accountNumber: String? = null,
    val isFavorite: Boolean = false
)

object StaticContacts {
    val contacts = listOf(
        Contact(
            id = "contact_001",
            name = "Mohammed EL ALAMI",
            phone = "+212 6 61 45 78 90",
            email = "m.elalami@gmail.com",
            accountNumber = "007 820 0012345678 19",
            isFavorite = true
        ),
        Contact(
            id = "contact_002",
            name = "Fatima-Zahra BENANI",
            phone = "+212 6 62 33 44 55",
            email = "fz.benani@hotmail.com",
            accountNumber = "007 820 0087654321 25",
            isFavorite = true
        ),
        Contact(
            id = "contact_003",
            name = "Ahmed IDRISSI",
            phone = "+212 6 70 12 34 56",
            email = "ahmed.idrissi@outlook.com",
            accountNumber = "007 820 0045678912 33",
            isFavorite = false
        ),
        Contact(
            id = "contact_004",
            name = "Salma EL FASSI",
            phone = "+212 6 77 88 99 00",
            email = "salma.elfassi@gmail.com",
            accountNumber = "007 820 0098765432 41",
            isFavorite = true
        ),
        Contact(
            id = "contact_005",
            name = "Omar TAZI",
            phone = "+212 6 68 55 44 33",
            email = "omar.tazi@yahoo.fr",
            accountNumber = "007 820 0011223344 58",
            isFavorite = false
        )
    )
}

// ============================================
// Statistics Data
// ============================================

data class MonthlyStatistic(
    val month: String,
    val income: Double,
    val expense: Double
)

data class CategoryStatistic(
    val category: TransactionCategory,
    val amount: Double,
    val percentage: Float,
    val transactionCount: Int
)

object StaticStatistics {
    val monthlyStats = listOf(
        MonthlyStatistic("Jan", 27000.00, 14825.00),
        MonthlyStatistic("Fév", 26500.00, 13900.00),
        MonthlyStatistic("Mar", 27000.00, 15200.00),
        MonthlyStatistic("Avr", 27000.00, 12800.00),
        MonthlyStatistic("Mai", 28500.00, 16100.00),
        MonthlyStatistic("Juin", 27000.00, 14685.00)
    )
    
    val categoryStats = listOf(
        CategoryStatistic(TransactionCategory.SHOPPING, 9580.00, 33f, 3),
        CategoryStatistic(TransactionCategory.FOOD, 3170.00, 28f, 2),
        CategoryStatistic(TransactionCategory.TRANSPORT, 45.00, 4f, 1),
        CategoryStatistic(TransactionCategory.ENTERTAINMENT, 0.00, 5f, 0),
        CategoryStatistic(TransactionCategory.BILLS, 1090.00, 18f, 2),
        CategoryStatistic(TransactionCategory.OTHER, 0.00, 12f, 0)
    )
    
    // Calculate spending percentage (55% in image)
    val totalIncome = monthlyStats.last().income
    val totalExpense = monthlyStats.last().expense
    val spendingPercentage = ((totalExpense / totalIncome) * 100).toInt()
}

// ============================================
// Language Data
// ============================================

data class Language(
    val code: String,
    val name: String,
    val nativeName: String,
    val flag: String
)

object SupportedLanguages {
    val languages = listOf(
        Language("en", "English", "English", "🇬🇧"),
        Language("fr", "French", "Français", "🇫🇷"),
        Language("ar", "Arabic", "العربية", "🇲🇦"),
        Language("es", "Spanish", "Español", "🇪🇸"),
        Language("de", "German", "Deutsch", "🇩🇪")
    )
}
