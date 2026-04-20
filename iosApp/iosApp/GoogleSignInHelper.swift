//
// Created by James Willian on 19/04/26.
//

import Foundation
import GoogleSignIn
import UIKit
import ComposeApp

@objc public class GoogleSignInProviderIos: NSObject, GoogleSignInProvider {

    public func signIn() async throws -> String {
        return try await withCheckedThrowingContinuation { continuation in
            DispatchQueue.main.async {
                guard let rootVC = Self.topViewController() else {
                    continuation.resume(throwing: NSError(
                        domain: "GoogleSignIn",
                        code: -1,
                        userInfo: [NSLocalizedDescriptionKey: "Não foi possível obter a view controller raiz"]
                    ))
                    return
                }

                GIDSignIn.sharedInstance.signIn(withPresenting: rootVC) { result, error in
                    if let error = error {
                        continuation.resume(throwing: error)
                        return
                    }
                    guard let idToken = result?.user.idToken?.tokenString else {
                        continuation.resume(throwing: NSError(
                            domain: "GoogleSignIn",
                            code: -2,
                            userInfo: [NSLocalizedDescriptionKey: "ID token não disponível"]
                        ))
                        return
                    }
                    continuation.resume(returning: idToken)
                }
            }
        }
    }

    private static func topViewController(
        base: UIViewController? = nil
    ) -> UIViewController? {
        let baseVC = base ?? UIApplication.shared.connectedScenes
        .compactMap { $0 as? UIWindowScene }
        .flatMap { $0.windows }
        .first(where: { $0.isKeyWindow })?.rootViewController

        if let nav = baseVC as? UINavigationController {
            return topViewController(base: nav.visibleViewController)
        }
        if let tab = baseVC as? UITabBarController, let selected = tab.selectedViewController {
            return topViewController(base: selected)
        }
        if let presented = baseVC?.presentedViewController {
            return topViewController(base: presented)
        }
        return baseVC
    }
}