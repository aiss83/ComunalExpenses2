package ru.aiss83.comunalexpenses2.utils

import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import platform.UIKit.popoverPresentationController

/**
 * Top-most view controller of the key window.
 * Walks the `presentedViewController` chain so a second sheet is not presented on a controller
 * that is already covered.
 */
internal fun topViewController(): UIViewController? {
    val window = UIApplication.sharedApplication.keyWindow ?: return null
    var controller = window.rootViewController
    while (controller?.presentedViewController != null) {
        controller = controller.presentedViewController
    }
    return controller
}

/**
 * Presents a share sheet.
 *
 * On iPad `UIActivityViewController` is shown as a popover and must have a source view,
 * otherwise UIKit throws at presentation time. Anchoring it to the presenting view fixes that
 * and is a no-op on iPhone.
 */
@OptIn(ExperimentalForeignApi::class)
internal fun presentShareSheet(activityViewController: UIActivityViewController) {
    val presenter = topViewController() ?: return

    activityViewController.popoverPresentationController?.apply {
        sourceView = presenter.view
        sourceRect = presenter.view.bounds
    }
    presenter.presentViewController(activityViewController, animated = true, completion = null)
}
