## epubby

epubby is a framework for working with the EPUB file format for Kotlin and Java.

## Known Problems

- In EPUB3 contexts, if a `meta` element with an unknown property is encountered, that property is *never* kept in any
  way, and is simply skipped. This means that when writing the file out again, some elements may be lost.
- The [collection](https://www.w3.org/publishing/epub3/epub-packages.html#elemdef-collection) element is not supported
  in any manner. `Epub` instances will internally retain any read `collection` elements, so they will not be lost upon
  saving, but at the moment `collection` is no exposed in any way to the user.
- When creating the virtual file system for the `Epub` instance, the attributes of the files from the original epub file
  are currently not copied over.
- Only one root file is supported at the moment, the system will not panic if there are multiple `rootfile` entries, but
  only the *first* `rootfile` element with a `media-type` of `application/oebps-package+xml` will be used as the "root"
  of the returned `Epub` instance, all others will simply be ignored. If no such `rootfile` exists then the reading will
  fail. Proper support for multiple `rootfile` elements is not planned, and will most likely not happen.