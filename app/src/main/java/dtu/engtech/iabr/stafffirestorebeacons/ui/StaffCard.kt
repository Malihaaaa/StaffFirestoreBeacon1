package dtu.engtech.iabr.stafffirestorebeacons.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dtu.engtech.iabr.stafffirestorebeacons.model.StaffMember

@Composable
fun StaffCard(
    staffMember: StaffMember,
    modifier: Modifier = Modifier
) {

        Column() {
            Text(
                text = staffMember?.id ?: ""
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = staffMember?.navn ?: "",
                modifier = Modifier.padding(all = 4.dp),
                style = MaterialTheme.typography.body2
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = staffMember?.lokation ?: "",
                modifier = Modifier.padding(all = 4.dp),
                style = MaterialTheme.typography.body2
            )
        }

}