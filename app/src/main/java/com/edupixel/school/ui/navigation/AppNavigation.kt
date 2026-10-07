package com.edupixel.school.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.edupixel.school.ui.screens.approvals.ApprovalsScreen
import com.edupixel.school.ui.screens.attendance.AttendanceScreen
import com.edupixel.school.ui.screens.books.BooksScreen
import com.edupixel.school.ui.screens.classes.ClassesScreen
import com.edupixel.school.ui.screens.dashboard.DashboardScreen
import com.edupixel.school.ui.screens.reports.ReportsScreen
import com.edupixel.school.ui.screens.results.AnnualResultsScreen
import com.edupixel.school.ui.screens.results.MidtermResultsScreen
import com.edupixel.school.ui.screens.scores.ScoreEntryScreen
import com.edupixel.school.ui.screens.schools.SchoolsScreen
import com.edupixel.school.ui.screens.settings.SettingsScreen
import com.edupixel.school.ui.screens.studentdetail.StudentDetailScreen
import com.edupixel.school.ui.screens.students.StudentsScreen
import com.edupixel.school.ui.screens.subjects.SubjectsScreen
import com.edupixel.school.ui.screens.years.AcademicYearsScreen

data class NavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
)

val primaryNavItems = listOf(
    NavItem(Screen.Dashboard.route, "Dashboard", Icons.Outlined.Dashboard),
    NavItem(Screen.Classes.route, "Classes", Icons.Outlined.Class),
    NavItem(Screen.Students.route, "Students", Icons.Outlined.People),
    NavItem(Screen.MidtermResults.route, "Midterm", Icons.Outlined.Assessment),
    NavItem(Screen.AnnualResults.route, "Annual", Icons.Outlined.School)
)

