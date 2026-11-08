package pope.project.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSUserDefaults
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UIKit.UIViewController
import platform.darwin.NSObject
import platform.posix.memcpy

private object UserDefaultsStore : KeyValueStore {
    override fun getString(key: String): String? = NSUserDefaults.standardUserDefaults.stringForKey(key)
    override fun putString(key: String, value: String) {
        NSUserDefaults.standardUserDefaults.setObject(value, forKey = key)
    }
}

@Composable
actual fun rememberKeyValueStore(): KeyValueStore = UserDefaultsStore

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    val result = ByteArray(size)
    if (size > 0) result.usePinned { memcpy(it.addressOf(0), bytes, length) }
    return result
}

private class PickerDelegate(
    private val onResult: (ByteArray?) -> Unit,
) : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {

    override fun imagePickerController(
        picker: UIImagePickerController,
        didFinishPickingMediaWithInfo: Map<Any?, *>,
    ) {
        val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
        val data = image?.let { UIImageJPEGRepresentation(it, 0.7) }
        picker.dismissViewControllerAnimated(true, null)
        onResult(data?.toByteArray())
    }

    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        picker.dismissViewControllerAnimated(true, null)
        onResult(null)
    }
}

private fun topViewController(): UIViewController? {
    var top = UIApplication.sharedApplication.keyWindow?.rootViewController
    while (top?.presentedViewController != null) top = top.presentedViewController
    return top
}

@Composable
actual fun rememberCameraLauncher(onResult: (ByteArray?) -> Unit): CameraLauncher {
    val currentOnResult by rememberUpdatedState(onResult)
    val delegate = remember { PickerDelegate { currentOnResult(it) } }
    return remember {
        CameraLauncher {
            val cameraType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
            val picker = UIImagePickerController().apply {
                sourceType = if (UIImagePickerController.isSourceTypeAvailable(cameraType)) cameraType
                else UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
                this.delegate = delegate
            }
            topViewController()?.presentViewController(picker, animated = true, completion = null)
        }
    }
}

/** Status bar style is configured in Info.plist (light content over the navy header). */
@Composable
actual fun SystemBarsAppearance(isNight: Boolean) = Unit
