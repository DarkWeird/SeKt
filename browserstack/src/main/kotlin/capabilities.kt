package me.darkweird.sekt.browserstack

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.darkweird.sekt.core.Caps
import me.darkweird.sekt.core.capability


object BrowserStackCapabilities {
    var Caps.browserStack: Capabilities by capability("bstack:options")
}


inline fun browserStack(f: Capabilities.() -> Unit): Capabilities {
    val caps = Capabilities()
    f.invoke(caps)
    return caps
}

/**
 * BrowserStack specific capabilities
 */
@Serializable
class Capabilities {
    /**
     * Authorization userName.
     * See https://www.browserstack.com/accounts/settings
     */
    var userName: String? = null

    /**
     * Authorization accessKey.
     * See https://www.browserstack.com/accounts/settings
     */
    var accessKey: String? = null

    /**
     * OS you want to test.
     */
    var os: OSName? = null

    /**
     * OS version you want to test.
     */
    var osVersion: OSVersion? = null

    /**
     * Allows the user to specify a name for a logical group of builds.
     * Default: Untitled Project
     */
    var projectName: String? = null

    /**
     * Allows the user to specify a name for a logical group of tests.
     * Default: Untitled Build
     */
    var buildName: String? = null

    /**
     * Allows the user to specify an identifier for the test run.
     */
    var sessionName: String? = null

    /**
     * Use this capability to test your locally hosted websites on BrowserStack by setting the value to true.
     * To enable access to the local machine you need to setup BrowserStack Local binary.
     * https://www.browserstack.com/docs/automate/selenium/getting-started/java/local-testing
     */
    var local: Boolean? = null

    /**
     * Use this capability to specify the unique Local Testing connection name in your test.
     */
    var localIdentifier: String? = null

    /**
     * Required if you want to generate screenshots at various steps in your test.
     * Default: false
     */
    var debug: Boolean? = null

    /**
     * Required if you want to capture browser console logs at various steps in your test.
     * Console Logs are available for Selenium tests on Desktop Chrome and Mobile Chrome (Android devices).
     * Default: errors
     */
    var consoleLogs: ConsoleLog? = null

    /**
     * Required if you want to capture network logs for your test.
     * Network Logs are supported for all desktop browsers, Android and iOS devices with
     * a few exceptions - IE 10 on any OS; IE 11 on Windows 7 / 8.1 and any browser on MacOS High Sierra and Mojave.
     * Default: false
     * Note: You may experience minor reductions in performance when testing with Network Logs turned on with Desktop sessions.
     */
    var networkLogs: Boolean? = null

    /**
     * Required if you want to capture raw appium logs for your test.
     * Default: true
     */
    var appiumLogs: Boolean? = null

    /**
     * Required if you want to enable video recording during your test.
     * Default: true
     */
    var video: Boolean? = null

    /**
     * Required if you want to enable selenium logs for your desktop browser tests.
     * Default: true
     */
    var seleniumLogs: Boolean? = null

    /**
     * Required if you want to capture telemetry logs for your test.
     * Telemetry Logs are supported for all desktop browsers on any
     * OS except for Windows XP and all MacOS versions below Sierra.
     * Default: false
     * Note: Only Selenium versions 4.0.0-alpha-6 and above are supported.
     */
    var telemetryLogs: Boolean? = null

    /**
     * Use this capability to simulate website and mobile behavior from different locations.
     * Traffic to your website or mobile app will originate
     * from an IP address hosted in the country you have chosen.
     *
     *
     * Note: This capability is available with enterprise plans only.
     */
    var getLocation: GetLocation? = null

    /**
     * Use this capability to run your tests on a custom timezone.
     * Note: The functionality is supported across all Windows, macOS, Android and iOS v13 and above devices.
     * Example:
     * New_York for America/New_York, London for Europe/London, Kolkata for Asia/Kolkata.
     * Set the city name as value.
     */
    var timezone: String? = null

    /**
     * Set the resolution of VM before beginning of your test.
     * Default: 1920x1080
     */
    var resolution: Resolution? = null

    /**
     * Use this capability to set the Selenium WebDriver version in test scripts.
     * Note: Latest jar might not be compatible with older browsers.
     */
    var seleniumVersion: String? = null

