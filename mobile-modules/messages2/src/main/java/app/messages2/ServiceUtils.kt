package app.messages2

import android.content.ComponentName
import android.content.Context
import android.content.Intent



fun Context.convertImplicitIntentToExplicitIntent(implicitIntent: Intent): Intent? {
    val pm = packageManager
    val resolveInfoList = pm.queryIntentServices(implicitIntent, 0)
    if (resolveInfoList == null || resolveInfoList.size != 1) {
        return null
    }
    val serviceInfo = resolveInfoList[0]
    val component = ComponentName(serviceInfo.serviceInfo.packageName, serviceInfo.serviceInfo.name)
    val explicitIntent = Intent(implicitIntent)
    explicitIntent.component = component
    return explicitIntent
}
