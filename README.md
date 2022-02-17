# SeKt

Pure kotlin async selenium/webdriver client

# Status

Features:

* [ ] Page object support
* [x] Handling StaleReferenceException
* [x] Strict models (kotlinx.serialization)
* [ ] More sugar
* [ ] Support for DevTools
* [ ] Docs

Protocols:

* [x] W3C
* [ ] Appium
* [ ] SauceLab
* [ ] BrowserStack

Http clients:

* [x] Ktor

Platforms (not multiplatform yet):

* [x] Jvm
* [ ] Js
* [ ] Native

# Usage

```kotlin
       val webdriver = webdriver(
            "http://localhost:4444",
            listOf(
                w3cConverter()
            )
        )

        val session = webdriver.session(
            DefaultSessionCreator,

            capabilities
            {
                browserName = "firefox"
                platformName = "linux"
                browserVersion = "92.0"

            }) {
            setUrl(PageUrl("http://google.com"))
            findElement(xpath("//input[@name = 'q']")).sendKeys(Text("I want to found something"))
            findElement(xpath("//input[@name = 'btnK']")).click()
                //...
        }

```

# Contributing

1. Fork
2. Make changes
3. Create PR
