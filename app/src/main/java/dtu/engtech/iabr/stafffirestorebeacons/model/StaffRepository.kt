package dtu.engtech.iabr.stafffirestorebeacons.model

interface StaffRepository {
    abstract val staff: List<StaffMember>
    fun getStaffMember(staffID: String)
    fun addListener()
}