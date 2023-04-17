## epubby

epubby is a framework for working with the EPUB file format for Kotlin and Java.

## Known Problems & Non Spec Compliance

- In EPUB3 contexts, if a `meta` element with an unknown property is encountered, that property is *never* kept in any
  way, and is simply skipped. This means that when writing the file out again, some elements may be lost.
- The [collection](https://www.w3.org/publishing/epub3/epub-packages.html#elemdef-collection) element is not supported
  in any manner. `Epub` instances will internally retain any read `collection` elements, so they will not be lost upon
  saving, but at the moment `collection` is no exposed in any way to the user.
- When creating the virtual file system for the `Epub` instance, the attributes of the files from the original epub file
  are currently not copied over.
- The system only supports using one `rootfile` element as the actual source of info for an `Epub` at the moment. The element needs to have a `media-type` value of `application/oebps-package+xml`. 