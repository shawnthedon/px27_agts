// swift-tools-version: 6.0
// The swift-tools-version declares the minimum version of Swift required to build this package.

import PackageDescription

let package = Package(
    name: "Px27PhoneControl",
    platforms: [
        .iOS(.v17),
        .macOS(.v14)
    ],
    products: [
        .library(
            name: "Px27PhoneControl",
            targets: ["Px27PhoneControl"]
        ),
        .executable(
            name: "Px27PhoneControlApp",
            targets: ["Px27PhoneControlApp"]
        )
    ],
    dependencies: [],
    targets: [
        .target(
            name: "Px27PhoneControl",
            dependencies: [],
            path: "Sources/Px27PhoneControl"
        ),
        .executableTarget(
            name: "Px27PhoneControlApp",
            dependencies: ["Px27PhoneControl"],
            path: "Sources/Px27PhoneControlApp"
        ),
        .testTarget(
            name: "Px27PhoneControlTests",
            dependencies: ["Px27PhoneControl"],
            path: "Tests/Px27PhoneControlTests"
        )
    ]
)
