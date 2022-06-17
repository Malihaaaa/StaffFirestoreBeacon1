package dtu.engtech.iabr.stafffirestorebeacons.ui


import androidx.compose.runtime.Composable

@Composable
fun StaffScreen(


    staffViewModel: StaffViewModel
) {
    StaffCardList(
        list = staffViewModel.staffRepository.staff
    )

}