    /**
     * Use this capability to mask the data sent or retrieved by certain commands.
     * Default: empty array
     * Note: You can pass multiple commands in a single array, separated by commas.
     */
    var maskCommands: List<MaskCommands>? = null

    /**
     * BrowserStack triggers BROWSERSTACK_IDLE_TIMEOUT error when a session is left idle for more than 90 seconds.
     * This happens as BrowserStack by default waits for the timeout duration for additional steps or commands
     * to run, if we do not receive any command during that time, the session is stopped, changing the session
     * status to TIMEOUT on the Automate dashboard.
     * This capability can be used to modify the timeout value.
     * Default: 90
     * Valid: 0-300
     */
    var idleTimeout: Int? = null

    /**
     * If you use basic authentication in your test cases, the username and password would be visible in text logs.
     * Use this capability to mask those credentials.
     * Default: false
     */
    var maskBasicAuth: Boolean? = null

    /**
     * Use this capability to specify a custom delay between the execution of Selenium commands.
     * Default: 20
     */
    var autoWait: Int? = null

    /**
     * Use this capability to add host entry (/etc/hosts) in remote BrowserStack machine.
     * For example, if you use staging.website.com in test cases but do not have a DNS entry for the domain and
     * the public IP, you can use this capability to add host entry in the machine.
     * Note: Supported only on desktop machines.
     * Format (hosts file): '<IP_Address> <Domain_name>'
     */
    var hosts: String? = null

    /**
     * IE 11 browser uses cached pages when you navigate using the backward or forward browser buttons.
     * You can use this capability to disable the use of cached pages.
     * Valid: 1 and 0
     */
    var bfcache: Byte? = null

    /**
     * Chrome browser v71 and above have changed the way PAC files are supported.
     * Use this capability to enable WSS (WebSocket Secure) connections to work with
     * Network Logs on Chrome browser v71 and above.
     * If you are using localhost in your test, change it to bs-local.com
     * Default: false
     * Note: This capability is only valid for Chrome browsers v71 and above.
     */
    var wsLocalSupport: Boolean? = null

    /**
     * Use this capability to disable cross origin restrictions in Safari.
     * Available for Monterey, Big Sur, Catalina and Mojave.
     * Default: false
     */
    var disableCorsRestrictions: Boolean? = null


    /**
     * Specifies a particular mobile device for the test environment.
     * See at https://www.browserstack.com/list-of-browsers-and-platforms/automate
     */
    var deviceName: String? = null

    /**
     * Use this flag to test on a physical mobile device.
     * Default: false
     */
    var realMobile: Boolean? = null

    /**
     * Use this capability to set the Appium version in your test scripts.
     */
    var appiumVersion: String? = null

    /**
     * Set the screen orientation of mobile device.
     * Default: Portrait
     */
    var deviceOrientation: DeviceOrientation? = null

    /**
     * Required if you want to simulate the custom network condition.
     * Note: The supported operating systems are iOS and Android.
     * Format: "('1000', '1000', '100', '1')" - download speed (kbps), upload speed (kbps), latency (ms), packet loss (%)
     *
     */
    var customNetwork: String? = null // TODO provide custom type

    /**
     * Required if you want to simulate different network conditions.
     * Note: The supported operating systems are iOS and Android.
     * https://www.browserstack.com/docs/automate/selenium/simulate-network-conditions#changing-the-network-profile-while-the-tests-are-running
     */
    var networkProfile: String? = null // TODO provide custom type

    /**
     * Chrome related options.
     */
    var chrome: ChromeOptionsBS? = null

    /**
     * Internet Explorer/Edge related options.
     */
    var ie: IEOptionsBS? = null

    /**
     * Safari related options
     */
    var safari: SafariOptionsBS? = null

    var firefox: FirefoxOptionsBS? = null
}

@Serializable
class SafariOptionsBS {
    /**
     * Safari browser disables pop-ups by default, that is,
     * any action that triggers a popup window is disabled in Safari.
     * Use this capability to enable popups in Safari.
     * Default: false
     */
    var enablePopups: Boolean? = null

    /**
     * Use this capability to enable all cookies in Safari.
     * Default: false
     */
    var allowAllCookies: Boolean? = null

    /**
     * Use this capability to specify the Safari WebDriver version.
     * Default: 2.45
     */
    var driver: String? = null
}

