## epubby

epubby is a framework for working with the EPUB file format for Kotlin and Java.

## Known Problems

- The `TableOfContents`class does not currently properly keep all attributes defined by certain elements in the XHTML nav document for 3.x EPUB files.

## Credits

The `ISBN` class is taken from the [ISBN](https://github.com/kymmt90/ISBN) GitHub repository by [kymmt90](https://github.com/kymmt90), it is available under the Apache 2.0 license.

## Installation

Gradle

- Groovy

  ```groovy
  repositories {
      jcenter()
      maven {
          url "https://dl.bintray.com/kotlin/kotlinx"
          name "kotlinx"
      }
  }
  
  dependencies {
      implementation "moe.kanon.epubby:epubby:LATEST_VERSION"
  }
  ```

- Kotlin

  ```kotlin
  repositories {
      jcenter()
      maven(url = "https://dl.bintray.com/kotlin/kotlinx") { setName("kotlinx") }
  }
  
  dependencies {
      implementation(group = "moe.kanon.epubby", name = "epubby", version = "LATEST_VERSION")
  }
  ```

Maven

```xml
<dependency>
    <groupId>moe.kanon.epubby</groupId>
    <artifactId>epubby</artifactId>
    <version>LATEST_VERSION</version>
    <type>pom</type>
</dependency>

```

## License

````
Copyright 2019 Oliver Berg

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
````