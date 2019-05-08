# Add project specific ProGuard rules here.
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

-ignorewarnings

# Specifies to write out the entire configuration that has been parsed to the file app/build/proguard-merged-config.txt.
# It includes LocusMaps Android SDK's own locuslabs-proguard-rules.pro which were exposed via the Gradle property consumerProguardFiles
# For more information see https://android.maps.locuslabs.com/v2.0/docs/frequently-asked-questions#section-what-exceptions-does-locusmaps-android-sdk-require-for-proguard-
-printconfiguration build/proguard-merged-config.txt