@Serializable
class IEOptionsBS {
    /**
     * Use this capability to disable flash on Internet Explorer.
     * Default: false
     */
    var noFlash: Boolean? = null

    /**
     * Use this capability to set Internet Explorer Compatibility View.
     */
    var compatibility: IECompatiability? = null

    /**
     * Use this capability to specify the IE WebDriver architecture.
     * Default: Browserstack automatically selects the IE WebDriver architecture based on the browser version provided.
     */
    var arch: IEArch? = null

    /**
     * Use this capability to specify the IE WebDriver version.
     * Default: Browserstack automatically selects the IE WebDriver version based on the browser version provided.
     * Valid: "3.13.0", "3.14.0", "3.141.0", "3.141.5", "3.141.59".
     */
    var driver: String? = null

    /**
     * Use this capability to enable the popups in IE and Edge.
     * Default: false
     */
    var enablePopups: Boolean? = null

    /**
     * Set the capability to ‘True’ while using sendKeys on IE 11 browser.
     * Default: false
     * Note: https://www.browserstack.com/docs/automate/selenium/using-sendkeys-on-remote-IE11
     */
    var sendKeys: Boolean? = null
}

@Serializable
enum class IEArch {
    @SerialName("x32")
    X32,
    @SerialName("x64")
    X64
}

/**
 * https://docs.microsoft.com/en-us/previous-versions/windows/internet-explorer/ie-developer/general-info/ee330730(v=vs.85)?redirectedfrom=MSDN#browser_emulation
 */
@Serializable
enum class IECompatiability {
    @SerialName("11001")
    IE_11_EDGE,

    @SerialName("11000")
    IE_11_DOCTYPE,

    @SerialName("10001")
    IE_10_STANDARTS,

    @SerialName("10000")
    IE_10_DOCTYPE,

    @SerialName("9999")
    IE_9_STANDARTS,

    @SerialName("9000")
    IE_9_DOCTYPE,

    @SerialName("8888")
    IE_8_STANDARTS,

    @SerialName("8000")
    IE_8_DOCTYPE,

    @SerialName("7000")
    IE_7_DOCTYPE

}

@Serializable
class ChromeOptionsBS {
    /**
     * Use this capability to specify the chromedriver version.
     * Default: Browserstack automatically selects the chromedriver version based on the browser version provided.
     */
    var driver: String? = null
}


@Serializable
class FirefoxOptionsBS {
    /**
     * Use this capability to specify the version of geckodriver.
     * Default: 0.21.0
     */
    var driver: String? = null
}


@Serializable
enum class DeviceOrientation {
    @SerialName("portrait")
    PORTRAIT,

    @SerialName("landscape")
    LANDSCAPE
}

@Serializable
enum class MaskCommands {

    /**
     * All the text send via sendKeys command will be redacted.
     */
    @SerialName("setValues")
    SET_VALUES,

    /**
     * All the text retrieved via get command will be redacted.
     */
    @SerialName("getValues")
    GET_VALUE,


    /**
     * All the cookies which are set by the addCookie command will be redacted.
     */
    @SerialName("setCookies")
    SET_COOKIES,


    /**
     * All the cookie values obtained using the getCookies and getCookieNamed command will be redacted.
     */
    @SerialName("getCookies")
    GET_COOKIES

}

@Serializable
enum class Resolution {
    _800x600,
    _1024x768,
    _1280x800,
    _1280x1024,
    _1366x768,
    _1440x900,
    _1680x1050,
    _1600x1200,
    _1920x1200,
    _1920x1080,
    _2048x1536
}

@Serializable
enum class GetLocation {
    @SerialName("AR")
    ARGENTINA,

    @SerialName("AU")
    AUSTRALIA,

    @SerialName("AT")
    AUSTRIA,

    @SerialName("BD")
    BANGLADESH,

    @SerialName("BE")
    BELGIUM,

    @SerialName("BR")
    BRAZIL,

    @SerialName("BG")
    BULGARIA,

    @SerialName("CA")
    CANADA,

    @SerialName("CL")
    CHILE,

    @SerialName("CN")
    CHINA,

    @SerialName("CO")
    COLOMBIA,

    @SerialName("HR")
    CROATIA,

    @SerialName("CZ")
    CZECH_REPUBLIC,

