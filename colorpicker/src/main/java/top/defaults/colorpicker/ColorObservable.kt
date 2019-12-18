package top.defaults.colorpicker

interface ColorObservable {
    fun subscribe(observer: ColorObserver?)
    fun unsubscribe(observer: ColorObserver?)
    val color: Int
}