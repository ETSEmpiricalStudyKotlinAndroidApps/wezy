package com.newcore.wezy.ui.map

import android.app.Application
import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.*
import com.google.android.gms.maps.model.LatLng
import com.newcore.wezy.WeatherApplication
import com.newcore.wezy.services.ReCallService
import com.newcore.wezy.shareprefrances.MLocation
import com.newcore.wezy.ui.AppStateViewModel
import com.newcore.wezy.utils.Constants
import com.newcore.wezy.utils.ViewHelpers
import java.util.*

class MapsViewModel(val app: Application) : AndroidViewModel(app) {
    var locationMutableLiveData = MutableLiveData<MLocation>()

    fun setLocation(latLng: LatLng, appStateViewModel: AppStateViewModel)
        {
            try {
                if (appStateViewModel.hasInternet()) {
                    requestUpdateLocation(latLng,appStateViewModel)
                } else {
                    ReCallService.recall(
                        Constants.GET_ADDRESS_AFTER_INTERNET_BACK,
                        {
                            requestUpdateLocation(latLng,appStateViewModel)
                        },
                        app
                    )
                }
            } catch (t: Throwable) {
                Log.e("GeoCoderError", t.message.toString())
            }
        }


    private  fun requestUpdateLocation(latLng: LatLng,appStateViewModel:AppStateViewModel) {
        try {
            val locale = ViewHelpers
                .returnByLanguage(appStateViewModel.getSettings().language,
                    Locale("ar"),
                    Locale("en")
                )

            val geocoder = Geocoder(app,locale)
            val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)

                if (addresses.isNotEmpty()) {
                    val address = addresses[0]
                    val name = "${address.countryName}, ${address.adminArea}"
                    println("address")
                    println(address)
                    locationMutableLiveData.postValue(MLocation(latLng, name, address.countryName))
                } else {
                    locationMutableLiveData.postValue(MLocation(latLng, null, null))
                }
        } catch (t: Throwable) {
            Log.e("requestUpdateLocation", t.message ?: "")
        }
    }

    // view model factory
    class Factory(private val app: WeatherApplication) : ViewModelProvider.Factory {
       val z=  """ [addressLines=[0:"Sowdari, السودان"],
           |feature=Sowdari,admin=North Kurdufan,
           |sub-admin=Sowdari,locality=null,
           |thoroughfare=null,postalCode=null,
           |countryCode=SD,countryName=السودان,
           |hasLatitude=true,latitude=15.561428499999998,
           |hasLongitude=true,
           |longitude=29.045992700000003,
           |phone=null,
           |url=null,
           |extras=null]"""
           .trimMargin()

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MapsViewModel(app) as T
        }
    }
}

