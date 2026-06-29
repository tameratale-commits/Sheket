package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.TrendingFlat
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.CustomerEntity
import com.example.data.model.TransactionEntity
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerScreen(
    viewModel: LedgerViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }

    // Collect data from viewModel
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val totalSales by viewModel.totalSales.collectAsStateWithLifecycle()
    val totalExpenses by viewModel.totalExpenses.collectAsStateWithLifecycle()
    val totalDebts by viewModel.totalDebts.collectAsStateWithLifecycle()

    val selectedCustomer by viewModel.selectedCustomer.collectAsStateWithLifecycle()
    val selectedCustomerTransactions by viewModel.selectedCustomerTransactions.collectAsStateWithLifecycle()
    val customerSearchQuery by viewModel.customerSearchQuery.collectAsStateWithLifecycle()

    // Dialog trigger states
    var showSaleDialog by remember { mutableStateOf(false) }
    var showExpenseDialog by remember { mutableStateOf(false) }
    var showAddCustomerDialog by remember { mutableStateOf(false) }
    var showAddDebtDialog by remember { mutableStateOf<CustomerEntity?>(null) }
    var showReceivePaymentDialog by remember { mutableStateOf<CustomerEntity?>(null) }
    var pendingDeleteTransaction by remember { mutableStateOf<TransactionEntity?>(null) }

    // Theme Custom Colors (Teal & Emerald Emerald Bookkeeping vibe)
    val themePrimary = Color(0xFF0D9488) // Deep Teal
    val themeSecondary = Color(0xFF0F172A) // Slate Blue / Dark Charcoal
    val cardBackground = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "ሰላም፣ ተስፋዬ",
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "ሱቅ መዝገብ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable {
                                Toast.makeText(
                                    context,
                                    "የሱቅ መዝገብ - በኢትዮጵያ ለሚገኙ ሱቆች የተዘጋጀ (Dual language store bookkeeping tool)",
                                    Toast.LENGTH_LONG
                                ).show()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "T",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFFF3F4F9),
                tonalElevation = 0.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0 && selectedCustomer == null,
                    onClick = {
                        selectedTab = 0
                        viewModel.selectCustomer(null)
                    },
                    icon = { Icon(Icons.Rounded.Home, contentDescription = "Ledger") },
                    label = { Text("መዝገብ (Ledger)", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = selectedTab == 1 || selectedCustomer != null,
                    onClick = {
                        selectedTab = 1
                    },
                    icon = { Icon(Icons.Rounded.People, contentDescription = "Debtors") },
                    label = { Text("እዳ/ደንበኞች", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = selectedTab == 2 && selectedCustomer == null,
                    onClick = {
                        selectedTab = 2
                        viewModel.selectCustomer(null)
                    },
                    icon = { Icon(Icons.Rounded.Assessment, contentDescription = "Stats") },
                    label = { Text("ሪፖርት (Stats)", fontSize = 11.sp) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Customer Details View (Overlays normal screens if a customer is selected)
            if (selectedCustomer != null) {
                CustomerDetailsView(
                    customer = selectedCustomer!!,
                    transactions = selectedCustomerTransactions,
                    onBack = { viewModel.selectCustomer(null) },
                    onAddDebt = { showAddDebtDialog = selectedCustomer },
                    onReceivePayment = { showReceivePaymentDialog = selectedCustomer },
                    onDeleteCustomer = {
                        viewModel.deleteCustomer(selectedCustomer!!)
                        Toast.makeText(context, "ደንበኛ ተሰርዟል (Customer deleted)", Toast.LENGTH_SHORT).show()
                    },
                    onDeleteTransaction = { viewModel.deleteTransaction(it) }
                )
            } else {
                when (selectedTab) {
                    0 -> LedgerTab(
                        transactions = transactions,
                        totalSales = totalSales,
                        totalExpenses = totalExpenses,
                        totalDebts = totalDebts,
                        onRecordSaleClick = { showSaleDialog = true },
                        onRecordExpenseClick = { showExpenseDialog = true },
                        onDeleteTransactionClick = { pendingDeleteTransaction = it },
                        onLoadSampleData = { insertSampleData(viewModel) }
                    )
                    1 -> CustomersTab(
                        customers = customers,
                        searchQuery = customerSearchQuery,
                        onSearchQueryChange = { viewModel.searchCustomers(it) },
                        onAddCustomerClick = { showAddCustomerDialog = true },
                        onCustomerClick = { viewModel.selectCustomer(it.id) }
                    )
                    2 -> StatsTab(
                        totalSales = totalSales,
                        totalExpenses = totalExpenses,
                        totalDebts = totalDebts,
                        transactionsCount = transactions.size,
                        customersCount = customers.size,
                        onLoadSampleData = { insertSampleData(viewModel) }
                    )
                }
            }

            // Dialogs
            if (showSaleDialog) {
                AddTransactionDialog(
                    titleAmharic = "የሽያጭ ገቢ መዝግብ",
                    titleEnglish = "Record Sales Revenue",
                    isExpense = false,
                    customers = customers,
                    onDismiss = { showSaleDialog = false },
                    onConfirm = { amount, description, customerId ->
                        viewModel.recordSale(amount, description, customerId)
                        showSaleDialog = false
                        Toast.makeText(context, "ገቢ ሽያጭ ተመዝግቧል (Sale recorded)", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            if (showExpenseDialog) {
                AddTransactionDialog(
                    titleAmharic = "የሱቅ ወጪ መዝግብ",
                    titleEnglish = "Record Shop Expense",
                    isExpense = true,
                    customers = emptyList(),
                    onDismiss = { showExpenseDialog = false },
                    onConfirm = { amount, description, _ ->
                        viewModel.recordExpense(amount, description)
                        showExpenseDialog = false
                        Toast.makeText(context, "ወጪ ተመዝግቧል (Expense recorded)", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            if (showAddCustomerDialog) {
                AddCustomerDialog(
                    onDismiss = { showAddCustomerDialog = false },
                    onConfirm = { name, phone, initialDebt, notes ->
                        viewModel.addCustomer(name, phone, notes, initialDebt)
                        showAddCustomerDialog = false
                        Toast.makeText(context, "አዲስ ደንበኛ ተመዝግቧል (Customer created)", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            if (showAddDebtDialog != null) {
                AddDebtOrPaymentDialog(
                    customer = showAddDebtDialog!!,
                    isDebt = true,
                    onDismiss = { showAddDebtDialog = null },
                    onConfirm = { amount, description ->
                        viewModel.recordDebt(showAddDebtDialog!!.id, amount, description)
                        showAddDebtDialog = null
                        Toast.makeText(context, "እዳ ተጨምሯል (Debt added)", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            if (showReceivePaymentDialog != null) {
                AddDebtOrPaymentDialog(
                    customer = showReceivePaymentDialog!!,
                    isDebt = false,
                    onDismiss = { showReceivePaymentDialog = null },
                    onConfirm = { amount, description ->
                        viewModel.recordDebtPayment(showReceivePaymentDialog!!.id, amount, description)
                        showReceivePaymentDialog = null
                        Toast.makeText(context, "የእዳ ክፍያ ተመዝግቧል (Payment received)", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            if (pendingDeleteTransaction != null) {
                AlertDialog(
                    onDismissRequest = { pendingDeleteTransaction = null },
                    title = { Text("መዝገብ ሰርዝ (Confirm Delete)") },
                    text = {
                        Text("ይህን የሂሳብ መዝገብ ለማጥፋት እርግጠኛ ነዎት?\n(Are you sure you want to delete this ledger entry: \"${pendingDeleteTransaction!!.description}\"?)")
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.deleteTransaction(pendingDeleteTransaction!!)
                                pendingDeleteTransaction = null
                                Toast.makeText(context, "ተሰርዟል (Deleted)", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("ሰርዝ (Delete)")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { pendingDeleteTransaction = null }) {
                            Text("ተመለስ (Cancel)")
                        }
                    }
                )
            }
        }
    }
}

// ==================== LEDGER TAB ====================
@Composable
fun LedgerTab(
    transactions: List<TransactionEntity>,
    totalSales: Double,
    totalExpenses: Double,
    totalDebts: Double,
    onRecordSaleClick: () -> Unit,
    onRecordExpenseClick: () -> Unit,
    onDeleteTransactionClick: (TransactionEntity) -> Unit,
    onLoadSampleData: () -> Unit
) {
    val cashInHand = totalSales - totalExpenses

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Cash Summary Card (styled as the hero Artistic card)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer, // #D3E3FD
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer // #001C38
                ),
                shape = RoundedCornerShape(28.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "የቀረው የሱቅ ጥሬ ገንዘብ (Cash-in-Hand)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        // Artistic positive status badge matching HTML text-white rounded-full bg-[#BA1A1A] or similar
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(if (cashInHand >= 0) Color(0xFF0061A4) else Color(0xFFBA1A1A))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (cashInHand >= 0) "+12%" else "-5%",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Column {
                        Text(
                            text = "ብር",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Text(
                            text = String.format("%,.2f", cashInHand),
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            letterSpacing = (-0.5).sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f))
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.Start) {
                            Text("ጠቅላላ ገቢ (Sales)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.65f))
                            Text(String.format("%,.1f ብር", totalSales), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("ጠቅላላ ወጪ (Exp.)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.65f))
                            Text(String.format("%,.1f ብር", totalExpenses), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFBA1A1A))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("የደንበኞች እዳ (Debts)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.65f))
                            Text(String.format("%,.1f ብር", totalDebts), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
            }
        }

        // Action Buttons Row (using Artistic Flair styling with 16.dp corners and custom theme backgrounds)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onRecordSaleClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .testTag("record_sale_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary, // #E1E2EC
                        contentColor = MaterialTheme.colorScheme.onSecondary // #191C1E
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AddCircle,
                        contentDescription = "Sale",
                        tint = MaterialTheme.colorScheme.onSecondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(horizontalAlignment = Alignment.Start) {
                        Text("ሽያጭ መዝግብ", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Record Sale", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.7f))
                    }
                }

                Button(
                    onClick = onRecordExpenseClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .testTag("record_expense_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer, // #FAD8FD
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer // #2B1630
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.RemoveCircle,
                        contentDescription = "Expense",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(horizontalAlignment = Alignment.Start) {
                        Text("ዕዳ/ወጪ መዝግብ", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Record Debt/Exp", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f))
                    }
                }
            }
        }

        // Section Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "የቅርብ ጊዜ እንቅስቃሴዎች (Recent History)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "${transactions.size} መዝገቦች",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Transaction List
        if (transactions.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ReceiptLong,
                        contentDescription = "No receipts",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "ምንም የሂሳብ እንቅስቃሴ አልተመዘገበም",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "No bookkeeping records registered yet.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onLoadSampleData,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("የሙከራ ሱቅ መዝገብ አስገባ (Load Sample Data)", fontSize = 12.sp)
                    }
                }
            }
        } else {
            items(transactions, key = { it.id }) { txn ->
                TransactionRowItem(transaction = txn, onDelete = { onDeleteTransactionClick(txn) })
            }
        }
    }
}

// ==================== TRANSACTION ROW ITEM ====================
fun getEmojiForTransaction(type: String, description: String): String {
    val descLower = description.lowercase()
    return when {
        descLower.contains("ዳቦ") || descLower.contains("bread") -> "🥖"
        descLower.contains("ዘይት") || descLower.contains("oil") -> "🛢️"
        descLower.contains("ስኳር") || descLower.contains("sugar") -> "🍬"
        descLower.contains("ሳሙና") || descLower.contains("soap") -> "🧼"
        descLower.contains("ወተት") || descLower.contains("milk") -> "🥛"
        descLower.contains("ኤሌክትሪክ") || descLower.contains("electricity") || descLower.contains("መብራት") || descLower.contains("light") -> "💡"
        descLower.contains("እንቁላል") || descLower.contains("egg") -> "🥚"
        type == "DEBT" || descLower.contains("እዳ") || descLower.contains("debt") || descLower.contains("credit") -> "👤"
        type == "EXPENSE" -> "📉"
        else -> "💰"
    }
}

@Composable
fun TransactionRowItem(
    transaction: TransactionEntity,
    onDelete: () -> Unit
) {
    val emoji = getEmojiForTransaction(transaction.type, transaction.description)
    val emojiBg = when (transaction.type) {
        "DEBT" -> Color(0xFFFFDAD6) // Light Red / Pink for Debt
        "EXPENSE" -> Color(0xFFFFF1F2) // Light Rose
        else -> Color(0xFFF0F0F0) // Light Gray for Sales
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Status Icon (Artistic Rounded Corner square with elegant emoji)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(emojiBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = emoji,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Transaction Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.description,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = when (transaction.type) {
                            "SALE" -> "ገቢ ሽያጭ (Sale)"
                            "EXPENSE" -> "የሱቅ ወጪ (Expense)"
                            else -> "እዳ የቀረ (Credit Debt)"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (transaction.type) {
                            "SALE" -> Color(0xFF0061A4) // Artistic Blue
                            "EXPENSE" -> Color(0xFFBA1A1A) // Artistic Red
                            else -> Color(0xFFBA1A1A) // Debt red
                        }
                    )
                    if (transaction.customerName != null) {
                        Text(
                            text = "• ደንበኛ፡ ${transaction.customerName}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
                Text(
                    text = formatTime(transaction.timestamp),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Transaction Amount
            Text(
                text = when (transaction.type) {
                    "SALE" -> String.format("+%,.2f", transaction.amount)
                    "EXPENSE" -> String.format("-%,.2f", transaction.amount)
                    else -> String.format("እዳ: %,.2f", transaction.amount)
                },
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = when (transaction.type) {
                    "SALE" -> Color(0xFF0061A4) // Artistic Blue
                    "EXPENSE" -> Color(0xFFBA1A1A) // Artistic Red
                    else -> Color(0xFFBA1A1A) // Debt red
                }
            )

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = "Delete Record",
                    tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ==================== DEBTORS / CUSTOMERS TAB ====================
@Composable
fun CustomersTab(
    customers: List<CustomerEntity>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onAddCustomerClick: () -> Unit,
    onCustomerClick: (CustomerEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Add Customer Top Button
        Button(
            onClick = onAddCustomerClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("add_customer_button"),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Rounded.PersonAdd, contentDescription = "Add Client")
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("አዲስ ባለእዳ ደንበኛ መዝግብ", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("Register New Customer", fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("customer_search"),
            placeholder = { Text("ስም ወይም ስልክ ቁጥር ፈልግ... (Search Customers)") },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = "Search") },
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "የደንበኞች ዝርዝርና እዳ (Customers list & Balances)",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Customer List
        if (customers.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Rounded.PeopleOutline,
                    contentDescription = "No customers",
                    modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "ምንም የተመዘገበ ደንበኛ የለም",
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = "No customers found. Click top button to add.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.8f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(customers, key = { it.id }) { customer ->
                    CustomerCardItem(customer = customer, onClick = { onCustomerClick(customer) })
                }
            }
        }
    }
}

// ==================== CUSTOMER CARD ITEM ====================
@Composable
fun CustomerCardItem(
    customer: CustomerEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Customer Avatar with Initials in Artistic PrimaryContainer colors
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = customer.name.take(1).uppercase(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = customer.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (customer.phone.isNotBlank()) "ስልክ: ${customer.phone}" else "ምንም ስልክ አልተጻፈም",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Debt balance Info
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "የእዳ መጠን",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = if (customer.currentDebt > 0) String.format("%,.1f ብር", customer.currentDebt) else "እዳ የለባቸውም",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (customer.currentDebt > 0) Color(0xFFBA1A1A) else Color(0xFF0061A4)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = "Details",
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}

// ==================== DETAILED CUSTOMER VIEW ====================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailsView(
    customer: CustomerEntity,
    transactions: List<TransactionEntity>,
    onBack: () -> Unit,
    onAddDebt: () -> Unit,
    onReceivePayment: () -> Unit,
    onDeleteCustomer: () -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit
) {
    val context = LocalContext.current
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Custom Back Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text("የደንበኛ ዝርዝር መረጃ (Customer Details)", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                Text(customer.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.weight(1f))

            IconButton(
                onClick = {
                    if (customer.phone.isNotBlank()) {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${customer.phone}"))
                        context.startActivity(intent)
                    } else {
                        Toast.makeText(context, "ይቅርታ የስልክ ቁጥር የለም", Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                Icon(Icons.Rounded.Call, contentDescription = "Call", tint = MaterialTheme.colorScheme.primary)
            }

            IconButton(onClick = { showDeleteConfirmDialog = true }) {
                Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }

        Divider()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Debt Balance summary
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (customer.currentDebt > 0) Color(0xFFFFF1F2) else Color(0xFFECFDF5)
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (customer.currentDebt > 0) "በእዳ ያለባቸው ጠቅላላ ቀሪ ሂሳብ" else "ቀሪ እዳ የለባቸውም",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (customer.currentDebt > 0) MaterialTheme.colorScheme.error else Color(0xFF0061A4)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format("%,.2f ብር", customer.currentDebt),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = if (customer.currentDebt > 0) MaterialTheme.colorScheme.error else Color(0xFF0061A4)
                        )

                        if (customer.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.6f))
                            ) {
                                Text(
                                    text = "ማስታወሻ፡ ${customer.notes}",
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(8.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // Quick Debt actions
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onAddDebt,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Rounded.ShoppingCart, contentDescription = "Credit Sale")
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("እዳ ጨምር", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Add Credit Debt", fontSize = 8.sp, color = Color.White.copy(alpha = 0.7f))
                        }
                    }

                    Button(
                        onClick = onReceivePayment,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Rounded.Payments, contentDescription = "Receive Pay")
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("እዳ ክፍያ መዝግብ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("Receive Payment", fontSize = 8.sp, color = Color.White.copy(alpha = 0.7f))
                        }
                    }
                }
            }

            // Customer History list
            item {
                Text(
                    text = "የዚህ ደንበኛ መዝገብ ታሪክ (Customer History Statement)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            if (transactions.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.History,
                            contentDescription = "No history",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "ለዚህ ደንበኛ ምንም የተመዘገበ ታሪክ የለም",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                items(transactions, key = { it.id }) { txn ->
                    TransactionRowItem(transaction = txn, onDelete = { onDeleteTransaction(txn) })
                }
            }
        }
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("ደንበኛ ሰርዝ (Confirm Delete)") },
            text = {
                Text("ደንበኛውን \"${customer.name}\" ማጥፋት ይፈልጋሉ? ደንበኛው ሲጠፋ አብሮ ተያያዥ የሆኑት የክፍያ ታሪኮች በሙሉ ይደመሰሳሉ።\n\n(Are you sure you want to delete customer \"${customer.name}\"? This cannot be undone.)")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeleteCustomer()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("አጥፋ (Delete)")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("ተመለስ (Cancel)")
                }
            }
        )
    }
}

// ==================== STATS / INSIGHTS TAB ====================
@Composable
fun StatsTab(
    totalSales: Double,
    totalExpenses: Double,
    totalDebts: Double,
    transactionsCount: Int,
    customersCount: Int,
    onLoadSampleData: () -> Unit
) {
    val profit = totalSales - totalExpenses
    val totalVolume = totalSales + totalExpenses + totalDebts

    // Progressive charts
    val salesRatio = if (totalVolume > 0) (totalSales / totalVolume).toFloat() else 0f
    val expensesRatio = if (totalVolume > 0) (totalExpenses / totalVolume).toFloat() else 0f
    val debtsRatio = if (totalVolume > 0) (totalDebts / totalVolume).toFloat() else 0f

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            Text(
                text = "የሱቅ የሂሳብ ሪፖርት (Store Business Intelligence)",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = "Simple analytics generated from local records",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }

        // Profit card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (profit >= 0) Color(0xFFECFDF5) else Color(0xFFFFF1F2)
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ጠቅላላ ትርፍ / ኪሳራ (Calculated Net Profit)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (profit >= 0) Color(0xFF0061A4) else MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = String.format("%,.2f ብር", profit),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = if (profit >= 0) Color(0xFF0061A4) else MaterialTheme.colorScheme.error,
                        letterSpacing = (-0.5).sp
                    )
                }
            }
        }

        // AI Store Advisor
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer // #D3E3FD
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Lightbulb,
                            contentDescription = "Insight Icon",
                            tint = Color(0xFFEAB308) // Nice Amber yellow
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "የሱቅ አማካሪ ምክር (Bookkeeping Tip)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = when {
                                totalSales == 0.0 && totalExpenses == 0.0 -> {
                                    "ምንም የሱቅ ሽያጭ የለም! እባክዎን የሙከራ ዳታ በማስገባት ይሞክሩ። (No transactions recorded yet.)"
                                }
                                totalDebts > totalSales -> {
                                    "ማስጠንቀቂያ፡ የደንበኞች እዳ ከሽያጭዎ በልጧል! እዳዎችን በፍጥነት ለመሰብሰብ ደንበኞችዎ ጋር ይደውሉ። (Warning: Customer debts exceed sales. Collect credits rapidly.)"
                                }
                                profit < 0 -> {
                                    "ኪሳራ እያሳዩ ነው! ወጪዎችዎን ይቀንሱ ወይም የትርፍ መጠንዎን ያስተካክሉ። (Loss indicator! Please optimize costs or markups.)"
                                }
                                profit > 0 && totalDebts < totalSales -> {
                                    "ግሩም ስራ! ሱቅዎ በጥሩ የትርፍ ሁኔታ ላይ ነው። (Excellent store performance. Profit metrics are healthy.)"
                                }
                                else -> {
                                    "ሂሳብዎን በየቀኑ በጥንቃቄ መመዝገብዎን ይቀጥሉ። (Keep recording daily balances accurately.)"
                                }
                              },
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }

        // Graphic ratio indicators
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "የካፒታል ስርጭት (Capital Metrics Chart)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Sales Bar
                    Text("ሽያጭ (Sales): ${String.format("%.1f%%", salesRatio * 100)}", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { salesRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = Color(0xFF0061A4), // Artistic Blue
                        trackColor = Color(0xFFF1F5F9)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Expenses Bar
                    Text("ወጪዎች (Expenses): ${String.format("%.1f%%", expensesRatio * 100)}", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { expensesRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = Color(0xFFBA1A1A), // Artistic Red
                        trackColor = Color(0xFFF1F5F9)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Debts Bar
                    Text("የደንበኞች እዳ (Outstanding Debts): ${String.format("%.1f%%", debtsRatio * 100)}", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { debtsRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = Color(0xFFBA1A1A), // Artistic Red (or orange alternative)
                        trackColor = Color(0xFFF1F5F9)
                    )
                }
            }
        }

        // Statistics list
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("ፈጣን ቆጠራዎች (Ledger Summary Logs)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("ጠቅላላ መዝገብ እንቅስቃሴዎች", fontSize = 12.sp)
                        Text("$transactionsCount entries", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("ጠቅላላ ባለእዳ ደንበኞች", fontSize = 12.sp)
                        Text("$customersCount customers", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        // Demo data loader if empty
        item {
            Button(
                onClick = onLoadSampleData,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("የሙከራ ሱቅ ዳታዎችን ሙሉ (Load Demo Store Records)", fontSize = 12.sp)
            }
        }
    }
}

// ==================== TRANSACTION ADD DIALOG ====================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    titleAmharic: String,
    titleEnglish: String,
    isExpense: Boolean,
    customers: List<CustomerEntity>,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, description: String, customerId: Int?) -> Unit
) {
    var amountStr by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCustomer by remember { mutableStateOf<CustomerEntity?>(null) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(titleAmharic, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(titleEnglish, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("የገንዘብ መጠን (Amount in ETB)") },
                    placeholder = { Text("ለምሳሌ፡ 250.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("dialog_amount_input")
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("ዝርዝር መግለጫ (Description / Item Name)") },
                    placeholder = { Text("ለምሳሌ፡ 2 ኪሎ ስኳር ሽያጭ") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("dialog_description_input")
                )

                // If a Sale, we can optionally link a customer
                if (!isExpense && customers.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = dropdownExpanded,
                        onExpandedChange = { dropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedCustomer?.name ?: "ደንበኛ አልተመረጠም (No Customer selected)",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("የደንበኛ ሂሳብ (Link Customer - Optional)") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("ደንበኛ አትምረጥ (None - Cash Sale)") },
                                onClick = {
                                    selectedCustomer = null
                                    dropdownExpanded = false
                                }
                            )
                            customers.forEach { customer ->
                                DropdownMenuItem(
                                    text = { Text(customer.name) },
                                    onClick = {
                                        selectedCustomer = customer
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountStr.toDoubleOrNull()
                    if (amount != null && amount > 0 && description.isNotBlank()) {
                        onConfirm(amount, description, selectedCustomer?.id)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isExpense) Color(0xFFBE123C) else Color(0xFF0D9488)
                )
            ) {
                Text("መዝግብ (Record)")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("ተመለስ (Cancel)")
            }
        }
    )
}

// ==================== DEBT ADD OR RECEIVE DIALOG ====================
@Composable
fun AddDebtOrPaymentDialog(
    customer: CustomerEntity,
    isDebt: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, description: String) -> Unit
) {
    var amountStr by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = if (isDebt) "ለ${customer.name} አዲስ እዳ መዝግብ" else "ከ${customer.name} እዳ ክፍያ ተቀበል",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = if (isDebt) "Add credit debt (items taken on trust)" else "Receive partial/full debt cash payoff",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("የገንዘብ መጠን በብር (Amount ETB)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("debt_amount_input")
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("የመዝገቡ ምክንያት / ዕቃ (Reason / Item list)") },
                    placeholder = { Text(if (isDebt) "ለምሳሌ፡ ዱቄትና ዘይት በዱቤ" else "ለምሳሌ፡ የእዳ ከፊል ክፍያ") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("debt_desc_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountStr.toDoubleOrNull()
                    if (amount != null && amount > 0 && description.isNotBlank()) {
                        onConfirm(amount, description)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDebt) Color(0xFFBE123C) else Color(0xFF0D9488)
                )
            ) {
                Text(if (isDebt) "እዳ ጨምር (Add Debt)" else "ክፍያ መዝግብ (Pay)")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("ተመለስ (Cancel)")
            }
        }
    )
}

// ==================== ADD CUSTOMER DIALOG ====================
@Composable
fun AddCustomerDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, phone: String, initialDebt: Double, notes: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var initialDebtStr by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("አዲስ ባለእዳ ደንበኛ መዝግብ", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Create Debt Customer", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("የደንበኛ ሙሉ ስም (Customer Full Name)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("cust_name_input")
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("ስልክ ቁጥር (Phone Number - Optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = initialDebtStr,
                    onValueChange = { initialDebtStr = it },
                    label = { Text("ነባር የቀደመ እዳ ካለ (Previous / Initial Debt - Optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("አጭር ማስታወሻ (Notes / Home details)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val initialDebt = initialDebtStr.toDoubleOrNull() ?: 0.0
                        onConfirm(name, phone, initialDebt, notes)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))
            ) {
                Text("ደንበኛ ፍጠር (Save Customer)")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("ተመለስ (Cancel)")
            }
        }
    )
}

// ==================== HELPERS ====================
fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

// Injects Demo Store Data so they can preview easily!
fun insertSampleData(viewModel: LedgerViewModel) {
    viewModel.recordSale(2850.0, "የጅምላ ዕቃዎች ሽያጭ (Bulk soap sale)")
    viewModel.recordSale(120.0, "የዳቦና ወተት የችርቻሮ ሽያጭ (Retail milk/bread)")
    viewModel.recordExpense(850.0, "የሱቅ ኤሌክትሪክና መብራት ክፍያ (Shop electricity payment)")
    viewModel.recordExpense(1500.0, "ለሱቅ ረዳት የቀን ደሞዝ ክፍያ (Shop helper day payment)")
    viewModel.recordSale(450.0, "ለስላሳዎችና መጠጦች ሽያጭ (Beverage sales)")

    // Add sample debtors
    viewModel.addCustomer("ካሊድ መሐመድ (Khalid)", "0911223344", "የኪዮስክ ብስኩት በዱቤ (Biscuit credit)", 750.0)
    viewModel.addCustomer("ቤተልሔም ግርማ (Bethelhem)", "0920445566", "የዱቄትና ዘይት እዳ (Oil & Flour credit)", 1800.0)
    viewModel.addCustomer("ዮናስ ታደሰ (Yonas)", "0930778899", "የእንቁላል ሳጥን (Eggs carton)", 340.0)
}
