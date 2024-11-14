# GC2ASM

[![Maven Central Version](https://img.shields.io/maven-central/v/fr.ifpen.allotropeconverters/gc2asm)][maven-central]
[![License CeCILL 2.1](https://img.shields.io/badge/License-CeCILL_2.1-green)][license]



A Java converter from GC proprietary data to Allotrope's ASM data

A project from [IFP Energies Nouvelles](https://www.ifpenergiesnouvelles.com/), a public research, innovation and training organization in the fields of energy, transport and the environment

## Usage
```java
        String filePath = pathToGCFile;
        GcToAllotropeJsonConverter converter = new GcToAllotropeJsonConverter();
        ObjectNode result = converter.convertFile(filePath);
```

## Supported files
- Chemstation V179
- Chemstation V181

## Roadmap
Support for Thermo's Chromeleon data.

## License
The code is available under the [CeCILL 2.1](https://cecill.info/licences/Licence_CeCILL_V2.1-fr.txt) licence, which is compatible with GNU GPL, GNU Affero GPL and EUPL.
The [ASM JSON schemas](https://www.allotrope.org/asm) are available under [CC-BY-NC 4.0](https://creativecommons.org/licenses/by-nc/4.0/) terms.

[maven-central]: https://central.sonatype.com/artifact/fr.ifpen.allotropeconverters/gc2asm
[license]: https://opensource.org/license/cecill-2-1