    @SerialName("DK")
    DENMARK,

    @SerialName("DO")
    DOMINICAN_REPUBLIC,

    @SerialName("EG")
    EGYPT,

    @SerialName("FI")
    FINLAND,

    @SerialName("FR")
    FRANCE,

    @SerialName("GE")
    GEORGIA,

    @SerialName("DE")
    GERMANY,

    @SerialName("GR")
    GREECE,

    @SerialName("HK")
    HONG_KONG,

    @SerialName("HU")
    HUNGARY,

    @SerialName("IS")
    ICELAND,

    @SerialName("IN")
    INDIA,

    @SerialName("ID")
    INDONESIA,

    @SerialName("IE")
    IRELAND,

    @SerialName("IL")
    ISRAEL,

    @SerialName("IT")
    ITALY,

    @SerialName("JP")
    JAPAN,

    @SerialName("JO")
    JORDAN,

    @SerialName("KE")
    KENYA,

    @SerialName("KW")
    KUWAIT,

    @SerialName("LU")
    LUXEMBOURG,

    @SerialName("MY")
    MALAYSIA,

    @SerialName("MX")
    MEXICO,

    @SerialName("NL")
    NETHERLANDS,

    @SerialName("NZ")
    NEW_ZEALAND,

    @SerialName("NG")
    NIGERIA,

    @SerialName("NO")
    NORWAY,

    @SerialName("OM")
    OMAN,

    @SerialName("PK")
    PAKISTAN,

    @SerialName("PA")
    PANAMA,

    @SerialName("PY")
    PARAGUAY,

    @SerialName("PE")
    PERU,

    @SerialName("PH")
    PHILIPPINES,

    @SerialName("PL")
    POLAND,

    @SerialName("PT")
    PORTUGAL,

    @SerialName("QA")
    QATAR,

    @SerialName("RU")
    RUSSIA,

    @SerialName("SA")
    SAUDI_ARABIA,

    @SerialName("SG")
    SINGAPORE,

    @SerialName("ZA")
    SOUTH_AFRICA,

    @SerialName("KR")
    SOUTH_KOREA,

    @SerialName("ES")
    SPAIN,

    @SerialName("SE")
    SWEDEN,

    @SerialName("CH")
    SWITZERLAND,

    @SerialName("TW")
    TAIWAN,

    @SerialName("TH")
    THAILAND,

    @SerialName("TR")
    TURKEY,

    @SerialName("UA")
    UKRAINE,

    @SerialName("AE")
    UNITED_ARAB_EMIRATES,

    @SerialName("GB")
    UNITED_KINGDOM,

    @SerialName("US")
    UNITED_STATES,

    @SerialName("VN")
    VIETNAM,
}

@Serializable
enum class OSName {
    @SerialName("Windows")
    WINDOWS,

    @SerialName("OS X")
    OSX
}

@Serializable
enum class OSVersion {
    @SerialName("XP")
    WIN_XP,

    @SerialName("7")
    WIN_7,

    @SerialName("8")
    WIN_8,

    @SerialName("8.1")
    WIN_8_1,

    @SerialName("10")
    WIN_10,

    @SerialName("11")
    WIN_11,

    @SerialName("Snow Leopard")
    MAC_SNOW_LEOPARD,

    @SerialName("Lion")
    MAC_LION,

    @SerialName("Mountain Lion")
    MAC_MOUNTAIN_LION,

    @SerialName("Mavericks")
    MAC_MAVERICKS,

    @SerialName("Yosemite")
    MAC_YOSEMITE,

    @SerialName("El Capitan")
    MAC_EL_CAPITAN,

    @SerialName("Sierra")
    MAC_SIERRA,

    @SerialName("High Sierra")
    MAC_HIGH_SIERRA,

    @SerialName("Mojave")
    MAC_MOJAVE,

    @SerialName("Catalina")
    MAC_CATALINA,

    @SerialName("Big Sur")
    MAC_BIG_SUR,

    @SerialName("Monterey")
    MAC_MONTEREY
}

@Serializable
enum class ConsoleLog {
    @SerialName("disable")
    DISABLE,

    @SerialName("errors")
    ERRORS,

    @SerialName("warnings")
    WARNINGS,

    @SerialName("info")
    INFO,

    @SerialName("verbose")
    VERBOSE
}