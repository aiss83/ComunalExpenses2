package ru.aiss83.comunalexpenses2.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerMode
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIApplication
import platform.darwin.NSObject

/**
 * iOS implementation: native UIDocumentPickerViewController for JSON files.
 * Uses the delegate pattern to read picked file content.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberJsonFilePicker(onFilePicked: (String?) -> Unit): () -> Unit {
    val delegate = remember {
        object : NSObject(), UIDocumentPickerDelegateProtocol {
            @Suppress("UNUSED_PARAMETER")
            @OptIn(BetaInteropApi::class)
            override fun documentPicker(
                controller: UIDocumentPickerViewController,
                didPickDocumentAtURL: NSURL
            ) {
                val nsString = NSString.create(
                    contentsOfURL = didPickDocumentAtURL,
                    encoding = NSUTF8StringEncoding,
                    error = null
                )
                onFilePicked(nsString?.toString())
            }

            @Suppress("UNUSED_PARAMETER")
            override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
                onFilePicked(null)
            }
        }
    }

    return {
        val picker = UIDocumentPickerViewController(
            documentTypes = listOf("public.json"),
            inMode = UIDocumentPickerMode.UIDocumentPickerModeImport
        )
        picker.delegate = delegate
        val rootVC = UIApplication.sharedApplication.keyWindow?.rootViewController
        rootVC?.presentViewController(picker, animated = true, completion = null)
    }
}
