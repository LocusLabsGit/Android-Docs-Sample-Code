package com.example.locuslabs.recommendedimplementation;

/**
 * This class is for testing Proguard rules, it has no functional use.  It should get obfuscated by Proguard, as described in the case below.
 *
 * Scenario: Proguard rules should not prevent obfuscation of customer's code
 * Given the RecommendedImplementation app
 *   And the developer ran ./gradlew clean build
 * When the developer opens the file ./app/build/outputs/mapping/release/mapping.txt
 * Then the line starting "com.example.locuslabs.recommendedimplementation.ProguardTestClass ->" should say this class was renamed, for example to "com.example.locuslabs.recommendedimplementation.a"
 *   And the file should not say "com.example.locuslabs.recommendedimplementation.ProguardTestClass -> com.example.locuslabs.recommendedimplementation.ProguardTestClass"
 *
 * For more information see https://android.maps.locuslabs.com/v2.0/docs/frequently-asked-questions#section-what-exceptions-does-locusmaps-android-sdk-require-for-proguard-
 *
 * LocusLabs internal source: https://app.asana.com/0/16049761259391/812529377090330/f
 */
public class ProguardTestClass {
    private int avar;

    public ProguardTestClass(int avar) {
        this.avar = avar;
    }

    public int incrementValue() {
        return ++avar;
    }
}
