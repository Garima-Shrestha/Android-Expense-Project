package com.example.individualproject.model

import android.os.Parcel
import android.os.Parcelable

data class ExpenseCalculatorModel (
    val id: String = "",
    val calExpenseAmount: Double = 0.0,
    val calExpenseDescription: String = "",
    val calExpenseDate: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()?: "",
        parcel.readDouble()?: 0.0,
        parcel.readString()?: "",
        parcel.readString()?: ""
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeDouble(calExpenseAmount)
        parcel.writeString(calExpenseDescription)
        parcel.writeString(calExpenseDate)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ExpenseCalculatorModel> {
        override fun createFromParcel(parcel: Parcel): ExpenseCalculatorModel {
            return ExpenseCalculatorModel(parcel)
        }

        override fun newArray(size: Int): Array<ExpenseCalculatorModel?> {
            return arrayOfNulls(size)
        }
    }
}
