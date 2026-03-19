package com.apiscall.skeletoncode.workpermitmodule.data.local.datasource


import com.apiscall.skeletoncode.workpermitmodule.domain.models.ApprovalAction
import com.apiscall.skeletoncode.workpermitmodule.domain.models.ApprovalHistory
import com.apiscall.skeletoncode.workpermitmodule.domain.models.Attachment
import com.apiscall.skeletoncode.workpermitmodule.domain.models.FieldType
import com.apiscall.skeletoncode.workpermitmodule.domain.models.FormField
import com.apiscall.skeletoncode.workpermitmodule.domain.models.Notification
import com.apiscall.skeletoncode.workpermitmodule.domain.models.NotificationPriority
import com.apiscall.skeletoncode.workpermitmodule.domain.models.NotificationType
import com.apiscall.skeletoncode.workpermitmodule.domain.models.Permit
import com.apiscall.skeletoncode.workpermitmodule.domain.models.PermitStatus
import com.apiscall.skeletoncode.workpermitmodule.domain.models.PermitType
import com.apiscall.skeletoncode.workpermitmodule.domain.models.Role
import com.apiscall.skeletoncode.workpermitmodule.domain.models.User
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockDataSource @Inject constructor() {

    private val users = mutableListOf<User>()
    private val permits = mutableListOf<Permit>()
    private val notifications = mutableListOf<Notification>()
    private val permitForms = mutableMapOf<PermitType, List<FormField>>()

    init {
        initializeUsers()
        initializePermits()
        initializeNotifications()
        initializePermitForms()
    }

    private fun initializeUsers() {
        users.addAll(
            listOf(
                User(
                    id = "1",
                    username = "supervisor",
                    email = "supervisor.p1@ptw.local",
                    fullName = "John Supervisor",
                    role = Role.SUPERVISOR,
                    department = "Production",
                    employeeId = "EMP001"
                ),
                User(
                    id = "2",
                    username = "admin",
                    email = "admin@ptw.local",
                    fullName = "Admin User",
                    role = Role.ADMIN,
                    department = "Administration",
                    employeeId = "EMP002"
                ),
                User(
                    id = "3",
                    username = "requestor",
                    email = "requestor.p1@ptw.local",
                    fullName = "Requestor User",
                    role = Role.REQUESTOR,
                    department = "Projects",
                    employeeId = "EMP003"
                ),
                User(
                    id = "4",
                    username = "issuer",
                    email = "issuer.p1@ptw.local",
                    fullName = "Issuer User",
                    role = Role.ISSUER,
                    department = "EHS",
                    employeeId = "EMP004"
                ),
                User(
                    id = "5",
                    username = "ehs",
                    email = "ehs.p1@ptw.local",
                    fullName = "EHS Officer",
                    role = Role.EHS_OFFICER,
                    department = "EHS",
                    employeeId = "EMP005"
                ),
                User(
                    id = "6",
                    username = "areaowner",
                    email = "areaowner.p1@ptw.local",
                    fullName = "Area Owner",
                    role = Role.AREA_OWNER,
                    department = "Maintenance",
                    employeeId = "EMP006"
                ),
                User(
                    id = "7",
                    username = "worker",
                    email = "worker@ptw.local",
                    fullName = "Worker User",
                    role = Role.WORKER,
                    department = "Field Operations",
                    employeeId = "EMP007"
                )
            )
        )
    }

    private fun initializePermits() {
        val now = Date()
        val calendar = Calendar.getInstance()

        permits.addAll(
            listOf(
                Permit(
                    id = "1",
                    permitNumber = "PTW-2024-001",
                    permitType = PermitType.HOT_WORK,
                    title = "Welding at Reactor Area",
                    description = "Hot work permit for welding repairs at reactor R-101",
                    status = PermitStatus.PENDING_ISSUER_APPROVAL,
                    requester = users[0],
                    location = "Reactor Area - Unit A",
                    startDate = Date(now.time + 86400000), // tomorrow
                    endDate = Date(now.time + 172800000), // day after tomorrow
                    createdAt = now,
                    updatedAt = now,
                    formData = mapOf(
                        "workDescription" to "Welding of support brackets",
                        "equipment" to "Welding machine #W123",
                        "fireWatchman" to "Required",
                        "gasTest" to "Pending"
                    ).toString(),
                    attachments = listOf(
                        Attachment(
                            id = "1",
                            fileName = "risk_assessment.pdf",
                            filePath = "/mock/ra.pdf",
                            fileType = "application/pdf",
                            fileSize = 1024,
                            uploadedBy = users[0],
                            uploadedAt = now
                        )
                    ),
                    approvalHistory = listOf(
                        ApprovalHistory(
                            id = "1",
                            permitId = "1",
                            action = ApprovalAction.SUBMITTED,
                            user = users[0],
                            timestamp = now,
                            comments = "Permit submitted for approval"
                        )
                    )
                ),
                Permit(
                    id = "2",
                    permitNumber = "PTW-2024-002",
                    permitType = PermitType.CONFINED_SPACE,
                    title = "Tank Cleaning - Tank T-205",
                    description = "Confined space entry for cleaning tank T-205",
                    status = PermitStatus.APPROVED,
                    requester = users[4],
                    issuer = users[1],
                    areaOwner = users[3],
                    ehsOfficer = users[2],
                    location = "Tank Farm - Tank T-205",
                    startDate = Date(now.time + 43200000), // 12 hours from now
                    endDate = Date(now.time + 259200000), // 3 days from now
                    createdAt = Date(now.time - 86400000), // yesterday
                    updatedAt = Date(now.time - 43200000), // 12 hours ago
                    formData = mapOf(
                        "gasTestResult" to "Pass",
                        "oxygenLevel" to "20.9%",
                        "ventilation" to "Forced air in place",
                        "attendant" to "Assigned"
                    ).toString(),
                    approvalHistory = listOf(
                        ApprovalHistory(
                            id = "2",
                            permitId = "2",
                            action = ApprovalAction.SUBMITTED,
                            user = users[4],
                            timestamp = Date(now.time - 86400000),
                            comments = "Please approve"
                        ),
                        ApprovalHistory(
                            id = "3",
                            permitId = "2",
                            action = ApprovalAction.APPROVED,
                            user = users[1],
                            timestamp = Date(now.time - 72000000),
                            comments = "Approved with conditions"
                        ),
                        ApprovalHistory(
                            id = "4",
                            permitId = "2",
                            action = ApprovalAction.APPROVED,
                            user = users[3],
                            timestamp = Date(now.time - 64800000),
                            comments = "Area owner approved"
                        ),
                        ApprovalHistory(
                            id = "5",
                            permitId = "2",
                            action = ApprovalAction.APPROVED,
                            user = users[2],
                            timestamp = Date(now.time - 57600000),
                            comments = "EHS approved"
                        )
                    )
                ),
                Permit(
                    id = "3",
                    permitNumber = "PTW-2024-003",
                    permitType = PermitType.WORK_AT_HEIGHT,
                    title = "Pipe Rack Inspection",
                    description = "Work at height for pipe rack inspection at Unit B",
                    status = PermitStatus.ACTIVE,
                    requester = users[0],
                    issuer = users[1],
                    areaOwner = users[3],
                    ehsOfficer = users[2],
                    location = "Pipe Rack PR-200",
                    startDate = Date(now.time - 3600000), // 1 hour ago
                    endDate = Date(now.time + 28800000), // 8 hours from now
                    createdAt = Date(now.time - 172800000), // 2 days ago
                    updatedAt = Date(now.time - 3600000), // 1 hour ago
                    formData = mapOf(
                        "harness" to "Yes",
                        "lanyard" to "Yes",
                        "anchorPoint" to "Certified",
                        "toolLanyard" to "Required"
                    ).toString(),
                    workers = listOf(users[5]),
                    attachments = listOf(
                        Attachment(
                            id = "2",
                            fileName = "safety_harness_inspection.jpg",
                            filePath = "/mock/harness.jpg",
                            fileType = "image/jpeg",
                            fileSize = 2048,
                            uploadedBy = users[5],
                            uploadedAt = Date(now.time - 1800000) // 30 mins ago
                        )
                    )
                )
            )
        )
    }

    private fun initializeNotifications() {
        val now = Date()
        notifications.addAll(
            listOf(
                Notification(
                    id = "1",
                    title = "New Permit Request",
                    message = "Hot work permit #PTW-2024-001 requires your approval",
                    type = NotificationType.NEW_PERMIT_REQUEST,
                    permitId = "1",
                    isRead = false,
                    createdAt = Date(now.time - 3600000), // 1 hour ago
                    priority = NotificationPriority.HIGH
                ),
                Notification(
                    id = "2",
                    title = "Permit Approved",
                    message = "Confined space permit #PTW-2024-002 has been fully approved",
                    type = NotificationType.PERMIT_APPROVAL,
                    permitId = "2",
                    isRead = true,
                    createdAt = Date(now.time - 86400000), // yesterday
                    priority = NotificationPriority.MEDIUM
                ),
                Notification(
                    id = "3",
                    title = "Permit Expiring Soon",
                    message = "Work at height permit #PTW-2024-003 expires in 2 hours",
                    type = NotificationType.PERMIT_EXPIRING,
                    permitId = "3",
                    isRead = false,
                    createdAt = Date(now.time - 7200000), // 2 hours ago
                    priority = NotificationPriority.HIGH
                )
            )
        )
    }

    private fun initializePermitForms() {
        permitForms[PermitType.HOT_WORK] = listOf(
            FormField("hw1", "Work Description", FieldType.TEXTAREA, true, order = 1),
            FormField(
                "hw2",
                "Fire Watch Required",
                FieldType.RADIO,
                true,
                options = listOf("Yes", "No"),
                order = 2
            ),
            FormField(
                "hw3",
                "Gas Test Required",
                FieldType.RADIO,
                true,
                options = listOf("Yes", "No"),
                order = 3
            )
        )
        permitForms[PermitType.COLD_WORK] = listOf(
            FormField("cw1", "Work Description", FieldType.TEXTAREA, true, order = 1),
            FormField("cw2", "Tools Used", FieldType.TEXT, false, order = 2)
        )
        permitForms[PermitType.LOTO] = listOf(
            FormField("l1", "Equipment to Lock", FieldType.TEXT, true, order = 1),
            FormField("l2", "Number of Locks", FieldType.NUMBER, true, order = 2)
        )
        permitForms[PermitType.CONFINED_SPACE] = listOf(
            FormField("cs1", "Space Description", FieldType.TEXTAREA, true, order = 1),
            FormField(
                "cs2",
                "Gas Test Result",
                FieldType.DROPDOWN,
                true,
                options = listOf("Pass", "Fail"),
                order = 2
            )
        )
        permitForms[PermitType.WORK_AT_HEIGHT] = listOf(
            FormField("wah1", "Height (meters)", FieldType.NUMBER, true, order = 1),
            FormField(
                "wah2",
                "Harness Used",
                FieldType.CHECKBOX,
                true,
                options = listOf("Yes"),
                order = 2
            )
        )
        permitForms[PermitType.LIFTING] = listOf(
            FormField(
                "lf1",
                "Crane Type",
                FieldType.DROPDOWN,
                true,
                options = listOf("Mobile", "Tower"),
                order = 1
            ),
            FormField("lf2", "Load Weight (tons)", FieldType.NUMBER, true, order = 2)
        )
        permitForms[PermitType.LIVE_EQUIPMENT] = listOf(
            FormField("le1", "Equipment ID", FieldType.TEXT, true, order = 1),
            FormField(
                "le2",
                "Voltage",
                FieldType.DROPDOWN,
                true,
                options = listOf("Low", "Medium", "High"),
                order = 2
            )
        )
    }

    // Data access methods
    fun getUsers(): List<User> = users

    fun getUserById(id: String): User? = users.find { it.id == id }

    fun getUserByUsername(username: String): User? = users.find { it.username == username }

    fun getPermits(): List<Permit> = permits

    fun getPermitById(id: String): Permit? = permits.find { it.id == id }

    fun getPermitsByRequester(requesterId: String): List<Permit> =
        permits.filter { it.requester.id == requesterId }

    fun getPermitsByStatus(status: PermitStatus): List<Permit> =
        permits.filter { it.status == status }

    fun getPendingApprovals(): List<Permit> = permits.filter {
        it.status == PermitStatus.PENDING_ISSUER_APPROVAL ||
                it.status == PermitStatus.PENDING_AREA_OWNER_APPROVAL ||
                it.status == PermitStatus.PENDING_EHS_APPROVAL
    }

    fun getActivePermits(): List<Permit> = permits.filter { it.status == PermitStatus.ACTIVE }

    fun addPermit(permit: Permit) {
        permits.add(permit)
    }

    fun updatePermit(permit: Permit) {
        val index = permits.indexOfFirst { it.id == permit.id }
        if (index != -1) {
            permits[index] = permit
        }
    }

    fun getNotifications(): List<Notification> = notifications

    fun getUnreadNotifications(): List<Notification> = notifications.filter { !it.isRead }

    fun markNotificationAsRead(id: String) {
        val index = notifications.indexOfFirst { it.id == id }
        if (index != -1) {
            notifications[index] = notifications[index].copy(isRead = true)
        }
    }

    fun getPermitForm(permitType: PermitType): List<FormField> =
        permitForms[permitType] ?: emptyList()

    fun addNotification(notification: Notification) {
        notifications.add(0, notification)
    }

    fun getUserByEmail(email: String): User? {
        return users.find { it.email.equals(email, ignoreCase = true) }
    }
}