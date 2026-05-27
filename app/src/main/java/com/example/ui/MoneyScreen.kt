package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.*

@Composable
fun MoneyScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val dark = viewModel.darkThemeEnabled
    val currentProject by viewModel.activeProject.collectAsState()
    val allTransactions by viewModel.transactions.collectAsState()

    // Filters & search state
    val query = viewModel.transactionSearchQuery
    val typeFilter = viewModel.transactionTypeFilter
    val catFilter = viewModel.transactionCategoryFilter

    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }

    // Aggregate lists filtered for current selected project
    val projectTransactions = remember(allTransactions, currentProject) {
        val projId = currentProject?.id
        if (projId == null) emptyList()
        else allTransactions.filter { it.projectId == projId }
    }

    // Calculations of unfiltered numbers
    val totalIn = remember(projectTransactions) {
        projectTransactions.filter { it.type == "Money In" }.sumOf { it.amount }
    }
    val totalOut = remember(projectTransactions) {
        projectTransactions.filter { it.type == "Money Out" }.sumOf { it.amount }
    }
    val balance = totalIn - totalOut

    // Filter results dynamically!
    val filteredTransactions = remember(projectTransactions, query, typeFilter, catFilter) {
        projectTransactions.filter { tx ->
            val matchesType = (typeFilter == "All") || (tx.type == typeFilter)
            val matchesCategory = (catFilter == "All") || (tx.category == catFilter)
            val matchesQuery = (query.isBlank()) || 
                    tx.description.contains(query, ignoreCase = true) || 
                    tx.category.contains(query, ignoreCase = true) ||
                    (tx.partyName?.contains(query, ignoreCase = true) == true) ||
                    tx.reference.contains(query, ignoreCase = true) ||
                    tx.paymentMethod.contains(query, ignoreCase = true)
            matchesType && matchesCategory && matchesQuery
        }
    }

    // List of categories for category filter chips
    val categories = listOf("All", "Material", "Labor", "Equipment", "Client Advance", "Other")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Page title & CSV Export
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Cash Flows",
                        color = if (dark) NeonCyan else Color(0xFF0284C7),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Transactions ledger for ${currentProject?.name ?: "None"}",
                        color = if (dark) TextSecondary else TextSecondaryLight,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                val context = LocalContext.current
                IconButton(
                    onClick = { viewModel.exportTransactionsCSV(context) },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDownload,
                        contentDescription = "Export CSV",
                        tint = if (dark) NeonCyan else Color(0xFF0284C7),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Stats grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Cash-In Card
                GlassCard(
                    modifier = Modifier.weight(1f),
                    darkTheme = dark,
                    padding = 8.dp,
                    borderColor = if (dark) GlassBorderDark else null
                ) {
                    Text("MONEY IN", color = NeonGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(
                        currencyFormatter.format(totalIn),
                        color = if (dark) TextPrimary else TextPrimaryLight,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Cash-Out Card
                GlassCard(
                    modifier = Modifier.weight(1f),
                    darkTheme = dark,
                    padding = 8.dp,
                    borderColor = if (dark) GlassBorderDark else null
                ) {
                    Text("MONEY OUT", color = NeonPink, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(
                        currencyFormatter.format(totalOut),
                        color = if (dark) TextPrimary else TextPrimaryLight,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Net balance Card
                GlassCard(
                    modifier = Modifier.weight(1f),
                    darkTheme = dark,
                    padding = 8.dp,
                    borderColor = if (dark) GlassBorderNeonCyan else null
                ) {
                    Text("NET BALANCE", color = NeonCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(
                        currencyFormatter.format(balance),
                        color = if (balance >= 0) NeonGreen else NeonPink,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Search Bar Block
        item {
            GlassTextField(
                value = query,
                onValueChange = { viewModel.transactionSearchQuery = it },
                label = "Search ledger...",
                darkTheme = dark,
                placeholder = "Type description or cement/steel...",
                icon = Icons.Default.Search,
                focusedStroke = NeonCyan
            )
        }

        // Filter Type Tabs: All, Money In, Money Out
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Money In", "Money Out").forEach { tab ->
                    GlassChip(
                        text = tab,
                        selected = typeFilter == tab,
                        onClick = { viewModel.transactionTypeFilter = tab },
                        darkTheme = dark,
                        activeColor = if (tab == "Money In") NeonGreen else if (tab == "Money Out") NeonPink else NeonCyan
                    )
                }
            }
        }

        // Horizontal scrolling category chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Simple wraps or columns
                Column {
                    Text(
                        text = "Categories",
                        color = if (dark) TextSecondary else TextSecondaryLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.take(3).forEach { cat ->
                            GlassChip(
                                text = cat,
                                selected = catFilter == cat,
                                onClick = { viewModel.transactionCategoryFilter = cat },
                                darkTheme = dark,
                                activeColor = NeonPurple
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.drop(3).forEach { cat ->
                            GlassChip(
                                text = cat,
                                selected = catFilter == cat,
                                onClick = { viewModel.transactionCategoryFilter = cat },
                                darkTheme = dark,
                                activeColor = NeonPurple
                            )
                        }
                    }
                }
            }
        }

        // Transaction list or Empty view
        if (filteredTransactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Inbox,
                            contentDescription = "Empty",
                            tint = if (dark) TextMuted else TextSecondaryLight,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No matching cash records found",
                            color = if (dark) TextSecondary else TextSecondaryLight,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        } else {
            items(filteredTransactions) { tx ->
                val accentBorder = if (tx.type == "Money In") NeonGreen else NeonPink
                
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    darkTheme = dark,
                    borderColor = accentBorder.copy(alpha = 0.40f),
                    padding = 12.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (tx.type == "Money In") Icons.Default.KeyboardDoubleArrowDown else Icons.Default.KeyboardDoubleArrowUp,
                                contentDescription = null,
                                tint = accentBorder,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = tx.description,
                                    color = if (dark) TextPrimary else TextPrimaryLight,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (!tx.partyName.isNullOrEmpty()) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(if (dark) Color(0x3310B981) else Color(0x2210B981))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = tx.partyName,
                                                color = if (dark) NeonGreen else Color(0xFF047857),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                                val extraDetails = mutableListOf<String>()
                                if (tx.paymentMethod.isNotEmpty()) extraDetails.add(tx.paymentMethod)
                                if (tx.reference.isNotEmpty()) extraDetails.add("Ref: ${tx.reference}")
                                val extraStr = if (extraDetails.isNotEmpty()) " • " + extraDetails.joinToString(" • ") else ""
                                Text(
                                    text = "${tx.category} • ${tx.date}$extraStr",
                                    color = if (dark) TextSecondary else TextSecondaryLight,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${if (tx.type == "Money In") "+" else "-"}${currencyFormatter.format(tx.amount)}",
                                color = accentBorder,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // Simple quick delete option
                            IconButton(
                                onClick = { viewModel.deleteTransaction(tx) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete",
                                    tint = if (dark) TextMuted else TextSecondaryLight,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
