package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BorderDark
import com.example.ui.theme.BorderGlass
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.PrimaryViolet
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.NavigationTab

@Composable
fun SolesFinBottomBar(
    currentTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xF012131C), // Frosted glass blur effect
                            Color(0xF50C0D13)
                        )
                    )
                )
                .border(1.dp, BorderGlass, RoundedCornerShape(26.dp))
                .padding(vertical = 8.dp, horizontal = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavItem(
                    icon = Icons.Default.Home,
                    label = "Inicio",
                    isSelected = currentTab == NavigationTab.DASHBOARD,
                    testTag = "nav_tab_dashboard",
                    activeColor = AccentGold,
                    onClick = { onTabSelected(NavigationTab.DASHBOARD) }
                )
                NavItem(
                    icon = Icons.Default.Savings,
                    label = "Metas",
                    isSelected = currentTab == NavigationTab.METAS,
                    testTag = "nav_tab_metas",
                    activeColor = AccentGold,
                    onClick = { onTabSelected(NavigationTab.METAS) }
                )
                NavItem(
                    icon = Icons.Default.PieChart,
                    label = "Presupuesto",
                    isSelected = currentTab == NavigationTab.PRESUPUESTOS,
                    testTag = "nav_tab_presupuestos",
                    activeColor = AccentGold,
                    onClick = { onTabSelected(NavigationTab.PRESUPUESTOS) }
                )
                NavItem(
                    icon = Icons.Default.Assessment,
                    label = "Analítica",
                    isSelected = currentTab == NavigationTab.ANALITICA,
                    testTag = "nav_tab_analitica",
                    activeColor = AccentGold,
                    onClick = { onTabSelected(NavigationTab.ANALITICA) }
                )
            }
        }
    }
}

@Composable
fun SolesFinSideBar(
    currentTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit,
    onOpenProfile: () -> Unit = {},
    onOpenNotifications: () -> Unit = {},
    unreadNotificationCount: Int = 0,
    modifier: Modifier = Modifier
) {
    Surface(
        color = SurfaceDark,
        modifier = modifier
            .width(220.dp)
            .fillMaxHeight()
            .border(1.dp, BorderDark)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // Brand Logo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 24.dp, top = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(EmeraldDark, Color(0xFF064E3B))))
                        .border(1.dp, EmeraldLight.copy(alpha = 0.7f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("BC-BANK", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                    Text("Banca Segura", fontSize = 11.sp, color = EmeraldLight)
                }
            }

            // Navigation Items
            SideNavItem(
                icon = Icons.Default.AccountBalance,
                label = "Inicio / Cuentas",
                isSelected = currentTab == NavigationTab.DASHBOARD,
                testTag = "side_nav_dashboard",
                onClick = { onTabSelected(NavigationTab.DASHBOARD) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            SideNavItem(
                icon = Icons.Default.Savings,
                label = "Metas de Ahorro",
                isSelected = currentTab == NavigationTab.METAS,
                testTag = "side_nav_metas",
                onClick = { onTabSelected(NavigationTab.METAS) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            SideNavItem(
                icon = Icons.Default.PieChart,
                label = "Presupuestos",
                isSelected = currentTab == NavigationTab.PRESUPUESTOS,
                testTag = "side_nav_presupuestos",
                onClick = { onTabSelected(NavigationTab.PRESUPUESTOS) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            SideNavItem(
                icon = Icons.Default.Assessment,
                label = "Analítica Financiera",
                isSelected = currentTab == NavigationTab.ANALITICA,
                testTag = "side_nav_analitica",
                onClick = { onTabSelected(NavigationTab.ANALITICA) }
            )

            Spacer(modifier = Modifier.weight(1f))

            // Quick Access shortcuts
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceCard)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(contentAlignment = Alignment.TopEnd) {
                    IconButton(onClick = onOpenNotifications, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notificaciones", tint = TextSecondary, modifier = Modifier.size(20.dp))
                    }
                    if (unreadNotificationCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(EmeraldPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("$unreadNotificationCount", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                IconButton(onClick = onOpenProfile, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Person, contentDescription = "Mi Perfil", tint = TextSecondary, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun SideNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) EmeraldPrimary.copy(alpha = 0.15f) else Color.Transparent)
            .border(
                1.dp,
                if (isSelected) EmeraldPrimary.copy(alpha = 0.5f) else Color.Transparent,
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .testTag(testTag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) EmeraldLight else TextMuted,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) Color.White else TextSecondary
        )
    }
}

@Composable
private fun NavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    testTag: String,
    activeColor: Color = AccentGold,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .testTag(testTag)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(32.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) activeColor else TextMuted,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) activeColor else TextMuted
        )
    }
}
