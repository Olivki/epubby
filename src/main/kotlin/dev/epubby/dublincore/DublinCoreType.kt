/*
 * Copyright 2019-2023 Oliver Berg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.epubby.dublincore

/**
 * The nature or genre of the resource.
 *
 * Recommended best practice is to use a controlled vocabulary such as the
 * [DCMI Type Vocabulary](http://dublincore.org/specifications/dublin-core/dcmi-type-vocabulary/#H7). To describe
 * the file format, physical medium, or dimensions of the resource, use the [Format] element.
 */
public interface DublinCoreType : DublinCore, NonRequiredDublinCore