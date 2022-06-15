package dtu.engtech.iabr.stafffirestorebeacons.ui


import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import dtu.engtech.iabr.stafffirestorebeacons.model.StaffMember
import dtu.engtech.iabr.stafffirestorebeacons.model.StaffRepository
import dtu.engtech.iabr.stafffirestorebeacons.model.StaffRepositoryFirestore

class StaffViewModel: ViewModel() {

    var staffRepository: StaffRepository = StaffRepositoryFirestore()

    private var _staff = staffRepository.staff.toMutableStateList()

    val staff: List<StaffMember>
        get() = _staff

    fun getStaff(staffID: String) {
        staffRepository.getStaffMember(staffID)
    }
    fun setStaff(staffID: String) {
        staffRepository.getStaffMember(staffID)
    }

}
