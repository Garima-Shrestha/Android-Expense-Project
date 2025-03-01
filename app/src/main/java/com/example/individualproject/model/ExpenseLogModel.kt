package com.example.individualproject.model

import android.os.Parcel
import android.os.Parcelable

data class ExpenseLogModel (
    var LogId : String = "",
    var ExpenseAmount : Int = 0,
    var ExpenseCategory : String = "",
    var ExpenseDate : String = "",
    var ExpensePurpose: String ="",
) : Parcelable{
    constructor(parcel: Parcel) : this(
        parcel.readString()?: "",
        parcel.readInt()?: 0,
        parcel.readString()?: "",
        parcel.readString()?: "",
        parcel.readString()?: ""
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(LogId)
        parcel.writeInt(ExpenseAmount)
        parcel.writeString(ExpenseCategory)
        parcel.writeString(ExpenseDate)
        parcel.writeString(ExpensePurpose)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ExpenseLogModel> {
        override fun createFromParcel(parcel: Parcel): ExpenseLogModel {
            return ExpenseLogModel(parcel)
        }

        override fun newArray(size: Int): Array<ExpenseLogModel?> {
            return arrayOfNulls(size)
        }
    }

}