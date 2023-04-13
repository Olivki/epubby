## epubby

epubby is a framework for working with the EPUB file format for Kotlin and Java.

## Known Problems

- In EPUB3 contexts, if a `meta` element with an unknown property is encountered, that property is *never* kept in any
  way, and is simply skipped. This means that when writing the file out again, some elements may be lost.