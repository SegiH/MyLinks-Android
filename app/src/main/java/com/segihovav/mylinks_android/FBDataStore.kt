package com.segihovav.mylinks_android

class FBDataStore {
    var Name: String = ""
    var URL: String = ""

    get() = field

    constructor(_Name: String, _URL:String) {
        this.Name=_Name
        this.URL=_URL
    }
}