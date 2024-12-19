# GC2ASM

[![Maven Central Version][maven-central-badge]][maven-central]
[![License CeCILL 2.1][license-badge]][cecill-2.1]

A Java converter from GC proprietary data to Allotrope's ASM data.

A project from [IFP Energies Nouvelles][ifpen], a public research, innovation and
training organization in the fields of energy, transport and the environment.

## Usage

```java
import fr.ifpen.allotropeconverters.gc.GcToAllotropeJsonConverter;
import fr.ifpen.allotropeconverters.gc.chemstation.ChemStationToAllotropeMapper;
import fr.ifpen.allotropeconverters.gc.chemstation.ChemStationToAllotropeMapperBuilder;

String folderPath = "path to folder containing .ch, .xml and .txt files";
String filePath = "path to .ch file";

// Using default mapper
GcToAllotropeJsonConverter converter = new GcToAllotropeJsonConverter();

// Conversion from folder to Allotrope JSON
ObjectNode allotropeFromFolder = converter.convertFolderToAllotrope(folderPath);

// Conversion from .ch file to Allotrope JSON
ObjectNode allotropeFromFile = converter.convertChFileToAllotrope(filePath);


// ChemStation to Allotrope mapper can be customized
ChemStationToAllotropeMapperBuilder builder = new ChemStationToAllotropeMapperBuilder();
ChemStationToAllotropeMapper mapper = builder.withZoneId(ZoneId.of("Europe/Paris"))
                                             .withChFileName("file.ch")
                                             .withXmlFileName("file.xml")
                                             .build();
GcToAllotropeJsonConverter customizedConverter = new GcToAllotropeJsonConverter(mapper);

// Convert folders and files
// [...]
```

## Supported files

- Chemstation V179
- Chemstation V181

## Roadmap

Support for Thermo's Chromeleon data.

## License

The code is available under the [CeCILL 2.1][cecill-2.1] license, 
which is compatible with GNU GPL, GNU Affero GPL and EUPL.  
The [ASM JSON schemas][asm] are available under [CC-BY-NC 4.0][cc-by-nc-4.0] terms.

[//]: # (@formatter:off)

[maven-central-badge]: https://img.shields.io/maven-central/v/fr.ifpen.allotropeconverters/gc2asm
[license-badge]: https://img.shields.io/badge/License-CeCILL_2.1-green

[maven-central]: https://central.sonatype.com/artifact/fr.ifpen.allotropeconverters/gc2asm
[cecill-2.1]: https://opensource.org/license/cecill-2-1
[ifpen]: https://www.ifpenergiesnouvelles.com/
[asm]: https://www.allotrope.org/asm
[cc-by-nc-4.0]: https://creativecommons.org/licenses/by-nc/4.0/