val moreNavItems = listOf(
    NavItem(Screen.Schools.route, "Schools", Icons.Outlined.School),
    NavItem(Screen.AcademicYears.route, "Academic Years", Icons.Outlined.CalendarMonth),
    NavItem(Screen.Subjects.route, "Subjects", Icons.Outlined.MenuBook),
    NavItem(Screen.Scores.route, "Score Entry", Icons.Outlined.EditNote),
    NavItem(Screen.Attendance.route, "Attendance", Icons.Outlined.CalendarToday),
    NavItem(Screen.Reports.route, "Reports", Icons.Outlined.Summarize),
    NavItem(Screen.Books.route, "Textbooks", Icons.Outlined.AutoStories),
    NavItem(Screen.Approvals.route, "Approvals", Icons.Outlined.Verified),
    NavItem(Screen.Settings.route, "Settings", Icons.Outlined.Settings)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val configuration = LocalConfiguration.current
    val isWideScreen = configuration.screenWidthDp >= 720

    var showMoreSheet by remember { mutableStateOf(false) }

    Row(modifier = Modifier.fillMaxSize()) {
        // Navigation Rail for Tablets / Wide screens
        if (isWideScreen) {
            NavigationRail(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                modifier = Modifier.fillMaxHeight()
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                val allItems = primaryNavItems + moreNavItems
                allItems.forEach { item ->
                    NavigationRailItem(
                        selected = currentRoute == item.route,
                        onClick = {
                            if (currentRoute != item.route) {
                                navController.navigate(item.route) {
                                    popUpTo(Screen.Dashboard.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) }
                    )
                }
            }
        }

        Scaffold(
            bottomBar = {
                if (!isWideScreen) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    ) {
                        primaryNavItems.forEach { item ->
                            val selected = currentRoute == item.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    if (currentRoute != item.route) {
                                        navController.navigate(item.route) {
                                            popUpTo(Screen.Dashboard.route) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = { Icon(item.icon, contentDescription = item.title) },
                                label = { Text(item.title) }
                            )
                        }

                        // "More" navigation tab
                        val isMoreSelected = moreNavItems.any { it.route == currentRoute }
                        NavigationBarItem(
                            selected = isMoreSelected,
                            onClick = { showMoreSheet = true },
                            icon = { Icon(Icons.Outlined.MoreHoriz, contentDescription = "More") },
                            label = { Text("More") }
                        )
                    }
                }
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = Screen.Dashboard.route,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                composable(Screen.Dashboard.route) {
                    DashboardScreen(
                        onNavigateToSchools = { navController.navigate(Screen.Schools.route) },
                        onNavigateToClasses = { navController.navigate(Screen.Classes.route) },
                        onNavigateToStudents = { navController.navigate(Screen.Students.route) },
                        onNavigateToMidterm = { navController.navigate(Screen.MidtermResults.route) },
                        onNavigateToAnnual = { navController.navigate(Screen.AnnualResults.route) },
                        onNavigateToScores = { navController.navigate(Screen.Scores.route) },
                        onNavigateToAttendance = { navController.navigate(Screen.Attendance.route) },
                        onNavigateToReports = { navController.navigate(Screen.Reports.route) }
                    )
                }

                composable(Screen.Schools.route) {
                    SchoolsScreen(
                        onNavigateToClasses = { navController.navigate(Screen.Classes.route) },
                        onNavigateToYears = { navController.navigate(Screen.AcademicYears.route) }
                    )
                }

                composable(Screen.AcademicYears.route) {
                    AcademicYearsScreen(
                        onNavigateToSchools = { navController.navigate(Screen.Schools.route) }
                    )
                }

                composable(Screen.Classes.route) {
                    ClassesScreen(
                        onNavigateToSchools = { navController.navigate(Screen.Schools.route) },
                        onNavigateToStudents = { navController.navigate(Screen.Students.route) },
                        onNavigateToSubjects = { navController.navigate(Screen.Subjects.route) }
                    )
                }

                composable(Screen.Subjects.route) {
                    SubjectsScreen(
                        onNavigateToClasses = { navController.navigate(Screen.Classes.route) }
                    )
                }

                composable(Screen.Students.route) {
                    StudentsScreen(
                        onNavigateToClasses = { navController.navigate(Screen.Classes.route) },
                        onStudentClick = { studentId ->
                            navController.navigate(Screen.StudentDetail.createRoute(studentId))
                        }
                    )
                }

                composable(
                    route = Screen.StudentDetail.route,
                    arguments = listOf(navArgument("studentId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val studentId = backStackEntry.arguments?.getInt("studentId") ?: 0
                    StudentDetailScreen(
                        studentId = studentId,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(Screen.Scores.route) {
                    ScoreEntryScreen(
                        onNavigateToClasses = { navController.navigate(Screen.Classes.route) }
                    )
                }

                composable(Screen.Attendance.route) {
                    AttendanceScreen(
                        onNavigateToClasses = { navController.navigate(Screen.Classes.route) }
                    )
                }

                composable(Screen.MidtermResults.route) {
                    MidtermResultsScreen(
                        onNavigateToClasses = { navController.navigate(Screen.Classes.route) },
                        onStudentClick = { studentId ->
                            navController.navigate(Screen.StudentDetail.createRoute(studentId))
                        }
                    )
                }

                composable(Screen.AnnualResults.route) {
                    AnnualResultsScreen(
                        onNavigateToClasses = { navController.navigate(Screen.Classes.route) },
                        onStudentClick = { studentId ->
                            navController.navigate(Screen.StudentDetail.createRoute(studentId))
                        }
                    )
                }

                composable(Screen.Reports.route) {
                    ReportsScreen(
                        onNavigateToClasses = { navController.navigate(Screen.Classes.route) }
                    )
                }

                composable(Screen.Books.route) {
                    BooksScreen(
                        onNavigateToClasses = { navController.navigate(Screen.Classes.route) }
                    )
                }

                composable(Screen.Approvals.route) {
                    ApprovalsScreen(
                        onNavigateToClasses = { navController.navigate(Screen.Classes.route) }
                    )
                }

                composable(Screen.Settings.route) {
                    SettingsScreen()
                }
            }
        }
    }

    if (showMoreSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMoreSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "MORE MODULES",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                moreNavItems.forEach { item ->
                    NavigationDrawerItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = currentRoute == item.route,
                        onClick = {
                            showMoreSheet = false
                            navController.navigate(item.route) {
                                popUpTo(Screen.Dashboard.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
