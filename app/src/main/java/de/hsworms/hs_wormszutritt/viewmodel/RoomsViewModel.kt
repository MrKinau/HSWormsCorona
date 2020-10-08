package de.hsworms.hs_wormszutritt.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RoomsViewModel : ViewModel() {

    var rooms: MutableLiveData<MutableList<String>> = MutableLiveData(mutableListOf())

}